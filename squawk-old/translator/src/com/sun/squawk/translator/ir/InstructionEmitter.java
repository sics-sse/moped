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

package com.sun.squawk.translator.ir;

import com.sun.squawk.util.Assert;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.translator.NoSuchMethodError;
import com.sun.squawk.translator.NoSuchFieldError;
import com.sun.squawk.translator.Translator;
import com.sun.squawk.translator.ClassFile;
import com.sun.squawk.translator.ci.ConstantPool;
import com.sun.squawk.translator.ir.instr.*;

import com.sun.squawk.vm.*;

import java.util.Hashtable;

import com.sun.squawk.*;
import com.sun.squawk.translator.ObjectTable;

/**
 * This in an instruction visitor implementation that will emit Squawk bytecodes.
 *
 */
public class InstructionEmitter implements InstructionVisitor {

/*if[J2ME.STATS]*/
    /**
     * Trace counters.
     */
    static private int trace_methods, trace_static_methods, trace_clearingExtends, trace_slotsCleared, trace_clinit;

    /**
     * Trace slot clearing histogram limit.
     */
    final static int TRACE_HIST_COUNT = 15;

    /**
     * Trace counters.
     */
    static private int[] trace_clearingHistogram = new int[TRACE_HIST_COUNT];
/*end[J2ME.STATS]*/

    /**
     * The trace flag.
     */
    private boolean trace;

    /**
     * The number of methods processed.
     */
    private static int methodsProcessed;

    /**
     * The number of passes.
     */
    private static int passes;

    /**
     * The table of int globals allocated.
     */
    private static Hashtable intGlobals = Global.getGlobalInts();

    /**
     * The table of address globals allocated.
     */
    private static Hashtable addrGlobals = Global.getGlobalAddrs();

    /**
     * The table of oop reference globals allocated.
     * This table is an inverse of VM.getGlobalOop().
     */
    private static Hashtable oopGlobals;

/*if[SUITE_VERIFIER]*/
    private static String oopGlobalReverse[];
/*end[SUITE_VERIFIER]*/

    static {
        oopGlobals = Global.getGlobalOops();
/*if[SUITE_VERIFIER]*/
        oopGlobalReverse = new String[oopGlobals.size()];
        java.util.Enumeration e = oopGlobals.keys();
        while (e.hasMoreElements()) {
            String name = (String)e.nextElement();
            int index = ((Integer)oopGlobals.get(name)).intValue();
            Assert.that(index >= 0 && index < oopGlobalReverse.length && oopGlobalReverse[index] == null);
            oopGlobalReverse[index] = name;
        }
/*end[SUITE_VERIFIER]*/
    }

    /**
     * The IR to be emitted.
     */
    private IR ir;

    /**
     * Squawk bytecode array.
     */
    private byte[] code;

/*if[TYPEMAP]*/
    /**
     * The type map describing the type of the value (if any) written to memory by each instruction in 'code'.
     */
    private byte[] typeMap;

    /**
     * The type that the instruction currently being emitted writes to a local or pushes to the stack.
     */
    private byte resultType;
/*end[TYPEMAP]*/

    /**
     * The number of local variables (after the first one) that need clearing
     */
    private final int clearedSlots;

    /**
     * Current count of bytes written.
     */
    private int count;

    /**
     * The class file for the method being converted.
     */
    private final ClassFile classFile;

    /**
     * The method of the ir.
     */
    private final Method method;

    /**
     * The emitter execution states.
     */
    private final static int NONE  = 0,
                             INIT  = 1,
                             CHECK = 2,
                             EMIT  = 3;

    /**
     * The emitter state
     */
    private int state = NONE;

    /**
     * Flag specifying that an instruction emitting pass subsequent to the first one is required.
     */
    private boolean needAnotherPass;

    /**
     * Flag to show if the method being converted is considered an application class.
     */
    private boolean isAppClass = true;
    
    final static String SYSTEM_CLASS_PREFIX = "com.sun.squawk.";

    /**
     * A system class is a class whose methods must not be interrupted by thread
     * reschedule (which implies can't be interrupted by GC).
     * A system class must be part of the bootstrap suite.
     * System classes are not preemptable, so long running system methods must call Thread.yield()
     * every so often at safe points.
     * 
     * @return true if klass is a system class
     */
    private static boolean isSystemClass(Klass klass) {
        String name = klass.getInternalName();
        if (name.startsWith(SYSTEM_CLASS_PREFIX) 
                  || name.startsWith("java.")
                  || name.startsWith("com.sun.")
                //|| name.startsWith("javax.")
                //|| name.startsWith("tests.")
                ) {
            // System.out.println("Found system class: " + name);
            return true;
        }
        // System.out.println("Found APP class: " + name);
        return false;
    }

    /**
     * Constructor.
     *
     * @param ir
     * @param classFile the class file for the method being converted
     * @param method    the method of the ir
     * @param clearedSlots the number of local variables (after the first one) that need clearing
     */
    InstructionEmitter(IR ir, ClassFile classFile, Method method, int clearedSlots) {
        this.ir           = ir;
        this.classFile    = classFile;
        this.method       = method;
        this.clearedSlots = clearedSlots;
        this.trace        = Translator.TRACING_ENABLED && Tracer.isTracing("emitter", method.toString());

        if (VM.getCurrentIsolate().getLeafSuite().isBootstrap()) {
            isAppClass = !isSystemClass(classFile.getDefinedClass());
        }
    }

    /**
     * Visit all the Squawk bytecodes.
     *
     * @return the number of bytecodes emitted
     */
    private int visitAll() {

        /*
         * Trace.
         */
        InstructionTracer tracer = null;
        if (trace) {
            Tracer.traceln("");
            Tracer.traceln("++++ InstructionEmitter trace for " + method + " ++++");
            tracer = new InstructionTracer(ir);
        }

        /*
         * Clear the byte counter and emit an EXTEND instruction.
         */
        count = 0;
        if (clearedSlots > 0) {
            emitOpcode(OPC.EXTEND);  // TEMP -- for slow vm only
            emit(clearedSlots);
        } else {
            emitOpcode(OPC.EXTEND0); // TEMP -- for slow vm only
        }

/*if[J2ME.STATS]*/
        /*
         * Trace slot clearing info
         */
        trace_methods++;
        if (method.isStatic()) {
            trace_static_methods++;
        }
        if (clearedSlots > 0) {
            trace_clearingExtends++;
        }
        if (clearedSlots >= TRACE_HIST_COUNT) {
            trace_clearingHistogram[TRACE_HIST_COUNT-1]++;
        } else {
            trace_clearingHistogram[clearedSlots]++;
        }
        trace_slotsCleared += clearedSlots;
/*end[J2ME.STATS]*/

        /*
         * Emit a CLASS_CLINIT instruction if necessary.
         */
        if (method.requiresClassClinit()) {
            emitOpcode(OPC.CLASS_CLINIT);
/*if[J2ME.STATS]*/
            trace_clinit++;
/*end[J2ME.STATS]*/
        }

        /*
         * Iterate over the IR.
         */
        for (Instruction instruction = ir.getHead() ; instruction != null ; instruction = instruction.getNext()) {
            instruction.setBytecodeOffset(count);
            
            if (trace) {
                tracer.trace(instruction);
            }

/*if[TYPEMAP]*/
            if (typeMap != null && state == EMIT) {
                if (instruction instanceof Mutator) {
                    Mutator mutator = (Mutator)instruction;
                    if (instruction instanceof StackProducer || instruction instanceof LocalVariable) {
                        resultType = getPushOrStoreResultType(mutator.getMutationType());
                    } else {
                        Assert.that(instruction instanceof FieldAccessor || instruction instanceof ArrayStore || instruction instanceof Return);
                        resultType = getFieldOrArrayResultType(mutator.getMutationType());
                    }
                } else {
                    resultType = AddressType.UNDEFINED;
                }
            }
/*end[TYPEMAP]*/
            instruction.visit(this);
        }

        /*
         * Trace.
         */
        if (trace) {
            Tracer.traceln("---- InstructionEmitter trace for " + method + " [state = " + state + " count = " + count + "] ----");
        }

        /*
         * Increment the number of passes made and return the byte count.
         */
        passes++;
        return count;
    }

/*if[J2ME.STATS]*/
    /**
     * Print stats.
     */
    private static String percent(int a, int b) {
        int pcent = (a*100)/b;
        int units = pcent/100;
        pcent -= units*100;
        String res = ""+units+".";
        if (pcent < 10) {
            res += "0";
        }
        return res+pcent;
    }

