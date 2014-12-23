package sics.messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckMessage.
 */
public class InstallAckMessage extends Message {

	private byte pluginId;

	/**
	 * Instantiates a new install ack message.
	 * 
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public InstallAckMessage(byte pluginId) {
		super(MessageType.INSTALL_ACK);
		this.pluginId = pluginId;
	}

	public byte getPluginId() {
		return pluginId;
	}

	public void setPluginId(byte pluginId) {
		this.pluginId = pluginId;
	}

}
