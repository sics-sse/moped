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
import com.sun.squawk.util.Assert;

/**
 * An instance of <code>ComparisonOp</code> represents an instruction that
 * pops two values off the operand stack, compares them and pushes the
 * result of the comparison.  This type of instruction is not directly
 * supported in Squawk and must be converted.
 *
 */
public final class ComparisonOp extends StackProducer {
    
    /* Definition of native-method based comparisons: */
    
    /** Dummy opcode for LCMP: */
    public static final int LCMP = -1;

    /**
     * The left operand of the operation.
     */
    private StackProducer left;

    /**
     * The left operand of the operation.
     */
    private StackProducer right;

    /**
     * The JVM opcode corresponding to this operation.
     */
    private final int opcode;

    /**
     * Creates a <code>ComparisonOp</code> instance representing a binary
     * comparison operation.
     *
     * @param   left    the left operand of the operation
     * @param   right   the right operand of the operation
     * @param   opcode  the Squawk opcode corresponding to the operation
     */
    public ComparisonOp(StackProducer left, StackProducer right, int opcode) {
        super(Klass.INT);
        this.left   = left;
        this.right  = right;
        this.opcode = opcode;
    }

    /**
     * Gets the left operand of this comparison operation.
     *
     * @return the left operand of this comparison operation
     */
    public StackProducer getLeft() {
        return left;
    }

    /**
     * Gets the right operand of this comparison operation.
     *
     * @return the right operand of this comparison operation
     */
    public StackProducer getRight() {
        return right;
    }

    /**
     * Gets the Squawk opcode corresponding this comparison operation.
     *
     * @return the Squawk opcode corresponding this comparison operation
     */
    public int getOpcode() {
        Assert.that(opcode >= 0);
        return opcode;
    }

    /**
     * Returns true if doing a long comparison (performed by a native method...).
     */
    public boolean isLCMP() {
        return (opcode == LCMP);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only the LCMP veriosn may constrain the stack, and only if it isn't later replaced by an "if".
     * This could be optimized a little by implementing
     * some kind of opcode lookahead.
     */
    public boolean constrainsStack() {
        return isLCMP();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doComparisonOp(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        left = visitor.doOperand(this, left);
        right = visitor.doOperand(this, right);
    }
}
