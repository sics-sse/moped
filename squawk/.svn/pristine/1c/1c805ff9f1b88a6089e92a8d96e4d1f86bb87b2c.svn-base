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
import com.sun.squawk.builder.util.*;


/**
 * A Macroizer instance is used to recognise certain patterns (in comments) before C function
 * definitions and convert those functions into either macro definitions or inline function
 * definitions.
 * <p>
 * The patterns recognized by the Macroizer and their corresponding semantics are described
 * in the Macroizer.html document.
 * @todo write Macroizer.html document
 *
 */
public class Macroizer {

    public void execute(File inputFile, File outputFile, boolean macroize) {
        FileReader fr = null;
        LineReader in = null;
        PrintWriter out = null;
        try {
            fr = new FileReader(inputFile);
            in = new LineReader(fr, inputFile.getPath(), (int)inputFile.length());
            out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            execute(in, out, macroize);
        } catch (MacroizerException e) {
            throw new BuildException("Error macroizing " + in.getContext() + ": " + e.getMessage());
        } catch (IOException e) {
            throw new BuildException("IO error macroizing " + inputFile.getPath() + ": " + e.getMessage(), e);
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    throw new BuildException("error closing file reader for macroizer", e);
                }
            }
            if (out != null) {
                out.close();
            }
        }
    }

/*
    private void execute(LineReader in, Writer out) {

    }

    private Function parseFunction(LineReader in, Writer out, String conversion) {
        Function function = new Function(conversion);

        LineNumberReader lnr;

        return function;
    }

    static class Function {

        final String conversion;
        Function(String conversion) {
            this.conversion = conversion;
        }
    }
*/

    private void execute(LineReader in, PrintWriter out, boolean macroize) {
        String line;

        out.print("/**** Created by Squawk builder from \"" + in.getSource() + "\" ****/ ");

        // The current macro definition being parsed.
        MacroDefinition macro = null;

        while ((line = in.readLine()) != null) {

            // Expand /*INL*/, /*MAC*/ and /*DEF*/ lines
            boolean isMacroDefLine  = line.startsWith("/*MAC*/");
            boolean isDefineDefLine = line.startsWith("/*DEF*/");
            boolean isInlineDefLine = line.startsWith("/*INL*/");
            if (
                ((isMacroDefLine || macro != null) && macroize) ||
                (isDefineDefLine || macro != null) ||
                (isInlineDefLine || macro != null)
               ) {
                if (isMacroDefLine) {
                    line = line.substring("/*MAC*/".length()).trim();
                }
                if (isDefineDefLine) {
                    line = line.substring("/*DEF*/".length()).trim();
                }
                if (isInlineDefLine) {
                    line = line.substring("/*INL*/".length()).trim();
                }
                SpaceStreamTokenizer st = new SpaceStreamTokenizer(line);
                try {
                    if (isMacroDefLine || isDefineDefLine || isInlineDefLine) {
                        macro = new MacroDefinition(st, line, !isDefineDefLine);
                        if (isInlineDefLine) {
                            macro.setInline();

                        }
                    }

                    macro.parseStatements(st);

                    if (macro.nestingLevel == 0) {
                        Iterator<String> e = macro.lines.listIterator();
                        String macroDecl = e.next();
                        // Emit inlined function or macro definition
                        if (macro.inline()) {
                            line = macro.functionDeclLine;
                            line = "INLINE "+line.replace('$', ' ');
                        } else {
                            line = "#define " + macroDecl;
                            if (e.hasNext()) {
                                line += " \\";
                            }
                        }
                        writeLine(out, line);

                        // Emit the rest of the lines
                        while (e.hasNext()) {
                            line = e.next();

                            if (!macro.inline() && e.hasNext()) {
                                // Macro lines cannot use C++ style comments
                                if (line.indexOf("//") != -1) {
                                    throw new IOException("Cannot use C++ style comments inside a macro");
                                }

                                line += " \\";
                            }
                            writeLine(out, line);
                        }

                        macro = null;
                    }

                } catch (IOException ioe) {
                    throw new BuildException("Could not parse macro " + in.getContext() + ": "+ioe.getMessage());
                }
            } else {
                if (isMacroDefLine) {
                    line = line.substring("/*MAC*/".length()).trim();
                    line = "INLINE "+line;
                }
                if (line.startsWith("//")) {
                    line = "";
                } else {
                    if (line.indexOf('$') != -1) {
                        if (macroize) {
                            System.err.println("Warning: replacing '$' with '#' " + in.getContext());
                            line = line.replace('$', '#');
                        } else {
                            line = line.replace('$', '_');
                        }
                    }
                }
                writeLine(out, line);
            }
        }

        if (macro != null) {
            throw new BuildException("unclosed macro");
        }
    }

    /*
     * writeLine
     */
    void writeLine(PrintWriter out, String line) {
        String delims = " \t\n\r(){/";
        StringTokenizer st = new StringTokenizer(line, delims, true);
        while(st.hasMoreTokens()) {
            String next = st.nextToken();
            if (next.charAt(0) != '\r' && next.charAt(0) != '\n') {
                out.print(next);
            }
        }
        out.println();
    }
}

