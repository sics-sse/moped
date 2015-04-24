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

package com.sun.squawk.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.flash.IMemoryHeapBlock;
import com.sun.squawk.util.DataOutputUTF8Encoder;

/**
 * A RecordStoreEntry is an entry that describes a RecordStore, which contains zero or more RecordEntry objects.
 */
public class RecordStoreEntry extends RmsEntry implements IRecordStoreEntry {
    
    public static final int TYPE = 3;
    protected static final int INSERT_PADDING = 10;

    protected IRecordStoreManager manager;
    protected int id;
    protected int[] recordIds;
    protected Address[] recordAddresses;
    protected String name;
    protected int numRecords;
    protected int lastRecordId;
    protected long lastModifiedTimestamp;
    protected int version;

    public RecordStoreEntry(IRecordStoreManager manager) {
        this.manager = manager;
        // It is important that 0 not be used, as 0 is the default value when I
        // create a new array to handle growing upon adding of records
        lastRecordId = 0;
        recordIds = new int[INSERT_PADDING];
        recordAddresses = new Address[recordIds.length];
        name = "";
        address = Address.zero();
    }

    public void absorbTempEntry(IRecordStoreEntry tempEntry) {
        RecordStoreEntry castTempEntry = (RecordStoreEntry) tempEntry;
        this.recordAddresses = castTempEntry.recordAddresses;
        this.recordIds = castTempEntry.recordIds;
        this.lastRecordId = castTempEntry.lastRecordId;
        this.lastModifiedTimestamp = castTempEntry.lastModifiedTimestamp;
        this.numRecords = castTempEntry.numRecords;
    }
    
    public int addRecord(byte[] bytes, int offset, int length) throws RecordStoreException, RecordStoreFullException {
        // Add the record first in order to ensure we dont get a RecordStoreFullException in process
        IRecordEntry record = new RecordEntry();
        record.setBytes(bytes, offset, length);
        record.setStoreId(id);
        record.setId(lastRecordId + 1);
        Address address = manager.logEntry(record);
        // At this point we know we have successfully added the record to the log
        int index = numRecords;
        insertAtIndex(index);
        lastRecordId++;
        recordIds[index] = lastRecordId;
        recordAddresses[index] = address;
        changed();
        return lastRecordId;
    }
    
    protected void changed() {
        lastModifiedTimestamp = System.currentTimeMillis();
        version++;
    }
    
    public void deleteRecord(int recordId) throws InvalidRecordIDException, RecordStoreException {
        int index = findRecordIndex(recordId);
        findRecord(recordId);
        manager.invalidateEntryAt(recordAddresses[index]);
        System.arraycopy(recordIds, index + 1, recordIds, index, recordIds.length - index -1);
        recordIds[numRecords - 1] = 0;
        System.arraycopy(recordAddresses, index + 1, recordAddresses, index, recordAddresses.length - index -1);
        recordAddresses[numRecords - 1] = Address.zero();
        numRecords--;
        changed();
    }
    
    public void deleteRecords() throws RecordStoreException {
        for (int i=0, max=numRecords; i < max; i++) {
            if (recordAddresses[i].ne(Address.zero())) {
                manager.invalidateEntryAt(recordAddresses[i]);
                recordAddresses[i] = Address.zero();
                recordIds[i] = 0;
            }
        }
    }
    
    protected Address findRecordAddress(int recordId) throws InvalidRecordIDException, RecordStoreException {
        int index = findRecordIndex(recordId);
        if (index < 0) {
            throw new InvalidRecordIDException("No record with id: " + recordId);
        }
        return recordAddresses[index];
    }
    
