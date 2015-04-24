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

package com.sun.midp.ssl;

import java.io.IOException;
import java.util.Vector;

import com.sun.spot.peripheral.TrustManager; // SPOT specific

import javax.microedition.pki.CertificateException;
import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;
import com.sun.midp.pki.CertStore;
import com.sun.midp.pki.Utils;
import com.sun.midp.pki.X509Certificate;
import com.sun.spot.security.DigestException;
import com.sun.spot.security.MessageDigest;
import com.sun.spot.security.NoSuchAlgorithmException;
import com.sun.spot.security.PrivateKey;
import com.sun.spot.security.PublicKey;
import com.sun.spot.security.implementation.RSAPublicKey;
import com.sun.spot.security.implementation.RSAPrivateKey;
import com.sun.spot.security.implementation.SecureRandom;
import com.sun.spot.security.implementation.ecc.ECCurve;
import com.sun.spot.security.implementation.ECPrivateKeyImpl;
import com.sun.spot.security.implementation.ECPublicKeyImpl;
import com.sun.spot.security.implementation.ECKeyImpl;
import com.sun.spotx.crypto.KeyAgreement;
import com.sun.spotx.crypto.Cipher;

/**
 * This class implements the SSL handshake protocol which is responsible
 * for negotiating security parameters used by the record layer.
 * Currently, only client-side functionality is implemented.
 */
// visible only within this package
class Handshake {
	public String[] verificationChain = null;

    private TrustManager trustManager;
    
    static final byte SUPPORTED_CIPHERS = 7;
    /** ARCFOUR_128_SHA (0x0005). */
    static final short ARCFOUR_128_SHA = (short) 0x0005;
    /** ARCFOUR_128_MD5 (0x0004). */
    static final short ARCFOUR_128_MD5 = (short) 0x0004;
    /**  ARCFOUR_40_MD5 (0x0003). */
    static final short ARCFOUR_40_MD5  = (short) 0x0003;
    /** ECC cipher suites */
    static final short ECDH_ECDSA_RC4_128_SHA  = (short) 0xc002;
    static final short ECDHE_ECDSA_RC4_128_SHA = (short) 0xc007;
    static final short ECDH_RSA_RC4_128_SHA    = (short) 0xc00c;
    static final short ECDHE_RSA_RC4_128_SHA   = (short) 0xc011;

    /**
     * This contains the cipher suite encoding length in the first
     * two bytes, followed by an encoding of the cipher suites followed
     * by the compression suite length in one byte and the compression
     * suite. For now, we only propose the two most commonly used
     * cipher suites.
     */
    private static final byte[] SSL3_SUITES_COMP = {
        // Use this to propose 128-bit encryption as preferred
        0x00, 0x06, 0x00, (byte) ARCFOUR_128_SHA, 0x00, (byte) ARCFOUR_128_MD5,
        0x00, (byte) ARCFOUR_40_MD5, 0x01, 0x00
                // Use this to propose 40-bit encryption as preferred
                // 0x00, 0x06, 0x00, ARCFOUR_40_MD5, 0x00, ARCFOUR_128_RSA,
                // 0x00, ARCFOUR_128_SHA, 0x01, 0x00
    };
    
    /**
     * When using TLS, we propose additional ECC ciphers and include the 
     * required TLS extensions. 
     * TODO: Adjust this when we support additional ECC ciphers or curves.
     */
    private static final byte[] TLS_SUITES_COMP_EXT = {
        0x00, 0x06, // Cipher list len followed by cipher list
        (byte) 0xc0, 0x02, // ECDH_ECDSA_RC4_128_SHA
        0x00, (byte) ARCFOUR_128_SHA, 
        0x00, (byte) ARCFOUR_128_MD5,
        0x01, 0x00,  // Compression
        0x00, 0x0e,  // Extensions length
        0x00, 0x0a,  // Supported curves extension
        0x00, 0x04,  // extension data len
        0x00, 0x02,  // curve list len
        0x00, 0x10,  // secp160r1
        0x00, 0x0b,  // Supported point formats extension
        0x00, 0x02,  // extension data len
        0x01,        // supported point format list len
        0x00         // Uncompressed
    };
    
    private byte[] suitesCompEtc = null;
    
    /**
     * Each handshake message has a four-byte header containing
     * the type (1 byte) and length (3 byte).
     */
    private static final byte HDR_SIZE = 4;
    
    // Handshake message types
    /** Hello Request (0). */
    private static final byte HELLO_REQ = 0;
    /** Client Hello (1). */
    private static final byte C_HELLO   = 1;
    /** Server Hello (2). */
    private static final byte S_HELLO   = 2;
    /** Certificate (11). */
    private static final byte CERT      = 11;
    /** Server Key Exchange (12). */
    private static final byte S_KEYEXCH = 12;
    /** Certificate Request (13). */
    private static final byte CERT_REQ  = 13;
    /** Server Hello Done (14). */
    private static final byte S_DONE    = 14;
    /** Certificate Verify (15). */
    private static final byte CERT_VRFY = 15;
    /** Client Key Exchange (16). */
    private static final byte C_KEYEXCH = 16;
    /** Finished (20). */
    private static final byte FINISH    = 20;
    
    // Number of bytes in an MD5/SHA digest
    /** Number of bytes in an MD5 Digest (16). */
    private static final byte MD5_SIZE = 16;
    /** Number of bytes in an SHA Digest (20). */
    private static final byte SHA_SIZE = 20;
    
    /**
     * The length of the finished message is explicitly
     * set in sndFinished since it depends on the negotiated
     * version of SSL (for v3 this is 36 or 0x24 but for
     * TLSv1 that is 12 or 0x0c)
     */
    private static final byte[] FINISH_PREFIX = {
        FINISH, 0x00, 0x00, 0x00
    };
    
    /** Handle to trusted certificate store. */
    private CertStore certStore = null;
    /** Current record to process. */
    private Record rec;
    /** Peer host name . */
    private String peerHost;
    /** Peer port number. */
    private int peerPort;
    /** Local random number seed. */
    private SecureRandom rnd = null;
    /** Previous session context to this server host and port, 
     * if there was one. 
     */
    private Session cSession = null;
    /** Previous session context for client-specified Id. */
    private Session sSession = null;
    /** Session id returned by server. */
    private byte[] sSessionId = null;
    /** Client random number. */
    private byte[] crand = null;
    /** Server random number. */
    private byte[] srand = null;
    
    /** Proposed SSL version. */
    private byte ver;
    /** Role (always CLIENT for now). */
    private byte role;
    /** Negotiated cipher suite. */
    short negSuite;
    /** Name of negotiated cipher suite. */
    String negSuiteName;
    /** Flag to indicate certificate request received. */
    private byte gotCertReq = 0;
    /** Pre-master secret. */
    private byte[] preMaster = null;
    /** Master secret. */
    private byte[] master = null;
    /**
     * Public key used to encrypt the appropriate
     * usage of sKey certs in chain.
     */
    private RSAPublicKey eKey = null;

    /**
     * For server-side code, sPrivKey contains the server's
     * private key corresponding to the public key in the certificate.
     */
    private PrivateKey sPrivKey = null;

    /*
     * These are used in the handling of ECC extensions.
     */
    private static final byte[] pointFormatExt = 
           {0x00, 0x0B, 0x00, 0x02, 0x01, 0x00};
    boolean curveSupported = true;
    boolean uncompressedOk = true;
    boolean pointFormatNegotiated = false;
    byte ourCurve = 0x10; // XXX: this should be read from the cert

    /** 
     * Client's public and private key pair.
     */
    private PublicKey cPubKey = null;
    private PrivateKey cPrivKey = null;
    private int curveType = -1;

    // we also need a temporary place to store the server certificate
    // in parseChain so it can be examined later rcvSrvrKeyExch() for
    // keyUsage checks and the parent connection.
    /** Temporary storage for server certificate. */
    X509Certificate sCert = null;

    /** Certificate to be squirted in the Certificate message
     * when negotiating a SSL/TLS handshake as a server. 
     * TODO: Revisit this when we need to support certificate chains.
     */
    private byte[] sCertBytes = null;
    /* We don't use RSA certs but this is a place holder in case we want to
     * use them. They are supported. */
    private byte[] sRSACertBytes = null;
    private byte[] sRSAPrivateKeyMod = null;
    private byte[] sRSAPrivateKeyExp = null;

