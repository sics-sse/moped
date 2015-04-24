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

package com.sun.squawk;



/**
 * An instance of <code>ClassFileConstantField</code> encapsulates all the
 * symbolic information of a field declaration in a class file that has a
 * ConstantValue attribute.
 * This class is provided for a subsystem (such as the translator) that
 * loads a class definition from a class file.
 *
 */
public final class ClassFileConstantField extends ClassFileField {

    /**
     * The primitive value in the ConstantValue attribute.
     */
    final long primitiveConstantValue;

    /**
     * The String value in the ConstantValue attribute.
     */
    final String stringConstantValue;

    /**
     * Creates a new <code>ClassFileConstantField</code> instance for a field with a
     * primitive ConstantValue attribute.
     *
     * @param   name          the name of the field
     * @param   modifiers     the modifiers of the field
     * @param   type          the type of the field
     * @param   constantValue the primitive constant value (as a long)
     */
    public ClassFileConstantField(String name, int modifiers, Klass type, long constantValue) {
        super(name, modifiers, type);
        this.primitiveConstantValue = constantValue;
        this.stringConstantValue = null;
    }

    /**
     * Creates a new <code>ClassFileConstantField</code> instance for a field with a
     * String ConstantValue attribute.
     *
     * @param   name          the name of the field
     * @param   modifiers     the modifiers of the field
     * @param   type          the type of the field
     * @param   constantValue the string constant value
     */
    public ClassFileConstantField(String name, int modifiers, Klass type, String constantValue) {
        super(name, modifiers, type);
        this.primitiveConstantValue = 0;
        this.stringConstantValue = constantValue;
    }
}
