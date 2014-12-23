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
 * A collection of methods for performing peek and poke operations on memory addresses. <p>
 * 
 * The Unsafe class is primarily concerned with accessing memory ouside of the Java heap, but improper use of the Unsafe
 * class can corrupt the Java heap (hence the name).<p>
 * 
 * Only "trusted" Isolates may 
 * use the methods of this class. Calls to Any memory accessor methods by an untrusted Isolate will throw a SecurityException.
 *
 * @see Isolate
 *
 */
public final class Unsafe {
    
    private Unsafe() {}
    
    /**
     * Performs access check.
     */
    static {
        if (!VM.getCurrentIsolate().isTrusted()) {
            throw new SecurityException("illegal access to com.sun.squawk.Unsafe");
        }
    }
    
    /*-----------------------------------------------------------------------*\
     *                      Storing to/loading from memory                   *
    \*-----------------------------------------------------------------------*/
    
    
    /**
     * Sets an 8 bit value in memory.
     *
     * @param base   the base address
     * @param offset the offset (in bytes) from <code>base</code> at which to write
     * @param value the value to write
     */
    public static void setByte(Address base, int offset, int value) {
        NativeUnsafe.setByte(base, offset, value);
    }
    
    /**
     * Sets a signed 16 bit value in memory.
     *
     * @param base   the base address
     * @param offset the offset (in 16 bit words) from <code>base</code> at which to write
     * @param value  the value to write
     */
    public static void setShort(Address base, int offset, int value) {
        NativeUnsafe.setShort(base, offset, value);
    }
    
    /**
     * Sets an unsigned 16 bit value in memory.
     *
     * @param base   the base address
     * @param offset the offset (in 16 bit words) from <code>base</code> at which to write
     * @param value  the value to write
     */
    public static void setChar(Address base, int offset, int value) {
        NativeUnsafe.setChar(base, offset, value);
    }
    
    /**
     * Sets a 32 bit value in memory.
     *
     * @param base   the base address
     * @param offset the offset (in 32 bit words) from <code>base</code> at which to write
     * @param value  the value to write
     */
    public static void setInt(Address base, int offset, int value) {
        NativeUnsafe.setInt(base, offset, value);
    }
    
    /**
     * Sets a UWord value in memory.
     *
     * @param base   the base address
     * @param offset the offset (in UWords) from <code>base</code> at which to write
     * @param value  the value to write
     */
    public static void setUWord(Address base, int offset, UWord value) {
        NativeUnsafe.setUWord(base, offset, value);
    }
    
    /**
     * Sets a 64 bit value in memory.
     *
     * @param base   the base address
     * @param offset the offset (in 64 bit words) from <code>base</code> at which to write
     * @param value  the value to write
     */
    public static void setLong(Address base, int offset, long value) {
        NativeUnsafe.setLong(base, offset, value);
    }
    
    /**
     * Sets a 64 bit value in memory at a 32 bit word offset.
     *
     * @param base   the base address
     * @param offset the offset (in 32 bit words) from <code>base</code> at which to write
     * @param value  the value to write
     */
    public static void setLongAtWord(Address base, int offset, long value) {
        NativeUnsafe.setLongAtWord(base, offset, value);
    }
    
    /**
     * Sets a pointer value in memory without updating the write barrier.
     *
     * If this method is being called in a
     * {@link VM#isHosted() hosted} environment then the corresponding bit in the
     * oop map (if any) is also set.
     *
     * @param base   the base address
     * @param offset the offset (in UWords) from <code>base</code> at which to write
     * @param value  the value to write
     */
    public static void setAddress(Address base, int offset, Address value) {
        NativeUnsafe.setAddress(base, offset, value);
    }
    
//    /**
//     * Sets a pointer value in memory and updates the write barrier.
//     *
//     * @param base   the base address
//     * @param offset the offset (in UWords) from <code>base</code> at which to write
//     * @param value  the value to write
//     */
//    public static void setObject(Address base, int offset, Address value) {
//        NativeUnsafe.setObject(base, offset, value);
//    }
    
