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

import com.sun.spot.security.InvalidKeyException;
import com.sun.spot.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.pki.Certificate;
import javax.microedition.pki.CertificateException;

import com.sun.midp.log.LogChannels;
import com.sun.midp.log.Logging;
import com.sun.spot.security.GeneralSecurityException;
import com.sun.spot.security.MessageDigest;
import com.sun.spot.security.PublicKey;
import com.sun.spot.security.Signature;
import com.sun.spot.security.implementation.RSAPublicKey;
import com.sun.spot.security.implementation.ecc.ECCurve;
import com.sun.spot.security.implementation.ECPublicKeyImpl;
//import com.sun.spotx.crypto.Cipher;

/**
 * This class implements methods for creating X.509 certificates and
 * accessing their attributes such as subject/issuer names, public keys
 * and validity information. Publicly visible methods methods are 
 * modeled after those in the X509Certificate classes 
 * from J2SE (standard edition) but there are some differences and 
 * these are documented below. <P />
 * NOTE: For now, only X.509 certificates containing RSA public keys
 * and signed either using md2WithRSA, md5WithRSA, or sha-1WithRSA are
 * supported.
 * This version of the implementation is unable to parse certificates
 * containing DSA keys or signed using DSA. Certificates containing
 * RSA keys but signed using an unsupported algorithm
 * can be parsed but cannot be verified. Not all version 3 extensions are 
 * supported (only subjectAltName, basicConstraints, keyUsage and
 * extendedKeyUsage are recognized) but if an unrecognized
 * extension is marked critical, an error notification is generated. 
 * <P />
 */
public class X509Certificate implements Certificate {

    /** Indicates a no error condition. */
    public static final byte NO_ERROR                   = 0;

    /**
     * Indicates that no information is available on
     * the pathLengthConstraint associated with this certificate
     * (this could happen if the certifiate is a v1 or v2 cert or
     * a v3 cert without basicConstraints or a non-CA v3 certificate).
     */
    public static final int MISSING_PATH_LENGTH_CONSTRAINT = -1;
    /** Indicates there is no limit to the server certificate chain length. */
    public static final int UNLIMITED_CERT_CHAIN_LENGTH = 65535;

    /** We expect issuer/subject names to fit within these many bytes. */
    private static final int MAX_NAME_LENGTH = 300;
    
    /** ASN ANY_STRING type used in certificate parsing (0x00). */
    private static final byte ANY_STRING_TYPE = 0x00; // our own impl
    // private static final byte BOOLEAN_TYPE  = 0x01 ?????
    /** ASN INTEGER type used in certificate parsing (0x02). */
    private static final byte INTEGER_TYPE    = 0x02;
    /** ASN BIT STRING type used in certificate parsing (0x03). */
    private static final byte BITSTRING_TYPE  = 0x03;
    /** ASN OCTET STRING type used in certificate parsing (0x04). */
    private static final byte OCTETSTR_TYPE   = 0x04;
    /** ASN OBJECT ID type used in certificate parsing (0x06). */
    private static final byte OID_TYPE        = 0x06;
    /** ASN UTF8 STRING type used in certificate parsing (0x0c). */
    private static final byte UTF8STR_TYPE    = 0x0c;
    /** ASN UNICODE STRING type used in certificate parsing (0x12). */
    private static final byte UNIVSTR_TYPE    = 0x12;
    /** ASN PRINT STRING type used in certificate parsing (0x13). */
    private static final byte PRINTSTR_TYPE   = 0x13;
    /** ASN TELETEX STRING type used in certificate parsing (0x14). */
    private static final byte TELETEXSTR_TYPE = 0x14;
    // private static final byte BMPSTR_TYPE     = 0x??
    /** ASN IA5 STRING type used in certificate parsing (0x16). */
    private static final byte IA5STR_TYPE     = 0x16;  // Used for EmailAddress
    /** ASN SEQUENCE type used in certificate parsing (0x30). */
    private static final byte SEQUENCE_TYPE   = 0x30;
    /** ASN SET type used in certificate parsing (0x31). */
    private static final byte SET_TYPE        = 0x31;

    /** Email address (rfc 822) alternative name type code. */
    public static final byte TYPE_EMAIL_ADDRESS = 1;
    /** DNS name alternative name type code. */
    public static final byte TYPE_DNS_NAME = 2;
    /** URI alternative name type code. */
    public static final byte TYPE_URI = 6;

    /** Bit mask for digital signature key usage.  */
    public static final int DIGITAL_SIG_KEY_USAGE = 0x00000001;
    /** Bit mask for non repudiation key usage. */
    public static final int NON_REPUDIATION_KEY_USAGE = 0x00000002;
    /** Bit mask for key encipherment key usage. */
    public static final int KEY_ENCIPHER_KEY_USAGE = 0x00000004;
    /** Bit mask for data encipherment key usage. */
    public static final int DATA_ENCIPHER_KEY_USAGE = 0x00000008;
    /** Bit mask for key agreement key usage. */
    public static final int KEY_AGREEMENT_KEY_USAGE = 0x00000010;
    /** Bit mask for key certificate sign key usage. */
    public static final int CERT_SIGN_KEY_USAGE = 0x00000020;
    /** Bit mask for CRL sign key usage. */
    public static final int CRL_SIGN_KEY_USAGE = 0x00000040;
    /** Bit mask for encipher only key usage. */
    public static final int ENCIPHER_ONLY_KEY_USAGE = 0x00000080;
    /** Bit mask for decipher only key usage. */
    public static final int DECIPHER_ONLY_KEY_USAGE = 0x00000100;

    /** Bit mask server auth for extended key usage. */
    public static final int SERVER_AUTH_EXT_KEY_USAGE = 0x00000002;
    /** Bit mask client auth for extended key usage. */
    public static final int CLIENT_AUTH_EXT_KEY_USAGE = 0x00000004;
    /** Bit code signing mask for extended key usage. */
    public static final int CODE_SIGN_EXT_KEY_USAGE = 0x00000008;
    /** Bit email protection mask for extended key usage. */
    public static final int EMAIL_EXT_KEY_USAGE = 0x00000010;
    /** Bit IPSEC end system mask for extended key usage. */
    public static final int IPSEC_END_SYS_EXT_KEY_USAGE = 0x00000020;
    /** Bit IPSEC tunnel mask for extended key usage. */
    public static final int IPSEC_TUNNEL_EXT_KEY_USAGE = 0x00000040;
    /** Bit IPSEC user mask for extended key usage. */
    public static final int IPSEC_USER_EXT_KEY_USAGE = 0x00000080;
    /** Bit time stamping mask for extended key usage. */
    public static final int TIME_STAMP_EXT_KEY_USAGE = 0x00000100;
    
    /**
     * The validity period is contained in thirteen bytes
     * yymmddhhmmss followed by 'Z' (for zulu ie GMT), if yy < 50
     * assume 20yy else 19yy.
     */
    private static final int UTC_LENGTH      = 13;

    /**
     * Maps byte codes that follow id-at (0x55 0x04) to corresponding name
     * component tags (e.g. Commom Name, or CN, is 0x55, 0x04, 0x03 and
     * Country, or C, is 0x55, 0x04, 0x06). See getName. See X.520 for
     * the OIDs and RFC 1779 for the printable labels. Place holders for
     * unknown labels have a 0 as the first char.
     */
    private static final char[][] nameAttr = {
        { 0 },
        { 0 },
        { 0 },
        { 'C', 'N' },                    // Common name: id-at 3
        { 'S', 'N'},                     // Surname: id-at 4
        { 0 },
        { 'C'},                          // Country: id-at 6
        { 'L'},                          // Locality: id-at 7
        { 'S', 'T'},                     // State or province: id-at 8
        { 'S', 'T', 'R', 'E', 'E', 'T'}, // Street address: id-at 9
        { 'O'},                          // Organization: id-at 10
        { 'O', 'U'},                     // Organization unit: id-at 11
    };

    /** Email attribute label in bytes. "EmailAddress" */
    private static final char[] EMAIL_ATTR_LABEL = {
        'E', 'm', 'a', 'i', 'l', 'A', 'd', 'd', 'r', 'e', 's', 's'
    };

    /** Email attribute object identifier. */
    private static final byte[] EMAIL_ATTR_OID = {
        (byte)0x2a, (byte)0x86, (byte)0x48, (byte)0x86, (byte)0xf7, 
        (byte)0x0d, (byte)0x01, (byte)0x09, (byte)0x01
    };

