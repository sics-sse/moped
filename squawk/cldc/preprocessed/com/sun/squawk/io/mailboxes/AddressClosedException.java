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

import java.io.IOException;

/**
 * This exception is thrown when trying to send to a closed MailboxAddress,
 * or when reading from a Mailbox, and the MailBoxHandler is managing
 * a private address that has closed.
 * 
 */
public class AddressClosedException extends IOException {
    
    private MailboxAddress closedAddress;
    
    /** Creates a new instance of AddressClosedException */
    AddressClosedException(MailboxAddress closedAddress) {
        this.closedAddress = closedAddress;
    }
    
    MailboxAddress getClosedAddress() {
        return closedAddress;
    }
    
}
