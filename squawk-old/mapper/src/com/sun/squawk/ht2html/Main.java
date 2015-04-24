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

package com.sun.squawk.ht2html;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.sun.squawk.vm.HDR;
import com.sun.squawk.util.LongHashtable;

/**
 * A tool for processing the trace lines from a squawk execution that start with "*HEAP*:"
 * and generating a HTML file that shows the configuration of the heap at various
 * times in the execution.
 *
 */
public class Main {

    public static void main(String[] args) {
        Main instance = new Main();
        if (instance.parseArgs(args)) {
            instance.run();
        }
    }

    /**
     * The input trace file.
     */
    private String traceFile;

    /**
     * Directory into which the generated HTML files are written.
     */
    private File outDir = new File("heaptrace");

    /**
     * The height (in pixels) of the HTML tables used to represent the heap.
     */
    private int height = 700;

    /**
     * The filter to be applied to the traces.
     */
    private String filter;

    /**
     * The maximum number of traces to be displayed.
     */
    private int limit = -1;

    /**
     * Determines if a file will be generated for each heap that shows the console
     * output since the previous heap trace.
     */
    private boolean showConsoleOutput = true;

    /**
     * Displays usage message.
     *
     * @param errMsg  error message displayed first if it is non-null
     */
    private void usage(String errMsg) {
        PrintStream out = System.err;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("usage: Main [options] tracefile");
        out.println();
        out.println("where options include:");
        out.println("    -d:<directory> output directory (default='"+outDir.getPath()+"')");
        out.println("    -height:<n>    height of heap HTML table (default=" + height + ")");
        out.println("    -filter:<text> disregard traces that don't include 'text'");
        out.println("    -noconsole     ignore console output between heap traces");
        out.println("    -l:<limit>     max traces to be displayed");
        out.println("    -h             display help message and exit");
        out.println();
    }

