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

package com.sun.squawk.platform;

import com.sun.squawk.Klass;
import com.sun.squawk.VM;

/**
 *
 * This class provides access to the platform-specific implementations of various features.
 */
public class Platform {
    public final static boolean DEBUG = false;

    /**
     * Basic kinds of PLATFORM_TYPE, as defined in build.properties
     */
    public final static int BARE_METAL = 0;
    public final static int DELEGATING = 1;
    public final static int NATIVE = 2;
    public final static int SOCKET = 3;

    public final static boolean IS_BARE_METAL = (/*VAL*/999/*PLATFORM_TYPE*/ == BARE_METAL);
    public final static boolean IS_DELEGATING = (/*VAL*/999/*PLATFORM_TYPE*/ == DELEGATING);
    public final static boolean IS_NATIVE = (/*VAL*/999/*PLATFORM_TYPE*/ == NATIVE);
    public final static boolean IS_SOCKET = (/*VAL*/999/*PLATFORM_TYPE*/ == SOCKET);

    private static GCFSockets gcfSockets;
    private static GCFFile gcfFile;

    private static String NATIVE_PLATFORM_NAME;
    
    private Platform() { }

    public static String getPlatformName() {
        if (NATIVE_PLATFORM_NAME == null) {
            NATIVE_PLATFORM_NAME = com.sun.cldc.jna.Platform.getPlatform().getPlatformPackageName();
        }
        return NATIVE_PLATFORM_NAME;
    }

    /**
     * Create an instance of the class named NATIVE_PLATFORM_NAME . name.
     * If the class can't be found, halt the VM.
     * @param name the leaf name of the class.
     * @return an instance of the class
     */
    private static Object getPlatformInstance(String name) {
        Exception e = null;
        String fullname = getPlatformName() + "." + name;
        if (DEBUG) { VM.println("looking for class " + fullname); }
        Klass klass = Klass.lookupKlass(fullname);
        if (klass != null) {
            if (DEBUG) { VM.println("    found class"); }
            Object result = klass.newInstance();
            if (DEBUG) { VM.println("    got instance"); }
            return result;
        }
        VM.println("Error loading platform " + fullname + "\n   " + e);
        VM.stopVM(1);
        return null;
    }

    public static synchronized GCFSockets getGCFSockets() {
        if (IS_NATIVE) {
            if (gcfSockets == null) {
                gcfSockets = (GCFSockets) getPlatformInstance("GCFSocketsImpl");
            }
            return gcfSockets;
        } else {
            return null;
        }
    }
    
    /**
     * Create the correct kind of SystemEvents Handler, or null if none needed.
     * 
     * @return
     */
    public static SystemEvents createSystemEvents() {
        if (IS_NATIVE) {
            return (SystemEvents) getPlatformInstance("SystemEventsImpl");
        } else {
            return null;
        }
    }

    public static synchronized GCFFile getFileHandler() {
        if (IS_NATIVE) {
            if (gcfFile == null) {
                gcfFile = (GCFFile) getPlatformInstance("GCFFileImpl");
            }
            return gcfFile;
        } else {
            return null;
        }
    }
    
}
