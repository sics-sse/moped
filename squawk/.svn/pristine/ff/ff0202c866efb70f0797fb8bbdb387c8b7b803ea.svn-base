package com.sun.squawk.test;


/*
 * Test Echo Server with 3 servers and 5 clients each
 *
 * Created on Mar 5, 2009 3:37:29 PM;
 */


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

    protected void startApp() throws MIDletStateChangeException {
        try {
        System.out.println("Hello, EchoTest");

        new Thread(new EchoServer(8300)).start();
        new Thread(new EchoServer(8301)).start();
        
        EchoServerTest.runEchoServerTest(8300);
        EchoServerTest.runEchoServerTest(8301);
        EchoServerTest.runEchoServerTest(8302);

        new Thread(new EchoServer(8302)).start();

        while (true) {
            sleep(1000);
        }
        } finally {

            notifyDestroyed();                      // cause the MIDlet to exit
        }

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