    /**
     * Print stats.
     */
    public static void printStats() {
        System.out.println("-- Slot clearing stats --");
        System.out.println("Total methods:                " + trace_methods);
        System.out.println("Total static methods:         " + trace_static_methods+" "+percent(trace_static_methods, trace_methods)+" per method");
        System.out.println("Total class_clinits:          " + trace_clinit+" "+percent(trace_clinit, trace_methods)+" per method " +percent(trace_clinit, trace_static_methods)+" per static method");
        System.out.println("Total slots cleared:          " +percent(trace_slotsCleared, trace_methods)+" per method");
        System.out.println("Total clearing extends:       " + trace_clearingExtends);
        System.out.println("Clearing histogram:           ");
        for (int i = 0 ; i < TRACE_HIST_COUNT ; i++) {
            System.out.println("    "+i+" = "+trace_clearingHistogram[i]+" ("+percent(trace_clearingHistogram[i], trace_methods)+")");
        }

        System.out.println();
        System.out.println("-- Emitting --");
        System.out.println("Total methods:                " + methodsProcessed);
        System.out.println("Total emitter passes:         " + passes);
/*if[FLOATS]*/
        System.out.println("Passes/method:                " + ((float)passes/methodsProcessed));
/*end[FLOATS]*/
    }
/*end[J2ME.STATS]*/

    /**
     * Emit the Squawk bytecodes.
     */
    void emit() {

        /*
         * Visit all the instructions to establish the minimum bytecode length.
         */
        needAnotherPass = false;
        state = INIT;
        int last = visitAll();
        int size = last;

        /*
         * Visit all the instructions checking that the length has reached its asymptote.
         */
        if (needAnotherPass) {
            state = CHECK;
            size = visitAll();
            while (size != last) {
                last = size;
                visitAll();
                size = visitAll();
            }
        }

        /*
         * Visit all the instructions producing the final bytecode sequence.
         */
        code = new byte[size];
/*if[TYPEMAP]*/
        if (VM.usingTypeMap()) {
            typeMap = new byte[size];
        }
/*end[TYPEMAP]*/
        state = EMIT;
        last = visitAll();
        Assert.always(last == size);

        methodsProcessed++;

        /*
         * Trace.
         */
        if (trace) {
            Tracer.traceln("**** InstructionEmitter summary -- methodsProcessed = "+methodsProcessed+ " passes = "+passes
/*if[FLOATS]*/
                      + " avg="+((float)passes/methodsProcessed)
/*end[FLOATS]*/
                      + " ****");
        }

    }

    /**
     * Get the table of int globals allocated.
     *
     * @return the table
     */
    public static Hashtable getGlobalIntVariables() {
        return intGlobals;
    }

    /**
     * Get the table of address globals allocated.
     *
     * @return the table
     */
    public static Hashtable getGlobalAddrVariables() {
        return addrGlobals;
    }

    /**
     * Get the table of oop reference globals allocated.
     *
     * @return the table
     */
    public static Hashtable getGlobalOopVariables() {
        return oopGlobals;
    }

/*if[SUITE_VERIFIER]*/
    /**
     * Get the field name of an oop reference global.
     * This method is an inverse mapping to VM.getGlobalOop().
     */
    public static String getGlobalOopVariable(int index) {
        return oopGlobalReverse[index];
    }
/*end[SUITE_VERIFIER]*/

    /**
     * Gets the Squawk bytecode array.
     *
     * @return the array
     */
    byte[] getCode() {
        return code;
    }

/*if[TYPEMAP]*/
    /**
     * Gets the type map describing the type of the value (if any) written to memory by each instruction.
     *
     * @return the type map describing the type of the value (if any) written to memory by each instruction
     */
    byte[] getTypeMap() {
        return typeMap;
    }
/*end[TYPEMAP]*/

    /**
     * Emits a byte.
     *
     * @param b the byte
     */
    private void emit(int b) {
        Assert.that((b&0xFFFFFF00) == 0);
        if (state == EMIT && count < code.length) {
            code[count] = (byte)b;

/*if[TYPEMAP]*/
            // Record the type written to memory by the current instruction
            if (typeMap != null) {
                typeMap[count] = (byte)( (resultType << AddressType.MUTATION_TYPE_SHIFT) | AddressType.BYTECODE);
            }
/*end[TYPEMAP]*/
        }
        count++;
    }

    /**
     * Emit a short.
     *
     * @param s the short
     */
    private void emitShort(int s) {
        if (state == EMIT) {
            if (VM.isBigEndian()) {
                emit((s >> 8 ) & 0xFF);
                emit((s >> 0 ) & 0xFF);
            } else {
                emit((s >> 0 ) & 0xFF);
                emit((s >> 8 ) & 0xFF);
            }
        } else {
            count += 2;
        }
    }

    /**
     * Emit an int.
     *
     * @param i the int
     */
    private void emitInt(int i) {
        if (state == EMIT) {
            if (VM.isBigEndian()) {
                emit((i >> 24) & 0xFF);
                emit((i >> 16) & 0xFF);
                emit((i >> 8 ) & 0xFF);
                emit((i >> 0 ) & 0xFF);
            } else {
                emit((i >> 0 ) & 0xFF);
                emit((i >> 8 ) & 0xFF);
                emit((i >> 16) & 0xFF);
                emit((i >> 24) & 0xFF);
            }
        } else {
            count += 4;
        }
    }

    /**
     * Emit a long.
     *
     * @param l the long
     */
    private void emitLong(long l) {
        if (state == EMIT) {
            if (VM.isBigEndian()) {
                emit((int)(l >> 56) & 0xFF);
                emit((int)(l >> 48) & 0xFF);
                emit((int)(l >> 40) & 0xFF);
                emit((int)(l >> 32) & 0xFF);
                emit((int)(l >> 24) & 0xFF);
                emit((int)(l >> 16) & 0xFF);
                emit((int)(l >> 8 ) & 0xFF);
                emit((int)(l >> 0 ) & 0xFF);
            } else {
                emit((int)(l >> 0 ) & 0xFF);
                emit((int)(l >> 8 ) & 0xFF);
                emit((int)(l >> 16) & 0xFF);
                emit((int)(l >> 24) & 0xFF);
                emit((int)(l >> 32) & 0xFF);
                emit((int)(l >> 40) & 0xFF);
                emit((int)(l >> 48) & 0xFF);
                emit((int)(l >> 56) & 0xFF);
            }
        } else {
            count += 8;
        }
    }

    /**
     * Emit the opcode.
     *
     * @param opcode the opcode
     */
    private void emitOpcode(int opcode) {
        if (opcode > 255) {
            emit(OPC.ESCAPE);
        }
        emit(opcode & 0xFF);
    }

    /**
     * Emits an opcode and temporarily forces its stack effect to be a given type.
     *
     * @param opcode  the opcode to emit
     * @param type    the type of the value that the instruction denoted by opcode writes to memory
     */
    private void emitOpcode(int opcode, byte type) {
/*if[TYPEMAP]*/
        byte savedResultType = resultType;
        resultType = type;
/*end[TYPEMAP]*/
        emitOpcode(opcode);
/*if[TYPEMAP]*/
        resultType = savedResultType;
/*end[TYPEMAP]*/
    }

    /**
     * Emit a wide_0 instruction.
     *
     * @param opcode the opcode
     */
    private void emitWide0Opcode(int opcode) {
        if (opcode < 256) {
            emit(OPC.WIDE_0);
        } else {
            emit(OPC.ESCAPE_WIDE_0);
        }
        emitOpcode(opcode & 0xFF);
    }

