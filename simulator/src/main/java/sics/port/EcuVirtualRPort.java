package sics.port;

import java.util.ArrayList;

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

	@Override
	public ArrayList<Integer> getConnectedPluginPorts() {
	    return connectedPluginPorts;
	}

	@Override
	public void addConnectedPluginPort(int portId) {
	    connectedPluginPorts.add(portId);
	}
}
