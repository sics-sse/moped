/**** Created by Squawk builder from "vmcore/src/vm/memory.c.spp.preprocessed" ****/ /*
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

    /*-----------------------------------------------------------------------*\
     *                                     DEFNS                             *
    \*-----------------------------------------------------------------------*/


#if 0

#define INLINE_UNALIGNED INLINE
#else
#define INLINE_UNALIGNED 
#endif /* PLATFORM_UNALIGNED_LOADS */

#ifndef PLATFORM_UNALIGNED_64_LOADS
#define PLATFORM_UNALIGNED_64_LOADS PLATFORM_UNALIGNED_LOADS
#endif

    /*-----------------------------------------------------------------------*\
     *                           Memory access verification                  *
    \*-----------------------------------------------------------------------*/

/**
 * Comment the following line for %50 slower but better type checking
 */
#define FASTER_SET_TYPES

/**
 * Comment the following line for assume code that is 20% faster.
 */


/**
 * Address to look for in setType()
 */
#define BADSETTYPE 0

/**
 * Fast object allocation
 */
#define FASTALLOC true

#if SETASSUMES
#define setAssume(x) assume(x)
int     getByte(Address oop, int offset);
int     getUShort(Address oop, int offset);
UWord   getUWord(Address oop, int offset);
jlong   getLong(Address oop, int offset);
jlong   getLongAtWord(Address oop, int offset);
Address getObject(Address oop, int offset);
#else
#define setAssume(x) /**/
#endif

#ifndef C_PARMS_LEFT_TO_RIGHT
#define C_PARMS_LEFT_TO_RIGHT false
#endif

#ifndef C_PARMS_RIGHT_TO_LEFT
#define C_PARMS_RIGHT_TO_LEFT false
#endif

INLINE boolean setArrayLength(Address _oop, int _size);

        /*-----------------------------------------------------------------------*\
         *                               Assertions                               *
        \*-----------------------------------------------------------------------*/

INLINE int inRAM(Address ea_0) {
            Address addr = (ea_0);
    return addr >= com_sun_squawk_GC_ramStart && addr < com_sun_squawk_GC_ramEnd;
        }

INLINE int inROM(Address ea_1) {
            Address addr = (ea_1);
    return addr >= com_sun_squawk_VM_romStart && addr < com_sun_squawk_VM_romEnd;
        }

#ifdef FLASH_MEMORY
#define  inCode(ea_2) (  \
         inROM((ea_2))  \
        )
#else
INLINE int inCode(Address ea_3) {
            Address addr = (ea_3);
    return addr >= com_sun_squawk_VM_romStart && addr < com_sun_squawk_GC_ramEnd;
        }
#endif

        /*-----------------------------------------------------------------------*\
         *                            Type map checking                          *
        \*-----------------------------------------------------------------------*/

#if TYPEMAP
        /**
         * Gets the ASCII character representing a given type.
         *
         * @param type  the type to represent
         * @return the ASCII representation of 'type'
         */
INLINE char getTypeMnemonic(char type_4) {
            return AddressType_Mnemonics[(type_4) & AddressType_TYPE_MASK];
        }

        /**
         * Determines if a given address is within the range of type mapped memory.
         *
         * @param ea   the address to test
         * @return true if 'ea' is within the range of type mapped memory
         */
INLINE boolean isInTypedMemoryRange(Address ea_5) {
            return (hieq((ea_5), memory) && lo((ea_5), memoryEnd));
        }

        /**
         * Gets the address at which the type for a given address is recorded.
         *
         * @param ea   the address for which the type is being queried
         * @return the address at which the type for 'ea' is recorded
         */
INLINE char *getTypePointer(Address ea_6) {
            /*if (!isInTypedMemoryRange((ea_6))) {
                fprintf(stderr, format("access outside of 'memory' chunk: %A\n"), (ea_6));
                return;
             }
             */

            return (char *)(ea_6) + memorySize;
        }

        /**
         * Records the type of the value written to a given address.
         *
         * @param ea   the address written to
         * @param type the type of the value written to 'ea'
         * @param size the length in bytes of the field
         */
INLINE void setType(Address ea_8, char type_10, int size_12) {
            if (isInTypedMemoryRange((ea_8))) {
                char *ptr = getTypePointer((ea_8));
                switch ((size_12)) {
                    case 1:                                                                            break;
                    case 2: *( (unsigned short *)ptr)    = (unsigned short)AddressType_UNDEFINED_WORD; break;
                    case 4: *( (unsigned int   *)ptr)    = (unsigned int)  AddressType_UNDEFINED_WORD; break;
                    case 8: *( (unsigned int   *)ptr)    = (unsigned int)  AddressType_UNDEFINED_WORD;
                            *(((unsigned int   *)ptr)+1) = (unsigned int)  AddressType_UNDEFINED_WORD; break;
                    default: fatalVMError("unknown size in setType()");
                }
                *ptr = (type_10);

                if (BADSETTYPE && (ea_8) == (Address)BADSETTYPE) {
                    openTraceFile();
                    fprintf(
                            traceFile,
                            format("setType @ %A is %c,  [ea - rom = %A]\n"),
                            (ea_8),
                            getTypeMnemonic((type_10)),
                            Address_diff((ea_8), com_sun_squawk_VM_romStart)
                           );
                    printStackTrace("setType");
                }
            }
        }

        /**
         * Verifies that the type of the value at a given address matches a given type.
         *
         * @param ea   the address to test
         * @param type the type to match
         */
        void checkTypeError(Address ea, char recordedType, char type) {
            fprintf(
                    stderr,
                    format("checkType @ %A is %c, not %c  [ea - rom = %A]\n"),
                    ea,
                    getTypeMnemonic(recordedType),
                    getTypeMnemonic(type),
                    Address_diff(ea, com_sun_squawk_VM_romStart)
                   );
            fatalVMError("memory access type check failed");
        }

        /**
         * Verifies that the type of the value at a given address matches a given type.
         *
         * @param ea   the address to test
         * @param type the type to match
         */
