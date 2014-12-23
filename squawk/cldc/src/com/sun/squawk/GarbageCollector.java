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

import com.sun.squawk.vm.*;
import com.sun.squawk.pragma.*;

/**
 * Base class for all garbage collectors.
 *
 */
public abstract class GarbageCollector implements GlobalStaticFields {
    
    /** If build.properties defines NATIVE_GC_ONLY= true, then define constant NATIVE_GC_ONLY = true.*/
    public final static boolean NATIVE_GC_ONLY = /*VAL*/false/*NATIVE_GC_ONLY*/;
    public final static boolean INTERP_GC_ONLY = !/*VAL*/false/*GC2C*/;
    
    /* package-private**/ GarbageCollector() {}

    /**
     * Creates and initializes the garbage collector. The exact type of the
     * garbage collector will be determined by a system property that is
     * potentially defined on the command line.
     *
     * @param permanentMemoryStart start of the memory region in which the permanent objects have already been allocated
     *                             (including the collector itself)
     * @param memoryStart          start of memory region available to the collector for the collectible object
     *                             memory as well as any auxilary data structures it requires. This value also
     *                             denotes the end of the permanent object space
     * @param memoryEnd            end of garbage collector's allocate memory region
     */
    abstract void initialize(Address permanentMemoryStart, Address memoryStart, Address memoryEnd);

    /**
     * Collect the garbage.
     *
     * @param allocTop the current top of the allocation space - all the space between this address and the
     *                 top of the heap is free
     * @param forceFullGC  forces a collection of the whole heap
     * @return true if the collector performed a collection of the full heap, false otherwise
     */
    final boolean collectGarbage(Address allocTop, boolean forceFullGC) {
        boolean didFull;
        long freeBeforeGC = GC.freeMemory();
        long bytesAllocated = GC.getBytesAllocatedSinceLastGC();
        long start = VM.getTimeMillis();
        if (INTERP_GC_ONLY) {
            didFull = collectGarbageInJava(allocTop, forceFullGC);
        } else if (NATIVE_GC_ONLY) {
            didFull = collectGarbageInC(allocTop, forceFullGC);
        } else if (interpGC) {
            didFull = collectGarbageInJava(allocTop, forceFullGC);
        } else {
            didFull = collectGarbageInC(allocTop, forceFullGC);
        }
        
        lastCollectionTime = VM.getTimeMillis() - start;
        if (didFull) {
            totalFullCollectionTime += lastCollectionTime;
            if (maxFullCollectionTime < lastCollectionTime) {
                maxFullCollectionTime = lastCollectionTime;
            }
        } else {
            totalPartialCollectionTime += lastCollectionTime;
            if (maxPartialCollectionTime < lastCollectionTime) {
                maxPartialCollectionTime = lastCollectionTime;
            }
        }
        
        numBytesLastFreed = GC.freeMemory() - freeBeforeGC;
        numBytesFreedTotal += numBytesLastFreed;
        totalBytesAllocatedCheckPoint += bytesAllocated; // update running total.
        return didFull;
    }

    /**
     * The time taken by the last call to {@link #collectGarbage}.
     */
    private long lastCollectionTime;
    
    /**
     * The time taken by the longest full GC.
     */
    private long maxFullCollectionTime;
    
    /**
     * The time taken by the longest partial GC
     */
    private long maxPartialCollectionTime;

    /**
	 * The time taken by all full GCs {@link #collectGarbage}.
	 */
	private long totalFullCollectionTime;
    
    /**
	 * The time taken by all partial GCs {@link #collectGarbage}.
	 */
	private long totalPartialCollectionTime;
    
    /**
     * The number of bytes scanned in the last collection.
     */
    protected long numBytesLastScanned;
    
    /**
     * The number of bytes freed in the last collection {@link #collectGarbage}.
     */
    private long numBytesLastFreed;
    
    /**
     * The total number of bytes freed in the all collectionss {@link #collectGarbage}.
     */
    private long numBytesFreedTotal;

    /**
     * Running count of byte allocated since the start of the VM until the end of the last GC.
     * EG. It is only updated at GC time.
     */
    private long totalBytesAllocatedCheckPoint;

