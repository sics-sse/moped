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
import java.util.*;
import javax.microedition.io.*;

import com.sun.squawk.io.MulticastOutputStream;

import com.sun.squawk.io.mailboxes.Mailbox;
import com.sun.squawk.io.mailboxes.MailboxAddress;

import com.sun.squawk.platform.Platform;

import com.sun.squawk.pragma.*;
import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;

/************************************ WARNING: THIS CLASS CAN'T REQUIRE A STATIC INITIALIZER! ***********************************************/

/**
 * The <code>Isolate</code> class represents a "process-like" unit of computation that is <i>isolated</i> from other instances of  <code>Isolate</code>. The mutable objects of one isolate are
 * logically separate from the mutable objects of another isolate. Similarly, the static variables of one isolate are seperate from the static variables of another isolate.<p>
 *
 * <h3>A Simple Example</h3>
 * The following code example shows the creation of a simple isolate with a single main method argument of "test" and a default context. The created isolate simply prints its arguments array.<p>
 *
 * <pre>
 *  // The creating isolate
 *  String[] args = {"test"};
 *  Isolate i = new Isolate("org.example.App", args, null, Isolate.currentIsolate().getParentSuiteSourceURI());
 *  i.start();
 *  i.join(); // wait for child isolate to finish
 *  System.out.println("Child isolate returned with exit code: " + i.getExitCode());
 *
 *  // The newly created isolate
 *  package org.example;
 *
 *  public class App {
 *      public static void main(String[] args) {
 *          for(int i = 0; i < args.length; i++ )
 *              System.out.println(args[i]);
 *      }
 *  }
 *</pre>
 *
 * Note that the last two arguments to the constructor are a classpath, and a URI, which specify where the isolate's main class can be found. The classpath is used when Squawk is
 * configured to translate classes to the Squawk suite format dynamically, while the URI specifies the suite which contains the translated class file org.example.App. In this example code
 * we specified that the child isolate will use the same suite as the parent Isolate.<p>
 *
 * <h3>Hibernation</h3>
 * An isolate may be suspended in hibernation. Hibernation removes the isolate and all of the isolate's threads from further execution.
 *
 * <h5><i>Optional</i></h5>
 * Once an isolate is hibernated, it can be
 * serialized using the <code>save()</code> method. The saved form of the isolate includes all reachable objects, the state of all static variables, and
 * the current execution context of all of the isolate's threads (the thread stacks, etc). The saved form can be stored in a file, sent over a network, etc.
 * <code>load()</code> can be used reconstruct the saved isolate. Isolate saving and restoring functionality is controlled by the ENABLE_ISOLATE_MIGRATION build property.<p>
 *
 * <h3>Isolate Lifecycle</h3>
 *
 * An Isolate may be in one of several states: NEW, ALIVE, HIBERNATED, and EXITED. The methods {@link Isolate#isNew()}, {@link Isolate#isAlive()}, {@link Isolate#isHibernated()},
 * {@link Isolate#isExited()} can be used to determine an isolate's current state. An Isolate starts out in the NEW state. When the {@link Isolate#start()} method is called the isolate
 * becomes ALIVE. {@link Isolate#hibernate()} causes an isolate to become HIBERNATED, while {@link Isolate#unhibernate()} brings a HIBERNATED back to ALIVE. An ALIVE
 * isolate may become EXITED by calling {@link Isolate#exit(int)}.<p>
 *
 * <h4>Isolate LifecycleNotification</h4>
 * An isolate can register listeners to be notified of changes in an isolate's lifecycle, such as hibernating, unhibernating, or exiting. An isolate can listen for it's own events,
 * or for events on another isolate. To receive notifications of lifecycle events, create a class that implements {@link Isolate.LifecycleListener}, and register the listener using
 * {@link #addLifecycleListener} and one or more of the lifecycle event masks (such as {@link #SHUTDOWN_EVENT_MASK}). When the isolate state changes to the specified event, the system
 * will call the listener's {@link Isolate.LifecycleListener#handleLifecycleListenerEvent} method, passing in the appropriate isolate and event kind.
 *
 * <h3>Inter-Isolate Communication</h3>
 * Isolates may communicate between each other using {@link com.sun.squawk.io.mailboxes.Channel} and {@link com.sun.squawk.io.mailboxes.ServerChannel} instances,
 * or a parent isolate may pass arguments to the main method of the child isolate, or add properties to a child isolate be calling {@link Isolate#setProperty(String, String)}.<p>
 *
 * The properties parameter to the Isolate constructor provides another way to set the system properties of the new isolate. If this parameter is not specified, the child
 * isolate starts with the same set of system properties that it would have if initiated as a standalone application. If this parameter is specified, this default set of
 * properties is augmented with those specified in the parameter, in the same way as if they had been specified with -D on a command line invocation. In addition, all isolate's
 * inherit the properties defined on the Squawk command line using the -D option.
 *
 * <h3>OtherDetails</h3>
 * Each Isolate has independent output streams for {@link System#out} and  {@link System#err}. These output streams can be attached to instances of
 * {@link javax.microedition.io.Connection} by passing Generic Connection Framework URIs to {@link Isolate#addOut(String)} or {@link Isolate#addErr(String)}.<p>
 *
 * Squawk Isolates are loosely based on the Isolates of <a href="http://jcp.org/aboutJava/communityprocess/final/jsr121/index.html">JSR 121 - Application Isolation API<a>, but
 * are not compliant with that specification. In particular, Squawk Isolates support hibernation, and use a different inter-isolate communication mechanism than JSR 121. <p>
 *
 * @see  com.sun.squawk.io.mailboxes.Channel
 * @see  com.sun.squawk.io.mailboxes.ServerChannel
 *
 */
public final class Isolate implements Runnable {

    private final static boolean DEBUG_CODE_ENABLED = false;
    
    public final static boolean ENABLE_MULTI_ISOLATE = true;

    /**
     * Constant denoting that an isolate has been created but not yet {@link #start() started}.
     */
    private final static int NEW = 0;

    /**
     * Constant denoting that an isolate has been {@link #start() started} and the
     * {@link #run()} method has been called on its initial thread.
     */
    private final static int ALIVE = 1;

    /**
     * Constant denoting that an isolate has been {@link #hibernate() hibernated}.
     */
    private final static int HIBERNATED = 2;

    /**
     * Constant denoting that an isolate has been {@link #exit exited}.
     */
    private final static int EXITED = 3;

    /**
     * The name of the wrapper class used to start up midlets.
     */
    final static String MIDLET_WRAPPER_CLASS = "com.sun.squawk.imp.MIDletMainWrapper";



////    /**
////     * List of stack chunks in an isolate {@link #save saved} to a stream. This is
////     * only used by a generational collector that needs to track the thread stacks
////     * in the system.
////     */
////    private Object savedStackChunks;



    /**
     * The debugger agent under which this isolate is being debugged by (if any).
     */
    private Debugger debugger;


    /**
     * The system wide unique identifier for this isolate.
     */
    private final int id;

    /**
     * The name of the class to be executed.
     */
    private final String mainClassName;

    /**
     * The command line arguments for the class to be executed.
     */
    private String[] args;

    /**
     * This is the starting point when doing class look up. It is also
     * the suite into which any dynamically loaded classes are installed if it
     * is not {@link Suite#isClosed closed}.
     */
    private Suite leafSuite;

    /**
     * The immutable bootstrap that is shared across all isolates.
     */
    private final Suite bootstrapSuite;

    /**
     * The child threads of the isolate.
     */
    private SquawkHashtable childThreads = new SquawkHashtable();
    
    /**
     * Flag to show that class Klass has been initialized.
     */
    private boolean classKlassInitialized;

    /**
     * The current state of the isolate.
     */
    private int state;

    /**
     * Isolate exit code.
     */
    private int exitCode;

    /**
     * The source URI of the direct parent suite of the leaf suite.
     */
    private String parentSuiteSourceURI;



      /**
       * The path where class files and suite files can be found.
       */
      private final String classPath;


    /**
     * The channel I/O handle.
     */
    private int channelContext;



////    /**
////     * The hibernated channel context.
////     */
////    private byte[] hibernatedChannelContext;



    /**
     * List of threads ready to run after return from hibernated state.
     */
    private VMThread hibernatedRunThreads;

    /**
     * List of threads to be placed on the timer queue after return from hibernated state.
     */
    private VMThread hibernatedTimerThreads;




////    /**
////     * The GUI input channel.
////     */
////    private int guiIn;
////
////    /**
////     * The GUI output channel.
////     */
////    private int guiOut;


    /**
     * SquawkHashtable that holds the monitors for objects in ROM.
     */
    private SquawkHashtable monitorHashtable = new SquawkHashtable();

    /**
     * The translator that is to be used to locate, load and convert classes
     * that are not currently installed in the system.
     */
    private TranslatorInterface translator;

    /**
     * Pointer to first class state record. These are the structures that store the static field values
     * and initialization state of a class.
     */
    private Object classStateQueue;

    /**
     * The interned strings for the isolate.
     */
    private SquawkHashtable internedStrings;

    /**
     * Properties that can be set by the owner of the isolate.
     */
    private SquawkHashtable properties;
    
    /**
     * Properties read from a JAD file. These will override any manifest properties returned by MIDlet.getAppProperty() or VM.getManifestProperty().
     */
    private SquawkHashtable jadProperties;



////    /**
////     * List of finalizers that need to be run.
////     */
////    private Finalizer finalizers;


    /**
     * The state that we are transitioning to, or NEW if not transitioning
     */
    private int transitioningState;


    /**
     * Table of registered & anonymous mailboxes owned by this isolate.
     * This is a table of all inward links to this isolate.
     */
    private SquawkHashtable mailboxes;

    /**
     * Table of all MailboxAddresses that this Isolate uses to refer to other Isolates.
     * This is a table of all outward links.
     * (Note that an isolate might use mailboxes internally, so some mailboxes
     * referred to by a MailboxAddress may in fact be local to the isolate.)
     */
    private SquawkHashtable mailboxAddresses;

    
    /**
     * Isolate lifecycle callback handlers.
     */
    private CallbackManager shutdownHooks;
    

        /**
     * The parent isolate that created and started this isolate.
     */
    private Isolate parentIsolate;

    /**
     * The child isolates of the isolate.
     */
    private SquawkHashtable childIsolates;
    
    private CallbackManager suspendHooks;
    private CallbackManager resumeHooks;

    /**
     * Registered to run at VM.exit() time.
     */
    private Runnable shutdownHook;

    /**
     * List of threads waiting for the isolate to exit or hibernate.
     */
    private VMThread joiners;

    
    /**
     * name for isolate
     */
    private String name;

