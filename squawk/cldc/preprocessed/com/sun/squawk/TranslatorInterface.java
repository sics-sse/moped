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

package com.sun.squawk;

import java.io.PrintStream;

/**
 * The <code>TranslatorInterface</code> is the interface by which new
 * classes can be created and loaded into the runtime system.<p>
 *
 * The runtime system (per isolate) can have at most one open connection with
 * a translator (i.e. an object that implements this interface). The
 * correct usage of a translator is described by the following state
 * transistion machine:
 * <p><blockquote><pre>
 *
 *             +----------- open() ----------+     +---------+
 *             |                             |     |         |
 *             |                             V     V         |
 *        +--------+                       +---------+       |
 *   ---> | CLOSED |                       |  OPEN   |  load() / convert()
 *        +--------+                       +---------+       |
 *             ^                             |     |         |
 *             |                             |     |         |
 *             +---------- close() ----------+     +---------+
 *
 * </pre></blockquote><p>
 *
 * That is, a translator can be {@link #open opened} and then have any
 * number of {@link #load} and {@link #convert} operations
 * performed on it before being {@link #close closed}.<p>
 *
 */
public interface TranslatorInterface {

    /**
     * Opens a connection with the translator to load & create classes in
     * the context of a given suite.
     *
     * @param  suite  the suite in which classes created during the connection
     *                with this translator will be installed.
     */
    public void open(Suite suite, String classPath) throws NoClassDefFoundError;

    /**
     * Determines if a given name is a valid class name according the JVM specification.
     *
     * @param name   the class name to test
     * @return true is <code>name</code> is a valid class name
     */
    public boolean isValidClassName(String name);

    /**
     * For error recovery, you can get the name of the last class that was processed.
     * If an exception occurs during the convert or close cycle, you can use this to get
     * the class that was being processed.
     */
    public String getLastClassName();
    
    /**
     * Ensures that a given class has had its definition initialized, loading
     * it from a class file if necessary. This does not include verifying the
     * bytecodes for the methods (if any) and converting them to Squawk
     * bytecodes.
     *
     * @param   klass  the class whose definition must be initialized
     * @throws LinkageError if there were any problems while loading and linking the class
     */
    public void load(Klass klass) throws NoClassDefFoundError;

    /**
     * Ensures that all the methods (if any) in a given class have been verified
     * and converted to Squawk bytecodes.
     *
     * @param   klass  the class whose methods are to be verified and converted
     * @throws LinkageError if there were any problems while converting the class
     */
    public void convert(Klass klass) throws NoClassDefFoundError;

    /**
     * Closes the connection with the translator. This computes the closure
     * of the classes in the current suite and ensures they are all loaded and
     * converted.
     *
     * @param suiteType indicates if the the translator can assume that package-private classes and member will not be accessible outside of the 
     *  the unit of translation (the suite). The value pased should be one of the constants specified in {@link java.lang.Suite}
     */
    public void close(int type) throws NoClassDefFoundError;
    
    /**
     * Get the bytes for the resource named <code>name</code>.
     * The first resource found by combining each classPath entry of the currently active suite
     * will be returned.  Meaning that <code>name</code> is relative to the root/default package.
     * 
     * @param name of the resource to fetch
     * @return byte[] null if there is no resource <code>name</code> to be found.
     */
    public byte [] getResourceData(String name);
    
    
    /**
     * Print the usage information for the translator's trace flags to the outputstream.
     *
     * @param out the stream to print on.
     */
    public void printTraceFlags(PrintStream out);
    
    /**
     * Print the usage information for the translator'soption properties to the outputstream.
     * Can print as either generic properties or as command line parameters:
     *  -Dtranslator.foo=value
     *   -foo:value
     *
     * @param out the stream to print on.
     * @param asParameters if true, print prop as "-foo:value", instead of "-Dtranslator.foo=value"
     */
    public void printOptionProperties(PrintStream out, boolean asParameters);
    
    /**
     * If <code>arg</code> represents a valid translator argument, then process it by parsing into property name and value, and set the property value.
     * and return true. Otherwise return false.
     *
     * @param arg
     *
     * @retun true if this was a valid translator argument.
     */
    public boolean processOption(String arg);
  
    /**
     * If <code>arg</code> represents a valid translator argument, then return true. Otherwise return false.
     *
     * @param arg
     *
     * @retun true if this was a valid translator argument.
     */
    public boolean isOption(String arg);
    
}
