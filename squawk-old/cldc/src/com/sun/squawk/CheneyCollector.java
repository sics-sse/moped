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
 * The classic two-space Cheney garbage collector.
 *
 */
public final class CheneyCollector extends GarbageCollector {

    /**
     * Specifies if the collector is being used to copy an object graph as opposed to collect garbage.
     */
    private boolean copyingObjectGraph;

    /**
     * The forwarding repair map is a sequence of {Address, UWord} pairs where the first element
     * in the pair is the address of a forwarded object and the second element is the original
     * value of the object header's class word. This map is used to undo forwarding after
     * an object graph copy has been performed.
     */
    private Address forwardingRepairMap;
    private Address forwardingRepairMapTop;

    /**
     * Start address of 'from' space.
     */
    private Address fromSpaceStartPointer;

    /**
     * End address of 'from' space.
     */
    private Address fromSpaceEndPointer;

    /**
     * Start address of 'to' space.
     */
    private Address toSpaceStartPointer;

    /**
     * End address of 'to' space.
     */
    private Address toSpaceEndPointer;

    /**
     * Address of current allocation point in 'to' space.
     */
    private Address toSpaceAllocationPointer;

    /**
     * The class of com.sun.squawk.util.SquawkHashtable.
     */
    private Klass HashTableKlass;

    /**
     * The class of byte[].
     */
    private Klass ByteArrayKlass;

    /**
     * The class of com.sun.squawk.Isolate
     */
    private Klass IsolateKlass;

/*if[DEBUG_CODE_ENABLED]*/
    /**
     * The class of import com.sun.squawk.VMThread
     */
    private Klass VMThreadKlass;

    /**
     * The class of import com.sun.squawk.ObjectMemory
     */
    private Klass ObjectMemoryKlass;
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * The isolate object (if any) currently being copied by copyObjectGraph().
     */
    private Isolate theIsolate;

    /**
     * The BitSet instance that must be updated during a graph copy operation.
     */
    private BitSet oopMap;

    /**
     * The decoder used to decode the type table of a method.
     */
    private final VMBufferDecoder decoder;

    /**
     * The timing statistics related to garbage collection.
     */
    private final Timings collectionTimings;

    /**
     * The timing statistics related to object graph copying.
     */
    private final Timings copyTimings;

    static final class Timings {
        long setup;
        long copyRoots;
        long copyNonRoots;
        long repair;
        long finalize;

        long getTotal() {
            return setup + copyRoots + copyNonRoots + repair + finalize;
        }
    }

    /**
     * Creates a CheneyCollector.
     *
     * @param ramStart       start of the RAM allocated to the VM
     * @param ramEnd         end of the RAM allocated to the VM
     */
    CheneyCollector(Address ramStart, Address ramEnd) {

        /*
         * Initialize the heap trace
         */
        if ((HEAP_TRACE || GC.GC_TRACING_SUPPORTED) && (GC.isTracing(GC.TRACE_HEAP_BEFORE_GC) || GC.isTracing(GC.TRACE_HEAP_AFTER_GC))) {
            traceHeapInitialize(ramStart, ramEnd);
        }

        decoder = Klass.DEBUG_CODE_ENABLED ? new VMBufferDecoder() : null;
        collectionTimings = new Timings();
        copyTimings = new Timings();
    }

    /**
     * {@inheritDoc}
     */
    void initialize(Address ignored, Address memoryStart, Address memoryEnd) {

        /*
         * Set the start of 'from' space.
         */
        fromSpaceStartPointer = memoryStart.roundUpToWord();

        /*
         * Calculate the semi space size.
         */
        int heapSize = memoryEnd.diff(fromSpaceStartPointer).toInt();
        int semispaceSize = GC.roundDownToWord(heapSize / 2);
        Assert.always(heapSize > 0);

        /*
         * Set the end of 'from' space.
         */
        fromSpaceEndPointer = fromSpaceStartPointer.add(semispaceSize);

        /*
         * Set up the 'to' space pointers.
         */
        toSpaceStartPointer = fromSpaceEndPointer;
        toSpaceEndPointer   = toSpaceStartPointer.add(semispaceSize);

        /*
         * Set up the current allocation point.
         */
        toSpaceAllocationPointer = toSpaceStartPointer;

        /*
         * Check that both semispaces have the same size.
         */
        Assert.that(fromSpaceEndPointer.diff(fromSpaceStartPointer).eq(toSpaceEndPointer.diff(toSpaceStartPointer)), "semi-spaces are different sizes");

        /*
         * Set the main RAM allocator to the 'to' space.
         */
        GC.setAllocationParameters(toSpaceStartPointer, toSpaceAllocationPointer, toSpaceEndPointer, toSpaceEndPointer);

        /*
         * Output trace information.
         */
        if (GC.isTracing(GC.TRACE_BASIC)) {
            traceVariables();
        }
    }

    /**
     * Returns the amount of free memory in the system. Calling the <code>gc</code>
     * method may result in increasing the value returned by <code>freeMemory.</code>
     *
     * @param  allocationPointer  the current allocationPointer
     * @return an approximation to the total amount of memory currently
     *         available for future allocated objects, measured in bytes.
     */
    long freeMemory(Address allocationPointer) {
        return toSpaceEndPointer.diff(allocationPointer).toPrimitive();
    }

    /**
     * Returns the total amount of RAM memory in the Squawk Virtual Machine. The
     * value returned by this method may vary over time, depending on the host
     * environment.
     * <p>
     * Note that the amount of memory required to hold an object of any given
     * type may be implementation-dependent.
     *
     * @return the total amount of memory currently available for current and
     *         future objects, measured in bytes.
     */
    long totalMemory() {
        return toSpaceEndPointer.diff(toSpaceStartPointer).toPrimitive();
    }

    /**
     * Sets a region of memory that must not be written to. This is only used
     * when debugging problems relating to this specific garbage collector.
     *
     * @param start   the start of the memory region to be protected
     * @param end     the end of the memory region to be protected
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="cheneyEndMemoryProtect = end; cheneyStartMemoryProtect = start;")
/*end[JAVA5SYNTAX]*/
    private static native void memoryProtect(Address start, Address end);