    /**
     * Creates the root isolate.
     *
     * @param mainClassName  the name of the class with bootstrap main()
     * @param args           the command line arguments
     * @param suite          the initial leaf suite
     */
    Isolate(String mainClassName, String[] args,  Suite suite) {    
        this.mainClassName        = mainClassName;
        this.args                 = args;
        this.leafSuite            = suite;


        this.classPath            = null;

        this.parentSuiteSourceURI = null;
        this.state                = NEW;
        this.id                   = VM.allocateIsolateID();
        this.name                 = mainClassName;

        while (suite.getParent() != null) {
            suite = suite.getParent();
        }
        this.bootstrapSuite = suite;

        VM.registerIsolate(this);
        Assert.always(VM.getCurrentIsolate() == null, "Isolate.java", 386);
    }
    
    public void morphBootstrapInto(Hashtable properties, String classPath, String parentSuiteSourceURI) {


        if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("Isolate.java", 392); // morphBootstrapInto not suppored in this case...

        this.parentSuiteSourceURI = parentSuiteSourceURI;

        /*
         * Copy in command line properties and passed in properties now.
         * Do this eagerly instead of at getProperty() time to preserve
         * isolate hygene - we need to create copies of "external" strings that are
         * in RAM. We can safely share Strings in ROM though.
         */
        // add in properties from the command line:
        addProperties(VM.getCommandLineProperties());
        // now add in specified properties (may override the command line properties)
        addProperties(properties);

        try {
           updateLeafSuite(true); // TO DO: Also updated in run, but that is too late to find the main class
        } catch (Error e) {
            // note errors releated to loading the suites
            System.err.println("Error morphing " + isolateInfoStr());
            throw e;
        }
    }

    /**
     * Add the properties in the hashtable to this isolate, copying strings
     * when needed to ensure isolate hygiene.
     *
     * @param properties
     */
    private void addProperties(Hashtable properties) {
        if (properties != null) {
            Enumeration e = properties.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = (String) properties.get(key);
                setProperty(key, value);
            }
        }
    }

    /**
     * Creates an new isolate. {@link Isolate#start()} will create a new execution context, and start executing the <code>main</code> method of the class
     * specified by <code>mainClassName</code>. System properties can be set for the isolate by passing in a hashtable where the keys are strings of property names
     * and the values are strings containing property values. The passed in property values will override any property values that the isolate inherits
     * from the command line properties.
     *
     * <p> Note that system properties are disjoint from manifest properties.
     *
     * @param properties    a hashtable of properties to be set in the isolate (may be null)
     * @param mainClassName the name of the class with main()
     * @param args          the command line arguments
     * @param classPath     the path where classes and suites can be found (may be null)
     * @param parentSuiteSourceURI the URI of the suite containing mainClassName. (may be null)
     *
     * @throws NullPointerException if <code>mainClassName</code> or <code>args</code> is <code>null</code>
     * @throws ClassCastException if <code>properties</code> contains keys or values that are not Strings.
     */
    public Isolate(Hashtable properties, String mainClassName, String[] args,  String classPath, String parentSuiteSourceURI) {
    	if (mainClassName == null || args == null) {
            throw new NullPointerException();
        }
        this.mainClassName        = copyIfCurrentThreadIsExternal(mainClassName);
        this.args                 = copyIfCurrentThreadIsExternal(args);


        this.classPath            = copyIfCurrentThreadIsExternal(classPath);

        this.parentSuiteSourceURI = copyIfCurrentThreadIsExternal(parentSuiteSourceURI);
        this.state                = NEW;
        this.id                   = VM.allocateIsolateID();
        this.name                 = this.mainClassName;
        
        Isolate currentIsolate = VM.getCurrentIsolate();
        if (false) Assert.that(currentIsolate != null);

        currentIsolate.addIsolate(this);
        bootstrapSuite = parentIsolate.bootstrapSuite;

////      bootstrapSuite = null;

        /*
         * Copy in command line properties and passed in properties now.
         * Do this eagerly instead of at getProperty() time to preserve
         * isolate hygene - we need to create copies of "external" strings that are
         * in RAM. We can safely share Strings in ROM though.
         */
        // add in properties from the command line:
        addProperties(VM.getCommandLineProperties());
        //System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$ properties from the command line: "+ VM.getCommandLineProperties());
        // now add in specified properties (may override the command line properties)
        addProperties(properties);

        try {
        	/*
        	System.out.println("#########################################################################################");
        	System.out.println("# mainClassName: "+mainClassName);
        	System.out.println("# size of args: " + args.length);
        	System.out.println("# name:" + name);
        	System.out.println("#########################################################################################");
        	*/
           updateLeafSuite(true); // TO DO: Also updated in run, but that is too late to find the main class
        } catch (Error e) {
            // note errors releated to loading the suites
            if (DEBUG_CODE_ENABLED) {
                System.err.println("Error constructing " + isolateInfoStr());
            } else {
                System.err.println("Error constructing suite");
            }
            throw e;
        }
        VM.registerIsolate(this);
    }

    /**
     * Creates an new isolate. {@link Isolate#start()} will create a new execution context, and start executing the <code>main</code> method of the class
     * specified by <code>mainClassName</code>.
     *
     * @param mainClassName the name of the class with main()
     * @param args          the command line arguments
     * @param classPath     the path where classes and suites can be found (may be null)
     * @param parentSuiteSourceURI the URI of the suite containing mainClassName. (may be null)
     *
     * @throws NullPointerException if <code>mainClassName</code> or <code>args</code> is <code>null</code>
     */
    public Isolate(String mainClassName, String[] args,  String classPath, String parentSuiteSourceURI) {
        this(null, mainClassName, args, classPath, parentSuiteSourceURI);
    }

    /**
     * Creates an new isolate. {@link Isolate#start()} will create a new execution context, and start executing the <code>startApp</code> method of the MIDlet
     * specified by the manifest property named <code>MIDlet-</code><i>midletNum</i>. System properties can be set for the isolate by passing in a hashtable where
     * the keys are strings of property names and the values are strings containing property values.  The passed in property values will override any property
     * values that the isolate inherits from the command line properties.
     *
     * <p> Note that system properties are disjoint from manifest properties.
     *
     * @param properties    a hashtable of properties to be set in the isolate (may be null)
     * @param midletNum     the midlet property that contains the name of the MIDlet to instantiate
     * @param classPath     the path where classes and suites can be found (may be null)
     * @param parentSuiteSourceURI the URI of the suite containing the midlet and the MIDlet properties. (may be null)
     *
     * @throws NullPointerException if <code>mainClassName</code> or <code>args</code> is <code>null</code>
     * @throws ClassCastException if <code>properties</code> contains keys or values that are not Strings.
     */
    public Isolate(Hashtable properties, int midletNum, String classPath, String parentSuiteSourceURI) {
        this(properties, MIDLET_WRAPPER_CLASS, new String[0], classPath, parentSuiteSourceURI);
        args = new String[1];
        args[0] = "MIDlet-" + midletNum;
    }

    /**
     * Determines if the current thread is not owned by this isolate.
     *
     * @return true if the current thread is not owned by this isolate
     */
    private boolean isCurrentThreadExternal() {

        if(!VM.isHosted()) {
            VMThread currentThread = VMThread.currentThread();
            if (currentThread != null && currentThread.getThreadNumber() != 0 && currentThread.getIsolate() != this) {
                return true;
            }
        }

        return false;
    }

    /**
     * Makes a copy of a given string if it is not null and the current thread is not owned by this isolate.
     * <p>
     * Make sure that all strings handed outside of <code>this</code> isolate are "hygenic" - either unshared copies of the original,
     * or Strings in ROM that are safe to share.
     *
     * @param s   the string to conditionally copy
     * @return the original or copy of <code>s</code>
     */
    private String copyIfCurrentThreadIsExternal(String s) {
        if (ENABLE_MULTI_ISOLATE && s != null && GC.inRam(s) && isCurrentThreadExternal()) {
            return new String(s);
        } else {
            return s;
        }
    }

    /**
     * Makes a copy of a given string array if it is not null and the current thread is not owned by this isolate.
     *
     * @param arr   the string array to conditionally copy
     * @return the original or copy of <code>arr</code>
     */
    private String[] copyIfCurrentThreadIsExternal(String[] arr) {
        if (ENABLE_MULTI_ISOLATE && arr != null && isCurrentThreadExternal()) {
            String[] result = new String[arr.length];
            for (int i = 0; i != arr.length; ++i) {
                result[i] = copyIfCurrentThreadIsExternal(arr[i]);
            }
            return result;
        }
        return arr;
    }

    /**
     * Gets name of the isolate.
     * By default, this is the mainClassName passed to the constructor, or the MIDlet's class name
     *
     * @return the isolate name
     */
    public String getName() {
        return copyIfCurrentThreadIsExternal(name);
    }

    /**
     * Sets name of the isolate.
     *
     * @param newName (must not be null)
     */
    public void setName(String newName) {
        if (newName == null) {
            throw new IllegalArgumentException();
        }
        name = copyIfCurrentThreadIsExternal(newName);
    }

    /**
     * Gets the class path for the isolate.
     *
     * @return the class path
     */
    public String getClassPath() {

//        return null;

        return copyIfCurrentThreadIsExternal(classPath);

    }

    /**
     * Returns an array of Isolate objects. The array contains one entry for each isolate
     * object that is currently reachable in the system. These isolates may be in the <code>NEW</code>, <code>ALIVE</code>,
     * <code>HIBERNATED</code>, or <code>EXITED</code> states. The system only keeps <code>ALIVE</code> isolates reachable,
     * so isolates in other states may become unreachable unless referenced by an <code>ALIVE</code> isolate.
     * New isolates may have been constructed or existing ones terminated by the time method returns.
     *
     * @return the Isolate objects present at the time of the call
     */
    public static Isolate[] getIsolates() {

        SquawkVector set = new SquawkVector();
        VM.copyIsolatesInto(set);
        Isolate[] isolates = new Isolate[set.size()];
        set.copyInto(isolates);
        return isolates;

////        Isolate[] isolates = new Isolate[1];
////        isolates[0] = VM.getCurrentIsolate();
////        return isolates;

    }

    /**
     * @return the URI of suite from which this isolate was started. This
     *         value will be "memory:bootstrap" if the isolate was not given
     *         an explicit suite at creation time.
     */
    public String getParentSuiteSourceURI() {
        return parentSuiteSourceURI == null ? ObjectMemory.BOOTSTRAP_URI : parentSuiteSourceURI;
    }

    /**
     * Get the name of the main class.
     *
     * @return the name
     */

    @Vm2c(code="return com_sun_squawk_Isolate_mainClassName(this);")

    public String getMainClassName() {
        return copyIfCurrentThreadIsExternal(mainClassName);
    }

    /**
     * Gets the current isolate context.
     *
     * @return the current Isolate context.
     */
    public static Isolate currentIsolate() {
        return VM.getCurrentIsolate();
    }

    /**
     * Determines if this isolate can access trusted classes. A trusted class will call this
     * method in its static constructor.
     *
     * @return boolean
     */
    public boolean isTrusted() {
        // TODO: put authentication infrastructure in place
        return true;
    }

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public String[] getMainClassArguments() {
        return copyIfCurrentThreadIsExternal(args);
    }

    /**
     * Gets the bootstrap suite.
     *
     * @return the bootstrap suite
     */
    Suite getBootstrapSuite() {
        return bootstrapSuite;
    }

    /**
     * Gets the suite that is the starting point for class lookup in this isolate.
     * If it is not {@link Suite#isClosed closed}, then it's also the suite into which
     * any dynamically loaded classes (i.e. those loaded via {@link Class#forName(String)})
     * are installed.
     *
     * @return the leaf suite
     */
    public Suite getLeafSuite() {
        return leafSuite;
    }

    /**
     * Gets the monitor hash table for the isolate
     *
     * The monitorHashtable holds the monitors for objects that are in ROM.
     *
     * @return the hash table
     */
    SquawkHashtable getMonitorHashtable() {
        return monitorHashtable;
    }

    /**
     * Sets the translator.
     *
     * @param translator the translator.
     */
    void setTranslator(TranslatorInterface translator) throws HostedPragma {
        this.translator = translator;
    }

    /**
     * Gets a translator that is to be used to locate, load and convert
     * classes that are not currently installed in this isolate's runtime
     * environment.
     *
     * @return  a translator for installing new classes or null if the system does not support dynamic class loading
     */
    public static TranslatorInterface getDefaultTranslator() throws AllowInlinedPragma {

//        return null;

          String translatorSuiteUrl = System.getProperty("com.sun.squawk.Isolate.getDefaultTranslator");
          if (translatorSuiteUrl == null) {
              translatorSuiteUrl = "file://translator.suite";
          }
          Suite tsuite = Suite.getSuite(translatorSuiteUrl, false);
          if (tsuite != null) {
              Klass klass = tsuite.lookup("com.sun.squawk.translator.Translator");
              if (klass != null) {
                  return (TranslatorInterface)klass.newInstance();
              }
          }
        return null;

    }

    /**
     * Gets a translator that is to be used to locate, load and convert
     * classes that are not currently installed in this isolate's runtime
     * environment.
     *
     * @return  a translator for installing new classes or null if the system does not support dynamic class loading
     */
    public TranslatorInterface getTranslator() throws AllowInlinedPragma {
        if (VM.isHosted()) {
            return translator;
        }

//        return null;

      /*
       * Create the translator instance reflectively. This (compile and runtime) dynamic
       * binding to the translator means that it can be an optional component.
       */
      Klass klass = leafSuite.lookup("com.sun.squawk.translator.Translator");
      if (klass == null) {
          return getDefaultTranslator();
      }
      return (TranslatorInterface)klass.newInstance();

    }

    /**
     * Adds a named JAD property to this isolate.
     * 
     * This will not replace existing properties stored as JAD properties.
     *
     * @param key    the name of the property
     * @param value  the value of the property
     */
    public void setJADProperty(String key, String value) {
        if (jadProperties == null) {
            jadProperties = new SquawkHashtable();
        }

        if (value == null) {
            throw new IllegalArgumentException();
        }

        if (jadProperties.get(key) == null) {
            key = copyIfCurrentThreadIsExternal(key);
            value = copyIfCurrentThreadIsExternal(value);
            jadProperties.put(key, value);
        }
    }

    /**
     * Gets a named JAD property of this isolate.
     *
     * @param key  the name of the property to get
     * @return the value of the property named by 'key' or null if there is no such property
     */
    public String getJADProperty(String key) {
        if (jadProperties == null) {
            return null;
        }

        return copyIfCurrentThreadIsExternal((String)jadProperties.get(key));
    }

    /**
     * Add the properties in the hashtable to this JAD properties of this isolate, copying strings
     * when needed to ensure isolate hygiene.
     * 
     * This will not replace existing properties stored as JAD properties.
     *
     * @param properties
     */
    public void addJADProperties(Hashtable properties) {
        if (properties != null) {
            Enumeration e = properties.keys();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = (String) properties.get(key);
                setJADProperty(key, value);
            }
        }
    }
    
    /**
     * Adds a named property to this isolate. These properties are included in the
     * look up performed by {@link System#getProperty}.
     *
     * @param key    the name of the property
     * @param value  the value of the property or null to remove the property
     */
    public void setProperty(String key, String value) {
        if (properties == null) {
            properties = new SquawkHashtable();
        }

        key = copyIfCurrentThreadIsExternal(key);
        if(value == null){
        	properties.remove(key);
        }else{
        	value = copyIfCurrentThreadIsExternal(value);
        	properties.put(key, value);
        }
    }

    /**
     * Gets a named property of this isolate.
     * <p>
     * Isolate properties include those passed into the isolate's constructor,
     * properties inhertited from the squawk command line, and properties set
     * by {@link Isolate#setProperty}.
     *
     * @param key  the name of the property to get
     * @return the value of the property named by 'key' or null if there is no such property
     */
    public String getProperty(String key) {
        if (properties == null) {
            return null;
        }

        return copyIfCurrentThreadIsExternal((String)properties.get(key));
    }

    /**
     * Enumeration wrapper over an isolate's property keys, that handles
     * copying strings when needed (copyIfCurrentThreadIsExternal).
     */
    static class PropEnumeration implements Enumeration {
        private Enumeration realEnum;
        private Isolate iso;

        PropEnumeration(Isolate iso) {
            this.iso = iso;
            this.realEnum = iso.properties.keys();
        }

        public boolean hasMoreElements() {
            return realEnum.hasMoreElements();
        }

        public Object nextElement() {
            return iso.copyIfCurrentThreadIsExternal((String)realEnum.nextElement());
        }

    }

    /**
     * Get an enumeration of isolate property keys. These keys can be used with {@link Isolate#getProperty(String)} to get the
     * property values.
     * <p>
     * Isolate properties include those passed into the isolate's constructor,
     * properties inhertited from the squawk command line, and properties set
     * by {@link Isolate#setProperty}.
     *
     * @return enumeration of property keys
     */
    public Enumeration getProperties() {
        return new PropEnumeration(this);
    }

    /*---------------------------------------------------------------------------*\
     *                        Isolate life-cycle support                         *
    \*---------------------------------------------------------------------------*/

    /**
     * Event kind indicating that an isolate is exiting.
     *
     * Used for {@link Isolate.LifecycleListener}s that will be called when the Isolate terminates via
     * {@link #exit}, {@link com.sun.squawk.VM#stopVM}, or when the last non-daemon thread in this isolate ends.
     * All other ways to terminate an isolate,
     * including {@link com.sun.squawk.VM#haltVM} do not cause the shutdown hooks to be run.
     */
    public final static int SHUTDOWN_EVENT_MASK = 1;

    /**
     * Event kind indicating that an isolate is hibernating.
     *
     * Used for {@link Isolate.LifecycleListener}s that will be called when the Isolate suspends via
     * {@link #hibernate}.
     */
    public final static int HIBERNATE_EVENT_MASK = 2;

    /**
     * Event kind indicating that an isolate is unhibernating.
     *
     * Used for {@link Isolate.LifecycleListener}s that will be called when the Isolate resumes via
     * {@link #unhibernate}.
     */
    public final static int UNHIBERNATE_EVENT_MASK = 4;


    public final static int SUPPORTED_EVENTS = SHUTDOWN_EVENT_MASK | HIBERNATE_EVENT_MASK | UNHIBERNATE_EVENT_MASK;

