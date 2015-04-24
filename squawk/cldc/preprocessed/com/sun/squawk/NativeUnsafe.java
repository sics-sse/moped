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

import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.squawk.pragma.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;


/**
 * A collection of methods for performing peek and poke operations on
 * memory addresses.
 * <p>
 * Only the public methods of this class which do not override any of the
 * methods in java.lang.Object will be available in a {@link VM#isHosted() non-hosted}
 * environment. The translator replaces any calls to these methods to native
 * method calls.
 *
 */
public final class NativeUnsafe {

    private NativeUnsafe() {}

    /*-----------------------------------------------------------------------*\
     *                      Storing to/loading from memory                   *
    \*-----------------------------------------------------------------------*/

    /**
     * @see Unsafe#setByte
     */

    @Vm2c(proxy="")

     public static void setByte(Object base, int offset, int value) throws NativePragma {
    	int index = ((Address)base).add(offset).asIndex();
        checkAddress(index);
        memory[index] = (byte)(value>>0);
        setType0(index, AddressType.BYTE);
     }
    
     /**
      * @see Unsafe#setShort
      */

    @Vm2c(proxy="")

    public static void setShort(Object base, int offset, int value) throws NativePragma {
        setChar(base, offset, value);
    }

    /**
     * @see Unsafe#setChar
     */

    @Vm2c(proxy="")

    public static void setChar(Object base, int offset, int value) throws NativePragma {
        int index = ((Address)base).add(offset * 2).asIndex();
        checkAddress(index + 1);
        if (VM.isBigEndian()) {
            memory[index+0] = (byte)(value>>8);
            memory[index+1] = (byte)(value>>0);
        } else {
            memory[index+0] = (byte)(value>>0);
            memory[index+1] = (byte)(value>>8);
        }
        setType0(index, AddressType.SHORT);
    }

    /**
     * @see Unsafe#setInt
     */

    @Vm2c(proxy="")

    public static void setInt(Object base, int offset, int value) throws NativePragma {
        int index = ((Address)base).add(offset * 4).asIndex();
        checkAddress(index + 3);
        if (VM.isBigEndian()) {
            memory[index + 0] = (byte) (value >> 24);
            memory[index + 1] = (byte) (value >> 16);
            memory[index + 2] = (byte) (value >> 8);
            memory[index + 3] = (byte) (value >> 0);
        }
        else {
            memory[index + 0] = (byte) (value >> 0);
            memory[index + 1] = (byte) (value >> 8);
            memory[index + 2] = (byte) (value >> 16);
            memory[index + 3] = (byte) (value >> 24);
        }
        setType0(index, AddressType.INT);
    }

    /**
     * @see Unsafe#setUWord
     */

    @Vm2c(proxy="")

    public static void setUWord(Object base, int offset, UWord value) throws NativePragma {
        setInt/*S64*/(base, offset, value.toPrimitive());
        int index = ((Address)base).add(offset * HDR.BYTES_PER_WORD).asIndex();
        setType0(index, AddressType.UWORD);
    }

    /**
     * @see Unsafe#setLong
     */

    @Vm2c(proxy="")

    public static void setLong(Object base, int offset, long value) throws NativePragma {
        int index = ((Address)base).add(offset * 8).asIndex();
        checkAddress(index + 7);
        if (VM.isBigEndian()) {
            memory[index+0] = (byte)(value>>56);
            memory[index+1] = (byte)(value>>48);
            memory[index+2] = (byte)(value>>40);
            memory[index+3] = (byte)(value>>32);
            memory[index+4] = (byte)(value>>24);
            memory[index+5] = (byte)(value>>16);
            memory[index+6] = (byte)(value>>8);
            memory[index+7] = (byte)(value>>0);
        } else {
            memory[index+0] = (byte)(value>>0);
            memory[index+1] = (byte)(value>>8);
            memory[index+2] = (byte)(value>>16);
            memory[index+3] = (byte)(value>>24);
            memory[index+4] = (byte)(value>>32);
            memory[index+5] = (byte)(value>>40);
            memory[index+6] = (byte)(value>>48);
            memory[index+7] = (byte)(value>>56);
        }
        setType0(index, AddressType.LONG);
    }

