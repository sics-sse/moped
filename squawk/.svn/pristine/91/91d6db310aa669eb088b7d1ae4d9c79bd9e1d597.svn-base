/*
 * TestIsolateLifecycleLocal.java
 *
 * Created on June 22, 2007, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

import com.sun.squawk.*;
import java.io.IOException;

/**
 *
 * Test getting callbacks for isolate exit, hibernate, and unhibernate().
 *
 * @author dw29446
 */
public class TestIsolateLifecycleLocal {
    
    
    final static String ISOLATE = "tests.LifecycleTest";
    final static int COUNT = 3;
    
    public static void main(String[] args) {
        String[] isoArgs = new String[1];
        String cp = Isolate.currentIsolate().getClassPath();
        String parentURI =  Isolate.currentIsolate().getParentSuiteSourceURI();
        
        System.err.println("classpath: " + cp);
        
        System.err.println("---- test individual listeners for shutdown, hibernate, and unhibernate:");
        isoArgs[0] = "seperate";
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate(ISOLATE,  isoArgs, cp, parentURI);
            iso.start();
            iso.join();
        }
        System.err.println("---- done individual test\n");
        
        // this should do nothing at all:
        System.err.println("---- There should be no listeners called in this run:");
        isoArgs[0] = "nop";
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate(ISOLATE,  isoArgs, cp, parentURI);
            iso.start();
            iso.join();
        }
        System.err.println("---- done nop test\n");
        
        System.err.println("---- test shared listener for shutdown, hibernate, and unhibernate:");
        isoArgs[0] = "shared";
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate(ISOLATE,  isoArgs, cp, parentURI);
            iso.start();
            iso.join();
        }
        System.err.println("---- done shared test\n");
        
        // this should do nothing at all:
        System.err.println("---- There should be no listeners called in this run:");
        String[] test9 = {"nop", "shared"};
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate(ISOLATE,  test9, cp, parentURI);
            iso.start();
            iso.join();
        }
        System.err.println("---- done nop shared test\n");
        
        System.gc();
        System.err.println("---- test hibernation with individual listeners for shutdown, hibernate, and unhibernate:");
        String[] test2 = {"hibernate", "seperate"};
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate(ISOLATE, test2, cp, parentURI);
            System.err.println("------- starting isolate " + i);
            iso.start();
            System.err.println("------- joining isolate " + i);
            iso.join();
            if (!iso.isHibernated()) {
                throw new RuntimeException(iso + " is not hibernated");
            }
            System.err.println("------- unhibernate isolate " + i);
            iso.unhibernate();
            System.err.println("------- joining isolate " + i);
            iso.join();
            System.err.println("------- joined isolate " + i);
        }
        System.err.println("---- done hibernate test\n");
        
        System.gc();
        // set up some isolates and hibernate them. The callbacks should NOT run.
        System.err.println("---- Test isolates left hibernated. There should be no listeners called for these during VM.stopVM() later on:");
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate(ISOLATE, test2, cp, parentURI);
            iso.start();
            iso.join();
            if (!iso.isHibernated()) {
                throw new RuntimeException(iso + " is not hibernated");
            }
        }
        System.err.println("---- left some isolates hibernated\n");
        
        System.err.println("---- test recursive exit call in shutdown hook, and hibernate call in hibernate hook:");
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate("tests.RecursiveLifecycleTest", new String[0], cp, parentURI);
            iso.start();
            iso.join();
            if (!iso.isHibernated()) {
                throw new RuntimeException(iso + " is not hibernated");
            }
            iso.unhibernate();
            iso.join();
        }
        System.err.println("---- done recursive test\n");
        
        
        System.err.println("---- test callback that throws RuntimeException");
        isoArgs[0] = "RuntimeException";
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate("tests.BugInCallbackTest", isoArgs, cp, parentURI);
            iso.start();
            iso.join();
        }
        System.err.println("---- done callback that throws RuntimeException test\n");
        
//        System.err.println("---- test callback that throws Error");
//        isoArgs[0] = "Error";
//        for (int i = 0; i < COUNT; i++) {
//            Isolate iso = new Isolate("tests.BugInCallbackTest", isoArgs, cp, parentURI);
//            iso.start();
//            iso.join();
//        }
//        System.err.println("---- done callback that throws Error test\n");
        
        
        // for last trick, test that isolate shutdown events are handled in VM.stopVM()
        
        System.err.println("---- test shutdown listeners on VM.stopVM()");
        isoArgs[0] = "wait";
        for (int i = 0; i < COUNT; i++) {
            Isolate iso = new Isolate(ISOLATE,  isoArgs, cp, parentURI);
            iso.start();
        }
        
        try {
            System.err.println("... sleeping()...");
            Thread.sleep(10000); // wait for all isolates to start,
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        System.err.println("Calling VM.stopVM() - isolate shutdown hooks should be called:");
        VM.stopVM(100);
        
    }

    private TestIsolateLifecycleLocal() {
    }
    
}

class BugInCallbackTest {
    
    public static void main(String[] args) {
        System.err.println("------------ BugInCallbackTest ----------------");
        final boolean throwable = args[0].equals("Error");
        
        Isolate.LifecycleListener shutdownListener = new Isolate.LifecycleListener() {
            public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
                System.err.println("Starting shutdownListener");
                if (throwable) {
                    System.err.println("THROWING THROWABLE");
                     Object o = null;
                    try {
                        Class cls = Class.forName("java.lang.Error");
                        o = cls.newInstance(); // NOTE: printed stack trace will be for this location!
                    } catch (IllegalAccessException ex) {
                        ex.printStackTrace();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (InstantiationException ex) {
                        ex.printStackTrace();
                    } catch (Error ex) {
                        System.err.println("Got eror in newinstance?: " + ex);
                        ex.printStackTrace();
                    }
                    throw (Error)o;
                } else {
                    System.err.println("THROWING RuntimeException");
                    throw new RuntimeException("What are you gonna do about this?");
                }
            }
        };
        
        Isolate.currentIsolate().addLifecycleListener(shutdownListener, Isolate.SHUTDOWN_EVENT_MASK);
    }

    private BugInCallbackTest() {
    }
}


class RecursiveLifecycleTest {
    
    public static void main(String[] args) {
        System.err.println("-----------RecursiveLifecycleTest-------------");
        
        Isolate.LifecycleListener shutdownListener;
        Isolate.LifecycleListener hibernateListener;
        
        
        shutdownListener = new Isolate.LifecycleListener() {
            public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
                System.err.println("Starting shutdownListener");
                iso.exit(999); // ! RECURSIVE CALL - SHOULD FAIL OR BE IGNORED
                System.err.println("Completed shutdownListener. PASSED");  // if we really exited, we wouldn't be here now!'
            }
        };
        hibernateListener = new Isolate.LifecycleListener() {
            public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
                System.err.println("Started hibernateListener: (Should only print 1x per isolate");
                try {
                    iso.hibernate(); // ! RECURSIVE CALL - SHOULD FAIL OR BE IGNORED
                } catch (IllegalStateException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } // ! RECURSIVE CALL - SHOULD FAIL OR BE IGNORED
                System.err.println("Completed hibernateListener: PASSED");
            }
        };
        
        Isolate.currentIsolate().addLifecycleListener(shutdownListener, Isolate.SHUTDOWN_EVENT_MASK);
        Isolate.currentIsolate().addLifecycleListener(hibernateListener, Isolate.HIBERNATE_EVENT_MASK);
        
        try {
            Isolate.currentIsolate().hibernate(); // ! RECURSIVE CALL - SHOULD FAIL OR BE IGNORED
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private RecursiveLifecycleTest() {
    }
}


