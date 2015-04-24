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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.squawk.VM;
import com.sun.squawk.util.Arrays;

/**
 * Collection of utility functions
 */
public class Utils {
    public static int SIZE_OF_SHORT = 2;
    public static int SIZE_OF_INT = 4;
    public static int SIZE_OF_LONG = 8;
    
    public static final boolean diagnostics = false;
    
    /**
     * Display a log message if the "spot.diagnostics" property is set
     * @param message the message to display
     */
    public static void log(String message) {
        if (diagnostics) {
            if (message.length() == 0 || message.charAt(0) != '[') {
                System.err.print("[SpotLib] ");
            }
            System.err.println(message);
        }
    }

    /**
     * Make a copy of a byte array
     * @param aByteArray the array to be copied
     * @return the copy
     */
    public static byte[] copy(byte[] aByteArray) {
        return Arrays.copy(aByteArray, 0, aByteArray.length, 0, aByteArray.length);
    }
    
    /**
     * Parse a decimal or hexadecimal string to get an int.
     * The number may start with 0x or x as the hex indicator.
     * @param number the string to be parsed
     * @return the parsed value
     */
    public static int readNumber(String number) {
        return (int)(readLong(number) & 0xFFFFFFFFL);
    }

    /**
     * Parse a decimal or hexadecimal string to get a long.
     * The number may start with 0x or x as the hex indicator.
     * @param number the string to be parsed
     * @return the parsed value
     */
    public static long readLong(String number) {
        int radix = 10;
        if (number.startsWith("0x")) {
            number = number.substring(2);
            radix = 16;
        }
        if (number.startsWith("x")) {
            number = number.substring(1);
            radix = 16;
        }
        long answer  = Long.parseLong(number, radix);
        return answer;
    }
    
    /**
     * Read a little-endian long from an array
     * @param byteArray the array containing the number as 8 consecutive bytes
     * @param offset the offset into the array of the number
     * @return the number
     */
    public static long readLittleEndLong(byte[] byteArray, int offset) {
        return readLittleEndNumber(byteArray, offset, SIZE_OF_LONG);
    }
    
    /**
     * Read a little-endian int from an array
     * @param byteArray the array containing the number as 4 consecutive bytes
     * @param offset the offset into the array of the number
     * @return the number
     */
    public static int readLittleEndInt(byte[] byteArray, int offset) {
        return (int)readLittleEndNumber(byteArray, offset, SIZE_OF_INT);
    }

    /**
     * Read a big-endian int from an array
     * @param byteArray the array containing the number as 4 consecutive bytes
     * @param offset the offset into the array of the number
     * @return the number
     */
    public static int readBigEndInt(byte[] byteArray, int offset) {
        return (int)readBigEndNumber(byteArray, offset, SIZE_OF_INT);
    }

    /**
     * Read a little-endian short from an array
     * @param byteArray the array containing the number as 2 consecutive bytes
     * @param offset the offset into the array of the number
     * @return the number
     */
    public static int readLittleEndShort(byte[] byteArray, int offset) {
        return (int)readLittleEndNumber(byteArray, offset, SIZE_OF_SHORT);
    }

    /**
     * Read a big-endian short from an array
     * @param byteArray the array containing the number as 2 consecutive bytes
     * @param offset the offset into the array of the number
     * @return the number
     */
    public static int readBigEndShort(byte[] byteArray, int offset) {
        return (int)readBigEndNumber(byteArray, offset, SIZE_OF_SHORT);
    }

    /**
     * Read a big-endian long from an array
     * @param byteArray the array containing the number as 8 consecutive bytes
     * @param offset the offset into the array of the number
     * @return the number
     */
    public static long readBigEndLong(byte[] byteArray, int offset) {
        return readBigEndNumber(byteArray, offset, SIZE_OF_LONG);
    }

    /**
     * Write a big-endian long into an array
     * @param byteArray the array to contain the number
     * @param offset the offset into the array where the number is to be placed
     * @param value the number
     */
    public static void writeBigEndLong(byte[] byteArray, int offset, long value) {
        writeBigEndNumber(byteArray, offset, SIZE_OF_LONG, value);
    }
    
    /**
     * Write a little-endian long into an array
     * @param byteArray the array to contain the number
     * @param offset the offset into the array where the number is to be placed
     * @param value the number
     */
    public static void writeLittleEndLong(byte[] byteArray, int offset, long value) {
        writeLittleEndNumber(byteArray, offset, SIZE_OF_LONG, value);
    }

