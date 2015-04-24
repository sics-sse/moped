/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk;

import com.sun.squawk.VMThread.*;
import com.sun.squawk.util.*;

/**
 * A Debugger is an object that acts as a conduit between an isolate being debugged
 * and an attached JPDA debugger client.
 *
 */
public abstract class Debugger {

    /**
     * An Event instance encapsulates the details of an event that occurred
     * in the VM that an attached JPDA debugger client may want to be
     * notified about.
     */
    public static class Event {

        public final static int VM_DISCONNECTED = 100;
        public final static int SINGLE_STEP = 1;
        public final static int BREAKPOINT = 2;
        public final static int FRAME_POP = 3;
        public final static int EXCEPTION = 4;
        public final static int USER_DEFINED = 5;
        public final static int THREAD_START = 6;
        public final static int THREAD_END = 7;
        public final static int CLASS_PREPARE = 8;
        public final static int CLASS_UNLOAD = 9;
        public final static int CLASS_LOAD = 10;
        public final static int FIELD_ACCESS = 20;
        public final static int FIELD_MODIFICATION = 21;
        public final static int EXCEPTION_CATCH =	30;
        public final static int METHOD_ENTRY = 40;
        public final static int METHOD_EXIT =	41;
        public final static int VM_INIT =	90;
        public final static int VM_DEATH = 99;
        public final static int VM_START = VM_INIT;
        public final static int THREAD_DEATH = THREAD_END;

        /**
         * The <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jpda/jdwp/jdwp-protocol.html#JDWP_EventKind">JDWP event constant</a>
         * describing the kind of event.
         */
        public final int kind;

        /**
         * The object (if any) to which the event applies.
         */
        public final Object object;
        
       /**
        * The debugger id for the thread associated with this event (set by debugger)
        */
        private Object threadID;
        
       /**
        * The thread associated with this event (set by debugger)
        */
        private Thread thread;

        public Event(int kind, Object object) {
            this.kind = kind;
            this.object = object;
        }

/*if[DEBUG_CODE_ENABLED]*/
        public String toString() {
            return "event[kind=" + kind + ",object=" + object + "]";
        }
/*end[DEBUG_CODE_ENABLED]*/
        
        public final void setThread(Thread thread, Object threadID) {
            Assert.always(thread != null && threadID != null);
            Assert.always(this.thread == null && this.threadID == null);
            this.thread = thread;
            this.threadID = threadID;
        } 
        
        /**
         * @return the threadID associated with this event. (set by debugger).
         */
        public final Object getThreadID() {
            return threadID;
        }
        
        /**
         * @return the thread associated with this event.  (set by debugger)
         */
        public final Thread getThread() {
            return thread;
        }
    }

    /**
     * A <code>LocationEvent</code> includes extra detail about the location (method and instruction offset)
     * at which an event occurred.
     */
    public static abstract class LocationEvent extends Event {
        public final ExecutionPoint location;
        public LocationEvent(int type, Object object, ExecutionPoint location) {
            super(type, object);
            this.location = location;
        }
/*if[DEBUG_CODE_ENABLED]*/
        public String toString() {
            return name() + "[kind=" + kind + ",object=" + object + ",location{" + location + "}]";
        }
/*end[DEBUG_CODE_ENABLED]*/

        abstract String name();
    }

    public static class BreakpointEvent extends LocationEvent {
        public BreakpointEvent(ExecutionPoint location) {
            super(BREAKPOINT, null, location);
        }
        String name() {
            return "breakpoint";
        }
    }

    public static class SingleStepEvent extends LocationEvent {
        public SingleStepEvent(ExecutionPoint location) {
            super(SINGLE_STEP, null, location);
        }
        String name() {
            return "step";
        }
    }

    public static class ExceptionEvent extends LocationEvent {
        public final ExecutionPoint catchLocation;
        public final boolean isCaught;
        public ExceptionEvent(Throwable exception, ExecutionPoint throwLocation, ExecutionPoint catchLocation, boolean isCaught) {
            super(EXCEPTION, exception, throwLocation);
            this.catchLocation = catchLocation;
            this.isCaught = isCaught;
        }
/*if[DEBUG_CODE_ENABLED]*/
        public String toString() {
            return name() + "[kind=" + kind + ",exception=" + object + ",throwLocation{" + location +
                "},catchLocation{" + catchLocation + "},caught=" + isCaught + "]";
        }
/*end[DEBUG_CODE_ENABLED]*/
        String name() {
            return "exception";
        }
    }

    /**
     * Notifies this debugger when an event it may be interested in occurs.
     *
     * @param event  the event being reported to this debug agent
     */
    public abstract void notifyEvent(Event event);

    /*=======================================================================*\
     *                                SingleStep                             *
    \*=======================================================================*/

