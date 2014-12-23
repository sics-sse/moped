package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class UninstallAckMessage.
 */
public class UninstallAckMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The vehicle plugin id. */
	private String pluginName;

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
	public UninstallAckMessage(String pluginName) {
		super(MessageType.UNINSTALL_ACK);
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}
