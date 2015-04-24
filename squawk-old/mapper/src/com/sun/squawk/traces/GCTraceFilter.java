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

import java.io.*;
import java.util.regex.*;

/**
 * Filters a Squawk GC to show method names when tracing activation frames.
 */
public class GCTraceFilter {

    /**
     * The name of the output file.
     */
    private String outFile;

    /**
     * The name of the input file.
     */
    private String inFile;



    /**
     * The map used to do the filtering.
     */
    private Symbols symbols = new Symbols();

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
        out.println("Usage: GCTraceFilter [options] input_file [output_file]");
        out.println("where options include:");
        out.println();
        out.println("    -s file       load additional symbols from file");
        out.println("    -r spec       relocate canonical symbols according to spec which has the format:");
        out.println("                     *ROM*:<romStart>:<romEnd>:*NVM*:<nvmStart>:<nvmEnd>");
        out.println("                  e.g.:");
        out.println("                     *ROM*:10000000:2000000:*NVM*:2000000:3000000");
        out.println();
        out.println("Note: symbols are loaded from all *.sym files in the current directory");
    }

    /**
     * Constructs a filter from some command line arguments.
     *
     * @param args   the command line arguments
     * @throws IOException
     */
    private GCTraceFilter(String[] args) throws IOException {

        // Load all *.sym files first
        symbols.loadIfFileExists(new File("squawk.sym"));
        symbols.loadIfFileExists(new File("squawk_dynamic.sym"));

        // Load additional symbol files
        int argc = 0;
        String relocation = null;
        while (argc != args.length) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            } else if (arg.startsWith("-s:")) {
                File file = new File(arg.substring("-s:".length()));
                symbols.loadIfFileExists(file);
            } else if (arg.startsWith("-r:")) {
                relocation = arg.substring("-r:".length());
            } else {
                usage("Unknown option: " + arg);
                System.exit(1);
            }
            argc++;
        }

        if (argc == args.length) {
            usage("missing input file");
            System.exit(1);
        }
        inFile = args[argc++];

        if (argc == args.length) {
            outFile = inFile;
        } else {
            outFile = args[argc++];
        }

        relocateCanonicalSymbols(symbols, relocation);
    }

    /**
     * Relocates the any canonical symbols base on a relocation specification.
     *
     * @param symbols    a set of loaded symbols, some of which may be canonical
     * @param relocation a relocation specification. If null, then the first line of the file "trace" is used
     */
    private static void relocateCanonicalSymbols(Symbols symbols, String relocation) {
        if (relocation == null) {
            File trace = new File("trace");
            if (trace.exists()) {
                InputFile in = new InputFile(new File("trace"));
                relocation = in.readLine();
            }
        }

        if (relocation == null) {
            System.err.println("*** 'trace' not found - no relocation performed on symbols ***");
            return;
        }

        Matcher m = Pattern.compile(".*ROM\\*:(\\d+):(\\d+):\\*NVM\\*:(\\d+):(\\d+).*").matcher(relocation);
        if (!m.matches()) {
            throw new TraceParseException(relocation, m.pattern().pattern());
        }

        long romStart = Long.parseLong(m.group(1));
        long romEnd   = Long.parseLong(m.group(2));
        long nvmStart = Long.parseLong(m.group(3));
        long nvmEnd   = Long.parseLong(m.group(4));

        AddressRelocator relocator = new AddressRelocator(romStart, (int)(romEnd - romStart), nvmStart, (int)(nvmEnd - nvmStart));
        symbols.relocate(relocator, true);
    }

    /**
     * Runs the filter over the input.
     *
     * @throws IOException
     */
    private void run() throws IOException {
        InputStream in = new FileInputStream(inFile);
        if (in.available() == 0) {
            System.err.println("Nothing available on standard input stream");
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(in.available());
        PrintStream out = new PrintStream(baos);

        Pattern romMethod = Pattern.compile("mp = [0-9]+ \\(image @ ([0-9]*)\\)");
        Pattern ramMethod = Pattern.compile("mp = ([0-9]+)");

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line = br.readLine();
        while (line != null) {

            int index = line.indexOf("mp = ");
            if (index != -1) {

                Matcher matcher = romMethod.matcher(line);
                String address = null;
                int start = 0;
                int end = 0;

                if (matcher.find()) {
                    address = matcher.group(1);
                    start = matcher.start(1);
                    end = matcher.end(1);
                } else {
                    matcher = ramMethod.matcher(line);
                    if (matcher.find()) {
                        address = matcher.group(1);
                        start = matcher.start(1);
                        end = matcher.end(1);
                    }
                }

                if (address != null) {
                    try {
                        Symbols.Method method = symbols.lookupMethod(Long.parseLong(address));
                        if (method != null) {
                            line = line.substring(0, start) + address + " [" + method.getName(true) + "]" + line.substring(end);
                        }
                    } catch (Symbols.UnknownMethodException e) {
                        line = line.substring(0, start) + address + " [ <unknown> ]" + line.substring(end);
                        System.err.println("unknown method at " + address);
                    }
                }
            }
            out.println(line);
            line = br.readLine();
        }

        out.close();
        in.close();

        new FileOutputStream(outFile).write(baos.toByteArray());
    }

    /**
     * Command line interface.
     *
     * @param args   command line arguments
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new GCTraceFilter(args).run();
    }
}
