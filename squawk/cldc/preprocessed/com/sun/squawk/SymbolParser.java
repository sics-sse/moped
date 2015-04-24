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
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.SquawkVector;
import com.sun.squawk.util.Tracer;
import com.sun.squawk.vm.CID;

/**
 * This is a utility class for parsing and reifying the components in
 * the symbols of a class.<p>
 *
 * The format of the symbol data is described by the following pseudo-C
 * structures:
 * <p><hr><blockquote><pre>
 *  symbols {
 *      u1      flags;        // indicates which sections exist. A
 *                            // section exists if bit at position 'n'
 *                            // is set where 'n' is the constant
 *                            // denoting the section
 *      section sections[n];  // invariant: n >= 0
 *  }
 *
 *  section {
 *      u1     category;     // invariant: INSTANCE_FIELDS <= category <= STATIC_METHODS
 *      member members[n];   // invariant: n > 0
 *  }
 *
 *  member {
 *      u2   length;       // length of member after 'length' item [invariant: length > STATIC_METHODS]
 *      u2   access_flags;
 *      u2   offset;
 *      utf8 name;
 *      u4   type;          // returnType for method/type for field
 *      union {
 *          u4   parameters[];  // method parameters: occupies remainder of struct
 *          u1   value[n];      // constant value
 *      }
 *  }
 * </pre></blockquote><hr><p>
 *
 * The structures described above are actually stored in a byte array
 * encoded and decoded with a {@link ByteBufferEncoder} and
 * {@link ByteBufferDecoder} respectively.
 *
 */

final class SymbolParser extends ByteBufferDecoder {

    private SymbolParser(byte[] symbols, Klass[] classTable) {
        super(symbols, 0);
        this.classTable = classTable;
    }


    /*---------------------------------------------------------------------------*\
     *                         Static fields and methods                         *
    \*---------------------------------------------------------------------------*/

    /**
     * A constant denoting the instance fields member category.
     */
    final static int INSTANCE_FIELDS = 0;

    /**
     * A constant denoting the static fields member category.
     */
    final static int STATIC_FIELDS = 1;

    /**
     * A constant denoting the virtual methods member category.
     */
    final static int VIRTUAL_METHODS = 2;

    /**
     * A constant denoting the static methods member category.
     */
    final static int STATIC_METHODS  = 3;

    /**
     * The first cached parser.
     */
    private static SymbolParser p1;

    /**
     * The first cached parser.
     */
    private static SymbolParser p2;

    /**
     * Create a symbols parser.
     *
     * @param  symbols    the symbols to parse
     * @param  classTable the table of classes that are referenced from <code>symbols</code>
     * @return the created parser
     */
    static SymbolParser create(byte[] symbols, Klass[] classTable) {

//        KlassMetadata.Debug.creates++;

        
        if (p1 != null && p1.buf == symbols) {
            return p1;
        }
        if (p2 == null) {
            p2 = new SymbolParser(symbols, classTable);
        }
        if (p2.buf != symbols) {

//            KlassMetadata.Debug.createMisses++;

            p2.setup(symbols, classTable);
        }

        /*
         * Swap p1 and p2 so that the other one is replaced next time
         */
        SymbolParser temp = p2;
        p2 = p1;
        p1 = temp;
        return p1;
    }

    /**
     * Create a symbols parser. The returned parser is positioned at the
     * method or field identified by <code>id</code>.
     *
     * @param  symbols    the symbols to parse
     * @param  classTable the table of classes that are referenced from <code>symbols</code>
     * @param  id         the identifier of the method or field to select
     * @return the created parser
     */
    static SymbolParser create(byte[] symbols, Klass[] classTable, int id) {
        SymbolParser parser = create(symbols, classTable);
        parser.select(id);
        return parser;
    }

    /**
     * Flush the cached section parser objects
     */
    static void flush() {
        p1 = p2 = null;
        symbolsBuffer = null;
    }