/**
 * This is a stream tokenizer that keeps track of the spaces preceeding
 * each token.
 */
class SpaceStreamTokenizer extends StreamTokenizer {
    private final static char[] SPACE_CHARS = new char[10000];
    static {
        for (int i = 0; i != SPACE_CHARS.length; i++) {
            SPACE_CHARS[i] = ' ';
        }
    }

    private int spaces;
    public int nextToken() throws IOException {
        spaces = 0;
        int t = super.nextToken();
        while (t == ' ') {
            spaces++;
            t = super.nextToken();
        }
        return t;
    }

    public void appendSpaces(StringBuffer buf) {
        if (spaces > 0) {
            buf.append(SPACE_CHARS, 0, spaces);
        }
    }

    public String getSpaces() {
        return new String(SPACE_CHARS, 0, spaces);
    }

    SpaceStreamTokenizer(String line) {
        super(new StringReader(line));
        for (char ch = ' '; ch != 0x7F; ch++) {
            if (Character.isJavaIdentifierPart(ch)) {
                super.wordChars(ch, ch);
            }
        }
        super.ordinaryChar(' ');
        // Turn off string recognition
        super.ordinaryChar('"');
        super.ordinaryChar('\'');
        // Turn off special comment recognition
        super.ordinaryChar('/');
    }
}



class MacroDefinition {
    /** A counter used to give each macro local variable a unique name. */
    private static int macroLocalVariableSuffix;

    /** The current block nesting level (i.e. number of open '{'s). */
    int nestingLevel;
    /** The original first line of the macro. */
    String functionDeclLine;
    /** */
//        String macroDeclLine;
    /** The lines of the macro with the parameter substitution. */
    ArrayList<String> lines;
    /** The block of inner locals declaration and initialisations. */
    String innerLocals;
    /** A flag determining whether or not the macro actual needs to
        be rewritten as an inline function. */
    private boolean inline;
    /** A flag determining whether or not the macro can be a function instead. */
    final boolean canInline;
    /** A flag determining if the macro return type is void. */
    boolean isVoid;
    /** A remembered set of the parameters that have been used. */
    List<String> usedParms;
    /** A map from parameter names (without the leading '$') to the
        name of the inner scope local variable used to guarantee
        the idempotent semantics of the actual parameter. */
    Hashtable<String, String> locals;
    int statementCount;

    StringBuffer currentLine;

    public void setInline() throws IOException {
        if (!canInline) {
            throw new IOException("Cannot convert /*DEF*/ into a macro (most likely because a parameter is used twice)");
        }
        inline = true;
    }

    public boolean inline() {
        return inline;
    }

