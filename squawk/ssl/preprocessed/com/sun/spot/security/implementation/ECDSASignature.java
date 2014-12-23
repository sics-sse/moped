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


import com.sun.spot.security.DigestException;
import com.sun.spot.security.GeneralSecurityException;
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.MessageDigest;
import com.sun.spot.security.NoSuchAlgorithmException;
import com.sun.spot.security.PrivateKey;
import com.sun.spot.security.PublicKey;
import com.sun.spot.security.Signature;
import com.sun.spot.security.SignatureException;
import com.sun.spot.security.implementation.ecc.ECCurve;
import com.sun.spot.security.implementation.ecc.ECPoint;
import com.sun.spot.security.implementation.ecc.FFA;
import com.sun.spot.security.implementation.ecc.PrimeField;


public class ECDSASignature extends Signature {
    
    private MessageDigest digest;
    private String algorithm;
    private ECKeyImpl key;
    private ECPrivateKeyImpl signKey;
    private ECPublicKeyImpl verifyKey;
    private byte[] digestBuf;
    
    // Some ASN.1 Syntax and Encoding Info:
    // ECDSA-Sig-Value ::= SEQUENCE {
    //     r INTEGER,
    //     s INTEGER
    // }
    // Sequence and Integer are encoded as follows:
    //     <type id><encoded length><data>
    // type id: single byte for primitives (eg. ASN_INTEGER)
    // encoded length (let b = first byte):
    //     if b < 0x80: actual length
    //     if b = 0x80: undefined length
    //     if b > 0x80: (b & 0x7f) is the number of bytes to follow, that
    //                  specify the actual length
    
    private static final byte ASN_CONSTRUCTED = 0x20;
    private static final byte ASN_INTEGER = 2;
    private static final byte ASN_SEQUENCE = 16;
    
    
    protected ECDSASignature(String algorithm) throws NoSuchAlgorithmException {
	int index=algorithm.indexOf("WITHECDSA");
	if ((index<=0)) {	    
	    create("ECDSA",algorithm.substring(index));
	}
    }
    
    protected ECDSASignature(String algorithm,String hashAlg) throws NoSuchAlgorithmException  {
	create(algorithm,hashAlg);
    }

