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
import com.sun.spot.security.PublicKey;
import com.sun.spot.util.Utils;
import com.sun.spot.security.implementation.Util;

import javax.microedition.pki.CertificateException;

import java.io.IOException;
import java.util.Date;

/* @author: Iljya Kalai */
class CertInfo {
    
    /* # of bytes read when certinfo was deserialized */
    private int bytesRead;
    
    /* Serialization version */
    private int version;
    private String nickname;
    private String certId;
    private String subjectName;
    private String issuerName;
    private long validFrom;
    private long validTo;
    private PublicKey key;
    private int flags;
    
    private String flagsString;
    
    private static final byte W_TRUST_FLAG = 1;
    private static final byte P_TRUST_FLAG = 2;
    private static final byte O_TRUST_FLAG = 4;
    private static final byte S_TRUST_FLAG = 8;
    
    private static final int VERSION_OFFSET = 0; //byte
    private static final int VALID_FROM_OFFSET = 1; //long
    private static final int VALID_TO_OFFSET = 9; //long
    private static final int FLAGS_OFFSET = 17; //byte
    
    public static final byte CURRENT_CERT_INFO_VERSION = 1;
    
    /* defines how long the static section of the serialized certinfo is */
    private static final int STATIC_SECTION_LENGTH = 18;
    
    public CertInfo(String nickname, String flags, X509Certificate cert)
            throws CertificateException, IllegalArgumentException {
        this.nickname = nickname;
        this.certId = cert.getSerialNumber();
        this.subjectName = cert.getSubject();
        this.issuerName = cert.getIssuer();
        this.validTo = cert.getNotAfter();
        this.validFrom = cert.getNotBefore();
        this.key = cert.getPublicKey();
        this.flagsString = new String(flags);
        for (int i=0; i<flags.length(); i++) {
            switch(flags.charAt(i)) {
                case 'W':
                case 'w': this.flags |= W_TRUST_FLAG; break;
                case 'P':
                case 'p': this.flags |= P_TRUST_FLAG; break;
                case 'O':
                case 'o': this.flags |= O_TRUST_FLAG; break;
                case 'S':
                case 's': this.flags |= S_TRUST_FLAG; break;
                default:
                    throw new IllegalArgumentException("Unrecognized trust flags");
            }
        }
    }
    
    public CertInfo(byte[] rawCert) throws IOException {
        createCertInfo(rawCert, 0);
    }
    
    public CertInfo(byte[] rawCert, int offset) throws IOException {
        createCertInfo(rawCert, offset);
    }
    
    private void createCertInfo(byte[] rawCert, int offset)
            throws IOException {
        version = rawCert[offset + VERSION_OFFSET];
        if (version != CURRENT_CERT_INFO_VERSION) {
            throw new IOException("Cert version " + version +
                    " does not match current version " + CURRENT_CERT_INFO_VERSION);
        } else {
            parseCurrentFormat(rawCert, offset);
        }
    }
    
    private void parseCurrentFormat(byte[] rawCert, int offset) 
            throws IOException {
        validFrom = Utils.readLittleEndLong(rawCert, offset + VALID_FROM_OFFSET);
        validTo = Utils.readLittleEndLong(rawCert, offset + VALID_TO_OFFSET);
        flags = Utils.readLittleEndInt(rawCert, offset + FLAGS_OFFSET);
        flagsString = createFlagsString(flags);
        
        int strAddr = offset + STATIC_SECTION_LENGTH;
        
        // read nickname
        int len = Utils.readLittleEndShort(rawCert, strAddr);
        strAddr += 2;
        nickname = new String(rawCert, strAddr, len);
        strAddr += len;
        
        //read subject name
        len = Utils.readLittleEndShort(rawCert, strAddr);
        strAddr += 2;
        subjectName = new String(rawCert, strAddr, len);
        strAddr += len;
        
        //read issuer name
        len = Utils.readLittleEndShort(rawCert, strAddr);
        strAddr += 2;
        issuerName = new String(rawCert, strAddr, len);
        strAddr += len;
        
        //read serial number
        len = Utils.readLittleEndShort(rawCert, strAddr);
        strAddr += 2;
        certId = new String(rawCert, strAddr, len);
        strAddr += len;
        
        //read public key
        len = Utils.readLittleEndShort(rawCert, strAddr);
        strAddr += 2;
        try {
            key = (PublicKey)KeySerializer.deserialize(rawCert, strAddr);
        } catch (GeneralSecurityException e) {
            throw new IOException("Key in rawCert is corrupt");
        }
        strAddr += len;
        
        bytesRead = strAddr - offset;
    }
    
