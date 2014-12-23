package tests;

import javax.microedition.midlet.*;


public class TestMIDletNeverEnding extends MIDlet {
    
    protected void startApp() throws MIDletStateChangeException {
        System.out.println(this.getClass().getName() + ".startApp invoked !!!");
        ResourceTest.main(new String[0]);
        try {
            System.exit(999);
        } catch (SecurityException e) {
            System.out.println("PASSED: System.exit() throw exception");
        }
        
        System.out.println("TestMIDletNeverEnding should not end until forced to. Press Ctrl-C when tired of checking this.");
       ////notifyDestroyed();
    }
    
    protected void pauseApp() {
    }
    
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        System.out.print(this.getClass().getName() +".destroyApp invoked !!!");
    }
    
}

