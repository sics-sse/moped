/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011-2013 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.suiteconverter;

import com.sun.squawk.ObjectMemoryFile;
import com.sun.squawk.ObjectMemorySerializer;
import com.sun.squawk.VM;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

//import com.sun.spot.peripheral.ConfigPage;

//import com.sun.squawk.security.verifier.SignatureVerifier;


/**
 * Suite allows suite files to be saved in a form that can execute directly from flash memory.
 *
 * NOTE: This is a file that originally came from the spot build process.
 *
 * Squawk suites are normally saved in "canonical" form, where the base address of the bootstrap suite is assumed
 * to be 0, the base address of child suites to be directly above the last word of the bootstrap suite, and so on
 * for grandchildren. The suites also containing an OOP map that identifies which words are actually pointers. On the
 * desktop, when a suite is loaded into memory, its pointers can then be remapped according to the actual address
 * at which it finds itself, its parent, grandparent, and so on.
 *
 * On the SPOT, this remapping can't happen, as the suites are in flash memory. So instead, we remap them on the host
 * before downloading to the device. This relies on the suites living at known addresses in SPOT virtual memory.
 *
 * This class also supports a truncated suite header for the bootstrap suite, which only contains 12 bytes (no parent fields, oopmap, etc).
 *
 */
public class Suite {
    public final static int FLASH_SUITE_MINOR_VERSION = 0;
    public final static int FLASH_SUITE_MAJOR_VERSION = 2;

	private String parentURL;
	private int rootOffset;
	private int memorySize;
	private byte[] oopMap;
	private byte[] objectMemory;
	private int canonicalStart;
	private Suite parentSuite = null;
	private int outputHeaderSize;
	private short version_minor;
	private short version_major;
	private int unpaddedHdrSize;
	private int hash;

    private static boolean isBigEndian = false; // ARM target is little endian

	public static final int EXPECTED_SUITE_VERSION_MAJOR = ObjectMemorySerializer.CURRENT_MAJOR_VERSION;
    public static final int EXPECTED_SUITE_VERSION_MINOR = ObjectMemorySerializer.CURRENT_MINOR_VERSION;;

    /* copied from ConfigPage: */
    public static final String LIBRARY_URI =  "spotsuite" + "://library";

    public static boolean isTargetBigEndian() {
        return isBigEndian;
    }

    public static void setIsTargetBigEndian(boolean value) {
        isBigEndian = value;
    }

	/**
	 * Load a suite file from a {@link DataInputStream}. See also {@link #loadFromFile(String, String, int[])}
	 *
	 * @param dis The {@link DataInputStream} to read the suite from
	 * @param bootstrapFilename A filepath to read the bootstrap suite from (note that this is NOT typically
	 * this suite's parent.
	 * @throws IOException
	 */
	public void loadFromStream(DataInputStream dis, String bootstrapFilename) throws IOException {
		int magic = dis.readInt();
		if (magic != 0xDEADBEEF) {
			throw new IOException("Suite file has wrong magic word: " + Integer.toHexString(magic));
		}
		version_minor = dis.readShort();
		version_major = dis.readShort();
		if ((version_major != EXPECTED_SUITE_VERSION_MAJOR)
				|| (version_minor != EXPECTED_SUITE_VERSION_MINOR))
			throw new RuntimeException("Unsupported suite file version: "
					+ version_major + "." + version_minor + ". Expected "
					+ EXPECTED_SUITE_VERSION_MAJOR + "."
					+ EXPECTED_SUITE_VERSION_MINOR);

		dis.readInt(); // attributes
		readParentHash(dis);
		parentURL = dis.readUTF();
		rootOffset = dis.readInt();
		memorySize = dis.readInt();

		int oopMapSize = calculateOopMapSizeInBytes(memorySize);
		oopMap = new byte[oopMapSize];
		dis.readFully(oopMap);

		if (hasParent()) {
			parentSuite = new Suite();
			if (parentURL.equals("memory:bootstrap")) {
				parentSuite.loadFromFile(bootstrapFilename, null);
			} else {
				// skip "file://"
				// System.out.println(parentURL);
			    // EAT:
			    // Using the path to the bootstrap is not really the right way to go, we should have a mechanism for setting
			    // the path to use to find suites when we need them.  This used to work as the suite written out by SuiteCreator
			    // would have a full path to the original suite.  I didn't think that was a good idea and therefore introduced
			    // concept of suite class path type of thing.  In order to adapt your concept of Suite to fit this, I would have
			    // to have changed a lot of classes, and wanted to limit scope of changes I was making.
			    String parentFileName = new File(new File(bootstrapFilename).getParent(), new File(parentURL.substring("file://".length())).getName()).getAbsolutePath();
				parentSuite.loadFromFile(parentFileName, bootstrapFilename);
			}
			canonicalStart = parentSuite.getCanonicalEnd();
			unpaddedHdrSize = 24 + 2 + getSpotParentURL().length() + oopMapSize;
			outputHeaderSize = ((unpaddedHdrSize + 3) / 4) * 4;
		} else {
			outputHeaderSize = 12;
		}

		// skip padding
		dis.skipBytes((4 - ((oopMapSize + parentURL.length() + 2) % 4)) % 4);
		objectMemory = new byte[memorySize];
		dis.readFully(objectMemory);
		hash = objectMemory.length;
		for (int i = 0; i != objectMemory.length; ++i) {
			hash += objectMemory[i];
		}
        
        //logHeader("stream");
	}

