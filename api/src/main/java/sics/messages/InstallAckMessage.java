package sics.messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckMessage.
 */
public class InstallAckMessage extends Message {

	private String pluginName;

	/**
	 * Instantiates a new install ack message.
	 * 
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public InstallAckMessage(String pluginName) {
		super(MessageType.INSTALL_ACK);
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}
