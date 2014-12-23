package com.sun.squawk.test;


/*
 * Test ECho Server with 3 servers and 5 cleints each
 *
 * Created on Mar 5, 2009 3:37:29 PM;
 */


import com.sun.cldc.jna.*;
import com.sun.squawk.VM;
import java.io.IOException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;


/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class StartApplication extends MIDlet {

    void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    void testTime() {
        long t1, t2;
        long sum;
        long max;
        long min;
        int i;

        sum = 0;
        max = 0;
        min = Long.MAX_VALUE;
        for (i = 0; i < 10; i++) {
            long diff;
            t1 = VM.getTimeMicros();
            t2 = VM.getTimeMicros();
            diff = t2 - t1;
            sum += diff;
            if (diff > max) {
                max = diff;
            }
            if (diff < min) {
                min = diff;
            }
        }
        System.out.println("testTime: getTimeMicros took avg of " + (sum / i) + " usec ");
        System.out.println("    min = " + min + " usec ");
        System.out.println("    max = " + max + " usec ");

        sum = 0;
        max = 0;
        min = Long.MAX_VALUE;
        for (i = 0; i < 10; i++) {
            long diff;
            t1 = VM.getTimeMicros();
            t2 = VM.getTimeMillis();
            diff = (t2 * 1000) - t1;
            sum += diff;
            if (diff > max) {
                max = diff;
            }
            if (diff < min) {
                min = diff;
            }
        }
        System.out.println("testTime: getTimeMicros vs. getTimeMillis took avg of " + (sum / i) + " usec ");
        System.out.println("    min = " + min + " usec ");
        System.out.println("    max = " + max + " usec ");
    }

    BlockingFunction usleepPtr =  NativeLibrary.getDefaultInstance().getBlockingFunction("usleep");

    /**
     * Sleep usec microseconds using native code
     * @param usec
     */
    void nativeUsleep(int usec) {
        long time = VM.getTimeMicros();
        usleepPtr.call1(usec);
        time = VM.getTimeMicros() - time;
        if (time - usec != 0) {
            System.out.println("nativeUsleep missed deadline of " + usec + " usec by " + (time - usec));
        } else {
            System.out.println("nativeUsleep of " + usec + " ok");
        }
    }

    /**
     * Sleep ms milliseconds using native code
     * @param ms
     */
    void nativeSleep(int ms) {
        nativeUsleep(ms * 1000);
    }
    
    /**
     * Sleep ms milliseconds using native code
     * @param ms
     */
    void systemSleep(int ms) {
        long time = VM.getTimeMicros();
        sleep(ms);
        time = VM.getTimeMicros() - time;
        if (time - (ms * 1000) != 0) {
            System.out.println("sleep missed deadline of " + ms + " ms by " + (time - (ms * 1000)) + " usec");
        } else {
            System.out.println("sleep of " + ms + " ok");
        }
    }

    void testSystemSleep() {
        System.out.println("testSystemSleep");

        systemSleep(1000);
        systemSleep(100);
        systemSleep(100);
        systemSleep(100);
        systemSleep(10);
        systemSleep(10);
        systemSleep(10);
        systemSleep(5);
        systemSleep(5);
        systemSleep(5);
        systemSleep(2);
        systemSleep(2);
        systemSleep(2);
        systemSleep(1);
        systemSleep(1);
        systemSleep(1);
        System.out.println(Thread.currentThread() + " done testSystemSleep");
    }

    void testSleep() {
        System.out.println("testSleep");

        nativeSleep(1000);
        nativeSleep(100);
        nativeSleep(100);
        nativeSleep(100);
        nativeSleep(10);
        nativeSleep(10);
        nativeSleep(10);
        nativeSleep(5);
        nativeSleep(5);
        nativeSleep(5);
        nativeSleep(2);
        nativeSleep(2);
        nativeSleep(2);
        nativeSleep(1);
        nativeSleep(1);
        nativeSleep(1);
        System.out.println(Thread.currentThread() + " done testSleep");
    }

    void testSleepMT() {
        final int NUM_THREADS = 5;
        Thread[] threads = new Thread[NUM_THREADS];
        System.out.println("testSleepMT: " + NUM_THREADS + " threads.");

        for (int i = 0; i < NUM_THREADS; i++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    testSleep();
                }
            },
                    "testSleepMT-" + i);
            threads[i] = t;
            System.out.println("testSleepMT: starting " + t);
            t.start();
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    BlockingFunction dummyFuncPtr =  NativeLibrary.getDefaultInstance().getBlockingFunction("squawk_dummy_func");
    Function dummyFuncNBPtr =  NativeLibrary.getDefaultInstance().getFunction("squawk_dummy_func");

    long timeDummyFunc() {
        long time = VM.getTimeMicros();
        dummyFuncPtr.call0();
        return VM.getTimeMicros() - time;
    }

    long timeDummyFuncNB() {
        long time = VM.getTimeMicros();
        dummyFuncNBPtr.call0();
        return VM.getTimeMicros() - time;
    }

    void timeDefaultBlockingCall0() {
        long sum = 0;
        long max = 0;
        long min = Long.MAX_VALUE;
        int i;
        for (i = 0; i < 500; i++) {
            long time = timeDummyFunc();
            sum += time;
            if (time > max) {
                max = time;
            }
            if (time < min) {
                min = time;
            }
        }
        System.out.println("timeDummyFunc took avg of " + (sum / i) + " usec ");
        System.out.println("    min = " + min + " usec ");
        System.out.println("    max = " + max + " usec ");
    }

    void timeDefaultBlockingCall() {
        System.out.println("timeDefaultBlockingCall: ");
        timeDefaultBlockingCall0();
    }

    void timeDefaultNonBlockingCall() {
        System.out.println("timeDefaultNonBlockingCall: ");
        long sum = 0;
        long max = 0;
        long min = Long.MAX_VALUE;
        int i;
        for (i = 0; i < 500; i++) {
            long time = timeDummyFuncNB();
            sum += time;
            if (time > max) {
                max = time;
            }
            if (time < min) {
                min = time;
            }
        }
        System.out.println("timeDummyFunc took avg of " + (sum / i) + " usec ");
        System.out.println("    min = " + min + " usec ");
        System.out.println("    max = " + max + " usec ");
    }

    void timeDefaultBlockingCallWTask() {
        System.out.println("timeDefaultBlockingCall: with specified TaskExecutor");
        TaskExecutor te = new TaskExecutor("timeDummyFunc runner");
        dummyFuncPtr.setTaskExecutor(te);
        timeDefaultBlockingCall0();
        te.stopTaskExecutor();
    }

