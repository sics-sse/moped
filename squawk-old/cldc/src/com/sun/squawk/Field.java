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

import com.sun.squawk.vm.CID;

/**
 * An instance of <code>Field</code> encapsulates the information about the
 * field of a class. This includes the name of the field, its type, access
 * flags etc.
 *
 */
public final class Field extends Member {

    /**
     * Creates a new <code>Field</code>.
     *
     * @param  metadata the metadata of the class that declared the field
     * @param  id       the index of this field within <code>metadata</code>
     */
    Field(KlassMetadata metadata, int id) {
        super(metadata, id);
    }
    
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return false;
    }

    /*---------------------------------------------------------------------------*\
     *              Access permissions and member property queries               *
    \*---------------------------------------------------------------------------*/

    /**
     * Determines if this field is transient.
     *
     * @return  true if this field is transient
     */
    public boolean isTransient() {
        return Modifier.isTransient(parser().getModifiers());
    }

    /**
     * Determines if this field had a ConstantValue attribute in its class file
     * definition. Note that this does not necessarily mean that the field is 'final'.
     *
     * @return  if there is a constant value associated with this field
     */
    public boolean hasConstant() {
        return Modifier.hasConstant(parser().getModifiers());
    }


    /*---------------------------------------------------------------------------*\
     *                        Field component getters                            *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets this declared type of this field.<p>
     *
     * @return   this declared type of this field
     */
    public Klass getType() {
        return parser().getSignatureTypeAt(0);
    }

    /**
     * Gets the String constant value of this static field.
     *
     * @return  the value derived from the ConstantValue classfile attribute
     * @throws  IllegalArgumentException if this field did not have a ConstantValue
     *          attribute in its class file or if the constant is not a String
     */
    public String getStringConstantValue() throws IllegalArgumentException {
        if (!hasConstant() || getType().getSystemID() != CID.STRING) {
            throw new IllegalArgumentException();
        }
        return parser().getStringConstantValue();
    }

    /**
     * Gets the primitive constant value of this static field.
     *
     * @return  the value derived from the ConstantValue classfile attribute
     * @throws  IllegalArgumentException if this field did not have a ConstantValue
     *          attribute in its class file or if the constant is not a primitive value
     */
    public long getPrimitiveConstantValue() throws IllegalArgumentException {
        if (!hasConstant() || !getType().isPrimitive()) {
            throw new IllegalArgumentException();
        }
        return parser().getPrimitiveConstantValue();
    }
}
