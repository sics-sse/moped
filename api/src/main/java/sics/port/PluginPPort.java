package sics.port;

import sics.plugin.PlugInComponent;

public class PluginPPort implements PluginPort {
	private int id;
	private String name;
	private PlugInComponent plugin;

	public PluginPPort(PlugInComponent plugin, String name) {
		this.plugin = plugin;
		this.name = name;
		id = plugin.getPortId(name);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public PlugInComponent getPlugin() {
		return plugin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPlugInComponent(PlugInComponent plugin) {
		this.plugin = plugin;
	}

	public void write(int value) {
	}

	public void write(String value) {
	}

	public void send(String value) {
	}
}