////  public final static int SUPPORTED_EVENTS = SHUTDOWN_EVENT_MASK;

    
    /**
     * Monitor isolate lifecycle events such as shutdown, hibernate, and unhibernate. Isolate life-cycle events can be
     * monitored by implementing LifecycleListener, and registering it with the isolate using
     * {@link Isolate#addLifecycleListener}, specifying the kind of event to monitor.<p>
     *
     * An LifecycleListener can be registered for more than one event kind.
     */
    public interface LifecycleListener {

        /**
         * This method will be called when the lifecycle event occurs on the isolate that this listener
         * was registered for using <code>addLifecycleListener</code>.
         *
         * @param iso the isolate that had the lifecycle event
         * @param eventKind the lifecycle event that occurred.
         *                  One of {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}
         *
         * @see Isolate#addLifecycleListener
         * @see #exit
         * @see #hibernate
         * @see #unhibernate
         */
        public void handleLifecycleListenerEvent(Isolate iso, int eventKind);
    }

    private CallbackManager getCallbackManager(int eventKind) {
        switch (eventKind) {
            case SHUTDOWN_EVENT_MASK: {
                if (shutdownHooks == null) {
                    if (ENABLE_MULTI_ISOLATE) {
                        shutdownHooks = new CallbackManager(true);
                    } else {
                        shutdownHooks = VM.shutdownHooks; // just one level of shutdown...
                    }
                }
                return shutdownHooks;
            }

            case HIBERNATE_EVENT_MASK: {
                if (suspendHooks == null) {
                    suspendHooks = new CallbackManager(false);
                }
                return suspendHooks;
            }
            case UNHIBERNATE_EVENT_MASK: {
                if (resumeHooks == null) {
                    resumeHooks = new CallbackManager(false);
                }
                return resumeHooks;
            }

            default:
                throw new IllegalArgumentException();
        }
    }


    /**
     * A LocalListenerWrapper simply calls the wrapped listener's handleLifecycleListenerEvent method with the correct arguments.
     */
    static class LocalListenerWrapper implements HookWrapper {
        final LifecycleListener listener;
        final int eventKind;

        /**
         * Create a wrapper for a listener that is intended to run in connection with events in THIS isolates.
         *
         * @param listener the listener to be run based on events in this isolate
         * @param eventKind the event that this listener is registered for.
         */
        LocalListenerWrapper(LifecycleListener listener, int eventKind) {
            this.listener = listener;
            this.eventKind = eventKind;
        }

        /**
         * Called by system, will call the wrapped listener when the event occurs.
         */
        public void run() {
            listener.handleLifecycleListenerEvent(Isolate.currentIsolate(), eventKind);
        }

        /**
         * Return the wrapped listener.
         */
        public Object getWrappedHook() {
            return listener;
        }
    } /* LocalListenerWrapper */


    /**
     * A RemoteListenerWrapper is used to wrap a listener that is supposed to run due to events in another isolate. The listener will run in
     * the isolate that created the RemoteHookWrapper. Remote listener need to ensure that they are removed if the creating isolate exits before
     * the remote isolate does. The RemoteHookWrapper helps ensure that this happens by creating a "cleanup hook", and registering the
     * cleanup hook in the local isolate.
     *
     * RemoteListenerWrapper objects should not be reused for different event types.
     */
    static final class RemoteListenerWrapper extends LocalListenerWrapper {
        private final Runnable cleanupHook;
        private final Isolate local;
        private final Isolate remote;

        final class RemoteListenerCleanupHook implements Runnable {
            /**
             * This cleanup hook will remove the user's listener (actually the wrapper around it) from the remote isolate.
             */
            public void run() {
                RemoteListenerWrapper thisWrapper = RemoteListenerWrapper.this; // the wrapper that encloses this anon Runnable
                CallbackManager cbm = thisWrapper.remote.getCallbackManager(thisWrapper.eventKind);
                cbm.remove(thisWrapper);
            }
        }

        /**
         * Create a wrapper for a hook that is intended to run in connection with events in other isolates,
         * but will run in the context of the current isolate. The RemoteHookWrapper will ensure that
         * the hook in the remote isolate will be removed if the local isolate exits first.
         *
         * RemoteListenerWrapper should not be shared
         *
         * @param remote the isolate that is being monitored
         * @param listener the listener to be run based on events in remoteIsolate
         * @param eventKind the event that this listener is registered for.
         */
        RemoteListenerWrapper(Isolate remote, LifecycleListener listener, int eventKind) {
            super(listener, eventKind);
            this.local = Isolate.currentIsolate();
            this.remote = remote;
            this.cleanupHook = new RemoteListenerCleanupHook();
            if (false) Assert.that(local != remote); // RemoteListenerWrapper is only needed for remote isolates.
        }

        /**
         * Called by system, will call the listener when the event occurs on the remote isolate.
         */
        public void run() {
            listener.handleLifecycleListenerEvent(remote, eventKind);
            if (eventKind == SHUTDOWN_EVENT_MASK) {
                // we've done our bit, can deregister now.
                local.getCallbackManager(SHUTDOWN_EVENT_MASK).remove(getCleanupHook());
            }
        }

        /**
         * Return the cleanup hook.
         */
        Runnable getCleanupHook() {
            return cleanupHook;
        }

    } /* RemoteListenerWrapper */

    /**
     * Add a listener to be run when this isolate terminates, hibernates, or unhibernates, depending on <code>evenSet</code>. <p>
     *
     * The listener may listen to multiple events by using bitwise OR to construct a set of events from the various event masks.<p>
     *
     * This isolate may be the current isolate (the local case) or another isolate (the remote case).
     * If this isolate is remote, then this method will also add a listener to the local isolate that will
     * remove this listener on the remote isolate. This cleans up the listeners if
     * the local isolate exits before the remote isolate does.<p>
     *
     * <b>Execution:</b><p>
     * The listener will run in the current Isolate's context.
     * All listener from the same Isolate may run in the same thread. Any RuntimeExceptions thrown
     * by the listener will be printed to System.err, but are otherwise ignored.
     *
     * @param listener a LifecycleListener that will be called when event occurs.
     * @param eventSet a set of lifecycle events to be monitored.
     *                  One or more of {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}.
     * @throws IllegalArgumentException when eventSet does not contain {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}
     *
     * @see #removeLifecycleListener
     * @see #exit
     * @see #hibernate
     * @see #unhibernate
     */
    public void addLifecycleListener(LifecycleListener listener, int eventSet) {
        if ((eventSet & SUPPORTED_EVENTS) == 0) {
            throw new IllegalArgumentException();
        }

        if ((eventSet & SHUTDOWN_EVENT_MASK) != 0) {
            addLifecycleListener0(listener, SHUTDOWN_EVENT_MASK);
        }

        if (ENABLE_MULTI_ISOLATE) {
            if ((eventSet & HIBERNATE_EVENT_MASK) != 0) {
                addLifecycleListener0(listener, HIBERNATE_EVENT_MASK);
            }
            if ((eventSet & UNHIBERNATE_EVENT_MASK) != 0) {
                addLifecycleListener0(listener, UNHIBERNATE_EVENT_MASK);
            }
        }
    }

    /**
     * Add a listener to be run when one particular event occurs. <p>
     *
     * @param listener a LifecycleListener that will be called when event occurs.
     * @param eventKind the lifecycle event to be monitored.
     *                  One of {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}.
     * @throws IllegalArgumentException when eventKind is not one of {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}
     */
    private void addLifecycleListener0(LifecycleListener listener, int eventKind) {
        CallbackManager cbm = getCallbackManager(eventKind);
        Isolate currentIsolate = Isolate.currentIsolate();


        if (this == currentIsolate) { // local
            // add (wrapper) hook to local isolate
            cbm.add(currentIsolate, new LocalListenerWrapper(listener, eventKind));
        } else {

            RemoteListenerWrapper rlw = new RemoteListenerWrapper(this, listener, eventKind);
            // add cleanup hook in current isolate
            currentIsolate.getCallbackManager(SHUTDOWN_EVENT_MASK).add(currentIsolate, rlw.getCleanupHook());

            // add (wrapper) hook to remote isolate
            cbm.add(currentIsolate, rlw);

////          throw new IllegalStateException();

        }
    }

    /**
     * Remove an <code>Isolate.LifecycleListener</code> from this isolate. Must be called from the same isolate that added the listener.
     *
     * @param listener a Isolate.LifecycleListener to be removed.
     * @param eventSet a set of lifecycle events that the listener should stop listening to.
     *                  One or more of {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}.
     * @return true if the listener was registered with the all of the events in the eventSet on this isolate; false otherwise.
     * @throws IllegalArgumentException when eventSet does not contain {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}
     *
     * @see #addLifecycleListener
     */
    public boolean removeLifecycleListener(LifecycleListener listener, int eventSet) {
        if ((eventSet & SUPPORTED_EVENTS) == 0) {
            throw new IllegalArgumentException();
        }

        boolean result = true;
        if ((eventSet & SHUTDOWN_EVENT_MASK) != 0) {
            result &= removeLifecycleListener0(listener, SHUTDOWN_EVENT_MASK);
        }

        if (ENABLE_MULTI_ISOLATE) {
            if ((eventSet & HIBERNATE_EVENT_MASK) != 0) {
                result &= removeLifecycleListener0(listener, HIBERNATE_EVENT_MASK);
            }
            if ((eventSet & UNHIBERNATE_EVENT_MASK) != 0) {
                result &= removeLifecycleListener0(listener, UNHIBERNATE_EVENT_MASK);
            }
        }
        return result;
    }

    /**
     * Remove an <code>Isolate.LifecycleListener</code> from this isolate. Must be called from the same isolate that added the listener.
     *
     * @param listener a Isolate.LifecycleListener to be removed.
     * @param eventKind the lifecycle event to be monitored.
     *                  One of {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}.
     * @return true if the listener was registered with the given eventKind on this isolate; false otherwise.
     * @throws IllegalArgumentException when eventKind is not one of {@link #SHUTDOWN_EVENT_MASK}, {@link #HIBERNATE_EVENT_MASK}, or {@link #UNHIBERNATE_EVENT_MASK}
     *
     */
    private boolean removeLifecycleListener0(LifecycleListener listener, int eventKind) {
        CallbackManager cbm = getCallbackManager(eventKind);
        Isolate currentIsolate = Isolate.currentIsolate();
        HookWrapper hw = cbm.findHookWrapper(currentIsolate, listener);
        if (hw == null) {
            return false;
        }

        if (this == currentIsolate) { // local
            // remove (wrapper) hook in this Isolate
            if (false) Assert.that(!(hw instanceof RemoteListenerWrapper));
            return cbm.remove(hw);
        } else if (hw instanceof RemoteListenerWrapper) {
            RemoteListenerWrapper rlw = (RemoteListenerWrapper)hw;
            // remove the cleanup hook in current Isolate
            currentIsolate.getCallbackManager(SHUTDOWN_EVENT_MASK).remove(rlw.getCleanupHook());

            // remove remote hook in this Isolate
            return cbm.remove(rlw);
        } 

////        if (this == currentIsolate) { // local
////            // remove (wrapper) hook in this Isolate
////            return cbm.remove(hw);
////        }


        throw !Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : Assert.shouldNotReachHere();
    }


    /*---------------------------------------------------------------------------*\
     *                           Class state management                          *
    \*---------------------------------------------------------------------------*/

    /**
     * Get a class state.
     *
     * @param klass the class of the variable
     * @return the class state object or null if none exists
     */
    Object getClassState(Klass klass) {
        VM.extendsEnabled = false; //------------------------ NO CALL ZONE ---------------------------

        Object first = classStateQueue;

        if (first == null) {
            VM.extendsEnabled = true; //----------------------------------------------------------------------
            Assert.always(!classKlassInitialized, "Isolate.java", 1282); // assert goes AFTER extendsEnabled = true
            return null;
        } else {
            Object res = null;
            
            /*
             * Do quick test for class state at the head of the queue.
             */
            if (NativeUnsafe.getObject(first, CS.klass) == klass) {
                res = first;
            } else {
                /*
                 * Start searching.
                 */
                Object last = first;
                Object ks = NativeUnsafe.getObject(first, CS.next);
                while (ks != null) {
                    if (NativeUnsafe.getObject(ks, CS.klass) == klass) {
                        // if (false) Assert.that(last != null); can't call assert in NO CALL ZONE
                        /*
                         * Move to head of queue.
                         */
                        Object ksnext = NativeUnsafe.getObject(ks, CS.next);
                        NativeUnsafe.setObject(last, CS.next, ksnext);
                        NativeUnsafe.setObject(ks, CS.next, first);
                        classStateQueue = ks;
                        res = ks;
                        break;
                    }
                    last = ks;
                    ks = NativeUnsafe.getObject(ks, CS.next);
                }
            }
            VM.extendsEnabled = true; //----------------------------------------------------------------------

            if (res != null) {
                CS.check(res);
                CS.check(classStateQueue);
                VM.addToClassStateCache(klass, res);
            }

            return res;
        }
    }

    /**
     * Get a class state.
     *
     * @param klass the class of the variable
     * @return the class state object or null if none exists
     */
    Object getClassStateForInterpreter(Klass klass) throws NotInlinedPragma, HostedPragma {
        Object first = classStateQueue;

        if (first == null) {
            return null;
        }

        /*
         * Do quick test for class state at the head of the queue.
         */
        if (NativeUnsafe.getObject(first, CS.klass) == klass) {
            VM.addToClassStateCache(klass, first);
            return first;
        } else {
            /*
             * Start searching.
             */
            Object last = first;
            Object ks = NativeUnsafe.getObject(first, CS.next);
            while (ks != null) {
                Object ksnext = NativeUnsafe.getObject(ks, CS.next);
                if (NativeUnsafe.getObject(ks, CS.klass) == klass) {
                    if (false) Assert.that(last != null);
                    /*
                     * Move to head of queue.
                     */
                    NativeUnsafe.setObject(last, CS.next, ksnext);
                    NativeUnsafe.setObject(ks, CS.next, first);
                    classStateQueue = ks;
                    VM.addToClassStateCache(klass, ks);
                    return ks;
                }
                last = ks;
                ks = ksnext;
            }
        }

        return null;
    }

    /**
     * Add a class state to the system.
     *
     * @param ks the class state to add
     */
    void addClassState(Object ks) {
        CS.check(ks);
        VM.extendsEnabled = false; //------------------------ NO CALL ZONE ---------------------------
        Object first = classStateQueue;
        NativeUnsafe.setObject(ks, CS.next, first);
        classStateQueue = ks;
        VM.extendsEnabled = true; //----------------------------------------------------------------------
    }

    /**
     * Get a class state in order to access a static variable.
     *
     * @param klass the class of the variable
     * @param offset the offset to the variable
     * @return the class state object
     */
    Object getClassStateForStaticVariableAccess(Klass klass, int offset) {
        /*
         * Lookup the class state in the isolate.
         */
        Object ks = getClassState(klass);

        /*
         * If the class state was not found in the list, then the class
         * is either not initialized, has suffered an initialization
         * failure, or is in the process of being initialized. In either
         * case calling initializeInternal() will either yield a pointer to the
         * class state object or result in an exception being thrown.
         */
        if (ks == null) {
            if (false) Assert.that(klass.getSystemID() != CID.KLASS);
            ks = klass.initializeInternal();
        }

        if (false) Assert.that(ks != null);
        if (false) Assert.that(offset >= CS.firstVariable);
        if (false) Assert.that(offset < GC.getArrayLength(ks));
        return ks;
    }


    /*---------------------------------------------------------------------------*\
     *                           String interning                                *
    \*---------------------------------------------------------------------------*/

    /**
     * Returns a canonical representation for the string object from the current isolate.
     *
     * @param value
     * @return  a string that has the same contents as this string, but is
     *          guaranteed to be from a pool of unique strings.
     *
     * @see #intern
     */
    private String intern0(String value) {
        if (value == null) {
            return null;
        }
        if (false) Assert.that(VM.isHosted() || (this == VM.getCurrentIsolate() && this.isAlive()));
        if (!GC.inRam(value)) {
            return value;
        }
        if (internedStrings == null) {
            internedStrings = new SquawkHashtable();
        }
        String internedString = (String) internedStrings.get(value);
        if (internedString == null) {
            if (!VM.isHosted()) {
                internedString = GC.findInRomString(value); // depends on current isolate
            }
            if (internedString == null) {
                internedString = value;
            }
            internedStrings.put(internedString, internedString);
        }
        return internedString;
    }

    /**
     * Returns a canonical representation for the string object from the current isolate.
     * <p>
     * A pool of strings, initially empty, is maintained privately by the
     * class <code>Isolate</code>.
     * <p>
     * When the intern method is invoked, if the pool already contains a
     * string equal to this <code>String</code> object as determined by
     * the {@link #equals(Object)} method, then the string from the pool is
     * returned. Otherwise, this <code>String</code> object is added to the
     * pool and a reference to this <code>String</code> object is returned.
     * <p>
     * It follows that for any two strings <code>s</code> and <code>t</code>,
     * <code>s.intern() == t.intern()</code> is <code>true</code>
     * if and only if <code>s.equals(t)</code> is <code>true</code>.
     * <p>
     * All literal strings and string-valued constant expressions are
     * interned. String literals are defined in &sect;3.10.5 of the
     * <a href="http://java.sun.com/docs/books/jls/html/">Java Language
     * Specification</a>
     *
     * @param value
     * @return  a string that has the same contents as this string, but is
     *          guaranteed to be from a pool of unique strings.
     */
    public static String intern(String value) {
        return VM.getCurrentIsolate().intern0(value);
    }

    /*---------------------------------------------------------------------------*\
     *                            Isolate Execution                              *
    \*---------------------------------------------------------------------------*/

    /**
     * Start the primordial isolate.
     */
    void primitiveThreadStart() {
        VMThread.asVMThread(new CrossIsolateThread(this, "primitive-thread")).primitiveThreadStart();
    }

    /**
     * Start the isolate running.
     *
     * @throws IllegalStateException if the isolate has already been started
     */
    public void start() {
        transitioningState = ALIVE;
        String isoname = new StringBuffer(mainClassName).append(" - main").toString();
        Thread t = new CrossIsolateThread(this, isoname);
        t.start();
    }

    /**
     * Manually initialize com.sun.squawk.Klass.
     */
    void initializeClassKlass() {
        if (!classKlassInitialized) {
            Klass klassKlass       = bootstrapSuite.getKlass(CID.KLASS);
            Klass klassGlobalArray = bootstrapSuite.getKlass(CID.GLOBAL_ARRAY);
            Object cs = GC.newClassState(klassKlass, klassGlobalArray);
            addClassState(cs);
            klassKlass.clinit();
            classKlassInitialized = true;
        }
    }

    /**
     * Get the Channel I/O handle.
     *
     * @return the I/O handle
     */
    int getChannelContext() {
        if (channelContext == 0) {

            if (Platform.IS_DELEGATING || Platform.IS_SOCKET) {
                channelContext = VM.createChannelContext(null);
            } else {
                channelContext = ChannelConstants.CHANNEL_GENERIC;
            }

////          channelContext = VM.createChannelContext(hibernatedChannelContext);
////          hibernatedChannelContext = null;

        }
        return channelContext;
    }



