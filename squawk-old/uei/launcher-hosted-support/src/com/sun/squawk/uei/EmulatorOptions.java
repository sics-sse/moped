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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.sun.squawk.uei.utilities.J2MEApplicationDescriptor;
import com.sun.squawk.uei.utilities.MidletEntryPoint;

/**
 * An object containing all UEI-specified options randomly-accessible; also
 * parses a given array of command line arguments into the associated options.
 * All option names should be the lowercase version of their command line
 * counterparts, enabling dynamic assignment of said variables and in any
 * extensions thereof through use of the <code>setOption</code> method.
 */
public class EmulatorOptions {
    
    public final static String PROPERTY_INVALID = "";
    
    public boolean help        = false;
    public boolean main        = false;  // TODO add support for running main classes?
    public boolean version     = false;
    public boolean xdebug      = false;
    public boolean xquery      = false;
    
    public String  classpath   = null;
    public String  midletName  = null;
    public String  xautotest   = null;
    public String  xdescriptor = null;
    public String  xdevice     = null;
    public String  xheapsize   = null;
    public String  xjam        = null;
    public String  xrunjdwp    = null;
    public String  xverbose    = null;
    
    public String  xrunjdwp_transport = "dt_socket";
    public String  xrunjdwp_address   = null;
    public boolean xrunjdwp_server    = false;
    public boolean xrunjdwp_suspend   = true;
    
    public final Properties emulateeSystemProperties = new Properties(System.getProperties());
    
    public final List<String> unrecognizedOptions = new ArrayList<String>();
    
    public String[] getSupportedOptions() {
        return new String[] {"Xdescriptor", "Xdebug"};
    }

    /**
     * Processes the given argument in accordance with the UEI 1.0.2 spec.
     * 
     * @param arg The argument to be processed.
     * @return true if this argument was processed, false otherwise.
     */
    protected boolean processArgument(String arg) {
        if (arg.charAt(0) != '-') {
            midletName = arg;
        } else if (arg.equalsIgnoreCase("-help")) {
            help = true;
        } else if (arg.equalsIgnoreCase("-main")) {
            help = true;
        } else if (arg.equalsIgnoreCase("-version")) {
            version = true;
        } else if (arg.equalsIgnoreCase("-Xdebug")) {
            xdebug = true;
        } else if (arg.equalsIgnoreCase("-Xquery")) {
            xquery = true;
        } else if (arg.startsWith("-X")) {
            if (arg.equalsIgnoreCase("-Xverbose")) {
                arg = arg + ":all";
            }
            return processXcolonProperty(arg);
        } else if (arg.startsWith("-D")) {
            return processDproperty(arg);
        } else {
            return false;
        }
        
        return true;
    }
    
