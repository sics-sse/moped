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
 * An instance of <code>InstanceOf</code> represents an instruction that pops
 * an object from the operand stack, tests to see if it is assignable to a
 * specified type and pushes the boolean result of the test back to the stack.
 *
 */
public final class InstanceOf extends StackProducer {

    /**
     * The object whose type is being tested.
     */
    private StackProducer object;

    /**
     * The type the object is being tested against.
     */
    private final Klass checkType;

    /**
     * Creates a <code>InstanceOf</code> instance representing an instruction
     * that pops an object from the operand stack, tests to see if it is
     * assignable to a specified type and pushes the boolean result of the
     * test back to the stack.
     *
     * @param checkType  the type the object is being tested against
     * @param object     the object whose type is being tested
     */
    public InstanceOf(Klass checkType, StackProducer object) {
        super(Klass.INT);
        this.object = object;
        this.checkType = checkType;
    }

    /**
     * Gets the object whose type is being tested.
     *
     * @return  the object whose type is being tested
     */
    public StackProducer getObject() {
        return object;
    }

    /**
     * Gets the type the object is being tested against.
     *
     * @return the type the object is being tested against
     */
    public Klass getCheckType() {
        return checkType;
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
        return checkType;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doInstanceOf(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        object = visitor.doOperand(this, object);
    }
}
