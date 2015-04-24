//if[!FLASH_MEMORY]
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

package com.sun.squawk;

import java.lang.ref.*;

import com.sun.squawk.vm.*;

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
        x47();
        x48();
        x49();
        x50();
        x51();
        x52();
        x53();
        randomTimeTest();
	sleepTest();

        // Give the finalizers (if any) a chance to run
        VMThread.yield();

        VM.print("Finished tests\n");
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
        VM.print(" failed\n");
        System.exit(54321);
    }

    static void result(String name, boolean b) {
        if (b) {
            passed(name);
        } else {
            failed(name);
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
            Klass c = (Klass)null;
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
            NativeUnsafe.setInt(t, (int)FieldOffsets.com_sun_squawk_VMThread$stackSize, Math.max(stackSize, VMThread.MIN_STACK_SIZE));
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
        NativeUnsafe.setInt(t, (int)FieldOffsets.com_sun_squawk_VMThread$stackSize, Math.max(stackSize, VMThread.MIN_STACK_SIZE));
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
        Ref ref = GC.getCollector().references;
//System.out.println("referents:");
        int count = 0;
        while (ref != null) {
//System.out.println("  " + ref.get());
            ref = ref.next;
            count ++;
        }
        return count;
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

	static void convertBytes(String enc) {
		byte[] bytes = new byte[2048];

		int b = '0';
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte)b++;
			if (b > 127) {
				b = '0';
			}
		}
		System.gc();
		long tm = System.currentTimeMillis();
		try {
			for (int i = 0; i < 10; i++) {
				String str = new String(bytes, enc);
			}
		} catch (java.io.UnsupportedEncodingException ex) {
			System.out.println(enc + " encoder not supported in this configuration: " + ex);
		}
		tm = System.currentTimeMillis() - tm;
		System.out.println(enc + " conversions took " + tm + "ms");
	}
		
    static void printArray(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            System.err.print(b[i]);
            System.err.print(' ');
        }
    }
    
	static void x53() {
		final String ASCII_STR = "Hello World!";
		final String NON_ASCII_STR = new StringBuffer().append('\u00BF').append(' ').append('\u0394').append('\u00A3').append('\u00AC').append('\u0394').append('\u20AC').append('?').toString();
		final String UTF_8_ENCODER = "UTF-8";
		final String DEFAULT_ENCODER = "ISO8859_1";

		String tmp;
		boolean result = true;
		byte[] bytes;

		bytes = ASCII_STR.getBytes();
		if (bytes.length != ASCII_STR.length()) {
			System.out.println("default encoder looks broken");
			result = false;
		}
		tmp = new String(bytes);
		if (!ASCII_STR.equals(tmp)) {
			System.out.println("default encoder/decoder looks broken: " + tmp);
			result = false;
		}

		convertBytes(DEFAULT_ENCODER);

		try {
			bytes = ASCII_STR.getBytes(UTF_8_ENCODER);
			if (bytes.length != ASCII_STR.length()) {
				System.out.println("utf-8 encoder looks broken");
				result = false;
			}
			tmp = new String(bytes, UTF_8_ENCODER);
			if (!ASCII_STR.equals(tmp)) {
				System.out.println("utf-8 encoder/decoder looks broken: " + tmp);
				result = false;
			}

			bytes = NON_ASCII_STR.getBytes(UTF_8_ENCODER);
			if (bytes.length == NON_ASCII_STR.length()) {
				System.out.println("utf-8 encoder looks broken (b). NON_ASCII_STR: " + NON_ASCII_STR);
				System.out.println("    NON_ASCII_STR.length: " + NON_ASCII_STR.length());
				System.out.println("    bytes.length: " + bytes.length);
                printArray(bytes);
				result = false;
			}
			tmp = new String(bytes, UTF_8_ENCODER);
			if (!NON_ASCII_STR.equals(tmp)) {
				System.out.println("utf-8 encoder/decoder looks broken: " + tmp);
				result = false;
			}
			convertBytes(UTF_8_ENCODER);
		
		} catch (java.io.UnsupportedEncodingException ex) {
			System.out.println("UTF-8 encoder not supported in this configuration: " + ex);
		}

		result("x53", result);
	}

    static void sleepTest() {

        System.out.println("Current time is: " + new java.util.Date());
        VM.waitForEvent(1000);
        System.out.println("After sleep 1 second, time is: " + new java.util.Date());
        VM.waitForEvent(3000);
        System.out.println("After sleep 3 seconds, time is: " + new java.util.Date());

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