    /**
     * Switch over 'from' space and 'to' space.
     */
    private void toggleSpaces() {
        Address newStartPointer  = fromSpaceStartPointer;
        Address newEndPointer    = fromSpaceEndPointer;

        fromSpaceStartPointer    = toSpaceStartPointer;
        fromSpaceEndPointer      = toSpaceEndPointer;

        toSpaceStartPointer      = newStartPointer;
        toSpaceEndPointer        = newEndPointer;

        toSpaceAllocationPointer = toSpaceStartPointer;

        /*
         * Output trace information.
         */
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::toggleSpaces");
            traceVariables();
        }

    }

    /**
     * Gets the class of an object. This method takes into account that any of the
     * following may have been moved: the object itself, its class or its association.
     *
     * @param object the object address
     * @return the klass
     */
    private Klass getKlass(Address object) {
        object = getPossiblyForwardedObject(object);
        Object classWord = NativeUnsafe.getObject(object, HDR.klass);
        Address something = getPossiblyForwardedObject(classWord);
        Address klass = NativeUnsafe.getAddress(something, (int)FieldOffsets.com_sun_squawk_Klass$self);
        return VM.asKlass(getPossiblyForwardedObject(klass));
    }

    /**
     * Set the forwarding pointer of an object.
     *
     * @param object         the object pointer
     * @param forwardPointer the object forwarding pointer
     */
    private void setForwardPointer(Address object, Address forwardPointer) {
        if (copyingObjectGraph) {

            // If this fails, then there is an error in the calculated required minimum
            // size of the forwarding repair map
            Assert.that(fromSpaceEndPointer.diff(forwardingRepairMapTop).ge(Offset.fromPrimitive(HDR.BYTES_PER_WORD * 2)), "forwarding repair map is too small");

            UWord classWord = NativeUnsafe.getAddress(object, HDR.klass).toUWord();
            NativeUnsafe.setAddress(forwardingRepairMapTop, 0, object);
            NativeUnsafe.setUWord(forwardingRepairMapTop, 1, classWord);
            forwardingRepairMapTop = forwardingRepairMapTop.add(HDR.BYTES_PER_WORD * 2);

            NativeUnsafe.setAddress(object, HDR.klass, forwardPointer.or(UWord.fromPrimitive(HDR.forwardPointerBit)));
        } else {
            memoryProtect(Address.zero(), Address.zero());
            NativeUnsafe.setAddress(object, HDR.klass, forwardPointer.or(UWord.fromPrimitive(HDR.forwardPointerBit)));
            memoryProtect(fromSpaceStartPointer, fromSpaceEndPointer);
        }
    }

    /**
     * Get the forwarded copy of an object.
     *
     * @param  object  the pointer to an object that has been forwarded
     * @return the forwarding pointer
     */
    private static Address getForwardedObject(Address object) {
        Address classWord = NativeUnsafe.getAddress(object, HDR.klass);
        Assert.that(classWord.and(UWord.fromPrimitive(HDR.forwardPointerBit)).ne(Address.zero()), "object is not forwarded");
        return classWord.and(UWord.fromPrimitive(~HDR.headerTagMask));
    }

    /**
     * Get the forwarding pointer of an object.
     *
     * @param object   the object pointer
     * @return the forwarding pointer
     */
    private Address getPossiblyForwardedObject(Address object) {
        if (isForwarded(object)) {
            return getForwardedObject(object);
        } else {
            return object;
        }
    }

    /**
     * Get the forwarding pointer of an object.
     *
     * @param object   the object pointer
     * @return the forwarding pointer
     */
    private Address getPossiblyForwardedObject(Object object) {
        return getPossiblyForwardedObject(Address.fromObject(object));
    }

    /**
     * Test to see if an object is in 'from' space.
     *
     * @param pointer the object pointer
     * @return true if it is
     */
    private boolean isInFromSpace(Address pointer) {
        return fromSpaceStartPointer.loeq(pointer) && pointer.lo(fromSpaceEndPointer);
    }

    /**
     * Test to see if an object is in 'to' space.
     *
     * @param pointer the object pointer
     * @return true if it is
     */
    private boolean isInToSpace(Address pointer) {
        return toSpaceStartPointer.loeq(pointer) && pointer.lo(toSpaceEndPointer);
    }

    /**
     * Tests the class pointer word of an object to see if it is a forwarding pointer.
     *
     * @param object   the object to test
     * @return boolean true if object has been forwarded
     */
    private boolean isForwarded(Address object) {
        UWord classWord = NativeUnsafe.getAddress(object, HDR.klass).toUWord();
        return classWord.and(UWord.fromPrimitive(HDR.forwardPointerBit)).ne(UWord.zero());
    }

    /**
     * Copy an object from the 'from' space to the 'to' space. If the object
     * reference supplied is not in the 'from' space the object is not copied
     * and the address of the supplied object is returned. If the object reference
     * points to an object that has already been copied to the 'to' space then
     * the address of the already copied object is returned.
     *
     * @param object the object to copy
     * @return the address of the copied object
     */
    private Address copyObject(final Address object) {

        /*
         * Check that the pointer is word aligned.
         */
        Assert.that(object.roundUpToWord().eq(object), "cannot copy unaligned object");

        /*
         * Trace.
         */
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print("CheneyCollector::copyObject - object = ");
            VM.printAddress(object);
        }

        /*
         * Deal with null pointers.
         */
        if (object.isZero()) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println(" is null");
            }
            return null;
        }

        /*
         * Check to see if the pointer is in the space we are collecting.
         */
        if (!isInFromSpace(object)) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print(" klass = ");
                VM.print(getKlass(object).getInternalName());
                VM.println(" Not in from space ");
            }
            return object;
        }

        /*
         * Certain objects must never be copied.
         */
        Assert.that(object.toObject() != this, "cannot copy the CheneyCollector object");

        /*
         * Check to see if the pointer is in the space we are collecting.
         */
        if (isForwarded(object)) {
            Address forwardPointer = getForwardedObject(object);
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print(" klass = ");
                VM.print(getKlass(forwardPointer).getInternalName());
                VM.print(" already forwarded to ");
                VM.printAddress(forwardPointer);
                VM.println();
            }
            Assert.that(forwardPointer.roundUpToWord().eq(forwardPointer), "unaligned forward pointer");
            return forwardPointer;
        }

        /*
         * Copy the object.
         */
        Address block  = GC.oopToBlock(getKlass(object), object);
        int headerSize = object.diff(block).toInt();
        int blockSize  = headerSize + GC.getBodySize(getKlass(object), object);
        Assert.that(GC.blockToOop(block).eq(object), "mis-sized header for copied object");
        VM.copyBytes(block, 0, toSpaceAllocationPointer, 0, blockSize, false);
