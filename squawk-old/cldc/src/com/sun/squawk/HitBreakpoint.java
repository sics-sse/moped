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

package com.sun.squawk;

import com.sun.squawk.util.Assert;

/**
 * A <code>HitBreakpoint</code> instance represents a breakpoint that has been hit
 * in a given thread. This object is used to report the breakpoint event to an
 * attached debugger and then resume execution. This object is also used to
 * report exception events (which are special type of breakpoint).
 * <p>
 * All the frame offsets in this class are relative to the top of the stack
 * (i.e. where the first call on the stack is found). This is because
 * offsets relative to the bottom of the stack will be invalidated should
 * the stack be extended.
 *
 */
public final class HitBreakpoint {

    /**
     * Value denoting that a non-exception breakpoint has been hit.
     */
    public final static int BP_HIT = 1;

    /**
     * Value denoting that breakpoint has been handled, thread has been resumed
     * but instruction at breakpoint has not yet been executed.
     */
    public final static int BP_REPORTED = 2;

    /**
     * Value denoting that an exception an exception was raised/thrown on a thread.
     */
    public final static int EXC_HIT = 3;

    /**
     * Value denoting that an exception is being reported to a debug client.
     */
    public final static int EXC_REPORTING = 4;

    /**
     * Value denoting that an exception has been reported and execution should
     * continue in the corresponding exception handler.
     */
    public final static int EXC_REPORTED = 5;

    /**
     * The thread in which the breakpoint was hit.
     */
    final VMThread thread;

    /**
     * Breakpoint reporting state.
     */
    private int state;

    /**
     * The offset (in bytes) from the top of the stack to the frame that threw the exception
     * or hit the breakpoint being reported.
     */
    final Offset hitOrThrowFO;

    /**
     * The bytecode index of the execution point within the method that threw the exception
     * or hit the breakpoint being reported.
     */
    final Offset hitOrThrowBCI;

    /**
     * The offset (in bytes) from the top of the stack to the frame of the handler that will catch
     * the exception.
     */
    final Offset catchFO;

    /**
     * The bytecode index of the catch handler.
     */
    final Offset catchBCI;

    /**
     * The exception to be reported to debugger, or null.
     */
    final Throwable exception;

    /**
     * Creates a HitBreakpoint for reporting a non-exception breakpoint.
     *
     * @param thread   the thread in which the breakpoint was hit
     * @param hitFO    the offset (in bytes) of the frame in which the breakpoint was hit
     * @param hitBCI    the bytecode index of the instruction at which the breakpoint was hit
     */
    HitBreakpoint(VMThread thread, Offset hitFO, Offset hitBCI) {
        this.thread = thread;
        this.exception = null;
        this.hitOrThrowFO = hitFO;
        this.hitOrThrowBCI = hitBCI;
        this.catchFO = Offset.zero();
        this.catchBCI = Offset.zero();
        setState(BP_HIT);

        Assert.that(!hitFO.isZero());
        Assert.that(getMethod() != null);
    }

    /**
     * Creates a HitBreakpoint for reporting an exception breakpoint.
     *
     * @param thread    the thread in which the breakpoint was hit
     * @param exception the exception thrown/raised
     * @param throwFO   the frame in which the exception was thrown
     * @param throwBCI  the bytecode index of the instruction at which the exception was thrown
     * @param catchFO   the frame that will catch exception
     * @param catchBCI  the bytecode index of the exception handler
     */
    HitBreakpoint(VMThread thread, Offset throwFO, Offset throwBCI, Throwable exception, Offset catchFO, Offset catchBCI) {
        this.thread = thread;
        this.hitOrThrowFO = throwFO;
        this.hitOrThrowBCI = throwBCI;
        this.catchFO = catchFO;
        this.catchBCI = catchBCI;
        this.exception = exception;
        setState(EXC_HIT);

        Assert.that(!throwFO.isZero());
        Assert.that(getMethod() != null);
        Assert.that(!catchFO.isZero());
        Assert.that(getCatchMethod() != null);
    }

    Throwable getException() {
        return exception;
    }

    ExecutionPoint getLocation() {
        return new ExecutionPoint(hitOrThrowFO, hitOrThrowBCI, getMethod());
    }

    ExecutionPoint getCatchLocation() {
        return new ExecutionPoint(catchFO, catchBCI, getCatchMethod());
    }

/*if[DEBUG_CODE_ENABLED]*/
    void dumpState() {
        boolean isException = (state != BP_HIT && state != BP_REPORTED);
        VM.print("   state: ");
        VM.println(state);
        VM.print("   thread: ");
        VM.print(thread.getName());
        VM.println();
        VM.print("   thread.appThreadTop: ");
        VM.printOffset(thread.getAppThreadTop());
        VM.println();
        VM.print("   thread.appThreadTop.mp: ");
        VM.printAddress(VM.getMP(thread.frameOffsetAsPointer(thread.getAppThreadTop())));
        VM.println();
        if (isException) {
            VM.print("   exception: ");
            VM.printAddress(exception);
            VM.println();
            VM.print("   throwFO: ");
            VM.printOffset(hitOrThrowFO);
            VM.println();
            VM.print("   throwMP: ");
            VM.printAddress(getMethod());
            VM.println();
            VM.print("   throwBCI: ");
            VM.printOffset(hitOrThrowBCI);
            VM.println();
            VM.print("   catchFO: ");
            VM.printOffset(catchFO);
            VM.println();
            VM.print("   catchBCI: ");
            VM.printOffset(catchBCI);
            VM.println();
            VM.print("   catchMP: ");
            VM.printAddress(getCatchMethod());
            VM.println();
            VM.print("   isCaught: ");
            VM.print(isCaught());
            VM.println();
        } else {
            VM.print("   hitFO: ");
            VM.printOffset(hitOrThrowFO);
            VM.println();
            VM.print("   hitMP: ");
            VM.printAddress(getMethod());
            VM.println();
            VM.print("   hitBCI: ");
            VM.printOffset(hitOrThrowBCI);
            VM.println();
        }
    }
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * @return the state value
     */
    final int getState() {
        return state;
    }

    /**
     * Sets the debugger state to a given value.
     *
     * @param value  the new value
     */
    final void setState(int value) {
        Assert.that(value >= BP_HIT && value <= EXC_REPORTED);
        state = value;
    }

    /**
     * @return the method that threw the exception or hit the breakpoint being reported
     */
    Object getMethod() {
        return VM.getMP(thread.frameOffsetAsPointer(hitOrThrowFO));
    }

    /**
     * Return the method that will catch the exception.
     *
     * @return method object or null
     */
    Object getCatchMethod() {
        return catchFO.isZero() ? null : VM.getMP(thread.frameOffsetAsPointer(catchFO));
    }

    /**
     * Determines if the thrown exception is caught within an application frame.
     * There is always a top level handler for all exceptions in a system frame.
     *
     * @return if the handler for the exception is within an application
     */
    boolean isCaught() {
        Assert.that(!catchFO.isZero());
        Offset appThreadTop = thread.getAppThreadTop();
        return appThreadTop.isZero() || appThreadTop.lt(catchFO);
    }
}

