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

import java.lang.ref.*;
import com.sun.squawk.*;
import com.sun.squawk.vm.*;

/**
 * This is a version of com.sun.squawk.Test that is suitable for running as a MIDlet,
 * not in the squawk package/bootstrap suite.
 */
public class Test {

/*---------------------------------------------------------------------------*\
 *                                   "X" Tests                               *
\*---------------------------------------------------------------------------*/

    private static int two;

    public static void main(String[] args) {
        System.out.print("Running: com.sun.squawk.Test");
        for (int i = 0 ; i < args.length ; i++) {
            System.out.print(" " + args[i]);
        }
        System.out.println();

        try {
            runXTests();
        } finally {
            System.out.println("runXTests() returned!");
        }
    }

    public static void runXTests() {
        two = 2;
//x2_1();
//VM.stopVM(-555);

        x2_1();
        x2_2();
        x2_3();
        x2_4();
/*if[FLOATS]*/
        x2_5();
//VM.stopVM(-555);
        x2_6();
        x2_7();
        x2_8();
/*end[FLOATS]*/
        x3();
        x4();
        x5(2);
        x6(2);
        x7(2);
        x8(2);
        x9(2);
        x10();
        x11();
        x12();
        x13();
        x14();
        x15();
        x16();
        x20();
        x30();
        x31();
        x32();
        x33();
        x34();
        x35();
        x36();
        x37();
        x38();
        x39();
        x40();
        x41(null, 123);
        x42();
        x43();
        x44();
        x45();
//        x46();
//        x47();
        x48();
        x49();
        x50();
        x51();
        x52();
        X53Class.x53();
        x54();
        randomTimeTest();

        sleepTest();

        // Give the finalizers (if any) a chance to run
        Thread.yield();

        System.out.print("Finished tests\n");
        System.exit(12345);
    }

    static void passed(String name) {
        if (VM.isVerbose()) {
            VM.print("Test ");
            VM.print(name);
            VM.print(" passed\n");
        }
    }