    /**
     * @see Unsafe#setLongAtWord
     */

    @Vm2c(proxy="")

    public static void setLongAtWord(Object base, int offset, long value) throws NativePragma {
        Address ea = ((Address)base).add(offset * HDR.BYTES_PER_WORD);
        setLong(ea, 0, value);
        setType0(ea.asIndex(), AddressType.LONG);
    }

    /**
     * @see Unsafe#setAddress
     */

    @Vm2c(proxy="setObject")

    public static void setAddress(Object base, int offset, Object value) throws NativePragma {
        Address ea = ((Address)base).add(offset * HDR.BYTES_PER_WORD);
        if (value instanceof Klass) {
            unresolvedClassPointers.put(ea, value);
            setUWord(ea, 0, UWord.zero());
        } else {
            if (false) Assert.that(value instanceof Address);
            unresolvedClassPointers.remove(ea);
            if (value == null) {
                setUWord(ea, 0, UWord.zero());
            } else {
                setUWord(ea, 0, ((Address)value).toUWord());
            }
        }
        oopMap.set(ea.asIndex() / HDR.BYTES_PER_WORD);
        setType0(ea.asIndex(), AddressType.REF);
    }

    /**
     * @see Unsafe#setObject
     */

    @Vm2c(proxy="setObjectAndUpdateWriteBarrier")

    public static void setObject(Object base, int offset, Object value) throws NativePragma {
        setAddress(base, offset, value);
    }


    private static void setType0(int index, byte type) throws HostedPragma {

//        typeMap[index] = type;

    }

    /**
     * Sets the type of a value at a given address.
     *
     * This operation is a nop when {@link VM#usingTypeMap()} returns false.
     *
     * @param ea   the address of the value
     * @param type the type of the value
     * @param size the size (in bytes) of the value
     */
    public static void setType(Address ea, byte type, int size) throws NativePragma {

//        setType0(ea.asIndex(), type);

    }

    /**
     * Sets the type of each value in an array.
     *
     * This operation is a nop when {@link VM#usingTypeMap()} returns false.
     *
     * @param ea            the address of an array
     * @param componentType the component type of the array
     * @param componentSize the size (in bytes) of <code>componentType</code>
     * @param length        the length of the array
     */
    public static void setArrayTypes(Address ea, byte componentType, int componentSize, int length) throws NativePragma {

//        for (int i = 0; i != length; ++i) {
//            setType0(ea.asIndex(), componentType);
//            ea = ea.add(componentSize);
//        }

    }

    /**
     * Gets the type of a value at a given address.
     *
     * This operation is a nop when {@link VM#usingTypeMap()} returns false.
     *
     * @param ea   the address to query
     * @return the type of the value at <code>ea</code>
     */

    @Vm2c(proxy="")

    public static byte getType(Address ea) throws NativePragma {

//        return typeMap[ea.asIndex()];

        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere();

    }

    /**
     * Block copies the types recorded for a range of memory to another range of memory.
     *
     * @param src    the start address of the source range
     * @param dst    the start address of the destination range
     * @param length the length (in bytes) of the range
     */

    @Vm2c(proxy="")

    public static void copyTypes(Address src, Address dst, int length) throws NativePragma {

//        System.arraycopy(typeMap, src.asIndex(), typeMap, dst.asIndex(), length);

    }

    /**
     * @see Unsafe#getByte
     */

    @Vm2c(proxy="")

    public static int getByte(Object base, int offset) throws NativePragma {
        int index = ((Address)base).add(offset).asIndex();
        checkAddress(index);
        return memory[index];
    }
    
