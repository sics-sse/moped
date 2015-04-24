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
 * This MessageDigest class provides applications the functionality of a
 * message digest algorithm, such as MD5 or SHA.
 * Message digests are secure one-way hash functions that take arbitrary-sized
 * data and output a fixed-length hash value.
 *
 * <p>A <code>MessageDigest</code> object starts out initialized. The data is 
 * processed through it using the <code>update</code>
 * method. At any point {@link #reset() reset} can be called
 * to reset the digest. Once all the data to be updated has been
 * updated, the <code>digest</code> method should 
 * be called to complete the hash computation.
 *
 * <p>The <code>digest</code> method can be called once for a given number 
 * of updates. After <code>digest</code> has been called, 
 * the <code>MessageDigest</code>
 * object is reset to its initialized state.
 */
public abstract class MessageDigest {
    /** Protected constructor. */
    protected MessageDigest() {
    }
    
    /**
     * Generates a <code>MessageDigest</code> object that implements
     * the specified digest
     * algorithm. 
     *
     * @param algorithm the name of the algorithm requested. 
     * See Appendix A in the 
     * Java Cryptography Architecture API Specification &amp; Reference
     * for information about standard algorithm names.
     *
     * @return a MessageDigest object implementing the specified
     * algorithm. 
     * @exception NoSuchAlgorithmException if the algorithm is
     * not available in the caller's environment.  
     */
    public static MessageDigest getInstance(String algorithm) 
            throws NoSuchAlgorithmException {

	if (algorithm == null) {
            throw new NoSuchAlgorithmException();
        }

        algorithm = algorithm.toUpperCase();

        try {
            Class sigClass;

            if (algorithm.equals("MD5")) {
                sigClass = Class.forName("com.sun.spot.security.implementation.MD5");
            } else if (algorithm.equals("SHA-1") || (algorithm.equals("SHA"))) {
                sigClass = Class.forName("com.sun.spot.security.implementation.SHA");             	
            } else {
                throw new NoSuchAlgorithmException(algorithm+ " not supported.");
            }                
            return (MessageDigest)sigClass.newInstance();
        } catch (Throwable e) {
            throw new NoSuchAlgorithmException("Provider not found");
        }
    }
    
    /** 
     * Gets the message digest algorithm.
     * <p>
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification.
     * @return algorithm implemented by this MessageDigest object
     */
    public abstract String getAlgorithm();
    
    /** 
     * Gets the length (in bytes) of the hash.
     * <p>
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification.
     * @return byte-length of the hash produced by this object
     */
    public abstract int getDigestLength();
    
    /**
     * Updates the digest using the specified array of bytes, starting at the 
     * specified offset. 
     * @param input the array of bytes.
     * @param offset the offset to start from in the array of bytes.
     * @param len the number of bytes to use, starting at <code>offset</code>. 
     */
    public abstract void update(byte[] input, int offset, int len);
    
    /**
     * Completes the hash computation by performing final operations
     * such as padding. The digest is reset after this call is made.
     *
     * @param buf output buffer for the computed digest
     *
     * @param offset offset into the output buffer to begin storing the digest
     *
     * @param len number of bytes within buf allotted for the digest
     *
     * @return the number of bytes placed into <code>buf</code>
     * 
     * @exception DigestException if an error occurs.
     */
    public abstract int digest(byte[] buf, int offset, int len)
        throws DigestException;

    /** 
     * Resets the MessageDigest to the initial state for further use.
     */
    public abstract void reset();
     
    /** 
     * Clones the MessageDigest object.
     * <p>
     * <b>Warning</b>: This method is not part of the Security and Trust Services
     * API (SATSA) 1.0 specification. 
     * @return a clone of this object
     */
    public abstract Object clone();
}
