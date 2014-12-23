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
 * A <code>FullMethodMetadata</code> instance represents the even less frequently used information
 * about a method body. This includes the information found in the JVM
 * LocalVariableTable class file attributes.
 *
 */
public final class FullMethodMetadata extends MethodMetadata {

    /**
     * The local variable table.
     *
     * @see  #getLocalVariableTable()
     */
    private final ScopedLocalVariable[] lvt;

    /**
     * Creates a new <code>FullMethodMetadata</code> instance.
     *
     * @param offset  the offset of the method in the static/virtual method table.
     * @param lnt      the table mapping instruction addresses to the
     *                 source line numbers that start at the addresses.
     *                 The table is encoded as an int array where the high
     *                 16-bits of each element is an instruction address and
     *                 the low 16-bits is the corresponding source line
     * @param lvt      the table describing the symbolic information for
     *                 the local variables in the method
     */
    FullMethodMetadata(int offset, ScopedLocalVariable[] lvt, int[] lnt) {
        super(offset, lnt);
        this.lvt = lvt;
    }

    /**
     * Gets a table describing the scope, name and type of each local variable
     * in the method.
     *
     * @return the local variable table or null if there is no local variable
     *         information for the method
     */
    public ScopedLocalVariable[] getLocalVariableTable() {
        return lvt;
    }

}
