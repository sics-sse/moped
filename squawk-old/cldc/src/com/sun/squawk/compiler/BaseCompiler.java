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

package com.sun.squawk.compiler;


/**
 * Language-independent base compiler interface to support general-purpose applications.
 *
 */
public interface BaseCompiler extends Types {

    /*-----------------------------------------------------------------------*\
     *                         Procedure preambles                           *
    \*-----------------------------------------------------------------------*/

    /**
     * Standard C-style calling convention and parameter passing procedures.
     */
    public static final int E_NONE     = 0;

    /**
     * @todo this preamble is not currently used?
     */
    public static final int E_NULL     = 1;     // Slot zero is set to null.

    /**
     * Squawk Interpreter procedure preamble. The address of the bytecodes to be interpreted are
     * passed in the special JVM register. This preamble is used only for the Squawk Interpreter.
     */
    public static final int E_REGISTER = 2;     // Slot zero is assigned the value of a certain register.

    /**
     * Compiled Squawk procedure preamble. The address of the compiled bytecodes are passed in the
     * special JVM register. This preamble is used only for Squawk bytecode procedures that have
     * been compiled into native code.
     */
    public static final int E_ADDRESS  = 3;     // Slot zero is assigned the address of the function.

    /*-----------------------------------------------------------------------*\
     *                      Squawk Calling Conventions                       *
    \*-----------------------------------------------------------------------*/

    /**
     * Normal C-style calling convention with the parameters and procedure address on the shadow stack.
     * The parameters are passed as per the standard Application Binary Interface specification of the platform.
     */
    public static final int C_NORMAL      = 0;  // Standard call with nothing from the runtime stack.

    /**
     * Normal C-style calling convention except the parameters are on the runtime stack but the procedure
     * address is on the shadow stack.
     */
    public static final int C_DYNAMIC     = 1;  // Standard call with all the data on the runtime stack.

    /**
     * Java-style calling convention for Squawk methods where the address of the method's bytecodes
     * or machine code is passed to the function, and all parameters are provided on the shadow stack.
     */
    public static final int C_JVM         = 2;  // JVM call with nothing on the runtime stack.

    /**
     * Java-style calling convention for Squawk methods with the parameters on the runtime stack but
     * the method address on the shadow stack.
     */
    public static final int C_JVM_DYNAMIC = 3;  // JVM call with all the data from the runtime stack.


    /**
     * Local and parm register allocation hints.
     * <p>
     * A compliant compiler may ignore these values, but if the compiler
     * does honor them then the following semantics is desired.
     * <p>
     * <p>P_LOW variables may be allocated in registers or activation slots.
     * <p>P_MEDIUM variables are better allocated in registers if there are
     * free registers available, otherwise they are allocated in activation slots.
     * <p>P_HIGH variables hust be allocated in registers, and a compiler
     * that honors the hints should produce a fatal error if this is not possible.
     * <p>
     * Within each category the variables defined first have higher priority over those
     * defined later.
     */
    public static final int P_LOW         = 0,
                            P_MEDIUM      = 1,
                            P_HIGH        = 3;


    /*-----------------------------------------------------------------------*\
     *                            Label management                           *
    \*-----------------------------------------------------------------------*/

    /**
     * Allocate a label.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @return the label
     */
    public Label label();

    /**
     * Bind a label to the current location.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @param label the label to bind
     * @return the compiler object
     */
    public Compiler bind(Label label);

    /*-----------------------------------------------------------------------*\
     *                           Function definition                         *
    \*-----------------------------------------------------------------------*/

    /**
     * Emit a function prologue.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param label a label to be bound to the start of the function
     * @param preambleCode the special preamble code for the function
     * @return the compiler object
     */
    public Compiler enter(Label label, int preambleCode);

    /**
     * Emit a function prologue.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param label a label to be bound to the start of the function
     * @return the compiler object
     */
    public Compiler enter(Label label);

    /**
     * Emit a function prologue.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param preambleCode the special preamble code for the function
     * @return the compiler object
     */
    public Compiler enter(int preambleCode);

    /**
     * Emit a function prologue.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler enter();

    /**
     * Define a parameter variable.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param type the type of the local variable (Must be primary)
     * @param hint the P_XXXX priority hint.
     * @return the compiler object
     */
    public Local parm(Type type, int hint);

    /**
     * Define a parameter variable.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param type the type of the local variable (Must be primary)
     * @return the compiler object
     */
    public Local parm(Type type);

    /**
     * Define the function result.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param type the type of the parameter (Must be primary)
     * @return the compiler object
     */
    public Compiler result(Type type);

