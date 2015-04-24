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

import com.sun.squawk.builder.gen.*;
import com.sun.squawk.builder.Build;
import com.sun.squawk.builder.BuildException;
import com.sun.squawk.builder.Command;
import com.sun.squawk.builder.util.FileSet;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import com.sun.squawk.builder.gen.Generator.*;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;

///**
// * Exception thrown when error occurs reading/parsing/writing JNA class files.
// */
//class JNAGenException extends Exception {
//    JNAGenException(String msg) {
//            super(msg);
//    }
//}

/**
 * A tool that reads Java interfaces and classes that describe C functions, structures, and constants
 * to import from C. JNAGen then generates Java classes that defines Java access to the
 * C functions, structures, and constants.
 * 
 * This is part of CLDC Java Native Access
 * 
 * 
 * @TODO: Support C++ better by:
 *    - handling name mangling
 *    - handle calling C++ methods by creating "pointer to member" values?
 *       - We'd need these at runtime on the target though. Serialize the data at JNAGen-time into a Java array, then 
 *         at runtime recreate the "pointer to member" from Java data?
 * 
 * @see com.sun.cldc.jna
 */
public class JNAGenDirect extends Command {
    public final static String GEN_CLASS_SUFFIX = "Impl";

    public final static int TAB = 4; // number of spaces to indent generated Java code by, per-level

    /**
     * The dir containing the declaration src files, class files. and C build files. Optionally the generated Java files.
     */
    File baseDir;
    
    /**
     * The dir containing the generated Java files.
     */
    File dstDir;
    String classpath;
    
    PrintWriter out;
    
    public JNAGenDirect(Build env, File baseDir, File dstDir, String classpath) {
        super(env, "JNAGenDirect");
        this.baseDir = baseDir;
        this.dstDir = dstDir;
        this.classpath = classpath;
    } 
    
    void indent(int n) {
        for (int i = 0; i < n; i++) {
            out.print(' ');
        }
    }
    
    /**
     * Print a line with "indent" containing a "printf" of the String "line"
     * @param indent spaces to indent in the "printf"
     * @param line the text to print
     */
    void metaPrint(int indent, String line) {
        indent(4);
        indent(indent * TAB);
        out.println(line);
    }
    
    /**
     * Print a line with "indent" containing a "printf" of the String "line"
     * @param indent spaces to indent in the "printf"
     * @param line the text to print
     */
    void metaPrintln(int indent, String line) {
        indent(4);
        indent(indent * TAB);
        out.println(line);
    }
    
//    /**
//     * Print a line with "indent" containing a "printf" of the String "line", with additional printf arguments.
//     * @param indent spaces to indent before the "printf"
//     * @param line the text to print
//     */
//    void metaPrint(int indent, String line, String[] printfArgs) {
//            indent(4);
//            indent(indent * TAB);
//            out.println(line);
//            for (int i = 0; i < printfArgs.length; i++) {
//                out.print(", ");
//                out.print(printfArgs[i]);
//            }
//            out.println(");");
//    }
//
//    /**
//     * Print a line with "indent" containing a "printf" of the String "line", with additional printf arguments.
//     * @param indent spaces to indent before the "printf"
//     * @param line the text to print
//     */
//    void metaPrintln(int indent, String line, String[] printfArgs) {
//            indent(4);
//            out.print("fprintf(out, \"");
//            indent(indent * TAB);
//            out.print(line + "\\n\"");
//            for (int i = 0; i < printfArgs.length; i++) {
//                out.print(", ");
//                out.print(printfArgs[i]);
//            }
//            out.println(");");
//    }
    
    
    void metaPrintln(int indent, String[] lines) {
        for (int i = 0; i < lines.length; i++) {
            metaPrintln(indent, lines[i]);
        }
    }
    
    /**
     * Prints code to generate the the standard Squawk VM copyright message from C.
     * 
     * This is the viral nature of the GPL enshrined in C!
     * 
     * @param out  where to print the message
     */
    final void printCopyright(InterfaceDecl interfaceDecl) {
        out.println("static FILE* out;");
        out.println("");
        out.println("void printCopyright() {");
        metaPrintln(0, Generator.COPYRIGHT_LINES);
        metaPrintln(0, "");
        metaPrintln(0, "/* **** GENERATED FILE -- DO NOT EDIT ****");
        metaPrintln(0, " *      generated by " + this.getClass().getName());
        metaPrintln(0, " *      from the CLDC/JNA Interface class " + interfaceDecl.interfaceClass.getName());
        metaPrintln(0, " */");
        metaPrintln(0, "");
        out.println("}\n");
    }

    private void printConstructor(InterfaceDecl interfaceDecl, int level) {
        metaPrintln(level, "public " + interfaceDecl.interfaceClass.getSimpleName() + GEN_CLASS_SUFFIX + "() {");
        level++;
        metaPrintln(level, "NativeLibrary jnaNativeLibrary = Native.getLibraryLoading();");

        for (String initializer : fieldInitializers) {
            metaPrintln(level, initializer);
        }
        fieldInitializers.clear();
        level--;
        metaPrintln(level, "}");
        metaPrintln(level, "");
    }
    
