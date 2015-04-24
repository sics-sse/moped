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
package com.sun.squawk.vm2c;

import java.io.*;
import java.util.*;

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import com.sun.squawk.vm2c.ProcessedMethod.*;

/**
 * Converts the methods in multiple Java compilation units to C functions.
 *
 */
public class Converter {

    /**
     * Logger for displaying conversion error messages.
     */
    final Log log;
    /**
     * Table of type name substitutions.
     */
    private Map<Name, Name> typeSubs;
    /**
     * Table for substituting a given variable name with another if it clashes with a global variable.
     */
    private Map<Name, Name> localVarNameSubs;
    /**
     * The converted implementers of abstract methods denoted with the 'implementers' annotation.
     */
    private Map<MethodSymbol, List<ProcessedMethod>> abstractMethodImplementers = new HashMap<MethodSymbol, List<ProcessedMethod>>();
    /**
     * Objects for mapping input positions within each compilation unit
     * back to a source file and line number.
     */
    private final Map<JCTree.JCCompilationUnit, LineNumberTable> lineNumberTables = new HashMap<JCTree.JCCompilationUnit, LineNumberTable>();
    /**
     * Set of methods processed by this converter.
     */
    private final Map<MethodSymbol, ProcessedMethod> methods = new HashMap<MethodSymbol, ProcessedMethod>();
    /**
     * The list of methods that are annotated by the "@vm2c root" annotation.
     * These methods and all methods in their call graphs are converted.
     */
    private final SortedSet<MethodSymbol> roots;
    final Context context;
    boolean lineAndFile;
    boolean omitRuntimeChecks;
    final Types types;
    private Name COM_SUN_SQUAWK_ADDRESS;
    private Name COM_SUN_SQUAWK_OFFSET;
    private Name COM_SUN_SQUAWK_UWORD;
    private Name.Table nameTable;

    public Converter(Context context) {
        this.log = Log.instance(context);
        this.context = context;
        this.types = Types.instance(context);
        methodNames = new HashMap<MethodSymbol, String>();
        METHOD_COMPARATOR = new Comparator<MethodSymbol>() {

            public int compare(MethodSymbol o1, MethodSymbol o2) {
                return asString(o1).compareTo(asString(o2));
            }
        };
        roots = new TreeSet<MethodSymbol>(METHOD_COMPARATOR);
    }

