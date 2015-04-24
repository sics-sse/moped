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

package com.sun.squawk.imp;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.jmock.integration.junit3.MockObjectTestCase;

import com.sun.squawk.VM;
import com.sun.squawk.rms.ApplicationDescriptorEntry;

public class ImpGlobalTest extends MockObjectTestCase {

    public void testGetRecordStoreManager1() {
        
        try {
            ImpGlobal.getRecordStoreManager();
            fail();
        } catch (RecordStoreException e) {
        }
        try {
            ImpGlobal.getRecordStoreManager();
            fail();
        } catch (RecordStoreException e) {
        }
    }
    
    public void testGetRecordStoreManager2() {
        
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, "name");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, "vendor");
        VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, "version");
        try {
            ImpGlobal.getRecordStoreManager();
        } catch (RecordStoreException e) {
            fail();
        } finally {
            VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_NAME, null);
            VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VENDOR, null);
            VM.setManifestProperty(ApplicationDescriptorEntry.PROPERTY_MIDLET_VERSION, null);
        }
    }
    
    public void testForceEraseRecordStores() {
        ImpGlobal.forceEraseRecordStores();
        assertNull(RecordStore.listRecordStores());
    }
    
}
