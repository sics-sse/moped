package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class UninstallMessage.
 */
public class UninstallMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int remoteEcuId;
	private byte pluginId;
	private String pluginName;
	private int callbackPortID;

	/**
	 * Instantiates a new uninstall message.
	 */
	public UninstallMessage() {
	}

	/**
	 * Instantiates a new uninstall message.
	 * 
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public UninstallMessage(int remoteEcuId, byte pluginId, String pluginName,
			int callbackPortID) {
		super(MessageType.UNINSTALL);
		this.remoteEcuId = remoteEcuId;
		this.pluginId = pluginId;
		this.pluginName = pluginName;
		this.callbackPortID = callbackPortID;
	}

	public int getRemoteEcuId() {
		return remoteEcuId;
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

	public int getCallbackPortID() {
		return callbackPortID;
	}

	public void setCallbackPortID(int callbackPortID) {
		this.callbackPortID = callbackPortID;
	}

}
