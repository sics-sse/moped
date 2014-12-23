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

package com.sun.cldc.jna;

import com.sun.squawk.Address;
import com.sun.squawk.VM;
import com.sun.squawk.vm.ChannelConstants;

/**
 * Represents a handle to a runtime library (for example, as returned by 
 * the POSIX function "dlopen" http://www.opengroup.org/onlinepubs/009695399/functions/dlopen.html
 * 
 * <h3>Differences from JNA</h3>
 * <ul>
 * <li> Throws RuntimeExceptions instead of UnsatisfiedLinkErrors. Are link errors really "unrecoverable"? Platform independant code might want to work around missing functions.
 * <li> Search paths unimplemented
 * <li> Calling conventions unimplemented
 * <li> no finalization in cldc, need to call dispose() explicitly (could add a shutdownhook though).
 * <li> no parseVersion();
 * <li> no getFile()
 * </ul>
 */
public class NativeLibrary {

    private final static boolean DEBUG = false;
    

    private final static NativeLibrary RTLD_DEFAULT = new NativeLibrary("RTLD_DEFAULT", Address.zero());
    
    private final String name;
    
    /**
     * Note that we encode RTLD_DEFAULT as having a ptr value of NULL. The C code will translate this to the correct value for RTLD_DEFAULT.
     */
    private final Address ptr;
    
    private boolean closed;

    private NativeLibrary(String name, Address ptr) {
        this.name = name;
        this.ptr = ptr;
    }
    
    private static String nativeLibraryName(String baseName) {
        Platform platform = Platform.getPlatform();
        if (platform.isSolaris() || platform.isLinux()) {
            return "lib" + baseName + ".so";
        } else if (platform.isMac()) {
            return "lib" + baseName + ".dylib";
        } else if (platform.isWindows()) {
            return baseName;// + ".dll";
        } else {
            return baseName;
        }
    }

    /**
     * getFunction a symbol's address by name (warpper around dlsym)
     * @param name
     * @return Address of symbol
     */
    private Address getSymbolAddress(String name) {
        if (closed) {
            throw new IllegalStateException("closed");
        }
        if (DEBUG) {
            VM.print("Calling DLSYM on ");
            VM.println(name);
        }
        Pointer name0 = Pointer.createStringBuffer(name);
        int result = VM.execSyncIO(ChannelConstants.DLSYM, ptr.toUWord().toInt(), name0.address().toUWord().toInt(), 0, 0, 0, 0, null, null);
        name0.free();
        return Address.fromPrimitive(result);
    }
    
    /**
     * Dynamically look up a native function by name.
     *
     * WARNING: Do NOT use for calling C functions that may block (or take a "long" time).
     * Use the "blocking" version instead.
     * 
     * Look up the symbol in the specified library
     * 
     * @param funcName
     * @return an object that can be used to call the named function
     * @throws RuntimeException if there is no function by that name.
     */
    public Function getFunction(String funcName) {
        Address result = getFunction0(funcName);
        return new Function(funcName, result);
    }

/*if[!PLATFORM_TYPE_BARE_METAL]*/

    /**
     * Dynamically look up a blocking native function by name.
     *
     * Look up the symbol in the specified library
     *
     * @param funcName
     * @return an object that can be used to call the named function
     * @throws RuntimeException if there is no function by that name.
     */
    public BlockingFunction getBlockingFunction(String funcName) {
        Address result = getFunction0(funcName);
        return new BlockingFunction(funcName, result);
    }
/*end[PLATFORM_TYPE_BARE_METAL]*/

    /**
     * Dynamically look up a native function address by name.
     * Look up the symbol in the specified library
     *
     * @param funcName
     * @return address of the function
     * @throws RuntimeException if there is no function by that name.
     */
    private Address getFunction0(String funcName) {
        Address result = getSymbolAddress(funcName);
        if (DEBUG) {
            VM.print("Function Lookup for ");
            VM.print(funcName);
            VM.print(" = ");
            VM.printAddress(result);
            VM.println();
        }
        if (result.isZero()) {
            if (Platform.getPlatform().isWindows()) {
                if (funcName.charAt(funcName.length() - 1) != 'A') {
                    return getFunction0(funcName + 'A');
                }
            } else if (funcName.charAt(0) != '_') {
                return getFunction0("_" + funcName);
            }
            throw new RuntimeException("Can't find native symbol " + funcName + ". OS Error: " + errorStr());
        }
        return result;
    }
    
