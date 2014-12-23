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

package com.sun.squawk.io.j2me.spotsuite;

import java.io.*;

import javax.microedition.io.*;

import com.sun.squawk.*;
import com.sun.squawk.io.*;
import com.sun.squawk.vm.ChannelConstants;

/**
 *
 */
public class Protocol extends ConnectionBase implements InputConnection {
	
	private int memoryBase;

	public int getMemoryBase() {
		return memoryBase;
	}

	public InputStream openInputStream() {
		return new FlashInputStream(this);
	}

	public DataInputStream openDataInputStream() {
		return new FlashDataInputStream(new FlashInputStream(this));
	}

	public Connection open(String protocol, String shortUrl, int mode, boolean timeouts) throws IOException {
		if (mode != Connector.READ) {
			throw new IOException("illegal mode: " + mode);
		}
		String url = protocol + ":" + shortUrl;
		memoryBase = VM.execSyncIO(ChannelConstants.GET_FILE_VIRTUAL_ADDRESS, url.length(), 0, 0, 0, 0, 0, url, null);
		if (memoryBase == -1) {
			throw new IOException("Couldn't find the FlashFile for url " + url);
		}
		if (memoryBase == 0) {
			throw new IOException("The FlashFile for url " + url + " is not mapped to a virtual address");
		}
		return this;
	}

	static class FlashDataInputStream extends DataInputStream implements Pointer {
		public FlashDataInputStream(FlashInputStream mis) {
			super(mis);
		}

		public int getCurrentAddress() {
			return ((FlashInputStream) in).getCurrentAddress();
		}
	}

	static class FlashInputStream extends InputStream implements Pointer {
		private Protocol parent;

		private int currentMemoryPointer;

		public FlashInputStream(Protocol protocol) {
			parent = protocol;
			currentMemoryPointer = 0;
		}

		public int read() throws IOException {
			int signedValue = Unsafe.getByte(Address.fromPrimitive(getCurrentAddress()), 0);
			int result = signedValue & 0xff;
			currentMemoryPointer++;
			return result;
		}
		
		public int read(byte[] b, int off, int len) throws IOException {
			Unsafe.getBytes(Address.fromPrimitive(getCurrentAddress()), 0, b, off, len);
			currentMemoryPointer += len;
			return len;
		}

		public long skip(long n) throws IOException {
			currentMemoryPointer = (int) (n + currentMemoryPointer);
			return n;
		}

		public int getCurrentAddress() {
			return parent.getMemoryBase() + currentMemoryPointer;
		}
	}
}
