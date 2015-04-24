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

package com.sun.spot.peripheral;

import com.sun.midp.pki.KeySerializer;
import com.sun.midp.pki.SpotCertStore;
import com.sun.midp.pki.X509Certificate;
import com.sun.spot.security.implementation.ECKeyImpl;
import com.sun.spot.security.implementation.ECPrivateKeyImpl;
import com.sun.spot.security.implementation.ECPublicKeyImpl;
import com.sun.spot.security.implementation.ecc.ECCurve;
import com.sun.spot.util.Utils;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import java.io.IOException;

/**
 * Each Spot reserves some flash memory for the cryptographic information.
 * The TrustManager class converts that information to and fro between a
 * raw byte array and structured Java data.<br><br>
 *
 * To obtain the current trust manager use:<br><br>
 *
 * <code>
 * Spot.getInstance().getTrustManager()
 * </code>
 *
 *
 * @author Iljya Kalai
 */
public class TrustManager {
    
    /**
     * The TrustManager page is split up into two sections: the static section
     * and the data section. The static section is of fixed length and contains
     * properties, such as the TrustManager page version. The data section is
     * of variable length and contains TrustManager data, such as the spot
     * private key.
     */
    private boolean wasLoaded;
    
    private ECPrivateKeyImpl spotPrivateKey;
    private byte[] spotCertificateBytes;
    private X509Certificate spotCertificate;
    private SpotCertStore certStore;
    
    public static final int STATIC_SECTION_LENGTH = 1;
    
    public static final int TRUST_MANAGER_VERSION_OFFSET = 0; // byte
    public static final int TRUST_MANAGER_DATA_OFFSET    = 1; // short
    
    public static final byte CURRENT_TRUST_MANAGER_VERSION = 3;
    private static final int MAX_TRUSTMANAGER_SIZE       = 8 * 1024;
    
    protected static TrustManager singleton;
    /**
     * Get the trust manager held in flash
     * @return the trust manager
     */
    public static TrustManager getTrustManager() {
        if (singleton == null) {
            singleton = new TrustManager();
        }
        return singleton;
    }
    
    /**
     * Write a TrustManager into the flash.
     */
    public void flashTrustManager() {
    }
    
    /**
     * Create a newly initialized config page
     * This constructor is for system use only.
     * Please use Spot.getInstance().getTrustManager()
     */
    public TrustManager() {
        initializeTrustManager();
    }
    
    /**
     * Create a TrustManager page from a byte array
     * @param rawTrustManager Byte array to use as input
     */
    public TrustManager(byte[] rawTrustManager) {
        int ver = rawTrustManager[TRUST_MANAGER_VERSION_OFFSET];
        msg("ver: " + ver);
        msg("current: " + CURRENT_TRUST_MANAGER_VERSION);
        if (ver == CURRENT_TRUST_MANAGER_VERSION) {
            try {
                parseCurrentFormat(rawTrustManager, TRUST_MANAGER_DATA_OFFSET);
            } catch (Exception e) {
                msg("Parsing of TrustManager failed." +
                        " Initializing default TrustManager");
                e.printStackTrace();
                initializeTrustManager();
            }
        } else {
            msg("Version mismatch - initializing with default values");
            initializeTrustManager();
        }
    }
    
    private void parseCurrentFormat(byte[] rawTrustManager, int offset) throws Exception {
        
        int strAddr = offset;
        msg("[parsing] private key - offset: " + strAddr);
        /* Read spots private key */
        int len = Utils.readLittleEndShort(rawTrustManager, strAddr);
        strAddr += 2;
        if (len != 0) {
            spotPrivateKey = (ECPrivateKeyImpl)KeySerializer.deserialize(
                    rawTrustManager, strAddr);
            strAddr += len;
        } else {
            spotPrivateKey = null;
        }
        msg("[parsing] cert - offset: " + strAddr);
        /* Read spots certificate */
        len = Utils.readLittleEndShort(rawTrustManager, strAddr);
        strAddr += 2;
        if (len != 0) {
            try {
                spotCertificateBytes = new byte[len];
                System.arraycopy(rawTrustManager, strAddr,
                        spotCertificateBytes, 0, len);
                spotCertificate = X509Certificate.generateCertificate(
                        spotCertificateBytes, 0, spotCertificateBytes.length);
                strAddr += len;
            } catch (Exception e) {
                System.err.println("Spot certificate is corrupted and" +
                        " can not be loaded");
                e.printStackTrace();
                spotCertificateBytes = new byte[0];
            }
        } else {
            spotCertificateBytes = new byte[0];
        }
        
        msg("[parsing] cert store - offset: " + strAddr);
        /* Read public certstore */
        len = Utils.readLittleEndShort(rawTrustManager, strAddr);
        strAddr += 2;
        if (len != 0) {
            try {
                certStore = new SpotCertStore(rawTrustManager, strAddr);
            } catch (Exception e) {
                System.err.println("CertStore is corrupted and" +
                        " can not be loaded");
                e.printStackTrace();
                certStore = new SpotCertStore();
            }
        } else {
            certStore = new SpotCertStore();
        }
        wasLoaded = true;
    }
    
