package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

/**
 * The Class DataTableDao.
 */
public class DataTableDao {

	/** The logger. */
	private static Logger logger = Logger.getLogger(DataTableDao.class);

	/** The plugin data table. */
	// key: Plug-in Name(String), value: DataRecord
	private HashMap<String, DataRecord> pluginDataTable;

	/** The db path. */
	private static final String DB_PATH = "db" + File.separator;

	/**
	 * Instantiates a new data table dao.
	 */
	public DataTableDao() {
		pluginDataTable = fileToMap();
//		if (pluginTableExists()) {
//			pluginDataTable = (HashMap<String, DataRecord>) fileToMap();
//		} else {
//			System.out.println("else...");
//			pluginDataTable = new HashMap<String, DataRecord>();
//		}
	}

	public void insertRecord(String pluginName, DataRecord dataRecord) {
		// to check if application exists
		if (pluginDataTable.containsKey(pluginName)) {
			System.out.println("@ Plugin (" + pluginName
					+ ") has been installed before");
			//return;
		}
		{
			pluginDataTable.put(pluginName, dataRecord);
			// to synchronize memory with storage
			this.object2File(pluginDataTable);
		}
	}

	public DataRecord getRecord(String pluginName) {
		return pluginDataTable.get(pluginName);
	}
	/**
	 * Removes the record.
	 * 
	 * @param localPluginID
	 *            the local plugin id
	 */
	public void removeRecord(String pluginName) {
		pluginDataTable.remove(pluginName);

		// to synchronize memory with storage
		this.object2File(pluginDataTable);
	}
	

	public HashMap<String, DataRecord> getInstalledAppRecords(int targetEcuId) {
		HashMap<String, DataRecord> result = new HashMap<String, DataRecord>();
		Iterator<Entry<String, DataRecord>> iterator = pluginDataTable.entrySet().iterator();
		System.out.println("getInstalledAppRecords " + targetEcuId);
		while(iterator.hasNext()) {
			Entry<String, DataRecord> entry = iterator.next();
			DataRecord value = entry.getValue();
			int recordEcuId = value.getRemoteEcuId();
			System.out.println(" " + recordEcuId + " " + entry.getKey());
			if(targetEcuId == recordEcuId) {
				String key = entry.getKey();
				result.put(key, value);
			}
		}
		
		return result;
	}
	
	public int getAppId(String pluginName) {
		DataRecord dataRecord = pluginDataTable.get(pluginName);
//		System.out.println("ddddddd "+pluginName);
		int appId = dataRecord.getAppId();
		return appId;
	}
	
	/**
	 * Gets the remote ecu.
	 * 
	 * @param remotePluginClassname
	 *            the remote plugin classname
	 * @return the remote ecu
	 */
	// public byte getRemoteECU(String remotePluginClassname) {
	// Iterator<Entry<Byte, LinkedList<DataRecord>>> iterator = pluginDataTable
	// .entrySet().iterator();
	// while (iterator.hasNext()) {
	// Entry<Byte, LinkedList<DataRecord>> entry = iterator.next();
	// DataRecord record = entry.getValue();
	// Map<String, Byte> contexts = record.getPluginContex();
	// for (String key : contexts.keySet()) {
	// if (key.toString().equals(remotePluginClassname.toString())) {
	// return contexts.get(key);
	// }
	// }
	// }
	// return -1;
	// }

	/**
	 * Object2 file.
	 * 
	 * @param object
	 *            the object
	 */
	public void object2File(Object object) {
//		logger.debug("To convert object(" + object.toString() + ") to file ("
//				+ DB_PATH + ")");
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			File dest = new File(DB_PATH);
			if (!dest.exists())
				dest.mkdirs();
			fos = new FileOutputStream(DB_PATH + "plugin.DB");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
//			logger.debug("Object(" + object.toString() + ") convered to file ("
//					+ DB_PATH + ")");
		} catch (FileNotFoundException e) {
			logger.debug(e.toString());
		} catch (IOException e) {
			logger.debug(e.toString());
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.debug(e.toString());
				}
			}
			fos = null;
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					logger.debug(e.toString());
				}
			}
			oos = null;
		}
	}

	/**
	 * Convert a database file to a HashMap object
	 * 
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	private HashMap<String, DataRecord> fileToMap() {
		ObjectInputStream ois = null;
		HashMap<String, DataRecord> pluginMap = null;
		
		try {
			ois = new ObjectInputStream(new FileInputStream(DB_PATH + "plugin.DB"));
			pluginMap = (HashMap<String, DataRecord>)ois.readObject();
		} catch (FileNotFoundException ex) {
			pluginMap = new HashMap<String, DataRecord>();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return pluginMap;
	}

//	/**
//	 * Check if there is a table of installed plugins
//	 * 
//	 * @return true, if a table of installed plugins exists
//	 */
//	private boolean pluginTableExists() {
//		File file = new File(DB_PATH + "plugin.DB");
//		if (file.isFile())
//			return true;
//		else
//			return false;
//	}

	public HashMap<String, DataRecord> getPluginDataTable() {
		return pluginDataTable;
	}

	public void setPluginDataTable(HashMap<String, DataRecord> pluginDataTable) {
		this.pluginDataTable = pluginDataTable;
	}

}