    /**
     * @see Unsafe#getUByte
     *
     * @todo Could add real native for better performance
     */

    @Vm2c(proxy="")

    public static int getUByte(Object base, int offset) {
        return getByte(base, offset) & 0xFF;
    }

    /**
     * @see Unsafe#getShort
     */

    @Vm2c(proxy="")

    public static int getShort(Object base, int offset) throws NativePragma {
        return (short)getChar(base, offset);
    }

    /**
     * @see Unsafe#getChar
     */

    @Vm2c(proxy="getUShort")

    public static int getChar(Object base, int offset) throws NativePragma {
        int index = ((Address)base).add(offset * 2).asIndex();
        checkAddress(index + 1);
        int b0 = memory[index] & 0xFF;
        int b1 = memory[index + 1] & 0xFF;
        if (VM.isBigEndian()) {
            return b0 << 8 | b1;
        } else {
            return b1 << 8 | b0;
        }
    }


    /**
     * @see Unsafe#getInt
     */

    @Vm2c(proxy="")

    public static int getInt(Object base, int offset) throws NativePragma {
        int index = ((Address)base).add(offset * 4).asIndex();
        checkAddress(index + 3);
        int b0 = memory[index + 0] & 0xFF;
        int b1 = memory[index + 1] & 0xFF;
        int b2 = memory[index + 2] & 0xFF;
        int b3 = memory[index + 3] & 0xFF;
        if (VM.isBigEndian()) {
            return (b0<<24) | (b1<<16) | (b2<<8) | b3;
        } else {
            return (b3<<24) | (b2<<16) | (b1<<8) | b0;
        }
    }

    /**
     * @see Unsafe#getUWord
     */

    @Vm2c(proxy="")

    public static UWord getUWord(Object base, int offset) throws NativePragma {
        return UWord.fromPrimitive(getInt/*S64*/(base, offset));
    }

    /**
     * @see Unsafe#getLong
     */

    @Vm2c(proxy="")

    public static long getLong(Object base, int offset) throws NativePragma {
        int index = ((Address)base).add(offset * 8).asIndex();
        checkAddress(index + 7);
        long b0 = memory[index + 0] & 0xFF;
        long b1 = memory[index + 1] & 0xFF;
        long b2 = memory[index + 2] & 0xFF;
        long b3 = memory[index + 3] & 0xFF;
        long b4 = memory[index + 4] & 0xFF;
        long b5 = memory[index + 5] & 0xFF;
        long b6 = memory[index + 6] & 0xFF;
        long b7 = memory[index + 7] & 0xFF;
        if (VM.isBigEndian()) {
            return (b0<<56) | (b1<<48) | (b2<<40) | (b3<<32) | (b4<<24) | (b5<<16) | (b6<<8) | b7;
        } else {
            return (b7<<56) | (b6<<48) | (b5<<40) | (b4<<32) | (b3<<24) | (b2<<16) | (b1<<8) | b0;
        }
    }

    /**
     * @see Unsafe#getLongAtWord
     */

    @Vm2c(proxy="")

    public static long getLongAtWord(Object base, int offset) throws NativePragma {
        return getLong(((Address)base).add(offset * HDR.BYTES_PER_WORD), 0);
    }

    /**
     * @see Unsafe#getObject
     */

    @Vm2c(proxy="")

    public static Object getObject(Object base, int offset) throws NativePragma {
        return Address.get(getUWord(base, offset).toPrimitive());
    }

    /**
     * @see Unsafe#getAddress
     */

    @Vm2c(proxy="getObject")

    public static Address getAddress(Object base, int offset) throws NativePragma {
        return Address.fromObject(getObject(base, offset));
    }

    /**
     * Gets a UWord value from memory ignoring any recorded type of the value at the designated location.
     * This operation is equivalent to {@link #getUWord(Object, int)} when {@link VM#usingTypeMap() runtime type checking}
     * is disabled.
     *
     * @param base   the base address
     * @param offset the offset (in words) from <code>base</code> from which to load
     * @return the value
     */

