/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.vm;

import com.sun.squawk.GC;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.pragma.AllowInlinedPragma;
import com.sun.squawk.util.Assert;

/**
 * This class contains the offsets that define the layout of the array
 * (of type "[-global-") that holds the class state (i.e. static variables/globals) for a class.
 */
public class CS {

    /**
     * The index of the pointer to the class to which the variables pertain.
     */
    public final static int klass = 0;

    /**
     * The index of the pointer to the next class state record.
     */
    public final static int next = 1;

    /**
     * The index of the first static variable.
     */
    public final static int firstVariable = 2;
    
    private final static boolean DEBUG =  /*VAL*/false/*DEBUG_CODE_ENABLED*/;
    
    private CS() {}
    
    /**
     * Give error if cs is not really global array.
     */
    public static void check(Object cs) throws AllowInlinedPragma {
        if (DEBUG) {
            Assert.always(cs != null);
            if (GC.isSafeToSwitchThreads()) {
                Assert.always(GC.getKlass(cs).getSystemID() == CID.GLOBAL_ARRAY);
                if  (NativeUnsafe.getObject(cs, CS.klass) != null) {
                    Assert.always(GC.getKlass(NativeUnsafe.getObject(cs, CS.klass) ).getSystemID() == CID.KLASS);
                }
                if  (NativeUnsafe.getObject(cs, CS.next) != null) {
                    Assert.always(GC.getKlass(NativeUnsafe.getObject(cs, CS.next) ).getSystemID() == CID.GLOBAL_ARRAY);
                }
            }
        }
    }

}
