package tests;

import com.sun.squawk.*;
import java.io.IOException;
/**
 * This is the isolate class that we will test.
 */
public class LifecycleTest {
    
    /**
     * Fail if listener ever called.
     */
    public static class FailingListener implements Isolate.LifecycleListener {
        public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
           checkThat("listener should have been removed" != null);
        }
    }
    
    private static void checkThat(boolean tst) {
        if (!tst) {
            throw new RuntimeException("checkThat failed!");
        }
    }
    
    public static void main(String[] args) {
        boolean nop = false;
        boolean shared = false;
        boolean wait = false;
        boolean hibernate = false;
        
        for (int i = 0; i < args.length; i++) {
            String testKind = args[i];
            if (testKind.equals("nop")) {
                nop = true;
            } else if (testKind.equals("shared")) {
                shared = true;
            } else if (testKind.equals("seperate")) {
                shared = false;
            } else if (testKind.equals("wait")) {
                wait = true;
            } else if (testKind.equals("hibernate")) {
                hibernate = true;
            } else {
                throw new RuntimeException("Illegal testKind " + testKind);
            }
        }

        System.err.println("Test: " + (nop ? "nop " : "") + (shared ? "SHARED " : "seperate ") + (wait ? "wait" : "") + (hibernate ? "hibernate" : ""));
        checkThat(!(hibernate & wait));
        Isolate.LifecycleListener shutdownListener;
        Isolate.LifecycleListener hibernateListener;
        Isolate.LifecycleListener unhibernateListener;
        
        if (shared) {
            if (nop) {
                shutdownListener = new FailingListener();
            } else {
                shutdownListener = new Isolate.LifecycleListener() {
                    public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
                        System.err.println("Isolate.Lifecycle event: " + eventKind);
                    }
                };
                
            }
            hibernateListener = shutdownListener;
            unhibernateListener = shutdownListener;
        } else {
            if (nop) {
                shutdownListener = new FailingListener();
                hibernateListener = new FailingListener();
                unhibernateListener = new FailingListener();
            } else {
                shutdownListener = new Isolate.LifecycleListener() {
                    public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
                        checkThat(eventKind == Isolate.SHUTDOWN_EVENT_MASK);
                        System.err.println("Isolate.Lifecycle event: SHUTDOWN_EVENT_MASK");
                    }
                };
                hibernateListener = new Isolate.LifecycleListener() {
                    public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
                        checkThat(eventKind == Isolate.HIBERNATE_EVENT_MASK);
                        System.err.println("Isolate.Lifecycle event: HIBERNATE_EVENT_MASK");
                    }
                };
                unhibernateListener = new Isolate.LifecycleListener() {
                    public void handleLifecycleListenerEvent(Isolate iso, int eventKind) {
                        checkThat(eventKind == Isolate.UNHIBERNATE_EVENT_MASK);
                        System.err.println("Isolate.Lifecycle event: UNHIBERNATE_EVENT_MASK");
                    }
                };
            }
        }
        
        if (shared) {
            Isolate.currentIsolate().addLifecycleListener(shutdownListener,
                    Isolate.SHUTDOWN_EVENT_MASK | Isolate.HIBERNATE_EVENT_MASK | Isolate.UNHIBERNATE_EVENT_MASK);
        } else {
            Isolate.currentIsolate().addLifecycleListener(shutdownListener, Isolate.SHUTDOWN_EVENT_MASK);
            Isolate.currentIsolate().addLifecycleListener(hibernateListener, Isolate.HIBERNATE_EVENT_MASK);
            Isolate.currentIsolate().addLifecycleListener(unhibernateListener, Isolate.UNHIBERNATE_EVENT_MASK);
            
            // try some bogus removes that should fail:
            checkThat(!Isolate.currentIsolate().removeLifecycleListener(shutdownListener, Isolate.HIBERNATE_EVENT_MASK));
            checkThat(!Isolate.currentIsolate().removeLifecycleListener(hibernateListener, Isolate.SHUTDOWN_EVENT_MASK));
            checkThat(!Isolate.currentIsolate().removeLifecycleListener(unhibernateListener, Isolate.SHUTDOWN_EVENT_MASK));
        }
        
        if (nop) {
            if (shared) {
                // try some removes that should succeed:
                checkThat(Isolate.currentIsolate().removeLifecycleListener(shutdownListener,
                        Isolate.SHUTDOWN_EVENT_MASK | Isolate.HIBERNATE_EVENT_MASK | Isolate.UNHIBERNATE_EVENT_MASK));
            } else {
                // try some removes that should succeed:
                checkThat(Isolate.currentIsolate().removeLifecycleListener(shutdownListener, Isolate.SHUTDOWN_EVENT_MASK));
                checkThat(Isolate.currentIsolate().removeLifecycleListener(hibernateListener, Isolate.HIBERNATE_EVENT_MASK));
                checkThat(Isolate.currentIsolate().removeLifecycleListener(unhibernateListener, Isolate.UNHIBERNATE_EVENT_MASK));
            }
        } else if (wait) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        } else if (hibernate) {
            try {
                System.err.println("Now hibernating...");
                Isolate.currentIsolate().hibernate();
                System.err.println("Done hibernating");
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private LifecycleTest() {
    }
    
}
