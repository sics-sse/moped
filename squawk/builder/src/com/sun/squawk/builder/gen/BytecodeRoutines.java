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
 * Generator for the class com.sun.squawk.translator.ir.Verifier.
 *
 */
public class BytecodeRoutines extends Verifier {

    protected void printHeader(PrintWriter out) {
        out.println("package com.sun.squawk.vm;");
        out.println("");
        out.println("import com.sun.squawk.compiler.*;");
        out.println();
        out.println("/**");
        out.println(" * This class defines the routines used by the Squawk interpreter and jitter.");
        out.println(" *");
        out.println(" * @author   Nik Shaylor");
        out.println(" */");
        out.println("abstract public class BytecodeRoutines implements Types {");
    }

    String getFirstLine() {
        return null;
    }

    void printFooter(PrintWriter out) {
        out.println("}");
    }

    private Set<String> functionDefs = new HashSet<String>();

    void printCases(PrintWriter out, List<Instruction> list) {
        for (Instruction instruction: list) {
            if (instruction.compact == null) {
                String functionDef = getFunction(instruction, false);
                if (!functionDefs.contains(functionDef)) {
                    functionDefs.add(functionDef);
                    out.println("    abstract protected void " + functionDef);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public File getGeneratedFile(File baseDir) {
        return new File(baseDir, "com/sun/squawk/vm/BytecodeRoutines.java");
    }
}