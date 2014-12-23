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
 * An instance of <code>ArrayLength</code> represents an instruction that pops
 * an array reference from the operand stack and pushes its length.
 *
 */
public final class ArrayLength extends StackProducer {

    /**
     * The array reference.
     */
    private StackProducer array;

    /**
     * Creates a <code>ArrayLength</code> instance representing an instruction
     * that pops an array reference from the operand stack and pushes its
     * length.
     *
     * @param array   the array reference
     */
    public ArrayLength(StackProducer array) {
        super(Klass.INT);
        this.array = array;
    }

    /**
     * Gets the referenced array.
     *
     * @return the referenced array
     */
    public StackProducer getArray() {
        return array;
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
        visitor.doArrayLength(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        array = visitor.doOperand(this, array);
    }

}
