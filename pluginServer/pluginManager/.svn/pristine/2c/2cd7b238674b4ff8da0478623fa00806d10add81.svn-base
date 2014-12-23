package model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class AppConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private int appId;
	private String vehicleName;
	private String brand;
	private Set<PluginConfig> pluginConfigs = new HashSet<PluginConfig>();

	public AppConfig() {
	}

	public AppConfig(int appId, String vehicleName, String brand) {
		this.appId = appId;
		this.vehicleName = vehicleName;
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

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getVehicleName() {
		return vehicleName;
	}

	public void setVehicleName(String vehicleName) {
		this.vehicleName = vehicleName;
	}

	@OneToMany(mappedBy = "appConfig", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<PluginConfig> getPluginConfigs() {
		return pluginConfigs;
	}

	public void setPluginConfigs(Set<PluginConfig> pluginConfigs) {
		this.pluginConfigs = pluginConfigs;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

}