    /**
     * Write a big-endian int into an array
     * @param byteArray the array to contain the number
     * @param offset the offset into the array where the number is to be placed
     * @param value the number
     */
    public static void writeBigEndInt(byte[] byteArray, int offset, int value) {
        writeBigEndNumber(byteArray, offset, SIZE_OF_INT, value);
    }
    
    /**
     * Write a little-endian int into an array
     * @param byteArray the array to contain the number
     * @param offset the offset into the array where the number is to be placed
     * @param value the number
     */
    public static void writeLittleEndInt(byte[] byteArray, int offset, int value) {
        writeLittleEndNumber(byteArray, offset, SIZE_OF_INT, value);
    }
    
    /**
     * Write a little-endian short into an array
     * @param byteArray the array to contain the number
     * @param offset the offset into the array where the number is to be placed
     * @param value the number
     */
    public static void writeLittleEndShort(byte[] byteArray, int offset, int value) {
        writeLittleEndNumber(byteArray, offset, SIZE_OF_SHORT, value);
    }
    
    /**
     * Write a big-endian short into an array
     * @param byteArray the array to contain the number
     * @param offset the offset into the array where the number is to be placed
     * @param value the number
     */
    public static void writeBigEndShort(byte[] byteArray, int offset, int value) {
        writeBigEndNumber(byteArray, offset, SIZE_OF_SHORT, value);
    }
        
    private static long readLittleEndNumber(byte[] byteArray, int offset, int numberOfBytes) {
        long result = 0;
        for (int i=numberOfBytes-1; i>=0; i--) {
            result = (result << 8) | (byteArray[offset+i] & 0xFF);
        }
        return result;
    }

    private static long readBigEndNumber(byte[] byteArray, int offset, int numberOfBytes) {
        long result = 0;
        for (int i=0; i<numberOfBytes; i++) {
            result = (result << 8) | (byteArray[offset+i] & 0xFF);
        }
        return result;
    }

    private static void writeBigEndNumber(byte[] byteArray, int offset, int numberOfBytes, long value) {
        for (int i=numberOfBytes-1; i>=0; i--) {
            byteArray[offset+i] = (byte)(value & 0xFF);
            value = value >> 8;
        }
    }

    private static void writeLittleEndNumber(byte[] byteArray, int offset, int numberOfBytes, long value) {
        for (int i=0; i<numberOfBytes; i++) {
            byteArray[offset+i] = (byte)(value & 0xFF);
            value = value >> 8;
        }
    }
    
    /**
     * Truncates an int to 6 bits
     * @param number the number to truncate
     * @return the truncated value
     */
    public static int as6BitNumber(int number){
        return number & 0x3F;
    }
    
    /**
     * Do a Thread.sleep(...), catching and ignoring any InterruptedException
     * @param milliseconds the period to sleep
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Get the value of a property from this suite's manifest (or the manifest of a parent suite).
     * @param manifestPropertyName the name of the property
     * @param valueIfNotInManifest the value to return if not found
     * @return the value
     */
    public static String getManifestProperty(String manifestPropertyName, String valueIfNotInManifest) {
        String value = VM.getManifestProperty(manifestPropertyName);
        if (value != null) {
            return value;
        } else {
            return valueIfNotInManifest;
        }
    }
    
