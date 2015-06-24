/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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

package com.sun.squawk;

import com.sun.squawk.microedition.io.FileConnection;
import java.io.*;
import javax.microedition.io.*;

import com.sun.squawk.pragma.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;

import java.util.Enumeration;


/**
 * Pure static class that handles object creation and GC control and monitoring.
 * 
 * The particular GC implementation used is a subclass of {@link GarbageCollector}. Many of
 * the "public" methods of this class are actually "suite-private". See the file "squawk.library.properties"
 * for the list of exported methods.
 */
public class GC implements GlobalStaticFields {
    
    /**
     * Purely static class should not be instantiated.
     */
    private GC() {}

    /**
     * The plug-in garbage collection algorithm. It is critical that this object
     * is not allocated in the heap that is managed by the collector.
     */
    private static GarbageCollector collector;

    /**
     * Counter for the number of full-heap collections.
     */
    private static int fullCollectionCount;

    /**
     * Counter for the number of partial-heap collections.
     */
    private static int partialCollectionCount;

    /**
     * Flags whether or not the VM is in the collector.
     */
    private static boolean collecting;

    /**
     * Gets a reference to the installed collector.
     *
     * @return a reference to the installed collector
     */
    public static GarbageCollector getCollector() {
        return collector;
    }

    /**
     * Excessive GC flag.
     */
    private static boolean excessiveGC;

    /**
     * Counter for the number of monitor exit operations.
     */
    private static int monitorExitCount;

    /**
     * Counter for the number of monitors released at exit time.
     */
    private static int monitorReleaseCount;

    /**
     * Sets the state of the excessive GC flag.
     *
     * @param value true of excessive GC should be is enabled
     */
    static void setExcessiveGC(boolean value) {
        excessiveGC = value;
    }

    /**
     * Gets the state of the excessive GC flag.
     *
     * @return true of excessive GC is enabled
     */
    static boolean getExcessiveGC() {
        return excessiveGC;
    }

    /**
     * Rounds up a 32 bit value to the next word boundry.
     *
     * @param value  the value to round up
     * @return the result
     */
    public static int roundUpToWord(int value)  throws ForceInlinedPragma {
        return (value + (HDR.BYTES_PER_WORD-1)) & ~(HDR.BYTES_PER_WORD-1);
    }

    /**
     * Rounds up a 32 bit value based on a given alignment.
     *
     * @param value      the value to be rounded up
     * @param alignment  <code>value</value> is rounded up to be a multiple of this value
     * @return the aligned value
     */
    static int roundUp(int value, int alignment) {
        return (value + (alignment-1)) & ~(alignment-1);
    }

    /**
     * Rounds down a 32 bit value to the next word boundry.
     *
     * @param value  the value to round down
     * @return the result
     */
    static int roundDownToWord(int value)  throws ForceInlinedPragma {
        return value & ~(HDR.BYTES_PER_WORD-1);
    }

    /**
     * Rounds down a 32 bit value based on a given alignment.
     *
     * @param value      the value to be rounded down
     * @param alignment  <code>value</value> is rounded down to be a multiple of this value
     * @return the aligned value
     */
    static int roundDown(int value, int alignment) {
        return value & ~(alignment-1);
    }

    /*---------------------------------------------------------------------------*\
     *                                  Tracing                                  *
    \*---------------------------------------------------------------------------*/

    /**
     * Used for conditionally compiling trace code.
     */
    final static boolean GC_TRACING_SUPPORTED = Klass.DEBUG_CODE_ENABLED;

    /**
     * GC tracing flag specifying basic tracing.
     * 
     * This trace level should always be enabled (not dependent on GC_TRACING_SUPPORTED)
     */
    static final int TRACE_BASIC = 1;

    /**
     * GC tracing flag specifying tracing of allocation.
     */
    static final int TRACE_ALLOCATION = 2;

    /**
     * GC tracing flag specifying detailed tracing of garbage collection.
     */
    static final int TRACE_COLLECTION = 4;

    /**
     * GC tracing flag specifying detailed tracing of object graph copying.
     */
    static final int TRACE_OBJECT_GRAPH_COPYING = 8;

    /**
     * GC tracing flag specifying heap tracing before each collection.
     */
    static final int TRACE_HEAP_BEFORE_GC = 16;

    /**
     * GC tracing flag specifying heap tracing before each collection.
     */
    static final int TRACE_HEAP_AFTER_GC = 32;

    /**
     * GC tracing flag specifying heap tracing before each collection.
     */
    static final int TRACE_HEAP_CONTENTS = 64;

    /**
     * The mask of GC trace flags.
     */
    private static int traceFlags;

    /**
     * The number of collections that must occur before garbage collector
     * tracing is enabled. Only effective if GarbageCollector.HEAP_TRACE || GC.GC_TRACING_SUPPORTED.
     */
    private static int traceThreshold;

    /**
     * Determines if a specified GC tracing option is enabled.
     *
     * @param option  the GC tracing option to test (one of the <code>GC.TRACE_...</code> values)
     * @return true if the option is enabled and it's one of the basic options (TRACE_BASIC or TRACE_ALLOCATION)
     *         or the collection count threshold has been met
     */
    static boolean isTracing(int option) {
        if ((traceFlags & option & TRACE_BASIC) != 0) {
            return true;
        } else if ((GarbageCollector.HEAP_TRACE || GC.GC_TRACING_SUPPORTED) && (traceFlags & option) != 0) {
            if ((traceFlags & option & TRACE_ALLOCATION) != 0) {
                return true;
            } else {
                return (collector == null || getTotalCount() >= traceThreshold);
            }
        } else {
            return false;
        }
    }

    /**
     * Sets the number of collections that must occur before garbage collector
     * tracing is enabled.
     *
     * @param threshold the number of collections that must occur before garbage collector
     * tracing is enabled
     */
    static void setTraceThreshold(int threshold) {
        traceThreshold = threshold;
    }

    /**
     * Gets the number of collections that must occur before garbage collector
     * tracing is enabled.
     *
     * @return the number of collections that must occur before garbage collector
     * tracing is enabled
     */
    static int getTraceThreshold() {
        return traceThreshold;
    }

    /*---------------------------------------------------------------------------*\
     *                       Read only object memories                           *
    \*---------------------------------------------------------------------------*/

    /**
     * The set of object memories that are in read-only memory. This array is not
     * owned by any isolate and there is a test in the object graph copier that
     * an ObjectMemory instance is never part of a copied object graph.
     */
    private static ObjectMemory[] readOnlyObjectMemories;

    /**
     * Searches for an ObjectMemory in read-only memory that corresponds to a given URI.
     *
     * @param uri   the URI to search with
     * @return the ObjectMemory that corresponds to <code>uri</code> or null if there is no such ObjectMemory
     */
    static ObjectMemory lookupReadOnlyObjectMemoryBySourceURI(String uri) {
        if (readOnlyObjectMemories != null) {
            for (int i = 0; i != readOnlyObjectMemories.length; ++i) {
                ObjectMemory om = readOnlyObjectMemories[i];
                if (om.getURI().equals(uri)) {
                    return om;
                }
            }
        }
        return null;
    }

    /**
     * Searches for an ObjectMemory in read-only memory that corresponds to a given root object.
     *
     * @param root   the root object to search with
     * @return the ObjectMemory that corresponds to <code>root</code> or null if there is no such ObjectMemory
     */
    static ObjectMemory lookupReadOnlyObjectMemoryByRoot(Object root) {
        if (readOnlyObjectMemories != null) {
            for (int i = 0; i != readOnlyObjectMemories.length; ++i) {
                ObjectMemory om = readOnlyObjectMemories[i];
                if (om.getRoot() == root) {
                    return om;
                }
            }
        }
        return null;
    }

