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


import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.Key;
import com.sun.spot.security.NoSuchAlgorithmException;
import com.sun.spot.security.implementation.ecc.ECCurve;
import com.sun.spot.security.implementation.ecc.FFA;

public abstract class ECKeyImpl implements Key {
    /**
     * Named Elliptic Curve over a Prime Field: "<code>secp160r1</code>"
     */
    //public static final int SECP160R1 = 0;
    
    /** Key size in bits, e.g. for RSA, this is modulus size. */
    protected int bitsize; 
    protected int bytesize;
    protected int curveid;
    /** Flag indicating if the key has been initialized. */
    protected boolean initOk; 
    protected ECCurve curve;
    protected FFA ffa;

    protected ECKeyImpl(int curveid, boolean isPrivate) {
        this.curveid = curveid;
        curve = ECCurve.getInstance(curveid);
        if (isPrivate) {
            ffa = curve.getOrder().getFFA();
        } else {
            ffa = curve.getField().getFFA();
        }
        bitsize = ffa.getBitSize();
        bytesize = (bitsize + 7) >>> 3;
    }
    
    public int getSize() {
	return bitsize;
    }
    
    public boolean isInitialized() {
	return initOk;
    }
    
    public int getCurve() {
        return curveid;
    }
    
    public ECCurve getECCurve() {
        return curve;
    }
    
    private static int[] mask = {0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF};
    
    public static void genKeyPair(ECPublicKeyImpl publicKey, ECPrivateKeyImpl privateKey)
            throws InvalidKeyException, NoSuchAlgorithmException {
        
        // both keys must be initialized with the same curve
        if (publicKey.curveid != privateKey.curveid) {
            throw new InvalidKeyException();
        }
        ECCurve curve = privateKey.curve;
        FFA ffa = curve.getOrder().getFFA();
        publicKey.clearKey();
        privateKey.clearKey();
        
        // generate a random number in the range: 0 < x < field.prime
        SecureRandom random = SecureRandom.getInstance(SecureRandom.ALG_SECURE_RANDOM);
        int lastBit = curve.getOrder().getBitSize() - 1;
        byte[] priv = new byte[(lastBit >> 3) + 1];
        
        do {
            random.generateData(priv, 0, priv.length);
            priv[0] &= (byte)mask[lastBit % 8];
            // now 'priv' contains our random number, where bit positions
            // beginning at the bit length of the prime are masked out.
            ffa.from(privateKey.keyData, priv, 0, priv.length);
            // loop until the generated random number is in the desired range.
            // the worst case probability that this loops is 50%
        } while ((ffa.cmp(privateKey.keyData, curve.getN()) >= 0) ||
                 (ffa.is(privateKey.keyData, 0)));
        
        // generate the public key
        curve.copy(publicKey.keyData, curve.getGenerator());
        curve.multiply(publicKey.keyData, privateKey.keyData);
        privateKey.keyLength = (ffa.bitLength(privateKey.keyData) + 7) >>> 3;        
        
        // both keys are initialized by now
        privateKey.initOk = true;
        publicKey.initOk = true;
    }

    
    public String getAlgorithm() {
	 
	return "ECDSA"; 
    }

    public byte[] getEncoded() {	
	return null;
    }

    public String getFormat() {	
	return "RAW";
    }
    
}