    /**
     * Get the value of a property from this suite's manifest (or the manifest of a parent suite).
     * @param manifestPropertyName the name of the property
     * @param valueIfNotInManifest the value to return if not found
     * @return the value
     */
    public static int getManifestProperty(String manifestPropertyName, int valueIfNotInManifest) {
        String value = VM.getManifestProperty(manifestPropertyName);
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return valueIfNotInManifest;
        }
    }
    
    /**
     * Get the value of a system property.
     * @param propertyName the name of the property
     * @param valueIfNotDefined the value to return if not found
     * @return the value
     */
    public static int getSystemProperty(String propertyName, int valueIfNotDefined) {
        String property = System.getProperty(propertyName);
        if(property==null) {
            return valueIfNotDefined;
        } else {
            return Integer.parseInt(property);
        }
    }

        /**
         * Get the value of a system property.
         * @param propertyName the name of the property
         * @param valueIfNotDefined the value to return if not found
         * @return the value
         */
        public static String getSystemProperty(String propertyName, String valueIfNotDefined) {
            String property = System.getProperty(propertyName);
            if(property==null) {
                return valueIfNotDefined;
            } else {
                return property;
            }
        }

    /**
     * Convert an Enumeration to a Vector
     * @param items the Enumeration to convert
     * @return the Vector
     */
    public static Vector enumToVector(Enumeration items) {
        Vector result = new Vector();
        while (items.hasMoreElements()) {
            result.addElement(items.nextElement());
        }
        return result;
    }
    
    /**
     * Generate a string representation of a byte array.
     * @param b the array
     * @return the string representation
     */
    public static String stringify(byte[] b) {
        StringBuffer result = new StringBuffer(b.length*6);
        for (int i=0; i<b.length; i++) {
            result.append("[");
            String hexString = Integer.toHexString(b[i] & 0xFF);
            if (hexString.length() == 1) {
                result.append('0');
            }
            result.append(hexString);
            result.append("]");
        }
        return result.toString();
    }
    
    /**
     * Generate a String that is a copy of the supplied String but with all spaces replaced by nulls.
     * @param string the input String
     * @return the output String
     */
    public static String withSpacesReplacedByZeros(String string) {
        StringBuffer result = new StringBuffer();
        boolean previousCharWasSpace = true;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if(c == ' '){
                if(previousCharWasSpace){
                    continue;
                }
                previousCharWasSpace = true;
            }else{
                previousCharWasSpace = false;
            }
            result.append(c);
        }
        return result.toString().replace(' ',(char)0);
    }

    /**
     * Test whether a boolean option is set in the system properties
     * @param optionName
     * @param defaultIfAbsent the value to return if optionName is absent from system properties
     * @return true if optionName exists and has the value true, or optionName absent and defaultIfAbsent
     */
    public static boolean isOptionSelected(String optionName, boolean defaultIfAbsent) {
        String option = System.getProperty(optionName);
        return option == null ? defaultIfAbsent : "true".equals(option.toLowerCase());
    }

    /**
     * Split a string into parts.
     * Utils.split("foo:bar", ':') returns String[] {"foo", "bar"}
     * @param s the string to split
     * @param marker the character that marks the separation between parts
     * @return the parts
     */
    public static String[] split(String s, char marker) {
        int partCount = 1;
        int startIndex;
        for (startIndex=0; startIndex<s.length(); startIndex++) {
            if (s.charAt(startIndex) == marker) {
                partCount++;
            }
        }
        String[] result = new String[partCount];
        startIndex = 0;
        for (int i = 0; i < partCount; i++) {
            int endIndex = s.indexOf(marker, startIndex);
            if (endIndex == -1) {
                endIndex = s.length();
            }
            result[i] = s.substring(startIndex, endIndex);
            startIndex = endIndex+1;
        }
        return result;
    }
    
    /**
     * Read data from a stream, checking the CRC.
     * The stream must contain, in order: a big-endian int that is the number of data bytes to read,
     * the data, a 16-bit CRC.
     * @param dataInputStream the stream holding the data
     * @return the checked data
     * @throws IOException
     * @throws SpotFatalException if the length is improbable or the CRC is incorrect
     * @deprecated
     */
    public static byte[] getDataWithCRC(DataInputStream dataInputStream) throws IOException {
        int length = dataInputStream.readInt();
        if(length < 0){
            length = 0;
        }
        if (length > (1024 * 1024) || length < 0) {
            throw new IOException("Attempt to read unlikely checked byte array size: " + length);
        }

        byte[] data = new byte[length];
        dataInputStream.readFully(data);
        int checksum = dataInputStream.readShort() & 0xFFFF;
        int crc = CRC.crc(data,0,length) & 0xFFFF;
        if (crc != checksum) {
            throw new IOException("Checksum for received checked byte array is incorrect");
        }
        return data;
    }
    
    /**
     * Put data onto a stream, adding a CRC. The length is written first, as a big-endian int, then the
     * data, and finally the 16-bit CRC.
     * @param dataOutputStream the stream to receive the data
     * @param data the array containing the data
     * @param offset the offset into the array at which the data starts
     * @param length the number of bytes of data
     * @throws IOException
     * @deprecated
     */
    public static void putDataWithCRC(DataOutputStream dataOutputStream, byte[] data, int offset, int length) throws IOException {
        dataOutputStream.writeInt(length);
        if (length > 0) {
            dataOutputStream.write(data, offset, length);
        }
        dataOutputStream.writeShort(CRC.crc(data, offset, length));
    }
}
