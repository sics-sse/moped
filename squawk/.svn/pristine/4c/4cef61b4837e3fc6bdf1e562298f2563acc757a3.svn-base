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

/**
 * An instance of <code>NewDimension</code> represents an instruction that
 * pops an array reference and a length value from the operand stack and
 * adds another dimension to the array of the specified length. The array
 * reference is then pushed back to the stack.
 *
 */
public final class NewDimension extends StackProducer {

    /**
     * The array to be extended.
     */
    private StackProducer array;

    /**
     * The length of the new dimension.
     */
    private StackProducer length;

    /**
     * Creates a <code>NewDimension</code> instance representing an instruction
     * that pops an array reference and a length value from the operand stack
     * and adds another dimension to the array of the specified length.
     *
     * @param  array   the array to be extended by one dimension
     * @param  length  the length of the new dimension
     */
    public NewDimension(StackProducer array, StackProducer length) {
        super(array.getType());
        this.array = array;
        this.length = length;
    }

    /**
     * Gets the array being extended.
     *
     * @return the array being extended
     */
    public StackProducer getArray() {
        return array;
    }

    /**
     * Gets the length of the new dimension.
     *
     * @return the length of the new dimension
     */
    public StackProducer getLength() {
        return length;
    }

    /**
     * {@inheritDoc}
     */
    public boolean constrainsStack() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doNewDimension(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        array = visitor.doOperand(this, array);
        length = visitor.doOperand(this, length);
    }
}
