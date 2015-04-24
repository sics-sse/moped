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
//import com.sun.squawk.VM;
//import com.sun.squawk.util.*;
//import com.sun.squawk.vm.*;
//
///**
// * The message database interface.
// */
//public class Database {
//
//    /**
//     * The size of the maxumum key.
//     */
//    private static int MAX_MESSAGE_KEY_SIZE = MessageStruct.MAX_MESSAGE_KEY_SIZE;
//
//    /**
//     * Allocates a message buffer.
//     *
//     * @return the addrss of the buffer or null is none was available
//     */
//    static Address allocateBuffer() throws IOException {
//	Address res = VM.execMessageIO(ChannelConstants.INTERNAL_ALLOCATE_MESSAGE_BUFFER, null, null, 0);
//        Assert.that(Unsafe.getUWord(res, MessageBuffer.pos).eq(UWord.zero()));
//        Assert.that(Unsafe.getUWord(res, MessageBuffer.next).eq(UWord.zero()));
//        Assert.that(Unsafe.getUWord(res, MessageBuffer.count).eq(UWord.zero()));
//        return res;
//    }
//
//    /**
//     * Frees a message buffer.
//     *
//     * @param buffer the buffer
//     */
//    static void freeBuffer(Address buffer) {
//	try {
//             VM.execMessageIO(ChannelConstants.INTERNAL_FREE_MESSAGE_BUFFER, null, buffer, 0);
//	} catch (IOException e) {
//	     /* discard -- should not happen */
//	}
//    }
//
//    /**
//     * Send a message.
//     *
//     * @param op the opcode
//     * @param key the message key
//     * @param message the message to be sent
//     */
//    private static void send(int op, String key, Message message, int status) throws IOException {
//        Assert.always(key.length() <= MAX_MESSAGE_KEY_SIZE, "Message key must be less than "+MAX_MESSAGE_KEY_SIZE+1);
//        VM.execMessageIO(op, key, message.getData(), status);
//    }
//
//    /**
//     * Retrieve a message from a client.
//     *
//     * @param op the opcode
//     * @param key the message key
//     * @return the message
//     */
//    private static Message receive(int op, String key) throws IOException {
//        Assert.always(key.length() <= MAX_MESSAGE_KEY_SIZE, "Message key must be less than "+MAX_MESSAGE_KEY_SIZE+1);
//        Address data = VM.execMessageIO(op, key, null, 0);
//        return MessageResourceManager.allocateMessage(data);
//    }
//
//    /**
//     * Send a message to the server.
//     *
//     * @param key the message key
//     * @param message the message to be sent
//     */
//    static void sendToServer(String key, Message message, int status) throws IOException {
//        send(ChannelConstants.INTERNAL_SEND_MESSAGE_TO_SERVER, key, message, ChannelConstants.RESULT_OK);
//    }
//
//    /**
//     * Send a message to the server.
//     *
//     * @param key the message key
//     * @param message the message to be sent
//     */
//    static void sendToClient(String key, Message message, int status) throws IOException {
//        send(ChannelConstants.INTERNAL_SEND_MESSAGE_TO_CLIENT, key, message, status);
//    }
//
//    /**
//     * Retrieve a message from a client.
//     *
//     * @param key the message key
//     * @return the message
//     */
//    public static Message receiveFromClient(String key) throws IOException {
//        return receive(ChannelConstants.INTERNAL_RECEIVE_MESSAGE_FROM_CLIENT, key);
//    }
//
//    /**
//     * Retrieve a message from a server.
//     *
//     * @param key the message key
//     * @return the message
//     */
//    static Message receiveFromServer(String key) throws IOException {
//        return receive(ChannelConstants.INTERNAL_RECEIVE_MESSAGE_FROM_SERVER, key);
//    }
//
//}
//
//
