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

package com.sun.squawk.io.j2se.systemproperties;

import java.io.*;
import java.util.*;
import javax.microedition.io.*;
import com.sun.squawk.io.*;
import java.net.*;

/**
 * Simple protocol to read system properties.
 *
 */
public class Protocol extends ConnectionBase implements InputConnection {

    /**
     * Open the connection
     * @param name       the target for the connection
     * @param timeouts   a flag to indicate that the called wants
     *                   timeout exceptions
     */
     public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {
         if (name.length() != 0) {
             throw new IllegalArgumentException( "Bad protocol option:" + name);
         }
         return this;
     }


    /**
     * Return the system properties as a stream of UTF8 encoded <name,value> pairs.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    @SuppressWarnings(value = "unchecked")
    public InputStream openInputStream() throws IOException {
        Map properties = new TreeMap(System.getProperties());

        // Try to add the localhost address and name
        try {
            InetAddress localhost = InetAddress.getLocalHost();

            // Don't add the local address if it is just the loopback address as this has
            // no meaning when sent to other machines.
            if (!localhost.isLoopbackAddress()) {
                properties.put("net.localhost.address", localhost.getHostAddress());
                properties.put("net.localhost.name", localhost.getHostName());
            }

        } catch (UnknownHostException e) {
            // Oh well, we can't get this info
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (Iterator iterator = properties.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry)iterator.next();
            dos.writeUTF((String)entry.getKey());
            dos.writeUTF((String)entry.getValue());
        }
        baos.close();
        return new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
    }

    /**
     * Test driver.
     */
    public static void main(String[] args) throws IOException {
        DataInputStream propertiesStream = Connector.openDataInputStream("systemproperties:");
        Map<String, String> properties = new TreeMap<String, String>();
        while (propertiesStream.available() != 0) {
            properties.put(propertiesStream.readUTF(), propertiesStream.readUTF());
        }
        for (Iterator i = properties.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry e = (Map.Entry)i.next();
            System.out.println(e.getKey() + "=" + e.getValue());
        }
    }
}
