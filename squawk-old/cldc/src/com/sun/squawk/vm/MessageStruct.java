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
 * Method data structure. All the fields are UWords.
 * Keep in sync with msg.c.
 */
public interface MessageStruct {

    /**
     * The buffer ID of the next message struct.
     */
    public final static int next = 0;

    /**
     * The status of the connection and message.
     */
    public final static int status = 1;

    /**
     * The offset to the start of the data.
     */
    public final static int data = 2;

    /**
     * The offset to the start of the key naming this message.
     */
    public final static int key = 3;

    /**
     * The start of the buffer.
     */
    public final static int HEADERSIZE = key * HDR.BYTES_PER_WORD;

    /**
     * The size of the maxumum message key. This must be kept in sync with
     * the definition of a messageStruct (e.g., in vmcore/src/vm/msg.c.
     */
    public final static int MAX_MESSAGE_KEY_SIZE = 128 - HEADERSIZE;
}
