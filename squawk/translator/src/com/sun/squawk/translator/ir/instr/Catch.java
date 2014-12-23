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

import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.ir.*;
import com.sun.squawk.*;

/**
 * An instance of <code>Catch</code> is a pseudo instruction that
 * represents the position in an IR at which an exception handler starts.
 * <p>
 *
 */
public class Catch extends StackProducer implements TargetedInstruction {

    /**
     * The targeted address represented by this object.
     */
    private final Target target;

    /**
     * Creates a <code>Catch</code> instance representing the entry point
     * to a handler that catches exceptions of a given type.
     * <p>
     * Section 4.9.5 of the JVMS says that exception handlers may not be
     * entered by any other mechanism other than an exception being thrown.
     * However it then goes on to say that the verifier does not have to
     * check for this and so we have TCK tests that contain gotos to
     * handlers that are never actually executed. This poses a problem
     * for the Squawk bytecode verifier when stack items have been spilled
     * prior to code flow into the handler. To solve this IR for Catch is
     * set always to 'constrainsStack()' so that all stack input is always
     * spilled and then the code InstructionEmitter will output a CONST_NULL
     * instruction at the start of the handler and the address of the handler
     * in the exception table is incremented by one byte to avoid this
     * instruction.
     *
     * @param type  the type of the exceptions caught by the exception handler
     */
    public Catch(Klass type, Target target) {
        super(type);
        Assert.that(getType() != null);
        this.target = target;
        target.setTargetedCatchInstruction(this);
    }

    /**
     * Gets the address of the first real instruction that is used for exception
     * flow targets to the handler.
     *
     * @return the address
     */
    public int getExceptionBytecodeOffset() {
        return getBytecodeOffset();
    }

    /**
     * Tests if the handler is also a control flow target.
     *
     * @return true if it is
     */
    public boolean isControlFlowTarget() {
        return target.isBackwardBranchTarget() || target.isForwardBranchTarget();
    }

    /**
     * {@inheritDoc}
     */
    public Target getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public final boolean constrainsStack() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Object getConstantObject() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doCatch(this);
    }
}
