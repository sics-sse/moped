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

import java.io.*;
import com.sun.squawk.util.*;

/**
 * The KlassMetadata class is a container for all the meta-information
 * pertaining to a class where this information is not necessarily
 * required by the runtime system.
 *
 * For example, the names and signatures of a class's fields and methods
 * are contained in this class. These types of information are only
 * required if the runtime system includes support for features such
 * as dynamic class loading, stack traces etc.
 *
 * @version 1.0
 * @see     com.sun.squawk.Klass
 */
public class KlassMetadata {

    /**
     * The Klass instance to which the meta-information in this object pertains.
     */
    private final Klass definedClass;

    /**
     * The symbolic information for the class. This includes the signatures
     * of the fields and methods of the class. The structure encoded in this
     * array of bytes is accessed by a SymbolsParser instance.
     */
    protected byte[] symbols;

    /**
     * The table of classes that are referred to from the symbols. The class references
     * in the symbols are encoded indexes into this table.
     */
    protected Klass[] classTable;

    /**
     * KlassMetadata.Full contains debugging information of some kind (sourcefile or MethodMetadata).
     */
    static final class Full extends KlassMetadata {

        /**
         * The debug information for the virtual methods described in
         * <code>symbols</code>.
         */
        private final MethodMetadata[] virtualMethodsMetadata;

        /**
         * The debug information for the static methods described in
         * <code>symbols</code>.
         */
        private final MethodMetadata[] staticMethodsMetadata;

        /**
         * The source file from which a class was compiled. This field's value
         * may be null if the corresponding class had no SourceFile attribute.
         */
        private final String sourceFile;

        /**
         * Create a new <code>KlassMetadata.Full</code> for a <code>Klass</code> instance.
         *
         * @param definedClass   the class to which this metadata pertains
         * @param virtualMethods the virtual methods of the class
         * @param staticMethods  the static methods of the class
         * @param instanceFields the instance fields of the class
         * @param staticFields   the static fields of the class
         * @param sourceFile     the source file from which the class was compiled
         */
        Full(Klass definedClass,
                ClassFileMethod[] virtualMethods,
                ClassFileMethod[] staticMethods,
                ClassFileField[] instanceFields,
                ClassFileField[] staticFields,
                String sourceFile,
                int vtableSize,
                int stableSize) {
            super(definedClass, null, null);
            SquawkVector types = new SquawkVector();
            this.symbols = SymbolParser.createSymbols(virtualMethods, staticMethods, instanceFields, staticFields, types);
            this.sourceFile = sourceFile;
            this.virtualMethodsMetadata = new MethodMetadata[vtableSize];
            this.staticMethodsMetadata = new MethodMetadata[stableSize];

            classTable = new Klass[types.size()];
            types.copyInto(classTable);
//            if (classTable.length == 1 && classTable[0] == definedClass) {
//                System.out.println("trivial classtable for " + definedClass);
//            }
        }
      
        /**
         * Constructor for stripping.
         *
         * @param definedClass
         * @param symbols   the symbols (stripped or otherwise)
         * @param classTable
         * @param srcFile
         * @param virtualMethodsMetadata
         * @param staticMethodsMetadata
         */
        Full(Klass definedClass, byte[] symbols, Klass[] classTable, String srcFile, MethodMetadata[] virtualMethodsMetadata, MethodMetadata[] staticMethodsMetadata) {
            super(definedClass, symbols, classTable);
            if (false) Assert.that(srcFile != null || staticMethodsMetadata != null || virtualMethodsMetadata != null);
            this.sourceFile = srcFile;
            this.staticMethodsMetadata = staticMethodsMetadata;
            this.virtualMethodsMetadata = virtualMethodsMetadata;
        }

        /**
         * Get the source file from which the class was compiled.
         *
         * @return the file name
         */
        final String getSourceFileName() {
            return sourceFile;
        }

        /**
         * Get the static or instance method metedatas
         *
         * @return the file name
         */
        final MethodMetadata[] geMethodMetadata(boolean isStatic) {
            return isStatic ? staticMethodsMetadata : virtualMethodsMetadata;
        }

