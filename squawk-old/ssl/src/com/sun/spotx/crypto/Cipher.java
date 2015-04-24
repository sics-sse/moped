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

package com.sun.spotx.crypto;

import com.sun.spot.security.InvalidAlgorithmParameterException;
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.Key;
import com.sun.spot.security.NoSuchAlgorithmException;
import com.sun.spot.security.spec.AlgorithmParameterSpec;

/**
 * This class provides the functionality of a cryptographic cipher for 
 * encryption and decryption. It forms the core of the Java Cryptographic
 * Extension (JCE) framework. 
 * <p>In order to create a Cipher object, the application calls the Cipher’s
 * <code>getInstance</code> method, and passes the name of the requested 
 * <i>transformation</i> to it.
 * <p>A <i>transformation</i> is a string that describes the operation 
 * (or set of operations) to be performed on the given input, to produce 
 * some output. A transformation always includes the name of a cryptographic 
 * algorithm (e.g., <i>DES</i>), and may be followed by a feedback mode and
 * padding scheme.
 * <p>
 * A transformation is of the form:
 * <ul>
 * <li><i>“algorithm/mode/padding”</i> or</li>
 * <li><i>“algorithm”</i></li>
 * </ul>
 * <p>
 * (in the latter case, provider-specific default values for the mode and 
 * padding scheme are used). For example, the following is a 
 * valid transformation:
 * <p><code>
 * 	Cipher c = Cipher.getInstance(“DES/CBC/PKCS5Padding”);
 * </code>
 * <p>When requesting a block cipher in stream cipher mode (e.g., 
 * <code>DES</code> in <code>CFB</code> or <code>OFB</code> mode), the 
 * user may optionally specify the number of bits to be 
 * processed at a time, by appending this number to the mode name as shown in 
 * the <i>“DES/CFB8/NoPadding”</i> and <i>“DES/OFB32/PKCS5Padding”</i> 
 * transformations. If no such number is specified, a provider-specific
 * default is used.
 * 
 * Implements an abstract class that generalizes all ciphers. It is
 * modelled after javax.crypto.Cipher.
 */ 
public class Cipher {
    /**
     * Flag to indicate the current cipher algorithm is unknown.
     */
    protected static final int MODE_UNINITIALIZED = 0;
    
    /** Constant used to initialize cipher to encryption mode. */
    public static final int ENCRYPT_MODE      = 1;

    /** Constant used to initialize cipher to decryption mode. */
    public static final int DECRYPT_MODE      = 2;