    private void create(String algorithm, String hashAlg) throws NoSuchAlgorithmException {
	    this.algorithm = algorithm;
	        digest = MessageDigest.getInstance(hashAlg);
		digestBuf = new byte[digest.getDigestLength()];
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public int getLength() {
       if (key == null) {
	   return 0;
        }
        if (!key.isInitialized()) {
            return 0;
        }
        // The following is save for n smaller than 488 bits.
        // The sequence and both integers have a 2 byte header (type, length)
        int bytelen = (key.getECCurve().getOrder().getFFA().getBitSize() >> 3) + 1;
        return 6 + 2 * bytelen;
    }
   
    
    public int sign(byte[] outbuf, int offset, int length) throws SignatureException {
        // See: ANSI X9.62-1998, 5.3 Signature Generation
        
        if (signKey == null) {
            throw new SignatureException("Not initialized.");
        }
        if (!signKey.isInitialized()) {
            throw new SignatureException("Key not initialized.");
        }       
        
        try {
	    digest.digest(digestBuf, 0,digestBuf.length);
	} catch (DigestException e3) {
	   throw new SignatureException(e3.getMessage());
	}
        
        // We can use the PrimeField class to do all the (mod n) computations
        PrimeField field = key.getECCurve().getOrder();
        FFA ffa = field.getFFA();        
        try {
            ECPublicKeyImpl publicKey =new ECPublicKeyImpl(key.getCurve());
            ECPrivateKeyImpl privateKey = new ECPrivateKeyImpl(key.getCurve());
        
        int[] r = ffa.acquireVar();
        int[] s = ffa.acquireVar();
        
        int[] e = ffa.acquireVar();
        int[] tmp, k, d;
        
        do {
            // Generate a key pair to get the random number 'k' (private key)
            // and the x-coordinate of k*G (public key)
            try {
        	ECKeyImpl.genKeyPair(publicKey, privateKey);        	
	    } catch (NoSuchAlgorithmException e1) {		
		throw new SignatureException(e1.getMessage());
 	    }
	    tmp = publicKey.getECPoint().x;
            field.trim(r, tmp);         // r = x1 mod n

            tmp = ffa.from(ffa.acquireVar(digestBuf.length * 8), digestBuf, 0, digestBuf.length);
            field.trim(e, tmp);         // e = e mod n
            k = ffa.adjustLength((privateKey).getKeyData());
            d = ffa.adjustLength(signKey.getKeyData());

            field.multiply(s, d, r);    // s = d*r (mod n)
            field.add(s, s, e);         // s = e + d*r (mod n)
            field.invert(k, k);
            field.multiply(s, s, k);    // s = k^-1 * (e + d*r) (mod n)
            // don't panic: this is so unlikely - it will never loop
        } while (ffa.is(r, 0) || ffa.is(s, 0));
        
        
        int rLen = (ffa.bitLength(r) >> 3) + 1;
        int sLen = (ffa.bitLength(s) >> 3) + 1;
        int sequenceLen = 4 + rLen + sLen;
        
        // TODO: Improve the encoding of lengths to support sequences longer
        //       than 127 bytes (save as long as bitLength(n) < 488).
        //       See also: getLength()
        
        // Write sequence header.
        outbuf[offset++] = ASN_CONSTRUCTED | ASN_SEQUENCE;
        outbuf[offset++] = (byte)(sequenceLen);
        
        // Write first integer 'r'
        outbuf[offset++] = ASN_INTEGER;
        outbuf[offset++] = (byte)(rLen);
        ffa.toByteArray(outbuf, offset, rLen, r); offset += rLen;
        
        // Write second integer 's'
        outbuf[offset++] = ASN_INTEGER;
        outbuf[offset++] = (byte)(sLen);
        ffa.toByteArray(outbuf, offset, sLen, s);
        
        ffa.releaseVar(r);
        ffa.releaseVar(s);
        ffa.releaseVar(e);
        
        
        return (sequenceLen + 2);
        } catch (GeneralSecurityException e) {
            throw new SignatureException(e.getMessage());
        }
        
    }
    
    public void update(byte[] inBuff, int inOffset, int inLength) {
        
        digest.update(inBuff, inOffset, inLength);
    }
    
    public boolean verify(byte[] outbuf, int offset, int length)
    throws SignatureException {
        
        // See: ANSI X9.62-1998, 5.4 Signature Verification
        
        if (verifyKey == null) {
            throw new SignatureException("Not initialized.");
        }
        if (!verifyKey.isInitialized()) {
            throw new SignatureException("Key not initialized.");
        }
        
        try {
	    digest.digest(digestBuf, 0,digestBuf.length);
	} catch (DigestException e) {
	    throw new SignatureException(e.getMessage());
	}
        
        // We can use the PrimeField class to do all the (mod n) computations
        ECCurve curve = verifyKey.getECCurve();
        PrimeField field = curve.getOrder();
        FFA ffa = field.getFFA();
        
        // check the sequence header
        if ((length < 6) || (outbuf[offset++] != (ASN_CONSTRUCTED | ASN_SEQUENCE))) return false;
        int sequenceLen = (int)outbuf[offset++];
        if ((sequenceLen != length - 2) || (sequenceLen < 4)) return false;
        
        // read the first integer: 'r'
        if (outbuf[offset++] != ASN_INTEGER) return false;
        int len = (int)outbuf[offset++];
        sequenceLen -= (2 + len);
        if (sequenceLen < 2) return false;
        int[] r = ffa.from(outbuf, offset, len); offset += len;
        
        // read the second integer: 's'
        if (outbuf[offset++] != ASN_INTEGER) return false;
        len = (int)outbuf[offset++];
        sequenceLen -= (2 + len);
        if (sequenceLen != 0) return false;
        int[] s = ffa.from(outbuf, offset, len);
        
        // 'r' and 's' must be in the interval [1..n-1]
        int[] n = field.getP();
        if (ffa.is(r, 0) || ffa.is(s, 0) || (ffa.cmp(r, n) >= 0)
            || (ffa.cmp(s, n) >= 0)) {
            return false;
        }
        
        int[] u1 = ffa.acquireVar();
        int[] u2 = ffa.acquireVar();
        
        int[] tmp = ffa.from(ffa.acquireVar(digestBuf.length * 8), digestBuf, 0, digestBuf.length);
        field.trim(u1, tmp);        // u1 = e mod n
        
        field.invert(s, s);
        field.multiply(u1, u1, s);  // u1 = (e * s^-1) mod n
        field.multiply(u2, r, s);   // u2 = (r * s^-1) mod n
        
        ECPoint G = curve.getGenerator().clonePoint();
        ECPoint Q = verifyKey.getECPoint();
        
        curve.multiplySum(G, u1, Q, u2);    // G = u1 * G + u2 * Q;
        
        field.trim(s, G.x);         // s = x1 mod n
        
        boolean verified = (ffa.cmp(r, s) == 0);
        
        ffa.releaseVar(r);
        ffa.releaseVar(s);
        ffa.releaseVar(u1);
        ffa.releaseVar(u2);
        G.release();
        Q.release();
        
        return verified;
    }


    public void initSign(PrivateKey theKey) throws InvalidKeyException {
	try {
                signKey = (ECPrivateKeyImpl)theKey;
                verifyKey = null;
        } catch (ClassCastException e) {
           throw new InvalidKeyException();
        }
        key = (ECKeyImpl)theKey;

        // Insert code here to add support for F2^m. The rest of this class
        // should be independent of the of the curve type.
	
    }


    
    public void initVerify(PublicKey theKey) throws InvalidKeyException {
	try {
    
                verifyKey = (ECPublicKeyImpl)theKey;
                signKey = null;
        } catch (ClassCastException e) {
            throw new InvalidKeyException();
        }
        key = (ECKeyImpl)theKey;

        // Insert code here to add support for F2^m. The rest of this class
        // should be independent of the of the curve type.
	
    }
   
}
