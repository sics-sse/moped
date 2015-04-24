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

package com.sun.squawk.platform;

import com.sun.squawk.VM;
import java.io.IOException;

/**
 * Base class of common implementation of GCFFile
 * @author dw29446
 */
public abstract class BaseGCFFile implements GCFFile {
    public static final int INVALID_HANDLE = -1;

    /*
     * Placeholder for the file handle from the native layer.
     * Value of <code>INVALID_HANDLE</code> indicates that file is either closed or
     * does not exists
     */
    /** Read handle. */
    protected int readHandle  = INVALID_HANDLE;
    /** Write handle. */
    protected int writeHandle = INVALID_HANDLE;

    /** File name. */
    protected String nativeFileName;
    
    /**
     * Connect file handler to the abstract file target. This operation should
     * not trigger any access to the native filesystem.
     *
     * @param rootName The name of the root directory.
     *
     * @param fileName Full path to the file to be handled by this handler.
     *
     * @throws IllegalArgumentException if filename contains characters
     * not allowed by the file system. This check should not involve
     * any actual access to the filesystem.
     */
    public void connect(String rootName, String fileName) {
        if (containsIllegalFilenameChars(rootName)) {
            throw new IllegalArgumentException(rootName);
        }
        if (containsIllegalFilenameChars(fileName)) {
            throw new IllegalArgumentException(fileName);
        }
        StringBuffer name = new StringBuffer(fileName);
        pathToNativeSeparator(name, 0, name.length());
        nativeFileName = name.toString();
    }

    /**
     * Gets the system-dependent file separator character.
     * @return The file separator character.
     */
    public static char getFileSeparator() {
        return VM.getFileSeparatorChar();
    }

    boolean containsIllegalFilenameChars(String filename) {
        String illegalChars = illegalFileNameChars();
        for (int i = 0; i < illegalChars.length(); i++) {
            char c = illegalChars.charAt(i);
            if (filename.indexOf(c) != -1) {
//VM.print("Illegal filename character in: ");
//VM.print(filename);
//VM.println();
//VM.print("char: ");
//VM.print(c);
//VM.println();
                return true;
            }
        }
        return false;
    }

    /**
     * Replace all entries of the "//" with "/" (multiple separators
     * with single separator) and all "/" with the native separator.
     *
     * @param name StringBuffer to process
     * @param off offset from where to start the conversion
     * @param len length to convert
     *
     * @return the same StringBuffer after the process
     */
    private StringBuffer pathToNativeSeparator(StringBuffer name, int off, int len) {
        int length = off + len;
        int curr = off;
        char sep = getFileSeparator();
        while ((curr + 1) < length) {
            if (name.charAt(curr) == '/' && name.charAt(curr + 1) == '/') {
                name.deleteCharAt(curr);
                length--;
                continue;
            } else if (name.charAt(curr) == '/') {
                name.setCharAt(curr, sep);
            }
            curr++;
        }
        // trim trailing slash if it exists
        if (length != 1 && name.charAt(length - 1) == '/') {
            name.deleteCharAt(length - 1);
        }
        return name;
    }

    /**
     * Close file associated with this handler. Open file and all system
     * resources should be released by this call. Handler object can be
     * reused by subsequent call to connect().
     *
     * @throws IOException if I/O error occurs
     */
    public void close() throws IOException {
        closeForReadWrite();
    }

    /**
     * Closes the file for both reading and writing.
     * @throws IOException if any error occurs during input/output operations.
     */
    public void closeForReadWrite()
            throws IOException {
        closeForRead();
        closeForWrite();
    }
}