/*if[TYPEMAP]*/
        if (VM.usingTypeMap()) {
            NativeUnsafe.copyTypes(block, toSpaceAllocationPointer, blockSize);
        }
/*end[TYPEMAP]*/
        Address copiedObject = toSpaceAllocationPointer.add(headerSize);
        Assert.that(copiedObject.roundUpToWord().eq(copiedObject), "unaligned copied object");
        toSpaceAllocationPointer = toSpaceAllocationPointer.add(blockSize);
        setForwardPointer(object, copiedObject);

        /*
         * Trace.
         */
        Klass klass = getKlass(copiedObject);
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print(" blockSize = ");
            VM.print(blockSize);
            VM.print(" klass = ");
            VM.print(Klass.getInternalName(klass));
            VM.print(" copied to ");
            VM.printAddress(copiedObject);
            VM.println();
        }

        /*
         * If the object is a stack chunk then update all the frame pointers.
         */
        if (Klass.getSystemID(klass) == CID.LOCAL_ARRAY) {
            updateStackChunkInternalPointers(object, copiedObject);
        }

/*if[DEBUG_CODE_ENABLED]*/
        /*
         * Run some assertions that will hopefully ensure isolation
         */
        if (copyingObjectGraph) {
            assertIsolation(object, klass);
        }
/*end[DEBUG_CODE_ENABLED]*/

/*if[!FLASH_MEMORY]*/
        if (VM.isVerbose() && Klass.getSystemID(klass) == CID.BYTECODE_ARRAY) {
            int old = VM.setStream(VM.STREAM_SYMBOLS);
            if (!copyingObjectGraph) {
                VM.print("METHOD.");
                VM.printAddress(copiedObject);
                VM.print(".MOVED_FROM=");
                VM.printAddress(object);
                VM.println();
            } else {
                VM.print("SAVED_METHOD.");
                VM.printOffset(copiedObject.diff(toSpaceStartPointer));
                VM.print(".COPIED_FROM=");
                VM.printAddress(object);
                VM.println();
            }
            VM.setStream(old);
        }
/*end[FLASH_MEMORY]*/        

        /*
         * Return the new object pointer.
         */
        return copiedObject;
    }

/*if[DEBUG_CODE_ENABLED]*/
    /**
     * Determines if a given object address does not break isolation. This is not
     * a comprehensive test but should catch most errors.
     *
     * @param object   the object address to test
     * @param klass    the class of the object
     */
    private void assertIsolation(Address object, Klass klass) {

        Address global;

        // Ensure that the object does not coincides with the value of a global reference
        for (int i = 0 ; i < VM.getGlobalOopCount() ; i++) {
            global = Address.fromObject(VM.getGlobalOop(i));
            if (global.eq(object)) {
                VM.print("cannot copy value shared with global '");
                VM.printGlobalOopName(i);
                VM.println("' -- most likely caused by a local variable in a method in com.sun.squawk.VMThread pointing to a global");
                Assert.shouldNotReachHere();
            }
        }

        Assert.that(klass != ObjectMemoryKlass, "cannot copy an ObjectMemory instance");

        if (theIsolate != null) {
            if (Klass.isSubtypeOf(klass, VMThreadKlass)) {
                Assert.that(NativeUnsafe.getObject(object, (int)FieldOffsets.com_sun_squawk_VMThread$isolate) == theIsolate,
                            "cannot copy thread from another isolate");
            } else if (Klass.isSubtypeOf(klass, IsolateKlass)) {
                Assert.that(object.toObject() == theIsolate, "cannot copy another isolate");
            } else if (Klass.getSystemID(klass) == CID.LOCAL_ARRAY) {
                Object thread = NativeUnsafe.getObject(object, SC.owner);
                Assert.that(thread == null || NativeUnsafe.getObject(thread, (int)FieldOffsets.com_sun_squawk_VMThread$isolate) == theIsolate,
                            "cannot copy stack chunk from another isolate");
            }
        }
    }
