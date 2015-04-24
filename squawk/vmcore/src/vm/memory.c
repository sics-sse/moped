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

INLINE int inRAM(Address ea_42) {
            Address addr = (ea_42);
    return addr >= com_sun_squawk_GC_ramStart && addr < com_sun_squawk_GC_ramEnd;
        }

INLINE int inROM(Address ea_43) {
            Address addr = (ea_43);
    return addr >= com_sun_squawk_VM_romStart && addr < com_sun_squawk_VM_romEnd;
        }

#ifdef FLASH_MEMORY
#define  inCode(ea_44) (  \
         inROM((ea_44))  \
        )
#else
INLINE int inCode(Address ea_45) {
            Address addr = (ea_45);
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
INLINE char getTypeMnemonic(char type_46) {
            return AddressType_Mnemonics[(type_46) & AddressType_TYPE_MASK];
        }

        /**
         * Determines if a given address is within the range of type mapped memory.
         *
         * @param ea   the address to test
         * @return true if 'ea' is within the range of type mapped memory
         */
INLINE boolean isInTypedMemoryRange(Address ea_47) {
            return (hieq((ea_47), memory) && lo((ea_47), memoryEnd));
        }

        /**
         * Gets the address at which the type for a given address is recorded.
         *
         * @param ea   the address for which the type is being queried
         * @return the address at which the type for 'ea' is recorded
         */
INLINE char *getTypePointer(Address ea_48) {
            /*if (!isInTypedMemoryRange((ea_48))) {
                fprintf(stderr, format("access outside of 'memory' chunk: %A\n"), (ea_48));
                return;
             }
             */

            return (char *)(ea_48) + memorySize;
        }

        /**
         * Records the type of the value written to a given address.
         *
         * @param ea   the address written to
         * @param type the type of the value written to 'ea'
         * @param size the length in bytes of the field
         */
INLINE void setType(Address ea_50, char type_52, int size_54) {
            if (isInTypedMemoryRange((ea_50))) {
                char *ptr = getTypePointer((ea_50));
                switch ((size_54)) {
                    case 1:                                                                            break;
                    case 2: *( (unsigned short *)ptr)    = (unsigned short)AddressType_UNDEFINED_WORD; break;
                    case 4: *( (unsigned int   *)ptr)    = (unsigned int)  AddressType_UNDEFINED_WORD; break;
                    case 8: *( (unsigned int   *)ptr)    = (unsigned int)  AddressType_UNDEFINED_WORD;
                            *(((unsigned int   *)ptr)+1) = (unsigned int)  AddressType_UNDEFINED_WORD; break;
                    default: fatalVMError("unknown size in setType()");
                }
                *ptr = (type_52);

                if (BADSETTYPE && (ea_50) == (Address)BADSETTYPE) {
                    openTraceFile();
                    fprintf(
                            traceFile,
                            format("setType @ %A is %c,  [ea - rom = %A]\n"),
                            (ea_50),
                            getTypeMnemonic((type_52)),
                            Address_diff((ea_50), com_sun_squawk_VM_romStart)
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
INLINE void checkType2(Address ea_56, char recordedType_58, char type_60) {
            char recordedType = (char)((recordedType_58) & AddressType_TYPE_MASK);
            if (recordedType != AddressType_ANY && recordedType != (type_60)) {
                checkTypeError((ea_56), recordedType, (type_60));
            }
        }

        /**
         * Verifies that the type of the value at a given address matches a given type.
         *
         * @param ea   the address to test
         * @param type the type to match
         * @param size the length in bytes of the field
         */
INLINE Address checkType(Address ea_61, char type_62, int size_63) {
            if (isInTypedMemoryRange((ea_61))) {
                /* AddressType_ANY always matches */
                if ((type_62) != AddressType_ANY) {
                    char *a = (char *)(ea_61);
                    char *p = getTypePointer((ea_61));
                    char fillType = ((type_62) == AddressType_BYTECODE) ? AddressType_BYTECODE : AddressType_UNDEFINED;
#ifdef FASTER_SET_TYPES
                    checkType2(a++, *p, (type_62));
#else
                    switch ((size_63)) {
                        case 8: {
                            checkType2(a++, *p++, (type_62));
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
                            checkType2(a++, *p++, (type_62));
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            checkType2(a++, *p++, fillType);
                            break;
                        }
                        case 2: {
                            checkType2(a++, *p++, (type_62));
                            checkType2(a++, *p++, fillType);
                            break;
                        }
                        case 1: {
                            checkType2(a++, *p, (type_62));
                            break;
                        }
                        default: shouldNotReachHere();
                    }
#endif
                }
            }
            return (ea_61);
        }

        /**
         * Gets the type recorded for a given address.
         *
         * @param  the address to test
         */
#define  getType(ea_64) (  \
             *getTypePointer((ea_64))  \
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
#define  checkPostWrite(ea_65, size_67) { Address  ea_66 = ea_65;  int  size_68 = size_67;  \
            checkOneAddress((ea_66), (size_68), (Address)BAD_ADDRESS); \
            cheneyCheck((ea_66)); \
        }

        /**
         * Given a base address and offset to a byte value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value specified by 'oop' and 'offset'
         */
#define  getByteTyped(base_69, offset_70, type_71) (  \
             *((signed char *)checkType(&((signed char *)(base_69))[(offset_70)], (type_71), 1))  \
        )

        /**
         * Given a base address and offset to a byte value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value specified by 'oop' and 'offset'
         */
#define  getUByteTyped(base_72, offset_73, type_74) (  \
             *((unsigned char *)checkType(&((unsigned char *)(base_72))[(offset_73)], (type_74), 1))  \
        )

        /**
         * Given a base address and offset to a byte value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setByteTyped(base_75, offset_77, type_79, value_81) { Address  base_76 = base_75;  Offset  offset_78 = offset_77;  char  type_80 = type_79;  signed char  value_82 = value_81;  \
            signed char *ea = &((signed char *)(base_76))[(offset_78)]; \
            setType(ea, (type_80), 1); \
            *ea = (value_82); \
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
#define  getShortTyped(base_83, offset_84, type_85) (  \
             *((short *)checkType(&((short *)(base_83))[(offset_84)], (type_85), 2))  \
        )

        /**
         * Given a base address and offset to a short value, returns the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param type   the expected type of the value about to be read from the effective address
         * @return       the value
         */
#define  getUShortTyped(base_86, offset_87, type_88) (  \
             *((unsigned short *)checkType(&((unsigned short *)(base_86))[(offset_87)], (type_88), 2))  \
        )

        /**
         * Given a base address and offset to a short value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setShortTyped(base_89, offset_91, type_93, value_95) { Address  base_90 = base_89;  Offset  offset_92 = offset_91;  char  type_94 = type_93;  short  value_96 = value_95;  \
            short *ea = &((short *)(base_90))[(offset_92)]; \
            setType(ea, (type_94), 2); \
            *ea = (value_96); \
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
#define  getIntTyped(base_97, offset_98, type_99) (  \
             *((int *)checkType(&((int *)(base_97))[(offset_98)], (type_99), 4))  \
        )

        /**
         * Given a base address and offset to an integer value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setIntTyped(base_100, offset_102, type_104, value_106) { Address  base_101 = base_100;  Offset  offset_103 = offset_102;  char  type_105 = type_104;  int  value_107 = value_106;  \
            int *ea = &((int *)(base_101))[(offset_103)]; \
            setType(ea, (type_105), 4); \
            *ea = (value_107); \
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
#define  getLongAtWordTyped(base_108, offset_109, type_110) (  \
             *((jlong *)checkType(&((UWordAddress)(base_108))[(offset_109)], (type_110), 8))  \
        )

        /**
         * Given a base address and offset to a 64 bit value, sets the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setLongAtWordTyped(base_111, offset_113, type_115, value_117) { Address  base_112 = base_111;  Offset  offset_114 = offset_113;  char  type_116 = type_115;  jlong  value_118 = value_117;  \
            jlong *ea = (jlong *)&((UWordAddress)(base_112))[(offset_114)]; \
            setType(ea, (type_116), 8); \
            *ea = (value_118); \
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
#define  getLongTyped(base_119, offset_120, type_121) (  \
             *((jlong *)checkType(&((jlong *)(base_119))[(offset_120)], (type_121), 8))  \
        )

        /**
         * Given a base address and offset to a 64 bit value, set the corresponding value.
         *
         * @param base   the base address
         * @param offset the offset (in 64 bit words) from 'base' at which to write
         * @param type   the type of the value about to be written to the effective address
         * @param value  the value
         */
#define  setLongTyped(base_122, offset_124, type_126, value_128) { Address  base_123 = base_122;  Offset  offset_125 = offset_124;  char  type_127 = type_126;  jlong  value_129 = value_128;  \
            jlong *ea = (jlong *)&((jlong *)(base_123))[(offset_125)]; \
            setType(ea, (type_127), 8); \
            *ea = (value_129); \
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
#define  getUWordTyped(base_130, offset_131, type_132) (  \
             (UWord)getLongTyped((base_130), (offset_131), (type_132))  \
        )
