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

package com.sun.squawk.builder.ccompiler;

import java.io.*;

import com.sun.squawk.builder.*;
import com.sun.squawk.builder.platform.*;

/**
 * This class is the abstract interface to the C compiler used to build the slow VM.
 */
public abstract class CCompiler {

    /**
     * The options that control the compilation.
     */
    public static class Options {

        /**
         * Enables tracing code (i.e. enables '-Xts', '-Xtr', '-Xte' VM options). This may slow down the VM considerably.
         */
        public boolean tracing;

        /**
         * Enables profiling code (i.e. enables '-Xprof' VM option). This may slow down the VM considerably.
         */
        public boolean profiling;

        /**
         * Enables kernel mode (i.e. enables asynchronous handling of interrupts). This may slow down the VM slightly.
         */
        public boolean kernel;
   
   		  /**
         * Enables native Verification mode. Uses SHA1 c code instead of java code for suite verification.  This accelerates verification considerable
         */
        public boolean nativeVerification;

        /**
         * Asserts that squawk will run hosted under a 1.4.2+ JVM. This allows us to use the signal-interposition
         * library provided by the hosting JVM.
         */
        public boolean hosted;

        /**
         * Enables compiler flags that optimize for size.
         */
        public boolean o1;

        /**
         * Enables compiler flags that optimize for speed without greatly increasing compile time.
         */
        public boolean o2;

        /**
         * Enables compiler flags that optimize for maximum speed, no matter what the compile cost is.
         */
        public boolean o3;

        /**
         * Enables assertion code. This may slow down the VM considerably.
         */
        public boolean assume;

        /**
         * Enables memory access checking code. This may slow down the VM considerably.
         */
        public boolean typemap;

        /**
         * Enables compiler flags to produce a 64 bit executable. Compilation may fail if 64 bit compilation is not possible.
         */
        public boolean is64;

        /**
         * Includes standard C library "math.h".
         */
        public boolean floatsSupported;

        /**
         * Performs macroization of C functions that are prefixed with directives recognized by the {@link Macro} preprocessor.
         */
        public boolean macroize;

        /**
         * Extra flags to be passed to the compiler.
         */
        public String cflags = "";
        
        /**
         * Specifies the kind of platform squawk will use.
         */
        public String platformType = null;
        
        public final static String DELEGATING = "DELEGATING";
        public final static String NATIVE     = "NATIVE";
        public final static String BARE_METAL = "BARE_METAL";
        public final static String SOCKET     = "SOCKET";
        
        public boolean isPlatformType(String type) {
            return platformType.equals(type);
        }
    }

    /**
     * Identifies the C compiler.
     */
    public final String  name;

    /**
     * The builder environment in which this compiler operates.
     */
    protected final Build env;

    /**
     * The platform on which this compiler operates.
     */
    protected final Platform platform;

    /**
     * The options controlling compilation.
     */
    public Options options;

    /**
     * Creates CCompiler instance.
     *
     * @param name  the compiler's identifier
     */
    protected CCompiler(String name, Build env, Platform platform) {
        this.name = name;
        this.env = env;
        this.platform = platform;
        //this.options = new Options();
    }
    
    /**
     * Return the name of the directory within vmcore/rts which has the os.c and other files
     * needed to compile the VM.
     * 
     * @return
     */
    public String getRtsIncludeName() {
    	return name;
    }

    /**
     * Gets the identifier for this compiler.
     *
     * @return the identifier for this compiler
     */
    public final String getName() {
        return name;
    }

    /**
     * Compiles a given source file into an object file.
     *
     * @param includeDirs  the directories to search for header files
     * @param source       the source file
     * @param dir          the output directory
     * @param disableOpts  ignores any compiler optimization flags for this compilation
     * @return the object file created
     */
    public abstract File compile(File[] includeDirs, File source, File dir, boolean disableOpts);

    /**
     * Links one or more object files into an executable or dynamic shared library.
     *
     * @param objects      the input object files for the linker
     * @param out          prefix for the created executable or library
     * @param dll          create a dynamic shared library instead of an executable
     * @return the executable or dynamic shared library created
     */
    public abstract File link(File[] objects, String out, boolean dll);

    /**
     * Creates a string of compiler switches based on {@link #options}
     *
     * @param disableOpts  ignores any compiler optimizations specified in {@link #options}
     * @return the compiler switches corresponding to {@link #options}
     */
    public abstract String options(boolean disableOpts);

    /**
     * Formats an array of directories as a set of include switches.
     *
     * @param dirs    include directories
     * @param incOpt  the switch used to prefix include directories (e.g. "-I")
     * @return the given directories formatted as a compiler switch
     */
    public final String include(File[] dirs, String incOpt) {
        StringBuffer buf = new StringBuffer(dirs.length * 10);
        for (int i = 0; i != dirs.length; ++i) {
            buf.append(incOpt).
                append(dirs[i].getPath()).
                append(' ');
        }
        return buf.toString();
    }

    /**
     * Gets the architecture that the Squawk dymanic compiler will target. This name will be used to
     * create an instance of the class <code>com.sun.squawk.compiler.</code><i>arch</i><code>Compiler</code>.
     * Note that this value is only a guess based on the C compiler being used. The '-arch:'
     * romizer flag should be used to override this if necessary.
     *
     * @return        the architecture that the Squawk dymanic compiler will target
     */
    public abstract String getArchitecture();
    
    /**
     * Returns true on any varient of x86 architectures and OSs
     *
     * @return boolean
     */
    public boolean isTargetX86Architecture() {
        String arch = getArchitecture().toLowerCase();
        return arch.equals("x86") || arch.equals("i386") || arch.equals("amd64") || arch.equals("ia64") || arch.equals("x86_64");
    }
  
    /**
     * @return true if we are cross compile to a different platform.
     */
    public boolean isCrossPlatform() {
        return false;
    }
}
