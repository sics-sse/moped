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

import java.io.PrintStream;
import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.ci.ConstantPool;
import com.sun.squawk.translator.ci.Opcode;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.vm.*;
import com.sun.squawk.*;
import java.util.*;

/**
 * This in an instruction visitor implementation for tracing the instructions
 * in an IR.
 *
 */
public class InstructionTracer implements InstructionVisitor {

    /*
     * StringBuffer for composing output.
     */
    private StringBuffer out;

    /**
     * An alias for Tracer.getPrintStream().
     */
    private PrintStream ps = Tracer.getPrintStream();

    /**
     * The IR being traced.
     */
    private IR ir;

    /**
     * The current indent level.
     */
    private String indent = "        ";

    /**
     * Creates a string of space characters. The length of the returned string
     * is equal:
     * <p><blockquote><pre>
     *     String.valueOf(value).length() + extra
     * </pre></blockquote></p>
     *
     * @param   value  an integer value
     * @param   extra  the extra numbers of spaces
     * @return  a string of space characters whose length is specified by
     *                 <code>value</code> and <code>extra</code>
     */
    private String createSpaces(int value, int extra) {
        int length = String.valueOf(value).length() + extra;
        StringBuffer buf = new StringBuffer(length);
        for (int i = 0 ; i != length ; ++i) {
            buf.append(' ');
        }
        return buf.toString();
    }

    /**
     * Constuctor.
     *
     * @param ir the IR to trace
     */
    public InstructionTracer(IR ir) {
        this.ir = ir;
        indent = createSpaces(getAddressOf(ir.getTail()), 2);
    }

    /**
     * Traces all the instructions in a given IR to the tracing output stream.
     */
    public void traceAll() {
        for (Instruction instruction = ir.getHead() ; instruction != null ; instruction = instruction.getNext()) {
            trace(instruction);
        }
    }

    /**
     * Traces all the instructions in a given IR to the tracing output stream
     * combining the output with a MethodBodyTracer.
     *
     * @param mbt the MethodBodyTracer
     */
    public void traceWithBody(MethodBodyTracer mbt) {
        for (Instruction instruction = ir.getHead() ; instruction != null ; instruction = instruction.getNext()) {
            if (
                   (instruction instanceof Position)
                || (instruction instanceof Try)
                || (instruction instanceof TryEnd)
                || (instruction instanceof Catch)
                || (instruction instanceof Phi)
//                || (instruction instanceof Phi && ((Phi)instruction).getTarget().isBackwardBranchTarget() == false)
                || (instruction instanceof StackMerge)
                || (instruction instanceof StackOp)
               ) {
                continue;
            }
            out = new StringBuffer();
            instruction.visit(this);

            Instruction next = instruction.getNext();
            int nextoffset = -1;
            if (next != null) {
                nextoffset = next.getBytecodeOffset();
            }
            ps.print(mbt.traceUntil(nextoffset));
            ps.println(out.toString());
        }
    }

    /**
     * Trace in instruction.
     *
     * @param instruction the instruction to trace
     */
    public void trace(Instruction instruction) {
        if (instruction instanceof Position) {
            return;
        }
        /*
         * indent to indicate leaving exception handler scope
         */
        if (instruction instanceof TryEnd) {
            indent = indent.substring(4);
        }

        String address = String.valueOf(getAddressOf(instruction));
        String prefix = address + ':' + indent.substring(address.length());
        String line = traceToString(instruction, prefix);
        ps.println(line);

        /*
         * outdent to indicate entering exception handler scope
         */
        if (instruction instanceof Try) {
            indent += "    ";
        }
    }

    /**
     * Trace to a string with a prefix.
     *
     * @param instruction the instruction
     * @param prefix the prefix
     */
   private String traceToString(Instruction instruction, String prefix) {
        out = new StringBuffer();
        out.append(prefix);
        instruction.visit(this);
        return out.toString();
   }

