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

package com.sun.squawk;

import com.sun.squawk.pragma.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;


/**
 * The offset type is used by the runtime system and collector to denote
 * the directed distance between two machine addresses. It is used instead
 * of 'int' or 'Object' for coding clarity, machine-portability (it can map
 * to 32 bit and 64 bit integral types) and access to unsigned operations
 * (Java does not have unsigned int types).
 * <p>
 * This class is known specially by the translator as a <code>SQUAWKPRIMITIVE</code>
 * and programming with it requires adhering to the restrictions implied by this
 * attribute. Some of these constraints are imposed to keep the job of the
 * translator simple. All of these constraints are currently enforced by the
 * translator. The constraints are:
 * <ul>
 *   <li>
 *       A local variable slot allocated by javac for a Squawk primitive variable
 *       must never be used for a value of any other type (including a different
 *       Squawk primitive type). This is required as the translator cannot currently
 *       de-multiplex reference type slots into disjoint typed slots. This restriction
 *       on javac is achieved by declaring all Squawk primitive local variables at
 *       the outer most scope (as javac using lexical based scoping for register
 *       allocation liveness).
 *   </li>
 *   <li>
 *       A Squawk primitive value of type T cannot be assigned to or compared with
 *       values of any other type (including <code>null</code>) than T.
 *   </li>
 *   <li>
 *       A Squawk primitive value of type T cannot be passed as a parameter
 *       values of any other type than T. For example, you cannot
 *       call T.toString(), or String.valueOf(T). The methods of the classes NativeUnsafe and GC
 *       have a special permission to allow Squawk primitive values to passed in place of 
 *       parameters of type Object.
 *   </li>
 * </ul>
 *
 * <p>
 * Only the public methods of this class which do not override any of the
 * methods in java.lang.Object will be available in a {@link VM#isHosted() non-hosted}
 * environment. The translator replaces any calls to these methods to native
 * method calls.
 * <p>
 * This mechanism was largely inspired by the VM_Address class in the Jikes RVM.
 *
 */

public final class Offset {

    /**
     * Casts an offset expressed as the appropriate Java primitive type for the platform (i.e. int or long)
     * into a value of type Offset.
     *
     * @param  value   an offset expressed as an int or long
     * @return the canonical Offset instance for <code>offset</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return (Offset)value;")
/*end[JAVA5SYNTAX]*/
    public static Offset fromPrimitive(int/*S64*/ value) throws NativePragma {
        return get(value);
    }

    /**
     * Casts a value of type Offset into the appropriate Java primitive type for the platform (i.e. int or long).
     * This will cause a fatal error if this cast cannot occur without changing this offset's sign or
     * truncating its magnitude.
     *
     * @return this Offset value as an int or long
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this;")
/*end[JAVA5SYNTAX]*/
    public int/*S64*/ toPrimitive() throws NativePragma {
        return value;
    }

    /**
     * Casts a value of type Offset into an int. This will cause a fatal error if this offset
     * value cannot be expressed as a signed 32 bit Java int without changing its sign or
     * truncating its magnitude.
     *
     * @return this Offset value as an int
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="assume((int)this == this); return (int)this;")
/*end[JAVA5SYNTAX]*/
    public int toInt() throws NativePragma {
        Assert.that((int)value == value);
        return (int)value;
    }

    /**
     * Casts a value of type Offset into a UWord.
     *
     * @return this Offset value as a UWord
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return (UWord)this;")
/*end[JAVA5SYNTAX]*/
    public UWord toUWord() throws NativePragma {
        return UWord.fromPrimitive(value);
    }

    /**
     * Gets the canonical Offset representation of <code>null</code>.
     *
     * @return the canonical Offset representation of <code>null</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return 0;")
/*end[JAVA5SYNTAX]*/
    public static Offset zero() throws NativePragma {
        return get(0);
    }

    /**
     * Adds a value to this offset and return the resulting offset.
     *
     * @param delta   the signed value to add
     * @return the result of adding <code>delta</code> to this offset
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this + delta;")
/*end[JAVA5SYNTAX]*/
    public Offset add(int delta) throws NativePragma {
        return get(value + delta);
    }

    /**
     * Subtracts a value from this offset and return the resulting offset.
     *
     * @param delta   the signed value to subract
     * @return the result of subtracting <code>delta</code> from this offset
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this - delta;")
/*end[JAVA5SYNTAX]*/
    public Offset sub(int delta) throws NativePragma {
        return get(value - delta);
    }

