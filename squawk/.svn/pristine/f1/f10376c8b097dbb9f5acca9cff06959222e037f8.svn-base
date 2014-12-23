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

/**
 * Dummy version of Pointer used by Import declarations.
 */
public class Pointer {
    private static Pointer NULL;
    
    public static synchronized Pointer NULL() {
        return null;
    }
    
    /**
     * Create a pointer and allocate backing native memory of the requestsed size.
     * This backing memory can be freed later using {@link #free()}.
     * 
     * @param size the number of bytes that can be referenced from this pointer
     */
    public Pointer(int size) {
    }
    
    /**
     * Create a pointer that refers to a memory range from [base..base+size).
     * 
     * This pointer should NOT be freed.
     * 
     * @param base the base address of the pointer
     * @param size the number of bytes that can be referenced from this pointer
     */
    public Pointer(long base, int size) {
    }
    
    /*---------------------------------- From RawMemoryAccess ---------------------------*/
    
    /**
     * Gets the <code>byte</code> at the given offset in the memory area
     *  associated with this object.  The byte is always loaded from memory
     *  in a single atomic operation.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory
     *  from which to load the byte.
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if the byte falls in an invalid address range.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     *
     *
     * @return The byte from raw memory.
     */
    public byte getByte(long offset) {
        return -1;
    }

    /**
     * Gets <code>number</code> bytes starting at the given offset in the memory area
     *  associated with this object and assigns
     * them to the byte array passed starting at position <code>low</code>.
     *  Each byte is loaded from memory in a single atomic operation.  Groups of bytes
     *  may be loaded together, but this is unspecified.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory from which to start loading.
     *
     * @param bytes The array into which the loaded items are placed.
     *
     * @param low The offset which is the starting point in the given array for the
     *            loaded items to be placed.
     *
     * @param number The number of items to load.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the byte falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The <code>bytes</code> array could, therefore, be
     *      partially updated if the raw memory is unmapped or remapped mid-method.
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     *
     */
    public void getBytes(long offset, byte[] bytes, int low, int number) {
    }

    /**
     * Gets the <code>int</code> at the given offset in the memory area
     *  associated with this object.  If the integer is aligned on a "natural"
     *  boundary it is always loaded from memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be loaded atomically, and
     *  the number and order of the load operations is unspecified.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to load the integer.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the integer falls in an invalid address range.
     *
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     *
     * @return The integer from raw memory.
     */
    public int getInt(long offset) {
        return -1;
    }

    /**
     * Gets <code>number</code> integers starting at the given offset in the memory area
     *  associated with this object and assign
     * them to the int array passed starting at position <code>low</code>.
     * <p>
     *  If the integers are aligned on natural boundaries
     *  each integer is loaded from memory in a single atomic operation.  Groups of integers
     *  may be loaded together, but this is unspecified.
     *<p>
     *  If the integers are not aligned on natural boundaries they may not be loaded atomically and
     *  the number and order of load operations is unspecified.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      at which to start loading.
     *
     * @param ints The array into which the integers read from the raw memory are placed.
     *
     * @param low The offset which is the starting point in the given array for the
     *            loaded items to be placed.
     *
     * @param number The number of integers to loaded.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if the integers fall in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The <code>ints</code> array could, therefore, be
     *      partially updated if the raw memory is unmapped or remapped mid-method.
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void getInts(long offset, int[] ints, int low, int number) {
    }

    /**
     * Gets the <code>long</code> at the given offset in the memory area
     *  associated with this object.
     * <p>
     *  The load is not required to be atomic even it is located on a natural boundary.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to load the long.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is invalid.
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the double falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     *
     * @return The long from raw memory.
     */
    public long getLong(long offset) {
        return -1;
    }

