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
 * An instance of <code>InvokeSlot</code> represents an instruction that
 * invokes a method that is specified by a method offset popped from the
 * operand stack. This kind of invoke is used to implement invocation of
 * interface methods by providing support for a minimal reflection capability.
 *
 */
public final class InvokeSlot extends Invoke {

    /**
     * Creates an <code>InvokeSlot</code> representing an instruction
     * that invokes a method that is specified by a method offset popped
     * from the operand stack.
     *
     * @param  method      the method invoked
     * @param  parameters  the parameters passed to the invocation
     */
    public InvokeSlot(Method method, StackProducer[] parameters) {
        super(method, parameters);
    }

    /**
     * {@inheritDoc}
     */
    boolean pushesClassOfMethod() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doInvokeSlot(this);
    }
}
