//if[EXCLUDE]
/*
 * TestExitHooks.java
 *
 * Created on May 2, 2007, 2:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

import com.sun.squawk.VM;

/**
 *
 * @author dw29446
 */
public class TestExitHooks {
    
    /** Creates a new instance of TestExitHooks */
    public TestExitHooks() {
    }
    
    static class DummyHook extends Thread {
        public void run() {
            System.out.println("In DummyHook on " + this + "...");
        }
    }
    
    static class NeverHook extends Thread {
        public void run() {
            System.out.println("FAILED: NeverHook should have been removed.");
        }
    }
    
    static class BadHook1 extends Thread {
        public void run() {
            System.out.println("NOTE: in BadHook1. Should see \"Uncaught exception in Thread.run()\".");
            VM.addShutdownHook(new DummyHook());
            System.out.println("FAILED: VM.addShutdownHook didn't fail in an exit hook!");
        }
    }
    
    static class BadHook2 extends Thread {
        public void run() {
            System.out.println("NOTE: in BadHook2. Should see \"Uncaught exception in Thread.run()\".");
            VM.removeShutdownHook(this);
            System.out.println("FAILED: VM.removeShutdownHook didn't fail in an exit hook!");
        }
    }
    
    public static void main(String[] args) {
        Thread h1 = new DummyHook();
        
        VM.addShutdownHook(h1);
        
        try {
            VM.addShutdownHook(h1);
            System.out.println("FAILED: VM.addShutdownHook allowed duplicate");
        } catch (RuntimeException e) {
            System.out.println("SUCCEEDED: VM.addShutdownHook didn't allow duplicate: " + e);
        }
        Thread n1 = new NeverHook();
        Thread n2 = new NeverHook();
        
        VM.addShutdownHook(n1);
        VM.addShutdownHook(new BadHook1());
        VM.addShutdownHook(new BadHook2());
        VM.addShutdownHook(new DummyHook());
        VM.addShutdownHook(n2);
        if (!VM.removeShutdownHook(n2)) {
            System.out.println("FAILED: VM.removeShutdownHook failed on n2");
        }
        if (!VM.removeShutdownHook(n1)) {
            System.out.println("FAILED: VM.removeShutdownHook failed on n1");
        }

        
        System.out.println("Setup done, now exiting...");
        VM.stopVM(0);
    }
}
