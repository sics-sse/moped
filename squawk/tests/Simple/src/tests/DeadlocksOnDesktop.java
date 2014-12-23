package tests;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.squawk.Isolate;
import com.sun.squawk.VM;
import com.sun.squawk.io.mailboxes.AddressClosedException;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.MailboxClosedException;
import com.sun.squawk.io.mailboxes.MailboxInUseException;
import com.sun.squawk.io.mailboxes.NoSuchMailboxException;
import com.sun.squawk.io.mailboxes.ServerChannel;

public class DeadlocksOnDesktop extends MIDlet {
	public static final String CHANNEL_NAME = "CHANNEL_NAME";

	public static void main(String[] args) throws MIDletStateChangeException {
		new DeadlocksOnDesktop().startApp();
	}

	public static class ChildServer {
		public static void main(String[] args) throws InterruptedException, NoSuchMailboxException, AddressClosedException {
            Isolate.currentIsolate().addLifecycleListener(new Isolate.LifecycleListener() {
                public void handleLifecycleListenerEvent(Isolate iso, int event) {
                    System.out.println("Exiting " + iso);
                    System.out.flush();
                }
            },
                    Isolate.SHUTDOWN_EVENT_MASK);
            
			Thread.sleep(1000); // Time for master to do serverChannel.accept()
            
			final Channel channel = Channel.lookup(CHANNEL_NAME);
            
			Thread thread = new Thread() {
				public void run() {
					try {
						channel.receive();
					} catch (AddressClosedException e) {
						e.printStackTrace();
					} catch (MailboxClosedException e) {
						e.printStackTrace();
					}
				}
			};
            
			VM.setAsDaemonThread(thread);
			thread.start();
			
			Thread.sleep(1000);
			System.out.println("child is running");
		}
	}

	protected void startApp() throws MIDletStateChangeException {
        Isolate.currentIsolate().addLifecycleListener(new Isolate.LifecycleListener() {
                public void handleLifecycleListenerEvent(Isolate iso, int event) {
                    System.out.println("Exiting " + iso);
                    System.out.flush();
                }
            },
                    Isolate.SHUTDOWN_EVENT_MASK);
            
		try {
			ServerChannel serverChannel = ServerChannel.create(CHANNEL_NAME);
			Isolate isolate = new Isolate(ChildServer.class.getName(), new String[] {}, Isolate.currentIsolate().getClassPath(), Isolate.currentIsolate()
					.getParentSuiteSourceURI());
			isolate.start();
            try {
                Channel privateChannel = serverChannel.accept();
            } catch (MailboxClosedException ex) {
                ex.printStackTrace();
            }
			isolate.join();
			System.out.println("Isolate name: " + isolate);
		} catch (MailboxInUseException e) {
			e.printStackTrace();
		}
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}
}

