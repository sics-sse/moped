package network.internal;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import db.DataRecord;
import ecm.Ecm;
import ecm.Loader;
import messages.InstallAckMessage;
import messages.InstallMessage;
import messages.LinkContextEntry;
import messages.LoadMessage;
import messages.Message;
import messages.MessageType;
import messages.PWMMessage;
import messages.PublishMessage;
import messages.RestoreAckMessage;
import messages.RestoreMessage;
import messages.UninstallAckMessage;
import messages.UninstallMessage;

public class SocketEcuManager implements EcuManager {
	private Ecm ecm;
	// ID: Integer, ClientThread: ClientThread
	private HashMap<Integer, DataOutputStream> outsource = new HashMap<Integer, DataOutputStream>();
	private int port;

	public SocketEcuManager(int port) {
		this.port = port;
	}

	public void run() {
		// Implementation of building Server Socket in the Linux
		// try {
		// ServerSocket serverSocket = new ServerSocket(port);
		// while (true) {
		// // waiting for client connect
		// System.out.println("Waiting for new Autosar ECU socket to connect");
		// Socket clientSocket = serverSocket.accept();
		// clientSocket.setKeepAlive(true);
		// System.out.println("Socket connection is built");
		// DataInputStream dis = new DataInputStream(
		// clientSocket.getInputStream());
		// DataOutputStream dos = new DataOutputStream(
		// clientSocket.getOutputStream());
		//
		// Thread newThread = new Thread(
		// new NewDataHandlerThread(dis, dos));
		// newThread.start();
		// System.out.println("New Autosar Socket client connected");
		// }
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		Socket clientSocket;
		try {
			clientSocket = new Socket("192.168.0.8", port);
			DataInputStream dis = new DataInputStream(
					clientSocket.getInputStream());
			DataOutputStream dos = new DataOutputStream(
					clientSocket.getOutputStream());

			Thread newThread = new Thread(new NewDataHandlerThread(dis, dos));
			newThread.start();
			System.out.println("Linux connects to Autosar RPI");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}

	@Override
	public Ecm getEcm() {
		return ecm;
	}

//	@Override
	public void loadPlugins(int ecuId) {
		// Prepare APPs
		HashMap<String, DataRecord> installedApps = ecm.getInstalledApps(ecuId);
		Iterator<Entry<String, DataRecord>> iterator = installedApps.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, DataRecord> entry = iterator.next();
			DataRecord record = entry.getValue();

			int reference = record.getRemoteEcuId();
			String executablePluginName = record.getExecutablePluginName();
			int callbackPortID = record.getCallbackPortID();
			HashMap<String, Integer> portInitialContext = record
					.getPortInitialContext();
			ArrayList<LinkContextEntry> portLinkingContext = record.getPortLinkingContext();
			String location = record.getLocation();
			byte[] pluginBytes;
			try {
				pluginBytes = readBytesFromFile(location);
				LoadMessage loadMessage = new LoadMessage(reference,
						executablePluginName, callbackPortID,
						portInitialContext, portLinkingContext, pluginBytes);

				ecm.process(loadMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void sendMessage(Message message) {
		System.out.println("<<< ecm-core/SocketEcuManager " + message.getMessageType());
		switch (message.getMessageType()) {
		case MessageType.INSTALL:
			InstallMessage installMessage = (InstallMessage) message;
			sendMessage(installMessage);
			break;
		case MessageType.UNINSTALL:
			System.out.println("[SocketEcuManager - sendMessage(Message)]");
			UninstallMessage uninstallMessage = (UninstallMessage) message;
			sendMessage(uninstallMessage);
			break;
		case MessageType.RESTORE:
			RestoreMessage restoreMessage = (RestoreMessage) message;
			sendMessage(restoreMessage);
			break;
		case MessageType.LOAD:
			LoadMessage loadMessage = (LoadMessage) message;
			sendMessage(loadMessage);
			break;
		case MessageType.PWM:
			PWMMessage pwmMessage = (PWMMessage) message;
			sendMessage(pwmMessage);
			break;
		default:
			System.out
					.println("Error: Wrong message type pushed to sending channel");
		}
	}

	private void sendMessage(InstallMessage message) {
		int id = message.getRemoteEcuId();
		String executablePluginName = message.getExecutablePluginName();
		byte[] plugin = message.getBinaryFile();

		int index = 0;
		int pluginNameSize = executablePluginName.length();
		int sizeofBinary = plugin.length;
		System.out.println("File length: " + sizeofBinary);
		
		String portInitContextBuffer = "";
		HashMap<String,Integer> portInitialContext = message.getPortInitialContext();
		Iterator<Entry<String, Integer>> iterator = portInitialContext.entrySet().iterator();
		
		while(iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());
			if(portInitContextBuffer.equals("")) {
				portInitContextBuffer += key;
				portInitContextBuffer += "|";
				portInitContextBuffer += value;
			} else {
				portInitContextBuffer += "|";
				portInitContextBuffer += key;
				portInitContextBuffer += "|";
				portInitContextBuffer += value;
			}
		}
		byte[] portInitContextBufferBytes = portInitContextBuffer.getBytes();
		int portInitContextBytesSize = portInitContextBufferBytes.length;
		
		ArrayList<LinkContextEntry> linkContext = message.getLinkContext();
		int portLinkContextNum = linkContext.size();
		int portLinkContextByteSize =  portLinkContextNum*12;
		
		// 2: $$, 4: message size, 1: Message Type, 4: Size of PlugIn Name(M),
		// M: PlugIn Name(byte array), 4: Size of Byte Array(N), N: Byte Array, 4: Size of PortInitContext(P), P: byte array, 4: PortLinkContextByteSize(Q), Q*3*4: byte array  
		int totalSize = 2 + 4 + 1 + 4 + pluginNameSize + 4 + sizeofBinary + 4 + portInitContextBytesSize + 4 + portLinkContextByteSize;
		byte byteArray[] = new byte[totalSize + 32];

		System.out.println("Plugin Name: " + executablePluginName + ", Total Size: "
				+ totalSize);

		// prepare starting sign $$
		byteArray[index++] = '$';
		byteArray[index++] = '$';

		// prepare size of message
		byteArray[index++] = (byte) (totalSize >> 24);
		byteArray[index++] = (byte) ((totalSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((totalSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (totalSize & 0xFF);

		// prepare Message Type
		byteArray[index++] = MessageType.INSTALL;

		// prepare plugin name size
		byteArray[index++] = (byte) (pluginNameSize >> 24);
		byteArray[index++] = (byte) ((pluginNameSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((pluginNameSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (pluginNameSize & 0xFF);

		// prepare plugin name
		for (int i = 0; i < pluginNameSize; i++) {
			byteArray[index++] = (byte) executablePluginName.charAt(i);
		}
		// copy binary size
		byteArray[index++] = (byte) (sizeofBinary >> 24);
		byteArray[index++] = (byte) ((sizeofBinary >> 16) & 0xFF);
		byteArray[index++] = (byte) ((sizeofBinary >> 8) & 0xFF);
		byteArray[index++] = (byte) (sizeofBinary & 0xFF);

		// prepare binary
		for (int j = 0; j < sizeofBinary; j++) {
			byteArray[index++] = plugin[j];
		}
		
		// prepare PortInitContext
		byteArray[index++] = (byte) (portInitContextBytesSize >> 24);
		byteArray[index++] = (byte) ((portInitContextBytesSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((portInitContextBytesSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (portInitContextBytesSize & 0xFF);
		for (int k=0;k<portInitContextBytesSize; k++) {
			byteArray[index++] = portInitContextBufferBytes[k];
		}
		
		// prepare PortLinkContext
		byteArray[index++] = (byte) (portLinkContextByteSize >> 24);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (portLinkContextByteSize & 0xFF);
		for(LinkContextEntry linkEntry:linkContext) {
			int fromPortId = linkEntry.getFromPortId();
			byte[] fromPortIdBytes = intToByteArray(fromPortId);
			for(int i=0;i<4;i++) {
				byteArray[index++] = fromPortIdBytes[i];
			}
			int toPortId = linkEntry.getToPortId();
			byte[] toPortIdBytes = intToByteArray(toPortId);
			for(int j=0;j<4;j++) {
				byteArray[index++] = toPortIdBytes[j];
			}
			int remotePortId = linkEntry.getRemotePortId();
			byte[] remotePortIdBytes = intToByteArray(remotePortId);
			for(int k=0;k<4;k++) {
				byteArray[index++] = remotePortIdBytes[k];
			}
		}
		
//		System.out.println("------------------");
		try {
			DataOutputStream dos = outsource.get(id);

			int count = totalSize / 30 + 1;
			for (int i = 0; i < count; i++) {
				byte sdbuf[] = new byte[31];
				sdbuf[0] = '2';
				for (int j = 0; j < 30; j++) {
					sdbuf[j + 1] = byteArray[i * 30 + j];
				}
				dos.write(sdbuf);
//				System.out.println(Arrays.toString(sdbuf));
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			System.out.println("------------------");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendMessage(UninstallMessage message) {
		System.out.println("SocketEcuManger-sendMessage(UninstallMessage)");
		int id = message.getRemoteEcuId();
		System.out.println("SocketEcuManager-id:" + id);
		String pluginName = message.getPluginName();
		System.out.println("plugin name:" + pluginName);
		byte[] pluginNameBytes = pluginName.getBytes();
		int pluginNameSize = pluginName.length();
		// 1: package type, 2: $$, 4: totalSize, 1: message type, 4: plugin name
		// size, M: size of plugin name bytes
		int totalSize = 2 + 4 + 1 + 4 + pluginNameSize;
		int index = 0;
		byte[] messageBytes = new byte[31];
		messageBytes[index++] = '2';
		messageBytes[index++] = '$';
		messageBytes[index++] = '$';
		messageBytes[index++] = (byte) (totalSize >> 24);
		messageBytes[index++] = (byte) ((totalSize >> 16) & 0xFF);
		messageBytes[index++] = (byte) ((totalSize >> 8) & 0xFF);
		messageBytes[index++] = (byte) (totalSize & 0xFF);
		messageBytes[index++] = MessageType.UNINSTALL;
		messageBytes[index++] = (byte) (pluginNameSize >> 24);
		messageBytes[index++] = (byte) ((pluginNameSize >> 16) & 0xFF);
		messageBytes[index++] = (byte) ((pluginNameSize >> 8) & 0xFF);
		messageBytes[index++] = (byte) (pluginNameSize & 0xFF);
		System.out.println("Plugin name size:" + pluginNameSize);
		for (int m = 0; m < pluginNameSize; m++) {
			messageBytes[index++] = pluginNameBytes[m];
		}
		try {
			DataOutputStream dos = outsource.get(id);
			System.out.println("------------------");
			System.out.println("Send uninstall message");
			System.out.println(Arrays.toString(messageBytes));
			System.out.println("------------------");
			dos.write(messageBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendMessage(RestoreMessage message) {

	}

	private void sendMessage(LoadMessage message) {
		int id = message.getRemoteEcuId();
		String executablePluginName = message.getExecutablePluginName();
		byte[] plugin = message.getBinaryFile();

		int index = 0;
		int pluginNameSize = executablePluginName.length();
		int sizeofBinary = plugin.length;
		System.out.println("File length: " + sizeofBinary);
		
		String portInitContextBuffer = "";
		HashMap<String,Integer> portInitialContext = message.getPortInitialContext();
		Iterator<Entry<String, Integer>> iterator = portInitialContext.entrySet().iterator();
		
		while(iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());
			if(portInitContextBuffer.equals("")) {
				portInitContextBuffer += key;
				portInitContextBuffer += "|";
				portInitContextBuffer += value;
			} else {
				portInitContextBuffer += "|";
				portInitContextBuffer += key;
				portInitContextBuffer += "|";
				portInitContextBuffer += value;
			}
		}
		byte[] portInitContextBufferBytes = portInitContextBuffer.getBytes();
		int portInitContextBytesSize = portInitContextBufferBytes.length;
		
		ArrayList<LinkContextEntry> linkContext = message.getLinkContext();
		int portLinkContextNum = linkContext.size();
		int portLinkContextByteSize =  portLinkContextNum*12;
		
		// 2: $$, 4: message size, 1: Message Type, 4: Size of PlugIn Name(M),
		// M: PlugIn Name(byte array), 4: Size of Byte Array(N), N: Byte Array, 4: Size of PortInitContext(P), P: byte array, 4: PortLinkContextByteSize(Q), Q*3*4: byte array  
		int totalSize = 2 + 4 + 1 + 4 + pluginNameSize + 4 + sizeofBinary + 4 + portInitContextBytesSize + 4 + portLinkContextByteSize;
		byte byteArray[] = new byte[totalSize + 32];

		System.out.println("Plugin Name: " + executablePluginName + ", Total Size: "
				+ totalSize);

		// prepare starting sign $$
		byteArray[index++] = '$';
		byteArray[index++] = '$';

		// prepare size of message
		byteArray[index++] = (byte) (totalSize >> 24);
		byteArray[index++] = (byte) ((totalSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((totalSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (totalSize & 0xFF);

		// prepare Message Type
		byteArray[index++] = MessageType.LOAD;

		// prepare plugin name size
		byteArray[index++] = (byte) (pluginNameSize >> 24);
		byteArray[index++] = (byte) ((pluginNameSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((pluginNameSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (pluginNameSize & 0xFF);

		// prepare plugin name
		for (int i = 0; i < pluginNameSize; i++) {
			byteArray[index++] = (byte) executablePluginName.charAt(i);
		}
		// copy binary size
		byteArray[index++] = (byte) (sizeofBinary >> 24);
		byteArray[index++] = (byte) ((sizeofBinary >> 16) & 0xFF);
		byteArray[index++] = (byte) ((sizeofBinary >> 8) & 0xFF);
		byteArray[index++] = (byte) (sizeofBinary & 0xFF);

		// prepare binary
		for (int j = 0; j < sizeofBinary; j++) {
			byteArray[index++] = plugin[j];
		}
		
		// prepare PortInitContext
		byteArray[index++] = (byte) (portInitContextBytesSize >> 24);
		byteArray[index++] = (byte) ((portInitContextBytesSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((portInitContextBytesSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (portInitContextBytesSize & 0xFF);
		for (int k=0;k<portInitContextBytesSize; k++) {
			byteArray[index++] = portInitContextBufferBytes[k];
		}
		
		// prepare PortLinkContext
		byteArray[index++] = (byte) (portLinkContextByteSize >> 24);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (portLinkContextByteSize & 0xFF);
		for(LinkContextEntry linkEntry:linkContext) {
			int fromPortId = linkEntry.getFromPortId();
			byte[] fromPortIdBytes = intToByteArray(fromPortId);
			for(int i=0;i<4;i++) {
				byteArray[index++] = fromPortIdBytes[i];
			}
			int toPortId = linkEntry.getToPortId();
			byte[] toPortIdBytes = intToByteArray(toPortId);
			for(int j=0;j<4;j++) {
				byteArray[index++] = toPortIdBytes[j];
			}
			int remotePortId = linkEntry.getRemotePortId();
			byte[] remotePortIdBytes = intToByteArray(remotePortId);
			for(int k=0;k<4;k++) {
				byteArray[index++] = remotePortIdBytes[k];
			}
		}
		
//		System.out.println("------------------");
		try {
			DataOutputStream dos = outsource.get(id);

			int count = totalSize / 30 + 1;
			for (int i = 0; i < count; i++) {
				byte sdbuf[] = new byte[31];
				sdbuf[0] = '2';
				for (int j = 0; j < 30; j++) {
					sdbuf[j + 1] = byteArray[i * 30 + j];
				}
				dos.write(sdbuf);
//				System.out.println(Arrays.toString(sdbuf));
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			System.out.println("------------------");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendMessage(PWMMessage message) {
		int id = message.getRemoteEcuId();
		DataOutputStream dos = outsource.get(id);
		byte[] dataBytes = message.getData();

		byte sdbuf[] = new byte[31];

		// set package type id: 1
		sdbuf[0] = '1';
		for (int i = 0; i < dataBytes.length; i++) {
			sdbuf[i + 1] = dataBytes[i];
		}

		try {
			dos.write(sdbuf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public void sendToVCU(String str, int portid) {
	System.out.println("sendToVCU NYI");
    }

	private class NewDataHandlerThread implements Runnable {

		private DataInputStream in;
		private DataOutputStream out;

		public NewDataHandlerThread(DataInputStream in, DataOutputStream out) {
			this.in = in;
			this.out = out;
		}

		public void run() {
		    // Turned into just dummy code, because SocketEcuManager
		    // is not used, and so we can't keep it up to date with
		    // the rest.
		}

		public void registerEcu(int id) {
			outsource.put(id, out);
		}

		private Message getMessage(int size, byte[] data) {
			int pluginNameSize;
			String pluginName;
			byte[] pluginNameBytes;

			byte messageType = data[0];
		System.out.println(">>> ecm-core/SocketEcuManager " + messageType);
			switch (messageType) {
			case MessageType.INSTALL_ACK:
				pluginNameSize = 0;
				pluginNameSize = (data[1] << 24) | (data[2] << 16)
						| (data[3] << 8) | data[4];
				pluginNameBytes = new byte[pluginNameSize];
				for (int i = 0; i < pluginNameSize; i++) {
					pluginNameBytes[i] = data[i + 5];
				}
				pluginName = new String(pluginNameBytes);
				InstallAckMessage installAckMessage = new InstallAckMessage((byte)1,
						pluginName);
				return installAckMessage;
			case MessageType.UNINSTALL_ACK:
				byte[] tempSize4Uninstall = new byte[4];
				tempSize4Uninstall[0] = data[1];
				tempSize4Uninstall[1] = data[2];
				tempSize4Uninstall[2] = data[3];
				tempSize4Uninstall[3] = data[4];
				int pluginNameSize4Uninstall = byteArrayToInt(tempSize4Uninstall);

				byte[] pluginNameBytes4Uninstall = new byte[pluginNameSize4Uninstall];
				for (int k = 0; k < pluginNameSize4Uninstall; k++) {
					pluginNameBytes4Uninstall[k] = data[k + 5];
				}
				pluginName = new String(pluginNameBytes4Uninstall);
				UninstallAckMessage uninstallAckMessage = new UninstallAckMessage(
						pluginName);
				return uninstallAckMessage;
			case MessageType.RESTORE_ACK:
				pluginNameSize = 0;
				pluginNameSize = (data[1] << 24) | (data[2] << 16)
						| (data[3] << 8) | data[4];
				pluginNameBytes = new byte[pluginNameSize];
				pluginName = new String(pluginNameBytes);
				RestoreAckMessage restoreAckMessage = new RestoreAckMessage(
						pluginName);
				return restoreAckMessage;
			case MessageType.PLUGIN_MESSAGE:
				break;
			case MessageType.PUBLISH:
				int index = 1;
				byte[] tempSize = new byte[4];
				tempSize[0] = data[index++];
				tempSize[1] = data[index++];
				tempSize[2] = data[index++];
				tempSize[3] = data[index++];
				int keySize = byteArrayToInt(tempSize);
				byte[] keyBytes = new byte[keySize];
				for (int i = 0; i < keySize; i++) {
					keyBytes[i] = data[index++];
				}
				String keyName = new String(keyBytes);

				byte[] valueSizeBytes = new byte[4];
				valueSizeBytes[0] = data[index++];
				valueSizeBytes[1] = data[index++];
				valueSizeBytes[2] = data[index++];
				valueSizeBytes[3] = data[index++];
				int valueSize = byteArrayToInt(valueSizeBytes);
				byte[] valueBytes = new byte[valueSize];
				for (int j = 0; j < valueSize; j++) {
					valueBytes[j] = data[index++];
				}
				System.out.println("value size:" + valueSize);
				String value = new String(valueBytes);

				System.out.println("[Publish in SocketEcuManager - key:"
						+ keyName + ", value:" + value + "]");
				PublishMessage publishMessage = new PublishMessage(keyName,
						value);
				return publishMessage;
			default:
				System.out.println("Error: Wrong message type");
			}
			return null;
		}
	}

	private byte[] readBytesFromFile(String location) throws IOException {
		File file = new File(location);
		InputStream is = new FileInputStream(file);
		// Get the size of the file
		long length = file.length();
		// You cannot create an array using a long type.
		// It needs to be an integer type.
		// Before converting to an integer type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName() + " as it is too long (" + length
					+ " bytes, max supported " + Integer.MAX_VALUE + ")");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	private int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}

	private byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i /* >> 0 */);
		return result;
	}
}
