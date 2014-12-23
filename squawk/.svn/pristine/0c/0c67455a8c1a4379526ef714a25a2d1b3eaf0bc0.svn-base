/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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
import java.util.*;
import com.sun.squawk.vm.*;
import com.sun.squawk.util.*;
import com.sun.squawk.translator.Translator;

public class VM {

    public static boolean bigEndian;
    static boolean verbose;
    static boolean veryverbose;
    static PrintStream out = System.err;
    static Isolate currentIsolate;
    static Translator translator;

    /**
     * The default classpath contains directories that are listed by executing 'ls -d *\j2meclasses'
     * in the current working directory.
     */
    public static final String DEFAULT_CLASSPATH;

    static {
        final StringBuffer cp = new StringBuffer();
        new File(".").listFiles(new FileFilter(){
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    File j2meclassesDir = new File(file, "j2meclasses");
                    if (j2meclassesDir.exists() && j2meclassesDir.isDirectory()) {
                        cp.append(j2meclassesDir.getPath()).append(File.pathSeparatorChar);
                    }
                }
                return false;
            }
        });
        DEFAULT_CLASSPATH = cp.toString();
    }

    /**
     * Determines if the endianess of the object memory in a given file is big or little.
     *
     * @param omf  the file to test
     * @return true if the object memory in <code>omf</code> is big, false otherwise
     */
    public static boolean isBigEndian(File omf) {
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(omf));
            boolean result = ObjectMemoryLoader.loadHeader(dis, "file://" + omf.getPath()).isBigEndian();
            dis.close();
            return result;
        } catch (IOException e) {
            throw new LinkageError("error opening or reading from " + omf + ": " + e);
        }
    }

    /**
     * Creates and initializes the translator.
     *
     * @param classPath   the class search path
     */
    static void initializeTranslator(String classPath) {
        Suite suite = new Suite("-open-", null, Suite.EXTENDABLE_LIBRARY);
        Isolate isolate = new Isolate(null, null, suite);
        VM.setCurrentIsolate(isolate);

        isolate.setTranslator(new Translator());
        VM.translator = (Translator)isolate.getTranslator();
        VM.translator.open(suite, classPath);

//Tracer.enableFeature("loading");

        /*
         * Trigger the class initializer for java.lang.Klass. An error will have
         * occurred if it was triggered before this point.
         */
        Klass.TOP.getClass();
    }

    /**
     * The system-dependent path-separator character. This character is used to
     * separate filenames in a sequence of files given as a <em>path list</em>.
     * On UNIX systems, this character is <code>':'</code>; on Windows
     * systems it is <code>';'</code>.
     *
     * @return  the system-dependent path-separator character
     */
    public static char getPathSeparatorChar() {
        return File.pathSeparatorChar;
    }

    /**
     * The system-dependent default name-separator character.  This field is
     * initialized to contain the first character of the value of the system
     * property <code>file.separator</code>.  On UNIX systems the value of this
     * field is <code>'/'</code>; on Microsoft Windows systems it is <code>'\'</code>.
     *
     * @see     java.lang.System#getProperty(java.lang.String)
     */
    public static char getFileSeparatorChar() {
        return File.separatorChar;
    }

    private static int nextIsolateID;
    static int allocateIsolateID() {
        return nextIsolateID++;
    }

    /**
     * Get the isolate of the currently executing thread.
     *
     * @return the isolate
     */
    public static Isolate getCurrentIsolate() {
        return currentIsolate;
    }

    static void registerIsolate(Isolate isolate) {
    }
    
    /**
     * Set the isolate of the currently executing thread.
     *
     * @param isolate the isolate
     */
    static void setCurrentIsolate(Isolate isolate) {
        currentIsolate = isolate;
    }

    /**
     * Gets the flag indicating if the VM is running in verbose mode.
     *
     * @return true if the VM is running in verbose mode
     */
    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVerbose(boolean verbose) {
        VM.verbose = verbose;
    }

    /**
     * Gets the flag indicating if the VM is running in verbose mode.
     *
     * @return true if the VM is running in verbose mode
     */
    public static boolean isVeryVerbose() {
        return veryverbose;
    }

    public static void setVeryVerbose(boolean veryverbose) {
        VM.veryverbose = veryverbose;
    }

    /**
     * Get the endianess.
     *
     * @return true if the system is big endian
     */
    public static boolean isBigEndian() {
        return bigEndian;
    }

    /**
     * Determines if the Squawk system is being run in a hosted environment
     * such as the romizer or mapper application.
     *
     * @return true if the Squawk system is being run in a hosted environment
     */
    public static boolean isHosted() {
        return true;
    }

    /**
     * Determines if the VM was built with memory access type checking enabled.
     *
     * @return true
     */
    public static boolean usingTypeMap() {
        return true;
    }

    /**
     * Gets the Klass object corresponding to the address of a class in the image.
     *
     * @param klassOop  the address of a class in the image
     * @return the Klass instance corresponding to <code>klassOop</code>
     */
    static Klass asKlass(Object klassOop) {
        Klass klass = classCache.get(klassOop);
        if (klass == null) {
            Object nameOop = NativeUnsafe.getObject(klassOop, (int)FieldOffsets.com_sun_squawk_Klass$name);
            String name = asString(nameOop);
            klass = Klass.getClass(name, false);
            translator.load(klass);
            classCache.put(klassOop, klass);
        }
        return klass;
    }

    private static HashMap<Object, Klass> classCache = new HashMap<Object, Klass>();

    /**
     * Gets the String instance corresponding to a String in the image.
     *
     * @param stringOop    the address of a String in the image
     * @return the String instance corresponding to <code>stringOop</code>
     */
    static String asString(Object stringOop) {
        Assert.that(!((Address)stringOop).isZero());
        int length = GC.getArrayLengthNoCheck(stringOop);

        // get the class ID of the string
        Object something = NativeUnsafe.getObject(stringOop, HDR.klass);
        Object stringOopKlass =  NativeUnsafe.getObject(something, (int)FieldOffsets.com_sun_squawk_Klass$self);
        int classID = NativeUnsafe.getShort(stringOopKlass, (int)FieldOffsets.com_sun_squawk_Klass$id);
        Assert.that(classID == CID.STRING || classID == CID.STRING_OF_BYTES);


        // assume it is an 8-bit string
        StringBuffer buf = new StringBuffer(length);
        for (int i = 0 ; i < length; i++) {
            char ch;
            if (classID == CID.STRING) {
                ch = (char) NativeUnsafe.getChar(stringOop, i);
            } else {
                ch = (char)(NativeUnsafe.getByte(stringOop, i) & 0xFF);
            }
            buf.append(ch);
        }

        return buf.toString();
    }

    /**
     * Assert a condition.
     *
     * @param b a boolean value that must be true.
     */
    public static void assume(boolean b) {
        Assert.that(b);
    }

    /**
     * Stop fatally.
     */
    public static void fatalVMError() {
         Assert.shouldNotReachHere("fatalVMError");
    }

    /**
     * Print an error message.
     *
     * @param msg the message
     */
    public static void println(String msg) {
        System.err.println(msg);
    }

    public static void print(String s) {
        System.err.print(s);
    }

    public static void print(int i) {
        System.err.print(i);
    }

    public static void print(boolean b) {
        System.err.print(b);
    }
    
    public static void print(char ch) {
        System.err.print(ch);
    }

    public static void println() {
        System.err.println();
    }

    public static void printAddress(Object val) {
        System.err.print(val);
    }

    public static void printAddress(Address val) {
        printUWord(val.toUWord());
    }

    public static void printUWord(UWord val) {
        System.err.print(val);
    }

    public static void printOffset(Offset val) {
        System.err.print(val);
    }

    /**
     * VM-private version of System.arraycopy for arrays of primitives that does little error checking.
     */
    public static void arraycopyPrimitive0(Object src, int src_position, Object dst, int dst_position, 
                                           int totalLength, int dataSize) {
        System.arraycopy(src, src_position, dst, dst_position, totalLength);
    }
    
    /**
     * VM-private version of System.arraycopy for arrays of objects that does little error checking.
     */
    public static void arraycopyObject0(Object src, int src_position, Object dst, int dst_position, 
                                           int length) {
        System.arraycopy(src, src_position, dst, dst_position, length);
    }
    
}
