/*
 * Copyright 2006-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.spot.security.implementation.ecc;



/** This FiniteField class  operates on integer arrays, but interprets
    them as elements of a finite field.
 */
/*
 * Implementation of finite field arithmetics
 * All arithmetic methods have the following conventions:
 *   r ... result
 *   a ... operand 1
 *   b ... operand 2 (optional)
 * The array used for the result can be the same as used for either
 * (or both) of the operands.
 */
public abstract class FiniteField {
    
    protected final FFA ffa;
    protected final int[] p;
    
    protected FiniteField(FFA ffa, int[] p) {
        this.ffa = ffa;
        this.p = p;
    }
    
    public int[] getP() {
        return p;
    }
    
    public FFA getFFA() {
        return ffa;
    }
    
    public int getBitSize() {
        return ffa.getBitSize();
    }
    
    public abstract void add(int[] r, int[] a, int[] b);
    public abstract void subtract(int[] r, int[] a, int[] b);
    public abstract void multiply(int[] r, int[] a, int[] b);
    public abstract void square(int[] r, int[] a);
    public abstract void multiply2(int[] r, int[] a);
    public abstract void divide2(int[] r, int[] a);
    public abstract void negate(int[] r, int[] a);
    public abstract void invert(int[] r, int[] a);
    
    // makes 'a' fit into the field by doing a general reduction
    // 'a' can be any size but will be modified.
    public abstract void trim(int[] r, int[] a);
    
}