#else
#define  getUWordTyped(base_133, offset_134, type_135) (  \
             (UWord)getIntTyped((base_133), (offset_134), (type_135))  \
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
#define  setUWordTyped(base_136, offset_138, type_140, value_142) { Address  base_137 = base_136;  Offset  offset_139 = offset_138;  char  type_141 = type_140;  UWord  value_143 = value_142;  \
            setLongTyped((base_137), (offset_139), (type_141), (UWord)(value_143)); \
        }
#else
#define  setUWordTyped(base_144, offset_146, type_148, value_150) { Address  base_145 = base_144;  Offset  offset_147 = offset_146;  char  type_149 = type_148;  UWord  value_151 = value_150;  \
            setIntTyped((base_145), (offset_147), (type_149), (UWord)(value_151)); \
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
#define  setByte(base_152, offset_154, value_156) { Address  base_153 = base_152;  Offset  offset_155 = offset_154;  int  value_157 = value_156;  \
            setByteTyped((base_153), (offset_155), AddressType_BYTE, (signed char)(value_157)); \
            setAssume(((value_157) & 0xFF) == (getByte((base_153), (offset_155)) & 0xFF)); \
        }

        /**
         * Sets a 16 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' at which to write
         * @param value  the value to write
         */
#define  setShort(base_158, offset_160, value_162) { Address  base_159 = base_158;  Offset  offset_161 = offset_160;  int  value_163 = value_162;  \
            setShortTyped((base_159), (offset_161), AddressType_SHORT, (short)(value_163)); \
            setAssume(((value_163) & 0xFFFF) == getUShort((base_159), (offset_161))); \
        }

        /**
         * Sets a 32 bit value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' at which to write
         * @param value  the value to write
         */