    public void logHeader(String name) {
        System.out.println("SuiteReloInfo: " + name);
        System.out.println("canonicalStart: 0x" + Integer.toHexString(canonicalStart));
        System.out.println("canonicalEnd: 0x" + Integer.toHexString(canonicalStart + memorySize));
        System.out.println("parentURL: " + parentURL);
        System.out.println("rootOffset: 0x" + Integer.toHexString(rootOffset));
        System.out.println("size: " + memorySize);
        System.out.println("unpadded header size: " + unpaddedHdrSize);
        System.out.println("header size: " + ((unpaddedHdrSize + 3) / 4));
    }

	private int readParentHash(DataInputStream dis) throws IOException {
		return dis.readInt();
	}

	/**
	 * Write the suite to a stream
	 * @param dos the data output stream
	 * @throws IOException
	 */
	public void writeToStream(DataOutputStream dos) throws IOException {
///*if[!SIMPLE_VERIFY_SIGNATURES]*/
        DataOutputStream output = dos;
        final int MAX_HEADER_SIZE = Integer.MAX_VALUE;
///*else[SIMPLE_VERIFY_SIGNATURES]*/
//      SigningOutputStream output = new SigningOutputStream(dos);
//      final int MAX_HEADER_SIZE = SignatureVerifier.MAXIMUM_HEADER_SIZE;
///*end[SIMPLE_VERIFY_SIGNATURES]*/

        if (hasParent()) {
            if (VM.isVerbose()) {
                System.out.println("Converting library/app suite:");
            }
            /*
             * u4 magic // 0xDEADBEEF
             * u2 minor_version;
             * u2 major_version;
             * u4 attributes; // mask of the ATTRIBUTE_* constants in this class
             * u4 parent_hash;
             * utf8 parent_url;
             * u4 root; // offset (in bytes) in 'memory' of the root of the graph
             * u4 size; // size (in bytes) of memory  'memory' on a word boundary
             * u1 memory[size];
             */
            output.writeInt(0xDEADBEEF);
            output.writeShort(FLASH_SUITE_MINOR_VERSION); // minor
            output.writeShort(FLASH_SUITE_MAJOR_VERSION); // major
            output.writeInt(ObjectMemoryFile.ATTRIBUTE_32BIT | (isTargetBigEndian() ? ObjectMemoryFile.ATTRIBUTE_BIGENDIAN : 0)); // 32 bit
            output.writeInt(getParent().getHash());
            output.writeUTF(getSpotParentURL());
            output.writeInt(rootOffset);
            output.writeInt(memorySize);
            output.write(oopMap);
            writePad(output, outputHeaderSize - unpaddedHdrSize);
            if ((outputHeaderSize - oopMap.length) > MAX_HEADER_SIZE) {
                throw new RuntimeException(
                        "Header size of suite is too large. For compatibility with SuiteSignatureVerifier header size "
                        + "must not be larger than "
                        + MAX_HEADER_SIZE
                        + " bytes, but it is "
                        + outputHeaderSize
                        + " bytes");
            }
        } else {
            if (VM.isVerbose()) {
                System.out.println("Converting boostrap suite:");
                System.out.println("rootOffset: 0x" + Integer.toHexString(rootOffset + outputHeaderSize));
                System.out.println("hash: 0x" + Integer.toHexString(getHash()));
                System.out.println("size: 0x" + Integer.toHexString(memorySize));
            }
            writeLittleEndianInt(output, rootOffset + outputHeaderSize);
            writeLittleEndianInt(output, getHash());
            writeLittleEndianInt(output, memorySize);
        }
        output.write(objectMemory);

///*if[!SIMPLE_VERIFY_SIGNATURES]*/
        output.flush();
///*else[SIMPLE_VERIFY_SIGNATURES]*/
//		if (hasParent())
//		// If this is not the bootstrap suite write the hash
//		// and sign the suite.
//		{
//			output.writeInt(getHash());
//			output.flushAndAppendSignature();
//		}else{
//			output.flushWithoutSignature();
//		}
///*end[SIMPLE_VERIFY_SIGNATURES]*/
	}

	/**
	 * Answer a parent URL for use on the SPOT bootstrap's parent is null - same
	 * as the host lib's parent is memory:bootstrap - same as the host apps's
	 * parent (all other cases!) is different and defined as flash://<lib addr>
	 */
	private String getSpotParentURL() {
		if (parentURL.equals("memory:bootstrap") || (parentURL == null)) {
			return parentURL;
		} else {
//			return ConfigPage.LIBRARY_URI;
			return LIBRARY_URI;
		}
	}

	/**
	 * @return The calculated hash of this Suite.
	 */
	public int getHash() {
		return hash;
	}

