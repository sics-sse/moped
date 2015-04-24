/*
 * Copyright 1999-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.cldc.i18n.j2me;

import java.io.*;
import com.sun.cldc.i18n.*;

/**
 * Default class for writing output streams.
 *
 * @version 1.0 10/18/99
 */
public class ISO8859_1_Writer extends StreamWriter {

    /**
     * If set to zero, disable buffering.
     */
    final private static int BUFFERSIZE = 32;

    /**
     * Buffer to speed up things.
     */
    private final byte[] buf;

    public ISO8859_1_Writer() {
        if (BUFFERSIZE > 0) {
             buf = new byte[BUFFERSIZE];
        }
    }

    /**
     * Write a single character.
     *
     * @exception  IOException  If an I/O error occurs
     */
    synchronized public void write(int c) throws IOException {
        //if(c > 255) {
        //    c = '?';                // was ---->    throw new RuntimeException("Unknown character "+c);
        //}
        out.write(c);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  cbuf  Buffer of characters to be written
     * @param  off   Offset from which to start reading characters
     * @param  len   Number of characters to be written
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            if (BUFFERSIZE == 0) {
        while(len-- > 0) {
            write(cbuf[off++]);
        }
            } else {
        while(len > 0) {
            int i = 0;
            while(len > 0 && i < BUFFERSIZE) {
                buf[i++] = (byte)cbuf[off++];
                len--;
            }
            out.write(buf, 0, i);
        }
    }
        }
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  String to be written
     * @param  off  Offset from which to start reading characters
     * @param  len  Number of characters to be written
     *
     * @exception  IOException  If an I/O error occurs
     */
    public void write(String str, int off, int len) throws IOException {
        synchronized (lock) {
            if (BUFFERSIZE == 0) {
        for (int i = 0 ; i < len ; i++) {
            write(str.charAt(off + i));
        }
            } else {
        while(len > 0) {
            int i = 0;
            while(len > 0 && i < BUFFERSIZE) {
                buf[i++] = (byte)str.charAt(off++);
                len--;
            }
            out.write(buf, 0, i);
        }
    }
        }
    }

    /**
     * Get the size in bytes of an array of chars
     */
    public int sizeOf(char[] array, int offset, int length) {
        return length;
    }

}
