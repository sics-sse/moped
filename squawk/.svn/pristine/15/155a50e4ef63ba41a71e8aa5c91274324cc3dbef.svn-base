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

import com.sun.spot.security.GeneralSecurityException;
import com.sun.spot.security.KeyException;
import com.sun.spot.security.PrivateKey;


public final class ECPrivateKeyImpl extends ECKeyImpl implements PrivateKey {
    
    int[] keyData;  
    int keyLength;  // actual length of the key (current data)

    public ECPrivateKeyImpl(int curveid) {
        super(curveid, true);
        keyData = ffa.acquireVar();
    }
    
    public void setS(byte[] buffer, int offset, int length) throws GeneralSecurityException {
	initOk = false;
        ffa.from(keyData, buffer, offset, length);
        if ((ffa.is(keyData, 0)) || (ffa.cmp(keyData, curve.getOrder().getP()) >= 0)) {
            throw new GeneralSecurityException("Illegal Value");
        }
        keyLength = (ffa.bitLength(keyData) + 7) >>> 3;
        initOk = true;
    }
    
    public int getS(byte[] buffer, int offset) throws KeyException {
        if (!initOk) {
            throw new KeyException("Key not initialized");            
        }
        ffa.toByteArray(buffer, offset, keyLength, keyData);
        return keyLength;
    }

    public void clearKey() {
	initOk = false;
        for (int i = keyData.length - 1; i >= 0; i--) {
            keyData[i] = 0;
        }
    }
    
    public int[] getKeyData() {
	int[] returnValue = new int[keyData.length];
	System.arraycopy(keyData, 0, returnValue, 0, returnValue.length);
	return returnValue;
    }
    
    public String toString() {
        String val = "ECPrivateKey: (";
        try {
            int curveId = this.getCurve();
            val += "CurveId: ";
            if (curveId == 0) 
                val += "secp160r1";
            else
                val += curveId;
            byte[] sval = new byte[((this.getSize() + 7) >>> 3) + 3];
            int len = this.getS(sval, 0);
            if (len <= sval.length) {
                val += ", S:" + Util.hexEncode(sval, len);                
            }
        } catch (Exception e) {
            
        }
        return val + ")";
    }
}

