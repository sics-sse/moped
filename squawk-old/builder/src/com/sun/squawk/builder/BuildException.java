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

package com.sun.squawk.builder;

/**
 * A <code>BuildException</code> is thrown to indicate that an execution of a builder is to terminate.
 */
public class BuildException extends RuntimeException {

    /**
     * The exit value that of the command that will be passes to {@link System#exit}
     * if this termination halts the builder.
     */
    public final int exitValue;

    /**
     * Creates a new BuildException with a specified detail message, exit value and cause
     *
     * @param msg        the detail message
     * @param cause      the cause of the exception
     */
    public BuildException(String msg, Throwable cause) {
        super(msg, cause);
        this.exitValue = -1;
    }

    /**
     * Creates a new BuildException with a specified detail message and exit value.
     *
     * @param msg        the detail message
     * @param exitValue  the exit value to be passed to {@link System#exit} if necessary
     */
    public BuildException(String msg, int exitValue) {
        super(msg);
        this.exitValue = exitValue;
    }

    /**
     * Creates a new BuildException with a specified detail message.
     *
     * @param msg        the detail message
     */
    public BuildException(String msg) {
        this(msg, -1);
    }
}

