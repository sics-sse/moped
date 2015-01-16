package sics.port;

import sics.plugin.PlugInComponent;

public class PluginRPort implements PluginPort {
	private int id;
	private String name;
	private PlugInComponent plugin;
	private Object data;

	public PluginRPort(PlugInComponent plugin, String name) {
		this(plugin, name, null);
	}
	
	public PluginRPort(PlugInComponent plugin, String name, Object data) {
		this.plugin = plugin;
		this.name = name;
		this.data = data;
		id = plugin.getPortId(name);
	}

	// public PluginRPort(String name, Object data) {
	// this.name = name;
	// }

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

	public String readString() {
		return null;
	}

	private int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}
}