////    /**
////     * Get the GUI input channel.
////     *
////     * @return the I/O handle
////     */
////    int getGuiInputChannel() throws IOException {
////        if (guiIn == 0) {
////            guiIn = VM.getChannel(ChannelConstants.CHANNEL_GUIIN);
////        }
////        return guiIn;
////    }
////
////    /**
////     * Get the GUI output channel.
////     *
////     * @return the I/O handle
////     */
////    int getGuiOutputChannel() throws IOException {
////        if (guiOut == 0) {
////            guiOut = VM.getChanneupdateLeafSuite(true)l(ChannelConstants.CHANNEL_GUIOUT);
////            VM.execGraphicsIO(ChannelConstants.SETWINDOWNAME, 0, 0, 0, 0, 0, 0, mainClassName, null);
////        }
////        return guiOut;
////    }


    /**
     * Updates the leaf suite if this isolate was initialized with a non-null
     * parent suite URI or class path.
     */
    private void updateLeafSuite(boolean prepass) {
        if (!prepass) {
            if (false) Assert.that(VM.getCurrentIsolate() == this);
        }


//        if (parentSuiteSourceURI != null) {
//            Suite parent = Suite.getSuite(parentSuiteSourceURI);
//            Assert.that(parent != null);
//            leafSuite = parent;
//        } else {
//            leafSuite = bootstrapSuite;
//        }

////      leafSuite = bootstrapSuite; // only bootrsap suite is supported

//

          if (parentSuiteSourceURI != null || classPath != null) {       
              Suite parent = (parentSuiteSourceURI == null ? bootstrapSuite : Suite.getSuite(parentSuiteSourceURI));
              if (false) Assert.that(parent != null);
  
              // Don't create a suite for loading new classes if the class path is null
              if (classPath == null) {
                  leafSuite = parent;
              } else {
                  String leafSuiteName = getProperty("leaf.suite.name");
                  if (leafSuiteName == null) {
                      leafSuiteName = "-leaf" + VM.getNextHashcode() + "-";
                  }
                  leafSuite = new Suite(leafSuiteName, parent, Suite.EXTENDABLE_LIBRARY);
              }
          } else {
              leafSuite = bootstrapSuite;
          }

    }


    /**
     * When isolate starts or unhibernates, call this to add the VM shutdown hook.
     */
    private void addVMShutdownHook() {
        if (false) Assert.that(this.shutdownHook == null);
        shutdownHook = new Runnable() {
            public void run() {
                runShutdownListeners();
            }
        };

        VM.addShutdownHook(this, shutdownHook);
    }

    /**
     * When Isolate terminates or hibernates, call this to remove the VM shutdown hook.
     */
    private void removeVMShutdownHook() {
        if (shutdownHook != null) {
            VM.removeShutdownHook(this, shutdownHook);
            shutdownHook = null;
        }
    }

    
    /**
     * Starts running this isolate.
     *
     * @deprecated This is called by the system (in {@link Isolate#start}}, and shouldn't be called directly
     * @throws IllegalStateException if this isolate has already been started
     */
    public final void run() throws IllegalStateException {
        // Check and set the class state.
        if (state != NEW) {
            throw new IllegalStateException();
        }
        if (VMThread.currentThread().getIsolate() != this) {
            throw new IllegalStateException();
        }

        changeState(ALIVE);
        transitioningState = NEW; // not in transition

        // Manually initialize com.sun.squawk.Klass.
        initializeClassKlass();

        // Update the leaf suite
        updateLeafSuite(false);

        // It is important that java.lang.System is initialized before com.sun.cldc.i18n.Helper
        // so initialized it now.
        System.currentTimeMillis();

        String initializerClassName = VM.getIsolateInitializerClassName();

        // Verbose trace.
        if (DEBUG_CODE_ENABLED && VM.isVeryVerbose()) {
            System.out.print("[Starting " + isolateInfoStr() + " with args");
            if (args != null) {
                for (int i = 0; i != args.length; ++i) {
                    System.out.print(" " + args[i]);
                }
            }

            if (initializerClassName != null) {
                System.out.print(" will invoke specified initializer " + initializerClassName);
            }

            System.out.println("]");
        }


        addVMShutdownHook();

        
        // Invoke the main of the specified Isolate initializer specified on command line as -isolateinit:
        if (initializerClassName != null) {
            Klass klass = null;
            try {
                klass = Klass.forName(initializerClassName);
            } catch (ClassNotFoundException e) {
                System.err.println("initializerClassName " + initializerClassName + ": " + e);
                exit(998);
            }
            boolean wasFirstInitialized = VM.isFirstIsolateInitialized();
            if (!wasFirstInitialized) {
                VM.setFirstIsolateInitialized(true);
            }
            klass.main(new String[] {wasFirstInitialized?"false":"true"});
        }


        // Notify debugger of event:
        if (debugger != null && !isMidlet()) {
            debugger.notifyEvent(new Debugger.Event(Debugger.Event.VM_INIT, VMThread.currentThread()));

            // This gives the debugger a chance to receive the THREAD_START event for the
            // initial thread in an isolate
//            debugger.notifyEvent(new Debugger.Event(Debugger.Event.THREAD_START, Thread.currentThread()));
        }


        runMain(mainClassName, args);
    }
    
    /** 
     * Find the main class and call it's main().
     * 
     * @param mainClassName
     * @param args 
     */
    static void runMain(String mainClassName, String[] args) {
        try {
            Klass klass = Klass.forName(mainClassName);
            klass.main(args);
            System.out.flush();
            System.err.flush();
        } catch (ClassNotFoundException ex) {
            System.err.println("No main class " + mainClassName + ": " + ex);
            VM.getCurrentIsolate().exit(999);
        }
    }


    /**
     * Waits for all the other threads and child isolates belonging to this isolate to stop.
     *
     * WARNING: Only one thread can join an isolate, because this method clears the childIsolates list
     */
    public void join() {

        /*
         * If this isolate is has not yet been started or is still alive then wait until it has exited or been hibernated
         */
        if (state <= ALIVE) {
            VMThread.isolateJoin(this);
        }

        /*
         * Join all the child isolates.
         */
        if (childIsolates != null) {
            for (Enumeration e = childIsolates.elements() ; e.hasMoreElements() ;) {
                Isolate isolate = (Isolate)e.nextElement();
                // if a child isolate is not alive, or has not been started, then it never will, so don't wait for it.
                if (state == ALIVE || (state == NEW && transitioningState == ALIVE)) {
                    isolate.join();
                }
            }
        }

        /*
         * Eliminate child isolates from the isolate object graph.
         */
        childIsolates = null;
    }

    /**
     * Adds a child isolate to this isolate.
     *
     * @param childIsolate  the child isolate
     */
    void addIsolate(Isolate childIsolate) {
        if (false) Assert.that(childIsolate.parentIsolate == null && childIsolate != this);
        childIsolate.parentIsolate = this;

        if (childIsolates == null) {
            childIsolates = new SquawkHashtable();
        }
        if (false) Assert.that(!childIsolates.containsKey(childIsolate));
        childIsolates.put(childIsolate, childIsolate);
    }


    /**
     * Add a thread to the isolate.
     *
     * @param thread the thread
     */
    void addThread(VMThread thread) {
        if (false) Assert.that(!childThreads.containsKey(thread));
        childThreads.put(thread, thread);
    }

    /**
     * Remove a thread from the isolate.
     * if <code>thread</code> was the last non-daemon thread in the system,  this will return true,
     * indicating that the caller should exit the isolate.
     *
     * @param thread the thread
     * @return true if the isolate should exit.
     */
    boolean removeThread(VMThread thread) {
        if (false) Assert.that(childThreads.containsKey(thread));
        childThreads.remove(thread);

        /*
         * Check for rundown condition. That is, keep running the isolate
         * if at least one non-daemon threading is still running.
         */
        if (thread.isDaemon()) {
            return false; // exiting a daemon thread can't cause isolate to exit
        }

        for (Enumeration e = childThreads.elements(); e.hasMoreElements(); ) {
            thread = (VMThread)e.nextElement();
            if (!thread.isDaemon()) {
                return false;
            }
        }

        /*
         * If all the non-daemon threads are dead then stop the isolate.
         */
        return true;
    }

    /**
     * Test to see if class Klass is initialized.
     *
     * @return true if it is
     */
    public boolean isClassKlassInitialized() {
        return classKlassInitialized;
    }


    private void runHooks(CallbackManager cbm, String label) {
        if (DEBUG_CODE_ENABLED && VM.isVerbose()) {
            System.out.print("Running isolate");
            System.out.print(label);
            System.out.print(" hooks for ");
            System.out.println(this);
        }
        cbm.runHooks();
        if (DEBUG_CODE_ENABLED && VM.isVerbose()) {
            System.out.print("Done with isolate");
            System.out.print(label);
            System.out.print(" hooks for ");
            System.out.println(this);
        }
    }

    /**
     * Call any registered LifecyleListeners.
     */
    private void runShutdownListeners() {
        if (shutdownHooks != null) {
            runHooks(shutdownHooks, "SHUTDOWN_EVENT");
            shutdownHooks.removeAll();
            shutdownHooks = null;
        } else {
            if (DEBUG_CODE_ENABLED && VM.isVerbose()) {
                System.out.println("No isolate SHUTDOWN_EVENT hooks for " + this);
            }
        }
    }

    
    /**
     * Stop the isolate. The <code>handleLifecycleListenerEvent()</code> method will be called on any {@link LifecycleListener LifecycleListeners} registered
     * to handle <code>EXIT</code> events on this isolate.
     *
     * @param code the exit code
     * @throws IllegalStateException if this isolate is not <code>ALIVE</code>
     */
    public void exit(int code) {
        if (state != ALIVE) {
            throw new IllegalStateException();
        }
        shutdown(code, true);
    }

    /**
     * Stop the isolate without running shutdown hooks.
     *
     * @param code the exit code
     */
    void abort(int code) {
        shutdown(code, false);
    }

    /**
     * Stop the isolate.
     *
     * @param code the exit code
     * @param doExitHooks if true, run the shutdown hooks.
     */
    void shutdown(int code, boolean doExitHooks) {
        if (state == ALIVE) {
            exitCode = code;
        }


        // Notify debugger of event:
        //Debugger debugger = VM.getCurrentIsolate().getDebugger();
        if (debugger != null) { // debugger always sends this event
            debugger.notifyEvent(new Debugger.Event(Debugger.Event.VM_DEATH, this));
        }


        try {
            hibernate(EXITED, doExitHooks);
        } catch (IOException e) {
            e.printStackTrace();
            if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) Assert.shouldNotReachHere("Isolate.java", 1919);
        }
    }



