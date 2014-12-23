package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckPackage.
 */
public class InstallLinuxAckPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String pluginName;

	/**
	 * Instantiates a new install ack package.
	 */
	public InstallLinuxAckPacket() {
	}

	/**
	 * Instantiates a new install ack package.
	 * 
	 * @param vin
	 *            the vin
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public InstallLinuxAckPacket(String vin, String pluginName) {
		super(MessageType.INSTALL_LINUX_ACK, vin);
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}