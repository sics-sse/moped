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

import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import com.sun.squawk.flash.IMemoryHeapBlock;


/**
 * I exist in order to allow the delete record, set record, and any other record store operation
 * that essentially does not need to have a new log entry posted.  But any operation which
 * changes the state of a record store must also change the version/sequence number of
 * the store, so we mark this with a simple marker.
 * 
 *
 */
public class RecordStoreSequenceEntry extends RmsEntry implements IRecordStoreSequenceEntry {
    public static final int TYPE = 4;

    protected int storeId;

    public RecordStoreSequenceEntry() {
        super();
    }

    public int getType() {
        return TYPE;
    }
    
    public int getStoreId() {
        return storeId;
    }
    
    public void readFrom(IMemoryHeapBlock memoryBlock) throws IOException {
        super.readFrom(memoryBlock);
        storeId = memoryBlock.getDataInputStream().readInt();
    }
    
    public void visit(IRmsEntryVisitor visitor) throws RecordStoreException {
        visitor.visitRecordStoreSequence(this);
    }
    
    public void writeTo(DataOutputStream dataOut) throws IOException {
        super.writeTo(dataOut);
        dataOut.writeInt(storeId);
    }

    public int size() {
        return 4;
    }

}
