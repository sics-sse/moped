package tests;

import com.sun.squawk.VM;
import javax.microedition.midlet.*;

public class TestDestroyApp extends MIDlet {

    protected void startApp() throws MIDletStateChangeException {
        System.out.println(this.getClass().getName() + ".startApp invoked !!!");
        VM.stopVM(0);

        notifyDestroyed();
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        System.out.print(this.getClass().getName() + ".destroyApp invoked !!!");
    }
}
