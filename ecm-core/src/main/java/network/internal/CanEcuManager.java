package network.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import messages.InstallAckMessage;
import messages.InstallMessage;
import messages.LinkContextEntry;
import messages.LoadMessage;
import messages.Message;
import messages.MessageType;
import messages.PWMMessage;
import messages.PluginMessage;
import messages.PublishMessage;
import messages.RestoreMessage;
import messages.UninstallAckMessage;
import messages.UninstallMessage;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

import ecm.Ecm;

public class CanEcuManager implements EcuManager {
	private Ecm ecm;

	private int channelNumber;
	private JavaCanLibrary javaCanLibrary;
	private HashMap<String, Integer> senders = new HashMap<String, Integer>();
	private ArrayList<ICanReceiver> canReceivers = new ArrayList<ICanReceiver>();

	public CanEcuManager(HashMap<String, Integer> senders,
			HashMap<Integer, String> receivers) {
		this.senders = senders;

		Iterator<Entry<Integer, String>> iter = receivers.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, String> entry = iter.next();
			int id = entry.getKey();
			String function = entry.getValue();
			if (function.equals("cantp")) {
				ICanReceiver r = new CanTPReceiver(id);
				canReceivers.add(r);
			} else if (function.equals("can")) {
				ICanReceiver r = new ClassicalCanReceiver(id);
				canReceivers.add(r);
			} else {
				System.out
						.println("Error: there is wrong function name for receivers");
				System.exit(-1);
			}
		}

