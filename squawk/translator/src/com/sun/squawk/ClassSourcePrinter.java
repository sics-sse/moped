/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk;

import com.sun.squawk.util.LineReader;
import java.util.Vector;
import java.io.*;
import javax.microedition.io.*;
import com.sun.squawk.io.connections.ClasspathConnection;

/**
 * This class enables printing lines of source code for a class in any order.
 * Used to Display mixed source and bytecode disassembly.
 *
 */
public class ClassSourcePrinter {

    private Vector lines;

    /**
     * Creates a new instance of ClassSourcePrinter
     *
     * Attempts to find a source file for the given class by searching the source path
     *
     * @param klass the klass to display
     * @param the sourcepath to search
     * @throws IOException if no source file is found.
     */
    public ClassSourcePrinter(Klass klass, String sourcePath) throws IOException {
        String filename = klass.getSourceFilePath();
        if (filename == null) {
            throw new IOException(klass + " has no record of it's source file name.");
        }

        // it turns out that ClasspathConnection works for any kind of file, not just .class Even works on jar files of source?
        ClasspathConnection sourcepath = (ClasspathConnection)Connector.open("classpath://" + sourcePath);
        InputStream is = sourcepath.openInputStream(filename);
        InputStreamReader isr = new InputStreamReader(is);
        LineReader lr = new LineReader(isr);
        lines = lr.readLines(null);
        is.close();
        sourcepath.close();
    }

    /**
     * Return a string for the given (zero-base) line number).
     *
     * @param lineNo the zero-based line number to retreive.
     */
    public String getLine(int lineNo) {
        return (String)lines.elementAt(lineNo);
    }

    /**
     * Return the number of lines for this source file.
     */
    public int numLines() {
        return lines.size();
    }

    /**
     * Tests for ClassSourcePrinter.
     */
    public static void main(String[] args) {
        String className = args[0];
        String sourcepath = args[1];
        int lineNo = -1;

        if (args.length > 2) {
            lineNo = Integer.parseInt(args[2]);
        }

        try {
            System.err.println("Looking for klass " + className);
            Klass klass = Klass.getClass(className, false);

            System.err.println("Found " + klass + " searching for source in " + sourcepath);
            ClassSourcePrinter csp = new ClassSourcePrinter(klass, sourcepath);
            if (lineNo == -1) {
                for (int i = 0; i < csp.numLines(); i++) {
                    System.out.print("Line " + i + ": ");
                    System.out.println(csp.getLine(i));
                }
            } else {
                System.out.print("Line " + lineNo + ": ");
                System.out.println(csp.getLine(lineNo));
            }
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }

    }

}
