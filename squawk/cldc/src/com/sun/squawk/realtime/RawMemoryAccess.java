/*
 * @(#)RawMemoryAccess.java	1.5 06/11/24
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.squawk.realtime;

import com.sun.squawk.Address;
import com.sun.squawk.GC;
import com.sun.squawk.NativeUnsafe;

//import com.sun.rtsjx.PhysicalMemoryRequest;
import com.sun.squawk.UWord;
import com.sun.squawk.VM;
import com.sun.squawk.util.Assert;

/**
 * An instance of <code>RawMemoryAccess</code> models a range of physical
 * memory as a fixed sequence of bytes. A full complement of accessor
 * methods allow the contents of the physical area to be accessed through
 * offsets from the base, interpreted as byte, short, int, or long data
 * values or as arrays of these types.
 * <p>
 * The <code>RawMemoryAccess</code> class allows a real-time program to implement device
 * drivers, memory-mapped I/O, flash memory, battery-backed RAM, and similar
 * low-level software.
 * <p>
 * A raw memory area cannot contain references to Java objects.
 * Such a capability would be unsafe (since it could be used to defeat
 * Java's type checking) and error-prone (since it is sensitive to the
 * specific representational choices made by the Java compiler).
 * <p>
 * Many of the constructors and methods
 * in this class throw {@link OffsetOutOfBoundsException}. This
 * exception means that the value given in the offset parameter is either
 * negative or outside the memory area.
 * <p>
 * Many of the constructors and methods
 * in this class throw {@link SizeOutOfBoundsException}. This
 * exception means that the value given in the size parameter is either
 * negative, larger than an allowable range, or would cause an accessor
 * method to access an address outside of the memory area.
 * <p>
 *  Unlike other integral parameters in this chapter, negative values are
 *  valid for
 *  <code>byte, short, int,</code> and <code>long</code> values that are
 *  copied in and out of memory by the <code>set</code> and <code>get</code>
 *  methods of this class.
 *  <p>
 *  All offset values used in this class are measured in bytes.
 *  <p>
 *  Atomic loads and stores on raw memory are defined in terms of
 *  physical memory.  This memory may be accessible to threads outside the
 *  JVM and to non-programmed access (e.g., DMA), consequently atomic
 *  access must be supported by hardware.  This specification
 *  is written with the assumption that all suitable hardware platforms
 *  support atomic loads for aligned bytes, shorts, and ints.
 *  Atomic access beyond the specified minimum may be supported by the implementation.
 *  <p>
 *  Storing values into raw memory is more hardware-dependent than loading values.
 *  Many processor architectures do not support atomic stores of variables except for
 *  aligned stores of the processor's word size.  For instance, storing a byte into memory
 *  might require reading a 32-bit quantity into a processor register, updating the register to
 *  reflect the new byte value, then re-storing the whole 32-bit quantity.  Changes to other bytes
 *  in the 32-bit quantity that take place between the load and the store will be lost.
 * <p>
 *  Some processors have mechanisms that can be used to implement an atomic store of a byte, but
 *  those mechanisms are often slow and not universally supported.
 * <p>
 *  This class supports unaligned access to data, but it does
 *  not require the implementation to make such access atomic.  Accesses to
 *  data aligned on its natural boundary will be atomic if the processor
 *  implements atomic loads and stores of that data size.
 *  <p>
 *  Except where noted, accesses to raw memory are not atomic with respect to the memory
 *  or with respect to schedulable objects.  A raw memory area could be
 *    updated by another schedulable object, or
 *  even unmapped in the middle of a method.
 *  <p>
 *  The characteristics of raw-memory access are necessarily platform dependent.
 *  This specification provides a minimum requirement for the RTSJ platform, but it also
 *  supports optional system properties that identify a platform's level of support for atomic
 *  raw put and get.  The properties represent a four-dimensional sparse array with boolean values
 *  indicating
 *  whether that combination of access attributes is atomic.  The default value for array entries is
 *  false.
 *  The dimension are
 * <table width="95%" border="1">
 * <tr>
 *   <td><div align="center"><strong>Attribute</strong></div></td>
 *   <td><div align="center"><strong>Values</strong></div></td>
 *   <td><div align="center"><strong>Comment</strong></div></td>
 * </tr>
 * <tr>
 *   <td>Access type</td>
 *   <td>read, write</td>
 *   <td>&nbsp;</td>
 * </tr>
 * <tr>
 *   <td>Data type</td>
 *   <td><ul>
 *      <li>byte,
 *      <li>short,
 *      <li>int,
 *      <li>long,
 *      <li>float,
 *      <li>double</ul></td>
 *   <td>&nbsp;</td>
 * </tr>
 * <tr>
 *   <td>Alignment</td>
 *   <td>0 to 7</td>
 *   <td><p>For each data type, the possible alignments range from </p>
 *     <ul>
 *       <li>0 == aligned </li>
 *       <li>to data size - 1 == only the first byte of the data is <em>alignment</em>
 *         bytes away from natural alignment.</li>
 *     </ul></td>
 * </tr>
 * <tr>
 *   <td>Atomicity</td>
 *   <td><ul><li>processor, <li>smp, <li>memory</ul></td>
 *   <td><ul>
 *       <li><em>processor</em> means access is atomic with respect to other schedulable objects
 *         on that processor.</li>
 *       <li><em>smp</em> means that access is <em>processor</em> atomic, and atomic
 *         with respect across the processors in an SMP.</li>
 *       <li><em>memory</em> means that access is <em>smp</em> atomic, and atomic
 *         with respect to all access to the memory including DMA.</li>
 *     </ul></td>
 * </tr>
 * </table>
 *
 *  The true values in the table are represented by properties of the following form.
 *  javax.realtime.atomicaccess_&lt;access>_&lt;type>_&lt;alignment>_atomicity=true
 *  for example:
 * <pre>
 *  javax.realtime.atomicaccess_read_byte_0_memory=true
 * </pre>
 *  Table entries with a value of false may be explicitly represented, but since false
 *  is the default value, such properties are redundant.
 * <p>
 *    All raw memory access is treated as volatile, and <em>serialized</em>.  The run-time must be forced to re-read
 * memory or write to memory on each call to a raw memory getxxx or putxxx method, and to complete the reads and writes in the order they
 * appear in the program order.
 *
 */
