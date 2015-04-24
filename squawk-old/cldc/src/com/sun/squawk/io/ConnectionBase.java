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

package com.sun.squawk.io;

import java.io.*;
import javax.microedition.io.*;

/**
 * Base class for Connection protocols.
 */
abstract public class ConnectionBase implements Connection {

    /**
     * Open a connection to a target.
     *
     * @param protocol         The URL protocol
     * @param name             The URL for the connection
     * @param mode             The access mode
     * @param timeouts         A flag to indicate that the caller
     *                         wants timeout exceptions
     * @return                 A new Connection object
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the connection cannot
     *                                        be found.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    abstract public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException, ConnectionNotFoundException;

    /**
     * Open and return a data input stream for a connection.
     *
     * @return                 An input stream
     * @exception IOException  If an I/O error occurs
     */
    public InputStream openInputStream() throws IOException {
        return null;
    }

    /**
     * Open and return a data output stream for a connection.
     *
     * @return                 An input stream
     * @exception IOException  If an I/O error occurs
     */
    public OutputStream openOutputStream() throws IOException {
        return null;
    }

    /**
     * Close
     *
     * @exception IOException  If an I/O error occurs
     */
    public void close() throws IOException {
    }


    /**
     * Create and open a connection input stream.
     *
     * @return                 A DataInputStream.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the connection cannot
     *                                        be found.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public DataInputStream openDataInputStream() throws IOException, ConnectionNotFoundException {
        InputStream is = openInputStream();
        if (!(is instanceof DataInputStream)) {
           is = new DataInputStream(is);
        }
        return (DataInputStream)is;
    }

    /**
     * Create and open a connection output stream.
     *
     * @return                 A DataOutputStream.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the connection cannot
     *                                        be found.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public DataOutputStream openDataOutputStream() throws IOException, ConnectionNotFoundException {
        OutputStream os = openOutputStream();
        if (!(os instanceof DataOutputStream)) {
           os = new DataOutputStream(os);
        }
        return (DataOutputStream)os;
    }
}


