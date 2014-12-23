/*
 * Copyright 1994-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.io.j2me.channel;

import java.io.*;
import com.sun.squawk.vm.ChannelConstants;
import com.sun.squawk.*;

/**
 * ChannelOutputStream
 */
public class ChannelOutputStream extends OutputStream implements DataOutput {

    Protocol parent;
    int channelID;

    public ChannelOutputStream(Protocol parent) throws IOException {
        this.parent = parent;
        this.channelID = parent.channelID.id;
        VM.execIO(ChannelConstants.OPENOUTPUT, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public void flush() throws IOException {
        VM.execIO(ChannelConstants.FLUSH, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public void close() throws IOException {
        VM.execIO(ChannelConstants.CLOSEOUTPUT, channelID, 0, 0, 0, 0, 0, 0, null, null);
        channelID = -1;
        parent.decrementCount();
    }

    public void write(int v) throws IOException {
        VM.execIO(ChannelConstants.WRITEBYTE, channelID, v, 0, 0, 0, 0, 0, null, null);
    }

    public void writeShort(int v) throws IOException {
        VM.execIO(ChannelConstants.WRITESHORT, channelID, v, 0, 0, 0, 0, 0, null, null);
    }

    public void writeChar(int v) throws IOException {
        writeShort(v);
    }

    public void writeInt(int v) throws IOException {
        VM.execIO(ChannelConstants.WRITEINT, channelID, v, 0, 0, 0, 0, 0, null, null);
    }

    public void writeLong(long v) throws IOException {
        VM.execIO(ChannelConstants.WRITELONG, channelID, (int)(v >>> 32), (int)v, 0, 0, 0, 0, null, null);
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        VM.execIO(ChannelConstants.WRITEBUF, channelID, off, len, 0, 0, 0, 0, b, null);
    }
    
    /**
     * Writes to the output stream all the bytes in array <code>b</code>.
     * If <code>b</code> is <code>null</code>,
     * a <code>NullPointerException</code> is thrown.
     * If <code>b.length</code> is zero, then
     * no bytes are written. Otherwise, the byte
     * <code>b[0]</code> is written first, then
     * <code>b[1]</code>, and so on; the last byte
     * written is <code>b[b.length-1]</code>.
     *
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }
    
    /*** utilities to satisfy the DataOutput interface */
    
    /**
     * Writes out a <code>byte</code> to the underlying output stream as
     * a 1-byte value. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>1</code>.
     *
     * @param      v   a <code>byte</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeByte(int v) throws IOException {
        write(v);
    }
    
/*if[FLOATS]*/

    /**
     * Writes a 32 bit float.
     * @param v the float value to be written
     */
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * Writes a 64 bit double.
     * @param v the double value to be written
     */
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

/*end[FLOATS]*/
    
    /**
     * Writes a <code>boolean</code> value to this output stream.
     * If the argument <code>v</code>
     * is <code>true</code>, the value <code>(byte)1</code>
     * is written; if <code>v</code> is <code>false</code>,
     * the  value <code>(byte)0</code> is written.
     * The byte written by this method may
     * be read by the <code>readBoolean</code>
     * method of interface <code>DataInput</code>,
     * which will then return a <code>boolean</code>
     * equal to <code>v</code>.
     *
     * @param      v   the boolean to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeBoolean(boolean v) throws IOException {
        if (v) {
            write(1);
        } else {
            write(0);
        }
    }
    
     /**
     * Writes a string to the underlying output stream as a sequence of
     * characters. Each character is written to the data output stream as
     * if by the <code>writeChar</code> method. If no exception is
     * thrown, the counter <code>written</code> is incremented by twice
     * the length of <code>s</code>.
     *
     * @param      s   a <code>String</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.DataOutputStream#writeChar(int)
     */
    public final void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            writeChar(s.charAt(i));
        }
    }

    /**
     * Writes a string to the underlying output stream using UTF-8
     * encoding in a machine-independent manner.
     * <p>
     * First, two bytes are written to the output stream as if by the
     * <code>writeShort</code> method giving the number of bytes to
     * follow. This value is the number of bytes actually written out,
     * not the length of the string. Following the length, each character
     * of the string is output, in sequence, using the UTF-8 encoding
     * for the character. If no exception is thrown, the counter
     * <code>written</code> is incremented by the total number of
     * bytes written to the output stream. This will be at least two
     * plus the length of <code>str</code>, and at most two plus
     * thrice the length of <code>str</code>.
     *
     * @param      str   a string to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeUTF(String str) throws IOException {
        com.sun.squawk.util.DataOutputUTF8Encoder.writeUTF(str, this, true);
    }

}
