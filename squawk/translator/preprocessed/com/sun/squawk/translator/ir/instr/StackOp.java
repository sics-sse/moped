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
 * An instance of <code>StackOp</code> is a place holder in an IR for a point
 * at which there was a stack manipulation instruction in the input bytecode.
 * That is, it is a place holder for a <i>dup</i>, <i>dup_x1</i>, <i>dup_x2</i>,
 * <i>dup2</i>, <i>dup2_x1</i>, <i>dup2_x2</i> or <i>swap</i> instruction.
 *
 */
public final class StackOp extends Instruction {

    private final int opcode;

    /**
     * Creates an instance of <code>StackOp</code> as a place holder for
     * a stack manipulation instruction.
     *
     * @param opcode  the opcode of the stack manipulation instruction
     */
    public StackOp(int opcode) {
        this.opcode = opcode;
    }

    /**
     * Gets the opcode of the stack manipulation instruction for which this
     * object is a placeholder.
     *
     * @return the opcode of the encapsulated stack manipulation instruction
     */
    public int getOpcode() {
        return opcode;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doStackOp(this);
    }
}