    /**
    * @return index of the search key, if it is contained in the list;
    *         otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
    *         <i>insertion point</i> is defined as the point at which the
    *         key would be inserted into the list: the index of the first
    *         element greater than the key, or <tt>list.size()</tt>, if all
    *         elements in the list are less than the specified key.  Note
    *         that this guarantees that the return value will be &gt;= 0 if
    *         and only if the key is found.
    */
    protected int findRecordIndex(int recordId) {
        int low = 0;
        int high = numRecords - 1;
        while (low <= high) {
            int mid = (low + high) >> 1;
            long midVal = recordIds[mid];
            if (midVal < recordId) {
                low = mid + 1;
            } else if (midVal > recordId) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }
    
    protected IRecordEntry findRecord(int recordId) throws InvalidRecordIDException, RecordStoreException {
        Address recordAddress = findRecordAddress(recordId);
        IRmsEntry entry = manager.getEntryAt(recordAddress);
        if (!(entry instanceof IRecordEntry)) {
            throw new RecordStoreException("System error found a non record entry where one expected for id: " + recordId);
        }
        return (IRecordEntry) entry;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getNextRecordId() {
        return lastRecordId + 1;
    }
    
    public int getNumRecords() {
        return numRecords;
    }
    
    public byte[] getRecord(int recordId) throws InvalidRecordIDException, RecordStoreException {
        IRecordEntry record = findRecord(recordId);
        return record.getBytes();
    }

    public int getRecord(int recordId, byte[] buffer, int offset) throws InvalidRecordIDException, RecordStoreException {
        IRecordEntry record = findRecord(recordId);
        return record.getBytes(buffer, offset, record.getBytesLength());
    }

    public int[] getRecordIdsCopy() {
        int[] copy = new int[numRecords];
        System.arraycopy(recordIds, 0, copy, 0, numRecords);
        return copy;
    }
    
    public int getRecordSize(int recordId) throws InvalidRecordIDException, RecordStoreException {
        IRecordEntry record = findRecord(recordId);
        return record.getBytesLength();
    }
    
    public int getRecordsSize() throws RecordStoreException {
        int size = 0;
        try {
            for (int i=0, max=numRecords; i < max; i++) {
                IRmsEntry entry = manager.getEntryAt(recordAddresses[i]);
                if (!(entry instanceof IRecordEntry)) {
                    throw new RecordStoreException("System error found a non record entry where one expected");
                }
                size += entry.size();
            }
        } catch (RecordStoreException e) {
            throw new RuntimeException(e.getMessage());
        }
        return size;
    }

    public long getTimestamp() {
        return lastModifiedTimestamp;
    }
    
    public int getType() {
        return TYPE;
    }
    
    public int getVersion() {
        return version;
    }
    
    protected void insertAtIndex(int index) {
        // See if there is room for one more
        if (numRecords == recordIds.length) {
            int[] tempIds = new int[recordIds.length + INSERT_PADDING];
            System.arraycopy(recordIds, 0, tempIds, 0, index);
            if (index != numRecords) {
                System.arraycopy(recordIds, index, tempIds, index + 1, recordIds.length - index);
            }
            recordIds = tempIds;
            Address[] tempAddresses = new Address[recordIds.length];
            System.arraycopy(recordAddresses, 0, tempAddresses, 0, index);
            tempAddresses[index] = Address.zero();
            if (index != numRecords) {
                System.arraycopy(recordAddresses, index, tempAddresses, index + 1, recordAddresses.length - index);
            }
            recordAddresses = tempAddresses;
        } else if (index != numRecords) {
            // above, dont bother doing a copy if we are inserting at the end.
            System.arraycopy(recordIds, index, recordIds, index + 1, recordIds.length - index -1);
            recordIds[index] = 0;
            System.arraycopy(recordAddresses, index, recordAddresses, index + 1, recordAddresses.length - index -1);
            recordAddresses[index] = Address.zero();
        } else {
            recordAddresses[numRecords] = Address.zero();
        }
        numRecords++;
    }
    
    public void readFrom(IMemoryHeapBlock memoryBlock) throws IOException {
        super.readFrom(memoryBlock);
        DataInputStream input = memoryBlock.getDataInputStream();
        id = input.readInt();
        name = input.readUTF();
        lastModifiedTimestamp = input.readLong();
        version = input.readInt();
    }
    
    public void reVisitRecordEntry(IRecordEntry record) throws RecordStoreException {
        int recordId = record.getId();
        int index = findRecordIndex(recordId);
        if (index < 0) {
            throw new RecordStoreException("System error, record should have been found");
        } else {
            recordAddresses[index] = record.getAddress();
        }
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setRecord(int recordId, byte[] bytes, int offset, int length) throws InvalidRecordIDException, RecordStoreException, RecordStoreFullException {
        int index = findRecordIndex(recordId);
        if (index < 0) {
            throw new InvalidRecordIDException();
        }
        // Write new record first, then invalidate the old one
        Address oldAddress = recordAddresses[index];
        IRecordEntry record = new RecordEntry();
        record.setBytes(bytes, offset, length);
        record.setStoreId(id);
        record.setId(recordId);
        recordAddresses[index] = manager.logEntry(record);
        manager.invalidateEntryAt(oldAddress);
        changed();
    }
    
    public void visit(IRmsEntryVisitor visitor) throws RecordStoreException {
        visitor.visitRecordStore(this);
    }
    
    public void visitRecordEntry(IRecordEntry record) throws RecordStoreException {
        int recordId = record.getId();
        int index = findRecordIndex(recordId);
        if (index < 0) {
            // record id was not found
            index = -index - 1;
            insertAtIndex(index);
            recordIds[index] = recordId;
            recordAddresses[index] = record.getAddress();
        } else {
            // record id was found, which means we had a failure to finish a prior setRecord operation
            manager.invalidateEntryAt(recordAddresses[index]);
            recordAddresses[index] = record.getAddress();
        }
        if (recordId > lastRecordId) {
            lastRecordId = recordId;
        }
    }

    public void writeTo(DataOutputStream dataOut) throws IOException {
        super.writeTo(dataOut);
        dataOut.writeInt(id);
        dataOut.writeUTF(name);
        dataOut.writeLong(lastModifiedTimestamp);
        dataOut.writeInt(version);
    }

    public int size() {
        return 4 + (2 + DataOutputUTF8Encoder.lengthAsUTF(name)) + 8 + 4;
    }
    
}
