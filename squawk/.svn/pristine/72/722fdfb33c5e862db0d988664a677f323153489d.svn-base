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

 /**
  * This class is a an extension of the J2SE class FilterOutputStream.
  * It has the same interface as FilterOutputStream but in addition
  * it extends UniversalOutputStream and so provides all the functionality
  * therein. This class is useful for converting a regular OutputStream
  * into a UniversalOutputStream.
  *
  * @version 1.0 1/7/2000
 */
public
class UniversalFilterOutputStream extends OutputStream {
    /**
     * The underlying output stream to be filtered.
     */
    protected OutputStream out;

    /**
     * Connection object that need closing
     */
    protected Connection con;

    /**
     * Logging stream.
     */
    protected OutputStream log;

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param   out   the underlying output stream to be assigned to
     *                the field <tt>this.out</tt> for later use, or
     *                <code>null</code> if this instance is to be
     *                created without an underlying stream.
     */
    public UniversalFilterOutputStream(OutputStream out) {
        this.out = out;
    }

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param   con the connection which must be closed when this stream is closed
     *
     * @param   out   the underlying output stream to be assigned to
     *                the field <tt>this.out</tt> for later use, or
     *                <code>null</code> if this instance is to be
     *                created without an underlying stream.
     */
    public UniversalFilterOutputStream(Connection con, OutputStream out) {
        this.out = new BufferedOutputStream(out);
        this.con = con;
    }

    /**
     * Creates an output stream filter built on top of the specified
     * underlying output stream.
     *
     * @param   con the connection which must be closed when this stream is closed
     *
     * @param   out   the underlying output stream to be assigned to
     *                the field <tt>this.out</tt> for later use, or
     *                <code>null</code> if this instance is to be
     *                created without an underlying stream.
     */
    public UniversalFilterOutputStream(Connection con, OutputStream out, String logURL) throws IOException {
        this(con, out);
        log = Connector.openOutputStream(logURL);
    }

    /**
     * Writes the specified <code>byte</code> to this output stream.
     * <p>
     * The <code>write</code> method of <code>UniversalFilterOutputStream</code>
     * calls the <code>write</code> method of its underlying output stream,
     * that is, it performs <tt>out.write(b)</tt>.
     * <p>
     * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>.
     *
     * @param      b   the <code>byte</code>.
     * @exception  IOException  if an I/O error occurs.
     */

    public void write(int b) throws IOException {
        out.write(b);
        if (log != null) {
            log.write(b);
            log.flush();
        }
    }

    /**
     * Writes <code>b.length</code> bytes to this output stream.
     * <p>
     * The <code>write</code> method of <code>UniversalFilterOutputStream</code>
     * calls its <code>write</code> method of three arguments with the
     * arguments <code>b</code>, <code>0</code>, and
     * <code>b.length</code>.
     * <p>
     * Note that this method does not call the one-argument
     * <code>write</code> method of its underlying stream with the single
     * argument <code>b</code>.
     *
     * @param      b   the data to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.UniversalFilterOutputStream#write(byte[], int, int)
     */
    public void write(byte b[]) throws IOException {
        out.write(b, 0, b.length);
        if (log != null) {
            log.write(b, 0, b.length);
            log.flush();
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified
     * <code>byte</code> array starting at offset <code>off</code> to
     * this output stream.
     * <p>
     * The <code>write</code> method of <code>UniversalFilterOutputStream</code>
     * calls the <code>write</code> method of one argument on each
     * <code>byte</code> to output.
     * <p>
     * Note that this method does not call the <code>write</code> method
     * of its underlying input stream with the same arguments. Subclasses
     * of <code>UniversalFilterOutputStream</code> should provide a more efficient
     * implementation of this method.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     * @see        javax.microedition.io.UniversalFilterOutputStream#write(int)
     */
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
        if (log != null) {
            log.write(b, off, len);
            log.flush();
        }
/*
        if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
            throw new IndexOutOfBoundsException();

        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
*/
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out to the stream.
     * <p>
     * The <code>flush</code> method of <code>UniversalFilterOutputStream</code>
     * calls the <code>flush</code> method of its underlying output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        javax.microedition.io.UniversalFilterOutputStream#out
     */
    public void flush() throws IOException {
        out.flush();
        if (log != null) {
            log.flush();
        }
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with the stream.
     * <p>
     * The <code>close</code> method of <code>UniversalFilterOutputStream</code>
     * calls its <code>flush</code> method, and then calls the
     * <code>close</code> method of its underlying output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        javax.microedition.io.UniversalFilterOutputStream#flush()
     * @see        javax.microedition.io.UniversalFilterOutputStream#out
     */
    public void close() throws IOException {
        try {
          flush();
        } catch (IOException ignored) {
        }
        out.close();
        if(con != null) {
            con.close();
        }
        if (log != null) {
            log.close();
        }
    }
}
