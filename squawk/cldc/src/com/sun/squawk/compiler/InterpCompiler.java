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
 * Language-independent interpreter-compiler interface to support the construction
 * of interpreters.
 *
 */

public interface InterpCompiler extends BaseCompiler {
    /*-----------------------------------------------------------------------*\
     *           Methods to support accessing of interpreter types.          *
    \*-----------------------------------------------------------------------*/

    /**
     * Get the offset from the frame pointer to slot used for the MP variable.
     *
     * @return the offset in bytes
     */
    public int getMPOffset();

    /**
     * Get the offset from the frame pointer to slot used for the IP variable.
     *
     * @return the offset in bytes
     */
    public int getIPOffset();

    /**
     * Get the offset from the frame pointer to slot used for the LP variable.
     *
     * @return the offset in bytes
     */
    public int getLPOffset();

    /*-----------------------------------------------------------------------*\
     *           Methods to support accessing of the stack frame.            *
    \*-----------------------------------------------------------------------*/

    /**
     * Get the length of a jump instruction.
     *
     * @return the length in bytes
     */
    public int getJumpSize();

    /**
     * Get a single byte of a jump instruction sequence.
     *
     * @param bytecodes the address of the bytecode array
     * @param interp the address of the interpreter
     * @param offset the offset to the byte to return
     * @return the byte
     */
    public int getJumpByte(int bytecodes, int interp, int offset);

    /**
     * Add a stack allocation node to the IR.
     *
     * <p>
     * Stack: ..., SIZE -> ...
     * <p>
     *
     * @return the compiler object
     */
    public Compiler alloca();

    /**
     * Ensure that there is enough stack (the values are in  bytes).
     *
     * <p>
     * Stack: EXTRA_LOCALS, EXTRA_STACK -> _
     * <p>
     *
     * @return the compiler object
     */
    public Compiler stackCheck();

    /**
     * Peek the receiver in the runtime stack.
     *
     * <p>
     * Stack: ... -> ..., OOP
     * <p>
     *
     * @return the compiler object
     */
    public Compiler peekReceiver();

    /**
     * Push the frame pointer.
     *
     * <p>
     * Stack: ... -> ..., REF
     * <p>
     *
     * @return the compiler object
     */
    public Compiler framePointer();

    /**
     * Map the FP.xxx offset codes into real ABI offsets.
     *
     * @param fp_value   the FP.xxx code
     * @return           the offset in bytes
     */
    public int getFramePointerByteOffset(int fp_value);


    /*-----------------------------------------------------------------------*\
     *                  Methods that override Compiler methods               *
     *                 to provide support for MP, IP, etc types.             *
    \*-----------------------------------------------------------------------*/

    /**
     * Define a parameter variable.
     *
     * <p>
     * Stack: _ -> _
     * <p>
     *
     * @param type the type of the local variable (Must be primary, or MP, or IP)
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
     * @param type the type of the local variable (Must be primary, or MP, or IP)
     * @return the compiler object
     */
    public Local parm(Type type);

    /**
     * Define a local variable type.
     *
     * <p>
     * Stack: ... -> ...
     * <p>
     *
     * @param type the type of the local variable (Must be primary, or MP, or IP)
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
     * @param type the type of the local variable (Must be primary, or MP, or IP)
     * @return the compiler object
     */
    public Local local(Type type);

    /**
     * Set a local variable or parameter to a value popped from the stack.
     *
     * <p>
     * Stack: ..., VALUE -> ...
     * <p>
     *
     * <p>
     * Checks:
     * 1- The local and the value from the stack need to be a primary type or
     *    one of MP/IP/LP/SS (i.e., a pointer).
     * <p>
     *
     * @param local the local variable to store into
     * @return the compiler object
     */
    public Compiler store(Local local);

    /**
     * Store a value at a reference. The value and reference are popped from the stack
     * and the value is written to the referenced address according to the specified type.
     * The type parameter is used to check primary types and to narrow secondary types.
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
     *    secondary, and pseudo types MP/IP/LP/SS), except for VOID.
     * <p>
     *
     * @param type the type of the data to load
     * @return the compiler object
     */
    public Compiler write(Type type);


}