	/* Keep these around so we can use the code on platforms other than 
	 * the SPOTs, e.g. in a Java SE environment as a small standalone 
	 * SSL library
 	 */
	/*
    private byte[] sECCCertBytes = { // 222 bytes 
        (byte) 0x30, (byte) 0x81, (byte) 0xDB, (byte) 0x30, (byte) 0x81, (byte) 0x9B, (byte) 0x02, (byte) 0x01, (byte) 0x01, (byte) 0x30, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x2A, (byte) 0x86, (byte) 0x48,
        (byte) 0xCE, (byte) 0x3D, (byte) 0x04, (byte) 0x01, (byte) 0x30, (byte) 0x11, (byte) 0x31, (byte) 0x0F, (byte) 0x30, (byte) 0x0D, (byte) 0x06, (byte) 0x03, (byte) 0x55, (byte) 0x04, (byte) 0x03, (byte) 0x13,
        (byte) 0x06, (byte) 0x53, (byte) 0x55, (byte) 0x4E, (byte) 0x57, (byte) 0x2D, (byte) 0x45, (byte) 0x30, (byte) 0x1E, (byte) 0x17, (byte) 0x0D, (byte) 0x30, (byte) 0x34, (byte) 0x30, (byte) 0x38, (byte) 0x32,
        (byte) 0x30, (byte) 0x30, (byte) 0x35, (byte) 0x33, (byte) 0x33, (byte) 0x30, (byte) 0x33, (byte) 0x5A, (byte) 0x17, (byte) 0x0D, (byte) 0x30, (byte) 0x38, (byte) 0x30, (byte) 0x39, (byte) 0x32, (byte) 0x38,
        (byte) 0x30, (byte) 0x35, (byte) 0x33, (byte) 0x33, (byte) 0x30, (byte) 0x33, (byte) 0x5A, (byte) 0x30, (byte) 0x18, (byte) 0x31, (byte) 0x16, (byte) 0x30, (byte) 0x14, (byte) 0x06, (byte) 0x03, (byte) 0x55,
        (byte) 0x04, (byte) 0x03, (byte) 0x13, (byte) 0x0D, (byte) 0x31, (byte) 0x35, (byte) 0x32, (byte) 0x2E, (byte) 0x37, (byte) 0x30, (byte) 0x2E, (byte) 0x33, (byte) 0x35, (byte) 0x2E, (byte) 0x32, (byte) 0x34,
        (byte) 0x31, (byte) 0x30, (byte) 0x3E, (byte) 0x30, (byte) 0x10, (byte) 0x06, (byte) 0x07, (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0xCE, (byte) 0x3D, (byte) 0x02, (byte) 0x01, (byte) 0x06, (byte) 0x05,
        (byte) 0x2B, (byte) 0x81, (byte) 0x04, (byte) 0x00, (byte) 0x08, (byte) 0x03, (byte) 0x2A, (byte) 0x00, (byte) 0x04, (byte) 0xE7, (byte) 0x23, (byte) 0xCB, (byte) 0xAD, (byte) 0x65, (byte) 0x5E, (byte) 0x50,
        (byte) 0x57, (byte) 0x34, (byte) 0x9F, (byte) 0x48, (byte) 0x9F, (byte) 0x8B, (byte) 0x7E, (byte) 0xDA, (byte) 0x34, (byte) 0xE5, (byte) 0xB7, (byte) 0xB7, (byte) 0x5B, (byte) 0xEE, (byte) 0xE1, (byte) 0xC8,
        (byte) 0xB7, (byte) 0x64, (byte) 0xC3, (byte) 0x08, (byte) 0x1F, (byte) 0x04, (byte) 0xEC, (byte) 0x9A, (byte) 0xBD, (byte) 0xD4, (byte) 0xF8, (byte) 0x90, (byte) 0x88, (byte) 0x96, (byte) 0x6C, (byte) 0x54,
        (byte) 0x5F, (byte) 0x30, (byte) 0x09, (byte) 0x06, (byte) 0x07, (byte) 0x2A, (byte) 0x86, (byte) 0x48, (byte) 0xCE, (byte) 0x3D, (byte) 0x04, (byte) 0x01, (byte) 0x03, (byte) 0x30, (byte) 0x00, (byte) 0x30,
        (byte) 0x2D, (byte) 0x02, (byte) 0x14, (byte) 0x1C, (byte) 0x59, (byte) 0xF0, (byte) 0x67, (byte) 0x94, (byte) 0x91, (byte) 0x86, (byte) 0x42, (byte) 0x64, (byte) 0x82, (byte) 0x97, (byte) 0x84, (byte) 0x24,
        (byte) 0x2E, (byte) 0x4E, (byte) 0xB3, (byte) 0x66, (byte) 0x80, (byte) 0x65, (byte) 0xFB, (byte) 0x02, (byte) 0x15, (byte) 0x00, (byte) 0xEF, (byte) 0x7C, (byte) 0xFD, (byte) 0xD7, (byte) 0x57, (byte) 0x3F,
        (byte) 0x0E, (byte) 0x9A, (byte) 0x25, (byte) 0x24, (byte) 0xB9, (byte) 0x5D, (byte) 0xDD, (byte) 0xEF, (byte) 0x25, (byte) 0x25, (byte) 0xF5, (byte) 0x3D, (byte) 0xBD, (byte) 0x83,
    };

    private byte[] sECCPrivateKeyValue = {
        (byte) 0x7D, (byte) 0x09, (byte) 0xC2, (byte) 0x65, 
        (byte) 0x58, (byte) 0x72, (byte) 0x7C, (byte) 0x01, 
        (byte) 0x4A, (byte) 0xC8, (byte) 0xA5, (byte) 0x6F, 
        (byte) 0x69, (byte) 0xA1, (byte) 0xF1, (byte) 0x58, 
        (byte) 0x41, (byte) 0x3E, (byte) 0x34, (byte) 0x54
    };

    private byte[] sRSACertBytes = { // 412-byte RSA test cert
        (byte) 0x30,(byte) 0x82,(byte) 0x01,(byte) 0x98,(byte) 0x30,(byte) 0x82,
        (byte) 0x01,(byte) 0x01,(byte) 0x02,(byte) 0x01,(byte) 0x03,(byte) 0x30,
        (byte) 0x0D,(byte) 0x06,(byte) 0x09,(byte) 0x2A,(byte) 0x86,(byte) 0x48,
        (byte) 0x86,(byte) 0xF7,(byte) 0x0D,(byte) 0x01,(byte) 0x01,(byte) 0x04,
        (byte) 0x05,(byte) 0x00,(byte) 0x30,(byte) 0x11,(byte) 0x31,(byte) 0x0F,
        (byte) 0x30,(byte) 0x0D,(byte) 0x06,(byte) 0x03,(byte) 0x55,(byte) 0x04,
        (byte) 0x03,(byte) 0x13,(byte) 0x06,(byte) 0x53,(byte) 0x55,(byte) 0x4E,
        (byte) 0x57,(byte) 0x2D,(byte) 0x45,(byte) 0x30,(byte) 0x1E,(byte) 0x17,
        (byte) 0x0D,(byte) 0x30,(byte) 0x34,(byte) 0x30,(byte) 0x39,(byte) 0x31,
        (byte) 0x35,(byte) 0x30,(byte) 0x30,(byte) 0x30,(byte) 0x36,(byte) 0x30,
        (byte) 0x32,(byte) 0x5A,(byte) 0x17,(byte) 0x0D,(byte) 0x30,(byte) 0x38,
        (byte) 0x31,(byte) 0x30,(byte) 0x32,(byte) 0x34,(byte) 0x30,(byte) 0x30,
        (byte) 0x30,(byte) 0x36,(byte) 0x30,(byte) 0x32,(byte) 0x5A,(byte) 0x30,
        (byte) 0x18,(byte) 0x31,(byte) 0x16,(byte) 0x30,(byte) 0x14,(byte) 0x06,
        (byte) 0x03,(byte) 0x55,(byte) 0x04,(byte) 0x03,(byte) 0x13,(byte) 0x0D,
        (byte) 0x31,(byte) 0x35,(byte) 0x32,(byte) 0x2E,(byte) 0x37,(byte) 0x30,
        (byte) 0x2E,(byte) 0x33,(byte) 0x35,(byte) 0x2E,(byte) 0x32,(byte) 0x34,
        (byte) 0x31,(byte) 0x30,(byte) 0x81,(byte) 0x9F,(byte) 0x30,(byte) 0x0D,
        (byte) 0x06,(byte) 0x09,(byte) 0x2A,(byte) 0x86,(byte) 0x48,(byte) 0x86,
        (byte) 0xF7,(byte) 0x0D,(byte) 0x01,(byte) 0x01,(byte) 0x01,(byte) 0x05,
        (byte) 0x00,(byte) 0x03,(byte) 0x81,(byte) 0x8D,
        (byte) 0x00,(byte) 0x30,(byte) 0x81,(byte) 0x89,
        (byte) 0x02,(byte) 0x81,(byte) 0x81,(byte) 0x00,(byte) 0xB7,(byte) 0x05,(byte) 0xD0,(byte) 0x76,(byte) 0x37,(byte) 0x6B,(byte) 0x73,(byte) 0x76,(byte) 0x30,(byte) 0x87,(byte) 0x66,(byte) 0x08,
        (byte) 0xE7,(byte) 0x45,(byte) 0x3E,(byte) 0x28,(byte) 0xBE,(byte) 0x23,(byte) 0xA9,(byte) 0x4C,(byte) 0x00,(byte) 0xF1,(byte) 0xB7,(byte) 0xFD,(byte) 0x20,(byte) 0x2F,(byte) 0xD7,(byte) 0x93,
        (byte) 0x60,(byte) 0xFF,(byte) 0x1C,(byte) 0x1F,(byte) 0x7E,(byte) 0x7B,(byte) 0xE6,(byte) 0xE4,(byte) 0xB1,(byte) 0x19,(byte) 0x03,(byte) 0xE2,(byte) 0x99,(byte) 0x03,(byte) 0x6B,(byte) 0xCB,
        (byte) 0x8E,(byte) 0xFB,(byte) 0x40,(byte) 0x10,(byte) 0x15,(byte) 0xA1,(byte) 0x85,(byte) 0x05,(byte) 0xAF,(byte) 0x9A,(byte) 0x9E,(byte) 0xB7,(byte) 0xE3,(byte) 0x09,(byte) 0x1C,(byte) 0x85,
        (byte) 0xAC,(byte) 0x10,(byte) 0x66,(byte) 0xF9,(byte) 0xBD,(byte) 0xCF,(byte) 0x9D,(byte) 0x4C,(byte) 0x3C,(byte) 0x31,(byte) 0xE0,(byte) 0x99,(byte) 0xB0,(byte) 0xDD,(byte) 0x3B,(byte) 0x9C,
        (byte) 0xC2,(byte) 0x4E,(byte) 0x38,(byte) 0x81,(byte) 0x74,(byte) 0x3C,(byte) 0x73,(byte) 0x9A,(byte) 0x20,(byte) 0x66,(byte) 0xE9,(byte) 0xBF,(byte) 0x1B,(byte) 0xC1,(byte) 0x97,(byte) 0x2D,
        (byte) 0xC2,(byte) 0x79,(byte) 0x37,(byte) 0x56,(byte) 0xCC,(byte) 0x47,(byte) 0x89,(byte) 0x3C,(byte) 0x4C,(byte) 0x0A,(byte) 0x5D,(byte) 0x2D,(byte) 0xFC,(byte) 0x87,(byte) 0x62,(byte) 0x25,
        (byte) 0x00,(byte) 0x7E,(byte) 0x26,(byte) 0xDA,(byte) 0x55,(byte) 0xF4,(byte) 0x58,(byte) 0xFD,(byte) 0xCB,(byte) 0x52,(byte) 0x17,(byte) 0x64,(byte) 0xBC,(byte) 0x4C,(byte) 0x38,(byte) 0x5E,
        (byte) 0x7F,(byte) 0x3D,(byte) 0xBB,(byte) 0x41,(byte) 0x02,(byte) 0x03,(byte) 0x01,(byte) 0x00,(byte) 0x01,(byte) 0x30,(byte) 0x0D,(byte) 0x06,(byte) 0x09,(byte) 0x2A,(byte) 0x86,(byte) 0x48,
        (byte) 0x86,(byte) 0xF7,(byte) 0x0D,(byte) 0x01,(byte) 0x01,(byte) 0x04,(byte) 0x05,(byte) 0x00,(byte) 0x03,(byte) 0x81,(byte) 0x81,(byte) 0x00,(byte) 0x99,(byte) 0xE6,(byte) 0xCC,(byte) 0x07,
        (byte) 0x2A,(byte) 0xE2,(byte) 0x2B,(byte) 0xBC,(byte) 0xC1,(byte) 0xE7,(byte) 0x04,(byte) 0xB7,(byte) 0x7B,(byte) 0xD2,(byte) 0x09,(byte) 0x03,(byte) 0xFB,(byte) 0xEC,(byte) 0x01,(byte) 0x13,
        (byte) 0x7F,(byte) 0x61,(byte) 0x09,(byte) 0x1C,(byte) 0x6C,(byte) 0x74,(byte) 0x6F,(byte) 0xAB,(byte) 0xC3,(byte) 0xAD,(byte) 0xC7,(byte) 0xA4,(byte) 0x89,(byte) 0x89,(byte) 0x7F,(byte) 0x99,
        (byte) 0x44,(byte) 0xB5,(byte) 0xBF,(byte) 0xAE,(byte) 0x47,(byte) 0xEF,(byte) 0x71,(byte) 0x89,(byte) 0xB7,(byte) 0x5A,(byte) 0x53,(byte) 0x47,(byte) 0x67,(byte) 0x18,(byte) 0x04,(byte) 0x82,
        (byte) 0x24,(byte) 0x34,(byte) 0x6F,(byte) 0xA2,(byte) 0xAD,(byte) 0x25,(byte) 0xF2,(byte) 0x52,(byte) 0xCF,(byte) 0xF9,(byte) 0xAA,(byte) 0x8B,(byte) 0xAB,(byte) 0x2A,(byte) 0x95,(byte) 0x5F,
        (byte) 0x02,(byte) 0xA7,(byte) 0x0C,(byte) 0x91,(byte) 0xDF,(byte) 0xA8,(byte) 0x53,(byte) 0x3A,(byte) 0xCB,(byte) 0x27,(byte) 0xEA,(byte) 0x01,(byte) 0x09,(byte) 0x37,(byte) 0x5C,(byte) 0x24,
        (byte) 0x3A,(byte) 0x20,(byte) 0x5C,(byte) 0x80,(byte) 0xC5,(byte) 0x84,(byte) 0x74,(byte) 0xC9,(byte) 0x89,(byte) 0x7E,(byte) 0x41,(byte) 0xB9,(byte) 0x4A,(byte) 0x41,(byte) 0x7B,(byte) 0xE4,
        (byte) 0xB8,(byte) 0xC6,(byte) 0x82,(byte) 0x1C,(byte) 0x6B,(byte) 0xEF,(byte) 0x8E,(byte) 0x78,(byte) 0x95,(byte) 0x2F,(byte) 0xBC,(byte) 0x9A,(byte) 0x76,(byte) 0x7C,(byte) 0xF3,(byte) 0xD5,
        (byte) 0x9A,(byte) 0x6E,(byte) 0x20,(byte) 0x0C,(byte) 0x34,(byte) 0x55,(byte) 0x06,(byte) 0xD4,(byte) 0xCD,(byte) 0xD7,(byte) 0x93,(byte) 0xE3
    };

    private byte[] sRSAPrivateKeyExp = {
        (byte) 0xa1, (byte) 0xe3, (byte) 0x49, (byte) 0xb6, (byte) 0x17, (byte) 0x65, (byte) 0x3a, (byte) 0xf7, (byte) 0x13, (byte) 0x0b, (byte) 0xe0, (byte) 0xcc, (byte) 0x05, (byte) 0x4d, (byte) 0x31, (byte) 0x23,
        (byte) 0x36, (byte) 0xb4, (byte) 0x32, (byte) 0x7e, (byte) 0xa0, (byte) 0xe3, (byte) 0x2e, (byte) 0x3c, (byte) 0x7e, (byte) 0xe8, (byte) 0xf2, (byte) 0x85, (byte) 0x51, (byte) 0x29, (byte) 0xb6, (byte) 0x45,
        (byte) 0x81, (byte) 0x4a, (byte) 0xb5, (byte) 0x3c, (byte) 0x1a, (byte) 0x0e, (byte) 0x27, (byte) 0x6a, (byte) 0x9a, (byte) 0xaf, (byte) 0xf4, (byte) 0xfc, (byte) 0x54, (byte) 0x17, (byte) 0x97, (byte) 0x5d,
        (byte) 0xe2, (byte) 0xd4, (byte) 0x40, (byte) 0xe6, (byte) 0x7b, (byte) 0xa2, (byte) 0x87, (byte) 0xbb, (byte) 0xdd, (byte) 0xdd, (byte) 0x64, (byte) 0x56, (byte) 0x34, (byte) 0x91, (byte) 0x39, (byte) 0x93,
        (byte) 0x34, (byte) 0x97, (byte) 0xff, (byte) 0x44, (byte) 0x92, (byte) 0x50, (byte) 0xb4, (byte) 0xcc, (byte) 0xb8, (byte) 0xcc, (byte) 0x11, (byte) 0x71, (byte) 0xd2, (byte) 0x88, (byte) 0x3f, (byte) 0xc1,
        (byte) 0x8c, (byte) 0xee, (byte) 0x39, (byte) 0x37, (byte) 0x24, (byte) 0x8f, (byte) 0xbe, (byte) 0x3b, (byte) 0x32, (byte) 0xe5, (byte) 0xa6, (byte) 0xf1, (byte) 0x66, (byte) 0xab, (byte) 0x50, (byte) 0x95,
        (byte) 0xa2, (byte) 0xef, (byte) 0xa5, (byte) 0x42, (byte) 0xc4, (byte) 0x30, (byte) 0x2c, (byte) 0x91, (byte) 0x4a, (byte) 0xd3, (byte) 0x8f, (byte) 0xff, (byte) 0xef, (byte) 0x99, (byte) 0x6d, (byte) 0xa0,
        (byte) 0x97, (byte) 0x59, (byte) 0x8e, (byte) 0x1a, (byte) 0x78, (byte) 0xce, (byte) 0x5b, (byte) 0x3d, (byte) 0x3f, (byte) 0x5c, (byte) 0xa5, (byte) 0x5e, (byte) 0x2a, (byte) 0x6d, (byte) 0xc2, (byte) 0x85,
    };
    
    private byte[] sRSAPrivateKeyMod = {
        (byte) 0xb7, (byte) 0x05, (byte) 0xd0, (byte) 0x76, (byte) 0x37, (byte) 0x6b, (byte) 0x73, (byte) 0x76,
        (byte) 0x30, (byte) 0x87, (byte) 0x66, (byte) 0x08, (byte) 0xe7, (byte) 0x45,
        (byte) 0x3e, (byte) 0x28, (byte) 0xbe, (byte) 0x23, (byte) 0xa9, (byte) 0x4c, (byte) 0x00, (byte) 0xf1, (byte) 0xb7, (byte) 0xfd, (byte) 0x20, (byte) 0x2f, (byte) 0xd7, (byte) 0x93, (byte) 0x60,
        (byte) 0xff, (byte) 0x1c, (byte) 0x1f, (byte) 0x7e, (byte) 0x7b, (byte) 0xe6, (byte) 0xe4, (byte) 0xb1, (byte) 0x19, (byte) 0x03, (byte) 0xe2, (byte) 0x99, (byte) 0x03, (byte) 0x6b, (byte) 0xcb,
        (byte) 0x8e, (byte) 0xfb, (byte) 0x40, (byte) 0x10, (byte) 0x15, (byte) 0xa1, (byte) 0x85, (byte) 0x05, (byte) 0xaf, (byte) 0x9a, (byte) 0x9e, (byte) 0xb7, (byte) 0xe3, (byte) 0x09, (byte) 0x1c,
        (byte) 0x85, (byte) 0xac, (byte) 0x10, (byte) 0x66, (byte) 0xf9, (byte) 0xbd, (byte) 0xcf, (byte) 0x9d, (byte) 0x4c, (byte) 0x3c, (byte) 0x31, (byte) 0xe0, (byte) 0x99, (byte) 0xb0, (byte) 0xdd,
        (byte) 0x3b, (byte) 0x9c, (byte) 0xc2, (byte) 0x4e, (byte) 0x38, (byte) 0x81, (byte) 0x74, (byte) 0x3c, (byte) 0x73, (byte) 0x9a, (byte) 0x20, (byte) 0x66, (byte) 0xe9, (byte) 0xbf, (byte) 0x1b,
        (byte) 0xc1, (byte) 0x97, (byte) 0x2d, (byte) 0xc2, (byte) 0x79, (byte) 0x37, (byte) 0x56, (byte) 0xcc, (byte) 0x47, (byte) 0x89, (byte) 0x3c, (byte) 0x4c, (byte) 0x0a, (byte) 0x5d, (byte) 0x2d,
        (byte) 0xfc, (byte) 0x87, (byte) 0x62, (byte) 0x25, (byte) 0x00, (byte) 0x7e, (byte) 0x26, (byte) 0xda, (byte) 0x55, (byte) 0xf4, (byte) 0x58, (byte) 0xfd, (byte) 0xcb, (byte) 0x52, (byte) 0x17,
        (byte) 0x64, (byte) 0xbc, (byte) 0x4c, (byte) 0x38, (byte) 0x5e, (byte) 0x7f, (byte) 0x3d, (byte) 0xbb, (byte) 0x41                
    };
   	*/
    
