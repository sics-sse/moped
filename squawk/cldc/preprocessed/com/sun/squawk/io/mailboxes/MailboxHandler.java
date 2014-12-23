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
 * <b>deprecated</b> <i>Use the higher-level {@link Channel} and {@link ServerChannel} classes instead.</i><p>
 *
 * The MailboxHandler is responsible for creating a logical connection to the client. It may modify the
 * MailboxAddress that the client will use for future messages, and it may create and manage custom state
 * for that logical connection.
 *
 * @see Channel
 * @see ServerChannel
 */
public interface MailboxHandler {

    /**
     * Called when a client looks up a MailboxAddress. A handler can decide create a new Mailbox to handle
     * this logical connection, or simply use the current Mailbox. A handler can record other state to manage
     * this logical connection.
     *
     * @param originalMailbox the original Mailbox connected to.
     * @param originalAddress a new private MailboxAddress created by the system for the client to use
     *                        communicate with Mailbox.
     * @param replyAddress the address to reply to.
     * @return a MailboxAddress that the client should continue to use for the rest of it's communication.
     *         The <code>originalAddress</code> is often returned, but a MailboxHandler can create a new
     *         new private Mailbox and return a MailboxAddress to this new
     */
    MailboxAddress handleOpen(Mailbox originalMailbox, MailboxAddress originalAddress, MailboxAddress replyAddress);

    /**
     * Called after a client closes a logical connection. The handler can clean up after a logical connection,
     * and can control if getting a close should cause any Mailbox.receive() calls on this Mailbox to throw a
     * AddressClosedException. Typically registered "server" mail boxes should not throw an exception, but
     * private mail boxes should.
     *
     * @param address the closed MailboxAddress.
     * @return null, or an exception to be thrown by Mailbox.receive().
     */
    AddressClosedException handleClose(MailboxAddress address);
    
}
