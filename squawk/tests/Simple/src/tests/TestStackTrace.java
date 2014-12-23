/*
 * TestStackTrace.java
 *
 * Created on March 13, 2007, 9:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

import com.sun.squawk.Isolate;

/**
 * Test throwing and reporting stack traces. Especially uncaight exception, end errors in the streams that the error is printed on.
 * @author dw29446
 */
public class TestStackTrace {
    final static  int NUM_TESTS = 4;
    
    public static void main(String[] args) {
        System.out.println("Testing stack trace printing. Should see several stack traces:");
        doIt();
        System.out.println("DONE Testing stack trace printing.");
        
        // test again, within another isolate. By setting redirect std err in another isolate, the
        // error is delayed until an exception is printed, which will test how system handles errors
        // in the print stream while printing errors.
        Isolate iso = new Isolate("tests.TestStackTraceIsolate", new String[0], "samples/j2meclasses/", Isolate.currentIsolate().getParentSuiteSourceURI());
        iso.addErr("file://samples/src/tests/locked.txt");
        iso.start();
        iso.join();
    }
    
    public static void doIt()  {
        for (int i = 0; i < NUM_TESTS; i++) {
            Thread t = new Thread(new ThrowIt(i));
            t.start();
            
            try {
                t.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private TestStackTrace() {
    }
    
}

class ThrowIt implements Runnable {
    int testNum;
    
    ThrowIt(int testNum) {
        this.testNum = testNum;
    }
    
    public void run() {
        System.out.println();
        System.out.println("Running test " + testNum);
        
        switch (testNum) {
            case 0: {
                try {
                    throw new RuntimeException("Try to catch me - should succeed");
                } catch (RuntimeException e) {
                    System.out.println("Caught exception " + e);
                    e.printStackTrace();
                }
                break;
            }
            
            case 1: {
                throw new RuntimeException("No one is looking for me");
            }
            
            case 2: {
                throw new BigStackTraceException("BigStackTraceException will throw an out of memory exception");
            }
            
            case 3: {
                throw new BigException("BigException will throw an out of memory exception");
            }
            
            default:
                System.out.println("Unknown test num " + testNum);
        }
    }
    
}

class BigException extends RuntimeException {
    
    BigException(String msg) {
        super(msg);
    }
    
    public String getMessage() {
        Object o = new long[Integer.MAX_VALUE];
        return "You should never see this - an out of memory should have happened";
    }
}

class BigStackTraceException extends RuntimeException {
    
    BigStackTraceException(String msg) {
        super(msg);
    }
    
    public void printStackTrace() {
        Object o = new long[Integer.MAX_VALUE];
        System.err.println("You should never see this - an out of memory should have happened");
    }
}
