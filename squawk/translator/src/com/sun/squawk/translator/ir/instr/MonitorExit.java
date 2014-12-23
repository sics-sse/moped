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
 * An instance of <code>MonitorExit</code> represents an instruction that
 * pops a referenced typed value off the operand stack and releases a
 * lock on its monitor.
 *
 */
public final class MonitorExit extends Instruction {

    /**
     * The object whose monitor is released. This will be null if this
     * instruction is locking the monitor of the enclosing class to implement
     * static method synchronization.

     */
    private StackProducer object;

    /**
     * Creates a <code>MonitorExit</code> instance representing an instruction
     * that pops a referenced typed value off the operand stack and releases a
     * lock on its monitor.
     *
     * @param object  the object whose monitor is released or null when this
     *                instruction is implementing static method synchronization
     */
    public MonitorExit(StackProducer object) {
        this.object = object;
    }

    /**
     * Gets the object whose monitor is released.  This will be null if this
     * instruction is locking the monitor of the enclosing class to implement
     * static method synchronization.

     *
     * @return the object whose monitor is released or <code>null</code>
     */
    public StackProducer getObject() {
        return object;
    }

    /**
     * {@inheritDoc}
     */
    public boolean constrainsStack() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doMonitorExit(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        if (object != null) {
            object = visitor.doOperand(this, object);
        }
    }
}
