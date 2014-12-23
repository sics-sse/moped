package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class PluginCommunicationMessage.
 */
public class PWMMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int remoteEcuId;

	private byte[] data;

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
	public PWMMessage(int remoteEcuId, byte[] data) {
		super(MessageType.PWM);
		this.remoteEcuId = remoteEcuId;
		this.data = data;
	}

	public int getRemoteEcuId() {
		return remoteEcuId;
	}

	public void setRemoteEcuId(int remoteEcuId) {
		this.remoteEcuId = remoteEcuId;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
