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

import java.io.File;
import java.util.*;

/**
 * A Command instance describes a builder command.
 */
public abstract class Command {
    
    /**
     * An iterator that translates a command name to a command as it iterates
     * over a collection of command names.
     */
    class CommandNameIterator implements Iterator {

        private final Iterator nameIterator;

        CommandNameIterator(Collection commandNames) {
            nameIterator = commandNames.iterator();
        }

        /**
         * {@inheritDoc}
         *
         * @return <tt>false</tt>
         */
        public boolean hasNext() {
            return nameIterator.hasNext();
        }

        /**
         * {@inheritDoc}
         *
         * Throws NoSuchElementException.
         */
        public Object next() {
            String name = (String)nameIterator.next();
            Command command = env.getCommand(name);
            if (command == null) {
                throw new BuildException("command not found: " + name);
            }
            return command;
        }

        /**
         * {@inheritDoc}
         *
         * Throws  UnsupportedOperationException.
         */
        public void remove() {
            nameIterator.remove();
        }

    }

    protected final Build env;
    protected final String name;
    private ArrayList<String> dependencies;
    private ArrayList<String> triggeredCommands;
    private File baseDir;

    /**
     * Creates a new command.
     *
     * @param   env   the builder environment in which this command will run
     * @param   name  the name of this command
     */
    public Command(Build env, String name) {
        this.env = env;
        this.name = name;
        dependencies = new ArrayList<String>();
        triggeredCommands = new ArrayList<String>();
    }

    /**
     * Runs the command.
     *
     * @param  args  the command line argmuents
     * @throws BuildException if the command failed
     */
    public abstract void run(String[] args) throws BuildException;

    /**
     * Gets the name of this command.
     *
     * @return the name of this command
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a brief one-line description of what this command does.
     *
     * @return a brief one-line description of what this command does
     */
    public String getDescription() {
        return "<< no description available >>";
    }
    
    /**
     * Print a usage message for command line options
     * @param errMsg optional error message string to print
     */
    public void usage(String errMsg) {
        defaultUsage(null, errMsg);
    }
    
    /**
     * Print a simple usage message containing the command name and an optional desription of simple args that fit on one line.
     * optionall preceed with the errMsg.
     * @param simpleArgs [optional] the end of the usage line
     * @param errMsg [optional] error message to print before usage
     */
    public void defaultUsage(String simpleArgs, String errMsg) {
        if (errMsg != null) {
            System.err.println(errMsg);
        }
        System.err.print("Usage: " + getName());
        if (simpleArgs != null) {
            System.err.print(simpleArgs);

        }
        System.err.println();
    }

    /**
     * Filter out dependencies that are directories.
     * @param cmd
     * @return true if command that should execute first
     */
    private boolean processDependencies(String cmd) {
        // @TODO FIX:
        return true;

//        if (env.getCommand(cmd) != null) {
//                return true;
//            } else {
//                File dir = new File(getBaseDir(), cmd);
//                if (!dir.exists() || !dir.isDirectory()) {
//                    throw new BuildException("Dependent directory not found: " + dir);
//                }
//                env.processBuilderDotPropertiesFile(dir);
//                return false;
//            }
    }

    /**
     * Adds one or more commands that this command depends upon. The dependencies of a command
     * are run before the command itself is run.
     *
     * @param names   the names of the commands to add separated by spaces
     */
    public final void dependsOn(String names) {
        StringTokenizer st = new StringTokenizer(names);
        while (st.hasMoreTokens()) {
            String cmd = st.nextToken();
            if (dependencies == null) {
                dependencies = new ArrayList<String>();
            }
            if (processDependencies(cmd)) {
                dependencies.add(cmd);
            }
        }
    }

    /**
     * Gets an iteration of the dependencies of this command.
     *
     * @return an iteration of the dependencies of this command
     */
    public final List<String> getDependencyNames() {
        return dependencies;
    }

    /**
     * Allow directories to be specified as a dependency. If a dir, 
     * load any builder.properties commands, and remove the dir from the 
     * dependencies.
     * 
     * @param oldDependencies ArrayList of command or directories containing new commands
     * @return ArrayList of strings of real dependency commands
     */
    public ArrayList<String> processDependencies(ArrayList<String> oldDependencies) {
        ArrayList<String> result = new ArrayList<String>(oldDependencies.size());
        for (String cmd: oldDependencies) {
            if (env.getCommand(cmd) != null) {
                result.add(cmd);
            } else {
                File dir = new File(getBaseDir(), cmd);
                if (!dir.exists() || !dir.isDirectory()) {
                    throw new BuildException("Dependent directory not found: " + dir);
                }
                env.processBuilderDotPropertiesFile(dir);
            }
        }
        return result;
    }
    
    /**
     * Adds a command that is triggered by this command. That is, a command that
     * will always be run after this command has been run.
     *
     * @param names   the names of the commands to add separated by spaces
     */
    public final void triggers(String names) {
        StringTokenizer st = new StringTokenizer(names);
        while (st.hasMoreTokens()) {
            String cmd = st.nextToken();
            if (triggeredCommands == null) {
                triggeredCommands = new ArrayList<String>();
            }
            triggeredCommands.add(cmd);
        }
    }

    /**
     * Gets an iteration of the commands triggered by this command.
     *
     * @return an iteration of the commands triggered by this command
     */
    public final List<String> getTriggeredCommandNames() {
        return triggeredCommands;
    }

    /**
     * Removes all the files generated by running this command.
     */
    public void clean() {
    }
        
    /**
     * Returns the base directory for this command.
     * By default returns teh current directory
     *
     * @return the name of this command
     */
    public File getBaseDir() {
        if (baseDir == null) {
            return new File("");
        } else {
            return baseDir;
        }
    }
   
    /**
     * Sets the base directory for this command.
     * @param newBaseDir the new base dir for this 
     */
    public void setBaseDir(File newBaseDir) {
        baseDir = newBaseDir;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return name;
    }
}