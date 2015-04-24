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

package com.sun.spot.security.implementation.ecc;


/**
 *  This ECPoint class is the representation of a point on an elliptic curve. 
 *  It asically consists of 3 int arrays for the x, y and z coordinates. 
 */

public class ECPoint {
    
    public int[] x;
    public int[] y;
    public int[] z;
    
    protected final ECCurve curve;
    protected final FFA ffa;
    
    private ECPoint(ECPoint p) {
        this.curve = p.curve;
        this.ffa = curve.getField().getFFA();
        x = ffa.acquireVar();
        y = ffa.acquireVar();
        z = ffa.acquireVar();
        ffa.copy(x, p.x);
        ffa.copy(y, p.y);
        ffa.copy(z, p.z);
    }
    
    public ECPoint(ECCurve curve, int[] x, int[] y) {
        this.curve = curve;
        this.ffa = curve.getField().getFFA();
        this.x = x;
        this.y = y;
        this.z = ffa.acquireVar();
        ffa.set(z, 1);
    }
    
    public ECPoint(ECCurve curve) {
        this.curve = curve;
        this.ffa = curve.getField().getFFA();
        this.x = ffa.acquireVar();
        this.y = ffa.acquireVar();
        this.z = ffa.acquireVar();
    }
    
    public Object clone() {
        return clonePoint();
    }
    
    public ECPoint clonePoint() {
        return new ECPoint(this);
    }
    
    public void release() {
        ffa.releaseVar(x);
        ffa.releaseVar(y);
        ffa.releaseVar(z);
    }
    
}
