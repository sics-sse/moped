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
package com.sun.squawk.suiteconverter;

import com.sun.squawk.ObjectMemoryLoader;
import com.sun.squawk.VM;
import java.io.*;

/**
 *
 * Main program around the suite converter in the Suite class.
 */
public class FlashConverter {
    private int requiredRelocationAddress;
    private String suiteFilePath;
    private String bootstrapSuitePath;
    private Boolean bigEndian;
    private String outFile;
    private int libraryAddress;
    private int bootstrapAddress;

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
        out.println("Usage: suiteconverter [-options] suite_file_address [[library_address] bootstrap_address]");
        out.println("where addresses are specified in hex and options include:");
        out.println();
        out.println("    -boot:<file>    file path for bootstrap suite (default=squawk.suite)");
        out.println("    -suitepath:<directories separated by '" + File.pathSeparatorChar + "'>");
        out.println("    -endian:<value> convert object memories to this endianess (default is endianess of object_memory_file)");
        out.println("    -verbose, -v    provide more output while running");
        out.println("    -h              show this help message");
    }

    /**
     * Address should be a hex number, but also support address based on file size
     * address = file://
     * 
     * @param address a hex number or file://file
     * @return
     */
    int parseAddress(String addressStr) {
        if (addressStr.startsWith("file://")) {
            int size;
            File f = new File(addressStr.substring("file://".length()));
            size = (int) f.length();
            return size;
        } else {
            try {
                return Integer.parseInt(addressStr, 16);
            } catch (NumberFormatException ex) {
                usage("illegal hexadecimal relocation address: " + addressStr);
                return -1;
            }
        }
    }

    /**
     * Parses the command line arguments to configure an execution of the mapper.
     *
     * @param args   the command line arguments
     * @return boolean true if there were no errors in the arguments
     */
    private boolean parseArgs(String[] args)
            throws IOException {
        bootstrapSuitePath = "squawk.suite";

        int argc = 0;
        for (; argc != args.length; ++argc) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            }
            if (arg.startsWith("-boot:")) {
                bootstrapSuitePath = arg.substring("-boot:".length());
            } else if (arg.startsWith("-suitepath:")) {
                String path = arg.substring("-suitepath:".length());
                ObjectMemoryLoader.setFilePath(path);
            } else if (arg.startsWith("-o:")) {
                outFile = arg.substring("-o:".length());
            } else if (arg.startsWith("-endian:")) {
                String value = arg.substring("-endian:".length());
                if (value.equals("big")) {
                    bigEndian = Boolean.TRUE;
                } else if (value.equals("little")) {
                    bigEndian = Boolean.FALSE;
                } else {
                    usage("invalid endianess: " + value);
                    return false;
                }
            } else if (arg.equals("-verbose") | arg.equals("-v")) {
                VM.setVerbose(true);
                VM.setVeryVerbose(true);
            } else if (arg.equals("-h")) {
                usage(null);
                return false;
            } else {
                usage("unknown option: " + arg);
                return false;
            }
        }

        if (args.length - argc != 2) {
            usage("missing suite file and/or relocation address");
            return false;
        }

        suiteFilePath = args[argc++];
        if (outFile == null) {
            outFile = suiteFilePath + ".bin";
        }

        try {
            requiredRelocationAddress = parseAddress(args[argc++]);
            if (args.length - argc == 2) { // app case
                libraryAddress = parseAddress(args[argc++]); // ConfigPage.LIBRARY_VIRTUAL_ADDRESS
            }
            if (args.length - argc == 1) { // handles both library and app cases
                bootstrapAddress = parseAddress(args[argc++]); // configPage.getBootstrapAddress()
            }
        } catch (NumberFormatException ex) {
            usage("illegal hexadecimal relocation address");
            return false;
        }

        // Set endianess
        if (bigEndian == null) {
            Suite.setIsTargetBigEndian(VM.isBigEndian(new File(suiteFilePath)));
        } else {
            Suite.setIsTargetBigEndian(bigEndian.booleanValue());
        }

        return true;
    }

    private int[] getMemoryAddrs() {
      if (libraryAddress != 0) {
          return new int[] {requiredRelocationAddress, libraryAddress, bootstrapAddress};
      } else if (bootstrapAddress != 0) {
          return new int[] {requiredRelocationAddress, bootstrapAddress};
      } else {
          return new int[] {requiredRelocationAddress};
      }
    }

    /**
     * Execute the mapper and produce the dump.
     */
    private void run() throws IOException {
        File binFilePath = new File(outFile);
        int[] memoryAddrs = getMemoryAddrs();
        if (VM.isVerbose()) {
            System.out.println("Relocating address in " + suiteFilePath + " to " + Integer.toHexString(requiredRelocationAddress) + " to new file " + binFilePath);
        }
		Suite suite = new Suite();
		suite.loadFromFile(suiteFilePath,
                new File(bootstrapSuitePath).getPath());
		suite.relocateMemory(memoryAddrs);
		File outputFile = new File(binFilePath.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(outputFile);
		suite.writeToStream(new DataOutputStream(fos));
		fos.close();
    }
    
    /*---------------------------------------------------------------------------*\
     *                                  main                                     *
    \*---------------------------------------------------------------------------*/

    public static void main(String[] args) throws IOException {
        FlashConverter converter = new FlashConverter();
        if (converter.parseArgs(args)) {
            converter.run();
        }
    }
}
