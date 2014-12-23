/*
 * Copyright 1999-2008 Sun Microsystems, Inc. All Rights Reserved.
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
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.io.j2se.serversocket;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.microedition.io.*;

import com.sun.squawk.io.*;
import com.sun.squawk.io.j2se.socket.Protocol.Parameters;

/**
 * StreamConnectionNotifier to the Palm Server Socket API.
 *
 * @version 1.0 10/08/99
 */
public class Protocol extends ConnectionBase implements StreamConnectionNotifier {

    /** Server Socket object */
    ServerSocket ssocket;

    /** Extra parameters for a socket. */
    Parameters parameters = new Parameters();

    /**
     * Open the connection
     * @param name the target for the connection
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     * <p>
     * The name string for this protocol should be:
     * "<port number>
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {

        if(name.charAt(0) != '/' || name.charAt(1) != '/' || name.charAt(2) != ':') {
            throw new IllegalArgumentException("Protocol must start with \"//:\" "+name);
        }

        name = parameters.parse(name.substring(3));
        try {
            int port;

            /* Get the port number */
            port = Integer.parseInt(name);

            /* Open the socket */
            ssocket = new ServerSocket(port);

            /* Set timeout value for accept and subsequent sockets reads. */
            ssocket.setSoTimeout(parameters.acceptTimeout);

        } catch(NumberFormatException x) {
            throw new IllegalArgumentException("Invalid port number in "+name);
        }
        return this;
    }

    /**
     * Returns a GenericConnection that represents a server side
     * socket connection
     * @return     a socket to communicate with a client.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    public StreamConnection acceptAndOpen() throws IOException {
        try {
            Socket soc = ssocket.accept();
            com.sun.squawk.io.j2se.socket.Protocol con =
                new com.sun.squawk.io.j2se.socket.Protocol();
            con.open(soc, parameters);
            return con;
        } catch (SocketTimeoutException e) {
            throw new InterruptedIOException();
        }
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    public InputStream openInputStream() throws IOException {
        throw new RuntimeException();
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        throw new RuntimeException();
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    public void close() throws IOException {
        ssocket.close();
    }

}
