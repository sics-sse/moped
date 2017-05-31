package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class UninstallAckPackage.
 */
public class UninstallAckPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String pluginName;

	/**
	 * Instantiates a new uninstall ack package.
	 */
	public UninstallAckPacket() {
	}

	/**
	 * Instantiates a new uninstall ack package.
	 * 
	 * @param vin
	 *            the vin
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public UninstallAckPacket(String vin, String pluginName) {
		super(MessageType.UNINSTALL_ACK, vin);
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}