/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.cldc.jna.ptr;

import com.sun.cldc.jna.Pointer;
import com.sun.cldc.jna.PointerType;

/**
 *
 * Superclass of types representing native pointers
 */
public abstract class ByReference extends PointerType {
    /**
     * Allocate backing memory of the correct size
     * @param dataSize size in bytes
     */    
    protected ByReference(int dataSize) {
        super(new Pointer(dataSize));
    }

    /**
     * Deallocate the native buffer.
     * @throws java.lang.IllegalStateException if free has already been called on this.
     */
    public void free() {
        getPointer().release();
    }
}
