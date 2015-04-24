/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.translator.ci;

import java.io.*;
import java.util.*;

import com.sun.squawk.translator.*;
import com.sun.squawk.translator.ci.ClassFileReader.*;
import com.sun.squawk.translator.ir.*;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.Comparer;
import com.sun.squawk.util.SquawkHashtable;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.util.IntHashtable;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.util.Assert;
import com.sun.squawk.vm.*;
import com.sun.squawk.*;


/**
 * An instance of <code>CodeParser</code> is used to decode the "Code" attribute
 * of a method.
 *
 */
public final class CodeParser implements Context {

    private static final ExceptionHandler[] NO_EXCEPTIONHANDLERS = {};

    /**
     * The method being processed by this parser.
     */
    private final Method method;

    /**
     * The constant pool used by elements of the "Code" attributes to
     * refer to other classes and constants.
     */
    private final ConstantPool constantPool;

    /**
     * The maximum depth of the operand stack at any point during
     * execution of the method.
     */
    private final int maxStack;

    /**
     * The number of local variables allocated upon invocation of the method.
     */
    private final int maxLocals;

    /**
     * The exception handlers for the method.
     */
    private final ExceptionHandler[] exceptionHandlers;

    /**
     * The number of instructions seen whose address corresponds with the
     * start_pc, end_pc or handler_pc of a handler. Upon method scanning completion, this
     * number must be eaxctly 3 for each handler.
     */
    private final byte[] exceptionHandlerInstructionAddressesSeen;

    /**
     * The reader used to read the parts of the code attribute including the
     * bytecode array.
     */
    private ClassFileReader cfr;

    /**
     * The input stream used to read the bytecode array that supports querying
     * the current read position.
     */
    private final IndexedInputStream bcin;

    /**
     * SquawkVector of pseudo opcodes used in getLastPseudoOpcodes().
     */
    private SquawkVector pseudoOpcodes = new SquawkVector();

    /**
     * Track how many stackmaps have been read.
     * Zero is technically a legal value (implies a "default" stackmap), but 1 is the common case.
     */
    private int stackmaps;
    
    private final boolean trace;

    /**
     * Creates a <code>CodeReader</code> instance to read and decode
     * the bytecode of a given method.
     *
     * @param  translator   the translation context
     * @param  method       the method to be processed
     * @param  code         the data of the "Code" attribute for <code>method</code>
     * @param  constantPool the constant pool used by the elements of the
     *                      "Code" attribute
     */
    public CodeParser(Translator translator, Method method, byte[] code, ConstantPool constantPool) {
        this.method = method;
        this.constantPool = constantPool;
        this.trace = Translator.TRACING_ENABLED && Tracer.isTracing("classfile", method.toString());

        Klass klass = method.getDefiningClass();
        String filePath =  ClassFileLoader.getClassFilePath(klass);
        cfr = new ClassFileReader(new ByteArrayInputStream(code), filePath);

        maxStack = cfr.readUnsignedShort("cod-maxStack");
        maxLocals = cfr.readUnsignedShort("cod-maxLocals");

        if (maxLocals < method.getRuntimeParameterTypes(false).length) {
            throw cfr.formatError("the max_locals attribute must be at least as large as the size of the parameters.");
        }

        int codeLength = cfr.readInt("cod-length");

        if (codeLength <= 0) {
            throw cfr.formatError("the value of code_length must be greater than 0");
        } else if (codeLength >= 0xFFFF) {
            throw cfr.formatError("method code longer than 64 KB");
        }

        /*
         * Mark the position at which the bytecodes begin
         */
        bcin = new IndexedInputStream(code, 8, codeLength);
        cfr.skip(codeLength, null);

        /*
         * Read in the exception handlers
         */
        int ehCount = cfr.readUnsignedShort("hnd-handlers");
        if (ehCount != 0) {
            exceptionHandlers = new ExceptionHandler[ehCount];
            exceptionHandlerInstructionAddressesSeen = new byte[ehCount];
            for (int i = 0; i < ehCount; i++) {
                int startPC = cfr.readUnsignedShort("hnd-startPC");
                int endPC = cfr.readUnsignedShort("hnd-endPC");
                int handlerPC = cfr.readUnsignedShort("hnd-handlerPC");
                int catchIndex = cfr.readUnsignedShort("hnd-catchIndex");

                /*
                 * Verify that all the addresses are valid
                 */
                if (startPC >= codeLength || endPC > codeLength || startPC >= endPC || handlerPC >= codeLength) {
                    throw cfr.formatError( "invalid exception handler code range");
                }

                Klass catchType = null;
                if (catchIndex != 0) {
                    catchType = constantPool.getResolvedClass(catchIndex, this);
                    if (!Klass.THROWABLE.isAssignableFrom(catchType)) {
                        throw verifyError("invalid exception handler type");
                    }
                }
                exceptionHandlers[i] = new ExceptionHandler(startPC, endPC, handlerPC, catchType);
            }
        } else {
            exceptionHandlers = NO_EXCEPTIONHANDLERS;
            exceptionHandlerInstructionAddressesSeen = null;
        }

        /*
         * Read in the code attributes
         */
        SquawkVector lvt = null;
        SquawkVector lnt = null;
        IntHashtable sm = null;
        int attributesCount = cfr.readUnsignedShort("cod-attributesCount");
        for (int i = 0; i < attributesCount; i++) {
            Attribute attribute = cfr.openAttribute(constantPool);
            if (attribute.name.equals("StackMap")) {
                if (stackmaps != 0) {
                    cfr.formatError( "multiple stackmap attributes specified for same method");
                }
                stackmaps++;
                sm = StackMap.loadStackMap(this, cfr, constantPool, codeLength);
            } else if (attribute.name.equals("LineNumberTable")) {
                lnt = loadLineNumberTable(codeLength);
            } else if (attribute.name.equals("LocalVariableTable")) {
                lvt = loadLocalVariableTable(translator, codeLength);
            } else {
                attribute.skip();
            }
            attribute.close();
        }
        localVariableTable = lvt;
        lineNumberTable = lnt;
        targets = sm;

        /*
         * Reset the class file reader to now read the bytecode array
         */
        cfr = new ClassFileReader(bcin, filePath);
    }