INLINE void checkType2(Address ea_14, char recordedType_16, char type_18) {
            char recordedType = (char)((recordedType_16) & AddressType_TYPE_MASK);
            if (recordedType != AddressType_ANY && recordedType != (type_18)) {
                checkTypeError((ea_14), recordedType, (type_18));
            }
        }

        /**
         * Verifies that the type of the value at a given address matches a given type.
         *
         * @param ea   the address to test
         * @param type the type to match
         * @param size the length in bytes of the field
         */
INLINE Address checkType(Address ea_19, char type_20, int size_21) {
            if (isInTypedMemoryRange((ea_19))) {
                /* AddressType_ANY always matches */
                if ((type_20) != AddressType_ANY) {
                    char *a = (char *)(ea_19);
                    char *p = getTypePointer((ea_19));
                    char fillType = ((type_20) == AddressType_BYTECODE) ? AddressType_BYTECODE : AddressType_UNDEFINED;
#ifdef FASTER_SET_TYPES
                    checkType2(a++, *p, (type_20));
#else
                    switch ((size_21)) {
                        case 8: {
                            checkType2(a++, *p++, (type_20));
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            break;
                        }
                        case 4: {
                            checkType2(a++, *p++, (type_20));
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            break;
                        }
                        case 2: {
                            checkType2(a++, *p++, (type_20));
                            checkType2(a++, *p++, fillType);
                            break;
                        }
                        case 1: {
                            checkType2(a++, *p, (type_20));
                            break;
                        }
                        default: shouldNotReachHere();
                    }
#endif
                }
            }
            return (ea_19);
        }

        /**
         * Gets the type recorded for a given address.
         *
         * @param  the address to test
         */
#define  getType(ea_22) (  \
             *getTypePointer((ea_22))  \
        )

        /**
         * Gets the type of the value that is written to memory by the current memory mutating instruction.
         * This method assumes that the current value of 'ip' is one byte past the current
         * instruction (i.e. it points to the opcode of the next instruction).
         */
#define  getMutationType() (  \
             (char)((*getTypePointer(ip - 1) >> AddressType_MUTATION_TYPE_SHIFT) & AddressType_TYPE_MASK)  \
        )

        /**
         * Sets the type recorded for each address in a range of word-aligned memory to be AddressType_ANY, the default for every type.
         *
         * @param start   the start address of the memory range
         * @param end     the end address of the memory range
         */
        void zeroTypes(Address start, Address end) {
            /* memset is not used as this can only be called on the service thread. */
            UWordAddress s = (UWordAddress)(getTypePointer(start));
            UWordAddress e = (UWordAddress)(getTypePointer(end));
            assume(isWordAligned((UWord)s));
            assume(isWordAligned((UWord)e));
            while (s < e) {
                *s++ = AddressType_ANY_WORD;
            }
        }

        /**
         * Block copies the types recorded for a range of memory to another range of memory.
         *
         * @param src    the start address of the source range
         * @param dst    the start address of the destination range
         * @param length the length (in bytes) of the range
         */
        void copyTypes(Address src, Address dst, int length) {
            /* memmove is not used as this can only be called on the service thread. */
            assume(length >= 0);
/*fprintf(stderr, format("copyTypes: src=%A, dst=%A, length=%d\n"), src, dst, length);*/
            if (lo(src, dst)) {
                char *s = getTypePointer(src) + length;
                char *d = getTypePointer(dst) + length;
                char *end = getTypePointer(src);
                while (s != end) {
                    *--d = *--s;
                }
            } else if (hi(src, dst)) {
                char *s = getTypePointer(src);
                char *d = getTypePointer(dst);
                char *end = s + length;
                while (s != end) {
                    *d++ = *s++;
                }
            }
        }

#else

/**
 * These macros disable the type checking for a production build.
 * A macro replacement for 'getType()' is intentionally omitted.
 */
#define setType(ea, type, size)
#define checkType(ea, type, size)     ea
#define setTypeRange(ea, length, type)
#define zeroTypes(start, end)
#define copyTypes(src, dst, length)
#define getMutationType() 0
        char getType(Address ea) {
            fatalVMError("getType() called without TYPEMAP");
            return 0;
        }
#endif /* TYPEMAP */

    /*-----------------------------------------------------------------------*\
     *                              Memory addressing                        *
    \*-----------------------------------------------------------------------*/







#ifdef BAD_ADDRESS
void checkOneAddress(Address ea, int size, Address addr);
#else
#define checkOneAddress(ea, size, addr)
#endif /* BAD_ADDRESS */

#ifdef com_sun_squawk_CheneyCollector
#define cheneyCheck(ea) assume(cheneyStartMemoryProtect == 0 || \
                   lo(ea, cheneyStartMemoryProtect) || \
                   hieq(ea, cheneyEndMemoryProtect))
#else
#define cheneyCheck(ea)
#endif /* com_sun_squawk_CheneyCollector */

        /**
         * Performs a number of checks on a given part of memory immediately after
         * it was written to.
         *
         * @param ea   the address of the last write to memory
         * @param size the number of bytes written
         */
