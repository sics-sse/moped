package dao;

import model.Vehicle;
import common.AllocationStrategy;

public interface VehicleDao {
	public void removeOneAppId(String vin, int uninstalledAppId);
	public byte generateEcuId(String vin, AllocationStrategy allocationStrategy, byte ecuReference);
	public Vehicle getVehicle(String vin);
	public void addApp(String vin, int appId);
	public void saveVehicle(Vehicle vehicle);
}