    /**
     * Gets <code>number</code> longs starting at the given offset in the memory area
     *  associated with this object and assign
     * them to the long array passed starting at position <code>low</code>.
     * <p>
     *  The loads are not required to be atomic even if they are located on natural boundaries.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      at which to start loading.
     *
     * @param longs The array into which the loaded items are placed.
     *
     * @param low The offset which is the starting point in the given array for the
     *            loaded items to be placed.
     *
     * @param number The number of longs to load.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if a long falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The <code>longs</code> array could, therefore, be
     *      partially updated if the raw memory is unmapped or remapped mid-method.
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void getLongs(long offset, long[] longs, int low, int number) {
    }

    /**
     * Gets the virtual memory location at which the memory region is mapped.
     *
     * @return The virtual address to which this is mapped (for reference
     *         purposes). Same as the base address if virtual memory is not supported.
     *
     *  @throws IllegalStateException Thrown if the raw memory object is not in the
     *      mapped state.
     */
    public long getMappedAddress() {
        return -1;
    }

    /**
     * Gets the <code>short</code> at the given offset in the memory area
     *  associated with this object. If the short is aligned on a natural
     *  boundary it is always loaded from memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be loaded atomically, and
     *  the number and order of the load operations is unspecified.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to load the short.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the short falls in an invalid address range.
     *
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     * @return The short loaded from raw memory.
     */
    public short getShort(long offset) {
        return -1;
    }

    /**
     * Gets <code>number</code> shorts starting at the given offset in the memory area
     *  associated with this object and assign
     * them to the short array passed starting at position <code>low</code>.
     * <p>
     *  If the shorts are located on natural boundaries
     *  each short is loaded from memory in a single atomic operation.  Groups of shorts
     *  may be loaded together, but this is unspecified.
     *  <p>
     *  If the shorts are not located on natural boundaries the load may not be atomic, and
     *  the number and order of load
     *  operations is unspecified.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       from which to start loading.
     *
     * @param shorts The array into which the loaded items are placed.
     *
     * @param low The offset which is the starting point in the given array for the
     *            loaded shorts to be placed.
     *
     * @param number The number of shorts to load.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if a short falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The <code>shorts</code> array could, therefore, be
     *      partially updated if the raw memory is unmapped or remapped mid-method.
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void getShorts(long offset, short[] shorts, int low, int number) {
    }

    /**
     * Maps the physical memory range into virtual memory. No-op if the system
     * doesn't support virtual memory.
     * <p>
     *  The run time environment is allowed to choose the virtual address where
     *  the raw memory area corresponding to this object will be mapped. 
     * <p>
     *  If the object is already mapped into virtual memory, this method
     *  does not change anything.
     *
     * @return The starting point of the virtual memory range.
     *
     *  @throws OutOfMemoryError Thrown if there is insufficient free virtual address
     *      space to map the object.
     */
    public long map() throws OutOfMemoryError {
        return -1;
    }
    
    /**
     * Maps the physical memory range into virtual memory at the specified
     * location. No-op if the system doesn't support virtual memory.
     * <p>
     *  If the object is already mapped into virtual memory at a different
     *  address, this method remaps it to <code>base</code>.
     * <p>
     *  If a remap is requested while another schedulable object
     *  is accessing the raw memory, the
     *  map will block until one load or store completes.  It can interrupt
     *  an array operation between entries.
     *
     * @param base The location to map at the virtual memory space.
     *
     * @return The starting point of the virtual memory.
     *
     *  @throws OutOfMemoryError Thrown if there is insufficient free virtual
     *      memory at the specified address.
     *
     *  @throws IllegalArgumentException Thrown if <code>base</code> is not a
     *      legal value for a virtual address.
     */
    public long map(long base) {
        return -1;
    }
 
