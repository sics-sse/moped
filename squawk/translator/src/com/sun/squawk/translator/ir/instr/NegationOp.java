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
 * An instance of <code>NegationOp</code> represents an unary arithmetic
 * operation that pops a value off the operand stack and pushes its
 * negated value.
 *
 */
public final class NegationOp extends StackProducer {

    /**
     * The value being negated.
     */
    private StackProducer value;

    /**
     * The Squawk opcode corresponding to this operation.
     */
    private final int opcode;

    /**
     * Creates a <code>NegationOp</code> instance representing an instruction
     * that negates a value on the operand stack.
     *
     * @param value   the value being negated
     * @param opcode  the Squawk opcode corresponding to the operation
     */
    public NegationOp(StackProducer value, int opcode) {
        super(value.getType());
        this.value = value;
        this.opcode = opcode;
    }

    /**
     * Gets the value being negated.
     *
     * @return the value being negated
     */
    public StackProducer getValue() {
        return value;
    }

    /**
     * Gets the Squawk opcode corresponding this negation operation.
     *
     * @return the Squawk opcode corresponding this negation operation
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doNegationOp(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        value = visitor.doOperand(this, value);
    }
}
