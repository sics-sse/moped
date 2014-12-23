//if[FINALIZATION]
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

package com.sun.squawk;

public class FinalizerTest extends FinalizerTestParent {

    public static void main(String[] args) {
        System.out.println("Running: com.sun.squawk.FinalizerTest");
        System.gc();
        createObjects();
        while (count > 0) {
            System.out.println("Calling gc()");
            System.gc();
            VMThread.yield();
        }
        System.out.println("FinalizerTest done");
    }

    static void createObjects() {
        System.out.println("Creating "+new FinalizerTest());
        System.out.println("Creating "+new FinalizerTest());
        System.out.println("Creating "+new FinalizerTest());
        System.out.println("Creating "+new FinalizerTest());
        System.out.println("Creating "+new FinalizerTest());
    }
}


/*
 * Put finalizer in parent class to test that the finalize() method is inherited correctly.
 */
class FinalizerTestParent {
    static int count = 5;
    protected void finalize() {
        System.out.println("finalize() called for "+this);
        --count;
    }
}