public class RawMemoryAccess {

    /**
     * Construct an instance of <code>RawMemoryAccess</code> with the given parameters,
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
     *
     */
    public RawMemoryAccess(Object type, long size)
            throws SizeOutOfBoundsException, OutOfMemoryError {
        int sz = (int)size;
        if (sz < 0) {
            throw new SizeOutOfBoundsException();
        }
        reachable_size = UWord.fromPrimitive(sz);
        vbase = NativeUnsafe.malloc(reachable_size);
        if (vbase.isZero()) {
            throw new OutOfMemoryError("malloc failed in RawMemoryAccess");
        }
        state = MALLOCED;
    }

    /**
     * Construct an instance of <code>RawMemoryAccess</code> with the given parameters,
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
     * @exception SecurityException Thrown if application doesn't have
     *            permissions to access physical memory, the
     *              specified range of addresses, or the given type of memory.
     *
     * @exception OffsetOutOfBoundsException    Thrown if the address is invalid.
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
     *
     */
    public RawMemoryAccess(Object type, long base, long size)
            throws SizeOutOfBoundsException,
                   OffsetOutOfBoundsException {
/*if[!SQUAWK_64]*/
        int bs = (int) base;
        int sz = (int) size;
/*else[SQUAWK_64]*/
//       long bs = base;
//       long sz = size;
/*end[SQUAWK_64]*/
        if (sz < 0) {
            throw new SizeOutOfBoundsException();
        } /*else if (base < 0) {
            throw new OffsetOutOfBoundsException();
        }*/
        state = SHARED;

        reachable_size = UWord.fromPrimitive(sz);
        
        if (type instanceof RawMemoryAccess) {
            RawMemoryAccess parent = (RawMemoryAccess)type;
             if (bs + sz < 0) {
                throw new OffsetOutOfBoundsException();
            }
            if (bs + sz > parent.reachable_size.toPrimitive()) {
                throw new SizeOutOfBoundsException();
            }
            // don't allow a sub-range of a null pointer.
            if (parent.vbase.isZero()) {
                throw new SizeOutOfBoundsException();
            }
            vbase = parent.vbase.add(bs);
        } else {

            vbase = Address.fromPrimitive(bs);
            
            // the only good null is an empty null...
            if (vbase.isZero() && !reachable_size.isZero()) {
                throw new SizeOutOfBoundsException();
            }
            if (GC.isGCEnabled() && GC.inRam(vbase, vbase.add(sz))) {
                throw new SecurityException("invalid memory range - base: " + base + ", size: " + size);
            }
        }
    }