#define  setInt(base_164, offset_166, value_168) { Address  base_165 = base_164;  Offset  offset_167 = offset_166;  int  value_169 = value_168;  \
            setIntTyped((base_165), (offset_167), AddressType_INT, (value_169)); \
            setAssume((value_169) == getInt((base_165), (offset_167))); \
        }

        /**
         * Sets a UWord value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from oop at which to write
         * @param value  the value to write
         */
#define  setUWord(base_170, offset_172, value_174) { Address  base_171 = base_170;  Offset  offset_173 = offset_172;  UWord  value_175 = value_174;  \
            setUWordTyped((base_171), (offset_173), AddressType_UWORD, (value_175)); \
            setAssume((value_175) == getUWord((base_171), (offset_173))); \
        }

        /**
         * Sets a pointer value in memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from oop at which to write
         * @param value  the value to write
         */
#define  setObject(base_176, offset_178, value_180) { Address  base_177 = base_176;  Offset  offset_179 = offset_178;  Address  value_181 = value_180;  \
            setUWordTyped((base_177), (offset_179), AddressType_REF, (UWord)(value_181)); \
            setAssume((value_181) == getObject((base_177), (offset_179))); \
        }

        /**
         * Sets a pointer value in memory and updates write barrier bit for the pointer if
         * a write barrier is being maintained.
         *
         * @param base   the base address
         * @param offset the offset to a field in the object
         */
