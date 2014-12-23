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

package com.sun.squawk.io.j2se.zip;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.microedition.io.*;
import com.sun.squawk.io.j2se.*;
import com.sun.squawk.io.*;

/**
 * "zip://file.zip/path/filename.ext"
 *
 * @version 1.0 10/08/99
 */

public class Protocol extends ConnectionBase implements StreamConnection {

    /** InputStream object */
    InputStream is;

    /** Zip file handle */
    ZipFile  z;

    /** Open count */
    int opens = 0;

    /**
     * Open the connection
     */
    public void open(String name, int mode, boolean timeouts) throws IOException {
        throw new RuntimeException("Should not be called");
    }

    /**
     * Open the connection
     * @param name the target for the connection
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     * <p>
     * The name string for this protocol should be:
     * "<name or IP number>:<port number>
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {

        if(name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException("Protocol must start with \"//\" "+name);
        }

        name = name.substring(2);

        int index = name.indexOf('@');
        if(index == -1) {
            throw new IllegalArgumentException("Bad zip protocol: " + name);
        }

        String zipname  = name.substring(0, index);
        String filename = name.substring(index+1);

        if (mode == Connector.READ) {
            try {
                if (name.endsWith("/")) {
                    is = getListingFor(zipname, filename);
                } else {
                    z = new ZipFile(zipname);
                    ZipEntry e = z.getEntry(filename);
                    if (e != null) {
                        is = z.getInputStream(e);
                    } else {
                        throw new ConnectionNotFoundException(name);
                    }
                }
            } catch (IOException ex) {
                throw new ConnectionNotFoundException(name);
            }
        } else {
            throw new IllegalArgumentException("Bad mode");
        }
        opens++;
        return this;
    }

    /**
     * Returns a directory listing
     */
    private InputStream getListingFor(String zipName, String fileName) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        boolean recursive = fileName.endsWith("//");
        if (recursive) {
            fileName = fileName.substring(0, fileName.length() - 1);
        }
        if (fileName.equals("/")) {
            fileName = "";
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipName);

            Enumeration e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry)e.nextElement();
                String name = zipEntry.getName();
                if (name.startsWith(fileName) && name.charAt(name.length() - 1) != '/' &&  (recursive || name.indexOf('/', fileName.length()) == -1)) {
                    dos.writeUTF(name);
                }
            }
        } catch (IOException ioe) {
            throw new ConnectionNotFoundException(zipName);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException ex) {
                }
            }
        }

        dos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    public InputStream openInputStream() throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("Bad mode");
        }
        InputStream res = new UniversalFilterInputStream(this, is);
        is = null;
        opens++;
        return res;
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        throw new IllegalArgumentException("Bad mode");
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    public void close() throws IOException {
        if (opens > 0) {
            opens--;
            if (opens == 0 && z != null) {
                z.close();
            }
        }
    }

}
