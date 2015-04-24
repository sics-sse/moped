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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.peripheral.INorFlashSector;
import com.sun.squawk.util.Assert;

/**
 * Metadata for a NorFlashSector
 */
public class NorFlashSectorState implements INorFlashSectorState {
    public static final boolean DEBUG = false;

    public static final byte[] ERASED_HEADER = new byte[] {'S', 'Q', 'U', 'A', 'W', 'K'};
    // erased value (byte) + header.length + sequence (int) + erased value (byte)
    public static final int ERASED_HEADER_SIZE = 2 + ERASED_HEADER.length + 4 + 4;
    public static final byte[] BUFFER = new byte[ERASED_HEADER_SIZE];

    protected Address startAddress;
    protected Address endAddress;
    protected int writeHead;
    protected INorFlashSector flashSector;
    protected int sequence;
    protected INorFlashSectorState nextSector;
    protected INorFlashSectorStateList owningList;
    protected int mallocedBlockCount;
    protected int freedBlockCount;
    protected boolean hasErasedHeader;

    protected NorFlashSectorState() {
    }
    
    public NorFlashSectorState(INorFlashSector flashSector) {
        this.flashSector = flashSector;
        init(flashSector.getStartAddress(), flashSector.getSize());
    }

    public String toString() {
        if (DEBUG) {
            return "{NorFlashSectorState for " + startAddress.toUWord().toPrimitive() + ", size: " + getSize()
                    + " available: " + getAvailable()
                    + " malloc count: " + getAllocatedBlockCount()
                    + " free count: " + getFreedBlockCount()
                    + " erasedHeader: " + hasErasedHeader
                    + "}";
        } else {
            return super.toString();
        }
    }

    protected void checkReadWriteParameters(int offset, byte[] buffer, int bufferStart, int bufferLength) {
        if (bufferLength == 0) {
            return;
        }
        if (DEBUG) {
            String message;
            if (offset < 0 || bufferStart < 0 || bufferLength < 0) {
                message = "one of offset(" + offset + "), bufferStart(" + bufferStart + "), bufferLength(" + bufferLength + ") is < 0";
            } else if (offset >= flashSector.getSize()) {
                message = "offset(" + offset + ") >= size(" + flashSector.getSize() + ")";
            } else if ((offset + bufferLength) > getSize()) {
                message = "offset(" + offset + ") + bufferLength(" + bufferLength + ") > size(" + getSize() + ")";
            } else if (bufferStart >= buffer.length) {
                message = "bufferStart(" + bufferStart + ") >= buffer.length(" + buffer.length + ")";
            } else if (bufferStart + bufferLength > buffer.length) {
                message = "bufferStart(" + bufferStart + ") + bufferLength(" + bufferLength + ") > buffer.length(" + buffer.length + ")";
            } else {
                return;
            }
            throw new IndexOutOfBoundsException(message);
        } else {
            if (offset < 0 || bufferStart < 0 || bufferLength < 0) {
            } else if (offset >= flashSector.getSize()) {
            } else if ((offset + bufferLength) > getSize()) {
            } else if (bufferStart >= buffer.length) {
            } else if (bufferStart + bufferLength > buffer.length) {
            } else {
                return;
            }
           throw new IndexOutOfBoundsException();
        }
    }

    public void decrementMallocedCount() {
        mallocedBlockCount--;
    }

    public void erase(long sequence) throws RecordStoreException {
        flashSector.erase();
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(ERASED_HEADER_SIZE);
        DataOutputStream dataOut = new DataOutputStream(bytesOut);
        writeHead = 0;
        try {
            dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
            dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
            dataOut.write(ERASED_HEADER);
            dataOut.writeInt((int)sequence);
            dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
            dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
            //add 2 ERASED_VALUE_XOR
            dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
            dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
            Assert.that(bytesOut.size() == ERASED_HEADER_SIZE);
            dataOut.close();
        } catch (IOException e) {
            throw new RecordStoreException("Unexpected IO exception: " + e);
        }
        writeBytes(bytesOut.toByteArray(), 0, bytesOut.size());
        hasErasedHeader = true;
        freedBlockCount = 0;
        mallocedBlockCount = 0;
    }

    public void forceErase() throws RecordStoreException {
        // first 2 bytes are erased value xor, already
        flashSector.setBytes(2, new byte[] {NorFlashMemoryHeap.ERASED_VALUE_XOR, NorFlashMemoryHeap.ERASED_VALUE_XOR}, 0, 2);
    }

    public INorFlashSectorState getNextSector() {
        return nextSector;
    }
    
    public INorFlashSectorStateList getOwningList() {
        return owningList;
    }

    public Address getEndAddress() {
        return endAddress;
    }
    
    public int getFreedBlockCount() {
        return freedBlockCount;
    }

