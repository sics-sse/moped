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

package com.sun.squawk.util;

import java.io.*;

/**
 * This class provides one function for decoding a UTF-8 encoded string from a
 * {@link DataInput}. This provides almost the same functionality as
 * {@link DataInputStream#readUTF(DataInput)} except that it can be used
 * to decode a UTF-8 string from a Java class file which differs from
 * the "standard" UTF-8 format in that 0 is encoded in two
 * bytes as opposed to one.
 *
 */
public final class DataInputUTF8Decoder {

    private DataInputUTF8Decoder() {
    }

    /**
     * Reads a UTF-8 encoded String from <code>in</code>.
     *
     * @param in            a data input stream.
     * @param isClassFile   if true, then 0 is decoded from two bytes as opposed to one
     * @param twoByteLength if true, then the length of the encoded string is given in two bytes as opposed to 4
     * @return the decoded string
     */
    public final static String readUTF(DataInput in, boolean isClassFile, boolean twoByteLength) throws IOException {
        return readUTF(in, isClassFile, twoByteLength ? in.readUnsignedShort() : in.readInt());
    }

    /**
     * Reads a UTF-8 encoded String from <code>in</code>.
     *
     * @param in            a data input stream.
     * @param isClassFile   if true, then 0 is decoded from two bytes as opposed to one
     * @param utflen        the numbers of bytes to be decoded
     * @return the decoded string
     */
    public final static String readUTF(DataInput in, boolean isClassFile, int utflen) throws IOException {
        byte bytearr [] = new byte[utflen];
        int c, char2, char3;
        int count = 0;

        ///in.readFully(bytearr, 0, utflen);  OLD

        boolean sevenBit = true;
        for (int i = 0 ; i < utflen ; i++) {
            byte ch = in.readByte();
            bytearr[i] = ch;
            if (ch < 0 || (isClassFile && ch == 0)) {
                sevenBit = false;
            }
        }

        if (sevenBit) {
            return new String(bytearr, 0, utflen);
        }

        StringBuffer str = new StringBuffer(utflen);

        while (count < utflen) {
            c = (int) bytearr[count] & 0xff;
            if (isClassFile && c == 0) {
                throw new UTFDataFormatException();
            }
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    /* 0xxxxxxx*/
                    count++;
                    str.append((char)c);
                    break;
                case 12: case 13:
                    /* 110x xxxx   10xx xxxx*/
                    count += 2;
                    if (count > utflen) {
                        throw new UTFDataFormatException();
                    }
                    char2 = (int) bytearr[count-1];
                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException();
                    }
                    str.append((char)(((c & 0x1F) << 6) | (char2 & 0x3F)));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;
                    if (count > utflen) {
                        throw new UTFDataFormatException();
                    }
                    char2 = (int) bytearr[count-2];
                    char3 = (int) bytearr[count-1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException();
                    }
                    str.append((char)(((c     & 0x0F) << 12) |
                                      ((char2 & 0x3F) << 6)  |
                                      ((char3 & 0x3F) << 0)));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException();
                }
        }
        // The number of chars produced may be less than utflen
        return new String(str);
    }
}
