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



/**
 * An instance of <code>ExceptionHandler</code> describes a single exception
 * handler in a method. It can be used to represent an exception handler in
 * a JVM or Squawk method.
 *
 */
public final class ExceptionHandler {

    /**
     * The bytecode address at which the exception handler becomes active.
     */
    private final int start;

    /**
     * The bytecode address at which the exception handler becomes deactive.
     */
    private final int end;

    /**
     * The entry bytecode address of the handler.
     */
    private final int handler;

    /**
     * The <code>Throwable</code> subclass caught by this handler.
     *
     */
    private final Klass klass;

    /**
     * Create an exception handler.
     *
     * @param start   the start of code range protected by the handler
     * @param end     the end of code range protected by the handler
     * @param handler the handler's entry point
     * @param klass   the <code>Throwable</code> subclass caught by the handler.
     */
    public ExceptionHandler (int start, int end, int handler, Klass klass) {
        this.start   = start;
        this.end     = end;
        this.handler = handler;
        this.klass   = klass;
    }

    /**
     * Gets the address at which this exception handler becomes active.
     *
     * @return the address at which this exception handler becomes active
     */
    public int getStart() {
        return start;
    }

    /**
     * Gets the address at which this exception handler becomes deactive.
     *
     * @return the address at which this exception handler becomes deactive
     */
    public int getEnd() {
        return end;
    }

    /**
     * Gets the address of the entry to this exception handler.
     *
     * @return the address of the entry to this exception handler
     */
    public int getHandler() {
        return handler;
    }

    /**
     * Gets the subclass of {@link Throwable} caught by this handler.
     *
     * @return  the subclass of <code>Throwable</code> caught by this handler
     */
    public Klass getKlass() {
        return klass;
    }
}