    @Vm2c(code="return getUWordTyped(base, offset, AddressType_ANY);")

    public static UWord getAsUWord(Object base, int offset) throws NativePragma {
        return getUWord(base, offset);
    }

    /**
     * Gets a signed 8 bit value from memory ignoring any recorded type of the value at the designated location.
     * This operation is equivalent to {@link #getByte(Object, int)} when {@link VM#usingTypeMap() runtime type checking}
     * is disabled.
     *
     * @param base   the base address
     * @param offset the offset (in 8 bit words) from <code>base</code> from which to load
     * @return the value
     */

    @Vm2c(code="return getByteTyped(base, offset, AddressType_ANY);")

    public static int getAsByte(Object base, int offset) throws NativePragma {
        return getByte(base, offset);
    }

     /**
     * Gets a signed 16 bit value from memory ignoring any recorded type of the value at the designated location.
     * This operation is equivalent to {@link #getShort(Object, int)} when {@link VM#usingTypeMap() runtime type checking}
     * is disabled.
     *
     * @param base   the base address
     * @param offset the offset (in 16 bit words) from <code>base</code> from which to load
     * @return the value
     */

    @Vm2c(code="return getShortTyped(base, offset, AddressType_ANY);")

    public static int getAsShort(Object base, int offset) throws NativePragma {
        return getShort(base, offset);
    }

    /**
     * Gets a signed 32 bit value from memory ignoring any recorded type of the value at the designated location.
     * This operation is equivalent to {@link #getInt(Object, int)} when {@link VM#usingTypeMap() runtime type checking}
     * is disabled.
     *
     * @param base   the base address
     * @param offset the offset (in 32 bit words) from <code>base</code> from which to load
     * @return the value
     */

    @Vm2c(code="return getIntTyped(base, offset, AddressType_ANY);")

    public static int getAsInt(Object base, int offset) throws NativePragma {
        return getInt(base, offset);
    }

    /**
     * Gets character from a string.
     *
     * @param str   the string
     * @param index the index to the character
     * @return the value
     */

    @Vm2c(code="Address cls = com_sun_squawk_Klass_self(getObject(str, HDR_klass)); if (com_sun_squawk_Klass_id(cls) == com_sun_squawk_StringOfBytes) { return getUByte(str, index); } else { return getUShort(str, index); }")

    public static char charAt(String str, int index) throws NativePragma {
        return str.charAt(index);
    }

    /*-----------------------------------------------------------------------*\
     *                        Function Ptr Support                            *
    \*-----------------------------------------------------------------------*/
    
    /**
     * Call a function pointer with no arguments
     * 
     * @vm2c code( funcPtr0 f0 = (funcPtr0)address; return (*f0)(); )
     */
    public static int call0(Address fptr) throws NativePragma {
         throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }
    
    /**
     * Call a function pointer with one arguments
     * 
     * @vm2c code( funcPtr1 f1 = (funcPtr1)address; return (*f1)(i1)); )
     */
    public static int call1(Address fptr, int i1) throws NativePragma {
         throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }
    
    /**
     * Call a function pointer with two arguments
     * 
     * @vm2c code( funcPtr2 f2 = (funcPtr2)address; return (*f2)(i1, i2)); )
     */
    public static int call2(Address fptr, int i1, int i2) throws NativePragma {
         throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }
    
    /**
     * Call a function pointer with three arguments
     * 
     * @vm2c code( funcPtr3 f3 = (funcPtr3)address; return (*f3)(i1, i2, i3)); )
     */
    public static int call3(Address fptr, int i1, int i2, int i3) throws NativePragma {
         throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }
    
    /**
     * Call a function pointer with four arguments
     * 
     * @vm2c  code( funcPtr4 f4 = (funcPtr4)address; return (*f4)(i1, i2, i3, i4)); )
     */
    public static int call4(Address fptr, int i1, int i2, int i3, int i4) throws NativePragma {
         throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }
    