/*end[DEBUG_CODE_ENABLED]*/

    /**
     * Update an object reference in an object copying the object referred to
     * from 'from' space to 'to' space if necessary.
     *
     * @param base the base address of the object holding the reference
     * @param offset the offset (in words) from 'base' of the reference
     */
    private Address updateReference(Address base, int offset) {
        Assert.that(base.roundUpToWord().eq(base), "unaligned base address");
        Address oldObject = NativeUnsafe.getAddress(base, offset);
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print("CheneyCollector::updateReference - [");
            VM.printAddress(base);
            VM.print("%");
            VM.print(offset);
            VM.print("] = ");
            VM.printAddress(oldObject);
            VM.println();
        }
        Address newObject = copyObject(oldObject);
        if (newObject.ne(oldObject)) {
            NativeUnsafe.setAddress(base, offset, newObject);
        }

        if (copyingObjectGraph) {
            // Update the oop map
            Address pointerAddress = base.add(offset * HDR.BYTES_PER_WORD);
            recordPointer(pointerAddress);
        }

        return newObject;
    }

    /**
     * Update the pointers that point to within a stack chunk. These are the pointers from one frame to
     * its return frame (i.e. the pointers at offset {@link FP#returnFP} within each frame).
     *
     * @param oldChunk the old stack chunk
     * @param newChunk the new stack chunk
     */
    private void updateStackChunkInternalPointers(Address oldChunk, Address newChunk) {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::updateStackChunkInternalPointers");
        }

        /*
         * Update the frame pointers.
         */
        Offset offsetToPointer = Offset.fromPrimitive(SC.lastFP).wordsToBytes();
        Address oldFP = NativeUnsafe.getAddress(oldChunk.addOffset(offsetToPointer), 0);
        while (!oldFP.isZero()) {
            Address newPointerAddress = newChunk.addOffset(offsetToPointer);
            Offset delta = oldFP.diff(oldChunk);
            Address newFP = newChunk.addOffset(delta);
            NativeUnsafe.setAddress(newPointerAddress, 0, newFP);
            if (copyingObjectGraph) {
                recordPointer(newPointerAddress);
            }

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("CheneyCollector::updateStackChunkInternalPointers offset = ");
                VM.printOffset(offsetToPointer);
                VM.print(" oldFP = ");
                VM.printAddress(oldFP);
                VM.print(" newFP = ");
                VM.printAddress(newFP);
                VM.println();
            }

            offsetToPointer = delta.add(FP.returnFP * HDR.BYTES_PER_WORD);
            oldFP = NativeUnsafe.getAddress(oldChunk.addOffset(offsetToPointer), 0);
        }
    }


    /**
     * Update an activation record.
     *
     * @param fp                     the frame pointer
     * @param isInnerMostActivation  specifies if this is the inner most activation frame on the chunk
     *                               in which case only the first local variable (i.e. the method pointer) is scanned
     */
    private void updateActivation(Address fp, boolean isInnerMostActivation) {
        Address mp  = NativeUnsafe.getAddress(fp, FP.method);
        Address previousFP = VM.getPreviousFP(fp);
        Address previousIP = VM.getPreviousIP(fp);

        /*
         * Trace.
         */
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print("CheneyCollector::updateActivation fp = ");
            VM.printAddress(fp);
            VM.print(" mp = ");
            VM.printAddress(mp);
            VM.println();
        }

        /*
         * Adjust the previous IP and MP.
         */
        if (!previousIP.isZero()) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println("CheneyCollector::updateActivation -- change previous MP");
            }

            /*
             * Adjust the MP
             */
            Assert.that(!previousFP.isZero(), "activation frame has null previousFP");
            Address oldPreviousMP = NativeUnsafe.getAddress(previousFP, FP.method);
            Assert.that(!isInToSpace(oldPreviousMP), "a method was copied before activation frame of the method");
            Address newPreviousMP = updateReference(previousFP, FP.method);

            /*
             * Adjust the IP
             */
            Offset delta = newPreviousMP.diff(oldPreviousMP);
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println("CheneyCollector::updateActivation -- change previous IP");
                VM.print("        oldPreviousMP = ");
                VM.printAddress(oldPreviousMP);
                VM.print(" oldPreviousIP = ");
                VM.printAddress(previousIP);
                VM.print(" newPreviousMP = ");
                VM.printAddress(newPreviousMP);
                VM.print(" delta = ");
                VM.printOffset(delta);
                VM.print(" newPreviousIP = ");
                VM.printAddress(previousIP.addOffset(delta));
                VM.println("");
            }
            previousIP = previousIP.addOffset(delta);
            VM.setPreviousIP(fp, previousIP);

            /*
             * Record the pointer if graph copying.
             */
            if (copyingObjectGraph) {
                Address pointerAddress = fp.add(FP.returnIP * HDR.BYTES_PER_WORD);
                recordPointer(pointerAddress);
            }

        } else {
            Assert.that(previousFP.isZero(), "previousFP should be null when previousIP is null");
        }

        /*
         * Get the method pointer and setup to go through the parameters and locals.
         */
        int localCount     = isInnerMostActivation ? 1 : MethodHeader.decodeLocalCount(mp.toObject());
        int parameterCount = MethodHeader.decodeParameterCount(mp.toObject());
        int mapOffset      = MethodHeader.decodeOopmapOffset(mp.toObject());
        int bitOffset      = -1;
        int byteOffset     = 0;

        /*
         * Parameters.
         */
        int varOffset = FP.parm0;
        while (parameterCount-- > 0) {
            bitOffset++;
            if (bitOffset == 8) {
                bitOffset = 0;
                byteOffset++;
            }
            int bite = NativeUnsafe.getByte(mp, mapOffset+byteOffset);
            boolean isOop = ((bite>>bitOffset)&1) != 0;
            if (isOop) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("CheneyCollector::updateActivation -- update parm at offset ");
                    VM.print(varOffset);
                    VM.println();
                }
                updateReference(fp, varOffset);
            }
            varOffset++; // Parameters go upwards
        }

        /*
         * Locals.
         */
        varOffset = FP.local0;
        while (localCount-- > 0) {
            bitOffset++;
            if (bitOffset == 8) {
                bitOffset = 0;
                byteOffset++;
            }
            int bite = NativeUnsafe.getByte(mp, mapOffset + byteOffset);
            boolean isOop = ((bite >> bitOffset) & 1) != 0;
            if (isOop) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("CheneyCollector::updateActivation -- update local at offset ");
                    VM.print(varOffset);
                    VM.println();
                }
                updateReference(fp, varOffset);
            }
            varOffset--; // Locals go downwards
        }
    }

    /**
     * Determines if the value of any Address-typed slots in an activation frame indicate
     * an address within the heap.
     *
     * @param fp                     the frame pointer
     * @param isInnerMostActivation  specifies if this is the inner most activation frame on the chunk
     *                               in which case no local variables are scanned
     */
    private void checkActivationForAddresses(Address fp, boolean isInnerMostActivation) {
        Address mp  = NativeUnsafe.getAddress(fp, FP.method);

        /*
         * Trace.
         */
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print("CheneyCollector::checkActivationForAddresses fp = ");
            VM.printAddress(fp);
            VM.print(" mp = ");
            VM.printAddress(mp);
            VM.println();
        }

        /*
         * Get the method pointer and setup to go through the parameters and locals.
         */
        int localCount      = isInnerMostActivation ? 0 : MethodHeader.decodeLocalCount(mp.toObject());
        int parameterCount  = MethodHeader.decodeParameterCount(mp.toObject());
        int typeTableSize   = MethodHeader.decodeTypeTableSize(mp.toObject());
        int typeTableOffset = MethodHeader.decodeTypeTableOffset(mp.toObject());

        if (typeTableSize > 0) {
            decoder.reset(mp.toObject(), typeTableOffset);
            int typeTableEndOffset = typeTableOffset + typeTableSize;
            while (decoder.getOffset() < typeTableEndOffset) {
                int cid  = decoder.readUnsignedInt();
                int slot = decoder.readUnsignedInt();
                if (cid == CID.ADDRESS) {
                    if (slot < parameterCount) {
                        int varOffset = FP.parm0 + slot; // parameters go upward
                        Address value = NativeUnsafe.getAddress(fp, varOffset);
                        if (value.hieq(fromSpaceStartPointer) && value.loeq(fromSpaceEndPointer)) {
                            VM.print("**WARNING**: parameter ");
                            VM.print(slot);
                            VM.print(" of type Address points into the heap: mp = ");
                            VM.printAddress(mp);
                            VM.println();
                        }
                    } else {
                        int local = slot - parameterCount;
                        if (local < localCount) {
                            int varOffset = FP.local0 - local; // locals go downward
                            Address value = NativeUnsafe.getAddress(fp, varOffset);
                            if (value.hieq(fromSpaceStartPointer) && value.loeq(fromSpaceEndPointer)) {
                                VM.print("**WARNING**: local ");
                                VM.print(local);
                                VM.print(" of type Address points into the heap: mp = ");
                                VM.printAddress(mp);
                                VM.println();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Update the references in a stack chunk.
     *
     * @param chunk the stack chunk.
     */
    private void updateStackChunk(Address chunk) {
        Address fp = NativeUnsafe.getAddress(chunk, SC.lastFP);

        /*
         * Trace.
         */
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println();
            VM.print("CheneyCollector::updateStackChunk chunk = ");
            VM.printAddress(chunk);
            VM.println();
        }

        /*
         * Update the pointers in the header part of the stack chunk
         */
        Assert.always(NativeUnsafe.getAddress(chunk, SC.next).isZero());
        updateReference(chunk, SC.owner);

        /*
         * Update the pointers in each activation frame
         */
        boolean isInnerMostActivation = true;
        while (!fp.isZero()) {
            updateActivation(fp, isInnerMostActivation);
            if (Klass.ASSERTIONS_ENABLED) {
                checkActivationForAddresses(fp, isInnerMostActivation);
            }
            fp = VM.getPreviousFP(fp);
            isInnerMostActivation = false;
        }
    }

    /**
     * Update the object references in the root objects copying the objects referred to
     * from 'from' space to 'to' space as necessary.
     */
    private void copyRootObjects() {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::copyRootObjects --------------- Start");
        }
        for (int i = 0 ; i < VM.getGlobalOopCount() ; i++) {
            Address oldObject = Address.fromObject(VM.getGlobalOop(i));
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("CheneyCollector::copyRootObjects index = ");
                VM.print(i);
                VM.print(" object = ");
                VM.printAddress(oldObject);
                VM.println();
            }
            Address newObject = copyObject(oldObject);
            if (newObject.ne(oldObject)) {
                VM.setGlobalOop(newObject.toObject(), i);
            }
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::copyRootObjects --------------- End");
        }
    }

    /**
     * Update the object references in an object copying the objects referred to
     * from 'from' space to 'to' space as necessary.
     *
     * @param object the object in 'to' space
     */
    private void updateOops(Address object) {
        Address associationOrKlass = updateReference(object, HDR.klass);
        Klass klass = VM.asKlass(updateReference(associationOrKlass, (int)FieldOffsets.com_sun_squawk_Klass$self));
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print("CheneyCollector::updateOops object = ");
            VM.printAddress(object);
            VM.print(" klass = ");
            VM.println(Klass.getInternalName(klass));
        }
        if (Klass.isSquawkArray(klass)) {
            switch (Klass.getSystemID(klass)) {
                case CID.BOOLEAN_ARRAY:
                case CID.BYTE_ARRAY:
                case CID.CHAR_ARRAY:
                case CID.DOUBLE_ARRAY:
                case CID.FLOAT_ARRAY:
                case CID.INT_ARRAY:
                case CID.LONG_ARRAY:
                case CID.SHORT_ARRAY:
                case CID.UWORD_ARRAY:
                case CID.ADDRESS_ARRAY:
                case CID.STRING:
                case CID.STRING_OF_BYTES: {
                    break;
                }
                case CID.BYTECODE_ARRAY: {
                    updateReference(object, HDR.methodDefiningClass);
                    break;
                }
                case CID.GLOBAL_ARRAY: {
                    Klass gaklass = VM.asKlass(NativeUnsafe.getObject(object, CS.klass));
                    if (gaklass == null) { // This can occur if a GC occurs when a class state is being allocated
                        Assert.that(NativeUnsafe.getObject(object, CS.next) == null); // The 'next' pointer should not yet be set either
                        if (GC.GC_TRACING_SUPPORTED && tracing()) {
                            VM.println("CheneyCollector::updateOops GLOBAL_ARRAY with null CS.klass not scanned");
                        }
                    } else {
                        if (GC.GC_TRACING_SUPPORTED && tracing()) {
                            VM.print("CheneyCollector::updateOops globals for ");
                            VM.println(Klass.getInternalName(gaklass));
                        }
                        int end = CS.firstVariable + Klass.getRefStaticFieldsSize(gaklass);
                        for (int i = 0 ; i < end ; i++) {
                            Assert.that(i < GC.getArrayLength(object), "class state index out of bounds");
                            updateReference(object, i);
                        }
                    }
                    break;
                }
                case CID.LOCAL_ARRAY: {
                    updateStackChunk(object);
                    break;
                }
                default: { // Pointer array
                    int length = GC.getArrayLength(object);
                    for (int i = 0; i < length; i++) {
                        updateReference(object, i);
                    }
                    break;
                }
            }

        } else { // Instance

            /*
             * If the object is a hashtable and we are doing a graph copy for hibernation then
             * zero the first oop which is the transient field called entryTable.
             */
            if (copyingObjectGraph && Klass.isSubtypeOf(klass, HashTableKlass)) {
                NativeUnsafe.setAddress(object, (int)FieldOffsets.com_sun_squawk_util_SquawkHashtable$entryTable, Address.zero());
            }

            /*
             * Update the oops
             */
            int nWords = Klass.getInstanceSize(klass);
            for (int i = 0 ; i < nWords ; i++) {
                if (Klass.isInstanceWordReference(klass, i)) {
                    updateReference(object, i);
                }
            }
        }
    }

    /**
     * Update the object references in all the objects currently in the 'to' space
     * copying the objects referred to from 'from' space to 'to' space as necessary.
     *
     * @param   toSpaceUpdatePointer  the address in 'to' space at which to start processing
     * @return  the address in 'to' space at which the next call to this method should start processing
     */
    private Address copyNonRootObjects(Address toSpaceUpdatePointer) {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::copyNonRootObjects --------------- Start");
        }
        while (toSpaceUpdatePointer.lo(toSpaceAllocationPointer)) {
            Address object = GC.blockToOop(toSpaceUpdatePointer);
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println();
                VM.print("CheneyCollector::copyNonRootObjects block = ");
                VM.printAddress(toSpaceUpdatePointer);
                VM.print(" object = ");
                VM.printAddress(object);
                VM.print(" klass = ");
                VM.println(Klass.getInternalName(getKlass(object)));
            }
            Klass klass = getKlass(object);
            Assert.that(GC.oopToBlock(klass, object).eq(toSpaceUpdatePointer), "bad size for copied object");
            updateOops(object);
            toSpaceUpdatePointer = object.add(GC.getBodySize(klass, object));
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::copyNonRootObjects --------------- End");
        }
        return toSpaceUpdatePointer;
    }

