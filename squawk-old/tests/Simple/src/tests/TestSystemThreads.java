/*
 * TestSystemThreads.java
 *
 * Created on November 8, 2006, 4:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

import com.sun.squawk.*;

/**
 *
 * @author dw29446
 */
public class TestSystemThreads {

    public static void main (String[] args) {
        Thread t;
        
        // test basic API
        t = new Thread("Test thread");
        VM.setSystemThreadPriority(t, VM.MAX_SYS_PRIORITY);
        t.start();
        System.out.println("Passed #0");
        
        // check that you can set as system after start
        t = new Thread("Test thread");
        t.start();
        VM.setSystemThreadPriority(t, 11);
        System.out.println("Passed #1");

        try {
            // level too low
            t = new Thread("Test thread");
            VM.setSystemThreadPriority(t, -1);
            System.out.println("Failed #3");
        } catch (IllegalArgumentException e) {
            System.out.println("Passed #3");
        }
        
        try {
            // only two system levels
            t = new Thread("Test thread");
            VM.setSystemThreadPriority(t, VM.MAX_SYS_PRIORITY+1);
            System.out.println("Failed #4");
        } catch (IllegalArgumentException e) {
            System.out.println("Passed #4");
        }
        
        // check that uncaught exception processing works
        t = new Thread() {
            public void run() {
                throw new RuntimeException("This is a dumb exception");
            }
        };
        t.start();
        System.out.println("CHECK: Should see \"This is a dumb exception\", followed by stack trace");
        
        // check that uncaught exception processing works
        t = new Thread() {
            public void run() {
                throw new RuntimeException() {
                    public void printStackTrace() {
                        throw new RuntimeException("Exception in my wacky printStackTrace() method.");
                    }
                };
            }
        };
        t.start();
        System.out.println("CHECK: Should see \"Exception in my wacky printStackTrace() method.\", followed by stack trace");
        
    }

    private TestSystemThreads() {
    }
}
