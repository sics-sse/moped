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

/*
 * Utility class to print a byte array in the form commonly used by
 * network monitoring applications.
 *
 * Author: Vipul Gupta
 */
package com.sun.midp.ssl;

public class PrettyPrint {
    /* Each line in the output has the offset, as a hex value, in the 
     * first four columns, followed by " - " (3 columns), followed by
     * the hex values (BYTES_IN_ROW*3 columns), followed by "  " 
     * (2 columns), followed by the printable ASCII values.
     */
    private static int BYTES_IN_ROW = 16;
    private static int OFFSET_SIZE = 4;
    private static int SEPARATOR1_SIZE = 3;
    private static int SEPARATOR2_SIZE = 2;  
    private static int ASCII_START = OFFSET_SIZE + SEPARATOR1_SIZE +
      (BYTES_IN_ROW * 3) + SEPARATOR2_SIZE;
    private static char PRINTABLE_LOW = ' ';  // 0x20
    private static char PRINTABLE_HIGH = '~'; // 0x7e

    /* Hexadecimal digits. */
    private static char[] hc = {
        '0', '1', '2', '3', '4', '5', '6', '7', 
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    
    /**
     * Converts a byte array to a pretty printed string of the kind 
     * shown below
     * <pre>
     * 0000 - 16 03 00 00 38 ee ba df-fa fa 64 0c 45 5e 11 e3   ....8.....d.E^..
     * 0010 - 5a 0f 11 33 48 23 d8 02-ad 17 9b 45 03 dd f6 7d   Z..3H#.....E...}
     * 0020 - 88 91 d4 2c e1 2e 78 da-5a 6f 2c 39 98 0e 38 d5   ...,..x.Zo,9..8.
     * 0030 - bb 29                                             .)              
     * </pre>
     *
     * @param b byte array containing the bytes to be converted
     * @return a pretty printed string of corresponding hexadecimal 
     * and printable ASCII values.
     */ 
    public static String prettyPrint(byte[] b) {
        return prettyPrint(b, 0, b.length);
    }

    /**
     * Converts a subsequence of bytes in a byte array into a 
     * pretty printed string of the kind shown below
     * <pre>
     * 0000 - 16 03 00 00 38 ee ba df-fa fa 64 0c 45 5e 11 e3   ....8.....d.E^..
     * 0010 - 5a 0f 11 33 48 23 d8 02-ad 17 9b 45 03 dd f6 7d   Z..3H#.....E...}
     * 0020 - 88 91 d4 2c e1 2e 78 da-5a 6f 2c 39 98 0e 38 d5   ...,..x.Zo,9..8.
     * 0030 - bb 29                                             .)              
     * </pre>
     *
     * @param b byte array containing the bytes to be converted
     * @param off starting offset of the byte subsequence inside b
     * @param len number of bytes to be converted
     * @return a pretty printed string of corresponding hexadecimal 
     * and printable ASCII values.
     */ 
    public static String prettyPrint(byte[] b, int off, int len) {
        String out = "";
        int rows = len / BYTES_IN_ROW;

        // Deal with each complete group of BYTES_IN_ROW bytes
        for (int i = 0; i < rows; i++) {
            out += new String(prettyPrintToCharArray(b, 
                off + i * BYTES_IN_ROW, BYTES_IN_ROW)) + "\n";
        }
        
        // Deal with the last incomplete group, if any
        if ((len % BYTES_IN_ROW) != 0) {
            out += new String(prettyPrintToCharArray(b, 
                off + rows * BYTES_IN_ROW, (len % BYTES_IN_ROW))) + "\n";
        }
        return out;
    }

    /*
     * Prints at most BYTES_IN_ROW bytes at a time, i.e. 0 < len <= BYTES_IN_ROW
     * and offset must be a multiple of BYTES_IN_ROW
     */
    private static char[] prettyPrintToCharArray(byte[] b, int off, int len) {
        char[] r = new char[ASCII_START + BYTES_IN_ROW];
        int byteVal = 0;
        int j = 0;

        // Initialize with spaces
        for (int i = 0; i < r.length; i++) r[i] = ' ';

        // Print the starting offset in hex (4 columns)
        for (int i = 1; i <= OFFSET_SIZE; i++) {
            r[j++] = hc[(off >> 4*(OFFSET_SIZE - i)) & 0x0f];
        }

        // separator (3 columns)
        r[j++] = ' ';
        r[j++] = '-';
        r[j++] = ' ';

        // Print the hex values and printable ASCII characters
        for (int i = 0; i < len; i++) {
            byteVal = b[off + i] & 0xff;
            // hex values (BYTES_IN_ROW*3 = 48 columns)
            r[j++] = hc[byteVal >> 4];
            r[j++] = hc[byteVal & 0x0f];
            // print something other than ' ' at halfway mark for readability
            if (i == ((BYTES_IN_ROW + 1) / 2) - 1) {
                r[j++] = '-';
            } else {
                r[j++] = ' ';
            }

            // ASCII values ...
            if ((byteVal < (byte) PRINTABLE_LOW) ||
              (byteVal > (byte) PRINTABLE_HIGH)) {
                r[ASCII_START + i] = '.';
            } else {
                r[ASCII_START + i] = (char) (byteVal);
            }
        }

        return r;
    }

    public static void main(String[] args) {
        byte[] test = {
            0x16, 0x03, 0x00, 0x00, 0x38, (byte) 0xee, (byte) 0xba, (byte) 0xdf,
            (byte) 0xfa, (byte) 0xfa, 0x64, (byte) 0x0c, 0x45, 0x5e, 0x11, (byte) 0xe3,
            0x5a, 0x0f, 0x11, 0x33, 0x48, 0x23, (byte) 0xd8, 0x02,
            (byte) 0xad, 0x17, (byte) 0x9b, 0x45, 0x03, (byte) 0xdd, (byte) 0xf6, 0x7d,
            (byte) 0x88, (byte) 0x91, (byte) 0xd4, 0x2c, (byte) 0xe1, 0x2e, 0x78, (byte) 0xda,
            0x5a, 0x6f, 0x2c, 0x39, (byte) 0x7e, 0x0e, 0x38, (byte) 0xd5,
            (byte) 0x7f, 0x29, (byte) 0x9d, 0x11, (byte) 0x94, (byte) 0x8a, 0x46, 0x48,
            (byte) 0xe3, 0x39, 0x56, 0x77, (byte) 0xa4
        };

        System.out.println(prettyPrint(test));
    }
}
