package fresta.port.instances;

import sics.port.EcuVirtualPPort;
import gui.CarModel;

public class VirtualRearWheelPPort extends EcuVirtualPPort {
	
	public VirtualRearWheelPPort(int id) {
		super(id);
	}
	
	@Override
	public Object deliver() {
		return (int)(Math.abs(100 * CarModel.vehicleSpeed));
	}

	@Override
	public Object deliver(int portId) {
		return new Integer(Math.abs((int)(100.0 * CarModel.vehicleSpeed)));  // Unit: cm/s, absolute value
	}

}
