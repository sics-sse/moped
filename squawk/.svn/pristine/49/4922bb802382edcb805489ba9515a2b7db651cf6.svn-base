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

import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;
import com.sun.squawk.pragma.AllowInlinedPragma;
import com.sun.squawk.pragma.NotInlinedPragma;

/**
 * A collector based on the lisp 2 algorithm described in "Garbage Collection : Algorithms for Automatic Dynamic Memory Management"
 * by Richard Jones, Rafael Lins.<p>
 *
 * <h3>Object Header Layout</h3>
 * For objects that move during a collection, forwarding offsets are installed in high bits
 * of the class word in the object's header. The class pointer is made relative to the space
 * in which the class lies (i.e. ROM, NVM or RAM) and this offset is stored in the lower bits.
 * the two lowest bits of the class word are used as a determine if the object has forwarded
 * ('00' if not) and if so, where is the class located ('01' in the heap, '11' in NVM and '10'
 * in ROM). The forwarding offset is relative to the start of a "slice" with the absolute
 * offset of the slice stored in a fixed size "slice offset table".
 * 
 * <p><blockquote><pre>
 *       <-------------- (W-C-2) ---------> <------ C ------> <-2->
 *      +----------------------------------+-----------------+-----+
 *      |  forwarding offset               | class offset    | tag |
 *      +----------------------------------+-----------------+-----+
 *       <--------------------------- w -------------------------->
 *                                          <-- sliceOffsetShift ->
 * </pre></blockquote>
 *
 * <h3>Heap Layout</h3>
 * <blockquote><pre>
    
                      memoryEnd ->
                                    Slice table


                                    Bitmap


                                    Fixed marking stack

                        heapEnd ->                                                 --+
                                                                                     |
                                    Unused heap (except as extra marking stack)      |
                                                                                     |
              youngGenerationEnd ->                                                  |
                                                                                     |
                                    Young Generation                                 |
                                                                                     |
            youngGenerationStart ->                                                  | Covered by bitmap
                                                                                     |
                                    Old Generation                                   |
                                                                                     |
                       heapStart ->                                                  |
                                                                                     |
                                    {unused space to align heapStart for bitmap}     |
                                                                                     |
                     memoryStart ->                                                  |
                                                                                     |
                                    Permanent space                                  |
                                                                                     |
            permanentMemoryStart ->                                                --+
 * </pre></blockquote>
 *
 * ENABLE_DYNAMIC_CLASSLOADING:
 * If the build property ENABLE_DYNAMIC_CLASSLOADING is true, then GC has to handle Klasses in the heap,
 * and forwarded klasses. Otherwise we can assume that a Klass is in ROM (or NVM).
 */
public final class Lisp2GenerationalCollector extends GarbageCollector {

    /**
     * The default size of the young generation as a percent of the heap size.
     */
    private final static int DEFAULT_YOUNG_GENERATION_PERCENT = 20;

    /**
     * The marking stack.
     */
    private final MarkingStack markingStack;

    /**
     * This class is a namespace to encapsulate the constants that denote
     * the bits in the class word of an object's header that denote whether
     * or not the object has been forwarded and if so, how to decode the
     * encoded class pointer.
     */
    static final class ClassWordTag {

        /**
         * The number of bits used for the tag.
         */
        final static int BIT_COUNT = 2;

        /**
         * The mask applied to a class word to extract the tag.
         */
        final static int MASK = 0x3;

        /**
         * The tag value denoting a non-forwarded object. The class pointer is simply the value of the class word.
         */
        final static int POINTER = 0x00;

        /**
         * The tag value denoting that the encoded class pointer is a word offset relative to the start of the heap.
         */
        final static int HEAP = 0x01;

        /**
         * The tag value denoting that the encoded class pointer is a word offset relative to the start of NVM.
         */
        final static int NVM = 0x02;

        /**
         * The tag value denoting that the encoded class pointer is a word offset relative to the start of ROM.
         */
        final static int ROM = 0x03;

        /**
         * Determines if a given class word value is a direct pointer to a class.
         *
         * @param word   the word to test
         * @return true if <code>word</code> is a direct pointer
         */
        static boolean isPointer(UWord word) throws AllowInlinedPragma {
            return (word.and(UWord.fromPrimitive(MASK))).eq(UWord.fromPrimitive(POINTER));
        }

        /**
         * Determines if a given class word value encodes a class pointer as a heap relative word offset.
         *
         * @param word   the word to test
         * @return true if <code>word</code> encodes a class pointer as a heap relative word offset
         */
        static boolean isHeapOffset(UWord word) throws AllowInlinedPragma  {
            return (word.and(UWord.fromPrimitive(MASK))).eq(UWord.fromPrimitive(HEAP));
        }

        /**
         * Determines if a given class word value encodes a class pointer as a NVM relative word offset.
         *
         * @param word   the word to test
         * @return true if <code>word</code> encodes a class pointer as a NVM relative word offset
         */
        static boolean isNVMOffset(UWord word) throws AllowInlinedPragma {
            return (word.and(UWord.fromPrimitive(MASK))).eq(UWord.fromPrimitive(NVM));
        }

        /**
         * Determines if a given class word value encodes a class pointer as a ROM relative word offset.
         *
         * @param word   the word to test
         * @return true if <code>word</code> encodes a class pointer as a ROM relative word offset
         */
        static boolean isROMOffset(UWord word) throws AllowInlinedPragma {
            return (word.and(UWord.fromPrimitive(MASK))).eq(UWord.fromPrimitive(ROM));
        }

        private ClassWordTag() {
        }
    }

    static final class Timer {
        public static final int SETUP = 0;
        public static final int MARK = 1;
        public static final int COMPUTE_ADDRESSES = 2;
        public static final int UPDATE_POINTERS = 3;
        public static final int COMPACT_OBJECTS = 4;
        public static final int FIXUP_OOPMAPS = 5;
        public static final int POST = 6;
        public static final int FINALIZE = 7;
        public static final int UNFORWARD = 8;

        final long[] times = new long[9];
        long max = 0;
        long min = Long.MAX_VALUE;


        // Cannot be static as no class initializer can be run when a Timer instance is created
        final String[] labels;

        Timer(GarbageCollector gc) {
            labels = new String[times.length];
            labels[0] = "setup:            ";
            labels[1] = "mark:             ";
            labels[2] = "computeAddresses: ";
            labels[3] = "updatePointers:   ";
            labels[4] = "compactObjects:   ";
            labels[5] = "fixupOopmaps:     ";
            labels[6] = "post:             ";
            labels[7] = "finalize:         ";
            labels[8] = "unforward:        ";
            this.gc = gc;
        }

        private long start;
        private final GarbageCollector gc;

        long reset() {
            // cannot nest timings
            Assert.that(start == 0);
            start = gc.now();
            return start;
        }

        long finish(int phase) {
            long now = gc.now();
            times[phase] += (now - start);
            start = 0;
            return now;
        }

        void updateMaxMin(long t) {
            if (min > t) min = t;
            if (max < t) max = t;
        }

        long getTotal() {
            long total = 0;
            for (int i = 0; i != times.length; ++i) {
                total += times[i];
            }
            return total;
        }

        private void dump(java.io.PrintStream out, String label, long value, long total) {
            out.println(label + value + gc.timerUnitSuffix() + "\t[" + asPercentOf(value, total) + "%]");
        }

        private void dump(java.io.PrintStream out, String indent, boolean showZeroTimes) {
            long total = getTotal();
            out.println(indent + "total time:       " + total + gc.timerUnitSuffix());
            for (int i = 0; i != times.length; ++i) {
                if (showZeroTimes || times[i] != 0) {
                    dump(out, indent + labels[i], times[i], total);
                }
            }
        }

        void dump(java.io.PrintStream out, boolean isFull, int count) {
            long total = getTotal();
            if (count != 0) {
                String tus = gc.timerUnitSuffix();
                out.println((isFull ? "Full " : "Partial ") +
                            "collections: [average = " + (total/count) + tus + ", " +
                            "min = " + min + tus + ", max = " + max + tus + "]");
                out.println(    "    collection count: " + count);
                if (GC.GC_TRACING_SUPPORTED) {
                    dump(out, "    ", true);
                }
            }
        }

    }

    /**
     * Start of the memory region in which the permanent objects have already been allocated.
     */
    private Address permanentMemoryStart;

    /**
     * Start address of memory allocated to the garbage collector.
     */
    private Address memoryStart;

    /**
     * The starting address of the heap. The heap is the memory range in which objects are allocated.
     */
    private Address heapStart;

    /**
     * The size (in bytes) of the heap.
     */
    private int heapSize;

    /**
     * The end address of the heap.
     */
    private Address heapEnd;

    /**
     * End address of the memory allocated to the garbage collector.
     */
    private /*final*/ Address memoryEnd;

    /**
     * Address at which to start collecting. The range <code>[collectionStart .. collectionEnd)</code>
     * is the young generation.
     */
    private Address collectionStart;

    /**
     * Address at which to end collecting.
     */
    private Address collectionEnd;

    /**
     * The address at which the slice table starts.
     */
    private Address sliceTable;

    /**
     * The number of slices the heap is partitioned into (which is also the number of entries in the slice table).
     */
    private int sliceCount;

    /**
     * The size (in bytes) of a slice.
     */
    private int sliceSize;

    /**
     * The size (in bytes) of the slice table.
     */
    private int sliceTableSize;

    /**
     * The number of bits in an encoded class word used to express the slice-relative offset to which an object will be moved.
     */
    private int sliceOffsetBits;

    /**
     * The amount by which an encoded class word is logically right shifted to
     * extract the slice-relative offset to which the corresponding object will be moved.
     */
    private int sliceOffsetShift;

    /**
     * The number of bits in an encoded class word used to express the offset of the class in ROM, NVM or the heap.
     */
    private int classOffsetBits;

    /**
     * The mask applied to an encoded class word to extract the offset to the class.
     */
    private int classOffsetMask;

    /**
     * Specifies that the next time a collection is performed, the complete heap
     * will be collected instead of just the young generation. This will be the case
     * when the space reclaimed by the previous collection is less than the size of
     * the young generation.
     */
    private boolean nextCollectionIsFull;

    /**
     * This is the start address of the young generation.
     */
    private Address youngGenerationStart;

    /**
     * The ideal size of the young generation as a percent of the heap size.
     */
    private int idealYoungGenerationSizePercent;

    /**
     * The maximum number of times that {@link #markObject(Address)} may be called recursively.
     */
    private static final int MAX_MARKING_RECURSION = 4;

    /**
     * The number of remaining times the {@link #markObject(Address)} may be called recursively
     * before the object being marked is pushed on the marking stack instead.
     */
    private int markingRecursionLevel;

/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
    /**
     * Count of the number of classes whose oop map moved during the last collection.
     */
    private int movedOopMaps;
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/

    /**
     * The weakly-linked list of stack chunks in the system.
     */
    private Object stackChunks;

    /**
     * The timer for the current action being performed; full collection, partial collection or object graph copy.
     */
    private Timer timer;

    /**
     * Creates a Lisp2GenerationalCollector.
     *
     * @param ramStart       start of the RAM allocated to the VM
     * @param ramEnd         end of the RAM allocated to the VM
     */
    Lisp2GenerationalCollector(Address ramStart, Address ramEnd) {
        int ramSize = ramEnd.diff(ramStart).toInt();
        Assert.always(ramSize > 0);
        int maxHeapSize = calculateMaxHeapSize(ramSize);
        int bitmapSize = ramSize - maxHeapSize;
        Address bitmap = ramEnd.sub(bitmapSize);

        // Only after this call can the VM execute any bytecode that involves updating the write barrier
        // such as 'putfield_o', 'astore_o', 'putstatic_o'... etc.
        Lisp2Bitmap.initialize(bitmap, bitmapSize, ramStart);

        // Initialize the heap trace
        if ((HEAP_TRACE || GC.GC_TRACING_SUPPORTED) && (GC.isTracing(GC.TRACE_HEAP_BEFORE_GC) || GC.isTracing(GC.TRACE_HEAP_AFTER_GC))) {
            traceHeapInitialize(ramStart, ramEnd);
        }

        // Create the marking stack.
        markingStack = new MarkingStack();

        fullCollectionTimer = new Timer(this);
        partialCollectionTimer = new Timer(this);
/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//      copyTimer = new Timer(this);
/*end[ENABLE_ISOLATE_MIGRATION]*/
    }

    /**
     * Computes the size (in bytes) of the largest memory space that may contain classes.
     *
     * @param heapSize   the size of the heap
     * @return the size (in bytes) of the largest memory space that may contain classes
     */
    private static int computeMaxClassAddressSpace(int heapSize) {
        int romSize = VM.getRomEnd().diff(VM.getRomStart()).toInt();
        int nvmSize = GC.getNvmSize();
        int max = heapSize > romSize ? heapSize : romSize;
        return nvmSize > max ? nvmSize : max;
    }

    /**
     * Computes the number of bits required to represent the range of values from
     * 0 up to but not including some limit.
     *
     * @param limit   the limit of the range of values
     * @return the number of bits required to represent values in the range <code>[0 .. limit)</code>
     */
    private static int bitsRequiredFor(int limit) {
        int bits = 0;
        while (limit != 0) {
            bits++;
            limit = limit >>> 1;
        }
        return bits;
    }

    /**
     * Calculates the size (in bytes) of a bitmap that must contain a bit for every word in a memory
     * range of a given size.
     *
     * @param memorySize   the size (in bytes) of the memory range that must be covered by the bitmap
     * @return  the size (in bytes) of the bitmap that will contain a bit for every word in the memory
     */
    private static int calculateBitmapSize(int memorySize) {
        Assert.always(GC.roundDownToWord(memorySize) == memorySize);
        int alignment = HDR.BITS_PER_WORD * HDR.BYTES_PER_WORD;
        memorySize = GC.roundUp(memorySize, alignment);
        int bitCount = memorySize / HDR.BYTES_PER_WORD; // one bit per word
        int size = bitCount / HDR.BITS_PER_BYTE;
        return GC.roundUpToWord(size);
    }

    /**
     * Calculates the maximum heap size for a given memory size where the memory will also
     * contain a bitmap with a bit for every word in the heap. This calculation assumes that
     * the memory will only be used for the heap and bitmap.
     *
     * @param memorySize   the size of the memory to be partitioned into a heap and bitmap
     * @return the maximum size of heap that can be allocated in the memory while leaving
     *                     sufficient space for the bitmap
     */
    private static int calculateMaxHeapSize(int memorySize) {
        // heapSize = memorySize * (32/33)   <-- 32-bit system
        // heapSize = memorySize * (64/65)   <-- 64-bit system
        int heapSize = GC.roundDownToWord((memorySize / (HDR.BITS_PER_WORD + 1)) * HDR.BITS_PER_WORD);
        Assert.always(memorySize >= heapSize + calculateBitmapSize(heapSize));
        return heapSize;
    }

    /**
     * {@inheritDoc}
     */
    void initialize(Address permanentMemoryStart, Address memoryStart, Address memoryEnd) {

        Assert.always(memoryEnd.roundUpToWord().eq(memoryEnd));
        Assert.always(memoryStart.roundUpToWord().eq(memoryStart));
        Assert.always(permanentMemoryStart.roundUpToWord().eq(permanentMemoryStart));

        this.permanentMemoryStart = permanentMemoryStart;
        this.memoryStart = memoryStart;
        this.memoryEnd = memoryEnd;

        initializeHeap();

        int youngGenerationSize = getYoungGenerationSize();
        Address youngGenerationEnd = youngGenerationStart.add(youngGenerationSize);

        // Set up allocation to occur within the young space.
        GC.setAllocationParameters(heapStart, youngGenerationStart, youngGenerationEnd, heapEnd);

        // Output trace information.
        if (GC.GC_TRACING_SUPPORTED && GC.isTracing(GC.TRACE_BASIC)) {
            markingStack.setup(heapEnd, Lisp2Bitmap.getStart());
            traceVariables();
        }
    }

