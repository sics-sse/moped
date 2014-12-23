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
	private String dbPath;

	/**
	 * Instantiates a new data table dao.
	 * 
	 * @param ecuName
	 *            the ecu name
	 */
	@SuppressWarnings("unchecked")
	public DataTableDao() {
		dbPath = "db" + File.separator;
		if (isTableExisted()) {
			pluginDataTable = (HashMap<String, DataRecord>) file2Object();
		} else {
			pluginDataTable = new HashMap<String, DataRecord>();
		}
	}

	public void insertRecord(String pluginName, DataRecord dataRecord) {
		// to check if application exists
		if (pluginDataTable.containsKey(pluginName)) {
			System.out.println("@ Plugin (" + pluginName
					+ " has been installed before");
			return;
		} else {
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
		while(iterator.hasNext()) {
			Entry<String, DataRecord> entry = iterator.next();
			DataRecord value = entry.getValue();
			int recordEcuId = value.getRemoteEcuId();
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
		logger.debug("To convert object(" + object.toString() + ") to file ("
				+ dbPath + ")");
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			File dest = new File(dbPath);
			if (!dest.exists())
				dest.mkdirs();
			fos = new FileOutputStream(dbPath + "plugin.DB");
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			logger.debug("Object(" + object.toString() + ") convered to file ("
					+ dbPath + ")");
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
	 * File2 object.
	 * 
	 * @return the object
	 */
	public Object file2Object() {
		logger.debug("To convert file(" + dbPath + ") to object)");
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(dbPath + "plugin.DB");
			ois = new ObjectInputStream(fis);
			Object object = ois.readObject();
			logger.debug("File(" + dbPath + ") converted to object");
			return object;
		} catch (FileNotFoundException e) {
			logger.debug(e.toString());
		} catch (IOException e) {
			logger.debug(e.toString());
		} catch (ClassNotFoundException e) {
			logger.debug(e.toString());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					logger.debug(e.toString());
				}
			}
			fis = null;
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					logger.debug(e.toString());
				}
			}
			ois = null;
		}
		return null;
	}

	/**
	 * Checks if is table existed.
	 * 
	 * @return true, if is table existed
	 */
	private boolean isTableExisted() {
		File file = new File(dbPath + "plugin.DB");
		if (file.isFile())
			return true;
		else
			return false;
	}

	/**
	 * Gets the db path.
	 * 
	 * @return the db path
	 */
	public String getDbPath() {
		return dbPath;
	}

	/**
	 * Sets the db path.
	 * 
	 * @param dbPath
	 *            the new db path
	 */
	public void setDbPath(String dbPath) {
		this.dbPath = dbPath;
	}

	public HashMap<String, DataRecord> getPluginDataTable() {
		return pluginDataTable;
	}

	public void setPluginDataTable(HashMap<String, DataRecord> pluginDataTable) {
		this.pluginDataTable = pluginDataTable;
	}

}
