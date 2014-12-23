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

package com.sun.squawk.rms;

import java.io.IOException;
import java.util.Arrays;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import org.jmock.InThisOrder;

import com.sun.squawk.Address;

public class RecordStoreEntryTest extends RmsEntryTest {
    public static final int NUM_RECORDS = 5;
    public static final int BYTES_SIZE = 100;
    
    protected IRecordStoreManager manager;
    protected RecordStoreEntry castStore;
    protected IRecordStoreEntry store;
    
    public void setUp() throws Exception {
        super.setUp();
        manager = mock(IRecordStoreManager.class);
        castStore = new RecordStoreEntry(manager);
        store = castStore;
    }
    
    public void tearDown() throws Exception {
        super.tearDown();
        store = null;
    }

    public void doCompareEntries(IRmsEntry entryWritten, IRmsEntry entryRead) {
        IRecordStoreEntry castEntry = (IRecordStoreEntry) entryWritten;
        IRecordStoreEntry castReadEntry = (IRecordStoreEntry) entryRead;

        assertEquals(castEntry.getId(), castReadEntry.getId());
        assertEquals(castEntry.getName(), castReadEntry.getName());
        assertEquals(castEntry.getNextRecordId(), castReadEntry.getNextRecordId());
    }

    /*
     * null bytes
     */
    public void testAddRecord1() throws Exception {
        final Address address = Address.fromPrimitive(10);
        expects(new InThisOrder() {{
            one(manager).logEntry(with(an(IRecordEntry.class)));will(returnValue(address));
        }});
        
        int expectedRecordId = store.getNextRecordId();
        int recordId = store.addRecord(null, 0, 0);
        assertTrue(recordId >= 0);
        assertEquals(expectedRecordId, recordId);
        assertEquals(recordId, castStore.recordIds[0]);
        assertSame(address, castStore.recordAddresses[0]);
        assertEquals(1, store.getNumRecords());
    }
    
    /*
     * some bytes
     */
    public void testAddRecord2() throws Exception {
        final Address address = Address.fromPrimitive(10);
        final byte[] bytes = new byte[10];
        expects(new InThisOrder() {{
            one(manager).logEntry(with(an(IRecordEntry.class)));will(returnValue(address));
        }});
        
        int expectedRecordId = store.getNextRecordId();
        int recordId = store.addRecord(bytes, 0, bytes.length);
        assertTrue(recordId >= 0);
        assertEquals(expectedRecordId, recordId);
        assertEquals(recordId, castStore.recordIds[0]);
        assertSame(address, castStore.recordAddresses[0]);
        assertEquals(1, store.getNumRecords());
    }
    
    /*
     * some bytes, with an exception on logEntry
     */
    public void testAddRecord3() throws Exception {
        final byte[] bytes = new byte[10];
        expects(new InThisOrder() {{
            one(manager).logEntry(with(an(IRecordEntry.class)));will(throwException(new RecordStoreFullException()));
        }});
        
        int expectedRecordId = store.getNextRecordId();
        try {
            store.addRecord(bytes, 0, bytes.length);
            fail();
            return;
        } catch (RecordStoreFullException e) {
        }
        assertEquals(expectedRecordId, store.getNextRecordId());
        assertEquals(0, store.getNumRecords());
        assertEquals(0, store.getVersion());
        assertEquals(0, castStore.recordIds[0]);
        assertSame(null, castStore.recordAddresses[0]);
    }
    
    /*
     * null bytes
     */
    public void testAddRecord4() throws Exception {
        final Address address = Address.fromPrimitive(10);
        expects(new InThisOrder() {{
            one(manager).logEntry(with(an(IRecordEntry.class)));will(returnValue(address));
        }});
        
        int expectedRecordId = store.getNextRecordId();
        
        int recordId = store.addRecord(null, 0, 0);
        
        assertTrue(recordId >= 0);
        assertEquals(expectedRecordId, recordId);
        assertEquals(recordId, castStore.recordIds[0]);
        assertSame(address, castStore.recordAddresses[0]);
        assertEquals(1, store.getNumRecords());
    }
    
    /*
     * Delete non-existent record id
     */
    public void testDeleteRecord1() throws RecordStoreException {
        try {
            store.deleteRecord(0);
            fail();
        } catch (InvalidRecordIDException e) {
        }
    }

    /*
     * Delete existing record id
     */
    public void testDeleteRecord2() throws Exception {
        castStore.numRecords = 2;
        int idToDelete = 1;
        castStore.recordIds[0] = idToDelete;
        castStore.recordIds[1] = 2;
        final Address address1 = castStore.recordAddresses[0] = Address.fromPrimitive(10);
        Address address2 = castStore.recordAddresses[1] = Address.fromPrimitive(20);
        
        expects(new InThisOrder() {{
            one(manager).getEntryAt(address1);will(returnValue(mock(IRecordEntry.class)));
            one(manager).invalidateEntryAt(address1);
        }});
        
        store.deleteRecord(idToDelete);
        
        assertEquals(1, store.getNumRecords());
        assertEquals(2, castStore.recordIds[0]);
        assertEquals(0, castStore.recordIds[1]);
        assertEquals(address2, castStore.recordAddresses[0]);
        assertEquals(Address.zero(), castStore.recordAddresses[1]);
    }