    /**
     * Parse a function declaration and reformat it into a macro declaration.
     * @param st
     * @param macro
     * @param parameterMap Map of parameter names to local variable names.
     * @return
     * @throws IOException
     */
    MacroDefinition(SpaceStreamTokenizer st, String line, boolean canInline) throws IOException {
        this.functionDeclLine = line;
        this.canInline = canInline;

        currentLine = new StringBuffer(100);

        // Parse the access modifiers, return type and name of the function
        List<String> tokens = new ArrayList<String>();
        int token;
        while ((token = st.nextToken()) != '(') {
            if (token == SpaceStreamTokenizer.TT_WORD) {
                tokens.add(st.getSpaces()+st.sval);
            }
        }

        // Build the first line of the macro definition
        for (Iterator<String> e = tokens.listIterator(); e.hasNext();) {
            String t = e.next();
            if (e.hasNext()) {
                if (t.endsWith("void")) {
                    isVoid = true;
                }
            } else {
                // This is the function/macro name
                currentLine.append(t).append('(');
            }
        }

        locals = new Hashtable<String, String>();
        if (!isVoid) {
            usedParms = new ArrayList<String>();
        }

        // Parse the function parameters
        String parm = null;
        int dims = 0;
        StringBuffer localsDecl = (isVoid ? new StringBuffer() : null);
        do {
            token = st.nextToken();
            if (token == SpaceStreamTokenizer.TT_WORD) {
                parm = st.sval;
                if (isVoid) {
                    st.appendSpaces(localsDecl);
                    localsDecl.append(st.sval);
                }
            } else if (token == ',' || token == ')') {
                if (parm != null) {
                    currentLine.append(createLocal(parm, dims, localsDecl));
                    if (token == ',') {
                        currentLine.append(", ");
                    }
                }
                parm = null;
                dims = 0;
            } else if (token == '[' || token == ']') {
                if (isVoid && token == ']') {
                    dims++;
                }
            } else {
                if (token == SpaceStreamTokenizer.TT_EOF || token == SpaceStreamTokenizer.TT_EOL) {
                    throw new IOException("Macro declaration (up to '{') must be on one line");
                } else {
                    throw new IOException("Unexpected token while parsing macro declaration: "+st.sval);
                }
            }

        } while (token != ')');
        currentLine.append(") { ");

        if (st.nextToken() != '{') {
            throw new IOException("missing opening '{'");
        }
        nestingLevel++;

        if (isVoid) {
            currentLine.append(localsDecl.toString().replace('$', ' '));
        }
    }

    /**
     * Parse the stream up to and including the next ';' adding the parsed
     * content to the macro string being built.
     * @param st
     * @param macro
     * @return
     * @throws IOException
     */
    void parseStatements(SpaceStreamTokenizer st) throws IOException {
        int token = -1;
        while ((token = st.nextToken()) != SpaceStreamTokenizer.TT_EOF) {
            switch (token) {
                case SpaceStreamTokenizer.TT_WORD: {
                    st.appendSpaces(currentLine);
                    String parm = st.sval;
                    boolean isParm = parm.charAt(0) == '$';
                    if (isParm) {
                        if (!isVoid) {
                            if (usedParms.contains(parm)) {
                                setInline();
                            } else {
                                usedParms.add(parm);
                            }
                        }
                        parm = (String)locals.get(parm);
                        if (parm == null) {
                            throw new IOException("Cannot find parameter "+st.sval);
                        }
                        currentLine.append('(').append(parm).append(')');
                    } else {
                        currentLine.append(parm);
                    }
                    break;
                }
                case SpaceStreamTokenizer.TT_NUMBER: {
                    st.appendSpaces(currentLine);
                    // Cast the value to an int if it is an int value so that
                    // the StringBuffer does not append a double representation
                    // for non-double numbers.
                    if ((double)((int)st.nval) == st.nval) {
                        currentLine.append((int)st.nval);
                    } else {
                        currentLine.append(st.nval);
                    }
                    break;
                }
                case ';': {
                    ++statementCount;
                    if (!isVoid && statementCount > 1) {
                        setInline();
                    }
                    st.appendSpaces(currentLine);
                    currentLine.append(';');

                    break;
                }
                case '{': {
                    ++nestingLevel;
                    st.appendSpaces(currentLine);
                    currentLine.append('{');
                    break;
                }
                case '}': {
                    --nestingLevel;
                    st.appendSpaces(currentLine);
                    currentLine.append('}');
                    break;
                }
                default: {
                    char ctoken = (char)token;
                    st.appendSpaces(currentLine);
                    currentLine.append(ctoken);
                    break;
                }
            }
        }

        if (lines == null) {
            lines = new ArrayList<String>();
        }
        lines.add(currentLine.toString());

        if (nestingLevel == 0) {
            currentLine = null;
            finishedParsing();
        } else {
            currentLine = new StringBuffer(100);
        }
    }

