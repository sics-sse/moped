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
package com.sun.cldc.jna;

import com.sun.squawk.Klass;
import com.sun.squawk.VM;
import com.sun.squawk.util.Assert;

/**
 * Abstract class for proxies to native structure types.
 */
public abstract class Structure {
    
    private final static boolean DEBUG = false;

    protected Pointer backingNativeMemory;

    protected Structure(Pointer backingNativeMemory) {
        this.backingNativeMemory = backingNativeMemory;
    }

    protected Structure() {
        this.backingNativeMemory = null;
    }

    /**
     *  Copy the fields of the struct from native memory to the Java fields
     */
    public abstract void read();

    /**
     *  Copy the java fields of the struct to native memory from the Java fields
     */
    public abstract void write();

    /**
     * 
     * @return the size of the native structure
     */
    public abstract int size();

    /** 
     * Get the backing native memory used by this structure. May return null if allocateMemory()
     * has not been called, or if the Structure is permantently attached to C memory.
     * @return the memory
     */
    public final Pointer getPointer() {
        return backingNativeMemory;
    }

    /** 
     * Set the backing native memory used by this structure. 
     * @param m the native memory
     * @throws IllegalStateException if this structure already has memory allocated
     */
    public final void useMemory(Pointer m) {
        if (backingNativeMemory != null &&
            backingNativeMemory.isValid()) {
            throw new IllegalStateException();
        }
        backingNativeMemory = m;
    }

    /** Debug utility */
    private void VMprintStruct() {
        VM.print("Structure(");
        VM.print(Klass.asKlass(getClass()).getInternalName());
        VM.print(" size: ");
        VM.print(size());
        if (backingNativeMemory == null) {
            VM.println(" memory never allocated)");
        } else {
            VM.print(" Pointer(");
            VM.printAddress(getPointer().address());
            VM.println("))");
        }
    }

    /**
     * Attempt to allocate backing memory for the structure.
     * 
     * This memory should be freed when not needed
     * 
     * @throws OutOfMemoryError if backing native memory cannot be allocated.
     * @throws IllegalStateException if this structure already has memory allocated
     */
    public void allocateMemory() throws OutOfMemoryError {
        if (backingNativeMemory != null &&
            backingNativeMemory.isValid()) {
            throw new IllegalStateException();
        }
        backingNativeMemory = new Pointer(size());
        if (DEBUG) {
            VM.print("Allocated memory for ");
            VMprintStruct();
        }
    }
    
    /**
     * Attempt to allocate backing memory for the structure.
     * 
     * @param size in bytes ito allocate
     * @throws IllegalArgumentException if the requested size is smaller than the default size
     * @throws OutOfMemoryError if backing native memory cannot be allocated.
     * @throws IllegalStateException if this structure already has memory allocated
     */
    public void allocateMemory(int size) throws OutOfMemoryError {
        int defaultsize = size();
        if (size < defaultsize) {
            throw new IllegalArgumentException();
        }
        if (backingNativeMemory != null &&
            backingNativeMemory.isValid()) {
            throw new IllegalStateException();
        }
        backingNativeMemory = new Pointer(size);
        if (DEBUG) {
            VM.print("Allocated memory of special size for ");
            VMprintStruct();
        }
    }

    /**
     * Free the backing memory for the structure.
     * 
     * @throws IllegalStateException if the memory has already been freed.
     */
    public void freeMemory() throws IllegalStateException {
        if (DEBUG) {
            VM.print("Freeing memory for ");
            VMprintStruct();
        }
        backingNativeMemory.free();
        backingNativeMemory = null;
    }

    /**
     * Release the backing memory for the structure, if it was malloced
     *
     * @throws IllegalStateException if the memory has already been freed.
     */
    public void release() throws IllegalStateException {
        if (DEBUG) {
            VM.print("Releasing memory for ");
            VMprintStruct();
        }
        backingNativeMemory.release();
        backingNativeMemory = null;
    }

    /**
     * Set the backing memory to zeros.
     */
    public void clear() {
        backingNativeMemory.clear(size());
    }
    
    public String toString() {
        String backingStr = (backingNativeMemory == null) ? " memory never allocated" : backingNativeMemory.toString();
        return "Structure(" + getClass().getName() + " size: " + size() + backingStr + ")";
    }
    
    /**
     * Singleton object that can be used as a NULL structure instance.
     */
    public final static Structure NULL = new NullStruct();
    
    static class NullStruct extends Structure {

        NullStruct() {
            super(Pointer.NULL());
        }

        public void read() {
            Assert.shouldNotReachHere();
        }

        public void write() {
            Assert.shouldNotReachHere();
        }

        public int size() {
            return 0;
        }
    }
}