#define  checkPostWrite(ea_23, size_25) { Address  ea_24 = ea_23;  int  size_26 = size_25;  \
            checkOneAddress((ea_24), (size_26), (Address)BAD_ADDRESS); \
            cheneyCheck((ea_24)); \
        }

        /**
         * Given a base address and offset to a byte value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value specified by 'oop' and 'offset'
         */
#define  getByteTyped(base_27, offset_28, type_29) (  \
             *((signed char *)checkType(&((signed char *)(base_27))[(offset_28)], (type_29), 1))  \
        )

        /**
         * Given a base address and offset to a byte value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value specified by 'oop' and 'offset'
         */
#define  getUByteTyped(base_30, offset_31, type_32) (  \
             *((unsigned char *)checkType(&((unsigned char *)(base_30))[(offset_31)], (type_32), 1))  \
        )

        /**
         * Given a base address and offset to a byte value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setByteTyped(base_33, offset_35, type_37, value_39) { Address  base_34 = base_33;  Offset  offset_36 = offset_35;  char  type_38 = type_37;  signed char  value_40 = value_39;  \
            signed char *ea = &((signed char *)(base_34))[(offset_36)]; \
            setType(ea, (type_38), 1); \
            *ea = (value_40); \
            checkPostWrite(ea, 1); \
        }

        /**
         * Given a base address and offset to a short value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value
         */
#define  getShortTyped(base_41, offset_42, type_43) (  \
             *((short *)checkType(&((short *)(base_41))[(offset_42)], (type_43), 2))  \
        )

        /**
         * Given a base address and offset to a short value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value
         */
#define  getUShortTyped(base_44, offset_45, type_46) (  \
             *((unsigned short *)checkType(&((unsigned short *)(base_44))[(offset_45)], (type_46), 2))  \
        )

        /**
         * Given a base address and offset to a short value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setShortTyped(base_47, offset_49, type_51, value_53) { Address  base_48 = base_47;  Offset  offset_50 = offset_49;  char  type_52 = type_51;  short  value_54 = value_53;  \
            short *ea = &((short *)(base_48))[(offset_50)]; \
            setType(ea, (type_52), 2); \
            *ea = (value_54); \
            checkPostWrite(ea, 2); \
        }

        /**
         * Given a base address and offset to an integer value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value specified by 'oop' and 'offset'
         */
#define  getIntTyped(base_55, offset_56, type_57) (  \
             *((int *)checkType(&((int *)(base_55))[(offset_56)], (type_57), 4))  \
        )

        /**
         * Given a base address and offset to an integer value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setIntTyped(base_58, offset_60, type_62, value_64) { Address  base_59 = base_58;  Offset  offset_61 = offset_60;  char  type_63 = type_62;  int  value_65 = value_64;  \
            int *ea = &((int *)(base_59))[(offset_61)]; \
            setType(ea, (type_63), 4); \
            *ea = (value_65); \
            checkPostWrite(ea, 4); \
        }

        /**
         * Given a base address and offset to a 64 bit value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value
         */
#define  getLongAtWordTyped(base_66, offset_67, type_68) (  \
             *((jlong *)checkType(&((UWordAddress)(base_66))[(offset_67)], (type_68), 8))  \
        )

        /**
         * Given a base address and offset to a 64 bit value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setLongAtWordTyped(base_69, offset_71, type_73, value_75) { Address  base_70 = base_69;  Offset  offset_72 = offset_71;  char  type_74 = type_73;  jlong  value_76 = value_75;  \
            jlong *ea = (jlong *)&((UWordAddress)(base_70))[(offset_72)]; \
            setType(ea, (type_74), 8); \
            *ea = (value_76); \
            checkPostWrite(ea, 8); \
        }

        /**
         * Given a base address and offset to a 64 bit value, return the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 64 bit words) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value
         */
#define  getLongTyped(base_77, offset_78, type_79) (  \
             *((jlong *)checkType(&((jlong *)(base_77))[(offset_78)], (type_79), 8))  \
        )

        /**
         * Given a base address and offset to a 64 bit value, set the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 64 bit words) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setLongTyped(base_80, offset_82, type_84, value_86) { Address  base_81 = base_80;  Offset  offset_83 = offset_82;  char  type_85 = type_84;  jlong  value_87 = value_86;  \
            jlong *ea = (jlong *)&((jlong *)(base_81))[(offset_83)]; \
            setType(ea, (type_85), 8); \
            *ea = (value_87); \
            checkPostWrite(ea, 8); \
        }

        /**
         * Given a base address and offset to a UWord value, return the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value
         */
#if SQUAWK_64
#define  getUWordTyped(base_88, offset_89, type_90) (  \
             (UWord)getLongTyped((base_88), (offset_89), (type_90))  \
        )
#else
#define  getUWordTyped(base_91, offset_92, type_93) (  \
             (UWord)getIntTyped((base_91), (offset_92), (type_93))  \
        )
#endif

        /**
         * Given a base address and offset to a UWord value, set the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#if SQUAWK_64
#define  setUWordTyped(base_94, offset_96, type_98, value_100) { Address  base_95 = base_94;  Offset  offset_97 = offset_96;  char  type_99 = type_98;  UWord  value_101 = value_100;  \
            setLongTyped((base_95), (offset_97), (type_99), (UWord)(value_101)); \
        }
#else
#define  setUWordTyped(base_102, offset_104, type_106, value_108) { Address  base_103 = base_102;  Offset  offset_105 = offset_104;  char  type_107 = type_106;  UWord  value_109 = value_108;  \
            setIntTyped((base_103), (offset_105), (type_107), (UWord)(value_109)); \
        }
#endif


        /*-----------------------------------------------------------------------*\
         *                           Memory access interface                     *
        \*-----------------------------------------------------------------------*/

        /**
         * Sets an 8 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' at which to write
         * @param value the value to write
         */
