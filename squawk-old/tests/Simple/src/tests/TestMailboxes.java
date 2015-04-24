//if[EXCLUDE]
////Mailbox API is now private...
///*
// * TestMailboxes.java
// *
// * Created on February 27, 2006, 4:28 PM
// *
// * To change this template, choose Tools | Template Manager
// * and open the template in the editor.
// */
//
//package tests;
//
//
//import com.sun.squawk.*;
//import com.sun.squawk.util.Assert;
//import com.sun.squawk.io.mailboxes.*;
//
//import java.io.*;
//import javax.microedition.io.*;
//
///**
// *
// * @author dw29446
// */
//public class TestMailboxes {
//    public static final String msg1 = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
//    public final static int NUM_MESSAGES = 1000;
//    public final static int NUM_CLIENTS = 3;
//    
//    static void x1() {
//        Isolate iso = Isolate.currentIsolate();
//        
//        Isolate server = new Isolate("tests.MailboxServer", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        server.start();
//        
//        Isolate client1 = new Isolate("tests.MailboxClient", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        client1.start();
//        
//        Isolate client2 = new Isolate("tests.MailboxClient", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        client2.start();
//        
//        Isolate client3 = new Isolate("tests.MailboxClient", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        client3.start();
//        
//        client1.join();
//        System.out.println("Joined 1...");
//        client2.join();
//        System.out.println("Joined 2...");
//        client3.join();
//        System.out.println("Joined 3...");
//        
//        // server will exit when it sees all NUM_CLIENTS close.
//    }
//    
//    /**
//     * Test ByteArrayInputStreamEnvelopes.
//     */
//    static void x2() {
//        Isolate iso = Isolate.currentIsolate();
//        
//        Isolate server = new Isolate("tests.MailboxBAISServer", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        System.err.println("Starting tests.MailboxBAISServer");
//        server.start();
//        
//        System.err.println("Starting tests.MailboxBAISClient");
//        Isolate client1 = new Isolate("tests.MailboxBAISClient", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        client1.start();
//        
//        Isolate client2 = new Isolate("tests.MailboxBAISClient", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        client2.start();
//        
//        Isolate client3 = new Isolate("tests.MailboxBAISClient", new String[0], iso.getClassPath(), iso.getParentSuiteSourceURI());
//        client3.start();
//        
//        client1.join();
//        System.out.println("Joined 1...");
//        client2.join();
//        System.out.println("Joined 2...");
//        client3.join();
//        System.out.println("Joined 3...");
//        
//        // server will exit when it sees all NUM_CLIENTS close.
//    }
//    
//    public static void main(String[] args) {
//        Runtime runtime = Runtime.getRuntime();
//        
//        System.out.println("Sanity tests: ByteArrayEnvelope");
//        x1();
//        
//        System.out.println("Sanity tests:ByteArrayInputStreamEnvelope");
//        x2();
//        
//        System.out.println("\n cleanup GC:");
//        System.gc();
//        System.gc();
//        System.out.println("\nTime test of new IIC (3 runs):");
//        try {
//            long free1 = runtime.freeMemory();
//            NewMsgTimeTest.main(new String[0]);
//            long free2 = runtime.freeMemory();
//            NewMsgTimeTest.main(new String[0]);
//            NewMsgTimeTest.main(new String[0]);
//            System.out.println("at least " + (free1 - free2) + " bytes used in one run");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        
//        System.out.println("\n cleanup GC:");
//        System.gc();
//        System.gc();
//        System.out.println("\nTime test of new IIC using ByteArrayInputStreams (3 runs):");
//        try {
//            long free1 = runtime.freeMemory();
//            ISMsgTimeTest.main(new String[0]);
//            long free2 = runtime.freeMemory();
//            ISMsgTimeTest.main(new String[0]);
//            ISMsgTimeTest.main(new String[0]);
//            System.out.println("at least " + (free1 - free2) + " bytes used in one run");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        
//        System.out.println("\n cleanup GC:");
//        System.gc();
//        System.gc();
//        System.out.println("\nTime test of old IIC (3 runs):");
//        try {
//            long free1 = runtime.freeMemory();
//            OldMsgTimeTest.main(new String[0]);
//            long free2 = runtime.freeMemory();
//            OldMsgTimeTest.main(new String[0]);
//            OldMsgTimeTest.main(new String[0]);
//            System.out.println("at least " + (free1 - free2) + " bytes used in one run");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        
//        
//        System.exit(0);
//    }
//}
//
///**
// * This simple server waits for a byte, and it replies with an array of byte repeated three times.
// * 
// * It will quit when it gets notified that NUM_CLIENTS have closed their connections.
// */
//class MailboxServer {
//    private static int clientClosedCount = 0;
//        
//    public static void main(String[] args) {
//        Mailbox inBox = null;
//        try {
//            inBox = Mailbox.create("echo", new SharedMailboxHandler() {
//                 // We need a way to shut down the server isolate, so count number of client closes.
//                 // We could have thrown an AddressClosedException exception instead, and done 
//                 // counting in catch handler around Mailbox.receive().
//                 public AddressClosedException handleClose(MailboxAddress address) {
//                     if (++clientClosedCount >= TestMailboxes.NUM_CLIENTS) {
//                         return new AddressClosedException(address);
//                     }
//                     return null;
//                 }
//            });
//        } catch (MailboxInUseException ex) {
//            throw new RuntimeException(ex.toString());
//        }
//        
//        boolean run = true;
//        try {
//            // handle messages:
//            while (run) {
//                Envelope msg = null;
//                try {
//                    msg = inBox.receive();
//                } catch (AddressClosedException ex) {
//                    // all NUM_CLIENTS have closed, so go home
//                    run = false;
//                } catch (IOException ex) {
//                    System.err.println("MailboxServer got an IOException: " + ex);
//                    ex.printStackTrace();
//                }
//                
//                if (msg instanceof ByteArrayEnvelope) {
//                    ByteArrayEnvelope dataEnv = (ByteArrayEnvelope)msg;
//                    byte[] data = dataEnv.getData();
//                    Assert.that(data.length == 1);
//                    byte b = data[0];
//                    byte[] replyData = new byte[3];
//                    replyData[0] = b;
//                    replyData[1] = b;
//                    replyData[2] = b;
//                    
//                    Envelope replyEnv = new ByteArrayEnvelope(replyData);
//                    try {
//                        msg.getReplyAddress().send(replyEnv);
//                    } catch (AddressClosedException ex) {
//                        System.out.println("Client seems to have gone away. Oh well. " + msg.getToAddress());
//                    }
//                }
//            }
//        } finally {
//            System.out.println("Closing server...");
//            inBox.close();
//        }
//    }
//    
//}
//
///**
// * Send a byte to the "echo" mailbox, and check that we got the same byte repeated 3 times back.
// */
//class MailboxClient {
//    
//    public static void main(String[] args) {
//        try {
//            Mailbox inBox = Mailbox.create(); // create anon. mailbox for messages from radio.
//            MailboxAddress echoBox = null;
//            
//            // retry a few times in case client got ahead of server...
//            for (int i = 0; i < 10; i++) {
//                try {
//                    echoBox = MailboxAddress.lookupMailbox("echo", inBox);  // send OPEN Service cmd.
//                    break;
//                } catch (NoSuchMailboxException e) {
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//            
//            if (echoBox == null) {
//                System.err.println("ERROR: MailboxClient couldn't find mailbox echo");
//                return;
//            }
//            
//            byte counter = (byte)System.currentTimeMillis(); // init with semi random value.
//            byte[] cmdData = new byte[1];
//            
//            for (int i = 0; i < 100; i++) {
//                cmdData[0] = counter;
//                
//                Envelope cmdEnv = new ByteArrayEnvelope(cmdData);
//                echoBox.send(cmdEnv);
//                ByteArrayEnvelope replyEnv = (ByteArrayEnvelope)inBox.receive();
//                byte[] replyData = replyEnv.getData();
//                if (replyData.length == 3 && (replyData[0] == counter) && (replyData[1] == counter) && (replyData[2] == counter)) {
//                    System.out.println("Message sent and received OK: " + counter);
//                } else {
//                    System.out.println("Reply doesn't match command: " + counter);
//                    System.out.println("    replyData[0]: " + replyData[0]);
//                    System.out.println("    replyData[1]: " + replyData[1]);
//                    System.out.println("    replyData[2]: " + replyData[2]);
//                }
//                counter++;
//            }
//            System.out.println("Closing client...");
//            
//            echoBox.close();
//            inBox.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        
//    }
//    
//}
//
///**
// * This simple server waits for a byte, and it replies with an array of byte repeated three times.
// */
//class MailboxBAISServer {
//    private static int clientClosedCount = 0;
//        
//    public static void main(String[] args) {
//        Mailbox inBox = null;
//        try {
//            inBox = Mailbox.create("MailboxBAISServer", new SharedMailboxHandler() {
//                 // We need a way to shut down the server isolate, so count number of client closes.
//                 // We could have thrown an AddressClosedException exception instead, and done 
//                 // counting in catch handler around Mailbox.receive().
//                 public AddressClosedException handleClose(MailboxAddress address) {
//                     if (++clientClosedCount >= TestMailboxes.NUM_CLIENTS) {
//                         return new AddressClosedException(address);
//                     }
//                     return null;
//                 }
//            });
//        } catch (MailboxInUseException ex) {
//            throw new RuntimeException(ex.toString());
//        }
//        System.err.println("MailboxBAISServer created.");
//        boolean run = true;
//        try {
//            
//            // handle messages:
//            while (run) {
//                Envelope msg = null;
//                try {
//                    msg = inBox.receive();
//                } catch (AddressClosedException ex) {
//                    run = false;
//                } catch (IOException ex) {
//                    System.err.println("MailboxServer got an IOException: " + ex);
//                    ex.printStackTrace();
//                }
//                
//                if (msg instanceof ByteArrayInputStreamEnvelope) {
//                    ByteArrayInputStreamEnvelope dataEnv = (ByteArrayInputStreamEnvelope)msg;
//                    ByteArrayInputStream data = dataEnv.getData();
//
//                    byte b = (byte)data.read();
//                    byte[] replyData = new byte[3];
//                    replyData[0] = b;
//                    replyData[1] = b;
//                    replyData[2] = b;
//                    
//                    Envelope replyEnv = new ByteArrayInputStreamEnvelope(replyData);
//                    try {
//                        msg.getReplyAddress().send(replyEnv);
//                    } catch (AddressClosedException ex) {
//                        System.out.println("Client seems to have gone away. Oh well. " + msg.getToAddress());
//                    }
//                }
//            }
//        } finally {
//            System.out.println("Closing server...");
//            inBox.close();
//        }
//    }
//    
//}
//
///**
// * Send a byte to the "echo" mailbox, and check that we got the same byte repeated 3 times back.
// */
//class MailboxBAISClient {
//    
//    public static void main(String[] args) {
//        try {
//            Mailbox inBox = Mailbox.create(); // create anon. mailbox for messages from radio.
//            MailboxAddress echoBox = null;
//            // retry a few times in case client got ahead of server...
//            for (int i = 0; i < 10; i++) {
//                try {
//                    echoBox = MailboxAddress.lookupMailbox("MailboxBAISServer", inBox);  // send OPEN Service cmd.
//                    break;
//                } catch (NoSuchMailboxException e) {
//                    try {
//                        Thread.sleep(5);
//                    } catch (InterruptedException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//            }
//            
//            if (echoBox == null) {
//                System.err.println("ERROR: MailboxBAISClient couldn't find mailbox MailboxBAISServer");
//                return;
//            }
//            
//            byte counter = (byte)System.currentTimeMillis(); // init with semi random value.
//            byte[] cmdData = new byte[1];
//            
//            for (int i = 0; i < 100; i++) {
//                cmdData[0] = counter;
//                
//                Envelope cmdEnv = new ByteArrayInputStreamEnvelope(cmdData);
//                echoBox.send(cmdEnv);
//                ByteArrayInputStreamEnvelope replyEnv = (ByteArrayInputStreamEnvelope)inBox.receive();
//                ByteArrayInputStream replyData = replyEnv.getData();
//                byte b1 = (byte)replyData.read();
//                byte b2 = (byte)replyData.read();
//                byte b3 = (byte)replyData.read();
//                if ((b1 == counter) && (b2 == counter) && (b3 == counter)) {
//                    System.out.println("Message sent and received OK: " + counter);
//                } else {
//                    System.out.println("Reply doesn't match command: " + counter);
//                    System.out.println("    b1: " + b1);
//                    System.out.println("    b2: " + b2);
//                    System.out.println("    b3: " + b3);
//                }
//                
//                counter++;
//            }
//            System.out.println("Closing client...");
//            
//            echoBox.close();
//            inBox.close();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        
//    }
//    
//}
//
///**
// *===============================================================================
// * Time new inter-isolate mechanism.
// * Time client sending NUM_MESSAGES messages, where server responds with error code.
// */
//class NewMsgTimeTest {
//
//    public static void main(String[] args) throws Exception {
//        Client client =new Client();
//        
//        Server server = new Server();
//        server.start();
//        
//        client.start();
//        client.join();
//        
//        server.join();
//    }
//    
//    /**
//     * The client thread class.
//     */
//    static class Client extends Thread {
//        public void run() {
//             try {
//                byte[] data = TestMailboxes.msg1.getBytes();
//                
//                long start = System.currentTimeMillis();
//                Mailbox inBox = Mailbox.create(); // create anon. mailbox for messages from radio.
//                MailboxAddress echoBox = MailboxAddress.lookupMailbox("NewMsgTimeTest", inBox);  // send OPEN Service cmd.
//                
//                for (int i = 0; i < TestMailboxes.NUM_MESSAGES; i++) {
//                    Envelope cmdEnv = new ByteArrayEnvelope(data);
//                    echoBox.send(cmdEnv);
//                    ByteArrayEnvelope replyEnv = (ByteArrayEnvelope)inBox.receive();
//                    byte[] replyData = replyEnv.getData();
//                    if (replyData.length != 1 || (replyData[0] != 0)) {
//                        System.err.println("Reply not OK");
//                    }
//                }
//                long time = System.currentTimeMillis() - start;
//                
//                System.err.println("Client sent " + TestMailboxes.NUM_MESSAGES + " messages of " + data.length + " bytes in " + time + "ms");
//                echoBox.close();
//                inBox.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//    
//    /**
//     * The server thread class.
//     */
//    static class Server extends Thread {
//        
//        public void run() {
//            byte[] replyData = new byte[1];
//            Mailbox inBox = null;
//            
//            try {
//                // use handler that will throw exception when client closes:
//                inBox = Mailbox.create("NewMsgTimeTest", new SharedMailboxHandler() {
//                    // arrange to shut down server when client goes away:
//                    public AddressClosedException handleClose(MailboxAddress address) {
//                        return new AddressClosedException(address);
//                    }
//                });
//            } catch (MailboxInUseException ex) {
//                throw new RuntimeException(ex.toString());
//            }
//            
//            try {
//                
//                // handle messages:
//                while (true) {
//                    Envelope msg;
//                    try {
//                        msg = inBox.receive();
//                    } catch (MailboxClosedException e) {
//                        // TODO Auto-generated catch block
//                        System.out.println("Server seems to have gone away. Oh well. " + inBox);
//                        break;
//                    }
//                    
//                    if (msg instanceof ByteArrayEnvelope) {
//                        ByteArrayEnvelope dataEnv = (ByteArrayEnvelope)msg;
//                        byte[] data = dataEnv.getData();
//                        
//                        replyData[0] = 0;
//                        Envelope replyEnv = new ByteArrayEnvelope(replyData);
//                        try {
//                            msg.getReplyAddress().send(replyEnv);
//                        } catch (AddressClosedException ex) {
//                            System.out.println("Client seems to have gone away. Oh well. " + msg.getToAddress());
//                        }
//                    }
//                }
//            } catch (IOException ex) {
//                // ok, just close server.
//            } finally {
//                // no way to get here:
//                System.out.println("Closing server...");
//                inBox.close();
//            }
//        }
//    }
//}
//
///**
// *===============================================================================
// * Time new inter-isolate mechanism.
// * Time client sending NUM_MESSAGES messages, where server responds with error code.
// */
//class ISMsgTimeTest {
//
//    public static void main(String[] args) throws Exception {
//        Client client =new Client();
//        
//        Server server = new Server();
//        server.start();
//        
//        client.start();
//        client.join();
//        
//        server.join();
//    }
//    
//    /**
//     * The client thread class.
//     */
//    static class Client extends Thread {
//        public void run() {
//             try {
//                byte[] data = TestMailboxes.msg1.getBytes();
//                
//                long start = System.currentTimeMillis();
//                Mailbox inBox = Mailbox.create(); // create anon. mailbox for messages from radio.
//                MailboxAddress echoBox = MailboxAddress.lookupMailbox("ISMsgTimeTest", inBox);  // send OPEN Service cmd.
//                
//                for (int i = 0; i < TestMailboxes.NUM_MESSAGES; i++) {
//                    Envelope cmdEnv = new ByteArrayInputStreamEnvelope(data);
//                    echoBox.send(cmdEnv);
//                    ByteArrayInputStream replyData = (ByteArrayInputStream)inBox.receive().getContents();
//                    if (replyData.read() != 0) {
//                        System.err.println("Reply not OK");
//                    }
//                }
//                long time = System.currentTimeMillis() - start;
//                
//                System.err.println("Client sent " + TestMailboxes.NUM_MESSAGES + " messages of " + data.length + " bytes in " + time + "ms");
//                echoBox.close();
//                inBox.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//    
//    /**
//     * The server thread class.
//     */
//    static class Server extends Thread {
//        
//        public void run() {
//            byte[] replyData = new byte[1];
//            Mailbox inBox = null;
//            
//            try {
//                // use handler that will throw exception when client closes:
//                inBox = Mailbox.create("ISMsgTimeTest", new SharedMailboxHandler() {
//                    // arrange to shut down server when client goes away:
//                    public AddressClosedException handleClose(MailboxAddress address) {
//                        return new AddressClosedException(address);
//                    }
//                });
//            } catch (MailboxInUseException ex) {
//                throw new RuntimeException(ex.toString());
//            }
//            
//            try {
//                
//                // handle messages:
//                while (true) {
//                    Envelope msg;
//                    try {
//                        msg = inBox.receive();
//                    } catch (MailboxClosedException e) {
//                        // TODO Auto-generated catch block
//                        System.out.println("Server seems to have gone away. Oh well. " + inBox);
//                        break;
//                    }
//                
//                    if (msg instanceof ByteArrayInputStreamEnvelope) {
//                        ByteArrayInputStream data = (ByteArrayInputStream)msg.getContents();
//                        
//                        replyData[0] = 0;
//                        Envelope replyEnv = new ByteArrayInputStreamEnvelope(replyData);
//                        try {
//                            msg.getReplyAddress().send(replyEnv);
//                        } catch (AddressClosedException ex) {
//                            System.out.println("Client seems to have gone away. Oh well. " + msg.getToAddress());
//                        }
//                    }
//                }
//            } catch (IOException ex) {
//                // ok, just close server.
//            } finally {
//                // no way to get here:
//                System.out.println("Closing server...");
//                inBox.close();
//            }
//        }
//    }
//}
//
///**
// *===============================================================================
// * Time old inter-isolate mechanism.
// * Time client sending NUM_MESSAGES messages, where server responds with error code.
// */
//class OldMsgTimeTest {
//    
//    /**
//     * Entry point.
//     *
//     * @param args the command like arguments
//     */
//    public static void main(String[] args) throws Exception {
//        Client client =new Client();
//        
//        Server server = new Server();
//        server.start();
//        
//        client.start();
//        client.join();
//        
//        server.join();
//    }
//    
//    /**
//     * The client thread class.
//     */
//    static class Client extends Thread {
//        public void run() {
//            byte[] data = TestMailboxes.msg1.getBytes();
//            
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < TestMailboxes.NUM_MESSAGES; i++) {
//                try {
//                    StreamConnection con = (StreamConnection)Connector.open("msg:///sunstock/is64again");
//                    
//                    InputStream  in  = con.openInputStream();
//                    OutputStream out = con.openOutputStream();
//                    
//                    out.write(data);
//                    out.close();
//                    
//                    int errorcode = in.read();
//                    
//                    in.close();
//                    con.close();
//                } catch (IOException ioe) {
//                    System.err.println("IOException in client "+ioe);
//                }
//            }
//            long time = System.currentTimeMillis() - start;
//            System.err.println("Client sent " + TestMailboxes.NUM_MESSAGES + " messages in " + time + "ms");
//        }
//    }
//    
//    /**
//     * The server thread class.
//     */
//    static class Server extends Thread {
//        
//        public void run() {
//            int connection = 0;
//            byte[] dataBuffer = new byte[TestMailboxes.msg1.getBytes().length];
//            
//            try {
//                StreamConnectionNotifier scn = (StreamConnectionNotifier)Connector.open("msgserver:///sunstock/is64again");
//                while (true) {
//                    connection++;
//                    if (connection > TestMailboxes.NUM_MESSAGES) {
//                        return;
//                    }
//                    StreamConnection con = scn.acceptAndOpen();
//                    InputStream  in  = con.openInputStream();
//                    OutputStream out = con.openOutputStream();
//                    
//                    int i = 0;
//                    while (true) {
//                        int ch = in.read();
//                        if (ch == -1) {
//                            break;
//                        }
//                        dataBuffer[i++] = (byte)ch;
//                    }
//                    in.close();
//                    
//                    out.write(0);
//                    out.close();
//                    con.close();
//                }
//            } catch (IOException ioe) {
//                System.err.println("IOException in server "+ioe);
//            }
//        }
//    }
//}