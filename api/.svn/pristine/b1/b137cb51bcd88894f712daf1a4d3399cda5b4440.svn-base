package sics.plugin;

import java.util.Hashtable;

public abstract class PlugInComponent {
	private Hashtable<String, Integer> portInitContext = new Hashtable<String, Integer>();
	
	public PlugInComponent(String[] args) {
		for (int i = 0; i < args.length; i = i + 2) {
			portInitContext.put(args[i], Integer.parseInt(args[i + 1]));
		}
	}
	
	public abstract void init();
	
	public int getPortId(String portName) {
		return portInitContext.get(portName);
	}
}