//    private void testHTTP() {
//        System.out.println("testHTTP - try to read web page from desktop http server");
//        System.out.println("    NOTE: $$$$ is normal - it indicates that other Java threads can run while sockets are blocked on reads.");
//
//        String[] args = {"10.0.0.11", "80"};
//        try {
//            com.sun.squawk.io.j2me.socket.Test.main(args);
//            System.out.println("testHTTP - DONE");
//        } catch (IOException ex) {
//            System.err.println("Connection to web page failed: " + ex);
//        }
//    }

    void testEcho() {
        System.out.println("testEcho");

        new Thread(new EchoServer(8300)).start();
        new Thread(new EchoServer(8301)).start();

        EchoServerTest.runEchoServerTest(8300);
        EchoServerTest.runEchoServerTest(8301);

        System.out.println("testEcho - sleeping 5 sec...");
        sleep(5 * 1000);
        System.out.println("testEcho - DONE");
    }

    protected void startApp() throws MIDletStateChangeException {
        System.out.println("Hello, Blocking Test");
        System.gc();

        System.out.println("-----------------");
        testTime();

        System.out.println("-----------------");
        timeDefaultNonBlockingCall();

        System.out.println("-----------------");
        timeDefaultBlockingCall();
        
        System.out.println("-----------------");
        timeDefaultBlockingCallWTask();

        System.out.println("-----------------");
        testSystemSleep();

        System.out.println("-----------------");
        testSleep();

        System.out.println("-----------------");
        testSleepMT();


        System.out.println("-----------------");
        testEcho();

//        System.out.println("-----------------");
//        testHTTP();


        System.out.println("-----------------");
        System.out.println("Done Blocking Test");
        notifyDestroyed();
    }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     * I.e. if startApp throws any exception other than MIDletStateChangeException,
     * if the isolate running the MIDlet is killed with Isolate.exit(), or
     * if VM.stopVM() is called.
     *
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true when this method is called, the MIDlet must
     *    cleanup and release all resources. If false the MIDlet may throw
     *    MIDletStateChangeException  to indicate it does not want to be destroyed
     *    at this time.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }


}
