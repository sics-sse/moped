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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import org.jmock.integration.junit3.MockObjectTestCase;

import com.sun.squawk.flash.MemoryHeapBlock;

public abstract class RmsEntryTest extends MockObjectTestCase {
    protected IRmsEntryVisitor visitor;

    public void setUp() throws Exception {
        visitor = mock(IRmsEntryVisitor.class);
    }

    public void tearDown() throws Exception {
    }

    public void doCompareEntries(IRmsEntry entryWritten, IRmsEntry entryRead) {
        assertEquals(entryWritten.getAddress(), entryRead.getAddress());
        assertEquals(entryWritten.size(), entryRead.size());
    }
    
    public void doTestEntry(IRmsEntry entryToWrite, IRmsEntry entryToRead) throws RecordStoreException, IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        DataOutputStream output = new DataOutputStream(bytesOut);
        entryToWrite.writeTo(output);

        MemoryHeapBlock block = new MemoryHeapBlock();
        block.setBytes(bytesOut.toByteArray(), 0, bytesOut.size());
        entryToRead.readFrom(block);
        
        assertEquals(-1, block.getDataInputStream().read());
        assertNotNull(entryToRead);
        assertNotSame(entryToRead, entryToWrite);
        assertSame(entryToWrite.getClass(), entryToRead.getClass());
        doCompareEntries(entryToWrite, entryToRead);
    }
    
    public abstract void testEntry() throws RecordStoreException, IOException;
    
    public abstract void testVisit() throws RecordStoreException ;
    
}