    /**
     * Registers an ObjectMemory that is in read-only memory.
     *
     * @param om  the object memory to register
     */
    static void registerReadOnlyObjectMemory(ObjectMemory om) {
        Assert.that(!GC.inRam(om.getStart()));
        Assert.that(lookupReadOnlyObjectMemoryByRoot(om.getRoot()) == null);
        ObjectMemory[] current = readOnlyObjectMemories;
        ObjectMemory[] arr = new ObjectMemory[current.length + 1];
        System.arraycopy(current, 0, arr, 0, current.length);
        arr[current.length] = om;
        readOnlyObjectMemories = arr;

/*if[!FLASH_MEMORY]*/
        if (VM.isVeryVerbose()) {
            PrintStream out = null;
            try {
                FileConnection f = (FileConnection)Connector.open("file://squawk.reloc");
                f.create();
                out = new PrintStream(f.openOutputStream());
                for (int i = 0; i != readOnlyObjectMemories.length; ++i) {
                    om = readOnlyObjectMemories[i];
                    out.println(om.getURI() + "=" + om.getStart().toUWord().toPrimitive());
                }
                System.out.println("[wrote/updated relocation info for read-only object memories to 'squawk.reloc']");
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
/*end[FLASH_MEMORY]*/

    }

    /**
     * Un-registers an ObjectMemory that is in read-only memory.
     *
     * @param om  the object memory to register
     */
    static void unRegisterReadOnlyObjectMemory(ObjectMemory om) {
        ObjectMemory[] current = readOnlyObjectMemories;
        int index = -1;
        for (int i=0; i < readOnlyObjectMemories.length; i++) {
        	if (readOnlyObjectMemories[i] == om) {
        		index = i;
        		break;
        	}
        }
        Assert.that(index >= 0);
        ObjectMemory[] arr = new ObjectMemory[current.length - 1];
        System.arraycopy(current, 0, arr, 0, index);
        System.arraycopy(current, index + 1, arr, index, current.length - index - 1);
        readOnlyObjectMemories = arr;

        if (VM.isVeryVerbose()) {
	        System.out.println("[removed read only object memory: " + om.getURI() +"]");
        }
    }
    
    /**
     * Look through the registered read-only memories and collect all of the Suites.
     * 
     * @return array of Suites, or null if no registered suites in read-only memory
     */
    static Suite[] getSuites() {
        if (readOnlyObjectMemories == null) {
            return null;
        } else {
            int suiteCount = 0;
            
            for (int i = 0; i != readOnlyObjectMemories.length; ++i) {
                ObjectMemory om = readOnlyObjectMemories[i];
                if (om.getRoot() instanceof Suite) {
                    suiteCount++;
                }
            }
            
            Suite[] result = new Suite[suiteCount];
            suiteCount = 0;
            for (int i = 0; i != readOnlyObjectMemories.length; ++i) {
                ObjectMemory om = readOnlyObjectMemories[i];
                if (om.getRoot() instanceof Suite) {
                    result[suiteCount++] = (Suite)om.getRoot();
                }
            }
            return result;
        }
    }

    /*---------------------------------------------------------------------------*\
     *                       Non-volatile memory management                      *
    \*---------------------------------------------------------------------------*/

    /**
     * The start of non-volatile memory.
     */
    private static Address nvmStart;

    /**
     * The end of non-volatile memory.
     */
    private static Address nvmEnd;

    /**
     * The current allocation point in non-volatile memory.
     */
    private static Address nvmAllocationPointer;

    /**
     * Allocate a buffer in NVM. The return buffer has not had its contents zeroed.
     *
     * @param   size        the length in bytes to be allocated (must be a mutiple of HDR.BYTES_PER_WORD)
     * @return  the address of the allocated buffer
     * @exception OutOfMemoryError if the allocation fails
     */
    static Address allocateNvmBuffer(int size) {
        Assert.that(size == roundUpToWord(size));
        Address block  = nvmAllocationPointer;
        Address next = nvmAllocationPointer.add(size);

        if (VM.isHosted()) {
        	nvmEnd = next;
        } else if (next.hi(nvmEnd)) {
            throw VM.getOutOfMemoryError();
        }
        nvmAllocationPointer = next;
        return block;
    }

    /**
     * Gets the size (in bytes) of the NVM.
     *
     * @return the size (in bytes) of the NVM
     */
    static int getNvmSize() {
        return nvmEnd.diff(nvmStart).toInt();
    }

    /**
     * Determines if a given object is within NVM.
     *
     * @param object   the object to test
     * @return true if <code>object</code> is within NVM
     */
    static boolean inNvm(Address object) {
        /*
         * Need to account for the object's header on the low
         * end and zero sized objects on the high end
         */
        return object.hi(nvmStart) && object.loeq(nvmEnd);
    }

    /**
     * Gets the offset (in bytes) of an object in NVM from the start of NVM
     *
     * @param object  the object to test
     * @return the offset (in bytes) of <code>object</code> in NVM from the start of NVM
     */
    static Offset getOffsetInNvm(Address object) {
        Assert.that(inNvm(object));
        return object.diff(nvmStart);
    }

    /**
     * Gets an object in NVM given a offset (in bytes) to the object from the start of NVM.
     *
     * @param offset   the offset (in bytes) of the object to retrieve
     * @return the object at <code>offset</code> bytes from the start of NVM
     */
    static Address getObjectInNvm(Offset offset) {
        return nvmStart.addOffset(offset);
    }

    /*---------------------------------------------------------------------------*\
     *                            RAM memory management                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Flag to show that memory allocation is enabled.
     */
    private static boolean allocationEnabled;

    /**
     * Flag to show that garbage collection is enabled.
     */
    private static boolean gcEnabled;

    /**
     * Start of RAM.
     */
    private static Address ramStart;

    /**
     * End of RAM.
     */
    private static Address ramEnd;

    /**
     * The start of object heap managed by the collector.
     */
    private static Address heapStart;

    /**
     * The end of object heap managed by the collector.
     */
    private static Address heapEnd;

    /**
     * The last value to which {@link #allocTop} was set by a call to {@link #setAllocationParameters}.
     */
    private static Address allocStart;

    /**
     * The next allocation address.
     */
    private static Address allocTop;

    /**
     * The limit for {@link #allocTop} after which the collector should be called.
     */
    private static Address allocEnd;

    /**
     * Count of all objects allocated (may wrap)
     */
    static int newCount;

    /**
     * Count of all allocations that were fulfilled by interpreter loop. Not updated in all build configurations.
     */
    static int newHits;

    /**
     * Initialize the memory system when hosted
     */
    static void initialize() throws HostedPragma {
        ramStart = Address.zero();
        ramEnd = Address.zero();
        nvmStart = Address.zero();
        nvmEnd = Address.zero();
        nvmAllocationPointer = nvmStart;

        // Get the pointers rounded correctly.
        Assert.always(ramStart.eq(ramStart.roundUpToWord()), "RAM limit is not word aligned");
        Assert.always(ramEnd.eq(ramEnd.roundDownToWord()), "RAM limit is not word aligned");

        // Temporarily set the main allocation point to the start-end addresses
        // supplied. This allows permanent objects to be allocated outside the
        // garbage collected heap.
        setAllocationParameters(ramStart, ramStart, ramEnd, ramEnd);
        setAllocationEnabled(true);

        readOnlyObjectMemories = new ObjectMemory[]{};
    }
        
    /**
     * Initialize the memory system.
     */
    static void initialize(Suite bootstrapSuite) {
        Assert.that(!VM.isHosted());

        // Get the pointers rounded correctly.
        Assert.always(ramStart.eq(ramStart.roundUpToWord())); // "RAM limit is not word aligned"
        Assert.always(ramEnd.eq(ramEnd.roundDownToWord()));   // "RAM limit is not word aligned"

        // Temporarily set the main allocation point to the start-end addresses
        // supplied. This allows permanent objects to be allocated outside the
        // garbage collected heap.
        setAllocationParameters(ramStart, ramStart, ramEnd, ramEnd);
        setAllocationEnabled(true);

        // Allocate the collector object outside the scope of the memory that will be managed by the collector.
        GarbageCollector newcollector = new /*VAL*/CheneyCollector/*GC*/(ramStart, ramEnd);
        // Initialize the collector.
        newcollector.initialize(ramStart, allocTop, ramEnd);
        // Initialize the record of loaded/resident object memories
        readOnlyObjectMemories = new ObjectMemory[]{ObjectMemory.createBootstrapObjectMemory(bootstrapSuite)};
        // Enable GC.
        GC.collector = newcollector;
        gcEnabled = true;
    }
    
//    private static Suite appSuite;
//    public static void setAppSuite(Suite appSuiteObj) {
//    	appSuite = appSuiteObj;
//    }
//    public static Suite getAppSuite() {
//    	return appSuite;
//    }
    
//    public static void initializeAppSuite(Suite appSuite) {
////		ObjectMemory om = new ObjectMemory(VM.getAppStart(), VM
////				.getAppEnd().diff(VM.getAppStart()).toInt(), "memory:bootstrap",
////				VM.getAppSuite(), VM.getAppHash(), null);
//    	
//    	ObjectMemory parent = GC.lookupReadOnlyObjectMemoryBySourceURI("memory:bootstrap");
////    	String name = getStringAt(NativeUnsafe.getAddress(VM.getAppSuite(), FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Suite$name)));
////    	Suite appSuite = new Suite("tests.HelloWorld", (Suite)parent.getRoot(), NativeUnsafe.getInt(VM.getAppSuite(), FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_Suite$type)));
//    	
//    	
//    	ObjectMemory om = new ObjectMemory(VM.getAppStart(), VM.getAppEnd().diff(VM.getAppStart()).toInt(), "file://tests/HelloWorld/HellloWorld.suite", appSuite, VM.getAppHash(), parent);
//    	GC.registerReadOnlyObjectMemory(om);
//    }
    

    /**
     * Sets the allocation parameters. The allocator will continue allocating from <code>allocStart</code>
     * until <code>allocEnd</code> is reached.
     *
     * @param heapStart      the start of the memory in which objects are allocated
     * @param allocStart     the start of the current allocation window
     * @param allocEnd       the end of the current allocation window
     * @param heapEnd        the end of the memory in which objects are allocated
     */
    static void setAllocationParameters(Address heapStart, Address allocStart, Address allocEnd, Address heapEnd) {
        GC.heapStart = heapStart;
        GC.allocTop = GC.allocStart = allocStart;
        GC.allocEnd = allocEnd;
        GC.heapEnd = heapEnd;
    }
    
    static void setAllocTop(Address address)  throws HostedPragma {
    	allocTop = address;
    }

    /**
     * Get the number of bytes allocated since the last GC.
     *
     * May be inaccurate during a copyObjectGraph operation.
     * 
     * @return bytes
     */
    public static int getBytesAllocatedSinceLastGC() {
        return allocTop.diff(allocStart).toInt();
    }

    /**
     * Gets the number of objects currently allocated.
     *
     * @return the number of objects currently allocated
     */
    static int countObjectsInRamAllocationSpace() {
        Address end = allocTop;
        int count = 0;
        for (Address block = heapStart; block.lo(end); ) {
            ++count;
            Address object = GC.blockToOop(block);
            Klass klass = GC.getKlass(object.toObject());
            block = object.add(GC.getBodySize(klass, object));
        }
        return count;
    }

    /**
     * Gets the offset (in bytes) of an object in RAM from the start of RAM
     *
     * @param object  the object to test
     * @return the offset (in bytes) of <code>object</code> in RAM from the start of RAM
     */
    static Offset getOffsetInRam(Address object) {
        Assert.that(inRam(object));
        return object.diff(ramStart);
    }

    /**
     * Determines if a given object is in RAM.
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="fatalVMError(\"hosted-only method\"); return false;")
/*end[JAVA5SYNTAX]*/
    static boolean inRamHosted(Object object) throws HostedPragma {
        return !(object instanceof Address);
    }

    /**
     * Determines if a given object is in RAM.
     *
     * @param   object  the object to test
     * @return  true if <code>object</code> is an instance in RAM
     */
    public static boolean inRam(Object object) throws ForceInlinedPragma {
        if (VM.isHosted()) {
            return inRamHosted(object);
        } else {
            Address ptr = Address.fromObject(object);
            if (ptr.loeq(ramStart)) {
                return false;
            } else if (ptr.hi(ramEnd)) {
                return false;
            }
        }
        return true;
    }

     private static boolean inRange(Address ptr, Address start, Address end) {
         if (ptr.loeq(ramStart)) {
                return false;
            } else if (ptr.hi(ramEnd)) {
                return false;
            }
          return true;
    }
    
    /**
     * Determines if any part of the range is in RAM.
     *
     * @param start 
     * @param end
     * @return true if any part of the range start..end overlaps with the heap
     */
    public static boolean inRam(Address start, Address end) {
        Offset userRangeSize = end.diff(start);
        Assert.always(userRangeSize.ge(Offset.zero()));
        if (userRangeSize.ge(ramEnd.diff(ramStart))) {
            // user range is larger - it may contain part of heap:
            if (inRange(ramStart, start, end) ||
                    inRange(ramEnd, start, end)) {
                return true;
            }
        } else {
            // heap is larger - it may contain part of user range:
            if (inRange(start, ramStart, ramEnd) ||
                    inRange(end, ramStart, ramEnd)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Enable or disable memory allocation.
     *
     * @param newState the state that allocation should be set to
     * @return the allocation state before this call
     */
    static boolean setAllocationEnabled(boolean newState) {
        boolean oldState  = allocationEnabled;
        allocationEnabled = newState;
        return oldState;
    }

    /**
     * Enable or disable the garbage collector.
     *
     * @param newState the new abled/disabled state of the garbage collector
     * @return the garbage collector's state before this call
     */
    public static boolean setGCEnabled(boolean newState) {
        boolean oldState = gcEnabled;
        gcEnabled = newState;
        return oldState;
    }

    /**
     * Is Garnage collection enabled?
     * @return true if GC enabled.
     */
    public static boolean isGCEnabled() {
        return gcEnabled;
    }

    /**
     * Test to see if this is a safe time to switch threads.
     *
     * @return true if it is
     */
    public static boolean isSafeToSwitchThreads() {
        return allocationEnabled;
    }

    /**
     * Allocate a chunk of zeroed memory from RAM in a hosted environment.
     *
     * @param   size        the length in bytes of the object and its header (i.e. the total number of
     *                      bytes to be allocated). This size is rounded up by this function to be word-aligned.
     * @param   klass       the class of the object being allocated
     * @param   arrayLength the number of elements in the array being allocated or -1 if a non-array
     *                      object is being allocated
     * @return a pointer to a well-formed object or null if the allocation failed
     */
    private static Object allocatePrimHosted(int size, Object klass, int arrayLength) throws HostedPragma {
        boolean isArray  = arrayLength != -1;
        int headerSize   = isArray ? HDR.arrayHeaderSize : HDR.basicHeaderSize;
        UWord encodedArrayLength = encodeLengthWord(arrayLength);
        Object res;

        VM.extendsEnabled = false;  //------------------------ NO CALL ZONE ---------------------------

        Address block = allocTop;
        Offset available = allocEnd.diff(block);
        if (size < 0 || available.lt(Offset.fromPrimitive(size))) {
            res = null;
        } else {
            Address oop = block.add(headerSize);
            NativeUnsafe.setObject(oop, HDR.klass, klass);
            if (isArray) {
                NativeUnsafe.setUWord(oop, HDR.length, encodedArrayLength);
            }
            allocTop = block.add(size);
            VM.zeroWords(oop, allocTop);
            res = oop.toObject();
        }

        VM.extendsEnabled = true; //----------------------------------------------------------------------

        return res;
    }

    /**
     * Allocate a chunk of zeroed memory from RAM. Only this method contains
     * a sequence of code that exposes some allocated some memory before it is been
     * registered with the garbage collector (i.e. assigned to a reference).
     *
     * @param   size        the length in bytes of the object and its header (i.e. the total number of
     *                      bytes to be allocated). This size must be word-aligned and non-negative
     * @param   klass       the class of the object being allocated
     * @param   arrayLength the number of elements in the array being allocated or -1 if a non-array
     *                      object is being allocated
     * @return a pointer to a well-formed object or null if the allocation failed
     */
    private static Object allocatePrim(int size, Object klass, int arrayLength) {
        Assert.that(allocationEnabled);
        Assert.that(size == roundUpToWord(size));
        Assert.that(size >= 0);
        /*
         * When romizing, allocation always bumps the ram end pointer.
         */
        if (VM.isHosted()) {
            allocEnd = allocTop.add(size);
            NativeUnsafe.setMemorySize(allocTop.toUWord().toOffset().add(size).toInt());
            return allocatePrimHosted(size, klass, arrayLength);
        }

        Object oop = VM.allocate(size, klass, arrayLength);

        /*
         * Trace.
         */
        if (oop != null) {
            if (GC.GC_TRACING_SUPPORTED && isTracing(TRACE_ALLOCATION)) {
                VM.print("[Allocated object: block = ");
                VM.printUWord(GC.oopToBlock(VM.asKlass(klass), Address.fromObject(oop)).toUWord());
                VM.print(" oop = ");
                VM.printAddress(oop);
                VM.print(" size = ");
                VM.print(size);
                VM.print(" klass = ");
                VM.printAddress(klass);
                VM.print(" ");
                VM.print(GC.getKlass(oop).getInternalName());
                VM.println("]");
            }
        } else {
            if (GC.GC_TRACING_SUPPORTED && isTracing(TRACE_ALLOCATION)) {
                VM.print("[Failed allocation of ");
                VM.print(size);
                VM.print(" bytes, klass = ");
                VM.print(VM.asKlass(klass).getInternalName());
//                VM.print(", bcount = ");
//                VM.print(VM.branchCount());
                VM.print(" (bytes free: ");
                VM.printOffset(allocEnd.diff(allocTop));
                VM.print(" in alloc space, ");
                VM.printOffset(heapEnd.diff(allocTop));
                VM.println(" in total)]");
            }
        }

        return oop;
    }

    /**
     * Allocate memory for an object from RAM.
     *
     * @param   size        the length in bytes of the object and its header (i.e. the total number of
     *                      bytes to be allocated). This size must be word-aligned.
     * @param   klass       the class of the object being allocated
     * @param   arrayLength the number of elements in the array being allocated or -1 if a non-array object is being allocated
     * @return  a pointer to a well-formed object
     * @exception OutOfMemoryError if the allocation fails
     */
    private static Object allocate(int size, Object klass, int arrayLength) {
        Object oop = (excessiveGC && !VMThread.currentThread().isServiceThread()) ? null : allocatePrim(size, klass, arrayLength);
        if (oop == null) {
            Assert.always(VM.isThreadingInitialized()); // "insufficient memory to start VM"
            if (gcEnabled) {
		//VM.println("allocate 2 " + size + " " + arrayLength);
                VM.collectGarbage(false);
                oop = allocatePrim(size, klass, arrayLength);
                if (oop == null) {
		    //VM.println("allocate 3 " + size + " " + arrayLength);
                    // try harder!
                    VM.collectGarbage(true);
                    oop = allocatePrim(size, klass, arrayLength);
                }
            } else {
                VM.println("ALLOCATION WHILE GC is DISABLED!");
            }
            if (oop == null) {
                if (GC.GC_TRACING_SUPPORTED) {
                    VM.print("allocate size: ");
                    VM.print(size);
                    VM.print(", klass: ");
                    VM.print(((Klass) klass).getInternalName());
                    VM.print(", arrayLength: ");
                    VM.print(arrayLength);
                    VM.println();
                    VM.print("bytes free: ");
                    VM.printOffset(allocEnd.diff(allocTop));
                    VM.print(" in alloc space, ");
                    VM.printOffset(heapEnd.diff(allocTop));
                    VM.println(" in total");
                }
                throw VM.getOutOfMemoryError();
            }
        }
        // The Lisp2Collector relies on instances always being at a higher address
        // than their class if the klass is itself in RAM
        Assert.that(VM.isHosted() || !inRam(klass) || Address.fromObject(klass).lo(Address.fromObject(oop)));
        return oop;
    }

    /**
     * Collect the garbage.
     *
     * @param forceFullGC  forces a collection of the whole heap
     */
    static void collectGarbage(boolean forceFullGC) {

        // Trace.
        long free = freeMemory();

        // Prunes 'dead' isolates from weakly linked global list of isolates.
        VM.pruneIsolateList();

        // Clear the class state cache.
        VM.invalidateClassStateCache();

        // Set the collector re-entry guard.
        Assert.always(!collecting);
        collecting = true;

        // Disable allocation and check that it was enabled.
        boolean oldState = setAllocationEnabled(false);
        if (oldState == false) {
            VM.fatalVMError();
        }

        // Call the collector with the current allocation pointer.
        boolean fullCollection = collector.collectGarbage(allocTop, forceFullGC);

        // Unset the collector re-entry guard.
        collecting = false;

        // Allow the collector to do any post collection stuff
        collector.postCollection();

        // Enable allocation again.
        setAllocationEnabled(true);

	// Arndt: if I enable the below block, it says "[Full GC" and then
	// nothing more
        if (isTracing(TRACE_BASIC)) {
            long afterFree = freeMemory();
            if (fullCollection) {
                VM.print("[Full GC ");
            } else {
                VM.print("[GC ");
            }

            if (GC.GC_TRACING_SUPPORTED && isTracing(TRACE_COLLECTION)) {
                VM.print("[count : ");
                VM.print(getTotalCount());
                VM.print(", backward branch count: ");
                VM.print(VM.getBranchCount());
                VM.print("] ");
            }
            VM.print(free);
            VM.print("->");
            VM.print(afterFree);
            VM.print("(");
            VM.print(totalMemory());
            VM.print("), ");
            VM.print(collector.getLastGCTime());
            VM.print("ms");
            collector.verbose();
            VM.println("]");
        }

        // Update the relevant collection counter
        if (fullCollection) {
            fullCollectionCount++;
        } else {
            partialCollectionCount++;
        }

        // Check that the class state cache is still clear.
        Assert.always(VM.invalidateClassStateCache());
    }

    /**
     * Calculates the size (in bytes) of an oop map that will have a bit for
     * every word in a memory of a given size.
     *
     * @param size   the size (in bytes) of the memory that the oop map will describe
     * @return the size (in bytes) of an oop map that will have a bit for every word in
     *               a memory region of size <code>size</code> bytes
     */
    public static int calculateOopMapSizeInBytes(int size) {
        return ((size / HDR.BYTES_PER_WORD) + 7) / 8;
    }

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//    /**
//     * @see VM#copyObjectGraph(Object)
//     */
//    static void copyObjectGraph(Address object, ObjectMemorySerializer.ControlBlock cb) {
//        /*
//         * Trace.
//         */
//        if (GC.GC_TRACING_SUPPORTED && isTracing(TRACE_OBJECT_GRAPH_COPYING)) {
//            VM.print("** Copying object graph rooted by ");
//            VM.print(GC.getKlass(object).getName());
//            VM.print(" instance @ ");
//            VM.printAddress(object);
//            VM.print(" ** (collection count: ");
//            VM.print(getTotalCount());
//            VM.print(", backward branch count:");
//            VM.print(VM.getBranchCount());
//            VM.println(")");
//        }
//
//        /*
//         * Set the collector re-entry guard.
//         */
//        Assert.always(!collecting);
//        collecting = true;
//
//        Address copiedObjects = collector.copyObjectGraph(object, cb, allocTop);
//
//        /*
//         * Unset the collector re-entry guard.
//         */
//        collecting = false;
//
//        // Update the fields of the control block that weren't updated by the collector
//        Assert.always(copiedObjects.isZero() || !cb.start.isZero(), "collector must have recorded base address for internal pointers in copied object graph");
//        cb.memory = (byte[])copiedObjects.toObject();
//    }
/*end[ENABLE_ISOLATE_MIGRATION]*/

    private static void encodeLengthWordError() throws NotInlinedPragma {
			VM.println("encodeLengthWord");
            throw VM.getOutOfMemoryError();
    }

    /**
     * Encode an array length word.
     *
     * @param length the length to encode
     * @return the encoded length word
     */
    private static UWord encodeLengthWord(int length) throws ForceInlinedPragma {
        // Can only support arrays whose length can encoded in 30 bits. Throwing
        // an out of memory error is the cleanest way to handle this situtation
        // in the rare case that there was enough memory to allocate the array
        if (length > 0x3FFFFFF) {
            encodeLengthWordError();
        }
        return UWord.fromPrimitive((length << HDR.headerTagBits) | HDR.arrayHeaderTag);
    }

    /**
     * Decode an array length word.
     *
     * @param word encoded length word
     * @return the decoded length
     */
    static int decodeLengthWord(UWord word) throws ForceInlinedPragma {
        return (int)word.toPrimitive() >>> HDR.headerTagBits;
    }

    /**
     * Setup the class pointer field of a header.
     *
     * @param oop object pointer
     * @param klass the address of the object's classs
     */
    /*private*/ static void setHeaderClass(Address oop, Object klass) throws ForceInlinedPragma {
        NativeUnsafe.setAddress(oop, HDR.klass, klass);
    }

    /**
     * Setup the length word of a header.
     *
     * @param oop object pointer
     * @param length the length in elements of the array
     */
    /*private*/ static void setHeaderLength(Address oop, int length) throws ForceInlinedPragma {
        NativeUnsafe.setUWord(oop, HDR.length, encodeLengthWord(length));
    }

    /**
     * Get the class of an object.
     *
     * @param object the object
     * @return its class
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(proxy="getClass")
/*end[JAVA5SYNTAX]*/
    public static Klass getKlass(Object object)  throws ForceInlinedPragma {
        Assert.that(object != null);
        Address classOrAssociation = NativeUnsafe.getAddress(object, HDR.klass);
/*if[DEBUG_CODE_ENABLED]*/
        if (!isPointer(classOrAssociation)) {
            VM.print("object: ");
            VM.printAddress(object);
            VM.println();
            
            VM.print("classOrAssociation: ");
            VM.printAddress(classOrAssociation);
            VM.println();
        }
        Assert.always(!classOrAssociation.isZero());
/*end[DEBUG_CODE_ENABLED]*/
        Assert.that(!classOrAssociation.isZero());
        Object klass = NativeUnsafe.getObject(classOrAssociation, (int)FieldOffsets.com_sun_squawk_Klass$self);
        //Assert.that(!klass.isZero());
        Assert.that(klass != null);
        return VM.asKlass(klass);
    }

    /**
     * Get the length of an array.
     *
     * @TODO: Think about replacing calls to this with the OPC.ARRAY_LENGTH bytecode
     *
     * @param array the array
     * @return the length in elements of the array
     */
    static int getArrayLengthNoCheck(Object array) throws ForceInlinedPragma {
        return decodeLengthWord(NativeUnsafe.getUWord(array, HDR.length));
    }

    /**
     * Get the length of an array.
     *
     * @param array the array
     * @return the length in elements of the array
     */
    public static int getArrayLength(Object array) throws ForceInlinedPragma {
        Assert.that(Klass.isSquawkArray(getKlass(array)));
        return getArrayLengthNoCheck(array);
    }

    /**
     * Create a new object instance in RAM.
     *
     * @param   klass  the object's class
     * @return  a pointer to the object
     * @exception OutOfMemoryError if allocation fails
     */
    static Object newInstance(Klass klass) {
        Object oop = allocate((klass.getInstanceSize() * HDR.BYTES_PER_WORD) + HDR.basicHeaderSize, klass, -1);

/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//        /*
//         * If the object requires finalization and it is in RAM then allocate
//         * a Finalizer for it. (Objects in ROM or NVM cannot be finalized.)
//         */
//        if (!VM.isHosted() && klass.hasFinalizer() && GC.inRam(oop)) {
//            collector.addFinalizer(new Finalizer(oop));
//        }
/*end[FINALIZATION]*/
        return oop;
    }

/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//    /**
//     * Eliminate a finalizer.
//     *
//     * @param obj the object of the finalizer
//     */
//    static void eliminateFinalizer(Object obj) {
//        collector.eliminateFinalizer(obj);
//    }
/*end[FINALIZATION]*/

    /**
     * Create a new array instance.
     *
     * @param  klass         a pointer to the array's class
     * @param  length        the number of elements in the array
     * @param  dataSize      the size in bytes for an element of the array
     * @return a pointer to the allocated array
     * @exception OutOfMemoryError if allocation fails
     */
    private static Object newArray(Object klass, int length, int dataSize) {

       /*
        * Need to handle integer arithmetic wrapping. If byteLength is very large
        * then "length * dataSize" can go negative.
        */
        int bodySize = length * dataSize;
        if (bodySize < 0) {
            VM.println("newArray neg size");
            throw VM.getOutOfMemoryError();
        }

        /*
         * Allocate and return the array.
         */
        int size = roundUpToWord(HDR.arrayHeaderSize + bodySize);
        return allocate(size, klass, length);
    }

    /**
     * Create a new array instance in RAM.
     *
     * @param  klass   the array's class
     * @param  length  the number of elements in the array
     * @return a pointer to the array
     * @exception OutOfMemoryError if allocation fails
     * @exception NegativeArraySizeException if length is negative
     */
    static Object newArray(Klass klass, int length) {
        if (length < 0) {
            throw new NegativeArraySizeException();
        }
        Klass componentType = klass.getComponentType();
        int componentSize = componentType.getDataSize();
        Object result = newArray(klass, length, componentSize);
/*if[TYPEMAP]*/
        byte addressType;
        switch (componentType.getSystemID()) {
            case CID.BOOLEAN:  // fall through
            case CID.BYTE:     addressType = AddressType.BYTE;      break;
            case CID.SHORT:    // ...
            case CID.CHAR:     addressType = AddressType.SHORT;     break;
            case CID.FLOAT:    addressType = AddressType.FLOAT;     break;
            case CID.INT:      addressType = AddressType.INT;       break;
            case CID.LONG:     addressType = AddressType.LONG;      break;
            case CID.DOUBLE:   addressType = AddressType.DOUBLE;    break;
            case CID.VOID:     addressType = AddressType.UNDEFINED; break;
            case CID.OFFSET:   // fall through
            case CID.UWORD:    addressType = AddressType.UWORD;     break;
            case CID.ADDRESS:  // fall through
            default:           addressType = AddressType.REF;       break;
        }
        NativeUnsafe.setArrayTypes(Address.fromObject(result), addressType, componentSize, length);
/*end[TYPEMAP]*/
        return result;
    }

    /**
     * Get a new stack. This method may call the garbage collector.
     *
     * @param   length  the number of words that the new stack should contain.
     * @param   owner   the owner of the new stack
     * @return a pointer to the new stack or null if the allocation fails
     */
    static Object newStack(int length, VMThread owner) {
	//VM.println("newStack " + length);
        int size = roundUpToWord(HDR.arrayHeaderSize + (length * HDR.BYTES_PER_WORD));
        Object stack = allocatePrim(size, Klass.LOCAL_ARRAY, length);
        if (stack != null) {
            NativeUnsafe.setObject(stack, SC.owner, owner);
            collector.registerStackChunks(stack);
        }
        return stack;
    }

    /**
     * Copy the contents of a stack to a new stack.
     *
     * @param srcChunk   the old stack
     * @param dstChunk   the new stack
     */
     static void stackCopy(Object srcChunk, Object dstChunk) {

         if (GC.GC_TRACING_SUPPORTED && isTracing(TRACE_ALLOCATION)) {
             VM.print("GC::stackCopy - srcChunk = ");
             VM.printAddress(srcChunk);
             VM.print(" dstChunk = ");
             VM.printAddress(dstChunk);
             VM.println();
         }

         Address src = Address.fromObject(srcChunk);
         Address dst = Address.fromObject(dstChunk);

         int srcSize = getArrayLength(src) * HDR.BYTES_PER_WORD;
         int dstSize = getArrayLength(dst) * HDR.BYTES_PER_WORD;
         int extra = dstSize - srcSize;

         Address srcLastFP = NativeUnsafe.getAddress(src, SC.lastFP);
         Address dstLastFP = dst.addOffset(srcLastFP.diff(src)).add(extra);

        /*
         * Copy the meta info in the stack chunk (except for the 'next' pointer) and then copy
         * the body (i.e. the space used for activation frames):
         *
         *             <- SC.limit -> <-----  oldBodySize  ------>
         *            +--------------+----------------------------+
         *  oldStack  |     meta     |           body             |
         *            +--------------+----------------------------+
         *
         *
         *             <- SC.limit -> <- extra -> <-----  oldBodySize  ------>
         *            +--------------+-----------+----------------------------+
         *  newStack  |     meta     |           |           body             |
         *            +--------------+-----------+----------------------------+
         */
        NativeUnsafe.setAddress(dst, SC.owner, NativeUnsafe.getAddress(src, SC.owner));
        NativeUnsafe.setAddress(dst, SC.lastFP, NativeUnsafe.getAddress(src, SC.lastFP));
        NativeUnsafe.setUWord(dst, SC.lastBCI, NativeUnsafe.getUWord(src, SC.lastBCI));
        Assert.always(NativeUnsafe.getAddress(src, SC.guard).isZero());

        Address srcEnd = src.add(srcSize);
        int srcUsedSize = srcEnd.diff(srcLastFP).toInt();
        VM.copyBytes(srcLastFP, 0, dstLastFP, 0, srcUsedSize, false);

        /*
         * Adjust the frame pointers in the new stack.
         */
        NativeUnsafe.setAddress(dst, SC.lastFP, dstLastFP);

        Address srcFP = srcLastFP;
        Address dstFP = dstLastFP;
        int fpCount = 0;

        while (!srcFP.isZero()) {
/*if[DEBUG_CODE_ENABLED]*/
            if (!GC.inRam(srcFP)) {
             VM.println("GC::stackCopy - BAD srcFP");
             VM.print(" srcChunk = ");
             VM.printAddress(srcChunk);
             VM.print(" dstChunk = ");
             VM.printAddress(dstChunk);
             VM.println();
             VM.print(" srcFP = ");
             VM.printAddress(srcFP);
             VM.println();
             VM.print(" fpCount = ");
             VM.print(fpCount);
             VM.println();
            }
/*end[DEBUG_CODE_ENABLED]*/

            // Calculate the change in fp when returning to the previous frame in the src stack chunk
            Address srcReturnFP = NativeUnsafe.getAddress(srcFP, FP.returnFP);
            Offset delta = srcReturnFP.diff(srcFP);
            srcFP = srcReturnFP;

            // Apply the change to the fp in the dst stack chunk
            Address dstReturnFP = srcFP.isZero() ? Address.zero() : dstFP.addOffset(delta);
            NativeUnsafe.setAddress(dstFP, FP.returnFP, dstReturnFP);
            dstFP = dstReturnFP;
            fpCount++;
        }

        // Deregister the old stack chunk from the collector.
        collector.deregisterStackChunk(src.toObject());
    }

/*if[DEBUG_CODE_ENABLED]*/
    static void checkSC(Address scAddress) {
        Assert.always(!scAddress.isZero());
        Assert.always(!scAddress.eq(Address.fromPrimitive(0xDEADBEEF)));
     //   Assert.always(VM.asKlass(getKlass(scAddress)).getSystemID() == CID.LOCAL_ARRAY);
        // if ownerless, make sure that we ignore activation records:
        Assert.always(NativeUnsafe.getObject(scAddress, SC.owner) != null || NativeUnsafe.getAddress(scAddress, SC.lastFP).isZero());
    }
    
    /**
     * GC may temporarily encode a ptrs's value so it is not a valid ptr.
     * Try to detect this case.
     * @param ptr
     * @return true if ptr looks like a valid pointer
     */
    static boolean isPointer(Address ptr) {
        return ptr.and(UWord.fromPrimitive(0x3)).isZero();
    }
        
    static void checkSC(Object sc) {
        Assert.always(sc != null);
        Assert.always(!Address.fromObject(sc).eq(Address.fromPrimitive(0xDEADBEEF)));
        if (isPointer(NativeUnsafe.getAddress(sc, HDR.klass))) {
            Assert.always(GC.getKlass(sc).getSystemID() == CID.LOCAL_ARRAY);
        }
        // if ownerless, make sure that we ignore activation records:
        Object owner = NativeUnsafe.getObject(sc, SC.owner);
        Address lastFP = NativeUnsafe.getAddress(sc, SC.lastFP);
        Assert.always(owner != null || lastFP.isZero());
    }
    
    static void checkSC(VMThread thr) {
        Object sc = thr.getStack();
        Object owner = NativeUnsafe.getObject(sc, SC.owner);
        Assert.always(owner == thr || thr.isServiceThread());
        checkSC(sc);
    }
        
/*else[DEBUG_CODE_ENABLED]*/
//    static void checkSC(Address scAddress) throws ForceInlinedPragma { }
//    static void checkSC(Object sc) throws ForceInlinedPragma { }
//    static void checkSC(VMThread thr) throws ForceInlinedPragma { }
/*end[DEBUG_CODE_ENABLED]*/

        
    /**
     * Create a new method.
     *
     * @param   definingClass     the class in which the method is defined
     * @param   body              the method body to encode
     * @return a pointer to the method
     * @exception OutOfMemoryError if allocation fails
     */
    static Object newMethod(Object definingClass, MethodBody body) {
        boolean isHosted = VM.isHosted();

        /*
         * Get a ByteBufferEncoder and write the method header into it.
         */
        ByteBufferEncoder enc = new ByteBufferEncoder();
        body.encodeHeader(enc);
        int roundup = roundUpToWord(enc.getSize());
        int padding = roundup - enc.getSize();

        /*
         * Calculate the total size of the object and allocate it.
         *
         * +word for header length word, +word for defining class pointer.
         */
        int hsize = enc.getSize() + padding + HDR.arrayHeaderSize + HDR.BYTES_PER_WORD + HDR.BYTES_PER_WORD;
        Assert.that(GC.roundUpToWord(hsize) == hsize);
        int hsizeInWords = hsize / HDR.BYTES_PER_WORD;
        int bsize = body.getCodeSize();
        UWord bsizeEncoded = encodeLengthWord(bsize);
        int totalSize = roundUpToWord(hsize + bsize);
        Assert.that((hsize & HDR.headerTagMask) == 0);

        /*
         * The method is intially allocated as a byte array so that 'oop' points to
         * a well formed object. The header is fixed up later.
         */
        Object oop = allocate(totalSize, Klass.BYTE_ARRAY, totalSize - HDR.arrayHeaderSize);

        /*
         * Disable extends and get dirty with real pointers.
         */
        Klass BYTECODE_ARRAY = Klass.BYTECODE_ARRAY;
        VM.extendsEnabled = false;  //------------------------ NO CALL ZONE ---------------------------
        Address block = Address.fromObject(oop).sub(HDR.arrayHeaderSize);
        Address methodOopAsAddress = block.add(hsize);

        /*
         * Set up the class pointer and array length header for the object inside the byte array
         * that will eventually become the bytecode array object.
         */
        NativeUnsafe.setAddress(methodOopAsAddress, HDR.klass, BYTECODE_ARRAY);
        NativeUnsafe.setUWord(methodOopAsAddress, HDR.length, bsizeEncoded);

        /*
         * Write the header length word and update the local variable 'oop'. These two operations
         * convert the allocated object from a byte array to a bytecode array. This is completely
         * safe as the only pointer in this object that the garbage collector will try to update (i.e.
         * the class in which the method was defined) is still null. Also, there will not
         * yet be any activation records described by the as yet incomplete header.
         */
        UWord headerWord = UWord.fromPrimitive((hsizeInWords << HDR.headerTagBits) | HDR.methodHeaderTag);
        NativeUnsafe.setUWord(block, 0, headerWord); // Tag the header size word to indicate that this is a method

        /*
         * Zero the padding bytes.
         */
        if (isHosted) {
            // Clear the pointer to Klass.BYTE_ARRAY
            NativeUnsafe.clearObject(block, 1);
        } else {
            for (int i = 0; i < padding; i++) {
                NativeUnsafe.setByte(block, i + HDR.BYTES_PER_WORD, 0);
            }
        }
        oop = methodOopAsAddress.toObject();

        /*
         * Clear the Address pointers before the next real invoke.
         */
        block = methodOopAsAddress = Address.zero();
        VM.extendsEnabled = true; //----------------------------------------------------------------------

        /*
         * Plug in the defining class.
         */
        NativeUnsafe.setObject(oop, HDR.methodDefiningClass, definingClass);

        /*
         * Copy the header and the bytecodes to the object.
         */
        enc.writeToVMMemory(oop, 0 - hsize + HDR.BYTES_PER_WORD + padding);  // Copy the header
        body.writeToVMMemory(oop);                          // Copy the bytecodes
/*if[TYPEMAP]*/
        if (VM.usingTypeMap()) {
            body.writeTypeMapToVMMemory(oop);
        }
/*end[TYPEMAP]*/

/*if[DEBUG_CODE_ENABLED]*/
        /*
         * Verify the method.
         */
        body.verifyMethod(oop);
/*end[DEBUG_CODE_ENABLED]*/

        /*
         * Write the symbol table entries.
         */
/*if[!FLASH_MEMORY]*/
        if (isHosted || VM.isVerbose()) {
            Method method = body.getDefiningMethod();
            String name = method.toString();
            String file = body.getDefiningClass().getSourceFilePath();
            String lnt = Method.lineNumberTableAsString(method.getLineNumberTable());

            int old = VM.setStream(VM.STREAM_SYMBOLS);

            VM.print("METHOD.");
            VM.printAddress(oop);
            VM.print(".NAME=");
            VM.println(name);

            VM.print("METHOD.");
            VM.printAddress(oop);
            VM.print(".FILE=");
            VM.println(file);

            VM.print("METHOD.");
            VM.printAddress(oop);
            VM.print(".LINETABLE=");
            VM.println(lnt);

            VM.setStream(old);
        }
/*end[FLASH_MEMORY]*/
        
        /*
         * Return the method object.
         */
        return oop;
    }

    /**
     * Copy data from one array to another.
     *
     * @param src    the source array
     * @param srcPos the start position in the source array
     * @param dst    the destination array
     * @param dstPos the start position in the destination array
     * @param lth    number of elements to copy
     */
    public static void arraycopy(Object src, int srcPos, Object dst, int dstPos, int lth) {
        Assert.that(GC.getKlass(src).isArray());
        Assert.that(GC.getKlass(dst).isArray());
        int itemLength = Klass.getComponentType(GC.getKlass(src)).getDataSize(); // static inlines better
        Assert.that(GC.getKlass(dst).getComponentType().getDataSize() == itemLength);
        VM.copyBytes(Address.fromObject(src), srcPos * itemLength, Address.fromObject(dst), dstPos * itemLength, lth * itemLength, false);
    }

    /**
     * Get the size of the elements in a string.
     *
     * @param string the string
     * @return the element size
     */
    private static int getStringOperandSize(Object string) {
        switch (GC.getKlass(string).getSystemID()) {
            case CID.STRING:
            case CID.CHAR_ARRAY: {
                return 2;
            }
            case CID.STRING_OF_BYTES:
            case CID.BYTE_ARRAY: {
                return 1;
            }
            default: {
                VM.fatalVMError();
                return 0;
            }
        }
    }
    
    /**
     * Copy data from one string to another.
     *
     * @param src the source string
     * @param srcPos the start position in the source string
     * @param dst the destination string
     * @param dstPos the start position in the destination string
     * @param lth number of characters to copy
     */
    public static void stringcopy(Object src, int srcPos, Object dst, int dstPos, int lth) {
        int srcsize = getStringOperandSize(src);
        if (srcsize == getStringOperandSize(dst)) {
            VM.copyBytes(Address.fromObject(src), srcPos * srcsize,  Address.fromObject(dst), dstPos * srcsize, lth * srcsize, false);
        } else if (srcsize == 1) {
            Assert.that(getStringOperandSize(dst) == 2);
            for (int i = 0 ; i < lth ; i++) {
                int ch = NativeUnsafe.getByte(src, srcPos++) & 0xFF;
                NativeUnsafe.setChar(dst, dstPos++, ch);
            }
       } else {
            Assert.that(srcsize == 2 && getStringOperandSize(dst) == 1);
            for (int i = 0 ; i < lth ; i++) {
                int ch = NativeUnsafe.getChar(src, srcPos++) & 0xFF;
                NativeUnsafe.setByte(dst, dstPos++, ch);
            }
        }
    }

    /**
     * Change the type of the given object to com.sun.squawk.StringOfBytes.
     *
     * @param oop the object
     * @return the converted object
     */
    public static String makeEightBitString(Object oop) {
        NativeUnsafe.setAddress(oop, HDR.klass, Klass.STRING_OF_BYTES);
        return (String) oop;
    }

    /**
     * Change the type of the given object to java.lang.String.
     *
     * @param oop the object
     * @return the converted object
     */
    public static String makeSixteenBitString(Object oop) {
        NativeUnsafe.setAddress(oop, HDR.klass, Klass.STRING);
        return (String) oop;
    }

    /**
     * Create a String given the address of a null terminated ASCII C string.
     *
     * @param cstring   the address of a null terminated ASCII C string
     * @return the String instance corresponding to
     */
    static String convertCString(Address cstring) {
        int size = 0;
        for (int i = 0 ;; i++) {
            int ch = NativeUnsafe.getByte(cstring, i) & 0xFF;
            if (ch == 0) {
                break;
            }
            size++;
        }
        char[] chars = new char[size];
        for (int i = 0 ; i != size; i++) {
            int ch = NativeUnsafe.getByte(cstring, i) & 0xFF;
            chars[i] = (char)ch;
        }
        return new String(chars);
    }

    /**
     * Create an array of String given the address and length of an array of null terminated ASCII C strings.
     *
     * @param cstringArray   the address of an array of null terminated ASCII C strings
     * @param strings        the String[] instance into which the elements of cstringArray should be copied
     */
    static void copyCStringArray(Address cstringArray, String[] strings) {
        for (int i = 0 ; i < strings.length ; i++) {
            strings[i] = convertCString(NativeUnsafe.getAddress(cstringArray, i));
        }
    }

    /**
     * Get the hashcode for an object.
     *
     * @param object the object the hashcode is needed for.
     * @return the hashcode
     */
    public static int getHashCode(Object object) {
        if (GC.inRam(object)) {
            return getObjectAssociation(object).getHashCode();
        } else {
            return VM.hashcode(object);
        }
    }

    /**
     * Get or allocate the Monitor for an object.
     *
     * @param object the object the monitor is needed for.
     * @return the monitor
     */
    static Monitor getMonitor(Object object) {
        if (GC.inRam(object)) {
            /*
             * Objects in RAM have their monitors attached to ObjectAssociation
             * that sits between the object and its class.
             */
            ObjectAssociation assn = getObjectAssociation(object);
            Monitor monitor = assn.getMonitor();
            if (monitor == null) {
                monitor = new Monitor(object);
                assn.setMonitor(monitor);
            }
            return monitor;
        } else {
            /*
             * Objects in ROM or NVM have their monitors in a hashtable that is
             * maintained by the isolate.
             */
            SquawkHashtable monitorTable = VM.getCurrentIsolate().getMonitorHashtable();
            Monitor monitor = (Monitor)monitorTable.get(object);
            if (monitor == null) {
                monitor = new Monitor(object);
                monitorTable.put(object, monitor);
            }
            return monitor;
        }
    }

/*if[SMARTMONITORS]*/
    /**
     * Remove the monitor (and ObjectAssociation) if possible.
     *
     * @param object the object
     */
    static void removeMonitor(Object object, boolean cond) {
        monitorExitCount++;
        if (cond) {
            if (GC.inRam(object)) {
                ObjectAssociation assn = lookupObjectAssociation(object);
                if (!assn.hashCodeInUse()) {
                    NativeUnsafe.setObject(object, HDR.klass, getKlass(object));
                    monitorReleaseCount++;
                }
            } else {
                SquawkHashtable monitorTable = VM.getCurrentIsolate().getMonitorHashtable();
                monitorTable.remove(object);
                monitorReleaseCount++;
            }
        }
    }

    /**
     * Tests to see if an object has a real monitor object.
     *
     * @param object the object
     * @return true if is does
     */
    static boolean hasRealMonitor(Object object) {
        Monitor monitor = null;
        if (GC.inRam(object)) {
            Object something = NativeUnsafe.getObject(object, HDR.klass);
            Klass klass = getKlass(object);
            if (something == klass) {
                return false;
            } else {
                monitor = ((ObjectAssociation)something).getMonitor();
            }
        } else {
            SquawkHashtable monitorTable = VM.getCurrentIsolate().getMonitorHashtable();
            monitor = (Monitor)monitorTable.get(object);
        }
        return monitor != null;
    }
/*end[SMARTMONITORS]*/

    /**
     * Get or allocate the ObjectAssociation for an object.
     *
     * @param object the object the ObjectAssociation is needed for.
     * @return the ObjectAssociation
     */
    private static ObjectAssociation getObjectAssociation(Object object) throws ForceInlinedPragma {
        Assert.that(GC.inRam(object));
        Object classOrAssociation = NativeUnsafe.getObject(object, HDR.klass);
        Object klass = NativeUnsafe.getObject(classOrAssociation, (int)FieldOffsets.com_sun_squawk_Klass$self);
        if (classOrAssociation != klass) {
            return (ObjectAssociation)classOrAssociation;
        }
        
        return createObjectAssociation(object, VM.asKlass(klass));
    }
    
    private static ObjectAssociation lookupObjectAssociation(Object object) throws ForceInlinedPragma {
        Assert.that(GC.inRam(object));
        Object classOrAssociation = NativeUnsafe.getObject(object, HDR.klass);
        return (ObjectAssociation)classOrAssociation;
    }
    
    private static ObjectAssociation createObjectAssociation(Object object, Klass klass) {
        ObjectAssociation assn = new ObjectAssociation(klass);
        
        // The Lisp2Collector relies on ObjectAssociations always being at a higher address
        // than the object with which they are associated
        Assert.that(VM.isHosted() || Address.fromObject(object).lo(Address.fromObject(assn)));
        
        NativeUnsafe.setObject(object, HDR.klass, assn);
        return assn;
    }

    /**
     * Returns the amount of free memory in the system. Calling the <code>gc</code>
     * method may result in increasing the value returned by <code>freeMemory.</code>
     *
     * @return an approximation to the total amount of memory currently
     *         available for future allocated objects, measured in bytes.
     */
    public static long freeMemory() {
        return collector.freeMemory(allocTop);
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
    public static long totalMemory() {
        return collector.totalMemory();
    }

    /**
     * Returns the number of partial-heap collections.
     *
     * @return the count of partial-heap collections.
     */
    public static int getPartialCount() {
        return partialCollectionCount;
    }

    /**
     * Returns the number of full-heap collections.
     *
     * @return the count of full-heap collections.
     */
    public static int getFullCount() {
        return fullCollectionCount;
    }
    
    /**
     * Returns the total number of garbage collections that have been performed by the VM.
     *
     * @return the total count of collections.
     */
    public static int getTotalCount() {
        return fullCollectionCount + partialCollectionCount;
    }

    /**
     * Create class state object. This method is used for boot strapping
     * com.sun.squawk.Klass.
     *
     * @param klass             the class for which the state is needed
     * @param klassGlobalArray  the class for class state records
     * @return a pointer to the class state
     * @exception OutOfMemoryError if allocation fails
     */
    static Object newClassState(Klass klass, Klass klassGlobalArray) {
        Object res = newArray(klassGlobalArray, CS.firstVariable + klass.getStaticFieldsSize(), HDR.BYTES_PER_WORD);
        NativeUnsafe.setObject(res, CS.klass, klass);
        CS.check(res);
//VM.print(VM.getCurrentIsolate().getMainClassName());
//VM.print(": created class state for ");
//VM.print(Klass.getInternalName(klass));
//VM.print(" -> ");
//VM.printAddress(res);
//VM.print("    bcount=");
//VM.println(VM.branchCount());

        return res;
    }

    /**
     * Create class state object.
     *
     * @param  klass the class for which the state is needed
     * @return a pointer to the class state
     * @exception OutOfMemoryError if allocation fails
     */
    static Object newClassState(Klass klass) {
        return newClassState(klass, Klass.GLOBAL_ARRAY);
    }

    /*---------------------------------------------------------------------------*\
     *                        Object layout and traversal                        *
    \*---------------------------------------------------------------------------*/

    /**
     * Get the address at which the body of an object starts given the address of the
     * block of memory allocated for the object.<p>
     *
     * In conjunction with {@link #oopToBlock(Klass, Address) oopToBlock} and {@link #getBodySize(Klass, Address) getBodySize},
     * this method can be used to traverse a range of contiguous objects between <code>start</code> and <code>end</code> as follows:
     * <p><hr><blockquote><pre>
     *      for (Address block = start; block.LT(end); ) {
     *          Address object = GC.blockToOop(block);
     *          // do something with 'object'...
     *          block = object.add(GC.getBodySize(GC.getKlass(object), object));
     *      }
     * </pre></blockquote><hr>
     *
     * @param block   the address of the block of memory allocated for the object (i.e. the
     *                address at which the object's header starts)
     * @return the address at which the body of the object contained in <code>block</code> starts
     */
    static Address blockToOop(Address block) {
        UWord taggedWord = NativeUnsafe.getAsUWord(block, 0);
        switch (taggedWord.and(UWord.fromPrimitive(HDR.headerTagMask)).toInt()) {
            case HDR.basicHeaderTag:  return block.add(HDR.basicHeaderSize);                 // Instance
            case HDR.arrayHeaderTag:  return block.add(HDR.arrayHeaderSize);                 // Array
            case HDR.methodHeaderTag: return block.add(GC.decodeLengthWord(taggedWord) * HDR.BYTES_PER_WORD); // Method
            default: VM.fatalVMError();
        }
        return null;
    }

    /**
     * Get the address of the block of memory allocated for an object. The returned
     * address is where the header of the object starts.<p>
     *
     * See {@link #blockToOop(Address) blockToOop} to see how this method can be used to traverse a range of objects.
     *
     * @param klass    the class of <code>object</code>
     * @param object   the address of an object
     * @return the address of the block of memory allocated for <code>object</code>
     */
    static Address oopToBlock(Klass klass, Address object) {
        if (Klass.isSquawkArray(klass)) {
            if (Klass.getSystemID(klass) == CID.BYTECODE_ARRAY) {
                return MethodHeader.oopToBlock(object);   // Method
            } else {
                return object.sub(HDR.arrayHeaderSize); // Array
            }
        } else {
            return object.sub(HDR.basicHeaderSize); // Instance
        }
    }

    /**
     * Get the size (in bytes) of the body of an object. That is, the size of
     * block of memory allocated for the object minus the size of the object's
     * header.<p>
     *
     * See {@link #blockToOop(Address) blockToOop} to see how this method can be used to traverse a range of objects.
     *
     * @param klass   the class of <code>object</code>
     * @param object  the address of the object to measure
     * @return the size (in bytes) of <code>object</code>'s body
     */
    static int getBodySize(Klass klass, Address object) {
        if (Klass.isSquawkArray(klass)) {
            int length = GC.getArrayLengthNoCheck(object);
            int elementSize = Klass.getSquawkArrayComponentDataSize(klass);
            return GC.roundUpToWord(length * elementSize);
        } else {
            return Klass.getInstanceSize(klass) * HDR.BYTES_PER_WORD;
        }
    }

    /**
     * Trace a range of an object memory containing contiguous objects.
     *
     * @param start  the address of the first object block in the range
     * @param end    the address of the first byte after the range
     * @param toString specifies if the toString method should be called on the objects (ignored if <code>trace</code> if false)
     */
    static void traceMemory(Address start, Address end, boolean toString) {
        if (Klass.TRACING_ENABLED) {
            VM.print("Trace of object memory range [");
            VM.printAddress(start);
            VM.print(" .. ");
            VM.printAddress(end);
            VM.println(")");
            for (Address block = start; block.lo(end); ) {
                Address object = GC.blockToOop(block);
                Klass klass = GC.getKlass(object);
                
                VM.print("  object = ");
                VM.printAddress(object);
                VM.print(", block = ");
                VM.printAddress(block);
                VM.print(", classOrAssociation = ");
                VM.printAddress(NativeUnsafe.getAddress(object, HDR.klass));
                VM.print(" is instance of ");
                VM.printAddress(klass);
                VM.print(" which is ");
                VM.print(klass.getInternalName());
                
                if (toString) {
                    VM.print(" [toString=\"");
                    VM.print(object.toObject().toString());
                    VM.print("\"]");
                }
                VM.println();
                
                block = object.add(GC.getBodySize(GC.getKlass(object), object));
            }
        }
    }


    /*---------------------------------------------------------------------------*\
     *                        String intern table initialization                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Finds all the instances of String in the read only memory that the
     * current isolate is bound against and adds them to a given hash table.
     *
     * @param   strings  the table to which the strings are to be added
     */
    static String findInRomString(String string) {

        Isolate isolate = VM.getCurrentIsolate();

        Suite suite = isolate.getLeafSuite();
        ObjectMemory parent = null;
        while (parent == null) {
            Assert.that(suite != null);
            parent = suite.getReadOnlyObjectMemory();
            suite = suite.getParent();
            if (suite == null) {
                return null;
            }
        }

        int percent = 0;
        if (GC.GC_TRACING_SUPPORTED && VM.isVeryVerbose()) {
            VM.print("Scanning String objects from read-only memory");
        }
        
        while (parent != null) {
            Address end = parent.getStart().add(parent.getSize());
            for (Address block = parent.getStart(); block.lo(end); ) {
                Address object = GC.blockToOop(block);
                if (object.toObject() instanceof String) {
                    if (object.toObject().equals(string)) {
                        return (String) object.toObject();
                    }
                }
              
                if (GC.GC_TRACING_SUPPORTED && VM.isVeryVerbose()) {
                    Address start = parent.getStart();
                    Offset size = end.diff(start);
                    int percentNow = (block.diff(start).toPrimitive() * 100) / size.toPrimitive();
                    if (percentNow != percent) {
                        VM.print('.');
                        percent = percentNow;
                    }
                }
                
                block = object.add(GC.getBodySize(GC.getKlass(object), object));
            }
            parent = parent.getParent();
        }
        if (GC.GC_TRACING_SUPPORTED && VM.isVeryVerbose()) {
            VM.println(" done");
        }
        return null;
    }
    
    /*---------------------------------------------------------------------------*\
     *                               Heap Statistics                             *
    \*---------------------------------------------------------------------------*/
    
    private static SquawkHashtable heapstats;
    
    private final static String DYNAMIC_CLASSES = "Any dynamically created class, such as array classes";
    
    /**
     * Pre-create all data structures used in heap stats, so heap walking won't allocate more memory.
     */
    public static void initHeapStats() {
        if (heapstats == null) {
            SquawkHashtable table = new SquawkHashtable();
            
            Suite[] suites = getSuites();
            for (int i = 0; i < suites.length; i++) {
                Suite s = suites[i];
                int classCount = s.getClassCount();
                for (int j = 0; j < classCount; j++) {
                    Klass k = s.getKlass(j);
                    if (k != null && k.isInstantiable()) {
                        Assert.always(table.get(s.getKlass(j)) == null);
                        table.put(s.getKlass(j), new ClassStat());
                    }
                }
            }
            
            table.put(DYNAMIC_CLASSES, new ClassStat());
            heapstats = table;
        }
    }
    
    public static class ClassStat {
        public int count;
        public int size;
        
        public final void clear() {
            count = 0;
            size = 0;
        }
        
        public final void update(int objSize) {
            count++;
            size += objSize;
        }
    }
    
    /**
     * Enable heap statistics data structures to be GCed.
     */
    static void clearHeapStats() {
        heapstats = null;
    }
    
    /**
     * Print representation of obj to VM.print stream using as little memory as possible.
     * Currently only handles strings, string buffers, and some arrays.
     * 
     * @param obj
     */
    static void printObject(Object obj, Klass klass, int size) {
        if (obj instanceof String) {
            VM.print("String size: ");
            VM.print(size);
            VM.print(", ");
            VM.print((String) obj);
        } else if (obj instanceof StringBuffer) {
            StringBuffer sb = (StringBuffer) obj;
            VM.print("StringBuffer size: ");
            VM.print(size);
            VM.print(", ");
            for (int i = 0; i < sb.length(); i++) {
                VM.print(sb.charAt(i));
            }
        } else if (klass.isArray()) {
            VM.print(klass.getInternalName());
            VM.print(" size: ");
            VM.print(size);
            VM.print(", length: ");
            VM.print(GC.getArrayLength(obj));

            if (Klass.getSystemID(klass) == CID.CHAR_ARRAY) {
                char[] cha = (char[]) obj;
                VM.print(", ");
                for (int i = 0; i < cha.length; i++) {
                    VM.print(cha[i]);
                }
            } else if (Klass.getSystemID(klass) == CID.GLOBAL_ARRAY) {
                VM.print(" Static variables for ");
                Klass owner = VM.asKlass(NativeUnsafe.getObject(obj, CS.klass));
                VM.print(owner.getInternalName());
            } else if (Klass.getSystemID(klass) == CID.LOCAL_ARRAY) {
                VM.print(" Local variables for thread ");
                VM.printAddress(NativeUnsafe.getObject(obj, SC.owner));
             } else if (klass.getComponentType().isReferenceType()) {
                VM.print(" @");
                VM.printAddress(obj);
                VM.println();
                Object[] array = (Object[])obj;
                for (int i = 0; i < array.length; i++) {
                    VM.print("    [");
                    VM.print(i);
                    VM.print("] ");
                    if (array[i] != null) {
                        printObject(array[i]);
                    } else {
                        VM.println("null");
                    }
                }
                return; // don't do final println
            } else {
                VM.print(" @");
                VM.printAddress(obj);
            }
       } else if (obj instanceof Monitor) {
            Monitor mon = (Monitor) obj;
            VM.print("Monitor for ");
            printObject(mon.object);
            return; // don't do final println
       } else if (obj instanceof VMThread) {
            VMThread thr = (VMThread) obj;
            VM.print("VMThread:");
            VM.print(thr.getName());
            VM.print(" size: ");
            VM.print(size);
            VM.print(", @");
            VM.printAddress(obj);
       } else if (obj instanceof SquawkHashtable) {
            SquawkHashtable table = (SquawkHashtable) obj;
            VM.print("SquawkHashtable:");
            VM.print(" object size: ");
            VM.print(size);
            VM.print(", table size : ");
            VM.print(table.size());
            VM.print(", @");
            VM.printAddress(obj);
            VM.println();
            SquawkHashtable.printTable(table);
            return; // don't do final println
        } else {
            VM.print(klass.getInternalName());
            VM.print(" size: ");
            VM.print(size);
            VM.print(", @");
            VM.printAddress(obj);
        }
        VM.println();
    }

    public static void printObject(Object obj) {
        Klass klass = GC.getKlass(obj);
        int blkSize = GC.getBodySize(klass, Address.fromObject(obj));
        int objSize = blkSize + (klass.isArray() ? HDR.arrayHeaderSize : HDR.basicHeaderSize);
        printObject(obj, klass, objSize);
    }
    
   /**
     * Get the size of this instance or array in bytes,
     * including header words.
     * 
     * @return the size in bytes
     */
    static int getObjectBytes(Object object) {
        Klass klass = GC.getKlass(object);
        Assert.that(Klass.getSystemID(klass) != CID.BYTECODE_ARRAY); //  Doesn't calculate headers sizes for methods correctly
        int blkSize = GC.getBodySize(klass, Address.fromObject(object));
        return blkSize + (klass.isArray() ? HDR.arrayHeaderSize : HDR.basicHeaderSize);
    }
    
   /**
     * Get the size of instances of this klass in bytes,
     * including header words.
     * 
     * @return the size in bytes
     */
    static int getObjectBytes(Klass klass) {
        Assert.that(Klass.getSystemID(klass) != CID.BYTECODE_ARRAY); //  Doesn't calculate headers sizes for methods correctly
        int size = klass.getInstanceSize() << HDR.LOG2_BYTES_PER_WORD;
        return size += HDR.basicHeaderSize;
    }

//    static class DoIsSuiteUsedRefsBlock extends DoBlock {
//        Address start;
//        Address end;
//        ObjectMemory suiteOM;
//        boolean used;
//        boolean verbose;
//        Object referer;
//
//        DoIsSuiteUsedRefsBlock(Suite suite, boolean verbose) {
//            this.suiteOM = suite.getReadOnlyObjectMemory();
//            this.used = false;
//            this.verbose = verbose;
//            this.referer = null;
//        }
//
//        public Object value(Object object) {
//            if (suiteOM.containsAddress(Address.fromObject(object))) {
//                used = true;
//                if (verbose) {
//                    VM.print("Suite is referenced by ");
//                    printObject(object, GC.getKlass(object), 0);
//                    if (referer != null) {
//                        VM.print(" from ");
//                        printObject(referer, GC.getKlass(referer), 0);
//                    }
//                }
//            }
//            return object;
//        }
//
//    }
//
//    static class DoIsSuiteUsedObjectsBlock extends DoBlock {
//        DoIsSuiteUsedRefsBlock refBlock;
//
//        DoIsSuiteUsedObjectsBlock(DoIsSuiteUsedRefsBlock refBlock) {
//            this.refBlock = refBlock;
//        }
//
//        public Object value(Object object) {
//            // readOnlyObjectMemories points to all suites, so skip over it...
//            if (object != readOnlyObjectMemories) {
//                refBlock.referer = object;
//                allReferencesDo(object, refBlock);
//                refBlock.referer = null;
//            }
//            return object;
//        }
//
//    }
//
//    static boolean isSuiteParent(Suite suite) {
//        boolean result = false;
//        Suite[] suites = getSuites();
//        for (int i = 0; i < suites.length; i++) {
//            if (suites[i].getParent() == suite) {
//                result = true;
//                break;
//            }
//        }
//        suites = null;
//        return result;
//    }
//
//    /**
//     * Check to see if a Suite is in use by scanning roots and objects in heap looking for a reference to an object in the given suite.
//     *
//     * @param suite
//     * @return true if the suite is in use
//     */
//    public static boolean isSuiteUsed(Suite suite, boolean verbose) {
//        if (isSuiteParent(suite)) {
//            if (verbose) {
//                VM.println("Suite is a parent of another suite");
//            }
//            return true;
//        }
//
//        DoIsSuiteUsedRefsBlock refsBlock = new DoIsSuiteUsedRefsBlock(suite, verbose);
//        DoIsSuiteUsedObjectsBlock objsBlock = new DoIsSuiteUsedObjectsBlock(refsBlock);
//
//        GC.collectGarbage(true);
//
//        refsBlock.referer = "Global statics";
//        allReferencesInRootsDo(refsBlock);
//        refsBlock.referer = null;
//        if (refsBlock.used) {
//            return true;
//        }
//
//        allObjectsFromDo(null, objsBlock);
//        return refsBlock.used;
//    }
    
    /**
     * Perform doBlock with all objects starting from startObject.
     *
     * @param startObj the object to start walking from , or null
     * @param doBlock callback to perform on each object
     */
    public static void allObjectsFromDo(Object startObj, DoBlock doBlock) {
        Address start;
        if (startObj == null) {
            start = heapStart;
        } else {
            if (!inRam(startObj)) {
                throw new IllegalArgumentException();
            }
            start = GC.oopToBlock(GC.getKlass(startObj), Address.fromObject(startObj));
        }

        int oldPartialCollectionCount = partialCollectionCount;
        int oldFullCollectionCount = fullCollectionCount;

        Address end = allocTop;

        for (Address block = start; block.lo(end); ) {
            Address object = GC.blockToOop(block);
            Klass klass = GC.getKlass(object);
            int blkSize = GC.getBodySize(klass, object);
            doBlock.value(object.toObject());
            if ((oldPartialCollectionCount != partialCollectionCount) ||
                (oldFullCollectionCount != fullCollectionCount)) {
                throw new IllegalStateException("GC during heap walk");
            }
            block = object.add(blkSize);
        }
    }
    
//    /**
//     * Traverses the references in the roots (single-level)
//     *
//     * @param doBlock  the callback to apply to each oop in the traversed objects
//     */
//    private static void allReferencesInRootsDo(DoBlock doBlock) {
//        for (int i = 0 ; i < VM.getGlobalOopCount() ; i++) {
//            Object object = VM.getGlobalOop(i);
//            if (object != null) {
//                doBlock.value(object);
//            }
//        }
//    }
//
//    /**
//     * Perform doBlock on all references in object.
//     *
//     * @param object the object to inspect
//     */
//    public static void allReferencesDo(Object object, DoBlock doBlock) {
//        Klass klass = getKlass(object);
//        doBlock.value(klass);
//
//        // Visit the rest of the pointers
//        if (Klass.isSquawkArray(klass)) {
//            allReferencesInArrayObjectDo(object, klass, doBlock);
//        } else {
//            allReferencesInNonArrayObjectDo(object, klass, doBlock);
//        }
//    }
//
//    private static void allReferencesInArrayObjectDo(Object object, Klass klass, DoBlock doBlock) {
//        switch (Klass.getSystemID(klass)) {
//            case CID.BOOLEAN_ARRAY:
//            case CID.BYTE_ARRAY:
//            case CID.CHAR_ARRAY:
//            case CID.DOUBLE_ARRAY:
//            case CID.FLOAT_ARRAY:
//            case CID.INT_ARRAY:
//            case CID.LONG_ARRAY:
//            case CID.SHORT_ARRAY:
//            case CID.UWORD_ARRAY:
//            case CID.ADDRESS_ARRAY:
//            case CID.STRING:
//            case CID.STRING_OF_BYTES: {
//                break;
//            }
//            case CID.BYTECODE_ARRAY: {
//                doBlock.value(NativeUnsafe.getObject(object, HDR.methodDefiningClass));// this won't happen if not ENABLE_DYNAMIC_CLASSLOADING
//                break;
//            }
//            case CID.GLOBAL_ARRAY: {
//                Klass gaklass = VM.asKlass(NativeUnsafe.getObject(object, CS.klass));
//                Assert.that(gaklass != null);
//                // All the pointer static fields precede the non-pointer fields
//                int end = CS.firstVariable + Klass.getRefStaticFieldsSize(gaklass);
//                for (int i = 0; i < end; i++) {
//                    Object slot = NativeUnsafe.getObject(object, i);
//                    if (slot != null) {
//                        doBlock.value(slot);
//                    }
//                }
//                break;
//            }
//            case CID.LOCAL_ARRAY: {
//                allReferencesInStackChunkDo(object, doBlock);
//                break;
//            }
//            default: { // Pointer array
//                int length = GC.getArrayLengthNoCheck(object);
//                for (int i = 0; i < length; i++) {
//                    Object slot = NativeUnsafe.getObject(object, i);
//                    if (slot != null) {
//                        doBlock.value(slot);
//                    }
//                }
//                break;
//            }
//        }
//    }
//
//    /**
//     * Traverses the oops in a stack chunk.
//     *
//     * @param chunk    the stack chunk to traverse
//     * @param doBlock  the callback to apply to each oop in the traversed objects
//     * @param header   specifies if the header part of the stack chunk should be traversed
//     */
//    private static void allReferencesInStackChunkDo(Object chunk, DoBlock doBlock) {
//        GC.checkSC(chunk);
//        Address fp = NativeUnsafe.getAddress(chunk, SC.lastFP);
//
//        /*
//         * Traverse the pointers in the header part of the stack chunk
//         */
//        doBlock.value(NativeUnsafe.getObject(chunk, SC.owner));
//
//        /*
//         * Update the pointers in each activation frame
//         */
//        boolean isInnerMostActivation = true;
//        while (!fp.isZero()) {
//            allReferencesInActivationDo(fp, isInnerMostActivation, doBlock);
//            fp = VM.getPreviousFP(fp);
//            isInnerMostActivation = false;
//        }
//    }
//
//    /**
//     * Traverses the oops in an activation record.
//     *
//     * @param fp                     the frame pointer
//     * @param isInnerMostActivation  specifies if this is the inner most activation frame on the chunk
//     *                               in which case only the first local variable (i.e. the method pointer) is scanned
//     * @param doBlock                the callback to apply to each traversed oop
//     */
//    private static void allReferencesInActivationDo(Address fp, boolean isInnerMostActivation, DoBlock doBlock) {
//        Address mp  = NativeUnsafe.getAddress(fp, FP.method);
//
//        /*
//         * Get the method pointer and setup to go through the parameters and locals.
//         */
//        int localCount     = isInnerMostActivation ? 1 : MethodHeader.decodeLocalCount(mp.toObject());
//        int parameterCount = MethodHeader.decodeParameterCount(mp.toObject());
//        int mapOffset      = MethodHeader.decodeOopmapOffset(mp.toObject());
//        int bitOffset      = -1;
//        int byteOffset     = 0;
//
//        /*
//         * Parameters.
//         */
//        int varOffset = FP.parm0;
//        while (parameterCount-- > 0) {
//            bitOffset++;
//            if (bitOffset == 8) {
//                bitOffset = 0;
//                byteOffset++;
//            }
//            int bite = NativeUnsafe.getByte(mp, mapOffset+byteOffset);
//            boolean isOop = ((bite >>> bitOffset)&1) != 0;
//            if (isOop) {
//                Object slot = NativeUnsafe.getObject(fp, varOffset);
//                if (slot != null) {
//                    doBlock.value(slot);
//                }
//            }
//            varOffset++; // Parameters go upwards
//        }
//
//        /*
//         * Locals.
//         */
//        varOffset = FP.local0;
//        while (localCount-- > 0) {
//            bitOffset++;
//            if (bitOffset == 8) {
//                bitOffset = 0;
//                byteOffset++;
//            }
//            int bite = NativeUnsafe.getByte(mp, mapOffset + byteOffset);
//            boolean isOop = ((bite >>> bitOffset) & 1) != 0;
//            if (isOop) {
//                Object slot = NativeUnsafe.getObject(fp, varOffset);
//                if (slot != null) {
//                    doBlock.value(slot);
//                }
//            }
//            varOffset--; // Locals go downwards
//        }
//    }
//
//    /**
//     * Traverses all the oops within a non-array object.
//     *
//     * @param object   the object being traversed
//     * @param klass    the class of <code>object</code>. If the class has been forwarded, then this is its pre-forwarding address.
//     * @param doBlock  the callback to apply to each pointer in <code>object</code>
//     */
//    private static void allReferencesInNonArrayObjectDo(Object object, Klass klass, DoBlock doBlock) {
//        Address oopMap;
//        UWord oopMapWord;
//
//        int instanceSize = Klass.getInstanceSize(klass);
//        oopMapWord = NativeUnsafe.getUWord(klass, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord);
//        if (instanceSize > HDR.BITS_PER_WORD) {
//            oopMap = NativeUnsafe.getAddress(klass, (int)FieldOffsets.com_sun_squawk_Klass$oopMap);
//
//            int oopMapLength = ((instanceSize + HDR.BITS_PER_WORD) - 1) / HDR.BITS_PER_WORD;
//            for (int i = 0; i != oopMapLength; ++i) {
//                int/*S64*/ word = NativeUnsafe.getUWord(oopMap, i).toPrimitive();
//                allReferencesInNonArrayObjectForBitmapWordDo(object, doBlock, word, i * HDR.BITS_PER_WORD);
//            }
//        } else {
//            int/*S64*/ word = oopMapWord.toPrimitive();
//            allReferencesInNonArrayObjectForBitmapWordDo(object, doBlock, word, 0);
//        }
//    }
//
//    /**
//     * Traverses the oops within an object whose bits are set in a given oop map word.
//     *
//     * @param object   the object being traversed
//     * @param doBlock  the callback to apply to each pointer in <code>object</code>
//     * @param word     a word from an oop map describing where the pointers are in <code>object</code>
//     * @param offset   the offset of the first field in <code>object</code> mapped by <code>word</code>
//     */
//    private static void allReferencesInNonArrayObjectForBitmapWordDo(Object object, DoBlock doBlock, int/*S64*/ word, int offset) {
//        while (word != 0) {
//            if ((word & 1) != 0) {
//                Object slot = NativeUnsafe.getObject(object, offset);
//                if (slot != null) {
//                    doBlock.value(slot);
//                }
//            }
//            offset++;
//            word = word >>> 1;
//        }
//    }
    
    /**
     * Do actual heap walk, from start object, or whole heap is startObj is null.
     * Collect statistics in heapstats table.
     * 
     * @param startObj the object to start walking from , or null
     */
    static void collectHeapStats(Object startObj, Object endObject, boolean printInstances, boolean collectStats) {
        Address start;
        Address end;
        if (startObj == null) {
            start = heapStart;
        } else {
            if (!inRam(startObj)) {
                throw new IllegalArgumentException();
            }
            start = GC.oopToBlock(GC.getKlass(startObj), Address.fromObject(startObj));
        }
        if (endObject == null) {
            end = allocTop;
        } else {
            if (!inRam(endObject)) {
                throw new IllegalArgumentException();
            }
            end = GC.oopToBlock(GC.getKlass(endObject), Address.fromObject(endObject));
        }

        int oldPartialCollectionCount = partialCollectionCount;
        int oldFullCollectionCount = fullCollectionCount;

        for (Address block = start; block.lo(end); ) {
            Address object = GC.blockToOop(block);
            Klass klass = GC.getKlass(object);
            int blkSize = GC.getBodySize(klass, object);
            int objSize = blkSize + (klass.isArray() ? HDR.arrayHeaderSize : HDR.basicHeaderSize);
            
            if (collectStats) {
                ClassStat cs = (ClassStat)heapstats.get(klass);
                if (cs == null) {
                    // unknown class, must be dyanmic:
                    cs = (ClassStat)heapstats.get(DYNAMIC_CLASSES);
                    if (!klass.isArray()) {
                        VM.print("collectHeapStats - unknown class: ");
                        VM.println(klass.getInternalName());
                    }
                }
                cs.update(objSize);
            }
            if (printInstances) {
                printObject(object.toObject(), klass, objSize);
            }
            
            if ((oldPartialCollectionCount != partialCollectionCount) ||
                (oldFullCollectionCount != fullCollectionCount)) {
                throw new IllegalStateException("GC during heap walk");
            }
            block = object.add(blkSize);
        }
    }
    
    private static void print1Stat(String key, int count, int size) {
        System.out.print(key);
        System.out.print(": \t");
        System.out.print(count);
        System.out.print(" \t");
        System.out.print(size);
        System.out.println();
    }
    
   /**
     * Do actual heap walk, from start object, or whole heap is startObj is null.
     * Count how many instances, and how many bytes are used, by all objects that are the same age or younger than
     * startObj. Print out statistics of each class that has at least one instance in the set found in the heap walk.
     * Statistics are NOT sorted.
     *
     * @param startObj the object to start walking from , or null
     * @param printInstances if true, print information about each object before printing statistics
     */
    public static void printHeapStats(Object startObj, boolean printInstances, boolean collectStats) {
        Object endObjectMarker = new Object();
        
        if (collectStats) {
            initHeapStats();

            Enumeration e = heapstats.elements();
            while (e.hasMoreElements()) {
                ClassStat cs = (ClassStat)e.nextElement();
                cs.clear();
            }
        }

        if (printInstances) {
            VM.println("Instances in heap:");
        }
        collectHeapStats(startObj, endObjectMarker, printInstances, collectStats);

        if (collectStats) {
            VM.println("Class:\t Count: \t Bytes:");
            Enumeration e = heapstats.keys();
            while (e.hasMoreElements()) {
                Object key = e.nextElement();
                ClassStat cs = (ClassStat)heapstats.get(key);
                if (cs.count > 0) {
                    print1Stat(key.toString(), cs.count, cs.size);
                }
            }
        }
        heapstats = null;
    }
    
}