    private void printIncludes(InterfaceDecl interfaceDecl) {
        out.println("#include <stddef.h>");
        out.println("#include <stdlib.h>");
        out.println("#include <stdio.h>");
        out.println("#include <errno.h>");

        for (String include : interfaceDecl.includes) {
            out.println("#include " + include);
        }
        out.println("");
    }
    
    private void printPackages(InterfaceDecl interfaceDecl) {
        metaPrintln(0, "package " + interfaceDecl.interfaceClass.getPackage().getName() + ";");
        metaPrintln(0, "");
        metaPrintln(0, "import com.sun.cldc.jna.*;");
        metaPrintln(0, "import com.sun.cldc.jna.ptr.*;");
       // metaPrintln(0, "import " + interfaceDecl.interfaceClass.getName() + ".*;");
        metaPrintln(0, "");
        out.println();
    }
    
    /* only supprto top-level libraryName defn */
    private void printLibraryDefinition(InterfaceDecl interfaceDecl) {        
//        if (interfaceDecl.libraryName == null) {
//            //out.println("    /* used default library */");
//            metaPrintln(1, "public final static NativeLibrary NATIVE_LIBRARY = NativeLibrary.getDefaultInstance();");
//        } else {
//            metaPrintln(1, "public final static NativeLibrary NATIVE_LIBRARY = NativeLibrary.getInstance(\\\"" + interfaceDecl.libraryName + "\\\");");
//        }
//        metaPrintln(0, "");
//        out.println();
    }
    
    /*------------------------- DEFINES --------------------------*/
    
//    private void printADefine(Field f, String format, int level) {
//        String[] args = new String[1];
//        args[0] = f.getName();
//        metaPrintln(level, "public final static " + f.getType().getSimpleName() + " " + f.getName() + " = " + format + ";", args);
//    }
//
//     private void printAnIfDefDefine(Field f, int level) {
//         out.println("#ifdef " + f.getName());
//         metaPrintln(level, "public final static " + f.getType().getSimpleName() + " " + f.getName() + " = true;");
//         out.println("#else");
//         metaPrintln(level, "public final static " + f.getType().getSimpleName() + " " + f.getName() + " = false;");
//         out.println("#endif");
//    }
//
//    private void printASizeOfDefine(Field f, String format, int level) throws JNAGenException {
//        String[] args = new String[1];
//        int endPos = f.getName().indexOf("_SIZEOF");
//        if (endPos < 1) {
//            throw new JNAGenException("Fields using Library.SIZEOF must have names that end with _SIZEOF: " + f);
//        }
//        String fieldName = f.getName().substring(0, endPos);
//        args[0] = "sizeof(" + fieldName + ")";
//        metaPrintln(level, "public final static " + f.getType().getSimpleName() + " " + f.getName() + " = " + format + ";", args);
//    }
        
    private void printDefines(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        if (interfaceDecl.defines.size() == 0) {
            return;
        }
        
        metaPrintln(level, "/*----------------------------- defines -----------------------------*/");
        metaPrintln(0, "");
        
//        // Field[] fields = interfaceClass.getDeclaredFields();
//        for (Field f : interfaceDecl.defines) {
//            try {
//                if (f.getType().equals(Boolean.TYPE) && f.getBoolean(null) == Library.DEFINED) {
//                    printAnIfDefDefine(f, level);
//                } else if (f.getType().equals(Integer.TYPE)) {
//                    switch (f.getInt(null)) {
//                        case Library.IMPORT:
//                            printADefine(f, "%d", level);
//                            break;
//                        case Library.SIZEOF:
//                            printASizeOfDefine(f, "%d", level);
//                            break;
//                        default:
//                            throw new JNAGenException("JNAGEn cannot handle fields of type " + f.getType() + " for field " + f);
//                    }
//                } else if (f.getType().equals(Long.TYPE)) {
//                    printADefine(f, "%dll", level);
//                } else if (f.getType().equals(String.class)) {
//                    printADefine(f, "\"%s\"", level);
//                } else if (Library.class.isAssignableFrom(f.getType())) {
//                    // ignore
//                } else {
//                    throw new JNAGenException("JNAGEn cannot handle fields of type " + f.getType() + " for field " + f);
//                }
//            } catch (IllegalArgumentException ex) {
//                ex.printStackTrace();
//            } catch (IllegalAccessException ex) {
//                ex.printStackTrace();
//            }
//        }
        metaPrintln(0, "");
        out.println();
    }
   
    /*------------------------- GLOBAL VARIABLES --------------------------*/
    Vector<String> fieldInitializers = new Vector<String>();

    private static String getPtrName(String nativeName) {
        return nativeName + "Ptr";
    }

    private void printVarPtr(InterfaceDecl interfaceDecl, String nativeName, int varSize, int level) {
        String varDeclStr = "private final Pointer " + getPtrName(nativeName) + ";";
        String varInitStr = getPtrName(nativeName) + " = jnaNativeLibrary.getGlobalVariableAddress(\\\"" + nativeName + "\\\", " + varSize + ");";

        fieldInitializers.add(varInitStr);
        metaPrintln(level, varDeclStr);
        metaPrintln(level, "");
    }
    
