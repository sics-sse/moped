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

package com.sun.squawk.util;

import java.io.*;

/**
 * This is a utility similiar to the Unix 'xxd' utility.
 */
public class HexDump {

    private int columns = 16;
    private boolean binary;
    private int radix = 16;
    private final InputStream in;
    private final PrintStream out;

    public HexDump(String[] args) throws IOException {
        int argc = 0;
        while (argc != args.length) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            } else if (arg.equals("-c")) {
                columns = Integer.parseInt(getOptArg(args, ++argc, "-c"));
            } else if (arg.equals("-radix")) {
                radix = Integer.parseInt(getOptArg(args, ++argc, "-radix"));
            } else if (arg.equals("-b") || arg.equals("-bits")) {
                binary = true;
            } else if (arg.equals("-h")) {
                usage(null);
                System.exit(0);
            } else {
                usage("Unknown option: " + arg);
                System.exit(1);
            }
            argc++;
        }

        if (argc < args.length) {
            in = new BufferedInputStream(new FileInputStream(args[argc++]));
            if (argc < args.length) {
                out = new PrintStream(new BufferedOutputStream(new FileOutputStream(args[argc])));
            } else {
                out = System.out;
            }
        } else {
            in = new BufferedInputStream(System.in);
            out = System.out;
        }
    }

    /**
     * Gets the argument to a command line option. If the argument is not
     * provided, then a usage message is printed and RuntimeException is
     * thrown.
     *
     * @param  args   the command line arguments
     * @param  index  the index at which the option's argument is located
     * @param  opt    the name of the option
     * @return the value of the option's argument
     * @throws RuntimeException if the required argument is missing
     */
    private String getOptArg(String[] args, int index, String opt) {
        if (index >= args.length) {
            usage("The " + opt + " option requires an argument.");
            throw new RuntimeException();
        }
        return args[index];
    }

    /**
     * Prints the usage message.
     *
     * @param  errMsg  an optional error message
     */
    private void usage(String errMsg) {
        PrintStream out = System.out;
        if (errMsg != null) {
            out.println(errMsg);
        }

        Integer.toString(4, 4);
        out.println("Usage: HexDump [-options] [infile [outfile]]");
        out.println("where options include:");
        out.println();
        out.println("    -c cols        format <cols> octets per line (default=16)");
        out.println("    -b             binary digit dump");
        out.println("    -radix n       format offsets with radix <n> (default=16)");
        out.println("    -h             print this help message and exit");
    }

    private void run() throws IOException {
        byte[] line = new byte[columns];
        char[] hexTable = "0123456789abcdef".toCharArray();
        int offset = 0;

        int count = in.read(line);
        while (count != -1) {
            String sOffset = Integer.toString(offset, radix);
            int pad = 7 - sOffset.length();
            if (pad > 0) {
                out.print("0000000".substring(0, pad));
            }

            out.print(sOffset + ':');
            for (int i = 0; i != count; ++i) {
                if ((i % 2) == 0) {
                    out.print(' ');
                }
                int value = line[i];
                out.print(hexTable[(value >> 4) & 0xF]);
                out.print(hexTable[(value     ) & 0xF]);
            }
            out.print(' ');
            for (int i = 0; i != count; ++i) {
                int value = line[i];
                if (value > 0 && value < 128 && !Character.isISOControl((char)value)) {
                    out.print((char)value);
                } else {
                    out.print('.');
                }
            }
            out.println();

            offset += count;
            count = in.read(line);
        }

        in.close();
        out.close();
    }

    public static void main(String[] args) throws IOException {
        new HexDump(args).run();
    }
}
