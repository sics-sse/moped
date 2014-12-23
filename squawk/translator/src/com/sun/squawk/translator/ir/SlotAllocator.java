/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle. All Rights Reserved.
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
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */

package com.sun.squawk.translator.ir;

import java.util.*;

import com.sun.squawk.translator.Translator;
import com.sun.squawk.translator.ir.instr.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.*;    // Version without synchronization

/**
 * This is the slot allocator for local variables (but not parameters)
 * in a Squawk activation record.
 *
 * <p>
 * NOTE - Some of the logic here could probably be moved to an earlier
 * stage of control flow graph construction.
 *
 */
public final class SlotAllocator {

    /**
     * Flag to reserve slot zero (the first local variable) in a method to
     * be the method pointer. This is not an option in the Squawk system,
     * it must always be set this way and this is just a formal declaration
     * of this aspect of the Squawk design.
     */
    public final static boolean RESERVE_SLOT_ZERO = true; // Do not change

    /**
     * The method being transformed.
     */
    private final Method method;

    /**
     * The IR of the method being transformed.
     */
    private final IR ir;

    /**
     * List of slots.
     */
    private SquawkVector slots;

    /**
     * The number of reference slots that requiring clearing at start of the method.
     */
    private int clearedSlotCounter = 0;

    /**
     * Creates a SlotAllocator.
     *
     * @param ir       the IR to be transformed
     * @param method   the method encapsulating the IR
     */
    public SlotAllocator(IR ir, Method method) {
        this.ir     = ir;
        this.method = method;
    }