    /**
     * Maps the physical memory range into virtual memory. No-op if the system
     * doesn't support virtual memory.
     * <p>
     *  If the object is already mapped into virtual memory at a different
     *  address, this method remaps it to <code>base</code>.
     * <p>
     *  If a remap is requested while another schedulable object is accessing the raw memory, the
     *  map will block until one load or store completes.  It can interrupt
     *  an array operation between entries.
     *
     * @param base The location to map at the virtual memory space.
     *
     * @param size The size of the block to map in.  If the size of the
     *     raw memory area is greater than <code>size</code>, the object is unchanged
     *      but accesses beyond the mapped region will throw {@link SizeOutOfBoundsException}.
     *      If the size of the raw memory area is smaller than the mapped region access to the
     *      raw memory will behave as if the mapped region matched the raw memory area, but
     *      additional virtual address space will be consumed after the end of the
     *      raw memory area.
     *
     * @return The starting point of the virtual memory.
     *
     *  @throws IllegalArgumentException Thrown if size is not greater than zero, or
     *      <code>base</code> is not a
     *      legal value for a virtual address.
     */
    public long map(long base, long size) {
        return -1;
    }

    /**
     * Sets the <code>byte</code> at the given offset in the memory area
     *  associated with this object.
     *  <p>
     *  This memory access may involve a load and a store, and it may have unspecified
     *  effects on surrounding bytes in the presence of concurrent access.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       to which to write the byte.
     *
     * @param value The byte to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if the byte falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     *
     */
    public void setByte(long offset, byte value) {
    }

    /**
     * Sets <code>number</code> bytes starting at the given offset in the memory area
     *  associated with this object from the
     * byte array passed starting at position <code>low</code>.
     *  <p>
     *  This memory access may involve multiple load and a store operations, and it may have unspecified
     *  effects on surrounding bytes (even bytes in the range being stored)
     *  in the presence of concurrent access.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       to which to start writing.
     *
     * @param bytes The array from which the items are obtained.
     *
     * @param low The offset which is the starting point in the given array for the
     *            items to be obtained.
     *
     * @param number The number of items to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the a short falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The store of the array into memory could, therefore, be
     *      only partially  complete if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setBytes(long offset, byte[] bytes, int low, int number) {
    }

    /**
     * Sets the <code>int</code> at the given offset in the memory area
     *  associated with this object.
     *  On most processor architectures an aligned integer can be stored in an atomic operation, but
     *  this is not required.
     *  <p>
     *  This memory access may involve multiple load and a store operations, and it may have unspecified
     *  effects on surrounding bytes (even bytes in the range being stored)
     *  in the presence of concurrent access.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to write the integer.
     *
     * @param value The integer to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if the integer falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setInt(long offset, int value) {
    }

    /**
     * Sets <code>number</code> ints starting at the given offset in the memory area
     *  associated with this object from the
     * int array passed starting at position <code>low</code>.
     *  On most processor architectures each aligned integer can be stored in an atomic operation, but
     *  this is not required.
     *  <p>
     *  This memory access may involve multiple load and a store operations, and it may have unspecified
     *  effects on surrounding bytes (even bytes in the range being stored)
     *  in the presence of concurrent access.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to start writing.
     *
     * @param ints The array from which the items are obtained.
     *
     * @param low The offset which is the starting point in the given array for the
     *            items to be obtained.
     *
     * @param number The number of items to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if  an int falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The store of the array into memory could, therefore, be
     *      only partially  complete if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setInts(long offset, int[] ints, int low, int number) {
    }

    /**
     * Sets the <code>long</code> at the given offset in the memory area
     *  associated with this object.
     *  Even if it is aligned, the long value may not be updated atomically.  It is unspecified how many
     *  load and store operations will be used or in what order.
     *  <p>
     *  This memory access may involve multiple load and a store operations, and it may have unspecified
     *  effects on surrounding bytes (even bytes in the range being stored)
     *  in the presence of concurrent access.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to write the long.
     *
     * @param value The long to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the long falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setLong(long offset, long value) {
    }

    /**
     * Sets <code>number</code> longs starting at the given offset in the memory area
     *  associated with this object from the
     * long array passed starting at position <code>low</code>.
     *  Even if they are aligned, the long values may not be updated atomically.  It is unspecified how many
     *  load and store operations will be used or in what order.
     *  <p>
     *  This memory access may involve multiple load and a store operations, and it may have unspecified
     *  effects on surrounding bytes (even bytes in the range being stored)
     *  in the presence of concurrent access.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to start writing.
     *
     * @param longs The array from which the items are obtained.
     *
     * @param low The offset which is the starting point in the given array for the
     *            items to be obtained.
     *
     * @param number The number of items to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the a short falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The store of the array into memory could, therefore, be
     *      only partially  complete if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setLongs(long offset, long[] longs, int low, int number) {
    }

    /**
     * Sets the <code>short</code> at the given offset in the memory area
     *  associated with this object.
     * <p>
     *  This memory access may involve a load and a store, and it may have unspecified
     *  effects on surrounding shorts in the presence of concurrent access.
     *  <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to write the short.
     *
     * @param value The short to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the short falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setShort(long offset, short value) {
    }

    /**
     * Sets <code>number</code> shorts starting at the given offset in the memory area
     *  associated with this object from the
     * short array passed starting at position <code>low</code>.
     * <p>
     *  Each write of a short value may involve a load and a store, and it may have unspecified
     *  effects on surrounding shorts in the presence of concurrent access - even on other shorts
     *  in the array.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to start writing.
     *
     * @param shorts The array from which the items are obtained.
     *
     * @param low The offset which is the starting point in the given array for the
     *            items to be obtained.
     *
     * @param number The number of items to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the a short falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The store of the array into memory could, therefore, be
     *      only partially  complete if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setShorts(long offset, short[] shorts, int low, int number) {
    }
    
    /*---------------------------------- From RawMemoryFloatAccess ---------------------------*/