    /**
     * Gets another parser for the "Code" attribute being parsed by this
     * parser.
     *
     * @param translator  the translation context
     * @return a copy of this parser
     */
    public CodeParser reset(Translator translator) {
        return new CodeParser(translator, method, bcin.getBuffer(), constantPool);
    }


    /*---------------------------------------------------------------------------*\
     *                  Temporary types for uninitialized instances              *
    \*---------------------------------------------------------------------------*/

    /**
     * The table of interned types representing uninitialised objects created
     * by <i>new</i> bytecodes.
     */
    private SquawkHashtable UninitializedObjectClasses;

    /**
     * Gets an <code>UninitializedObjectClass</code> instance representing the
     * type pushed to the stack by a <i>new</i> bytecode.
     *
     * @param   address        the address of the <i>new</i> bytecode
     * @param   uninitializedType  the class specified by the operand of the
     *                         <i>new</i> bytecode
     * @return a type representing the uninitialized object of type
     *                         <code>uninitializedType</code> created by a
     *                         <i>new</i> bytecode at address
     *                         <code>address</code>
     */
    public UninitializedObjectClass getUninitializedObjectClass(int address, Klass uninitializedType) {
        if (UninitializedObjectClasses == null) {
            UninitializedObjectClasses = new SquawkHashtable();
        }
        String name = "-new@" + address + "-";
        UninitializedObjectClass klass = (UninitializedObjectClass)UninitializedObjectClasses.get(name);
        if (klass == null) {
            klass = new UninitializedObjectClass(name, uninitializedType);
            UninitializedObjectClasses.put(name, klass);
        } else if (!klass.hasInitializedTypeBeenSet()) {
            klass.setInitializedType(uninitializedType);
        }
        return klass;
    }

    /**
     * Verifies that the instruction offset in any ITEM_NewObject stack map entry corresponds with
     * a 'new' instruction.
     */
    public void verifyUninitializedObjectClassAddresses() {
        if (UninitializedObjectClasses != null) {
            for (Enumeration e = UninitializedObjectClasses.elements(); e.hasMoreElements(); ) {
                if (!((UninitializedObjectClass)e.nextElement()).hasInitializedTypeBeenSet()) {
                    throw verifyError("invalid 'new' instruction address in stackmap entry");
                }
            }
        }
    }

    /*---------------------------------------------------------------------------*\
     *             Positions for LocalVariableTable and LineNumberTable          *
    \*---------------------------------------------------------------------------*/

    /**
     * The table of positions within the bytecodes that are referenced by the
     * "LocalVariableTable" and "LineNumberTable" attributes.
     */
    private IntHashtable positions;

    /**
     * Gets a <code>Position</code> instance representing a logical
     * instruction position whose physical address may change as the
     * bytecodes are transformed from JVM bytecodes to Squawk bytecodes.
     *
     * @param   address  the address of the position in the JVM bytecode
     * @return  the relocatable position denoted by <code>address</code>
     */
    private Position getPosition(int address) {
        if (positions == null) {
            positions = new IntHashtable();
        }
        Position position = (Position)positions.get(address);
        if (position == null) {
            position = new Position(address);
            positions.put(address, position);
        }
        return position;
    }

    /**
     * Gets the bytecode positions in a sorted array.
     *
     * @return the bytecode positions in a sorted array
     */
    private Position[] sortPositions() {
        Assert.that(positions != null);
        Position[] sorted = new Position[positions.size()];
        Enumeration e = positions.elements();
        for (int i = 0; i != sorted.length; ++i) {
            sorted[i] = (Position)e.nextElement();
        }
        Arrays.sort(sorted, new Comparer() {
            public int compare(Object o1, Object o2) {
                return ((Position)o1).getBytecodeOffset() - ((Position)o2).getBytecodeOffset();
            }
        });
        return sorted;
    }


    /*---------------------------------------------------------------------------*\
     *                            LocalVariableTable                             *
    \*---------------------------------------------------------------------------*/


    /**
     * The (optional) local variable table for the method.
     */
    private final SquawkVector localVariableTable;

    /**
     * Load the "LocalVariableTable" attribute the code parser is
     * currently positioned at.
     *
     * @param  translator    the translation context
     * @param  codeLength    the length of the bytecode array for the
     *                       enclosing method
     * @return the table as a vector of <code>LocalVariableTableEntry</code>
     */
    private SquawkVector loadLocalVariableTable(Translator translator, int codeLength) {
        int count = cfr.readUnsignedShort("lvt-localVariableTableLength");
        SquawkVector table = new SquawkVector(count);
        for (int i = 0; i != count; ++i) {
            int start_pc = cfr.readUnsignedShort("lvt-startPC");
            if (start_pc >= codeLength) {
                throw cfr.formatError("start_pc of LocalVariableTable is out of range");
            }
            int length = cfr.readUnsignedShort("lvt-length");
            if (start_pc+length > codeLength) {
                throw cfr.formatError("start_pc+length of LocalVariableTable is out of range");
            }
            String name = constantPool.getUtf8(cfr.readUnsignedShort("lvt-nameIndex"));
            String desc = constantPool.getUtf8(cfr.readUnsignedShort("lvt-descriptorIndex"));
            Klass type = Klass.getClass(desc, true);
            int index = cfr.readUnsignedShort("lvt-index");
            Position start = getPosition(start_pc);
            Position end = getPosition(start_pc + length);
            table.addElement(new LocalVariableTableEntry(start, end, name, type, index));
        }
        return table;
    }

