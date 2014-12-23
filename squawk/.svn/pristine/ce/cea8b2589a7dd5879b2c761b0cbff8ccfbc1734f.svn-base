//if[!AUTOGEN_JNA_NATIVES]
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

/* This is a HAND-GENERATED INTERFACE to  <sys/ioctl.h>
 */

package com.sun.squawk.platform.posix.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public abstract class IoctlImpl implements Ioctl {

    /*----------------------------- defines -----------------------------*/


    /*----------------------------- methods -----------------------------*/
    private final Function ioctlPtr;
    
    public int ioctl(int arg0, int arg1, int arg2, int arg3, int arg4) {
        int result0 = ioctlPtr.call5(arg0, arg1, arg2, arg3, arg4);
        int result = (int)result0;
        return result;
    }

    public int ioctl(int arg0, int arg1, IntByReference arg2) {
       int result0 = ioctlPtr.call3(arg0, arg1, arg2.getPointer());
        int result = (int)result0;
        return result;
    }

    public int ioctl(int arg0, int arg1, int arg2, int arg3) {
        int result0 = ioctlPtr.call4(arg0, arg1, arg2, arg3);
        int result = (int)result0;
        return result;
    }
    
    public int ioctl(int arg0, int arg1, int arg2) {
        int result0 = ioctlPtr.call3(arg0, arg1, arg2);
        int result = (int)result0;
        return result;
    }
    
    public IoctlImpl() {
        NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();
        ioctlPtr = jnaNativeLibrary.getFunction("ioctl");
    }
    
}


