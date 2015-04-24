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

package com.sun.squawk.io.j2me.classpath;

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import javax.microedition.io.*;

import com.sun.squawk.io.*;
import com.sun.squawk.io.connections.*;
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.StringTokenizer;
import com.sun.squawk.VM;


/**
 * Simple protocol to set connection variables
 *
 */
public class Protocol extends ConnectionBase implements ClasspathConnection {

    public static final String sepch;

    private static final boolean MULTIPLEINSTANCESCHECK = false;

    static {
        String sep = System.getProperty("path.separator");
        if (sep != null ) {
            sepch = sep;
        } else {
            sepch = "" + VM.getPathSeparatorChar();
        }
    }

    /**
     * The classpath array
     */
    private Vector classPathArray = new Vector();

    /**
     * Open the connection
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {

//System.out.println("classpath: name="+name);

        if(name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new IllegalArgumentException("Protocol must start with \"//\" "+name);
        }

        String path = name.substring(2);

        StringTokenizer st = new StringTokenizer(path, sepch);
        while (st.hasMoreTokens()) {
            String dirName = st.nextToken();
            if (dirName.endsWith("\\") || dirName.endsWith("/")) {
                dirName = dirName.substring(0, dirName.length() - 1);
            }
            classPathArray.addElement(dirName);
        }

        return this;
    }

    /**
     * If <code>fileName</code> ends with "/" or "//" then this is treated as a
     * directory listing request, where a "//" ending implies a recursive listing.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    public InputStream openInputStream(String fileName) throws IOException {

        if (fileName.endsWith("/")) {
            return lookupRequest(fileName);
        }

        InputStream is = null;
        // Must go in reverse order in order to find first match from right to left
        for (int i = classPathArray.size()-1 ; i >= 0; i--) {

           /*
            * Get the section of the classpath array
            */
            String classPathEntry = (String)classPathArray.elementAt(i);

//System.out.println("classPathEntry = "+classPathEntry);

            if (classPathEntry.endsWith(".zip") || classPathEntry.endsWith(".jar")) {

               /*
                * Open the file inside a zip file
                */
                try {
//System.out.println("trying "+"zip://"+classPathEntry+"~"+fileName);
                    InputStream _is = Connector.openDataInputStream("zip://"+classPathEntry+"@"+fileName);
                    if (is == null)  {
                        if (_is != null && !MULTIPLEINSTANCESCHECK) {
                            return _is;
                        }
                        is = _is;
                    } else {
                        _is.close();
                        if (fileName.endsWith(".class")) {
                            System.err.println("Warning: multiple instances of " + fileName + " in class path may cause linkage errors");
                        }
                    }
                } catch (ConnectionNotFoundException e) {
                }

            } else {

               /*
                * Open the file
                */
                try {
//System.out.println("trying "+"file://"+classPathEntry+"/"+fileName);
                    InputStream _is = Connector.openDataInputStream("file://" + classPathEntry + "/" + fileName);
                    if (is == null)  {
                        if (_is != null && !MULTIPLEINSTANCESCHECK) {
                            return _is;
                        }
                        is = _is;
                    } else {
                        _is.close();
                        if (fileName.endsWith(".class")) {
                            System.err.println("Warning: multiple instances of " + fileName + " on class path may cause linkage errors");
                        }
                    }
                } catch (ConnectionNotFoundException e ) {
                }
            }
        }

        if (is == null) {
            throw new ConnectionNotFoundException(fileName);
        }

        return is;
    }


    /**
     * Get a file listing for the pattern.
     */
    public InputStream lookupRequest(String fileName) throws IOException {

        Hashtable table = new Hashtable();

        for (int i = 0  ; i < classPathArray.size() ; i++) {

            DataInputStream is = null;

           /*
            * Get the section of the classpath array
            */
            String classPathEntry = (String)classPathArray.elementAt(i);
            String prefix;
            if (classPathEntry.endsWith(".zip") || classPathEntry.endsWith(".jar")) {

                prefix = null;
               /*
                * Open the file inside a zip file
                */
                try {
                    is = Connector.openDataInputStream("zip://"+classPathEntry+"@"+fileName);
                } catch (ConnectionNotFoundException ex) {
                }

            } else {

                prefix = classPathEntry.replace('\\', '/') + '/';
               /*
                * Open the file
                */
                try {
                    is = Connector.openDataInputStream("file://"+classPathEntry+"/"+fileName);
                } catch (ConnectionNotFoundException ex) {
                }
            }

            if (is != null) {
                try {
                    for (;;) {
                        String str = is.readUTF();
                        // Make entry relative to this classpath entry
                        if (prefix != null && str.startsWith(prefix)) {
                            str = str.substring(prefix.length());
                        }
                        if (table.get(str) == null) {
                            table.put(str, str);
                        }
                    }
                } catch (EOFException ex) {
                }
                is.close();
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        for (Enumeration e = table.keys() ; e.hasMoreElements() ; ) {
            String str = (String)e.nextElement();
            dos.writeUTF(str);
        }
        dos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getBytes(String name) throws IOException {
        DataInputStream input= null;
        try {
            input = new DataInputStream(openInputStream(name));
            int available = input.available();
            byte[] bytes = new byte[available];
            input.readFully(bytes);
            Assert.that(input.available() == 0);
            return bytes;
        } finally {
            if (input != null) {try {input.close();} catch (IOException e1) {}};
        }
    }

/*if[DEBUG_CODE_ENABLED]*/
    /**
     * Test driver.
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            ClasspathConnection classpath = (ClasspathConnection)Connector.open("classpath://j2se/classes;j2me/classes");
            print(classpath, "com/sun/squawk/util/");
            classpath.close();
        }
        else {
            ClasspathConnection classpath = (ClasspathConnection)Connector.open("classpath://" + args[0]);
            for (int i = 1; i != args.length; i++) {
                print(classpath, args[i]);
            }
            classpath.close();
        }
    }

    private static void print(ClasspathConnection classpath, String entry) throws IOException {
        DataInputStream dis = new DataInputStream(classpath.openInputStream(entry));
        System.out.println("Directory: "+entry);
        try {
            for (;;) {
                System.out.println(dis.readUTF());
            }
        } catch (EOFException ex) {
        }
        dis.close();
    }
/*end[DEBUG_CODE_ENABLED]*/
}