/*if[FINALIZATION]*/
    /**
     * Processes the finalizer queue. A finalizer for an object that is now dead (i.e. not forwarded to 'toSpace')
     * is removed from the global queue of finalizers and added to the per-isolate queue of finalizers pending
     * execution.
     *
     * @param toSpaceUpdatePointer  the address in 'to' space where finalizers should be copied
     * @return the new value of <code>toSpaceUpdatePointer</code>
     */
    private Address processFinalizerQueue(Address toSpaceUpdatePointer) {

        Finalizer entry = finalizers;
        finalizers = null;

        while (entry != null) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println("CheneyCollector::processFinalizerQueue -- processing finalizer ");
            }
            Assert.that(!isInToSpace(getPossiblyForwardedObject(Address.fromObject(entry))), "finalizer for object copied prematurely");
            Finalizer next = entry.getNext();
            Address object = Address.fromObject(entry.getObject());
            boolean referenced = isInToSpace(getPossiblyForwardedObject(object));

            entry = (Finalizer)copyObject(Address.fromObject(entry)).toObject();
            entry.setNext(null);
            toSpaceUpdatePointer = copyNonRootObjects(toSpaceUpdatePointer);

            if (referenced) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("CheneyCollector::processFinalizerQueue -- requeue ");
                    VM.printAddress(entry);
                    VM.print(" klass ");
                    VM.print(getKlass(Address.fromObject(entry.getObject())).getInternalName());
                    VM.println();
                }
                addFinalizer(entry);        // Requeue the finalizer back on the collecor.
            } else {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("CheneyCollector::processFinalizerQueue -- release ");
                    VM.printAddress(entry);
                    VM.print(" klass ");
                    VM.print(getKlass(Address.fromObject(entry.getObject())).getInternalName());
                    VM.print(" queued to isolate for class ");
                    VM.print(entry.getIsolate().getMainClassName());
                    VM.println();
                }
                entry.queueToIsolate();     // Queue for execution when the isolate is next preempted.
            }
            entry = next;
        }

        return toSpaceUpdatePointer;
    }
