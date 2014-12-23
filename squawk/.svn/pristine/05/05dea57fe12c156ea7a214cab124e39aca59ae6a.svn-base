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

import com.sun.squawk.util.Assert;
import com.sun.squawk.compiler.Compiler;

/**
 * X86-specific abstract data that supports the <code>Compiler</code> interface.
 *
 */
abstract class AbstractX86Compiler implements Compiler {

    /**
     * @see InterpCompiler
     */
    public int getFramePointerByteOffset(int fp_value) {
        switch (fp_value) {
            case com.sun.squawk.vm.FP.parm0:        return  8;
            case com.sun.squawk.vm.FP.returnIP:     return  4;
            case com.sun.squawk.vm.FP.returnFP:     return  0;
            case com.sun.squawk.vm.FP.local0:       return -4;
        }
        Assert.shouldNotReachHere();
        return 0;
    }

    /**
     * @see Compiler
     */
    public boolean isBigEndian() {
        return false;
    }

    /**
     * @see Compiler
     */
    public boolean tableSwitchPadding() {
        return false;
    }

    /**
     * @see Compiler
     */
    public boolean tableSwitchEndPadding() {
        return false;
    }

}
