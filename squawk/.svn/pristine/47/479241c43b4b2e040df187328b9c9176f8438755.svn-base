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

package com.sun.squawk.io.j2se.deletefiles;

import java.io.*;
import javax.microedition.io.*;
import com.sun.squawk.io.j2se.UniversalFilterOutputStream;
import com.sun.squawk.io.ConnectionBase;

/**
 * GenericStreamConnection to delete files using the J2SE File API.
 *
 * @version 1.0 04/04/2005
 */
public class Protocol extends ConnectionBase implements StreamConnection {

    /**
     * Opens the connection
     */
    public void open(String name, int mode, boolean timeouts) throws IOException {
        throw new RuntimeException("Should not be called");
    }

    /**
     * Opens a connection to delete one or more files.  Files are deleted when the
     * connection is closed.
     *
     * @param name the target for the connection (including any parameters).
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {
        if (name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException("Protocol must start with \"//\" " + name);
        }

        return this;
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    public InputStream openInputStream() throws IOException {
       throw new IOException("Input stream not available");
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        OutputStream output = new ByteArrayOutputStream() {
            public void close() throws IOException {
                // Look for each file.
//System.err.println("closing deletefiles:// output stream");
                DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf, 0, count));
                try {
                    String path = dis.readUTF();
                    File file = new File(path);
                    file.delete();
                } catch (EOFException e) {
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        OutputStream res = new UniversalFilterOutputStream(this, output);
        return res;
    }

    /**
     * Closes the connection and deletes the files.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    public void close() throws IOException {
//System.err.println("closing deletefiles://");
        }
    }
