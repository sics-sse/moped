package model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "DatabasePlugin")
public class DatabasePlugin implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private String zipName;
	private String fullClassName;
	private String strategy;
	private int reference;
	private String location;
	private String zipLocation;
	private Application application;

	// private Set<VehiclePlugin> vehiclePlugins = new HashSet<VehiclePlugin>();

	public DatabasePlugin() {
	}

	public DatabasePlugin(String name, String zipName, String fullClassName,
			String strategy, int reference, String location, String zipLocation) {
		this.name = name;
		this.name = name;
		this.fullClassName = fullClassName;
		this.strategy = strategy;
		this.reference = reference;
		this.location = location;
		this.zipLocation = zipLocation;
	}

	@Id
	@GeneratedValue
	@Column(name = "pluginID")
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

	public String getZipName() {
		return zipName;
	}

	public void setZipName(String zipName) {
		this.zipName = zipName;
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public void setFullClassName(String fullClassName) {
		this.fullClassName = fullClassName;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int reference) {
		this.reference = reference;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public String getZipLocation() {
		return zipLocation;
	}

	public void setZipLocation(String zipLocation) {
		this.zipLocation = zipLocation;
	}

	// @OneToMany(mappedBy = "databasePlugin", cascade = { CascadeType.ALL },
	// fetch = FetchType.EAGER)
	// public Set<VehiclePlugin> getVehiclePlugins() {
	// return vehiclePlugins;
	// }
	//
	// public void setVehiclePlugins(Set<VehiclePlugin> vehiclePlugins) {
	// this.vehiclePlugins = vehiclePlugins;
	// }

}
