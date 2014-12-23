/*
 * Copyright 2012 Oracle Corporation. All Rights Reserved.
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
package tests;

import com.sun.squawk.VM;


/**
 * Test "framework" for running within Squawk (as opposed to mocked in Java SE)
 */
public abstract class MiniTestHelper {

    private static int failures = 0;

    String testName;

    MiniTestHelper(String testName) {
        this.testName = testName;
    }

    static void passed(String name) {
        if (VM.isVerbose()) {
            VM.print("Test ");
            VM.print(name);
            VM.print(" passed\n");
        }
    }

    synchronized static void failed(String msg) {
        failures++;
        throw new TestFailedException("Test FAILED: " + msg);
    }

    synchronized static int getFailCount() {
        return failures;
    }

    static void result(String name, boolean b) {
        if (b) {
            passed(name);
        } else {
            failed(name);
        }
    }

    static void expect(String msg, long expected, long actual) {
        if (expected != actual) {
            String details = " - expected: " + expected + " actual value: " + actual;
            result(msg + details, expected == actual);
        } else {
            result(msg, expected == actual);
        }
    }

    static void expect(String msg, long expected, long actual, int delta) {
        if (Math.abs(expected - actual) > delta) {
            String details = " - expected: " + expected + " +/-: " + delta + " actual value: " + actual;
            result(msg + details, false);
        } else {
            result(msg, true);
        }
    }

    public void runTest() {
        boolean passed = false;

        try {
            System.out.println("Starting test " + testName);
            test();
            passed = (getFailCount() == 0);
        } catch (Throwable thr) {
            System.err.println("Unexpected exception: " + thr);
            thr.printStackTrace();
        } finally {
            if (passed) {
                System.out.println("Test " + testName + " PASSED");
            } else {
                System.out.println("Test " + testName + " FAILED. " + getFailCount() + " tests failed");
            }
        }
    }

    /**
     * The actual test code must be implemented in the test() method.
     */
    public abstract void test();
}

/**
 * TestRunner runs the abstract test method, and catches and reports any TestFailedExceptions.
 */
abstract class TestRunner implements Runnable {
    public void run() {
        try {
            test();
        } catch (TestFailedException ex) {
            System.out.flush();
            System.err.print(">>>>>>>> ");
            System.err.println(Thread.currentThread().getName());
            System.err.print("    ");
            System.err.println(ex.getMessage());
            if (VM.isVerbose()) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * The actual test code must be implemented in the test() method.
     */
    public abstract void test();
}

class TestFailedException extends RuntimeException {

    TestFailedException(String msg) {
        super(msg);
    }

    TestFailedException() {
    }
}