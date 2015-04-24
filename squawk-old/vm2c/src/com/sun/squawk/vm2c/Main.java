/*
 * Copyright 2005-2008 Sun Microsystems, Inc. All Rights Reserved.
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
package com.sun.squawk.vm2c;

import java.io.*;
import java.net.URL;
import java.util.*;

import sun.misc.URLClassPath;

import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Todo;
import com.sun.tools.javac.main.CommandLine;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Options;
import javax.tools.JavaFileObject;

/**
 * The command line interface for the tool that converts the call graphs
 * of one or more methods in the Squawk VM code base to C functions.
 *
 */
public class Main {

    static void usage(String errMsg) {
        PrintStream out = System.err;
        if (errMsg != null) {
            out.println(errMsg);
        }
        out.println("usage: vm2c [-options] <root files>");
        out.println("where options include:");
        out.println("    <root files>  treats methods annotated with \"@vm2c root\"");
        out.println("                in any class whose name includes 'name' as a");
        out.println("                substring as the root methods for conversion");
        out.println("    -cp:<path>  class path");
        out.println("    -sp:<path>  source path");
        out.println("    -o:<file>   generate to 'file' (default=stdout)");
        out.println("    -laf        inserts #line directives in output for Java source file positions");
        out.println("    -orc        omits runtime null pointer and array bounds checks");
        out.println("    -h          shows this help message and quit");
        out.println();
    }

    /**
     * The command line entry point. See {@link #usage} for more details.
     */
    public static void main(String... args) throws IOException {

        try {
            // Expands '@' arg files
            args = CommandLine.parse(args);
        } catch (IOException e) {
            System.err.println("IO error while parsing args: " + e.getMessage());
            System.exit(1);
        }

        int argc = 0;
        String classPathArg = null;
        String sourcePathArg = null;
        String outFile = null;
        boolean lineAndFile = false;
        boolean omitRuntimeChecks = false;
        Set<String> rootClassNames = new HashSet<String>();
        while (argc != args.length) {
            String arg = args[argc];
            if (arg.charAt(0) != '-') {
                break;
            } else if (arg.startsWith("-cp:")) {
                classPathArg = arg.substring("-cp:".length());
            } else if (arg.startsWith("-sp:")) {
                sourcePathArg = arg.substring("-sp:".length());
            } else if (arg.startsWith("-orc")) {
                omitRuntimeChecks = true;
            } else if (arg.startsWith("-root:")) {
                rootClassNames.add(arg.substring("-root:".length()));
            } else if (arg.startsWith("-h")) {
                usage(null);
                return;
            } else if (arg.startsWith("-laf")) {
                lineAndFile = true;
            } else if (arg.startsWith("-o:")) {
                outFile = arg.substring("-o:".length());
            } else {
                usage("Unknown option ignored: " + arg);
                System.exit(1);
            }
            argc++;
        }

        // Set up the class path
        if (classPathArg == null) {
            System.err.println("missing -cp option");
            System.exit(1);
        }

        // Set up the source path
        if (sourcePathArg == null) {
            System.err.println("missing -sp option");
            System.exit(1);
        }

        if (argc == args.length) {
            System.err.println("No input files found.");
            System.exit(1);
        }

        if (rootClassNames.isEmpty()) {
            System.err.println("No root classes specified with '-root' option.");
            System.exit(1);
        }

        Context context = new Context();
        Options options = Options.instance(context);
        options.put("-nowarn", "true"); // NOI18N
        options.put("-source", "1.5");
        options.put("-verbose", null);  // dump out paths and classfile loading
//        options.put("-s", "");
        options.put("-printsource", "");
        options.put("-sourcepath", sourcePathArg);
        options.put("-classpath", "");

        JavaCompiler compiler = JavaCompiler.instance(context);
        compiler.keepComments = true;
        Log log = Log.instance(context);

        // Get the root class names
        List<JCTree.JCCompilationUnit> units = List.nil();
        URLClassPath sourcePath = new URLClassPath(URLClassPath.pathToURLs(sourcePathArg));
        while (argc != args.length) {
            String arg = args[argc++];
//            String rootFileName = arg.replace('.', File.separatorChar) + ".java";            
            String rootFileName = arg;
            URL resource = sourcePath.findResource(rootFileName, true);
            if (resource == null) {
                System.err.println("Cannot find file: " + rootFileName);
                System.exit(1);
            }
            InputStream input = resource.openStream();
            try {
                javax.tools.JavaCompiler c = javax.tools.ToolProvider.getSystemJavaCompiler();
                javax.tools.StandardJavaFileManager fileManager = c.getStandardFileManager(null, null, null);
                Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[]{new File(rootFileName)}));
                fileManager.close();

                JCTree.JCCompilationUnit unit = compiler.parse(compilationUnits.iterator().next());
                checkErrorsDuringParsing(log);
                units = units.prepend(unit);
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
        Enter enter = Enter.instance(context);
        List<JCTree.JCCompilationUnit> trees = List.nil();
        for (JCTree.JCCompilationUnit unit : units) {
            trees = trees.prepend(unit);
        }
        enter.main(trees);
        checkErrorsDuringParsing(log);

        Todo todo = Todo.instance(context);
        Attr attr = Attr.instance(context);
        while (todo.nonEmpty()) {
            Env<AttrContext> env = todo.next();
            attr.attribClass(env.tree.pos(), env.enclClass.sym);
            checkErrorsDuringParsing(log);
        }

        Converter converter = new Converter(context);
        converter.lineAndFile = lineAndFile;
        converter.omitRuntimeChecks = omitRuntimeChecks;
        converter.parse(units, rootClassNames);

        StringWriter buf = new StringWriter();
        PrintWriter out = new PrintWriter(buf);        
        converter.emit(out);
        out.close();

        if (Log.instance(context).nerrors == 0) {
            Writer os = (outFile == null ? new OutputStreamWriter(System.out) : new FileWriter(outFile));
            os.write(buf.toString());
            os.close();
        } else {
            System.exit(1);
        }
    }

    protected static void checkErrorsDuringParsing(Log log) {
        if (log.nerrors > 0) {
            throw new RuntimeException("Errors during parsing");
        }
    }
}
