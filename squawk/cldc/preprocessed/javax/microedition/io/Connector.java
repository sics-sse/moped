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

package javax.microedition.io;

import java.io.*;
import java.util.Hashtable;

import com.sun.squawk.VM;
import com.sun.squawk.io.*;
import com.sun.squawk.platform.Platform;

/**
 * This class is a factory for creating new Connection objects.
 * <p>
 * The creation of Connections is performed dynamically by looking
 * up a protocol implementation class whose name is formed from the
 * platform name (read from a system property) and the protocol name
 * of the requested connection (extracted from the parameter string
 * supplied by the application programmer.)
 *
 * The parameter string that describes the target should conform
 * to the URL format as described in RFC 2396.
 * This takes the general form:
 * <p>
 * <code>{scheme}:[{target}][{params}]</code>
 * <p>
 * where <code>{scheme}</code> is the name of a protocol such as
 * <i>http</i>}.
 * <p>
 * The <code>{target}</code> is normally some kind of network
 * address.
 * <p>
 * Any <code>{params}</code> are formed as a series of equates
 * of the form ";x=y".  Example: ";type=a".
 * <p>
 * An optional second parameter may be specified to the open
 * function. This is a mode flag that indicates to the protocol
 * handler the intentions of the calling code. The options here
 * specify if the connection is going to be read (READ), written
 * (WRITE), or both (READ_WRITE). The validity of these flag
 * settings is protocol dependent. For instance, a connection
 * for a printer would not allow read access, and would throw
 * an IllegalArgumentException. If the mode parameter is not
 * specified, READ_WRITE is used by default.
 * <p>
 * An optional third parameter is a boolean flag that indicates
 * if the calling code can handle timeout exceptions. If this
 * flag is set, the protocol implementation may throw an
 * InterruptedIOException when it detects a timeout condition.
 * This flag is only a hint to the protocol handler, and it
 * does not guarantee that such exceptions will actually be thrown.
 * If this parameter is not set, no timeout exceptions will be
 * thrown.
 * <p>
 * Because connections are frequently opened just to gain access
 * to a specific input or output stream, four convenience
 * functions are provided for this purpose.
 *
 * See also: {@link DatagramConnection DatagramConnection}
 * for information relating to datagram addressing
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   CLDC 1.0
 */

public class Connector {

/*
 * Implementation notes: The open parameter is used for
 * dynamically constructing a class name in the form:
 * <p>
 * <code>com.sun.cldc.io.{platform}.{protocol}.Protocol</code>
 * <p>
 * The platform name is derived from the system by looking for
 * the system property "microedition.platform".  If this property
 * key is not found or the associated class is not present, then
 * one of two default directories are used. These are called
 * "j2me" and "j2se". If the property "microedition.configuration"
 * is non-null, then "j2me" is used, otherwise "j2se" is assumed.
 * <p>
 * The system property "microedition.protocolpath" can be used to
 * change the root of the class space that is used for looking
 * up the protocol implementation classes.
 * <p>
 * The protocol name is derived from the parameter string
 * describing the target of the connection. This takes the from:
 * <p>
 * <code> {protocol}:[{target}][ {params}] </code>
 * <p>
 * The protocol name is used for dynamically finding the
 * appropriate protocol implementation class.  This information
 * is stripped from the target name that is given as a parameter
 * to the open() method.
 */

    /**
     * Access mode READ.
     */
    public final static int READ  = 1;

    /**
     * Access mode WRITE.
     */
    public final static int WRITE = 2;

    /**
     * Access mode READ_WRITE.
     */
    public final static int READ_WRITE = (READ|WRITE);

    /**
     * Name of host system. (j2se/j2me/parm/ipaq etc.)
     */
     private static String host;

    /**
     * The root of the classes.
     */
    private static String classRoot;

