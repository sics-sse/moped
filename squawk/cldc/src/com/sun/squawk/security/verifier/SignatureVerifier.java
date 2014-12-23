//if[FLASH_MEMORY]
/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.	security.verifier;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sun.squawk.Address;
import com.sun.squawk.GC;
import com.sun.squawk.Offset;
import com.sun.squawk.Unsafe;
import com.sun.squawk.VM;
import com.sun.squawk.security.CryptoException;
import com.sun.squawk.security.ECPublicKey;
import com.sun.squawk.security.HexEncoding;
import com.sun.squawk.security.ecc.ECCurveFp;
import com.sun.squawk.security.ecc.ECPoint;
import com.sun.squawk.security.ecc.FFA;
import com.sun.squawk.security.ecc.PrimeField;
import com.sun.squawk.util.Arrays;
import com.sun.squawk.vm.ChannelConstants;


/**
 * Used to verify the signature of certain signed data, including Suites.
 *
 */
public class SignatureVerifier {

	/**
	 * DEBUG is used for enabling and disabling debug code, usually to write debug messages
	 * which are defined in the form: <br> if (SignatureVerifier.DEBUG) {<some code} <br>
	 * If enabled is set to false the compiler will no include the debug code to the class file, as the
	 * statements are unreachable. Thus using this kind of debug statements doesn't increase the size of 
	 * the code. This wouldn't be the case if the statement is in another method, and as 
	 * code size and execution time is crucial for spots no debug_output method is included in the Debug class
	 * and the if statement must be indcluded in the code which needs debug output.<br>
	 * REMARK: debugging can only be enabled and disabled by recompiling and reflashing the squawk library. 
	 * 
	 */
	static final boolean DEBUG=false;
    
    /** The class is never to be instantiated. All methods and fields are static. */
	private SignatureVerifier() {
	}

	/**
	 * The maximum size the header of a suite can have to be compatible with
	 * SignatureVerifier. In the flash memory case this is guaranteed, the
	 * header 48 byte for application suites, the header of the library suite is
	 * slightly smaller. Higher maximum header sizes (actually higher
	 * differences between maximum and minimum header sizes) decrease the
	 * minimum size of the suite supported by verifySuite.
	 */
	public static final int MAXIMUM_HEADER_SIZE = 100;
	private static final int UNPADDED_HEADER_SIZE_WITHOUT_URL_OR_OOP_MAP = 24 + 2;
	private static final int MAXIMUM_URL_SIZE = MAXIMUM_HEADER_SIZE-UNPADDED_HEADER_SIZE_WITHOUT_URL_OR_OOP_MAP;

	/**
	 * The numbe of bytes read from flash memory and passed to the signature
	 * verification per loop. Must be larger than MAXIMUM_HEADER_SIZE!.
	 * Theoretically should the verification be faster for larger buffer sizes,
	 * but measurements show that the influence is only measurable for very
	 * small buffer sizes, and even then is very small. (That is not very
	 * surprising, because larger buffer sizes only accelerate the message
	 * digesting, while the time for elliptic curve computations, and especially
	 * the flash memory operations does not change. (The time for ecc
	 * verifySuite is about 1s)) Suite (43800 byte) BUFFER_SIZE Verification
	 * time 48: 3125ms 128: 3092ms 256: 3043ms 512: 3047ms 1024: 3017ms 2048:
	 * 2984ms 4096: 3014ms 65536: 3009ms
	 */
    public static final int BUFFER_SIZE = 1024;
    private static final byte ASN_CONSTRUCTED = 0x20;
    private static final byte ASN_INTEGER = 2;
    private static final byte ASN_SEQUENCE = 16;
    private static final int SUITE_VERIFIED = 0xFE;
	
/*if[NATIVE_VERIFICATION_ONLY]*/
/*else[NATIVE_VERIFICATION_ONLY]*/
//	private static SHA md;
/*end[NATIVE_VERIFICATION_ONLY]*/

	private static byte EXPECTED_SIGNATURE_VERSION = 0;

	private static int SIGNATURE_HEADER_LENGTH = 9;

