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

import java.util.*;
import java.io.*;

import com.sun.squawk.vm.*;
import com.sun.squawk.translator.ir.InstructionEmitter;

/**
 * This class is used to create/update the C header file required by the C implementation
 * of the Squawk interpreter. The header file provides definitions for the symbols
 * in the bootstrap suite that are required by the interpreter.
 *
 */
public final class CHeaderFileCreator {

    /**
     * Properties from "squawk.sym".
     */
    private final Properties map;

    /**
     *
     * @param bootstrapSuite Suite
     * @param file File
     * @param properties Properties
     * @return  true if the file was overwritten or created, false if not
     * @throws IOException
     */
    public static boolean update(Suite bootstrapSuite, File file, Properties properties) throws IOException {
        CHeaderFileCreator creator = new CHeaderFileCreator(properties);

        CharArrayWriter caw = new CharArrayWriter(file.exists() ? (int)file.length() : 0);
        PrintWriter out = new PrintWriter(caw);
        creator.writeHeader(bootstrapSuite, out);

        char[] content = caw.toCharArray();
        char[] oldContent = null;

        if (file.exists()) {
            FileReader fr = new FileReader(file);
            int length = (int)file.length();
            int n = 0;
            oldContent = new char[length];
            while (n < length) {
                int count = fr.read(oldContent, n, length - n);
                if (count < 0) {
                    throw new EOFException();
                }
                n += count;
            }
            fr.close();
        }

        if (!Arrays.equals(content, oldContent)) {
            file.delete();
            file.getParentFile().mkdirs();
            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.close();
            file.setReadOnly();
            return true;
        } else {
            return false;
        }

    }

    private CHeaderFileCreator(Properties properties) {
        this.map = properties;
    }

    /**
     * Gets the class corresponding to a given name.
     *
     * @param suite      the suite in which to start the lookup
     * @param name       the name of the class
     * @param fieldSpec  the field specification from which the class name was derived
     * @return the Klass instance corresponding to <code>name</code>
     */
    private Klass lookupClass(Suite suite, String name, String fieldSpec) {
        Klass klass = suite.lookup(name);
        if (klass == null) {
            throw new RuntimeException("Can't find the class '" + name + "' specified in '" + fieldSpec + "'");
        }
        return klass;
    }

    /**
     * Verify that the offset definitions in com.sun.squawk.vm.FieldOffsets are correct.
     */
    private void verifyFieldOffsets() {
        Suite suite = VM.getCurrentIsolate().getBootstrapSuite();
        Klass k = suite.lookup("com.sun.squawk.vm.FieldOffsets");
        int count = k.getFieldCount(true);
        for (int i = 0; i != count; ++i) {
            Field field = k.getField(i, true);
            if (field.isPrivate()) {
                continue;
            }
            String name = field.getName();
            long constantValue = field.getPrimitiveConstantValue();
            int offset = FieldOffsets.decodeOffset(constantValue);
            int typeID = FieldOffsets.decodeSystemID(constantValue);

            int indexOf$ = name.indexOf('$');
            if (indexOf$ == -1) {
                throw new RuntimeException("Constant defined in com.sun.squawk.vm.FieldOffsets does not include '$': " + name);
            }

            String className = name.substring(0, indexOf$).replace('_', '.');
            String fieldName = name.substring(indexOf$ + 1);

            Klass klass = lookupClass(suite, className, field.toString());
            boolean found = verifyFieldOffset(field.toString(), offset, typeID, fieldName, klass, false) ||
                            verifyFieldOffset(field.toString(), offset, typeID, fieldName, klass, true);
            if (!found) {
                throw new RuntimeException("Missing definition of '" + className + "." + fieldName + "'");
            }
        }

    }

