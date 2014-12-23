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

import com.sun.squawk.Isolate;
import com.sun.squawk.VM;
import com.sun.squawk.util.Assert;

/**
 *  <b>deprecated</b> <i>Use the higher-level {@link Channel} and {@link ServerChannel} classes instead.</i><p>
 *
 * In the context of Channels, MailboxAddresses are the way a local Channel refers to a remote Channel. Envelopes
 * are tagged with the address of the sender and the receiver. The receiver can use {@link #getReplyAddress}
 * 
 * The MailboxAddress class is likely to become a private imeplementation-only class.<p>
 *
 * A MailboxAddress is a private reference to a remote Mailbox. It is a logical connection between a sender and a Mailbox.<p>
 *
 * MailboxAddress are created by lookupMailbox(), which creates a new MailboxAddress each time called,
 * or by receiving a reference to a MailboxAddress in an envelope.<p>
 *
 * If several isolates want to send messages to the same Mailbox in another isolate, each will use a different
 * MailboxAddress. Even within an isolate, it may be simpler to use different MailboxAddresses to the same Mailbox.<p>
 *
 * Any attempt to call send() on a MailboxAddress to a Mailbox that is closed throws a MailboxClosedException.<p>
 *
 * @see Channel
 * @see ServerChannel
 */
public final class MailboxAddress {
    private final static int UNOWNED = 0;
    private final static int OPEN = 1;
    private final static int CLOSED = 2;
    
    private volatile int state = UNOWNED;
    
    /**
     * Every MailboxAddress is "owned" by the sending (remote) Isolate. This is only
     */
    private Isolate owner = null;
    
    /**
     * The other address in the address pair. This is set during lookup().
     */
    private MailboxAddress otherAddress;
    
    /**
     * A direct reference to the Mailbox. This must be cleraed when an isolate is hibernated.
     */
    private Mailbox mailbox;
    
    /**
     * Create address, but register with VM yet.
     */
    MailboxAddress(Mailbox mailbox) {
        this.mailbox = mailbox;
    }
    
    /**
     * Sends a close message to the corresponding Mailbox, explaining that no more messages will be sent via this
     * MailboxAddress. Also closes otherAddress if it's open, to eagerly break connections between isolates.
     * Redundant closes on a Mailbox are ignored.
     *
     * Do NOT call while synchronized on "this". Can deadlock on otherAddress.close().
     *
     * @todo Rethink how to close otherAddress only semi-agressivly. Reaper thread, or close
     * dead Mailbox addresses before attampting a hibernate.
     */
    public void close() {
        try {
            synchronized (this) {
                if (state != OPEN) {
                    return;
                }
                Assert.that(mailbox != null, "mailbox is null");
                Assert.that(otherAddress != null, "otherAddress is null");
                Assert.that(owner != null, "owner is null");
                
                send(new AddressClosedEnvelope());
                closeLocalState();
            }
            
            if (otherAddress.isOpen()) {
                otherAddress.close();
            }
        } catch (AddressClosedException ex) {
            // send() will have closeLocalState() for us...
        }
    }
    
    /**
     * Clear local state for this address.
     *
     */
    private void closeLocalState() {
        if (state != OPEN) {
            return;
        }
        // break inter-isolate pointers
        owner.forgetMailboxAddress(this);
        
        mailbox = null;
        state = CLOSED;
        owner = null;
    }
    
    /**
     * Returns true if close() was called, or a MailboxClosedException was thrown by send().
     *
     * Note - this is intentionally unsynchronized to avoid deadlock in close(), where we call otherAddress.close().
     */
    public boolean isOpen() {
        return state == OPEN;
    }
    
    /**
     * Returns true if anIsolate is the owner of this address.
     */
    public boolean isOwner(Isolate anIsolate) {
        return owner == anIsolate;
    }
    
    /**
     * This method looks up a MailboxAddress that has been registered with the system,
     * and implicitly opens the connection to the remote Mailbox. It also opens the reply address
     * for the remote mailbox, so it may send responses back to this Isolate.
     *
     * @param name the name of the Mailbox to lookup.
     * @param replyMailbox the address of a local mailbox that the remote isolate can send replies to. 
     * @return an open address to the remote mailbox named <code>name</code>.
     * @throws NoSuchMailboxException if there is no mailbox named <code>name</code>.
     */
    public static MailboxAddress lookupMailbox(String name, Mailbox replyMailbox) throws NoSuchMailboxException {
        Mailbox box = VM.lookupMailbox(name);
        
        if (box == null) {
            throw new NoSuchMailboxException(name);
        }
        
        MailboxAddress replyAddress =  new MailboxAddress(replyMailbox);
        MailboxAddress startingAddress = new MailboxAddress(box);
        MailboxAddress finalAddress = box.callHandleOpen(startingAddress, replyAddress);
        
        if (finalAddress == null) {
            // something fell apart in callHandleOpen
            throw new NoSuchMailboxException(name);
        }

        // record the address of the remote mailbox with this isolate:
        finalAddress.recordAddress(Isolate.currentIsolate(), replyAddress);
        
        // also, tell the isolate containing the remote mailbox about the 
        // reply address that it will use to send replies back to this isolate.
        replyAddress.recordAddress(box.getOwner(), finalAddress);
        
        return finalAddress;
    }
    
