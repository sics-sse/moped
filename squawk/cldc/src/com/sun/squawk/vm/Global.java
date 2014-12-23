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

import com.sun.squawk.VM;
import com.sun.squawk.pragma.HostedPragma;
import java.util.Hashtable;

/**
 * Definition of all the Squawk classes that use global variables.
 * <p>
 * The Squawk VM supports four types of variables. These are local
 * variables, instance variables, static variables, and global variables.
 * Static variables are those defined in a class using the static keyword
 * these are allocated dynamically by the VM when their classes are
 * initialized, and these variables are created on a per-isolate basis.
 * Global variables are allocated by the romizer and used in place of
 * the static variables in this hard-wired set of system classes. This is
 * done in cases where certain components of the system must have static
 * state before the normal system that support things like static variables
 * are running. Global variables are shared between all isolates.
 *
 * This class is only used by the romize process, not used at runtime.
 */
public final class Global {
    
    /**
     * Purely static class should not be instantiated.
     */
    private Global() {}

    /**
     * The tables of int, address, and reference globals.
     */
    private static Hashtable intGlobals,
                             addrGlobals,
                             oopGlobals;

    static {
        if (VM.isHosted()) {
            initGlobals();
        }
    }

    /**
     * Fields specified here will be allocated constant offsets.
     */
    static void initGlobals() throws HostedPragma {
        intGlobals = new Hashtable();
        addrGlobals = new Hashtable();
        oopGlobals = new Hashtable();

        Oop("com.sun.squawk.VM.currentIsolate");
        Int("com.sun.squawk.VM.extendsEnabled");
        Int("com.sun.squawk.VM.usingTypeMap");

        Int("com.sun.squawk.GC.traceFlags");
        Int("com.sun.squawk.GC.collecting");
        Int("com.sun.squawk.GC.monitorExitCount");
        Int("com.sun.squawk.GC.monitorReleaseCount");
        Int("com.sun.squawk.GC.newCount");
        Int("com.sun.squawk.GC.newHits");

        Int("com.sun.squawk.VMThread.nextThreadNumber");
        Oop("com.sun.squawk.VMThread.currentThread");
        Oop("com.sun.squawk.VMThread.otherThread");
        Oop("com.sun.squawk.VMThread.serviceThread");

        Oop("com.sun.squawk.ServiceOperation.pendingException");
        Int("com.sun.squawk.ServiceOperation.code");
        Int("com.sun.squawk.ServiceOperation.context");
        Int("com.sun.squawk.ServiceOperation.op");
        Int("com.sun.squawk.ServiceOperation.channel");
        Int("com.sun.squawk.ServiceOperation.i1");
        Int("com.sun.squawk.ServiceOperation.i2");
        Int("com.sun.squawk.ServiceOperation.i3");
        Int("com.sun.squawk.ServiceOperation.i4");
        Int("com.sun.squawk.ServiceOperation.i5");
        Int("com.sun.squawk.ServiceOperation.i6");
        Add("com.sun.squawk.ServiceOperation.o1");
        Add("com.sun.squawk.ServiceOperation.o2");
        Int("com.sun.squawk.ServiceOperation.result");
        Add("com.sun.squawk.ServiceOperation.addressResult");

        Int("branchCountHigh");
        Int("branchCountLow");
        Int("traceStartHigh");
        Int("traceStartLow");
        Int("traceEndHigh");
        Int("traceEndLow");
        Int("com.sun.squawk.VM.tracing");
        Int("runningOnServiceThread");
        Int("currentThreadID");
/*if[GC_com.sun.squawk.CheneyCollector]*/
        Add("cheneyStartMemoryProtect");
        Add("cheneyEndMemoryProtect");
/*end[GC_com.sun.squawk.CheneyCollector]*/
    }

    /**
     * Add a global int.
     *
     * @param name the field name
     * @return the field constant
     */
    private static int Int(String name) {
        int index = intGlobals.size();
        intGlobals.put(name, new Integer(index));
        return index;
    }

    /**
     * Add a global address.
     *
     * @param name the field name
     * @return the field constant
     */
    private static int Add(String name) {
        int index = addrGlobals.size();
        addrGlobals.put(name, new Integer(index));
        return index;
    }

    /**
     * Add a global oop reference.
     *
     * @param name the field name
     * @return the field constant
     */
    private static int Oop(String name) {
        int index = oopGlobals.size();
        oopGlobals.put(name, new Integer(index));
        return index;
    }

    /**
     * Get the hashtable of global ints.
     *
     * @return the hashtable
     */
    public static Hashtable getGlobalInts() {
        return intGlobals;
    }

    /**
     * Get the hashtable of global addresses.
     *
     * @return the hashtable
     */
    public static Hashtable getGlobalAddrs() {
        return addrGlobals;
    }

    /**
     * Get the hashtable of global oops.
     *
     * @return the hashtable
     */
    public static Hashtable getGlobalOops() {
        return oopGlobals;
    }

}
