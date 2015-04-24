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

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.util.*;

/**
 * Used to create C code during conversion of a Java method to a C function.
 *
 */
public class CCodeBuffer {

    /**
     * The buffer in which this buffer is nested (if any).
     */
    private final CCodeBuffer owner;

    /**
     * The buffer for building the C body of the current method.
     */
    private final StringWriter out;

    /** Indentation width.
     */
    private int width = 4;

    /** The current left margin.
     */
    int lmargin = 0;


    public CCodeBuffer() {
        this.out = new StringWriter();
        this.owner = null;
    }

    private CCodeBuffer(CCodeBuffer owner) {
        this.out = new StringWriter();
        this.owner = owner;
    }

    /**
     * Creates a new buffer to which output can be redirected while still
     * collecting callees to this buffer.
     */
    public CCodeBuffer enter() {
        return new CCodeBuffer(this);
    }

    /**
     * Leaves a buffer used for redirection and return the buffer from which
     * it was created.
     */
    public CCodeBuffer leave() {
        assert owner != null;
        return owner;
    }

    /**
     * Align code to be indented to left margin.
     */
    void align() {
        for (int i = 0; i < lmargin; i++) {
            out.write(' ');
        }
    }

    /**
     * Increase left margin by indentation width.
     */
    void indent() {
        lmargin = lmargin + width;
    }

    /**
     * Decrease left margin by indentation width.
     */
    void undent() {
        lmargin = lmargin - width;
    }

    /**
     * Enter a new precedence level. Emit a `(' if new precedence level
     * is less than precedence level so far.
     *
     * @param contextPrec    The precedence level in force so far.
     * @param ownPrec        The new precedence level.
     */
    void open(int contextPrec, int ownPrec) {
        if (ownPrec < contextPrec) {
            write("(");
        }
    }

    /**
     * Leave precedence level. Emit a `(' if inner precedence level
     * is less than precedence level we revert to.
     *
     * @param contextPrec    The precedence level we revert to.
     * @param ownPrec        The inner precedence level.
     */
    void close(int contextPrec, int ownPrec) {
        if (ownPrec < contextPrec) {
            write(")");
        }
    }

    /**
     * Print string, replacing all non-ascii character with unicode escapes.
     */
    public void print(Object s) {
        write(Convert.escapeUnicode(s.toString()));
    }

    public void aprint(Object s) {
        align();
        print(s);
    }

    /**
     * Print string, replacing all non-ascii character with unicode escapes.
     */
    public void println(Object s) {
        write(Convert.escapeUnicode(s.toString()));
        write(LINE_SEP);
    }

    public void aprintln(Object s) {
        align();
        println(s);
    }

    /**
     * Prints the body of a C function including the surrounding '{' anf '}'.
     */
    public void printFunctionBody(String body) {
        print("{");
        println();
        indent();
        align();
        print(body);
        println();
        undent();
        align();
        print("}");
        println();
    }

    /**
     * Prints the body of a C function including the surrounding '{' anf '}'.
     */
    public void printMacroBody(String body) {
        print(body);
        println();
    }

    /**
     * Print new line.
     */
    public void println() {
        write(LINE_SEP);
    }

    private void write(String s) {
        out.write(s);
    }

    public String toString() {
        return out.toString();
    }

    public static final String LINE_SEP = System.getProperty("line.separator");
}
