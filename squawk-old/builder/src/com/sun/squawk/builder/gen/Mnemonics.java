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
 * Generator for the source of the class com.sun.squawk.vm.Mnemonics.
 *
 */
public class Mnemonics extends Generator {

    /**
     * {@inheritDoc}
     */
    void generate(PrintWriter out) {

        // Generate class header
        printCopyright(this.getClass(), out);
        out.println("package com.sun.squawk.vm;");
        out.println();
        out.println("import com.sun.squawk.util.Assert;");
        out.println();
        out.println("/**");
        out.println(" * This class defines effect of the Squawk VM instructions on the operand stack.");
        out.println(" *");
        out.println(" */");
        out.println("public final class Mnemonics {");
        out.println();

        // Generate getEffect() function
        out.println();
        out.println("    /**");
        out.println("     * Gets the effect of an instruction on the operand stack. Each character");
        out.println("     * in the returned string preceeding the ':' character denotes a type of value");
        out.println("     * popped from the stack and a character after the ':' denotes a type of value");
        out.println("     * pushed to the stack. The possible types in the string are:");
        out.println("     *");
        out.println("     *    I  int");
        out.println("     *    O  object/reference");
        out.println("     *    F  float");
        out.println("     *    W  address/word/offset");
        out.println("     *    L  long");
        out.println("     *    D  double");
        out.println("     *    *  clears the stack (only preceeds ':')");
        out.println("     *");
        out.println("     * @param  opcode  an instruction opcode");
        out.println("     * @return the effect of the instruction on the operand stack");
        out.println("     * @throws IndexOutOfBoundsException if <code>opcode</code> is not valid");
        out.println("     */");
        out.println("    public static String getMnemonic(int opcode) {");
        out.println("        return mnemonics[opcode];");
        out.println("    }");
        out.println("     ");
	out.println("    public static int length() {");
	out.println("        return mnemonics.length;");
	out.println("    }");
	out.println("     ");

        // Generate operand stack effects table
        out.println("    private final static String[] mnemonics = { ");
        int opcode = printMnemonics(out, Instruction.getInstructions(), 0, false);
        out.println("/*if[FLOATS]*/");
        printMnemonics(out, Instruction.getFloatInstructions(), opcode, true);
        out.println("/*end[FLOATS]*/");
        out.println("    };");

        out.println("}");
    }

    /**
     * {@inheritDoc}
     */
    public File getGeneratedFile(File baseDir) {
        return new File(baseDir, "com/sun/squawk/vm/Mnemonics.java");
    }

    private static int printMnemonics(PrintWriter out, List<Instruction> list, int opcodeCheck, boolean closeArrayInitializer) {

        for (Iterator<Instruction> iterator = list.iterator(); iterator.hasNext(); ) {
            Instruction instruction = iterator.next();
            if (opcodeCheck != instruction.opcode) {
                throw new RuntimeException("instructions are not ordered by opcode");
            }

            out.print("        \"" + instruction.mnemonic + "\"");
            if (closeArrayInitializer && !iterator.hasNext()) {
                out.println();
            } else {
                out.println(",");
            }
            opcodeCheck++;
        }
        return opcodeCheck;
    }
}