#define  setByte(base_110, offset_112, value_114) { Address  base_111 = base_110;  Offset  offset_113 = offset_112;  int  value_115 = value_114;  \
            setByteTyped((base_111), (offset_113), AddressType_BYTE, (signed char)(value_115)); \
            setAssume(((value_115) & 0xFF) == (getByte((base_111), (offset_113)) & 0xFF)); \
        }

        /**
         * Sets a 16 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param value  the value to write
         */
#define  setShort(base_116, offset_118, value_120) { Address  base_117 = base_116;  Offset  offset_119 = offset_118;  int  value_121 = value_120;  \
            setShortTyped((base_117), (offset_119), AddressType_SHORT, (short)(value_121)); \
            setAssume(((value_121) & 0xFFFF) == getUShort((base_117), (offset_119))); \
        }

        /**
         * Sets a 32 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' at which to write
         * @param value  the value to write
         */
#define  setInt(base_122, offset_124, value_126) { Address  base_123 = base_122;  Offset  offset_125 = offset_124;  int  value_127 = value_126;  \
            setIntTyped((base_123), (offset_125), AddressType_INT, (value_127)); \
            setAssume((value_127) == getInt((base_123), (offset_125))); \
        }

        /**
         * Sets a UWord value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from oop at which to write
         * @param value  the value to write
         */
#define  setUWord(base_128, offset_130, value_132) { Address  base_129 = base_128;  Offset  offset_131 = offset_130;  UWord  value_133 = value_132;  \
            setUWordTyped((base_129), (offset_131), AddressType_UWORD, (value_133)); \
            setAssume((value_133) == getUWord((base_129), (offset_131))); \
        }

        /**
         * Sets a pointer value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from oop at which to write
         * @param value  the value to write
         */
#define  setObject(base_134, offset_136, value_138) { Address  base_135 = base_134;  Offset  offset_137 = offset_136;  Address  value_139 = value_138;  \
            setUWordTyped((base_135), (offset_137), AddressType_REF, (UWord)(value_139)); \
            setAssume((value_139) == getObject((base_135), (offset_137))); \
        }

        /**
         * Sets a pointer value in memory and updates write barrier bit for the pointer if
         * a write barrier is being maintained.
         *
         * @param base   the base address
         * @param offset the offset to a field in the object
         */
#define  setObjectAndUpdateWriteBarrier(base_140, offset_142, value_144) { Address  base_141 = base_140;  Offset  offset_143 = offset_142;  Address  value_145 = value_144;  \
            setObject((base_141), (offset_143), (value_145)); \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
 \
        }

        /**
         * Sets a 64 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 64 bit words) from 'base' at which to write
         * @param value  the value to write
         */
#define  setLong(base_146, offset_148, value_150) { Address  base_147 = base_146;  Offset  offset_149 = offset_148;  jlong  value_151 = value_150;  \
            setLongTyped((base_147), (offset_149), AddressType_LONG, (value_151)); \
            setAssume((value_151) == getLong((base_147), (offset_149))); \
        }

        /**
         * Sets a 64 bit value in memory at a UWord offset.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' at which to write
         * @param value  the value to write
         */
#define  setLongAtWord(base_152, offset_154, value_156) { Address  base_153 = base_152;  Offset  offset_155 = offset_154;  jlong  value_157 = value_156;  \
            if (SQUAWK_64 || PLATFORM_UNALIGNED_64_LOADS) { \
                setLongAtWordTyped((base_153), (offset_155), AddressType_LONG, (value_157)); \
            } else { \
                const int highOffset = (PLATFORM_BIG_ENDIAN) ? (offset_155)     : (offset_155) + 1; \
                const int lowOffset  = (PLATFORM_BIG_ENDIAN) ? (offset_155) + 1 : (offset_155); \
                setIntTyped((base_153), highOffset, AddressType_LONG,  (int)((value_157) >> 32)); \
                setIntTyped((base_153), lowOffset,  AddressType_LONG2, (int) (value_157)); \
            } \
            setAssume((value_157) == getLongAtWord((base_153), (offset_155))); \
        }

        /**
         * Sets a 16 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param value  the value to write
         */
INLINE_UNALIGNED void setUnalignedShort(Address base, Offset offset, int value) {
            signed char *ea = &((signed char *)base)[offset];

            if (PLATFORM_UNALIGNED_LOADS) {
                 setShortTyped(ea, 0, AddressType_SHORT, (short)value);
            } else {
                int b0 =  value       & 0xFF;
                int b1 = (value >> 8) & 0xFF;

                if (PLATFORM_BIG_ENDIAN) {
                    setByteTyped(ea, 0, AddressType_ANY, (char)b1);
                    setByteTyped(ea, 1, AddressType_ANY, (char)b0);
                } else {
                    setByteTyped(ea, 0, AddressType_ANY, (char)b0);
                    setByteTyped(ea, 1, AddressType_ANY, (char)b1);
                }
            }
            setAssume((value & 0xFFFF) == getUnalignedShort(base, offset));
        }

        /**
         * Sets a 32 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' at which to write
         * @param value  the value to write
         */
