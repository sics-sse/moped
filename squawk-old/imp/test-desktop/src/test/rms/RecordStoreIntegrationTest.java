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

package test.rms;

import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import com.sun.squawk.VM;
import com.sun.squawk.imp.ImpGlobal;
import com.sun.squawk.peripheral.INorFlashSector;
import com.sun.squawk.peripheral.INorFlashSectorAllocator;
import com.sun.squawk.peripheral.SimulatedNorFlashSectorAllocator;
import com.sun.squawk.rms.ApplicationDescriptorEntry;

import junit.framework.TestCase;

/**
 * Series of tests to test the RecordStore API.
 * 
 *
 */
public class RecordStoreIntegrationTest extends TestCase {
    public static final int numSectors = 16;

    public int erasedSequenceCount;

    public static void assertEquals(byte[] bytes, int length, int startingValue) {
        for (int i=0; i < length; i++) {
            assertEquals(bytes[i], (byte) startingValue++);
        }
    }
    
    public static void fill(byte[] bytes, int startingValue) {
        for (int i=0, max=bytes.length; i < max; i++) {
            bytes[i] = (byte) startingValue++;
        }
    }
    
    public static void main(String[] args) {
        JUnitCore.main(RecordStoreIntegrationTest.class.getName());
    }
    
    public static void setUpIntegrationTests(boolean setup, boolean useFiles) {
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, "name");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, "vendor");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, "version");
        final int sectorSize = 65536;
        if (setup) {
            SimulatedNorFlashSectorAllocator.getSingleton().setupSectors(numSectors, sectorSize, INorFlashSector.RMS_PURPOSED, useFiles);
        } else {
            SimulatedNorFlashSectorAllocator.getSingleton().installSectors(numSectors, sectorSize, INorFlashSector.RMS_PURPOSED);
        }
        ImpGlobal.resetRecordStoreManager();
    }

    public static void tearDownIntegrationTests() {
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, null);
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, null);
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, null);
        SimulatedNorFlashSectorAllocator.resetSingleton();
        ImpGlobal.resetRecordStoreManager();
    }

    @Before
    public void setUp() throws IOException {
        setUpIntegrationTests(true, false);
        // TODO - not quite right, it should be user purposed ones only
        erasedSequenceCount = numSectors * 3;
    }
    
    @After
    public void tearDown() {
        tearDownIntegrationTests();
    }

    /*
     * Continously add then immediately delete record, repeat many times
     */
    @Test
    public void testAddRecordDeleteRecord1() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        String storeName = "testAddRecordDeleteRecord1";
        RecordStore store = RecordStore.openRecordStore(storeName, true);
        byte[] record = new byte[1024];
        int count = 0;
        long startingSequence = ImpGlobal.getRecordStoreManagerErasedSequenceCurrentValue();
        while ((ImpGlobal.getRecordStoreManagerErasedSequenceCurrentValue() - startingSequence) < erasedSequenceCount) {
            fill(record, count);
            int recordId = store.addRecord(record, 0, record.length);
            fill(record, (byte) (count+1));
            byte[] bytes = store.getRecord(recordId);
            assertEquals(record, record.length, (byte) (count + 1));
            assertEquals(bytes, bytes.length, (byte) count);
            store.deleteRecord(recordId);
            try {
                store.getRecord(recordId);
                fail();
            } catch (InvalidRecordIDException e) {
            }
            count++;
        }
        store.closeRecordStore();
        RecordStore.deleteRecordStore(storeName);
    }

    /*
     * Add records until we run out of room, then delete them all, repeat whole process
     * multiple times
     */
    @Test
    public void testAddRecordDeleteRecord2() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        String storeName = "testAddRecordDeleteRecord2";
        RecordStore store = RecordStore.openRecordStore(storeName, true);
        byte[] record = new byte[1024];
        int firstCountAdd = -1;
        for (int i=0; i < 4; i++) {
            int countAdd = 0;
            try {
                while (true) {
                    fill(record, countAdd);
                    store.addRecord(record, 0, record.length);
                    countAdd++;
                }
            } catch (RecordStoreFullException e) {
            }
            assertTrue(countAdd > 0);
            if (firstCountAdd == -1) {
                firstCountAdd = countAdd;
            } else {
                assertEquals(firstCountAdd, countAdd);
            }
            RecordEnumeration records = store.enumerateRecords(null, null, false);
            int countDelete = 0;
            while (records.hasNextElement()) {
                int recordId = records.nextRecordId();
                byte[] bytes = store.getRecord(recordId);
                assertEquals(bytes, bytes.length, countDelete);
                store.deleteRecord(recordId);
                countDelete++;
            }
            assertEquals(countDelete, countAdd);
        }
        store.closeRecordStore();
        RecordStore.deleteRecordStore(storeName);
    }

    /*
     * Add records until we run out of room, then delete every second one, then add
     * until we are out of room again, repeat whole process multiple times
     */
    @Test
    public void testAddRecordDeleteRecord3() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        String storeName = "testAddRecordDeleteRecord3";
        RecordStore store = RecordStore.openRecordStore(storeName, true);
        byte[] record = new byte[1024];
        int firstCountAdd = -1;
        int countDelete = 0;
        for (int i=0; i < 4; i++) {
            int countAdd = 0;
            try {
                while (true) {
                    int recordId = store.getNextRecordID();
                    fill(record, recordId);
                    store.addRecord(record, 0, record.length);
                    countAdd++;
                }
            } catch (RecordStoreFullException e) {
            }
            assertTrue(countAdd > 0);
            if (firstCountAdd == -1) {
                firstCountAdd = countAdd;
            } else {
                assertEquals(countDelete, countAdd);
            }
            RecordEnumeration records = store.enumerateRecords(null, null, false);
            countDelete = 0;
            boolean delete = false;
            while (records.hasNextElement()) {
                int recordId = records.nextRecordId();
                if (delete) {
                    store.deleteRecord(recordId);
                    countDelete++;
                } else {
                    byte[] bytes = store.getRecord(recordId);
                    assertEquals(bytes, bytes.length, recordId);
                }
                delete = !delete;
            }
            assertTrue(countDelete > 0);
        }
        store.closeRecordStore();
        RecordStore.deleteRecordStore(storeName);
    }

    /*
     * Add records until we run out of room, then delete the store, repeat whole process
     * multiple times
     */
    @Test
    public void testAddRecordDeleteStore1() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        byte[] record = new byte[1024];
        int firstCountAdd = -1;
        for (int i=0; i < 4; i++) {
            String storeName = "testAddRecordDeleteStore1";
            RecordStore store = RecordStore.openRecordStore(storeName, true);
            int countAdd = 0;
            try {
                while (true) {
                    fill(record, countAdd);
                    store.addRecord(record, 0, record.length);
                    countAdd++;
                }
            } catch (RecordStoreFullException e) {
            }
            assertTrue(countAdd > 0);
            if (firstCountAdd == -1) {
                firstCountAdd = countAdd * 90 / 100;
            } else {
                assertTrue(countAdd >= firstCountAdd);
            }
            store.closeRecordStore();
            RecordStore.deleteRecordStore(storeName);
        }
    }

    /*
     * Continously add then immediately delete record, repeat many times
     */
    @Test
    public void testAddStoreDeleteStore1() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        int count = 0;
        long startingSequence = ImpGlobal.getRecordStoreManagerErasedSequenceCurrentValue();
        while ((ImpGlobal.getRecordStoreManagerErasedSequenceCurrentValue() - startingSequence) < erasedSequenceCount) {
            String storeName = "testAddStoreDeleteStore1_" + count;
            RecordStore store = RecordStore.openRecordStore(storeName, true);
            store.closeRecordStore();
            RecordStore.deleteRecordStore(storeName);
            count++;
        }
        assertTrue(count > 0);
    }

    /*
     * Done to simulate what actually happens in RecordStoreSystemTestMidlet, running each test
     * one after the other.
     */
    public void testsAllSequential() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        testAddRecordDeleteRecord1();
        testAddRecordDeleteRecord2();
        testAddRecordDeleteRecord3();
        testAddRecordDeleteStore1();
        testAddStoreDeleteStore1();
        testRestartState();
        testSetRecord1();
        testSetRecord2();
    }
    
    /*
     * Add a number of stores and records, then reset the record store manager, in
     * order to cause it to re-scan the heap and re-create its information as if device
     * was turned off and back on
     */
    public void testRestartState() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        final int numStores = 10;
        final byte[] record = new byte[16];
        String storeNamePrefix = "testRestartState_";
        for (int i=0; i < numStores; i++) {
            // Turn device off, then back on
            ImpGlobal.resetRecordStoreManager();
            // Create a new store, add some records to it
            RecordStore store = RecordStore.openRecordStore(storeNamePrefix + i, true);
            for (int k=0; k < i; k++) {
                int recordId = store.getNextRecordID();
                fill(record, recordId);
                int actualRecordId = store.addRecord(record, 0, record.length);
                assertEquals(recordId, actualRecordId);
            }
            store.closeRecordStore();
            String[] storeNames = RecordStore.listRecordStores();
            assertEquals(i + 1, storeNames.length);
            // Go through all the other stores we have added and see if they have
            // the records we added
            for (int j=0; j < i; j++) {
                RecordStore readStore = RecordStore.openRecordStore(storeNamePrefix + j, false);
                RecordEnumeration enumeration = readStore.enumerateRecords(null, null, false);
                for (int k=0; k < j; k++) {
                    int recordId = enumeration.nextRecordId();
                    byte[] bytes = readStore.getRecord(recordId);
                    assertEquals(bytes, bytes.length, recordId);
                }
                assertFalse(enumeration.hasNextElement());
            }
        }
        for (int i=1; i < numStores; i++) {
            ImpGlobal.resetRecordStoreManager();
            RecordStore store = RecordStore.openRecordStore(storeNamePrefix + i, false);
            store.deleteRecord(1);
            store.closeRecordStore();
        }
        for (int i=0; i < numStores; i++) {
            ImpGlobal.resetRecordStoreManager();
            String name = storeNamePrefix + i;
            RecordStore store = RecordStore.openRecordStore(name, false);
            RecordEnumeration enumeration = store.enumerateRecords(null, null, false);
            for (int k=0; k < (i - 1); k++) {
                int recordId = enumeration.nextRecordId();
                assertNotSame(recordId, 1);
                byte[] bytes = store.getRecord(recordId);
                assertEquals(bytes, bytes.length, recordId);
            }
            assertFalse(enumeration.hasNextElement());
            store.closeRecordStore();
            RecordStore.deleteRecordStore(name);
        }
        ImpGlobal.resetRecordStoreManager();
        assertNull(RecordStore.listRecordStores());
    }
    
    /*
     * setRecord on same record for a long time
     */
    @Test
    public void testSetRecord1() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        String storeName = "testSetRecord1";
        RecordStore store = RecordStore.openRecordStore(storeName, true);
        byte[] record = new byte[1024];
        int count = 0;
        int recordId = store.addRecord(record, 0, record.length);
        long startingSequence = ImpGlobal.getRecordStoreManagerErasedSequenceCurrentValue();
        while ((ImpGlobal.getRecordStoreManagerErasedSequenceCurrentValue() - startingSequence) < erasedSequenceCount) {
            fill(record, count);
            store.setRecord(recordId, record, 0, record.length);
            byte[] bytes = store.getRecord(recordId);
            assertEquals(bytes, bytes.length, count);
            count++;
        }
        assertTrue(count > 0);
        store.closeRecordStore();
        RecordStore.deleteRecordStore(storeName);
    }

    /*
     * Keep adding records until we run out of room, then try a setRecord on
     * one I added, make sure record is safe when out of room exception occurs
     * on set
     */
    @Test
    public void testSetRecord2() throws RecordStoreException {
        assertNull(RecordStore.listRecordStores());
        String storeName = "testSetRecord2";
        RecordStore store = RecordStore.openRecordStore(storeName, true);
        byte[] record = new byte[1024];
        fill(record, 0);
        int recordId = store.addRecord(record, 0, record.length);
        int i = 1;
        try {
            while (true) {
                fill(record, i++);
                store.addRecord(record, 0, record.length);
            }
        } catch (RecordStoreFullException e) {
        }
        fill(record, 1);
        try {
            store.setRecord(recordId, record, 0, record.length);
            fail();
        } catch (RecordStoreFullException e) {
        }
        byte[] bytes = store.getRecord(recordId);
        assertEquals(bytes, bytes.length, 0);
        store.closeRecordStore();
        RecordStore.deleteRecordStore(storeName);
    }

}
