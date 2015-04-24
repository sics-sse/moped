/*
 * Copyright 2007-2008 Sun Microsystems, Inc. All Rights Reserved.
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
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.sun.squawk.Romizer;
import com.sun.squawk.debugger.sdp.SDP;
import com.sun.squawk.uei.utilities.OutputDisplayer;

/**
 * UEI device emulator imlementation for Squawk.
 */
public class SquawkDeviceDescriptor extends DeviceDescriptor {
    protected static final File VANILLA_SQUAWK_DIRECTORY = new File(EMULATOR_DIRECTORY, "squawk");
    protected static final File CHILD_BUILD_DIRECTORY = new File(EMULATOR_DIRECTORY, "temp");
    protected static final String CHILD_SUITE_NAME = "test";
    
    public static SquawkDeviceDescriptor getCldc10() {
        return new SquawkDeviceDescriptor("Squawk-CLDC-1.0", CONFIGURATION_CLDC10, null, "cldc10.jar");
    }
    
    public static SquawkDeviceDescriptor getCldc11() {
        return new SquawkDeviceDescriptor("Squawk-CLDC-1.1", CONFIGURATION_CLDC11, null, "cldc11.jar");
    }
    
    public static SquawkDeviceDescriptor getImp10() {
        return new SquawkDeviceDescriptor("Squawk-IMP-1.0", CONFIGURATION_CLDC11, PROFILE_IMP10, "cldc11.jar", "imp10.jar");
    }
    
    public SquawkDeviceDescriptor(String name, String configuration, String profile, String... bootstrapClasspath) {
        super(name, configuration, profile, bootstrapClasspath);
    }
    
    public void startMIDlet(EmulatorOptions options) {
        List<String> squawkArgs = new ArrayList<String>();
        
        squawkArgs.add(new File(VANILLA_SQUAWK_DIRECTORY, "squawk").getPath());
        if (!options.xdebug) {
            squawkArgs.add("-suite:" + new File(CHILD_BUILD_DIRECTORY, CHILD_SUITE_NAME).getPath());
            squawkArgs.add("-testMIDlet:" + options.midletName);
        } else {
            squawkArgs.add("-suite:" + new File(CHILD_BUILD_DIRECTORY, CHILD_SUITE_NAME).getPath());
            squawkArgs.add("com.sun.squawk.debugger.sda.SDA");
            squawkArgs.add("com.sun.squawk.imp.MIDletMainWrapper");
            squawkArgs.add("-name");
            squawkArgs.add(options.midletName);
        }
        
        buildSuite(options);
        
        if (options.xdebug) {
            startSDProxy(options);
        }
        
        System.err.println("******** Starting user code *************");
        
        try {
            // Allow printing to sync (for NetBeans)
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Do nothing
        }
        
        try {
            Process process = new ProcessBuilder(squawkArgs).directory(VANILLA_SQUAWK_DIRECTORY).start();
            new OutputDisplayer(process).waitForProcessEnd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void buildSuite(EmulatorOptions options) {
        System.err.println("Building suite...");
        
        String[] args = {
                "-nobuildproperties",
                "-boot:" + new File(VANILLA_SQUAWK_DIRECTORY, "squawk").getPath(),
                "-metadata",
                "-strip:d",
                "-lnt",
                "-cp:" + options.classpath,
                "-o:" + new File(CHILD_BUILD_DIRECTORY, CHILD_SUITE_NAME).getPath(),
                options.classpath };
        
        PrintStream stdout = System.out;
        try {
            // Capture romize output to stderr
            System.setOut(System.err);
            
            Romizer.main(args);
        } catch (IOException e) {
            throw new RuntimeException("Romize failed.", e);
        } finally {
            System.setOut(stdout);
        }
        
        System.err.println();
    }
    
    protected void startSDProxy(EmulatorOptions options) {
        final String[] lib_jars = { "cldc11", "imp10", "debugger" };
        
        String j2meClasspath = options.classpath;
        for (String jarName : lib_jars) {
            j2meClasspath = new File(LIB_DIRECTORY, jarName + ".jar").getPath() + File.pathSeparator + j2meClasspath;
        }
        
        if (options.xrunjdwp_address.startsWith("localhost")) {
            options.xrunjdwp_address = options.xrunjdwp_address.substring("localhost".length());
        }
        String debugURL = (options.xrunjdwp_server ? "server" : "") + "socket://" + options.xrunjdwp_address;
        
        final String[] args = { "-cp:" + j2meClasspath, "-debugger:" + debugURL };
        
        new Thread("Proxy") {
            public void run() {
                try {
                    SDP.main(args);
                } catch (IOException e) {
                    throw new RuntimeException("Error starting debugger-proxy.", e);
                }
                // Build.main(args);
                // new Build(null).java(j2seClasspath, false, null, "com.sun.squawk.debugger.sdp.SDP", args);
            }
        }.start();
        
    }
    
}
