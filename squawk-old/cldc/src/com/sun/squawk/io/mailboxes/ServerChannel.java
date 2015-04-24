//if[NEW_IIC_MESSAGES]
/*
 * Copyright 2004-2010 Sun Microsystems, Inc. All Rights Reserved.
 * Copyright 2011 Oracle Corporation. All Rights Reserved.
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
 * Please contact Oracle Corporation, 500 Oracle Parkway, Redwood
 * Shores, CA 94065 or visit www.oracle.com if you need additional
 * information or have any questions.
 */

package com.sun.squawk.io.mailboxes;

import com.sun.squawk.util.Assert;

/**
 * Given that a {@link Channel} is a one-to-one connection between two isolates, a ServerChannel 
 * provides a factory to create new Channels by name. It is similar to how network sockets
 * can use a port number to accept a number of client connections.<p>
 * 
 * A server can use the <code>accept</code> method to accept new client connections, which will return a new Channel
 * that the server can use to talk to the client. A server may choose to service each Channel in a separate thread.
 *
 * <h3>Example</h3>
 * <pre>
 *class NewChannelTimeTest {
    public final static String MAILBOX_NAME = "NewChannelTimeTest";
    public static final String msg1 = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    public final static int NUM_MESSAGES = 1000;
    public final static int NUM_CLIENTS = 3;

    public static void main(String[] args) throws Exception {
        Client client =new Client();
        
        Server server = new Server();
        server.start();
        
        client.start();
        client.join();
        
        server.join();
    }
    
    static class Client extends Thread {
        public void run() {
             try {
                byte[] data = msg1.getBytes();
                
                long start = System.currentTimeMillis();
                Channel testChan = Channel.lookup(MAILBOX_NAME);
                
                for (int i = 0; i < NUM_MESSAGES; i++) {
                    Envelope cmdEnv = new ByteArrayEnvelope(data);
                    testChan.send(cmdEnv);
                    ByteArrayEnvelope replyEnv = (ByteArrayEnvelope)testChan.receive();
                    byte[] replyData = replyEnv.getData();
                    if (replyData.length != 1 || (replyData[0] != 0)) {
                        System.err.println("Reply not OK");
                    }
                }
                long time = System.currentTimeMillis() - start;
                
                System.err.println("Client sent " + NUM_MESSAGES + " messages of " + data.length + " bytes in " + time + "ms");
                testChan.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    static class Server extends Thread {
        
        public void run() {
            byte[] replyData = new byte[1];
            ServerChannel serverChannel = null;
            Channel aChannel = null;
            try {
                serverChannel = ServerChannel.create(MAILBOX_NAME);
            } catch (MailboxInUseException ex) {
                throw new RuntimeException(ex.toString());
            }
            
            try {
                aChannel = serverChannel.accept();

                // handle messages:
                while (true) {
                    Envelope msg;
                    try {
                        msg = aChannel.receive();
                    } catch (MailboxClosedException e) {
                        System.out.println("Server seems to have gone away. Oh well. " + aChannel);
                        break;
                    }
                    
                    if (msg instanceof ByteArrayEnvelope) {
                        ByteArrayEnvelope dataEnv = (ByteArrayEnvelope)msg;
                        byte[] data = dataEnv.getData();
                        
                        replyData[0] = 0;
                        Envelope replyEnv = new ByteArrayEnvelope(replyData);
                        try {
                            aChannel.send(replyEnv);
                        } catch (AddressClosedException ex) {
                            System.out.println("Client seems to have gone away. Oh well. " + aChannel);
                        }
                    }
                }
            } catch (IOException ex) {
                // ok, just close server.
            } finally {
                // no way to get here:
                System.out.println("Closing server...");
                aChannel.close();
                serverChannel.close();
            }
        }
    }
}
 * </pre>
 *
 * @see Channel
 */
public final class ServerChannel {
    /**
     * The named mailbox that clients will initially talk to.
     */
    private Mailbox serverBox;
    
    /**
     * Creates a new instance of ServerChannel
     */
    private ServerChannel() {
    }
    
    /**
     * Handler that creates a new private mailbox when connect to by client in Channel.lookup().
     */
    final static class ServerChannelMailboxHandler implements MailboxHandler {
        