    /**
     * Emit a wide_1 instruction.
     *
     * @param opcode the opcode
     */
    private void emitWide1Opcode(int opcode) {
        if (opcode < 256) {
            emit(OPC.WIDE_1);
        } else {
            emit(OPC.ESCAPE_WIDE_1);
        }
        emitOpcode(opcode & 0xFF);
    }

    /**
     * Emit a wide_m1 instruction.
     *
     * @param opcode the opcode
     */
    private void emitWideM1Opcode(int opcode) {
        if (opcode < 256) {
            emit(OPC.WIDE_M1);
        } else {
            emit(OPC.ESCAPE_WIDE_M1);
        }
        emitOpcode(opcode & 0xFF);
    }

    /**
     * Emit a wide_short instruction.
     *
     * @param opcode the opcode
     */
    private void emitWideShortOpcode(int opcode) {
        if (opcode < 256) {
            emit(OPC.WIDE_SHORT);
        } else {
            emit(OPC.ESCAPE_WIDE_SHORT);
        }
        emitOpcode(opcode & 0xFF);
    }

    /**
     * Emit a wide_int instruction.
     *
     * @param opcode the opcode
     */
    private void emitWideIntOpcode(int opcode) {
        if (opcode < 256) {
            emit(OPC.WIDE_INT);
        } else {
            emit(OPC.ESCAPE_WIDE_INT);
        }
        emitOpcode(opcode & 0xFF);
    }

    /**
     * Emit an instruction that has an unsigned immediate parameter.
     *
     * @param opcode  the opcode
     * @param value   the value of the unsigned immediate parameter
     */
    private void emitUnsigned(int opcode, int value) {
        if ((value & 0xFFFFFF00) == 0) {
            emitOpcode(opcode);
            emit(value);
        } else if ((value & 0xFFFFFE00) == 0) {
            emitWide1Opcode(opcode);
            emit(value & 0xFF);
        } else if (value >= 0 && value < 32768) {
            emitWideShortOpcode(opcode);
            emitShort(value);
        } else {
            emitWideIntOpcode(opcode);
            emitInt(value);
        }
    }

    /**
     * Emit a branch instruction.
     *
     * @param opcode       the opcode
     * @param targetOffset the from the start of the method to the target
     * @param operandSize  the number of bytes allowed for the operand (1, 2, 3, or 5)
     * @return             true if the branch would fit into the specified operand size
     */
    private boolean emitBranch(int opcode, int targetOffset, int operandSize) {
        int savePosition = count;
        switch (operandSize) {
            case 1: {                               // Single byte
                emitOpcode(opcode);
                int value = targetOffset - (count + 1);
                if (value >= -128 && value < 128) {
                    emit(value & 0xFF);
                    return true;
                }
                break;
            }
            case 2: {                               // Single byte with a wide_m1, wide_0, or wide_1
                emitWideM1Opcode(opcode);
                int value = targetOffset - (count + 1);
                if (value < 0 && value >= -256) {
                    emit(value & 0xFF);
                    return true;
                }

                // rewind
                count = savePosition;

                emitWide0Opcode(opcode);
                value = targetOffset - (count + 1);
                if (value >= 0 && value < 256) {
                    emit(value & 0xFF);
                    return true;
                }

                // rewind
                count = savePosition;

                emitWide1Opcode(opcode);
                value = targetOffset - (count + 1);
                if (value >= 0 && value < 512) {
                    emit(value & 0xFF);
                    return true;
                }
                break;
            }
            case 3: {                               // Two bytes and a wide_short
                emitWideShortOpcode(opcode);
                int value = targetOffset - (count + 2);
                if (value >= -32768 && value < 32768) {
                    emitShort(value);
                    return true;
                }
                break;
            }
            case 5: {                               // Four bytes and a wide_int
                emitWideIntOpcode(opcode);
                int value = targetOffset - (count + 4);
                emitInt(value);
                return true;
            }
            default: Assert.shouldNotReachHere();
        }
        count = savePosition;
        return false;
    }

    /**
     * Emit an usigned opcode instruction which has a compact form.
     *
     * @param opcode   the opcode
     * @param opcode_0 the first compact opcode
     * @param limit    the number of compact opcodes
     * @param value    the value
     */
    private void emitCompact(int opcode, int opcode_0, int limit, int value) {
        if (value < limit) {
            emitOpcode(opcode_0 + value);
        } else {
            emitUnsigned(opcode, value);
        }
    }

    /**
     * Emit a load integer instruction.
     *
     * @param value the value
     */
    private void emitConstantInt(int value) {
        if (value == -1) {
            emitOpcode(OPC.CONST_M1);
        } else if (value >= 0 && value < 16) {
            emitOpcode(OPC.CONST_0 + value);
        } else if (value >= -128 && value < 128) {
            emitOpcode(OPC.CONST_BYTE);
            emit(value & 0xFF);
        } else if (value >= -32768 && value < 32767) {
            emitOpcode(OPC.CONST_SHORT);
            emitShort(value);
        } else if ((value & 0xFFFF0000) == 0) {
            emitOpcode(OPC.CONST_CHAR);
            emitShort(value);
        } else {
            emitOpcode(OPC.CONST_INT);
            emitInt(value);
        }
    }

    /**
     * Emit a load long instruction.
     *
     * @param value the value
     */
    private void emitConstantLong(long value) {
        if (value >= Integer.MIN_VALUE && value < Integer.MAX_VALUE) {
            emitConstantInt((int)value);
            emitOpcode(OPC.I2L);
        } else {
            emitOpcode(OPC.CONST_LONG);
            emitLong(value);
        }
    }

/*if[FLOATS]*/

    /**
     * Emit a load float instruction.
     *
     * @param value the value
     */
    private void emitConstantFloat(float value) {
        int   value_bits  = Float.floatToIntBits(value);
        int   ivalue      = (int)value;
        float fvalue      = (float)ivalue;
        int   fvalue_bits = Float.floatToIntBits(fvalue);
        if (value_bits == fvalue_bits && ivalue >= -1 && ivalue < 16) {
            emitConstantInt(ivalue);
            emitOpcode(OPC.I2F);
        } else {
            emitOpcode(OPC.CONST_FLOAT);
            emitInt(value_bits);
        }
    }

    /**
     * Emit a load double instruction.
     *
     * @param value the value
     */
    private void emitConstantDouble(double value) {
        long   value_bits  = Double.doubleToLongBits(value);
        int    ivalue      = (int)value;
        double dvalue      = (double)ivalue;
        long   dvalue_bits = Double.doubleToLongBits(dvalue);
        if (value_bits == dvalue_bits) {
            emitConstantInt(ivalue);
            emitOpcode(OPC.I2D);
        } else {
            emitOpcode(OPC.CONST_DOUBLE);
            emitLong(value_bits);
        }
    }

/*end[FLOATS]*/

    /**
     * Emit a load object instruction.
     *
     * @param object the object
     */
    private void emitConstantObject(Object object) {
/*if[TYPEMAP]*/
        byte savedResultType = resultType;
        resultType = AddressType.REF;
/*end[TYPEMAP]*/
        if (object == null) {
            emitOpcode(OPC.CONST_NULL);
        } else {
            try {
                int index = classFile.getObjectTable().getConstantObjectIndex(object, state == EMIT);
                Object o2 = classFile.getDefinedClass().getObject(index);
//                if (!object.equals(o2)) {
//                    byte[] a = (byte[])object;
//                    byte[] b = (byte[])o2;
//                    for (int i = 0; i < a.length; i++) {
//                        System.out.println("a: " + a[i] + " b: " + b[i]);
//                    }
//                }

                Assert.always((object.equals(o2) || ObjectTable.compareIgnoringCount(object, o2) == 0),
                        classFile.getDefinedClass() + " object = '" + object + "' index = " + index  + " object2 = " + o2);
                emitCompact(OPC.OBJECT, OPC.OBJECT_0, 16, index);
            } catch (java.util.NoSuchElementException ex) {
                throw new NoClassDefFoundError("no copy of object in class's object table: " + object);
            }
        }
/*if[TYPEMAP]*/
        resultType = savedResultType;
/*end[TYPEMAP]*/
    }

