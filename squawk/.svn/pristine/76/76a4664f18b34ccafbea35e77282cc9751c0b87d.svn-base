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
 * This NIST160PrimeField class implements efficient reduction for 
 * the prime field with NIST 160-bit reduction modulus. It is used
 * in the SECP160R1 elliptic curve.    
 *
 */
public final class NIST160PrimeField extends PrimeField {
    
    private static final int BMASK = 0x0fffffff;
    
    private static final int[] p_const =
        {0xFFFFFFFF,0xFFFFFFFF,0xFFFFFFFF,0xFFFFFFFF,0x7FFFFFFF};
    
    
    public NIST160PrimeField(FFA ffa) {
        super(ffa, ffa.from(p_const));
    }
    
    /**
     * Note that this function is FFA implementation specific and expects
     * that only the 28 LS-bits of each integer are used.
     *  p = 2^160-2^31-1
     */
    protected void reduce(int[] r, int[] a) {
        int m;
        int h0, h1, h2, h3, h4, h5;
        
        // align hi
        h5 = ((a[11] << 8) & BMASK) | (a[10] >> 20);
        h4 = ((a[10] << 8) & BMASK) | (a[9]  >> 20);
        h3 = ((a[9]  << 8) & BMASK) | (a[8]  >> 20);
        h2 = ((a[8]  << 8) & BMASK) | (a[7]  >> 20);
        h1 = ((a[7]  << 8) & BMASK) | (a[6]  >> 20);
        h0 = ((a[6]  << 8) & BMASK) | (a[5]  >> 20);
        // hi is in h5..h0
        // lo is in a[5..0]
        
        // lo = lo + hi + (hi << 31);
        m  = a[0] + h0;             r[0] = m & BMASK; m >>>= 28;
        m += a[1] + h1 + (h0 << 3); r[1] = m & BMASK; m >>>= 28;
        m += a[2] + h2 + (h1 << 3); r[2] = m & BMASK; m >>>= 28;
        m += a[3] + h3 + (h2 << 3); r[3] = m & BMASK; m >>>= 28;
        m += a[4] + h4 + (h3 << 3); r[4] = m & BMASK; m >>>= 28;
        m += (a[5] & 0x000fffff) + h5 + (h4 << 3); r[5] = m & 0x000fffff; m >>>= 20;
        m += (h5 << 11); h0 = m & BMASK; h1 = m >>> 28;
        
        // finished the first iteration. here the intermediate result
        // is in h1:h0 and r[5..0]

        // lo = lo + hi + (hi << 31);
        m  = r[0] + h0;             r[0] = m & BMASK; m >>>= 28;
        m += r[1] + h1 + (h0 << 3); r[1] = m & BMASK; m >>>= 28;
        m += r[2] +      (h1 << 3); r[2] = m & BMASK; m >>>= 28;
        if (m > 0) {
            m += r[3]; r[3] = m & BMASK; m >>= 28;
            m += r[4]; r[4] = m & BMASK; m >>= 28;
            r[5] += m;
        }
        
        // if r >= prime then r -= prime
        if ((r[5] >= 0x000fffff)
         && (r[4] >= 0x0fffffff)
         && (r[3] >= 0x0fffffff)
         && (r[2] >= 0x0fffffff)
         && (r[1] >= 0x0ffffff7)
         && (r[0] >= 0x0fffffff)) {
             
            m  = r[0] + 0x00000001; r[0] = m & BMASK; m >>= 28;
            m += r[1] + 0x00000008; r[1] = m & BMASK; m >>= 28;
            if (m > 0) {
                m += r[2]; r[2] = m & BMASK; m >>= 28;
                m += r[3]; r[3] = m & BMASK; m >>= 28;
                m += r[4]; r[4] = m & BMASK; m >>= 28;
                m += r[5]; r[5] = m & 0x000fffff;
            }
        }
    }
    
}