    /**
     * Return a string containing the Java code to get a value of type "valtype" from the
     * pointer "ptrName" using offset.
     * @param valType
     * @param ptrName
     * @param offset
     * @return a Java expression that gets the value
     */
    private String getValue(Class valType, String ptrName, int offset) throws JNAGenException {
        if (valType.equals(Integer.TYPE)) {
            return ptrName + ".getInt(" + offset + ")";
        } else if (valType.equals(Long.TYPE)) {
            return ptrName + ".getLong(" + offset + ")";
        } else if (valType.equals(String.class)) {
            return ptrName + ".getString(" + offset + ")";
        } else if (valType.equals(Pointer.class)) {
            return ptrName + ".getPointer(" + offset + ")";
        } else {
            throw new JNAGenException("JNAGEn cannot get values of type " + valType);
        }
    }
    
    /**
     * Return a string containing the Java code to get a value of type "valtype" from the
     * pointer "ptrName" using offset.
     * 
     * @param valtype Class of the value
     * @param ptrName name of the pointer
     * @param valName the name of the value to set
     * @param offset
     * @return a Java expression that sets the value
     */
    private String setValue(Class valType, String ptrName, String valName, int offset) throws JNAGenException {
        if (valType.equals(Integer.TYPE)) {
            return ptrName + ".setInt(" + offset + ", " + valName + ")";
        } else if (valType.equals(Long.TYPE)) {
            return ptrName + ".setLong(" + offset + ", " + valName + ")";
        } else if (valType.equals(String.class)) {
            return ptrName + ".setString(" + offset + ", " + valName + ")";
        } else if (valType.equals(Pointer.class)) {
            return ptrName + ".setPointer(" + offset + ", " + valName + ")";
        } else {
            throw new JNAGenException("JNAGEn cannot get values of type " + valType);
        }
    }

