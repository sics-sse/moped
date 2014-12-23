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

package com.sun.squawk.util;

import com.sun.squawk.*;
import com.sun.squawk.pragma.*;

/**
 * Provides support for assertions that can be removed on demand in order for
 * building a release version.
 *
 * If the preprocessor is running with showLineNumbers=true, it will call the versions
 * of these methods that take a filename and linenumber argument.
 *
 * @version  1.00
 */
public class Assert {

    /**
     * Whether assertions are included in the bytecodes or not.
     */
    public static final boolean ASSERTS_ENABLED = Klass.ASSERTIONS_ENABLED;

    /**
     * Flag to always enable shouldNotReachHere().
     */
    public static final boolean SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED = true;

    /**
     * Whether Assert.always is a fatal error or throws an exception (usually TRUE)
     */
    public static final boolean ASSERT_ALWAYS_IS_FATAL = true;

    /**
     * Don't let anyone instantiate this class.
     */
    private Assert() {}

    /**
     * Create one centralized place where an exception is thrown in case of Assert failure.
     * Makes it easier to place a single breakpoint and debug.
     * 
     * @param message
     */
    protected static void throwAssertFailedException(String message)  throws NotInlinedPragma {
        if (System.err != null) {
            System.err.flush();
            System.out.flush();
            throw new RuntimeException("Assertion failed: " + message);
        } else {
            VM.print("Assertion failed: ");
            VM.println(message);
            VM.println("Too early to throw exception");
            VM.fatalVMError();
        }
    }

    /**
     * Create one centralized place where an exception is thrown in case of Assert failure.
     * Makes it easier to place a single breakpoint and debug.
     *
     * @param message
     */
    private static void throwAssertFailedException(String systemMessage, String message)  throws NotInlinedPragma {
        throwAssertFailedException(systemMessage + message);
    }

    /**
     * Create one centralized place where an exception is thrown in case of Assert failure.
     * Makes it easier to place a single breakpoint and debug.
     *
     * @param message
     */
    private static void throwAssertFailedException(String systemMessage, String message, String filename, int lineno)  throws NotInlinedPragma {
        throwAssertFailedException(systemMessage + "(" + filename + ":" + lineno + "): " + message);
    }
    
    /**
     * Asserts that the specified condition is true. If the condition is false,
     * a RuntimeException is thrown with the specified message.
     *
     * @param   cond  condition to be tested
     * @param   msg   message that explains the failure
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void that(boolean cond, String msg) {
        if (ASSERTS_ENABLED && !cond) {
            throwAssertFailedException(msg);
        }
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void that(boolean cond, String msg, String filename, int lineno) {
        if (ASSERTS_ENABLED && !cond) {
            throwAssertFailedException("", msg, filename, lineno);
        }
    }

    /**
     * Asserts that the specified condition is true. If the condition is false,
     * a RuntimeException is thrown.
     *
     * @param   cond  condition to be tested
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", #cond, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void that(boolean cond) {
        if (ASSERTS_ENABLED && !cond) {
            throwAssertFailedException("");
        }
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", #cond, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void that(boolean cond, String filename, int lineno) {
        if (ASSERTS_ENABLED && !cond) {
            throwAssertFailedException("", "", filename, lineno);
        }
    }

    /**
     * Asserts that the compiler should never reach this point.
     *
     * @param   msg   message that explains the failure
     * @return a null RuntimeException so that constructions such
     *         as <code>throw Assert.shouldNotReachHere()</code> will
     *         be legal and thus avoid the need to return meaningless
     *         values from functions that have failed.
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="{ fprintf(stderr, \"shouldNotReachHere: %s -- %s:%d\n\", msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHere(String msg) throws NotInlinedPragma {
        throwAssertFailedException("should not reach here: ", msg);
        // NO-OP
        return null;
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="{ fprintf(stderr, \"shouldNotReachHere: %s -- %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHere(String msg, String filename, int lineno) throws NotInlinedPragma {
        throwAssertFailedException("should not reach here: ", msg, filename, lineno);
        // NO-OP
        return null;
    }

    /**
     * Asserts that the compiler should never reach this point.
     *
     * @return a null RuntimeException so that constructions such
     *         as <code>throw Assert.shouldNotReachHere()</code> will
     *         be legal and thus avoid the need to return meaningless
     *         values from functions that have failed.
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="{ fprintf(stderr, \"shouldNotReachHere -- %s:%d\n\", __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHere() {
        throwAssertFailedException("should not reach here");
        // NO-OP
        return null;
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="{ fprintf(stderr, \"shouldNotReachHere -- %s:%d\n\", __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHere(String filename, int lineno) {
        throwAssertFailedException("", "should not reach here", filename, lineno);
        // NO-OP
        return null;
    }


    /*---------------------------------------------------------------------------*\
     *                      Fatal versions of the above methods                  *
    \*---------------------------------------------------------------------------*/

