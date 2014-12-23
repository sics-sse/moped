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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import org.jmock.InAnyOrder;
import org.jmock.InThisOrder;
import org.jmock.api.Invocation;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.jmock.lib.action.CustomAction;
import org.junit.runner.JUnitCore;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.flash.IMemoryHeapBlock;
import com.sun.squawk.flash.INorFlashMemoryHeap;
import com.sun.squawk.flash.INorFlashMemoryHeapScanner;
import com.sun.squawk.flash.INorFlashSectorState;
import com.sun.squawk.flash.MemoryHeapBlock;

//TODO Write tests that cover the extraneous cases where data read is wrong, sectors gone bad ?
public class RecordStoreManagerTest extends MockObjectTestCase {
    public static final String RECORD_STORE_NAME1 = "test1";
    public static final String RECORD_STORE_NAME2 = "test2";

    public RecordStoreManager castManager;
    public IRecordStoreManager manager;
    
    public static void main(String[] args) {
        JUnitCore.main(RecordStoreManagerTest.class.getName());
    }
    
    public void setUp() throws Exception {
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, "name");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, "vendor");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, "version");
        final INorFlashSectorState[] sectors = new INorFlashSectorState[2];
        sectors[0] = mock(INorFlashSectorState.class, "sector-0");
        castManager = new RecordStoreManager();
        manager = castManager;
    }

    public void tearDown() throws Exception {
        manager = null;
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, null);
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, null);
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, null);
    }
    
    public void testCheckMIDletVersion() throws RecordStoreException {
        final String midletName = "name";
        final String midletVendor = "vendor";
        final String midletVersion = "version";

        assertNotNull(castManager.checkMIDletVersion(midletName, midletVendor, midletVersion));
        
        final IApplicationDescriptorEntry applicationEntry = mock(IApplicationDescriptorEntry.class);
        castManager.currentApplicationDescriptor = applicationEntry;
        expects(new InAnyOrder() {{
            allowing(applicationEntry).getMidletName();will(returnValue(midletName));
            allowing(applicationEntry).getMidletVendor();will(returnValue(midletVendor));
            allowing(applicationEntry).getMidletVersion();will(returnValue(midletVersion));
        }});
        
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, midletVersion));

        assertNotNull(castManager.checkMIDletVersion(null, null, null));
        assertNotNull(castManager.checkMIDletVersion(midletName, null, null));
        assertNotNull(castManager.checkMIDletVersion(midletName, midletVendor, null));
        assertNotNull(castManager.checkMIDletVersion(null, midletVendor, midletVersion));
        assertNotNull(castManager.checkMIDletVersion(midletName, null, midletVersion));
        assertNotNull(castManager.checkMIDletVersion(midletName, midletVendor, null));
        assertNotNull(castManager.checkMIDletVersion(null, null, midletVersion));
    }
    
    public void testCheckMIDletVersionUpgrade() throws RecordStoreException {
        final String midletName = "name";
        final String midletVendor = "vendor";
        final String[] midletVersionHandle = new String[] {""};

        final IApplicationDescriptorEntry applicationEntry = mock(IApplicationDescriptorEntry.class);
        castManager.currentApplicationDescriptor = applicationEntry;
        expects(new InAnyOrder() {{
            allowing(applicationEntry).getMidletName();will(returnValue(midletName));
            allowing(applicationEntry).getMidletVendor();will(returnValue(midletVendor));
            allowing(applicationEntry).getMidletVersion();will(new CustomAction("getMidletVersion") {
                public Object invoke(Invocation invocation) throws Throwable {
                    return midletVersionHandle[0];
                }
            });
        }});

        midletVersionHandle[0] = "1.0";
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.0"));
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.1"));
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.1.1"));
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.00"));

        midletVersionHandle[0] = "1.1";
        assertNotNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.0"));
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.1"));
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.2"));
        assertNull(castManager.checkMIDletVersion(midletName, midletVendor, "1.1.1"));
}
    
    /*
     * record store name is not found
     */
    public void testDeleteRecordStore1() throws RecordStoreException {
        final IRecordStoreEntry recordStore = mock(IRecordStoreEntry.class);
        final String name = "name";
        castManager.currentRecordStores = new IRecordStoreEntry[] {null, recordStore};
        
        expects(new InThisOrder() {{
            one(recordStore).getName();will(returnValue(name));
        }});
        
        boolean deleted = manager.deleteRecordStore("not-there");
        assertFalse(deleted);
    }

    /*
     * record store name is found
     */
    public void testDeleteRecordStore2() throws Exception {
        final IRecordStoreEntry recordStore = mock(IRecordStoreEntry.class);
        final String name = "name";
        final Address address = Address.fromPrimitive(10);
        final INorFlashMemoryHeap heap = mock(INorFlashMemoryHeap.class);
        castManager.currentRecordStores = new IRecordStoreEntry[] {null, recordStore};
        castManager.memoryHeap = heap;
        
        expects(new InThisOrder() {{
            one(recordStore).getName();will(returnValue(name));
            one(recordStore).deleteRecords();
            one(recordStore).getAddress();will(returnValue(address));
            one(heap).freeBlockAt(address);
            one(recordStore).getId();will(returnValue(1));
        }});
        
        boolean deleted = manager.deleteRecordStore(name);
        assertTrue(deleted);
        assertNull(castManager.currentRecordStores[1]);
    }

    /*
     * address is to a freed block
     */
    public void testGetEntryAt() throws IOException, RecordStoreException {
        final Address address = Address.fromPrimitive(11);
        final INorFlashMemoryHeap heap = mock(INorFlashMemoryHeap.class);
        castManager.memoryHeap = heap;
        
        expects(new InThisOrder() {{
            one(heap).getBlockAt(address);will(returnValue(null));
        }});
        
        assertNull(manager.getEntryAt(address));
    }

    /*
     * address is to a block with 0 bytes
     */
    public void testGetEntryAt1() throws IOException, RecordStoreException {
        final Address address = Address.fromPrimitive(11);
        final INorFlashMemoryHeap heap = mock(INorFlashMemoryHeap.class);
        final MemoryHeapBlock block = new MemoryHeapBlock();
        castManager.memoryHeap = heap;
        
        expects(new InThisOrder() {{
            one(heap).getBlockAt(address);will(returnValue(block));
        }});
        
        assertNull(manager.getEntryAt(address));
    }

    /*
     * address is to a block with bytes for a simple IRmsEntry
     */
    public void testGetEntryAt2() throws IOException, RecordStoreException {
        final Address address = Address.fromPrimitive(11);
        final INorFlashMemoryHeap heap = mock(INorFlashMemoryHeap.class);
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(bytesOut);
        IRecordStoreSequenceEntry entry = new RecordStoreSequenceEntry();
        dataOut.writeByte(entry.getType());
        entry.writeTo(dataOut);
        final MemoryHeapBlock block = new MemoryHeapBlock();
        block.setIsAllocated(true);
        int padding = 0;
        if ((bytesOut.size() & 1) == 1) {
            bytesOut.write(0);
            padding = 1;
        }
        block.setBytes(bytesOut.toByteArray(), 0, bytesOut.size() - padding);
        castManager.memoryHeap = heap;
        
        expects(new InThisOrder() {{
            one(heap).getBlockAt(address);will(returnValue(block));
        }});
        
        IRmsEntry getEntry = manager.getEntryAt(address);
        assertNotNull(getEntry);
        assertSame(entry.getClass(), getEntry.getClass());
    }

    /*
     * 
     */
    public void testGetRecordStoreById() {
        IRecordStoreEntry recordStore = mock(IRecordStoreEntry.class);
        IRecordStoreEntry[] entries = new IRecordStoreEntry[] {null, recordStore};
        castManager.currentRecordStores = entries;

        // does not grow
        IRecordStoreEntry entry = castManager.getRecordStore(0);
        assertNull(entry);
        assertSame(entries, castManager.currentRecordStores);
        entry = castManager.getRecordStore(1);
        assertNotNull(entry);
        assertSame(recordStore, entry);
        assertSame(entries, castManager.currentRecordStores);
        
        // grows entries
        entry = castManager.getRecordStore(2);
        assertNull(entry);
        assertNotSame(entries, castManager.currentRecordStores);
        
        // check array was copied over on grow
        entry = castManager.getRecordStore(1);
        assertNotNull(entry);
        assertSame(recordStore, entry);
    }
    
    /*
     * name not found and do not create
     */
    public void testGetRecordStoreByName1() throws RecordStoreException {
        final IRecordStoreEntry recordStore = mock(IRecordStoreEntry.class);
        final String name = "name";
        castManager.currentRecordStores = new IRecordStoreEntry[] {null, recordStore};
        
        expects(new InThisOrder() {{
            one(recordStore).getName();will(returnValue(name));
        }});
        
        IRecordStoreEntry got = manager.getRecordStore("not-there", false);
        assertNull(got);
    }
    
    /*
     * name found
     */
    public void testGetRecordStoreByName2() throws RecordStoreException {
        final IRecordStoreEntry recordStore = mock(IRecordStoreEntry.class);
        final String name = "name";
        castManager.currentRecordStores = new IRecordStoreEntry[] {null, recordStore};
        
        expects(new InThisOrder() {{
            one(recordStore).getName();will(returnValue(name));
        }});
        
        IRecordStoreEntry got = manager.getRecordStore(name, false);
        assertSame(recordStore, got);
    }
    
    /*
     * name not found and do create
     */
    public void testGetRecordStoreByName3() throws IOException, RecordStoreException {
        final IRecordStoreEntry recordStore = mock(IRecordStoreEntry.class);
        final String name = "name";
        final INorFlashMemoryHeap heap = mock(INorFlashMemoryHeap.class);
        final INorFlashMemoryHeapScanner heapScanner = mock(INorFlashMemoryHeapScanner.class);
        castManager.currentRecordStores = new IRecordStoreEntry[] {null, recordStore};
        castManager.memoryHeap = heap;
        castManager.memoryHeapScanner = heapScanner;
        
        expects(new InAnyOrder() {{
            exactly(2).of(recordStore).getName();will(returnValue(name));
            exactly(2).of(heap).allocateAndWriteBlock(with(an(byte[].class)), with(an(Integer.class)), with(an(Integer.class)), with(same(heapScanner)));
        }});
        
        // No need to grow entries
        String notThereName = "not-there";
        IRecordStoreEntry got1 = manager.getRecordStore(notThereName, true);
        assertNotNull(got1);
        assertSame(0, got1.getId());
        assertSame(got1, castManager.currentRecordStores[0]);
        assertEquals(notThereName, got1.getName());
        
        // Will need to grow entries
        String notThereName2 = "not-there2";
        IRecordStoreEntry got2 = manager.getRecordStore(notThereName2, true);
        assertNotNull(got2);
        assertSame(2, got2.getId());
        assertSame(got2, castManager.currentRecordStores[2]);
        assertNotSame(got2, got1);
        assertEquals(notThereName2, got2.getName());
    }
    
    /*
     * No record stores present
     */
    public void testGetRecordStoreNames1() {
        castManager.currentRecordStores = new IRecordStoreEntry[0];
        
        String[] names = manager.getRecordStoreNames();
        assertNull(names);
    }
    
    /*
     * Record stores present
     */
    public void testGetRecordStoreNames2() {
        final IRecordStoreEntry recordStore = mock(IRecordStoreEntry.class);
        final String name = "name";
        castManager.currentRecordStores = new IRecordStoreEntry[] {null, recordStore};
        
        expects(new InThisOrder() {{
            one(recordStore).getName();will(returnValue(name));
        }});

        String[] names = manager.getRecordStoreNames();
        assertNotNull(names);
        assertEquals(1, names.length);
        assertEquals(name, names[0]);
    }
    
    public void testScanBlock() throws IOException, RecordStoreException {
        final IMemoryHeapBlock block = mock(IMemoryHeapBlock.class);
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream dataOut = new DataOutputStream(bytesOut);
        ApplicationDescriptorEntry entry = new ApplicationDescriptorEntry();
        dataOut.writeByte(entry.getType());
        entry.writeTo(dataOut);
        final ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytesOut.toByteArray());
        final DataInputStream dataIn = new DataInputStream(bytesIn);
        final Address address = Address.fromPrimitive(2);
        
        expects(new InThisOrder() {{
            one(block).isAllocated();will(returnValue(true));
            one(block).getDataInputStream();will(returnValue(dataIn));
            one(block).getAddress();will(returnValue(address));
            one(block).getDataInputStream();will(returnValue(dataIn));
        }});
        
        IRmsEntry entryRead = castManager.getEntryIn(block);
        assertNotNull(entryRead);
        assertSame(address, entryRead.getAddress());
    }
    
}
