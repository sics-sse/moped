/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.util;

import java.io.*;

public class SerializableIntHashtable extends IntHashtable implements Serializable {

    public SerializableIntHashtable(int initialCapacity) {
        super(initialCapacity);
    }

    public SerializableIntHashtable() {
        super();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        count = in.readInt();
        threshold = in.readInt();
        int tableLength = in.readInt();
        table = new IntHashtableEntry[tableLength];
        for (int i=0; i < tableLength; i++) {
            IntHashtableEntry entry = null;
            while (true) {
                if (!in.readBoolean()) {
                    break;
                }
                IntHashtableEntry nextEntry = new IntHashtableEntry();
                nextEntry.key = in.readInt();
                nextEntry.value = in.readObject();
                if (entry == null) {
                    table[i] = nextEntry;
                    entry = nextEntry;
                } else {
                    entry.next = nextEntry;
                    entry = nextEntry;
                }
            }
        }
        rehash();
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(count);
        out.writeInt(threshold);
        int tableLength = table.length;
        out.writeInt(tableLength);
        for (int i=0; i < tableLength; i++) {
            IntHashtableEntry entry = table[i];
            while (entry != null) {
                out.writeBoolean(true);
                out.writeInt(entry.key);
                out.writeObject(entry.value);
                entry = entry.next;
            }
            out.writeBoolean(false);
        }
    }
    
}
