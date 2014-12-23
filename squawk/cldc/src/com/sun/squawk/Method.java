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

import com.sun.squawk.pragma.PragmaException;


/**
 * An instance of <code>Method</code> encapsulates the information about the
 * method of a class. This includes the name of the method, its return type,
 * parameter types, access flags etc.
 *
 */
public final class Method extends Member {

    /**
     * Creates a new <code>Method</code>.
     *
     * @param  metadata the metadata of the class that declared the method
     * @param  id       the index of this method within <code>metadata</code>
     */
    Method(KlassMetadata metadata, int id) {
        super(metadata, id);
    }
    
    public int hashCode() {
        return metadata.hashCode() ^ id;
    }
    
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof Method) {
            Method otherMethod = (Method)other;
            if (this.metadata == otherMethod.metadata && this.id == otherMethod.id) {
                return true;
            }
        }
        return false;
    }


    /*---------------------------------------------------------------------------*\
     *              Access permissions and member property queries               *
    \*---------------------------------------------------------------------------*/

    private int getPragmas() {
        return parser().getPragmas();
    }

    /**
     * Returns whether this method should only be called in a hosted environment.
     */
    public boolean isHosted() {
        return PragmaException.isHosted(getPragmas());
    }

    /**
     * Returns whether the body of this method is used to replace the body of
     * a constructor with the same signature.
     */
    public boolean isReplacementConstructor() {
        return PragmaException.isReplacementConstructor(getPragmas());
    }

    /**
     * Returns whether this method is only invoked from the interpreter, and never from Java code.
     */
    public boolean isInterpreterInvoked() {
        return PragmaException.isInterpreterInvoked(getPragmas());
    }

    /**
     * Returns whether this method is forceably inlined by the translator.
     */
    public boolean isForceInlined() {
        return PragmaException.isForceInlined(getPragmas());
    }

    /**
     * Returns whether this method is allowed to be inlined in extra circumstances by the translator.
     */
    public boolean isAllowInlined() {
        return PragmaException.isAllowInlined(getPragmas());
    }

    /**
     * Returns whether this method is never inlined by the translator.
     */
    public boolean isNotInlined() {
        return PragmaException.isNotInlined(getPragmas());
    }

    /**
     * Returns whether this native method is only native due to a pragma.
     */
    public boolean isNativeDueToPragma() {
        return PragmaException.isNative(getPragmas());
    }

    /**
     * Determines if this method is a native method.
     *
     * @return  true if this method is a native method
     */
    public boolean isNative() {
        return Modifier.isNative(parser().getModifiers());
    }

    /**
     * Determines if this method is an abstract method.
     *
     * @return  true if this method is an abstract method
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(parser().getModifiers());
    }

    /**
     * Determines if this method is a synchronized method.
     *
     * @return  true if this method is a synchronized method
     */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(parser().getModifiers());
    }

    /**
     * Determines if this method is a constructor.
     *
     * @return  true if this method is a constructor
     */
    public boolean isConstructor() {
        return Modifier.isConstructor(parser().getModifiers());
    }

    /**
     * Determines if this method is a constructor.
     *
     * @return  true if this method is a constructor
     */
    public boolean isDefaultConstructor() {
        return isConstructor() && getParameterCount() == 0;
    }

    /**
     * Determines if this method is a class initialization method
     *
     * @return  true if this method is a class initialization method
     */
    public boolean isClassInitializer() {
        return getName().equals("<clinit>");
    }

    /**
     * Gets the formal return type of this method.<p>
     *
     * @return   the formal return type of this method
     */
    public Klass getReturnType() {
        return parser().getSignatureTypeAt(0);
    }

    /**
     * Gets the formal parameter types, in declaration order, of this method.
     * Returns an array of length 0 if this method takes no parameters.<p>
     *
     * @return   the formal parameter types of this method
     */
    public Klass[] getParameterTypes() {
        SymbolParser parser = parser();
        Klass[] parameterTypes = new Klass[parser.getSignatureCount() - 1];
        for (int i = 0 ; i < parameterTypes.length ; i++) {
            parameterTypes[i] = parser.getSignatureTypeAt(i+1);
        }
        return parameterTypes;
    }

    /**
     * Returns the number of parameters for this method.
     *
     * @return   the number of parameters for this method.
     */
    public int getParameterCount() {
        return parser().getSignatureCount() - 1;
    }

    /**
     * Tests to see if this method requires a CLASS_CLINIT instruction to be emitted
     * before the body of the method's bytecodes.
     *
     * @return true if it is
     */
    public boolean requiresClassClinit() {
        return getDefiningClass().mustClinit() &&
               isStatic() &&
              !isConstructor() &&
              !getName().equals("<clinit>") &&
               getDefiningClass() != Klass.KLASS; // The initializer for class Class is called explicitly in the VM startup sequence
    }

    /**
     * Return the number of words used as parameters to this method. Includes any implicit "this" parameter.
     */
    public int getParametersSize() {
        boolean isStatic = isStatic() && !isConstructor();
        SymbolParser parser = parser();
        int sigcount = parser.getSignatureCount();
        int count = isStatic ? sigcount - 1 : sigcount;
        for (int i = 1 ; i < sigcount ; i++) {
            Klass type = parser.getSignatureTypeAt(i);
            if (!Klass.SQUAWK_64 && type.isDoubleWord()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Gets the runtime parameter types, in declaration order, of this method.
     * Returns an array of length 0 if this method takes no parameters.<p>
     *
     * @param  reverseParameters true if the parameters are normally pushed right-to-left
     * @return the formal parameter types of this method
     */
    public Klass[] getRuntimeParameterTypes(boolean reverseParameters) {
        boolean isStatic = isStatic() && !isConstructor();
        SymbolParser parser = parser();

        /*
         * Count the parameter words.
         */
        int sigcount = parser.getSignatureCount();
        int count = getParametersSize();
        Klass[] parameterTypes = new Klass[count];

        /*
         * Add receiver if not static.
         */
        int j = 0;
        if (!isStatic) {
            parameterTypes[j++] = getDefiningClass();
        }

        /*
         * Add all the other parameter words.
         */
        for (int i = 1 ; i < sigcount ; i++) {
            Klass type = parser.getSignatureTypeAt(i);
            parameterTypes[j++] = type;
            if (!Klass.SQUAWK_64) {
                if (type == Klass.LONG) {
                    parameterTypes[j++] = Klass.LONG2;
                }
/*if[FLOATS]*/
                if (type == Klass.DOUBLE) {
                    parameterTypes[j++] = Klass.DOUBLE2;
                }
/*end[FLOATS]*/
            }
        }

        /*
         * Reverse the order of the parameters if needed.
         */
        if (isInterpreterInvoked() && reverseParameters) {
            Klass[] revparmtypes = new Klass[parameterTypes.length];
            j = 0;
            for (int i = parameterTypes.length - 1 ; i >= 0 ; i--) {
                Klass type = parameterTypes[i];
                if (!Klass.SQUAWK_64 && (type == Klass.LONG2 
/*if[FLOATS]*/
                        || type == Klass.DOUBLE2
/*end[FLOATS]*/
                        )) {
                    i--;
                    revparmtypes[j++] = parameterTypes[i];
                }
                revparmtypes[j++] = type;
            }
            parameterTypes = revparmtypes;
        }

        /*
         * Return the result.
         */
        return parameterTypes;
    }

    /*---------------------------------------------------------------------------*\
     *                 Method debug info                                         *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the table mapping instruction addresses to the source line numbers
     * that start at the addresses. The table is encoded as an int array where
     * the high 16-bits of each element is an instruction address and the low
     * 16-bits is the corresponding source line.
     *
     * @return the line number table or null if there is no line number
     *         information for the method
     */
    public int[] getLineNumberTable() {
        MethodMetadata method = metadata.getMethodMetadata(isStatic(), getOffset());
        if (method != null) {
            return method.getLineNumberTable();
        }
        return null;
    }

    /**
     * Gets a table describing the scope, name and type of each local variable
     * in the method.
     *
     * @return the local variable table or null if there is no local variable
     *         information for the method
     */
    public ScopedLocalVariable[] getLocalVariableTable() {
        MethodMetadata method = metadata.getMethodMetadata(isStatic(), getOffset());
        if (method != null) {
            return method.getLocalVariableTable();
        }
        return null;
    }

    /**
     * Converts a given line number table as a string of numbers where each pair of numbers
     * represents an ip address and a line number.
     *
     * @param lnt      the table mapping instruction addresses to the
     *                 source line numbers that start at the addresses.
     *                 The table is encoded as an int array where the high
     *                 16-bits of each element is an instruction address and
     *                 the low 16-bits is the corresponding source line
     * @return the line number table as a string
     */
    public static String lineNumberTableAsString(int[] lnt) {
        StringBuffer sb = new StringBuffer();
        if (lnt != null) {
            for (int i = 0 ; i < lnt.length ; i++) {
                int entry = lnt[i];
                sb.append(" "+(entry>>>16) +" "+(entry&0xFFFF));
            }
        }
        return sb.toString();
    }

    /**
     * Gets the line number for a bytecode index
     *
     * @param lnt      the table mapping bytecode indexes to the
     *                 source line numbers that start at the addresses.
     *                 The table is encoded as an int array where the high
     *                 16-bits of each element is an instruction address and
     *                 the low 16-bits is the corresponding source line
     * @param bci      a bytecode index
     * @return the line number or -1 if it could not be found
     */
    public static int getLineNumber(int[] lnt, int bci) {
        int lno = -1;
        if (lnt != null) {
            lno = 0;
            for (int i = 0 ; i < lnt.length ; i++) {
                int entry = lnt[i];
                int addr = entry >>> 16;
                if (addr >= bci) {
                    break;
                }
                lno = entry & 0xFFFF;
            }
        }
        return lno;
    }

    /**
     * DO NOT USE
     */
    public final void invoke(Object[] args) {
        throw new RuntimeException("Method put in place for JavaCard support, temporary, do not use");
    }

}
