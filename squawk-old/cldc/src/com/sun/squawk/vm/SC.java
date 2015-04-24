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
 * This class contains the offsets that define that define the layout of the array
 * (of type "[-local-") that implements a stack for a thread.
 */
public interface SC {

    /**
     * The index of the pointer to the next stack chunk in a list of stack chunks.
     * This is only used by a generational collector.
     */
    public final static int next = 0;

    /**
     * The index of the pointer to the VMThread instance that owns this chunk.
     */
    public final static int owner = 1;

    /**
     * The pointer to the inner most activation frame in the stack.
     */
    public final static int lastFP = 2;

    /**
     * The bytecode index of the next instruction to be executed in the inner most
     * activation frame's method.
     */
    public final static int lastBCI = 3;

    /**
     * This is a word that is always unused. If this word is ever non-zero then an
     * overflow of the stack has occurred.
     */
    public final static int guard = 4;

    /**
     * The offset of the stack limit (i.e. the last slot that can be used without overwriting
     * one of the header slots defined above).
     *
     *
     *        :                  :
     *        |      parmN       |  8
     *        +------------------+        +
     *        |     returnIP     |  7     |
     *        +------------------+        |
     *        |     returnFP     |  6     } FIXED_FRAME_SIZE
     *        +------------------+        |
     *  sl -->|        MP        |  5     |
     *        +==================+        +
     *        |      guard       |  4
     *        +------------------+
     *        |      lastBCI     |  3
     *        +------------------+
     *        |      lastFP      |  2
     *        +------------------+
     *        |      owner       |  1
     *        +------------------+
     *        |       next       |  0
     *        +------------------+
     *
     */
    public final static int limit = guard + 1;
}
