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

import com.sun.squawk.translator.ci.ConstantPool;
import com.sun.squawk.*;

/**
 * An instance of <code>ConstantObject</code> represents an instruction that
 * pushes an object constant (such as a <code>String</code> constant or
 * <code>Klass</code> reference) onto the operand stack.
 *
 */
public final class ConstantObject extends Constant {

    /**
     * Creates a <code>ConstantObject</code> instance representing the
     * loading of a constant object value to the operand stack.
     *
     * @param type   the type of the value
     * @param value  the object value
     */
    public ConstantObject(Klass type, Object value) {
        super(type, ConstantPool.CONSTANT_Object, value);
    }

    /**
     * {@inheritDoc}
     *
     * @return  true if the constant value is 0L
     */
    public boolean isDefaultValue() {
        return value == null;
    }

    /**
     * {@inheritDoc}
     *
     * @return the object
     */
    public Object getConstantObject() {
        return value;
    }
}
