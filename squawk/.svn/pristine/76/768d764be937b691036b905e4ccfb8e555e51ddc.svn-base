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

import com.sun.squawk.util.Assert;


/**
 * An instance of <code>Member</code> encapsulates the information about the
 * method or field of a class.
 *
 */
public abstract class Member {

    /**
     * The metadata of the class that declared this method or field.
     */
    final KlassMetadata metadata;

    /**
     * The index of this field or method within <code>metadata</code>.
     */
    final int id;

    /**
     * Gets the parser for this field or method's components.
     *
     * @return the parser for this field or method's components
     */
    final SymbolParser parser() {
        return metadata.getSymbolParser(id);
    }

    /**
     * Creates a new <code>Member</code>.
     *
     * @param  metadata the metadata of the class that declared the member
     * @param  id       the index of the member within <code>metadata</code>
     */
    Member(KlassMetadata metadata, int id) {
        this.metadata = metadata;
        this.id = id;
    }

    /**
     * Get the index of this field or method within <code>metadata</code>.
     *
     * @return the index
     */
    int getID() {
        return id;
    }


    /*---------------------------------------------------------------------------*\
     *             Access permissions and member property queries                *
    \*---------------------------------------------------------------------------*/

    /**
     * Determines if this method or field is public.
     *
     * @return  true if this method or field is public
     */
    public final boolean isPublic() {
        return Modifier.isPublic(parser().getModifiers());
    }

    /**
     * Determines if this method or field is package private.
     *
     * @return  true if this method or field is package private
     */
    public final boolean isPackagePrivate() {
        int modifiers = parser().getModifiers();
        return (modifiers & (Modifier.PUBLIC| Modifier.PRIVATE| Modifier.PROTECTED)) == 0;
    }

    /**
     * Determines if this method or field is static.
     *
     * @return  true if this method or field is static
     */
    public final boolean isStatic() {
        return Modifier.isStatic(parser().getModifiers());
    }

    /**
     * Determines if this method or field is final.
     *
     * @return  true if this method or field is final
     */
    public final boolean isFinal() {
        return Modifier.isFinal(parser().getModifiers());
    }

    /**
     * Determines if this method or field is protected.
     *
     * @return  true if this method or field is protected
     */
    public final boolean isProtected() {
        return Modifier.isProtected(parser().getModifiers());
    }

    /**
     * Determines if this method or field is private.
     *
     * @return  true if this method or field is private
     */
    public final boolean isPrivate() {
        return Modifier.isPrivate(parser().getModifiers());
    }

    /**
     * Determines if this method or field does not appear in any source code.
     *
     * @return  true if this method or field does not appear in any source code
     */
    public final boolean isSourceSynthetic() {
        return Modifier.isSourceSynthetic(getModifiers());
    }

    /*---------------------------------------------------------------------------*\
     *                       Method component getters                            *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the name of this field or method.<p>
     *
     * @return   the name of this field or method
     */
    public final String getName() {
        return parser().getName();
    }

    /**
     * Gets the fully qualified name of this field or method.
     *
     * @return the fully qualified name of this field or method
     */
    public final String getFullyQualifiedName() {
        return getDefiningClass().getInternalName() + "." + getName();
    }

    /**
     * Gets the offset for the member. The semantics of the offset is different
     * for each category of member:<p>
     * <table width="90%" border="1" cellpadding="5">
     *   <tr align="center">
     *     <th>Category</th>
     *     <th>Meaning</th>
     *   </tr>
     *   <tr>
     *     <td>static method</td>
     *     <td>The index into the static method table of this class at which
     *         the implementation of the static method is located.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td>virtual method</td>
     *     <td>The index into the vtable of this class at which
     *         the implementation of the virtual method is located.</td>
     *   </tr>
     *   <tr>
     *     <td>static field</td>
     *     <td>The offset (in words) from the address of the class's static field
     *         state at which the value of the static field is located.
     *     </td>
     *   </tr>
     *   <tr>
     *     <td>instance field</td>
     *     <td>The type-related offset from the address of an instance of this
     *         class at which the value of the instance field is located. For
     *         <code>byte</code> or <code>boolean</code> fields, the offset is
     *         in terms of bytes, for <code>char</code> or <code>short</code>
     *         fields, the offset is in terms of half-words and for all other
     *         fields, the offset is in terms of words (<code>double</code>
     *         and <code>long</code> fields are expressed as a pair of words).
     *     </td>
     *   </tr>
     * </table>
     *
     * @return   the offset for the member.
     */
    public final int getOffset() {
        return parser().getOffset();
    }

    /**
     * Gets the class that defined this field or method.
     *
     * @return   the member's type
     */
    public final Klass getDefiningClass() {
        return metadata.getDefinedClass();
    }

    /**
     * Gets the mask of access flags describing the access permissions and
     * other properties of this member. The returned value will be a mask of
     * constants defined in {@link Modifier}.
     *
     * @return the access flags for this member
     */
    public final int getModifiers() {
        return parser().getModifiers();
    }

    /**
     * Gets a String representation of this field or method.
     */
    public final String toString() {
        if (VM.isHosted()) {
            return Klass.toString(this, true);
        } else {
            return super.toString();
        }
    }

    /*---------------------------------------------------------------------------*\
     *                       Access permission checking                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Determines if this member is accessible from a given class.
     *
     * @param  klass the class to test for accessibility
     */
    public final boolean isAccessibleFrom(Klass klass) {
        return Klass.isAccessibleFrom(this, klass);
    }

    /**
     * Gets the hash code.
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        throw Assert.shouldNotReachHere(
/*if[DEBUG_CODE_ENABLED]*/
                "Should not be put in hashtables"
/*end[DEBUG_CODE_ENABLED]*/
                );
    }
}
