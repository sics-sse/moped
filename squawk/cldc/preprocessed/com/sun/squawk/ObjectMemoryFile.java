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

package com.sun.squawk;

/**
 * An ObjectMemoryFile encapsulates all the data in a serialized object graph.
 * The format of a serialized object graphic is
 * described by the following pseudo C struct:
 *
 * <p><hr><blockquote><pre>
 *    ObjectMemory {
 *        u4 magic               // 0xDEADBEEF
 *        u2 minor_version;
 *        u2 major_version;
 *        u4 attributes;         // mask of the ATTRIBUTE_* constants in this class
 *        u4 parent_hash;
 *        utf8 parent_uri;
 *        u4 root;               // offset (in bytes) in 'memory' of the root of the graph
 *        u4 size;               // size (in bytes) of memory
 *        u1 oopmap[((size / HDR.BYTES_PER_WORD) + 7) / 8];
 *        u1 padding[n];         // 0 <= n < HDR.BYTES_PER_WORD to align 'memory' on a word boundary
 *        u1 memory[size];
 *        u1 typemap[size];      // only present if ATTRIBUTE_TYPEMAP is set
 *    }
 * </pre></blockquote><hr><p>
 *
 *
 */
public final class ObjectMemoryFile {

    /**
     * Denotes a object memory file that has a type map describing the type of the value at every
     * address in the 'memory' component. The entries in the map are described in
     * {@link com.sun.squawk.vm.AddressType}.
     */
    public static final int ATTRIBUTE_TYPEMAP = 0x01;

    /**
     * Denotes a object memory file that is only compatible with a 32 bit system. Otherwise the object memory
     * file is only compatible with a 64 bit system.
     */
    public static final int ATTRIBUTE_32BIT = 0x02;

    /**
     * Denotes a object memory file that is in big endian format. Otherwise the object memory
     * file is in little endian format.
     */
    public static final int ATTRIBUTE_BIGENDIAN = 0x04;

    public final int minor;
    public final int major;
    public final int attributes;
    public final int parentHash;
    public final String parentURI;
    public final ObjectMemory objectMemory;

    public ObjectMemoryFile(int minor,
                            int major,
                            int attributes,
                            int parentHash,
                            String parentURI,
                            ObjectMemory objectMemory)
    {
        this.minor = minor;
        this.major = major;
        this.attributes = attributes;
        this.parentHash = parentHash;
        this.parentURI = parentURI;
        this.objectMemory= objectMemory;
    }

    /**
     * Determines if <code>attributes</code> value in this object memory file denoted a big endian format memory.
     *
     * @return true if <code>(this.attributes & ATTRIBUTE_BIGENDIAN) != 0</code>
     */
    public boolean isBigEndian() {
        return (attributes & ATTRIBUTE_BIGENDIAN) != 0;
    }
}