    /**
     * Gets the time taken by the last collection.
     * @return ms
     */
    public final long getLastGCTime() {
        return lastCollectionTime;
    }
    
    /**
     * Gets the time of the longest full GC.
     * @return ms
     */
    public final long getMaxFullGCTime() {
        return maxFullCollectionTime;
    }
    
    /**
     * Gets the time of the longest partial GC.
     * @return ms
     */
    public final long getMaxPartialGCTime() {
        return maxPartialCollectionTime;
    }

    /**
     * Get the time taken by the slowest garbage collection.
     * @return ms
     */
    public final long getMaxGCTime() {
        return Math.max(getMaxFullGCTime(), getMaxPartialGCTime());
    }
    
    /**
     * Gets the time taken by all GC.
     * @return ms
     */
    public final long getTotalGCTime() {
        return totalFullCollectionTime + totalPartialCollectionTime;
    }
    
    /**
     * Gets the time taken by all full garbage collections.
     * @return ms
     */
    public final long getTotalFullGCTime() {
        return totalFullCollectionTime;
    }
    
    /**
     * Get the time taken by all partial garbage collections.
     * @return ms
     */
    public final long getTotalPartialGCTime() {
        return totalPartialCollectionTime;
    }
    
    /**
     * Get the number of bytes freed in the last collection
     * @return bytes
     */
    public final long getBytesLastFreed() {
        return numBytesLastFreed;
    }
    
    /**
     * Get the number of bytes scanned in the last collection.

     * This is a measure of how much "work" the collector did in the last GC. It measures the size of the heap that was last collected.
     * With generational collectors this may be much less than the size of the used heap.
     * 
     * It can also be used to compute "bytes surviving collection" = getBytesLastScanned() - getBytesLastFreed().
     * @return the number of bytes scanned in the last collection.
     */
    public final long getBytesLastScanned() {
        return numBytesLastScanned;
    }
    
    /**
     * Get the number of bytes freed in all collections
     * @return bytes
     */
    public final long getBytesFreedTotal() {
        return numBytesFreedTotal;
    }
    
    /**
     * Get the total number of bytes allocated between JVM startup and the last collection.
     * @return bytes
     */
    final long getTotalBytesAllocatedCheckPoint() {
        return totalBytesAllocatedCheckPoint;
    }
    
    /**
     * Get the number of bytes allocated since the last GC.
     *
     * May be inaccurate during a copyObjectGraph operation.
     * 
     * @return bytes
     */
    public int getBytesAllocatedSinceLastGC() {
        return GC.getBytesAllocatedSinceLastGC();
    }
    
    /**
     * Get the number of bytes allocated since JVM startup.
     *
     * Watch out for overflow.
     * @return bytes
     */
    public long getBytesAllocatedTotal() {
        return getTotalBytesAllocatedCheckPoint() + getBytesAllocatedSinceLastGC();
    }
    
    /**
     * Determines if the native code includes a native implementation of the configured collector.
     */
    final native boolean hasNativeImplementation();

    /**
     * Hook for using the translated-to-C version of the collector.
     */
    final native boolean collectGarbageInC(Address allocTop, boolean forceFullGC);

    /**
     * Implements collection as a Java routine. It is this routine that will be translated to C and hooked
     * in as the implementation of {@link #collectGarbageInC}.
     */
    abstract boolean collectGarbageInJava(Address allocTop, boolean forceFullGC);