        /**
         * Set the debug information for a method body to the collection for the defining class.
         *
         * @param isStatic  true if the method is static
         * @param index     the index for a method body
         * @param metadata  the debug information for the method (may be null)
         */
        final void setMethodMetadata(boolean isStatic, int index, MethodMetadata metadata) {
            if (metadata != null) {
                if (isStatic) {
                    if (false) Assert.that(staticMethodsMetadata[index] == null);
                    staticMethodsMetadata[index] = metadata;
                } else {
                    if (false) Assert.that(virtualMethodsMetadata[index] == null);
                    virtualMethodsMetadata[index] = metadata;
                }
            }
        }
    }
    
    KlassMetadata(Klass definedClass, byte[] symbols, Klass[] classTable) {
    	this.definedClass = definedClass;
    	this.symbols = symbols;
    	this.classTable = classTable;
//         if (classTable.length == 1 && classTable[0] == definedClass) {
//                System.out.println("trivial2 classtable for " + definedClass);
//            }
	}

    /**
     * Adds a Klass instance that is a type in a method or field signature to a vector if it not
     * one of the classes with a fixed class ID and it is not already in the vector.
     *
     * @param types   the type vector to append to
     * @param type    the type to conditionally append to <code>types</code>
     * @return the encoded ID for <code>type</code> that can be used with {@link #getSignatureType} to retrieve <code>type</code>
     */
    static int addSignatureType(SquawkVector types, Klass type) {
        if (false) Assert.that(types != null);
        int systemID = type.getSystemID();
        if (systemID >= 0) {
            return (systemID << 1);
        } else {
            int index = types.indexOf(type);
            if (index == -1) {
                index = types.size();
                types.addElement(type);
            }
            return (index << 1) | 0x1;
        }
    }

    /**
     * Gets the Klass instance corresponding to a type ID that was generated by {@link #addSignatureType}.
     *
     * @param types   the list of types against which the type was encoded
     * @param typeID  a value returned from {@link #addSignatureType}
     * @return the Klass instance corresponding to <code>typeID</code>
     */
    static Klass getSignatureType(Klass[] types, int typeID) {
        if (false) Assert.that(types != null);
        if ((typeID & 1) == 0) {
            int systemID = typeID >>> 1;
            return VM.getCurrentIsolate().getBootstrapSuite().getKlass(systemID);
        } else {
            int index = typeID >>> 1;
            return types[index];
        }
    }

    /**
     * Set the debug information for a method body to the collection for the defining class.
     *
     * @param isStatic  true if the method is static
     * @param index     the index for a method body
     * @param metadata  the debug information for the method (may be null)
     */
    void setMethodMetadata(boolean isStatic, int index, MethodMetadata metadata) {
        if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("Adding method metadata", "KlassMetadata.java", 232);
    }

    /**
     * Get the debug information for a method body.
     *
     * @param isStatic  true if the method is static
     * @param index     the index for a method body
     * @return the debug information for the method
     */
    final MethodMetadata getMethodMetadata(boolean isStatic, int index) {
        MethodMetadata[] methods = geMethodMetadata(isStatic);
        if (methods == null) {
            return null;
        }
        
        Object[] methodArray = isStatic ? definedClass.getStaticMethods()  : definedClass.getVirtualMethods();
        if (methodArray.length == methods.length) {
            // MethodMetadata table was NOT stripped of nulls.
             return methods[index];
        } else {
            // MethodMetadata table was stripped of nulls. need to search for MethodMetadata
            int realIndex = MethodMetadata.binarySearch(methods, index);
            if (realIndex >= 0) {
                 return methods[realIndex];
            } else {
                return null;
            }
        }
    }

    /**
     * Get the class to which this metadata pertains.
     *
     * @return the class to which this metadata pertains
     */
    final Klass getDefinedClass() {
        return definedClass;
    }

