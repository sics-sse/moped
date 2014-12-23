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

package com.sun.squawk.vm;

/**
 * This class enumerates the identifiers for the special system classes that the
 * Squawk VM must be able to identify without necessary having a reference to a
 * Klass object.
 */
public interface CID {

    public final static int
        NULL                 = 0,
        OBJECT               = 1,
        STRING               = 2,
        THROWABLE            = 3,
        KLASS                = 4,
        VOID                 = 5,
        BOOLEAN              = 6,
        BYTE                 = 7,
        CHAR                 = 8,
        SHORT                = 9,
        INT                  = 10,
        LONG                 = 11,
        LONG2                = 12,
        FLOAT                = 13,
        DOUBLE               = 14,
        DOUBLE2              = 15,
        OBJECT_ARRAY         = 16,
        STRING_ARRAY         = 17,
        BOOLEAN_ARRAY        = 18,
        BYTE_ARRAY           = 19,
        CHAR_ARRAY           = 20,
        SHORT_ARRAY          = 21,
        INT_ARRAY            = 22,
        LONG_ARRAY           = 23,
        FLOAT_ARRAY          = 24,
        DOUBLE_ARRAY         = 25,
        STRING_OF_BYTES      = 26,  /* Small strings.                       */
        LOCAL                = 27,  /* Slot in stack chunk structure.       */
        GLOBAL               = 28,  /* Slot in class state structure.       */
        LOCAL_ARRAY          = 29,  /* Stack chunk structure.               */
        GLOBAL_ARRAY         = 30,  /* Class state structure.               */
        GLOBAL_ARRAYARRAY    = 31,  /* Table of class state structures.     */
        BYTECODE             = 32,  /* A bytecode.                          */
        BYTECODE_ARRAY       = 33,  /* An array of bytes that is a method.  */
        ADDRESS              = 34,  /* Abstraction over machine addresses   */
        ADDRESS_ARRAY        = 35,  /* Abstraction over machine addresses   */
        UWORD                = 36,  /* Abstraction over machine words       */
        UWORD_ARRAY          = 37,  /* Abstraction over machine words       */
        OFFSET               = 38,  /* Abstraction over directed address offsets */
        NATIVEUNSAFE         = 39,  /* Peek/poke methods                    */
        TOP                  = 40,
        ONE_WORD             = 41,  // only used by translator
        TWO_WORD             = 42,  // only used by translator
        REFERENCE            = 43,  // only used by translator
        UNINITIALIZED        = 44,  // only used by translator
        UNINITIALIZED_THIS   = 45,  // only used by translator
        UNINITIALIZED_NEW    = 46,  // only used by translator

        LAST_SYSTEM_ID        = UNINITIALIZED_NEW;
}
