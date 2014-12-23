package tests.msgtests;

import java.io.*;
import javax.microedition.io.*;

/**
 * The application class.
 */
public class Main {

    /**
     * Entry point.
     *
     * @param args the command like arguments
     */
    public static void main(String[] args) throws Exception {
        Client[] clients = new Client[20];

        /*
         * Start 3 clients.
         */
        for (int i = 0 ; i < clients.length ; i++) {
            clients[i] = new Client();
            clients[i].start();
        }

        /*
         * Start the server.
         */
        new Server().start();

        /*
         * Wait for the clients to finish.
         */
        for (int i = 0 ; i < clients.length ; i++) {
            clients[i].join();
        }

//try { Thread.sleep(1000); } catch(Exception ex) {}

        /*
         * Stop the server.
         */
        System.exit(1);
    }
}

/**
 * The client thread class.
 */
class Client extends Thread {
    public void run() {
        try {
            StreamConnection con = (StreamConnection)Connector.open("msg:///sunstock/is64again");

            InputStream  in  = con.openInputStream();
            OutputStream out = con.openOutputStream();

            long start = System.currentTimeMillis();

            out.write("Hello".getBytes());
            out.close();

            StringBuffer sb = new StringBuffer();
            while (true) {
                int ch = in.read();
                if (ch == -1) {
                    break;
                }
                sb.append((char)ch);
            }
            in.close();

            long time = System.currentTimeMillis() - start;

            System.err.println("Client got: \"" + sb + "\" in " + time + "ms");

            con.close();

        } catch (IOException ioe) {
            System.err.println("IOException in client "+ioe);
        }
    }
}

/**
 * The server thread class.
 */
class Server extends Thread {
    public void run() {
        int connection = 0;
        try {
            StreamConnectionNotifier scn = (StreamConnectionNotifier)Connector.open("msgserver:///sunstock/is64again");
            for (;;) {
                connection++;
                StreamConnection con = scn.acceptAndOpen();
                InputStream  in  = con.openInputStream();
                OutputStream out = con.openOutputStream();

                StringBuffer sb = new StringBuffer();
                while (true) {
                    int ch = in.read();
                    if (ch == -1) {
                        break;
                    }
                    sb.append((char)ch);
                }
                in.close();

                System.err.println("Server got: "+sb);

                out.write(sb.toString().getBytes());
                out.write((" World "+connection).getBytes());
                out.close();
                con.close();
            }
        } catch (IOException ioe) {
            System.err.println("IOException in server "+ioe);
        }
    }
}
