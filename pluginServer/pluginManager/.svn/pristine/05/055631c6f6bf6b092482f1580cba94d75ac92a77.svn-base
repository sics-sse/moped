package messages;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class RestorePackage.
 */
public class RestorePacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The install message data list. */
	private ArrayList<InstallPacketData> installMessageDataList;
	
	/**
	 * Instantiates a new restore package.
	 */
	public RestorePacket() {}
	
	/**
	 * Instantiates a new restore package.
	 *
	 * @param vin the vin
	 * @param installMessageDataList the install message data list
	 */
	public RestorePacket(String vin, ArrayList<InstallPacketData> installMessageDataList) {
		super(MessageType.RESTORE, vin);
		this.installMessageDataList = installMessageDataList;
	}

	/**
	 * Gets the install message data list.
	 *
	 * @return the install message data list
	 */
	public ArrayList<InstallPacketData> getInstallMessageDataList() {
		return installMessageDataList;
	}

	/**
	 * Sets the install message data list.
	 *
	 * @param installMessageDataList the new install message data list
	 */
	public void setInstallMessageDataList(
			ArrayList<InstallPacketData> installMessageDataList) {
		this.installMessageDataList = installMessageDataList;
	}
}