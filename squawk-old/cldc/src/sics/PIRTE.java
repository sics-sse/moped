package sics;

//import java.util.Enumeration;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import sics.port.Port;
import sics.messages.ChannelFrameType;
import sics.messages.LinkContextEntry;
import sics.messages.Message;
import sics.port.EcuVirtualPPort;
import sics.port.EcuVirtualRPort;
import sics.port.PluginPort;
import sics.port.VirtualPPort;
import sics.port.VirtualPort;
import sics.port.VirtualRPort;
import sics.port.instances.ReadPluginDataFromTCUVirtualPPort;
import sics.port.instances.ReadSpeedInPirteVirtualPPort;
import sics.port.instances.ReadSteerInPirteVirtualPPort;
import sics.port.instances.VCUReadPluginDataFromSCUVirtualPPort;
import sics.port.instances.VirtualAdcPPort;
import sics.port.instances.VirtualFrontWheelPPort;
import sics.port.instances.VirtualIMUPPort;
import sics.port.instances.VirtualLedRPort;
import sics.port.instances.SCUWritePluginData2VCURPort;
import sics.port.instances.VirtualPositionPPort;
import sics.port.instances.VirtualPublishPort;
import sics.port.instances.VirtualRearWheelPPort;
import sics.port.instances.VirtualSpeedPort;
import sics.port.instances.VirtualSteeringPort;
import sics.port.instances.VirtualUltraSonicPPort;

import sics.configs.Configuration;
import sics.link.Linker;
import sics.messages.*;

import com.sun.squawk.Isolate;
import com.sun.squawk.VM;
import com.sun.squawk.io.mailboxes.AddressClosedException;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;
import com.sun.squawk.io.mailboxes.MailboxClosedException;
import com.sun.squawk.io.mailboxes.MailboxInUseException;
import com.sun.squawk.io.mailboxes.ServerChannel;
import com.sun.squawk.util.StringTokenizer;

public class PIRTE {
	// attributes that are used to make a one-to-one connection between two
	// isolate
	// TODO: make generic channel in the future
	public final static String MAILBOX_NAME = "PirteChannel";
	public final static int NUM_Messages = 1000;
	public final static int NUM_CLient = 3;
	public final static int CHANNEL_CONTEXT_TYPE = 1;
	public final static int CHANNEL_DATA_TYPE = 2;

	private Hashtable<String, Isolate> installedPlugins = new Hashtable<String, Isolate>();
	
	public static Hashtable<Integer, Port> vpports = new Hashtable<Integer, Port>();
	public static Hashtable<Integer, Port> vrports = new Hashtable<Integer, Port>();

	public static int speed;
	public static int steer;
	
	// TODO: Replace Vector with ArrayList work
	// public static Hashtable<String, Vector<Integer>> linkTable = new
	// Hashtable<String, Vector<Integer>>();

	// public static Hashtable<String, Vector<Integer>> linkTable2 = new
	// Hashtable<String, Vector<Integer>>();
	private Linker linker;

	public PIRTE() {
		linker = new Linker(this);

//		vrports.put(0, new VirtualSpeedPort(0)); // TODO: remove id's from
//													// constructor arguments
//		vrports.put(1, new VirtualSteeringPort(1));
//
//		vrports.put(2, new VirtualPublishPort(2));
//
//		vpports.put(3, new VirtualFrontWheelPPort(3));
//
//		vpports.put(4, new VirtualAdcPPort(4));
//
//		vpports.put(5, new VirtualRearWheelPPort(5));
//		
//		vpports.put(6, new VirtualUltraSonicPPort(6));
//
//		vpports.put(7, new VirtualPositionPPort(7));
		vpports.put(-1, new ReadSpeedInPirteVirtualPPort(-1));
		vpports.put(-2, new ReadSteerInPirteVirtualPPort(-2));
		vrports.put(0, new VirtualPublishPort(0));
		vpports.put(1, new VCUReadPluginDataFromSCUVirtualPPort(1));
		
		vrports.put(3, new VirtualSpeedPort(3));
		vrports.put(4, new VirtualSteeringPort(4));
		vpports.put(5, new VirtualFrontWheelPPort(5));
		vpports.put(6, new VirtualRearWheelPPort(6));
		vpports.put(7, new VirtualAdcPPort(7));
		vpports.put(8, new VirtualPositionPPort(8));
		vrports.put(9, new VirtualLedRPort(9));
		
		vrports.put(11, new SCUWritePluginData2VCURPort(11));
		
		vpports.put(13, new VirtualUltraSonicPPort(13));
		vpports.put(14, new VirtualIMUPPort(14));
		
		vpports.put(15, new ReadPluginDataFromTCUVirtualPPort(15));
		Server server = new Server();
		server.start();

		// linker.registerPort(new VirtualSpeedPort(0)); // TODO: id's should be
		// given automatically
		// linker.registerPort(new VirtualSteeringPort(1));

		// vrports.put(0, new VirtualSpeedPort(0)); //TODO: remove id's from
		// constructor arguments
		// vrports.put(1, new VirtualSteeringPort(1));
	}