    /**
     * Scales this offset which currently expresses an offset in words to express
     * the same offset in bytes. That is, the value of this offset is multiplied by
     * the number of bytes in a machine word.
     *
     * @return  the scaled up offset
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this << HDR_LOG2_BYTES_PER_WORD;")
/*end[JAVA5SYNTAX]*/
    public Offset wordsToBytes() throws NativePragma {
        return get(value << HDR.LOG2_BYTES_PER_WORD);
    }

    /**
     * Scales this offset which currently expresses an offset in bytes to express
     * the same offset in words. That is, the value of this offset is divided by
     * the number of bytes in a machine word. This method should only be called for offsets
     * which are guaranteed to be a muliple of the number of bytes in a machine word.
     *
     * @return  the scaled down offset
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this >> HDR_LOG2_BYTES_PER_WORD;")
/*end[JAVA5SYNTAX]*/
    public Offset bytesToWords() throws NativePragma {
        Assert.that((value % HDR.BYTES_PER_WORD) == 0);
        return get(value >> HDR.LOG2_BYTES_PER_WORD);
    }

    /**
     * Determines if this offset is 0.
     *
     * @return true if this offset is 0.
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this == 0;")
/*end[JAVA5SYNTAX]*/
    public boolean isZero() throws NativePragma {
        return this == zero();
    }

    /**
     * Determines if this offset is equal to a given offset.
     *
     * @param offset2   the offset to compare this offset against
     * @return true if this offset is equal to <code>offset2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this == offset2;")
/*end[JAVA5SYNTAX]*/
    public boolean eq(Offset offset2) throws NativePragma {
        return this == offset2;
    }

    /**
     * Determines if this offset is not equal to a given offset.
     *
     * @param offset2   the offset to compare this offset against
     * @return true if this offset is not equal to <code>offset2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this != offset2;")
/*end[JAVA5SYNTAX]*/
    public boolean ne(Offset offset2) throws NativePragma {
        return this != offset2;
    }

    /**
     * Determines if this offset is less than a given offset.
     *
     * @param offset2   the offset to compare this offset against
     * @return true if this offset is less than or equals to <code>offset2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this < offset2;")
/*end[JAVA5SYNTAX]*/
    public boolean lt(Offset offset2) throws NativePragma {
        return this.value < offset2.value;
    }

    /**
     * Determines if this offset is less than or equal to a given offset.
     *
     * @param offset2   the offset to compare this offset against
     * @return true if this offset is less than or equal to <code>offset2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this <= offset2;")
/*end[JAVA5SYNTAX]*/
    public boolean le(Offset offset2) throws NativePragma {
        return (this == offset2) || lt(offset2);
    }

    /**
     * Determines if this offset is greater than a given offset.
     *
     * @param offset2   the offset to compare this offset against
     * @return true if this offset is greater than <code>offset2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this > offset2;")
/*end[JAVA5SYNTAX]*/
    public boolean gt(Offset offset2) throws NativePragma {
        return offset2.lt(this);
    }

    /**
     * Determines if this offset is greater than or equal to a given offset.
     *
     * @param offset2   the offset to compare this offset against
     * @return true if this offset is greater than or equal to <code>offset2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this >= offset2;")
/*end[JAVA5SYNTAX]*/
    public boolean ge(Offset offset2) throws NativePragma {
        return offset2.le(this);
    }

    /*-----------------------------------------------------------------------*\
     *                      Hosted execution support                         *
    \*-----------------------------------------------------------------------*/

    /**
     * Gets a hashcode value for this offset which is just the value itself.
     *
     * @return  the value of this offset
     */
    public int hashCode() throws HostedPragma {
        return (int)value;
    }

    /**
     * Gets a string representation of this offset.
     *
     * @return String
     */
    public String toString() throws HostedPragma {
        return ""+value;
    }

    /**
     * The offset value.
     */
    private final int/*S64*/ value;

    /**
     * Unique instance pool.
     */
    private static /*S64*/IntHashtable pool;

    /**
     * Gets the canonical Offset instance for a given offset.
     *
     * @param  value   the machine offset
     * @return the canonical Offset instance for <code>value</code>
     */
    private static Offset get(int/*S64*/ value) throws HostedPragma {
        if (pool == null) {
            pool = new /*S64*/IntHashtable();
        }
        Offset instance = (Offset)pool.get(value);
        if (instance == null) {
            instance = new Offset(value);
            try {
                pool.put(value, instance);
            } catch (OutOfMemoryError e) {
                throw new OutOfMemoryError("Failed to grow instance pool when adding " + value);
            }
        }
        return instance;
    }

    /**
     * Constructor.
     *
     * @param value  a machine offset
     */
    private Offset(int/*S64*/ value) throws HostedPragma {
        this.value = value;
    }
}
