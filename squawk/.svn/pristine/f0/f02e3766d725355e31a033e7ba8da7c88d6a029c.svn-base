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

/**
 * Abstract emulator, containing basic implementations of things that can be
 * implemented in a reasonable manner for all device emulators.
 */
public abstract class DeviceDescriptor {
    public static final String CONFIGURATION_CLDC10 = "CLDC-1.0";
    public static final String CONFIGURATION_CLDC11 = "CLDC-1.1";
    public static final String CONFIGURATION_CDC10 = "CDC-1.0";
    public static final String CONFIGURATION_CDC11 = "CDC-1.1";
    public static final String PROFILE_MIDP10 = "MIDP-1.0";
    public static final String PROFILE_MIDP20 = "MIDP-2.0";
    public static final String PROFILE_IMP10 = "IMP-1.0";
    public static final String PROFILE_IMPNG = "IMP-NG";
    
    protected static final File EMULATOR_DIRECTORY = new File(System.getProperty("user.dir")).getParentFile();
    protected static final File LIB_DIRECTORY = new File(EMULATOR_DIRECTORY, "lib");

    protected String configuration;
    protected String profile;
    protected String[] bootstrapClasspath;
    protected String name;
    
    /**
     * Constructs an Emulator, using the specified <code>EmulatorOptions</code>.
     * 
     * @param options The default <code>EmulatorOptions</code>.
     */
    public DeviceDescriptor(String name, String configuration, String profile, String... bootstrapClasspath) {
        this.name = name;
        this.configuration = configuration;
        this.profile = profile;
        this.bootstrapClasspath = new String[bootstrapClasspath.length];
        for (int i = 0; i < bootstrapClasspath.length; i++) {
            this.bootstrapClasspath[i] = new File(LIB_DIRECTORY, bootstrapClasspath[i]).getAbsolutePath();
        }
    }
    
    public String[] getBootstrapClasspath() {
        return bootstrapClasspath;
    }

    /**
     * Return the configuration implemented by this device.  Should be one of CDC, CLDC, ...
     * 
     * @return
     */
    public String getConfigurationName() {
        return configuration;
    }

    public String getDeviceName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getOptionalAPIs() {
        return null;
    }
    
    /**
     * Return the profile implemented by this device.  Should be one of MIDP, IMP, ...
     * 
     * @return
     */
    public String getProfileName() {
        return profile;
    }
    
    public abstract void startMIDlet(EmulatorOptions options);
    
    /**
     * Return true if this device supports the -Xautotest option.
     * 
     * @return
     */
    public boolean supportsAutotestOption() {
        return false;
    }

    /**
     * Return true if this device supports the -Xdebug set of options.
     * 
     * @return
     */
    public boolean supportsDebugOption() {
        return true;
    }

    /**
     * Return true if this device supports the -Xdescriptor option.
     * 
     * @return
     */
    public boolean supportsDescriptorOption() {
        return true;
    }

    /**
     * Return true if this device supports the -Xheapsize option.
     * 
     * @return
     */
    public boolean supportsHeapSizeOption() {
        return false;
    }

    /**
     * Return true if this device supports the -Xjam option.
     * 
     * @return
     */
    public boolean supportsJamOption() {
        return false;
    }

    /**
     * Return true if this device supports the -D<property name>=<value> option.
     * 
     * @return
     */
    public boolean supportsSetPropertyOption() {
        return false;
    }

    /**
     * Return true if this device supports the -Xverbose set of options.
     * 
     * @return
     */
    public boolean supportsVerboseOption() {
        return false;
    }

}
