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

package com.sun.squawk.compiler;

import com.sun.squawk.compiler.Compiler;

/**
 * Steps of compilation: compile and link.
 *
 */
public class Compilation {

    /**
     * Create a new compiler from a class name.
     *
     * @param compilerClassName  the name
     * @return                   the compiler
     */
    private static Compiler newCompilerPrim(String compilerClassName) {
        try {
            Class compilerClass = Class.forName(compilerClassName);
            return (Compiler) compilerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not load and instantiate compiler class " + compilerClassName + " [" + e.getMessage() + "]");
        }
    }

    /**
     * Create a new compiler for a specific architecture.
     *
     * @param arch the architecture  name
     * @return                       the compiler
     */
    public static Compiler newCompiler(String arch) {
        return newCompilerPrim("com.sun.squawk.compiler."+arch+"Compiler");
    }

    /**
     * Create a new default compiler by determining which architecture the VM
     * is running on.  By default, set it to the X86 architecture.
     *
     * @return the compiler
     */
    public static Compiler newCompiler() {
        String arch = "${build.properties:ARCHITECTURE}"; // Will be edited by the romizer
        if (arch.endsWith("ARCHITECTURE}")) {             // If the code is not romized...
            arch = System.getProperty("squawk.architecture");
            if (arch == null) {
                arch = "X86"; // The platform du jour
            }
        }
        return newCompiler(arch);
    }

    /**
     * Create a new linker.
     *
     * @param compiler  the compiler to be linked
     * @return          the linker
     */
/*TODO
    public static Linker newLinker(Compiler compiler) {
        return new Linker(compiler);
    }
*/
}
