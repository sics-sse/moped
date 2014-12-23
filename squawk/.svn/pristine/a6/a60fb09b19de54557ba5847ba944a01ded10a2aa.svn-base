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
 * An instance of <code>Return</code> represents an instruction that returns
 * the flow of control to the caller of the method enclosing the instruction.
 *
 */
public class Return extends Instruction implements Mutator {

    /**
     * The returned value or null if this is a void return.
     */
    private StackProducer value;

    /**
     * Creates a Return instance representing an instruction that returns
     * the flow of control to the caller of the method enclosing the
     * instruction.
     *
     * @param value  the returned value or <code>null</code> if this is
     *               a void return
     */
    public Return(StackProducer value) {
        this.value = value;
    }

    /**
     * Gets the returned value or <code>null</code> if this is a void return.
     *
     * @return the returned value or <code>null</code> if this is a void return
     */
    public StackProducer getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public Klass getMutationType() {
        return value == null ? Klass.VOID : value.getType();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doReturn(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        if (value != null) {
            value = visitor.doOperand(this, value);
        }
    }
}
