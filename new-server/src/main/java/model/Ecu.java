package model;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="Ecu")
public class Ecu implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int ecuId;
	private String name;
	private String function;
	private String manufactory;
	private String description;
	private VehicleConfig vehicleConfig;
	private Set<Port> ports;
	
	public Ecu() {
	}
	
	public Ecu(String name) {
		this.name = name;
	}

	public Ecu(int ecuId, String name, String function, String manufactory,
			String description) {
		this.ecuId = ecuId;
		this.name = name;
		this.function = function;
		this.manufactory = manufactory;
		this.description = description;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEcuId() {
		return ecuId;
	}

	public void setEcuId(int ecuId) {
		this.ecuId = ecuId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public String getManufactory() {
		return manufactory;
	}

	public void setManufactory(String manufactory) {
		this.manufactory = manufactory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public VehicleConfig getVehicleConfig() {
		return vehicleConfig;
	}

	public void setVehicleConfig(VehicleConfig vehicleConfig) {
		this.vehicleConfig = vehicleConfig;
	}

	@OneToMany(mappedBy = "ecu", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<Port> getPorts() {
		return ports;
	}

	public void setPorts(Set<Port> ports) {
		this.ports = ports;
	}

	
}
