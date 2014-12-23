package db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import messages.LinkContextEntry;

// TODO: Auto-generated Javadoc
/**
 * The Class DataRecord.
 */
public class DataRecord implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int appId;
	private int remoteEcuId;
	private int sendingPortID;
	private int callbackPortID;
	private String pluginName;
	private String executablePluginName;

	/** The location. */
	private String location;

	/** The plug-in context. */
	// key: portName(String), value: portId(Integer)
	private HashMap<String, Integer> portInitialContext;
	// LinkingContextEntry
	private ArrayList<LinkContextEntry> portLinkingContext;

	public DataRecord(int appId, int remoteEcuId, int sendingPortID,
			int callbackPortID, String pluginName, String executablePluginName,
			String location, HashMap<String, Integer> portInitialContext,
			ArrayList<LinkContextEntry> portLinkingContext) {
		this.appId = appId;
		this.remoteEcuId = remoteEcuId;
		this.sendingPortID = sendingPortID;
		this.callbackPortID = callbackPortID;
		this.pluginName = pluginName;
		this.executablePluginName = executablePluginName;
		this.location = location;
		this.portInitialContext = portInitialContext;
		this.portLinkingContext = portLinkingContext;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public int getRemoteEcuId() {
		return remoteEcuId;
	}

	public int getSendingPortID() {
		return sendingPortID;
	}

	public void setSendingPortID(int sendingPortID) {
		this.sendingPortID = sendingPortID;
	}

	public int getCallbackPortID() {
		return callbackPortID;
	}

	public void setCallbackPortID(int callbackPortID) {
		this.callbackPortID = callbackPortID;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public String getExecutablePluginName() {
		return executablePluginName;
	}

	public void setExecutablePluginName(String executablePluginName) {
		this.executablePluginName = executablePluginName;
	}

	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * Sets the location.
	 * 
	 * @param location
	 *            the new location
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	public HashMap<String, Integer> getPortInitialContext() {
		return portInitialContext;
	}

	public void setPortInitialContext(
			HashMap<String, Integer> portInitialContext) {
		this.portInitialContext = portInitialContext;
	}

	public ArrayList<LinkContextEntry> getPortLinkingContext() {
		return portLinkingContext;
	}

	public void setPortLinkingContext(
			ArrayList<LinkContextEntry> portLinkingContext) {
		this.portLinkingContext = portLinkingContext;
	}

}
