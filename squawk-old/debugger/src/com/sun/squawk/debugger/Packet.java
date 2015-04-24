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

import com.sun.squawk.*;
import com.sun.squawk.util.*;

/**
 * A <code>Packet</code> encapsulates a JDWP command or reply packet.
 *
 */
public abstract class Packet {

    /**
     * The flag bit value indicating a reply packet.
     */
    public final static short FLAG_REPLY = 0x80;

    /**
     * Indicates a packet for which no reply is expected.
     */
    public final static short FLAG_NEEDS_REPLY = 0x40;

    /**
     * A simple monotonic counter used to allocate unique packet IDs. This allows 2^32
     * unique packets which should be more than enough. To avoid confusion in traces,
     * the SDA allocates even values amd SDP allocates odd values.
     */
    private static int nextPacketID = VM.isHosted() ? Integer.MIN_VALUE : Integer.MIN_VALUE + 1;

    /**
     * An identfier that is used to pair this packet with its associated command or reply packet.
     */
    private final int id;

    /**
     * The data in this packet. This will be null if this packet was not
     * initialized from some received data.
     */
    private final byte[] data;

    /**
     * The buffer used to build the data in this packet. This will be null if this packet was
     * initialized from some received data.
     */
    private final ByteArrayOutputStreamWithGetBytes dataBuffer;

    /**
     * Check that inputstream is only returned once.
     */
    private boolean createdInputStream;

    /**
     * Check that oututstream is only returned once.
     */
    private boolean createdOutputStream;

    /**
     * Creates a Packet for a new JDWP request.
     *
     * @param id     the identifier of the new packet
     */
    public Packet(int id) {
        this.id = id;
        dataBuffer = new ByteArrayOutputStreamWithGetBytes();
        data = null;
    }

    /**
     * Creates a Packet to encapsulate a received JDWP packet.
     *
     * @param owner       the JDWPListener that received the packet
     * @param id          the identifier in the packet
     * @param dataLength  the length of the data to read from <code>data</code>
     * @param data        the contents of the data field of the packet
     * @throws IOException
     */
    public Packet(int id, int dataLength, DataInputStream data) throws IOException {
        this.id = id;
        dataBuffer = null;
        this.data = new byte[dataLength];
        data.readFully(this.data);
    }

    /**
     * Gets a DataInputStream to read the data in this packet.
     *
     * @return a DataInputStream to read the data in this packet
     */
    public PacketInputStream getInputStream() {
        Assert.that(!createdInputStream);
        ByteArrayInputStream bais = (data != null ? new ByteArrayInputStream(data) : new ByteArrayInputStream(dataBuffer.getBytes(), 0, dataBuffer.size()));
        createdInputStream = true;
        return new PacketInputStream(new DataInputStream(bais));
    }

    /**
     * Gets a PacketOutputStream to write some data to this packet. This cannot be called on
     * a packet that was initialized with some received data.
     *
     * @return a PacketOutputStream to write the data in this packet
     */
    public PacketOutputStream getOutputStream() {
        Assert.that(!createdOutputStream);
        Assert.that(data == null, "cannot update the data in an incoming packet");
        createdOutputStream = true;
        return new PacketOutputStream(new DataOutputStream(dataBuffer));
    }

    /**
     * Gets the ID that is used to pair this packet with its associated command or reply packet (if any).
     *
     * @return  the ID that is unique to this command and reply packet pair this packet belongs to (if any)
     */
    public final int getID() {
        return id;
    }

    /**
     * Allocates a unique packet identifier.
     *
     * @return a unique packet identifier
     */
    static synchronized int allocateUniqueID() {
        return nextPacketID += 2;
    }

    /**
     * {@inheritDoc}
     */
    public abstract String toString();


    /**
     * Appends a textualized form of this packet's data to a given StringBuffer.
     *
     * @param buf  the buffer to extend
     */
    protected void appendData(StringBuffer buf) {
/*if[DEBUG_CODE_ENABLED]*/
        int dataLength;
        byte[] data;
        if (dataBuffer != null) {
            dataLength = dataBuffer.size();
            data = dataBuffer.getBytes();
        } else {
            dataLength = this.data.length;
            data = this.data;
        }

        buf.append("{data[").append(dataLength).append("]:");
        for (int i = 0; i < dataLength; i++) {
            buf.append(Integer.toHexString( (int) data[i] & 0xFF)).append(' ');
        }
        buf.append('}');
/*end[DEBUG_CODE_ENABLED]*/
    }


    /**
     * Gets the value for the 'flags' field in the header of this packet.
     */
    public abstract int getFlags();

    /**
     * Hook for sublasses to write their specific fields to a packet being sent.
     *
     * @param dos  the stream to write the fields to
     * @throws IOException if an IO error occurs
     */
    protected abstract void writeFields(DataOutputStream dos) throws IOException;

    /**
     * Gets the size (in bytes) of the entire packet. This value may change if there
     * is currently an output stream adding data to the packet.
     *
     * @return the size (in bytes) of the packets (include the header fields)
     */
    public final int getSize() {
        return 11 + (dataBuffer != null ? dataBuffer.size() : data.length);
    }

    /**
     * Writes this packet to a given output stream. No further data can be added to this
     * packet once this operation is completed.
     *
     * @param dos   where to write the packet
     * @throws IOException if an IO error occurs
     */
    public final void write(DataOutputStream dos) throws IOException {
        int dataLength;
        byte[] data;
        if (dataBuffer != null) {
            dataBuffer.close();
            dataLength = dataBuffer.size();
            data = dataBuffer.getBytes();
        } else {
            dataLength = this.data.length;
            data = this.data;
        }

        int length = dataLength + 11;
        int flags = getFlags();

        // write common header:
        dos.writeInt(length);
        dos.writeInt(id);
        dos.write(flags);
                if (Log.verbose()) {
                    Log.log("Packet.write: length: " + length + " id: " + id + " flags: " + flags);
                }

        // write command bytes or error code:
        writeFields(dos);

        // write variable data:
        dos.write(data, 0, dataLength);
        dos.flush();
    }

}
