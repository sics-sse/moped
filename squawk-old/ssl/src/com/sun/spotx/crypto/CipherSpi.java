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
 * CipherSpi is the interface for implementations of Cipher's
 * It's similar to JCE while it's not part of the SATSA specification.
 * 
 * 
 * 
 *
 */
public abstract class CipherSpi {
    protected abstract void engineInit(int opmode, Key key, AlgorithmParameterSpec params)
    	throws InvalidKeyException, InvalidAlgorithmParameterException;

   
    
    protected abstract int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws IllegalStateException, ShortBufferException;
    
    protected abstract int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws IllegalStateException, ShortBufferException, IllegalBlockSizeException, BadPaddingException;
    
    protected abstract String engineGetAlgorithm();
    protected abstract byte[] engineGetIv();
    
    
    /**
     * Called by the factory method to set the mode and padding parameters.
     * Need because Class.newInstance does not take args.
     *
     * @param mode the chaining mode parsed from the transformation parameter
     *             of getInstance and upper cased
     * @param padding the paddinge parsed from the transformation parameter of
     *                getInstance and upper cased
     *
     * @exception NoSuchPaddingException if <code>transformation</code>
     * contains a padding scheme that is not available.
     * @throws NoSuchAlgorithmException 
     */    
    protected abstract void engineSetChainingModeAndPadding(String mode, String padding) throws NoSuchPaddingException, NoSuchAlgorithmException;
    
}