    /** Includes DER encoding for OID 1.2.840.113549.1.1. */
    private static final byte[] PKCS1Seq = {
        (byte) 0x30, (byte) 0x0d, (byte) 0x06, (byte) 0x09, 
        (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0x86,
        (byte) 0xf7, (byte) 0x0d, (byte) 0x01, (byte) 0x01,
    };

    /* DER encoding forECDSAwithSHA1 */
    private static final byte[] ECDSAwithSHA1Seq = {
        (byte) 0x30, (byte) 0x09, (byte) 0x06, (byte) 0x07, 
        (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0xce,
        (byte) 0x3d, (byte) 0x04, (byte) 0x01, 
    };
    
    /* PKCS1 OID */
    private static final byte[] PKCS1OID = {
        (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0x86,
        (byte) 0xf7, (byte) 0x0d, (byte) 0x01, (byte) 0x01,
    };
    
    /* ECDSA with SHA1 OID */
    private static final byte[] ECDSAwithSHA1OID = {
        (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0xce,
        (byte) 0x3d, (byte) 0x04, (byte) 0x01, 
    };

    /* EC public key OID */
    private static final byte[] ECpublicKeyOID = {
        (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0xce,
        (byte) 0x3d, (byte) 0x02, (byte) 0x01  // ANSI_X962_OID, 0x02, 0x01
    };
    
    /* SECG OID */
    private static final byte[] SECGOID = {
        (byte) 0x2b, (byte) 0x81, (byte) 0x04, (byte) 0x00
    };
    
    /* ANSIX962 OID */
    private static final byte[] ANSIX962GFpOID = {
        (byte) 0x2a, (byte) 0x86, (byte) 0x48, (byte) 0xce,
        (byte) 0x3d, (byte) 0x03, (byte) 0x01,   // ANSI_X962_OID, 0x03, 0x01
    };
    
    /*
     * These signature algorithms are encoded as PKCS1Seq followed by
     * a single byte with the corresponding value shown below, e.g.
     * md5WithRSAEncryption OBJECT IDENTIFIER  ::=  { 
     *     iso(1) member-body(2) us(840) rsadsi(113549) pkcs(1)
     *     pkcs-1(1) 4  
     * }
     */
    /** Uknown algorithm (-1). */
    private static final byte NONE           = -1;
    /** RAS ENCRYPTION (0x01). */
    private static final byte RSA_ENCRYPTION = 0x01;
    /** MD2_RSA algorithm (0x02). */
    private static final byte MD2_RSA        = 0x02;
    /** MD4_RSA algorithm (0x03). */
    private static final byte MD4_RSA        = 0x03;
    /** MD4_RSA algorithm (0x04). */
    private static final byte MD5_RSA        = 0x04;
    /** SHA1_RSA algorithm (0x05). */
    private static final byte SHA1_RSA       = 0x05;

    /* XXX: This is getting hacky. ECC algorithms do not fit the above
     * so for now, we carve out a new name space starting at 0x10.
     */
    private static final byte EC_PUBLIC_KEY = 0x10;
    private static final byte ECDSA_WITH_SHA1 = 0x11;

    /**
     * Expected prefix in decrypted value when MD2 hash is used for signing
     *  30 20 30 0c 06 08 2a 86 48 86 f7 0d 02 02 05 00 04 10 see verify().
     */
    private static final byte[] PREFIX_MD2 = {
        (byte) 0x30, (byte) 0x20, (byte) 0x30, (byte) 0x0c,
        (byte) 0x06, (byte) 0x08, (byte) 0x2a, (byte) 0x86,
        (byte) 0x48, (byte) 0x86, (byte) 0xf7, (byte) 0x0d,
        (byte) 0x02, (byte) 0x02, (byte) 0x05, (byte) 0x00, 
        (byte) 0x04, (byte) 0x10
    };
    
    /**
     * Expected prefix in decrypted value when MD5 hash is used for signing
     *  30 20 30 0c 06 08 2a 86 48 86 f7 0d 02 05 05 00 04 10 see verify().
     */
    private static final byte[] PREFIX_MD5 = {
        (byte) 0x30, (byte) 0x20, (byte) 0x30, (byte) 0x0c,
        (byte) 0x06, (byte) 0x08, (byte) 0x2a, (byte) 0x86,
        (byte) 0x48, (byte) 0x86, (byte) 0xf7, (byte) 0x0d,
        (byte) 0x02, (byte) 0x05, (byte) 0x05, (byte) 0x00, 
        (byte) 0x04, (byte) 0x10
    };
    /**
     * Expected prefix in decrypted value when SHA-1 hash is used for signing
     * 30 21 30 09 06 05 2b 0e 03 02 1a 05 00 04 14.
     */
    private static final byte[] PREFIX_SHA1 = {
        (byte) 0x30, (byte) 0x21, (byte) 0x30, (byte) 0x09,
        (byte) 0x06, (byte) 0x05, (byte) 0x2b, (byte) 0x0e,
        (byte) 0x03, (byte) 0x02, (byte) 0x1a, (byte) 0x05,
        (byte) 0x00, (byte) 0x04, (byte) 0x14
    };
    
    /** ASN encoding for NULL. */
    private static final byte[] NullSeq = {
        (byte) 0x05, (byte) 0x00
    };

    /** This is how the encoding of validity information begins. */
    private static final byte[] ValiditySeq = {
        (byte) 0x30, (byte) 0x1e
    };
    
    /** This is how the encoding of UTCTime begins. */
    private static final byte[] UTCSeq = {
        (byte) 0x17, (byte) 0x0d
    };
    
    /** Includes DER encoding for id-kp (key purpose). */
    private static final byte[] ID_KP = {
        (byte) 0x2b, (byte) 0x06, (byte) 0x01, (byte) 0x05,
        (byte) 0x05, (byte) 0x07, (byte) 0x03
    };

    /** True iff subject matches issuer. */
    private boolean selfSigned = false;
    /** X.509 version. For more readable code the version field starts a 1. */
    private byte version = 1;
    /** MD5 fingerprint of the certificate. */
    private byte[] fp = null;  
    /** Certificate serial number. */
    private String serialNumber;
    /** Certificate subject. */
    private String subject;
    /** Certificate issuer. */
    private String issuer;
    /** Beginning of certificate validity period. */
    private long from = 0; 
    /** End of certificate validity period. */
    private long until = 0;
    /** Certificate RSA Public key. */
    private PublicKey pubKey = null;
    
    // The following fields are only meaningful in certificates created
    // by fully parsing the DER encoding. They are meaningless on
    // certificates created using the Certificate constructor below.
    /** Index inside encoding. */
    private int idx = 0; 
    /** Contains Certificate DER encoding. */
    private byte[] enc = null;
    /** Offset where TBSCertificate starts. */
    private int TBSStart = 0;
    /** Length of TBSCertificate. */
    private int TBSLen = 0;
    /** Algorithm used to sign the cert. */
    private byte sigAlg = NONE;
    /** Issuer signature on certificate. */
    private byte[] signature = null;
    /**  Hash of TBSCertificate. */
    private byte[] TBSCertHash = null;
    /** TBSCertificate bytes */
    private byte[] TBSCertBytes = null;
    /**  True iff cert has unrecognized critical extension. */
    private boolean badExt = false; 
    /**  Alternate name. */

    /** format of the subject alternative name, 2 means a DNS name */
    private byte subAltNameType;
    /** subject alternative name */
    private Object subAltName;
    /** does the cert include BasicConstaints. */
    private boolean hasBC = false; 
    /** CA value in BasicConstraints. */
    private boolean isCA = false;
    /** Path Length constriant from Basic constraints. */
    private int pLenConstr = MISSING_PATH_LENGTH_CONSTRAINT;
    /** Collection of keyUsage bits. */
    private int keyUsage = -1;
    /** Collection of extended keyUsage bits. */
    private int extKeyUsage = -1;
    
    /** Private constructor */
    private X509Certificate() {
    }
    
       /**
    * Creates an X.509 certificate with the specified attributes.
    * This constructor is only used for creating trusted certificates.
    * <BR />
    * <B>NOTE:</B> All signature related values in these certificates
    * (such as the signing algorithm and signature) are set to null and
    * invoking methods that access signature information, e.g. verify()
    * and getSigAlgName() can produce unexpected errors.
    * <P />
    * @param ver        byte containing X.509 version
    * @param serialNoStr serial number as string
    * @param sub        subject name
    * @param iss        issuer name
    * @param notBefore  start of validity period expressed in milliseconds
    *                   since midnight Jan 1, 1970 UTC
    * @param notAfter   end of validity period expressed as above
    * @param publicKey  public key associated with the certificate
    * @param chash      16-byte MD5 hash of the certificate's ASN.1
    *                   DER encoding
    * @param pLen       Is the pathLenConstraint associated with a version 3
    *                   certificate. This parameter is ignored for v1 and
    *                   v2 certificates. If a v3 certificate does not
    *                   have basicConstraints or is not a CA cert, callers
    *                   should pass MISSING_PATH_LENGTH_CONSTRAINT. If the
    *                   v3 certificate has basicConstraints, CA is set but
    *                   pathLenConstraint is missing (indicating no limit
    *                   on the certificate chain), callers should pass
    *                   UNLIMITED_CERT_CHAIN_LENGTH.
    */
   public X509Certificate(byte ver, String serialNoStr, String sub,
           String iss, long notBefore, long notAfter,
           PublicKey publicKey, byte[] chash,
           int pLen) {
       version = ver;
       serialNumber = serialNoStr;
       /*
        * We are paranoid so we don't just assign a reference as in
        * fp = chash; subject = sub; issuer = iss;
        */
       if (chash != null) {
           fp = new byte[chash.length];
           System.arraycopy(chash, 0, fp, 0, chash.length);
       }
       subject = new String(sub);
       issuer = new String(iss);
       from = notBefore;
       until = notAfter;
       sigAlg = NONE;
       if (subject.compareTo(issuer) == 0) {
           selfSigned = true;
       }
       pubKey = publicKey;
       if ((ver == 3) && (pLen != MISSING_PATH_LENGTH_CONSTRAINT)) {
           hasBC = isCA = true;
           pLenConstr = pLen;
       }
   }
    
    /**
     * Creates an X.509 certificate with the specified attributes.
     * This constructor is only used for creating trusted certificates. 
     * <BR />
     * <B>NOTE:</B> All signature related values in these certificates 
     * (such as the signing algorithm and signature) are set to null and
     * invoking methods that access signature information, e.g. verify()
     * and getSigAlgName() can produce unexpected errors.
     * <P />
     * @param ver        byte containing X.509 version
     * @param serialNoStr serial number as string
     * @param sub        subject name
     * @param iss        issuer name
     * @param notBefore  start of validity period expressed in milliseconds
     *                   since midnight Jan 1, 1970 UTC 
     * @param notAfter   end of validity period expressed as above
     * @param mod        modulus associated with the RSA Public Key
     * @param exp        exponent associated with the RSA Public Key
     * @param chash      16-byte MD5 hash of the certificate's ASN.1 
     *                   DER encoding
     * @param pLen       Is the pathLenConstraint associated with a version 3
     *                   certificate. This parameter is ignored for v1 and
     *                   v2 certificates. If a v3 certificate does not
     *                   have basicConstraints or is not a CA cert, callers
     *                   should pass MISSING_PATH_LENGTH_CONSTRAINT. If the
     *                   v3 certificate has basicConstraints, CA is set but
     *                   pathLenConstraint is missing (indicating no limit
     *                   on the certificate chain), callers should pass
     *                   UNLIMITED_CERT_CHAIN_LENGTH.
     * @exception Exception in case of a problem with RSA public key parameters
     */
    public X509Certificate(byte ver, String serialNoStr, String sub,
                           String iss, long notBefore, long notAfter, 
                           byte[] mod, byte[] exp, byte[] chash, 
                           int pLen)
        throws Exception {
            version = ver;
            serialNumber = serialNoStr;

            /*
             * We are paranoid so we don't just assign a reference as in
             * fp = chash; subject = sub; issuer = iss;
             */ 
            if (chash != null) {
                fp = new byte[chash.length];
                System.arraycopy(chash, 0, fp, 0, chash.length);
            }

            subject = new String(sub);
            issuer = new String(iss);
            from = notBefore;
            until = notAfter;
            sigAlg = NONE;
          
            if (subject.compareTo(issuer) == 0) {
                selfSigned = true;
            }

            pubKey = new RSAPublicKey(mod, exp);

            if ((ver == 3) && (pLen != MISSING_PATH_LENGTH_CONSTRAINT)) {
                hasBC = isCA = true;
                pLenConstr = pLen;
            }
        }

    /**
     * Matches the contents of buf against this certificates DER
     * encoding (enc) starting at the current offset (idx).
     * <P />
     * @param buf buffer whose contents are to be matched against the
     *            certificate encoding
     * @exception Exception if the match fails
     */ 
    private void match(byte[] buf) throws Exception {
        if (idx + buf.length < enc.length) {
            for (int i = 0; i < buf.length; i++) {
                if (enc[idx++] != buf[i]) 
                    throw new Exception("match() error 1");
            }
        } else {
            throw new Exception("match() error 2");
        }
    }

    /**
     * Matches the specified ASN type against this certificates DER
     * encoding (enc) starting at the current offset (idx) and returns
     * its encoded length.
     * <P />
     * @param type ASN type to be matched
     * @return the size in bytes of the sub-encoding associated with
     *         the given type
     * @exception IOException if the length is not formated correctly
     */ 
    private int getLen(byte type) throws IOException {

        if ((enc[idx] == type) || 
            ((type == ANY_STRING_TYPE) && // ordered by likelihood of match
            ((enc[idx] == PRINTSTR_TYPE) || (enc[idx] == TELETEXSTR_TYPE) ||
            (enc[idx] == UTF8STR_TYPE) || (enc[idx] == IA5STR_TYPE) ||
            (enc[idx] == UNIVSTR_TYPE)))) {
            idx++;
            int size = (enc[idx++] & 0xff);
            if (size >= 128) {
                int tmp = size - 128;
                // NOTE: for now, all sizes must fit int two bytes
                if ((tmp > 2) || (idx + tmp > enc.length)) {
                    throw new IOException("getLen() err 1");
                } else {
                    size = 0;
                    while (tmp > 0) {
                        size = (size << 8) + (enc[idx++] & 0xff);
                        tmp--;
                    }
                }
            }
            return size;
        }

        throw new IOException("getLen() err 2");
    }
    
    /**
     * Parses a signature algorithm in the DER encoding (enc) starting at 
     * the current offset (idx). 
     * <P />
     * @return a single-byte algorithm identifier, e.g. MD5_RSA, MD2_RSA
     * @exception IOException if an error is encountered during parsing
     */ 
    private byte getSigAlg() throws IOException {
        int idxSaved;
        int len = 0;
        byte val = NONE;

        try {
            len = getLen(SEQUENCE_TYPE);
            len = getLen(OID_TYPE);
            idxSaved = idx;
            if (len == ECDSAwithSHA1OID.length) {
                match(ECDSAwithSHA1OID);
                return ECDSA_WITH_SHA1;
            }
            idx = idxSaved;
            if (len == PKCS1OID.length + 1) {
                match(PKCS1OID);
                val = enc[idx++];
                match(NullSeq);
                return val;
            }
        } catch (Exception e) {
            throw new IOException("Algorithm Id parsing failed");
        }
        return NONE;
    }

    /**
     * Parses a SubjectName or IssuerName in the DER encoding
     * (enc) starting at the current offset (idx) and ending
     * at end. 
     * <P />
     * @param end ending offset for the DER-encoded name
     * @return a human friendly string representation of the name
     * @exception IOException if an error is encountered during parsing
     */ 
    private String getName(int end) throws IOException {
        byte[] name = new byte[MAX_NAME_LENGTH];
        int nameLen = 0;
        int len = 0;
        int cidx;   // index where the most recently seen name component starts
        int clen;   // Component length
        char[] label = null;
        int aidx;
        
        while (idx < end) {
            if (nameLen != 0) {
                // this is not the first time so insert a separator
                name[nameLen++] = (byte)';';
            }
            
            getLen(SET_TYPE);
            getLen(SEQUENCE_TYPE);

            /*
             * Save the start of name component, e.g CommonName
             * ... and its length
             */
            clen = getLen(OID_TYPE);
            cidx = idx;            
            idx += clen;

            /*
             * At this point we tag the name component, e.g. C= or hex
             * if unknown.
             */
            if ((clen == 3) && (enc[cidx] == 0x55) &&
                    (enc[cidx + 1] == 0x04)) {
                // begins with id-at, so try to see if we have a label
                aidx = enc[cidx + 2] & 0xFF;
                if ((aidx < nameAttr.length) && (nameAttr[aidx][0] != 0)) {
                    label = nameAttr[aidx];
                } else {
                    label = Utils.hexEncodeToChars(enc, cidx, clen);
                }
            } else if (Utils.byteMatch(enc, cidx, EMAIL_ATTR_OID, 0,
                       EMAIL_ATTR_OID.length)) {
                label = EMAIL_ATTR_LABEL;
            } else {
                label = Utils.hexEncodeToChars(enc, cidx, clen);
            }

            for (int i = 0; i < label.length; i++) {
                name[nameLen++] = (byte)label[i];
            }

            name[nameLen++] = (byte)'=';

            len = getLen(ANY_STRING_TYPE);

            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    name[nameLen++] = enc[idx++];
                }
            }
        }

        //return new String(name, 0, nameLen, "UTF-8");
        //    Spot doesn't support the UTF-8 constructor for string
        return new String(name, 0, nameLen); 
    }

    /**
     * Gets a string representation of the UTC time whose DER ecnoding
     * is contained in the specified buffer.
     * <P />
     * @param buf buffer containing the DER encoding of UTC Time
     * @param off starting offset of the encoding inside buf
     * @return a string represntation of the UTC time in the form
     * yy/mm/dd hh:mm:ss
     * @exception IOException if an error is encountered during parsing
     */ 
    private static long getUTCTime(byte[] buf, int off) throws IOException {
        Calendar cal;
        int[] period = new int[6]; // year, month, day, hour, minute, second

        if (buf[off + UTC_LENGTH - 1] != (byte) 'Z') 
            throw new IOException("getUTCTime() err 1");
        for (int i = 0; i < 6; i++) {
            period[i] = 0;
            if ((buf[2*i + off] < (byte) '0') || 
                (buf[2*i + off] > (byte) '9'))
                throw new IOException("getUTCTime() err 2");
            period[i] = buf[2*i + off] - (int) '0';
            if ((buf[2*i + off + 1] < (byte) '0') ||
                (buf[2*i + off + 1] > (byte) '9'))
                throw new IOException("getUTCTime() err 3");
            period[i] = (period[i] * 10) + (buf[2*i + off + 1] - (int) '0');
        }

        if (period[0] < 50) {  // from rfc2459
            period[0] += 2000;
        } else {
            period[0] += 1900;
        }

        cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.set(Calendar.YEAR, period[0]);
        cal.set(Calendar.MONTH, period[1] - 1);  // months go 0-11
        cal.set(Calendar.DAY_OF_MONTH, period[2]);
        cal.set(Calendar.HOUR_OF_DAY, period[3]);
        cal.set(Calendar.MINUTE, period[4]);
        cal.set(Calendar.SECOND, period[5]);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    } 
    
    /**
     * Parses X.509v3 extensions in the certificate encoding until
     * the specified index.
     * <p />
     * @param end index of the last byte in the certificate encoding
     *        to be processed
     * @exception IOException in case of parsing problems
     */ 
    private void parseExtensions(int end) throws IOException {
        /*
         * NOTE: If one does not wish to support v3 extensions
         * at all (to save code), one can simply set badExt to
         * true and return -- the code that actually parses extensions
         * can be commented out
         */ 
        String extId = null;
        int extIdIdx = 0;
        int extIdLen = 0;
        boolean crit;
        int extValIdx = 0;
        int extValLen = 0;
        int tmp;
        
        getLen((byte) 0xa3);   // extensions start with 0xa3
        getLen(SEQUENCE_TYPE);
        while (idx < end) {
            extId = null;
            getLen(SEQUENCE_TYPE);
            extIdLen = getLen(OID_TYPE);
            extIdIdx = idx;
            idx += extIdLen;
            crit = false;
            if ((enc[idx] == 0x01) && (enc[idx + 1] == 0x01)) {
                idx += 2;
                crit = (enc[idx++] == (byte) 0xff) ? true : false;
            }
            extValLen = getLen(OCTETSTR_TYPE);
            extValIdx = idx;
            if ((enc[extIdIdx] == 0x55) && (enc[extIdIdx + 1] == 0x1d)) {
                // Do we recognize this? NOTE: id-ce is 0x55, 0x1d
                switch (enc[extIdIdx + 2] & 0xff) {
                case 0x0f:   // keyUsage = id-ce 15
                    extId = "KU";
                    if (keyUsage == -1) {
                        keyUsage = 0;
                    }

                    tmp = getLen(BITSTRING_TYPE) - 1;
                    int unused = enc[idx++]; // get unused bits in last octet
                    byte b = 0;

                    // process each bit in the bitstring starting with
                    // the most significant
                    for (int i = 0; i < ((tmp << 3) - unused); i++) {
                        if ((i % 8) == 0) {
                            b = enc[idx++];
                        }

                        if (b < 0) {
                            keyUsage |= 1 << i;
                        }

                        b = (byte) (b << 1);
                    }

                    break;
                    
                case 0x11:   // subAltName = id-ce 17
                    StringBuffer temp = new StringBuffer();
                    int start = idx + 4;
                    int length = extValLen - 4;
                    extId = "SAN";

                    /*
                     * First byte stores the type e.g. 1=rfc822Name(email), 
                     * 2=dNSName, 6=URI etc
                     */ 
                    subAltNameType = (byte) (enc[idx + 2] - 0x80);

                    switch (subAltNameType) {
                    case TYPE_EMAIL_ADDRESS:
                    case TYPE_DNS_NAME:
                    case TYPE_URI:
                        for (int i = 0; i < length; i++) {
                            temp.append((char)enc[start + i]);
                        }

                        subAltName = temp.toString();
                        break;

                    default:
                        subAltName = new byte[length];
                        for (int i = 0; i < length; i++) {
                            ((byte[])subAltName)[i] = enc[start + i];
                        }
                    }
                    
                    break;

                case 0x13:  // basicConstr = id-ce 19
                    hasBC = true;
                    extId = "BC";
                    tmp = getLen(SEQUENCE_TYPE);
                    if (tmp == 0) break;
                    // ca is encoded as an ASN boolean (default is false)
                    if ((enc[idx] == 0x01) && (enc[idx + 1] == 0x01) &&
                        (enc[idx + 2] == (byte) 0xff)) {
                        isCA = true;
                        idx += 3;
                    }

                    /*
                     * path length constraint is encoded as optional ASN
                     * integer
                     */
                    if ((enc[idx] == 0x02) && (enc[idx + 1] != 0)) {
                        tmp = getLen(INTEGER_TYPE);
                        pLenConstr = 0;
                        for (int i = 0; i < tmp; i++) {
                            pLenConstr = (pLenConstr << 16) + enc[idx + i];
                        }
                        idx += tmp;
                    } else {
                        if (isCA) pLenConstr = UNLIMITED_CERT_CHAIN_LENGTH;
                    }
                    break;
                    
                case 0x25:  // extendedKeyUsage = id-ce 37
                    extId = "EKU";
                    if (extKeyUsage == -1) {
                        extKeyUsage = 0;
                    }

                    getLen(SEQUENCE_TYPE);
                    int kuOidLen;
                    while (idx < extValIdx + extValLen) {
                        kuOidLen = getLen(OID_TYPE);
                        if ((kuOidLen == ID_KP.length + 1) &&
                            Utils.byteMatch(enc, idx, 
                                            ID_KP, 0, ID_KP.length) &&
                            (enc[idx + ID_KP.length] > 0) &&
                            (enc[idx + ID_KP.length] < 9)) {
                            extKeyUsage |= 
                                (1 << (enc[idx + ID_KP.length]));
                        } else {
                            if (crit) badExt = true;
                        }
                        idx += kuOidLen;
                    }

                    if (!crit) {
                        // ignore extended key usage if not critical
                        extKeyUsage = -1;
                    }

                    break;
                    /* 
                     * Extensions which we do not currently support include: 
                     * subjectDirectoryAttribute 0x09, 
                     * subjectKeyIdentifier 0x0e, privateKeyUsagePeriod 0x10,
                     * issuerAltName 0x12, cRLNumber 0x14, reasonCode 0x15,
                     * instructionCode 0x17, invalidityDate 0x18,
                     * deltaCRLIndicator 0x1b, issuingDistributionPoint 0x1c,
                     * certificateIssuer 0x1d, nameConstraints 0x1e,
                     * cRLDistributionPoints 0x1f, certificatePolicies 0x20,
                     * policyMappings 0x21, authorityKeyIdentifier 0x23,
                     * policyConstraints 0x24
                     */ 
                }
            }
            
            // For debugging only
	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "<Id: " + 
                               Utils.hexEncode(enc, extIdIdx, extIdLen) +
                               (crit ? ", critical, " : ", ") +
                               Utils.hexEncode(enc, extValIdx, extValLen) +
                               ">" + 
                               ((extId == null) ? " (Unrecognized)" : ""));
	    }
            
            if ((extId == null) && crit) badExt = true;

            idx = extValIdx + extValLen;
        }
        
        if (idx != end) {
            throw new IOException("Extension parsing problem");
        }

    }   // Done processing extensions

    private PublicKey parsePublicKey(int start, int len) 
        throws Exception {
        int savedIdx = start;
        int id;
        int modulusPos;
        int modulusLen;
        int exponentPos;
        int exponentLen;
        int publicKeyPos = start;
        int publicKeyLen = len;
        int size;
        int curveType = -1;

        // System.out.println("Public key: " + Utils.hexEncode(enc, start, len));
        
        size = getLen(SEQUENCE_TYPE);
        size = getLen(OID_TYPE);
        if (size == PKCS1OID.length + 1) {
            match(PKCS1OID);
            id = enc[idx++];
            match(NullSeq);
        } else {
            match(ECpublicKeyOID);
            id = EC_PUBLIC_KEY;
            size = getLen(OID_TYPE);
            // TODO: convert curve OID to curve number
            if (size == SECGOID.length + 1) {
                match(SECGOID);
                if (enc[idx++] == 0x08) 
                    curveType = ECCurve.SECP160R1;
                else
                    throw new IOException("Unsupported SECG curve");
            } else if (size == ANSIX962GFpOID.length + 1) {
                match(ANSIX962GFpOID);
                throw new IOException("Unsupported ANSI X9.62 GFp curve");
                // if (enc[idx++] == 0x07) 
                //    curveType = ECCurve.NISTP256;
            }
        }

	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
              "Public Key Algorithm: " + id);
	    }

