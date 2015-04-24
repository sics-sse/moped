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
 * An instance of <code>LoadLocal</code> represents an instruction that loads
 * a value from a specified local variable and pushes it onto the operand stack.
 *
 */
public final class LoadLocal extends StackProducer implements LocalVariable {

    /**
     * The local containing the value that is loaded.
     */
    private final Local local;

    /**
     * Creates an instance of <code>LoadLocal</code> representing an
     * instruction that loads a value from a specified local variable and
     * pushes it onto the operand stack.
     *
     * @param type   the type of the value loaded
     * @param local  the local variable from which the value that is loaded
     */
    public LoadLocal(Klass type, Local local) {
        super(type);
        this.local = local;
    }

    /**
     * {@inheritDoc}
     */
    public Local getLocal() {
        return local;
    }

    /**
     * Returns <code>false</code> to indicate that a load does not write a value
     * to the referenced local variable.
     *
     * @return  false
     */
    public boolean writesValue() {
        return false;
    }

    /**
     * Returns <code>true</code> to indicate that a load reads a value from
     * the referenced local variable.
     *
     * @return true
     */
    public boolean readsValue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doLoadLocal(this);
    }

}
