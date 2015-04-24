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

/**
 * The word type is used by the runtime system and collector to denote machine
 * word-sized quantities. It is used instead of 'int' or 'Object' for coding
 * clarity, machine-portability (it can map to 32 bit and 64 bit integral types)
 * and access to unsigned operations (Java does not have unsigned int types).
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
 * This mechanism was largely inspired by the VM_Word class in the Jikes RVM.
 *
 */
public final class UWord {

    /**
     * Casts a word expressed as the appropriate Java primitive type for the platform (i.e. int or long)
     * into a value of type UWord.
     *
     * @param  value   a word expressed as an int or long
     * @return the canonical UWord instance for <code>value</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return (UWord)value;")
/*end[JAVA5SYNTAX]*/
    public static UWord fromPrimitive(int/*S64*/ value) throws NativePragma {
        return get(value);
    }

    /**
     * Casts a value of type UWord into the appropriate Java primitive type for the platform (i.e. int or long).
     * This will cause a fatal error if this cast cannot occur without changing this uword's sign or
     * truncating its magnitude.
     *
     * @return this UWord value as an int or long
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return (Offset)this;")
/*end[JAVA5SYNTAX]*/
    public int/*S64*/ toPrimitive() throws NativePragma {
        return value;
    }

    /**
     * Casts a value of type UWord into an int. This will cause a fatal error if this UWord
     * value cannot be expressed as a signed 32 bit Java int without changing its sign or
     * truncating its magnitude.
     *
     * @return this UWord value as an int
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="assume((int)this == this); return (int)this;")
/*end[JAVA5SYNTAX]*/
    public int toInt() throws NativePragma {
        Assert.that((int)value == value);
        return (int)value;
    }

    /**
     * Casts a value of type UWord into an Offset. This may cause a change in sign if this word value cannot be expressed
     * as a signed quantity.
     *
     * @return this UWord value as an Offset
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return (Offset)this;")
/*end[JAVA5SYNTAX]*/
    public Offset toOffset() throws NativePragma {
        return Offset.fromPrimitive(value);
    }

    /**
     * Gets the canonical UWord representation of <code>null</code>.
     *
     * @return the canonical UWord representation of <code>null</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return 0;")
/*end[JAVA5SYNTAX]*/
    public static UWord zero() throws NativePragma {
        return get(0);
    }

    /**
     * Gets the largest possible machine word.
     *
     * @return  the largest possible machine word
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return (UWord)-1;")
/*end[JAVA5SYNTAX]*/
    public static UWord max() throws NativePragma {
        return get(-1);
    }

    /**
     * Logically OR a word with this word.
     *
     * @param word   the word to OR this word with
     * @return       the result of the OR operation
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this | word;")
/*end[JAVA5SYNTAX]*/
    public UWord or(UWord word) throws NativePragma {
        return get(this.value | word.value);
    }

    /**
     * Logically AND a word with this word.
     *
     * @param word   the word to AND this word with
     * @return       the result of the AND operation
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this & word;")
/*end[JAVA5SYNTAX]*/
    public UWord and(UWord word) throws NativePragma {
        return get(this.value & word.value);
    }

    /**
     * Determines if this word is 0.
     *
     * @return true if this word is 0.
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this == 0;")
/*end[JAVA5SYNTAX]*/
    public boolean isZero() throws NativePragma {
        return this == zero();
    }

    /**
     * Determines if this word is equals to {@link #max() max}.
     *
     * @return true if this word is equals to {@link #max() max}
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this == ((UWord)-1);")
/*end[JAVA5SYNTAX]*/
    public boolean isMax() throws NativePragma {
        return this == max();
    }

    /**
     * Determines if this word is equal to a given word.
     *
     * @param word2   the word to compare this word against
     * @return true if this word is equal to <code>word2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this == word2;")
/*end[JAVA5SYNTAX]*/
    public boolean eq(UWord word2) throws NativePragma {
        return this == word2;
    }

    /**
     * Determines if this word is not equal to a given word.
     *
     * @param word2   the word to compare this word against
     * @return true if this word is not equal to <code>word2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this != word2;")
/*end[JAVA5SYNTAX]*/
    public boolean ne(UWord word2) throws NativePragma {
        return this != word2;
    }

    /**
     * Determines if this word is lower than a given word.
     *
     * @param word2   the word to compare this word against
     * @return true if this word is lower than or equals to <code>word2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this < word2;")
/*end[JAVA5SYNTAX]*/
    public boolean lo(UWord word2) throws NativePragma {
        if (value >= 0 && word2.value >= 0) return value < word2.value;
        if (value < 0 && word2.value < 0) return value < word2.value;
        if (value < 0) return false;
        return true;
    }

    /**
     * Determines if this word is lower than or equal to a given word.
     *
     * @param word2   the word to compare this word against
     * @return true if this word is lower than or equal to <code>word2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this <= word2;")
/*end[JAVA5SYNTAX]*/
    public boolean loeq(UWord word2) throws NativePragma {
        return (this == word2) || lo(word2);
    }

    /**
     * Determines if this word is higher than a given word.
     *
     * @param word2   the word to compare this word against
     * @return true if this word is higher than <code>word2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this > word2;")
/*end[JAVA5SYNTAX]*/
    public boolean hi(UWord word2) throws NativePragma {
        return word2.lo(this);
    }

    /**
     * Determines if this word is higher than or equal to a given word.
     *
     * @param word2   the word to compare this word against
     * @return true if this word is higher than or equal to <code>word2</code>
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(code="return this >= word2;")
/*end[JAVA5SYNTAX]*/
    public boolean hieq(UWord word2) throws NativePragma {
        return word2.loeq(this);
    }

    /*-----------------------------------------------------------------------*\
     *                      Hosted execution support                         *
    \*-----------------------------------------------------------------------*/

    /**
     * Gets a hashcode value for this word which is just the value itself.
     *
     * @return  the value of this word
     */
    public int hashCode() throws HostedPragma {
        return (int)value;
    }

    /**
     * Gets a string representation of this word.
     *
     * @return String
     */
    public String toString() throws HostedPragma {
        return ""+value;
    }

    /**
     * The word value.
     */
    private final int/*S64*/ value;

    /**
     * Unique instance pool.
     */
    private static /*S64*/IntHashtable pool;

    /**
     * Gets the canonical UWord instance for a given word.
     *
     * @param  value   the machine word
     * @return the canonical UWord instance for <code>value</code>
     */
    private static UWord get(int/*S64*/ value) throws HostedPragma {
        if (pool == null) {
            pool = new /*S64*/IntHashtable();
        }
        UWord instance = (UWord)pool.get(value);
        if (instance == null) {
            instance = new UWord(value);
            try {
                pool.put(value, instance);
            } catch (OutOfMemoryError e) {
                throw new OutOfMemoryError("Failed to grow pool when adding " + value);
            }
        }
        return instance;
    }

    /**
     * Constructor.
     *
     * @param value  a machine word
     */
    private UWord(int/*S64*/ value) throws HostedPragma {
        this.value = value;
    }
}
