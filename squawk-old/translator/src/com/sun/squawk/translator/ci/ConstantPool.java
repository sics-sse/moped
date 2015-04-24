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

import com.sun.squawk.util.Assert;
import com.sun.squawk.translator.*;
import com.sun.squawk.util.SquawkVector;    // Version without synchronization
import com.sun.squawk.util.SquawkHashtable; // Version without synchronization
import com.sun.squawk.*;

/**
 * This class represents data in the <code>constant_pool</code> of a class
 * file as well as mechanisms for reflecting on this information.
 *
 */
public class ConstantPool {


    /*---------------------------------------------------------------------------*\
     *                          Constant pool tags                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Constant pool tag value denoting a UTF-8 encoded string entry.
     */
    public static final int CONSTANT_Utf8 = 1;

    /**
     * Constant pool tag value denoting a unicode encoded string entry.
     */
    public static final int CONSTANT_Unicode = 2;

    /**
     * Constant pool tag value denoting a 4-byte <code>int</code> constant.
     */
    public static final int CONSTANT_Integer = 3;

    /**
     * Constant pool tag value denoting a 4-byte <code>float</code> constant.
     */
    public static final int CONSTANT_Float = 4;

    /**
     * Constant pool tag value denoting an 8-byte <code>long</code> constant.
     */
    public static final int CONSTANT_Long = 5;

    /**
     * Constant pool tag value denoting an 8-byte <code>double</code> constant.
     */
    public static final int CONSTANT_Double = 6;

    /**
     * Constant pool tag value denoting a class or interface reference.
     */
    public static final int CONSTANT_Class = 7;

    /**
     * Constant pool tag value denoting a <code>String</code> value.
     */
    public static final int CONSTANT_String = 8;

    /**
     * Constant pool tag value denoting a field reference.
     */
    public static final int CONSTANT_Fieldref = 9;

    /**
     * Contant pool tag value denoting a method reference.
     */
    public static final int CONSTANT_Methodref = 10;

    /**
     * Constant pool tag value denoting an interface method reference.
     */
    public static final int CONSTANT_InterfaceMethodref = 11;

    /**
     * Constant pool tag value denoting a members name and type values.
     */
    public static final int CONSTANT_NameAndType = 12;

    /**
     * Constant pool tag value denoting an Object value. This is an extension
     * to the constants defined in the JVM specification.
     */
    public static final int CONSTANT_Object = 14;


    /*---------------------------------------------------------------------------*\
     *                  Enumerated type for valid name formats                   *
    \*---------------------------------------------------------------------------*/

    /**
     * This is an enumerated type representing the various formats for
     * names occuring in the constant pool of a Java classfile.
     */
    public static class ValidNameFormat {

        /**
         * Enumerated value allocator.
         */
        private static int nextValue = 1;

        /**
         * Create a new enumerated constant.
         *
         * @param asString  the name of the constant
         */
        private ValidNameFormat(String asString) {
            this.asString = asString;
            this.value = nextValue++;
        }

        /**
         * Gets the name of the constant.
         *
         * @return  the name of the constant
         */
        public String toString() {
            return asString;
        }

        public final String asString;
        public final int    value;

        /**
         * A constant denoting a legality check on a name with respect to
         * the valid format for a class name.
         */
        public static final ValidNameFormat CLASS  = new ValidNameFormat("class");

        /**
         * A constant denoting a legality check on a name with respect to
         * the valid format for a field name.
         */
        public static final ValidNameFormat FIELD  = new ValidNameFormat("field");

        /**
         * A constant denoting a legality check on a name with respect to
         * the valid format for a method name.
         */
        public static final ValidNameFormat METHOD = new ValidNameFormat("method");
    }


    /*---------------------------------------------------------------------------*\
     *                               Constant pool                               *
    \*---------------------------------------------------------------------------*/

    /*
     * The constant pool entries are encoded as regular Java objects. The list
     * of valid objects for each tag type are:
     *
     *   CONSTANT_Utf8               null (Not retained)
     *   CONSTANT_NameAndType        null (Not retained)
     *   CONSTANT_Integer            java.lang.Integer
     *   CONSTANT_Float              java.lang.Float
     *   CONSTANT_Long               java.lang.Long
     *   CONSTANT_Double             java.lang.Double
     *   CONSTANT_String             java.lang.String
     *   CONSTANT_Class              com.sun.squawk.translator.Klass
     *   CONSTANT_Field              com.sun.squawk.translator.Field
     *   CONSTANT_Method             com.sun.squawk.translator.Method
     *   CONSTANT_InterfaceMethod    com.sun.squawk.translator.Method
     *
     * Thus only a null, Integer, Long, Float, Double, Klass, Field, or Method
     * will be found in this array.
     *
     * CONSTANT_Utf8 entries are converted into Strings
     * CONSTANT_NameAndType are not needed becuse the UTF8 strings they refer
     * to is converted into strings and places in the approperate Field and Method
     * data structures.
     */

