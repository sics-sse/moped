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

import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.MessageDigest;
import com.sun.spot.security.NoSuchAlgorithmException;
import com.sun.spot.security.PrivateKey;
import com.sun.spot.security.PublicKey;
import com.sun.spot.security.Signature;
import com.sun.spot.security.SignatureException;


/**
 * Implements RSA SHA1 Signatures.
 */ 
public final class RsaShaSig extends Signature {
    /**
     * Expected prefix in decrypted value when SHA-1 hash is used 
     * with RSA signing. This prefix is followed by the SHA hash.
     */ 
    private static final byte[] PREFIX_SHA1 = {
        (byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09,
        (byte) 0x06, (byte) 0x05, (byte) 0x2b, (byte) 0x0e,
        (byte) 0x03, (byte) 0x02, (byte) 0x1a, (byte) 0x05,
        (byte) 0x00, (byte) 0x04, (byte) 0x14
    };

    /** Common signature class. */
    RSASig rsaSig;

    /**
     * Constructs an RSA signature object that uses SHA1 as 
     * message digest algorithm.
     *
     * @exception RuntimeException if SHA-1 is not available
     */
    public RsaShaSig() {
        try {
            rsaSig =
                new RSASig(PREFIX_SHA1, MessageDigest.getInstance("SHA-1"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Needed algorithm not available");
        }
    }
    
    /**
     * Gets the signature algorithm.
     *
     * @return the algorithmimplemented by this signature object
     */ 
    public String getAlgorithm() {
	return "SHA1withRSA";
    }
    
    /**
     * Gets the byte-length of the signature.
     * 
     * @return the byte-length of the signature produced by this object
     */ 
    public int getLength() {
        return rsaSig.getLength();
    }
    
    /**
     * Initializes the <CODE>RSASig</CODE> object with the appropriate
     * <CODE>Key</CODE> for signature verification.
     * 
     * @param theKey the key object to use for verification
     *
     * @exception InvalidKeyException if the key type is inconsistent 
     * with the mode or signature implementation.
     */
    public void initVerify(PublicKey theKey) throws InvalidKeyException {
        rsaSig.initVerify(theKey);
    }

    /**
     * Initializes the <CODE>RSASig</CODE> object with the appropriate
     * <CODE>Key</CODE> for signature creation.
     * 
     * @param theKey the key object to use for signing
     *
     * @exception InvalidKeyException if the key type is inconsistent 
     * with the mode or signature implementation.
     */
    public void initSign(PrivateKey theKey) throws InvalidKeyException {
        rsaSig.initSign(theKey);
    }

    /**
     * Accumulates a signature of the input data. When this method is used,
     * temporary storage of intermediate results is required. This method
     * should only be used if all the input data required for the signature
     * is not available in one byte array. The sign() or verify() method is 
     * recommended whenever possible. 
     *
     * @param inBuf the input buffer of data to be signed
     * @param inOff starting offset within the input buffer for data to
     *              be signed
     * @param inLen the byte length of data to be signed
     *
     * @exception SignatureException
     * if the signature algorithm does not pad the message and the
     * message is not block aligned
     * @see #verify(byte[], int, int)
     */ 
    public void update(byte[] inBuf, int inOff, int inLen)
	throws SignatureException {

        rsaSig.update(inBuf, inOff, inLen);
    }

    /**
     * Generates the signature of all/last input data. A call to this
     * method also resets this signature object to the state it was in
     * when previously initialized via a call to init(). That is, the
     * object is reset and available to sign another message.
     * 
     * @param sigBuf the output buffer to store signature data
     * @param sigOff starting offset within the output buffer at which
     *               to begin signature data
     * @param sigLen max length the signature can be
     *
     * @return number of bytes of signature output in sigBuf
     *
     * @exception SignatureException
     * if the signature algorithm does not pad the message and the
     * message is not block aligned
     */ 
    public int sign(byte[] sigBuf, int sigOff, int sigLen) 
	throws SignatureException {

        return rsaSig.sign(sigBuf, sigOff, sigLen);
    }
    
    /**
     * Verifies the signature of all/last input data against the passed
     * in signature. A call to this method also resets this signature 
     * object to the state it was in when previously initialized via a
     * call to init(). That is, the object is reset and available to 
     * verify another message.
     * 
     * @param sigBuf the input buffer containing signature data
     * @param sigOff starting offset within the sigBuf where signature
     *               data begins
     * @param sigLen byte length of signature data
     *
     * @return true if signature verifies, false otherwise
     *
     * @exception SignatureException
     * if the signature algorithm does not pad the message and the
     * message is not block aligned
     */ 
    public boolean verify(byte[] sigBuf, int sigOff, int sigLen)
	throws SignatureException {

        return rsaSig.verify(sigBuf, sigOff, sigLen);
    }
}
