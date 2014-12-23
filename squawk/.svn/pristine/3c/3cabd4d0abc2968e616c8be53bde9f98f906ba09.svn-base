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

import com.sun.squawk.mapper.*;
import com.sun.squawk.util.*;
import com.sun.squawk.util.Arrays;
import com.sun.squawk.util.StringTokenizer;
import com.sun.squawk.vm.*;

/**
 * This class provides base functionality for dumping one or more object memories, annotating the
 * dump with symbolic information using the class files from which the object memories were generated.
 *
 */
public class ObjectMemoryMapper {

    /*---------------------------------------------------------------------------*\
     *                      Translator                                           *
    \*---------------------------------------------------------------------------*/

    private static final int BYTES_PER_WORD = HDR.BYTES_PER_WORD;

    private int bytecodes = 0;

    /**
     * Stores a histogram of how many instances of each type are present in the object memory
     * currently being dumped.
     */
     private Histogram classHistogram;
     
    /*---------------------------------------------------------------------------*\
     *                                Relocation                                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Map of object memories to the addresses at which they were relocated in an execution.
     */
    private Hashtable<ObjectMemory, Address> relocationTable;

    /**
     * The address to which the current object memory was relocated or null if it wasn't relocated.
     */
    private Offset currentRelocation;

    /**
     * Updates the {@link relocationTable relocation table} for a given object memory and its parents
     * based on a given properties object mapping object memory URLs to addresses.
     *
     * @param om          an object memory
     * @param properties  map of URLs to addresses
     * @return the address at which <code>om</code> was relocated or null if it wasn't
     */
    private Address setRelocationFor(ObjectMemory om, Properties properties) {
        ObjectMemory parent = om.getParent();
        Address parentRelocation = Address.zero();
        if (parent != null) {
            parentRelocation = setRelocationFor(parent, properties);
            if (parentRelocation == null) {
                return null;
            }
        }

        String value = properties.getProperty(om.getURI());
        Address relocatedTo = null;
        if (value == null) {
            if (parent != null) {
                relocatedTo = parentRelocation.add(parent.getSize());
                relocationTable.put(om, relocatedTo);
            }
        } else {
            relocatedTo = Address.zero().addOffset(Offset.fromPrimitive((int/*S64*/)Long.parseLong(value)));
            relocationTable.put(om, relocatedTo);
        }
        return relocatedTo;
    }

