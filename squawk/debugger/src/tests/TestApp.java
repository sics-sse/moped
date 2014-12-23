//if[!FLASH_MEMORY]
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

package tests;

class Base {
    static int x = 0;
    static int y = 0;
    static int z = 0;
}

class Instance {
       int x = 0;
     int y = 0;
     int z = 0;  
     
     Instance() {
         x = 1;
         y = 2;
         z = 3;
     }
     
     public String toString() {
         return "<x: " + x + ", y: " + y + ", z: " + z + ">";
     }
}

public class TestApp extends Base {
    // primitives
    static byte b = 5;
    static short s = 2;
    static long l = -1;

    // objects:
    static String str = "ABC";
    // objects:
    static Object obj = new Object();
    int[] intarray = {1, 2, 3};
    static String[] strarray = {"foo", "bar", "baz"};

    // primitive objects
    static Boolean booleanObj = new Boolean(true);
    static Byte byteObj = new Byte((byte)111);
    static Short shortObj = new Short((short)2222);
    static Character charObj = new Character('Z');
    static Integer intObj = new Integer(4444);
    static Long longObj = new Long(8888);

    static Class thisClass = TestApp.class;
    static Thread thisThread = Thread.currentThread();
    
    static Object base = new Base();

    static void zorch1() {
        // zorchs should never appear on stack trace
    }

    static void f1(int i) {
        if (!(intObj instanceof Integer)) {
            throw new RuntimeException("inobj = " + intObj);
        }
        f2(i + 1);
    }

    static void zorch2() {
    }

    static void f2(int i) {
        x = 2; // create a different offset for each call.
        f3(i + 1);
    }

    static void zorch3() {
    }

    static void f3(int i) {
        x = 2;
        y = 2;
        f4(i + 1);
    }

    static void zorch4() {
    }

    static void f4(int i) {
        x = 2;
        y = 2;
        z = 2;
        f5(i + 1);
    }

    static void zorch5() {
    }

    static void f5(int i) {
        x += y;
        y += z;
        z += x+y;
        f6(i + 1);
        i = x;
        throw new RuntimeException("This is the expected exception in the TestApp.");
    }

    static void zorch6() {
    }

    static void f6(int i) {
        for (int j = 0; j < 2; j++) {
            int k = j;
        }

        int k = 2;
        while (k > 0) {
            k--;
        }

        int m = 2;
        do {
            m--;
        } while(m > 0);

        f7(i + 1);
    }

    static void f7(int i) {
        int j = 1;
        switch (j) {
            case 1:
                i = 1;
            case 2:
                i = 2;
                break;
            case 3:
                i = 3;
                break;

        }
        j = 4;

        f8(i + 1);
    }

    static void f8(int i) {
        for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
                i = i + 1;
            }
        }

        int j = 2;
        while (j > 0) {
            int k = 2;
            while (k > 0) {
                k--;
            }
            j--;
        }

        j = 2;
        while (j > 0) {
            for (int k = 0; k < 2; k++) {
                i = i + 1;
            }
            j--;
        }

        for (int l = 0; l < 2; l++) {
            int k = 2;
            while (k > 0) {
                k--;
            }
        }

        for (int l = 0; l < 2; l++) {
            int m = 2;
            do {
                m--;
            } while(m > 0);
        }

        f9(f9a(), f9b(), f9(f9a(), f9b(), 12));
        for (int p = 0; p < 2; p++) { }

    }

    static int f9(int i, int j, int k) {
        int l = i + j + k;
        return l;
    }

    static int f9a() {
        return 10;
    }

    static int f9b() {
        return 11;
    }

/*if[FLOATS]*/
    public static class Ball {
        /**
         * The veolicty of the ball, roughly in units of LEDs / msec.
         */
        private double v = 0.0;
        /**
         * Position of the ball, in units of LEDs.
         */
        private double x = 3.5;   //initial position is about halfway along the LEDs.
        /**
         * Position as last animation frame, an optimization for collsion detection
         */
        private double oldX = 3.5;

        /** Creates a new instance of Ball */
        public Ball() {
        }

        /**
         * Returns the ball velocity
         * @return The current velocity in units of LEDs / msec.
         */
        public double getV() {
            return v;
        }

        /**
         * Set the ball velocity.
         * @param v The new value for velocity
         */
        public void setV(double v) {
            this.v = v;
        }

        /**
         * Returns the current position
         * @return The current position in units of LEDs.
         */
        public double getX() {
            return x;
        }

        /**
         * Sets the current position
         * @param x2 The desired new position in units of LEDs
         */
        public void setX(double x2) {
            oldX = x;
            this.x = x2;
        }

        public double getOldX() {
            return oldX;
        }
    }

    static double getD1() {
        return 1.0;
    }

    static void testDoubles(double p1) {
        final double friction = 0.0005;
        final double dt = 10;

        double aX = 0.0;
        Ball ball = new Ball();

        aX = getD1();


        double v = ball.getV();
        double x = ball.getX();

        // standard Physics 101 here ...
        aX = aX - friction * v;           //diminish accel by friction
        v = v + aX * dt;
        x = x + v * dt + 0.5 * aX * dt * dt;
    }

    static void testFP(double m, double n, int o, double p) {
        double a = 6.0;
        int b = 7;
        double c = 8.0;
        double d = 9.0;

        System.out.println("m: " + m + " n: " + n + " o: " + o + " p: " + p);
        System.out.println("a: " + a + " b: " + b + " c: " + c + " d: " + d);
        System.out.println();
        System.out.println("BITS: m: " + Double.doubleToLongBits(m) + " n: " + Double.doubleToLongBits(n) + " o: " + o + " p: " + Double.doubleToLongBits(p));
        System.out.println("BITS a: " + Double.doubleToLongBits(a) + " b: " + b + " c: " + Double.doubleToLongBits(c) + " d: " + Double.doubleToLongBits(d));
        System.out.println();
        b = 8;

        System.out.println("m: " + m + " n: " + n + " o: " + o + " p: " + p);
        System.out.println("a: " + a + " b: " + b + " c: " + c + " d: " + d);
        System.out.println();
        System.out.println("BITS: m: " + Double.doubleToLongBits(m) + " n: " + Double.doubleToLongBits(n) + " o: " + o + " p: " + Double.doubleToLongBits(p));
        System.out.println("BITS a: " + Double.doubleToLongBits(a) + " b: " + b + " c: " + Double.doubleToLongBits(c) + " d: " + Double.doubleToLongBits(d));
    }