    /**
     * Emit a function epilogue.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler leave(MethodMap mmap);

    /**
     * Emit a function epilogue.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler leave();

    /*-----------------------------------------------------------------------*\
     *                           Scope definition                            *
    \*-----------------------------------------------------------------------*/

    /**
     * Begin a code scope.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @return the compiler object
     */
    public Compiler begin();

    /**
     * Define a local variable type.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @param type the type of the local variable (Must be primary)
     * @param hint the P_XXXX priority hint.
     * @return the compiler object
     */
    public Local local(Type type, int hint);

    /**
     * Define a local variable type.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @param type the type of the local variable (Must be primary)
     * @return the compiler object
     */
    public Local local(Type type);

    /**
     * End a code scope.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @return the compiler object
     */
    public Compiler end();

    /**
     * Return the number of locals currently defined.
     *
     * @return the number
     */
    public int getLocalCount();

    /**
     * Return the type of the instruction on the top of the stack.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @return the type
     */
    public Type tosType();

    /**
     * Get the value of a parameter word.
     *
     * <p>
     * Stack: ..., INDEX -> ..., INT
     * <p>
     *
     * Note that this method never returns a long.  The user of this interface has
     * to deal with longs as two words.
     *
     * <p>
     * Checks:
     * 1- The index needs to be of type INT.
     * <p>
     *
     * @return the compiler object
     */
    public Compiler loadParm();

    /**
     * Set the value of a parameter word.
     *
     * <p>
     * Stack: ..., VALUE, INDEX -> ...
     * <p>
     *
     * <p>
     * Checks:
     * 1- The index needs to be of type INT.
     * <p>
     *
     * @return the compiler object
     */
    public Compiler storeParm();

    /**
     * Add a comment node to the IR.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @param str the comment
     * @return the compiler object
     */
    public Compiler comment(String str);


    /**
     * Get a local variable or parameter and push it onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param local the local variable to load
     * @return the compiler object
     */
    public Compiler load(Local local);

    /**
     * Set a local variable or parameter to a value popped from the stack.
     *
     * <p>
     * Stack: ..., VALUE -> ...
     * <p>
     *
     * <p>
     * Checks:
     * 1- The local and the value from the stack need to be a primary type.
     * <p>
     *
     * @param local the local variable to store into
     * @return the compiler object
     */
    public Compiler store(Local local);

    /**
     * Load a value from a reference. The reference is popped from the stack
     * and the type specified is loaded from that address. Secondary types
     * (BYTE, UBYTE, SHORT, USHORT) are all widened to INT by this operation.
     *
     * <p>
     * Stack: ..., REF -> ..., VALUE
     * <p>
     *
     * <p>
     * Checks:
     * 1- The value on the top of the stack is of type reference (REF) or OOP.
     * 2- The type of data to be read is either of primary or secondary type.
     * <p>
     *
     * @param type the type of the data to load
     * @return the compiler object
     */
    public Compiler read(Type type);

    /**
     * Store a value at a reference. The value and reference are popped from the stack
     * and the value is written to the referenced address according to the specified type.
     * The type parameter is used to check primary types and to narrow secondry types.
     *
     * <p>
     * Stack: ..., VALUE, REF -> ...
     * <p>
     *
     * <p>
     * Checks:
     * 1- The address on the top of the stack should be of type reference (REF).
     * 2- The other value on the stack has to have the same primitive type as the
     *    type of the write.  All types can be used with this method (primary,
     *    secondary), except for pseudo types.
     * <p>
     *
     * @param type the type of the data to load
     * @return the compiler object
     */
    public Compiler write(Type type);

    /**
     * Push an integer constant onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param n the value of the constant
     * @return the compiler object
     */
    public Compiler literal(int n);

    /**
     * Push a long constant onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param n the value of the constant
     * @return the compiler object
     */
    public Compiler literal(long n);

    /**
     * Push a float constant onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param n the value of the constant
     * @return the compiler object
     */
    public Compiler literal(float n);

    /**
     * Push a double constant onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param n the value of the constant
     * @return the compiler object
     */
    public Compiler literal(double n);

    /**
     * Push a boolean constant onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param n the value of the constant
     * @return the compiler object
     */
    public Compiler literal(boolean n);

    /**
     * Push an address of an array onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param array the array
     * @return the compiler object
     */
    public Compiler literal(Object array);


    /**
     * Push an address of an unresolved symbol onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param name  the name of the symbol
     * @return the compiler object
     */
    public Compiler symbol(String name);