    public void testFindRecordIndex(int[] ids) {
        castStore.recordIds = ids;
        castStore.numRecords = ids.length;
        for (int i=0; i < ids.length; i++) {
            assertTrue(castStore.findRecordIndex(ids[i]) == i);
            assertTrue(castStore.findRecordIndex(ids[i] - 1) == (-i - 1));
            assertTrue(castStore.findRecordIndex(ids[i] + 1) == (-(i + 1) - 1));
        }
        assertTrue(castStore.findRecordIndex(0) == -1);
        assertTrue(castStore.findRecordIndex(ids[ids.length - 1] + 10) == -ids.length - 1);
    }
    
    public void testFindRecordIndex() {
        int[] ids = new int[] {1, 3, 6};
        testFindRecordIndex(ids);

        ids = new int[] {1, 3, 6, 9};
        testFindRecordIndex(ids);
    }

    /*
     * Find non-existen record id
     */
    public void testFindRecord() throws RecordStoreException, IOException {
        try {
            castStore.findRecord(1);
            fail();
        } catch (InvalidRecordIDException e) {
        }
    }
    
    /*
     * Find existing record id
     */
    public void testFindRecord1() throws RecordStoreException, IOException {
        final int recordId = 1;
        final Address address = Address.fromPrimitive(10);
        final IRecordEntry record = mock(IRecordEntry.class);
        castStore.recordIds = new int[] {recordId};
        castStore.recordAddresses = new Address[] {address};
        castStore.numRecords = castStore.recordIds.length;
        
        expects(new InThisOrder() {{
            one(manager).getEntryAt(address);will(returnValue(record));
        }});
        IRecordEntry foundRecord = castStore.findRecord(recordId);
        assertSame(record, foundRecord);
    }
    

    /*
     * find non-existent record id with actual records in place
     */
    public void testFindRecord2() throws RecordStoreException, IOException {
        final int recordId = 1;
        final Address address = Address.fromPrimitive(10);
        castStore.recordIds = new int[] {recordId};
        castStore.recordAddresses = new Address[] {address};
        castStore.numRecords = castStore.recordIds.length;
        
        try {
            castStore.findRecord(recordId + 1);
            fail();
        } catch (RecordStoreException e) {
        }
    }
    
    public void testGetRecord() throws RecordStoreException, IOException {
        final int recordId = 1;
        final Address address = Address.fromPrimitive(10);
        final IRecordEntry record = mock(IRecordEntry.class);
        final byte[] bytes = new byte[0];
        castStore.recordIds = new int[] {recordId};
        castStore.recordAddresses = new Address[] {address};
        castStore.numRecords = castStore.recordIds.length;
        
        expects(new InThisOrder() {{
            one(manager).getEntryAt(address);will(returnValue(record));
            one(record).getBytes();will(returnValue(bytes));
        }});
        
        byte[] recordBytes = store.getRecord(recordId);
        assertSame(bytes, recordBytes);
    }
    
    public void testGetRecordBuffered() throws RecordStoreException, IOException {
        final int recordId = 1;
        final Address address = Address.fromPrimitive(10);
        castStore.recordIds = new int[] {recordId};
        castStore.recordAddresses = new Address[] {address};
        castStore.numRecords = castStore.recordIds.length;
        
        final IRecordEntry record = mock(IRecordEntry.class);
        final byte[] bytes = new byte[0];
        final int bytesOffset = 5;
        expects(new InThisOrder() {{
            one(manager).getEntryAt(address);will(returnValue(record));
            one(record).getBytesLength();will(returnValue(0));
            one(record).getBytes(bytes, bytesOffset, 0);
        }});
        store.getRecord(recordId, bytes, bytesOffset);
    }
    
    public void testGetRecordIdsCopy() {
        castStore.recordIds = new int[] {1, 4, 5};
        castStore.numRecords = castStore.recordIds.length;
        int[] copy = store.getRecordIdsCopy();
        assertNotSame(castStore.recordIds, copy);
        assertTrue(Arrays.equals(castStore.recordIds, copy));
    }
    
    public void testGetRecordSize() throws RecordStoreException, IOException {
        final int recordId = 1;
        final Address address = Address.fromPrimitive(10);
        castStore.recordIds = new int[] {recordId};
        castStore.recordAddresses = new Address[] {address};
        castStore.numRecords = castStore.recordIds.length;
        
        final IRecordEntry record = mock(IRecordEntry.class);
        final int bytesSize = 10;
        expects(new InThisOrder() {{
            one(manager).getEntryAt(address);will(returnValue(record));
            one(record).getBytesLength();will(returnValue(bytesSize));
        }});
        assertSame(bytesSize, store.getRecordSize(recordId));
    }
    
