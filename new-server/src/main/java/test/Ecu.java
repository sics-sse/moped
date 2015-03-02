package test;

//import java.util.Set;
//import javax.persistence.CascadeType;
//import javax.persistence.Entity;
//import javax.persistence.FetchType;
//import javax.persistence.GeneratedValue;
//import javax.persistence.Id;
//import javax.persistence.ManyToOne;
//import javax.persistence.OneToMany;
//import javax.persistence.Table;

//@Entity
//@Table(name="Ecu")
public class Ecu {
	private int id;
	private String name;
//	private VehicleConfig vehicleConfig;
//	private Set<Port> ports;
	
	public Ecu() {}
	public Ecu(String name) {
		this.name = name;
	}

	public int getId() { return id;	}
	public void setId(int id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

//	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
//	public VehicleConfig getVehicleConfig() {
//		return vehicleConfig;
//	}
//
//	public void setVehicleConfig(VehicleConfig vehicleConfig) {
//		this.vehicleConfig = vehicleConfig;
//	}

//	@OneToMany(mappedBy = "ecu", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
//	public Set<Port> getPorts() {
//		return ports;
//	}
//
//	public void setPorts(Set<Port> ports) {
//		this.ports = ports;
//	}

	public boolean equals(Object obj) {
		if ((obj == null) || 
				(!this.getClass().equals(obj.getClass()))) { 
			return false;
		}

		Ecu ecu = (Ecu)obj;
		if((this.id == ecu.getId()) && 
				(this.name.equals(ecu.getName()))) {
			return true;
		}
		 
		return false;
   }
	
   public int hashCode() {
      return (id + name).hashCode();
   }
	
}