    /**
     * Class initializer.
     */
    static {
        host = "j2me";
        classRoot = "com.sun.squawk.io";

        /* Get the system configuration name */
        if (System.getProperty("microedition.configuration") == null) {
            host = "j2se"; /* Use "j2se" if none is specified */
        }

        /* See if there is an alternate protocol class root path */
        String propertyClassRoot = System.getProperty("javax.microedition.io.Connector.protocolpath");
        if (propertyClassRoot != null) {
            classRoot = propertyClassRoot;
        }
    }

    /**
     * Prevent instantiation of this class.
     */
    private Connector() { }

    /**
     * Create and open a Connection.
     *
     * @param name             The URL for the connection.
     * @return                 A new Connection object.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the requested connection
     *   cannot be made, or the protocol type does not exist.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public static Connection open(String name) throws IOException {
        return open(name, READ_WRITE);
    }

    /**
     * Create and open a Connection.
     *
     * @param name             The URL for the connection.
     * @param mode             The access mode.
     * @return                 A new Connection object.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the requested connection
     *   cannot be made, or the protocol type does not exist.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public static Connection open(String name, int mode) throws IOException {
        return open(name, mode, false);
    }

    /**
     * Create and open a Connection.
     *
     * @param name             The URL for the connection
     * @param mode             The access mode
     * @param timeouts         A flag to indicate that the caller
     *                         wants timeout exceptions
     * @return                 A new Connection object
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException if the requested connection
     * cannot be made, or the protocol type does not exist.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public static Connection open(String name, int mode, boolean timeouts) throws IOException {
        /* Test for null argument */
        if (name == null) {
            throw new IllegalArgumentException("Null URI");
        }

        /* Look for : as in "http:", "file:", or whatever */
        int colon = name.indexOf(':');

        /* Test for null argument */
        if (colon < 1) {
            throw new IllegalArgumentException("no ':' in URI");
        }

        /* Strip off the protocol name */
        String protocol = name.substring(0, colon);

        /* Strip the protocol name from the rest of the string */
        name = name.substring(colon+1);

//        VM.print("@@@ Connector.java - open() - before call openPrim() - protocol: ");VM.print(protocol);VM.print(", name: ");VM.print(name);VM.print(", host: ");VM.print(host);VM.print(", classRoot: ");VM.println(classRoot);
        /* First try for specific host class */
        Connection result = openPrim(protocol, protocol, name, mode, timeouts, host, classRoot);
        if (result == null) {
            if (host.equals("j2me")) {
                // Try the phoneme set of protocols
                result = openPrim(protocol, protocol, name, mode, timeouts, "j2me", "com.sun.midp.io");
            } else {
                // make sure we try j2me before giving up, but only if the first try was not already j2me
                result = openPrim(protocol, protocol, name, mode, timeouts, "j2me", classRoot);
            }
            if (result == null && Platform.IS_DELEGATING) {
                // try to channel out to embedded JVM
                result = openPrim("channel", protocol, name, mode, timeouts, host, classRoot);
            }
            if (result == null) {
                throw new ConnectionNotFoundException("The '"+protocol+"' protocol does not exist");
            }
        }
        
