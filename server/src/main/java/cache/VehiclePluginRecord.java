package cache;

import java.util.ArrayList;
import java.util.HashMap;
import messages.LinkContextEntry;

public class VehiclePluginRecord {
	private String pluginName;
	private int ecuId;
	private int sendingPortId;
	private int callbackPortId;
	private HashMap<String, Integer> portInitialContext;
	private ArrayList<LinkContextEntry> linkingContext;
	private String location;
	private String executablePluginName;

	public VehiclePluginRecord(String pluginName, int ecuId,
			int sendingPortId, int callbackPortId,
			HashMap<String, Integer> portInitialContext,
			ArrayList<LinkContextEntry> linkingContext, String location,
			String executablePluginName) {
		super();
		this.pluginName = pluginName;
		this.ecuId = ecuId;
		this.sendingPortId = sendingPortId;
		this.callbackPortId = callbackPortId;
		this.portInitialContext = portInitialContext;
		this.linkingContext = linkingContext;
		this.location = location;
		this.executablePluginName = executablePluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public int getEcuId() {
		return ecuId;
	}

	public void setEcuId(int ecuId) {
		this.ecuId = ecuId;
	}

	public int getSendingPortId() {
		return sendingPortId;
	}

	public void setSendingPortId(int sendingPortId) {
		this.sendingPortId = sendingPortId;
	}

	public int getCallbackPortId() {
		return callbackPortId;
	}

	public void setCallbackPortId(int callbackPortId) {
		this.callbackPortId = callbackPortId;
	}

	public HashMap<String, Integer> getPortInitialContext() {
		return portInitialContext;
	}

	public void setPortInitialContext(
			HashMap<String, Integer> portInitialContext) {
		this.portInitialContext = portInitialContext;
	}

	public ArrayList<LinkContextEntry> getLinkingContext() {
		return linkingContext;
	}

	public void setLinkingContext(ArrayList<LinkContextEntry> linkingContext) {
		this.linkingContext = linkingContext;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getExecutablePluginName() {
		return executablePluginName;
	}

	public void setExecutablePluginName(String executablePluginName) {
		this.executablePluginName = executablePluginName;
	}

}
