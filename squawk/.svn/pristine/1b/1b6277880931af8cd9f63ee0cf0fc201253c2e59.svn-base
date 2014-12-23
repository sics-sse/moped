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

/**
 * A Channel is a private bidirectional message passing mechanism, typically between Isolates. A Channel can {@link #send} or {@link #receive}
 * envelopes, which can contain {@link com.sun.squawk.io.mailboxes.ByteArrayEnvelope bytes arrays}, 
 * {@link com.sun.squawk.io.mailboxes.ByteArrayInputStreamEnvelope bytes array input streams}, or other 
 * {@link com.sun.squawk.io.mailboxes.ObjectEnvelope objects}.<p>
 *
 * Channels are created by {@link #lookup}, which creates a local channel instance, then looks for a registered {@link ServerChannel} by name
 * and asks it to create a remote channel instance to handle communication with the new local channel. When a Channel is {@link #close closed}, 
 * the remote channel is also closed.<p>
 *
 * When an Isolate exits or is hibernated, all of it's channels are closed. An external isolate waiting in <code>receive()</code> will 
 * get an <code>AddressClosedException</code>. Similarly, any threads in the local isolate that are waiting in <code>receive()</code> will 
 * get an <code>AddressClosedException</code>. When an Isolate that was hibernated is unhibernated, any further calls to <code>send()</code> 
 * will throw <code>AddressClosedException</code>, and any calls to <code>receive()</code> will 
 * get an <code>MailboxClosedException</code>.<p>
 *
 * Code that uses inter-isolate communication and supports isolate hibernation must detect AddressClosedExceptions and MailboxClosedExceptions, and re-connect the channels
 * to the appropriate isolates. Note that an isolate may be hibernated, migrated to another isolate, and then unhibernated on another device.
 * The new device may have capabilities than the original device, which might mean that there is now appropriate ServerChannel to reconnect to.
 * Or the new device may have different properties (such as radio addresses) that must be taken into account by higher-level libraries.<p>
 *
 * @see ServerChannel
 * @see com.sun.squawk.Isolate
 */
public final class Channel {
    Mailbox inBox;
    MailboxAddress outBox;
    
    Channel(MailboxAddress outBox, Mailbox inBox) {
        this.outBox = outBox;
        this.inBox = inBox;
    }
    
    /**
     * Create a connection to a remote Channel using the name of registered ServerChannel. The act of creating
     * the local channel causes the ServerChannel to create a corresponding remote Channel.
     *
     * @param serverChannelName the name of a registered ServerChannel
     * @return a Channel to the new Channel created by the ServerChannel to accept the connection
     * @throws com.sun.squawk.io.mailboxes.NoSuchMailboxException if there is no ServerChannel registered with that name
     */
    public static Channel lookup(String serverChannelName) throws NoSuchMailboxException {
        Mailbox inBox = Mailbox.create(); // create anon. mailbox for messages from other isolate.
        MailboxAddress outBox = MailboxAddress.lookupMailbox(serverChannelName, inBox);
        
        return new Channel(outBox, inBox);
    }
    
    /**
     * Sends a message to the remote channel. Does not wait for acknowledgment. The channel must not be closed
     * or an AddressClosedException will be thrown.
     * 
     * @param env the message to send
     * @throws com.sun.squawk.io.mailboxes.AddressClosedException if the channel is closed. 
     */
    public void send(Envelope env) throws AddressClosedException {
        outBox.send(env);
    }
    
    /**
     * Wait for an envelope sent to this channel.
     *
     * Blocks waiting for messages.
     *
     * If the channel was closed before this call, a MailboxClosedException will be thrown.
     * If the channel was closed while waiting for an envelope, an AddressClosedException will be thrown.
     * This can occur both if the local or remote isolate exits while a thread is waiting for an envelope.
     *
     * @return an Envelope containing the sent message.
     * @throws AddressClosedException if the channel is closed while waiting
     * @throws MailboxClosedException if the channel closed when called
     */
    public Envelope receive() throws AddressClosedException, MailboxClosedException {
        return inBox.receive();
    }
    
    /**
     * Closes the Channel at both ends asynchronously.
     */
    public void close() {
        if (inBox.isOpen()) {
            inBox.close();
        }
        if (outBox.isOpen()) {
            outBox.close();
        }
    }

    /**
     * Return true if the channel is open, both from here to the remote channel, and from the remote channel back.
     *
     * @return true if open.
     */
    public boolean isOpen() {
        return inBox.isOpen() && outBox.isOpen();
    }
    
}