    /*
     * These accumulate MD5 and SHA digests of all handshake
     * messages seen so far.
     */
    /** Accumulation of MD5 digests. */
    private MessageDigest ourMD5 = null;
    /** Accumulation of SHA digests. */
    private MessageDigest ourSHA = null;
    
    /*
     * The following fields maintain a buffer of available handshake
     * messages. Note that a single SSL record may include multiple
     * handshake messages.
     */
    /** Start of message in data buffer. */
    private int start = 0;
    /** Start of next message in data buffer. */
    private int nextMsgStart = 0;
    /** Count of bytes left in the data buffer. */
    private int cnt = 0;
    /** NegotiatedVersion **/
    private byte negVersion;
    private long hsTime;
    private String hsType;
    
    /**
     * Converts cipher suite number to cipher suite name.
     */
    private String getSuiteName(short suite) {
        switch (suite) {
        case ARCFOUR_40_MD5:
            return ("TLS_RSA_EXPORT_WITH_RC4_40_MD5");
        case ARCFOUR_128_MD5:
            return ("TLS_RSA_WITH_RC4_128_MD5");
        case ARCFOUR_128_SHA:
            return ("TLS_RSA_WITH_RC4_128_SHA");
        case ECDH_ECDSA_RC4_128_SHA:
            return ("TLS_ECDH_ECDSA_WITH_RC4_128_SHA");
        case ECDHE_ECDSA_RC4_128_SHA:
            return ("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA");
        case ECDH_RSA_RC4_128_SHA:
            return ("TLS_ECDH_RSA_WITH_RC4_128_SHA");
        case ECDHE_RSA_RC4_128_SHA:
            return ("TLS_ECDHE_RSA_WITH_RC4_128_SHA");
        }
        return ("Unknown");
    };
    
    /**
     * Validates a chain of certificates and returns the RSA public
     * key from the first certificate in that chain. The format of
     * the chain is specific to the ServerCertificate payload in an
     * SSL handshake.
     *
     * @param msg  byte array containing the SSL ServerCertificate
     *             payload (this is a chain of DER-encoded X.509
     *             certificates, in which each certificate is preceded
     *             by a 3-byte length field)
     * @param off  offset in the byte array where the cert chain begins
     * @param end  position in the byte array where the cert chain ends + 1
     *
     * @return server's certificate in the chain
     *
     * @exception IOException if the there is a binary formating error
     * @exception CertificateException if there a verification error
     */
    private X509Certificate parseChain(byte[] msg, int off, int end)
    throws IOException, CertificateException {
        
        Vector certs = new Vector();
        int len;
        
        // We have a 3-byte length field before each cert in list
        while (off < (end - 3)) {
            len = ((msg[off++] & 0xff) << 16) +
                    ((msg[off++] & 0xff) << 8) + (msg[off++] & 0xff);
            
            if (len < 0 || len + off > msg.length) {
                throw new IOException("SSL certificate length too long");
            }
            
            certs.addElement(
                    X509Certificate.generateCertificate(msg, off, len));
            
            off += len;
        }
        
        /*
         * The key usage extension of the server certificate is checked later
         * a based on the key exchange. Only the extended key usage is checked
         * now.
         */
//        if (certStore == null) {
//            System.out.println("** SKIPPING CERT VERIFICATION **");
//        } else {
        	verificationChain = X509Certificate.verifyChain(certs, -1,
          		X509Certificate.SERVER_AUTH_EXT_KEY_USAGE, certStore);
//        }
        
        // The first cert if specified to be the server cert.
        return (X509Certificate)certs.elementAt(0);
    }
    
    /**
     * Creates an Handshake object that is used to negotiate a
     * version 3 handshake with an SSL peer.
     *
     * @param host hostname of the peer
     * @param port port number of the peer
     * @param r    Record instance through which handshake
     *             will occur
     * @param tcs  trusted certificate store containing certificates
     *
     * @exception RuntimeException if SHA-1 or MD5 is not available
     */
    Handshake(String host, int port, Record r, CertStore tcs) {
        trustManager = TrustManager.getTrustManager(); // SPOT specific
        
        peerHost = new String(host);
        peerPort = port;
        rec = r;
        certStore = tcs;
        sPrivKey = null;
        gotCertReq = 0;
        start = 0;
        cnt = 0;
        
        try {
            ourMD5 = MessageDigest.getInstance("MD5");
            ourSHA = MessageDigest.getInstance("SHA-1");
            rnd = SecureRandom.getInstance(SecureRandom.ALG_SECURE_RANDOM);
        } catch (NoSuchAlgorithmException e) {
            // should only happen, if digests are not included in the build
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Returns the version of ssl which was negotiated with the
     * server. Currently SSL 3.0 (0x30) and TLS 1.0 (0x31) are supported.
     *
     * @return version of the ssl protocol negotiated with the server
     *
     */
    byte getNegotiatedVersion() {
        return negVersion;
    }

    String getType() {
        return hsType;
    }

    long getTimeMillis() {
        return hsTime;
    }
    
    /**
     * Obtains the next available handshake message.
     * <p>
     * The message returned has the header plus the number of
     * bytes indicated in the handshake message header.</p>
     *
     * @param type the desired handshake message type
     * @return number of bytes in the next handshake message
     * of the desired type or -1 if the next message is not of
     * the desired type
     * @exception IOException if there is a problem reading the
     * next handshake message
     */
    private int getNextMsg(byte type) throws IOException {
        if (cnt == 0) {
            rec.rdRec(true, Record.HNDSHK);
            
            if (rec.plainTextLength < HDR_SIZE) {
                throw new IOException("getNextMsg refill failed");
            }
            
            cnt = rec.plainTextLength;
            nextMsgStart = 0;
        }
        
        if (rec.inputData[nextMsgStart] == type) {
            int len = ((rec.inputData[nextMsgStart + 1] & 0xff) << 16) +
                      ((rec.inputData[nextMsgStart + 2] & 0xff) << 8) +
                      (rec.inputData[nextMsgStart + 3] & 0xff) + HDR_SIZE;
            
            if (cnt < len) {
                System.out.println("Got short message ...");
                System.out.println(Utils.hexEncode(rec.inputData, 
                    nextMsgStart, cnt));
                throw new IOException("Refill got short msg " +
                        "c=" + cnt + " l=" + len);
            }
            
            start = nextMsgStart;
            nextMsgStart += len;
            cnt -= len;
            return len;
        } else {
            return -1;
        }
    }
    
    /**
     * Sends an SSL version 3.0 Client hello handshake message.
     * <P />
     * @exception IOException if there is a problem writing to
     * the record layer
     */
    private void sndHello3() throws IOException {
        cSession = Session.get(peerHost, peerPort);
        int len = (cSession == null) ? 0 : cSession.id.length;
        /*
         * Size = 4 (HDR_SIZE) + 2 (client_version) + 32 (crand.length) +
         * 1 (session length) + len + 2 (cipher suite length) +
         * (2*CipherSuiteList.length) + 1 (compression length) + 1 (comp code)
         */
        byte[] msg = new byte[39 + len + suitesCompEtc.length];
        int idx = 0;
        // Fill the header -- type (1 byte) length (3 bytes)
        msg[idx++] = C_HELLO;
        int mlen = msg.length - HDR_SIZE;
        msg[idx++] = (byte) (mlen >>> 16);
        msg[idx++] = (byte) (mlen >>> 8);
        msg[idx++] = (byte) (mlen & 0xff);
        // ... client_version
        msg[idx++] = (byte) (ver >>> 4);
        msg[idx++] = (byte) (ver & 0x0f);
        // ... random
        /*
         * TODO: overwrite the first four bytes of crand with
         * current time and date in standard 32-bit UNIX format.
         */
        crand = new byte[32];
        rnd.nextBytes(crand, 0, 32);
        System.arraycopy(crand, 0, msg, idx, crand.length);
        idx += crand.length;
        // ... session_id
        msg[idx++] = (byte) (len & 0xff);
        if (cSession != null) {
            System.arraycopy(cSession.id, 0, msg, idx, cSession.id.length);
            idx += cSession.id.length;
        }
        // ... cipher_suites and compression methods
        System.arraycopy(suitesCompEtc, 0, msg, idx, suitesCompEtc.length);
        
        ourMD5.update(msg, 0, msg.length);
        ourSHA.update(msg, 0, msg.length);
        
        // Finally, write this handshake record
        rec.wrRec(Record.HNDSHK, msg, 0, msg.length);
    }
    
    /**
     * Receives a Server hello handshake message.
     * <P />
     * @return 0 on success, -1 on failure
     * @exception IOException if there is a problem reading the
     * message
     */
    private int rcvSrvrHello() throws IOException {
        int msgLength = getNextMsg(S_HELLO);
        int idx = start + HDR_SIZE;
        int endOfMsg = start + msgLength;
        
        /*
         * Message must be long enough to contain a 4-byte header,
         * 2-byte version, a 32-byte random, a 1-byte session Id
         * length (plus variable lenght session Id), 2 byte cipher
         * suite, 1 byte compression method.
         */
        if (msgLength < 42) {
            return -1;
        }
        
        // Get the server's  version
        byte srvrMajorNo, srvrMinorNo;
        
        srvrMajorNo=rec.inputData[start + idx++];
        srvrMinorNo=rec.inputData[start + idx++];
        
        // Assuming that all the versions higher than v3.0 will have
        // fall back mode to previous versions >=3.0
        // Presently the  support  is for v3.0 and v3.1        
        if (srvrMajorNo >= (byte)(0x03) &&
                srvrMajorNo <= (byte)(ver >>> 4) &&
                srvrMinorNo <= (byte)(ver & 0x0f)) {
            negVersion =  (byte)((srvrMajorNo<<4) + (srvrMinorNo & 0x0f)) ;
            // set version at record layer to the negotiated version
            // TODO: revisit this in light of the recent TLS WG discussion
            rec.setNegVersion(negVersion); 
        } else {
            return -1;
        }
        
        // .. the 32-byte server random
        srand = new byte[32];
        System.arraycopy(rec.inputData, idx, srand, 0, 32);
        idx += 32;
        
        // ... the session_Id length in 1 byte (and session_Id)
        int slen = rec.inputData[idx++] & 0xff;
        if (slen != 0) {
            if (endOfMsg < idx + slen) {
                return -1;
            }
            
            sSessionId = new byte[slen];
            System.arraycopy(rec.inputData, idx, sSessionId, 0, slen);
            idx += slen;
        }
        
        // ... the cipher suite
        negSuite = (short) (((rec.inputData[idx++] & 0xff) << 8) +
          (rec.inputData[idx++] & 0xff));
        
        /*
         * Check the cipher suite and compression method. The compression
         * method better be 0x00 since that is the only one we ever propose.
         */
        if ((negSuite != ARCFOUR_128_SHA) &&
                (negSuite != ARCFOUR_128_MD5) &&
                (negSuite != ARCFOUR_40_MD5) &&
                (rec.inputData[idx++] != (byte) 0x00)) {
            return -1;
        }
        
        // Update the hash of handshake messages
        ourMD5.update(rec.inputData, start, msgLength);
        ourSHA.update(rec.inputData, start, msgLength);
        
        negSuiteName = getSuiteName(negSuite);
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
                    "Negotiated " + negSuiteName);
        }
        
        return 0;
    }
    
