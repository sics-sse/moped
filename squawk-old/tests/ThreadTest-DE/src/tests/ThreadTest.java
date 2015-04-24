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
import java.util.Hashtable;

/**
 * ThreadTest effects of changing the system clock on thread scheduling. 
 * 
 * Run both periodic tasks, which should be based on relative time, and absolute tasks, which are based on clock time (currentTimeMillis).
 * 
 * NOTE: This test relies on debug code in the VM that is only enabled when property DEBUG_CODE_ENABLED is true.
 */
public class ThreadTest extends MiniTestHelper {
    final static int SLOP = 100; // ms that wakeups are allowed to be early or late
    
    final static int INTERVAL = 400; // interval in ms between absolute tasks
    final static int ABSOLUTE_COUNT = 25; // number of absolute time tasks
    
    final static int PERIOD = 200; // period in ms of periodic task
    final static int PERIODIC_COUNT = 10 + (ABSOLUTE_COUNT * INTERVAL) / PERIOD;
    
    final static int ADJUST1 = SLOP * 2; // try adjusting clock by small ammount
    final static int ADJUST2 = INTERVAL * 3; // try adjusting clock by large ammount
    final static int ADJUST_INTERVAL = 1800; // time between adjusting clock

    /**
     * If the clock is moved forwards, absolute time tasks may be late. There's no helping that, so add extra slop so tests don't fail.
     */
    public static volatile int currentSlop = SLOP; 

    public ThreadTest() {
        super("ThreadTest");
    }
    
    static void printLog(String msg) {
        long absTime = VM.getTimeMillis();
        long relTime = VM.relativeTimeMillis();
        System.out.print(Thread.currentThread().getName());
        System.out.print(": absTime: ");
        System.out.print(absTime);
        System.out.print(", relTime: ");
        System.out.print(relTime);
        System.out.print(".    ");
        System.out.println(msg);
    }

    static void delay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Wait until the clock is "msTime". Will re-delay if clock change results in early wake-up.
     * @param msTime 
     */
    static void waitUntil(long msTime) {
        long delay = msTime - System.currentTimeMillis();
        while (delay > 0) {
            delay(delay);
            //printLog("waitUntil: woke up");
            delay = msTime - System.currentTimeMillis();
        }
        printLog("scheduled");
        expect("late delay", 0, delay, currentSlop); 
    }

    /**
     * Wait for msDelay ms. Will re-delay if clock change results in early wake-up.
     * @param msDelay 
     */
    static void waitFor(long msDelay) {
        if (msDelay < 0) {
            throw new IllegalArgumentException("negative delay");
        }
        long targetTime = VM.relativeTimeMillis() + msDelay;
        long delay = msDelay;
        do {
            delay(delay);
            // printLog("waitFor: woke up");
            delay = targetTime - VM.relativeTimeMillis();
        } while (delay > 0);
        printLog("scheduled");
        expect("late delay", 0, delay, SLOP); 
    }
    
    static void setSystemClockMock(long newTime) {
        System.out.println("-----------------------------");
        printLog("setting time to : " + newTime);
        VM.setSystemClockMock(newTime);
        printLog("time now set");
        System.out.println("-----------------------------");
    }
    
    static class PeriodicRunnable extends TestRunner {
        private static volatile boolean enabled = false;
        
        public static void enable(boolean enable) {
            enabled = enable;
        }

        /**
         * Test waits to be enabled, then runs periodically. 
         * Note that this schedules each new period from the END of the last period, so skew is expected.
         */
        public void test() {
            int i = 0;
            System.out.println("Schedule " + PERIODIC_COUNT + " periodic tasks - every " + PERIOD + "ms starting from " + System.currentTimeMillis());
            System.out.flush();

            while (!enabled) {
                Thread.yield();
            }
            
            do {
                waitFor(PERIOD);
                i++;
            } while (enabled && i < PERIODIC_COUNT);
            
            printLog("Done test thread");
        }
    }

    static class AbsoluteRunnable extends TestRunner {
        long targetTime;
        static final Hashtable threads = new Hashtable();

        AbsoluteRunnable(long targetTime) {
            this.targetTime = targetTime;
            threads.put(this, this);
        }

        public void test() {
            waitUntil(targetTime);
            
            threads.remove(this);
            if (threads.isEmpty()) {
                PeriodicRunnable.enable(false);
                printLog("All absolute threads done");
            }
        }
    }
        
    public void test() throws TestFailedException {
        long now;
        Thread p = new Thread(new PeriodicRunnable(), "periodic-----"); // run this thread at 10ms interval

        VM.setSystemClockMockInit(1000); // set time to nominal value to make eyballing results easier
        
        p.start();
        delay(10); // let p run to get output to look pretty
        
        // create schedule:
        System.out.println("Schedule for absolute tasks:");
        now = System.currentTimeMillis();
        for (int i = 0; i < ABSOLUTE_COUNT; i++) {
            long targetTime = now + (INTERVAL * (i + 1)) + 500; // offset 500ms to allow all threads to start up before deadline passes
            Thread t = new Thread(new AbsoluteRunnable(targetTime), ("absolute-" + targetTime));
            System.out.println("   " + targetTime + ": " + t);
            t.start();
        }
        System.gc();
        
        PeriodicRunnable.enable(true);

        // run for a while
        delay(ADJUST_INTERVAL);

        // move time up little:
        currentSlop = SLOP + ADJUST1;
        setSystemClockMock(System.currentTimeMillis() + ADJUST1);
        delay(ADJUST_INTERVAL);

        // move time back little:
        currentSlop = SLOP;
        setSystemClockMock(System.currentTimeMillis() - ADJUST1);
        delay(ADJUST_INTERVAL);

        // move time up a lot:
        currentSlop = SLOP + ADJUST2;          // this scheme isn't really thread safe...
        setSystemClockMock(System.currentTimeMillis() + ADJUST2);
        delay(ADJUST_INTERVAL);
        currentSlop = SLOP;

        // move time back a lot:
        setSystemClockMock(System.currentTimeMillis() - ADJUST2);
        delay(ADJUST_INTERVAL);

        try {
            p.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
}
