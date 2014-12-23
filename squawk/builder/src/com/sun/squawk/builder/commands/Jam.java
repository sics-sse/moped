/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.builder.commands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import com.sun.squawk.builder.Build;
import com.sun.squawk.builder.BuildException;

public class Jam extends Thread {

    /**
     * Environment information.
     */
    protected Build env;

    /**
     * Flag indicating if the TCK Agent and Client to be used should be the cobbled source that we
     * have in our javatest-agent module.
     */
    protected boolean useOurAgent = false;

    /**
     * What arguments to pass of to the Squawk VM when running the TCK suite created.
     */
    protected Vector<String> squawkArgs;

    /**
     * Flag indicating whether we should execute the TCK main in debug mode or not, ie whether the
     * TCK main will be run inside of an SDA or not.
     */
    protected boolean includeDebugger;

    /**
     * Flag indicating if as much information as possible should be logged to console while I am
     * running.
     */
    protected boolean verbose;

    /**
     * Flag used to determine whether or not a getNextApp call succeeded at least once. This is done
     * in order to simplify the output I put out when an IO occurs on attempting to fetch the next
     * application descriptor.
     */
    protected boolean suceededAtLeastOnceToGetNextApplicationDescriptor;

    /**
     * URL to get the next application descriptor from a JavaTest server
     */
    protected String getNextAppUrlString;

    protected String jamId;

    protected PrintStream out;

    protected boolean failFast;
    
    protected boolean hasSynchronizedWithVm;

    /**
     * Holder for all information provided in an application descriptor as fetched from a JavaTest
     * server.
     * 
     * 
     */
    class ApplicationDescriptor {
        public String applicationName;
        public String jarFileUrlString;
        public int jarFileSize;
        public String mainClassName;
        public String midlet1Property;
        public boolean useOnce;
        public Properties jadProperties;
        
        void printDescriptor() {
                System.out.println("applicationName: " + applicationName);
                System.out.println("jarFileUrlString: " + jarFileUrlString);
                System.out.println("jarFileSize: " + jarFileSize);
                System.out.println("mainClassName: " + mainClassName);
                System.out.println("midlet1Property: " + midlet1Property);
                System.out.println("useOnce: " + useOnce);
                
                Enumeration e = jadProperties.keys();
                while (e.hasMoreElements()) {
                    Object key = e.nextElement();
                    Object value = jadProperties.get(key);
                    System.out.println("    " + key + "=" + value);
                }
        }
    }

    /**
     * Clean up files when done?
     */
    private boolean noCleanUp;

    public Jam(String jamId, String getNextAppUrlString, Build env, boolean useOurAgent, Vector<String> squawkArgs, boolean includeDebugger, boolean verbose, boolean noCleanUp, PrintStream out, boolean failFast) {
        this.jamId = jamId;
        this.env = env;
        this.useOurAgent = useOurAgent;
        this.squawkArgs = squawkArgs;
        this.includeDebugger = includeDebugger;
        this.verbose = verbose;
        this.noCleanUp = noCleanUp;
        this.getNextAppUrlString = getNextAppUrlString;
        this.out = out;
        this.failFast = failFast;
    }

    public void init() {
    }

