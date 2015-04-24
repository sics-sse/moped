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

/*
 * $Id$
 *
 * Implements the MD5 hashing algorithm.
 * 
 * $Log$
 * Revision 1.1  2006/09/15 21:41:47  christian
 * Sun Spot CryptoLibrary.
 * Implements the SATSA specification.
 *
 * Revision 1.1  2006/09/15 21:36:52  christian
 * Changed to SATSA interfaces, javadoc added,
 * stripped down BigInteger classes.
 *
 * Revision 1.2  2006/07/25 00:25:54  christian
 * TLS support working.
 * Fixed a bug in MD5 if  digest buffer is larger than actual buffer.
 *
 * Revision 1.1  2006/07/21 23:37:42  christian
 * SSL code based on midp (cougar).
 *
 * Revision 1.1  2006/04/10 18:50:47  christian
 * *** empty log message ***
 *
 * Revision 1.1  2006/02/22 23:11:44  cp198493
 * Secure remote deployment based on sdk-16Feb2006
 *
 * Revision 1.2  2000/03/31 02:24:21  vgupta
 * Modified reset() to zero out buffer.
 *
 * Revision 1.1  2000/03/29 02:51:15  vgupta
 * Initial revision
 *
 */

package com.sun.spot.security.implementation;

import com.sun.spot.security.MessageDigest;


public final class MD5 extends MessageDigest {

    private static final int[] S1 = { 7, 12, 17, 22};
    private static final int[] S2 = { 5,  9, 14, 20};
    private static final int[] S3 = { 4, 11, 16, 23};
    private static final int[] S4 = { 6, 10, 15, 21};
    
    private static final int[] CONSTS = {
        0xD76AA478, 0xE8C7B756, 0x242070DB, 0xC1BDCEEE,
        0xF57C0FAF, 0x4787C62A, 0xA8304613, 0xFD469501,
        0x698098D8, 0x8B44F7AF, 0xFFFF5BB1, 0x895CD7BE,
        0x6B901122, 0xFD987193, 0xA679438E, 0x49B40821,

        0xF61E2562, 0xC040B340, 0x265E5A51, 0xE9B6C7AA,
        0xD62F105D, 0x02441453, 0xD8A1E681, 0xE7D3FBC8,
        0x21E1CDE6, 0xC33707D6, 0xF4D50D87, 0x455A14ED,
        0xA9E3E905, 0xFCEFA3F8, 0x676F02D9, 0x8D2A4C8A,

        0xFFFA3942, 0x8771F681, 0x6D9D6122, 0xFDE5380C,
        0xA4BEEA44, 0x4BDECFA9, 0xF6BB4B60, 0xBEBFBC70,
        0x289B7EC6, 0xEAA127FA, 0xD4EF3085, 0x04881D05,
        0xD9D4D039, 0xE6DB99E5, 0x1FA27CF8, 0xC4AC5665,

        0xF4292244, 0x432AFF97, 0xAB9423A7, 0xFC93A039,
        0x655B59C3, 0x8F0CCC92, 0xFFEFF47D, 0x85845DD1,
        0x6FA87E4F, 0xFE2CE6E0, 0xA3014314, 0x4E0811A1,
        0xF7537E82, 0xBD3AF235, 0x2AD7D2BB, 0xEB86D391
    };

    /* Internal context */
    // state: A, B, C, D
    private int[] state = new int[4];
    // number of bits modulo 2^64
    private long count;
    private int index;
    // input buffer
    private byte[] buffer = new byte[64];
    private int[] block = new int[16];
    
    
    /* 
     * MD5 basic transformation. Transforms state based on a 64-byte block.
     */
    private void transform (byte[] bblock, int offset) {
        int tmp;
        int i, j, s;
        int bufIdx;
        
	int a = state[0];
	int b = state[1];
	int c = state[2];
	int d = state[3];
	    
	for (i = 0, j = offset; j < offset + 64; i++, j += 4) {
	    block[i] = ((bblock[j  ] & 0xff)      ) |
                       ((bblock[j+1] & 0xff) <<  8) |
                       ((bblock[j+2] & 0xff) << 16) |
                       ((bblock[j+3] & 0xff) << 24);
	}
        
        i = 0;
	    
	/* Round 1 */
        bufIdx = 0;
        for (; i < 16; i++) {
            s = S1[i & 0x03];
            a += ((b & c) | (~b & d)) + block[bufIdx] + CONSTS[i];
            tmp = d; d = c; c = b; b += ((a << s) | (a >>> (32 - s))); a = tmp;
            bufIdx++;
        }
	
	/* Round 2 */
        bufIdx = 1;
        for (; i < 32; i++) {
            s = S2[i & 0x03];
            a += ((b & d) | (c & ~d)) + block[bufIdx] + CONSTS[i];
            tmp = d; d = c; c = b; b += ((a << s) | (a >>> (32 - s))); a = tmp;
            bufIdx = (bufIdx + 5) & 0x0f;
        }
	
	/* Round 3 */
        bufIdx = 5;
        for (; i < 48; i++) {
            s = S3[i & 0x03];
            a += (b ^ c ^ d) + block[bufIdx] + CONSTS[i];
            tmp = d; d = c; c = b; b += ((a << s) | (a >>> (32 - s))); a = tmp;
            bufIdx = (bufIdx + 3) & 0x0f;
        }
	
	/* Round 4 */
        bufIdx = 0;
        for (; i < 64; i++) {
            s = S4[i & 0x03];
            a += (c ^ (b | ~d)) + block[bufIdx] + CONSTS[i];
            tmp = d; d = c; c = b; b += ((a << s) | (a >>> (32 - s))); a = tmp;
            bufIdx = (bufIdx + 7) & 0x0f;
        }
	
	state[0] += a;
	state[1] += b;
	state[2] += c;
	state[3] += d;
    }
    
