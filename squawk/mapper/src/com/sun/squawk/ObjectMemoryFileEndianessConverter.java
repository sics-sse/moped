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
import com.sun.squawk.vm.*;

/**
 * Converts the endianess of an object memory file to a specified endianess.
 *
 */
public class ObjectMemoryFileEndianessConverter {


    public static void main(String[] args) {

        File in = null;
        File out = null;
        Boolean outputBigEndian = null;
        String bootstrapSuiteURL = "file://squawk.suite";

        // parse args
        int argc = 0;
        while (argc != args.length) {
            String arg = args[argc];

            if (arg.charAt(0) != '-') {
                break;
            } else if (arg.startsWith("-boot:")) {
                bootstrapSuiteURL = arg.substring("-boot:".length());
            } else if (arg.equals("-big")) {
                outputBigEndian = Boolean.TRUE;
            } else if (arg.equals("-little")) {
                outputBigEndian = Boolean.FALSE;
            } else if (arg.startsWith("-trace")) {
                if (arg.startsWith("-tracefilter:")) {
                    String optArg = arg.substring("-tracefilter:".length());
                    if (Klass.TRACING_ENABLED) {
                        Tracer.setFilter(optArg);
                    } else {
                        System.err.println("warning: '" + arg + "' option is disabled in current build");
                    }
                } else {
                    if (Klass.TRACING_ENABLED) {
                        Tracer.enableFeature(arg.substring("-trace".length()));
                        if (arg.equals("-traceconverting")) {
                            Tracer.enableFeature("loading");
                        }
                    } else {
                        System.err.println("warning: '" + arg + "' option is disabled in current build");
                    }
                }
            } else if(arg.startsWith("-h")) {
                usage("");
                return;
            } else {
                usage("Unknown option: " + arg);
                return;
            }
            argc++;
        }

        System.setProperty("bootstrap.suite.url", bootstrapSuiteURL);

        if (outputBigEndian == null) {
            usage("missing -big or -little flag");
            return;
        }

        if (argc >= args.length) {
            usage("missing input file");
            return;
        }
        in = new File(args[argc++]);

        // Set "host" endianess from input object memory file
        VM.bigEndian = VM.isBigEndian(in);

        if (argc >= args.length) {
            usage("missing output file");
            return;
        }
        out = new File(args[argc]);

        // why did this ever need the translator?
        // VM.initializeTranslator(VM.DEFAULT_CLASSPATH);

        try {

            DataInputStream dis = new DataInputStream(new FileInputStream(in));
            ObjectMemoryFile omf = ObjectMemoryLoader.load(dis, "file://" + in.getPath(), false);
            dis.close();

            if (VM.isBigEndian() != outputBigEndian.booleanValue()) {
                System.out.println("Swapping endianess.");
                convertObjectMemory(omf.objectMemory, out, outputBigEndian.booleanValue());
            } else {
                System.out.println("Input object memory is already in desired endian format.");
                if (!in.equals(out)) {
                    System.out.println("Copying object memory to: " + out);
                    cp(in, out);
                }
            }

        } catch (IOException e) {
            System.err.println("Error converting object memory: " + e);
            e.printStackTrace();
            return;
        }
        System.out.println("Done!");
    }

    private static void usage(String errMsg) {
        PrintStream out = System.out;

        if (errMsg != null) {
            out.println(errMsg);
        }

        out.println("Converts the endianess of a given object memory file");
        out.println("Usage: ObjectMemoryFileEndianessSwapper [-options] input output");
        out.println("where options include:");
        out.println();
        out.println("    -big     convert object memory file to big endian");
        out.println("    -little  convert object memory file to little endian");
        out.println("    -boot:<url>      URL for bootstrap suite (default=file://squawk.suite)");
    }

    /**
     * Swap endianness of given object memory file.
     *
     * @param inputURI   URI to the object memory file to be modified
     * @param outputURI  URI of the output object memory file
     */
    private static void convertObjectMemory(ObjectMemory om, File out, boolean bigEndian) throws IOException {

        // save memory in correct format
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(out));

        ObjectMemorySerializer.ControlBlock cb = ObjectMemorySerializer.ControlBlock.createControlBlock();
        cb.root = Address.fromObject(om.getRoot()).diff(om.getStart()).toInt();

        int size = om.getSize();
        Address startAddress = om.getParent() == null ? Address.zero() : om.getParent().getCanonicalEnd();
        cb.memory = new byte[size];
        cb.start = startAddress;
        int start = startAddress.toUWord().toInt();
        NativeUnsafe.copyMemory(cb.memory, start, 0, size);
        cb.oopMap = new BitSet();
        cb.oopMap.or(NativeUnsafe.getOopMap(), -start / HDR.BYTES_PER_WORD);
        ObjectMemorySerializer.save(dos, "file://" + out.getPath(), cb, om.getParent(), bigEndian);
    }

    /**
     * Copies a file.
     *
     * @param from source file
     * @param to   destination file
     */
    public static void cp(File from, File to) throws IOException {
        if (from.equals(to)) {
            return;
        }
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(from)));
        OutputStream os = new BufferedOutputStream(new FileOutputStream(to));

        byte[] content = new byte[(int)from.length()];
        dis.readFully(content);
        os.write(content);
        dis.close();
        os.close();
    }

    private ObjectMemoryFileEndianessConverter() {
    }
}