    /**
     * Initializes the heap, bitmap, marking stack and slice table based on the current memory
     * allocated to the collector.
     */
    private void initializeHeap() {

        // This is the size used for aligning the heap size. It corresponds
        // to the number of bytes covered by a single word in the bitmap.
        int alignment = HDR.BITS_PER_WORD * HDR.BYTES_PER_WORD;

        // Determine the size of the memory that must be partitioned into the
        // object heap, bitmap, slice table and minimum marking stack.
        heapStart = memoryStart.roundUp(alignment);
        final Address alignedMemoryEnd = memoryEnd.roundDown(alignment);
        int memorySize = alignedMemoryEnd.diff(heapStart).toInt();
        Assert.always(memorySize > 0, "insufficient memory for collector");

        // Determine the space reserved for the marking stack.
        final int minimumMarkingStackSize = MarkingStack.MINIMUM_MARKING_STACK_SIZE * HDR.BYTES_PER_WORD;

        /*
         * Configuring the heap involves solving a complex set of simultaneous equations
         * involving the heap size, bitmap size and slice table size. This is achieved by
         * the iterative process in the loop further below. To limit the number of iterations
         * required, an initial value for the heap size is calculated by
         * assuming that the slice table size will be 0.
         */
        heapSize = calculateMaxHeapSize(memorySize - minimumMarkingStackSize);
        Assert.always(heapSize >= alignment, "insufficient memory for collector");

        // Align the heap size
        heapSize = GC.roundUp(heapSize, alignment);
        heapEnd = heapStart.add(heapSize);
        Assert.always(heapSize > 0, "insufficient memory for collector");

        // The bitmap covers the range [permanentMemoryStart .. heapEnd]
        int bitmappedMemorySize = heapEnd.diff(permanentMemoryStart).toInt() + HDR.BYTES_PER_WORD;
        int bitmapSize = calculateBitmapSize(bitmappedMemorySize);

        boolean firstIteration = true;
        while (firstIteration || (heapSize + bitmapSize + sliceTableSize + minimumMarkingStackSize > memorySize)) {
            if (firstIteration) {
                firstIteration = false;
            } else {
                heapSize -= alignment;
            }

            // Determine the end of the heap
            heapEnd = heapStart.add(heapSize);

            // The bitmap covers the range [permanentMemoryStart .. heapEnd]
            bitmappedMemorySize = heapEnd.diff(permanentMemoryStart).toInt() + HDR.BYTES_PER_WORD;
            bitmapSize = calculateBitmapSize(bitmappedMemorySize);

            // Determine max number of bits needed for an offset (in words) to a heap object relative to heap start
            int heapOffsetBits = bitsRequiredFor(heapSize / HDR.BYTES_PER_WORD);

            // Determine number of bits needed for an offset (in words) to a class relative to the start of any of the
            // spaces in which a class may be located (i.e. ROM, NVM or RAM).
            int maxClassSpaceSize = computeMaxClassAddressSpace(heapSize);
            classOffsetBits = bitsRequiredFor(maxClassSpaceSize / HDR.BYTES_PER_WORD);

            // Determine mask used to extract class offset bits from an encoded class word
            classOffsetMask = ((1 << classOffsetBits) - 1) << ClassWordTag.BIT_COUNT;

            // Determine the amount by which an encoded class word must be logically right shifted to
            // extract the slice-relative offset (in words) to the forwarded object
            sliceOffsetShift = classOffsetBits + ClassWordTag.BIT_COUNT;

            // Determine how many remaining bits in an encoded class word will be used for a slice-relative offset
            sliceOffsetBits = (HDR.BITS_PER_WORD - sliceOffsetShift);

            // Configure the slice table based on whether or not a slice is smaller than the heap
            if (sliceOffsetBits < heapOffsetBits) {
                sliceSize = (1 << sliceOffsetBits) * HDR.BYTES_PER_WORD;
                sliceCount = (heapSize + (sliceSize - 1)) / sliceSize;
                sliceTableSize = sliceCount * HDR.BYTES_PER_WORD;
            } else {
                sliceOffsetBits = heapOffsetBits;
                sliceTableSize = HDR.BYTES_PER_WORD;
                sliceSize = heapSize;
                sliceCount = 1;
            }
        }

//            VM.print("sliceOffsetBits: "); VM.print(sliceOffsetBits); VM.println();
//            VM.print("sliceTableSize: "); VM.print(sliceTableSize); VM.println();
//            VM.print("sliceSize: "); VM.print(sliceSize); VM.println();
//            VM.print("sliceCount: "); VM.print(sliceCount); VM.println();
//            VM.print("classOffsetBits: "); VM.print(classOffsetBits); VM.println();
//            VM.print("classOffsetMask: "); VM.print(classOffsetMask); VM.println();
//            VM.print("sliceOffsetShift: "); VM.print(sliceOffsetShift); VM.println();
//            VM.print("heapOffsetBits: "); VM.print(bitsRequiredFor(heapSize / HDR.BYTES_PER_WORD)); VM.println();
            
        sliceTable = alignedMemoryEnd.sub(sliceTableSize);
        Address bitmap = sliceTable.sub(bitmapSize);

        idealYoungGenerationSizePercent = DEFAULT_YOUNG_GENERATION_PERCENT;
        youngGenerationStart = heapStart;

        Lisp2Bitmap.initialize(bitmap, bitmapSize, permanentMemoryStart);

        Assert.always(sliceTable.add(sliceTableSize).loeq(alignedMemoryEnd), "slice table overflows memory boundary");
        Assert.always(sliceOffsetBits < 32, "slice size overflows 32 bit address space");
        Assert.always(bitmap.add(bitmapSize).loeq(sliceTable), "bitmap collides with slice table");
        Assert.always(heapEnd.loeq(bitmap.sub(minimumMarkingStackSize)), "heap collides with marking stack");
        Assert.always((heapSize % alignment) == 0, "heap size is non-aligned");
        Assert.always((bitmapSize % HDR.BYTES_PER_WORD) == 0, "bitmap size is non-aligned");
        Assert.always((sliceTableSize % HDR.BYTES_PER_WORD) == 0, "slice table size is non-aligned");
        Assert.always(Lisp2Bitmap.getAddressOfBitmapWordFor(permanentMemoryStart).eq(Lisp2Bitmap.getStart()), "incorrect bitmap base");
        Assert.always(Lisp2Bitmap.getAddressOfBitmapWordFor(heapEnd).lo(Lisp2Bitmap.getEnd()), "incorrect bitmap base");
    }

    /**
     * Process a given command line option that may be specific to the collector implementation.
     *
     * @param arg   the command line option to process
     * @return      true if <code>arg</code> was a collector option
     */
    boolean processCommandLineOption(String arg) {
        if (arg.startsWith("-young:")) {
            int percent = Integer.parseInt(arg.substring("-young:".length()));
            if (percent < 10 || percent > 100) {
                System.err.println("Warning: ratio specified for young generation invalid");
            } else {
                idealYoungGenerationSizePercent = percent;
            }
            return true;
        } else {
            return super.processCommandLineOption(arg);
        }
    }

    /**
     * Prints the usage message for the collector specific options (if any).
     *
     * @param out  the stream on which to print the message
     */
    void usage(java.io.PrintStream out) {
        out.println("    -young:<n>            young space size as % of heap (default="+DEFAULT_YOUNG_GENERATION_PERCENT+"%)");
        super.usage(out);
    }

    /**
     * {@inheritDoc}
     */
    long freeMemory(Address allocationPointer) {
        return heapEnd.diff(allocationPointer).toPrimitive();
    }

    /**
     * {@inheritDoc}
     */
    long totalMemory() {
        return heapSize;
    }

    /**
     * Gets the actual size (in bytes) of the young generation based on the current start address
     * of the young generation, the ideal young generation size and the remaining
     * amount of free memory in the heap.
     *
     * @return the minimum of the ideal young generation size and the amount of memory
     *         between the current start of the young generation and the end of the heap
     */
    public int getYoungGenerationSize() {
        int size = getIdealYoungGenerationSize();
        int available = heapEnd.diff(youngGenerationStart).toInt();
        if (available < size) {
            Assert.that((available % HDR.BYTES_PER_WORD) == 0);
            return available;
        } else {
            Assert.that((size % HDR.BYTES_PER_WORD) == 0);
            return size;
        }
    }

    /**
     * Calculates one amount as a percent of another amount.
     *
     * @return <code>part</code> as a percentage of <code>total</code>
     */
    static int asPercentOf(long part, long total) {
        // Take care of long multiplication overflow
        if (part > (Long.MAX_VALUE/100)) {
            total /= 100;
            part /= 100;
        }
        return (total == 0 ? 0 : (int)((part * 100) / total));
    }

    /**
     * Gets a size for the young generation based on its ideal ratio to the heap size.
     *
     * @return the ideal young generation size
     */
    public int getIdealYoungGenerationSize() {
        // Calculate percentage avoiding integer overflow
        return GC.roundDown((heapSize / 100) * idealYoungGenerationSizePercent, HDR.BYTES_PER_WORD);
    }
    
    /**
     * Sets the size for the young generation based on its ideal ratio to the heap size.
     *
     * @param ygPct the new young generation percent
     */
     public void setIdealYoungGenerationSizePercent(int ygPct){
        idealYoungGenerationSizePercent = ygPct;
    } 

    /**
     * Determines if the current collection is a full collection. That is, is it processing the
     * full heap as opposed to just the young generation.
     *
     * @return true if the current collection is a full collection
     */
    private boolean isFullCollection() throws AllowInlinedPragma {
        return heapStart.eq(collectionStart);
    }

    /**
     * {@inheritDoc}
     */
    void dumpTimings(java.io.PrintStream out) {
        fullCollectionTimer.dump(out, true, GC.getFullCount());
        partialCollectionTimer.dump(out, false, GC.getPartialCount());
/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//        if (GC.GC_TRACING_SUPPORTED) {
//            out.println("Object graph copying:");
//            copyTimer.dump(out, "    ", false);
//        }
/*end[ENABLE_ISOLATE_MIGRATION]*/
    }

    /*---------------------------------------------------------------------------*\
     *                               Collection                                  *
    \*---------------------------------------------------------------------------*/

    /**
     * The timing statistics related to garbage collection.
     */
    private final Timer fullCollectionTimer;
    private final Timer partialCollectionTimer;

/*if[JAVA5SYNTAX]*/
    @Vm2c(root="collectGarbage")
/*end[JAVA5SYNTAX]*/
    boolean collectGarbageInJava (Address allocTop, boolean forceFullGC) {

        // Set up the limits of the space to be collected.
        if (nextCollectionIsFull || forceFullGC) {
            collectionStart = heapStart;
            nextCollectionIsFull = false;
        } else {
            collectionStart = youngGenerationStart;
        }
        collectionEnd = allocTop;
        numBytesLastScanned = collectionEnd.diff(collectionStart).toPrimitive();

        // Chooses the relevant timer
        timer = (isFullCollection() ? fullCollectionTimer : partialCollectionTimer);
        long start = timer.reset();

        // Sets up the marking stack.
        markingStack.setup(collectionEnd, Lisp2Bitmap.getStart());

        // Reset the marking recursion level.
        markingRecursionLevel = MAX_MARKING_RECURSION;
        remarkPasses = 0;

        // Output trace information.
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            if (isFullCollection()) {
                VM.println("Lisp2GenerationalCollector::collectGarbage - performing full collection");
            } else {
                VM.println("Lisp2GenerationalCollector::collectGarbage - performing partial collection");
            }
            traceVariables();
        }
        if ((HEAP_TRACE || GC.GC_TRACING_SUPPORTED) && GC.isTracing(GC.TRACE_HEAP_BEFORE_GC)) {
            traceHeap("Before collection", allocTop);
        }

        // Phase 0: Deregistered any remaining unused stack chunks...
        findUnusedStackChunks();
        
        // Clears the bitmap corresponding to the collection area. Must clear one past the
        // end of the collection area as there can be an object there that has a zero length body.
        Lisp2Bitmap.clearBitsFor(collectionStart, collectionEnd.add(HDR.BYTES_PER_WORD));

/*if[DEBUG_CODE_ENABLED]*/
        // Verify that the write barrier is correct
        if (!isFullCollection()) {
            verifyWriteBarrier(heapStart, collectionStart);
        }
        verifyStackChunks();
/*end[DEBUG_CODE_ENABLED]*/

        timer.finish(Timer.SETUP);

        // Phase 1: Mark objects transitively from roots
        mark();

        // Phase 2: Insert forward pointers in unused near object bits
        // if doing a full collection (but not one forced by user), allow some slop in the dense prefix
        Offset slopAllowed = Offset.zero(); //disable slop code.
        
        //TODO: fix slop code. Still not working correctly
        // slopAllowed = Offset.fromPrimitive(GC.roundDownToWord(collectionEnd.diff(collectionStart).toInt() / 100)); // 1%
        computeAddresses((forceFullGC || !isFullCollection()) ? Offset.zero() : slopAllowed);
        
        if (!firstMovingBlock.isZero()) {

            // Phase 3: Adjust interior pointers using forward pointers from phase2
            updatePointers();

            // Phase 4: Compact
            youngGenerationStart = compactObjects(firstDeadBlock, firstMovingBlock);
/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
            if (movedOopMaps != 0) {
                fixupOopMaps(heapStart, youngGenerationStart, movedOopMaps);
                movedOopMaps = 0;
            }
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/
        } else {
            youngGenerationStart = lastDeadBlock;
        }

        timer.reset();

        // Clear all the bits in the write barrier
        Lisp2Bitmap.clearBitsFor(permanentMemoryStart, youngGenerationStart);

        // Zap the free space with deadbeefs
        VM.deadbeef(youngGenerationStart, collectionEnd);

        // Set the main RAM allocator to the young generation.
        int youngGenerationSize = getYoungGenerationSize();
        allocTop = youngGenerationStart.add(youngGenerationSize);
        GC.setAllocationParameters(heapStart, youngGenerationStart, allocTop, heapEnd);

        // The next run of the collector will collect the whole heap if the actual size of the young
        // generation is smaller than the ideal size of the young generation.
        nextCollectionIsFull = youngGenerationSize < getIdealYoungGenerationSize();

        // Output trace information.
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            traceVariables();
        }
        if ((HEAP_TRACE || GC.GC_TRACING_SUPPORTED) && GC.isTracing(GC.TRACE_HEAP_AFTER_GC)) {
            traceHeap("After collection", youngGenerationStart);
        }

/*if[DEBUG_CODE_ENABLED]*/
        // Verify that the memory looks well formed
        verifyStackChunks();
        verifyObjectMemory(permanentMemoryStart, memoryStart);
        verifyObjectMemory(heapStart, youngGenerationStart);
/*end[DEBUG_CODE_ENABLED]*/

        timer.updateMaxMin(timer.finish(Timer.FINALIZE) - start);

        return isFullCollection();
    }

    void verbose() {
        VM.print(", Re-Mark passes: ");
        VM.print(remarkPasses);
    }

/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
    /**
     * Resets the 'oopMapWord' field of all the classes in the heap whose oop map was moved. This
     * field was used to record the old location of the oop map which is required so that instances
     * of the class can have their pointers traversed in the 'updatePointers' phase.
     *
     * @param start        the start address of the heap
     * @param end          the address one past the last valid object in the heap
     * @param movedOopMaps the number of classes to fix up
     */
    private void fixupOopMaps(Address start, Address end, int movedOopMaps) {

        timer.reset();

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::fixupOopMaps --------------- Start");
        }

        Address block;
        for (block = start; block.lo(end) && movedOopMaps != 0; ) {
            Address object = GC.blockToOop(block);
            if (GC.getKlass(object).getSystemID() == CID.KLASS) {
                Klass klass = VM.asKlass(object.toObject());
                if (!klass.isInterface() && !klass.isArray() && !klass.isSynthetic() && klass.getInstanceSize() > HDR.BITS_PER_WORD) {
                    if (NativeUnsafe.getUWord(klass, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord).ne(UWord.zero())) {
                        if (GC.GC_TRACING_SUPPORTED && tracing()) {
                            VM.print("Lisp2GenerationalCollector::fixupOopMaps - resetting 'oopMapWord' field in ");
                            VM.println(klass.getInternalName());
                        }
                        NativeUnsafe.setUWord(klass, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord, UWord.zero());
                        movedOopMaps--;
                    }
                }
            }
            block = object.add(GC.getBodySize(GC.getKlass(object), object));
        }

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::fixupOopMaps --------------- End");
        }

        timer.finish(Timer.FIXUP_OOPMAPS);
    }
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/

    /**
     * {@inheritDoc}
     */
    void postCollection() {
        timer.reset();
/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//      postProcessFinalizers();
/*end[FINALIZATION]*/
        postProcessWeakReferences();
        timer.finish(Timer.POST);
    }

