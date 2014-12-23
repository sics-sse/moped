/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.util;

import java.util.Enumeration;
import java.util.Vector;

import com.sun.squawk.*;

/**
 * A collection of utilities for command line argument parsing.
 */
public class ArgsUtilities {
    
    /**
     * Purely static class should not be instantiated.
     */
    private ArgsUtilities() {}

    /**
     * Processes a file containing command line arguments. The file is parsed as a
     * sequence of white space separated arguments.
     *
     * @param   name  the name of the args file
     * @param   args  the vector of arguments to be added to
     */
    public static void readArgFile(String name, Vector args) {
        Vector lines = new Vector();
        LineReader.readLines(name, lines);
        for (Enumeration e = lines.elements(); e.hasMoreElements();) {
            String line = (String) e.nextElement();
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
                args.addElement(st.nextToken());
            }
        }
    }

    /**
     * Expands any argfiles. Finds any components in <code>args</code> that
     * start with '@' (thus denoting a file containing more arguments) and
     * expands the arguments inline in <code>args</code>. The expansion is
     * not recursive.
     *
     * @param   args  the original command line arguments
     * @return  the given arguments with any inline argfiles expanded
     */
    public static String[] expandArgFiles(String[] args) {
        Vector expanded = new Vector(args.length);
        for (int i = 0; i != args.length; ++i) {
            String arg = args[i];
            if (arg.charAt(0) == '@') {
                readArgFile(arg.substring(1), expanded);
            } else {
                expanded.addElement(arg);
            }
        }
        if (expanded.size() != args.length) {
            args = new String[expanded.size()];
            expanded.copyInto(args);
        }
        return args;
    }

    /**
     * Converts a given file or class path to the correct format for the
     * underlying platform. For example, if the underlying platform uses
     * '/' to separate directories in a file path then any instances of
     * '\' in <code>path</code> will be converted to '/'.
     *
     * @param   path         to the path to convert
     * @param   isClassPath  specifies if <code>path</code> is a class path
     * @return  the value of <code>path</code> reformatted (if necessary) to
     *                be correct for the underlying platform
     */
    public static String toPlatformPath(String path, boolean isClassPath) {
        char fileSeparatorChar = VM.getFileSeparatorChar();
        if (fileSeparatorChar == '/') {
            path = path.replace('\\', '/');
        } else if (fileSeparatorChar == '\\') {
            path = path.replace('/', '\\');
        } else {
            throw new RuntimeException("OS with unknown separator: '" + fileSeparatorChar + "'");
        }
        if (isClassPath) {
            char pathSeparatorChar = VM.getPathSeparatorChar();
            if (pathSeparatorChar == ':') {
                path = path.replace(';', ':');
            } else if (pathSeparatorChar == ';') {
                // Need special processing so as to not convert "C:\" into "C;\"
                char[] pathChars = path.toCharArray();
                int start = 0;
                for (int i = 0; i != pathChars.length; ++i) {
                    if (pathChars[i] == ';') {
                        start = i + 1;
                    }
                    if (pathChars[i] == ':') {
                        if (i - start == 1) {
                            // If there is only a single character between the start of the
                            // current path component and the next ':', we assume that this
                            // is a drive letter and so need to leave the ':' unchanged
                        } else {
                            pathChars[i] = ';';
                            start = i + 1;
                        }
                    }
                }

                path = new String(pathChars);
            } else {
                throw new RuntimeException("OS with unknown path separator: '"+ pathSeparatorChar+"'");
            }
        }
        return path;
    }
  

    /**
     * Cuts a string of white space separated tokens into an array of strings, one element for each token.
     *
     * @param str   the string to cut
     * @return 'str' as an array of strings
     */
    public static String[] cut(String str) {
        StringTokenizer st = new StringTokenizer(str, " ");
        String res[] = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            res[i++] = st.nextToken();
        }
        return res;
    }
    
}