    /**
     * Do a bounds check on a read.
     * <p>
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to read.
     * @param size The size of the read in bytes
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
    final void checkRead(int offset, int size) throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
        Assert.that(size > 0 && size <= 8);
        checkBounds(reachable_size.toPrimitive(), offset, size);
        if (vbase.isZero()) {
            throw new SizeOutOfBoundsException();
        }
//        if (!request.get_readable()) {
//            throw new SecurityException();
//        }
    }

    /**
     * Do a bounds check on reading a range.
     * <p>
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to read.
     * @param number The number of items to load.
     * @param elemsize The size of the read in bytes
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
    protected final void checkMultiRead(int offset, int number, int elemsize) throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
        checkMultiBounds(reachable_size.toInt(), offset, number, elemsize);
         if (vbase.isZero()) {
            throw new SizeOutOfBoundsException();
        }
    }
   
    /**
     * Do a bounds check on accessing a range.
     * <p>
     *
     * @param length the length pof the memory area being accessed, in bytes
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to read.
     * @param number The number of items to load.
     * @param elemsize The size of the each item in bytes
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
    protected static void checkMultiBounds(int length, int offset, int number, int elemsize) throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        int bsize = number * elemsize;
        if (bsize < 0) {
            throw new OffsetOutOfBoundsException();
        }
        
        if (offset < 0 ||
                offset > (length - bsize)) {
            throw new OffsetOutOfBoundsException();
        }
    }

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
        int off = (int)offset;
        checkRead(off, 1);
        return (byte) NativeUnsafe.getByte(vbase, off);
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
        int off = (int)offset;
        checkMultiRead(off, number, 1);
        VM.getData(vbase, off, bytes, low, number, 1);
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
        int off = (int)offset;
        checkRead(off, 4);
        return NativeUnsafe.getUnalignedInt(vbase, off);
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
        int off = (int)offset;
        checkMultiRead(off, number, 4);
        VM.getData(vbase, off, ints, low, number, 4);
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
        int off = (int)offset;
        checkRead(off, 8);
        return NativeUnsafe.getUnalignedLong(vbase, off);
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
        int off = (int)offset;
        checkMultiRead(off, number, 8);
        VM.getData(vbase, off, longs, low, number, 4);
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
        return vbase.toUWord().toPrimitive();
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
    protected final Address getAddress() {
        return vbase;
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
    protected final UWord size() {
        return reachable_size;
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
        int off = (int)offset;
        checkRead(off, 2);
        return (short) NativeUnsafe.getUnalignedShort(vbase, off);
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
        int off = (int)offset;
        checkMultiRead(off, number, 2);
        VM.getData(vbase, off, shorts, low, number, 2);
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
     * Do a bounds check on a write.
     * <p>
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to write.
     * @param size The size of the write in bytes
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
    protected static void checkBounds(int length, int offset, int size) throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
        Assert.that(size > 0 && size <= 8);
        if (offset < 0 ||
                offset > (length - size)) {
            throw new OffsetOutOfBoundsException();
        }
    }
    
    /**
     * Do a bounds check on a write.
     * <p>
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to write.
     * @param size The size of the write in bytes
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
    final void checkWrite(int offset, int size) throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
        Assert.that(size > 0 && size <= 8);
        checkBounds(reachable_size.toPrimitive(), offset, size);
        if (vbase.isZero()) {
            throw new SizeOutOfBoundsException();
        }
//        if (!request.get_writable()) {
//            throw new SecurityException();
//        }
    }
    
    /**
     * Do a bounds check on writing a range.
     * <p>
     *
     * @param offset The offset in bytes from the beginning of the raw memory area
     *      from which to read
     * @param number The number of items to store.
     * @param elemsize The size of each element in bytes
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
    protected final void checkMultiWrite(int offset, int number, int elemsize) throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
        checkMultiBounds(reachable_size.toInt(), offset, number, elemsize);
        if (vbase.isZero()) {
            throw new SizeOutOfBoundsException();
        }
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
        int off = (int)offset;
        checkWrite(off, 1);
        NativeUnsafe.setByte(vbase, off, value);
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
        int off = (int)offset;
        checkMultiWrite(off, number, 1);
        VM.setData(vbase, off, bytes, low, number, 1);
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
        int off = (int)offset;
        checkWrite(off, 4);
        NativeUnsafe.setUnalignedInt(vbase, off, value);
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
        int off = (int)offset;
        checkMultiWrite(off, number, 4);
        VM.setData(vbase, off, ints, low, number, 4);
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
        int off = (int)offset;
        checkWrite(off, 8);
        NativeUnsafe.setUnalignedLong(vbase, off, value);

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
        int off = (int)offset;
        checkMultiWrite(off, number, 8);
        VM.setData(vbase, off, longs, low, number, 8);
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
        int off = (int)offset;
        checkWrite(off, 2);
        NativeUnsafe.setUnalignedShort(vbase, off, value);
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
        int off = (int)offset;
        checkMultiWrite(off, number, 2);
        VM.setData(vbase, off, shorts, low, number, 2);
    }

    /**
     * Return true if this specific RawMemoryAccess allocated native memory.
     * @return true if the memory referred to by this RawMemoryAccess was allocated by this RawMemoryAccess
     */
    protected boolean wasMalloced() {
        return state == MALLOCED;
    }
    
