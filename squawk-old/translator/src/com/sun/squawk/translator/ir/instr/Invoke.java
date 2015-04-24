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
import com.sun.squawk.util.*;
import com.sun.squawk.*;

/**
 * An instance of <code>Invoke</code> represents an instruction that invokes
 * a method.
 *
 */
public abstract class Invoke extends StackProducer {

    /**
     * The method invoked.
     */
    private final Method method;

    /**
     * The parameters passed to the invocation.
     */
    private final StackProducer[] parameters;

    /**
     * Creates an <code>Invoke</code> representing an instruction that invokes
     * a method.
     *
     * @param  method      the method invoked
     * @param  parameters  the parameters passed to the invocation
     */
    public Invoke(Method method, StackProducer[] parameters) {
        super(method.getReturnType());
        this.method = method;
        this.parameters = parameters;
        Assert.always(!method.isInterpreterInvoked(), "the symbols for " + method + " should have been stripped");
    }

    /**
     * Gets the method invoked by this instruction.
     *
     * @return the method invoked by this instruction
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the parameters passed to the invocation.
     *
     * @return  the parameters passed to the invocation
     */
    public StackProducer[] getParameters() {
        return parameters;
    }

    /**
     * {@inheritDoc}
     */
    public final Object getConstantObject() {
        if (pushesClassOfMethod()) {
            return method.getDefiningClass();
        } else {
            return null;
        }
    }

    /**
     * Determines if this instruction is requires the invoked method's defining class
     * to be pushed to the stack so that the method table is made available.
     *
     * @return boolean
     */
    abstract boolean pushesClassOfMethod();

    /**
     * {@inheritDoc}
     */
    public boolean constrainsStack() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        for (int i = 0; i != parameters.length; ++i) {
            StackProducer parameter = parameters[i];
            parameter = visitor.doOperand(this, parameter);
            parameters[i] = parameter;
        }
    }
}
