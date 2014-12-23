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

/**
 * A <code>JavaCommand</code> denotes a builder command that executes a Java program.
 * The execution occurs in a separate JVM process.
 */
public class JavaCommand extends Command {

    public final String classPath;
    public final boolean bootclasspath;
    public final String extraJVMArgs;
    public final String mainClassName;
    public String description;

    /**
     * Creates a new Java command.
     *
     * @param name   the name of the command
     */
    public JavaCommand(String name, String classPath, boolean bootclasspath, String extraJVMArgs, String mainClassName, Build env) {
        super(env, name);
        this.classPath = classPath;
        this.bootclasspath = bootclasspath;
        this.extraJVMArgs = extraJVMArgs;
        this.mainClassName = mainClassName;
    }

    /**
     * {@inheritDoc}
     */
    public JavaCommand setDescription(String desc) {
        description = desc;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return description == null ? super.getDescription() : description;
    }

    /**
     * {@inheritDoc}
     */
    public void run(String[] args) {
        env.java(classPath, bootclasspath, extraJVMArgs, mainClassName, args);
    }

    @Override
    public void usage(String errMsg) {
        if (errMsg != null) {
            System.err.println(errMsg);
        }
        
        // delegate usage message to the java command itself
        String[] usageArgs = {"-h"};
        env.java(classPath, bootclasspath, extraJVMArgs, mainClassName, usageArgs);
    }
}
