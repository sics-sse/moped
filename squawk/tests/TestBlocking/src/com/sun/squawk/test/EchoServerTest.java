/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.squawk.test;

import java.io.*;
import javax.microedition.io.*;

/**
 * Connect to an EchoServer, send data and verify data comes back...
 * @author dw29446
 */
public class EchoServerTest implements Runnable {

    public final static int NUM_TEST_CLIENTS = 3;
    public final static int MESSAGES = 20;

    private final String url;

    EchoServerTest(String url) {
        this.url = url;
    }

    public void checkEquals(byte[] a, byte[] b) {
        int n = Math.min(a.length, b.length);
        for (int i =0; i < n; i++) {
            if (a[i] != b[i]) {
                System.err.println("Message not equal! a[" + i + "] = " + a[i] + ", b[" + i + "] = " + b[i]);
            }
        }
    }

    public void run() {
        final int BUF_SIZE = 1024;
        byte[] buffer = new byte[BUF_SIZE];
        StreamConnection conn = null;
        DataInputStream in = null;
        DataOutputStream out = null;

        try {
             System.out.println("Echo client Connecting to " + url);
            conn = (StreamConnection) Connector.open(url);
            System.out.println("Connection with " + url + " established");
            in = conn.openDataInputStream();
            out = conn.openDataOutputStream();
            for (int i = 0; i < MESSAGES; i++) {
                String msg = "Message from " + Thread.currentThread();
                byte[] msgBytes = msg.getBytes();
                long ms = System.currentTimeMillis();
                out.write(msgBytes);
//Thread.yield();
                in.readFully(buffer, 0, msgBytes.length);
                ms = System.currentTimeMillis() - ms;

                checkEquals(msgBytes, buffer);
                System.out.println("Local echo round trip (ms): " + ms);
            }
        } catch (IOException ex) {
            System.out.println("Echo client closed due to " + ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (conn != null) {
                    conn.close();
                }
                System.out.println("Echo client done " + url);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /** Start up N echo test clients on port*/
    public static void runEchoServerTest(int port) {
        String url = "socket://127.0.0.1:" + port;
        for (int i = 0; i < NUM_TEST_CLIENTS; i++) {
            EchoServerTest client = new EchoServerTest(url);
            Thread clientThread = new Thread(client, "Client # " + i + " for port: " + port);
            clientThread.start();
        }
    }

    public static void main(String[] args) {
        runEchoServerTest(8007);
    }
}
