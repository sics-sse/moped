package ecm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import db.DataRecord;
import db.DataTableDao;
import messages.InstallAckMessage;
import messages.InstallAckPacket;
import messages.LinkContextEntry;
import messages.LoadMessage;
import messages.Message;
import messages.MessageType;
import messages.PublishMessage;
import messages.PluginMessage;
import messages.PublishPacket;
import messages.RestoreAckMessage;
import messages.RestoreAckPacket;
import messages.UninstallAckMessage;
import messages.UninstallAckPacket;
import network.external.CarDriver;
import network.external.CarMobile;
import network.external.CarNav;
import network.external.CommunicationManager;
import network.external.IoTManager;
import network.internal.EcuManager;

public class Ecm {
	// interact with ECUs
	private EcuManager ecuManager;
	// interact with trusted server
	private CommunicationManager commuManager;
	// interact with IoT server
	private IoTManager iotManager;
	private CarDriver carDriver;
	private CarMobile carMobile;
	private CarNav carNav;
	
	private DataTableDao dbDao;
	// key: plug-in temporary id
	private HashMap<Byte, DataRecord> tmpDBRecords = new HashMap<Byte, DataRecord>();

	// key: plug-in temporary id, value: plug-in name
	private HashMap<Byte, String> id2name4UninstallCache = new HashMap<Byte, String>();
	
    // Define access methods instead of public.
    public String subscriberName = "AllVCU";
    public int subscriberPort = -1;

    public byte [] crashbytes;

	public Ecm() {
	    System.out.println("Ecm new");
		dbDao = new DataTableDao();
	}

	public void init(EcuManager ecuManager, CommunicationManager commuManager,
			 IoTManager iotManager, CarDriver carDriver) {
	    init(ecuManager, commuManager, iotManager, carDriver, null, null);
	}

	public void init(EcuManager ecuManager, CommunicationManager commuManager,
			 IoTManager iotManager, CarDriver carDriver, CarMobile carMobile, CarNav carNav) {
		this.ecuManager = ecuManager;
		this.commuManager = commuManager;
		this.iotManager = iotManager;
		this.carDriver = carDriver;
		this.carMobile = carMobile;
		this.carNav = carNav;
		
		ecuManager.setEcm(this);
		commuManager.setEcm(this);
		carDriver.setEcm(this);
		if (carMobile != null) {
		    carMobile.setEcm(this);
		}
		if (carNav != null) {
		    carNav.setEcm(this);
		}
//		iotManager.setEcm(this);

	    System.out.println("Ecm init");

	}
	
	public void start(String [] args) {
		new Thread(ecuManager).start();
		new Thread(carDriver).start();
		if (carMobile != null) {
		    new Thread(carMobile).start();
		}
		if (carNav != null) {
		    new Thread(carNav).start();
		}
		new Thread(iotManager).start();
		new Thread(commuManager).start();
		
		System.out.println("Ecm start");
		// TODO: make it dynamic later
		// loadPlugins
		for (int i = 0; i < args.length; i++) {
		    System.out.println("arg " + i + " " + args[i]);
		}

		if (args.length > 0 && args[0].equals("--noinstall")) {
		    System.out.println("no plugin installation");
		} else {
		    loadPlugins(2);
		    loadPlugins(3);
		}
	}
	
