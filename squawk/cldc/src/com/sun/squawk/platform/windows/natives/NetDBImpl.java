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

/* **** HAND-GENERATED FILE
 */

package com.sun.squawk.platform.windows.natives;

import com.sun.cldc.jna.*;
import com.sun.cldc.jna.ptr.*;

public class NetDBImpl implements NetDB {

    /*----------------------------- defines -----------------------------*/

    public final static int HOST_NOT_FOUND = 1;
    public final static int TRY_AGAIN = 2;
    public final static int NO_RECOVERY = 3;
    public final static int NO_DATA = 4;

        public int initConstInt(int index) {
        final int[] dummy = {};
        return dummy[index];
    }

    /*----------------------------- variables -----------------------------*/

    private final Pointer h_errnoPtr;
    
    public int h_errno() {
        return h_errnoPtr.getInt(0);
    }


    /*----------------------------- methods -----------------------------*/
    private final Function gethostbynamePtr;
    
    public hostent gethostbyname(String arg0) {
        Pointer var0 = Pointer.createStringBuffer(arg0);
        int result0 = gethostbynamePtr.call1(var0);
        hostent result = (hostent)Function.returnStruct(hostent.class, result0);
        var0.free();
        return result;
    }
    
    public NetDBImpl() {
        NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();
        h_errnoPtr = jnaNativeLibrary.getGlobalVariableAddress("h_errno", 4);
        gethostbynamePtr = jnaNativeLibrary.getFunction("gethostbyname");
    }
    
    public static abstract class hostentImpl extends Structure {
    
        protected hostentImpl() {}

        public int size() {
            return 20;
        }

    }
    

}


