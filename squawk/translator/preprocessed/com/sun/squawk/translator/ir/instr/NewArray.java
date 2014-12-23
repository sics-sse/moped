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
 * An instance of <code>NewArray</code> represents an instruction that pops
 * a value from the operand stack and uses it to create an new array of a
 * specified type whose length is determined by the popped value.
 *
 */
public final class NewArray extends StackProducer {

    /**
     * The length of the array.
     */
    private StackProducer length;

    /**
     * Creates a <code>NewArray</code> instance representing an instruction
     * that pops a value from the operand stack and uses it to create an new
     * array of a specified type whose length is determined by the popped value.
     *
     * @param type    the type of the array to create
     * @param length  the length of the array
     */
    public NewArray(Klass type, StackProducer length) {
        super(type);
        this.length = length;
    }

    /**
     * Gets the length of the array.
     *
     * @return the length of the array
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
    public Object getConstantObject() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doNewArray(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        length = visitor.doOperand(this, length);
    }
}
