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

package com.sun.squawk.builder.util;

import java.io.*;

/**
 * An LineReader instance is used to read some character input, line by line.
 */
public class LineReader {

    /**
     * The line number of the last successful call to {@link #readLine}.
     */
    private int lastLineNo;

    /**
     * The underlying buffered reader.
     */
    private final BufferedReader reader;

    /**
     * The description of the source from which the BufferedReader was constructed.
     */
    private final String source;

    /**
     * Creates a LineReader to read from a given Reader, line by line.
     *
     * @param reader   the reader to read from
     * @param source   a description of <code>reader</code>'s source
     * @param size     the size to use when creating the internal BufferedReader (-1 for default)
     */
    public LineReader(Reader reader, String source, int size) {
        this.source = source;
        this.reader = size == -1 ? new BufferedReader(reader) : new BufferedReader(reader, size);
    }

    /**
     * Reads a line from the input.
     *
     * @return  the line read or null if EOF is reached
     */
    public String readLine() {
        try {
            String line = reader.readLine();
            if (line != null) {
                ++lastLineNo;
                if ((lastLineNo % 10000) == 0) {
                    System.err.println("read " + lastLineNo + " lines...");
                }
            }
            return line;
        } catch (IOException ex) {
            throw error(ex.toString(), ex);
        }
    }

    /**
     * Gets a description of this reader's source (e.g. a file path).
     *
     * @return String a description of this reader's source
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets the line number of the last line {@link #read}.
     *
     * @return the last line number
     */
    public int getLastLineNumber() {
        return lastLineNo;
    }

    /**
     * Returns a textual description of this line reader's input source and last read line number.
     *
     * @return the context message
     */
    public String getContext() {
        return "at line " + lastLineNo + " in " + source;
    }

    /**
     * Creates a runtime exception detailing an error while reading from this file.
     *
     * @param msg  description of the error
     * @return  a runtime exception detailing the error
     */
    public RuntimeException error(String msg, Throwable cause) {
        msg = "Error " + getContext() + ": " + msg;
        if (cause != null) {
            return new RuntimeException(msg, cause);
        } else {
            return new RuntimeException(msg);
        }
    }

}
