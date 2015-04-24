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

import javax.microedition.rms.RecordStoreException;

import org.jmock.InThisOrder;
import org.jmock.integration.junit3.MockObjectTestCase;
import org.junit.runner.JUnitCore;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.flash.INorFlashMemoryHeap;
import com.sun.squawk.flash.INorFlashSectorState;
import com.sun.squawk.util.NotImplementedYetException;

public class RecordStoreManagerScanVisitorTest extends MockObjectTestCase {
    public static final String RECORD_STORE_NAME1 = "test1";
    public static final String RECORD_STORE_NAME2 = "test2";

    public RecordStoreManager manager;
    public RecordStoreManagerScanVisitor visitor;
    
    public static void main(String[] args) {
        JUnitCore.main(RecordStoreManagerScanVisitorTest.class.getName());
    }
    
    public void setUp() throws Exception {
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, "name");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, "vendor");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, "version");
        final INorFlashSectorState[] sectors = new INorFlashSectorState[2];
        sectors[0] = mock(INorFlashSectorState.class, "sector-0");
        manager = new RecordStoreManager();
        visitor = new RecordStoreManagerScanVisitor(manager);
    }

    public void tearDown() throws Exception {
        manager = null;
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, null);
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, null);
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, null);
    }
    
    public void testVisitApplicationDescriptor() throws RecordStoreException, IOException {
        final IApplicationDescriptorEntry entry1 = mock(IApplicationDescriptorEntry.class, "entry1");
        final INorFlashMemoryHeap heap = mock(INorFlashMemoryHeap.class);
        manager.memoryHeap = heap;
        
        // first app desc
        visitor.visitApplicationDescriptor(entry1);
        assertSame(entry1, manager.currentApplicationDescriptor);

        // sec app desc
        final Address address = Address.fromPrimitive(11);
        final IApplicationDescriptorEntry entry2 = mock(IApplicationDescriptorEntry.class, "entry2");
        expects(new InThisOrder() {{
            one(entry1).getAddress();will(returnValue(address));
            one(heap).freeBlockAt(address);
        }});
        visitor.visitApplicationDescriptor(entry2);
        assertSame(entry2, manager.currentApplicationDescriptor);

        // IO error on invalidate entry
        expects(new InThisOrder() {{
            one(entry2).getAddress();will(returnValue(address));
            one(heap).freeBlockAt(address);will(throwException(new RecordStoreException()));
        }});
        try {
            visitor.visitApplicationDescriptor(entry2);
            fail();
        } catch (RecordStoreException e) {
        }
    }

    public void testVisitRecord() throws RecordStoreException {
        final IRecordEntry record = mock(IRecordEntry.class);
        final IRecordStoreEntry store = mock(IRecordStoreEntry.class);

        // error condition of no store for the record
        manager.currentRecordStores = new IRecordStoreEntry[] {null};
        expects(new InThisOrder() {{
            one(record).getStoreId();will(returnValue(0));
            one(record).getId();will(returnValue(0));
            one(record).getAddress();will(returnValue(Address.zero()));
        }});

        visitor.visitRecord(record);

        // normal case, record store found
        manager.currentRecordStores = new IRecordStoreEntry[] {store};
        
        expects(new InThisOrder() {{
            one(record).getStoreId();will(returnValue(0));
            one(store).visitRecordEntry(record);
        }});
        
        visitor.visitRecord(record);
    }

    public void testVisitRecordStore() throws RecordStoreException, IOException {
        final IRecordStoreEntry entry1 = mock(IRecordStoreEntry.class, "entry1");
        final INorFlashMemoryHeap heap = mock(INorFlashMemoryHeap.class);
        manager.currentRecordStores = new IRecordStoreEntry[] {null, null};
        manager.memoryHeap = heap;
        expects(new InThisOrder() {{
            one(entry1).getId();will(returnValue(1));
        }});
        
        // first encounter of record store id 1
        visitor.visitRecordStore(entry1);
        assertSame(entry1, manager.currentRecordStores[1]);

        // second encounter of record store id 1
        final IRecordStoreEntry entry2 = mock(IRecordStoreEntry.class, "entry2");
        final Address address1 = Address.fromPrimitive(12);
        expects(new InThisOrder() {{
            one(entry2).getId();will(returnValue(1));
            atLeast(1).of(entry1).getAddress();will(returnValue(address1));
            one(heap).freeBlockAt(address1);
        }});
        visitor.visitRecordStore(entry2);
        assertSame(entry2, manager.currentRecordStores[1]);
        
        // IO error on invalidate entry
        final Address address2 = Address.fromPrimitive(13);
        expects(new InThisOrder() {{
            one(entry2).getId();will(returnValue(1));
            atLeast(1).of(entry2).getAddress();will(returnValue(address2));
            one(heap).freeBlockAt(address2);will(throwException(new RecordStoreException()));
        }});
        try {
            visitor.visitRecordStore(entry2);
            fail();
        } catch (RecordStoreException e) {
        }
    }

    public void testVisitRecordStoreSequence() throws RecordStoreException {
        IRecordStoreSequenceEntry entry = mock(IRecordStoreSequenceEntry.class);
        
        try {
            visitor.visitRecordStoreSequence(entry);
            fail();
        } catch (NotImplementedYetException e) {
        }
    }
    
    public void testVisitUnknownEntry() throws RecordStoreException {
        visitor.visitUnknown(null);
    }

}