    /**
     * Gets a signed 8 bit value from memory.
     *
     * @param base   the base address
     * @param offset the offset (in bytes) from <code>base</code> from which to load
     * @return the value
     */
    public static int getByte(Address base, int offset) {
        return NativeUnsafe.getByte(base, offset);
    }
    
    /**
     * Gets an unsigned 8 bit value from memory.
     *
     * @param base   the base address
     * @param offset the offset (in bytes) from <code>base</code> from which to load
     * @return the value
     */
    public static int getUByte(Address base, int offset) {
        return NativeUnsafe.getUByte(base, offset);
    }
    
    /**
     * Gets a signed 16 bit value from memory.
     *
     * @param base   the base address
     * @param offset the offset (in 16 bit words) from <code>base</code> from which to load
     * @return the value
     */
    public static int getShort(Address base, int offset) {
        return NativeUnsafe.getShort(base, offset);
    }
    
    /**
     * Gets an unsigned 16 bit value from memory.
     *
     * @param base   the base address
     * @param offset the offset (in 16 bit words) from <code>base</code> from which to load
     * @return the value
     */
    public static int getChar(Address base, int offset) {
        return NativeUnsafe.getChar(base, offset);
    }
    
    
    /**
     * Gets a signed 32 bit value from memory.
     *
     * @param base   the base address
     * @param offset the offset (in 32 bit words) from <code>base</code> from which to load
     * @return the value
     */
    public static int getInt(Address base, int offset) {
        return NativeUnsafe.getInt(base, offset);
    }
    
    /**
     * Gets an unsigned 32 or 64 bit value from memory.
     *
     * @param base   the base address
     * @param offset the offset (in UWords) from <code>base</code> from which to load
     * @return the value
     */
    public static UWord getUWord(Address base, int offset) {
        return NativeUnsafe.getUWord(base, offset);
    }
    
    /**
     * Gets a 64 bit value from memory using a 64 bit word offset.
     *
     * @param base   the base address
     * @param offset the offset (in 64 bit words) from <code>base</code> from which to load
     * @return the value
     */
    public static long getLong(Address base, int offset) {
        return NativeUnsafe.getLong(base, offset);
    }
    
    /**
     * Gets a 64 bit value from memory using a 32 bit word offset.
     *
     * @param base   the base address
     * @param offset the offset (in 32 bit words) from <code>base</code> from which to load
     * @return the value
     */
    public static long getLongAtWord(Address base, int offset) {
        return NativeUnsafe.getLongAtWord(base, offset);
    }
    
    /**
     * Gets a pointer from memory as an Address.
     *
     * @param base   the base address
     * @param offset the offset (in UWords) from <code>base</code> from which to load
     * @return the value
     */
    public static Address getAddress(Address base, int offset) {
        return NativeUnsafe.getAddress(base, offset);
    }

    
// The main uses of the Unsafe memory are to non-Java heap areas, which are NOT covered by the TypeMap anyway, so
// users shouldn't need these versions.
//    /**
//     * Gets a UWord value from memory ignoring any recorded type of the value at the designated location.
//     * This operation is equivalent to {@link #getUWord(Address, int)} when {@link VM#usingTypeMap() runtime type checking}
//     * is disabled.
//     *
//     * @param base   the base address
//     * @param offset the offset (in words) from <code>base</code> from which to load
//     * @return the value
//     */
//    public static UWord getAsUWord(Address base, int offset) {
//        return NativeUnsafe.getAsUWord(base, offset);
//    }
//    
//    /**
//     * Gets a signed 8 bit value from memory ignoring any recorded type of the value at the designated location.
//     * This operation is equivalent to {@link #getByte(Address, int)} when {@link VM#usingTypeMap() runtime type checking}
//     * is disabled.
//     *
//     * @param base   the base address
//     * @param offset the offset (in 8 bit words) from <code>base</code> from which to load
//     * @return the value
//     */
//    public static int getAsByte(Address base, int offset) {
//        return NativeUnsafe.getAsByte(base, offset);
//    }
//    
//    /**
//     * Gets a signed 16 bit value from memory ignoring any recorded type of the value at the designated location.
//     * This operation is equivalent to {@link #getShort(Address, int)} when {@link VM#usingTypeMap() runtime type checking}
//     * is disabled.
//     *
//     * @param base   the base address
//     * @param offset the offset (in 16 bit words) from <code>base</code> from which to load
//     * @return the value
//     */
//    public static int getAsShort(Address base, int offset) {
//        return NativeUnsafe.getAsByte(base, offset);
//    }
//    /**
//     * Gets a signed 32 bit value from memory ignoring any recorded type of the value at the designated location.
//     * This operation is equivalent to {@link #getInt(Address, int)} when {@link VM#usingTypeMap() runtime type checking}
//     * is disabled.
//     *
//     * @param base   the base address
//     * @param offset the offset (in 32 bit words) from <code>base</code> from which to load
//     * @return the value
//     */
//    public static int getAsInt(Address base, int offset) {
//        return NativeUnsafe.getAsByte(base, offset);
//    }
    