    private void printGetter(Method m, String nativeName, int level) throws JNAGenException {
        if (m.getParameterTypes().length != 0 ||
            m.getReturnType() == Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        
        printMethodDecl(m, level);
        metaPrintln(level + 1, "return " + getValue(m.getReturnType(), getPtrName(nativeName), 0) + ";");
        metaPrintln(level, "}");
        metaPrintln(0, "");
    }

    private void printSetter(Method m, String nativeName, int level) throws JNAGenException {
        if (m.getParameterTypes().length != 1 ||
            m.getReturnType() != Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        
        printMethodDecl(m, level);
        metaPrintln(level + 1, setValue(m.getParameterTypes()[0], getPtrName(nativeName), "arg0", 0) + ";");
        metaPrintln(level, "}");
        metaPrintln(0, "");
    }
    
    /*
     * Given that a getter and/or a setter is declared, rteurn the type of the value
     */
    private Class getVariableType(Method getter, Method setter) throws JNAGenException {
        if (getter != null) {
            return getter.getReturnType();
        } else if (setter != null) {
            return setter.getParameterTypes()[0];
        } else {
            throw new JNAGenException("getter or setter expected.");
        }
    }
    
    private int getVariableSize(Class c) throws JNAGenException {
          if (c.equals(Integer.TYPE)) {
            return 4;
        } else if (c.equals(Long.TYPE)) {
            return 8;
        } else if (c.equals(String.class)) {
            return 1024;
        } else if (c.equals(Pointer.class)) {
            return 4;
        } else {
            throw new JNAGenException("JNAGEn cannot get values of type " + c);
        }
    }

    private void printVars(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        if (interfaceDecl.globals.size() == 0) {
            return;
        }

        metaPrintln(level, "/*----------------------------- variables -----------------------------*/");
        metaPrintln(0, "");
        
        for (String varname : interfaceDecl.globals) {
            Method getter = interfaceDecl.getters.get(varname);
            Method setter = interfaceDecl.setters.get(varname);
            Class varType = getVariableType(getter, setter);
            int size = getVariableSize(varType);

            printVarPtr(interfaceDecl, varname, size, level);
            if (getter != null) {
                printGetter(getter, varname, level);
            }
            if (setter != null) {
                printSetter(setter, varname, level);
            }
        }
        metaPrintln(0, "");
        out.println();
    }
    
    /*------------------------- METHODS --------------------------*/
    
    private static String getPtrName(Member m) {
        return m.getName() + "Ptr";
    }
        
    private void printFunctionPtr(InterfaceDecl interfaceDecl, String nativeName, Method m, int level) {
        if (!functionPointers.contains(nativeName)) {
            String varDeclStr = "private final Function " + getPtrName(m) + ";";
            String varInitStr = getPtrName(m) + " = jnaNativeLibrary.getFunction(\\\"" + nativeName + "\\\");";

            fieldInitializers.add(varInitStr);

            functionPointers.add(nativeName);

            metaPrintln(level, varDeclStr);
            metaPrintln(level, "");
        }
    }
        
    /**
     * Print any declarations and initializations to create the native parameter, and return the name of the native parameter
     * @param type
     * @param i
     * @param level
     * @param firstHalf
     * @return name of the native param
     * @throws com.sun.cldc.jna.JNAGenException
     */
    private String createNativeParam(Class type, int i, int level, boolean firstHalf) throws JNAGenException {
        if (type.equals(String.class)) {
            metaPrintln(level,              "Pointer var" + i + " = Pointer.createStringBuffer(arg" + i + ");");
            return "var" + i;
        } else if (Structure.class.isAssignableFrom(type)) {
            metaPrintln(level,              "arg" + i + ".allocateMemory();");
            metaPrintln(level,              "arg" + i + ".write();");
            metaPrintln(level,              "Pointer var" + i + " = arg" + i + ".getPointer();");
            return "var" + i;
        } else if (PointerType.class.isAssignableFrom(type)) {
            metaPrintln(level,              "Pointer var" + i + " = arg" + i + ".getPointer();");
            return "var" + i;
        } else if (Pointer.class.isAssignableFrom(type)) {
            return "arg" + i;
        } else if (type.isPrimitive()) {
            if (type.equals(Long.TYPE) ||
                    type.equals(Double.TYPE)) {
                if (firstHalf) {
                    metaPrintln(level,      "int var" + i + " = (int)(arg" + i + " >>> 32);");
                    return "var" + i;
                } else {
                    metaPrintln(level,      "int var" + (i + 1) + " = (int)(arg" + i + ");");
                    return "var" + (i + 1);
                }
            } else {
                return "arg" + i;
            }
         } else if (type.isArray() && type.getComponentType().isPrimitive()) {
            metaPrintln(level,              "Pointer var" + i + " = PrivatePointer.createArrayBuffer(arg" + i + ");");
            return "var" + i;
         } else {
            throw new JNAGenException("Can't translate arguments of type " + type);
        }
    }
    
    private void cleanupNativeParam(Class type, int i, int level) throws JNAGenException {
        if (type.equals(String.class)) {
            metaPrintln(level,              "var" + i + ".free();");
        } else if (Structure.class.isAssignableFrom(type)) {
            metaPrintln(level,              "arg" + i + ".read();");
            metaPrintln(level,              "arg" + i + ".freeMemory();");
        } else if (type.isPrimitive()) {
        } else if (PointerType.class.isAssignableFrom(type)) {
        } else if (Pointer.class.isAssignableFrom(type)) {
        } else if (type.isArray() && type.getComponentType().isPrimitive()) {
            metaPrintln(level,              "var" + i + ".release();");
        } else {
            throw new JNAGenException("Can't translate arguments of type " + type);
        }
    }
    
    private void convertReturn(Class type, int level) throws JNAGenException {
        if (type.equals(String.class)) {
            metaPrintln(level,          type.getSimpleName() + " result = Function.returnString(result0);");
        } else if (Structure.class.isAssignableFrom(type)) {
            if (false) { // inline version...
                metaPrintln(level, type.getSimpleName() + " result = new " + type.getSimpleName() + "();");
                metaPrintln(level, "Pointer rp = new Pointer(result0, result.size());");
                metaPrintln(level, "result.useMemory(rp);");
                metaPrintln(level, "result.read();");
                metaPrintln(level, "result.release();");
            } else {
                metaPrintln(level, type.getSimpleName() + " result = (" + type.getSimpleName() + ")Function.returnStruct(" + type.getSimpleName() + ".class, result0);");
            }
        } else if (type.isPrimitive()) {
            if (type.equals(Boolean.TYPE)) {
                metaPrintln(level,      "boolean result = (result0 == 0) ? false : true;");
            } else if (type.equals(Long.TYPE) || type.equals(Double.TYPE)) {
                throw new JNAGenException("Can't translate return values of type " + type);
            } else if (type.equals(Float.TYPE)) {
                metaPrintln(level,      type.getSimpleName() + " result =  Float.intBitsToFloat(result0);");
            } else if (type.equals(Void.TYPE)) {
               // do nothing!
            } else {
                metaPrintln(level,      type.getSimpleName() + " result = (" +  type.getSimpleName() + ")result0;");
            }
        } else {
            throw new JNAGenException("Can't translate return values of type " + type);
        }
    }
    
    /**
     * The fastest way to get Java array data into C is to "pin" the object and pass a pointer.
     * Given that we should never call a C routine that blocks then this should be OK - GC should 
     * never be callable anyway. To be paranoid though, disable GC around all calls that need to "pin"
     * an object.
     * 
     * @param m method being called
     * @return true if the method will pin one or more objects over eth call to the C function.
     */
    private boolean shouldDisableGC(Method m) {
        Class[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class type = parameterTypes[i];
            if (type.isArray() && type.getComponentType().isPrimitive()) {
                return true;
            }
        }
        return false;
    }
    
    /***
     * Given a method, print code to do any translation of Java->C parameters, and return an array of strings naming the native parameter variables.
     * @param parameterTypes
     * @return  array of strings naming the native parameter variables.
     */
    private String[] createNativeParams(Method m, int level) throws JNAGenException {
        Class[] parameterTypes = m.getParameterTypes();
        int numNativeParamss = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class type = parameterTypes[i];
            numNativeParamss++;
            if (type.equals(Long.TYPE) ||
                type.equals(Double.TYPE)) {
                numNativeParamss++;
            }
        }
        
        String[] result = new String[numNativeParamss];
        int j = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class type = parameterTypes[i];
            result[j++] = createNativeParam(type, i, level, true);
            if (type.equals(Long.TYPE) ||
                type.equals(Double.TYPE)) {
                result[j++] = createNativeParam(type, i, level, false);
            }
        }
        return result;
    }
    
