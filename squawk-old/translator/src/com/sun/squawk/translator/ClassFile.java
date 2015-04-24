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

import java.util.Vector;
import com.sun.squawk.util.*;
import com.sun.squawk.translator.ci.*;
import com.sun.squawk.*;

/**
 * This represents a class that has not yet been loaded and linked.
 *
 */
public final class ClassFile {


    /*---------------------------------------------------------------------------*\
     *               Global constants for zero length arrays.                    *
    \*---------------------------------------------------------------------------*/

    /**
     * A zero length array of methods.
     */
    public static final Code[] NO_METHODS = {};

    /**
     * A zero length array of Objects.
     */
    public static final Object[] NO_OBJECTS = {};

    /**
     * A zero length array of Squawk bytecode methods.
     */
    public static final byte[][] NO_SUITE_METHODS = {};


    /*---------------------------------------------------------------------------*\
     *                           Fields of ClassFile                             *
    \*---------------------------------------------------------------------------*/

    /**
     * The class defined by this class file.
     */
    private final Klass definedClass;

    /**
     * The code for the virtual methods defined in this class file. The
     * elements corresponding to abstract and native methods will be null.
     */
    private Code[] virtualMethods;

    /**
     * The code for the static methods defined in this class file.
     */
    private Code[] staticMethods;

    /**
     * The constant pool of this class file.
     */
    private ConstantPool constantPool;
    

    /*---------------------------------------------------------------------------*\
     *                               Constructor                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Creates a new <code>ClassFile</code> instance.
     *
     * @param   klass      the class defined by this class file
     */
    public ClassFile(Klass klass) {
        this.definedClass = klass;
        this.staticMethods  = NO_METHODS;
        this.virtualMethods = NO_METHODS;
    }

    /*---------------------------------------------------------------------------*\
     *                                Setters                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Sets the constant pool for this class.
     *
     * @param constantPool  the constant pool for this class
     */
    public void setConstantPool(ConstantPool constantPool) {
        Assert.that(this.constantPool == null || this.constantPool == constantPool, "cannot reset the constant pool");
        this.constantPool = constantPool;
    }

    /**
     * Sets the virtual methods for this class.
     *
     * @param  methods  the virtual methods declared by this class
     */
    public void setVirtualMethods(Code[] methods) {
        Assert.that(this.virtualMethods == NO_METHODS, "cannot reset the virtual methods");
        this.virtualMethods = methods;
    }

    /**
     * Sets the static methods for this class.
     *
     * @param  methods  the static methods declared by this class
     */
    public void setStaticMethods(Code[] methods) {
        Assert.that(this.staticMethods == NO_METHODS, "cannot reset the static methods");
        this.staticMethods = methods;
    }


    /*---------------------------------------------------------------------------*\
     *                                Getters                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the class defined by this class file.
     *
     * @return  the class defined by this class file
     */
    public Klass getDefinedClass() {
        return definedClass;
    }

    /**
     * Gets the constant pool of this class.
     *
     * @return the constant pool of this class
     */
    public ConstantPool getConstantPool() {
        return constantPool;
    }
    
    int getStaticMethodCount() {
        return staticMethods.length;
    }
    
    Code getStaticMethod(int i) {
        if (i >= staticMethods.length || i < 0) {
            throw new RuntimeException("bad index: " + i + " max is: " + staticMethods.length);
        }
        
        if (staticMethods.length == 0) {
            throw new RuntimeException("bad index: " + i + " max is: " + staticMethods.length);
        }
        return staticMethods[i];
    }
    
    int getVirtualMethodCount() {
        return virtualMethods.length;
    }
    
    Code getVirtualMethod(int i) {
        return virtualMethods[i];
    }


    /*---------------------------------------------------------------------------*\
     *                    Table of constant and class references                 *
    \*---------------------------------------------------------------------------*/

    private ObjectTable objectTable;

    /**
     * Create an ObjectTable for this class based on the ObjectTables of all of the used methods in the class.
     * @param translator
     */
    void createMergedObjectTable(Translator translator) {
        objectTable = new ObjectTable(definedClass);

        objectTable.mergeMethodsObjectTable(translator, staticMethods, true);
        objectTable.mergeMethodsObjectTable(translator, virtualMethods, false);
        if (Arg.get(Arg.OPTIMIZE_CONSTANT_OBJECTS).getBool() && ((translator.dce == null) || translator.dce.isMarked(definedClass))) {
            objectTable.sortObjectTable();
        }
    }
    
    public ObjectTable getObjectTable() {
        return objectTable;
    }

    /*---------------------------------------------------------------------------*\
     *                       Class loading and converting                        *
    \*---------------------------------------------------------------------------*/

