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

import java.io.*;

import com.sun.squawk.util.*;
import com.sun.squawk.vm.*;


/**
 * A tracer for method bodies.
 *
 */
public class MethodBodyTracer extends BytecodeTracer {

    /**
     * An alias for Tracer.getPrintStream().
     */
    private PrintStream ps = Tracer.getPrintStream();

    /**
     * Internal string buffer.
     */
    private StringBuffer out;

    /**
     * The method body being traced.
     */
    private MethodBody body;

    /**
     * The bytecode array.
     */
    private byte[] code;

    /**
     * Record of which bci begin with an empty stack.
     */
    private int[] emptyOffsets;


    /**
     * The method's line number table, or null.
     */
    private int[] lnt;

    /**
     * The method's local variable table, or null.
     */
    private ScopedLocalVariable[] lvt;

    /**
     * The source file for this method.
     */
    private String srcFile;


    /**
     * The source file path for this method.
     */
    private String srcPath;

    /**
     * optional printer to display intermingled source and squawk byte codes
     */
    private ClassSourcePrinter printer;

/*if[TYPEMAP]*/
    /**
     * The type map describing the type of the value (if any) written to memory by each instruction in 'code'.
     */
    private byte[] typeMap;
/*end[TYPEMAP]*/

    /**
     * The offset of the last instuction.
     */
    private int lastPosition = 0;

    /**
     * The current decoding offset.
     */
    private int currentPosition = 0;

    /**
     * Common initialization code.
     */
    private final void init() {
        code = body.getCode();
        emptyOffsets = com.sun.squawk.translator.ci.CodeParser.getEmptyStackOffsets(code);

        Method method = body.getDefiningMethod();
        if (method != null) {
            lnt = method.getLineNumberTable();
            lvt = method.getLocalVariableTable();
            srcFile = method.getDefiningClass().getSourceFileName();
            srcPath = method.getDefiningClass().getSourceFilePath();
        }

/*if[TYPEMAP]*/
        typeMap = body.getTypeMap();
/*end[TYPEMAP]*/
    }

    /**
     * Constuctor.
     *
     * @param body the method body being traced
     * @param printer the optional printer to display source code
     */
    public MethodBodyTracer(MethodBody body, ClassSourcePrinter printer) {
        this.body = body;
        this.printer = printer;
        init();
    }

    /**
     * Constuctor.
     *
     * @param body the method body being traced
     */
    public MethodBodyTracer(MethodBody body) {
        this.body = body;
        this.printer = null;
        init();
    }

    /**
     * Traces the instructions in the method.
     */
    public void traceAll() {
        traceHeader();
        traceBody();
    }

    /**
     * Traces the method header.
     *
     * @param types the type array
     * @param from the start element
     * @param to the end element
     */
    private void traceTypes(Klass[] types, int from, int to) {
        for (int i = from ; i <= to ; i++) {
            ps.print(" "+types[i].getName());
        }
    }

    /**
     * Traces the method header.
     *
     * @param types the type array
     * @param from the start element
     * @param to the end element
     */
    private void traceOops(Klass[] types, int from, int to) {
        for (int i = from ; i <= to ; i++) {
            ps.print(" "+(types[i].isReferenceType() ? "1" : "0"));
        }
    }

    /**
     * Traces the method header.
     */
    public void traceHeader() {

        /*
         * Trace the basic parameters.
         */
        Klass[] types = body.getTypes();
        int parameterCount = body.getParametersCount();
        int localCount = types.length - parameterCount;

        ps.println("Stack    = "+body.getMaxStack());

        if (parameterCount > 0) {
            ps.print("Parms    = {");
            traceTypes(types, 0, parameterCount-1);
            ps.println(" }");
        }

        if (localCount > 0) {
            ps.print("Locals   = {");
            traceTypes(types, parameterCount, types.length-1);
            ps.println(" }");
        }

        /*
         * Trace the oopmap.
         */
        if (localCount > 0) {
            ps.print("Oops     = {");
            traceOops(types, parameterCount, types.length-1);
            ps.println(" }");
        }

        /*
         * Trace the exception table.
         */
        ExceptionHandler[] exceptionTable = body.getExceptionTable();
        if (exceptionTable != null && exceptionTable.length > 0) {
            ps.print("Handlers = { ");
            for (int i = 0 ; i < exceptionTable.length ; i++) {
                if (i > 0) {
                    ps.print("\n             ");
                }
                ExceptionHandler handler = exceptionTable[i];
                ps.print(""+handler.getStart()+","+handler.getEnd()+"->"+handler.getHandler()+" "+handler.getKlass().getName());
            }
            ps.println(" }");
        }

        if (body.getDefiningMethod().isInterpreterInvoked()) {
            ps.println("Interpreter Invoked = TRUE");
        }

        if (printer != null) {
            ps.print("File     = ");
            ps.println(srcPath);
        }

    }