    /**
     * Handles the allocation of a local variable by updating the local variable
     * table (if it exists) to record the correlation between the relevant
     * local variable table entry and the allocated local.
     *
     * @param address  the bytecode address at which the local variable was allocated
     * @param local    the allocated local variable
     */
    public void localVariableAllocated(int address, Local local) {
        if (localVariableTable != null) {
            SquawkVector table = localVariableTable;
            for (Enumeration e = table.elements(); e.hasMoreElements();) {
                LocalVariableTableEntry lve = (LocalVariableTableEntry)e.nextElement();
                if (lve.matches(local.getJavacIndex(), address)) {
                    lve.setLocal(local);
                }
            }
        }
    }

    /**
     * Returns an array of scoped local variables.
     *
     * @return the array or null if the local variable information is unavailable
     */
    public ScopedLocalVariable[] getLocalVariableTable() {
        ScopedLocalVariable[] table = null;
        if (localVariableTable != null) {
            int paramSlotCount = method.getRuntimeParameterTypes(true).length;
            int length = 0;
            for (Enumeration e = localVariableTable.elements(); e.hasMoreElements();) {
                LocalVariableTableEntry entry = (LocalVariableTableEntry)e.nextElement();
                if (entry.getSlot(paramSlotCount) != -1) {
                    ++length;
                }
            }

            table = new ScopedLocalVariable[length];
            int i = 0;
            for (Enumeration e = localVariableTable.elements(); e.hasMoreElements();) {
                LocalVariableTableEntry entry = (LocalVariableTableEntry)e.nextElement();
                int start = entry.getStart().getBytecodeOffset();
                int lth   = entry.getEnd().getBytecodeOffset() - start;
                int slot  = entry.getSlot(paramSlotCount);
                if (slot != -1) {
                    ScopedLocalVariable local = new ScopedLocalVariable(entry.getName(),
                        entry.getType(),
                        slot,
                        start,
                        lth);
                    table[i++] = local;
                }
            }
        }
        return table;
    }

    /**
     * Gets an enumeration over all the entries in the local variable debug table.
     *
     * @return  an enumeration of <code>LocalVariableTableEntry</code>s or null if there are none
     */
    public Enumeration getLocalVariableTableEntries() {
        return localVariableTable == null ? null : localVariableTable.elements();
    }

    /*---------------------------------------------------------------------------*\
     *                              LineNumberTable                              *
    \*---------------------------------------------------------------------------*/

    /**
     * The (optional) line number table for the method.
     */
    private final SquawkVector lineNumberTable;

    /**
     * Load the "LineNumberTable" attribute the code parser is
     * currently positioned at.
     *
     * @param  codeLength      the length of the bytecode array for the enclosing method
     * @return the line number table encoded as a vector (<code>Position</code>, <code>Integer</code>)
     *                       pairs representing the IP and source line number respectively
     */
    private SquawkVector loadLineNumberTable(int codeLength) {
        int length = cfr.readUnsignedShort("lin-lineNumberTableLength");
        SquawkVector table = new SquawkVector(length * 2);
        for (int i = 0; i < length; ++i) {
            int pc = cfr.readUnsignedShort("lnt-startPC");
            if (pc >= codeLength) {
                throw cfr.formatError(method + ": " + "start_pc of LineNumberTable is out of range");
            }
            int sourceLine = cfr.readUnsignedShort("lnt-lineNumber");
            Position position = getPosition(pc);
            table.addElement(position);
            table.addElement(new Integer(sourceLine));
        }

        return table;
    }

    /**
     * Gets the number of the source line whose implementation includes a given opcode address.
     *
     * @param  address  the opcode address
     * @return the number of the source line whose implementation includes <code>address</code>
     */
    public int getSourceLineNumber(int address) {
        int lno = -1;
        if (lineNumberTable != null) {
            Enumeration e = lineNumberTable.elements();
            while (e.hasMoreElements()) {
                Position position = (Position)e.nextElement();
                if (position.getBytecodeOffset() > address) {
                    break;
                }
                lno = ((Integer)e.nextElement()).intValue();
            }
        }
        return lno;
    }

