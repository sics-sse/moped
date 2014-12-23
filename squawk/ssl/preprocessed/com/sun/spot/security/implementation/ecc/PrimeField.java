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
 * Implementation of prime field arithmetics. <p>
 */
public class PrimeField extends FiniteField {
    
    private int[] tmp;
    
    public PrimeField(FFA ffa, int[] p) {
        super(ffa, p);
        tmp = ffa.acquireDoubleVar();
    }
    
    // r = a + b (mod p)
    public void add(int[] r, int[] a, int[] b) {
        // have to subtract the prime if we have a carry or the
        // result is greater or equal to the prime.
        if ((ffa.add(r, a, b) != 0) || (ffa.cmp(r, p) >= 0)) {
            ffa.sub(r, r, p);
        }
    }
    
    public void subtract(int[] r, int[] a, int[] b) {
        // if we have a borrow-out, add the prime
        if (ffa.sub(r, a, b) != 0) {
            ffa.add(r, r, p);
        }
    }
    
    synchronized public void multiply(int[] r, int[] a, int[] b) {
        ffa.mul(tmp, a, b);
        reduce(r, tmp);
    }
    
    synchronized public void square(int[] r, int[] a) {
        ffa.sqr(tmp, a);
        reduce(r, tmp);
    }
    
    public void multiply2(int[] r, int[] a) {
        add(r, a, a);
    }
    
    public void divide2(int[] r, int[] a) {
        if (ffa.isOdd(a)) {
            // if 'a' is odd, we have to add the prime to make it
            // even. after that, we can shift it right by 1.
            int xhi = ffa.add(r, a, p);
            ffa.shr(r, r, 1);
            r[a.length - 1] |= (xhi << (FFA.BITS_PER_WORD - 1));
        } else {
            ffa.shr(r, a, 1);
        }
    }
    
    public void negate(int[] r, int[] a) {
        subtract(r, p, a);
    }
    
    public void invert(int[] r, int[] a) {
        // Algorithm 2.22 - Binary algorithm for inversion in Fp
        // D. Hankerson, A. Menezes, S. Vanstone: Guide to Elliptic Curve Cryptography
      	int[] u = ffa.acquireVar();
      	int[] v = ffa.acquireVar();
      	int[] x1 = ffa.acquireVar();
      	int[] x2 = ffa.acquireVar();
    
    	ffa.copy(u, a);
    	ffa.copy(v, p);
    	ffa.set(x1, 1);
    	ffa.set(x2, 0);
    
    	while (!ffa.is(u, 1) && !ffa.is(v, 1)) {
        	
    		while (ffa.isEven(u)) {
    			ffa.shr(u, u, 1);
    			divide2(x1, x1);
    		}
    
    		while (ffa.isEven(v)) {
    			ffa.shr(v, v, 1);
    			divide2(x2, x2);
    		}
    
    		if (ffa.cmp(u, v) >= 0) {
    			ffa.sub(u, u, v);
    			subtract(x1, x1, x2);
    		} else {
    			ffa.sub(v, v, u);
    			subtract(x2, x2, x1);
    		}
    
    	}
    
    	if (ffa.is(u, 1)) {
    		ffa.copy(r, x1);
    	} else {
    		ffa.copy(r, x2);
    	}
    
    	ffa.releaseVar(u);
    	ffa.releaseVar(v);
    	ffa.releaseVar(x1);
    	ffa.releaseVar(x2);
    }

    public void trim(int[] r, int[] a) {
        ffa.mod(a, p);
        if (r != a) {
            ffa.copy(r, a);
        }
    }

    /*
     * Generic code for reduction. Subclasses should override this
     * method to provide faster implementations for special prime fields.
     */
    protected void reduce(int[] r, int[] a) {
        // Note: The content of 'a' gets detroyed...
        trim(r, a);
    }
    
    
    public void pow(int[] r, int[] a, int[] e) {
	
	r[0]=1;
	for (int i=1;i<r.length;i++) {
	    r[i]=0;
	}
	int i;
	for (i=getBitSize()-1;i>=0;i--) {
		square(r, r);
		if (ffa.testBit(e,i)) {
		    multiply(r, a, r);
		}
	}
    }
    
}
