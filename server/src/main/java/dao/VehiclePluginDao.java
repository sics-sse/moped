package dao;

import java.util.List;

import cache.VehiclePluginRecord;

import model.VehiclePlugin;

public interface VehiclePluginDao {
	public int getApplicationId(String VIN, String pluginName);
	public VehiclePlugin getVehiclePlugin(String VIN, String pluginName);
	public List<VehiclePlugin> getVehilePlugins(String vin, int appId);
//	public short generateVehiclePluginID(String vin);
	public void saveVehiclePlugin(VehiclePlugin vehiclePlugin);
	public void saveVehiclePlugin(String vin, int appId, VehiclePluginRecord record);
//	public void setIsInstallFlag(String vin, String vehiclePluginName);
	public void removeVehiclePlugin(String vin, String vehiclePluginName);
	public List<VehiclePlugin> getPluginsInSpecificEcu(String vin, int ecuReference);
}

