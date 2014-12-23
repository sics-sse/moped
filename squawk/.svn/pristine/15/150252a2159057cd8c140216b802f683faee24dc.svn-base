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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.sun.squawk.builder.Build;
import com.sun.squawk.builder.BuildException;
import com.sun.squawk.builder.Command;
import com.sun.squawk.builder.CommandException;

/**
 * Command that performs the functions of a JAM as described in CLDC Technology Compatibility Kit Version 1.1 User's Guide Appendix A.
 * Do an HTTP get from the specified URL and parse result as an Application Descriptor
 *      <code>Application-Name</code>: Test Suite
 *      <code>JAR-File-URL</code>: URL of next test bundle (Jar)
 *      <code>JAR-File-Size</code>: SizeInBytes Will not exceed limit set in Jar, File Size Limit interview question; see Section 4.2.2 'The Configuration Interview'
 *      <code>Main-Class</code>: com.sun.tck.cldc.javatest.agent.CldcAgent
 *      <code>Use-Once</code>: yes Means do not cache test bundles (which all have the same URL)
 * Fetch the JAR file from the <code>JAR-File-URL</code> specified, install JAR and invoke main on specified <code>Main-Class</code>
 * 
 *
 */
public class JamCommand extends Command {

    private static final String NEXT_APP_URL_ARG = "-url:";

    /**
     * Flag to be passed to the command line to output debug output to a file.
     */

    private static final String OUTPUT_TO_FILE_ARG = "-output-to-file";

    /**
     * Flag to be passed to the command line to specify what profile we are testing
     */
    private static final String PROFILE_ARG = "-profile:";

    /**
     * CLDC profile. 
     */
    protected static final String CLDC_PROFILE = "cldc";

    /**
     * IMP profile
     */
    protected static final String IMP_PROFILE = "imp";

    /**
     * Profile to test against: imp or cldc
     */
    protected String profile;

    /**
     * How many times I should repeat execuction of runOnce.
     * A value of -1 indicated that repeat should be forever.
     */
    protected int repeatCount;

    /**
     * How many parallel instances/threads I should spawn to perform tests.  This is only useful
     * in case of running tests on desktop.
     */
    protected int parallelCount = 1;
    
    /**
     * Include IMP classes in bootstrap.
     */
    protected boolean includeImp;

    /**
     * Upon being run, should I go through a fresh build of all componens necessary to run TCK tests.
     */
    protected boolean doBuild;
    
    /**
     * Force a rebuild of bootstrap even though -nobuild was used.
     */
    protected boolean doBuildBootstrap;

    /**
     * If true then skip cleaning up any temp files I may have/will created.
     */
    protected boolean noCleanup;

    /**
     * JAM ID to use with the URL specified to identify this instance of JAM command from another.
     */
    protected int jamIdArgument;

    /**
     * Flag indicating whether we should execute the TCK main in debug mode or not, ie
     * whether the TCK main will be run inside of an SDA or not.
     */
    protected boolean includeDebugger;

    /**
     * Flag indicating if as much information as possible should be logged to console while I am running.
     */
    protected boolean verbose;
    
    /**
     * Flag to indicate I should exit with an error on first problem translating classes into suite.
     */
    protected boolean failFast;

    /**
     * What arguments to pass of to the Squawk VM when running the TCK suite created.
     */
    protected Vector<String> squawkArgs;

    /**
     * Flag indicating if the TCK Agent and Client to be used should be the cobbled source that
     * we have in our javatest-agent module.
     */
    protected boolean useOurAgent = false;

    /**
     * URL to use in order to get the next test app.
     * ex. http://localhost:8080/test/getNextApp (CLDC) or 
     *     http://localhost:8089/test/getNextApp.jad (IMP) 
     *     Refer to your Java Test Harness to determine the URL.
     */
    protected String getNextAppUrlString;

    /**
     * If true, debug output will be printed to a file
     */
    private boolean outputToFile;

    /**
     * Create a new instance with its command string set to <code>"jam"</code>.
     * 
     * @param env
     */
    public JamCommand(Build env) {
        this(env, "jam");
    }

    /**
     * Create a new instance with its command string set to <code>name</code>.
     * 
     * @param env
     * @param name
     */
    protected JamCommand(Build env, String name) {
        super(env, name);
        squawkArgs = new Vector<String>();
        useOurAgent = true;
        profile = CLDC_PROFILE;
    }

