//if[FLASH_MEMORY]
/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk;

import java.io.*;

import com.sun.squawk.io.j2me.spotsuite.Pointer;
import com.sun.squawk.util.*;

/**
 * 
 */
public class FlashObjectMemoryLoader extends ObjectMemoryLoader {

	private Address memoryAddress;

	private int myHash;

	private boolean isHashSet = false;

	/**
	 * @param reader
	 * @param loadIntoReadOnlyMemory
	 */
	public FlashObjectMemoryLoader(ObjectMemoryReader reader, boolean loadIntoReadOnlyMemory) {
		super(reader, loadIntoReadOnlyMemory);
	}

	protected byte[] loadMemory(int size) {
		// record the current address of the reader (this is the location of
		// memory)
		memoryAddress = ((FlashObjectMemoryReader) reader).getCurrentAddress();

		// skip ahead size bytes to simulate reading that off the suite file
		reader.skip(size, "simulating flash memory load");

		myHash = reader.readInt("getting hash");
		isHashSet = true;
		byte[] dummy = {}; // return a dummy value to satisfy our superclass
		return dummy;
	}

	protected int getHash(byte[] dummy) {
		if (!isHashSet) {
			throw new IllegalStateException("Attempt to get hash before reading it");
		}
		return myHash;
	}

	protected void skipMemoryPadding(String parentURI, int memorySize) {
		FlashObjectMemoryReader r = (FlashObjectMemoryReader) reader;
		Offset off = r.getCurrentAddress().roundUpToWord().diff(r.getCurrentAddress());
		r.skip(off.toInt(), "skipping pad");
	}

	protected Address relocateMemory(ObjectMemory parent, byte[] buffer, BitSet oopMap) {

		// Return the previously cached address
		if (VM.isVerbose()) {
			System.out.println("Loading flash suite to address 0x"
					+ Integer.toHexString(memoryAddress.toUWord().toInt()));
		}
		return memoryAddress;
	}

	protected BitSet loadOopMap(int size) {
		int oopMapSize = GC.calculateOopMapSizeInBytes(size);
		reader.skip(oopMapSize, "simulating oop map read");
		return null;
	}
}

class FlashObjectMemoryReader extends ObjectMemoryReader {
	private Pointer pointer;

	/**
	 * Creates a <code>ObjectMemoryReader</code> that reads object memory file
	 * components from a given input stream.
	 * 
	 * @param in
	 *            the input stream
	 * @param filePath
	 *            the file from which <code>in</code> was created
	 */
	public FlashObjectMemoryReader(InputStream in, String filePath) {
		super(in, filePath);

		// cache the underlying input stream - we will want to talk to that.
		try {
			pointer = (Pointer) in;
		} catch (ClassCastException e) {
			Assert.shouldNotReachHere();
		}
	}

	public Address getCurrentAddress() {
		return Address.zero().add(pointer.getCurrentAddress());
	}
}
