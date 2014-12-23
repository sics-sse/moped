/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk;

import java.io.*;
import javax.microedition.io.Connector;

import com.sun.squawk.util.Tracer;
import com.sun.squawk.util.ArgsUtilities;
import com.sun.squawk.util.Assert;

/**
 * The Java application manager is the master isolate used to coordinate
 * application execution.
 * 
 */
public class JavaApplicationManager {

	/**
	 * Purely static class should not be instantiated.
	 */
	private JavaApplicationManager() {
	}

	/* if[ENABLE_DYNAMIC_CLASSLOADING] */
	/**
	 * The class path to use when loading through the translator instance (if
	 * any).
	 */
	private static String classPath;
	/* else[ENABLE_DYNAMIC_CLASSLOADING] */
	// private static final String classPath = null;
	/* end[ENABLE_DYNAMIC_CLASSLOADING] */

	/**
	 * The suite to which the leaf suite will be bound (if any).
	 */
	private static String parentSuiteURI;

	/**
	 * Command line option to enable display of execution statistics before
	 * exiting.
	 */
	private static boolean displayExecutionStatistics;

	/* if[DEBUG_CODE_ENABLED] */
	/**
	 * Specifies if the application is to be serialized, deserialized and then
	 * restarted when it stops by hibernating itself.
	 */
	private static boolean testoms;

	/**
	 * Specify the Midlet class name directly.
	 */
	private static String testMIDletClass;
	/* end[DEBUG_CODE_ENABLED] */

	/**
	 * Specify the MIDlet- property to extract to determine which MIDlet should
	 * be run from a suite.
	 */
	private static int midletPropertyNum;

