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

package com.sun.squawk.traces;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import com.sun.squawk.util.Assert;
import com.sun.squawk.util.LongHashtable;
import com.sun.squawk.util.IntHashtable;

/**
 * A <code>Symbols</code> instance represents the symbolic information associated with
 * a Squawk memory or image.
 */
class Symbols {

    /**
     * The exception thrown when a method lookup based on an address fails.
     */
    public static class UnknownMethodException extends RuntimeException {

        /**
         * The address for which no information exists.
         */
        public final long address;

        /**
         * Creates an exception denoting that there is no method information for a given address.
         *
         * @param address  the address for which no information exists
         */
        public UnknownMethodException(long address) {
            super("no method at " + address);
            this.address = address;
        }
    }

    /**
     * The exception thrown when a native method lookup based on a native method identifier fails.
     */
    public static class UnknownNativeMethodException extends RuntimeException {

        public final int identifier;

        /**
         * Creates an exception denoting that there is no method information for a given native method identifier.
         *
         * @param identifier  the identifier for which no information exists
         */
        public UnknownNativeMethodException(int identifier) {
            super("unknown native method identifier " + identifier);
            this.identifier = identifier;
        }
    }

    /**
     * A <code>Method</code> instance encapsulates the symbolic information for a method.
     */
    public static class Method {

        /**
         * The pattern used to extract the return type, fully qualified name and parameters from a method signature.
         */
        private static final Pattern SIGNATURE = Pattern.compile("([^ ]+) *([^(]+)\\(([^)]*)\\)");

        /**
         * The signature of the method.
         */
        private String signature;

        /**
         * The fully qualified path (relative to some source path) to the file in which the method was defined.
         */
        private String filePath;

        /**
         * The bytecode offsets corresponding to the start of a source code line.
         */
        private int[] startPCs;

        /**
         * The source code line numbers corresponding to the bytecode offsets in <code>startPCs</code>.
         */
        private int[] lineNumbers;

        /**
         * Sets the signature of this method.
         *
         * @param signature   the method's signature which must not be null
         * @return the old value of this method's signature
         */
        public String setSignature(String signature) {
            Assert.that(signature != null, "'signature' must not be null");
            String old = this.signature;
            this.signature = signature;
            return old;
        }

        /**
         * Sets the file path of this method. This can only be done once.
         *
         * @param filePath   the method's file path which must not be null
         * @return the old value of this method's file path
         */
        public String setFilePath(String filePath) {
            Assert.that(filePath != null, "'filePath' must not be null");
            String old = this.filePath;
            this.filePath = filePath;
            return old;
        }

        /**
         * Sets the line number table of this method. This can only be done once.
         *
         * @param table   the entry from the symbols file describing the line number table
         */
        public void setLineNumberTable(String table) {
            Assert.that(table != null, "'table' must not be null");
            StringTokenizer st = new StringTokenizer(table);
            int count = st.countTokens();
            Assert.that((count % 2) == 0, "format error in line number table: odd number of tokens");
            count /= 2;
            startPCs = new int[count];
            lineNumbers = new int[count];
            for (int i = 0; i != count; i++) {
                startPCs[i] = Integer.parseInt(st.nextToken());
                lineNumbers[i] = Integer.parseInt(st.nextToken());
            }
        }

        /**
         * Gets a string representation of this method.
         *
         * @return a string representation of this method
         */
//        public String toString() {
//            StringBuffer buf = new StringBuffer("method: signature=" + signature + ", file=" + filePath + ", lineNumberTable={ ");
//            if (startPCs != null) {
//                for (int i = 0; i != startPCs.length; ++i) {
//                    buf.append(startPCs[i]).append("->").append(lineNumbers[i]).append(' ');
//                }
//            }
//            buf.append('}');
//            return buf.toString();
//        }

        /**
         * Gets the signature of this method. This can only be called after the signature has been {@link #setSignature(String) set}.
         *
         * @return the signature of this method
         */
        public String getSignature() {
            Assert.that(signature != null, "signature is not yet set");
            return signature;
        }

