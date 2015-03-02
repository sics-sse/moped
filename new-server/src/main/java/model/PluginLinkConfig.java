package model;

import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class PluginLinkConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String fromStr;
	private String toStr;
	private String remote;
	private PluginConfig pluginConfig;

	public PluginLinkConfig() {
	}

	public PluginLinkConfig(String fromStr, String toStr, String remote) {
		this.fromStr = fromStr;
		this.toStr = toStr;
		this.remote = remote;
	}
	
	public PluginLinkConfig(String fromStr, String toStr, int connectionType) {
		this(fromStr, toStr, ""+connectionType);
	}

	@Id
	@GeneratedValue
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFromStr() {
		return fromStr;
	}

	public void setFromStr(String fromStr) {
		this.fromStr = fromStr;
	}

	public String getToStr() {
		return toStr;
	}

	public void setToStr(String toStr) {
		this.toStr = toStr;
	}

	public String getRemote() {
		return remote;
	}

	public void setRemote(String remote) {
		this.remote = remote;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public PluginConfig getPluginConfig() {
		return pluginConfig;
	}

	public void setPluginConfig(PluginConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

}
