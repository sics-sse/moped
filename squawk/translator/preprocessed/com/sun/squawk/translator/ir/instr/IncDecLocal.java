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

import com.sun.squawk.translator.ir.*;
import com.sun.squawk.*;

/**
 * An instance of <code>IncDecLocal</code> represents an instruction that
 * adjusts the value of an integer typed local variable by 1 or -1.
 */
public final class IncDecLocal extends Instruction implements LocalVariable, Mutator {

    /**
     * The local containing the value that is adjusted.
     */
    private final Local local;

    /**
     * Specifies if this instruction increments or decrements the
     * local variable.
     */
    private final boolean increment;

    /**
     * Creates a <code>IncDecLocal</code> instance representing an instruction
     * that adjusts the value of an integer typed local variable by 1 or -1.
     *
     * @param local      the local variable adjusted by the instruction
     * @param increment  true if the adjustment is 1, false if it is -1
     */
    public IncDecLocal(Local local, boolean increment) {
        this.local = local;
        this.increment = increment;
    }

    /**
     * Determines if this instruction increments or decrements the
     * local variable.
     *
     * @return true if this instruction increments the local variable, false
     *         otherwise
     */
    public boolean isIncrement() {
        return increment;
    }

    /**
     * {@inheritDoc}
     */
    public Local getLocal() {
        return local;
    }

    /**
     * {@inheritDoc}
     */
    public Klass getMutationType() {
        return local.getType();
    }

    /**
     * Returns <code>true</code> to indicate that this instruction writes a
     * value to the referenced local variable.
     *
     * @return  false
     */
    public boolean writesValue() {
        return true;
    }

    /**
     * Returns <code>true</code> to indicate that this instruction reads a
     * value from the referenced local variable.
     *
     * @return true
     */
    public boolean readsValue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doIncDecLocal(this);
    }
}
