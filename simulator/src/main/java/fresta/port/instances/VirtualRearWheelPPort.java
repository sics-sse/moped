package fresta.port.instances;

import sics.port.EcuVirtualPPort;
import gui.CarModel;

public class VirtualRearWheelPPort extends EcuVirtualPPort {
	private int id;
	
	public VirtualRearWheelPPort(int id) {
		super(id);
	}
	
	@Override
	public Object deliver() {
		return (int)CarModel.rearWheelSpeed;
	}

	@Override
	public Object deliver(int portId) {
		// TODO Auto-generated method stub
		return null;
	}

}
