/*
 * TestChannels.java
 *
 * Created on March 3, 2006, 1:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package tests;

import com.sun.squawk.*;
import com.sun.squawk.io.mailboxes.*;

import java.io.*;

/**
 *
 * @author dw29446
 */
public class TestChannels {
    public static final String msg1 = "0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
    public final static int NUM_MESSAGES = 1000;
    
    public final static String MAILBOX_NAME = "NewChannelTimeTest";
    
    public static void sleep(long ms) {
        try {
            System.out.println("[sleep " + ms + "]");
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        
        System.gc();
        System.gc();
        
        System.out.println("Time test of new IIC (3 runs):");
        try {
            long free1 = runtime.freeMemory();
            NewChannelTimeTest.main(new String[0]);
            long free2 = runtime.freeMemory();
            NewChannelTimeTest.main(new String[0]);
            NewChannelTimeTest.main(new String[0]);
            System.out.println("at least " + (free1 - free2) + " bytes used in one run");
            
            // try in isolate
            System.out.println("\nTime test of new IIC, in seperate Isolates:");
            String[] isoargs = {};
            Isolate curIso = Isolate.currentIsolate();
            Isolate isoServer = new Isolate("tests.ServerIsolate", isoargs, curIso.getClassPath(), curIso.getParentSuiteSourceURI());
            isoServer.start();
            
            sleep(1000);
            
            Isolate isoClient = new Isolate("tests.ClientIsolate", isoargs, curIso.getClassPath(), curIso.getParentSuiteSourceURI());
            isoClient.start();
            
            isoServer.join();
            isoClient.join();
            
            
             // try in isolate, with hibernation
            System.out.println("\ntry in isolate, with hibernation:");
            String[] hiberargs = {"-hibernate"};
            
            isoServer = new Isolate("tests.ServerIsolate", hiberargs, curIso.getClassPath(), curIso.getParentSuiteSourceURI());
            isoServer.start();
            
            sleep(1000);
            System.err.println("Hibernating isolate...");
            isoServer.hibernate();
            System.err.println("Done Hibernating isolate...");
            
            sleep(5000);
            System.err.println("Un-Hibernating isolate...");
            isoServer.unhibernate();
            System.err.println("Done Un-Hibernating isolate...");
            sleep(5000);
            
            isoClient = new Isolate("tests.ClientIsolate", hiberargs, curIso.getClassPath(), curIso.getParentSuiteSourceURI());
            isoClient.start();
            
            isoServer.join();
            isoClient.join();
            
            // try in isolate, with hibernation in the middle
            System.out.println("\ntry in isolate, with hibernation:");
            String[] hiberargs2 = {"-hibernate2"};
            
            isoServer = new Isolate("tests.ServerIsolate", hiberargs2, curIso.getClassPath(), curIso.getParentSuiteSourceURI());
            isoServer.start();
            sleep(500);
            
            isoClient = new Isolate("tests.ClientIsolate", hiberargs2, curIso.getClassPath(), curIso.getParentSuiteSourceURI());
            isoClient.start();
            
            sleep(1000);
            System.err.println("Hibernating isolate...");
            isoServer.hibernate();
            System.err.println("Done Hibernating isolate...");
            
            sleep(5000);
            System.err.println("Un-Hibernating isolate...");
            isoServer.unhibernate();
            System.err.println("Done Un-Hibernating isolate...");
            sleep(5000);
            
            isoServer.join();
            isoClient.join();
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
       /* System.out.println("\n cleanup GC:");
        System.gc();
        System.gc();
        System.out.println("\nTime test of old IIC (3 runs):");
        try {
            long free1 = runtime.freeMemory();
            OldMsgTimeTest.main(new String[0]);
            long free2 = runtime.freeMemory();
            OldMsgTimeTest.main(new String[0]);
            OldMsgTimeTest.main(new String[0]);
            System.out.println("at least " + (free1 - free2) + " bytes used in one run");
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
        
        
        System.exit(0);
    }
    
    /**
     * The client thread class.
     */
    static class Client extends Thread {

        public void run() {
            try {
                byte[] data = TestChannels.msg1.getBytes();
                
                long start = System.currentTimeMillis();
                Channel testChan = Channel.lookup(MAILBOX_NAME);
                
                for (int i = 0; i < TestChannels.NUM_MESSAGES; i++) {
                    Envelope cmdEnv = new ByteArrayEnvelope(data);
                    testChan.send(cmdEnv);
                    ByteArrayEnvelope replyEnv = (ByteArrayEnvelope)testChan.receive();
                    byte[] replyData = replyEnv.getData();
                    if (replyData.length != 1 || (replyData[0] != 0)) {
                        System.err.println("Reply not OK");
                    }
                }
                long time = System.currentTimeMillis() - start;
                
                System.err.println("Client sent " + TestChannels.NUM_MESSAGES + " messages of " + data.length + " bytes in " + time + "ms");
                testChan.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * The server thread class.
     */
    static class Server extends Thread {
        public boolean pauseAfterCreate = false;
 
                public boolean pauseAfter10 = false;
                
        public void run() {
            byte[] replyData = new byte[1];
            ServerChannel serverChannel = null;
            Channel aChannel = null;
            try {
                serverChannel = ServerChannel.create(MAILBOX_NAME);
            } catch (MailboxInUseException ex) {
                throw new RuntimeException(ex.toString());
            }
            
            if (pauseAfterCreate) {
                System.err.println("Pauseing server...");
                TestChannels.sleep(3000);
                System.err.println("Done pauseing server...");
            }
            
            try {
                aChannel = null;
                do {
                    try {
                        aChannel = serverChannel.accept();
                    } catch (MailboxClosedException ex) {
                        System.err.println("Caught MailboxClosedException while in accept(). try to re-open");
                        TestChannels.sleep(100);
                        
                        // try again.
                        try {
                            serverChannel.reOpen();
                        } catch (MailboxInUseException ex2) {
                            ex2.printStackTrace();
                        }
                        System.err.println("ServerChannel.reOpen() succeeded");
                    }
                } while (aChannel == null);

                // handle messages:
                int i = 0;
                while (true) {
                    Envelope msg;
                    try {
                        msg = aChannel.receive();
                    } catch (MailboxClosedException e) {
                        //System.out.println("caught " + e);
                        //System.out.println("Server seems to have gone away. Oh well.");
                        break;
                    } catch (AddressClosedException e) {
                        //System.out.println("caught " + e);
                        //System.out.println("Server seems to have gone away. Oh well.");
                        break;
                    }
                    i++;
                    
                    if (msg instanceof ByteArrayEnvelope) {
                        ByteArrayEnvelope dataEnv = (ByteArrayEnvelope)msg;
                        byte[] data = dataEnv.getData();
                        
                        if (pauseAfter10 && i == 10) {
                            System.err.println("Pauseing server in middle of message...");
                            TestChannels.sleep(3000);
                            System.err.println("Done pauseing in middle of message...");
                        }
                        
                        replyData[0] = 0;
                        Envelope replyEnv = new ByteArrayEnvelope(replyData);
                        try {
                            aChannel.send(replyEnv);
                        } catch (AddressClosedException e) {
                            System.out.println("caught " + e);
                            System.out.println("Client seems to have gone away. Oh well.");
                        }
                    }
                }
            } finally {
                // no way to get here:
                System.out.println("Closing server...");
                aChannel.close();
                serverChannel.close();
            }
        }
    }
    
    static class IDEnvelope extends Envelope {
        private String value;
        
        public IDEnvelope(String value) {
            this.value = new String(value);
        }
        
        public Object getContents() {
            return value;
        }
    }// IDEnvelope
    
}


/**
 *===============================================================================
 * Time new inter-isolate mechanism.
 * Time client sending NUM_MESSAGES messages, where server responds with error code.
 */
class NewChannelTimeTest {
    

    public static void main(String[] args) throws Exception {
        TestChannels.Client client = new TestChannels.Client();
        
        TestChannels.Server server = new TestChannels.Server();
        server.start();
        
        client.start();
        client.join();
        
        server.join();
    }
    
}

class ServerIsolate {

    public static void main(String[] args) throws Exception {
        boolean testHibernate = false;
        boolean testHibernate2 = false;
        
        if (args.length > 0) {
            if (args[0].equals("-hibernate")) {
                testHibernate = true;
            } else if (args[0].equals("-hibernate2")) {
                testHibernate2 = true;
            }
        }
        TestChannels.Server server = new TestChannels.Server();
        if (testHibernate) {
            server.pauseAfterCreate = true;
            
        } else if (testHibernate2) {
            server.pauseAfter10 = true;
        }
        
        server.start();
        server.join();
    }
}

class ClientIsolate {

    public static void main(String[] args) throws Exception {
        TestChannels.Client client = new TestChannels.Client();
        client.start();
        client.join();
    }
    
}

class IsolateTestRunner {

	public static final String CHANNEL_NAME = "INTER_ISOLATE_TEST_RESULTS";

	public void run(String[] classesToRun) throws MailboxInUseException, AddressClosedException, MailboxClosedException, Exception {
		ServerChannel serverChannel = ServerChannel.create(CHANNEL_NAME);
		String uri = VM.getCurrentIsolate().getParentSuiteSourceURI();
		Isolate[] isolate = new Isolate[classesToRun.length];
		
		for (int i = 0; i < classesToRun.length; i++) {
			isolate[i] = new Isolate("tests.ChildIsolateTestHelper", new String[] {""+i}, null, uri);
			isolate[i].start();
		}

		for (int i = 0; i < isolate.length; i++) {
			Channel aChannel = serverChannel.accept();
			TestChannels.IDEnvelope msg = (TestChannels.IDEnvelope)aChannel.receive();
			System.out.println("Received: " + msg.getContents());
			aChannel.close();
		}
		
		for (int i = 0; i < isolate.length; i++) {
			isolate[i].join();
			if (isolate[i].getExitCode() != 0)
				throw new Exception("exception in " + classesToRun[i] + " isolate");			
		}
		
        serverChannel.close();
	}
}

class ChildIsolateTestRunner {

	public static final String CHANNEL_NAME = "INTER_ISOLATE_TEST_RESULTS";

	public void run(String[] classesToRun) throws MailboxInUseException, AddressClosedException, MailboxClosedException, Exception {
		ServerChannel serverChannel = ServerChannel.create(CHANNEL_NAME);
		String uri = VM.getCurrentIsolate().getParentSuiteSourceURI();
		Isolate[] isolate = new Isolate[classesToRun.length];
		
		for (int i = 0; i < classesToRun.length; i++) {
			isolate[i] = new Isolate("tests.ChildIsolateTestHelper", new String[] {""+i}, null, uri);
			isolate[i].start();
		}

		for (int i = 0; i < isolate.length; i++) {
			Channel aChannel = serverChannel.accept();
			TestChannels.IDEnvelope msg = (TestChannels.IDEnvelope)aChannel.receive();
			System.out.println("Received: " + msg.getContents());
			aChannel.close();
		}
		
		for (int i = 0; i < isolate.length; i++) {
			isolate[i].join();
			if (isolate[i].getExitCode() != 0)
				throw new Exception("exception in " + classesToRun[i] + " isolate");			
		}
		
        serverChannel.close();
	}
}

class ChildIsolateTestHelper {
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
 
       TestChannels.sleep(5);
        
		Channel aChannel = null;
		try {

			aChannel = Channel.lookup(ChildIsolateTestRunner.CHANNEL_NAME);
System.err.println("isolate # " + args[0] + " opened " + aChannel);
			Thread.yield();
			aChannel.send(new TestChannels.IDEnvelope(args[0]));
		} catch (NoSuchMailboxException e) {
			e.printStackTrace();
		} catch (AddressClosedException e) {
			e.printStackTrace();
		} finally {
			if (aChannel != null) {
                System.err.println("isolate # " + args[0] + "is closing channel");
				aChannel.close();
			}
		}
	
	}
}

class MultiIsolateTest {

	public static void main(String[] args) throws Exception {
		System.out.println("Testing child isolates can communicate with remote slave");
		
		String[] classesToRun = new String[] {
				"tests.ChildIsolateTest",
				"tests.ChildIsolateTest",
                "tests.ChildIsolateTest",
                "tests.ChildIsolateTest",
                "tests.ChildIsolateTest",
                "tests.ChildIsolateTest",
                "tests.ChildIsolateTest",
                "tests.ChildIsolateTest",
				"tests.ChildIsolateTest"
		};
		ChildIsolateTestRunner childIsolateTestRunner = new ChildIsolateTestRunner();
		childIsolateTestRunner.run(classesToRun);
	}
}