#define  setObjectAndUpdateWriteBarrier(base_182, offset_184, value_186) { Address  base_183 = base_182;  Offset  offset_185 = offset_184;  Address  value_187 = value_186;  \
            setObject((base_183), (offset_185), (value_187)); \
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
#define  setLong(base_188, offset_190, value_192) { Address  base_189 = base_188;  Offset  offset_191 = offset_190;  jlong  value_193 = value_192;  \
            setLongTyped((base_189), (offset_191), AddressType_LONG, (value_193)); \
            setAssume((value_193) == getLong((base_189), (offset_191))); \
        }

        /**
         * Sets a 64 bit value in memory at a UWord offset.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' at which to write
         * @param value  the value to write
         */
#define  setLongAtWord(base_194, offset_196, value_198) { Address  base_195 = base_194;  Offset  offset_197 = offset_196;  jlong  value_199 = value_198;  \
            if (SQUAWK_64 || PLATFORM_UNALIGNED_64_LOADS) { \
                setLongAtWordTyped((base_195), (offset_197), AddressType_LONG, (value_199)); \
            } else { \
                const int highOffset = (PLATFORM_BIG_ENDIAN) ? (offset_197)     : (offset_197) + 1; \
                const int lowOffset  = (PLATFORM_BIG_ENDIAN) ? (offset_197) + 1 : (offset_197); \
                setIntTyped((base_195), highOffset, AddressType_LONG,  (int)((value_199) >> 32)); \
                setIntTyped((base_195), lowOffset,  AddressType_LONG2, (int) (value_199)); \
            } \
            setAssume((value_199) == getLongAtWord((base_195), (offset_197))); \
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
#define  getByte(base_200, offset_201) (  \
             getByteTyped((base_200), (offset_201), AddressType_BYTE)  \
        )

        /**
         * Gets an unsigned 8 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in bytes) from 'base' from which to load
         * @return the value
         */
#define  getUByte(base_202, offset_203) (  \
             getUByteTyped((base_202), (offset_203), AddressType_BYTE)  \
        )

        /**
         * Gets a signed 16 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' from which to load
         * @return the value
         */
#define  getShort(base_204, offset_205) (  \
             getShortTyped((base_204), (offset_205), AddressType_SHORT)  \
        )

        /**
         * Gets an unsigned 16 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in 16 bit words) from 'base' from which to load
         * @return the value
         */
#define  getUShort(base_206, offset_207) (  \
             getUShortTyped((base_206), (offset_207), AddressType_SHORT)  \
        )

        /**
         * Gets a signed 32 bit value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in 32 bit words) from 'base' from which to load
         * @return the value
         */
#define  getInt(base_208, offset_209) (  \
             getIntTyped((base_208), (offset_209), AddressType_INT)  \
        )

        /**
         * Gets a UWord value from memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' from which to load
         * @return the value
         */
#define  getUWord(base_210, offset_211) (  \
             getUWordTyped((base_210), (offset_211), AddressType_UWORD)  \
        )

        /**
         * Gets a pointer from memory.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' from which to load
         * @return the value
         */
#define  getObject(base_212, offset_213) (  \
             (Address)getUWordTyped((base_212), (offset_213), AddressType_REF)  \
        )

        /**
         * Gets a 64 bit value from memory using a 64 bit word offset.
         *
         * @param base   the base address
         * @param offset the offset (in 64 bit words) from 'base' from which to load
         * @return the value
         */
#define  getLong(base_214, offset_215) (  \
             getLongTyped((base_214), (offset_215), AddressType_LONG)  \
        )

        /**
         * Gets a 64 bit value from memory using a UWord offset.
         *
         * @param base   the base address
         * @param offset the offset (in UWords) from 'base' from which to load
         * @return the value
         */
