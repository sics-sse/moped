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

package com.sun.squawk.os;

/**
 * Static class that represents the 'C' programming environment.
 *
 */
public class CSystem {

    static boolean lowParmFirst;

    /*
     * Load the DLL in a J2SE system
     */
    static {
        if (System.getProperty("microedition.configuration") == null) {
	    /**
	     * java.library.path needs to include the path to the 
	     * CSystem library.  The following code, though it compiles, 
	     * does not update the path that the VM is already using, 
	     * therefore, you need to either have set your library 
	     * path correctly or use -Djava.library.path=<path> at 
	     * compile time on the command line.
  	     *
	     *   String sep = System.getProperty("path.separator");
	     *   System.setProperty("java.library.path", 
	     *   	"." + sep + System.getProperty("java.library.path"));  
	     */
// TODO - Only used by compiler, should be moved somewhere else so not in base
//            System.loadLibrary("CSystem");
//            lowParmFirst = _setup() == 1;
            throw new RuntimeException("Not implemented yet");
        }
    }

    /**
     * Lookup a C link symbol.
     *
     * @param s the symbole
     * @return the address
     */
    public static int lookup(String s) {
        return _lookup(s);
    }

    /**
     * Malloc some memory.
     *
     * @param size the size in bytes
     * @return the address
     */
    public static int malloc(int size) {
        return _malloc(size);
    }

    /**
     * Malloc some memory and copy a byte array into it.
     *
     * @param bytes the byte array
     * @return the address
     */
    public static int mallocBytes(byte[] bytes) {
        return copy(_malloc(bytes.length), bytes, bytes.length);
    }

    /**
     * Malloc some memory and copy a string into it.
     *
     * @param str the string
     * @return the address
     */
    public static int mallocString(String str) {
        byte[] bytes = str.getBytes();
        return copy(_malloc(bytes.length+1), bytes, bytes.length);
    }

    /**
     * Set a byte in memory.
     *
     * @param address the address
     * @param bite the value
     */
    public static void setByte(int address, int bite) {
        _setByte(address, bite);
    }

    /**
     * Get a byte in memory.
     *
     * @param address the address
     * @return the value
     */
    public static int getByte(int address) {
        return _getByte(address);
    }

    /**
     * Set a word in memory.
     *
     * @param address the address
     * @param value the value
     */
    public static void setInt(int address, int value) {
        if (lowParmFirst) { // ??????????????????????????????????? does this mean little endian?
            _setByte(address+0, (value >>  0) & 0xFF);
            _setByte(address+1, (value >>  8) & 0xFF);
            _setByte(address+2, (value >> 16) & 0xFF);
            _setByte(address+3, (value >> 24) & 0xFF);
        } else {
            _setByte(address+0, (value >> 24) & 0xFF);
            _setByte(address+1, (value >> 16) & 0xFF);
            _setByte(address+2, (value >>  8) & 0xFF);
            _setByte(address+3, (value >>  0) & 0xFF);
        }
    }

    /**
     * Get a word in memory.
     *
     * @param address the address
     * @return the value
     */
    public static int getInt(int address) {
        int ch1 = _getByte(address+0);
        int ch2 = _getByte(address+1);
        int ch3 = _getByte(address+2);
        int ch4 = _getByte(address+3);
        if (lowParmFirst) { // ??????????????????????????????????? does this mean little endian?
            return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
        } else {
            return (ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0);
        }
    }

    /**
     * Copy data from a byte array into a malloc buffer.
     *
     * @param addr the address of the malloc buffer
     * @param bytes the byte array to copy
     * @param lth the length of the buffer to copy
     * @return the address the data was copied to
     */
    public static int copy(int addr, byte[] bytes, int lth) {
        for (int i = 0 ; i < lth ; i++) {
            _setByte(addr+i, bytes[i]);
        }
        return addr;
    }

    /**
     * Free a buffer allocated with malloc.
     *
     * @param address the address of the buffer
     */
    public static void free(int address) {
        _free(address);
    }


    /**
     * Call a 'C' function.
     *
     * @param x the parameter list
     */
    public static void vcall(Parm x) {
        try {
            _icall(x);
        } finally {
            x.free();
        }
    }

    /**
     * Call a 'C' function.
     *
     * @param x the parameter list
     * @return the result
     */
    public static int icall(Parm x) {
        try {
            return _icall(x);
        } finally {
            x.free();
        }
    }

    /**
     * Call a 'C' function.
     *
     * @param x the parameter list
     * @return the result
     */
    public static long lcall(Parm x) {
        try {
            return _lcall(x);
        } finally {
            x.free();
        }
    }

/*if[FLOATS]*/

    /**
     * Call a 'C' function.
     *
     * @param x the parameter list
     * @return the result
     */
    public static float fcall(Parm x) {
        try {
            return _fcall(x);
        } finally {
            x.free();
        }
    }

    /**
     * Call a 'C' function.
     *
     * @param x the parameter list
     * @return the result
     */
    public static double dcall(Parm x) {
        try {
            return _dcall(x);
        } finally {
            x.free();
        }
    }

/*end[FLOATS]*/

    /**
     * Call a 'C' function.
     *
     * @param x the parameter list
     * @return the result
     */
    public static Object ocall(Parm x) {
        try {
            return _ocall(x);
        } finally {
            x.free();
        }
    }

    private static native long   _setup();
    private static native int    _lookup(String s);
    private static native int    _malloc(int size);
    private static native void   _setByte(int address, int bite);
    private static native int    _getByte(int address);
    private static native void   _free(int address);
    private static native int    _icall(Parm x);
    private static native long   _lcall(Parm x);
/*if[FLOATS]*/
    private static native float  _fcall(Parm x);
    private static native double _dcall(Parm x);
/*end[FLOATS]*/
    private static native Object _ocall(Parm x);
    private static native int    _pcall(Parm x);

}
