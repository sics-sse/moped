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

package com.sun.squawk.translator.ci;

import com.sun.squawk.*;

/**
 * An instance of <code>MethodSignature</code> encapsulates a method's signature.
 *
 */
public final class MethodSignature {

    /**
     * The return type of a method.
     */
    public final Klass returnType;

    /**
     * The declared parameter types of a method.
     */
    public final Klass[] parameterTypes;

    /**
     * Construct a method signature.
     */
    public MethodSignature(Klass returnType, Klass[] parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes  = parameterTypes;
    }

    /**
     * Gets the number of words used by the parameters where double and long parameters
     * use two words. Also, one word is added for the implicit 'this' parameter of a
     * non-static method.
     */
    public int getParametersLength(boolean isStatic) {
        int length = isStatic ? 0 : 1;
        for (int i = 0; i != parameterTypes.length; ++i) {
            length += parameterTypes[i].isDoubleWord() ? 2 : 1;
        }
        return length;
    }

    /**
     * A sentinel object representing an invalid signature.
     */
    public static final MethodSignature INVALID = new MethodSignature(null, null);

    /**
     * Change the return type of a signature. If the new return type
     * is different from the existing one, a new instance of
     * <code>Signature</code> is created and returned.
     *
     * @param type the new return type.
     */
    public MethodSignature modifyReturnType(Klass type) {
        if (type == returnType) {
            return this;
        } else {
            return new MethodSignature(type, parameterTypes);
        }
    }
}

