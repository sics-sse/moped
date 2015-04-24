package sics.port;

import java.io.IOException;

import com.sun.squawk.VM;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;
import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;

import sics.messages.ChannelFrameType;
import sics.plugin.PlugInComponent;

public class PluginRPort implements PluginPort {
	private int id;
	private String name;
	private PlugInComponent plugin;

	public PluginRPort(PlugInComponent plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		id = plugin.getPortId(name);
	}

	// public PluginRPort(String name, Object data) {
	// this.name = name;
	// }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPlugInComponent(PlugInComponent plugin) {
		this.plugin = plugin;
	}

	public int readInt() {
		// 1: type, 4:id
		int totalSize = 5;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = ChannelFrameType.INT_VALUE_RQ;
		dataBytes[1] = (byte) (id >> 24);
		dataBytes[2] = (byte) (id >> 16);
		dataBytes[3] = (byte) (id >> 8);
		dataBytes[4] = (byte) (id);
		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			Channel clientChannel = plugin.getClientChannel();
			clientChannel.send(dataEnv);
			Envelope msg = clientChannel.receive();
			ByteArrayEnvelope replyDataEnv = (ByteArrayEnvelope) msg;
			byte[] replyDataBytes = replyDataEnv.getData();
			byte frameType = replyDataBytes[0];
			switch (frameType) {
			case ChannelFrameType.INT_VALUE_TRANSMIT:
				byte resultBytes[] = new byte[4];
				for (int r = 0; r < 4; r++) {
					resultBytes[r] = replyDataBytes[r + 1];
				}
				int result = new Integer(byteArrayToInt(resultBytes));
				return result;
			default:
				VM.println("Error: channel type");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	public long readLong() {
		long result = 0;
		// 1: type, 4:id
		int totalSize = 5;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = ChannelFrameType.LONG_VALUE_RQ;
		dataBytes[1] = (byte) (id >> 24);
		dataBytes[2] = (byte) (id >> 16);
		dataBytes[3] = (byte) (id >> 8);
		dataBytes[4] = (byte) (id);
		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			Channel clientChannel = plugin.getClientChannel();
			clientChannel.send(dataEnv);
			Envelope msg = clientChannel.receive();
			ByteArrayEnvelope replyDataEnv = (ByteArrayEnvelope) msg;
			byte[] replyDataBytes = replyDataEnv.getData();
			byte frameType = replyDataBytes[0];
			switch (frameType) {
			case ChannelFrameType.LONG_VALUE_TRANSMIT:
				byte resultBytes[] = new byte[8];
				for (int r = 0; r < 8; r++) {
					resultBytes[r] = replyDataBytes[r + 1];
				}
				result = new Long(byteArrayToLong(resultBytes));
				return result;
			default:
				VM.println("Error: channel type");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return result;
	}

	public String readString() {
		// 1: type, 4:id
		int totalSize = 5;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = ChannelFrameType.STRING_VALUE_RQ;
		dataBytes[1] = (byte) (id >> 24);
		dataBytes[2] = (byte) (id >> 16);
		dataBytes[3] = (byte) (id >> 8);
		dataBytes[4] = (byte) (id);
		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			Channel clientChannel = plugin.getClientChannel();
			clientChannel.send(dataEnv);
			Envelope msg = clientChannel.receive();
			ByteArrayEnvelope replyDataEnv = (ByteArrayEnvelope) msg;
			byte[] replyDataBytes = replyDataEnv.getData();
			byte frameType = replyDataBytes[0];
			switch (frameType) {
			case ChannelFrameType.STRING_VALUE_TRANSMIT:
				int length = replyDataBytes.length;
				byte[] resultBytes = new byte[length - 1];
				for (int r = 0; r < length - 1; r++) {
					resultBytes[r] = replyDataBytes[r + 1];
				}
				String result = new String(resultBytes);
				return result;
			default:
				VM.println("Error: channel type");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public Object receive() {
		// 1: type, 4:id
		int totalSize = 5;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = ChannelFrameType.STRING_VALUE_RECEIVE;
		dataBytes[1] = (byte) (id >> 24);
		dataBytes[2] = (byte) (id >> 16);
		dataBytes[3] = (byte) (id >> 8);
		dataBytes[4] = (byte) (id);
		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			Channel clientChannel = plugin.getClientChannel();
			clientChannel.send(dataEnv);
			Envelope msg = clientChannel.receive();
			ByteArrayEnvelope replyDataEnv = (ByteArrayEnvelope) msg;
			byte[] replyDataBytes = replyDataEnv.getData();
			byte frameType = replyDataBytes[0];
			switch (frameType) {
			case ChannelFrameType.STRING_VALUE_RECEIVE:
				int length = replyDataBytes.length;
				byte[] resultBytes = new byte[length - 1];
				for (int r = 0; r < length - 1; r++) {
					resultBytes[r] = replyDataBytes[r + 1];
				}
				String result = new String(resultBytes);
				return result;
			case ChannelFrameType.NONE:
				return null;
			default:
				VM.println("Error: channel type");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			VM.println("NULL POINTER in PluginRPort.receive()");
			ex.printStackTrace();
		}
		return null;
	}

	private int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}

	private long byteArrayToLong(byte[] b) {
		return (long) (b[7] & 0xFF) | ((long) (b[6] & 0xFF)) << 8
				| ((long) (b[5] & 0xFF)) << 16 | ((long) (b[4] & 0xFF)) << 24
				| ((long) (b[3] & 0xFF)) << 32 | ((long) (b[2] & 0xFF)) << 40
				| ((long) (b[1] & 0xFF)) << 48 | ((long) (b[0] & 0xFF)) << 56;
	}
}
