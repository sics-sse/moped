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

package com.sun.squawk.io.j2se.http;

import java.io.*;
import java.net.*;
import javax.microedition.io.*;
import com.sun.squawk.io.j2se.*;

/**
 * HTTP connection for J2SE.
 *
 * @version 1.0 01/03/2000
 */

public class Protocol extends com.sun.squawk.io.ConnectionBase implements ContentConnection {

    /** URL Connection object */
    URLConnection conn = null;

    /**
     * Open the http connection
     * @param name the target for the connection
     * @param mode The access mode
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     * <p>
     * The name string for this protocol should be:
     * "<name or IP number>:<port number>
     */
    //public void open(String name, int mode, boolean timeouts) throws IOException {
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {

        if(name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException("Protocol must start with \"//\" "+name);
        }

        name = name.substring(2);

        URL url = new URL(protocol + "://" + name);
        conn = url.openConnection();
        conn.connect();
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
        return new UniversalFilterInputStream(this, conn.getInputStream());
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        return new UniversalFilterOutputStream(this, conn.getOutputStream());
    }
    /**
     * Returns the value of the <code>content-type</code> header field.
     *
     * @return  the content type of the resource that the URL references,
     *          or <code>null</code> if not known.
     */
    public String getType() {
        return conn.getContentType();
    }

    /**
     * Returns the value of the <code>content-encoding</code> header field.
     *
     * @return  the content encoding of the resource that the URL references,
     *          or <code>null</code> if not known.
     */
    public String getEncoding() {
        return conn.getContentEncoding();
    }

    /*
     * Returns the contents of the specified header field.
     *
     * @param   name   the name of a header field.
     * @return  the value of the named header field, or <code>null</code>
     *          if there is no such field in the header.
     */
    public String getHeaderField(String name) {
        return conn.getHeaderField(name);
    }

    /**
     * Returns the value of the <code>content-length</code> header field.
     *
     * @return  the content length of the resource that this connection's URL
     *          references, or <code>-1</code> if the content length is
     *          not known.
     */
    public long getLength() {
        return conn.getContentLength();
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    public void close() throws IOException {
    }

}
