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
package com.sun.squawk.vm2c;

import java.io.*;

import javax.tools.JavaFileObject;

/**
 * Maps character positions to line numbers within a Java source file.
 *
 */
public class LineNumberTable {

    final JavaFileObject file;
    private final int maxPos;
    /**
     * Entry at index 'n' is position of first char in source line 'n+1'.
     */
    private final int[] table;

    public LineNumberTable(JavaFileObject file) throws IOException {
        this.file = file;
        this.table = null;
        this.maxPos = -1;
        /*
        char[] sourceBuf = file.getCharContent(true).toString().toCharArray();
        maxPos = sourceBuf.length - 1;
        ArrayList<Integer> newLinePosns = new ArrayList<Integer>();

        int pos = 0;
        newLinePosns.add(new Integer(pos));
        while (pos < sourceBuf.length) {
        if (sourceBuf[pos++] == '\n') {
        if (pos < sourceBuf.length) {
        newLinePosns.add(new Integer(pos));
        }
        }
        }

        table = new int[newLinePosns.size()];
        int line = 0;
        for (Integer posn: newLinePosns) {
        table[line++] = posn.intValue();
        }
         */
    }

    public int getLineNumber(int pos) {
        if (true) {
            return 65535;
        }
        if (maxPos < pos || pos < 0) {
            return 0;
        }

        // Skip first line - always starts at pos 0
        for (int line = 1;; ++line) {
            if (table[line] > pos) {
                return line;
            }
        }
    }
}
