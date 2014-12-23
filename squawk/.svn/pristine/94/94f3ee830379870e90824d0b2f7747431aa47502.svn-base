/*
 * StartApplication.java
 *
 * Created on Mar 5, 2009 3:37:29 PM;
 */

package com.sun.first.test;


import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.squawk.io.j2me.socket.Test;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 * 
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class StartApplication extends MIDlet {


    protected void startApp() throws MIDletStateChangeException {
        System.out.println("Hello, Dan");

        // Socket client test code:
//        try {
//            String[] args = {"10.0.0.6", "8001"};
//           // String[] args = {"www.sun.com"};
//            Test.main(args);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }

        EchoServer.runEchoServer(8007);
        notifyDestroyed();                      // cause the MIDlet to exit

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
