/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.platform;

import java.io.IOException;

/**
 * This defines the interface between the GCF sockets protocol and the platform's SocketAdapter.
 * This interface is not designed for other uses.
 */
public interface GCFSockets {

    /**
     * Opens a TCP connection to a server.
     *
     * @param hostname host name
     * @param port TCP port at host
     * @param mode connection mode (ignored, should be read/write?
     *
     * @return a native handle to the network connection.
     * @throws IOException 
     */
    public int open(String hostname, int port, int mode) throws IOException;
    
    /**
     * Opens a server TCP connection to clients.
     * Creates, binds, and listens
     *
     * @param port local TCP port to listen on
     * @param backlog listen backlog.
     *
     * @return a native handle to the network connection.
     * @throws IOException 
     */
    public int openServer(int port, int backlog) throws IOException;

    /**
     * Accept client connections on server socket fd.
     * Blocks until a client connects.
     *
     * @param fd open server socket. See {@link #openServer}.
     *
     * @return a native handle to the network connection.
     * @throws IOException 
     */
    public int accept(int fd) throws IOException;

    /**
     * Reads from the open socket connection.
     *
     * @param handle the socket descriptor
     * @param b the buffer into which the data is read.
     * @param off the start offset in array <code>b</code>
     *            at which the data is written.
     * @param len the maximum number of bytes to read.
     *
     * @return the total number of bytes read into the buffer, or
     *         <tt>-1</tt> if there is no more data because the end of
     *         the stream has been reached.
     * @throws IOException 
     */
    public int readBuf(int handle, byte b[], int off, int len) throws IOException;

    /**
     * Read a byte from the open socket connection.
     * This function will return an unsigned byte (0-255) if data was read,
     * or -1 if EOF was reached.
     *
     * @param fd the socket descriptor
     *
     * @return the byte read or -1
     * @throws IOException 
     */
    public int readByte(int handle) throws IOException;

    /**
     * Read a byte from the open socket connection.
     * This function will return an unsigned byte (0-255) if data was read,
     * or -1 if EOF was reached.
     * This version is passed a temporary buffer instead of allocating one.
     *
     * @param fd the socket descriptor
     * @param b
     * @return the byte read or -1
     * @throws IOException
     */
    public int readByte(int fd, byte[] b) throws IOException;


    /**
     * Writes to the open socket connection.
     *
     * @param handle the socket descriptor
     * @param b the buffer of the data to write
     * @param off the start offset in array <tt>b</tt>
     *            at which the data is written.
     * @param len the number of bytes to write.
     *
     * @return the total number of bytes written
     * @throws IOException 
     */
    public int writeBuf(int handle, byte b[], int off, int len) throws IOException;

    /**
     * Writes to the open socket connection.
     *
     * @param handle the socket descriptor
     * @param b the byte to write
     *
     * @return the total number of bytes written
     * @throws IOException 
     */
    public int writeByte(int handle, int b) throws IOException;

    /**
     * Gets the number of bytes that can be read without blocking.
     *
     * @param handle the socket descriptor
     * @return number of bytes that can be read without blocking
     * @throws IOException 
     */
    public int available(int handle) throws IOException;

    /**
     * Closes the socket connection.
     * @param handle the socket descriptor
     * @throws IOException 
     */
    public void close(int handle) throws IOException;
    
    /**
     * set a socket option
     * 
     * @param socket socket descriptor
     * @param option_name 
     * @param option_value new value
     * @throws IOException on error
     */
    public void setSockOpt(int socket, int option_name, int option_value) throws IOException;
  
    /**
     * get a socket option
     * 
     * @param socket socket descriptor
     * @param option_name 
     * @return option value
     * @throws IOException on error
     */
    public int getSockOpt(int socket, int option_name) throws IOException;

}
