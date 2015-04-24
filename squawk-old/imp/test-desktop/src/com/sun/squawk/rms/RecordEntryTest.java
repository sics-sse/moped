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

import javax.microedition.rms.RecordStoreException;

import org.jmock.InThisOrder;

public class RecordEntryTest extends RmsEntryTest {

    protected byte[] recordBytes;
    
    public void doCompareEntries(IRmsEntry entryWritten, IRmsEntry entryRead) {
        super.doCompareEntries(entryWritten, entryRead);
        final IRecordEntry castEntry = (IRecordEntry) entryWritten;
        final IRecordEntry castReadEntry = (IRecordEntry) entryRead;

        assertEquals(castEntry.getBytesLength(), castReadEntry.getBytesLength());
        assertEquals(castEntry.getId(), castReadEntry.getId());
        assertEquals(castEntry.getStoreId(), castReadEntry.getStoreId());
        assertTrue(Arrays.equals(castEntry.getBytes(), castReadEntry.getBytes()));
    }
    
    public void testEntry() throws RecordStoreException,IOException {
        recordBytes = new byte[] {0, 1, 2, 3, 127, (byte) 255};
        IRecordEntry record = new RecordEntry();
        record.setId(0);
        record.setStoreId(0);
        record.setBytes(recordBytes, 0, recordBytes.length);
        doTestEntry(record, new RecordEntry());
    }

    public void testEntry1() throws RecordStoreException,IOException {
        IRecordEntry record = new RecordEntry();
        record.setId(0);
        record.setStoreId(0);
        record.setBytes(null, 0, 0);
        doTestEntry(record, new RecordEntry());
    }

    public void testVisit() throws RecordStoreException {
        final RecordEntry entry = new RecordEntry();
        
        expects(new InThisOrder() {{
            one(visitor).visitRecord(entry);
        }});
        
        entry.visit(visitor);
    }

}
