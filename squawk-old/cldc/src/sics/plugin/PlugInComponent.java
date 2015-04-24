package sics.plugin;

import com.sun.squawk.Field;
import com.sun.squawk.Klass;
import com.sun.squawk.Method;
import com.sun.squawk.NativeUnsafe;
import com.sun.squawk.VM;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;

import java.io.IOException;
import java.util.Hashtable;

import sics.ArrayList;
import sics.messages.ChannelFrameType;
import sics.port.PluginPPort;
import sics.port.PluginPort;
import sics.port.PluginRPort;

// TODO: Auto-generated Javadoc
/**
 * The Class PlugInComponent.
 */
public abstract class PlugInComponent implements Runnable {

	public final static String MAILBOX_NAME = "PirteChannel";
	public Channel clientChannel = null;
	private Hashtable<String, Integer> portInitContext = new Hashtable<String, Integer>();

	public PlugInComponent() {}
	
	public PlugInComponent(String[] args) {
		Client client = new Client();
		client.start();

		if (clientChannel == null) {
			VM.println("Client channel not ready yet, attempting once again...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				VM.println("Interrupted.\r\n");
			}

			if (clientChannel == null) {
				VM.println("CLIENT CHANNEL STILL NULL, PROBLEMS AHEAD...");
			} else {
				VM.println("Client channel initialized after some sleep");
			}
		}

		if (portInitContext == null) 
			VM.println("PortInitContext is NULL!!!");

		for (int i = 0; i < args.length; i = i + 2) {
			portInitContext.put(args[i], Integer.parseInt(args[i + 1]));
		}

		// reflection
//		Klass asKlass = Klass.asKlass(this.getClass());
//
//		int fieldCount = asKlass.getFieldCount(false);
//		VM.println(fieldCount);
//		for (int i = 0; i < fieldCount; i++) {
//			Field field = asKlass.getField(i, false);
//			Klass type = field.getType();
//			String typeName = type.getName();
//			if (typeName.equals("sics.port.PluginPPort")) {
//				PluginPPort pport = (PluginPPort) type.newInstance();
//				String pportName = field.getName();
//				portNames.add(pportName);
//			} else if (typeName.equals("sics.port.PluginRPort")) {
//				PluginRPort rport = (PluginRPort) type.newInstance();
//				String rportName = field.getName();
//				portNames.add(rportName);
//			}
//		}

		// int methodCount = asKlass.getMethodCount(false);
		// for(int i=0;i<methodCount;i++) {
		// Method method = asKlass.getMethod(i, false);
		// String name = method.getName();
		// VM.println(name);
		// if(pports.containsKey(name)) {
		// VM.println("Enter PPort set Method");
		// PluginPPort port = pports.get(name);
		// method.invoke(new Object[]{port});
		// } else if(rports.containsKey(name)) {
		// VM.println("Enter RPort set Method");
		// PluginRPort port = rports.get(name);
		// method.invoke(new Object[]{port});
		// }
		// }

	}

	public abstract void init();
//	public void initPluginPPort(PluginPPort... portArray) {
//		for (int i = 0; i < portArray.length; i++) {
//			String name = portNames.get(i);
//			portArray[i].setName(name);
//			portArray[i].setPlugInComponent(this);
//		}
//	}
//
//	public void initPluginRPort(PluginRPort... portArray) {
//		for (int i = 0; i < portArray.length; i++) {
//			String name = portNames.get(i);
//			portArray[i].setName(name);
//			portArray[i].setPlugInComponent(this);
//		}
//	}

	// public void init() {
	// // reflection
	// Class cls = this.getClass();
	// Klass asKlass = Klass.asKlass(cls);
	// int fieldCount = asKlass.getFieldCount(false);
	//
	// for(int i=0;i<fieldCount;i++) {
	// Field field = asKlass.getField(i, false);
	// String attributeClassStr = field.getClass().toString();
	// VM.print("@@@ Reflection - attribute class type:");
	// VM.print(attributeClassStr);
	// VM.println("@@@");
	// }
	// }

	class Client extends Thread {
		public void run() {
			try {
				clientChannel = Channel.lookup(MAILBOX_NAME);
				while (true) {
					ByteArrayEnvelope replyEnv = (ByteArrayEnvelope) clientChannel
							.receive();
					byte[] replyData = replyEnv.getData();
					switch (replyData[0]) {
					case ChannelFrameType.START_SIGNAL:
						break;
					case ChannelFrameType.STOP_SIGNAL:
						break;
					case ChannelFrameType.INT_VALUE_TRANSMIT:
						break;
					default:
						System.out.println("Error: Wrong channel frame type");
					}
				}

			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public Channel getClientChannel() {
		return clientChannel;
	}

	public int getPortId(String portName) {
		return portInitContext.get(portName);
	}
	
	public void initPortInitContext(String[] portInitContextArray) {
		for (int i = 0; i < portInitContextArray.length; i = i + 2) {
			portInitContext.put(portInitContextArray[i], Integer.parseInt(portInitContextArray[i + 1]));
		}
	}

}
