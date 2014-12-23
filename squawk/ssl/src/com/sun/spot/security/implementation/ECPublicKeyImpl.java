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
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.PublicKey;
import com.sun.spot.security.implementation.ecc.ECPoint;
import com.sun.spot.security.implementation.Util;

public final class ECPublicKeyImpl extends ECKeyImpl implements PublicKey {
    
    protected ECPoint keyData;

    public ECPublicKeyImpl(int curveid) {
        super(curveid, false);
        keyData = new ECPoint(curve);
    }
    
    public void setW(byte[] buffer, int offset, int length) throws GeneralSecurityException {
        initOk = false;
        boolean ok = curve.decodePoint(keyData, buffer, offset, length);
        if ((!ok) || (!curve.isOnCurve(keyData))) {
            throw new GeneralSecurityException("Illegal Value");
        }
        initOk = true;
    }

    public int getW(byte[] buffer, int offset) throws InvalidKeyException {
        if (!initOk) {
            throw new InvalidKeyException("Not initialized");
            
        }
        return curve.encodePoint(keyData, buffer, offset);
    }
     	
    public ECPoint getECPoint() {
	return keyData.clonePoint();
    }
    
    public void clearKey() {
        ffa.set(keyData.x, 0);
        ffa.set(keyData.y, 0);
        ffa.set(keyData.z, 0);
	initOk = false;
    }
    
    
    public String toString() {
        String val = "ECPublicKey: (";
        try {
            int curveId = this.getCurve();
            val += "CurveId: ";
             if (curveId == 0) 
                val += "secp160r1";
            else
                val += curveId;
            byte[] wval = new byte[((this.getSize() + 7) >>> 3) * 2 + 3];
            int len = this.getW(wval, 0);
            if (len <= wval.length) {
                val += ", W:" + Util.hexEncode(wval, len);
            }
        } catch (Exception e) {
            
        }
        return val + ")";
    }
}

