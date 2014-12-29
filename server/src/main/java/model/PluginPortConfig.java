package model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class PluginPortConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String name;
	private PluginConfig pluginConfig;

	public PluginPortConfig() {
	}

	public PluginPortConfig(String name) {
		this.name = name;
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

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}

	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

}
