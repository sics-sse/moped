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
 * A ObjectMemoryEndianessSwapper instance is used to swap the endianess of all the data values in
 * an {@link ObjectMemory} that are accessed via direct loads by the machine where such loads
 * assume a fixed endianess of the data.
 *
 */
public final class ObjectMemoryEndianessSwapper {

    /**
     * The Objectmemory whose endianess is being swapped.
     */
    private final ObjectMemory om;

    /**
     * The object currently having its endianess swapped.
     */
    private Address object;

    /**
     * True if the object memory was originally in the opposite endianess of the host platform.
     */
    private final boolean toPlatform;

    /**
     * True if the addresses in the object memory are in canonical form.
     */
    private final boolean isCanonical;

    /**
     * Swaps the endianess of all the data values in <code>om</code> that are accessed via
     * direct loads by the machine where such loads assume a fixed endianess of the data.
     *
     * @param om          the object memory whose endianess is to be swapped
     * @param toPlatform  true if <code>om</code> is in the opposite endianess of the host platform
     * @param isCanonical true if <code>om</code> is in canonical form (i.e. has not had its pointers relocated)
     */
    public static void swap(ObjectMemory om, boolean toPlatform, boolean isCanonical) {

        if (Klass.TRACING_ENABLED && Tracer.isTracing("swapper")) {
            VM.print("ObjectMemoryEndianessSwapper::swap - om = { ");
            VM.printAddress(om.getStart());
            VM.print(", ");
            VM.printAddress(om.getEnd());
            VM.print(", ");
            VM.printAddress(om.getCanonicalStart());
            VM.print(", ");
            VM.printAddress(om.getCanonicalEnd());
            VM.print("} toPlatform = ");
            VM.print(toPlatform);
            VM.print(" isCanonical = ");
            VM.print(isCanonical);
            VM.println();
        }

        new ObjectMemoryEndianessSwapper(om, toPlatform, isCanonical).run();
    }

    /**
     * Creates a swapper.
     *
     * @param om          the object memory whose endianess is to be swapped
     * @param toPlatform  true if <code>om</code> is in the opposite endianess of the host platform
     * @param isCanonical true is <code>om</code> is in canonical form (i.e. has not had its pointers relocated)
     */
    private ObjectMemoryEndianessSwapper(ObjectMemory om, boolean toPlatform, boolean isCanonical) {
        this.om = om;
        this.toPlatform = toPlatform;
        this.isCanonical = isCanonical;
    }

    /**
     * Determines if the value at a given address needs to be swapped before being loaded.
     *
     * @param ea   the address of a value about to be loaded from memory
     * @return true if the value at <code>ea</code> is in the wrong endianess for the load
     */
    private boolean requiresSwapping(Address ea) {
        Assert.that(om.containsAddress(ea));
        if (toPlatform) {
            return ea.hieq(object);
        } else {
            return ea.lo(object);
        }
    }

    /**
     * Gets an int value from memory, swapping the value at the effective address if necessary before loading it.
     * If swapping is performed before the load, it is undone before returning the value.
     *
     * @param base   the base address
     * @param offset the offset (in 32 bit units) from <code>base</code> of the value
     * @return the value
     */
    private int getInt(Address base, int offset) {
        Address ea = base.add(offset * 4);
        int result;

        if (om.containsAddress(ea)) {
            if (requiresSwapping(ea)) {
                NativeUnsafe.swap4(ea);
                result = NativeUnsafe.getInt(base, offset);
                NativeUnsafe.swap4(ea);
            } else {
                result = NativeUnsafe.getInt(base, offset);
            }

        } else {
            result = NativeUnsafe.getInt(base, offset);
        }

        return result;
    }

    /**
     * Gets a short value from memory, swapping the value at the effective address if necessary before loading it.
     * If swapping is performed before the load, it is undone before returning the value.
     *
     * @param base   the base address
     * @param offset the offset (in 16 bit units) from <code>base</code> of the value
     * @return the value
     */
    private int getShort(Address base, int offset) {
        Address ea = base.add(offset * 2);
        int result;

        if (om.containsAddress(ea)) {
            if (requiresSwapping(ea)) {
                NativeUnsafe.swap2(ea);
                result = NativeUnsafe.getShort(base, offset);
                NativeUnsafe.swap2(ea);
            } else {
                result = NativeUnsafe.getShort(base, offset);
            }

        } else {
            result = NativeUnsafe.getShort(base, offset);
        }

        return result;
    }

