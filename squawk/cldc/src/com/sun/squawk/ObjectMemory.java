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

import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;

/**
 * An ObjectMemory instance is an immutable wrapper for an object memory and
 * its metadata.
 *
 */
public final class ObjectMemory {

    /**
     * The reserved URI denoting the bootstrap suite.
     */
    public static final String BOOTSTRAP_URI = "memory:bootstrap";

    /**
     * The property name used to specify a different default than squawk.suite for the bootstrap suite.
     */
    public static final String BOOTSTRAP_URI_PROPERTY = "bootstrap.suite.url";

    /**
     * The hash object memory in canonical form.
     */
    private final int hash;

    /**
     * The URI identifying this object memory.
     */
    private final String uri;

    /**
     * The start address of the object memory.
     */
    private final Address start;

    /**
     * The size (in bytes) of the object memory.
     */
    private final int size;

    /**
     * The canonical start address of the object memory.
     */
    private final Address canonicalStart;

    /**
     * The root of the serialized graph in the object memory.
     */
    private Object root;

    /**
     * The direct parent object memory.
     */
    private final ObjectMemory parent;

    /**
     * Constructs a new object memory file.
     * 
     * @param start   the start address of the object memory.
     * @param size    the size (in bytes) of the object memory
     * @param uri     the URI identifying this object memory
     * @param root    the root of the serialized graph in the object memory
     * @param hash    the hash object memory in canonical form
     * @param parent  the direct parent object memory
     */
    public ObjectMemory(Address start, int size, String uri, Object root, int hash, ObjectMemory parent) {
        this.start = start;
        this.size = size;
        this.uri = uri;
        this.root = root;
        this.hash = hash;
        this.parent = parent;
        if (parent == null) {
            this.canonicalStart = Address.zero();
        } else {
            this.canonicalStart = parent.getCanonicalEnd();
        }
    }

    /**
     * Creates an ObjectMemory that is a wrapper for the bootstrap suite.
     *
     * @param bootstrapSuite 
     * @return an ObjectMemory that is a wrapper for the bootstrap suite
     */
    static public ObjectMemory createBootstrapObjectMemory(Suite bootstrapSuite) {
        return new ObjectMemory(VM.getBootstrapStart(),
                                VM.getBootstrapEnd().diff(VM.getBootstrapStart()).toInt(),
                                null,
                                bootstrapSuite,
                                VM.getBootstrapHash(),
                                null);
    }
    
//    public static  ObjectMemory createAppObjectMemory(Suite appSuite) {
//        return new ObjectMemory(VM.getAppStart(),
//                                VM.getAppEnd().diff(VM.getAppStart()).toInt(),
//                                null,
//                                appSuite,
//                                VM.getBootstrapHash(),
//                                null);
//    }

    /**
     * Gets the canonical starting address of this object memory. This address is computed
     * as the sum of the size of all the parent object memories.
     *
     * @return  the canonical starting address of this object memory
     */
    public Address getCanonicalStart() {
        return canonicalStart;
    }

    /**
     * Gets the address one byte past the end of the canonical object memory.
     *
     * @return the address one byte past the end of the canonical object memory
     */
    public Address getCanonicalEnd() {
        return getCanonicalStart().add(size);
    }

    /**
     * Gets the size (in bytes) of the object memory.
     *
     * @return the size of the object memory
     */
    public int getSize() {
        return size;
    }

    /**
     * Gets the root object in this object memory.
     *
     * @return the root object in this object memory
     */
    public Object getRoot() {
        return root;
    }
    
    /**
     * Set the root object of this object memory.
     * ONLY to be used by Romizer.
     * 
     * @param root
     */
    void setRoot(Object root) {
    	this.root = root;
    }

    /**
     * Gets the direct parent object memory of this object memory.
     *
     * @return the direct parent object memory of this object memory
     */
    public ObjectMemory getParent() {
        return parent;
    }

    /**
     * Gets the number of parents in the chain of parent object memories.
     *
     * @return the number of parents in the chain of parent object memories
     */
    public int getParentCount() {
        if (parent == null) {
            return 0;
        } else {
            return 1 + parent.getParentCount();
        }
    }

    /**
     * Gets the hash of the canonical form of this object memory.
     *
     * @return the hash of the canonical form of this object memory
     */
    public int getHash() {
        return hash;
    }

    /**
     * Gets the URI identifiying this object memory. If this is the object memory
     * containing the bootstrap suite, then "memory:bootstrap" is returned.
     *
     * @return the URI identifying this object memory
     */
    public String getURI() {
        if (parent == null) {
            return BOOTSTRAP_URI;
        } else {
            return uri;
        }
    }

    /**
     * Gets the start address of the object memory.
     *
     * @return the address of the object memory
     */
    public Address getStart() {
        if (!VM.isHosted() && parent == null) {
            return VM.getRomStart();
        }
        return start;
    }

