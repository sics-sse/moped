package fresta.port.instances;

import gui.CarModel;
import sics.port.EcuVirtualRPort;

public class VirtualSteeringRPort extends EcuVirtualRPort {

	public VirtualSteeringRPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		CarModel.steeringAngle = ((int) data) / 100.0;
	}
}