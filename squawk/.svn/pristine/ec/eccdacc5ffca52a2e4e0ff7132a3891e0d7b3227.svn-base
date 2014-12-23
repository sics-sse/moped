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
 * An instance of <code>FindSlot</code> represents an instruction that creates
 * the virtual slot number for an invoke interface from an interface name and
 * the runtime receiver object.
 *
 */
public final class FindSlot extends StackProducer {

    /**
     * The interface method invoked.
     */
    private final Method method;

    /**
     * The instruction producing the receiver.
     */
    private StackProducer receiver;

    /**
     * Creates a <code>FindSlot</code> instance representing an instruction that
     * creates the virtual slot number for an invoke interface.
     *
     * @param  method   the method invoked
     * @param  receiver the instruction producing the receiver
     */
    public FindSlot(Method method, StackProducer receiver) {
        super(Klass.INT);
        this.method   = method;
        this.receiver = receiver;
    }

    /**
     * Gets the interface invoked by this instruction.
     *
     * @return the method invoked by this instruction
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Get the instruction producing the receiver.
     *
     * @return  the instruction producing the receiver
     */
    public StackProducer getReceiver() {
        return receiver;
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
    public Object getConstantObject() {
        return getMethod().getDefiningClass();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doFindSlot(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        receiver = visitor.doOperand(this, receiver);
    }
}
