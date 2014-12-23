/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * This interface defines the capabilities that a datagram connection must have.
 * <p>
 * Reminder: Since the CLDC Specification does not define any actual network
 * protocol implementations, the syntax for datagram addressing is not defined
 * in the CLDC Specification. Rather, syntax definition takes place at the level
 * of Java ME profiles such as MIDP.
 * <p>
 * In the sample implementation that is provided as part of the CLDC reference
 * implementation, the following addressing scheme is used:
 * <p>
 * The parameter string describing the target of a connection in the CLDC
 * reference implementation takes the following form:
 *
 * <pre>
 *  {protocol}://[{host}]:[{port}]
 * </pre>
 *
 * A datagram connection can be opened in a "client" mode or "server" mode. If
 * the "//{host}" part is missing then the connection is opened as a "server"
 * (by "server", we mean that a client application initiates communication).
 * When the "//{host}" part is specified, the connection is opened as a
 * "client".
 * <p>
 * Examples:
 * <p>
 * A datagram connection for accepting datagrams<br>
 * datagram://:1234
 * <p>
 * A datagram connection for sending to a server:<br>
 * datagram://123.456.789.12:1234
 * <p>
 *
 * Note that the port number in "server mode" (unspecified host name) is that of
 * the receiving port. The port number in "client mode" (host name specified) is
 * that of the target port. The reply-to port in both cases is never
 * unspecified. In "server mode", the same port number is used for both
 * receiving and sending. In "client mode", the reply-to port is always
 * dynamically allocated.
 * <p>
 * Also note that the allocation of datagram objects is done in a more abstract
 * way than in Java 2 Standard Edition (J2SE). Instead of providing a concrete
 * <code>DatagramPacket</code> class, an abstract <code>Datagram</code>
 * interface is provided. This is to allow a single platform to support several
 * different datagram interfaces simultaneously. Datagram objects must be
 * allocated by calling the <code>newDatagram</code> methods of the
 * <code>DatagramConnection</code> object. The resulting object is defined
 * using another interface type called
 * <code>javax.microedition.io.Datagram</code>.
 *
 * @since Java Card 3.next
 */
public interface MulticastConnection extends UDPDatagramConnection {


    /**
     * Gets the local address to which the socket is bound.
     *
     * <P>
     * The host address(IP number) that can be used to connect to this end of
     * the socket connection from an external system. Since IP addresses may be
     * dynamically assigned, a remote application will need to be robust in the
     * face of IP number reasssignment.
     * </P>
     * <P>
     * The local hostname (if available) can be accessed from
     * <code> System.getProperty("microedition.hostname")</code>
     * </P>
     *
     * @return the local address to which the socket is bound.
     * @exception IOException
     *                if the connection was closed.
     * @see ServerSocketConnection
     */
    public String getLocalAddress() throws IOException;

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is connected.
     * @exception IOException
     *                if the connection was closed.
     * @see ServerSocketConnection
     */
    public int getLocalPort() throws IOException;

}
