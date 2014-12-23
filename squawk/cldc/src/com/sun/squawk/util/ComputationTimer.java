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

package com.sun.squawk.util;

import java.util.Enumeration;
import java.util.Stack;
import java.io.PrintStream;

/**
 * This is a singleton class that provides support for timing computations,
 * including nested computations.<p>
 *
 */
public final class ComputationTimer {

    /**
     * Purely static class should not be instantiated.
     */
    private ComputationTimer() {}
    
    /**
     * A computation to be timed that does not throw a checked exception.
     * The computation is performed by invoking
     * Timer.time on the ComputationTimer.Computation object.
     */
    public static interface Computation {
        /**
         * Performs the computation that will be timed.
         *
         * @return   a context dependent value that may represent the result of
         *           the computation.
         */
        public Object run();
    }

    /**
     * A computation to be timed that throws a checked exception.
     * The computation is performed by invoking
     * Timer.time on the ComputationTimer.Computation object.
     */
    public static interface ComputationException {
        /**
         * Performs the computation that will be timed.
         *
         * @return   a context dependent value that may represent the result of
         *           the computation.
         */
        public Object run() throws Exception;
    }

    /**
     * An instance of <code>Execution</code> encapsulates the state of a
     * computation including its duration, result and any exception thrown.
     */
    static class Execution {
        /**
         * The duration of the computation inclusing any nested compuatutions.
         */
        long nestedTimes;

        /**
         * The result of the computation.
         */
        Object result;

        /**
         * The exception (if any) thrown by the computation.
         */
        Exception exception;
    }

    /**
     * The collected flat times.
     */
    private static final SquawkHashtable flatTimes = new SquawkHashtable();

    /**
     * The collected nested times.
     */
    private static final SquawkHashtable totalTimes = new SquawkHashtable();

    /**
     * A stack to model the nesting of computations.
     */
    private static final Stack executions = new Stack();

    /**
     * Execution a computation.
     *
     * @param   id  the identifier of the computation
     * @param   c   the <code>Computation</code> or
     *              <code>ComputationException</code> instance representing
     *              the computation to be executed
     * @return  the dynamic state of the computation's execution
     */
    private static Execution execute(String id, Object c) {
        long start = System.currentTimeMillis();
        Execution e = new Execution();
        executions.push(e);
        Long currentTotal = (Long)totalTimes.get(id);
        try {
            if (c instanceof Computation) {
                e.result = ((Computation)c).run();
            } else {
                e.result = ((ComputationException)c).run();
            }
        } catch (Exception ex) {
            e.exception = ex;
        } finally {
            executions.pop();
            long time = System.currentTimeMillis() - start;
            if (!executions.isEmpty()) {
                ((Execution)executions.peek()).nestedTimes += time;
            }
            totalTimes.put(id, new Long(time+(currentTotal == null ? 0L : currentTotal.longValue())));

            Long flatTime = (Long)flatTimes.get(id);
            if (flatTime == null) {
                flatTimes.put(id, new Long(time - e.nestedTimes));
            } else {
                flatTimes.put(id, new Long(flatTime.longValue() + (time - e.nestedTimes)));
            }
        }
        return e;
    }

    /**
     * Time a specified computation denoted by a specified identifier. The
     * time taken to perform the computation is added to the accumulative
     * time to perform all computations with the same identifier.
     *
     * @param   id           the identifier for the computation
     * @param   computation  the computation to be performed and timed
     * @return  the result of the computation
     */
    public static Object time(String id, Computation computation) {
        Execution e = execute(id, computation);
        if (e.exception != null) {
            Assert.that(e.exception instanceof RuntimeException);
            throw (RuntimeException)e.exception;
        }
        return e.result;
    }

    /**
     * Time a specified computation denoted by a specified identifier. The
     * time taken to perform the computation is added to the accumulative
     * time to perform all computations with the same identifier.
     *
     * @param   id           the identifier for the computation
     * @param   computation  the computation to be performed and timed
     * @return  the result of the computation.
     */
    public static Object time(String id, ComputationException computation) throws Exception {
        Execution e = execute(id, computation);
        if (e.exception != null) {
            throw e.exception;
        }
        return e.result;
    }

    /**
     * Gets an enumeration over the identifiers of computations for which
     * times were collected.
     *
     * @return  an enumeration over the identifiers of computations for which
     *          times were collected
     */
    public static Enumeration getComputations() {
        return flatTimes.keys();
    }

    /**
     * Gets an enumeration over the collected flat times.
     *
     * @return  an enumeration over the collected flat times
     */
    public static Enumeration getFlatTimes() {
        return flatTimes.elements();
    }

    /**
     * Gets an enumeration over the collected accumulative times.
     *
     * @return  an enumeration over the collected accumulative times
     */
    public static Enumeration getTotalTimes() {
        return totalTimes.elements();
    }

    /**
     * Resets all the data gathered by the timer.
     *
     * @throws IllegalStateException if there is an execution currently being timed
     */
    public static void reset() {
        if (!executions.isEmpty()) {
            throw new IllegalStateException();
        }
        flatTimes.clear();
        totalTimes.clear();
    }

    /**
     * Returns a string representation of the times accumulated by the timer
     * in the form of a set of entries, enclosed in braces and separated
     * by the ASCII characters ", " (comma and space). Each entry is rendered
     * as the computation identifier, a colon sign ':', the total time
     * associated with the computation, a colon sign ':' and the flat time
     * associated with the computation.
     *
     * @return a string representation of the collected times
     */
    public static String timesAsString() {
        StringBuffer buf = new StringBuffer("{ ");
        Enumeration keys = flatTimes.keys();
        Enumeration ftimes = flatTimes.elements();
        Enumeration ttimes = totalTimes.elements();
        while (keys.hasMoreElements()) {
            String id = (String)keys.nextElement();
            Long ftime = (Long)ftimes.nextElement();
            Long ttime = (Long)ttimes.nextElement();
            buf.append(id).append(":").append(ttime.toString()).append(":").append(ftime.toString());
            if (keys.hasMoreElements()) {
                buf.append(", ");
            }
        }
        return buf.append(" }").toString();
    }

    /**
     * Print a summary of the times.
     *
     * @param out PrintStream
     */
    public static void dump(PrintStream out) {
        out.println("Times: flat | total | computation");
        Enumeration keys = flatTimes.keys();
        Enumeration ftimes = flatTimes.elements();
        Enumeration ttimes = totalTimes.elements();
        while (keys.hasMoreElements()) {
            String id = (String)keys.nextElement();
            Long ftime = (Long)ftimes.nextElement();
            Long ttime = (Long)ttimes.nextElement();
            out.println(ftime.toString() + '\t' + ttime + '\t' + id);
        }
    }
}
