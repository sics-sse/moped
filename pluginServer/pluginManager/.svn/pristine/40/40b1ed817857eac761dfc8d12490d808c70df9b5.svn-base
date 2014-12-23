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
@Table(name = "PluginConfig")
public class PluginConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private int ecuId;
	private Set<PluginPortConfig> pluginPortConfigs;
	private Set<PluginLinkConfig> pluginLinkConfigs;
	private AppConfig appConfig;

	public PluginConfig() {

	}

	public PluginConfig(String name, byte ecuId, AppConfig appConfig) {
		this.name = name;
		this.ecuId = ecuId;
		this.appConfig = appConfig;
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

	public int getEcuId() {
		return ecuId;
	}

	public void setEcuId(int ecuId) {
		this.ecuId = ecuId;
	}

	@OneToMany(mappedBy = "pluginConfig", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<PluginPortConfig> getPluginPortConfigs() {
		return pluginPortConfigs;
	}

	public void setPluginPortConfigs(Set<PluginPortConfig> pluginPortConfigs) {
		this.pluginPortConfigs = pluginPortConfigs;
	}

	@OneToMany(mappedBy = "pluginConfig", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<PluginLinkConfig> getPluginLinkConfigs() {
		return pluginLinkConfigs;
	}

	public void setPluginLinkConfigs(Set<PluginLinkConfig> pluginLinkConfigs) {
		this.pluginLinkConfigs = pluginLinkConfigs;
	}

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public AppConfig getAppConfig() {
		return appConfig;
	}

	public void setAppConfig(AppConfig appConfig) {
		this.appConfig = appConfig;
	}

}