    /**
     * Push the address of a label onto the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param label the label
     * @return the compiler object
     */
    public Compiler literal(Label label);

    /**
     * Define some data.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param label the label to the data
     * @param obj the array
     * @return the compiler object
     */
    public Compiler data(Label label, Object obj);

    /**
     * Dup the top element of the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @return the compiler object
     */
    public Compiler dup();

    /**
     * Dup the receiver element of the stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * In a method call, the receiver is the object whose method is being invoked.
     * This is the implicit first parameter of method calls.  Used by all functions
     * that have an invoke virtual.
     *
     * @return the compiler object
     */
    public Compiler dupReceiver();

    /**
     * Pop the top element of the compiler stack.
     *
     * <p>
     * Stack: ..., VALUE -> ...
     * <p>
     *
     * @return the compiler object
     */
    public Compiler drop();

    /**
     * Pop all the element of the compiler stack.
     *
     * <p>
     * Stack: ... -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler dumpAll();

    /**
     * Swap the top two elements of the stack.
     *
     * <p>
     * Stack: ... VALUE1, VALUE2 -> ..., VALUE2, VALUE1
     * <p>
     *
     * @return the compiler object
     */
    public Compiler swap();

    /**
     * Swap the contence of the stack.
     *
     * <p>
     * Stack: [VALUE0, ..., VALUEN] -> [VALUEN, ... , VALUE0]
     * <p>
     *
     * @return the compiler object
     */
    public Compiler swapAll();

    /**
     * Swap the contence of the stack if the target ABI push parameters right-to-left.
     *
     * <p>
     * Stack: [VALUE0, ..., VALUEN] -> [VALUEN, ... , VALUE0] // Only on right-to-left systems
     * <p>
     *
     * @return the compiler object
     */
    public Compiler swapForABI();

    /**
     * Push the data onto the runtime stack.
     *
     * <p>
     * Stack: ..., VALUE -> ...
     * <p>
     *
     * @return the compiler object
     */
    public Compiler push();

    /**
     * Pop the top element of the runtime stack.
     *
     * <p>
     * Stack: ... -> ..., VALUE
     * <p>
     *
     * @param type the data type to pop
     * @return the compiler object
     */
    public Compiler pop(Type type);

    /**
     * Pop all the elements of the runtime stack.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @return the compiler object
     */
    public Compiler popAll();

    /**
     * Pop the top most element, force it to be the new type, and push the result.
     * Force uses the same bit representation.
     *
     * <p>
     * Stack: ... OLDVALUE -> ..., NEWVALUE
     * <p>
     *
     * Force rules:
     * <ul>
     * <li> the source type is a primary type
     * <li> the destination type is a primary type
     * <li> the source and destination types are of the same bit size
     * <li> the following types cannot be forced
     *      <ul>
     *      <li> ref -> uint
     *      <li> oop -> anything other than ref
     *      <li> uint -> ref, oop, float
     *      <li>float -> uint, ref, oop
     *      </ul>
     * </ul>
     * <p>
     * The force rules are summarized as follows
     * <pre>
     * From/to   int     uint    long    ulong   float   double  ref     oop
     *  int      fnop    f                       f               f32     f32
     *  uint     f       fnop                                    f32     f32
     *  long                     fnop    f               f       f64     f64
     *  ulong                    f       fnop                    f64     f64
     *  float    f                               fnop
     *  double                   f                       fnop
     *  ref      f32     f32     f64     f64                     fnop    f
     *  oop      f32     f32     f64     f64                     f       fnop
     * </pre>
     *
     * where
     * <ul>
     * <li> f stands for force allowed
     * <li> fnop stands for force allowed and do nothing
     * <li> f32 stands for force allowed on 32 bit platform
     * <li> f64 stands for force allowed on 64 bit platform
     * </ul>
     *
     * @param to the type to force to
     * @return the compiler object
     */
    public Compiler force(Type to);

    /**
     * Pop the top most element, convert it to a new type, and push the result.
     * Conversions use different bit representation, where needed.
     *
     * <p>
     * Stack: ... OLDVALUE -> ..., NEWVALUE
     * <p>
     *
     * Convert rules:
     * <ul>
     * <li> the source type is a primary type
     * <li> the destination type is a primary type, except when the source type
     *      is int, in which case the destination type can be a secondary type
     * <li> the following types can be converted
     *      <pre>
     *  From\To 2nd     int     uint    long    ulong   float   double  ref     oop
     *  2nd
     *  int     c       cnop            c               c       c
     *  uint                    cnop            c
     *  long            c               cnop            c       c
     *  ulong                   c               cnop
     *  float           c               c               cnop    c
     *  double          c               c               c       cnop
     *  ref                                                             cnop
     *  oop                                                                     cnop
     *      </pre>
     *      where
     *      <ul>
     *      <li> c stands for convert allowed
     *      <li> cnop stands for convert allowed and do nothing
     *      <li> 2nd stands for all secondary types
     *      </ul>
     * </ul>
     *
     * @param to the type to convert to
     * @return the compiler object
     */
    public Compiler convert(Type to);