        /**
         * Gets the name of this method.
         *
         * @param fullyQualified  if true, the fully qualified method name is returned otherwise the base name is returned
         * @return  the name of this method
         */
        public String getName(boolean fullyQualified) {
            Matcher m = SIGNATURE.matcher(signature);
            boolean matches = m.matches();
            Assert.that(matches, "method signature malformated: " + signature);
            String fullyQualifiedName = m.group(2);
            if (fullyQualified) {
                return fullyQualifiedName;
            }
            return fullyQualifiedName.substring(fullyQualifiedName.lastIndexOf('.') + 1);
        }

        /**
         * Gets the file path of this method. This can only be called after the file path has been {@link #setFilePath(String) set}.
         *
         * @return the fully qualified path (relative to some source path) to the file in which the method was defined
         */
        public String getFilePath() {
            Assert.that(filePath != null, "file is not yet set");
            return filePath;
        }

        /**
         * Gets the last name in the pathname returned by {@link #getFilePath()}.  If the pathname's name sequence
         * is empty, then the empty string is returned.
         *
         * @return  the last name in the pathname returned by {@link #getFilePath()}
         */
        public String getFile() {
            File file = new File(filePath);
            return file.getName();
        }

        /**
         * Gets the source line number for a given instruction address. This can only be called after the
         * line number table has been {@link #setLineNumberTable(String) set}.
         *
         * @param pc     an instruction address
         * @return the source code line number for <code>pc</code> or -1 if it is unavailable
         */
        public int getSourceLineNumber(int pc) {
            Assert.that(startPCs != null, "line number table is not yet set");
            int lineNumber = lineNumbers.length == 0 ? -1 : lineNumbers[0];
            for (int i = 0; i != startPCs.length; ++i) {
                int startPC = startPCs[i];
                if (pc < startPC) {
                    break;
                }
                lineNumber = lineNumbers[i];
            }
            return lineNumber;
        }

        /**
         * Gets a string representation of this method formatted as a partial standard stack trace line.
         *
         * @param pc   the bytecode offset for which the corresponding source line will
         *             be included in the returned string
         * @return the method as a partial stack trace line
         */
        public String toString(int pc) {
            return getName(true) + "(" + getFile() + ":" + getSourceLineNumber(pc) + ")";
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return toString(0);
        }
    }

    /**
     * Constructor.
     */
    public Symbols() {
        symbols = new LongHashtable();
        nativeMethods = new IntHashtable();
    }

    /**
     * Table of addresses to entries encapsulating the symbolic information for the objects at the addresses.
     */
    private LongHashtable symbols;

    /**
     * Table of native method identifiers to the names of the methods.
     */
    private IntHashtable nativeMethods;