    /**
     * Call a function pointer with five arguments
     * 
     * @vm2c code( funcPtr5 f5 = (funcPtr5)address; return (*f5)(i1, i2, i3, i4, i5)); )
     */
    public static int call5(Address fptr, int i1, int i2, int i3, int i4, int i5) throws NativePragma {
         throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }

    /**
     * Call a function pointer with six arguments
     *
     * @vm2c code( funcPtr6 f6 = (funcPtr6)address; return (*f6)(i1, i2, i3, i4, i5, i6)); )
     */
    public static int call6(Address fptr, int i1, int i2, int i3, int i4, int i5, int i6)
            throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }

    /**
     * Call a function pointer with 10 arguments
     *
     * @vm2c code( funcPtr10 f10 = (funcPtr10)address; return (*f10)(i1, i2, i3, i4, i5, i6, i7, i8, i9, i10)); )
     */
    public static int call10(Address fptr, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i19)
            throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }

    /*-----------------------------------------------------------------------*\
     *                      Blocking C function support                     *
    \*-----------------------------------------------------------------------*/

    public final static int NATIVE_TASK_EVENTID_OFFSET =

//            (1 * 2);

            (1 * 1);


    public final static int NATIVE_TASK_RESULT_OFFSET = NATIVE_TASK_EVENTID_OFFSET + 3;
    public final static int NATIVE_TASK_LOW_RESULT_OFFSET = NATIVE_TASK_RESULT_OFFSET + 1;
    public final static int NATIVE_TASK_NT_ERRNO_RESULT_OFFSET = NATIVE_TASK_LOW_RESULT_OFFSET + 1;
    public final static int NATIVE_TASK_ARGS_OFFSET = NATIVE_TASK_NT_ERRNO_RESULT_OFFSET + 1;

    /*  ----- Natives: define unconditionally to avoid renumbering native methods ------------*/
    public static int cancelTaskExecutor(Address taskExecutor) throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }

    public static int deleteTaskExecutor(Address taskExecutor) throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }

    public static Address runBlockingFunctionOn(Address taskExecutor, Address fptr,
            int arg1, int arg2, int arg3, int arg4, int arg5,
            int arg6, int arg7, int arg8, int arg9, int arg10) throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }

    public static int deleteNativeTask(Address ntask) throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }


