package sics.plugin;

import java.util.Hashtable;

public abstract class PlugInComponent implements Runnable {
	private Hashtable<String, Integer> portInitContext = new Hashtable<String, Integer>();

	public PlugInComponent() {
	    System.out.println("api/PlugInComponent");
	}
	
	public PlugInComponent(String[] args) {
	    System.out.println("api/PlugInComponent");
	}

	public abstract void init();

	public int getPortId(String name) {
		return portInitContext.get(name);
	}

	public void initPortInitContext(String[] portInitContextArray) {
	    System.out.println("simulator/PlugInComponent initPortInitContext");
		for (int i = 0; i < portInitContextArray.length; i = i + 2) {
			portInitContext.put(portInitContextArray[i], Integer.parseInt(portInitContextArray[i + 1]));
		}	
	}

}
