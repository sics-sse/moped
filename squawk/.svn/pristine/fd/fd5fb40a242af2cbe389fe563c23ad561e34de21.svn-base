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
 * An instance of <code>ClassFileMember</code> encapsulates all the
 * common symbolic information of a field or method declaration in a class file.
 * This class is provided for a subsystem (such as the translator) that
 * loads a class definition from a class file.
 *
 */
public abstract class ClassFileMember {

    /**
     * The name of the field or method.
     */
    final private String name;

    /**
     * The modifiers of the field or method. This is a mask of the constants
     * defined in {@link Modifier}.
     */
    final int modifiers;

    /**
     * The offset computed for the field or method. See
     * {@link Member#getOffset() getOffset} for a description of the semantics
     * of the offset.
     */
    private int offset;

    /**
     * Constructor for subclasses.
     *
     * @param   name       the name of the field or method
     * @param   modifiers  the modifiers of the field or method
     */
    ClassFileMember(String name, int modifiers) {
        this.name = name;
        this.modifiers = modifiers;
    }

    /**
     * Gets the name of this field or method.
     *
     * @return the name of this field or method
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the fully qualified name of this field or method.
     *
     * @param definingClass  the class in which this field or method was defined
     * @return the fully qualified name of this field or method
     */
    public final String getFullyQualifiedName(Klass definingClass) {
        return definingClass.getInternalName() + "." + getName();
    }

    /**
     * Gets the modifiers of this field or method.
     *
     * @return a mask of the constants defined in {@link  Modifier}
     */
    public final int getModifiers() {
        return modifiers;
    }

    /**
     * Sets the offset of this field or method. This method is package-private
     * as it should only be called from {@link Klass}.
     *
     * @param offset  the offset of this field or method
     * @see   Member#getOffset()
     */
    final void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Gets the offset of this field or method. This method is package-private
     * as it should only be called from {@link KlassMetadata}.
     *
     * @return  the offset of this field or method
     * @see   Member#getOffset()
     */
    final int getOffset() {
        return offset;
    }

    /**
     * Determines if this field or method is a staic member of its class.
     *
     * @return  true if this is a staric class member
     */
    public final boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }
}
