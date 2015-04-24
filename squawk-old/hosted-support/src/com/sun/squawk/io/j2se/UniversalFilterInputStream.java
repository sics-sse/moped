/*
 * Copyright 2000-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.io.j2se;

import java.io.*;
import javax.microedition.io.*;
import java.net.*;

/**
 * This class is a an extension of the J2SE class FilterInputStream.
 * It has the same interface as FilterInputStream but in addition
 * it extends UniversalInputStream and so provides all the functionality
 * therein. This class is useful for converting a regular InputStream
 * into a UniversalInputStream.
 *
 * @version 1.0 1/7/2000
 */
public
class UniversalFilterInputStream extends InputStream {
    /**
     * The input stream to be filtered.
     */
    protected InputStream in;

    /**
     * A flag to show if reset() will seek to the start of the data
     */
    protected boolean marked = false;

    /**
     * Connection object that needs closing
     */
    protected Connection con;

    /**
     * Logging stream.
     */
    protected OutputStream log;

    /**
     * Creates a <code>UniversalFilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use.
     *
     * @param   in   the underlying input stream.
     */
    public UniversalFilterInputStream(InputStream in) {
        this.in = new BufferedInputStream(in);
        if(in.markSupported()) {
            try {
                in.mark(in.available());
                marked = true;
            } catch(IOException x) {
            }
        }
    }

    /**
     * Creates a <code>UniversalFilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use. Also accepts a Connection
     * parameter that must be closed after the stream is closed
     *
     * @param   con the connection which must be closed when this stream is closed
     * @param   in   the underlying input stream.
     */
    public UniversalFilterInputStream(Connection con, InputStream in) {
        this(in);
        this.con = con;
    }

    /**
     * Creates a <code>UniversalFilterInputStream</code>
     * by assigning the  argument <code>in</code>
     * to the field <code>this.in</code> so as
     * to remember it for later use. Also accepts a Connection
     * parameter that must be closed after the stream is closed
     *
     * @param   con the connection which must be closed when this stream is closed
     * @param   in   the underlying input stream.
     */
    public UniversalFilterInputStream(Connection con, InputStream in, String logURL) throws IOException {
        this(con, in);
        this.log = Connector.openOutputStream(logURL);
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        javax.microedition.io.UniversalFilterInputStream#in
     */
    public int read() throws IOException {
        try {
            int b = in.read();
            if (log != null) {
                log.write(b);
                log.flush();
            }
            return b;
        } catch (SocketTimeoutException e) {
            throw new InterruptedIOException();
        }
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. This method blocks until some input is
     * available.
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code>
     * and returns the result.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        javax.microedition.io.UniversalFilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        try {
            int count = in.read(b, off, len);
            if (log != null && count != -1) {
                log.write(b, off, count);
                log.flush();
            }
            return count;
        } catch (SocketTimeoutException e) {
            throw new InterruptedIOException();
        }
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the
     * input stream. The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly <code>0</code>. The actual number of bytes skipped is
     * returned.
     * <p>
     * This method
     * simply performs <code>in.skip(n)</code>.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        try {
            return in.skip(n);
        } catch (SocketTimeoutException e) {
            throw new InterruptedIOException();
        }
    }

    /**
     * Returns the number of bytes that can be read from this input
     * stream without blocking.
     * <p>
     * This method
     * simply performs <code>in.available(n)</code> and
     * returns the result.
     *
     * @return     the number of bytes that can be read from the input stream
     *             without blocking.
     * @exception  IOException  if an I/O error occurs.
     * @see        javax.microedition.io.UniversalFilterInputStream#in
     */
    public int available() throws IOException {
        return in.available();
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     * This
     * method simply performs <code>in.close()</code>.
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        javax.microedition.io.UniversalFilterInputStream#in
     */
    public void close() throws IOException {
        in.close();
        if(con != null) {
            con.close();
        }
        if (log != null) {
            log.close();
        }
    }

    /**
     * Marks the current position in this input stream. A subsequent
     * call to the <code>reset</code> method repositions this stream at
     * the last marked position so that subsequent reads re-read the same bytes.
     * <p>
     * The <code>readlimit</code> argument tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     * <p>
     * This method simply performs <code>in.mark(readlimit)</code>.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     javax.microedition.io.UniversalFilterInputStream#in
     * @see     javax.microedition.io.UniversalFilterInputStream#reset()
     */
    public synchronized void mark(int readlimit) {
        marked = false; /* If the caller is marking then we cannot */
        in.mark(readlimit);
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     * <p>
     * This method
     * simply performs <code>in.reset()</code>.
     * <p>
     * Stream marks are intended to be used in
     * situations where you need to read ahead a little to see what's in
     * the stream. Often this is most easily done by invoking some
     * general parser. If the stream is of the type handled by the
     * parse, it just chugs along happily. If the stream is not of
     * that type, the parser should toss an exception when it fails.
     * If this happens within readlimit bytes, it allows the outer
     * code to reset the stream and try another parser.
     *
     * @exception  IOException  if the stream has not been marked or if the
     *               mark has been invalidated.
     * @see        javax.microedition.io.UniversalFilterInputStream#in
     * @see        javax.microedition.io.UniversalFilterInputStream#mark(int)
     */
    public synchronized void reset() throws IOException {
        in.reset();
    }

    /**
     * Tests if this input stream supports the <code>mark</code>
     * and <code>reset</code> methods.
     * This method
     * simply performs <code>in.markSupported()</code>.
     *
     * @return  <code>true</code> if this stream type supports the
     *          <code>mark</code> and <code>reset</code> method;
     *          <code>false</code> otherwise.
     * @see     javax.microedition.io.UniversalFilterInputStream#in
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    public boolean markSupported() {
        return in.markSupported();
    }

    /**
     * Seek to a position in the stream
     *
     * @param pos the offset in bytes from the start of the data
     */
    public void seek(long pos) throws IOException, IllegalAccessException {
        try {
            if (marked) {
                in.reset();
                in.skip((int)pos);
            } else {
                throw new IllegalAccessException();
            }
        } catch (SocketTimeoutException e) {
            throw new InterruptedIOException();
        }
    }
}