    static void failed(String name) {
        VM.print("Test ");
        VM.print(name);
        VM.print(" FAILED\n");
        System.exit(54321);
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
    static void x2_1() { result("x2_1", Integer.toString(2).equals("2"));     }
    static void x2_2() { result("x2_2", Long.toString(2L).equals("2"));       }
    static void x2_3() { result("x2_3", String.valueOf(true).equals("true")); }
    static void x2_4() { result("x2_4", String.valueOf('2').equals("2"));     }

/*if[FLOATS]*/
    static void x2_5() { result("x2_5", Double.toString(2.0d).equals("2.0")); }
    static void x2_6() { result("x2_6", Float.toString(2.0f).equals("2.0"));  }
    static void x2_7() { result("x2_7", Double.toString(12345.0d).equals("12345.0")); }
    static void x2_8() { result("x2_8", Float.toString(12345.0f).equals("12345.0")); }
/*end[FLOATS]*/

    static void x3() {
        int four = 4;
        result("x3", -four == -4);
    }

    static void x4() {
        passed("x4");
    }

    static void x5(int n) {
        boolean res = false;
        if (n == 2) {
            res = true;
        }
        result("x5", res);
    }

    static void x6(int n) {
        result("x5", n == 2);
    }

    static void x7(int n) {
        result("x7", 5+n == 7);
    }

    static void x8(int n) {
        result("x8", 5*n == 10);
    }

    static void x9(int n) {
        result("x9", -5*n == -10);
    }

    static void x10() {
        result("x10", -5*two == -10);
    }

    static void x11() {
        for (int i = 0 ; i < 10 ; i++) {
            VM.collectGarbage(false);
        }
        passed("x11");
    }

    static void x12() {
        result("x12", fib(20) == 10946);
    }

    public static int fib (int n) {
        if (n == 0) {
            VM.collectGarbage(false);
        }
        if (n<2) {
            return 1;
        }
        int x = fib(n/2-1);
        int y = fib(n/2);
        if (n%2==0) {
            return x*x+y*y;
        } else {
            return (x+x+y)*y;
        }
    }

    static void x13() {
        result("x13",(!(null instanceof Test)));
    }

    static void x14() {
        result("x14",("a string" instanceof String));
    }

    static void x15() {
        boolean res = true;
        try {
            Class c = (Class)null;
        } catch (Throwable t) {
            res = false;
        }
        result("x15",res);
    }

    static void x16() {
        boolean res = true;
        try {
            (new String[3])[1] = null;
        } catch (Throwable t) {
            res = false;
        }
        result("x16",res);
    }

    static void x20() {
        Test t = new Test();
        result("x20", t != null);
    }


    static void x30() {
        Object[] o = new Object[1];
        result("x30", o != null);
    }


    static void x31() {
        Object[] o = new Object[1];
        o[0] = o;
        result("x31", o[0] == o);
    }

    static void x32() {
        Object[] o1 = new Object[1];
        Object[] o2 = new Object[1];
        o1[0] = o1;
        System.arraycopy(o1, 0, o2, 0, 1);
        result("x32", o2[0] == o1);
    }

    static void x33() {
        Object[] o1 = new Object[2];
        Object[] o2 = new Object[2];
        o1[0] = o1;
        o1[1] = o2;
        System.arraycopy(o1, 0, o2, 0, 2);
        result("x33", o2[0] == o1 && o2[1] == o2);
    }

    static void x34() {
        Object[] o1 = new Object[2];
        String[] o2 = new String[2];
        o1[0] = "Hello";
        o1[1] = "World";
        System.arraycopy(o1, 0, o2, 0, 2);
        result("x34", o2[0].equals("Hello") && o2[1].equals("World"));
    }

    static void x35() {
        Object o = new Throwable();
        result("x35", o != null);
    }

    static void x36() {
        long l = 0xFF;
        int  i = 0xFF;
        result("x36",(l << 32) == 0xFF00000000L && ((long)i << 32) == 0xFF00000000L);
    }

    static void x37() {
        byte[] o1 = new byte[2];
        o1[0] = (byte)-3;
        result("x37", o1[0] == -3 && o1[1] == 0);
    }

    static void x38() {
        Object x = null;
        Object o = new Object();
        java.util.Vector v1 = new java.util.Vector();
        v1.addElement(v1);
        java.util.Vector v2 = new java.util.Vector();
        v2.addElement(v2);
        for (int i = 0 ; i < 1000 ; i++) {
            synchronized(o) {
                synchronized(v2) {
                    x = v1.elementAt(0);
                }
                synchronized(v1) {
                    x = v2.elementAt(0);
                }
            }
        }
        result("x38", true);
    }



    static int x39count;

    static void x39() {
        boolean res = false;
        try {
            x39prim();
        } catch(OutOfMemoryError ex) {
            res = true;
            System.out.println("x39 count = " + x39count);
        }
        result("x39", res && x39count == 1);
    }

    static void x39prim() {
        System.gc();
        int freeWords = (int)GC.freeMemory() / com.sun.squawk.vm.HDR.BYTES_PER_WORD;
        int length = freeWords / 2;
        Object[] last = new Object[length];
        while(true) {
            x39count++;
            Object[] next = new Object[length];
            next[0] = last;
            last = next;
        }
    }

    static int x40count = 0;

    static void x40() {
        boolean res = false;
        try {
            recursiveCall();
        } catch(OutOfMemoryError ex) {
            res = true;
            System.out.println("x40 recursion level = " + x40count);
        }
        result("x40", res);
    }

    static void recursiveCall() {
        x40count++;
        recursiveCall();
    }

    static void x41(Object obj, int val) {
        switch(val) {
            default:
                val++;
                break;
        }
        if (val == 123) {
            x41(obj, 3);
        }
        result("x41", true);
    }

    static void x42() {
        int res = 0;
        FOO foo = new FOO();
        Throwable thro = null;
        try {
            foo.foo(123L, 456L);
        } catch(ClassCastException ex) {
            thro = ex;
            res = 1;
        } catch(Exception ex) {
            thro = ex;
            res = 2;
        } catch(Error ex) {
            thro = ex;
            res = 3;
        }
        if (thro != null) {
            System.out.println("x42 printStackTrace - this should be a java.lang.NullPointerException");
            thro.printStackTrace();
        }
        result("x42", res == 2);
    }

    
    static class FOO {
        FOO xxx = null;
        
        int foo(long a, long b) {
            final Object printme = this;
            new Runnable() {
                public void run() {
                    System.out.println("This will never print: " + printme.toString());
                }
            }.run();

            return (int)(a+b);
        }
        
        public String toString() {
             xxx.xxx();
             return "should never happen";
        }
        
        void xxx() {
        }
        protected void finalize() throws Throwable {
            System.out.println("FOO::finalize()");
        }
    }

    static void x43() {
        try {
           throw new Throwable("foo");
        } catch (Throwable ex) {
        }
        result("x43", true);
    }

    static void x44() {
        String[] arr = null;
        if (arr != null) {
            arr[0].concat("should not reach here");
        }
        result("x44", true);
    }



    static int x45count;

    /**
     * Try to start threads with stacks from 0 .. 200 words.
     */
    static void x45() {
//VM.print("x45 bcount = "); VM.println(VM.branchCount());
        for (int i = 0; i != 200; i++) {
//VM.print("x45 i = "); VM.print(i); VM.print(" bcount = "); VM.println(VM.branchCount());
            VMThread t = x45Thread(i);
            if (t == null) {
                break;
            }
            try {
                t.join();
            } catch (InterruptedException ie) {
            }

        }
        if (x45count != 200) {
            System.out.println("x45 count = " + x45count);
        }
        result("x45", x45count == 200);
    }

    static int threadcounter = 0;
    static VMThread x45Thread(int stackSize) {

        try {
            Thread r = new Thread("x45Thread-" + (threadcounter++)) {
                public void run() {
                    f();
                }

                private void f() {
                    x45count++;
//VM.print("x45 count = "); VM.println(x45count);
                }
            };
            VMThread t = VMThread.asVMThread(r);
            //NativeUnsafe.setInt(t, (int)FieldOffsets.com_sun_squawk_VMThread$stackSize, Math.max(stackSize, VMThread.MIN_STACK_SIZE));
            r.start();
            return t;
        }
        catch (OutOfMemoryError e) {
            System.out.println("x45 out of memory creating/starting thread with stack size = " + stackSize);
            return null;
        }
    }

    static void x46Prim(int stackSize) {
        Thread r = new Thread() {
            public void run() {

            }
        };
        VMThread t = VMThread.asVMThread(r);
        //NativeUnsafe.setInt(t, (int)FieldOffsets.com_sun_squawk_VMThread$stackSize, Math.max(stackSize, VMThread.MIN_STACK_SIZE));
        t.start();
        try {
            t.join();
        } catch (InterruptedException ie) {

        }
    }

    /**
     * This tests that the VM can handle the case where a thread's stack takes up the remaining free memory.
     */
    static void x46() {
        int delta = -50;
        while (true) {
            int stackSize = 0;
            try {
                VM.collectGarbage(false);
                stackSize = ((int)GC.freeMemory() / com.sun.squawk.vm.HDR.BYTES_PER_WORD) + delta;
                x46Prim(stackSize);
                if (VM.isVerbose()) {
                    VM.print("stackSize = ");
                    VM.println(stackSize);
                }
                delta++;
            } catch (OutOfMemoryError oome) {
                break;
            }
        }
        result("x46", true);
    }

    static void randomTimeTest() {
        long start = System.currentTimeMillis();
        long iterations = start & 255;
        iterations = iterations*iterations*iterations;
        VM.print("random time test ("+iterations+" empty loop iterations)... ");
        while (iterations-- > 0) {
            iterations = iterations - 1;
        }
        VM.println(System.currentTimeMillis() - start + "ms");
    }

    static void x47() {
        System.gc();
        int initialRefs = x47countRefs();
        boolean result = x47Prim();
        System.gc();
        result = result && (x47countRefs() - initialRefs) == 2;
        permRefs = null;
        System.gc();
        result = result && (x47countRefs() - initialRefs) == 0;
        result("x47", result);
    }

    /**
     * References to non-heap (i.e. permanent) objects
     */
    static WeakReference[] permRefs;

    static int x47countRefs() {
//        Ref ref = GC.getCollector().references;
////System.out.println("referents:");
//        int count = 0;
//        while (ref != null) {
////System.out.println("  " + ref.get());
//            ref = ref.next;
//            count ++;
//        }
//        return count;

        return -1; // can't do this outside of squawk package?
    }

    static boolean x47Prim() {
        String C = new String("C");
        String F = new String("F");

        WeakReference refA = new WeakReference(new String("A")); // collectable RAM object
        WeakReference refB = new WeakReference("B");             // permanent ROM object
        WeakReference refC = new WeakReference(C);               // non-collectable RAM object
        WeakReference refD = new WeakReference(new String("D")); // collectable RAM object
        WeakReference refE = new WeakReference("E");             // permanent ROM object
        WeakReference refF = new WeakReference(F);               // non-collectable RAM object

        System.gc();
        VMThread.yield();

        if (refA.get() != null) return false;
        if (refB.get() == null) return false;
        if (refC.get() == null) return false;
        if (refD.get() != null) return false;
        if (refE.get() == null) return false;
        if (refF.get() == null) return false;

        permRefs = new WeakReference[] { refB, refE };

        return true;
    }

    static void x48() {
        System.gc();

        int freeWords = ((int)GC.freeMemory() - HDR.arrayHeaderSize) / com.sun.squawk.vm.HDR.BYTES_PER_WORD;

        // Allocate all but one word of remaining memory
        Object[] obj = new Object[freeWords - HDR.BYTES_PER_WORD];

        // These allocations of one word objects should work
        for (int i = 0; i != 3; ++i) {
            Object o = new Object();
        }
        result("x48", true);
    }
    
    static void x49() {
        System.out.println("Date: " + new java.util.Date());
        result("x49", true);
    }
    
    static void testEmptyLoop(int len) {
        final int COUNT = 1000000;
        int sum = 0;
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < len; j++) {
                sum += 3;
            }
        }
    }
    
    static void callInt(int i) {
        
    }
    
    static void callLong(long l) {
        
    }
    
    static void callException(int x) {
        if (x == 789632476) {
            try {
                if (x != Integer.parseInt(Integer.toString(x))) {
                    throw new RuntimeException("It didn't happen");
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    static void x50() {
        final int COUNT = 1000000;
        final int len = 16;
        int sum = 0;
        long start = System.currentTimeMillis();
        long delta;
        
        System.gc();
        
        
        // time empty loop:
        System.gc();
        start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < len; j++) {
                sum += 3;
            }
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("Empty loop timings per " + (len * COUNT) + " = " + delta + "ms");
        
        // time empty loop:
        System.gc();
        start = System.currentTimeMillis();
        testEmptyLoop(len);
        delta = System.currentTimeMillis() - start;
        System.out.println("Empty loop timings(2) per " + (len * COUNT) + " = " + delta + "ms");
        
        // time simple calls:
        System.gc();
        start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            callInt(2);
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("Empty simple call per " + COUNT + " = " + delta + "ms");
        
        // time long calls:
        System.gc();
        start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            callLong(2);
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("Empty long call per " + COUNT + " = " + delta + "ms");
        
        // time exception calls:
        System.gc();
        start = System.currentTimeMillis();
        for (int i = 0; i < COUNT; i++) {
            callException(2);
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("Empty exception calls per " + COUNT + " = " + delta + "ms");
        
    }

    public interface IDictionary {
    }

    static class HD implements IDictionary {
    }

    static class AD implements IDictionary {
    }

    static IDictionary getElements(boolean deepCopy) {
        IDictionary dictThis = null;
        if (deepCopy) {
            dictThis = new HD();
        } else {
            dictThis = new AD();
        }
        if (dictThis == null) {
            return null;
        }
        return dictThis;
    }

    static void x51() {
        result("x51: getElements(true)", getElements(true) instanceof IDictionary);
        result("x51: getElements(false)", getElements(false) instanceof IDictionary);
    }

    static void x52() {
        Runnable r = new Concrete1();
        r.run();
    }
    
    /**
     * Test NativeUnsfe getters and setters
     */
    static class X53Class {

        static int makeShort(int b0, int b1) {
            return (short) ((b0) << 8) | (b1 & 0xFF);
        }

        static int makeInt(int b0, int b1, int b2, int b3) {
            return ((b0) << 24) | ((b1 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b3 & 0xFF);
        }

        static long makeLong(int b0, int b1, int b2, int b3, int b4, int b5, int b6, int b7) {
            return ((long) (b0) << 56) | ((long) (b1 & 0xFF) << 48) | ((long) (b2 & 0xFF) << 40) | ((long) (b3 & 0xFF) << 32)
                    | ((long) (b4 & 0xFF) << 24) | ((b5 & 0xFF) << 16) | ((b6 & 0xFF) << 8) | (b7 & 0xFF);
        }

        static int swap2(short i) {
            return (short) (((i & 0xFF00) >> 8) | (i << 8));
        }

        static int swap4(int i) {
            return ((i >>> 24))
                    | ((i >> 8) & 0xFF00)
                    | ((i << 8) & 0xFF0000)
                    | ((i << 24));
        }

        static long swap8(long i) {
            i = (i & 0x00ff00ff00ff00ffL) << 8 | (i >>> 8) & 0x00ff00ff00ff00ffL;
            return (i << 48) | ((i & 0xffff0000L) << 16)
                    | ((i >>> 16) & 0xffff0000L) | (i >>> 48);
        }

        static int bigShort(short value) {
            return (VM.isBigEndian()) ? value : swap2(value);
        }

        static int bigInt(int value) {
            return (VM.isBigEndian()) ? value : swap4(value);
        }

        static long bigLong(long value) {
            return (VM.isBigEndian()) ? value : swap8(value);
        }

        static void testByteGetter(String msg, Address ptr, byte[] buffer) {
            for (int i = 0; i < buffer.length; i++) {
                expect(msg + "getByte()", buffer[i], NativeUnsafe.getByte(ptr, i));
            }
        }

        static void clearBuffer(Address ptr, int length) {
            for (int i = 0; i < length; i++) {
                NativeUnsafe.setByte(ptr, i, 0);
                expect("clearBuffer()", 0, NativeUnsafe.getByte(ptr, i));
            }
        }
        
        static void printBuffer(Address ptr, int length) {
            for (int i = 0; i < length; i++) {
                System.err.print(Integer.toHexString(NativeUnsafe.getByte(ptr, i)) + "    ");
            }
            System.err.println();
        }
                
        /**
         * Clear buffer, and set default values for bytes not covered by the aligned writes that we are testing
         */
        static void resetBuffer(Address ptr, byte[] buffer, int size, boolean unaligned) {
            clearBuffer(ptr, buffer.length);
            int start = (buffer.length / size) * size;
            if (unaligned) {
                NativeUnsafe.setByte(ptr, 0, buffer[0]); // for unaligned, don't forget to set beginning/end
                start++;
            }
            for (int i = start; i < buffer.length; i++) {
                NativeUnsafe.setByte(ptr, i, buffer[i]);
            }
        }

        static void testAllGetters(String msg, Address ptr, byte[] buffer) {
            // bytes:
            testByteGetter(msg, ptr, buffer); // test getByte()

            for (int i = 0; i < buffer.length; i++) {
                expect(msg + "getUByte()", (buffer[i] & 0xFF), NativeUnsafe.getUByte(ptr, i));
            }

            for (int i = 0; i < buffer.length; i++) {
                expect(msg + "getAsByte()", buffer[i], NativeUnsafe.getAsByte(ptr, i));
            }

            // shorts:
            for (int i = 0; i < (buffer.length / 2); i++) {
                int expected = makeShort(buffer[i * 2], buffer[i * 2 + 1]);
                int actual = bigShort((short) NativeUnsafe.getShort(ptr, i));
                expect(msg + "getShort()", expected, actual);
            }

            for (int i = 0; i < (buffer.length / 2); i++) {
                int expected = makeShort(buffer[i * 2], buffer[i * 2 + 1]);
                int actual = bigShort((short) NativeUnsafe.getAsShort(ptr, i));
                expect(msg + "getAsShort()", expected, actual);
            }

            for (int i = 0; i < (buffer.length / 2); i++) {
                int expected = makeShort(buffer[i * 2 + 1], buffer[i * 2 + 2]);
                int actual = bigShort((short) NativeUnsafe.getUnalignedShort(ptr, i * 2 + 1));
                expect(msg + "getUnalignedShort()", expected, actual);
            }

            // ints:
            for (int i = 0; i < (buffer.length / 4); i++) {
                int bindex = i * 4;
                int expected = makeInt(buffer[bindex], buffer[bindex + 1], buffer[bindex + 2], buffer[bindex + 3]);
                int actual = bigInt(NativeUnsafe.getInt(ptr, i));
                expect(msg + "getInt()", expected, actual);
            }

            for (int i = 0; i < (buffer.length / 4); i++) {
                int bindex = i * 4;
                int expected = makeInt(buffer[bindex], buffer[bindex + 1], buffer[bindex + 2], buffer[bindex + 3]);
                int actual = bigInt(NativeUnsafe.getAsInt(ptr, i));
                expect(msg + "getAsInt()", expected, actual);
            }

            for (int i = 0; i < (buffer.length / 4); i++) {
                int bindex = i * 4 + 1;
                int expected = makeInt(buffer[bindex], buffer[bindex + 1], buffer[bindex + 2], buffer[bindex + 3]);
                int actual = bigInt(NativeUnsafe.getUnalignedInt(ptr, bindex));
                expect(msg + "getUnalignedInt()", expected, actual);
            }

            // long:
            for (int i = 0; i < (buffer.length / 8); i++) {
                int bindex = i * 8;
                long expected = makeLong(buffer[bindex], buffer[bindex + 1], buffer[bindex + 2], buffer[bindex + 3],
                        buffer[bindex + 4], buffer[bindex + 5], buffer[bindex + 6], buffer[bindex + 7]);
                long actual = bigLong(NativeUnsafe.getLong(ptr, i));
                expect(msg + "getLong()", expected, actual);
            }

            for (int i = 0; i < (buffer.length / 8); i++) {
                int bindex = i * 8 + 1;
                long expected = makeLong(buffer[bindex], buffer[bindex + 1], buffer[bindex + 2], buffer[bindex + 3],
                        buffer[bindex + 4], buffer[bindex + 5], buffer[bindex + 6], buffer[bindex + 7]);
                long actual = bigLong(NativeUnsafe.getUnalignedLong(ptr, bindex));
                expect(msg + "getUnalignedLong()", expected, actual);
            }
        }

        /**
         * Test unsafe memory access.
         * Write pattern into native buffer, read using all getters.
         * The write same pattern with various setters and verify
         * NOTE: Writing in big-endian format, so need to translate pattern on little-endian systems...
         */
        static void x53() {
            //________ index______ 0  1  2  3  4  5  6  7  0  11  12  13  14  15  16  17  18
            final byte[] BUFFER = {1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -2, -3, -4, -5, -6, -7, -8};

            // test utility funcs:
            expect("x53: makeShort()", 0x00000102, makeShort(1, 2));
            expect("x53: makeInt()", 0x01020304, makeInt(1, 2, 3, 4));
            expect("x53: makeLong()", 0x0102030405060708L, makeLong(1, 2, 3, 4, 5, 6, 7, 8));
            expect("x53: makeLong(2)", 0x09FFFEFDFCFBFAF9L, makeLong(9, -1, -2, -3, -4, -5, -6, -7));

            expect("x53: swap2()", 0x0201, swap2((short) 0x0102));
            expect("x53: swap4()", 0x04030201, swap4(0x01020304));
            expect("x53: swap8()", 0x0807060504030201L, swap8(0x0102030405060708L));
            expect("x53: swap2(-1)", -1, swap2((short) -1));
            expect("x53: swap4(-1)", -1, swap4(-1));
            expect("x53: swap8(-1)", -1, swap8(-1L));

            expect("x53: swap2(-)", -21, swap2((short) 0xEBFF));
            expect("x53: swap4(-)", -21, swap4(0xEBFFFFFF));
            expect("x53: swap8(-)", -21, swap8(0xEBFFFFFFFFFFFFFFL));

            // set up test data:
            Address ptr = NativeUnsafe.malloc(UWord.fromPrimitive(BUFFER.length));
            for (int i = 0; i < BUFFER.length; i++) {
                NativeUnsafe.setByte(ptr, i, BUFFER[i]);
            }

            testAllGetters("x53: ", ptr, BUFFER);

            // test setters:

            // set shorts:
            resetBuffer(ptr, BUFFER, 2, false);
            for (int i = 0; i < (BUFFER.length / 2); i++) {
                int bindex = i * 2;
                int expected = makeShort(BUFFER[bindex], BUFFER[bindex + 1]);
                NativeUnsafe.setShort(ptr, i, bigShort((short) expected));
            }
            testByteGetter("x53: after setShort(): ", ptr, BUFFER);

            resetBuffer(ptr, BUFFER, 2, true);
            for (int i = 0; i < (BUFFER.length / 2); i++) {
                int bindex = i * 2 + 1;
                int expected = makeShort(BUFFER[bindex], BUFFER[bindex + 1]);
                NativeUnsafe.setUnalignedShort(ptr, bindex, bigShort((short) expected));
            }
            testByteGetter("x53: after setUnalignedShort(): ", ptr, BUFFER);

            // ints:
            resetBuffer(ptr, BUFFER, 4, false);
            for (int i = 0; i < (BUFFER.length / 4); i++) {
                int bindex = i * 4;
                int expected = makeInt(BUFFER[bindex], BUFFER[bindex + 1], BUFFER[bindex + 2], BUFFER[bindex + 3]);
                NativeUnsafe.setInt(ptr, i, bigInt(expected));
            }
            testByteGetter("x53: after setInt(): ", ptr, BUFFER);

            resetBuffer(ptr, BUFFER, 4, true);
            for (int i = 0; i < (BUFFER.length / 4); i++) {
                int bindex = i * 4 + 1;
                int expected = makeInt(BUFFER[bindex], BUFFER[bindex + 1], BUFFER[bindex + 2], BUFFER[bindex + 3]);
                NativeUnsafe.setUnalignedInt(ptr, bindex, bigInt(expected));
            }
            testByteGetter("x53: after setUnalignedInt(): ", ptr, BUFFER);

            // long:
            resetBuffer(ptr, BUFFER, 8, false);
            for (int i = 0; i < (BUFFER.length / 8); i++) {
                int bindex = i * 8;
                long expected = makeLong(BUFFER[bindex],     BUFFER[bindex + 1], BUFFER[bindex + 2], BUFFER[bindex + 3],
                                         BUFFER[bindex + 4], BUFFER[bindex + 5], BUFFER[bindex + 6], BUFFER[bindex + 7]);
                NativeUnsafe.setLong(ptr, i, bigLong(expected));
            }
            testByteGetter("x53: after setLong(): ", ptr, BUFFER);

            resetBuffer(ptr, BUFFER, 8, true);
            for (int i = 0; i < (BUFFER.length / 8); i++) {
                int bindex = i * 8 + 1;
                long expected = makeLong(BUFFER[bindex],     BUFFER[bindex + 1], BUFFER[bindex + 2], BUFFER[bindex + 3],
                                         BUFFER[bindex + 4], BUFFER[bindex + 5], BUFFER[bindex + 6], BUFFER[bindex + 7]);
                NativeUnsafe.setUnalignedLong(ptr, bindex, bigLong(expected));
            }
            testByteGetter("x53: after setUnalignedLong(): ", ptr, BUFFER);
        }
    }

    private static void x54() {
        final int PATTERN = 0xAAAAAAAA;
        final int MAX = 0xFFFFFFFF;
        final long PATTERN_L = 0xAAAAAAAAAAAAAAAAL;
        final long PATTERN_2 = 0xAAAAAAAAFFFFFFFFL;
        
        long l1 = VM.makeLong(PATTERN, PATTERN);
        expect("makeLong() 1", PATTERN_L, l1);
        
        expect("getHi() 1", PATTERN, VM.getHi(l1));
        expect("getLo() 1", PATTERN, VM.getLo(l1));
        
        l1 = VM.makeLong(PATTERN, MAX);
        expect("makeLong() 2", PATTERN_2, l1);
        
        expect("getHi() 2", PATTERN, VM.getHi(l1));
        expect("getLo() 2",     MAX, VM.getLo(l1));
    }
    
    static void sleepTest() {
        try {
            System.out.println("Current time is: " + new java.util.Date());
            VMThread.sleep(1000);
            System.out.println("After sleep 1 second, time is: " + new java.util.Date());
            VMThread.sleep(3000);
            System.out.println("After sleep 3 seconds, time is: " + new java.util.Date());
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private Test() {
    }
}

class Base1 implements Runnable {
    public void run() { System.out.println("ERROR: In Base1.run()"); }
}

abstract class Mid1 extends Base1 implements Runnable {
        public void run() { System.out.println("In Mid1.run()"); }
}

class Concrete1 extends Mid1 {
}
