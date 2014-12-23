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

/**
 * This is the base class for the IR instructions representing the Java bytecode
 * <i>lookupswitch</i> and <i>tableswitch</i> instructions.
 *
 */
public abstract class Switch extends Instruction {

    /**
     * The value being switched upon.
     */
    private StackProducer key;

    /**
     * The targets corresponding to each non-default case.
     */
    private final Target[] targets;

    /**
     * The target for the default case.
     */
    private final Target defaultTarget;

    /**
     * The padding calculated for the instruction during relocation.
     */
    private int padding;

    /**
     * Creates a <code>Switch</code> instance.
     *
     * @param  key           the value being switched upon
     * @param  targetsCount  the number of non-default cases
     * @param  defaultTarget the target for the default case
     */
    public Switch(StackProducer key, int targetsCount, Target defaultTarget) {
        this.key = key;
        this.targets = new Target[targetsCount];
        this.defaultTarget = defaultTarget;
    }

    /**
     * Adds a target corresponding to a case of the switch.
     *
     * @param  index   the index in the targets table
     * @param  target  the target
     */
    protected void addTarget(int index, Target target) {
        targets[index] = target;
    }

    /**
     * Gets the value being switched upon.
     *
     * @return the value being switched upon
     */
    public StackProducer getKey() {
        return key;
    }

    /**
     * Gets the targets corresponding to each non-default case.
     *
     * @return the targets corresponding to each non-default case
     */
    public Target[] getTargets() {
        return targets;
    }

    /**
     * Gets the target for the default case.
     *
     * @return the target for the default case
     */
    public Target getDefaultTarget() {
        return defaultTarget;
    }

    /**
     * Sets the padding calculated for this switch during relocation.
     *
     * @param  padding  the padding calculated for this switch
     */
    public void setPadding(int padding) {
        this.padding = padding;
    }

    /**
     * Gets the padding calculated for this switch during relocation.
     *
     * @return  the padding calculated for this switch
     */
    public int getPadding() {
        return padding;
    }

    /**
     * {@inheritDoc}
     */
    public boolean constrainsStack() {
        if (defaultTarget.isBackwardBranchTarget()) {
            return true;
        }
        for (int i = 0; i != targets.length; ++i) {
            if (targets[i].isBackwardBranchTarget()) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void visit(OperandVisitor visitor) {
        key = visitor.doOperand(this, key);
    }
}
