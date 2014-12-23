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

package com.sun.midp.pki;

import com.sun.spot.security.GeneralSecurityException;
import com.sun.spot.security.InvalidAlgorithmParameterException;
import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.KeyException;
import com.sun.spot.security.Key;
import com.sun.spot.security.implementation.RSAPublicKey;
import com.sun.spot.security.implementation.ECPublicKeyImpl;
import com.sun.spot.security.implementation.ECPrivateKeyImpl;
import com.sun.spot.util.Utils;
import java.io.IOException;

/** Serializes and deserializes cryptographic keys
 * Since the key classes specified in JSR177 do not support serialization
 * this is a substitute.
 * @author: Iljya Kalai */
public class KeySerializer {
    
    /* Format version */
    private static final byte CURRENT_VERSION = 1;
        
    /* Key types */
    private static final int RSA_PUBLIC_KEY = 0;
    private static final int ECDSA_PUBLIC_KEY = 1;
    private static final int ECDSA_PRIVATE_KEY = 2;
       
    /* Serialization offsets */
    private static final int VERSION_OFFSET = 0; // byte
    private static final int KEY_TYPE_OFFSET = 1; // byte
    private static final int KEY_OFFSET = 2; // variable length
    
    /* All of the methods in this class are static */
    private KeySerializer() {}
    
    /** Deserializes a cryptographic key from a byte[]
     * @param rawKey the byte[] containing the serialized key
     * @param int the index at which the serialized key data starts
     */
    public static Key deserialize(byte[] rawKey, int offset) throws 
                                       IOException, GeneralSecurityException {
        
        byte ver = rawKey[offset + VERSION_OFFSET];
        if (ver != CURRENT_VERSION) {
            throw new IOException("Key version " + ver + 
                    " does not match serializer version " + CURRENT_VERSION);
        } else {
            byte keyType = rawKey[offset + KEY_TYPE_OFFSET];
            switch(keyType) {
                case ECDSA_PUBLIC_KEY: return parseECPublicKeyImpl(rawKey,
                                                        offset + KEY_OFFSET);
                case RSA_PUBLIC_KEY: return parseRSAPublicKey(rawKey,
                                                        offset + KEY_OFFSET);
                case ECDSA_PRIVATE_KEY: return parseECPrivateKeyImpl(rawKey,
                                                        offset + KEY_OFFSET);
                default: throw new IOException("Unrecognized key type");
            }
        }
    }
    
    /** Serializes cryptographic keys into byte[]
     * @param key the cryptographic key to serialize
     * @exception Exception if the key cannot be serialized
     */
    public static byte[] serialize(Key key) throws Exception {
        byte[] rawKey;
        byte keyType;
        Class clas = key.getClass();
        if (clas == RSAPublicKey.class) {
            rawKey = serializeRSAPublicKey((RSAPublicKey)key, KEY_OFFSET);
            keyType = RSA_PUBLIC_KEY;
        } else if (clas == ECPublicKeyImpl.class) {
            rawKey = serializeECPublicKey((ECPublicKeyImpl)key, KEY_OFFSET);
            keyType = ECDSA_PUBLIC_KEY;
        } else if (clas == ECPrivateKeyImpl.class) {
            rawKey = serializeECPrivateKey((ECPrivateKeyImpl)key, KEY_OFFSET);
            keyType = ECDSA_PRIVATE_KEY;        
        } else {
            throw new InvalidAlgorithmParameterException("Only RSA public and " +
                    "ECDSA public/private keys are supported");
        }
        rawKey[VERSION_OFFSET] = CURRENT_VERSION;
        rawKey[KEY_TYPE_OFFSET] = keyType;
        return rawKey;
    }
    
    /** Parses an RSAPublicKey
     * @param rawKey the raw key to deserialize
     * @param offset where to start parsing
     */
    private static RSAPublicKey parseRSAPublicKey(byte[] rawKey, int offset) {
        int strAddr = offset;
        int modLen = Utils.readLittleEndShort(rawKey, strAddr);
        strAddr += 2;
        byte[] mod = new byte[modLen];
        System.arraycopy(rawKey, strAddr, mod, 0, mod.length);
        strAddr += modLen;
        int expLen = Utils.readLittleEndShort(rawKey, strAddr);
        strAddr += 2;
        byte[] exp = new byte[expLen];
        System.arraycopy(rawKey, strAddr, exp, 0, exp.length);
        
        return new RSAPublicKey(mod, exp);
    }
       