	private static ECPublicKey publicKey=null;
	public static void initialize(byte[] publicKeyBytes, int offset, int length) throws SignatureVerifierException {

		try {
			publicKey = SignatureVerifier.getPublicECKeyFromX962Encoding(publicKeyBytes, offset, length);
			if (SignatureVerifier.DEBUG) {
				System.out.println("SignatureVerifier.initialize:\n\tpublicKey: " + publicKey);
            }
	
/*if[NATIVE_VERIFICATION_ONLY]*/
/*else[NATIVE_VERIFICATION_ONLY]*/
//			md=new SHA();
/*end[NATIVE_VERIFICATION_ONLY]*/
        } catch (Exception ex) {
			publicKey = null;			
/*if[NATIVE_VERIFICATION_ONLY]*/
/*else[NATIVE_VERIFICATION_ONLY]*/
//			md = null;
/*end[NATIVE_VERIFICATION_ONLY]*/			
			if (SignatureVerifier.DEBUG) {
				ex.printStackTrace();
            }
			throw new SignatureVerifierException("Setting public key failed. (" + ex + ")");
		}
	}

    /**
	 * Verifies a buffer 
     * @param buffer 
     * @param signature 
     * @todo javadoc
     * @throws SignatureVerifierException
     * @throws IOException 
	 */
	public static void verify(byte[] buffer, byte[] signature) throws SignatureVerifierException, IOException {
		verify(buffer, 0, buffer.length, signature, 0, signature.length);
	}

	/**
	 * Verifies a buffer 
     * @param buffer 
     * @param bufferOffset 
     * @param bufferLength 
     * @param signature 
     * @param signatureOffset 
     * @param signatureLength
     * @todo javadoc
	 * @throws SignatureVerifierException
	 */
	public static void verify(byte[] buffer, int bufferOffset, int bufferLength, byte[] signature, int signatureOffset,
			int signatureLength) throws SignatureVerifierException {
		ensureInitialized();
		byte[] result = new byte[20];

/*if[NATIVE_VERIFICATION]*/
    	Address address=Address.fromObject((buffer));
    	address=address.addOffset(Offset.fromPrimitive(bufferOffset));
    	if (SignatureVerifier.DEBUG) {
    		System.out.println("verify using native SHA1.\n\t Address for compute hash : "+address.toUWord().toInt()+
    				"Includes Offset: "+bufferOffset);
        }
    	VM.execSyncIO(ChannelConstants.INTERNAL_COMPUTE_SHA1_FOR_MEMORY_REGION,address.toUWord().toInt(), bufferLength , 0, 0, 0, 0, result, null);
		
		result = Arrays.copy(result, 0, 20, 0, 20);
		if (SignatureVerifier.DEBUG) {
			System.out.println("Hash computed using NATIVE function: \n"+HexEncoding.hexEncode(result));
        }
/*else[NATIVE_VERIFICATION]*/	
//	md.reset();
//  md.doFinal(buffer, bufferOffset, bufferLength, result, 0);
//  if (SignatureVerifier.DEBUG)		
//			System.out.println("Hash computed using Java class: \n"+HexEncoding.hexEncode(result));
//		
/*end[NATIVE_VERIFICATION]*/
		if (!verifyMessageDigest(result, signature,signatureOffset,signatureLength)) {
			throw new SignatureVerifierException("Signature verification failed");
		}
}
	
	/**
	 * Verifies a suite in flash memory. Remark: The suite header must be
	 * shorter than MAXIMUM_HEADER_SIZE bytes. This is only guarenteed in the
	 * case that the suite is in flash memory on a Spot, because it is ensured
	 * in com/syn/squawk/suiteconverter/Suite.java by replacing the parentURL
	 * with a url of the form flash://<address>.lib. If the suite is a suite
	 * file on the desktop, this is not garanteed, because the parent URL can
	 * have any length. Furthermore verifySuite expects that the first integer
	 * after the object memory in the suite is the hash. This is only the case
	 * for suite converted for flashmemory, thus it won't work for other suites.
	 * 
	 * @param suiteIn
	 *            An input stream which allows retrieving a suite. This usually
	 *            is a FlashInputStream pointing to a suite in the flash memory.
     * @throws SignatureVerifierException
     * @throws IOException 
	 */
	public static void verifySuite(InputStream suiteIn) throws SignatureVerifierException, IOException {
/*if[NATIVE_VERIFICATION]*/
		verifySuite(suiteIn, true);
/*else[NATIVE_VERIFICATION]*/
//		verifySuite(suiteIn, false);
/*end[NATIVE_VERIFICATION]*/
	}
 