    /**
     * This is a hook that allows a collector to do something after a collection has completed
     * and the collection guard has been reset.
     */
    abstract void postCollection();
    
/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//    /**
//     * Copies an object graph. Upon entry, <code>cb.oopMap</code> must be an oop map initialized
//     * to have a bit for every word in the used part of the heap. The current value of the other two fields of
//     * <code>cb</code> are ignored. If the return value is null, there was not enough room for
//     * the copy of the graph. Otherwise, it will be the address of a byte array containing the copied
//     * graph, <code>cb.root</code> will be the offset (in bytes) to the copy of <code>object</code> in the byte array
//     * and <code>cb.start</code> will be the address that all internal pointers within the copied graph are relative to.
//     *
//     * @param object   the root of the object graph to copy
//     * @param cb       the in and out parameters of this call
//     * @param allocTop the current top of the allocation space - all the space between this address and the
//     *                 top of the heap is free
//     * @return the address of the byte array containing the copied graph or null if insufficent memory
//     */
//    final Address copyObjectGraph(Address object, ObjectMemorySerializer.ControlBlock cb, Address allocTop) {
//        Address result;
//        if (!NATIVE_GC_ONLY && interpGC) {
//            result = copyObjectGraphInJava(object, cb, allocTop);
//        } else {
//            result = copyObjectGraphInC(object, cb, allocTop);
//        }
//        return result;
//    }
//
//    /**
//     * Hook for using the translated-to-C version of the collector.
//     */
//    final native Address copyObjectGraphInC(Address object, ObjectMemorySerializer.ControlBlock cb, Address allocTop);
//
//    /**
//     * Implements graph copying as a Java routine. It is this routine that will be translated to C and hooked
//     * in as the implementation of {@link #copyObjectGraphInC}.
//     */
//    abstract Address copyObjectGraphInJava(Address object, ObjectMemorySerializer.ControlBlock cb, Address allocTop);
/*end[ENABLE_ISOLATE_MIGRATION]*/

    /**
     * Dumps various timing information to the console.
     *
     * @param out  where to dump the timing info
     */
    abstract void dumpTimings(java.io.PrintStream out);

    /**
     * Returns the amount of free memory in the system. Calling the <code>gc</code>
     * method may result in increasing the value returned by <code>freeMemory.</code>
     *
     * @param  allocationPointer  the current allocationPointer
     * @return an approximation to the total amount of memory currently
     *         available for future allocated objects, measured in bytes.
     */
    abstract long freeMemory(Address allocationPointer);

    /**
     * Returns the total amount of memory in the Squawk Virtual Machine. The
     * value returned by this method may vary over time, depending on the host
     * environment.
     * <p>
     * Note that the amount of memory required to hold an object of any given
     * type may be implementation-dependent.
     *
     * @return the total amount of memory currently available for current and
     *         future objects, measured in bytes.
     */
    abstract long totalMemory();

    /**
     * Specifies if the native version of the collectGarbage and copyObjectGraph methods
     * should be used.
     */
    private boolean interpGC = !hasNativeImplementation();

    /**
     * Specifies if timings should be in terms of microseconds or milliseconds.
     */
/*if[FLASH_MEMORY]*/
    private static final boolean useMicrosecondTimer = false; //  no microsecond resolution on SPOT
/*else[FLASH_MEMORY]*/
//    private boolean useMicrosecondTimer;
/*end[FLASH_MEMORY]*/

    /**
     * Gets the time now in terms of microseconds or milliseconds since some epoch.
     */
    final long now() {
        return useMicrosecondTimer ? VM.getTimeMicros() : VM.getTimeMillis();
    }

    /**
     * Gets the suffix used to denote the units of timer measured by the timer.
     */
    final String timerUnitSuffix() {
        return useMicrosecondTimer ? "usec" : "ms";
    }

    /**
     * Process a given command line option that may be specific to the collector implementation.
     *
     * @param arg   the command line option to process
     * @return      true if <code>arg</code> was a collector option
     */
    boolean processCommandLineOption(String arg) {
        if (!INTERP_GC_ONLY && !NATIVE_GC_ONLY) {
            if (arg.equals("-nativegc")) {
                if (!hasNativeImplementation()) {
                    System.out.println("Warning: unsupported -nativegc switch ignored");
                } else {
                    interpGC = false;
                }
                return true;
            } else if (arg.equals("-interpgc")) {
                interpGC = true;
                return true;
            }
        }
/*if[FLASH_MEMORY]*/
/*else[FLASH_MEMORY]*/
//      if (arg.equals("-usecgctimer")) {
//            useMicrosecondTimer = true;
//            return true;
//      }
/*end[FLASH_MEMORY]*/
        return false;
    }

