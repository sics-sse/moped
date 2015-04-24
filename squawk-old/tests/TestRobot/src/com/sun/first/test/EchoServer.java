/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.first.test;

import java.io.*;
import javax.microedition.io.*;

/**
 *
 * @author dw29446
 */
public class EchoServer implements Runnable {
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

    /**
     * The notifier for incoming connections if this is a server socket connection.
     */
    private StreamConnectionNotifier server;

    private final static int MAX_RUNNERS = 5;

    private volatile static int runnerCount;

    private final static Object runnerCountLock = new Object();

    EchoServer(StreamConnection conn) {
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
                int n = in.available();
                n = Math.min(n, BUF_SIZE);
                in.readFully(buffer, 0, n);
                out.write(buffer, 0, n);
            }
        } catch (IOException ex) {
            System.out.println("Echo server closed due to " + ex);
        } finally {
            synchronized (runnerCountLock) {
                runnerCount--;
            }
            try {
                in.close();
                out.close();
                conn.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void runEchoServer(int port) {
        String url = "serversocket://:" + port;
        try {
            StreamConnectionNotifier serverConn = (StreamConnectionNotifier) Connector.open(url);
            while (true) {
                if (runnerCount < MAX_RUNNERS) {
                    try {
                        StreamConnection conn = serverConn.acceptAndOpen();
                        System.out.println("Connection with " + url + " established");
                        EchoServer server = new EchoServer(conn);
                        Thread serverThread = new Thread(server);
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
        runEchoServer(8007);
    }
}
