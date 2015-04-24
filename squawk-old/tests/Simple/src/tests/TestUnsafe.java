/*
 * TestUnsafe.java
 *
 * Created on September 5, 2007, 11:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

import com.sun.squawk.*;


/**
 * Tests peek/poke mechanism from non-image class.
 *
 * @author dw29446
 */
public class TestUnsafe {
    
    final static String DATAB = "ABCDEFGHIJKLMNOP";
    
    final static int COUNT = 1000000;

    static void peekIntArray(Address raw, int offset, int length) {
        int end = offset + length;
        for (int i = offset; i != end; ++i) {
            System.out.println("Unsafe.getByte(raw, " + i + ") = " + Unsafe.getByte(raw, i));
        }
    }
    
    static void testEmptyLoop(int len) {
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

    public static void main(String[] args) {
        int len = DATAB.length();
        int sum = 0;
        long start = System.currentTimeMillis();
        long delta;
        
        System.gc();
        // sanity tests:
        Address raw = Address.fromObject(DATAB);
        peekIntArray(raw, 0, len);
        
        for (int j = 0; j < len; j++) {
            if ((char)Unsafe.getByte(raw, j) != DATAB.charAt(j)) {
                System.err.println("Bad data read. getByte said " + ((char)Unsafe.getByte(raw, j)) + " but charAt() said " + DATAB.charAt(j));
            }
        }
        
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
        
        // timing tests:
        System.gc();
        start = System.currentTimeMillis();
        raw = Address.fromObject(DATAB);
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < len; j++) {
                sum += Unsafe.getByte(raw, j);
            }
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("Unsfae.getByte() timings per " + (len * COUNT) + " = " + delta + "ms");
        
         // timing tests:
        System.gc();
        start = System.currentTimeMillis();
        raw = Address.fromObject(DATAB);
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < (len / 4); j++) {
                sum += Unsafe.getInt(raw, j);
            }
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("Unsfae.getInt() timings per " + ((len / 4) * COUNT) + " = " + delta + "ms");
        
        
        // timing compared:
        System.gc();
        start = System.currentTimeMillis();
        raw = Address.fromObject(DATAB);
        for (int i = 0; i < COUNT; i++) {
            for (int j = 0; j < len; j++) {
                sum += DATAB.charAt(j);
            }
        }
        delta = System.currentTimeMillis() - start;
        System.out.println("String.charAt() timings per " + (len * COUNT) + " = " + delta + "ms");
        
     /*   arr[0] = 555;
        Unsafe.setInt(raw, 1, 666);
        peekIntArray(raw, 0, 2);*/

        
    }

    private TestUnsafe() {
    }

}
