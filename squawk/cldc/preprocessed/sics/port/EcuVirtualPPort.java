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

	public int byteArrayToInt(byte[] b) {
		return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16
				| (b[0] & 0xFF) << 24;
	}
}