	// public void link(LinkContextEntry entry) {
	// linker.link(entry);
	// }

	// public void writeData(int portId, Message message) {
	// VirtualRPort virtualRPort = (VirtualRPort) linker
	// .getVirtualPort(portId);
	// virtualRPort.deliver(-1, message);
	// }

	// public PluginPort getPluginPort(int portID) {
	// PluginPort port = linker.getPluginPort(portID);
	// return port;
	// }
	//
	// public VirtualPort getVirtualPort(int vportId) {
	// VirtualPort virtualPort = linker.getVirtualPort(vportId);
	// return virtualPort;
	// }

	public void registerPort(Port port) {
		linker.registerPort(port);
	}

	// protected abstract void registerVPorts(int[] vpports, int[] vrports);

	class ServerChannelHandler extends Thread {
		private Channel channel;

		public ServerChannelHandler(Channel channel) {
			this.channel = channel;
		}

		public void run() {
			int portId;
			byte[] portIdBytes = new byte[4];
			VM.println("[Server] Server channel is built");
			// handle messages:
			while (true) {
				Envelope msg;
				try {
					msg = channel.receive();
					VM.println("[Server] Server is receiving message");
					if (msg instanceof ByteArrayEnvelope) {
						ByteArrayEnvelope dataEnv = (ByteArrayEnvelope) msg;
						//VM.println("getData");
						byte[] data = dataEnv.getData();
						byte frameType = data[0];
						VM.println("frameType " + frameType);
						switch (frameType) {
						case ChannelFrameType.INT_VALUE_TRANSMIT:
							// Get port ID
							portIdBytes[0] = data[1];
							portIdBytes[1] = data[2];
							portIdBytes[2] = data[3];
							portIdBytes[3] = data[4];
							portId = byteArrayToInt(portIdBytes);

							// Get int value
							byte valueBytes[] = new byte[4];
							valueBytes[0] = data[5];
							valueBytes[1] = data[6];
							valueBytes[2] = data[7];
							valueBytes[3] = data[8];
							int value = byteArrayToInt(valueBytes);

							deliverValue(portId, value);
							break;
						case ChannelFrameType.STRING_VALUE_TRANSMIT:
							// Get port ID
							portIdBytes[0] = data[1];
							portIdBytes[1] = data[2];
							portIdBytes[2] = data[3];
							portIdBytes[3] = data[4];
							portId = byteArrayToInt(portIdBytes);

							// Get value byte size
							byte valueSizeBytes[] = new byte[4];
							valueSizeBytes[0] = data[5];
							valueSizeBytes[1] = data[6];
							valueSizeBytes[2] = data[7];
							valueSizeBytes[3] = data[8];
							int valueBytesSize = byteArrayToInt(valueSizeBytes);

							byte[] stringValueBytes = new byte[valueBytesSize];
							for (int v = 0; v < valueBytesSize; v++) {
								stringValueBytes[v] = data[v + 9];
							}

							String stringValue = new String(stringValueBytes);

							deliverValue(portId, stringValue);
							break;
						case ChannelFrameType.INT_VALUE_RQ:
							// Get port ID
							portIdBytes[0] = data[1];
							portIdBytes[1] = data[2];
							portIdBytes[2] = data[3];
							portIdBytes[3] = data[4];
							portId = byteArrayToInt(portIdBytes);
							VM.println("calling fetchInt");
							int replyValue = fetchIntVal(portId);
							VM.println("called fetchInt " + replyValue);
							// 1: type, 4: intValue
							int totalSize = 5;
							byte dataBytes[] = new byte[totalSize];
							dataBytes[0] = ChannelFrameType.INT_VALUE_TRANSMIT;
							dataBytes[1] = (byte) (replyValue >> 24);
							dataBytes[2] = (byte) (replyValue >> 16);
							dataBytes[3] = (byte) (replyValue >> 8);
							dataBytes[4] = (byte) (replyValue);
							Envelope replyDataEnv = new ByteArrayEnvelope(
									dataBytes);
							VM.println("channel.send 1");
							channel.send(replyDataEnv);
							VM.println("channel.send 2");
							break;
						case ChannelFrameType.STRING_VALUE_RQ:
							// Get port ID
							portIdBytes[0] = data[1];
							portIdBytes[1] = data[2];
							portIdBytes[2] = data[3];
							portIdBytes[3] = data[4];
							portId = byteArrayToInt(portIdBytes);
							String replyValStr = fetchStrVal(portId);
							byte[] replyValBytes = replyValStr.getBytes();
							int replyValBytesSize = replyValBytes.length;
							// 1: type, strValBytes
							int replyMsgSize = 1 + replyValBytesSize;
							byte replyMsgBytes[] = new byte[replyMsgSize];
							replyMsgBytes[0] = ChannelFrameType.STRING_VALUE_TRANSMIT;
							for (int r = 0; r < replyValBytesSize; r++) {
								replyMsgBytes[r + 1] = replyValBytes[r];
							}
							Envelope replyStringDataEnv = new ByteArrayEnvelope(
									replyMsgBytes);
							channel.send(replyStringDataEnv);
							break;
						case ChannelFrameType.LONG_VALUE_RQ:
							// Get port ID
							portIdBytes[0] = data[1];
							portIdBytes[1] = data[2];
							portIdBytes[2] = data[3];
							portIdBytes[3] = data[4];
							portId = byteArrayToInt(portIdBytes);
							long replyLongValue = fetchLongVal(portId);
							// 1: type, 4: longValue
							int totalSize4LongVal = 9;
							byte dataBytes4LongVal[] = new byte[totalSize4LongVal];
							dataBytes4LongVal[0] = ChannelFrameType.LONG_VALUE_TRANSMIT;
							dataBytes4LongVal[1] = (byte) (replyLongValue >> 56);
							dataBytes4LongVal[2] = (byte) (replyLongValue >> 48);
							dataBytes4LongVal[3] = (byte) (replyLongValue >> 40);
							dataBytes4LongVal[4] = (byte) (replyLongValue >> 32);
							dataBytes4LongVal[5] = (byte) (replyLongValue >> 24);
							dataBytes4LongVal[6] = (byte) (replyLongValue >> 16);
							dataBytes4LongVal[7] = (byte) (replyLongValue >> 8);
							dataBytes4LongVal[8] = (byte) (replyLongValue);
							Envelope replyDataEnv4LongVal = new ByteArrayEnvelope(
									dataBytes4LongVal);
							channel.send(replyDataEnv4LongVal);
							break;
						case ChannelFrameType.STRING_VALUE_SEND:
							// Get port ID
						    VM.println("sending 1 " + portIdBytes);
							portIdBytes[0] = data[1];
							portIdBytes[1] = data[2];
							portIdBytes[2] = data[3];
							portIdBytes[3] = data[4];
							portId = byteArrayToInt(portIdBytes);
						    VM.println("sending to " + portId);
							// Get value byte size
							byte sentValueSizeBytes[] = new byte[4];
							sentValueSizeBytes[0] = data[5];
							sentValueSizeBytes[1] = data[6];
							sentValueSizeBytes[2] = data[7];
							sentValueSizeBytes[3] = data[8];
							int sentValueBytesSize = byteArrayToInt(sentValueSizeBytes);
							byte[] sentValueBytes = new byte[sentValueBytesSize];
							for (int v = 0; v < sentValueBytesSize; v++) {
								sentValueBytes[v] = data[v + 9];
							}
							String sentValueStr = new String(sentValueBytes);
							
							// Get remote plugin port ID
							int remotePluginPortId = linker.getPluginRPortId(portId);
							PluginMessage pluginMessage = new PluginMessage(remotePluginPortId, sentValueStr);
							// Arndt: 11
							sendValue2(11, pluginMessage);
							//							sendValue(portId, pluginMessage);
							break;
						case ChannelFrameType.STRING_VALUE_RECEIVE:
							// Get port ID
							VM.println("receiving 1 " + portIdBytes);
							portIdBytes[0] = data[1];
							portIdBytes[1] = data[2];
							portIdBytes[2] = data[3];
							portIdBytes[3] = data[4];
							portId = byteArrayToInt(portIdBytes);
							VM.println("receiving from " + portId);
							Object replyObject = receivePluginData(portId);
							if(replyObject == null) {
								byte[] replyNullMsgBytes = new byte[1];
								replyNullMsgBytes[0] = ChannelFrameType.NONE;
								Envelope replyNullDataEnv = new ByteArrayEnvelope(
										replyNullMsgBytes);
								channel.send(replyNullDataEnv);
							} else {
								String replyReceivedValStr = (String) replyObject;
								byte[] replyReceivedValBytes = replyReceivedValStr.getBytes();
								int replyReceivedValBytesSize = replyReceivedValBytes.length;
								// 1: type, strValBytes
								int replyReceivedMsgSize = 1 + replyReceivedValBytesSize;
								byte replyReceivedMsgBytes[] = new byte[replyReceivedMsgSize];
								replyReceivedMsgBytes[0] = ChannelFrameType.STRING_VALUE_RECEIVE;
								for (int r = 0; r < replyReceivedValBytesSize; r++) {
									replyReceivedMsgBytes[r + 1] = replyReceivedValBytes[r];
								}
								Envelope replyReceivedDataEnv = new ByteArrayEnvelope(
										replyReceivedMsgBytes);
								channel.send(replyReceivedDataEnv);
							}
							break;
						default:
							VM.println("Error: Wrong channel frame type");
							return;
						}
					} else {
					    VM.println("not an envelope");
					}
				} catch (MailboxClosedException e) {
					VM.println("[Server] Server seems to have gone away. Oh well. "
									+ channel);
					break;
				} catch (AddressClosedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NullPointerException e) {
				    //VM.println("NULL POINTER in ServerChannelHandler of PIRTE");
					e.printStackTrace();
					try {
					    byte[] replyNullMsgBytes = new byte[1];
					    replyNullMsgBytes[0] = ChannelFrameType.NONE;
					    Envelope replyNullDataEnv = new ByteArrayEnvelope(
											      replyNullMsgBytes);
					    channel.send(replyNullDataEnv);
					} catch (AddressClosedException e2) {
					    // TODO Auto-generated catch block
					    e2.printStackTrace();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				// finally {
				// channel.close();
				// }

			}
		}
	}

	class Server extends Thread {
		public void run() {

			ServerChannel serverChannel = null;
			Channel aChannel = null;
			try {
				serverChannel = ServerChannel.create(MAILBOX_NAME);
			} catch (MailboxInUseException ex) {
				throw new RuntimeException(ex.toString());
			}
			while (true) {
				try {
					aChannel = serverChannel.accept();
					new Thread(new ServerChannelHandler(aChannel)).start();
				} catch (IOException ex) {
					// ok, just close server.
				}
				// finally {
				// // no way to get here:
				// System.out.println("Closing server...");
				// VM.println("[Server] Closing server...");
				// aChannel.close();
				// serverChannel.close();
				// }
			}
		}
	}

	public static void sendPublishData(String key, String value) {
		//1: message type, m: publish data
		byte[] keyBytes = key.getBytes();
		int keyByteSize = keyBytes.length;
		byte[] valueBytes = value.getBytes();
		int valueByteSize = valueBytes.length;
		int totalSize = /*2 + 4 +*/ 1 + 4 + keyByteSize + 4 + valueByteSize;

		byte buffer[] = new byte[totalSize];
		int index = 0;

		// message type
		byte type = 0;
		type = (byte) (MessageType.PUBLISH << 6);
		buffer[index++] = type;
		
		// key
		buffer[index++] = (byte) (keyByteSize >> 24);
		buffer[index++] = (byte) ((keyByteSize >> 16) & 0xFF);
		buffer[index++] = (byte) ((keyByteSize >> 8) & 0xFF);
		buffer[index++] = (byte) (keyByteSize & 0xFF);

		for (int i = 0; i < keyByteSize; i++) {
			buffer[index++] = keyBytes[i];
		}

		// value
		buffer[index++] = (byte) (valueByteSize >> 24);
		buffer[index++] = (byte) ((valueByteSize >> 16) & 0xFF);
		buffer[index++] = (byte) ((valueByteSize >> 8) & 0xFF);
		buffer[index++] = (byte) (valueByteSize & 0xFF);

		for (int j = 0; j < valueByteSize; j++) {
			buffer[index++] = valueBytes[j];
		}

		// send out
		VM.jnaSendPackageData(totalSize, buffer);

	}

	private void loadPlugin(String pluginUrl, String[] portInitContextArray)
			throws Exception {
		VM.print("Creating plugin isolate from PIRTE\r\n");
		String extractClassName = extractClass(pluginUrl);
		// Isolate iso = new Isolate(null, extractClassName, new String[0],
		// null,
		// pluginUrl);
		//1 VM.println("loadPlugin 1");
		Isolate iso = new Isolate(null, extractClassName, portInitContextArray,
				null, pluginUrl);

		//1 VM.println("loadPlugin 2 " + extractClassName);
		// register Isolate and name to installedPlugins
		String shortPluginName = pluginUrl
				.substring(pluginUrl.lastIndexOf('/') + 1);
		installedPlugins.put(shortPluginName, iso);

		//1 VM.println("loadPlugin 3 " + extractClassName);
		iso.start();
		//1 VM.println("loadPlugin 4 " + extractClassName);
		iso.join();
		//1 VM.println("loadPlugin 5 " + extractClassName);
	}

	class PluginLoader implements Runnable {
		private String name;
		private String[] portInitContextArray;

		public PluginLoader(String name, String[] portInitContextArray) {
			this.name = name;
			this.portInitContextArray = portInitContextArray;
		}

		@Override
		public void run() {
			try {
			    VM.println("run " + name);
				loadPlugin(name, portInitContextArray);
			    VM.println("run2 " + name);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private String extractClass(String pluginUrl) {
		int begin = pluginUrl.indexOf("//") + 2;
		int end = pluginUrl.lastIndexOf('/');
		String substring = pluginUrl.substring(begin, end);
		String res = substring.replace('/', '.');
		return res;
	}

	private void sendMessage(RequestIdAckMessage message) {
		// 2: $$, 4: total size, 1: message type, 4: id
		int totalSize = 11;
		byte buffer[] = new byte[totalSize];
		int index = 0;

		// assign value

		// starting sign: $$
		buffer[index++] = '$';
		buffer[index++] = '$';

		// message size
		buffer[index++] = (byte) (totalSize >> 24);
		buffer[index++] = (byte) ((totalSize >> 16) & 0xFF);
		buffer[index++] = (byte) ((totalSize >> 8) & 0xFF);
		buffer[index++] = (byte) (totalSize & 0xFF);

		// message typeinstalledPluginsShortcut
		buffer[index++] = (byte) message.getMessageType();

		// id
		int id = message.getId();
		buffer[index++] = (byte) (id >> 24);
		buffer[index++] = (byte) ((id >> 16) & 0xFF);
		buffer[index++] = (byte) ((id >> 8) & 0xFF);
		buffer[index++] = (byte) (id & 0xFF);

		VM.jnaSendPackageData(totalSize, buffer);
	}

	private void sendMessage(InstallAckMessage message) {
		byte pluginId = message.getPluginId();
		// 2 bits: message type, 6 bits:plugin ID
		byte data = 0;
		data = (byte) (MessageType.INSTALL_ACK << 6);
		data |= pluginId;

		// send out
		VM.jnaSendAckByte(data);
	}

	private void sendMessage(UninstallAckMessage message) {
		byte pluginId = message.getPluginId();
		// 2 bits: message type, 6 bits:plugin ID
		byte data = 0;
		data = (byte) (MessageType.UNINSTALL_ACK << 6);
		data |= pluginId;

		// send out
		VM.jnaSendAckByte(data);
	}

	private void sendMessage(RestoreAckMessage message) {

	}

	public void process() {
		boolean isConsectutive = false;
		int messageSize = 0;
		int index = 0;
		byte[] message = null;
		int stuck = 0;
		/*
		 * Listen for incoming bytecode and start plugins immediately after
		 * completion of every code transfer
		 */
		int nodatacount = 0;
		while (true) {
			// check how many data bytes updated
			// count = VM.jnaReadCount();
			// VM.print("#### count: "+count+"\r\n");
			// for (i = 0; i < count; i++) {

			int isNewPack = VM.jnaCheckIfNewPackage();
			if (isNewPack == 1) {
				messageSize = VM.jnaGetLengthPackage();
				message = new byte[messageSize+200];
				//message = new byte[messageSize];
				index = 0;
				isConsectutive = true;
				VM.println("Java: messageSize:" + messageSize);
			}

			//VM.println("consec " + isConsectutive);
			if (isConsectutive && index<messageSize) {
			    //int size2 = VM.jnaFetchNewDataSize();
			    //VM.println("jna size " + size2);
				// if(size<=0)
				// continue;
				int startIndex = VM.jnaGetReadStartIndex();
				int rearIndex = VM.jnaGetReadRearIndex();
				//VM.println("new data?");
				if (startIndex != rearIndex) {
				    //VM.println("PIRTE: indices (" + startIndex + ") (" + rearIndex + ")");
				}
				if (!hasNewData(startIndex, rearIndex)) {
				    nodatacount++;
				    //VM.println("no new data " + nodatacount);
				    if (nodatacount > 100 && false) {
					nodatacount = 0;
						index = 0;
						message = null;
						isConsectutive = false;
						messageSize = 0;
				    }
					continue;
				} else {
				    nodatacount = 0;
				}
				int size = getNewDataSize(startIndex, rearIndex);
				//VM.print("size:");
				//VM.println(size);

				if(size<=0) {
				    VM.println("size " + size);
					continue;
				}
				for (int n = 0; n < size; n++) {
				    byte bb = VM.jnaFetchByte(rearIndex);
					message[index++] = bb;
					//VM.print(" (_" + (bb & 0xff) + "_)");
				}
				//VM.println("");
				//1 VM.print("index:");
				//1 VM.println(index);
				// Process message when message is ready
				// Do something meaningful if >, because that's
				// an error
				if (index >= messageSize) {
					byte messageType = message[0];
					switch (messageType) {
					case MessageType.REQUEST_ID:
						VM.println("Receive request id");
						RequestIdAckMessage requestIdAckMessage = new RequestIdAckMessage(
								2);
						VM.println("Send request ack");
						sendMessage(requestIdAckMessage);

						// clear context
						// isNewStart = false;
						// isMessageReady = false;
						// isMessageSizeReady = false;
						messageSize = 0;
						index = 0;
						message = null;
						break;
					case MessageType.INSTALL:
					case MessageType.LOAD:
						index = 1;
						// Get plugin ID
						byte pluginId = message[index++];

						
						// Handle size of PlugIn Name
						int pluginNameSize = (message[index++] << 24)
								+ (message[index++] << 16)
								+ (message[index++] << 8)
								+ (message[index++] << 0);

						VM.println("plugin name size: " + pluginNameSize);

						// Handle PlugIn Name
						String pluginName = "";
						for (int i = 0; i < pluginNameSize; i++) {
						    //1 VM.println(" (" + i + ") (" + (char) message[index] + ")");
							pluginName += (char) message[index++];
						}

						//1 VM.println("plugin name: " + pluginName);

						// Handle size of PlugIn
						byte pluginSizeBuffer[] = new byte[4];
						for (int z = 0; z < 4; z++) {
							pluginSizeBuffer[z] = message[index++];
						}

						int pluginSize = byteArrayToInt(pluginSizeBuffer);
//						VM.println("@pluginSize@ " + pluginSize);
						// Handle PlugIn
						byte[] plugin = new byte[pluginSize];
						for (int m = 0; m < pluginSize; m++) {
							plugin[m] = message[index++];
						}

						// Handle size of port init context
						byte portInitContextSizeBytes[] = new byte[4];
						portInitContextSizeBytes[0] = message[index++];
						portInitContextSizeBytes[1] = message[index++];
						portInitContextSizeBytes[2] = message[index++];
						portInitContextSizeBytes[3] = message[index++];
						int portInitContextSize = byteArrayToInt(portInitContextSizeBytes);

						// Handle port init context
						String[] portInitContext;
						StringTokenizer tok;

						byte[] portInitContextBytes = new byte[portInitContextSize];
						for (int z = 0; z < portInitContextSize; z++) {
							portInitContextBytes[z] = message[index++];
						}
						String portInitContextStr = new String(
								portInitContextBytes);
						tok = new StringTokenizer(portInitContextStr, "|");
						int countTokens = tok.countTokens();
						portInitContext = new String[countTokens];
						for (int i = 0; i < countTokens; i++) {
							portInitContext[i] = (String) tok.nextElement();
						}

						// Handle port link context
						ArrayList<LinkContextEntry> linkContext = new ArrayList<LinkContextEntry>();
						int fromPortIdTmp;
						int toPortIdTmp;
						int remotePortIdTmp;
						byte[] fromPortIdBytes = new byte[4];
						byte[] toPortIdBytes = new byte[4];
						byte[] remotePortIdBytes = new byte[4];

						byte[] portLinkContextNumBytes = new byte[4];
						portLinkContextNumBytes[0] = message[index++];
						portLinkContextNumBytes[1] = message[index++];
						portLinkContextNumBytes[2] = message[index++];
						portLinkContextNumBytes[3] = message[index++];
						int portLinkContextByteSize = byteArrayToInt(portLinkContextNumBytes);
						for (int y = 0; y < portLinkContextByteSize; y = y + 12) {
							fromPortIdBytes[0] = message[index++];
							fromPortIdBytes[1] = message[index++];
							fromPortIdBytes[2] = message[index++];
							fromPortIdBytes[3] = message[index++];
							fromPortIdTmp = byteArrayToInt(fromPortIdBytes);
							toPortIdBytes[0] = message[index++];
							toPortIdBytes[1] = message[index++];
							toPortIdBytes[2] = message[index++];
							toPortIdBytes[3] = message[index++];
							toPortIdTmp = byteArrayToInt(toPortIdBytes);
							remotePortIdBytes[0] = message[index++];
							remotePortIdBytes[1] = message[index++];
							remotePortIdBytes[2] = message[index++];
							remotePortIdBytes[3] = message[index++];
							remotePortIdTmp = byteArrayToInt(remotePortIdBytes);
							LinkContextEntry entry = new LinkContextEntry(
									fromPortIdTmp, toPortIdTmp, remotePortIdTmp);
							linkContext.add(entry);
							VM.println("Added PLC: " + fromPortIdTmp + " -> " + toPortIdTmp + " via " + remotePortIdTmp);
						}

						// finish reading all executable bytes
						VM.println("###################################");
						VM.println("Size of Total Message Size: " + messageSize);
						VM.println("Plugin Name: " + pluginName);
						VM.println("Size of bytes:" + pluginSize);
						VM.println("###################################");

						// Store the byte array in a globally accessible static variable
						 
						VM.registerPluginObjectMemory(pluginName, plugin);
						VM.print("plugin registered\r\n");

						if (messageType == MessageType.INSTALL) {
							// send InstallAckMessage
							VM.println("Send install ack message");
							VM.print("pluginName:");
							VM.println(pluginName);

							// String shortPluginName = pluginName
							// .substring(pluginName.lastIndexOf('/') + 1);
							// VM.println("ShortPluginName:" + shortPluginName);
							InstallAckMessage installAckMessage = new InstallAckMessage(
									pluginId);

							sendMessage(installAckMessage);
						}

						try {
							// Register links
							Iterator<LinkContextEntry> iterator = linkContext
									.iterator();
							while (iterator.hasNext()) {
								LinkContextEntry entry = iterator.next();
								linker.link(entry);
							}

							// loadPlugin(pluginName);
							new Thread(new PluginLoader(pluginName,
									portInitContext)).start();
							VM.print("plugin loaded\r\n");
						} catch (Exception e) {
							VM.print(e.toString());
							e.printStackTrace();
						}

						// clear the context
						pluginNameSize = -1;
						pluginName = "";
						index = 0;
						plugin = null;
						message = null;
						isConsectutive = false;
						messageSize = 0;
						// Arndt, should not be needed
						// I added it
						try {
						    Thread.sleep(1000);
						} catch (InterruptedException e) {
						    // TODO Auto-generated catch block
						    e.printStackTrace();
						}

						break;
					case MessageType.UNINSTALL:
						index = 1;
						byte pluginId4Uninstall = message[index++];
						VM.print("plugin id for uninstallation:");
						VM.println(pluginId4Uninstall);
						byte[] tempSizeBytes = new byte[4];
						tempSizeBytes[0] = message[index++];
						tempSizeBytes[1] = message[index++];
						tempSizeBytes[2] = message[index++];
						tempSizeBytes[3] = message[index++];
						int pluginNameSize4Uninstall = byteArrayToInt(tempSizeBytes);
						VM.println("plugin name size: "
								+ pluginNameSize4Uninstall);

						// Handle PlugIn Name
						String pluginName4Uninstall = "";
						for (int i = 0; i < pluginNameSize4Uninstall; i++) {
							pluginName4Uninstall += (char) message[index++];
						}

						VM.println("plugin name: " + pluginName4Uninstall);

						// TODO: do function
						// Stop Isolate
						Isolate isolate = installedPlugins
								.get(pluginName4Uninstall);
						if(isolate != null) {
						
							if(!isolate.isExited()) {
								isolate.exit(1);
							}
							
							UninstallAckMessage uninstallAckMessage = new UninstallAckMessage(
									pluginId4Uninstall);
							sendMessage(uninstallAckMessage);
						}
						// clear the context
						// isNewStart = false;
						// isMessageReady = false;
						// isMessageSizeReady = false;
						pluginNameSize4Uninstall = -1;
						pluginName4Uninstall = "";
						index = 0;
						plugin = null;
						isConsectutive = false;
						message = null;
						VM.println("The end of uninstallation handling");
						break;
					case MessageType.RESTORE:
						break;
					case MessageType.PLUGIN_MESSAGE:
						break;
					case MessageType.PORT_LINK_CONTEXT_MESSAGE:
						break;
					default:
						VM.println("Error: Wrong message type pushed to receiving channel (" + message[0] + ")");
						if (false) {
						    pluginNameSize = -1;
						    pluginName = "";
						    index = 0;
						    plugin = null;
						    message = null;
						    isConsectutive = false;
						    messageSize = 0;
						}

						// Arndt added needed?
						index = 0;

						stuck = 1;
					}
				}
		
				// update count
				// VM.jnaUpdateCount(1);
			} else {
			    //VM.println("nothing");
			    if (stuck > 0 && false) {
				    VM.println("new data?");
				int startIndex = VM.jnaGetReadStartIndex();
				    VM.println("new data2? " + startIndex);
				int rearIndex = VM.jnaGetReadRearIndex();
				    VM.println("new data3? " + rearIndex);
				//VM.println("new data?");
				if (!hasNewData(startIndex, rearIndex)) {
				    VM.println("no new data");
				} else {
				    VM.println("new data");
				}
			    }
			}

			try {
			    if (true) {
				if (stuck > 0)
				    Thread.sleep(5);
				else
				    Thread.sleep(5);
			    }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}

	public static void main(String[] args) {
		VM.print("PIRTE is running 2 ...\r\n");

		PIRTE p = new PIRTE();
		

		// try {
		// server.join();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// p.checkInstalledApp();
		// while(true) {
		// try {
		// Thread.sleep(5);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		// p.waitIdRequestAndSendId(2);

		// clear incoming buffer
		// VM.jnaClearIncomingBuffer();
		
		// p.prepareApps();
		p.process();
	}

	public void deliverValue(int pportId, int value) {
	    //VM.println(pportId);
		int vrportId = linker.getVirtualRPortId(pportId);
		//VM.println(vrportId);
		EcuVirtualRPort vrport = (EcuVirtualRPort) vrports.get(vrportId);
		if (vrport == null) {
			VM.print("Error: Fail to get Virtual Port");
			VM.println(vrportId);
			return;
		}
		vrport.deliver(new Integer(value));
	}

	public void deliverValue(int pportId, String value) {
		int vrportId = linker.getVirtualRPortId(pportId);
	    VM.println("deliverValue " + pportId);
		EcuVirtualRPort vrport = (EcuVirtualRPort) vrports.get(vrportId);
	    VM.println("deliverValue 2 " + pportId);
		vrport.deliver(value);
	    VM.println("deliverValue 3 " + pportId);
	}
	
	public void sendValue(int pportId, PluginMessage message) {
	    VM.println("vrports = " + vrports);
	    VM.println("vpports = " + vpports);
	    VM.println("sendValue 0 " + pportId);
		int vrportId = linker.getVirtualRPortId(pportId);
	    VM.println("sendValue 1 " + pportId + " " + vrportId);
		EcuVirtualRPort vrport = (EcuVirtualRPort) vrports.get(vrportId);
	    VM.println("sendValue 2 " + pportId);
		vrport.deliver(message);
	    VM.println("sendValue 3 " + pportId);
	}

	public void sendValue2(int vrportId, PluginMessage message) {
	    VM.println("sendValue2 1 " + vrportId);
		EcuVirtualRPort vrport = (EcuVirtualRPort) vrports.get(vrportId);
	    VM.println("sendValue2 2 " + vrportId);
		vrport.deliver(message);
	    VM.println("sendValue2 3 " + vrportId);
	}

	private int fetchIntVal(int rportId) {
		int vpportId = linker.getVirtualPPortId(rportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
		Integer intVal = (Integer) vpport.deliver();
		return intVal.intValue();
	}
	
	private long fetchLongVal(int rportId) {
		int vpportId = linker.getVirtualPPortId(rportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
		Long longVal = (Long) vpport.deliver();
		return longVal.longValue();
	}

	private String fetchStrVal(int rportId) {
		int vpportId = linker.getVirtualPPortId(rportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
		String strVal = (String) vpport.deliver();
		return strVal;
	}
	
	private Object receivePluginData(int rportId) {
	    VM.println("vrports = " + vrports);
	    VM.println("vpports = " + vpports);
	    VM.println("receivePluginData 0 " + rportId);
	    //int vpportId = linker.getVirtualPPortId(rportId);
	    // Arndt
	    int vpportId = 1;
	    VM.println("receivePluginData 1 " + rportId + " " + vpportId);
		EcuVirtualPPort vpport = (EcuVirtualPPort) vpports.get(vpportId);
	    VM.println("receivePluginData 2 " + rportId);
		Object res = vpport.deliver(rportId);
	    VM.println("receivePluginData 3 " + rportId);
		return res;
	}

	private boolean hasNewData(int startIndex, int rearIndex) {
		return startIndex != rearIndex ? true : false;
	}

	private int getNewDataSize(int startIndex, int rearIndex) {
		int size = 0;
		if (startIndex > rearIndex) {
		    // This number (5000) must be the same as BUFFERSIZE
		    // in {demo_SCU,demo_VCU}MOPED_signal.h in autosar.
			size = 5000 - startIndex + rearIndex;
		} else if (startIndex < rearIndex) {
			size = rearIndex - startIndex;
		}
		return size;
	}

}
