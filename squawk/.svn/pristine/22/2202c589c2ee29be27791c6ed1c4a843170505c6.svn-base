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
import java.util.regex.*;

/**
 * Generator for the file switch.c, a part of the Squawk VM C based interpreter.
 *
 */
public class SwitchDotC extends Generator {


    /**
     * {@inheritDoc}
     */
    void generate(PrintWriter out) {

        List<Instruction> instructions = Instruction.getInstructions();
        List<Instruction> floatInstructions = Instruction.getFloatInstructions();

        printCopyright(this.getClass(), out);

        // Generate class header
        out.println();
        out.println("        switch(opcode) {");

        // Generate opcode constants
        printCases(out, instructions);
        out.println();
        out.println("/*if[FLOATS]*/");
        printCases(out, floatInstructions);
        out.println("/*end[FLOATS]*/");
        out.println("           default: fatalVMError(\"unimplemented opcode\");");
        out.println("        }");
    }

    private static final Pattern NUMERIC_SUFFIX = Pattern.compile("(.*)_([\\d]+)");

    private static String getFunction(Instruction instruction) {
        String mnemonic = instruction.mnemonic;

        if (mnemonic.endsWith("_m1")) {
            return "do_" + mnemonic.substring(0, mnemonic.length() - 3) + "_n(-1);";
        }

        Matcher m = NUMERIC_SUFFIX.matcher(mnemonic);
        if (m.matches()) {
            String prefix = m.group(1);
            String suffix = m.group(2);
            return "do_" + prefix + "_n(" + suffix + ");";
        }

        if (mnemonic.startsWith("getfield") || mnemonic.startsWith("putfield")) {
            boolean oopIn0 = false;
            if (mnemonic.charAt("getfield".length()) == '0') {
                oopIn0 = true;
                // Remove the '0'
                mnemonic = mnemonic.substring(0, 8) + mnemonic.substring(9);
            }
            return "do_" + mnemonic + "(" + oopIn0 + ");";
        }

        if (mnemonic.indexOf("getstatic") != -1 || mnemonic.indexOf("putstatic") != -1) {
            boolean inCP = false;
            if (mnemonic.startsWith("class_")) {
                inCP = true;
                // Remove the 'class_'
                mnemonic = mnemonic.substring(6);
            }
            return "do_" + mnemonic + "(" + inCP + ");";
        }

        if (mnemonic.startsWith("if_")) {
            boolean zero = true;
            if (mnemonic.startsWith("if_cmp")) {
                zero = false;
                // Remove the 'cmp'
                mnemonic = mnemonic.substring(0, 3) + mnemonic.substring(6);
            }
            return "do_" + mnemonic + "(" + zero + ");";
        }

        if (mnemonic.startsWith("invoke")) {
            int suffixStart = mnemonic.length() - 2;
            if (mnemonic.charAt(suffixStart) != '_') {
                throw new RuntimeException("unrecognised invoke instruction");
            }
            // Strip the return type suffix
            mnemonic = mnemonic.substring(0, suffixStart);
            return "do_" + mnemonic + "();";
        }

        if (mnemonic.startsWith("tableswitch")) {
            int size;
            if (mnemonic.endsWith("_s")) {
                size = 2;
            } else {
                if (!mnemonic.endsWith("_i")) {
                    throw new RuntimeException("unrecognised tableswitch instruction");
                }
                size = 4;
            }
            return "do_tableswitch(" + size + ");";
        }

        return "do_" + mnemonic + "();";
    }

    /**
     * Prints the opcode constant definitions for the instructions in a given list.
     *
     * @param out      where to print
     * @param list     a list of Instructions
     */
    private static void printCases(PrintWriter out, List<Instruction> list) {
        for (Instruction instruction: list) {
            if (instruction.compact == null) {
                out.print(pad("            case OPC_" + instruction.mnemonic.toUpperCase() + ": ", 50));
                Instruction wide = instruction.wide();
                if (wide == null) {
                    out.println("iparmNone();");
                    out.println(pad("", 50) + pad(getFunction(instruction), 35) + "break;");
                } else {
                    Instruction.IParm iparm = instruction.iparm;
                    if (iparm == Instruction.IParm.B) {
                        out.println("iparmByte();");
                    } else {
                        if (iparm != Instruction.IParm.A) {
                            throw new RuntimeException("a widenable instruction can only have a byte or ubyte immediate parameter");
                        }
                        out.println("iparmUByte();");
                    }
                    out.println(pad("            case OPC_" + wide.mnemonic.toUpperCase() + ": ", 50) +
                                pad(getFunction(instruction), 35) + "break;");
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public File getGeneratedFile(File baseDir) {
        return new File(baseDir, "vm/switch.c.spp");
    }
}