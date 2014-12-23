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

package com.sun.squawk;

import com.sun.squawk.vm.HDR;
import com.sun.squawk.util.Assert;
import java.io.PrintStream;

/**
 * An ExecutionPoint instance encapsulates the details of a point of execution
 * including the thread, a frame offset, the method to which the
 * frame pertains and the bytecode index of an instruction in the method.
 * 
 * Used for printing stack traces, setting breakpoints, etc.
 */
public final class ExecutionPoint {

    public final Offset frame;
    public final Offset bci;
    public final Object mp;

    public ExecutionPoint(Offset frame, Offset bci, Object mp) {
        super();
        this.frame = frame;
        this.bci = bci;
        this.mp = mp;
    }

//    public int hashCode() {
//        throw new RuntimeException("ExecutionPoints cannot be keys in a hashtable");
//    }

/*if[DEBUG_CODE_ENABLED]*/
    public String toString() {
        return "frame=" + frame.toPrimitive() + ",bci=" + bci.toPrimitive() + ",mp=" + Address.fromObject(mp).toUWord().toPrimitive() + "]";
    }
/*end[DEBUG_CODE_ENABLED]*/
    
    /**
     * Return the klass that defined the method referred to by this ExecutionPoint.
     * @return the defining klass.
     */
    public Klass getKlass() {
        return VM.asKlass(NativeUnsafe.getObject(mp, HDR.methodDefiningClass));
    }

    /**
     * Return the high-level Method object for the (low-level) method refered to by this ExecutionPoint.
     * 
     * This relies on metadata data being saved in the suite for this Method.
     * 
     * @return the Method or null if no metadata exists for the method.
     */
    public Method getMethod() {
/*if[ENABLE_RUNTIME_METADATA]*/
        return getKlass().findMethod(mp);
/*else[ENABLE_RUNTIME_METADATA]*/
//      return null;
/*end[ENABLE_RUNTIME_METADATA]*/
    }

    private void printKnownMethod(PrintStream out, Klass klass, String methodName, int[] lnt) {
        out.print("at ");
        out.print(klass.getName());
        out.print('.');
        out.print(methodName);
        out.print('(');
        String src = klass.getSourceFileName();
        if (src != null) {
            out.print(src);
            out.print(':');
        }

        if (lnt != null) {
            int lno = Method.getLineNumber(lnt, bci.toPrimitive());
            out.print(lno);
        } else {
            out.print("bci=");
            out.print(bci.toPrimitive());
        }
        out.print(')');
    }

    private void printUnknownMethod(PrintStream out, Klass klass, String methodKind, int index) {
        out.print("in ");
        out.print(methodKind);
        out.print(" method #");
        out.print(index);
        out.print(" of ");
        out.print(klass.getName());
        out.print("(bci=");
        out.print(bci.toPrimitive());
        out.print(')');
    }

    private String calcStaticMethodName(Klass klass, int index) {
        if (index == klass.getClinitIndex()) {
            return "<clinit>";
        } else if (index == klass.getDefaultConstructorIndex()) {
            return "<init>";
        } else if (index == klass.getMainIndex()) {
            return "main";
        }
        return null;
    }

    private String calcVirtualMethodName(Klass klass, int index, Object[] temp) {
        if (klass != null) {
            Method method = klass.lookupVirtualMethod(index);
            if (method != null) {
                Assert.that(method.getOffset() == index);
                return method.getName();
            } else {
                int islot = klass.findISlot(index, temp);
                if (islot >= 0) {
                    Klass iKlass = (Klass) temp[0];
                    method = iKlass.getMethod(islot, false);
                    if (method != null) {
                        return method.getName();
                    } else {
                        return null;
                    }
                }
            }
            return calcVirtualMethodName(klass.getSuperclass(), index, temp);
        }
        return null;
    }

    private void printToVM(Klass klass, String methodName, int[] lnt) {
        VM.print("at ");
        VM.print(klass.getInternalName());
        VM.print('.');
        VM.print(methodName);
        VM.print('(');
        String src = klass.getSourceFileName();
        if (src != null) {
            VM.print(src);
            VM.print(':');
        }

        if (lnt != null) {
            int lno = Method.getLineNumber(lnt, bci.toPrimitive());
            VM.print(lno);
        } else {
            VM.print("bci=");
            VM.printOffset(bci);
        }
        VM.println(')');
    }