    /**
     * Prints the usage message for the collector specific options (if any).
     *
     * @param out  the stream on which to print the message
     */
    void usage(java.io.PrintStream out) {
        if (!NATIVE_GC_ONLY && !INTERP_GC_ONLY && hasNativeImplementation()) {
            out.println("    -nativegc             use native version of collector (default)");
            out.println("    -interpgc             use interpreted version of collector");
        }
/*if[!FLASH_MEMORY]*/
        out.println("    -usecgctimer          use microsecond (not millisecond) timer for GC");
/*end[FLASH_MEMORY]*/
    }

    void verbose() {
    }

    /*---------------------------------------------------------------------------*\
     *                             Stack chunk tracking                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Notifies this collector of a stack chunk that has just been allocated
     * or a list of stack chunks that have been loaded from a serialized object memory.
     *
     * This will be used by generational collectors to scan the pointers within
     * all thread stacks when doing a partial collection. Updates to these pointers
     * are not tracked by the write barrier.
     */
    void registerStackChunks(Object chunk) {}

    /**
     * Notifies this collector of a stack chunk that has just been replaced
     * by another stack chunk and so is no longer alive.
     */
    void deregisterStackChunk(Object chunk) {}

    /*---------------------------------------------------------------------------*\
     *                             Weak references                               *
    \*---------------------------------------------------------------------------*/

    /**
     * List of weak references.
     */
    protected Ref references;

    /**
     * Adds a weak reference to the list of weak references.
     *
     * @param reference the reference to add
     */
    final void addWeakReference(Ref reference) {
        reference.next = references;
        references = reference;
    }

/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//    /*---------------------------------------------------------------------------*\
//     *                                 Finalization                              *
//    \*---------------------------------------------------------------------------*/
//
//    /**
//     * Queue of pending finalizers.
//     */
//    protected Finalizer finalizers;
//
//    /**
//     * Adds a finalizer to the queue of finalizers.
//     *
//     * @param finalizer the finalizer to add
//     */
//    final void addFinalizer(Finalizer finalizer) {
//        finalizer.setNext(finalizers);
//        finalizers = finalizer;
//    }
//
//    /**
//     * Removes a finalizer from the queue of finalizers.
//     *
//     * @param obj the object of the finalizer
//     */
//    final void eliminateFinalizer(Object obj) {
//        Finalizer prev = null;
//        Finalizer finalizer = finalizers;
//        while (finalizer != null) {
//            if (finalizer.getObject() == obj) {
//                Finalizer next = finalizer.getNext();
//                if (prev == null) {
//                    finalizers = next;
//                } else {
//                    prev.setNext(next);
//                }
//                return;
//            }
//            prev = finalizer;
//            finalizer = finalizer.getNext();
//        }
//    }
/*end[FINALIZATION]*/

    /*---------------------------------------------------------------------------*\
     *                                 Tracing                                   *
    \*---------------------------------------------------------------------------*/

    /**
     * Prints a line with the name and value of an Address GC variable to the VM stream.
     *
     * @param name   the name of the variable
     * @param value  the value of the variable
     */
    final void traceVariable(String name, Address value) {
        VM.print("    ");
        VM.print(name);
        VM.print(" = ");
        VM.printAddress(value);
        VM.println();
    }

    /**
     * Prints a line with the name and value of an integer GC variable to the VM stream.
     *
     * @param name   the name of the variable
     * @param value  the value of the variable
     */
    final void traceVariable(String name, int value) {
        VM.print("    ");
        VM.print(name);
        VM.print(" = ");
        VM.print(value);

        // Format sizes for human readable format (e.g., 1K 234M etc)
        if (value > (1024 * 1024)) {
            VM.print(" [");
            VM.print(value / (1024 * 1024));
            VM.print(".");
            int rem = (value % (1024 * 1024)) / ((1024 * 1024) / 10);
            VM.print(rem);
            VM.print("M]");
        } else if (value > 1024) {
            VM.print(" [");
            VM.print(value / (1024));
            VM.print(".");
            int rem = (value % (1024)) / ((1024) / 10);
            VM.print(rem);
            VM.print("K]");
        }

        VM.println();
    }

    /**
     * Traces the heap's state.
     *
     * @param description         a description of the temporal state of the VM
     * @param  allocationPointer  the current allocationPointer
     */
    abstract void traceHeap(String description, Address allocationPointer);

