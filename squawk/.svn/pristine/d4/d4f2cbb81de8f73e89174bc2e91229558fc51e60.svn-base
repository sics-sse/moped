/*
 * Copyright 1994-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.compiler;

/**
 * Oopmap table.
 *
 */
public class MethodMap {

    /**
     * The number of local slots.
     */
    private int localSlotCount;

    /**
     * The local oopmap.
     */
    private byte[] localOopMap;

    /**
     * The number of parameter slots.
     */
    private int parameterSlotCount;

    /**
     * The parameter oopmap.
     */
    private byte[] parameterOopMap;

    /**
     * The constructor.
     */
    public MethodMap() {
    }

    /**
     * Set the contants of the method map from the code generator.
     */
    public void setup(int localSlotCount, byte[] localOopMap, int parameterSlotCount, byte[] parameterOopMap) {
        this.localSlotCount     = localSlotCount;
        this.localOopMap        = localOopMap;
        this.parameterSlotCount = parameterSlotCount;
        this.parameterOopMap    = parameterOopMap;
    }

    /**
     * Get the count of locals allocaed.
     *
     * @return the number allocated
     */
    public int getLocalSlotCount() {
        return localSlotCount;
    }

    /**
     * Get an oopmap for the local variables.
     *
     * @return the oopmap
     */
    public byte[] getLocalOopMap() {
        return localOopMap;
    }

    /**
     * Get the count of parameters allocaed.
     *
     * @return the number allocated
     */
    public int getParameterSlotCount() {
        return parameterSlotCount;
    }

    /**
     * Get an oopmap for the parameter variables.
     *
     * @return the oopmap
     */
    public byte[] getParameterOopMap() {
        return parameterOopMap;
    }

}
