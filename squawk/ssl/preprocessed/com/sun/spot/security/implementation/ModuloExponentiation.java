/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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

/**
 * 
 * This class allows modulo exponentiation for odd moduli as used in the
 * RSA algorithm.
 * It is a stripped down version of the BigInteger class of the CDC 
 * configuration, which was extended with the MutableBigInteger class.
 */

public final class ModuloExponentiation {
/**
 * This is a stripped down version of the original MutableBigInteger class
 * A class used to represent multiprecision integers that makes efficient use of
 * allocated space by allowing a number to occupy only part of an array so that
 * the arrays do not have to be reallocated as often. When performing an
 * operation with many iterations the array used to hold a number is only
 * reallocated when necessary and does not have to be the same size as the
 * number it represents. A mutable number allows calculations to occur on the
 * same number without having to create a new number for every step of the
 * calculation as occurs with BigIntegers.
 * 
 * @see BigInteger
 * @version 1.7, 02/02/00
 * @author Michael McCloskey
 * @since 1.3
 */
    private static class MutableBigInteger {

    /**
         * Holds the magnitude of this MutableBigInteger in big endian order.
         * The magnitude may start at an offset into the value array, and it may
         * end before the length of the value array.
         */
    private int[] value;

    /**
         * The number of ints of the value array that are currently used to hold
         * the magnitude of this MutableBigInteger. The magnitude starts at an
         * offset and offset + intLen may be less than value.length.
         */
    private int intLen;

    /**
         * The offset into the value array where the magnitude of this
         * MutableBigInteger begins.
         */
    private int offset = 0;

    /**
         * This mask is used to obtain the value of an int as if it were
         * unsigned.
         */
    private final static long LONG_MASK = 0xffffffffL;

    // Constructors

    /**
         * The default constructor. An empty MutableBigInteger is created with a
         * one word capacity.
         */
    private MutableBigInteger() {
	value = new int[1];
	intLen = 0;
    }
    /**
         * Construct a new MutableBigInteger with the specified value array up
         * to the length of the array supplied.
         */
    private MutableBigInteger(int[] val) {
	value = val;
	intLen = val.length;
    }

    /**
         * Clear out a MutableBigInteger for reuse.
         */
    private void clear() {
	offset = intLen = 0;
	for (int index = 0, n = value.length; index < n; index++)
	    value[index] = 0;
    }

    /**
         * Compare the magnitude of two MutableBigIntegers. Returns -1, 0 or 1
         * as this MutableBigInteger is numerically less than, equal to, or
         * greater than <tt>b</tt>.
         */
    private final int compare(MutableBigInteger b) {
	if (intLen < b.intLen)
	    return -1;
	if (intLen > b.intLen)
	    return 1;

	for (int i = 0; i < intLen; i++) {
	    int b1 = value[offset + i] + 0x80000000;
	    int b2 = b.value[b.offset + i] + 0x80000000;
	    if (b1 < b2)
		return -1;
	    if (b1 > b2)
		return 1;
	}
	return 0;
    }

    /**
         * Ensure that the MutableBigInteger is in normal form, specifically
         * making sure that there are no leading zeros, and that if the
         * magnitude is zero, then intLen is zero.
         */
    private final void normalize() {
	if (intLen == 0) {
	    offset = 0;
	    return;
	}

	int index = offset;
	if (value[index] != 0)
	    return;

	int indexBound = index + intLen;
	do {
	    index++;
	} while (index < indexBound && value[index] == 0);

	int numZeros = index - offset;
	intLen -= numZeros;
	offset = (intLen == 0 ? 0 : offset + numZeros);
    }

    /**
         * Convert this MutableBigInteger into an int array with no leading
         * zeros, of a length that is equal to this MutableBigInteger's intLen.
         */
    private int[] toIntArray() {
	int[] result = new int[intLen];
	for (int i = 0; i < intLen; i++)
	    result[i] = value[offset + i];
	return result;
    }

    /**
         * Sets this MutableBigInteger's value array to the specified array. The
         * intLen is set to the specified length.
         */
    private void setValue(int[] val, int length) {
	value = val;
	intLen = length;
	offset = 0;
    }

    /**
         * Sets this MutableBigInteger's value array to a copy of the specified
         * array. The intLen is set to the length of the new array.
         */
    private void copyValue(MutableBigInteger val) {
	int len = val.intLen;
	if (value.length < len)
	    value = new int[len];

	for (int i = 0; i < len; i++)
	    value[i] = val.value[val.offset + i];
	intLen = len;
	offset = 0;
    }

     
 