    /**
     * Parses the command line arguments.
     *
     * @param args  the command line arguments
     * @return  true if the program should continue running
     */
    private boolean parseArgs(String[] args) {
        int argc = 0;
        while(argc != args.length) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            }
            if (arg.startsWith("-height:")) {
                height = Integer.parseInt(arg.substring("-height:".length()));
            } else if (arg.startsWith("-filter:")) {
                filter = arg.substring("-filter:".length());
            } else if (arg.startsWith("-l:")) {
                limit = Integer.parseInt(arg.substring("-l:".length()));
            } else if (arg.startsWith("-d:")) {
                String dir = arg.substring("-d:".length());
                outDir = new File(dir);
            } else if (arg.equals("-noconsole")) {
                showConsoleOutput = false;
            } else if (arg.equals("-h")) {
                usage(null);
                return false;
            } else {
                usage("Unknown option: " + arg);
                return false;
            }
            argc++;
        }

        if (argc == args.length) {
            usage("No input trace file specified.");
            return false;
        }
        traceFile = args[argc++];

        /*
         * Create the output directory if it doesn't exist
         */
        if (!outDir.exists()) {
            if (!outDir.mkdirs()) {
                System.err.println("Could not create output directory '" + outDir.getPath() + "'");
                return false;
            }
        }

        return true;
    }

    /**
     * Reads the trace and generates a HTML file visualizing the heap.
     */
    private void run() {

        InputFile in = new InputFile(traceFile);
        String line = in.readLine();
        while (line != null && !line.startsWith("*HEAP*:initialize:")) {
            line = in.readLine();
        }

        if (line == null) {
            throw in.error("no line starting with '*HEAP*:initialize:' found", null);
        }

        // Trim off prefix
        line = line.substring("*HEAP*:initialize:".length());

        // The start and end address of the memory being modelled.
        long start = 0;
        long end = 0;

        // Parse the memory range start and end addresses
        StringTokenizer st = new StringTokenizer(line, ":");
        try {
            start = parseUWord(st.nextToken());
            end = parseUWord(st.nextToken());
        } catch (NoSuchElementException e) {
            throw in.error("bad format - expected '*HEAP*:initialize:<address>:<address>'", e);
        }

        // Generate the index file (this is just a copy)
        createFile(openTemplate("index.html"), "index.html", null);

        // The set of heap traces parsed.
        ArrayList<String> heaps = new ArrayList<String>();

        // The heap trace currently being parsed.
        Heap heap = null;
        int id = 1;

        Template summaryTemplate = openTemplate("summary.html");
        Template segmentsTemplate = openTemplate("segments.html");
        Template heapTemplate = openTemplate("heap.html");
        Template consoleTemplate = showConsoleOutput ? openTemplate("console.html") : null;
        Vector<String> consoleLines = showConsoleOutput ? new Vector<String>() : null;

        while ((line = in.readLine()) != null) {
            if (line.startsWith("*HEAP*:")) {

                // Trim off prefix
                line = line.substring("*HEAP*:".length());

                // Extract tag and any parameters from the line
                String tag = line;
                String parameters;
                int index = line.indexOf(':');
                if (index != -1) {
                    tag = line.substring(0, index);
                    parameters = line.substring(index + 1);
                } else {
                    parameters = "";
                }
                if (tag.length() == 0) {
                    throw in.error("no tag found after '*HEAP*:'", null);
                }

                // Dispatch to tag handler
                if (tag.equals("start")) {
                    if (filter == null || parameters.indexOf(filter) != -1) {
                        heap = parseStart(parameters, in);

                        if (consoleLines != null) {
                            createFile(consoleTemplate, "console" + id + ".html", new ConsoleInstantiator(consoleLines));
                            consoleLines.removeAllElements();
                        }
                    }
                } else if (tag.equals("end")) {
                    if (heap != null) {
                        // Generate the detailed heap trace HTML frame for the current heap
                        createFile(heapTemplate, "heap" + id + ".html", new HeapInstantiator(heap));

                        // Generate the heap summary HTML frame for the current heap
                        createFile(summaryTemplate, "summary" + id + ".html", new HeapSummaryInstantiator(heap, start, end));

                        // Generate the heap segments HTML frame for the current heap
                        createFile(segmentsTemplate, "segments" + id + ".html", new HeapSegmentsInstantiator(heap, start, end, height));

                        // Record the description of the current heap for the index
                        int percentFree = (int)((heap.getFreeMemory() * 100) / heap.getTotalMemory());
                        String indexEntry = heap.getDescription() + "{<i>free:" + percentFree + "%</i>}";
                        if (heap.getBranchCount() != -1) {
                            indexEntry += "{<i>bcount:" + heap.getBranchCount() + "</i>}";
                        }
                        heaps.add(indexEntry);

                        id++;

                        if (limit != -1 && id > limit) {
                            break;
                        }
                    }
                    heap = null;
                } else if (tag.equals("segment")) {
                    if (heap != null) {
                        heap.addSegment(parseSegment(parameters, in));
                    }
                } else {
                    if (heap != null) {
                        // must be an object
                        try {
                            long objectStart = parseUWord(tag);
                            heap.appendObject(parseObject(objectStart, parameters, in));
                        } catch (NumberFormatException nfe) {
                            throw in.error("unknown tag: " + tag, nfe);
                        } catch (Throwable t) {
                            throw in.error("Error parsing object", t);
                        }
                    }
                }
            } else {
                if (consoleLines != null) {
                    consoleLines.addElement(line);
                }
            }
        }

        // Generate the frame with an index to all the heaps
        createFile(openTemplate("heapindex.html"), "heapindex.html", new HeapsIndexInstantiator(heaps, showConsoleOutput));
    }

    /**
     * Creates a file in the output directory based on a template.
     *
     * @param template     the template for the file to be created
     * @param name         the name of the file to be created
     * @param instantiator the object used to bind content to the variables in the template (if any)
     */
    private void createFile(Template template, String name, Template.Instantiator instantiator) {
        System.err.print("creating " + name + "...");
        PrintStream out = openOutputFile(name);
        template.instantiate(out, instantiator);
        out.close();
        System.err.println(" done.");
        System.err.flush();
    }

    /**
     * Handles parsing of a 'start' heap trace line.
     *
     * @param parameters the remainder of the heap trace line after the 'start' tag
     * @param in         the file from which the trace is being read
     * @return the parsed segment
     */
    private Heap parseStart(String parameters, InputFile in) {
        StringTokenizer st = new StringTokenizer(parameters, ":");

        try {
            long branchCount = parseUWord(st.nextToken());
            long freeMemory = parseUWord(st.nextToken());
            long totalMemory = parseUWord(st.nextToken());
            String description = st.nextToken("");

            // drop leading ':'
            description = description.substring(1);

            return new Heap(description, branchCount, freeMemory, totalMemory);
        } catch (NoSuchElementException e) {
            throw in.error("bad format - expected '*HEAP*:start:<address>:<long>:<long>:<label>'", e);
        }
    }

    /**
     * Handles parsing of a 'segment' heap trace line.
     *
     * @param parameters the remainder of the heap trace line after the 'segment' tag
     * @param in         the file from which the trace is being read
     * @return the parsed segment
     */
    private Segment parseSegment(String parameters, InputFile in) {
        StringTokenizer st = new StringTokenizer(parameters, ":");

        try {
            long start = parseUWord(st.nextToken());
            long end = parseUWord(st.nextToken());
            String label = st.nextToken("");

            // drop leading ':'
            label = label.substring(1);

            return new Segment(label, start, end);
        } catch (NoSuchElementException e) {
            throw in.error("bad format - expected '*HEAP*:segment:<address>:<address>:<label>'", e);
        }
    }

    /**
     * Handles parsing of a 'object' heap trace line.
     *
     * @param start  the address of the object's header
     * @param parameters the remainder of the heap trace line after the object's address
     * @param in         the file from which the trace is being read
     * @return the parsed object
     */
    private HeapObject parseObject(long start, String parameters, InputFile in) {
        StringTokenizer st = new StringTokenizer(parameters, ":");
        try {
            int count = st.countTokens();
            String[] data = new String[count - 2];
            String token;
            int i = 0;
            while (!(token = st.nextToken()).equals("oop")) {
                data[i++] = token;
            }
            int headerSize = i;
            int bodySize = 0;
            while (i != data.length) {
                token = st.nextToken();
                if (token.charAt(0) == '*') {
                    int repeats = Integer.parseInt(token.substring(1));
                    bodySize += repeats;
                } else {
                    bodySize++;
                }
                data[i++] = token;
            }
            String klass = st.nextToken();
            return new HeapObject(klass, start, headerSize, bodySize, data);
        } catch (Throwable e) {
            throw in.error("bad format - expected '*HEAP*:<address>*:oop:<address>*:<class>'", e);
        }
    }

    /**
     * Opens a file to be created in the output directory.
     *
     * @param name   the name of the file
     * @return a PrintStream on the opened file
     */
    private PrintStream openOutputFile(String name) {
        File path = new File(outDir, name);
        try {
            return new PrintStream(new BufferedOutputStream(new FileOutputStream(path)));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error while opening output file '" + path.getPath() + "'", e);
        }

    }

    /**
     * Opens a template file and returns its contents in a Template object. The path to the file
     * is composed of the value of the system property "ht2html.templates.path" and the a given
     * name. If the above property does not exist, then the directory of this source file is used.
     *
     * @param   name  the name of the template
     * @return the template's contents
     */
    private Template openTemplate(String name) {
        String path = System.getProperty("ht2html.templates.path");
        if (path == null) {
            path = Main.class.getName().replace('.', File.separatorChar);
            path = path.substring(0, path.indexOf("Main"));
            path = "mapper/src/" + path + name;
            if (File.separatorChar != '/') {
                path = path.replace('/', File.separatorChar);
            }
        }
        try {
            return new Template(new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Error reading template file: " + path, e);
        }

    }

    /**
     * Parses a string that represents a UWord value and returns it as a long. The returned value may
     * have a different sign than the string representation passed in if it is greater than
     * {@link Long#MAX_VALUE}.
     *
     * @param value   a string representation of a UWord
     * @return long   the corresponding long value
     * @throws NumberFormatException if the value cannot be represented in 64 bits
     */
    public static long parseUWord(String value) throws NumberFormatException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException nfe) {
            java.math.BigInteger bi = new java.math.BigInteger(value);
            if (bi.bitCount() > 64) {
                throw new NumberFormatException("value cannot be expressed as a long: " + value);
            }
            return bi.longValue();
        }
    }
}

