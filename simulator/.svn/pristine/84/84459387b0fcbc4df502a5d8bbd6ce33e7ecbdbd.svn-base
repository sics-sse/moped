package hw.instance;

import autosar.SWCRPort;
import hw.Actuator;

public class SpeedActuator extends Actuator {
	private SWCRPort<Integer> speedRPort;

	@Override
	public void run() {
		while(true) {
			await();
			
			Integer data = speedRPort.read();
			System.out.println("Speed:"+data);
		}
	}
	
}
