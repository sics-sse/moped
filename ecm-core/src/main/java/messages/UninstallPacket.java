package messages;

import java.util.ArrayList;

import messages.Packet;


// TODO: Auto-generated Javadoc
/**
 * The Class UninstallPackage.
 */
public class UninstallPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The uninstall package data list. */
	private ArrayList<UninstallPacketData> uninstallPacketDataList;

	/**
	 * Instantiates a new uninstall package.
	 */
	public UninstallPacket() {
	}

	/**
	 * Instantiates a new uninstall package.
	 * 
	 * @param vin
	 *            the vin
	 * @param uninstallPackageDataList
	 *            the uninstall package data list
	 */
	public UninstallPacket(String vin, ArrayList<UninstallPacketData> uninstallPacketDataList) {
		super(MessageType.UNINSTALL, vin);
		this.uninstallPacketDataList = uninstallPacketDataList;
	}

	public ArrayList<UninstallPacketData> getUninstallPacketDataList() {
		return uninstallPacketDataList;
	}

	public void setUninstallPacketDataList(ArrayList<UninstallPacketData> uninstallPacketDataList) {
		this.uninstallPacketDataList = uninstallPacketDataList;
	}

}
