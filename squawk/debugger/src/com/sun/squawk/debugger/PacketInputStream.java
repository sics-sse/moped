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

import com.sun.squawk.debugger.DataType.*;

/**
 * A PacketInputStream is used to read data from the data part of a {@link Packet}.
 *
 */
public class PacketInputStream {
    private final static boolean ENABLE_VERBOSE = false;

    private final DataInputStream dis;

    public PacketInputStream(DataInputStream dis) {
        this.dis = dis;
    }
    
    protected PacketInputStream(PacketInputStream inner) {
        this.dis = inner.dis;
    }
    
    /** 
     * Get the underlying inputstream for sniffing purposes
     */
    public final DataInputStream getInputStream() {
        return dis;
    }

    /**
     * Reads one byte from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @throws IOException if there was an IO error while reading
     */
    public final byte readByte(String s) throws IOException {
        byte value = dis.readByte();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[byte]      " + s + "=" + value);
        return value;
    }

    /**
     * Reads a boolean value from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return     the <code>boolean</code> value read.
     * @throws IOException if there was an IO error while reading
     */
    public final boolean readBoolean(String s) throws IOException {
        boolean value = dis.readBoolean();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[boolean]   " + s + "=" + value);
        return value;
    }

    /**
     * Reads a char value from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return     the next two bytes of this input stream as a Unicode
     *             character.
     * @throws IOException if there was an IO error while reading
     */
    public final char readChar(String s) throws IOException {
        char value = dis.readChar();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[char]      " + s + "=" + value);
        return value;
    }

    /**
     * Reads a short value from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return     the next two bytes of this input stream, interpreted as a
     *             signed 16-bit number.
     * @throws IOException if there was an IO error while reading
     */
    public final short readShort(String s) throws IOException {
        short value = dis.readShort();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[short]     " + s + "=" + value);
        return value;
    }

    /**
     * Reads an int value from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return     the next four bytes of this input stream, interpreted as an
     *             <code>int</code>.
     * @throws IOException if there was an IO error while reading
     */
    public final int readInt(String s) throws IOException {
        int value = dis.readInt();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[int]       " + s + "=" + value);
        return value;
    }

    /**
     * Reads a long value from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return     the next eight bytes of this input stream, interpreted as a
     *             <code>long</code>.
     * @throws IOException if there was an IO error while reading
     */
    public final long readLong(String s) throws IOException {
        long value = dis.readLong();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[long]      " + s + "=" + value);
        return value;
    }

/*if[FLOATS]*/
    /**
     * Reads a float value from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return the read 32 bit float.
     * @throws IOException if there was an IO error while reading
     */
    public final float readFloat(String s) throws IOException {
        int value = dis.readInt();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[float]     " + s + "=" + value);
        return Float.intBitsToFloat(value);
    }

    /**
     * Reads a double value from this stream.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return the 64 bit double read.
     * @throws IOException if there was an IO error while reading
     */
    public final double readDouble(String s) throws IOException {
        long value = dis.readLong();
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[double]    " + s + "=" + value);
        return Double.longBitsToDouble(value);
    }
/*end[FLOATS]*/

    /**
     * Reads from the
     * stream a representation
     * of a Unicode  character string encoded in
     * Java modified UTF-8 format; this string
     * of characters  is then returned as a <code>String</code>.
     * The format of the string in the stream is the same as that for
     * <code>DataInput</code>, but the characters are preceeded by a
     * 4-byte length field.
     *
     * @param  s   prefix to use if this read is logged. A value of null prevents logging altogether.
     * @return     a Unicode string.
     * @throws IOException if there was an IO error while reading
     */
    public final String readString(String s) throws IOException {
        // A String is encoded in a JDWP packet as a UTF-8 encoded array, not zero
        // terminated, preceded by a *four-byte* integer length.
        String value = com.sun.squawk.util.DataInputUTF8Decoder.readUTF(dis, false, false);
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[string]    " + s + "=" + value);
        return value;
    }

    public ObjectID readObjectID(String s) throws IOException {
        ObjectID value = new ObjectID(dis.readInt());
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[object]    " + s + "=" + value);
        return value;
    }

    public TaggedObjectID readTaggedObjectID(String s) throws IOException {
        TaggedObjectID value = new TaggedObjectID(dis.readByte(), dis.readInt());
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[t-object]  " + s + "=" + value);
        return value;
    }

    public ReferenceTypeID readReferenceTypeID(String s) throws IOException {
        ReferenceTypeID value = new ReferenceTypeID(dis.readInt());
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[type]      " + s + "=" + value);
        return value;
    }

    public MethodID readMethodID(String s) throws IOException {
        int encID = dis.readInt();
        MethodID value = new MethodID(encID);
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[method]    " + s + "=" + value);
        return value;
    }

    public FrameID readFrameID(String s) throws IOException {
        FrameID value = new FrameID(readObjectID(null), dis.readInt());
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[frame]     " + s + "=" + value);
        return value;
    }

    public FieldID readFieldID(String s) throws IOException {
        ReferenceTypeID definingClass = readReferenceTypeID("definingClass");
        int encID = dis.readInt();
        FieldID value = new FieldID(encID, definingClass);
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[field]     " + s + "=" + value);
        return value;
    }

    public Location readLocation(String s) throws IOException {
        Location value = new Location(dis.readByte(), readReferenceTypeID(null), readMethodID(null), dis.readLong());
        if (ENABLE_VERBOSE && s != null && Log.verbose()) Log.log("in[location]  " + s + "=" + value);
        return value;
    }

    /**
     * Attempts to skip over <code>n</code> bytes in the stream.
     *
     * @param n  number of bytes to skip
     * @return number of bytes actually skipped
     * @throws IOException if there was an IO error while skipping
     */
    public int skipBytes(int n) throws IOException {
        return dis.skipBytes(n);
    }

    /**
     * Closes this stream and its underlying stream.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        dis.close();
    }
}
