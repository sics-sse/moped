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
 * An instance of <code>ClassFileField</code> encapsulates all the
 * symbolic information of a field declaration in a class file.
 * This class is provided for a subsystem (such as the translator) that
 * loads a class definition from a class file.
 *
 */
public class ClassFileField extends ClassFileMember {

    /**
     * A zero-length array of <code>ClassFileField</code>s.
     */
    public static final ClassFileField[] NO_FIELDS = {};

    /**
     * The type of this field.
     */
    private final Klass type;

    /**
     * Creates a new <code>ClassFileField</code> instance.
     *
     * @param   name       the name of the field
     * @param   modifiers  the modifiers of the field
     * @param   type       the type of the field
     */
    public ClassFileField(String name, int modifiers, Klass type) {
        super(name, modifiers);
        this.type = type;
    }

    /**
     * Gets the type of this field.
     *
     * @return  the type of this field
     */
    public final Klass getType() {
        return type;
    }
}
