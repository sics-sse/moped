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




import com.sun.midp.pki.Utils;
import com.sun.spot.security.GeneralSecurityException;
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.MessageDigest;
import com.sun.spot.security.NoSuchAlgorithmException;
import com.sun.spot.security.PrivateKey;
import com.sun.spot.security.PublicKey;
import com.sun.spot.security.SignatureException;
import com.sun.spotx.crypto.Cipher;
import com.sun.spotx.crypto.NoSuchPaddingException;

/**
 * Implements RSA Signatures.
 */ 
final class RSASig {

    /** Current algorithm. */
    String alg;

    /** Current message digest. */
    MessageDigest md = null;

    /** Current cipher. */
    Cipher c = null;

    /** Current key. */
    RSAKey k = null;

    /** Signature prefix. */
    byte[] prefix;    

    /**
     * Constructs an RSA signature object that uses the specified
     * signature algorithm.
     *
     * @param sigPrefix Prefix for the signature
     * @param messageDigest Message digest for the signature
     *
     * @exception NoSuchAlgorithmException if RSA is
     * not available in the caller's environment.  
     */
    RSASig(byte[] sigPrefix, MessageDigest messageDigest)
            throws NoSuchAlgorithmException {
        prefix = sigPrefix;
        md = messageDigest;

        try {
            c = Cipher.getInstance("RSA");
        } catch (NoSuchPaddingException e) {
            // we used the default mode and padding this should not happen
            throw new NoSuchAlgorithmException();
        }
    }
    
    /**
     * Gets the byte-length of the signature.
     * 
     * @return the byte-length of the signature produced by this object
     */ 
    public int getLength() {
        if (k == null)
            return (short)0;
        else  // return the modulus length in bytes
            return (short)k.getModulusLen();
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
        if (!(theKey instanceof RSAPublicKey)) {
            throw new InvalidKeyException();
        }

        c.init(Cipher.DECRYPT_MODE, theKey);

        k = (RSAKey)theKey;
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
        if (!(theKey instanceof RSAPrivateKey)) {
            throw new InvalidKeyException();
        }

        c.init(Cipher.ENCRYPT_MODE, theKey);

        k = (RSAKey)theKey;
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
     * @exception SignatureException if this signature object is not 
     * initialized properly.          
     */ 
    public void update(byte[] inBuf, int inOff, int inLen)
            throws SignatureException {
        if (k == null) {
            throw new SignatureException("Illegal State");
        }

        md.update(inBuf, inOff, inLen);
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
     * @param sigLen max byte length of signature data
     *
     * @return number of bytes of signature output in sigBuf
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or len is less than the actual signature
     */ 
    public int sign(byte[] sigBuf, int sigOff, int sigLen) 
            throws SignatureException {
        if (k == null || !(k instanceof RSAPrivateKey)) {
            throw new SignatureException("Illegal State");
        }

        if (sigLen < k.getModulusLen()) {
            throw new SignatureException("Buffer too short");
        }

        byte[] data = new byte[prefix.length + md.getDigestLength()];

        // Include the OID of signing algorithm in padding
        System.arraycopy(prefix, 0, data, 0, prefix.length);
        try {
            md.digest(data, prefix.length, md.getDigestLength());

            /*
             * we can cast to a short because a private key encryption is
             * is less than the key length, which is a short.
             */
            return c.doFinal(data, 0, data.length, sigBuf, sigOff);
        } catch (GeneralSecurityException ce) {
            throw new SignatureException(ce.getMessage());
        }
    };
    
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
     * @exception SignatureException if this signature object is not 
     * initialized properly, or the passed-in signature is improperly 
     * encoded or of the wrong type, etc.
     */ 
    public boolean verify(byte[] sigBuf, int sigOff, int sigLen)
            throws SignatureException {
        if (k == null || !(k instanceof RSAPublicKey)) {
            throw new SignatureException("Illegal State");
        }

        byte[] res = null;
        int val;
        byte[] digest = new byte[md.getDigestLength()];

        try {
            md.digest(digest, 0, digest.length);
            res = new byte[k.getModulusLen()];
            val = c.doFinal(sigBuf, sigOff, sigLen, res, 0);
        } catch (IllegalArgumentException iae) {
            throw new SignatureException(iae.getMessage());
        } catch (GeneralSecurityException e) {
            System.out.println("RSASig.verify() caught " + e +
                    " returning false");
            return false;
        }

        int size = prefix.length + md.getDigestLength();

        if (val != size) {
            return false;
        }

//        System.out.println("RSASig:res:[" + res.length + "]:" +
//                Utils.hexEncode(res));
//        System.out.println("RSASig:prefix:[" + prefix.length + "]:" +
//                Utils.hexEncode(prefix));
//        System.out.println("RSASig:digest:[" + digest.length + "]:" +
//                Utils.hexEncode(digest));
        // Match the prefix corresponding to the signature algorithm
        for (int i = 0; i < prefix.length; i++) {
            if (res[i] != prefix[i]) {
                return false;
            }
        }

        for (int i = prefix.length; i < size; i++) {
            if (res[i] != digest[i - prefix.length]) {
                    return false;
            }
        }

        return true;
    }
}
