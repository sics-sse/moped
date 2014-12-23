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

package tests;

/**
 * Test performance of static variable references
 */
public class Test {

    public final static int CLASS_CACHE_SIZE = 6;

    // use non-final statics here for further stress testing:
    public static int LOOP_COUNT = 100000;

    private static int i;

    public static void main(String[] args) {
        System.out.print("Running: Test");
       
        try {
            runXTests();
        } finally {
            System.out.println("runXTests() returned!");
        }
    }

    public static void runXTests() {
        testOneClass();
        testFewClasses();
        testMoreClasses();
        testManyClasses();
    }

    private static void testOneClass() {
        long t = System.currentTimeMillis();

        for (i = 0; i < LOOP_COUNT; i++) {
            ClassA.a = i;
            if (ClassA.a != i) {
                throw new RuntimeException("Test failed at " + i);
            }
        }
        t = System.currentTimeMillis() - t;
        System.out.println("One class time (ms): " + t );
    }

    private static void testFewClasses() {
        long t = System.currentTimeMillis();
        int v;
        
        for (i = 0; i < LOOP_COUNT; i++) {
            ClassA.a = i;
            v = ClassA.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassB.a = i;
            v = ClassB.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassC.a = i;
            v = ClassC.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassD.a = i;
            v = ClassD.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
        }
        t = System.currentTimeMillis() - t;
        System.out.println("Few classes time (ms): " + t);
        System.out.println("    relative (ms): " + (t / 4));
    }

    private static void testMoreClasses() {
        long t = System.currentTimeMillis();
        int v;

        for (i = 0; i < LOOP_COUNT; i++) {
            ClassA.a = i;
            v = ClassA.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassB.a = i;
            v = ClassB.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassC.a = i;
            v = ClassC.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassD.a = i;
            v = ClassD.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassE.a = i;
            v = ClassE.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassF.a = i;
            v = ClassF.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassG.a = i;
            v = ClassG.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassH.a = i;
            v = ClassH.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassI.a = i;
            v = ClassI.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassJ.a = i;
            v = ClassJ.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }

        }
        t = System.currentTimeMillis() - t;
        System.out.println("More classes time (ms): " + t);
        System.out.println("    relative (ms): " + (t / 10));
    }

    private static void testManyClasses() {
        long t = System.currentTimeMillis();
        int v;

        for (i = 0; i < LOOP_COUNT; i++) {
            ClassA.a = i;
            v = ClassA.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassB.a = i;
            v = ClassB.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassC.a = i;
            v = ClassC.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassD.a = i;
            v = ClassD.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassE.a = i;
            v = ClassE.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassF.a = i;
            v = ClassF.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassG.a = i;
            v = ClassG.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassH.a = i;
            v = ClassH.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassI.a = i;
            v = ClassI.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassJ.a = i;
            v = ClassJ.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }

            ClassK.a = i;
            v = ClassK.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassL.a = i;
            v = ClassL.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassM.a = i;
            v = ClassM.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassN.a = i;
            v = ClassN.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassO.a = i;
            v = ClassO.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassP.a = i;
            v = ClassP.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassQ.a = i;
            v = ClassQ.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassR.a = i;
            v = ClassR.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassS.a = i;
            v = ClassS.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassT.a = i;
            v = ClassT.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }

            ClassU.a = i;
            v = ClassU.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassV.a = i;
            v = ClassV.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassW.a = i;
            v = ClassW.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassX.a = i;
            v = ClassX.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassY.a = i;
            v = ClassY.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            ClassZ.a = i;
            v = ClassZ.a;
            if (v != i) {
                throw new RuntimeException("Test failed at " + i);
            }
            
        }
        t = System.currentTimeMillis() - t;
        System.out.println("Many classes time (ms): " + t);
        System.out.println("    relative (ms): " + (t / 26));
    }

    private Test() {
    }
}


class ClassA {
    static int a;
}
class ClassB {
    static int a;
}
class ClassC {
    static int a;
}
class ClassD {
    static int a;
}
class ClassE {
    static int a;
}
class ClassF {
    static int a;
}
class ClassG {
    static int a;
}
class ClassH {
    static int a;
}
class ClassI {
    static int a;
}
class ClassJ {
    static int a;
}
class ClassK {
    static int a;
}
class ClassL {
    static int a;
}
class ClassM {
    static int a;
}
class ClassN {
    static int a;
}
class ClassO {
    static int a;
}
class ClassP {
    static int a;
}
class ClassQ {
    static int a;
}
class ClassR {
    static int a;
}
class ClassS {
    static int a;
}
class ClassT {
    static int a;
}
class ClassU {
    static int a;
}
class ClassV {
    static int a;
}
class ClassW {
    static int a;
}
class ClassX {
    static int a;
}
class ClassY {
    static int a;
}
class ClassZ {
    static int a;
}