INLINE_UNALIGNED void setUnalignedInt(Address base, Offset offset, int value) {
            signed char *ea = &((signed char *)base)[offset];
            if (PLATFORM_UNALIGNED_LOADS) {
                 setIntTyped(ea, 0, AddressType_INT, value);
            } else {
                int b0 =  value        & 0xFF;
                int b1 = (value >> 8)  & 0xFF;
                int b2 = (value >> 16) & 0xFF;
                int b3 = (value >> 24) & 0xFF;
                if (PLATFORM_BIG_ENDIAN) {
                    setByteTyped(ea, 0, AddressType_ANY, (char)b3);
                    setByteTyped(ea, 1, AddressType_ANY, (char)b2);
                    setByteTyped(ea, 2, AddressType_ANY, (char)b1);
                    setByteTyped(ea, 3, AddressType_ANY, (char)b0);
                } else {
                    setByteTyped(ea, 0, AddressType_ANY, (char)b0);
                    setByteTyped(ea, 1, AddressType_ANY, (char)b1);
                    setByteTyped(ea, 2, AddressType_ANY, (char)b2);
                    setByteTyped(ea, 3, AddressType_ANY, (char)b3);
                }
            }
            setAssume(value == getUnalignedInt(base, offset));
        }

        /**
         * Sets a 64 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 64 bit words) from 'base' at which to write
         * @param value  the value to write
         */
INLINE_UNALIGNED void setUnalignedLong(Address base, Offset offset, jlong value) {
            signed char *ea = &((signed char *)base)[offset];
            if (PLATFORM_UNALIGNED_64_LOADS || (!PLATFORM_UNALIGNED_LOADS && isAligned((UWord)ea, 8))) {
                setLongTyped(ea, 0, AddressType_LONG, value);
            } else if (PLATFORM_UNALIGNED_LOADS) {
                /* optimize for 32-bit registers */
                unsigned int hi = value >> 32;
                unsigned int lo = value & 0xFFFFFFFF;
                if (PLATFORM_BIG_ENDIAN) {
                    setIntTyped(ea, 0, AddressType_LONG, hi);
                    setIntTyped(ea, 1, AddressType_LONG, lo);
                } else {
                    setIntTyped(ea, 0, AddressType_LONG, lo);
                    setIntTyped(ea, 1, AddressType_LONG, hi);
                }
            } else {
                /* optimize for 32-bit registers */
                unsigned int hi = value >> 32;
                unsigned int lo = value & 0xFFFFFFFF;
                unsigned int b0 =  hi        & 0xFF;
                unsigned int b1 = (hi >> 8)  & 0xFF;
                unsigned int b2 = (hi >> 16) & 0xFF;
                unsigned int b3 = (hi >> 24) & 0xFF;
                unsigned int b4 = (lo >> 0) & 0xFF;
                unsigned int b5 = (lo >> 8) & 0xFF;
                unsigned int b6 = (lo >> 16) & 0xFF;
                unsigned int b7 = (lo >> 24) & 0xFF;
                if (PLATFORM_BIG_ENDIAN) {
                    setByteTyped(ea, 0, AddressType_ANY, (char)b7);
                    setByteTyped(ea, 1, AddressType_ANY, (char)b6);
                    setByteTyped(ea, 2, AddressType_ANY, (char)b5);
                    setByteTyped(ea, 3, AddressType_ANY, (char)b4);
                    setByteTyped(ea, 4, AddressType_ANY, (char)b3);
                    setByteTyped(ea, 5, AddressType_ANY, (char)b2);
                    setByteTyped(ea, 6, AddressType_ANY, (char)b1);
                    setByteTyped(ea, 7, AddressType_ANY, (char)b0);
                } else {
                    setByteTyped(ea, 0, AddressType_ANY, (char)b0);
                    setByteTyped(ea, 1, AddressType_ANY, (char)b1);
                    setByteTyped(ea, 2, AddressType_ANY, (char)b2);
                    setByteTyped(ea, 3, AddressType_ANY, (char)b3);
                    setByteTyped(ea, 4, AddressType_ANY, (char)b4);
                    setByteTyped(ea, 5, AddressType_ANY, (char)b5);
                    setByteTyped(ea, 6, AddressType_ANY, (char)b6);
                    setByteTyped(ea, 7, AddressType_ANY, (char)b7);
                }
            }
            setAssume(value == getUnalignedLong(base, offset));
        }

        /**
         * Gets a signed 8 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' from which to load
         * @return the value
         */
#define  getByte(base_158, offset_159) (  \
             getByteTyped((base_158), (offset_159), AddressType_BYTE)  \
        )

        /**
         * Gets an unsigned 8 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' from which to load
         * @return the value
         */
#define  getUByte(base_160, offset_161) (  \
             getUByteTyped((base_160), (offset_161), AddressType_BYTE)  \
        )

        /**
         * Gets a signed 16 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' from which to load
         * @return the value
         */
#define  getShort(base_162, offset_163) (  \
             getShortTyped((base_162), (offset_163), AddressType_SHORT)  \
        )

        /**
         * Gets an unsigned 16 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' from which to load
         * @return the value
         */
#define  getUShort(base_164, offset_165) (  \
             getUShortTyped((base_164), (offset_165), AddressType_SHORT)  \
        )

        /**
         * Gets a signed 32 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' from which to load
         * @return the value
         */
#define  getInt(base_166, offset_167) (  \
             getIntTyped((base_166), (offset_167), AddressType_INT)  \
        )

        /**
         * Gets a UWord value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' from which to load
         * @return the value
         */
#define  getUWord(base_168, offset_169) (  \
             getUWordTyped((base_168), (offset_169), AddressType_UWORD)  \
        )

        /**
         * Gets a pointer from memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' from which to load
         * @return the value
         */
#define  getObject(base_170, offset_171) (  \
             (Address)getUWordTyped((base_170), (offset_171), AddressType_REF)  \
        )

        /**
         * Gets a 64 bit value from memory using a 64 bit word offset.
         *
         * @param base   the base address
         * @param offset the offset (in 64 bit words) from 'base' from which to load
         * @return the value
         */