    /**
     * Searches for a field and confirms that its offset and type are as expected.
     *
     * @param fieldSpec     the string form of the specification from which the field info to be verified was derived
     * @param fieldOffset   the expected offset of the field
     * @param fieldTypeID   the expected system ID of the field's type
     * @param fieldName     the name of the field
     * @param klass         the class in which the field is defined
     * @param isStatic      specifies if the instance or static fields of <code>klass</code> should be searched
     * @return  false if the field was not found, true if it was and verifies correctly
     * @throws RuntimeException if the field was found but did not verify
     */
    private boolean verifyFieldOffset(String fieldSpec, int fieldOffset, int fieldTypeID, String fieldName, Klass klass, boolean isStatic) {
        int fieldCount = klass.getFieldCount(isStatic);
        boolean found = false;
        for (int j = 0; j != fieldCount; ++j) {
            Field squawkField = klass.getField(j, isStatic);
            if (squawkField.getName().equals(fieldName)) {
                int offset = squawkField.getOffset();
                Klass type = squawkField.getType();
                int  systemID = type.getSystemID();
                switch (systemID) {
                    default:
                    	systemID = CID.OBJECT;
                    	break;
                    case CID.BOOLEAN:
                    	systemID = CID.BYTE;
                    	break;
                    case CID.BYTE:
                    case CID.CHAR:
                    case CID.SHORT:
                    case CID.INT:
                    case CID.LONG:
                    case CID.FLOAT:
                    case CID.DOUBLE:
                    	break;
                }
                if (offset != fieldOffset) {
                    throw new RuntimeException("The value of '" + fieldSpec + "' should be " + offset + " not " + fieldOffset);
                }
                if (fieldTypeID != systemID) {
                    throw new RuntimeException("The CID of '" + fieldSpec + "' should be " + fieldTypeID + " not " + systemID);
                }
                found = true;
                break;
            }
        }
        return found;
    }