    /**
     * Gets the <code>double</code> at the given offset in the memory area
     *  associated with this object.
     * <p>
     *  The load is not required to be atomic even it is located on a natural boundary.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to load the long.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is invalid.
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the double falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     *
     *
     * @return The double from raw memory.
     */
    public double getDouble(long offset) {
        return -1;
    }

    /**
     * Gets <code>number</code> doubles starting at the given offset in the memory area
     *  associated with this object and assign
     * them to the double array passed starting at position <code>low</code>.
     * <p>
     *  The loads are not required to be atomic even if they are located on natural boundaries.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      at which to start loading.
     *
     * @param doubles The array into which the loaded items are placed.
     *
     * @param low The offset which is the starting point in the given array for the
     *            loaded items to be placed.
     *
     * @param number The number of doubles to load.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the <code>SizeOutOfBoundsException</code> somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if a double falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The <code>doubles</code> array could, therefore, be
     *      partially updated if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void getDoubles(long offset, double[] doubles, int low, int number) {
    }

    /**
     * Gets the <code>float</code> at the given offset in the memory area
     *  associated with this object.  If the float is aligned on a "natural"
     *  boundary it is always loaded from memory
     *  in a single atomic operation.  If it is not on a natural boundary it may not be loaded atomically, and
     *  the number and order of the load operations is unspecified.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to load the float.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the <code>SizeOutOfBoundsException</code> somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the float falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     *
     *
     * @return The float from raw memory.
     */
    public float getFloat(long offset) {
        return -1;
    }

