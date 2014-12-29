package model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Vehicle")
public class Vehicle implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String VIN;
	private String name;
	private String INSTALLED_APPS;
	// private Set<Ecu> ecus = new HashSet<Ecu>();
	// private Set<VehiclePlugin> vehiclePlugins = new HashSet<VehiclePlugin>();
	private int vehicleConfigId;

	public Vehicle() {
	}

	public Vehicle(String VIN, String name) {
		this.VIN = VIN;
		this.name = name;
		INSTALLED_APPS = "";
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

	@Column(length = 17)
	public String getVIN() {
		return VIN;
	}

	public void setVIN(String vIN) {
		VIN = vIN;
	}

	// @OneToMany(mappedBy = "vehicle", cascade = { CascadeType.ALL }, fetch =
	// FetchType.EAGER)
	// public Set<Ecu> getEcus() {
	// return ecus;
	// }
	//
	// public void setEcus(Set<Ecu> ecus) {
	// this.ecus = ecus;
	// }

	// @OneToMany(mappedBy = "vehicle", cascade = { CascadeType.ALL }, fetch =
	// FetchType.EAGER)
	// public Set<VehiclePlugin> getVehiclePlugins() {
	// return vehiclePlugins;
	// }
	//
	// public void setVehiclePlugins(Set<VehiclePlugin> vehiclePlugins) {
	// this.vehiclePlugins = vehiclePlugins;
	// }

	public String getINSTALLED_APPS() {
		return INSTALLED_APPS;
	}

	public void setINSTALLED_APPS(String iNSTALLED_APPS) {
		INSTALLED_APPS = iNSTALLED_APPS;
	}

	public int getVehicleConfigId() {
		return vehicleConfigId;
	}

	public void setVehicleConfigId(int vehicleConfigId) {
		this.vehicleConfigId = vehicleConfigId;
	}

	public void removeApp(int appId) {
		String appIdStr = appId + "";
		if (!INSTALLED_APPS.contains(",") && appIdStr.equals(INSTALLED_APPS)) {
			INSTALLED_APPS = "";
		} else {
			String[] split = INSTALLED_APPS.split(",");
			String result = "";
			for (int i = 0; i < split.length; i++) {
				if (!split[i].equals(appIdStr) && !split[i].equals("")) {
					result += split[i];
					result += ",";
				}
			}
			INSTALLED_APPS = result;
		}

	}
}
