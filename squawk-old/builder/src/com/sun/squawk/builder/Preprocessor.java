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
import java.util.*;
import java.util.regex.*;
import com.sun.squawk.builder.util.*;

/**
 * A Preprocessor instance is used to process one or more input files in a manner similar to
 * the standard C preprocessor. The patterns recognized by the Preprocessor and their corresponding
 * semantics are described in the Preprocessor.html document.
 *
 */
public class Preprocessor {

    /**
     * Enables/disables verbose execution.
     * <p>
     * The default value is <code>false</code>.
     */
    public boolean verbose;

    /**
     * The printstream to which verbose output will be sent.
     * <p>
     * The default value is {@link System#out}.
     */
    public PrintStream log = System.out;

    /**
     * The properties supplying values for names used in conditional compilation and constant value
     * preprocessing directives.
     */
    public Properties properties;

    /**
     * If true, then lines of code to be conditionally excluded during preprocessing are
     * prefixed by a C++ style comment. Otherwise, they are replaced by the empty string.
     * <p>
     * The default value is <code>true</code>.
     */
    public boolean disableWithComments = true;

    /**
     * If true, then any <code>S64</code> directives are processed to produce code intended for
     * deployment on a 64 bit system.
     */
    public boolean processS64;

    /**
     * Determines if calls to methods in the Assert class are processed. This will typically be true
     * for code to be deployed in Squawk and false for code that will only be deployed in a standard
     * J2SE VM.
     * <p>
     * The default value is <code>true</code>.
     */
    public boolean processAssertions = true;

    /**
     * If <code>true</code> and {@link #processAssertions} is also <code>true</code>, then assertions
     * are left enabled. Otherwise, they are disabled.
     * <p>
     * The default value is <code>false</code>.
     */
    public boolean assertionsEnabled;
    
    /**
     * If <code>true</code> then assertion failure messages contain the file and line number.
     * <p>
     * The default value is <code>false</code>.
     */
    public boolean showLineNumbers = true;

    /**
     * Processes a set of files placing the resulting output in a given directory.
     *
     * @param inputFiles  the files to be processed
     * @param destDir     the directory where the processed files are to be written
     */
    public void execute(FileSet inputFiles, File destDir) {
        for (File inputFile: inputFiles.list()) {
            if (inputFile.length() != 0) {
                File outputFile = inputFiles.replaceBaseDir(inputFile, destDir);
                execute(inputFile, outputFile);
            }
        }
    }

    /**
     * Used for debugging purposes, print out all properties and their values.
     *
     */
    protected void printProperties() {
        for (Map.Entry<?, ?> entry: properties.entrySet()) {
            System.out.print(entry.getKey());
            System.out.print("=");
            System.out.println(entry.getValue());
        }
    }
    
    /**
     * A per-file flag indicating if assertions are to be made fatal.
     */
    private boolean makeAssertionsFatal;

