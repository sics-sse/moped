/*
 * Copyright 2004-2008 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.squawk.debugger;

import java.io.*;

/**
 * A <code>ReplyPacket</code> encapsulates a JDWP reply packet.
 */
public final class ReplyPacket extends Packet {

    /**
     * The error code of the reply.
     */
    private int errorCode;

    /**
     * Creates a packet to send a reply to a command packet.
     *
     * @param command   the packet to which this is a reply
     * @param errorCode the error code of the reply
     */
    public ReplyPacket(CommandPacket command, int errorCode) {
        super(command.getID());
        this.errorCode = errorCode;
    }

    /**
     * Creates a packet to encapsulate a received JDWP reply packet.
     *
     * @param id          the identifier in the reply
     * @param dataLength  the length of the data to read from <code>data</code>
     * @param data        the contents of the data field of the packet
     * @param errorCode   the error code
     * @throws IOException
     */
    public ReplyPacket(int id, int dataLength, DataInputStream data, int errorCode) throws IOException {
        super(id, dataLength, data);
        this.errorCode = errorCode;
    }

    public void updateErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * {@inheritDoc}
     */
    protected void writeFields(DataOutputStream dos) throws IOException {
        dos.writeShort(errorCode);
    }

    /**
     * @return  the value of this reply's error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    public int getFlags() {
        return FLAG_REPLY;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("ReplyPacket[id=").
            append(getID()).
            append(",size=").
            append(getSize());
        if (errorCode != JDWP.Error_NONE) {
            buf.append(",error=").append(errorCode);
        }
        buf.append("]");
        if (Log.DEBUG_ENABLED && Log.debug()) {
            appendData(buf);
        }
        return buf.toString();
    }
}
