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

package com.sun.squawk.security.ecc;



public final class ECCurveFp{
    
    private static ECCurveFp curve;
	protected final boolean aIsMinus3;
    protected final int[] t1;
    protected final int[] t2;
    protected final int[] t3;
    protected final int[] t4;
	protected final PrimeField field;
	protected final int[] a;
	protected final int[] b;
	protected final int h;
	protected final ECPoint generator;
	protected final PrimeField order;
	protected final FFA ffa;
    
    private ECCurveFp() {
        FFA ffa = new FFA(160);
        FFA orderFFA = new FFA(161);
       
	        this.field = new NIST160PrimeField(ffa);
	        this.ffa = field.getFFA();
	        this.a = ffa.from( "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF7FFFFFFC");
	        this.b = ffa.from( "1C97BEFC54BD7A8B65ACF89F81D4D4ADC565FA45");
	        this.order = new PrimeField(orderFFA, orderFFA.from("100000000000000000001F4C8F927AED3CA752257"));
	        this.h = 1;
	        this.generator = new ECPoint(this, ffa.from( "4A96B5688EF573284664698968C38BB913CBFC82"), ffa.from( "23A628553168947D59DCC912042351377AC5FB32"));
    	/*this.field = field;
        this.ffa = field.getFFA();
        this.a = a;
        this.b = b;
        this.order = order;
        this.h = h;
        this.generator = new ECPoint(this, genX, genY);*/
    	//super(field, a, b, genX, genY, order, h);
        
        // check if A == -3
        // in this case we can use a slightly faster point doubling algorithm
        t1 = ffa.acquireVar();
        t2 = ffa.acquireVar();
        t3 = ffa.acquireVar();
        t4 = ffa.acquireVar();
        ffa.set(t1, 3);
        field.negate(t1, t1);
        aIsMinus3 = (ffa.cmp(t1, a) == 0);
    }
    public static synchronized ECCurveFp getInstance() {
    	if (curve==null)
    		curve=new ECCurveFp();
    	return curve;
		
	}
    public void add(ECPoint a, ECPoint b) {
        // Algorithm 3.22
        // D. Hankerson, A. Menezes, S. Vanstone: Guide to Elliptic Curve Cryptography
        int[] x1 = a.x;
        int[] y1 = a.y;
        int[] z1 = a.z;
        int[] x2 = b.x;
        int[] y2 = b.y;
        
        field.square(t1, z1);
        field.multiply(t2, t1, z1);
        field.multiply(t1, t1, x2);
        field.multiply(t2, t2, y2);
        field.subtract(t1, t1, x1);
        field.subtract(t2, t2, y1);
        
        if (ffa.is(t1, 0) && ffa.is(t2, 0)) {
            // A == B, so double A
            twice(a);
            return;
        }

        field.multiply(z1, z1, t1);
        field.square(t3, t1);
        field.multiply(t4, t3, t1);
        field.multiply(t3, t3, x1);
        field.multiply2(t1, t3);
        field.square(x1, t2);
        field.subtract(x1, x1, t1);
        field.subtract(x1, x1, t4);
        field.subtract(t3, t3, x1);
        field.multiply(t3, t3, t2);
        field.multiply(t4, t4, y1);
        field.subtract(y1, t3, t4);
    }
    
    public void negate(ECPoint a) {
        field.negate(a.y, a.y);
    }
    
    public void twice(ECPoint a) {
        // Algorithm 3.21
        // D. Hankerson, A. Menezes, S. Vanstone: Guide to Elliptic Curve Cryptography
        int[] x = a.x;
        int[] y = a.y;
        int[] z = a.z;
        
        field.square(t1, z);
        
        // at this point: T1 = Z^2
        if (aIsMinus3) {
            // calculate: T2 = 3(X - Z^2)(X + Z^2)
            field.subtract(t2, x, t1);
            field.add(t1, x, t1);
            field.multiply(t2, t2, t1);
            field.multiply2(t3, t2);
            field.add(t2, t3, t2);
        } else {
            // calculate: T2 = (3X^2 + aZ^4)
            field.square(t1, t1);
            field.multiply(t1, t1, this.a);
            field.square(t2, x);
            field.multiply2(t3, t2);
            field.add(t2, t3, t2);
            field.add(t2, t2, t1);
        }
        // at this point: T2 = (3X^2 + aZ^4)
        
        field.multiply2(y, y);
        field.multiply(z, y, z);
        field.square(y, y);
        field.multiply(t3, y, x);
        field.square(y, y);
        field.divide2(y, y);
        field.square(x, t2);
        field.multiply2(t1, t3);
        field.subtract(x, x, t1);
        field.subtract(t1, t3, x);
        field.multiply(t1, t1, t2);
        field.subtract(y, t1, y);
    }
    
    public void makeAffine(ECPoint a) {
        field.invert(a.z, a.z);
        field.square(t1, a.z);
        field.multiply(a.x, a.x, t1);
        field.multiply(t1, a.z, t1);
        field.multiply(a.y, a.y, t1);
        ffa.set(a.z, 1);
    }
    
