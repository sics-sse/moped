package tests;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import sics.RTEBridge;

//import sics.RTEBridge;
import com.sun.squawk.VM;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class HelloWorld extends MIDlet {

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }
    
    protected void startApp() throws MIDletStateChangeException {
//        System.out.println("Hello world (from a MIDlet)");
//    	RTEBridge.CLibrary.INSTANCE.output("hello world from JNA");
    	VM.print("Hello World from JNA");
//    	RTEBridge.output("hello world from JNA");
        notifyDestroyed();
    }
}
