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
 * Generator for the class com.sun.squawk.vm.JitterSwitch.
 *
 */
public class JitterSwitch extends Verifier {

    protected void printHeader(PrintWriter out) {
        out.println("package com.sun.squawk.vm;");
        out.println("");
        out.println("import com.sun.squawk.compiler.*;");
        out.println("import com.sun.squawk.util.Assert;");
        out.println();
        out.println("/**");
        out.println(" * This class defines the switch table used by the Squawk jitter.");
        out.println(" *");
        out.println(" * @author   Nik Shaylor");
        out.println(" */");
        out.println("abstract public class JitterSwitch extends Common implements Types {");
        out.println("");
        out.println("    /**");
        out.println("     * The the immediate operand value of the current bytecode.");
        out.println("     */");
        out.println("    protected int iparm;");
        out.println();
        out.println("    abstract protected void iparmNone();");
        out.println("    abstract protected void iparmByte();");
        out.println("    abstract protected void iparmUByte();");
        out.println();
        out.println("    /**");
        out.println("     * Generate the native code for a bytecode.");
        out.println("     *");
        out.println("     * @param opcode the opcode to jit");
        out.println("     */");
        out.println("    protected void do_switch(int opcode) {");
        out.println("        switch(opcode) {");
    }
    
    protected void printFooter(PrintWriter out) {
        out.println("            default: Assert.shouldNotReachHere(\"unknown opcode \" + opcode);");
        super.printFooter(out);
    }
    
    String getFirstLine() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public File getGeneratedFile(File baseDir) {
        return new File(baseDir, "com/sun/squawk/vm/JitterSwitch.java");
    }
}