/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//    /**
//     * Process the global queue of finalizers after a collection to remove the finalizers for
//     * objects that were determined to be unreferenced during the collection.
//     */
//    private void postProcessFinalizers() {
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.println("Lisp2GenerationalCollector::postProcessFinalizers --------------- Start");
//        }
//
//        Finalizer entry = finalizers;
//        finalizers = null;
//
//        while (entry != null) {
//
//            Finalizer next = entry.getNext();
//            boolean referenced = entry.isReferenced();
//            entry.setReferenced(false);
//            if (referenced) {
//                addFinalizer(entry);
//            } else {
//                entry.queueToIsolate();
//            }
//
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.print("Lisp2GenerationalCollector::postProcessFinalizers -- finalizer = ");
//                VM.printAddress(entry);
//                VM.print(" object = ");
//                VM.printAddress(entry.getObject());
//                VM.print(" class = ");
//                VM.print(Klass.getInternalName(GC.getKlass(Address.fromObject(entry.getObject()))));
//                VM.print(" referenced = ");
//                VM.print(referenced);
//                VM.println();
//            }
//            entry = next;
//        }
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.println("Lisp2GenerationalCollector::postProcessFinalizers --------------- End");
//            VM.println();
//        }
//    }
/*end[FINALIZATION]*/

    /**
     * Remove any stale entries in the global references list
     */
    private void postProcessWeakReferences() {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
             VM.println("Lisp2GenerationalCollector::postProcessWeakReferences --------------- Start");
         }

         Ref ref = references;
         references = null;

         while (ref != null) {
             if (GC.GC_TRACING_SUPPORTED && tracing()) {
                 VM.println("Lisp2GenerationalCollector::postProcessWeakReferences -- processing weak reference ");
             }

             Ref next = ref.next;

             // Remove entries where the referent is null
             if (!ref.referent.isZero()) {

                 if (GC.GC_TRACING_SUPPORTED && tracing()) {
                     VM.print("Lisp2GenerationalCollector::postProcessWeakReferences -- kept weak reference @ ");
                     VM.printAddress(ref);
                     VM.print(" to ");
                     VM.printAddress(ref.referent);
                     VM.println(" in queue");
                 }
                 addWeakReference(ref);
             } else {
                 if (GC.GC_TRACING_SUPPORTED && tracing()) {
                     VM.print("Lisp2GenerationalCollector::postProcessWeakReferences -- removed weak reference @ ");
                     VM.printAddress(ref);
                     VM.println(" from queue");
                 }
             }

             ref = next;
         }
         if (GC.GC_TRACING_SUPPORTED && tracing()) {
             VM.println("Lisp2GenerationalCollector::postProcessWeakReferences --------------- End");
             VM.println();
        }
    }

    /*---------------------------------------------------------------------------*\
     *                          Stack chunk tracking                             *
    \*---------------------------------------------------------------------------*/

    /**
     * Determines whether or not a given stack chunk is present in a given list of stack chunks.
     *
     * @param haystack the head of a stack chunk list
     * @param needle   the stack chunk to find
     * @return true if <code>needle</code> is present in the list headed by <code>haystack</code>
     */
    private boolean findStackChunk(Object haystack, Object needle) {
        while (haystack != null) {
            GC.checkSC(haystack);
            if (haystack == needle) {
                return true;
            }
            haystack = NativeUnsafe.getObject(haystack, SC.next);
        }
        return false;
    }

    /**
     * Links a stack chunk into the registered list of stack chunks.
     * The stack chunk must be live (i.e. the pointer to its owning thread must not be null).
     */
    private void registerStackChunk(Object chunk) {
        Assert.always(NativeUnsafe.getObject(chunk, SC.owner) != null);
        Assert.always(NativeUnsafe.getAddress(chunk, SC.next).isZero());
        GC.checkSC(chunk);

        Assert.that(!findStackChunk(stackChunks, chunk), "stack chunk already exists in list of stack chunks");
        NativeUnsafe.setObject(chunk, SC.next, stackChunks);
        stackChunks = Address.fromObject(chunk);
//VM.print("Lisp2GenerationalCollector::registerStackChunk - added chunk ");
//VM.printAddress(chunk);
//VM.print(" bcount=");
//VM.println(VM.branchCount());
    }

    /**
     * {@inheritDoc}
     */
    void registerStackChunks(Object sc) {
        verifyStackChunks();
        while (sc != null) {
            Object next = NativeUnsafe.getObject(sc, SC.next);
            NativeUnsafe.setObject(sc, SC.next, null);
            registerStackChunk(sc);
            sc = next;
        }
        verifyStackChunks();
    }

    /**
     * {@inheritDoc}
     */
    void deregisterStackChunk(Object chunk) {
        verifyStackChunks();
        Assert.that(chunk != null);
        Assert.that(stackChunks != null);
        GC.checkSC(chunk);
        if (chunk == stackChunks) {
            stackChunks = NativeUnsafe.getObject(chunk, SC.next);
        } else {
            Object prev = stackChunks;
            while (NativeUnsafe.getObject(prev, SC.next) != chunk) {
                GC.checkSC(prev);
                prev = NativeUnsafe.getObject(prev, SC.next);
                Assert.that(prev != null);
            }
            NativeUnsafe.setObject(prev, SC.next, NativeUnsafe.getObject(chunk, SC.next));
        }
        NativeUnsafe.setObject(chunk, SC.next, null);
        NativeUnsafe.setAddress(chunk, SC.owner, null);
        NativeUnsafe.setAddress(chunk, SC.lastFP, Address.zero());
        verifyStackChunks();
    }

    /*---------------------------------------------------------------------------*\
     *                               OopVisitor                                  *
    \*---------------------------------------------------------------------------*/

    private final static int MARK_VISITOR = 0;
    private final static int UPDATE_VISITOR = 1;
    private final static int VERIFY_VISITOR = 2;
    private final static int WRITE_BARRIER_VERIFY_VISITOR = 3;

    /**
     * Mark an object referred to by the pointer at base + offset.
     * @param base
     * @param offset
     */
    private void markOop(Address base, int offset) throws AllowInlinedPragma {
        Address object = NativeUnsafe.getAddress(base, offset);
        if (!object.isZero()) {
            markObject(object);
        }
    }

    /**
     * Visit an object pointer.
     *
     * @param base   the base address
     * @param offset the offset (in words) from <code>base</code> of the oop to visit
     * @return the value of the oop after visit is complete
     */
    private void visitOop(int visitor, Address base, int offset)
/*if[!DEBUG_CODE_ENABLED]*/
            throws AllowInlinedPragma
/*end[DEBUG_CODE_ENABLED]*/
    {
        Address pointerAddress;
        Address object;
        Assert.that(GC.inRam(base, base));
        
        switch (visitor) {
            case MARK_VISITOR: {
                markOop(base, offset);
                return;
            }
            case UPDATE_VISITOR: {
                updateOop(base, offset);
                return;
            }
/*if[DEBUG_CODE_ENABLED]*/
            case VERIFY_VISITOR: {
                object = NativeUnsafe.getAddress(base, offset);
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("ObjectMemoryVerificationVisitor::visitOop - ");
                    VM.printAddress(base);
                    VM.print(" @ ");
                    VM.print(offset);
                    VM.print(" = ");
                    VM.printAddress(object);
                }

                if (!object.isZero()) {

                    Klass klass = GC.getKlass(object);
                    Assert.always(GC.getKlass(klass).getSystemID() == CID.KLASS, "class of referenced object is invalid");

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.print(" (");
                        VM.print(klass.getInternalName());
                        VM.print(")");
                    }
                }

                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.println();
                }
                return;
            }
            case WRITE_BARRIER_VERIFY_VISITOR: {
                object = NativeUnsafe.getAddress(base, offset);
                pointerAddress = base.add(offset * HDR.BYTES_PER_WORD);

                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("WriteBarrierVerificationVisitor::visitOop - ");
                    VM.printAddress(base);
                    VM.print(" @ ");
                    VM.print(offset);
                    VM.print(" = ");
                    VM.printAddress(object);
                }

                if (!object.isZero()) {
                    Assert.always(!inCollectionSpace(object) || Lisp2Bitmap.testBitFor(pointerAddress), "write barrier bit not set as expected");
                }

                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.println();
                }
                return;
            }

            default:
                Assert.shouldNotReachHere("illegal oop traversal phase");
/*end[DEBUG_CODE_ENABLED]*/
        }
    }

    /**
     * Visit object pointers in an array
     *
     * @param base   the base address
     * @param offset the offset (in words) from <code>base</code> of the oop to visit
     * @return the value of the oop after visit is complete
     */
    private void visitOops(int visitor, Address base, int len) {
        Address pointerAddress;
        Address object;
        Assert.that(GC.inRam(base, base));
        
        switch (visitor) {
            case MARK_VISITOR: {
                for (int i = 0; i < len; i++) {
                    markOop(base, i);
                }
                return;
            }
            case UPDATE_VISITOR: {
                for (int i = 0; i < len; i++) {
                    updateOop(base, i);
                }
                return;
            }
/*if[DEBUG_CODE_ENABLED]*/
            case VERIFY_VISITOR: {
                for (int i = 0; i < len; i++) {
                    object = NativeUnsafe.getAddress(base, i);
                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.print("ObjectMemoryVerificationVisitor::visitOops - ");
                        VM.printAddress(base);
                        VM.print(" @ ");
                        VM.print(i);
                        VM.print(" = ");
                        VM.printAddress(object);
                    }

                    if (!object.isZero()) {

                        Klass klass = GC.getKlass(object);
                        Assert.always(GC.getKlass(klass).getSystemID() == CID.KLASS, "class of referenced object is invalid");

                        if (GC.GC_TRACING_SUPPORTED && tracing()) {
                            VM.print(" (");
                            VM.print(klass.getInternalName());
                            VM.print(")");
                        }
                    }

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.println();
                    }
                }
                return;
            }
            case WRITE_BARRIER_VERIFY_VISITOR: {
                for (int i = 0; i < len; i++) {
                    object = NativeUnsafe.getAddress(base, i);
                    pointerAddress = base.add(i * HDR.BYTES_PER_WORD);

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.print("WriteBarrierVerificationVisitor::visitOops - ");
                        VM.printAddress(base);
                        VM.print(" @ ");
                        VM.print(i);
                        VM.print(" = ");
                        VM.printAddress(object);
                    }

                    if (!object.isZero()) {
                        Assert.always(!inCollectionSpace(object) || Lisp2Bitmap.testBitFor(pointerAddress), "write barrier bit not set as expected");
                    }

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.println();
                    }
                }
                return;
            }

            default:
                Assert.shouldNotReachHere("illegal oop traversal phase");
/*end[DEBUG_CODE_ENABLED]*/
        }
    }


    /**
     * Visits a pointer to within an object that will be moved. For example, to visit the returnIP.
     *
     * @param base      the base address
     * @param offset    the offset (in words) from <code>base</code> of the internal pointer
     * @param object    the object into which the internal pointer points
     */
    private void visitInternalPointer(int visitor, Address base, int offset, Address object) {

        Address ipointer;
        Address destination;

        switch (visitor) {
            case MARK_VISITOR: {
                break;
            }
            case UPDATE_VISITOR: {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    indentTrace();
                    VM.print("Lisp2GenerationalCollector::UpdateVisitor::visitInternalPointer - object = ");
                    VM.printAddress(object);
                    VM.print(" ipointer = ");
                    VM.printAddress(NativeUnsafe.getAddress(base, offset));
                }

                if (isForwarded(object)) {
                    destination = getForwardedObject(object);
                    Offset delta = object.diff(destination);
                    ipointer = NativeUnsafe.getAddress(base, offset);
                    ipointer = ipointer.subOffset(delta);
                    NativeUnsafe.setAddress(base, offset, ipointer);

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.print(" -> ");
                        VM.printAddress(ipointer);
                        VM.print(", delta = ");
                        VM.printOffset(delta);
                        VM.println();
                    }
                } else {
                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.println(" [no update]");
                    }
                }

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//                if (copyingObjectGraph) {
//                    // Update the oop map
//                    Address pointerAddress = base.add(offset * HDR.BYTES_PER_WORD);
//                    recordPointer(pointerAddress);
//                }
/*end[ENABLE_ISOLATE_MIGRATION]*/
                break;
            }
/*if[DEBUG_CODE_ENABLED]*/
            case VERIFY_VISITOR: {
                ipointer = NativeUnsafe.getAddress(base, offset);
                Offset ioffset = ipointer.diff(object);
                Klass klass = GC.getKlass(object);
                int size = GC.getBodySize(klass, object);

                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("ObjectMemoryVerificationVisitor::visitInternalPointer - ");
                    VM.printAddress(base);
                    VM.print(" @ ");
                    VM.print(offset);
                    VM.print(" = ");
                    VM.printAddress(ipointer);
                    VM.print(" (");
                    VM.printAddress(object);
                    VM.print(" + ");
                    VM.printOffset(ioffset);
                    VM.println(")");
                }

                Assert.always(size > ioffset.toPrimitive(), "internal pointer points outside of object");
                break;
            }
            case WRITE_BARRIER_VERIFY_VISITOR: {
                break;
            }
            default:
                Assert.shouldNotReachHere("illegal oop traversal phase");
