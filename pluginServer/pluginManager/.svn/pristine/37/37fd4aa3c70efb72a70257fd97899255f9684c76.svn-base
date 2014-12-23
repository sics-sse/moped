package messages;

import java.util.ArrayList;

public class InstallPacket extends Packet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// InstallPacketData
	private ArrayList<InstallPacketData> installPacketDataList;

	public InstallPacket() {
	}

	public InstallPacket(String vin,
			ArrayList<InstallPacketData> installPacketDataList) {
		super(MessageType.INSTALL, vin);
		this.installPacketDataList = installPacketDataList;
	}

	public ArrayList<InstallPacketData> getInstallPacketDataList() {
		return installPacketDataList;
	}

	public void setInstallMessageDataList(
			ArrayList<InstallPacketData> installPacketDataList) {
		this.installPacketDataList = installPacketDataList;
	}
}