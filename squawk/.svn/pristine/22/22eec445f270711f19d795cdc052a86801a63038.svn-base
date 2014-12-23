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

package com.sun.cldc.jna;

import com.sun.squawk.builder.Build;
import com.sun.squawk.builder.util.FileSet;
import java.io.*;
import java.util.Iterator;
import java.util.regex.*;

/**
 * Class that preprocesses JNA import declarations and creates JNA definitions.
 * 
 *  - Preserves comments.
 *  - Strips out annotations
 */
public class SourceProcessor {
    boolean keepComments = true;
    boolean commentOutAnnotations = true;

    String libName = null;
    String inputFileName;
    
    Pattern packagePattern = Pattern.compile("package\\s*\\S+;");
    Pattern libraryPattern = Pattern.compile("interface\\s*(\\w+)\\s*extends\\s*Library");
    Pattern structPattern = Pattern.compile("class\\s*(\\w+)\\s*extends\\s*Structure");
    Pattern constImportPattern = Pattern.compile("(\\w+)(\\s*)=(\\s*)IMPORT");
    Pattern constDefinedPattern = Pattern.compile("(\\w+)(\\s*)=(\\s*)DEFINED");
    Pattern constSizeOfPattern = Pattern.compile("(\\w+)(\\s*)=(\\s*)SIZEOF");

    Pattern annotationPattern = Pattern.compile("@\\w+(\\(.*\\))?");

    String replaceString = "LIBRARY CLASS NOT PARSED CORRECTLY";
    
    /**
     * CommentSkippingReader parses input into alternating segments of
     * source code and comments.
     */
    static class CommentSkippingReader {
        public final static int NO_COMMENT  = 0;
        public final static int EOL_COMMENT = 1; // a "//" comment
        public final static int ML_COMMENT  = 2; // /* a "/*" */ comment
        public final static int SAW_STAR    = 3; // /* a "/*" */ comment

        PushbackReader in;
        int inComment;

        CommentSkippingReader(Reader in) {
            this.in = new PushbackReader(in, 1);
            this.inComment = NO_COMMENT;
        }

        private int read() throws IOException {
            int ch = in.read();
            if (ch < 0) {
                throw new EOFException();
            }
            return ch;
        }

        /**
         * Read text into segments of text that are either comments or code.
         * 
         * @param sb StringBuilder that will hold the new text
         * @return true if code, false if comment
         * @throws java.io.EOFException when stream is fully read
         */
        public boolean readText(StringBuffer sb) throws IOException {
            if (!in.ready()) {
                throw new EOFException();
            }
            sb.setLength(0);

            switch (inComment) {
                case EOL_COMMENT: {
                    sb.append("//");
                    break;
                }
                case ML_COMMENT: {
                    sb.append("/*");
                }
            }

            try {
                while (true) {
                    int ch;
                    switch (inComment) {
                        case NO_COMMENT: {
                            ch = read();
                            if (ch == '/') {
                                ch = in.read();
                                switch (ch) {
                                    case '/': {
                                        inComment = EOL_COMMENT;
                                        return true;
                                    }
                                    case '*': {
                                        inComment = ML_COMMENT;
                                        return true;
                                    }
                                    default: {
                                        sb.append('/');
                                        in.unread(ch);
                                        continue;
                                    }
                                }
                            } else {
                                sb.append((char) ch);
                            }
                            break;
                        }
                        case EOL_COMMENT: {
                            do {
                                ch = read();
                                sb.append((char) ch);
                            } while (ch != '\n');
                            inComment = NO_COMMENT;
                            return false; // signal "end-of-comment
                        }
                        case ML_COMMENT: {
                            while (true) {
                                ch = read();
                                sb.append((char) ch);
                                if (inComment == SAW_STAR) {
                                    if (ch == '/') {
                                        inComment = NO_COMMENT;
                                        return false; // signal "end-of-comment
                                    } else if (ch != '*') {
                                        inComment = ML_COMMENT;
                                    }
                                } else if (ch == '*') {
                                    inComment = SAW_STAR;
                                }
                            }
                        }
                        default:
                            throw new IllegalStateException("Whah?");
                    }
                }
            } catch (EOFException e) {
                if (sb.length() == 0) {
                    throw e;
                } else {
                    return (inComment == NO_COMMENT);
                }
            }
        }


        public void close() throws IOException {
            in.close();
        }
    }

    private StringBuffer replaceInterfaceDecl(StringBuffer inbuf) {
        Matcher m = libraryPattern.matcher(inbuf);
        StringBuffer outbuf = new StringBuffer();
        
        while (m.find()) {
            // m.appendReplacement(outbuf, "interface $1 extends Library, $1Import");
            // m.appendReplacement(outbuf, "interface $1 implements Library");
            //m.appendReplacement(outbuf, "interface $1");

            libName = m.group(1);
        }
        replaceString = "$1$2=$3" + libName + JNAGen.GEN_CLASS_SUFFIX + ".$1";
        m.appendTail(outbuf);
        return outbuf;
    }