        return result;
    }

    /**
     * Create and open a Connection.
     *
     * @param protocolClassName The URL protocol
     * @param name             The URL for the connection
     * @param mode             The access mode
     * @param timeouts         A flag to indicate that the caller
     *                         wants timeout exceptions
     * @param platform         Platform name
     * @return                 A new Connection object, or null if protocol class not found.
     *
     * @exception IOException If some other kind of I/O error occurs.
     */
    private static Connection openPrim(String protocolClassName, String protocolName, String name, int mode, boolean timeouts, String platform, String packageRoot) throws IOException {
        try {
            ConnectionBase con;


////            if (platform.equals("j2me") && protocolName.equals("msg")) {
////                con = MessageConector.allocateClientProtocol();
////            } else

            {
                /*
                 * Use the platform and protocol names to look up a class
                 * to implement the connection and construct a new instance
                 */
                String fullclassname = packageRoot + "." + platform + "." + protocolClassName + ".Protocol";
                Class clazz = null;
                
                if (protocolTable == null) { // bootstrapping gets in the way. Wait until class is initialized before getting fancy
                    try {
                        clazz = Class.forName(fullclassname);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                } else {
                    // cache protocol classes to avoid many linear searches
                    Object classOrNot = protocolTable.get(fullclassname);
                    if (classOrNot == null) {
                        try {
//                        	VM.print("@@@ Connector.java - openPrim() - before call Class.forName - fullclassname: ");VM.println(fullclassname);
                            clazz = Class.forName(fullclassname);
                            protocolTable.put(fullclassname, clazz);
                        } catch (ClassNotFoundException e) {
                            protocolTable.put(fullclassname, NO_CLASS_FOUND);
                            return null;
                        }
                    } else if (classOrNot == NO_CLASS_FOUND) {
                        return null;
                    } else {
                        clazz = (Class)classOrNot;
                    }
                }
                con = (ConnectionBase)clazz.newInstance();
            }
            /* Open the connection, and return it */
            return con.open(protocolName, name, mode, timeouts);

        } catch (InstantiationException x) {
            throw new IOException(x.toString());
        } catch (IllegalAccessException x) {
            throw new IOException(x.toString());
        } catch (ClassCastException x) {
            throw new IOException(x.toString());
        }
    }

    // support for class caching in openPrim().
    private final static String NO_CLASS_FOUND = "NO PROTOCOL CLASS";
    private final static Hashtable protocolTable = new Hashtable(3);

    /**
     * Create and open a connection input stream.
     *
     * @param  name            The URL for the connection.
     * @return                 An InputStream.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the target of the
     *   name cannot be found, or if the requested protocol type
     *   is not supported.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public static InputStream openInputStream(String name) throws IOException {

        return openDataInputStream(name);
    }

    /**
     * Create and open a connection output stream.
     *
     * @param  name            The URL for the connection.
     * @return                 An OutputStream.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the target of the
     *   name cannot be found, or if the requested protocol type
     *   is not supported.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public static OutputStream openOutputStream(String name) throws IOException {

        return openDataOutputStream(name);
    }

    /**
     * Create and open a connection input stream.
     *
     * @param  name            The URL for the connection.
     * @return                 A DataInputStream.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the target of the
     *   name cannot be found, or if the requested protocol type
     *   is not supported.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public static DataInputStream openDataInputStream(String name) throws IOException {
	
  	    InputConnection con = null;
        try {
            con = (InputConnection)Connector.open(name, Connector.READ);
        } catch (ClassCastException e) {
            throw new IOException(e.toString());
        }

        try {
            return con.openDataInputStream();
        } finally {
            con.close();
        }
    }

    /**
     * Create and open a connection output stream.
     *
     * @param  name            The URL for the connection.
     * @return                 A DataOutputStream.
     *
     * @exception IllegalArgumentException If a parameter is invalid.
     * @exception ConnectionNotFoundException If the target of the
     *   name cannot be found, or if the requested protocol type
     *   is not supported.
     * @exception IOException  If some other kind of I/O error occurs.
     */
    public static DataOutputStream openDataOutputStream(String name) throws IOException {
        OutputConnection con = null;
        try {
            con = (OutputConnection)Connector.open(name, Connector.WRITE);
        } catch (ClassCastException e) {
            throw new IOException(e.toString());
        }

        try {
            return con.openDataOutputStream();
        } finally {
            con.close();
        }
    }



/////**
//// * This class exists so the above code will execute in a J2SE system where
//// * com.sun.squawk.io.j2me.msg.MessageResourceManager is not availible.
//// */
////class MessageConector {
////    /**
////     * Allocates a client protocol object
////     */
////    public static ConnectionBase allocateClientProtocol() {
////        return com.sun.squawk.io.j2me.msg.MessageResourceManager.allocateClientProtocol();
////    }
////}

}
