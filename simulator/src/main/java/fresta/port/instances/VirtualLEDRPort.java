package fresta.port.instances;

import gui.CarModel;
import sics.port.EcuVirtualRPort;

public class VirtualLEDRPort extends EcuVirtualRPort {

	public VirtualLEDRPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		String dataStr = (String)data;
		int barIndex = dataStr.indexOf('|');
		String pin = dataStr.substring(0, barIndex);
		String value = dataStr.substring(barIndex+1);
		
		CarModel.lamp[Integer.parseInt(pin)-1] = (value == "1");
		
		// Currently, this method just prints the value of the LED pin.
		// TODO: Show the LEDs in the graphical GUI.
		System.out.println("LED " + pin + " = " + value);
	}
	
}