/**
 * An InputFile instance is used to read a file, line by line.
 */
class InputFile {

    /**
     * The current line number.
     */
    private int lineNo;

    /**
     * The underlying reader.
     */
    private final BufferedReader reader;

    /**
     * The path to the file being read.
     */
    private final String path;

    /**
     * Opens a file for reading.
     *
     * @param path   the path to the file
     */
    InputFile(String path) {
        this.lineNo = 1;
        this.path = path;
        try {
            reader = new BufferedReader(new FileReader(path), 1000000);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error opening input file: " + path, e);
        }
    }

    /**
     * Reads a line from the input.
     *
     * @return  the line read or null if EOF is reached
     */
    String readLine() {
        try {
            String line = reader.readLine();
            if (line != null) {
                ++lineNo;
            }
            return line;
        } catch (IOException ex) {
            throw error(ex.toString(), ex);
        }
    }

    /**
     * Gets the line number of the current read position.
     *
     * @return the line number of the current read position
     */
    int getLineNumber() {
        return lineNo;
    }

    /**
     * Creates a runtime exception detailing an error while reading from this file.
     *
     * @param msg  description of the error
     * @return  a runtime exception detailing the error
     */
    RuntimeException error(String msg, Throwable cause) {
        msg = "Error at line " + lineNo + " in " + path + ": " + msg;
        if (cause != null) {
            return new RuntimeException(msg, cause);
        } else {
            return new RuntimeException(msg);
        }
    }
}

/**
 * A ConsoleInstantiator instance is used to instantiate the variables in the "console.html" template.
 */
class ConsoleInstantiator implements Template.Instantiator {

    /**
     * The lines of the console output.
     */
    private final Vector lines;

