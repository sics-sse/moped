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
 * An instance of <code>ArrayStore</code> represents an instruction that
 * pops a value off the operand stack and stores it into an array at a
 * given index.
 *
 */
public final class ArrayStore extends Instruction implements Mutator {

    /**
     * The array component type.
     */
    private Klass componentType;

    /**
     * The array reference.
     */
    private StackProducer array;

    /**
     * The array index.
     */
    private StackProducer index;

    /**
     * The value being stored in the array.
     */
    private StackProducer value;

    /**
     * Creates an <code>ArrayStore</code> instance for an instruction that pops
     * a value off the operand stack and stores it into an array at a given
     * index.
     *
     * @param  componentType  the array component type
     * @param  array          the array to which the value is stored
     * @param  index          the index of the stored value
     * @param  value          the value being stored
     */
    public ArrayStore(Klass componentType, StackProducer array, StackProducer index, StackProducer value) {
        this.componentType = componentType;
        this.array         = array;
        this.index         = index;
        this.value         = value;
    }

    /**
     * Gets the array component type.
     *
     * @return the array component type
     */
    public Klass getComponentType() {
        return componentType;
    }

    /**
     * {@inheritDoc}
     */
    public Klass getMutationType() {
        return getComponentType();
    }

    /**
     * Gets the array reference.
     *
     * @return the array reference
     */
    public StackProducer getArray() {
        return array;
    }

    /**
     * Gets the array index.
     *
     * @return the array index
     */
    public StackProducer getIndex() {
        return index;
    }

    /**
     * Gets the value being stored in the array.
     *
     * @return the value being stored in the array
     */
    public StackProducer getValue() {
        return value;
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
    public boolean constrainsStack() {
        return !value.getType().isPrimitive();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doArrayStore(this);
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        array = visitor.doOperand(this, array);
        index = visitor.doOperand(this, index);
        value = visitor.doOperand(this, value);
    }
}
