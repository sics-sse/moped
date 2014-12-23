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

package com.sun.spotx.crypto;

import com.sun.spot.security.GeneralSecurityException;
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.NoSuchAlgorithmException;
import com.sun.spot.security.PrivateKey;
import com.sun.spotx.crypto.implementation.ECDHKeyAgreement;

/**
 * The <code>KeyAgreement</code> class is the base class for key agreement
 * algorithms such as Diffie-Hellman and EC Diffie-Hellman [IEEE P1363].
 * Implementations of <code>KeyAgreement</code> algorithms must extend this
 * class and implement all the abstract methods.
 * <p> 
 *   <b>Warning</b>: This class is not part of the Security and Trust Services
 * API (SATSA) 1.0 specification.
 */
public abstract class KeyAgreement {
    
    /**
     * Elliptic curve secret value derivation primitive, Diffie-Hellman version,
     * as per [IEEE P1363]. 
     */
    public static final String ALG_EC_SVDP_DH = "EC_SVDP_DH";
    
    
    /**
     * Protected constructor.
     */
    protected KeyAgreement() {}
    
    
    /**
     * Initializes the object with the given private key.
     *
     * @param privKey the private key
     *
     * @throws InvalidKeyException if the input key type
     *       is inconsistent with the <code>KeyAgreement</code> algorithm, for
     *       example, if the <code>KeyAgreement</code> algorithm is
     *       <code>ALG_EC_SVDP_DH</code> and the key type is
     *       <code>TYPE_RSA_PRIVATE</code>, or if <code>privKey</code> is
     *       inconsistent with the implementation or if
     *       <code>privKey</code> is uninitialized.</li>
     *   </ul>
     */
    public abstract void init(PrivateKey privKey) throws InvalidKeyException;

    /**
     * Generates the secret data as per the requested algorithm using the
     * <code>PrivateKey</code> specified during initialization and the public
     * key data provided. Note that in the case of the algorithms
     * <code>ALG_EC_SVDP_DH</code> and <code>ALG_EC_SVDP_DHC</code> the public
     * key data provided should be the public elliptic curve point of the second
     * party in the protocol, specified as per ANSI X9.62. A specific
     * implementation need not support the compressed form, but must support the
     * uncompressed form of the point. 
     *
     * @param publicData buffer holding the public data of the second party
     * @param publicOffset offset into the <code>publicData</code> buffer at
     *   which the data begins
     * @param publicLength byte length of the public data
     * @param secret buffer to hold the secret output
     * @param secretOffset - offset into the <code>secret</code> array at which
     *   to start writing the secret
     *
     * @return byte length of the secret
     *
     * @throws GeneralSecurityException 
     *   <ul><li>if the
     *       <code>publicData</code> data format is incorrect, or if the
     *       <code>publicData</code> data is inconsistent with the
     *       <code>PrivateKey</code> specified during initialization.</li>
     *     <li>if this
     *       <code>KeyAgreement</code> object is not initialized.</li>
     *   </ul>
     */
    public abstract int generateSecret(byte[] publicData, int publicOffset,
            int publicLength, byte[] secret, int secretOffset)
            throws GeneralSecurityException;
    
    /**
     * Gets the <code>KeyAgreement</code> algorithm.
     * @return the algorithm code defined above
     */
    public abstract String getAlgorithm();
    
    /**
     * Creates a <code>KeyAgreement</code> object instance of the selected
     * algorithm.
     *
     * @param algorithm the desired key agreement algorithm. Valid codes listed
     *   in <code>ALG_..</code> constants above.
     *
     * @return the <code>KeyAgreement</code> object instance of the requested
     *   algorithm 
     *
     * @throws com.sun.spot.security.NoSuchAlgorithmException if the requested
     *       algorithm or shared access mode is not supported.
     */
    public static KeyAgreement getInstance(String algorithm)
            throws NoSuchAlgorithmException { 
        
        if (algorithm.equals(KeyAgreement.ALG_EC_SVDP_DH)) {
                return new ECDHKeyAgreement();
        } else {
            throw new NoSuchAlgorithmException("algorithm: "+algorithm);
        }
    }
    
}
