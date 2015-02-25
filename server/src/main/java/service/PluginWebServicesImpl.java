package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.mina.core.session.IoSession;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cache.Cache;
import cache.VehiclePluginRecord;
import common.GlobalVariables;
import dao.AppConfigDao;
import dao.AppConfigDaoImpl;
import dao.ApplicationDao;
import dao.ApplicationDaoImpl;
import dao.DBConnection;
import dao.DatabasePluginDao;
import dao.DatabasePluginDaoImpl;
import dao.PluginConfigDaoImpl;
import dao.VehicleConfigDao;
import dao.VehicleConfigDaoImpl;
import dao.VehicleDao;
import dao.VehicleDaoImpl;
import dao.VehiclePluginDao;
import dao.VehiclePluginDaoImpl;
import messages.InstallPacket;
import messages.InstallPacketData;
import messages.LinkContextEntry;
import messages.DebugPacket;
import messages.RestorePacket;
import messages.UninstallPacket;
import messages.UninstallPacketData;
import mina.ServerHandler;
import model.AppConfig;
import model.Application;
import model.DatabasePlugin;
import model.Ecu;
import model.Link;
import model.PluginConfig;
import model.PluginLinkConfig;
import model.PluginPortConfig;
import model.Port;
import model.Vehicle;
import model.VehicleConfig;
import model.VehiclePlugin;
import service.exception.PluginWebServicesException;
import utils.CompressUtils;
import utils.SuiteGen;

@WebService(endpointInterface = "service.PluginWebServices")
public class PluginWebServicesImpl implements PluginWebServices {
	private ApplicationDao applicationDao;
	private VehiclePluginDao vehiclePluginDao;
	private VehicleDao vehicleDao;
	private VehicleConfigDao vehicleConfigDao;
	private DatabasePluginDao databasePluginDao;
	private AppConfigDao appConfigDao;
//	private PluginConfigDaoImpl pluginConfigDao;
	private SuiteGen suiteGen = new SuiteGen("/lhome/sse/squawk");
	
	private DBConnection db = null;
	private ServerHandler handler = null;
	
	public PluginWebServicesImpl(ServerHandler handler) {
		this.handler = handler;
		
		db = new DBConnection();
		
		vehicleDao = new VehicleDaoImpl(db);
		vehicleConfigDao = new VehicleConfigDaoImpl(db);
		appConfigDao = new AppConfigDaoImpl(db);
		applicationDao = new ApplicationDaoImpl(db);
		databasePluginDao = new DatabasePluginDaoImpl(db);
//		pluginConfigDao = new PluginConfigDaoImpl(db);
	}

	public ApplicationDao getApplicationDao() {
		return applicationDao;
	}

	public void setApplicationDao(ApplicationDao applicationDao) {
		this.applicationDao = applicationDao;
	}

	public VehiclePluginDao getVehiclePluginDao() {
		return vehiclePluginDao;
	}

	public void setVehiclePluginDao(VehiclePluginDao vehiclePluginDao) {
		this.vehiclePluginDao = vehiclePluginDao;
	}

	public VehicleDao getVehicleDao() {
		return vehicleDao;
	}

	public void setVehicleDao(VehicleDao vehicleDao) {
		this.vehicleDao = vehicleDao;
	}

	public VehicleConfigDao getVehicleConfigDao() {
		return vehicleConfigDao;
	}

	public void setVehicleConfigDao(VehicleConfigDao vehicleConfigDao) {
		this.vehicleConfigDao = vehicleConfigDao;
	}

	public DatabasePluginDao getDatabasePluginDao() {
		return databasePluginDao;
	}

	public void setDatabasePluginDao(DatabasePluginDao databasePluginDao) {
		this.databasePluginDao = databasePluginDao;
	}

	public AppConfigDao getAppConfigDao() {
		return appConfigDao;
	}

	public void setAppConfigDao(AppConfigDao appConfigDao) {
		this.appConfigDao = appConfigDao;
	}

	public SuiteGen getSuiteGen() {
		return suiteGen;
	}

	public void setSuiteGen(SuiteGen suiteGen) {
		this.suiteGen = suiteGen;
	}
	
