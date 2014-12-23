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
 * An instance of <code>ConversionOp</code> represents an unary
 * operation that pops a value of a given type off the operand stack,
 * converts it to a value of another type and pushes the result of the
 * operation.
 *
 */
public final class ConversionOp extends StackProducer {

    /**
     * The Squawk opcode corresponding to this operation.
     */
    private final int opcode;

    /**
     * The value being converted.
     */
    private StackProducer value;

    /**
     * Creates a <code>ConversionOp</code> instance representing an
     * instruction that converts a value on the operand stack from one type to
     * another.
     *
     * @param  to     the type the value is converted to
     * @param  value  the value being converted
     * @param  opcode the Squawk opcode corresponding to the operation
     */
    public ConversionOp(Klass to, StackProducer value, int opcode) {
        super(to);
        this.value = value;
        this.opcode = opcode;
    }

    /**
     * Gets the value being converted.
     *
     * @return the value being converted
     */
    public StackProducer getValue() {
        return value;
    }

    /**
     * Gets the type the value is being converted to.
     *
     * @return the type the value is being converted tp
     */
    public Klass getTo() {
        return getType();
    }

    /**
     * Gets the type the value is being converted from.
     *
     * @return the type the value is being converted from
     */
    public Klass getFrom() {
        return value.getType();
    }

    /**
     * Gets the Squawk opcode corresponding this conversion operation.
     *
     * @return the Squawk opcode corresponding this conversion operation
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doConversionOp(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        value = visitor.doOperand(this, value);
    }
}
