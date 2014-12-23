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
 * An instance of <code>ConstantLong</code> represents an instruction that
 * pushes a long constant onto the operand stack.
 *
 */
public final class ConstantLong extends Constant {

    /**
     * Creates a <code>ConstantLong</code> instance representing the
     * loading of a constant long value to the operand stack.
     *
     * @param value  the long value (wrapped in a {@link Long} object)
     */
    public ConstantLong(Long value) {
        super(Klass.LONG, ConstantPool.CONSTANT_Long, value);
    }

    /**
     * {@inheritDoc}
     *
     * @return  true if the constant value is 0L
     */
    public boolean isDefaultValue() {
        return ((Long)value).longValue() == 0L;
    }
}