    /**
     * Gets the address one byte past the end of the object memory.
     *
     * @return the address one byte past the end of the object memory
     */
    public Address getEnd() {
        return getStart().add(size);
    }

    /**
     * Determines if this object memory contains a given address.
     *
     * @param address  the address to test
     * @return true if this ObjectMemory contains <code>address</code>
     */
    public boolean containsAddress(Address address) {
        return (address.hieq(start) && address.lo(getEnd()));
    }

    /**
     * Determines if this object memory contains a given canonical address.
     *
     * @param canonicalAddress  the address to test
     * @return true if this ObjectMemory contains <code>canonicalAddress</code>
     */
    public boolean containsCanonicalAddress(Address canonicalAddress) {
        return (canonicalAddress.hieq(canonicalStart) && canonicalAddress.lo(getCanonicalEnd()));
    }

    /**
     * Searches up a chain of object memories starting from this one for a given physical address.
     * The given address must be within the chain of object memories.
     *
     * @param address  the address to search for
     * @return the ObjectMemory in which <code>address</code> lies
     */
    public ObjectMemory findAddress(Address address) {
        if (address.hieq(start)) {
            Assert.that(address.lo(getEnd()), "address is out of range");
            return this;
        }
        Assert.that(parent != null, "address is out of range");
        return parent.findAddress(address);
    }

    /**
     * Searches up a chain of object memories starting from this one for a given canonical address.
     * The given address must be within the chain of object memories.
     *
     * @param canonicalAddress  the canonical address to search for
     * @return the ObjectMemory in which <code>canonicalAddress</code> lies
     */
    public ObjectMemory findCanonicalAddress(Address canonicalAddress) {
        if (canonicalAddress.hieq(canonicalStart)) {
            Assert.that(canonicalAddress.lo(getCanonicalEnd()), "address is out of range");
            return this;
        }
        Assert.that(parent != null, "address is out of range");
        return parent.findCanonicalAddress(canonicalAddress);
    }

    /**
     * Converts a physical address that is within the range of this object memory into
     * a canonical address.
     *
     * @param address   the physical address to convert
     * @return  the canonical corresponding to <code>address</code>
     */
    public Address toCanonical(Address address) {
        Offset delta = address.diff(start);
        Assert.that(delta.toPrimitive() < size, "address is not within this object memory");
        return canonicalStart.addOffset(delta);
    }

    /**
     * Converts a canonical address that is within the range of this object memory into
     * a physical address.
     *
     * @param canonicalAddress   the canonical address to convert
     * @return  the physical corresponding to <code>canonicalAddress</code>
     */
    public Address fromCanonical(Address canonicalAddress) {
        Offset delta = canonicalAddress.diff(canonicalStart);
        Assert.that(delta.toPrimitive() < size, "address is not within this object memory");
        return start.addOffset(delta);
    }

    /*---------------------------------------------------------------------------*\
     *                                  Relocation                               *
    \*---------------------------------------------------------------------------*/

    /**
     * An error thrown during relocation to indicate that the buffer containing the pointers
     * being relocated has moved due to a garbage collection.
     */
    static class GCDuringRelocationError extends Error {
    }

    /**
     * Relocate all the pointers in a range of memory that point to one or more parent object memories.
     *
     * @param uri         the URI of the source memory whose pointers are being relocated
     * @param startBuffer the object containing the source memory if it is in a RAM buffer otherwise null
     * @param start       the start address of the source memory
     * @param oopMap      describes where all the not yet relocated pointers in the source memory are. The bits are
     *                    are cleared as the corresponding pointers are relocated.
     * @param parent      the direct parent memory of the given memory
     * @param toCanonical specifies the direction of the relocation
     * @param requiresEndianSwap  specifies if the endianess of the pointers differs from the endianess of the platform
     * @param trace       enables tracing if true
     */
    public static void relocateParents(String uri,
                                       Object startBuffer,
                                       Address start,
                                       BitSet oopMap,
                                       ObjectMemory parent,
                                       boolean toCanonical,
                                       boolean requiresEndianSwap,
                                       boolean trace)
    {
        while (parent != null) {
            relocate(uri,
                     startBuffer,
                     start,
                     oopMap,
                     parent.getStart(),
                     parent.getCanonicalStart(),
                     parent.getSize(),
                     toCanonical,
                     requiresEndianSwap,
                     trace,
                     false);
            parent = parent.getParent();
        }
    }