    /**
     * Sends a message to the Mailbox. Does not wait for acknowledgment. The MailboxAddress must have been 
     * opened by #lookup, must not have been closed, and caller must by the owning isolate of this MailboxAddress, 
     * or an IllegateStateException will be thrown.
     *
     * @param env the message to send
     * @throws IllegateStateException if the address is not in the open state, or if the caller is not the 
     *         "owner" of this MailboxAddress.
     */
    public void send(Envelope env) throws AddressClosedException {
        if (state == CLOSED) {
            throw new AddressClosedException(this);
        } else if (!mailbox.isOpen()) {
            closeLocalState();
            throw new AddressClosedException(this);
        } else if (state == UNOWNED) {
            throw new IllegalStateException(this + " has not been opened for sending.");
        } else if (Isolate.currentIsolate() != owner && !(env instanceof AddressClosedEnvelope)) {
            throw new IllegalStateException("Attempted send on " + this + " by " + Isolate.currentIsolate());
        }
        
        env.setAddresses(this);
        mailbox.handleMessage(env);
    }
    
    /**
     * This version is used internally by ServerChannel to call send using a temporory mailAddress that is unowned.
     *
     * @param env the message to send
     * @throws IllegateStateException if the address is not in the open state, or if the caller is not the 
     *         "owner" of this MailboxAddress.
     */
    void send0(Envelope env) throws AddressClosedException {
        if (state == CLOSED) {
            throw new AddressClosedException(this);
        } else if (!mailbox.isOpen()) {
            closeLocalState();
            throw new AddressClosedException(this);
        }
        
        env.setAddresses(this);
        mailbox.handleMessage(env);
    }
    
    /**
     * Return the Mailbox referred to by this MailboxAddress. Throws an IllegalStateException if
     * called by an isolate that is not the owner of Mailbox, or if the address is not in the open state.
     *
    package-private Mailbox getMailbox() {
        if (state == CLOSED || mailbox.isClosed()) {
            closeLocal();
            throw new IllegalStateException("Address or Mailbox closed");
        }
        
        if (state == UNOWNED) {
            throw new IllegalStateException("Address unowned");
        }
        
        if (mailbox.getOwner() != Isolate.currentIsolate()) {
            throw new IllegalStateException("Caller does not own the Mailbox");
        }
        
        return mailbox;
    }*/
    
    /**
     * Tell system to track address, in order to handle cleanup after
     * isolate exit, etc. Called as part of lookup. Each isolate keeps track of 
     * the MailboxAddresses that they have that refere to other mailboxes.
     *
     * @param isolate the isolate that will be sending messages to the local mailbox.
     */
    private void recordAddress(Isolate isolate, MailboxAddress otherAddress) {
        if (state != UNOWNED) {
            throw new IllegalStateException("This address has already been recorded");
        } else if (otherAddress == null) {
            throw new IllegalArgumentException();
        }
        this.owner = isolate;
        owner.recordMailboxAddress(this);
        this.otherAddress = otherAddress;
        this.state = OPEN;
    }
    
    /**
     * Get the reply address that was associated with this address during lookup.
     * The returned reply address may already be closed.
     *
     * @return the reply address for this address
     * @throws IllegalStateException if this address is not open
     */
    public MailboxAddress getReplyAddress() {
        if (otherAddress == null) {
            throw new IllegalStateException("This address has no reply address");
        }

        return otherAddress;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        switch (state) {
            case UNOWNED: 
                return "MailboxAddress (UNOWNED) of " + mailbox.toString();
            case OPEN:
                return "MailboxAddress of " + mailbox.toString();
            case CLOSED:
                return "MailboxAddress (CLOSED)";
            default:
                throw Assert.shouldNotReachHere();
        }
    }
    
    /**
     * This package-private class is created as a side-effect of close(),
     * or by the system. As this envelop is handled by the Mailbox, it may create
     * and store an excpetion to be thrown later.
     */
    static class AddressClosedEnvelope extends Envelope {
        public Object getContents() {
            return getToAddress();
        }
    } // AddressClosedEnvelope
    
}
