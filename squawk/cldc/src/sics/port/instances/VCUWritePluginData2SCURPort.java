package sics.port.instances;

import com.sun.squawk.VM;

import sics.messages.PluginMessage;
import sics.port.EcuVirtualRPort;

//TODO: Make this class generic (so that data doesn't need casting)
public class VCUWritePluginData2SCURPort extends EcuVirtualRPort {

	public VCUWritePluginData2SCURPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		PluginMessage pluginMessage = (PluginMessage) data;
		int remotePortId = pluginMessage.getRemotePortId();
		String value = pluginMessage.getValue();
		byte[] valBytes = value.getBytes();
		int valLength = valBytes.length;
		int totalSize = 4 + valLength;
		byte[] dataBytes = new byte[totalSize];
		dataBytes[0] = (byte) (remotePortId >> 24);
		dataBytes[1] = (byte) (remotePortId >> 16);
		dataBytes[2] = (byte) (remotePortId >> 8);
		dataBytes[3] = (byte) (remotePortId);
		for(int i=0;i<valLength;i++) {
			dataBytes[i+4] = valBytes[i];
		}
		// use the same function, but when done on VCU, it will
		// write to SCU
		VM.jnaWritePluginData2VCU(totalSize, dataBytes);
	}

}
