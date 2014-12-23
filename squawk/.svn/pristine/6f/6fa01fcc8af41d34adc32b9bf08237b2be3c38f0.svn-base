/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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
package com.sun.cldchi.jvm;

import com.sun.squawk.VM;

public class JVM {
    
    /** do not instantiate */
    private JVM() {}

    /**
     * Copy an array from the specified source array, beginning at the
     * specified position, to the specified position of the destination array.
     * <p>
     * Impose the following restrictions on the input arguments:
     * <ul>
     * <li><code>dst</code> is not <code>null</code>.
     * <li><code>src</code> is not <code>null</code>.
     * <li>The <code>srcOffset</code> argument is not negative.
     * <li>The <code>dstOffset</code> argument is not negative.
     * <li>The <code>length</code> argument is not negative.
     * <li><code>srcOffset+length</code> is not greater than
     *     <code>src.length</code>, the length of the source array.
     * <li><code>dstOffset+length</code> is not greater than
     *     <code>dst.length</code>, the length of the destination array.
     * <li>any actual component of the source array from position 
     *     <code>srcOffset</code> through <code>srcOffset+length-1</code> 
     *     can be converted to the component type of the destination array
     * </ul>
     * <p>
     * The caller is responsible that these restrictions are not violated.
     * If any of the restrictions above is violated, the behavior is undefined.
     *
     * @param      src          the source array.
     * @param      srcOffset    start position in the source array.
     * @param      dst          the destination array.
     * @param      dstOffset    start position in the destination data.
     * @param      length       the number of array elements to be copied.
     */
    public static void unchecked_byte_arraycopy(byte[] src,
            int srcOffset,
            byte[] dst,
            int dstOffset,
            int length) {
        VM.arraycopyPrimitive0(src, srcOffset, dst, dstOffset, length, 1);
    }

    public static void unchecked_char_arraycopy(char[] src,
            int srcOffset,
            char[] dst,
            int dstOffset,
            int length) {
        VM.arraycopyPrimitive0(src, srcOffset, dst, dstOffset, length, 2);
    }

    public static void unchecked_int_arraycopy(int[] src,
            int srcOffset,
            int[] dst,
            int dstOffset,
            int length) {
        VM.arraycopyPrimitive0(src, srcOffset, dst, dstOffset, length, 4);
    }

    public static void unchecked_long_arraycopy(long[] src,
            int srcOffset,
            long[] dst,
            int dstOffset,
            int length) {
        VM.arraycopyPrimitive0(src, srcOffset, dst, dstOffset, length, 8);
    }

    public static void unchecked_obj_arraycopy(Object[] src,
            int srcOffset,
            Object[] dst,
            int dstOffset,
            int length) {
        VM.arraycopyObject0(src, srcOffset, dst, dstOffset, length);
    }
}