    /**
     * Create an inner scoped local variable for a macro parameter. The local
     * variable will have a name based on the macro parameter name concatenated
     * with a unique suffix to ensure that all macro local variables have a
     * unique name.
     * @param parm The fucntion parameter (which must start with '$').
     * @param localsDecl The string buffer being used to build up the
     * declaration and initialisation
     * @return 'parm' with the leading '$' stripped off and a unique suffix
     * appended to it.
     */
    private String createLocal(String parm, int dims, StringBuffer localsDecl) throws IOException {
        if (parm.charAt(0) != '$') {
            throw new IOException("Macro parameters must start with '$'");
        }

        String base = parm.substring(1);
        String uniqParm = base + "_" + (macroLocalVariableSuffix++);

        if (isVoid) {
            String suffix = "_" + (macroLocalVariableSuffix++);
            String local = base + suffix;
            locals.put(parm, local);
            if (dims != 0) {
                int index = localsDecl.toString().lastIndexOf("$");
                localsDecl.insert(index+1, ASTERISKS, 0, dims);
            }
            localsDecl.append(suffix + " = " + uniqParm + "; ");
        } else {
            locals.put(parm, uniqParm);
        }
        return uniqParm;
    }
    private static final char[] ASTERISKS = { '*', '*', '*', '*' };

    void finishedParsing() throws IOException {
        if (inline) {
            String l = functionDeclLine;
            // Replace parameters with their unique names
            StringBuffer buf = new StringBuffer(l.length());

            int end = 0;
            while (true) {
                int index = l.indexOf('$', end);
                if (index != -1) {
                    if (end < index) {
                        buf.append(l.substring(end, index));
                    }

                    end = index;
                    while (end < l.length() &&
                           Character.isJavaIdentifierPart(l.charAt(end))) {
                        end++;
                    }
                    String uniqParm = (String)locals.get(l.substring(index, end));
                    buf.append(uniqParm);
                } else {
                    buf.append(l.substring(end));
                    break;
                }
            }
            functionDeclLine = buf.toString();

        }

        // Transform return statements
        if (!inline) {
            for (int i = 0; i != lines.size(); i++) {
                String line = lines.get(i);
                int retIndex = line.indexOf("return");
                if (retIndex != -1) {
                    if (!isVoid) {
                        line = line.substring(0, retIndex) +
                               line.substring(retIndex + "return".length());
                    } else {
                        if (line.charAt(retIndex + "return".length()) == ';') {
                            throw new IOException("Cannot use return statements in a void function that may become a macro");
                        }
                    }
                }
                if (!isVoid) {
                    line = line.replace('{', '(').
                                replace('}', ')').
                                replace(';', ' ');
                }
                lines.set(i, line);
            }
        }
    }
}

/**
 * The exception thrown when there is an error parsing or processing one of the macroizing directives.
 */
class MacroizerException extends RuntimeException {

    public MacroizerException(String msg) {
        super(msg);
    }
}