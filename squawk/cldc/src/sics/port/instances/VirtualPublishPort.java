package sics.port.instances;

import com.sun.squawk.VM;

import sics.PIRTE;
import sics.port.EcuVirtualRPort;

//TODO: Make this class generic (so that data doesn't need casting)
public class VirtualPublishPort extends EcuVirtualRPort {

	public VirtualPublishPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		String stringVal = (String) data;
		int splitter = stringVal.indexOf('|');
		String key = stringVal.substring(0, splitter);
		String value = stringVal.substring(splitter+1); 
		PIRTE.sendPublishData(key, value);
	}

//	@Override
//	public void registerP2PContext(int fromPluginPortId, int toPluginPortId) {
//	}

}
