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

import com.sun.squawk.builder.platform.*;

import com.sun.squawk.builder.*;

/**
 * The interface to the GCC compiler on Mac OS X.
 */
public class GccMacOSXCompiler extends GccCompiler {
	public static final String RTS_INCLUDE_NAME = "gcc-macosx";

    public GccMacOSXCompiler(Build env, Platform platform) {
        this(RTS_INCLUDE_NAME, env, platform);
    }

    protected GccMacOSXCompiler(String name, Build env, Platform platform) {
        super(name, env, platform);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getLinkSuffix() {
        String suffix = " " + get64BitOption() + " -framework CoreFoundation -dead-strip " + getArchOptions() + " ";
        if (options.isPlatformType(Options.DELEGATING)) {
            suffix = suffix + " -framework JavaVM";
        }
        return suffix;
    }

    /**
     * {@inheritDoc}
     */
    public String getSharedLibrarySwitch() {
        return "-dynamiclib -single_module";
    }

    /**
     * {@inheritDoc}
     */
    public String getArchitecture() {
        return "PPC";
    }

    public String getArchOptions() {
        return "-arch ppc ";
    }

    /**
     * {@inheritDoc}
     */
    public String options(boolean disableOpts) {
        String result = super.options(disableOpts);
        result += getArchOptions() + "-mdynamic-no-pic -fvisibility=hidden ";
        return result;
    }

}