    /**
         * Right shift this MutableBigInteger n bits. The MutableBigInteger is
         * left in normal form.
         */
    private void rightShift(int n) {
	if (intLen == 0)
	    return;
	int nInts = n >>> 5;
	int nBits = n & 0x1F;
	this.intLen -= nInts;
	if (nBits == 0)
	    return;
	int bitsInHighWord = /* BigInteger. */bitLen(value[offset]);
	if (nBits >= bitsInHighWord) {
	    this.primitiveLeftShift(32 - nBits);
	    this.intLen--;
	} else {
	    primitiveRightShift(nBits);
	}
    }

    /**
         * Left shift this MutableBigInteger n bits.
         */
    private void leftShift(int n) {
	/*
         * If there is enough storage space in this MutableBigInteger already
         * the available space will be used. Space to the right of the used ints
         * in the value array is faster to utilize, so the extra space will be
         * taken from the right if possible.
         */
	if (intLen == 0)
	    return;
	int nInts = n >>> 5;
	int nBits = n & 0x1F;
	int bitsInHighWord = ModuloExponentiation.bitLen(value[offset]);

	// If shift can be done without moving words, do so
	if (n <= (32 - bitsInHighWord)) {
	    primitiveLeftShift(nBits);
	    return;
	}

	int newLen = intLen + nInts + 1;
	if (nBits <= (32 - bitsInHighWord))
	    newLen--;
	if (value.length < newLen) {
	    // The array must grow
	    int[] result = new int[newLen];
	    for (int i = 0; i < intLen; i++)
		result[i] = value[offset + i];
	    setValue(result, newLen);
	} else if (value.length - offset >= newLen) {
	    // Use space on right
	    for (int i = 0; i < newLen - intLen; i++)
		value[offset + intLen + i] = 0;
	} else {
	    // Must use space on left
	    for (int i = 0; i < intLen; i++)
		value[i] = value[offset + i];
	    for (int i = intLen; i < newLen; i++)
		value[i] = 0;
	    offset = 0;
	}
	intLen = newLen;
	if (nBits == 0)
	    return;
	if (nBits <= (32 - bitsInHighWord))
	    primitiveLeftShift(nBits);
	else
	    primitiveRightShift(32 - nBits);
    }

    /**
         * A primitive used for division. This method adds in one multiple of
         * the divisor a back to the dividend result at a specified offset. It
         * is used when qhat was estimated too large, and must be adjusted.
         */
    private int divadd(int[] a, int[] result, int offset) {
	long carry = 0;

	for (int j = a.length - 1; j >= 0; j--) {
	    long sum =
		    (a[j] & LONG_MASK) + (result[j + offset] & LONG_MASK)
			    + carry;
	    result[j + offset] = (int) sum;
	    carry = sum >>> 32;
	}
	return (int) carry;
    }

    /**
         * This method is used for division. It multiplies an n word input a by
         * one word input x, and subtracts the n word product from q. This is
         * needed when subtracting qhat*divisor from dividend.
         */
    private int mulsub(int[] q, int[] a, int x, int len, int offset) {
	long xLong = x & LONG_MASK;
	long carry = 0;
	offset += len;

	for (int j = len - 1; j >= 0; j--) {
	    long product = (a[j] & LONG_MASK) * xLong + carry;
	    long difference = q[offset] - product;
	    q[offset--] = (int) difference;
	    carry =
		    (product >>> 32)
			    + (((difference & LONG_MASK) > (((~(int) product) & LONG_MASK))) ? 1
				    : 0);
	}
	return (int) carry;
    }

    /**
         * Right shift this MutableBigInteger n bits, where n is less than 32.
         * Assumes that intLen > 0, n > 0 for speed
         */
    private final void primitiveRightShift(int n) {
	int[] val = value;
	int n2 = 32 - n;
	for (int i = offset + intLen - 1, c = val[i]; i > offset; i--) {
	    int b = c;
	    c = val[i - 1];
	    val[i] = (c << n2) | (b >>> n);
	}
	val[offset] >>>= n;
    }

    /**
         * Left shift this MutableBigInteger n bits, where n is less than 32.
         * Assumes that intLen > 0, n > 0 for speed
         */
    private final void primitiveLeftShift(int n) {
	int[] val = value;
	int n2 = 32 - n;
	for (int i = offset, c = val[i], m = i + intLen - 1; i < m; i++) {
	    int b = c;
	    c = val[i + 1];
	    val[i] = (b << n) | (c >>> n2);
	}
	val[offset + intLen - 1] <<= n;
    }

   

  
    