	public void loadPlugins(int ecuId) {
		System.out.println("In loadPlugins()");
		System.out.println("Loading plugins to ecuId: " + ecuId);
		// Prepare APPs
		HashMap<String, DataRecord> installedApps = getInstalledApps(ecuId);
		
		//TODO: TEMP
		if (installedApps != null) {
			System.out.println("Nr installed apps: " + installedApps.size());
			System.out.println(installedApps);
		} else
			System.out.println("installedApps: NULL!!!!!!!!!!!!!!");
		
		if (!installedApps.isEmpty()) {
		    for (Iterator<String> it1 = installedApps.keySet().iterator(); it1.hasNext(); ) {
			String key = it1.next();
			DataRecord rec = installedApps.get(key);
			
			System.out.println("Found db_key: " + key);
			System.out.println("app_id: " + rec.getAppId() + 
					"; remoteEcuId: " + rec.getRemoteEcuId() + 
					"; sendingPortId: " + rec.getSendingPortID() + 
					"; callbackPortId: " + rec.getCallbackPortID() + 
					"; pluginName: " + rec.getPluginName() + 
					"; executablePluginName + " + rec.getExecutablePluginName() + 
					"; location: " + rec.getLocation()); 

			System.out.println("portInitialContext: ");
			HashMap<String, Integer> initCxt = rec.getPortInitialContext();
			for (Iterator<String> it = initCxt.keySet().iterator(); it.hasNext(); ) {
				String ctxKey = it.next();
				System.out.println("\tkey: " + ctxKey + "; value: " + initCxt.get(ctxKey));
			}
			
			System.out.println("portLinkingContext: ");
			ArrayList<LinkContextEntry> linkCxt = rec.getPortLinkingContext();
			for (int i = 0; i < linkCxt.size(); i++) {
				LinkContextEntry entry = linkCxt.get(i);
				System.out.println("\tfromPortId: " + entry.getFromPortId() + 
						"; toPortId: " + entry.getToPortId() + 
						"; remotePortId: " + entry.getRemotePortId());

				if (rec.getPluginName().equals(subscriberName + ".suite") && entry.getFromPortId() == 22) {
					subscriberPort = entry.getToPortId();
					System.out.println("port " + subscriberPort + " is a subscriber");
			    }

			}
		    }
		}
		
		Iterator<Entry<String, DataRecord>> iterator = installedApps.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, DataRecord> entry = iterator.next();
			DataRecord record = entry.getValue();

			int reference = record.getRemoteEcuId();
			String executablePluginName = record.getExecutablePluginName();
			int callbackPortID = record.getCallbackPortID();
			HashMap<String, Integer> portInitialContext = record
					.getPortInitialContext();
			ArrayList<LinkContextEntry> portLinkingContext = record
					.getPortLinkingContext();
			String location = record.getLocation();
			byte[] pluginBytes;
			try {
				pluginBytes = readBytesFromFile(location);
				LoadMessage loadMessage = new LoadMessage(reference,
						executablePluginName, callbackPortID,
						portInitialContext, portLinkingContext, pluginBytes);

				process(loadMessage);
				Thread.sleep(2000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private byte[] readBytesFromFile(String location) throws IOException {
		File file = new File(location);
		InputStream is = new FileInputStream(file);
		// Get the size of the file
		long length = file.length();
		// You cannot create an array using a long type.
		// It needs to be an integer type.
		// Before converting to an integer type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName() + " as it is too long (" + length
					+ " bytes, max supported " + Integer.MAX_VALUE + ")");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			is.close();
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	public void process(Message message) {
		String pluginName;
		// the plugin number in the vehicle, starting from 1:
		byte pluginId;
		
		int messageType = message.getMessageType();
		    if (messageType != MessageType.PWM) {
			System.out.println(">>> ecm-core/Ecm " + messageType);
		    }
		switch (messageType) {
		case MessageType.INSTALL:
		case MessageType.UNINSTALL:
		case MessageType.RESTORE:
		case MessageType.LOAD:
		case MessageType.PWM:
		    if (messageType != MessageType.PWM) {
			System.out.println("<<< ecm-core/Ecm " + messageType);
		    }
			ecuManager.sendMessage(message);
			break;
		case MessageType.INSTALL_ACK:
			// forward to trusted server
			InstallAckMessage installAckMessage = (InstallAckMessage) message;
			pluginId = installAckMessage.getPluginId();
			pluginName = installAckMessage.getPluginName();
			System.out.println("INSTALL_ACK in Ecm");
			System.out.println("@@@ pluginName:"+pluginName);
			updateDB(pluginId);
			System.out.println("@@@ local pluginId:"+pluginId);
			int appId = getAppId(pluginName);
			System.out.println("@@@ appId:"+appId);
			if(pluginName.contains(".zip")) {
				pluginName = pluginName.replace(".zip", ".suite");
			}
			InstallAckPacket installAckPacket = new InstallAckPacket(getVin(), appId, 
					pluginName);
		    System.out.println("<<< ecm-core/Ecm " + messageType);
			commuManager.write(installAckPacket);
			break;
		case MessageType.UNINSTALL_ACK:
			// forward to trusted server
			UninstallAckMessage uninstallAckMessage = (UninstallAckMessage) message;
			pluginName = uninstallAckMessage.getPluginName();

			// Remove the PlugIn file in the local and the item in the DB
			System.out.println("pluginName = " + pluginName);
			DataRecord record = getRecord(pluginName);
			String location = record.getLocation();
			System.out.println("location = " + location);
			
			deletePlugInFile(location);
			removeRecord(pluginName);
			
			System.out.println("UNINSTALL_ACK pluginName " + pluginName);

			if(pluginName.contains(".zip")) {
				pluginName = pluginName.replace(".zip", ".suite");
			}
			UninstallAckPacket uninstallAckPacket = new UninstallAckPacket(
					getVin(), pluginName);
			
		    System.out.println("<<< ecm-core/Ecm " + messageType);
			commuManager.write(uninstallAckPacket);
			break;
		case MessageType.RESTORE_ACK:
			// forward to trusted server
			RestoreAckMessage restoreAckMessage = (RestoreAckMessage) message;
			pluginName = restoreAckMessage.getPluginName();
			RestoreAckPacket restoreAckPacket = new RestoreAckPacket(getVin(), pluginName);
		    System.out.println("<<< ecm-core/Ecm " + messageType);
			commuManager.write(restoreAckPacket);
			break;
		case MessageType.PUBLISH:
			// forward to IoT server
			PublishMessage publishMessage = (PublishMessage) message;
			String key = publishMessage.getKey();
			String value = publishMessage.getValue();
			System.out.println("[Publish in ECM] - key: " + key + ", value: "
					+ value);
			PublishPacket publishPacket = new PublishPacket(key, value);
		    System.out.println("<<< ecm-core/Ecm " + messageType);
		    if (iotManager != null) {
			iotManager.sendPacket(publishPacket);
		    }
			break;
		case MessageType.PLUGIN_MESSAGE:
			PluginMessage pluginMessage = (PluginMessage) message;
			String val = (String) pluginMessage.getValue();
			System.out.println("iot subscribe message: " + val);
			System.out.println("sending to VCU:" + subscriberPort);
			ecuManager.sendToVCU(val, subscriberPort);

			break;
		default:
			System.out.println("Error: Wrong message type " + messageType);
		}
	}

	// access to DB
	public void insertTmpDBRecord(Byte pluginId, DataRecord dataRecord) {
	    System.out.println("Ecm insert tmp " + pluginId);
	    tmpDBRecords.put(pluginId, dataRecord);
	    System.out.println(tmpDBRecords);
	}
	
	public String getPluginNameFromTmpDB(Byte pluginId) {
		DataRecord dataRecord = tmpDBRecords.get(pluginId);
		return dataRecord.getPluginName();
	}
	
	public boolean hasPluginInTmpDB(Byte pluginId) {
		return tmpDBRecords.containsKey(pluginId);
	}

	private void removeRecord(String pluginName) {
	    System.out.println("Ecm remove " + pluginName);
	    dbDao.removeRecord(pluginName);
	}
	
	private void updateDB(byte pluginId) {
		DataRecord dataRecord = tmpDBRecords.get(pluginId);
		System.out.println("ecm updateDB local id " + pluginId);
	    System.out.println(tmpDBRecords);
		System.out.println("ecm updateDB " + dataRecord);
		String pluginName = dataRecord.getPluginName();
		System.out.println("ecm updateDB " + pluginName);
		dbDao.insertRecord(pluginName, dataRecord);
		tmpDBRecords.remove(pluginId);
	}

	private DataRecord getRecord(String pluginName) {
		return dbDao.getRecord(pluginName);
	}


	// save jar file in the ECM
	protected void generateFile(byte[] data, String path) {
		try {
			OutputStream output = null;
			try {
				output = new BufferedOutputStream(new FileOutputStream(path));
				output.write(data);
			} finally {
				output.close();
			}
		} catch (FileNotFoundException ex) {
			System.out.println("File not found.");
		} catch (IOException ex) {
			System.out.println(ex);
		}
	}

	private boolean deletePlugInFile(String filepath) {
		boolean flag = false;
		File file = new File(filepath);
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	public CommunicationManager getCommuManager() {
		return commuManager;
	}

	public IoTManager getIotManager() {
		return iotManager;
	}

	public HashMap<String, DataRecord> getInstalledApps(int ecuId) {
		HashMap<String, DataRecord> installedAppRecords = dbDao
				.getInstalledAppRecords(ecuId);
		return installedAppRecords;
	}

	private String getVin() {
		return commuManager.getVin();
	}
	
	private int getAppId(String pluginName) {
		return dbDao.getAppId(pluginName);
	}
	
	public String getPluginNameFromUninstallCache(Byte pluginId) {
		return id2name4UninstallCache.get(pluginId);
	}
	
	public void addPluginIdPluginName2UninstallCache(Byte pluginId, String pluginName) {
		id2name4UninstallCache.put(pluginId, pluginName);
	}
	
	public boolean hasPluginInUninstallCache(Byte pluginId) {
		return id2name4UninstallCache.containsKey(pluginId);
	}
}
