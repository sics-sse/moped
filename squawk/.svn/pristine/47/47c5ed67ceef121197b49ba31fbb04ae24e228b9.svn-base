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

package com.sun.squawk.io.j2me.socket;

import java.io.*;
import javax.microedition.io.*;
import com.sun.squawk.io.ConnectionBase;
import com.sun.squawk.platform.GCFSockets;
import com.sun.squawk.platform.Platform;



/**
 * Connection to the J2ME socket API.
 *
 * @version 1.0 1/16/2000
 */

public class Protocol extends ConnectionBase implements SocketConnection {

    /** Socket object used by native code */
    int handle;

    /** Access mode */
    private int mode;

    /** Open count */
    int opens = 0;

    /** Connection open flag */
    private boolean copen = false;

    /** Input stream open flag */
    protected volatile boolean isopen = false;

    /** Output stream open flag */
    protected volatile boolean osopen = false;
    
    /** port number */
    private int port;
    
    /** remote host name used in open */
    private String remoteHostName;
    
    static GCFSockets gcfSockets = Platform.getGCFSockets();

    /**
     * Open the connection
     * @param name the target for the connection. It must be in this
     *        format: "//<name or IP number>:<port number>"
     * @param mode read/write mode of the connection (currently ignored).
     * @param timeouts A flag to indicate that the called wants timeout
     *        exceptions (currently ignored).
     * @return new connection
     * @throws IOException 
     */
    public Connection open(String protocol, String name, int mode, boolean timeouts)
            throws IOException {
// System.err.println("open: protocol: " + protocol + " name: " + name);

        if (!name.startsWith("//")) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "bad socket connection name: " + name
/* #endif */
            );
        }
        int i = name.indexOf(':');
        if (i < 0) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "bad socket connection name: port missing"
/* #endif */
            );
        }
        remoteHostName = name.substring(2, i);
        
        if (remoteHostName.length() == 0) {
            /*
             * If the open string is "socket://:nnnn" then we regard this as
             * "serversocket://:nnnn"
             */
            /* socket:// and socket://: are also valid serversocket urls */
            com.sun.squawk.io.j2me.serversocket.Protocol con =
                    new com.sun.squawk.io.j2me.serversocket.Protocol();
//System.out.println("Found server socket. Trying name = " + name);
            con.open("serversocket", name, mode, timeouts);
            return con;
        }

        try {
            port = Integer.parseInt(name.substring(i+1));
        } catch (NumberFormatException e) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "bad socket connection name: bad port"
/* #endif */
            );
        }
        synchronized (this) {
//System.err.println("open: hostname: " + hostname + " port: " + port + " mode: " + mode);
            handle = gcfSockets.open(remoteHostName, port, mode);
            opens++;
            copen = true;
            this.mode = mode;
        }
        return this;
     }
    
    /** default constructor used by GCF */
    public Protocol() {
    }
        
   /**
     * Open the connection
     * @param fd the accepted socket handle
     */
    public Protocol(int fd) {
    	synchronized (this) {
            handle = fd;
            opens++;
            copen = true;
            mode = Connector.READ_WRITE;
        }
     }

    /**
     * Ensure connection is open
     */
    void ensureOpen() throws IOException {
        if (!copen) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Connection closed"
/* #endif */
            );
        }
    }

    /**
     * Returns an input stream for this socket.
     *
     * @return     an input stream for reading bytes from this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          input stream.
     */
    synchronized public InputStream openInputStream() throws IOException {
        ensureOpen();
        if ((mode&Connector.READ) == 0) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Connection not open for reading"
/* #endif */
            );
        }
        if (isopen) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Input stream already opened"
/* #endif */
            );
        }
        isopen = true;
        InputStream in = new PrivateInputStream(this);
        opens++;
        return in;
    }

    /**
     * Returns an output stream for this socket.
     *
     * @return     an output stream for writing bytes to this socket.
     * @exception  IOException  if an I/O error occurs when creating the
     *                          output stream.
     */
    synchronized public OutputStream openOutputStream() throws IOException {
        ensureOpen();
        if ((mode&Connector.WRITE) == 0) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Connection not open for writing"
/* #endif */
            );
        }
        if (osopen) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Output stream already opened"
