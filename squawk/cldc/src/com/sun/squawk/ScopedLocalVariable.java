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
 * An instance of <code>ScopedLocalVariable</code> encapsulates the symbolic
 * information for a local variable that has a limited scope in a Squawk
 * bytecode method.
 *
 */
public final class ScopedLocalVariable {

    /**
     * The name of the local variable.
     */
    public final String name;

    /**
     * The type of the local variable.
     */
    public final Klass type;

    /**
     * The logical slot index of the local variable.
     */
    public final int slot;

    /**
     * The address at which the scope of the local variable starts.
     */
    public final int start;

    /**
     * The offset from 'start' at which the scope of the local variable ends.
     */
    public final int length;

    /**
     * Creates a <code>ScopedLocalVariable</code> instance representing the
     * symbolic information for a local variable in a Squawk bytecode method.
     *
     * @param  name    the local variable's name
     * @param  type    the local variable's type
     * @param  slot    the local variable's logical slot index
     * @param  start   the address at which the scope of the local variable starts
     * @param  length  the offset from <code>start</code> at which the scope of
     *                 the local variable ends
     */
    public ScopedLocalVariable(String name, Klass type, int slot, int start, int length) {
        this.name   = name;
        this.type   = type;
        this.slot   = slot;
        this.start  = start;
        this.length = length;
    }
}
