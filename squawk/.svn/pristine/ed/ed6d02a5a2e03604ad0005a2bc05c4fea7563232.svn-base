package sics.port.instances;

import com.sun.squawk.VM;

import sics.port.EcuVirtualRPort;

//TODO: Make this class generic (so that data doesn't need casting)
public class VirtualLedRPort extends EcuVirtualRPort {

	public VirtualLedRPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		String dataStr = (String) data;
		int separator = dataStr.indexOf('|');
		String pinStr = dataStr.substring(0, separator);
		int pin = Integer.parseInt(pinStr);
		String valStr = dataStr.substring(separator + 1);
		int val = Integer.parseInt(valStr);
		VM.jnaSetLED(pin, val);
	}

//	@Override
//	public void registerP2PContext(int fromPluginPortId, int toPluginPortId) {
//	}

}
