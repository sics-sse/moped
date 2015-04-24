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
package com.sun.squawk.builder.commands;

import com.sun.squawk.builder.*;
import com.sun.squawk.builder.util.FileSet;
import java.io.*;
import java.util.*;

/**
 * Command that creates a library or application suite from a typical "module"
 * (a directory with src and resource sub-directories). Supports dependencies.
 */
public class LinkTarget extends Command {
    /**
     * The basic name of the command, also name of the compile target command
     */
    final String baseName;
    final File baseDir;
    final String parent;
    final String suitePath;

        
    /**
     * Creates a new suite command.
     *
     * @param baseDir        the base directory under which the various intermediate and output directories are created
     * @param parent         the location of the parent suite
     * @param env Build      the builder environment in which this command will run
     * @param suitePath      the suitepath to search (beyond "." and baseDir)
     */
    public LinkTarget(String baseDir, String parent, Build env, String suitePath) {
        this(baseDir, parent, env, new File(baseDir).getName(), suitePath);
    }
    
    /**
     * Creates a new suite command.
     *
     * @param baseDir        the base directory under which the various intermediate and output directories are created
     * @param parent         the location of the parent suite
     * @param env Build      the builder environment in which this command will run
     * @param baseName       the basic command name (the name of the dir)
     * @param suitePath      the suitepath to search (beyond "." and baseDir)
     */
    public LinkTarget(String baseDir, String parent, Build env, String baseName, String suitePath) {
        super(env, baseName + "-suite");
        this.parent = parent;
        this.baseDir = new File(baseDir);
        this.baseName = baseName;
        this.suitePath = suitePath;
    }
    
    @Override
    public String getDescription() {
        return "link the classes in " +  new File(baseDir, "j2meclasses") + " into a suite";
    }

    @Override
    public void run(String[] args) {
        String curParent = parent;
        String extraSuitePath = null;
        Vector<String> extraArgs = new Vector<String>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].startsWith("-parent:")) {
                    curParent = args[i].substring("-parent:".length());
                    continue;
                } else if (args[i].startsWith("-suitepath:")) {
                    extraSuitePath = args[i].substring("-suitepath:".length());
                    continue;
                } 
            }
            extraArgs.add(args[i]); // collect other options
        }
        
        // note that by default, dependnecies will run before this, including compiling source...
            
        String curSuitePath = null;
        if (suitePath != null && suitePath.length() != 0) {
            curSuitePath = suitePath;
        }
        if (extraSuitePath != null) {
            if (curSuitePath != null) {
                curSuitePath += File.pathSeparator + extraSuitePath;
            } else {
                curSuitePath = extraSuitePath;
            }
        } else {
            if (curSuitePath == null) {
                curSuitePath = "";
            }
        }

        final File dstFile = new File(baseDir, baseDir.getName() + ".suite"); // name of the 
        final File classesDir = new File(baseDir, "j2meclasses");
        final File resourcesDir = new File(baseDir, "resources");

        FileSet.Selector isOutOfDateFromClasses = new FileSet.AndSelector(Build.JAVA_CLASS_SELECTOR, new FileSet.DependSelector(new FileSet.Mapper() {
            public File map(File from) {
                return dstFile;
            }
        }));
        FileSet.Selector isOutOfDateFromAnyFile = new FileSet.DependSelector(new FileSet.Mapper() {
            public File map(File from) {
                return dstFile;
            }
        });

        // Rebuilds the suite file if any of the *.class files in ./j2meclasses, or if any file in ./resources have
        // a later modification date than the suite file.
        if (!dstFile.exists() ||
                (classesDir.exists() && !new FileSet(classesDir, isOutOfDateFromClasses).list().isEmpty()) ||
                (resourcesDir.exists() && !new FileSet(resourcesDir, isOutOfDateFromAnyFile).list().isEmpty())) {
            Vector<String> rargs = new Vector<String>();
            rargs.add("-cp:" + classesDir);
            rargs.add("-suitepath:" + curSuitePath);
            rargs.add("-boot:squawk");
            if (curParent != null && curParent.length() != 0) {
                rargs.add("-parent:" + curParent);
            }
            rargs.add("-o:" + new File(baseDir, baseDir.getName()));
            if (env.verbose) {
                rargs.add("-v");
            }
            rargs.add("-metadata");
            rargs.addAll(extraArgs);
            rargs.add(classesDir.getPath());
            if (resourcesDir.exists()) {
                rargs.add(resourcesDir.getPath());
            }

            Command romize = env.getCommand("romize");
            romize.run(Build.toStringArray(rargs));
        } else {
            env.log(env.info, "[no linking necessary. " + dstFile + " is up to date]");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void clean() {
        Build.delete(new File(baseDir, baseName + ".suite"));
        Build.delete(new File(baseDir, baseName + ".suite.api"));
        Build.delete(new File(baseDir, baseName + ".suite.metadata"));
        Build.delete(new File(baseDir, baseName + ".sym"));
    }

    @Override
    public void usage(String errMsg) {
        if (errMsg != null) {
            System.err.println(errMsg);
        }
        System.err.println("Usage: " + getName() + " [romize options] ");
        System.err.println("    run 'romize -h' for more details");
    }
}
