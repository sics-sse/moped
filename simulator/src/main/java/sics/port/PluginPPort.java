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

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	public void write(int value) {
		plugin.getPirte().deliverValue(id, value);
	}

	public void write(String value) {
		plugin.getPirte().deliverValue(id, value);
	}
}
