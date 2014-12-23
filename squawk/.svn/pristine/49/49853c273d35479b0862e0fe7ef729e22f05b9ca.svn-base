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

package com.sun.spotx.crypto.implementation;


import com.sun.spot.security.GeneralSecurityException;
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.PrivateKey;
import com.sun.spot.security.implementation.ECPrivateKeyImpl;
import com.sun.spot.security.implementation.ecc.ECCurve;
import com.sun.spot.security.implementation.ecc.ECPoint;
import com.sun.spot.security.implementation.ecc.FFA;
import com.sun.spotx.crypto.KeyAgreement;


public class ECDHKeyAgreement extends KeyAgreement {
    
    private ECPrivateKeyImpl privKey;
    
    public ECDHKeyAgreement() {
    }
    
    public void init(PrivateKey pKey) throws InvalidKeyException {
	ECPrivateKeyImpl privKey;
	if (pKey instanceof ECPrivateKeyImpl) {
	    privKey=(ECPrivateKeyImpl)pKey;
	} else {
	    throw new InvalidKeyException("Invalid key");
	}
	    
        if (!(privKey instanceof ECPrivateKeyImpl)) {
            throw new InvalidKeyException("Illegal value");
        }
        if (!privKey.isInitialized()) {
            throw new InvalidKeyException("Uninitialized key");
        }
        this.privKey = (ECPrivateKeyImpl)privKey;
    }

    public int generateSecret(byte[] publicData, int publicOffset,
            int publicLength, byte[] secret, int secretOffset)
            throws GeneralSecurityException {

        if (privKey == null) {
            throw new GeneralSecurityException("Not initialized");            
        }
        ECCurve curve = privKey.getECCurve();
        FFA ffa = curve.getField().getFFA();
        int numLen = ffa.getByteSize();
        if ((secret.length - secretOffset) < numLen) {
            return 0;
        }
        ECPoint point = new ECPoint(curve);
        if (!curve.decodePoint(point, publicData, publicOffset, publicLength)) {           
            throw new GeneralSecurityException("Illegal value");
        }
        curve.multiply(point, privKey.getKeyData());
        ffa.toByteArray(secret, secretOffset, numLen, point.x);
        point.release();
        return numLen;
    }
    
    public String getAlgorithm() {
        return KeyAgreement.ALG_EC_SVDP_DH;        
    }
    
}
