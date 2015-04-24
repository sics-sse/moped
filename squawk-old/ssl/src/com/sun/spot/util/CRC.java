/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.spot.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * Standalone class to provide 16-bit crc calculations
 */
public class CRC {
    /* Table of CRCs of all 8-bit messages. */
    private static int[] crc_table = make_crc_table();
    
    /**
     * Return the CRC of the specified part of a byte array.
     * 
     * @param buf source of bytes
     * @param offset offset in buf to first byte
     * @param len number of bytes to include in calculation
     * @return 16-bit CRC value
     */
    public static short crc(byte[] buf, int offset, int len) {
        /* Update a running CRC with the bytes buf[0..len-1]--the CRC
         * should be initialized to all 1's, and the transmitted value
         * is the 1's complement of the final running CRC (see the
         * crc() routine below)). */
        int c = 0xffff;

        for (int n = offset; n < len + offset; n++) {
            c = update_crc(buf[n], c);
        }

        return (short)(c ^ 0xffff);
    }
    
    /**
     * Return the CRC value of the specified number of bytes from an InputStream.
     * 
     * @param stream to read bytes from
     * @param flashedByteCount number of bytes to read
     * @return 16-bit CRC value
     * @throws IOException
     */
    public static short crc(InputStream stream, int flashedByteCount) throws IOException {
        /* Update a running CRC with the bytes buf[0..len-1]--the CRC
         * should be initialized to all 1's, and the transmitted value
         * is the 1's complement of the final running CRC (see the
         * crc() routine below)). */
        int c = 0xffff;

        for (int n = 0; n < flashedByteCount; n++) {
            c = update_crc((byte)stream.read(), c);
        }

        return (short)(c ^ 0xffff);
    }   

    private static int update_crc(byte b, int c) {
        return crc_table[(c ^ b) & 0xff] ^ (c >> 8);
    }
    
    /* Make the table for a fast CRC. */
    private static int[] make_crc_table() {
        int[] crc_table = new int[256];
        int c;
   
        for (int n = 0; n < 256; n++) {
            c = n;
            for (int k = 0; k < 8; k++) {
                if ((c & 1) != 0)
                    c = 0x8408 ^ (c >> 1);
                else
                    c = c >> 1;
            }
            crc_table[n] = c;
        }
        return crc_table;
    }

    
}
