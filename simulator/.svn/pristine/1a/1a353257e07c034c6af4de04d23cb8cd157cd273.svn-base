package hw;

import java.util.HashMap;

import autosar.SWC;

public class Ecu implements BaseEUnit {
	// ECU ID
	private int id;
	// Collection of SWC
	private HashMap<Integer, SWC> swcs = new HashMap<Integer, SWC>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addSWC(SWC swc) {
		swcs.put(swc.getId(), swc);
	}

	@Override
	public void wakeup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void await() {
		// TODO Auto-generated method stub
		
	}
}
