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

	public Object readData() {
		return data;
	}

	public Object read() {
		return plugin.getPirte().fetchVal(id);
	}

	public Object receive() {
		return plugin.getPirte().fetchVal(id);
	}

	public int readInt() {
		return plugin.getPirte().fetchIntVal(id);
	}

	public long readLong() {
		return plugin.getPirte().fetchLongVal(id);
	}

	public String readString() {
		return plugin.getPirte().fetchStringVal(id);
	}

}
