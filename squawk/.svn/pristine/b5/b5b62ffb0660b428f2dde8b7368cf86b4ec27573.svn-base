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
 * An instance of <code>GetField</code> represents an instruction that pops
 * an object from the operand stack and pushes the value of a field of the
 * object to the stack.
 *
 */
public class GetField extends StackProducer implements InstanceFieldAccessor {

    /**
     * The referenced field.
     */
    private final Field field;

    /**
     * The object encapsulating the field's value.
     */
    private StackProducer object;

    /**
     * Creates a <code>GetField</code> instance representing an instruction
     * that pops an object from the operand stack and pushes the value of a
     * field of the object to the stack.
     *
     * @param field   the referenced field
     * @param object  the object encapsulating the field's value
     */
    public GetField(Field field, StackProducer object) {
        super(field.getType());
        this.field = field;
        this.object = object;
    }

    /**
     * {@inheritDoc}
     */
    public Field getField() {
        return field;
    }

    /**
     * {@inheritDoc}
     */
    public StackProducer getObject() {
        return object;
    }

    /**
     * {@inheritDoc}
     */
    public boolean mayCauseGC(boolean isStatic) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doGetField(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        object = visitor.doOperand(this, object);
    }
}