    /**
     * Gets a word value from memory, swapping the value at the effective address if necessary before loading it.
     * If swapping is performed before the load, it is undone before returning the value.
     *
     * @param base   the base address
     * @param offset the offset (in words) from <code>base</code> of the value
     * @return the value
     */
    private UWord getUWord(Address base, int offset) {
        Address ea = base.add(offset * HDR.BYTES_PER_WORD);
        UWord result;

        if (om.containsAddress(ea)) {
            if (requiresSwapping(ea)) {
                NativeUnsafe.swap(ea, HDR.BYTES_PER_WORD);
                result = NativeUnsafe.getUWord(base, offset);
                NativeUnsafe.swap(ea, HDR.BYTES_PER_WORD);
            } else {
                result = NativeUnsafe.getUWord(base, offset);
            }
        } else {
            result = NativeUnsafe.getUWord(base, offset);
        }

        return result;
    }

    /**
     * Gets an address value from memory, swapping the value at the effective address if necessary before loading it.
     * If swapping is performed before the load, it is undone before returning the value.
     *
     * @param base   the base address
     * @param offset the offset (in words) from <code>base</code> of the value
     * @return the value
     */
    private Address getAddress(Address base, int offset) {
        Address ea = base.add(offset * HDR.BYTES_PER_WORD);
        Address result;

        if (om.containsAddress(ea)) {
            if (requiresSwapping(ea)) {
                NativeUnsafe.swap(ea, HDR.BYTES_PER_WORD);
                result = NativeUnsafe.getAddress(base, offset);
                NativeUnsafe.swap(ea, HDR.BYTES_PER_WORD);
            } else {
                result = NativeUnsafe.getAddress(base, offset);
            }

            // If the pointer is in canonical form, it must de-canonicalised
            if (isCanonical) {
                ObjectMemory om = this.om.findCanonicalAddress(result);
                result = om.fromCanonical(result);
            }

        } else {
            result = NativeUnsafe.getAddress(base, offset);
        }
        return result;
    }

    /**
     * Swaps the endianess of a word sized value.
     *
     * @param base   the base address
     * @param offset the offset (in words) from <code>base</code> of the value
     */
    private static void swapWord(Address base, int offset) {
        Address ea = base.add(offset * HDR.BYTES_PER_WORD);
        NativeUnsafe.swap(ea, HDR.BYTES_PER_WORD);
    }

