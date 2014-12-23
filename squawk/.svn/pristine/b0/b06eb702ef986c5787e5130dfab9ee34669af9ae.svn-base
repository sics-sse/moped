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
 * An instance of <code>Position</code> is a pseudo instruction that
 * represents a logical instruction position whose physical address may change
 * as the bytecodes are transformed from JVM bytecodes to Squawk
 * bytecodes. These positions are referenced by sub-attributes of
 * a "Code" attribute such as a "LocalVariableTable" or "LineNumberTable"
 * attribute.
 *
 */
public final class Position extends Instruction implements PseudoInstruction {

    /**
     * Creates a <code>Position</code> instance to represent
     * a logical instruction position whose physical address may change
     * as the bytecodes are transformed from JVM bytecodes to Squawk
     * bytecodes.
     *
     * @param offset the address of this position in the enclosing JVM bytecodes
     */
    public Position(int offset) {
        setBytecodeOffset(offset);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doPosition(this);
    }

}
