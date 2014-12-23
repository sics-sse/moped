//if[FLOATS]
/*
 * @(#)RawMemoryFloatAccess.java	1.2 05/10/20
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.squawk.realtime;

import com.sun.squawk.Address;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.Offset;
import com.sun.squawk.VM;

/**
 * This class holds the accessor methods for accessing a raw memory area
 * by float and double types. Implementations are required to implement
 * this class if and only if the underlying Java Virtual Machine supports
 * floating point data types.
 * <p>
 *  By default, the byte addressed by <code>offset</code> is the byte at the
 *  lowest address of the
 *  floating point processor's floating point representation.
 * <p>
 *  <p>
 *  All offset values used in this class are measured in bytes.
 *  <p>
 *  Atomic loads and stores on raw memory are defined in terms of
 *  physical memory.  This memory may be accessible to threads outside the
 *  JVM and to non-programmed access (e.g., DMA), consequently atomic
 *  access must be supported by hardware.  This specification
 *  is written with the assumption that all suitable hardware platforms
 *  support atomic loads for aligned floats.
 *  Atomic access beyond the specified minimum may be supported by the implementation.
 *  <p>
 *  Storing values into raw memory is more hardware-dependent than loading values.
 *  Many processor architectures do not support atomic stores of variables except for
 *  aligned stores of the processor's word size.
 * <p>
 *  This class supports unaligned access to data, but it does
 *  not require the implementation to make such access atomic.  Accesses to
 *  data aligned on its natural boundary will be atomic if the processor
 *  implements atomic loads and stores of that data size.
 *  <p>
 *  Except where noted, accesses to raw memory are not atomic with respect to the memory
 *  or with respect to threads.  A raw memory area could be updated by another thread, or
 *  even unmapped in the middle of a method.
 *  <p>
 *  The characteristics of raw-memory access are necessarily platform dependent.
 *  This specification provides a minimum requirement for the RTSJ platform, but it also
 *  supports a optional system properties that identify a platform's level of support for atomic
 *  raw put and get.  (See {@link RawMemoryAccess}.) The properties represent a four-dimensional sparse array with boolean values
 *  whether that combination of access attributes is atomic.  The default value for array entries is
 *  false.
 *  <p>
 *  Many of the constructors and methods in this class throw
 * {@link OffsetOutOfBoundsException}.  This exception means that the
 * value given in the offset parameter is either negative or outside the
 * memory area.
 *
 * <p> Many of the constructors and methods in this class throw
 * {@link SizeOutOfBoundsException}.  This exception means that the value
 * given in the size parameter is either negative, larger than an allowable
 * range, or would cause an accessor method to access an address outside of
 * the memory area.
 *
 */
public class RawMemoryFloatAccess extends RawMemoryAccess {

    /**
     * Construct an instance of <code>RawMemoryFloatAccess</code> with the given parameters,
     *  and set the object to the mapped state.
     *  If the platform supports virtual memory, map
     *  the raw memory into virtual memory.
     * <p>
     *  The run time environment is allowed to choose the virtual address where
     *  the raw memory area corresponding to this object will be mapped.
     *
     * @param type An instance of <code>Object</code> representing the type of
     *        memory required (e.g., <em>dma, shared</em>) - used to define the base address
     *	      and control the mapping.  If the required memory has more than one
     *      attribute, <code>type</code> may be an array of objects.    If <code>type</code>
     *      is null or a reference to an array with no entries, any type of memory
     *      is acceptable.
     *
     * @param size The size of the area in bytes.
     *
     * @exception SecurityException Thrown if the application doesn't have
     *            permissions to access physical memory, the
     *              specified range of addresses, or the given type of memory.
     *
     * @exception SizeOutOfBoundsException Thrown if the size is negative or
     *            extends into an invalid range of memory.
     *
     * @exception UnsupportedPhysicalMemoryException Thrown if the underlying
     *            hardware does not support the given type.
     *
     * @exception MemoryTypeConflictException Thrown if the specified base does not point to
     *            memory that matches the request type, or if <code>type</code> specifies
     *            incompatible memory attributes.
     *
     * @exception OutOfMemoryError  Thrown if the requested type of memory exists, but there is not
     *      enough of it free to satisfy the request.
     */
    public RawMemoryFloatAccess(Object type, long size) throws SizeOutOfBoundsException, OutOfMemoryError {
        super(type, size);
    }

    /**
     * Construct an instance of <code>RawMemoryFloatAccess</code> with the given parameters,
     *  and set the object to the mapped state.
     *  If the platform supports virtual memory, map
     *  the raw memory into virtual memory.
     * <p>
     *  The run time environment is allowed to choose the virtual address where
     *  the raw memory area corresponding to this object will be mapped.
     *
     * @param type An instance of <code>Object</code> representing the type of
     *        memory required (e.g., <em>dma, shared</em>) - used to define the base address
     *	      and control the mapping.  If the required memory has more than one
     *      attribute, <code>type</code> may be an array of objects.    If <code>type</code>
     *      is null or a reference to an array with no entries, any type of memory
     *      is acceptable.
     *
     * @param base The physical memory address of the region.
     *
     * @param size The size of the area in bytes.
     *
     * @exception SecurityException Thrown if the application doesn't have
     *            permissions to access physical memory, the
     *              specified range of addresses, or the given type of memory.
     *
     * @exception OffsetOutOfBoundsException Thrown if the address is invalid.
     *
     * @exception SizeOutOfBoundsException Thrown if the size is negative or
     *            extends into an invalid range of memory.
     *
     * @exception UnsupportedPhysicalMemoryException Thrown if the underlying
     *            hardware does not support the given type.
     *
     * @exception MemoryTypeConflictException Thrown if the specified base does not point to
     *            memory that matches the request type, or if <code>type</code> specifies
     *            incompatible memory attributes.
     *
     * @exception OutOfMemoryError  Thrown if the requested type of memory exists, but there is not
     *
     */
    public RawMemoryFloatAccess(Object type, long base, long size) {
        super(type, base, size);
    }

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
        return VM.longBitsToDouble(getLong(offset));
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
        int off = (int) offset;
        checkMultiRead(off, number, 8);
        VM.getData(vbase, off, doubles, low, number, 8);
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
        return VM.intBitsToFloat(getInt(offset));
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
        int off = (int) offset;
        checkMultiRead(off, number, 4);
        VM.getData(vbase, off, floats, low, number, 4);
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
        setLong(offset, VM.doubleToLongBits(value));
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
        int off = (int) offset;
        checkMultiWrite(off, number, 8);
        VM.setData(vbase, off, doubles, low, number, 8);
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
        setInt(offset, VM.floatToIntBits(value));
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
        int off = (int) offset;
        checkMultiWrite(off, number, 4);
        VM.setData(vbase, off, floats, low, number, 4);
    }
}
