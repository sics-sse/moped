package sics.port;

import sics.plugin.PlugInComponent;

public class PluginRPort implements PluginPort {
	private int id;
	private String name;
	private PlugInComponent plugin;

	public PluginRPort(PlugInComponent plugin, String name) {
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPlugInComponent(PlugInComponent plugin) {
		this.plugin = plugin;
	}

	public int readInt() {
		return 0;
	}

	public long readLong() {
		return 0;
	}

	public String readString() {
		return null;
	}

	public Object receive() {
		return null;
	}
}
