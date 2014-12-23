/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.builder;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import com.sun.squawk.builder.util.*;

/**
 * The Java compilation interface. If the javamake tool is available and the user did not
 * specify the '-nojavamake' flag, then it is used for compilation otherwise javac is used.
 * The benefit of javamake is that it does much more extensive dependency analysis.
 */
public class JavaCompiler {

    final Build env;

    /**
     * Controls if javamake should be used.
     */
    public boolean javamake = true;

    /**
     * Controls if javac is called via Build.exec()
     */
    public boolean externalJavac = false;

    /**
     * The set of compiler options.
     */
    private List<String> args = new ArrayList<String>();

    /**
     * Creates an object for performing Java source compilations.
     *
     * @param env  the builder environment
     */
    public JavaCompiler(Build env) {
        this.env = env;

        /*
         * The Sun PPC port of javac does not compile our code properly,
         * however, we still want to use this java implementation.
         * The linux-ppc is our mercury board which requires the IBM
         * javac to be called. Thus we must call the external javac,
         * which means we must also set appropriate PATH environment
         * information.  ie. sun-ppc version of 'java'
         *                   IBM version of 'javac'
         */
        Properties properties = System.getProperties();
        if(properties.getProperty("use.external.javac", "false").equals("true") ||
            env.getPlatform().toString().toLowerCase().equals("linux-ppc")) {
            javamake = false;
            externalJavac = true;
        }
    }

    private Method javacMethod;
    private Class<?>  javamakeClass;
    private Method javamakeMethod;
    private Method javadocMethod;

    /**
     * Initializes the reflection objects used to invoke the various tools used for compilation.
     */
    private void initializeTools() {
        if (javamakeMethod != null || javacMethod != null) {
            // Already initialized
            return;
        }
        env.log(env.verbose, "[initializing Java compiler tools]");


        // Try to initialize javadoc
        String javadocClassName = System.getProperty("builder.tools.javadoc.class", "com.sun.tools.javadoc.Main");
        String javadocMethodName = System.getProperty("builder.tools.javadoc.method", "execute");
        try {
            Class<?> javadocClass = Class.forName(javadocClassName);
            javadocMethod = javadocClass.getMethod(javadocMethodName, new Class[] {String[].class});
            if (!Modifier.isStatic(javadocMethod.getModifiers())) {
                throw new BuildException("javadoc entry method must be static");
            }
        } catch (ClassNotFoundException e) {
            env.log(env.verbose, "[internal javadoc not found]");
        } catch (NoSuchMethodException e) {
            env.log(env.verbose, "[internal javadoc not found]");
        }

        // Try to initialize javamake
        if (javamake) {
            String javamakeClassName = "com.sun.tools.javamake.Main";
            String javamakeMethodName = "mainProgrammatic";
            Method customizeOutput = null;
            try {
                javamakeClass = Class.forName(javamakeClassName);
                javamakeMethod = javamakeClass.getMethod(javamakeMethodName, new Class[] {String[].class});

                customizeOutput = javamakeClass.getMethod("customizeOutput", new Class[]{ boolean.class, boolean.class, boolean.class });
                customizeOutput.invoke(null, new Object[] {new Boolean(env.info), new Boolean(env.info), Boolean.TRUE});

                return;
            } catch (NoSuchMethodException e) {
                throw new BuildException("cannot find javamake entry method: " + javamakeMethodName + "(String[])");
            } catch (InvocationTargetException e) {
                throw new BuildException("reflection error invoking " + customizeOutput, e);
            } catch (IllegalAccessException e) {
                throw new BuildException("reflection error invoking " + customizeOutput, e);
            } catch (ClassNotFoundException e) {
                env.log(env.verbose, "[javamake not found, switching to standard compilation: add javamake.jar to builder's classpath to prevent this]");
            }
        }

        // Fall back on javac
        String javacClassName = System.getProperty("builder.tools.javac.class", "com.sun.tools.javac.Main");
        String javacMethodName = System.getProperty("builder.tools.javac.method", "compile");
        try {
            Class<?> javacClass = Class.forName(javacClassName);
            javacMethod = javacClass.getMethod(javacMethodName, new Class[] {String[].class});
            if (!Modifier.isStatic(javacMethod.getModifiers())) {
                throw new BuildException("javac entry method must be static");
            }
        } catch (ClassNotFoundException e) {
            throw new BuildException("cannot find javac main class: " + javacClassName);
        } catch (NoSuchMethodException e) {
            throw new BuildException("cannot find javac entry method: " + javacMethodName + "(String[])");
        }

    }

