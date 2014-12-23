/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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
 * 
 *
 * The FFA class is a simple and fast big-integer implementation suited for
 * public key cryptography.
 *<p><h2>API</h2><p>
 * 
 * <h3>Conventions (for all functions but 'mul' and 'sqr')</h3><br>
 * The functions require that their arguments have the same length.<br>
 * The arguments are given in the following sequence:<br><ul>
 *  <li><code>r</code> result</li>
 *  <li><code>a</code> operand a</li>
 *  <li><code>b</code> operand b</li></ul>
 * The same int-array that is used for one or more of the operands
 * can be used to store the result at the same time.
 * 
 * <h3>Exceptions</h3><ul>
 *  <li>For 'mul' and 'sqr', the result must have twice the size of
 *   the operands. Therefore it is not possible to store the result
 *   to the same array that is used for either of the operands.</li>
 *  <li>'copy' and 'cmp' can handle arrays of different sizes.</li>
 * </ul>
 * <p>
 * In addition, this class provides methods to acquire varibles of
 * a specific size. To reduce creation of new objects, it is possible
 * to release the variables after use, so that they can be recycled
 * (re-acquired).<p>
 *
 *<h2>Implementation details</h2>
 *<p>
 * Big integers are represented by int-arrays in little endian
 * word order, using only 28 bits per word.
 *<p>
 * Example:<br><ul>
 *   <li>a[0] ... bits 27 ~  0</li>
 *   <li>a[1] ... bits 55 ~ 28</li>
 *   <li>etc.</li></ul>
 * Bits 31 ~ 28 in each integer are 0.
 *
 */

public final class FFA {
    
    public static final int BITS_PER_WORD = 28;
    public static final int BMASK = 0x0fffffff;

    //vars[][] saves up to 16 int[len] which where released by releaseVar for future reuse
    private int[][] vars;
    
    private int varsCount;
    
    private int bitLength;
    private int byteLength;
    private int len;
    private int doubleLen;
    
    
    // create a new instance of FFA. variables acquired thru this
    // instance are big enough for the given bit length.
    public FFA(int bitLength) {
        this.bitLength = bitLength;
        len = (bitLength + 27) / 28;
        byteLength = (bitLength + 7) / 8;
        doubleLen = 2 * len;
        vars = new int[16][];
    }

    
    // returns a uninitialized varible of given size.
    public int[] acquireVar(int bits) {
        int len = (bits + 27) / 28;
        if (len == this.len) {
            return acquireVar();
        } else {
            return new int[len];
        }
    }
    
    // returns a uninitialized varible of single size.
    // release it after use.
    synchronized public int[] acquireVar() {
        return (varsCount > 0) ? vars[--varsCount] : new int[len];
    }
    
    // returns a uninitialized varible of double size.
    // release it after use.
    public int[] acquireDoubleVar() {
        return new int[doubleLen];
    }
    
    
    // releases a varible of single size
    synchronized public void releaseVar(int[] var) {
        if ((varsCount < vars.length) && (var.length == len)) {
            vars[varsCount++] = var;
        } // otherwise let the garbage collector do the rest...
    }
        
    
    public int getBitSize() {
        return bitLength;
    }
    
    public int getByteSize() {
        return byteLength;
    }
    
    public int getIntSize() {
        return len;
    }

        
    
    private static char[] hex = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    
    // converts an integer array to a string
    public String toString(int[] a) {
        StringBuffer res = new StringBuffer(a.length * 8);
        for (int i = a.length - 1; i >= 0; i--) {
            int ai = a[i];
            for (int j = 24; j >= 0; j -= 4) {
                res.append(hex[(ai >> j) & 0x0000000f]);
            }
        }
        return res.toString();
    }
    
