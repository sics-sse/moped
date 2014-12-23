package sics.messages;

// TODO: Auto-generated Javadoc
/**
 * The Class UninstallAckMessage.
 */
public class UninstallAckMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The vehicle plugin id. */
	private byte pluginId;

	/**
	 * Instantiates a new uninstall ack message.
	 */
	public UninstallAckMessage() {
	}

	/**
	 * Instantiates a new uninstall ack message.
	 * 
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public UninstallAckMessage(byte pluginId) {
		super(MessageType.UNINSTALL_ACK);
		this.pluginId = pluginId;
	}

	public byte getPluginId() {
		return pluginId;
	}

	public void setPluginId(byte pluginId) {
		this.pluginId = pluginId;
	}

}