/*end[DEBUG_CODE_ENABLED]*/
        }
    }

    /*---------------------------------------------------------------------------*\
     *                        Shared traversal routines                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Indents a trace line according to the current object graph traversal recursion level.
     */
    private void indentTrace() {
        int level = MAX_MARKING_RECURSION - markingRecursionLevel;
        for (int i = 0; i != level; ++i) {
            VM.print("  ");
        }
    }

    /**
     * Traverses all the oops within an object.
     *
     * @param object   the object being traversed
     * @param klass    the class of <code>object</code>. If the class has been forwarded, then this is its pre-forwarding address.
     * @param visitor  the visitor to apply to each pointer in <code>object</code>
     */
    private void traverseOopsInObject(Address object, Address klass, int visitor) {

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            indentTrace();
            VM.print("Lisp2GenerationalCollector::traverseOopsInObject - object = ");
            VM.printAddress(object);
            VM.print(" class = ");
            printKlassName(klass.toObject());
            VM.println();
        }

        // The class/ObjectAssociation pointer is handled specially
        if (visitor == MARK_VISITOR) {
            markOop(object, HDR.klass);
        } else {
            Assert.that(visitor == UPDATE_VISITOR);
            if (isForwarded(object)) {
/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
                updateClassPointerIfClassIsForwarded(object);
/*else[ENABLE_DYNAMIC_CLASSLOADING]*/
//              Assert.that(!isClassPointer(object, getClassOrAssociationFromForwardedObject(object))
//                          || !isForwarded(getClassOrAssociationFromForwardedObject(object))); // class is not forwarded if no DYNAMIC_CLASSLOADING
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/
            } else {
                // Update the pointer to a forwarded ObjectAssociation
                // so that it doesn't have to be done in compactObjects
                updateOAPointerIfOAIsForwarded(object);
            }
        }

        // Visit the rest of the pointers
        if (Klass.isSquawkArray(VM.asKlass(klass))) { // any kind of array
            traverseOopsInArrayObject(object, klass, visitor);
        } else {
            traverseOopsInNonArrayObject(object, klass, visitor);
        }
    }

    /**
     * Traverses all the oops within an array object.
     *
     * @param object   the object being traversed
     * @param klass    the class of <code>object</code>. If the class has been forwarded, then this is its pre-forwarding address.
     * @param visitor  the visitor to apply to each pointer in <code>object</code>
     */
    private void traverseOopsInArrayObject(Address object, Address klass, int visitor) throws NotInlinedPragma {
        switch (Klass.getSystemID(VM.asKlass(klass))) {
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
                visitOop(visitor, object, HDR.methodDefiningClass); // this won't happen if not ENABLE_DYNAMIC_CLASSLOADING
                break;
            }
            case CID.GLOBAL_ARRAY: {
                Klass gaklass = VM.asKlass(NativeUnsafe.getObject(object, CS.klass));
                if (gaklass == null) { // This can occur if a GC occurs when a class state is being allocated
                    Assert.that(NativeUnsafe.getObject(object, CS.next) == null); // The 'next' pointer should not yet be set either
                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.println("Lisp2GenerationalCollector::updateOops GLOBAL_ARRAY with null CS.klass not scanned");
                    }
                } else {
                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        indentTrace();
                        VM.print("Lisp2GenerationalCollector::traverseOopsInObject - globals for ");
                        printKlassName(gaklass);
                        VM.println();
                    }
                    
                    if (visitor == MARK_VISITOR) {
                        CS.check(object.toObject());
                    }
                    // All the pointer static fields precede the non-pointer fields
                    int end = CS.firstVariable + Klass.getRefStaticFieldsSize(gaklass);
                    Assert.that(GC.inRam(object, object));
                    visitOops(visitor, object, end);
                }
                break;
            }
            case CID.LOCAL_ARRAY: {
/*if[DEBUG_CODE_ENABLED]*/
                if (NativeUnsafe.getObject(object, SC.owner) == null) {
                    // should only be null for the service thread.
                    VM.print("traverseOopsInStackChunk on service stack: ");
                    VM.printAddress(object);
                    VM.println();
                }
/*end[DEBUG_CODE_ENABLED]*/
                traverseOopsInStackChunk(object, visitor, true);

                if (visitor == UPDATE_VISITOR) {
                    if (!copyingObjectGraph) {
                        if (isForwarded(object)) {
                            updateInternalStackChunkPointers(object, getForwardedObject(object));
                        }
                    }
                }
                break;
            }
            default: { // Pointer array
                int length = GC.getArrayLengthNoCheck(object);
                Assert.that(GC.inRam(object, object));
                visitOops(visitor, object, length);
                break;
            }
        }
    }

    /**
     * Traverses all the oops within a non-array object.
     *
     * @param object   the object being traversed
     * @param klass    the class of <code>object</code>. If the class has been forwarded, then this is its pre-forwarding address.
     * @param visitor  the visitor to apply to each pointer in <code>object</code>
     */
    private void traverseOopsInNonArrayObject(Address object, Address klass, int visitor) {

        Address oopMap;
        UWord oopMapWord;

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//        if (copyingObjectGraph) {
//            // If the object is a hashtable and we are doing a graph copy then
//            // zero the first oop which is the transient field called entryTable.
//            if(Klass.isSubtypeOf(VM.asKlass(klass), HashTableKlass)) {
//                NativeUnsafe.setAddress(object, (int)FieldOffsets.com_sun_squawk_util_SquawkHashtable$entryTable, Address.zero());
//            }
//        }
/*end[ENABLE_ISOLATE_MIGRATION]*/

/*if[TRUSTED]*/
        Assert.shouldNotReachHere("need to extend test below to catch instances of TrustedKlass");
/*end[TRUSTED]*/

/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
        if (visitor == UPDATE_VISITOR && !copyingObjectGraph) {
            if (Klass.getSystemID(VM.asKlass(klass)) == CID.KLASS) {
                oopMap = NativeUnsafe.getAddress(object, (int)FieldOffsets.com_sun_squawk_Klass$oopMap);
                if (!oopMap.isZero() && isForwarded(oopMap)) {
                    /*
                     * Need to keep a pointer to the original location of the oop map for a class whose
                     * oop map will be moved
                     */
//VM.print("Testing oopMapWord at ");
//VM.printAddress(object);
//VM.print(" @ ");
//VM.print((int)FieldOffsets.com_sun_squawk_Klass$oopMapWord);
//VM.print(" = ");
//VM.printUWord(NativeUnsafe.getUWord(object, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord));
//VM.println();
                    Assert.that(NativeUnsafe.getUWord(object, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord).eq(UWord.zero()), "oopMapWord not zero'ed by last collection");
//VM.print("Setting oopMapWord for ");
//VM.print(Klass.getInternalName(VM.asKlass(object)));
//VM.print(" to ");
//VM.printAddress(oopMap);
//VM.println();
                    NativeUnsafe.setUWord(object, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord, oopMap.toUWord());
                    movedOopMaps += 1;
                }
            }
        }
/*else[ENABLE_DYNAMIC_CLASSLOADING]*/
//    Assert.that((visitor != UPDATE_VISITOR) || copyingObjectGraph || Klass.getSystemID(VM.asKlass(klass)) != CID.KLASS
//            || NativeUnsafe.getAddress(object, (int)FieldOffsets.com_sun_squawk_Klass$oopMap).isZero()
//            || !isForwarded(NativeUnsafe.getAddress(object, (int)FieldOffsets.com_sun_squawk_Klass$oopMap)));
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/

        int instanceSize = Klass.getInstanceSize(VM.asKlass(klass));
        oopMapWord = NativeUnsafe.getUWord(klass, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord);
        if (instanceSize > HDR.BITS_PER_WORD) {
            if (visitor == UPDATE_VISITOR && !oopMapWord.isZero()) {
                Assert.that(!copyingObjectGraph, "oop map backup pointer should not be made during an object graph copy ");
                /*
                 * This means that the oop map itself was forwarded and the pointer to it has already been updated.
                 * The old location of the oop map is stored in the (otherwise unused) oopMapWord field.
                 */
                oopMap = Address.fromPrimitive(oopMapWord.toPrimitive());
            } else {
                Assert.that(oopMapWord.isZero(), "oopMapWord not zero'ed by last collection");
                oopMap = NativeUnsafe.getAddress(klass, (int)FieldOffsets.com_sun_squawk_Klass$oopMap);
            }

            int oopMapLength = ((instanceSize + HDR.BITS_PER_WORD) - 1) / HDR.BITS_PER_WORD;
            for (int i = 0; i != oopMapLength; ++i) {
                int/*S64*/ word = NativeUnsafe.getUWord(oopMap, i).toPrimitive();
                traverseOopsForBitmapWord(object, visitor, word, i * HDR.BITS_PER_WORD);
            }
        } else {
            int/*S64*/ word = oopMapWord.toPrimitive();
            traverseOopsForBitmapWord(object, visitor, word, 0);
        }
    }

    /**
     * Updates the class pointer for a forwarded object if the class is forwarded and
     * there is no interleaving ObjectAssociation. This is required as objects are always at a
     * higher address than their class and so in the {@link #compactObjects compact objects}
     * phase, its class will already have been moved.
     *
     * @param object  an object whose (encoded) class pointer is to be updated if its class
     *                is forwarded and there is no interleaving ObjectAssociation
     */
    private void updateClassPointerIfClassIsForwarded(Address object) {
        Address classOrAssociation = getClassOrAssociationFromForwardedObject(object);
        if (isClassPointer(object, classOrAssociation)) {
            if (isForwarded(classOrAssociation)) {
                Address destination = getForwardedObject(object);
                NativeUnsafe.setAddress(object, HDR.klass, getForwardedObject(classOrAssociation));
                forwardObject(object, destination);
            }
        }
    }

    /**
     * Updates the pointer to an ObjectAssociation for an object that is not forwarded.
     *
     * @param object  an object whose class pointer is to be updated if it points to a forwarded ObjectAssociation
     */
    private void updateOAPointerIfOAIsForwarded(Address object) {
        Address classOrAssociation = NativeUnsafe.getAddress(object, HDR.klass);
        if (!isClassPointer(object, classOrAssociation)) {
            updateOop(object, HDR.klass);
        }
    }

    /**
     * Traverses the oops within an object whose bits are set in a given oop map word.
     *
     * @param object   the object being traversed
     * @param visitor  the visitor to apply to each pointer in <code>object</code>
     * @param word     a word from an oop map describing where the pointers are in <code>object</code>
     * @param offset   the offset of the first field in <code>object</code> mapped by <code>word</code>
     */
    private void traverseOopsForBitmapWord(Address object, int visitor, int/*S64*/ word, int offset) {
        while (word != 0) {
            if ((word & 1) != 0) {
                visitOop(visitor, object, offset);
            }
            offset++;
            word = word >>> 1;
        }
    }

    /**
     * Traverses the oops in a stack chunk.
     *
     * @param chunk    the stack chunk to traverse
     * @param visitor  the visitor to apply to each oop in the traversed objects
     * @param header   specifies if the header part of the stack chunk should be traversed
     */
    private void traverseOopsInStackChunk(Address chunk, int visitor, boolean header) throws NotInlinedPragma {
        GC.checkSC(chunk.toObject());
        Address fp = NativeUnsafe.getAddress(chunk, SC.lastFP);

        // Trace.
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println();
            indentTrace();
            VM.print("Lisp2GenerationalCollector::traverseOopsInStackChunk - chunk = ");
            VM.printAddress(chunk);
            VM.println();
        }

        /*
         * Traverse the pointers in the header part of the stack chunk
         */
        if (header) {
            if (visitor != MARK_VISITOR) {
                // A copied stack chunk must always be accessible from the list of copied stack chunks
/*if[DEBUG_CODE_ENABLED]*/
/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//                if (copyingObjectGraph) {
//                    Assert.always(visitor != UPDATE_VISITOR || findStackChunk(copiedStackChunks, chunk.toObject()));
//                }
/*end[ENABLE_ISOLATE_MIGRATION]*/
                visitOop(visitor, chunk, SC.next);
/*else[DEBUG_CODE_ENABLED]*/
//              Assert.that(UPDATE_VISITOR == visitor);
//              visitOop(UPDATE_VISITOR, chunk, SC.next);
/*end[DEBUG_CODE_ENABLED]*/

            } else {
                // The 'next' pointer is a weak reference and is
                // only explicitly traversed during marking
                // in 'markStackChunks()' if necessary
            }
            visitOop(visitor, chunk, SC.owner);
        }

        /*
         * Update the pointers in each activation frame
         */
        boolean isInnerMostActivation = true;
        while (!fp.isZero()) {
            traverseOopsInActivation(fp, isInnerMostActivation, visitor);
            fp = VM.getPreviousFP(fp);
            isInnerMostActivation = false;
        }

        // Trace.
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println();
        }
    }

    /**
     * Traverses the oops in an activation record.
     *
     * @param fp                     the frame pointer
     * @param isInnerMostActivation  specifies if this is the inner most activation frame on the chunk
     *                               in which case only the first local variable (i.e. the method pointer) is scanned
     * @param visitor                the visitor to apply to each traversed oop
     */
    private void traverseOopsInActivation(Address fp, boolean isInnerMostActivation, int visitor) {
        Address mp  = NativeUnsafe.getAddress(fp, FP.method);
        Address returnFP = VM.getPreviousFP(fp);
        Address returnIP = VM.getPreviousIP(fp);
        Assert.that(GC.inRam(fp, fp));

        /*
         * Trace.
         */
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            indentTrace();
            VM.print("Lisp2GenerationalCollector::traverseOopsInActivation - fp = ");
            VM.printAddress(fp);
            VM.print(" mp = ");
            VM.printAddress(mp);
            VM.println();
        }

        /*
         * Visit the return IP.
         */
        if (!returnIP.isZero()) {

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                indentTrace();
                VM.print("Lisp2GenerationalCollector::traverseOopsInActivation -- returnIP = ");
                VM.printAddress(returnIP);
                VM.println();
            }
            Assert.that(!returnFP.isZero(), "activation frame has null returnFP");
            Address returnMP = NativeUnsafe.getAddress(returnFP, FP.method);
/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
            visitInternalPointer(visitor, fp, FP.returnIP, returnMP);
/*else[ENABLE_DYNAMIC_CLASSLOADING]*/
//          Assert.that(!inHeap(returnIP));
//          Assert.that(!inHeap(returnMP)); // returnMP will never move, and will always be live.
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/
        } else {
            Assert.that(returnFP.isZero(), "returnFP should be null when returnIP is null");
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
            boolean isOop = ((bite >>> bitOffset)&1) != 0;
            if (isOop) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    indentTrace();
                    VM.print("Lisp2GenerationalCollector::traverseOopsInActivation -- parm at offset ");
                    VM.print(varOffset);
                    VM.println();
                }
                visitOop(visitor, fp, varOffset);
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
            boolean isOop = ((bite >>> bitOffset) & 1) != 0;
            if (isOop) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    indentTrace();
                    VM.print("Lisp2GenerationalCollector::traverseOopsInActivation -- local at offset ");
                    VM.print(varOffset);
                    VM.println();
                }
                visitOop(visitor, fp, varOffset);
            }
            varOffset--; // Locals go downwards
        }
    }

    /**
     * Traverses all the oops that in the old generation that have their write barrier bit set.
     *
     * @param start   the start address of the old generation
     * @param end     the end address of the old generation
     * @param visitor the visitor to apply to each pointer in the traversed objects
     */
    private void traverseWriteBarrierOops(Address start, Address end, int visitor) throws AllowInlinedPragma {
        Assert.that(GC.inRam(start, end));
        Lisp2Bitmap.Iterator.start(start, end, false);
        Address objectAddress;
        while (!(objectAddress = Lisp2Bitmap.Iterator.getNext()).isZero()) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                indentTrace();
                VM.print("Lisp2GenerationalCollector::traverseWriteBarrierOops -- visit oop at ");
                VM.printAddress(objectAddress);
                VM.println();
            }
            Assert.that(GC.inRam(objectAddress, objectAddress));
            visitOop(visitor, objectAddress, 0);
        }
    }

   /**
     * Mark all the oops that in the old generation that have their write barrier bit set.
     *
     * @param start   the start address of the old generation
     * @param end     the end address of the old generation
     */
    private void markWriteBarrierOops(Address start, Address end) throws NotInlinedPragma {
        traverseWriteBarrierOops(start, end, MARK_VISITOR);
    }

    /**
     * Mark all the oops that in the old generation that have their write barrier bit set.
     *
     * @param start   the start address of the old generation
     * @param end     the end address of the old generation
     */
    private void updateWriteBarrierOops(Address start, Address end) throws NotInlinedPragma {
        traverseWriteBarrierOops(start, end, UPDATE_VISITOR);
    }

    /*---------------------------------------------------------------------------*\
     *                            Marking routines                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Marks all the reachable objects in the collection area.
     */
    private void mark() {

        Assert.that(!copyingObjectGraph);
        timer.reset();

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** Start Lisp2GenerationalCollector::mark **********");
        }

        // Mark the objects reachable from the GC roots
        markRoots();

        // Mark the objects reachable from the old generation and the stack chunks
        if (!isFullCollection()) {

            markWriteBarrierRoots();

            // Mark the objects reachable from the stack chunks
            markStackChunks();
        }

        // Recursively mark from the already marked objects in the collection space
        markCollectionSpace();

/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//        // Process the finalizer queue
//        if (finalizers != null) {
//
//            // Set the flag in each finalizer specifying what to do with it after the collection
//            processFinalizers();
//
//            // Mark the finalizer queue
//            markObject(Address.fromObject(finalizers));
//
//            // Remark collection space
//            markCollectionSpace();
//        }
/*end[FINALIZATION]*/

        // Process the weak reference queue
        if (references != null) {

            // Mark the weak references whose referent will die
            processWeakReferences();

            // Mark the references queue
            markObject(Address.fromObject(references));

            // Remark collection space
            markCollectionSpace();
        }

        if (isFullCollection()) {
            // Prune the un-marked stack chunks
            pruneStackChunks();
        }

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** End Lisp2GenerationalCollector::mark **********");
            VM.println();
        }

        timer.finish(Timer.MARK);
    }

    private int remarkPasses;

    /**
     * Mark all the objects reachable from the already marked objects in the collection space.
     * This process repeats itself if there was an overflow on the marking stack.
     */
    private void markCollectionSpace() throws NotInlinedPragma {
        while (markingStack.hasOverflowed()) {
            remarkPasses = remarkPasses + 1;
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println("Lisp2GenerationalCollector::mark - remarking from marked objects after marking stack overflow");
            }
            markingStack.resetOverflow();
            Address object;
            Lisp2Bitmap.Iterator.start(collectionStart, collectionEnd, true);
            while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {
                traverseOopsInObject(object, Address.fromObject(GC.getKlass(object)), MARK_VISITOR);
                while (!(object = markingStack.pop()).isZero()) {
                    traverseOopsInObject(object, Address.fromObject(GC.getKlass(object)), MARK_VISITOR);
                }
            }
        }
    }

    /**
     * Marks all the objects in the collection space reachable from the GC roots.
     */
    private void markRoots() {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::markRoots --------------- Start");
        }
        for (int i = 0 ; i < VM.getGlobalOopCount() ; i++) {
            Address object = Address.fromObject(VM.getGlobalOop(i));
            if (!object.isZero()) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("Lisp2GenerationalCollector::markRoots - [");
                    VM.print(i);
                    VM.print("] = ");
                    VM.printAddress(object);
                    VM.println();
                }
                markObject(object);
            }
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::markRoots --------------- End");
            VM.println();
        }
    }

    /**
     * Marks the objects reachable from old generation if this is not a full GC.
     */
    private void markWriteBarrierRoots() {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::markWriteBarrierRoots --------------- Start");
        }
        markWriteBarrierOops(heapStart, collectionStart);
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::markWriteBarrierRoots --------------- End");
            VM.println();
        }
    }

    /**
     * Marks all the objects in the collection space reachable from the global list of stack chunks.
     */
    private void markStackChunks() throws NotInlinedPragma {
        Address chunk = Address.fromObject(stackChunks);
        Assert.that(!copyingObjectGraph);
        while (!chunk.isZero()) {
            GC.checkSC(chunk);

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::markStackChunks - chunk = ");
                VM.printAddress(chunk);
                VM.println();
            }

            // The stack chunk has already been traversed if it is in the collection
            // space and its mark bit is set otherwise it must be traversed now.
            if (!inCollectionSpace(chunk) || !Lisp2Bitmap.testBitFor(chunk)) {
                traverseOopsInStackChunk(chunk, MARK_VISITOR, true);
            } else {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.println("Lisp2GenerationalCollector::markStackChunks - chunk is already marked");
                }
            }
            chunk = NativeUnsafe.getAddress(chunk, SC.next);
        }
    }


/*if[DEBUG_CODE_ENABLED]*/
    private void verifyStackChunks() {
        Address sc = Address.fromObject(stackChunks);
        while (!sc.isZero()) {
            /*VM.print("verifyStackChunk: ");
            VM.printAddress(sc);
            VM.println();*/
            GC.checkSC(sc);
            sc = NativeUnsafe.getAddress(sc, SC.next);
        }
    }
/*else[DEBUG_CODE_ENABLED]*/
//    private void verifyStackChunks() throws AllowInlinedPragma {}
/*end[DEBUG_CODE_ENABLED]*/

    /** 
     * Prune stack chunks that have been marked as unused. Can be called in both partial and full collections.
     */
    private void findUnusedStackChunks() throws NotInlinedPragma {
        int keptCount = 0;
        int prunedCount = 0;
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::findUnsedStackChunks --------------- Start");
        }

        verifyStackChunks();
        Address sc = Address.fromObject(stackChunks);
        stackChunks = null;
        Address kept = Address.zero();
        Address next;

        while (!sc.isZero()) {
            GC.checkSC(sc);
            next = NativeUnsafe.getAddress(sc, SC.next);
            if (NativeUnsafe.getObject(sc, SC.owner) != null) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("Lisp2GenerationalCollector::findUnsedStackChunks - keep ");
                    VM.printAddress(sc);
//VM.print("  thread = ");
//VM.print(((VMThread)NativeUnsafe.getObject(sc, SC.owner)).getName());
                    VM.println();
                    keptCount++;
                }
                if (kept.isZero()) {
                    kept = sc;
                    NativeUnsafe.setAddress(sc, SC.next, Address.zero());
                } else {
                    NativeUnsafe.setAddress(sc, SC.next, kept);
                    Lisp2Bitmap.setBitFor(sc.add(SC.next));
                    kept = sc;
                }
            } else {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("Lisp2GenerationalCollector::findUnsedStackChunks - discard ");
                    VM.printAddress(sc);
//VM.print("  thread = ");
//VM.print(((VMThread)NativeUnsafe.getObject(sc, SC.owner)).getName());
                    VM.println();
                    prunedCount++;
                }

                // Tag the stack chunk as dead
                NativeUnsafe.setAddress(sc, SC.owner,  Address.zero());
                NativeUnsafe.setAddress(sc, SC.next,   Address.zero());
                NativeUnsafe.setAddress(sc, SC.lastFP, Address.zero());
            }

            sc = next;
        }

        stackChunks = kept;

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print("Lisp2GenerationalCollector::findUnsedStackChunks --------------- End  [pruned ");
            VM.print(prunedCount);
            VM.print(" of ");
            VM.print(prunedCount + keptCount);
            VM.println("]");
       }
       verifyStackChunks();
    }
    
    /**
     * Find un-referenced stack chunks.
     * Actually, I'm not sure if a stack chunk could still be "in-use, eg. owner field not null,
     * but unreferenced. but check anyway (dw 10-9-2006)
     */
    private void pruneStackChunks() throws NotInlinedPragma {
        Assert.always(isFullCollection());

        int keptCount = 0;
        int prunedCount = 0;
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::pruneStackChunks --------------- Start");
        }

        verifyStackChunks();
        Address sc = Address.fromObject(stackChunks);
        stackChunks = null;
        Address kept = Address.zero();
        Address next;

        while (!sc.isZero()) {
            GC.checkSC(sc);
            next = NativeUnsafe.getAddress(sc, SC.next);
            Assert.that(NativeUnsafe.getObject(sc, SC.owner) != null);
            if (Lisp2Bitmap.testBitFor(sc)) {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("Lisp2GenerationalCollector::pruneStackChunks - keep ");
                    VM.printAddress(sc);
//VM.print("  thread = ");
//VM.print(((VMThread)NativeUnsafe.getObject(sc, SC.owner)).getName());
                    VM.println();
                    keptCount++;
                }
                if (kept.isZero()) {
                    kept = sc;
                    NativeUnsafe.setAddress(sc, SC.next, Address.zero());
                } else {
                    NativeUnsafe.setAddress(sc, SC.next, kept);
                    kept = sc;
                }
            } else {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("Lisp2GenerationalCollector::pruneStackChunks - discard ");
                    VM.printAddress(sc);
//VM.print("  thread = ");
//VM.print(((VMThread)NativeUnsafe.getObject(sc, SC.owner)).getName());
                    VM.println();
                    prunedCount++;
                }

                // Tag the stack chunk as dead
                NativeUnsafe.setAddress(sc, SC.owner,  Address.zero());
                NativeUnsafe.setAddress(sc, SC.next,   Address.zero());
                NativeUnsafe.setAddress(sc, SC.lastFP, Address.zero());
            }

            sc = next;
        }

        stackChunks = kept;

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.print("Lisp2GenerationalCollector::pruneStackChunks --------------- End  [pruned ");
            VM.print(prunedCount);
            VM.print(" of ");
            VM.print(prunedCount + keptCount);
            VM.println("]");
        }
        verifyStackChunks();

    }