    /**
     * Scans all the method declarations in a set of compilation units and records the
     * parse tree associated with each method as well as which methods
     * are the roots for conversion.
     */
    public void parse(Iterable<? extends JCTree.JCCompilationUnit> units, final Set<String> rootClassNames) throws IOException {

        final Map<MethodSymbol, String[]> implementers = new HashMap<MethodSymbol, String[]>();
        TreeScanner scanner = new TreeScanner() {

            private JCTree.JCCompilationUnit unit;
            private ProcessedMethod method;
            private AnnotationParser ap = new AnnotationParser();

            @Override
            public void visitTopLevel(JCTree.JCCompilationUnit tree) {
                assert unit == null;
                unit = tree;
                super.visitTopLevel(tree);
                unit = null;
            }

            @Override
            public void visitMethodDef(JCTree.JCMethodDecl tree) {
                ProcessedMethod outerMethod = method;
                method = new ProcessedMethod(tree.sym, tree, unit);
                Object old = methods.put(tree.sym, method);
                assert old == null;

                try {
                    Map<String, String> annotations = ap.parse(method);
                    if (annotations.containsKey("root")) {
                        String enclClass = tree.sym.enclClass().fullname.toString();
                        for (String s : rootClassNames) {
                            if (enclClass.indexOf(s) != -1) {
                                roots.add(tree.sym);
                            }
                        }
                    }
                    method.hasCode = annotations.containsKey("code");
                    method.isMacro = annotations.containsKey("macro");
                    method.hasProxy = annotations.containsKey("proxy");

                    String impls = annotations.get("implementers");
                    if (impls != null) {
                        if ((method.sym.flags() & Flags.ABSTRACT) == 0) {
                            throw new InconvertibleNodeException(tree, "'implementers' annotation can only be applied to abstract methods");
                        }
                        implementers.put(method.sym, impls.split(" +"));
                    }

                    if (!annotations.containsKey("proxy") && !annotations.containsKey("code") && !annotations.containsKey("macro")) {
                        // skip the methods that have replacement definitions
                        super.visitMethodDef(tree);
                    }
                } catch (InconvertibleNodeException e) {
                    log.rawError(method.tree.pos, e.getMessage());
                } finally {
                    log.useSource(unit.sourcefile);
                    method = outerMethod;
                }
            }

            @Override
            public void visitApply(JCTree.JCMethodInvocation tree) {
                if (method != null) {
                    MethodSymbol callee = (MethodSymbol) getSymbol(tree);
                    CallSite call = new CallSite(callee, method.sym, tree);
                    method.calls.add(call);
                    super.visitApply(tree);
                } else {
                    // must be in a static initializer which is ignored
                }
            }
        };

        for (JCTree.JCCompilationUnit unit : units) {
            // Initialize the type substitution table if it isn't initialized
            if (typeSubs == null) {
                nameTable = unit.packge.name.table;
                typeSubs = initTypeSubs(nameTable);
                localVarNameSubs = initLocalVarNameSubs(nameTable);
            }
            scanner.visitTopLevel(unit);
        }

        Types types = Types.instance(context);
        for (Map.Entry<MethodSymbol, String[]> entry : implementers.entrySet()) {
            MethodSymbol abstractMethod = entry.getKey();
            List<ProcessedMethod> impls = List.nil();
            for (String impl : entry.getValue()) {
                boolean found = false;
                for (ProcessedMethod method : methods.values()) {
                    if (method.sym != abstractMethod && overrides(method.sym, abstractMethod)) {
                        String implClass = method.sym.enclClass().fullname.toString();
                        if (implClass.equals(impl)) {
                            impls = impls.prepend(method);
                            roots.add(method.sym);
                            found = true;
                            break;
                        }

                    }
                }
                if (!found) {
                    ProcessedMethod pm = methods.get(abstractMethod);
                    log.useSource(pm.unit.sourcefile);
                    log.rawError(pm.tree.pos, "could not find implementation of " + abstractMethod + " in " + impl);
                }
            }
            this.abstractMethodImplementers.put(abstractMethod, impls);
        }
    }

