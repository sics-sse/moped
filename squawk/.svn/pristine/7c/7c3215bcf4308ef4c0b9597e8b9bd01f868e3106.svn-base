/*
 * Copyright 2003-2008 Sun Microsystems, Inc. All Rights Reserved.
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
 * Data types supported by the <code>Compiler</code> interface.
 *
 */
public interface Types {

    /**
     * Define primary types.
     */
    public static final Type REF     = Type.REF,        // A 16/32/64 bit address (depending on the system)
                             OOP     = Type.OOP,        // A reference to a Java object
                             INT     = Type.INT,        // 32 bit signed integer
                             UINT    = Type.UINT,       // 32 bit unsigned integer
                             LONG    = Type.LONG,       // 64 bit signed integer
                             ULONG   = Type.ULONG,      // 64 bit unsigned integer
                             FLOAT   = Type.FLOAT,      // 32 bit floating point number
                             DOUBLE  = Type.DOUBLE;     // 64 bit floating point number

    /**
     * Define secondary types.
     */
    public static final Type BYTE    = Type.BYTE,       // 8 bit signed integer
                             UBYTE   = Type.UBYTE,      // 8 bit unsigned integer
                             SHORT   = Type.SHORT,      // 16 bit signed integer
                             USHORT  = Type.USHORT;     // 16 bit unsigned integer


    /**
     * Special dummy type for call and return.
     */
    public static final Type VOID    = Type.VOID;       // Pseudo type

    /**
     * Special dummy types for supporting building of the <code>Interpreter</code>.
     */
    public static final Type MP      = Type.MP,         // Pseudo types
                             IP      = Type.IP,
                             LP      = Type.LP,
                             SS      = Type.SS;

    /**
     * Define the size of stack entries.
     */
    public static final Type WORD    = Type.WORD,
                             UWORD   = Type.UWORD;

    /**
     * Relocation type for absolute integer addresses.
     */
    public static final int RELOC_ABSOLUTE_INT = Type.RELOC_ABSOLUTE_INT;

    /**
     * Relocation type for relative integer addresses.
     */
    public static final int RELOC_RELATIVE_INT = Type.RELOC_RELATIVE_INT;

}