    /**
     * Asserts that the specified condition is true. If the condition is false
     * the specified message is displayed and the VM is halted.
     *
     * @param   cond  condition to be tested
     * @param   msg   message that explains the failure
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void thatFatal(boolean cond, String msg) {
        if (!cond) {
            VM.print("Assertion failed: ");
            VM.println(msg);
            VM.fatalVMError();
        }
    }

    private static void printContext(String msg, String filename, int lineno) {
        VM.print("Assertion failed: ");
        VM.print(msg);
        VM.print("(");
        VM.print(filename);
        VM.print(":");
        VM.print(lineno);
        VM.print("): ");
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void thatFatal(boolean cond, String msg, String filename, int lineno) {
        if (!cond) {
            printContext("", filename, lineno);
            VM.println(msg);
            VM.fatalVMError();
        }
    }

    /**
     * Asserts that the specified condition is true. If the condition is false
     * the VM is halted.
     *
     * @param   cond  condition to be tested
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", #cond, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void thatFatal(boolean cond) {
        if (!cond) {
            VM.println("Assertion failed");
            VM.fatalVMError();
        }
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", #cond, __FILE__, __LINE__); fatalVMError(\"\"); })")
/*end[JAVA5SYNTAX]*/
    public static void thatFatal(boolean cond, String filename, int lineno) {
        if (!cond) {
            printContext("", filename, lineno);
            VM.fatalVMError();
        }
    }

    /**
     * Asserts that the compiler should never reach this point.
     *
     * @param   msg   message that explains the failure
     * @return a null RuntimeException so that constructions such
     *         as <code>throw Assert.shouldNotReachHere()</code> will
     *         be legal and thus avoid the need to return meaningless
     *         values from functions that have failed.
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="{ fprintf(stderr, \"shouldNotReachHere: %s -- %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHereFatal(String msg) {
        VM.print("Assertion failed: ");
        VM.print("should not reach here: ");
        VM.println(msg);
        VM.fatalVMError();
        return null;
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="{ fprintf(stderr, \"shouldNotReachHere: %s -- %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHereFatal(String msg, String filename, int lineno) {
        printContext("should not reach here: ", filename, lineno);
        VM.println(msg);
        VM.fatalVMError();
        return null;
    }

    /**
     * Asserts that the compiler should never reach this point.
     *
     * @return a null RuntimeException so that constructions such
     *         as <code>throw Assert.shouldNotReachHere()</code> will
     *         be legal and thus avoid the need to return meaningless
     *         values from functions that have failed.
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="{ fprintf(stderr, \"shouldNotReachHere -- %s:%d\n\", __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHereFatal() {
        VM.print("Assertion failed: ");
        VM.println("should not reach here: ");
        VM.fatalVMError();
        return null;
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="{ fprintf(stderr, \"shouldNotReachHere -- %s:%d\n\", __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static RuntimeException shouldNotReachHereFatal(String filename, int lineno) {
        printContext("should not reach here: ", filename, lineno);
        VM.fatalVMError();
        return null;
    }

    /*---------------------------------------------------------------------------*\
     *        Fatal VM assertions that won't be removed by the pre-processor     *
    \*---------------------------------------------------------------------------*/

    /**
     * Asserts that the specified condition is true. If the condition is false
     * the specified message is displayed and the VM is halted.
     *
     * Calls to this method are never removed by the Squawk pre-processor and as
     * such should only be placed in frequent execution paths absolutely necessary.
     *
     * @param   cond  condition to be tested
     * @param   msg   message that explains the failure
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void always(boolean cond, String msg) {
        if (!cond) {
            if (ASSERT_ALWAYS_IS_FATAL || VMThread.currentThread().isServiceThread()) {
                VM.print("Assertion failed: ");
                VM.println(msg);
                VM.fatalVMError();
            } else {
                throwAssertFailedException("Assertion failed: ", msg);
            }
        }
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", (char*)msg, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void always(boolean cond, String msg, String filename, int lineno) {
        if (!cond) {
            if (ASSERT_ALWAYS_IS_FATAL || VMThread.currentThread().isServiceThread()) {
                printContext("", filename, lineno);
                VM.println(msg);
                VM.fatalVMError();
            } else {
                throwAssertFailedException("Assertion failed: ", msg, filename, lineno);
            }
        }
    }

    /**
     * Asserts that the specified condition is true. If the condition is false
     * the VM is halted.
     *
     * Calls to this method are never removed by the Squawk pre-processor and as
     * such should only be placed in frequent execution paths absolutely necessary.
     *
     * @param   cond  condition to be tested
     */
/*if[JAVA5SYNTAX]*/
    @Vm2c(macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", #cond, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void always(boolean cond) {
        if (!cond) {
            if (ASSERT_ALWAYS_IS_FATAL || VMThread.currentThread().isServiceThread()) {
                VM.println("Assertion failed");
                VM.fatalVMError();
            } else {
                throwAssertFailedException("Assertion failed");
            }
        }
    }

/*if[JAVA5SYNTAX]*/
    @Vm2c (macro="if (!(cond)) { fprintf(stderr, \"Assertion failed: %s, at %s:%d\n\", #cond, __FILE__, __LINE__); fatalVMError(\"\"); }")
/*end[JAVA5SYNTAX]*/
    public static void always(boolean cond, String filename, int lineno) {
        if (!cond) {
            if (ASSERT_ALWAYS_IS_FATAL || VMThread.currentThread().isServiceThread()) {
                printContext("", filename, lineno);
                VM.fatalVMError();
            } else {
                throwAssertFailedException("Assertion failed", "", filename, lineno);
            }
        }
    }
}