    /**
     * A <code>SingleStep</code> instance represents the stepping state of a thread
     * that is currently performing a single step.
     */
    public static final class SingleStep {

        /**
         * The thread has been put into single stepping mode and has yet to complete a step.
         */
        public final static int REQUESTED = 1;

        /**
         * A step has been completed and the thread is waiting for the debugger to take some action as a result.
         */
        public final static int HIT = 2;

        /**
         * The previously completed step was to a location in a class that excluded from the
         * relevent StepEvent.
         */
        public final static int DEFERRED = 3;

        /**
         * Indicates single step mode.
         */
        private int state;

        /**
         * The offset (in bytes) from the top of the stack of the frame where the step was started.
         */
        final Offset startFO;

        /**
         * The bytecode index of the instruction where the step was started.
         */
        final Offset startBCI;

        /**
         * The bytecode index of the next instruction after <code>startBCI</code> that starts a new source line.
         * Invariant: <code>dupBCI == -1 || targetBCI &lt; dupBCI</code>.
         */
        final int targetBCI;

        /**
         * The bytecode index of another instruction apart from <code>startBCI</code> that returns to the same line as <code>startBCI</code>.
         * This is usually the loop variable increment in a <code>for</code> loop.
         * Invariant: <code>dupBCI == -1 || targetBCI &lt; dupBCI</code>.
         */
        final int dupBCI;

        /**
         * The bytecode index of the first instruction after <code>dupBCI</code> that is on a new source line.
         */
        final int afterDupBCI;

        /**
         * The offset (in bytes) of the frame to which the thread stepped (may be same as <code>fp</code>).
         */
        final Offset reportedFO;

        /**
         * The bytecode index of the instruction to which the thread stepped.
         */
        final Offset reportedBCI;

        /**
         * As defined by the JDWP protocol
         */
        final int size;

        /**
         * As defined by the JDWP protocol
         */
        final int depth;

        /**
         * Creates a SingleStep in the {@link #REQUESTED} state.
         *
         * @param startFO     the offset (in bytes) from the top of the stack of the frame in which the step started
         * @param startBCI    the bytecode index of the instruction at which the step started
         * @param targetBCI   the bytecode index of the next instruction after <code>startBCI</code> that starts a new source line
         * @param dupBCI      the bytecode index of another instruction apart from <code>startBCI</code> that returns to the same line as <code>startBCI</code>
         * @param afterDupBCI the bytecode index of the first instruction after <code>dupBCI</code> that is on a new source line
         * @param size        the granularity of the step (1 = source line, 0 = minimum possible step)
         * @param depth       the call stack depth of the step (0 = step into, 1 = step over, 2 = step out)
         */
        public SingleStep(Offset startFO, Offset startBCI, int targetBCI, int dupBCI, int afterDupBCI, int size, int depth) {
            this.state = SingleStep.REQUESTED;
            this.startFO = startFO;
            this.startBCI = startBCI;
            this.targetBCI = targetBCI;
            this.dupBCI = dupBCI;
            this.afterDupBCI = afterDupBCI;
            this.reportedFO = Offset.zero();
            this.reportedBCI = Offset.zero();
            this.size = size;
            this.depth = depth;
        }

        /**
         * Changes the state to reflect some progression through the phases of a single step.
         *
         * @param newState  the new state
         */
        void changeState(int newState) {
/*if[DEBUG_CODE_ENABLED]*/
            verifyStateChange(state, newState);
/*end[DEBUG_CODE_ENABLED]*/
            this.state = newState;
        }

        /**
         * Gets the stepping state
         * @return REQUESTED, HIT, or DEFERRED
         */
        public final int getState() {
            return this.state;
        }

/*if[DEBUG_CODE_ENABLED]*/
        /**
         * Asserts that the path of the state machine is followed strictly.
         */
        private static void verifyStateChange(int oldState, int newState) {
            switch(newState) {
                case SingleStep.HIT:
                    Assert.that(oldState == REQUESTED, "step state new value: " + newState + ", old value: " + oldState  + ", expected old: " + REQUESTED);
                    break;
                case SingleStep.DEFERRED:
                    Assert.that(oldState == HIT, "step state new value: " + newState + ", old value: " + oldState + ", expected old: " + HIT);
                    break;
                default:
                    Assert.shouldNotReachHere();
            }
        }

        public String toString() {
            return "step: state=" + state + " startFO=" + startFO.toPrimitive() + " startBCI=" + startBCI.toPrimitive() + " targetBCI=" + targetBCI +
                " dupBCI=" + dupBCI + " afterDupBCI=" + afterDupBCI + " reportedFP=" + reportedFO.toPrimitive() +
                " reportedBCI=" + reportedBCI.toPrimitive() + " size=" + size + " depth=" + depth;
        }
/*end[DEBUG_CODE_ENABLED]*/
    }

}
