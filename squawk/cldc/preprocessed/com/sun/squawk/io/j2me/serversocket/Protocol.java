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

package com.sun.squawk.io.j2me.serversocket;

import java.io.*;
import javax.microedition.io.*;
import com.sun.squawk.io.*;
import com.sun.squawk.platform.*;

/**
 * StreamConnectionNotifier for Server Socket API.
 *
 * @author  Nik Shaylor
 * @version 1.0 10/08/99
 */
public class Protocol extends ConnectionBase 
                      implements StreamConnectionNotifier, ServerSocketConnection {

    /** Server Socket object */
   // ServerSocket ssocket;

    static GCFSockets gcfSockets = Platform.getGCFSockets();
    
    /** Socket object used by native code */
    int handle;

//    /** Access mode */
//    private int mode;

    /** Open count */
    int opens = 0;

    /** Connection open flag */
    private boolean copen = false;
    
    /** local port number */
    private int port;
        
    /**
     * Open the connection
     * @param name the target for the connection
     * @param mode a flag that is true if the caller expects to write to the
     *        connection.
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     * <p>
     * The name string for this protocol should be:
     * "<port number>
     * @throws IOException 
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {

        //if(name.charAt(0) != '/' || name.charAt(1) != '/' || name.charAt(2) != ':') {
        if(name.charAt(0) != '/' || name.charAt(1) != '/') { 
            throw new IllegalArgumentException("Protocol must start with \"//\" "+name);
        }

//        /* socket:// case.  System assigned incoming port */
//        if (name.length() == 2) {
//           open();
//           return this;
//        }

        if(name.length() == 2 || name.charAt(2) != ':') { 
            throw new IllegalArgumentException("Protocol must start with \"//:\" "+name);
        }

        name = name.substring(3);
 
        /* socket://: case.  System assigned incoming port */
        if (name.length() == 0) {
           throw new IllegalArgumentException("Protocol must specify port "+name);
//           open();
//           return this;
        }

        try {
            /* Get the port number */
            port = Integer.parseInt(name);
//            checkMIDPPermission(port);
            /* Open the socket: inbound server */
            handle = gcfSockets.openServer(port, 5);
            opens++;
            copen = true;
            return this;
        } catch (NumberFormatException x) {
            throw new IllegalArgumentException("Invalid port number in " + name);
        }
    }

//    /**
//     * Open the connection to an arbitrary system selected port and address
//     */
//     private void open() throws IOException {
//        ssocket = new ServerSocket();
//        /* binding to null to get the system assigned port */
//        ssocket.bind(null);
//     }

    /**
     * Returns a GenericConnection that represents a server side
     * socket connection
     * @return     a socket to communicate with a client.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    public StreamConnection acceptAndOpen() throws IOException {
        int newHandle = gcfSockets.accept(handle);

        return new com.sun.squawk.io.j2me.socket.Protocol(newHandle);
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    synchronized public void close() throws IOException {
        if (copen) {
            copen = false;
            realClose();
        }
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized void realClose() throws IOException {
        if (--opens == 0) {
             gcfSockets.close(this.handle);
        }
    }

    /**
     * Ensure connection is open
     */
    void ensureOpen() throws IOException {
        if (!copen) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Connection closed"
/* #endif */
            );
        }
    }
    
    public String getLocalAddress() throws IOException {
        ensureOpen();
        // TODO: better
        // TODO: Call down to getsockname()
        return "127.0.0.1";
    }

    public int getLocalPort() throws IOException {
        ensureOpen();
        // TODO: Call down to getsockname()
        return port;
    }


}
