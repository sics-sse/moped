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

/* **** HAND_GENERATED FILE 
 */

package com.sun.squawk.platform.windows.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public class SelectImpl implements Select {

    /*----------------------------- defines -----------------------------*/

    public final static int FD_SETSIZE = 64;
    // we're having some problems with class initialization being required for thread initialization!
    public final static int fd_set_SIZEOF = 260;//Native.getLibraryLoading().getGlobalVariableAddress("sysFD_SIZE", 4).getInt(0);

    public int initConstInt(int index) {
        final int[] dummy = {};
        return dummy[index];
    }

    /*----------------------------- methods -----------------------------*/
//    private final Function selectPtr;
//
//    public int select(int arg0, Pointer arg1, Pointer arg2, Pointer arg3, Pointer arg4) {
//        int result0 = selectPtr.call5(arg0, arg1, arg2, arg3, arg4);
//        int result = (int)result0;
//        return result;
//    }
    
    private final Function FD_ISSETPtr;
    
    public boolean FD_ISSET(int arg0, Pointer arg1) {
        int result0 = FD_ISSETPtr.call2(arg0, arg1);
        boolean result = (result0 == 0) ? false : true;
        return result;
    }
    
    private final Function FD_CLRPtr;
    
    public void FD_CLR(int arg0, Pointer arg1) {
        FD_CLRPtr.call2(arg0, arg1);
    }
    
    private final Function FD_SETPtr;
    
    public void FD_SET(int arg0, Pointer arg1) {
        FD_SETPtr.call2(arg0, arg1);
    }
    
    public SelectImpl() {
        NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();
//        selectPtr = jnaNativeLibrary.getFunction("select");
        FD_ISSETPtr = jnaNativeLibrary.getFunction("sysFD_ISSET");
        FD_CLRPtr = jnaNativeLibrary.getFunction("sysFD_CLR");
        FD_SETPtr = jnaNativeLibrary.getFunction("sysFD_SET");
    }

}


