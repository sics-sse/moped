package fresta.port.instances;

import sics.port.EcuVirtualPPort;
import gui.CarModel;

public class VirtualVoltagePPort extends EcuVirtualPPort {
	
	public VirtualVoltagePPort(int id) {
		super(id);
	}
	
	@Override
	public Object deliver() {
		return (int)CarModel.voltage;
	}

	@Override
	public Object deliver(int portId) {
		return new Integer((int)(CarModel.voltage));
	}
}
