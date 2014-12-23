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
 * The GlobalVar annotation can be applied to method declarations in a 
 * Library to indicate that the body of the method should be a getter or setter 
 * or a C global variable of a similar name.<p>
 * 
 * When applied to a method with no parameters and a non-void return type, 
 * a getter method will be generated for the C variable with
 * the same name as the method.<p>
 * 
 * When applied to a method with a name starting with "set", and 
 * with one parameters and a void return type, a setter method will be
 * generated for the C variable with the name as the method (ignoring teh prefix "set").<p>
 * 
 * All other cases will result in an error at code generation time.<p>
 * 
 * Example: Accessors for  the C variable "errno"
 * 
 *    @GlobalVar
 *    public int errno();
 * 
 *    @GlobalVar
 *    public void seterrno(int value);
 * 
 * The GlobalVar annotation can be used with the {@link NativeName} annotation to specify
 * a sperate natve name for the global variable getter and/or setter.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalVar {
}