    public String getCertId() {
        return certId;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public String getIssuerName() {
        return issuerName;
    }
    
    public long getValidFrom() {
        return validFrom;
    }
    
    public long getValidTo() {
        return validTo;
    }
    
    public PublicKey getKey() {
        return key;
    }
    
    public String getKeyType() {
        return key.getAlgorithm();
    }
    
    public String getFlags() {
        return flagsString;
    }
    
    public byte[] asByteArray() throws Exception {
        byte[] rawKey = KeySerializer.serialize(key);
        byte[] b = new byte[STATIC_SECTION_LENGTH +
                2 + nickname.length() +
                2 + subjectName.length() +
                2 + issuerName.length() +
                2 + certId.length() +
                2 + rawKey.length];
        
        /* version */
        b[VERSION_OFFSET] = CURRENT_CERT_INFO_VERSION;
        /* valid from */
        Utils.writeLittleEndLong(b, VALID_FROM_OFFSET, validFrom);
        /* valid to */
        Utils.writeLittleEndLong(b, VALID_TO_OFFSET, validTo);
        /* flags */
        Utils.writeLittleEndInt(b, FLAGS_OFFSET, flags);
        
        int strAddr = STATIC_SECTION_LENGTH;
        
        /* nickname */
        strAddr += writeString(b, strAddr, nickname);
        /* subject name */
        strAddr += writeString(b, strAddr, subjectName);
        /* issuer name */
        strAddr += writeString(b, strAddr, issuerName);
        /* cert id */
        strAddr += writeString(b, strAddr, certId);
        /* public key */
        Utils.writeLittleEndShort(b, strAddr, (short)rawKey.length);
        strAddr += 2;
        System.arraycopy(rawKey, 0, b, strAddr, rawKey.length);
        
        return b;
    }
    
    private int writeString(byte[] dest, int offset, String value) {
        byte[] valueBytes = value.getBytes();
        Utils.writeLittleEndShort(dest, offset, valueBytes.length);
        System.arraycopy(valueBytes, 0, dest, offset + 2, valueBytes.length);
        offset += valueBytes.length;
        return valueBytes.length + 2;
    }
    
    private String createFlagsString(int flags) {
        StringBuffer sb = new StringBuffer();
        if ((flags & O_TRUST_FLAG) != 0) sb.append("o");
        if ((flags & W_TRUST_FLAG) != 0) sb.append("w");
        if ((flags & P_TRUST_FLAG) != 0) sb.append("p");
        if ((flags & S_TRUST_FLAG) != 0) sb.append("s");
        return sb.toString();
    }
    
    public int getBytesRead() {
        return bytesRead;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[CertInfo\n");
        sb.append("Nickname : " + nickname);
        sb.append("\n");
        sb.append("certId   : " + certId);
        sb.append("\n");
        sb.append("subject  : " + subjectName);
        sb.append("\n");
        sb.append("issuer   : " + issuerName);
        sb.append("\n]\n");
        
        return sb.toString();
    }
    
    /**
     * Creates an {@link X509Certificate} using the given cert information.
     *
     * @param certInfo cert information
     * @return X509 certificate
     */
    public X509Certificate getCertificate() { 
        try {
            X509Certificate c = new X509Certificate((byte)1,
                    getCertId(), 
                    getSubjectName(),
                    getIssuerName(),
                    getValidFrom(),
                    getValidTo(),
                    getKey(),
                    null, //we don't use finger prints
                    0);
            // System.out.println("Converted to " + c.toString() + " ...");
            return c;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void msg(String s) {
        System.out.println("[CertInfo] " + s);
    }
}