    /**
     * Performs the swapping.
     */
    private void run() {

        Address end = om.getEnd();

        Address klass;
        Address classOrAssociation;

        Address dataMap;
        UWord dataMapWord;

        for (Address block = om.getStart(); block.lo(end); ) {

            // Load header
            if (toPlatform) {
                swapWord(block, 0);
            }

            object = GC.blockToOop(block);
            int headerSize = object.diff(block).toInt();

            if (Klass.TRACING_ENABLED && Tracer.isTracing("swapper")) {
                VM.print("ObjectMemoryEndianessSwapper::run - block = ");
                VM.printAddress(block);
                VM.print(", object = ");
                VM.printAddress(object);
                VM.print(", canonicalBlock = ");
                VM.printAddress(om.toCanonical(block));
                VM.print(", canonicalObject = ");
                VM.printAddress(om.toCanonical(block).add(headerSize));
                VM.println();
            }

            // Swap the rest of the header words
            if (!toPlatform) {
                swapWord(block, 0);
            }
            if (headerSize == HDR.basicHeaderSize) {
                // This is a non-array object: nothing to do
            } else {
                swapWord(object, HDR.klass);
                if (headerSize == HDR.arrayHeaderSize) {
                    // This is an array: nothing to do
                } else {
                    // This is a method body: swap the bytecode array length and defining class pointer
                    swapWord(object, HDR.length);
                    swapWord(object, HDR.methodDefiningClass);
                }
            }

            // Decode the address of the object's class
            classOrAssociation = getAddress(object, HDR.klass);
            klass = getAddress(classOrAssociation, (int)FieldOffsets.com_sun_squawk_Klass$self);

            if (headerSize != HDR.basicHeaderSize) {

                int dataSize;
                int length = (int)GC.decodeLengthWord(getUWord(object, HDR.length));

                // This is a method body
                if (headerSize != HDR.arrayHeaderSize) {

                    swapInlineConstants(length);
                    dataSize = 1;
                } else {
                    int encodedDataSize = getUWord(klass, (int) FieldOffsets.com_sun_squawk_Klass$dataMapWord).toInt();
                    dataSize = 1 << (encodedDataSize & Klass.DATAMAP_ENTRY_MASK);

                    // Don't bother swapping byte arrays
                    if (dataSize > 1) {
                        for (int i = 0; i < length; i++) {
                            NativeUnsafe.swap(object.add(i * dataSize), dataSize);
                        }
                    }
                }
                // Calculate the next block
                block = object.add(length * dataSize).roundUpToWord();

            } else {

                int dataMapLength = getShort(klass, (int)FieldOffsets.com_sun_squawk_Klass$dataMapLength);
                int byteOffset = 0;

                if (dataMapLength > Klass.DATAMAP_ENTRIES_PER_WORD) {
                    dataMap = getAddress(klass, (int)FieldOffsets.com_sun_squawk_Klass$dataMap);
                    int i = 0;
                    while (dataMapLength > 0) {
                        dataMapWord = getUWord(dataMap, i++);
                        byteOffset = swapFields(object, byteOffset, dataMapWord, Math.min(dataMapLength, Klass.DATAMAP_ENTRIES_PER_WORD));
                        dataMapLength -= Klass.DATAMAP_ENTRIES_PER_WORD;
                    }
                } else {
                    dataMapWord = getUWord(klass, (int)FieldOffsets.com_sun_squawk_Klass$dataMapWord);
                    byteOffset = swapFields(object, 0, dataMapWord, Math.min(dataMapLength, Klass.DATAMAP_ENTRIES_PER_WORD));
                }

                // Calculate the next block
                block = object.add(byteOffset).roundUpToWord();
            }
        }
    }

    /**
     * Looks within a method for inline constants that are accessed via direct loads (and therefore
     * need to be in the correct endianess) and swaps their endianess.
     *
     * @param  length   the number of bytes in the method body
     */
    private void swapInlineConstants(int length) {

        // Inspect each instruction
        for (int ip = 0; ip < length; ) {

            int opcode = (0xFF & NativeUnsafe.getAsByte(object, ip));
            // If opcode is ESCAPE then we need to add 256 to the following opcode to get the real opcode
            if (opcode == OPC.ESCAPE) {
                ip++;
                opcode = (0xFF & NativeUnsafe.getAsByte(object, ip)) + 256;
            }
            switch (opcode) {

                // Swap 2 byte constant
                case OPC.CONST_SHORT:
                case OPC.CONST_CHAR: {
                    NativeUnsafe.swap2(object.add(ip + 1));
                    ip += OPC.getSize(opcode);
                    break;
                }

                // Swap 4 byte constant
                case OPC.CONST_INT:
/*if[FLOATS]*/
                case OPC.CONST_FLOAT:
/*end[FLOATS]*/
                {
                    NativeUnsafe.swap4(object.add(ip + 1));
                    ip += OPC.getSize(opcode);
                    break;
                }

                // Swap 8 byte constant
                case OPC.CONST_LONG:
/*if[FLOATS]*/
                case OPC.CONST_DOUBLE:
/*end[FLOATS]*/
                {
                    NativeUnsafe.swap8(object.add(ip + 1));
                    ip += OPC.getSize(opcode);
                    break;
                }

                case OPC.WIDE_M1:
                case OPC.WIDE_0:
                case OPC.WIDE_1:
                case OPC.ESCAPE_WIDE_M1:
                case OPC.ESCAPE_WIDE_0:
                case OPC.ESCAPE_WIDE_1: {
                    // 1 byte for OPC.WIDE_* or OPC.ESCAPE_WIDE_*, 1 byte for real opcode + 1 byte for operand
                    ip += 3;
                    break;
                }

                // If we have a WIDE_SHORT, the operand is 2 bytes
                case OPC.ESCAPE_WIDE_SHORT: // Fall through
                case OPC.WIDE_SHORT: {
                    NativeUnsafe.swap2(object.add(ip + 2));
                    // 1 byte for OPC.WIDE_SHORT, 1 byte for next instruction + 2 bytes for short
                    ip += 4;
                    break;
                }

                // If we have a WIDE_INT, the operand is 4 bytes
                case OPC.WIDE_INT: // Fall through
                case OPC.ESCAPE_WIDE_INT: {
                    // 1 byte for OPC.WIDE_SHORT, 1 byte for next instruction + 4 bytes for int
                    NativeUnsafe.swap4(object.add(ip + 2));
                    ip += 6;
                    break;
                }

                // Fix switch tables
                case OPC.TABLESWITCH_S: {
                    ip = swapSwitchTable(ip, 2);
                    break;
                }
                case OPC.TABLESWITCH_I: {
                    ip = swapSwitchTable(ip, 4);
                    break;
                }

                // Nothing to fix, continue to next instructions
                default: {
                    ip += OPC.getSize(opcode);
                }
            }
        }
    }

