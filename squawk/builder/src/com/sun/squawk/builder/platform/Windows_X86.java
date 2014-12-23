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

package com.sun.squawk.builder.platform;

import java.io.*;
import com.sun.squawk.builder.*;
import com.sun.squawk.builder.ccompiler.*;

/**
 * This class represents Windows on an x86 or amd64 processor.
 */
public final class Windows_X86 extends Platform {

    public Windows_X86(Build env) {
        super(env);
    }

    /**
     * {@inheritDoc}
     */
    public String getExecutableExtension() {
        return ".exe";
    }

    /**
     * {@inheritDoc}
     */
    public File getToolsDir() {
        return new File("tools/windows-x86");
    }

    /**
     * {@inheritDoc}
     */
    public String getJVMLibraryPath() {
        File dll = Build.find(env.getJDK().getHome(), "jvm.dll", false);
        if (dll == null) {
            throw new BuildException("could not find 'jvm.dll'");
        }
        return dll.getPath();
    }

    /**
     * {@inheritDoc}
     */
    public void showJNIEnvironmentMessage(PrintStream out) {
        String env = getJVMLibraryPath();
        if (env != null) {
            out.println();
            out.println("To configure the environment for Squawk, try the following command:");
            out.println();
            out.println("    set JVMDLL="+env);
            out.println();
        } else {
            out.println();
            out.println("The JVMDLL environment variable must be set to the full path of 'jvm.dll'.");
            out.println();
        }
    }

    /**
     * {@inheritDoc}
     */
    public CCompiler createDefaultCCompiler() {
        return new MscCompiler(env, this);
    }
}