    /**
     * Copy from memory to byte array.
     * Copy <code>number</code> bytes from the memory location specified by the address <code>dst</code> and byte offset <code>boffset</code> to 
     * the byte array <code>bytes</code> starting at position <code>low</code>.
     *
     * @param src the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param bytes the destination byte array
     * @param low the offset in the destination array
     * @param number the number of bytes to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the dst array
     */
    public static void getBytes(Address src, int boffset, byte[] bytes, int low, int number) {
        VM.getData(src, boffset, bytes, low, number, 1);
    }
    
    /**
     * Copy from memory to short array.
     * Copy <code>number</code> shorts from the memory location specified by the address <code>dst</code> and byte offset <code>boffset</code> to 
     * the short array <code>shorts</code> starting at position <code>low</code>.
     *
     * @param src the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param shorts the destination short array
     * @param low the offset in the destination array
     * @param number the number of shorts to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the dst array
     */
    public static void getShorts(Address src, int boffset, short[] shorts, int low, int number) {
        VM.getData(src, boffset, shorts, low, number, 2);
    }
    
    /**
     * Copy from memory to int array.
     * Copy <code>number</code> ints from the memory location specified by the address <code>dst</code> and byte offset <code>boffset</code> to 
     * the int array <code>ints</code> starting at position <code>low</code>.
     *
     * @param src the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param ints the destination int array
     * @param low the offset in the destination array
     * @param number the number of ints to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the dst array
     */
    public static void getInts(Address src, int boffset, int[] ints, int low, int number) {
        VM.getData(src, boffset, ints, low, number, 4);
    }
    
    /**
     * Copy from memory to long array.
     * Copy <code>number</code> longs from the memory location specified by the address <code>dst</code> and byte offset <code>boffset</code> to 
     * the long array <code>longs</code> starting at position <code>low</code>.
     *
     * @param src the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param longs the destination long array
     * @param low the offset in the destination array
     * @param number the number of ints to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the dst array
     */
    public static void getLongs(Address src, int boffset, long[] longs, int low, int number) {
        VM.getData(src, boffset, longs, low, number, 8);
    }
    
    /**
     * Copy from byte array to memory.
     * Copy <code>number</code> bytes from byte array <code>bytes</code> starting at position <code>low</code>.to the memory location specified
     * by the address <code>dst</code> and byte offset <code>boffset</code>.
     *
     * @param dst the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param bytes the src byte array
     * @param low the offset in the src array
     * @param number the number of bytes to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the src array
     */
    public static void setBytes(Address dst, int boffset, byte[] bytes, int low, int number) {
        VM.setData(dst, boffset, bytes, low, number, 1);
    }
    
    /**
     * Copy from short array to memory.
     * Copy <code>number</code> shorts from short array <code>shorts</code> starting at position <code>low</code>.to the memory location specified
     * by the address <code>dst</code> and byte offset <code>boffset</code>.
     *
     * @param dst the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param shorts the src short array
     * @param low the offset in the src array
     * @param number the number of bytes to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the src array
     */
    public static void setShorts(Address dst, int boffset, short[] shorts, int low, int number) {
        VM.setData(dst, boffset, shorts, low, number, 2);
    }
    
