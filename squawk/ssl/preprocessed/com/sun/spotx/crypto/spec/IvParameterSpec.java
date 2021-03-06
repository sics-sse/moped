/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.spotx.crypto.spec;

import com.sun.spot.security.implementation.Util;
import com.sun.spot.security.spec.AlgorithmParameterSpec;

/**
 * This class specifies an <i>initialization vector</i> (IV).
 * Examples which use IVs are ciphers in feedback mode, e.g., DES in
 * CBC mode and RSA ciphers with OAEP encoding operation.
 */
public class IvParameterSpec implements AlgorithmParameterSpec {

    /** Initial vector. */
    private byte[] IV; // initial vector

    /**
     * Uses the first <code>len</code> bytes in <code>iv</code>,
     * beginning at <code>offset</code> inclusive, as the IV.
     *
     * <p> The bytes that constitute the IV are those between
     * <code>iv[offset]</code> and <code>iv[offset+len-1]</code> inclusive.
     *
     * @param iv the buffer with the IV
     * @param offset the offset in <code>iv</code> where the IV
     * starts
     * @param len the number of IV bytes
     */
    public IvParameterSpec(byte[] iv, int offset, int len) {
        IV = Util.cloneSubarray(iv, offset, len);
    }

    /** 
     * Returns the initialization vector (IV).
     *
     * @return the initialization vector (IV)
     */
    public byte[] getIV() {
        return Util.cloneArray(IV);
    }
}
