/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk;

import com.sun.squawk.util.*;


/**
 * A byte buffer encoder can be used to build a byte array of values encoded
 * using a space-saving encoding. The byte array can be decoded with
 * a {@link ByteBufferDecoder byte buffer decoder}.
 *
 */
public final class ByteBufferEncoder {

    /**
     * The amount of data written to the buffer.
     */
    protected int count;

    /**
     * The data written to this buffer.
     */
    protected byte[] buffer = new byte[32];

    /**
     * Clears the buffer.
     */
    public void reset() {
        count = 0;
    }

    ByteBufferEncoder() {}

    /**
     * Ensures that the buffer is large enough.
     *
     * @param needed  the extra amount of data about to be written
     */
    protected final void ensure(int needed) {
        Assert.that(needed > 0);
        int remain = buffer.length - count;
        Assert.that(remain >= 0);
        if (remain < needed) {
            int newCapacity = count + needed + 32;
            byte newData[] = new byte[newCapacity];
            System.arraycopy(buffer, 0, newData, 0, count);
            buffer = newData;
        }
        Assert.that(buffer.length - count >= needed);
    }

    /**
     * Adds an unencoded byte to the buffer.
     *
     * @param value the byte to be added.
     */
    public final void addUnencodedByte(int value) {
        ensure(1);
        buffer[count++] = (byte)value;
    }

    /**
     * Adds an unsigned byte value to the buffer.
     *
     * @param value  the byte to be added (must be between 0 and 0xFF).
     */
    public final void addUnsignedByte(int value) {
        Assert.that(value >= 0 && value <= 0xFF);
        addUnencodedByte(value);
    }

    /**
     * Adds an unsigned short value to the buffer. The number of bytes
     * added to the buffer will be 1 if <code>value</code> is less than
     * 128 otherwise the value will be encoded in 2 bytes.
     *
     * @param value  the byte to be added (must be between 0 and 0xFFFF).
     */
    public final void addUnsignedShort(int value) {
        Assert.that(value >= 0 && value <= 0xFFFF);
        addUnsignedInt(value);
    }

    /**
     * Adds an unsigned integer whose value must be between 0 and 0x0FFFFFFF.
     * The number of bytes added to the buffer is given below:
     * <p><blockquote><pre>
     *     Value range               Bytes used for encoding
     *     0       .. 127                 1
     *     128     .. 16383               2
     *     16384   .. 2097151             3
     *     2097152 .. 268435455           4
     * </pre></blockquote></p>
     *
     * @param value  the unsigned integer value to add (must be between
     *               0 and 0x0FFFFFFF)
     */
    void addUnsignedInt(int value) {
        Assert.that(value >= 0 && value < 0x0FFFFFFF);
        if (value < 128) {
            /* 0xxxxxxx */
            ensure(1);
            buffer[count++] = (byte)value;
        } else if (value < 16384) {
            /* 1xxxxxxx 0xxxxxxx */
            ensure(2);
            buffer[count++] = (byte)(((value >> 0) & 0x7F) | 0x80);
            buffer[count++] = (byte)(value >> 7);
        } else if (value < 2097152) {
            /* 1xxxxxxx 1xxxxxxx 0xxxxxxx */
            ensure(3);
            buffer[count++] = (byte)(((value >> 0) & 0x7F) | 0x80);
            buffer[count++] = (byte)(((value >> 7) & 0x7F) | 0x80);
            buffer[count++] = (byte)(value >> 14);
        } else {
            /* 1xxxxxxx 1xxxxxxx 1xxxxxxx 0xxxxxxx */
            ensure(4);
            buffer[count++] = (byte)(((value >> 0) & 0x7F) | 0x80);
            buffer[count++] = (byte)(((value >> 7) & 0x7F) | 0x80);
            buffer[count++] = (byte)(((value >> 14) & 0x7F) | 0x80);
            buffer[count++] = (byte)(value >> 21);
        }
    }


    /**
     * ASSERTION ONLY CODE:
     * Determines if a given string is equal to the UTF8 encoded string at
     * a given position in this buffer.
     *
     * @param   s     the string to test
     * @param   pos   the position of the encoded UTF8 string to test against
     * @return  true if the UTF8 encoded string at <code>pos</code> in this
     *                buffer equals <code>s</code>
     */
    private boolean equalsUtf8(String s, int pos) {
/*if[ASSERTIONS_ENABLED]*/
        ByteBufferDecoder decoder = new ByteBufferDecoder(buffer, pos);
        int length = decoder.readUnsignedShort();
        if (length != s.length()) {
            return false;
        }
        for (int i = 0; i != length; i++) {
            char c = decoder.readChar();
            if (c != s.charAt(i)) {
                return false;
            }
        }
/*end[ASSERTIONS_ENABLED]*/
        return true;
    }

    /**
     * Adds a string to the buffer in UTF8 encoded form. The length of the
     * string is prepended with {@link #addUnsignedShort(int)}.
     *
     * @param s  the string to add
     */
    public void addUtf8(String s) {
        int strlen = s.length();
        int max = strlen;
        int start = count;
        addUnsignedShort(strlen);
        if (max > 0) {
            ensure(max);
            for (int i = 0; i < strlen; i++) {
                int c = s.charAt(i);
                if ((c >= 0x0001) && (c <= 0x007F)) {
                    /* 0xxxxxxx*/
                    buffer[count++] = (byte)c;
                } else if (c > 0x07FF) {
                    /* 1110xxxx 10xxxxxx 10xxxxxx */
                    ensure(max += 3);
                    buffer[count++] = (byte)(0xE0 | ((c >> 12) & 0x0F));
                    buffer[count++] = (byte)(0x80 | ((c >> 6) & 0x3F));
                    buffer[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));
                } else {
                    /* 110xxxxx 10xxxxxx*/
                    ensure(max += 2);
                    buffer[count++] = (byte)(0xC0 | ((c >> 6) & 0x1F));
                    buffer[count++] = (byte)(0x80 | ((c >> 0) & 0x3F));
                }
            }
        }
        Assert.that(equalsUtf8(s, start));
    }

    /**
     * Adds the contents of another ByteBuffer to this buffer. The length
     * of <code>buf</code> is added with {@link #addUnsignedInt(int)} before
     * its contents are.
     *
     * @param buf  the buffer to add
     */
    public void add(ByteBufferEncoder buf) {
        int    bufcount  = buf.count;
        byte[] bufbuffer = buf.buffer;
        Assert.that(bufcount > SymbolParser.STATIC_METHODS, "invalid member length");
        addUnsignedInt(bufcount);
        ensure(bufcount);
        for (int i = 0; i < bufcount; i++) {
            addUnsignedByte(bufbuffer[i] & 0xFF);
        }
    }

    /**
     * Gets the contents of this buffer as a byte array.
     *
     * @return  the contents of this buffer as a byte array
     */
    public byte[] toByteArray() {
        byte[] newbuf = new byte[count];
        System.arraycopy(buffer, 0, newbuf, 0, count);
        return newbuf;
    }

    /**
     * Get the size of the encoded data.
     *
     * @return the size in bytes
     */
    int getSize() {
        return count;
    }

    /**
     * Write the data into VM memory.
     *
     * @param oop the object address
     * @param offset the offset
     */
    void writeToVMMemory(Object oop, int offset) {
        for (int i = 0 ; i < count ; i++) {
            NativeUnsafe.setByte(oop, offset+i, buffer[i]);
        }
    }

}
