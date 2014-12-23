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
import com.sun.squawk.util.SimpleLinkedList;

/**
 * <b>deprecated</b> <i>Use the higher-level {@link Channel} and {@link ServerChannel} classes instead.</i><p>
 *
 * The Mailbox class is likely to become a private imeplementation-only class.<p>
 *
 * A Mailbox is used to receive and read messages. Only the "owning" isolate has a reference to
 * a Mailbox. A Mailbox may be registered with the system by name, allowing clients to get a MailboxAddress that refers
 * to this Mailbox. A Mailbox can accept messages from any number of clients, and these messages are queued in the order received.<p>
 *
 * Messages from each client are tagged by the client's private MailboxAddress. A client may use multiple independent MailboxAddress
 * to the same Mailbox. This allows multiple logical connections to be managed by the same message queue.<p>
 *
 * The logical connections are created by the system when the client does a MailboxAdress.lookup(). The connections are
 * closed when the client closes the MailboxAddress, which causes a AddressClosedEnvelope message to be sent to the Mailbox,
 * or when the Mailbox is closed, which invalidates the MailboxAddresses that refer to it.<p>
 *
 * A Mailbox has to acknowledge a new logical connection by providing the MailboxAdress that a client should use for future
 * messages. A server must specify a MailboxHandler to handle opening new logical connections when it registers it's Mailbox by name.
 * A MailboxHandler may create a new MailBox and new Mailbox address, in order to support private Mailboxes for different clients (see {@link ServerChannel}).<p>
 *
 * The MailboxHandler is also responisble for handling MailboxAddress closed events. It may track the event, and cleanup other state, or it may indicate that
 * that an AddressClosedException should be thrown once all previous sent envelopes have been received.
 *
 * @see Channel
 * @see ServerChannel
 */
public final class Mailbox {
    
    private final String name;
    
    private boolean closed = false;
    
    private boolean registered = false;
    
    private MailboxHandler handler;
    
    private Isolate owner;
    
    private SimpleLinkedList inbox;
    
    private static int anonCounter; /* default to zero */
    
    /**
     * Construct a Mailbox owned by the current Isolate.
     */
    private Mailbox(String name, MailboxHandler handler) {
        this(name, handler, Isolate.currentIsolate());
    }
    
    /**
     * Generic constructor.
     *
     * @param name mailbox name (both for anonymous and registered mailboxes)
     * @param handler specify hwo to handle open and close events
     * @param owner the isolate that ownes this mailbox
     */ 
    private Mailbox(String name, MailboxHandler handler, Isolate owner) {
        this.name = name;
        this.handler = handler;
        this.owner = owner;
        this.inbox = new SimpleLinkedList();
        owner.recordMailbox(this);
    }
    
    /**
     * Returns the registered name of the mail box, if registered. Otherwise returns some other String
     * (may or may not be unique).
     *
     * @return the name of this mailbox.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Closes a Mailbox. If registered, unregisters the Mailbox. Any further attempts by clients to send
     * messages to this Mailbox result in MailboxClosedException being thrown. If any threads are waiting to receive
     * messages, they will be woken up and an exception will be thrown.
     *
     * Redundant closes on a Mailbox are ignored.
     */
    public synchronized void close() {        
        if (closed) {
            return;
        }
        
        if (registered) {
            VM.unregisterMailbox(name, this);
        }
        closed = true;
        
        // wake up all threads waiting on receive(). If a thread doesn't have any messages waiting,
        // it will throw a MailboxClosedException.'
        synchronized (inbox) {
            inbox.notifyAll();
        }
        
        owner.forgetMailbox(this);
        
        // THINK: Do we really want to do this?
        inbox.clear();

        owner = null;
        handler = null;
    }
    
    /**
     * Returns true if the mailbox is open.
     */
    public boolean isOpen() {
        return !closed;
    }
    
    /**
     * Creates a Mailbox with the given name and registers it with the system. 
     * 
     * <code>create</code> may immediately begin receiving messages.
     * The <code>handler</code> will be used to initiate and close the logical connection to a client.
     *
     * @param name        the name that this Mailbox can be looked up under.
     * @param handler     the class used to manage clients opening and closing new logical connections to the new Mailbox.
     * @return the new Mailbox.
     * @throws MailboxInUseException if there is already a mailbox registered under the name <code>name</code>.
     */
    public static Mailbox create(String name, MailboxHandler handler) throws MailboxInUseException {
        if (name == null) throw new IllegalArgumentException();
        
        Mailbox box = new Mailbox(name, handler);
        
        if (VM.registerMailbox(name, box)) {
            box.registered = true;
            return box;
        } else {
            box.close();
            throw new MailboxInUseException(name);
        }
    }
    
