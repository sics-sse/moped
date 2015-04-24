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

import java.io.ByteArrayInputStream;

/**
 * A ByteArrayInputStreamEnvelope can be used to pass a byte array, or subsection of a byte array, as a 
 * ByteArrayInputStream. This is a zero-copy way  (except for the envelope object itself) to send the 
 * contents of a byte array to another isolate.<p>
 *
 * The main drawback of using ByteArrayInputStreamEnvelopes instead of ByteArrayEnvelopes is that
 * if the sender makes any changes to the byte array encapsulated by the ByteArrayInputStreamEnvelope,
 * these changes may be visible to the receiver of the envelope. <p>
 *
 * This can avoided in applications that implement replies or awknowledgements for all sent envelopes by 
 * not re-using the original byte array until the receiver sends some reply or acknowledgment back.
 * 
 */
public class ByteArrayInputStreamEnvelope extends Envelope {
    private ByteArrayInputStream contents;
    
    /**
     * Create a ByteArrayEnvelope for the subsection of the specified array.
     *
     * @param array the array of bytes to be sent.
     * @param offset offset to the first byte in the array to be sent.
     * @param len the number of bytes to be sent.
     */
    public ByteArrayInputStreamEnvelope(byte[] array, int offset, int len) {
        contents = new ByteArrayInputStream(array, offset, len);
    }
    
    /**
     * Create a ByteArrayEnvelope for the specified array.
     *
     * @param array the array of bytes to be sent.
     */
    public ByteArrayInputStreamEnvelope(byte[] array) {
        contents = new ByteArrayInputStream(array);
    }

    /**
     * Return the contents of the envelope.
     */
    public Object getContents() {
        checkCallContext();
        return contents;
    }
    
    /**
     * Return the contents of the envelope, which is a ByteArrayInputStream.
     * Note that this should not be called by the sending isolate.
     */
    public ByteArrayInputStream getData() {
        checkCallContext();
        return contents;
    }
    
    /**
     * Create a copy of this envelope.
     *
     * @return a copy
     */
    Envelope copy() {
        // a shallow copy is fine.
        return super.copy();
    }
   
    
}