    /**
     * Traces the bytecodes.
     */
    private void traceBody() {
        out = new StringBuffer(1024);
        while (currentPosition < code.length) {
            if (printer != null) {
                int lineNo = getLineNumber(lnt, currentPosition);
                if (lineNo > 0) {
                    out.append("  Line ");
                    out.append(lineNo + ": ");
                    out.append(printer.getLine(lineNo - 1));
                    out.append('\n');
                }
            }
            traceByteCode();
        }
        ps.println(out);
    }

    /**
     * Traces the bytecodes.
     *
     * @param pos the position to stop before
     * @return a string of lines where all but the last line has a '\n' at the end and
     *         where the last line is exactly 30 characters wide.
     */
    public String traceUntil(int pos) {
        out = new StringBuffer(32);
        if (pos == -1) {
            pos = code.length;
        }
        while (currentPosition < pos) {
            traceByteCode();
        }
        if (out.length() > 0) {
            out.deleteCharAt(out.length()-1); // remove the final '\n'
        }
        return out.toString();
    }

    /**
     * Print a string.
     *
     * @param str the string
     */
    protected void print(String str) {
        String s = ""+lastPosition+":";
        while (s.length() < 5) {
            s = s + " ";
        }
/*if[TYPEMAP]*/
        if (typeMap != null) {
            String type = AddressType.getMnemonic((byte)((typeMap[lastPosition] >> AddressType.MUTATION_TYPE_SHIFT) & AddressType.TYPE_MASK));
            s = s + type + " ";
        }
/*end[TYPEMAP]*/

        s = s+" "+str;
        while (s.length() < 30) {
            s = s + " ";
        }
        out.append(s);
        printCommentary(lastPosition);
        out.append('\n');
        lastPosition = currentPosition;
    }

    /**
     * This is subtley different from the Method.GetLineNumber version.
     * That version actually gives the number after the bci.
     */
    static int getLineNumber(int[] lnt, int bci) {
        if (lnt != null) {
            for (int i = 0 ; i < lnt.length ; i++) {
                int entry = lnt[i];
                int addr = entry >>> 16;
                if (addr == bci) {
                    return entry & 0xFFFF;
                }
            }
        }
        return -1;
    }
    /**
     * Print any notes, such as as debug info, etc.
     */
    void printCommentary(int bci) {
        boolean didLineNo = false;

        if (printer == null) {
            int lineNo = getLineNumber(lnt, bci);
            if (lineNo != -1) {
                out.append("// " + srcFile + ":" + lineNo);
                didLineNo = true;
            }
        }

        boolean isStackEmpty = false;
        for (int i = 0; i < emptyOffsets.length; i++) {
            if (bci == emptyOffsets[i]) {
                isStackEmpty = true;
                break;
            }
        }

        if (isStackEmpty) {
            if (didLineNo) {
                out.append(", [_|_]");
            } else {
                out.append("// [_|_]");
            }
        }

    }

    /**
     * Optional method to print the object constant
     *
     * @param index the class's object table index
     * @retuns the object, or null if no object found.
     */
    protected String getObjectDetails(int index) {
        Klass klass = body.getDefiningMethod().getDefiningClass();
        Object obj = (klass != null && klass != klass.OBJECT && klass.getState() == Klass.STATE_CONVERTED) ? klass.getObject(index) : null;

        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * Lookup the appropriate local variable for the given variable index and byte code offset.
     *
     * @param lvt the local variable table or null.
     * @param bci the byte code index
     * @param index the variable index
     */
    static ScopedLocalVariable findLocalVariable(ScopedLocalVariable[] lvt, int bci, int index) {
        if (lvt != null) {
            for (int i = 0; i < lvt.length; i++) {
                ScopedLocalVariable slv  = lvt[i];
                if ((index == slv.slot) &&
                     (bci >= slv.start) &&
                     (bci <  slv.start + slv.length)) {
                    return slv;
                }
            }

        }

        return null;
    }

    /**
     * Optional method to print the name of a local variable
     *
     * @param index the local variable's index (0..n for params, 0..m for local vars)
     * @param param true if the index refers to a parameter
     */
    protected String getVarDetails(int index, boolean param) {
        ScopedLocalVariable slv = findLocalVariable(lvt, currentPosition, (param ? index : (index + body.getParametersCount())));
        if (slv != null) {
           return slv.name;
        } else {
            return super.getVarDetails(index, param);
        }
    }

    /**
     * Get the next signed byte from the method.
     *
     * @return the value
     */
    protected int getByte() {
        return code[currentPosition++];
    }

    /**
     * Get the current bytecode offset.
     *
     * @return the value
     */
    protected int getCurrentPosition() {
        return currentPosition;
    }

}
