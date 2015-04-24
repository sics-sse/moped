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
public class TestSockets extends MIDlet {

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
    }

    protected void pauseApp() {
    }
    
    protected void startApp() throws MIDletStateChangeException {
        String[] args = {"labs.oracle.com"};
		try {
			main(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
        notifyDestroyed();
    }

        private static boolean runTwiddler = true;

    private static void getPage(String host, String port, String path, boolean byByte)
            throws IOException {
        StreamConnection c = (StreamConnection) Connector.open("socket://" + host + ":" + port);
        OutputStream out = c.openOutputStream();
        InputStream in = c.openInputStream();
        // specify 1.0 to get non-persistent connections.
        // Otherwise we have to parse the replies to detect when full reply is received.
        String command = "GET /" + path + " HTTP/1.0\r\nHost: " + host + "\r\n\r\n";
        byte[] data = command.getBytes();

        out.write(data, 0, data.length);

        long time = System.currentTimeMillis();
        if (byByte) {
            int b = 0;
            while ((b = in.read()) != -1) {
                System.out.print((char) b);
            }
        } else {
            int n = 0;
            while ((n = in.read(data, 0, data.length)) != -1) {
                for (int i = 0; i < n; i++) {
                    System.out.print((char) data[i]);
                }
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println("-------------- Took " + time + "ms");
        c.close();
    }

    /**
     * test code
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        try {
            String host = args[0];
            String port = "80";
            String path = "index.html";
            if (args.length > 1) {
                port = args[1];
            }
            if (args.length > 2) {
                path = args[2];
            }
            System.err.println("creating twiddler");
            Thread twiddler = new Thread(new Runnable() {
                public void run() {
                    while (runTwiddler) {
                        VM.print('$');
//                        try {
//                            Thread.sleep(10);
//                        } catch (InterruptedException ex) {
//                            ex.printStackTrace();
//                        }
                        Thread.yield();
                    }
                }
            }, "Twiddler Thread");
            twiddler.setPriority(Thread.MIN_PRIORITY);
            VM.setAsDaemonThread(twiddler);
            twiddler.start();
            try {
                Thread.sleep(2);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            // DNS, web server, or proxy cache be faster 2nd time around, so do twice...
            System.out.println("---------- Read in buffer (add to cache): ");
            getPage(host, port, path, false);
            System.out.println("---------- Read in buffer (cached): ");
            getPage(host, port, path, false);

            System.out.println("---------- Read in by byte: ");
            getPage(host, port, path, true);

            runTwiddler = false;

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
