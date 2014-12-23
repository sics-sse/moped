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

import com.sun.spot.security.Key;
import com.sun.spot.security.implementation.Util;
import com.sun.spot.security.spec.KeySpec;

/**
 * This class specifies a secret key in a provider-independent fashion.
 * <p>It can be used to construct a <code>SecretKey</code> from a byte array.
 * <p>This class is only useful for raw secret keys that can be represented
 *  as a byte array and have no key parameters associated with them,
 *   e.g., DES or Triple DES keys.
 */ 
public final class SecretKeySpec implements Key, KeySpec {
    /** Type of key, e.g. DES, RSA etc. */
    private String alg;

    /** Local secret. */
    private byte[] secret = null;

    /**
     * Constructs a secret key from the given byte array, using the first len
     * bytes of key, starting at offset inclusive.
     * </p>
     * The bytes that constitute the secret key are those between key[offset]
     * and key[offset+len-1] inclusive.
     * </p>
     * This constructor does not check if the given bytes indeed specify a
     * secret key of the specified algorithm. For example, if the algorithm
     * is DES, this constructor does not check if key is 8 bytes long, and
     * also does not check for weak or semi-weak keys. In order for those
     * checks to be performed, an algorithm-specific key specification class
     * must be used.
     *
     * @param key the key material of the secret key.
     * @param offset the offset in <code>key</code> where the key material starts.
     * @param len the length of the key material.
     * @param algorithm the name of the secret-key algorithm to be associated with
     * 	 the given key material. See Appendix A in the Java Cryptography Extension 
     * 	Reference Guide for information about standard algorithm names.
     */
    public SecretKeySpec(byte[] key, int offset, int len, String algorithm) {
	alg = algorithm;
        secret = Util.cloneSubarray(key, offset, len);
    }

    /** 
     * Returns the name of the algorithm associated with this secret key.
     *
     * @return the secret key algorithm.
     */
    public String getAlgorithm() { 
        return alg;
    }

    /** 
     * Returns the name of the encoding format for this secret key.
     *
     * @return the string "RAW".
     */
    public String getFormat() {	
        return "RAW"; 
    }

    /** 
     * Returns the key material of this secret key.
     *
     * @return the key material
     */
    public byte[] getEncoded() { 
        return Util.cloneArray(secret);
    }   
}