/*end[FINALIZATION]*/

    /**
     * Processes the weak reference queue. A weak reference to an object that is now dead should
     * be cleared, while a weak reference to an object that is still live should be updated to reflect
     * the new location of the object.
     */
    private void processWeakReferenceQueue() {

        Ref ref = references;
        references = null;

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::processWeakReferenceQueue -- start ");
        }

        while (ref != null) {

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("CheneyCollector::processWeakReferenceQueue -- processing weak reference @ ");
                VM.printAddress(ref);
                VM.print(" to ");
                VM.printAddress(ref.referent);
                VM.println();
            }

            Address refAddress = Address.fromObject(ref);
            Assert.that(isInFromSpace(refAddress));
            Ref next = ref.next;

            memoryProtect(Address.zero(), Address.zero());

            if (isForwarded(refAddress)) {
                // The Ref object is reachable

                refAddress = getForwardedObject(refAddress);
                ref = (Ref)refAddress.toObject();

                Address referent = ref.referent;
                if (!referent.isZero()) {
                    referent = getPossiblyForwardedObject(referent);
                    if (!isInFromSpace(referent)) {
                        // The referent is reachable
                        ref.referent = referent;
                        addWeakReference(ref);

                        if (GC.GC_TRACING_SUPPORTED && tracing()) {
                            VM.print("CheneyCollector::processWeakReferenceQueue -- kept weak reference and updated referent to ");
                            VM.printAddress(referent);
                            VM.println();
                        }
                    } else {
                        // The referent is unreachable
                        ref.referent = Address.zero();

                        if (GC.GC_TRACING_SUPPORTED && tracing()) {
                            VM.println("CheneyCollector::processWeakReferenceQueue -- discarded weak reference and cleared referent");
                        }
                    }
                }
            } else {
                // The Ref object is unreachable

                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.println("CheneyCollector::processWeakReferenceQueue -- discarded weak reference");
                }
            }
            memoryProtect(fromSpaceStartPointer, fromSpaceEndPointer);

            ref = next;
        }

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("CheneyCollector::processWeakReferenceQueue -- end");
        }
    }

    /**
     * Debuging aid. Fill from space with the 0xDEADBEEF pattern.
     */
    private void clearFromSpace() {
        VM.deadbeef(fromSpaceStartPointer, fromSpaceEndPointer);
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c(root="collectGarbage")
/*end[JAVA5SYNTAX]*/
    boolean collectGarbageInJava(Address allocTop, boolean forceFullGC) {

        long start = now();

        // Output heap trace
        if ((HEAP_TRACE || GC.GC_TRACING_SUPPORTED) && GC.isTracing(GC.TRACE_HEAP_BEFORE_GC)) {
            traceHeap("Before collection", allocTop);
        }
        this.numBytesLastScanned = allocTop.diff(toSpaceStartPointer).toPrimitive();
        
        // Switch semi-spaces
        toggleSpaces();

        // Set the from space to be read-only
        memoryProtect(fromSpaceStartPointer, fromSpaceEndPointer);

        collectionTimings.setup += now() - start;

        // Copy all the reachable objects.
        start = now();
        copyRootObjects();
        collectionTimings.copyRoots =+ now() - start;

        start = now();

        Address toSpaceUpdatePointer = copyNonRootObjects(toSpaceStartPointer);

        collectionTimings.copyNonRoots += now() - start;
        start = now();

/*if[FINALIZATION]*/
        // Process the finalizer queue
        processFinalizerQueue(toSpaceUpdatePointer);
/*end[FINALIZATION]*/

        // Process weak reference queue
        processWeakReferenceQueue();

        // Set the from space to be read-write
        memoryProtect(Address.zero(), Address.zero());

        // Fill from space with the 0xDEADBEEF pattern.
        clearFromSpace();

        // Set the main RAM allocator to the 'to' space.
        GC.setAllocationParameters(toSpaceStartPointer, toSpaceAllocationPointer, toSpaceEndPointer, toSpaceEndPointer);

        // Output heap trace
        if ((HEAP_TRACE || GC.GC_TRACING_SUPPORTED) && GC.isTracing(GC.TRACE_HEAP_AFTER_GC)) {
            traceHeap("After collection", toSpaceAllocationPointer);
        }

        collectionTimings.finalize += now() - start;

        // The Cheney collector always collects the full heap
        return true;
    }

    /**
     * {@inheritDoc}
     */
    void postCollection() {

    }

    private static int percent(long part, long total) {
        return (total == 0 ? 0 : (int)((part * 100) / total));
    }

    private void dumpTiming(java.io.PrintStream out, String label, long value, long total) {
        out.println(label + value + timerUnitSuffix() + " [" + percent(value, total) + "%]");
    }


    /**
     * {@inheritDoc}
     */
    void dumpTimings(java.io.PrintStream out) {

        Timings timings = collectionTimings;
        long total = timings.getTotal();
        int count = GC.getTotalCount();
        if (count != 0) {
            out.println("Collection: [average = " + (total/count) + timerUnitSuffix() +"]");
            dumpTiming(out, "    setup:        ", timings.setup, total);
            dumpTiming(out, "    copyRoots:    ", timings.copyRoots, total);
            dumpTiming(out, "    copyNonRoots: ", timings.copyNonRoots, total);
            dumpTiming(out, "    finalize:     ", timings.finalize, total);
        }

        timings = copyTimings;
        total = timings.getTotal();
        out.println("Copying:");
        dumpTiming(out, "    setup:        ", timings.setup, total);
        dumpTiming(out, "    copyNonRoots: ", timings.copyNonRoots, total);
        dumpTiming(out, "    repair:       ", timings.repair, total);
        dumpTiming(out, "    finalize:     ", timings.finalize, total);

    }

    /*---------------------------------------------------------------------------*\
     *                          Object graph copying                             *
    \*---------------------------------------------------------------------------*/

/*if[JAVA5SYNTAX]*/
    @Vm2c(root="copyObjectGraph")
/*end[JAVA5SYNTAX]*/
    Address copyObjectGraphInJava(Address object, ObjectMemorySerializer.ControlBlock cb, Address allocTop) {

        // Get the special classes if this is the first time a copy is being performed
        if (HashTableKlass == null) {

            Isolate isolate = VM.getCurrentIsolate();
            Suite bootstrapSuite = isolate.getBootstrapSuite();
            HashTableKlass = bootstrapSuite.lookup("com.sun.squawk.util.SquawkHashtable");
            ByteArrayKlass = bootstrapSuite.getKlass(CID.BYTE_ARRAY);
            IsolateKlass = GC.getKlass(isolate);

/*if[DEBUG_CODE_ENABLED]*/
            VMThreadKlass = bootstrapSuite.lookup("com.sun.squawk.VMThread");
            ObjectMemoryKlass = bootstrapSuite.lookup("com.sun.squawk.ObjectMemory");
/*end[DEBUG_CODE_ENABLED]*/
        }

        long start = now();

        // Set up the map that will be used to undo pointer forwarding
        if (!initializeForwardingRepairMap(toSpaceStartPointer, allocTop, toSpaceEndPointer)) {
            return Address.zero();
        }

        copyingObjectGraph = true;
        oopMap = cb.oopMap;

        if (GC.getKlass(object.toObject()) == IsolateKlass) {
            theIsolate = (Isolate)object.toObject();
        }

        // Switch semi-spaces
        toggleSpaces();

        copyTimings.setup += now() - start;

        // Copy all the reachable objects.
        start = now();
        object = copyObject(object);
        copyNonRootObjects(toSpaceStartPointer);
        copyTimings.copyNonRoots += now() - start;

        // Get the start and end of the serialized graph
        Address graph = toSpaceStartPointer;
        Address graphEnd = toSpaceAllocationPointer;
        Address graphCopy;

        // Toggle the spaces back
        toggleSpaces();

        // Repair the class word of the forwarded objects
        start = now();
        repairForwardedObjects();
        copyTimings.repair += now() - start;

        int graphSize = graphEnd.diff(graph).toInt();
        int freeSpace = toSpaceEndPointer.diff(allocTop).toInt() - HDR.arrayHeaderSize;

        if (graphSize <= freeSpace) {

            start = now();

            // Copy the serialized graph to the start of free memory and make it a byte array
            graphCopy = allocTop.add(HDR.basicHeaderSize);
            GC.setHeaderClass(graphCopy, ByteArrayKlass);
            GC.setHeaderLength(graphCopy, graphSize);
            VM.copyBytes(graph, 0, graphCopy, 0, graphSize, false);
/*if[TYPEMAP]*/
            if (VM.usingTypeMap()) {
                NativeUnsafe.copyTypes(graph, graphCopy, graphSize);
            }
/*end[TYPEMAP]*/
            cb.root = object.diff(graph).toInt();
            cb.start = graph;

            // Adjust the allocation parameters
            allocTop = graphCopy.add(graphSize);
            GC.setAllocationParameters(toSpaceStartPointer, allocTop, toSpaceEndPointer, toSpaceEndPointer);

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println();
                VM.print("CheneyCollector::copyObjectGraph - graph = ");
                VM.printAddress(graph);
                VM.print(" graphEnd = ");
                VM.printAddress(graphEnd);
                VM.print(" cb.size ");
                VM.print(cb.memory.length);
                VM.println();
            }

        } else {
            start = now();
            graphCopy = Address.zero();
        }

        copyingObjectGraph = false;
        theIsolate = null;
        oopMap = null;

        copyTimings.finalize += now() - start;
        return graphCopy;
    }

    /**
     * Determines the number of objects in the allocated portion of the heap.
     *
     * @param start  the first allocated object
     * @param end    the address of the byte one past the last allocated object
     * @return the number of objects between <code>[start .. end)</code>
     */
    private int countObjects(Address start, Address end) {
        int count = 0;
        for (Address block = start; block.lo(end); ) {
            ++count;
            Address object = GC.blockToOop(block);
            Klass klass = GC.getKlass(object);
            block = object.add(GC.getBodySize(klass, object));
        }
        return count;
    }

    /**
     * Reserves space in the free part of the heap for the map that will be used to undo
     * forwarded pointers.
     *
     * @param allocStart  the start of the heap
     * @param allocTop    the address of the byte one past the last allocated object
     * @param allocEnd    the end of the heap
     * @return true if there was enought space for the map
     */
    private boolean initializeForwardingRepairMap(Address allocStart, Address allocTop, Address allocEnd) {
        int mapSize = (countObjects(allocStart, allocTop) * 2) * HDR.BYTES_PER_WORD;
        int freeSpace = allocEnd.diff(allocTop).toInt();
        if (freeSpace < mapSize) {
            return false;
        }
        forwardingRepairMapTop = allocEnd.sub(mapSize);
        forwardingRepairMap = forwardingRepairMapTop;
        return true;
    }

    /**
     * Repairs the class word of any objects that were part of the copied graph.
     */
    private void repairForwardedObjects() {
        Address address = forwardingRepairMap;
        while (address.ne(forwardingRepairMapTop)) {
            Object forwardedObject = NativeUnsafe.getObject(address, 0);
            UWord classWord = NativeUnsafe.getUWord(address, 1);
            NativeUnsafe.setAddress(forwardedObject, HDR.klass, Address.zero().or(classWord));
            address = address.add(HDR.BYTES_PER_WORD * 2);
        }
    }

    /**
     * Records the address of a pointer in the graph being copied.
     *
     * @param pointerAddress the address of a pointer
     */
    private void recordPointer(Address pointerAddress) {
        // Update the oop map
        if (isInToSpace(pointerAddress)) {
            int word = (pointerAddress.diff(toSpaceStartPointer).toInt() / HDR.BYTES_PER_WORD);
            oopMap.set(word);
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("CheneyCollector::recordPointer - set bit in oop map for pointer at ");
                VM.printAddress(pointerAddress);
                VM.println();
            }
        }
    }

    /*---------------------------------------------------------------------------*\
     *                                 Tracing                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * Tests to see if tracing is enabled.
     *
     * @return true if it is
     */
    private boolean tracing() {
        if (GC.GC_TRACING_SUPPORTED) {
            if (!copyingObjectGraph) {
                return GC.isTracing(GC.TRACE_COLLECTION);
            } else {
                return GC.isTracing(GC.TRACE_OBJECT_GRAPH_COPYING);
            }
        }
        return false;
    }

    /**
     * Trace the collector variables.
     */
    private void traceVariables() {
        /*
         * Output trace information.
         */
        VM.println("CheneyCollector variables");
        traceVariable("fromSpaceStartPointer", fromSpaceStartPointer);
        traceVariable("fromSpaceEndPointer", fromSpaceEndPointer);
        traceVariable("fromSpaceSize", fromSpaceEndPointer.diff(fromSpaceStartPointer).toInt());
        traceVariable("toSpaceStartPointer", toSpaceStartPointer);
        traceVariable("toSpaceEndPointer", toSpaceEndPointer);
        traceVariable("toSpaceSize", toSpaceEndPointer.diff(toSpaceStartPointer).toInt());
        traceVariable("toSpaceAllocationPointer", toSpaceAllocationPointer);
    }

    /**
     * {@inheritDoc}
     */
    void traceHeap(String description, Address allocationPointer) {
        traceHeapStart(description, freeMemory(allocationPointer), totalMemory());
        traceHeapSegment("fromSpace", fromSpaceStartPointer, fromSpaceEndPointer);
        traceHeapSegment("toSpace{used}", toSpaceStartPointer, allocationPointer);
        traceHeapSegment("toSpace{free}", allocationPointer, toSpaceEndPointer);

        if (GC.isTracing(GC.TRACE_HEAP_CONTENTS)) {
            for (Address block = toSpaceStartPointer; block.lo(allocationPointer); ) {
                Address object = GC.blockToOop(block);
                Klass klass = GC.getKlass(object);
                int size = GC.getBodySize(klass, object);
                traceHeapObject(block, object, klass, size / HDR.BYTES_PER_WORD);
                block = object.add(size);
            }
        }

        traceHeapEnd();
    }

}
