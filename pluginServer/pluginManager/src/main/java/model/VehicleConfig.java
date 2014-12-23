package model;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "VehicleConfig")
public class VehicleConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String brand;
	private Set<Link> links;
	private Set<Ecu> ecus;

	public VehicleConfig() {
	}

	public VehicleConfig(String name, String brand) {
		this.name = name;
		this.brand = brand;
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	@OneToMany(mappedBy = "vehicleConfig", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<Ecu> getEcus() {
		return ecus;
	}

	public void setEcus(Set<Ecu> ecus) {
		this.ecus = ecus;
	}

	@OneToMany(mappedBy = "vehicleConfig", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<Link> getLinks() {
		return links;
	}

	public void setLinks(Set<Link> links) {
		this.links = links;
	}

}