    /**
     * Resets the arguments to be empty.
     */
    public void reset() {
        args = new ArrayList<String>();
    }

    /**
     * Adds an argument that has no option to the compiler.
     *
     * @param arg  a compilation argument to add
     */
    public JavaCompiler arg(String arg) {
        assert arg.indexOf(' ') == -1;
        args.add(arg);
        return this;
    }

    /**
     * Adds an argument that has a sub-option to the compiler.
     *
     * @param arg     a compilation argument to add
     * @param option  the option for <code>arg</code>
     */
    public JavaCompiler arg(String arg, String option) {
        return arg(arg).arg(option);
    }

    /**
     * Adds zero or more arguments from a space separated string.
     *
     * @param args  a string og zero or more space separated arguments
     */
    public JavaCompiler args(String args) {
        StringTokenizer st = new StringTokenizer(args);
        while (st.hasMoreTokens()) {
            this.args.add(st.nextToken());
        }
        return this;
    }

    /**
     * Compiles a given set of files. This may perform conditional compilation if javamake is available.
     *
     * @param classPath the compilation classpath
     * @param outputDir the compilation output directory
     * @param srcDirs   the directories with the files to compile
     * @param j2me      true if this is a CLDC/J2ME compilation
     */
    public void compile(String classPath, File outputDir, File[] srcDirs, boolean j2me) {
        initializeTools();

        // Try to use javamake first
        if (javamakeMethod != null) {
            javamake(srcDirs, classPath, outputDir, j2me);
        } else {
            javac(srcDirs, classPath, outputDir, j2me);
        }
    }

    /**
     * Performs a compilation via the javamake tool. The first element in <code>srcDirs</code> is the primary
     * source directory and will be used for javamake's project file (i.e. javamake.pdb) as well as the
     * args file given to javamake (i.e. javamake.input).
     *
     * @param srcDirs        the directories with the files to (conditionally) compile
     * @param classPath      the compilation classpath
     * @param outputDir      the compilation output directory
     * @param bootclasspath  true if <code>classPath</code> is a boot classpath
     * @throws BuildException if javamake is available but was not successfully executed
     */
    private void javamake(File[] srcDirs, String classPath, File outputDir, boolean bootclasspath) {

        File primarySrcDir = srcDirs[0];
        StringBuffer args = new StringBuffer(2000);

        // Create the project path
        if (classPath != null) {
            StringTokenizer st = new StringTokenizer(Build.toPlatformPath(classPath, true), File.pathSeparator);
            if (st.hasMoreTokens()) {
                args.append("-projclasspath ");
                while (st.hasMoreTokens()) {
                    File dirOrJar = new File(st.nextToken());
                    if (dirOrJar.isDirectory()) {
                        File classesJar = new File(dirOrJar.getPath() + ".jar");
                        if (!classesJar.exists()) {
                            throw new BuildException("cannot find " + classesJar);
                        }
                        dirOrJar = classesJar;
                    }
                    args.append(dirOrJar.getPath());
                    if (st.hasMoreTokens()) {
                        args.append(File.pathSeparatorChar);
                    }
                }
            }
        }

        // Prepend the output directory to the class path
        if (classPath == null) {
            classPath = outputDir.getPath();
        } else {
            classPath = Build.toPlatformPath(outputDir.getPath() + ':' + classPath, true);
        }

        args.append(" -pdb ").
            append(new File(primarySrcDir, "javamake.pdb").getPath()).
            append(bootclasspath ? " -bootclasspath " : " -classpath ").
            append(outputDir.getPath()).
            append(" -d ").
            append(outputDir.getPath());

        for (Iterator<String> iterator = this.args.iterator(); iterator.hasNext(); ) {
            String arg = (String)iterator.next();
            if (arg.equals("-bootclasspath")) {
                args.append(' ').append(arg);
                if (!iterator.hasNext()) {
                    throw new BuildException("-bootclasspath requires a value");
                }
                args.append(' ').append(iterator.next());
            } else {
                // add -C to the non-javamake arg to pass it through to the compiler
                args.append(" -C").append(arg);
            }
        }

        // Add the source files to the args
        for (int i = 0; i != srcDirs.length; ++i) {
            File srcDir = srcDirs[i];
            List<File> files = new FileSet(srcDir, Build.JAVA_SOURCE_SELECTOR).list();
            for (File file: files) {
                args.append(' ').append(file.getPath());
            }
        }
        // Generate the args file for javamake
        File argsFile = new File(primarySrcDir, "javamake.input");
        createArgsFile(args.toString(), argsFile);

        // Run javamake
        try {
            env.log(env.info, "[running 'javamake @" + argsFile + "'...]");
            Object instance = javamakeClass.newInstance();
            javamakeMethod.invoke(instance, new Object[] { new String[] { "@" + argsFile } });
        } catch (IllegalAccessException e) {
            throw new BuildException("cannot instantiate or invoke javamake", e);
        } catch (InstantiationException e) {
            throw new BuildException("cannot create javamake instance", e);
        } catch (InvocationTargetException e) {
            throw new BuildException("reflection error invoking javamake", e);
        } catch (IllegalArgumentException e) {
            throw new BuildException("reflection error invoking javamake", e);
        }

        // Create classes.jar if it does not exist or is older than any of the class files in the classes directory
        updateClassesJar(outputDir);
    }