    /**
     * Gets the line number table for this method encoded as an integer array.
     * The high 16-bits of each entry in the array is an address in the
     * bytecodes and the low 16-bits is the number of the source line whose
     * implementation starts at that address.
     *
     * @return  the encoded line number table or null if there is no such
     *          table for this method
     */
    public int[] getLineNumberTable(byte[] code) {
        int[] table = null;
        if (lineNumberTable != null) {
            int[] emptyStackOffsets = getEmptyStackOffsets(code);
            table = new int[lineNumberTable.size() / 2];
            Enumeration e = lineNumberTable.elements();
            int i = 0; // index into 'table'
            int j = 0; // index into 'emptyStackOffsets'
            while (e.hasMoreElements()) {
                Position position = (Position)e.nextElement();
                int lineNo = ((Integer)e.nextElement()).intValue();
                int address;
                if (position.getPrevious() instanceof Catch) {
                    address = position.getPrevious().getBytecodeOffset();
                } else {
                    address = position.getBytecodeOffset();
                }

                // Adjust the address forward to the next instruction where
                // the stack is empty before its execution. This is required
                // as the interpreter cannot execute the method that registers
                // a breakpoint event with anything on the operand stack.
                while (emptyStackOffsets[j] < address) {
                    if (j != emptyStackOffsets.length - 1) {
                        ++j;
                    } else {
                        break;
                    }
                }
                if (address != emptyStackOffsets[j]) {
//String msg = method.toString() + ": bumped lnt offset from " + address + " to " + emptyStackOffsets[j];
//System.err.println(msg);
                    address = emptyStackOffsets[j];
                }

                Assert.that((address & 0xFFFF) == address, "address overflow");
                Assert.that((lineNo & 0xFFFF) == lineNo, "line number overflow");
                Assert.that(address != 0, "cannot set a breakpoint at an extend instruction");
                table[i++] = (address << 16) | lineNo;
            }
            Assert.that(i == table.length);
        }
        return table;
    }
    /**
     * Executes the effect of an instruction on the operand stack.
     *
     * @param  opcode  an instruction opcode
     * @param  stack   an operand stack pointer
     * @return the value of <code>stack</code> after the instruction has been executed
     * @throws IndexOutOfBoundsException if <code>opcode</code> is not valid
     */
    public static int applyOperandStackEffect(int opcode, int stack) {

        String ose = OperandStackEffect.getEffect(opcode);
        boolean popping = true;
        int slotsPerLong = Klass.SQUAWK_64 ? 1 : 2;
        for (int i = 0; i != ose.length(); ++i) {
            char ch = ose.charAt(i);
            switch (ch) {
                case ':': {
                    popping = false;
                    break;
                }
                case 'F':
                case 'I':
                case 'W':
                case 'O': {
                    if (popping) {
                        --stack;
                    } else {
                        ++stack;
                    }
                    break;
                }
                case 'L':
                case 'D': {
                    if (popping) {
                        stack -= slotsPerLong;
                    } else {
                        stack += slotsPerLong;
                    }
                    break;
                }
                case '*': {
                    Assert.that(popping);
                    // Clear stack and seek to ':'
                    stack = 0;
                    while (i + 1 != ose.length() && ose.charAt(i + 1) != ':') {
                        ++i;
                    }
                    break;
                }
                default: {
                    Assert.shouldNotReachHere();
                }
            }
         }
         return stack;
     }


    /**
     * Builds a list of all the instructions in a method where the stack is empty
     * before the instruction is executed.
     *
     * @return int[]
     */
    public static int[] getEmptyStackOffsets(byte[] code) {
        Assert.always(code.length < Character.MAX_VALUE);
        StringBuffer buf = new StringBuffer(code.length / 2);

        int stack = 0;
        for (int ip = 0; ip < code.length; ) {

            if (stack == 0) {
                buf.append((char)ip);
            }

            int opcode = fetchUByte(code, ip);
            int extra = 0;

            // Handle prefixes first
            switch (opcode) {
                case OPC.WIDE_M1:
                case OPC.WIDE_0:
                case OPC.WIDE_1:
                    extra = 1;
                    opcode = fetchUByte(code, ip + 1) + OPC.Properties.WIDE_DELTA;
                    break;
                case OPC.WIDE_SHORT:
                    extra = 2;
                    opcode = OPC.Properties.WIDE_DELTA + fetchUByte(code, ip + 1);
                    break;
                case OPC.WIDE_INT:
                    extra = 4;
                    opcode = OPC.Properties.WIDE_DELTA + fetchUByte(code, ip + 1);
                    break;
                case OPC.ESCAPE:
                    extra = 1;
                    opcode = fetchUByte(code, ip + 1) + 256;
                    break;
                case OPC.ESCAPE_WIDE_M1:
                case OPC.ESCAPE_WIDE_0:
                case OPC.ESCAPE_WIDE_1:
                    extra = 1;
                    opcode = fetchUByte(code, ip + 1) + 256 + OPC.Properties.ESCAPE_WIDE_DELTA;
                    break;
                case OPC.ESCAPE_WIDE_SHORT:
                    extra = 2;
                    opcode = OPC.Properties.ESCAPE_WIDE_DELTA + fetchUByte(code, ip + 1);
                    break;
                case OPC.ESCAPE_WIDE_INT:
                    extra = 4;
                    opcode = OPC.Properties.ESCAPE_WIDE_DELTA + fetchUByte(code, ip + 1);
                    break;
            }

            int length;
            if (opcode == OPC.TABLESWITCH_I) {
                length = calculateSwitchInstructionLength(code, ip, 4);
            } else if (opcode == OPC.TABLESWITCH_S) {
                length = calculateSwitchInstructionLength(code, ip, 2);
            } else {
                length = OPC.getSize(opcode);
            }

            Assert.always(length > 0);
            ip += length + extra;
            stack = applyOperandStackEffect(opcode, stack);
        }

        int[] table = new int[buf.length()];
        for (int i = 0; i != table.length; ++i) {
            table[i] = buf.charAt(i);
        }

        return table;

    }

    /**
     * Decodes an unsigned byte from <code>code</code> at <code>ip</code>.
     *
     * @param code  a bytecode stream
     * @param ip    the position in code at which to decoded
     * @return the decoded unsigned byte
     */
    private static int fetchUByte(byte[] code, int ip) {
        return code[ip] & 0xFF;
    }