    /* This was introduced temporarily, perhaps to get Java5 synatx support working.
     * But for now, keep standard access control in order to pass TCK tests.
     * Might need to turn into a parameter option (default to false!).
     */
    private final static boolean ALLOW_LOOSE_ACCESS_CONTROL = false;
    
    /**
     * The translation context
     */
    private final Translator translator;

    /**
     * The ClassFileReader from which the constant pool is read.
     */
    private final ClassFileReader cfr;

    /**
     * The class defined by the class file containing this constant pool.
     */
    private final Klass definedClass;

    /**
     * Pool entry tags.
     */
    private final byte[] tags;

    /**
     * Resolved pool entries for all object types.
     */
    private final Object[] entries;

    /**
     * Cache of method signature strings to MethodSignature objects.
     */
    private final SquawkHashtable methodSigCache;

    /**
     * Gets class containing this constant pool.
     *
     * @return   class containing this constant pool
     */
    public Klass getDefinedClass() {
        return definedClass;
    }

    /**
     * Verifies that an index is within range and the indexed entry
     * is of an expected type.
     *
     * @param index    the index to check
     * @param tag      the type to check
     * @throws ClassFormatError if the test fails
     */
    private void verifyEntry(int index, int tag) {
        if (index < 1 || index >= entries.length) {
            throw cfr.formatError("constant pool index out of range");
        }
        if (tags[index] != tag) {
            throw cfr.formatError("invalid constant pool entry type");
        }
    }

