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
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;
import com.sun.squawk.VMThread;

/**
 * A pointer to a native function that can be called from Java.
 *
 * A native function represented by a Function should not block. If you need to calling a blocking function,
 * use  the BlockingFunction class.
 * 
 * <h3>Differences from JNA</h3>
 * <ul>
 * <li> Function is NOT a subclass of Pointer
 * <li> Invocation is via calls to one of the predefined "call()" methods, not using the generic invoke() method.
 * <li> Throws RuntimeExceptions instead of UnsatisfiedLinkErrors. Are link errors really "unrecoverable"? Platform independent code might want to work around missing functions.
 * <li> Calling conventions unimplemented
 * <li> no finalization in cldc, need to call dispose() explicitly (could add a shutdownhook though).
 * <li> no parseVersion();
 * <li> no getFile()
 * </ul>
 */
public class Function {
    protected final static boolean DEBUG = false;

    protected final Address funcAddr;
    protected final String name; // for debugging/tracing

    /**
     * Create a new function pointer with the given name to the given address
     * 
     * @param name the native name of the function
     * @param funcAddr the address of the native function
     */
    Function(String name, Address funcAddr) {
        this.funcAddr = funcAddr;
        this.name = name;
    }

     /**
     * Dynamically look up a native function by name in the named library.
     * 
     * @param libraryName 
     * @param funcName
     * @return an object that can be used to call the named function
     * @throws RuntimeException if there is no function by that name.
     */
    public static Function getFunction(String libraryName, String funcName) {
        return NativeLibrary.getInstance(libraryName).getFunction(funcName);
    }

    /**
     * Return the system errno value from the last native function call made by this Java thread.
     * @return errno
     */
    public static int errno() {
        return VMThread.currentThread().getErrno();
    }
    
    /**
     * @return the function name
     */
    public String getName() {
        return name;
    }
    
    public String toString() {
        return "Function(" + name + ", " + funcAddr.toUWord().toInt() + ")";
    }

    protected void preamble() {
        VM.print(toString());
        VM.println(".call");
    }

    protected void postscript(int result) {
        VM.print("call returned: ");
        VM.print(result);
        VM.println();
    }

    public int call0() {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call0(funcAddr);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);