    ConsoleInstantiator(Vector lines) {
        this.lines = lines;
    }

    /**
     * {@inheritDoc}
     */
    public boolean instantiate(PrintStream out, String key) {
        if (key.equals("console")) {
            int id = 1;
            for (Enumeration e = lines.elements(); e.hasMoreElements();) {
                out.println(e.nextElement());
            }
            return true;
        } else {
            return false;
        }
    }
}

/**
 * A HeapIndexInstantiator instance is used to instantiate the variables in the "heapindex.html" template.
 */
class HeapsIndexInstantiator implements Template.Instantiator {

    private final ArrayList<String> heaps;
    private final boolean showConsoleOutput;

    HeapsIndexInstantiator(ArrayList<String> heaps, boolean showConsoleOutput) {
        this.heaps = heaps;
        this.showConsoleOutput = showConsoleOutput;
    }

    private void anchor(PrintStream out, String base, int id) {
        out.println("[<a href=\"#heap" + (id - 1) + "\" onClick=\"select(" + id + ", '" + base + "')\">" + base + "</a>]");
    }

    /**
     * {@inheritDoc}
     */
    public boolean instantiate(PrintStream out, String key) {
        if (key.equals("heaps.index")) {
            int id = 1;
            for (String idstr: heaps) {
                out.println("<span id=\"h" + id + "\">" + id + ". " + idstr + "</span><br />");
                out.println("<a name=\"heap" + id + "\"></a>");
                anchor(out, "summary", id);
                anchor(out, "segments", id);
                anchor(out, "heap", id);
                if (showConsoleOutput) {
                    anchor(out, "console", id);
                }
                out.println("<br /><br />");
                id++;
            }
            return true;
        } else if (key.equals("heap.count")) {
            out.print(heaps.size());
            return true;
        } else {
            return false;
        }
    }
}

/**
 * A HeapSummaryInstantiator instance is used to instantiate the variables in the "summary.html" template.
 */
class HeapSummaryInstantiator implements Template.Instantiator {

    /**
     * A ClassSummary instance records a frequency and total size for the instances of a class.
     */
    static class ClassSummary {

        /**
         * The name of the class.
         */
        final String klass;

        /**
         * The count of instances of the class.
         */
        int frequency;

        /**
         * The total size (in bytes) of instances of the class.
         */
        int size;

        ClassSummary(String klass) {
            this.klass = klass;
        }

        /**
         * Imposes an ascending order with respect to total size on a ClassSummary array.
         */
        static Comparator<ClassSummary> SIZE_COMPARATOR = new Comparator<ClassSummary>() {
            public int compare(ClassSummary o1, ClassSummary o2) {
                return o1.size - o2.size;
            }
        };

        /**
         * Imposes an ascending order with respect to frequency on a ClassSummary array.
         */
        static Comparator<ClassSummary> FREQUENCY_COMPARATOR = new Comparator<ClassSummary>() {
            public int compare(ClassSummary o1, ClassSummary o2) {
                return o1.frequency - o2.frequency;
            }
        };

        /**
         * Reformats a string such that any preceeding '['s are moved to the end of the string.
         *
         * @param s  the string to reformat
         * @return the reformatted string
         */
        private static String moveBracketsToEnd(String s) {
            while (s.charAt(0) == '[') {
                s = s.substring(1) + '[';
            }
            return s;
        }

        /**
         * Imposes an ascending order with respect to class name on a ClassSummary array.
         */
        static Comparator<ClassSummary> CLASS_COMPARATOR = new Comparator<ClassSummary>() {
            public int compare(ClassSummary o1, ClassSummary o2) {
                return moveBracketsToEnd(o1.klass).
                    compareTo(moveBracketsToEnd(o2.klass));
            }
        };
    }

    final HashMap<String, ClassSummary> summary;
    final long memorySize;
    final String description;
    final long totalMemory;

    HeapSummaryInstantiator(Heap heap, long start, long end) {
        this.summary = summarize(heap);
        this.memorySize = (end - start);
        this.description = heap.getDescription();
        this.totalMemory = heap.getTotalMemory();
    }

