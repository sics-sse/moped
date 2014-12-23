/*
 * Copyright 1994-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.compiler;

/**
 * Type representation and types for the <code>Compiler</code>.
 *
 */
public class Type {

    /**
     * Size in words for this type in an activation record.
     */
    private final int asize;

    /**
     * Size in bytes for this type in an object or array.
     */
    private final int osize;

    /**
     * The type code the form the bottom 4 bits of the IR opcodes.
     */
    private final int code;

    /**
     * The primitive version of the type.
     */
    private final Type primitive;

    /**
     * The name for debugging.
     */
    private final String name;

    /**
     * Constructor.
     *
     * @param asize the size in words for this type in an activation record
     * @param osize the size in bytes for this type in an object or array
     * @param code the type code the form the bottom 4 bits of the IR opcodes
     * @param name the name if the type
     */
    public Type(int asize, int osize, int code, String name) {
        this.asize     = asize;
        this.osize     = osize;
        this.code      = code;
        this.primitive = this;
        this.name      = name;
    }

    /**
     * Constructor.
     *
     * @param asize the size in words for this type in an activation record
     * @param osize the size in bytes for this type in an object or array
     * @param code the type code the form the bottom 4 bits of the IR opcodes
     * @param primitive type for this type
     * @param name the name if the type
     */
    public Type(int asize, int osize, int code, Type primitive, String name) {
        this.asize     = asize;
        this.osize     = osize;
        this.code      = code;
        this.primitive = primitive;
        this.name      = name;
    }

    /**
     * Get the activation record size.
     *
     * @return the size in words for this type in an activation record
     */
    public int getActivationSize() {
        return asize;
    }

    /**
     * Get the structure size.
     *
     * @return the size in bytes for this type in an object or array
     */
    public int getStructureSize() {
        return osize;
    }

    /**
     * Get the opcode code.
     *
     * @return code the type code the form the bottom 4 bits of the IR opcodes
     */
    public int getTypeCode() {
        return code;
    }

    /**
     * Get the primitive version of the type.
     *
     * @return the primitive the type for this type
     */
    public Type getPrimitiveType() {
        return primitive;
    }

    /**
     * Test the type to see if it is a pointer
     *
     * @return true if it is
     */
    public boolean isPointer() {
        return code == Code_R || code == Code_O;
    }

    /**
     * Test the type to see if it is an oop
     *
     * @return true if it is
     */
    public boolean isOop() {
        return code == Code_O;
    }

    /**
     * Test the type to see if it is a reference
     *
     * @return true if it is
     */
    public boolean isRef() {
        return code == Code_R;
    }

    /**
     * Test the type to see if it is a primary type.
     *
     * @return true if it is a primary type, false otherwise.
     */
    public boolean isPrimary() {
        if (this == REF || this == OOP || this == INT || this == UINT ||
            this == LONG || this == ULONG || this == FLOAT || this == DOUBLE)
            return true;
        return false;
    }

    /**
     * Test the type to see if it is a secondary type.
     *
     * @return true if it is a secondary type, false otherwise.
     */
    public boolean isSecondary() {
        if (this == BYTE || this == UBYTE || this == SHORT || this == USHORT)
            return true;
        return false;
    }

    /**
     * Test the type to see if it is a signed type.
     *
     * @return true if it is a signed type, false otherwise.
     */
    public boolean isSigned() {
        if (this == INT || this == LONG || this == FLOAT || this == DOUBLE ||
            this == BYTE || this == SHORT) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the name of the type
     */
    public String toString() {
        return name;
    }

    /**
     * Prints to stderr information about this type.
     * This method is used for debuggging purposes.
     */
    public void print() {
        System.err.print(name);
    }

    /*-----------------------------------------------------------------------*\
     *                             Sentinel types                            *
    \*-----------------------------------------------------------------------*/

    /**
     * Opcode subtype codes.
     */
    public static final int Code_V = 1,
                            Code_R = 2,
                            Code_O = 3,
                            Code_I = 4,
                            Code_U = 5,
                            Code_L = 6,
                            Code_F = 7,
                            Code_D = 8,
                            Code_B = 9,
                            Code_A = 10,
                            Code_S = 11,
                            Code_C = 12,
                            Code_G = 13;


    /**
     * Define primary types.
     */
    public static final Type REF     = new Type(1, 4, Code_R, "REF"),         // A 16/32/64 bit address (depending on the system)
                             OOP     = new Type(1, 4, Code_O, "OOP"),         // A reference to a Java object
                             INT     = new Type(1, 4, Code_I, "INT"),         // 32 bit signed integer
                             UINT    = new Type(1, 4, Code_U, "UINT"),        // 32 bit unsigned integer
                             LONG    = new Type(2, 8, Code_L, "LONG"),        // 64 bit signed integer
                             ULONG   = new Type(2, 8, Code_G, "ULONG"),       // 64 bit unsigned integer (for 64 bit VMs only)
                             FLOAT   = new Type(1, 4, Code_F, "FLOAT"),       // 32 bit floating point number
                             DOUBLE  = new Type(2, 8, Code_D, "DOUBLE");      // 64 bit floating point number

    /**
     * Define secondary types.
     */
    public static final Type BYTE    = new Type(0, 1, Code_B, INT, "BYTE"),   // 8 bit signed integer
                             UBYTE   = new Type(0, 1, Code_A, INT, "UBYTE"),  // 8 bit unsigned integer
                             SHORT   = new Type(0, 2, Code_S, INT, "SHORT"),  // 16 bit signed integer
                             USHORT  = new Type(0, 2, Code_C, INT, "USHORT"); // 16 bit unsigned integer


    /**
     * Special dummy type for call and return.
     */
    public static final Type VOID    = new Type(0, 0, Code_V, "VOID");          // Pseudo type

    /**
     * Special dummy types for specifing the MP, IP and LP variables.
     */
    public static final Type MP      = new Type(1, 4, Code_O, OOP, "MP"),       // Pseudo types
                             IP      = new Type(1, 4, Code_R, REF, "IP"),
                             LP      = new Type(1, 4, Code_R, REF, "LP"),
                             SS      = new Type(1, 4, Code_R, REF, "SS");

    /**
     * Define the size of stack entries.
     */
    public static final Type WORD    = INT  /*s64*/,
                             UWORD   = UINT /*s64*/;

    /**
     * Relocation type for absolute int addresses.
     */
    public static final int RELOC_ABSOLUTE_INT = 0;

    /**
     * Relocation type for relative int addresses.
     */
    public static final int RELOC_RELATIVE_INT = 1;


}
