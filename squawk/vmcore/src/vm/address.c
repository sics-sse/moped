/**** Created by Squawk builder from "vmcore/src/vm/address.c.spp.preprocessed" ****/ /*
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

/*
 * This file define types and functions to abstract the size of machine words and addresses.
 * This eases portability between 32 and 64 bit platforms.
 */

typedef void*          Address;
typedef unsigned char* ByteAddress;
#if SQUAWK_64
typedef ujlong UWord;
typedef jlong Offset;
const Address ADDRESS_MAX = (Address)0xFFFFFFFFFFFFFFFFL;
const UWord WORD_MAX = (UWord)0xFFFFFFFFFFFFFFFFL;
const UWord DEADBEEF = 0xDEADBEEFDEADBEEFL;
#else
typedef unsigned int UWord;
typedef int Offset;
const Address ADDRESS_MAX = (Address)0xFFFFFFFF;
const UWord WORD_MAX = (UWord)0xFFFFFFFF;
const UWord DEADBEEF = 0xDEADBEEF;
#endif /* SQUAWK_64 */
typedef UWord* UWordAddress;

        /**
         * Unsigned comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is lower than 'right' in an unsigned comparison
         */
#define  lo(left_0, right_1) (  \
             (UWord)(left_0) < (UWord)(right_1)  \
        )

        /**
         * Unsigned comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is lower than or equal to 'right' in an unsigned comparison
         */
#define  loeq(left_2, right_3) (  \
             (UWord)(left_2) <= (UWord)(right_3)  \
        )

        /**
         * Unsigned comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is higher than 'right' in an unsigned comparison
         */
#define  hi(left_4, right_5) (  \
             (UWord)(left_4) > (UWord)(right_5)  \
        )

        /**
         * Unsigned comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is higher than or equal to 'right' in an unsigned comparison
         */
#define  hieq(left_6, right_7) (  \
             (UWord)(left_6) >= (UWord)(right_7)  \
        )

        /**
         * Signed comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is lower than 'right' in a signed comparison
         */
#define  lt(left_8, right_9) (  \
             (Offset)(left_8) < (Offset)(right_9)  \
        )

        /**
         * Signed comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is lower than or equal to 'right' in a signed comparison
         */
#define  le(left_10, right_11) (  \
             (Offset)(left_10) <= (Offset)(right_11)  \
        )

        /**
         * Signed comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is higher than 'right' in a signed comparison
         */
#define  gt(left_12, right_13) (  \
             (Offset)(left_12) > (Offset)(right_13)  \
        )

        /**
         * Signed comparison between two machine word sized values.
         *
         * @param left  the first value
         * @param right the second value
         * @return true if 'left' is higher than or equal to 'right' in a signed comparison
         */
#define  ge(left_14, right_15) (  \
             (Offset)(left_14) >= (Offset)(right_15)  \
        )


        /**
         * Adds a machine word sized signed offset to an address.
         *
         * @param address  the base address
         * @param offset   the signed offset (in bytes) to add
         * @return the result of adding 'offset' to 'address'
         */
#define  Address_add(address_16, offset_17) (  \
             (Address)((UWord)(address_16) + (Offset)(offset_17))  \
        )

        /**
         * Subtracts a machine word sized signed offset to an address.
         *
         * @param address  the base address
         * @param offset   the signed offset (in bytes) to subtract
         * @return the result of subtracting 'offset' from 'address'
         */
#define  Address_sub(address_18, offset_19) (  \
             (Address)((UWord)(address_18) - (Offset)(offset_19))  \
        )

        /**
         * Computes the signed distance between two addresses.
         *
         * @param address1  the first address
         * @param address2  the second address
         * @return the signed distance (in bytes) between 'address1' and 'address2'
         */
#define  Address_diff(address1_20, address2_21) (  \
             (Offset)(address1_20) - (Offset)(address2_21)  \
        )
