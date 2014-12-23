package sics.port;

import fresta.pirte.PIRTE;
import sics.plugin.PlugInComponent;

public class PluginRPort implements PluginPort {
	private int id;
	private String name;
	private PlugInComponent plugin;
	private Object data;
	
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
		System.out.println("PluginRPort id:"+id);
		return plugin.getPirte().fetchVal(id);
	}
	
	public int readInt() {
		System.out.println("PluginRPort id:"+id);
		return plugin.getPirte().fetchIntVal(id);
	}

}
