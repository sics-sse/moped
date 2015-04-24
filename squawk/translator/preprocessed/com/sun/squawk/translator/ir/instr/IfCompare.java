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
 * An instance of <code>IfCompare</code> represents an instruction that pops
 * two values off the operand stack, compares them against each other and
 * transfers the flow of control to a specified address if the result of the
 * comparison is true.
 *
 */
public final class IfCompare extends If {

    /**
     * The right hand value of the comparison.
     */
    private StackProducer right;

    /**
     * Creates a <code>If</code> instance representing an instruction that
     * pops two values off the operand stack, compares them against each
     * other and transfers the flow of control to a specified address if the
     * result of the comparison is true
     *
     * @param  left    the left hand value of the comparison
     * @param  right   the right hand value of the comparison
     * @param  opcode  the opcode denoting the semantics of the comparison
     * @param  target  the address to which the flow of control is transferred
     *                 if the comparison returns true
     */
    public IfCompare(StackProducer left, StackProducer right, int opcode, Target target) {
        super(left, opcode, target);
        this.right = right;
    }

    /**
     * Gets the left hand value of the comparison.
     *
     * @return the left hand value of the comparison
     */
    public StackProducer getLeft() {
        return getValue();
    }

    /**
     * Gets the right hand value of the comparison.
     *
     * @return the right hand value of the comparison
     */
    public StackProducer getRight() {
        return right;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doIfCompare(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        super.visit(visitor);
        right = visitor.doOperand(this, right);
    }
}