    /**
     * Decodes an unsigned short from <code>code</code> at <code>ip</code>.
     *
     * @param code  a bytecode stream
     * @param ip    the position in code at which to decoded
     * @return the decoded unsigned short
     */
    private static int fetchShort(byte[] code, int ip) {
        int b1 = code[ip++] & 0xFF;
        int b2 = code[ip++] & 0xFF;
        if (!VM.isBigEndian()) {
            return (short)((b2 << 8) | b1);
        } else {
            return (short)((b1 << 8) | b2);
        }
    }

    /**
     * Decodes an int from <code>code</code> at <code>ip</code>.
     *
     * @param code  a bytecode stream
     * @param ip    the position in code at which to decoded
     * @return the decoded int
     */
    private static int fetchInt(byte[] code, int ip) {
        int b1 = code[ip++] & 0xFF;
        int b2 = code[ip++] & 0xFF;
        int b3 = code[ip++] & 0xFF;
        int b4 = code[ip++] & 0xFF;
        if (!VM.isBigEndian()) {
            return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
        } else {
            return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        }
    }

    private static int calculateSwitchInstructionLength(byte[] code, int ip, int dataSize) {
        int padding = (dataSize - ( (ip + 1) % dataSize)) % dataSize;
        int low;
        int high;
        ip += 1 + padding;
        if (dataSize == 2) {
            low = fetchShort(code, ip);
            high = fetchShort(code, ip + 2);
        } else {
            Assert.that(dataSize == 4);
            low = fetchInt(code, ip);
            high = fetchInt(code, ip + 4);
        }
        int entries = (high - low) + 1;
        return 1 +                   // opcode
               padding +             // alignment padding
               (3 * dataSize) +      // 'low', 'high', 'default' operands
               (entries * dataSize); // table entries
    }

    /*---------------------------------------------------------------------------*\
     *                    Stack map and target entry lookup                      *
    \*---------------------------------------------------------------------------*/

    /**
     * Interned targets.
     */
    private final IntHashtable targets;

    /**
     * Gets the <code>Target</code> instance encapsulating the stack map entry
     * at a bytecode address which must have a stack map entry.
     *
     * @param   address  a bytecode address
     * @return  the object encapsulating the stack map entry at
     *                   <code>address</code>
     * @throws  VerifyError if there is no target at <code>address</code>
     */
    public Target getTarget(int address) {
        if (targets != null) {
            Target target = (Target)targets.get(address);
            if (target != null) {
                return target;
            }
        }
        if (stackmaps == 0) {
            throw verifyError("missing stack map entry for address. Check that class file has been preverified for Java ME: " + cfr.getFileName());
        } else {
            throw verifyError("missing stack map entry for address");
        }
    }

    /**
     * Gets the numbers of targets (i.e. stack maps) in the code.
     *
     * @return the numbers of targets (i.e. stack maps) in the code
     */
    public int getTargetCount() {
        return targets == null ? 0 : targets.size();
    }

    /**
     * Gets an enumeration over all the targets representing the stack map
     * entries in the code.
     *
     * @return  an enumeration of <code>Target</code>s or null if there are none
     */
    public Enumeration getTargets() {
        return targets == null ? null : targets.elements();
    }


    /*---------------------------------------------------------------------------*\
     *                            Pseudo opcodes                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * An instance of <code>PseudoOpcode</code> represents represents a point in
     * the bytecode stream delimiting a range of code protected by an exception
     * handler, the entry point for an exception handler or an explicit target
     * of a control flow instruction.
     */
    public final static class PseudoOpcode {

        /**
         * A pseudo opcode tag constant denoting a point in the bytecode stream
         * where an exception handler becomes active.
         */
        public static final int TRYEND = 0;

        /**
         * A pseudo opcode tag constant denoting a point in the bytecode stream
         * where an exception handler becomes deactive.
         */
        public static final int TRY = 1;

        /**
         * A pseudo opcode tag constant denoting a point in the bytecode stream
         * that is the entry to an exception handler.
         */
        public static final int CATCH = 2;

        /**
         * A pseudo opcode tag constant denoting a point in the bytecode stream
         * that is an explicit target of a control flow instruction.
         */
        public static final int TARGET = 3;

        /**
         * A pseudo opcode tag constant denoting a point in the bytecode stream
         * that is referenced by a LocalVariableTable or LineNumberTable
         * attribute.
         */
        public static final int POSITION = 4;

        /**
         * A tag denoting the semantics of this pseudo opcode.
         */
        private final int tag;

        /**
         * If this is a TRY or TRYEND pseudo opcode, then this is the index
         * of the correpsonding ExceptionHandler in the exception handler
         * table. This index is used to preserve the ordering of the exception
         * handlers.
         */
        private final int index;

        /**
         * If <code>tag</code> is <code>TARGET</code> or <code>CATCH</code>,
         * then this is an instance of {@link Target} otherwise it's an
         * instance of {@link ExceptionHandler} unless <code>tag</code>
         * is <code>POSITION</code> in which case it is an instance of
         * {@link Position}.
         */
        private final Object context;

        PseudoOpcode(int tag, Object context, int index) {
            this.tag     = tag;
            this.context = context;
            this.index   = index;
        }

        /**
         * Gets the constant denoting the semantics of this pseudo opcode.
         *
         * @return {@link #TRY}, {@link #TRYEND}, {@link #CATCH},
         *         {@link #TARGET} or {@link #POSITION}
         */
        public int getTag() {
            return tag;
        }

