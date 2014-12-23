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

package com.sun.squawk.translator.ir.instr;

import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.ir.*;
import com.sun.squawk.*;

/**
 * This is the base class of all the instruction that push a value to the
 * operand stack.
 *
 */
public abstract class StackProducer extends Instruction implements Mutator {

    /**
     * The type of the value pushed to the stack by this instruction.
     */
    private Klass type;

    /**
     * Counter of number of times the StackProducer is used.
     */
    private int useCount;

    /**
     * Creates an instance of an instruction that writes to the operand stack.
     *
     * @param type  the type of the value pushed to the stack by the instruction
     */
    public StackProducer(Klass type) {
        Assert.that(type != null);
        Assert.that(type != Klass.LONG2);
/*if[FLOATS]*/
        Assert.that(type != Klass.DOUBLE2);
/*end[FLOATS]*/
        this.type = type;
    }

    /**
     * Gets the type of the value pushed to the stack by this instruction.
     *
     * @return  the type of the value pushed to the stack by the instruction
     */
    public final Klass getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    public final Klass getMutationType() {
        return getType();
    }

    /**
     * Updates the type of this instruction.
     *
     * @param initializedType  the new type
     */
    public final void updateType(Klass newType) {
        type = newType;
    }

    /*---------------------------------------------------------------------------*\
     *                                 Duping                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Flags if the value pushed by this instruction was subquently manipulated
     * by one of the untyped stack manipulation instructions.
     */
    private boolean isDuped;

    /**
     * Sets the flag indicating that the value pushed by this instruction was
     * subquently manipulated by one of the untyped stack manipulation
     * instructions.
     */
    public void setDuped(Frame frame) {
        isDuped = true;
        if (!isSpilt()) {
            spill(frame.allocateLocalForSpill(this));
        }
    }

    /**
     * Unsets the flag indicating that the value pushed by this instruction was
     * subquently manipulated by one of the untyped stack manipulation
     * instructions.
     */
    public void cancelDuping() {
        Assert.that(isDuped == true);
        isDuped = false;
    }

    /**
     * Determines if the value pushed by this instruction was
     * subquently manipulated by one of the untyped stack manipulation
     * instructions.
     *
     * @return true the value pushed by this instruction was subquently
     *              manipulated by one of the untyped stack manipulation
     *              instructions
     */
    public boolean isDuped() {
        return isDuped;
    }


    /*---------------------------------------------------------------------------*\
     *                                Spilling                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * The local variable to which the value is spilt.
     */
    private Local spillLocal;

    /**
     * Determines whether or not the value pushed by this instruction
     * must be spilt.
     *
     * @return  true if this instruction spills
     */
    public boolean isSpilt() {
        return spillLocal != null;
    }

    /**
     * Determines whether or not the value pushed by this instruction
     * is really on the runtime stack or has been spilt.
     *
     * @return  true if this instruction's value is on the stack
     */
    public boolean isOnStack() {
        return !isSpilt();
    }

    /**
     * Sets the local variable to which the value is spilt.
     */
    public void spill(Local spillLocal) {
        this.spillLocal = spillLocal;
    }

    /**
     * Gets the local variable to which the value is spilt.
     *
     * @return the local variable to which the value is spilt
     */
    public Local getSpillLocal() {
        Assert.that(spillLocal != null, "producer doesn't spill");
        return spillLocal;
    }

    /**
     * Undoes the spilling.
     */
    public void cancelSpilling() {
        spillLocal = null;
    }

    /**
     * Increments the number of times the StackProducer is used.
     */
    public void incUseCount() {
        useCount++;
    }

    /**
     * Gets the the number of times the StackProducer is used.
     *
     * @return the number of times the StackProducer is used
     */
    public int getUseCount() {
        return useCount;
    }
}
