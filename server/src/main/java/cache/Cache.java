package cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Cache {
	private static Cache instance;
	private final static int CACHE_MAX_SIZE = 50;
	// key: VIN, value: {key: Plug-In name, value: vehiclePluginRecord}
	// private LinkedHashMap<String, HashMap<Integer, HashMap<String,
	// VehiclePluginRecord>>> installCache;
	// key: VIN, value: List<plugIn name>
	private LinkedHashMap<String, HashMap<Integer, ArrayList<String>>> uninstallCache;

	private LinkedHashMap<String, HashMap<Integer, ArrayList<VehiclePluginRecord>>> installCache;

	@SuppressWarnings("serial")
	private Cache() {
		this.installCache = new LinkedHashMap<String, HashMap<Integer, ArrayList<VehiclePluginRecord>>>(
				CACHE_MAX_SIZE, 0.75f, true) {
			protected boolean removeEldestEntry(
					Map.Entry<String, HashMap<Integer, ArrayList<VehiclePluginRecord>>> eldest) {
				return size() > CACHE_MAX_SIZE;
			}
		};

		this.uninstallCache = new LinkedHashMap<String, HashMap<Integer, ArrayList<String>>>(
				CACHE_MAX_SIZE, 0.75f, true) {
			protected boolean removeEldestEntry(
					Map.Entry<String, HashMap<Integer, ArrayList<String>>> eldest) {
				return size() > CACHE_MAX_SIZE;
			}
		};
	}

	public static Cache getCache() {
		if (instance == null) {
			instance = new Cache();
		}
		return instance;
	}

	public void addInstallCache(String vin, int appId,
			ArrayList<VehiclePluginRecord> records) {
		if (installCache.containsKey(vin)) {
			HashMap<Integer, ArrayList<VehiclePluginRecord>> appList = installCache
					.get(vin);
			if (appList.containsKey(appId)) {
				appList.remove(appId);
			}

			appList.put(appId, records);
			installCache.put(vin, appList);
		} else {
			HashMap<Integer, ArrayList<VehiclePluginRecord>> appList = new HashMap<Integer, ArrayList<VehiclePluginRecord>>();
			appList.put(appId, records);
			installCache.put(vin, appList);
		}
	}

	public VehiclePluginRecord getVehiclePluginRecord(String vin, int appId,
			String pluginName) {
		HashMap<Integer, ArrayList<VehiclePluginRecord>> appList = installCache
				.get(vin);
		ArrayList<VehiclePluginRecord> records = appList.get(appId);
		for (VehiclePluginRecord record : records) {
			if (record.getPluginName().equals(pluginName)) {
				records.remove(record);
				return record;
			}
		}
		return null;
	}

	public boolean IsAllPluginInstalled(String vin, int appId) {
		if (!installCache.containsKey(vin))
			return true;
		else {
			HashMap<Integer, ArrayList<VehiclePluginRecord>> appList = installCache
					.get(vin);
			if (appList.containsKey(appId)) {
				ArrayList<VehiclePluginRecord> records = appList.get(appId);
				if (records.isEmpty()) {
					return true;
				} else {
					return false;
				}
			} else {
				return true;
			}
		}
	}

	public void addUninstallCache(String vin, int appId, ArrayList<String> pluginNames) {
		if (uninstallCache.containsKey(vin)) {
			HashMap<Integer, ArrayList<String>> appList = uninstallCache
					.get(vin);
			if (appList.containsKey(appId))
				appList.remove(appId);
			appList.put(appId, pluginNames);
			uninstallCache.put(vin, appList);
		} else {
			HashMap<Integer, ArrayList<String>> appList = new HashMap<Integer, ArrayList<String>>();
			appList.put(appId, pluginNames);
			uninstallCache.put(vin, appList);
		}
	}

	public boolean updateUninstallCacheAndCheckIfRemovable(String vin,
			int appId, String pluginName) {
		// Check
		if (uninstallCache.containsKey(vin)) {
			HashMap<Integer, ArrayList<String>> appHashMap = uninstallCache
					.get(vin);

			if(appHashMap.containsKey(appId)) {
				ArrayList<String> records = appHashMap.get(appId);
				if(records.isEmpty())
					return true;
				else {
					for(String record : records) {
						if(record.equals(pluginName)) {
							records.remove(pluginName);
							break;
						}
					}
					if(records.isEmpty())
						return true;
					else
						return false;
				}
			} else {
				return true;
			}
		} else {
			return true;
		}

	}
}