    /**
     * Parses a given file containing relocation information and updates the {@link relocationTable relocation table}
     * for a given object memory and its parents. Each line in a relocation file must be of the format '<url>=<address>'.
     *
     * @param file    the name of the file containing relocation information
     * @param om      an object memory
     * @throws IOException
     */
    private void parseRelocationFile(String file, ObjectMemory om) throws IOException {
        if (!new File(file).exists()) {
            if (!file.equals("squawk.reloc")) {
                throw new RuntimeException(file + " does not exist");
            }
        }

        Properties properties = new Properties();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = br.readLine();
        int lno = 1;
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line, "=");
            if (st.countTokens() != 2) {
                throw new RuntimeException(file + ":" + lno + ": does not match '<url>=<address>' pattern");
            }
            properties.setProperty(st.nextToken(), st.nextToken());
            line = br.readLine();
            lno++;
        }

        relocationTable = new Hashtable<ObjectMemory, Address>();
        if (setRelocationFor(om, properties) == null) {
            relocationTable = null;
        }
    }

    /**
     * Relocates an address that is within the range of the current object memory. No relocation
     * is performed if a relocation file was not provided.
     *
     * @param addr   the address to relocate
     * @return the relocated address
     */
    private Address relocate(Address addr) {
        if (relocationTable == null) {
            return addr;
        }
        return addr.addOffset(currentRelocation);
    }

    /**
     * Relocates an address that is within the range of any of the loaded object memories. No relocation
     * is performed if a relocation file was not provided.
     *
     * @param addr   the address to relocate
     * @return the relocated address
     */
    private Address relocatePointer(Address addr) {
        if (relocationTable == null || addr == Address.zero()) {
            return addr;
        }

        ObjectMemory[] memories = loadedObjectMemories;
        for (ObjectMemory memory : memories) {
            Address start = memory.getStart();
            Address end = memory.getEnd();
            if (addr.hi(start) && addr.loeq(end)) {
                Offset delta = ((Address)relocationTable.get(memory)).diff(memory.getStart());
                return addr.addOffset(delta);
            }
        }
        return addr.addOffset(currentRelocation);
    }

    /*---------------------------------------------------------------------------*\
     *                        Command line parsing and main loop                 *
    \*---------------------------------------------------------------------------*/

    /**
     * Prints the usage message.
     *
     * @param  errMsg  an optional error message
     */
    private void usage(String errMsg) {
        PrintStream out = System.out;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("Usage: mapper [-options] object_memory_file");
        out.println("where options include:");
        out.println();
        out.println("    -boot:<url>     URL for bootstrap suite (default=file://squawk.suite)");
        out.println("    -cp:<directories and jar/zip files separated by '"+File.pathSeparatorChar+"'>");
        out.println("                    paths where classes can be found (default=" + VM.DEFAULT_CLASSPATH + ")");
        out.println("    -cp/a:<directories and jar/zip files separated by '"+File.pathSeparatorChar+"'>");
        out.println("                    append to end of class path");
        out.println("    -cp/p:<directories and jar/zip files separated by '"+File.pathSeparatorChar+"'>");
        out.println("                    prepend in front of class path");
        out.println("    -suitepath:<directories separated by '"+File.pathSeparatorChar+"'>");
        out.println("                    path where suite files can be found");
        out.println("    -o:<file>       dump to 'file' (default=<object_memory_file>.map)");
        out.println("    -all            dump the complete chain of object memories");
        out.println("    -r:<file>       uses file or relocation info (default=squawk.reloc)");
        out.println("    -endian:<value> convert object memories to this endianess (default is endianess of object_memory_file)");
        out.println("    -nofielddefs    do not show field definitions");
        out.println("    -show:<class>   only show details for the specified class (can appear multiple times in the command line)");
        out.println("    -summary        only show class and package sizes");
/*if[TYPEMAP]*/
        out.println("    -notypemap      omit typemap info");
/*end[TYPEMAP]*/
        out.println("    -verbose, -v    provide more output while running");
        out.println("    -h              show this help message");
    }
    
    /**
     * The output dump stream.
     */
    private PrintStream out;

    /**
     * Set true when the next output line is the destination of an oop.
     */
    private boolean oopNext;

    /**
     * Specifies if typemap info will be shown (if available).
     */
    private boolean omitTypemap;

    /**
     * Collects the classes whose field definitions are to be appended to the output.
     */
    private Vector<Klass> fieldDefs = new Vector<Klass>();

    /**
     * The remaining object memories to be dumped.
     */
    private Stack<ObjectMemory> objectMemories;

    /**
     * The loaded object memories.
     */
    private ObjectMemory[] loadedObjectMemories;

    /**
     * The object memory currently being dumped.
     */
    private ObjectMemory currentObjectMemory;

    /**
     * The number of methods in the suite.
     */
    private int totalMethods;

    /**
     * The total number of bytes in the method headers.
     */
    private int totalMethodHeaderLength;
    
    /**
     * Total number of bytes of bytecodes
     */
    private int totalBytecodes;
    
    /**
     * The total number of bytes in the method headers due to oopmaps
     */
    private int totalMethodOopMap;
    
     /**
     * The total number of bytes in the method headers due to exception tables
     */
    private int totalMethodExceptionTableSize;
    
    private int totalRelocationTableSize;
    private int totalTypeTableSize;
    private int totalMinfoSize;

    /**
     * Total number of method locals.
     */
    private int totalMethodLocals;

    /**
     * Total size of max operand stack size.
     */
    private int totalMethodOpStack;

    /**
     * Print any per-object data
     */
    private boolean showObjects = true;
    
    /**
     * Print classes and packages summary
     */
    private boolean showAllClassesAndPackages = true;
    
   /**
    * Print detailed ownership of objects by each class
    */
    private boolean highlightAllClasses = false;
    
    private Hashtable<String, Histogram> ownerHistograms ;

    /**
     * Display a warning message for a supplied command line option that is not
     * enabled in the system.
     *
     * @param option the disabled option
     */
    private void showDisabledOptionWarning(String option) {
        System.err.println("warning: '" + option + "' option is disabled in current build");
    }

    /**
     * Parses the command line arguments to configure an execution of the mapper.
     *
     * @param args   the command line arguments
     * @return boolean true if there were no errors in the arguments
     */
    private final boolean parseArgs(String[] args) throws IOException {
        String outFile = null;
        boolean all = false;
        String relocationFile = null;
        String classPath = VM.DEFAULT_CLASSPATH;
        String prependClassPath = null;
        String appendClassPath = null;
        String bootstrapSuiteURL = "file://squawk.suite";
        Boolean bigEndian = null;

        int argc = 0;
        for (; argc != args.length; ++argc) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            }
            if (arg.startsWith("-all")) {
                all = true;
            } else if (arg.startsWith("-boot:")) {
                bootstrapSuiteURL = arg.substring("-boot:".length());
            } else if (arg.startsWith("-cp:")) {
                classPath = arg.substring("-cp:".length());
            } else if (arg.startsWith("-cp/p:")) {
                prependClassPath = arg.substring("-cp/p:".length());
            } else if (arg.startsWith("-cp/a:")) {
                appendClassPath = arg.substring("-cp/a:".length());
            } else if (arg.startsWith("-suitepath:")) {
                String path = arg.substring("-suitepath:".length());
                ObjectMemoryLoader.setFilePath(path);
            } else if (arg.startsWith("-r:")) {
                relocationFile = arg.substring("-r:".length());
            } else if (arg.startsWith("-o:")) {
                outFile = arg.substring("-o:".length());
            } else if (arg.startsWith("-trace")) {
                if (arg.startsWith("-tracefilter:")) {
                    String optArg = arg.substring("-tracefilter:".length());
                    if (Klass.TRACING_ENABLED) {
                        Tracer.setFilter(optArg);
                    } else {
                        showDisabledOptionWarning(arg);
                    }
                } else {
                    if (Klass.TRACING_ENABLED) {
                        Tracer.enableFeature(arg.substring("-trace".length()));
                        if (arg.equals("-traceconverting")) {
                            Tracer.enableFeature("loading"); // -traceconverting subsumes -traceloading
                        }
                    } else {
                        showDisabledOptionWarning(arg);
                    }
                }
            } else if (arg.equals("-nofielddefs")) {
                fieldDefs = null;
            } else if (arg.equals("-summary")) {
                showObjects = false;
                fieldDefs = null;
            } else if (arg.equals("-notypemap")) {
                omitTypemap = true;
            } else if (arg.startsWith("-endian:")) {
                String value = arg.substring("-endian:".length());
                if (value.equals("big")) {
                    bigEndian = Boolean.TRUE;
                } else if (value.equals("little")) {
                    bigEndian = Boolean.FALSE;
                } else {
                    usage("invalid endianess: " + value);
                    return false;
                }
            } else if (arg.startsWith("-show:")) {
                String classname = arg.substring("-show:".length());
                if ("all".equals(classname)) {
                    highlightAllClasses = true;
                } else {
                    highlightClasses.add(classname);
                }
                showAllClassesAndPackages = false; // only show info for specified class(es)
                showObjects = false;
                fieldDefs = null;
            } else if (arg.equals("-verbose") | arg.equals("-v")) {
                    System.setProperty("translator.verbose", "true");
                    VM.setVerbose(true);
                    VM.setVeryVerbose(true);
            } else if (arg.equals("-h")) {
                usage(null);
                return false;
            } else {
                usage("unknown option: " + arg);
                return false;
            }
        }

        if (argc == args.length) {
            usage("missing object memory file");
            return false;
        }

        System.setProperty(ObjectMemory.BOOTSTRAP_URI_PROPERTY, bootstrapSuiteURL);

        String objectMemoryFile = args[argc];
        if (outFile == null) {
            outFile = objectMemoryFile + ".map";
        }

        // Set endianess
        if (bigEndian == null) {
            VM.bigEndian = VM.isBigEndian(new File(objectMemoryFile));
        } else {
            VM.bigEndian = bigEndian.booleanValue();
        }

        if (prependClassPath != null) {
            classPath = prependClassPath + File.pathSeparatorChar + classPath;
        }
        if (appendClassPath != null) {
            classPath += File.pathSeparatorChar + appendClassPath;
        }
        VM.initializeTranslator(classPath);
        out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile), 10000));
        ObjectMemory leafObjectMemory = loadObjectMemories(objectMemoryFile, all).objectMemory;

        if (relocationFile != null) {
            parseRelocationFile(relocationFile, leafObjectMemory);
        }

        out.println("loaded object memories: " + objectMemories.size());
        for (ObjectMemory objectMemory : objectMemories) {
            showSummary(objectMemory);
        }

        return true;
    }

    void printOwnedHistogram(Hashtable<String, Histogram> ownerHistograms, OwnerStats owner, String prefix) {
        Histogram histogram = ownerHistograms.get(owner.name);
        Assert.that(histogram.totalSize == owner.size);
        out.println(prefix + owner.name + " = " + owner.size);
        histogram.dump(out, prefix + "    ", false);
    }
    
    /**
     * Print all of the objects owned by "thwOwner", in address order.
     */
    void printOwnedData(OwnerStats theOwner) {
        // 1) Get owned objects into an array
        Vector<Object> ownedObjectBuffer = new Vector<Object>();
    	for (Map.Entry<Object, OwnerStats> entry : ownerTable.entrySet()) {
            Object object = entry.getKey();
            OwnerStats owner = entry.getValue();
            if (owner == theOwner) {
                ownedObjectBuffer.addElement(object);
            }
        }
        Object[] ownedObjects = new Object[ownedObjectBuffer.size()];
        ownedObjectBuffer.copyInto(ownedObjects);
        
        // 2. sort by address:
        Arrays.sort(ownedObjects, new Comparer() {
             public int compare(Object a, Object b) {
                 if (((Address)a).lo((Address)b)) {
                     return 0;
                 } else if (((Address)a).lo((Address)b)) {
                     return -1;
                 } else {
                     return 1;
                 }
            }
        });
        
        boolean oldShowObjects   = showObjects;
        showObjects               = true;
        Address nextBlock = null;
        
        // 3. Print objects
        Object object = null;
        try {
            for (int i = 0; i < ownedObjects.length; i++) {
                object = ownedObjects[i];
                if (ownerTable.get(object) == theOwner) {
                    Address block = objectTable.getObjectBlock(object);
                    if (i != 0 && block.ne(nextBlock)) {
                        out.println("..."); // if blocks not contiguous, let reader know
                    }
                    int tag = NativeUnsafe.getUWord(block, 0).and(UWord.fromPrimitive(HDR.headerTagMask)).toInt();
                    switch ((int)tag) {
                        case (int) HDR.basicHeaderTag:   nextBlock = decodeInstance(block); break;
                        case (int) HDR.arrayHeaderTag:   nextBlock = decodeArray(block);    break;
                        case (int) HDR.methodHeaderTag:  nextBlock = decodeMethod(block);   break;
                        default: Assert.shouldNotReachHere("Invalid header word");
                        break;
                    }
                }
            }
        } catch (RuntimeException e) {
            System.err.println(e + " while updating stats for " + object + " owned by " + theOwner.name);
            throw e;
        }
        showObjects   = oldShowObjects;
    }
    
    /**
     * Print a hostogram of the objects owned by the class, and optionally the objects owned by the class.
     *
     * @return true if there was a histogram prepared for this class.
     */
    private boolean printClassHistogram(String classname) {
        ClassOwnerStats owner = ClassOwnerStats.getKlassStats(classname);
        if (owner == null) {
            return false;
        } else {
            out.println("Histogram of bytes owned by " + classname);
            printOwnedHistogram(ownerHistograms, owner, "");
            out.println();
            if (!highlightAllClasses) {
                out.println("Dump of objects owned by " + classname);
                printOwnedData(owner);
                out.println();
            }
            return true;
        }
    }
    
    /**
     * Execute the mapper and produce the dump.
     */
    private final void run() {
        try {
            while (!objectMemories.isEmpty()) {
                currentObjectMemory = objectMemories.pop();
                if (relocationTable != null) {
                    Address relocatedTo = relocationTable.get(currentObjectMemory);
                    currentRelocation = relocatedTo.diff(currentObjectMemory.getCanonicalStart());
                }
                resetStats();
                
                Address block = currentObjectMemory.getStart();
                Address end = block.add(currentObjectMemory.getSize());
                
                // in intial pass, walk graph from classes, claiming ownership:
                while (block.lo(end)) {
                    Address object = GC.blockToOop(block);
                    if (GC.getKlass(object) == Klass.KLASS) {
                        Klass klass = VM.asKlass(object);
                        registerKlass(klass, object);
                    }
                    block = object.add(GC.getBodySize(GC.getKlass(object), object));
                }
                block = currentObjectMemory.getStart();
                
                out.println("+++++++++++++++++++++  start of object memory with URI " + currentObjectMemory.getURI() + " +++++++++++++++++++++++");
                shouldRecordObjects = true;
                while (block.lo(end)) {
                    int tag = NativeUnsafe.getUWord(block, 0).and(UWord.fromPrimitive(HDR.headerTagMask)).toInt();
                    switch ((int)tag) {
                        case (int) HDR.basicHeaderTag:   block = decodeInstance(block); break;
                        case (int) HDR.arrayHeaderTag:   block = decodeArray(block);    break;
                        case (int) HDR.methodHeaderTag:  block = decodeMethod(block);   break;
                        default: Assert.shouldNotReachHere("Invalid header word");
                            break;
                    }
                }
                shouldRecordObjects = false;
                out.flush();
                
                // Appends the field definitions
                if (fieldDefs != null) {
                	for (Klass klass : fieldDefs) {
                        printFieldDefinitions(klass, false);
                        printFieldDefinitions(klass, true);
                    }
                    out.println();
                    fieldDefs = new Vector<Klass>();
                }
                
                ownerHistograms = makeOwnerHistograms();
                Assert.always(registeredSuite);
                int memorySize = currentObjectMemory.getSize();
                out.println("Suite Statistics (bytes):");
                out.println("    Oopmap = " + GC.calculateOopMapSizeInBytes(memorySize));
                out.println("    Suite memory = " + memorySize);
                String padding = "        ";
                printOwnedHistogram(ownerHistograms, SUITE_STATS, padding);
                printOwnedHistogram(ownerHistograms, PROPERTIES_STATS, padding);
                printOwnedHistogram(ownerHistograms, RESOURCE_FILES_STATS, padding);
                printOwnedHistogram(ownerHistograms, METADATA_STATS, padding);
                printOwnedHistogram(ownerHistograms, SHARED_OWNER, padding);
                printOwnedHistogram(ownerHistograms, ALL_CLASSES_STATS, padding);
                if (UNASSIGNED_STATS.size != 0) { // should be zero!
                    printOwnedHistogram(ownerHistograms, UNASSIGNED_STATS, padding);
                }
                out.println();
                
                // report details of requested classes:
                if (highlightAllClasses) {
                    for (String classname : ClassOwnerStats.getKlassNames()) {
                        ClassOwnerStats owner = ClassOwnerStats.getKlassStats(classname);
                        printClassHistogram(classname);
                    }
                } else {
                    Vector<String> remainingHighlightClasses = new Vector<String>();
                    for (String classname : highlightClasses) {
                        if (!printClassHistogram(classname)) {
                            remainingHighlightClasses.addElement(classname);
                        }
                    }
                    highlightClasses = remainingHighlightClasses;
                }
                
                // Show the histogram of counts
                out.println("Histogram of instance counts:");
                classHistogram.dump(out, "", true);
                out.println("Total: " + classHistogram.totalCount);
                out.println();

                // Show the histogram of sizes
                out.println("Histogram of instance sizes:");
                classHistogram.dump(out, "", false);
                out.println("Total: " + classHistogram.totalSize);
                out.println();

                // Show method stats
                out.println("Total Bytecode count = " + bytecodes);
                out.println("Method count = " + totalMethods);
                out.println("Average method header = " + ((double)totalMethodHeaderLength)/totalMethods);
                out.println("    avg header oopmap length = " + ((double)totalMethodOopMap)/totalMethods);
                out.println("    avg header exception table length = " + ((double)totalMethodExceptionTableSize)/totalMethods);
                out.println("    avg header relocation table length = " + ((double)totalRelocationTableSize)/totalMethods);
                out.println("    avg header type table length = " + ((double)totalTypeTableSize)/totalMethods);
                out.println("    avg header minfo size = " + ((double)totalMinfoSize)/totalMethods);
                out.println("Average locals = " + ((double)totalMethodLocals)/totalMethods);
                out.println("Average op stack = " + ((double)totalMethodOpStack)/totalMethods);
                out.println();

                if (showAllClassesAndPackages) {
                    printPackageTable();
                    printClassTable();
                }
            }
            
            // look for requested classes that we couldn't find:'
            int numClasses = highlightClasses.size();
            for (int i = 0; i < numClasses; i++) {
                String classname = (String)highlightClasses.elementAt(i);
                out.println("*** No class named " + classname + " ignoring -show:" + classname + " option");
            }
        } finally {
            out.flush();
            out.close();
        }
    }
    
    /**
     * Get a reference field by field's type and offset.
     *
     * @param objAddr the address of the object to check 
     * @param Offset  the word offset of the field in the object
     * @return referece
     */
    private Object getField(Address objAddr, int Offset) {
        return NativeUnsafe.getObject(objAddr, Offset);
    }

    /**
     * Get a reference field by field's type and name.
     *
     * @param objAddr the address of the object to check 
     * @param objKlass the klass of the object
     * @param fieldKlass the klass of the field
     * @param fieldname the name of the field
     * @return referece
     */
    private Object getField(Address objAddr, Klass objKlass, Klass fieldKlass, String fieldname) {
        Field f = objKlass.lookupField(fieldname, fieldKlass, false);
        if (f == null) throw new RuntimeException("Can't find field " + fieldname + " of type " + fieldKlass + " in " + objKlass);
        return getField(objAddr, f.getOffset());
    }
    
    
    /**
     * Get a reference field by the name of the field's type and the field's name.
     *
     * @param objAddr the address of the object to check 
     * @param objKlass the klass of the object
     * @param fieldKlassName the name of the klass of the field
     * @param fieldname the name of the field
     * @return the size of the field's objects in bytes
     */
    private Object getField(Address objAddr, Klass objKlass, String fieldKlassName, String fieldname) {
        Klass fieldKlass = Klass.getClass(fieldKlassName, false);
        if (fieldKlass == null)  throw new RuntimeException("Can't find class " + fieldKlassName);
        return getField(objAddr, objKlass, fieldKlass, fieldname);
    }
    
    /**
     * Table of oop -> Stat
     *
     * This gets populated when walking over suite memory.
     */
    private ObjectTable objectTable;
    
    /*---------------------------------------------------------------------------*\
     *                              Object Ownership                             *
    \*---------------------------------------------------------------------------*/

    /** 
     * Table that tracks object ownership. Objects that are only refered to by a single class or resource
     * will be charged to that class or resource. Objects refered to by more than one owner are
     * charged to SHARED_OWNER.
     *
     * This is a map of object->owners, where owner is a OwnerStats.
     */
    private Hashtable<Object, OwnerStats> ownerTable;
    
    private OwnerStats SHARED_OWNER;
    private OwnerStats RESOURCE_FILES_STATS;
    private OwnerStats PROPERTIES_STATS;
    private OwnerStats SUITE_STATS;
    private OwnerStats METADATA_STATS;
    
    /**
     * Unassigned data is not shared, but it doesn't belong to onew of the top-level items we are tracking
     * (klass, suite, resource file, etc.)
     */
    private OwnerStats UNASSIGNED_STATS;
    
    /**
     * Summary of all object owned by classes. Generted in makeOwnerHistograms.
     */
    private OwnerStats ALL_CLASSES_STATS;
    
    /**
     * List of class names specified by user that should have detailed statistics displayed.
     */
    private Vector<String> highlightClasses = new Vector<String>();
    
    /**
     * Reset statistics for each object memory being dumped.
     */
    private void resetStats() {
        objectTable = new ObjectTable();
        ownerTable = new Hashtable<Object, OwnerStats>();
        ClassOwnerStats.reset();
        classHistogram = new Histogram();
        packageTable = new Hashtable<String, OwnerStats>();
        SHARED_OWNER = new OwnerStats("Shared Objects");
        RESOURCE_FILES_STATS = new OwnerStats("Resource Files");
        PROPERTIES_STATS = new OwnerStats("Properties");
        SUITE_STATS = new OwnerStats("Suite Data");
        METADATA_STATS = new OwnerStats("Meta Data");
        UNASSIGNED_STATS = new OwnerStats("Unassigned Data");
        ALL_CLASSES_STATS = new OwnerStats("All Classes");
    }

    /**
     * Make newOwner the owner of objct.
     */
    private void updateOwner(OwnerStats newOwner, Object object) {
        Assert.that(object != Address.zero());
        Assert.that(Address.fromObject(object).loeq(currentObjectMemory.getCanonicalEnd()));
        Object oldOwner = ownerTable.put(object, newOwner);
        Assert.that(oldOwner == null || newOwner == SHARED_OWNER);
    }
    
    /**
     * Attempt to "color" all of the objects in this object graph by the owner. When
     * we detect that another owner is attempting to color an already colored object,
     * we then color it (and the objects reachable from it) in the special "SHARED" color.
     * 
     * The owner's size is NOT updated in this pass. A Pass is taken over the ownerTable
     * later to update owner sizes.
     *
     * @param owner the entity that is attempting to claim ownership of the objects in the graph.
     * @param object the root of the object graph to count
     * @param countFirstKlass if false, ignore klasses
     */
    private void claimObjectGraph(OwnerStats owner, Object object, boolean countFirstKlass) {
        if (object != Address.zero() && currentObjectMemory.containsAddress(Address.fromObject(object))) {
            Klass objklass = GC.getKlass(object);
            int cid = objklass.getSystemID();
            if (cid == CID.KLASS && !countFirstKlass) {
                // only account for klass objects when we ask for them.
                return;
            } else {
                OwnerStats oldOwner = ownerTable.get(object);
                OwnerStats newOwner = null;
                if (oldOwner == null) {
                    newOwner = owner;
                    if (owner == UNASSIGNED_STATS) {
                        out.println("*** Found unowned object of type " + objklass);
                    }
                } else if (oldOwner == owner || owner == UNASSIGNED_STATS) {
                    newOwner = null;
                } else if (oldOwner != SHARED_OWNER) {
                    newOwner = SHARED_OWNER;
                }
                
                if (newOwner == null) {
                    // nothing to change, so leave:
                    return;
                }
                
                if (objklass.isSquawkArray()) {
                    int length = GC.getArrayLengthNoCheck(object);
                    updateOwner(newOwner, object);
                    
                    if (owner == UNASSIGNED_STATS) {
                        return;
                    }
                    
                    Klass elementType = objklass.getComponentType();
                    if (elementType!= null && (elementType.isArray() || (elementType.isReferenceType() && !elementType.isSynthetic()))) {
                        for (int i = 0; i < length; i++) {
                            Object element = NativeUnsafe.getObject(object, i);
                            if (element != null) {
                                try {
                                claimObjectGraph(owner, element, false);
                                } catch (RuntimeException e) {
                                    System.err.println("Error decoding element (type = " + elementType + ") of array (type = " + objklass + ")");
                                    throw e;
                                }
                            }
                        }
                    }
                } else {
                    int nWords = objklass.getInstanceSize();
                    updateOwner(newOwner, object);
                    
                    if (owner == UNASSIGNED_STATS) {
                        return;
                    }
                    
                    for (int i = 0 ; i < nWords ; i++) {
                        if (Klass.isInstanceWordReference(objklass, i)) {
                            Object refField = NativeUnsafe.getObject(object, i);
                            if (refField != null) {
                                claimObjectGraph(owner, refField, false);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Walk over the owner table, and make a histogram for each type of owner 
     * (SHARED, RESOURCE, PROPERTIES, SUITE, META_DATA, ALL_CLASSES). Place each 
     * new histogram in a hashtable keyed by the owner's name.
     *
     * This also sets the size statistics for each owner.
     *
     * This must be called after the suite has been completely analyzed.
     *
     * @return a Hashtable of Histograms, keyed by the name of the owner
     */
    private Hashtable<String, Histogram> makeOwnerHistograms() {
        Hashtable<String, Histogram> allHistograms = new Hashtable<String, Histogram>();
        OwnerStats[] allStatEntries = {SHARED_OWNER, RESOURCE_FILES_STATS, PROPERTIES_STATS, SUITE_STATS, METADATA_STATS, UNASSIGNED_STATS, ALL_CLASSES_STATS};
        for (int i = 0; i < allStatEntries.length; i++) {
            allHistograms.put(allStatEntries[i].name, new Histogram());
        }
        Histogram allClassesHistogram = (Histogram)allHistograms.get(ALL_CLASSES_STATS.name);
        
        // add Histograms for requested classes:
        if (highlightAllClasses) {
            for (String classname : ClassOwnerStats.getKlassNames()) {
                 allHistograms.put(classname, new Histogram());
            }
        } else {
            for (String classname : highlightClasses) {
                allHistograms.put(classname, new Histogram());
            }
        }
        
        for (Map.Entry<Object, OwnerStats> entry : ownerTable.entrySet()) {
			Object object = entry.getKey();
			OwnerStats owner = entry.getValue();
	        try {
	            int objectSize = objectTable.getObjectSize(object);
	            
	            // get the histogram for this owner:
	            Klass objKlass = GC.getKlass(object);
	            Histogram histogram = (Histogram)allHistograms.get(owner.name);
	            if (histogram == null || owner.isClass()) { // owner is a class, so use summary histogram
	                allClassesHistogram.updateFor(objKlass, objectSize);
	                ALL_CLASSES_STATS.updateSize(objectSize); // keep the stats value and the histogram's totals in sync.'
	            }
	            
	            if (histogram != null) {
	                histogram.updateFor(objKlass, objectSize);
	            }
	            
	            // also update the owner's size statistics:
	            owner.updateSize(objectSize);
	        } catch (RuntimeException e) {
	            System.err.println(e + " while updating stats for " + object + " owned by " + owner.name);
	            throw e;
	        }
        }
        
        return allHistograms;
    }
    
    /**
     * Create the ClassOwnerStats for klass, and claim the object graph rooted from klassAddr.
     *
     * @param klass the klass object of the klass in the object memory.
     * @param klassAddr the address of the the klass in the object memory.
     */
    private void registerKlass(Klass klass, Address klassAddr) {
        String fullName = klass.getInternalName();
        OwnerStats klassOwner = new ClassOwnerStats(fullName);
        claimObjectGraph(klassOwner, klassAddr, true);
    }
    
    
    /*---------------------------------------------------------------------------*\
     *                   Class and package size analysis                         *
    \*---------------------------------------------------------------------------*/
    /**
     * This is a map of package name -> OwnerStats(s).
     */
    Hashtable<String, OwnerStats> packageTable;
    
    
    private void printClassTable() {
        OwnerStats[] stats  = mapToArray(ClassOwnerStats.allKlassStats);
        Arrays.sort(stats, new OwnerStats.CompareByName());
        
        out.println("# classes:" + stats.length);
        out.println("Class statistics sorted alphabetically (bytes):");
        for (int i = 0; i < stats.length; i++) {
            OwnerStats stat = stats[i];
            out.println(stat.name + ": " + stat.size);
        }
        Arrays.sort(stats, new Comparer(){
            public int compare(Object o1, Object o2) {
                OwnerStats stat1 = (OwnerStats) o1;
                OwnerStats stat2 = (OwnerStats) o2;
                if (stat1.size == stat2.size) {
                    return 0;
                }
                if (stat1.size < stat2.size) {
                    return +1;
                }
                return -1;
            }
        });
        out.println();
        out.println("# classes:" + stats.length);
        out.println("Class statistics sorted by size (bytes):");
        for (int i = 0; i < stats.length; i++) {
            OwnerStats stat = stats[i];
            out.println(stat.name + ": " + stat.size);
        }
    }
    
    /**
     * Create an array conatining all of the elements of the hashtable t.
     *
     * @param t the hashtable
     * @returns array of elements.
     */
    private OwnerStats[] tableToArray(Hashtable<String, ? extends OwnerStats> t) {
        OwnerStats[] stats  = new OwnerStats[t.size()];
        t.values().toArray(stats);
        return stats;
    }
    
    
        /**
     * Create an array conatining all of the elements of the hashtable t.
     *
     * @param t the hashtable
     * @returns array of elements.
     */
    private OwnerStats[] mapToArray(HashMap<String, ? extends OwnerStats> t) {
        OwnerStats[] stats  = new OwnerStats[t.size()];
        t.values().toArray(stats);
        return stats;
    }
    
    /**
     * Count the occurences of ch in the string.
     * @param str the string
     * @param ch the character
     * @return the count
     */
    private int charCount(String str, char ch) {
        int len = str.length();
        int result = 0;
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == ch) {
                result++;
            }
        }
        return result;
    }
    
    /**
     * Given a full class name, add the specified size to all of the package's
     * statitiscs that contain the class. For example, given "java.lang.Object", 
     * update the size for the pakacges "java" and "java.lang".
     * 
     * @param name a full class name
     * @param size the size (in bytes) of objects owned by the class.
     */
    private void doPackageAccounting(String name, int size) {
        int last;
        if (name.indexOf("[L") == 0) {
            name = name.substring(2);
        }
        while ((last = name.lastIndexOf('.')) != -1) {
            name = name.substring(0, last);
            OwnerStats oldVal = packageTable.get(name);
            if (oldVal == null) {
                oldVal = new OwnerStats(name);
                packageTable.put(name, oldVal);
            } 
            oldVal.updateSize(size);
        }
    }
    
    /**
     * Print the size in bytes of all of the objects owned by each packge.
     */
    void printPackageTable() {
    	for (OwnerStats stat : ClassOwnerStats.allKlassStats.values()) {
            doPackageAccounting(stat.name, stat.size);
        }
        
        out.println("Package statistics (bytes):");
        OwnerStats[] stats  = tableToArray(packageTable);
        Arrays.sort(stats, new OwnerStats.CompareByName());
        for (OwnerStats stat : stats) {
            int depth = charCount(stat.name, '.');
            if (depth > 0) {
                for (int t = 0; t < depth; t++) {
                    out.print('\t');
                }
                out.print(stat.name.substring(stat.name.lastIndexOf('.')));
            } else {
                out.print(stat.name);
            }
            out.println(": " + stat.size);
        }
        
        out.println();
    }
        
    /**
     * Initialize the contents of memory from one or more object memory files.
     *
     * @param file the tail object memory file name
     * @param all  if true, add all object memories to {@link #objectMemories}
     * @return the object memory in <code>file</code>
     */
    private ObjectMemoryFile loadObjectMemories(String file, boolean all) throws IOException {
        String url = "file://" + file;
        ObjectMemoryFile leafObjectMemoryFile = ObjectMemoryLoader.load(Connector.openDataInputStream(url), url, false);
        objectMemories = new Stack<ObjectMemory>();
        ObjectMemory objectMemory = leafObjectMemoryFile.objectMemory;
        Stack<ObjectMemory> allObjectMemories = new Stack<ObjectMemory>();

        while (objectMemory != null) {
            if (objectMemory == leafObjectMemoryFile.objectMemory || all) {
                objectMemories.push(objectMemory);
            }
            allObjectMemories.push(objectMemory);
            objectMemory = objectMemory.getParent();

        }

        // Register the classes and methods defined in the object memories
        loadedObjectMemories = new ObjectMemory[allObjectMemories.size()];
        int index = 0;
        while (!allObjectMemories.isEmpty()) {

            objectMemory = (ObjectMemory)allObjectMemories.pop();
            loadedObjectMemories[index++] = objectMemory;

            // Register the classes
            Address block = objectMemory.getStart();
            Address end = block.add(objectMemory.getSize());
            while (block.lo(end)) {
                Address object = GC.blockToOop(block);
                Klass klass = GC.getKlass(object);
                if (suiteKlass == null && suiteKlassName.equals(klass.getInternalName())) {
                    suiteKlass = klass;
                }
                block = object.add(GC.getBodySize(klass, object));
            }

            // Register the methods
            int staticMethodsOffset = Klass.KLASS.lookupField("staticMethods", Klass.OBJECT_ARRAY, false).getOffset();
            int virtualMethodsOffset = Klass.KLASS.lookupField("virtualMethods", Klass.OBJECT_ARRAY, false).getOffset();
            block = objectMemory.getStart();
            while (block.lo(end)) {
                Address object = GC.blockToOop(block);
                if (GC.getKlass(object) == Klass.KLASS) {
                    Klass klass = VM.asKlass(object);
                    registerMethods(klass, NativeUnsafe.getObject(object, virtualMethodsOffset), false);
                    registerMethods(klass, NativeUnsafe.getObject(object, staticMethodsOffset), true);
                }
                block = object.add(GC.getBodySize(GC.getKlass(object), object));
            }
        }

        return leafObjectMemoryFile;
    }

    /**
     * Show a summary of an object memory.
     *
     * @param memory   the object memory to summarize
     */
    private void showSummary(ObjectMemory memory) {
        if (memory != null) {
            int size = memory.getSize();
            out.println(memory.getURI() + " {");
            out.println("                size = " + size);
            out.println("                hash = " + memory.getHash());
            out.println("                root = " + Address.fromObject(memory.getRoot()).diff(memory.getStart()));
            out.println("     canonical start = " + memory.getCanonicalStart());
            out.println("       canonical end = " + memory.getCanonicalStart().add(size));
            out.println("         oopmap size = " + GC.calculateOopMapSizeInBytes(size));
           
            if (relocationTable != null) {
                Address relocatedTo = relocationTable.get(memory);
                out.println("    relocation start = " + relocatedTo);
                out.println("      relocation end = " + relocatedTo.add(size));
            }
            out.println("}");
        }
    }

    /*---------------------------------------------------------------------------*\
     *                        Space padding                                      *
    \*---------------------------------------------------------------------------*/

    /**
     * An array of characters used to speed up padding of string buffers.
     */
    private static final char[] SPACES;
    static {
        SPACES = new char[1000];
        for (int i = 0; i != SPACES.length; ++i) {
            SPACES[i] = ' ';
        }
    }

    /**
     * Appends space characters to the end of a given string buffer until its length
     * is equal to a given width. If the given width is greater than or equal to the
     * string buffer's current length, then no padding is performed.
     *
     * @param buf   the StringBuffer to pad
     * @param width the length 'buf' should be after padding
     */
    private void pad(StringBuffer buf, int width) {
        int pad = width - buf.length();
        if (pad > 0) {
            buf.append(SPACES, 0, pad);
        }
    }

    /*---------------------------------------------------------------------------*\
     *                        Print line primitives                              *
    \*---------------------------------------------------------------------------*/

    /**
     * Print a line of the map file corresponding to one word.
     *
     * @param addr    the address of the word
     * @param text    the text to be written after the hex
     * @param isOop   true if the word at <code>addr</code> is a reference
     */
    private void printWordLine(Address addr, String text, boolean isOop) {
        printLine(addr, BYTES_PER_WORD, text, (isOop && NativeUnsafe.getUWord(addr, 0).ne(UWord.zero())));
    }

    /**
     * Print a line of the map file corresponding to one or more bytes.
     *
     * @param addr    the address of the first byte
     * @param length  the number of bytes
     * @param text    the text to be written after the hex
     * @param isOop   true if the word at <code>addr</code> is a reference
     */
    private void printLine(Address addr, int length, String text, boolean isOop) {

        Assert.that(length <= 9);
        StringBuffer buf = new StringBuffer(120);

        // Denote a pointer/reference by prefixing the line with a '+'
        if (NativeUnsafe.isReference(addr)) {
            buf.append('+');
        } else {
            Assert.that(!isOop, "oop map bit not set for pointer at " + addr);
            buf.append(' ');
        }

        // Pad the address with leading spaces
        String address = relocate(addr).toString();
        pad(buf, 11 - address.length());

        // Dump the address
        buf.append(address);


        // Denote the start of an object with '*'
        if (oopNext) {
            buf.append(" * ");
            oopNext = false;
        } else {
            buf.append(" : ");
        }

        // Only print up to 8 bytes per line
        int startBytes = buf.length();
        for (int i = 0 ; i != 8 ; i++) {
            if (i < length) {
                int b = NativeUnsafe.getByte(addr, i) & 0xFF;
                if (b < 16) {
                    buf.append(" 0").append(Integer.toHexString(b));
                } else {
                    buf.append(" ").append(Integer.toHexString(b));
                }
/*if[TYPEMAP]*/
                if (!omitTypemap) {
                    byte type = NativeUnsafe.getType(addr.add(i));
                    buf.append(':').append(AddressType.getMnemonic(type));
                }
/*end[TYPEMAP]*/
            }
        }

        // Add padding after the bytes on the assumption that each byte occupies 3 spaces
/*if[TYPEMAP]*/
        pad(buf, (startBytes + 8 * (omitTypemap ? 3 : 5)));
/*else[TYPEMAP]*/
//      pad(buf, (startBytes + 8 * 3));
/*end[TYPEMAP]*/


        // Pad after the bytes
        buf.append("   ");

        // Dump the line
        out.print(buf);
        if (length > 8) {
            out.println(text + " ...");
            printLine(addr.add(8), length - 8, "... " + text, false);
        } else {
            out.println(text);
        }
    }

    /**
     * Print a line of the map file corresponding to one or more bytes.
     *
     * @param addr    the address of the first byte
     * @param length  the number of bytes
     * @param text    the text to be written after the hex
     */
    private void printLine(Address addr, int length, String text) {
        printLine(addr, length, text, false);
    }

    /**
     * Formats a base type, optional field name or element index and value and appends
     * this to a given string buffer.
     *
     * @param buf   the StringBuffer to append to
     * @param type  the base type of the value
     * @param name  the field name or element index
     * @param value the value
     */
    private void appendTypeValueName(StringBuffer buf, String type, String name, String value) {
        buf.append("  ").append(type);
        pad(buf, 9);
        if (name != null) {
            buf.append(name).append(" = ");
        }
        buf.append(value).append(' ');
    }

    /**
     * Prints a line for a primitive value.
     *
     * @param address       the address of the value
     * @param size          the size (in byters) of the value
     * @param type          the type of the value
     * @param name          the name of the value or null
     * @param value         the value
     */
    private void printPrimitiveLine(Address address, int size, String type, String name, String value) {
        StringBuffer buf = new StringBuffer(40);
        appendTypeValueName(buf, type, name, value);
        printLine(address, size, buf.toString());
    }

    /**
     * Prints a line for a pointer value. Some extra annotation is also appended for pointers of certain types.
     *
     * @param pointerAddress      the address of the pointer
     * @param fieldName           the name of an instance field or index of an array
     * @param declaredType        the declared type of the object pointed to or null if this is a pointer to
     *                            within an object
     */
    private void printPointerLine(Address pointerAddress, String name, Klass declaredType) {
        Address ref = (Address)NativeUnsafe.getObject(pointerAddress, 0);
        StringBuffer buf = new StringBuffer(80);
        boolean isInternalPointer = (declaredType == null);
        appendTypeValueName(buf, isInternalPointer ? "iref" : "ref", name, relocatePointer(ref).toString());

        if (!isInternalPointer) {
            pad(buf, 35);
            if (ref.isZero()) {
                buf.append(declaredType.getName());
            } else {
                Klass actualType = GC.getKlass(ref);
                String actualtypeName = actualType.getName();
                buf.append(actualtypeName);
                switch (actualType.getSystemID()) {
                    case CID.KLASS: {
                        pad(buf, 70);
                        buf.append("// ").append(VM.asKlass(ref).getName());
                        break;
                    }
                    case CID.BYTECODE_ARRAY: {
                        pad(buf, 70);
                        String signature = getMethodSignature(ref);
                        buf.append("// ").append(signature);
                        break;
                    }
                    case CID.STRING:
                    case CID.STRING_OF_BYTES: {
                        pad(buf, 70);
                        String value = VM.asString(ref);
                        buf.append("// \"");
                        for (int j = 0; j != value.length(); ++j) {
                            char ch = value.charAt(j);
                            if (ch < ' ' || ch > '~') {
                                ch = '~';
                            }
                            buf.append(ch);
                        }
                        buf.append('"');
                        break;
                    }
                    default:
                        if (actualtypeName.startsWith("com.sun.squawk.")) {
                            if (actualtypeName.equals("com.sun.squawk.KlassMetadata") || actualtypeName.equals("com.sun.squawk.KlassMetadata$Full")) {
                                Address definedClassRef = NativeUnsafe.getAddress(ref, FieldOffsets.decodeOffset(FieldOffsets.com_sun_squawk_KlassMetadata$definedClass));
                                pad(buf, 70);
                                buf.append("// for klass ").append(VM.asKlass(definedClassRef).getName());
                            }
                        }
                }
            }
        }
        printWordLine(pointerAddress, buf.toString(), true);
    }

    /*---------------------------------------------------------------------------*\
     *                        Header printing                                    *
    \*---------------------------------------------------------------------------*/

    private String classWordAnnotation(Address object) {
        Address classOrAssociation = NativeUnsafe.getAddress(object, HDR.klass);
        return "    { class = " + classOrAssociation + " }";
    }

    /**
     * Prints the type of a non-array instance.
     *
     * @param oop the address of a non-array instance
     */
    private void printInstanceHeader(Address oop) {
        if (showObjects) {
            Klass klass = GC.getKlass(oop);
            printWordLine(oop.sub(BYTES_PER_WORD), "instance " + klass.getInternalName() + classWordAnnotation(oop), true);
        }
        oopNext = true;
    }

    /**
     * Prints the type and length of an array.
     *
     * @param oop the address of an array
     * @return  the length of the array
     */
    private int printArrayHeader(Address oop) {
        Klass klass = GC.getKlass(oop);
        int length = GC.getArrayLengthNoCheck(oop);
        if (showObjects) {
            printWordLine(oop.sub(HDR.arrayHeaderSize), "[" + length + "]", false);
            printWordLine(oop.sub(HDR.basicHeaderSize), "array " + klass.getInternalName() + classWordAnnotation(oop), true);
        }
        oopNext = true;
        return length;
    }

    /**
     * Prints the detail in a method header.
     *
     * @param block   the block containing a method
     * @param oop     the address of the method
     * @return int    the size of the method's header
     */
    private int printMethodHeader(Address block, Address oop) {
        int headerLength = oop.diff(block).toInt();
        totalMethodHeaderLength += headerLength;
        totalMethods++;
        int bytecodes = GC.getArrayLengthNoCheck(oop);
        int totalSize = headerLength + bytecodes;
        totalBytecodes += bytecodes;
        
        String headerString = getMethodHeaderText(oop); // does accounting also...
        if (showObjects) {
            printWordLine(block, "{"+headerLength+"}", false);

            Address definingClassAddress = oop.add(HDR.methodDefiningClass * BYTES_PER_WORD);
            for (Address i = block.add(BYTES_PER_WORD) ; i.lo(definingClassAddress); i = i.add(BYTES_PER_WORD)) {
                printWordLine(i, headerString, false);
                headerString = "";
            }

            Klass definingClass = VM.asKlass(NativeUnsafe.getObject(oop, HDR.methodDefiningClass));
            printWordLine(definingClassAddress, "defined in "+definingClass.getName(), true);
            printWordLine(oop.sub(HDR.arrayHeaderSize),  "["+GC.getArrayLengthNoCheck(oop)+"] total size " + totalSize, false);
            printWordLine(oop.sub(HDR.basicHeaderSize), "method "+getMethodSignature(oop) + classWordAnnotation(oop), true);
        }
        oopNext = true;
        return headerLength;
    }


    /**
     * Get the text for the method header.
     *
     * @param oop method object pointer
     * @return the text
     */
    private String getMethodHeaderText(Object oop) {
        StringBuffer sb    = new StringBuffer();
        int localCount     = MethodHeader.decodeLocalCount(oop);
        int parameterCount = MethodHeader.decodeParameterCount(oop);
        int maxStack       = MethodHeader.decodeStackCount(oop);

        sb.append("p="+parameterCount+" l="+localCount+" s="+maxStack);
        totalMethodLocals += localCount;
        totalMethodOpStack += maxStack;
        totalMethodOopMap += (parameterCount+localCount+7) / 8;
        int b0 = NativeUnsafe.getByte(oop, HDR.methodInfoStart) & 0xFF;
        
        if (b0 < 128) {
            totalMinfoSize += 2;
        } else {
            totalMinfoSize += 1;// fmt byte
            if (localCount >= 128 ) {
               totalMinfoSize += 2;
            } else {
               totalMinfoSize += 1;
            }
            if (parameterCount >= 128 ) {
               totalMinfoSize += 2;
            } else {
               totalMinfoSize += 1;
            }
            if (maxStack >= 128 ) {
               totalMinfoSize += 2;
            } else {
               totalMinfoSize += 1;
            }
            if (MethodHeader.decodeExceptionTableSize(oop) >= 128 ) {
               totalMinfoSize += 2;
            } else {
               totalMinfoSize += 1;
            }

            totalMinfoSize += 1;

            if (MethodHeader.decodeTypeTableSize(oop) >= 128 ) {
               totalMinfoSize += 2;
            } else {
               totalMinfoSize += 1;
            }
        }

        // Format the oopmap.
        if (parameterCount+localCount > 0) {
            sb.append(" map=");
            int offset = MethodHeader.decodeOopmapOffset(oop);
            for (int i = 0 ; i < parameterCount+localCount ; i++) {
                int pos = i / 8;
                int bit = i % 8;
                int bite = NativeUnsafe.getByte(oop, offset+pos) & 0xFF;
                boolean isOop = ((bite>>bit)&1) != 0;
                sb.append(isOop?"1":"0");
            }
        }

        // Format the type map.
        if (parameterCount+localCount > 0) {
            sb.append(" types=");
            Klass[] types = MethodHeader.decodeTypeMap(oop);
            for (int i = 0; i != types.length; ++i) {
                String name = types[i].getName();
                int index = name.indexOf("java.lang.");
                if (index != -1) {
                    name = name.substring(0, index) + name.substring(index + "java.lang.".length());
                }
                sb.append(name);
                if (i != types.length - 1) {
                    sb.append(',');
                }
            }
        }


        // Format the exception table (if any).
        int exceptionTableSize = MethodHeader.decodeExceptionTableSize(oop);
        totalMethodExceptionTableSize += (exceptionTableSize); //in bytes
        if (exceptionTableSize != 0) {
            sb.append(" exceptionTable={");
            int size   = MethodHeader.decodeExceptionTableSize(oop);
            int offset = MethodHeader.decodeExceptionTableOffset(oop);
            VMBufferDecoder dec = new VMBufferDecoder(oop, offset);
            long end = offset + size;
            VM.asKlass(NativeUnsafe.getObject(oop, HDR.methodDefiningClass));
            while (dec.getOffset() < end) {
                sb.append(" [start="+dec.readUnsignedShort()).
                    append(" end="+dec.readUnsignedShort()).
                    append(" handler="+dec.readUnsignedShort()).
                    append(" catch_type="+dec.readUnsignedShort()).
                    append("]");
            }
            sb.append(" }");
        }
        totalTypeTableSize += MethodHeader.decodeTypeTableSize(oop); //in bytes
        
        if (MethodHeader.isInterpreterInvoked(oop)) {
            sb.append(" INTERPRETER_INVOKED");
        }
        return sb.toString();
    }


    /*---------------------------------------------------------------------------*\
     *                        Non-array object decoding                          *
    \*---------------------------------------------------------------------------*/

    final static String suiteKlassName = "com.sun.squawk.Suite";
    /** cache this class so we don't have to do string compare on generated class name...
     */
    static Klass suiteKlass = null;
    
    /*
     */
    boolean registeredSuite;
    
    /**
     * Decode an instance.
     *
     * @param block  the address of the header of the object
     * @return the address of the next object header
     */
    private Address decodeInstance(Address block) {
        Address oop = block.add(HDR.basicHeaderSize);
        printInstanceHeader(oop);
        Klass klass = GC.getKlass(oop);
        if (fieldDefs != null && klass == Klass.KLASS) {
            Klass defClass = VM.asKlass(oop);
            fieldDefs.add(defClass);
        }

        Assert.always(suiteKlass != null);
        if (klass == suiteKlass) {
            registerSuite(oop);
        }
        Address nextBlock = printAllInstanceFields(oop, klass);
        recordObject(klass, block, oop, nextBlock);

        return nextBlock;
    }
    
    

    /**
     * When finding a suite, account for various metadata.
     * {IS there only one suite object within a objectmemoryfile?]
     */
    private void registerSuite(Address oop) {
        Assert.always(!registeredSuite);

        registeredSuite = true;
        updateOwner(SUITE_STATS, oop);
        claimObjectGraph(SUITE_STATS, getField(oop, suiteKlass, Klass.STRING, "name"), false);
        claimObjectGraph(SUITE_STATS, getField(oop, suiteKlass, Klass.STRING, "configuration"), false);
        claimObjectGraph(SUITE_STATS, getField(oop, suiteKlass, "[com.sun.squawk.Klass", "classes"), false);
        claimObjectGraph(SUITE_STATS, getField(oop, suiteKlass, Klass.STRING_ARRAY, "noClassDefFoundErrorClassNames"), false);
        
        claimObjectGraph(METADATA_STATS,       getField(oop, suiteKlass, "[com.sun.squawk.KlassMetadata", "metadatas"), false);
        claimObjectGraph(RESOURCE_FILES_STATS, getField(oop, suiteKlass, "[com.sun.squawk.ResourceFile", "resourceFiles"), false);
        claimObjectGraph(PROPERTIES_STATS,     getField(oop, suiteKlass, "[com.sun.squawk.ManifestProperty", "manifestProperties"), false);
    }
    
    /**
     * Print the fields of a non-array object.
     *
     * @param oop    the address of the object
     * @param klass  the class of the object
     * @return the address of the next object header
     */
    private Address printAllInstanceFields(Address oop, Klass klass) {
        Klass superClass = klass.getSuperclass();
        Address superEnd;
        if (superClass != null) {
            superEnd = printAllInstanceFields(oop, superClass);
        } else {
            superEnd = oop;
        }
        Address oopEnd = printFields(oop, superEnd, klass, false);
        return oopEnd;
    }

    private int modifiersSeen;
    
    private String classFieldAnnotation(String name, final Address fieldAddress) {
        String annotation = "";
        if (name.equals("modifiers")) {
            modifiersSeen = NativeUnsafe.getInt(fieldAddress, 0);
            annotation = " (" + Modifier.toString(NativeUnsafe.getInt(fieldAddress, 0)) + ")";
        } else if (name.equals("oopMapWord")) {
            annotation = " (" + Integer.toBinaryString(NativeUnsafe.getInt(fieldAddress, 0)) + ")";
        } else if (name.equals("dataMapWord")) {
            annotation = " (" + Integer.toBinaryString(NativeUnsafe.getInt(fieldAddress, 0)) + ")";
        } else if (modifiersSeen != -1
                && Modifier.hasDefaultConstructor(modifiersSeen)
                && name.equals("initModifiers")) {
            annotation = " (" + Modifier.toString(NativeUnsafe.getByte(fieldAddress, 0)) + ")";
        } else if (name.equals("state") && NativeUnsafe.getByte(fieldAddress, 0) == Klass.STATE_CONVERTED) {
            annotation = " (converted)"; // all classes in suite should be converted....
        } else if (name.equals("id")) {
            int id = NativeUnsafe.getShort(fieldAddress, 0);
            int suiteID = id >= 0 ? id : -(id + 1);
            annotation = " (suite index = " + suiteID + ")";
        }
        return annotation;
    }
    
    /**
     * Print the fields of an instance for a given class in the hierarchy of the instance.
     *
     * @param oop         the pointer to the instance
     * @param firstField  the address of the first field in the instance defined by <code>klass</code>
     * @param klass       the class whose fields are to be printed
     * @return the address at which the first field of the next sub-class in the instance's hierachy (if any)
     *                    will be located
     */
    private Address printFields(final Address oop, final Address firstField, Klass klass, boolean isStatic) {
        int size = (isStatic ? klass.getStaticFieldsSize() + CS.firstVariable : klass.getInstanceSize()) * BYTES_PER_WORD;
        Address nextField = firstField;
        modifiersSeen = -1;

        // Dump the class pointer and next pointer
        if (isStatic && showObjects) {
            printPointerLine(oop.add(CS.klass * BYTES_PER_WORD), "CS.klass", Klass.KLASS);
            printPointerLine(oop.add(CS.next * BYTES_PER_WORD), "CS.next", Klass.KLASS);
        }

        // Print the fields.
        int fieldCount = klass.getFieldCount(isStatic);
        for (int i = 0 ; i != fieldCount; i++) {
            Field field = klass.getField(i, isStatic);
            String name = field.getName();
            int offset = field.getOffset();
            Klass type = field.getType();

            int fsize = type.getDataSize();
            Address fieldAddress = oop.add(offset * fsize);

            if (isStatic) {
                fieldAddress = firstField.add(offset * BYTES_PER_WORD);
                fsize = Math.max(BYTES_PER_WORD, type.getDataSize());
                if (field.isFinal() && field.hasConstant()) {
                    continue;
                }
            }

            if (!Klass.SQUAWK_64 && !isStatic && (type.isDoubleWord())) {
                fieldAddress = oop.add(offset * 4);
            }

            if (showObjects) {
                String annotation = "";
                if (klass == Klass.KLASS) { // do more decoding of fields...
                    annotation = classFieldAnnotation(name, fieldAddress);
                }
                
                switch (type.getSystemID()) {
                    case CID.BOOLEAN:   printPrimitiveLine(fieldAddress, fsize, "bool",   name, NativeUnsafe.getByte(fieldAddress, 0) + annotation);  break;
                    case CID.BYTE:      printPrimitiveLine(fieldAddress, fsize, "byte",   name, NativeUnsafe.getByte(fieldAddress, 0) + annotation);  break;
                    case CID.CHAR:      printPrimitiveLine(fieldAddress, fsize, "char",   name, NativeUnsafe.getChar(fieldAddress, 0) + annotation);  break;
                    case CID.SHORT:     printPrimitiveLine(fieldAddress, fsize, "short",  name, NativeUnsafe.getShort(fieldAddress, 0) + annotation); break;
                    case CID.INT:       printPrimitiveLine(fieldAddress, fsize, "int",    name, NativeUnsafe.getInt(fieldAddress, 0) + annotation);   break;
                    case CID.FLOAT:     printPrimitiveLine(fieldAddress, fsize, "float",  name, Float.intBitsToFloat(NativeUnsafe.getInt(fieldAddress, 0)) + annotation);   break;
                    case CID.LONG:      printPrimitiveLine(fieldAddress, fsize, "long",   name, NativeUnsafe.getLong(fieldAddress, 0) + annotation);  break;
                    case CID.DOUBLE:    printPrimitiveLine(fieldAddress, fsize, "double", name, Double.longBitsToDouble(NativeUnsafe.getLong(fieldAddress, 0)) + annotation);  break;
                    case CID.UWORD:     printPrimitiveLine(fieldAddress, fsize, "uword",  name, NativeUnsafe.getUWord(fieldAddress, 0) + annotation);  break;
                    case CID.OFFSET:    printPrimitiveLine(fieldAddress, fsize, "offset", name, NativeUnsafe.getUWord(fieldAddress, 0) + annotation);  break;
                    default: {
                        printPointerLine(fieldAddress, name, type);
                        break;
                    }
                }
            }
            nextField = fieldAddress.add(fsize);
        }

        nextField = nextField.roundUpToWord();
        Assert.that(oop.add(size).roundUpToWord() == nextField);
        return nextField;
    }

    /** 
     * Should record objects when maling linear pass over object memory.
     */
    boolean shouldRecordObjects = true;

    /**
     * Generic help to remember this object or later.
     * Update class histogram and object table.
     */
    private void recordObject(Klass klass, Address block, Address oop, Address nextBlock) {
        if (!shouldRecordObjects) {
            return;
        }
        int size = nextBlock.diff(block).toInt();
        classHistogram.updateFor(klass, size);
        objectTable.recordObject(block, oop, size);
        claimObjectGraph(UNASSIGNED_STATS, oop, false);
    }
    
    /*---------------------------------------------------------------------------*\
     *                        Array object decoding                              *
    \*---------------------------------------------------------------------------*/

    /**
     * Decodes an array object.
     *
     * @param block   the address of the header of the array object
     * @return the address of the next object header
     */
    private Address decodeArray(Address block) {
        Address oop = block.add(HDR.arrayHeaderSize);
        Klass klass = GC.getKlass(oop);
        Klass componentType = klass.getComponentType();
        int componentSize = klass.getSquawkArrayComponentDataSize();

        int length = printArrayHeader(oop);
        if (length == 0) {
            recordObject(klass, block, oop, oop);
            return oop;
        }
        
        if (klass == Klass.LOCAL_ARRAY) {
            decodeChunk(oop, length);
        } else if (klass == Klass.GLOBAL_ARRAY) {
            Klass globalsKlass = VM.asKlass(NativeUnsafe.getObject(oop, CS.klass));
            if (showObjects) {
                printFields(oop, oop.add(CS.firstVariable * BYTES_PER_WORD), globalsKlass, true);
            }
        } else if (klass == Klass.STRING_OF_BYTES || klass == Klass.STRING) {
            String value = VM.asString(oop);
            int charsPerLine = 8 / componentSize;
            Address addr = oop;
            
            while (value.length() != 0) {
                int charsThisLine = (int)Math.min(charsPerLine, value.length());
                int bytesThisLine = charsThisLine * componentSize;
                if (showObjects) {
                    printLine(addr, bytesThisLine, '"' + value.substring(0, charsThisLine) + '"');
                }
                
                if (value.length() >= charsThisLine) {
                    value = value.substring(charsThisLine);
                    addr = addr.add(bytesThisLine);
                }
            }
        } else if (showObjects) {
            for (int i = 0 ; i < length ; ++i) {
                Address componentAddress = oop.add(i * componentSize);
                String index = ""+i;
                switch (klass.getSystemID()) {
                    case CID.BOOLEAN_ARRAY:   printPrimitiveLine(componentAddress, componentSize, "bool",   index, ""+NativeUnsafe.getByte(componentAddress, 0));  break;
                    case CID.CHAR_ARRAY:      printPrimitiveLine(componentAddress, componentSize, "char",   index, ""+NativeUnsafe.getChar(componentAddress, 0));  break;
                    case CID.SHORT_ARRAY:     printPrimitiveLine(componentAddress, componentSize, "short",  index, ""+NativeUnsafe.getShort(componentAddress, 0)); break;
                    case CID.INT_ARRAY:       printPrimitiveLine(componentAddress, componentSize, "int",    index, ""+NativeUnsafe.getInt(componentAddress, 0));   break;
                    case CID.FLOAT_ARRAY:     printPrimitiveLine(componentAddress, componentSize, "float",  index, ""+Float.intBitsToFloat(NativeUnsafe.getInt(componentAddress, 0)));   break;
                    case CID.LONG_ARRAY:      printPrimitiveLine(componentAddress, componentSize, "long",   index, ""+NativeUnsafe.getLong(componentAddress, 0));  break;
                    case CID.DOUBLE_ARRAY:    printPrimitiveLine(componentAddress, componentSize, "double", index, ""+Double.longBitsToDouble(NativeUnsafe.getLong(componentAddress, 0)));  break;
                    case CID.UWORD_ARRAY:     printPrimitiveLine(componentAddress, componentSize, "word",   index, ""+NativeUnsafe.getUWord(componentAddress, 0));   break;
                    
                    default: {
                        printPointerLine(componentAddress, index, componentType);
                        break;
                    }
                    
                    case CID.BYTE_ARRAY: {
                        int b  = NativeUnsafe.getByte(componentAddress, 0);
                        StringBuffer s = new StringBuffer(80).
                                append("    byte   ").
                                append(b);
                        if (b < 0) {
                            s.append(" (").append(b&0xFF).append(')');
                        }
                        char ch = (char)(b & 0xFF);
                        if (ch >= ' ' && ch <= '~') {
                            pad(s, 30);
                            s.append(ch);
                        }
                        printLine(componentAddress, 1, s.toString());
                        break;
                    }
                }
            }
        }
        
        Address nextBlock = oop.add(componentSize * length).roundUpToWord();
        recordObject(klass, block, oop, nextBlock);
        return nextBlock;

    }

    /*---------------------------------------------------------------------------*\
     *                        Stack chunk decoding                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Decodes and dumps a stack chunk.
     *
     * @param chunk  the pointer to the stack chunk
     * @param size   the size of a word
     * @param length the number of words
     */
    private void decodeChunk(Address chunk, int length) {
        printPointerLine(chunk.add(SC.next * BYTES_PER_WORD), "next", Klass.OBJECT);
        printPointerLine(chunk.add(SC.owner * BYTES_PER_WORD), "owner", Klass.OBJECT);
        printPointerLine(chunk.add(SC.lastFP * BYTES_PER_WORD), "last fp", null);
        printPrimitiveLine(chunk.add(SC.lastBCI * BYTES_PER_WORD), BYTES_PER_WORD, "uword", "last bci", NativeUnsafe.getUWord(chunk, SC.lastBCI).toString());
        printPrimitiveLine(chunk.add(SC.guard * BYTES_PER_WORD), BYTES_PER_WORD, "uword", "guard", NativeUnsafe.getUWord(chunk, SC.lastBCI).toString());

        Address fp = Address.fromObject(NativeUnsafe.getObject(chunk, SC.lastFP));
        Address bogusEnd;

        // Calculate where the last bogus slot is
        if (fp.isZero()) {
            // The whole chunk is bogus
            bogusEnd = chunk.add(length * BYTES_PER_WORD);
        } else {
            // The last bogus slot is the local slot 1 as local slot 0 contains the
            // method pointer which should be valid
            bogusEnd = fp.sub(BYTES_PER_WORD);
        }

        // Dump the bogus/unused slots
        for (Address slot = chunk.add(SC.limit * BYTES_PER_WORD); slot.loeq(bogusEnd); slot = slot.add(BYTES_PER_WORD)) {
            printPrimitiveLine(slot, BYTES_PER_WORD, "bogus", null, "" + NativeUnsafe.getUWord(slot, 0));
        }

        boolean isInnerMostActivation = true;
        while (!fp.isZero()) {
            decodeActivation(fp, isInnerMostActivation);
            fp = Address.fromObject(NativeUnsafe.getObject(fp, FP.returnFP));
            isInnerMostActivation = false;
        }
    }

    /**
     * Prints a line for a stack chunk slot corresponding to a local variable or parameter.
     *
     * @param lp     the address of the local variable or parameter
     * @param type   the type of the local variable or parameter as specified by {@link MethodHeader.decodeTypeMap(Object)}
     * @param name   the name of the local variable or parameter
     */
    private void printLocalOrParameterLine(Address lp, Klass type, String name) {
        switch (type.getSystemID()) {
            case CID.INT:     printPrimitiveLine(lp, BYTES_PER_WORD, "int",     name, "" + NativeUnsafe.getInt(lp, 0)); break;
            case CID.LONG:    printPrimitiveLine(lp, BYTES_PER_WORD, "long",    name, "" + NativeUnsafe.getUWord(lp, 0) + "  {" + NativeUnsafe.getLong(lp, 0) + "L}"); break;
            case CID.LONG2:   printPrimitiveLine(lp, BYTES_PER_WORD, "long2",   name, "" + NativeUnsafe.getInt(lp, 0)); break;
            case CID.FLOAT:   printPrimitiveLine(lp, BYTES_PER_WORD, "float",   name, "" + NativeUnsafe.getInt(lp, 0) + "  {" + Float.intBitsToFloat(NativeUnsafe.getInt(lp, 0)) + "F}"); break;
            case CID.DOUBLE:  printPrimitiveLine(lp, BYTES_PER_WORD, "double",  name, "" + NativeUnsafe.getUWord(lp, 0) + "  {" + Double.longBitsToDouble(NativeUnsafe.getLong(lp, 0)) + "D}"); break;
            case CID.DOUBLE2: printPrimitiveLine(lp, BYTES_PER_WORD, "double2", name, "" + NativeUnsafe.getInt(lp, 0)); break;
            case CID.ADDRESS: printPrimitiveLine(lp, BYTES_PER_WORD, "address", name, "" + NativeUnsafe.getAddress(lp, 0)); break;
            case CID.UWORD:   printPrimitiveLine(lp, BYTES_PER_WORD, "uword",   name, "" + NativeUnsafe.getUWord(lp, 0)); break;
            case CID.OFFSET:  printPrimitiveLine(lp, BYTES_PER_WORD, "offset",  name, "" + NativeUnsafe.getUWord(lp, 0).toOffset()); break;
            case CID.OBJECT:  printPointerLine(lp, name, Klass.OBJECT); break;
            default: Assert.shouldNotReachHere();
        }
    }

    /**
     * Decodes a single activation frame.
     *
     * @param fp                     the frame pointer
     * @param isInnerMostActivation  specifies if this is the inner most activation frame on the chunk
     *                               in which case it's local variables are bogus
     */
    private void decodeActivation(Address fp, boolean isInnerMostActivation) {

        Address mp = Address.fromObject(NativeUnsafe.getObject(fp, FP.method));

        StringBuffer buf = new StringBuffer(120);
        pad(buf, 43);
        buf.append("---------- ").
            append(getMethodSignature(mp)).
            append(" ----------");
        out.println(buf);

        int localCount     = isInnerMostActivation ? 1 : MethodHeader.decodeLocalCount(mp);
        int parameterCount = MethodHeader.decodeParameterCount(mp);
        Klass[] typeMap     = MethodHeader.decodeTypeMap(mp);
        int typeIndex = typeMap.length;


        // Print the locals
        while (localCount-- > 0) {
            Address lp = fp.sub(localCount * BYTES_PER_WORD);
            String name = (lp == fp ? "MP/local0" : "local" + localCount);
            printLocalOrParameterLine(lp, typeMap[--typeIndex], name);
        }

        // Print the return FP and return IP
        printPointerLine(fp.add(FP.returnFP * BYTES_PER_WORD), "returnFP", null);
        printPointerLine(fp.add(FP.returnIP * BYTES_PER_WORD), "returnIP", null);

        // Print the parameters
        int offset = FP.parm0;
        typeIndex = 0;
        while (parameterCount-- > 0) {
            Address lp = fp.add(offset * BYTES_PER_WORD);
            printLocalOrParameterLine(lp, typeMap[typeIndex], "parm" + typeIndex);
            offset++;
            typeIndex++;
        }
    }

    /*---------------------------------------------------------------------------*\
     *                        Method body decoding                               *
    \*---------------------------------------------------------------------------*/

    /**
     * Decode a method.
     *
     * @param block the address before the header of the object
     * @return the address of the next object header
     */
    private Address decodeMethod(Address block) {
        int headerLength = (int)GC.decodeLengthWord(NativeUnsafe.getUWord(block, 0));
        Address oop = block.add(headerLength * BYTES_PER_WORD);
        Klass klass = GC.getKlass(oop);
        int length = GC.getArrayLengthNoCheck(oop);
        printMethodHeader(block, oop);
        MapBytecodeTracer tracer = new MapBytecodeTracer(oop, length);
        for (int i = 0 ; i < length ;) {
            BytecodeTracerEntry entry = tracer.trace();
            if (showObjects) {
                String text = entry.getText();
                printLine(entry.getAddress(), entry.getSize(), text);
            }
            i += entry.getSize();
            bytecodes++;
        }

        Address nextBlock = oop.add(length).roundUpToWord();
        recordObject(klass, block, oop, nextBlock);
        return nextBlock;
    }

    /*-----------------------------------------------------------------------*\
     *                           Methods                                     *
    \*-----------------------------------------------------------------------*/

    /**
     * The map from method addresses to method signatures.
     */
    private HashMap<Object, String> methodMap = new HashMap<Object, String>();

    /**
     * Registers the signature of all the methods defined by a given class.
     *
     * @param klass Klass
     * @param methods Object
     * @param isStatic boolean
     */
    private void registerMethods(Klass klass, Object methods, boolean isStatic) {
        if (!klass.isArray() && methods != Address.zero()) {
            int length = GC.getArrayLengthNoCheck(methods);
            int cfLength = (isStatic ? klass.getStaticMethods() : klass.getVirtualMethods()).length;
            if (!klass.isInterface() && cfLength != length) {
                System.err.println("version skew detected: " + (isStatic ? "static" : "virtual") +
                        " method table length for " + klass + " in suite file ("+length+") differs from " +
                        "the same table in the class file ("+cfLength+") loaded from the class path");
                //throw new RuntimeException("Version skew");
                for (int i = 0; i != length; ++i) {
                    Object methodBody = NativeUnsafe.getObject(methods, i);
                    putMethodSignature(methodBody, "?version-skew, unknown?");
                }
            } else {
                int count = klass.getMethodCount(isStatic);
                for (int i = 0; i != count; ++i) {
                    Method method = klass.getMethod(i, isStatic);
                    if (method.isHosted() || method.isAbstract() || method.isNative()) {
                        continue;
                    }
                    
                    int offset = method.getOffset();
                    String signature = method.toString();
                    Assert.always(offset < length);
                    Object methodBody = NativeUnsafe.getObject(methods, offset);
                    putMethodSignature(methodBody, signature);
                }
            }
        }
    }
    
    private void putMethodSignature(Object methodBody, String signature) {
        if (methodBody != Address.zero() &&
            methodMap.get(methodBody) == null) {
            this.methodMap.put(methodBody, signature);
        }
    }

    /**
     * Gets the signature of a method based on a given address.
     *
     * @param addr   an address
     * @return the signature of the method at <code>addr</code> or null if there is no method there
     */
    private String getMethodSignature(Address addr) {
        return methodMap.get(addr);
    }

    /*-----------------------------------------------------------------------*\
     *                           Fields                                      *
    \*-----------------------------------------------------------------------*/

    /**
     * Dumps the signature and offsets of all the fields defined by a given class.
     *
     * @param klass Klass
     * @param isStatic boolean
     */
    private void printFieldDefinitions(Klass klass, boolean isStatic) {
        int count = klass.getFieldCount(isStatic);
        if (count > 0) {
            out.println( (isStatic ? "Static" : "Instance") + " fields for " + klass);
            for (int i = 0; i != count; ++i) {
                Field field = klass.getField(i, isStatic);
                int offset = field.getOffset();
                Klass type = field.getType();

                StringBuffer buf = new StringBuffer(100);
                buf.append("    ").append(field.getName());
                pad(buf, 30);
                buf.append(" [offset=").
                    append(offset).
                    append(" type=").
                    append(type.getInternalName());
                if (field.hasConstant()) {
                    if (type.isPrimitive()) {
                        buf.append(" constant=").
                            append(field.getPrimitiveConstantValue());
                    } else {
                        buf.append(" constant=\"").
                            append(field.getStringConstantValue()).
                            append('"');
                    }
                }
                buf.append("]");
                out.println(buf.toString());
            }
        }
    }

    /*---------------------------------------------------------------------------*\
     *                                  main                                     *
    \*---------------------------------------------------------------------------*/

    public static void main(String[] args) throws IOException {
        ObjectMemoryMapper mapper = new ObjectMemoryMapper();
        if (mapper.parseArgs(args)) {
            mapper.run();
        }
    }
}
