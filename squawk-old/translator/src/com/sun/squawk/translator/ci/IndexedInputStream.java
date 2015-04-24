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

package com.sun.squawk.translator.ci;

import java.io.ByteArrayInputStream;

/**
 * This is a subclass of <code>ByteArrayInputStream</code> that enables the
 * current read position to be queried. That is, the index in the underlying
 * array corresponding to the current read position can be queried.
 */
public final class IndexedInputStream extends ByteArrayInputStream {

    /**
     * Creates an <code>IndexedInputStream</code> instance.
     *
     * @param   buf      the input buffer
     * @param   offset   the offset in the buffer of the first byte to read
     * @param   length   the maximum number of bytes to read from the buffer
     */
    IndexedInputStream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    /**
     * Gets the index in the underlying byte array buffer corresponding to the
     * current read position of the stream. Note that the returned value is
     * the index relative to the offset of the first byte read by this stream.
     *
     * @return  the index in the underlying byte array buffer corresponding
     *          to the current read position of the stream. This is the index
     *          relative to value of the <code>offset</code> parameter in the
     *          constructor.
     */
    int getCurrentIndex() {
        return pos - mark;
    }

    /**
     * Gets the underlying byte array.
     *
     * @return the underlying byte array
     */
    byte[] getBuffer() {
        return buf;
    }
}
