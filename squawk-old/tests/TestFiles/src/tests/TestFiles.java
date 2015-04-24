package tests;

import com.sun.squawk.VM;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 *
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 */
public class TestFiles extends MIDlet {

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }
    
    protected void startApp() throws MIDletStateChangeException {
        String[] args = {"file://build.properties"};
		main(args);
        notifyDestroyed();
    }

    /**
     * test code
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.err.println("creating twiddler"); // start thread to verify that sockets are non-blocking...

            Thread twiddler = new Thread(new Runnable() {

                public void run() {
                    while (true) {
                        VM.print('$');
                        Thread.yield();
                    }
                }
            }, "Twiddler Thread");
            twiddler.setPriority(Thread.MIN_PRIORITY);
            VM.setAsDaemonThread(twiddler);
            System.err.println("starting twiddler");

            twiddler.start();
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            StreamConnection conn = null;
            InputStream is = null;
            System.err.println("openning connection on " + args[0]);

            try {
                conn = (StreamConnection) Connector.open(args[0], Connector.READ);

                is = conn.openInputStream();
                int ch;
                while ((ch = is.read()) != -1) {
                    System.out.print((char) ch);
                }
            } finally {
                try {
                    is.close();
                    conn.close();
                } catch (Exception ex) {
                    // ignore any null pointers etc for this example test
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