    /**
     * Processes a given file.
     *
     * @param inputFile   the file to process
     * @param outputFile  the file to which the output should be written
     */
    public void execute(File inputFile, File outputFile) {
        FileReader fr = null;
        LineReader in = null;
        try {

            // Open file
            fr = new FileReader(inputFile);
            in = new LineReader(fr, inputFile.getPath(), 200);

            // Process the first line to see if there is a file-exclusion directive
            String line = new BufferedReader(fr).readLine();
            if (line == null) {
                throw new BuildException("Cannot preprocess empty file: " + inputFile);
            }
            Matcher m = EXCLUDE_FILE_DIRECTIVE.matcher(line);
            if (m.matches()) {
                String name = m.group(1);
                boolean value;
                if (name.charAt(0) == '!') {
                    name = name.substring(1);
                    value = !getBooleanProperty(name);
                } else {
                    value = getBooleanProperty(name);
                }
                if (!value) {
                    if (verbose) {
                        log.println(inputFile + ": excluded file");
                    }
                    return;
                }
            }

            // Now that the file is to be kept, re-open it and create the output writer
            fr.close();
            fr = new FileReader(inputFile);
            in = new LineReader(fr, inputFile.getPath(), (int)inputFile.length());
            Build.mkdir(outputFile.getParentFile());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
            PrintWriter out = new PrintWriter(bw);
            makeAssertionsFatal = false;
            process(in, out, null);
            out.close();

            if (verbose) {
                log.println(in.getSource() + ": preprocessed");
            }
        } catch (PreprocessorException e) {
            throw new BuildException("Error preprocessing " + in.getContext() + ": " + e.getMessage());
        } catch (IOException e) {
            throw new BuildException("IO error preprocessing " + inputFile.getPath() + ": " + e.getMessage(), e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ex) {
                    throw new BuildException("error closing file reader for preprocessor", ex);
                }
            }
        }
    }

    /**
     * Pattern for matching a line starting with "//if[...]".
     * Capturing group 1 is property name controlling the directive.
     */
    private static final Pattern EXCLUDE_FILE_DIRECTIVE = Pattern.compile("^//if\\[([^\\]]+)\\].*");

    /**
     * Pattern for matching a line starting a conditionally compiled block of code.
     * Capturing group 1 is property name controlling the directive.
     */
    private static final Pattern IF_DIRECTIVE = Pattern.compile("^/\\*if\\[([^\\]]+)\\]\\*/.*");

    /**
     * Pattern for matching a line starting the else clause of conditionally compiled block of code.
     * Capturing group 1 is property name controlling the directive.
     */
    private static final Pattern ELSE_DIRECTIVE = Pattern.compile("^/\\*else\\[([^\\]]+)\\]\\*/.*");

    /**
     * Pattern for matching a line ending a conditionally compiled block of code.
     * Capturing group 1 is property name controlling the directive.
     */
    private static final Pattern END_DIRECTIVE = Pattern.compile("^/\\*end\\[([^\\]]+)\\]\\*/.*");

    /**
     * Pattern for matching a VAL directive.
     * Capturing group 1 is the text to be replaced, group 2 is property name whose value is
     * to be used for the replacement and group 3 is the remainder of the line.
     */
    private static final Pattern VALUE_DIRECTIVE = Pattern.compile("/\\*VAL\\*/([\\w\\.]+)/\\*([\\w\\.]+)\\*/(.*)");

    /**
     * Pattern for matching a S64 directive.
     * Capturing group 1 is the text before the S64 directive, group 2 is the "int", "Int" or "INT" text to replace
     * and group 3 is the remainder of the line.
     */
    private static final Pattern S64_PREFIX_DIRECTIVE = Pattern.compile("(.*)/\\*S64\\*/(Int|INT|int)(.*)");

    /**
     * Pattern for matching a S64 directive.
     * Capturing group 1 is the text before the S64 directive, group 2 is the "int", "Int" or "INT" text to replace
     * and group 3 is the remainder of the line.
     */
    private static final Pattern S64_SUFFIX_DIRECTIVE = Pattern.compile("(.*)(Int|INT|int)/\\*S64\\*/(.*)");

    /**
     * A <code>Conditional</code> instance represents the scope of a conditional in a file being preprocessed.
     * These scopes can be nested.
     */
    static class ConditionalScope {

        ConditionalScope(String name, boolean value, ConditionalScope outer) {
            this.name = name;
            this.value = value;
            this.outer = outer;
        }

        /**
         * The name of the property whose value is the predicate for the conditional.
         */
        final String name;

        /**
         * The value of the predicate for the conditional.
         */
        final boolean value;

        /**
         * The conditional in which this conditional is nested.
         */
        final ConditionalScope outer;

        /**
         * Specifies if processing of this conditional is in the 'else' clause.
         */
        boolean elseClause;

        /**
         * Determines if the block of code controlled by this conditional is to be enabled in the output.
         *
         * @return true if the code is to be enabled in the output
         */
        boolean enabled() {
            if (outer != null && !outer.enabled()) {
                return false;
            }
            return value != elseClause;
        }
    }

    /**
     * Gets the value of a boolean property. If there is no entry in {@link #properties} named <code>name</code>
     * then <code>true</code> is returned. Otherwise, the value must be "true" or "false".
     *
     * @param name       the name of the property
     * @return the value of the property named <code>name</code>
     */
    private boolean getBooleanProperty(String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            throw new PreprocessorException("no value for property '" + name + "'");
        }
        if (value.equals("false")) {
            return false;
        } else {
            if (!value.equals("true")) {
                throw new PreprocessorException("property '" + name + "' does have boolean value of \"true\" or \"false\"");
            }
            return true;
        }
    }

    /**
     * Gets the value of a property.
     *
     * @param name       the name of the property
     * @param mustExist  if true and there is no property corresponding to <code>name</code>, then this method throws a BuildException
     * @return the value of the property named <code>name</code>
     */
    private String getProperty(String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            throw new PreprocessorException("value for required property '" + name + "' not specified");
        }
        return value;
    }

    /**
     * Matches a line against an 'if', 'else' or 'end' conditional compilation directive.
     *
     * @param pattern   the pattern to match
     * @param line      the line to test
     * @return the name of the property that is the predicate for the conditional compilation
     */
    private static String matchConditionalDirective(Pattern pattern, String line) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            throw new PreprocessorException("malformed preprocessing directive - expected '" + pattern.pattern()+ "'");
        }
        return matcher.group(1);
    }

    /**
     * Process the input.
     *
     * @param in          the input to process
     * @param out         the stream to which the processed output should be written
     * @param conditional the currently conditional compilation scope (if any)
     */
    private void process(LineReader in, PrintWriter out, ConditionalScope conditional) {

        boolean outputEnabled = conditional == null || conditional.enabled();

        String line;
        while((line = in.readLine()) != null) {
            if (line.startsWith("/*MAKE_")) {
                if (line.equals("/*MAKE_ASSERTIONS_FATAL[true]*/")) {
                    makeAssertionsFatal = true;
                } else if (line.equals("/*MAKE_ASSERTIONS_FATAL[false]*/")) {
                    makeAssertionsFatal = false;
                } else {
                    throw new PreprocessorException("malformed preprocessing directive - expected '/*MAKE_ASSERTIONS_FATAL[true]*/' or '/*MAKE_ASSERTIONS_FATAL[false]*/'");
                }
                out.println();  // Replace the directive with an empty line
            } else if (line.startsWith("/*if[")) {
                // Process /*if[...]*/ directive
                String name = matchConditionalDirective(IF_DIRECTIVE, line);
                boolean value;
                if (name.charAt(0) == '!') {
                    name = name.substring(1);
                    value = !getBooleanProperty(name);
                } else {
                    value = getBooleanProperty(name);
                }
                ConditionalScope inner = new ConditionalScope(name, value, conditional);
                out.println();  // Replace the directive with an empty line
                process(in, out, inner);
            } else if (line.startsWith("/*else[")) {
                // Process /*else[...]*/ directive
                String name = matchConditionalDirective(ELSE_DIRECTIVE, line);
                if (conditional == null || !conditional.name.equals(name)) {
                    throw new BuildException("'else' directive does not match a preceeding 'if' directive");
                }
                conditional.elseClause = true;
                outputEnabled = conditional.enabled();
                out.println();  // Replace the directive with an empty line
            } else if (line.startsWith("/*end[")) {
                // Process /*end[...]*/ directive
                String name = matchConditionalDirective(END_DIRECTIVE, line);
                if (conditional == null || !conditional.name.equals(name)) {
                    throw new BuildException("'end' directive does not match a preceeding 'if' directive");
                }
                out.println();  // Replace the directive with an empty line
                return;
            } else {
                if (outputEnabled) {

                    // Strip leading '//' from 'else' clause
                    if (conditional != null && conditional.elseClause) {
                        if (!line.startsWith("//")) {
                            throw new PreprocessorException("lines in 'else' clause must start with '//'");
                        }

                        line = "  " + line.substring(2);
                    }

                    // Process /*VAL*/.../*...*/ directive
                    line = processValueDirective(line);

                    // Process /*S64*/ directive
                    if (processS64) {
                        line = process64BitDirective(line);
                    }

                    // Process assertions
                    if (processAssertions) {
                        line = processAssertions(in, line, makeAssertionsFatal);
                    }

                    // Output the line
                    out.println(line);
                } else {
                    if (disableWithComments) {
                        out.println("//" + line);
                    } else {
                        out.println();
                    }
                }
            }
        }

        if (conditional != null) {
            throw new PreprocessorException("incomplete 'if' directive");
        }
    }

    /**
     * Searches for the pattern "/*VAL* /<value>/*<name>* / and if found, replaces 'value' with the value of the
       property denoted by 'name'.
     *
     * @param line     the line being processed
     * @return the content of the line after the replacement (if any) has been performed
     */
    private String processValueDirective(String line) {
        int index = line.indexOf("/*VAL*/");
        if (index != -1) {
            Matcher matcher = VALUE_DIRECTIVE.matcher(line.substring(index));
            if (!matcher.matches()) {
                throw new PreprocessorException("malformed preprocessing directive - expected '" + VALUE_DIRECTIVE.pattern()+ "'");
            }

            String name = matcher.group(2);
            String value = getProperty(name);
            if (value != null) {
                return line.substring(0, index) + value + matcher.group(3);
            }
        }
        return line;
    }

    /**
     * Replaces "int", "Int" or "INT" with "long", "Long" or "LONG" respectively.
     *
     * @param text   the text to replace
     * @return the replacement
     */
    private static String intToLong(String text) {
        if (text.equals("int")) {
            return "long";
        } else if (text.equals("Int")) {
            return "Long";
        } else {
            assert text.equals("INT");
            return "LONG";
        }

    }

    /**
     * Searches for the pattern "/*S64* /" prefixed or suffixed by "int", "Int" or "INT"
     * and if found, replaces the prefix or suffix with "long", "Long" or "LONG" respectively.
     *
     * @param infile   the file being processed
     * @param line     the line being processed
     * @param lno      the number of the line being processed
     * @return the content of the line after the replacement (if any) has been performed
     */
    private String process64BitDirective(String line) throws BuildException {
        if (line.indexOf("/*S64*/") != -1) {
            Matcher matcher = S64_PREFIX_DIRECTIVE.matcher(line);
            if (!matcher.matches()) {
                matcher = S64_SUFFIX_DIRECTIVE.matcher(line);
                if (!matcher.matches()) {
                    throw new PreprocessorException("malformed preprocessing directive - expected '" + S64_PREFIX_DIRECTIVE.pattern() + "' or '" + S64_SUFFIX_DIRECTIVE + "'");
                }
            }
            line = matcher.group(1) + intToLong(matcher.group(2)) + matcher.group(3);

            // Recurse in case there are any more S64 directives on the line
            return process64BitDirective(line);
        }
        return line;
    }

    /**
     * Converts a call to a method in the Assert class to use the fatal version of that call.
     * No conversion is done if the call is already the fatal version.
     *
     * @param line      the line containing the call
     * @param bracket   the index of the opening '(' of the call
     * @return the converted line
     */
    private String makeAssertionFatal(String line, int bracket) {
        // Only convert to a fatal assert if it isn't already one
        if (line.lastIndexOf("Fatal", bracket) == -1 && line.lastIndexOf("always", bracket) == -1) {

            // convert call to fatal version
            line = line.substring(0, bracket) + "Fatal" + line.substring(bracket);
        }
        return line;
    }

    /**
     * Converts a call to a method in the Assert class so that the input context (i.e. file and line number) is prepended
     * to the String constant passed as the first parameter.
     *
     * @param line      the line containing the call
     * @param invoke    the index of the "Assert." in the line
     * @param in        the LineReader supplying the context of <code>line</code>
     * @return the converted line
     */
    private String prependContext(String line, int invoke, LineReader in) {
        File inFile = new File(in.getSource());
        String context = "\"" + inFile.getName() + "\", " + in.getLastLineNumber();
            int bracket = line.lastIndexOf(");");
            if (bracket != -1) {
                String comma = ", ";
                if (line.charAt(bracket - 1) == '(') {
                    comma = "";
                }
                line = line.substring(0, bracket) + comma + context + line.substring(bracket);
            }
        return line;
    }

    /**
     * Process any calls to methods in the Assert class. If assertions are enabled and the line
     * contains a call to a method in the Assert class that passes a String constant as a parameter,
     * then the current file name and line number are prepended to the string. Otherwise, if
     * assertions are not enabled and the line contains a call to a method in the Assert class, the
     * line is commented out.
     *
     * @param in                  the LineReader supplying the context of <code>line</code>
     * @param line                the line to process
     * @param makeAssertionsFatal if true and assertions are {@link #assertionsEnabled enabled} then convert and assertions
     *                            to the form that causes a fatal VM error when the predicate fails
     * @return the processed line
     */
    public String processAssertions(LineReader in, String line, final boolean makeAssertionsFatal) {
        int invoke = line.indexOf("Assert.");
        if (invoke != -1 && line.indexOf('(', invoke) != -1) {
            if (!assertionsEnabled) {
                int bracket = line.indexOf('(', invoke);
                if (makeAssertionsFatal) {
                    line = makeAssertionFatal(line, bracket);
                }

                String method = line.substring(invoke + "Assert.".length(), line.indexOf('(', invoke));
                if (method.startsWith("that") || method.startsWith("should")) {
                    String newLine;
                    if (line.lastIndexOf("throw", invoke) != -1) {
                        // We can either throw null, or a more specific RuntimeException. In either case the failure is detected.
                        newLine = line.substring(0, invoke) + "!Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED ? (RuntimeException)null : " + line.substring(invoke);
                    } else if (method.startsWith("that")) {
                        // Change "Assert..." into "if (false) Assert..."
                        newLine = line.substring(0, invoke) + "if (false) " + line.substring(invoke);
                    } else {
                        if (showLineNumbers) {
                            line = prependContext(line, invoke, in);
                        }
                        newLine = line.substring(0, invoke) + "if (Assert.SHOULD_NOT_REACH_HERE_ALWAYS_ENABLED) " + line.substring(invoke);
                    }
                    line = newLine;
                } else if (method.startsWith("always")) {
                    if (showLineNumbers) {
                        line = prependContext(line, invoke, in);
                    }
                }
            } else {
                if (makeAssertionsFatal) {
                    line = makeAssertionFatal(line, line.indexOf('(', invoke));
                }
                if (showLineNumbers) {
                    // prepend file name and line number to message
                    line = prependContext(line, invoke, in);
                }

            }
        }
        return line;
    }
}

/**
 * The exception thrown when there is an error parsing or processing one of the preprocessor directives.
 */
class PreprocessorException extends RuntimeException {

    public PreprocessorException(String msg) {
        super(msg);
    }
}