    /**
     * Make a fresh build of all known modules, and then build the Squawk VM such that
     * it can be used to execute TCK test bundle jars.
     *
     */
    protected void build() {
        // Make sure we have a fresh build of everything needed to run a JAM session
        //   - bootstrap, translator and agent suites
        builder(new String[] {"clean"});
        builder(new String[0]);
    }

    /**
     * Build up Squawk VM, along with bootstrap and translator suites.
     *
     */
    protected void buildBootstrap() {
        List<String> command = new ArrayList<String>();
        command.add("-prod");
        command.add("-mac");
        command.add("-o2");
        command.add("rom");
        command.add("-strip:d");
        command.add("cldc");
        if (includeImp) {
        	command.add("imp");
        }
        if (includeDebugger) {
            command.add("debugger");
        }
        builder(command.toArray(new String[command.size()]));
    }

    /**
     * Execute <code>commandLine</code> as a command passed to builder.
     * 
     * @param commandLine The command line containing all arguments to pass on to the builder
     */
    protected void builder(String[] parameters) {
        // Pass through the command line options from the current builder
    	String[] args = new String[env.getBuilderArgs().size() + parameters.length];
    	int i=0;
        List<?> passThroughArgs = env.getBuilderArgs();
        for (Object arg : passThroughArgs) {
        	args[i++] = (String) arg;
		}
        System.arraycopy(parameters, 0, args, i, parameters.length);
        new Build(null).mainProgrammatic(args);
    }

    /**
     * Parse the command line argument passed in.
     * 
     * @param args
     * @return boolean   true if the command was actually parsed by me, false if option unknown to me
     * @throws BuildException
     */
    protected boolean parseArg(String arg, String[] args, int i) throws BuildException {
        if (arg.startsWith(PROFILE_ARG)) {
            profile = arg.substring(PROFILE_ARG.length());
        } else if (arg.equals("-repeat")) {
            repeatCount = -1;
        } else if (arg.startsWith("-repeat:")) {
            String string = arg.substring(8);
            try {
                repeatCount = Integer.parseInt(string);
            } catch (NumberFormatException e) {
                throw new BuildException("error in repeat number specified: " + e.getMessage());
            }
        } else if (arg.startsWith("-id:")) {
            String string = arg.substring(4);
            try {
                jamIdArgument = Integer.parseInt(string);
            } catch (NumberFormatException e) {
                throw new BuildException("error in jam ID specified: " + e.getMessage());
            }
        } else if (arg.equals("-debug")) {
            includeDebugger = true;
        } else if (arg.equals("-nocleanup")) {
            noCleanup = true;
        } else if (arg.startsWith("-parallel:")) {
            repeatCount = -1;
            String string = arg.substring(10);
            try {
                parallelCount = Integer.parseInt(string);
            } catch (NumberFormatException e) {
                throw new BuildException("error in parallel number specified: " + e.getMessage());
            }
        } else if (arg.equals("-v") || arg.equals("-verbose")) {
            verbose = true;
        } else if (arg.equals("-nobuild")) {
            doBuild = false;
        } else if (arg.equals("-failfast")) {
            failFast = true;
        } else if (arg.equals("-buildbootstrap")) {
            doBuildBootstrap = true;
        } else if (arg.startsWith("-Xts:")) {
            squawkArgs.add(arg);
        } else if (arg.equals("-tckagent")) {
            useOurAgent = false;
        } else if (arg.startsWith(NEXT_APP_URL_ARG)) {
            System.out.println("parsing " + arg);
            getNextAppUrlString = arg.substring(NEXT_APP_URL_ARG.length());
        } else if (arg.startsWith(OUTPUT_TO_FILE_ARG)) {
            outputToFile = true;
        } else {
            return false;
        }

        return true;
    }

    /**
     * Go through the command line options and pull out the information I need to run.
     * 
     * @param args
     * @throws BuildException
     */
    protected void parseArgs(String[] args) throws BuildException {
        for (int i = 0; i != args.length; ++i) {
            String arg = args[i];
            boolean parsed = parseArg(arg, args, i);
            if (!parsed) {
                throw new CommandException(this, "Unknown option: " + arg);
            }
        }
    }

