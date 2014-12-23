package sics.port.instances;

import sics.PIRTE;
import sics.port.EcuVirtualPPort;

public class ReadSteerInPirteVirtualPPort extends EcuVirtualPPort {
	public ReadSteerInPirteVirtualPPort(int id) {
		super(id);
	}
	
	@Override
	public Object deliver() {
		return PIRTE.steer;
	}

	@Override
	public Object deliver(int portId) {
		// TODO Auto-generated method stub
		return null;
	}

}
