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

package com.sun.squawk.translator.ir;

import com.sun.squawk.translator.ir.instr.*;

/**
 * This interface provides a visitor pattern mechanism for traversing
 * the instructions in an IR.
 *
 */
public interface InstructionVisitor {

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doArithmeticOp      (ArithmeticOp       instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doArrayLength       (ArrayLength        instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doArrayLoad         (ArrayLoad          instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doArrayStore        (ArrayStore         instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doBranch            (Branch             instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doCheckCast         (CheckCast          instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doConversionOp      (ConversionOp       instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doComparisonOp      (ComparisonOp       instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doTry               (Try                instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doTryEnd            (TryEnd             instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doIf                (If                 instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doIfCompare         (IfCompare          instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doIncDecLocal       (IncDecLocal        instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doInstanceOf        (InstanceOf         instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doFindSlot          (FindSlot           instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doInvokeSlot        (InvokeSlot         instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doInvokeStatic      (InvokeStatic       instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doInvokeSuper       (InvokeSuper        instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doInvokeVirtual     (InvokeVirtual      instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doConstant          (Constant           instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doCatch             (Catch              instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doGetField          (GetField           instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doGetStatic         (GetStatic          instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doLoadLocal         (LoadLocal          instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doLookupSwitch      (LookupSwitch       instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doMonitorEnter      (MonitorEnter       instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doMonitorExit       (MonitorExit        instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doNegationOp        (NegationOp         instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doNewArray          (NewArray           instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doNewDimension      (NewDimension       instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doNew               (New                instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doPhi               (Phi                instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doPop               (Pop                instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doPosition          (Position           instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doReturn            (Return             instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doPutField          (PutField           instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doPutStatic         (PutStatic          instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doStoreLocal        (StoreLocal         instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doStackMerge        (StackMerge         instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doStackOp           (StackOp            instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doTableSwitch       (TableSwitch        instruction);

    /**
     * Visit instruction.
     *
     * @param instruction the instruction
     */
    public void doThrow             (Throw              instruction);
}