    /**
     * Throw an exception if I am not in a state to be run, otherwise do nothing.
     * @throws BuildException
     */
    protected void checkCanRun() throws BuildException {
        if (profile.equals(CLDC_PROFILE)) {
            if (getNextAppUrlString == null) {
                getNextAppUrlString = "http://localhost:8080/test/getNextApp";
            }
        } else if (profile.equals(IMP_PROFILE)) {
        	includeImp = true;
            if (getNextAppUrlString == null) {
                getNextAppUrlString = "http://localhost:8091/test/getNextApp.jad";
            }
        } else {
            throw new CommandException(this, "The profile specified is invalid: " + profile);
        }
    }

    /**
     * Run the JAM command
     */
    public void run(String[] args) throws BuildException {
        jamIdArgument = -1;
        doBuild = true;
        doBuildBootstrap = false;
        repeatCount = 1;
        parseArgs(args);
        checkCanRun();

        if (doBuild) {
            build();
        }
        
        if (doBuild || doBuildBootstrap) {
        	buildBootstrap();
        }

        runJams();
    }

    /**
     * Create and run the JAMs for the test.
     */
    protected void runJams() {
        Vector<Jam> jams = new Vector<Jam>();
        // Create all JAMs
        env.log(true, "Synchronizing with VMs");
        for (int i = 0; i < parallelCount; i++) {
            Jam jam = createJam(i);
            jams.add(jam);
            jam.synchronizeWithVm();
        }
        env.log(true, "  Done synchronizing with VMs");
        for (Jam jam: jams) {
            jam.start();
            // Give a chance to the jam thread to start running right away
            Thread.yield();
        }
        // Block until all JAMs are done
        for (Jam jam : jams) {
            try {
                jam.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
		}
    }

    /**
     * Create the appropriate JAMs
     * @param jamId ID associated to the JAM
     * @return JAM to run
     */
    protected Jam createJam(int jamId) {
        PrintStream out = getPrintStream("jam" + jamId + ".txt");
        return new Jam("jam" + jamId, getNextAppUrlString, env, useOurAgent, squawkArgs, includeDebugger, verbose, noCleanup, out, failFast);

    }

    /**
     * Gets the printstream to use for debug output. If using outputToFile, this method will return a PrintStream that ouputs to sysout and a file.
     * @param fileName File name to use if we are ouputting to a file. If outputToFile is false, this will be ignored in the method
     * @return the PrintStream to use.
     */
    protected PrintStream getPrintStream(String fileName) {
        if (fileName.contains("/dev/")) {
            fileName = fileName.replaceAll("/dev/", "");
        }
        if (fileName.contains("/")) {
            fileName = fileName.replaceAll("/", "");
        }

        PrintStream out = null;
        if (outputToFile) {
            try {
                out = new PrintStream(new FileOutputStream(fileName)) {
                    public void println(String line) {
                        super.println(line);
                        System.out.println(line);
                    }
                };
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            out = System.out;
        }
        return out;
    }

    /**
     * Usage message.
     * @param errMsg   optional error message
     */
    public void usage(String errMsg) {
        PrintStream out = System.err;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("usage: jam [-options]");
        out.println("where options include:");
        out.println("    " + NEXT_APP_URL_ARG + "<next app url>    URL location of the next app to run (Refer to your Java Test Harness configuration)");
        out.println("    " + PROFILE_ARG + "profile    The profile to use, valid are: " + CLDC_PROFILE + ", " + IMP_PROFILE + " (default=" + CLDC_PROFILE + ")");
        out.println("    -repeat         Stay in jam mode forever");
        out.println("    -parallel:<n>   Run n clients in parallel");
        out.println("    -id:<n>         JAM id to use when requesting next app from JavaTest server");
        out.println("    -debug          Include debugger in bootstrap, and launch JavaTest suite in debug mode");
        out.println("    -nobuild        Do not rebuild the bootstrap, translator and agent suites");
        out.println("    -buildbootstrap Force a rebuild of the bootstrap suite");
        out.println("    -nocleanup      Do not delete the jar and suite files created");
        out.println("    -Xts:<n>        Start tracing after 'n' backward branches\n");
        out.println("    -h              Show this help message and exit");
        out.println("    -verbose        Log more info to console");
        out.println("    -failfast       Exit if any error on translating classes into suite happens");
        out.println("    -tckagent       Use the tck agent directly, instead of our own CldcAgent when running on device");
        out.println("");
    }

}