    /**
     * Verify that the offset definitions in com.sun.squawk.vm.MethodOffsets are correct.
     */
    private void verifyMethodOffsets() {
        boolean errors = false;
        Suite suite = VM.getCurrentIsolate().getBootstrapSuite();
        Klass MethodOffsetsKlass = suite.lookup("com.sun.squawk.vm.MethodOffsets");
        int count = MethodOffsetsKlass.getFieldCount(true);
        for (int i = 0; i != count; ++i) {
            Field field = MethodOffsetsKlass.getField(i, true);
            String name = field.getName();
            int value = (int)field.getPrimitiveConstantValue();
            boolean isStatic = !name.startsWith("virtual$");
            if (!isStatic) {
                name = name.substring("virtual$".length());
            }

            int indexOf$ = name.indexOf('$');
            if (indexOf$ == -1) {
                System.err.println("Constant defined in com.sun.squawk.vm.MethodOffsets does not include '$': " + name);
                errors = true;
            } else {

                String className = name.substring(0, indexOf$).replace('_', '.');
                String nameAndParameters = name.substring(indexOf$ + 1);

                String methodName;
                Klass[] parameters;

                // get the parameter types (if any)
                indexOf$ = nameAndParameters.indexOf('$');
                if (indexOf$ != -1) {
                    // fix up the method name
                    methodName = nameAndParameters.substring(0, indexOf$);

                    // get the parameters
                    StringTokenizer st = new StringTokenizer(nameAndParameters.substring(indexOf$ + 1), "$");
                    parameters = new Klass[st.countTokens()];
                    for (int j = 0; j != parameters.length; ++j) {
                        String typeName = st.nextToken().replace('_', '.');
                        parameters[j] = lookupClass(suite, typeName, field.toString());
                    }
                } else {
                    methodName = nameAndParameters;
                    parameters = null;
                }


                Klass klass = lookupClass(suite, className, field.toString());
                int methodCount = klass.getMethodCount(isStatic);
                boolean found = false;
    nextMethod:
                for (int j = 0; j != methodCount; ++j) {
                    Method squawkMethod = klass.getMethod(j, isStatic);
                    if (squawkMethod.getName().equals(methodName)) {

                        if (parameters != null) {
                            Klass[] types = squawkMethod.getParameterTypes();
                            if (types.length != parameters.length) {
                                continue nextMethod;
                            }
                            for (int k = 0; k != types.length; ++k) {
                                if (types[k] != parameters[k]) {
                                    continue nextMethod;
                                }
                            }
                        }

                        int offset = squawkMethod.getOffset();
                        if (offset != value) {
                            System.err.println("The value of '" + field + "' should be " + offset + " not " + value);
                            errors = true;
                        }
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    System.err.println("Missing definition of '" + className + "." + nameAndParameters + "'");
                    errors = true;
                }
            }
        }
        if (errors) {
            System.exit(-1);
        }
    }

    /**
     * Outputs a C function definition that looks up the name of a global word or reference based
     * on a given index.
     *
     * @param out
     * @param globals
     * @param functionName
     */
    private void outputGlobalNames(PrintWriter out, Hashtable<String, Integer> globals, String functionName) {
        out.println("const char* " + functionName + "(int index) {");
        out.println("    switch(index) {");
        for (Map.Entry<String, Integer> entry : globals.entrySet()) {
			String name = entry.getKey();
			int offset = entry.getValue();
            name = name.replace('.', '_');
            out.println("        case " + offset + ": return \"" + name + "\";");
		}
        out.println("        default: return \"" + functionName + ": unknown global index\";");
        out.println("    }");
        out.println("}");
    }

    private final static String[] romizedPackagesInclude = {
        "com.sun.squawk.",
        "java.lang."
    };

    private final static String[] romizedPackagesExclude = {
        "com.sun.squawk.io.j2me."
    };
    
    /**
     * Decide if this class is one of the romized classes (included in rom.h).
     * @param classname the internal name of the class
     * @return true if the class must be romized
     */
    public static boolean isRomizedClass(String classname) {
        boolean isRomized = false;
        for (String pkg : romizedPackagesInclude) {
            if (classname.startsWith(pkg)) {
                isRomized = true;
                break;
            }
        }

        if (isRomized) {
            for (String pkg : romizedPackagesExclude) {
                if (classname.startsWith(pkg)) {
                    isRomized = false;
                    break; // ignore this class
                }
            }
        }
        return isRomized;
    }

    static class InterpreterMethodInfo {
        Klass klass;
        Method meth;

        InterpreterMethodInfo(Klass klass, Method meth) {
            this.klass = klass;
            this.meth = meth;
        }

        String toCName() {
            return fix(klass.getName() + '_' + meth.getName());
        }
    }

    Vector<InterpreterMethodInfo> interpreterInvokedMethods = new Vector<InterpreterMethodInfo>();

    /**
     * Write C declarations for a klass
     */
    @SuppressWarnings("unchecked")
    private void writeKlassDecls(Klass klass, int cid, Suite suite, PrintWriter out) throws IOException {
        String internalName = klass.getInternalName();

        if (klass == null || klass.isArray() || internalName.charAt(0) == '-') {
            return; // ignore this class
        }

        if (klass.isSynthetic()) {
            return; // ignore this class
        }

        if (!isRomizedClass(internalName)) {
            return;// ignore this class
        }

        out.println("#define " + fix(klass.getName()) + " " + cid);

        // Write the instance field getters.
        int fieldCount = klass.getFieldCount(false);
        for (int fid = 0; fid != fieldCount; fid++) {
            Field field = klass.getField(fid, false);
            String wholeName = fix(klass.getName() + '_' + field.getName());
            out.print("#define " + wholeName + "(oop) ");
            switch (field.getType().getSystemID()) {
                case CID.BOOLEAN:
                case CID.BYTE:
                    out.print("getByte");
                    break;
                case CID.CHAR:
                    out.print("getUShort");
                    break;
                case CID.SHORT:
                    out.print("getShort");
                    break;
                case CID.FLOAT:
                case CID.INT:
                    out.print("getInt");
                    break;
                case CID.DOUBLE:
                case CID.LONG:
                    out.print(Klass.SQUAWK_64
                            ? "getLong"
                            : "getLongAtWord");
                    break;
                case CID.UWORD:
                case CID.OFFSET:
                    out.print("getUWord");
                    break;
                default:
                    out.print("getObject");
                    break;
            }
            out.println("((oop), " + field.getOffset() + ")");
        }

        // Write the instance field setters.
        for (int fid = 0; fid != fieldCount; fid++) {
            Field field = klass.getField(fid, false);
            String wholeName = fix(klass.getName() + '_' + field.getName());
            out.print("#define set_" + wholeName + "(oop, value) ");
            switch (field.getType().getSystemID()) {
                case CID.BOOLEAN:
                case CID.BYTE:
                    out.print("setByte");
                    break;
                case CID.CHAR:
                case CID.SHORT:
                    out.print("setShort");
                    break;
                case CID.FLOAT:
                case CID.INT:
                    out.print("setInt");
                    break;
                case CID.DOUBLE:
                case CID.LONG:
                    out.print(Klass.SQUAWK_64
                            ? "setLong"
                            : "setLongAtWord");
                    break;
                case CID.UWORD:
                case CID.OFFSET:
                    out.print("setUWord");
                    break;
                default:
                    out.print("setObject");
                    break;
            }
            out.println("((oop), " + field.getOffset() + ", value)");
        }

        // Write the constants.
        fieldCount = klass.getFieldCount(true);
        nextField:
        for (int fid = 0; fid != fieldCount; fid++) {
            Field field = klass.getField(fid, true);
            if (field.hasConstant()) {
                String value;
                switch (field.getType().getSystemID()) {
                    case CID.BOOLEAN:
                        value = "" + (field.getPrimitiveConstantValue() != 0);
                        break;
                    case CID.BYTE:
                        value = "" + (byte) field.getPrimitiveConstantValue();
                        break;
                    case CID.CHAR:
                        value = "" + (int) (char) field.getPrimitiveConstantValue();
                        break;
                    case CID.SHORT:
                        value = "" + (short) field.getPrimitiveConstantValue();
                        break;
                    case CID.INT:
                        value = "" + (int) field.getPrimitiveConstantValue();
                        break;
                    case CID.LONG:
                        value = "JLONG_CONSTANT(" + field.getPrimitiveConstantValue() + ")";
                        break;
                    case CID.DOUBLE:
                        value = "" + Double.longBitsToDouble(field.getPrimitiveConstantValue());
                        break;
                    case CID.FLOAT:
                        value = "" + Float.intBitsToFloat((int) field.getPrimitiveConstantValue());
                        break;
                    case CID.STRING:
                        continue nextField;
                    default:
                        throw new RuntimeException("need another case statement for constants of type " + field.getType().getName());
                }

                String name = fix(klass.getName() + '_' + field.getName());
                if (name.startsWith("com_sun_squawk_vm_")) {
                    name = name.substring("com_sun_squawk_vm_".length());
                }
                out.println("#define " + name + " " + value);
            }
        }

        // collect the InterpreterInvoked methods
        int methodCount = klass.getMethodCount(true);
        for (int mid = 0; mid != methodCount; mid++) {
            Method meth = klass.getMethod(mid, true);
            if (meth.isInterpreterInvoked()) {
                interpreterInvokedMethods.addElement(new InterpreterMethodInfo(klass, meth));
                //out.println("#define " + name + " " + value);
            }
        }
    }

    /**
     * Create the "rom.h" for the slow VM.
     */
	@SuppressWarnings("unchecked")
	private void writeHeader(Suite suite, PrintWriter out) throws IOException {

        out.println("/*");
        out.println(" * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.");
        out.println(" * Copyright 2010 Oracle. All Rights Reserved.");
        out.println(" * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER");
        out.println(" * ");
        out.println(" * This code is free software; you can redistribute it and/or modify");
        out.println(" * it under the terms of the GNU General Public License version 2");
        out.println(" * only, as published by the Free Software Foundation.");
        out.println(" * ");
        out.println(" * This code is distributed in the hope that it will be useful, but");
        out.println(" * WITHOUT ANY WARRANTY; without even the implied warranty of");
        out.println(" * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU");
        out.println(" * General Public License version 2 for more details (a copy is");
        out.println(" * included in the LICENSE file that accompanied this code).");
        out.println(" * ");
        out.println(" * You should have received a copy of the GNU General Public License");
        out.println(" * version 2 along with this work; if not, write to the Free Software");
        out.println(" * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA");
        out.println(" * 02110-1301 USA");
        out.println(" * ");
        out.println(" * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood");
        out.println(" * Shores, CA 94065 or visit www.oracle.com if you need additional");
        out.println(" * information or have any questions.");
        out.println("*/");

        // Write the CID definitions.
        int classCount = suite.getClassCount();
        for (int cid = 0; cid != classCount; cid++) {
            Klass klass = suite.getKlass(cid);
            if (klass != null) {
                writeKlassDecls(klass, cid, suite, out);
            }
        }

        // Verify that the hard coded field and method offsets are correct
        verifyFieldOffsets();
        verifyMethodOffsets();

        if (false) {
        // Write the InterpreterInvoked entrypoints.
        for (int i = 0 ;; i++) {
            String name = getStringProperty("ENTRYPOINT."+i+".NAME");
            if (name == null) {
                break;
            }
            int addr  = getIntProperty("ENTRYPOINT."+i+".ADDRESS");
            out.println("#define "+name+" Address_add(com_sun_squawk_VM_romStart, "+addr+")");
        }
        }

        // Write the string constant that is the mnemonics for the types
        out.println("const char *AddressType_Mnemonics = \"" + AddressType.Mnemonics + "\";");
        out.println("#if TRACE");

        // Write function that will translate a bytecode into its name.
        out.println("char *getOpcodeName(int code) {");
        out.println("    switch(code) {");
        try {
            for (int i = 0; ; i++) {
                out.println("        case " + i + ": return \"" + Mnemonics.getMnemonic(i) + "\";");
            }
        } catch (IndexOutOfBoundsException e) {
        }
        out.println("        default: return \"Unknown opcode\";");
        out.println("    }");
        out.println("}");

        // Write function equivalent to OPC.hasWide()
        out.println("boolean opcodeHasWide(int code) {");
        out.println("    switch(code) {");
        try {
            for (int i = 0; ; i++) {
                if (OPC.hasWide(i)) {
                    String mnemonic = Mnemonics.getMnemonic(i).toUpperCase();
                    out.println("        case OPC_" + mnemonic + ":");
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }
        out.println("                 return true;");
        out.println("        default: return false;");
        out.println("    }");
        out.println("}");

        Hashtable<String, Integer> globalAddrs = InstructionEmitter.getGlobalAddrVariables();
        Hashtable<String, Integer> globalInts = InstructionEmitter.getGlobalIntVariables();
        Hashtable<String, Integer> globalOops = InstructionEmitter.getGlobalOopVariables();

        // Write function that will translate a global word index into its name.
        outputGlobalNames(out, globalAddrs, "getGlobalAddrName");
        outputGlobalNames(out, globalOops,  "getGlobalOopName");
        outputGlobalNames(out, globalInts,  "getGlobalIntName");

        out.println("#endif");

        // Write the accessors for the global Address ints
        for (Map.Entry<String, Integer> entry : globalInts.entrySet()) {
            String name = entry.getKey();
            int offset = entry.getValue();;
            name = name.replace('.', '_');
            out.println("#define " + name + " (Ints[" + offset + "])");
		}

        // Write the accessors for the global Address words
        for (Map.Entry<String, Integer> entry : globalAddrs.entrySet()) {
            String name = entry.getKey();
            int offset = entry.getValue();;
            name = name.replace('.', '_');
            out.println("#define " + name + " (Addrs[" + offset + "])");
        }

        // Write the accessors for the global Oops.
        for (Map.Entry<String, Integer> entry : globalOops.entrySet()) {
            String name = entry.getKey();
            int offset = entry.getValue();;
            name = name.replace('.', '_');
            out.println("#define " + name + " (Oops[" + offset + "])");
        }

        // Write the endianess constant
        out.println("#define ROM_BIG_ENDIAN "          + getStringProperty("PMR.BIG_ENDIAN"));
        out.println("#define ROM_REVERSE_PARAMETERS "  + getStringProperty("PMR.REVERSE_PARAMETERS"));

        // Write the definition of the globals.
        out.println("#define ROM_GLOBAL_INT_COUNT  " + getIntProperty("ROM.GLOBAL.INT.COUNT"));
        out.println("#define ROM_GLOBAL_OOP_COUNT  " + getIntProperty("ROM.GLOBAL.OOP.COUNT"));
        out.println("#define ROM_GLOBAL_ADDR_COUNT " + getIntProperty("ROM.GLOBAL.ADDR.COUNT"));

        // Write the var decls for invoked methods.
        for (InterpreterMethodInfo minfo: interpreterInvokedMethods) {
            out.println("static Address " + minfo.toCName() + ";");
        }
        
        // Write the initializer for the interpreter invoked methods.
        out.println("\nstatic void initMethods() {");
        for (InterpreterMethodInfo minfo: interpreterInvokedMethods) {
            out.println("    " + minfo.toCName() + " = lookupStaticMethod(" + minfo.klass.getSuiteID() + ", " + minfo.meth.getOffset() + ");");
        }
        out.println("}");

        out.close();
    }

    /**
     * Fixup a sumbol.
     *
     * @param str the symbol name
     * @return the symbol with '.' and '$' turned into '_' and primitive types made upper case
     */
    private static String fix(String str) {
        str = str.replace('.', '_');
        str = str.replace('$', '_');
        if (str.indexOf('_') == -1) {
            str = str.toUpperCase(); // int, float, etc.
        }
        return str;
    }

    /**
     * Get a string property
     *
     * @param name the property name
     * @return the property value
     */
    private String getStringProperty(String name) {
        return map.getProperty(name);
    }

    /**
     * Get an int property
     *
     * @param name the property name
     * @return the property value
     */
    private int getIntProperty(String name) {
        try {
            return Integer.parseInt(getStringProperty(name));
        } catch(NumberFormatException ex) {
            throw new RuntimeException("in getIntProperty("+name+") = " + getStringProperty(name));
        }
    }

}