    /**
     * Create a byte[] representation of the TrustManager
     *
     * @return The byte array
     */
    public byte[] asByteArray() throws Exception {
        /* buffer for certstore */
        byte[] cs;        
        /* TODO: Handle serialization exceptions properly */
        byte[] rawPrivKey;
        
        /* serialize the certstore */        
        cs = certStore.asByteArray();
        msg("Certstore has length: " + cs.length);
        if (spotPrivateKey == null) {
            rawPrivKey = new byte[0];
        } else {
            rawPrivKey = KeySerializer.serialize(spotPrivateKey);
        }
        
        /* calculate the length of the trust manager */
        int len = STATIC_SECTION_LENGTH +
                2 + rawPrivKey.length +      // 2 bytes for key length
                2 + spotCertificateBytes.length + // 2 bytes for cert length
                2 + cs.length;               // 2 bytes for keystore length
        
        /* Write out the TrustManager */        
        /* version */
        byte[] rawTrustManager = new byte[len];
        rawTrustManager[TRUST_MANAGER_VERSION_OFFSET] =
                CURRENT_TRUST_MANAGER_VERSION;
        
        int strAddr = TRUST_MANAGER_DATA_OFFSET;
        msg("[serializing] private key - offset: " + strAddr);
        /* private key length */
        Utils.writeLittleEndShort(rawTrustManager, strAddr,
                (short)rawPrivKey.length);
        strAddr += 2;
        
        /* private key */
        System.arraycopy(rawPrivKey, 0,
                rawTrustManager, strAddr,
                rawPrivKey.length);
        strAddr += rawPrivKey.length;
        
        msg("[serializing] cert - offset: " + strAddr);
        /* certificate length */
        Utils.writeLittleEndShort(rawTrustManager,
                strAddr, (short)spotCertificateBytes.length);
        strAddr += 2;
        
        /* certificate */
        System.arraycopy(spotCertificateBytes, 0,
                rawTrustManager, strAddr, spotCertificateBytes.length);
        strAddr += spotCertificateBytes.length;
        
        msg("[serializing] cert store - offset: " + strAddr);
        /* cert store length */
        Utils.writeLittleEndShort(rawTrustManager, strAddr, (short)cs.length);
        strAddr += 2;
        
        /* keystore */
        System.arraycopy(cs, 0, rawTrustManager, strAddr, cs.length);
        
        msg("[serializing] rawTrustManager:");
        
        // msg(PrettyPrint.prettyPrint(rawTrustManager));        
        return rawTrustManager;
    }
    
    /**
     * Get the version number of this page
     *
     */
    public int getTrustManagerVersion() {
        return CURRENT_TRUST_MANAGER_VERSION;
    }
    
    /**
     * Discover whether this TrustManager page was initialized by loading
     * from a byte array or by initialization from default values
     * @return true if this TrustManagager page was loaded from a byte array
     */
    public boolean wasLoaded() {
        return wasLoaded;
    }
    
    public void reset() {
        initializeTrustManager();
    }
    
    private void initializeTrustManager() {
        spotPrivateKey = null;
        spotCertificateBytes = new byte[0];
        certStore = new SpotCertStore();
        wasLoaded = false;
    }
    
    /** Returns the SPOTs private key
     * @return SPOTs private key
     * @throws IllegalStateException if the key has not been intialized
     */
    public ECPrivateKeyImpl getSpotPrivateKey() throws IllegalStateException {
        if (spotPrivateKey == null)
            throw new IllegalStateException(
                    "SPOT Private Key has not been initialized");
        return spotPrivateKey;
    }
    
    /** Returns the SPOTs X509 certificate bytes
     * @return SPOTs certificate bytes
     * @throws IllegalStateException if the certificate has not been intialized
     */
    public byte[] getSpotCertificateBytes() throws IllegalStateException {
        if (spotCertificateBytes.length == 0) {
            throw new IllegalStateException(
                    "SPOT Certificate has not been initialized");
        }
        return spotCertificateBytes;
    }
    
    /** Returns the SPOTs X509 certificate
     * @return SPOTs certificate
     * @throws IllegalStateException if the certificate has not been intialized
     */
    public X509Certificate getSpotCertificate() throws IllegalStateException {
        if (spotCertificate == null) {
            throw new IllegalStateException(
                    "SPOT Certificate has not been initialized");
        }
        return spotCertificate;
    }
    
    public void setSpotCertificate(byte[] certBytes) throws IOException {
        spotCertificateBytes = certBytes;
        spotCertificate = X509Certificate.generateCertificate(
                certBytes, 0, certBytes.length);
        certStore.addCert(SpotCertStore.PERSONAL_CERT_NICKNAME,
                "s", spotCertificate);
    }
    
    public void deleteSpotCert() {
        spotCertificateBytes = new byte[0];
        spotCertificate = null;
        certStore.removeCert(SpotCertStore.PERSONAL_CERT_NICKNAME);
    }
    
    public void deleteSpotKeys() {
        spotPrivateKey = null;
    }
    
    public SpotCertStore getCertStore() {
        return certStore;
    }
    
    public boolean SpotKeysAndCertificateReady() {
        return spotCertificateBytes.length != 0 && spotPrivateKey != null;
    }
    
    /** generates a new key pair for the spot and returns the new public key
     * @return spots new publickey
     */
    public byte[] generateSpotKeyPair() throws Exception {
        ECPublicKeyImpl pubKey;
        
        // Generate spot key pair
        pubKey = new ECPublicKeyImpl(ECCurve.SECP160R1);
        spotPrivateKey = new ECPrivateKeyImpl(ECCurve.SECP160R1);
        
        ECKeyImpl.genKeyPair((ECPublicKeyImpl) pubKey,
                (ECPrivateKeyImpl)spotPrivateKey);
        
        return KeySerializer.serialize(pubKey);
    }
    
    private void msg(String s) {
	    // HACK Removing spam
        // System.out.println("[TrustManager] " + s);
    }
}

