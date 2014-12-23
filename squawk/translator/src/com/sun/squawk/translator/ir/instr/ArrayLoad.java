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
 * An instance of <code>ArrayLoad</code> represents an instruction that
 * loads an value from an array at given index and pushes it onto the
 * operand stack.
 *
 */
public final class ArrayLoad extends StackProducer {

    /**
     * The array reference.
     */
    private StackProducer array;

    /**
     * The array index.
     */
    private StackProducer index;

    /**
     * Creates an <code>ArrayLoad</code> instance for an instruction that loads
     * a value from an array and pushes it to the operand stack.
     *
     * @param  componentType the type of the value
     * @param  array         the array from which the value is loaded
     * @param  index         the index of the loaded value
     */
    public ArrayLoad(Klass componentType, StackProducer array, StackProducer index) {
        super(componentType);
        this.array = array;
        this.index = index;
    }

    /**
     * Gets the array reference.
     *
     * @return the array reference
     */
    public StackProducer getArray() {
        return array;
    }

    /**
     * Gets the array index.
     *
     * @return the array index
     */
    public StackProducer getIndex() {
        return index;
    }

    /**
     * {@inheritDoc}
     */
    public boolean mayCauseGC(boolean isStatic) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doArrayLoad(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        array = visitor.doOperand(this, array);
        index = visitor.doOperand(this, index);
    }
}
