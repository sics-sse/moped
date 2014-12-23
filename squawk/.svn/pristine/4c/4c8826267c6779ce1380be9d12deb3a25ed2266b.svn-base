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

/**
 * Represents a pointer to a 32-bit buffer. Used to pass integers by reference to 
 * native functions.
 * 
 * The native buffer is allocated by the constructor, accessed by getVlaue(), setValue(),
 * and freed by free() (of all things).
 */
public class IntByReference extends ByReference {
    
    /**
     * Construct a native 32-bit buffer and set its initial value to "value".
     * You must call the free() method to deallocate the native buffer.
     * 
     * @param value the initial value of the buffer
     */
    public IntByReference(int value) {
        super(4);
        setValue(value);
    }
    
    public int getValue() {
        return getPointer().getInt(0);
    }
    
    public void setValue(int newValue) {
        getPointer().setInt(0, newValue);
    }
    
}
