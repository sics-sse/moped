/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk;

/**
 * Stores a resource file (name and contents) in the suite file.
 *
 */
public final class ResourceFile {
    public final String name;
    public byte [] data;
    protected boolean isNew;
    protected boolean isPersistent;
    protected int length;
    
    public ResourceFile(String name, boolean isPersistent) {
        this.name = name;
        this.data = new byte[0];
        this.isPersistent = isPersistent;
        this.isNew = true;
    }

    /**
     * Creates a resource file object.
     * 
     * @param name resource name
     * @param data resource data
     */
    public ResourceFile(String name, byte [] data) {
        this.name = name;
        this.data = data;
        this.isNew = false;
        this.length = data.length;
    }

    public void close() {
        if (!isNew) {
            return;
        }
        if (data.length == length) {
            return;
        }
        byte[] newData = new byte[length];
        System.arraycopy(data, 0, newData, 0, length);
        data = newData;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void write(int index, byte byt) {
        if (!isNew) {
            throw new RuntimeException("Cannot write to a file resource that is not new");
        }
        if (index >= data.length) {
            byte[] newData = new byte[data.length + 1024];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        data[index] = byt;
        if (index >= length) {
            length = index + 1;
        }
    }
    
}
