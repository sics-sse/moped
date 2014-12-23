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
 * Generator for the class com.sun.squawk.translator.ir.Verifier.
 *
 */
public class Verifier extends AbstractSwitch {


    String getFirstLine() {
        return "//if[SUITE_VERIFIER]";
    }

    void printFooter(PrintWriter out) {
        out.println("        }");
        out.println("    }");
        out.println("}");
    }

    void printHeader(PrintWriter out) {
        out.println("package com.sun.squawk.translator.ir.verifier;");
        out.println("import com.sun.squawk.vm.OPC;");
        out.println("import com.sun.squawk.util.Assert;");
        out.println();
        out.println("/**");
        out.println(" * This class defines the switch table used by the backend verifier in the translator.");
        out.println(" *");
        out.println(" */");
        out.println("public class Verifier extends VerifierBase {");
        out.println("");
        out.println("    /**");
        out.println("     * Verifies the current instruction denoted by the opcode variable.");
        out.println("     *");
        out.println("     * @param opcode the opcode to verify");
        out.println("     */");
        out.println("    protected void do_switch() {");
        out.println("        switch(opcode) {");
    }

    static final Pattern NUMERIC_SUFFIX = Pattern.compile("(.*)_([\\d]+)");

    String getFunction(Instruction instruction, boolean call) {
        String mnemonic = instruction.mnemonic;

        if (mnemonic.endsWith("_m1")) {
            String parms = call ? "(-1)" : "(int n)";
            return "do_" + mnemonic.substring(0, mnemonic.length() - 3) + parms + ";";
        }

        // Handle instructions that have an implicit numeric operand in their opcode
        Matcher m = NUMERIC_SUFFIX.matcher(mnemonic);
        if (m.matches()) {
            String prefix = m.group(1);
            String parms = call ? "(" + m.group(2) + ")" : "(int n)";
            return "do_" + prefix + parms + ";";
        }

        String type = "";
        if (mnemonic.charAt(mnemonic.length() - 2) == '_') {
            switch (mnemonic.charAt(mnemonic.length() - 1)) {
                case 'i': type = "INT";    break;
                case 'l': type = "LONG";   break;
                case 'f': type = "FLOAT";  break;
                case 'd': type = "DOUBLE"; break;
                case 'o': type = "OOP";    break;
                case 'b': type = "BYTE";   break;
                case 's': type = "SHORT";  break;
                case 'c': type = "USHORT"; break;
                case 'v': type = "VOID";   break;
                default: throw new RuntimeException("instruction with unknown mnemonic suffix: " + mnemonic);
            }
            mnemonic = mnemonic.substring(0, mnemonic.length() - 2);
        }

        if (mnemonic.startsWith("if_cmp")) {
            String cc = mnemonic.substring(6, 8);
            String parms = call ? "(2, " + cc.toUpperCase() + ", " + type + ")" :
                                  "(int operands, int cc, Type t)";
            return "do_if" + parms + ";";
        }

        if (mnemonic.startsWith("if_")) {
            String cc = mnemonic.substring(3, 5);
            String parms = call ? "(1, " + cc.toUpperCase() + ", " + type + ")" :
                                  "(int operands, int cc, Type t)";
            return "do_if" + parms + ";";
        }

        if (type != "") {
            String parms = call ? "(" + type + ")" : "(Type t)";
            return "do_" + mnemonic + parms + ";";
        }
        return "do_" + mnemonic + "();";
    }

    String startCase(Instruction instruction) {
        return "case OPC." + instruction.mnemonic.toUpperCase() + ": ";
    }

    String endCase() {
        return "break;";
    }

    /**
     * Prints the opcode constant definitions for the instructions in a given list.
     *
     * @param out      where to print
     * @param list     a list of Instructions
     */
    void printCases(PrintWriter out, List<Instruction> list) {
        for (Instruction instruction: list) {
            if (instruction.compact == null) {
                out.print(pad("            " + startCase(instruction), 50));
                Instruction wide = instruction.wide();
                if (wide == null) {
                    out.println("iparmNone();");
                    out.println(pad("", 50) +
                                pad(getFunction(instruction, true), 35)
                                + endCase());
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
                    out.println(pad("            " + startCase(wide), 50) +
                                pad(getFunction(instruction, true), 35) +
                               endCase());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public File getGeneratedFile(File baseDir) {
        return new File(baseDir, "com/sun/squawk/translator/ir/verifier/Verifier.java");
    }
}