	/**
	 * Verifies a suite in flash memory using either the java Signature or a
	 * native read from flash and sha1 code. It is only intented to ensure that 
	 * the byte code wasn't changed for Java compliance reasons. It does NOT 
	 * protect against attackers (especially if ignoreSuiteVerifiedFlag==false). 
	 * For functionallity which require such protection (like access control 
	 * for over the air deployment)use the verify method.
	 * <p>
	 * Remark: The suite header must be shorter than MAXIMUM_HEADER_SIZE bytes.
	 * This is only guarenteed in the case that the suite is in flash memory on
	 * a Spot, because it is ensured in com/syn/squawk/suiteconverter/Suite.java
	 * by replacing the parentURL with a url of the form flash://<address>.lib.
	 * If the suite is a suite file on the desktop, this is not garanteed,
	 * because the parent URL can have any length. Furthermore verifySuite
	 * expects that the first integer after the object memory in the suite is
	 * the hash. This is only the case for suite converted for flashmemory, thus
	 * it won't work for other suites.
	 * 
	 * @param suiteIn
	 *            An input stream which allows retrieving a suite. This usually
	 *            is a FlashInputStream pointing to a suite in the flash memory.
	 * @param useNativeSHA
	 *            Use faster message digest computation. Native C is about 85x faster 
	 * 	      than the pure Java version. useNativeSHA=true cannot be 
	 *            used if NATIVE_VERIFICATION compile flag not set, and 
	 *            useNativeSHA=false cannot be used if NATIVE_VERIFICATION_ONLY is
	 *             set.In this cases verifySuite will throw a RuntimeException
     * @throws SignatureVerifierException
     * @throws IOException 
	 */	
	public static void verifySuite(InputStream suiteIn, boolean useNativeSHA) throws SignatureVerifierException, IOException {
	    int suiteAddress = -1;		
/*if[FLASH_MEMORY]*/		
	    com.sun.squawk.io.j2me.spotsuite.Pointer pointer = ((com.sun.squawk.io.j2me.spotsuite.Pointer) suiteIn);
		suiteAddress = pointer.getCurrentAddress();
/*else[FLASH_MEMORY]*/
//      throw new RuntimeException("Internal Error: This code does not yet work outside of FLASH case");
/*end[FLASH_MEMORY]*/
		// Make sure that a key to verify was set
		ensureInitialized();	
	
		if (useNativeSHA) {
/*if[NATIVE_VERIFICATION]*/
/*if[FLASH_MEMORY]*/
			if (SignatureVerifier.DEBUG) {
				System.out.println("verifysuite (with native C SHA1)\n\tsuiteAddress:" + suiteAddress);
			}
/*end[FLASH_MEMORY]*/
/*else[NATIVE_VERIFICATION]*/
//			throw new RuntimeException("Internal Error: useNativeSHA=true in call to verifySuite although squawk was built without native verification support. ");
/*end[NATIVE_VERIFICATION]*/
		} else {
			if (SignatureVerifier.DEBUG) {
				System.out.println("Verifysuite (Java Signature digest)\n");
			}
/*if[NATIVE_VERIFICATION_ONLY]*/
			throw new RuntimeException(
					"Internal Error: useNativeSHA=false in call to verifySuite although squawk was built without Java verification support. (NATIVE_VERIFICATION_ONLY==TRUE)");
/*end[NATIVE_VERIFICATION_ONLY]*/
		}
		byte[] bytes = new byte[BUFFER_SIZE];
		int read = suiteIn.read(bytes, 0, MAXIMUM_HEADER_SIZE);
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
		int magic = dis.readInt();
		if (magic != 0xDEADBEEF) {
			throw new SignatureVerifierException("Suite verification failed. Suite file has wrong magic word.");
		}
		int version_minor = dis.readShort();
		int version_major = dis.readShort();
		if (version_major < 2) {
			throw new SignatureVerifierException(
					"Suite verification failed. Suite is not signed. (wrong suite format version " + version_major
							+ "." + version_minor + " Must be 2.0 or higher)");
        }
		if (SignatureVerifier.DEBUG) {
			System.out.println("\n\tSuite file version: " + version_major + "." + version_minor);
        }

		dis.readInt(); // attributes
		dis.readInt(); // int parentHash
		String parentURL = dis.readUTF();
		boolean hasParent = parentURL.length() != 0;
		dis.readInt(); // int rootOffset
		int memorySize = dis.readInt();
		int oopMapSize = GC.calculateOopMapSizeInBytes(memorySize);

		// The offset of the signature INCLUDING the signature header. (version,
		// timestamp,...)
		// The real signature starts at signatureOffset+SIGNATURE_HEADER_LENGTH
		// Remark: the hash appended to the suite directly after the
		// objectmemory
		// is not part of the signature header.
		int signatureOffset;
		if (hasParent) { // suite is an application or library suite
			if (parentURL.length() > MAXIMUM_URL_SIZE) {
				throw new SignatureVerifierException("Suite verification failed. The parent url \"" + parentURL
						+ "\"is incompatible with SuiteSignatureVerifier. The url must have"
						+ " a length smaller than " + MAXIMUM_URL_SIZE + " bytes, but is " + parentURL.length() + " bytes");
            }
			int unpaddedHdrSize = UNPADDED_HEADER_SIZE_WITHOUT_URL_OR_OOP_MAP + parentURL.length() + oopMapSize;
			int outputHeaderSize = ((unpaddedHdrSize + 3) / 4) * 4;

			signatureOffset = outputHeaderSize + memorySize + 4;
			// Total length of suite (=offset of signature) is the sum of the
			// size
			// of the header (including the padding to ensure that object memory
			// begins on a 32-bit boundary), the size of the object memory, and
			// 4 byte where the hash is saved. The hash is only save for suites
			// in flash memory, thus verifySuite won't work for suites saved in
			// files on the desktop.

			if (SignatureVerifier.DEBUG) {
				System.out.println("\n\tsignatureOffset=" + signatureOffset);
            }
		} else { // the suite is the bootstrap suite
			throw new SignatureVerifierException(
					"Suite verification failed. SuiteSignatureVerifier doesn't support bootstrap suite.");
		}
		if (dis.available() == 0) {
			throw new SignatureVerifierException(
					"Suite verification failed. Header size was larger than the default we hacked in of "
							+ bytes.length);
		}
		int signatureLength = 0;

		// true if the first MAXIMUM_HEADER_SIZE (=48) bytes already contain (a
		// part of)
		// the signature. This case is not supported, as
		// it should not occur in reality (it probably can only occur for
		// libraries which are smaller than 4 byte) and is relativly hard to
		// handle.
		if (read > signatureOffset) {
			throw new SignatureVerifierException(
					"Suite verification failed. The suite is incompatible with SuiteSignatureVerifier. The object memory is"
							+ "too small (<4 bytes), so that buffer for reading suite header already contains part of signature.  ");
		}
		// If native SHA is used skip to the signature,
		// if java message digest is used, read this bytes
		// and update the message digest
		if (useNativeSHA) {
			if (SignatureVerifier.DEBUG) {
				System.out.println("\tSkipping to Signature Header");
            }
			suiteIn.skip(signatureOffset - read);
			read += signatureOffset - read;
		} else {
/*if[NATIVE_VERIFICATION_ONLY]*/
			throw new RuntimeException(
					"Internal Error: useNativeSHA=false in call to verifySuite although squawk was built without Java verification support. (NATIVE_VERIFICATION_ONLY==TRUE)");			
/*else[NATIVE_VERIFICATION_ONLY]*/
//			if (SignatureVerifier.DEBUG)
//				System.out.println("\tReading suite");
//			int totalRead = 0;
//			md.reset();
//			while (read > 0) {
//				md.update(bytes, 0, read);
//				totalRead += read;
//				read = suiteIn.read(bytes, 0, signatureOffset > totalRead + bytes.length ? bytes.length
//						: signatureOffset - totalRead);
//			}
/*end[NATIVE_VERIFICATION_ONLY]*/
		}
        
		// Read the signature header. This part of the suite is included in the
		// signature verification
		// signature header
		// byte signature_version
		// long timestamp (not checked her, but to be checked in secure remote
		// deployment)
		read = suiteIn.read(bytes, 0, SIGNATURE_HEADER_LENGTH);

		// The signature header (but not the signature itself)
		// is part of the signed data. Thus for the non native
		// implementation update the signature here,
		// in the native case this just has to be considered in
		// the size parameter passed to the SHA C call later.
		if (!useNativeSHA) {
/*if[NATIVE_VERIFICATION_ONLY]*/
			throw new RuntimeException(
					"Internal Error: useNativeSHA=false in call to verifySuite although squawk was built without Java verification support. (NATIVE_VERIFICATION_ONLY==TRUE)");			
/*else[NATIVE_VERIFICATION_ONLY]*/
//			md.update(bytes, 0, read);
/*end[NATIVE_VERIFICATION_ONLY]*/
		}
		
		if (read < SIGNATURE_HEADER_LENGTH) {
			throw new SignatureVerifierException("Signature verification failed. Signature header invalid.");
        }

		byte signature_version = bytes[0];
		if (signature_version != EXPECTED_SIGNATURE_VERSION) {
			throw new SignatureVerifierException("Signature verification failed. Unsupported signature version "
					+ signature_version + ". Expected version: " + EXPECTED_SIGNATURE_VERSION);
        }

		// Read the signature
		suiteIn.read(bytes, 0, 2);
		signatureLength = bytes[1];
		suiteIn.read(bytes, 2, signatureLength);
		signatureLength += 2;
		if (SignatureVerifier.DEBUG) {
			System.out.println("\tSignature of suite: " + HexEncoding.hexEncode(bytes, signatureLength) + " ("
					+ signatureLength + " bytes)");
        }

		// Compute the sha1 hash either in in c by using execSyncIO (if
		// useNative)
		// or by using java Signature.
		// As not only the hashing itself, but also the reading from flash can
		// be performed
		// more efficient in C, the speed up is impressive:
		// Java
		// Read SHA 139kb (transducerlib+EX1-Reactomatic)
		// 2300ms 3700ms= 6000 ms
		// C (RFC3174, not used anymore)
		// Read + SHA 134kB (spotlib+template application)
		// 69 ms
		// C (Mozilla NSS, used because of simpler licensing)
		// Read + SHA 134kB (spotlib+template application)
		// 183 ms (with compilerflag -O0)
		// ?   ms 

		// Now the ECC computation time is the dominates startup.
		// For each suite ECC verification takes about 1000ms.
		byte[] result = new byte[20];
		
		if (useNativeSHA) {
/*if[NATIVE_VERIFICATION]*/
			if (SignatureVerifier.DEBUG) {
				System.out.println("Excuting syncIO COMPUTE_SHA1...");
				System.out.println("\taddress:" + suiteAddress + "\n\tnumberOfBytes:"
						+ (signatureOffset + SIGNATURE_HEADER_LENGTH));
			}
			long startTime = System.currentTimeMillis();
			int r = VM.execSyncIO(ChannelConstants.INTERNAL_COMPUTE_SHA1_FOR_MEMORY_REGION, suiteAddress, signatureOffset
					+ SIGNATURE_HEADER_LENGTH, 0, 0, 0, 0, result, null);

			result = Arrays.copy(result, 0, 20, 0, 20);
			long endTime = System.currentTimeMillis();
			if (SignatureVerifier.DEBUG) {
				System.out.println("execsyncIO returned:" + r);
				System.out.println("Time for read from flash and hash: " + (endTime - startTime) + "ms");
			}

			if (SignatureVerifier.DEBUG) {
				System.out.println("syncIO COMPUTE_SHA returned");
				System.out.println("\nSHA hash: " + HexEncoding.hexEncode(result));
			}
/*end[NATIVE_VERIFICATION]*/
		} else { // !useNativeSHA
			// Assume that code flow will never reach here if NATIVE_VERIFICATION==false 
			//(because of exception throwing at beginning of method. But use flags here
			// to allow compiling if COMPUTE_SHA1_FOR_MEMORY_REGION is not defined 
/*if[NATIVE_VERIFICATION_ONLY]*/
			throw new RuntimeException (
				"Internal Error: useNativeSHA=false in call to verifySuite although squawk was built without Java verification support. (NATIVE_VERIFICATION_ONLY==TRUE)");			
/*else[NATIVE_VERIFICATION_ONLY]*/
//			long startTime;
//			if (SignatureVerifier.DEBUG)
//				startTime = System.currentTimeMillis();
//			md.doFinal(bytes, 0, 0, result,0);
//			if (SignatureVerifier.DEBUG) {
//				long verifyTime = System.currentTimeMillis() - startTime;
//				System.out.println("Time for theSig.verify :" + verifyTime + " ms");
//			}
/*end[NATIVE_VERIFICATION_ONLY]*/
			// Verify the suite and throw an exception if verification failed.
			// bytes only contains the signature, thus the length for the input
			// buffer is zero.
			// (TODO: For clearness a verifySuite which only takes the signature
			// (and no buffer) should be added to the crypto library)
			
		}
        
		// Verify the suite and throw an exception if
        // verification failed.
        // result contains the pre-computed SHA hash.
        if (!verifyMessageDigest(result, bytes, 0, signatureLength)) {
            throw new SignatureVerifierException(
                "Suite verification failed.\nSignature: "
                    + HexEncoding.hexEncode(bytes, signatureLength)
                    + "\nSHA hash: " + HexEncoding.hexEncode(result));
        }
/*if[FLASH_MEMORY]*/
        try {
            setSuiteVerifiedFlag(suiteAddress);	
            if (SignatureVerifier.DEBUG) {
                System.out.println("Suite verified flag set");
            }
        } catch (IllegalArgumentException ex) {
            throw new SignatureVerifierException("Reading suiteVerified flag failed.("+ex+")");
        }
/*end[FLASH_MEMORY]*/
	}

