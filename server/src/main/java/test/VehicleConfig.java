package test;

//import java.util.HashSet;
import java.util.Set;

public class VehicleConfig {
	private int id;
	private String model;
	private String version;
	private Set<Ecu> ecus; // = new HashSet<Ecu>();

	public VehicleConfig() {}
	public VehicleConfig(String model, String version) {
		this.model = model;
		this.version = version;
	}

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	public String getModel() { return model; }
	public void setModel(String model) { this.model = model; }
	public String getVersion() { return version; }
	public void setVersion(String version) { this.version = version; }
	public Set<Ecu> getEcus() { return ecus; }
	public void setEcus(Set<Ecu> ecus) { this.ecus = ecus; }

}