    /**
     * Receives a Server certificate message containing a certificate
     * chain starting with the server certificate.
     * <P />
     * @return 0 if a trustworthy server certificate is found, -1 otherwise
     * @exception IOException if there is a problem reading the message
     */
    private int rcvCert() throws IOException {
        int msgLength;
        int endOfMsg;
        int idx;
        int len;
        
        msgLength = getNextMsg(CERT);
        endOfMsg = start + msgLength;
        
        /*
         * Message should atleast have a 4-byte header and an empty cert
         * list with 3-byte length
         */
        if (msgLength < 7) {
            return -1;
        }
        
        idx = start + HDR_SIZE;
        len = 0;
        
        // Check the length ...
        len = ((rec.inputData[idx++] & 0xff) << 16) +
                ((rec.inputData[idx++] & 0xff) << 8) + (rec.inputData[idx++] &
                0xff);
        if ((idx + len) > endOfMsg)
            return -1;
        
        // Parse the certificate chain and get the server's public key
        sCert = parseChain(rec.inputData, idx, endOfMsg);
        
        // Update the hash of handshake messages
        ourMD5.update(rec.inputData, start, msgLength);
        ourSHA.update(rec.inputData, start, msgLength);
        
        return 0;
    }
    
    /**
     * Receives a Server key exchange message. For now only RSA key
     * exchange is supported and this message includes temporary
     * RSA public key parameters signed by the server's long-term
     * private key. This message is optional.
     * <P />
     * @return 0 on success, -1 on failure
     * @exception IOException if there is a problem reading the
     * message
     * @exception RuntimeException if SHA-1 or MD5 is not available
     */
    private int rcvSrvrKeyExch() throws IOException {
        int msgLength = getNextMsg(S_KEYEXCH);
        int idx = start + HDR_SIZE;
        int endOfMsg = start + msgLength;
        
        switch (negSuite) {
        case ECDH_ECDSA_RC4_128_SHA:
        case ECDH_RSA_RC4_128_SHA:
            if (msgLength == -1) 
                return 0;
            else
                throw new IOException("Unexpected srvrKeyExch");

        case ECDHE_ECDSA_RC4_128_SHA:
        case ECDHE_RSA_RC4_128_SHA:
            throw new IOException("Not supported");

        default: 
            /* RSA key exchange:
             * Also note that the server key exchange is optional and used
             * only if the public key included in the certificate chain
             * is unsuitable for encrypting the pre-master secret.
             */
            RSAPublicKey sKey = (RSAPublicKey)sCert.getPublicKey();
            int keyUsage = sCert.getKeyUsage();

            if (msgLength == -1) {
                // We can use the server key to encrypt premaster secret
                eKey = sKey;
            
                /*
                 * Make sure sKey can be used for premaster secret encryption,
                 * i.e. if key usage extension is present, the key encipherment
                 * bit must be set
                 */
                if (keyUsage != -1 &&
                  (keyUsage & X509Certificate.KEY_ENCIPHER_KEY_USAGE) !=
                  X509Certificate.KEY_ENCIPHER_KEY_USAGE) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR, LogChannels.LC_SECURITY,
                          "The keyEncipherment was bit is " +
                          "set in server certificate key " +
                          "usage extension.");
                    }
                    throw new CertificateException(sCert,
                      CertificateException.INAPPROPRIATE_KEY_USAGE);
                }
                
