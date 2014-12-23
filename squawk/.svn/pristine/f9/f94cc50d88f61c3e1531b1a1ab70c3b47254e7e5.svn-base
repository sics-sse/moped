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

package com.sun.spot.security;

/**
 * This <code>Signature</code> class is used to provide applications 
 * the functionality
 * of a digital signature algorithm. Digital signatures are used for
 * authentication and integrity assurance of digital data.
 *
 * <p> The signature algorithm can be, among others, the NIST standard
 * DSA, using DSA and SHA-1. The DSA algorithm using the
 * SHA-1 message digest algorithm can be specified as <tt>SHA1withDSA</tt>.
 * In the case of RSA, there are multiple choices for the message digest
 * algorithm, so the signing algorithm could be specified as, for example,
 * <tt>MD2withRSA</tt>, <tt>MD5withRSA</tt>, or <tt>SHA1withRSA</tt>.
 * The algorithm name must be specified, as there is no default.
 *
 * When an algorithm name is specified, the system will
 * determine if there is an implementation of the algorithm requested
 * available in the environment, and if there is more than one, if
 * there is a preferred one.<p>
 *
 * <p>A <code>Signature</code> object can be used to generate and 
 * verify digital signatures.
 *
 * <p>There are three phases to the use of a <code>Signature</code>
 *  object for verifying a signature:<ol>
 *
 * <li>Initialization, with a public key, which initializes the
 * signature for  verification
 * </li>
 *
 * <li>Updating
 *
 * <p>Depending on the type of initialization, this will update the
 * bytes to be verified. </li>
 * <li> Verifying a signature on all updated bytes. </li>
 *
 * </ol>
 */ 
public abstract class Signature {

    /**
     * Protected constructor.
     */ 
    protected Signature() {
    }
    
    /**
     * Generates a <code>Signature</code> object that implements
     * the specified digest
     * algorithm.
     *
     * @param algorithm the standard name of the algorithm requested. 
     * See Appendix A in the 
     * Java Cryptography Architecture API Specification &amp; Reference 
     * for information about standard algorithm names.
     *
     * @return the new <code>Signature</code> object.
     *
     * @exception NoSuchAlgorithmException if the algorithm is
     * not available in the environment.
     */
    public static Signature getInstance(String algorithm)
	throws NoSuchAlgorithmException {

        if (algorithm == null) {
            throw new NoSuchAlgorithmException();
        }

        algorithm = algorithm.toUpperCase();

        try {
            Class sigClass;

            if (algorithm.equals("MD5WITHRSA")) {
                sigClass = Class.forName("com.sun.spot.security.implementation.RsaMd5Sig");
            } else if (algorithm.equals("SHA1WITHRSA")) {
                sigClass = Class.forName("com.sun.spot.security.implementation.RsaShaSig");
            } else if (algorithm.equals("MD5WITHECDSA"))  {
        	sigClass = Class.forName("com.sun.spot.security.implementation.ECDSAWithMD5Signature"); 
            } else if (algorithm.equals("SHA1WITHECDSA"))  {
        	sigClass = Class.forName("com.sun.spot.security.implementation.ECDSAWithSHA1Signature");  
        	
            } else {
                throw new NoSuchAlgorithmException();
            }                
            return (Signature)sigClass.newInstance();
        } catch (Throwable e) {
            throw new NoSuchAlgorithmException("Provider not found");
        }
    }
    
    /** 
     * Gets the signature algorithm.
     * 
     * <p>
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification.
     * <p>
     * 
     * @return the algorithm code defined above
     */ 
    public abstract String getAlgorithm();
    
    /**
     * Gets the byte length of the signature data.
     * 
     * <p>
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification.
     * <p>
     * 
     * @return the byte length of signature data
     */ 
    public abstract int getLength();
    
    /**
     * Initializes this object for verification. If this method is called 
     * again with a different argument, it negates the effect of this call.
     * <P />
     * @param publicKey the public key of the identity whose signature is going to be 
     * 	verified.
     *
     * @exception InvalidKeyException if the key is invalid.
     */
    public abstract void initVerify(PublicKey publicKey)
        throws InvalidKeyException;

    /**
     * Initializes the <CODE>Signature</CODE> object with the appropriate
     * <CODE>Key</CODE> for signature creation.
     * <P />
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification.
     * <p>
     * @param theKey the key object to use for signing
     *
     * @exception InvalidKeyException if the key type is inconsistent 
     * with the mode or signature implementation.
     */
    public abstract void initSign(PrivateKey theKey)
        throws InvalidKeyException;

    /**
     * Updates the data to be verified or signed, using the specified array of bytes, 
     * starting at the specified offset.
     * 
     * @param data the array of bytes.
     * @param off the offset to start from in the array of bytes.
     * @param len the number of bytes to use, starting at offset.
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly.          
     */ 
    public abstract void update(byte[] data, int off, int len)
        throws SignatureException;
    
    /**
     * Generates the signature of all/last input data. A call to this
     * method also resets this signature object to the state it was in
     * when previously initialized via a call to initSign() and the
     * message to sign given via a call to update(). 
     * That is, the object is reset and available to sign another message.
     * <P />
     * @param outbuf the output buffer to store signature data
     *
     * @return number of bytes of signature output in sigBuf
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or outbuf.length is less than the actual signature
     */ 
    public int sign(byte[] outbuf) throws SignatureException {
        return sign(outbuf, 0, outbuf.length);
    }
    
    /**
     * Generates the signature of all/last input data. A call to this
     * method also resets this signature object to the state it was in
     * when previously initialized via a call to initSign() and the
     * message to sign given via a call to update(). 
     * That is, the object is reset and available to sign another message.
     * <P />
     * <p>
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification.
     * <p>
     * @param outbuf the output buffer to store signature data
     * @param offset starting offset within the output buffer at which
     *               to begin signature data
     * @param len    max byte to write to the buffer
     *
     * @return number of bytes of signature output in sigBuf
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or len is less than the actual signature
     */
    public abstract int sign(byte[] outbuf, int offset, int len)
        throws SignatureException;
    
    /**
     * Verifies the passed-in signature. 
     * 
     * <p>A call to this method resets this signature object to the state 
     * it was in when previously initialized for verification via a
     * call to <code>initVerify(PublicKey)</code>. That is, the object is 
     * reset and available to verify another signature from the identity
     * whose public key was specified in the call to <code>initVerify</code>.
     *      
     * @param signature the signature bytes to be verified.
     *
     * @return true if the signature was verified, false if not. 
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or the passed-in signature is improperly 
     * encoded or of the wrong type, etc.
     */
    public boolean verify(byte[] signature) throws SignatureException {
        return verify(signature, 0, signature.length);
    }

    /**
     * Verifies the passed-in signature. 
     * <p>
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification.
     * <p>
     * <p>A call to this method resets this signature object to the state 
     * it was in when previously initialized for verification via a
     * call to <code>initVerify(PublicKey)</code>. That is, the object is 
     * reset and available to verify another signature from the identity
     * whose public key was specified in the call to <code>initVerify</code>.
     *
     * @param signature the input buffer containing signature data
     * @param offset starting offset within the signature where signature
     *               data begins
     * @param len byte length of signature data
     *
     * @return true if signature verifies, false if not.
     *
     * @exception SignatureException if this signature object is not 
     * initialized properly, or the passed-in signature is improperly 
     * encoded or of the wrong type, etc.
     */ 
    public abstract boolean verify(byte[] signature, int offset, int len)
        throws SignatureException;
}