	@Override
	public void insertPluginInDb(String location, String name) 
			throws PluginWebServicesException {
		File configFile = null;
		
		try {
			File loc = new File(location);
			location = loc.getCanonicalPath();
		
			JarFile jar = new JarFile(new File(location + File.separator + name + ".jar"));
			Manifest mf = jar.getManifest();
			if (mf != null) {		
				Attributes attributes = mf.getMainAttributes();
				
				String publisher = attributes.getValue("Built-By");
				if (publisher == null)
					publisher = "unknown";
				String version = attributes.getValue("Manifest-Version");
				if (version == null)
					version = "1.0";
				String brand = attributes.getValue("Vehicle-Brand");
				if (brand == null)
					brand = "SICS";
				String vehicleName = attributes.getValue("Vehicle-Name");
				if (vehicleName == null)
					vehicleName = "MOPED";
				String ecuRef = attributes.getValue("Ecu");
				if (ecuRef == null)
					ecuRef = "0";
				String configFileName = attributes.getValue("Pirte-Config");
				if (configFileName == null)
					configFileName = name + ".xml";
				//TODO: UGLY HACK (only allows one .class file)
				String fullClassName = "";
				for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
					JarEntry entry = entries.nextElement();
					String fileName = entry.getName();
					if (fileName.endsWith(".class")) {
						fullClassName = fileName.substring(0, fileName.length() - 6);
					} else if (fileName.equals(configFileName)) {
						configFile = File.createTempFile("tempxml.xml", null);
				        BufferedReader reader = new BufferedReader(new InputStreamReader(
				        		jar.getInputStream(entry)));
				        BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
				        String line;
				        while ((line = reader.readLine()) != null) {
				        	writer.write(line);
				        }
				        reader.close();
				        writer.close();
					}
				}
				
				
				Application application = new Application(name, publisher, version);
				int appId = applicationDao.saveApplication(application);
				int appConfigId = appConfigDao.saveAppConfig(
						new AppConfig(appId, vehicleName, brand));	
				DatabasePlugin dbPlugin = new DatabasePlugin(
						name, name + ".zip", fullClassName, 
						"", Integer.parseInt(ecuRef), 
						location + File.separator + name, location);
				dbPlugin.setApplication(applicationDao.getApplication(appId));
				databasePluginDao.saveDatabasePlugin(dbPlugin);
				//TODO: Why + .suite???
				PluginConfig pluginConfig = new PluginConfig(name + ".suite", Integer.parseInt(ecuRef));
				pluginConfig.setAppConfig(appConfigDao.getAppConfig(appConfigId));
				//TODO: Inconsequence
				appConfigDao.savePluginConfig(pluginConfig);
				
				//TODO: Refactor
				// Xml-parsing
				Document doc = DocumentBuilderFactory.newInstance().
						newDocumentBuilder().parse(configFile);
				doc.getDocumentElement().normalize();
				
				NodeList ports = doc.getElementsByTagName("port");
				for (int i = 0; i < ports.getLength(); i++) {
					Element port = (Element)ports.item(i);
					String portName = port.getElementsByTagName("name").item(0).getTextContent();
					
					PluginPortConfig portConfig = new PluginPortConfig(portName);
					portConfig.setPluginConfig(appConfigDao.getPluginConfig(pluginConfig));
					appConfigDao.savePluginPortConfig(portConfig);
				}
				
				NodeList links = doc.getElementsByTagName("link");
				for (int i = 0; i < ports.getLength(); i++) {
					Element link = (Element)links.item(i);
					String linkSource = link.getElementsByTagName("from").item(0).getTextContent();
					String linkTarget = link.getElementsByTagName("to").item(0).getTextContent();

					int connectionType = GlobalVariables.PPORT2PPORT;
					if (linkSource.matches("(\\d+)")) {
						connectionType = GlobalVariables.VPORT2PORT;
					}
					if (linkTarget.matches("(\\d+)")) {
						connectionType = GlobalVariables.PPORT2VPORT;
					}
					
					PluginLinkConfig linkConfig = new PluginLinkConfig(linkSource, linkTarget, connectionType);
					linkConfig.setPluginConfig(appConfigDao.getPluginConfig(pluginConfig));
					appConfigDao.savePluginLinkConfig(linkConfig);
				}
			}
			else {
				System.out.println("MF is NULL");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		System.out.println("insertPluginInDb done");
	}
	
	@Override
	public boolean get_ack_status(String vin, int appId)
			throws PluginWebServicesException {
		Application app = (Application)db.getSingleResult(
				"FROM Application a WHERE a.applicationId = " + appId);
		
		if (app == null)
			return false;
		
		if (handler.existsAckMessage(vin + "_" + app.getApplicationName()))
			return true;
		
		return false;
	}
	
	@Override
	public void register_vehicle(String name, String vin, String type)
			throws PluginWebServicesException {
		System.out.println("VEHICLE NAME: " + name);
		System.out.println("VIN: " + vin);		
		System.out.println("VEHICLE TYPE: " + type);
		
		if (type.startsWith("moped_3rpi_v")) {
			List<String> ecuNames =  Arrays.asList("moped_tcu", "moped_vcu", "moped_scu");
//			vehicleConfigDao.newSaveVehicleConfig(type.substring(0, 10), type.substring(12), ecuNames);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public String install(String vin, int appID, String jvm) 
			throws PluginWebServicesException {
		System.out.println("vin in install(): " + vin);
		System.out.println("appID in install(): " + appID);
		
		// Fetch the connection session between Server and Vehicle
		IoSession session = ServerHandler.getSession(vin);
		if (session == null) {
			// If null, there is no connection between the server and the vehicle
			System.out.println("IoSession is NULL");
			return "false";
		} else {
			System.out.println("IoSession.address: " + session.getLocalAddress());
			
			// Achieve contexts
			// key: portName(String), value:
			// portId(Integer)>
			HashMap<String, Integer> portInitialContext = new HashMap<String, Integer>();
			// HashMap<String, ArrayList<LinkingContextEntry>> linkingContexts =
			// new HashMap<String, ArrayList<LinkingContextEntry>>();
			HashMap<String, ArrayList<LinkContextEntry>> linkingContexts = new HashMap<String, ArrayList<LinkContextEntry>>();

			// Create an array list for cache
			ArrayList<VehiclePluginRecord> installCachePlugins = new ArrayList<VehiclePluginRecord>();

			System.out.println("Fetching vehicle, vehicleDao: " + vehicleDao);
			Vehicle vehicle = vehicleDao.getVehicle(vin);
			System.out.println("Vehicle found: " + vehicle);

			// VehicleConfig
			int vehicleConfigId = vehicle.getVehicleConfigId();
			VehicleConfig vehicleConfig = vehicleConfigDao
					.getVehicleConfig(vehicleConfigId);
			System.out.println("VehicleConfig found: " + vehicleConfig);
			
			// AppConfig
			String vehicleName = vehicleConfig.getName();
			String brand = vehicleConfig.getBrand();
			System.out.println("Found vehicle: " + vehicleName + " of brand: " + brand + "... (next step not implemented yet)");
			AppConfig appConfig = appConfigDao.getAppConfig(appID, vehicleName, brand);
			System.out.println("AppConfig found, id: " + appConfig.getId());
			
			// PluginConfig
			//TODO: Move it to a warmer place
			if (appConfig != null) {
				List<PluginConfig> pluginConfigs = (List<PluginConfig>)db.getAllResults(
						"FROM PluginConfig pc WHERE pc.appConfig = " + appConfig.getId());
				for (PluginConfig pluginConfig : pluginConfigs) {
					System.out.println("pluginConfig.id: " + pluginConfig.getId());
					
					List<PluginPortConfig> pluginPortConfigs = db.getAllResults(
							"FROM PluginPortConfig ppc WHERE ppc.pluginConfig = " + pluginConfig.getId());
					for (PluginPortConfig pluginPortConfig : pluginPortConfigs) {
						int pluginPortId = pluginPortConfig.getId();
						String pluginPortName = pluginPortConfig.getName();
						portInitialContext.put(pluginPortName, pluginPortId);
					}
				}
				
				//TEMP_DEBUG
				System.out.println("PORT INITIAL CONTEXTS: ");
				for (String ctxt : portInitialContext.keySet()) {
					System.out.println("PIC: <" + ctxt + ", " + portInitialContext.get(ctxt) + ">");
				}
					
				for (PluginConfig pluginConfig : pluginConfigs) {
					// Plugin Link Config
					String pluginName = pluginConfig.getName();
					// Initiate LinkingContext
					ArrayList<LinkContextEntry> linkingContext = new ArrayList<LinkContextEntry>();
				
					List<PluginLinkConfig> pluginLinkConfigs = db.getAllResults(
							"FROM PluginLinkConfig plc WHERE plc.pluginConfig = " + pluginConfig.getId());
					for (PluginLinkConfig pluginLinkConfig : pluginLinkConfigs) {
						String from = pluginLinkConfig.getFromStr();
						String to = pluginLinkConfig.getToStr();
						String remote = pluginLinkConfig.getRemote();

						int fromPortId = 0;
						int toPortId = 0;
						int remoteId = 0;

						Scanner scanner = new Scanner(remote);
						boolean remoteTag = scanner.hasNextInt();

						if (remoteTag) {
							remoteId = scanner.nextInt();
							switch (remoteId) {
							case GlobalVariables.PPORT2PPORT:
								fromPortId = portInitialContext.get(from);
								toPortId = portInitialContext.get(to);
								break;
							case GlobalVariables.PPORT2VPORT:
								fromPortId = portInitialContext.get(from);
								toPortId = Integer.parseInt(to);
								break;
							case GlobalVariables.VPORT2PORT:
								fromPortId = Integer.parseInt(from);
								toPortId = portInitialContext.get(to);
								break;
							default:
								System.out.println("Error: Wrong link type in GlobalVariables");
								System.exit(-1);
							}
						} else {
							// Plug-In -> VRPort
							// remote represents the name of remote port
							remoteId = portInitialContext.get(remote);
							fromPortId = portInitialContext.get(from);
							toPortId = Integer.parseInt(to);
						}
						
						scanner.close();

						LinkContextEntry entry = new LinkContextEntry(fromPortId,
								toPortId, remoteId);
						linkingContext.add(entry);
					}
				
					linkingContexts.put(pluginName, linkingContext);
					
					//TEMP_DEBUG
					System.out.println("PORT LINKING CONTEXTS (" + pluginName + "): ");
					for (Iterator<LinkContextEntry> ctxtIter = linkingContext.iterator(); ctxtIter.hasNext(); ) {
						LinkContextEntry ctxt = ctxtIter.next();
						System.out.println("PLC: <" + ctxt.getFromPortId() + ", " + ctxt.getToPortId() + "> via " + ctxt.getRemotePortId());
					}
				}
			}

			// Achieve jars
			ArrayList<InstallPacketData> installPackageDataList = new ArrayList<InstallPacketData>();

			// Fetch application data from DB
			Application application = applicationDao.getApplication(appID);
			System.out.println("Found application: " + application.getApplicationName());
			
			// Fetch PlugIns from DB
			// HashMap<String, Byte> contexts = new HashMap<String, Byte>();
			@SuppressWarnings("unchecked")
			List<DatabasePlugin> plugins = (List<DatabasePlugin>)db.getAllResults(
					"FROM DatabasePlugin dp WHERE dp.application = " + application.getApplicationId());
//			Set<DatabasePlugin> plugins = application.getDatabasePlugins();
			System.out.println("Found plugins, size: " + plugins.size());
			for (DatabasePlugin plugin : plugins) {
				String pluginName = plugin.getName();
				System.out.println("Found plugin with name: " + pluginName);
				
				int remoteEcuId = plugin.getReference();
				int sendingPortId = vehicleConfigDao.getSendingPortId(
						vehicleConfigId, remoteEcuId);
				int callbackPortId = vehicleConfigDao.getCallbackPortId(
						vehicleConfigId, remoteEcuId);
				String executablePluginName = "plugin://"
						+ plugin.getFullClassName() + "/" + pluginName;
				String pluginSuiteName = pluginName + ".suite"; //TODO: Come on...
				// Find PlugIn location. For instance,
				// some_dir/uploaded/app_name/version/kdkdks.zip
				String location = /*
								 * GlobalVariables.APPS_DIR +
								 */plugin.getLocation();
				
				String fileType = ".zip";
				if (jvm.equals("Squawk")) {
					fileType = ".suite";
					location += File.separator + pluginName;
				}
				
				location += fileType;
				pluginName += fileType;
				executablePluginName += fileType;
				
				File file = new File(location);
				byte[] fileBytes;
				try {
					// ArrayList<LinkingContextEntry> linkingContext =
					// linkingContexts
					// .get(pluginName);
					ArrayList<LinkContextEntry> linkingContext = (ArrayList<LinkContextEntry>) linkingContexts
							.get(pluginSuiteName);
					fileBytes = readBytesFromFile(file);
					InstallPacketData installPacketData = new InstallPacketData(
							appID, pluginName/* +".zip" */, sendingPortId,
							callbackPortId, remoteEcuId, portInitialContext,
							linkingContext, executablePluginName, fileBytes); 

					installPackageDataList.add(installPacketData);

					// Store it temporarily to cache and will be used after the
					// arrival of acknowledge messages
					VehiclePluginRecord record = new VehiclePluginRecord(
							pluginName, remoteEcuId, sendingPortId,
							callbackPortId, portInitialContext, linkingContext,
							location, executablePluginName);

					installCachePlugins.add(record);
					
					System.out.println("READY FOR INSTALLATION WRITING!!!!!!!!!!!!!!!!!!!!");
					
					Cache.getCache().addInstallCache(vin, appID, installCachePlugins);
					InstallPacket installPacket = new InstallPacket(vin,
							installPackageDataList);
					session.write(installPacket);
					
					System.out.println("SUCCESSFULLY INSTALLED SOME STUFF!!!!!!!!!!!!!!!!!!!!");

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return "true";
		}
	}

	public boolean uninstall(String vin, int appID)
			throws PluginWebServicesException {
		IoSession session = ServerHandler.getSession(vin);
		if (session == null) {
			// If null, response user about the disconnection between Sever and
			// Vehicle
			return false;
		} else {
			// Fetch un_installation PlugIns
			ArrayList<UninstallPacketData> uninstallPackageDataList = new ArrayList<UninstallPacketData>();

			// Save pluginname into array list cache for uninstallation
			ArrayList<String> uninstallCacheName = new ArrayList<String>();

			List<VehiclePlugin> vehiclePlugins = vehiclePluginDao
					.getVehilePlugins(vin, appID);
			for (VehiclePlugin vehiclePlugin : vehiclePlugins) {
				String pluginName = vehiclePlugin.getName();
				int sendingPortId = vehiclePlugin.getSendingPortId();
				int callbackPortId = vehiclePlugin.getCallbackPortId();
				int reference = vehiclePlugin.getEcuId();

				UninstallPacketData uninstallPackageData = new UninstallPacketData(
						sendingPortId, callbackPortId, reference, pluginName);
				uninstallPackageDataList.add(uninstallPackageData);

				uninstallCacheName.add(pluginName);
			}

			Cache.getCache().addUninstallCache(vin, appID, uninstallCacheName);

			UninstallPacket uninstallPackage = new UninstallPacket(vin,
					uninstallPackageDataList);
			session.write(uninstallPackage);
			return true;
		}
	}

	public boolean upgrade(String vin, int oldAppID)
			throws PluginWebServicesException {
		IoSession session = ServerHandler.getSession(vin);
		if (session == null) {
			// If null, response user about the disconnection between Sever and
			// Vehicle
			return false;
		} else {
			uninstall(vin, oldAppID);
			try {
				Thread.sleep(2000);
				int newAppId = applicationDao.getNewestApplication(oldAppID);
				if (newAppId > -1) {
					install(vin, newAppId, "Squawk"); //TODO: This should also be implemented for JDK (all the way to the upgrade button in the php-interface)
					return true;
				}
				else 
					return false;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}

	}

	private byte[] readBytesFromFile(File file) throws IOException {
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

	@WebMethod
	public void setUpgradeFlag(int oldAppID) throws PluginWebServicesException {
		applicationDao.setHasNewVersionFlag(oldAppID);
	}

	@WebMethod
	public boolean restoreEcu(String vin, int ecuReference)
			throws PluginWebServicesException {
		IoSession session = ServerHandler.getSession(vin);
		if (session == null) {
			// If null, response user about the disconnection between Sever and
			// Vehicle
			return false;
		} else {

			ArrayList<InstallPacketData> installPackageDataList = new ArrayList<InstallPacketData>();
			List<VehiclePlugin> vehiclePlugins = vehiclePluginDao
					.getPluginsInSpecificEcu(vin, ecuReference);
			for (VehiclePlugin vehiclePlugin : vehiclePlugins) {
				int appId = vehiclePlugin.getAppId();
				String pluginName = vehiclePlugin.getName();
				int sendingPortId = vehiclePlugin.getSendingPortId();
				int callbackPortId = vehiclePlugin.getCallbackPortId();
				int reference = vehiclePlugin.getEcuId();
				String executablePluginName = vehiclePlugin
						.getExecutablePluginName();
				HashMap<String, Integer> portInitialContext = vehiclePlugin
						.getPortInitialContext();
				ArrayList<LinkContextEntry> portLinkingContext = vehiclePlugin
						.getPortLinkingContext();
				// String location = GlobalVariables.APPS_DIR +
				// dp.getLocation();
				String location = vehiclePlugin.getLocation();
				File file = new File(location);
				byte[] fileBytes;
				try {
					fileBytes = readBytesFromFile(file);
					InstallPacketData installPackageData = new InstallPacketData(
							appId, pluginName, sendingPortId, callbackPortId,
							reference, portInitialContext, portLinkingContext,
							executablePluginName, fileBytes);
					installPackageDataList.add(installPackageData);
				} catch (IOException e) {
					System.out
							.println("Error! Fail to read PlugIn file from Server.");
					return false;
				}

			}

			RestorePacket restorePackage = new RestorePacket(vin,
					installPackageDataList);
			session.write(restorePackage);
			return true;
		}

	}

	@Override
	public String parseVehicleConfiguration(String path) 
			throws PluginWebServicesException {
		System.out.println("In parseVehicleConfiguration");
		System.out.println("pwd: " + java.nio.file.Paths.get(".").toAbsolutePath().normalize().toString());
		System.out.println("config path: " + path);

		// key: port ID, value: ECU ID
		HashMap<Integer, Integer> portId2EcuId = new HashMap<Integer, Integer>();

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(path);
			Document doc = dombuilder.parse(is);
			// vehicle
			Element root = doc.getDocumentElement();
			System.out.println("Start parsing XML");

			// name of vehicle
			Element vehicleName = (Element) root.getElementsByTagName("name")
					.item(0);
			String vehicleNameStr = vehicleName.getTextContent();

			// brand of vehicle
			Element vehicleBrand = (Element) root.getElementsByTagName("brand")
					.item(0);
			String vehicleBrandStr = vehicleBrand.getTextContent();
			if (vehicleBrandStr == null) {
				vehicleBrandStr = "";
			}

			VehicleConfig vehicleConfig = new VehicleConfig(vehicleNameStr,
															vehicleBrandStr);
			
			vehicleConfigDao.saveVehicleConfig(vehicleConfig);

			// VIN
			Element vinElement = (Element) root.getElementsByTagName("vin").item(0);
			if(vinElement == null) {
				System.out.println("There is no VIN element in vehicle configuration file");
				System.exit(-1);
			}
			String vinStr = vinElement.getTextContent();
			
//			Vehicle vehicle = new Vehicle();
//			vehicle.setName(vehicleNameStr);
//			vehicle.setVIN(vinStr);
//			vehicle.setVehicleConfigId(vehicleConfig.getId());
//			vehicleDao.saveVehicle(vehicle);
			
			// ecus
			Element ecusElement = (Element) root.getElementsByTagName("ecus")
					.item(0);
			if (ecusElement == null) {
				System.out
						.println("There is no ecus element in vehicle configuration file");
				System.exit(-1);
			}

			NodeList ecuList = ecusElement.getElementsByTagName("ecu");

			for (int i = 0; i < ecuList.getLength(); i++) {
				// ecu
				System.out.println("<ecu>");
				Ecu ecu = new Ecu();
				Element ecuElement = (Element) ecuList.item(i);
				if (ecuElement == null) {
					System.out
							.println("There is no ecu element in vehicle configuration file");
					System.exit(-1);
				}
				Element idElement = (Element) ecuElement.getElementsByTagName(
						"id").item(0);
				if (idElement == null) {
					System.out
							.println("There is no id element in ecu range in vehicle configuration file");
					System.exit(-1);
				}
				String ecuIdStr = idElement.getTextContent();
				System.out.println("  <id>" + ecuIdStr + "</id>");
				int ecuId = Integer.parseInt(ecuIdStr);
				ecu.setEcuId(ecuId);
				ecu.setVehicleConfig(vehicleConfig);

				vehicleConfigDao.saveEcu(ecu);

				// swcs
				Element swcsElement = (Element) ecuElement
						.getElementsByTagName("swcs").item(0);
				if (swcsElement == null) {
					System.out
							.println("There is no swcs element in ecu range in vehicle configuration file");
					System.exit(-1);
				}
				NodeList swcList = swcsElement.getElementsByTagName("swc");
				for (int s = 0; s < swcList.getLength(); s++) {
					// swc
					Element swcElement = (Element) swcList.item(s);
					if (swcElement == null) {
						System.out
								.println("There is no swc element in ecu range in vehicle configuration file");
						System.exit(-1);
					}
					// hasPirte
					Element hasPirteElement = (Element) swcElement
							.getElementsByTagName("hasPirte").item(0);
					if (hasPirteElement == null) {
						System.out
								.println("There is no hasPirte in ecu range in vehicle configuration file");
						System.exit(-1);
					}
					String hasPirteStr = hasPirteElement.getTextContent();
					if (hasPirteStr.equals("true")) {
						// ports
						Element portsElement = (Element) swcElement
								.getElementsByTagName("ports").item(0);
						if (portsElement == null) {
							System.out
									.println("There is no ports element in ecu range in vehicle configuraiton file");
							System.exit(-1);
						}
						NodeList portsList = portsElement
								.getElementsByTagName("port");
						for (int j = 0; j < portsList.getLength(); j++) {
							// port
							Element portElement = (Element) portsList.item(j);
							Port port = new Port();

							// port ID
							Element portIdElement = (Element) portElement
									.getElementsByTagName("id").item(0);
							if (portIdElement == null) {
								System.out
										.println("There is no id element in port range in vehicle configuration file");
								System.exit(-1);
							}
							String portIdStr = portIdElement.getTextContent();
							int portId = Integer.parseInt(portIdStr);

							portId2EcuId.put(portId, ecuId);

							System.out.println("      <id>" + portIdStr
									+ "</id>");
							port.setPortId(portId);

							// port direction
//							Element portDirectionElement = (Element) portElement
//									.getElementsByTagName("direction").item(0);
//							if (portDirectionElement == null) {
//								System.out
//										.println("There is no direction in port range in vehicle configuration file");
//								System.exit(-1);
//							}
//							String portDirectionStr = portDirectionElement
//									.getTextContent();
//							System.out.println("      <direction>"
//									+ portDirectionStr + "</direction>");
//							port.setDirection(portDirectionStr);

							port.setEcu(ecu);

							System.out.println("    </port>");

							vehicleConfigDao.savePort(port);
						}

						System.out.println("  </ports>");
					}
				}
			}
			System.out.println("</ecu>");

			// links
			NodeList links = root.getElementsByTagName("link");

			for (int i = 0; i < links.getLength(); i++) {
				// link
				Element linkElement = (Element) links.item(i);

				// type
				Element typeElement = (Element) linkElement
						.getElementsByTagName("type").item(0);
				if (typeElement == null) {
					System.out
							.println("There is no type element in link range in vehicle configuration file");
					System.exit(-1);
				}
				String typeStr = typeElement.getTextContent();
				int type = Integer.parseInt(typeStr);

				// fromPort
				Element fromPortElement = (Element) linkElement
						.getElementsByTagName("from").item(0);
				if (fromPortElement == null) {
					System.out
							.println("There is no from element in link range in vehicle configuration file");
					System.exit(-1);
				}
				String fromPortStr = fromPortElement.getTextContent();
				int fromPortId = Integer.parseInt(fromPortStr);

//				Integer fromEcuId;
//				if (fromPortId >= 100)
//					fromEcuId = -1;
//				else
//					fromEcuId = portId2EcuId.get(fromPortId);
				int fromEcuId = portId2EcuId.get(fromPortId);
				
				// toPort
				Element toPortElement = (Element) linkElement
						.getElementsByTagName("to").item(0);
				if (toPortElement == null) {
					System.out
							.println("There is no to element in link range vehicle configuration file");
					System.exit(-1);
				}
				String toPortStr = toPortElement.getTextContent();
				int toPortId = Integer.parseInt(toPortStr);

//				Integer toEcuId;
//				if (toPortId >= 100)
//					toEcuId = -1;
//				else
//					toEcuId = portId2EcuId.get(toPortId);
				int toEcuId = portId2EcuId.get(toPortId);
				
				Link link = new Link(type, fromEcuId, toEcuId, fromPortId,
						toPortId);
				link.setVehicleConfig(vehicleConfig);
				vehicleConfigDao.saveLink(link);

				System.out.println("Done saving config!!!!!!");
			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "true";

	}

	@WebMethod
	public String parsePluginConfiguration(int appId, String path)
			throws PluginWebServicesException {
		AppConfig appConfig = new AppConfig();
		// key: portName, PluginPortConfig
		HashMap<String, PluginConfig> portName2PluginConfigs = new HashMap<String, PluginConfig>();

		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder;
		try {
			dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(path);
			Document doc = dombuilder.parse(is);
			// vehicle
			Element root = doc.getDocumentElement();
			System.out.println("Start parsing XML");

			appConfig.setAppId(appId);

			// vehicle name
			Element vehicleNameElement = (Element) root.getElementsByTagName(
					"vehicleName").item(0);
			String vehicleNameStr = vehicleNameElement.getTextContent();
			appConfig.setVehicleName(vehicleNameStr);
			System.out.println("vehicle name: " + vehicleNameStr);

			// brand
			Element brandElement = (Element) root.getElementsByTagName("brand")
					.item(0);
			String brandStr = brandElement.getTextContent();
			appConfig.setBrand(brandStr);
			System.out.println("brand:" + brandStr);

			appConfigDao.saveAppConfig(appConfig);

			// list of plug-in
			Element plugins = (Element) root.getElementsByTagName("plugins")
					.item(0);
			Set<PluginConfig> pluginConfigs = new HashSet<PluginConfig>();

			NodeList pluginList = plugins.getElementsByTagName("plugin");
			for (int i = 0; i < pluginList.getLength(); i++) {
				PluginConfig pluginConfig = new PluginConfig();

				// get plug-in
				Element plugin = (Element) pluginList.item(i);

				// get name
				Element pluginNameElement = (Element) plugin
						.getElementsByTagName("name").item(0);
				String pluginNameStr = pluginNameElement.getTextContent();
				pluginConfig.setName(pluginNameStr);
				System.out.println("plugin name: " + pluginNameStr);

				// get ECU id
				Element ecuElement = (Element) plugin.getElementsByTagName(
						"ecu").item(0);
				int ecuId = Integer.parseInt(ecuElement.getTextContent());
				pluginConfig.setEcuId(ecuId);
				System.out.println("ecu id:" + ecuId);

				// get ports
				Element portsElement = (Element) plugin.getElementsByTagName(
						"ports").item(0);
				// LinkedList<ExperimentalPluginPortConfig> ports = new
				// LinkedList<ExperimentalPluginPortConfig>();

				// get list of ports
				NodeList portList = portsElement.getElementsByTagName("port");

				pluginConfig.setAppConfig(appConfig);
				appConfigDao.savePluginConfig(pluginConfig);

				// get port
				for (int j = 0; j < portList.getLength(); j++) {
					Element port = (Element) portList.item(j);

					// get name
					Element portNameElement = (Element) port
							.getElementsByTagName("name").item(0);
					String portNameStr = portNameElement.getTextContent();
					PluginPortConfig pluginPortConfig = new PluginPortConfig();
					pluginPortConfig.setName(portNameStr);
					portName2PluginConfigs.put(portNameStr, pluginConfig);
					System.out.println("port name:" + portNameStr);

					pluginPortConfig.setPluginConfig(pluginConfig);
					appConfigDao.savePluginPortConfig(pluginPortConfig);
				}

				pluginConfigs.add(pluginConfig);
			}

			appConfig.setPluginConfigs(pluginConfigs);

			// get links
			Element links = (Element) root.getElementsByTagName("links")
					.item(0);

			// get list of links
			NodeList linkList = links.getElementsByTagName("link");
			for (int k = 0; k < linkList.getLength(); k++) {
				// get link
				Element linkElement = (Element) linkList.item(k);

				// get from
				Element fromElement = (Element) linkElement
						.getElementsByTagName("from").item(0);
				// Element fromTypeElement = (Element) fromElement
				// .getElementsByTagName("type").item(0);
				// String fromTypeStr = fromTypeElement.getTextContent();
				// Element fromNameElement = (Element) fromElement
				// .getElementsByTagName("name").item(0);
				String fromNameStr = fromElement.getTextContent();
				boolean fromType = isPluginPort(fromNameStr,
						portName2PluginConfigs);
				// get to
				Element toElement = (Element) linkElement.getElementsByTagName(
						"to").item(0);
				// Element toTypeElement = (Element) toElement
				// .getElementsByTagName("type").item(0);
				// String toTypeStr = toTypeElement.getTextContent();
				// Element toNameElement = (Element) toElement
				// .getElementsByTagName("name").item(0);
				String toNameStr = toElement.getTextContent();
				boolean toType = isPluginPort(toNameStr, portName2PluginConfigs);

				if (fromType && toType) {
					// fromType = PluginPort, toType = PluginPort

					// judge whether these two PlugIns are located in the same
					// ECU or different ECUs.
					int fromEcuId = portName2PluginConfigs.get(fromNameStr)
							.getEcuId();
					int toEcuId = portName2PluginConfigs.get(toNameStr)
							.getEcuId();
					if (fromEcuId != toEcuId) {
						// different ECUs
						int[] link = vehicleConfigDao.getType2PortId(
								vehicleNameStr, brandStr, fromEcuId, toEcuId);
						if (link == null) {
							System.out
									.println("Error: "
											+ fromNameStr
											+ " - "
											+ toNameStr
											+ " is allocated to wrong virtual port for link");
							System.exit(-1);
						}

						// int type2rPortId =
						// vehicleConfigDao.getType2PortId(vehicleNameStr,
						// brandStr, fromEcuId);
						// if(type2rPortId == -1 ) {
						// System.out.println("Error: "+ toNameStr+
						// " is allocated to wrong virtual port for link, toEcuId:"+fromEcuId);
						// System.exit(-1);
						// }

						PluginLinkConfig p2vEntry = new PluginLinkConfig(
								fromNameStr, link[0] + "", toNameStr);

						PluginConfig pluginConfig = portName2PluginConfigs
								.get(fromNameStr);

						p2vEntry.setPluginConfig(pluginConfig);
						appConfigDao.savePluginLinkConfig(p2vEntry);

						PluginLinkConfig v2pEntry = new PluginLinkConfig(
								link[1] + "", toNameStr,
								GlobalVariables.VPORT2PORT + "");

						PluginConfig pluginConfig2 = portName2PluginConfigs
								.get(toNameStr);

						v2pEntry.setPluginConfig(pluginConfig2);

						appConfigDao.savePluginLinkConfig(v2pEntry);
					} else {
						// same ECU
						PluginLinkConfig pluginLinkConfig = new PluginLinkConfig(
								fromNameStr, toNameStr,
								GlobalVariables.PPORT2PPORT + "");

						PluginConfig pluginConfig = portName2PluginConfigs
								.get(fromNameStr);
						pluginLinkConfig.setPluginConfig(pluginConfig);

						appConfigDao.savePluginLinkConfig(pluginLinkConfig);
					}
				} else if (fromType && !toType) {
					// fromType = PluginPort, toType = VirtualPort
					// Plug-In accesses to sensor
					PluginLinkConfig p2vEntry = new PluginLinkConfig(
							fromNameStr, toNameStr, GlobalVariables.PPORT2VPORT
									+ "");

					PluginConfig pluginConfig = portName2PluginConfigs
							.get(fromNameStr);

					p2vEntry.setPluginConfig(pluginConfig);

					appConfigDao.savePluginLinkConfig(p2vEntry);
				} else if (!fromType && toType) {
					// fromType = VirtualPort, toType = PluginPort

					// Sensor accesses to Plug-In
					PluginLinkConfig v2pEntry = new PluginLinkConfig(
							fromNameStr, toNameStr, GlobalVariables.VPORT2PORT
									+ "");

					PluginConfig pluginConfig = portName2PluginConfigs
							.get(toNameStr);

					v2pEntry.setPluginConfig(pluginConfig);

					appConfigDao.savePluginLinkConfig(v2pEntry);
				} else {
					System.out
							.println("Error: wrong port type in plugin configuration file.");
					return "Error: wrong port type in plugin configuration file.";
				}

			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "true";
	}

	private boolean isPluginPort(String portName,
			HashMap<String, PluginConfig> portName2PluginConfigs) {
		return portName2PluginConfigs.containsKey(portName);
	}

	@WebMethod
	public String generateSuite(String zipFile, String fullClassName)
			throws PluginWebServicesException {
		CompressUtils util = new CompressUtils();
		System.out.println("Calling unzip on " + zipFile);
		String dest = util.unzip(zipFile);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Unzipped into: " + dest); 
		dest = dest.substring(0,  dest.length() - 11); //Remove "j2meclasses"
		
		String reply = suiteGen.generateSuite(dest); // + "/" + fullClassName);
		return reply;
	}

	public String install4Jdk(String vin, int appID)
			throws PluginWebServicesException {
		// Fetch the connection session between Server and Vehicle
		IoSession session = ServerHandler.getSession(vin);
		if (session == null) {
			// If null, response user about the disconnection between Sever and
			// Vehicle
			return "false";
		} else {
			// Achieve contexts
			// key: portName(String), value:
			// portId(Integer)>
			HashMap<String, Integer> portInitialContext = new HashMap<String, Integer>();
			// HashMap<String, ArrayList<LinkingContextEntry>> linkingContexts =
			// new HashMap<String, ArrayList<LinkingContextEntry>>();
			HashMap<String, ArrayList<LinkContextEntry>> linkingContexts = new HashMap<String, ArrayList<LinkContextEntry>>();

			// Create an array list for cache
			ArrayList<VehiclePluginRecord> installCachePlugins = new ArrayList<VehiclePluginRecord>();

			Vehicle vehicle = vehicleDao.getVehicle(vin);

			// VehicleConfig
			int vehicleConfigId = vehicle.getVehicleConfigId();
			VehicleConfig vehicleConfig = vehicleConfigDao
					.getVehicleConfig(vehicleConfigId);

			// AppConfig
			String vehicleName = vehicleConfig.getName();
			String brand = vehicleConfig.getBrand();
			AppConfig appConfig = appConfigDao.getAppConfig(appID, vehicleName,
					brand);
			// PluginConfig
			Set<PluginConfig> pluginConfigs = appConfig.getPluginConfigs();

			// Plugin Port Config
			// Build lookup table (PlugIn port name, PlugIn port ID)
			// HashMap<Integer, Integer> pluginId2EcuId = new HashMap<Integer,
			// Integer>();

			for (PluginConfig pluginConfig : pluginConfigs) {
				Set<PluginPortConfig> pluginPortConfigs = pluginConfig
						.getPluginPortConfigs();
				for (PluginPortConfig pluginPortConfig : pluginPortConfigs) {
					int pluginPortId = pluginPortConfig.getId();
					String pluginPortName = pluginPortConfig.getName();
					portInitialContext.put(pluginPortName, pluginPortId);
				}

			}

			// Plugin Link Config
			for (PluginConfig pluginConfig : pluginConfigs) {
				String pluginName = pluginConfig.getName();
				// Initiate LinkingContext
				ArrayList<LinkContextEntry> linkingContext = new ArrayList<LinkContextEntry>();

				Set<PluginLinkConfig> pluginLinkConfigs = pluginConfig
						.getPluginLinkConfigs();
				for (PluginLinkConfig pluginLinkConfig : pluginLinkConfigs) {
					String from = pluginLinkConfig.getFromStr();
					String to = pluginLinkConfig.getToStr();
					String remote = pluginLinkConfig.getRemote();

					int fromPortId = 0;
					int toPortId = 0;
					int remoteId = 0;

					Scanner scanner = new Scanner(remote);
					boolean remoteTag = scanner.hasNextInt();

					if (remoteTag) {
						remoteId = scanner.nextInt();
						switch (remoteId) {
						case GlobalVariables.PPORT2PPORT:
							fromPortId = portInitialContext.get(from);
							toPortId = portInitialContext.get(to);
							break;
						case GlobalVariables.PPORT2VPORT:
							fromPortId = portInitialContext.get(from);
							toPortId = Integer.parseInt(to);
							break;
						case GlobalVariables.VPORT2PORT:
							fromPortId = Integer.parseInt(from);
							toPortId = portInitialContext.get(to);
							break;
						default:
							System.out
									.println("Error: Wrong link type in GlobalVariables");
							System.exit(-1);
						}
					} else {
						// Plug-In -> VRPort
						// remote represents the name of remote port
						remoteId = portInitialContext.get(remote);
						fromPortId = portInitialContext.get(from);
						toPortId = Integer.parseInt(to);

					}

					scanner.close();

					// remoteTag: -1(PPORT2PPORT), -2(PPORT2VPORT),
					// -3(VPORT2PORT)
					// int remoteTag = Integer.parseInt(remote);
					// switch(remoteTag) {
					// case GlobalVariables.PPORT2PPORT:
					// fromPortId = portInitialContext.get(from);
					// toPortId = portInitialContext.get(to);
					// // judge whether they are in the same ECU or different
					// ECUs
					//
					// break;
					// case GlobalVariables.PPORT2VPORT:
					// fromPortId = portInitialContext.get(from);
					// toPortId = Integer.parseInt(to);
					// break;
					// case GlobalVariables.VPORT2PORT:
					// fromPortId = Integer.parseInt(from);
					// toPortId = portInitialContext.get(to);
					// break;
					// default:
					// System.out
					// .println("Error: Wrong link type in GlobalVariables");
					// System.exit(-1);
					// }

					LinkContextEntry entry = new LinkContextEntry(fromPortId,
							toPortId, remoteId);
					linkingContext.add(entry);

				}
				linkingContexts.put(pluginName, linkingContext);
			}

			// Achieve jars
			ArrayList<InstallPacketData> installPackageDataList = new ArrayList<InstallPacketData>();

			// Fetch application data from DB
			Application application = applicationDao.getApplication(appID);
			// Fetch PlugIns from DB
			// HashMap<String, Byte> contexts = new HashMap<String, Byte>();
			Set<DatabasePlugin> plugins = application.getDatabasePlugins();
			for (DatabasePlugin plugin : plugins) {
				// // Generate vehicle PlugIn ID
				// short vehiclePluginID = vehiclePluginDao
				// .generateVehiclePluginID(vin);

				String pluginName = plugin.getZipName();
				int dotIndex = pluginName.lastIndexOf('.');
				String pluginSuiteName = pluginName.substring(0, dotIndex) + ".suite";
				int remoteEcuId = plugin.getReference();
				int sendingPortId = vehicleConfigDao.getSendingPortId(
						vehicleConfigId, remoteEcuId);
				int callbackPortId = vehicleConfigDao.getCallbackPortId(
						vehicleConfigId, remoteEcuId);
				String executablePluginName = "plugin://"
						+ plugin.getFullClassName() + "/" + pluginName;
				// Find PlugIn location. For instance,
				// some_dir/uploaded/app_name/version/kdkdks.zip
				String location = plugin.getZipLocation();
				File file = new File(location);
				byte[] fileBytes;
				try {
					// ArrayList<LinkingContextEntry> linkingContext =
					// linkingContexts
					// .get(pluginName);
					ArrayList<LinkContextEntry> linkingContext = (ArrayList<LinkContextEntry>) linkingContexts
							.get(pluginSuiteName);
					fileBytes = readBytesFromFile(file);
					InstallPacketData installPacketData = new InstallPacketData(
							appID, pluginName/* +".zip" */, sendingPortId,
							callbackPortId, remoteEcuId, portInitialContext,
							linkingContext, executablePluginName, fileBytes); // NOTE:
																				// the
																				// portIntialContext
																				// includes
																				// all
																				// the
																				// ports
																				// of
																				// all
																				// the
																				// PlugIns.
																				// In
																				// the
																				// future,
																				// it
																				// should
																				// only
																				// include
																				// all
																				// the
																				// port
																				// of
																				// the
																				// specific
																				// PlugIn.

					installPackageDataList.add(installPacketData);

					// Store it temporarily to cache and will be used after the
					// arrival of acknowledge messages
					VehiclePluginRecord record = new VehiclePluginRecord(
							pluginName, remoteEcuId, sendingPortId,
							callbackPortId, portInitialContext, linkingContext,
							location, executablePluginName);

					installCachePlugins.add(record);

				} catch (IOException e) {
//					System.out.println("This application has already been installed.");
//					return "false2";
					e.printStackTrace();
				}
			}

			Cache.getCache().addInstallCache(vin, appID, installCachePlugins);
			InstallPacket installPacket = new InstallPacket(vin,
					installPackageDataList);
			session.write(installPacket);
			return "true";
		}
	}

}