#define  getLong(base_172, offset_173) (  \
             getLongTyped((base_172), (offset_173), AddressType_LONG)  \
        )

        /**
         * Gets a 64 bit value from memory using a UWord offset.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' from which to load
         * @return the value
         */
INLINE jlong getLongAtWord(Address base_174, Offset offset_175) {
            if (SQUAWK_64 || PLATFORM_UNALIGNED_64_LOADS) {
                return getLongAtWordTyped((base_174), (offset_175), AddressType_LONG);
            } else {
                const int highOffset = (PLATFORM_BIG_ENDIAN) ? (offset_175)     : (offset_175) + 1;
                const int lowOffset  = (PLATFORM_BIG_ENDIAN) ? (offset_175) + 1 : (offset_175);
                const unsigned int high = getIntTyped((base_174), highOffset, AddressType_LONG);
                const unsigned int low  = getIntTyped((base_174), lowOffset,  AddressType_LONG2);

                /*Some strange MSC 6 bug prevents the following line from working:
                  return (jlong)(((jlong)high) << 32 | (((jlong)low) & 0xFFFFFFFF)); */

                /* But, for some reason, the following two lines do: */
const jlong res = makeLong(high, low);
                return res;
            }
        }

        /**
         * Gets a signed 16 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' from which to load
         * @return the value
         */
INLINE_UNALIGNED int getUnalignedShort(Address base, Offset offset) {
            signed char *ea = &((signed char *)base)[offset];
            if (PLATFORM_UNALIGNED_LOADS) {
                 return getShortTyped(ea, 0, AddressType_ANY);
            } else {
                if (PLATFORM_BIG_ENDIAN) {
                    int b1 = getByteTyped(ea, 0, AddressType_ANY);
                    int b2 = getUByteTyped(ea, 1, AddressType_ANY);
                    return (b1 << 8) | b2;
                } else {
                    int b1 = getUByteTyped(ea, 0, AddressType_ANY);
                    int b2 = getByteTyped(ea,  1, AddressType_ANY);
                    return (b2 << 8) | b1;
                }
            }
        }

        /**
         * Gets a signed 32 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' from which to load
         * @return the value
         */
