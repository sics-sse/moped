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

package com.sun.squawk.traces;

/**
 * A <code>ExecutionPoint</code> describes the method and line number context of a sampled point of execution.
 */
class ExecutionPoint {

    /**
     * The method in which the execution point is located.
     */
    public final Symbols.Method method;

    /**
     * The source code line number of the execution point.
     */
    public final int lineNumber;

    /**
     * The bytecode index of the execution point.
     */
    public final int bci;

    /**
     * Creates an object representing a specified execution point.
     *
     * @param method     the method in which the execution point is located
     * @param lineNumber the line number of the execution point
     */
    public ExecutionPoint(Symbols.Method method, int lineNumber, int bci) {
        this.method = method;
        this.lineNumber = lineNumber;
        this.bci = bci;
    }

    /**
     * Determines if a given object is equal to this one.
     *
     * @return true iff <code>obj</code> is an ExecutionPoint instance and its {@link #method} and {@link #lineNumber}
     *         field values are equal to this object
     */
    public boolean equals(Object obj) {
        if (obj instanceof ExecutionPoint) {
            ExecutionPoint ep = (ExecutionPoint)obj;
            return ep.lineNumber == lineNumber && ep.method == method;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return method.getSignature().hashCode() + lineNumber;
    }

    /**
     * {@inheritDoc}
     *
     * @return the source code position of this execution point as 'file':'line number'
     */
    public String toString() {
        return method.getFile() + ":" + lineNumber;
    }
}

