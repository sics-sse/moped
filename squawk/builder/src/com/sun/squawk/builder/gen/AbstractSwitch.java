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
 * Abstract generator for the code that has the form of a switch statement with a case for each instruction.
 *
 */
abstract public class AbstractSwitch extends Generator {


    /**
     * {@inheritDoc}
     */
    void generate(PrintWriter out) {

        List<Instruction> instructions = Instruction.getInstructions();
        List<Instruction> floatInstructions = Instruction.getFloatInstructions();

        String firstLine = getFirstLine();
        if (firstLine != null) {
            out.println(firstLine);
        }

        printCopyright(this.getClass(), out);

        // Generate header
        printHeader(out);

        // Generate opcode constants
        printCases(out, instructions);
        out.println();
        out.println("/*if[FLOATS]*/");
        printCases(out, floatInstructions);
        out.println("/*end[FLOATS]*/");

        printFooter(out);
    }

    String getFirstLine() {
        return null;
    }

    abstract void printFooter(PrintWriter out);
    abstract void printHeader(PrintWriter out);

    /**
     * Prints the opcode constant definitions for the instructions in a given list.
     *
     * @param out      where to print
     * @param list     a list of Instructions
     */
    abstract void printCases(PrintWriter out, List<Instruction> list);
}