/*end[FLOATS]*/

     static void testLong(long m, long n, int o, long p) {
        long a = 6;
        int b = 7;
        long c = 8;
        long d = 9;

        System.out.println("m: " + m + " n: " + n + " o: " + o + " p: " + p);
        System.out.println("a: " + a + " b: " + b + " c: " + c + " d: " + d);
        System.out.println();
        b = 8;

        System.out.println("m: " + m + " n: " + n + " o: " + o + " p: " + p);
        System.out.println("a: " + a + " b: " + b + " c: " + c + " d: " + d);
    }

      public static class LongBall {

        private long v = 0x11;

        private long x = 0x1122334455667788L;

        private long oldX = 0x22;

        public long getV() { return v; }

        public long getX() { return x; }

        public long getOldX() { return oldX; }
    }

      
    static void testLongs(long p1) {
        final long friction = 0x0011223344556677L;
        final long dt = 0x1122334455667788L;

        long aX = 0;
        LongBall ball = new LongBall();

        long v = ball.getV();
        long x = 0x22;

        // standard Physics 101 here ...
        aX = aX - friction * v;           //diminish accel by friction
        v = v + aX * dt;
        x = x + v * dt + 5 * aX * dt * dt;
    }

    static void mainLoop(boolean runExceptionThread) {
        int count = 0;
        System.err.println("Entering test loop...");
        
/*if[FLOATS]*/
        testDoubles(3.5);
        testFP(1.0, 2.0, 3, 4.0);
/*end[FLOATS]*/
        testLongs(0x1122334455667788L);
        testLong(1, 2, 3, 4);

        while (true) {
            System.err.println("Loop iteration: " + count);
            try {
                f1(1);
            } catch (Exception e) {
                System.err.println("caught exception.");
            } finally {
                System.err.println("finally.");
            }
            count++;

            if (count % 5 == 0) {
                System.gc();

                if (runExceptionThread) {
                    new ExceptionThread().start();

                    System.out.println("for execution point in static initializer");
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }
    
    static void valCheck(long v) {
        System.out.println("v is " + v);
    }

    static void valCheck(Object v) {
        System.out.println("v is " + v);
    }
    
    static void testVals() {
        System.out.println("Check static fields");
        valCheck(b);
        valCheck(b);
        valCheck(b);
        
        valCheck(s);
        valCheck(s);
        valCheck(s);
        
        valCheck(l);
        valCheck(l);
        valCheck(l);
        
        valCheck(str);
        valCheck(str);
        valCheck(str);
        
        valCheck(obj);
        valCheck(obj);
        valCheck(obj);
        
        valCheck(strarray[1]);
        valCheck(strarray[1]);
        valCheck(strarray[1]);
        
        System.out.println("Check instance fields");
        Object foo = new Instance();
        System.out.println("foo: " + foo);
        System.out.println("foo: " + foo);
        System.out.println("foo: " + foo);
        
        System.out.println("booleanObj: " + booleanObj);
        System.out.println("booleanObj: " + booleanObj);
        System.out.println("booleanObj: " + booleanObj);
        
        System.out.println("byteObj: " + byteObj);
        System.out.println("byteObj: " + byteObj);
        System.out.println("byteObj: " + byteObj);
        
        System.out.println("shortObj: " + shortObj);
        System.out.println("shortObj: " + shortObj);
        System.out.println("shortObj: " + shortObj);
        
        System.out.println("charObj: " + charObj);
        System.out.println("charObj: " + charObj);
        System.out.println("charObj: " + charObj);
        
        System.out.println("intObj: " + intObj);
        System.out.println("intObj: " + intObj);
        System.out.println("intObj: " + intObj);
        
        System.out.println("longObj: " + longObj);
        System.out.println("longObj: " + longObj);
        System.out.println("longObj: " + longObj);
    }
    
    static class ExceptionThread extends Thread {
        static int counter;
        public void run() {
            int j = StaticInitializer.value;
            for (int i = 0; i != 10; i++) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
                System.err.println("spin " + counter + ": " + i);
            }
            counter++;
            throw new RuntimeException("uncaught exception");
        }
    }
    
    static void testMT() {
        System.out.println("Hello World");
        Runnable r=new Runnable() {
            public void run() {
                while (true) {
                    Thread.yield();
                }
            }
        };
        Runnable r1=new Runnable() {
            public void run() {
                while (true) {
                    Thread.yield();
                }
            }
        };
        (new Thread(r)).start();
        (new Thread(r1)).start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
    }
//    public void run() {
//        mainLoop();
//    }

    public static void main(String[] args) {
        testVals();
        testMT();
        mainLoop(args.length == 0);
    }
} // TestIsolate

class StaticInitializer {
    static int showInitializer() {
        System.out.println("in StaticInitializer.<clinit>");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }
        return 5;
    }
    static int value = showInitializer();
}
