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

import java.util.*;

import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.*;

/**
 * A method that has been processed by a <code>CompilationUnitConverter</code>.
 */
public class ProcessedMethod {

    /**
     * Describes a call site.
     */
    public static class CallSite implements Comparable {

        final MethodSymbol callee;
        final MethodSymbol caller;
        final JCTree.JCMethodInvocation call;

        public CallSite(MethodSymbol callee, MethodSymbol caller, JCTree.JCMethodInvocation call) {
            assert callee != null;
            assert caller != null;
            assert call != null;
            this.callee = callee;
            this.caller = caller;
            this.call = call;
        }

        /**
         * Orders call sites within a method.
         */
        public int compareTo(Object o) {
            if (o instanceof CallSite) {
                CallSite cs = (CallSite) o;
                return call.pos - cs.call.pos;
            }
            return -1;
        }

        public boolean equals(Object o) {
            if (o instanceof CallSite) {
                CallSite cs = (CallSite) o;
                return caller == cs.caller && callee == cs.callee && call == cs.call;
            }
            return false;
        }

        public int hashCode() {
            return caller.hashCode() + call.pos;
        }
    }
    /**
     * The method's symbol.
     */
    final MethodSymbol sym;
    /**
     * The AST for this method.
     */
    final JCTree.JCMethodDecl tree;
    /**
     * The compilation unit in which this method was defined.
     */
    final JCTree.JCCompilationUnit unit;
    InconvertibleNodeException error;
    /**
     * The methods that this method (directly) calls.
     */
    final Set<CallSite> calls = new TreeSet<CallSite>();

    public final static int NEVER_INLINE = -1;
    public final static int MAY_INLINE = 0;
    public final static int MUST_INLINE = 1;

    final int shouldInline;

    public boolean isMacro;
    public boolean hasCode;
    public boolean hasProxy;

    public ProcessedMethod(MethodSymbol method, JCTree.JCMethodDecl tree, JCTree.JCCompilationUnit unit) {
        this.sym = method;
        this.tree = tree;
        this.unit = unit;
        List<JCTree.JCExpression> thrown = tree.thrown;
        int _shouldInline = MAY_INLINE;

        if (thrown != null) {
            for (JCTree.JCExpression thrownExc : thrown) {
                if ((thrownExc instanceof JCTree.JCIdent)) {
                    JCTree.JCIdent t = (JCTree.JCIdent) thrownExc;
                    String name = t.name.toString();
                    if (name.equals("ForceInlinedPragma")
                            || name.equals("AllowInlinedPragma")
                            || name.equals("NativePragma")) {
                        _shouldInline = MUST_INLINE;
//                        if (name.equals("AllowInlinedPragma")) {
//                            System.out.println("Auto inlining " + this);
//                        }
                        break;
                    } else if (name.equals("NotInlinedPragma")) {
                        _shouldInline = NEVER_INLINE;
                        break;
                    }
                }
            }
        }
        this.shouldInline = _shouldInline;
    }

    public final boolean equals(Object o) {
        return o instanceof ProcessedMethod && ((ProcessedMethod) o).sym == sym;
    }

    public final int hashCode() {
        return sym.hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(sym.enclClass().fullname + "." + sym);
        switch (shouldInline) {
            case MUST_INLINE:
                sb.append(" INLINE");
                break;
            case NEVER_INLINE:
                sb.append(" NOINLINE");
                break;
        }
        if (isMacro) {
            sb.append(" MACRO");
        } else if (hasProxy) {
            sb.append(" PROXY");
        } else if (hasCode) {
            sb.append(" CODE");
        }
        
        return sb.toString();
    }

    public int getInliningMode() {
        return shouldInline;
    }

}