    /**
     * Verifies that a legal field name occurs at a given offset of a string.
     *
     * @param  s          the string to test
     * @param  offset     the offset at which a legal field name should occur
     * @param  slashOkay  true if an embedded '/' is legal
     * @return the first character after the legal field name or -1 if there
     *                    is no legal field name at <code>offset</code>
     */
    private static int skipOverFieldName(String s, int offset, boolean slashOkay) {
        char lastCh = (char)0;
        char ch;
        int i;
        for (i = offset; i != s.length(); i++, lastCh = ch) {
            ch = s.charAt(i);
            if ((int)ch < 128) {
                /*
                 * Quick check for ascii
                 */
                if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (lastCh != 0 && ch >= '0' && ch <= '9')) {
                    continue;
                }
            } else {
                /*
                 * This is a unicode character and all unicode characters are valid
                 * identifier characters apart from the first character
                 */
                if (lastCh == 0) {
                    return -1;
                }
                continue;
            }
            if (slashOkay && (ch == '/') && (lastCh != 0)) {
                if (lastCh == '/') {
                    return -1;    // Don't permit consecutive slashes
                }
            } else if (ch == '_' || ch == '$') {
                continue;
            } else {
                return lastCh != 0 ? i : -1;
            }
        }
        return lastCh != 0 ? i : -1;
    }

    /**
     * Verifies that a legal type name occurs at a given offset of a string.
     *
     * @param  s          the string to test
     * @param  offset     the offset at which a legal type name should occur
     * @param  voidOkay   true if the void type is legal
     * @return the first character after the legal type name or -1 if there
     *                    is no legal type name at <code>offset</code>
     */
    private static int skipOverFieldType(String s, int offset, boolean voidOkay) {
        int length = s.length();
        int depth = 0;
        for (int i = offset; i != length; i++) {
            switch (s.charAt(i)) {
                case 'V':
                    if (!voidOkay) {
                        return -1;
                    }
                    /* FALL THROUGH */
                case 'Z':
                case 'B':
                case 'C':
                case 'S':
                case 'I':
                case 'J':
                case 'F':
                case 'D': {
                    return i + 1;
                }
                case 'L': {
                    /* Skip over the class name, if one is there. */
                    int end = skipOverFieldName(s, i + 1, true);
                    if (end != -1 && end < length && s.charAt(end) == ';') {
                        return end + 1;
                    }
                    else {
                        return -1;
                    }
                }
                case '[': {
                    /* The rest of what's there better be a legal signature.  */
                    if (depth++ == 255) {
                        return -1;
                    }
                    voidOkay = false;
                    break;
                }
                default: {
                    return -1;
                }
            }
        }
        return -1;
    }

    /**
     * Tests whether a name for a given class component has a valid format
     * with respect to the specified format for that component type.
     *
     * @param  name    the name to test
     * @param  format  the format specification to test against
     * @return true if the given string is valid
     */
    public static boolean isLegalName(String name, ValidNameFormat format) {
        Assert.that(format != null);
        boolean result = false;
        int length = name.length();
        if (length > 0) {
            if (name.charAt(0) == '<') {
                result = (format == ValidNameFormat.METHOD) && (name.equals("<init>") || name.equals("<clinit>"));
            } else {
                int end;
                if (format == ValidNameFormat.CLASS && name.charAt(0) == '[') {
                    end = skipOverFieldType(name, 0, false);
                } else {
                    end = skipOverFieldName(name, 0, format == ValidNameFormat.CLASS);
                }
                result = (end != -1) && (end == name.length());
            }
        }
        return result;
    }


    /*---------------------------------------------------------------------------*\
     *                  Well-formedness verification methods                     *
    \*---------------------------------------------------------------------------*/

    /**
     * Verifies that a name for a given class component has a valid format
     * with respect to the specified format for that component type.
     *
     * @param  name    the name to test.
     * @param  format  the format specification to test against
     * @throws ClassFormatError if the name is invalid
     */
    public String verifyName(String name, ValidNameFormat format) {
        if (!isLegalName(name, format)) {
            throw cfr.formatError("invalid " + format + " name");
        }
        return name;
    }

    /**
     * Verifies that a field signature has a valid format and returns the
     * corresponding <code>Klass</code> instance for the signature.
     *
     * @param   sig   the field signature to test
     * @return  the class representing <code>sig</code>
     * @throws  ClassFormatError if the signature is invalid
     */
    public Klass verifyFieldType(String sig) {
        if (skipOverFieldType(sig, 0, false) != sig.length()) {
            throw cfr.formatError("invalid field signature");
        }
        return Klass.getClass(sig, true);
    }

    /**
     * Verifies that a method signature has a valid format and returns the
     * corresponding <code>Signature</code> instance for the signature.
     *
     * @param   sig            the method signature to test
     * @param   specialMethod  true if this method's name startsWith '<'
     * @param   isStatic       the signature is for a static method
     * @return the types in the method's signature.
     * @throws  ClassFormatError if the signature is invalid
     */
    public MethodSignature verifyMethodType(String sig, boolean specialMethod, boolean isStatic) {
        MethodSignature signature = (MethodSignature)methodSigCache.get(sig);
        if (signature == null) {
            Klass returnType = null;
            SquawkVector parameterTypes = new SquawkVector();

            /*
             * The first character must be a '('
             */
            int length = sig.length();
            if (length > 0 && sig.charAt(0) == '(') {
                int offset = 1;

                /*
                 * Skip over however many legal field signatures there are
                 */
                while (offset < length) {
                    int nextOffset = skipOverFieldType(sig, offset, false);
                    if (nextOffset == -1) {
                        break;
                    }
                    Klass parameterType = Klass.getClass(sig.substring(offset, nextOffset), true);
                    parameterTypes.addElement(parameterType);
                    offset = nextOffset;
                }

                /*
                 * The first non-signature thing better be a ')'
                 */
                if (offset < length && (sig.charAt(offset) == ')')) {
                    offset++;
                    if (specialMethod) {
                        /*
                         * All internal methods must return void
                         */
                        if ((offset == length - 1) && (sig.charAt(offset) == 'V')) {
                            returnType = Klass.VOID;
                        }
                    } else {
                        /*
                         * Now, we better just have a return value.
                         */
                        if (skipOverFieldType(sig, offset, true) == length) {
                            returnType = Klass.getClass(sig.substring(offset, length), true);
                        }
                    }
                }
            }

            if (returnType == null) {
                signature = MethodSignature.INVALID;
            } else {
                Klass[] classes = new Klass[parameterTypes.size()];
                parameterTypes.copyInto(classes);
                signature = new MethodSignature(returnType, classes);
            }
            methodSigCache.put(sig, signature);
        }
        if (signature == MethodSignature.INVALID || signature.getParametersLength(isStatic) > 255) {
            throw cfr.formatError("invalid method signature");
        }
        return signature;
    }

    /**
     * Verifies that the modifiers for a class are valid.
     *
     * @param    modifiers  the modifiers to test
     * @return   the modifiers the VM should use
     * @throws   ClassFormatError if the modifiers are invalid
     */
    public int verifyClassModifiers(int modifiers) {
        boolean valid;
        int finalAndAbstract = (Modifier.FINAL | Modifier.ABSTRACT);
        if ((modifiers & Modifier.INTERFACE) != 0) {
            valid = (modifiers & finalAndAbstract) == Modifier.ABSTRACT;
        } else {
            valid = (modifiers & finalAndAbstract) != finalAndAbstract; // Cannot be final and abstract
        }
        if (valid) {
            return modifiers;
        } else {
            throw cfr.formatError("invalid class modifiers "+modifiers);
        }
    }

    /**
     * Verifies that the modifiers for a field are valid.
     *
     * @param    modifiers       the modifiers to test
     * @param    classModifiers  the modifiers of the enclosing class
     * @throws   ClassFormatError if the modifiers are invalid
     */
    public void verifyFieldModifiers(int modifiers, int classModifiers) {
        boolean valid;
        if ((classModifiers & Modifier.INTERFACE) == 0) {
            /*
             * Class or instance fields
             */
            int flags = modifiers & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED);

            /*
             * Make sure that modifiers has at most one of its ACC_PRIVATE,
             * ACC_PROTECTED bits set. That is, do a population count of these
             * bit positions corresponding to these flags and ensure that it is
             * at most 1.
             */
            valid = (flags == 0 || (flags & ~(flags - 1)) == flags);

            /*
             * A field can't be both final and volatile
             */
            int finalAndVolatile = Modifier.FINAL | Modifier.VOLATILE;
            valid = valid && ((modifiers & finalAndVolatile) != finalAndVolatile);
        } else {
            /*
             * interface fields must be public static final (i.e. constants).
             */
            valid  = (modifiers == (Modifier.STATIC | Modifier.FINAL | Modifier.PUBLIC));
        }
        if (!valid) {
            throw cfr.formatError("invalid field modifiers");
        }
    }

    /**
     * Verifies that the modifiers for a method are valid.
     *
     * @param    modifiers       the modifiers to test
     * @param    classModifiers  the modifiers of the enclosing class
     * @param    isInit          true if the method is "<init>"
     * @throws   ClassFormatError if the modifiers are invalid
     */
     public void verifyMethodModifiers(int modifiers, int classModifiers, boolean isInit) {

        /*
         * These are all small bits.  The value is between 0 and 7.
         */
        int flags = modifiers & (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED);

        /*
         * Make sure that modifiers has at most one of its ACC_PRIVATE,
         * ACC_PROTECTED bits set. That is, do a population count of these
         * bit positions corresponding to these flags and ensure that it is
         * at most 1.
         */
        boolean valid = (flags == 0 || (flags & ~(flags - 1)) == flags);
        if (valid) {
            if ((classModifiers & Modifier.INTERFACE) == 0) {
                /*
                 * class or instance methods
                 */
                if ((modifiers & Modifier.ABSTRACT) != 0) {
                    if ((modifiers & (Modifier.FINAL | Modifier.NATIVE | Modifier.SYNCHRONIZED | Modifier.PRIVATE | Modifier.STATIC | Modifier.STRICT)) != 0) {
                        valid = false;
                    }
                }
            } else {
                /*
                 * All interface methods must have their ACC_ABSTRACT and ACC_PUBLIC modifiers
                 * set and may not have any of the other modifiers in Table 4.5 set (2.13.3.2).
                 *
                 * Note that <clinit> is special, and not handled by this
                 * function.  It's not abstract, and static.
                 */
                final int abstractAndPublic = Modifier.ABSTRACT | Modifier.PUBLIC;
                valid = ((modifiers & (abstractAndPublic | Modifier.STATIC)) == (abstractAndPublic));
            }

            if (valid) {
                if (isInit) {
                    /*
                     * A specific instance initialization method (3.9) may have
                     * at most one of its ACC_PRIVATE, ACC_PROTECTED, and
                     * ACC_PUBLIC modifiers set and may also have its ACC_STRICT
                     * modifier set, but may not have any of the other modifiers
                     * in Table 4.5 set.
                     */
                    valid = ((modifiers & ~(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE | Modifier.STRICT)) == 0);
                }
            }
        }
        if (!valid) {
//verifyMethodModifiers(modifiers, classModifiers, isInit);
            throw cfr.formatError("invalid method modifiers");
        }
    }


    /*---------------------------------------------------------------------------*\
     *                        Accessors to constant entries                      *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the size of the constant pool.
     *
     * @return  the number of entries in the constant pool
     */
    public int getSize() {
        return entries.length;
    }

    /**
     * Gets the tag for the entry at a given index.
     *
     * @param  index  the index of the entry
     * @return the tag for the entry at <code>index</code>
     */
    public int getTag(int index) {
        if (index < 0 || index >= entries.length ) {
            throw cfr.formatError("invalid constant index");
        }
        return tags[index];
    }

    /**
     * Gets the integer constant value at a given index.
     *
     * @param   index  the index of an integer constant in the pool
     * @return  the value of the integer constant at <code>index</code>
     */
    public int getInt(int index) {
        verifyEntry(index, CONSTANT_Integer);
        return ((Integer)entries[index]).intValue();
    }

    /**
     * Gets the long constant value at a given index.
     *
     * @param   index  the index of a long constant in the pool
     * @return  the value of the long constant at <code>index</code>
     */
    public long getLong(int index) {
        verifyEntry(index, CONSTANT_Long);
        return ((Long)entries[index]).longValue();
    }

