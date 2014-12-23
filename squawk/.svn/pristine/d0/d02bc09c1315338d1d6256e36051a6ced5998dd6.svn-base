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

import java.io.PrintStream;
import com.sun.squawk.pragma.GlobalStaticFields;


/**
 * The Tracer class encapsulates a set of static methods that are used to
 * emit execution traces to a print stream.<p>
 *
 * It is intended for usage of this class to be statically conditional. That is,
 * it need not exist in a production deployment. As such, all calls/references
 * to this class should be wrapped in a conditional test of the final static
 * boolean variable {@link Klass#TRACING_ENALBED}. For example:<p>
 *
 * <p><hr><blockquote><pre>
 *     if (Klass.TRACING_ENABLED) && Tracer.isTracing("loading", klass.getName()) {
 *         Tracer.traceln("Loading "+klass.getName());
 *     }
 * </pre></blockquote><hr><p>
 *
 * If the value of <code>Klass.TRACING_ENABLED</code> was statically
 * determined to be false by javac, then this whole block of code would be
 * omitted from the compiled class.
 */
public final class Tracer implements GlobalStaticFields {

    /**
     * The features that are to be traced.
     */
    private static SquawkHashtable _features;
    private static SquawkHashtable features() {
        if (_features == null) {
            _features = new SquawkHashtable();
        }
        return _features;
    }

    /**
     * The print stream to which trace output it written. The
     * default is {@link System#out}.
     */
    private static PrintStream _out;
    private static PrintStream out() {
        if (_out == null) {
            _out = System.out;
        }
        return _out;
    }

    /**
     * A string that is used to determine whether or not a current block
     * of trace code should be executed.
     */
    private static String filter;

    /**
     * Sets the print stream to which traces will be written. This must be
     * done before a call to {@link #trace(String)} or {@link #traceln(String)}.
     *
     * @param  out     the print stream to which trace output is written
     */
    public static void setPrintStream(PrintStream out) {
        Assert.that(out != null, "print stream for tracer cannot be null");
        Tracer._out = out;
    }

    /**
     * Gets the print stream to which traces will be written.
     *
     * @return the print stream to which trace output is written
     */
    public static PrintStream getPrintStream() {
        return Tracer.out();
    }

    /**
     * Sets the string used to enable/disable the tracer. If <code>filter</code>
     * is null, then filtering is turned off.
     *
     * @param  filter  the string used to enable/disable the tracer
     */
    public static void setFilter(String filter) {
        Tracer.filter = filter;
    }

    /**
     * Enable a given feature to be traced.
     *
     * @param feature
     */
    public static void enableFeature(String feature) {
        Assert.that(feature != null);
        features().put(feature, feature);
    }

    /**
     * Disable all the enabled features and clears the filter (if any).
     */
    public static void reset() {
        features().clear();
        filter = null;
    }

    /**
     * Determines whether or not a given feature is being traced for a given
     * component.
     *
     * @param  feature    the feature to be traced
     * @param  component  the component to be traced
     * @return true if tracing this feature
     */
    public static boolean isTracing(String feature, String component) {
        return (features().contains(feature) &&
                (filter == null ||
                 component == null ||
                 component.indexOf(filter) != -1));
    }

    /**
     * Determines whether or not a given feature is being traced.
     *
     * @param  feature    the feature to be traced
     * @return true if tracing this feature
     */
    public static boolean isTracing(String feature) {
        return (features().containsKey(feature));
    }

    /**
     * Prevent construction.
     */
    private Tracer() {
    }

    /**
     * Output a string to the tracer's print stream.
     *
     * @param message the message to write
     */
    public static void trace(String message) {
        out().print(message);
    }

    /**
     * Output a string to the tracer's print stream followed by a newline.
     *
     * @param message the message to write
     */
    public static void traceln(String message) {
        out().println(message);
    }
}
