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

import java.util.*;
import java.io.*;

/**
 * Generator for the source of the class com.sun.squawk.vm.OPC.
 *
 */
public class OPC extends Generator {

    /**
     * {@inheritDoc}
     */
    void generate(PrintWriter out) {

        List<Instruction> instructions = Instruction.getInstructions();
        List<Instruction> floatInstructions = Instruction.getFloatInstructions();
        List<Instruction> allInstructions = Instruction.getAllInstructions();

        printCopyright(this.getClass(), out);

        // Generate class header
        out.println("package com.sun.squawk.vm;");
        out.println();
        out.println("/**");
        out.println(" * This class defines the bytecodes used in the Squawk system.");
        out.println(" *");
        out.println(" */");

        // Generate opcode constants
        out.println("public final class OPC {");
        out.println();
        printOpcodes(out, instructions, "Non-floating point instructions");
        out.println();
        out.println("/*if[FLOATS]*/");
        printOpcodes(out, floatInstructions, "Floating point instructions");
        out.println("/*end[FLOATS]*/");

        // Generate getSize() function
        out.println();
        out.println("    /**");
        out.println("     * Gets the size (in bytes) of an instruction based on a given opcode.");
        out.println("     *");
        out.println("     * @param  opcode  an instruction opcode");
        out.println("     * @return the size of the instruction");
        out.println("     * @throws IndexOutOfBoundsException if <code>opcode</code> is not valid");
        out.println("     * @throws IllegalArgumentException if <code>opcode</code> denotes a prefix or variable size instruction");
        out.println("     */");
        out.println("    public static int getSize(int opcode) {");
        out.println("        int size = (byte)sizes.charAt(opcode);");
        out.println("        if (size < 1) {");
        out.println("            throw new IllegalArgumentException();");
        out.println("        }");
        out.println("        return size;");
        out.println("    }");
        out.println();
        out.println("    private final static String sizes =");
        int opcode = printSizesDef(out, instructions, 0);
        out.println("/*if[FLOATS]*/");
        printSizesDef(out, floatInstructions, opcode);
        out.println("/*end[FLOATS]*/");
        out.println("       \"\";");

        // Generate Properties inner class
        int[] deltas = new int[2];
        calculateWideDelta(allInstructions, deltas);
        out.println("    public static class Properties {");
        out.println("        /** The number of non-floating point instructions. */");
        out.println("        public static final int NON_FLOAT_BYTECODE_COUNT = " + instructions.size() + ";");
        out.println();
        out.println("        /** The number of floating point instructions. */");
        out.println("        public static final int FLOAT_BYTECODE_COUNT = /*VAL*/false/*FLOATS*/ ? " + floatInstructions.size() + " : 0;");
        out.println();
        out.println("        /** The total number of instructions. */");
        out.println("        public static final int BYTECODE_COUNT = NON_FLOAT_BYTECODE_COUNT + FLOAT_BYTECODE_COUNT;");
        out.println();
        out.println("        /** The delta that is applied to an opcode < 256 to get the widened version of the opcode. */");
        out.println("        public static final int WIDE_DELTA = " + deltas[0] + ";");
        out.println();
        out.println("        /** The delta that is applied to an opcode >= 256 to get the widened version of the opcode. */");
        out.println("        public static final int ESCAPE_WIDE_DELTA = " + deltas[1] + ";");
        out.println("    }");

        // Generate widened attribute test
        out.println();
        out.println("    /**");
        out.println("     * Determines if a given opcode has a wide version.");
        out.println("     *");
        out.println("     * @param  opcode  an instruction opcode");
        out.println("     * @return true if there is a wide version of <code>opcode</code>");
        out.println("     * @throws IndexOutOfBoundsException if <code>opcode</code> is not valid");
        out.println("     */");
        out.println("    public static boolean hasWide(int opcode) {");
        out.println("        int unit = wideTable.charAt(opcode / 8);");
        out.println("        return (unit & (1 << (opcode % 8))) != 0;");
        out.println("    }");
        out.println();
        out.println("    private final static String wideTable = \"" + getWideTable(allInstructions) + "\";");

        out.println("}");
    }

    private String getWideTable(List<Instruction> list) {
        byte[] widened = new byte[(list.size() + 7) / 8];
        for (Instruction instruction: list) {
            if (instruction.wide() != null) {
                int bit = instruction.opcode;
                widened[bit / 8] |= (byte)(1 << (bit % 8));
            }
        }

        StringBuffer buf = new StringBuffer(widened.length * 5);
        for (int i = 0; i != widened.length; ++i) {
            buf.append(encodeByteAsChar((char)widened[i]));
        }
        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    public File getGeneratedFile(File baseDir) {
        return new File(baseDir, "com/sun/squawk/vm/OPC.java");
    }

    /**
     * Prints the opcode constant definitions for the instructions in a given list.
     *
     * @param out      where to print
     * @param list     a list of Instructions
     * @param comment  a string that will be prepended to the definitions as a javadoc comment
     */
    private static void printOpcodes(PrintWriter out, List<Instruction> list, String comment) {
        out.println("    /** " + comment + ". */");
        out.println("    public final static int");
        for (Iterator<Instruction> iterator = list.iterator(); iterator.hasNext(); ) {
            Instruction instruction = iterator.next();
            out.print(pad("        " + instruction.mnemonic.toUpperCase(), 30) + " = " + instruction.opcode);
            if (iterator.hasNext()) {
                out.println(",");
            } else {
                out.println(";");
            }
        }
    }

    /**
     * Calculates the delta between the wide opcodes and their compact form in a given
     * list of instructions. The delta must be constant within the list.
     *
     * @param list  a list of instructions
     * @return the delta
     */
    private static void calculateWideDelta(List<Instruction> list, int[] deltas) {
        for (Instruction instruction: list) {
            if (instruction.compact != null) {
                int index = (instruction.compact.opcode < 256 ? 0 : 1);
                if (deltas[index] == 0) {
                    deltas[index] = instruction.opcode - instruction.compact.opcode;
                } else {
                    if (deltas[index] != (instruction.opcode - instruction.compact.opcode)) {
                        throw new RuntimeException("delta between opcodes and their widened version is not constant: " +
                           "opcode = " + instruction.compact.opcode + " wide opcode = " + instruction.opcode);
                    }
                }
            }
        }
    }

    private static int printSizesDef(PrintWriter out, List<Instruction> list, int opcodeCheck) {

        for (Instruction instruction: list) {
            if (opcodeCheck != instruction.opcode) {
                throw new RuntimeException("instructions are not ordered by opcode");
            }

            int size;
            if (instruction.iparm == Instruction.IParm.T) {
                size = -1; // table instructions are variable length
            } else {
                size = instruction.iparm.size + 1; // add one for opcode
            }
            out.println("        " + pad("/* " + instruction.mnemonic.toUpperCase() + " */", 32) +
                        "\"" + encodeByteAsChar((char)size) + "\" +");
            opcodeCheck++;
        }
        return opcodeCheck;
    }

    private static String encodeByteAsChar(char ch) {
        char converted = (char)((byte)ch);
        if (converted != ch) {
            throw new RuntimeException("Cannot encode a non-byte sized value as a char in a StringOfBytes");
        }

        return "\\u00" + hex(ch>>4) + hex(ch>>0);
    }

    private static char hex(int i) {
        String hextable = "0123456789abcdef";
        return hextable.charAt(i&0xF);
    }
}