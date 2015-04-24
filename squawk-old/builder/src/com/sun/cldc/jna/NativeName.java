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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The NativeName annotation can be applied to the top-level interface declarations,
 * variable declarations, method declarations, and structure definitions to specify a 
 * native name ("C name" ).
 * 
 * <h2>Details</h2>
 * <h3>Library Declaration</h3>
 * When a JNA Library interface declaration has a NativeName annotationm, the annotation
 * specifies the name of a C library ("dll" or "shared library") that
 * contains the code that the JNA Library declaration is accessing.
 *
 * The specified library name should be the generic, unadorned library name, such as
 * "ssl", not "libssl.so", etc.
 *  
 * Example:
 * 
 *    @NativeName("ssl")
 *    public interface SSL extends LibraryImport { ... }
 * 
 * <h3>Method and Variable Declaration</h3>
 * When a method declaration has a NativeName annotation, the annotation
 * specifies the name of a C function or global variable that the JNA method or variable shoudl map to.
 * 
 * <h3>Structure Declaration</h3>
 * When a structure declaration has a NativeName annotation, the annotation
 * specifies the name of the C struct that the JNA structure maps to.
 * 
 * @TODO What about constants?
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface NativeName {

    /**
     * Returns the native name of the element.
     * @return String
     */
    String value();
}
