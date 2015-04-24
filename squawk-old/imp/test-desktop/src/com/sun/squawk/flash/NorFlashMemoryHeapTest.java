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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import org.jmock.InAnyOrder;
import org.jmock.InThisOrder;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.action.CustomAction;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.peripheral.SimulatedNorFlashSector;
import com.sun.squawk.peripheral.INorFlashSector;
import com.sun.squawk.peripheral.SimulatedNorFlashSectorAllocator;
import com.sun.squawk.util.UnexpectedException;

public class NorFlashMemoryHeapTest extends MockObjectTestCase {
    protected INorFlashMemoryHeap heap;
    protected NorFlashMemoryHeap castHeap;
    
    @Override
    public void setUp() {
        castHeap = new NorFlashMemoryHeap();
        heap = castHeap;
    }

    /*
     * constructor(ILogSector[])
     */
    public void test1() throws IOException {
        INorFlashSectorState sector = mock(INorFlashSectorState.class);
        INorFlashSectorState[] sectors = new INorFlashSectorState[] {sector};
        castHeap = new NorFlashMemoryHeap(sectors);
        assertSame(sectors, castHeap.sectorStates);
    }
    
    /*
     * constructor(int purpose), and no sectors setup
     */
    public void test2() throws IOException {
        try {
            new NorFlashMemoryHeap(INorFlashSector.RMS_PURPOSED);
            fail();
        } catch (RuntimeException e) {
        }
    }
    
    /*
     * constructor(int purpose)
     */
    public void test3() throws IOException {
        final int numSectors = 1;
        SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(numSectors, 1024, INorFlashSector.RMS_PURPOSED, false);
        
        castHeap = new NorFlashMemoryHeap(INorFlashSector.RMS_PURPOSED);
        
        SimulatedNorFlashSectorAllocator.getSingleton().uninstallSectors();
        
        assertSame(numSectors, castHeap.sectorStates.length);
    }
    
