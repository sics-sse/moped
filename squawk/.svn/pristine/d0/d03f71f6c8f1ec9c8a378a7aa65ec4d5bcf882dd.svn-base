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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.flash.IMemoryHeapBlock;
import com.sun.squawk.flash.INorFlashMemoryHeap;
import com.sun.squawk.flash.INorFlashMemoryHeapScanner;
import com.sun.squawk.flash.NorFlashMemoryHeap;

/**
 * Keeps track of multiple records stores (by name). Also handles case where owning midlet has changed,
 * so store has to be erased.
 */
public class RecordStoreManager implements IRecordStoreManager {
    private final static boolean DO_VERSION_CHECKS = false; // app rollback means that we might be going backwards intentionally

    protected INorFlashMemoryHeap memoryHeap;
    protected IRecordStoreEntry[] currentRecordStores;
    protected IApplicationDescriptorEntry currentApplicationDescriptor;
    protected INorFlashMemoryHeapScanner memoryHeapScanner;
    protected IRmsEntryVisitor scanVisitor;
    protected IRmsEntryVisitor reScanVisitor;
    
    protected RecordStoreManager() {  // used by testing
    }

    public RecordStoreManager(INorFlashMemoryHeap memoryHeap) throws RecordStoreException {
        this.memoryHeap = memoryHeap;
        this.memoryHeapScanner = this;
        init();
    }
    
    // is this used?
    public RecordStoreManager(INorFlashMemoryHeap memoryHeap, INorFlashMemoryHeapScanner memoryHeapScanner, IRmsEntryVisitor scanVisitor, IRmsEntryVisitor reScanVisitor) throws RecordStoreException {
        this.memoryHeap = memoryHeap;
        this.memoryHeapScanner = memoryHeapScanner;
        this.scanVisitor = scanVisitor;
        this.reScanVisitor = reScanVisitor;
        currentRecordStores = new IRecordStoreEntry[4];
        init();
    }
    
    protected String checkMIDletVersion(String nameInstalled, String vendorInstalled, String versionInstalled) {
        if (currentApplicationDescriptor == null) {
            return "Initializing RMS for MID-let: " + nameInstalled + ", -Version: " + versionInstalled + ", -Vendor: " + vendorInstalled;
        }
        String nameFound = currentApplicationDescriptor.getMidletName();
        String vendorFound = currentApplicationDescriptor.getMidletVendor();
        String versionFound = currentApplicationDescriptor.getMidletVersion();
        if (nameInstalled == null) {
            nameInstalled = "";
        }
        if (!nameInstalled.equals(nameFound)) {
            return "Erasing RMS, since installed MIDlet-Name: " + nameInstalled + " does not match prior -Name: " + nameFound;
        }
        if (vendorInstalled == null) {
            vendorInstalled = "";
        }
        if (!vendorInstalled.equals(vendorFound)) {
            return "Erasing RMS, since installed MIDlet-Vendor: " + vendorInstalled + " does not match prior -Vendor: " + vendorFound;
        }
        if (!DO_VERSION_CHECKS) {
            return null;
        } else {
            if (versionInstalled == null) {
                if (versionFound == null) {
                    return null;
                }
            } else if (versionInstalled.compareTo(versionFound) >= 0) {
                return null;
            }
        }
        return "Erasing RMS, since installed MIDlet-Version: " + versionInstalled + " does not match prior -Version: " + versionFound;
    }
    
    public boolean deleteRecordStore(String name) throws RecordStoreException {
        IRecordStoreEntry recordStore = getRecordStore(name, false);
        if (recordStore == null) {
            return false;
        }
        recordStore.deleteRecords();
        invalidateEntryAt(recordStore.getAddress());
        currentRecordStores[recordStore.getId()] = null;
        return true;
    }
    
    public IRmsEntry getEntryAt(Address address) throws RecordStoreException {
        return getEntryIn(memoryHeap.getBlockAt(address));
    }
    
    public IRmsEntry getEntryIn(IMemoryHeapBlock block) throws RecordStoreException {
        if (block == null || !block.isAllocated()) {
            return null;
        }
        IRmsEntry entry;
        try {
            byte type = block.getDataInputStream().readByte();
            switch (type) {
                case ApplicationDescriptorEntry.TYPE:
                    entry = new ApplicationDescriptorEntry();
                    break;
                case RecordEntry.TYPE:
                    entry = new RecordEntry();
                    break;
                case RecordStoreEntry.TYPE:
                    entry = new RecordStoreEntry(this);
                    break;
                case RecordStoreSequenceEntry.TYPE:
                    entry = new RecordStoreSequenceEntry();
                    break;
                default:
                    entry = new UnknownEntry();                    
            }
            entry.setAddress(block.getAddress());
            entry.readFrom(block);
            return entry;
        } catch (IOException e) {
            String msg = "Error parsing record at: " + block.getAddress().toUWord().toPrimitive() + ".\n    Enclosed error: " + e.toString();
            if (NorFlashMemoryHeap.CONTINUE_PAST_ERRORS) {
                System.err.println("[RMS] " + msg);
                return null;
            } else {
                throw new RecordStoreException(msg);
            }
        }
    }
    
    public long getErasedSequenceCurrentValue() {
        return memoryHeap.getErasedSequenceCurrentValue();
    }
    
    protected IRecordStoreEntry getRecordStore(int storeId) {
        if (storeId >= currentRecordStores.length) {
            IRecordStoreEntry[] temp = currentRecordStores;
            currentRecordStores = new IRecordStoreEntry[storeId + 1];
            System.arraycopy(temp, 0, currentRecordStores, 0, temp.length);
        }
        return currentRecordStores[storeId];
    }
    
