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

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A class that doescontaining <code>DeviceEmulator</code>s
 *
 */
public class UeiCommandProcessor {
    
    protected String emulatorName;
    protected String emulatorVersion;
    
    protected Map<String, DeviceDescriptor> deviceDescriptors;
    protected String defaultDeviceName;
    
    public UeiCommandProcessor() {
        this("Squawk", "1.0");
    }
    
    public UeiCommandProcessor(String emulatorName, String emulatorVersion) {
        this.emulatorName = emulatorName;
        this.emulatorVersion = emulatorVersion;
        deviceDescriptors = new HashMap<String, DeviceDescriptor>();
        // Put this back in when CLDC 1.0 works again
        // addDevice(SquawkDeviceDescriptor.getCldc10());
        addDevice(SquawkDeviceDescriptor.getCldc11());
        addDevice(SquawkDeviceDescriptor.getImp10());
    }

    public String getEmulatorName() {
        return emulatorName;
    }
    
    public String getEmulatorVersion() {
        return emulatorVersion;
    }
    
    /**
     * @return The concatenation of the emulator's name and version.
     */
    public String getEmulatorNameAndVersion() {
        return getEmulatorName() + " " + getEmulatorVersion();
    }
    
    /**
     * @return A <code>Map</code> of all <code>DeviceDescriptor</code>s registered
     *         with this launcher.
     */
    public Map<String, DeviceDescriptor> getDeviceEmulators() {
        return deviceDescriptors;
    }
    
    /**
     * @return The <code>DeviceDescriptor</code> with name <code>deviceName</code>
     *         registered with this launcher.
     */
    public DeviceDescriptor getDeviceDescriptor(String deviceName) {
        return deviceDescriptors.get(deviceName);
    }
    
    /**
     * @return An array of all device names registered with this launcher.
     */
    public String[] getDeviceNames() {
        String[] deviceNames = deviceDescriptors.keySet().toArray(new String[deviceDescriptors.size()]);
        Arrays.sort(
                deviceNames,
                new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return o1.compareToIgnoreCase(o2);
                    }
                }
        );
        return deviceNames;
    }
    
    /**
     * Adds the specified <code>DeviceDescriptor</code> to this launcher's list of
     * registered emulators. If an emulator with the specified name already
     * exists, nothing will be changed. Otherwise, if the default emulator is
     * not yet specified, the given <code>DeviceDescriptor</code> will become the
     * default.
     * 
     * @param device The <code>DeviceDescriptor</code> to add.
     * @return true if the list of emulators was changed, false otherwise.
     */
    public boolean addDevice(DeviceDescriptor device) {
        return addDevice(device, (defaultDeviceName == null));
    }
    
    /**
     * Adds the specified <code>DeviceDescriptor</code> to this launcher's list of
     * registered emulators. If an emulator with the specified name already
     * exists, nothing will be changed. Otherwise, if the
     * <code>makeDefault</code> is <code>true</code>, the given
     * <code>DeviceDescriptor</code> will become the default.
     * 
     * @param device The <code>DeviceDescriptor</code> to add.
     * @param makeDefault Whether or not to make this the default emulator.
     * @return true if the list of emulators was changed, false otherwise.
     */
    public boolean addDevice(DeviceDescriptor device, boolean makeDefault) {
        String deviceName = device.getDeviceName();
        
        if (deviceDescriptors.containsKey(deviceName)) {
            return false;
        }
        
        deviceDescriptors.put(deviceName, device);
        if (makeDefault) {
            defaultDeviceName = deviceName;
        }
        return true;
    }
    
    protected void runWithEmulator(UeiOutputDisplayer out, String[] args) throws EmulatorException {
        if (deviceDescriptors.size() == 0) {
            throw new EmulatorException("No registered device emulators.", false);
        }
        
        if (args.length == 0) {
            out.printUsage("No arguments", null);
            return;
        }

        String deviceName = null;
        for (String arg : args) {
            if (arg.startsWith("-Xdevice:")) {
                deviceName = arg.substring("-Xdevice:".length());
                break;
            }
        }
        
        DeviceDescriptor device;
        
        if (deviceName == null) {
            if (defaultDeviceName == null) {
                throw new EmulatorException("No device or default specified.", true);
            }
            device = getDeviceDescriptor(defaultDeviceName);
        } else {
            device = getDeviceDescriptor(deviceName);
        }
        
        if (device == null) {
            throw new EmulatorException("Specified device (" + deviceName + ") has not been registered with this launcher.", true);
        }
        
        EmulatorOptions options = new EmulatorOptions();
        options.processCommandLineArguments(args);
        
        if (options.help) {
            if (deviceName != null) {
                out.printUsage(null, device);
            } else {
                out.printUsage(null, null);
            }
        } else if (options.version) {
            out.printVersion(getEmulatorNameAndVersion(), device.getConfigurationName(), device.getProfileName(), device.getOptionalAPIs());
        } else if (options.xquery) {
            out.printUEIQuery("1.0.1", getSupportedOptions(options), getDeviceNames(), null);
            if (deviceName != null) {
                out.printDeviceQuery(device, options);
            } else {
                for (DeviceDescriptor each : deviceDescriptors.values()) {
                    out.printDeviceQuery(each, options);
                }
            }
        } else if (!options.unrecognizedOptions.isEmpty()) {
            throw new EmulatorException("Unrecognized option: " + options.unrecognizedOptions.get(0), true);
        } else {
            options.processPropertyValues();
            device.startMIDlet(options);
        }
    }
    
    /**
     * @return An array of the arguments common to all devices registered with
     *         this launcher.
     */
    public String[] getSupportedOptions(EmulatorOptions options) {
        SortedSet<String> arguments = new TreeSet<String>();
        arguments.addAll(Arrays.asList(options.getSupportedOptions()));
        arguments.add("Xdevice");
        return arguments.toArray(new String[arguments.size()]);
    }

    /**
     * Programmatic entry for the launcher, directs the standard out to
     * <code>System.out</code>.
     * 
     * @param args UEI-compliant command line arguments.
     * @return The emulator's exit code: 0 iff it exits normally.
     */
    public int run(String[] args) {
        return run(System.out, args);
    }
    
    /**
     * Programmatic entry for the launcher.
     * 
     * @param output The output stream to send stdout to.
     * @param args UEI-compliant command line arguments.
     * @return The exit status of the emulator: 0 if it completed normally; -1
     *         if it encountered an error in the given arguments; -2 if it
     *         encountered a feature yet to be implemented; and 1 if the
     *         emulated program threw an uncaught exception.
     */
    public int run(PrintStream output, String[] args) {
        UeiOutputDisplayer out = new UeiOutputDisplayer(output);
        
        try {
            runWithEmulator(out, args);
        } catch (EmulatorException e) {
            out.println(e.getMessage());
            if (e.showHelp()) {
                out.printUsage(null, null);
            }
            return -1;
        } catch (UnimplementedUeiFeatureException e) {
            out.println("This feature is not implemented!");
            e.printStackTrace();
            return -2;
        } catch (Throwable e) {
            e.printStackTrace();
            return 1;
        }
        
        return 0;
    }
    
    public static void main(String[] args) {
        UeiCommandProcessor launcher = new UeiCommandProcessor();
        int returnCode = launcher.run(args);
        System.exit(returnCode);
    }
    
}