    private void cleanUpNativeParams(Method m, int level) throws JNAGenException {
        Class[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            cleanupNativeParam(parameterTypes[i], i, level);
        }
    }

    private void printMethodDecl(Method m, int level) {
        StringBuffer argsStr = new StringBuffer("public " + m.getReturnType().getSimpleName() + " " + m.getName() + "(");
        Class[] parameterTypes = m.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i != 0) {
                argsStr = argsStr.append(", ");
            }
            argsStr = argsStr.append(parameterTypes[i].getSimpleName()).append(" arg" + i);
        }
        argsStr = argsStr.append(") {");
        metaPrintln(level, argsStr.toString());
    }
     
//    private void printGlobalGetter(InterfaceDecl interfaceDecl, Method m, String nativeName, int level) throws JNAGenException {
//            metaPrint(level + 1, "int result0 = ");
//            metaPrint(0, getPtrName(m) + ".call" + numParams + "(");
//            for (int i = 0; i < numParams; i++) {
//                String comma = i != 0 ? ", " : "";
//                metaPrint(0, comma + nativeParams[i]);
//            }
//            metaPrintln(0, ");");
//            convertReturn(m.getReturnType(), level);
//            cleanUpNativeParams(m, nativeParams, level);
//            metaPrintln(level + 1, "return result;");
//
//            metaPrintln(level, "}");
//            metaPrintln(level, "");
//        } catch (JNAGenException ex) {
//            System.err.println(ex.getMessage());
//            System.err.println("While importing method " + m);
//            throw ex;
//        }
//    }
//    
//        private void printGlobalSetter(InterfaceDecl interfaceDecl, Method m, int level) throws JNAGenException {
//        try {
//            printFunctionPtr(interfaceDecl, m, level);
//            printMethodDecl(m, level);
//
//            Class[] parameterTypes = m.getParameterTypes();
//            int numParams = parameterTypes.length;
//            String[] nativeParams = createNativeParams(m, level);
//            metaPrint(level + 1, "int result0 = ");
//            metaPrint(0, getPtrName(m) + ".call" + numParams + "(");
//            for (int i = 0; i < numParams; i++) {
//                String comma = i != 0 ? ", " : "";
//                metaPrint(0, comma + nativeParams[i]);
//            }
//            metaPrintln(0, ");");
//            convertReturn(m.getReturnType(), level);
//            cleanUpNativeParams(m, nativeParams, level);
//            metaPrintln(level + 1, "return result;");
//
//            metaPrintln(level, "}");
//            metaPrintln(level, "");
//        } catch (JNAGenException ex) {
//            System.err.println(ex.getMessage());
//            System.err.println("While importing method " + m);
//            throw ex;
//        }
//    }
            
    /**
     * Create a method that calls out to C code.
     * @param interfaceDecl
     * @param m method
     * @param level nesting level
     * @throws com.sun.cldc.jna.JNAGenException
     */
    private void printMethod(InterfaceDecl interfaceDecl, String nativeName, Method m, int level) throws JNAGenException {
        try {
            boolean disableGC = shouldDisableGC(m);
            boolean isVoid = m.getReturnType().equals(Void.TYPE);

            printFunctionPtr(interfaceDecl, nativeName, m, level);
            printMethodDecl(m, level);
            
            level++;
                        
            if (disableGC) {
                metaPrintln(level, "boolean oldState = PrivatePointer.setUpArrayBufferState();");
                metaPrintln(level, "/*------------------- DISABLE GC: ---------------------------*/");
//              metaPrintln(level, "try {");
//              level++;
            }

            String[] nativeParams = createNativeParams(m, level);
            int numParams = nativeParams.length; // may be larger than parameterTypes.length for longs and double parameters
            StringBuffer callStr;
            if (isVoid) {
               callStr = new StringBuffer(getPtrName(m) + ".call" + numParams + "(");
            } else {
               callStr = new StringBuffer("int result0 = " + getPtrName(m) + ".call" + numParams + "(");
            }
            for (int i = 0; i < numParams; i++) {
                if (i != 0) {
                    callStr = callStr.append(", ");
                }
                callStr = callStr.append(nativeParams[i]);
            }
            metaPrintln(level, callStr.append(");").toString());
            convertReturn(m.getReturnType(), level);
            cleanUpNativeParams(m, level);
            
            if (disableGC) {
//              level--;
//              metaPrintln(level, "} finally {");
                metaPrintln(level+1, "PrivatePointer.tearDownArrayBufferState(oldState);");
                metaPrintln(level+1, "/*------------------- ENABLE GC: ---------------------------*/");
//              metaPrintln(level, "}");
            }

            if (!isVoid) {
                metaPrintln(level, "return result;");
            }
            level--;
            metaPrintln(level, "}");
            metaPrintln(level, "");
        } catch (JNAGenException ex) {
            System.err.println(ex.getMessage());
            System.err.println("While importing method " + m);
            throw ex;
        }
    }
    
    /**
     * Create the methods that call out to C code for the given class.
     * 
     * @param interfaceDecl
     * @param level nesting level
     * @throws com.sun.cldc.jna.JNAGenException
     */
    private void printMethods(InterfaceDecl interfaceDecl, int level) throws JNAGenException {   
        if (interfaceDecl.methods.size() == 0) {
            return;
        }
        
        metaPrintln(level, "/*----------------------------- methods -----------------------------*/");
        for (Method m : interfaceDecl.methods.keySet()) {
            String nativeName = interfaceDecl.methods.get(m);
            printMethod(interfaceDecl, nativeName, m, level);
        }
        out.println();
    }
        
    /*------------------------- Structures  --------------------------*/
    
    private static String getNativeTypeName(Class c) {
        // TODO: Allow type name mappings....
        return "struct " + c.getSimpleName();
    }
    
    private static String getNativeTypeName(Field f) {
        return getNativeTypeName(f.getDeclaringClass());
    }
    
    /**
     * @param f field
     * @return a String that C will evaluate as the byte offset of this field
     */
    private static String getOffsetStr(Field f) {
        return "offsetof(" + getNativeTypeName(f) + ", " + f.getName() + ")";
    }

     /**
     * @param f field
     * @return a String that C will evaluate as the byte size of this field
     */
    private static String getSizeStr(Field f) {
        return "SIZEOF(" + getNativeTypeName(f) + ", " + f.getName() + ")";
    }
    
    /**
     * @param c  class that describes a C struct
     * @return a String that C will evaluate as the size of a struct in bytes
     */
    private static String getSizeofStr(Class c) {
        return "sizeof(" + getNativeTypeName(c) + ")";
    }
    
    private void printFieldDecl(Field f, int level) {
        metaPrintln(level, "public " + f.getType().getSimpleName() + " " + f.getName() + ";");
    }
    
    void startIfDefField(Field f) {
        IfDef ifdef = f.getAnnotation(IfDef.class);
        IfNDef ifndef = f.getAnnotation(IfNDef.class);
        boolean notDef = false;
        String ifdefStr = null;
        if (ifdef != null) {
            ifdefStr = ifdef.value();
        } else if (ifndef != null) {
            notDef = true;
            ifdefStr = ifndef.value();
        }
        if (ifdefStr != null) {
            if (notDef) {
                out.println("#ifndef " + ifdefStr);
            } else {
                out.println("#ifdef " + ifdefStr);
            }
        }
    }

    void endIfDefField(Field f) {
        IfDef ifdef = f.getAnnotation(IfDef.class);
        IfNDef ifndef = f.getAnnotation(IfNDef.class);
        if (ifdef != null || ifndef != null) {
            out.println("#endif");
        }
    }