////    /**
////     * Serializes the object graph rooted by this hibernated isolate and writes it to a given stream.
////     * The endianess of the serialized object graph is the endianess of the unerdlying platform.
////     *
////     * @param  dos       the DataOutputStream to which the serialized isolate should be written
////     * @param  uri       a URI identifying the serialized isolate
////     *
////     * @throws java.io.IOException
////     * @throws IllegalStateException if this isolate is not currently hibernated or exited
////     */
////    public void save(DataOutputStream dos, String uri) throws java.io.IOException {
////        save(dos, uri, VM.isBigEndian());
////    }
////
////    /**
////     * Serializes the object graph rooted by this hibernated isolate and writes it to a given stream.
////     *
////     * @param  dos       the DataOutputStream to which the serialized isolate should be written
////     * @param  uri       a URI identifying the serialized isolate
////     * @param  bigEndian the endianess to be used when serializing this isolate
////     *
////     * @throws java.io.IOException
////     * @throws IllegalStateException if this isolate is not currently hibernated or exited
////     */
////    public void save(DataOutputStream dos, String uri, boolean bigEndian) throws java.io.IOException {
////        if (state != HIBERNATED && state != EXITED) {
////            throw new IllegalStateException("cannot save unhibernated isolate");
////        }
////
////        // Null out the interned string cache as it will be rebuilt on demand
////        internedStrings = null;
////
////        Assert.always(savedStackChunks == null);
////        ObjectMemorySerializer.ControlBlock cb;
////        cb = VM.copyObjectGraph(this);
////        Assert.always(savedStackChunks == null);
////
////        Suite readOnlySuite = leafSuite;
////        while (GC.inRam(readOnlySuite)) {
////            readOnlySuite = readOnlySuite.getParent();
////        }
////
////        ObjectMemorySerializer.save(dos, uri, cb, readOnlySuite.getReadOnlyObjectMemory(), bigEndian);
////    }
////
////    /**
////     * Loads a serialized isolate from an input stream into RAM. It is up to the caller to unhibernate the isolate.
////     *
////     * @param dis  the data input stream to load from
////     * @param uri  a URI identifying the serialized isolate
////     * @return isolate that was stored in dis
////     */
////    public static Isolate load(DataInputStream dis, String uri) {
////        ObjectMemory om = ObjectMemoryLoader.load(dis, uri, false).objectMemory;
////        return load(om);
////    }
////
////    /**
////     * Loads a serialized isolate into RAM. It is up to the caller to unhibernate the isolate.
////     *
////     * @param om the object memory loader to load from
////     */
////    private static Isolate load(ObjectMemory om) {
////        Object root = om.getRoot();
////        if (!(root instanceof Isolate)) {
////            throw new Error("object memory with URI '" + om.getURI() + "' does not contain an isolate");
////        }
////
////        Isolate isolate = (Isolate)root;
////        GC.getCollector().registerStackChunks(isolate.savedStackChunks);
////        isolate.savedStackChunks = null;
////        VM.registerIsolate(isolate);
////
/////*if[!FLASH_MEMORY]*/
////        if (VM.isVerbose()) {
////            int old = VM.setStream(VM.STREAM_SYMBOLS);
////            VM.print("UNHIBERNATED_ISOLATE.RELOCATION=");
////            VM.printUWord(om.getStart().toUWord());
////            VM.println();
////            VM.setStream(old);
////        }
/////*end[FLASH_MEMORY]*/
////
////        return isolate;
////    }



    /**
     * Hibernate the isolate. The <code>handleLifecycleListenerEvent()</code> method will be called on any {@link LifecycleListener LifecycleListeners} registered
     * to handle <code>HIBERNATE</code> events on this isolate.  Any Channel I/O will be hibernated, and inter-isolate communication {@link com.sun.squawk.io.mailboxes.Channel channels} will be broken.
     * If the current thread is in this isolate then this function will only return when the isolate is unhibernated.
     *
     * @throws IOException if the underlying IO system cannot be serialized
     * @throws IllegalStateException if this isolate is not <code>ALIVE</code> or if it has a debugger attached to it
     */
    public void hibernate() throws java.io.IOException, IllegalStateException {
        if (state != ALIVE) {
            throw new IllegalStateException();
        }
        

        if (debugger != null) {
            throw new IllegalStateException("cannot hibernate an isolate with an attached debugger");
        }


        if (DEBUG_CODE_ENABLED && VM.isVeryVerbose()) {
            System.out.println("[Hibernating " + isolateInfoStr() + "]");
        }

        hibernate(HIBERNATED, true);
    }

    
    /**
     * Modifies the state of this isolate.
     *
     * @param newState  the state to which the current state should transition
     */
    private void changeState(int newState) {
        this.state = newState;
    }

    /**
     * Hibernate or exit the isolate. If the current thread is in this isolate then
     * this function will only return when the isolate is unhibernated. If hibernating,
     * also hibernate the underlying IO system.
     *
     * @param  newState    the state that this isolate should be put into once this method completes
     * @param  doHooks     if true, run the shutdown hooks on exit
     * @throws IOException if the underlying IO system cannot be serialized
     */
    private void hibernate(int newState, boolean doHooks) throws java.io.IOException {
        if ((state != newState) && (newState > transitioningState)) {
            // note that while in transition to exit, a concurrent call to exit or hibernate will be ignored.
            transitioningState = newState; // we are in process of moving to newState;

            if (doHooks) {
                switch (newState) {
                    case HIBERNATED: {
                        if (suspendHooks != null) {
                            runHooks(suspendHooks, "HIBERNATE_EVENT");
                        }
                        break;
                    }
                    case EXITED: {
                        runShutdownListeners();
                        break;
                    }
                    default:
                        throw new IllegalArgumentException();
                }
            }
            
            removeVMShutdownHook();



            cleanupMailboxes();


            int channelContextToSave = 0;


            if (Platform.IS_DELEGATING || Platform.IS_SOCKET) {
                channelContextToSave = getChannelContext();
            }

////            channelContextToSave = getChannelContext();
////            /*
////             * Serialize the underlying context if this is not an exiting isolate
////             */
////            if ((HIBERNATED == newState) && channelContextToSave > 0) {
////                hibernatedChannelContext = VM.hibernateChannelContext(channelContextToSave);
////            }


            changeState(newState);
            transitioningState = NEW; // actually, not in transition.
            /*
             * Close the channel I/O
             */
            if (channelContextToSave > 0) {
                if (Platform.IS_DELEGATING || Platform.IS_SOCKET) {
                    VM.deleteChannelContext(channelContextToSave);
                }
                this.channelContext = 0;
            }


           /*
            * Remove this isolate from its parent's list of children. The parentIsolate pointer
            * will be null for the bootstrap isolate as well as for unhibernated isolates
            */
            if (parentIsolate != null) {
                parentIsolate.childIsolates.remove(this);
                parentIsolate = null;
            }

            
            /*
             * Hibernate all the executing threads.
             */
            VMThread.hibernateIsolate(this, state == EXITED);
        }
    }


    /*
     * Add a thread to the list of hibernated run threads.
     *
     * @param thread the thread to add
     */
    void addToHibernatedRunThread(VMThread thread) {
        if (false) Assert.that(thread.nextThread == null);
        thread.nextThread = hibernatedRunThreads;
        hibernatedRunThreads = thread;
    }

    /*
     * Add a thread to the list of hibernated timer threads.
     *
     * @param thread the thread to add
     */
    void addToHibernatedTimerThread(VMThread thread) {
        if (false) Assert.that(thread.nextTimerThread == null);
        thread.nextTimerThread = hibernatedTimerThreads;
        hibernatedTimerThreads = thread;
    }
    
    /**
     * Unhibernate the isolate. The <code>handleLifecycleListenerEvent()</code> method will be called on any {@link LifecycleListener LifecycleListeners} registered
     * to handle <code>UNHIBERNATE</code> events on this isolate.
     *
     * @throws IllegalStateException if the isolate is not <code>HIBERNATED</code>
     */
    public void unhibernate() {
        if (state != HIBERNATED) {
            throw new RuntimeException();
        }
        changeState(ALIVE);
        // don't need to mess with transitioningState becuase we immediately switch to ALIVE state in above.

        // Attach to current isolate as a child
        Isolate currentIsolate = VM.getCurrentIsolate();
        if (false) Assert.that(currentIsolate != null);
        currentIsolate.addIsolate(this);

        VMThread.unhibernateIsolate(this);

        addVMShutdownHook();

        if (resumeHooks != null) {
            runHooks(resumeHooks, "UNHIBERNATE_EVENT");
        }
    }

    /*
     * Get all the hibernated run threads.
     *
     * @return the thread linked by thread.nextThread
     */
    VMThread getHibernatedRunThreads() {
        VMThread res = hibernatedRunThreads;
        hibernatedRunThreads = null;
        return res;
    }

    /*
     * Get all the hibernated timer threads.
     *
     * @return the thread linked by thread.nextTimerThread
     */
    VMThread getHibernatedTimerThreads() {
        VMThread res = hibernatedTimerThreads;
        hibernatedTimerThreads = null;
        return res;
    }

