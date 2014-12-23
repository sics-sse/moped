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
import com.sun.squawk.pragma.NotInlinedPragma;
import com.sun.squawk.pragma.AllowInlinedPragma;

/**
 * The MethodHeader class handles decoding Method headers.
 *
 * The format of the header is described by the
 * following pseudo-C structures:
 * <p><hr><blockquote><pre>
 *  header {
 *      {
 *           u2 type;                               // class id of a float, long, double, Address, Offset or UWord local variable
 *           u4 index;                              // index of the local variable
 *      } type_table[type_table_size];
 *      {
 *          u4 start_pc;
 *          u4 end_pc;
 *          u4 handler_pc;
 *          u2 catch_type;    // index into defining class's objectTable
 *      } exception_table[exception_table_size];
 *      u1 oopMap[oopMap_size];
 *      union {
 *          {
 *              u1 lo;      //  lllsssss
 *              u1 hi;      //  0pppppll
 *          } small_minfo;  //  'lllll' is locals_count, 'sssss' is max_stack, 'ppppp' is parameters_count
 *          {
 *              minfo_size type_table_size;         // exists only if 'T' bit in 'fmt' is set
 *              minfo_size exception_table_size;    // exists only if 'E' bit in 'fmt' is set
 *              minfo_size parameters_count;
 *              minfo_size locals_count;
 *              minfo_size max_stack;
 *              u1 fmt;                             // 1000ITRE
 *          } large_minfo;
 *      }
 *  }
 *
 * The minfo_size type is a u1 value if its high bit is 0, otherwise its a u2 value where
 * the high bit is masked off.
 *
 * </pre></blockquote><hr><p>
 *
 * The structures described above are actually stored in a byte array
 * encoded and decoded with a {@link ByteBufferEncoder} and
 * {@link ByteBufferDecoder} respectively.
 *
 */
public class MethodHeader {

    /**
     * Configuration option.
     * <p>
     * If set true then a long or double local variable will be
     * referenced as slot+0. If set false then it is addressed as slot+1.
     * <p>
     * Setting this false is will produce the correct offsets when the locals
     * are allocated at a negative offset from the frame pointer (which is common
     * for virtually all C ABIs).
     */
    public final static boolean LOCAL_LONG_ORDER_NORMAL = false;


    /**
     * The method info is capable of encoding the types of the parameters and locals,
     * but this is currently only used for debugging when using the CheneyCollector (see checkActivationForAddresses()).
     *
     * Since the LARGE format can impose a 5% penalty on method calls (complicating extend and return),
     * only generate type tables when asserts are on.
     */
    public static final boolean ENABLE_SPECIFIC_TYPE_TABLES = Klass.ASSERTIONS_ENABLED;

    /**
     * Debug LARGE format.
     */
    public static final boolean FORCE_LARGE_FORMAT = /*VAL*/false/*DEBUG_CODE_ENABLED*/;

    /**
     * Minfo format encodings.
     */
    protected final static int FMT_LARGE   = 0x80,   // specifies a large minfo section
                               FMT_E       = 0x01,   // specifies that there is an exception table
                               FMT_T       = 0x02,   // specifies that there is a type table
                               FMT_I       = 0x04;   // specifies that the method is only invoked by the interpreter
 
    private MethodHeader() {    }


    /*-----------------------------------------------------------------------*\
     *                                Decoding                               *
    \*-----------------------------------------------------------------------*/

    /**
     * Determines if a given method is only invoked from the interpreter
     *
     * @param oop the pointer to the method
     * @return true if oop is an interpreter invoked only method
     */
    public static boolean isInterpreterInvoked(Object oop) {
        int b0 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart);
        if (b0 < 128) {
            return false;
        } else {
            return (b0 & FMT_I) != 0;
        }
    }

    /**
     * Decodes the parameter count from the method header.
     *
     * @param oop the pointer to the method
     * @return the number of parameters
     */
    static int decodeParameterCount(Object oop) throws AllowInlinedPragma {
        int b0 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart);
        if (b0 < 128) {
            return b0 >> 2;
        } else {
            return minfoValue3(oop);
        }
    }

    /**
     * Decodes the local variable count from the method header.
     *
     * @param oop the pointer to the method
     * @return the number of locals
     */
    static int decodeLocalCount(Object oop) throws AllowInlinedPragma {
        int b0 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart);
        if (b0 < 128) {
            int b1 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart-1);
            return (((b0 << 8) | b1) >> 5) & 0x1F;
        } else {
            return minfoValue2(oop);
        }
    }

    /**
     * Decodes the stack count from the method header.
     *
     * @param oop the pointer to the method
     * @return the number of stack words
     */
    static int decodeStackCount(Object oop) throws AllowInlinedPragma {
        int b0 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart);
        if (b0 < 128) {
            int b1 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart-1);
            return b1 & 0x1F;
        } else {
            return minfoValue1(oop);
        }
    }

    /**
     * Decodes the exception table size from the method header.
     *
     * @param oop the pointer to the method
     * @return the number of bytes
     */
    static int decodeExceptionTableSize(Object oop) throws AllowInlinedPragma {
        int b0 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart);
        if (b0 < 128 || ((b0 & FMT_E) == 0)) {
            return 0;
        }
        return minfoValue4(oop);
    }

    /**
     * Decodes the type table size from the method header.
     *
     * @param oop the pointer to the method
     * @return the number of bytes
     */
    static int decodeTypeTableSize(Object oop)