	protected static void ensureInitialized() throws SignatureVerifierException {
/*if[NATIVE_VERIFICATION]*/
		if ((publicKey == null)||(publicKey.initOk==false)) {
			throw new SignatureVerifierException(
					"Public key must be initialized via SignatureVerifier.initialize() prior to verifying.");
        }
/*end[NATIVE_VERIFICATION]*/
	}
	
	
	/**
     * Verifies the signature with an externally computed sha1 hash. 
     *  
     * <p>
     * A call to this method also resets this <code>Signature</code> object to
     * the state it was in when previously initialized via a call to
     * <code>init()</code>. That is, the object is reset and available to
     * verify another message. In addition, note that the initial vector(IV)
     * used in AES and DES algorithms in CBC mode will be reset to 0.
     * <p>
     *  To use this method Signature.init has to be called before to initialize 
     *  the key, while calling Signature.update does not have any effect on the 
     *  result.
     *  
     * <p>
     * Note:
     * <ul>
     *   <li>AES, DES, and triple DES algorithms in CBC mode reset the initial
     *     vector(IV) to 0. The initial vector(IV) can be re-initialized using
     *     the <code>init(Key, byte, byte[], short, short)</code> method.</li>
     * </ul>
     * <p>     
     *  
     * @param digestBuf The externally compute sha1 hash to be verified 
     * @param sigBuff the input buffer containing signature data
     * @param sigOffset the offset into <code>sigBuff</code> where signature
     *   data begins
     * @param sigLength the byte length of the signature data 
     *
     * @return <code>true</code> if the signature verifies, <code>false</code>
     *   otherwise. Note, if <code>sigLength</code> is inconsistent with this
     *   <code>Signature</code> algorithm, <code>false</code> is returned. 
     *
     * @throws CryptoException with the following reason codes:
     *   <ul>
     *     <li><code>CryptoException.UNINITIALIZED_KEY</code> if key not
     *       initialized.</li>
     *     <li><code>CryptoException.INVALID_INIT</code> if this
     *       <code>Signature</code> object is not initialized or initialized
     *       for signature sign mode.</li>
     *     <li><code>CryptoException.ILLEGAL_USE</code> if one of the following
     *       conditions is met:
     *       <ul>
     *         <li>if this <code>Signature</code> algorithm does not pad the
     *           message and the message is not block aligned.</li>
     *         <li>if this <code>Signature</code> algorithm does not pad the
     *           message and no input data has been provided in
     *           <code>inBuff</code> or via the <code>update()</code>
     *           method.</li>
     *       </ul></li>
     *   </ul>
     */
    private static boolean  verifyMessageDigest(byte[]digestBuf, byte[] sigBuff, int sigOffset, int sigLength)
            throws CryptoException {
             
        // See: ANSI X9.62-1998, 5.4 Signature Verification
        
        if (publicKey == null) {
            CryptoException.throwIt(CryptoException.INVALID_INIT);
        }
        if (publicKey.initOk==false) {
            CryptoException.throwIt(CryptoException.UNINITIALIZED_KEY);
        }
        
        // We can use the PrimeField class to do all the (mod n) computations
        ECCurveFp curve = publicKey.getCurve();
        PrimeField field = curve.getOrder();
        FFA ffa = field.getFFA();
        
        // check the sequence header
        if ((sigLength < 6) || (sigBuff[sigOffset++] != (ASN_CONSTRUCTED | ASN_SEQUENCE))) { return false; }
        int sequenceLen = sigBuff[sigOffset++];
        if ((sequenceLen != sigLength - 2) || (sequenceLen < 4)) { return false; }
        
        // read the first integer: 'r'
        if (sigBuff[sigOffset++] != ASN_INTEGER) { return false; }
        int len = sigBuff[sigOffset++];
        sequenceLen -= (2 + len);
        if (sequenceLen < 2) { return false; }
        int[] r = ffa.from(sigBuff, sigOffset, len); sigOffset += len;
        
        // read the second integer: 's'
        if (sigBuff[sigOffset++] != ASN_INTEGER) { return false; }
        len = sigBuff[sigOffset++];
        sequenceLen -= (2 + len);
        if (sequenceLen != 0) { return false; }
        int[] s = ffa.from(sigBuff, sigOffset, len);
        
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
        
        //System.out.println("e = " + ffa.toString(u1));
        //System.out.println("r = " + ffa.toString(r));
        //System.out.println("s = " + ffa.toString(s));
        
        field.invert(s, s);
        field.multiply(u1, u1, s);  // u1 = (e * s^-1) mod n
        field.multiply(u2, r, s);   // u2 = (r * s^-1) mod n
        
        ECPoint G = curve.getGenerator().clonePoint();
        ECPoint Q = publicKey.getKeyData().clonePoint();
        
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
    
    /** gets the ECPublicKey from the X962 encoding in a byte array
     *  @param publicKeyS EC public key in X962 encoding. Must be a SEC160r key.
     *  @return ECPublicKey the Elliptic curve public key, or null if publicKeyS
     *   is not a valid key
     */
    private static ECPublicKey getPublicECKeyFromX962Encoding(byte[] publicKeyBA,  int offset, int length) throws CryptoException  {
        //KeyPair keyPair = new KeyPair(KeyPair.ALG_EC_FP, ECKey.SECP160R1);
        
        //keyPair.genKeyPair();
        //ECPublicKey publicKey=new ECPublicKeyImpl();
        ECPublicKey newKey=new ECPublicKey();
        if (SignatureVerifier.DEBUG) {
            System.out.println("getPublicECKeyFromX962Encoding:"+HexEncoding.hexEncode(publicKeyBA,publicKeyBA.length));
        }
        //((ECPublicKey)keyPair.getPublic()).setW(publicKeyBA,0, publicKeyBA.length);
        newKey.setW(publicKeyBA,offset, length);
        if (SignatureVerifier.DEBUG) {
            byte [] buf= new byte[length];
            newKey.getW(buf, 0);
            System.out.println("getPublicECKeyFromX962Encoding from point:"+HexEncoding.hexEncode(buf,buf.length));
        }
        
        //return (ECPublicKey)keyPair.getPublic();
        return newKey;
    }
    
    public static boolean getSuiteVerifiedFlag(int suiteAddress) throws IllegalArgumentException {
/*if[FLASH_MEMORY]*/
        int suiteVerifiedFlagAddress=getSuiteVerifiedFlagAddress(suiteAddress);
        int suiteVerifiedFlag =
            (Unsafe.getByte(Address.zero().add(suiteVerifiedFlagAddress), 0)) & (0xff);
        return (0x01!=(suiteVerifiedFlag & (0x01))); //LSB bit is suite verified flag.
/*else[FLASH_MEMORY]*/
//	    return false;
/*end[FLASH_MEMORY]*/
    }
    
    private static void setSuiteVerifiedFlag(int suiteAddress) throws IllegalArgumentException {
	int verifiedFlagAddress = getSuiteVerifiedFlagAddress(suiteAddress);
	
//	if (SignatureVerifier.DEBUG)
//		System.out
//			.println("Setting suiteVerifiedFlag.\n\tCurrent value"
//				+ suiteVerifiedFlag + "\n\tShould be"
//				+ suiteVerifiedFlag + "\n\tSet to "
//				+ SUITE_VERIFIED + "\n\tsuiteaddress:"
//				+ suiteAddress + "\n\tverifiedFlagAddress:"
//				+ (verifiedFlagAddress));
    
	    // Set the suiteVerifiedFlag to verified.
	    byte[] tb = new byte[2];
	    // Ensure to flash at an even address.
	    // Not doing so just hangs the SPOT.
	    if (verifiedFlagAddress % 2 == 0) {
			tb[0] = (byte) SUITE_VERIFIED;
			tb[1] = (byte) (Unsafe.getByte(Address.zero().add(verifiedFlagAddress + 1), 0) & (0xff));
			if (SignatureVerifier.DEBUG) {
			    System.out.println("Address even. writing: "
				    + HexEncoding.hexEncode(tb) + " to "
				    + verifiedFlagAddress);
            }
			VM.execSyncIO(ChannelConstants.FLASH_WRITE, verifiedFlagAddress, 2, 0, 0, 0, 0, tb, null);
	    } else {
			tb[0] = (byte) (Unsafe.getByte(Address.zero().add(verifiedFlagAddress - 1), 0) & (0xff));
			tb[1] = (byte) SUITE_VERIFIED;
			if (SignatureVerifier.DEBUG) {
			    System.out.println("Address uneven. writing: "
				    + HexEncoding.hexEncode(tb) + " to "
				    + (verifiedFlagAddress - 1));
            }
			VM.execSyncIO(ChannelConstants.FLASH_WRITE, verifiedFlagAddress - 1, 2, 0, 0, 0, 0, tb, null);
	    }
	    if (!getSuiteVerifiedFlag(suiteAddress)) {
	    	System.out.println("Warning: Setting suite verified flag failed.");
        }
    }
    
    private static int getSuiteVerifiedFlagAddress(int suiteAddress) throws IllegalArgumentException {
		int slotSize = VM.execSyncIO(ChannelConstants.GET_ALLOCATED_FILE_SIZE, suiteAddress);
		if (slotSize <=0 ) {
			String errorMessage = "suiteAddress 0x" + Integer.toHexString(suiteAddress) + " is not pointing to a mapped file";
			if (SignatureVerifier.DEBUG) {
				System.out.println(errorMessage);
			}
			throw new IllegalArgumentException(errorMessage);
		}
		int verifiedFlagAddress = suiteAddress + slotSize - 1;
		if (SignatureVerifier.DEBUG) {
			System.out.println("verifiedFlagAddres for suite address " + Integer.toHexString(suiteAddress) + " is "
					+ Integer.toHexString(verifiedFlagAddress));
        }
		return verifiedFlagAddress;
	}
    
    public static boolean isVerifiedSuite(InputStream suiteIn) {
	    com.sun.squawk.io.j2me.spotsuite.Pointer pointer = ((com.sun.squawk.io.j2me.spotsuite.Pointer) suiteIn);
		return SignatureVerifier.getSuiteVerifiedFlag(pointer.getCurrentAddress());
	}
}