    public boolean overrides(MethodSymbol m1, MethodSymbol m2) {
        if (m1.name.equals(m2.name)) {
            if (m1.enclClass().isSubClass(m2.enclClass(), types)) {
                if (m1.type.equals(m2.type)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Emits the file of C declarations after all compilation units have been {@link #parse parsed}.
     * It's only during emitting that error messages are logged to the diagnostic listener.
     */
    public void emit(PrintWriter out) {

        if (roots.isEmpty()) {
            System.err.println("No methods annotated with '@vm2c root' in their javadoc comment were found");
            System.exit(1);
        }

        out.println("/*");
        out.println(" * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.");
        out.println(" * Copyright 2011 Oracle Corporation. All Rights Reserved.");
        out.println(" * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER");
        out.println(" * ");
        out.println(" * This code is free software; you can redistribute it and/or modify");
        out.println(" * it under the terms of the GNU General Public License version 2");
        out.println(" * only, as published by the Free Software Foundation.");
        out.println(" * ");
        out.println(" * This code is distributed in the hope that it will be useful, but");
        out.println(" * WITHOUT ANY WARRANTY; without even the implied warranty of");
        out.println(" * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU");
        out.println(" * General Public License version 2 for more details (a copy is");
        out.println(" * included in the LICENSE file that accompanied this code).");
        out.println(" * ");
        out.println(" * You should have received a copy of the GNU General Public License");
        out.println(" * version 2 along with this work; if not, write to the Free Software");
        out.println(" * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA");
        out.println(" * 02110-1301 USA");
        out.println(" * ");
        out.println(" * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood");
        out.println(" * Shores, CA 94065 or visit www.oracle.com if you need additional");
        out.println(" * information or have any questions.");
        out.println(" */");
        out.println("");
        out.println("/* **** GENERATED FILE -- DO NOT EDIT ****");
        out.println(" *      generated by " + this.getClass().getName());
        out.println(" */");
        out.println("");

        out.println("/* Forward declarations. */");
        emitFunctions(out, true);
        out.println("Address getObjectForCStringLiteral(int key, int klassIndex, const char *cstr);");

        emitBuiltins(out);

        out.println();
        emitFunctions(out, false);

        emitLiteralTables(out);
    }

    /**
     * Not sure why roots.contains is not reliably return all of the root methods, but that's the case...
     * @param method
     * @return true
     */
    boolean isRootMethod(MethodSymbol method) {
        for (MethodSymbol ms: roots) {
            if (ms == method) {
                return true;
            }
        }
        return false;
    }

    /**
     * Emits a C function declaration.
     *
     * @param out     where to emit
     * @param method  the method to emit
     */
    private void emitFunctionDeclaration(PrintWriter out, MethodSymbol method) {
        String retType = asString(method.type.getReturnType());
        out.print("static ");
        out.print(retType);
        for (int i = 15 - retType.length(); i > 0; --i) {
            out.print(' ');
        }
        out.print(' ');
        if (isRootMethod(method)) {
            ProcessedMethod pm = methods.get(method);
            Map<String, String> annotations = new AnnotationParser().parse(pm);
            String cRoot = annotations.get("root");
            if (cRoot != null) {
                out.print(cRoot + "("); // use name from "root" annotation
            } else {
                out.print(asString(method) + "(");
            }
        } else {
            out.print(asString(method) + "(");
        }

        out.print(getReceiverDecl(method, true));
        int params = method.params().size();
        for (VarSymbol param : method.params()) {
            out.print(asString(param.type));
            if (--params != 0) {
                out.print(", ");
            }
        }
        out.println(");");
    }

    /**
     * Emits a C function definition.
     *
     * @param out     where to emit
     * @param method  the method to emit
     * @param calls   the stack trace of methods from a root in a call graph to this method
     */
    private void emitFunctionDefinition(PrintWriter out, ProcessedMethod method, java.util.List<CallSite> calls) {
        LineNumberTable lnt = lnt(method.unit);
        MethodConverter mc = new MethodConverter(method, Converter.this, lnt);
        log.useSource(lnt.file);

        try {
            for (CallSite call : method.calls) {
                MethodSymbol callee = call.callee;
                if (!methods.containsKey(callee)) {
                    PrintWriter err = log.errWriter;
                    err.println("No definition found for " + callee.enclClass().fullname + "." + callee + " called from:");
                    ArrayList<CallSite> callsWithCM = new ArrayList<CallSite>(calls);
                    callsWithCM.add(new CallSite(callee, method.sym, call.call));
                    printStackTrace(err, callsWithCM);
                    err.println();
                }
            }
            String definition = mc.convert();
            out.println();
            if (lineAndFile) {
                out.println("#line " + lnt.getLineNumber(method.tree.pos) + " \"" + lnt.file.toString() + "\"");
            }
            out.println(definition);
        } catch (InconvertibleNodeException e) {
            log.errWriter.println("Callers:");
            printStackTrace(log.errWriter, calls);
            log.rawError(e.node.pos, e.getMessage());
        }
    }

    /**
     * Emits the C functions for the methods present in the call graphs of the roots.
     *
     * @param out        where to emit
     * @param declsOnly  if true, emit function declarations only otherwise emit function definitions
     */
    private void emitFunctions(final PrintWriter out, final boolean declsOnly) {
        final SortedSet<MethodSymbol> decls = declsOnly ? new TreeSet<MethodSymbol>(METHOD_COMPARATOR) : null;
        CallGraphVisitor cgv = new CallGraphVisitor(false) {

            @Override
            public void visitMethod(ProcessedMethod method, java.util.List<CallSite> calls) {
                if (declsOnly) {
                    decls.add(method.sym);
                } else {
                    emitFunctionDefinition(out, method, calls);
                }
            }
        };
        for (MethodSymbol method : roots) {
            ProcessedMethod pmethod = methods.get(method);
            cgv.scan(pmethod, methods);
        }

        if (declsOnly) {
            for (MethodSymbol method : decls) {
                emitFunctionDeclaration(out, method);
            }
        }
    }

    public List<ProcessedMethod> getImplementersOf(MethodSymbol abstractMethod) {
        List<ProcessedMethod> impls = abstractMethodImplementers.get(abstractMethod);
        if (impls == null) {
            impls = List.nil();
        }
        return impls;
    }

    private void printStackTrace(PrintWriter out, java.util.List<CallSite> calls) {
        for (CallSite call : calls) {
            ProcessedMethod caller = methods.get(call.caller);
            LineNumberTable lnt = lnt(caller.unit);
            String file = lnt.file.toString();
            int index = file.lastIndexOf(File.separatorChar);
            if (index != -1) {
                file = file.substring(index + 1);
            }
            out.println("    " + caller.sym.enclClass().className() + '.' + caller.sym.flatName() + '(' + file + ':' + lnt.getLineNumber(call.call.pos) + ')');
        }
    }
    private final Comparator<MethodSymbol> METHOD_COMPARATOR;

    private void emitBuiltins(PrintWriter out) {

        // Int and long division
        out.println();
        out.println("INLINE int div_i(int lhs, int rhs) {");
        out.println("    if (unlikely(rhs == 0)) {");
        out.println("        fatalVMError(\"divide by zero\");");
        out.println("    }");
        out.println("    if (unlikely(lhs == 0x80000000 && rhs == -1)) {");
        out.println("        return lhs;");
        out.println("    }");
        out.println("    return lhs / rhs;");
        out.println("}");

        out.println();
        out.println("INLINE long div_l(jlong lhs, jlong rhs) {");
        out.println("    if (unlikely(rhs == 0)) {");
        out.println("        fatalVMError(\"divide by zero\");");
        out.println("    }");
        out.println("/*if[SQUAWK_64]*/");
        out.println("    if (unlikely(rhs == -1L && lhs == 0x8000000000000000L)) {");
        out.println("        return lhs;");
        out.println("    }");
        out.println("/*end[SQUAWK_64]*/");
        out.println("    return lhs / rhs;");
        out.println("}");

        out.println();
        out.println("INLINE int rem_i(int lhs, int rhs) {");
        out.println("    if (unlikely(rhs == 0)) {");
        out.println("        fatalVMError(\"divide by zero\");");
        out.println("    }");
        out.println("    if (unlikely(lhs == 0x80000000 && rhs == -1)) {");
        out.println("        return 0;");
        out.println("    }");
        out.println("    return lhs % rhs;");
        out.println("}");

        out.println();
        out.println("INLINE long rem_l(jlong lhs, jlong rhs) {");
        out.println("    if (unlikely(rhs == 0)) {");
        out.println("        fatalVMError(\"divide by zero\");");
        out.println("    }");
        out.println("/*if[SQUAWK_64]*/");
        out.println("    if (unlikely(rhs == -1L && lhs == 0x8000000000000000L)) {");
        out.println("        return 0;");
        out.println("    }");
        out.println("/*end[SQUAWK_64]*/");
        out.println("    return lhs % rhs;");
        out.println("}");

        // Array length
        out.println();
        out.println("INLINE UWord Array_length(Address oop) {");
        out.println("    return (getUWord(oop, HDR_length) >> 2);");
        out.println("}");

        // Null pointer check
        if (omitRuntimeChecks) {
            out.println("#define nullPointerCheck(oop) (oop)");
            out.println("#define arrayBoundsCheck(oop, index)");
        } else {
            out.println();
            out.println("INLINE Address nullPointerCheck(Address oop) {");
            out.println("    if (unlikely(oop == null)) {");
            out.println("        fatalVMError(\"null pointer exception\");");
            out.println("    }");
            out.println("    return oop;");
            out.println("}");

            // Array index out of bounds check (includes null pointer check)
            out.println();
            out.println("INLINE void arrayBoundsCheck(Address oop, int index) {");
            out.println("    UWord length;");
            out.println("    nullPointerCheck(oop);");
            out.println("    length = Array_length(oop);");
            out.println("    if (unlikely(((UWord)index) >= length)) {");
            out.println("        fatalVMError(\"array index out of bounds exception\");");
            out.println("    }");
            out.println("}");
        }

        // Array loads
        out.println();
        out.println("int aload_b(Address oop, int index) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    return getByte(oop, index);");
        out.println("}");

        out.println();
        out.println("int aload_s(Address oop, int index) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    return getShort(oop, index);");
        out.println("}");

        out.println();
        out.println("int aload_c(Address oop, int index) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    return getUShort(oop, index);");
        out.println("}");

        out.println();
        out.println("int aload_i(Address oop, int index) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    return getInt(oop, index);");
        out.println("}");

        out.println();
        out.println("jlong aload_l(Address oop, int index) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    return getLong(oop, index);");
        out.println("}");

        out.println();
        out.println("Address aload_o(Address oop, int index) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    return getObject(oop, index);");
        out.println("}");

        // Array stores
        out.println();
        out.println("void astore_b(Address oop, int index, int value) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    setByte(oop, index, value);");
        out.println("}");

        out.println();
        out.println("void astore_s(Address oop, int index, int value) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    setShort(oop, index, value);");
        out.println("}");

        out.println();
        out.println("void astore_i(Address oop, int index, int value) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    setInt(oop, index, value);");
        out.println("}");

        out.println();
        out.println("void astore_l(Address oop, int index, jlong value) {");
        out.println("    arrayBoundsCheck(oop, index);");
        out.println("    setLong(oop, index, value);");
        out.println("}");
    }

    /**
     * Initializes the table used to translate the names of Java classes representing
     * the special primitive types in Squawk to C type names.
     */
    private Map<Name, Name> initTypeSubs(final Name.Table t) {
        Map<Name, Name> m = new HashMap<Name, Name>();
        m.put(COM_SUN_SQUAWK_ADDRESS = asName("com.sun.squawk.Address"), asName("Address"));
        m.put(COM_SUN_SQUAWK_OFFSET = asName("com.sun.squawk.Offset"), asName("Offset"));
        m.put(COM_SUN_SQUAWK_UWORD = asName("com.sun.squawk.UWord"), asName("UWord"));
        return m;
    }

    /**
     * Initializes the table used to rename C local variables whose name
     * corresponds with a global variable defined in vmcore/src/vm/global.h.
     * Can ignore conditionally defined variables such as io_socket, etc.
     */
    private Map<Name, Name> initLocalVarNameSubs(final Name.Table t) {
        Map<Name, Name> m = new HashMap<Name, Name>();
        m.put(asName("memory"), asName("_memory"));
        m.put(asName("memoryEnd"), asName("_memoryEnd"));
        m.put(asName("memorySize"), asName("_memorySize"));
        m.put(asName("iparm"), asName("_iparm"));
        m.put(asName("ip"), asName("_ip"));
        m.put(asName("fp"), asName("_fp"));
        m.put(asName("sp"), asName("_sp"));
        m.put(asName("lastIP"), asName("_lastIP"));
        m.put(asName("lastFP"), asName("_lastFP"));
        m.put(asName("saved_ip"), asName("_saved_ip"));
        m.put(asName("saved_fp"), asName("_saved_fp"));
        m.put(asName("saved_sp"), asName("_saved_sp"));
        m.put(asName("sl"), asName("_sl"));
        m.put(asName("ss"), asName("_ss"));
        m.put(asName("bc"), asName("_bc"));
        m.put(asName("internalLowResult"), asName("_internalLowResult"));
        m.put(asName("Ints"), asName("_Ints"));
        m.put(asName("Addrs"), asName("_Addrs"));
        m.put(asName("Oops"), asName("_Oops"));
        m.put(asName("Buffers"), asName("_Buffers"));
        m.put(asName("BufferCount"), asName("_BufferCount"));
        m.put(asName("cachedClassState"), asName("_cachedClassState"));
        m.put(asName("cachedClass"), asName("_cachedClass"));
        m.put(asName("pendingMonitors"), asName("_pendingMonitors"));
        m.put(asName("pendingMonitorStackPointer"), asName("_pendingMonitorStackPointer"));
        m.put(asName("streams"), asName("_streams"));
        m.put(asName("currentStream"), asName("_currentStream"));
        m.put(asName("STREAM_COUNT"), asName("_STREAM_COUNT"));
        return m;
    }

    private LineNumberTable lnt(JCTree.JCCompilationUnit unit) {
        LineNumberTable lnt = lineNumberTables.get(unit);
        if (lnt == null) {
            try {
                lnt = new LineNumberTable(unit.getSourceFile());
            } catch (IOException e) {
                assert false : e;
            }
            lineNumberTables.put(unit, lnt);
        }
        return lnt;
    }

    public Name asName(String s) {
        return Name.fromString(nameTable, s);
    }

    /**
     * Converts a Java variable type to a C variable type.
     */
    public String asString(Type type) {
        switch (type.tag) {
            case TypeTags.BYTE:
                return "signed char";
            case TypeTags.CHAR:
                return "unsigned short";
            case TypeTags.SHORT:
                return "short";
            case TypeTags.INT:
                return "int";
            case TypeTags.LONG:
                return "jlong";
            case TypeTags.BOOLEAN:
                return "boolean";
            case TypeTags.VOID:
                return "void";
            case TypeTags.DOUBLE:
                return "double";
            case TypeTags.FLOAT:
                return "float";
            default: {
                assert !type.isPrimitive();
                Name name = typeSubs.get(type.tsym.flatName());
                if (name != null) {
                    return name.toString();
                } else {
                    return "Address";
                }
            }
        }
    }

    /**
     * Converts a Java variable type to a unique char.
     */
    public char asChar(Type type) {
        switch (type.tag) {
            case TypeTags.BYTE:
                return 'B';
            case TypeTags.CHAR:
                return 'C';
            case TypeTags.SHORT:
                return 'S';
            case TypeTags.INT:
                return 'I';
            case TypeTags.LONG:
                return 'J';
            case TypeTags.BOOLEAN:
                return 'Z';
            case TypeTags.VOID:
                return 'V';
            case TypeTags.DOUBLE:
                return 'D';
            case TypeTags.FLOAT:
                return 'F';
            default: {
                assert !type.isPrimitive();
                Name name = typeSubs.get(type.tsym.flatName());
                if (name != null) {
                    return name.toString().charAt(0);
                } else {
                    return 'L';
                }
            }
        }
    }

    /**
     * Determines if variables of a given type are references that need to be checked
     * for null when used as a receiver or field access.
     */
    boolean isReferenceType(Type type) {
        assert COM_SUN_SQUAWK_UWORD != null;
        return !type.isPrimitive() &&
                type.tsym.flatName() != COM_SUN_SQUAWK_ADDRESS &&
                type.tsym.flatName() != COM_SUN_SQUAWK_UWORD &&
                type.tsym.flatName() != COM_SUN_SQUAWK_OFFSET;
    }
    private final Map<MethodSymbol, String> methodNames;

    /**
     * Converts a method to a unique C function name.
     */
    public String asString(MethodSymbol method) {
        String s = methodNames.get(method);
        if (s == null) {
            s = "_" + method.name;
            if (!method.params().isEmpty()) {
                s += '_';
                for (List<VarSymbol> l = method.params(); l.nonEmpty(); l = l.tail) {
                    s += asChar(l.head.type);
                }
            }

            String qualifiedClassName = method.enclClass().fullname.toString();
            String unqualifiedClassName = qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.') + 1);

            String functionName = unqualifiedClassName + s;
            if (methodNames.containsValue(functionName)) {
                functionName = qualifiedClassName + s;
                assert !methodNames.containsValue(functionName);
            }
            methodNames.put(method, functionName);
        }
        return s;
    }

    /**
     * Substitutes a given variable name with another if it clashes with a global variable.
     */
    public Name subVarName(Name varName) {
        Name subVarName = this.localVarNameSubs.get(varName);
        return subVarName == null ? varName : subVarName;
    }

    /**
     * Converts the use of a variable to the appropriate C syntax.
     *
     * @param tree   the AST node using the variable
     * @param var    the variable
     * @param lvalue true if the variable is being assigned to, false if it is being read
     * @param object the object owning the variable if it is an instance field, null otherwise
     */
    public String asString(JCTree tree, VarSymbol var, boolean lvalue, String object) {
        String s;
        if (var.isLocal()) {
            s = subVarName(var.name).toString();
        } else if (var.name == nameTable._this || var.name.toString().equals("null") || var.name.toString().equals("true") || var.name.toString().equals("false")) {
            s = var.name.toString();
        } else {
            s = var.enclClass().className().replace('.', '_') + '_' + var.name;
            if (!isGlobalVariable(var)) {
                if (var.isStatic()) {
                    Object constant = var.getConstantValue();
                    if (constant != null) {
                        assert !lvalue;
                        if (s.startsWith("com_sun_squawk_vm_")) {
                            s = s.substring("com_sun_squawk_vm_".length());
                        }

                        // Java identifiers can contain '$', C identifiers can't
                        // so just inline constant fields whose names have a '$'
                        if (s.indexOf('$') != 0) {
                            if (constant instanceof Long) {
                                s = "JLONG_CONSTANT(" + constant + ")";
                            } else if (constant instanceof String) {
                                s = "\"" + constant + "\"";
                            } else {
                                s = constant.toString();
                            }
                        }
                        return s;
                        /*
                        } else if (var.type.tag == TypeTags.NULL) {
                        return "null";
                         */
                    } else {
                        MethodConverter.inconvertible(tree, "access to non-constant static field");
                        throw new RuntimeException("No done");
                    }
                } else {
                    if (lvalue) {
                        s = "set_" + s + '(' + object + ", ";
                    } else {
                        s += '(' + object + ')';
                    }
                }
            }
        }
        return s;
    }

    public String asString(ClassSymbol clazz) {
        Name name = typeSubs.get(clazz.fullname);
        if (name != null) {
            return name.toString();
        } else {
            if (clazz.type.isPrimitive()) {
                return asString(clazz.type);
            } else {
                return clazz.className().replace('.', '_');
            }
        }
    }

    public String getReceiverDecl(MethodSymbol method, boolean typeOnly) {
        if (method.isStatic()) {
            return "";
        }

        StringBuilder buf = new StringBuilder(asString(method.enclClass().type));
        if (!typeOnly) {
            buf.append(" this");
        }
        if (!method.params().isEmpty()) {
            buf.append(", ");
        }
        return buf.toString();
    }

    /**
     * Determines if the static fields of a given class are actually globals in Squawk.
     * That is, does the class implement the <code>com.sun.squawk.pragma.GlobalStaticFields</code>
     * marker interface.
     */
    private boolean hasGlobalStaticFields(ClassSymbol clazz) {
        Boolean b = globalStaticFields.get(clazz);
        if (b == null) {
            b = Boolean.FALSE;
            for (Type iface : ((Type.ClassType) clazz.type).interfaces_field) {
                String ifaceName = iface.tsym.flatName().toString();
                if (ifaceName.equals("com.sun.squawk.pragma.GlobalStaticFields")) {
                    b = Boolean.TRUE;
                    break;
                }
            }
            globalStaticFields.put(clazz, b);
        }
        return b.booleanValue();
    }
    private final Map<ClassSymbol, Boolean> globalStaticFields = new HashMap<ClassSymbol, Boolean>();

    /**
     * Determines if a given variable is a global in Squawk.
     */
    public boolean isGlobalVariable(Symbol var) {
        if (var instanceof VarSymbol && var.isStatic()) {
            if (((VarSymbol) var).getConstantValue() == null) {
                return (hasGlobalStaticFields(var.enclClass()));
            }
        }
        return false;
    }

    /**
     * Gets the symbol corresponding to a tree.
     */
    public static Symbol getSymbol(JCTree tree) {
        switch (tree.getTag()) {
            case JCTree.CLASSDEF:
                return ((JCTree.JCClassDecl) tree).sym;
            case JCTree.METHODDEF:
                return ((JCTree.JCMethodDecl) tree).sym;
            case JCTree.VARDEF:
                return ((JCTree.JCVariableDecl) tree).sym;
            case JCTree.SELECT:
                return ((JCTree.JCFieldAccess) tree).sym;
            case JCTree.IDENT:
                return ((JCTree.JCIdent) tree).sym;
            case JCTree.INDEXED:
                return getSymbol(((JCTree.JCArrayAccess) tree).indexed);
            case JCTree.APPLY:
                return getSymbol(((JCTree.JCMethodInvocation) tree).meth);
            default:
                return null;
        }
    }
    private final Map<ClassSymbol, ArrayList<String>> stringLiterals = new HashMap<ClassSymbol, ArrayList<String>>();
    private final Map<ClassSymbol, Integer> stringLiteralClasses = new HashMap<ClassSymbol, Integer>();

    public String getLiteralKey(ClassSymbol clazz, String literal) {
        int classKey;
        ArrayList<String> literals = stringLiterals.get(clazz);
        if (literals == null) {
            classKey = stringLiteralClasses.size();
            stringLiteralClasses.put(clazz, new Integer(classKey));
            literals = new ArrayList<String>();
            stringLiterals.put(clazz, literals);
        } else {
            classKey = stringLiteralClasses.get(clazz).intValue();
        }

        int literalKey = literals.indexOf(literal);
        if (literalKey == -1) {
            literalKey = literals.size();
            literals.add(literal);
        }

        assert classKey < Short.MAX_VALUE;
        assert literalKey < Short.MAX_VALUE;

        return Integer.toString(classKey << 16 | literalKey);
    }

    private void emitLiteralTables(PrintWriter out) {
        out.println();
        out.println("Address *ALL_LITERALS[" + stringLiteralClasses.size() + "];");
        for (Map.Entry<ClassSymbol, ArrayList<String>> entry : stringLiterals.entrySet()) {
            out.print("Address LITERALS_FOR_" + entry.getKey().fullname.toString().replace('.', '_') + '[' + entry.getValue().size() + "] = {");
            ClassSymbol clazz = entry.getKey();
            ArrayList<String> literals = stringLiterals.get(clazz);
            for (int i = 0; i < (literals.size() - 1); i++) {
                if (i % 10 == 0) {
                    out.print("\n    ");
                }
                out.print("NULL, ");
            }
            out.println("NULL};");
        }

        out.println();
        out.println("Address findCStringInObjects(Address objects, int length, const char *s) {");
        out.println("    int i = 0;");
        out.println("    int slen = strlen(s);");
        out.println("    while (i != length) {");
        out.println("        Address object = aload_o(objects, i);");
        out.println("        if (strncmp(s, (const char *)object, slen) == 0) {");
        out.println("            assume(!GC_inRam_L(object));");
        out.println("            return object;");
        out.println("        }");
        out.println("        ++i;");
        out.println("    }");
        out.println("    return null;");
        out.println("}");


        out.println();
        out.println("Address getObjectForCStringLiteral(int key, int klassIndex, const char *cstr) {");
        out.println("    static boolean initialized = false;");
        out.println("    int classKey = key >> 16 & 0xFFFF;");
        out.println("    int literalKey = key & 0xFFFF;");
        out.println("    Address *literals;");
        out.println("    Address str;");
        out.println();
        out.println("    if (!initialized) {");
        out.println("        initialized = true;");

        for (Map.Entry<ClassSymbol, Integer> entry : stringLiteralClasses.entrySet()) {
            ClassSymbol clazz = entry.getKey();
            int classID = entry.getValue().intValue();
            String className = clazz.fullname.toString().replace('.', '_');
            String var = "LITERALS_FOR_" + className;
            out.println("        ALL_LITERALS[" + classID + "] =  " + var + ";");
        }
        out.println("    }");
        out.println();
        out.println("    literals = ALL_LITERALS[classKey];");
        out.println("    str = literals[literalKey];");
        out.println("    if (str == null) {");
        out.println("        Address bootstrapSuite = com_sun_squawk_ObjectMemory_root(aload_o(com_sun_squawk_GC_readOnlyObjectMemories, 0));");
        out.println("        Address classes = com_sun_squawk_Suite_classes(bootstrapSuite);");
        out.println("        Address klass = aload_o(classes, klassIndex);");
        out.println("        Address objects = com_sun_squawk_Klass_objects(klass);");
        out.println("        int length = Array_length(objects);");
        out.println();
        out.println("        str = findCStringInObjects(objects, length, cstr);");
        out.println("        if (str == null) {");
        out.println("            fatalVMError(\"accessing string literal in conditionally compiled out code\");");
        out.println("        }");
        out.println("        literals[literalKey] = str;");
        out.println("    }");
        out.println("    return str;");
        out.println("}");
    }
}
