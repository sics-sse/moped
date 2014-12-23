//if[DEBUG_CODE_ENABLED]
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

package com.sun.squawk.test;

/**
 * This tests that the translator correctly handles the case of
 * assignment to an interface type where the derived type does
 * not directly implement the interface type.
 */
public class VerifyUseOfInterface extends Base {

    private static Interface sIface;
    private Interface vIface;

    private void doIt() {
    }

    static void main(String[] args) {

        // call private method with null receiver
        VerifyUseOfInterface vuoi = null;
        try {
            vuoi.doIt();
            throw new RuntimeException("Expected NullPointerException");
        } catch (NullPointerException npe) {
            // ok
            vuoi = new VerifyUseOfInterface();
        }

        Interface iface = new SubClass();
        // derived type of 'iface' is now 'SubClass'

        if (args == null) {
            iface = new AnotherSubClass();
            // derived type of 'iface' on this branch is now 'AnotherSubClass'
        }

        // at this merge point, derived type of 'iface' is now Base which does
        // not directly imnplement 'Interface'

        // translator/verifier should treat derived type of 'iface' as
        // 'java.lang.Object' for the following statements
        vuoi.vIface = iface;
        sIface = iface;
        int it = iface.getIt();
        iface = null;
        useInterface(iface);
    }

    static void useInterface(Interface iface) {
    }
}

interface Interface {
    int getIt();
}

class Base {
    public Base() {
    }
}

class SubClass extends Base implements Interface {
    public int getIt() {
        return 1;
    }
}

class AnotherSubClass extends Base implements Interface {
    public int getIt() {
        return 1;
    }
}