    private StringBuffer replaceImportDecl(StringBuffer inbuf) {
        StringBuffer outbuf = new StringBuffer();
        Matcher m = constImportPattern.matcher(inbuf);
        while (m.find()) {
            m.appendReplacement(outbuf, replaceString);
        }
        m.appendTail(outbuf);

        inbuf = outbuf;
        outbuf = new StringBuffer();
        m = constDefinedPattern.matcher(inbuf);
        while (m.find()) {
            m.appendReplacement(outbuf, replaceString);
        }
        m.appendTail(outbuf);

        inbuf = outbuf;
        outbuf = new StringBuffer();
        m = constSizeOfPattern.matcher(inbuf);
        while (m.find()) {
            m.appendReplacement(outbuf, replaceString);
        }
        m.appendTail(outbuf);

        return outbuf;
    }
    
    /**
     * Replace text "class stat extends Structure" with 
     * "class stat extends statImpl"
     */ 
    private StringBuffer replaceStructDecl(StringBuffer inbuf) {
        StringBuffer outbuf = new StringBuffer();
        Matcher m = structPattern.matcher(inbuf);
        while (m.find()) {
            //String structName = m.group(1);
            m.appendReplacement(outbuf, "class $1 extends " + libName + JNAGen.GEN_CLASS_SUFFIX + ".$1Impl");
        }
        m.appendTail(outbuf);
        return outbuf;
        //return inbuf;
    }
    
    /**
     * Comment out annotations
     */ 
    private StringBuffer replaceAnnotations(StringBuffer inbuf) {
        StringBuffer outbuf = new StringBuffer();
        Matcher m = annotationPattern.matcher(inbuf);
        while (m.find()) {
            m.appendReplacement(outbuf, "/*$0*/");
        }
        m.appendTail(outbuf);
        return outbuf;
    }
    
    /**
     * Add warning
     */ 
    private StringBuffer insertWarning(StringBuffer inbuf) {
        Matcher m = packagePattern.matcher(inbuf);
        if (m.find()) {
            String warning = "/* **** GENERATED FILE -- DO NOT EDIT ****\n" +
                    " *      This is a CLDC/JNA Interface class definition\n" +
                    " *      generated by com.sun.cldc.jna.JNAGen\n" +
                    " *      from the CLDC/JNA Interface class declaration in " + inputFileName + "\n" +
                    " */\n";
            inbuf.insert(m.start(), warning);
        }
        return inbuf;
    }
    
    /**
     * Read in source from "in", modify source and write to "out".
     */
    public void process(Reader in, FileWriter out) throws IOException {
        StringBuffer inbuf = new StringBuffer(255);
        CommentSkippingReader reader = new CommentSkippingReader(in);
        boolean wroteWarning = false;

        try {
            while (true) {
                boolean code = reader.readText(inbuf);
                StringBuffer tbuf = inbuf;
                if (code) {
                    if (libName == null) {
                        tbuf = replaceInterfaceDecl(inbuf);
                    }
                    if (!wroteWarning) {
                        tbuf = insertWarning(inbuf);
                    }
                    tbuf = replaceImportDecl(tbuf);
                    tbuf = replaceStructDecl(tbuf);
                    if (commentOutAnnotations) {
                        tbuf = replaceAnnotations(tbuf);
                    }
                    out.write(tbuf.toString());
                } else if (keepComments) {
                    // write out comments unchanged
                    out.write(inbuf.toString());
                }
            }
        } catch (EOFException ex) {
            // we were expecting that...
        }

        reader.close();
        out.flush();
        out.close();  
    }
    
    /**
     * Processes a given file.
     *
     * @param inputFile   the file to process
     * @param outputFile  the file to which the output should be written
     */
    public void execute(File inputFile, File outputFile) throws IOException {
        inputFileName = inputFile.getPath();
        FileReader freader = new FileReader(inputFile);
        Build.mkdir(outputFile.getParentFile());
        if (outputFile.isFile() && outputFile.exists()) {
            outputFile.delete();
        }
        FileWriter writer = new FileWriter(outputFile);
        libName = null;
        process(freader, writer);
        outputFile.setReadOnly();
    }
    
    /**
     * Processes a set of files placing the resulting output in a given directory.
     *
     * @param inputFiles  the files to be processed
     * @param destDir     the directory where the processed files are to be written
     */
    public void execute(FileSet inputFiles, File destDir) {
        Iterator iterator = inputFiles.list().iterator();
        while (iterator.hasNext()) {
            File inputFile = (File)iterator.next();
            if (inputFile.length() != 0) {
                File outputFile = inputFiles.replaceBaseDir(inputFile, destDir);
                try {
                    execute(inputFile, outputFile);
                } catch (IOException ex) {
                    System.out.println("Error processing import file " + inputFile);
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FileReader freader = new FileReader(args[0]);
        FileWriter writer = new FileWriter(args[1]);
        
        SourceProcessor sp = new SourceProcessor();
        sp.process(freader, writer);
    }

}
