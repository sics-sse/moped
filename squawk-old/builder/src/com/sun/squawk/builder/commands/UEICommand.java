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

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.jar.Manifest;

import com.sun.squawk.builder.Build;
import com.sun.squawk.builder.BuildException;
import com.sun.squawk.builder.Command;
import com.sun.squawk.builder.Target;
import com.sun.squawk.builder.util.FileSet;

/**
 * Command to build a UEI-compliant squawk emulator.
 */
public class UEICommand extends Command {
    protected Target targetEmulatorJ2SE;
    protected Target targetEmulatorJ2ME;
    protected PrintStream stdout;
    protected PrintStream stderr;
    protected PrintStream vbsout;
    protected File ueiModuleDirectory;
    protected File targetDirectory;
    
    public UEICommand(Build env) {
        super(env, "uei");
        ueiModuleDirectory = getFile(getName());
        targetDirectory = new File(ueiModuleDirectory, "build");
        targetEmulatorJ2SE = getTarget(
                new File (ueiModuleDirectory, "launcher-hosted-support"),
                getFile("cldc", "classes") + File.pathSeparator + getFile("romizer", "classes") + File.pathSeparator + getFile("debugger-proxy", "classes") + File.pathSeparator + getFile("build-commands.jar"),
                false);
        targetEmulatorJ2ME = getTarget(
                new File(ueiModuleDirectory, "launcher"),
                getFile("cldc", "classes").getPath(),
                true);
    }
    
    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "Builds the Unified Emulator Interface(UEI) module";
    }
    
    public void usage(String errMsg) {
        //Column     123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
        stderr.println();
        if (errMsg != null) {
            stderr.println(errMsg);
        }
        stderr.println("usage: " + name + " [directory]");
        stderr.println();
        stderr.println("This will build a UEI-compliant emulator in the specified directory.  If no");
        stderr.println("directory is supplied, it will be built in:");
        stderr.println("    " + targetDirectory);
        stderr.println();
    }
    
    /**
     * {@inheritDoc}
     */
    public void run(String args[]) throws BuildException {
        stdout = System.out;
        stderr = System.err;
        vbsout = new PrintStream(new OutputStream() { public void write(int b) {;} });
        
        if (args.length > 1) {
            usage("no options allowed for this command");
            return;
        }
        if (args.length == 1) {
            targetDirectory = new File(args[0]).getAbsoluteFile();
        }
        if (env.verbose) {
            vbsout = stdout;
        }
        try {
            // Capture subprocess output to the verbose out
            System.setOut(vbsout);
            clean();
            buildUEI(targetDirectory);
        } finally {
            System.setOut(stdout);
        }
    }
    
    /**
     * Builds the UEI in the specified directory
     * 
     * @param directory The target emulator directory
     * 
     * @throws BuildException if the build fails
     */
    protected void buildUEI(File directory) throws BuildException {
        final String squawkExeFilename = "squawk" + env.getPlatform().getExecutableExtension();
        final String emulatorExeFilename = "emulator" + env.getPlatform().getExecutableExtension();
        final File preverifier = env.getPlatform().preverifier();
        
        stdout.println("Building UEI in  " + directory);
        Build.mkdir(directory);
        for (String dirName : new String[] { "bin", "lib", "doc", "squawk", "logs", "temp" }) {
            Build.mkdir(directory, dirName);
        }

        stdout.println("Building emulator squawk...");
        builder("clean", "cldc", "hosted-support", "squawk.jar");
        builder("-override:" + getFile(ueiModuleDirectory, "emulator-build.properties"));
        builder("hosted-support"); // does cldc by dependency
        targetEmulatorJ2SE.run(null);
        targetEmulatorJ2ME.run(null);
        builder(
                "-override:" + getFile(ueiModuleDirectory, "emulator-build.properties"),
                "-prod",
                "-mac",
                "-o2",
                "rom",
                "-lnt",
                "-strip:d", 
                getFile("cldc").getPath(),
                targetEmulatorJ2ME.baseDir.getPath());
        
        // Copy emulator squawk
        File binDir = new File(directory, "bin");
        for (String fileName : new String[] {"squawk.suite"}) {
            copyFile(getFile(), fileName, binDir);
        }

        File emulatorJarContent = new File(directory, "emulator-jar");
        emulatorJarContent.mkdirs();
        for (String moduleName : new String[] { "cldc/classes.jar", "squawk.jar", "romizer/classes.jar", "translator/classes.jar", "debugger/classes.jar", "debugger-proxy/classes.jar", "uei/launcher-hosted-support/classes.jar" }) {
            String cmd = "jar xf ../../../" + moduleName;
            env.exec(cmd, null, emulatorJarContent);
        }
        Build.clear(new File(emulatorJarContent, "META-INF"), true);
        createJar(new File(binDir, "squawk.jar"), emulatorJarContent);
        Build.clear(emulatorJarContent, true);

        copyFile(getFile(), squawkExeFilename, binDir, emulatorExeFilename);
        env.chmod(new File(binDir, emulatorExeFilename), "+x");
        copyFile(preverifier.getParentFile(), preverifier.getName(), binDir);
        env.chmod(new File(binDir, preverifier.getName()), "+x");
        
        stdout.println("Building vanilla squawk...");
        Build.clearFilesMarkedAsSvnIgnore(getFile("cldc"));
        builder("clean", "cldc");
        builder("cldc", "imp", "debugger");
        builder(
                "-prod",
                "-mac",
                "-o2",
                "rom",
                "-metadata",
                "-lnt",
                "-strip:d",
                getFile("cldc").getPath(),
                getFile("imp").getPath(),
                getFile("debugger").getPath());
        
        // Copy vanilla squawk
        File vanillaDir = new File(directory, "squawk");
        for (String fileName : new String[] { "squawk.suite", "squawk.suite.metadata", "squawk.jar"}) {
            copyFile(getFile(), fileName, vanillaDir);
        }
        copyFile(getFile(), squawkExeFilename, vanillaDir);
        env.chmod(new File(vanillaDir, squawkExeFilename), "+x");
        
        // Copy API jars
        stdout.println("Creating API jars...");
        File libDir = new File(directory, "lib");
        createJar(new File(libDir, "cldc11.jar"), getFile("cldc", "j2meclasses"), getFile("cldc", "preprocessed"));
        createJar(new File(libDir, "imp10.jar"), getFile("imp", "j2meclasses"), getFile("imp", "preprocessed"));
        // Create API javadoc
        stdout.println("Creating API javadoc...");
        try {
            // Capture javadoc warnings to vbsout
            System.setErr(vbsout);
            env.getJavaCompiler().javadoc(new String[] {
                    "-d", new File(directory, "doc").getPath(),
                    "-sourcepath", getFile("cldc", "preprocessed") + File.pathSeparator + getFile("imp", "preprocessed"),
                    "-subpackages", "com:java:javax",
                    "-windowtitle", "Java 2 Platform ME CLDC-1.1/IMP-1.0",
                    "-doctitle",  "Java<sup><font size=-2>TM</font></sup> 2 Platform Micro Edition<br>CLDC-1.1 / IMP-1.0 API Specification",
                    "-header", "<b>Java<sup><font size=-2>TM</font></sup> 2 Platform<br><font size=-1>Micro Ed. CLDC-1.1 / IMP-1.0</font></b>",
                    "-bottom", "<font size=-1>Copyright 2008 Sun Microsystems, Inc.  All rights reserved.</font>",
                    "-quiet" }, true);
        } finally {
            System.setErr(stderr);
        }
    }
    
    /**
     * Convenience method that gets the file designated by <code>path</code>
     * relative to <code>parent</code>.
     * 
     * @param parent The parent directory.
     * @param path The file path relative to the parent directory.
     * @return The file specified by <code>path</code> relative to <code>parent</code>.
     */
    protected File getFile(File parent, String... path) {
        File file = parent;
        for (String element: path) {
            file = new File(file, element);
        }
        return file;
    }
    
    /**
     * Convenience method that gets the file designated by <code>path</code>
     * relative to current working directory.
     * 
     * @param path The file path relative to the parent directory.
     * @return The file specified by <code>path</code> relative to current working directory.
     */
    protected File getFile(String... path) {
        File file = null;
        for (String element: path) {
            file = new File(file, element);
        }
        return file;
    }
    
    /**
     * Convenience method that gets the <code>Target</code> associated with the
     * specified module assuming that the module's entire source code is found
     * solely in a subdirectory "src", that the module's directory is a valid
     * <code>Target</code> name, and that preprocessing is enabled.
     * 
     * @param baseDir The base directory of the module.
     * @param classpath Additional classpath required by the module.
     * @param j2me Whether or not this is a j2me module.
     * @return The associated <code>Target</code>.
     */
    protected Target getTarget(File baseDir, String classpath, boolean j2me) {
        return new Target(classpath, j2me, baseDir.getPath(), new File[] { new File(baseDir, "src") }, true, env, baseDir.getName());
    }
    
    /**
     * Convenience method that invokes <code>Build.main(String[])</code> with
     * the given arguments. Note that since an instance of <code>Build</code>
     * will only execute commands once, it is necessary to use the static
     * command line <code>main</code> entry point.
     * 
     * @param args The builder arguments.
     * 
     * @see Build#main(String[])
     */
    protected void builder(String... args) {
        vbsout.println("Executing builder: " + Build.join(args, 0, args.length, " "));
        
        Build.main(args);
    }

    /*
     * Convenience method that invokes <code>Build.exec(String)</code> with
     * the given arguments.
     * 
     * @param args The execution arguments.
     * 
     * @see Build#exec(String)
     */
    /*
    protected void buildExec(String... args) {
        String cmd = Build.join(args, 0, args.length, " ");
        
        vbsout.println("Executing process: " + cmd);
        
        env.exec(cmd);
    }
    */
    
    /**
     * Convenience method that copies a file from one directory to another.
     * 
     * @param srcPath The parent directory of the target file.
     * @param srcName The name of the target file.
     * @param destPath The destination directory.
     * 
     * @see UEICommand#copyFile(File, File)
     * @see UEICommand#copyFile(File, String, File, String)
     * @see Build#cp(File, File, boolean)
     */
    protected void copyFile(File srcPath, String srcName, File destPath) {
        copyFile(srcPath, srcName, destPath, srcName);
    }
    
    /**
     * Convenience method that copies and renames a file from one directory to
     * another.
     * 
     * @param srcPath The parent directory of the target file.
     * @param srcName The name of the target file.
     * @param destPath The destination directory.
     * @param destName The destination name.
     * 
     * @see UEICommand#copyFile(File, File)
     * @see Build#cp(File, File, boolean)
     */
    protected void copyFile(File srcPath, String srcName, File destPath, String destName) {
        copyFile(new File(srcPath, srcName), new File(destPath, destName));
    }
    
    /**
     * Convenience method that copies and renames a file.
     * 
     * @param src The target file.
     * @param dest The destination file.
     * 
     * @see Build#cp(File, File, boolean)
     */
    protected void copyFile(File src, File dest) {
        vbsout.println("copying " + src);
        vbsout.println("     to " + dest);
        
        Build.cp(src, dest, false);
    }
    
    /**
     * Convenience method that creates a jar-file using all the files in
     * <code>srcFolder</code> and the specified <code>manifest</code>.
     * 
     * @param dest The jar-file to be created.
     * @param srcFolder The source for the jar.
     * @param manifest The manifest for the jar. If this value is null, then no
     *            manifest will be included.
     * 
     * @throws BuildException if the build fails.
     * 
     * @see Build#createJar(File, FileSet[], Manifest)
     */
    protected void createJar(File dest, File... srcFolders) throws BuildException {
        Manifest mf = null;
        ArrayList<FileSet> fileSets = new ArrayList<FileSet>();
        for (File srcFolder: srcFolders) {
            if (mf == null) {
                File manifestFile = getFile(srcFolder.getParentFile(), "resources", "META-INF", "MANIFEST.MF");
                if (manifestFile.canRead()) {
                    try {
                        mf = new Manifest(new FileInputStream(manifestFile));
                    } catch (Exception e) {
                        throw new BuildException("Error reading manifest: " + manifestFile, e);
                    }
                }
            }
            fileSets.add(new FileSet(srcFolder, (FileSet.Selector) null));
        }
        env.createJar(dest, fileSets.toArray(new FileSet[fileSets.size()]), mf);
    }
    
    @Override
    public void clean() {
        Build.clearFilesMarkedAsSvnIgnore(new File(getName()));
        Build.clear(new File(getName(), "build"), true);
    }

}
