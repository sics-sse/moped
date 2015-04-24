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
 * Implements ARCFOUR encryption/decryption.
 * 
 * $Log$
 * Revision 1.1  2006/09/15 21:41:48  christian
 * Sun Spot CryptoLibrary.
 * Implements the SATSA specification.
 *
 * Revision 1.1  2006/09/15 21:36:53  christian
 * Changed to SATSA interfaces, javadoc added,
 * stripped down BigInteger classes.
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
 * Revision 1.2  2000/03/31 02:35:13  vgupta
 * Removed those checks from doFinal that are duplicated in update.
 *
 * Revision 1.1  2000/03/29 02:55:17  vgupta
 * Initial revision
 *
 */

package com.sun.spotx.crypto.implementation;


import com.sun.spot.security.InvalidAlgorithmParameterException;
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.Key;
import com.sun.spot.security.spec.AlgorithmParameterSpec;
import com.sun.spotx.crypto.BadPaddingException;
import com.sun.spotx.crypto.Cipher;
import com.sun.spotx.crypto.CipherSpi;
import com.sun.spotx.crypto.IllegalBlockSizeException;
import com.sun.spotx.crypto.NoSuchPaddingException;
import com.sun.spotx.crypto.ShortBufferException;
import com.sun.spotx.crypto.spec.SecretKeySpec;



/**
 * This class implements the ARCFOUR stream cipher
 */
public final class Alg2 extends CipherSpi {
    protected final static String ALG2 = "Alg2";
    
    private int mode;
    private Key key;
    private boolean initOk;
    private boolean needsReset;
    private byte[] S;
    private int ii, jj;
    
    public Alg2() {
        S = new byte[256];
    }
    
    
    protected void engineSetChainingModeAndPadding(String mode, String padding) throws NoSuchPaddingException {
	if (!(mode.equals("") || mode.equals("NONE"))) {
            throw new IllegalArgumentException();
        }

        // NOPADDING is not an option.
        if (!(padding.equals("") || padding.equals("NOPADDING"))) {
            throw new NoSuchPaddingException();
        }
	
    }
/*
    public byte getAlgorithm() {
	return Cipher.ALG_ARCFOUR;
    }*/

    /*
     * Initializes the cipher S based on the key
     */

    protected void engineInit(int theMode, Key theKey, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
	if (params!=null) 
	    throw new InvalidAlgorithmParameterException(ALG2+" does not support AlgorithmParameterSpec. Must be null"); 
	if (!(theKey instanceof SecretKeySpec)) {
	    throw new InvalidKeyException();
	}
	 if   ((theMode != Cipher.ENCRYPT_MODE) && 
	     (theMode != Cipher.DECRYPT_MODE)) {
	    throw new InvalidAlgorithmParameterException();	    
	    //CryptoException.throwIt(CryptoException.ILLEGAL_VALUE);
	}
        /*if (!theKey.isInitialized()) {
	    CryptoException.throwIt(CryptoException.UNINITIALIZED_KEY);
        }*/
	
	this.mode = theMode;
        this.key = theKey;
        
	byte[] K = ((SecretKeySpec)theKey).getEncoded();
        int kLen = ((SecretKeySpec)theKey).getEncoded().length;
	
	/* Initialize S */
	for (int i = 0; i < 256; i++) {
            S[i] = (byte) i;
        }
	
	int j = 0;
        int k = 0;
	byte temp;
	for (int i = 0; i < 256; i++) {
	    j = (j + ((S[i] + K[k]) & 0xff)) & 0xff;
	    temp = S[i];
	    S[i] = S[j];
	    S[j] = temp;
            if (++k >= kLen) k = 0;
	}
        
        jj = ii = 0;
        
        initOk = true;
        needsReset = false;
    }
    
    private int transform(byte[] inBuf, int inOff, int inLen,
			   byte[] outBuf, int outOff) {
	byte tmp;
	int t;
	byte kt;
	
	// We don't use loop unrolling since a typical processor for
	// the Kjava platform is unlikely to benefit from it. Conserving
	// memory is likely to be more important
	for (int i = 0; i < inLen; i++) {
	    ii = (ii + 1) & 0xff;
	    tmp = S[ii];
	    jj = (jj + (tmp & 0xff)) & 0xff;
	    S[ii] = S[jj];
	    S[jj] = tmp;
	    t = (S[ii] + tmp) & 0xff;
	    kt = S[t];
	    outBuf[outOff + i] = (byte) (inBuf[inOff + i] ^ kt);
	}

	return inLen;
    }

    protected int engineUpdate(byte[] inBuf, int inOff, int inLen, 
			byte[] outBuf, int outOff) throws IllegalStateException, ShortBufferException {
	if ((inLen < 0) || 
	    (inOff + inLen > inBuf.length) ||
	    (outOff + inLen > outBuf.length))
	    		throw new ShortBufferException();
			//CryptoException.throwIt(CryptoException.ILLEGAL_USE);
	
	if (!initOk) {
	    throw new IllegalStateException("ARC4  not initialized correctly.");
	    //CryptoException.throwIt(CryptoException.INVALID_INIT);
        }
        
        if (needsReset) {
            try {
        	engineInit(mode,key, null);    // reset
            } catch (Exception e) {
        	throw new IllegalStateException("init failed. "+e);
            }
            
        }
	
	int val = transform(inBuf, inOff, inLen, outBuf, outOff);
	return val;
    }
    
    protected int engineDoFinal(byte[] inBuf, int inOff, int inLen, 
			 byte[] outBuf, int outOff) throws	
			 IllegalStateException, ShortBufferException,
			 IllegalBlockSizeException, BadPaddingException{
	int val = engineUpdate(inBuf, inOff, inLen, outBuf, outOff);
        // The specification wants us to reset this object, but key expansion
        // is expensive and it is possible that this object is not used again.
        // So we delay this reset to the next usage.
        needsReset = true;
	return val;
    }


    protected String engineGetAlgorithm() {
	return ALG2;
    }


    protected byte[] engineGetIv() {
	//RC4 doesn't use IV. 
	return null;
    }    



    
}