    /**
     * Dynamically look up a native variable by name.
     * 
     * Look up the symbol in the default list of loaded libraries.
     * 
     * @param varName 
     * @param size the size of the variable in bytes
     * @return a Pointer that can be used to get/set the variable
     * @throws RuntimeException if there is no function by that name.
     */
    public VarPointer getGlobalVariableAddress(String varName, int size) {
        Address result = getSymbolAddress(varName);
        if (DEBUG) {
            VM.print("Var Lookup for ");
            VM.print(varName);
            VM.print(", size: ");
            VM.print(size);
            VM.print(" returned ");
            VM.printAddress(result);
            VM.println();
        }
        if (result.isZero()) {
            if (varName.charAt(0) != '_') {
                return getGlobalVariableAddress("_" + varName, size);
            }
            throw new RuntimeException("Can't find native symbol " + varName + ". OS Error: " + errorStr());
        }
        return new VarPointer(varName, result, size);
    }
    
    /**
     * Look up a native libray named "name". Load if not loaded. Name can be a path or a libray name.
     * The Library will be looked up in a platform-dependant manor.
     * 
     * @param name short library name, full file name, or path to the library file
     * @return NativeLibrary
     */
    public static NativeLibrary getInstance(String name) {
        String nativeName = nativeLibraryName(name);
        Pointer name0 = Pointer.createStringBuffer(nativeName);
        if (DEBUG) {
            VM.print("Calling DLOPEN on ");
            VM.println(name);
        }
        int result = VM.execSyncIO(ChannelConstants.DLOPEN, name0.address().toUWord().toInt(), 0, 0, 0, 0, 0, null, null);
        Address r = Address.fromPrimitive(result);
        name0.free();
        if (r.isZero()) {
            throw new RuntimeException("Can't open library " + name + ". OS Error: " + errorStr());
        }
        return new NativeLibrary(name, r);
    }
    
    /**
     * Return reference to the "default" library. All lookups in the default library
     * will follow the platform's default getFunction semantics.
     * The Library will be looked up in a platform-dependant maner.
     * 
     * @return NativeLibrary
     */
    public static NativeLibrary getDefaultInstance() {
        return RTLD_DEFAULT;
    }

    /**
     * Close the library, as in dlclose.
     * @throws RuntimeException if dlcose fails
     */
    public void dispose() {
        if (closed || ptr.isZero()) {
            throw new RuntimeException("closed or RTLD_DEFAULT");
        }
        if (DEBUG) {
            VM.print("Calling DLCLOSE on ");
            VM.println(name);
        }
        Pointer name0 = Pointer.createStringBuffer(name);
        int result = VM.execSyncIO(ChannelConstants.DLCLOSE, ptr.toUWord().toInt(), 0, 0, 0, 0, 0, 0, null, null);
        name0.free();
        if (result != 0) {
            throw new RuntimeException("Error on dlclose: " + errorStr());
        }
        closed = true;
    }
    
    /**
     * Get any error message provided by the platform (as in dlerror). If no error, then return null.
     * 
     * Note that calling this method clears the error state, so calling two times without calling any other 
     * platform getFunction method (getInstance(), getFunction(), getGlobalVariableAddress(), dispose()) will result in returning null.
     * @return String (may be null)
     */
    public static String errorStr() {
        if (DEBUG) {
            VM.println("Calling DLERROR");
        }
        int result = VM.execSyncIO(ChannelConstants.DLERROR, 0, 0, 0, 0, 0, 0, null, null);
        Address r = Address.fromPrimitive(result);
        if (r.isZero()) {
            return null;
        } else {
            return Pointer.NativeUnsafeGetString(r);
        }
    }
    
    public String toString() {
        return "NativeLibrary(" + name + ", " + ptr.toUWord().toInt() + ")";
    }
    
    
}
