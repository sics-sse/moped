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

package com.sun.squawk.pragma;

/**
 * Allow a method declared to throw this exception to be inlined. This is like ForceInlinedPragma,
 * except that if an AllowInlinedPragma method is too large to inline, it won't be. This is good for 
 * debug code that is conditionally compiled.
 * 
 * For a virtual method, this allows inlining EVEN IF THE RECEIVER MIGHT BE NULL.
 *    For example:
 *       void foo() { return; }
 *       void bar(Foo aFoo) { aFoo.foo(); }
 *
 *   Normal inlining rules prohibit inlining foo() into bar, because it eliminates the implicit null check that invokevirtual does.
 *   But if foo() is declared to throw AllowInlinedPragma, then it may be inlined Method size limits may still prevent the method from being inlined.
 *
 * For a static method, this allows inlining EVEN IF THE CLASS HAS A STATIC INITIALIZER. 
 * Inlining will skip calling the static initializer. Any getstatic, putstatic, or invokestatic bytecodes within the inlined method will still 
 * cause the static initializer to be called if needed.
 *
 * 
 *
 * @see NativePragma
 * @see ForceInlinedPragma
 */
public class AllowInlinedPragma extends PragmaException {
    
    AllowInlinedPragma() {
    }
    
}