    // converts a string to an integer array
    public int[] from(int[] r, String a) {
        int strLen = a.length() - 1;
        int intPos = strLen / 7;
        int nibblePos = (strLen % 7) * 4;
        int tmp = 0;
        
        for (int i = r.length - 1; i > intPos; i--) {
            r[i] = 0;
        }
        for (int i = 0; i <= strLen; i++) {
            char ch = a.charAt(i);
            int nibble = 0;
            if ((ch >= '0') && (ch <= '9')) {
                nibble = (int)(ch - '0');
            } else
            if ((ch >= 'a') && (ch <= 'f')) {
                nibble = (int)(ch - 'a') + 10;
            } else
            if ((ch >= 'A') && (ch <= 'F')) {
                nibble = (int)(ch - 'A') + 10;
            }
            tmp = tmp | (nibble << nibblePos);
            if ((nibblePos -= 4) < 0) {
                r[intPos--] = tmp;
                tmp = 0;
                nibblePos = 24;
                if (intPos < 0) {
                    break;
                }
            }
        }
        return r;
    }
    
    // converts a string to an integer array, acquiring the array first.
    public int[] from(String a) {
        return from(acquireVar(), a);
    }
    
    // converts from a big-endian int array to the internal representation
    public int[] from(int[] r, int[] a) {
        int srcPos = a.length;
        int dstBit = 0;
        int dstPos = 0;
        int dstLen = r.length;
        long rNext = 0;
        
        while (--srcPos >= 0) {
            rNext |= (((long)a[srcPos] & 0xffffffffL) << dstBit);
            dstBit += 32;
            while (dstBit >= BITS_PER_WORD) {
                r[dstPos++] = (int)rNext & BMASK;
                if (dstPos >= dstLen) return r;
                rNext >>>= BITS_PER_WORD;
                dstBit -= BITS_PER_WORD;
            }
        }
        
        r[dstPos++] = (int)rNext & BMASK;
        while (dstPos < dstLen) {
            r[dstPos++] = 0;
        }
        
        return r;
    }
    
    // converts from a big-endian int array to the internal representation,
    // acquiring the destination array first
    public int[] from(int[] a) {
        return from(acquireVar(), a);
    }

    // converts from a right aligned big-endian byte array to the internal
    // representation
    public int[] from(int[] r, byte[] a, int ofs, int len) {
        int srcPos = ofs + len;
        int dstBit = 0;
        int dstPos = 0;
        int dstLen = r.length;
        long rNext = 0;
        
        while (--srcPos >= ofs) {
            rNext |= (((long)a[srcPos] & 0xffL) << dstBit);
            dstBit += 8;
            while (dstBit >= BITS_PER_WORD) {
                r[dstPos++] = (int)rNext & BMASK;
                if (dstPos >= dstLen) return r;
                rNext >>>= BITS_PER_WORD;
                dstBit -= BITS_PER_WORD;
            }
        }
        
        r[dstPos++] = (int)rNext & BMASK;
        while (dstPos < dstLen) {
            r[dstPos++] = 0;
        }
        
        return r;
    }
    
    // converts from a right aligned big-endian byte array to the internal
    // representation, acquiring the destination array first
    public int[] from(byte[] a, int ofs, int len) {
        return from(acquireVar(), a, ofs, len);
    }
    
    
    public void toByteArray(byte[] dst, int ofs, int len, int[] a) {
        int srcPos = 0;
        int srcLen = a.length;
        int dstBit = 0;
        int dstPos = ofs + len - 1;
        int dstLen = len;
        long rNext = 0;
        
        if (dstLen <= 0) return;
        
        while (srcPos < srcLen) {
            rNext |= (((long)(a[srcPos++] & BMASK)) << dstBit);
            dstBit += BITS_PER_WORD;
            while (dstBit >= 8) {
                dst[dstPos--] = (byte)rNext;
                if (dstPos < ofs) return;
                rNext >>>= 8;
                dstBit -= 8;
            }
        }
        
        dst[dstPos--] = (byte)rNext;
        while (dstPos >= ofs) {
            dst[dstPos--] = 0;
        }
    }

    
    // r = a + b;
    public int add(int[] r, int[] a, int[] b) {
        int len = a.length;
        int m = 0;
        
        for (int i = 0; i < len; i++) {
            m += (a[i] + b[i]);
            r[i] = m & BMASK; m >>= 28;
        }

        return m;
    }
    