    /**
     * Set the pointer value to NULL
     */
    protected final void invalidate() {
        vbase = Address.zero();
    }
    
    /**
     * Virtual base address.
     * <p>It is null if the RawMemoryAccess object is not mapped.
     */
    protected Address vbase;
    
    /*
    The size that can be accessed with this RawMemoryAccess object is
    not necessarily the size that is mapped. The spec says: "If the
    size of the raw memory area is greater than size, the object is
    unchanged but accesses beyond the mapped region will throw
    SizeOutOfBoundsException." As I understand it, it means that if it
    is requested that an RawMemoryAcess object be remapped and if the
    new size of the mapping is less than the old size, no remapping is
    performed, but access is restricted to the new size.
    - an access to [base, base+new size[ is valid
    - an access to [base+new size, base+old size[ triggers SizeOutOfBoundsException
    - an access beyon base+old size triggers OffsetOutOfBoundsException
    So we need to keep track of what part of the RawMemoryAccess is
    really reachable.
     */
    private final UWord reachable_size;
    
    /* @TODO: switch over to subclasses */ 
    private final static int SHARED = 0;
    private final static int MALLOCED = 1;
    private final static int STACK = 2;

    
    /** set MALLOCED when this specific RawMemoryAccess allocated native memory. */
    final private byte state;
}