    public void testAllocateAndWriteBlock() throws RecordStoreException {
        final byte[] bytes = new byte[10];
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final Address address = Address.fromPrimitive(9);
        castHeap.currentSectorState = sector;
        castHeap.hasScannedBlocks = true;
        
        expects(new InThisOrder() {{
            one(sector).hasAvailable(NorFlashMemoryHeap.BLOCK_HEADER_SIZE + bytes.length + 2);will(returnValue(true));
            one(sector).getWriteHeadAddress();will(returnValue(address));
            one(sector).writeBytes(with(an(byte[].class)), with(equal(0)), with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)));
            one(sector).writeBytes(bytes, 0, bytes.length);
            one(sector).writeBytes(with(an(byte[].class)), with(equal(0)), with(equal((bytes.length & 1) + 2)));
            one(sector).incrementAllocatedBlockCount();
            one(sector).getStartAddress();will(returnValue(Address.zero()));
        }});
        
        Address mallocAddress = heap.allocateAndWriteBlock(bytes, 0, bytes.length, null);
        assertNotNull(mallocAddress);
        assertSame(address, mallocAddress);
    }

    /*
     * Write to an actual sector an verify that the bytes we are expecting are written
     */
    public void testAllocateAndWriteBlock2() throws RecordStoreException {
        final byte[] block = new byte[10];
        for (int i=0; i < block.length; i++) {
            block[i] = (byte) i;
        }
        final Address address = Address.fromPrimitive(9);
        final NorFlashSectorState sector = new NorFlashSectorState(new SimulatedNorFlashSector(address, 1024, 0, false));
        castHeap.currentSectorState = sector;
        castHeap.hasScannedBlocks = true;
        
        Address mallocAddress = heap.allocateAndWriteBlock(block, 0, block.length, null);
        byte[] bytes = new byte[sector.getWriteHeadPosition()];
        sector.flashSector.getBytes(0, bytes, 0, bytes.length);
        assertEquals(0, bytes[0]);
        assertEquals(0, bytes[1]);
        assertEquals(0, bytes[2]);
        assertEquals(block.length, bytes[3]);
        assertEquals((byte) 0xFF, bytes[4]);
        assertEquals((byte) 0xFF, bytes[5]);
        for (int i=0; i < block.length; i++) {
            assertEquals((byte) i, bytes[6+i]);
        }
        assertSame(address, mallocAddress);
    }
    
    /*
     * Test case of free block where the sector is not on the to be erased queue
     */
    public void testFreeBlockAt1() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final Address address = Address.fromPrimitive(12);
        final INorFlashSectorStateList inUseList = mock(INorFlashSectorStateList.class);
        final INorFlashSectorStateList toBeErasedList = mock(INorFlashSectorStateList.class);
        castHeap.sectorStates = new INorFlashSectorState[] {sector};
        castHeap.inUseSectorStateList = inUseList;
        castHeap.toBeErasedSectorStateList = toBeErasedList;
        castHeap.hasScannedBlocks = true;
        
        expects(new InAnyOrder() {{
            allowing(sector).getStartAddress();will(returnValue(Address.zero()));
            allowing(sector).getEndAddress();will(returnValue(Address.zero().add(1024)));
            one(sector).writeBytes(with(equal(address.toUWord().toInt() + NorFlashMemoryHeap.BLOCK_HEADER_SIZE - 2)), with(an(byte[].class)), with(equal(0)), with(equal(2)));
            one(sector).decrementMallocedCount();
            one(sector).incrementFreedBlockCount();
            one(sector).getOwningList();will(returnValue(castHeap.toBeErasedSectorStateList));
        }});
        
        heap.freeBlockAt(address);
    }

    /*
     * Test case of free block where the sector is not on the to be erased queue and has no malloced blocks left
     */
    public void testFreeBlockAt2() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final Address address = Address.fromPrimitive(12);
        final INorFlashSectorStateList inUseList = mock(INorFlashSectorStateList.class);
        final INorFlashSectorStateList toBeErasedList = mock(INorFlashSectorStateList.class);
        castHeap.sectorStates = new INorFlashSectorState[] {sector};
        castHeap.inUseSectorStateList = inUseList;
        castHeap.toBeErasedSectorStateList = toBeErasedList;
        castHeap.hasScannedBlocks = true;

        expects(new InAnyOrder() {{
            allowing(sector).getStartAddress();will(returnValue(Address.zero()));
            allowing(sector).getEndAddress();will(returnValue(Address.zero().add(1024)));
            one(sector).writeBytes(with(equal(address.toUWord().toInt() + NorFlashMemoryHeap.BLOCK_HEADER_SIZE - 2)), with(an(byte[].class)), with(equal(0)), with(equal(2)));
            one(sector).decrementMallocedCount();
            one(sector).incrementFreedBlockCount();
            one(sector).getOwningList();will(returnValue(castHeap.inUseSectorStateList));
            one(sector).getAllocatedBlockCount();will(returnValue(0));
            one(toBeErasedList).addLast(sector);
        }});
        
        heap.freeBlockAt(address);
    }

    // No sector contains the specified address
    public void testGetBlockAtAddress1() {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        castHeap.sectorStates = new INorFlashSectorState[] {sector};
        
        expects(new InThisOrder() {{
            one(sector).getStartAddress();will(returnValue(Address.fromPrimitive(1)));
            one(sector).getSize();will(returnValue(1));
        }});
        
        try {
            heap.getBlockAt(Address.zero());
            fail();
        } catch (RecordStoreException e) {
        }
    }
    
    public void testGetBlockAtAddress2(boolean blocksScanned) throws IOException, RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        castHeap.sectorStates = new INorFlashSectorState[] {sector};
        
        expects(new InAnyOrder() {{
            allowing(sector).getStartAddress();will(returnValue(Address.zero()));
            allowing(sector).getEndAddress();will(returnValue(Address.zero().add(1)));
            one(sector).getWriteHeadPosition();will(returnValue(0));
        }});

        IMemoryHeapBlock block = heap.getBlockAt(Address.zero());
        assertNull(block);
    }
    
    // Reading at or past the head position of the sector
    public void testGetBlockAtAddress2() throws IOException, RecordStoreException {
        testGetBlockAtAddress2(true);
        testGetBlockAtAddress2(false);
    }
    
    // testing of read block that is both smaller and larger than sectorSize. 
    public void testGetBlockAtAddress3(final int sectorSize, final int blockSize) throws IOException, RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        castHeap.sectorStates = new INorFlashSectorState[] {sector};
        
        expects(new InAnyOrder() {{
            allowing(sector).getStartAddress();will(returnValue(Address.fromPrimitive(0)));
            allowing(sector).getEndAddress();will(returnValue(Address.zero().add(sectorSize)));
            one(sector).getWriteHeadPosition();will(returnValue(1));
            allowing(sector).getSize();will(returnValue(sectorSize));
            if (blockSize < sectorSize) {
                one(sector).readBytes(with(equal(0)), with(an(byte[].class)), with(equal(0)), with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)));will(new CustomAction("") {
                    public Object invoke(Invocation invocation) throws Throwable {
                        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                        DataOutputStream dataOut = new DataOutputStream(bytesOut);
                        dataOut.writeInt(blockSize);
                        dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                        dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                        for (int i=0; i < blockSize; i++) {
                            bytesOut.write(0);
                        }
                        System.arraycopy(bytesOut.toByteArray(), 0, (byte[]) invocation.getParameter(1), 0, NorFlashMemoryHeap.BLOCK_HEADER_SIZE);
                        return null;
                    }
                });
                one(sector).readBytes(with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)), with(an(byte[].class)), with(equal(0)), with(equal(blockSize + (blockSize & 1) + 2)));will(new CustomAction("") {
                    public Object invoke(Invocation invocation) throws Throwable {
                        byte[] bytes = ((byte[]) invocation.getParameter(1));
                        int index = (Integer) invocation.getParameter(3);
                        bytes[index-2] = NorFlashMemoryHeap.ERASED_VALUE_XOR;
                        bytes[index-1] = NorFlashMemoryHeap.ERASED_VALUE_XOR;
                        return null;
                    }
                });
            }
        }});

        try {
            IMemoryHeapBlock block = heap.getBlockAt(Address.zero());
            if (blockSize >= sectorSize) {
                assertNull(block);
            } else {
                assertNotNull(block);
                assertEquals(blockSize, block.getLength());
            }
        } catch (RecordStoreException e) {
            if (blockSize < sectorSize) {
                fail();
            }
        }
    }
    
    // Block size read results in chunk larger than what is left in sector
    public void testGetBlockAtAddress3() throws IOException, RecordStoreException {
        testGetBlockAtAddress3(10, 1);
        testGetBlockAtAddress3(1, 10);
    }

    // address in a valid sector and not passed end of the sector's write head, so normal case
    public void testGetBlockAtAddress4() throws IOException, RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final int sectorSize = 1024;
        castHeap.sectorStates = new INorFlashSectorState[] {sector};
        
        expects(new InAnyOrder() {{
            allowing(sector).getStartAddress();will(returnValue(Address.zero()));
            allowing(sector).getEndAddress();will(returnValue(Address.zero().add(sectorSize)));
            one(sector).getWriteHeadPosition();will(returnValue(100));
            one(sector).readBytes(with(equal(0)), with(an(byte[].class)), with(equal(0)), with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)));will(new CustomAction("copy bytes") {
                public Object invoke(Invocation invocation) throws Throwable {
                    byte[] bytes = (byte[]) invocation.getParameter(1);
                    bytes[0] = 0;
                    bytes[1] = 0;
                    bytes[2] = 0;
                    bytes[3] = 0;
                    bytes[4] = NorFlashMemoryHeap.ERASED_VALUE;
                    bytes[5] = NorFlashMemoryHeap.ERASED_VALUE;
                    return null;
                }
            });
            one(sector).readBytes(with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)), with(an(byte[].class)), with(equal(0)), with(equal(2)));will(new CustomAction("copy bytes") {
                public Object invoke(Invocation invocation) throws Throwable {
                    byte[] bytes = (byte[]) invocation.getParameter(1);
                    bytes[0] = NorFlashMemoryHeap.ERASED_VALUE_XOR;
                    bytes[1] = NorFlashMemoryHeap.ERASED_VALUE_XOR;
                    return null;
                }
            });
            allowing(sector).getSize();will(returnValue(sectorSize));
        }});

        IMemoryHeapBlock block = heap.getBlockAt(Address.zero());
        assertNotNull(block);
        assertNotNull(block.getAddress());
    }

    // encounter the erased value on reading the entry size, indicating the end block written to that sector
    public void testGetBlockAtSector1() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final int offset = 10;
        final Address address = Address.fromPrimitive(11);
        
        expects(new InAnyOrder() {{
            one(sector).getSize();will(returnValue(1024));
            one(sector).getStartAddress();will(returnValue(address));
            one(sector).readBytes(with(equal(offset)), with(an(byte[].class)), with(equal(0)), with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)));will(new CustomAction("") {
                public Object invoke(Invocation invocation) throws Throwable {
                    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                    DataOutputStream dataOut = new DataOutputStream(bytesOut);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    assertEquals(bytesOut.size(), NorFlashMemoryHeap.BLOCK_HEADER_SIZE);
                    System.arraycopy(bytesOut.toByteArray(), 0, (byte[]) invocation.getParameter(1), 0, NorFlashMemoryHeap.BLOCK_HEADER_SIZE);
                    return null;
                }
            });
        }});
        MemoryHeapBlock block = new MemoryHeapBlock();
        assertFalse(castHeap.getBlockAt(block, sector, offset));
        assertSame(address.add(offset), block.getAddress());
    }
    
    public void testGetBlockAtSector2() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final int offset = 10;
        final MemoryHeapBlock block = new MemoryHeapBlock();
        final Address address = Address.fromPrimitive(11);
        final int sectorSize = 1024;

        expects(new InAnyOrder() {{
            one(sector).getSize();will(returnValue(sectorSize));
            one(sector).getStartAddress();will(returnValue(address));
            one(sector).readBytes(with(equal(offset)), with(an(byte[].class)), with(equal(0)), with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)));will(new CustomAction("") {
                public Object invoke(Invocation invocation) throws Throwable {
                    block.setBytes(new byte[] {0, 0}, 0, 1);
                    return null;
                }
            });
        }});
        try {
            castHeap.getBlockAt(block, sector, offset);
            fail();
        } catch (UnexpectedException e) {
        }
    }
    
    // Test the throwing of the RecordStoreException("This block did not finish being written") exception
    public void testGetBlockAtSector3() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final int offset = 10;
        final MemoryHeapBlock block = new MemoryHeapBlock();
        final Address address = Address.fromPrimitive(11);
        final int sectorSize = 1024;
        
        expects(new InAnyOrder() {{
            one(sector).getSize();will(returnValue(sectorSize));
            one(sector).getStartAddress();will(returnValue(address));
            one(sector).readBytes(with(equal(offset)), with(an(byte[].class)), with(equal(0)), with(equal(NorFlashMemoryHeap.BLOCK_HEADER_SIZE)));will(new CustomAction("") {
                public Object invoke(Invocation invocation) throws Throwable {
                    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                    DataOutputStream dataOut = new DataOutputStream(bytesOut);
                    dataOut.writeInt(0);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    dataOut.writeByte(NorFlashMemoryHeap.ERASED_VALUE);
                    System.arraycopy(bytesOut.toByteArray(), 0, (byte[]) invocation.getParameter(1), 0, NorFlashMemoryHeap.BLOCK_HEADER_SIZE);
                    return null;
                }
            });
            allowing(sector).getSize();will(returnValue(sectorSize));
            one(sector).readBytes(with(equal(offset + NorFlashMemoryHeap.BLOCK_HEADER_SIZE)), with(an(byte[].class)), with(equal(0)), with(equal(2)));will(new CustomAction("") {
                public Object invoke(Invocation invocation) throws Throwable {
                    byte[] bytes = (byte []) invocation.getParameter(1);
                    bytes[0] = NorFlashMemoryHeap.ERASED_VALUE;
                    bytes[1] = NorFlashMemoryHeap.ERASED_VALUE;
                    return null;
                }
            });
        }});
        try {
            castHeap.getBlockAt(block, sector, offset);
            assertFalse(block.isAllocated());
        } catch (RecordStoreException e) {
            fail();
        }
    }
    
    public void testGetSectorContaining(final int numSectors, final int sectorSize) {
        final Address[] addresses = new Address[numSectors];
        Address endAddress = Address.fromPrimitive(0);
        for (int i=0; i < numSectors; i++) {
            addresses[i] = endAddress;
            endAddress = endAddress.add(sectorSize);
        }
        final INorFlashSectorState[] sectors = new INorFlashSectorState[addresses.length];
        castHeap.sectorStates = sectors;
        expects(new InAnyOrder() {{
            for (int i=0; i < sectors.length; i++) {
                INorFlashSectorState sector = mock(INorFlashSectorState.class, "sector-" + i);
                sectors[i] = sector;
                allowing(sector).getStartAddress();will(returnValue(addresses[i]));
                allowing(sector).getEndAddress();will(returnValue(addresses[i].add(sectorSize)));
            }
        }});
        int max = endAddress.toUWord().toPrimitive();
        for (int i=0; i < max; i++) {
            INorFlashSectorState sector = castHeap.getSectorContaining(Address.fromPrimitive(i));
            int index = i / sectorSize;
            assertSame("index: " + i, sectors[index], sector);
        }
        assertNull(castHeap.getSectorContaining(Address.fromPrimitive(max)));
        assertNull(castHeap.getSectorContaining(Address.fromPrimitive(max + 1)));
    }
    
    public void testGetSectorContainingEven() {
        testGetSectorContaining(16, 10);
    }
    
    public void testGetSectorContainingOdd() {
        testGetSectorContaining(17, 10);
    }
    
    public void testGetSizeAvailable1() throws RecordStoreException {
        heap = castHeap;
        castHeap.hasScannedBlocks = true;
        castHeap.toBeErasedSectorStateList = mock(INorFlashSectorStateList.class);

        expects(new InThisOrder() {{
            INorFlashSectorState sector1 = mock(INorFlashSectorState.class);
            INorFlashSectorState sector2 = mock(INorFlashSectorState.class);
            castHeap.sectorStates = new INorFlashSectorState[] {sector1, sector2};
            one(sector1).getSequence();will(returnValue(1L));
            one(sector2).getSequence();will(returnValue(2L));
            one(sector1).hasErasedHeader();will(returnValue(false));
            one(castHeap.toBeErasedSectorStateList).addLast(sector1);
            one(sector2).hasErasedHeader();will(returnValue(false));
            one(castHeap.toBeErasedSectorStateList).addLast(sector2);
            one(sector1).getSize();will(returnValue(2));
            one(sector2).getSize();will(returnValue(10));
        }});
        
        int size = heap.getSizeAvailable();
        assertEquals(2 + 10, size);
    }
    
    public void testGetUserNorFlashSectors() {
        // TODO: Need to fix this test
        if (true) {
            return;
        }
        VM.getPeripheralRegistry().removeAll(INorFlashSector.class);
        assertSame(0, VM.getPeripheralRegistry().getAll(INorFlashSector.class).length);
        final INorFlashSector sector1 = mock(INorFlashSector.class);
        final INorFlashSector sector2 = mock(INorFlashSector.class);
        final INorFlashSector sector3 = mock(INorFlashSector.class);

        expects(new InAnyOrder() {{
            allowing(sector1).getPurpose(); will(returnValue(INorFlashSector.RMS_PURPOSED));
            allowing(sector2).getPurpose(); will(returnValue(INorFlashSector.SYSTEM_PURPOSED));
            allowing(sector3).getPurpose(); will(returnValue(INorFlashSector.RMS_PURPOSED));
        }});
//        VM.getPeripheralRegistry().add(sector1);
//        VM.getPeripheralRegistry().add(sector2);
//        VM.getPeripheralRegistry().add(sector3);
        
        Vector<?> sectors = NorFlashMemoryHeap.getNorFlashSectors(INorFlashSector.RMS_PURPOSED);
        assertSame(2, sectors.size());
        assertTrue(sectors.indexOf(sector1) >= 0);
        assertTrue(sectors.indexOf(sector3) >= 0);
        
        VM.getPeripheralRegistry().removeAll(INorFlashSector.class);
    }
    
    public void testIncrementErasedSequence() {
        assertEquals(0, castHeap.erasedSequenceCurrentValue);
        assertEquals(1, castHeap.incrementErasedSequence());
        assertEquals(1, castHeap.erasedSequenceCurrentValue);
    }
    
    public void testInit() {
        final INorFlashSectorState sector1 = mock(INorFlashSectorState.class);
        final INorFlashSectorState sector2 = mock(INorFlashSectorState.class);
        final INorFlashSectorState sector3 = mock(INorFlashSectorState.class);
        final INorFlashSectorState sector4 = mock(INorFlashSectorState.class);
        INorFlashSectorState[] sectors = new INorFlashSectorState[] {sector4, sector3, sector2, sector1};
        
        expects(new InAnyOrder() {{
            allowing(sector1).getStartAddress();will(returnValue(Address.fromPrimitive(1)));
            allowing(sector1).getSequence();will(returnValue(1L));
            allowing(sector2).getStartAddress();will(returnValue(Address.fromPrimitive(2)));
            allowing(sector2).getSequence();will(returnValue(2L));
            allowing(sector3).getStartAddress();will(returnValue(Address.fromPrimitive(3)));
            allowing(sector3).getSequence();will(returnValue(3L));
            allowing(sector4).getStartAddress();will(returnValue(Address.fromPrimitive(4)));
            allowing(sector4).getSequence();will(returnValue(3L));
        }});
        
        castHeap.init(sectors);
        assertSame(sectors, castHeap.sectorStates);
        assertSame(castHeap.sectorStates[0], sector1);
        assertSame(castHeap.sectorStates[1], sector2);
        assertSame(castHeap.sectorStates[2], sector3);
        assertSame(castHeap.sectorStates[3], sector4);
        assertSame(0, castHeap.erasedSequenceCurrentValue);
        assertNotNull(castHeap.inUseSectorStateList);
        assertNotNull(castHeap.toBeErasedSectorStateList);
    }

    /*
     * currentSector not null and has room
     */
    public void testMakeRoomToWrite1() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        castHeap.currentSectorState = sector;
        
        expects(new InThisOrder() {{
            one(sector).hasAvailable(with(an(Integer.class)));will(returnValue(true));
        }});
        
        castHeap.makeRoomToWrite(0, null);
        assertSame(sector, castHeap.currentSectorState);
    }
    
    /*
     * currentSector does not have room, test which queue it gets put into
     */
    public void testMakeRoomToWrite2() throws RecordStoreException {
        final INorFlashSectorState sector = mock(INorFlashSectorState.class);
        final INorFlashSectorStateList inUseList = mock(INorFlashSectorStateList.class);
        final INorFlashSectorStateList toBeErasedList = mock(INorFlashSectorStateList.class);
        castHeap.sectorStates = new INorFlashSectorState[] {sector};
        castHeap.currentSectorState = sector;
        castHeap.inUseSectorStateList = inUseList;
        castHeap.toBeErasedSectorStateList = toBeErasedList;
        castHeap.hasScannedBlocks = true;
        
        expects(new InThisOrder() {{
            one(sector).hasAvailable(with(an(Integer.class)));will(returnValue(false));
            one(sector).getAllocatedBlockCount();will(returnValue(0));
            one(toBeErasedList).addLast(sector);
            one(toBeErasedList).consumeFirst();will(returnValue(null));
        }});
        
        try {
            castHeap.makeRoomToWrite(0, null);
            fail();
        } catch (RecordStoreFullException e) {
        }
        assertNull(castHeap.currentSectorState);

        castHeap.currentSectorState = sector;
        
        expects(new InThisOrder() {{
            one(sector).hasAvailable(with(an(Integer.class)));will(returnValue(false));
            one(sector).getAllocatedBlockCount();will(returnValue(1));
            one(inUseList).addLast(sector);
            one(toBeErasedList).consumeFirst();will(returnValue(null));
        }});
        
        try {
                castHeap.makeRoomToWrite(0, null);
                fail();
        } catch (RecordStoreFullException e) {
        }
        assertNull(castHeap.currentSectorState);
}
    
    public void testMakeRoomToWrite4() throws RecordStoreException {
        final INorFlashSectorState sector1 = mock(INorFlashSectorState.class, "sector1");
        final INorFlashSectorState sector2 = mock(INorFlashSectorState.class, "sector2");
        castHeap.sectorStates = new INorFlashSectorState[] {sector1, sector2};
        castHeap.currentSectorState = sector1;
        castHeap.toBeErasedSectorStateList = mock(INorFlashSectorStateList.class);
        
        expects(new InAnyOrder() {{
            one(sector1).hasAvailable(0);will(returnValue(false));
            one(sector1).getAllocatedBlockCount();will(returnValue(0));
            one(castHeap.toBeErasedSectorStateList).addLast(sector1);
            one(castHeap.toBeErasedSectorStateList).size();will(returnValue(2));
            one(castHeap.toBeErasedSectorStateList).consumeFirst();will(returnValue(sector2));
            one(sector2).erase(1L);
            one(sector2).hasAvailable(with(an(Integer.class)));will(returnValue(true));
        }});
        
        castHeap.makeRoomToWrite(0, null);
        assertSame(sector2, castHeap.currentSectorState);
    }
    
    /*
     * None of the sectors need to be erased, and current does not have room
     */
    public void testMakeRoomToWrite5() throws RecordStoreException {
        final INorFlashSectorState sector1 = mock(INorFlashSectorState.class, "sector1");
        final INorFlashSectorState sector2 = mock(INorFlashSectorState.class, "sector2");
        castHeap.sectorStates = new INorFlashSectorState[] {sector1, sector2};
        castHeap.currentSectorState = sector1;
        castHeap.toBeErasedSectorStateList = mock(INorFlashSectorStateList.class);
        
        expects(new InAnyOrder() {{
            one(sector1).hasAvailable(0);will(returnValue(false));
            one(sector1).getAllocatedBlockCount();will(returnValue(0));
            one(castHeap.toBeErasedSectorStateList).addLast(sector1);
            one(castHeap.toBeErasedSectorStateList).size();will(returnValue(2));
            one(castHeap.toBeErasedSectorStateList).consumeFirst();will(returnValue(sector2));
            one(sector2).erase(1L);
            one(sector2).hasAvailable(0);will(returnValue(false));
            one(sector2).getSize();will(returnValue(0));
        }});
        
        try {
            castHeap.makeRoomToWrite(0, null);
            fail();
        } catch (RecordStoreFullException e) {
        }
        assertSame(sector2, castHeap.currentSectorState);
    }

    /*
     * test scanBlocks()
     */
    public void testScanBlocks() throws RecordStoreException {
        final boolean[] calledScanBlocksHandle = new boolean[1];
        castHeap = new NorFlashMemoryHeap() {
            @Override
            public void scanBlocks(INorFlashMemoryHeapScanner scanner) throws RecordStoreException {
                assertNull(scanner);
                calledScanBlocksHandle[0] = true;
            }
        };
        heap = castHeap;
        
        castHeap.scanBlocks(null);
        assertTrue(calledScanBlocksHandle[0]);
    }
    
    /*
     * 
     */
    public void testScanBlocksWithScanner() throws RecordStoreException {
        final INorFlashSectorState sector1 = mock(INorFlashSectorState.class, "sector1");
        final INorFlashSectorState sector2 = mock(INorFlashSectorState.class, "sector2");
        final INorFlashSectorState sector3 = mock(INorFlashSectorState.class, "sector3");
        // key: put them in a different order, so that we can test outcome of sort happening in scan
        INorFlashSectorState[] sectors = new INorFlashSectorState[] {sector3, sector2, sector1};
        final INorFlashSectorStateList inUseList = mock(INorFlashSectorStateList.class, "inUse");
        final INorFlashSectorStateList toBeErasedList = mock(INorFlashSectorStateList.class, "toBeErased");
        final byte[] sampleBytes = new byte[] {0, 1, 2, 3};
        castHeap = new NorFlashMemoryHeap() {
            @Override
            protected boolean getBlockAt(IMemoryHeapBlock block, INorFlashSectorState sector, int offset) throws RecordStoreException {
                if (offset == 0) {
                    block.setIsAllocated(false);
                    block.setNextOffset(1);
                } else if (offset == 1) {
                    block.setIsAllocated(true);
                    block.setBytes(sampleBytes, 0, sampleBytes.length);
                    block.setNextOffset(2);
                } else {
                    return false;
                }
                return true;
            }
        };
        heap = castHeap;
        castHeap.sectorStates = sectors;
        castHeap.inUseSectorStateList = inUseList;
        castHeap.toBeErasedSectorStateList = toBeErasedList;
        
        expects(new InThisOrder() {{
            expects(new InAnyOrder() {{
                allowing(sector1).getSequence();will(returnValue(1L));
                allowing(sector2).getSequence();will(returnValue(2L));
                allowing(sector3).getSequence();will(returnValue(3L));
            }});
            one(sector1).hasErasedHeader();will(returnValue(false));
            one(toBeErasedList).addLast(sector1);
            one(sector2).hasErasedHeader();will(returnValue(true));
            one(sector2).resetHead();
            one(sector2).getWriteHeadPosition();will(returnValue(0));
            one(sector2).incrementFreedBlockCount();
            one(sector2).incrementAllocatedBlockCount();
            one(sector2).setWriteHeadPosition(2);
            one(sector2).getAllocatedBlockCount();will(returnValue(1));
            one(inUseList).addLast(sector2);
            one(sector3).hasErasedHeader();will(returnValue(true));
            one(sector3).resetHead();
            one(sector3).getWriteHeadPosition();will(returnValue(0));
            one(sector3).incrementFreedBlockCount();
            one(sector3).incrementAllocatedBlockCount();
            one(sector3).setWriteHeadPosition(2);
            one(sector3).getAllocatedBlockCount();will(returnValue(0));
            one(toBeErasedList).addLast(sector3);
        }});
        INorFlashMemoryHeapScanner scanner = new INorFlashMemoryHeapScanner() {
            public void reScanBlock(Address oldAddress, Address newAddress, IMemoryHeapBlock block) throws RecordStoreException {
                fail();
            }

            public void scanBlock(IMemoryHeapBlock block) throws RecordStoreException {
                for (int i=0; i < block.getLength(); i++) {
                    assertEquals(sampleBytes[i], block.getBytes()[block.getOffset() + i]);
                }
            }
        };

        castHeap.scanBlocks(scanner);
    }
    
    // TODO More testWriteBlock tests
    // - add a test that ensures that the oldAddress is freed
    // - test the odd even handling to make sure it writes into correct part of byte array
    /**
     * Case where oldAddress is zero, indicating we are inserting new data
     *
     */
    public void testWriteBlock1() throws RecordStoreException {
        final INorFlashSectorState sectorState = mock(INorFlashSectorState.class);
        final INorFlashMemoryHeapScanner scanner = mock(INorFlashMemoryHeapScanner.class);
        final byte[] bytes = new byte[0];
        final Address oldAddress = Address.zero();
        castHeap.currentSectorState = sectorState;
        castHeap.hasScannedBlocks = true;
        
        expects(new InThisOrder() {{
            one(sectorState).hasAvailable(8);will(returnValue(true));
            one(sectorState).getWriteHeadAddress();will(returnValue(Address.zero()));
            one(sectorState).writeBytes(with(an(byte[].class)), with(equal(0)), with(equal(6)));
            one(sectorState).writeBytes(bytes, 0, bytes.length);
            one(sectorState).writeBytes(with(an(byte[].class)), with(equal(0)), with(equal(2)));
            one(sectorState).incrementAllocatedBlockCount();
            one(sectorState).getStartAddress();will(returnValue(Address.zero()));
        }});
        
        MemoryHeapBlock block = new MemoryHeapBlock();
        block.setBytes(bytes, 0, 0);
        try {
            castHeap.writeBlock(block, scanner, oldAddress);
            assertSame(Address.zero(), block.getAddress());
        } catch (RecordStoreException e) {
            fail();
        }
    }
}
