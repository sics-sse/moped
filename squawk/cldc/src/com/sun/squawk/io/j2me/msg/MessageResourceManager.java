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
//import java.util.*;
//
//import com.sun.squawk.*;
//
///**
// * This class allows for the highly dubious practice of object reuse ;-).
// *
// */
//
//public class MessageResourceManager {
//
///*if[REUSEABLE_MESSAGES]*/
//
//    /**
//     * List of reuseable ClientProtocols
//     */
//    private static Stack freeClientProtocols = new Stack();
//
//    /**
//     * List of reuseable ServerProtocols
//     */
//    private static Stack freeServerProtocols = new Stack();
//
//    /**
//     * List of reuseable MessageInputStreams
//     */
//    private static Stack freeInputStreams = new Stack();
//
//    /**
//     * List of reuseable MessageInputStreams
//     */
//    private static Stack freeOutputStreams = new Stack();
//
//    /**
//     * List of reuseable MessageInputStreams
//     */
//    private static Stack freeMessages = new Stack();
//
///*end[REUSEABLE_MESSAGES]*/
//
//    /**
//     * Allocates a client protocol object
//     */
//    public static ClientProtocol allocateClientProtocol() {
//        ClientProtocol res;
///*if[REUSEABLE_MESSAGES]*/
//        if (!freeClientProtocols.isEmpty()) {
//            res = (ClientProtocol)freeClientProtocols.pop();
//            res.resetInstanceState();
//        } else
///*end[REUSEABLE_MESSAGES]*/
//        {
//            res = new ClientProtocol();
//        }
//        return res;
//    }
//
//    /**
//     * Allocates a server protocol object.
//     *
//     * @param name the namespace target for the message
//     * @param data the input data
//     */
//    public static ServerProtocol allocateServerProtocol(String name, Message data) {
//        ServerProtocol res;
///*if[REUSEABLE_MESSAGES]*/
//        if (!freeServerProtocols.isEmpty()) {
//            res = (ServerProtocol)freeServerProtocols.pop();
//        } else
///*end[REUSEABLE_MESSAGES]*/
//        {
//            res = new ServerProtocol();
//        }
//        res.resetInstanceState(name, data);
//        return res;
//    }
//
//    /**
//     * Frees a ClientProtocol.
//     *
//     * @param con the connection
//     */
//    static void freeClientProtocol(ClientProtocol con) {
///*if[REUSEABLE_MESSAGES]*/
//        freeClientProtocols.push(con);
///*end[REUSEABLE_MESSAGES]*/
//    }
//
//    /**
//     * Frees a ServerProtocol.
//     *
//     * @param con the connection
//     */
//    static void freeServerProtocol(ServerProtocol con) {
///*if[REUSEABLE_MESSAGES]*/
//        freeServerProtocols.push(con);
///*end[REUSEABLE_MESSAGES]*/
//    }
//
//    /**
//     * Allocates a message input stream.
//     *
//     * @param msc the call back object
//     */
//    static MessageInputStream allocateInputStream(MessageStreamCallback msc) {
//        MessageInputStream is;
///*if[REUSEABLE_MESSAGES]*/
//        if (!freeInputStreams.isEmpty()) {
//            is = (MessageInputStream)freeInputStreams.pop();
//        } else
///*end[REUSEABLE_MESSAGES]*/
//        {
//            is = new MessageInputStream();
//        }
//        is.resetInstanceState(msc);
//        return is;
//    }
//
//    /**
//     * Frees a message input stream.
//     *
//     * @param is the stream
//     */
//    static void freeInputStream(MessageInputStream is) {
///*if[REUSEABLE_MESSAGES]*/
//        freeInputStreams.push(is);
///*end[REUSEABLE_MESSAGES]*/
//    }
//
//    /**
//     * Allocates a message output stream.
//     *
//     * @param msc the call back object
//     */
//    static MessageOutputStream allocateOutputStream(MessageStreamCallback msc) {
//        MessageOutputStream os;
///*if[REUSEABLE_MESSAGES]*/
//        if (!freeOutputStreams.isEmpty()) {
//            os = (MessageOutputStream)freeOutputStreams.pop();
//        } else
///*end[REUSEABLE_MESSAGES]*/
//        {
//            os = new MessageOutputStream();
//        }
//        os.resetInstanceState(msc);
//        return os;
//    }
//
//    /**
//     * Frees a message output stream.
//     *
//     * @param os the stream
//     */
//    static void freeOutputStream(MessageOutputStream os) {
///*if[REUSEABLE_MESSAGES]*/
//        freeOutputStreams.push(os);
///*end[REUSEABLE_MESSAGES]*/
//    }
//
//    /**
//     * Allocates a message.
//     *
//     * @return the message
//     */
//    static Message allocateMessage() {
//        Message msg;
///*if[REUSEABLE_MESSAGES]*/
//        if (!freeMessages.isEmpty()) {
//            msg = (Message)freeMessages.pop();
//        } else
///*end[REUSEABLE_MESSAGES]*/
//        {
//            msg = new Message();
//        }
//        msg.resetInstanceState();
//        return msg;
//    }
//
//    /**
//     * Allocates a message.
//     *
//     * @param buffers a list of buffers
//     * @return the message
//     */
//    static Message allocateMessage(Address buffers) {
//        Message msg;
///*if[REUSEABLE_MESSAGES]*/
//        if (!freeMessages.isEmpty()) {
//            msg = (Message)freeMessages.pop();
//        } else
///*end[REUSEABLE_MESSAGES]*/
//        {
//            msg = new Message();
//        }
//        msg.setData(buffers);
//        return msg;
//    }
//
//    /**
//     * Frees a message.
//     *
//     * @param msg the message
//     */
//    static void freeMessage(Message msg) {
///*if[REUSEABLE_MESSAGES]*/
//        msg.freeAll();
//        freeMessages.push(msg);
///*end[REUSEABLE_MESSAGES]*/
//    }
//
//}
//
//
