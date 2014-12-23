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

import java.io.File;

import com.sun.squawk.builder.*;
import com.sun.squawk.builder.ccompiler.*;

/**
 * This class represents Solaris on an X86 processor.
 * 
 *
 */
public class SunOS_X86 extends Unix {

    public SunOS_X86(Build env) {
        super(env);
    }

    /**
     * {@inheritDoc}
     */
    public File getToolsDir() {
        return new File("tools", "sunos-x86");
    }

    /**
     * {@inheritDoc}
     */
    public CCompiler createDefaultCCompiler() {
        return new GccCompiler(env, this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isBigEndian() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean allowUnalignedLoads() {
        return true;
    }

}
