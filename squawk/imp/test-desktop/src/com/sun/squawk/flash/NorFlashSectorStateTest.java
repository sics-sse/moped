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

package com.sun.squawk.flash;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import org.jmock.integration.junit3.MockObjectTestCase;

import com.sun.squawk.Address;
import com.sun.squawk.flash.NorFlashSectorState;
import com.sun.squawk.flash.INorFlashSectorState;
import com.sun.squawk.peripheral.SimulatedNorFlashSector;

public class NorFlashSectorStateTest extends MockObjectTestCase {

    Address startAddress;
    int testSize;
    int testSizeAvailable;
    NorFlashSectorState castSector;
    INorFlashSectorState sector;

    public void setUp() throws Exception {
        this.testSize = 32;
        assertTrue((testSize / 2) > 0);
        startAddress = Address.zero();
        castSector = new NorFlashSectorState(new SimulatedNorFlashSector(startAddress, testSize, 0, false));
        sector = castSector;
    }

    public void tearDown() throws Exception {
        sector = null;
    }
    
    public void testCheckReadWriteParameters(int offset, byte[] buffer, int bufferStart, int bufferLength) {
        try {
            castSector.checkReadWriteParameters(offset, buffer, bufferStart, bufferLength);
            fail();
        } catch (IndexOutOfBoundsException e) {
        };
    }

    public void testCheckReadWriteParameters() {
        castSector.checkReadWriteParameters(-1, null, 0, 0);
        castSector.checkReadWriteParameters(0, null, -1, 0);
        
        testCheckReadWriteParameters(-1, null, 0, 2);
        testCheckReadWriteParameters(0, null, -1, 2);
        testCheckReadWriteParameters(0, null, 0, -1);

        testCheckReadWriteParameters(testSize, null, 0, 2);
        testCheckReadWriteParameters(testSize + 1, null, 0, 2);

        byte[] buffer = new byte[10];
        castSector.checkReadWriteParameters(testSize - 2, buffer, 0, 2);
        testCheckReadWriteParameters(0, buffer, 0, testSize);
        
        castSector.checkReadWriteParameters(0, buffer, 0, buffer.length);
        testCheckReadWriteParameters(0, buffer, 0, buffer.length + 2);
    }
    
