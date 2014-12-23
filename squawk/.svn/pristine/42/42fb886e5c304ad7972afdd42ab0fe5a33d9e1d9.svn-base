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
 * An instance of <code>Branch</code> represents an instruction that transfers
 * the flow of control to a specified address.<p>
 *
 * This class is also the base of all the classes that represent a transfer of
 * flow control to one or more targets.
 *
 */
public class Branch extends Instruction {

    /**
     * The address to which the flow of control is transferred.
     */
    private final Target target;

    /**
     * True if the branch is forward.
     */
    private final boolean isForward;

    /**
     * Creates a <code>Branch</code> instance representing an instruction that
     * transfers the flow of control to a specified address.
     *
     * @param target the address to which the flow of control is transferred
     */
    public Branch(Target target) {
        this.target = target;
        isForward = target.getTargetedInstruction() == null;
    }

    /**
     * Gets the address to which the flow of control is transferred.
     *
     * @return the address to which the flow of control is transferred
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Test to see if the branch goes forward.
     *
     * @return true if it is
     */
    public boolean isForward() {
        return isForward;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean constrainsStack() {
        //return !isForward;
        return true; // For Cristina's compiler
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doBranch(this);
    }
}