    /**
         * This method is used for division of an n word dividend by a one word
         * divisor. The quotient is placed into quotient. The one word divisor
         * is specified by divisor. The value of this MutableBigInteger is the
         * dividend at invocation but is replaced by the remainder.
         * 
         * NOTE: The value of this MutableBigInteger is modified by this method.
         */
    private void divideOneWord(int divisor, MutableBigInteger quotient) {
	long divLong = divisor & LONG_MASK;

	// Special case of one word dividend
	if (intLen == 1) {
	    long remValue = value[offset] & LONG_MASK;
	    quotient.value[0] = (int) (remValue / divLong);
	    quotient.intLen = (quotient.value[0] == 0) ? 0 : 1;
	    quotient.offset = 0;

	    value[0] = (int) (remValue - (quotient.value[0] * divLong));
	    offset = 0;
	    intLen = (value[0] == 0) ? 0 : 1;

	    return;
	}

	if (quotient.value.length < intLen)
	    quotient.value = new int[intLen];
	quotient.offset = 0;
	quotient.intLen = intLen;

	// Normalize the divisor
	int shift = 32 - ModuloExponentiation.bitLen(divisor);

	int rem = value[offset];
	long remLong = rem & LONG_MASK;
	if (remLong < divLong) {
	    quotient.value[0] = 0;
	} else {
	    quotient.value[0] = (int) (remLong / divLong);
	    rem = (int) (remLong - (quotient.value[0] * divLong));
	    remLong = rem & LONG_MASK;
	}

	int xlen = intLen;
	int[] qWord = new int[2];
	while (--xlen > 0) {
	    long dividendEstimate =
		    (remLong << 32)
			    | (value[offset + intLen - xlen] & LONG_MASK);
	    if (dividendEstimate >= 0) {
		qWord[0] = (int) (dividendEstimate / divLong);
		qWord[1] = (int) (dividendEstimate - (qWord[0] * divLong));
	    } else {
		divWord(qWord, dividendEstimate, divisor);
	    }
	    quotient.value[intLen - xlen] = (int) qWord[0];
	    rem = (int) qWord[1];
	    remLong = rem & LONG_MASK;
	}

	// Unnormalize
	if (shift > 0)
	    value[0] = rem %= divisor;
	else
	    value[0] = rem;
	intLen = (value[0] == 0) ? 0 : 1;

	quotient.normalize();
    }

    /**
         * Calculates the quotient and remainder of this div b and places them
         * in the MutableBigInteger objects provided.
         * 
         * Uses Algorithm D in Knuth section 4.3.1. Many optimizations to that
         * algorithm have been adapted from the Colin Plumb C library. It
         * special cases one word divisors for speed. The contents of a and b
         * are not changed.
         * 
         */
    private void divide(MutableBigInteger b, MutableBigInteger quotient,
	    MutableBigInteger rem) {
	if (b.intLen == 0)
	    throw new ArithmeticException("BigInteger divide by zero");

	// Dividend is zero
	if (intLen == 0) {
	    quotient.intLen = quotient.offset = rem.intLen = rem.offset = 0;
	    return;
	}

	int cmp = compare(b);

	// Dividend less than divisor
	if (cmp < 0) {
	    quotient.intLen = quotient.offset = 0;
	    rem.copyValue(this);
	    return;
	}
	// Dividend equal to divisor
	if (cmp == 0) {
	    quotient.value[0] = quotient.intLen = 1;
	    quotient.offset = rem.intLen = rem.offset = 0;
	    return;
	}

	quotient.clear();

	// Special case one word divisor
	if (b.intLen == 1) {
	    rem.copyValue(this);
	    rem.divideOneWord(b.value[b.offset], quotient);
	    return;
	}

	// Copy divisor value to protect divisor
	int[] d = new int[b.intLen];
	for (int i = 0; i < b.intLen; i++)
	    d[i] = b.value[b.offset + i];
	int dlen = b.intLen;

	// Remainder starts as dividend with space for a leading zero
	if (rem.value.length < intLen + 1)
	    rem.value = new int[intLen + 1];

	for (int i = 0; i < intLen; i++)
	    rem.value[i + 1] = value[i + offset];
	rem.intLen = intLen;
	rem.offset = 1;

	int nlen = rem.intLen;

	// Set the quotient size
	int limit = nlen - dlen + 1;
	if (quotient.value.length < limit) {
	    quotient.value = new int[limit];
	    quotient.offset = 0;
	}
	quotient.intLen = limit;
	int[] q = quotient.value;

	// D1 normalize the divisor
	int shift = 32 - ModuloExponentiation.bitLen(d[0]);
	if (shift > 0) {
	    // First shift will not grow array
	    ModuloExponentiation.primitiveLeftShift(d, dlen, shift);
	    // But this one might
	    rem.leftShift(shift);
	}

	// Must insert leading 0 in rem if its length did not change
	if (rem.intLen == nlen) {
	    rem.offset = 0;
	    rem.value[0] = 0;
	    rem.intLen++;
	}

	int dh = d[0];
	long dhLong = dh & LONG_MASK;
	int dl = d[1];
	int[] qWord = new int[2];

	// D2 Initialize j
	for (int j = 0; j < limit; j++) {
	    // D3 Calculate qhat
	    // estimate qhat
	    int qhat = 0;
	    int qrem = 0;
	    boolean skipCorrection = false;
	    int nh = rem.value[j + rem.offset];
	    int nh2 = nh + 0x80000000;
	    int nm = rem.value[j + 1 + rem.offset];

	    if (nh == dh) {
		qhat = ~0;
		qrem = nh + nm;
		skipCorrection = qrem + 0x80000000 < nh2;
	    } else {
		long nChunk = (((long) nh) << 32) | (nm & LONG_MASK);
		if (nChunk >= 0) {
		    qhat = (int) (nChunk / dhLong);
		    qrem = (int) (nChunk - (qhat * dhLong));
		} else {
		    divWord(qWord, nChunk, dh);
		    qhat = qWord[0];
		    qrem = qWord[1];
		}
	    }

	    if (qhat == 0)
		continue;

	    if (!skipCorrection) { // Correct qhat
		long nl = rem.value[j + 2 + rem.offset] & LONG_MASK;
		long rs = ((qrem & LONG_MASK) << 32) | nl;
		long estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);

		if (unsignedLongCompare(estProduct, rs)) {
		    qhat--;
		    qrem = (int) ((qrem & LONG_MASK) + dhLong);
		    if ((qrem & LONG_MASK) >= dhLong) {
			estProduct = (dl & LONG_MASK) * (qhat & LONG_MASK);
			rs = ((qrem & LONG_MASK) << 32) | nl;
			if (unsignedLongCompare(estProduct, rs))
			    qhat--;
		    }
		}
	    }

	    // D4 Multiply and subtract
	    rem.value[j + rem.offset] = 0;
	    int borrow = mulsub(rem.value, d, qhat, dlen, j + rem.offset);

	    // D5 Test remainder
	    if (borrow + 0x80000000 > nh2) {
		// D6 Add back
		divadd(d, rem.value, j + 1 + rem.offset);
		qhat--;
	    }

	    // Store the quotient digit
	    q[j] = qhat;
	} // D7 loop on j

