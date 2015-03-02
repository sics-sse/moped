package messages;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class UninstallPackageData.
 */
public class UninstallPacketData implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int sendingPortID;
	private int callbackPortID;
	private int reference;

	private String pluginName;

	/**
	 * Instantiates a new uninstall package data.
	 */
	public UninstallPacketData() {
	}

	/**
	 * Instantiates a new uninstall package data.
	 * 
	 * @param ecuID
	 *            the ecu id
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public UninstallPacketData(int sendingPortID, int callbackPortID,
			int reference, String pluginName) {
		this.sendingPortID = sendingPortID;
		this.callbackPortID = callbackPortID;
		this.reference = reference;
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

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public int getReference() {
		return reference;
	}

	public void setReference(int reference) {
		this.reference = reference;
	}

}