    /**
     * Performs an analysis for a set of non-parameter, reference typed local variables
     * to find those that are uninitialized at some point at which a garbage collection
     * may occur. As a result of the analysis, the local variables found to have this
     * property will return true when isUninitializedAtGC() is invoked on them.
     *
     * @param   localRefs   the set of local variables to analyze
     * @return  the number of slots that need clearing at the start of the method
     */
    private int doSlotClearingAnalysis(SquawkVector localRefs) {
        final boolean trace = Translator.TRACING_ENABLED && Tracer.isTracing("slotclearingdetail", method.toString());
        int cleared = 0;

        if (trace) {
            Tracer.traceln("");
            Tracer.traceln("++++ Slot clearing analysis for " + method + " ++++");
        }

        /*
         * If the method is going to have a CLASS_CLINIT instruction then all the
         * local pointers must be cleared. (An alternate solution would be to
         * clear all the local variables at runtime if the CLASS_CLINIT bytecode
         * is calling into Java code, but the cost of doing it this way appears to
         * be very small.)
         */
        boolean requiresClassClinit = method.requiresClassClinit();

        /*
         * Iterate through all the pointer locals and test if they need clearing.
         */
        for (Enumeration e = localRefs.elements(); e.hasMoreElements(); ) {
            Local local = (Local)e.nextElement();
            Assert.that(!local.isParameter() && local.getType() == Klass.REFERENCE);
/*if[FULL_SLOT_CLEARING_ANALYSIS]*/
            boolean mustClear = false;
            SquawkVector traversed = new SquawkVector();
            if (requiresClassClinit || !traverse(local, ir.getHead(), null, traversed)) {
                mustClear = true;
            } else { // Traverse all the exception handlers
                Enumeration handlers = ir.getExceptionHandlers();
                if (handlers != null) {
                    while (handlers.hasMoreElements()) {
                        IRExceptionHandler handler = (IRExceptionHandler)handlers.nextElement();
                        Target target = handler.getTarget();
                        if (!traverse(local, handler.getCatch().getNext(), target, traversed)) {
                            mustClear = true;
                            break;
                        }
                    }
                }
            }
/*else[FULL_SLOT_CLEARING_ANALYSIS]*/
//          boolean mustClear = true;
/*end[FULL_SLOT_CLEARING_ANALYSIS]*/
            if (trace) {
                Tracer.traceln(" local = " + local+ " mustClear = "+mustClear);
            }
            if (mustClear) {
                if (local.setUninitializedAtGC()) {
                    cleared++;
                }
            }
        }

        if (trace) {
            Tracer.traceln("---- Slot clearing analysis for " + method + " cleared = "+cleared + " not cleared = "+(localRefs.size()-cleared)+" ----");
        }

        return cleared;
    }

/*if[FULL_SLOT_CLEARING_ANALYSIS]*/
    /**
     * Does a depth first traversal corresponding to the execution paths from a given
     * basic block. For a given local, the traversal will return false if there is any
     * path of execution which contains a garbage collection point at which the local
     * is uninitialized.
     *
     * @param  instruction  the first instruction in the basic block
     * @param  basicBlock   the target representing the entry to the basic block. This
     *                      will be null if it is the first basic block in the method.
     * @param  traversed    the set of basic blocks that have already been visited in
     *                      the traversal
     * @return true if all paths on this traversal come across an initialization of local before a GC point
     */
    private boolean traverse(Local local, Instruction instruction, Target basicBlock, SquawkVector traversed) {

        /*
         * Do a quick check to see of this basic block has already been examined.
         */
        if (basicBlock != null) {
            if (traversed.contains(basicBlock)) {
                return true;
            }
            traversed.addElement(basicBlock);
        }

        /*
         * Skip the Phi at head of a basic block (unless it is the start of the method).
         */
        if (instruction instanceof Phi && basicBlock != null) {
            Target target = ((Phi)instruction).getTarget();
            if (target.isBackwardBranchTarget()) {
                return false;
            }
            instruction = instruction.getNext();
        }

        while (true) {
            Assert.that(!(instruction instanceof Catch));

            if (instruction instanceof StoreLocal) {
                if (((StoreLocal)instruction).getLocal().hasSameSlotAs(local)) {
                    return true;
                }
            } else if (instruction instanceof Branch) {
                Target target = ((Branch)instruction).getTarget();
                if (!traverse(local, (Instruction)target.getTargetedInstruction(), target, traversed)) {
                    return false;
                }

                /*
                 * If this is a 'goto' then finish the basic block otherwise the traversal
                 * falls through to the 'not taken' branch of the conditional branch.
                 */
                if (!(instruction instanceof If)) {
                    Assert.that(!(instruction instanceof IfCompare));
                    return true;
                }
            } else if (instruction instanceof Switch) {
                if (instruction instanceof LookupSwitch) {
                    return false;
                }
                Switch sw = (Switch)instruction;
                Target target = sw.getDefaultTarget();
                if (!traverse(local, (Instruction)target.getTargetedInstruction(), target, traversed)) {
                    return false;
                }
                Target[] targets = sw.getTargets();
                for (int i = 0; i < targets.length; i++) {
                    target = targets[i];
                    if (!traverse(local, (Instruction)target.getTargetedInstruction(), target, traversed)) {
                        return false;
                    }
                }
                return true; // A Switch always ends a basic block
            } else if (instruction instanceof Phi) { // fell through into a Phi
                Target target = ((Phi)instruction).getTarget();
                if (target.isBackwardBranchTarget()) {
                    return false;
                }
                Assert.that(basicBlock != target);
                return traverse(local, instruction, target, traversed);
            } else if (instruction.mayCauseGC(method.isStatic())) {
                return false;
            } else if (instruction instanceof Return || instruction instanceof Throw) {
                return true;
            }

            Assert.that(instruction.getNext() != null);
            instruction = instruction.getNext();
        }
    }
/*end[FULL_SLOT_CLEARING_ANALYSIS]*/

