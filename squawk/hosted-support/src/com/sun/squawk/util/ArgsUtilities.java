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

import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.*;

import com.sun.squawk.io.connections.ClasspathConnection;
import com.sun.squawk.*;
import com.sun.squawk.pragma.HostedPragma;

/**
 * The "hosted" version of ArgsUtilities from cldc.
 * 
 * A collection of utilities for command line argument parsing.
 *
 * NOTE: This class in hosted-supported replaces the same class defined in cldc.
 */
public class ArgsUtilities {

    /**
     * Skip svn entries, etc...
     */
    public final static String[] skipList = {".svn", ".DS_Store"};

    private static boolean nameInSkipList(String name) {
        for (int i = 0; i < skipList.length; i++) {
            String skipTarget = skipList[i] + VM.getFileSeparatorChar();
            if (name.startsWith(skipTarget)) {
                return true;
            } else if (name.indexOf(VM.getFileSeparatorChar() + skipTarget) >= 0) {
                return true;
            }
        }
        return false;
    }

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
     * Processes a single command line argument that specifies a file containing a set of
     * class names, one per line.
     *
     * @param   arg      the command line argument to process
     * @param   classes  the list of class names to augment
     */
    public static void processClassListArg(String arg, Vector classes) throws HostedPragma {
        LineReader.readLines(arg, classes);
    }

    /**
     * Processes a single command line argument that specifies a jar or zip
     * file of class files.
     *
     * @param   arg      the command line argument to process
     * @param   classes  the list of class names to augment
     * @param   resources the list of resource to augment
     */
    public static void processClassJarOrZipArg(String arg, Vector classes, Vector resources) throws HostedPragma  {
        try {
            ClasspathConnection cp = (ClasspathConnection)Connector.open("classpath://" + arg);
            processClasspathConnection(cp, classes, resources);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Processes a single command line argument that specifies a jar or zip
     * file of class files.
     *
     * @param   cp      the command line argument to process
     * @param   classes  the list of class names to augment
     * @param   resources the list of resource to augment
     */
    public static void processClasspathConnection(ClasspathConnection cp, Vector classes, Vector resources) throws HostedPragma {
        try {
            DataInputStream dis = new DataInputStream(cp.openInputStream("//"));
            try {
                for (;;) {
                    String name = dis.readUTF();
                    if (name.endsWith(".class")) {
                        name = name.substring(0, name.length() - ".class".length());
                        name = name.replace('/', '.');
                        classes.addElement(name);
                    } else if (nameInSkipList(name)) {
                        System.out.println("Skipping resource file: " + name);
                        continue;
                    } else {
                        try {
                            byte[] bytes = cp.getBytes(name);
                            ResourceFile resource = new ResourceFile(name, bytes);
                            resources.addElement(resource);
                        } catch (IOException e) {
                        }
                    }
                }
            } catch (EOFException ex) {
            }
            dis.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Processes a single command line argument that specifies one or more
     * class names or resource names.
     *
     * @param   arg      the command line argument to process
     * @param   classes  the list of class names to augment
     * @param   resources Vector<ResourceFile> the list of resources to augment
     */
    public static void processClassArg(String arg, Vector classes, Vector resources) throws HostedPragma {
        if (arg.charAt(0) == '@') {
            arg = arg.substring(1);
            processClassListArg(arg, classes);
        } else if (arg.endsWith(".zip") || arg.endsWith(".jar")) {
            processClassJarOrZipArg(arg, classes, resources);
        } else {
            DataInputStream dis = null;
            try {
                dis = Connector.openDataInputStream("file://" + arg + "//");
                try {
                    int baseDirPrefix = arg.length() + 1;
                    while (true) {
                        String fullName = dis.readUTF();
                        if (fullName.indexOf(".svn/") != -1) {
                            continue;
                        }
                        /*
                         * Strip off the base directory name
                         */
                        String name = fullName.substring(baseDirPrefix);
                        int indexOfExclude = name.indexOf(".svn/");
                        if (indexOfExclude != -1) {
                            continue;
                        }
                        indexOfExclude = name.indexOf(".hg/");
                        if (indexOfExclude != -1) {
                            continue;
                        }
                        indexOfExclude = name.indexOf("CVS/");
                        if (indexOfExclude != -1) {
                            continue;
                        }
                        if (fullName.endsWith(".DS_Store")) {
                            continue;
                        }
                        if (name.endsWith(".class")) {
                            name = name.substring(0, name.length() - ".class".length());
                            name = name.replace('/', '.');
                            classes.addElement(name);
                        } else if (nameInSkipList(name)) {
                            System.out.println("Skipping resource file: " + name);
                            continue;
                        } else {
                            InputStream input= null;
                            ByteArrayOutputStream output;
                            try {
                                input = Connector.openInputStream("file://" + fullName);
                                output = new ByteArrayOutputStream(1024);
                                byte[] buffer;
                                int read;
                                buffer = new byte[256];
                                int bufferSize = buffer.length;
                                while ((read = input.read(buffer, 0, bufferSize)) != -1) {
                                    output.write(buffer, 0, read);
                                }
                                buffer = output.toByteArray();
                                ResourceFile resource = new ResourceFile(name, buffer);
                                resources.addElement(resource);
                            } catch (IOException e) {
                                if (input != null) {try {input.close();} catch (IOException e1) {}};
                            }
                        }
                    }
                } catch (EOFException ex) {
                    return;
                }
            } catch (ConnectionNotFoundException e) {
                // argument is not a directory - add it as a class name
                classes.addElement(arg);
            } catch (IOException ioe) {
                throw new RuntimeException("error parsing '" + arg + "': " + ioe);
            } finally {
                if (dis != null) {
                    try {
                        dis.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        Assert.shouldNotReachHere();
                    }
                }
            }
        }
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