INLINE_UNALIGNED int getUnalignedInt(Address base, Offset offset) {
            signed char *ea = &((signed char *)base)[offset];
            if (PLATFORM_UNALIGNED_LOADS) {
                return getIntTyped(ea, 0, AddressType_ANY);
            } else {
                int b1 = getUByteTyped(ea, 0, AddressType_ANY);
                int b2 = getUByteTyped(ea, 1, AddressType_ANY);
                int b3 = getUByteTyped(ea, 2, AddressType_ANY);
                int b4 = getUByteTyped(ea, 3, AddressType_ANY);
                if (PLATFORM_BIG_ENDIAN) {
                    return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
                } else {
                    return (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
                }
            }
        }

        /**
         * Gets a 64 bit value from memory using a byte offset.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' from which to load
         * @return the value
         */
INLINE_UNALIGNED jlong getUnalignedLong(Address base, Offset offset) {
            signed char *ea = &((signed char *)base)[offset];
            if (PLATFORM_UNALIGNED_64_LOADS || (!PLATFORM_UNALIGNED_LOADS && isAligned((UWord)ea, 8))) {
                return getLongTyped(ea, 0, AddressType_ANY);
            } else if (PLATFORM_UNALIGNED_LOADS) {
                /* optimize for 32-bit registers */
                unsigned int first = getIntTyped((Address)ea, 0, AddressType_ANY);
                unsigned int second = getIntTyped((Address)ea, 1, AddressType_ANY);
                if (PLATFORM_BIG_ENDIAN) {
                      return ((jlong)first << 32) | second;
                } else {
                      return ((jlong)second << 32) | first;
                }
            } else {
                unsigned int b1 = getUByteTyped(ea, 0, AddressType_ANY);
                unsigned int b2 = getUByteTyped(ea, 1, AddressType_ANY);
                unsigned int b3 = getUByteTyped(ea, 2, AddressType_ANY);
                unsigned int b4 = getUByteTyped(ea, 3, AddressType_ANY);
                unsigned int b5 = getUByteTyped(ea, 4, AddressType_ANY);
                unsigned int b6 = getUByteTyped(ea, 5, AddressType_ANY);
                unsigned int b7 = getUByteTyped(ea, 6, AddressType_ANY);
                unsigned int b8 = getUByteTyped(ea, 7, AddressType_ANY);
                if (PLATFORM_BIG_ENDIAN) {
                    unsigned int lo =  (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
                    unsigned int hi =  (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
                    return ((jlong)hi << 32) | lo;
                } else {
                    unsigned int lo =  (b4 << 24) | (b3 << 16) | (b2 << 8) | b1;
                    unsigned int hi =  (b8 << 24) | (b7 << 16) | (b6 << 8) | b5;
                    return ((jlong)hi << 32) | lo;
                }
            }
        }

        /*-----------------------------------------------------------------------*\
         *                           Endianess swapping                          *
        \*-----------------------------------------------------------------------*/

        /**
         * Swaps the endianess of a 2 byte value.
         *
         * @param address   the address of the value
         */
#define  swap2(address_176) { Address  address_177 = address_176;  \
            char type = (TYPEMAP ? getType((address_177)) : AddressType_UNDEFINED); \
            if (!(PLATFORM_UNALIGNED_LOADS || isAligned((UWord)(address_177), 2))) { \
                int b0 = getUByteTyped((address_177), 0, AddressType_ANY); \
                int b1 = getUByteTyped((address_177), 1, AddressType_ANY); \
                setByteTyped((address_177), 0, AddressType_ANY, (char)b1); \
                setByteTyped((address_177), 1, AddressType_ANY, (char)b0); \
            } else { \
                int value = getUShortTyped((address_177), 0, AddressType_ANY); \
                int b0 =  value       & 0xFF; \
                int b1 = (value >> 8) & 0xFF; \
                value = (b0 << 8) | b1; \
                setShortTyped((address_177), 0, type, (short)value); \
            } \
        }

        INLINE unsigned int SWAP4_VAL(unsigned int val) {
            return ((val & 0x000000FF) << 24) | ((val & 0x0000FF00) << 8) | ((val & 0x00FF0000) >> 8) | ((val & 0xFF000000) >> 24);
        }

        /**
         * Swaps the endianess of a 4 byte value.
         *
         * @param address   the address of the value
         */
        void swap4(Address address) {
            char type = (TYPEMAP ? getType(address) : AddressType_UNDEFINED);
            if (!(PLATFORM_UNALIGNED_LOADS || isAligned((UWord)address, 4))) {
                int b0 = getUByteTyped(address, 0, AddressType_ANY);
                int b1 = getUByteTyped(address, 1, AddressType_ANY);
                int b2 = getUByteTyped(address, 2, AddressType_ANY);
                int b3 = getUByteTyped(address, 3, AddressType_ANY);
                setByteTyped(address, 0, AddressType_ANY, (char)b3);
                setByteTyped(address, 1, AddressType_ANY, (char)b2);
                setByteTyped(address, 2, AddressType_ANY, (char)b1);
                setByteTyped(address, 3, AddressType_ANY, (char)b0);
            } else {
                setIntTyped(address, 0, type, SWAP4_VAL(getIntTyped(address, 0, AddressType_ANY)));
            }
        }

        /**
         * Swaps the endianess of a 8 byte value.
         *
         * @param address   the address of the value
         */
        void swap8(Address address) {
            char type = (TYPEMAP ? getType(address) : AddressType_UNDEFINED);
            if (!(PLATFORM_UNALIGNED_LOADS || isAligned((UWord)address, 4))) {
                int b0 = getUByteTyped(address, 0, AddressType_ANY);
                int b1 = getUByteTyped(address, 1, AddressType_ANY);
                int b2 = getUByteTyped(address, 2, AddressType_ANY);
                int b3 = getUByteTyped(address, 3, AddressType_ANY);
                int b4 = getUByteTyped(address, 4, AddressType_ANY);
                int b5 = getUByteTyped(address, 5, AddressType_ANY);
                int b6 = getUByteTyped(address, 6, AddressType_ANY);
                int b7 = getUByteTyped(address, 7, AddressType_ANY);
                setByteTyped(address, 0, AddressType_ANY, (char)b7);
                setByteTyped(address, 1, AddressType_ANY, (char)b6);
                setByteTyped(address, 2, AddressType_ANY, (char)b5);
                setByteTyped(address, 3, AddressType_ANY, (char)b4);
                setByteTyped(address, 4, AddressType_ANY, (char)b3);
                setByteTyped(address, 5, AddressType_ANY, (char)b2);
                setByteTyped(address, 6, AddressType_ANY, (char)b1);
                setByteTyped(address, 7, AddressType_ANY, (char)b0);
            } else {
                /* optimize for 32-bit registers */
                unsigned int hi = getIntTyped(address, 0, AddressType_ANY);
                unsigned int lo = getIntTyped(address, 1, AddressType_ANY);
                setIntTyped(address, 0, type, SWAP4_VAL(lo));
                setIntTyped(address, 1, type, SWAP4_VAL(hi));
            }
        }

        /**
         * Swaps the endianess of a value.
         *
         * @param address   the address of the value
         * @param dataSize  the size (in bytes) of the value
         */
#define  swap(address_178, dataSize_180) { Address  address_179 = address_178;  int  dataSize_181 = dataSize_180;  \
            /*fprintf(stderr, format("swap(%A, %d)\n"), (address_179), (dataSize_181));*/ \
            switch ((dataSize_181)) { \
                case 1:               break; \
                case 2: swap2((address_179)); break; \
                case 4: swap4((address_179)); break; \
                case 8: swap8((address_179)); break; \
                default: \
                    fprintf(stderr, "dataSize=%d\n", (dataSize_181)); \
                    shouldNotReachHere(); \
            } \
        }

        /**
         * Swaps the endianess of a word sized value.
         *
         * @param address   the address of the value
         */
#define  swapWord(address_182) { Address  address_183 = address_182;  \
            swap((address_183), HDR_BYTES_PER_WORD); \
        }

        /*-----------------------------------------------------------------------*\
         *                             Memory management                         *
        \*-----------------------------------------------------------------------*/

        /**
         * Zeros a range of words.
         *
         * @param start the start address
         * @param end the end address
         */
#define  zeroWords(start_184, end_186) { UWordAddress  start_185 = start_184;  UWordAddress  end_187 = end_186;  \
            assume(isWordAligned((UWord)(start_185))); \
            assume(isWordAligned((UWord)(end_187))); \
            zeroTypes((start_185), (end_187)); \
            while ((start_185) < (end_187)) { \
                *(start_185) = 0; \
                (start_185)++; \
            } \
        }