    /**
     * {@inheritDoc}
     */
    public boolean instantiate(PrintStream out, String key) {
        if (key.equals("instanceSizeTable") || key.equals("instanceSizeTableByClass")) {
            doInstanceSizeTable(out, key.equals("instanceSizeTable"));
        } else if (key.equals("instanceCountTable") || key.equals("instanceCountTableByClass")) {
            doInstanceCountTable(out, key.equals("instanceCountTable"));
        } else if (key.equals("heap.description")) {
            out.print(description);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Creates a heap summary based on a given heap.
     *
     * @param heap   the heap trace to summarize
     * @return a summary of the frequency and total size of each class represented in the trace
     */
    private static HashMap<String, ClassSummary> summarize(Heap heap) {
        HashMap<String, ClassSummary> summary = new HashMap<String, ClassSummary>();
        Enumeration objects = heap.getObjects();
        while (objects.hasMoreElements()) {
            HeapObject object = (HeapObject)objects.nextElement();

            String klass = object.getKlass();
            ClassSummary cs = (ClassSummary)summary.get(klass);
            if (cs == null) {
                cs = new ClassSummary(klass);
                summary.put(klass, cs);
            }
            cs.size += object.getSize();
            cs.frequency++;
        }
        return summary;
    }

    /**
     * Sorts the heap summary into an array based on a given sorting criteria.
     *
     * @param comparator   the sorting criteria
     * @return the sorted heap summary
     */
    private ClassSummary[] sortSummary(Comparator<ClassSummary>  comparator) {
        ClassSummary[] cs = new ClassSummary[summary.size()];
        summary.values().toArray(cs);
        Arrays.sort(cs, comparator);
        return cs;
    }

    /**
     * Replace the 'instanceSizeTable' variable.
     *
     * @param out  writes the replacement to this stream
     */
    private void doInstanceSizeTable(PrintStream out, boolean sortBySize) {
        Comparator<ClassSummary> c = sortBySize ? ClassSummary.SIZE_COMPARATOR : ClassSummary.CLASS_COMPARATOR;
        ClassSummary[] summaryies= sortSummary(c);
        for (int i = 0; i != summaryies.length; ++i) {
            ClassSummary cs = summaryies[i];
            int percent = (int)((cs.size * 100) / totalMemory);
            out.println("<tr><td nowrap align=\"right\">" + cs.size + " {" + percent + "%}&nbsp;</td><td nowrap align=\"left\">&nbsp;" + cs.klass + "</td></tr>");
        }
    }

    /**
     * Replace the 'instanceCountTable' variable.
     *
     * @param out  writes the replacement to this stream
     */
    private void doInstanceCountTable(PrintStream out, boolean sortByFrequency) {
        Comparator<ClassSummary> c = sortByFrequency ? ClassSummary.FREQUENCY_COMPARATOR : ClassSummary.CLASS_COMPARATOR;
        ClassSummary[] summaryies = sortSummary(c);
        for (int i = 0; i != summaryies.length; ++i) {
            ClassSummary cs = summaryies[i];
            out.println("<tr><td nowrap align=\"right\">" + cs.frequency + "&nbsp;</td><td nowrap align=\"left\">&nbsp;" + cs.klass + "</td></tr>");
        }
    }

}

/**
 * A HeapSegmentsInstantiator instance is used to instantiate the variables in the "segments.html" template.
 */
class HeapSegmentsInstantiator implements Template.Instantiator {

    final Segment[] segments;
    final long memorySize;
    final int graphHeight;
    final long totalMemory;

    HeapSegmentsInstantiator(Heap heap, long start, long end, int graphHeight) {
        this.segments = heap.getContiguousSegments(start, end);
        this.memorySize = (end - start);
        this.graphHeight = graphHeight;
        this.totalMemory = heap.getTotalMemory();
    }

    /**
     * {@inheritDoc}
     */
    public boolean instantiate(PrintStream out, String key) {
        if (key.equals("graph")) {
            doSegmentsGraph(out);
        } else if (key.equals("table")) {
            doSegmentsTable(out);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Replace the 'graph' variable.
     *
     * @param out  writes the replacement to this stream
     */
    private void doSegmentsGraph(PrintStream out) {
        for (int i = segments.length - 1; i >= 0; --i) {
            Segment segment = segments[i];

            long height = (segment.getSize() * graphHeight) / memorySize;
            if (height < 1) {
                height = 1;
            }

            String color = segment.getColor();
            out.println("<tr><td width=\"180\" height=\"" + height + "\" bgColor=\"" + color + "\"></td></tr>");
        }
    }

    /**
     * Replace the 'table' variable.
     *
     * @param out  writes the replacement to this stream
     */
    private void doSegmentsTable(PrintStream out) {
        for (int i = segments.length - 1; i >= 0; --i) {
            Segment segment = segments[i];

            String style = "color: " + segment.getColor() + ";";
            out.println("<tr align=\"left\" style=\"" + style + "\">" +
                        "<td align=\"right\">" + segment.label + "</td>" +
                        "<td>" + segment.start + "</td>" +
                        "<td>" + segment.end + "</td>" +
                        "<td>" + (segment.end - segment.start) + "</td>" +
                        "</tr>");
        }
    }
}


/**
 * A HeapInstantiator instance is used to instantiate the variables in the "heap.html" template.
 */
class HeapInstantiator implements Template.Instantiator {

    final Heap heap;
    HeapInstantiator(Heap heap) {
        this.heap = heap;
    }

    /**
     * {@inheritDoc}
     */
    public boolean instantiate(PrintStream out, String key) {
        if (key.equals("contents")) {
            doContents(out);
            return true;
        } else {
            return false;
        }
    }

    private static char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    /**
     * Converts a word value to a hexadecimal string with a space between each byte.
     *
     * @param word   the value to convert
     * @return 'word' as a hexadecimal string
     */
    private String toSpacedHex(long word) {
        StringBuffer hex = new StringBuffer(3 * HDR.BYTES_PER_WORD);
        for (int i = 0 ; i != HDR.BYTES_PER_WORD; i++) {
            hex.append(' ').append(HEX_DIGITS[(int)(word & 0xF)]);
            word = word >> 4 ;
            hex.append(HEX_DIGITS[(int)(word & 0xF)]);
            word = word >> 4;
        }
        return hex.toString();
    }

    /**
     * Replace the 'contents' variable.
     *
     * @param out  writes the replacement to this stream
     */
    private void doContents(PrintStream out) {
        Enumeration objects = heap.getObjects();
        while (objects.hasMoreElements()) {
            HeapObject object = (HeapObject)objects.nextElement();

            long address = object.getStart();
            long oop = object.getOop();
            Enumeration data = object.getData();
            while (data.hasMoreElements()) {
                // Open row
                out.print("<tr align=\"left\">");

                // Print class or space
                if (address == oop) {
                    out.print("<td align=\"right\">" + object.getKlass() + "</td>");
                } else {
                    out.print("<td>&nbsp;</td>");
                }

                // Print address cell
                if (address == oop) {
                    out.print("<td style=\"color: red;\">" +
                              "<a name=\"a" + address + "\">" + address + "</a></td>");
                } else {
                    out.print("<td align=\"right\">" + address + "</td>");
                }

                // Print data cell in decimal
                long word = Main.parseUWord((String)data.nextElement());
                if (heap.getObject(word) != null) {
                    out.print("<td><a href=\"#a"+word+"\">" + word + "</a></td>");
                } else {
                    out.print("<td>" + word + "</td>");
                }

                // Print data cell in hex
                out.print("<td style=\"font-family: monospace\">" + toSpacedHex(word) + "</td>");

                // Close row
                out.println("</tr>");

                address += HDR.BYTES_PER_WORD;
            }
//            assert address == object.getEnd() : "address=" + address + " end=" + object.getEnd();

        }

        out.close();
    }

}

/**
 * A template encapsulates the content and variables in a template file.
 */
class Template {

    /**
     * The file from which the template was read.
     */
    private final File file;

    /**
     * The content of the template.
     */
    private final String content;

    /**
     * A map from positions in the template to the variables that must be replaced by dynamic content.
     */
    private final SortedMap<Integer, String> variables;

    /**
     * Generates a RuntimeException for an error that occurs when instantiating this template.
     *
     * @param position    the current position in the template
     * @param msg         error message
     * @param e           cause of error or null
     * @return a RuntimeException instance detailing the location and nature of the error
     */
    private RuntimeException error(int position, String msg, Throwable e) {
        int line = 1;
        int index = content.indexOf('\n', 0);
        while (index != -1 && index < position) {
            line++;
            index = content.indexOf('\n', index + 1);
        }
        if (e != null) {
            throw new RuntimeException(file.getPath() + ':' + line + ':' + msg, e);
        } else {
            throw new RuntimeException(file.getPath() + ':' + line + ':' + msg);
        }
    }

    /**
     * Creates a template based on a given file.
     *
     * @param file File
     */
    Template(File file) throws IOException {
        this.file = file;
        int length = (int)file.length();
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[length];
        fis.read(data);

        this.content = new String(data);
        this.variables = new TreeMap<Integer, String>();

        Pattern pattern = Pattern.compile("\\$\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            int start = matcher.start();
            int varStart = matcher.start(1);
            int varEnd = matcher.end(1);
            String variable = content.substring(varStart, varEnd);
            variables.put(new Integer(start), variable);
        }
    }

    /**
     * Instantiates this template to a given output stream using a given instantiator to replace
     * any variables in the template with content.
     *
     * @param out          where the instantiated template is written
     * @param instantiator the object used to bind content to the variables in the template (if any)
     */
    void instantiate(PrintStream out, Instantiator instantiator) {

        int position = 0;
        Iterator iterator = variables.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            int start = ((Integer)entry.getKey()).intValue();
            String variable = (String)entry.getValue();

            // Copy interleaving content
            if (position < start) {
                out.print(content.substring(position, start));
            }

            if (instantiator == null || !instantiator.instantiate(out, variable)) {
                error(start, "unknown variable '" + variable + "'", null);
            }

            position = start + "${}".length() + variable.length();
        }
        if (position < content.length()) {
            out.print(content.substring(position));
        }
    }

    /**
     * An Instantiator instance is used to instantiate some content for every variable in a template.
     */
    static interface Instantiator {

        /**
         * Generates some content to replace a variable in some content being generated from a template.
         *
         * @param out   the stream to which the generated content is written
         * @param key   the variable for which content is to be generated
         * @return  true if the variable was recognised by this Instantiator instance
         */
        public boolean instantiate(PrintStream out, String key);
    }

}

/**
 * A Heap instance represents a heap as described by a heap trace.
 */
class Heap {

    /**
     * A description of when the trace was taken. For example, "after collection".
     */
    private final String description;

    /**
     * The segments the heap is partitioned into.
     */
    private final Vector<Segment> segments;

    /**
     * The objects in the heap.
     */
    private final Vector<HeapObject> objects;

    /**
     * Maps oops to objects;
     */
    private LongHashtable objectMap;

    /**
     * The branch count when the trace was taken. This will be -1 if VM was not built with tracing enabled.
     */
    private final long branchCount;

    /**
     * The amount of free memory in this heap.
     */
    private final long freeMemory;

    /**
     * The amount of total memory in this heap.
     */
    private final long totalMemory;

    /**
     * Creates a Heap instance for a single heap trace.
     *
     * @param description  a description of when the trace was taken
     * @param freeMemory  the amount of free memory as a percent of total memory
     */
    Heap(String description, long branchCount, long freeMemory, long totalMemory) {
        this.description = description;
        this.segments = new Vector<Segment>();
        this.objects = new Vector<HeapObject>();
        this.branchCount = branchCount;
        this.freeMemory = freeMemory;
        this.totalMemory = totalMemory;
    }

    /**
     * Adds a segment description to this heap.
     *
     * @param segment   the segment to add
     */
    void addSegment(Segment segment) {
        segments.addElement(segment);
    }

    /**
     * The branch count when the trace was taken. This will be -1 if VM was not built with tracing enabled.
     *
     * @return the branch count when the trace was taken
     */
    long getBranchCount() {
        return branchCount;
    }

    /**
     * Gets the amount of free memory in this heap.
     *
     * @return the amount of free memory in this heap
     */
    long getFreeMemory() {
        return freeMemory;
    }

    /**
     * Gets the amount of free memory in this heap.
     *
     * @return the amount of free memory in this heap
     */
    long getTotalMemory() {
        return totalMemory;
    }

    /**
     * Appends an object's description to this heap.
     *
     * @param object   the object to add
     */
    void appendObject(HeapObject object) {
        if (!objects.isEmpty()) {
            HeapObject previous = (HeapObject)objects.lastElement();
//            assert object.getStart() == previous.getEnd() : "start=" + object.getStart() + " previousEnd=" + previous.getEnd();
        }

        objects.addElement(object);
        if (objectMap != null) {
            objectMap.put(object.getOop(), object);
        }
    }

    /**
     * Gets the object referred to by a given oop.
     *
     * @param oop   a pointer to an object's body
     * @return the HeapObject referred to by <code>oop</code>
     */
    HeapObject getObject(long oop) {
        if (objectMap == null) {
            objectMap = new LongHashtable(objects.size());
            for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
                HeapObject object = (HeapObject)e.nextElement();
                objectMap.put(object.getOop(), object);
            }
        }
        return (HeapObject)objectMap.get(oop);
    }

    /**
     * Gets an enumeration over all the objects in the heap. The returned enumeration
     * is sorted by starting addresses in ascending order.
     *
     * @return an enumeration over all the objects in the heap
     */
    Enumeration getObjects() {
        return objects.elements();
    }

    /**
     * Gets the description of when this heap trace was taken.
     *
     * @return String
     */
    String getDescription() {
        return description;
    }

    /**
     * Gets a set of segments that account for every address in a given memory range.
     *
     * @param start   the start of the memory range
     * @param end     the end of the memory range
     * @return a set of sorted segments that account for every address in the range <code>[start .. end)</code>
     */
    Segment[] getContiguousSegments(long start, long end) {
        Segment[] segs = new Segment[segments.size()];
        segments.copyInto(segs);
        Arrays.sort(segs);

        Vector<Segment> contiguousSegments = new Vector<Segment>(segs.length * 2);
        long previousEnd = start;
        for (int i = 0; i < segs.length; ++i) {
            Segment s = segs[i];
            addUnusedSegment(contiguousSegments, previousEnd, s.start);
            contiguousSegments.addElement(s);
            previousEnd = s.end;
        }
        addUnusedSegment(contiguousSegments, previousEnd, end);

        if (segs.length != contiguousSegments.size()) {
            segs = new Segment[contiguousSegments.size()];
            contiguousSegments.copyInto(segs);
        }
        return segs;
    }

    /**
     * Creates and adds a segment to a vector of segments if the next segment that will be
     * added start at an address greater than the end of the last segment added to the vector.
     *
     * @param segments     a vector of segments
     * @param previousEnd  the end of the (conceptually) last segment in the vector
     * @param nextStart    the start of the next segment that will (conceptually) be added to the vector
     */
    private static void addUnusedSegment(Vector<Segment> segments, long previousEnd, long nextStart) {
        if (nextStart > previousEnd) {
            segments.addElement(new Segment("unused", previousEnd, nextStart));
        }

    }
}

class HeapObject {

    /**
     * The class of the object.
     */
    private final String klass;

    /**
     * The address at which the object's header begins.
     */
    private final long start;

    /**
     * The size (in words) of the object's header.
     */
    private final int headerSize;

    /**
     * The size (in words) of the object's body.
     */
    private final int bodySize;

    /**
     * The data in the object.
     */
    private final String[] data;

    HeapObject(String klass, long start, int headerSize, int bodySize, String[] data) {
        this.klass = klass;
        this.start = start;
        this.headerSize = headerSize;
        this.bodySize = bodySize;
        this.data = data;
    }

    /**
     * Gets the address at which the object's header begins.
     *
     * @return the address at which the object's header begins
     */
    long getStart() {
        return start;
    }

    /**
     * Gets the total size (in bytes) of this object.
     *
     * @return the size (in bytes) of this object's header and body
     */
    int getSize() {
        return (headerSize + bodySize) * HDR.BYTES_PER_WORD;
    }

    /**
     * Gets the address of the next word after the end of this object.
     *
     * @return the address of the next word after the end of this object
     */
    long getEnd() {
        return start + getSize();
    }

    String getKlass() {
        return klass;
    }

    Enumeration getData() {
        return new Enumeration() {
            int index = 0;
            int repeats = 0;
            public boolean hasMoreElements() {
                return index != data.length;
            }
            public Object nextElement() {
                if (index >= data.length) {
                    throw new NoSuchElementException(""+index);
                }
                if (repeats == 0) {
                    String d = data[index];
                    if (d.charAt(0) == '*') {
                        repeats = Integer.parseInt(d.substring(1));
                        d = data[index - 1];
                        if (--repeats == 0) {
                            index++;
                        }
                    } else {
                        index++;
                    }
                    return d;
                } else {
                    String d = data[index - 1];
                    if (--repeats == 0) {
                        index++;
                    }
                    return d;
                }
            }
        };
    }

    /**
     * Gets the address at which the object's body begins.
     *
     * @return the address at which the object's body begins
     */
    long getOop() {
        return start + (headerSize * HDR.BYTES_PER_WORD);
    }
}

/**
 * A Segment instance encapsulates the information for a segment of memory in the heap trace.
 */
class Segment implements Comparable {
    final String label;
    final long start;
    final long end;

    Segment(String label, long start, long end) {
        this.label = label;
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the size of this segment.
     *
     * @return the size of this segment
     */
    long getSize() {
        return end - start;
    }

    /**
     * Gets the color for this segment.
     *
     * @return  the color for this segment
     */
    String getColor() {
        return Segment.getColor(this);
    }

    /**
     * Compares this segment to another segment returning -1 if this segment is at a lower
     * address than 'other', 1 if this segment is at a higher address than 'other' and 0 if
     * 'other' is this segment.
     *
     * @param other   the segment to compare against
     * @return the result of the comparison
     */
    public int compareTo(Object other) {
        if (this == other) {
            return 0;
        }
        Segment s = (Segment)other;
        if (s.start > this.start) {
            return -1;
        } else {
            if (s.start == this.start) {
                throw new RuntimeException("segment starting addresses should be unique: " + this + " -- " + other);
            }
            return 1;
        }
    }

    /**
     * The set of available colors.
     */
    private static final String[] COLORS = { "blue", "green", "lime", "maroon", "navy", "olive", "purple", "red", "fuchsia" };

    /**
     * Used to allocate colors to segments.
     */
    private static Hashtable<String, String> colorMap = new Hashtable<String, String>();

    /**
     * Gets the color for a segment, allocating it first if necessary.
     *
     * @param segment   the segment
     * @return  the color for <code>segment</code>
     */
    private static String getColor(Segment segment) {
        String label = segment.label;
        if (label.equals("unused")) {
            return "gray";
        }
        String color = (String)colorMap.get(label);
        if (color == null) {
            color = COLORS[colorMap.size() % COLORS.length];
            colorMap.put(label, color);
        }
        return color;
    }
}
