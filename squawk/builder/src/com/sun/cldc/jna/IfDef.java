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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The IfDef annotation can be applied to field, 
 * variable, method, and structure definitions to specify a 
 * to specify that the item should be conditionally imported 
 * only if the named C macro is defined.
 * 
 * An "unimported" field, variable, method, or structure may have a Java declaration,
 * but the definition is unspecified. Any use of the unimported name may result in an runtime exception.
 * 
 * @TODO Only implemented for constant definitions and structure fields for now...
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IfDef {
    /**
     * Returns the name of the C macro that the element is dependent on.
     * @return String
     */
    String value();
}