    /**
     * Converts a set of methods from their Java bytecode form to their
     * Squawk bytecode form.
     *
     * @param translator   the translation context
     * @param isStatic     specifies static or virtual methods
     * @param phase        the conversion phase number to perform (1 or 2) or 0 for both
     * @param bodies       {@link Vector} to insert method bodies into
     */
    private void convertMethods(Translator translator, boolean isStatic, int phase, Vector bodies) {
        Code[] methodsCode = isStatic ? staticMethods : virtualMethods;
        for (int i = 0 ; i < methodsCode.length ; i++) {
            Method method = definedClass.getMethod(i, isStatic);
            Code code = methodsCode[i];
            if (method.isAbstract() && (phase == 0 || phase == 1)) {
                translator.methodDB.recordMethod(method, 0);
            }
            
            if (!method.isHosted() && !method.isAbstract() && !method.isNative()) {
                Assert.that(code != null);
                if (phase == 0 || phase == 1) {
                    code.convert(translator, method, method.getOffset(), 1, null);
                    int size = 0;
                    if (Translator.TRACING_ENABLED) {
                        size = code.getIR().size();
                    }
                    translator.methodDB.recordMethod(method, size);
                }
                
                if (phase == 0 || phase == 2) {
                    code.convert(translator, method, method.getOffset(), 2, bodies);
                    methodsCode[i] = null; // Allow GC
                }
//if (phase == 0) {
//    VM.println("Finished converting " + method);
//    System.gc();
//}
            }
        }
    }

    /**
     * Performs a pre-pass over all of the methods in the class, generating IR for each method.
     * Changes the state of the definedClass from <code>STATE_LOADED</code> to <code>STATE_CONVERTING</code>.
     *
     * @param translator   the translation context
     * @param generateIR   generateIR now if true. otherwise wait for pass2.
     */
    void convertPhase1(Translator translator, boolean generateIR) {
        int state = definedClass.getState();
        Assert.that(state == Klass.STATE_LOADED, "class must be loaded before conversion");
        Assert.that(!definedClass.isSynthetic(), "synthetic classes should not require conversion");
        Assert.that(!definedClass.isPrimitive(), "primitive types should not require conversion");

        /*
         * Convert this type's super class first
         */
        Klass superClass = definedClass.getSuperclass();
        if (superClass != null) {
            translator.convert(superClass);
        }

        /*
         * Write trace message
         */
        if (Translator.TRACING_ENABLED && Tracer.isTracing("converting", definedClass.getName())) {
            Tracer.traceln("[converting " + definedClass + "]");
        }

        /*
         * Generate IR if doing two-pass translation
         */
        if (generateIR) {
            // This conversion first builds the IR for all the methods.
            convertMethods(translator, true, 1, null);
            convertMethods(translator, false, 1, null);
        }
        
        definedClass.changeState(Klass.STATE_CONVERTING);
    }

    /**
     * Generate squawk bytecodes for methods of this class from either the IR generated in phase1,
     * or generate IR and squawk bytecode in one pass.
     * Changes the state of the definedClass from <code>STATE_CONVERTING</code> to <code>STATE_CONVERTED</code>.
     *
     * @param translator   the translation context
     * @param doInOnePass  pass1 didn't actually generateIR, so do it all now, in one pass.
     */
    void convertPhase2(Translator translator, boolean doInOnePass) {
        int state = definedClass.getState();
        Assert.that(state == Klass.STATE_CONVERTING, "class must be loaded before conversion");
        Assert.that(!definedClass.isSynthetic(), "synthetic classes should not require conversion");
        Assert.that(!definedClass.isPrimitive(), "primitive types should not require conversion");

        /*
         * Convert this type's super class first
         */
        Klass superClass = definedClass.getSuperclass();
        if (superClass != null) {
            translator.convertPhase2(superClass);
        }
        
        Vector bodies = null;
/*if[SUITE_VERIFIER]*/
        bodies = new Vector();
/*end[SUITE_VERIFIER]*/

        try {
            if (doInOnePass) {
                // generate IR and squawk code in one pass:
                convertMethods(translator, true, 0, bodies);
                convertMethods(translator, false, 0, bodies);
                createMergedObjectTable(translator);
                Object[] objects = objectTable.getConstantObjectArray();
                definedClass.setObjectTable(objects);
            } else {
                // create and optimize the objecttable:
                createMergedObjectTable(translator);
                Object[] objects = objectTable.getConstantObjectArray();
                definedClass.setObjectTable(objects);
                
                // Now generate squawk code from IR.
                convertMethods(translator, true, 2, bodies);
                convertMethods(translator, false, 2, bodies);
            }
        } catch (NoClassDefFoundError e) {
            definedClass.changeState(Klass.STATE_ERROR);
            throw e;
        }

/*if[SUITE_VERIFIER]*/
        for (int i = 0; i < bodies.size(); i++) {
            MethodBody body = (MethodBody)bodies.elementAt(i);
            if (body != null) {
                new com.sun.squawk.translator.ir.verifier.Verifier().verify(body);
            }
        }
/*end[SUITE_VERIFIER]*/

        definedClass.changeState(Klass.STATE_CONVERTED);

        /*
         * Write trace message
         */
        if (Translator.TRACING_ENABLED && Tracer.isTracing("converting", definedClass.getName())) {
            Tracer.traceln("[converted " + definedClass + "]");
        }
    }

}

