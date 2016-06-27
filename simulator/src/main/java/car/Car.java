package car;

import ecm.Ecm;

public class Car {
	private String name;
	private String brand;
	private Ecm ecm;
	
	public void init(String [] args) {
		ecm.start(args);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Ecm getEcm() {
		return ecm;
	}
	
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}
	
}