    public void testErase() throws IOException, RecordStoreException {
        byte[] erasedMarker = NorFlashSectorState.ERASED_HEADER;
        final long sequence = 10;

        sector.incrementFreedBlockCount();
        sector.incrementAllocatedBlockCount();
        sector.erase(sequence);
        assertTrue(sector.hasErasedHeader());
        assertEquals(NorFlashSectorState.ERASED_HEADER_SIZE, sector.getWriteHeadPosition());
        assertEquals(0, sector.getFreedBlockCount());
        assertEquals(0, sector.getAllocatedBlockCount());
        byte[] bytes = new byte[testSize];
        sector.readBytes(0, bytes, 0, testSize);
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(bytes));
        assertEquals(NorFlashMemoryHeap.ERASED_VALUE_XOR, input.readByte());
        assertEquals(NorFlashMemoryHeap.ERASED_VALUE_XOR, input.readByte());
        for (int i=0; i < erasedMarker.length; i++) {
            assertEquals(erasedMarker[i], input.readByte());
        }
        assertEquals(input.readLong(), sequence);
        assertEquals(NorFlashMemoryHeap.ERASED_VALUE_XOR, input.readByte());
        assertEquals(NorFlashMemoryHeap.ERASED_VALUE_XOR, input.readByte());
    }
    
    public void testGetStartAddress() {
        assertSame(startAddress, sector.getStartAddress());
    }
    
    public void testGetSize() {
        assertEquals(testSize, sector.getSize());
    }

    public void testHasAvailableNegative() {
    }
    
    public void testGetWriteHeadPosition() throws RecordStoreException {
        int write = testSize;
        byte[] bytes = new byte[write];
        assertEquals(0, sector.getWriteHeadPosition());
        int startHead = sector.getWriteHeadPosition();
        sector.writeBytes(bytes, 0, write);
        assertEquals(startHead + write, sector.getWriteHeadPosition());
    }
    
    public void testGetWriteHeadAddress() throws RecordStoreException {
        Address[] addresses = new Address[] {Address.zero(), Address.fromPrimitive(10)};
        int[] offsets = new int[] {0, 2};
        
        for (Address address : addresses) {
            castSector.startAddress = address;
            for (int offset : offsets) {
                castSector.writeHead = offset;
                assertEquals(address.add(offset), castSector.getWriteHeadAddress());
            }
        }
    }
    
    public void testHasAvailableGreaterThanSize() {
        assertFalse(sector.hasAvailable(testSize + 1));
    }
    
    public void testHasAvailableAtEndLength1() throws RecordStoreException {
        byte[] bytes = new byte[testSize];
        sector.writeBytes(bytes, 0, bytes.length);
        assertFalse(sector.hasAvailable(1));
    }
    
    public void testHasAvailableAtEndLength0() throws RecordStoreException {
        byte[] bytes = new byte[testSize];
        sector.writeBytes(bytes, 0, bytes.length);
        sector.hasAvailable(0);
    }
    
    public void testHasAvailableContinuousWrite() throws RecordStoreException {
        byte[] bytes = new byte[2];
        for (int i=0; i < (testSize / bytes.length); i++) {
            sector.writeBytes(bytes, 0, bytes.length);
            assertTrue(sector.hasAvailable(testSize - (bytes.length * (i + 1))));
        }
    }

    public void testHasAvailableContinuousWriteFail() throws RecordStoreException {
        byte[] bytes = new byte[2];
        for (int i=0; i < (testSize / bytes.length); i++) {
            sector.writeBytes(bytes, 0, bytes.length);
            assertFalse(sector.hasAvailable(testSize + 2));
        }
    }

    public void testIncrementAlocatedBlockCount() {
        assertEquals(0, sector.getAllocatedBlockCount());
        sector.incrementAllocatedBlockCount();
        assertEquals(1, sector.getAllocatedBlockCount());
    }
    
    public void testIncrementFreedBlockCount() {
        assertEquals(0, sector.getFreedBlockCount());
        sector.incrementFreedBlockCount();
        assertEquals(1, sector.getFreedBlockCount());
    }
    
    /*
     * Ensure that a freshly created sector is in to be erased state
     */
    public void testInitState() {
        assertFalse(sector.hasErasedHeader());
    }
    
    /*
     * Test creating a sector whose size is smaller than the header we place
     */
    public void testInitState2() {
        try {
            ;
            castSector = new NorFlashSectorState(new SimulatedNorFlashSector(Address.zero(), new byte[] {0}, 0));
            sector = castSector;
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
    
    /*
     * Does sector get put into correct TO_BE_ERASED state when its created with content which
     * does not match the erased marker.
     */
    public void testInitState3() {
        final byte[] bytes = new byte[NorFlashSectorState.ERASED_HEADER_SIZE];
        castSector = new NorFlashSectorState(new SimulatedNorFlashSector(Address.zero(), bytes, 0));
        sector = castSector;
        assertFalse(sector.hasErasedHeader());
        assertSame(0, sector.getWriteHeadPosition());
    }
    
    /*
     * Does sector get put into correct DIRTY_OR_CLEAN state when its created with content which
     * does include the erased marker.
     */
    public void testInitState4() throws IOException {
        final long sequence = 10;
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(bytesOut);
        dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
        dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
        dataOut.write(NorFlashSectorState.ERASED_HEADER);
        dataOut.writeLong(sequence);
        dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
        dataOut.write(NorFlashMemoryHeap.ERASED_VALUE_XOR);
        castSector = new NorFlashSectorState(new SimulatedNorFlashSector(Address.zero(), bytesOut.toByteArray(), 0));
        sector = castSector;
        assertTrue(sector.hasErasedHeader());
        assertEquals(bytesOut.size(), sector.getWriteHeadPosition());
        assertEquals(sequence, castSector.sequence);
    }
    
    public void testReadBytes() throws RecordStoreException {
        byte[] bytes = new byte[testSize];
        sector.readBytes(0, bytes, 0, bytes.length);
    }

    public void testReadBytesByOne() throws RecordStoreException {
        byte[] bytes = new byte[testSize];
        for (int i=0; i < bytes.length; i++) {
            sector.readBytes(i, bytes, 0, 1);
        }
    }

    public void testReadBytesPassedEnd() throws RecordStoreException {
        try {
            byte[] bytes = new byte[testSize + 1];
            sector.readBytes(0, bytes, 0, bytes.length);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }
    
    public void testResetHead() {
        castSector.hasErasedHeader = false;
        sector.resetHead();
        assertSame(0, sector.getWriteHeadPosition());
        castSector.hasErasedHeader = true;
        sector.resetHead();
        assertEquals(NorFlashSectorState.ERASED_HEADER_SIZE, sector.getWriteHeadPosition());
    }
    public void testSetHeadPositionIndex() {
        sector.setWriteHeadPosition(testSize - 1);
        assertEquals(testSize - 1, sector.getWriteHeadPosition());
    }
    
    public void testSetHeadPositionIndexPassedEnd() {
        try {
            sector.setWriteHeadPosition(testSize + 1);
        } catch (IndexOutOfBoundsException e) {
        }
    }
    
    public void testWriteBytes0() throws RecordStoreException {
        sector.writeBytes(null, 0, 0);
    }

    /*
     * Write a single byte array
     */
    public void testWriteBytes() throws RecordStoreException {
        final byte[] bytes = new byte[testSize];
        sector.writeBytes(bytes, 0, bytes.length);
        assertEquals(testSize, sector.getWriteHeadPosition());
    }

    public void testWriteBytesBy2() throws RecordStoreException {
        final byte[] bytes = new byte[2];
        for (int i=0; i < (testSize / bytes.length); i++) {
            sector.writeBytes(bytes, 0, bytes.length);
        }
        assertEquals(testSize, sector.getWriteHeadPosition());
    }

    public void testWriteBytesPassedEnd() throws RecordStoreException {
        final byte[] bytes = new byte[testSize + 1];
        try {
            sector.writeBytes(bytes, 0, bytes.length);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }
    
    public void testWriteBytesBy1PassedEnd() throws RecordStoreException {
        final byte[] bytes = new byte[2];
        for (int i=0; i < (testSize / bytes.length); i++) {
            sector.writeBytes(bytes, 0, bytes.length);
        }
        try {
            sector.writeBytes(bytes, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException e) {
        }
    }

}