    /**
     * Swaps the entries in a switch statement table
     *
     * @param   ip   the byte index into the method body
     * @return  the byte index into method body for the instruction following switch table
     */
    private int swapSwitchTable(int ip, int dataSize) {

        Address lowAddress, highAddress, defAddress, tableEntriesAddress;

        int padding = (dataSize - ( (ip + 1) % dataSize)) % dataSize;

        // Calculate address for table characteristics
        lowAddress = object.add(ip + 1 + padding);
        highAddress = lowAddress.add(dataSize);
        defAddress = highAddress.add(dataSize);
        tableEntriesAddress = defAddress.add(dataSize);

        if (toPlatform) {
            NativeUnsafe.swap(lowAddress, dataSize);
            NativeUnsafe.swap(highAddress, dataSize);
            NativeUnsafe.swap(defAddress, dataSize);
        }

        // Upper and lower bounds of table entries
        int low, high;

        if (dataSize == 2) {
            low = NativeUnsafe.getAsShort(lowAddress, 0);
            high = NativeUnsafe.getAsShort(highAddress, 0);
        } else {
            low = NativeUnsafe.getAsInt(lowAddress, 0);
            high = NativeUnsafe.getAsInt(highAddress, 0);
        }

        if (!toPlatform) {
            NativeUnsafe.swap(lowAddress, dataSize);
            NativeUnsafe.swap(highAddress, dataSize);
            NativeUnsafe.swap(defAddress, dataSize);
        }

        // Swap the jump table
        int entries = (high - low) + 1;
        for (int j = 0; j < entries; j++) {
            NativeUnsafe.swap(tableEntriesAddress.add(j * dataSize), dataSize);
        }

        // header is 3*dataSize + padding bytes + instruction bytecode
        ip += (entries * dataSize) + (padding + (3 * dataSize) + 1);
        return ip;
    }

    /**
     * Swaps the endian of the fields corresponding to the entries in a given (partial) data map.
     *
     * @param object      the object whose fields are being swapped
     * @param byteOffset  the offset (in bytes) from <code>object</code> of the field to start swapping from
     * @param dataMapWord the (portion of the) data map describing the data size of the fields
     * @param entries     the number of fields described by <code>dataMapWord</code>
     * @return the offset (in bytes) from <code>object</code> one past the last field swapped
     */
    private static int swapFields(Address object, int byteOffset, UWord dataMapWord, int entries) {
        for (int i = 0; i != entries; ++i) {

            int log2DataSize = (int)(dataMapWord.toPrimitive() >> ((i % Klass.DATAMAP_ENTRIES_PER_WORD) * Klass.DATAMAP_ENTRY_BITS)) & 0x03;
            int dataSize = (1 << log2DataSize);

            // Offset is in terms of dataSize
            NativeUnsafe.swap(object.add(byteOffset), dataSize);

            byteOffset += dataSize;
        }
        return byteOffset;
    }
}
