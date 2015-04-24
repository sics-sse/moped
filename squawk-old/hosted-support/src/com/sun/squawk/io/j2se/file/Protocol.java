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

package com.sun.squawk.io.j2se.file;

import java.io.*;
import java.util.Vector;

import javax.microedition.io.*;
import com.sun.squawk.io.j2se.*;
import com.sun.squawk.io.*;

/**
 * GenericStreamConnection to the J2SE file API.
 *
 * @version 1.0 10/08/99
 */

public class Protocol extends ConnectionBase implements StreamConnection {

    /** FileInputStream object */
    InputStream fis;

    /** FileInputStream object */
    OutputStream fos;

//    /**
//     * Open the connection
//     */
//    public void open(String name, int mode, boolean timeouts) throws IOException {
//        throw new RuntimeException("Should not be called");
//    }

    /**
     * Open the connection to file.
     * @param name the target for the connection (including any parameters).
     * @param timeouts A flag to indicate that the called wants timeout exceptions
     * <p>
     * The name string for this protocol should be:
     *             "<absolute or relative file name>[;<name>=<value>]*"
     *
     *             Any additional parameters must be separated by a ";" and
     *             spaces are not allowed.
     *
     *             The optional parameters are:
     *
     *             append:    Specifies if the file should be opened in append mode. Default is false.
     *             pathentry Specifies a directory path to look for the file in, can have as many as needed.
     * @throws java.io.IOException 
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {
        String fileName = name;
        if(fileName.length() >= 2 && (fileName.charAt(0) != '/' || fileName.charAt(1) != '/')) {
            throw new IllegalArgumentException("Protocol must start with \"//\" "+fileName);
        }

        class Parameters extends ParameterParser {
            boolean append;
            java.util.Vector<String> path = new Vector<String>();
            public boolean parameter(String key, String value) {
                if (key.equals("append")) {
                    append = value.equals("true");
                } else  if (key.equals("pathelement")) {
                    path.add(value);
                } else {
                    return false;
                }
                return true;
            }
        }

        Parameters p = new Parameters();
        fileName = p.parse(fileName.substring(2));


        try {
            if ((mode & Connector.READ) != 0) {
                if (fileName.endsWith("/")) {
                    fis = getListingFor(fileName);
                } else {
                    if (p.path.size() == 0 || new File(fileName).isAbsolute()) {
                        fis = new FileInputStream(fileName);
                    } else {
                        boolean found = false;
                        for (String path : p.path) {
                            try {
                            	File file = new File(path, fileName);
                                fis = new FileInputStream(file);
                                found = true;
                                break;
                            } catch (IOException e) {
                            }
                        }
                        if (!found) {
                            throw new IOException();
                        }
                    }
                }
            }
            if ((mode & Connector.WRITE) != 0) {
                if (p.path.size() == 0 || new File(fileName).isAbsolute()) {
                    fos = new FileOutputStream(fileName, p.append);
                } else {
                    boolean found = false;
                    for (String path : p.path) {
                        try {
                            fos = new FileOutputStream(new File(path, fileName), p.append);
                            found = true;
                            break;
                        } catch (IOException e) {
                        }
                    }
                    if (!found) {
                        throw new IOException();
                    }
                }
            }
        } catch (IOException ex) {
            throw new ConnectionNotFoundException(name);
        }
        return this;
    }

    /**
     * Returns a directory listing
     */
    private InputStream getListingFor(String dirName) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        getListingForPrim(dirName, dos, dirName.endsWith("//"));
        dos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Build a directory listing
     */
    private void getListingForPrim(String dirName, DataOutputStream dos, boolean recursive) throws IOException {

        File dir = new File(dirName.replace('/',File.separatorChar));

        File[] files = dir.listFiles();
        if (files == null) {
            throw new ConnectionNotFoundException(dirName);
        }

        for(int i = 0 ; i < files.length ; i++) {
            File f = files[i];
            if (!f.isDirectory()) {
                dos.writeUTF(f.getPath().replace('\\','/'));
            }
            else {
                if (recursive) {
                    getListingForPrim(f.getPath(), dos, recursive);
                }
            }
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
        if (fis == null) {
            throw new IllegalArgumentException("Bad mode");
        }
        InputStream res = new UniversalFilterInputStream(this, fis);
        fis = null;
        return new BufferedInputStream(res);
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    public OutputStream openOutputStream() throws IOException {
        if (fos == null) {
            throw new IllegalArgumentException("Bad mode");
        }
        OutputStream res = new UniversalFilterOutputStream(this, fos);
        fos = null;
        return res;
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
