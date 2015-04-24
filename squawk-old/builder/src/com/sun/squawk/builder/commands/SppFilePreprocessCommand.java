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

import java.io.*;
import java.util.*;
import com.sun.squawk.builder.*;
import com.sun.squawk.builder.ccompiler.*;

/**
 * This is the command that preprocesses the *.spp files in a given directory.
 *
 */
public class SppFilePreprocessCommand extends Command {

    public SppFilePreprocessCommand(Build env) {
        super(env, "spp");
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        return "preprocesses one or more *.spp files";
    }

    public void usage(String errMsg) {
        PrintStream out = System.err;
        
        if (errMsg != null) {
            out.println(errMsg);
        }

        out.println();
        out.println("usage: spp files...");
        out.println();
    }

    /**
     *
     * @param sppFile File
     * @return File
     * @throws IllegalArgumentException if <code>sppFile</code> does not end with ".spp"
     */
    private static File getFileDerivedFromSppFile(File sppFile) {
        String path = sppFile.getPath();
        if (!path.endsWith(".spp")) {
            throw new IllegalArgumentException("file does not end with \".spp\": " + path);
        }
        path = path.substring(0, path.length() - ".spp".length());
        return new File(path);
    }

    /**
     * Preprocesses a Squawk preprocessor input file (i.e. a file with a ".spp" suffix). The file is processed with
     * the {@link #preprocessSource() Java source preprocessor} and then with the {@link Macro} preprocessor.
     * The file generated after the preprocessing is the input file with the ".spp" suffix removed.
     *
     * @param sppFile         the input file that must end with ".spp"
     * @param generatedFiles  a list to which any generated files will be added
     * @param preprocessor    the Squawk preprocessor
     * @param macroizer       the Squawk macroizer
     * @param macroize        true if the macroizer should "do its stuff" or just convert the leading '$' in
     *                        any identifiers to '_' (which stops a C compiler complaining about invalid identifiers)
     * @throws IllegalArgumentException if <code>sppFile</code> does not end with ".spp"
     */
    public static void preprocess(File sppFile, List<File> generatedFiles, Preprocessor preprocessor, Macroizer macroizer, boolean macroize) {
        File outputFile = getFileDerivedFromSppFile(sppFile);
        File preprocessedFile = new File(sppFile.getPath() + ".preprocessed");

        // Remove generated files
        Build.delete(outputFile);
        Build.delete(preprocessedFile);

        // Run the Java source preprocessor
        boolean save = preprocessor.disableWithComments;
        preprocessor.disableWithComments = false;
        preprocessor.execute(sppFile, preprocessedFile);
        preprocessor.disableWithComments = save;

        // Run the Macro conversion tool
        macroizer.execute(preprocessedFile, outputFile, macroize);

        generatedFiles.add(preprocessedFile);
        generatedFiles.add(outputFile);

        // Make the generated file read-only
        if (!outputFile.setReadOnly()) {
            throw new BuildException("could not make generated file read-only: " + outputFile);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void run(String[] args) {
        if (args.length == 0) {
            throw new CommandException(this, "no files specified");    
        }

        Preprocessor preprocessor = env.getPreprocessor();
        Macroizer macroizer = env.getMacroizer();
        CCompiler ccompiler = env.getCCompiler();

        List<File> generatedFiles = new ArrayList<File>();
        for (String arg: args) {
            File file = new File(arg);
            preprocess(file, generatedFiles, preprocessor, macroizer, ccompiler.options.macroize);
        }

        env.log(env.verbose, "Generated the following files:");
        for (File file: generatedFiles) {
            env.log(env.verbose, "    " + file);
        }

    }
}