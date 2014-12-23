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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A parsed out version of the Class for easy code generation
 */
public class InterfaceDecl {

    Class interfaceClass;
    String[] includes;
    String libraryName;
    ArrayList<Field> defines;
    HashSet<String> globals;
    ArrayList<Field> instanceVars;
    ArrayList<Class> structs;
    HashMap<Method, String> methods;
    HashMap<String, Method> getters;
    HashMap<String, Method> setters;

    public InterfaceDecl(Class interfaceClass) throws JNAGenException {
        this.interfaceClass = interfaceClass;
        defines = new ArrayList<Field>();
        globals = new HashSet<String>();
        instanceVars = new ArrayList<Field>();
        structs = new ArrayList<Class>();
        methods = new HashMap<Method, String>();
        includes = new String[0];
        getters = new HashMap<String, Method>();
        setters = new HashMap<String, Method>();
        processClass();
    }

    void checkMatchingAccessors(Method getter, Method setter) throws JNAGenException {
        if (getter != null && setter != null) {
            if (getter.getReturnType() != setter.getParameterTypes()[0]) {
                throw new JNAGenException("The signature of the getter " + getter + " != the setter " + setter);
            }    
        }        
    }
    
    void processGetter(Method m, String nativeName) throws JNAGenException {
        if (m.getParameterTypes().length != 0 || m.getReturnType() == Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        checkMatchingAccessors(m, setters.get(nativeName));
        getters.put(nativeName, m);
    }

    void processSetter(Method m, String nativeName) throws JNAGenException {
        if (m.getParameterTypes().length != 1 ||
                m.getReturnType() != Void.class) {
            throw new JNAGenException("JNAGEn cannot handle setters of the form " + m);
        }
        checkMatchingAccessors(getters.get(nativeName), m);
        setters.put(nativeName, m);
    }

    void processClass() throws JNAGenException {
        
        // read class annotations:
        @SuppressWarnings("unchecked")
        Includes inclAnnot = (Includes) interfaceClass.getAnnotation(Includes.class);
        if (inclAnnot != null) {
            includes = inclAnnot.value();
            //includes = inclStr.split("[,\\s]+");
        }

        @SuppressWarnings("unchecked")
        NativeName libNameAnnot = (NativeName) interfaceClass.getAnnotation(NativeName.class);
        if (libNameAnnot != null) {
            libraryName = libNameAnnot.value();
        }
   
        // read fields:
        Field[] fields = interfaceClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            int modifier = f.getModifiers();
            if (Modifier.isStatic(modifier)) {
                if (Modifier.isFinal(modifier)) {
                    defines.add(f);
                } else {
                    throw new RuntimeException("not expecting non-final static fields in interface");
                }
            } else {
                instanceVars.add(f);
            }
        }
        
        // read methods:
        Method[] meths = interfaceClass.getDeclaredMethods();
        for (int i = 0; i < meths.length; i++) {
            Method m = meths[i];
            String nativeName = null;
            boolean doAccessor = false;
            boolean mayBeSetter = m.getName().indexOf("set") == 0;
            NativeName annot = m.getAnnotation(NativeName.class);
            if (annot != null) {
                nativeName = annot.value();
            }
            if (m.getAnnotation(GlobalVar.class) != null) {
                doAccessor = true;
            }

            if (nativeName == null) {
                if (doAccessor && mayBeSetter) {
                    nativeName = m.getName().substring(3);
                } else {
                    nativeName = m.getName();
                }
            }

            if (doAccessor) {
                globals.add(nativeName);
                if (mayBeSetter) {
                    processSetter(m, nativeName);
                } else {
                    processGetter(m, nativeName);
                }
            } else {
                if (Modifier.isAbstract(m.getModifiers())) {
                    methods.put(m, nativeName);
                }
            }
        }
    }
}
