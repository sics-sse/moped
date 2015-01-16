package dao;

import java.util.List;

import model.Ecu;
import model.Link;
import model.Port;
import model.VehicleConfig;

public interface VehicleConfigDao {
	public VehicleConfig getVehicleConfig(int vehicleConfigId);
	public VehicleConfig getVehicleConfig(String vehicle, String brand);
	public void savePort(Port port);
	public void saveEcu(Ecu ecu);
	public void saveLink(Link link);
	public void saveVehicleConfig(VehicleConfig vehicleConfig);
//	public void newSaveVehicleConfig(String model, String version, List<String> ecuNames); //TODO: AK_new
//	public LinkedList<LinkingContextEntry> getLinkingContext(String vehicleName, String brandName, byte ecuId, String portName, int remotePluginPortId);
//	public byte getVPortId(String vehicleName, String brandName, byte ecuId, String portName);
	public int getSendingPortId(int vehicleConfigId, int recipientEcu);
	public int getCallbackPortId(int vehicleConfigId, int sendingEcu);
	public int[] getType2PortId(String vehicle, String brand, int fromEcuId, int toEcuId);
	//public int getType2RPortId(String vehicle, String brand, int remoteEcuId);
}