    /**
     * Emit a load instruction.
     *
     * @param index the local
     */
    private void emitLoad(boolean isParm, boolean isLong, int index) {
        if (isLong) {
            if (isParm) {
                emitUnsigned(OPC.LOADPARM_I2, index);
            } else {
                emitUnsigned(OPC.LOAD_I2, index);
            }
        } else {
            if (isParm) {
                emitCompact(OPC.LOADPARM, OPC.LOADPARM_0, 8, index);
            } else {
                emitCompact(OPC.LOAD, OPC.LOAD_0, 16, index);
            }
        }
    }

    /**
     * Emit a store instruction.
     *
     * @param index the local
     */
    private void emitStore(boolean isParm, boolean isLong, int index) {
        Assert.that(isParm || index != 0, "slot 0 is reserved for method pointer");
        if (isLong) {
            if (isParm) {
                emitUnsigned(OPC.STOREPARM_I2, index);
            } else {
                emitUnsigned(OPC.STORE_I2, index);
            }
        } else {
            if (isParm) {
                emitUnsigned(OPC.STOREPARM, index);
            } else {
                emitCompact(OPC.STORE, OPC.STORE_0, 16, index);
            }
        }
    }

    /**
     * Emit a int or dec instruction.
     *
     * @param index the local
     */
    private void emitIncDec(boolean isInc, boolean isParm, int index) {
        if (isInc) {
            if (isParm) {
                emitUnsigned(OPC.INCPARM, index);
            } else {
                emitUnsigned(OPC.INC, index);
            }
        } else {
            if (isParm) {
                emitUnsigned(OPC.DECPARM, index);
            } else {
                emitUnsigned(OPC.DEC, index);
            }
        }
    }

/*if[TYPEMAP]*/
    /**
     * Gets the type of the value written to a local or pushed to the stack based
     * on the result type of the mutating instruction.
     *
     * @param type   result type of the mutating instruction
     * @return the stack or local variable type corresponding to <code>type</code>
     */
    private byte getPushOrStoreResultType(Klass type) {
        switch (type.getSystemID()) {
            case CID.BOOLEAN:  // fall through
            case CID.BYTE:     // ...
            case CID.SHORT:    // ...
            case CID.CHAR:     // ...
            case CID.FLOAT:    // ...
            case CID.INT:      return AddressType.INT;
            case CID.LONG:     // fall through
            case CID.DOUBLE:   return AddressType.LONG;
            case CID.VOID:     return AddressType.UNDEFINED;
            case CID.OFFSET:   // fall through
            case CID.UWORD:    return AddressType.UWORD;
            case CID.ADDRESS:  // fall through
            default:           return AddressType.REF;
        }
    }

    /**
     * Gets the type of the value written to a field or array element based
     * on the result type of the mutating instruction.
     *
     * @param type   result type of the mutating instruction
     * @return the field or array element type corresponding to <code>type</code>
     */
    private byte getFieldOrArrayResultType(Klass type) {
        switch (type.getSystemID()) {
            case CID.BOOLEAN:  // fall through
            case CID.BYTE:     return AddressType.BYTE;
            case CID.SHORT:    // ...
            case CID.CHAR:     return AddressType.SHORT;
            case CID.FLOAT:    return AddressType.FLOAT;
            case CID.INT:      return AddressType.INT;
            case CID.LONG:     return AddressType.LONG;
            case CID.DOUBLE:   return AddressType.DOUBLE;
            case CID.VOID:     return AddressType.UNDEFINED;
            case CID.OFFSET:   // fall through
            case CID.UWORD:    return AddressType.UWORD;
            case CID.ADDRESS:  // fall through
            default:           return AddressType.REF;
        }
    }

/*end[TYPEMAP]*/

    /*---------------------------------------------------------------------------*\
     *                         InstructionVisitor methods                        *
    \*---------------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    public void doArithmeticOp(ArithmeticOp instruction) {
        emitOpcode(instruction.getOpcode());
    }

    /**
     * {@inheritDoc}
     */
    public void doArrayLength(ArrayLength instruction) {
        emitOpcode(OPC.ARRAYLENGTH);
    }

    /**
     * {@inheritDoc}
     */
    public void doArrayLoad(ArrayLoad instruction) {
        int opcode;
        switch (instruction.getType().getSystemID()) {
            case CID.BOOLEAN:
            case CID.BYTE:    opcode = OPC.ALOAD_B; break;
            case CID.CHAR:    opcode = OPC.ALOAD_C; break;
            case CID.SHORT:   opcode = OPC.ALOAD_S; break;
            case CID.INT:     opcode = OPC.ALOAD_I; break;
            case CID.LONG:    opcode = OPC.ALOAD_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.ALOAD_F; break;
            case CID.DOUBLE:  opcode = OPC.ALOAD_D; break;
/*else[FLOATS]*/
//          case CID.FLOAT:
//          case CID.DOUBLE:  Assert.shouldNotReachHere();
/*end[FLOATS]*/
            case CID.OFFSET:  // fall through
            case CID.UWORD:   // ...
            case CID.ADDRESS: opcode = Klass.SQUAWK_64 ?
                                       OPC.ALOAD_L:
                                       OPC.ALOAD_I; break;
            default:          opcode = OPC.ALOAD_O; break;
        }
        emitOpcode(opcode);
    }

    /**
     * {@inheritDoc}
     */
    public void doArrayStore(ArrayStore instruction) {
        int opcode;
        switch (instruction.getComponentType().getSystemID()) {
            case CID.BOOLEAN:
            case CID.BYTE:    opcode = OPC.ASTORE_B; break;
            case CID.CHAR:    // fall through ...
            case CID.SHORT:   opcode = OPC.ASTORE_S; break;
            case CID.INT:     opcode = OPC.ASTORE_I; break;
            case CID.LONG:    opcode = OPC.ASTORE_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.ASTORE_F; break;
            case CID.DOUBLE:  opcode = OPC.ASTORE_D; break;
/*else[FLOATS]*/
//          case CID.FLOAT:
//          case CID.DOUBLE:  Assert.shouldNotReachHere();
/*end[FLOATS]*/
            case CID.OFFSET:  // fall through
            case CID.UWORD:   // ...
            case CID.ADDRESS: opcode = Klass.SQUAWK_64 ?
                                       OPC.ASTORE_L:
                                       OPC.ASTORE_I; break;
            default:          opcode = OPC.ASTORE_O; break;
        }
        emitOpcode(opcode);
    }