#define  traceAllocation(oop_188, size_190) { Address  oop_189 = oop_188;  int  size_191 = size_190;  \
            fprintf(stderr, "%s allocating object: size=%d, alloc free=%d, total free=%d, ptr=%d\n", \
                    (((oop_189) != 0) ? "succeeded" : "failed"), \
                    (size_191), \
                    Address_diff(com_sun_squawk_GC_allocEnd, com_sun_squawk_GC_allocTop), \
                    Address_diff(com_sun_squawk_GC_heapEnd, com_sun_squawk_GC_allocTop), \
                    (Offset)(oop_189)); \
        }

        /**
         * Allocate a chunk of zeroed memory from RAM.
         *
         * @param   size        the length in bytes of the object and its header (i.e. the total number of bytes to be allocated).
         * @param   arrayLength the number of elements in the array being allocated or -1 if a non-array object is being allocated
         * @return a pointer to a well-formed object or null if the allocation failed
         */
        Address allocate(int size, Address klass, int arrayLength) {
            Address block = com_sun_squawk_GC_allocTop;
            Offset available = Address_diff(com_sun_squawk_GC_allocEnd, block);
            Address oop;
            assume(size >= 0);
            if (unlikely(lt(available, size))) {
                if (available < 0) {
                    /* The last allocation overflowed the allocEnd boundary */
                    /*traceAllocation(null, size);*/
                    return null;
                } else {
                    /*
                     * If the object being allocated does not fit in the remaining allocation space
                     * (e.g. Lisp2 young generation) but there is enough total memory available,
                     * then allow the allocation to succeed. Without this, allocation of objects larger than
                     * the allocation space would never succeed.
                     */
                    available = Address_diff(com_sun_squawk_GC_heapEnd, block);
                    if (lt(available, size)) {
                        /*traceAllocation(null, size);*/
                        return null;
                    }
                }
            }

            if (arrayLength == -1) {
                oop = Address_add(block, HDR_basicHeaderSize);
                setObject(oop, HDR_klass, klass);
            } else {
                oop = Address_add(block, HDR_arrayHeaderSize);
                setObject(oop, HDR_klass, klass);
                if (!setArrayLength(oop, arrayLength)) {
                    return 0;
                }
            }
            com_sun_squawk_GC_allocTop = Address_add(block, size);
            zeroWords(oop, com_sun_squawk_GC_allocTop);
            com_sun_squawk_GC_newCount++;
            /*traceAllocation(oop, size);*/
            return oop;
        }

        /**
         * Allocate a chunk of zeroed memory from RAM with hosted.
         *
         * @param   size        the length in bytes of the object and its header (i.e. the total number of
         *                      bytes to be allocated).
         * @param   arrayLength the number of elements in the array being allocated or -1 if a non-array
         *                      object is being allocated
         * @return a pointer to a well-formed object or null if the allocation failed
         */
INLINE Address allocateFast(int size_192, Address klass_193, int arrayLength_194) {
            if (
                com_sun_squawk_GC_excessiveGC != false       ||
                com_sun_squawk_GC_allocationEnabled == false ||
                (com_sun_squawk_GC_GC_TRACING_SUPPORTED && (com_sun_squawk_GC_traceFlags & com_sun_squawk_GC_TRACE_ALLOCATION) != 0)
               ) {
                return null; /* Force call to Java code */
            }
            return allocate((size_192), (klass_193), (arrayLength_194));
        }

        /**
         * Static version of {@link #getDataSize()} so that garbage collector can
         * invoke this method on a possibly forwarded Klass object.
         */
INLINE int getDataSize(Address klass_195) {
            switch (com_sun_squawk_Klass_id((klass_195))) {
                case CID_BOOLEAN:
                case CID_BYTECODE:
                case CID_BYTE: {
                    return 1;
                }
                case CID_CHAR:
                case CID_SHORT: {
                    return 2;
                }
                case CID_DOUBLE:
                case CID_LONG: {
                    return 8;
                }
                case CID_FLOAT:
                case CID_INT: {
                    return 4;
                }
                default: {
                    return HDR_BYTES_PER_WORD;
                }
            }
        }

        /**
         * Copies bytes using memmove.
         */
        void copyBytes(Address src, int srcPos, Address dst, int dstPos, int length, boolean nvmDst) {
            /*fprintf(stderr, format("copying  %d bytes from %A at offset %d to %A at offset %d nvmDst=%d\n"), length, src, srcPos, dst, dstPos, nvmDst);*/
            if (nvmDst) {
                sysToggleMemoryProtection(com_sun_squawk_GC_nvmStart, com_sun_squawk_GC_nvmEnd, false);
            }
            memmove(Address_add(dst, dstPos), Address_add(src, srcPos), length);
            checkPostWrite(Address_add(dst, dstPos), length);
            if (nvmDst) {
                sysToggleMemoryProtection(com_sun_squawk_GC_nvmStart, com_sun_squawk_GC_nvmEnd, true);
            }
        }

#ifdef BAD_ADDRESS
        /**
         * Checks to see if a specific address was written to and print it if it was.
         *
         * @param ea    the address of the last write to memory
         * @param size  the number of bytes written
         * @param addr  the address to check for
         */
        void checkOneAddress(Address ea, int size, Address addr) {
            ByteAddress start  = (ByteAddress)ea;
            ByteAddress end    = start + size;
            ByteAddress target = (ByteAddress)addr;
            if (target >= start && target < end) {
                UWord value = ((UWord *)target)[0];
                fprintf(stderr, format("*******************  [%A] = %A [bcount=%L]\n"), target, value, getBranchCount());
#ifdef BAD_VALUE
                if (value == BAD_VALUE) {
                    fprintf(stderr, format("Stopping because bad value %A written in the range [%A .. %A)\n"), value, start, end);
                    stopVM(-1);
                }
#endif
            }
        }
#endif /* BAD_ADDRESS */