    // ***************** Connection with test harness *****************
    /**
     * Get the contents to be found at URL <code>urlString</code> and place it into the file
     * <code>destination</code>.
     * 
     * @param urlString URL to fetch content from
     * @param destination File to write content of URL to
     * @throws BuildException
     */
    protected void getJar(String urlString, File destination) throws BuildException {
        InputStream input = null;
        OutputStream output = null;
        try {
            URL url = new URL(urlString);
            input = new BufferedInputStream(url.openStream());
            output = new BufferedOutputStream(new FileOutputStream(destination));
            byte buffer[] = new byte[1024];
            int readCount;
            while ((readCount = input.read(buffer, 0, buffer.length)) != -1) {
                output.write(buffer, 0, readCount);
            }
        } catch (IOException e) {
            throw new BuildException("error getJar \"" + urlString + "\" into \"" + destination.getName() + "\"", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
            input = null;
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
            output = null;
        }
    }

    /**
     * Connect to getNextAppUrl and return the ApplicationDescriptor found there. If there is an
     * error connecting to JavaTest, it will keep trying forever.
     * 
     * @return
     */
    protected ApplicationDescriptor getNextApplicationDescriptor() {
        boolean reportedError = false;
        int timesToWaitForJavaTest = 10;

        while (true) {
            try {
                return downloadApplicationDescriptor();
            } catch (BuildException e) {
                if (suceededAtLeastOnceToGetNextApplicationDescriptor) {
                    printPrefix();
                    if (--timesToWaitForJavaTest <= 0) {
                        out.println("Seems JavaTest is no longer running - GIVING UP.");
                        return null;
                    } else {
                        out.println("Seems JavaTest is no longer running, will keep trying " + timesToWaitForJavaTest +  " more times.");
                    }
                } else if (!reportedError) {
                    printPrefix();
                    out.println("Seems you have not yet started JavaTest, will keep trying.");
                }
				if (!reportedError) {
                    String urlString = getNextAppUrlString;
                    if (jamId != null) {
                        urlString += "/" + jamId;
                    }
                    printPrefix();
                    out.println("   URL: " + urlString);
                    printPrefix();
                    out.println("   err: " + e);
                    reportedError = true;
				}
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /**
     * Connect to getNextAppUrl and return the result.
     * 
     * @param jamId The JAM ID to pass on all requests to the JavaServer
     * @return
     */
    protected ApplicationDescriptor downloadApplicationDescriptor() throws BuildException {
        ApplicationDescriptor descriptor = new ApplicationDescriptor();
        BufferedReader fileReader = null;
        try {
            String urlString = getNextAppUrlString;
            if (jamId != null) {
                urlString += "/" + jamId;
            }
            URL url = new URL(urlString);
            fileReader = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            descriptor.jadProperties = new Properties();
            while ((line = fileReader.readLine()) != null) {
                Reader lineReader = new StringReader(line);
                StringBuffer keyBuffer = new StringBuffer();
                StringBuffer valueBuffer = new StringBuffer();
                int read;
                while (((read = lineReader.read()) != -1) && (read != ':')) {
                    keyBuffer.append((char) read);
                }
                if (read == ':') {
                    while (((read = lineReader.read()) != -1)) {
                        valueBuffer.append((char) read);
                    }
                    String key = keyBuffer.toString().trim();
                    // CLDC Related properties
                    String value = valueBuffer.toString().trim();
                    if (key.equals("Application-Name")) {
                        descriptor.applicationName = value;
                    } else if (key.equals("JAR-File-URL")) {
                        descriptor.jarFileUrlString = value;
                    } else if (key.equals("JAR-File-Size")) {
                        descriptor.jarFileSize = Integer.parseInt(value);
                    } else if (key.equals("Main-Class")) {
                        descriptor.mainClassName = value;
                    } else if (key.equals("Use-Once")) {
                        descriptor.useOnce = value.equals("yes");
                        // IMP related properties
                    } else if (key.equals("MIDlet-Name")) {
                        descriptor.applicationName = value;
                    } else if (key.equals("MIDlet-Jar-URL")) {
                        descriptor.jarFileUrlString = value;
                    } else if (key.equals("MIDlet-Jar-Size")) {
                        descriptor.jarFileSize = Integer.parseInt(value);
                    } else if (key.equals("MIDlet-1")) {
                        descriptor.midlet1Property = value;
                    } else {
                        // Put all the properties we do not use into a generic bin
                        descriptor.jadProperties.setProperty(key, value);
                    }
                }
            }
//System.out.println("Application descriptor: ");
//descriptor.printDescriptor();
            suceededAtLeastOnceToGetNextApplicationDescriptor = true;
            return descriptor;
        } catch (IOException e) {
            throw new BuildException("unable to get application descriptor: " + e.getMessage());
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                }
            }
            fileReader = null;
        }
    }

    // ***************** END OF Connection with test harness *****************

    /**
     * Return the classpath to be used in order to create a test bundle jar suite.
     * 
     * @param jarPath Path to the jar containing the test bundle from which to create a suite
     * @return
     */
    protected Vector<String> getTestSuiteClassPath(String jarPath) {
        Vector<String> result = new Vector<String>();
        result.add(jarPath);
        if (useOurAgent) {
            result.add(new File("javatest-device/j2meclasses").getAbsolutePath());
        }
        return result;
    }

    /**
     * Return the path to the folder containing the bootstrap and parent suites.
     * 
     * @return Path to boostrap, or null if none to be specified
     */
    protected String getTestSuiteSuiteFolder() {
        return ".";
    }

    /**
     * Get the default little endian value to use when building the test bundle suite.
     * 
     * @return
     */
    protected Boolean getTestSuiteLitteEndian() {
        return null;
    }

    /**
     * Get the default parent suite to use when building the test bundle suite.
     * 
     * @return
     */
    protected String getTestSuiteParent() {
        return null;
    }

    /**
     * Return the name of the Squawk VM executable that can be used to invoke it from within a
     * shell/command box. Use the squawk executable from the current directory assuming you are
     * running from the Squawk directory
     * 
     * @return Name of the Squawk VM executable
     */
    protected String getSquawkExecutable() {
        return "./squawk" + env.getPlatform().getExecutableExtension();
    }

    /**
     * Create a suite based on the parameters specified.
     * 
     * @param parent Name of the parent suite of the suite to be created
     * @param bootstrapSuitePath Path to the bootstrap suite of the suite to be created
     * @param classPath Classpath containing all classes to be included in suite to be created
     * @param littleEndian True if the suite to be created is to be little endian, false for big endian, and
     *                     null to use the default of the host platform
     * @param suiteName The name of the suite to be created
     * @param properties properties from the application descriptor file (.jad)
     */
    protected void createSuite(String parent, String suiteFolder, Vector<String> classPath, Boolean littleEndian, String suiteName, Properties properties) {
    	// Loop forever and only exit once we successfully created the suite
    	final String romizeCommandName = "romize";
        Vector<String> args = new Vector<String>();
        args.add(romizeCommandName);
        if (verbose) {
        	args.add("-verbose");
        }
    	args.add("-nobuildproperties");
    	args.add("-suitepath:" + suiteFolder);
    	args.add("-boot:squawk");
    	if (parent != null) {
    		args.add("-parent:" + parent);
    	}
    	String cp = "-cp";
    	for (String string : classPath) {
			cp += ":" + string;
		}
    	args.add(cp);
    	if (littleEndian == Boolean.TRUE) {
    		args.add("-endian:little");
    	}
    	args.add("-o:" + suiteName);
        if (parent != null) {
            args.add("-parent:" + parent);
        }
        args.add("-strip:d");
        if (littleEndian != null && littleEndian.booleanValue()) {
        	args.add("-endian:little");
        }
        for (Enumeration<?> keys = properties.keys(); keys.hasMoreElements(); ) {
            String key = (String) keys.nextElement();
            String value = (String) properties.get(key);
            args.add("-key:" + key);
            args.add("-value:" + value);
        }
    	args.addAll(classPath);
    	if (verbose) {
    		for (String arg: args) {
    			System.out.print("  arg: ");
    			System.out.println(arg);
    		}
    	}
		env.mainProgrammatic(args.toArray(new String[args.size()]));
    }

    /**
     * Defined by JAMs that require deploying the application to a device (JamSpot for example)
     * 
     * @param suitePath path to the suite file
     * @param mainClassName name of the class containing the main method.
     */
    protected void deploySuite(String suitePath, String mainClassName) {
    }

    /**
     * Execute the suite named <code>suiteName</code> by invoking the main of
     * <code>mainClassName</code>.
     * 
     * @param suiteName
     * @param mainClassName
     */
    protected void executeSuite(String suiteName, String mainClassName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getSquawkExecutable());
        for (int i = 0; i < squawkArgs.size(); i++) {
            buffer.append(" ");
            buffer.append(squawkArgs.get(i));
        }
        if (env.getBooleanProperty("PLATFORM_TYPE_DELEGATING")) {
            buffer.append(" -J-Djava.awt.headless=true");
        }
        buffer.append(" -suite:");
        buffer.append(suiteName);
        if (includeDebugger) {
            buffer.append(" com.sun.squawk.debugger.sda.SDA");
        }

        // If mainClassName null then we assume MIDlet launch semantics
        if (mainClassName != null) {
            buffer.append(" ");
            buffer.append(mainClassName);
        }
        String command = buffer.toString();
    	printPrefix();
        out.println("executing suite with command: " + command);
        env.exec(command);
    }

    /**
     * For each JAM cycle, various files may be created, make sure to remove these.
     * 
     * @param jarFile File of the jar file used to create the test suite
     * @param suitePath Path, including filename of the suite file created
     */
    protected void doCleanup(File jarFile, String suitePath) {
        if (jarFile != null) {
            jarFile.delete();
        }
        if (suitePath != null) {
            new File(suitePath + MakeAPI.SUITE_FILE_EXTENSION).delete();
            new File(suitePath + MakeAPI.SUITE_FILE_EXTENSION + MakeAPI.SUITE_FILE_EXTENSION_METADATA).delete();
            new File(suitePath + MakeAPI.SUITE_FILE_EXTENSION + MakeAPI.SUITE_FILE_EXTENSION_API).delete();
            new File(suitePath + ".sym").delete();
        }
    }

    /**
     * Run the JAM
     */
    public void run() {
    	if (!hasSynchronizedWithVm) {
    		throw new RuntimeException("You must synchornizeWithVm prior to running");
    	}
        while (true) {

            ApplicationDescriptor descriptor;
        	if (verbose) {
            	printPrefix();
        		out.println("getNextApplicationDescriptor");
        	}
            descriptor = getNextApplicationDescriptor();
            if (descriptor == null) {
                return;
            }
            File jarFile = null;
            String suitePath = null;
            final String jarExtension = ".jar";

            try {
                try {
                    env.clearRunSet();
                    // This is a trick to get the correct dir, it seems that using
                    // new File("") as a direct argument
                    // does not yield the same result.
                    File currentDirectory = new File("").getCanonicalFile();
                    jarFile = File.createTempFile("jam-", jarExtension, currentDirectory);
                    out.println("Running jam " + jarFile.getName());
                } catch (IOException e) {
                    throw new BuildException("unable to create temp file to store jar file: " + e.getMessage());
                }
            	if (verbose) {
                	printPrefix();
            		out.println("  " + jarFile.getName() +" getJar: " + descriptor.jarFileUrlString);
            	}
                getJar(descriptor.jarFileUrlString, jarFile);
                String jarPath = jarFile.getPath();
                suitePath = jarPath.substring(0, jarPath.length() - jarExtension.length());
                try {
                	if (verbose) {
                    	printPrefix();
                		out.println("  " + jarFile.getName() +" createSuite: " + descriptor.jarFileUrlString);
                	}
                    createSuite(getTestSuiteParent(), getTestSuiteSuiteFolder(), getTestSuiteClassPath(jarPath), getTestSuiteLitteEndian(), suitePath, descriptor.jadProperties);
                	if (verbose) {
                    	printPrefix();
                		out.println("  " + jarFile.getName() +" deploySuite: " + descriptor.jarFileUrlString);
                	}
                    deploySuite(suitePath, descriptor.mainClassName);
                	if (verbose) {
                    	printPrefix();
                		out.println("  " + jarFile.getName() +" executeSuite: " + descriptor.jarFileUrlString);
                	}
                    executeSuite(suitePath, descriptor.mainClassName);
                } catch (BuildException e) {
                    System.err.println("Error building test:");
                    System.err.println("    " + e.getMessage());
                } finally {
                }
            } finally {
                if (!noCleanUp) {
                	if (verbose) {
                    	printPrefix();
                		out.println("  " + jarFile.getName() +" cleanup: " + descriptor.jarFileUrlString);
                	}
                    doCleanup(jarFile, suitePath);
//                    if (jarFile != null) {
//                        jarFile.delete();
//                    }
//                    if (suitePath != null) {
//                        new File(suitePath + MakeAPI.SUITE_FILE_EXTENSION).delete();
//                        new File(suitePath + MakeAPI.SUITE_FILE_EXTENSION + MakeAPI.SUITE_FILE_EXTENSION_API).delete();
//                        new File(suitePath + ".bintemp").delete();
//                    }
                }
            }
            Thread.yield();
        }

    }
    
    protected void printPrefix() {
    	if (jamId == null) {
    		out.print("jam:");
    	} else {
    		out.print(jamId);
    		out.print(':');
    	}
    }
    
    public void synchronizeWithVm() {
    	if (verbose) {
        	printPrefix();
    		out.println("init");
    	}
        init();
    	if (verbose) {
        	printPrefix();
    		out.println("  done init");
    	}
    	hasSynchronizedWithVm = true;
    }

}