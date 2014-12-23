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

package com.sun.squawk.translator;

import com.sun.squawk.Klass;
import com.sun.squawk.Suite;
import com.sun.squawk.VM;
import com.sun.squawk.util.*;
import com.sun.squawk.Method;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Uses the MethodDB to figure out which which methods in a given suite are definitely NOT used. It has to be conservative 
 * about methods that might be called, for example any methods that are exported from a Suite (not private and not stripped).
 *
 * After calling computeMethodsUsed(), the translator can use isMarkedUsed() to determine unused methods.
 *
 */
public class DeadMethodEliminator {
    
    private Translator translator;
    private MethodDB methodDB;
    
    private Hashtable usedMethods = null;
    
    /** Creates a new instance of DeadMethodEliminator
     * @param translator 
     */
    public DeadMethodEliminator(Translator translator) {
        this.translator = translator;
        this.methodDB = translator.methodDB;
    }
    
    /*---------------------------------------------------------------------------*\
     *                         Track unused methods                              *
    \*---------------------------------------------------------------------------*/
    
    /**
     * Is this a method that might be called by the system through some basic mechanism,
     * such as "main", called by interpreter, etc.
     *
     * @param mw The method
     * @return true if this is a main method, an methdo of the class Object
     */
    private static boolean isBasicRoot(MethodDB.Entry mw) {
        Method m = mw.m;
        
        if (m.isInterpreterInvoked()) {
            return true;
        }
        
        if (m.getDefiningClass() == Klass.OBJECT) {
            // basic methods may be called by system:
            return true;
        }
        
        if (m.isStatic()) {
            if (m.getName().equals("main") &&
                m.getReturnType() == Klass.VOID) {
                Object[] parameterTypes = m.getParameterTypes();
                if (parameterTypes.length == 1 &&
                    parameterTypes[0] == Klass.STRING_ARRAY) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Is this a method that might be called by the system because the class exists?
     * This includes default constructors and static initializers.
     *
     * Report seperately to help identify code that could be eliminated by eliminating the class.
     *
     * @param mw The method
     * @return true if this is a default constructors or static initializer
     */
    private static boolean isClassMandatedRoot(MethodDB.Entry mw) {
        Method m = mw.m;
        
        if (m.isStatic()) {
            if (m.isClassInitializer() || m.isReplacementConstructor()) {
                return true;
            }
             if (m.isDefaultConstructor()) {
                // Don't preserve uncalled private constructors.
                // This means that the unlikely call of ClassFoo.newInstance() while within 
                // ClassFoo will fail when it shouldn't.
                if (Arg.get(Arg.DELETE_UNUSED_PRIVATE_CONSTRUCTORS).getBool() && (m.isPrivate())) {
                    return false;
                }
               /* if (VM.isInternal(m.getDefiningClass())) {
                    // no way to look up this class dynamically, so can eliminate constructor if not called explicitly:
                    System.out.println("May eliminate " + m);
                    return false;
                }*/
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Given a SquawkVector of Strings, return a sorted array of those strings.
     */
    private String[] sortStringVector(SquawkVector v) {
        String[] tmp = new String[v.size()];
        v.copyInto(tmp);
        Arrays.sort(tmp, new Comparer () {
            public int compare(Object a, Object b) {
                String astr = (String)a;
                String bstr = (String)b;
                
                return astr.compareTo(bstr);
            }
        });
        return tmp;
    }
    
    /**
     * Given a SquawkVector of Strings, print it sorted
     *
     * @param v a SquawkVector of Strings
     * @param prefix string to print before element.
     */
    private void printVectorSorted(SquawkVector v, String prefix) {
        String[] results = sortStringVector(v);
        for (int i = 0; i < results.length; i++) {
            Tracer.trace(prefix);
            Tracer.traceln(results[i]);
        }
    }
    
    private void printMethodVector(SquawkVector vector, String prefix) {
        if (vector != null) {
            int len = vector.size();
            Tracer.traceln(prefix);
            for (int i = 0; i < len; i++) {
                MethodDB.Entry cw = (MethodDB.Entry)vector.elementAt(i);
                Tracer.traceln("        " +  cw);
            }
        }
    }
    
    private void printMethodTable() {
        Enumeration e = methodDB.getAllMethods();
        while (e.hasMoreElements()) {
            MethodDB.Entry mw = (MethodDB.Entry)e.nextElement();
            Tracer.traceln("Method: " +  mw);
            
            printMethodVector(mw.getCalls(),        "    calls:");
            printMethodVector(mw.getOverrides(),    "    overrides:");
            printMethodVector(mw.getSuperMethods(), "    supermethods:");
        }
    }
    
    /**
     * Return true if the class has any constructor that has not been eliminated
     * @param klass
     * @return true if still any constructors...
     */
    private boolean hasUsedConstructor(Klass klass) {
        for (int j = klass.getMethodCount(true) - 1; j >= 0; j--) {
            Method m = klass.getMethod(j, true);
            if (m.isConstructor() && isMarkedUsed(m)) {
                return true;
            }
        }
        return false;
    }

    public void computeMethodsUsed() {
        boolean trace = (Translator.TRACING_ENABLED && Tracer.isTracing("DME")) || VM.isVeryVerbose();
        usedMethods = new Hashtable();
        Enumeration e;
        SquawkVector foundMethods = new SquawkVector(); // used for tracing
        Suite suite = translator.getSuite();

        // look for action based not on the methods defined in the class, but in the 
        // effects of inherited methods and implemented interfaces:
        // This may populate more overrides and supermethods entries.
        for (int cno = 0; cno < suite.getClassCount(); cno++) {
            Klass klass = suite.getKlass(cno);
            if (klass != null) {
                methodDB.computeInheritedImplementorsInfo(klass);
            }
        }
        
        if (Klass.TRACING_ENABLED && Tracer.isTracing("callgraph")) {
            printMethodTable();
        }

        // Preserve methods that might be called automatically by system, beyond the powers of analysis:
        e = methodDB.getAllMethods();
        foundMethods.removeAllElements();
        while (e.hasMoreElements()) {
            MethodDB.Entry mw = (MethodDB.Entry)e.nextElement();
            if (isBasicRoot(mw)) {
                if (trace) {
                    foundMethods.addElement(mw.toString() + " size: " + mw.getSize());
                }
                markMethodUsed(mw);
            }
        }
        if (trace && foundMethods.size() != 0) {
            Tracer.traceln("[translator DME: ==== System roots:  " + foundMethods.size() + " =====");
            printVectorSorted(foundMethods, "System root: ");
        }
        
        // Preserve all externally visible methods
        e = methodDB.getAllMethods();
        foundMethods.removeAllElements();
        while (e.hasMoreElements()) {
            MethodDB.Entry mw = (MethodDB.Entry)e.nextElement();
            if (methodDB.isExternallyVisible(mw) && !isMarkedUsed(mw)) {
                if (trace) {
                    foundMethods.addElement(mw.toString() + " size: " + mw.getSize());
                }
                markMethodUsed(mw);
            }
        }
        if (trace && foundMethods.size() != 0) {
            Tracer.traceln("[translator DME: ==== Callable roots:  " + foundMethods.size() + " =====");
            printVectorSorted(foundMethods, "Callable root: ");
        }
        
        // Preserve all reflectively visible methods
        e = methodDB.getAllMethods();
        foundMethods.removeAllElements();
        while (e.hasMoreElements()) {
            MethodDB.Entry mw = (MethodDB.Entry)e.nextElement();
            if (isClassMandatedRoot(mw) && !isMarkedUsed(mw)) {
                if (trace) {
                    foundMethods.addElement(mw.toString() + " size: " + mw.getSize());
                }
                markMethodUsed(mw);
            }
        }
        if (trace && foundMethods.size() != 0) {
            Tracer.traceln("[translator DME: ==== Class Mandated roots:  " + foundMethods.size() + " =====");
            printVectorSorted(foundMethods, "Class root: ");
        }

        // NOT READY FOR PRIME-TIME
//        // look for classes that are no longer instantiable, becuase constructors were eliminated:
//        Suite ste = translator.getSuite();
//        foundMethods.removeAllElements();
//        for (int i = ste.getClassCount() - 1; i >= 0; i--) {
//            Klass klass = ste.getKlass(i);
//            if (klass.isInstantiable()) {
//                if (!hasUsedConstructor(klass)) {
//                    for (int j = klass.getMethodCount(false) - 1; j >= 0; j--) {
//                        Method m = klass.getMethod(j, false);
//                        MethodDB.Entry mw = methodDB.lookupMethodEntry(m);
//                        if (isMarkedUsed(mw) && !isBasicRoot(mw)) {
//                            foundMethods.addElement(mw.toString() + " size: " + mw.getSize());
//                        }
//                    }
//                }
//            }
//        }
//        if (foundMethods.size() != 0) {
//            Tracer.traceln("[translator DME: ==== MAYBE UNCALLABLE METHODS?:  " + foundMethods.size() + " (called methods: " + usedMethods.size() + ") =====");
//            printVectorSorted(foundMethods, "    ");
//        }

        
        // report unused methods:
        if (trace || VM.isVeryVerbose()) {
            e = methodDB.getAllMethods();
            foundMethods.removeAllElements();
            while (e.hasMoreElements()) {
                MethodDB.Entry mw = (MethodDB.Entry)e.nextElement();
                if (!isMarkedUsed(mw) && !mw.m.isAbstract()) {
                    foundMethods.addElement(mw.toString() + " size: " + mw.getSize());
                }
            }
            if (foundMethods.size() != 0) {
                Tracer.traceln("[translator DME: ==== Uncalled methods:  " + foundMethods.size() + " (called methods: " + usedMethods.size() + ") =====");
                printVectorSorted(foundMethods, "    ");
            }
        }
    }
    
    private void markMethodVectorUsed(SquawkVector vector) {
        if (vector != null) {
            int len = vector.size();
            for (int i = 0; i < len; i++) {
                MethodDB.Entry cw = (MethodDB.Entry)vector.elementAt(i);
                markMethodUsed(cw);
            }
        }
    }
    
    private boolean isMarkedUsed(MethodDB.Entry mw) {
        return usedMethods.get(mw) != null;
    }
    
    /**
     * Have we determined that method M is called, or callable.
     * Called by the rest of the translator to do the actual method elimination.
     *
     * @param m the Method
     * @return true if M is called, or is callable from outside of the suite.
     */
    public boolean isMarkedUsed(Method m) {
        return isMarkedUsed(methodDB.lookupMethodEntry(m));
    }
    
    /**
     * Mark this method as used, 
     * all of the methods that it calls as used, 
     * and if it's a virtual method, mark any method that overrides this method as used,
     * and all methods that it overrides as used.
     */
    private void markMethodUsed(MethodDB.Entry mw) {
        if (!isMarkedUsed(mw)) {
            // then unmarked so far, now mark it.
            usedMethods.put(mw, mw);

            // call unless we are testing for failures in DME work.
            if (Translator.FORCE_DME_ERRORS && (mw.m.getFullyQualifiedName().toLowerCase().indexOf("test") >= 0) && "main".equals(mw.m.getName())) {
                System.out.println("TESTING: Not marking called methods as used from " + mw); 
            } else {
                markMethodVectorUsed(mw.getCalls());
            }
            
            if (!mw.m.isStatic()) {
                markMethodVectorUsed(mw.getOverrides());
                markMethodVectorUsed(mw.getSuperMethods());
            }
        }
    }
     
}
