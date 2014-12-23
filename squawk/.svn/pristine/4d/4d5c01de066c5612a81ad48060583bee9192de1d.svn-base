/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.security;

/**
 * Encode / Decode hexadecimal strings to / from byte arrays
 *
 */
public class HexEncoding 
{
    /** The class is never to be instantiated. All methods and fields are static. */
    private HexEncoding() 
    {
    }
    
    /** Hexadecimal character digits (0-f). */
    private final static char[] hc = 
    {
	'0', '1', '2', '3', '4', '5', '6', '7', 
	'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    
     /**
     * Create a string containing the all of the data in the byte array <code>b</code>, encoded as hexadecimal characters.
     *
     * @param b the data to encode
     * @return the new string
     */
    public static String hexEncode(byte[] b)
    {
        return HexEncoding.hexEncode(b,b.length);
    }
    
    /**
     * Create a string containing the data in the byte array <code>b</code>, encoded as hexadecimal characters.
     *
     * @param b the data to encode
     * @param len encode bytes 0 to len-1
     * @return the new string
     */
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
    
    /**
     * Decode a string containing hexadecimal characters into a byte array.
     *
     * @param str the string containing hexadecimal characters 
     * @return a new array containing the decoded data.
     */
    public static byte[] hexDecode(String str) 
    {
        int len = (str.length() + 1) / 2;
        byte[] res = new byte[len];
        hexDecode(str, res, 0, len);
        return res;
    }
    
    /**
     * Decodes the given hexadecimal string to a byte array. Returns number of bytes
     * actually used.
     *
     * @param str  the string containing hexadecimal characters 
     * @param b the byte array to decode into
     * @param ofs the offset into <code>b</code> to begin decoding. 
     * @param len
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
}
