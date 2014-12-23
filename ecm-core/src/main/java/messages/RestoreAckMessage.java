package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckMessage.
 */
public class RestoreAckMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String pluginName;

	/**
	 * Instantiates a new install ack message.
	 * 
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public RestoreAckMessage(String pluginName) {
		super(MessageType.RESTORE_ACK);
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}
