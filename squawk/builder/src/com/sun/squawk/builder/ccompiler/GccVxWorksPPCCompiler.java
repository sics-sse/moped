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
import com.sun.squawk.builder.platform.*;
import com.sun.squawk.builder.*;

/**
 * The interface to the GCC compiler on VxWorks/PPC
 */
public class GccVxWorksPPCCompiler extends GccCompiler {
    
    /** the name of the windows environment var that points to the wind river sdk*/
    final static String WINDRIVER_SDK_BASE = "WIND_BASE";
    final static String WINDRIVER_GNU_PATH = "WIND_GNU_PATH";


    private final String wind_base_path;
    private final String wind_gnu_path;


    public GccVxWorksPPCCompiler(Build env, Platform platform) {
        super("vxworks", env, platform);
        defaultSizeofPointer = 4;

        wind_base_path = System.getenv(WINDRIVER_SDK_BASE);
        if (wind_base_path == null) {
            throw new BuildException("The Windows environment var \"" + WINDRIVER_SDK_BASE + "\" is not set");
        }

        wind_gnu_path = System.getenv(WINDRIVER_GNU_PATH);
        if (wind_gnu_path == null) {
            throw new BuildException("The Windows environment var \"" + WINDRIVER_GNU_PATH + "\" is not set");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String options(boolean disableOpts) {
        StringBuffer sb = new StringBuffer(super.options(disableOpts));
        // -ansi disables '//' comments, which we use, so don't use -ansi
        sb.append(" -mcpu=603 -mstrict-align -mno-implicit-fp -mlongcall -DCPU=PPC603 -DTOOL_FAMILY=gnu -DTOOL=gnu -D_WRS_KERNEL -DVXWORKS ");
                
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getArchitecture() {
        return "PPC";
    }

    /**
     * {@inheritDoc}
     *
     * CC_ARCH_SPEC = -mcpu=603 -mstrict-align -mno-implicit-fp -mlongcall
     * LIBPATH =
     * LIBS =
     *
     * IDE_INCLUDES = -I$(WIND_BASE)/target/h -I$(WIND_BASE)/target/h/WPILib -I$(WIND_BASE)/target/h/wrn/coreip
     *
     * IDE_LIBRARIES = $(WIND_BASE)/target/lib/WPILib.a
     *
     * DEBUGFLAGS_C-Compiler = -O2 -fstrength-reduce -fno-builtin
     * 
     */
    @Override
    public File compile(File[] includeDirs, File source, File dir, boolean disableOpts) {
        File object = new File(dir, source.getName().replaceAll("\\.c", "\\.o"));

        /* WARNING: Things are really weird in vxworks/windriver.
         * It seems that if ANY -I include dirs are specified,
         * gcc eventually gets confused and refuses to find a header in one of the standard dirs.
         *
         * EXAMPLE ERROR:
         * In file included from C:\WindRiver\vxworks-6.3/target/h/types/vxANSI.h:55,
         *        from C:\WindRiver\vxworks-6.3/target/h/stdio.h:60,
         *        from vmcore\src\vm/platform.h:26,
         *        from vmcore\src\vm\fp\e_rem_pio2.c:31:
         * C:\WindRiver\vxworks-6.3/target/h/types/vxArch.h:161:30: vmcore\src\vm/arch/ppc/archPpc.h: Invalid argument
         *
         * The solution is to disable all standard includes with -nostdinc, and re-specify them as -I options.
         * TIP: You can use the -v option to see the search paths used.
         */
        File[] newIncludes = new File[] {
            new File(wind_base_path + "/target/h"),
            new File(wind_base_path + "/target/h/wrn/coreip"), // For networking
            new File(wind_gnu_path  + "/x86-win32/lib/gcc/powerpc-wrs-vxworks/3.4.4/include"),
            new File(wind_base_path + "/target/h/WPILib")
        };

        env.exec("ccppc -c " + 
                 options(disableOpts) + " " + "-nostdinc " + 
                 include(newIncludes, "-I") +
                 include(includeDirs, "-I") +
                 " -o \"" + object + "\" \"" + source + "\"");
        return object;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public File link(File[] objects, String out, boolean dll) {
        String output;
        String exec;

        String ccName = "ccppc";
        
        File[] newObjects = new File[objects.length];

        for(int f = 0; f < objects.length; f++) {
            newObjects[f] = objects[f];
        }

        output = out + platform.getExecutableExtension();
        //exec = "--gc-sections -o " + output + " " + Build.join(newObjects);
        exec = "-o " + output + " " + Build.join(newObjects);
        
        // TODO: Generate the ctdt.o from the object and link it into the .out file.
        //       Without this, static variables will not be initialized.
        env.exec(ccName + " -nostdlib -r -Wl,-X " + exec);

        return new File(output);
    }

    @Override
    public String getLinkSuffix() {
        throw new RuntimeException("not used in this config.");
    }

    @Override
    public String getSharedLibrarySwitch() {
        throw new RuntimeException("not used in this config.");
    }

    @Override
    protected int getDefaultSizeofPointer() {
        return 4;
		//throw new RuntimeException("not used in this config.");
    }

    
    @Override
    public boolean isCrossPlatform() {
        return true;
    }
}
