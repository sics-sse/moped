/*
 * Copyright 2006-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2010-2011 Oracle. All Rights Reserved.
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

package com.sun.squawk.util;

import java.io.*;

/**
 * This class provides one function for UTF-8 encoding a string to a
 * {@link DataOutput}. This provides almost the same functionality as
 * {@link DataOutputStream#writeUTF} except that it can be used
 * to encode a UTF-8 string with a 4-byte length header as opposed to
 * the standard 2-byte length header.
 *
 */
public final class DataOutputUTF8Encoder {

    private DataOutputUTF8Encoder() {
    }

    /**
     * Writes a string to <code>out</code> in UTF-8 encoded form.
     *
     * @param str           the string to encode
     * @param out           a data output stream.
     * @param twoByteLength if true, then the length of the encoded string is to be encoded in two bytes as opposed to 4
     * @return the decoded string
     */
    public final static int writeUTF(String str, DataOutput out, boolean twoByteLength) throws IOException {

        int strlen = str.length();
        int utflen = 0;
        char[] charr = new char[strlen];
        int c, count = 0;

        str.getChars(0, strlen, charr, 0);

        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        int maxLen = twoByteLength ? 65535 : Integer.MAX_VALUE;

        if (utflen > maxLen) {
            throw new UTFDataFormatException();
        }
        byte[] bytearr = new byte[utflen + (twoByteLength ? 2 : 4)];
        if (!twoByteLength) {
            bytearr[count++] = (byte) ((utflen >>> 24) & 0xFF);
            bytearr[count++] = (byte) ((utflen >>> 16) & 0xFF);
        }
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;
            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        out.write(bytearr);
        return utflen + 2;
    }

    /**
     * Calculate the number of bytes required to encode str as a UTF-8 string.
     *
     * @param str
     * @return number of bytes need to hold the string encoded as UTF-8
     */
    public static int lengthAsUTF(String str) {
        int strlen = str.length();
        int utflen = 0;
        int c = 0;

        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }
        return utflen;
    }
}
