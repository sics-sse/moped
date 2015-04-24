package sics.port;

import sics.ArrayList;

public abstract class EcuVirtualRPort implements VirtualRPort {
	private int id;
	private ArrayList<Integer> connectedPluginPorts = new ArrayList<Integer>();

	public EcuVirtualRPort(int id) {
		this.id = id;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<Integer> getConnectedPluginPorts() {
	    return connectedPluginPorts;
	}

	public void addConnectedPluginPort(int portId) {
	    connectedPluginPorts.add(portId);
	}
}