    /**
     * Processes UEI-compliant command line arguments, depositing them into this
     * randomly-accessible options object.
     * 
     * @param args The command line arguments to parse.
     * @throws EmulatorException if the arguments passed contain obvious errors.
     */
    public void processCommandLineArguments(String args[]) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.equalsIgnoreCase("-classpath")) {
                if (++i < args.length && args[i].charAt(0) != '-') {
                    classpath = args[i];
                } else {
                    classpath = EmulatorOptions.PROPERTY_INVALID;
                }
            } else if (processCustomArgument(arg)) {
                ;  // Argument was processed; do nothing
            } else if (processArgument(arg)) {
                ;  // Argument was processed; do nothing
            } else {
                unrecognizedOptions.add(arg);
            }
        }
    }
    
    /**
     * Extensions of this class that want to process custom arguments should
     * override this method.
     * 
     * @param arg The argument to be processed.
     * @return true if this argument was processed, false otherwise.
     */
    protected boolean processCustomArgument(String arg) {
        return false;
    }
    
    /**
     * Extracts midletName and classpath from JAD file.
     * 
     * @throws EmulatorException if either midletName or classpath conflict with
     *             already values.
     */
    protected void processDescriptor() throws EmulatorException {
        try {
            // Read the descriptor to find the JAR file name as well as the
            // entry points
            File descriptorFile = new File(xdescriptor);
            
            J2MEApplicationDescriptor applicationDescriptor;
            applicationDescriptor = new J2MEApplicationDescriptor(descriptorFile);
            Collection<MidletEntryPoint> midletEntryPoints = applicationDescriptor.getMidletEntryPoints();
            
            String jarFileName = applicationDescriptor.getJarUrl();
            // Find absolute path for the JAR file
            File jarFile = new File(jarFileName);
            if (!jarFile.isAbsolute()) {
                jarFile = new File(descriptorFile.getParentFile(), jarFileName);
            }
            
            String jarpath = jarFile.getAbsolutePath();
            if (classpath == null) {
                classpath = jarpath;
            } else if (!classpath.equals(jarpath)) {
                throw new EmulatorException("Error: JAD classpath does not match specified classpath: " + classpath + " : " + jarpath, false);
            }
            if (!new File(classpath).exists()) {
                throw new EmulatorException("Error: Specificed classpath does not exist: " + classpath, false);
            }
            
            // System.out.println("Looking for MIDlet in " + jarpath);
            
            Iterator<MidletEntryPoint> midletIterator = midletEntryPoints.iterator();
            
            if (midletIterator.hasNext()) {
                // Find MIDlet-1
                MidletEntryPoint midlet1 = midletIterator.next();
                String midletOneName = midlet1.getClassName();
                
                // Start squawk with the appropriate classpath and class
                if (midletName == null) {
                    midletName = midletOneName;
                } else if (!midletName.equals(midletOneName)) {
                    throw new EmulatorException("Error: JAD MIDlet-1 does not match specified MIDlet: " + midletName + " : " + midletOneName, false);
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Processes an argument of the form "-Dproperty=value"
     * 
     * @param arg The argument to be processed.
     * @return true if this argument was processed, false otherwise.
     */
    protected boolean processDproperty(String arg) {
        int equalsIndex = arg.indexOf('=');
        if (equalsIndex <= 0) {
            return false;
        }
        
        String property, value;
        property = arg.substring(2, equalsIndex);
        value = arg.substring(equalsIndex + 1);
        
        emulateeSystemProperties.setProperty(property, value);
        return true;
    }
    
    /**
     * Process the values of the properties previously read into this object.
     * 
     * @throws EmulatorException if the arguments passed contain obvious errors.
     */
    public void processPropertyValues() throws EmulatorException {
        
        if (classpath != null) {
            if (classpath.equals(PROPERTY_INVALID)) {
                throw new EmulatorException("-classpath requires classpath specification", true);
            } else if (!new File(classpath).exists()) {
                throw new EmulatorException("Specificed classpath does not exist: " + classpath, false);
            }
        }
        if (midletName != null && midletName.equals(PROPERTY_INVALID)) {
            throw new EmulatorException("Invalid MIDlet specified", true);
        }
        if (xdescriptor != null) {
            if (xdescriptor.equals(PROPERTY_INVALID)) {
                throw new EmulatorException("-Xdescriptor requires a jad-path specification", true);
            } else if (xdescriptor.startsWith("file:")) {
                xdescriptor = xdescriptor.substring("file:".length());
            }
            if (!new File(xdescriptor).exists()) {
                throw new EmulatorException("Specificed jad-file does not exist: " + xdescriptor, false);
            }
            processDescriptor();
        }
        
        if (xdebug == true && xrunjdwp == null) {
            throw new EmulatorException("-Xdebug must be used with -Xrunjdwp", true);
        }
        if (xrunjdwp != null) {
            if (xrunjdwp.equals(PROPERTY_INVALID)) {
                throw new EmulatorException("-Xrunjdwp requires jdwp specifications", true);
            } else if (xdebug == false) {
                throw new EmulatorException("-Xrunjdwp must be used with -Xdebug", true);
            }
            processXrunjdwpValue();
        }
        
        if (xdevice != null && xdevice.equals(PROPERTY_INVALID)) {
            throw new EmulatorException("-Xdevice requires a device specification", true);
        }
        if (xheapsize != null && xheapsize.equals(PROPERTY_INVALID)) {
            throw new EmulatorException("-Xheapsize requires a memory size specification", true);
        }
        if (xverbose != null && xverbose.equals(PROPERTY_INVALID)) {
            throw new EmulatorException("Invalid -Xverbose option specified", true);
        }
        
        // After processing JAD file, recheck midletName and classpath
        if (midletName == null || midletName.equals(EmulatorOptions.PROPERTY_INVALID)) {
            throw new EmulatorException("Error: No MIDlet specified", true);
        }
        if (classpath == null || classpath.equals(EmulatorOptions.PROPERTY_INVALID)) {
            throw new EmulatorException("Error: No classpath specified", true);
        }
    }
    
    /**
     * Processes an argument of the form "-Xproperty:value"
     * 
     * @param arg The argument to be processed.
     * @return true if this argument was processed, false otherwise.
     */
    protected boolean processXcolonProperty(String arg) {
        String option, property;
        
        int colonIndex = arg.indexOf(':');
        if (colonIndex < 0) {
            option   = arg.substring(1);
            property = EmulatorOptions.PROPERTY_INVALID;
        } else {
            option   = arg.substring(1, colonIndex);
            property = arg.substring(colonIndex + 1);
        }
        
        return setOption(option, property);
    }
    
    /**
     * Extracts the transport, address, server, and suspend values from the
     * "-Xrunjdwp" argument.
     * 
     * @throws EmulatorException if the arguments do not comply with UEI spec.
     */
    protected void processXrunjdwpValue() throws EmulatorException {
        String jdwpOptions = xrunjdwp;
        while (jdwpOptions.length() > 0) {
            String option;
            
            int commaIndex = jdwpOptions.indexOf(',');
            if (commaIndex > 0) {
                option = jdwpOptions.substring(0, commaIndex);
                jdwpOptions = jdwpOptions.substring(commaIndex + 1);
            } else {
                option = jdwpOptions;
                jdwpOptions = "";
            }
            
            String name, value;
            int equalsIndex = option.indexOf('=');
            name = option.substring(0, equalsIndex);
            value = option.substring(equalsIndex + 1);
            
            if (name.equalsIgnoreCase("transport")) {
                xrunjdwp_transport = value;
            } else if (name.equalsIgnoreCase("address")) {
                String host, port;
                int colonIndex = value.indexOf(':');
                if (value.indexOf(':') < 0) {
                    host = "localhost";
                    port = value;
                } else {
                    host = value.substring(0, colonIndex);
                    port = value.substring(colonIndex + 1);
                }
                
                try {
                    Integer.parseInt(port);
                } catch (Exception e) {
                    throw new EmulatorException("Invalid port specified for -Xrunjdwp:" + option, false);
                }
                
                xrunjdwp_address = host + ":" + port;
            } else if (name.equalsIgnoreCase("server")) {
                if (value.equalsIgnoreCase("y")) {
                    xrunjdwp_server = true;
                } else if (value.equalsIgnoreCase("n")) {
                    xrunjdwp_server = false;
                } else {
                    throw new EmulatorException("-Xrunjdwp:" + name + "requires \"y\" or \"n\" argument", false);
                }
            } else if (name.equalsIgnoreCase("suspend")) {
                if (value.equalsIgnoreCase("y")) {
                    xrunjdwp_suspend = true;
                } else if (value.equalsIgnoreCase("n")) {
                    xrunjdwp_suspend = false;
                } else {
                    throw new EmulatorException("-Xrunjdwp:" + name + "requires \"y\" or \"n\" argument", false);
                }
            } else {
                unrecognizedOptions.add("-Xrunjdwp:" + option);
            }
        }
    }
    
    /**
     * Sets the field that is the lowercase version of <code>optionName</code>
     * to <code>value</code>.
     * 
     * @param optionName The option-field name.
     * @param value The value to set the option to.
     * @return True if an option-field was set; false otherwise.
     */
    public boolean setOption(String optionName, String value) {
        try {
            this.getClass().getField(optionName.toLowerCase()).set(this, value);
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }
    
}