    public void multiply(ECPoint R, int[] k) {
        // 'h' can be 2 bits longer than 'k'
        // therefore use longer int-arrays
        FFA ffa = order.getFFA();
        int[] e = ffa.acquireVar(ffa.getBitSize() + 2);
        int[] h = ffa.acquireVar(ffa.getBitSize() + 2);
        ffa.copy(e, k);
        
        // h = 3*e ^ e;
        ffa.add(h, e, e);
        ffa.add(h, h, e);
        ffa.xor(h, h, e);

        // P = R;
        // N = -R;
        ECPoint P = R.clonePoint();
        ECPoint N = R.clonePoint();
        negate(N);
        
        for (int bit = ffa.bitLength(h) - 2; bit > 0; bit--) {
            
            twice(R);       

            if (ffa.testBit(h, bit)) {
                if (!ffa.testBit(e, bit)) {
                    add(R, P);  // add
                } else {
                    add(R, N);  // subtract
                }
            }
            
        }
        
        P.release();
        N.release();
        ffa.releaseVar(e);
        ffa.releaseVar(h);

        makeAffine(R);
    }

    public void multiplySum(ECPoint R1, int[] k1, ECPoint R2, int[] k2) {
        // Algorithm 3.22 with NAF
        // D. Hankerson, A. Menezes, S. Vanstone: Guide to Elliptic Curve Cryptography
        
        // 'h' can be 2 bits longer than 'k'
        // therefore use longer int-arrays
        FFA ffa = order.getFFA();
        int bitSize = ffa.getBitSize() + 2;
        int[] e1 = ffa.acquireVar(bitSize);
        int[] h1 = ffa.acquireVar(bitSize);
        int[] e2 = ffa.acquireVar(bitSize);
        int[] h2 = ffa.acquireVar(bitSize);
        ffa.copy(e1, k1);
        ffa.copy(e2, k2);
        
        // compute NAFs
        // h = 3*e ^ e;
        ffa.add(h1, e1, e1);
        ffa.add(h1, h1, e1);
        ffa.xor(h1, h1, e1);
        ffa.add(h2, e2, e2);
        ffa.add(h2, h2, e2);
        ffa.xor(h2, h2, e2);

        // pre-calculate points:
        // P[0] = null; P[1] = +P1; P[2] = +P2; P[3] = P1+P2; P[4] = P1-P2;
        ECPoint[] P = {null, R1.clonePoint(), R2, R1.clonePoint(), R1.clonePoint()};
        
        add(P[3], P[2]);
        makeAffine(P[3]);
        negate(P[2]);
        add(P[4], P[2]);
        negate(P[2]);
        makeAffine(P[4]);
        
        // Indices into the P array. nagative values mean that the point in
        // P[abs(Pind)] needs to be subtracted.
        int[][] Pind = {{ 0,  2, -2},   //  0    ,     P2,    -P2
                        { 1,  3,  4},   //  P1   ,  P1+P2,  P1-P2
                        {-1, -4, -3}};  // -P1   , -P1+P2, -P1-P2
        
        // We start with a point at infinity
        boolean isInfinity = true;
        
        for (int bit = bitSize - 1; bit > 0; bit--) {
            
            if (!isInfinity) twice(R1);
            
            int[] Prow = Pind[ffa.testBit(h1, bit) ? (!ffa.testBit(e1, bit) ? 1 : 2) : 0];
            int point  = Prow[ffa.testBit(h2, bit) ? (!ffa.testBit(e2, bit) ? 1 : 2) : 0];
            
            if (point != 0) {
                boolean neg = (point < 0);
                if (neg) point = -point;
                ECPoint myP = P[point];
                if (neg) negate(myP);   // Negate first to subtract
                if (isInfinity) {
                    // If R1 is still at infinity, copy the selected point
                    copy(R1, myP);
                    isInfinity = false;
                } else {
                    // Otherwise add it.
                    add(R1, myP);
                }
                if (neg) negate(myP);   // Undo negation
            }
        }
        
        P[1].release();
        P[3].release();
        P[4].release();
        ffa.releaseVar(e1);
        ffa.releaseVar(h1);
        ffa.releaseVar(e2);
        ffa.releaseVar(h2);

        makeAffine(R1);
    }

    public boolean isOnCurve(ECPoint o1) {
        // Guide to ECC, Algorithm 4.25
        if ((o1.curve != this) || (ffa.is(o1.z, 0))
                || (ffa.cmp(o1.x, field.getP()) >= 0)
                || (ffa.cmp(o1.y, field.getP()) >= 0)
                || (ffa.cmp(o1.z, field.getP()) >= 0)) {
            // point is not associated with this curve at all, or it is at
            // infinity, or any of the coordinates is not in the range [0..p-1]
            return false;
        }
        if (!ffa.is(o1.z, 1)) {
            // convert to affine coordinates, if necessary
            makeAffine(o1);
        }
        
        // check if the point satisfies the EC equation:
        // y^2 == x^3 + a*x + b
        field.square(t1, o1.y);         // t1 = y^2 = left hand side
        field.square(t2, o1.x);
        field.multiply(t2, t2, o1.x);   // t2 = x^3
        field.multiply(t3, a, o1.x);    // t3 = a*x
        field.add(t2, t2, t3);
        field.add(t2, t2, b);           // t2 = right hand side
        if (ffa.cmp(t1, t2) != 0) {
            return false;
        }
        
        // TODO: If (h > 1) we have to perform the point multiplication n*P
        // and check that the result is infinity. Right now we only support
        // curves where h == 1, and the code above is sufficient the ensure
        // that the point is ok.
        return true;
    }

	public PrimeField getField() {
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

	public void copy(ECPoint dst, ECPoint src) {
	    ffa.copy(dst.x, src.x);
	    ffa.copy(dst.y, src.y);
	    ffa.copy(dst.z, src.z);
	}
    
}
