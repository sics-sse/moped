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
 * An instance of <code>PutStatic</code> represents an instruction that pops
 * a value from the operand stack and assigns the value to a static field of
 * a class.
 *
 */
public class PutStatic extends Instruction implements FieldAccessor, Mutator {

    /**
     * The referenced field.
     */
    private final Field field;

    /**
     * The value stored to the field.
     */
    private StackProducer value;

    /**
     * Creates a <code>PutStatic</code> instance representing an instruction
     * that pops a value from the operand stack and assigns the value to a
     * static field of a class.
     *
     * @param field   the referenced field
     * @param value   the value stored to the field
     */
    public PutStatic(Field field, StackProducer value) {
        super();
        this.field = field;
        this.value = value;
    }

    /**
     * {@inheritDoc}
     */
    public Field getField() {
        return field;
    }

    /**
     * Gets the value stored to the field.
     *
     * @return the value stored to the field
     */
    public StackProducer getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public Klass getMutationType() {
        return field.getType();
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
        return field.getDefiningClass();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doPutStatic(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        value = visitor.doOperand(this, value);
    }
}
