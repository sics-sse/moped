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

import java.io.*;

import javax.microedition.io.Connector;
import java.util.Hashtable;
import java.util.Stack;

import com.sun.squawk.util.Assert;
import com.sun.squawk.io.connections.*;
import com.sun.squawk.translator.ci.*;
import com.sun.squawk.util.ComputationTimer;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.*;
import com.sun.squawk.translator.ir.InstructionEmitter;

/**
 * The Translator class presents functionality for loading and linking
 * classes from class files (possibly bundled in jar files) into a
 * {@link Suite}.<p>
 *
 */
public final class Translator implements TranslatorInterface {
    
    public final static boolean TRACING_ENABLED = true;

    /*---------------------------------------------------------------------------*\
     *                      Translator options/optimization flags                *
    \*---------------------------------------------------------------------------*/
    
    protected final Stack lastClassNameStack = new Stack();

    public final static boolean FORCE_DME_ERRORS = false;

    /**
     * Returns true the translator should try to inline method calls.
     */
    public static boolean shouldDoInlining() {
        return false;
//        return Arg.get(Arg.INLINE_METHOD_LIMIT).getInt() > 0;
    }

    /**
     * Returns true if the translator should print verbose progress
     */
    public static boolean verbose() {
        return Arg.get(Arg.VERBOSE).getBool();
    }

    public Translator() {
        Arg.defineOptions();
    }
    
    private boolean optimizeSuite;
    
    /**
     * True if any optimization implies whole-suite optimization.
     */
    public boolean shouldOptimizeSuite() {
        return optimizeSuite;
    }
    
    /**
     * Read translator properties and set corresponding options.
     */
    private void setOptions() {
        Arg.setOptions();
        
        if (Arg.get(Arg.DEAD_CLASS_ELIMINATION).getBool() ||  Arg.get(Arg.DEAD_METHOD_ELIMINATION).getBool() /* || (Arg.get(Arg.INLINE_METHOD_LIMIT).getInt() > 0)*/) {
            translationStrategy = BY_SUITE;
        } else if (Arg.get(Arg.OPTIMIZE_CONSTANT_OBJECTS).getBool()) {
            translationStrategy = BY_CLASS;
        } else {
            translationStrategy = BY_METHOD;
        }
        
        if (translationStrategy >= BY_SUITE && (
                (shouldDoInlining()
                /*|| Arg.get(Arg.OPTIMIZE_BYTECODE).getBool()
                || Arg.get(Arg.OPTIMIZE_BYTECODE_CONTROL).getBool()
                || Arg.get(Arg.OPTIMIZE_DEADCODE).getBool()*/ ))) {
            optimizeSuite = true;
        }
    }

    private int progressCounter = 0;
    
    /**
     * Returns true if the translator should print verbose progress
     */
    public void traceProgress() {
        if (verbose()) {
            progressCounter++;
            Tracer.trace(".");
            if (progressCounter % 40 == 0) {
                Tracer.trace("\n");
            }
        }
    }
    
    /*---------------------------------------------------------------------------*\
     *                     Implementation of TranslatorInterface                 *
    \*---------------------------------------------------------------------------*/
    
    public final static int BY_METHOD = 1;
    public final static int BY_CLASS = 2;
    public final static int BY_SUITE = 3;
    public final static int BY_TRANSLATION = 4;
    
    /**
     * Should we translate a method at a time, class at a time, suite at a time, or bundle of suites at a time?
     */
    private int translationStrategy = 0;

    /**
     * The suite context for the currently open translator.
     */
    private Suite suite;

    /**
     * The suite type of the final suite. Note that when stripping a suite, we actually translate the unstriped suite.
     * This is set in close().
     */
    private int suiteType;

    /**
     * The loader used to locate and load class files.
     */
    private ClasspathConnection classPath;

    /**
     * The database of methods, callers, overrides, etc.
     */
    public MethodDB methodDB;
    
