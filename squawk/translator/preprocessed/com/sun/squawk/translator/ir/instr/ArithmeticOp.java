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
import com.sun.squawk.vm.OPC;

/**
 * An instance of <code>ArithmeticOp</code> represents an binary arithmetic
 * operation that pops two values off the operand stack and pushes the
 * result of the operation.
 *
 */
public final class ArithmeticOp extends StackProducer {

    /**
     * The left operand of the operation.
     */
    private StackProducer left;

    /**
     * The left operand of the operation.
     */
    private StackProducer right;

    /**
     * The Squawk opcode corresponding to this operation.
     */
    private final int opcode;

    /**
     * Creates a <code>ArithmeticOp</code> instance representing a binary
     * arithmetic operation.
     *
     * @param   left    the left operand of the operation
     * @param   right   the right operand of the operation
     * @param   opcode  the Squawk opcode corresponding to the operation
     */
    public ArithmeticOp(StackProducer left, StackProducer right, int opcode) {
        super(left.getType());
        this.left   = left;
        this.right  = right;
        this.opcode = opcode;
    }

    /**
     * Gets the left operand of this arithmetic operation.
     *
     * @return the left operand of this arithmetic operation
     */
    public StackProducer getLeft() {
        return left;
    }

    /**
     * Gets the right operand of this arithmetic operation.
     *
     * @return the right operand of this arithmetic operation
     */
    public StackProducer getRight() {
        return right;
    }

    /**
     * Gets the Squawk opcode corresponding this arithmetic operation.
     *
     * @return the Squawk opcode corresponding this arithmetic operation
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doArithmeticOp(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean mayCauseGC(boolean isStatic) {
        return opcode == OPC.DIV_I || opcode == OPC.DIV_L || opcode == OPC.REM_I || opcode == OPC.REM_L;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        left  = visitor.doOperand(this, left);
        right = visitor.doOperand(this, right);
    }
}