/*if[!FINALIZATION]*/
/*else[FINALIZATION]*/
//    /**
//     * Process the finalizer queue, setting the flag in each finalizer indicating if the associated
//     * object is referenced by something else besides the finalizer object. This flag will be used
//     * after the collector has finished to modify the global queue of pending finalizers and the
//     * per-isolate queue of finalizers.
//     */
//    private void processFinalizers() {
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.println("Lisp2GenerationalCollector::markFinalizers --------------- Start");
//        }
//
//        Finalizer entry = finalizers;
//        while (entry != null) {
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.println("Lisp2GenerationalCollector::markFinalizers -- processing finalizer ");
//            }
//
//            Address entryAddress = Address.fromObject(entry);
//            Assert.that(!inCollectionSpace(entryAddress) || !Lisp2Bitmap.testBitFor(entryAddress), "finalizer for object marked prematurely");
//
//            Finalizer next = entry.getNext();
//            Address object = Address.fromObject(entry.getObject());
//            boolean referenced = !inCollectionSpace(object) || Lisp2Bitmap.testBitFor(object);
//            entry.setReferenced(referenced);
//
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.print("Lisp2GenerationalCollector::markFinalizers -- finalizer = ");
//                VM.printAddress(entry);
//                VM.print(" object = ");
//                VM.printAddress(object);
//                VM.print(" class = ");
//                VM.print(Klass.getInternalName(GC.getKlass(Address.fromObject(entry.getObject()))));
//                VM.print(" referenced = ");
//                VM.print(referenced);
//                VM.println();
//            }
//            entry = next;
//        }
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.println("Lisp2GenerationalCollector::markFinalizers --------------- End");
//            VM.println();
//        }
//    }
/*end[FINALIZATION]*/

    /**
     * Process the weak reference queue. If an object to which the weak reference is pointing is
     * no longer live, clear the reference.  The global list will be updated post collection.
     */
    private void processWeakReferences() throws NotInlinedPragma {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::processWeakReferences --------------- Start");
        }

        Ref ref = references;
        while (ref != null) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.println("Lisp2GenerationalCollector::processWeakReferences -- processing weak reference ");
            }

            boolean keep = false;
            Address referent = ref.referent;
            Address refAddress = Address.fromObject(ref);

            if (!inCollectionSpace(refAddress) || Lisp2Bitmap.testBitFor(refAddress)) {
                // The Ref is reachable
                if (!inCollectionSpace(referent) || Lisp2Bitmap.testBitFor(referent)) {
                    // The referent is also reachable
                    keep = true;
                }
            }

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::processWeakReferences -- weak reference @ ");
                VM.printAddress(ref);
                VM.print(" to ");
                VM.printAddress(referent);

                if (keep) {
                    VM.println(" will be kept");
                } else {
                    VM.println(" will be removed");
                }
            }

            // Clear the referent to denote to postProcessWeakReferences() that the
            // Ref object should be removed from the global list of Ref objects
            if (!keep) {
                ref.referent = Address.zero();
            }

            // Move on to next object
            ref = ref.next;
        }

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::processWeakReferences --------------- End");
            VM.println();
        }
    }

    /**
     * Provides more detail for the mark phase. Should only be enabled when debugging the collector.
     */
    private static final boolean VERBOSE_MARK_OBJECT_TRACE = true;

    /**
     * Sets the mark bit for a given object if it is within the current collection
     * space. If the mark bit was not previously set for the object, then all of
     * it's pointer fields are also marked recursively with this operation.
     *
     * @param  object the object to (potentially) be marked
     */
    private void markObject(Address object) {

       /*
        * Ensure that the object is not forwarded
        */
        Assert.that(!isForwarded(object));

       /*
        * If the object is in the collection space and the corresponding bit
        * is not set then set it and traverse the object's pointers
        */
        if (inCollectionSpace(object)) {
            if (!Lisp2Bitmap.testAndSetBitFor(object)) {
                if (markingRecursionLevel == 0) {
                    if (VERBOSE_MARK_OBJECT_TRACE && GC.GC_TRACING_SUPPORTED && tracing()) {
                        indentTrace();
                        VM.print("Lisp2GenerationalCollector::markObject - object = ");
                        VM.printAddress(object);
                        VM.print(" class = ");
                        printKlassName(GC.getKlass(object));
                        VM.print("  {pushed on marking stack} ");
                        VM.print(VM.branchCount());
                        VM.println();
                    }
                    markingStack.push(object);
                    if (GC.GC_TRACING_SUPPORTED && tracing() && markingStack.hasOverflowed()) {
                        indentTrace();
                        VM.println("Lisp2GenerationalCollector::markObject - marking stack overflowed ");
                    }
                } else {
                    Klass klass = GC.getKlass(object);

/*if[DEBUG_CODE_ENABLED]*/
/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//                    // Run some assertions that will hopefully ensure isolation
//                    if (copyingObjectGraph) {
//                        assertIsolation(object, klass);
//                    }
/*end[ENABLE_ISOLATE_MIGRATION]*/
/*end[DEBUG_CODE_ENABLED]*/

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        indentTrace();
                        VM.print("Lisp2GenerationalCollector::markObject - object = ");
                        VM.printAddress(object);
                        VM.print(" class = ");
                        printKlassName(klass);
                        VM.println();
                    }
                    markingRecursionLevel -= 1;
                    traverseOopsInObject(object, Address.fromObject(klass), MARK_VISITOR);
                    while (!(object = markingStack.pop()).isZero()) {
                        klass = GC.getKlass(object);
                        traverseOopsInObject(object, Address.fromObject(klass), MARK_VISITOR);
                    }
                    markingRecursionLevel += 1;
                }
            } else {
                if (VERBOSE_MARK_OBJECT_TRACE && GC.GC_TRACING_SUPPORTED && tracing()) {
                    indentTrace();
                    VM.print("Lisp2GenerationalCollector::markObject - object = ");
                    VM.printAddress(object);
                    VM.print(" class = ");
                    printKlassName(GC.getKlass(object));
                    VM.println(" {already marked}");
                }
            }
        } else {
            if (VERBOSE_MARK_OBJECT_TRACE && GC.GC_TRACING_SUPPORTED && tracing()) {
                indentTrace();
                VM.print("Lisp2GenerationalCollector::markObject - object = ");
                VM.printAddress(object);
                VM.print(" class = ");
                printKlassName(GC.getKlass(object));
                VM.println(" {not in collection space}");
            }
        }
    }

    /*-----------------------------------------------------------------------*\
     *                         Compute Address Phase                         *
    \*-----------------------------------------------------------------------*/

    /**
     * Turn an object into a "safer", pointerless object. Used to turn "slop"
     * into innocuous objects.
     * @param object
     * @param size
     */
    private void killObject(Address object, int size) {
        Assert.that(size >= HDR.basicHeaderSize);
        Assert.that(size == GC.roundUpToWord(size));
        Suite bootstrapSuite = VM.getCurrentIsolate().getBootstrapSuite();
        if (size == HDR.basicHeaderSize) {
            //VM.println("killing by making object");
            GC.setHeaderClass(object, bootstrapSuite.getKlass(CID.OBJECT));
        } else {
            size -= HDR.arrayHeaderSize;
            //VM.print("killing by making byte array size ");
            //VM.print(size);
            //VM.println();
            GC.setHeaderClass(object, bootstrapSuite.getKlass(CID.BYTE_ARRAY));
            GC.setHeaderLength(object, size);
/*if[DEBUG_CODE_ENABLED]*/
            VM.deadbeef(object, object.add(size));
/*end[DEBUG_CODE_ENABLED]*/
        }
    }

    private Address firstDeadBlock;
    private Address lastDeadBlock;
    private Address firstMovingBlock;

    /**
     * Inner loop of computeAddresses used by collectGarbage().
     *
     * @param free where to start copying objects to
     * @param slopAllowed
     * @throws NotInlinedPragma
     */
    private void computeAddressesLoopGC(Address free, Offset slopAllowed) throws NotInlinedPragma {
        Address object;

        // Iterate over all the marked objects in the collection space
        Lisp2Bitmap.Iterator.start(collectionStart, collectionEnd, true);
        while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {

            Assert.that(!isForwarded(object));
            Klass klass = GC.getKlass(object);
            int headerSize = object.diff(GC.oopToBlock(klass, object)).toInt();

            Address objectDestination = free.add(headerSize);
            Offset delta = object.diff(objectDestination);
            int size = GC.getBodySize(klass, object);

            // Only install forwarding header if the object will actually move
            if (!delta.isZero()) {

                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("Lisp2GenerationalCollector::computeAddresses - object = ");
                    VM.printAddress(object);
                }

                if (slopAllowed.ge(delta)) {
                    slopAllowed = slopAllowed.sub(delta.toInt());
                    killObject(objectDestination, delta.toPrimitive());
					//Assert.that(klass == GC.getKlass(object)); // make sure we didn't stomp on this object
                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.print(" left dead object in place. Slop remaining = ");
                        VM.printOffset(slopAllowed);
                        VM.println();
                    }
                    objectDestination = object;
                } else {

                    forwardObject(object, objectDestination);
                    Assert.that(getForwardedObject(object).eq(objectDestination));

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.print(" destination = ");
                        VM.printAddress(objectDestination);
                        VM.print(" class = ");
                        printKlassName(klass);
                        VM.print(" delta = ");
                        VM.printOffset(delta);
                        VM.print(" size = ");
                        VM.print(size);
                        VM.print(" headerSize = ");
                        VM.print(headerSize);
                        VM.println();
                    }
                    if (firstDeadBlock.isZero()) {
                        firstDeadBlock = free;
                        firstMovingBlock = object.sub(headerSize);
                    }
                }
            } else {
                if (GC.GC_TRACING_SUPPORTED && tracing()) {
                    VM.print("Lisp2GenerationalCollector::computeAddresses - object = ");
                    VM.printAddress(object);
                    VM.print(" class = ");
                    printKlassName(klass);
                    VM.println(" [doesn't move]");
                }
            }

            free = objectDestination.add(size);
        }

        if (firstDeadBlock.isZero()) {
            firstDeadBlock = free;
            firstMovingBlock = null;
        }
        lastDeadBlock = free;

        // Only update weak references if this is a garbage collection.
        updateWeakReferences();
    }

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//    /**
//     * Inner loop of computeAddresses used by copyObjectGraph().
//     *
//     * @param free where to start copying objects to
//     * @return the end of the last object copied
//     * @throws NotInlinedPragma
//     */
//    private Address computeAddressesLoopCopyGraph(Address free) throws NotInlinedPragma {
//        Address object;
//        Address returnValue = Address.zero();
//
//        // Iterate over all the marked objects in the collection space
//        Lisp2Bitmap.Iterator.start(collectionStart, collectionEnd, true);
//        while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {
//
//            Assert.that(!isForwarded(object));
//            Klass klass = GC.getKlass(object);
//            int headerSize = object.diff(GC.oopToBlock(klass, object)).toInt();
//
//            Address objectDestination = free.add(headerSize);
//            Offset delta = object.diff(objectDestination);
//            int size = GC.getBodySize(klass, object);
//
//            Assert.that(!delta.isZero());
//
//            // Copy the object now so that a compactObjects phase is not required
//            Address block = object.sub(headerSize);
//            Address destinationBlock = objectDestination.sub(headerSize);
//
//            if (Klass.getSystemID(klass) != CID.LOCAL_ARRAY) {
//                int blockSize = headerSize + size;
//                Address destinationBlockEnd = destinationBlock.add(blockSize);
//                if (destinationBlockEnd.hi(heapEnd)) {
//                    Lisp2Bitmap.Iterator.terminate();
//                    timer.finish(Timer.COMPUTE_ADDRESSES);
//                    return Address.zero();
//                } else {
//                    returnValue = destinationBlockEnd;
//                }
//                VM.copyBytes(block, 0, destinationBlock, 0, blockSize, false);
///*if[TYPEMAP]*/
//                if (VM.usingTypeMap()) {
//                    NativeUnsafe.copyTypes(block, destinationBlock, blockSize);
//                }
///*end[TYPEMAP]*/
//            } else {
//                size = copyStackChunk(object, size, objectDestination);
//                if (size == -1) {
//                    Lisp2Bitmap.Iterator.terminate();
//                    timer.finish(Timer.COMPUTE_ADDRESSES);
//                    return Address.zero();
//                }
//            }
//
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.print("Lisp2GenerationalCollector::computeAddresses - object = ");
//                VM.printAddress(object);
//            }
//
//            forwardObject(object, objectDestination);
//            Assert.that(getForwardedObject(object).eq(objectDestination));
//
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.print(" destination = ");
//                VM.printAddress(objectDestination);
//                VM.print(" class = ");
//                printKlassName(klass);
//                VM.print(" delta = ");
//                VM.printOffset(delta);
//                VM.print(" size = ");
//                VM.print(size);
//                VM.print(" headerSize = ");
//                VM.print(headerSize);
//                VM.println();
//            }
//
//            free = objectDestination.add(size);
//        }
//
//        return returnValue;
//    }
/*end[ENABLE_ISOLATE_MIGRATION]*/

    /**
     * Computes the addresses to which objects will be moved/copied and encodes these target addresses
     * into the objects' headers. The return value is only meaningful when performing an object
     * graph copy; otherwise the relevant results are returned in {@link #firstDeadBlock},
     * {@link #lastDeadBlock} and {@link #firstMovingBlock}.
     *
     * @return <code>null</code> if there was not enough free room to copy all the objects
     *         in the graph otherwise it is the address of the byte one past the last copied object
     */
    private Address computeAddresses(Offset slopAllowed) throws NotInlinedPragma {

        timer.reset();

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** Start Lisp2GenerationalCollector::computeAddresses **********");
            VM.print("Lisp2GenerationalCollector::computeAddresses - collectionStart = ");
            VM.printAddress(collectionStart);
            VM.print(" collectionEnd = ");
            VM.printAddress(collectionEnd);
            VM.println();
            if (!slopAllowed.isZero()) {
                VM.print("slop allowed: ");
                VM.printOffset(slopAllowed);
                VM.println();
            }
        }

        Address returnValue = Address.zero();

        firstDeadBlock = Address.zero();
        lastDeadBlock = Address.zero();
        firstMovingBlock = Address.zero();

        // Clear the slice table
        for (int i = 0; i != sliceCount; ++i) {
            NativeUnsafe.setAddress(sliceTable, i, Address.zero());
        }

/*if[!ENABLE_ISOLATE_MIGRATION]*/
        computeAddressesLoopGC(collectionStart, slopAllowed);
