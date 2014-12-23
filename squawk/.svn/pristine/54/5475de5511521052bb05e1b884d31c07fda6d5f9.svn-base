package sics.port;

import com.sun.squawk.io.mailboxes.Channel;
import com.sun.squawk.io.mailboxes.Envelope;
import com.sun.squawk.io.mailboxes.ByteArrayEnvelope;
import java.io.IOException;

import sics.messages.ChannelFrameType;
import sics.plugin.PlugInComponent;

public class PluginPPort implements PluginPort {
	private int id;
	private String name;
	private PlugInComponent plugin;

	public PluginPPort(PlugInComponent plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		id = plugin.getPortId(name);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PlugInComponent getPlugin() {
		return plugin;
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

	public void write(int value) {
		// 1: type, 4:id, 4: intValue
		int totalSize = 9;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = ChannelFrameType.INT_VALUE_TRANSMIT;
		dataBytes[1] = (byte) (id >> 24);
		dataBytes[2] = (byte) (id >> 16);
		dataBytes[3] = (byte) (id >> 8);
		dataBytes[4] = (byte) (id);
		dataBytes[5] = (byte) (value >> 24);
		dataBytes[6] = (byte) (value >> 16);
		dataBytes[7] = (byte) (value >> 8);
		dataBytes[8] = (byte) (value);
		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			Channel clientChannel = plugin.getClientChannel();
			clientChannel.send(dataEnv);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void write(String value) {
		// 1: type, 4:id, 4: value size(M), M: bytes
		byte[] valueBytes = value.getBytes();
		int valueBytesSize = valueBytes.length;
		int totalSize = 9 + valueBytesSize;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = ChannelFrameType.STRING_VALUE_TRANSMIT;
		dataBytes[1] = (byte) (id >> 24);
		dataBytes[2] = (byte) (id >> 16);
		dataBytes[3] = (byte) (id >> 8);
		dataBytes[4] = (byte) (id);
		dataBytes[5] = (byte) (valueBytesSize >> 24);
		dataBytes[6] = (byte) (valueBytesSize >> 16);
		dataBytes[7] = (byte) (valueBytesSize >> 8);
		dataBytes[8] = (byte) (valueBytesSize);
		for (int i = 0; i < valueBytesSize; i++) {
			dataBytes[i + 9] = valueBytes[i];
		}
		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			Channel clientChannel = plugin.getClientChannel();
			clientChannel.send(dataEnv);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void send(String value) {
		// 1: type, 4:id, 4: value size(M), M: bytes
		byte[] valueBytes = value.getBytes();
		int valueBytesSize = valueBytes.length;
		int totalSize = 9 + valueBytesSize;
		byte dataBytes[] = new byte[totalSize];
		dataBytes[0] = ChannelFrameType.STRING_VALUE_SEND;
		dataBytes[1] = (byte) (id >> 24);
		dataBytes[2] = (byte) (id >> 16);
		dataBytes[3] = (byte) (id >> 8);
		dataBytes[4] = (byte) (id);
		dataBytes[5] = (byte) (valueBytesSize >> 24);
		dataBytes[6] = (byte) (valueBytesSize >> 16);
		dataBytes[7] = (byte) (valueBytesSize >> 8);
		dataBytes[8] = (byte) (valueBytesSize);
		for (int i = 0; i < valueBytesSize; i++) {
			dataBytes[i + 9] = valueBytes[i];
		}
		Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
		try {
			Channel clientChannel = plugin.getClientChannel();
			clientChannel.send(dataEnv);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// public void write(int value) {
	// int totalSize = name.length() + 4;
	// byte dataBytes[] = new byte[totalSize];
	// dataBytes[0] = (byte) (value >> 24);
	// dataBytes[1] = (byte) (value >> 16);
	// dataBytes[2] = (byte) (value >> 8);
	// dataBytes[3] = (byte) (value);
	// byte[] nameBytes = name.getBytes();
	// for (int i = 4; i < totalSize; i++) {
	// dataBytes[i] = nameBytes[i - 4];
	// }
	// Envelope dataEnv = new ByteArrayEnvelope(dataBytes);
	// try {
	// Channel clientChannel = plugin.getClientChannel();
	// clientChannel.send(dataEnv);
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// }

}