    private void printToVM(Klass klass, String methodKind, int index) {
        VM.print("in ");
        VM.print(methodKind);
        VM.print(" method #");
        VM.print(index);
        VM.print(" of ");
        VM.print(klass.getInternalName());
        VM.print("(bci=");
        VM.printOffset(bci);
        VM.println(')');
    }

    /** 
     * Print a one-line description of this execution point using low-level VM.print 
     * methods.
     * 
     * Will attempt to print the method name, if available, otherwise will print the 
     * method index and class name. Will also attempt to print the line number of the
     * execution pint if known,
     * 
     * @param out the stream to print to
     */
    void printToVM() {
        try {
            Klass klass = getKlass();
            int index;
/*if[ENABLE_RUNTIME_METADATA]*/
            Method method = klass.findMethod(mp);
            if (method == null) {
                if ((index = klass.getMethodIndex(mp, true)) >= 0) {
                    String methodName = calcStaticMethodName(klass, index);
                    if (methodName != null) {
                        printToVM(klass, methodName, null);
                    } else {
                        printToVM(klass, "static", index);
                    }
                } else if ((index = klass.getMethodIndex(mp, false)) >= 0) {
                    printToVM(klass, "virtual", index);
                }
            } else {
                printToVM(klass, method.getName(), method.getLineNumberTable());
            }
/*else[ENABLE_RUNTIME_METADATA]*/
//            if ((index = klass.getMethodIndex(mp, true)) >= 0) {
//                printToVM(klass, "static", index);
//            } else if ((index = klass.getMethodIndex(mp, false)) >= 0) {
//                printToVM(klass, "virtual", index);
//            }
/*end[ENABLE_RUNTIME_METADATA]*/
        } catch (Throwable e) {
            VM.println("Exception thrown in ExecutionPoint.printToVM()");
        }
    }

    /** 
     * Print a one-line description of this execution point to <code>stream</code>.
     * 
     * Will attempt to print the method name, if available, otherwise will print the 
     * method index and class name. Will also attempt to print the line number of the
     * execution pint if known,
     * 
     * @param out the stream to print to
     */
    public void print(PrintStream out) {
        try {
            Klass klass = getKlass();
            int index;
/*if[ENABLE_RUNTIME_METADATA]*/
            Method method = klass.findMethod(mp);
            if (method == null) {
                if ((index = klass.getMethodIndex(mp, true)) >= 0) {
                    String methodName = calcStaticMethodName(klass, index);
                    if (methodName != null) {
                        printKnownMethod(out, klass, methodName, null);
                    } else {
                        printUnknownMethod(out, klass, "static", index);
                    }
                } else if ((index = klass.getMethodIndex(mp, false)) >= 0) {
                    String methodName = calcVirtualMethodName(klass, index, new Object[1]);
                    if (methodName != null) {
                        printKnownMethod(out, klass, methodName, null);
                    } else {
                        printUnknownMethod(out, klass, "virtual", index);
                    }
                }
            } else {
                printKnownMethod(out, klass, method.getName(), method.getLineNumberTable());
            }
/*else[ENABLE_RUNTIME_METADATA]*/
//            if ((index = klass.getMethodIndex(mp, true)) >= 0) {
//                printUnknownMethod(out, klass, "static", index);
//            } else if ((index = klass.getMethodIndex(mp, false)) >= 0) {
//                printUnknownMethod(out, klass, "virtual", index);
//            }
/*end[ENABLE_RUNTIME_METADATA]*/
        } catch (Throwable e) {
/*if[DEBUG_CODE_ENABLED]*/
            if (VM.isVerbose()) {
                VM.printVMStackTrace(e, "***", "Exception thrown in ExecutionPoint.print():");
            }
/*end[DEBUG_CODE_ENABLED]*/
            VM.print("*** Error decoding this ExecutionPoint:\n    ");
            printToVM();
            // print it the simple way
            out.print("------");
        }
    }
}