	/**
	 * Main routine.
	 * 
	 * @param args
	 *            the command line argument array
	 * @throws java.lang.Exception
	 */
	public static void main(String[] args) throws Exception {

		// If no name is specified for MIDlet, assume MIDlet-1
		midletPropertyNum = 1;

		String mainClassName = null;
		String[] javaArgs = null;

		/* if[!EMULATOR_LAUNCHER] */

		/*
		 * Shortcut (AK) to simulate data transfer to PIRTE.
		 * Bytecodes are supplied as command line arguments and stored in a 
		 * global hashtable. 
		 * 
		 * This assumes that squawk is called with only pluginUrl and [bytecode] as 
		 * arguments, for example: 
		 * ./squawk plugin://tests/ClassLoaderInput/ClassLoaderInput.suite -34, -83, -66, -17, 0, 1, 0, ...
		 */
		if (args.length > 10) {
			/* Copy command line arguments into a byte array */
			byte[] data = new byte[args.length-1];
			for (int i = 1; i < args.length; i++) {
				if (args[i].indexOf(",") > -1)
					data[i-1] = Byte.parseByte(args[i].substring(0, args[i].indexOf(",")));
				else
					data[i-1] = Byte.parseByte(args[i]);
			}
			
			/* Store the byte array in a globally accessible static variable */
			VM.registerPluginObjectMemory(args[0], data);

			/* 
			 * Remove bytecode data from the command line arguments and instead add
			 * the main class name (e.g. PluginRuntimeEnvironment"). 
			 */
			String firstArg = args[0];
			args = new String[]{"sics.PIRTE"};
		}
		
		/*
		 * Process any switches.
		 */
		if (args.length != 0) {
			args = processVMOptions(args);
		}

		/* if[DEBUG_CODE_ENABLED] */
		if (testMIDletClass != null) {
			mainClassName = Isolate.MIDLET_WRAPPER_CLASS;
			javaArgs = new String[] { "-name", testMIDletClass };
		} else
		/* end[DEBUG_CODE_ENABLED] */
		if (args.length > 0) {
			/*
			 * Split out the class name from the other arguments.
			 */
			mainClassName = args[0].replace('/', '.');
			javaArgs = new String[args.length - 1];
			for (int i = 0; i < javaArgs.length; i++) {
				javaArgs[i] = args[i + 1];
			}
		} // else use midletPropertyNum

		/* else[EMULATOR_LAUNCHER] */
		// mainClassName = "com.sun.squawk.uei.j2me.Launcher";
		// javaArgs = new String[args.length];
		// for (int i = 0 ; i < javaArgs.length ; i++) {
		// javaArgs[i] = args[i];
		// }
		/* end[EMULATOR_LAUNCHER] */

		/*
		 * Get the start time.
		 */
		long startTime = System.currentTimeMillis();
		int exitCode = 999;
		
		/* if[ENABLE_MULTI_ISOLATE] */
		try {
			/*
			 * Create the application isolate and run it.
			 */
			Isolate isolate;
			if (mainClassName != null) {
				// create raw isolate
				isolate = new Isolate(null, mainClassName, javaArgs, classPath,
						parentSuiteURI);
			} else {
				// create midlet
				// System.out.println("@@@@@@@@@@@@@@@@@@@ midletPropertyNum = "+midletPropertyNum
				// + ", classPath ="+classPath +
				// ", parentSuiteURI = "+parentSuiteURI);
				isolate = new Isolate(null, midletPropertyNum, classPath,
						parentSuiteURI);
			}

			/*
			 * Start the isolate and wait for it to complete.
			 */
			isolate.start();
			isolate.join();

			/* if[!ENABLE_ISOLATE_MIGRATION] */
			/* else[ENABLE_ISOLATE_MIGRATION] */
			// /*
			// * If the isolate was hibernated then save it and restart it.
			// */
			// while (isolate.isHibernated() && testoms) {
			// try {
			// String url = "file://" + isolate.getMainClassName() + ".isolate";
			// DataOutputStream dos = Connector.openDataOutputStream(url);
			// isolate.save(dos, url, VM.isBigEndian());
			// System.out.println("Saved isolate to " + url);
			// dos.close();
			//
			// DataInputStream dis = Connector.openDataInputStream(url);
			// /*isolate = */ Isolate.load(dis, url);
			// dis.close();
			//
			// isolate.unhibernate();
			// isolate.join();
			//
			// } catch (java.io.IOException ioe) {
			// System.err.println("I/O error while trying to save or re-load isolate: ");
			// ioe.printStackTrace();
			// break;
			// }
			// }
			/* end[ENABLE_ISOLATE_MIGRATION] */

			/*
			 * Get the exit status.
			 */
			exitCode = isolate.getExitCode();

		} catch (Error e) {
			System.err.println(e);
			if (VM.isVerbose()) {
				e.printStackTrace();
			}
		}
		/*
		 * Show execution statistics if requested
		 */
		if (displayExecutionStatistics) {
			long endTime = System.currentTimeMillis();
			System.out.println();
			System.out.println("=============================");
			System.out.println("Squawk VM exiting with code " + exitCode);
			GC.getCollector().dumpTimings(System.out);
			System.out.println("Execution time " + (endTime - startTime)
					+ " ms");
			System.out.println("=============================");
			System.out.println();
		}

		/*
		 * Stop the VM.
		 */
		VM.stopVM(exitCode);

		/* else[ENABLE_MULTI_ISOLATE] */
		// AppThread appThread = null;
		// try {
		// /*
		// * Create the application thread and run it.
		// */
		// if (mainClassName != null) {
		// // create raw isolate
		// appThread = new AppThread(mainClassName, javaArgs);
		// } else {
		// // create midlet
		// args = new String[1];
		// args[0] = "MIDlet-" + midletPropertyNum;
		// appThread = new AppThread(Isolate.MIDLET_WRAPPER_CLASS, args);
		// }
		//
		// VM.getCurrentIsolate().morphBootstrapInto(null, classPath,
		// parentSuiteURI);
		// /*
		// * Start the application thread and wait for it to complete.
		// */
		// appThread.start();
		// //appThread.join(); // note that this isnt waiting for all app
		// threads to finish, just this thread.
		// } catch (Error e) {
		// System.err.println(e);
		// if (VM.isVerbose()) {
		// e.printStackTrace();
		// }
		// }
		/* end[ENABLE_MULTI_ISOLATE] */
	}

	/**
	 * Process any VM command line options.
	 * 
	 * @param args
	 *            the arguments as supplied by the VM.startup code
	 * @return the arguments needed by the main() routine of the isolate
	 */
	private static String[] processVMOptions(String[] args) {
		int offset = 0;
		if (false) Assert.that(VM.getCommandLineProperties().isEmpty());
		while (offset != args.length) {
			String arg = args[offset];
			if (arg.charAt(0) == '-') {
				processVMOption(arg);
			} else {
				break;
			}
			offset++;
		}
		String[] javaArgs = new String[args.length - offset];
		System.arraycopy(args, offset, javaArgs, 0, javaArgs.length);
		return javaArgs;
	}