    /**
     * Gets the address of an instruction.
     *
     * @param   object  an <code>Instruction</code> instance or a <code>Target</code>
     *                  instance from which an <code>Instruction</code> instance can be derived
     * @return  the address of the instruction denoted by <code>object</code>
     */
    private int getAddressOf(Object object) {
        Instruction instruction;
        if (object instanceof Instruction) {
            instruction = ((Instruction)object);
        } else {
            Assert.that(object instanceof Target);
            instruction = (Instruction)((Target)object).getTargetedInstruction();
        }
        int address = instruction.getBytecodeOffset();
        boolean isFill = (instruction instanceof LoadLocal);

        /*
         * Synthesized instructions (such as spills & fills) are given the same
         * address as the intructions they are logically associated with. This
         * makes viewing the result of spilling/filling clearer.
         */
        while (address == Instruction.OFFSETNOTDEFINED) {
            instruction = isFill ? instruction.getNext() : instruction.getPrevious();
            address = instruction.getBytecodeOffset();
        }
        return address;
    }


    /*---------------------------------------------------------------------------*\
     *                         InstructionVisitor methods                        *
    \*---------------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    public void doArithmeticOp(ArithmeticOp instruction) {
        out.append(Mnemonics.getMnemonic(instruction.getOpcode()));
    }

    /**
     * {@inheritDoc}
     */
    public void doArrayLength(ArrayLength instruction) {
        out.append(Mnemonics.getMnemonic(OPC.ARRAYLENGTH));
    }

