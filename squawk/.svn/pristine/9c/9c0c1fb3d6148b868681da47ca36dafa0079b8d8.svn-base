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

package com.sun.squawk.peripheral;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;

import com.sun.squawk.Address;
import com.sun.squawk.util.Arrays;

/**
 * I am a class that allows the RMS store to be used in Java heap as a byte array.  This is to
 * theoretically simplify testing.
 *
 */
public class SimulatedNorFlashSector implements INorFlashSector {

    public static final String SECTORS_FILE_EXTENSION = ".nor-flash";
    public static final String ZERO_PADDING = "00000000";
    
    protected int purpose;
    protected int size;
    protected Address startAddress;
    protected byte[] bytes;
    protected String fileName;

    public SimulatedNorFlashSector(Address startAddress, byte[] bytes, int purpose) {
        init(startAddress, bytes.length, purpose);
        this.bytes = bytes;
    }

    public SimulatedNorFlashSector(Address startAddress, int size, int purpose, boolean useFile) {
        init(startAddress, size, purpose);
        if (useFile) {
            String string = Integer.toHexString(startAddress.toUWord().toInt());
            if (string.length() < ZERO_PADDING.length()) {
                string = ZERO_PADDING + string;
                string = string.substring(string.length() - ZERO_PADDING.length());
            }
            this.fileName = "sector-" + string + SECTORS_FILE_EXTENSION;
        }
        this.bytes = new byte[size];
        erase();
    }

    public SimulatedNorFlashSector(String fileName) throws IOException {
        this.fileName = fileName;
        DataInputStream input = Connector.openDataInputStream("file://" + fileName);
        Address startAddress = Address.fromPrimitive(input.readInt());
        int size = input.readInt();
        int purpose = input.readShort();
        init(startAddress, size, purpose);
        bytes = new byte[input.available()];
        input.read(bytes, 0, bytes.length);
        input.close();
    }

    void ensureInBounds(int memoryOffset, byte[] buffer, int bufferOffset, int length) {
        if (memoryOffset > size || memoryOffset < 0) {
            throw new IndexOutOfBoundsException("sectorSize: " + size + "memoryOffset: " + memoryOffset);
        }
        if ((memoryOffset + length) > size) {
            throw new IndexOutOfBoundsException("sectorSize: " + size + "memoryOffset: " + memoryOffset + " length: " + length);
        }
        int bufferSize = buffer.length;
        if (bufferOffset > bufferSize || bufferOffset < 0) {
            throw new IndexOutOfBoundsException("bufferSize: " + bufferSize + "bufferOffset: " + bufferOffset);
        }
        if ((bufferOffset + length) > bufferSize) {
            throw new IndexOutOfBoundsException("bufferSize: " + bufferSize + "bufferOffset: " + bufferOffset + " length: " + length);
        }
    }

    public void erase() {
        Arrays.fill(bytes, 0, bytes.length, getErasedValue());
        setBytes(0, bytes, 0, size);
    }
    
    public void getBytes(int memoryOffset, byte[] buffer, int bufferOffset, int length) {
        if (length == 0) {
            return;
        }
        ensureInBounds(memoryOffset, buffer, bufferOffset, length);
        System.arraycopy(bytes, memoryOffset, buffer, bufferOffset, length);
    }
    
    public byte getErasedValue() {
        return (byte) 0xFF;
    }
    
    public int getPurpose() {
        return purpose;
    }
    
    public int getSize() {
        return size;
    }
    
    public Address getStartAddress() {
        return startAddress;
    }
    
    protected void init(Address startAddress, int size, int purpose) {
        if (size <= 0) {
            throw new IllegalArgumentException("Sector size must be greater than 0");
        }
        this.startAddress = startAddress;
        this.size = size;
        this.purpose = purpose;
    }
    
    public void setBytes(int memoryOffset, byte[] buffer, int bufferOffset, int length) {
        if (length == 0) {
            return;
        }
        if ((memoryOffset & 1) == 1) {
            throw new IndexOutOfBoundsException("offset must be even");
        }
        if ((length & 1) == 1) {
            throw new IndexOutOfBoundsException("length must be even");
        }
        ensureInBounds(memoryOffset, buffer, bufferOffset, length);
        System.arraycopy(buffer, bufferOffset, bytes, memoryOffset, length);
        if (fileName == null) {
            return;
        }
        try {
            DataOutputStream output = Connector.openDataOutputStream("file://" + fileName);
            output.writeInt(startAddress.toUWord().toInt());
            output.writeInt(size);
            output.writeShort(purpose);
            output.write(bytes);
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
