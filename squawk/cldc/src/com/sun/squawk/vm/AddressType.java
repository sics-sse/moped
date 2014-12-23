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

package com.sun.squawk.vm;

/**
 * This class defines the constants that can be used to describe the type of the
 * value stored at a given address. This type information is used when the VM is
 * built with runtime type checking enabled.
 */
public final class AddressType {
    
    private AddressType() {} // not instantiable

    /**
     * Denotes that an address that has never been written to or zeroed.
     */
    public final static byte UNDEFINED = 0x00;

    /**
     * Denotes that an address that has been zeroed and thus can be safely read as the default value for any type.
     */
    public final static byte ANY = 0x01;

    /**
     * Denotes that an address contains a bytecode instruction. The high 4 bits of the type entry describe the
     * type of the value that the instruction write to memory (if any).
     */
    public final static byte BYTECODE = 0x02;

    /**
     * Denotes that an address contains a byte value.
     */
    public final static byte BYTE = 0x03;

    /**
     * Denotes that an address contains a short value.
     */
    public final static byte SHORT = 0x04;

    /**
     * Denotes that an address contains an int value.
     */
    public final static byte INT = 0x05;

    /**
     * Denotes that an address contains a float value.
     */
    public final static byte FLOAT = 0x06;

    /**
     * Denotes that an address contains the first word of a long value.
     */
    public final static byte LONG = 0x07;

    /**
     * Denotes that an address contains the second word of a long value.
     */
    public final static byte LONG2 = 0x08;

    /**
     * Denotes that an address contains the first word of a double value.
     */
    public final static byte DOUBLE = 0x09;

    /**
     * Denotes that an address contains the second word of a double value.
     */
    public final static byte DOUBLE2 = 0x0A;

    /**
     * Denotes that an address contains a reference value.
     */
    public final static byte REF = 0x0B;

    /**
     * Denotes that an address contains a word (or offset) value.
     */
    public final static byte UWORD = 0x0C;

    /**
     * The mask applied to an address type value to extract the type information without
     * the mutation type information.
     */
    public final static int TYPE_MASK = 0x0F;

    /**
     * The amount by which an address type value must be right shifted to extract the
     * encoded mutation type.
     */
    public final static int MUTATION_TYPE_SHIFT = 4;

    /**
     * A constant denoting a word full of {@link #UNDEFINED}s.
     */
/*if[SQUAWK_64]*/
    public final static long UNDEFINED_WORD = 0x0000000000000000L;
/*else[SQUAWK_64]*/
//  public final static long UNDEFINED_WORD = 0x00000000;
/*end[SQUAWK_64]*/

    /**
     * A constant denoting a word full of {@link #ANY}s.
     */
/*if[SQUAWK_64]*/
    public final static long ANY_WORD = 0x0101010101010101L;
/*else[SQUAWK_64]*/
//  public final static int ANY_WORD = 0x01010101;
/*end[SQUAWK_64]*/

    /**
     * A char array (encoded in a string) provided a char representation for each
     * valid address type constant.
     */
    public final static String Mnemonics = "-ZbBSIFLlDdRU";

    /**
     * Gets the mnemonic for an address type value.
     *
     * @param type   the value to get a mnemonic representation for
     * @return the representation of 'type'
     */
    public static String getMnemonic(byte type) {
        String s = "" + Mnemonics.charAt(type & TYPE_MASK);
        if ((type & TYPE_MASK) == BYTECODE) {
            int resultType = (type >> MUTATION_TYPE_SHIFT) & TYPE_MASK;
            s += ":" + Mnemonics.charAt(resultType);
        }
        return s;
    }
}