    /**
     * Conditional compilation of usage of the heap tracing facility.
     * This is controlled by an additional (optional) property in build.properties
     * so that it can be enabled even if general debugging isn't.
     */
    final static boolean HEAP_TRACE = /*VAL*/false/*J2ME.HEAP_TRACE*/;


    /**
     * Writes the initial heap trace line describing the range of memory containing the heap.
     *
     * @param start    the start address of the memory being modelled
     * @param end      the end address of the memory being modelled
     */
    void traceHeapInitialize(Address start, Address end) {
        VM.print("*HEAP*:initialize:");
        VM.printAddress(start);
        VM.print(":");
        VM.printAddress(end);
        VM.println();
    }

    /**
     * Writes a heap trace line denoting the start of a heap trace.
     *
     * @param description  a description of the temporal state of the heap
     * @param freeMemory   the amount of free memory
     */
    final void traceHeapStart(String description, long freeMemory, long totalMemory) {
        VM.print("*HEAP*:start:");
        VM.print(VM.branchCount());
        VM.print(":");
        VM.print(freeMemory);
        VM.print(":");
        VM.print(totalMemory);
        VM.print(":");
        VM.println(description);
    }

    /**
     * Writes a heap trace line denoting the end of a heap trace.
     */
    final void traceHeapEnd() {
        VM.println("*HEAP*:end");
    }

    /**
     * Writes a heap trace line for a segment of the memory.
     *
     * @param label   a descriptor for the segment or null if the segment denotes space never used
     * @param start   the starting address of the segment
     * @param size    the size (in bytes) of the segment
     */
    final void traceHeapSegment(String label, Address start, int size) {
        if (size > 0) {
            VM.print("*HEAP*:segment:");
            VM.printAddress(start);
            VM.print(":");
            VM.printAddress(start.add(size));
            VM.print(":");
            VM.println(label);
        }
    }

    /**
     * Writes a heap trace line for a segment of the memory.
     *
     * @param label   a descriptor for the segment or null if the segment denotes space never used
     * @param start   the starting address of the segment
     * @param end     the address one byte past the end of the segment
     */
    final void traceHeapSegment(String label, Address start, Address end) {
        traceHeapSegment(label, start, end.diff(start).toInt());
    }

    /**
     * Writes a heap trace line for an object. The last has the pattern:
     *
     *   trace  ::= "*HEAP*:" word ":" word* ":oop:" (word ":" | repeat ":" )* class
     *   word   ::= [a 32 or 64 bit unsigned value]
     *   repeat ::= "*" int
     *
     * where the first word is the value of <code>start</code>, the words up to ":oop:"
     * compose the object's header and the remaining words compose the object's body.
     * A 'repeat' states that the previous word is repeated 'n' more times.
     *
     * @param start    the address of the object's header
     * @param oop      the address of the object's body
     * @param klass    the klass of the object
     * @param size     the size (in words) of the object's body
     */
    final void traceHeapObject(Address start, Address oop, Klass klass, int size) {
        VM.print("*HEAP*:");
        VM.printAddress(start);
        VM.print(":");
        while (start.lo(oop)) {
            VM.printUWord(NativeUnsafe.getAsUWord(start, 0));
            VM.print(":");
            start = start.add(HDR.BYTES_PER_WORD);
        }
        VM.print("oop:");
        if (size != 0) {
            UWord last = NativeUnsafe.getAsUWord(oop, 0);
            VM.printUWord(last);
            VM.print(":");
            int repeats = 0;
            for (int i = 1; i != size; ++i) {
                UWord word = NativeUnsafe.getAsUWord(oop, i);
                if (word.eq(last)) {
                    ++repeats;
                } else {
                    if (repeats != 0) {
                        VM.print("*");
                        VM.print(repeats);
                        VM.print(":");
                        repeats = 0;
                    }
                    VM.printUWord(word);
                    VM.print(":");
                    last = word;
                }
            }
            if (repeats != 0) {
                VM.print("*");
                VM.print(repeats);
                VM.print(":");
            }
        }
        VM.println(Klass.getInternalName(klass));
    }

}


