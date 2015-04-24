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


import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import org.jmock.integration.junit3.MockObjectTestCase;

import test.rms.RecordStoreIntegrationTest;

import com.sun.squawk.Address;
import com.sun.squawk.peripheral.INorFlashSector;
import com.sun.squawk.peripheral.SimulatedNorFlashSectorAllocator;

public class NorFlashMemoryHeapWith1IntegrationTest extends MockObjectTestCase {
    protected INorFlashMemoryHeap heap;
    protected NorFlashMemoryHeap castHeap;

    public int getBlockSize() {
        return getSectorSize() / 60;
    }
    
    public int getNumSectors() {
        return 1;
    }
    
    public int getSectorSize() {
        return 65536;
    }
    
    @Override
    public void setUp() throws RecordStoreException {
        SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(getNumSectors(), getSectorSize(), INorFlashSector.RMS_PURPOSED, false);
        castHeap = new NorFlashMemoryHeap(INorFlashSector.RMS_PURPOSED);
        heap = castHeap;
    }

    @Override
    public void tearDown() {
        SimulatedNorFlashSectorAllocator.getSingleton().uninstallSectors();
        castHeap = null;
        heap = null;
    }
    
    /*
     * allocate block, and immediately free it, should be able to loop forrever like this
     */
    public void testMakeRoomToWrite1() throws RecordStoreException {
        byte[] bytes = new byte[getBlockSize()];
        // We would like to go forever, but we can't so lets go until we have erased
        // all sectors at least 3 times
        while (castHeap.erasedSequenceCurrentValue < (getNumSectors() * 3)) {
            Address address = heap.allocateAndWriteBlock(bytes, 0, bytes.length, null);
            heap.freeBlockAt(address);
        }
    }

    /*
     * allocate blocks until we have no more room left, then loop through and free them up, should
     * be able to loop forever like this
     */
    public void testMakeRoomToWrite2() throws IOException, RecordStoreException {
        final byte[] payload = new byte[getBlockSize()];
        int firstCountAdd = -1;
        Vector<Address> addresses = new Vector<Address>();
        for (int i=0; i < 4; i++) {
            int countAdd = 0;
            try {
                while (true) {
                    Address address = heap.allocateAndWriteBlock(payload, 0, payload.length, null);
                    addresses.add(address);
                    countAdd++;
                }
            } catch (RecordStoreFullException e) {
            }
            assertTrue("On i=" + i + " countAdd==0", countAdd > 0);
            if (firstCountAdd == -1) {
                firstCountAdd = countAdd;
            } else {
                assertEquals("On i=" + i + " " + firstCountAdd + "!=" + countAdd, firstCountAdd, countAdd);
            }
            int countDelete = 0;
            for (Address address : addresses) {
                castHeap.freeBlockAt(address);
                countDelete++;
            }
            addresses.removeAllElements();
            assertEquals("On i=" + i + " " + countDelete + "!=" + countAdd, countDelete, countAdd);
        }
    }

    /*
     * Add some blocks that are never deleted, then go through and add and delete blocks, we
     * should still be able to do this forever, except in the 1 sector case
     */
    public void testMakeRoomToWrite3() throws RecordStoreException {
        final byte[] payload = new byte[getBlockSize()];
        int firstCountAdd = -1;
        final Vector<Address> addresses = new Vector<Address>();
        // Add a couple of blocks that will never be invalidated
        RecordStoreIntegrationTest.fill(payload, 1);
        final Address[] keeper1AddressHandle = new Address[] {heap.allocateAndWriteBlock(payload, 0, payload.length, null)};
        RecordStoreIntegrationTest.fill(payload, 2);
        final Address[] keeper2AddressHandle = new Address[] {heap.allocateAndWriteBlock(payload, 0, payload.length, null)};
        INorFlashMemoryHeapScanner scanner = new INorFlashMemoryHeapScanner() {
            public void reScanBlock(Address oldAddress, Address newAddress, IMemoryHeapBlock block) throws RecordStoreException {
                if (oldAddress.eq(keeper1AddressHandle[0])) {
                    keeper1AddressHandle[0] = newAddress;
                    return;
                }
                if (oldAddress.eq(keeper2AddressHandle[0])) {
                    keeper2AddressHandle[0] = newAddress;
                    return;
                }
                int index = -1;
                int i=0;
                for (Address address : addresses) {
                    if (address.eq(oldAddress)) {
                        index = i;
                    }
                    i++;
                }
                assertTrue(index >= 0);
                addresses.set(index, newAddress);
            }
            public void scanBlock(IMemoryHeapBlock block) throws RecordStoreException {
                fail();
            }
        };
        for (int i=0; i < 4; i++) {
            payload[i] = 0;
        }
        for (int i=4; i < payload.length; i++) {
            payload[i] = (byte) i;
        }
        for (int i=0; i < 4; i++) {
            int countAdd = 0;
            addresses.removeAllElements();
            try {
                while (true) {
                    payload[0] = (byte) i;
                    Address address = heap.allocateAndWriteBlock(payload, 0, payload.length, scanner);
                    addresses.add(address);
                    countAdd++;
                }
            } catch (RecordStoreFullException e) {
            }
            if (i > 0 && getNumSectors() ==1) {
                return;
            }
            assertTrue("On i=" + i + " countAdd==0", countAdd > 0);
            if (firstCountAdd == -1) {
                firstCountAdd = countAdd;
            } else {
                assertEquals("On i=" + i , firstCountAdd, countAdd);
            }
            int countDelete = 0;
            for (Address address : addresses) {
                heap.freeBlockAt(address);
                countDelete++;
            }
            addresses.removeAllElements();
            assertEquals("On i=" + i, countDelete, countAdd);
        }
        IMemoryHeapBlock block = heap.getBlockAt(keeper1AddressHandle[0]);
        assertNotNull(block);
        assertTrue(block.isAllocated());
        RecordStoreIntegrationTest.assertEquals(block.getBytes(), block.getLength(), 1);
        block = heap.getBlockAt(keeper2AddressHandle[0]);
        assertNotNull(block);
        assertTrue(block.isAllocated());
        RecordStoreIntegrationTest.assertEquals(block.getBytes(), block.getLength(), 2);
    }

