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
import java.util.*;

import com.sun.squawk.util.*;

/**
 * A JDWPListener receives and processes JDWP requests from
 * a remote debugger entity on behalf of a local debugger entity.
 *
 * The JDWPListener manages a JDWPConnection used to communicate with the remote entity.
 */
public abstract class JDWPListener implements Runnable {

    /**
     * A QuitException is thrown by {@link #waitForCommand} and {@link #waitForReply}
     * if this listener quits before receving the relevant packet.
     */
    public static class QuitException extends RuntimeException {
        QuitException(String closedBy) {
            super("Quit by " + closedBy);
        }
    }

    /**
     * The thread running this listener.
     */
    private Thread thisThread;

    /**
     * The command packets that have been sent, and are waiting for a reply.
     * NOTE: IntHashtables are NOT synchronized, so we need to synchronize access.
     */
    private final IntHashtable sentCommands;

    /**
     * The thread that {@link #quit} this listener.
     */
    private volatile String quittingThread;

    /**
     * The connection used by this listener.
     */
    private JDWPConnection connection;

    /**
     * The connection to the other host participating in a network connection that
     * is being proxied.
     */
    protected JDWPListener otherHost;

    /**
     * Binds this connection with the connection to the other host participating in a
     * proxied JDWP/SDWP debug session.
     *
     * @param p  the connection to the other host
     */
    public void bindProxyPeer(JDWPListener p) {
        otherHost = p;
        p.otherHost = this;

    }

    /**
     * Opens the connection used by this listener and performs a handshake with
     * the remote host once the connection has been established.
     *
     * @param url        the URL for opening the connection
     * @param handshake  an array of bytes that must be exchanged in each
     *                   direction to complete a handshake
     * @param initiate   true if the handshake is to be intiated by this host
     * @param isJDB 
     * @param delayer     code to run after connection but before handshake
     * @throws IOException if the connection could not be opened or if the handshake was not successful
     */
    public void open(String url, byte[] handshake, boolean initiate, boolean isJDB, Runnable delayer) throws IOException {
        connection = new JDWPConnection(url, handshake, initiate, isJDB, delayer);
    }

    /**
     * Processes a single command received over this listeners connection.
     * 
     * @param command
     * @throws IOException
     */
    protected abstract void processCommand(CommandPacket command) throws IOException;

    public final void run() {
        Assert.that(thisThread == null, "run() method should only be called as a result of Thread.start()");
        thisThread = Thread.currentThread();
        if (Log.info()) {
            Log.log("Started event loop");
        }

        try {
            while (!hasQuit()) {
                Packet packet = connection.readPacket();
                if (packet instanceof ReplyPacket) {
                    replyReceived((ReplyPacket)packet);
                    continue;
                }

                CommandPacket command = (CommandPacket)packet;
                if (Log.info()) {
                    Log.log("Received from " + sourceName() + ": " + command);
                }
                processCommand(command);
            }
        } catch (QuitException e) {
            // Listener waiting for packets has been shutdown
            if (Log.info()) {
                Log.log(e.getMessage());
            }
        } catch (JDWPConnection.ClosedException e) {
            // Connection used to send a reply was closed
            if (Log.info()) {
                Log.log("Stopping JDWP as connection was closed: " + e.getMessage());
            }
        } catch (IOException e) {
            if (Log.info()) {
                Log.log("Stopping JDWP listener due to exception: " + e);
                e.printStackTrace();
            }
        }

        if (Log.info()) {
            Log.log("Completed event loop");
        }

        quit();
    }

    /**
     * Creats a JDWPListener.
     */
    public JDWPListener() {
        sentCommands = new IntHashtable();
    }

    /**
     * Notifies this listener that a reply was received. This will notify
     * the thread that sent the command to which the reply pertains.
     *
     * This call is synchronized so that the thread that sent the command gets to
     * complete the send and
     *
     * @param reply  the reply that was received
     */
    private void replyReceived(ReplyPacket reply) {
        Assert.that(reply != null);
        CommandPacket command;
        int id = reply.getID();

        synchronized (sentCommands) {
            command = (CommandPacket)sentCommands.remove(id);
            if (command == null) {
                if (Log.info()) {
                    Log.log("***** Received reply with no sender: " + reply);
                }
                return;
            }
        }

        synchronized (command) {
            command.setReply(reply);
            command.notifyAll();
            if (Log.info()) {
                Log.log("Received from " + sourceName() + ": " + reply);
            }
        }
    }

    /**
     * Sends a command packet over this connection. If the command needs a reply,
     * the current thread blocks until the reply has been received.
     *
     * @param command   the command packet to send
     * @return reply to the command
     * @throws IOException if there was an IO error while sending the packet
     * @throws IllegalStateException if called before the listener has set up a connection and waited for connection to be ready for packets.
     * @throws SDWPException if the received reply has a non-zero error code
     */
    public final ReplyPacket sendCommand(CommandPacket command) throws IOException, SDWPException {
        if (Log.info()) {
            Log.log("Sending to " + sourceName() + ": " + command);
        }
        
        if (command.needsReply()) {

            synchronized (sentCommands) {
                connection.writePacket(command);

                if (Log.debug()) {
                    Log.log("Waiting for reply to: " + command);
                }

                // This is the thread also responsible for receiving replies on
                // the underlying connection and so the send and receive must
                // be serialized here.
                if (Thread.currentThread() == thisThread) {
                    if (Log.info()) {
                        Log.log("Serialized wait for reply to: " + command);
                    }
                    Assert.always(sentCommands.isEmpty());
                    Packet packet = connection.readPacket();
                    Assert.always(packet instanceof ReplyPacket);
                    ReplyPacket reply = (ReplyPacket)packet;
                    Assert.always(reply.getID() == command.getID());
                    command.setReply(reply);
                    return reply;
                } else {
                    sentCommands.put(command.getID(), command);
                }
            }

            synchronized (command) {
                ReplyPacket reply;
                while ((reply = command.getReply()) == null) {
                    checkQuit();
                    try {
                        command.wait();
                    } catch (InterruptedException e) {
                        if (Log.DEBUG_ENABLED && Log.verbose()) {
                            Log.log("Waiting for reply interrupted: " + command);
                        }
                    }
                }
                checkQuit();
                if (reply.getErrorCode() != 0) {
                    throw new SDWPException(reply.getErrorCode(), "reply to " + command + " had an error");
                }
                return command.getReply();
            }

        } else {
            connection.writePacket(command);
            return null;
        }
    }

