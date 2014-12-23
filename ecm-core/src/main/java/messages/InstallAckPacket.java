package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckPackage.
 */
public class InstallAckPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int appId;
	
	private String pluginName;

	/**
	 * Instantiates a new install ack package.
	 */
	public InstallAckPacket() {
	}

	/**
	 * Instantiates a new install ack package.
	 * 
	 * @param vin
	 *            the vin
	 * @param vehiclePluginID
	 *            the vehicle plugin id
	 */
	public InstallAckPacket(String vin, int appId, String pluginName) {
		super(MessageType.INSTALL_ACK, vin);
		this.appId = appId;
		this.pluginName = pluginName;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

}