    /**
     * Attempts to load some symbols from a file.
     *
     * @param file  the file to load if it exists
     */
    public void loadIfFileExists(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            load(fis);
        } catch (IOException ex) {
            System.err.println("warning: error while loading symbols from " + file + ": " + ex);
//            ex.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {
                    Assert.shouldNotReachHere(""+ioe.getMessage());
                }
            }
        }
    }

    /**
     * Loads symbolic information for a squawk memory from an input stream that is in the format
     * understood by {@link Properties#load(InputStream)}. Each call to this method augments
     * any previously loaded symbolic information.
     *
     * @param in    the input stream to load from
     * @throws IOException if there was an error while parsing the stream
     */
    public void load(InputStream in) throws IOException {
        new Loader(symbols, nativeMethods).load(in);
    }

    /**
     * Relocates any canonical addresses in the symbols table. Once complete, an execution profile or
     * trace should be able to do a direct lookup in this Symbols object based on a runtime address.
     *
     * @param relocator  the object used to relocate any canonical addresses
     */
    public void relocate(AddressRelocator relocator, boolean preserve) {
        LongHashtable relocatedSymbols = new LongHashtable(symbols.size());
        Enumeration keys = symbols.keys();
        Enumeration values = symbols.elements();
        while (keys.hasMoreElements()) {
            long address = ((Long)keys.nextElement()).longValue();
            Object symbol = values.nextElement();
            long relocAddress = relocator.relocate(address);
            relocatedSymbols.put(relocAddress, symbol);
            if (preserve) {
                relocatedSymbols.put(address, symbol);
            }
        }
        symbols = relocatedSymbols;
    }

    /**
     * Dumps the symbols to a print stream.
     *
     * @param out  where to print the symbols
     */
    public void dump(PrintStream out) {
        Enumeration keys = symbols.keys();
        Enumeration values = symbols.elements();
        while (keys.hasMoreElements()) {
            out.println(keys.nextElement() + " -> " + values.nextElement());
        }
    }

    /**
     * Gets the symbolic information for a method at a given address.
     *
     * @param address   the address of a method
     * @return the symbolic information for the method at <code>address</code> or null if there is no
     *         symbolic information for <code>address</code>
     * @throws UnknownMethodException if there is no recorded symbolic information for a method at <code>address</code>
     */
    public Method lookupMethod(long address) {
        Object entry = symbols.get(address);
        if (entry == null) {
            throw new UnknownMethodException(address);
        }
        return (Method)entry;
    }

    /**
     * Gets the name of the native method corresponding to a given native method identifier.
     *
     * @param identifier  the identifier to look up
     * @return the name corresponding to <code>identifier</code>
     * @throws UnknownNativeMethodException if there is no recorded symbolic information for a method at <code>address</code>
     */
    public String lookupNativeMethod(int identifier) {
        Object entry = nativeMethods.get(identifier);
        if (entry == null) {
            throw new UnknownNativeMethodException(identifier);
        }
        return (String)entry;
    }

    /**
     * This class handles parsing of a Squawk symbols file to reconstruct the symbolic information it represents.
     */
    static final class Loader extends Properties {

        /**
         * A regular expression for matching:
         *
         *   "METHOD." <address> ".NAME"
         *
         */
        private static final Matcher METHOD_NAME_KEY = Pattern.compile("^METHOD\\.(\\d+)\\.NAME$").matcher("");

        /**
         * A regular expression for matching:
         *
         *   "METHOD." <address> ".FILE"
         *
         */
        private static final Matcher METHOD_FILE_KEY = Pattern.compile("^METHOD\\.(\\d+)\\.FILE$").matcher("");

        /**
         * A regular expression for matching:
         *
         *   "METHOD." <address> ".LINETABLE"
         *
         */
        private static final Matcher METHOD_LINETABLE_KEY = Pattern.compile("^METHOD\\.(\\d+)\\.LINETABLE$").matcher("");

        /**
         * A regular expression for matching:
         *
         *   "METHOD." <address> ".MOVED_FROM"
         *
         */
        private static final Matcher METHOD_MOVED_FROM_KEY = Pattern.compile("^METHOD\\.(\\d+)\\.MOVED_FROM$").matcher("");

        /**
         * A regular expression for matching:
         *
         *   "SAVED_METHOD." <address> ".COPIED_FROM"
         *
         */
        private static final Matcher SAVED_METHOD_COPIED_FROM_KEY = Pattern.compile("^SAVED_METHOD\\.(\\d+)\\.COPIED_FROM$").matcher("");

        /**
         * A regular expression for matching:
         *
         *   "NATIVE." <identifier> ".NAME"
         *
         */
        private static final Matcher NATIVE_METHOD_NAME = Pattern.compile("^NATIVE\\.(\\d+)\\.NAME$").matcher("");

        /**
         * The table into which the parsed entries should be added.
         */
        final LongHashtable entries;

        /**
         * The table of native method identifiers to method names.
         */
        final IntHashtable nativeMethods;

        /**
         * Map from canonicalised method addresses to the addresses of the original method addresses.
         * This is used to map methods addresses in a saved isolate or suite back to their
         * symbolic information once they are re-loaded and relocated.
         */
        final LongHashtable savedMethods = new LongHashtable();

        /**
         * Specifies if debug level console logging output should be generated.
         */
        final boolean debug = System.getProperty("symbols.loader.debug", "false").equals("true");

        /**
         * Creates a loader for parsing a file in the format expected by {@link Properties#load(InputStream)}
         * and extracting any symbolic information for one or more methods.
         *
         * @param entries the table into which the extracted information should be added
         */
        Loader(LongHashtable entries, IntHashtable nativeMethods) {
            this.entries = entries;
            this.nativeMethods = nativeMethods;
        }

        /**
         * Gets the Method object for a given address from the entries table. If there is
         * no entry for <code>address</code> and <code>create</code> is true, then an entry
         * is created, added to the table and returned.
         *
         * @param address  the address to lookup
         * @param create   specifies if an entry should be ccreated if there is no entry for <code>address</code>
         * @return the object containing the symbolic information for the method at <code>address</code>
         *                 or null if there is no entry for this address and <code>create</code> is
         *                 false
         */
        private Method method(long address, boolean create) {
            Method method = (Method)entries.get(address);
            if (method == null && create) {
                method = new Method();
                entries.put(address, method);
            }
            return method;
        }

        /**
         * Preprocessed the addition of an entry whose key matches the regular expression
         * "METHOD\.*\.MOVED_FROM" to replace it with an entry that denotes the name of
         * the method at its new address.
         *
         * @param k  the key
         * @param v  the value
         * @return null
         */
        public Object put(Object k, Object v) {
            String key = (String)k;
            String value = (String)v;

            String old;
            if (METHOD_NAME_KEY.reset(key).matches()) {
                long address = Long.parseLong(METHOD_NAME_KEY.group(1));
                if ((old = method(address, true).setSignature(value)) != null) {
                    System.err.println("warning: overrode signature at " + address + ":");
                    System.err.println("  old: " + old);
                    System.err.println("  new: " + value);
                }
            } else if (METHOD_FILE_KEY.reset(key).matches()) {
                long address = Long.parseLong(METHOD_FILE_KEY.group(1));
                if ((old = method(address, true).setFilePath(value)) != null) {
                    System.err.println("warning: overrode file path at " + address + ":");
                    System.err.println("  old: " + old);
                    System.err.println("  new: " + value);
                }
            } else if (METHOD_LINETABLE_KEY.reset(key).matches()) {
                long address = Long.parseLong(METHOD_LINETABLE_KEY.group(1));
                method(address, true).setLineNumberTable(value);
            } else if (METHOD_MOVED_FROM_KEY.reset(key).matches()) {
                long from = Long.parseLong(value);
                long to = Long.parseLong(METHOD_MOVED_FROM_KEY.group(1));
                Method method = method(from, false);
                if (method != null) {
                    entries.put(to, method);
                } else {
                    System.err.println("warning: no method found at " + from);
                }
            } else if (SAVED_METHOD_COPIED_FROM_KEY.reset(key).matches()) {
                long from = Long.parseLong(value);
                long to = Long.parseLong(SAVED_METHOD_COPIED_FROM_KEY.group(1));
                Method method = method(from, false);
                if (method != null) {
                    savedMethods.put(to, method);
                    entries.put(to, method);
                } else {
                    System.err.println("warning: no method found at " + from);
                }
            } else if (NATIVE_METHOD_NAME.reset(key).matches()) {
                int identifier = Integer.parseInt(NATIVE_METHOD_NAME.group(1));
                nativeMethods.put(identifier, value);
            } else if (key.equals("UNHIBERNATED_ISOLATE.RELOCATION")) {
                long relocation = Long.parseLong(value);
                Enumeration keys = savedMethods.keys();
                Enumeration values = savedMethods.elements();
                while (keys.hasMoreElements()) {
                    long address = ((Long)keys.nextElement()).longValue();
                    Method method = (Method)values.nextElement();
                    entries.put(address + relocation, method);
                }
                savedMethods.clear();
            } else {
                if (debug) {
                    System.err.println("debug: ignored symbols entry with key " + key);
                }
            }
            return null;
        }
    }
}
