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

import java.io.File;

/**
 * A FileVisitor provides simple mechanism for traversing a set of files in the file system.
 */
public abstract class FileVisitor {

    /**
     * Exception thrown to terminate a visit.
     */
    static class TerminateVisitException extends Exception {
    }

    private static TerminateVisitException VISIT_TERMINATOR = new TerminateVisitException();

    public static boolean matchGlob(String string, String pattern, int stringStart, int patternStart) {
        int stringLength = string.length();
        int patternLength = pattern.length();
        
        while (true) {
            if (stringStart == stringLength)
                return patternStart == patternLength;
            if (patternStart == patternLength)
                return false;
 
            char a = string.charAt(stringStart);
            char b = pattern.charAt(patternStart);
            
            if (b == '*') {
                if (++patternStart == patternLength)
                    return true;
                for (int n = stringStart; n <= stringLength; n++)
                    if (matchGlob(string, pattern, n, patternStart))
                        return true;
                return false;
            }
 
            if (a != b && b != '?')
                return false;
            stringStart++;
            patternStart++;
        }
    }
    
    /**
     * Traverses one or more files in the file system starting with <code>file</code>,
     * invoking the {@link #visit(File)} call back for each file or directory traversed.
     * If <code>file</code> is a directory, then the entries in the directory are visited
     * before the directory itself is visited. If <code>recursive</code> is <code>true</code>,
     * then the traversal recurses over subdirectories.
     * 
     * The name part of the file can contain glob based file name pattern, but pattern will only
     * be applied to tol leve file entries matching the pattern in the parent of the file specified.
     *
     * @param file       the starting point of the traversal
     */
    public void run(File file) {
        try {
        	File parent = file.getParentFile();
        	if (parent == null) {
        		parent = new File(".");
        	}
        	String pattern = file.getName();
        	File[] files = parent.listFiles();
        	if (files == null) {
        	    return;
        	}
        	for (File eachFile: files) {
        		if (matchGlob(eachFile.getName(), pattern, 0, 0)) {
        			run0(eachFile);
        		}
        	}
        } catch (TerminateVisitException e) {
        }
    }

    private void run0(File file) throws TerminateVisitException {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] entries = file.listFiles();
                for (int i = 0; i != entries.length; i++) {
                    run0(entries[i]);
                }
            }
            if (!visit(file)) {
                throw VISIT_TERMINATOR;
            }
        }

    }

    /**
     * This method is invoked for every file or directory in a file system traversal.
     *
     * @param file    the file or directory currently being traversed
     * @return true to indicate that the traversal should continue, false to halt it immediately
     */
    public abstract boolean visit(File file);

    /**
     * Tests harness.
     *
     * @param args
     */
    public static void main(String[] args) {
        File file = new File(".");
        if (args.length != 0) {
            file = new File(args[0]);
        }

        FileVisitor visitor = new FileVisitor() {
            public boolean visit(File file) {
                System.out.println(file.getPath());
                return true;
            }
        };

        System.out.println("Recursive traversal:");
        visitor.run(file);
        System.out.println();
    }
}