INLINE jlong getLongAtWord(Address base_216, Offset offset_217) {
            if (SQUAWK_64 || PLATFORM_UNALIGNED_64_LOADS) {
                return getLongAtWordTyped((base_216), (offset_217), AddressType_LONG);
            } else {
                const int highOffset = (PLATFORM_BIG_ENDIAN) ? (offset_217)     : (offset_217) + 1;
                const int lowOffset  = (PLATFORM_BIG_ENDIAN) ? (offset_217) + 1 : (offset_217);
                const unsigned int high = getIntTyped((base_216), highOffset, AddressType_LONG);
                const unsigned int low  = getIntTyped((base_216), lowOffset,  AddressType_LONG2);

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
#define  swap2(address_218) { Address  address_219 = address_218;  \
            char type = (TYPEMAP ? getType((address_219)) : AddressType_UNDEFINED); \
            if (!(PLATFORM_UNALIGNED_LOADS || isAligned((UWord)(address_219), 2))) { \
                int b0 = getUByteTyped((address_219), 0, AddressType_ANY); \
                int b1 = getUByteTyped((address_219), 1, AddressType_ANY); \
                setByteTyped((address_219), 0, AddressType_ANY, (char)b1); \
                setByteTyped((address_219), 1, AddressType_ANY, (char)b0); \
            } else { \
                int value = getUShortTyped((address_219), 0, AddressType_ANY); \
                int b0 =  value       & 0xFF; \
                int b1 = (value >> 8) & 0xFF; \
                value = (b0 << 8) | b1; \
                setShortTyped((address_219), 0, type, (short)value); \
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
#define  swap(address_220, dataSize_222) { Address  address_221 = address_220;  int  dataSize_223 = dataSize_222;  \
            /*fprintf(stderr, format("swap(%A, %d)\n"), (address_221), (dataSize_223));*/ \
            switch ((dataSize_223)) { \
                case 1:               break; \
                case 2: swap2((address_221)); break; \
                case 4: swap4((address_221)); break; \
                case 8: swap8((address_221)); break; \
                default: \
                    fprintf(stderr, "dataSize=%d\n", (dataSize_223)); \
                    shouldNotReachHere(); \
            } \
        }

        /**
         * Swaps the endianess of a word sized value.
         *
         * @param address   the address of the value
         */
#define  swapWord(address_224) { Address  address_225 = address_224;  \
            swap((address_225), HDR_BYTES_PER_WORD); \
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
#define  zeroWords(start_226, end_228) { UWordAddress  start_227 = start_226;  UWordAddress  end_229 = end_228;  \
            assume(isWordAligned((UWord)(start_227))); \
            assume(isWordAligned((UWord)(end_229))); \
            zeroTypes((start_227), (end_229)); \
            while ((start_227) < (end_229)) { \
                *(start_227) = 0; \
                (start_227)++; \
            } \
        }

#define  traceAllocation(oop_230, size_232) { Address  oop_231 = oop_230;  int  size_233 = size_232;  \
            fprintf(stderr, "%s allocating object: size=%d, alloc free=%d, total free=%d, ptr=%d\n", \
                    (((oop_231) != 0) ? "succeeded" : "failed"), \
                    (size_233), \
                    Address_diff(com_sun_squawk_GC_allocEnd, com_sun_squawk_GC_allocTop), \
                    Address_diff(com_sun_squawk_GC_heapEnd, com_sun_squawk_GC_allocTop), \
                    (Offset)(oop_231)); \
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
INLINE Address allocateFast(int size_234, Address klass_235, int arrayLength_236) {
            if (
                com_sun_squawk_GC_excessiveGC != false       ||
                com_sun_squawk_GC_allocationEnabled == false ||
                (com_sun_squawk_GC_GC_TRACING_SUPPORTED && (com_sun_squawk_GC_traceFlags & com_sun_squawk_GC_TRACE_ALLOCATION) != 0)
               ) {
                return null; /* Force call to Java code */
            }
            return allocate((size_234), (klass_235), (arrayLength_236));
        }

        /**
         * Static version of {@link #getDataSize()} so that garbage collector can
         * invoke this method on a possibly forwarded Klass object.
         */
INLINE int getDataSize(Address klass_237) {
            switch (com_sun_squawk_Klass_id((klass_237))) {
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
