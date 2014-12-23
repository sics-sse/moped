/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.pragma;

/**
 * The root of the pragma exception hierarchy.  These exceptions are all dummy exceptions
 * that are never actually thrown.  Methods declare them to be thrown as a way of indicating
 * various properties.
 */
public class PragmaException extends RuntimeException {

    /**
     * Bit flag for hosted methods.
     *
     * @see HostedPragma
     */
    public static final int HOSTED = 0x0001;

    /**
     * Bit flag for replacement constructors.
     *
     * @see com.sun.squawk.pragma.ReplacementConstructorPragma
     */
    public static final int REPLACEMENT_CONSTRUCTOR = 0x0002;

    /**
     * Bit flag for methods that should only be invoked from the interpreter.
     *
     * @see com.sun.squawk.pragma.InterpreterInvokedPragma
     */
    public static final int INTERPRETER_INVOKED = 0x0004;

    /**
     * Bit flag for methods that are made into native methods by the translator.
     *
     * @see NativePragma
     */
    public static final int NATIVE = 0x0008;

    /**
     * Bit flag for methods that have relaxed inlining by the translator.
     *
     * @see AllowInlinedPragma
     */
    public static final int ALLOW_INLINED = 0x0010; 

    /**
     * Bit flag for methods that are never inlined by the translator.
     *
     * @see NotInlinedPragma
     */
    public static final int NOT_INLINED = 0x0020;

    /**
     * Bit flag for methods that are forceably inlined by the translator.
     * FORCE_INLINED implies ALLOW_INLINED
     *
     * @see ForceInlinedPragma
     */
    public static final int FORCE_INLINED_A = 0x0040;
    public static final int FORCE_INLINED = 0x0050;// == (ALLOW_INLINED | FORCE_INLINED_A)

    /**
     * Given a bit mask, tells whether the method is run only in a hosted environment.
     */
    public static boolean isHosted(int pragmaFlags) {
        return (pragmaFlags & HOSTED) != 0;
    }

    /**
     * Given a bit mask, tells whether a method has its body replace a constructor
     * with the same signature.  Such methods should never be explicitly called.
     */
    public static boolean isReplacementConstructor(int pragmaFlags) {
        return (pragmaFlags & REPLACEMENT_CONSTRUCTOR) != 0;
    }

    /**
     * Given a bit mask, tells whether the method is to be invoked only from
     * the interpreter or JIT compiled code.
     */
    public static boolean isInterpreterInvoked(int pragmaFlags) {
        return (pragmaFlags & INTERPRETER_INVOKED) != 0;
    }

    /**
     * Given a bit mask, tells whether the method will be turned into a native
     * method by the translator.
     */
    public static boolean isNative(int pragmaFlags) {
        return (pragmaFlags & NATIVE) != 0;
    }

    /**
     * Given a bit mask, tells whether the method must be inlined by the translator.
     */
    public static boolean isForceInlined(int pragmaFlags) {
        return (pragmaFlags & FORCE_INLINED) == FORCE_INLINED;
    }
    
    /**
     * Given a bit mask, tells whether the method must be inlined by the translator.
     */
    public static boolean isAllowInlined(int pragmaFlags) {
        return (pragmaFlags & ALLOW_INLINED) != 0;
    }

    /**
     * Given a bit mask, tells whether the method is never inlined by the translator.
     */
    public static boolean isNotInlined(int pragmaFlags) {
        return (pragmaFlags & NOT_INLINED) != 0;
    }

    /**
     * Converts the name of a pragma class to a corresponding bit constant.
     *
     * @param className   the name of one of the hard-wired pragma exception classes
     * @return  the constant corresponding to <code>className</code> or 0 if
     *          <code>className</code> does not denote a pragma
     */
    public static int toModifier(String className) {
        if (className.equals("com.sun.squawk.pragma.HostedPragma")) {
            return HOSTED;
        } else if (className.equals("com.sun.squawk.pragma.ReplacementConstructorPragma")) {
            return REPLACEMENT_CONSTRUCTOR;
        } else if (className.equals("com.sun.squawk.pragma.InterpreterInvokedPragma")) {
            return INTERPRETER_INVOKED;
        } else if (className.equals("com.sun.squawk.pragma.NativePragma")) {
            return NATIVE;
        } else if (className.equals("com.sun.squawk.pragma.ForceInlinedPragma")) {
            return FORCE_INLINED;
        } else if (className.equals("com.sun.squawk.pragma.NotInlinedPragma")) {
            return NOT_INLINED;
        } else if (className.equals("com.sun.squawk.pragma.AllowInlinedPragma")) {
            return ALLOW_INLINED;
        } else {
            return 0;
        }
    }

    PragmaException() {}
}