////    // dummy versions
////    void addToHibernatedRunThread(VMThread thread) {
////        Assert.that(thread.nextThread == null);
////    }
////
////    void addToHibernatedTimerThread(VMThread thread) {
////        Assert.that(thread.nextTimerThread == null);
////    }

    
    /**
     * Determines if this isolate is {@link #hibernate() hibernated}.
     *
     * @return true if it is
     */
    public boolean isHibernated() {
        if (ENABLE_MULTI_ISOLATE) {
            return state == HIBERNATED;
        } else {
            return false;
        }
    }
    
    /**
     * Determines if this isolate has been (re)started and not yet (re)hibernated or exited.
     *
     * @return true if it is
     */
    public boolean isAlive() {
        return state == ALIVE;
    }

    /**
     * Determines if this isolate is {@link #exit exited}.
     *
     * @return true if it is
     */
    public boolean isExited() {
        return state == EXITED;
    }

    /**
     * Determines if this isolate has not yet been {@link #start started}.
     *
     * @return true if it is
     */
    public boolean isNew() {
        return state == NEW;
    }

    /**
     * Determines whether this isolate is being debugged
     *
     * @return true if it is
     */
    public boolean isBeingDebugged() {

    	return debugger != null;

////    	return false;

    }

    /**
     * Return true if this isolate was created to run a midlet.
     * @return true if a midlet
     */
    public boolean isMidlet() {
        return mainClassName.equals(MIDLET_WRAPPER_CLASS);
    }

    /**
     * Get the isolate exit code.
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }



////    /**
////     * Add a finalizer to the queue of pending finalizers.
////     *
////     * @param finalizer the finalizer to add
////     */
////    void addFinalizer(Finalizer finalizer) {
////        finalizer.setNext(finalizers);
////        finalizers = finalizer;
////    }
////
////    /**
////     * Remove a finalizer.
////     *
////     * @return the finalizer or null if there are none.
////     */
////    Finalizer removeFinalizer() {
////        Finalizer finalizer = finalizers;
////        if (finalizer != null) {
////            finalizers = finalizer.getNext();
////            finalizer.setNext(null);
////        }
////        return finalizer;
////    }




    /**
     * Add a thread to the list of threads waiting for the isolate to finish.
     *
     * @param thread the thread
     */
    void addJoiner(VMThread thread) {
        thread.nextThread = joiners;
        joiners = thread;
    }

    /**
     * Get all the joining threads.
     *
     * WARNING: THIS CLEARS THE LIST OF JOINERS, SO ONLY CALL ONCE!
     *
     * @return all the threads
     */
    VMThread getJoiners() {
        VMThread res = joiners;
        joiners = null;
        return res;
    }

    
    /**
     * Get the string representation of the isolate.
     *
     * @return the string
     */
    public String toString() {
        StringBuffer res = new StringBuffer("isolate ").append(id).append(" \"").append(name).append("\" ");
        if (isAlive()) {
            res = res.append("ALIVE");
        } else if (isExited()) {
            res = res.append("EXITED");
        } else if (ENABLE_MULTI_ISOLATE && isHibernated()) {
            res = res.append("HIBERNATED");
        } else {
            res = res.append("NEW");
        }
        return res.toString();
    }

    private String isolateInfoStr() {
        StringBuffer sb = new StringBuffer();
        sb.append("isolate for '").append(mainClassName).append("'");


          if (classPath != null) {
              sb.append(" with class path '").append(classPath).append("'");
          }

        if (parentSuiteSourceURI != null) {
            sb.append(" with parent suite URI '").append(parentSuiteSourceURI).append("'");
        }

        if (leafSuite != null && !leafSuite.isClosed()) {
            sb.append(" and leaf suite '").append(leafSuite).append("'");
        }

        return sb.toString();
    }

    /*---------------------------------------------------------------------------*\
     *                            Standard streams                               *
    \*---------------------------------------------------------------------------*/

    /**
     * A DelayedURLOutputStream is used to write to a connection and ensure that the
     * connection is only opened in the context of the isolate that will use it.
     */
    static class DelayedURLOutputStream extends OutputStream {

        /**
         * The delegate output stream.
         */
        private OutputStream out;

        /**
         * The URL used to create the stream.
         */
        private final String url;

        /**
         * Gets the delegate output stream, creating it if it hasn't already been opened.
         *
         * @return the OutputStream
         * @throws IOException if something went wrong while attempting to open the stream
         */
        private synchronized OutputStream out() throws IOException {
            if (out == null) {
                try {
                    out = Connector.openOutputStream(url);
                } catch (IOException e) {
                    VM.println("IO error opening standard stream to " + url + ": " + e);
                    throw e;
                }
            }
            return out;
        }

        /**
         * Creates a DelayedURLOutputStream.
         *
         * @param url  specifies where to open the connection
         */
        DelayedURLOutputStream(String url) {
            this.url = url;
        }

        /**
         * {@inheritDoc}
         */
        public void write(int b) throws IOException {
            out().write(b);
        }

        /**
         * {@inheritDoc}
         */
        public void write(byte b[]) throws IOException {
            out().write(b);
        }

        /**
         * {@inheritDoc}
         */
        public void write(byte b[], int off, int len) throws IOException {
            out().write(b, off, len);
        }

        /**
         * {@inheritDoc}
         */
        public synchronized void flush() throws IOException {
            if (out != null) {
                out.flush();
            }
        }

        /**
         * {@inheritDoc}
         */
        public synchronized void close() throws IOException {
            if (out != null) {
                out.close();
                out = null;
            }
        }
    }

    public final MulticastOutputStream stdout = new MulticastOutputStream();
    public final MulticastOutputStream stderr = new MulticastOutputStream();

    /**
     * Adds a new connection to which {@link System#out} will send its output.
     * <p>
     * If the {@link Thread#currentThread current thread} is not owned by this isolate,
     * opening of the connection is delayed until the next time <code>System.out</code>
     * is written to by one of this isolate's threads. Otherwise the connection is
     * opened as part of this call.
     * <p>
     * Output will be multicast to the new stream as well as any preexisting connection streams.
     * <p>
     * The following code snippet is an example of how to pipe the standard output of the
     * current isolate to a network connection:
     * <p><blockquote><pre>
     *     Thread.currentThread().getIsolate().addOut("socket://server.domain.com:9999").
     * </pre></blockquote>
     *
     * @param url     the URL used to open the connection via {@link Connector#openOutputStream}
     *
     * @see #listOut
     * @see #removeOut
     * @see #clearOut
     */
    public void addOut(String url) {
        addStream(stdout, url);
    }

    /**
     * Adds a new connection to which {@link System#err} will send its output.
     * <p>
     * If the {@link Thread#currentThread current thread} is not owned by this isolate,
     * opening of the connection is delayed until the next time <code>System.err</code>
     * is written to by one of this isolate's threads. Otherwise the connection is
     * opened as part of this call.
     * <p>
     *  Output will be multicast to the new stream as well as any preexisting connection streams.
     *
     * @param url     the URL used to open the connection via {@link Connector#openOutputStream}
     *
     * @see #listErr
     * @see #removeErr
     * @see #clearErr
     */
    public void addErr(String url) {
        addStream(stderr, url);
    }

    /**
     * Removes the connection identified by <code>url</code> (if any) to which {@link System#out}
     * is currently sending its output. The removed connection is immediately flushed and closed. Any
     * IO exceptions are caught and might be printed.
     *
     * @param url     the URL identifying the connection to be removed
     * @throws IllegalArgumentException if <code>url</code> does not name a current out stream
     *
     * @see #listOut
     * @see #addOut
     * @see #clearOut
     */
    public void removeOut(String url) {
        OutputStream oldstrm = stdout.remove(url);
        if (oldstrm == null) {
            throw new IllegalArgumentException(url);
        }
        try {
            oldstrm.flush();
            oldstrm.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes the connection identified by <code>url</code> (if any) to which {@link System#err}
     * is currently sending its output. The removed connection is immediately flushed and closed. Any
     * IO exceptions are caught and might be printed.
     *
     * @param url     the URL identifying the connection to be removed
     * @throws IllegalArgumentException if <code>url</code> does not name a current error stream
     *
     * @see #listErr
     * @see #addErr
     * @see #clearErr
     */
    public void removeErr(String url) {
        OutputStream oldstrm = stderr.remove(url);
        if (oldstrm == null) {
            throw new IllegalArgumentException(url);
        }
        try {
            oldstrm.flush();
            oldstrm.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes all the connections to which {@link System#out} is sending its output.
     * The removed connections are immediately flushed and closed.. Any
     * IO exceptions are caught and might be printed.
     *
     * @see #addOut
     * @see #removeOut
     */
    public void clearOut() {
        try {
            stdout.flush();
            stdout.close();
         } catch (IOException ex) {
            ex.printStackTrace();
        }
        stdout.removeAll();
    }

    /**
     * Removes all the connections to which {@link System#err} is sending its output.
     * The removed connections are immediately flushed and closed. Any
     * IO exceptions are caught and are unlikely to be printed.
     *
     * @see #addErr
     * @see #removeErr
     */
    public void clearErr() {
        try {
            stderr.flush();
            stderr.close();
         } catch (IOException ex) {
             // we just deleted stderr who is going to see this?
            ex.printStackTrace();
        }
        stderr.removeAll();
    }

    private void addStream(MulticastOutputStream mos, String url) {
        if (ENABLE_MULTI_ISOLATE && isCurrentThreadExternal()) {
            url = new String(url);
            mos.add(url, new DelayedURLOutputStream(url));
        } else {
            try {
                mos.add(url, Connector.openOutputStream(url));
            } catch (IOException e) {
                VM.println("IO error opening standard stream to " + url + ": " + e);
            }
        }
    }

    /**
     * Gets a list of URLs denoting the streams to which {@link System#out} is currently sending its output.
     * Note that due to multi-threading, the returned list may not reflect the complete
     * set of streams. If a stream was {@link #addOut added} by another thread, then the returned list
     * may not include the URL of the added stream. If a stream was {@link #removeOut removed} by another thread,
     * then the returned list may include the URL of the removed stream.
     *
     * @return  the list of streams to which <code>System.out</code> is currently sending its output
     *
     * @see #addOut
     */
    public String[] listOut() {
        return listStreams(stdout);
    }

    /**
     * Gets a list of URLs denoting the streams to which {@link System#err} is currently sending its output.
     * Note that due to multi-threading, the returned list may not reflect the complete
     * set of streams. If a stream was {@link #addErr added} by another thread, then the returned list
     * may not include the URL of the added stream. If a stream was {@link #removeErr removed} by another thread,
     * then the returned list may include the URL of the removed stream.
     *
     * @return  the list of streams to which <code>System.err</code> is currently sending its output
     *
     * @see #addErr
     */
    public String[] listErr() {
        return listStreams(stderr);
    }

    private String[] listStreams(MulticastOutputStream mos) {
        String[] names = new String[mos.getSize()];
        Enumeration e = mos.listNames();
        int i = 0;
        try {
            for (; i != names.length; ++i) {
                names[i] = (String)e.nextElement();
            }
        } catch (NoSuchElementException ex) {
            // Another thread removed a stream - resize the array
            String[] old = names;
            names = new String[i];
            System.arraycopy(old, 0, names, 0, i);
        }
        return names;
    }


    /*---------------------------------------------------------------------------*\
     *                            Inter-isolate messages                         *
    \*---------------------------------------------------------------------------*/

    /**
     * Record this mailbox with the system. Called by Mailbox().
     *
     * @param mailbox the mailbox to record.
     */
    public void recordMailbox(Mailbox mailbox) {
        if (mailboxes == null) {
            mailboxes = new SquawkHashtable();
        } else if (mailboxes.get(mailbox) != null) {
            throw new IllegalStateException(mailbox + " is already recorded");
        }

        mailboxes.put(mailbox, mailbox);
    }

    /**
     * Tell the system to forget about this mailbox. Called by Mailbox.close().
     *
     * @param mailbox the mailbox to forget.
     */
    public void forgetMailbox(Mailbox mailbox) {
        if (mailboxes == null ||
            mailboxes.get(mailbox) == null) {
            throw new IllegalStateException(mailbox + " is not recorded");
        }

        mailboxes.remove(mailbox);
    }

    /**
     * Record all MailboxAddress objects that this Isolate uses to send messages to.
     *
     * @param address the mailbox to record.
     */
    public void recordMailboxAddress(MailboxAddress address) {
        if (mailboxAddresses == null) {
            mailboxAddresses = new SquawkHashtable();
        } else if (mailboxAddresses.get(address) != null) {
            throw new IllegalStateException(address + " is already recorded");
        }

        mailboxAddresses.put(address, address);
    }

    /**
     * Tell the system to forget about this mailbox. Called by Mailbox.close().
     *
     * @param address the mailbox to forget.
     */
    public void forgetMailboxAddress(MailboxAddress address) {
        if (mailboxAddresses == null ||
            mailboxAddresses.get(address) == null) {
            throw new IllegalStateException(address + " is not recorded");
        }

        mailboxAddresses.remove(address);
    }

    /**
     * Tell remote isolates that we won't talk to them again,
     * and close our Mailboxes.
     *
     * After this call, remote isolates may have MailboxAddress objects that refer
     * to the closed mailboxes, but when they try to use the address, they will get an exception.
     */
    public void cleanupMailboxes() {
        // this is a bad context to get an error in - so report and squash the error.
        try {
            // tell all remote mailboxes that these references are going away.
            if (mailboxAddresses != null) {
                if (VM.isVeryVerbose()) {
                    System.err.println("Closing addresses...");
                }
                Enumeration addressE = mailboxAddresses.elements();
                while (addressE.hasMoreElements()) {
                    MailboxAddress address = (MailboxAddress)addressE.nextElement();
                    if (VM.isVeryVerbose()) {
                        System.err.println("Closing address " + address);
                    }
                    address.close(); // tolerant of double closes()
                }
            }

            // close all local Mailboxes
            if (mailboxes != null) {
                if (VM.isVeryVerbose()) {
                    System.err.println("Closing mailboxes...");
                }
                Enumeration mailboxE = mailboxes.elements();
                while (mailboxE.hasMoreElements()) {
                    Mailbox mailbox = (Mailbox)mailboxE.nextElement();
                    if (VM.isVeryVerbose()) {
                        System.err.println("Closing mailbox " + mailbox);
                    }
                    mailbox.close(); // tolerant of double closes()
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Uncaught exception while cleaning up mailboxes: " + e);
            e.printStackTrace();
        }

        // TODO: What about hibernation, where server may have thread(s) waiting for messages in inbox.
        // we just threw away unhandled messages in mailbox.close(). Upon unhibernation,
        // will threads wake up still waiting for messages? And what about re-registering the
        // Mailbox?
    }


    /*---------------------------------------------------------------------------*\
     *                            Debugger Support                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Print out the thread state and stack trace for each thread belonging this isolate.
     *
     * @param out stream to print on
     */
    public void printAllThreadStates(PrintStream out) {
        Enumeration e = getChildThreads();
        while (e.hasMoreElements()) {
            VMThread thr = (VMThread)e.nextElement();
            thr.printState(out);
            if (thr.isAlive()) {
                thr.printStackTrace(out);
            }
        }
    }

    /**
     * Print out the thread state and stack trace for each thread of each isolate
     * in the system.
     *
     * @param out stream to print on
     */
    public static void printAllIsolateStates(PrintStream out) {
        Isolate[] isos = Isolate.getIsolates();
        VM.outPrintln(out, "------ VM State at time: " + VM.getTimeMillis() + " ------");
        for (int i = 0; i < isos.length; i++) {
            Isolate iso = isos[i];
            VM.outPrintln(out, "--- " + iso + " status ---");
            iso.printAllThreadStates(out);
        }
    }

    /**
     * A Breakpoint instance describes a point in a method at which a breakpoint has been set.
     */
    public static class Breakpoint {

        /**
         * The method context of the breakpoint.
         */
        public final Object mp;

        /**
         * The offset (in bytes) from <code>mp</code> of the breakpoint.
         */
        public final int ip;

        /**
         * Constructor.
         * @param mp
         * @param ip
         */
        public Breakpoint(Object mp, int ip) {
            this.mp = mp;
            this.ip = ip;
        }

        /**
         * {@inheritDoc}
         * @param o
         */
        public boolean equals(Object o) {
            if (o instanceof Breakpoint) {
                Breakpoint bp = (Breakpoint)o;
                return bp.mp == mp && bp.ip == ip;
            }
            return false;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return ip;
        }
    }


    /**
     * Sets or removes the debugger under which this isolate is executing.
     *
     * @param debugger  connection to a debugger or null to remove a connection
     */
    void setDebugger(Debugger debugger) {
        if (debugger == null && this.debugger != null) {
            // Removing debugger state in threads when detaching
            for (Enumeration e = childThreads.elements(); e.hasMoreElements();) {
                VMThread thread = (VMThread)e.nextElement();
                thread.clearStep();

                // Clear a hit breakpoint only if it is a non-exception breakpoint.
                // Exception breakpoints still need to re-throw the reported exception.
                HitBreakpoint hbp = thread.getHitBreakpoint();
                if (hbp != null && hbp.exception == null) {
                    thread.clearBreakpoint();
                }
            }
        }
        this.debugger = debugger;
    }

    /**
     * Gets the debugger under which this isolate is executing.
     *
     * @return  the debugger under which this isolate is executing (if any)
     */
    public Debugger getDebugger() {
        return debugger;
    }

////    /**
////     * Gets the debugger under which this isolate is executing.
////     *
////     * @return  the debugger under which this isolate is executing (if any)
////     */
////    public Debugger getDebugger() {
////        return null;
////    }


    /**
     * Gets the child threads of this isolate.
     *
     * @return  an Enumeration over the child threads of this isolate
     */
    public Enumeration getChildThreads() {
        return childThreads.elements();
    }

    /**
     * Gets the number of child threads of this isolate.
     *
     * @return  the number of child threads of this isolate
     */
    public int getChildThreadCount() {
        return childThreads.size();
    }

    /**
     * Gets the unique id for this isolate. The id is only unique among isolates that have allocated in the current run of this VM.
     * @return the id of this isolate
     */
    public int getId() {
    	return id;
    }
    

    public void updateBreakpoints(Breakpoint[] breakpoints) {
        this.breakpoints = breakpoints;
    }

    /**
     * The breakpoints that have been set in this isolate.
     * Read by the interpreter loop.
     */
    private Breakpoint[] breakpoints;

}