    /**
     * Creates/updates jar of the class files under a given directory. The path of the
     * created jar file is <code>outputDir.getPath() + ".jar"</code>.
     *
     * @param outputDir directory of class files
     */
    private void updateClassesJar(File outputDir) {
        final File jarFile = new File(outputDir.getPath() + ".jar");
        FileSet.Selector jarIsOutOfDate = new FileSet.AndSelector(Build.JAVA_CLASS_SELECTOR, new FileSet.DependSelector(new FileSet.Mapper() {
            public File map(File from) {
                return jarFile;
            }
        }));
        if (!new FileSet(outputDir, jarIsOutOfDate).list().isEmpty()) {
            env.createJar(jarFile, new FileSet[] { new FileSet(outputDir, Build.JAVA_CLASS_SELECTOR) }, null);
        }
    }

    /**
     * Refines a set of Java source files to those that are younger than their corresponding class files.
     *
     * @param srcDirs    the directories with the source files
     * @param outputDir  the output directory where the classes reside
     * @return the set of Java source files in <code>srcDir</code> that were modified more recently than their corresponding class files
     */
    private List<File> getModifiedFiles(File[] srcDirs, File outputDir) {
        List<File> modifiedFiles = null;
        for (int i = 0; i != srcDirs.length; ++i) {
            File srcDir = srcDirs[i];
            FileSet.Selector outOfDate = new FileSet.AndSelector(Build.JAVA_SOURCE_SELECTOR, new FileSet.DependSelector(new FileSet.SourceDestDirMapper(srcDir, outputDir) {
                public File map(File from) {
                    File to = super.map(from);
                    String toPath = to.getPath();
                    assert toPath.endsWith(".java");
                    return new File(toPath.substring(0, toPath.length() - ".java".length()) + ".class");
                }
            }));
            FileSet files = new FileSet(srcDir, outOfDate);
            if (modifiedFiles == null) {
                modifiedFiles = files.list();
            } else {
                modifiedFiles.addAll(files.list());
            }
        }
        return modifiedFiles;
    }

