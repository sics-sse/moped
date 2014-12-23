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
 * Method buffer data structure. All the fields are UWords.
 */
public interface MessageBuffer {

    /**
     * The buffer ID of the next buffer.
     */
    public final static int next = 0;

    /**
     * The read position in in bytes in the buffer.
     */
    public final static int pos = 1;

    /**
     * The number of bytes written in the buffer.
     */
    public final static int count = 2;

    /**
     * The offset to the start of the data.
     */
    public final static int buf = 3;

    /**
     * The start of the buffer.
     */
    public final static int HEADERSIZE = buf * HDR.BYTES_PER_WORD;

    /**
     * The size in bytes of the buf part of the buffer.
     */
    public final static int BUFFERSIZE = 128 - HEADERSIZE;
}
