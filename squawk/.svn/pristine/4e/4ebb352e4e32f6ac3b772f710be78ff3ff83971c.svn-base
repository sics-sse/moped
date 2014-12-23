/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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
 * This class represents MacOSX on an X86 processor.
 * 
 *
 */
public class MacOSX_X86 extends Unix {

    public MacOSX_X86(Build env) {
        super(env);
    }

    /**
     * {@inheritDoc}
     */
    public File getToolsDir() {
        return new File("tools", "macosx-x86");
    }

    /**
     * {@inheritDoc}
     */
    public CCompiler createDefaultCCompiler() {
        if (env.isWantingPpcCompilerOnMac()) {
            return new GccMacOSXCompiler(env, this);
        }
        return new GccMacOSXX86Compiler(env, this);
    }

    /**
     * {@inheritDoc}
     */
    public void showJNIEnvironmentMessage(PrintStream out) {
        out.println();
        out.println("There is no need to configure the environment for Squawk on Mac OS X/X86");
        out.println("as the location of the JavaVM framework is built into the executable.");
        out.println();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBigEndian() {
        return false;
    }

    public boolean isMacOsX() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean allowUnalignedLoads() {
        // unaligned loads results in 14% faster code, and 3% smaller exe
        return true;
    }

}