    /**
     * Get the source file from which the class was compiled.
     *
     * @return the file name
     */
    String getSourceFileName() {
        return null;
    }

    
    /**
     * Get the static or instance method metadatas
     *
     * @return array of MethodMetadata or null
     */
    MethodMetadata[] geMethodMetadata(boolean isStatic) {
        return null;
    }
    
    /**
     * Get a parser for the symbolic information for the class.
     *
     * @return a parser for the symbolic information for the fields and
     *         methods of the class
     */
    SymbolParser getSymbolParser() {
        return SymbolParser.create(symbols, classTable);
    }

    /**
     * Get a parser for the symbolic information of a member of the class.
     *
     * @param   memberID  the index of the member to parse
     * @return  a parser for the member indicated by <code>memberID</code>
     */
    SymbolParser getSymbolParser(int memberID) {
        return SymbolParser.create(symbols, classTable, memberID);
    }

    int getSize() {
        return symbols.length;
    }

    /**
     * Flush the cached section parser objects.
     */
    static void flush() {
        SymbolParser.flush();
    }

    static class KlassMetadataComparer implements Comparer {

        static int getValue(Object o) {
            int value = -1;
            if (o != null) {
                if (o instanceof KlassMetadata) {
                    value = ((KlassMetadata) o).getDefinedClass().getSuiteID();
                } else if (o instanceof Klass) {
                    value = ((Klass) o).getSuiteID();
                } else {
                    throw new RuntimeException("what type are we comparing: " + o);
                }
            } else {
                throw new RuntimeException("why are we comparing against null???? ");
            }
            return value;
        }

        public int compare(Object o1, Object o2) {
            return getValue(o1) - getValue(o2);
        }
    }

    static final Comparer comparer = new KlassMetadataComparer();


//    public String toString() {
//        return "KlassMetadata for " + getDefinedClass().getName();
//    }


    /*---------------------------------------------------------------------------*\
     *                                Stripping                                  *
    \*---------------------------------------------------------------------------*/

    /**
     * factory for stripping.
     *
     * @param symbols   the symbols (stripped or otherwise) (may be null)
     * @param classTable class table (may be null)
     * @param original  the original symbolic information before stripping
     * @param type       the stripping policy
     * @return new full or stripped KlassMetadata
     */
    public static KlassMetadata create(byte[] symbols, Klass[] classTable, KlassMetadata original, int type) {
        String srcFile = (type == Suite.DEBUG) ? original.getSourceFileName() : null;
        MethodMetadata[] staticMethodsMetadata = MethodMetadata.strip(original.geMethodMetadata(true));
        MethodMetadata[] virtualMethodsMetadata = MethodMetadata.strip(original.geMethodMetadata(false));
        if (srcFile != null || staticMethodsMetadata != null || virtualMethodsMetadata != null) {
            return new KlassMetadata.Full(original.definedClass, symbols, classTable, srcFile,
                    virtualMethodsMetadata, staticMethodsMetadata);
        } else {
            return new KlassMetadata(original.definedClass, symbols, classTable);
        }
    }

