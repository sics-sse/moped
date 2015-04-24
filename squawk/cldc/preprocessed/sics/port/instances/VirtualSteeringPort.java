package sics.port.instances;

import com.sun.squawk.VM;

import sics.port.EcuVirtualRPort;

public class VirtualSteeringPort extends EcuVirtualRPort {

	public VirtualSteeringPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		VM.jnaSendSteerPwmData(((Integer)data).intValue());
	}

//	@Override
//	public void registerP2PContext(int fromPluginPortId, int toPluginPortId) {
//	}

}
