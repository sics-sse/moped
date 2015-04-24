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

package com.sun.spot.security.implementation;

import com.sun.spot.security.PrivateKey;

/**
 * Specifies the RSA private key interface. An RSA key is not ready for
 * use until both the modulus and exponent have been set.
 */ 
public final class RSAPrivateKey extends RSAKey implements PrivateKey {
    /**
     * Constructor for RSA public key.
     *
     * @param modulus modulus of key to process
     * @param exponent exponent the key
     */
    public RSAPrivateKey(byte[] modulus, byte[] exponent) {
        super(modulus, 0, modulus.length, exponent, 0, exponent.length);
    }

    /**
     * Constructor for RSA public key.
     *
     * @param modulus modulus of key to process
     * @param modOffset offset of the modulus
     * @param modLen length of modulus in bytes
     * @param exponent exponent the key
     * @param expOffset offset of the exponent
     * @param expLen length of the exponent in bytes
     */
    public RSAPrivateKey(byte[] modulus, int modOffset, int modLen,
                         byte[] exponent, int expOffset, int expLen) {
        super(modulus, modOffset, modLen, exponent, expOffset, expLen);
    }
}
