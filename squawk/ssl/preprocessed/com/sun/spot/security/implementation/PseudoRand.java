/*
 * Copyright 2000-2008 Sun Microsystems, Inc. All Rights Reserved.
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

import com.sun.spot.security.DigestException;
import com.sun.spot.security.MessageDigest;
import com.sun.spot.security.NoSuchAlgorithmException;


/**
 * Implements a pseudo random number generator. * 
 * <p>
 * <b>Warning</b>: This method is not part of the Security and Trust Services
 * API (SATSA) 1.0 specification.
 */ 
public final class PseudoRand extends SecureRandom {
    /** Local handle to message digest. */
    private static MessageDigest md = null;

    /**
     * For an arbitrary choice of the default seed, we use bits from the 
     * binary expansion of pi (see IETF RFC 2412, 
     * http://www.ietf.org/rfc/rfc2412.txt)
     */
    private static byte[] seed = {
	(byte) 0xC9, (byte) 0x0F, (byte) 0xDA, (byte) 0xA2,
	(byte) 0x21, (byte) 0x68, (byte) 0xC2, (byte) 0x34,
	(byte) 0xC4, (byte) 0xC6, (byte) 0x62, (byte) 0x8B,
	(byte) 0x80, (byte) 0xDC, (byte) 0x1C, (byte) 0xD1
    };

    /** buffer of random bytes */
    private static byte[] randomBytes;
    
    /** number of random bytes currently available */
    private static int bytesAvailable = 0;
    
    /** temporary buffer to store System.currentTimeMillis() in a byte array */
    private static byte[] timeTmp;
    
    /** Constructor for random data. */
    public PseudoRand() throws NoSuchAlgorithmException {
	if (md != null) 
	    return;
	
	try {
	    md = MessageDigest.getInstance("MD5");
	} catch (Exception e) {
	    throw new NoSuchAlgorithmException("Algorithm MD5 not available");
	}
	randomBytes = new byte[seed.length];
        timeTmp = new byte[8];
	updateSeed();
    }
    
    /**
     * This does a reasonable job of producing unpredictable
     * random data by using a one way hash as a mixing function and
     * the current time in milliseconds as a source of entropy.
     * @param b buffer of input data
     * @param off offset into the provided buffer
     * @param len length of the data to be processed
     */ 
    public void generateData(byte[] b, int off, int len) {
        if (len == 0) return;
	synchronized (md) {
	    int i = 0;
	    
	    while (true) {
		// see if we need to buffer more random bytes
		if (bytesAvailable == 0) {
		    md.update(seed, 0, seed.length);		    
		    try {
			md.digest(randomBytes, 0, randomBytes.length);
		    } catch (DigestException e) {
			throw new RuntimeException("Internal Error: "+e.getMessage());
		    }
		    updateSeed();
		    bytesAvailable = randomBytes.length;
		}
		
		// hand out some of the random bytes from the buffer
		while (bytesAvailable > 0) {
		    if (i == len)
			return;
		    b[off + i] = randomBytes[--bytesAvailable];
		    i++;
		}
	    }
	}
    }
    /**
     * Set the random number seed.
     * @param b initial data to use as the seed 
     * @param off offset into the provided buffer
     * @param len length of the data to be used
     */
    public void setSeed(byte[] b, int off, int len) {
	int j = 0;

	if ((len <= 0) || (b.length < (off + len)))
	    return;
	for (int i = 0; i < seed.length; i++, j++) {
	    if (j == len) j = 0;
	    seed[i] = b[off + j];
	}
    }
    
    /**
     * This does a reasonable job of producing unpredictable
     * random data by using a one way hash as a mixing function and
     * the current time in milliseconds as a source of entropy.
     */ 
    private void updateSeed() {
	long t = System.currentTimeMillis();
	
	// Convert the long value into a byte array
	for (int i = 0; i < 8; i++) {
	    timeTmp[i] = (byte) (t & 0xff);
	    t = (t >>> 8);
	}
	
	md.update(seed, 0, seed.length);
	
	md.update(timeTmp, 0, timeTmp.length);
	try {
	    md.digest(seed, 0, seed.length);
	} catch (DigestException e) {
	    throw new RuntimeException("Internal Error: "+e.getMessage());	    
	}	
    }
}