	/**
	 * Shows the version information.
	 * 
	 * @param out
	 *            the print stream to use
	 */
	private static void showVersion(PrintStream out) {
		out.println((Klass.SQUAWK_64 ? "64" : "32") + " bit squawk:");
		out.println("    debug code "
				+ (Klass.DEBUG_CODE_ENABLED ? "enabled" : "disabled"));
		out.println("    assertions "
				+ (Klass.ASSERTIONS_ENABLED ? "enabled" : "disabled"));
		out.println("    tracing "
				+ (Klass.TRACING_ENABLED ? "enabled" : "disabled"));
		/* if[FLOATS] */
		out.println("    floating point supported");
		/* else[FLOATS] */
		// out.println("    no floating point support");
		/* end[FLOATS] */

		out.println("    bootstrap suite: ");
		out.print("    ");
		out.println(VM.getCurrentIsolate().getBootstrapSuite()
				.getConfiguration());
		VM.printConfiguration();
	}

	/**
	 * Process a VM command line option.
	 * 
	 * @param arg
	 *            the argument
	 */
	private static void processVMOption(String arg) {
		if (arg.equals("-h")) {
			usage(null);
			VM.stopVM(0);
			/* if[ENABLE_DYNAMIC_CLASSLOADING] */
		} else if (arg.startsWith("-cp:")) {
			// Fix up the class path with respect to the system dependant
			// separator characters
			classPath = ArgsUtilities.toPlatformPath(
					arg.substring("-cp:".length()), true);
			/* end[ENABLE_DYNAMIC_CLASSLOADING] */
		} else if (arg.equals("-egc")) {
			GC.setExcessiveGC(true);
		} else if (arg.equals("-nogc")) {
			VM.allowUserGC(false);
		} else if (arg.startsWith("-isolateinit:")) {
			String initializer = arg.substring(13);
			VM.setIsolateInitializerClassName(initializer);
		} else if (arg.startsWith("-MIDlet-")) {
			try {
				midletPropertyNum = Integer.parseInt(arg.substring("-MIDlet-"
						.length()));
			} catch (NumberFormatException ex) {
				usage("Bad value for -MIDlet- " + arg);
				VM.stopVM(0);
			}
		} else if (arg.equals("-version")) {
			showVersion(System.err);
			VM.stopVM(0);
		} else if (arg.equals("-verbose")) {
			if (!VM.isVerbose()) {
				VM.setVerboseLevel(1);
			}
		} else if (arg.equals("-veryverbose")) {
			if (!VM.isVeryVerbose()) {
				VM.setVerboseLevel(2);
			}
			/* if[DEBUG_CODE_ENABLED] */
		} else if (arg.startsWith("-testMIDlet:")) {
			testMIDletClass = arg.substring(12);
		} else if (arg.equals("-testoms")) {
			testoms = true;
			/* end[DEBUG_CODE_ENABLED] */
		} else if (Klass.TRACING_ENABLED && arg.startsWith("-trace")) {
			if (arg.startsWith("-tracefilter:")) {
				Tracer.setFilter(arg.substring("-tracefilter:".length()));
			} else {
				String feature = arg.substring("-trace".length());
				Tracer.enableFeature(feature);
				if (arg.equals("-traceconverting")) {
					Tracer.enableFeature("loading"); // -traceconverting
														// subsumes
														// -traceloading
				}
			}
		} else if (arg.equals("-stats")) {
			displayExecutionStatistics = true;
		} else if (arg.startsWith("-sampleStatData:")) {
			String url = arg.substring("-sampleStatData:".length());
			try {
				System.out.println("Sending samples of statictics to " + url);
				final DataOutputStream dos = Connector
						.openDataOutputStream(url);
				new Thread(new Runnable() {
					public void run() {
						VM.Stats stats = new VM.Stats();
						while (true) {
							stats.sendStatData(dos);
							try {
								Thread.sleep(500);
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}
					}
				}).start();
			} catch (IOException ex) {
				ex.printStackTrace();
				System.err.println("Ignoring -sampleStatData option");
			}
		} else if (arg.startsWith("-D")) {
			String propAndValue = arg.substring("-D".length());
			int seperator = propAndValue.indexOf('=');
			String prop = propAndValue.substring(0, seperator);
			String val = propAndValue.substring(seperator + 1);
			VM.getCommandLineProperties().put(prop, val);
			// System properties are not "global global"
			/* if[ENABLE_SUITE_LOADING] */
		} else if (arg.startsWith("-suite:")) {
			parentSuiteURI = "file://" + arg.substring(7)
					+ Suite.FILE_EXTENSION;
			/* if[FLASH_MEMORY] */
		} else if (arg.startsWith("-spotsuite:")) {
			parentSuiteURI = arg.substring(1);
			/* end[FLASH_MEMORY] */
		} else if (arg.startsWith("-suitepath:")) {
			String path = arg.substring("-suitepath:".length());
			ObjectMemoryLoader.setFilePath(path);
			/* end[ENABLE_SUITE_LOADING] */
		} else if (!GC.getCollector().processCommandLineOption(arg)) {
			usage("Unrecognised option: " + arg);
			VM.stopVM(0);
		}
	}

	/**
	 * Print a usage message and exit.
	 * 
	 * @param msg
	 *            error message
	 */
	private static void usage(String msg) {
		PrintStream out = System.out;
		out.println();
		if (msg != null) {
			out.println("** " + msg + " **\n");
		}
		out.print("Usage: squawk [-options] class [args...] | [-MIDlet-x]\n"
				+ "\n"
				+ "if there is no class specified, then load MIDlet-1\n"
				+ "where options include:\n"
				+
				/* if[ENABLE_DYNAMIC_CLASSLOADING] */
				"    -cp:<directories and jar/zip files separated by ':' (Unix) or ';' (Windows)>\n"
				+ "                          paths where classes and resources can be found\n"
				+
				/* end[ENABLE_DYNAMIC_CLASSLOADING] */
				/* if[ENABLE_SUITE_LOADING] */
				"    -suite:<name>         suite name (without \""
				+ Suite.FILE_EXTENSION
				+ "\") to load\n"
				+ "    -suitepath:<path>     host path to look for suites in\n"
				+
				/* if[FLASH_MEMORY] */
				"    -spotsuite:<name>     suite name (without \""
				+ Suite.FILE_EXTENSION
				+ "\") to load\n"
				+
				/* end[FLASH_MEMORY] */
				/* end[ENABLE_SUITE_LOADING] */
				"    -isolateinit:<class>  class whose main will be invoked on Isolate start, single arg \"true\" if first Isolate being initialized\n"
				+ "    -MIDlet-x             which MIDlet-x property to use from "
				+ Suite.PROPERTIES_MANIFEST_RESOURCE_NAME
				+ "\n"
				+ "    -sampleStatData:url   poll VM.Stats every 500ms and send samples to url\n"
				+ "    -version              print product version and exit\n"
				+ "    -verbose              report when a class is loaded\n"
				+ "    -veryverbose          report when a class is initialized and various other output\n"
				/* if[DEBUG_CODE_ENABLED] */
				+ "    -testMIDlet:<class>   specify MIDlet class name directly\n"
				+ "    -testoms              continually serialize, deserialize and restart the application if it hibernates itself\n"
		/* end[DEBUG_CODE_ENABLED] */
		);

		if (Klass.TRACING_ENABLED) {
			out.print("    -traceoms             trace object memory serialization\n"
					+ "    -traceswapper         trace endianess swapping\n");
		}

		GC.getCollector().usage(out);
		out.print("    -egc                  enable excessive garbage collection\n"
				+ "    -nogc                 disable application calls to Runtime.gc()\n"
				+ "    -stats                display execution statistics before exiting\n"
				+ "    -D<name>=<value>      set a system property\n"
				+ "    -h                    display this help message\n"
				+ "    -X                    display help on native VM options\n");
		VM.stopVM(0);
	}

}

class AppThread extends Thread {
	String mainClass;
	String[] args;

	AppThread(String mainClass, String[] args) {
		this.mainClass = mainClass;
		this.args = args;
	}

	public void run() {
		Isolate.runMain(mainClass, args);
	}
}
