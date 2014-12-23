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
import com.sun.squawk.translator.ci.UninitializedObjectClass;
import com.sun.squawk.*;

/**
 * An instance of <code>New</code> represents an instruction that creates
 * a new instance of a specified class and pushes it to the operand stack.
 *
 */
public final class New extends StackProducer {

    /**
     * Creates a <code>New</code> instance representing an instruction that
     * creates a new instance of a specified class and pushes it to the
     * operand stack
     *
     * @param type  the class of the new instance
     */
    public New(UninitializedObjectClass type) {
        super(type);
    }

    /**
     * Gets the absolute runtine type for the instruction. In all sane code the
     * 'new' instruction is paired with an invokespecial which causes the type of
     * the 'new' instruction to change from an UninitializedObjectClass to a real
     * runtime type. However there are TCK tests that do not do this and for these
     * cases the declared type from the class file constant pool is returned.
     *
     * @return the runtime type.
     */
    public Klass getRuntimeType() {
        Klass klass = getType();
        if (klass instanceof UninitializedObjectClass) {
            klass = ((UninitializedObjectClass)klass).getInitializedType();
        }
        return klass;
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
        return getRuntimeType();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doNew(this);
    }

}
