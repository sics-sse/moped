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
//import javax.microedition.io.*;
//import com.sun.squawk.io.*;
//import com.sun.squawk.util.Assert;
//
///**
// * Message Server Connection
// */
//public class ServerProtocol extends ConnectionBase implements StreamConnection, MessageStreamCallback {
//
//    /**
//     * The number of things opened.
//     */
//    private int opens;
//
//    /**
//     * The namespace target for the message.
//     */
//    private String name;
//
//    /**
//     * The input data.
//     */
//    private Message message;
//
//    /**
//     * The output stream.
//     */
//    private MessageOutputStream out;
//
//    /**
//     * The input stream.
//     */
//    private MessageInputStream in;
//
//    /**
//     * Flag to show the message was sent to the client.
//     */
//    private boolean messageSent;
//
//    /**
//     * Resets the instance state.
//     *
//     * @param name     the namespace target for the message
//     * @param message  the input data
//     */
//    void resetInstanceState(String name, Message message) {
//        in = null;
//        out = null;
//        this.name = name;
//        this.message = message;
//        opens = 1;
//    }
//
//    /**
//     * Server open.
//     */
//    public Connection open(String protocol, String name, int mode, boolean timeouts) throws IOException {
//        throw Assert.shouldNotReachHere();
//    }
//
//    /**
//     * openInputStream
//     */
//    public synchronized InputStream openInputStream() throws IOException {
//        Assert.always(in == null);
//        opens++;
//        in = MessageResourceManager.allocateInputStream(this);
//        in.setMessage(message);
//        return in;
//    }
//
//    /**
//     * openOutputStream
//     */
//    public synchronized OutputStream openOutputStream() throws IOException {
//        Assert.always(out == null);
//        opens++;
//        out = MessageResourceManager.allocateOutputStream(this);
//        return out;
//    }
//
//    /**
//     * Closes this connection.
//     */
//    public synchronized void close() throws IOException {
//        if (--opens == 0) {
//            if (out == null) {
//                openOutputStream();
//                out.close();
//            }
//            if (!messageSent) {
//                throw new IOException("Output stream was not closed");
//            }
//            MessageResourceManager.freeServerProtocol(this);
//        }
//    }
//
//    /**
//     * Signals that the input was opened.
//     *
//     * @param in the input stream
//     */
//    public void inputUsed(MessageInputStream in) throws IOException {
//    }
//
//    /**
//     * Signals that the input was closed.
//     *
//     * @param in the input stream
//     */
//    public void inputClosed(MessageInputStream in) throws IOException {
//        close();
//    }
//
//    /**
//     * Flushes output to client.
//     *
//     * @param out the output stream
//     */
//    public void outputFlushed(MessageOutputStream out) throws IOException {
//        Database.sendToClient(name, out.getMessage(), out.getMessageStatus());
//	out.resetMessageState();
//    }
//
//    /**
//     * Signals that the input was closed.
//     *
//     * @param out the output stream
//     */
//    public void outputClosed(MessageOutputStream out) throws IOException {
//        Database.sendToClient(name, out.getMessage(), out.getMessageStatus());
//        messageSent = true;
//        close();
//    }
//}
