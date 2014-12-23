package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckMessage.
 */
public class InstallAckMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private byte pluginId;
	private String pluginName;

	/**
	 * Instantiates a new install ack message.
	 * 
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public InstallAckMessage(byte pluginId, String pluginName) {
		super(MessageType.INSTALL_ACK);
		this.pluginId = pluginId;
		this.pluginName = pluginName;
	}

	public byte getPluginId() {
		return pluginId;
	}

	public void setPluginId(byte pluginId) {
		this.pluginId = pluginId;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}
