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
import com.sun.squawk.translator.ir.instr.StackMerge;
import com.sun.squawk.translator.ir.instr.*;
import java.util.Enumeration;
import com.sun.squawk.*;

/**
 * An instance of <code>Target</code> represents an index in a
 * bytecode array that is the target of a control flow instruction or the
 * entry point to an exception handler. That is, all the points in the
 * bytecode for which there must be a stack map entry.
 *
 */
public final class Target {

    /**
     * A constant denoting that there is an empty stack at this target.
     */
    private static final StackProducer[] EMPTY_DERIVED_STACK = {};

    /**
     * The bytecode address represented by this object.
     */
    private final int address;

    /**
     * The types on the operand stack as specified by a stack map entry
     * for this address. This array includes entries for the second word
     * of a two word type (such as long and double) and so its length
     * gives the size (in words) of the stack.
     */
    private final Klass[] stack;

    /**
     * The instructions that produced the values on the operand
     * stack at the target represented by this address.
     */
    private final StackProducer[] derivedStack;

    /**
     * The types in the local variables array as specified by a stack
     * map entry for this address. Double word types (<code>long</code>s and
     * <code>double</code>s) are represented by two single word types.
     */
    public final Klass[] locals;

    /**
     * The instruction at this target.
     */
    private TargetedInstruction instruction;

    /**
     * Flag to indicate that this target is the start of an exception handler.
     */
    private boolean isCatchTarget;
    
    /**
     * Flag to indicate that jumps to this target are illegal in Squawk, due to stack constraints at back-branches.
     * This should only happen in TCK tests, in code that is not reachable, and will be dead-code-eliminated.
     * The error (initially WARNING, unitl dead code elim is checked in)  is reported in InstructionEmitter
     */
    private boolean fatalTarget;

    /**
     * Flag to indicate that this target is used by a forward branch instruction.
     */
    private boolean isForwardBranchTarget;

    /**
     * Flag to indicate that this target is used by a backward branch instruction.
     */
    private boolean isBackwardBranchTarget;

    /**
     * Creates a new <code>Target</code> instance.
     *
     * @param  address the bytecode address represented by the target
     * @param  stack   the types on the operand stack
     * @param  locals  the types in the local variables array
     */
    public Target(int address, Klass[] stack, Klass[] locals) {
        this.address = address;
        this.stack = stack;
        this.locals = locals;
        if (stack.length == 0) {
            this.derivedStack = EMPTY_DERIVED_STACK;
        } else {
            this.derivedStack = new StackProducer[stack.length];
        }
    }

    /**
     * Gets the bytecode address represented by this target.
     *
     * @return the bytecode address represented by this object
     */
    public int getAddress() {
        return address;
    }

    /**
     * Get the real offset to the target instruction.
     *
     * @return the offset
     */
    public int getBytecodeOffset() {
        return ((Instruction)instruction).getBytecodeOffset();
    }

    /**
     * Gets the types on the operand stack as specified by a stack map entry
     * for this address.
     *
     * @return the types on the operand stack
     */
    public Klass[] getStack() {
        return stack;
    }

    /**
     * Gets the types in the local variables array as specified by a stack
     * map entry for this address.
     *
     * @return the types in the local variables array
     */
    public Klass[] getLocals() {
        return locals;
    }

    /**
     * Returns a <code>String</code> representation of this target.
     *
     * @return a <code>String</code> representation of this target
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(20);
        return buf.append(getAddress()).append(": stack={").
                append(Klass.getNames(stack)).
                append("} locals={").
                append(Klass.getNames(locals)).
                append("}").toString();
    }

    /**
     * Merges the current state of the operand stack in a frame with the
     * state of the operand stack at this target.
     *
     * @param frame  the frame encapsulating the operand stack whose current
     *               state is to be merged
     */
    public void merge(Frame frame) {
        int sp = frame.getStackSize();
        for (int i = 0, mapIndex = 0 ; i < sp ; ++i, ++mapIndex) {
            StackProducer producer = frame.getStackAt(i);
            StackProducer derived = derivedStack[i];
            if (derived != producer) {
                if (derived == null) {
                    derivedStack[i] = producer;
                } else {
                    StackMerge merge;
                    if (derived instanceof StackMerge) {
                        merge = (StackMerge)derived;
                    } else {
                        /*
                         * The result of the merge is dictated by the stack map
                         */
                        merge = new StackMerge(stack[mapIndex]);
                        merge.addProducer(derived);
                        derivedStack[i] = merge;
                    }
                    merge.addProducer(producer);
                }
            }
            if (producer.getType().isDoubleWord()) {
                i++;
                mapIndex++;
            }
        }
    }

    /**
     * Gets the instructions that produced the values on the operand
     * stack at the target represented by this address.
     *
     * @return the instructions that produced the values on the operand
     *         stack at the target represented by this address
     */
    StackProducer[] getDerivedStack() {
        return derivedStack;
    }

    /**
     * Sets the instruction targeted by this target.
     *
     * @param instruction  the instruction targeted by this target
     */
    public void setTargetedInstruction(TargetedInstruction instruction) {
        Assert.that(this.instruction == null, "cannot reset targeted instruction");
        this.instruction = instruction;
    }

    /**
     * Sets the catch instruction targeted by this target.
     *
     * @param instruction  the instruction targeted by this target
     */
    public void setTargetedCatchInstruction(Catch instruction) {
        setTargetedInstruction(instruction);
        isCatchTarget = true;
    }

    /**
     * Gets the instruction targeted by this target.
     *
     * @return the instruction targeted by this target
     */
    public TargetedInstruction getTargetedInstruction() {
        return instruction;
    }

    /**
     * Flag to indicate that the target is used by a forward branch.
     */
    public void markAsForwardBranchTarget() {
        isForwardBranchTarget = true;
        Assert.that(isBackwardBranchTarget == false);
    }

    /**
     * Flag to indicate that the target is used by a backward branch.
     */
    public void markAsBackwardBranchTarget() {
        isBackwardBranchTarget = true;
    }

    /**
     * Test to see of this is a backward branch target.
     *
     * @return true if it is
     */
    public boolean isBackwardBranchTarget() {
        return isBackwardBranchTarget;
    }

    /**
     * Test to see of this is a forward branch target.
     *
     * @return true if it is
     */
    public boolean isForwardBranchTarget() {
        return isForwardBranchTarget;
    }

    /**
     * Gets the instruction denoting the position in the IR at which the
     * code for this handler starts.
     *
     * @return  the entry position of this handler's code
     */
    public Catch getCatch() {
        Assert.that(isCatchTarget);
        return (Catch)instruction;
    }

    /**
     * Returns true if it an error to actually jump to this target (results inon-empty operand stack)
     *
     * @return  true if a fatelTarget
     */
    public boolean isFatalTarget() {
        return fatalTarget;
    }
    
    /**
     * Mark this target as being fatal to jump to.
     */
    void markAsFatalTarget() {
        fatalTarget = true;
    }
    
    /*---------------------------------------------------------------------------*\
     *                      Slot clearing analysis                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Specifies which locals that hold pointers are initialized at this target.
     */
//    private Vector uninitializedLocals;

//    public void addUnitializedLocal(Local local) {
//        if (uninitializedLocals == null) {
//            uninitializedLocals = new Vector();
//        }
//        uninitializedLocals.addElement(local);
//    }

//    public Enumeration getUninitializedLocals() {
//        return uninitializedLocals == null ? null : uninitializedLocals.elements();
//    }
}
