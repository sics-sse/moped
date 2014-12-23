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
 * An instance of <code>If</code> represents an instruction that pops a value
 * off the operand stack, compares it against 0 or <code>null</code> and
 * transfers the flow of control to a specified address if the result of the
 * comparison is true.
 *
 */
public class If extends Branch {

    /**
     * The value that is compared against 0 or <code>null</code>.
     */
    private StackProducer value;

    /**
     * The Squawk opcode corresponding to this operation.
     */
    private final int opcode;

    /**
     * Creates a <code>If</code> instance representing an instruction that
     * pops a value off the operand stack, compares it against 0 or
     * <code>null</code> and transfers the flow of control to a specified
     * address if the result of the comparison is true.
     *
     * @param  value   the value that is compared against 0 or <code>null</code>
     * @param  opcode  the opcode denoting the semantics of the comparison
     * @param  target  the address to which the flow of control is transferred
     *                 if the comparison returns true
     */
    public If(StackProducer value, int opcode, Target target) {
        super(target);
        this.opcode = opcode;
        this.value = value;
    }

    /**
     * Gets the value that is compared against 0 or <code>null</code>.
     *
     * @return the value that is compared against 0 or <code>null</code>
     */
    public StackProducer getValue() {
        return value;
    }

    /**
     * Gets the Squawk opcode corresponding this conditional branch operation.
     *
     * @return the Squawk opcode corresponding this conditional branch operation
     */
    public final int getOpcode() {
        return opcode;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doIf(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        value = visitor.doOperand(this, value);
    }
}