    /**
     * Creates a serialized representation of all the symbolic information
     * pertaining to the fields and methods of a class.
     *
     * @param  virtualMethods the virtual methods of the class
     * @param  staticMethods  the static methods of the class
     * @param  instanceFields the instance fields of the class
     * @param  staticFields   the static fields of the class
     * @param  types          the collection to which the types in the signatures of the members should be added
     * @return a serialized representation of all the symbolic in the
     *                        given fields and methods
     */
    synchronized static byte[] createSymbols(
                                              ClassFileMethod[] virtualMethods,
                                              ClassFileMethod[] staticMethods,
                                              ClassFileField[]  instanceFields,
                                              ClassFileField[]  staticFields,
                                              SquawkVector types
                                            ) {
        if (symbolsBuffer == null) {
            symbolsBuffer = new ByteBufferEncoder();
            membersBuffer = new ByteBufferEncoder();
        }
        symbolsBuffer.reset();

        // add place holder for flags
        int flags = 0;
        if (instanceFields.length != 0) {
            flags |= 1 << INSTANCE_FIELDS;
        }
        if (staticFields.length != 0) {
            flags |= 1 << STATIC_FIELDS;
        }
        if (virtualMethods.length != 0) {
            flags |= 1 << VIRTUAL_METHODS;
        }
        if (staticMethods.length != 0) {
            flags |= 1 << STATIC_METHODS;
        }
        symbolsBuffer.addUnsignedByte(flags);

        serializeFields(INSTANCE_FIELDS, instanceFields, types);
        serializeFields(STATIC_FIELDS,   staticFields, types);
        serializeMethods(VIRTUAL_METHODS, virtualMethods, types);
        serializeMethods(STATIC_METHODS,  staticMethods, types);

        return symbolsBuffer.toByteArray();
    }

    /**
     * Serialize the symbolic information for a set of fields into a byte buffer.
     *
     * @param category the category of the fields (must be INSTANCE_FIELDS or STATIC_FIELDS).
     * @param fields   the set of fields to serialize
     * @param types    the collection to which the types in the signatures of the fields should be added
     */
    private static void serializeFields(int category, ClassFileField[] fields, SquawkVector types) {
        if (false) Assert.that(category == INSTANCE_FIELDS || category == STATIC_FIELDS);
        if (fields.length != 0) {
            symbolsBuffer.addUnsignedByte(category);
            for (int i = 0; i != fields.length; ++i) {
                ClassFileField field = fields[i];
                int modifiers = field.getModifiers();
                Klass type = field.getType();
                membersBuffer.reset();
                membersBuffer.addUnsignedShort(modifiers);
                membersBuffer.addUnsignedShort(field.getOffset());
                membersBuffer.addUtf8(field.getName());
                membersBuffer.addUnsignedShort(KlassMetadata.addSignatureType(types, type));
                if (Modifier.hasConstant(modifiers)) {
                    if (type.getSystemID() == CID.STRING) {
                        membersBuffer.addUtf8(((ClassFileConstantField)field).stringConstantValue);
                    } else {
                        long value = ((ClassFileConstantField)field).primitiveConstantValue;
                        int dataSize = type.getDataSize();
                        for (int bite = 0; bite != dataSize; ++bite) {
                            membersBuffer.addUnencodedByte((byte)value);
                            value = value >> 8;
                        }
                    }
                }
                symbolsBuffer.add(membersBuffer);
            }
        }
    }

    /**
     * Serialize the symbolic information for a set of methods into a byte buffer.
     *
     * @param category the category of the methods (must be VIRTUAL_METHODS or STATIC_METHODS).
     * @param methods  the set of methods to serialize
     * @param types    the collection to which the types in the signatures of the fields should be added
     */
    private static void serializeMethods(int category, ClassFileMethod[] methods, SquawkVector types) {
        if (false) Assert.that(category == VIRTUAL_METHODS || category == STATIC_METHODS);
        if (methods.length != 0) {
            symbolsBuffer.addUnsignedByte(category);
            for (int i = 0; i != methods.length; ++i) {
                ClassFileMethod method = methods[i];
                int pragmas = method.getPragmas();
                int mod = method.getModifiers();

                membersBuffer.reset();

                if (false) Assert.that(Modifier.hasPragmas(mod) == (pragmas != 0));
                if (false) Assert.that(PragmaException.isHosted(pragmas) == (method.getOffset() == 0xFFFF));

                membersBuffer.addUnsignedShort(mod);
                membersBuffer.addUnsignedShort(method.getOffset());
                membersBuffer.addUtf8(method.getName());

                if (pragmas != 0) {
                    membersBuffer.addUnsignedShort(pragmas);
                }
                membersBuffer.addUnsignedShort(KlassMetadata.addSignatureType(types, method.getReturnType()));
                Klass[] parameterTypes = method.getParameterTypes();
                for (int j = 0; j != parameterTypes.length; ++j) {
                    membersBuffer.addUnsignedShort(KlassMetadata.addSignatureType(types, parameterTypes[j]));
                }
                symbolsBuffer.add(membersBuffer);
            }
        }
    }

