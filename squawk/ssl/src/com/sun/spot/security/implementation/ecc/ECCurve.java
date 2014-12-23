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
 *  This ECCurve class contains functions that operate on ECPoint's and to do point
 *  multiplication.<p>
 *  It has a factory to create ECCurve instances.
 */


public abstract class ECCurve {
    
    protected final FiniteField field;
    protected final int[] a, b;
    protected final int h;
    protected final ECPoint generator;
    protected final PrimeField order;
    
    protected final FFA ffa;
    
    protected ECCurve(FiniteField field, int[] a, int[] b, int[] genX, int[] genY, PrimeField order, int h) {
        this.field = field;
        this.ffa = field.getFFA();
        this.a = a;
        this.b = b;
        this.order = order;
        this.h = h;
        this.generator = new ECPoint(this, genX, genY);
    }
    
    public FiniteField getField() {
        return field;
    }
    
    public int[] getA() {
        return a;
    }
    
    public int[] getB() {
        return b;
    }
    
    public PrimeField getOrder() {
        return order;
    }
    
    public int[] getN() {
        return order.getP();
    }
    
    public int getH() {
        return h;
    }
    
    public ECPoint getGenerator() {
        return generator;
    }
    
    public boolean decodePoint(ECPoint point, byte[] data, int offset, int length) {
        int numLen = ffa.getByteSize();
        int totalLen = 2 * numLen + 1;
        if ((length != totalLen) || (data[offset] != 0x04)) {
            return false;
        }
        int[] x = ffa.from(point.x, data, offset + 1, numLen);
        int[] y = ffa.from(point.y, data, offset + numLen + 1, numLen);
        ffa.set(point.z, 1);
        return true;
    }
    
    public int encodePoint(ECPoint point, byte[] data, int offset) {
        int numLen = ffa.getByteSize();
        int totalLen = 2 * numLen + 1;
        if ((data.length - offset) < totalLen) {
            return 0;
        }
        if (!ffa.is(point.z, 1)) {
            makeAffine(point);
        }
        data[offset] = 0x04;
        ffa.toByteArray(data, offset + 1, numLen, point.x);
        ffa.toByteArray(data, offset + numLen + 1, numLen, point.y);
        return totalLen;
    }
    
    // The following functions are protected because their implementations
    // might have some restrictions on the point format (i.e. jacobian/affine)
    // while other functions work with affine coordinates.
    protected abstract void add(ECPoint o1, ECPoint o2);
    protected abstract void negate(ECPoint o1);
    protected abstract void twice(ECPoint o1);
    protected abstract void makeAffine(ECPoint o1);
    
    public abstract void multiply(ECPoint p, int[] k);
    public abstract void multiplySum(ECPoint p1, int[] k1, ECPoint p2, int[] k2);
    
    public abstract boolean isOnCurve(ECPoint o1);
    
    public void copy(ECPoint dst, ECPoint src) {
        ffa.copy(dst.x, src.x);
        ffa.copy(dst.y, src.y);
        ffa.copy(dst.z, src.z);
    }
    
    
    /**
     * ECCurve Factory.
     */
     
    public static final int SECP160R1 = 0;
    public static final int SECP192R1 = 1;
    
    public static final int SECP256R1 = 2;
    

    private static final int MAX_CURVES = 3;
    private static ECCurve[] instances = new ECCurve[MAX_CURVES];
    
    synchronized public static ECCurve getInstance(int curveId) {
        if ((curveId < 0) || (curveId >= MAX_CURVES)) {
            throw new IllegalArgumentException("Unknown Elliptic Curve");
        }
        
        if (instances[curveId] != null) {
            return instances[curveId];
        }
        
        FFA ffa;
        ECCurve curve = null;
        PrimeField order;
        
        switch (curveId) {
            case SECP160R1: {
                ffa = new FFA(160);
                FFA orderFFA = new FFA(161);
                order = new PrimeField(orderFFA, orderFFA.from("100000000000000000001F4C8F927AED3CA752257"));
       	        curve = new ECCurveFp(
       	                        new NIST160PrimeField(ffa),
       	                        //new PrimeField(ffa,ffa.from("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7FFFFFFF")),
             	           
       	                        ffa.from( "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7FFFFFFC"),
       	                        ffa.from( "1C97BEFC54BD7A8B65ACF89F81D4D4ADC565FA45"),
       	                        ffa.from( "4A96B5688EF573284664698968C38BB913CBFC82"),
       	                        ffa.from( "23A628553168947D59DCC912042351377AC5FB32"),
                                order, 1
       	                    );
                break;
            }
            case SECP192R1: {
                ffa = new FFA(192);
                FFA orderFFA = new FFA(192);
                order = new PrimeField(orderFFA, orderFFA.from("FFFFFFFFFFFFFFFFFFFFFFFF99DEF836146BC9B1B4D22831"));
       	        curve = new ECCurveFp(
       	                        //new PrimeField(ffa,ffa.from("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFF")),
       	                        new NIST192PrimeField(ffa),
       	                        ffa.from( "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFC"),
       	                        ffa.from( "64210519E59C80E70FA7E9AB72243049FEB8DEECC146B9B1"),
       	                        ffa.from( "188DA80EB03090F67CBF20EB43A18800F4FF0AFD82FF1012"),
       	                        ffa.from( "07192B95FFC8DA78631011ED6B24CDD573F977A11E794811"),
                                order, 1
       	                    );
                break;
            }
            case SECP256R1: {
                ffa = new FFA(256);
                FFA orderFFA = new FFA(256);//?
                
                order = new PrimeField(orderFFA, orderFFA.from("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551"));
       	        curve = new ECCurveFp(
       	                        new NIST256PrimeField(ffa),       	                        			       
       	                        //new PrimeField(ffa,ffa.from("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF")), //P
       	                        ffa.from( "FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC"),
       	                        ffa.from( "5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B"),
       	                        ffa.from( "6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296"),
       	                        ffa.from( "4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5"),
                                order, 1
       	                    );
                break;
            }
        }
        
        instances[curveId] = curve;
        return curve;
    }
     
}