    public long getSequence() {
        return sequence;
    }

    public int getSize() {
        return flashSector.getSize();
    }

    public Address getStartAddress() {
        return startAddress;
    }

    public int getAllocatedBlockCount() {
        return mallocedBlockCount;
    }

    public Address getWriteHeadAddress() {
        return startAddress.add(writeHead);
    }
    
    public int getWriteHeadPosition() {
        return writeHead;
    }

    public int getAvailable() {
        return getSize() - writeHead;
    }
    
    public boolean hasAvailable(int length) {
        if (length == 0) {
            return true;
        }
        if ((length > 0) && (writeHead + length <= getSize())) {
            return true;
        }
        return false;
    }
    
    public boolean hasErasedHeader() {
        return hasErasedHeader;
    }

    public void incrementAllocatedBlockCount() {
        mallocedBlockCount++;
    }

    public void incrementFreedBlockCount() {
        freedBlockCount++;
    }

    public void init(Address startAddress, int size) {
        this.startAddress = startAddress;
        this.endAddress = startAddress.add(size);
        writeHead = 0;
        initState();
    }
    
    protected void initState() {
        if (flashSector.getSize() < ERASED_HEADER_SIZE) {
            throw new IllegalArgumentException("Sector size is too small to be used");
        }
        byte[] bytes = BUFFER;
        readBytes(0, bytes, 0, ERASED_HEADER_SIZE);
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            if (input.readByte() != NorFlashMemoryHeap.ERASED_VALUE_XOR) {
                return;
            }
            if (input.readByte() != NorFlashMemoryHeap.ERASED_VALUE_XOR) {
                return;
            }
            for (int i=0, max=ERASED_HEADER.length; i < max; i++) {
                if (input.readByte() != ERASED_HEADER[i]) {
                    return;
                }
            }
            sequence = input.readInt();
            if (input.readByte() != NorFlashMemoryHeap.ERASED_VALUE_XOR) {
                return;
            }
            if (input.readByte() != NorFlashMemoryHeap.ERASED_VALUE_XOR) {
                return;
            }
            if (input.readByte() != NorFlashMemoryHeap.ERASED_VALUE_XOR) {
                return;
            }
            if (input.readByte() != NorFlashMemoryHeap.ERASED_VALUE_XOR) {
                return;
            }
        } catch (IOException e) {
            throw new RuntimeException("System error: " + e);
        }
        writeHead = bytes.length;
        hasErasedHeader = true;
    }
    
    public void readBytes(int offset, byte[] buffer, int bufferStart, int bufferLength) {
        // If zero bytes being requested then do nothing
        if (bufferLength == 0) {
            return;
        }
        checkReadWriteParameters(offset, buffer, bufferStart, bufferLength);
        flashSector.getBytes(offset, buffer, bufferStart, bufferLength);
    }
    
    public void removeErasedHeader() throws RecordStoreException {
        byte[] bytes = new byte[ERASED_HEADER_SIZE];
        for (int i=0; i < ERASED_HEADER_SIZE; i++) {
            bytes[i] = NorFlashMemoryHeap.ERASED_VALUE_XOR;
        }
        writeBytes(0, bytes, 0, ERASED_HEADER_SIZE);
        hasErasedHeader = false;
        freedBlockCount = 0;
        mallocedBlockCount = 0;
    }

    public void resetHead() {
        if (hasErasedHeader) {
            writeHead = ERASED_HEADER_SIZE;
        } else {
            writeHead = 0;
        }
    }
    
    public void setNextSector(INorFlashSectorState next) {
        this.nextSector = next;
    }

    public void setOwningList(INorFlashSectorStateList list) {
        this.owningList = list;
    }

    public void setWriteHeadPosition(int position) {
        writeHead = position;
    }

    /**
     * Write bytes to the flash sector at the current "writeHead" position.
     *
     * @param buffer
     * @param bufferStart
     * @param bufferLength
     */
    public void writeBytes(byte buffer[], int bufferStart, int bufferLength) {
        writeBytes(writeHead, buffer, bufferStart, bufferLength);
        writeHead += bufferLength;
    }

    /**
     * Write bytes to the flash sector starting at "offset".
     * 
     * @param offset
     * @param buffer
     * @param bufferStart
     * @param bufferLength
     */
    public void writeBytes(int offset, byte buffer[], int bufferStart, int bufferLength) {
        // Special case to handle no-zero values for bufferStart and a zero value for bufferLength
        // which causes an ArrayIndexOutOfBoundsException on System.arrayCopy
        if (bufferLength == 0) {
            return;
        }
        checkReadWriteParameters(offset, buffer, bufferStart, bufferLength);
        flashSector.setBytes(offset, buffer, bufferStart, bufferLength);
    }

}