/*if[FLOATS]*/

    /**
     * Gets the float constant value at a given index.
     *
     * @param   index  the index of a float constant in the pool
     * @return  the value of the float constant at <code>index</code>
     */
    public float getFloat(int index) {
        verifyEntry(index, CONSTANT_Float);
        return ((Float)entries[index]).floatValue();
    }

    /**
     * Gets the double constant value at a given index.
     *
     * @param   index  the index of a double constant in the pool
     * @return  the value of the double constant at <code>index</code>
     */
    public double getDouble(int index) {
        verifyEntry(index, CONSTANT_Double);
        return ((Double)entries[index]).doubleValue();
    }

/*end[FLOATS]*/

    /**
     * Gets the String constant value at a given index.
     *
     * @param   index  the index of a String constant in the pool
     * @return  the value of the String constant at <code>index</code>
     */
    public String getString(int index) {
        verifyEntry(index, CONSTANT_String);
        return (String)entries[index];
    }

    /**
     * Gets the UTF-8 encoded constant value at a given index.
     *
     * @param   index  the index of a UTF-8 encoded constant in the pool
     * @return  the value of the UTF-8 encoded constant at <code>index</code>
     */
    public String getUtf8(int index) {
        verifyEntry(index, CONSTANT_Utf8);
        return (String)entries[index];
    }

    /**
     * Gets the potentially unresolved class entry at a given index. While
     * this method does not resolve the class, it will create/find an
     * instance of Klass if this has not yet been done.
     *
     * @param   index  the index of a class reference in the pool
     * @return  the class reference at <code>index</code>
     */
    public Klass getKlass(int index) {
        verifyEntry(index, CONSTANT_Class);
        if (entries[index] instanceof String) {
            String name = verifyName((String)entries[index],
                                     ValidNameFormat.CLASS);
            Klass klass;
            if (name.charAt(0) == '[') {
                /*
                 * The name of array classes is in field descriptor form
                 */
                klass = Klass.getClass(name, true);
            } else {
                /*
                 * The name of non-array classes will be in JVM
                 * internal form (section 4.2 of the JVM spec) and
                 * needs to be converted to Squawk internal form
                 */
                name = name.replace('/', '.');
                klass = Klass.getClass(name, false);
            }
            entries[index] = klass;
        }
        return (Klass)entries[index];
    }

    /**
     * Gets the entry at a given index that has an expected type.
     *
     * @param   index  the index of the entry to retrieve
     * @param   tag    the expected type of the entry
     * @return  the entry at <code>index</code>
     */
    public Object getEntry(int index, int tag) {
        verifyEntry(index, tag);
        return entries[index];
    }

    /**
     * Gets the entry at a given index that encapsulates a name and type.
     *
     * @param   index  the index of the entry to retrieve
     * @return the <code>NameAndType</code> instance at <code>index</code>
     */
    private NameAndType getNameAndType(int index) {
        return (NameAndType)entries[index];
    }


    /*---------------------------------------------------------------------------*\
     *                 Accessors to entries that require resolving               *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the class entry at a given index, resolving it if it has not
     * already been resolved. Resolution means filling in the details of
     * the class from a class file but without converting its methods.
     *
     * @param   index   the index of a class reference in the pool
     * @param   context  the context of the request
     * @return  the class reference at <code>index</code>
     */
    public Klass getResolvedClass(int index, Context context) {
        Klass klass = getKlass(index);
        if (klass.getState() == Klass.STATE_ERROR) {
        	throw new NoClassDefFoundError(context.prefix(klass.getName()));
        }
        translator.load(klass);

        Klass base = klass;
        while (base.isArray()) {
            base = base.getComponentType();
        }

        if (!base.isAccessibleFrom(definedClass)) {
            if (ALLOW_LOOSE_ACCESS_CONTROL) {
                System.out.println("+++++++ DEREK HELP !!!");
                System.out.print(base);
                System.out.print(".isAccessible");
                System.out.println(definedClass);
                System.out.println("+++++++ DEREK HELP !!!");
            } else {
                throw new com.sun.squawk.translator.IllegalAccessError(context.prefix(klass.toString()));
            }
        }
        return klass;
    }

    /**
     * Gets the field entry at a given index, resolving it if it has not
     * already been resolved.
     *
     * @param   index       the index of a field reference in the pool
     * @param   isStatic    true if resolving a static field
     * @param   context      the context of the request
     * @return  the field reference at <code>index</code>
     */
    public Field getResolvedField(int index, boolean isStatic, Context context) {
        verifyEntry(index, CONSTANT_Fieldref);
        Field field;
        if (entries[index] instanceof FieldOrMethod) {
            FieldOrMethod entry = (FieldOrMethod)entries[index];
            Klass declaringClass = entry.getDefiningClass(this, context);
            NameAndType nt = getNameAndType(entry.nameAndTypeIndex);

            verifyName(nt.name, ValidNameFormat.FIELD);
            Klass type = verifyFieldType(nt.sig);

            field = declaringClass.lookupField(nt.name, type, isStatic);
            if (field == null) {
                field = declaringClass.lookupField(nt.name, type, !isStatic);
                if (field != null) {
                	throw new com.sun.squawk.translator.IncompatibleClassChangeError(context.prefix(field.toString()));
                } else {
                    /*
                     * NoSuchFieldError
                     */
                    String message = nt.sig + ' ' + declaringClass.getName() + '.' + nt.name;
                    throw new com.sun.squawk.translator.NoSuchFieldError(context.prefix(message));
                }
            }

            /*
             * Ensure the class that defined the field and its type are loaded
             */
            translator.load(type);

            /*
             * Write the resolved entry in the constant pool
             */
            entries[index] = field;

            /*
             * Since access only depends on the class, and not on the
             * specific byte code used to access the field, we don't need
             * to perform this check if the constant pool entry
             * has already been resolved.
             */
            if (!field.isAccessibleFrom(definedClass)) {
            	throw new com.sun.squawk.translator.IllegalAccessError(context.prefix(field.toString()));
            }
        } else {
            field = (Field)entries[index];
        }
        return field;
    }

    /**
     * Gets the method entry at a given index, resolving it if it has not
     * already been resolved.
     *
     * @param   index             the index of a method reference in the pool
     * @param   invokeinterface   true if resolving a method for an <i>invokeinterface</i> instruction
     * @param   isStatic          true if resolving a static field
     * @param   context            the context of the request
     * @return  the method reference at <code>index</code>
     */
    public Method getResolvedMethod(int index, boolean isStatic, boolean invokeinterface, Context context) {
        verifyEntry(index, (invokeinterface ? CONSTANT_InterfaceMethodref : CONSTANT_Methodref));
        /*
         * Resolve the method now if necessary
         */
        Method method;
        if (entries[index] instanceof FieldOrMethod) {
            FieldOrMethod entry = (FieldOrMethod)entries[index];
            Klass declaringClass = entry.getDefiningClass(this, context);
            NameAndType nt = getNameAndType(entry.nameAndTypeIndex);
            String name = nt.name;

            verifyName(name, ValidNameFormat.METHOD);
            boolean isSpecialMethod = !invokeinterface && (name.endsWith("init>"));
            MethodSignature sig = verifyMethodType(nt.sig, isSpecialMethod, isStatic);
            Klass returnType = sig.returnType;

            if (!invokeinterface) {
                if (name.equals("<init>")) {
                    /*
                     * Change the signature of a constructor to return an instance of the parent type.
                     */
                    sig = sig.modifyReturnType(declaringClass);
                    /*
                     * <init> methods are stored in the static array
                     */
                    isStatic = true;
                }
            }

            method = declaringClass.lookupMethod(name, sig.parameterTypes, returnType, definedClass, isStatic);
            if (method == null) {
                method = declaringClass.lookupMethod(name, sig.parameterTypes, returnType, definedClass, !isStatic);
                if (method != null) {
                	throw new com.sun.squawk.translator.IncompatibleClassChangeError(context.prefix(method.toString()));
                } else {
                    /*
                     * NoSuchMethodError
                     */
                    String message = declaringClass.getName() + '.' + nt.name + nt.sig;
                    throw new com.sun.squawk.translator.NoSuchMethodError(context.prefix(message));
                }
            }

            if (invokeinterface != method.getDefiningClass().isInterface() && method.getDefiningClass() != Klass.OBJECT) {

                if (!invokeinterface && declaringClass.isAbstract() && (declaringClass != method.getDefiningClass())) {
                    /*
                     * A call to an interface method via a receiver whose static type is an abstract
                     * class that implements the interface will be compiled as an invokevirtual. Given
                     * that the abstract class may not actually define the method itself, it is not
                     * guaranteed to have a vtable entry for the method. As such, the invokevirtual
                     * will be converted into an invokeinterface by the translator and the lookup
                     * will succeed against the runtime receiver instance (which must be of a concrete
                     * class that has a vtable entry for the method). For example:
                     *
                     * interface I {
                     *     void m();
                     * }
                     *
                     * abstract class A implements I {}
                     *
                     * class C extends A {
                     *     public void m() {}
                     * }
                     *
                     * class D {
                     *     void f() {
                     *         A a = new C();
                     *         a.m();     // <--- invokevirtual
                     *     }
                     * }
                     *
                     */
                } else {
                    String message;
                    if (invokeinterface) {
                        message = "invokeinterface cannot be applied to method '" + name + "' declared in " + declaringClass;
                    } else {
                        message = "invokevirtual cannot be applied to method '" + name + "' declared in " + declaringClass;
                    }
                    throw new com.sun.squawk.translator.IncompatibleClassChangeError(context.prefix(message));
                }
            }

            /*
             * Ensure the class that defined the field and its type are loaded
             */
            translator.load(returnType);
            for (int i = 0; i != sig.parameterTypes.length; ++i) {
                translator.load(sig.parameterTypes[i]);
            }

            /*
             * Write the resolved entry in the constant pool
             */
            entries[index] = method;

            /*
             * Since access only depends on the class, and not on the
             * specific byte code used to access the method, we don't need
             * to perform this check if the constant pool entry
             * has already been resolved.
             */
             if (!method.isAccessibleFrom(definedClass)) {
            	 throw new com.sun.squawk.translator.IllegalAccessError(context.prefix(method.toString()));
             }
        } else {
            method = (Method)entries[index];
        }
        return method;
    }


    /*---------------------------------------------------------------------------*\
     *                             Pool loading                                  *
    \*---------------------------------------------------------------------------*/

    /**
     * An instance if <code>NameAndType</code> is used to store a
     * partially resolved class, method or field entry.
     */
    private static class NameAndType {
        final String name;
        final String sig;
        NameAndType(String name, String sig) {
            this.name = name;
            this.sig  = sig;
        }
    }

    /**
     * A instance of <code>FieldOrMethod</code> represents the class pool
     * entry for a field, method or interface method before it is resolved.
     */
    private static class FieldOrMethod {
        final int classIndex;
        final int nameAndTypeIndex;
        FieldOrMethod(int classIndex, int nameAndTypeIndex) {
            this.classIndex       = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        Klass getDefiningClass(ConstantPool pool, Context context) {
            Klass klass = pool.getResolvedClass(classIndex, context);
            if (klass.isArray()) {
                throw pool.cfr.formatError("expected non-array class");
            }
            return klass;
        }
    }

    /**
     * Create a ConstantPool instance.
     *
     * @param translator    the translation context
     * @param cfr           the class file reader used for reading
     * @param definedClass  the class to which the constant pool belongs
     */
    public ConstantPool(Translator translator, ClassFileReader cfr, Klass definedClass) {
        this.translator = translator;
        this.methodSigCache = new SquawkHashtable();
        this.cfr = cfr;
        this.definedClass = definedClass;

        /*
         * Read the constant pool entry count
         */
        int count = cfr.readUnsignedShort("cp-count");

        /*
         * Allocate the required lists
         */
        tags      = new byte[count];
        entries   = new Object[count];
        int[] raw = new int[count];

        /*
         * Read the constant pool entries from the classfile
         * and initialize the constant pool correspondingly.
         * Remember that constant pool indices start from 1
         * rather than 0 and that last index is count-1.
         */

        /*
         * Pass 1 read in the primitive values
         */
        Isolate isolate = VM.getCurrentIsolate();
        for (int i = 1 ; i < count ; i++) {
            int tag = cfr.readUnsignedByte("cp-tag");
            tags[i] = (byte)tag;
            switch (tag) {
                case CONSTANT_Utf8: {
                    entries[i] = cfr.readUTF("CONSTANT_Utf8");
                    break;
                }
                case CONSTANT_Integer: {
                    entries[i] = new Integer(cfr.readInt("CONSTANT_Integer"));
                    break;
                }
                case CONSTANT_Float: {
/*if[FLOATS]*/
                    entries[i] = new Float(cfr.readFloat("CONSTANT_Float"));
                    break;
/*else[FLOATS]*/
//                  throw new Error(cfr.getFileName() + ": no floating point");
/*end[FLOATS]*/
                }

                case CONSTANT_Double: {
/*if[FLOATS]*/
                    entries[i] = new Double(cfr.readDouble("CONSTANT_Double"));
                    i++; // Doubles take two slots
                    if (i == count) {
                        throw cfr.formatError("invalid 64 bit constant in constant pool");
                    }
                    break;
/*else[FLOATS]*/
//                  throw new Error(cfr.getFileName() + ": no floating point");
/*end[FLOATS]*/
                }
                case CONSTANT_Long: {
                    entries[i] = new Long(cfr.readLong("CONSTANT_Long"));
                    i++; // Longs take two slots
                    if (i == count) {
                        throw cfr.formatError("invalid 64 bit constant in constant pool");
                    }
                    break;
                }
                case CONSTANT_String:
                case CONSTANT_Class: {
                    raw[i] = cfr.readUnsignedShort("CONSTANT_String/Class");
                    break;
                }
                case CONSTANT_Fieldref:
                case CONSTANT_Methodref:
                case CONSTANT_InterfaceMethodref:
                case CONSTANT_NameAndType: {
                    raw[i] = (cfr.readUnsignedShort("CONSTANT_F/M/I/N-1") << 16) | (cfr.readUnsignedShort("CONSTANT_F/M/I/N-2") & 0xFFFF);
                    break;
                }

                default: {
                    throw cfr.formatError("invalid constant pool entry");
                }
            }
        }

        /*
         * Pass 2 fixup types and strings
         */
        for (int i = 1 ; i < count ; i++) {
            try {
                switch (tags[i]) {
                    case CONSTANT_Class:
                    case CONSTANT_String: {
                        verifyEntry(raw[i], CONSTANT_Utf8);
                        entries[i] = (tags[i] == CONSTANT_String ? Isolate.intern((String)entries[raw[i]]) : entries[raw[i]]);
                        raw[i] = 0;
                        break;
                    }
                    case CONSTANT_NameAndType: {
                        fixupNameAndType(i, raw, null);
                        break;
                    }
                    case CONSTANT_Fieldref:
                    case CONSTANT_Methodref:
                    case CONSTANT_InterfaceMethodref: {
                        int classNameAndType = raw[i];
                        int classIndex = classNameAndType >> 16;
                        int nameAndTypeIndex = classNameAndType & 0xFFFF;
                        verifyEntry(classIndex, CONSTANT_Class);
                        verifyEntry(nameAndTypeIndex, CONSTANT_NameAndType);
                        ValidNameFormat format = tags[i] == CONSTANT_Fieldref ? ValidNameFormat.FIELD : ValidNameFormat.METHOD;
                        NameAndType nt = fixupNameAndType(nameAndTypeIndex, raw, format);
                        if (tags[i] == CONSTANT_Fieldref) {
                            verifyFieldType(nt.sig);
                        } else {
                            // MethodRefs should never refer to <clinit>, only <init> and normal methods
                            verifyMethodType(nt.sig, nt.name.endsWith("init>"), true);
                            if (nt.name.startsWith("<") && !nt.name.equals("<init>")) {
                                throw new com.sun.squawk.translator.ClassFormatError("Illegal Method reference name " + nt.name);
                            }
                        }
                        entries[i] = new FieldOrMethod(classIndex, nameAndTypeIndex);
                        break;
                    }

                }
            } catch (ArrayIndexOutOfBoundsException obe) {
                throw cfr.formatError("invalid constant pool index");
            }
        }
    }

    /**
     * Resolves a raw NameAndType entry if it hasn't already been resolved.
     *
     * @param index    the index of a NameAndType entry
     * @param raw      the unresolved pool entries
     * @param  format  if non-null, the format the name must conform to
     * @return NameAndType the resolved entry
     */
    private NameAndType fixupNameAndType(int index, int[] raw, ValidNameFormat format) {
        if (entries[index] == null) {
            int nameAndType = raw[index];
            int nameIndex = nameAndType >> 16;
            int descriptorIndex = nameAndType & 0xFFFF;
            verifyEntry(nameIndex, CONSTANT_Utf8);
            verifyEntry(descriptorIndex, CONSTANT_Utf8);
            entries[index] = new NameAndType(getUtf8(nameIndex), (String) entries[descriptorIndex]);
        }
        NameAndType entry = (NameAndType)entries[index];
        if (format != null) {
            verifyName(entry.name, format);
        }
        return entry;
    }
}