	/**
	 * @param dos
	 * @param i
	 * @throws IOException
	 */
	private void writeLittleEndianInt(DataOutput dos, int i)
			throws IOException {
		dos.write((i >>> 0) & 0xFF);
		dos.write((i >>> 8) & 0xFF);
		dos.write((i >>> 16) & 0xFF);
		dos.write((i >>> 24) & 0xFF);
	}

	/**
	 * @param
	 * @throws IOException
	 *
	 */
	private void writePad(DataOutput dos, int padCount)
			throws IOException {
		for (int i = 0; i < padCount; i++) {
			dos.writeByte(0);
		}
	}

	/**
	 * Relocate the suite's object memory in accordance with the memory addresses supplied
	 * @param memoryAddrs  An array of virtual memory addresses at which to assume suites will be mapped on the SPOT
	 * device. The first will be this suite's address, the second its parent, and so on until the the bootstrap address.
	 */
	public void relocateMemory(int[] memoryAddrs) {
		byte[] result = new byte[objectMemory.length];
		System.arraycopy(objectMemory, 0, result, 0, objectMemory.length);
		for (int i = 0; i < oopMap.length; i++) {
			byte currentByte = oopMap[i];
			for (int j = 0; j < 8; j++) {
				if (((currentByte >> j) & 1) == 1) {
					int index = 4 * ((i * 8) + j);
					int pointer = getObjectMemoryWord(objectMemory, index);
					writeObjectMemoryWord(result, index, this.mapPointer(
							pointer, memoryAddrs));
				}
			}
		}
		objectMemory = result;
	}

	/**
	 * pointer is canonical. If it points into our memory space, then assume our
	 * flash memory base is the first element of memoryAddrs and adjust
	 * accordingly. If it doesn't then delegate to our parent, having stripped
	 * our flash memory base from the array.
	 *
	 * @param pointer value to relocate
	 * @param memoryAddrs array of actual base addresses for this suite, and for each parent suite.
	 * @param offset offset to the memory address (in memoryAddrs) that goes with "this" suite
	 * @return
	 */
	private int mapPointer(int pointer, int[] memoryAddrs, int offset) {
		if (pointer == 0) {
			return 0;
		} else {
			if (pointer < canonicalStart) {
				// map pointer against parent memory address
				return getParent().mapPointer(pointer, memoryAddrs, offset + 1);
			} else {
				// map pointer against our memory address
				return memoryAddrs[offset] - canonicalStart + pointer
						+ outputHeaderSize;
			}
		}
	}

    /**
	 * pointer is canonical. If it points into our memory space, then assume our
	 * flash memory base is the first element of memoryAddrs and adjust
	 * accordingly. If it doesn't then delegate to our parent, having stripped
	 * our flash memory base from the array.
	 *
	 * @param pointer
	 * @param parentMemoryAddrs
	 * @return
	 */
	private int mapPointer(int pointer, int[] memoryAddrs) {
        return mapPointer(pointer, memoryAddrs, 0);
	}

	/**
	 * @param result
	 * @param index
	 * @param replacementPointer
	 */
	private void writeObjectMemoryWord(byte[] memory, int index, int value) {
		if (isTargetBigEndian()) {
			memory[index + 0] = (byte) (value >> 24);
			memory[index + 1] = (byte) (value >> 16);
			memory[index + 2] = (byte) (value >> 8);
			memory[index + 3] = (byte) (value >> 0);
		} else {
			memory[index + 0] = (byte) (value >> 0);
			memory[index + 1] = (byte) (value >> 8);
			memory[index + 2] = (byte) (value >> 16);
			memory[index + 3] = (byte) (value >> 24);
		}

	}

	static private int getObjectMemoryWord(byte[] memory, int index) {
		int b0 = memory[index + 0] & 0xFF;
		int b1 = memory[index + 1] & 0xFF;
		int b2 = memory[index + 2] & 0xFF;
		int b3 = memory[index + 3] & 0xFF;
		if (isTargetBigEndian()) {
			return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
		} else {
			return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
		}
	}

	private int getCanonicalEnd() {
		return memorySize + canonicalStart;
	}

	private boolean hasParent() {
		return parentURL.length() != 0;
	}

	/**
	 * @return The loaded parent suite of this Suite
	 */
	public Suite getParent() {
		return parentSuite;
	}

	/**
	 * Load a suite file from a {@link File}. See also {@link #loadFromStream(DataInputStream, String)}
	 *
	 * @param filename The filepath to read the suite from
	 * @param bootstrapFilename A filepath to read the bootstrap suite from (note that this is NOT typically
	 * this suite's parent.
	 * @throws IOException
	 */
	public void loadFromFile(String filename, String bootstrapFilename) throws IOException {
		File inputFile = new File(filename);
		FileInputStream fis = new FileInputStream(inputFile);
		loadFromStream(new DataInputStream(fis), bootstrapFilename);
		fis.close();
        //logHeader(filename);
	}

	private int calculateOopMapSizeInBytes(int size) {
		return ((size / 4) + 7) / 8;
	}

}