    /* 
     * MD5 finalization. Ends an MD5 message-digest operation, writing the
     * the message digest and zeroizing the context.
     */
    public int digest(byte[] outBuf, int outOff, int len) {
        int shift;
        int i = index;        
        
        int cnt = (outBuf.length > outOff) ? (outBuf.length - outOff) : 0;
	if (cnt == 0) return 0;
	if (cnt > getDigestLength()) cnt = getDigestLength();
        
        buffer[i++] = (byte)128;
        
	// Pad out to 56 mod 64. 
	if (i > 56) {
            while (i < 64) {
                buffer[i++] = (byte)0;
            }
            transform(buffer, 0);
            i = 0;
        }
        while (i < 56) {
            buffer[i++] = (byte)0;
        }
	
	// Append length (before padding) 
	buffer[56] = (byte) (count);
	buffer[57] = (byte) (count >>>  8);
	buffer[58] = (byte) (count >>> 16);
	buffer[59] = (byte) (count >>> 24);
	buffer[60] = (byte) (count >>> 32);
	buffer[61] = (byte) (count >>> 40);
	buffer[62] = (byte) (count >>> 48);
	buffer[63] = (byte) (count >>> 56);
        transform(buffer, 0);
	
	// Store state in digest
        shift = 0;
        for (i = 0; i < cnt; i++) {
            outBuf[outOff++] = (byte)(state[i>>2] >> shift);
            if ((shift += 8) == 32) {
                shift = 0;
            }
        }
        reset();
        return cnt;
    }
    
    public MD5() {
        reset();
    }
    
    public String getAlgorithm() { 
	return "MD5";
    }
    
    
    /* MD5 initialization. Begins an MD5 operation, writing a new context */
    public void reset() {
	count = 0;
        index = 0;

	// Load magic initialization constants.
	state[0] = 0x67452301;
	state[1] = 0xefcdab89;
	state[2] = 0x98badcfe;
	state[3] = 0x10325476;
	
	for (int i = 63; i >= 0; i--) buffer[i] = (byte)0;
	for (int i = 15; i >= 0; i--) block[i] = 0;
    }

    /*
     * MD5 block update operation. Continues an MD5 message-digest
     * operation, processing another message block, and updating internal
     * context.
     */
    public void update(byte[] input, int offset, int len) {
	int partLen;
	
	// Update number of bits 
	count += (len << 3);
	
        // If there is already something in the buffer, append the new data.
        if (index > 0) {
            partLen = 64 - index;
            if (partLen > len) partLen = len;
            System.arraycopy(input, offset, buffer, index, partLen);
            offset += partLen;
            len -= partLen;
            index += partLen;
            if (index == 64) {
                transform(buffer, 0);
                index = 0;
            }
        }
        
        // Process complete blocks directly from the input-array as long as
        // possible.
        while (len >= 64) {
            transform(input, offset);
            offset += 64;
            len -= 64;
        }
        
        // If there is some unprocessed data left, copy it to the buffer.
        if (len > 0) {
            System.arraycopy(input, offset, buffer, 0, len);
            index = len;
        }
    }

    /*public int doFinal(byte[] inBuf, int inOff,
                       int inLen, byte[] outBuf,
                       int outOff) {
	update(inBuf, inOff, inLen);
	int cnt = (outBuf.length > outOff) ? (outBuf.length - outOff) : 0;
	if (cnt == 0) return 0;
	if (cnt > 16) cnt = 16;
        digest(outBuf, outOff, cnt);
        reset();
	return cnt;
    }*/

    public Object clone() {
	MD5 cpy = new MD5();
	
	System.arraycopy(this.state, 0, cpy.state, 0, 4);
	cpy.count=count;
	cpy.index=index;
	
	System.arraycopy(this.buffer, 0, cpy.buffer, 0, 64);
	System.arraycopy(this.block, 0, cpy.block, 0, 16);
	return cpy;
    }

    
    public int getDigestLength() {	
	return 16;
    }    
    
}