    public void testInsertAtIndex(int[] ids, int index, int numRecords) {
        castStore.recordIds = new int[ids.length];
        System.arraycopy(ids, 0, castStore.recordIds, 0, ids.length);
        Address[] addresses = new Address[ids.length];
        for (int i=0; i < addresses.length; i++) {
            if (ids[i] != 0) {
                addresses[i] = Address.fromPrimitive(ids[i] * 10);
            }
        }
        castStore.recordAddresses = new Address[addresses.length];
        System.arraycopy(addresses, 0, castStore.recordAddresses, 0, addresses.length);
        castStore.numRecords = numRecords;
        
        castStore.insertAtIndex(index);
        assertSame(numRecords + 1, store.getNumRecords());
        assertSame(0, castStore.recordIds[index]);
        assertSame(Address.zero(), castStore.recordAddresses[index]);
        for (int i=0; i < index; i++) {
            assertSame("i: " + i, ids[i], castStore.recordIds[i]);
            assertSame("i: " + i, addresses[i], castStore.recordAddresses[i]);
        }
        for (int i=index + 1; i < castStore.numRecords; i++) {
            assertSame("i: " + i, ids[i-1], castStore.recordIds[i]);
            assertSame("i: " + i, addresses[i-1], castStore.recordAddresses[i]);
        }
    }

    public void testInsertAtIndex() {
        int[] ids = new int[] {1, 3, 5};
        int numRecords = ids.length;
        testInsertAtIndex(ids, 0, numRecords);
        testInsertAtIndex(ids, 1, numRecords);
        testInsertAtIndex(ids, 3, numRecords);
    }
    
    public void testInsertAtIndex1() {
        int[] ids = new int[] {1, 3, 5, 0, 0};
        int numRecords = 3;
        testInsertAtIndex(ids, 0, numRecords);
        testInsertAtIndex(ids, 1, numRecords);
        testInsertAtIndex(ids, 3, numRecords);
    }
    
    public void testEntry() throws RecordStoreException, IOException {
        doTestEntry(new RecordStoreEntry(manager), new RecordStoreEntry(manager));
    }
    
    public void testSetRecord() throws RecordStoreException {
        try {
            store.setRecord(0, null, 0, 0);
            fail();
        } catch (InvalidRecordIDException e) {
        }
    }

    public void testSetRecord1() throws RecordStoreException, IOException {
        final int recordId = 1;
        castStore.numRecords = 1;
        castStore.recordIds[0] = recordId;
        final Address originalAddress = Address.fromPrimitive(10);
        final Address newAddress = Address.fromPrimitive(20);
        castStore.recordAddresses[0] = originalAddress;

        expects(new InThisOrder() {{
            one(manager).logEntry(with(an(IRecordEntry.class)));will(returnValue(newAddress));
            one(manager).invalidateEntryAt(originalAddress);
        }});
        
        store.setRecord(recordId, null, 0, 0);
        assertEquals(1, castStore.numRecords);
        assertEquals(1, castStore.recordIds[0]);
        assertEquals(newAddress, castStore.recordAddresses[0]);
    }
    
    public void testVisit() throws RecordStoreException {
        final RecordStoreEntry entry = new RecordStoreEntry(manager);
        
        expects(new InThisOrder() {{
            one(visitor).visitRecordStore(entry);
        }});
        
        entry.visit(visitor);
    }

    /*
     * First time record with this id encountered
     */
    public void testVisitRecordEntry() throws RecordStoreException {
        final IRecordEntry record = mock(IRecordEntry.class);
        final int recordId = 1;
        final Address address = Address.fromPrimitive(10);
        
        expects(new InThisOrder() {{
            one(record).getId();will(returnValue(recordId));
            one(record).getAddress();will(returnValue(address));
        }});
        store.visitRecordEntry(record);
        assertSame(1, store.getNumRecords());
        assertSame(recordId, castStore.recordIds[0]);
        assertSame(address, castStore.recordAddresses[0]);
    }
    
    /*
     * Second time record with this id encountered
     */
    public void testVisitRecordEntryExists() throws RecordStoreException, IOException {
        final IRecordEntry record = mock(IRecordEntry.class);
        final int recordId = 1;
        final Address existingAddress = Address.fromPrimitive(10);
        final Address newAddress = Address.fromPrimitive(11);
        castStore.recordIds[0] = recordId;
        castStore.recordAddresses[0] = existingAddress;
        castStore.numRecords = 1;
        
        expects(new InThisOrder() {{
            one(record).getId();will(returnValue(recordId));
            one(manager).invalidateEntryAt(existingAddress);
            one(record).getAddress();will(returnValue(newAddress));
        }});
        store.visitRecordEntry(record);
        assertSame(1, store.getNumRecords());
        assertSame(recordId, castStore.recordIds[0]);
        assertSame(newAddress, castStore.recordAddresses[0]);
    }
    
}