/*else[ENABLE_ISOLATE_MIGRATION]*/
//        if (copyingObjectGraph) {
//            returnValue = computeAddressesLoopCopyGraph(copiedObjects);
//        } else {
//            computeAddressesLoopGC(collectionStart, slopAllowed);
//        }
/*end[ENABLE_ISOLATE_MIGRATION]*/

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** End Lisp2GenerationalCollector::computeAddresses **********");
            VM.println();
        }

        timer.finish(Timer.COMPUTE_ADDRESSES);

        return returnValue;
    }

    private Address updateWeakReferent(Ref weakRef, Address referent) {
        // Non-zero means the object is live
        if (!referent.isZero()) {
            Address newAddress = getPossiblyForwardedObject(referent);

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::updateWeakReferenceList -- weakRef = ");
                VM.printAddress(weakRef);
                VM.print(" referent = ");
                VM.printAddress(referent);
                VM.print(" -> ");
                VM.printAddress(newAddress);
                VM.println();
            }

            return newAddress;
        } else {
            return referent;
        }
    }

    /**
     * Update the object address contained within each weak reference.
     */
    private void updateWeakReferences() throws NotInlinedPragma {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
             VM.println("Lisp2GenerationalCollector::updateWeakReferenceList --------------- Start");
         }

         Ref ref = references;
         while (ref != null) {
             ref.referent = updateWeakReferent(ref, ref.referent);
             ref = ref.next;
         }

         if (GC.GC_TRACING_SUPPORTED && tracing()) {
             VM.println("Lisp2GenerationalCollector::updateWeakReferenceList --------------- End");
             VM.println();
        }
    }

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//    /**
//     * Calculates the address of the last slot in a given stack chunk that has been reserved by
//     * an activation frame in the stack.
//     *
//     * @param chunk   the stack chunk to examine
//     * @return the address of the last slot in <code>chunk</code> that has been reserved by an activation frame
//     */
//    private Address calculateLastReservedSlot(Address chunk) {
//        GC.checkSC(chunk);
//        Address fp = NativeUnsafe.getAddress(chunk, SC.lastFP);
//        Address lastReservedSlot = fp;
//        boolean isInnerMostActivation = true;
//        while (!fp.isZero()) {
//
//            Object mp = NativeUnsafe.getObject(fp, FP.method);
//            int localCount = isInnerMostActivation ? 1 : MethodHeader.decodeLocalCount(mp);
//            int stackCount = MethodHeader.decodeStackCount(mp);
//            int reserved = (localCount + stackCount + FP.FIXED_FRAME_SIZE)  * HDR.BYTES_PER_WORD;
//            if (fp.sub(reserved).lo(lastReservedSlot)) {
//                lastReservedSlot = fp.sub(reserved);
//            }
//
//            Address returnFP = NativeUnsafe.getAddress(fp, FP.returnFP);
//            fp = returnFP;
//
//            isInnerMostActivation = false;
//        }
//
//        return lastReservedSlot;
//    }
//
//    /**
//     * Copies a stack chunk, shrinking the copy such that any unused part of the stack chunk is
//     * not copied, reducing the size of serialized isolates.
//     *
//     * @param src   the chunk to copy
//     * @param size  the size (in bytes) of the body of the <code>chunk</code>
//     * @param dst   the address to which <code>chunk</code> should be copied
//     * @return the size (in bytes) of the body of the copied chunk
//     */
//    private int copyStackChunk(Address src, int size, Address dst) {
//        // Note that there is some slim chance that we would run into an ownerless stack chunk.
//        // Such chunks will have the lasFtP set to zero also, so we can chop them down to their header.
//        Assert.that(copyingObjectGraph);
//        Assert.that(NativeUnsafe.getUWord(src, SC.guard).isZero());
//        GC.checkSC(src);
//
//        // Copy the class pointer
//        NativeUnsafe.setAddress(dst, HDR.klass, NativeUnsafe.getAddress(src, HDR.klass));
//
//        // Calculate the used part of stack chunk (i.e. the live activation frames) and
//        // the (reduced) size of the copied chunk
//        Address srcEnd = src.add(size);
//        Address srcLRS = calculateLastReservedSlot(src);
//
//        final int fixedSize = SC.limit * HDR.BYTES_PER_WORD;
//        final int usedSize;
//
//        Address dstLRS;
//        Address dstEnd;
//
//        Address srcFP = NativeUnsafe.getAddress(src, SC.lastFP);
//        Address dstFP;
//
//        if (srcLRS.isZero()) {
//            usedSize = 0;
//            dstLRS = Address.zero();
//            dstEnd = dst.add(fixedSize);
//            dstFP = Address.zero();
//        } else {
//            usedSize = srcEnd.diff(srcLRS).toInt();
//            dstLRS = dst.add(fixedSize);
//            dstEnd = dstLRS.add(usedSize);
//            srcFP = NativeUnsafe.getAddress(src, SC.lastFP);
//            dstFP = dstLRS.addOffset(srcFP.diff(srcLRS));
//        }
//
//        size = dstEnd.diff(dst).toInt();
//
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.print("Lisp2GenerationalCollector::copyStackChunk - src = ");
//            VM.printAddress(src);
//            VM.print(" srcEnd = ");
//            VM.printAddress(srcEnd);
//            VM.print(" dst = ");
//            VM.printAddress(dst);
//            VM.print(" dstEnd = ");
//            VM.printAddress(dstEnd);
//            VM.print(" srcLRS = ");
//            VM.printAddress(srcLRS);
//            VM.print(" dstLRS = ");
//            VM.printAddress(dstLRS);
//            VM.print(" srcFP = ");
//            VM.printAddress(srcFP);
//            VM.print(" dstFP = ");
//            VM.printAddress(dstFP);
//            VM.println();
//        }
//
//        if (dstEnd.hi(heapEnd)) {
//            return -1;
//        }
//
//        // Copy the fixed part of the chunk (i.e. the part before the activation records)
//        VM.copyBytes(src, 0, dst, 0, fixedSize, false);
///*if[TYPEMAP]*/
//        if (VM.usingTypeMap()) {
//            NativeUnsafe.copyTypes(src, dst, fixedSize);
//        }
///*end[TYPEMAP]*/
//
//        // Prepend the copy to the list of copied stack chunks,
//        // only if chunk is alive
//        if (NativeUnsafe.getObject(src, SC.owner) == null) {
//            Assert.always(srcLRS.isZero());
//            Assert.always(srcFP.isZero());
//            Assert.always(dstFP.isZero());
//           // VM.println("  chunk was dead ");
//        } else {
//            //VM.println("  chunk was alive ");
//            if (copiedStackChunks != null) {
//                GC.checkSC(copiedStackChunks);
//            }
//            NativeUnsafe.setAddress(dst, SC.next, copiedStackChunks);
//            copiedStackChunks = dst;
//        }
//
//        // Copy the used part of stack chunk
//        if (!srcLRS.isZero()) {
//            VM.copyBytes(srcLRS, 0, dstLRS, 0, usedSize, false);
///*if[TYPEMAP]*/
//            if (VM.usingTypeMap()) {
//                NativeUnsafe.copyTypes(srcLRS, dstLRS, usedSize);
//            }
///*end[TYPEMAP]*/
//        }
//
//        // Update the length header of the copied chunk
//        GC.setHeaderLength(dst, size / HDR.BYTES_PER_WORD);
//
//        // Update the 'lastFP' field in the copied chunk
//        NativeUnsafe.setAddress(dst, SC.lastFP, dstFP);
//        recordPointer(dst.add(SC.lastFP * HDR.BYTES_PER_WORD));
//
//        // Relocate the internal stack chunk pointers
//        while (!srcFP.isZero()) {
//
//            // Calculate the change in fp when returning to the previous frame
//            Address returnFP = NativeUnsafe.getAddress(srcFP, FP.returnFP);
//            Offset delta = returnFP.diff(srcFP);
//            srcFP = returnFP;
//
//            // Apply the change to the fp in the copied stack chunk
//            Address pointerAddress = dstFP.add(FP.returnFP * HDR.BYTES_PER_WORD);
//            dstFP = srcFP.isZero() ? Address.zero() : dstFP.addOffset(delta);
//            NativeUnsafe.setAddress(pointerAddress, 0, dstFP);
//            recordPointer(pointerAddress);
//        }
//        GC.checkSC(dst);
//        return size;
//    }
/*end[ENABLE_ISOLATE_MIGRATION]*/


    /**
     * Determines if a given object is within the heap.
     *
     * @param object   the object to test
     * @return true if <code>object</code> is within heap
     */
    private boolean inHeap(Address object) throws AllowInlinedPragma {
        return object.hi(heapStart) && object.loeq(heapEnd);
    }

    /**
     * Gets the offset (in bytes) of an object in the heap from the start of the heap
     *
     * @param object  the object to test
     * @return the offset (in bytes) of <code>object</code> in the heap from the start of the heap
     */
    private Offset getOffsetInHeap(Address object) throws AllowInlinedPragma {
        Assert.that(inHeap(object));
        return object.diff(heapStart);
    }

    /**
     * Gets an object in the heap given an offset (in bytes) to the object from the start of the heap.
     *
     * @param offset   the offset (in bytes) of the object to retrieve
     * @return the object at <code>offset</code> bytes from the start of the heap
     */
    private Address getObjectInHeap(Offset offset) {
        return heapStart.addOffset(offset);
    }

    /**
     * Converts the header of an object to encode the address to which it will be forwarded
     * when the heap is compacted.
     *
     * @param object      the object that will be moved during compaction
     * @param objectDestination the address to which the object will be moved during compaction
     */
    private void forwardObject(Address object, Address objectDestination) {
        Address classOrAssociation = NativeUnsafe.getAddress(object, HDR.klass);
        UWord encodedClassWord = encodeForwardingPointer(object, objectDestination).or(encodeClassOrAssociationPointer(classOrAssociation));
        NativeUnsafe.setAddress(object, HDR.klass, Address.zero().or(encodedClassWord));
    }

    /**
     * Encodes a forwarding pointer.
     *
     * @param object       the object being forwarded
     * @param destination  the address to which it is being forwarded
     * @return <code>destination</code> as a word offset from the address to which the slice
     *         containing <code>object</code> will be moved, left shifted by {@link #sliceOffsetShift}
     */
    private UWord encodeForwardingPointer(Address object, Address destination) {
        int sliceIndex = getSliceIndexForObject(object);
        Address sliceDestination = NativeUnsafe.getAddress(sliceTable, sliceIndex);
        Offset offsetInSlice;
        if (sliceDestination.isZero()) {
            sliceDestination = destination;
            offsetInSlice = Offset.zero();
            NativeUnsafe.setAddress(sliceTable, sliceIndex, sliceDestination);
        } else {
            offsetInSlice = destination.diff(sliceDestination);
        }
        offsetInSlice = offsetInSlice.bytesToWords(); // convert to word-base offset
        Assert.that(offsetInSlice.ge(Offset.zero()) && offsetInSlice.lt(Offset.fromPrimitive(1 << sliceOffsetBits)));

        UWord encodedDestination = UWord.fromPrimitive(offsetInSlice.toPrimitive() << sliceOffsetShift);
        return encodedDestination;
    }

    /**
     * Decodes a forwarding pointer that was encoded by {@link #encodeForwardingPointer}.
     *
     * @param object              a forwarded object
     * @param encodedDestination  the encoded address to which it is forwarded
     * @return the decoded version of <code>encodedDestination</code>
     */
    private Address decodeForwardingPointer(Address object, UWord encodedDestination) {
        int sliceIndex = getSliceIndexForObject(object);
        Offset offsetInSlice = Offset.fromPrimitive(encodedDestination.toPrimitive() >>> sliceOffsetShift).wordsToBytes();
        Address sliceDestination = NativeUnsafe.getAddress(sliceTable, sliceIndex);
        return sliceDestination.addOffset(offsetInSlice);
    }

    /**
     * Encodes a class or association pointer as an offset (in words) within its memory space (i.e. the heap, NVM or ROM).
     *
     * @param classOrAssociation  a pointer to a Klass or ObjectAssociation
     * @return the pointer as an offset in words from the start of its memory space, left shifted by {@link ClassWordTag#BIT_COUNT}
     *         and or'ed with {@link ClassWordTag#HEAP}, {@link ClassWordTag#NVM} or {@link ClassWordTag#ROM}
     */
    private UWord encodeClassOrAssociationPointer(Address classOrAssociation) {

        Offset classOrAssociationOffset;
        int tag;

        if (inHeap(classOrAssociation)) {
            classOrAssociationOffset = getOffsetInHeap(classOrAssociation);
            tag = ClassWordTag.HEAP;
        } else if (VM.inRom(classOrAssociation)) {
            classOrAssociationOffset = VM.getOffsetInRom(classOrAssociation);
            tag = ClassWordTag.ROM;
        } else {
            Assert.that(GC.inNvm(classOrAssociation));
            classOrAssociationOffset = GC.getOffsetInNvm(classOrAssociation);
            tag = ClassWordTag.NVM;
        }
        classOrAssociationOffset = classOrAssociationOffset.bytesToWords(); // convert to word-base offset
        Assert.that(classOrAssociationOffset.ge(Offset.zero()) && classOrAssociationOffset.lt(Offset.fromPrimitive(1 << classOffsetBits)),
                    "encoded class or association pointer does not fit in reserved bits");

        // Construct and return the encoded pointer
        UWord encodedClassOrAssociation = UWord.fromPrimitive(classOrAssociationOffset.toPrimitive() << ClassWordTag.BIT_COUNT);
        encodedClassOrAssociation = encodedClassOrAssociation.or(UWord.fromPrimitive(tag));
        return encodedClassOrAssociation;
    }

    /**
     * Decodes a class or association pointer that was encoded by {@link #encodeClassOrAssociationPointer}.
     *
     * @param encodedClassOrAssociation   an encoded class or association pointer
     * @return the decoded version of <code>encodedClassOrAssociation</code>
     */
    private Address decodeClassOrAssociationPointer(UWord encodedClassOrAssociation) {
        int tag = (int)encodedClassOrAssociation.toPrimitive() & ClassWordTag.MASK;
        Offset classOrAssociationOffset = Offset.fromPrimitive((encodedClassOrAssociation.toPrimitive() & classOffsetMask) >>> ClassWordTag.BIT_COUNT).wordsToBytes();
        switch (tag) {
            case ClassWordTag.HEAP: return getObjectInHeap(classOrAssociationOffset);
            case ClassWordTag.ROM:  return VM.getObjectInRom(classOrAssociationOffset);
            case ClassWordTag.NVM:  return GC.getObjectInNvm(classOrAssociationOffset);
            default:
                Assert.shouldNotReachHere();
                return Address.zero();
        }
    }


    /**
     * Determines if a given object has been forwarded.
     *
     * @param object   the object to test
     * @return true if <code>object</code>'s header indicates that it has been forwarded
     */
    private static boolean isForwarded(Address object) throws AllowInlinedPragma {
        return !ClassWordTag.isPointer(NativeUnsafe.getAddress(object, HDR.klass).toUWord());
    }

    /**
     * Gets the forwarding address of an object.
     *
     * @param  object  an object that has been forwarded
     * @return the forwarding address of <code>object</code>
     */
    private Address getForwardedObject(Address object) throws AllowInlinedPragma {
        Assert.that(isForwarded(object));
        UWord encodedDestination = NativeUnsafe.getAddress(object, HDR.klass).toUWord();
        return decodeForwardingPointer(object, encodedDestination);
    }

    /**
     * Gets the forwarding address of an object if it is non-null and has been forwarded.
     *
     * @param  object  an object that has possibly been forwarded
     * @return the forwarding address of <code>object</code> if it non-null and has been
     *         forwarded otherwise <code>object</code>
     */
    private Address getPossiblyForwardedObject(Address object) {
        return (!object.isZero() && isForwarded(object)) ? getForwardedObject(object) : object;
    }

    /**
     * Decodes and returns the encoded pointer in a forwarded object's header that points to
     * the object's class or ObjectAssociation. The returned pointer may itself be pointing
     * to an object that will be moved.
     *
     * @param object    address of a object whose header has been modified to encode both the
     *                  location of its class or ObjectAssociation as well as where it will be
     *                  moved to
     * @return the value of <code>object</code>'s klass or ObjectAssociation
     */
    private Address getClassOrAssociationFromForwardedObject(Address object) {
        Assert.that(isForwarded(object));
        UWord encodedClassOrAssociation = NativeUnsafe.getAddress(object, HDR.klass).toUWord();
        return decodeClassOrAssociationPointer(encodedClassOrAssociation);
    }

    /**
     * Given an object's address and the value of the class pointer word in its header,
     * determines if the pointer points to the class or to an ObjectAssociation. This
     * test relies on the fact that an object's class will always be at a lower address
     * in the heap or not in the heap at all.
     *
     * If !ENABLE_DYNAMIC_CLASSLOADING, then classes will never be in heap, so use simpler test
     *
     * @param object              an object's address
     * @param classOrAssociation  the value of the class pointer word in <code>object</code>'s header
     * @return true if <code>classOrAssociation</code> is a pointer outside of the heap or to a heap address lower than <code>object</code>
     */
    private static boolean isClassPointer(Address object, Address classOrAssociation) throws AllowInlinedPragma {
        Assert.that(classOrAssociation.ne(object));
/*if[ENABLE_DYNAMIC_CLASSLOADING]*/
        return !GC.inRam(classOrAssociation) || classOrAssociation.lo(object);
/*else[ENABLE_DYNAMIC_CLASSLOADING]*/
//      return !GC.inRam(classOrAssociation);
/*end[ENABLE_DYNAMIC_CLASSLOADING]*/
    }

    /**
     * Gets the class of an (potentially forwarded) object.
     *
     * @param object  an object
     * @return the address of <code>object</code>'s class which may itself be forwarded
     */
    private Address getKlass(Address object) {
        Address classOrAssociation;
        if (isForwarded(object)) {
            classOrAssociation = getClassOrAssociationFromForwardedObject(object);
        } else {
            classOrAssociation = NativeUnsafe.getAddress(object, HDR.klass);
        }
        Assert.that(!classOrAssociation.isZero());
        Assert.that(!classOrAssociation.eq(Address.fromPrimitive(0xDEADBEEF)));
        
        Address klass = NativeUnsafe.getAddress(classOrAssociation, (int)FieldOffsets.com_sun_squawk_Klass$self);
//        if (!isClassPointer(object, classOrAssociation)) {
//            klass = NativeUnsafe.getAddress(classOrAssociation, (int)FieldOffsets.com_sun_squawk_Klass$self);
//            // Must be an ObjectAssociation
//            Assert.that(klass.ne(classOrAssociation));
//        } else {
//            klass = classOrAssociation;
//        }

        Assert.that(!klass.isZero());
        Assert.that(!klass.eq(Address.fromPrimitive(0xDEADBEEF)));
        return klass;
    }

    /**
     * Prints the name or address of a class. This method will print the name of the class
     * if it the class object is not forwarded (which guarantees that the name of the class
     * is not forwarded - it is always at a lower address) otherwise the address of the class will be printed.
     *
     * @param klass  the class to print
     */
    private static void printKlassName(Object klass) {
        Address klassAddress = Address.fromObject(klass);
        if (isForwarded(klassAddress)) {
            VM.printAddress(klassAddress);
        } else {
            String name = Klass.getInternalName(VM.asKlass(klass));
            Assert.that(!GC.inRam(klassAddress) || Address.fromObject(name).lo(klassAddress));
            VM.print(name);
        }
    }

    /**
     * Computes which slice a given object falls into.
     *
     * @param object      the object to test
     * @return the slice into which <code>object</code> falls
     */
    private int getSliceIndexForObject(Address object) {
        // Subtract a word as the object may have a zero-length body
        // and be at a slice boundary and/or the end of the heap
        Offset heapOffset = getOffsetInHeap(object).sub(HDR.BYTES_PER_WORD);

        int index = (int)(heapOffset.toPrimitive() / sliceSize);
        Assert.that(index >= 0 && index < sliceCount);
        return index;
    }

    /*---------------------------------------------------------------------------*\
     *                            Update routines                                *
    \*---------------------------------------------------------------------------*/

    /**
     * Updates all the pointers to objects that will be moved during the compaction phase.
     */
    private void updatePointers() {

        timer.reset();

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** Start Lisp2GenerationalCollector::updatePointers **********");
        }

        // Update the pointers in the old generation if this is not a full GC
        if (!isFullCollection()) {

            // Update the pointers in the activation frames that are not in the collection space
            updateOldGenerationStackChunkPointers();

            // Update all the other pointers in the old generation
            updateWriteBarrierOops(heapStart, collectionStart);
        }

        // Update the pointers in the roots
        updateRoots();

        // Update the pointers in the collection space
        updateCollectionSpacePointers();

        // Update any pointers in this collector object to heap objects that move
        traverseOopsInNonArrayObject(Address.fromObject(this), getKlass(Address.fromObject(this)), UPDATE_VISITOR);

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** End Lisp2GenerationalCollector::updatePointers **********");
        }

        timer.finish(Timer.UPDATE_POINTERS);
    }

    /**
     * Updates the pointers in the collection space.
     */
    private void updateCollectionSpacePointers() throws NotInlinedPragma {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::updateCollectionSpacePointers --------------- Start");
        }
        Lisp2Bitmap.Iterator.start(collectionStart, collectionEnd, true);
        Address object;
        while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {
            Address klass = getKlass(object);
            traverseOopsInObject(object, klass, UPDATE_VISITOR);
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::updateCollectionSpacePointers --------------- End");
            VM.println();
        }
    }

    /**
     * Updates all the root pointers to objects that have been forwarded.
     */
    private void updateRoots() throws NotInlinedPragma {
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::updateRoots --------------- Start");
        }
        for (int i = 0 ; i < VM.getGlobalOopCount() ; i++) {
            Address object = Address.fromObject(VM.getGlobalOop(i));
            if (!object.isZero()) {
                if (isForwarded(object)) {
                    Address destination = getForwardedObject(object);

                    if (GC.GC_TRACING_SUPPORTED && tracing()) {
                        VM.print("Lisp2GenerationalCollector::updateRoots - [");
                        VM.print(i);
                        VM.print("] = ");
                        VM.printAddress(object);
                        VM.print(" -> ");
                        VM.printAddress(destination);
                        VM.println();
                    }

                    VM.setGlobalOop(destination.toObject(), i);
                }
            }
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::updateRoots --------------- End");
            VM.println();
        }
    }

    /**
     * Updates an object pointer given a base and offset to the pointer. The pointer is only
     * changed if it points to a non-null object that has been forwarded.
     *
     * @param base     the base address of an object pointer
     * @param offset   the offset (in words) from <code>base</code> of the object pointer
     * @return the value of the pointer after the changes (if any) have been applied
     */
    private void updateOop(Address base, int offset) {
        Address object = NativeUnsafe.getAddress(base, offset);

        Address destination = getPossiblyForwardedObject(object);
        if (object.ne(destination)) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::updateOop - [");
                VM.printAddress(base);
                VM.print("%");
                VM.print(offset);
                VM.print("] = ");
                VM.printAddress(object);
                VM.print(" -> ");
                VM.printAddress(destination);
                VM.println();
            }

            NativeUnsafe.setAddress(base, offset, destination);
        }

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//        if (copyingObjectGraph) {
//            // Update the oop map
//            Address pointerAddress = base.add(offset * HDR.BYTES_PER_WORD);
//            recordPointer(pointerAddress);
//        }
/*end[ENABLE_ISOLATE_MIGRATION]*/
    }

    /**
     * Updates the pointers in each stack chunk that is in the old generation.
     * <p>
     * This separate parsing of stack chunks in the old generation obviates
     * maintaining the write barrier for local variables and parameters.
     */
    private void updateOldGenerationStackChunkPointers() throws NotInlinedPragma {
        Address chunk = Address.fromObject(stackChunks);
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::updateOldGenerationStackChunkPointers --------------- Start");
        }
        while (!chunk.isZero()) {
            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::updateOldGenerationStackChunkPointers - chunk = ");
                VM.printAddress(chunk);
                VM.println();
            }
            GC.checkSC(chunk);
            
            Address nextChunk = NativeUnsafe.getAddress(chunk, SC.next);
            if (!inCollectionSpace(chunk)) {
                // The pointers in the header part of the stack chunk are not updated here
                // as they will be updated as necessary when traversing the standard write barrier
                traverseOopsInStackChunk(chunk, UPDATE_VISITOR, false);
            }
            chunk = nextChunk;
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::updateOldGenerationStackChunkPointers --------------- End");
            VM.println();
        }

    }

    /**
     * Updates the {@link FP#returnFP} slot in each activation frame within a given stack chunk
     * that will be relocated during the compaction phase.
     *
     * @param chunk       the stack chunk at it's current location
     * @param destination the stack chunk at it's destination location
     */
    private void updateInternalStackChunkPointers(Address chunk, Address destination) throws NotInlinedPragma {
        Assert.that(!copyingObjectGraph);
        Offset offset = Offset.fromPrimitive(SC.lastFP).wordsToBytes();
        Address oldFP = NativeUnsafe.getAddress(chunk.addOffset(offset), 0);
        while (!oldFP.isZero()) {
            Address pointerAddress = chunk.addOffset(offset);
            Offset delta = oldFP.diff(chunk);
            Address newFP = destination.addOffset(delta);
            NativeUnsafe.setAddress(pointerAddress, 0, newFP);

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::updateInternalStackChunkPointers - [");
                VM.printAddress(chunk);
                VM.print(" + ");
                VM.printOffset(offset);
                VM.print("] = ");
                VM.printAddress(oldFP);
                VM.print(" ->  ");
                VM.printAddress(newFP);
                VM.println();
            }

            offset = delta.add(FP.returnFP * HDR.BYTES_PER_WORD);
            oldFP = NativeUnsafe.getAddress(chunk.addOffset(offset), 0);
        }
    }

    /*---------------------------------------------------------------------------*\
     *                          Write barrier verification                       *
    \*---------------------------------------------------------------------------*/

