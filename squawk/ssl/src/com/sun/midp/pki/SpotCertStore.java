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

import com.sun.spot.util.Utils;

import javax.microedition.pki.CertificateException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class SpotCertStore implements CertStore {
    
    private static final byte CURRENT_VERSION = 1;
    
    private static final int VERSION_OFFSET = 0; //byte
    private static final int NUM_CERTS_OFFSET = 1; //int
    private static final int CERTS_OFFSET = 5;
    private static final int MAX_CERTSTORE_SIZE = (8*1024 - 200);
    public static final String PERSONAL_CERT_NICKNAME = "*MyCert";
    
    /* A mapping from subject to vector of certinfos with that subject
     * certInfosBySubject: subject ---> Vector of CertInfos which have 
     *                                     the given subject name */
    private Hashtable certInfosBySubject;

    /* a mapping from nickname to (subject, index) pairs stored as a Vector
     * certInfosByNickname: nickname ---> {subject, index} such that 
     *           certInfosBySubject.get(subject).elementAt(index) is the
     *           CertInfo with the given nickname */      
    private Hashtable certInfoByNickname;

    /** Creates a new and empty SpotCertStore */
    public SpotCertStore() {
        certInfosBySubject = new Hashtable();
        certInfoByNickname = new Hashtable();
    }

    /** Create a new SpotCertStore from a serialized SpotCertStore
     * @param rawCertStore serialized SpotCertStore
     * @param offset the offset at which to start reading
     * @throws IOException if rawCertStore is corrupted
     */
    public SpotCertStore(byte[] rawCertStore, int offset)
            throws IOException {
        msg("Deserializing CertStore");
        byte ver = rawCertStore[offset + VERSION_OFFSET];
        msg("offset: " + (offset + VERSION_OFFSET));
        if (ver != CURRENT_VERSION) {
            throw new IOException("PublicCertStore version " + ver +
                    " does not match serializer version " + CURRENT_VERSION);
        } else {
            int numCerts = Utils.readLittleEndInt(rawCertStore,
                    offset +  NUM_CERTS_OFFSET);
            msg("Deserializing " + numCerts + " certs");
            certInfosBySubject = new Hashtable(numCerts);
            certInfoByNickname = new Hashtable(numCerts);
            int strAddr = offset + CERTS_OFFSET;
            CertInfo certInfo;
            Vector v;
            for (int i=0; i<numCerts; i++) {
                certInfo = new CertInfo(rawCertStore, strAddr);
                strAddr += certInfo.getBytesRead();
                addCert(certInfo.getNickname(), certInfo);
            }
        }
    }

    /* Adds a cert with the given nickname and flags
     * @param nickname the nickname of the cert to be added
     * @param flags the trust flags of the cert to be added
     * @param cert the cert to add
     * @throws CertificateException if the certificate could not be parsed
     */
    public void addCert(String nickname, String flags, X509Certificate cert)
            throws CertificateException {
        addCert(nickname, new CertInfo(nickname, flags, cert));
    }
    
    /* Adds a CertInfo with the given nickname
     * @param certInfo the certInfo to add to the cert store
     */
    private void addCert(String nickname, CertInfo certInfo) {
System.out.println("nick:" + nickname);
System.out.println("  subj:" + certInfo.getSubjectName());
        msg("Adding " + nickname);
        removeCert(nickname);
        certInfoByNickname.put(nickname, certInfo);
        Vector v = (Vector)certInfosBySubject.get(certInfo.getSubjectName());
        if (v == null) {
            v = new Vector();
            certInfosBySubject.put(certInfo.getSubjectName(), v);
        }
        v.addElement(certInfo);
    }

    /** Removes the cert corresponding to the given nickname
     * @param nickname the nick of the cert to remove
     * @return false if nickname wasn't found, true otherwise
     */
    public boolean removeCert(String nickname) {
        Object o = certInfoByNickname.get(nickname);
        if (o != null) {
            msg("Removing " + nickname);
            CertInfo ci = (CertInfo)o;
            String subjName = ci.getSubjectName();
            Vector v = (Vector)(certInfosBySubject.get(subjName));
            v.removeElement(ci);
            if (v.isEmpty()) certInfosBySubject.remove(subjName);
            certInfoByNickname.remove(nickname);
            return true;
        } else {
            return false;
        }
    }
        
    /** Returns the trust flags of the cert corresponding to the given nickname
     * @param nickname
     * @return string of trust flags
     */
    public String getTrustFlagsByNickname(String nickname) {
        Object val = certInfoByNickname.get(nickname);
        return val == null ? null : ((CertInfo)val).getFlags();
    }
    
    /**
     * Returns the X509Certificate corresponding to the given nickname
     * @param nickname
     * @return corresponding X509Certificate
     */
    public X509Certificate getCertByNickname(String nickname) {
        Object val = certInfoByNickname.get(nickname);
        return ((val == null) ? null : ((CertInfo)val).getCertificate());
    }
    
    /**
     * Returns an enumeration of the nicknames
     * @return enumeration of nicknames
     */
    public Enumeration getNicknames() {
        return certInfoByNickname.keys();
    }
    
    /**
     * Deletes all certificates in the SPOTs cert store
     */
//    public void clearAll() {
//        msg("Clearing all certs from cert store");
//        certInfoByNickname.clear();
//        certInfosBySubject.clear();
//    }

    /**
     * Deletes all certificates not beloging to the SPOT itself from cert store
     */
    public void clear() {
        msg("Clearing all certs except personal cert from cert store");
        Enumeration nicknames = getNicknames();
        String nick;
        while (nicknames.hasMoreElements()) {
            nick = (String)(nicknames.nextElement());
            if (!nick.equals(PERSONAL_CERT_NICKNAME)) {
                removeCert(nick);
            }
        }        
    }

    /**
     * Returns the certificate(s) corresponding to a subject name string.
     *
     * @param subjectName subject name of the certificate in printable form.
     * @return corresponding certificates or null (if not found)
     */
    public X509Certificate[] getCertificates(String subjectName) {
        Vector certsList;
        X509Certificate[] certs;
        
        certsList = (Vector)certInfosBySubject.get(subjectName);
        if (certsList == null) {
            return null;
        }
        
        certs = new X509Certificate[certsList.size()];
        for (int i = 0; i < certsList.size(); i++)
            certs[i] = ((CertInfo)certsList.elementAt(i)).getCertificate();
        
        return certs;
    }

    /**
     * Returns a byte representation of the SpotCertStore
     * @return serialized SpotCertStore
     * @throws Exception if the SpotCertStore could not be serialized
     */
    public byte[] asByteArray() throws Exception {
        int certStoreLength = 0;
        int numCerts = certInfoByNickname.size();
        msg("Serializing " + numCerts + " certs");
        Vector serializedCerts = new Vector(numCerts);
        for (Enumeration e=certInfoByNickname.elements(); e.hasMoreElements();) {
            byte[] cert = ((CertInfo)e.nextElement()).asByteArray();
            certStoreLength += cert.length;
            if (certStoreLength > MAX_CERTSTORE_SIZE) {
                msg("CertStore size too large");
                throw new Exception("CertStore size too large.");
            }
            serializedCerts.addElement(cert);
        }
        /* allocate enough for version, # of certs, and space for each cert */
        byte[] rawCertStore = new byte[1 + 4 + certStoreLength];
        rawCertStore[VERSION_OFFSET] = CURRENT_VERSION;
        Utils.writeLittleEndInt(rawCertStore, NUM_CERTS_OFFSET, numCerts);
        int offset = CERTS_OFFSET;
        for (int i=0; i < numCerts; i++) {
            byte[] cert = (byte[])(serializedCerts.elementAt(i));
            System.arraycopy(cert, 0, rawCertStore, offset, cert.length);
            offset += cert.length;
        }
        return rawCertStore;
    }
    
    private void msg(String msg) {
	    // HACK Removed spam
//        System.out.println("[SpotCertStore] " + msg);
    }

    public String toString() {
        Enumeration nicknames = getNicknames();
        StringBuffer sb = new StringBuffer();
        X509Certificate x;
        int nicknameLength = 14;
        int subjectLength = 24;
        int issuerLength = 19;
        
        if (!nicknames.hasMoreElements()) {
            sb.append("Keystore is empty.");
            return sb.toString();
        }
        
        //int flagsLength = 5;
        sb.append(padString("Nickname", nicknameLength));
        sb.append(padString("Subject", subjectLength));
        sb.append(padString("Issuer", issuerLength));
        //sb.append(padString("Flags", flagsLength));
        sb.append("Flags");
        sb.append("\n");
        while (nicknames.hasMoreElements()) {
            String nic = 
                    (String)(nicknames.nextElement());
            x = getCertByNickname(nic);
            sb.append(padString(nic, nicknameLength));
            sb.append(padString(x.getSubject(), subjectLength));
            sb.append(padString(x.getIssuer(), issuerLength));
            //sb.append(padString(getTrustFlagsByNickname(nic), flagsLength));
            sb.append(getTrustFlagsByNickname(nic));
            sb.append("\n");
        }
        return sb.toString();
    }

    private String padString(String s, int length) {
        StringBuffer sb = new StringBuffer();
        sb.append(s);
        for (int i=0; i<length-s.length(); i++)
            sb.append(" ");
        return sb.toString();
    }
}