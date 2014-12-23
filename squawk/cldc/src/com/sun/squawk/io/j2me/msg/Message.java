//if[OLD_IIC_MESSAGES]
///*
// * Copyright 1999-2008 Sun Microsystems, Inc. All Rights Reserved.
// * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
// * 
// * This code is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License version 2
// * only, as published by the Free Software Foundation.
// * 
// * This code is distributed in the hope that it will be useful, but
// * WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * General Public License version 2 for more details (a copy is
// * included in the LICENSE file that accompanied this code).
// * 
// * You should have received a copy of the GNU General Public License
// * version 2 along with this work; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// * 02110-1301 USA
// * 
// * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
// * Park, CA 94025 or visit www.sun.com if you need additional
// * information or have any questions.
// */
//
//package com.sun.squawk.io.j2me.msg;
//
//import java.io.*;
//
//import com.sun.squawk.*;
//import com.sun.squawk.UWord;
//import com.sun.squawk.Unsafe;
//import com.sun.squawk.vm.*;
//
///**
// * Message Class
// */
//public class Message {
//
//    /**
//     * The first message buffer in the chain.
//     */
//    private Address first;
//
//    /**
//     * The last message buffer in the chain.
//     */
//    private Address last;
//
//    /**
//     * Resets the instance state.
//     */
//    void resetInstanceState() {
//        try {
//            first = last = Database.allocateBuffer();
//        } catch (IOException e) {
//            first = last = Address.zero();
//        }
//    }
//
//    /**
//     * Free all the buffers.
//     */
//    void freeAll() {
//        while (first.ne(Address.zero())) {
//            Address next = getNext(first);
//            Database.freeBuffer(first);
//            first = next;
//        }
//        last = Address.zero();
//    }
//
//    /**
//     * Writes the specified byte to this message.
//     *
//     * @param b the byte to be written.
//     */
//    public void write(int b) {
//        int count = getCount(last);
//        if (count < MessageBuffer.BUFFERSIZE) {
//            setByte(last, count++, b);
//            setCount(last, count);
//        } else {
//            Address next;
//            try {
//                next = Database.allocateBuffer();
//            } catch (IOException e) {
//                next = Address.zero();
//            }
//            setNext(last, next);
//            last = next;
//            write(b);
//        }
//    }
//
//    /**
//     * Writes <code>len</code> bytes from the specified byte array
//     * starting at offset <code>off</code> to this message.
//     *
//     * @param   b     the data.
//     * @param   off   the start offset in the data.
//     * @param   len   the number of bytes to write.
//     */
//    public synchronized void write(byte b[], int off, int len) {
//        Arrays.boundsCheck(b.length, off, len);
//        if (len == 0) {
//            return;
//        }
//        // TEMP implementation.
//        for (int i = 0; i < len; i++) {
//            write(b[off + i]);
//        }
//    }
//
//    /**
//     * Reads the next byte of data from this input message.
//     *
//     * @return  the next byte of data, or <code>-1</code> if the end of the
//     *          stream has been reached.
//     */
//    public int read() throws IOException {
//        if (first.eq(Address.zero())) {
//            return -1;
//        }
//        int pos = getPos(first);
//        int count = getCount(first);
//        if (pos < count) {
//            int res = getByte(first, pos++);
//            setPos(first, pos);
//            return res;
//        }
//        Address next = getNext(first);
//        Database.freeBuffer(first);
//        first = next;
//        if (first.eq(Address.zero())) {
//            last = first;
//            return -1;
//        }
//        return read();
//    }
//
//    /**
//     * Reads up to <code>len</code> bytes of data into an array of bytes
//     * from this Message.
//     *
//     * @param   b     the buffer into which the data is read.
//     * @param   off   the start offset of the data.
//     * @param   len   the maximum number of bytes read.
//     * @return  the total number of bytes read into the buffer, or
//     *          <code>-1</code> if there is no more data because the end of
//     *          the stream has been reached.
//     */
//    public int read(byte b[], int off, int len) throws IOException {
//        Arrays.boundsCheck(b.length, off, len);
//        // TEMP implementation.
//        for (int i = 0; i < len; i++) {
//            int ch = read();
//            if (ch == -1) {
//                return i;
//            }
//            b[off + i] = (byte) ch;
//        }
//        return len;
//    }
//
//    /**
//     * Returns the number of bytes that can be read from this message.
//     *
//     * @return  the number of bytes that can be read
//     */
//    public int available() {
//        int res = 0;
//        Address buf = first;
//        while (buf.ne(Address.zero())) {
//            res += (getCount(buf) - getPos(buf));
//            buf = getNext(buf);
//        }
//        return res;
//    }
//
//    /**
//     * Returns the list of buffers.
//     *
//     * @return the the list of buffers.
//     */
//    Address getData() {
//        Address res = first;
//        first = last = Address.zero();
//        return res;
//    }
//
//    /**
//     * Sets the list of buffers.
//     *
//     * @param buffers the the list of buffers.
//     */
//    void setData(Address buffers) {
//        first = last = buffers;
//        Address next = last;
//        while (next.ne(Address.zero())) {
//            last = next;
//            next = getNext(last);
//        }
//    }
//
//    /**
//     * Returns the next buffer in a list of buffers.
//     *
//     * @param buffer the buffer
//     * @return the next buffer
//     */
//    private static Address getNext(Address buffer) {
//        return Unsafe.getAddress(buffer, MessageBuffer.next);
//    }
//
//    /**
//     * Sets the next buffer in a list of buffers.
//     *
//     * @param buffer the buffer
//     * @param next the next buffer
//     */
//    private static void setNext(Address buffer, Address next) {
//        Unsafe.setAddress(buffer, MessageBuffer.next, next);
//    }
//
//    /**
//     * Returns the count field of a buffer.
//     *
//     * @param buffer the buffer
//     * @return the count field
//     */
//    private static int getCount(Address buffer) {
//        return Unsafe.getUWord(buffer, MessageBuffer.count).toInt();
//    }
//
//    /**
//     * Sets the count field of a buffer.
//     *
//     * @param buffer the buffer
//     * @param count the value to set
//     */
//    private static void setCount(Address buffer, int count) {
//        Unsafe.setUWord(buffer, MessageBuffer.count, UWord.fromPrimitive(count));
//    }
//
//    /**
//     * Returns the pos field of a buffer.
//     *
//     * @param buffer the buffer
//     * @return the pos field
//     */
//    private static int getPos(Address buffer) {
//        return Unsafe.getUWord(buffer, MessageBuffer.pos).toInt();
//    }
//
//    /**
//     * Sets the pos field of a buffer.
//     *
//     * @param buffer the buffer
//     * @param pos the value to set
//     */
//    private static void setPos(Address buffer, int pos) {
//        Unsafe.setUWord(buffer, MessageBuffer.pos, UWord.fromPrimitive(pos));
//    }
//
//    /**
//     * Returns a byte in a message.
//     *
//     * @param buffer the buffer
//     * @param offset the offset
//     * @return the byte
//     */
//    private static int getByte(Address buffer, int offset) {
//        return Unsafe.getByte(buffer, MessageBuffer.HEADERSIZE + offset) & 0xFF;
//    }
//
//    /**
//     * Sets a byte in a message.
//     *
//     * @param buffer the buffer
//     * @param offset the offset
//     * @param value the value to set
//     */
//    private static void setByte(Address buffer, int offset, int value) {
//        Unsafe.setByte(buffer, MessageBuffer.HEADERSIZE + offset, value);
//    }
//}