        return result;
    }

    public int call1(int i1) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call1(funcAddr, i1);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call2(int i1, int i2) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call2(funcAddr, i1, i2);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call3(int i1, int i2, int i3) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call3(funcAddr, i1, i2, i3);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call4(int i1, int i2, int i3, int i4) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call4(funcAddr, i1, i2, i3, i4);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call5(int i1, int i2, int i3, int i4, int i5) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call5(funcAddr, i1, i2, i3, i4, i5);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call6(int i1, int i2, int i3, int i4, int i5, int i6) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call6(funcAddr, i1, i2, i3, i4, i5, i6);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call7(int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call10(funcAddr, i1, i2, i3, i4, i5, i6, i7, 0, 0, 0);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call8(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call10(funcAddr, i1, i2, i3, i4, i5, i6, i7, i8, 0, 0);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call9(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call10(funcAddr, i1, i2, i3, i4, i5, i6, i7, i8, i9, 0);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }

    public int call10(int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) {
        if (DEBUG) {
            preamble();
        }
//        VM.setBlocked(true);
        int result = NativeUnsafe.call10(funcAddr, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10);
        if (DEBUG) {
            postscript(result);
        }
//        VM.setBlocked(false);
        return result;
    }


    /* THE FOLLOWING METHODS THAT TAKE POINTER ARGUMENTS ARE CONVIENIENCE FUNCTIONS FOR HAND-WRITTEN WRAPPERS.
     *   They may disapear in future versions of this API.
     */

    /* ---- Call a function pointer with one arguments ---- */

    public int call1(Pointer p1) {
        return call1(p1.address().toUWord().toPrimitive());
    }

    /* ---- Call a function pointer with two arguments ---- */

    public int call2(int i1, Pointer p2) {
        return call2(i1, p2.address().toUWord().toPrimitive());
    }
    
    public int call2(Pointer p1, int i2) {
        return call2(p1.address().toUWord().toPrimitive(), i2);
    }
    
    public int call2(Pointer p1, Pointer p2) {
        return call2(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive());
    }

    /* ---- Call a function pointer with three arguments ---- */

    public int call3(int i1, int i2, Pointer p3) {
        return call3(i1, i2, p3.address().toUWord().toPrimitive());
    }

    public int call3(int i1, Pointer p2, int i3) {
        return call3(i1, p2.address().toUWord().toPrimitive(), i3);
    }

    public int call3(int i1, Pointer p2, Pointer p3) {
        return call3(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive());
    }

    public int call3(Pointer p1, int i2, int i3) {
        return call3(p1.address().toUWord().toPrimitive(), i2, i3);
    }

    public int call3(Pointer p1, int i2, Pointer p3) {
        return call3(p1.address().toUWord().toPrimitive(), i2, p3.address().toUWord().toPrimitive());
    }

    public int call3(Pointer p1, Pointer p2, int i3) {
        return call3(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), i3);
    }

    public int call3(Pointer p1, Pointer p2, Pointer p3) {
        return call3(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive());
    }
    
    /* Call a function pointer with four arguments */
    public int call4(int i1, int i2, int i3, Pointer p4) {
        return call4(i1, i2, i3, p4.address().toUWord().toPrimitive());
    }
    
    public int call4(int i1, int i2, Pointer p3, int i4) {
        return call4(i1, i2, p3.address().toUWord().toPrimitive(), i4);
    }

    public int call4(int i1, int i2, Pointer p3, Pointer p4) {
        return call4(i1, i2, p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive());
    }
    public int call4(int i1, Pointer p2, int i3, int i4) {
        return call4(i1, p2.address().toUWord().toPrimitive(), i3, i4);
    }

    public int call4(int i1, Pointer p2, int i3, Pointer p4) {
        return call4(i1, p2.address().toUWord().toPrimitive(), i3, p4.address().toUWord().toPrimitive());
    }
    public int call4(int i1, Pointer p2, Pointer p3, int i4) {
        return call4(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), i4);
    }

    public int call4(int i1, Pointer p2, Pointer p3, Pointer p4) {
        return call4(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive());
    }
        
    public int call4(Pointer p1, int i2, int i3, int i4) {
        return call4(p1.address().toUWord().toPrimitive(), i2, i3, i4);
    }

    public int call4(Pointer p1, int i2, int i3, Pointer p4) {
        return call4(p1.address().toUWord().toPrimitive(), i2, i3, p4.address().toUWord().toPrimitive());
    }
    
    public int call4(Pointer p1, int i2, Pointer p3, int i4) {
        return call4(p1.address().toUWord().toPrimitive(), i2, p3.address().toUWord().toPrimitive(), i4);
    }

    public int call4(Pointer p1, int i2, Pointer p3, Pointer p4) {
        return call4(p1.address().toUWord().toPrimitive(), i2, p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive());
    }

    public int call4(Pointer p1, Pointer p2, int i3, int i4) {
        return call4(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), i3, i4);
    }

    public int call4(Pointer p1, Pointer p2, int i3, Pointer p4) {
        return call4(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), i3, p4.address().toUWord().toPrimitive());
    }

    public int call4(Pointer p1, Pointer p2, Pointer p3, int i4) {
        return call4(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), i4);
    }
    
    public int call4(Pointer p1, Pointer p2, Pointer p3, Pointer p4) {
        return call4(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive());
    }
    
    /* ---- Call a function pointer with five arguments ---- 7*/
    public int call5(int i1, int i2, int i3, Pointer p4, int i5) {
        return call5(i1, i2, i3, p4.address().toUWord().toPrimitive(), i5);
    }
    
    public int call5(int i1, int i2, Pointer p3, int i4, int i5) {
        return call5(i1, i2, p3.address().toUWord().toPrimitive(), i4, i5);
    }

    public int call5(int i1, int i2, Pointer p3, Pointer p4, int i5) {
        return call5(i1, i2, p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), i5);
    }
    public int call5(int i1, Pointer p2, int i3, int i4, int i5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), i3, i4, i5);
    }

    public int call5(int i1, Pointer p2, int i3, Pointer p4, int i5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), i3, p4.address().toUWord().toPrimitive(), i5);
    }
    public int call5(int i1, Pointer p2, Pointer p3, int i4, int i5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), i4, i5);
    }

    public int call5(int i1, Pointer p2, Pointer p3, Pointer p4, int i5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), i5);
    }
        
    public int call5(Pointer p1, int i2, int i3, int i4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, i3, i4, i5);
    }

    public int call5(Pointer p1, int i2, int i3, Pointer p4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, i3, p4.address().toUWord().toPrimitive(), i5);
    }
    
    public int call5(Pointer p1, int i2, Pointer p3, int i4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, p3.address().toUWord().toPrimitive(), i4, i5);
    }

    public int call5(Pointer p1, int i2, Pointer p3, Pointer p4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), i5);
    }

    public int call5(Pointer p1, Pointer p2, int i3, int i4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), i3, i4, i5);
    }

    public int call5(Pointer p1, Pointer p2, int i3, Pointer p4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), i3, p4.address().toUWord().toPrimitive(), i5);
    }

    public int call5(Pointer p1, Pointer p2, Pointer p3, int i4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), i4, i5);
    }
    
    public int call5(Pointer p1, Pointer p2, Pointer p3, Pointer p4, int i5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), i5);
    }
    /*-*/
    
    public int call5(int i1, int i2, int i3, int i4, int i5, Pointer p5) {
        return call5(i1, i2, i3, i4, p5.address().toUWord().toPrimitive());
    }
    
    public int call5(int i1, int i2, int i3, Pointer p4, Pointer p5) {
        return call5(i1, i2, i3, p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }
    
    public int call5(int i1, int i2, Pointer p3, int i4, Pointer p5) {
        return call5(i1, i2, p3.address().toUWord().toPrimitive(), i4, p5.address().toUWord().toPrimitive());
    }

    public int call5(int i1, int i2, Pointer p3, Pointer p4, Pointer p5) {
        return call5(i1, i2, p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }
    public int call5(int i1, Pointer p2, int i3, int i4, Pointer p5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), i3, i4, p5.address().toUWord().toPrimitive());
    }

    public int call5(int i1, Pointer p2, int i3, Pointer p4, Pointer p5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), i3, p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }
    public int call5(int i1, Pointer p2, Pointer p3, int i4, Pointer p5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), i4, p5.address().toUWord().toPrimitive());
    }

    public int call5(int i1, Pointer p2, Pointer p3, Pointer p4, Pointer p5) {
        return call5(i1, p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }
        
    public int call5(Pointer p1, int i2, int i3, int i4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, i3, i4, p5.address().toUWord().toPrimitive());
    }

    public int call5(Pointer p1, int i2, int i3, Pointer p4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, i3, p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }
    
    public int call5(Pointer p1, int i2, Pointer p3, int i4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, p3.address().toUWord().toPrimitive(), i4, p5.address().toUWord().toPrimitive());
    }

    public int call5(Pointer p1, int i2, Pointer p3, Pointer p4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), i2, p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }

    public int call5(Pointer p1, Pointer p2, int i3, int i4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), i3, i4, p5.address().toUWord().toPrimitive());
    }

    public int call5(Pointer p1, Pointer p2, int i3, Pointer p4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), i3, p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }

    public int call5(Pointer p1, Pointer p2, Pointer p3, int i4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), i4, p5.address().toUWord().toPrimitive());
    }
    
    public int call5(Pointer p1, Pointer p2, Pointer p3, Pointer p4, Pointer p5) {
        return call5(p1.address().toUWord().toPrimitive(), p2.address().toUWord().toPrimitive(), p3.address().toUWord().toPrimitive(), p4.address().toUWord().toPrimitive(), p5.address().toUWord().toPrimitive());
    }





        /*--------------- HELPERS ------------------*/

    /**
     * Standard conversion function that creates an structure instance of type <code>klass</code> from a C address <code>ptr</code>.
     * If <code>addr0</code> is not NULL, create a new Structure object and copy the data
     *  from the C struct to the Structure object.
     * 
     * @param klass 
     * @param ptr the raw native address of the C struct
     * @return null, or a Structure containing the data from C struct
     */
    public static Structure returnStruct(Class klass, int ptr) {
        Address addr = Address.fromPrimitive(ptr);
        if (addr.isZero()) {
            return null;
        } else {
            try {
                Structure result = (Structure) klass.newInstance();
                result.useMemory(new Pointer(addr, result.size()));
                result.read();
                return result;
            } catch (InstantiationException ex) {
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * Standard conversion function that creates java string from a C char* <code>ptr</code>.
     * 
     * @param ptr the raw native address of the C struct
     * @return null, or Java String containing the string in Java format
     */
    public static String returnString(int ptr) {
        Address addr = Address.fromPrimitive(ptr);
        return Pointer.NativeUnsafeGetString(addr);
    }

}