        /** @inheritdoc */
        public MailboxAddress handleOpen(Mailbox originalMailbox, MailboxAddress originalAddress, MailboxAddress replyAddress) {
            // Note that this is called in the context of the client isolate. It creates several objects that
            // are passed directly to the server isolate, but doesn't retain pointers to them, so it's OK.
            
            // Create a new mailbox, and private address for it. Create a Channel wrapping all.
            Mailbox channelInBox = originalMailbox.createSubMailbox();
            MailboxAddress channeInboxAddress = new MailboxAddress(channelInBox);
            Channel newChannel = new Channel(replyAddress, channelInBox);
            
            try {
                // tell ServerChannel.accept about the new Channel:
                originalAddress.send0(new NewChannelEnvelope(newChannel));
            } catch (AddressClosedException ex) {
                // inBox closed down since lookup. Unlikely, but..
                return null; // lookup will fail with NoSuchMailboxException
            }
            
            // tell client address of the new channel
            return channeInboxAddress;
        }

        /** @inheritdoc */
        public AddressClosedException handleClose(MailboxAddress address) {
            throw Assert.shouldNotReachHere(); // Clients never get the address of serverBox, so this should never get called:
        }
        
    } /* ServerChannelMailboxHandler */
    
    /**
     * Creates a new ServerChannel with the given name and registers it with the system.
     *
     * When a client does a lookup on this ServerChannel, the ServerChannel creates a new private Channel,
     * and tells the client to use the private Channel.
     * 
     * Given a ServerChannel, a server may call accept(), waiting for clients to connect to it. Accept will return
     * with a new Channel to handle communication with this client.
     *
     * @param name        the name that this ServerChannel can be looked up under.
     * @return the new ServerChannel
     * @throws MailboxInUseException if there is already a ServerChannel registered under the name <code>name</code>.
     */
    public static ServerChannel create(String name) throws MailboxInUseException {
        Mailbox serverBox = Mailbox.create(name, new ServerChannelMailboxHandler());
        ServerChannel server = new ServerChannel();
        server.serverBox = serverBox;
        Assert.that(server.isOpen());
        return server;
    }
    
    /**
     * Re-opens a closed ServerChannel.<p>
     *
     * Can be called after hibernation or an explicit call to close() closed this ServerChannel. <p>
     *
     * NOTE: To avoid race conditions during hibernation, should sleep a little (100ms) between getting a 
     * MailboxClosedException and calling reOpen().<p>
     * 
     * @todo Design better scheme to handle race.
     *
     * @throws MailboxInUseException if there is already a ServerChannel registered under the name <code>name</code>.
     * @throws IllegalStateException if the ServerChannel is already open.
     */
    public void reOpen() throws MailboxInUseException {
        if (isOpen()) {
            throw new IllegalStateException();
        }
        serverBox = Mailbox.create(getName(), new ServerChannelMailboxHandler());
        Assert.that(isOpen());
    }
    
    /**
     * NewChannelEnvelope is used to pass the new channel created in handleopen to the receive() call in accept().
     *
     * We use receive() in order to get notification when the ServerChannel is closed (recieve will get an AddressClosedException.)
     */
    final static class NewChannelEnvelope extends Envelope {
        
        Channel newChannel;
        
        NewChannelEnvelope(Channel newChannel) {
            this.newChannel = newChannel;
        }

        public Object getContents() {
            return newChannel;
        }
        
        /**
         * Well-typed version of getObject().
         */
        Channel getData() {
            return newChannel;
        }
        
        /**
         * This override of copy() completely cheats. Although the envelope is created in one isolate and read in another, the
         * sending isolate doesn't retain any references to the envelope, so it's OK to pass original.
         */
        Envelope copy() {
            return this;
        }
    }
    
    /**
     * Get the name that this ServerChannel was registered under.
     *
     * @return the name
     */
    public String getName() {
        return serverBox.getName();
    }
    
    /**
     * Wait for a client to open a connection, then create an anonymous local Channel to use or further communication.
     *
     * @return a new Channel to the client
     * @throws MailboxClosedException if the ServerChannel is closed (either explicitly, or by isolate hibernation)
     */
    public Channel accept() throws MailboxClosedException {
        if (!isOpen()) {
            throw new MailboxClosedException(serverBox);
        }
        
        try {
            NewChannelEnvelope env = (NewChannelEnvelope)serverBox.receive();
            return env.getData();
        } catch (AddressClosedException ex) {
            throw new MailboxClosedException(serverBox); // this shouldn't happen
        }
    }
    
    /**
     * Unregisters this ServerChannel.
     * Existing Channels that came from this channel are not closed.
     */
    public void close() {
        if (serverBox.isOpen()) {
            serverBox.close();
        }
    }
    
   /**
     * Return true if the server channel is open, registered, and can accept new connections..
     *
     * @return true if open.
     */
    public boolean isOpen() {
        return serverBox.isOpen();
    }
}