    // r = a - b;
    public int sub(int[] r, int[] a, int[] b) {
        int len = a.length;
        int m = 1;
        
        for (int i = 0; i < len; i++) {
            m += (a[i] + (b[i] ^ BMASK));
            r[i] = m & BMASK; m >>= 28;
        }

        return (m ^ 1); // return borrow (0 or 1).
    }
	
    // r = a * b;
    // 'r' must be at least twice as big as 'a'.
    public void mul(int[] r, int[] a, int[] b) {
        int len = a.length - 1;
        int len2 = len + len + 1;
        int i, j, k;
        long acc = 0;
        /*
        acc += (long)a[0] * (long)b[0];
        r[ 0] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[1] * (long)b[0] + (long)a[0] * (long)b[1];
        r[ 1] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[2] * (long)b[0] + (long)a[1] * (long)b[1] + (long)a[0] * (long)b[2];
        r[ 2] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[3] * (long)b[0] + (long)a[2] * (long)b[1] + (long)a[1] * (long)b[2] + (long)a[0] * (long)b[3];
        r[ 3] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[4] * (long)b[0] + (long)a[3] * (long)b[1] + (long)a[2] * (long)b[2] + (long)a[1] * (long)b[3] + (long)a[0] * (long)b[4];
        r[ 4] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[5] * (long)b[0] + (long)a[4] * (long)b[1] + (long)a[3] * (long)b[2] + (long)a[2] * (long)b[3] + (long)a[1] * (long)b[4] + (long)a[0] * (long)b[5];
        r[ 5] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[5] * (long)b[1] + (long)a[4] * (long)b[2] + (long)a[3] * (long)b[3] + (long)a[2] * (long)b[4] + (long)a[1] * (long)b[5];
        r[ 6] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[5] * (long)b[2] + (long)a[4] * (long)b[3] + (long)a[3] * (long)b[4] + (long)a[2] * (long)b[5];
        r[ 7] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[5] * (long)b[3] + (long)a[4] * (long)b[4] + (long)a[3] * (long)b[5];
        r[ 8] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[5] * (long)b[4] + (long)a[4] * (long)b[5];
        r[ 9] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[5] * (long)b[5];
        r[10] = (int)acc & BMASK; acc >>>= 28;
        r[11] = (int)acc;
        */
        
        for (i = 0; i <= len; i++) {
            k = 0;
            j = i;
            do {
                acc += (long)a[j--] * (long)b[k++];
            } while (j >= 0);
            r[i] = (int)acc & BMASK;
            acc >>>= 28;
        }
        for (; i < len2; i++) {
            j = len;
            k = i - len;
            do {
                acc += (long)a[j--] * (long)b[k++];
            } while (k <= len);
            r[i] = (int)acc & BMASK;
            acc >>>= 28;
        }
        r[i] = (int)acc;
    }
    
    public void sqr(int[] r, int[] a) {
	int len = a.length - 1;
	sqr(r,a,len);
    }
    
