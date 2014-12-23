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
 * An instance of <code>StoreLocal</code> represents an instruction that pops
 * a value off the operand stack and stores it to a local variable.
 *
 */
public final class StoreLocal extends Instruction implements LocalVariable, Mutator {

    /**
     * The value being stored.
     */
    private StackProducer value;

    /**
     * The local containing the value that is stored to.
     */
    private final Local local;

    /**
     * Creates an instance of <code>StoreLocal</code> that pops a value from
     * the operand stack and stores it to a given local.
     *
     * @param local  the local to which the value is stored
     * @param value  the value stored to the local variable
     */
    public StoreLocal(Local local, StackProducer value) {
        this.local = local;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    public Local getLocal() {
        return local;
    }

    /**
     * Gets the value stored to the local variable.
     *
     * @return the value stored to the local variable
     */
    public StackProducer getValue() {
        return value;
    }

    /**
     * Returns <code>true</code> to indicate that a store writes a value
     * to the referenced local variable.
     *
     * @return  true
     */
    public boolean writesValue() {
        return true;
    }

    /**
     * Returns <code>false</code> to indicate that a store does not read a value
     * from the referenced local variable.
     *
     * @return  false
     */
    public boolean readsValue() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Klass getMutationType() {
        return local.getType();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doStoreLocal(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        value = visitor.doOperand(this, value);
    }
}
