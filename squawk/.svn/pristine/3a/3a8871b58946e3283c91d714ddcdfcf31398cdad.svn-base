package sics.port.instances;

import com.sun.squawk.VM;

import sics.port.EcuVirtualRPort;

//TODO: Make this class generic (so that data doesn't need casting)
public class VirtualSpeedPort extends EcuVirtualRPort {

	public VirtualSpeedPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		VM.jnaSendSpeedPwmData(((Integer)data).intValue());
	}

//	@Override
//	public void registerP2PContext(int fromPluginPortId, int toPluginPortId) {
//	}

}
