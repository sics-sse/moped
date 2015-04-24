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

package com.sun.squawk.vm;

/**
 * This class contains the offsets and constants that define the layout of an
 * activation frame for the current method. All the offsets are relative to
 * the current frame pointer, FP.
 */
public interface FP {

    /**
     * The offset of the slot containing the first parameter.
     */
    public final static int parm0 = 3;

    /**
     * The offset of the slot containing the IP of the caller of the current method.
     */
    public final static int returnIP = 2;

    /**
     * The offset of the slot containing the FP of the caller of the current method.
     */
    public final static int returnFP = 1;

    /**
     * The offset of the slot containing the first local variable of the current method.
     */
    public final static int local0 = 0;

    /**
     * The offset of the slot containing the pointer to the current method.
     */
    public final static int method = local0;

    /**
     * This is the number of slots that must be reserved for a call to a method
     * above and beyond the slots it requires for its local variables and operand
     * stack.
     */
    public final static int FIXED_FRAME_SIZE = 3;
}
