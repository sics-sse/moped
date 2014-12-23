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

package com.sun.squawk.vm;

import java.io.*;

/**
 * This class is the entry point when launching the Squawk VM from Java.
 */
public class Main {

    static {
        System.loadLibrary("squawk");
    }

    /**
     * Converts a command line argument into ASCII bytes and appends it to given
     * byte stream followed by the terminating 0 byte required by C strings.
     *
     * @param baos  the byte stream used to marshall the arguments for a call to a C 'main'
     * @param arg   a Java command line argument
     */
    void marshallArg(ByteArrayOutputStream baos, String arg) throws IOException {
        // convert arg to C string
        baos.write(arg.getBytes());

        // add 0 delimiter to C string
        baos.write(0);
    }

    /**
     * The entry point for the native Squawk VM.
     *
     * @param args   a byte array containing all the command line arguments formatted as
     *               0-delimited ASCII C strings
     * @param argc   the number of command line arguments contained in 'args'
     */
    static native int squawk(byte[] args, int argc);

    /**
     *
     * @param args String[]
     */
    int run(String[] args) {
        int exitCode = -1;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int i = 0; i != args.length; ++i) {
                marshallArg(baos, args[i]);
            }
            baos.close();
            byte[] argv = baos.toByteArray();
            exitCode = squawk(argv, args.length);
        } catch (IOException ioe) {
            throw new RuntimeException("should not reach here: " + ioe);
        }
        return exitCode;
    }

    /**
     * Command line entry point.
     *
     * @param args   command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.out.println("Exited with code = " + new Main().run(args));
    }

}