    /**
     * Gets <code>number</code> floats starting at the given offset in the memory area
     *  associated with this object and assign
     * them to the int array passed starting at position <code>low</code>.
     * <p>
     *  If the floats are aligned on natural boundaries
     *  each float is loaded from memory in a single atomic operation.  Groups of floats
     *  may be loaded together, but this is unspecified.
     *<p>
     *  If the floats are not aligned on natural boundaries they may not be loaded atomically and
     *  the number and order of load operations is unspecified.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      at which to start loading.
     *
     * @param floats The array into which the floats loaded from the raw memory are placed.
     *
     * @param low The offset which is the starting point in the given array for the
     *            loaded items to be placed.
     *
     * @param number The number of floats to loaded.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the <code>SizeOutOfBoundsException</code> somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if a float falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The <code>floats</code> array could, therefore, be
     *      partially updated if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void getFloats(long offset, float[] floats, int low, int number) {
    }

    /**
     * Sets the <code>double</code> at the given offset in the memory area
     *  associated with this object.
     *  Even if it is aligned, the double value may not be updated atomically.  It is unspecified how many
     *  load and store operations will be used or in what order.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to write the double.
     *
     * @param value The double to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the <code>SizeOutOfBoundsException</code> somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the double falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setDouble(long offset, double value) {
    }

    /**
     * Sets <code>number</code> doubles starting at the given offset in the memory area
     *  associated with this object from the
     * double array passed starting at position <code>low</code>.
     *  Even if they are aligned, the double values may not be updated atomically.  It is unspecified how many
     *  load and store operations will be used or in what order.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to start writing.
     *
     * @param doubles The array from which the items are obtained.
     *
     * @param low The offset which is the starting point in the given array for the
     *            items to be obtained.
     *
     * @param number The number of items to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the <code>SizeOutOfBoundsException</code> somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the a short falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The <code>doubles</code> array could, therefore, be
     *      partially updated if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setDoubles(long offset, double[] doubles, int low, int number) {
    }

    /**
     * Sets the <code>float</code> at the given offset in the memory area
     *  associated with this object.
     *  On most processor architectures an aligned float can be stored in an atomic operation, but
     *  this is not required.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to write the integer.
     *
     * @param value The float to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the <code>SizeOutOfBoundsException</code> somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException Thrown if the object is not mapped,
     *      or if the float falls in an invalid address range.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setFloat(long offset, float value) {
    }

    /**
     * Sets <code>number</code> floats starting at the given offset in the memory area
     *  associated with this object from the
     * float array passed starting at position <code>low</code>.
     *  On most processor architectures each aligned float can be stored in an atomic operation, but
     *  this is not required.
     * <p>
     *  Caching of the memory access is controlled by the memory <code>type</code> requested
     *  when the <code>RawMemoryAccess</code> instance was created.  If the memory is not cached,
     *  this method guarantees serialized access (that is, the memory access at the memory
     *  occurs in the same order as in the program.  Multiple writes to the same location
     *  may not be coalesced.)
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *       at which to start writing.
     *
     * @param floats The array from which the items are obtained.
     *
     * @param low The offset which is the starting point in the given array for the
     *            items to be obtained.
     *
     * @param number The number of floats to write.
     *
     * @throws OffsetOutOfBoundsException Thrown if the offset is negative or greater than the size of the
     *      raw memory area.  The role of the <code>SizeOutOfBoundsException</code> somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area. (See {@link RawMemoryAccess#map(long base, long size)}).
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the float falls in an invalid address range.  This is checked at every
     *      entry in the array to allow for the possibility that the memory area
     *      could be unmapped or remapped.  The store of the array into memory could, therefore, be
     *      only partially  complete if the raw memory is unmapped or remapped mid-method.
     *
     *
     * @throws ArrayIndexOutOfBoundsException Thrown if <code>low</code> is less than 0 or greater
     * 		than <code>bytes.length - 1</code>, or if <code>low + number</code> is greater than or
     * 		equal to <code>bytes.length</code>.
     *
     * @throws java.lang.SecurityException Thrown if this access is
     * not permitted by the security manager.
     */
    public void setFloats(long offset, float[] floats, int low, int number) {
    }
    
    
    /*------------------------ From Pointer ---------------*/
     
    /**
     * Create a new pointer that is a subset of this pointer, starting from offset
     * @param offset offset from this pointer to start the new pointer
     * @return new pointer
     */
    public Pointer share(long offset) {
                return null;
    }
    
    /**
     * Create a new pointer that is a subset of this pointer, starting from offset
     * @param offset offset from this pointer to start the new pointer
     * @param size size of the new pointer
     * @return new pointer
     */
    public Pointer share(long offset, long size) {
                return null;
    }
    
    boolean isPowerOf2(int value) {
        return false;
    }
    