/*if[!ASSERTIONS_ENABLED]*/
                                                throws AllowInlinedPragma
/*end[ASSERTIONS_ENABLED]*/
    {
        if (ENABLE_SPECIFIC_TYPE_TABLES) {
            int b0 = NativeUnsafe.getUByte(oop, HDR.methodInfoStart);
            if (b0 < 128 || ((b0 & FMT_T) == 0)) {
                return 0;
            }
            int offset = 4;
            if ((b0 & FMT_E) != 0) {
                offset++;
            }
            return minfoValue(oop, offset);
        } else {
            return 0;
        }
    }

    /**
     * Decode a counter from the minfo area.
     *
     * This is the canonical, unrolled form. Callers actually use the unrolled forms below.
     *
     * Note that these methods are also translated to C (as part of the vm2c process. So these
     * methods are actually the source for the interpreter too.
     *
     * @param oop the pointer to the method
     * @param offset the ordinal offset of the counter (e.g. 1st, 2nd, ...  etc.)
     * @return the value
     */
    private static int minfoValue(Object oop, int offset) throws NotInlinedPragma {
        int p = HDR.methodInfoStart - 1;
        int val = -1;
        Assert.that((NativeUnsafe.getUByte(oop, p+1) & FMT_LARGE) != 0);
        while(offset-- > 0) {
            val = NativeUnsafe.getUByte(oop, p--);
            if (val > 127) {
                p--;
            }
        }
        if (val > 127) {
            val = val & 0x7F;
            val = val << 8;
            val = val | (NativeUnsafe.getUByte(oop, p-1));
        }
        Assert.that(val >= 0);
        return val;
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c(root="MethodHeader_minfoValue1_L")
/*end[JAVA5SYNTAX]*/
    private static int minfoValue1(Object oop) throws NotInlinedPragma {
        int p = HDR.methodInfoStart - 1;
        int val;
        Assert.that((NativeUnsafe.getUByte(oop, p+1) & FMT_LARGE) != 0);
        val = NativeUnsafe.getUByte(oop, p--);
        if (val > 127) {
            val = val & 0x7F;
            val = val << 8;
            val = val | (NativeUnsafe.getUByte(oop, p));
        }
        Assert.that(val >= 0);
        return val;
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c(root="MethodHeader_minfoValue2_L")
/*end[JAVA5SYNTAX]*/
    private static int minfoValue2(Object oop) throws NotInlinedPragma {
        int p = HDR.methodInfoStart - 1;
        int val;
        Assert.that((NativeUnsafe.getUByte(oop, p+1) & FMT_LARGE) != 0);
        if (NativeUnsafe.getByte(oop, p--) < 0) {
            p--;
        }
        val = NativeUnsafe.getUByte(oop, p--);
        if (val > 127) {
            val = val & 0x7F;
            val = val << 8;
            val = val | NativeUnsafe.getUByte(oop, p);
        }
        Assert.that(val >= 0);
        return val;
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c(root="MethodHeader_minfoValue3_L")
/*end[JAVA5SYNTAX]*/
    private static int minfoValue3(Object oop) throws NotInlinedPragma {
        int p = HDR.methodInfoStart - 1;
        int val;
        Assert.that(((NativeUnsafe.getUByte(oop, p+1)) & FMT_LARGE) != 0);
        if (NativeUnsafe.getByte(oop, p--) < 0) {
            p--;
        }
        if (NativeUnsafe.getByte(oop, p--) < 0) {
            p--;
        }
        val = NativeUnsafe.getUByte(oop, p--);
        if (val > 127) {
            val = val & 0x7F;
            val = val << 8;
            val = val | NativeUnsafe.getUByte(oop, p);
        }
        Assert.that(val >= 0);
        return val;
    }

    private static int minfoValue4(Object oop) throws NotInlinedPragma  {
        int p = HDR.methodInfoStart - 1;
        int val;
        Assert.that(((NativeUnsafe.getByte(oop, p+1)) & FMT_LARGE) != 0);
        if (NativeUnsafe.getByte(oop, p--) < 0) {
            p--;
        }
        if (NativeUnsafe.getByte(oop, p--) < 0) {
            p--;
        }
        if (NativeUnsafe.getByte(oop, p--) < 0) {
            p--;
        }
        val = NativeUnsafe.getUByte(oop, p--);
        if (val > 127) {
            val = val & 0x7F;
            val = val << 8;
            val = val | NativeUnsafe.getUByte(oop, p);
        }
        Assert.that(val >= 0);
        return val;
    }

    /**
     * Get the offset to the last byte of the Minfo area.
     *
     * @param oop the pointer to the method
     * @return the length in bytes
     */
    private static int getOffsetToLastMinfoByte(Object oop) {
        int p = HDR.methodInfoStart;
        int b0 = NativeUnsafe.getUByte(oop, p--);
        if (b0 < 128) {
            return p;
        } else {
            return getOffsetToLastMinfoByte0(oop, p, b0);
        }
    }

    /**
     * Get the offset to the last byte of the Minfo area.
     *
     * @param oop the pointer to the method
     * @return the length in bytes
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(root="MethodHeader_getOffsetToLastMinfoByte0_LII")
/*end[JAVA5SYNTAX]*/
    private static int getOffsetToLastMinfoByte0(Object oop, int p, int b0) throws NotInlinedPragma {
            int offset = 3;
            if ((b0 & FMT_E) != 0) {
                offset++;
            }
            if (ENABLE_SPECIFIC_TYPE_TABLES && (b0 & FMT_T) != 0) {
                offset++;
            }
            while(offset-- > 0) {
                int val = NativeUnsafe.getUByte(oop, p--);
                if (val > 127) {
                    p--;
                }
            }
        return p + 1;
    }

    /**
     * Decodes the offset from the method header to the start of the oop map.
     *
     * @param oop the pointer to the method
     * @return the offset in bytes
     */
    static int decodeOopmapOffset(Object oop) {
        int vars = decodeLocalCount(oop) + decodeParameterCount(oop);
        int oopmapLth = (vars+7)/8;
        return getOffsetToLastMinfoByte(oop) - oopmapLth;
    }

    /**
     * Decodes the offset from the method header to the start of the exception table.
     *
     * @param oop the pointer to the method
     * @return the offset in bytes
     */
    static int decodeExceptionTableOffset(Object oop) {
        int vars = decodeLocalCount(oop) + decodeParameterCount(oop);
        int oopmapLth = (vars+7)/8;
        return getOffsetToLastMinfoByte(oop) - oopmapLth - decodeExceptionTableSize(oop);
    }

    /**
     * Decodes the offset from the method header to the start of the type table.
     *
     * @param oop the pointer to the method
     * @return the offset in bytes
     */
    static int decodeTypeTableOffset(Object oop) {
        int vars = decodeLocalCount(oop) + decodeParameterCount(oop);
        int oopmapLth = (vars+7)/8;
        return getOffsetToLastMinfoByte(oop) - oopmapLth - decodeExceptionTableSize(oop) - decodeTypeTableSize(oop);
    }

    /**
     * Decodes the oopmap and type table into an array of Klass instances.
     * <p>
     * This cannot be used by the garbage collector because it allocates an object.
     *
     * @param oop  the pointer to the method
     * @return the type map as an array of Klass instances
     */
    static Klass[] decodeTypeMap(Object oop) {

        int localCount     = decodeLocalCount(oop);
        int parameterCount = decodeParameterCount(oop);

        Klass types[] = new Klass[parameterCount+localCount];

        /*
         * Decodes the oopmap.
         */
        if (types.length > 0) {
            int offset = decodeOopmapOffset(oop);
            for (int i = 0 ; i < types.length ; i++) {
                int pos = i / 8;
                int bit = i % 8;
                int bite = NativeUnsafe.getUByte(oop, offset+pos);
                boolean isRef = ((bite>>bit)&1) != 0;
                types[i] = (isRef) ? Klass.OBJECT : Klass.INT;
            }
        }

        /*
         * Decodes the type table.
         */
        if (decodeTypeTableSize(oop) > 0) {
            int size   =  decodeTypeTableSize(oop);
            int offset =  decodeTypeTableOffset(oop);
            VMBufferDecoder dec = new VMBufferDecoder(oop, offset);
            int end = offset + size;
            while (dec.getOffset() < end) {
                int cid  = dec.readUnsignedShort();
                int slot = dec.readUnsignedInt();
                int slot2 = slot < parameterCount || LOCAL_LONG_ORDER_NORMAL ? slot + 1 : slot - 1;
                switch (cid) {
                    case CID.ADDRESS: types[slot]  = Klass.ADDRESS; break;
                    case CID.OFFSET:  types[slot]  = Klass.OFFSET;  break;
                    case CID.UWORD:   types[slot]  = Klass.UWORD;   break;
                    case CID.LONG:    types[slot]  = Klass.LONG;
                                      types[slot2] = Klass.LONG2;   break;
/*if[FLOATS]*/
                    case CID.FLOAT:   types[slot]  = Klass.FLOAT;   break;
                    case CID.DOUBLE:  types[slot]  = Klass.DOUBLE;
                                      types[slot2] = Klass.DOUBLE2; break;
/*end[FLOATS]*/
                    default: Assert.shouldNotReachHere();
                }
            }
        }
        return types;
    }

    /**
     * Return the address of the first word of the object header.
     *
     * @param oop the pointer to the method
     * @return the VM address of the header
     */
    static Address oopToBlock(Address oop) throws NotInlinedPragma {
        int offset = decodeTypeTableOffset(oop.toObject());
        while ((offset % HDR.BYTES_PER_WORD) != 0) {
            --offset;
        }
        offset -= HDR.BYTES_PER_WORD; // skip back the header length word
        return oop.add(offset);
    }

}
