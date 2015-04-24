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

import com.sun.squawk.builder.*;
import com.sun.squawk.builder.platform.*;
import java.io.*;
import java.util.Iterator;
import java.util.Properties;

/**
 * The interface for the "gcc" compiler.
 */
public class GccAutosarCompiler extends GccCompiler {
    
    boolean allowGCSections;

    public GccAutosarCompiler(String name, Build env, Platform platform) {
        super(name, env, platform);
        allowGCSections = !(platform.isMacOsX() || platform.getHostOsName().toLowerCase().startsWith("sunos"));
    }

    public GccAutosarCompiler(Build env, Platform platform) {
        this("gcc-rpi", env, platform);
    }

    /**
     * {@inheritDoc}
     */
    public String options(boolean disableOpts) {
    	String opts = super.options(disableOpts);
    	String flashMemOpt = env.getProperty("FLASH_MEMORY");
    	if (flashMemOpt != null && flashMemOpt.trim().equals("true")) {
    		opts += " -DFLASH_MEMORY";
    	}
    	opts += " -DAUTOSAR";
    	return opts;
    }

    protected int defaultSizeofPointer = -1;

    /**
     * Compiles a small C program to determine the default pointer size of this version of gcc.
     *
     * @return  the size (in bytes) of a pointer compiled by this version of gcc
     */
    protected int getDefaultSizeofPointer() {
        if (defaultSizeofPointer == -1) {
            try {
                File cFile = File.createTempFile("sizeofpointer", ".c");
                PrintStream out = new PrintStream(new FileOutputStream(cFile));
                out.println("#include <stdlib.h>");
                out.println("int main (int argc, char **argv) {");
                out.println("    exit(sizeof(char *));");
                out.println("}");
                out.close();

                String exePath = cFile.getPath();
                File exe = new File(exePath.substring(0, exePath.length() - 2));

                env.exec("arm-none-eabi-gcc -o " + exe.getPath() + " " + cFile.getPath());
                cFile.delete();

                try {
                    env.exec(exe.getPath());
                } catch (BuildException e) {
                    exe.delete();
                    return defaultSizeofPointer = e.exitValue;
                }
                throw new BuildException("gcc pointer size test returned 0");
            } catch (IOException ioe) {
                throw new BuildException("could run pointer size gcc test", ioe);
            }
        }
        return defaultSizeofPointer;
    }

    /**
     * Gets the compiler option for specifying the word size of the target architecture.
     *
     * @return word size compiler option
     */
    public String get64BitOption() {
//        int pointerSize = getDefaultSizeofPointer();
//        if (options.is64) {
//            return pointerSize == 8 ? "" : "-m64 ";
//        } else {
//            return pointerSize == 4 ? "" : "-m32 ";
//        }
    	return "";
    }

    /**
     * Gets the linkage options that must come after the input object files.
     *
     * @return the linkage options that must come after the input object files
     */
    public String getLinkSuffix() {
        String suffix = "";//" " + get64BitOption();
//        if (options.isPlatformType(Options.DELEGATING)) {
//            String jvmLib = env.getPlatform().getJVMLibraryPath();
//            suffix = suffix + " -L" + jvmLib.replaceAll(File.pathSeparator, " -L") + " -ljvm";
//        } else if (options.isPlatformType(Options.SOCKET) || options.isPlatformType(Options.NATIVE)) {
//            if (platform.getName().toLowerCase().startsWith("linux")) {
//                suffix = suffix +  " -lnsl -lpthread";
//            } else {
//                suffix = suffix + " -lsocket" + " -lnsl";
//            }
//        }
//
        if (options.kernel && options.hosted) {
            /* Hosted by HotSpot and so need to interpose on signal handling. */
            suffix = suffix + " -ljsig";
        }
        
        suffix += " -lgcc -lc -lm";

//        if (options.floatsSupported) {
//            suffix += " -ldl -lm" + suffix;
//        } else {
//            suffix += " -ldl" + suffix;
//        }
        
        return suffix;
    }

    /**
     * Gets the platform-dependant gcc switch used to produce a shared library.
     *
     * @return the platform-dependant gcc switch used to produce a shared library
     */
    public String getSharedLibrarySwitch() {
        return "-shared";
    }

    /**
     * {@inheritDoc}
     */
    public File compile(File[] includeDirs, File source, File dir, boolean disableOpts) {
        File object = new File(dir, source.getName().replaceAll("\\.c", "\\.o"));
        env.exec("arm-none-eabi-gcc -c " +
                 options(disableOpts) + " " +
                 include(includeDirs, "-I") +
                 " -o " + object + " " + source);
        return object;
    }

    /**
     * {@inheritDoc}
     */
    public File link(File[] objects, String out, boolean dll) {
        String output;
        String exec;

        if (dll) {
            output = System.mapLibraryName(out);
            exec = "-o " + output + " " + getSharedLibrarySwitch();
        } else {
        	output = out + platform.getExecutableExtension();
            exec = "-o " + output;
            if (allowGCSections) {
                exec = "-Wl,--gc-sections " + exec;
            }
        }
        exec += " " + Build.join(objects) + " " + getLinkSuffix();
        env.exec("arm-none-eabi-gcc " + exec);
        return new File(output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getArchitecture() {
        return "arm";
    }

    /**
     * Use more Java-friendly SSE2 FP instructions instead of x87.
     * SSE2 defined for P4 and newer CPUs, including Atom.
     * @return boolean
     */
    public boolean useSSE2Math() {
        return true;
    }

    @Override
    public boolean isCrossPlatform() {
        return true;
    }
}
