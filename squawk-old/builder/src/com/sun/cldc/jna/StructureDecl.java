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

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Information for a class delcaration that is a subclass of jna.Structure
 */
public class StructureDecl extends InterfaceDecl {

    ArrayList<Method> definedMethods;
    
    boolean providesSomeImpl;
    
        public static final Class[] NO_PARAMS = new Class[0];


    public StructureDecl(Class interfaceClass) throws JNAGenException {
        super(interfaceClass);
    }

    void processClass() throws JNAGenException {
        super.processClass();
        definedMethods = new ArrayList<Method>();
        
        Method[] meths = interfaceClass.getDeclaredMethods();
        for (int i = 0; i < meths.length; i++) {
            Method m = meths[i];

            if (!Modifier.isAbstract(m.getModifiers())) {
                definedMethods.add(m);
                if (isMethod(m, "size", NO_PARAMS)) {
                    providesSomeImpl = true;
                } else if (isMethod(m, "read", NO_PARAMS)) {
                    providesSomeImpl = true;
                } else if (isMethod(m, "write", NO_PARAMS)) {
                    providesSomeImpl = true;
                }
            }
        }
    }
    
    /**
     * Return true if this class implemenets at least one of the abstract methods in Structure.
     * @return
     */
    public boolean providesSomeImpl() {
        return providesSomeImpl;
    }
  
    /**
     * Return true if method "m" matches the given name and parameter types.
     * @param m
     * @param name
     * @param params
     * @return
     */
    static boolean isMethod(Method m, String name, Class[] params) {
        if (m.getName().equals(name)) {
            return Arrays.equals(m.getParameterTypes(), params);
        }
        return false;
    }
    
    /**
     * Return true if this class defines a method with teh given name and parameter list
     * @param name base name
     * @param params method parameter types
     * @return true if class defines the method
     */
    public boolean hasMethod(String name, Class[] params) {
        for (Method m : definedMethods) {
            if (m.getName().equals(name)) {
                return Arrays.equals(m.getParameterTypes(), params);
            }
        }
        return false;
    }

}