/* #endif */
            );
        }
        osopen = true;
        OutputStream os = new PrivateOutputStream(this);
        opens++;
        return os;
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs when closing the
     *                          connection.
     */
    public synchronized void close() throws IOException {
        if (copen) {
            copen = false;
            realClose();
        }
    }

    /**
     * Close the connection.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized void realClose() throws IOException {
        if (--opens == 0) {
             gcfSockets.close(this.handle);
        }
    }

    public void setSocketOption(byte option, int value) throws IllegalArgumentException, IOException {
        ensureOpen();
        
        gcfSockets.setSockOpt(handle, option, value);
    }

    public int getSocketOption(byte option) throws IllegalArgumentException, IOException {
        ensureOpen();
        return gcfSockets.getSockOpt(handle, option);
    }

    public String getLocalAddress() throws IOException {
        ensureOpen();
        // TODO: better
        // TODO: Call down to getsockname()
        return "127.0.0.1";
    }

    public int getLocalPort() throws IOException {
        ensureOpen();
        // TODO: Call down to getsockname()
        throw new IOException("Not supported yet.");
    }

    public String getAddress() throws IOException {
        ensureOpen();
        return remoteHostName;
    }

    public int getPort() throws IOException {
        ensureOpen();
        return port;
    }

}

/**
 * Input stream for the connection
 */
class PrivateInputStream extends InputStream {

    /**
     * Pointer to the connection
     */
    private volatile Protocol parent;

    /**
     * End of file flag
     */
    boolean eof = false;

    /**
     * Buffer used by readByte()
     */
    private byte[] tmpReadBuf;


    /**
     * Constructor
     * @param pointer to the connection object
     *
     * @exception  IOException  if an I/O error occurs.
     */
    /* public */ PrivateInputStream(Protocol parent) throws IOException {
        this.parent = parent;
        tmpReadBuf = new byte[1];
    }

    /**
     * Check the stream is open
     *
     * @exception  IOException  if it is not.
     */
    void ensureOpen() throws IOException {
        if (parent == null) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Stream closed"
/* #endif */
            );
        }
    }

    /**
     * Reads the next byte of data from the input stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized public int read() throws IOException {
        int res;
        ensureOpen();
        if (eof) {
            return -1;
        }
        res = Protocol.gcfSockets.readByte(parent.handle, tmpReadBuf);
        if (res == -1) {
            eof = true;
        }
        if (parent == null) {
            throw new InterruptedIOException();
        }
        return res;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized public int read(byte b[], int off, int len)
            throws IOException {
        ensureOpen();
        if (eof) {
            return -1;
        }
        if (len == 0) {
            return 0;
        }
        // Check for array index out of bounds, and NullPointerException,
        // so that the native code doesn't need to do it
        int test = b[off];
        test = b[off + len - 1];
        
        int n = Protocol.gcfSockets.readBuf(parent.handle, b, off, len);
        if (n == -1) {
            eof = true;
        }

        if (parent == null) {
            throw new InterruptedIOException();
        }
        return n;
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.
     *
     * @return     the number of bytes that can be read from this input stream.
     * @exception  IOException  if an I/O error occurs.
     */
    synchronized public int available() throws IOException {
        ensureOpen();
        return Protocol.gcfSockets.available(parent.handle);
    }

    /**
     * Close the stream.
     *
     * @exception  IOException  if an I/O error occurs
     */
    public synchronized void close() throws IOException {
        if (parent != null) {
            ensureOpen();
            parent.realClose();
            parent.isopen = false;
            parent = null;
        }
    }

}

/**
 * Output stream for the connection
 */
class PrivateOutputStream extends OutputStream {

    /**
     * Pointer to the connection
     */
    private Protocol parent;

    /**
     * Constructor
     * @param pointer to the connection object
     *
     * @exception  IOException  if an I/O error occurs.
     */
    /* public */ PrivateOutputStream(Protocol parent) throws IOException {
        this.parent = parent;
    }

    /**
     * Check the stream is open
     *
     * @exception  IOException  if it is not.
     */
    void ensureOpen() throws IOException {
        if (parent == null) {
            throw new IOException(
/* #ifdef VERBOSE_EXCEPTIONS */
/// skipped                       "Stream closed"
/* #endif */
            );
        }
    }

    /**
     * Writes the specified byte to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    synchronized public void write(int b) throws IOException {
        ensureOpen();
        while (true) {
            int res = Protocol.gcfSockets.writeByte(parent.handle, b);
            if (res != 0) {
                // IMPL_NOTE: should EOFException be thrown if write fails?
                return;
            }
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    synchronized public void write(byte b[], int off, int len)
            throws IOException {
        ensureOpen();
        if (len == 0) {
            return;
        }

        // Check for array index out of bounds, and NullPointerException,
        // so that the native code doesn't need to do it
        int test = b[off] + b[off + len - 1];

        int n = 0;
        while (true) {
            n += Protocol.gcfSockets.writeBuf(parent.handle, b, off + n, len - n);
            if (n == len) {
                break;
            }
        }
    }

    /**
     * Close the stream.
     *
     * @exception  IOException  if an I/O error occurs
     */
    public synchronized void close() throws IOException {
        if (parent != null) {
            ensureOpen();
            parent.realClose();
            parent.osopen = false;
            parent = null;
        }
    }
    
}