//    private void printFieldReader(Field f, int level) {
//        Class type = f.getType();
//
//        //String getter = "UNKNOWN";
//        if (type.isPrimitive()) {
//            if (type.equals(Integer.TYPE)) {
//                //getter = "getInt";
//            } else if (type.equals(Long.TYPE)) {
//                //getter = "getLong";
//            } else if (type.equals(Byte.TYPE)) {
//                //getter = "getByte";
//            } else if (type.equals(Short.TYPE)) {
//                //getter = "getShort";
//            } else {
//                throw new RuntimeException("Can't handle fields of type " + type + " in field " + f);
//            }
//        } else {
//            throw new RuntimeException("Can't handle fields of type " + type + " in field " + f);
//        }
//        startIfDefField(f);
//        String getter = "getGetter(" + getSizeStr(f) + ")";
//
//        metaPrintln(level + 1, "o." + f.getName() + " = p.%s(%d);", new String[]{getter, getOffsetStr(f)});
//        endIfDefField(f);
//    }
//
//    private void printFieldWriter(Field f, int level) {
//        Class type = f.getType();
//        //String setter = "UNKNOWN";
//        if (type.isPrimitive()) {
//            if (type.equals(Integer.TYPE)) {
//                //setter = "setInt";
//            } else if (type.equals(Long.TYPE)) {
//                //setter = "setLong";
//            } else if (type.equals(Byte.TYPE)) {
//                //setter = "setByte";
//            } else if (type.equals(Short.TYPE)) {
//                //setter = "setShort";
//            } else {
//                throw new RuntimeException("Can't handle fields of type " + type + " in field " + f);
//            }
//        } else {
//            throw new RuntimeException("Can't handle fields of type " + type + " in field " + f);
//        }
//        startIfDefField(f);
//        String setter = "getSetter(" + getSizeStr(f) + ")";
//        String cast = "actualJavaType(" + getSizeStr(f) + ")";
//        metaPrintln(level + 1, "p.%s(%d, (%s)o." + f.getName() + ");", new String[]{setter, getOffsetStr(f), cast});
//        endIfDefField(f);
//    }
        
    private void printStructSupport(StructureDecl structDecl, int level) throws JNAGenException {
//        String structName = structDecl.interfaceClass.getSimpleName();
//        String structImplName = structName + "Impl";
//        if (structDecl.instanceVars.size() == 0) {
//            return;
//        }
//
//        metaPrintln(level, "protected " + structImplName + "() {}");
//        metaPrintln(0, "");
//
//        if (!structDecl.hasMethod("size", StructureDecl.NO_PARAMS)) {
//            metaPrintln(level, "public int size() {");
//            metaPrintln(level + 1, "return %d;", new String[]{getSizeofStr(structDecl.interfaceClass)});
//            metaPrintln(level, "}");
//            metaPrintln(0, "");
//        }
//
//        if (!structDecl.hasMethod("read", StructureDecl.NO_PARAMS)) {
//            metaPrintln(level, "public void read() {");
//            metaPrintln(level + 1, "Pointer p = getPointer();");
//            metaPrintln(level + 1,  structName + " o = ("  + structName + ")this;"); // downcast from FooImpl to Foo, to get access to fields
//            for (Field f : structDecl.instanceVars) {
//                printFieldReader(f, level);
//            }
//            metaPrintln(level, "}");
//            metaPrintln(0, "");
//        }
//
//        if (!structDecl.hasMethod("write", StructureDecl.NO_PARAMS)) {
//            metaPrintln(level, "public void write() {");
//            metaPrintln(level + 1, "Pointer p = getPointer();");
//            metaPrintln(level + 1,  structName + " o = ("  + structName + ")this;"); // downcast from FooImpl to Foo, to get access to fields
//            for (Field f : structDecl.instanceVars) {
//                printFieldWriter(f, level);
//            }
//            metaPrintln(level, "}");
//            metaPrintln(0, "");
//        }

    }
               
    /*------------------------- CLASSES --------------------------*/

    private void printClassHeader(InterfaceDecl interfaceDecl, int level) {
        String baseName = interfaceDecl.interfaceClass.getSimpleName();
        if (level == 1) {
            metaPrintln(level - 1, "public class " + baseName + GEN_CLASS_SUFFIX + " implements " + baseName + " {");
        } else {
            StructureDecl structureDecl = (StructureDecl)interfaceDecl;
            if (structureDecl.providesSomeImpl()) {
                metaPrintln(level - 1, "public static abstract class " + baseName + GEN_CLASS_SUFFIX + " extends Structure {");
            } else {
                metaPrintln(level - 1, "public static class " + baseName + GEN_CLASS_SUFFIX + " extends Structure {");
            }
        }
        metaPrintln(level - 1, "");
        out.println();
    }
    
    private void printClassFooter(InterfaceDecl interfaceDecl, int level) {
        metaPrintln(level - 1, "}");
        metaPrintln(level - 1, "\\n");
        out.println();
    }
    
    private void printInnerClasses(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        for (Class innerClass : interfaceDecl.interfaceClass.getDeclaredClasses()) {
            if (innerClass.isInterface()) {
                InterfaceDecl innerDecl = new InterfaceDecl(innerClass);
                printClass(innerDecl, level + 1);
            } else if (Structure.class.isAssignableFrom(innerClass)) {
                StructureDecl structDecl = new StructureDecl(innerClass);
                printClass(structDecl, level + 1);
            } else {
                throw new JNAGenException("Class " + innerClass + " is not an interface or a subclass of Structure");
            }
        }
    }
        
    /**
     * Print all of the code for a class and it's inner classes
     * 
     * @param interfaceDecl
     * @param level 1 stands for top-level class.
     */
    private void printClass(InterfaceDecl interfaceDecl, int level) throws JNAGenException {
        printClassHeader(interfaceDecl, level);

        printDefines(interfaceDecl, level);

//        if (level == 1) {
//            printLibraryDefinition(interfaceDecl);
//        }
        printVars(interfaceDecl, level);
        printMethods(interfaceDecl, level);

        if (level == 1) {
            printConstructor(interfaceDecl, level);
        }
        if (interfaceDecl instanceof StructureDecl) {
            printStructSupport((StructureDecl)interfaceDecl, level);
        }
        printInnerClasses(interfaceDecl, level);

        printClassFooter(interfaceDecl, level);
    }
    
        /*------------------------- MAIN --------------------------*/
   private void printUtils() throws JNAGenException {
        out.println("char* getGetter(int size) {");
        out.println("    switch (size) {");
        out.println("        case 1: return \"getByte\";");
        out.println("        case 2: return \"getShort\";");
        out.println("        case 4: return \"getInt\";");
        out.println("        case 8: return \"getLong\";");
        out.println("        default: exit(1);");
        out.println("    }");
        out.println("}");
        out.println("");

        out.println("char* getSetter(int size) {");
        out.println("    switch (size) {");
        out.println("        case 1: return \"setByte\";");
        out.println("        case 2: return \"setShort\";");
        out.println("        case 4: return \"setInt\";");
        out.println("        case 8: return \"setLong\";");
        out.println("        default: exit(1);");
        out.println("    }");
        out.println("}");
        out.println("");

        out.println("char* actualJavaType(int size) {");
        out.println("    switch (size) {");
        out.println("        case 1: return \"byte\";");
        out.println("        case 2: return \"short\";");
        out.println("        case 4: return \"int\";");
        out.println("        case 8: return \"long\";");
        out.println("        default: exit(1);");
        out.println("    }");
        out.println("}");
        out.println("");

         /* #define SIZEOF(s,m) ((size_t) sizeof(((s *)0)->m)) */
        out.println("#define SIZEOF(s,m) ((size_t) sizeof(((s *)0)->m))");
    }
   private void printMain(InterfaceDecl interfaceDecl) throws JNAGenException {
        out.println("int main(int argc, char *argv[]) {");
        out.println("    if (argc < 2) {");
        out.println("        fprintf(stderr, \"Usage <cmd> out_file_name\\n\");");
        out.println("        exit(1);");
        out.println("    }");
        out.println("");
        out.println("    chmod(argv[1], 0222);"); // make existing file writable (if exists)
        out.println("    out = fopen(argv[1], \"w\");");
        out.println("    if (out == NULL) {");
        out.println("        fprintf(stderr, \"File create failed for %s, errno = %d\\n\", argv[1], errno);");
        out.println("        exit(1);");
        out.println("    }");
        out.println("");
        out.println("    printCopyright();");
        out.println("");
        
        printPackages(interfaceDecl);
        printClass(interfaceDecl, 1);
        
        out.println("    fclose(out);");
        out.println("    chmod(argv[1], 0444);");

        out.println("}\n");
    }

   HashSet<String> functionPointers;
    void generate(Class interfaceClass) {
        try {
            functionPointers = new HashSet<String>();
            InterfaceDecl interfaceDecl = new InterfaceDecl(interfaceClass);

            Generator.printCopyright(this.getClass(), out);
            printIncludes(interfaceDecl);
            printCopyright(interfaceDecl);
            printUtils();
            printMain(interfaceDecl);
        } catch (JNAGenException ex) {
            System.err.println(ex.toString());
            System.err.println("While importing library defined by " + interfaceClass);
        }
    }
    
    URL[] filesToURLs(File[] files) {
        URL[] result = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                result[i] = new URL("file:" + files[i].getPath() + "/");
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
    
    private String stripSuffix(String filename) {
        int index = filename.lastIndexOf('.');
        return filename.substring(0, index);
    }
    
    /**
     * Preprocess a given set of Java source files.
     *
     * @param  baseDir    the directory under which the "build" directory exists
     * @param  dstDir     the directory that will containing the generated Java files ("typically "native")
     * @param  classDirs  the set of directories that are searched recursively for the classes files to be imported
     * @return the preprocessor output directory
     */
    public File generate(File baseDir, File dstDir, File[] classDirs) {
        // Get the output directory
        final File buildDir = Build.mkdir(baseDir, "build");
        
        System.out.println("    Generating intermediate JNA files...");

        ClassLoader parent = this.getClass().getClassLoader(); // we need to load com.sun.cldc.jn.Library, which is in the same jar as this class...
        URLClassLoader loader = new URLClassLoader(filesToURLs(classDirs), parent);

        for (int i = 0; i != classDirs.length; ++i) {
            File classDir = classDirs[i];
            String classDirStr = classDir.getPath();
            //System.out.println("    Looking for class files in " + classDir + "...");

            FileSet fs = new FileSet(classDir, Build.JAVA_CLASS_SELECTOR);
            Iterator iterator = fs.list().iterator();
            while (iterator.hasNext()) {
                File inputFile = (File) iterator.next();
                if (inputFile.length() != 0 && inputFile.getName().indexOf('$') < 0) { // only look at outer classes. Inner classes will get sucked up by them
                    try {
                        String inFilePath = inputFile.getPath();
                        String classname = inFilePath.substring(classDirStr.length() + 1).replace(File.separatorChar, '.');
                        classname = stripSuffix(classname);
                        Class interfaceClass = Class.forName(classname, true, loader);

                        String outFileName = inputFile.getParent() + File.separator + stripSuffix(inputFile.getName()) + GEN_CLASS_SUFFIX + ".c";
                        File outputFile = fs.replaceBaseDir(new File(outFileName), buildDir);
                        Build.mkdir(outputFile.getParentFile());
                        System.out.println("        Creating " + outputFile);
                        out = new PrintWriter(outputFile);
                        generate(interfaceClass);
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } finally {
                        if (out != null) {
                            out.close();
                            out = null;
                        }
                    }
                }
            }

        }
        return buildDir;
    }
    
    @Override
    public void run(String[] args) throws BuildException {
        generate(baseDir, dstDir, new File[] {new File(baseDir, "classes")});
    }

}
