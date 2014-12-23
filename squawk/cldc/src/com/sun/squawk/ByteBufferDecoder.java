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

package com.sun.squawk;



/**
 * A byte buffer decoder can be used to decode a byte array of values encoded
 * with a {@link ByteBufferEncoder byte buffer encoder}.
 *
 */
public class ByteBufferDecoder extends GeneralDecoder {

    /**
     * The byte array of encoded values.
     */
    protected byte[] buf;

    /**
     * The current decoding position.
     */
    protected int pos;

    /**
     * Creates a ByteBufferDecoder to decode a byte array of values that
     * was encoded by a ByteArryEncoder.
     *
     * @param buf  the byte array of encoded values
     * @param pos  the initial decoding position
     */
    public ByteBufferDecoder(byte[] buf, int pos) {
        this.buf = buf;
        this.pos = pos;
    }

    /**
     * Get the next byte
     *
     * @return the next byte
     */
    int nextByte() {
        return buf[pos++];
    }

    /**
     * Decodes a UTF8 encoded string from the current position.
     *
     * @return  the decoded string
     */
    String readUtf8() {
        int length = readUnsignedShort();
        char[] chars = new char[length];
        for (int i = 0; i != chars.length; ++i) {
            chars[i] = readChar();
        }
        return new String(chars);
    }

}