                return 0;
            }
        
            // read and verify the encryption key parameters
            if (endOfMsg < (idx + 4)) {
                return -1;
            }
        
            // read the modulus length
            int len = ((rec.inputData[idx++] & 0xff) << 16) +
              (rec.inputData[idx++] & 0xff);
            if (endOfMsg < (idx + len + 2)) {
                return -1;
            }
        
            int modulusPos;
            int modulusLen;
            int exponentPos;
            int exponentLen;
            
            // ... and the modulus
            /*
             * Some weird sites (e.g. www.verisign.com) encode a
             * 512-bit modulus in 65 (rather than 64 bytes) with the
             * first byte set to zero. We accomodate this behavior
             * by using a special check.
             */
            if ((len == 65) && (rec.inputData[idx] == (byte)0x00)) {
                modulusPos = idx + 1;
                modulusLen = 64;
            } else {
                modulusPos = idx;
                modulusLen = len;
            }
        
            idx += len;
        
            // read the exponent length
            len = ((rec.inputData[idx++] & 0xff) << 16) +
              (rec.inputData[idx++] & 0xff);
            if (endOfMsg < (idx + len)) {
                return -1;
            }
        
            // ... and the exponent
            exponentPos = idx;
            exponentLen = len;
        
            eKey = new RSAPublicKey(rec.inputData, modulusPos, modulusLen,
              rec.inputData, exponentPos, exponentLen);
        
            idx += len;
        
            // mark where ServerRSAparams end
            int end = idx;
            
            // Now read the signature length
            len = ((rec.inputData[idx++] & 0xff) << 16) +
              (rec.inputData[idx++] & 0xff);
            if (endOfMsg < (idx + len)) {
                return -1;
            }
        
            // ... and the signature
            byte[] sig = new byte[len];
            System.arraycopy(rec.inputData, idx, sig, 0, sig.length);
            idx += len;
            if (endOfMsg != idx) {
                return -1;
            }
        
            // Compute the expected hash
            byte[] dat = new byte[MD5_SIZE + SHA_SIZE];
            try {
                MessageDigest di = MessageDigest.getInstance("MD5");
                
                di.update(crand, 0, crand.length);
                di.update(srand, 0, srand.length);
                di.update(rec.inputData, HDR_SIZE, end - HDR_SIZE);
                di.digest(dat, 0, MD5_SIZE);
                
                di = MessageDigest.getInstance("SHA-1");
                di.update(crand, 0, crand.length);
                di.update(srand, 0, srand.length);
                di.update(rec.inputData, HDR_SIZE, end - HDR_SIZE);
                di.digest(dat, MD5_SIZE, SHA_SIZE);
            } catch (Exception e) {
                throw new RuntimeException("No MD5 or SHA");
            }
        
            try {
                Cipher rsa = Cipher.getInstance("RSA");
                rsa.init(Cipher.DECRYPT_MODE, sKey);
                byte[] res = new byte[sKey.getModulusLen()];
                int val = rsa.doFinal(sig, 0, sig.length, res, 0);
                if (!Utils.byteMatch(res, 0, dat, 0, dat.length)) {
                    if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                        Logging.report(Logging.ERROR, LogChannels.LC_SECURITY,
                          "RSA params failed verification");
                    }
                    return -1;
                }
            } catch (Exception e) {
                throw new IOException("RSA decryption caught " + e);
            }
        }

        if (msgLength > 0) {
            // Update the hash of handshake messages
            ourMD5.update(rec.inputData, start, msgLength);
            ourSHA.update(rec.inputData, start, msgLength);
        }

        return 0;
    }
    
    /**
     * Receives a Certificate request message. This message is optional.
     * <P />
     * @return 0 (this method always completes successfully)
     * @exception IOException if there is a problem reading the
     * message
     */
    private int rcvCertReq() throws IOException {
        int msgLength = getNextMsg(CERT_REQ);
        if (msgLength == -1) {
            return 0; // certificate request is optional
        }
        
        /*
         * We do not support client-side certificates so if we see
         * a request for a certificate, remember it here so we can
         * complain later
         */
        gotCertReq = (byte) 1;
        
        // Update the hash of handshake messages
        ourMD5.update(rec.inputData, start, msgLength);
        ourSHA.update(rec.inputData, start, msgLength);
        
        // NOTE: We return zero without attempting to parse the message body.
        return 0;
    }
    
    /**
     * Receives a Server hello done message.
     * <P />
     * @return 0 on success, -1 on error
     * @exception IOException if there is a problem reading the
     * message
     */
    private int rcvSrvrHelloDone() throws IOException {
        int msgLength = getNextMsg(S_DONE);
        
        // A server_hello_done message has no body, just the header
        if (msgLength != HDR_SIZE) {
            return -1;
        }
        
        // Update the hash of handshake messages
        ourMD5.update(rec.inputData, start, msgLength);
        ourSHA.update(rec.inputData, start, msgLength);
        
        return 0;
    }
    
    /**
     * Sends a Client key exchange message. For now, only RSA key
     * exchange is supported and this message contains a pre-master
     * secret encrypted with the RSA public key of the server.
     * <P />
     * @exception IOException if there is a problem writing to the
     * record layer
     */
    private void sndKeyExch() throws IOException {
        int keyExchPayloadLen = 0;
        byte[] keyExchPayload = null;

        /*
         * If we get here, the server agreed to an RSA key exchange
         * and the RSA public key to be used for encrypting the
         * pre-master secret is available in eKey.
         */
        if (gotCertReq == 1) {
            // Send back an error ... we do not support client auth
            rec.alert(Record.FATAL, Record.NO_CERT);
            throw new IOException("No client cert");
        } 

        switch (negSuite) {
        case ECDHE_ECDSA_RC4_128_SHA:
        case ECDHE_RSA_RC4_128_SHA:
            throw new IOException("Not supported");

        case ECDH_ECDSA_RC4_128_SHA:
        case ECDH_RSA_RC4_128_SHA:
            // generate key pair on the same curve as the server
            // generate the premaster secret using our own private key 
            // and the server's public key
            // send our public key

            try {
                ECPublicKeyImpl sKey = (ECPublicKeyImpl) sCert.getPublicKey();
                int curveId = sKey.getCurve();
                cPubKey = new ECPublicKeyImpl(curveId);
                cPrivKey = new ECPrivateKeyImpl(curveId);
                ECKeyImpl.genKeyPair((ECPublicKeyImpl) cPubKey, 
                  (ECPrivateKeyImpl) cPrivKey);
                keyExchPayload = new byte[100];
                keyExchPayloadLen = 
                  ((ECPublicKeyImpl) cPubKey).getW(keyExchPayload, 1);
                keyExchPayload[0] = (byte) (keyExchPayloadLen++);
                // System.out.println("Key Exch payload length is " + keyExchPayloadLen + "\nPayload: " + Utils.hexEncode(keyExchPayload, 0, keyExchPayloadLen));
                
                // TODO: compute preMaster
                KeyAgreement agr1 = 
                  KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH);
                agr1.init((ECPrivateKeyImpl) cPrivKey);
                byte[] tmp = new byte[100];
                byte[] tmp2 = new byte[100];
                int len = ((ECPublicKeyImpl) sCert.getPublicKey()).getW(tmp, 0);
                int len2 = agr1.generateSecret(tmp, 0, len, tmp2, 0);
                preMaster = new byte[len2];
                System.arraycopy(tmp2, 0, preMaster, 0, len2);
                //System.out.println("preMaster [size=" + preMaster.length + 
                //  "]: " + Utils.hexEncode(preMaster, 0, preMaster.length));
            } catch (Exception e) {
                throw new IOException("sndKeyExch caught " + e);
            }
            break;
        default: // key exchange mechanism is RSA
            int encPreMasterIdx = 0;

            // Generate a 48-byte random pre-master secret
            preMaster = new byte[48];
            
            rnd.nextBytes(preMaster, 0, 48);
            // ... first two bytes must have client version
            /**
             * According to the TLS/SSL specs, the version should be the one
             * porposed in the clientHello message but because of a buggy but
             * very prevelant implementation of SSL that checks for only "3.0"
             * we hack our code to be compatible. The client sends the
             * "negVersion" and not the "ver". This breaks compliance  with
             * standard but is compatible with most deployed servers.
             * TODO: revisit this (MS claims to have fixed this bug in Vista)
             */            
            // preMaster[0] = (byte) (negVersion >>> 4);
            // preMaster[1] = (byte) (negVersion & 0x0f);
            preMaster[0] = (byte) (ver >>> 4);
            preMaster[1] = (byte) (ver & 0x0f);

            // Prepare a message containing the RSA encrypted pre-master
            // For TLS, the length of the RSA encrypted premaster is 
            // explicitly included as per Section 7.4.7.1 of RFC 4346 
            if (negVersion == 0x31) {
                keyExchPayloadLen = eKey.getModulusLen() + 2;
                keyExchPayload = new byte[keyExchPayloadLen];
                keyExchPayload[0] = (byte) (((keyExchPayloadLen - 2) >>> 8) 
                  & 0xff);
                keyExchPayload[1] = (byte) ((keyExchPayloadLen - 2) & 0xff);
                encPreMasterIdx = 2;
            } else {
                keyExchPayloadLen = eKey.getModulusLen();
                keyExchPayload = new byte[keyExchPayloadLen];
                encPreMasterIdx = 0;
            }

            // ... the encrypted pre-master secret
            try {
                Cipher rsa = Cipher.getInstance("RSA");
                
                rsa.init(Cipher.ENCRYPT_MODE, eKey);
                int val = rsa.doFinal(preMaster, 0, 48, 
                  keyExchPayload, encPreMasterIdx);
                if (val != (keyExchPayloadLen - encPreMasterIdx))
                    throw new IOException("RSA result (" + val + 
                      " bytes) does not match key exch payload length (" +
                        keyExchPayloadLen + " bytes)");
            } catch (Exception e) {
                throw new IOException("premaster encryption caught " + e);
            }
            break;
        }
            
        byte[] msg = new byte[HDR_SIZE + keyExchPayloadLen];
        int idx = 0;
        // Fill the type
        msg[idx++] = C_KEYEXCH;
        // ... message length
        msg[idx++] = (byte) (keyExchPayloadLen >>> 16);
        msg[idx++] = (byte) (keyExchPayloadLen >>> 8);
        msg[idx++] = (byte) (keyExchPayloadLen & 0xff);
            
        System.arraycopy(keyExchPayload, 0, msg, idx, keyExchPayloadLen);
           
        // Update the hash of handshake messages
        ourMD5.update(msg, 0, msg.length);
        ourSHA.update(msg, 0, msg.length);
            
        rec.wrRec(Record.HNDSHK, msg, 0, msg.length);
    }
    
    /**
     * Derives the master key based on the pre-master secret and
     * random values exchanged in the client and server hello messages.
     * <P />
     * @exception IOException if there is a problem during the computation
     */
    private void mkMaster() throws IOException {
        
        if (negVersion == 0x31) {
            byte[] temp = new byte[crand.length + srand.length];
            System.arraycopy(crand, 0, temp, 0, crand.length);
            System.arraycopy(srand, 0, temp, crand.length, srand.length);
            master = PRF(preMaster, "master secret", temp, 48);
            return;
        }
        
        //else it is 0x30
        
        byte[] expansion[] = {
            { (byte) 0x41 },                              // 'A'
            { (byte) 0x42, (byte) 0x42 },                 // 'BB'
            { (byte) 0x43, (byte) 0x43, (byte) 0x43 },    // 'CCC'
        };
        
        MessageDigest md = null;
        MessageDigest sd = null;
        
        /*
         * First, we compute the 48-byte (three MD5 outputs) master secret
         *
         * master_secret =
         *   MD5(pre_master + SHA('A' + pre_master +
         *                         ClientHello.random + ServerHello.random)) +
         *   MD5(pre_master + SHA('BB' + pre_master +
         *                         ClientHello.random + ServerHello.random)) +
         *   MD5(pre_master + SHA('CCC' + pre_master +
         *                         ClientHello.random + ServerHello.random));
         *
         * To simplify things, we use
         *   tmp = pre_master + ClientHello.random + ServerHello.random;
         */
        byte[] tmp = new byte[preMaster.length + crand.length + srand.length];
        System.arraycopy(preMaster, 0, tmp, 0, preMaster.length);
        System.arraycopy(crand, 0, tmp, preMaster.length, crand.length);
        System.arraycopy(srand, 0, tmp, preMaster.length + crand.length,
                srand.length);
        try {
            md = MessageDigest.getInstance("MD5");
            sd = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            /*
             * We should never catch this here (if these are missing,
             * we will catch this exception in the constructor)
             */
            throw new RuntimeException("No MD5 or SHA");
        }
        master = new byte[48];
        
        try {
            for (int i = 0; i < 3; i++) {
                md.update(preMaster, 0, preMaster.length);
                sd.update(expansion[i], 0, expansion[i].length);
                byte[] res = new byte[SHA_SIZE];
                sd.update(tmp, 0, tmp.length);
                sd.digest(res, 0, res.length);
                md.update(res, 0, res.length);
                md.digest(master, i << 4, MD5_SIZE);
            }
        } catch (DigestException e) {
            /*
             * We should never catch this here.
             */
            throw new RuntimeException("digest exception");
        }
    }
    
    /**
     * Sends a ChangeCipherSpec protocol message (this is not really
     * a handshake protocol message).
     * <P />
     * @exception IOException if there is a problem writing to the
     * record layer
     */
    private void sndChangeCipher() throws IOException {
        byte[] msg = new byte[1];
        // change cipher spec consists of a single byte with value 1
        msg[0] = (byte) 0x01;
        rec.wrRec(Record.CCS, msg, 0, 1); // msg.length is 1
    }
    
    /**
     * Computes the content of a Finished message.
     * <P />
     * @param who the role (either Record.CLIENT or
     * Record.SERVER) for which the finish message is computed
     * @return a byte array containing the hash of all handshake
     * messages seen so far
     * @exception IOException if handshake digests could not be computed
     */
    private byte[] computeFinished(byte who) throws IOException {
        
        if (negVersion == 0x31)
            return computeFinished31(who);
        
        byte[] sender[] = {
            { 0x53, 0x52, 0x56, 0x52}, // for server
            { 0x43, 0x4c, 0x4e, 0x54}  // for client
        };
        byte[] msg = new byte[MD5_SIZE + SHA_SIZE];
        byte[] tmp = null;
        
        try {
            // long t1 = System.currentTimeMillis();
            MessageDigest d = (MessageDigest) ourMD5.clone();
            d.update(sender[who], 0, 4);
            d.update(master, 0, master.length);
            tmp = new byte[MD5_SIZE];
            // MD5 padding length is 48
            d.update(MAC.PAD1, 0, 48);
            d.digest(tmp, 0, tmp.length);
            d.update(master, 0, master.length);
            d.update(MAC.PAD2, 0, 48);
            d.update(tmp, 0, tmp.length);
            d.digest(msg, 0, MD5_SIZE);
            
            d = (MessageDigest) ourSHA.clone();
            d.update(sender[who], 0, 4);
            d.update(master, 0, master.length);
            tmp = new byte[SHA_SIZE];
            // SHA padding length is 40
            d.update(MAC.PAD1, 0, 40);
            d.digest(tmp, 0, tmp.length);
            d.update(master, 0, master.length);
            d.update(MAC.PAD2, 0, 40);
            d.update(tmp, 0, tmp.length);
            d.digest(msg, MD5_SIZE, SHA_SIZE);
            
            return msg;
        } catch (Exception e) {
            throw new IOException("MessageDigest not cloneable");
        }
    }
    
    private byte[] computeFinished31(byte who) throws IOException {
        
        String[] sender = {
            "server finished",           //for Server
            "client finished"               // for client
        };
        
        MessageDigest md5 , sha;
        
        try {
            md5 = (MessageDigest) ourMD5.clone();
            sha = (MessageDigest) ourSHA.clone();
        } catch (Exception e) {
            throw new IOException("MessageDigest not cloneable");
        }
        try {
            byte[] md5Arr = new byte[MD5_SIZE];
            md5.digest(md5Arr, 0,md5Arr.length);
            
            
            byte[] shaArr = new byte[SHA_SIZE];
            sha.digest(shaArr, 0,shaArr.length);
            byte[] temp = new byte[MD5_SIZE + SHA_SIZE];
            
            System.arraycopy(md5Arr, 0, temp, 0, md5Arr.length);
            System.arraycopy(shaArr, 0,  temp,md5Arr.length, shaArr.length);
            
            return PRF(master, sender[who], temp, 12);
        } catch (Exception e) {
            throw new IOException("MessageDigest failed."+e);
        }
        
    }
    
    /**
     * Sends a Finished message.
     * <P />
     * @exception IOException if there is a problem writing to the
     * record layer
     */
    private void sndFinished() throws IOException {
        byte[] msg;
        byte[] tmp = computeFinished(role);
        msg = new byte[tmp.length + 4];
        System.arraycopy(FINISH_PREFIX, 0, msg, 0, 4);
        // Our default FINISH_PREFIX is for SSLv3, but TLS uses a
        // different payload length so we explicitly adjust that here.
        msg[3] = (byte) tmp.length;  
        System.arraycopy(tmp, 0, msg, 4, tmp.length);
        
        // Update the hash of handshake messages
        ourMD5.update(msg, 0, msg.length);
        ourSHA.update(msg, 0, msg.length);
        
        rec.wrRec(Record.HNDSHK, msg, 0, msg.length);
    }
    
    /**
     * Receives a ChangeCipherSpec protocol message (this is
     * not a handshake message).
     * <P />
     * @return 0 on success, -1 on error
     * @exception IOException if there is a problem reading the
     * message
     */
    private int rcvChangeCipher() throws IOException {
        /*
         * We make sure that there are no unread handshake messages
         * in the internal store when we get here.
         */
        if (cnt != 0) {
            if (Logging.REPORT_LEVEL <= Logging.ERROR) {
                Logging.report(Logging.ERROR, LogChannels.LC_SECURITY,
                        "Unread handshake mesg in store");
            }
            return -1;
        }
        
        /*
         * Note that CCS is not a handshake message (it is its own protocol)
         * The record layer header is 5 bytes and the CCS body is one
         * byte with value 0x01.
         */
        rec.rdRec(true, Record.CCS);
        if ((rec.inputData == null) || (rec.inputData.length != 1) ||
                (rec.inputData[0] != (byte) 0x01)) {
            return -1;
        }
        
        return 0;
    }
    
    /**
     * Receives a Finished message and verifies that it contains
     * the correct hash of handshake messages.
     * <P />
     * @return 0 on success, -1 on error
     * @exception IOException if there is a problem reading the
     * message
     */
    private int rcvFinished() throws IOException {
        int msgLength = getNextMsg(FINISH);
        if (msgLength != 16  &&  msgLength != 40) {// 16 for TLS, 40 for SSLv3
            return -1;
        }
        
        // Compute the expected hash
        byte[] expected = computeFinished((byte) (1 - role));
        
        if (!Utils.byteMatch(rec.inputData, start + HDR_SIZE, expected, 0,
                expected.length)) {
            return -1;
        } else {
            // Update the hash of handshake messages
            ourMD5.update(rec.inputData, start, msgLength);
            ourSHA.update(rec.inputData, start, msgLength);
            // now = System.currentTimeMillis();
            return 0;
        }
    }
    
    /**
     * It is the implementation of the pseudo random function defined
     * in rfc 2246 section 5
     * <P />
     * @param secret It  secret that needs to be expanded into blocks
     * of data for the purposes of key generation or validation
     * @param label An identifying string
     * @param seed  Random seed
     * @param destLen The length of the returned byte array
     * @return a byte array of length destLen
     */    
    public static  byte[] PRF( byte[] secret, String label, byte[] seed, 
            int destLen ) {
        
        byte[] firstHalfSecret;
        byte[] secondHalfSecret;
        int halfSecretLen;
        int odd = (secret.length % 2);
        int len = secret.length/2 + odd;
        firstHalfSecret = new byte [len];
        secondHalfSecret = new byte [len];
        System.arraycopy(secret,0,firstHalfSecret,0,len);
        System.arraycopy(secret,secret.length - len,secondHalfSecret,0,len);
        
        byte labelByteArr[] = label.getBytes();
        byte[] byteSeed = new byte[labelByteArr.length + seed.length];
        System.arraycopy(labelByteArr,0,byteSeed,0,labelByteArr.length);
        System.arraycopy(seed,0,byteSeed,labelByteArr.length, seed.length);
        byte[] p_md5 = p_hash("MD5",firstHalfSecret, byteSeed, destLen);
        byte[] p_sha = p_hash("SHA-1",secondHalfSecret, byteSeed, destLen);
        
        for ( int i=0;i < destLen; i++ ) {
            p_md5[i] = (byte) (p_md5[i] ^ p_sha[i]);
        }
        
        return p_md5;
    }
    
    /**
     * Implementation of p_hash construct defined
     * in rfc 2246 section 5
     * <P />
     * @param alg Hashing algorithm type. Currently MD5 or SHA
     * @param secret Secret that needs to be expanded into blocks
     * of data for the purposes of key generation or validation
     * @param label Identifying string
     * @param seed Random seed
     * @param length The length of the returned byte array
     * @return a byte array of length 'length'
     */    
    public static byte[] p_hash(String alg, byte[] secret, byte[] data, 
            int length) {
        int iterations;
        int algSize;
        algSize = (alg.equals("MD5")) ? MD5_SIZE:SHA_SIZE; //TODO: revisit
        iterations = (length%algSize == 0) ? length/algSize: length/algSize +1;
        
        byte[] algArr = new byte[length];
        byte[] A_i = data;   // calculated A(0)
        byte[] temp = new byte[data.length + algSize];
        
        
        int algArrIndex=0;
        
        for (int i = 0;i < iterations-1 ; i++) {
            A_i = hmac(alg, secret, A_i); // calculating A(i)
            System.arraycopy(A_i, 0, temp, 0, A_i.length); // copy it to temp
            System.arraycopy(data, 0, temp, A_i.length, data.length); 
                // data into temp
            System.arraycopy( hmac(alg, secret, temp), 0, algArr, 
                    algArrIndex, algSize);//algArr=algArr +HMAC(temp)
            algArrIndex += algSize;
        }
        
        for(int i =0; i<10000; i++);
        
        // in the last iteration have to fill up only upto the end of algArr
        A_i = hmac(alg, secret, A_i);
        System.arraycopy(A_i, 0, temp, 0, A_i.length);
        System.arraycopy(data, 0, temp, A_i.length, data.length);
        System.arraycopy( hmac(alg, secret, temp), 0, algArr, algArrIndex, 
                algArr.length -algArrIndex);
        
        return algArr;
        
    }
    
    
    
    /**
     * It is the implementation of the hashing function defined
     * in rfc 2104
     * <P />
     * @param alg Hashing algorithm type.
     * @param secret Secret whose hash is to be found
     * @param seed Random seed
     * @return a byte array of length 'length'
     */
    
    
    
    public static byte[] hmac(String alg,byte[] secret, byte[] seed) {
        int blockSize = 64;    // for both the algo block size is 64.
        byte[] ipad = new byte [64];
        byte[] opad = new byte[64];
        MessageDigest d = null;
        int algSize = 0;
        try {
            d = MessageDigest.getInstance(alg);
            algSize = d.getDigestLength();
        }catch (Exception e ) {
            // XXX: This should never happen
            System.out.println("No MD5 or SHA"); 
        }
        
        
        //Filling up ipad and opad
        int i;
        for (i = 0; i< 64; i++) {
            ipad[i] = (byte)0x36;
            opad[i] = (byte)0x5C;
            
        }
        
        // checking to see if seed is greater than 64
        if ( secret.length > blockSize) {
            byte[] temp = new byte[blockSize]; //does the specifications say 
            // that the array is initailzed to zero
            d.update(secret, 0, secret.length);
            try {
                d.digest(temp, 0, temp.length);
            } catch (DigestException e) {
                //  Ignore this exception, it should never happen
            }
            secret = temp;
        } else {
            byte temp[] = new byte[blockSize];
            System.arraycopy(secret, 0, temp,0, secret.length);
            secret = temp;
        }
        
        
        // ipad = K XOR ipad
        for (i = 0;i < blockSize; i++)
            ipad[i] = (byte) (secret[i] ^ ipad[i]);
        
        // ipad = H(K XOR ipad, seed)
        d.update(ipad,0,ipad.length);
        d.update(seed, 0, seed.length);
        try {
            d.digest(ipad, 0, ipad.length);
        } catch (DigestException e) {
            // Ignore this exception, it should never happen
        }
        
        
        //opad = K XOR opad
        for (i = 0; i<blockSize ; i++)
            opad[i] = (byte) ( secret[i] ^ opad[i]);
        byte[] temp = new byte[algSize];
        d.update(opad, 0, opad.length);
        d.update(ipad, 0, algSize);
        try {
            d.digest(temp, 0, temp.length);
        } catch (DigestException e) {
            // Ignore this exception, it should never happen
        }
        
        return temp;
    }
    
    /**
     * Initiates an SSL handshake with the peer specified previously
     * in the constructor.
     * <P />
     * @param aswho role played in the handshake (for now only
     * Record.CLIENT is supported)
     * @param proposedVersion is the highest  version the client
     * wants to propose to the server
     * @exception IOException if the handshake fails for some reason
     */
    void doHandShake(byte aswho, byte proposedVersion) throws IOException {
        long t1, t2;
        t1 = System.currentTimeMillis();
        if (aswho == Record.CLIENT)
            doHandShakeAsClient(proposedVersion);
        else
            doHandShakeAsServer(proposedVersion);
        t2 = System.currentTimeMillis();
        hsTime = t2 - t1;
        // System.out.println("doHandshake is done ...\n");

    }
    
    private void zeroSecrets() {
        // Zero out the premaster and master secrets
        if (preMaster != null) {
            // premaster can be null if we resumed an SSL session
            for (int i = 0; i < preMaster.length; i++) {
                preMaster[i] = 0;
            }
        }
        
        for (int i = 0; i < master.length; i++) {
            master[i] = 0;
        }
    }
    
    void matchName() throws Exception {
        if (sCert == null)
            throw new Exception("Missing server certificate");

        if (sCert.getSubject().endsWith(peerHost)) return;
        throw new Exception("Could not match host name <" +
                peerHost + "> to name <" + sCert.getSubject() +
                "> in server certificate");
    }
    
    /**
     * Initiates an SSL handshake as a Client with the peer specified
     * previously in the constructor.
     * <P />
     * @param proposedVersion is the highest  version the client
     * wants to propose to the server
     * @exception IOException if the handshake fails for some reason
     */
    // TODO: Allow handshake parameters such as cipher suites
    // and compression methods to be passed as arguments.
    void doHandShakeAsClient(byte proposedVersion) throws IOException {
        long t1 = System.currentTimeMillis();
        int code = 0;
        byte val = 0;
        
        ver = proposedVersion;
        role = Record.CLIENT;
        
        if (proposedVersion == 0x30)
            suitesCompEtc = SSL3_SUITES_COMP;
        else
            suitesCompEtc = TLS_SUITES_COMP_EXT;
            
        // System.out.println("Sending hello ...\n");
        sndHello3();
        
        // System.out.println("Receiving server hello ...\n");
        if (rcvSrvrHello() < 0) {
            complain("Bad ServerHello");
        };
        
        if ((sSessionId == null) || (cSession == null) ||
                (sSessionId.length != cSession.id.length) ||
                !Utils.byteMatch(sSessionId, 0, cSession.id, 0,
                sSessionId.length)) {
            // Session not resumed            
            try {
                // System.out.println("Receiving server cert ...\n");
                code = rcvCert();
                // System.out.println("Received server cert ...\n");
            } catch (CertificateException e) {
                complain(e);
            }
            
            if (code < 0) {
                complain("Corrupt server certificate message");
            }

            try {
                // System.out.println("Checking name in cert ...\n");
                matchName();
            } catch (Exception e) {
                complain(e.toString());
            }
            
            // ... get server_key_exchange (optional)
            try {
                // System.out.println("Receiving server key exch ...\n");
                code = rcvSrvrKeyExch();
            } catch (CertificateException e) {
                complain(e);
            }
            
            if (code < 0) {
                complain("Bad ServerKeyExchange");
            }
            
            // ... get certificate_request (optional)
            rcvCertReq();
            // System.out.println("Receiving server hello done ...\n");
            if (rcvSrvrHelloDone() < 0) {
                complain("Bad ServerHelloDone");
            }
            
            // ... send client_key_exchange
            // System.out.println("Sending key exchange ...\n");
            sndKeyExch();
            mkMaster();
            //System.out.println("Master secret [len=" + master.length +
            //  "] = " + Utils.hexEncode(master, 0, master.length));
            
            try {
                rec.init(role, crand, srand, negSuite, master);
            } catch (Exception e) {
                complain("Record.init() caught " + e);
            }
            
            // ... send change_cipher_spec
            // System.out.println("Sending CCS ...\n");
            sndChangeCipher();
            // ... send finished
            // System.out.println("Sending finished ...\n");
            sndFinished();
            
            // ... get change_cipher_spec
            // System.out.println("Receiving CCS ...\n");
            if (rcvChangeCipher() < 0) {
                complain("Bad ChangeCipherSpec");
            }
            
            // ... get finished
            // System.out.println("Receiving Finished ...\n");
            if (rcvFinished() < 0) {
                complain("Bad Finished");
            }
            hsType = "New, ID: " + Utils.hexEncode(sSessionId);
        } else {
            /*
             * The server agreed to resume a session.
             * Get the needed values from the previous session
             * now since the references could be overwritten if a
             * concurrent connection is made to this host and port.
             */            
            hsType = "Resumed, ID: " + Utils.hexEncode(sSessionId);
            master = new byte[cSession.master.length];
            System.arraycopy(cSession.master, 0, master, 0, cSession.master.length);
            sCert = cSession.cert;
            
            try {
                rec.init(role, crand, srand, negSuite, master);
            } catch (Exception e) {
                complain("Record.init() caught " + e);
            }
            
            // ... get change_cipher_spec
            if (rcvChangeCipher() < 0) {
                complain("Bad ChangeCipherSpec");
            }
            
            // ... get finished
            if (rcvFinished() < 0) {
                complain("Bad Finished");
            }
            
            // ... send change_cipher_spec
            sndChangeCipher();
            // ... send finished
            sndFinished();
        }

        // System.out.println("Adding session ...\n");
        Session.add(peerHost, peerPort, sSessionId, master, sCert);
        // System.out.println("Zeroing secrets ...\n");
        zeroSecrets();
    }
    
    /**
     * Sends a fatal alert indicating handshake_failure and marks
     * the corresponding SSL session is non-resumable.
     * <p />
     * @param msg string containing the exception message to be reported
     * @exception IOException with the specified string
     */
    private void complain(String msg) throws IOException {
        complain(new IOException(msg));
    }
    
    /**
     * Sends a fatal alert indicating handshake_failure and marks
     * the corresponding SSL session is non-resumable.
     * <p />
     * @param e the IOException to be reported
     * @exception IOException
     */
    private void complain(IOException e) throws IOException {
        try {
            rec.alert(Record.FATAL, Record.HNDSHK_FAIL);
            if (sSessionId != null) {
                Session.del(peerHost, peerPort, sSessionId);
            }
        } catch (Throwable t) {
            // Ignore, we are processing an exception currently
        }
        
        throw e;
    }
    
    /**
     * Initiates an SSL handshake as a Server with the peer specified
     * previously in the constructor.
     * <P />
     * @param preferredVersion is the preferred version the server
     * wants to accept
     * @exception IOException if the handshake fails for some reason
     */
    // TODO: Allow handshake parameters such as cipher suites
    // and compression methods to be passed as arguments.
    void doHandShakeAsServer(byte preferredVersion) throws IOException {
        int code = 0;
        role = Record.SERVER;
        
        // System.out.println("Inside doHandshakeAsServer");
        // System.out.println("Receiving clientHello (expecting v3 format) ...");
        if (rcvClientHello3() < 0) {
            complain("Bad ClientHello");
        };
        
        rec.setNegVersion(negVersion);
        /* recvClientHello initializes the master secret upon
         * finding a resumable session.
         */
        // System.out.println("Checking session ... ");
        if (sSession == null) {
            /* Need to engage in a full handshake */
            // System.out.println("Sending srvrHelloCertDone ... ");
            sndSrvrHelloCertDone();
           
            if (rcvKeyExch() < 0) {
                complain("Bad Key Exchange");
            }

            mkMaster();
            //System.out.println("Master secret [len=" + master.length +
            //  "] = " + Utils.hexEncode(master, 0, master.length));

            try {
                rec.init(role, crand, srand, negSuite, master);
            } catch (Exception e) {
                complain("Record.init() caught " + e);
            }

            // ... get change_cipher_spec
            // System.out.println("Receiving CCS ... ");
            if (rcvChangeCipher() < 0) {
                complain("Bad ChangeCipherSpec");
            }
            
            // ... get finished
            // System.out.println("Receiving Finished ... ");
            if (rcvFinished() < 0) {
                complain("Bad Finished");
            }
            
            // ... send change_cipher_spec
            // System.out.println("Sending CCS ... ");
            sndChangeCipher();
            // ... send finished
            // System.out.println("Sending Finished ... ");
            sndFinished();
            
            Session.add(peerHost, peerPort, sSessionId, master, sCert);
            hsType = "New, ID: " + Utils.hexEncode(sSessionId);
        } else {
            hsType = "Resumed, ID: " + Utils.hexEncode(sSessionId);
            try {
                rec.init(role, crand, srand, negSuite, master);
            } catch (Exception e) {
                complain("Record.init() caught " + e);
            }
            
            // ... send server_hello
            // System.out.println("Sending SrvrHelloDone ... ");
            sndSrvrHello();
            // ... send change_cipher_spec
            // System.out.println("Sending CCS ... ");
            sndChangeCipher();
            // ... send finished
            // System.out.println("Sending Finished ... ");
            sndFinished();
            
            // ... get change_cipher_spec
            // System.out.println("Receiving CCS ... ");
            if (rcvChangeCipher() < 0) {
                complain("Bad ChangeCipherSpec");
            }
            
            // ... get finished
            // System.out.println("Receiving Finished ... ");
            if (rcvFinished() < 0) {
                complain("Bad Finished");
            }
        }
        zeroSecrets();
    }
    
    private int rcvClientHello3() throws IOException {
        int msgLength = getNextMsg(C_HELLO);
        int idx = start + HDR_SIZE;
        int endOfMsg = start + msgLength;
        
        /* Message must be long enough to contain a 4-byte header,
         * 2-byte version, a 32-byte random, a 1-byte session Id
         * length (plus variable lenght session Id), 2 byte cipher
         * suite, at least 2-byte cipher suite list, 1 byte
         * compression length
         */
        if (msgLength < 44) {
            return -1;
        }
        
        byte clntMajorNo, clntMinorNo;
        clntMajorNo = rec.inputData[idx++];
        clntMinorNo = rec.inputData[idx++];
        
        if (clntMajorNo != 0x03) {
            System.out.println("Bad version major:" + clntMajorNo +
                    ", " + clntMinorNo);
            return -1;
        }
        
        if (clntMinorNo >= 0x01)
            negVersion = 0x31;
        else
            negVersion = 0x30;
        
        crand = new byte[32];
        System.arraycopy(rec.inputData, idx, crand, 0, crand.length);
        idx += 32;
        
        byte cSessionIdlen = rec.inputData[idx++];
        byte[] cSessionId = new byte[cSessionIdlen];
        if ((cSessionIdlen > 32) || (idx + cSessionIdlen > endOfMsg)) {
            System.out.println("Bad session length:" + cSessionIdlen);
            return -1;
        }
        
        if (cSessionIdlen != 0) {
            System.arraycopy(rec.inputData, idx, cSessionId, 0, cSessionIdlen);
            idx += cSessionIdlen;
            sSession = Session.get(cSessionId);
            if (sSession != null) {
                // Initialize master secret
                master = new byte[sSession.master.length];
                System.arraycopy(sSession.master, 0, master, 0, 
                        sSession.master.length);
                
                // Copy client's session Id into server's session Id
                sSessionId = new byte[cSessionIdlen];
                System.arraycopy(cSessionId, 0, sSessionId, 0, cSessionIdlen);
            }
        }
        
        // If needed, generate a new session Id
        if (sSessionId == null) {
            sSessionId = new byte[32];
            rnd.nextBytes(sSessionId, 0, 32);    
        }
                
        int cipherSuitelen = ((rec.inputData[idx++] & 0xff) << 8) +
                (rec.inputData[idx++] & 0xff);
        if (idx + cipherSuitelen > endOfMsg) {
            System.out.println("Bad cipher suite list length:" + 
                    cipherSuitelen);
            return -1;            
        }
        
        negSuite = 0;
        short cipherSuite = 0;
        boolean[] offered = new boolean[SUPPORTED_CIPHERS];
        for (int i = 0; i < SUPPORTED_CIPHERS; i++) offered[i] = false;
        for (int i = idx; i < idx + cipherSuitelen; i += 2) {
            cipherSuite = (short) (((rec.inputData[i] & 0xff) << 8) + 
              (rec.inputData[i + 1] & 0xff));
            // System.out.println("offered " + (cipherSuite & 0xffff));
            /* the offered array is ordered by preference */
            switch (cipherSuite) {
            case ECDH_RSA_RC4_128_SHA:
                offered[0] = true;
                break;
            case ECDH_ECDSA_RC4_128_SHA:
                offered[1] = true;
                break;
            case ECDHE_ECDSA_RC4_128_SHA:
                offered[2] = true;
                break;
            case ARCFOUR_128_SHA:
                offered[3] = true;
                break;
            case ARCFOUR_128_MD5:
                offered[4] = true;
                break;
            case ECDHE_RSA_RC4_128_SHA:
                offered[5] = true;
                break;
            case ARCFOUR_40_MD5:
                offered[6] = true;
                break;
            }
        }

        idx += cipherSuitelen;

        byte complen = 0;
        if (idx < endOfMsg) {
            complen = (byte) (rec.inputData[idx++] & 0xff);
        }
        idx += complen; // skip over list of supported compression algs

        if (endOfMsg > idx + 2) { // we have extensions, process them
            int totextlen = ((rec.inputData[idx++] & 0xff) << 8) + 
              (rec.inputData[idx++] & 0xff);
            int exttype = 0;
            int extlen = 0;
            while (idx < endOfMsg) {
                exttype = ((rec.inputData[idx++] & 0xff) << 8) + 
                  (rec.inputData[idx++] & 0xff);
                extlen = ((rec.inputData[idx++] & 0xff) << 8) + 
                  (rec.inputData[idx++] & 0xff);
                // System.out.println("Ext type: " + exttype + ", len: " + extlen);
                //System.out.println("Ext data: " + 
                //  Utils.hexEncode(rec.inputData, idx, extlen));
                switch (exttype) {
                case 10: // supported curves
                    int numCurves = (((rec.inputData[idx] & 0xff) << 8) + 
                      (rec.inputData[idx + 1] & 0xff))/2;
                    curveSupported = false;
                    for (int i = 0; i < numCurves; i++) {
                        //System.out.print("offered curve " +  
                        //    rec.inputData[idx + 2 + 2*i + 1]);
                        if ((rec.inputData[idx + 2 + 2*i] == 0x00) &&
                          (rec.inputData[idx + 2 + 2*i + 1] == ourCurve)) {
                            curveSupported = true;
                            // System.out.println(" ... supported");
                            break;
                        } else {
                            // System.out.println(" ... not supported");
                        }
                    }
                    if (!curveSupported) {
                        // System.out.println("Supported curve not offered");
                    }
                    break;

                case 11: // suported ec point formats
                    int numFormats = rec.inputData[idx] & 0xff;
                    uncompressedOk = false;
                    pointFormatNegotiated = true;
                    for (int i = 0; i < numFormats; i++) {
                        if (rec.inputData[idx + 1 + i] == 0x00) {
                            uncompressedOk = true;
                            break;
                        }
                    }
                    if (!uncompressedOk) {
                        //System.out.println("uncompressed pt format" +
                        //  " not offered");
                    } else {
                        //System.out.println("uncompressed pt format" +
                        //  " offered ... great");
                    }
                    break;
                default:
                    System.out.println("Ignoring unsupported extension");
                    break;
                }

                idx += extlen;
            }
        }
        
        /* Disable those for which we don't have the right certificate */
        if (sRSACertBytes == null) {
            offered[3] = false;
            offered[4] = false;
            offered[5] = false;
            offered[6] = false;
        } 

		/* Check if the EC keys are available 
		 * NOTE: here we make the assumption that the trustmanager only
		 * stores an ECC key (XXX revisit this if the assumption changes)
		 */
        if (!curveSupported || !uncompressedOk || 
			!trustManager.SpotKeysAndCertificateReady()) {
			// (sECCCertBytes == null)) { for platforms other than SPOTs
            offered[0] = false;
            offered[1] = false;
            offered[2] = false;
        }

        // .. and those not yet fully implemented
        offered[0] = false;
        offered[2] = false;
        offered[5] = false;

        negSuite = 0;
        for (int i = 0; i < SUPPORTED_CIPHERS; i++) {
            if (!offered[i]) continue;
            switch (i) {
            case 0:
                negSuite = ECDH_RSA_RC4_128_SHA;
                break;
            case 1:
                negSuite = ECDH_ECDSA_RC4_128_SHA;
                break;
            case 2:
                negSuite = ECDHE_ECDSA_RC4_128_SHA;
                break;
            case 3:
                negSuite = ARCFOUR_128_SHA;
                break;
            case 4:
                negSuite = ARCFOUR_128_MD5;
                break;
            case 5:
                negSuite = ECDHE_RSA_RC4_128_SHA;
                break;
            case 6:
                negSuite = ARCFOUR_40_MD5;
                break;
            }
            if (negSuite != 0) break;
        }

        // Set up server key and cert
        switch (negSuite) {
        case ARCFOUR_128_SHA:
        case ARCFOUR_128_MD5:
        case ARCFOUR_40_MD5:
        case ECDHE_RSA_RC4_128_SHA:
            sPrivKey = new RSAPrivateKey(sRSAPrivateKeyMod, sRSAPrivateKeyExp);
            sCertBytes = sRSACertBytes;
            break;
        case ECDHE_ECDSA_RC4_128_SHA:
        case ECDH_ECDSA_RC4_128_SHA:
        case ECDH_RSA_RC4_128_SHA:
            try {
				/* for platforms other than SPOTS 
                sPrivKey = new ECPrivateKeyImpl(ECCurve.SECP160R1);
                ((ECPrivateKeyImpl) sPrivKey).setS(sECCPrivateKeyValue, 
                  0, sECCPrivateKeyValue.length);
                sCertBytes = sECCCertBytes;
				*/
                sPrivKey = trustManager.getSpotPrivateKey();
                sCertBytes = trustManager.getSpotCertificateBytes();
            } catch (Exception e) {
                System.out.println("Caught " + e + " while initializing " +
                  	"server's EC private key.");
                return -1;
            }
            break;
        }
        
        if (negSuite == 0) {
            System.out.println("No common ciphers.");
            return -1; // No common ciphers
        } 

        // Update the hash of handshake messages
        ourMD5.update(rec.inputData, start, msgLength);
        ourSHA.update(rec.inputData, start, msgLength);
        
        // Save the negotiated suite and generate server random
        negSuiteName = getSuiteName(negSuite);
        //System.out.println("Negotiated " + negSuiteName + "(" +
        //	negSuite + ")");

        srand = new byte[32];
        rnd.nextBytes(srand, 0, 32);
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
                    "Negotiated " + negSuiteName);
        }
        
        return 0;
    }    

    private int getExtensionLen() {
        int val = 0;

        if (pointFormatNegotiated) 
             val += pointFormatExt.length; 
        if (val != 0) 
            return (val + 2); // include total length in front
        else
            return 0;
    }

    private int appendExtensions(byte[] buf, int off) {
        int oldoff = off;
        int extLen = getExtensionLen();

        if (extLen != 0) {
            buf[off++] = (byte) (((extLen - 2) >> 8) & 0xff);
            buf[off++] = (byte) ((extLen - 2) & 0xff);
         
            if (pointFormatNegotiated) {
                System.arraycopy(pointFormatExt, 0, buf, off, 
                  pointFormatExt.length);
                off += pointFormatExt.length;
            }
        }

        return (off - oldoff);
    }

    private void sndSrvrHelloCertDone() throws IOException {      
        int idx = 0;
        int mlen = 0;
                    
        /**
         * NOTE: We clump the Server Hello, (server) Certificate and 
         * Server Hello Done messages within a single SSL record.
         *
         * TODO: revisit this when we add support for TLS extensions
         * In the absence of such extensions, the ServerHello payload 
         * contains 2 bytes for the negotiated version, 32-bytes of the
         * server random, 1 byte for the server's session Id length, the
         * session Id itself, 2 bytes for the selected cipher suite, 1 byte
         * for the chosen compression method.
         *
         * The certificate message payload includes 3 bytes encoding the 
         * chain length and 3 bytes encoding the length of each certificate.
         * TODO: we'll need to revisit how we include chains.
         * We assume the latter
         */
         int extLen = getExtensionLen();
         int totmsglen = HDR_SIZE // Start of ServerHello msg
                 + 2 + srand.length
                 + 1 + sSessionId.length
                 + 2 // for the chosen cipher
                 + 1 // for the chosen compression method
                 + extLen // total extensions
                 + HDR_SIZE // Start of Certificate msg
                 + 3 // chain length
                 + 3 // certificate length TODO: revist for multiple certs
                 + sCertBytes.length
                 + HDR_SIZE; // Start of ServerHelloDone

         byte[] msg = new byte[totmsglen];
        
         // Start of ServerHello ...
         // ... header
         msg[idx++] = S_HELLO;
         mlen = 2 + srand.length + 1 + sSessionId.length + 2 + 1 + extLen;
         msg[idx++] = (byte) (mlen >>> 16);
         msg[idx++] = (byte) (mlen >>> 8);
         msg[idx++] = (byte) (mlen & 0xff);
         // ... negotiated version
         msg[idx++] = (byte) (negVersion >>> 4);
         msg[idx++] = (byte) (negVersion & 0x0f);
         // ... server random
         System.arraycopy(srand, 0, msg, idx, srand.length);
         idx += srand.length;
         // ... session Id
         msg[idx++] = (byte) sSessionId.length;
         System.arraycopy(sSessionId, 0, msg, idx, sSessionId.length);
         idx += sSessionId.length;
         // ... chosen cipher
         msg[idx++] = (byte) ((negSuite >> 8) & 0xff);
         msg[idx++] = (byte) (negSuite & 0xff);
         // ... compression method
         msg[idx++] = (byte) 0x00;
         // ... any extensions go next
         if (extLen != 0) idx += appendExtensions(msg, idx);

         // Start of Certificate ...
         // ... header
         msg[idx++] = CERT;
         mlen = 6 + sCertBytes.length;
         msg[idx++] = (byte) (mlen >>> 16);
         msg[idx++] = (byte) (mlen >>> 8);
         msg[idx++] = (byte) (mlen & 0xff);
         // ... cert chain length
         mlen -= 3;
         msg[idx++] = (byte) (mlen >>> 16);
         msg[idx++] = (byte) (mlen >>> 8);
         msg[idx++] = (byte) (mlen & 0xff);
         // ... cert length and contents
         mlen -= 3;
         msg[idx++] = (byte) (mlen >>> 16);
         msg[idx++] = (byte) (mlen >>> 8);
         msg[idx++] = (byte) (mlen & 0xff);
         System.arraycopy(sCertBytes, 0, msg, idx, sCertBytes.length);
         idx += sCertBytes.length;
         
         // Start of ServerHelloDone
         // ... header (there is no body)
         msg[idx++] = S_DONE;
         msg[idx++] = (byte) 0x00;
         msg[idx++] = (byte) 0x00;
         msg[idx++] = (byte) 0x00;

         
         ourMD5.update(msg, 0, msg.length);
         ourSHA.update(msg, 0, msg.length);
         
         // write this handshake record
         rec.wrRec(Record.HNDSHK, msg, 0, msg.length);
    }
    
    private void sndSrvrHello() throws IOException {
        int idx = 0;
        int mlen = 0;
        int extLen = getExtensionLen();
        int totmsglen = HDR_SIZE // Start of ServerHello msg
                + 2 + srand.length
                + 1 + sSessionId.length
                + 2 // for the chosen cipher
                + 1 + extLen;

        byte[] msg = new byte[totmsglen];

        // Start of ServerHello ...
        // ... header
        msg[idx++] = S_HELLO;
        mlen = 2 + srand.length + 1 + sSessionId.length + 2 + 1 + extLen;
        msg[idx++] = (byte) (mlen >>> 16);
        msg[idx++] = (byte) (mlen >>> 8);
        msg[idx++] = (byte) (mlen & 0xff);
        // ... negotiated version
        msg[idx++] = (byte) (negVersion >>> 4);
        msg[idx++] = (byte) (negVersion & 0x0f);
        // ... server random
        System.arraycopy(srand, 0, msg, idx, srand.length);
        idx += srand.length;
        // ... session Id
        msg[idx++] = (byte) sSessionId.length;
        System.arraycopy(sSessionId, 0, msg, idx, sSessionId.length);
        idx += sSessionId.length;
        // ... chosen cipher
        msg[idx++] = (byte) ((negSuite >> 8) & 0xff);
        msg[idx++] = (byte) (negSuite & 0xff);
        // ... compression method
        msg[idx++] = (byte) 0x00;
        // ... any extensions go next
        if (extLen != 0) idx += appendExtensions(msg, idx);

        ourMD5.update(msg, 0, msg.length);
        ourSHA.update(msg, 0, msg.length);
        
        // write this handshake record
        rec.wrRec(Record.HNDSHK, msg, 0, msg.length);
    }
        
    private int rcvKeyExch() throws IOException {
        int msgLength = getNextMsg(C_KEYEXCH);
        int idx = start + HDR_SIZE;
        int endOfMsg = start + msgLength;

        switch (negSuite) {
        case ARCFOUR_128_SHA:
        case ARCFOUR_128_MD5:
        case ARCFOUR_40_MD5:
            int tlsAdditionalBytes = 0;
            boolean isTLS = false;
            int modLen = ((RSAPrivateKey) sPrivKey).getModulusLen();
            int encodedModLen = msgLength - HDR_SIZE;
        
            // See Section 7.4.7.1 of RFC 4346 (TLS 1.1) for a subsequent 
            // clarification
            if (negVersion == 0x31) {
                isTLS = true;
                tlsAdditionalBytes = 2;
                encodedModLen = ((rec.inputData[idx++] & 0xff) << 8) + 
                  (rec.inputData[idx++] & 0xff);
            }
            
            if (msgLength != (HDR_SIZE + modLen + tlsAdditionalBytes)) {
                System.out.println("KeyExch message bad length (mod=" + 
                  modLen + ", msgLen=" + msgLength +
                  ", addlBytes=" + tlsAdditionalBytes);
                return -1;
            }
        
            if (encodedModLen != modLen) {
                System.out.println("Implied/encoded modLen (" + encodedModLen +
                  ") does not match expected length (" + modLen + ")\n");
                return -1;
            }
        
            // ... the encrypted pre-master secret
            try {
                Cipher rsa = Cipher.getInstance("RSA");
            
                preMaster = new byte[48];
                rsa.init(Cipher.DECRYPT_MODE, sPrivKey);
                int val = rsa.doFinal(rec.inputData, idx, encodedModLen, 
                  preMaster, 0);
                if (val != 48)
                    throw new IOException("Incorrect length of RSA decryption" +
                      " result.");
                // TODO: Add check for version in the first two bytes
            } catch (Exception e) {
                throw new IOException("premaster decryption caught " + e);
            }
            break;

        case ECDHE_RSA_RC4_128_SHA:
        case ECDHE_ECDSA_RC4_128_SHA:
        case ECDH_ECDSA_RC4_128_SHA:
        case ECDH_RSA_RC4_128_SHA:
            if (msgLength != (HDR_SIZE + rec.inputData[idx] + 1)) {
                System.out.println("KeyExch message bad length " + 
                  "(rec.inputData[" + idx + "] = " + 
                  rec.inputData[idx] + ", msglen = " + msgLength);
                return -1;
            }

            try {
                KeyAgreement agr1 = 
                  KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH);
                agr1.init((ECPrivateKeyImpl) sPrivKey);
                byte[] tmp = new byte[rec.inputData[idx]];
                int len = agr1.generateSecret(rec.inputData, idx + 1, 
                  rec.inputData[idx], tmp, 0);
                preMaster = new byte[len];
                System.arraycopy(tmp, 0, preMaster, 0, len);
                //System.out.println("preMaster [size=" + len + "]: " + 
                //  Utils.hexEncode(preMaster, 0, preMaster.length));
            } catch (Exception e) {
                System.out.println("ECC Key Exch caught " + e);
                return -1;
            }
            
            break;
        }

        // Update the hash of handshake messages
        ourMD5.update(rec.inputData, start, msgLength);
        ourSHA.update(rec.inputData, start, msgLength);
            
        return 0;
    }
}

    
    /**
     * This class implements methods to maintain resumable SSL
     * sessions.
     */