    /**
     * Common functionality for emitting absolute and conditional branches.
     *
     * @param opcode
     * @param instruction
     * @param target
     */
    private void doBranch(int opcode, Branch branch, Target target) {
        int targetOffset = target.getBytecodeOffset();
        /*
         * If this is the first pass and the target is ahead of the current
         * position then there is no way of knowing what it will be. In this
         * case just assume that the target is the current instruction. The real
         * address will be found on the next pass.
         */
        if (state == INIT && branch.isForward()) {
            count += 2; // Minimum size
            needAnotherPass = true;
        } else {
            /*
             * Emit the code and if the size of the ommited code does not equal
             * the extimated position of the next instruction then add 1 byte
             * to the estimate and try the instuction again until it is right.
             */
            if (emitBranch(opcode, targetOffset, 1)) {
                return;
            }
            if (emitBranch(opcode, targetOffset, 2)) {
                return;
            }
            if (emitBranch(opcode, targetOffset, 3)) {
                return;
            }
            if (emitBranch(opcode, targetOffset, 5)) {
                return;
            }
            Assert.shouldNotReachHere();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doBranch(Branch instruction) {
        doBranch(OPC.GOTO, instruction, instruction.getTarget());
    }

    /**
     * {@inheritDoc}
     */
    public void doCheckCast(CheckCast instruction) {
        emitConstantObject(instruction.getType());
        emitOpcode(OPC.CHECKCAST);
    }

    /**
     * {@inheritDoc}
     */
    public void doConversionOp(ConversionOp instruction) {
        emitOpcode(instruction.getOpcode());
    }

    /**
     * {@inheritDoc}
     */
    public void doComparisonOp(ComparisonOp instruction) {
        if (instruction.isLCMP()) {
            invokeNative(Native.com_sun_squawk_VM$lcmp,  Klass.INT);
        } else {
/*if[FLOATS]*/
            int opcode = instruction.getOpcode();
            Assert.that(opcode >= OPC.FCMPL && opcode <= OPC.DCMPG);
            emitOpcode(opcode);
/*end[FLOATS]*/
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doTry(Try instruction) {
    }

    /**
     * {@inheritDoc}
     */
    public void doTryEnd(TryEnd instruction) {
    }

    /**
     * {@inheritDoc}
     */
    public void doIf(If instruction) {
        doBranch(instruction.getOpcode(), instruction, instruction.getTarget());
    }

    /**
     * {@inheritDoc}
     */
    public void doIfCompare(IfCompare instruction) {
        doIf(instruction);
    }

    /**
     * {@inheritDoc}
     */
    public void doIncDecLocal(IncDecLocal instruction) {
        Local local = instruction.getLocal();
        Assert.that(local.getType() == Klass.INT);
        boolean isInc = instruction.isIncrement();
        if (local.isParameter()) {
            emitIncDec(isInc, true,  local.getSquawkParameterIndex());
        } else {
            emitIncDec(isInc, false, local.getSquawkLocalIndex());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doInstanceOf(InstanceOf instruction) {
        emitConstantObject(instruction.getCheckType());
        emitOpcode(OPC.INSTANCEOF);
    }

    /**
     * {@inheritDoc}
     */
    public void doFindSlot (FindSlot instruction) {
        Method callee = instruction.getMethod();
        Assert.that(!callee.isNative(), "Invalid native findslot to "+callee);
        emitConstantObject(callee.getDefiningClass());
        emitUnsigned(OPC.FINDSLOT, callee.getOffset());
    }

    /**
     * Verifies that the method is callable - it is not hosted, or has been deleted.
     */
    private void checkMethodCallable(Method callee) {
        int offset = callee.getOffset();
        if (offset == Klass.ILLEGAL_METHOD_OFFSET) {
        	throw new NoSuchMethodError("Call to hosted or other no longer available method: " + Klass.toString(callee, false) + " in " + Klass.toString(method, false));
        }
        
        Klass klass = callee.getDefiningClass();
        if (klass.getState() >= Klass.STATE_CONVERTED && !callee.isAbstract()) {
            Object m = klass.getMethodObject(callee);
            if (m == null || Klass.isMissingMethodObject(m, callee.isStatic())) {
                if (Translator.FORCE_DME_ERRORS) {
                    System.out.println("WARNING: Call to deleted method: " + Klass.toString(callee, false) + " in " + Klass.toString(method, false));
                    System.out.println("    Leaving call in to test error handling");
                    return;
                } else {
                	throw new NoSuchMethodError("Call to deleted method: " + Klass.toString(callee, false) + " in " + Klass.toString(method, false));
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeSlot(InvokeSlot instruction) {
        int opcode;
        Method callee = instruction.getMethod();
        checkMethodCallable(callee);
        switch (callee.getReturnType().getSystemID()) {
            case CID.VOID:    opcode = OPC.INVOKESLOT_V; break;
            case CID.BOOLEAN: // fall through ...
            case CID.BYTE:    // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
            case CID.INT:     opcode = OPC.INVOKESLOT_I; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.INVOKESLOT_F; break;
            case CID.DOUBLE:  opcode = OPC.INVOKESLOT_D; break;
/*else[FLOATS]*/
//          case CID.FLOAT:
//          case CID.DOUBLE:  Assert.shouldNotReachHere();
/*end[FLOATS]*/
            case CID.LONG:    opcode = OPC.INVOKESLOT_L; break;
            case CID.OFFSET:  // fall through
            case CID.UWORD:   // ...
            case CID.ADDRESS: opcode = Klass.SQUAWK_64?
                                       OPC.INVOKESLOT_L:
                                       OPC.INVOKESLOT_I; break;
            default:          opcode = OPC.INVOKESLOT_O; break;
        }
        Assert.that(!callee.isNative(), "Invalid native invokeslot to "+callee);
        emitOpcode(opcode);
    }

    /**
     * Emit an invoke to a native method.
     *
     * @param name   the fully qualified name of the native method
     * @param type   the return type of the method
     */
    private void invokeNative(String name, Klass type) {
        int identifier = VM.lookupNative(name);
        if (identifier == -1) {
            String msg = "Undefined native method invoked in " + method + ": " + name;
            throw new NoClassDefFoundError(msg);
        }
        invokeNative(identifier, type);
    }

    static int[] nativeMethodsUseCount = new int[Native.ENTRY_COUNT];

    /**
     * Emit an invoke to a native method.
     *
     * @param identifier   the native method identifier which must be one of the constants in {@link Native}
     * @param type the return type
     */
    private void invokeNative(int identifier, Klass type) {
/*if[KERNEL_SQUAWK]*/
        if (identifier == Native.com_sun_squawk_VM$pause) {
            emitOpcode(OPC.PAUSE); // Special case for VM.pause()
        } else
/*end[KERNEL_SQUAWK]*/
        {
            int opcode;
            switch (type.getSystemID()) {
                case CID.VOID:    opcode = OPC.INVOKENATIVE_V; break;
                case CID.BOOLEAN: // fall through ...
                case CID.BYTE:    // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...
                case CID.INT:     opcode = OPC.INVOKENATIVE_I; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.INVOKENATIVE_F; break;
                case CID.DOUBLE:  opcode = OPC.INVOKENATIVE_D; break;
/*else[FLOATS]*/
//              case CID.FLOAT:
//              case CID.DOUBLE:  Assert.shouldNotReachHere("Native ID: " + identifier);
/*end[FLOATS]*/
                case CID.LONG:    opcode = OPC.INVOKENATIVE_L; break;
                case CID.OFFSET:  // fall through
                case CID.UWORD:   // ...
                case CID.ADDRESS: opcode = Klass.SQUAWK_64?
                                           OPC.INVOKENATIVE_L:
                                           OPC.INVOKENATIVE_I; break;
                default:          opcode = OPC.INVOKENATIVE_O; break;
            }
            nativeMethodsUseCount[identifier]++;
            emitUnsigned(opcode, identifier);
        }
    }

    public static void printUncalledNativeMethods() {
        for (int i = 0; i < nativeMethodsUseCount.length; i++) {
            if (nativeMethodsUseCount[i] == 0) {
                System.out.println("Native method index " + i + " is not called in this suite");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeStatic(InvokeStatic instruction) {
        invokeStatic(instruction.getMethod());
    }

    /**
     * Emits the code for a state method invocation.
     *
     * @param method  the method being invoked
     */
    private void invokeStatic(Method method) {
        Klass returnType = method.getReturnType();
        if (method.isNative()) {
            String name = method.getFullyQualifiedName();
            invokeNative(name, returnType);
        } else {
            int opcode;
            checkMethodCallable(method);
            switch (returnType.getSystemID()) {
                case CID.VOID:    opcode = OPC.INVOKESTATIC_V; break;
                case CID.BOOLEAN: // fall through ...
                case CID.BYTE:    // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...
                case CID.INT:     opcode = OPC.INVOKESTATIC_I; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.INVOKESTATIC_F; break;
                case CID.DOUBLE:  opcode = OPC.INVOKESTATIC_D; break;
/*else[FLOATS]*/
//              case CID.FLOAT:
//              case CID.DOUBLE:  Assert.shouldNotReachHere();
/*end[FLOATS]*/
                case CID.LONG:    opcode = OPC.INVOKESTATIC_L; break;
                case CID.OFFSET:  // fall through
                case CID.UWORD:   // ...
                case CID.ADDRESS: opcode = Klass.SQUAWK_64?
                                           OPC.INVOKESTATIC_L:
                                           OPC.INVOKESTATIC_I; break;
                default:          opcode = OPC.INVOKESTATIC_O; break;
            }
            emitConstantObject(method.getDefiningClass());
            emitUnsigned(opcode, method.getOffset());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeSuper(InvokeSuper instruction) {
        int opcode;
        Method callee = instruction.getMethod();
        Klass returnType = callee.getReturnType();
        if (callee.isNative()) {
            Assert.that(callee.isFinal() || callee.getDefiningClass().isFinal() , "cannot invoke non-final native method "+callee);
            String name = callee.getFullyQualifiedName();
            invokeNative(name, returnType);
        } else {
            checkMethodCallable(callee);
            switch (returnType.getSystemID()) {
                case CID.VOID:    opcode = OPC.INVOKESUPER_V; break;
                case CID.BOOLEAN: // fall through ...
                case CID.BYTE:    // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...
                case CID.INT:     opcode = OPC.INVOKESUPER_I; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.INVOKESUPER_F; break;
                case CID.DOUBLE:  opcode = OPC.INVOKESUPER_D; break;
/*else[FLOATS]*/
//              case CID.FLOAT:
//              case CID.DOUBLE:  Assert.shouldNotReachHere("NO FLOATS");
/*end[FLOATS]*/
                case CID.LONG:    opcode = OPC.INVOKESUPER_L; break;
                case CID.OFFSET:  // fall through
                case CID.UWORD:   // ...
                case CID.ADDRESS: opcode = Klass.SQUAWK_64?
                                           OPC.INVOKESUPER_L:
                                           OPC.INVOKESUPER_I; break;
                default:          opcode = OPC.INVOKESUPER_O; break;
            }
            Assert.that(callee.isPrivate() || !callee.isNative(), this.method.toString() + ": invalid native invokesuper to "+callee);
            emitConstantObject(callee.getDefiningClass());
            emitUnsigned(opcode, callee.getOffset());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeVirtual(InvokeVirtual instruction) {
        Method callee = instruction.getMethod();
        Klass returnType = callee.getReturnType();
        if (callee.isNative()) {
            Assert.that(callee.isFinal() || callee.getDefiningClass().isFinal() , "cannot invoke non-final native method "+callee);
            String name = callee.getFullyQualifiedName();
            invokeNative(name, returnType);
        } else {
            int opcode;
            checkMethodCallable(callee);
            switch (returnType.getSystemID()) {
                case CID.VOID:    opcode = OPC.INVOKEVIRTUAL_V; break;
                case CID.BOOLEAN: // fall through ...
                case CID.BYTE:    // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...
                case CID.INT:     opcode = OPC.INVOKEVIRTUAL_I; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.INVOKEVIRTUAL_F; break;
                case CID.DOUBLE:  opcode = OPC.INVOKEVIRTUAL_D; break;
/*else[FLOATS]*/
//              case CID.FLOAT:
//              case CID.DOUBLE:  Assert.shouldNotReachHere("NO FLOATS");
/*end[FLOATS]*/
                case CID.LONG:    opcode = OPC.INVOKEVIRTUAL_L; break;
                case CID.OFFSET:  // fall through
                case CID.UWORD:   // ...
                case CID.ADDRESS: opcode = Klass.SQUAWK_64?
                                           OPC.INVOKEVIRTUAL_L:
                                           OPC.INVOKEVIRTUAL_I; break;
                default:          opcode = OPC.INVOKEVIRTUAL_O; break;
            }
            emitUnsigned(opcode, callee.getOffset());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doConstant(Constant instruction) {
        Object value = instruction.getValue();
        switch (instruction.getTag()) {
/*if[FLOATS]*/
            case ConstantPool.CONSTANT_Double: {
                emitConstantDouble(((Double)value).doubleValue());
                break;
            }
            case ConstantPool.CONSTANT_Float: {
                emitConstantFloat(((Float)value).floatValue());
                break;
            }
/*end[FLOATS]*/
            case ConstantPool.CONSTANT_Long: {
                emitConstantLong(((Long)value).longValue());
                break;
            }
            case ConstantPool.CONSTANT_Integer: {
                emitConstantInt(((Integer)value).intValue());
                break;
            }
            default: {
                emitConstantObject(value);
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doCatch(Catch instruction) {
        emitOpcode(OPC.CATCH);
    }

    /**
     * {@inheritDoc}
     */
    public void doGetField(GetField instruction) {
        int opcode;
        Field field = instruction.getField();
        switch (field.getType().getSystemID()) {
            case CID.BYTE:    // fall through ...
            case CID.BOOLEAN: opcode = OPC.GETFIELD_B; break;
            case CID.SHORT:   opcode = OPC.GETFIELD_S; break;
            case CID.CHAR:    opcode = OPC.GETFIELD_C; break;
            case CID.INT:     opcode = OPC.GETFIELD_I; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.GETFIELD_F; break;
            case CID.DOUBLE:  opcode = OPC.GETFIELD_D; break;
/*else[FLOATS]*/
//          case CID.FLOAT:
//          case CID.DOUBLE:  Assert.shouldNotReachHere("NO FLOATS");
/*end[FLOATS]*/
            case CID.LONG:    opcode = OPC.GETFIELD_L; break;
            case CID.OFFSET:  // fall through
            case CID.UWORD:   // ...
            case CID.ADDRESS: opcode = Klass.SQUAWK_64 ?
                                       OPC.GETFIELD_L:
                                       OPC.GETFIELD_I; break;
            default:          opcode = OPC.GETFIELD_O; break;
        }

        // Peephole optimization to emit getfield0
        if (instruction.getObject() instanceof LoadLocal) {
            LoadLocal load = (LoadLocal)instruction.getObject();
            Local local = load.getLocal();
            if (local.isParameter() && local.getSquawkParameterIndex() == 0) {
                if (load.getBytecodeOffset() != -1) {
                    ir.remove(load);

                    // Mark the instruction as removed
                    load.setBytecodeOffset(-1);
                    needAnotherPass = true;
                    return;
                }

                switch (opcode) {
                    case OPC.GETFIELD_B: opcode = OPC.GETFIELD0_B; break;
                    case OPC.GETFIELD_S: opcode = OPC.GETFIELD0_S; break;
                    case OPC.GETFIELD_C: opcode = OPC.GETFIELD0_C; break;
                    case OPC.GETFIELD_I: opcode = OPC.GETFIELD0_I; break;
                    case OPC.GETFIELD_O: opcode = OPC.GETFIELD0_O; break;
                    case OPC.GETFIELD_L: opcode = OPC.GETFIELD0_L; break;
/*if[FLOATS]*/
                    case OPC.GETFIELD_F: opcode = OPC.GETFIELD0_F; break;
                    case OPC.GETFIELD_D: opcode = OPC.GETFIELD0_D; break;
/*end[FLOATS]*/
                    default: Assert.shouldNotReachHere();
                }
            }
        }
        emitUnsigned(opcode, field.getOffset());
    }

    /**
     * {@inheritDoc}
     */
    public void doGetStatic(GetStatic instruction) {
        Field field = instruction.getField();
        Klass klass = field.getDefiningClass();
        if (klass.hasGlobalStatics()) {
            emitGlobalVariableAccess(field, true);
        } else {
            int opcode;
            switch (field.getType().getSystemID()) {
                case CID.BYTE:    // fall through ...
                case CID.BOOLEAN: // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...
                case CID.INT:     opcode = OPC.GETSTATIC_I; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.GETSTATIC_F; break;
                case CID.DOUBLE:  opcode = OPC.GETSTATIC_D; break;
/*else[FLOATS]*/
//              case CID.FLOAT:
//              case CID.DOUBLE:  Assert.shouldNotReachHere("NO FLOATS");
/*end[FLOATS]*/
                case CID.LONG:    opcode = OPC.GETSTATIC_L; break;
                case CID.OFFSET:  // fall through
                case CID.UWORD:   // ...
                case CID.ADDRESS: opcode = Klass.SQUAWK_64 ?
                                           OPC.GETSTATIC_L:
                                           OPC.GETSTATIC_I; break;
                default:          opcode = OPC.GETSTATIC_O; break;
            }
            if (klass == method.getDefiningClass()) {
                switch (opcode) {
                    case OPC.GETSTATIC_I: opcode = OPC.CLASS_GETSTATIC_I; break;
                    case OPC.GETSTATIC_L: opcode = OPC.CLASS_GETSTATIC_L; break;
                    case OPC.GETSTATIC_O: opcode = OPC.CLASS_GETSTATIC_O; break;
/*if[FLOATS]*/
                    case OPC.GETSTATIC_F: opcode = OPC.CLASS_GETSTATIC_F; break;
                    case OPC.GETSTATIC_D: opcode = OPC.CLASS_GETSTATIC_D; break;
/*end[FLOATS]*/
                    default: Assert.shouldNotReachHere();
                }
            } else {
                emitConstantObject(klass);
            }
            emitUnsigned(opcode, field.getOffset() + CS.firstVariable);
        }
    }

    /**
     * Emit access to a global variable.
     *
     * @param field the static field that represents the global variale
     * @param isRead true if the access is a read
     */
    private void emitGlobalVariableAccess(Field field, boolean isRead) {
        Klass varType;
        String nativename;
        Hashtable table;
        String fieldname = field.getFullyQualifiedName();

        /*
         * Work out the name and type of the accessor method.
         */
        switch (field.getType().getSystemID()) {
/*if[FLOATS]*/
            case CID.DOUBLE:  // fall through ...
/*else[FLOATS]*/
//          case CID.FLOAT:
//          case CID.DOUBLE: Assert.shouldNotReachHere();
/*end[FLOATS]*/
            case CID.LONG: {
                Assert.shouldNotReachHere("Only single word global varibles are supported");
            }
            case CID.OFFSET:  // fall through ...
            case CID.UWORD: {
                Assert.shouldNotReachHere(field.getType().getName() + " typed global varibles are not (yet) supported");
            }
            case CID.BYTE:    // fall through ...
            case CID.BOOLEAN: // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
/*if[FLOATS]*/
            case CID.FLOAT:   // fall through ...
/*end[FLOATS]*/
            case CID.INT: {
                nativename = "Int";
                varType    = Klass.INT;
                table      = intGlobals;
                Assert.always(oopGlobals.get(fieldname)  == null, field+" is an oop in Globals.java");
                Assert.always(addrGlobals.get(fieldname) == null, field+" is an address in Globals.java");
                break;
            }
            case CID.ADDRESS: {
                nativename = "Addr";
                varType    = Klass.SQUAWK_64 ? Klass.LONG : Klass.INT;
                table      = addrGlobals;
                Assert.always(oopGlobals.get(fieldname) == null, field+" is an oop in Globals.java");
                Assert.always(intGlobals.get(fieldname) == null, field+" is an int in Globals.java");
                break;
            }
            default: {
                nativename = "Oop";
                varType    = Klass.OBJECT;
                table      = oopGlobals;
                Assert.always(addrGlobals.get(fieldname) == null, field+" is an address in Globals.java");
                Assert.always(intGlobals.get(fieldname)  == null, field+" is an int in Globals.java");
                break;
            }
        }
        nativename = "com.sun.squawk.VM." + (isRead ? "getGlobal" : "setGlobal") + nativename;

        /*
         * Look for the field in either the integer or reference hashtables and
         * add the field to the appropriate table if it is missing.
         */
        Integer index = (Integer)table.get(fieldname);
        if (index == null) {
            throw new NoSuchFieldError("No built-in global field " + fieldname);
        }
        Assert.that(index != null);

/*if[SUITE_VERIFIER]*/
        if (table == oopGlobals) {
            while (index.intValue() >= oopGlobalReverse.length) {
                String tmp[] = new String[2*oopGlobalReverse.length + 1];
                System.arraycopy(oopGlobalReverse, 0, tmp, 0, oopGlobalReverse.length);
                oopGlobalReverse = tmp;
            }
            // Assert.that(oopGlobalReverse[index.intValue()] == null);
            Object oldval = oopGlobalReverse[index.intValue()];
            if (oldval != null) {
                Assert.that(oldval.equals(fieldname));
            } else {
                oopGlobalReverse[index.intValue()] = fieldname;
            }
        }
/*end[SUITE_VERIFIER]*/

        /*
         * Output the index as an extra parameter.
         */
        emitConstantInt(index.intValue());

        /*
         * Emit the invokenative.
         */
        invokeNative(nativename, isRead ? varType : Klass.VOID);
    }

    /**
     * {@inheritDoc}
     */
    public void doLoadLocal(LoadLocal instruction) {
        Local local = instruction.getLocal();
        boolean isLong = local.is64Bit();
        if (local.isParameter()) {
            emitLoad(true, isLong, local.getSquawkParameterIndex());
        } else {
            emitLoad(false, isLong, local.getSquawkLocalIndex());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doLookupSwitch(LookupSwitch instruction) {
        Object caseValues = instruction.getCaseValues();
        emitConstantObject(caseValues);
        if (caseValues instanceof int[]) {
            emitOpcode(OPC.LOOKUP_I, AddressType.INT);
            doSwitch(instruction, 0, ((int[])caseValues).length-1);
        } else if (caseValues instanceof short[]) {
            emitOpcode(OPC.LOOKUP_S, AddressType.INT);
            doSwitch(instruction, 0, ((short[])caseValues).length-1);
        } else if (caseValues instanceof byte[]) {
            emitOpcode(OPC.LOOKUP_B, AddressType.INT);
            doSwitch(instruction, 0, ((byte[])caseValues).length-1);
        } else {
            Assert.shouldNotReachHere();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doMonitorEnter(MonitorEnter instruction) {
        emitOpcode(instruction.getObject() == null ? OPC.CLASS_MONITORENTER : OPC.MONITORENTER);
    }

    /**
     * {@inheritDoc}
     */
    public void doMonitorExit(MonitorExit instruction) {
        emitOpcode(instruction.getObject() == null ? OPC.CLASS_MONITOREXIT : OPC.MONITOREXIT);
    }

    /**
     * {@inheritDoc}
     */
    public void doNegationOp(NegationOp instruction) {
        emitOpcode(instruction.getOpcode());
    }

    /**
     * {@inheritDoc}
     */
    public void doNewArray(NewArray instruction) {
        emitConstantObject(instruction.getType());
        emitOpcode(OPC.NEWARRAY);
    }

    /**
     * {@inheritDoc}
     */
    public void doNewDimension(NewDimension instruction) {
        emitOpcode(OPC.NEWDIMENSION);
    }

    /**
     * {@inheritDoc}
     */
    public void doNew(New instruction) {
        Klass klass = instruction.getRuntimeType();
        if (klass.isSquawkArray()) {
            emitConstantObject(null); // String allocation is done in by the methods that replace the <init> constuctors.
        } else {
            emitConstantObject(klass);
            emitOpcode(OPC.NEW);
        }
    }

    /**
     * This is a WARNING, untill dead code elimination is checked in. Then it should be made an error.
     */
    private void reportSquawkConstraintBroken(String msg, Instruction instr) {
        System.out.println("WARNING: Squawk constraint: " + msg + " was broken at " + instr + " in " + method);
        System.out.println("    This has only been seen in TCK tests, in code that is not reachable, so never executed. When dead-code elimination is checked in, we can be certain on this point.");
        // Translator.throwVerifyError("frame state contains uninitialized object at backward branch");
    }

    /**
     * {@inheritDoc}
     */
    public void doPhi(Phi instruction) {
        if (instruction.getTarget().isBackwardBranchTarget()) {
            if (instruction.getTarget().isFatalTarget()) {
                reportSquawkConstraintBroken("frame state contains uninitialized object at backward branch", instruction);
            }
            emitOpcode(isAppClass ? OPC.BBTARGET_APP : OPC.BBTARGET_SYS);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doPop(Pop instruction) {
        if (instruction.value().isOnStack()) {
            if (!Klass.SQUAWK_64 && instruction.value().getType().isDoubleWord()) {
                emitOpcode(OPC.POP_2);
            } else {
                emitOpcode(OPC.POP_1);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doPosition(Position instruction) {
    }

    /**
     * {@inheritDoc}
     */
    public void doReturn(Return instruction) {
        int opcode;
        StackProducer value = instruction.getValue();
        if (value == null) {
            opcode = OPC.RETURN_V;
        } else {
            switch (value.getType().getSystemID()) {
                case CID.BYTE:    // fall through ...
                case CID.BOOLEAN: // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...
                case CID.INT:     opcode = OPC.RETURN_I; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.RETURN_F; break;
                case CID.DOUBLE:  opcode = OPC.RETURN_D; break;
/*else[FLOATS]*/
//              case CID.FLOAT:
//              case CID.DOUBLE: Assert.shouldNotReachHere("NO FLOATS");
/*end[FLOATS]*/
                case CID.LONG:    opcode = OPC.RETURN_L; break;
                case CID.OFFSET:  // fall through
                case CID.UWORD:   // ...
                case CID.ADDRESS: opcode = Klass.SQUAWK_64 ?
                                           OPC.RETURN_L:
                                           OPC.RETURN_I; break;
                default:          opcode = OPC.RETURN_O; break;
            }
        }
        emitOpcode(opcode);
    }

    /**
     * {@inheritDoc}
     */
    public void doPutField(PutField instruction) {
        int opcode;
        Field field = instruction.getField();
        switch (field.getType().getSystemID()) {
            case CID.BYTE:    // fall through ...
            case CID.BOOLEAN: opcode = OPC.PUTFIELD_B; break;
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    opcode = OPC.PUTFIELD_S; break;
            case CID.INT:     opcode = OPC.PUTFIELD_I; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.PUTFIELD_F; break;
            case CID.DOUBLE:  opcode = OPC.PUTFIELD_D; break;
/*else[FLOATS]*/
//          case CID.FLOAT:
//          case CID.DOUBLE: Assert.shouldNotReachHere("NO FLOATS");
/*end[FLOATS]*/
            case CID.LONG:    opcode = OPC.PUTFIELD_L; break;
            case CID.OFFSET:  // fall through
            case CID.UWORD:   // ...
            case CID.ADDRESS: opcode = Klass.SQUAWK_64 ?
                                       OPC.PUTFIELD_L:
                                       OPC.PUTFIELD_I; break;
            default:          opcode = OPC.PUTFIELD_O; break;
        }

        // Peephole optimization to emit putfield0
        if (instruction.getObject() instanceof LoadLocal) {
            LoadLocal load = (LoadLocal)instruction.getObject();
            Local local = load.getLocal();
            if (local.isParameter() && local.getSquawkParameterIndex() == 0) {
                if (load.getBytecodeOffset() != -1) {
                    ir.remove(load);

                    // Mark the instruction as removed
                    load.setBytecodeOffset(-1);
                    needAnotherPass = true;
                    return;
                }

                switch (opcode) {
                    case OPC.PUTFIELD_B: opcode = OPC.PUTFIELD0_B; break;
                    case OPC.PUTFIELD_S: opcode = OPC.PUTFIELD0_S; break;
                    case OPC.PUTFIELD_I: opcode = OPC.PUTFIELD0_I; break;
                    case OPC.PUTFIELD_O: opcode = OPC.PUTFIELD0_O; break;
                    case OPC.PUTFIELD_L: opcode = OPC.PUTFIELD0_L; break;
/*if[FLOATS]*/
                    case OPC.PUTFIELD_F: opcode = OPC.PUTFIELD0_F; break;
                    case OPC.PUTFIELD_D: opcode = OPC.PUTFIELD0_D; break;
/*end[FLOATS]*/
                    default: Assert.shouldNotReachHere();
                }
            }
        }
        emitUnsigned(opcode, field.getOffset());
    }

    /**
     * {@inheritDoc}
     */
    public void doPutStatic(PutStatic instruction) {
        Field field = instruction.getField();
        Klass klass = field.getDefiningClass();
        if (klass.hasGlobalStatics()) {
            emitGlobalVariableAccess(field, false);
        } else {
            int opcode;
            switch (field.getType().getSystemID()) {
                case CID.BYTE:    // fall through ...
                case CID.BOOLEAN: // fall through ...
                case CID.SHORT:   // fall through ...
                case CID.CHAR:    // fall through ...
                case CID.INT:     opcode = OPC.PUTSTATIC_I; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.PUTSTATIC_F; break;
                case CID.DOUBLE:  opcode = OPC.PUTSTATIC_D; break;
/*else[FLOATS]*/
//              case CID.FLOAT:
//              case CID.DOUBLE: Assert.shouldNotReachHere("NO FLOATS");
/*end[FLOATS]*/
                case CID.LONG:    opcode = OPC.PUTSTATIC_L; break;
                case CID.OFFSET:  // fall through
                case CID.UWORD:   // ...
                case CID.ADDRESS: opcode = Klass.SQUAWK_64 ?
                                           OPC.PUTSTATIC_L:
                                           OPC.PUTSTATIC_I; break;
                default:          opcode = OPC.PUTSTATIC_O; break;
            }
            if (klass == method.getDefiningClass()) {
                switch (opcode) {
                    case OPC.PUTSTATIC_I: opcode = OPC.CLASS_PUTSTATIC_I; break;
                    case OPC.PUTSTATIC_L: opcode = OPC.CLASS_PUTSTATIC_L; break;
                    case OPC.PUTSTATIC_O: opcode = OPC.CLASS_PUTSTATIC_O; break;
/*if[FLOATS]*/
                    case OPC.PUTSTATIC_F: opcode = OPC.CLASS_PUTSTATIC_F; break;
                    case OPC.PUTSTATIC_D: opcode = OPC.CLASS_PUTSTATIC_D; break;
/*end[FLOATS]*/
                    default: Assert.shouldNotReachHere();
                }
            } else {
                emitConstantObject(klass);
            }
            emitUnsigned(opcode, field.getOffset() + CS.firstVariable);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doStoreLocal(StoreLocal instruction) {
        Local local = instruction.getLocal();
        boolean isLong = local.is64Bit();
        if (local.isParameter()) {
            emitStore(true, isLong, local.getSquawkParameterIndex());
        } else {
            emitStore(false, isLong, local.getSquawkLocalIndex());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doStackMerge(StackMerge instruction) {
        Assert.shouldNotReachHere();
    }

    /**
     * {@inheritDoc}
     */
    public void doStackOp(StackOp instruction) {
    }

    /**
     * Emit a table switch entry
     *
     * @param offset      the offset
     * @param size        the table size
     * @return            true if it worked
     */
    private boolean emitTableEntry(int offset, int size) {
        if (size == 2) {
            if (offset < -32768 || offset >= 32768) {
               return false;
            }
            emitShort(offset);
        } else {
            emitInt(offset);
        }
        return true;
    }

    /**
     * Try to make a switch table.
     *
     * @param instruction the instruction
     * @param low         the low bound
     * @param high        the high bound
     * @param size        the table size
     * @return            true if it worked
     */
    private boolean tryTable(Switch instruction, int low, int high, int size) {
        Target   def     = instruction.getDefaultTarget();
        Target[] targets = instruction.getTargets();
        emitOpcode((size == 2) ? OPC.TABLESWITCH_S : OPC.TABLESWITCH_I);
        while ((count % size) != 0) {
            emit(0); // Pad
        }
        if (!emitTableEntry(low, size)) {
            return false;
        }
        if (!emitTableEntry(high, size)) {
            return false;
        }
        int position = count + size;
        int offset = def.getBytecodeOffset() - position;
        if (!emitTableEntry(offset, size)) {
            return false;
        }
        for (int i = 0; i != targets.length; ++i) {
            offset = targets[i].getBytecodeOffset() - position;
            if (!emitTableEntry(offset, size)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Process a lookup switch or table switch.
     *
     * @param instruction the instruction
     * @param low         the low bound
     * @param high        the high bound
     */
    private void doSwitch(Switch instruction, int low, int high) {
        int savePosition = count;
        needAnotherPass = true;
        if (tryTable(instruction, low, high, 2) == false) {
            count = savePosition;
            tryTable(instruction, low, high, 4);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doTableSwitch(TableSwitch instruction) {
        doSwitch(instruction, instruction.getLow(), instruction.getHigh());
    }

    /**
     * {@inheritDoc}
     */
    public void doThrow(Throw instruction) {
        emitOpcode(OPC.THROW);
    }
}
