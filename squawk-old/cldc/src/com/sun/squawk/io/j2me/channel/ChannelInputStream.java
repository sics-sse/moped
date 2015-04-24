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
import com.sun.squawk.vm.ChannelConstants;

import java.io.*;
import com.sun.squawk.*;

/**
 * ChannelInputStream
 */
public class ChannelInputStream extends InputStream implements DataInput {

    Protocol parent;
    int channelID;

    public ChannelInputStream(Protocol parent) throws IOException {
        this.parent = parent;
        this.channelID = parent.channelID.id;
        VM.execIO(ChannelConstants.OPENINPUT, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public void close() throws IOException {
        if (channelID != -1) {
            VM.execIO(ChannelConstants.CLOSEINPUT, channelID, 0, 0, 0, 0, 0, 0, null, null);
            channelID = -1;
            parent.decrementCount();
        }
    }

    public int read() throws IOException {
        return VM.execIO(ChannelConstants.READBYTE, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public int readUnsignedShort() throws IOException {
        return VM.execIO(ChannelConstants.READSHORT, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public int readInt() throws IOException {
        return VM.execIO(ChannelConstants.READINT, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public long readLong() throws IOException {
        return VM.execIOLong(ChannelConstants.READLONG, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public int read(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        return VM.execIO(ChannelConstants.READBUF, channelID, off, len, 0, 0, 0, 0, null, b);
    }

    public long skip(long n) throws IOException {
        return VM.execIO(ChannelConstants.SKIP, channelID, (int)(n >>> 32), (int)n, 0, 0, 0, 0, null, null);
    }

    public int available() throws IOException {
        return VM.execIO(ChannelConstants.AVAILABLE, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public void mark(int readlimit) {
        try {
            VM.execIO(ChannelConstants.MARK, channelID, readlimit, 0, 0, 0, 0, 0, null, null);
        } catch (IOException ex) {}

    }

    public void reset() throws IOException {
        VM.execIO(ChannelConstants.RESET, channelID, 0, 0, 0, 0, 0, 0, null, null);
    }

    public boolean markSupported() {
        try {
            int res = VM.execIO(ChannelConstants.MARK, channelID, 0, 0, 0, 0, 0, 0, null, null);
            return res != 0;
        } catch (IOException ex) {
            return false;
        }
    }

     public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the number of bytes to read.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len) {
            int count = read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
            n += count;
        }
    }

    /**
     * Makes an attempt to skip over
     * <code>n</code> bytes
     * of data from the input
     * stream, discarding the skipped bytes. However,
     * it may skip
     * over some smaller number of
     * bytes, possibly zero. This may result from
     * any of a
     * number of conditions; reaching
     * end of file before <code>n</code> bytes
     * have been skipped is
     * only one possibility.
     * This method never throws an <code>EOFException</code>.
     * The actual
     * number of bytes skipped is returned.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the number of bytes skipped, which is always <code>n</code>.
     * @exception  EOFException  if this stream reaches the end before skipping
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) skip(n-total)) > 0)) {
            total += cur;
        }
        return total;
    }

    /**
     * Reads one input byte and returns
     * <code>true</code> if that byte is nonzero,
     * <code>false</code> if that byte is zero.
     * This method is suitable for reading
     * the byte written by the <code>writeBoolean</code>
     * method of interface <code>DataOutput</code>.
     *
     * @return     the <code>boolean</code> value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public boolean readBoolean() throws IOException {
        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (ch != 0);
    }

    /**
     * Reads and returns one input byte.
     * The byte is treated as a signed value in
     * the range <code>-128</code> through <code>127</code>,
     * inclusive.
     * This method is suitable for
     * reading the byte written by the <code>writeByte</code>
     * method of interface <code>DataOutput</code>.
     *
     * @return     the 8-bit value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public byte readByte() throws IOException {
        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return (byte)ch;
    }

    /**
     * Reads one input byte, zero-extends
     * it to type <code>int</code>, and returns
     * the result, which is therefore in the range
     * <code>0</code>
     * through <code>255</code>.
     * This method is suitable for reading
     * the byte written by the <code>writeByte</code>
     * method of interface <code>DataOutput</code>
     * if the argument to <code>writeByte</code>
     * was intended to be a value in the range
     * <code>0</code> through <code>255</code>.
     *
     * @return     the unsigned 8-bit value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public int readUnsignedByte() throws IOException {
        int ch = read();
        if (ch < 0) {
            throw new EOFException();
        }
        return ch;
    }

    /**
     * Reads two input bytes and returns
     * a <code>short</code> value. Let <code>a</code>
     * be the first byte read and <code>b</code>
     * be the second byte. The value
     * returned
     * is:
     * <p><pre><code>(short)((a &lt;&lt; 8) * | (b &amp; 0xff))
     * </code></pre>
     * This method
     * is suitable for reading the bytes written
     * by the <code>writeShort</code> method of
     * interface <code>DataOutput</code>.
     *
     * @return     the 16-bit value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public short readShort() throws IOException {
        return (short)readUnsignedShort();
    }
            
    /**
     * See the general contract of the <code>readChar</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream as a Unicode
     *             character.
     * @exception  EOFException  if this input stream reaches the end before
     *               reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public final char readChar() throws IOException {
        return (char)readUnsignedShort();
    }

/*if[FLOATS]*/

    /**
     * Reads a 32 bit float.
     * @return the read 32 bit float.
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads a 64 bit double.
     * @return the 64 bit double read.
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

/*end[FLOATS]*/

    /**
     * Reads in a string that has been encoded using a modified UTF-8 format.
     * The general contract of <code>readUTF</code>
     * is that it reads a representation of a Unicode
     * character string encoded in Java modified
     * UTF-8 format; this string of characters
     * is then returned as a <code>String</code>.
     * <p>
     * First, two bytes are read and used to
     * construct an unsigned 16-bit integer in
     * exactly the manner of the <code>readUnsignedShort</code>
     * method . This integer value is called the
     * <i>UTF length</i> and specifies the number
     * of additional bytes to be read. These bytes
     * are then converted to characters by considering
     * them in groups. The length of each group
     * is computed from the value of the first
     * byte of the group. The byte following a
     * group, if any, is the first byte of the
     * next group.
     * <p>
     * If the first byte of a group
     * matches the bit pattern <code>0xxxxxxx</code>
     * (where <code>x</code> means "may be <code>0</code>
     * or <code>1</code>"), then the group consists
     * of just that byte. The byte is zero-extended
     * to form a character.
     * <p>
     * If the first byte
     * of a group matches the bit pattern <code>110xxxxx</code>,
     * then the group consists of that byte <code>a</code>
     * and a second byte <code>b</code>. If there
     * is no byte <code>b</code> (because byte
     * <code>a</code> was the last of the bytes
     * to be read), or if byte <code>b</code> does
     * not match the bit pattern <code>10xxxxxx</code>,
     * then a <code>UTFDataFormatException</code>
     * is thrown. Otherwise, the group is converted
     * to the character:<p>
     * <pre><code>(char)(((a&amp; 0x1F) &lt;&lt; 6) | (b &amp; 0x3F))
     * </code></pre>
     * If the first byte of a group
     * matches the bit pattern <code>1110xxxx</code>,
     * then the group consists of that byte <code>a</code>
     * and two more bytes <code>b</code> and <code>c</code>.
     * If there is no byte <code>c</code> (because
     * byte <code>a</code> was one of the last
     * two of the bytes to be read), or either
     * byte <code>b</code> or byte <code>c</code>
     * does not match the bit pattern <code>10xxxxxx</code>,
     * then a <code>UTFDataFormatException</code>
     * is thrown. Otherwise, the group is converted
     * to the character:<p>
     * <pre><code>
     * (char)(((a &amp; 0x0F) &lt;&lt; 12) | ((b &amp; 0x3F) &lt;&lt; 6) | (c &amp; 0x3F))
     * </code></pre>
     * If the first byte of a group matches the
     * pattern <code>1111xxxx</code> or the pattern
     * <code>10xxxxxx</code>, then a <code>UTFDataFormatException</code>
     * is thrown.
     * <p>
     * If end of file is encountered
     * at any time during this entire process,
     * then an <code>EOFException</code> is thrown.
     * <p>
     * After every group has been converted to
     * a character by this process, the characters
     * are gathered, in the same order in which
     * their corresponding groups were read from
     * the input stream, to form a <code>String</code>,
     * which is returned.
     * <p>
     * The <code>writeUTF</code>
     * method of interface <code>DataOutput</code>
     * may be used to write data that is suitable
     * for reading by this method.
     * @return     a Unicode string.
     * @exception  EOFException            if this stream reaches the end
     *             before reading all the bytes.
     * @exception  IOException             if an I/O error occurs.
     * @exception  UTFDataFormatException  if the bytes do not represent a
     *             valid UTF-8 encoding of a string.
     */
    public String readUTF() throws IOException {
        return com.sun.squawk.util.DataInputUTF8Decoder.readUTF(this, false, true);
    }

}
