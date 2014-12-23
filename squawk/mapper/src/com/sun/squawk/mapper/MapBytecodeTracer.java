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

package com.sun.squawk.mapper;

import java.util.Vector;

import com.sun.squawk.Address;
import com.sun.squawk.NativeUnsafe;

/**
 * MapBytecodeTracer
 */
public class MapBytecodeTracer extends com.sun.squawk.BytecodeTracer {
    Address addr;
    int length;
    int currentPosition;
    int lastPosition;
    Vector<BytecodeTracerEntry> queue = new Vector<BytecodeTracerEntry>();

    /**
     * Constructor.
     */
    public MapBytecodeTracer(Address addr, int length) {
        this.addr   = addr;
        this.length = length;
    }

    /**
     * Get the current bytecode offset.
     *
     * @return the value
     */
    protected int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Print a string.
     *
     * @param str the string
     */
    protected void print(String str) {
        int size = currentPosition - lastPosition;
        String s = ""+lastPosition+":";
        while (s.length() < 5) {
            s += " ";
        }
        s += " ";
        BytecodeTracerEntry entry = new BytecodeTracerEntry(addr.add(lastPosition), size, "    "+s+str);
        queue.addElement(entry);
        lastPosition = currentPosition;
    }

    /**
     * Get the next signed byte from the method.
     *
     * @return the value
     */
    protected int getByte() {
        return NativeUnsafe.getByte(addr, currentPosition++);
    }

    /**
     * Get one trace entry.
     *
     * @return the entry
     */
    public BytecodeTracerEntry trace() {
        if (queue.isEmpty()) {
            traceByteCode();
        }
        BytecodeTracerEntry entry = (BytecodeTracerEntry)queue.firstElement();
        queue.removeElementAt(0);
        return entry;
    }

}
