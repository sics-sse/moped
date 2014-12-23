package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class PluginCommunicationMessage.
 */
public class PluginMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The plugin address. */
	private int remotePortId;

	/** The value. */
	private Object value;

	/**
	 * Instantiates a new plugin communication message.
	 * 
	 * @param messageType
	 *            the message type
	 * @param pluginAddress
	 *            the plugin address
	 * @param value
	 *            the value
	 */
	public PluginMessage(int remotePortId, Object value) {
		super(MessageType.PLUGIN_MESSAGE);
		this.remotePortId = remotePortId;
		this.value = value;
	}

	public int getRemotePortId() {
		return remotePortId;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 * 
	 * @param value
	 *            the new value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
}