//
//    public static int getNativeTaskEventID(Address ntask) {
//        return getAsInt(ntask, NATIVE_TASK_EVENTID_OFFSET);
//    }
//
//    public static int getNativeTaskResult(Address ntask) {
//        return getAsInt(ntask, NATIVE_TASK_RESULT_OFFSET);
//    }
//
//    public static int getNativeTaskErrno(Address ntask) {
//        return getAsInt(ntask, NATIVE_TASK_NT_ERRNO_RESULT_OFFSET);
//    }
//
//    public static Address createTaskExecutor(Address name, int priority, int stacksize) throws NativePragma {
//        throw Assert.shouldNotReachHere("unimplemented when hosted");
//    }
//
//    public static int waitForBlockingFunction(Address ntask) {
////VM.println("waitForBlockingFunction()...");
//        int evntid = getNativeTaskEventID(ntask);
//        VMThread.waitForEvent(evntid);
////VM.println("done waitForBlockingFunction()");
//        int result = getNativeTaskResult(ntask);
//        VMThread.currentThread().setErrno(getNativeTaskErrno(ntask));
//        // TODO: do something with error code. throw exception or store for later use.
//        int rc = deleteNativeTask(ntask);
//        if (rc != 0) {
//System.err.println("deleteNativeTask failed");
//        }
//        return result;
//    }
//    


    /*-----------------------------------------------------------------------*\
     *               Raw (byte-orietened) memory support                     *
    \*-----------------------------------------------------------------------*/
    
    /**
     * Allocate a block of memory outside of the Java heap.<p>
     * 
     * Memory may be be very restricted on some platforms. Callers shouldn't rely on 
     * this memory unless they have a good understanding of the memory resvered for malloc
     * on a particular platform.
     * 
     * @param size
     * @return address of memory block, or zero
     * @throws com.sun.squawk.pragma.NativePragma
     */
    public static Address malloc(UWord size) throws NativePragma {
         throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
    }
    
    public static void free(Address ptr) throws NativePragma {
         if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("unimplemented when hosted", "NativeUnsafe.java", 668);
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
     * @return The integer from raw memory.
     */
    public static int getUnalignedShort(Address base, int boffset) throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
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
    public static int getUnalignedInt(Address base, int boffset) throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
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
     * @return The integer from raw memory.
     */
    public static long getUnalignedLong(Address base, int boffset) throws NativePragma {
        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere("unimplemented when hosted");
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
    public static void setUnalignedShort(Address base, int boffset, int value) throws NativePragma {
        if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("unimplemented when hosted", "NativeUnsafe.java", 731);
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
    public static void setUnalignedInt(Address base, int boffset, int value) throws NativePragma {
        if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("unimplemented when hosted", "NativeUnsafe.java", 746);
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
    public static void setUnalignedLong(Address base, int boffset, long value) throws NativePragma {
        if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("unimplemented when hosted", "NativeUnsafe.java", 761);
    }
    
    /*-----------------------------------------------------------------------*\
     *                      Endianess swapping                               *
    \*-----------------------------------------------------------------------*/

    /**
     * Swaps the endianess of a value.
     *
     * @param address   the address of the value
     * @param dataSize  the size (in bytes) of the value
     */
    public static void swap(Address address, int dataSize) throws NativePragma {
        switch (dataSize) {
            case 1:              break;
            case 2: swap2(address); break;
            case 4: swap4(address); break;
            case 8: swap8(address); break;
            default: if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("NativeUnsafe.java", 780);
        }
    }

    /**
     * Swaps the endianess of a 2 byte value.
     *
     * @param address   the address of the value
     */
    public static void swap2(Address address) throws NativePragma {

//        byte type = NativeUnsafe.getType(address);
//        NativeUnsafe.setType(address, AddressType.ANY, 2);


        int val = NativeUnsafe.getChar(address, 0);

        int b0 = val        & 0xFF;
        int b1 = (val >> 8) & 0xFF;

        int newVal = (b0 << 8) | b1;

        NativeUnsafe.setChar(address, 0, newVal);


//        NativeUnsafe.setType(address, type , 2);

    }

    /**
     * Swaps the endianess of a 4 byte value.
     *
     * @param address   the address of the value
     */
    public static void swap4(Address address) throws NativePragma {

//        byte type = NativeUnsafe.getType(address);
//        NativeUnsafe.setType(address,AddressType.ANY, 4);


        int val = NativeUnsafe.getInt(address, 0);

        int b0 = val         & 0xFF;
        int b1 = (val >> 8)  & 0xFF;
        int b2 = (val >> 16) & 0xFF;
        int b3 = (val >> 24) & 0xFF;

        int newVal = (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;

        NativeUnsafe.setInt(address, 0, newVal);


//        NativeUnsafe.setType(address, type , 4);

    }

    /**
     * Swaps the endianess of a 8 byte value.
     *
     * @param address   the address of the value
     */
    public static void swap8(Address address) throws NativePragma {

//        byte type = NativeUnsafe.getType(address);
//        NativeUnsafe.setType(address, AddressType.ANY, 8);


        long val = NativeUnsafe.getLong(address, 0);

        long b0 = val         & 0xFF;
        long b1 = (val >> 8)  & 0xFF;
        long b2 = (val >> 16) & 0xFF;
        long b3 = (val >> 24) & 0xFF;
        long b4 = (val >> 32) & 0xFF;
        long b5 = (val >> 40) & 0xFF;
        long b6 = (val >> 48) & 0xFF;
        long b7 = (val >> 56) & 0xFF;

        long newVal = (b0 << 56) | (b1 << 48) | (b2 << 40) | (b3 << 32) | (b4 << 24) | (b5 << 16) | (b6 << 8) | b7;

        NativeUnsafe.setLong(address, 0, newVal);


//        NativeUnsafe.setType(address, type , 8);

    }

    /*-----------------------------------------------------------------------*\
     *                      Hosted execution support                         *
    \*-----------------------------------------------------------------------*/

    /**
     * A table of all the addresses that hold a pointer to a class which has
     * not yet been written to memory.
     */
    private static SquawkHashtable unresolvedClassPointers;

    /**
     * Resolve all the deferred writes of unresolved class pointers.
     *
     * @param classMap a map from JVM objects to their addresses in the image. This
     *                 is used to patch up class pointers in objects that were
     *                 written to the image before their classes were.
     */
    static void resolveClasses(ArrayHashtable classMap) throws HostedPragma {
        Enumeration keys = unresolvedClassPointers.keys();
        Hashtable unresolvedLeft = new Hashtable();
        while (keys.hasMoreElements()) {
            Address address = (Address) keys.nextElement();
            Klass unresolvedClass = (Klass) unresolvedClassPointers.get(address);
            Address klassAddress = (Address) classMap.get(unresolvedClass);
            if (klassAddress == null) {
                unresolvedLeft.put(unresolvedClass, unresolvedClass);
                continue;
            }
            setAddress(address, 0, klassAddress);
        }
        if (unresolvedLeft.size() > 0) {
            keys = unresolvedLeft.keys();

            StringBuilder builder = new StringBuilder();

////            StringBuffer builder = new StringBuffer();

            builder.append("The following Klasses were not serialized:");
            while (keys.hasMoreElements()) {
                builder.append("\n");
                builder.append(keys.nextElement());
            }
            builder.append("\n------");
            throw new RuntimeException(builder.toString());
        }
        unresolvedClassPointers.clear();
    }

    /**
     * Clears a pointer value in memory.
     *
     * @param base   the base address
     * @param offset the offset (in UWords) from <code>base</code> of the pointer to clear
     */
    public static void clearObject(Object base, int offset) throws HostedPragma {
        Address ea = ((Address)base).add(offset * HDR.BYTES_PER_WORD);
        setUWord(ea, 0, UWord.zero());
        unresolvedClassPointers.remove(ea);
        oopMap.clear(ea.asIndex() / HDR.BYTES_PER_WORD);
        setType0(ea.asIndex(), AddressType.UNDEFINED);
    }

    /*-----------------------------------------------------------------------*\
     *                      Memory model and initialization                  *
    \*-----------------------------------------------------------------------*/

    /**
     * The memory model.
     */
    private static byte[] memory;


//    /**
//     * The type checking map for memory.
//     */
//    private static byte[] typeMap;


    /**
     * The used amount of memory.
     */
    private static int memorySize;

    /**
     * The oop map describing where the pointers in memory are.
     */
    private static BitSet oopMap;
    
    /**
     * Do this little dance to avoid initializing static variables only used in a hosted environment.
     * Also used for testing.
     **/
    public static synchronized void hostedInit() throws HostedPragma {
        if (memory == null) {
            memorySize = 0;
            memory = new byte[0];

//            typeMap = new byte[0];

            oopMap = new BitSet();
            unresolvedClassPointers = new SquawkHashtable();
        }
    }

    /**
     * Verifies that a given address is within range of the currently allocated
     * memory.
     *
     * @param address  the address to check
     * @throws IndexOfOutBoundsException if the address is out of bounds
     */
    private static void checkAddress(int address) throws IndexOutOfBoundsException, HostedPragma {
        if (address < 0 || address >= memorySize) {
            throw new IndexOutOfBoundsException("address is out of range: " + address);
        }
    }

    /**
     * Ensures that the underlying buffer representing memory is at least a given size.
     *
     * @param size  the minimum size the memory buffer will be upon returning
     */
    private static void ensureCapacity(int size) throws HostedPragma {
        size = GC.roundUpToWord(size);
        if (memory.length < size) {
//System.err.println("growing memory: " + memory.length + " -> " + size*2);
            byte[] newMemory = new byte[size * 2];
            System.arraycopy(memory, 0, newMemory, 0, memory.length);
            memory = newMemory;

//            byte[] newTypeMap = new byte[memory.length];
//            System.arraycopy(typeMap, 0, newTypeMap, 0, typeMap.length);
//            typeMap = newTypeMap;

        }
    }

    /**
     * Initialize or appends to the contents of memory.
     *
     * @param buffer  a buffer containing a serialized object memory relative to 0
     * @param oopMap  an oop map specifying where the pointers in the serialized object memory are
     * @param append  specifies if the memory is being appended to
     */
    public static void initialize(byte[] buffer, BitSet oopMap, boolean append) throws HostedPragma {
        if (!append) {
            setMemorySize(buffer.length);
            System.arraycopy(buffer, 0, memory, 0, buffer.length);

            // Set up the oop map
            NativeUnsafe.oopMap.or(oopMap);
        } else {
            int canonicalStart = memorySize;
            setMemorySize(memorySize + buffer.length);
            System.arraycopy(buffer, 0, memory, canonicalStart, buffer.length);

            // OR the given oop map onto the logical end of the existing oop map
            int shift = canonicalStart / HDR.BYTES_PER_WORD;
            NativeUnsafe.oopMap.or(oopMap, shift);
        }
    }

    /**
     * Sets the size of used/initialized memory. If the new size is less than the current size, all
     * memory locations at index <code>newSize</code> and greater are zeroed.
     *
     * @param   newSize   the new size of memory
     */
    public static void setMemorySize(int newSize) throws HostedPragma {
        Assert.always(newSize >= 0, "NativeUnsafe.java", 1036);
        hostedInit();
        if (newSize > memorySize) {
            ensureCapacity(newSize);
        } else {
            for (int i = newSize ; i < memory.length ; i++) {
                memory[i] = 0;
                oopMap.clear(i / HDR.BYTES_PER_WORD);
            }
        }
        memorySize = newSize;
    }

    /**
     * Gets the amount of used/initialized memory.
     *
     * @return the amount of used/initialized memory
     */
    static int getMemorySize() throws HostedPragma {
        return memorySize;
    }

    /**
     * Determines if the word at a given address is a reference. A word is a reference if
     * the last update at the address was via {@link #setObject(Object,int,Object)}.
     *
     * @param address  the address to test
     * @return true if <code>address</code> is a reference
     */
    static boolean isReference(Address address) throws HostedPragma {
        return (address.asIndex() % HDR.BYTES_PER_WORD) == 0 && oopMap.get(address.asIndex() / HDR.BYTES_PER_WORD);
    }

    /**
     * Copies a range of memory into a buffer.
     *
     * @param buffer        the buffer to copy into
     * @param memoryOffset  the offset in memory at which to start copying from
     * @param bufferOffset  the offset in <code>buffer</code> at which to start copying to
     * @param               length the number of bytes to copy
     */
    public static void copyMemory(byte[] buffer, int memoryOffset, int bufferOffset, int length) throws HostedPragma {
        System.arraycopy(memory, memoryOffset, buffer, bufferOffset, length);
    }

    /**
     * Gets the oop map that describes where all the pointers in the memory are.
     *
     * @return the oop map that describes where all the pointers in the memory are
     */
    static BitSet getOopMap() throws HostedPragma {
        return oopMap;
    }
}