    /**
     * Do the allocation of slots for the IR.
     *
     * @return an array of Klasses representing the types of the slots
     */
    public Klass[] transform() {
        slots = new SquawkVector();
        SquawkVector localRefs = new SquawkVector();

        /*
         * Pass 1 - Identify the last load of every local variable and collect all the
         *          used reference variables
         */
        for (Instruction instruction = ir.getHead() ; instruction != null ; instruction = instruction.getNext()) {
            if (instruction instanceof LocalVariable) {
                Local local = ((LocalVariable)instruction).getLocal();
                if (instruction instanceof LoadLocal) {
                    local.setLastLoad(instruction);
                }
                if (!local.isParameter() && local.getType() == Klass.REFERENCE && !localRefs.contains(local)) {
                    localRefs.addElement(local);
                }
            }
        }

        /*
         * Pass 2 - Allocate the slots.
         */
        for (Instruction instruction = ir.getHead() ; instruction != null ; instruction = instruction.getNext()) {
            if (instruction instanceof LoadLocal) {
                ((LoadLocal)instruction).getLocal().setSlotForLoad(this, instruction);
            } else if (instruction instanceof IncDecLocal) {
                ((IncDecLocal)instruction).getLocal().setSlotForIncDec(this);
            } else if (instruction instanceof StoreLocal) {
                ((StoreLocal)instruction).getLocal().setSlotForStore(this);
            }
        }

        /*
         * Pass 3 - Do slot clearing analysis
         */
        clearedSlotCounter = doSlotClearingAnalysis(localRefs);

        /*
         * Assign the indexes to the slots. As slot 0 is reserved for the
         * method pointer, numbering starts at 1. If there are any slots
         * that require clearing upon entry to the method, then they are
         * assigned indexes first.
         */
        int index = 1;
        SquawkVector sortedSlots = null;
        if (clearedSlotCounter > 0) {
            sortedSlots = new SquawkVector(clearedSlotCounter);
            for (Enumeration e = slots.elements(); e.hasMoreElements();) {
                Slot slot = (Slot)e.nextElement();
                if (slot.needsClearing()) {
                    sortedSlots.addElement(slot);
                    index = setSlotIndex(slot, index);
                }
            }
        }
        Assert.that(index == clearedSlotCounter + 1);
        for (Enumeration e = slots.elements(); e.hasMoreElements();) {
            Slot slot = (Slot)e.nextElement();
            if (!slot.needsClearing()) {
                index = setSlotIndex(slot, index);
                if (clearedSlotCounter > 0) {
                    sortedSlots.addElement(slot);
                }
            }
        }
        if (clearedSlotCounter > 0) {
            slots = sortedSlots;
        }

        /*
         * Produce the array of Klasses representing the types of the slots
         */
        Klass[] localTypes = new Klass[index];
        localTypes[0] = Klass.REFERENCE;
        index = 1;
        for (Enumeration e = slots.elements(); e.hasMoreElements();) {
            Slot slot = (Slot)e.nextElement();
            Klass k = slot.getType();
            if (!Klass.SQUAWK_64 && k.isDoubleWord()) {
                if (k.getSystemID() == CID.LONG) {
                    localTypes[index++] = Klass.LONG2;
                } else {
/*if[FLOATS]*/
                    Assert.that(k.getSystemID() == CID.DOUBLE);
                    localTypes[index++] = Klass.DOUBLE2;
/*else[FLOATS]*/
//                  Assert.shouldNotReachHere("floating point types not supported in this configuration");
/*end[FLOATS]*/
                }
            }
            localTypes[index++] = k;
        }
        Assert.that(index == localTypes.length);
        return localTypes;
    }

    /**
     * Sets the index of a slot.
     *
     * @param   slot   the slot whose index is to be assigned
     * @param   index  the index
     * @return  the next available index which will be <code>index+1</code>
     *                 if <code>slot</code> has a single word type otherwise it
     *                 will be <code>index + 2</code>.
     */
    private int setSlotIndex(Slot slot, int index) {
        if (!Klass.SQUAWK_64 && slot.getType().isDoubleWord()) {
            if (MethodHeader.LOCAL_LONG_ORDER_NORMAL) {
                slot.setSquawkIndex(index);
            } else {
                slot.setSquawkIndex(index + 1);
            }
            index += 2;
        } else {
            slot.setSquawkIndex(index++);
        }
        return index;
    }

    /**
     * Gets the number of slots that requiring clearing upon entry the method.
     *
     * @return the number of slots to be cleared
     */
    public int getClearedSlotCount() {
        return clearedSlotCounter;
    }

    /**
     * Allocate a slot.
     *
     * @param type        the type of the slot to allocate
     * @param isForStack  true if the slot is for a stack value
     * @return the slot
     */
    Slot allocate(Klass type, boolean isForStack) {
        Assert.that(Frame.getLocalTypeFor(type) == type);

        /*
         * Look for a free slot if this is for a stack value.
         */
        if (isForStack) {
            for (Enumeration e = slots.elements(); e.hasMoreElements();) {
                Slot slot = (Slot)e.nextElement();
                if (slot.isFree() && slot.getType() == type) {
                    slot.setFree(false);
                    return slot;
                }
            }
        }
        Slot slot = new Slot(type, isForStack);
        slots.addElement(slot);
        return slot;
    }

    /**
     * Free a slot.
     *
     * @param slot the slot
     */
    void free(Slot slot) {
        slot.setFree(true);
    }
}