    public IRecordStoreEntry getRecordStore(String name, boolean createIfNecessary) throws RecordStoreException {
        for (int i=0, max=currentRecordStores.length; i < max; i++) {
            IRecordStoreEntry recordStore = currentRecordStores[i];
            if (recordStore != null) {
                if (recordStore.getName().equals(name)) {
                    return recordStore;
                }
            }
        }
        if (createIfNecessary) {
            IRecordStoreEntry recordStore = new RecordStoreEntry(this);
            recordStore.setName(name);
            // Get the next available store id
            int foundId = -1;
            for (int i=0, max=currentRecordStores.length; i < max; i++) {
                if (currentRecordStores[i] == null) {
                    foundId = i;
                    break;
                }
            }
            if (foundId == -1) {
                foundId = currentRecordStores.length;
                // Will cause the recordStoreEntries to grow accordingly
                getRecordStore(foundId);
            }
            recordStore.setId(foundId);
            currentRecordStores[foundId] = recordStore;
            logEntry(recordStore);
            return recordStore;
        }
        return null;
    }
    
    public String[] getRecordStoreNames() {
        Vector names = new Vector();
        for (int i=0, max=currentRecordStores.length; i < max; i++) {
            IRecordStoreEntry entry = currentRecordStores[i];
            if (entry != null) {
                names.addElement(entry.getName());
            }
        }
        if (names.size() == 0) {
            return null;
        }
        String[] strings = new String[names.size()];
        names.copyInto(strings);
        return strings;
    }

    public int getSizeAvailable() throws RecordStoreException {
        return memoryHeap.getSizeAvailable();
    }

    private void init() throws RecordStoreException {
        currentRecordStores = new IRecordStoreEntry[4];
        if (scanVisitor == null) {
            scanVisitor = new RecordStoreManagerScanVisitor(this);
        }
        if (reScanVisitor == null) {
            reScanVisitor = new RecordStoreManagerReScanVisitor(this);
        }
        memoryHeap.scanBlocks(memoryHeapScanner);
        // Done scanning sectors, now check post conditions
        String installedMidletName = VM.getManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME);
        String installedMidletVendor = VM.getManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR);
        String installedMidletVersion = VM.getManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION);
        if (installedMidletName == null || installedMidletVendor == null || installedMidletVersion == null) {
            throw new RecordStoreException("No MIDlet suite installed.");
        }
        String eraseRmsReason = checkMIDletVersion(installedMidletName, installedMidletVendor, installedMidletVersion);
        if (eraseRmsReason != null) {
            if (VM.isVerbose()) {
                System.out.println(eraseRmsReason);
            }
            if (currentApplicationDescriptor != null) {
                invalidateEntryAt(currentApplicationDescriptor.getAddress());
            }
            // TODO This is a down side to my optimization of scanning, where only valid entries are
            // ever present, we need to delete all stores and their records in this case, can I come up
            // with an optimization for this ?
            // This is where timestamps might come in handy, think about it
            // TODO Write a test for this case
            for (int i=0; i < currentRecordStores.length; i++) {
                IRecordStoreEntry store = currentRecordStores[i];
                if (store == null) {
                    continue;
                }
                store.deleteRecords();
                invalidateEntryAt(store.getAddress());
            }
            currentApplicationDescriptor = new ApplicationDescriptorEntry();
            currentApplicationDescriptor.setMidletName(installedMidletName);
            currentApplicationDescriptor.setMidletVendor(installedMidletVendor);
            currentApplicationDescriptor.setMidletVersion(installedMidletVersion);
            logEntry(currentApplicationDescriptor);
            currentRecordStores = new IRecordStoreEntry[4];
        }
    }
    
    /**
     * Mark the block at "address" as "STALE". Updates actual flash memory.
     * @param address of RmsEntry to free
     * @throws RecordStoreException 
     */
    public void invalidateEntryAt(Address address) throws RecordStoreException {
        memoryHeap.freeBlockAt(address);
    }
    
    /**
     * Write the RmsEntry type byte, RmsEntry data, and optional pad byte.
     * 
     * @param entry RmsEntry to write
     * @return address of the written RmsEntry
     * @throws RecordStoreException 
     */
    public Address logEntry(IRmsEntry entry) throws RecordStoreException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(64);
        DataOutputStream output = new DataOutputStream(bytesOut);
        try {
            output.writeByte(entry.getType());
            entry.writeTo(output);
            int padding = 0;
            if ((bytesOut.size() & 1) == 1) {
                bytesOut.write(0);
                padding = 1;
            }
            output.close();
            entry.setAddress(memoryHeap.allocateAndWriteBlock(bytesOut.toByteArray(), 0, bytesOut.size() - padding, memoryHeapScanner));
            return entry.getAddress();
        } catch (IOException e) {
            throw new RecordStoreException(e.getMessage());
        }
    }
    
    public void reScanBlock(Address oldAddress, Address newAddress, IMemoryHeapBlock block) throws RecordStoreException {
        IRmsEntry entry = getEntryIn(block);
        if (entry == null) {
            return;
        }
        entry.setAddress(newAddress);
        entry.visit(reScanVisitor);
    }

    public void scanBlock(IMemoryHeapBlock block) throws RecordStoreException {
        IRmsEntry entry = getEntryIn(block);
        if (entry == null) {
            return;
        }
        entry.visit(scanVisitor);
    }

}
