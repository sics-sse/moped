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
 * This represents a point in a method that is the explicit target of a
 * control flow instruction. If there is more than one control flow
 * instruction in the method that targets this point or if the instruction
 * immediately preceeding this point lets control flow fall through, then
 * a <code>Phi</code> instance also represents a merge of control flow.
 *
 */
public final class Phi extends Instruction implements TargetedInstruction {

    /**
     * The targeted address represented by this object.
     */
    private final Target target;

    /**
     * Creates an instance of <code>Phi</code> to represent a point in a
     * method that is the explicit target of one or more control flow
     * instructions.
     *
     * @param target  the object representing the targeted address
     */
    public Phi(Target target) {
        this.target = target;
        target.setTargetedInstruction(this);
    }

    /**
     * {@inheritDoc}
     */
    public Target getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doPhi(this);
    }
}
