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
 * This class represents Mac OS X on the PowerPC processor.
 */
public class MacOSX_PPC extends Unix {

    public MacOSX_PPC(Build env) {
        super(env);
    }

    /**
     * {@inheritDoc}
     */
    public File getToolsDir() {
        return new File("tools", "macosx-ppc");
    }

    /**
     * {@inheritDoc}
     */
    public void showJNIEnvironmentMessage(PrintStream out) {
        out.println();
        out.println("There is no need to configure the environment for Squawk on Mac OS X/PPC");
        out.println("as the location of the JavaVM framework is built into the executable.");
        out.println();
    }

    /**
     * {@inheritDoc}
     */
    public CCompiler createDefaultCCompiler() {
        return new GccMacOSXCompiler(env, this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBigEndian() {
        return true;
    }

    public boolean isMacOsX() {
        return false;
    }

}