/*if[DEBUG_CODE_ENABLED]*/

    /**
     * Visits all the objects within a given object memory, verifying that all
     * references an object in the collection space have the corresponding bit set in the write barrier.
     *
     * @param start   the address of the first block in the object memory
     * @param end     the address of byte one past the last object in the memory
     */
    private void verifyWriteBarrier(Address start, Address end) {

        Address object;
        Address klassAddress;

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::verifyWriteBarrier --------------- Start");
        }

        for (Address block = start; block.lo(end); ) {
            object = GC.blockToOop(block);
            Klass klass = GC.getKlass(object);
            klassAddress = Address.fromObject(klass);

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::verifyWriteBarrier - object = ");
                VM.printAddress(object);
                VM.print(" class = ");
                VM.println(klass.getInternalName());
            }

            if (Klass.isSquawkArray(klass)) { // any kind of array
                // Stack chunk pointers are scanned explicitly and therefore won't have their write barrier bits set
                if (klass.getSystemID() != CID.LOCAL_ARRAY) {
                    traverseOopsInArrayObject(object, klassAddress, WRITE_BARRIER_VERIFY_VISITOR);
                } else {
                    // Ensure that the stack chunk is either dead (i.e. owner is null) or reachable from the list of stack chunks
                    if (NativeUnsafe.getObject(object, SC.owner) != null) {
                        GC.checkSC(object);
                        // Not orphaned case
                        Object chunk = stackChunks;
                        boolean found = false;
                        while (chunk != null) {
                            if (Address.fromObject(chunk).eq(object)) {
                                found = true;
                                break;
                            }
                            chunk = NativeUnsafe.getObject(chunk, SC.next);
                            GC.checkSC(chunk);
                        }
                        Assert.always(found, "unreachable live stack chunk found");
                    }
                }
            } else {
                traverseOopsInNonArrayObject(object, klassAddress, WRITE_BARRIER_VERIFY_VISITOR);
            }

            block = object.add(GC.getBodySize(klass, object));
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::verifyWriteBarrier --------------- End");
            VM.println();
        }
    }

    /*---------------------------------------------------------------------------*\
     *                          Heap verification                                *
    \*---------------------------------------------------------------------------*/

    /**
     * Visits all the objects within a given object memory, verifying that they appear to
     * be well formed.
     *
     * @param start   the address of the first block in the object memory
     * @param end     the address of byte one past the last object in the memory
     */
    private void verifyObjectMemory(Address start, Address end) {

        Address object;
        Address klassAddress;

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::verifyObjectMemory --------------- Start");
        }

        for (Address block = start; block.lo(end); ) {
            object = GC.blockToOop(block);
            Klass klass = GC.getKlass(object);
            klassAddress = Address.fromObject(klass);

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::verifyObjectMemory - object = ");
                VM.printAddress(object);
                VM.print(" class = ");
                VM.println(klass.getInternalName());
            }
            Assert.that(GC.getKlass(klass).getSystemID() == CID.KLASS, "class of referenced object is invalid");
            // Verify the pointer to the class or ObjectAssociation
            visitOop(VERIFY_VISITOR, object, HDR.klass);

            if (Klass.isSquawkArray(klass)) { // any kind of array
                // An dead stack chunk in the old generation will most likely have invalid pointers
                if (klass.getSystemID() != CID.LOCAL_ARRAY || NativeUnsafe.getObject(object, SC.owner) != null) {
                    traverseOopsInArrayObject(object, klassAddress, VERIFY_VISITOR);
                }
            } else {

                if (klass.getSystemID() == CID.KLASS) {
                    int instanceSize = Klass.getInstanceSize(klass);
                    if (instanceSize > HDR.BITS_PER_WORD) {
                        Assert.always(!NativeUnsafe.getUWord(object, (int)FieldOffsets.com_sun_squawk_Klass$oopMapWord).eq(UWord.zero()), "oopMapWord not reset");
                    }
                }

                traverseOopsInNonArrayObject(object, klassAddress, VERIFY_VISITOR);
            }

            block = object.add(GC.getBodySize(klass, object));
        }
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("Lisp2GenerationalCollector::verifyObjectMemory --------------- End");
            VM.println();
        }
    }
/*end[DEBUG_CODE_ENABLED]*/

    /*---------------------------------------------------------------------------*\
     *                          Object graph copying                             *
    \*---------------------------------------------------------------------------*/

    /**
     * Specifies if the collector is being used to copy an object graph as opposed to collect garbage.
     */
/*if[!ENABLE_ISOLATE_MIGRATION]*/
    private static final boolean copyingObjectGraph = false;
/*else[ENABLE_ISOLATE_MIGRATION]*/
//  private boolean copyingObjectGraph;
//    /**
//     * The list of copied stack chunks.
//     */
//    private Object copiedStackChunks;
//
//    /**
//     * This is the starting address of the copied objects.
//     */
//    private Address copiedObjects;
//
//    /**
//     * The BitSet instance that must be updated during a graph copy operation.
//     */
//    private BitSet oopMap;
//
//    /**
//     * The class of com.sun.squawk.util.SquawkHashtable.
//     */
//    private Klass HashTableKlass;
//
//    /**
//     * The class of byte[].
//     */
//    private Klass ByteArrayKlass;
//    /**
//     * The class of com.sun.squawk.Isolate
//     */
//    private Klass IsolateKlass;
//
///*if[DEBUG_CODE_ENABLED]*/
//    /**
//     * The class of com.sun.squawk.VMThread
//     */
//    private Klass ThreadKlass;
//
//    /**
//     * The class of com.sun.squawk.ObjectMemory
//     */
//    private Klass ObjectMemoryKlass;
///*end[DEBUG_CODE_ENABLED]*/
//
//    /**
//     * The isolate object (if any) currently being copied by copyObjectGraph().
//     */
//    private Isolate theIsolate;
//
//    /**
//     * The timing statistics related to object graph copying.
//     */
//    private final Timer copyTimer;
/*end[ENABLE_ISOLATE_MIGRATION]*/

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//    /**
//     * Marks all the objects reachable from a given root.
//     *
//     * @param object  the root of the graph of objects to mark
//     */
//    private void markObjectGraph(Address object) {
//
//        timer.reset();
//
//        markObject(object);
//        while (markingStack.hasOverflowed()) {
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.println("Lisp2GenerationalCollector::markObjectGraph - remarking from marked objects after marking stack overflow");
//            }
//            markingStack.resetOverflow();
//            Lisp2Bitmap.Iterator.start(collectionStart, collectionEnd, true);
//            while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {
//                traverseOopsInObject(object, Address.fromObject(GC.getKlass(object)), MARK_VISITOR);
//                while (!(object = markingStack.pop()).isZero()) {
//                    traverseOopsInObject(object, Address.fromObject(GC.getKlass(object)), MARK_VISITOR);
//                }
//            }
//        }
//
//        timer.finish(Timer.MARK);
//    }
/*end[ENABLE_ISOLATE_MIGRATION]*/

/*if[ENABLE_ISOLATE_MIGRATION]*/   // weird way to deal with no nesting in else clause
/*if[JAVA5SYNTAX]*/
    @Vm2c(root="copyObjectGraph")
/*end[JAVA5SYNTAX]*/
/*end[ENABLE_ISOLATE_MIGRATION]*/

