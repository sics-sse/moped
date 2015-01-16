package fresta.port.instances;

import gui.CarModel;
import sics.port.EcuVirtualRPort;

public class VirtualAcceleratorRPort extends EcuVirtualRPort {

	public VirtualAcceleratorRPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		CarModel.motorPower = ((int) data) / 100.0;
	}
}
