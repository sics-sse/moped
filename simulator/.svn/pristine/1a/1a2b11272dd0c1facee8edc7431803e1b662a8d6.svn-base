package hw.instance;

import autosar.SWCRPort;
import hw.Actuator;

public class SteerActuator extends Actuator {
	private SWCRPort<Integer> steerRPort;
	
	@Override
	public void run() {
		while(true) {
			await();
			
			Integer data = steerRPort.read();
			System.out.println("Steer:"+data);
		}
	}
}