    /**
     * Create a new pointer that is a subset of this pointer, but is aligned to byteAlignment
     *
     * @param byteAlignment 
     * @return new pointer
     * @throws IllegalArgumentException if byteAlignment is not a power of 2
     */
    public Pointer align(int byteAlignment) { 
        return null;
    }
        
        
    /** 
     * Craete a ptr that's a based on an offset to some other pointer.
     * 
     * @param base pointer
     * @param offset from pointer
     * @param size of the subset of the base pointer
     */
    Pointer(Pointer base, int offset, int size) {
    }
    
    /**
     * Gets the size of the memory
     * @return the size of the native memory area
     */
    public final int getSize() {
        return -1;
    }
    
    /**
     * Zero the first "size" bytes of memory
     * @param size number of bytes
     */
    public void clear(int size) {
    }
    
    /**
     * Zero memory
     */
    public void clear() {
    }

    /**
     * Read a ptr value from memory at offset, and construct a new pointer representing the data stored there...
     * 
     * @param offset offset from <code>this</code> pointer that conatins a memory location that is an address.
     * @param size the size that the new pointer should have
     * @return a new pointer with e
     */
    public Pointer getPointer(int offset, int size) {
        return null;
    }
    
    /**
     * Set the word at <code>offset</code> from <code>this</code> pointer to the the address contained in 
     *  <code>ptr</code>.
     * @param offset offset in bytes from this pointer's base to the word to be set.
     * @param ptr the value that will be set
     */
    public void setPointer(int offset, Pointer ptr) {
    }
    
    /**
     * Create a Java string from a C string pointer to by this pointer at offset
     * @param offset the byte offset of the c string from the base of this pointer
     * @return a java string
     */
    public String getString(int offset) {
        return null;
    }

    /**
     * Copy string value to the location at <code>offset</code>. Convert the data in <code>value</code> to a
     * NULL-terminated C string, converted to native encoding.
     * 
     * @param offset the byte offset of the c string from the base of this pointer
     * @param value the string to copy
     */
    public final void setString(int offset, String value) {
    }
    
    /**
     * Copy bytes in <code>data</code> to the location at <code>offset</code>. Add 
     * NULL-termination. The bytes should already be converted to native encoding.
     * 
     * @param offset the byte offset of the c string from the base of this pointer
     * @param data
     */
    private final void setString(int offset, byte[] data) {
    }
    
    /**
     * Free the backing native memory for this pointer. After freeing the pointer,
     * all accesses to memory through this pointer will throw an exception.
     * 
     * @throws java.lang.IllegalStateException if free has already been called on this pointer.
     */
    public final void free() throws IllegalStateException {
    }
    
    /**
     * @return  false if the memory is the NULL pointer or has been freed.
     */
    public final boolean isValid() {
        return false;
    }

    /**
     * Create a native buffer containing the C-string version of the String <code>vaue</code>.
     * 
     * The returned point can be freed when not needed.
     * 
     * @param value the string to copy
     * @return Pointer the newly allocated memory
     * @throws OutOfMemoryError if the underlying memory cannot be allocated 
     */
    public static Pointer createStringBuffer(String value) throws OutOfMemoryError { return null; }
 
    /**
     * Copy <code>len</code> bytes from <code>src</code> to <code>dst</code> starting at the given offsets.
     * Throws exception if the memory ranges specified for  <code>src</code> dst <code>dst</code> stray outside the 
     * valid ranges for those <code>Pointers</code>.
     * 
     * @param src Pointer to the source bytes
     * @param srcOffset offset in bytes to start copying from
     * @param dst Pointer to the destination bytes
     * @param dstOffset  offset in bytes to start copying to
     * @param len number of bytes to copy
     * 
     * @throws OffsetOutOfBoundsException Thrown if an offset is negative or greater than the size of the
     *      raw memory area.  The role of the {@link SizeOutOfBoundsException} somewhat overlaps
     *      this exception since it is thrown if the offset is within the object but outside the
     *      mapped area.
     *
     * @throws SizeOutOfBoundsException  Thrown if the object is not mapped,
     *      or if the requested memory raange falls in an invalid address range.
     */
    public static void copyBytes(Pointer src, int srcOffset, Pointer dst, int dstOffset, int len){
    }

}
