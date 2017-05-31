package messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallPackageData.
 */
public class InstallPacketData implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int appId;
	/** The plugin name. */
	private String pluginName;

	private int sendingPortID;
	private int callbackPortID;

	private int reference;

	/** The portIntialContex. */
	// key: portName(String), value: portId(Integer)
	private HashMap<String, Integer> portInitialContext;
	// LinkContextEntry
	private ArrayList<LinkContextEntry> portLinkingContext;

	private String executablePluginName;

	/** The binary file. */
	private byte[] binaryFile;

	// must have default constructor
	/**
	 * Instantiates a new install package data.
	 */
	public InstallPacketData() {
	}

	/**
	 * Instantiates a new install package data.
	 * 
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 * @param pluginName
	 *            the plugin name
	 * @param reference
	 *            the reference
	 * @param context
	 *            the context
	 * @param binaryFile
	 *            the binary file
	 */
	public InstallPacketData(int appId, String pluginName, int sendingPortID,
			int callbackPortID, int reference,
			HashMap<String, Integer> portInitialContext,
			ArrayList<LinkContextEntry> portLinkingContext,
			String executablePluginName, byte[] binaryFile) {
		this.appId = appId;
		this.pluginName = pluginName;
		this.sendingPortID = sendingPortID;
		this.callbackPortID = callbackPortID;
		this.reference = reference;
		this.portInitialContext = portInitialContext;
		this.portLinkingContext = portLinkingContext;
		this.executablePluginName = executablePluginName;
		this.binaryFile = binaryFile;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	/**
	 * Gets the plugin name.
	 * 
	 * @return the plugin name
	 */
	public String getPluginName() {
		return pluginName;
	}

	/**
	 * Sets the plugin name.
	 * 
	 * @param pluginName
	 *            the new plugin name
	 */
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
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

	/**
	 * Gets the binary file.
	 * 
	 * @return the binary file
	 */
	public byte[] getBinaryFile() {
		return binaryFile;
	}

	/**
	 * Sets the binary file.
	 * 
	 * @param binaryFile
	 *            the new binary file
	 */
	public void setBinaryFile(byte[] binaryFile) {
		this.binaryFile = binaryFile;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int reference) {
		this.reference = reference;
	}

	public String getExecutablePluginName() {
		return executablePluginName;
	}

	public void setExecutablePluginName(String executablePluginName) {
		this.executablePluginName = executablePluginName;
	}

}