	// D8 Unnormalize
	if (shift > 0)
	    rem.rightShift(shift);

	rem.normalize();
	quotient.normalize();
    }

    /**
         * Compare two longs as if they were unsigned. Returns true iff one is
         * bigger than two.
         */
    private boolean unsignedLongCompare(long one, long two) {
	return (one + Long.MIN_VALUE) > (two + Long.MIN_VALUE);
    }

    /**
         * This method divides a long quantity by an int to estimate qhat for
         * two multi precision numbers. It is used when the signed value of n is
         * less than zero.
         */
    private void divWord(int[] result, long n, int d) {
	long dLong = d & LONG_MASK;

	if (dLong == 1) {
	    result[0] = (int) n;
	    result[1] = 0;
	    return;
	}

	// Approximate the quotient and remainder
	long q = (n >>> 1) / (dLong >>> 1);
	long r = n - q * dLong;

	// Correct the approximation
	while (r < 0) {
	    r += dLong;
	    q--;
	}
	while (r >= dLong) {
	    r -= dLong;
	    q++;
	}

	// n - q*dlong == r && 0 <= r <dLong, hence we're done.
	result[0] = (int) q;
	result[1] = (int) r;
    }
    }
    /*
         * Returns the multiplicative inverse of val mod 2^32. Assumes val is
         * odd.
         */
    private static int inverseMod32(int val) {
	// Newton's iteration!
	int t = val;
	t *= 2 - val * t;
	t *= 2 - val * t;
	t *= 2 - val * t;
	t *= 2 - val * t;
	return t;
    }

    /*
         * public int[] oddModPow(MutableBigInteger expMBI, MutableBigInteger
         * modMBI)
         */
    
    public static int oddModPow(byte[] result, byte[] base, byte[] exp, byte[] mod) {
	if (result.length%4!=0) 
	    throw new IllegalArgumentException("Length of result buffer must be a multiple of four.");
	int[] r= new int[result.length/4];
	int length = oddModPow(r,stripLeadingZeroBytes(base), stripLeadingZeroBytes(exp), stripLeadingZeroBytes(mod));

	for (int i=0;i<r.length;i++) {
	    result[i*4] =  (byte)(r[i]>>24&0xff);
	    result[i*4+1] =  (byte)(r[i]>>16&0xff);
	    result[i*4+2] =  (byte)(r[i]>>8&0xff);
	    result[i*4+3] =  (byte)(r[i]>>0&0xff);
	}	
	return length*4;
    }
    
    public static int oddModPow(int[] result, int[] base, int[] exp, int[] mod) {
	/*
         * The algorithm is adapted from Colin Plumb's C library.
         * 
         * The window algorithm: The idea is to keep a running product of b1 =
         * n^(high-order bits of exp) and then keep appending exponent bits to
         * it. The following patterns apply to a 3-bit window (k = 3): To append
         * 0: square To append 1: square, multiply by n^1 To append 10: square,
         * multiply by n^1, square To append 11: square, square, multiply by n^3
         * To append 100: square, multiply by n^1, square, square To append 101:
         * square, square, square, multiply by n^5 To append 110: square,
         * square, multiply by n^3, square To append 111: square, square,
         * square, multiply by n^7
         * 
         * Since each pattern involves only one multiply, the longer the pattern
         * the better, except that a 0 (no multiplies) can be appended directly.
         * We precompute a table of odd powers of n, up to 2^k, and can then
         * multiply k bits of exponent at a time. Actually, assuming random
         * exponents, there is on average one zero bit between needs to multiply
         * (1/2 of the time there's none, 1/4 of the time there's 1, 1/8 of the
         * time, there's 2, 1/32 of the time, there's 3, etc.), so you have to
         * do one multiply per k+1 bits of exponent.
         * 
         * The loop walks down the exponent, squaring the result buffer as it
         * goes. There is a wbits+1 bit lookahead buffer, buf, that is filled
         * with the upcoming exponent bits. (What is read after the end of the
         * exponent is unimportant, but it is filled with zero here.) When the
         * most-significant bit of this buffer becomes set, i.e. (buf & tblmask) !=
         * 0, we have to decide what pattern to multiply by, and when to do it.
         * We decide, remember to do it in future after a suitable number of
         * squarings have passed (e.g. a pattern of "100" in the buffer requires
         * that we multiply by n^1 immediately; a pattern of "110" calls for
         * multiplying by n^3 after one more squaring), clear the buffer, and
         * continue.
         * 
         * When we start, there is one more optimization: the result buffer is
         * implcitly one, so squaring it or multiplying by it can be optimized
         * away. Further, if we start with a pattern like "100" in the lookahead
         * window, rather than placing n into the buffer and then starting to
         * square it, we have already computed n^2 to compute the odd-powers
         * table, so we can place that into the buffer and save a squaring.
         * 
         * This means that if you have a k-bit window, to compute n^z, where z
         * is the high k bits of the exponent, 1/2 of the time it requires no
         * squarings. 1/4 of the time, it requires 1 squaring, ... 1/2^(k-1) of
         * the time, it reqires k-2 squarings. And the remaining 1/2^(k-1) of
         * the time, the top k bits are a 1 followed by k-1 0 bits, so it again
         * only requires k-2 squarings, not k-1. The average of these is 1. Add
         * that to the one squaring we have to do to compute the table, and
         * you'll see that a k-bit window saves k-2 squarings as well as
         * reducing the multiplies. (It actually doesn't hurt in the case k = 1,
         * either.)
         */
	if (result.length!=mod.length) {
	    throw new IllegalArgumentException("The result buffer must have the same length as the modulo buffer");
	}
	    
	if ((mod.length == 0) || ((mod[mod.length - 1] & 1) == 0)) {
	    throw new IllegalArgumentException("Modulus must be odd");
	}
	
	int modLen = mod.length; 

	// Select an appropriate window size
	int wbits = 0;

	int ebits = ModuloExponentiation.bitLength(exp, exp.length);

	while (ebits > bnExpModThreshTable[wbits])
	    wbits++;

	// Calculate appropriate table size
	int tblmask = 1 << wbits;

	// Allocate table for precomputed odd powers of base in Montgomery form
	int[][] table = new int[tblmask][];
	for (int i = 0; i < tblmask; i++)
	    table[i] = new int[modLen];

	// Compute the modular inverse
	int inv = -ModuloExponentiation.inverseMod32(mod[modLen - 1]);

	// Convert base to Montgomery form
	int[] a = leftShift(base, base.length, modLen << 5);

	MutableBigInteger q = new MutableBigInteger(), r =
		new MutableBigInteger(), a2 = new MutableBigInteger(a), b2 =
		new MutableBigInteger(mod);

	a2.divide(b2, q, r);
	table[0] = r.toIntArray();

	// Pad table[0] with leading zeros so its length is at least modLen
	if (table[0].length < modLen) {
	    int offset = modLen - table[0].length;
	    int[] t2 = new int[modLen];
	    for (int i = 0; i < table[0].length; i++)
		t2[i + offset] = table[0][i];
	    table[0] = t2;
	}

	// Set b to the square of the base
	int[] b = squareToLen(table[0], modLen, null);
	b = montReduce(b, mod, modLen, inv);

	// Set t to high half of b
	int[] t = new int[modLen];
	for (int i = 0; i < modLen; i++)
	    t[i] = b[i];

	// Fill in the table with odd powers of the base
	for (int i = 1; i < tblmask; i++) {
	    int[] prod = multiplyToLen(t, modLen, table[i - 1], modLen, null);
	    table[i] = montReduce(prod, mod, modLen, inv);
	}

	// Pre load the window that slides over the exponent
	int bitpos = 1 << ((ebits - 1) & (32 - 1));

	int buf = 0;
	int elen = exp.length;
	int eIndex = 0;
	for (int i = 0; i <= wbits; i++) {
	    buf = (buf << 1) | (((exp[eIndex] & bitpos) != 0) ? 1 : 0);
	    bitpos >>>= 1;
	    if (bitpos == 0) {
		eIndex++;
		bitpos = 1 << (32 - 1);
		elen--;
	    }
	}

	int multpos = ebits;

	// The first iteration, which is hoisted out of the main loop
	ebits--;
	boolean isone = true;

	multpos = ebits - wbits;
	while ((buf & 1) == 0) {
	    buf >>>= 1;
	    multpos++;
	}

	int[] mult = table[buf >>> 1];

	buf = 0;
	if (multpos == ebits)
	    isone = false;

	// The main loop
	while (true) {
	    ebits--;
	    // Advance the window
	    buf <<= 1;

	    if (elen != 0) {
		buf |= ((exp[eIndex] & bitpos) != 0) ? 1 : 0;
		bitpos >>>= 1;
		if (bitpos == 0) {
		    eIndex++;
		    bitpos = 1 << (32 - 1);
		    elen--;
		}
	    }

	    // Examine the window for pending multiplies
	    if ((buf & tblmask) != 0) {
		multpos = ebits - wbits;
		while ((buf & 1) == 0) {
		    buf >>>= 1;
		    multpos++;
		}
		mult = table[buf >>> 1];
		buf = 0;
	    }

	    // Perform multiply
	    if (ebits == multpos) {
		if (isone) {
		    // b = (int[])mult.clone();
		    b = new int[mult.length];

		    System.arraycopy(mult, 0, b, 0, mult.length);

		    isone = false;
		} else {
		    t = b;
		    a = multiplyToLen(t, modLen, mult, modLen, a);
		    a = montReduce(a, mod, modLen, inv);
		    t = a;
		    a = b;
		    b = t;
		}
	    }

	    // Check if done
	    if (ebits == 0)
		break;

	    // Square the input
	    if (!isone) {
		t = b;
		a = squareToLen(t, modLen, a);
		a = montReduce(a, mod, modLen, inv);
		t = a;
		a = b;
		b = t;
	    }
	}

	// Convert result out of Montgomery form and return
	int[] t2 = new int[2 * modLen];
	for (int i = 0; i < modLen; i++)
	    t2[i + modLen] = b[i];

	b = montReduce(t2, mod, modLen, inv);

	for (int i = 0; i < modLen; i++)
	    result[i] = b[i];
	return result.length;
	
    }

    /**
         * Montgomery reduce n, modulo mod. This reduces modulo mod and divides
         * by 2^(32*mlen). Adapted from Colin Plumb's C library.
         */
    private static int[] montReduce(int[] n, int[] mod, int mlen, int inv) {
	int c = 0;
	int len = mlen;
	int offset = 0;

	do {
	    int nEnd = n[n.length - 1 - offset];
	    int carry = mulAdd(n, mod, offset, mlen, inv * nEnd);
	    c += addOne(n, offset, mlen, carry);
	    offset++;
	} while (--len > 0);

	while (c > 0)
	    c += subN(n, mod, mlen);

	while (intArrayCmpToLen(n, mod, mlen) >= 0)
	    subN(n, mod, mlen);

	return n;
    }

    /*
         * Returns -1, 0 or +1 as big-endian unsigned int array arg1 is less
         * than, equal to, or greater than arg2 up to length len.
         */
    private static int intArrayCmpToLen(int[] arg1, int[] arg2, int len) {
	for (int i = 0; i < len; i++) {
	    long b1 = arg1[i] & MutableBigInteger.LONG_MASK;
	    long b2 = arg2[i] & MutableBigInteger.LONG_MASK;
	    if (b1 < b2)
		return -1;
	    if (b1 > b2)
		return 1;
	}
	return 0;
    }

    /**
         * bitLen(val) is the number of bits in val.
         */
    private static int bitLen(int w) {
	// Binary search - decision tree (5 tests, rarely 6)
	return (w < 1 << 15 ? (w < 1 << 7 ? (w < 1 << 3 ? (w < 1 << 1 ? (w < 1 << 0 ? (w < 0 ? 32
		: 0)
		: 1)
		: (w < 1 << 2 ? 2 : 3))
		: (w < 1 << 5 ? (w < 1 << 4 ? 4 : 5) : (w < 1 << 6 ? 6 : 7)))
		: (w < 1 << 11 ? (w < 1 << 9 ? (w < 1 << 8 ? 8 : 9)
			: (w < 1 << 10 ? 10 : 11))
			: (w < 1 << 13 ? (w < 1 << 12 ? 12 : 13)
				: (w < 1 << 14 ? 14 : 15))))
		: (w < 1 << 23 ? (w < 1 << 19 ? (w < 1 << 17 ? (w < 1 << 16 ? 16
			: 17)
			: (w < 1 << 18 ? 18 : 19))
			: (w < 1 << 21 ? (w < 1 << 20 ? 20 : 21)
				: (w < 1 << 22 ? 22 : 23)))
			: (w < 1 << 27 ? (w < 1 << 25 ? (w < 1 << 24 ? 24 : 25)
				: (w < 1 << 26 ? 26 : 27))
				: (w < 1 << 29 ? (w < 1 << 28 ? 28 : 29)
					: (w < 1 << 30 ? 30 : 31)))));
    }

    /**
         * Calculate bitlength of contents of the first len elements an int
         * array, assuming there are no leading zero ints.
         */
    private static int bitLength(int[] val, int len) {
	if (len == 0)
	    return 0;
	return ((len - 1) << 5) + bitLen(val[0]);
    }

    // Modular Arithmetic Operations

    static int[] bnExpModThreshTable =
	    { 7, 25, 81, 241, 673, 1793, Integer.MAX_VALUE }; // Sentinel

    /**
         * Multiply an array by one word k and add to result, return the carry
         */
    private static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
	long kLong = k & MutableBigInteger.LONG_MASK;
	long carry = 0;

	offset = out.length - offset - 1;
	for (int j = len - 1; j >= 0; j--) {
	    long product =
		    (in[j] & MutableBigInteger.LONG_MASK) * kLong + (out[offset] & MutableBigInteger.LONG_MASK)
			    + carry;
	    out[offset--] = (int) product;
	    carry = product >>> 32;
	}
	return (int) carry;
    }

    /**
         * Add one word to the number a mlen words into a. Return the resulting
         * carry.
         */
    private static int addOne(int[] a, int offset, int mlen, int carry) {
	offset = a.length - 1 - mlen - offset;
	long t = (a[offset] & MutableBigInteger.LONG_MASK) + (carry & MutableBigInteger.LONG_MASK);

	a[offset] = (int) t;
	if ((t >>> 32) == 0)
	    return 0;
	while (--mlen >= 0) {
	    if (--offset < 0) { // Carry out of number
		return 1;
	    } else {
		a[offset]++;
		if (a[offset] != 0)
		    return 0;
	    }
	}
	return 1;
    }

    /**
         * Squares the contents of the int array x. The result is placed into
         * the int array z. The contents of x are not changed.
         */
    private static final int[] squareToLen(int[] x, int len, int[] z) {
	/*
         * The algorithm used here is adapted from Colin Plumb's C library.
         * Technique: Consider the partial products in the multiplication of
         * "abcde" by itself:
         * 
         * a b c d e * a b c d e ================== ae be ce de ee ad bd cd dd
         * de ac bc cc cd ce ab bb bc bd be aa ab ac ad ae
         * 
         * Note that everything above the main diagonal: ae be ce de = (abcd) *
         * e ad bd cd = (abc) * d ac bc = (ab) * c ab = (a) * b
         * 
         * is a copy of everything below the main diagonal: de cd ce bc bd be ab
         * ac ad ae
         * 
         * Thus, the sum is 2 * (off the diagonal) + diagonal.
         * 
         * This is accumulated beginning with the diagonal (which consist of the
         * squares of the digits of the input), which is then divided by two,
         * the off-diagonal added, and multiplied by two again. The low bit is
         * simply a copy of the low bit of the input, so it doesn't need special
         * care.
         */
	int zlen = len << 1;
	if (z == null || z.length < zlen)
	    z = new int[zlen];

	// Store the squares, right shifted one bit (i.e., divided by 2)
	int lastProductLowWord = 0;
	for (int j = 0, i = 0; j < len; j++) {
	    long piece = (x[j] & MutableBigInteger.LONG_MASK);
	    long product = piece * piece;
	    z[i++] = (lastProductLowWord << 31) | (int) (product >>> 33);
	    z[i++] = (int) (product >>> 1);
	    lastProductLowWord = (int) product;
	}

	// Add in off-diagonal sums
	for (int i = len, offset = 1; i > 0; i--, offset += 2) {
	    int t = x[i - 1];
	    t = mulAdd(z, x, offset, i - 1, t);
	    addOne(z, offset - 1, i, t);
	}

	// Shift back up and set low bit
	primitiveLeftShift(z, zlen, 1);
	z[zlen - 1] |= x[len - 1] & 1;

	return z;
    }

    /**
         * Left shift int array a up to len by n bits. Returns the array that
         * results from the shift since space may have to be reallocated.
         */
    private static int[] leftShift(int[] a, int len, int n) {
	int nInts = n >>> 5;
	int nBits = n & 0x1F;
	int bitsInHighWord = bitLen(a[0]);

	// If shift can be done without recopy, do so
	if (n <= (32 - bitsInHighWord)) {
	    primitiveLeftShift(a, len, nBits);
	    return a;
	} else { // Array must be resized
	    if (nBits <= (32 - bitsInHighWord)) {
		int result[] = new int[nInts + len];
		for (int i = 0; i < len; i++)
		    result[i] = a[i];
		primitiveLeftShift(result, result.length, nBits);
		return result;
	    } else {
		int result[] = new int[nInts + len + 1];
		for (int i = 0; i < len; i++)
		    result[i] = a[i];
		primitiveRightShift(result, result.length, 32 - nBits);
		return result;
	    }
	}
    }

    // shifts a up to len right n bits assumes no leading zeros, 0<n<32
    private static void primitiveRightShift(int[] a, int len, int n) {
	int n2 = 32 - n;
	for (int i = len - 1, c = a[i]; i > 0; i--) {
	    int b = c;
	    c = a[i - 1];
	    a[i] = (c << n2) | (b >>> n);
	}
	a[0] >>>= n;
    }

    // shifts a up to len left n bits assumes no leading zeros, 0<=n<32
    private static void primitiveLeftShift(int[] a, int len, int n) {
	if (len == 0 || n == 0)
	    return;

	int n2 = 32 - n;
	for (int i = 0, c = a[i], m = i + len - 1; i < m; i++) {
	    int b = c;
	    c = a[i + 1];
	    a[i] = (b << n) | (c >>> n2);
	}
	a[len - 1] <<= n;
    }

    /**
         * Multiplies int arrays x and y to the specified lengths and places the
         * result into z.
         */
    private static int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen,
	    int[] z) {
	int xstart = xlen - 1;
	int ystart = ylen - 1;

	if (z == null || z.length < (xlen + ylen))
	    z = new int[xlen + ylen];

	long carry = 0;
	for (int j = ystart, k = ystart + 1 + xstart; j >= 0; j--, k--) {
	    long product = (y[j] & MutableBigInteger.LONG_MASK) * (x[xstart] & MutableBigInteger.LONG_MASK) + carry;
	    z[k] = (int) product;
	    carry = product >>> 32;
	}
	z[xstart] = (int) carry;

	for (int i = xstart - 1; i >= 0; i--) {
	    carry = 0;
	    for (int j = ystart, k = ystart + 1 + i; j >= 0; j--, k--) {
		long product =
			(y[j] & MutableBigInteger.LONG_MASK) * (x[i] & MutableBigInteger.LONG_MASK)
				+ (z[k] & MutableBigInteger.LONG_MASK) + carry;
		z[k] = (int) product;
		carry = product >>> 32;
	    }
	    z[i] = (int) carry;
	}
	return z;
    }

    /**
         * Subtracts two numbers of same length, returning borrow.
         */
    private static int subN(int[] a, int[] b, int len) {
	long sum = 0;

	while (--len >= 0) {
	    sum = (a[len] & MutableBigInteger.LONG_MASK) - (b[len] & MutableBigInteger.LONG_MASK) + (sum >> 32);
	    a[len] = (int) sum;
	}

	return (int) (sum >> 32);
    }
    
    /**
     * Returns a copy of the input array stripped of any leading zero bytes.
     */
    private static int[] stripLeadingZeroBytes(byte a[]) {
	int byteLength = a.length;
	int keep;

	// Find first nonzero byte
	for (keep = 0; keep < a.length && a[keep] == 0; keep++)
	    ;

	// Allocate new array and copy relevant part of input array
	int intLength = ((byteLength - keep) + 3) / 4;
	int[] result = new int[intLength];
	int b = byteLength - 1;
	for (int i = intLength - 1; i >= 0; i--) {
	    result[i] = a[b--] & 0xff;
	    int bytesRemaining = b - keep + 1;
	    int bytesToTransfer = Math.min(3, bytesRemaining);
	    for (int j = 8; j <= 8 * bytesToTransfer; j += 8)
		result[i] |= ((a[b--] & 0xff) << j);
	}
	return result;
}

}