    // r = a^2;
    // 'r' must be at least twice as big as 'a'.
    public void sqr(int[] r, int[] a, int len) {
        //int len = a.length - 1;
        int len2 = len + len + 1;
        int i, j, k;
        long acc = 0;
        /*
        acc += (long)a[0] * (long)a[0];
        r[ 0] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[1] * (long)a[0]) << 1);
        r[ 1] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[2] * (long)a[0]) << 1) + (long)a[1] * (long)a[1];
        r[ 2] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[3] * (long)a[0] + (long)a[2] * (long)a[1]) << 1);
        r[ 3] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[4] * (long)a[0] + (long)a[3] * (long)a[1]) << 1) + (long)a[2] * (long)a[2];
        r[ 4] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[5] * (long)a[0] + (long)a[4] * (long)a[1] + (long)a[3] * (long)a[2]) << 1);
        r[ 5] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[5] * (long)a[1] + (long)a[4] * (long)a[2]) << 1) + (long)a[3] * (long)a[3];
        r[ 6] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[5] * (long)a[2] + (long)a[4] * (long)a[3]) << 1);
        r[ 7] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[5] * (long)a[3]) << 1) + (long)a[4] * (long)a[4];
        r[ 8] = (int)acc & BMASK; acc >>>= 28;
        acc += (((long)a[5] * (long)a[4]) << 1);
        r[ 9] = (int)acc & BMASK; acc >>>= 28;
        acc += (long)a[5] * (long)a[5];
        r[10] = (int)acc & BMASK; acc >>>= 28;
        r[11] = (int)acc;
        */
        
        for (i = 0; i <= len; i++) {
            k = 0;
            j = i;
            while (k < j) {
                acc += ((long)a[j--] * (long)a[k++]) << 1;
            }
            if (k == j) {
                acc += (long)a[k] * (long)a[k];
            }
            r[i] = (int)acc & BMASK;
            acc >>>= 28;
        }
        for (; i < len2; i++) {
            j = len;
            k = i - len;
            while (k < j) {
                acc += ((long)a[j--] * (long)a[k++]) << 1;
            }
            if (k == j) {
                acc += (long)a[k] * (long)a[k];
            }
            r[i] = (int)acc & BMASK;
            acc >>>= 28;
        }
        r[i] = (int)acc;
    }
    
    // r = a << n;
    // returns carry out
    public int shl(int[] r, int[] a, int n) {
        int m = 28 - n;
        int len = a.length;
        
        int tmp;
        int res = 0;    // value to shift in (already aligned)
        
        for (int i = 0; i < len; i++) {
            tmp = a[i];
            r[i] = res | ((tmp << n) & BMASK);
            res = tmp >> m;
        }
        
        return res;
    }
    
    // r = a >>> n;
    // returns carry out
    public int shr(int[] r, int[] a, int n) {
        int m = 28 - n;
        int i = a.length;
        
        int tmp;
        int res = 0;    // value to shift in (already aligned)
        
        while (--i >= 0) {
            tmp = a[i];
            r[i] = res | (tmp >> n);
            res = (tmp << m) & BMASK;
        }
        
        return res;
    }
    
    // r = a ^ b;
    public void xor(int[] r, int[] a, int[] b) {
        for (int i = a.length - 1; i >= 0; i--) {
            r[i] = a[i] ^ b[i];
        }
    }
    
    // r = a;
    // can handle operands which differ in size.
    public void copy(int[] r, int[] a) {
        int rlen = r.length;
        int alen = a.length;
        if (rlen != alen) {
            while (rlen > alen) {
                r[--rlen] = 0;
            }
            alen = rlen;
        }
        while (--alen >= 0) {
            r[alen] = a[alen];
        }
    }
    
    public int[] adjustLength(int[] a) {
        if (a.length != len) {
            int[] r = acquireVar();
            copy(r, a);
            return r;
        } else {
            return a;
        }
    }
    
    // a < b: -1
    // a == b: 0
    // a > b:  1
    public int cmp(int[] a, int[] b) {
        
        int aLen = a.length;
        int bLen = b.length;
        
        while (aLen > bLen) {
            if (a[--aLen] != 0) return 1;
        }
        while (bLen > aLen) {
            if (b[--bLen] != 0) return -1;
        }
        
        while (--aLen >= 0) {
            if (a[aLen] < b[aLen]) return -1;
            if (a[aLen] > b[aLen]) return 1;
        }
        
        return 0;
    }
	
    // returns the number of bits needed to represent 'a'.
    // this is the position of the highest bit that is set, plus 1.
    public int bitLength(int[] a) {
        int len = a.length - 1;
        while (a[len] == 0) {
            if (len-- <= 0) return 0;
        }
        int bpos = BITS_PER_WORD - 1;
        int tmp = a[len];
        while (((tmp >>> bpos) & 0x01) == 0) {
            bpos--;
        }
        return (len * BITS_PER_WORD) + bpos + 1;
    }
    
