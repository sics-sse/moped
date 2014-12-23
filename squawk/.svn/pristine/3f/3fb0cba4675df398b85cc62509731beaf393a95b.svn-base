/*
 * Copyright 2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.util;

import java.io.ByteArrayInputStream;

/**
 * An extention of {@link java.io.ByteArrayInputStream} that allows the byte array buffer to be modified, using the 
 * {@link ByteArrayInputStreamWithSetBytes#setBytes} method.
 *
 * @see  java.io.ByteArrayInputStream
 */
public class ByteArrayInputStreamWithSetBytes extends ByteArrayInputStream {

    protected int offset;
    
    public ByteArrayInputStreamWithSetBytes(byte[] bytes) {
        super(bytes);
    }

    public ByteArrayInputStreamWithSetBytes(byte[] bytes, int offset, int length) {
        super(bytes, offset, length);
    }
    
    public int getPos() {
      return pos - offset;  
    }

    public void setBytes(byte[] bytes, int offset, int length) {
        this.buf = bytes;
        this.count = Math.min(offset + length, buf.length);
        this.offset = offset;
        this.pos = offset;
        this.mark = 0;
    }
    
}