// visible within the package
    class Session {
        /**  Maximum number of cached resumable sessions. */
        private static final byte MAX_SESSIONS = 4;
        
        /**
         * Stores the last index where a session was overwritten, we
         * try to do a round-robin selection of places to overwrite
         */
        private static int delIdx = 0;
        
    /*
     * A session is uniquely identified by the combination of
     * host, port and session identifier. The master secret is
     * included in the cached session information.
     */
        /** Target host name. */
        String host;
        /** Target port number. */
        int port;
        /** Session identifier. */
        byte[] id;
        /** Master secret. */
        byte[] master;
        /** Target Certificate. */
        X509Certificate cert;
        
        /** A cache of currently resumable sessions. */
        private static Session[] sessions = new Session[MAX_SESSIONS];
        
        /**
         * Gets the master secret associated with a resumable session.
         * The session is uniquely identified by the combination of the
         * host, port.
         *
         * @param h host name of peer
         * @param p port number of peer
         *
         * @return matching session
         */
        static synchronized Session get(String h, int p) {
            for (int i = 0; i < MAX_SESSIONS; i++) {
                if ((sessions[i] == null) ||
                        (sessions[i].id == null)) continue;
                
                if (sessions[i].host.compareTo(h) == 0 &&
                        sessions[i].port == p) {
                    return sessions[i];
                }
            }
            
            return null;
        }
        
        /**
         * Get a session matching the specified session identifier received
         * from the client.
         *
         * @param id session identifier proposed by the client
         *
         * @return matching session or NULL, if no match found
         */
        static synchronized Session get(byte[] sid) {
            for (int i = 0; i < MAX_SESSIONS; i++) {
                if ((sessions[i] == null) ||
                        (sessions[i].id == null)) continue;
                
                /* Note that since the server is responsible for
                 * picking session Ids, we don't need to match the
                 * peer's host name or port as long as all of our
                 * session Ids are unique across all hostname/port
                 * combinations.
                 */
                if (Utils.byteMatch(sessions[i].id, 0,
                        sid, 0,
                        sid.length)) {
                    return sessions[i];
                }
            }
            
            return null;
        }
        
        /**
         * Adds a new session with the specified parameters to the cache
         * of resumable sessions. At any given time, this class maintains
         * at most one resusumable session for any host/port pair.
         * <P />
         * @param h host name of peer
         * @param p port number of peer
         * @param id session identifier
         * @param mas master secret
         * @param cert certificate of peer
         */
        static synchronized void add(String h, int p, byte[] id, byte[] mas,
                X509Certificate cert) {
            
            // TODO: This will change if we stop using linear arrays
            int idx = MAX_SESSIONS;
            for (int i = 0; i < MAX_SESSIONS; i++) {
                // System.out.println("i is " + i);

                // Is this empty?
                if ((sessions[i] == null) || (sessions[i].id == null)) {
                    // possible candidate for overwriting
                    if (idx == MAX_SESSIONS) idx = i; 
                    continue;
                }
                
                // XXX: For server sessions, h is null, p is zero (revisit)
                if ((p != 0) && (sessions[i].host != null) &&
                    (sessions[i].host.compareTo(h) == 0) &&
                    (sessions[i].port == p)) {  // preferred candidate
                    idx = i;
                    // System.out.println("candidate*: " + i +
                    // "port: " + p + "host: " + h);
                    break;
                }

            }
            
        /*
         * If all else is taken, overwrite the one specified by
         * delIdx and move delIdx over to the next one. Simulates FIFO.
         */
            if (idx == MAX_SESSIONS) {
                idx = delIdx;
                delIdx++;
                if (delIdx == MAX_SESSIONS) delIdx = 0;
            }
            // System.out.println("Chosen : " + idx);            
            if (sessions[idx] == null) {
                sessions[idx] = new Session();
            }
            
            sessions[idx].id = id;
            
        /*
         * Since the master will change after this method, we need to
         * copy it, to preserve its current value for later.
         */
            sessions[idx].master = new byte[mas.length];
            System.arraycopy(mas, 0, sessions[idx].master, 0, mas.length);
            
            sessions[idx].host = new String(h); // "h" will be a URL substring
            sessions[idx].port = p;
            sessions[idx].cert = cert;
            // System.out.println("Added session at index: " + idx);
            // System.out.println(sessions[idx].toString());
        }
        
        /**
         * Deletes the session identified by the specfied parameters
         * from the cache of resumable sessions.
         * <P />
         * @param h host name of peer
         * @param p port number of peer
         * @param sid session identifier
         */
        static synchronized void del(String h, int p, byte[] sid) {
            for (int i = 0; i < MAX_SESSIONS; i++) {
                if ((sessions[i] == null) ||
                        (sessions[i].id == null)) continue;
                
                if (Utils.byteMatch(sessions[i].id, 0,
                        sid, 0,
                        sid.length) &&
                        (sessions[i].host.compareTo(h) == 0) &&
                        (sessions[i].port == p)) {
                    sessions[i].id = null;
                    sessions[i].master = null;
                    sessions[i].host = null;
                    sessions[i].cert = null;
                    break;
                }
            }
        }
        
        public String toString() {
            String res;

            res = "\tHost: " + host + "\n" +
                  "\tPort: " + port + "\n" +
                  "\tId: " + Utils.hexEncode(id) + "\n" + 
                  "\tMaster: " + Utils.hexEncode(master) + "\n";
            return res;
        }
    }
