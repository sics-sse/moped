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

import com.sun.squawk.Address;
import com.sun.squawk.flash.IMemoryHeapBlock;

/*
 * An abstract class that describes the variuous types of entries that are stored in the RMS:
 *  - RecordEntry
 *  - RecordStoreEntry
 *  - RecordStoreSequenceEntry
 *  - ApplicationEscriptorEntry
 *  - UnknownEntry
 */
public abstract class RmsEntry implements IRmsEntry {

    protected Address address;
    
    public RmsEntry() {
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void readFrom(IMemoryHeapBlock memoryBlock) throws IOException {
    }

    /** Get the size of the entry including any bookkeeping data stored with this entry.
     * This matches the number of bytes read/written by the readFrom()/writeTo() methods.
     *
     * @return the entry size in bytes
     */
    abstract public int size();

    public void writeTo(DataOutputStream dataOut) throws IOException {
    }

}
