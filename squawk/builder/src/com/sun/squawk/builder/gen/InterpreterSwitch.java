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

/**
 * Generator for the class com.sun.squawk.translator.ir.Verifier.
 *
 */
public class InterpreterSwitch extends Verifier {

    protected void printHeader(PrintWriter out) {
        out.println("package com.sun.squawk.vm;");
        out.println("");
        out.println("import com.sun.squawk.compiler.*;");
        out.println();
        out.println("/**");
        out.println(" * This class defines the switch table used by the generated Squawk interpreter.");
        out.println(" *");
        out.println(" * @author   Nik Shaylor");
        out.println(" */");
        out.println("abstract public class InterpreterSwitch extends Common implements Types {");
        out.println();
        out.println("    /**");
        out.println("     * Flags to show how the loading of the next bytecode should be done.");
        out.println("     */");
        out.println("    protected final static int FLOW_NEXT   = 0, // The next bytecode is always executed after the current one.");
        out.println("                               FLOW_CHANGE = 1, // The bytecode changes the control flow.");
        out.println("                               FLOW_CALL   = 2; // The bytecode either calls a routine, or might throw an exception.");
        out.println();
        out.println("    abstract protected void pre(int code);");
        out.println("    abstract protected void post();");
        out.println("    abstract protected void bind(int opcode);");
        out.println("    abstract protected void iparmNone();");
        out.println("    abstract protected void iparmByte();");
        out.println("    abstract protected void iparmUByte();");
        out.println();
        out.println("    /**");
        out.println("     * Create the bytecode interpreter.");
        out.println("     */");
        out.println("    protected void do_switch() {");
        out.println("        {");
    }

    String getFirstLine() {
        return null;
    }

    String startCase(Instruction instruction) {
        return "bind(OPC." + instruction.mnemonic.toUpperCase() + ");";
    }

    String endCase() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    String getFunction(Instruction instruction, boolean call) {
        return "pre(FLOW_" + instruction.flow.name + "); " + super.getFunction(instruction, true) + " post();";
    }

    /**
     * {@inheritDoc}
     */
    public File getGeneratedFile(File baseDir) {
        return new File(baseDir, "com/sun/squawk/vm/InterpreterSwitch.java");
    }
}
