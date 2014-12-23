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

import java.io.*;
import javax.microedition.io.*;

import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;

/**
 * This class facilitates saving a serialized object graph to a URL.
 * The format of the output is described by {@link ObjectMemoryFile}.
 *
 */
public class ObjectMemorySerializer {

    public static final int CURRENT_MAJOR_VERSION = 1;
    public static final int CURRENT_MINOR_VERSION = 1;
    
    /**
     * Purely static class should not be instantiated.
     */
    private ObjectMemorySerializer() {}

    /**
     * A ControlBlock instance is used to pass parameters in both directions when
     * calling the <code>copyObjectGraph</code> low level routine
     * that serializes an object graph.
     */
    static final class ControlBlock {
        private ControlBlock() {
        }

        /**
         * The buffer containing the serialized object graph.
         */
        public byte[] memory;

        /**
         * The address to which the pointers in <code>memory</code> are relative before
         * they are relocated to canonical addresses.
         */
        public Address start;

        /**
         * The oop map that describes which words in the serialized graph are pointers.
         */
        public BitSet oopMap;

        /**
         * The offset in the serialized graph to the root of the graph.
         */
        public int root;
        
        public static ControlBlock createControlBlock() {
            return new ControlBlock();
        }
    }


    /*---------------------------------------------------------------------------*\
     *                                  Saving                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * Writes a serialized object memory to a given output stream.
     *
     * @param    dos       where the object memory should be written
     * @param    uri       a URI identifying the object memory being saved
     * @param    cb        the control block describing the serialized object graph
     * @param    parent    the object memory to which the serialized object memory is bound
     * @param    bigEndian the endianess to be used when serializing the object memory
     * @throws IOException     if there is an IO error
     */
    public static void save(final DataOutputStream dos, final String uri, final ControlBlock cb, final ObjectMemory parent, final boolean bigEndian) throws IOException {
        Assert.that(parent != null  || VM.isHosted());

        // Figure out correct DataOutputStream to use
        ObjectMemoryOutputStream sfos = new ObjectMemoryOutputStream(dos);

        // Tracing
        if (Klass.TRACING_ENABLED && Tracer.isTracing("oms")) {
            Tracer.traceln("Saving object memory to " + uri);
        }

        // Write the magic file number
        sfos.writeInt(0xdeadbeef, "magic");

        // Write the version numbers
        sfos.writeShort(CURRENT_MINOR_VERSION, "minor_version");
        sfos.writeShort(CURRENT_MAJOR_VERSION, "major_version");

        // Write the attributes
        int attributes = 0;
/*if[TYPEMAP]*/
        if (VM.usingTypeMap()) {
            attributes |= ObjectMemoryFile.ATTRIBUTE_TYPEMAP;
        }
/*end[TYPEMAP]*/

        if (!Klass.SQUAWK_64) {
            attributes |= ObjectMemoryFile.ATTRIBUTE_32BIT;
        }

        if (bigEndian) {
            attributes |= ObjectMemoryFile.ATTRIBUTE_BIGENDIAN;
        }

        sfos.writeInt(attributes, "attributes");

        if (parent == null) {
            sfos.writeInt(0, "parent_hash");
            sfos.writeUTF("", "parent_uri");
        } else {
            sfos.writeInt(parent.getHash(), "parent_hash");
            sfos.writeUTF(parent.getURI(), "parent_uri");
        }

        final int size = cb.memory.length;
        sfos.writeInt(cb.root, "root");
        sfos.writeInt(size, "size");

        // Write the oop map
        byte[] bits = new byte[GC.calculateOopMapSizeInBytes(size)];
        cb.oopMap.copyInto(bits);
        sfos.write(bits, "oopmap");

        // Relocate the memory
        Address canonicalStart = relocateMemory(cb.memory, cb.start, cb.oopMap, parent, Klass.TRACING_ENABLED && Tracer.isTracing("oms"));
        Address srcAddress = VM.isHosted() ? cb.start : Address.fromObject(cb.memory);

        if (Klass.TRACING_ENABLED && Tracer.isTracing("oms")) {

            final int canStart = canonicalStart.toUWord().toInt();
            String pointersURI;
            if (uri.startsWith("memory:")) {
                pointersURI = "file://" + uri.substring("memory:".length());
            } else {
                pointersURI = uri;
            }
            PrintStream out = new PrintStream(Connector.openOutputStream(pointersURI + ".pointers"));
            BitSet oopMap = new BitSet(bits);

            for (int offset = oopMap.nextSetBit(0); offset != -1; offset = oopMap.nextSetBit(offset + 1)) {
                int pointerAddress = canStart + (offset * HDR.BYTES_PER_WORD);
                Address pointer = NativeUnsafe.getAddress(srcAddress, offset);
                out.println(pointerAddress + " [offset " + (offset * HDR.BYTES_PER_WORD) + "] : " + pointer.toUWord().toPrimitive());
            }
            out.close();
            Tracer.traceln("oopmap:{cardinality = " + oopMap.cardinality() + "}");
        }

        // Write the padding to ensure 'memory' is word aligned
        int pad = ObjectMemoryLoader.calculateMemoryPadding(parent == null ? "" : parent.getURI(), size);
        while (pad-- != 0) {
            sfos.writeByte(0);
        }

        // Do endianess swapping if required
        final boolean requiresEndianSwap = (VM.isBigEndian() != bigEndian);
        if (requiresEndianSwap) {
            ObjectMemory om = new ObjectMemory(srcAddress, size, "", null, 0, parent);
            ObjectMemoryEndianessSwapper.swap(om, false, true);

            if (VM.isHosted()) {
                // Need to copy swapped memory back into cb.memory
                NativeUnsafe.copyMemory(cb.memory, cb.start.toUWord().toInt(), 0, cb.memory.length);
            }
        }
        // Write the object memory itself.
        sfos.write(cb.memory, "memory");

/*if[TYPEMAP]*/
        if (VM.usingTypeMap()) {
            writeTypeMap(sfos, srcAddress, size);
        }
/*end[TYPEMAP]*/

        // Closes the stream if we created it
        sfos.flush();
        if (dos == null) {
            sfos.close();
        }

        // Tracing
        if (Klass.TRACING_ENABLED && Tracer.isTracing("oms")) {
            Tracer.traceln("Saved object memory to " + uri);
        }
    }