    /**
     * Add the top two elements on the stack. They must be of the same type, or be
     * a pointer and an integer.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler add();

    /**
     * Subtract the top two elements on the stack. They must be of the same type, or be
     * a pointer and an integer.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler sub();

    /**
     * Multiply the top two elements on the stack. They must be of the same type.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler mul();

    /**
     * Divide the top two elements on the stack. They must be of the same type.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler div();

    /**
     * Remainder the top two elements on the stack. They must be of the same type.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler rem();

    /**
     * And the top two elements on the stack. They must be of the same type.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler and();

    /**
     * Or the top two elements on the stack. They must be of the same type.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler or();

    /**
     * Xor the top two elements on the stack. They must be of the same type.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler xor();

    /**
     * Shift the second element on the stack left by the number of bits specified by the top element.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler shl();

    /**
     * Shift the second element on the stack right by the number of bits specified by the top element.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler shr();

    /**
     * Shift the second element on the stack right by the number of bits specified by the top element.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler ushr();

    /**
     * Negate (2's complement) the top element.
     *
     * <p>
     * Stack: ..., VALUE -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler neg();

    /**
     * Produce the 1's complement of the top element.
     *
     * <p>
     * Stack: ..., VALUE -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler com();

    /**
     * Test the top two elements. Push true if s[0] == s[1] else false.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler eq();

    /**
     * Test the top two elements. Push true if s[0] != s[1] else false.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler ne();

    /**
     * Test the top two elements. Push true if s[0] <= s[1] else false.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler le();

    /**
     * Test the top two elements. Push true if s[0] < s[1] else false.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler lt();

    /**
     * Test the top two elements. Push true if s[0] >= s[1] else false.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler ge();

    /**
     * Test the top two elements. Push true if s[0] > s[1] else false.
     *
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler gt();

    /**
     * Test the top two floating point elements.  Push true if s[0] < s[1]
     * else false.
     * 
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler cmpl();

    /**
     * Test the top two floating point elements.  Push true if s[0] > s[1]
     * else false.
     * 
     * <p>
     * Stack: ..., VALUE1, VALUE2 -> ..., RESULT
     * <p>
     *
     * @return the compiler object
     */
    public Compiler cmpg();

    /**
     * Unconditionally branch to the label.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * Checks
     * <ul>
     * <li> For a backward branch, the stack must be empty.
     * </ul>
     *
     * @param label the label to branch to
     * @return the compiler object
     */
    public Compiler br(Label label);

    /**
     * Branch to the label if the popped value is true.
     *
     * <p>
     * Stack: ..., VALUE -> ...
     * <p>
     *
     * @param label the label to branch to
     * @return the compiler object
     */
    public Compiler bt(Label label);

    /**
     * Branch to the label if the popped value is false.
     *
     * <p>
     * Stack: ..., VALUE -> ...
     * <p>
     *
     * @param label the label to branch to
     * @return the compiler object
     */
    public Compiler bf(Label label);

    /**
     * Unconditionally branch to the label.
     *
     * <p>
     * Stack: _ -> ._
     * <p>
     *
     * @param dst   the absolute destination
     * @return the compiler object
     */
    public Compiler br(int dst);

    /**
     * Branch to the label if the popped value is true.
     *
     * <p>
     * Stack: VALUE -> _
     * <p>
     *
     * @param dst   the absolute destination
     * @return the compiler object
     */
    public Compiler bt(int dst);

    /**
     * Branch to the label if the popped value is false.
     *
     * <p>
     * Stack: VALUE -> _
     * <p>
     *
     * @param dst   the absolute destination
     * @return the compiler object
     */
    public Compiler bf(int dst);

    /**
     * Jump somewhere. The top most value must be the address of the function to be called.
     *
     * <p>
     * Stack: ADDRESS -> _
     * <p>
     *
     * Checks
     * <ul>
     * <li> For a backward and forward branch, the stack must be empty.
     * <li> The popped value must be an address (i.e., it's base type must be REF or OOP).
     * </ul>
     *
     * @return the compiler object
     */
    public Compiler jump();

