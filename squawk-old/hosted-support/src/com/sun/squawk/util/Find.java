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

package com.sun.squawk.util;

import java.util.*;
import java.util.Vector;
import java.io.*;
import java.util.zip.*;

/**
 * This class provides a limited subset of the unix find utility.
 */
public class Find {

    /**
     * Find all the class files in a given path. Each entry in the path is a directory
     * (given in Unix format i.e. '/' as separator) and each dir is separated by ':' or ';'.
     * @param addTo The unique list of classes found are returned in this variable.
     */
    @SuppressWarnings(value = "unchecked")
    public static void findAllClassesInPath(String path, Vector addTo) {
        StringTokenizer st = new StringTokenizer(path,":;");
        HashSet uniqClasses = new HashSet();
        while (st.hasMoreTokens()) {
            Vector classes = new Vector();

            String entry = st.nextToken();
            if (entry.endsWith(".zip") || entry.endsWith(".jar")) {
                ZipFile zipFile = null;
                try {
                    zipFile = new ZipFile(entry);
                    Enumeration e = zipFile.entries();
                    while (e.hasMoreElements()) {
                        ZipEntry zipEntry = (ZipEntry)e.nextElement();
                        String name = zipEntry.getName();
                        if (name.endsWith(".class")) {
                            addTo.addElement(name.substring(0, name.length() - ".class".length()));
                        }
                    }
                } catch (IOException ioe) {
                    System.err.println("Exception while opening/reading " + entry);
                    continue;
                } finally {
                    if (zipFile != null) {
                        try {
                            zipFile.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            }
            else {
                String dirName = entry;

                /*
                 * Add ending '/' if it's missing
                 */
                if (!dirName.endsWith("/")) {
                    dirName += "/";
                }

                /*
                 * Convert to file system specific path and search
                 */
                File dir = new File(dirName.replace('/',File.separatorChar));
                try {
                    find(dir, ".class", classes, false);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    continue;
                }
                for (int i = 0; i != classes.size(); i++) {
                    String file = (String)classes.elementAt(i);
                    file = file.replace(File.separatorChar,'/');
                    if (!(file.startsWith(dirName) && file.endsWith(".class"))) {
                        throw new RuntimeException("find went wrong: file="+file+", dirName="+dirName);
                    }
                    file = file.substring(dirName.length(),file.length()-6);
                    uniqClasses.add(file);
                }
            }
        }
        addTo.addAll(uniqClasses);
    }

    /**
     * Find all the files or directories under a given directory recursively.
     *
     * @param   dir      the directory to search from
     * @param   suffix   the suffix used to filter the results or null
     *                   for no filtering
     * @param   results  the Vector into which the found files should be put
     * @throws  IOException
     */
    @SuppressWarnings(value = "unchecked")
    public static void find(File dir, String suffix, Vector results, boolean dirs) throws IOException {
        File[] files = dir.listFiles();
        if (dirs && (suffix == null || dir.getName().endsWith(suffix))) {
            results.addElement(dir.getPath());
        }
        if (files == null) {
            return;
        }
        for(int i = 0 ; i < files.length ; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                find(f, suffix, results, dirs);
            } else {
                if (!dirs && (suffix == null || f.getName().endsWith(suffix))) {
                    results.addElement(f.getPath());
                }
            }
        }
    }

    private Find() {
    }
}