    /**
     * Relocates the pointers in an object memory to be in their canonical form.
     *
     * @param memory       the serialized memory
     * @param start        the address of the serialized memory
     * @param oopMap       describes where all the not yet relocated pointers in the source memory are. The cardinality
     *                     of the set will be 0 upon returning.
     * @param parent       the parent object memory (may be null)
     * @param tracing      enables tracing
     * @return the canonical start address of the relocated memory
     */
    private static Address relocateMemory(byte[] memory, Address start, BitSet oopMap, ObjectMemory parent, boolean tracing) {
        Address canonicalStart;

//System.out.println("before parent: oopMap.cardinality = " + oopMap.cardinality());
        if (parent != null) {
            canonicalStart = parent.getCanonicalEnd();
            ObjectMemory.relocateParents("RAM",
                                         null,
                                         VM.isHosted() ? start : Address.fromObject(memory),
                                         oopMap,
                                         parent,
                                         true,
                                         false,
                                         tracing);
        } else {
            canonicalStart = Address.zero();
        }

//System.out.println("after parent: oopMap.cardinality = " + oopMap.cardinality());
        ObjectMemory.relocate("RAM",
                              null,
                              VM.isHosted() ? start : Address.fromObject(memory),
                              oopMap,
                              start,
                              canonicalStart,
                              memory.length,
                              true,
                              false,
                              tracing,
                              true);

//System.out.println("after self: oopMap.cardinality = " + oopMap.cardinality());
        Assert.always(oopMap.cardinality() == 0); // "some pointers were not relocated"
        return canonicalStart;
    }

/*if[TYPEMAP]*/
    /**
     * Writes the type map describing the type of every address in an object memory.
     *
     * @param sfos    where to write the map
     * @param start   the start address of the object memory
     * @param size    the size address of the object memory
     */
    private static void writeTypeMap(ObjectMemoryOutputStream sfos, Address start, int size) throws IOException {
        Address p = start;
        for (int i = 0; i != size; ++i) {
            byte type = NativeUnsafe.getType(p);
            sfos.writeByte(type);
            p = p.add(1);
        }
        if (Klass.TRACING_ENABLED && Tracer.isTracing("oms")) {
            Tracer.traceln("typemap:{size = " + size + "}");
        }
    }
/*end[TYPEMAP]*/

}

/**
 * An instance of <code>ObjectMemoryOutputStream</code> is used to write the components of
 * a object memory file to an output stream.
 *
 */
final class ObjectMemoryOutputStream extends DataOutputStream {

    /**
     * Creates a <code>ObjectMemoryOutputStream</code>.
     *
     * @param   os        the output stream
     */
    public ObjectMemoryOutputStream(OutputStream os) {
        super(os);
    }

    /**
     * Writes a byte array to the stream.
     *
     * @param   value   the value to write
     * @param   prefix  the optional prefix used when tracing this read
     * @see     DataOutputStream#writeByte(int)
     */
    public final void write(byte[] value, String prefix) throws IOException {
        super.write(value);
        if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing("oms")) {
            Tracer.traceln(prefix+":{wrote " + value.length + " bytes}");
        }
    }

    /**
     * Writes a byte to the stream.
     *
     * @param   value   the value to write
     * @param   prefix  the optional prefix used when tracing this read
     * @see     DataOutputStream#writeByte(int)
     */
    public final void writeByte(int value, String prefix) throws IOException {
        super.writeByte(value);
        if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing("oms")) {
            Tracer.traceln(prefix+":"+(value&0xFF));
        }
    }

    /**
     * Writes a short to the stream.
     *
     * @param   value   the value to write
     * @param   prefix  the optional prefix used when tracing this read
     * @see     DataOutputStream#writeShort(int)
     */
    public final void writeShort(int value, String prefix) throws IOException {
        super.writeShort(value);
        if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing("oms")) {
            Tracer.traceln(prefix+":"+value);
        }
    }

    /**
     * Writes a int to the stream.
     *
     * @param   value the value to write
     * @param   prefix  the optional prefix used when tracing this read
     * @see     DataOutputStream#writeInt(int)
     */
    public final void writeInt(int value, String prefix) throws IOException {
        super.writeInt(value);
        if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing("oms")) {
            Tracer.traceln(prefix+":"+value);
        }
    }

    /**
     * Writes a string to the stream.
     *
     * @param   value   the value to write
     * @param   prefix  the optional prefix used when tracing this read
     * @see     DataOutputStream#writeUTF(String)
     */
    public final void writeUTF(String value, String prefix) throws IOException {
        super.writeUTF(value);
        if (Klass.TRACING_ENABLED && prefix != null && Tracer.isTracing("oms")) {
            Tracer.traceln(prefix+":\""+value+"\"");
        }
    }
}