/*if[!ENABLE_ISOLATE_MIGRATION]*/
/*else[ENABLE_ISOLATE_MIGRATION]*/
//    Address copyObjectGraphInJava(Address object, ObjectMemorySerializer.ControlBlock cb, Address allocTop) {
//
//        // Get the special classes if this is the first time a copy is being performed
//        Isolate isolate;
//        Suite bootstrapSuite;
//        if (HashTableKlass == null) {
//
//            isolate = VM.getCurrentIsolate();
//            bootstrapSuite = isolate.getBootstrapSuite();
//            HashTableKlass = bootstrapSuite.lookup("com.sun.squawk.util.SquawkHashtable");
//            ByteArrayKlass = bootstrapSuite.getKlass(CID.BYTE_ARRAY);
//            IsolateKlass = GC.getKlass(isolate);
//
///*if[DEBUG_CODE_ENABLED]*/
//            ThreadKlass = bootstrapSuite.lookup("com.sun.squawk.VMThread");
//            ObjectMemoryKlass = bootstrapSuite.lookup("com.sun.squawk.ObjectMemory");
///*end[DEBUG_CODE_ENABLED]*/
//        }
//
//        timer = copyTimer;
//        timer.reset();
//
//        copiedStackChunks = null;
//        copyingObjectGraph = true;
//        oopMap = cb.oopMap;
//
//        if (GC.getKlass(object) == IsolateKlass) {
//            theIsolate = (Isolate)object.toObject();
//        }
//
//        // Set up the limits of the space to be collected.
//        collectionStart = heapStart;
//        collectionEnd = allocTop;
//
//        // Sets up the marking stack.
//        markingStack.setup(heapEnd, Lisp2Bitmap.getStart());
//
//        // Reset the marking recursion level.
//        markingRecursionLevel = MAX_MARKING_RECURSION;
//
//        // Output trace information.
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.print("Lisp2GenerationalCollector::copyObjectGraph - object = ");
//            VM.printAddress(object);
//            VM.println();
//            traceVariables();
//        }
//        verifyStackChunks();
//
//        // Clears the bitmap corresponding to the collection area. Must clear one past the
//        // end of the collection area as there can be an object there that has a zero length body.
//        Lisp2Bitmap.clearBitsFor(collectionStart, collectionEnd.add(HDR.BYTES_PER_WORD));
//
//        timer.finish(Timer.SETUP);
//
//        // Mark objects in graph
//        markObjectGraph(object);
//
//        copiedObjects = collectionEnd.add(HDR.arrayHeaderSize);
//        Address copiedObjectsEnd;
//
//        // Compute the address to which the objects will be copied and do the copy
//        copiedObjectsEnd = computeAddresses(Offset.zero());
//
//        if (!copiedObjectsEnd.isZero()) {
//
//            // Update the pointers in the copied objects
//            updatePointersInObjectMemory(copiedObjects, copiedObjectsEnd);
//
//            // Find out where the root of the object memory is
//            object = getForwardedObject(object);
//
//            // Unforward the class header word of all objects that have been copied
//            unforwardCopiedObjects(collectionStart, collectionEnd);
//
//            if (theIsolate != null) {
//                Assert.always(GC.getKlass(object) == IsolateKlass);
//                Assert.always(copiedStackChunks != null);
//
//                NativeUnsafe.setAddress(object, (int)FieldOffsets.com_sun_squawk_Isolate$savedStackChunks, copiedStackChunks);
//                recordPointer(object.add((int)FieldOffsets.com_sun_squawk_Isolate$savedStackChunks * HDR.BYTES_PER_WORD));
//            }
//
//            timer.reset();
//
//            // Make the object memory a byte array
//            int graphSize = copiedObjectsEnd.diff(copiedObjects).toInt();
//            GC.setHeaderClass(copiedObjects, ByteArrayKlass);
//            GC.setHeaderLength(copiedObjects, graphSize);
//
//            // Finalize the other return values
//            cb.root = object.diff(copiedObjects).toInt();
//            cb.start = copiedObjects;
//
//            // Adjust the allocation parameters
//            allocTop = copiedObjectsEnd;
//            youngGenerationStart = allocTop;
//            int youngGenerationSize = getYoungGenerationSize();
//            Address youngGenerationEnd = youngGenerationStart.add(youngGenerationSize);
//            GC.setAllocationParameters(heapStart, youngGenerationStart, youngGenerationEnd, heapEnd);
//
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.println();
//                VM.print("Lisp2GenerationalCollector::copyObjectGraph - objectMemory = ");
//                VM.printAddress(copiedObjects);
//                VM.print(" objectMemoryEnd = ");
//                VM.printAddress(copiedObjectsEnd);
//                VM.print(" size ");
//                VM.print(graphSize);
//                VM.println();
//            }
//        } else {
//            // Unforward the class header word of all objects that have been copied
//            unforwardCopiedObjects(collectionStart, collectionEnd);
//
//            timer.reset();
//            copiedObjects = Address.zero();
//        }
//
//        // Force the next collection to be full
//        nextCollectionIsFull = true;
//
//        // Output trace information.
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            traceVariables();
//        }
//
//        // Reset the re-entry guard.
//        copyingObjectGraph = false;
//        copiedStackChunks = null;
//        oopMap = null;
//        theIsolate = null;
//
///*if[DEBUG_CODE_ENABLED]*/
//        // Verify that the memory looks well formed
//        verifyStackChunks();
//        verifyObjectMemory(permanentMemoryStart, memoryStart);
//        verifyObjectMemory(heapStart, youngGenerationStart);
///*end[DEBUG_CODE_ENABLED]*/
//
//        timer.finish(Timer.FINALIZE);
//
//        return copiedObjects;
//    }
//
//    /**
//     * Reverts the class header word of all the objects in a given range that have been
//     * copied to another location and therefore had their class header word modified to
//     * encode the destination of the copy.
//     *
//     * @param start  the start of the first block in the range
//     * @param end    the end of the range
//     */
//    private void unforwardCopiedObjects(Address start, Address end) {
//
//        timer.reset();
//
//        Address object;
//        Address classOrAssociation;
//
//        // Iterate over all the marked objects in the collection space
//        Lisp2Bitmap.Iterator.start(start, end, true);
//        while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {
//
//            if (!isForwarded(object)) {
//                // This means that we ran out of space while copying the graph
//                Lisp2Bitmap.Iterator.terminate();
//                break;
//            }
//
//            classOrAssociation = getClassOrAssociationFromForwardedObject(object);
//            NativeUnsafe.setAddress(object, HDR.klass, classOrAssociation);
//        }
//
//        timer.finish(Timer.UNFORWARD);
//    }
//
//    /**
//     * Updates all the pointers within a given object memory that may contain references to
//     * forwarded objects. Called by copyObjectGraph.
//     *
//     * @param start   the address of the first block in the object memory
//     * @param end     the address of byte one past the last object in the memory
//     */
//    private void updatePointersInObjectMemory(Address start, Address end) throws NotInlinedPragma {
//        Assert.that(copyingObjectGraph);
//
//        timer.reset();
//
//        Address object;
//        Address klassAddress;
//
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.println("Lisp2GenerationalCollector::updatePointersInObjectMemory --------------- Start");
//        }
//
//        for (Address block = start; block.lo(end); ) {
//            object = GC.blockToOop(block);
//            Klass klass = GC.getKlass(object);
//            klassAddress = Address.fromObject(klass);
//
//            // Update the pointer to the class or ObjectAssociation
//            visitOop(UPDATE_VISITOR, object, HDR.klass);
//
//            if (Klass.isSquawkArray(klass)) {
//                traverseOopsInArrayObject(object, klassAddress, UPDATE_VISITOR);
//            } else {
//                traverseOopsInNonArrayObject(object, klassAddress, UPDATE_VISITOR);
//            }
//
//            block = object.add(GC.getBodySize(klass, object));
//        }
//        if (GC.GC_TRACING_SUPPORTED && tracing()) {
//            VM.println("Lisp2GenerationalCollector::updatePointersInObjectMemory --------------- End");
//            VM.println();
//        }
//
//        timer.finish(Timer.UPDATE_POINTERS);
//    }
//
//    /**
//     * Records the address of a pointer in the graph being copied.
//     *
//     * @param pointerAddress the address of a pointer
//     */
//    private void recordPointer(Address pointerAddress) {
//        // Update the oop map
//        if (inHeap(pointerAddress)) {
//            int word = (pointerAddress.diff(copiedObjects).toInt() / HDR.BYTES_PER_WORD);
//            oopMap.set(word);
//            if (GC.GC_TRACING_SUPPORTED && tracing()) {
//                VM.print("Lisp2GenerationalCollector::recordPointer - set bit in oop map for pointer at ");
//                VM.printAddress(pointerAddress);
//                VM.print(" [");
//                VM.printAddress(NativeUnsafe.getAddress(pointerAddress, 0));
//                VM.print("]");
//                VM.println();
//            }
//        }
//    }
//
///*if[DEBUG_CODE_ENABLED]*/
//    /**
//     * Determines if a given object address does not break isolation. This is not
//     * a comprehensive test but should catch most errors.
//     *
//     * @param object   the object address to test
//     * @param klass    the class of the object
//     */
//    private void assertIsolation(Address object, Klass klass) {
//
//        Address global;
//
//        // Ensure that the object does not coincide with the value of a global reference
//        for (int i = 0 ; i < VM.getGlobalOopCount() ; i++) {
//            global = Address.fromObject(VM.getGlobalOop(i));
//            if (global.eq(object)) {
//                VM.print("cannot copy value shared with global '");
//                VM.printGlobalOopName(i);
//                VM.println("' -- most likely caused by a local variable in a method in com.sun.squawk.VMThread pointing to a global");
//                Assert.shouldNotReachHere();
//            }
//        }
//
//        Assert.that(klass != ObjectMemoryKlass, "cannot copy an ObjectMemory instance");
//
//        if (theIsolate != null) {
//            if (Klass.isSubtypeOf(klass, ThreadKlass)) {
//                Assert.that(NativeUnsafe.getObject(object, (int)FieldOffsets.com_sun_squawk_VMThread$isolate) == theIsolate,
//                            "cannot copy thread from another isolate");
//            } else if (Klass.isSubtypeOf(klass, IsolateKlass)) {
//                Assert.that(object.eq(Address.fromObject(theIsolate)), "cannot copy another isolate");
//            } else if (Klass.getSystemID(klass) == CID.LOCAL_ARRAY) {
//                Object thread = NativeUnsafe.getObject(object, SC.owner);
//                Assert.that(thread == null || NativeUnsafe.getObject(thread, (int)FieldOffsets.com_sun_squawk_VMThread$isolate) == theIsolate,
//                            "cannot copy stack chunk from another isolate");
//            }
//        }
//    }
///*end[DEBUG_CODE_ENABLED]*/
/*end[ENABLE_ISOLATE_MIGRATION]*/

    /*---------------------------------------------------------------------------*\
     *                            Compaction routines                            *
    \*---------------------------------------------------------------------------*/

    /**
     * Compact the objects marked for moving.
     *
     * @param firstDeadBlock  the address of the first dead block. This is where the objects will started to be copied.
     * @return the first free address after compaction
     */
    private Address compactObjects(Address firstDeadBlock, Address firstMovingBlock) throws NotInlinedPragma {

        timer.reset();
        
        Assert.that(!firstDeadBlock.isZero());
        Address free = firstDeadBlock;
        Address object;

        // Trace all the non-moving objects in the collection space
        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** Start Lisp2GenerationalCollector::compactObjects **********");
            Lisp2Bitmap.Iterator.start(collectionStart, firstMovingBlock, true);
            while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {
                VM.print("Lisp2GenerationalCollector::compactObjects - object = ");
                VM.printAddress(object);
                VM.println(" [doesn't move]");
            }
        }

        // Iterate over all the moving objects in the collection space
        Lisp2Bitmap.Iterator.start(firstMovingBlock, collectionEnd, true);
        while (!(object = Lisp2Bitmap.Iterator.getNext()).isZero()) {

            Assert.that(isForwarded(object));
            Address objectDestination = getForwardedObject(object);

            // Get a pointer to the object's class.
            Klass klass;
            Address classOrAssociation = getClassOrAssociationFromForwardedObject(object);
            if (isClassPointer(object, classOrAssociation)) {
                // The class of an object is always moved before the object itself so the
                // value of classOrAssociation is a pointer to a valid class
                klass = VM.asKlass(classOrAssociation);
            } else {

                // An object is always moved before its ObjectAssociation
                Assert.that(isForwarded(classOrAssociation));

                // The 'self' pointer must be dereferenced in the old copy of the ObjectAssociation
                // as the new copy will be made during a subsequent iteration of this loop
                klass = VM.asKlass(NativeUnsafe.getObject(classOrAssociation, (int)FieldOffsets.com_sun_squawk_Klass$self));

                // Now get the address to which the ObjectAssociation will be moved
                classOrAssociation = getForwardedObject(classOrAssociation);
            }

            // Update the class pointer
            Assert.that(!classOrAssociation.isZero());
            NativeUnsafe.setAddress(object, HDR.klass, classOrAssociation);

            int headerSize = object.diff(GC.oopToBlock(klass, object)).toInt();
            int size = GC.getBodySize(klass, object);

            Address block = object.sub(headerSize);
            Address destinationBlock = objectDestination.sub(headerSize);
            int blockSize = headerSize + size;
            VM.copyBytes(block, 0, destinationBlock, 0, blockSize, false);
/*if[TYPEMAP]*/
            if (VM.usingTypeMap()) {
                NativeUnsafe.copyTypes(block, destinationBlock, blockSize);
            }
/*end[TYPEMAP]*/

            if (GC.GC_TRACING_SUPPORTED && tracing()) {
                VM.print("Lisp2GenerationalCollector::compactObjects - object = ");
                VM.printAddress(object);
                VM.print(" destination = ");
                VM.printAddress(objectDestination);
                VM.print(" class = ");
                printKlassName(klass);
                VM.println();
            }

            free = destinationBlock.add(blockSize);
            Assert.that(!isForwarded(objectDestination));
        }

        if (GC.GC_TRACING_SUPPORTED && tracing()) {
            VM.println("********** End Lisp2GenerationalCollector::compactObjects **********");
        }

        timer.finish(Timer.COMPACT_OBJECTS);

        return free;
    }

    /*---------------------------------------------------------------------------*\
     *                     Object state accessors and tests                      *
    \*---------------------------------------------------------------------------*/

    /**
     * Determines if a given object is within the range of memory being collected.
     *
     * @param object  the object to test
     * @return true if <code>object</code> lies within the range of memory being collected
     */
    private boolean inCollectionSpace(Address object) throws AllowInlinedPragma {
        // The test is exclusive of the range's start and inclusive of the range's end
        // which accounts for objects always having non-zero-length headers but
        // possibly having zero length bodies
        return object.hi(collectionStart) && object.loeq(collectionEnd);
    }

    /*---------------------------------------------------------------------------*\
     *                           Marking stack                                   *
    \*---------------------------------------------------------------------------*/

    final static class MarkingStack {

        /**
         * The minimum number of words to be reserved for the marking stack. The actual size of
         * the stack varies on each collection depending on the amount of the heap that is currently
         * unused (i.e. the space between heapEnd and youngGenerationEnd).
         */
        final static int MINIMUM_MARKING_STACK_SIZE = 10;

        /**
         * A compile time constant that can be set to true to test the behaviour when
         * the marking stack overflows.
         */
        private final static boolean TESTMARKSTACKOVERFLOW = false;//Klass.DEBUG;

        /**
         * The base address of the stack.
         */
        private Address base;

        /**
         * The number of slots in the stack.
         */
        private int size;

        /**
         * The index of the slot one past the top element on the stack.
         */
        private int index;

        /**
         * Specifies if the marking stack overflowed.
         */
        private boolean overflowed;

        /**
         * Initializes the parameters of this stack.
         *
         * @param base  the address of the stack's first slot
         * @param end   the address one past the stack's last slot
         */
        void setup(Address base, Address end) {
            this.base = base;
            this.size = end.diff(base).toInt() >> HDR.LOG2_BYTES_PER_WORD;
            this.index = 0;
            this.overflowed = false;
        }

        /**
         * Pushes an object on the marking stack or sets the flag indicating that
         * the marking stack overflowed if the stack is full.
         *
         * @param object   the (non-null) object to push on the stack
         */
        void push(Address object) {
//        Assert.that(stackInUse);
            Assert.that(!object.isZero());
            if (index == size || TESTMARKSTACKOVERFLOW) {
                overflowed = true;
            } else {
                NativeUnsafe.setAddress(base, index, object);
                index += 1;
            }
        }

        /**
         * Pops a value off the stack and returns it.
         *
         * @return  the value popped of the top of the stack or null if the stack was empty
         */
        Address pop() throws AllowInlinedPragma {
            if (index == 0) {
                return Address.zero();
            } else {
                index -= 1;
                return NativeUnsafe.getAddress(base, index);
            }
        }

        /**
         * Determines if an attempt has been made to push an object to this stack when it
         * was full since the last call to {@link #setup(Address, Address)}.
         *
         * @return true if the stack has overflowed
         */
        boolean hasOverflowed() throws AllowInlinedPragma {
            return overflowed;
        }

        /**
         * Resets the overflow flag to false.
         */
        void resetOverflow() throws AllowInlinedPragma {
            overflowed = false;
        }

        /**
         * Trace this marking stack's variables.
         *
         * @param parent  the collector context of this stack
         */
        void traceVariables(GarbageCollector parent) {
            parent.traceVariable("markingStack.size", size);
            parent.traceVariable("markingStack.base", base);
            parent.traceVariable("markingStack.index", index);
            parent.traceVariable("markingStack.overflowed", overflowed ? 1 : 0);
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
        VM.println("Lisp2GenerationalCollector variables");
        traceVariable("permanentMemoryStart", permanentMemoryStart);
        traceVariable("memoryStart", memoryStart);
        traceVariable("memoryEnd", memoryEnd);
        traceVariable("memorySize", memoryEnd.diff(memoryStart).toInt());
        traceVariable("bitmap", Lisp2Bitmap.getStart());
        traceVariable("bitmapEnd", Lisp2Bitmap.getEnd());
        traceVariable("bitmapSize", Lisp2Bitmap.getSize());
        traceVariable("bitmapBase", Lisp2Bitmap.getBase());

        traceVariable("sliceTable", sliceTable);
        traceVariable("sliceTableEnd", sliceTable.add(sliceTableSize));
        traceVariable("sliceTableSize", sliceTableSize);
        traceVariable("sliceOffsetShift", sliceOffsetShift);
        traceVariable("sliceOffsetBits", sliceOffsetBits);
        traceVariable("sliceSize", sliceSize);
        traceVariable("sliceCount", sliceCount);

        markingStack.traceVariables(this);

        traceVariable("heapStart", heapStart);
        traceVariable("heapEnd", heapEnd);
        traceVariable("heapSize", heapSize);

        int youngGenerationSize = getYoungGenerationSize();
        traceVariable("youngGenerationStart", youngGenerationStart);
        traceVariable("youngGenerationEnd", youngGenerationStart.add(youngGenerationSize));
        traceVariable("youngGenerationSize", youngGenerationSize);

        traceVariable("collectionStart", collectionStart);
        traceVariable("collectionEnd", collectionEnd);
        traceVariable("collectionSize", collectionEnd.diff(collectionStart).toInt());

        traceVariable("bitmappedStart", Lisp2Bitmap.getAddressForBitmapWord(Lisp2Bitmap.getStart()));
        traceVariable("bitmappedEnd", Lisp2Bitmap.getAddressForBitmapWord(Lisp2Bitmap.getEnd()));

        int overhead = 100 - asPercentOf(heapSize, memoryEnd.diff(memoryStart).toInt());
        traceVariable("overhead(%)", overhead);
    }



    /**
     * {@inheritDoc}
     */
    void traceHeap(String description, Address allocationPointer) {
        traceHeapStart(description, freeMemory(allocationPointer), totalMemory());

        int youngGenerationSize = getYoungGenerationSize();
        Address youngGenerationEnd = youngGenerationStart.add(youngGenerationSize);
        Address object, block;

        traceHeapSegment("sliceTable", sliceTable, sliceTable.add(sliceTableSize));
        traceHeapSegment("bitmap", Lisp2Bitmap.getStart(), Lisp2Bitmap.getEnd());
        traceHeapSegment("minimumMarkingStack", heapEnd, Lisp2Bitmap.getStart());
        traceHeapSegment("heap{unused}", youngGenerationEnd, heapEnd);
        traceHeapSegment("heap{youngGenerationFree}", allocationPointer, youngGenerationEnd);
        traceHeapSegment("heap{youngGenerationAllocated}", youngGenerationStart, allocationPointer);
        traceHeapSegment("heap{oldGeneration}", heapStart, youngGenerationStart);
        traceHeapSegment("permanentSpace", memoryStart, heapStart);

        if (GC.isTracing(GC.TRACE_HEAP_CONTENTS)) {
            for (block = permanentMemoryStart; block.lo(memoryStart); ) {
                object = GC.blockToOop(block);
                Klass klass = GC.getKlass(object);
                int size = GC.getBodySize(klass, object);
                traceHeapObject(block, object, klass, size / HDR.BYTES_PER_WORD);
                block = object.add(size);
            }

            for (block = heapStart; block.lo(allocationPointer); ) {
                object = GC.blockToOop(block);
                Klass klass = GC.getKlass(object);
                int size = GC.getBodySize(klass, object);
                traceHeapObject(block, object, klass, size / HDR.BYTES_PER_WORD);
                block = object.add(size);
            }
        }

        traceHeapEnd();
    }
}
