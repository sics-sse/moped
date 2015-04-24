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
package com.sun.cldc.jna;

import com.sun.squawk.Address;
import com.sun.squawk.Klass;
import com.sun.squawk.VM;
import com.sun.squawk.vm.ChannelConstants;
import java.util.Hashtable;

/**
 *
 * Provide simplified platform information
 */
public abstract class Platform {
    private final static boolean DEBUG = false;

    private final static Platform INSTANCE = makePlatform();

    public static Platform getPlatform() {
        return INSTANCE;
    }
    
    public boolean hasRuntimeExec() {
        return false;
    }

    public boolean isFreeBSD() {
        return false;
    }

    public boolean isLinux() {
        return false;
    }

    public boolean isMac() {
        return false;
    }

    public boolean isOpenBSD() {
        return false;
    }

    public boolean isSolaris() {
        return false;
    }

    public boolean isWindows() {
        return false;
    }

    public boolean isWindowsCE() {
        return false;
    }

    public boolean isX11() {
        return false;
    }

    /**
     * Get the name of the package that contains the platform classes (posix, windows, etc):
     */
    public abstract String getPlatformPackageName();

    /**
     * Get the name of the package that contains the native implementations for this platform (solaris, linux, windows, etc):
     */
    public abstract String getPlatformNativePackageName();

    protected static Hashtable commonMappings;// = new Hashtable();

    /**
     * Some platforms have wildly different names for standard libraries. Try to catch them here.
     * @todo Make extensible so other platforms don't have to modify shared class.
     * @param genericName
     * @return the base name of the library for the current platform
     */
    public static String commonLibraryMapping(String genericName) {
        return (String)commonMappings.get(genericName);
    }

    private final String platformName;
    
   /**
     * Get the name of the package that contains the native implementation for this platform:
     */
    private static String getNativePlatformName() {
        int result = VM.execSyncIO(ChannelConstants.INTERNAL_NATIVE_PLATFORM_NAME, 0, 0, 0, 0, 0, 0, null, null);
        Address r = Address.fromPrimitive(result);
        if (r.isZero()) {
            return null;
        } else {
            return Pointer.NativeUnsafeGetString(r);
        }
    }

    /**
     * Get the name of the package that contains the native implementation for this platform.
     * Name is all lower case.
     */
    public String platformName() {
        return platformName;
    }

    protected Platform() {
        commonMappings = new Hashtable();
        platformName = getNativePlatformName().toLowerCase();
    }

    public String toString() {
        return "Platform(" + platformName + ")";
    }

    public final static String PLATFORM_PACKAGE = "com.sun.cldc.jna.platform";

    private static Object getInstance(String name) {
        String fullname = PLATFORM_PACKAGE + "." + name;
        if (DEBUG) {
            VM.println("    Trying platform name: " + fullname);
        }
        Klass klass = Klass.lookupKlass(fullname);
        if (klass != null) {
            return klass.newInstance();
        }
        if (DEBUG) {
            VM.println("    Platform class not found: " + fullname);
        }
        return null;
    }

    private static Platform makePlatform() {
        if (DEBUG) {
            VM.println("Making Platform...");
        }
        Platform result = (Platform) getInstance(getNativePlatformName());
        if (result == null) {
            result = (Platform) getInstance("Posix");
        }
        if (result != null) {
            if (DEBUG) {
                VM.println("    created platform: " + result);
            }
            return result;
        }
        VM.println("Error in makePlatform for " + getNativePlatformName() + ". Exiting...");
        VM.haltVM(1);
        return null;
    }
}