    /**
     * {@inheritDoc}
     */
    public void doArrayLoad(ArrayLoad instruction) {
        int opcode;
        switch (instruction.getType().getSystemID()) {
            case CID.BOOLEAN:
            case CID.BYTE:   opcode = OPC.ALOAD_B; break;
            case CID.CHAR:   opcode = OPC.ALOAD_C; break;
            case CID.SHORT:  opcode = OPC.ALOAD_S; break;
            case CID.INT:    opcode = OPC.ALOAD_I; break;
            case CID.LONG:   opcode = OPC.ALOAD_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:  opcode = OPC.ALOAD_F; break;
            case CID.DOUBLE: opcode = OPC.ALOAD_D; break;
/*end[FLOATS]*/
            default:         opcode = OPC.ALOAD_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode));
    }

    /**
     * {@inheritDoc}
     */
    public void doArrayStore(ArrayStore instruction) {
        int opcode;
        switch (instruction.getComponentType().getSystemID()) {
            case CID.BOOLEAN:
            case CID.BYTE:   opcode = OPC.ASTORE_B; break;
            case CID.CHAR:   // fall through ...
            case CID.SHORT:  opcode = OPC.ASTORE_S; break;
            case CID.INT:    opcode = OPC.ASTORE_I; break;
            case CID.LONG:   opcode = OPC.ASTORE_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:  opcode = OPC.ASTORE_F; break;
            case CID.DOUBLE: opcode = OPC.ASTORE_D; break;
/*end[FLOATS]*/
            default:         opcode = OPC.ASTORE_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode));
    }

    /**
     * Common functionality for tracing absolute and conditional branches.
     *
     * @param opcode int
     * @param target Target
     */
    private void doBranch(int opcode, Target target) {
        out.append(Mnemonics.getMnemonic(opcode) + " " + getAddressOf(target));
    }

    /**
     * {@inheritDoc}
     */
    public void doBranch(Branch instruction) {
        doBranch(OPC.GOTO, instruction.getTarget());
    }

    /**
     * {@inheritDoc}
     */
    public void doCheckCast(CheckCast instruction) {
        out.append(Mnemonics.getMnemonic(OPC.CHECKCAST)+" "+instruction.getType());
    }

    /**
     * {@inheritDoc}
     */
    public void doConversionOp(ConversionOp instruction) {
        out.append(Mnemonics.getMnemonic(instruction.getOpcode()));
    }

    /**
     * {@inheritDoc}
     */
    public void doComparisonOp(ComparisonOp instruction) {
        if (instruction.isLCMP()) {
            out.append("call com.sun.squawk.VM.lcmp");  
        } else {
            out.append(Mnemonics.getMnemonic(instruction.getOpcode()));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doTry(Try instruction) {
        out.append("try {");
    }

    /**
     * {@inheritDoc}
     */
    public void doTryEnd(TryEnd instruction) {
        out.append("} // tryend");
    }

    /**
     * {@inheritDoc}
     */
    public void doIf(If instruction) {
        doBranch(instruction.getOpcode(), instruction.getTarget());
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
        out.append(Mnemonics.getMnemonic(instruction.isIncrement() ? OPC.INC : OPC.DEC) + " " + instruction.getLocal());
    }

    /**
     * {@inheritDoc}
     */
    public void doInstanceOf(InstanceOf instruction) {
        out.append(Mnemonics.getMnemonic(OPC.INSTANCEOF)+" "+instruction.getCheckType());
    }

    /**
     * Format a member.
     *
     * @param member the member
     * @return the string representation
     */
    private String member(Member member) {
        return " " + member + " [" + member.getOffset() + "]";
    }

    /**
     * {@inheritDoc}
     */
    public void doFindSlot (FindSlot instruction) {
        Method method = instruction.getMethod();
        out.append(Mnemonics.getMnemonic(OPC.FINDSLOT) + member(method));
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeSlot(InvokeSlot instruction) {
        int opcode;
        Method method = instruction.getMethod();
        switch (method.getReturnType().getSystemID()) {
            case CID.VOID:    opcode = OPC.INVOKESLOT_V; break;
            case CID.BOOLEAN: // fall through ...
            case CID.BYTE:    // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
            case CID.INT:     opcode = OPC.INVOKESLOT_I; break;
            case CID.LONG:    opcode = OPC.INVOKESLOT_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.INVOKESLOT_F; break;
            case CID.DOUBLE:  opcode = OPC.INVOKESLOT_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.INVOKESLOT_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) /*+ " " + method*/);
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeStatic(InvokeStatic instruction) {
        int opcode;
        Method method = instruction.getMethod();
        switch (method.getReturnType().getSystemID()) {
            case CID.VOID:    opcode = OPC.INVOKESTATIC_V; break;
            case CID.BOOLEAN: // fall through ...
            case CID.BYTE:    // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
            case CID.INT:     opcode = OPC.INVOKESTATIC_I; break;
            case CID.LONG:    opcode = OPC.INVOKESTATIC_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.INVOKESTATIC_F; break;
            case CID.DOUBLE:  opcode = OPC.INVOKESTATIC_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.INVOKESTATIC_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) + member(method));
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeSuper(InvokeSuper instruction) {
        int opcode;
        Method method = instruction.getMethod();
        switch (method.getReturnType().getSystemID()) {
            case CID.VOID:    opcode = OPC.INVOKESUPER_V; break;
            case CID.BOOLEAN: // fall through ...
            case CID.BYTE:    // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
            case CID.INT:     opcode = OPC.INVOKESUPER_I; break;
            case CID.LONG:    opcode = OPC.INVOKESUPER_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.INVOKESUPER_F; break;
            case CID.DOUBLE:  opcode = OPC.INVOKESUPER_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.INVOKESUPER_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) + member(method));
    }

    /**
     * {@inheritDoc}
     */
    public void doInvokeVirtual(InvokeVirtual instruction) {
        int opcode;
        Method method = instruction.getMethod();
        switch (method.getReturnType().getSystemID()) {
            case CID.VOID:    opcode = OPC.INVOKEVIRTUAL_V; break;
            case CID.BOOLEAN: // fall through ...
            case CID.BYTE:    // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
            case CID.INT:     opcode = OPC.INVOKEVIRTUAL_I; break;
            case CID.LONG:    opcode = OPC.INVOKEVIRTUAL_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.INVOKEVIRTUAL_F; break;
            case CID.DOUBLE:  opcode = OPC.INVOKEVIRTUAL_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.INVOKEVIRTUAL_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) + member(method));
    }

    /**
     * {@inheritDoc}
     */
    public void doConstant(Constant instruction) {
        int tag = instruction.getTag();
        int opcode;
        Object value = instruction.getValue();
        switch (tag) {
/*if[FLOATS]*/
            case ConstantPool.CONSTANT_Double:  opcode = OPC.CONST_DOUBLE;    break;
            case ConstantPool.CONSTANT_Float:   opcode = OPC.CONST_FLOAT;     break;
/*end[FLOATS]*/
            case ConstantPool.CONSTANT_Long:    opcode = OPC.CONST_LONG;      break;
            case ConstantPool.CONSTANT_Integer: opcode = OPC.CONST_INT;       break;
            default: {
                if (value == null) {
                    opcode = OPC.CONST_NULL;
                    value = "";
                } else {
                    opcode = OPC.OBJECT;
                    value  = "\"" + value + "\"";
                }
                break;
            }
        }
        out.append(Mnemonics.getMnemonic(opcode) + " " + value);
    }

    /**
     * {@inheritDoc}
     */
    public void doCatch(Catch instruction) {
        /*
         * Insert an CONST_NULL instruction because the handler
         * is a target for regular control flow. See comment in
         * Catch.java
         */
        if (instruction.isControlFlowTarget()) {
            out.append(Mnemonics.getMnemonic(OPC.CONST_NULL));
            out.append("\n    ");
        }

        out.append("catch");
        out.append(" " + instruction.getType().getName() + " [ ");

        Enumeration tryBlocks = ir.getExceptionHandlers();
        if (tryBlocks != null) {
            while (tryBlocks.hasMoreElements()) {
                IRExceptionHandler handler = (IRExceptionHandler)tryBlocks.nextElement();
                if (handler.getCatch() == instruction) {

                    Target target = handler.getCatch().getTarget();
                    if (target.isBackwardBranchTarget()) {
                        out.append("(b)");
                    }
                    if (target.isForwardBranchTarget()) {
                        out.append("(f)");
                    }
                    out.append(getAddressOf(handler.getEntry()) + "-" + getAddressOf(handler.getExit()) + " ");
                }
            }
        }
        out.append("]");
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
            case CID.LONG:    opcode = OPC.GETFIELD_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.GETFIELD_F; break;
            case CID.DOUBLE:  opcode = OPC.GETFIELD_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.GETFIELD_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) + member(field));
    }

    /**
     * {@inheritDoc}
     */
    public void doGetStatic(GetStatic instruction) {
        int opcode;
        Field field = instruction.getField();
        switch (field.getType().getSystemID()) {
            case CID.BYTE:    // fall through ...
            case CID.BOOLEAN: // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
            case CID.INT:     opcode = OPC.GETSTATIC_I; break;
            case CID.LONG:    opcode = OPC.GETSTATIC_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.GETSTATIC_F; break;
            case CID.DOUBLE:  opcode = OPC.GETSTATIC_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.GETSTATIC_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) + member(field));
    }

    /**
     * {@inheritDoc}
     */
    public void doLoadLocal(LoadLocal instruction) {
        out.append(Mnemonics.getMnemonic(OPC.LOAD) + " " + instruction.getLocal());
    }

    /**
     * {@inheritDoc}
     */
    public void doLookupSwitch(LookupSwitch instruction) {
        out.append("lookupswitch default=" + getAddressOf(instruction.getDefaultTarget()));
        Target[] targets = instruction.getTargets();
        out.append(" npairs=" + targets.length + " (");

        Object caseValuesObject = instruction.getCaseValues();
        if (caseValuesObject instanceof int[]) {
            int[] caseValues = (int[]) caseValuesObject;
            for (int i = 0; i != targets.length; ++i) {
                out.append(caseValues[i] + " -> " + getAddressOf(targets[i]));
                if (i != targets.length - 1) {
                    out.append(", ");
                }
            }
        } else if (caseValuesObject instanceof short[]) {
            short[] caseValues = (short[]) caseValuesObject;
            for (int i = 0; i != targets.length; ++i) {
                out.append(caseValues[i] + " -> " + getAddressOf(targets[i]));
                if (i != targets.length - 1) {
                    out.append(", ");
                }
            }
        } else if (caseValuesObject instanceof byte[]) {
            byte[] caseValues = (byte[]) caseValuesObject;
            for (int i = 0; i != targets.length; ++i) {
                out.append(caseValues[i] + " -> " + getAddressOf(targets[i]));
                if (i != targets.length - 1) {
                    out.append(", ");
                }
            }
        }
        out.append(")");
    }

    /**
     * {@inheritDoc}
     */
    public void doMonitorEnter(MonitorEnter instruction) {
        out.append(Mnemonics.getMnemonic(instruction.getObject() == null ? OPC.CLASS_MONITORENTER : OPC.MONITORENTER));
    }

    /**
     * {@inheritDoc}
     */
    public void doMonitorExit(MonitorExit instruction) {
        out.append(Mnemonics.getMnemonic(instruction.getObject() == null ? OPC.CLASS_MONITOREXIT : OPC.MONITOREXIT));
    }

    /**
     * {@inheritDoc}
     */
    public void doNegationOp(NegationOp instruction) {
        out.append(Mnemonics.getMnemonic(instruction.getOpcode()));
    }

    /**
     * {@inheritDoc}
     */
    public void doNewArray(NewArray instruction) {
        out.append(Mnemonics.getMnemonic(OPC.NEWARRAY) + " " + instruction.getType().getName());
    }

    /**
     * {@inheritDoc}
     */
    public void doNewDimension(NewDimension instruction) {
        out.append(Mnemonics.getMnemonic(OPC.NEWDIMENSION));
    }

    /**
     * {@inheritDoc}
     */
    public void doNew(New instruction) {
        out.append(Mnemonics.getMnemonic(OPC.NEW) + " " + instruction.getRuntimeType().getName());
    }

    /**
     * {@inheritDoc}
     */
    public void doPhi(Phi instruction) {
        Target target = instruction.getTarget();
        out.append("phi");
        if (target.isBackwardBranchTarget()) {
            out.append("(b)");
        }
        if (target.isForwardBranchTarget()) {
            out.append("(f)");
        }
        out.append(" stack={" + Klass.getNames(target.getStack()) + "} locals={" + Klass.getNames(target.getLocals()) + "}");
    }

    /**
     * {@inheritDoc}
     */
    public void doPop(Pop instruction) {
        if (instruction.value().isOnStack()) {
            if (instruction.value().getType().isDoubleWord()) {
                out.append(Mnemonics.getMnemonic(OPC.POP_2));
            } else {
                out.append(Mnemonics.getMnemonic(OPC.POP_1)).append(' ').append(instruction.value());
            }
        } else {
            out.append("/* pop */");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doPosition(Position instruction) {
        out.append("// position");
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
                case CID.LONG:    opcode = OPC.RETURN_L; break;
/*if[FLOATS]*/
                case CID.FLOAT:   opcode = OPC.RETURN_F; break;
                case CID.DOUBLE:  opcode = OPC.RETURN_D; break;
/*end[FLOATS]*/
                default:          opcode = OPC.RETURN_O; break;
            }
        }
        out.append(Mnemonics.getMnemonic(opcode));
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
            case CID.LONG:    opcode = OPC.PUTFIELD_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.PUTFIELD_F; break;
            case CID.DOUBLE:  opcode = OPC.PUTFIELD_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.PUTFIELD_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) + member(field));
    }

    /**
     * {@inheritDoc}
     */
    public void doPutStatic(PutStatic instruction) {
        int opcode;
        Field field = instruction.getField();
        switch (field.getType().getSystemID()) {
            case CID.BYTE:    // fall through ...
            case CID.BOOLEAN: // fall through ...
            case CID.SHORT:   // fall through ...
            case CID.CHAR:    // fall through ...
            case CID.INT:     opcode = OPC.PUTSTATIC_I; break;
            case CID.LONG:    opcode = OPC.PUTSTATIC_L; break;
/*if[FLOATS]*/
            case CID.FLOAT:   opcode = OPC.PUTSTATIC_F; break;
            case CID.DOUBLE:  opcode = OPC.PUTSTATIC_D; break;
/*end[FLOATS]*/
            default:          opcode = OPC.PUTSTATIC_O; break;
        }
        out.append(Mnemonics.getMnemonic(opcode) + member(field));
    }

    /**
     * {@inheritDoc}
     */
    public void doStoreLocal(StoreLocal instruction) {
        out.append(Mnemonics.getMnemonic(OPC.STORE) + " " + instruction.getLocal());
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
        out.append("/* " + Opcode.mnemonics[instruction.getOpcode()] + " */");
    }


    /**
     * {@inheritDoc}
     */
    public void doTableSwitch(TableSwitch instruction) {
        out.append("tableswitch default=" + getAddressOf(instruction.getDefaultTarget()));
        Target[] targets = instruction.getTargets();
        int low = instruction.getLow();
        int high = instruction.getHigh();
        out.append(" low=" + low + " high=" + high + " (");
        for (int i = 0; i != targets.length; ++i) {
            out.append((low + i) + " -> " + getAddressOf(targets[i]));
            if (i != targets.length - 1) {
                out.append(", ");
            }
        }
        out.append(")");
    }

    /**
     * {@inheritDoc}
     */
    public void doThrow(Throw instruction) {
        out.append(Mnemonics.getMnemonic(OPC.THROW) + " " + instruction.getThrowable().getType().getName());
    }
}
