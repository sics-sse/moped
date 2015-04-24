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

package com.sun.squawk.debugger.sdp;

import com.sun.squawk.debugger.DataType.FieldID;
import com.sun.squawk.*;

/**
 * A proxy for a field.
 *
 */
public final class ProxyField {

    /**
     * The proxied field.
     */
    private final Field field;

    /**
     * The JDWP identifier for the field.
     */
    private final FieldID id;

    /**
     * Creates a proxy for a field.
     *
     * @param id    the field's JDWP identifier
     * @param field the field
     */
    public ProxyField(FieldID id, Field field) {
        this.id = id;
        this.field = field;
    }

    /**
     * @return the proxied field
     */
    public Field getField() {
        return field;
    }

    /**
     * @return  the name of this field
     */
    public String getName() {
        return field.getName();
    }

    /**
     * @return the JNI signature of this field
     */
    public String getSignature() {
        return DebuggerSupport.getJNISignature(field);
    }

    /**
     * @return  the modifiers for this method
     * @see     Modifier#getJVMFielddModifiers
     */
    public int getModifiers() {
        return field.getModifiers() & Modifier.getJVMFieldModifiers();
    }

    public String toString() {
        return "<FIELD id: " + id + ", " + getName() + ": " + getSignature() + ">";
    }

    /**
     * @return  the JDWP identifier for this field
     */
    public FieldID getID() {
        return id;
    }
}