        switch (id) {
        case RSA_ENCRYPTION:
            // Get the bit string
            getLen(BITSTRING_TYPE);
            if (enc[idx++] != 0x00) {
                throw new IOException("Bitstring error while parsing public " +
                                      "key information");
            }

            getLen(SEQUENCE_TYPE);
            size = getLen(INTEGER_TYPE);
            if (enc[idx] == (byte) 0x00) {
                // strip off the sign byte
                size--;
                idx++;
            }
            
            // Build the RSAPublicKey
            modulusPos = idx;
            modulusLen = size;

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "Modulus:  " +
                  Utils.hexEncode(enc, modulusPos, modulusLen));
            }

            idx += size;
            size = getLen(INTEGER_TYPE);
            if (enc[idx] == (byte) 0x00) {
                // strip off the sign byte
                size--;
                idx++;
            }
            exponentPos = idx;
            exponentLen = size;

            pubKey = new RSAPublicKey(enc, modulusPos, modulusLen,
              enc, exponentPos, exponentLen);

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
                  "Exponent: " +
                  Utils.hexEncode(enc, exponentPos,
                    exponentLen));
            }
            idx += size;
            break;

        case EC_PUBLIC_KEY:
            size = getLen(BITSTRING_TYPE);
            if (enc[idx] == (byte) 0x00) {
                // strip off the sign byte
                size--;
                idx++;
            }

            pubKey = new ECPublicKeyImpl(curveType);
            ((ECPublicKeyImpl) pubKey).setW(enc, idx, size);
            idx += size;
            break;

        default:
            pubKey = null;
            break;
        }
        return pubKey;
    }

    /**
     * Creates a certificate by parsing the ASN.1 DER X.509 certificate
     * encoding in the specified buffer.<BR />
     * <B>NOTE:</B> In the standard edition, equivalent functionality
     * is provided by CertificateFactory.generateCertificate(InputStream).
     * <P />
     * @param buf byte array to be read
     * @param off offset within the byte array
     * @param len number of bytes to be read
     * @return a certificate object corresponding to the DER encoding
     *         or null (in case of an encoding problem)
     * @exception IOException if there is a parsing error
     */ 
    public static X509Certificate generateCertificate(byte[] buf, int off,
            int len) throws IOException {
        /*
         * force bad parameter errors now, so later we can consider any out of
         * bounds errors to be parsing errors
         */
	int test = buf[off] + buf[len - 1] + buf[off + len - 1];

        try {
            int start = 0;
            int size = 0;
            byte[] hash = new byte[16]; // for MD5 fingerprint
            X509Certificate res = null;
            int publicKeyLen;
            int publicKeyPos;

            // Compute the MD5 fingerprint
            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(buf, off, len);
            md.digest(hash, 0, hash.length);
            
            /*
             * Create a new certificate and fill its attributes by parsing 
             * the DER encoding
             */ 
            res = new X509Certificate();

            // Prepare to parse this certificate
            res.idx = 0;
            // Set the encoding
            res.enc = new byte[len];
            System.arraycopy(buf, off, res.enc, 0, len);
            // ... and the fingerprint
            res.fp = new byte[hash.length];
            System.arraycopy(hash, 0, res.fp, 0, hash.length);
        
	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "-------- Begin Certificate -------");
	    }
            /*
             * A Certificate is a sequence of a TBSCertificate, a signature
             * algorithm identifier and the signature
             */ 
            res.getLen(SEQUENCE_TYPE);
            // Now read the TBS certificate
            res.TBSStart = res.idx;
            size = res.getLen(SEQUENCE_TYPE);
	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "-------- Begin TBSCertificate -------");
	    }

            int sigAlgIdx = res.idx + size;
            res.TBSLen = sigAlgIdx - res.TBSStart;
            
            // Store all of the TBSCertBytes for signature verification
            res.TBSCertBytes = new byte[res.TBSLen];
            System.arraycopy(buf, off + res.TBSStart, res.TBSCertBytes, 0, 
                    res.TBSLen);
            // Now parse the version
            if ((res.enc[res.idx] & 0xf0) == 0xa0) {
                res.idx++;
		if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		    Logging.report(Logging.INFORMATION,
                                   LogChannels.LC_SECURITY,
				   "Version info: " + 
				   Utils.hexEncode(res.enc, (res.idx + 1), 
						   res.enc[res.idx]));
		}
                size = (res.enc[res.idx++] & 0xff);
                if (res.idx + size > res.enc.length) { 
                    throw new IOException("Version info too long");
                }

                // Note: raw version number of 0 means version one
                res.version = (byte) (res.enc[res.idx + (size - 1)] + 1);
                res.idx += size;
            } else {
                res.version = 1;  // No explicit version value
            }
            
            // Expect the serial number coded as an integer
            size = res.getLen(INTEGER_TYPE);
            res.serialNumber = Utils.hexEncode(res.enc, res.idx, size);
            res.idx += size;
            
            // Expect the signature AlgorithmIdentifier
            byte id = res.getSigAlg();
	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "Algorithm Id: " + id);
	    }
            // Expect the issuer name
            start = res.idx;
            size = res.getLen(SEQUENCE_TYPE);
            int end = res.idx + size;
            try {
                res.issuer = res.getName(end);
		if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		    Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
				   "Issuer: " + res.issuer);
		    
		}
            } catch (Exception e) {
                throw new IOException("Could not parse issuer name");
            }
            
            // Validity is a sequence of two UTCTime values
            try {
                res.match(ValiditySeq);
                // get start time
                res.match(UTCSeq);
                res.from = getUTCTime(res.enc, res.idx);
                res.idx += UTC_LENGTH;
                // get end time
                res.match(UTCSeq);
                res.until = getUTCTime(res.enc, res.idx);
                res.idx += UTC_LENGTH;
            } catch (Exception e) {
                throw new IOException("Could not parse validity information"
                                      + "caught " + e);
            }
            
            // Expect the subject name
            start = res.idx;
            size = res.getLen(SEQUENCE_TYPE);
            end = res.idx + size;
	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "Subject: " +
			       Utils.hexEncode(res.enc, start, size));
	    }
            if (size != 0) {
                try {
                    res.subject = res.getName(end);
		    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
			Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_SECURITY,
				       "Subject: " + res.subject);
		    }
                } catch (Exception e) {
                    throw new IOException("Could not parse subject name");
                }
            }  // NOTE: the subject can be null (empty sequence) if
            // subjectAltName is present
            
            // Parse the subject public key information
	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "SubjectPublicKeyInfo follows");
	    }

            publicKeyLen = res.getLen(SEQUENCE_TYPE);
            publicKeyPos = res.idx;
            try {
                res.pubKey = res.parsePublicKey(publicKeyPos, publicKeyLen);
            } catch (Exception e) {
                throw new IOException("PublicKey parsing error " + e);
            }
            res.idx = publicKeyPos + publicKeyLen;
            if (res.idx != sigAlgIdx) {
                if (res.version < 3) { 
                    throw new IOException(
                        "Unexpected extensions in old version cert");
                } else {
                    res.parseExtensions(sigAlgIdx);
                }
            }
            
            // get the signatureAlgorithm
            res.sigAlg = res.getSigAlg();

	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       "Signature Algorithm: " + 
			       res.getSigAlgName());
	    }

            /*
             * If this is a supported signature algorithm, compute and save
             * the hash of TBSCertificate. A null TBSCertHash indicates
             * the use of an unsupported signature algorithm (see verify())
             */
            md = null;
            if (res.sigAlg == MD2_RSA) {            
        	//MD2 is not supported for the SPOTs.
                //md = MessageDigest.getInstance("MD2");
            } else if (res.sigAlg == MD5_RSA) {            
                md = MessageDigest.getInstance("MD5");
            } else if ((res.sigAlg == SHA1_RSA) || 
                    (res.sigAlg == ECDSA_WITH_SHA1)) {
                md = MessageDigest.getInstance("SHA");
            }
            
            if (md != null) {
                res.TBSCertHash = new byte[md.getDigestLength()];
                md.update(buf, off + res.TBSStart, res.TBSLen);
                md.digest(res.TBSCertHash, 0, res.TBSCertHash.length);
            }

            // get the signature
            size = res.getLen(BITSTRING_TYPE);
            if (res.enc[res.idx++] != 0x00) {
                throw new IOException("Bitstring error in signature parsing");
            }
            
            /*
             * We pad the signature to a multiple of 8-bytes before storing
             * since we only support RSA modulus lengths that are multiples
             * of 8 bytes and the two should match for decryption to succeed.
             */ 
            int sigLen = (((size - 1) + 7) >>> 3) << 3;
            /* .. but for ECC signature need not be a multiple of 8 bytes
             * XXX this is getting hackish. We ought to reuse the Signature class
             * for RSA verification too (instead of using Cipher). 
             */
            if (res.sigAlg == ECDSA_WITH_SHA1) 
                sigLen = (size - 1);
            res.signature = new byte[sigLen];
            System.arraycopy(res.enc, res.idx, res.signature, 
                             (sigLen - (size - 1)), (size - 1));
            

	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			       sigLen + "-byte signature: " + 
			       Utils.hexEncode(res.signature));
	    }
            // System.out.println("Parsed certificate ...\n" + res);
            return res;
        } catch (IndexOutOfBoundsException e) {
            throw new IOException("Bad length detected in cert DER");
        } catch (GeneralSecurityException e) {
            throw new IOException(e.toString());
        }
    }


    /**
     * Verify a chain of certificates.
     *
     * @param certs list of certificates with first being entity certificate
     *     and the last being the CA issued certificate.
     * @param keyUsage -1 to not check the key usage extension, or
     *      a key usage bit mask to check for if the extension is present
     * @param extKeyUsage -1 to not check the extended key usage extension, or
     *      a extended key usage bit mask to check for if the extension
     *      is present
     * @param certStore store of trusted CA certificates
     *
     * @return authorization path: an array of names from most trusted to
     *    least trusted from the certificate chain
     *
     * @exception CertificateException if there is an error verifying the chain
     */
    public static String[] verifyChain(Vector certs, int keyUsage,
            int extKeyUsage, CertStore certStore)
            throws CertificateException {
        X509Certificate cert;
        X509Certificate prevCert;
        PublicKey key;
        Vector keys;
        X509Certificate[] caCerts; // CA X509Certificates
        int maxPathLen = -1; // 0 means a chain of 1 so -1 means no chain
        int prevMaxPathLen;
        Vector subjectNames = new Vector();
        String[] authPath;

        // must be an enitity certificate
        cert = (X509Certificate)certs.elementAt(0);
        checkKeyUsageAndValidity(cert, keyUsage, extKeyUsage);

        for (int i = 1; ; i++) {
            // look up the public key of the certificate issurer
            caCerts = certStore.getCertificates(cert.getIssuer());
            if (caCerts != null) {
                // found a known CA no need to go on to the next cert
                if (!caCerts[0].getSubject().toUpperCase().equals(cert.getSubject().toUpperCase())) {
                    subjectNames.addElement(caCerts[0].getSubject());
                }

                break;
            }
            
            if (i >= certs.size()) {
                throw new CertificateException(cert,
                    CertificateException.UNRECOGNIZED_ISSUER);
            }

            /* Save the name of subject. */
            subjectNames.addElement(cert.getSubject());

            prevCert = cert;
            cert = (X509Certificate)certs.elementAt(i);

            /*
             * This must be a CA so the key usage always is certficate
             * signing.
             */
            checkKeyUsageAndValidity(cert,
                X509Certificate.CERT_SIGN_KEY_USAGE, extKeyUsage);

            /*
             * This is a chain, check chain link:
             * the subject of this certificate must be the issuer of
             * the previous certificate
             */ 
            if (prevCert.getIssuer().compareTo(cert.getSubject()) != 0) {
                throw new CertificateException(prevCert,
                     CertificateException.BROKEN_CHAIN);
            }

            /*
             * Check if basicConstraints are satisfied. Note a zero
             * pathLength means we should have only processed one cert
             * so far.
             */ 
            prevMaxPathLen = maxPathLen;
            maxPathLen = cert.getBasicConstraints();

            if (maxPathLen != X509Certificate.UNLIMITED_CERT_CHAIN_LENGTH &&
                maxPathLen <= prevMaxPathLen) {
                if (cert.getSubject().equals(cert.getIssuer())) {
                    /*
                     * This cert is a redundent, self signed CA cert
                     * allowed to be at the end of the chain.
                     * These certificates may version 1, so will not
                     * have extensions. So this really should be the
                     * unrecognized issuer.
                     */
                    throw new CertificateException(prevCert,
                        CertificateException.UNRECOGNIZED_ISSUER);
                }

                if (maxPathLen ==
                    X509Certificate.MISSING_PATH_LENGTH_CONSTRAINT) {

                    throw new CertificateException(cert,
                         CertificateException.UNAUTHORIZED_INTERMEDIATE_CA);
                }

                /*
                 * An intermediate CA has over granted
                 * its given authority to the previous CA in the
                 * chain.
                 */
                throw new CertificateException(cert,
                     CertificateException.CERTIFICATE_CHAIN_TOO_LONG);
            }

            /* Save the most time intensive check for last. */
            prevCert.verify(cert.getPublicKey());
        }

        for (int i = 0; i < caCerts.length; i++) {
            try {
                cert.verify(caCerts[i].getPublicKey());
            } catch (CertificateException ce) {
                /*
                 * the exception will be when we get out side of the loop,
                 * if none of the other keys work
                 */
                continue;
            }

            // check the CA key for valid dates
            try {
                caCerts[i].checkValidity();
            } catch (CertificateException ce) {
                if (ce.getReason() == CertificateException.EXPIRED) {
                    /*
                     * Change the exception reason, so the
                     * application knows that the problem is with
                     * the device and not the server.
                     */
                    throw new CertificateException(caCerts[i],
                        CertificateException.ROOT_CA_EXPIRED);
                }
            
                throw ce;
            }

            // Success
            authPath = new String[subjectNames.size()];
            for (int j = subjectNames.size() - 1, k = 0; j >= 0;
                     j--, k++) {
                authPath[k] = (String)subjectNames.elementAt(j);
            }

            return authPath;
        }

        throw new CertificateException(cert,
            CertificateException.VERIFICATION_FAILED);
    }

    /**
     * Check the key usage, extended key usage and validity of a certificate.
     *
     * @param cert certificate to check
     * @param keyUsage -1 to not check the key usage extension, or
     *      a key usage bit mask to check for if the extension is present
     * @param extKeyUsage -1 to not check the extended key usage extension, or
     *      a extended key usage bit mask to check for if the extension
     *      is present
     *
     * @exception CertificateException if there is an error
     */
    private static void checkKeyUsageAndValidity(X509Certificate cert,
            int keyUsage, int extKeyUsage) throws CertificateException {
        int certKeyUsage;

        // Check if this certificate has any bad extensions
        cert.checkExtensions();

        certKeyUsage = cert.getKeyUsage();
        if (keyUsage != -1 && certKeyUsage != -1 &&
                (certKeyUsage & keyUsage) != keyUsage) {
            throw new CertificateException(cert,
                 CertificateException.INAPPROPRIATE_KEY_USAGE);
        }            

        certKeyUsage = cert.getExtKeyUsage();
        if (extKeyUsage != -1 && certKeyUsage != -1 &&
                (certKeyUsage & extKeyUsage) != extKeyUsage) {
            throw new CertificateException(cert,
                 CertificateException.INAPPROPRIATE_KEY_USAGE);
        }            

        cert.checkValidity();
    }

    /**
     * Gets the MD5 fingerprint of this certificate.<BR />
     * <b>NOTE:</b> this implementation returns a byte array filled
     * with zeros if there is no fingerprint associated with this
     * certificate. This may happen if a null was passed to the 
     * X509Certificate constructor.
     * <P />
     * @return a byte array containing this certificate's MD5 hash
     */
    public byte[] getFingerprint() {
         byte[] res = new byte[16];
         if (fp != null) System.arraycopy(fp, 0, res, 0, res.length);
         return res;
    }

    /**
     * Gets the name of this certificate's issuer. <BR />
     * <B>NOTE:</B> The corresponding method in the standard edition
     * is getIssuerDN() and returns a Principal.
     * <P />
     * @return a string containing this certificate's issuer in
     * user-friendly form
     */
    public String getIssuer() {
        return issuer;
    }

    /**
     * Gets the name of this certificate's subject. <BR />
     * <B>NOTE:</B> The corresponding method in the standard edition
     * is getSubjectDN() and returns a Principal. 
     * <P />
     * @return a string containing this certificate's subject in
     * user-friendly form
     */
    public String getSubject() {
        return subject;
    }
    
    /**
     * Gets the NotBefore date from the certificate's validity period.
     * <P />
     * @return a date before which the certificate is not valid
     */ 
    public long getNotBefore() {
        return from;
    }

    /**
     * Gets the NotAfter date from the certificate's validity period.
     *
     * @return a date after which the certificate is not valid (expiration
     * date)                                            
     */ 
    public long getNotAfter() {
        return until;
    }

    /**
     * Checks if a certificate has any (version 3) extensions that 
     * were not properly processed and continued use of this certificate
     * may be inconsistent with the issuer's intent. This may happen, for
     * example, if the certificate has unrecognized critical extensions. 
     *
     * @exception CertificateException with a reason ofr BAD_EXTENSIONS if
     *    there are any bad extensions
     */ 
    public void checkExtensions() throws CertificateException {
        if (badExt) {
            throw new CertificateException(this,
                CertificateException.BAD_EXTENSIONS);
        }
    }
    
    /** 
     * Checks if the certificate is currently valid. It is if the
     * current date and time are within the certificate's validity
     * period.
     *
     * @exception CertificateException with a reason of 
     *   EXPIRED or NOT_YET_VALID
     */ 
    public void checkValidity() throws CertificateException {
        checkValidity(System.currentTimeMillis());
    }
    
    /** 
     * Checks if the certificate is valid on the specified time. It is
     * if the specified time is within the certificate's validity
     * period. <BR />
     * <B>NOTE:</B> The standard edition provides a method with this
     * name but it throws different types of exceptions rather than
     * returning error codes.
     * <P />
     * @param time the time in milliseconds for which a certificate's
     * validity is to be checked
     *
     * @exception CertificateException with a reason of 
     *   EXPIRED or NOT_YET_VALID
     */ 
    public void checkValidity(long time) throws CertificateException {
        if (time < from) {
            throw new CertificateException(this,
                                           CertificateException.NOT_YET_VALID);
        }

        if (time > until) {
            throw new CertificateException(this,
                                           CertificateException.EXPIRED);
        }
    }

    /**
     * Get the type of the <CODE>Certificate</CODE>.
     * @return The type of the <CODE>Certificate</CODE>;
     * the value MUST NOT be <CODE>NULL</CODE>.
     */
    public String getType() {
        return "X.509";
    }

    /**
     * Gets the public key from this certificate.
     * <P />
     * @return the public key contained in the certificate
     *
     * @exception CertificateException if public key is not a supported type
     *            (could not be parsed).
     */
    public PublicKey getPublicKey() throws CertificateException {
        if (pubKey == null) {
            throw new CertificateException(this,
                CertificateException.UNSUPPORTED_PUBLIC_KEY_TYPE);
        }

        return pubKey;
    }

    /**
     * Gets the raw X.509 version number of this certificate. Version 1 is 0.
     *
     * @return the X.509 logic version number (1, 2, 3) of the certificate
     */
    public String getVersion() {
        return Integer.toString(version);
    }

    /**
     * Gets the certificate constraints path length from the 
     * <code>BasicConstraints</code> extension. <P />
     * 
     * The <code>BasicConstraints</code> extension identifies whether the
     * subject of the certificate is a Certificate Authority (CA) and how
     * deep a certification path may exist through the CA. The
     * <code>pathLenConstraint</code> field (see below) is meaningful only 
     * if <code>cA</code> is set to TRUE. In this case, it gives the maximum
     * number of CA certificates that may follow this certificate in a 
     * certification path. A value of zero indicates that only an end-entity
     * certificate may follow in the path. <P />
     * 
     * Note that for RFC 2459 this extension is always marked critical
     * if <code>cA</code> is TRUE, meaning this certificate belongs to a 
     * Certificate Authority. <P />
     * 
     * The ASN.1 definition for this is:
     * <PRE>
     *  BasicConstraints ::= SEQUENCE {
     *        cA                  BOOLEAN DEFAULT FALSE,
     *        pathLenConstraint   INTEGER (0..MAX) OPTIONAL 
     *  }
     *  </PRE>
     * 
     * @return MISSING_PATH_LENGTH_CONSTRAINT if the
     * <code>BasicConstraints</code> extension is absent or the subject
     * of the certificate is not a CA. If the subject of the certificate
     * is a CA and <code>pathLenConstraint</code> does not appear, 
     * <code>UNLIMITED_CERT_CHAIN_LENGTH</code> is returned to indicate that
     * there is no limit to the allowed length of the certification path.
     * In all other situations, the actual value of the 
     * <code>pathLenConstraint</code> is returned.
     */
    public int getBasicConstraints() {
        if (isCA) {
            return pLenConstr;
        } else {
            return MISSING_PATH_LENGTH_CONSTRAINT;
        }
    }
    
    /**
     * Gets a 32-bit bit vector (in the form of an integer) in which
     * each position represents a purpose for which the public key in
     * the certificate may be used (iff that bit is set). The correspondence
     * between bit positions and purposes is as follows: <BR />
     * <TABLE>
     * <TR><TD>digitalSignature</TD> <TD>0</TD> </TR>
     * <TR><TD>nonRepudiation</TD>   <TD>1</TD> </TR>
     * <TR><TD>keyEncipherment</TD>  <TD>2</TD> </TR>
     * <TR><TD>dataEncipherment</TD> <TD>3</TD> </TR>
     * <TR><TD>keyAgreement</TD>     <TD>4</TD> </TR>
     * <TR><TD>keyCertSign</TD>      <TD>5</TD> </TR>
     * <TR><TD>cRLSign</TD>          <TD>6</TD> </TR>
     * <TR><TD>encipherOnly</TD>     <TD>7</TD> </TR>
     * <TR><TD>decipherOnly</TD>     <TD>8</TD> </TR>
     * </TABLE>
     * <P />
     * @return a bitvector indicating approved key usage of the certificate
     * public key, -1 if a KeyUsage extension is not present.
     */ 
    public int getKeyUsage() {
        return keyUsage;
    }

    /**
     * Gets a 32-bit bit vector (in the form of an integer) in which
     * each position represents a purpose for which the public key in
     * the certificate may be used (iff that bit is set). The correspondence
     * between bit positions and purposes is as follows: <BR />
     * <TABLE>
     * <TR><TD>serverAuth</TD>       <TD>1</TD> </TR>
     * <TR><TD>clientAuth</TD>       <TD>2</TD> </TR>
     * <TR><TD>codeSigning</TD>      <TD>3</TD> </TR>
     * <TR><TD>emailProtection</TD>  <TD>4</TD> </TR>
     * <TR><TD>ipsecEndSystem</TD>   <TD>5</TD> </TR>
     * <TR><TD>ipsecTunnel</TD>      <TD>6</TD> </TR>
     * <TR><TD>ipsecUser</TD>        <TD>7</TD> </TR>
     * <TR><TD>timeStamping</TD>     <TD>8</TD> </TR>
     * </TABLE>
     * <P />
     * @return a bitvector indicating extended usage of the certificate
     * public key, -1 if a critical extendedKeyUsage extension is not present.
     */ 
    public int getExtKeyUsage() {
        return extKeyUsage;
    }

    /**
     * Gets the type of subject alternative name.
     *
     * @return type of subject alternative name
     */
    public int getSubjectAltNameType() {
        return subAltNameType;
    }

    /**
     * Gets the subject alternative name or null if it was not in the 
     * certificate.
     *
     * @return type of subject alternative name or null
     */
    public Object getSubjectAltName() {
        return subAltName;
    }

    /**
     * Gets the printable form of the serial number of this
     * <CODE>Certificate</CODE>. 
     * If the serial number within the <CODE>certificate</CODE>
     * is binary is should be formatted as a string using
     * hexadecimal notation with each byte represented as two
     * hex digits separated byte ":" (Unicode x3A).
     * For example,  27:56:FA:80.
     * @return A string containing the serial number
     * in user-friendly form; <CODE>NULL</CODE> is returned
     * if there is no serial number.
     */
    public String getSerialNumber() {
        return serialNumber;
    }
    
    /**
     * Checks if this certificate was signed using the private key
     * corresponding to the specified public key.
     *
     * @param k public key to be used for verifying certificate signature
     *
     * @exception CertificateException if there is an error
     */ 
    public void verify(PublicKey k) throws CertificateException {
        // System.out.println("*** VERIFICATION w/ " + getSigAlgName() + " ***");
        boolean validated = false;
        Signature sig = null;
        
        try {
            sig = Signature.getInstance(getSigAlgName());
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException("Issuer uses unsupported" +
                    "signature algorithm", this,
                    CertificateException.VERIFICATION_FAILED);
        }
        
        try {
            sig.initVerify(k);
        } catch (InvalidKeyException e) {
            throw new CertificateException("Verification key type is " +
                    "inappropriate for chosen signature algorithm", this,
                    CertificateException.VERIFICATION_FAILED);
        }
        
        try {
            sig.update(TBSCertBytes, 0, TBSCertBytes.length);
            validated = sig.verify(signature, 0, signature.length);
        } catch (Exception ex) {
            System.out.println("Caught " + ex + " in verify.");
        }
        
        if (!validated) {
            throw new CertificateException("Signature verification failed",
                    this, CertificateException.VERIFICATION_FAILED);
        } else {
            // System.out.println("*** VERIFICATION SUCCEEDED ***");
        }        
    }
     
    /**
     * Gets the name of the algorithm used to sign the certificate.
     * <P />
     * @return the name of signature algorithm
     */ 
    public String getSigAlgName() {
        /* 
         * These are ordered to maximize the likelihood of an
         * early match, md5WithRSA seems the most common
         */ 
        if (sigAlg == MD5_RSA) 
            return ("MD5withRSA");
        else if (sigAlg == MD2_RSA) 
            return ("MD2withRSA");
        else if (sigAlg == SHA1_RSA) 
            return ("SHA1withRSA");
        else if (sigAlg == NONE)
            return ("None");
        else if (sigAlg == MD4_RSA)
            return ("MD4withRSA");
        else if (sigAlg == ECDSA_WITH_SHA1)
            return ("SHA1withECDSA");
        else
            return ("Unknown (" + sigAlg + ")");
    }
    /** Array of purpose strings describing key usage role. */
    private static final String[] KEY_USAGE = {
        "digitalSignature", // 0
        "nonRepudiation",   // 1
        "keyEncipherment",  // 2
        "dataEncipherment", // 3
        "keyAgreement",     // 4
        "keyCertSign",      // 5
        "cRLSign",          // 6
        "encipherOnly",     // 7
        "decipherOnly",     // 8
        // below are for the extended key usage extension
        "9", "10", "11", "12", "13", "14", "15", "16", // 9-16
        "serverAuth",       // 17
        "clientAuth",       // 18
        "codeSigning",      // 19
        "emailProtection",  // 20
        "ipsecEndSystem",   // 21
        "ipsecTunnel",      // 22
        "ipsecUser",        // 23
        "timeStamping"      // 24
    };
    
    /**
     * Converts a Date object to a string containing the corresponding 
     * date.<br />
     * <b>NOTE:</b> This is here only because the J2ME date class does not
     * implement toString() in any meaningful way.
     * <p />
     * @param date Date object to be converted
     * @return a string representation of the Date object in
     *         the form "month/day/year hour:min:sec"
     */ 
    private static String date2str(Date date) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(date);
        String d = (c.get(Calendar.MONTH) + 1) + "/" +
            c.get(Calendar.DAY_OF_MONTH) + "/" +
            c.get(Calendar.YEAR) + " " +
            c.get(Calendar.HOUR_OF_DAY) + ":" +
            c.get(Calendar.MINUTE) + ":" + 
            c.get(Calendar.SECOND);
        return d;
    }

    /**
     * Returns a string representation of this certificate.
     * <p />
     * @return a human readable string repesentation of this certificate
     */
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        
        tmp.append("[Type: ");
        tmp.append(getType());
        tmp.append("v");
        tmp.append(version);

        tmp.append("\n");
        tmp.append("Serial number: ");
        tmp.append(serialNumber);

        tmp.append("\n");
        tmp.append("Issuer: ");
        tmp.append(issuer);
        
        tmp.append("\n");
        tmp.append("Subject: ");
        tmp.append(subject);

        tmp.append("\n");
        tmp.append("Valid from ");
        tmp.append(date2str(new Date(getNotBefore())));
        tmp.append(" GMT until ");
        tmp.append(date2str(new Date(getNotAfter())));
        tmp.append(" GMT");

        tmp.append("\n");        
        tmp.append(pubKey.toString());

        // tmp.append("\n");
        // tmp.append(TBSCertificate hash: ");
        // tmp.append(TBSCertHash == null ?
        //           "null" : Utils.hexEncode(TBSCertHash));

        tmp.append("\n");
        tmp.append("Signature Algorithm: ");
        tmp.append(getSigAlgName());

        if (subAltName != null) {
            tmp.append("\n");
            tmp.append("SubjectAltName: ");
            tmp.append(subAltName);
            tmp.append("(type ");
            tmp.append(subAltNameType);
            tmp.append(")");
        }

        if (keyUsage != -1) {
            tmp.append("\n");
            tmp.append("KeyUsage:");
            int t = (int) keyUsage;
            for (int i = 0; i < KEY_USAGE.length; i++) {
                if ((t & 0x01) == 0x01) {
                    tmp.append(" ");
                    tmp.append(KEY_USAGE[i]);
                }

                t = t >>> 1;
            }
        }

        if (hasBC) {
            tmp.append("\n");
            tmp.append("BasicConstraints: ");
            tmp.append(isCA ? "is a CA" : "not a CA");
            tmp.append(" (pathLengthConstraint ");
            if ((pLenConstr == MISSING_PATH_LENGTH_CONSTRAINT) ||
                    (pLenConstr == UNLIMITED_CERT_CHAIN_LENGTH)) {
                tmp.append("absent");
            } else {
                tmp.append(pLenConstr);
            }

            tmp.append(")");
        }

        // tmp.append("\n");
        // tmp.append("MD5 Fingerprint: ");
        // tmp.append(Utils.hexEncode(fp));
        
        if (signature != null) {
            tmp.append("\n");
            tmp.append("Signature: ");
            tmp.append(Utils.hexEncode(signature));            
        } 
        tmp.append("\n");
        tmp.append("]");
        return tmp.toString();
    }
}
