package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckPackage.
 */
public class RestoreAckPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String pluginName;

	/**
	 * Instantiates a new install ack package.
	 */
	public RestoreAckPacket() {
	}

	/**
	 * Instantiates a new install ack package.
	 * 
	 * @param vin
	 *            the vin
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public RestoreAckPacket(String vin, String pluginName) {
		super(MessageType.RESTORE_ACK, vin);
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}