    public final void sendReply(ReplyPacket reply) throws IOException {
        connection.writePacket(reply);

        if (Log.info()) {
            Log.log("Sent reply to " + sourceName() + ": " + reply);
        }
    }

    private void checkQuit() throws QuitException {
        if (quittingThread != null) {
            throw new QuitException(quittingThread);
        }
    }

    /**
     * Stops this listener's run loop
     *
     * @return  true if this method had already been called (i.e. the listener has already
     *          been requested to quit)
     */
    public boolean quit() {
        boolean quitPreviously;
        
        // quit ourself with our lock held.
        synchronized (this) {
            quitPreviously = (quittingThread != null);
            quittingThread = Thread.currentThread().toString();
            if (!quitPreviously) {

                if (Log.info()) {
                    Log.log("Initiating shutdown of " + this + "...");
                }

                // Wake up all threads waiting for a reply
                synchronized (sentCommands) {
                    Enumeration e = sentCommands.elements();
                    while (e.hasMoreElements()) {
                        Object command = e.nextElement();
                        synchronized (command) {
                            command.notifyAll();
                        }
                    }
                }

                if (connection != null) {
                    connection.close();
                }
            }
        }
        
        // quit otherHost without our lock held.
        if (!quitPreviously && otherHost != null) {
            otherHost.quit();
        }
        
        return quitPreviously;
    }

    /**
     * Determines if this listener has quit.
     *
     * @return boolean
     */
    public boolean hasQuit() {
        return quittingThread != null;
    }
     
    /**
     * Subclasses must return a meaning name for this listener that will be useful
     * as a suffix for log messages on this thread.
     *
     * @return the name of the thing that this listener is talking to.
     */
    public abstract String sourceName();
    
    /**
     * Get the input stream from a command packet (sniffer needs to wrap input stream.
     * @param command packet to read from
     * @return the input stream to read with
     */
    public PacketInputStream getInputStreamFor(CommandPacket command) {
        return command.getInputStream();
    }
    
    /*-----------------------------------------------------------------------*\
     *                            Command Set                                *
    \*-----------------------------------------------------------------------*/

    /**
     * The implementation of each JDWP Command Set subclasses this class.
     */
    public static abstract class CommandSet {

        /**
         * The command currently being processed.
         */
        protected CommandPacket command;

        /**
         * The reply to the current command.
         */
        protected ReplyPacket reply;

        /**
         * Stream from which data of current command is read.
         */
        protected PacketInputStream in;

        /**
         * Stream to which data in reply is written.
         */
        protected PacketOutputStream out;

        /**
         * Handles a command packet by setting up the variables used to interpret and reply
         * to the command and then dispatching to the specific command handler.
         *
         * @param listener the listener to reply through
         * @param command  the command to be handled
         * @return boolean true if the command was recognised and a reply was sent
         * @throws IOException if there was an IO error while sending a reply
         */
        public final boolean handle(JDWPListener listener, CommandPacket command) throws IOException {
            this.command = command;
            this.reply = command.createReply(JDWP.Error_NONE);
            this.in = listener.getInputStreamFor(command);
            this.out = reply.getOutputStream();

            boolean handled = true;
            try {
                handled = dispatch();
            } catch (SDWPException e) {
                if (Log.info()) {
                    System.err.println(command + " caused: " + e);
                    e.printStackTrace();
                }
                error(e.getError());
            } catch (IOException e) {
                System.err.println(command + " caused: " + e);
                e.printStackTrace();
                error(JDWP.Error_NOT_FOUND);
            }

            if (handled) {
                if (command.needsReply()) {
                    listener.sendReply(reply);
                }
                postDispatch();
            }

            return handled;
        }

        /**
         * Dispatches to the handler for the specific command in the command set.
         *
         * @return  true if the handler generated a reply which must now be sent
         * @throws IOException if an IO error occurs
         * @throws SDWPException if a JDWP/SDWP protocol error occurs
         */
        protected abstract boolean dispatch() throws IOException, SDWPException;

        /**
         * Used by subclasses to change the error code sent in the reply.
         *
         * @param errorCode  the new error code
         */
        protected final void error(int errorCode) {
            reply.updateErrorCode(errorCode);
        }

        /**
         * Reports that the JDWP/SDWP command is not (currently) implemented.
         */
        protected final void unimplemented() {
            error(JDWP.Error_NOT_IMPLEMENTED);
        }

        /**
         * A hook for the handler to do any post-processing after it has handled a command
         * and the reply was sent (if applicable).
         */
        protected void postDispatch() {}
    }
}
