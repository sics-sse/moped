//if[OLD_IIC_MESSAGES]
///*
// * Copyright 1994-2008 Sun Microsystems, Inc. All Rights Reserved.
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
///**
// * A <code>MessageInputStream</code> contains
// * an internal buffer that contains bytes that
// * may be read from the stream. An internal
// * counter keeps track of the next byte to
// * be supplied by the <code>read</code> method.
// *
// */
//class MessageInputStream extends InputStream {
//
//    /**
//     * The input message to be read.
//     */
//    private Message message;
//
//    /**
//     * Parent object to call back to when various events occur.
//     */
//    private MessageStreamCallback parent;
//
//    /**
//     * Flag to show the stream has been used.
//     */
//    private boolean used;
//
//    /**
//     * Flag to show the stream has been disabled.
//     */
//    private boolean disabled = false;
//
//    /**
//     * Resets the instance state (except parent).
//     *
//     */
//    void resetInstanceState() {
//        message = null;
//        used = false;
//        disabled = false;
//    }
//
//    /**
//     * Resets the instance state.
//     *
//     * @param parent the parent object.
//     */
//    void resetInstanceState(MessageStreamCallback parent) {
//        this.parent = parent;
//        resetInstanceState();
//    }
//
//    /**
//     * Constructor.
//     */
//    MessageInputStream() {
//    }
//
//    /**
//     * Set up the input message.
//     *
//     * @param message the data
//     */
//    void setMessage(Message message) {
//        this.message = message;
//    }
//
//    /**
//     * Disable the stream.
//     */
//    void disable() {
//        disabled = true;
//    }
//
//    /**
//     * Disable the stream.
//     */
//    private void checkEnabled() throws IOException {
//        if (disabled) {
//            throw new IOException("Accessing disabled stream");
//        }
//        if (!used) {
//            // Loads in current message
//            used = true;
//            parent.inputUsed(this);
//        }
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public int read() throws IOException {
//        checkEnabled();
//        return message.read();
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public int read(byte b[], int off, int len) throws IOException {
//        checkEnabled();
//        return message.read(b, off, len);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public int available() throws IOException {
//        checkEnabled();
//        if (message == null) {
//            return 0;
//        }
//        return message.available();
//
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public void close() throws IOException {
//        if (message != null) {
//            parent.inputClosed(this);
//            message.freeAll();
//            MessageResourceManager.freeMessage(message);
//            MessageResourceManager.freeInputStream(this);
//            disable();
//        }
//    }
//
//    /**
//     * Resets this input stream so that subsequent reads will be
//     * against a different message packet.
//     * <p>
//     */
//    public void reset() throws IOException {
//        if (message != null) {
//            message.freeAll();
//            MessageResourceManager.freeMessage(message);
//            resetInstanceState();
//        }
//    }
//
//}
//
