package tests;

import javax.microedition.midlet.*;


public class TestMIDlet extends MIDlet {
    
    protected void startApp() throws MIDletStateChangeException {
        System.out.println(this.getClass().getName() + ".startApp invoked !!!");
        ResourceTest.main(new String[0]);
        try {
            System.exit(999);
        } catch (SecurityException e) {
            System.out.println("PASSED: System.exit() threw exception");
        }
        
       notifyDestroyed();
    }
    
    protected void pauseApp() {
    }
    
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        System.out.print(this.getClass().getName() +".destroyApp invoked !!!");
    }
    
}