    /**
     * An AnonymousMailboxHandler is used for anonymous, single-client mailboxes created by Mailbox.create().
     */
    private static class AnonymousMailboxHandler implements MailboxHandler {
        /**
         * This will never be called, becuase the anonymous mailbox will never be found by MailboxAddress.lookup.
         */
        public MailboxAddress handleOpen(Mailbox originalMailbox, MailboxAddress originalAddress, MailboxAddress replyAddress) {
            return originalAddress;
        }
        
        /**
         * Called after a client closes a logical connection.
         *
         * @param address the closed MailboxAddress.
         * @return an AddressClosedException to be thrown by Mailbox.receive().
         */
        public AddressClosedException handleClose(MailboxAddress address) {
            return new AddressClosedException(address);
        }
    } // AnonymousMailboxHandler


    /**
     * This method creates a private, single-client, unregistered Mailbox. If the client closes its connection to this
     * Mailbox, Mailbox.receive() will throw an AddressClosedException exception.
     *
     * @return the new Mailbox.
     */
    public static Mailbox create() {
        return create(Isolate.currentIsolate());
    }
    
    /**
     * This method creates a private, single-client, unregistered Mailbox. If the client closes its connection to this
     * Mailbox, Mailbox.receive() will throw an AddressClosedException exception.
     *
     * @param owner the Isolate that owns this mailbox. A Server-channel-like usage may need to have one isolate create
     *              a mailbox for another isolate.
     * @return the new Mailbox.
     */
    private synchronized static Mailbox create(Isolate owner) {
         // debugging name ...
        int id = anonCounter++;
        String anonName = "anonymous mailbox " + id + " in " + owner.getName();
        
        Mailbox box = new Mailbox(anonName, new AnonymousMailboxHandler(), owner);
         // tell VM internal about it.
        return box;
    }
    
    /**
     * This method creates a private, single-client, unregistered Mailbox, owned by the same Isolate as this isolate.
     * If the client closes its connection to this Mailbox, Mailbox.receive() will throw an AddressClosedException exception.
     *
     * @return the new Mailbox.
     */
    public Mailbox createSubMailbox() {
        return Mailbox.create(owner);
    }
    
    /**
     * Blocks waiting for messages.
     *
     * If a logical connection to this Mailbox is closed, the MailboxHandler's handleClose() method will be called.
     * If that method returns an exception object, it will be thrown in due time by the receive() method. Otherwise receive() will
     * NOT throw an exception if a client closes its connection to the Mailbox.
     *
     * @return an Envelope containing the sent message.
     * @throws AddressClosedException if the MailboxHandler for this mailbox is setup to throw an exception when the client
     *         closes its connection to this mailbox (a DefaultClientHandler, for example).
     * @throws MailboxClosedException if the Mailbox itself is closed.
     */
    public Envelope receive() throws AddressClosedException, MailboxClosedException {
        if (Isolate.currentIsolate() != owner) {
            throw new IllegalStateException("Attempted receive() on " + this + " by " + Isolate.currentIsolate());
        }
        
        Envelope env = null;
        synchronized (inbox) {
            while (env == null) {
                while (inbox.size() == 0) {
                    if (closed) {
                        throw new MailboxClosedException(this);
                    }
                    
                    try {
                        inbox.wait();
                    } catch (InterruptedException e) {
                    }
                }
                env = (Envelope)inbox.removeLast();
                
                // handle closed MailboxAddresses:
                if (env instanceof MailboxAddress.AddressClosedEnvelope) {
                    MailboxAddress replyTo = env.getReplyAddress();
                    AddressClosedException e = handler.handleClose(replyTo);
                    if (e != null) {
                        throw e;
                    } else {
                        env = null; // repeat outer while loop.
                    }
                }
            }
        }
        
        return env;
    }

    /** 
     * [package-private]
     * Called by client as part of sending a message to this isolate. Note that this is called on the remote mailbox,
     * in the context of the sending Isolate.
     */
    void handleMessage(Envelope env) throws AddressClosedException {
        synchronized (env) {
            synchronized (inbox) {
                inbox.addFirst(env.copy());

                // notify any thread (in the receiving isolate) that another
                // message has been deposited in its inbox
                inbox.notifyAll();
            }
        }
    }
    
    /**
     * Returns true if this Mailbox is registered by name with the system.
     *
     * @return true if registered
     */
    public boolean isRegistered() {
        return registered;
    }
    
    /**
     * Part of lookup handshake. Called by client to find end address to use.
     */
    /*package*/ MailboxAddress callHandleOpen(MailboxAddress startingAddress, MailboxAddress replyAddress) {
        return handler.handleOpen(this, startingAddress, replyAddress);
    }
    
    /**
     * Return the isolate that owns this Mailbox.
     *
     * @return the owning isolate.
     */
    /*pakage*/ Isolate getOwner() {
        return owner;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        if (closed) {
            return "Mailbox " + name + " (CLOSED)";
        } else {
            return "Mailbox " + name + " of " + owner;
        }
    }
    
}
