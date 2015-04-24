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

package com.sun.spot.security.implementation;

/**
 * This class defines some static utility methods.
 * <p>
 * <b>Warning</b>: This class is not part of the Security and Trust Services
 * API (SATSA) 1.0 specification.
 * <p>
 */
public class Util {
    // These are here only to support toString() methods in SecretKey
    // and RSAKey
    /** Hexadecimal character digits (0-f). */
    private static char[] hc = {
	'0', '1', '2', '3', '4', '5', '6', '7', 
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    
   
    /**
     * Creates a copy of byte array.
     * @param array the byte array to be copied
     * @param offset where to begin
     * @param len how many bytes to copy
     * @return the copy of the array
     */
    public static byte[] cloneSubarray(byte[] array, int offset, int len) {
        byte[] data = new byte[len];
        System.arraycopy(array, 0, data, offset, len);
        return data;
    }

    /**
     * Creates a copy of byte array.
     * @param array the byte array to be copied
     * @return the copy of the array
     */
    public static byte[] cloneArray(byte[] array) {
        byte[] data = new byte[array.length];
        System.arraycopy(array, 0, data, 0, array.length);
        return data;
    }


    /**
     * Performs an XOR operation of arrays elements. Results are placed
     * into the first array.
     * @param a the first array
     * @param b the second array
     */
    public static void xorArrays(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] ^= b[i];
        }
    }

    /**
     * Unpacks data from string representation.
     * @param src packed data
     * @return unpacked data
     */
    public static byte[] unpackBytes(String src) {

        byte[] data = src.getBytes();
        int srcLen, len;

        byte[] res = new byte[len =
                  ((srcLen = data.length) >> 3) * 7 + (srcLen & 7) - 1];

        int i = 0;
        for (int k = len; k < srcLen; k++) {
            int mask = data[k];
            for (int j = 0; j < 7 && i < len; j++, i++) {
                res[i] = ((mask & (1 << j)) == 0) ? data[i] :
                              (byte) (data[i] | 0x80);
            }
        }
        return res;
    }

    /**
     * Returns 4 bytes from the buffer as integer value.
     * @param data data array
     * @param offset value offset
     * @return the value
     */
    public static int getInt(byte[] data, int offset) {
        int res = 0;
        for (int i = 0; i < 4; i++) {
            res = (res << 8) | (data[offset + i] & 0xff);
        }
        return res;
    }

    /**
     * Verifies that method parameters are correct.
     * NOTE: This method accepts too big outputOffset values - cipher
     * methods must throw ShortBufferException if there are output data.
     * @param input the input buffer
     * @param inputOffset the offset in <code>input</code> where the input
     * starts
     * @param inputLen the input length
     * @param output the buffer for the result
     * @param outputOffset the offset in <code>output</code> where the result
     * is stored
     * @throws IllegalArgumentException if parameters are wrong.
     */
    public static void checkBounds(byte[] input, int inputOffset, int inputLen,
                             byte[] output, int outputOffset) {

        if (inputLen != 0 && (input == null || inputOffset < 0 ||
                inputLen < 0 || inputOffset + inputLen > input.length)) {
            throw new IllegalArgumentException("input out of bounds");
        }

        if (output == null || outputOffset < 0) {
            throw new IllegalArgumentException("output out of bounds");
        }
    }

    private static int fromHex(char ch) 
    {
        if ((ch >= '0') && (ch <= '9')) 
        {
            return (int)(ch - '0');
        } 
        else if ((ch >= 'a') && (ch <= 'f')) 
        {
            return (int)(ch - 'a') + 10;
        } 
        else if ((ch >= 'A') && (ch <= 'F')) 
        {
            return (int)(ch - 'A') + 10;
        } 
        else 
        {
            return 0;
        }
    }

    public static byte[] hexDecode(String str) 
    {
        int len = (str.length() + 1) / 2;
        byte[] res = new byte[len];
        hexDecode(str, res, 0, len);
        return res;
    }

    /**
     * Decodes the given hex-string to a byte-array. Returns number of bytes
     * actually used.
     */
    private static int hexDecode(String str, byte[] b, int ofs, int len) 
    {
        int strLen = str.length();
        if ((strLen % 2) == 1) 
        {
            str = "0" + str;
            strLen++;
        }
        if ((len * 2) > strLen) 
        {
            len = strLen / 2;
        }
        for (int i = 0; i < strLen; i += 2) 
        {
            b[ofs++] = (byte)((fromHex(str.charAt(i)) << 4) + fromHex(str.charAt(i + 1)));
        }
        return len;
    }

    /**
     * Converts a byte array into a corresponding string of hexadecimal
     * digits. This is equivalent to hexEncode(b, 0, b.length).
     * 
     * @param b byte array to be converted
     * @return corresponding hexadecimal string
     */ 
    public static String hexEncode(byte[] b)
    {
        return hexEncode(b,b.length);
    }

    public static String hexEncode(byte[] b, int len) 
    {
        if (b == null)
        {
            return ("");
        }
        else 
        {
            char[] r = new char[len << 1];
            int v;
            for (int i = 0, j = 0; i < len; i++) 
            {
        	v = b[i] & 0xff;
        	r[j++] = hc[v >>> 4];
        	r[j++] = hc[v & 0x0f];
            }
            return (new String(r));
        }
    }

}


