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

package com.sun.squawk.builder.gen;

import java.io.*;
import java.util.*;

/**
 * This is the base class for the programs that generate a source file.
 *
 */
public abstract class Generator {
    
    public final static String[] COPYRIGHT_LINES = {
        "/*",
        " * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.",
        " * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER",
        " * ",
        " * This code is free software; you can redistribute it and/or modify",
        " * it under the terms of the GNU General Public License version 2",
        " * only, as published by the Free Software Foundation.",
        " * ",
        " * This code is distributed in the hope that it will be useful, but",
        " * WITHOUT ANY WARRANTY; without even the implied warranty of",
        " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU",
        " * General Public License version 2 for more details (a copy is",
        " * included in the LICENSE file that accompanied this code).",
        " * ",
        " * You should have received a copy of the GNU General Public License",
        " * version 2 along with this work; if not, write to the Free Software",
        " * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA",
        " * 02110-1301 USA",
        " * ",
        " * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo",
        " * Park, CA 94025 or visit www.sun.com if you need additional",
        " * information or have any questions.",
        " */"
    };
        
    /**
     * Runs this generator to generate or update the content in a given file.
     *
     * @param dir    the base directory of the file whose content is to be generated/updated
     * @return false if <code>file</code> already existed and whose content matched
     *               that produced by this generator, true otherwise
     */
    public final boolean run(File baseDir) {
        try {
            File file = getGeneratedFile(baseDir);
            CharArrayWriter caw = new CharArrayWriter(file.exists() ? (int)file.length() : 0);
            PrintWriter out;
            // Force the line feed termination to be linefeed, since we commit generated files into CVS
            // and we would like to make sure they are generated consistently.
            // TODO: It is a hack to set this property, but it seemed like a lot of work to extend the
            // generate() API to use a stream where we can change the line termination.
            String oldLineSeparator = System.getProperty("line.separator");
            try {
                System.setProperty("line.separator", "\n");
                out = new PrintWriter(caw);
            } finally {
                System.setProperty("line.separator", oldLineSeparator);
            }

            generate(out);

            char[] content = caw.toCharArray();
            char[] oldContent = null;

            if (file.exists()) {
                FileReader fr = new FileReader(file);
                int length = (int) file.length();
                int n = 0;
                oldContent = new char[length];
                while (n < length) {
                    int count = fr.read(oldContent, n, length - n);
                    if (count < 0) {
                        throw new EOFException();
                    }
                    n += count;
                }
                fr.close();
            }

            if (!Arrays.equals(content, oldContent)) {
                file.delete();
                file.getParentFile().mkdirs();
                FileWriter fw = new FileWriter(file);
                fw.write(content);
                fw.close();
                file.setReadOnly();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Prints the standard Squawk VM copywright message.
     *
     * @param out  where to print the message
     */
    public static void printCopyright(Class c, PrintWriter out) {
        for (int i = 0; i < COPYRIGHT_LINES.length; i++) {
            out.println(COPYRIGHT_LINES[i]);
        }
        out.println("");
        out.println("/* **** GENERATED FILE -- DO NOT EDIT ****");
        out.println(" *      generated by " + c.getName());
        out.println(" */");
        out.println("");
    }

    /**
     * Pads a given string up to a given length with trailing spaces.
     * If <code>s.length() >= length</code>, then no padding is applied.
     *
     * @param s       the string to pad
     * @param length  the size to which <code>s</code> should be padded
     * @return  the padded string
     */
    static String pad(String s, int length) {
        if (s.length() < length) {
            char[] padded = new char[length];
            s.getChars(0, s.length(), padded, 0);
            Arrays.fill(padded, s.length(), padded.length, ' ');
            return new String(padded);
        } else {
            return s;
        }
    }

    /**
     * Produces the content the Generator subclass is responsible for.
     *
     * @param out  where to write the content
     */
    abstract void generate(PrintWriter out);

    /**
     * Gets the file generated/updated by this generator.
     *
     * @param baseDir  the base directory where the file will be created
     * @return  the file under <code>baseDir</code> generated/updated by this generator
     */
    public abstract File getGeneratedFile(File baseDir);

    /**
     * Test harness. Usage:
     *
     * Generator <Generator subclass> <file>
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        Generator gen = (Generator)Class.forName("com.sun.squawk.builder.gen." + args[0]).newInstance();
        File file = new File(args[1]);
        boolean replaced = gen.run(file);
        if (replaced) {
            System.out.println("Generator replaced content in " + file);
        } else {
            System.out.println("Generator did not replace content in " + file);
        }

    }
}
