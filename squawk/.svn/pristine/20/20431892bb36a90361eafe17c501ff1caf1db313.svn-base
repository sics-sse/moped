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

import javax.microedition.rms.RecordStoreException;

class RecordStoreManagerReScanVisitor implements IRmsEntryVisitor {
    protected final RecordStoreManager manager;

    public RecordStoreManagerReScanVisitor(RecordStoreManager manager) {
        this.manager = manager;
    }

    public void visitApplicationDescriptor(IApplicationDescriptorEntry newDescriptor) throws RecordStoreException {
        manager.currentApplicationDescriptor.setAddress(newDescriptor.getAddress());
    }

    public void visitRecord(IRecordEntry entry) throws RecordStoreException {
        int storeId = entry.getStoreId();
        IRecordStoreEntry recordStore = this.manager.getRecordStore(storeId);
        if (recordStore == null) {
            throw new RecordStoreException("System error: encountered record prior to store existing");
        }
        recordStore.reVisitRecordEntry(entry);
    }

    public void visitRecordStore(IRecordStoreEntry entry) throws RecordStoreException {
        IRecordStoreEntry originalEntry = manager.currentRecordStores[entry.getId()];
        originalEntry.setAddress(entry.getAddress());
    }

    public void visitRecordStoreSequence(IRecordStoreSequenceEntry entry) {
    }

    public void visitUnknown(IUnknownEntry entry) {
    }

}
