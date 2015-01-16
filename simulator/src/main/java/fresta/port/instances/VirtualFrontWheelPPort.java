package fresta.port.instances;

import sics.port.EcuVirtualPPort;
import gui.CarModel;

public class VirtualFrontWheelPPort extends EcuVirtualPPort {
	private int id;
	
	public VirtualFrontWheelPPort(int id) {
		super(id);
	}
	
	@Override
	public Object deliver() {
		return (int)CarModel.vehicleSpeed;
	}

	@Override
	public Object deliver(int portId) {
		return new Integer((int)(100.0 * CarModel.vehicleSpeed));  // Unit: cm/s
	}

}
