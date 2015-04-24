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
import com.sun.squawk.vm2c.ProcessedMethod.*;

/**
 * A visitor that visits all the methods in the call graph of a
 * <code>ProcessedMethod</code>.
 *
 */
public class CallGraphVisitor {

    private static final Object WHITE = null;
    private static final Object GREY = new Object();
    private static final Object BLACK = new Object();
    private final Map<ProcessedMethod, Object> colours;

    /**
     * Creates an object for traversing the call graph of one or more methods.
     *
     * @param idempotentScans  if true, each {@link #scan scan} is idempotent otherwise methods
     *        visited in a previous call graph traversal with this object will not be visited in
     *        successive call graph traversals
     */
    public CallGraphVisitor(boolean idempotentScans) {
        colours = idempotentScans ? null : new HashMap<ProcessedMethod, Object>();
    }

    /**
     * Traverse the call graph of <code>method</code>.
     *
     * @param method   the root of a call graph
     * @param methods  the map from method symbols to their processed form
     * @return the set of methods traversed in this scan if this is an idempotent scanner otherwise
     *         the set of methods traversed in this and all previous scans
     */
    public Set<ProcessedMethod> scan(ProcessedMethod method, Map<MethodSymbol, ProcessedMethod> methods) {
        Map<ProcessedMethod, Object> colours = this.colours;
        if (colours == null) {
            colours = new HashMap<ProcessedMethod, Object>(methods.size());
        }
        scan(method, methods, colours, new Stack<CallSite>());
        return colours.keySet();
    }

    private static boolean isEarlyMethod(ProcessedMethod method) {
        return method.getInliningMode() == ProcessedMethod.MUST_INLINE ||
                method.isMacro || method.hasProxy;
    }

    private void scan(ProcessedMethod caller, Map<MethodSymbol, ProcessedMethod> methods,
            Map<ProcessedMethod, Object> colours, Stack<CallSite> calls) {
        Object colour = colours.get(caller);
        if (colour == WHITE) {
            colours.put(caller, GREY);


            if (caller.error == null) {
                // do inline first:
                for (CallSite call : caller.calls) {
                    ProcessedMethod callee = methods.get(call.callee);
                    if (callee != null && isEarlyMethod(callee)) {
                        calls.push(new CallSite(callee.sym, caller.sym, call.call));
                        scan(callee, methods, colours, calls);
                        calls.pop();
                    }
                }
                
            // Emit callees first
            if (isEarlyMethod(caller)) {
                doVisitMethod(caller, colours, calls);
            }

                for (CallSite call : caller.calls) {
                    ProcessedMethod callee = methods.get(call.callee);
                    if (callee != null && !isEarlyMethod(callee)) {
                        calls.push(new CallSite(callee.sym, caller.sym, call.call));
                        scan(callee, methods, colours, calls);
                        calls.pop();
                    }
                }
            }

            doVisitMethod(caller, colours, calls);
        }
    }

    private void doVisitMethod(ProcessedMethod method, Map<ProcessedMethod, Object> colours, List<CallSite> calls) {
        if (colours.get(method) == GREY) {
            visitMethod(method, Collections.unmodifiableList(calls));
            colours.put(method, BLACK);
        }
    }

    public void visitMethod(ProcessedMethod method, List<CallSite> calls) {
    }
}