    // checks if the bit on position 'bit' is set in 'a'
    public boolean testBit(int[] a, int bit) {
        return ((a[bit/BITS_PER_WORD] & (0x00000001 << (bit%BITS_PER_WORD))) != 0);
    }
    
    // checks if 'a' is even
    public boolean isEven(int[] a) {
        return ((a[0] & 0x00000001) == 0);
    }
    
    // checks if 'a' is odd
    public boolean isOdd(int[] a) {
        return ((a[0] & 0x00000001) != 0);
    }
    
    // checks if 'a' equals the integer value 'val'
    public boolean is(int[] a, int val) {
        if (a[0] != val) return false;
        for (int i = a.length - 1; i > 0; i--) {
            if (a[i] != 0) return false;
        }
        return true;
    }
    
    // sets 'r' to the integer value 'val'
    public void set(int[] r, int val) {
        r[0] = val;
        for (int i = r.length - 1; i > 0; i--) {
            r[i] = 0;
        }
    }

    
    
    /**
     * Some esoteric stuff follows...
     */
    
    
    // r -= (b << n);
    // length of 'b' can differ from length of 'r'
    private int subShifted(int[] r, int[] b, int n, int len) {
        int m = 1;
        
        int wordOfs = n / 28;
        int bitOfs = n % 28;
        int bLen = b.length;
        long tmp = 0;
        int i;
        
        for (i = wordOfs; (i < (wordOfs + bLen)) & (i < len); i++) {
            tmp |= ((long)b[i - wordOfs]) << bitOfs;
            m += (r[i] + (((int)tmp & BMASK) ^ BMASK));
            r[i] = m & BMASK; m >>= 28;
            tmp >>= 28;
        }
        
        while ((i < len) && ((tmp != 0) || (m != 1))) {
            m += (r[i] + (((int)tmp & BMASK) ^ BMASK));
            r[i++] = m & BMASK; m >>= 28;
            tmp >>= 28;
        }

        return (m ^ 1); // return borrow (0 or 1).
    }
    
    // r += (b << n);
    // length of 'b' can differ from length of 'r'
    private int addShifted(int[] r, int[] b, int n, int len) {
        int m = 0;
        
        int wordOfs = n / 28;
        int bitOfs = n % 28;
        int bLen = b.length;
        long tmp = 0;
        int i;
        
        for (i = wordOfs; (i < (wordOfs + bLen)) & (i < len); i++) {
            tmp |= ((long)b[i - wordOfs]) << bitOfs;
            m += (r[i] + ((int)tmp & BMASK));
            r[i] = m & BMASK; m >>= 28;
            tmp >>= 28;
        }
        
        while ((i < len) && ((tmp != 0) || (m != 0))) {
            m += (r[i] + ((int)tmp & BMASK));
            r[i++] = m & BMASK; m >>= 28;
            tmp >>= 28;
        }

        return m;
    }
    
    // a = a mod b;
    public void mod(int[] a, int[] b) {
        int bBits = bitLength(b);
        int aBits;
        int aLen;
        
        while (cmp(a, b) >= 0) {
            aBits = bitLength(a);
            aLen = (aBits / 28) + 1;
            if (subShifted(a, b, aBits - bBits, aLen) == 1) {
                addShifted(a, b, aBits - bBits - 1, aLen);
            }
        }
    }
    
    
    // a^e (mod m). 
    public void modPow(int[] r, int[] a, int[] e,int[] m) {	 
	
	int tmp[] = acquireDoubleVar();
	r[0]=1;	
	for (int i=1;i<r.length;i++) {
	    r[i]=0;
	}
	
	//Prescale, 
	int msbBit=getBitSize()-1;
	while((msbBit>=0)&&(testBit(e, msbBit)==false)) {
	    msbBit--;	    
	}
	
	for (int i=msbBit;i>=0;i--) {			
		sqr(tmp, r);
		mod(tmp, m);
		copy(r, tmp);
		if (testBit(e,i)) {
		    mul(tmp, r, a);
		    mod(tmp, m);
		    copy(r,tmp);
		}
	}
	releaseVar(tmp);		
    }
    

}