    /**
     * Call a function. The top most value must be the address of the function to be called.
     *
     * <p>
     * Stack: ..., VALUEN to VALUE1, ADDRESS -> ..., [RESULT]
     * <p>
     *
     * Checks
     * <ul>
     * <li> There are enough parameters on the evaluation stack.
     * <li> The address (top of stack) parameter is of type INT (for 32-bit compilations).
     * </ul>
     *
     * @param nparms the number of parameters to pop
     * @param type the return type to be pushed onto the stack
     * @param convention the calling convebtion
     * @return the compiler object
     */
    public Compiler call(int nparms, Type type, int convention);

    /**
     * Call a function. The top most value must be the address of the function to be called.
     *
     * <p>
     * Stack: ..., VALUEN to VALUE1, ADDRESS -> ..., [RESULT]
     * <p>
     *
     * @param nparms the number of parameters to pop
     * @param type the return type to be pushed onto the stack
     * @return the compiler object
     */
    public Compiler call(int nparms, Type type);

    /**
     * Call a function. The top most value must be the address of the function to be called.
     * and all the other parameters on the stack are taken as parameters.
     *
     * <p>
     * Stack: ..., VALUEN to VALUE1, ADDRESS -> ..., [RESULT]
     * <p>
     *
     * @param type the return type to be pushed onto the stack
     * @return the compiler object
     */
    public Compiler call(Type type);

    /**
     * Call a function. The top most value must be the address of the function to be called.
     * and all the other parameters on the stack are taken as parameters.
     *
     * <p>
     * Stack: ..., VALUEN to VALUE1, ADDRESS -> ..., [RESULT]
     * <p>
     *
     * @param type the return type to be pushed onto the stack
     * @param convention the calling convention
     * @return the compiler object
     */
    public Compiler call(Type type, int convention);

    /**
     * Return from a method.
     * The return type overwrites the return type that the method would have
     * had otherwise.
     *
     * <p>
     * Stack: ..., [VALUE] -> _
     * <p>
     *
     * Checks
     * <ul>
     * <li> If a value is to be returned, the evaluation stack has a value to return.
     * <li> If no value is to be returned, the evaluation stack is empty.
     * </ul>
     *
     * @param type the return type
     * @return the compiler object
     */
    public Compiler ret(Type type);

    /**
     * Return from a method.
     *
     * <p>
     * Stack: ..., [VALUE] -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler ret();

    /**
     * Specify an unreachable place.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler deadCode();

    /*-----------------------------------------------------------------------*\
     *                          Configuration Data                           *
    \*-----------------------------------------------------------------------*/

    /**
     * Boolean to say if the machine can execute unaligned loads.
     *
     * @return true if it is true
     */
    public boolean loadsMustBeAligned();

    /**
     * Boolean to say if the machine uses big endian ordering.
     *
     * @return true if it is true
     */
    public boolean isBigEndian();

    /**
     * Boolean to say if the tableswitch and stableswitch are to be padded in such
     * a way that the initial int (the low parameter) is placed on a 4 byte boundary.
     *
     * @return true if it is true
     */
    public boolean tableSwitchPadding();

    /**
     * Boolean to say if the tableswitch and stableswitch are to be padded in such
     * a way that the initial int (the low parameter) is placed on a 4 byte boundary
     * and any padding at the start of the bytecode sequence is complemented at the
     * end so that the size of the sequence is always a constant.
     *
     * @return true if it is true
     */
    public boolean tableSwitchEndPadding();


    /*-----------------------------------------------------------------------*\
     *                               Compilation                             *
    \*-----------------------------------------------------------------------*/

    /**
     * Compiler the IR.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler compile();

    /**
     * Dumps the assembler output of the compiler to the console.
     *
     */
    public void decode(int relocation);

    /**
     * Get the length of the compiled code.
     *
     * @return the length in bytes
     */
    public int getCodeSize();

    /**
     * Get the code array buffer.
     *
     * @return the code array
     */
    public byte[] getCode();

    /**
     * Return the relocation information. This is an array of ints where
     * each entry describes one offset into the code where relocation
     * is required. The format of each entry is such that the top 8 bits is
     * a code indicating the type of relocation required and the low 24 bits
     * is the offset.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @return the relocation information.
     */
    public int[] getRelocationInfo();

    /**
     * Return the offsets to unresolved symbols.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @return the table of offset to symbol associations
     */
    public SymbolicFixupTable getFixupInfo();

}