    /**
     * Relocates the pointers in a range of memory that point to some target range of memory.
     * If <code>toCanonical</code> is true, then the pointers are currently relative to the
     * real address of the target memory and are adjusted to be relative to its canonical address.
     * Otherwise, the pointers are currently relative to the canonical address of the target memory
     * and are adjusted to be relative to its real address.
     *
     * @param uri               the URI of the source memory whose pointers are being relocated
     * @param sourceStartBuffer the object containing the source memory if it is in a RAM buffer otherwise null
     * @param sourceStart       the start address of the source memory
     * @param oopMap            describes where all the not yet relocated pointers in the source memory are. The bits are
     *                          are cleared as the corresponding pointers are relocated.
     * @param targetStart       the real start address of the target space
     * @param targetCanonicalStart  the canonical start address of the target space
     * @param targetSize        the size of the target space
     * @param toCanonical       specifies the direction of the relocation
     * @param requiresEndianSwap  specifies if the endianess of the pointers differs from the endianess of the platform
     * @param trace             enables tracing if true
     * @param verifyClearOopMap if true, this routine will verify that the oop map is empty before returning
     *
     * @throws GCDuringRelocationError if 'sourceStartBuffer' is not null and some point has a different value than
     *                          'sourceStart' which implies that a garbage collection occurred
     */
    public static void relocate(String uri,
                                Object sourceStartBuffer,
                                Address sourceStart,
                                BitSet oopMap,
                                Address targetStart,
                                Address targetCanonicalStart,
                                int targetSize,
                                boolean toCanonical,
                                boolean requiresEndianSwap,
                                boolean trace,
                                boolean verifyClearOopMap) throws GCDuringRelocationError
    {
        final Address start;
        Address pointerAddress;
        Address pointer;
        final Offset delta;
        
        if (toCanonical) {
            start = targetStart;
            delta = targetCanonicalStart.diff(targetStart);
        } else {
            start = targetCanonicalStart;
            delta = targetStart.diff(targetCanonicalStart);
        }
        final Address end = start.add(targetSize);
        
        if (Klass.TRACING_ENABLED && trace) {
            VM.print("Relocating pointers from ");
            VM.print(uri);
            VM.print("  into range ( ");
            VM.printAddress(start);
            VM.print(" .. ");
            VM.printAddress(end);
            VM.println("] :");
        }

        for (int offset = oopMap.nextSetBit(0); offset != -1; offset = oopMap.nextSetBit(offset + 1)) {
            if (sourceStartBuffer != null) {
                if (!VM.isHosted() && sourceStartBuffer != sourceStart.toObject()) {
                    throw new GCDuringRelocationError();
                }
            }
            
            pointerAddress = sourceStart.add(offset * HDR.BYTES_PER_WORD);
            pointer = Address.fromObject(NativeUnsafe.getObject(pointerAddress, 0));
            if (pointer.isZero()) {
                oopMap.clear(offset);
            } else {
                // Endian swap must be done here for pointers
                if (requiresEndianSwap) {
                    NativeUnsafe.swap(pointerAddress, HDR.BYTES_PER_WORD);
                    pointer = Address.fromObject(NativeUnsafe.getObject(pointerAddress, 0));
                }
                
                if ((pointer.hi(start) && pointer.loeq(end))) {
            		Address relocatedPointer = pointer.addOffset(delta);
            		NativeUnsafe.setAddress(pointerAddress, 0, relocatedPointer);
            	
                    if (Klass.TRACING_ENABLED && trace) {
                        VM.print("  relocated pointer @ ");
                        VM.printAddress(pointerAddress);
                        VM.print(" [offset ");
                        VM.print(offset * HDR.BYTES_PER_WORD);
                        VM.print("] from ");
                        VM.printAddress(pointer);
                        VM.print(" to ");
                        VM.printAddress(relocatedPointer);
                        VM.println();
                    }
                    oopMap.clear(offset);
                }

                // Swap back the endianness
                if (requiresEndianSwap) {
                    NativeUnsafe.swap(pointerAddress, HDR.BYTES_PER_WORD);
                }
            }
        }

/*if[DEBUG_CODE_ENABLED]*/ 
        // This is used to debug issues with isolate migration that seem to keep
        // popping up on the Sun SPOT platform.
        if (verifyClearOopMap && oopMap.cardinality() != 0) {
            VM.println("These pointers were not relocated during object memory (de)serialization:");
            for (int offset = oopMap.nextSetBit(0); offset != -1; offset = oopMap.nextSetBit(offset + 1)) {
                pointerAddress = sourceStart.add(offset * HDR.BYTES_PER_WORD);

                // Endian swap must be done here for pointers
                if (requiresEndianSwap) {
                    NativeUnsafe.swap(pointerAddress, HDR.BYTES_PER_WORD);
                }

                pointer = Address.fromObject(NativeUnsafe.getObject(pointerAddress, 0));
                VM.print("  @ ");
                VM.printAddress(pointerAddress);
                VM.print(" [offset ");
                VM.print(offset * HDR.BYTES_PER_WORD);
                VM.print("] = ");
                VM.printAddress(pointer);
                VM.println();


                // Swap back the endianness
                if (requiresEndianSwap) {
                    NativeUnsafe.swap(pointerAddress, HDR.BYTES_PER_WORD);
                }
            }
            throw new RuntimeException();
           // VM.fatalVMError();
        }
/*end[DEBUG_CODE_ENABLED]*/
    }
}
