/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk;

import com.sun.squawk.util.Assert;
import com.sun.squawk.pragma.*;


/**
 * An instance of <code>ClassFileMethod</code> encapsulates all the
 * symbolic information of a method declaration in a class file.
 * This class is provided for a subsystem (such as the translator) that
 * loads a class definition from a class file.
 *
 */
public final class ClassFileMethod extends ClassFileMember {

    /**
     * A zero-length array of <code>ClassFileMethod</code>s.
     */
    public static final ClassFileMethod[] NO_METHODS = {};

    /**
     * The return type of this method.
     */
    private final Klass returnType;

    /**
     * The types of the parameters of this method.
     */
    private final Klass[] parameterTypes;

    /**
     * The {@link PragmaException pragmas} (if any) applying this method.
     */
    private final int pragmas;

    /**
     * The bytecode array of this method.
     */
    private byte[] code;

    /**
     * Creates a new <code>ClassFileMethod</code> instance.
     *
     * @param   name           the name of the method
     * @param   modifiers      the modifiers of the method
     * @param   returnType     the return type of the method
     * @param   parameterTypes the parameters types of the method
     * @param   pragmas        a mask of values denoting the {@link PragmaException pragmas}
     *                         (if any) that apply to the method
     */
    public ClassFileMethod(String name, int modifiers, Klass returnType, Klass[] parameterTypes, int pragmas) {
        super(name, modifiers);
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.pragmas = pragmas;
    }

    /**
     * Gets the return type of this method.
     *
     * @return  the return type of this method
     */
    public Klass getReturnType() {
        return returnType;
    }

    /**
     * Gets the parameter types of this method.
     *
     * @return  the parameter types of this method
     */
    public Klass[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Gets the byte array corresponding to the "Code" attribute in the
     * class file. This can only be called for a non-native, non-abstract
     * method.
     *
     * @return  the data in the "Code" attribute for this method
     */
    public byte[] getCode() {
        Assert.that(!isAbstract() && !isNative());
        return code;
    }

    /**
     * Sets the byte array corresponding to the "Code" attribute in the
     * class file. This can only be called for a non-native, non-abstract
     * method.
     *
     * @param  code  the data in the "Code" attribute for this method
     */
    public void setCode(byte[] code) {
        Assert.that(!isAbstract() && !isNative());
        this.code = code;
    }

    /**
     * Gets the {@link PragmaException pragmas} that apply to the method
     *
     * @return  a mask of the constants defined in {@link PragmaException}
     */
    public int getPragmas() {
        return pragmas;
    }

    /**
     * Determines if this is a <code>native</code> method.
     *
     * @return  true if this is a <code>native</code> method
     */
    public boolean isNative() {
        return Modifier.isNative(modifiers);
    }

    /**
     * Determines if this is a <code>abstract</code> method.
     *
     * @return  true if this is a <code>abstract</code> method
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(modifiers);
    }

    /**
     * Determines if this is a <code>protected</code> method.
     *
     * @return  true if this is a <code>protected</code> method
     */
    public boolean isProtected() {
        return Modifier.isProtected(modifiers);
    }

    /**
     * Determines if this method is <init>.
     *
     * @return  true if it is
     */
    public boolean isDefaultConstructor() {
        return getName().equals("<init>") && parameterTypes.length == 0;
    }

    /**
     * Determines if this method is <clinit>.
     *
     * @return  true if it is
     */
    public boolean isClinit() {
        return getName().equals("<clinit>") && parameterTypes.length == 0;
    }

/*if[FINALIZATION]*/
    /**
     * Determines if this method is "finalize()".
     *
     * @return  true if it is
     */
    public boolean isFinalize() {
        return getName().equals("finalize");
    }
/*end[FINALIZATION]*/

    /**
     * Determines if this method is a static void main(String[]).
     *
     * @return  true if it is
     */
    public boolean isMain() {
        return isStatic() &&
               getName().equals("main") &&
               returnType == Klass.VOID &&
               parameterTypes.length == 1 &&
               parameterTypes[0] == Klass.STRING_ARRAY;
    }
    
    public String toString(Klass klass) {
        StringBuffer buf = new StringBuffer(klass.getName());
        buf.append('.').append(getName()).append('(');
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append(parameterTypes[i]);
        }
        buf.append(')');
        return buf.toString();
    }
}
