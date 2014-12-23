//if[NEW_IIC_MESSAGES]
/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.io.mailboxes;

import com.sun.squawk.util.Assert;

/**
 * A ByteArrayEnvelope can be used to pass a byte array, or subsection of a byte array
 * through a Channel.<p>
 *
 * The specified section of the byte array will be copied as the envelope is being sent to
 * the remote channel.
 */
public class ByteArrayEnvelope extends Envelope {
    private byte[] contents;
    private int offset;
    private int len;

    /**
     * Create a ByteArrayEnvelope for the subsection of the specified array.
     *
     * @param array the array of bytes to be sent.
     * @param offset offset to the first byte in the array to be sent.
     * @param len the number of bytes to be sent.
     */
    public ByteArrayEnvelope(byte[] array, int offset, int len) {
        Assert.that((offset >= 0) && (len >= 0) && (offset + len <= array.length));
        this.contents = array;
        this.offset = offset;
        this.len = len;
    }
    
    /**
     * Create a ByteArrayEnvelope for the specified array.
     *
     * @param array the array of bytes to be sent.
     */
    public ByteArrayEnvelope(byte[] array) {
        this.contents = array;
        this.offset = 0;
        this.len = array.length;
    }
    
    /**
     * Return the contents of the envelope.
     */
    public Object getContents() {
        checkCallContext();
        return contents;
    }
    
    /**
     * Return the contents of the envelope, which is a byte array.
     */
    public byte[] getData() {
        checkCallContext();
        return contents;
    }
    
    /**
     * Create a copy of this envelope.
     *
     * @return a copy
     */
    Envelope copy() {
        ByteArrayEnvelope theCopy = (ByteArrayEnvelope)super.copy();
        theCopy.contents = new byte[this.len];
        theCopy.offset = 0;
        // theCopy.len = this.len; // shallow copy handled this.
        
        System.arraycopy(this.contents, this.offset, theCopy.contents, 0, this.len);
        return theCopy;
    }
   
}
