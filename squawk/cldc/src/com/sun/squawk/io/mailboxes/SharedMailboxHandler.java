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

/*
 * SharedMailboxHandler is a utility handler for server-type Mailboxes that simply tells the system to use the default MailBoxAddress
 * when a new logical connection is opened, and doesn't do anything when these connections are closed.
 *
 * A more sophisticated use would use the handleOpen callback to associate other connection state with the 
 * Mailbox address used by the client for this connection. It could also use the handleClose callback to clean up the
 * associated state.
 */
public class SharedMailboxHandler implements MailboxHandler {
    
    /** Creates a new instance of SharedMailboxHandler */
    public SharedMailboxHandler() {
    }
    
    /**
     * The system has created an address to the Mailbox for the client to use, and we will pass that on to the client.
     *
     * @param originalAddress a new private MailboxAddress created by the system for the client to use
     *                        communicate with Mailbox.
     * @param replyAddress the address to reply to.
     * @return originalAddress
     */
    public MailboxAddress handleOpen(Mailbox originalMailbox, MailboxAddress originalAddress, MailboxAddress replyAddress) {
        return originalAddress;
    }

    /**
     * Called after a client closes a logical connection. The handler can clean up after a logical connection,
     * and can control if getting a close should cause any Mailbox.receive() calls on this Mailbox to throw a
     * AddressClosedException. Typically registered "server" mail boxes should not throw an exception, but
     * private mail boxes should.
     *
     * @param address the closed MailboxAddress.
     * @return null, or an exception to be thrown by Mailbox.receive().
     */
    public AddressClosedException handleClose(MailboxAddress address) {
        return null;
    }
   
}