        /**
         * Gets the string representation of the pseudo opcode
         *
         * @return a string
         */
        public String toString() {
            if (Assert.ASSERTS_ENABLED) {
                String str = null;
                switch (tag) {
                    case TRY:      str = "try";         break;
                    case TRYEND:   str = "tryend";      break;
                    case CATCH:    str = "catch";       break;
                    case TARGET:   str = "target";      break;
                    case POSITION: str = "position";    break;
                    default: Assert.shouldNotReachHere();
                }
                return "["+str +"] index = "+index+" context = "+context;
            } else {
               return super.toString();
            }
        }

        /**
         * Gets an object describing extra information about the point in the
         * bytecode stream corresponding to the object.
         *
         * @return an instance of {@link Target}, {@link ExceptionHandler}
         *         or {@link Position}
         */
        public Object getContext() {
            return context;
        }

        /**
         * This Comparer sorts pseudo instructions at a given address in the
         * following ascending order: TRYEND, TRY, TARGET, CATCH, POSITION. Note
         * that it is impossible to have both a TARGET and a CATCH at the same
         * address. Mutilple TRY pseudo instructions are sorted so that those
         * who's exception table entry is at a higher index in the table come
         * before those whose index in the table is lower. The reverse is
         * true for mutliple TRYEND instructions. This preserves the ordering
         * of the exception handlers expressed in the class file.
         */
        static Comparer COMPARER = new Comparer() {
            public int compare(Object o1, Object o2) {
                if (o1 == o2) {
                    return 0;
                }
                PseudoOpcode po1 = (PseudoOpcode)o1;
                PseudoOpcode po2 = (PseudoOpcode)o2;
                int tag1 = po1.tag;
                int tag2 = po2.tag;
                if (tag1 < tag2) {
                    return -1;
                } else if (tag1 > tag2) {
                    return 1;
                } else {
                    Assert.that(tag1 == TRY || tag1 == TRYEND, "multiple incompatible pseudo opcodes at address");
                    if (tag1 == TRY) {
                        return po2.index - po1.index;
                    } else {
                        return po1.index - po2.index;
                    }
                }
            }
        };
    }

    /**
     * Gets the pseudo opcodes representing exception handler points or
     * branch targets at the address of the last parsed opcode.
     *
     * @return  the pseudo opcodes at this address or null if there are none
     */
    public PseudoOpcode[] getLastPseudoOpcodes() {

        /*
         * Quick test for nothing to do.
         */
        if (exceptionHandlers.length == 0 && targets == null && positions == null) {
            return null;
        }

        int address = lastOpcodeAddress;
        boolean isCatchAddress = false;

        /*
         * Create the pseduo opcodes for the exception handler points (if any)
         */
        if (exceptionHandlers.length != 0) {
            for (int i = 0; i != exceptionHandlers.length; ++i) {
                ExceptionHandler exceptionHandler = exceptionHandlers[i];
                if (exceptionHandler.getEnd() == address) {
                    PseudoOpcode pseudoOpcode = new PseudoOpcode(PseudoOpcode.TRYEND, exceptionHandler, i);
                    pseudoOpcodes.addElement(pseudoOpcode);
                    ++exceptionHandlerInstructionAddressesSeen[i];
                }
                if (exceptionHandler.getStart() == address) {
                    PseudoOpcode pseudoOpcode = new PseudoOpcode(PseudoOpcode.TRY, exceptionHandler, i);
                    pseudoOpcodes.addElement(pseudoOpcode);
                    ++exceptionHandlerInstructionAddressesSeen[i];
                }
                if (exceptionHandler.getHandler() == address) {
                    if (!isCatchAddress) {
                        isCatchAddress = true;
                        PseudoOpcode pseudoOpcode = new PseudoOpcode(PseudoOpcode.CATCH, getTarget(address), -1);
                        pseudoOpcodes.addElement(pseudoOpcode);
                    }
                    ++exceptionHandlerInstructionAddressesSeen[i];
                }
            }
        }

        /*
         * Get the pseudo opcode for the control flow target (if any).
         */
        if (!isCatchAddress && targets != null) {
            Target target = (Target)targets.get(address);
            if (target != null) {
                PseudoOpcode pseudoOpcode = new PseudoOpcode(PseudoOpcode.TARGET, target, -1);
                pseudoOpcodes.addElement(pseudoOpcode);
            }
        }

        /*
         * Get the pseudo opcode for a position (if any).
         */
        if (positions != null) {
            Position position = (Position)positions.get(address);
            if (position != null) {
                PseudoOpcode pseudoOpcode = new PseudoOpcode(PseudoOpcode.POSITION, position, -1);
                pseudoOpcodes.addElement(pseudoOpcode);
            }
        }

        /*
         * Optimize the way the result is returned.
         */
        switch (pseudoOpcodes.size()) {
            case 0: {
                return null;
            }
            case 1: {
                PseudoOpcode res = (PseudoOpcode)pseudoOpcodes.elementAt(0);
                pseudoOpcodes.removeAllElements();
                return new PseudoOpcode[] { res };
            }
            default: {
                PseudoOpcode[] opcodes = new PseudoOpcode[pseudoOpcodes.size()];
                pseudoOpcodes.copyInto(opcodes);

                /*
                 * Sort the pseudo opcodes
                 */
                if (opcodes.length > 1) {
                    Arrays.sort(opcodes, PseudoOpcode.COMPARER);
                }
                pseudoOpcodes.removeAllElements();
                return opcodes;
            }
        }
    }

    public void verifyExceptionHandlerInstructionAddresses() {
        if (exceptionHandlerInstructionAddressesSeen != null) {
            for (int i = 0; i != exceptionHandlerInstructionAddressesSeen.length; ++i) {
                if (exceptionHandlerInstructionAddressesSeen[i] != 3) {
                    throw verifyError("invalid start_pc, end_pc or handler_pc in exception handler");
                }
            }
        }
    }