    /**
     * Copy from int array to memory.
     * Copy <code>number</code> int from int array <code>ints</code> starting at position <code>low</code>.to the memory location specified
     * by the address <code>dst</code> and byte offset <code>boffset</code>.
     *
     * @param dst the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param ints the src int array
     * @param low the offset in the src array
     * @param number the number of bytes to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the src array
     */
    public static void setInts(Address dst, int boffset, int[] ints, int low, int number) {
        VM.setData(dst, boffset, ints, low, number, 4);
    }
    
    /**
     * Copy from long array to memory.
     * Copy <code>number</code> longs from long array <code>longs</code> starting at position <code>low</code>.to the memory location specified
     * by the address <code>dst</code> and byte offset <code>boffset</code>.
     *
     * @param dst the base memory address
     * @param boffset the byte offset to add to the base memory address
     * @param longs the src long array
     * @param low the offset in the src array
     * @param number the number of bytes to copy
     * @throws ArrayIndexOutOfBoundsException if the range specified by low and number does not fit within the src array
     */
    public static void setLongs(Address dst, int boffset, long[] longs, int low, int number) {
        VM.setData(dst, boffset, longs, low, number, 8);
    }
    
    /**
     * Gets the <code>short</code> at the given <b>byte</b> offset in the memory, starting from base.
     *  If the short is aligned on a "natural"
     *  boundary it is always loaded from memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be loaded atomically, and
     *  the number and order of the load operations is unspecified.
     *
     * @param base address of to region of memory
     * @param boffset The offset in bytes from base to the short to be loaded
     *
     * @return The short from raw memory.
     */
    public static int getUnalignedShort(Address base, int boffset) {
        return NativeUnsafe.getUnalignedShort(base, boffset);
    }

    /**
     * Gets the <code>int</code> at the given <b>byte</b> offset in the memory, starting from base.
     *  If the integer is aligned on a "natural"
     *  boundary it is always loaded from memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be loaded atomically, and
     *  the number and order of the load operations is unspecified.
     *
     * @param base address of to region of memory
     * @param boffset The offset in bytes from base to the int to be loaded
     *
     * @return The integer from raw memory.
     */
    public static int getUnalignedInt(Address base, int boffset) {
        return NativeUnsafe.getUnalignedInt(base, boffset);
    }

    /**
     * Gets the <code>long</code> at the given <b>byte</b> offset in the memory, starting from base.
     *  If the long is aligned on a "natural"
     *  boundary it is always loaded from memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be loaded atomically, and
     *  the number and order of the load operations is unspecified.
     *
     * @param base address of to region of memory
     * @param boffset The offset in bytes from base to the long to be loaded
     *
     * @return The long from raw memory.
     */
    public static long getUnalignedLong(Address base, int boffset) {
        return NativeUnsafe.getUnalignedLong(base, boffset);
    }

    /**
     * Sets the <code>short</code> at the given <b>byte</b> offset in the memory, starting from base.
     *  If the short is aligned on a "natural"
     *  boundary it is always stored to memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be stored atomically, and
     *  the number and order of the store operations is unspecified.
     *
     * @param base address of to region of memory
     * @param boffset The offset in bytes from base to the location to be stored
     * @param value 
     */
    public static void setUnalignedShort(Address base, int boffset, int value) {
        NativeUnsafe.setUnalignedShort(base, boffset, value);
    }

    /**
     * Gets the <code>int</code> at the given <b>byte</b> offset in the memory, starting from base.
     *  If the integer is aligned on a "natural"
     *  boundary it is always stored to memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be stored atomically, and
     *  the number and order of the store operations is unspecified.
     *
     * @param base address of to region of memory
     * @param boffset The offset in bytes from base to the location to be stored
     * @param value 
     */
    public static void setUnalignedInt(Address base, int boffset, int value) {
        NativeUnsafe.setUnalignedInt(base, boffset, value);
    }

    /**
     * Gets the <code>long</code> at the given <b>byte</b> offset in the memory, starting from base.
     *  If the long is aligned on a "natural"
     *  boundary it is always stored to memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be stored atomically, and
     *  the number and order of the store operations is unspecified.
     *
     * @param base address of to region of memory
     * @param boffset The offset in bytes from base to the location to be stored
     * @param value 
     */
    public static void setUnalignedLong(Address base, int boffset, long value) {
        NativeUnsafe.setUnalignedLong(base, boffset, value);
    }

    
    
}
