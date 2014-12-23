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

import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.tree.*;
import com.sun.tools.javac.util.List;

/**
 * Scans a method's tree to find all the variable declarations local to a block (i.e.
 * a sequence of statements surrounded by '{' and '}'.
 *
 */
public class LocalVarDeclScanner extends TreeScanner {

    private Scope scope;
    private Map<JCTree, List<VarSymbol>> blockDecls;

    /**
     * Parses a class tree to find the block local declarations.
     *
     * @param clazz JCClassDecl
     * @return  a hashtable mapping the first statement in a block
     *          to the set of variable declared in the block
     */
    public Map<JCTree, List<VarSymbol>> run(JCTree.JCMethodDecl method) {
        blockDecls = new HashMap<JCTree, List<VarSymbol>>();
        scope = new Scope(method.sym);
        visitMethodDef(method);
        return blockDecls;
    }

    public void visitClassDef(JCTree.JCClassDecl tree) {
        // skip inner classes
    }

    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        if (tree.body != null) {
            enter();
            super.visitMethodDef(tree);
            leave(tree.body.stats);
        }
    }

    public void visitBlock(JCTree.JCBlock tree) {
        enter();
        super.visitBlock(tree);
        leave(tree.stats);
    }

    public void visitVarDef(JCTree.JCVariableDecl tree) {
        if (tree.sym.isLocal()) {
            if ((tree.sym.flags() & Flags.PARAMETER) == 0) {
                scope.enter(tree.sym);
            }
        }
    }

    private void enter() {
        scope = scope.dup();
    }

    private void leave(List<? extends JCTree> stats) {
        if (stats != null && !stats.isEmpty() && scope.nelems != 0) {
            List<VarSymbol> symbols = List.nil();
            int nelems = scope.nelems;
            for (Scope.Entry entry = scope.elems; nelems-- != 0; entry = entry.sibling) {
                assert entry.scope == scope;
                assert entry.sym.kind == Kinds.VAR;
                symbols = symbols.prepend((VarSymbol) entry.sym);
            }
            blockDecls.put(stats.head, symbols);
        }
        scope = scope.leave();
    }
}
