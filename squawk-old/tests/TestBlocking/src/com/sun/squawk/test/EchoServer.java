/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.squawk.test;

import com.sun.squawk.VM;
import java.io.*;
import javax.microedition.io.*;

/**
 *
 * @author dw29446
 */
public class EchoServer implements Runnable {



    private final static int MAX_RUNNERS = 5;

    private volatile static int runnerCount;

    private final static Object runnerCountLock = new Object();

    private int port;

    public EchoServer(int port) {
        this.port = port;

    }

    public class EchoServerConnection implements Runnable {
        /**
         * The connection to the other end.
         */
        private final StreamConnection conn;
        /**
         * The stream used to send data down the connection.
         */
        private DataOutputStream out;
        /**
         * The stream used to receive data from the connection.
         */
        private DataInputStream in;

        EchoServerConnection(StreamConnection conn) {
            this.conn = conn;

        }

        public void run() {
            final int BUF_SIZE = 1024;
            byte[] buffer = new byte[BUF_SIZE];
            try {
                synchronized (runnerCountLock) {
                    runnerCount++;
                }
                System.out.println("Running echo server for " + conn);
                in = conn.openDataInputStream();
                out = conn.openDataOutputStream();
                while (true) {
                    int n = in.read(buffer, 0, BUF_SIZE);
                    if (n < 0) {
                        break; // EOF
                    }
                    out.write(buffer, 0, n);
//Thread.yield();
                }
            } catch (IOException ex) {
                System.out.println("Echo server closed due to " + ex);
            } catch (OutOfMemoryError e) {
                VM.println("Is there a stack trace?");
                e.printStackTrace();
                VM.stopVM(-1);
            } finally {
                synchronized (runnerCountLock) {
                    runnerCount--;
                }
                System.out.println("Closing echo server for " + conn);
                try {
                    in.close();
                    out.close();
                    conn.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void run() {
        String url = "socket://:" + port;
        int thrCount = 0;
        try {
            StreamConnectionNotifier serverConn = (StreamConnectionNotifier) Connector.open(url);
            while (true) {
                if (runnerCount < MAX_RUNNERS) {
                    try {
                        System.out.println(Thread.currentThread() + " is waiting for a connection...");
                        StreamConnection conn = serverConn.acceptAndOpen();
                        System.out.println("Connection with " + url + " established");
                        EchoServerConnection server = new EchoServerConnection(conn);
                        Thread serverThread = new Thread(server, "serverThread " + thrCount++);
                        serverThread.start();
                    } catch (InterruptedIOException e) {
                        serverConn.close();
                        throw e;
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to establish connection with " + url + ": " + e);
        }
    }

    public static void main(String[] args) {
        new Thread(new EchoServer(8007)).start();
    }
}
