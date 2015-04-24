/*
 * TestStackTrace.java
 *
 * Created on March 13, 2007, 9:53 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

/**
 * Test throwing and reporting stack traces. Especially uncaight exception, end errors in the streams that the error is printed on.
 * @author dw29446
 */

public class TestStackTraceIsolate {
    
    public static void main(String[] args) {
        System.out.println("Testing stack trace printing from within an isolate");
        
       TestStackTrace.doIt();
        
        System.out.println("DONE Testing stack trace printing within an isolate.");
    }

    private TestStackTraceIsolate() {
    }
    
}
