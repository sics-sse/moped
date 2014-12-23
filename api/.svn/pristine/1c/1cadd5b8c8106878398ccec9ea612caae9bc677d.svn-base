package sics.port;

import sics.ArrayList;
import sics.PIRTE;

public abstract class EcuVirtualPPort implements VirtualPPort {
	private int id;
	private PIRTE pirte;
	private ArrayList<PluginRPort> pluginRPorts = new ArrayList<PluginRPort>();
	
	public EcuVirtualPPort(int id) {
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
	public void setPirte(PIRTE pirte) {
		this.pirte = pirte;
	}

	@Override
	public void addConnectedRPort(PluginRPort rport) {
		pluginRPorts.add(rport);
	}

}