    /**
     * After
     * @return
     */
    public ExceptionHandler[] getExceptionHandlers() {
        return exceptionHandlers;
    }

    /*---------------------------------------------------------------------------*\
     *                            Max stack & locals                             *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the value of the <code>max_locals</code> item in the class file
     * for this method.
     *
     * @return the value of the <code>max_locals</code> item in the class file
     *         for this method
     */
    public int getMaxLocals() {
        return maxLocals;
    }

    /**
     * Gets the value of the <code>max_stack</code> item in the class file
     * for this method.
     *
     * @return the value of the <code>max_stack</code> item in the class file
     *         for this method
     */
    public int getMaxStack() {
        return maxStack;
    }

    /**
     * Gets the class file method being parsed by this parser.
     *
     * @return the method being parsed by this parser
     */
    public Method getMethod() {
        return method;
    }


    /*---------------------------------------------------------------------------*\
     *                            Bytecode parsing                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Constants denoting array element type for <i>newarray</i>.
     */
    public static final int T_BOOLEAN   = 0x00000004;
    public static final int T_CHAR      = 0x00000005;
    public static final int T_FLOAT     = 0x00000006;
    public static final int T_DOUBLE    = 0x00000007;
    public static final int T_BYTE      = 0x00000008;
    public static final int T_SHORT     = 0x00000009;
    public static final int T_INT       = 0x0000000a;
    public static final int T_LONG      = 0x0000000b;

    /**
     * {@inheritDoc}
     */
    public String prefix(String msg) {
        if (lastOpcodeAddress != -1) {
            int lno = getSourceLineNumber(lastOpcodeAddress);
            if (lno != -1) {
                String sourceFile = method.getDefiningClass().getSourceFileName();
                msg = "@" + lastOpcodeAddress + " (" + sourceFile + ":" + lno + "):\n " + msg;
            } else {
                msg = "@" + lastOpcodeAddress + ":\n " + msg;
            }
        } else {
            msg = " -> " + msg;
        }
        return "while translating " + method + msg;
    }

    /**
     * Throws a LinkageError instance to indicate there was a verification
     * error while processing the bytecode for a method.
     *
     * @param   msg  the cause of the error
     * @return  the LinkageError raised
     */
    public NoClassDefFoundError verifyError(String msg) {
        throw new com.sun.squawk.translator.VerifyError(prefix(msg));
    }

    /**
     * The index in the bytecode array of the last opcode returned by
     * {@link #parseOpcode()}.
     */
    private int lastOpcodeAddress = -1;

    /**
     * Gets the address of the last opcode returned by {@link #parseOpcode()}.
     *
     * @return  the address of the last opcode read
     */
    public int getLastOpcodeAddress() {
        return lastOpcodeAddress;
    }

    /**
     * Gets the current bytecode offset.
     *
     * @return the current bytecode offset
     */
    public int getCurrentIP() {
        return bcin.getCurrentIndex();
    }