    /**
     * A DeadMethodEliminator is created in close() if we do dead method elimination.
     */
    DeadMethodEliminator dme;
    
    /**
     * A DeadClassEliminator is created in close() if we do dead class elimination.
     */
    DeadClassEliminator dce;

    /**
     * {@inheritDoc}
     */
    public void open(Suite suite, String classPath) {
        this.suiteType = -9999; // This is set for real in close().
        this.suite = suite;
        this.classFiles = new Hashtable();
        setOptions();
        try {
            String url = "classpath://" +  classPath;
            this.classPath = (ClasspathConnection)Connector.open(url);
        } catch (IOException ioe) {
        	throw new LinkageError("Error while setting class path from '"+ classPath + "': " + ioe);
        }
        methodDB = new MethodDB(this);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValidClassName(String name) {
        return name.indexOf('/') == -1 && ConstantPool.isLegalName(name.replace('.', '/'), ConstantPool.ValidNameFormat.CLASS);
    }

    /**
     * {@inheritDoc}
     */
    public void load(Klass klass) {
        Assert.that(VM.isHosted() || VM.getCurrentIsolate().getLeafSuite() == suite);
        int state = klass.getState();
        if (state < Klass.STATE_LOADED) {
            if (klass.isArray()) {
                load(klass.getComponentType());
            } else {
            	lastClassNameStack.push(klass.getName());
                ClassFile classFile = getClassFile(klass);
                load(classFile);
                lastClassNameStack.pop();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void convert(Klass klass) {
    	lastClassNameStack.push(klass.getName());
        int state = klass.getState();
        if (state < Klass.STATE_CONVERTING) {
            if (klass.isArray()) {
                convert(Klass.OBJECT);
                klass.changeState(Klass.STATE_CONVERTED);
            } else {
                traceProgress();
                
                ClassFile classFile = getClassFile(klass);
                classFile.convertPhase1(this, translationStrategy != BY_METHOD);
                if (klass.hasGlobalStatics()) {
                    // record globals now.
                    recordGlobalStatics(klass);
                }
                
                if (translationStrategy == BY_METHOD || translationStrategy == BY_CLASS) {
                    // if NOT inlining, then generate squawk code now.
                    classFile.convertPhase2(this, translationStrategy == BY_METHOD);
                    classFiles.remove(klass.getName());
                }       
            }
        }
        lastClassNameStack.pop();
    }
    
    private void recordGlobalStatics(Klass klass) {
        
    }
    
    /**
     * Generate squawk code for methods of <code>klass</code> when doing whole-suite translation (inlining, etc.)
     *
     * @param   klass  the klass to generate code for
     */
    void convertPhase2(Klass klass) {
        Assert.that(translationStrategy != BY_METHOD);
        convert(klass);
        if (klass.getState() < Klass.STATE_CONVERTED) {
            if (!VM.isVerbose()) { // "squawk -verbose" will show the class names as it finishes loading them, which is progress enough
                traceProgress();
            }
        	lastClassNameStack.push(klass.getName());
            ClassFile classFile = getClassFile(klass);
        	classFile.convertPhase2(this, false);
            classFiles.remove(klass.getName());
            lastClassNameStack.pop();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void close(int suiteType) throws NoClassDefFoundError {
        long time = 0;
        this.suiteType = suiteType;
        
        if (verbose()) {
            Tracer.trace("[Translator: computing closure...");
            time = System.currentTimeMillis();
        }
        
        computeClosure();
        
        if (translationStrategy == BY_SUITE || translationStrategy == BY_TRANSLATION) {
            if (verbose()) {
                time = System.currentTimeMillis() - time;
                Tracer.traceln(time + "ms.]");
                Tracer.trace("[Translator: whole-suite optimizing and inlining...");
                time = System.currentTimeMillis();
            }
            // bytecode optimizations and inlining go here
            
            if (Arg.get(Arg.DEAD_METHOD_ELIMINATION).getBool()) {
                dme = new DeadMethodEliminator(this);
                dme.computeMethodsUsed();
            }
            
            if (Arg.get(Arg.DEAD_CLASS_ELIMINATION).getBool()) {
                dce = new DeadClassEliminator(this);
                dce.computeClassesUsed();
            }

            if (verbose()) {
                time = System.currentTimeMillis() - time;
                Tracer.traceln(time + "ms.]");
                Tracer.trace("[Translator: phase2...");
                time = System.currentTimeMillis();
            }
            
            for (int cno = 0; cno < suite.getClassCount(); cno++) {
                Klass klass = suite.getKlass(cno);
                Assert.always(Arg.get(Arg.DEAD_CLASS_ELIMINATION).getBool() || (klass != null));
                if (klass != null) {
                    convertPhase2(klass);
                }
            }
        }
        classFiles.clear();
        
        if (verbose()) {
            time = System.currentTimeMillis() - time;
            Tracer.traceln(time + "ms.]");

            if (VM.isVeryVerbose()) {
                InstructionEmitter.printUncalledNativeMethods();
            }
        }
        Assert.always(lastClassNameStack.empty());
    }
    
   /**
     * {@inheritDoc}
     */
    public void printTraceFlags(PrintStream out) {
        if (Translator.TRACING_ENABLED) {
            out.println("    -traceloading         trace class loading");
            out.println("    -traceconverting      trace method conversion (includes -traceloading)");
            out.println("    -tracejvmverifier     trace verification of JVM/CLDC bytecodes");
            out.println("    -traceemitter         trace Squawk bytecode emitter");
            out.println("    -tracesquawkverifier  trace verification of Squawk bytecodes");
            out.println("    -traceclassinfo       trace loading of class meta-info (i.e. implemented");
            out.println("                          interfaces, field meta-info & method meta-info)");
            out.println("    -traceclassfile       trace low-level class file elements");
            out.println("    -traceir0             trace the IR built from the JVM bytecodes");
            out.println("    -traceir1             trace optimized IR with JVM bytcode offsets");
            out.println("    -traceir2             trace optimized IR with Squawk bytcode offsets");
            out.println("    -tracemethods         trace emitted Squawk bytecode methods");
            out.println("    -tracemaps            trace stackmaps read from class files");
            out.println("    -traceDME             trace Dead Method Elimination");
            out.println("    -traceDCE             trace Dead Class Elimination");
            out.println("    -tracecallgraph       print table of methods and callees (only when doing DME)");
            out.println("    -tracefilter:<string> filter trace with simple string filter");
        }
    }
    
   /**
     * {@inheritDoc}
     */
    public void printOptionProperties(PrintStream out, boolean asParameters) {
        for (int i = 0; i < Arg.translatorArgs.length; i++) {
           Arg.translatorArgs[i].printOne(out, asParameters);
        }
    }
    

    public boolean processOption(String arg) {
        if (arg.startsWith("-")) {
            for (int i = 0; i < Arg.translatorArgs.length; i++) {
                Arg translatorArg = Arg.translatorArgs[i];
                String optionStr = translatorArg.getOptionName();
                if (arg.startsWith(optionStr)) {
                    String val = arg.substring(optionStr.length()).toUpperCase();
                    VM.setProperty(translatorArg.getPropertyName(), val);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isOption(String arg) {
        if (arg.startsWith("-")) {
            for (int i = 0; i < Arg.translatorArgs.length; i++) {
                if (arg.startsWith(Arg.translatorArgs[i].getOptionName())) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /*---------------------------------------------------------------------------*\
     *                                   Misc                                    *
    \*---------------------------------------------------------------------------*/

    public Suite getSuite() {
        return suite;
    }
    
    public int getSuiteType() {
        return suiteType;
    }
    
    public int getTranslationStrategy() {
        return translationStrategy;
    }

    /*---------------------------------------------------------------------------*\
     *                    Class lookup, creation and interning                   *
    \*---------------------------------------------------------------------------*/

    /**
     * The table of class files for classes.
     */
    private Hashtable classFiles;

    /**
     * Gets the array dimensionality indicated by a given class name.
     *
     * @return  the number of leading '['s in <code>name</code>
     */
    public static int countArrayDimensions(String name) {
        int dimensions = 0;
        while (name.charAt(dimensions) == '[') {
            dimensions++;
        }
        return dimensions;
    }

    /**
     * Gets the class file corresponding to a given instance class. The
     * <code>klass</code> must not yet be converted and it must not be a
     * {@link Klass#isSynthetic() synthetic} class.
     *
     * @param   klass  the instance class for which a class file is requested
     * @return  the class file for <code>klass</code>
     */
    ClassFile getClassFile(Klass klass) {
        Assert.that(!klass.isSynthetic(), "synthethic class has no classfile");
        String name = klass.getName();
        ClassFile classFile = (ClassFile)classFiles.get(name);
        if (classFile == null) {
            classFile = new ClassFile(klass);
            classFiles.put(name, classFile);
        }
        return classFile;
    }

    /**
     * Like
     * <code>klass</code> must not yet be converted and it must not be a
     * {@link Klass#isSynthetic() synthetic} class.
     *
     * @param   klass  the instance class for which a class file is requested
     * @return  the class file for <code>klass</code>, or null if that ClassFile has not been translated by this translator.
     */
    ClassFile lookupClassFile(Klass klass) {
        Assert.that(!klass.isSynthetic(), "synthethic class has no classfile");
        ClassFile classFile = (ClassFile)classFiles.get(klass.getInternalName());
        return classFile;
    }

	public String getLastClassName() {
		if (lastClassNameStack.empty()) {
			return null;
		}
		return (String) lastClassNameStack.peek();
	}

    /**
     * Gets the connection that is used to find the class files.
     *
     * @return  the connection that is used to find the class files
     */
    public ClasspathConnection getClassPath() {
        return classPath;
    }

    /*---------------------------------------------------------------------------*\
     *                     Class loading and resolution                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Loads a class's defintion from a class file.
     *
     * @param  classFile  the class file definition to load
     */
    private void load(final ClassFile classFile) {
        final ClassFileLoader loader = new ClassFileLoader(this);
        ComputationTimer.time("loading", new ComputationTimer.Computation() {
            public Object run() {
                loader.load(classFile);
                return null;
            }
        });
    }

    /**
     * Load and converts the closure of classes in the current suite.
     */
    public void computeClosure() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int cno = 0 ; cno < suite.getClassCount() ; cno++) {
                Klass klass = suite.getKlass(cno);

                if (klass == null) {
                    continue;  // if not floats, or if deferred errors, there can be some missing classes
                }

                if (klass.getState() < Klass.STATE_LOADED) {
                    load(klass);
                    changed = true;
                }
                if (klass.getState() < Klass.STATE_CONVERTING) {
                    convert(klass);
                    changed = true;
                }
            }
        }
    }
    
    public byte [] getResourceData(String name) {
        Assert.that(VM.isHosted() || VM.getCurrentIsolate().getLeafSuite() == suite);
        try {
            byte[] bytes = classPath.getBytes(name);
            ResourceFile resourceFile = new ResourceFile(name, bytes);
            suite.installResource(resourceFile);
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }
     
    /*---------------------------------------------------------------------------*\
     *                           Reversable parameters                           *
    \*---------------------------------------------------------------------------*/

    public static final boolean REVERSE_PARAMETERS = /*VAL*/true/*REVERSE_PARAMETERS*/;

    /*---------------------------------------------------------------------------*\
     *                          Debugging                                         *
    \*---------------------------------------------------------------------------*/

    public static void trace(MethodBody mb) {
        if (Translator.TRACING_ENABLED ) {
            Method method = mb.getDefiningMethod();
            Tracer.traceln("++++ Method for " + method + " ++++");
            new MethodBodyTracer(mb).traceAll();
            Tracer.traceln("---- Method for " + method + " ----");
        }
    }

}
