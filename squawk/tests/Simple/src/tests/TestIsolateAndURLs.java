/*
 * TestIsolateAndURLs.java
 *
 * Created on November 16, 2007, 9:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

import com.sun.squawk.*;

/**
 *
 * In this test, create graphics as a suite, and use samples as class files.
 * Make sure that graphics,.suite is in a directory containing a space in the path.
 *
 * this "emulates" the set up that Ron's emulator uses.
 *
 * This is what we are trying to do (should always work):
 *   > squawk -suite:graphics -cp:samples/j2meclasses/:samples/res example.mpeg.MPEG
 *
 * TEST 1 (should always work)
 *   > squawk -cp:samples/j2meclasses/:samples/res tests.TestIsolateAndURLs file://graphics.suite
 *
 *
 * TEST 2 (test with spaces in path):
 *   > mkdir 'odd folder'
 *   > cp graphics.suite 'odd folder'
 *   > squawk -cp:samples/j2meclasses/:samples/res tests.TestIsolateAndURLs file://graphics.suite
 *
 * @author dw29446
 */
public class TestIsolateAndURLs {
    
    public static void main(String[] args) {
        System.out.print("Loading graphics suite from url in args[0] ");
        String url = args[0];
        System.out.println(url);
        
        System.out.println("Loading classes from " + Isolate.currentIsolate().getClassPath());
        
        Isolate isolate = new Isolate(null, 2, Isolate.currentIsolate().getClassPath(), url);
        isolate.start();
        isolate.join();
    }
}