		javaCanLibrary = (JavaCanLibrary) Native.loadLibrary("javaCanLib",
				JavaCanLibrary.class);
		channelNumber = javaCanLibrary.init_can();
//		System.out.println("cn:" + channelNumber);

	}

	@Override
	public void run() {
		while (true) {
			for (ICanReceiver receiver : canReceivers) {
				byte[] data = receiver.receive();
				if(data != null)
				    try {
					byte[] parsedData = parseByteData(data[0]);
					switch (parsedData[0]) {
					case MessageType.INSTALL_ACK:

					    //System.out.println(" plugin id " + parsedData[1]);
						if(ecm.hasPluginInTmpDB(parsedData[1])) {
		System.out.println(">>> ecm-core/CanEcuManager " + parsedData[0]);
							String pluginName = ecm
									.getPluginNameFromTmpDB(parsedData[1]);
							InstallAckMessage installAckMessage = new InstallAckMessage(
									parsedData[1], pluginName);
							ecm.process(installAckMessage);
						} else {
						    //							System.out.println("There is no corresponding Plugin ID " + parsedData[1] + " in temporary DB");
						}
						break;
					case MessageType.UNINSTALL_ACK:
						byte pluginId4Uninstall = parsedData[1];
						if (pluginId4Uninstall == 51 && !ecm.hasPluginInUninstallCache(pluginId4Uninstall)) {
						    // special case for a raw message sent from VCU giving speed - Ecm shouldn't really see it.
						    break;
						}
		System.out.println(">>> ecm-core/CanEcuManager " + parsedData[0]);
						if(ecm.hasPluginInUninstallCache(pluginId4Uninstall)) {
							String pluginName = ecm.getPluginNameFromUninstallCache(pluginId4Uninstall);
							UninstallAckMessage uninstallAckMessage = new UninstallAckMessage(pluginName);
							ecm.process(uninstallAckMessage);
						} else {
							System.out.println("There is no corresponding Plugin ID " + parsedData[1] + " in uninstall cache");
						}
						break;
					case MessageType.PUBLISH:
		System.out.println(">>> ecm-core/CanEcuManager " + parsedData[0]);
					    System.out.println("data length = " + data.length);
						int index = 1;
						byte[] buffer = new byte[4];
						for(int i=0;i<4;i++) {
							buffer[i] = data[index++];
						}
						int keySize = byteArrayToInt(buffer);
						System.out.println("keysize " + keySize);
						byte[] keyBytes = new byte[keySize];
						for(int k=0;k<keySize;k++) {
							keyBytes[k] = data[index++];
						}
						String keyStr = new String(keyBytes);
						
						System.out.println("keystr " + keyStr);

						try {
						    for(int i=0;i<4;i++) {
							System.out.println(" " + i + " " + data[index]);
							buffer[i] = data[index++];
						    }
						} catch (Exception e) {
						    continue;
						}
						int valueSize = byteArrayToInt(buffer);
						System.out.println("valueSize " + valueSize);

						if (valueSize > 512)
						    break;
						byte[] valueBytes = new byte[valueSize];
						for(int v=0;v<valueSize;v++) {
							valueBytes[v] = data[index++];
						}
						String valueStr = new String(valueBytes);
						
						PublishMessage publishMessage = new PublishMessage(keyStr, valueStr);
						ecm.process(publishMessage);
						break;
					default:
		System.out.println(">>> ecm-core/CanEcuManager " + parsedData[0]);
						System.out
								.println("Error: wrong message type from autosar");
					}
				    } catch (Exception e) {
					e.printStackTrace();
				    }
				
			}

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	interface ICanReceiver {
		public byte[] receive();
	}

	class ClassicalCanReceiver implements ICanReceiver {
		private int canId;

		public ClassicalCanReceiver(int canId) {
			this.canId = canId;
		}

		@Override
		public byte[] receive() {
			Pointer p = javaCanLibrary.receiveByteData(channelNumber, canId);
			if(p != null) {
				byte len = p.getByte(0);
				if(len <= 0) {
					System.out.println("It should not receive value less than 0 in normal can channel");
					return null;
				}
				byte[] res = new byte[len];
				for (int i = 0; i < len; i++) {
					res[i] = p.getByte(i + 1);
				}
				return res;
			} else
				return null;
			
		}
	}

	class CanTPReceiver implements ICanReceiver {
		private int canId;

		public CanTPReceiver(int canId) {
			this.canId = canId;
		}

		@Override
		public byte[] receive() {
			byte[] res = null;
			Pointer p = javaCanLibrary.receiveData(channelNumber, canId);
			int len = javaCanLibrary.getPackageSize();
			if (len > 0) {
			    if (p == null) {
				//				System.out.println("CAN receive null pointer");
				return null;
			    }
			    res = new byte[len];
			    for (int i = 0; i < len; i++) {
				res[i] = p.getByte(i);
			    }
			    javaCanLibrary.resetPackageSize();
			    return res;
			}
			return null;
		}

	}

	@Override
	public void sendMessage(Message message) {
		System.out.println("<<< ecm-core/CanEcuManager " + message.getMessageType());
		switch (message.getMessageType()) {
		case MessageType.INSTALL:
			System.out.println("[CanEcuManager - sendInstallMessage(Message)]");
			InstallMessage installMessage = (InstallMessage) message;
			sendMessage(installMessage);
			break;
		case MessageType.UNINSTALL:
			System.out
					.println("[CanEcuManager - sendUninstallMessage(Message)]");
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
		case MessageType.PLUGIN_MESSAGE:
			PluginMessage pluginMessage = (PluginMessage) message;
			sendMessage(pluginMessage);
			break;
		default:
			System.out
					.println("Error: Wrong message type pushed to sending channel");
		}
	}

	private void sendMessage(LoadMessage message) {
		int id = message.getRemoteEcuId();
		String executablePluginName = message.getExecutablePluginName();
		byte[] plugin = message.getBinaryFile();
		
		System.out.print("PLUGIN BYTES:");
		for (int i = 0; i < plugin.length; i++) {
			System.out.print(" " + (plugin[i] & 0xFF));
		}
		System.out.println("");

		int index = 0;
		int pluginNameSize = executablePluginName.length();
		int sizeofBinary = plugin.length;
//		System.out.println("File length: " + sizeofBinary);

		String portInitContextBuffer = "";
		HashMap<String, Integer> portInitialContext = message
				.getPortInitialContext();
		Iterator<Entry<String, Integer>> iterator = portInitialContext
				.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());
			if (portInitContextBuffer.equals("")) {
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
		int portLinkContextByteSize = portLinkContextNum * 12;

		// 1: Message Type, 1: plugin ID, 4: Size of PlugIn Name(M),
		// M: PlugIn Name(byte array), 4: Size of Byte Array(N), N: Byte Array,
		// 4: Size of PortInitContext(P), P: byte array, 4:
		// PortLinkContextByteSize(Q), Q*3*4: byte array
		int totalSize = /* 2 + 4 + */1 + 1 + 4 + pluginNameSize + 4
				+ sizeofBinary + 4 + portInitContextBytesSize + 4
				+ portLinkContextByteSize;
		byte byteArray[] = new byte[totalSize];

		System.out.println("Plugin Name: " + executablePluginName
				+ ", Total Size: " + totalSize);

		// prepare starting sign $$
		// byteArray[index++] = '$';
		// byteArray[index++] = '$';

		// prepare size of message
		// byteArray[index++] = (byte) (totalSize >> 24);
		// byteArray[index++] = (byte) ((totalSize >> 16) & 0xFF);
		// byteArray[index++] = (byte) ((totalSize >> 8) & 0xFF);
		// byteArray[index++] = (byte) (totalSize & 0xFF);

		// prepare Message Type
		byteArray[index++] = MessageType.LOAD;

		// prepare plugin ID
		byteArray[index++] = (byte) 0;

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
		for (int k = 0; k < portInitContextBytesSize; k++) {
			byteArray[index++] = portInitContextBufferBytes[k];
		}

		// prepare PortLinkContext
		byteArray[index++] = (byte) (portLinkContextByteSize >> 24);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (portLinkContextByteSize & 0xFF);
		for (LinkContextEntry linkEntry : linkContext) {
			int fromPortId = linkEntry.getFromPortId();
			byte[] fromPortIdBytes = intToByteArray(fromPortId);
			for (int i = 0; i < 4; i++) {
				byteArray[index++] = fromPortIdBytes[i];
			}
			int toPortId = linkEntry.getToPortId();
			byte[] toPortIdBytes = intToByteArray(toPortId);
			for (int j = 0; j < 4; j++) {
				byteArray[index++] = toPortIdBytes[j];
			}
			int remotePortId = linkEntry.getRemotePortId();
			byte[] remotePortIdBytes = intToByteArray(remotePortId);
			for (int k = 0; k < 4; k++) {
				byteArray[index++] = remotePortIdBytes[k];
			}
		}

		// System.out.println("------------------");
		int can_id = senders.get(id + "-INSTALL");
		System.out.println("Gonna send can-install to ECU " + id + "; can_id: " + 
				can_id + "; channel: " + channelNumber);
		javaCanLibrary.sendBigData(channelNumber, can_id, 8, byteArray.length,
				byteArray);
	}

	private void sendMessage(InstallMessage message) {
		int id = message.getRemoteEcuId();
		byte pluginId = message.getPluginId();
		String executablePluginName = message.getExecutablePluginName();
		byte[] plugin = message.getBinaryFile();

		int index = 0;
		int pluginNameSize = executablePluginName.length();
		int sizeofBinary = plugin.length;
//		System.out.println("File length: " + sizeofBinary);

		String portInitContextBuffer = "";
		HashMap<String, Integer> portInitialContext = message
				.getPortInitialContext();
		Iterator<Entry<String, Integer>> iterator = portInitialContext
				.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, Integer> entry = iterator.next();
			String key = entry.getKey();
			String value = String.valueOf(entry.getValue());
			if (portInitContextBuffer.equals("")) {
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
		if (linkContext == null) {
		    System.out.println("linkContext null");
		}
		int portLinkContextNum = linkContext.size();
		int portLinkContextByteSize = portLinkContextNum * 12;

		// 1: Message Type, 1: plugin ID, 4: Size of PlugIn Name(M),
		// M: PlugIn Name(byte array), 4: Size of Byte Array(N), N: Byte Array,
		// 4: Size of PortInitContext(P), P: byte array, 4:
		// PortLinkContextByteSize(Q), Q*3*4: byte array
		int totalSize = /* 2 + 4 + */1 + 1 + 4 + pluginNameSize + 4
				+ sizeofBinary + 4 + portInitContextBytesSize + 4
				+ portLinkContextByteSize;
		byte byteArray[] = new byte[totalSize];

		System.out.println("Plugin Name: " + executablePluginName
				+ ", Total Size: " + totalSize);

		// prepare starting sign $$
		// byteArray[index++] = '$';
		// byteArray[index++] = '$';

		// prepare size of message
		// byteArray[index++] = (byte) (totalSize >> 24);
		// byteArray[index++] = (byte) ((totalSize >> 16) & 0xFF);
		// byteArray[index++] = (byte) ((totalSize >> 8) & 0xFF);
		// byteArray[index++] = (byte) (totalSize & 0xFF);

		// prepare Message Type
		byteArray[index++] = MessageType.INSTALL;

		// prepare plugin ID
		byteArray[index++] = pluginId;

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
		for (int k = 0; k < portInitContextBytesSize; k++) {
			byteArray[index++] = portInitContextBufferBytes[k];
		}

		// prepare PortLinkContext
		byteArray[index++] = (byte) (portLinkContextByteSize >> 24);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((portLinkContextByteSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (portLinkContextByteSize & 0xFF);
		for (LinkContextEntry linkEntry : linkContext) {
			int fromPortId = linkEntry.getFromPortId();
			byte[] fromPortIdBytes = intToByteArray(fromPortId);
			for (int i = 0; i < 4; i++) {
				byteArray[index++] = fromPortIdBytes[i];
			}
			int toPortId = linkEntry.getToPortId();
			byte[] toPortIdBytes = intToByteArray(toPortId);
			for (int j = 0; j < 4; j++) {
				byteArray[index++] = toPortIdBytes[j];
			}
			int remotePortId = linkEntry.getRemotePortId();
			byte[] remotePortIdBytes = intToByteArray(remotePortId);
			for (int k = 0; k < 4; k++) {
				byteArray[index++] = remotePortIdBytes[k];
			}
		}

		// System.out.println("------------------");
		int can_id = senders.get(id + "-INSTALL");
		javaCanLibrary.sendBigData(channelNumber, can_id, 8, byteArray.length,
				byteArray);

	}
	
	private void sendMessage(UninstallMessage message) {
		int id = message.getRemoteEcuId();
		byte pluginId = message.getPluginId();
		String pluginName = message.getPluginName();

		int index = 0;
		int pluginNameSize = pluginName.length();

		// 1: Message Type, 1: plugin ID, 4: Size of PlugIn Name(M),
		// M: PlugIn Name(byte array)
		int totalSize = 1 + 1 + 4 + pluginNameSize;
		byte byteArray[] = new byte[totalSize];

		System.out.println("Plugin Name: " + pluginName
				+ ", Total Size: " + totalSize);

		// prepare Message Type
		byteArray[index++] = MessageType.UNINSTALL;

		// prepare plugin ID
		byteArray[index++] = pluginId;

		// prepare plugin name size
		byteArray[index++] = (byte) (pluginNameSize >> 24);
		byteArray[index++] = (byte) ((pluginNameSize >> 16) & 0xFF);
		byteArray[index++] = (byte) ((pluginNameSize >> 8) & 0xFF);
		byteArray[index++] = (byte) (pluginNameSize & 0xFF);

		// prepare plugin name
		for (int i = 0; i < pluginNameSize; i++) {
			byteArray[index++] = (byte) pluginName.charAt(i);
		}

		// System.out.println("------------------");
		int can_id = senders.get(id + "-INSTALL");
		javaCanLibrary.sendBigData(channelNumber, can_id, 8, totalSize,
				byteArray);
	}

	private void sendMessage(PWMMessage message) {
		PWMMessage pwmMessage = (PWMMessage) message;
		int ecuId = pwmMessage.getRemoteEcuId();
		int can_id;
		if (ecuId == 1) {
		    can_id = 1124;
		} else {
		    can_id = senders.get(ecuId + "-PWM");
		}
		byte[] data = pwmMessage.getData();
		System.out.println(ecuId + " " + can_id + " " + Arrays.toString(data));
		javaCanLibrary.sendData(channelNumber, can_id, data);
	}
	
	private void sendMessage(PluginMessage message) {
		PluginMessage pluginMessage = (PluginMessage) message;
		// TODO: add a parameter for selecting destination ECU
		int can_id = senders.get(2 + "-SUB");
		String value = (String) pluginMessage.getValue();
		byte[] valBytes = value.getBytes();
		int valSize = valBytes.length;
		int totalSize = valSize + 4;
		byte[] byteArray = new byte[4 + totalSize];
		for(int i=0;i<valSize;i++) {
			byteArray[i+4] = valBytes[i];
		}
		javaCanLibrary.sendBigData(channelNumber, can_id, 8, totalSize, byteArray);
	}

    public void sendToVCU(String str, int portid) {
	int can_id = 1129;
	int len = str.length();
	byte [] data = new byte[len+4];
	// 469#06000001F4343600
	data[0] = (byte) (portid >> 24);
	data[1] = (byte) (portid >> 16);
	data[2] = (byte) (portid >> 8);
	int x = portid%256;
	data[3] = (byte) x;
	for (int i = 0; i < len; i++) {
	    data[i+4] = (byte) str.charAt(i);
	}
	javaCanLibrary.sendBigData(channelNumber, can_id, 8, len+4, data);
    }

	@Override
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}

	@Override
	public Ecm getEcm() {
		return ecm;
	}

	private byte[] intToByteArray(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) (i >> 24);
		result[1] = (byte) (i >> 16);
		result[2] = (byte) (i >> 8);
		result[3] = (byte) (i);
		return result;
	}
	
	private int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}

	private byte[] parseByteData(byte data) {
		// 2 bits: MessageType, 6 bits: plugin ID
		byte[] res = new byte[2];
		res[0] = (byte) ((int)(data & 0xFF) >> 6);
		res[1] = (byte) (0x3F & data);
		return res;
	}

}
