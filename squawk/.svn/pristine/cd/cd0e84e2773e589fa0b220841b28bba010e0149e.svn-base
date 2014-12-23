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

import com.sun.squawk.util.*;



/**
 * A byte buffer dencoder can be used to decode a byte array of values encoded
 * with a {@link ByteBufferEncoder byte buffer encoder}.
 *
 */
public final class VMBufferDecoder extends GeneralDecoder {

    /**
     * The VM address of the object.
     */
    private Object oop;

    /**
     * The offset of the next byte.
     */
    private int offset;

    /**
     * Create a VMBufferDecoder.
     */
    VMBufferDecoder() {
    }

    /**
     * Create a VMBufferDecoder to decode from an absolute byte buffer.
     *
     * @param oop    the VM address of object
     * @param offset the offset of the first byte
     */
    VMBufferDecoder(Object oop, int offset) {
        this.oop = oop;
        this.offset = offset;
    }

    /**
     * Get the next byte
     *
     * @return the next byte
     */
    int nextByte() {
        int b = NativeUnsafe.getByte(oop, offset);
        offset += 1;
        return b;
    }

    /**
     * Check that the offset is s certain value
     *
     * @param offset the value to be checked.
     */
    void checkOffset(int offset) {
        Assert.that(this.offset == offset);
    }

    /**
     * Get the current offset.
     *
     * @return the offset
     */
    int getOffset() {
        return offset;
    }

    /**
     * Reset the VMBufferDecoder to decode from a new byte buffer.
     *
     * @param oop    the VM address of object
     * @param offset the offset of the first byte
     */
    void reset(Object oop, int offset) {
        this.oop = oop;
        this.offset = offset;
    }
}
