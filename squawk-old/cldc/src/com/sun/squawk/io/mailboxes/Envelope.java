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

import com.sun.squawk.VM;
import com.sun.squawk.Isolate;

/**
 * Abstract class for messages passed through Channels. <p>
 *
 * The system defines several kinds of envelopes, including ObjectEnvelopes and ByteArrayEnvelopes.<p>
 *
 * Note that the conntents of the envelope should only be looked at by the receiver, or
 * inter-isolate pointers could be created. The getContents() method enforces this.
 */
public abstract class Envelope {
    
    private MailboxAddress toAddress;
    
    /**
     * Returns the MailBoxAddress that the envelope was sent to.
     *
     * @return the to address of the sent envelope, or null if the envelope has not been sent.
     */
    final MailboxAddress getToAddress() {
        return toAddress;
    }

    /**
     * Returns the MailBoxAddress to be used for any replies.
     *
     * @return the reply address of the sent envelope, or null if the envelope has not been sent.
     */
    final MailboxAddress getReplyAddress() {
        if (toAddress == null) {
            return null;
        }
        return toAddress.getReplyAddress();
    }

    /**
     * Return the contents of the envelope. This should only be called by the receiver of the 
     * envelope. All implementations should call checkCallContext.
     *
     * @return the contents of the envelope
     * @throws IllegalStateException if called before the envelopesent, or called by the sender.
     */
    public abstract Object getContents();
    
    /**
     * Address this envelope. Called by MailboxAddress.send().
     */
    final void setAddresses(MailboxAddress toAddress) {
        this.toAddress = toAddress;
    }
    
    /**
     * Check that this envelope has been sent, and that the caller's
     * isolate is the receiver of the envelope.
     *
     * This should be called by all implementations of getContents().
     *
     * @throws IllegalStateException if the conditions are not met.
     */
    protected void checkCallContext() throws IllegalStateException {
        MailboxAddress replyAddress = getReplyAddress();
        if (replyAddress == null) {
            throw new IllegalStateException("Envelope has not been sent.");
        } else if (replyAddress.isOpen() && !replyAddress.isOwner(Isolate.currentIsolate())) {
            throw new IllegalStateException("Calling isolate is not the receiver of the envelope.");
        }
    }
    
    /**
     * The copy() method is similar to Object.clone() in j2se, but the original envelope is 
     * "owned" by the sending isolate, and the copy will be "owned" by the receiving isolate.
     * 
     * The default Envelope.copy() method creates a new envelope of the actual type of
     * "this", and performs a shallow copy of the entire envelope. 
     * 
     * Note that all subclasses must be certain that the copy does not contain any direct pointers to objects 
     * from the sender's Isolate
     *
     * The copy() method itself should be careful not to store pointers to either the original or copied
     * object in static variables or any in other data structure. It's not defined which isolate will 
     * execute the copy() method.
     *
     * Called by the system once per Envelope, sometime between when the message is sent by send(),
     * and when it is received by receive().
     *
     * @return a copy of this Envelope
     */
    /* package-private*/ Envelope copy() {
        // note that this does NOT call a contructor - and neither does Object.clone() as far as I can tell.
        Envelope newEnv = (Envelope)VM.shallowCopy(this);
        return newEnv;
    }
    
}
