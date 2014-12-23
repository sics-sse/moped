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
//package com.sun.squawk.io;
//
//import com.sun.squawk.io.j2me.msg.*;
//import java.io.*;
//
//import javax.microedition.io.*;
//
///**
// * This interface defines the call back interface for kernel mode Java device drivers.
// *
// */
//public abstract class ServerConnectionHandler {
//
//    /**
//     * The next ServerConnectionHandler in the queue.
//     */
//    private ServerConnectionHandler next;
//
//    /**
//     * The name of the message stream being processed.
//     */
//    private final String name;
//
//    /**
//     * Creates a new ServerConnectionHandler.
//     *
//     * @param name the name of the message stream being processed
//     */
//    public ServerConnectionHandler(String name) {
//        this.name = name;
//    }
//
//    /**
//     * Sets the next field of the ServerConnectionHandler
//     *
//     * @param next the next ServerConnectionHandler
//     */
//    public void setNext(ServerConnectionHandler next) {
//        this.next = next;
//    }
//
//    /**
//     * Returns a name of the connection.
//     *
//     * @return the name of the message stream being processed
//     */
//    public String getConnectionName() {
//        return name;
//    }
//
//    /**
//     * Causes the pending server message to be processed.
//     */
//    public void processServerMessage() throws IOException {
//        Message msg = Database.receiveFromClient(name);
//        ServerProtocol con = MessageResourceManager.allocateServerProtocol(name, msg);
//        processConnection(con);
//    }
//
//    /**
//     * Searches a list of ServerConnectionHandlers for a handler whose {@link #getConnectionName name} matches a given string.
//     *
//     * @param sch    the handler to start searching from
//     * @param name   the string to match
//     * @return the ServerConnectionHandler whose name matches <code>name</code> or null if there isn't one
//     */
//    public static ServerConnectionHandler lookup(ServerConnectionHandler sch, String name) {
//        while (sch != null) {
//            if (sch.name.equals(name)) {
//                break;
//            }
//            sch = sch.next;
//        }
//        return sch;
//    }
//
//    /**
//     * Processes an incoming connection.
//     *
//     * @param con the incoming connection
//     */
//    public abstract void processConnection(StreamConnection con);
//}
//
//
