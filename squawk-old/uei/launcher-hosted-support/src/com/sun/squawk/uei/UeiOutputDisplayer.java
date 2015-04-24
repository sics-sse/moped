/*
 * Copyright 2008-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.uei;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;

public class UeiOutputDisplayer extends PrintStream {
    
    public UeiOutputDisplayer(OutputStream out) {
        super(out);
    }
    
    protected static String join(String delimiter, String... parts) {
        StringBuilder buf = new StringBuilder();
        
        boolean needDelimeter = false;
        for (String part : parts) {
            if (!needDelimeter) {
                needDelimeter = true;
            } else {
                buf.append(delimiter);
            }
            
            buf.append(part);
        }
        
        return buf.toString();
    }
    
    /**
     * Prints a help message for the specified UEI 1.0.2 commands.
     */
    public void printUsage(String problemWith, DeviceDescriptor device) {
        if (problemWith != null) {
            print(
                    "\n" +
                    "Problem with: " + problemWith);
        }
        //Column 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
        print(
                "\n" +
                "Usage: emulator [-options] [-classpath <path>] [MIDlet class name]\n" +
                "\n" +
                "If no MIDlet and/or classpath is specified, the emulator will try to find them\n" +
                "in the jad-file specified by the -Xdescriptor option.\n" +
                "\n" +
                "where options include:\n" +
                "    -help                   Display this help message\n" +
                "    -version                Display emulator version information\n" +
                "    -Xquery                 Print emulator and device information (optionally\n" +
                "                            specified by -Xdevice)\n" +
                "    -Xdevice:<device>       Set the device to be used by the emulator\n" +
                "    -classpath <path>       Set the classpath for the emulator\n");
        if (device == null || device.supportsDescriptorOption()) {
            print(
                "    -Xdescriptor:<jad-file> Set the Java Application Descriptor file to be used\n" +
                "                            by the emulator\n");
        }
        if (device == null || device.supportsHeapSizeOption()) {
            print(
                "    -Xheapsize:<size>[k|M]  Set the heap size in bytes, kilobytes or megabytes\n");
        }
        if (device == null || device.supportsSetPropertyOption()) {
            print(
                "    -D<property>=<value>    Set a system property\n");
        }
        if (device == null || device.supportsDebugOption()) {
            print(
                "    -Xdebug                 Enable runtime debugging (must be used with -Xrunjdwp)\n" +
                "    -Xrunjdwp:<jdwp-opts>   Set JDWP options (comma-separated), as follows:\n" +
                "      transport=<mechanism> Set the transport mechanism (default: dt_socket)\n" +
                "      address=[host:]<port> Set the transport address (in the port-only format,\n" +
                "                            the host is localhost)\n" +
                "      server=<y|n>          Start debug agent as a server (default: n)\n" +
                "      suspend=<y|n>         Suspend VM on debugger connection (default: y)\n");
        }
        if (device == null || device.supportsVerboseOption()) {
            print(
                "    -Xverbose               Identical to -Xverbose:all\n" +
                "    -Xverbose:<flags>       Run with specified diagnostic flags (comma-separated)\n" +
                "      all                   Run with all diagnostic options (!)\n" +
                "      allocation            Print every heap allocation and heap statistics\n" +
                "      gc                    Print every heap deallocation\n" +
                "      gcverbose             Print detailed garbage collection analysis (!)\n" +
                "      class                 Print class loading, creation and initialization\n" +
                "      classverbose          Print detailed class loading information (!)\n" +
                "      verifier              Print class verifier information\n" +
                "      stackmaps             Print class stackmap information\n" +
                "      bytecodes             Print every bytecode as it is executed (!)\n" +
                "      frames                Print stack frame information on push and pop\n" +
                "      stackchunks           Print stack and stack chunk creation\n" +
                "      exceptions            Print all thrown exceptions, even if caught\n" +
                "      events                Print all events (such as PENDOWN) as they are received\n" +
                "      threading             Print status of all threads\n" +
                "      monitors              Print information on monitor entry and exit (!)\n" +
                "      networking            Print detailed information on network-related calls (!)\n" +
                "      Options marked by (!) produce a lot of output and will slow down the emulator.\n");
        }
        if (device == null || device.supportsJamOption()) {
            print(
                "\n" +
                "Running in OTA Mode\n" +
                "Usage: emulator <command>\n" +
                "    -Xjam                   Run the interactive application manager\n" +
                "    -Xjam:install=<url>     Install application from <url>\n" +
                "    -Xjam:force             Overwrite previous installation when installing\n" +
                "    -Xjam:run=<app>         Run a previously installed application\n" +
                "    -Xjam:remove=<app>      Remove a previously installed application\n" +
                "    -Xjam:transient=<url>   Install, run and remove the application at <url>\n" +
                "    -Xjam:list              List all applications installed on the device\n" +
                "    -Xjam:storageNames      List all applications installed on the device\n");
        }
        if (device == null || device.supportsAutotestOption()) {
            print(
                "\n" +
                "Automated Testing\n" +
                "Usage: emulator -Xautotest:<url>\n" +
                "    -Xautotest:<url>        Repeatedly install and run MIDlet suites from <url>\n");
        }
        
        println();
    }
    
    public void printVersion(String emulatorNameVersion, String configuration, String profile, String... optionalAPIs) {
        println(emulatorNameVersion);
        println("Configuration: " + configuration);
        println("Profile: " + profile);
        
        if (optionalAPIs != null && optionalAPIs.length > 0) {
            println("Optional: " + join(",", optionalAPIs));
        }
    }
    
    /**
     * Prints the common response to the "-Xquery" command specified by UEI
     * 1.0.2.
     */
    public void printUEIQuery(String ueiVersion, String[] arguments, String[] devices, String[] securityDomains) {
        printQueryEntry("uei.version", ueiVersion, false);
        printQueryEntry("uei.arguments", arguments, false);
        printQueryEntry("device.list", devices);
        printQueryEntry("security.domains", securityDomains, false);
    }
    
    /**
     * Prints the device-specific response to the "-Xquery" command specified by
     * UEI 1.0.2.
     */
    public void printDeviceQuery(DeviceDescriptor device, EmulatorOptions options) {
        String classpath = "";
        StringBuilder stringBuilder = new StringBuilder(join(",", device.getBootstrapClasspath()));
        
        // Coerce usage of file separator '/' as per UEI spec
        int fileSeparatorIndex = stringBuilder.indexOf(File.separator);
        while (fileSeparatorIndex >= 0) {
            stringBuilder.replace(fileSeparatorIndex, fileSeparatorIndex + 1, "/");
            fileSeparatorIndex = stringBuilder.indexOf(File.separator, fileSeparatorIndex + 1);
        }
        
        classpath = stringBuilder.toString();

        String deviceName = device.getDeviceName();
        printQueryEntry(deviceName + ".screen.width", 0);
        printQueryEntry(deviceName + ".screen.height", 0);
        printQueryEntry(deviceName + ".screen.isColor", false);
        printQueryEntry(deviceName + ".screen.bitDepth", 0);
        printQueryEntry(deviceName + ".screen.isTouch", false);
        printQueryEntry(deviceName + ".uei.arguments", options.getSupportedOptions(), false);
        printQueryEntry(deviceName + ".bootclasspath", classpath);
        printQueryEntry(deviceName + ".apis", null, false);
        printQueryEntry(deviceName + ".version.configuration", device.getConfigurationName(), false);
        printQueryEntry(deviceName + ".version.profile", device.getProfileName(), false);
        printQueryEntry(deviceName + ".security.domains", null, false);
    }
    
    /**
     * Prints a property:value pair in the acceptable format for the "-Xquery"
     * command specified by UEI 1.0.2.
     */
    protected void printQueryEntry(String property, Object value) {
        printQueryEntry(property, value, true);
    }
    
    /**
     * Prints a property:value pair in the acceptable format for the "-Xquery"
     * command specified by UEI 1.0.2.
     * 
     * @param always If false, the pair will not print if value is
     *            <code>null</code>.
     */
    protected void printQueryEntry(String property, Object value, boolean always) {
        String s;
        if (value != null) {
            s = value.toString();
        } else if (always) {
            s = "";
        } else {
            return;
        }
        
        printf("%s: %s\n", property, s);
    }
    
    /**
     * Converts the array of strings into a single comma-delimited string, and
     * prints a property:value pair in the acceptable format for the "-Xquery"
     * command specified by UEI 1.0.2.
     */
    protected void printQueryEntry(String property, String[] values) {
        printQueryEntry(property, values, true);
    }
    
    /**
     * Converts the array of strings into a single comma-delimited string, and
     * prints a property:value pair in the acceptable format for the "-Xquery"
     * command specified by UEI 1.0.2.
     * 
     * @param always If false, the pair will not print if values is either
     *            <code>null</code> or empty.
     */
    protected void printQueryEntry(String property, String[] values, boolean always) {
        String value = null;
        if (values != null && values.length > 0) {
            value = join(",", values);
        }
        
        printQueryEntry(property, value, always);
    }
    
}
