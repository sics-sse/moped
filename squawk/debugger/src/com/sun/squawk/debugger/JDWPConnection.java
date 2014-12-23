/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2010 Oracle. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Oracle, 16 Network Circle, Menlo Park, CA 94025 or
 * visit www.oracle.com if you need additional information or have
 * any questions.
 */

package com.sun.squawk.debugger;

import java.io.*;
import javax.microedition.io.*;

import com.sun.squawk.util.*;

/**
 * A JDWPConnection represents a connection between two debugger entities that communicate
 * primarily in {@link Packet packets}.
 *
 */
public final class JDWPConnection {

    public static class ClosedException extends IOException {
        ClosedException(String closedBy) {
            super("Closed by " + closedBy);
        }
    }

    /**
     * The connection to the other end.
     */
    private final StreamConnection conn;

    /**
     * The URL on which this connection is based.
     */
    private final String url;

    /**
     * The stream used to send data down the connection.
     */
    private final DataOutputStream out;

    /**
     * The stream used to receive data from the connection.
     */
    private final DataInputStream in;

    /**
     * The notifier for incoming connections if this is a server socket connection.
     */
    private StreamConnectionNotifier server;

    /**
     * Denotes if this is a connection to a JPDA debugger.
     */
    private final boolean isJDB;

    /**
     * Denotes if this connection has been {@link #close closed} and the name of the thread on which it was closed.
     */
    private String closedBy;


    /**
     * Creates a connection based on a given URL and does a handshake with
     * the remote enity once the connection is established.
     *
     * @param url    the URL used to open the connection
     * @param handshake   the array of bytes that must be exchanged in each
     *                    direction to complete the handshake
     * @param initiate    true if the handshake is to be intiated by this host
     * @param isJDB       if true, this is connection to Debugger
     * @param delayer     code to run after connection but before handshake
     * @throws IOException if there is an error establising the connection
     */
    public JDWPConnection(String url, byte[] handshake, boolean initiate, boolean isJDB, Runnable delayer) throws IOException {
        this.url = url;
        this.isJDB = isJDB;
        if (Log.info()) {
            Log.log("Establishing connection with " + url + "...");
        }
        try {
            Connection c = Connector.open(url);
            if (c instanceof StreamConnectionNotifier) {
                server = (StreamConnectionNotifier) c;
                try {
                    conn = server.acceptAndOpen();
                } catch (InterruptedIOException e) {
                    server.close();
                    throw e;
                }
            } else {
                conn = (StreamConnection) c;
            }
            in = conn.openDataInputStream();
            out = conn.openDataOutputStream();
            if (Log.info()) {
                Log.log("Connection with " + url + " established");
            }
        } catch (IOException e) {
            if (Log.info()) {
                Log.log("Failed to establish connection with " + url + ": " + e);
            }
            close();
            throw e;
        }
        if (delayer != null) {
            delayer.run(); 
        }
        byte[] buf = new byte[handshake.length];
        try {
            if (initiate) {
                out.write(handshake);
                out.flush();
                in.readFully(buf);
            } else {
                in.readFully(buf);
                out.write(handshake);
                out.flush();
            }
        } catch (IOException e) {
            if (Log.info()) {
                Log.log("Failed to deliver/receive handshake: " + e);
            }
            close();
            throw e;
        }
        if (!Arrays.equals(handshake, buf)) {
            throw new IOException("handshake failed: " + new String(buf));
        }
   }

    /**
     * Closes this connection.
     */
    public synchronized void close() {
        if (closedBy == null) {
            closedBy = Thread.currentThread().toString();
            if (Log.info()) {
                Log.log("Closing connection: " + url);
            }
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                if (conn != null) {
                    conn.close();
                }
                if (server != null) {
                    server.close();
                }
            } catch (IOException e) {
                if (Log.info()) {
                    Log.log("IO error while closing connection: " + url);
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * Raises an exception if this connection has been {@link #close closed}
     *
     * @throws ClosedException if this connection has been closed
     */
    private void checkOpen() throws ClosedException {
        if (closedBy != null) {
            throw new ClosedException(closedBy);
        }
    }

    /**
     * Reads a packet from the connection.
     *
     * @return the received packet
     * @throws IOException if there was an IO error while reading
     */
    Packet readPacket() throws IOException {
        synchronized (in) {
            try {
                int length = in.readInt();
                int id = in.readInt();
                int flags = in.readByte();
                if (Log.verbose()) {
                    Log.log("readPacket: length: " + length + " id: " + id + " flags: " + flags);
                }

                Packet packet;
                int dataLength = length - 11;
                Assert.that(dataLength >= 0);

                if ( (flags & Packet.FLAG_REPLY) == 0) {
                    byte set = (byte) in.readUnsignedByte();
                    byte command = (byte) in.readUnsignedByte();
                    try {
                        packet = new CommandPacket(id, dataLength, in, set, command, isJDB || (flags & Packet.FLAG_NEEDS_REPLY) != 0);
                    } catch (OutOfMemoryError e) {
                        // This catches the situation on a SunSPOT where a debugger has disconnected
                        // and before the VirtualMachine.Dispose event causes this connection to be
                        // closed, some garbage comes across the radio.
                        throw new IOException("could not allocate memory to receive packet with length " + dataLength);
                    }
                } else {
                    int errorCode = in.readShort();
                    packet = new ReplyPacket(id, dataLength, in, errorCode);
                }

                if (Log.DEBUG_ENABLED && Log.debug()) {
                    Log.log("");
//                    Log.log("read("+url+"): " + packet);
                }
                return packet;
            } catch (IOException e) {
                checkOpen();
                throw e;
            }
        }
    }

    /**
     * Writes a packet to the connection.
     *
     * @param packet  the packet to write
     * @throws IOException if there was an IO error while writing
     */
    void writePacket(Packet packet) throws IOException {
        synchronized (out) {
            checkOpen();
            if (Log.DEBUG_ENABLED && Log.debug()) {
                Log.log("");
//                Log.log("write("+url+"): " + packet);
            }
            packet.write(out);
            out.flush();
        }
    }
}