    /**
     * Prunes the symbolic information for a given suite.
     *
     * @param suite      the enclosing suite
     * @param metadatas  the symbolic information for the classes in <code>suite</code>
     * @param type       the stripping policy
     * @return the stripped symbolic information which may be null
     */
    static KlassMetadata[] strip(Suite suite, KlassMetadata[] metadatas, int type) {
        if (metadatas == null) {
            return null;
        }

        if (type == Suite.APPLICATION && !MethodMetadata.lineNumberTablesKept()) {
            boolean anyRuntimeStaticsRequired = false;
            for (int i = 0; i != metadatas.length; ++i) {
                Klass klass = metadatas[i].getDefinedClass();
                if ((klass.getModifiers() & Modifier.COMPLETE_RUNTIME_STATICS) != 0) {
                    anyRuntimeStaticsRequired = true;
                    if (Klass.TRACING_ENABLED && Tracer.isTracing("stripping")) {
                        Tracer.traceln("COMPLETE_RUNTIME_STATICS required for " + klass + " in " + suite);
                    }
                    break;
                }
            }
            
            if (!anyRuntimeStaticsRequired) {
                if (Klass.TRACING_ENABLED && Tracer.isTracing("stripping")) {
                    Tracer.traceln("Discarded all metadata for " + suite);
                }
                return null;
            }
        }

        KlassMetadata[] newMetadatas = new KlassMetadata[metadatas.length];
        for (int i = 0; i != metadatas.length; ++i) {
            KlassMetadata metadata = metadatas[i];
            if (false) Assert.that(metadata != null);
            switch (type) {
                case Suite.DEBUG:
                    newMetadatas[i] = create(metadata.symbols, metadata.classTable, metadata, type);
                    break;
                case Suite.LIBRARY:
                case Suite.EXTENDABLE_LIBRARY:
                case Suite.APPLICATION: // COMPLETE_RUNTIME_STATICS case
                    newMetadatas[i] = metadata.strip(type);
                    break;
                default:
                    VM.fatalVMError();
            }
        }
        return newMetadatas;
    }

    public static boolean isExternaltoSuite(Klass klass, int stripType) {
        return (stripType != Suite.APPLICATION) && VM.isExported(klass);
    }
    
    /**
     * Prunes the symbols based on a given suite type.
     *
     * @param type  specifies a closed suite type. Must be {@link Suite#LIBRARY} or {@link Suite#EXTENDABLE_LIBRARY}.
     */
    private KlassMetadata strip(int type) {
        if (Klass.TRACING_ENABLED && Tracer.isTracing("stripping")) {
            Tracer.traceln("Processing metadata for " + definedClass);
        }
        boolean internalClass = !isExternaltoSuite(definedClass, type);
        if (internalClass && ((definedClass.getModifiers() & Modifier.COMPLETE_RUNTIME_STATICS) == 0) && !MethodMetadata.lineNumberTablesKept()) {
            if (Klass.TRACING_ENABLED && Tracer.isTracing("stripping")) {
                Tracer.traceln("  discarded all metadata");
            }
            return null;
        }

        SquawkVector types = new SquawkVector(classTable.length);
        byte[] newSymbols = getSymbolParser().strip(definedClass, type, types);
        Klass[] newClassTable = new Klass[types.size()];
        types.copyInto(newClassTable);
        return create(newSymbols, newClassTable, this, type);
    }

    /*---------------------------------------------------------------------------*\
     *                                 Debug                                     *
    \*---------------------------------------------------------------------------*/


//    static class Debug {
//        static int creates;
//        static int createMisses;
//        static int parses;
//        static int parseMisses;
//        static int selects;
//        static int selectMisses;
//
//        static void printStats(PrintStream out) {
//            out.println("-- SymbolParser stats --");
//            out.println();
//            out.println("Creates "+creates);
//            if (creates != 0) {
//                out.println("Misses  " + createMisses + "(" + ( (createMisses * 100) / creates) + "%)");
//            }
//            out.println();
//            out.println("Parses  "+parses);
//            if (parses != 0) {
//                out.println("Misses  " + parseMisses + "(" + ( (parseMisses * 100) / parses) + "%)");
//            }
//            out.println();
//            out.println("Selects "+selects);
//            if (selects != 0) {
//                out.println("Misses  " + selectMisses + "(" + ( (selectMisses * 100) / selects) + "%)");
//            }
//        }
//
//        static void dump(byte[] symbols) {
//            PrintStream out = System.out;
//            out.println("symbols:");
//            for (int i = 0; i != symbols.length; ++i) {
//                int b = symbols[i] & 0xFF;
//                String s = "    "+i;
//                while (s.length() < 15) {
//                    s += " ";
//                }
//                s += ""+b;
//                if (b > ' ' && b < '~') {
//                    while (s.length() < 25) {
//                        s += " ";
//                    }
//                    s += ""+(char)b;
//                }
//                System.out.println(s);
//            }
//        }
//         
//        /** do not instantiate */
//        private Debug() {}
//    }


}