    public void testMakeRoomToWrite4() throws RecordStoreException {
        final byte[] payload = new byte[getBlockSize()];
        int firstCountAdd = -1;
        final Vector<Address> addresses = new Vector<Address>();
        // Add a couple of blocks that will never be invalidated
        RecordStoreIntegrationTest.fill(payload, 1);
        final Address[] keeper1AddressHandle = new Address[] {heap.allocateAndWriteBlock(payload, 0, payload.length, null)};
        RecordStoreIntegrationTest.fill(payload, 2);
        final Address[] keeper2AddressHandle = new Address[] {heap.allocateAndWriteBlock(payload, 0, payload.length, null)};
        INorFlashMemoryHeapScanner scanner = new INorFlashMemoryHeapScanner() {
            public void reScanBlock(Address oldAddress, Address newAddress, IMemoryHeapBlock block) throws RecordStoreException {
                if (oldAddress.eq(keeper1AddressHandle[0])) {
                    keeper1AddressHandle[0] = newAddress;
                    return;
                }
                if (oldAddress.eq(keeper2AddressHandle[0])) {
                    keeper2AddressHandle[0] = newAddress;
                    return;
                }
                int index = -1;
                int i=0;
                for (Address address : addresses) {
                    if (address.eq(oldAddress)) {
                        index = i;
                    }
                    i++;
                }
                assertTrue(index >= 0);
                addresses.set(index, newAddress);
            }
            public void scanBlock(IMemoryHeapBlock block) throws RecordStoreException {
                fail();
            }
        };
        for (int i=0; i < 4; i++) {
            payload[i] = 0;
        }
        for (int i=4; i < payload.length; i++) {
            payload[i] = (byte) i;
        }
        int countDelete = 0;
        for (int i=0; i < 4; i++) {
            int countAdd = 0;
            try {
                while (true) {
                    payload[0] = (byte) i;
                    Address address = heap.allocateAndWriteBlock(payload, 0, payload.length, scanner);
                    addresses.add(address);
                    countAdd++;
                }
            } catch (RecordStoreFullException e) {
            }
            if (i > 0 && getNumSectors() ==1) {
                return;
            }
            assertTrue("On i=" + i + " countAdd==0", countAdd > 0);
            if (firstCountAdd == -1) {
                firstCountAdd = countAdd;
            } else {
                assertTrue("On i=" + i , (countAdd + countDelete) >= (firstCountAdd * 90 / 100));
            }
            countDelete = 0;
            Vector<Address> addressesCopy = new Vector<Address>();
            addressesCopy.addAll(addresses);
            for (int j=0, max=addressesCopy.size(); j < max; j += 2) {
                Address address = addressesCopy.elementAt(j);
                addresses.removeElement(address);
                heap.freeBlockAt(address);
                countDelete++;
            }
        }
        IMemoryHeapBlock block = heap.getBlockAt(keeper1AddressHandle[0]);
        assertNotNull(block);
        assertTrue(block.isAllocated());
        RecordStoreIntegrationTest.assertEquals(block.getBytes(), block.getLength(), 1);
        block = heap.getBlockAt(keeper2AddressHandle[0]);
        assertNotNull(block);
        assertTrue(block.isAllocated());
        RecordStoreIntegrationTest.assertEquals(block.getBytes(), block.getLength(), 2);
    }
    
}
