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

import java.io.File;
import java.io.PrintStream;

import com.sun.squawk.builder.*;

/**
 * An abstraction of Unix OS's.
 */
public abstract class Unix extends Platform {

    public Unix(Build env) {
        super(env);
    }

    /**
     * {@inheritDoc}
     */
    public String getExecutableExtension() {
        return "";
    }

    /**
     * Finds the directory within the JDK containing a given shared library.
     *
     * @param name   the name of the shared library to search for
     * @return  the path to the directory containing <code>name</code>
     */
    private String findJavaSharedLibraryDir(String name) {
        File so = Build.find(env.getJDK().getHome(), name, false);
        if (so== null) {
            throw new BuildException("could not find '" + name + "'");
        }
        return so.getParentFile().getPath();
    }

    /**
     * {@inheritDoc}
     */
    public String getJVMLibraryPath() {
        return findJavaSharedLibraryDir("libjvm.so") + ":" + findJavaSharedLibraryDir("libverify.so");
    }

    /**
     * {@inheritDoc}
     */
    public void showJNIEnvironmentMessage(PrintStream out) {
        try {
            String path = getJVMLibraryPath();
            out.println();
            out.println("To configure the environment for Squawk, try the following command under bash:");
            out.println();
            out.println("    export LD_LIBRARY_PATH=" + path + ":$LD_LIBRARY_PATH");
            out.println();
            out.println("or in csh/tcsh");
            out.println();
            out.println("    setenv LD_LIBRARY_PATH " + path + ":$LD_LIBRARY_PATH");
            out.println();
        } catch (BuildException e) {
            out.println();
            out.println("The LD_LIBRARY_PATH environment variable must be set to include the directories");
            out.println("containing 'libjvm.so' and 'libverify.so'.");
            out.println();
        }
    }
}