    /**
     * The buffer used to build the serialized symbols array for a class
     */
    private static ByteBufferEncoder symbolsBuffer;

    /**
     * The buffer used to build the serialized symbols array for a set
     * of methods or fields.
     */
    private static ByteBufferEncoder membersBuffer;

    /*---------------------------------------------------------------------------*\
     *                                   Stripping                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Determines if the symbolic information for a given field or method should be retained.
     *
     * @param type       specifies a closed suite type. Must be {@link Suite#LIBRARY} or {@link Suite#EXTENDABLE_LIBRARY}.
     * @param  modifiers the modifiers of the field or method in question
     * @param  fieldType the type of the field or null if this is a method
     * @return true if the symbolic information for <code>member</code> should be retained
     */
    private static boolean retainMember(int type, int modifiers, Klass fieldType) {
        // Discard primitive constants
        /*
         * It turns out that the TCK has .class files that use the getstatic bytecode to reference static constants, such as Double.MIN_VALUE.
         * In order to have the TCK pass, we need to keep these constants around.  Note that adding these constants seemed to add
         * 7 K to size of squawk.suite.
         * 
         * WAIT - doesnt tanslator transform the getstatic into a constant anyway?
         * NO - it has to leave the getstatic to get the exception thrown. Could transform to error func though...
         */
//        if (fieldType != null && Modifier.hasConstant(modifiers) && fieldType.isPrimitive() && Modifier.isFinal(modifiers)) {
//            return false;
//        }
        if (type == Suite.LIBRARY) {
            return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers);
        } else {
            if (false) Assert.that(type == Suite.EXTENDABLE_LIBRARY);
            return Modifier.isPublic(modifiers) || Modifier.isProtected(modifiers) || Modifier.isPackagePrivate(modifiers);
        }
    }

    /**
     * Prunes the symbols based on a given suite type.
     *
     * @param klass  the enclosing class
     * @param type   specifies a closed suite type. Must be {@link Suite#LIBRARY} or {@link Suite#EXTENDABLE_LIBRARY}.
     * @param types  the collection to which the types in the signatures of the remaining members should be added
     * @return the stripped symbols
     */
    synchronized byte[] strip(Klass klass, int type, SquawkVector types) {
        if (symbolsBuffer == null) {
            symbolsBuffer = new ByteBufferEncoder();
            membersBuffer = new ByteBufferEncoder();
        }
        symbolsBuffer.reset();

        // Place holder for flags
        int flagsPos = symbolsBuffer.count;
        symbolsBuffer.addUnsignedByte(0);

        int flags = 0;
        flags |= stripFields(klass, type, INSTANCE_FIELDS, types);
        flags |= stripFields(klass, type, STATIC_FIELDS, types);
        flags |= stripMethods(klass, type, VIRTUAL_METHODS, types);
        flags |= stripMethods(klass, type, STATIC_METHODS, types);

        symbolsBuffer.buffer[flagsPos] = (byte) flags;

        return symbolsBuffer.toByteArray();
    }

    private boolean keepForRuntimeStatics(Klass klass, Klass fieldType, int category) {
        if ((klass.getModifiers() & Modifier.COMPLETE_RUNTIME_STATICS) != 0
                && (category == STATIC_FIELDS)
                && Modifier.hasConstant(modifiers)
                && (fieldType != null)
                && (fieldType.isPrimitive() || fieldType == Klass.STRING)) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Prunes the fields based on a given suite type.
     *
     * @param klass     the enclosing class
     * @param type      specifies a closed suite type. Must be {@link Suite#LIBRARY} or {@link Suite#EXTENDABLE_LIBRARY}.
     * @param category  specifies instance or static fields
     * @param types     the collection to which the types in the signatures of the remaining fields should be added
     * @return an integer with the only the bit in position 'category' set if at least one field was not stripped otherwise 0
     */
    private int stripFields(Klass klass, int type, int category, SquawkVector types) {
        if (false) Assert.that(category == INSTANCE_FIELDS || category == STATIC_FIELDS);
        int count = getMemberCount(category);
        boolean keptAtLeastOne = false;
        if (count != 0) {
            for (int i = 0; i != count; ++i) {
                select(category, i);
                Klass fieldType = getSignatureType(getSignatureAt(0));
                Field field = klass.getField(i, category == STATIC_FIELDS);
                if (keepForRuntimeStatics(klass, fieldType, category)
                        || (retainMember(type, modifiers, fieldType) && VM.isExported(field))) {
                    if (!keptAtLeastOne) {
                        symbolsBuffer.addUnsignedByte(category);
                        keptAtLeastOne = true;
                    }
                    membersBuffer.reset();
                    membersBuffer.addUnsignedShort(modifiers);
                    membersBuffer.addUnsignedShort(getOffset());
                    membersBuffer.addUtf8(getName());
                    if (Modifier.hasPragmas(modifiers)) {
                        membersBuffer.addUnsignedShort(0);
                    }
                    membersBuffer.addUnsignedShort(KlassMetadata.addSignatureType(types, fieldType));
                    if (Modifier.hasConstant(modifiers)) {
                        if (!fieldType.isPrimitive()) {
                            membersBuffer.addUtf8(getStringConstantValue());
                        } else {
                            long value = getPrimitiveConstantValue();
                            int dataSize = fieldType.getDataSize();
                            for (int bite = 0; bite != dataSize; ++bite) {
                                membersBuffer.addUnencodedByte((byte)value);
                                value = value >> 8;
                            }
                        }
                    }
                    symbolsBuffer.add(membersBuffer);
                } else if (Klass.TRACING_ENABLED && Tracer.isTracing("stripping")) {
                    Tracer.trace("  discarded metadata for field: " + fieldType.getInternalName() + " " + getName());
                    if (Modifier.hasConstant(modifiers)) {
                        Tracer.trace(" [constantValue=" + (fieldType.isPrimitive() ? ""+getPrimitiveConstantValue() : getStringConstantValue()) + "]");
                    }
                    Tracer.traceln("");
                }

            }
        }
        return keptAtLeastOne ? 1 << category : 0;
    }

    /**
     * Prunes the methods based on a given suite type.
     * 
     * @todo Now we're keeping symbols for all methods if lnt is true. But we can strip symbols for methods that have been eliminated.
     *
     * @param klass     the enclosing class
     * @param type      specifies a closed suite type. Must be {@link Suite#LIBRARY} or {@link Suite#EXTENDABLE_LIBRARY}.
     * @param category  specifies virtual or static methods
     * @param types     the collection to which the types in the signatures of the remaining methods should be added
     * @return an integer with the only the bit in position 'category' set if at least one method was not stripped otherwise 0
     */
    private int stripMethods(Klass klass, int type, int category, SquawkVector types) {
        if (false) Assert.that(category == VIRTUAL_METHODS || category == STATIC_METHODS);
        int count = getMemberCount(category);
        boolean keptAtLeastOne = false;
        if (count != 0) {
            for (int i = 0; i != count; ++i) {
                select(category, i);
                String name = getName();
                if (!PragmaException.isHosted(pragmas) &&               // strip methods called only in hosted VM mode
                    !PragmaException.isInterpreterInvoked(pragmas) &&   // strip methods called from the interpreter
                    (MethodMetadata.lineNumberTablesKept() ||           // if we want line numbers then we want method names too...
                        (retainMember(type, modifiers, null) &&
                        VM.isExported(klass.getMethod(i, category == STATIC_METHODS)))))
                {
                    // keeping this method:
                    if (!keptAtLeastOne) {
                        symbolsBuffer.addUnsignedByte(category);
                        keptAtLeastOne = true;
                    }
                    membersBuffer.reset();
                    membersBuffer.addUnsignedShort(modifiers);
                    membersBuffer.addUnsignedShort(getOffset());
                    membersBuffer.addUtf8(name);
                    if (Modifier.hasPragmas(modifiers)) {
                        membersBuffer.addUnsignedShort(pragmas);
                    }
                    int sigCount = getSignatureCount();
                    for (int j = 0; j != sigCount; ++j) {
                        membersBuffer.addUnsignedShort(KlassMetadata.addSignatureType(types, getSignatureType(getSignatureAt(j))));
                    }
                    symbolsBuffer.add(membersBuffer);
                } else {
                    // Stripping this method:
                    if ((Modifier.isAbstract(modifiers) || klass.isInterface())
                        && !(Modifier.isPackagePrivate(modifiers) || Modifier.isSuitePrivate(klass.getModifiers()))) {
                        // If a class with abstract methods, or an interface, is exported from a suite, but the abstract methods are not exported,
                        // then there is no way to extend or implement the exported class or interface.
                        throw new IllegalStateException("Can't strip method " + name + " because it is abstract in a class exported from a suite: " + klass);
                    }
                    
                    if (Klass.TRACING_ENABLED && Tracer.isTracing("stripping")) {
                        String signature = name;
                        int parameterCount = getSignatureCount() - 1;
                        if (parameterCount == 0) {
                            signature += "()";
                        } else {
                            StringBuffer strbuf = new StringBuffer(15);
                            strbuf.append('(');
                            for (int j = 0 ; j < parameterCount ; j++) {
                                Klass parameterType = getSignatureType(getSignatureAt(j + 1));
                                strbuf.append(parameterType.getInternalName());
                                if (j != parameterCount - 1) {
                                    strbuf.append(',');
                                }
                            }
                            strbuf.append(')');
                            signature += strbuf.toString();
                        }
                        signature = getSignatureType(getSignatureAt(0)).getInternalName() + " " + signature;
                        Tracer.traceln("  discarded metadata for method: " + signature);
                    }
                }
            }
        }
        return keptAtLeastOne ? 1 << category : 0;
    }

    /*---------------------------------------------------------------------------*\
     *                                 Fields                                    *
    \*---------------------------------------------------------------------------*/

    /**
     * The table of classes that are referenced from the symbols.
     */
    private Klass[] classTable;

    /**
     * The offsets to the method and field sections.
     */
    private short[] sectionStart = new short[STATIC_METHODS + 1];

    /**
     * The number of entries in each method and field section.
     */
    private short[] sectionCount = new short[STATIC_METHODS + 1];

    /**
     * The identifier of the currently selected method or field.
     */
    private short selection;

    /**
     * Flags whether or not the sections have been parsed.
     */
    private boolean sectionsParsed;

    /**
     * The access flags of the currently selected method or field.
     */
    private int modifiers;

    /**
     * The pragma flags of the currently selected method or field.
     */
    private int pragmas;

    /**
     * The offset of the currently selected method or field.
     */
    private int offset;

    /**
     * The index of the name of the currently selected method or field.
     */
    private short nameStart;

    /**
     * The number of characters in the name (which may differ from its
     * UTF8 encoded length).
     */
    private int nameLength;

    /**
     * The index of the type of the currently selected field or
     * return type for the currently selected method.
     */
    private short signatureStart;

    /**
     * The number of components in the signature of the currently
     * selected field or method. If a method is currently selected, then
     * the first component of the signature is the return type and the
     * remainder of the signature is the parameter types. If a field
     * is currently selected, then there is only one component in the
     * signature which is the field's declared type. If the field has a constant
     * value then 'signatureCount - 1' is the data size (in bytes) of the value.
     */
    private short signatureCount;

    /**
     * Reads an encoded type identifier from the symbols.
     *
     * @return  the decoded type identifier
     */
    private int readTypeID() {
        return readUnsignedShort();
    }


    /*---------------------------------------------------------------------------*\
     *                      Section selection and parsing                        *
    \*---------------------------------------------------------------------------*/

    /**
     * Reconfigures the parser based on a new symbols array.
     *
     * @param  symbols    the new symbols that will be parsed
     * @param  classTable the table of classes that are referenced from <code>symbols</code>
     */
    private void setup(byte[] symbols, Klass[] classTable) {
        this.buf = symbols;
        this.pos = 0;
        this.classTable = classTable;
        sectionsParsed = false;
        selection = -1;
    }

    /**
     * Parses a single member section. The current parse position must be
     * at the section identifier byte.
     */
    private int parseSection() {
        int section = buf[pos];
        if (false) Assert.that(section >= INSTANCE_FIELDS && section <= STATIC_METHODS);
        pos++;
        sectionStart[section] = (short)pos;
        sectionCount[section] = 0;
        while (pos < buf.length) {
            int lengthOrSection = buf[pos];
            /*
             * Is the length actually a new section header?
             */
            if (lengthOrSection >= 0 &&
                lengthOrSection <= STATIC_METHODS) {
                break;
            }
            lengthOrSection = readUnsignedShort();
            pos += lengthOrSection;
            sectionCount[section]++;
        }
        return section;
    }

    /**
     * Parses the member sections.
     */
    private void parseSections() {

//        KlassMetadata.Debug.parses++;

        if (!sectionsParsed) {

//            KlassMetadata.Debug.parseMisses++;

            sectionStart[STATIC_FIELDS]   = 0;
            sectionCount[STATIC_FIELDS]   = 0;
            sectionStart[INSTANCE_FIELDS] = 0;
            sectionCount[INSTANCE_FIELDS] = 0;
            sectionStart[STATIC_METHODS]  = 0;
            sectionCount[STATIC_METHODS]  = 0;
            sectionStart[VIRTUAL_METHODS] = 0;
            sectionCount[VIRTUAL_METHODS] = 0;
            pos = 1;
            while (pos < buf.length) {
                parseSection();
            }
            sectionsParsed = true;
        }
    }

    /**
     * Determines if a given member section is empty.
     *
     * @param   category  the section of class members to test
     * @return  true if section <code>category</code> is empty
     */
    private boolean isSectionEmpty(int category) {
        return (buf[0] & (1 << category)) == 0;
    }

    /**
     * Gets the number entries in a section of fields or methods.
     *
     * @param   category  the section of class members to count
     * @return  the number of fields or methods counted
     */
    int getMemberCount(int category) {
        if (buf == null ||isSectionEmpty(category)) {
            return 0;
        }
        parseSections();
        return sectionCount[category];
    }

    /**
     * Gets the identifier for a field or method.
     *
     * @param   category  the section of class members to search
     * @param   index  the index of the required field or method
     * @return  the identifer of the indexed field or method
     */
    int getMemberID(int category, int index) {
        if (false) Assert.that(!isSectionEmpty(category));
        parseSections();
        if (false) Assert.that(sectionCount[category] > index);
        pos = sectionStart[category];
        while (index-- > 0) {
            int length = readUnsignedShort();
            pos += length;
        }
        return pos;
    }

    /*---------------------------------------------------------------------------*\
     *                    Field or method component selection                    *
    \*---------------------------------------------------------------------------*/

    /**
     * Select a field or method.
     *
     * @param   category  the section of class members to search
     * @param   index     the index of the field or method to select
     */
    private void select(int category, int index) {
        int id = getMemberID(category, index);
        if (false) Assert.that(id > 0);
        select(id);
    }

    /**
     * Select a field or method.
     *
     * @param   id  the identifier of the fields or method to select
     * @return  the identifier of the next field or method (this value is only valid
     *              if the category has more members)
     */
    private int select(int id) {

//        KlassMetadata.Debug.selects++;

        int next = -1;
        if (selection != id) {

//            KlassMetadata.Debug.selectMisses++;

            selection  = (short)id;
            pos        = (short)id;
            int length = readUnsignedShort();
            next       = pos + length;
            modifiers  = readUnsignedShort();
            offset     = readUnsignedShort();
            nameLength = readUnsignedShort();
            nameStart  = (short)pos;
            for (int i = 0 ; i < nameLength ; i++) {
                readChar();
            }
            if (Modifier.hasPragmas(modifiers)) {
                pragmas = readUnsignedShort();
                if (PragmaException.isHosted(pragmas)) {
                    offset = -1;
                }
            } else {
                pragmas = 0;
            }
            signatureStart = (short)pos;

            if (Modifier.hasConstant(modifiers)) {
                readTypeID();
                signatureCount = (short)(1 + (next - pos));
            } else {
                signatureCount = 0;
                while (pos < next) {
                    readTypeID();
                    signatureCount++;
                    if (false) Assert.that(pos <= next);
                }
            }
            pos = nameStart;
            //int ch = buf[pos]; // First character of name
        } else {
            pos = (short)id;
            int length = readUnsignedShort();
            next = pos + length;
        }
        return next;
    }

    /**
     * Parses the method or field signature the parser is currently
     * positioned at. The parsing is cut short if the signature does not
     * match the given signature components.
     *
     * @param   name               the name component of a signature
     * @param   parameterTypes     the parameter types of a method
     *                             signature. This will be a zero length
     *                             array when parsing a field signature
     * @param   returnOrFieldType  the return type of a method signature or
     *                             the type of a field signature
     * @param   end                the index in symbols of the byte
     *                             immediately the signature
     * @param   hasPragmas         whether we should read a short for
     *                             pragma flags after reading the name
     * @return  true if the parsed signature matches the given signature
     *                             components
     */
    private boolean matchSignature(String name, Klass[] parameterTypes, Klass returnOrFieldType, int end, boolean hasPragmas) {
        if (nameLength != name.length()) {
            return false;
        }
        nameStart = (short)pos;
        for (int i = 0 ; i < nameLength ; i++) {
            int c = readChar();
            if (c != name.charAt(i)) {
                return false;
            }
        }
        if (hasPragmas) {
            pragmas = readUnsignedShort();
        } else {
            pragmas = 0;
        }
        signatureStart = (short)pos;
        signatureCount = 1;
        if (getSignatureType(readTypeID()) == returnOrFieldType ||
            (name.equals("<init>") && returnOrFieldType == Klass.VOID)) {

            if (Modifier.hasConstant(modifiers)) {
                signatureCount += (short)(end - pos);
                return true;
            }
            for (int i = 0 ; i < parameterTypes.length && pos < end ; i++) {
                if (getSignatureType(readTypeID()) != parameterTypes[i]) {
                    return false;
                }
                signatureCount++;
            }
            return pos == end && signatureCount == (parameterTypes.length + 1);
        } else {
            return false;
        }
    }

    /**
     * Searches for a method or field based on a signature and returns the
     * identifier of the method or field if it was found or -1 otherwise.
     *
     * @param   category           the section of class members to search
     * @param   name               the name component of a signature
     * @param   parameterTypes     the parameter types of a method
     *                             signature. This will be a zero length
     *                             array when parsing a field signature
     * @param   returnOrFieldType  the return type of a method signature or
     *                             the type of a field signature
     * @return  the identifier of the method or field found that matches
     *          the given signature or -1 if none was found
     */
    int lookupMember(int category, String name, Klass[] parameterTypes, Klass returnOrFieldType) {
        if (isSectionEmpty(category)) {
            return -1;
        }
        parseSections();
        int count = this.sectionCount[category];
        pos = sectionStart[category];
        for (int index = 0; index != count; ++index) {
            selection  = (short)pos;
            int length = readUnsignedShort();
            int end    = pos + length;
            modifiers  = readUnsignedShort();
            offset     = readUnsignedShort();
            nameLength = readUnsignedShort();
            if (matchSignature(name, parameterTypes, returnOrFieldType, end, Modifier.hasPragmas(modifiers))) {
                return selection;
            }
            pos = (short)end;
        }
        this.selection = -1;
        return -1;
    }

    /**
     * Searches for a method based on an offset and returns the
     * identifier of the method if it was found or -1 otherwise.
     *
     * @param   category           the section of class methods to search
     * @param   offset             the offset to match
     * @return  the identifier of the method found that matches the given offset and type or -1 if none was found
     */
    int lookupMethod(int category, int offset) {
        if (isSectionEmpty(category)) {
            return -1;
        }
        parseSections();
        int count = this.sectionCount[category];
        pos = sectionStart[category];
        int id = pos;
        for (int index = 0; index != count; ++index) {
            int nextID = select(id);
            if (this.offset == offset) {
                if (category == STATIC_METHODS || category == VIRTUAL_METHODS) {
                    return id;
                }
            }
            id = nextID;
        }
        this.selection = -1;
        return -1;
    }

    /*---------------------------------------------------------------------------*\
     *                   Field or method component accessors                     *
    \*---------------------------------------------------------------------------*/

    /**
     * Gets the access flags for the currently selected field or method.
     *
     * @return a mask of the constants defined in {@link Modifier}
     */
    int getModifiers() {
        if (false) Assert.that(selection != -1);
        return modifiers;
    }

    /**
     * Gets the pragma flags for the currently selected field or method.
     *
     * @return a mask of the constants defined in {@link com.sun.squawk.pragma.PragmaException}
     */
    int getPragmas() {
        if (false) Assert.that(selection != -1);
        return pragmas;
    }

    /**
     * Gets the offset of the currently selected field or method.
     *
     * @return the offset of the currently selected field or method
     */
    int getOffset() {
        if (false) Assert.that(selection != -1);
        if (PragmaException.isHosted(pragmas)) {
            throw new Error("hosted methods have no entry in a method table");
        }
        return offset;
    }

    /**
     * Gets the name of the currently selected field or method.
     *
     * @return  the name of the currently selected field or method
     */
    String getName() {
        if (false) Assert.that(selection != -1);
        char[] chars = new char[nameLength];
        pos = nameStart;
        for (int i = 0; i != chars.length; ++i) {
            chars[i] = readChar();
        }
        return new String(chars);
    }

    /**
     * Gets the number of types in the signature of the currently selected
     * field or method.
     *
     * @return the number of types in the signature of the currently
     *         selected field or method
     */
    int getSignatureCount() {
        if (false) Assert.that(selection != -1);
        return signatureCount;
    }

    /**
     * Gets a type from the signature of the currently selected field or method.
     *
     * @param  index  the index of the type to retrieve
     * @return the type at index <code>index</code> in the signature of the currently selected field or method
     */
    Klass getSignatureTypeAt(int index) {
        return getSignatureType(getSignatureAt(index));
    }

    /**
     * Gets a type ID from the signature of the currently selected field or method.
     *
     * @param  index  the index of the type to retrieve
     * @return the type ID of the type at index <code>index</code> in the
     *                signature of the currently selected field or method
     */
    private int getSignatureAt(int index) {
        if (false) Assert.that(selection != -1);
        if (false) Assert.that(index >= 0);
        if (false) Assert.that(index < signatureCount);
        pos = signatureStart;
        int res = -1;
        while (index-- >= 0) {
            res = readTypeID();
        }
        return res;
    }

    /**
     * Decodes a type ID in the symbols to a Klass instance.
     *
     * @param typeID   the type ID to decode
     * @return the Klass instance corresponding to <code>typeID</code>
     */
    private Klass getSignatureType(int typeID) {
        return KlassMetadata.getSignatureType(classTable, typeID);
    }


    /**
     * Gets the constant value of a primitive static field.
     *
     * @return the constant value of this primitive static field
     */
    public long getPrimitiveConstantValue() {
        if (false) Assert.that(Modifier.hasConstant(modifiers));
        pos = signatureStart;
        int id = getSignatureType(readTypeID()).getSystemID();
        int dataSize = (signatureCount - 1);
        if (false) Assert.that(dataSize > 0);
        long value = 0;
        for (int bite = 0; bite != dataSize; ++bite) {
            int shift = bite * 8;
            long b = nextByte() & 0xFF;
            value |= (b << shift);
        }
        switch (id) {
            case CID.BOOLEAN:
            case CID.BYTE:    return (byte)value;
            case CID.CHAR:    return (char)value;
            case CID.SHORT:   return (short)value;

            case CID.FLOAT:

            case CID.INT:     return (int)value;

            case CID.DOUBLE:

            case CID.LONG:  return value;
            default:
                if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere(

//                                            "id = " + id

                        );
                return 0;
        }
    }

    /**
     * Gets the constant value of a String static field.
     *
     * @return the constant value of this String static field
     */
    public String getStringConstantValue() {
        if (false) Assert.that(Modifier.hasConstant(modifiers));
        pos = signatureStart;
        int id = readTypeID();
        if (false) Assert.that(getSignatureType(id).getSystemID() == CID.STRING);
        return readUtf8();
    }
}
