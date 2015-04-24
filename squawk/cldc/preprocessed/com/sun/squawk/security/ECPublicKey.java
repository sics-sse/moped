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

package com.sun.squawk.security;

import com.sun.squawk.security.ecc.ECCurveFp;
import com.sun.squawk.security.ecc.ECPoint;
import com.sun.squawk.security.ecc.FFA;

public final class ECPublicKey {
    
    protected ECPoint keyData;
    /** Key size in bits, e.g. for RSA, this is modulus size. */
    protected int bitsize; 
    protected int bytesize;
    /** Flag indicating if the key has been initialized. */
     
    protected ECCurveFp curve;
    protected FFA ffa;
    public boolean initOk;
    
    public ECPublicKey() {
    	    curve = ECCurveFp.getInstance();
           ffa = curve.getField().getFFA();
           bitsize = ffa.getBitSize();
           bytesize = (bitsize + 7) >>> 3;
        keyData = new ECPoint(curve);
    }
    
    /**
     * Sets the point of the curve comprising the public key. The point should
     * be specified as an octet string as per ANSI X9.62. A specific
     * implementation need not support the compressed form, but must support the
     * uncompressed form of the point. The plain text data format is big-endian
     * and right-aligned (the least significant bit is the least significant bit
     * of last byte). Input parameter data is copied into the internal
     * representation.
     *
     * @param buffer the input buffer
     * @param offset the offset into the input buffer at which the point
     *        specification begins
     * @param length the byte length of the point specificiation
     * @throws javacard.security.CryptoException with the following reason code:
     *   <ul>
     *     <li><code>CryptoException.ILLEGAL_VALUE</code> if the input
     *       parameter data format is incorrect, or if the input parameter
     *       data is inconsistent with the elliptic curve.</li>
     *   </ul>
     */
    public void setW(byte[] buffer, int offset, int length) throws CryptoException {
        initOk = false;
        boolean ok = curve.decodePoint(keyData, buffer, offset, length);
        if ((!ok) || (!curve.isOnCurve(keyData))) {
            CryptoException.throwIt(CryptoException.ILLEGAL_VALUE);
        }
        initOk = true;
    }
    /**
     * Returns the point of the curve comprising the public key in plain text
     * form. The point is represented as an octet string in compressed or
     * uncompressed forms as per ANSI X9.62. The data format is big-endian and
     * right-aligned (the least significant bit is the least significant bit of
     * last byte).
     *
     * @param buffer the output buffer
     * @param offset the offset into the output buffer at which the point
     *        specification data is to begin
     * @return the byte length of the point specificiation
     * @throws javacard.security.CryptoException with the following reason code:
     *   <ul>
     *     <li><code>CryptoException.UNINITIALIZED_KEY</code> if the point
     *       of the curve comprising the public key has not been
     *       successfully initialized since the time the initialized state
     *       of the key was set to false.</li>
     *   </ul>
     * @see javacard.security.Key
     */
    public int getW(byte[] buffer, int offset) throws CryptoException {
        if (!initOk) {
            CryptoException.throwIt(CryptoException.UNINITIALIZED_KEY);
        }
        return curve.encodePoint(keyData, buffer, offset);
    }
    
   
    public int getSize() {
    	return bitsize;
        }
    
    public ECPoint getKeyData()
    {
    	return keyData;
    }
    
    public void clearKey() {
        ffa.set(keyData.x, 0);
        ffa.set(keyData.y, 0);
        ffa.set(keyData.z, 0);
	initOk = false;
    }
  
    public ECCurveFp getCurve()
    {
    	return curve;
    }
    
}

