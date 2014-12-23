package sics.port;

import sics.ArrayList;

public interface VirtualRPort extends VirtualPort {
	public void deliver(Object data);
//	public void registerP2PContext(int fromPluginPortId, int toPluginPortId);
	public ArrayList<Integer> getConnectedPluginPorts();
	public void addConnectedPluginPort(int portId);
}
