/*
 * Copyright 2006-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2010-2011 Oracle. All Rights Reserved.
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

package com.sun.squawk.flash;

import java.io.DataInputStream;

import com.sun.squawk.Address;
import com.sun.squawk.util.ByteArrayInputStreamWithSetBytes;

/**
 * In memory cache of a block of flash memory
 */
public class MemoryHeapBlock implements IMemoryHeapBlock {
    protected Address address;
    protected boolean isAllocated;
    protected int nextBlockOffset;
    protected byte[] bytes;
    protected int offset;
    protected int length;
    protected ByteArrayInputStreamWithSetBytes bytesIn;
    protected DataInputStream dataIn;

    public ByteArrayInputStreamWithSetBytes getByteArrayInputStream() {
        if (dataIn == null) {
            getDataInputStream();
        }
        return bytesIn;
    }
    
    public Address getAddress() {
        return address;
    }
    
    public byte[] getBytes() {
        return bytes;
    }
    
    public DataInputStream getDataInputStream() {
        if (dataIn == null) {
            bytesIn = new ByteArrayInputStreamWithSetBytes(bytes, offset, length);
            dataIn = new DataInputStream(bytesIn);
        }
        return dataIn;
    }
    
    public int getLength() {
        return length;
    }
    
    public int getNextBlockOffset() {
        return nextBlockOffset;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public boolean isAllocated() {
        return isAllocated;
    }
    
    public void resetBytes() {
        bytes = null;
        offset = length = 0;
        bytesIn = null;
        dataIn = null;
    }
    
    public void setAddress(Address address) {
        this.address = address;
    }
    
    public void setBytes(byte[] bytes, int offset, int length) {
        if ((bytes.length &1) == 1) {
            throw new IllegalArgumentException("bytes passed in must be even in length");
        }
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        if (bytesIn != null) {
            bytesIn.setBytes(bytes, offset, length);
        }
    }

    public void setLength(int length) {
        offset = 0;
        this.length = length;
        if (bytes == null || bytes.length < (length + (length & 1))) {
            bytes = new byte[length + (length & 1)];
        }
        if (bytesIn != null) {
            bytesIn.setBytes(bytes, offset, length);
        }
    }
    
    public void setIsAllocated(boolean isAllocated) {
        this.isAllocated = isAllocated;
    }
    
    public void setNextOffset(int offset) {
        nextBlockOffset = offset;
    }
    
}