    /**
     * Determines whether or not this parser is at the end of the
     * instruction stream.
     *
     * @return true if this parser is at the end of the bytecode stream
     */
    public boolean atEof() {
        if (bcin.available() == 0) {
            lastOpcodeAddress = bcin.getCurrentIndex();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Read an opcode from the bytecode stream. The returned value will be
     * one of the <code>opc_...</code> defined in {@link Opcode}.
     *
     * @return  the opcode read
     */
    public int parseOpcode() {
        lastOpcodeAddress = bcin.getCurrentIndex();
        int opcode = cfr.readUnsignedByte(null);
        if (Translator.TRACING_ENABLED && trace) {
            Tracer.traceln("["+lastOpcodeAddress+"]:opcode:"+Opcode.mnemonics[opcode]);
        }
        return opcode;
    }

    /**
     * Parses a unsigned 8-bit operand.
     *
     * @return the parsed byte value
     */
    public int parseUnsignedByteOperand() {
        return cfr.readUnsignedByte("operand");
    }

    /**
     * Parses a signed 8-bit operand.
     *
     * @return the parsed byte value
     */
    public int parseByteOperand() {
        return cfr.readByte("operand");
    }

    /**
     * Parses a signed 16-bit operand.
     *
     * @return the parsed short value
     */
    public int parseShortOperand() {
        return cfr.readShort("operand");
    }

    /**
     * Parses a signed 32-bit operand.
     *
     * @return the parsed integer value
     */
    public int parseIntOperand() {
        return cfr.readInt("operand");
    }

    /**
     * Parses the operand of an instruction that is an index into the
     * constant pool and returns the object at that index.
     *
     * @param   wide         specifies an 8-bit index if false, 16-bit otherwise
     * @param   longOrDouble specifies if the entry is a <code>long</code> or
     *                       <code>double</code> value
     * @return  the object in the constant pool at the parsed index
     */
    public Object parseConstantPoolOperand(boolean wide, boolean longOrDouble) {
        int index = wide ? cfr.readUnsignedShort("operand") : cfr.readUnsignedByte("operand");
        int tag = constantPool.getTag(index);
        if (longOrDouble) {
            if (tag != ConstantPool.CONSTANT_Long && tag != ConstantPool.CONSTANT_Double) {
                throw verifyError("expected long or double constant");
            }
        } else {
            if (tag != ConstantPool.CONSTANT_Integer && tag != ConstantPool.CONSTANT_Float && tag != ConstantPool.CONSTANT_String) {
                throw verifyError("expected int, float or string constant");
            }
        }
        return constantPool.getEntry(index, tag);
    }

    /**
     * Parses the operand of an instruction that is a field reference (via the
     * constant pool) and returns the referenced field, resolving it first
     * if it has not already been resolved.
     *
     * @param   isStatic  specifies whether the field is static or not
     * @return  the resolved field reference
     */
    public Field parseFieldOperand(boolean isStatic, int opcode) {
        int index = cfr.readUnsignedShort("operand");
        int tag = constantPool.getTag(index);
        if (tag !=  ConstantPool.CONSTANT_Fieldref) {
            throw verifyError("invalid field reference");
        }
        Field field = constantPool.getResolvedField(index, isStatic, this);

        /*
         * Check for assignment to final field from outside class that defines the field
         */
        if (field.isFinal() && (opcode == Opcode.opc_putstatic || opcode == Opcode.opc_putfield) && field.getDefiningClass() != method.getDefiningClass()) {
        	throw new com.sun.squawk.translator.IllegalAccessError(prefix("invalid assignment to final field"));
        }
        return field;
    }

    /**
     * Parses the operand of an instruction that is a method reference (via the
     * constant pool) and returns the referenced method, resolving it first
     * if it has not already been resolved.
     *
     * @param   isStatic     specifies whether the method is static or not
     * @param   invokeinterface  specifies whether or not the instruction is
     *                       <i>invokeinterface</i>
     * @return  the resolved method reference
     */
    public Method parseMethodOperand(boolean isStatic, boolean invokeinterface) {
        int index = cfr.readUnsignedShort("operand");
        int tag = constantPool.getTag(index);
        if (tag != ConstantPool.CONSTANT_Methodref && tag != ConstantPool.CONSTANT_InterfaceMethodref) {
            throw verifyError("invalid method reference");
        }
        return constantPool.getResolvedMethod(index, isStatic, invokeinterface, this);
    }

    /**
     * Parses the operand of a <i>newarray</i> instruction and returns the
     * the type it denotes.
     *
     * @return  the array type denoted by the operand to <i>newarray</i>
     */
    public Klass parseNewArrayOperand() {
        int tag = cfr.readUnsignedByte("operand");
        switch (tag) {
            case T_BOOLEAN: return Klass.BOOLEAN_ARRAY;
            case T_BYTE:    return Klass.BYTE_ARRAY;
            case T_CHAR:    return Klass.CHAR_ARRAY;
            case T_SHORT:   return Klass.SHORT_ARRAY;
            case T_INT:     return Klass.INT_ARRAY;
            case T_LONG:    return Klass.LONG_ARRAY;
/*if[FLOATS]*/
            case T_FLOAT:   return Klass.FLOAT_ARRAY;
            case T_DOUBLE:  return Klass.DOUBLE_ARRAY;
/*else[FLOATS]*/
//            case T_FLOAT:
//            case T_DOUBLE:  throw cfr.formatError("floating point types are not supported in this configuration");
/*end[FLOATS]*/
            default:        throw verifyError("invalid array type");
        }
    }

    /**
     * Parses the operand of a <i>new</i> instruction and returns an
     * instance of <code>UninitializedObjectClass</code> that denotes
     * the type created as well as the address of the instruction.
     *
     * @return  the type denoted by the operand to <i>new</i>
     */
    public UninitializedObjectClass parseNewOperand() {
        int index = cfr.readUnsignedShort("operand");
        Klass type = constantPool.getResolvedClass(index, this);
        if (type.isArray()) {
            throw verifyError("can't create array with new");
        }
        if (type.isInterface() || type.isAbstract()) {
        	throw new com.sun.squawk.translator.InstantiationError(prefix("can't instantiate " + type));
        }
        return getUninitializedObjectClass(lastOpcodeAddress, type);
    }

    /**
     * Parses the operand of an instruction that is a class reference (via the
     * constant pool) and returns the referenced class, resolving it first
     * if it has not already been resolved.
     *
     * @return  the class denoted by the operand to <i>new</i>
     */
    public Klass parseClassOperand() {
        int index = cfr.readUnsignedShort("operand");
        return constantPool.getResolvedClass(index, this);
    }

    /**
     * Parses the operand to a local variable instruction.
     *
     * @param   wide         specifies an 8-bit index if false, 16-bit otherwise
     * @param   longOrDouble specifies if the type of the local variable is
     *                       <code>long</code> or <code>double</code>
     * @return  the index of the local variable denoted by the operand
     */
    public int parseLocalVariableOperand(boolean wide, boolean longOrDouble) {
        int index = wide ? cfr.readUnsignedShort("operand") : cfr.readUnsignedByte("operand");
        int adjust = longOrDouble ? 1 : 0;
        if (index+adjust >= maxLocals) {
            throw verifyError("invalid local variable index");
        }
        return index;
    }

    /**
     * Parses the offset operand to a control flow instruction.
     *
     * @param   wide   specifies a 16-bit index if false, 32-bit otherwise
     * @return the parsed offset
     */
    public Target parseBranchOperand(boolean wide) {
        int offset = wide ? cfr.readInt("operand") : cfr.readShort("operand");
        int address = lastOpcodeAddress + offset;
        return getTarget(address);
     }

     /**
      * Parse the 0-3 zero padded bytes after a <i>tableswitch</i> or
      * <i>lookupswitch</i> instruction.
      */
     public void parseSwitchPadding() {
         while (bcin.getCurrentIndex() % 4 != 0) {
             int ch = cfr.readUnsignedByte("operand");
             if (ch != 0) {
                 throw verifyError("tableswitch/lookupswitch instruction not padded with 0's");
             }
         }
     }
}