    private CipherSpi cipherSpi;

    
    private Cipher(CipherSpi cipherSpi) {
	this.cipherSpi=cipherSpi;
    }
    
    
    /**
     * Generates a <code>Cipher</code> object that implements the specified
     * transformation.
     *
     * @param transformation the name of the transformation, e.g.,
     * <i>DES/CBC/PKCS5Padding</i>.
     * See Appendix A in the
     * <a href="../../../guide/security/jce/JCERefGuide.html#AppA">
     * Java Cryptography Extension Reference Guide</a>
     * for information about standard transformation names.
     *
     * @return a cipher that implements the requested transformation
     *
     * @exception NoSuchAlgorithmException if the specified transformation is
     * not available
     * @exception NoSuchPaddingException if <code>transformation</code>
     * contains a padding scheme that is not available.
     */
    public static final Cipher getInstance(String transformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException {

        CipherSpi cipherSpi = null;
        String alg = ("" + transformation).toUpperCase().trim();
        String chainingMode = "";
        String padding = "";

        if (alg.indexOf("/") != -1) {
            int first = alg.indexOf("/");
            int second = alg.indexOf("/", first + 1);
            if (second == alg.lastIndexOf('/')) {
                chainingMode = alg.substring(first + 1, second).trim();
                padding = alg.substring(second + 1).trim();
                alg = alg.substring(0, first).trim();
            }
        }

        // We have the generic equivalent of RC4: ARC4 or ARCFOUR.
        if (alg.equals("RC4") || alg.equals("ARCFOUR")|| alg.equals("ARC4")) {
            //alg = "ARC4";
            //In the spot crypto lib the algorithm is called Alg2
            alg = "Alg2";
        }
        
        if (alg.equals("AES")) {           
            //In the spot crypto lib the algorithm is called Alg2
            alg = "Alg1";
        }

        try {
            Class cipherClass;

            cipherClass = Class.forName("com.sun.spotx.crypto.implementation." + alg);
            cipherSpi = (CipherSpi)cipherClass.newInstance();
        } catch (Throwable t) {
            throw new NoSuchAlgorithmException(transformation);
        }

        cipherSpi.engineSetChainingModeAndPadding(chainingMode, padding);
        
        return new Cipher(cipherSpi);
    }

    /**
     * Initializes this cipher with a key.
     *
     * <p>The cipher is initialized for one of the following operations:
     * encryption, decryption,  depending
     * on the value of <code>opmode</code>.
     *
     * <p>If this cipher requires any algorithm parameters that cannot be
     * derived from the given <code>key</code>, the underlying cipher
     * implementation is supposed to generate the required parameters itself
     * (using provider-specific default or random values) if it is being
     * initialized for encryption, and raise an
     * <code>InvalidKeyException</code> if it is being
     * initialized for decryption.
     *
     * <p>Note that when a Cipher object is initialized, it loses all
     * previously-acquired state. In other words, initializing a Cipher is
     * equivalent to creating a new instance of that Cipher and initializing
     * it.
     *
     * @param opmode the operation mode of this cipher (this is one of
     * the following:
     * <code>ENCRYPT_MODE</code> or <code>DECRYPT_MODE</code>)
     * @param key the key
     *
     * @exception InvalidKeyException if the given key is inappropriate for
     * initializing this cipher, or if this cipher is being initialized for
     * decryption and requires algorithm parameters that cannot be
     * determined from the given key, or if the given key has a keysize that
     * exceeds the maximum allowable keysize.
     */
    public final void init(int opmode, Key key) throws InvalidKeyException {
        try {
            init(opmode, key, null);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException();
        }
    }

    /**
     * Initializes this cipher with a key and a set of algorithm
     * parameters.
     *
     * <p>The cipher is initialized for one of the following  operations:
     * encryption or decryption depending
     * on the value of <code>opmode</code>.
     *
     * <p>If this cipher requires any algorithm parameters and
     * <code>params</code> is null, the underlying cipher implementation is
     * supposed to generate the required parameters itself (using
     * provider-specific default or random values) if it is being
     * initialized for encryption, and raise an
     * <code>InvalidAlgorithmParameterException</code> if it is being
     * initialized for decryption.
     *
     * <p>Note that when a Cipher object is initialized, it loses all
     * previously-acquired state. In other words, initializing a Cipher is
     * equivalent to creating a new instance of that Cipher and initializing
     * it.
     *
     * @param opmode the operation mode of this cipher (this is one of the
     * following:
     * <code>ENCRYPT_MODE</code> or <code>DECRYPT_MODE</code>)
     * @param key the encryption key
     * @param params the algorithm parameters
     *
     * @exception InvalidKeyException if the given key is inappropriate for
     * initializing this cipher, or its keysize exceeds the maximum allowable
     * keysize.
     * @exception InvalidAlgorithmParameterException if the given algorithm
     * parameters are inappropriate for this cipher,
     * or this cipher is being initialized for decryption and requires
     * algorithm parameters and <code>params</code> is null, or the given
     * algorithm parameters imply a cryptographic strength that would exceed
     * the legal limits.
     */
    public final void init(int opmode, Key key, AlgorithmParameterSpec params) 
        throws InvalidKeyException, InvalidAlgorithmParameterException {
	cipherSpi.engineInit(opmode, key, params);
    }

    /**
     * Continues a multiple-part encryption or decryption operation
     * (depending on how this cipher was initialized), processing another data
     * part.
     *
     * <p>The first <code>inputLen</code> bytes in the <code>input</code>
     * buffer, starting at <code>inputOffset</code> inclusive, are processed,
     * and the result is stored in the <code>output</code> buffer, starting at
     * <code>outputOffset</code> inclusive.
     *
     * <p>If the <code>output</code> buffer is too small to hold the result,
     * a <code>ShortBufferException</code> is thrown. In this case, repeat this
     * call with a larger output buffer.
     *
     * <p>If <code>inputLen</code> is zero, this method returns
     * a length of zero.
     *
     * <p>Note: this method should be copy-safe, which means the
     * <code>input</code> and <code>output</code> buffers can reference
     * the same byte array and no unprocessed input data is overwritten
     * when the result is copied into the output buffer.
     *
     * @param input the input buffer
     * @param inputOffset the offset in <code>input</code> where the input
     * starts
     * @param inputLen the input length
     * @param output the buffer for the result
     * @param outputOffset the offset in <code>output</code> where the result
     * is stored
     *
     * @return the number of bytes stored in <code>output</code>
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception ShortBufferException if the given output buffer is too small
     * to hold the result
     */
    public final int update(byte[] input, int inputOffset, int inputLen,
                               byte[] output, int outputOffset)
        throws IllegalStateException, ShortBufferException {
	return cipherSpi.engineUpdate(input, inputOffset, inputLen, output, outputOffset);
    }

    /**
     * Encrypts or decrypts data in a single-part operation, or finishes a
     * multiple-part operation. The data is encrypted or decrypted,
     * depending on how this cipher was initialized.
     *
     * <p>The first <code>inputLen</code> bytes in the <code>input</code>
     * buffer, starting at <code>inputOffset</code> inclusive, and any input
     * bytes that may have been buffered during a previous
     * <code>update</code> operation, are processed, with padding
     * (if requested) being applied.
     * The result is stored in the <code>output</code> buffer, starting at
     * <code>outputOffset</code> inclusive.
     *
     * <p>If the <code>output</code> buffer is too small to hold the result,
     * a <code>ShortBufferException</code> is thrown. In this case, repeat this
     * call with a larger output buffer.
     *
     * <p>Upon finishing, this method resets this cipher object to the state
     * it was in when previously initialized via a call to <code>init</code>.
     * That is, the object is reset and available to encrypt or decrypt
     * (depending on the operation mode that was specified in the call to
     * <code>init</code>) more data.
     *
     * <p>Note: if any exception is thrown, this cipher object may need to
     * be reset before it can be used again.
     *
     * <p>Note: this method should be copy-safe, which means the
     * <code>input</code> and <code>output</code> buffers can reference
     * the same byte array and no unprocessed input data is overwritten
     * when the result is copied into the output buffer.
     *
     * @param input the input buffer
     * @param inputOffset the offset in <code>input</code> where the input
     * starts
     * @param inputLen the input length
     * @param output the buffer for the result
     * @param outputOffset the offset in <code>output</code> where the result
     * is stored
     *
     * @return the number of bytes stored in <code>output</code>
     *
     * @exception IllegalStateException if this cipher is in a wrong state
     * (e.g., has not been initialized)
     * @exception IllegalBlockSizeException if this cipher is a block cipher,
     * no padding has been requested (only in encryption mode), and the total
     * input length of the data processed by this cipher is not a multiple of
     * block size
     * @exception ShortBufferException if the given output buffer is too small
     * to hold the result
     * @exception BadPaddingException if this cipher is in decryption mode,
     * and (un)padding has been requested, but the decrypted data is not
     * bounded by the appropriate padding bytes
     */
    public final int doFinal(byte[] input, int inputOffset, int inputLen,
        byte[] output, int outputOffset)
        throws IllegalStateException, ShortBufferException,
               IllegalBlockSizeException, BadPaddingException {
	return cipherSpi.engineDoFinal(input, inputOffset, inputLen, output, outputOffset);
    }

    /**
     * Returns the initialization vector (IV) in a new buffer.
     * This is useful in the case where a random IV was created.
     * @return the initialization vector in a new buffer,
     * or <code>null</code> if the underlying algorithm does
     * not use an IV, or if the IV has not yet been set.
     */
    public final byte[] getIV() {
        return cipherSpi.engineGetIv();
    }
}
