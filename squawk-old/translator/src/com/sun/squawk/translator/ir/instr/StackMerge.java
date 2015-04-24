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
 * An instance of <code>StackMerge</code> represents the merge of one
 * or more instructions that write to the same stack slot but on different
 * branches of control flow that meet at a control flow merge point.
 *
 */
public final class StackMerge extends StackProducer {

    /**
     * Constant denoting that the stack is empty at a merge point. Given that
     * this will be the common case, having a shared constant like this
     * saves memory.
     */
    private static final StackProducer[] NO_PRODUCERS = {};

    /**
     * The instructions that write to the operand stack slot.
     */
    private StackProducer[] producers;

    /**
     * Creates an instance of <code>StackMerge</code> that represents one or
     * more instructions that produce a value on the operand stack of a given
     * type.
     *
     * @param type  the type of the value produced on the operand stack
     */
    public StackMerge(Klass type) {
        super(type);
        producers = NO_PRODUCERS;
    }

    /**
     * Determines whether or not a given stack producer is in a given
     * array of stack producers.
     *
     * @param   producers  the array to search
     * @param   producer   the element to search for
     * @return  true if <code>producer</code> is an element of
     *                     <code>producers</code>
     */
    private static boolean contains(StackProducer[] producers, StackProducer producer) {
        for (int i = 0; i != producers.length; ++i) {
            if (producers[i] == producer) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a stack producer to the set of producers that write to the same
     * operand stack slot represented by this stack merge. This operation
     * also updates the local variable that represents the value in the
     * stack slot such that all the merged producers use exactly the same
     * variable.
     *
     * @param   producer  a stack producer that writes to the operand stack slot
     *                    represented by this stack merge
     */
    public void addProducer(StackProducer producer) {
        Assert.that(producer != this);
        if (producers.length == 0) {
            producers = new StackProducer[] { producer };
            if (producer.isSpilt()) {
                super.spill(producer.getSpillLocal());
            }
            return;
        }

        /*
         * Just return if the new producer is already merged
         */
        if (contains(producers, producer)) {
            return;
        }

        if (producer.isSpilt()) {
            spill(producer.getSpillLocal());
        } else if (isSpilt()) {
            producer.spill(getSpillLocal());
        }

        /*
         * Update the list of producers
         */
        StackProducer[] old = producers;
        producers = new StackProducer[old.length + 1];
        System.arraycopy(old, 0, producers, 1, old.length);
        producers[0] = producer;
    }

    /**
     * {@inheritDoc}<p>
     *
     * This implementation overrides the definition in
     * <code>StackProducer</code> so that the spill local for all the merged
     * producers is updated.
     *
     * @param  local {@inheritDoc}
     */
    public synchronized void spill(Local local) {
        if (!spillRecursionGuard) {
            spillRecursionGuard = true;
            super.spill(local);
            for (int i = 0; i != producers.length; ++i) {
                producers[i].spill(local);
            }
            spillRecursionGuard = false;
        }
    }

    /**
     * Prevents infinite recursion for cyclic graphs containing a cycle with a non-empty stack.
     */
    private boolean spillRecursionGuard;

    /**
     * Determines whether or not the value pushed by this instruction
     * is really on the runtime stack or has been spilt.
     *
     * @return  true if this instruction's value is on the stack
     */
    public boolean isOnStack() {
        if (producers == NO_PRODUCERS) {
            return false; // This is needed for some strange TCK condition.
        }
        return producers[0].isOnStack();
    }

    /*---------------------------------------------------------------------------*\
     *                                  Duping                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * {@inheritDoc}<p>
     *
     * This implementation overrides the definition in
     * <code>StackProducer</code> so that the duped flag for all the merged
     * producers is updated.
     */
    public void setDuped(Frame frame) {
        super.setDuped(frame);
        for (int i = 0; i != producers.length; ++i) {
            producers[i].setDuped(frame);
        }
    }

    /**
     * {@inheritDoc}<p>
     *
     * This implementation overrides the definition in
     * <code>StackProducer</code> so that the duped flag for all the merged
     * producers is updated.
     */
    public void cancelDuping() {
        super.cancelDuping();
        for (int i = 0; i != producers.length; ++i) {
            producers[i].cancelDuping();
        }
    }


    /*---------------------------------------------------------------------------*\
     *                             ProducerVisitor                               *
    \*---------------------------------------------------------------------------*/

    /**
     * This interface is implemented by a class that wants to be used to
     * traverse all the producers represented by a <code>StackMerge</code>
     * instance.
     *
     */
    public static interface ProducerVisitor {

        /**
         * Visits a single producer in a traversal of merged stack producers.
         *
         * @param producer  a stack producer that writes to a stack slot that
         *                  is written to by other stack producers
         * @return true if the traversal should continue, false otherwise
         */
        public boolean visit(StackProducer producer);
    }

    /**
     * Traverses all the merged producers represented by this object.
     *
     * @param visitor  an object whose <code>visit()</code> method will be
     *                 called for every merged producer represented by this
     *                 object
     * @return true if the traversal was not short-circuited (i.e. no visit
     *                 returned false)
     */
    public boolean visitProducers(ProducerVisitor visitor) {
        for (int i = 0; i != producers.length; ++i) {
            StackProducer producer = producers[i];
            if (producer instanceof StackMerge) {
                if (!((StackMerge)producer).visitProducers(visitor)) {
                    return false;
                }
            } else {
                if (!visitor.visit(producer)) {
                    return false;
                }
            }
        }
        return true;
    }
    
      public String toString() {
          String spr = super.toString() ;
          spr = spr + " num producers: "  + producers.length;
          for (int i = 0; i < producers.length; i++) {
              spr = spr + "\n    " + producers[i];
          }
        return spr;
    }

    /*---------------------------------------------------------------------------*\
     *                           InstructionVisitor                              *
    \*---------------------------------------------------------------------------*/

    /**
     * {@inheritDoc}
     */
    public void visit(InstructionVisitor visitor) {
        visitor.doStackMerge(this);
    }
}