    /**
     * Compiles a given set of files using javac. The first element in <code>srcDirs</code> is the primary
     * source directory and will be used for args file given to javac (i.e. javac.input).

     *
     * @param srcDirs        the directories with the files to compile
     * @param classPath      the compilation classpath
     * @param outputDir      the compilation output directory
     * @param bootclasspath  true if <code>classPath</code> is a boot classpath
     * @throws BuildException if javac was not successfully executed
     */
    private void javac(File[] srcDirs, String classPath, File outputDir, boolean bootclasspath) {

        File primarySrcDir = srcDirs[0];

        // Refine the sources to be compiled to those whose class files are out of date
        List<File> files = getModifiedFiles(srcDirs, outputDir);
        if (files.isEmpty()) {
            // Nothing needs to be recompiled
            env.log(env.info, "[no javac recompilation necessary]");
            return;
        }

        StringBuffer args = new StringBuffer(2000);

        // Set up the classpath or bootclasspath
        args.append(bootclasspath ? " -bootclasspath " : " -classpath ");
        args.append(classPath == null ? outputDir.getPath() : Build.toPlatformPath(outputDir.getPath() + ':' + classPath, true));
        args.append(" -d ");
        args.append(outputDir.getPath());

        // Add the other args
        for (String arg: this.args) {
            args.append(' ').append(arg);
        }

        // Add the sources files
        for (File file: files) {
            args.append(' ').append(file.getPath());
        }

        // Generate the args file for javac
        File argsFile = new File(primarySrcDir, "javac.input");
        createArgsFile(args.toString(), argsFile);

        // Run javac
        try {
            env.log(env.info, "[running 'javac @" + argsFile + "' to compile " + files.size() + " sources...]");

            if (externalJavac) {
                env.exec("javac" + env.getPlatform().getExecutableExtension() + " @" + argsFile);
            } else {
                int result = ((Integer)javacMethod.invoke(null, new Object[] { new String[] {"@" + argsFile} })).intValue();
                if (result != 0) {
                    throw new BuildException("compilation failed", result);
                }
            }
        } catch (IllegalAccessException e) {
            throw new BuildException("cannot invoke javac", e);
        } catch (InvocationTargetException e) {
            throw new BuildException("reflection error invoking javamake", e);
        } catch (IllegalArgumentException e) {
            throw new BuildException("reflection error invoking javamake", e);
        }

        // Create classes.jar if it does not exist or is older than any of the class files in the classes directory
        updateClassesJar(outputDir);
    }

    /**
     * Creates a file containing the arguments to javac in a format compatible with the '@' javac option.
     *
     * @param args      the arguments to be passed to javac
     * @param argsFile  the file to which these arguments are to be written
     */
    public static void createArgsFile(String args, File argsFile) {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(argsFile)));
            pw.println(args);
            pw.close();
        } catch (IOException e) {
            throw new BuildException("error creating args file '" + argsFile + "'", e);
        }
    }

    public static void createArgsFile(Collection<String> args, File argsFile) {
        StringBuffer buf = new StringBuffer(1000);
        for (String arg: args) {
            buf.append(arg).append(' ');
        }
        createArgsFile(buf.toString(), argsFile);
    }

    /**
     * Runs javadoc.
     *
     * @param args      the command line args to pass to javadoc
     * @param internal  specifies that javadoc should be run within this JVM process. This
     *                  is required by the {@link com.sun.squawk.builder.commands.MakeAPI makeapi} command.
     */
    public void javadoc(String[] args, boolean internal) {
        initializeTools();
        env.log(env.info, "[running 'javadoc " + Build.join(args) + "'...]");
        if (!internal) {
            env.exec(env.getJDK().javadoc() + ' ' + Build.join(args));
        } else {
            if (javadocMethod == null) {
                File lib = new File(System.getProperty("java.home"), "lib");
                File toolsDotJar = new File(lib, "tools.jar");
                throw new BuildException("cannot find javadoc - try adding " + toolsDotJar + " to the builder's classpath");
            }
            try {
                javadocMethod.invoke(null, new Object[] {args});
            } catch (InvocationTargetException e) {
                throw new BuildException("reflection error invoking javadoc", e);
            } catch (IllegalArgumentException e) {
                throw new BuildException("reflection error invoking javadoc", e);
            } catch (IllegalAccessException e) {
                throw new BuildException("reflection error invoking javadoc", e);
            }
        }
    }

}