    /** Parses an ECPublicKey
     * @param rawKey the raw key to deserialize
     * @param offset where to start parsing
     */
    private static ECPublicKeyImpl parseECPublicKeyImpl(byte[] rawKey,
            int offset) throws GeneralSecurityException {
        int strAddr = offset;
        int curveId = rawKey[strAddr];
        strAddr ++;
        ECPublicKeyImpl key = new ECPublicKeyImpl(curveId);
        int wlen = rawKey[strAddr];
        strAddr ++;     
        byte[] w = new byte[wlen];
        System.arraycopy(rawKey, strAddr, w, 0, w.length);
        key.setW(w, 0, w.length);
        return key;
    }

    /** Parses ECPrivateKeys
     * @param rawKey the raw key to deserialize
     * @param offset where to start parsing
     */
    private static ECPrivateKeyImpl parseECPrivateKeyImpl(byte[] rawKey,
            int offset) throws GeneralSecurityException {
        int strAddr = offset;
        int curveId = rawKey[strAddr];
        strAddr ++;
        ECPrivateKeyImpl key = new ECPrivateKeyImpl(curveId);
        int slen = rawKey[strAddr];
        strAddr ++;     
        byte[] s = new byte[slen];
        System.arraycopy(rawKey, strAddr, s, 0, s.length);
        key.setS(s, 0, s.length);
        return key;
    }
    
    /** Serializes RSAPublicKeys into the following format
     * [modLen][mod][expLen][exp]
     */
    private static byte[] serializeRSAPublicKey(RSAPublicKey key,
                                                int headerSize) {
        int modLen = key.getModulusLen();
        /* allocate enough for maximum possible exp */
        byte[] exp = new byte[modLen];
        /* get exp and exp_length */
        int expLen = key.getExponent(exp, (short)0);
        
        /* allocate enough for header, mod_length, mod, exp_length, exp */
        byte[] rawKey = new byte[headerSize + 2 + modLen + 2 + expLen];
        int strAddr = headerSize;
        /* write mod length */
        Utils.writeLittleEndShort(rawKey, strAddr, (short)modLen);
        strAddr += 2;
        /* write mod */
        key.getModulus(rawKey, (short)strAddr);
        strAddr += modLen;
        /* write exp length */
        Utils.writeLittleEndShort(rawKey, strAddr, (short)expLen);
        strAddr += 2;
        /* write exp */
        System.arraycopy(exp, 0, rawKey, strAddr, expLen);
        return rawKey;
    }
    
    /** Serializes ECPublicKeys into the following format
     */
    private static byte[] serializeECPublicKey(ECPublicKeyImpl key,
            int headerSize) throws InvalidKeyException, IOException {
        /* To store the EC Key we actually need
         * 1 + 2 * (key.getSize() + 7) >>3 bytes
         * according to the implementation of key.getW.
         * There should be a method for getting this number but there
         * doesn't seem to be.
         */
        int wLen = 1 + 2 * ((key.getSize() + 7) >>> 3);
        /* [curveId][wLength][w] */
        /* allocate space for the header, curve id, w length, w */
        byte[] rawKey = new byte[headerSize + 1 + 1 + wLen];
        int strAddr = headerSize;
        rawKey[strAddr] = (byte)key.getCurve();
        strAddr ++;
        rawKey[strAddr] = (byte)wLen;
        strAddr ++;
        if (key.getW(rawKey, strAddr) == 0) {
            throw new IOException("Serialization of EC Key failed");
        }
        return rawKey;
    }
    
    /** Serializes ECPrivateKey
     */
    private static byte[] serializeECPrivateKey(ECPrivateKeyImpl key,
            int headerSize) throws KeyException, IOException {
        byte[] s = new byte[(key.getSize() + 7) >>> 3];
        int sLen = key.getS(s, 0);
        if (sLen == 0)
            throw new IOException("Serialization of EC Key failed");
        /* [curveId][sLength][s] */
        /* allocate space for the header, curve id, s length, s */
        byte[] rawKey = new byte[headerSize + 1 + 1 + sLen];
        int strAddr = headerSize;
        rawKey[strAddr] = (byte)key.getCurve();
        strAddr ++;
        rawKey[strAddr] = (byte)sLen;
        strAddr ++;
        System.arraycopy(s, 0, rawKey, strAddr, sLen);
        return rawKey;
    }
    
    private void msg(String s) {
        System.out.println("[KeySerializer] " + s);
    }
}
