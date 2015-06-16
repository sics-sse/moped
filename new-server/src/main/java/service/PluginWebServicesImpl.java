package service;

import org.json.*;

import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.io.*;

import service.CallMySql;
import service.MySqlIterator;

import common.MopedException;

import java.lang.NumberFormatException;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.StringBufferInputStream;
import java.io.ObjectInputStream;
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
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Schema;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cache.Cache;
import cache.VehiclePluginRecord;
import common.GlobalVariables;
import messages.InstallPacket;
import messages.InstallPacketData;
import messages.LinkContextEntry;
import messages.RestorePacket;
import messages.PingcarPacket;
import messages.UninstallPacket;
import messages.UninstallPacketData;
import mina.ServerHandler;
import service.exception.PluginWebServicesException;
import utils.CompressUtils;
import utils.SuiteGen;

import java.sql.*;


import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

class LoggingErrorHandler implements ErrorHandler {

    public String message = "";

    private boolean isValid = true;

    public boolean isValid() {
        return this.isValid;
    }

    @Override
    public void warning(SAXParseException exc) {
	System.out.println("xml warning " + exc);
        // log info
        // valid or not?
    }

    @Override
    public void error(SAXParseException exc) {
	message += exc;
	System.out.println("xml error " + exc);
        // log info
        this.isValid = false;
    }

    @Override
    public void fatalError(SAXParseException exc) throws SAXParseException {
	message += exc;
	System.out.println("xml fatalerror " + exc);
        // log info
        this.isValid = false;
        throw exc;
    }
}

@WebService(endpointInterface = "service.PluginWebServices")
public class PluginWebServicesImpl implements PluginWebServices {
    private SuiteGen suiteGen = new SuiteGen("/home/arndt/moped/moped/squawk");
	
    private ServerHandler handler = null;
	
    @Resource
        WebServiceContext ctx;

    private Connection dbLite = null;
    private Statement stat = null;
	
    private CallMySql mysql = new CallMySql();

    public PluginWebServicesImpl(ServerHandler handler) {
	this.handler = handler;
		
    }
	
    @Override
	public String uploadApp(byte [] data, String appname,
				 String fversion)
	throws PluginWebServicesException {
	int rows;

	int appId;

	System.out.println(data.length);
	System.out.println("filename " + appname + " " + fversion);
	File configFile = null;

	String location;
	String name;

	try {
	    
	    //File jarFile = File.createTempFile("apptemp.jar", null);

	    String dir = "/home/arndt/moped/moped/webportal/moped_plugins/" + appname
		+ "/" + fversion;
	    new File(dir).mkdirs();
	    String p = dir + "/" + appname + ".jar";
	    // I don't know why we must delete the old one first, but if we don't,
	    // it seems its old data remains.
	    new File(p).delete();
	    OutputStream out = new BufferedOutputStream
		(Files.newOutputStream(Paths.get(p), CREATE));
	    out.write(data, 0, data.length);
	    out.close();

	    location = dir + "/";
	    name = appname;

	    File jarFile = new File(p);
	    //		BufferedWriter writer = new BufferedWriter(new FileWriter(jarFile));
	    //BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(jarFile));
	    //bos.write(data);
	    //bos.close();

	    JarFile jar = new JarFile(jarFile);
	    Manifest mf = jar.getManifest();
	    if (mf != null) {
		Attributes attributes = mf.getMainAttributes();
		String publisher = attributes.getValue("Built-By");
		if (publisher == null)
		    publisher = "unknown";
		System.out.println("publisher " + publisher);
		String version = attributes.getValue("Manifest-Version");
		if (version == null)
		    version = "1.0";
		String brand = attributes.getValue("Vehicle-Brand");
		if (brand == null)
		    brand = "SICS";
		String vehicleConfigName = attributes.getValue("Vehicle-Name");
		if (vehicleConfigName == null)
		    vehicleConfigName = "MOPED";
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
			String fullClassName1 = fileName.substring(0, fileName.length() - 6);
			System.out.println("class file " + fullClassName1);
			if (fullClassName1.endsWith(appname)) {
			    fullClassName = fullClassName1;
			}
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
		    } else {
			System.out.println("file in jar: " + fileName);
		    }
		}
		System.out.println("class name " + fullClassName);
		System.out.println("config file " + configFileName);

				
		String q1 = "select id from Application where " +
		    "name = '" + name +
		    //"' and publisher = '" + publisher +
		    "' and version = '" + version + 
		    "' and state >= '020-uploaded' and state < '060'";
		String c1 = mysql.getOne(q1);

		System.out.println("old App " + c1);

		// we delete the old one
		// but maybe this should be an error
		if (!c1.equals("none")) {
		    System.out.println("setting 060");
		    String q1u = "update Application set state='060-pending-delete' where id=" + c1;
		    int rows1u = mysql.update(q1u);
		    System.out.println("setting 060 " + rows1u);
		    if (rows1u != 1)
			return jsonError("upload: internal db error 1");
		}


		String q2 = "insert into Application " +
		    "(name,publisher,state,version,hasNewVersion) values ('" +
		    name + "','" +
		    publisher + "','" +
		    "010-created" + "','" +
		    version + "',0)";
		rows = mysql.update(q2);

		String q3 = "select id from Application " +
		    "where name = '" + name +
		    "' and publisher = '" + publisher +
		    "' and version = '" + version + 
		    "' and state < '060'";
		String c3 = mysql.getOne(q3);
		appId = Integer.parseInt(c3);

		System.out.println("new appId " + appId);

		String q4 = "insert into AppConfig (application_id,brand,vehicleConfigName) values (" +
		    appId + "," +
		    "'" + brand +"'" + "," +
		    "'" + vehicleConfigName + "'" + ")";
		int x4 = mysql.update(q4);

		String q41 = "select max(id) from AppConfig where application_id = " + appId + " and vehicleConfigName = '" + vehicleConfigName + "'";
		String x41 = mysql.getOne(q41);
		int appConfigId = Integer.parseInt(x41);

		String q5 = "insert into DatabasePlugin (name,fullClassName,ecuRef,location,application_id) values (" +
		    "'" + name + "'" + "," +
		    "'" + fullClassName + "'" + "," +
		    Integer.parseInt(ecuRef) + "," +
		    "'" + location + File.separator + name + "'" + "," +
		    appId + ")";
		int x5 = mysql.update(q5);


		//TODO: Why + .suite???

		//TODO: Inconsequence
				
		// Why do we match with name, why with ecuRef, and why
		// do we want to keep the old one at all?
		String q30 = "select * from PluginConfig where ecuId = " +
		    Integer.parseInt(ecuRef) + " and " +
		    "name = " + "'" + name + ".suite" +"'" + " and " +
		    "appConfig_id = " + appConfigId;
		String c30 = mysql.getOne(q3);
		System.out.println("existing PluginConfig: " + c30);

		int pluginConfig;
		if (c30.equals("none")) {
		    System.out.println("pluginconfig already exists");
		}

		String q31 = "insert into PluginConfig (ecuId,name,appConfig_id) values (" +
		    Integer.parseInt(ecuRef) + "," +
		    "'" + name + ".suite" +"'" + "," +
		    appConfigId + ")";
		int x3 = mysql.update(q31);
		System.out.println("updated rows " + x3);

		String q32 = "select * from PluginConfig where ecuId = " +
		    Integer.parseInt(ecuRef) + " and " +
		    "name = " + "'" + name + ".suite" +"'" + " and " +
		    "appConfig_id = " + appConfigId;
		String c32 = mysql.getOne(q32);
		pluginConfig = Integer.parseInt(c32);

		//TODO: Refactor
		// Xml-parsing
		Document doc = DocumentBuilderFactory.newInstance().
		    newDocumentBuilder().parse(configFile);
		doc.getDocumentElement().normalize();
				
		NodeList ports = doc.getElementsByTagName("port");
		for (int i = 0; i < ports.getLength(); i++) {
		    Element port = (Element)ports.item(i);
		    String portName = port.getElementsByTagName("name").item(0).getTextContent();
					
		    Element e1 = (Element) port.getParentNode().getParentNode();
		    Element e2 = (Element) e1.getElementsByTagName("name").item(0);
		    String ppluginName = e2.getTextContent();
		    System.out.println("pluginName for port = " + ppluginName);

		    String q6 = "insert into PluginPortConfig (name,pluginConfig_id,portPluginName) values ('" + portName + "'," + pluginConfig + ",'" + ppluginName +"'" + ")";
		    int rows6 = mysql.update(q6);

		}
				
		NodeList links = doc.getElementsByTagName("link");
		for (int i = 0; i < links.getLength(); i++) {
		    Element link = (Element)links.item(i);
		    // link may be null; check for that
		    // or there is no "from" or "to"
		    String linkSource = link.getElementsByTagName("from").item(0).getTextContent();
		    String linkTarget = link.getElementsByTagName("to").item(0).getTextContent();

		    int connectionType = GlobalVariables.PPORT2PPORT;
		    if (linkSource.matches("(\\d+)")) {
			connectionType = GlobalVariables.VPORT2PORT;
		    }
		    if (linkTarget.matches("(\\d+)")) {
			connectionType = GlobalVariables.PPORT2VPORT;
		    }

		    String q6 = "insert into PluginLinkConfig (fromStr,toStr,connectionType,pluginConfig_id) values ('" + linkSource + "','" + linkTarget + "','" + connectionType + "'," + pluginConfig + ")";
		    int rows6 = mysql.update(q6);
		}

		String q7 = "update Application set state='020-uploaded' where id=" + appId;
		int rows7 = mysql.update(q7);
		if (rows7 != 1)
		    return jsonError("uploadApp: internal db error 2");
	    }
	    else {
		System.out.println("manifest is NULL");
		return jsonError("uploadApp: no manifest");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	    return jsonError("uploadApp: I/O exception");
	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("uploadApp: SAXException");
	} catch (ParserConfigurationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("uploadApp: ParserConfigurationException");
	} catch (FactoryConfigurationError e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("uploadApp: FactoryConfigurationError");
	} 

	{
	    JSONObject o = new JSONObject();
	    JSONObject o1 = new JSONObject();

	    o.put("result", appId);
	    o.put("error", false);
	    return o.toString();
	}
    }

    @Override
	public boolean get_ack_status(String vin, int appId)
	throws PluginWebServicesException {

	String q1 =
	    "select * from Application " +
	    "where id = " + appId;
	String x = mysql.getOne(q1);

	if (x.equals("none")) {
	    return false;
	}

	String q2 =
	    "select name from Application " + 
	    "where id = " + appId;
	String name = mysql.getOne(q2);

	if (handler.existsAckMessage(vin + "_" + name))
	    return true;
		
	return false;
    }
	
    private String jsonError(String msg) {
	JSONObject o = new JSONObject();
	JSONObject o1 = new JSONObject();

	o.put("result", o1);
	o.put("error", true);
	o.put("message", msg);
	return o.toString();
    }

    private String jsonOK() {
	JSONObject o = new JSONObject();
	JSONObject o1 = new JSONObject();

	o.put("result", true);
	o.put("error", false);
	return o.toString();
    }

    private String jsonOK(String s) {
	JSONObject o = new JSONObject();
	JSONObject o1 = new JSONObject();

	o.put("result", s);
	o.put("error", false);
	return o.toString();
    }

    private String checkVIN(String vin) {
	String q1 =
	    "select * from Vehicle where vin = '" + vin + "'";
	String c = mysql.getOne(q1);

	if (c.equals("error")) {
	    return jsonError("internal db error");
	}

	if (c.equals("none")) {
	    return jsonError("no such car " + vin);
	}
	return null;
    }

    private String checkUser(int user_id) {
	String q1 =
	    "select * from wp_users where ID = " + user_id;
	String c = mysql.getOne(q1);

	if (c.equals("error")) {
	    return jsonError("internal db error");
	}

	if (c.equals("none")) {
	    return jsonError("no such user " + user_id);
	}
	return null;
    }

    private String checkApp(int appID) {
	String q11 =
	    "select * from Application where id = " + appID;
	String c11 = mysql.getOne(q11);

	if (c11.equals("error")) {
	    return jsonError("internal db error");
	}

	if (c11.equals("none")) {
	    return jsonError("no such app " + appID);
	}
	return null;
    }

    @Override
    @SuppressWarnings("unchecked")
	public String installApp(String vin, int appID)
	throws PluginWebServicesException {
	System.out.println("vin in install(): " + vin);
	System.out.println("appID in install(): " + appID);
		
	String cs1 = checkVIN(vin);
	if (cs1 != null)
	    return cs1;

	String q1 = "select vehicleConfig_id from Vehicle where vin = '" + vin + "'";
	String c = mysql.getOne(q1);

	// Fetch the connection session between Server and Vehicle
	IoSession session = ServerHandler.getSession(vin);
	if (session == null) {
	    // If null, there is no connection between the server and the vehicle
	    System.out.println("IoSession is NULL");
	    return jsonError("no connection with car " + vin);
	} 

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

	String q2 =
	    "select name,brand from VehicleConfig where id = " + c;
	String [] c2 = mysql.getOneSet(q2);
	if (c2 != null) {
	    System.out.println("vehicleconf name " + c2[0]);
	    System.out.println("vehicleconf brand " + c2[1]);
	}

	String q7 = "select vehicleConfig_id from Vehicle where vin = '" + vin + "'";
	String c7 = mysql.getOne(q7);

	if (c7.equals("none")) {
	    System.out.println("ERROR: no appropriate Vehicle/VehicleConfig combination");
	    return jsonError("no appropriate Vehicle/VehicleConfig combination");
	}

	// VehicleConfig
	int vehicleConfigId = Integer.parseInt(c7);
			
	// AppConfig
	String vehicleConfigName = c2[0];
	String brand = c2[1];

	//System.out.println("Found vehicle: " + vehicleName + " of brand: " + brand + "... (next step not implemented yet)");
	// what's this "next step"?

	String q3s = "select state from Application where id = " + appID +
	    " and state < '060'";
	String c3s = mysql.getOne(q3s);

	if (c3s.equals("error")) {
	    return jsonError("installApp: internal db error 3");
	}

	if (c3s.equals("none")) {
	    return jsonError("installApp: internal db error 4");
	}

	if (c3s.compareTo("030-compiled") < 0) {
	    return jsonError("installApp: application in wrong state: " + c3s);
	}

	// Is it necessary to match with brand here?
	String q3 = "select id from AppConfig where application_id = " + appID
	    + " and vehicleConfigName = '" +
	    vehicleConfigName + "'";
	String c3 = mysql.getOne(q3);
	System.out.println("appconfig id " + c3);
	if (c3.equals("none")) {
	    System.out.println("ERROR: no appropriate AppConfig exists");
	    return jsonError("no appropriate AppConfig exists");
	}
	int appConfigId = Integer.parseInt(c3);

	System.out.println("AppConfig found, id: " + appConfigId);

	// PluginConfig
	//TODO: Move it to a warmer place
	if (!c3.equals("none")) {
	    // can it happen that there is no AppConfig?
	    int pluginConfigId;

	    String q4 = "select id,name from PluginConfig where appConfig_id = " + c3;
	    ResultSet rs = mysql.getResults(q4);
	    try {
		while (rs.next()) {
		    String c4 = rs.getString(1);
		    System.out.println("rs: " + c4);
		    pluginConfigId = Integer.parseInt(c4);
		    System.out.println("again pluginConfig.id: " + pluginConfigId);

		    String q41 = "select id,name,portPluginName from PluginPortConfig where pluginConfig_id = " + pluginConfigId;

		    ResultSet rs2 = mysql.getResults(q41);
		    try {
			while (rs2.next()) {
			    int pluginPortId = Integer.parseInt
				(rs2.getString(1));
			    String pluginPortName = rs2.getString(2);
			    String portPluginName = rs2.getString(3);

			    String q41a = "select id from PluginPortConfig where name = '" + pluginPortName + "' and portPluginName = '" + portPluginName + "'";
			    String s41a = mysql.getOne(q41a, false);
			    System.out.println("actual id = " + s41a);

			    pluginPortId = Integer.parseInt(s41a);

			    System.out.println("initial context " +
					       pluginPortName + " " +
					       pluginPortId);
			    portInitialContext.put(pluginPortName, pluginPortId);
			}
			rs2.close();
		    } catch (SQLException ex) {
			System.out.println("db error 1");
			System.out.println(ex.getMessage());
			return jsonError("internal db error 1");
		    } 
		}
		// are they closed automatically?
		rs.close();

	    } catch (SQLException ex) {
		System.out.println("db error 2");
		System.out.println(ex.getMessage());
		return jsonError("internal db error 2");
	    } 


	    //TEMP_DEBUG
	    System.out.println("PORT INITIAL CONTEXTS: ");
	    for (String ctxt : portInitialContext.keySet()) {
		System.out.println("PIC: <" + ctxt + ", " + portInitialContext.get(ctxt) + ">");
	    }
					
	    ResultSet rs3 = mysql.getResults(q4);
	    try {
		while (rs3.next()) {
		    String c4 = rs3.getString(1);
		    System.out.println("rs: " + c4);
		    pluginConfigId = Integer.parseInt(c4);

		    // Plugin Link Config
		    String pluginName = rs3.getString(2);
		    // Initiate LinkingContext
		    ArrayList<LinkContextEntry> linkingContext = new ArrayList<LinkContextEntry>();
				
		    try {


			String q42 = "select id,fromStr,toStr,connectionType from PluginLinkConfig where pluginConfig_id = " + pluginConfigId;
			ResultSet rs4 = mysql.getResults(q42);

			while (rs4.next()) {
			    String from = rs4.getString(2);
			    String to = rs4.getString(3);
			    String remote = rs4.getString(4);
			    System.out.println("port " + from + " " + to + " " + remote);
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
				    return jsonError("installApp: wrong link type " + remoteId);
				}
			    } else {
				// Plug-In -> VRPort
				// remote represents the name of remote port
				remoteId = portInitialContext.get(remote);
				fromPortId = portInitialContext.get(from);
				toPortId = Integer.parseInt(to);
			    }
						
			    scanner.close();

			    System.out.println("linking context " +
					       fromPortId + " " +
					       toPortId + " " +
					       remoteId);
			    LinkContextEntry entry = new LinkContextEntry(fromPortId,
									  toPortId, remoteId);
			    linkingContext.add(entry);
			}
			rs4.close();
			    
		    } catch (SQLException ex) {
			System.out.println("db error 3");
			System.out.println(ex.getMessage());
			return jsonError("internal db error 3");
		    }
				
		    linkingContexts.put(pluginName, linkingContext);
					
		    //TEMP_DEBUG
		    System.out.println("PORT LINKING CONTEXTS (" + pluginName + "): ");
		    for (Iterator<LinkContextEntry> ctxtIter = linkingContext.iterator(); ctxtIter.hasNext(); ) {
			LinkContextEntry ctxt = ctxtIter.next();
			System.out.println("PLC: <" + ctxt.getFromPortId() + ", " + ctxt.getToPortId() + "> via " + ctxt.getRemotePortId());
		    }
		}
		rs3.close();
	    } catch (SQLException ex) {
		System.out.println("db error 4");
		System.out.println(ex.getMessage());
		return jsonError("internal db error 4");
	    }
	}

	// Achieve jars
	ArrayList<InstallPacketData> installPackageDataList = new ArrayList<InstallPacketData>();

			
	if (false) {
	    String q10 =
		"select name from Application where id = " + appID;
	    String c10 = mysql.getOne(q10);
	    System.out.println("application name " + c10);
	}

	// Fetch PlugIns from DB
	// HashMap<String, Byte> contexts = new HashMap<String, Byte>();
	@SuppressWarnings("unchecked")

	    String q12 =
	    "select name,ecuRef,location,fullClassName " +
	    "from DatabasePlugin where application_id = " + appID;
	ResultSet rs12 = mysql.getResults(q12);

	try {
	    //System.out.println("Found plugins, size: " + plugins.size());
	    while (rs12.next()) {
		String pluginName = rs12.getString(1);
		System.out.println("Found plugin with name: " + pluginName);
				
		System.out.println(rs12.getString(2));
		System.out.println(rs12.getString(3));
		System.out.println(rs12.getString(4));

		int remoteEcuId = Integer.parseInt(rs12.getString(2));
		//			int sendingPortId = vehicleConfigDao.getSendingPortId
		//			    (
		//			     vehicleConfigId, remoteEcuId
		//			     );
		//			int callbackPortId = vehicleConfigDao.getCallbackPortId
		//			    (
		//			     vehicleConfigId, remoteEcuId
		//			     );
		//			System.out.println("sendingPortId " + sendingPortId);
		//			System.out.println("callbackPortId " + callbackPortId);

		// there is code in VehicleConfigDao which looks these up,
		// but they seem to always be -1 in the database right now,
		// and it's not clear that it does it right way.
		int sendingPortId = -1;
		int callbackPortId = -1;

		String executablePluginName = "plugin://"
		    + rs12.getString(4) + "/" + pluginName;
		String pluginSuiteName = pluginName + ".suite"; //TODO: Come on...

		String location = rs12.getString(3);
				
		String q71 = "select simulator from Vehicle where vin = '" + vin + "'";
		String c71 = mysql.getOne(q71);
		if (c71.equals("none")) {
		    System.out.println("internal error 71");
		    return jsonError("internal db error");
		}

		System.out.println("c71 (" + c71 + ")");

		String fileType = ".jar";
		if (c71.equals("0")) {
		    fileType = ".suite";
		    location += File.separator + pluginName;
		}
				
		System.out.println("fileType " + fileType);

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
		    InstallPacketData installPacketData =
			new InstallPacketData
			(
			 appID, pluginName, sendingPortId,
			 callbackPortId, remoteEcuId, portInitialContext,
			 linkingContext, executablePluginName, fileBytes
			 );
		    installPackageDataList.add(installPacketData);

		    // Store it temporarily to cache and will be used after the
		    // arrival of acknowledge messages
		    VehiclePluginRecord record =
			new VehiclePluginRecord
			(
			 pluginName, remoteEcuId, sendingPortId,
			 callbackPortId, portInitialContext, linkingContext,
			 location, executablePluginName
			 );

		    installCachePlugins.add(record);
					
		    System.out.println("READY FOR INSTALLATION WRITING!");
					
		    Cache.getCache().addInstallCache(vin, appID, installCachePlugins);
		    InstallPacket installPacket = new InstallPacket(vin,
								    installPackageDataList);
		    session.write(installPacket);
					
		    System.out.println("SUCCESSFULLY INSTALLED SOME STUFF!");

		} catch (IOException e) {
		    e.printStackTrace();
		    // will rs12 not be closed now?
		    return jsonError("installApp: internal error: couldn't read from the app file " + location);
		}
	    }
	    rs12.close();
	} catch (SQLException ex) {
	    System.out.println("db error 5");
	    System.out.println(ex.getMessage());
	    return jsonError("internal db error 5");
	}

	return jsonOK();
    }

    public String uninstallApp(String vin, int appID)
	throws PluginWebServicesException {

	String cs1 = checkVIN(vin);
	if (cs1 != null)
	    return cs1;

	String cs2 = checkApp(appID);
	if (cs2 != null)
	    return cs2;

	// we should report that an app is not installed, even if the
	// car is not available.

	IoSession session = ServerHandler.getSession(vin);
	if (session == null) {
	    return jsonError("uninstallApp: no connection with car " + vin);
	} else {
	    // Fetch un_installation PlugIns
	    ArrayList<UninstallPacketData> uninstallPackageDataList = new ArrayList<UninstallPacketData>();

	    // Save pluginname into array list cache for uninstallation
	    ArrayList<String> uninstallCacheName = new ArrayList<String>();

	    String q2 = "select name,sendingPortId,callbackPortId,ecuId"
		+ " from VehiclePlugin where"
		+ " application_id = " + appID
		+ " and vin = '" + vin + "'";
	    ResultSet rs = mysql.getResults(q2);

	    // We should fetch all rows, but we don't expect there to
	    // be more than one.

	    int inst_n = 0;

	    try {
		while (rs.next()) {
		    inst_n++;

		    String pluginName = rs.getString(1);
		    int sendingPortId = Integer.parseInt(rs.getString(2));
		    int callbackPortId = Integer.parseInt(rs.getString(3));
		    int ecuRef = Integer.parseInt(rs.getString(4));

		    UninstallPacketData uninstallPackageData = new UninstallPacketData(
										       sendingPortId, callbackPortId, ecuRef, pluginName);
		    uninstallPackageDataList.add(uninstallPackageData);

		    uninstallCacheName.add(pluginName);
		}
		rs.close();
		if (inst_n == 0)
		    return jsonError("uninstallApp: app " + appID +
				     " not installed on car " + vin);
	    } catch (SQLException ex) {
		System.out.println("db error 6");
		System.out.println(ex.getMessage());
		return jsonError("internal db error 6");
	    }

	    Cache.getCache().addUninstallCache(vin, appID, uninstallCacheName);

	    UninstallPacket uninstallPackage = new UninstallPacket(vin,
								   uninstallPackageDataList);
	    session.write(uninstallPackage);
	    return jsonOK();
	}
    }

    public boolean upgrade(String vin, int oldAppID)
	throws PluginWebServicesException {
	IoSession session = ServerHandler.getSession(vin);

	// We should do a transaction around q1 and q2.

	String q1 = "select name from Application where"
	    + " id = " + oldAppID;
	String c1 = mysql.getOne(q1);

	System.out.println("upgrade " + c1);

	if (c1.equals("none")) {
	    System.out.println("no such appid");
	    return false;
	}

	String q2 = "select max(id) from Application where"
	    + " name = '" + c1 + "'";
	String c2 = mysql.getOne(q2);

	System.out.println("old " + oldAppID + " new " + c2);
	int newAppId = Integer.parseInt(c2);
	// upgrade only if this is newer?
	// use date or version instead?

	if (oldAppID == newAppId)
	    return false;

	if (session == null) {
	    // If null, response user about the disconnection between Sever and
	    // Vehicle
	    System.out.println("upgrade: no session");
	    return false;
	} else {
	    uninstallApp(vin, oldAppID);
	    try {
		Thread.sleep(2000);
		if (newAppId > -1) {
		    installApp(vin, newAppId); //TODO: This should also be implemented for JDK (all the way to the upgrade button in the php-interface)
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
	public boolean restoreEcu(String vin, int ecuRef)
	throws PluginWebServicesException {
	IoSession session = ServerHandler.getSession(vin);
	if (session == null) {
	    // If null, response user about the disconnection between Sever and
	    // Vehicle
	    return false;
	} else {

	    ArrayList<InstallPacketData> installPackageDataList = new ArrayList<InstallPacketData>();
	    String q1 = "select application_id,name,sendingPortId,"
		+ "callbackPortId,executablePluginName,"
		+ "portInitialContext,portLinkingContext,location"
		+ " from VehiclePlugin where"
		+ " vin = '" + vin + "'"
		+ " and ecuId = " + ecuRef;
	    String [] c1 = mysql.getOneSet(q1);

	    if (c1 == null) {
		System.out.println("nothing in db");
		return false;
	    }

	    String appId = c1[0];
	    String pluginName = c1[1];
	    String sendingPortId = c1[2];
	    String callbackPortId = c1[3];
	    String executablePluginName = c1[4];
	    String portInitialContext_blob = c1[5];
	    String portLinkingContext_blob = c1[6];
	    String location = c1[7];

	    System.out.println("blob " + portInitialContext_blob);
	    System.out.println("blob " + portLinkingContext_blob);

	    HashMap<String, Integer> portInitialContext;
	    ArrayList<LinkContextEntry> portLinkingContext;

	    if (portInitialContext_blob == null) {
		portInitialContext = null;
	    } else {
		try {
		    // deprecated; find what the modern way is
		    StringBufferInputStream fileIn = new StringBufferInputStream
			(portInitialContext_blob);

		    ObjectInputStream in = new ObjectInputStream(fileIn);
		    @SuppressWarnings("unchecked")
			HashMap<String, Integer> portInitialContextW =
			(HashMap<String, Integer>)
			in.readObject();
		    portInitialContext = portInitialContextW;
		    in.close();
		    fileIn.close();
		} catch(IOException e) {
		    System.out.println("reading from object");
		    e.printStackTrace();
		    throw new PluginWebServicesException();
		} catch(ClassNotFoundException e) {
		    System.out.println("class not found");
		    e.printStackTrace();
		    throw new PluginWebServicesException();
		}
	    }

	    if (portLinkingContext_blob == null) {
		portLinkingContext = null;
	    } else {
		try {
		    StringBufferInputStream fileIn = new StringBufferInputStream
			(portLinkingContext_blob);

		    ObjectInputStream in = new ObjectInputStream(fileIn);

		    @SuppressWarnings("unchecked")
			ArrayList<LinkContextEntry> portLinkingContextW =
			(ArrayList<LinkContextEntry>)
			in.readObject();
		    portLinkingContext = portLinkingContextW;
		    in.close();
		    fileIn.close();
		} catch(IOException e) {
		    System.out.println("reading from object");
		    e.printStackTrace();
		    throw new PluginWebServicesException();
		} catch(ClassNotFoundException e) {
		    System.out.println("class not found");
		    e.printStackTrace();
		    throw new PluginWebServicesException();
		}
	    }

	    //HashMap<String, Integer> portInitialContext;
	    //ArrayList<LinkContextEntry> portLinkingContext;

	    System.out.println("restoreEcu 1");

	    File file = new File(location);
	    byte[] fileBytes;
	    try {
		fileBytes = readBytesFromFile(file);
		InstallPacketData installPackageData =
		    new InstallPacketData
		    (
		     Integer.parseInt(appId),
		     pluginName,
		     Integer.parseInt(sendingPortId),
		     Integer.parseInt(callbackPortId),
		     ecuRef,
		     portInitialContext,
		     portLinkingContext,
		     executablePluginName,
		     fileBytes
		     );
		installPackageDataList.add(installPackageData);
	    } catch (IOException e) {
		System.out
		    .println("Error! Fail to read PlugIn file from Server.");
		return false;
	    }

	    System.out.println("restoreEcu 2");
	    RestorePacket restorePackage = new RestorePacket
		(vin,
		 installPackageDataList);
	    session.write(restorePackage);
	    return true;
	}

    }

    @WebMethod
	public String tellVehicle(String vin, int type, int val, String msg)
	throws PluginWebServicesException {
	IoSession session = ServerHandler.getSession(vin);
	if (session == null) {
	    return jsonError("no connection with car " + vin);
	}

	PingcarPacket pingcarPacket = new PingcarPacket
	    (vin, val);
	pingcarPacket.type = type;
	pingcarPacket.msg = msg;
	session.write(pingcarPacket);
	return jsonOK();
    }

    @WebMethod
	public String addVehicleConfig(String name, byte [] data)
	throws PluginWebServicesException {
	boolean status = false;
	System.out.println("In parseVehicleConfigurationFromStr");

	try {
	    File xmlFile = File.createTempFile("apptemp.jar", null);

	    BufferedWriter writer = new BufferedWriter(new FileWriter(xmlFile));
	    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(xmlFile));
	    bos.write(data);
	    bos.close();

	    FileInputStream is = new FileInputStream(xmlFile);

	    return parseVehicleConfiguration2(name, is);

	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("addVehicleConfig: tmp file error");
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("addVehicleConfig: I/O error");
	}
    }

    // returns JSON
    private String parseVehicleConfiguration2(String name, InputStream is)
	throws PluginWebServicesException {
	System.out.println("In parseVehicleConfiguration2");

	// key: port ID, value: ECU ID
	HashMap<Integer, Integer> portId2EcuId = new HashMap<Integer, Integer>();

	DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
	try {
	    domfac.setIgnoringElementContentWhitespace(true);
	    domfac.setNamespaceAware(true);
	    final SchemaFactory sf = 
		SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	    String SCHEMA_PATH = "/home/arndt/moped/moped/xml/vehicleconfig.xsd";
	    File f = new File(SCHEMA_PATH);
	    //SCHEMA_PATH = "http://merkur.sics.se/VehicleConfig/1.0/VehicleConfig.xsd";
	    //domfac.setValidating(true);  
	    //	    final Schema schema = sf.newSchema
	    //		(new StreamSource(getClass().getResourceAsStream(SCHEMA_PATH)));
	    final Schema schema = sf.newSchema
		(new StreamSource(f));
	    domfac.setSchema(schema);


	    DocumentBuilder dombuilder = domfac.newDocumentBuilder();

	    LoggingErrorHandler eh = new LoggingErrorHandler();
	    dombuilder.setErrorHandler(eh);


	    Document doc = dombuilder.parse(is);
	    if (!eh.isValid()) {
		return jsonError(eh.message);
	    }
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

	    if (!vehicleNameStr.equals(name)) {
		return jsonError("vehicle configuration: specified name not the same in the xml document: " + name + ", " + vehicleNameStr);
	    }

	    int rows;
	    String q1 = "select id from VehicleConfig where "
		+ "name = '" + vehicleNameStr + "'";
	    String c1 = mysql.getOne(q1);

	    if (!c1.equals("none")) {
		String q3 = "update VehicleConfig set name = '_deleted_' where id = " + c1;
		rows = mysql.update(q3);
	    }

	    /*
	      delete c.id from Ecu a,VehicleConfig b,Port c where a.vehicleConfig_id = b.id and b.name='_deleted_' and c.ecu_id=a.id;

	      delete a from Ecu a,VehicleConfig b where a.vehicleConfig_id = b.id and b.name='_deleted_';

	      delete b from VehicleConfig b where name='_deleted_';
	    */


	    if (false) {
		String q3 = "update Link set vehicleConfig_id = 0"
		    + " where vehicleConfig_id = " + c1;
		rows = mysql.update(q3);

	    }

	    String q2 = "insert into VehicleConfig (brand,name) " +
		"values ('" + vehicleBrandStr
		+ "','" + vehicleNameStr + "')";
	    rows = mysql.update(q2);

	    String q3 = "select id from VehicleConfig where"
		+ " name = '" + vehicleNameStr + "'";
	    String c3 = mysql.getOne(q3);

	    if (false) {
		// VIN
		Element vinElement = (Element) root.getElementsByTagName("vin").item(0);
		if(vinElement == null) {
		    System.out.println("There is no VIN element in vehicle configuration file");
		    return jsonError("vehicle configuration: no <vin> element");
		}
		String vinStr = vinElement.getTextContent();
			
		String q10 = "select * from Vehicle where vin = '" + vinStr + "'";
		String c10 = mysql.getOne(q10);
		if (c10.equals("none")) {
		    String q11 = "insert into Vehicle"
			+ " (name,vin,vehicleConfig_id) values "
			+ "('" + vehicleNameStr
			+ "','" + vinStr
			+ "'," + c3 + ")";
		    rows = mysql.update(q11);
		    System.out.println("vehicle created " + vinStr);
		} else {
		    System.out.println("vehicle exists " + vinStr);
		}
	    }

	    // ecus
	    Element ecusElement = (Element) root.getElementsByTagName("ecus")
		.item(0);
	    if (ecusElement == null) {
		System.out.println
		    ("There is no ecus element in vehicle configuration file");
		return jsonError("vehicle configuration: no <ecus> element");
	    }

	    NodeList ecuList = ecusElement.getElementsByTagName("ecu");

	    for (int i = 0; i < ecuList.getLength(); i++) {
		// ecu
		System.out.println("<ecu>");
		Element ecuElement = (Element) ecuList.item(i);
		if (ecuElement == null) {
		    System.out.println
			("There is no ecu element in vehicle " +
			 "configuration file");
		    return jsonError("vehicle configuration: no <ecu> element");
		}
		Element idElement = (Element)
		    ecuElement.getElementsByTagName("id").item(0);
		if (idElement == null) {
		    System.out.println
			("There is no id element " +
			 "in ecu range in vehicle configuration file");
		    return jsonError("vehicle configuration: no ecus/ecu/id element");
		}
		String ecuIdStr = idElement.getTextContent();
		System.out.println("  <id>" + ecuIdStr + "</id>");
		int ecuId = Integer.parseInt(ecuIdStr);

		String q4 = "insert into Ecu"
		    + " (ecuId,vehicleConfig_id) values "
		    + "(" + ecuId + "," + c3 + ")";
		rows = mysql.update(q4);

		String q41 = "select id from Ecu where"
		    + " vehicleConfig_id = " + c3
		    + " and ecuId = " + ecuId;
		String ecu_id = mysql.getOne(q41);

		// swcs
		Element swcsElement = (Element) ecuElement
		    .getElementsByTagName("swcs").item(0);
		if (swcsElement == null) {
		    System.out.println("There is no swcs element in ecu range in vehicle configuration file");
		    return jsonError("vehicle configuration: no ecus/ecu/swcs element");
		}
		NodeList swcList = swcsElement.getElementsByTagName("swc");
		for (int s = 0; s < swcList.getLength(); s++) {
		    // swc
		    Element swcElement = (Element) swcList.item(s);
		    if (swcElement == null) {
			System.out.println
			    ("There is no swc element " +
			     "in ecu range in vehicle configuration file");
			return jsonError("vehicle configuration: no ecus/ecu/swcs/swc element");
		    }
		    // hasPirte
		    Element hasPirteElement = (Element) swcElement
			.getElementsByTagName("hasPirte").item(0);
		    if (hasPirteElement == null) {
			System.out
			    .println("There is no hasPirte in ecu range in vehicle configuration file");
			return jsonError("vehicle configuration: no hasPirte element");
		    }
		    String hasPirteStr = hasPirteElement.getTextContent();
		    if (hasPirteStr.equals("true")) {
			// ports
			Element portsElement = (Element) swcElement
			    .getElementsByTagName("ports").item(0);
			if (portsElement == null) {
			    System.out
				.println("There is no ports element in ecu range in vehicle configuraiton file");
			    return jsonError("vehicle configuration: no <ports> element");
			}
			NodeList portsList = portsElement
			    .getElementsByTagName("port");
			for (int j = 0; j < portsList.getLength(); j++) {
			    // port
			    Element portElement = (Element) portsList.item(j);

			    // port ID
			    Element portIdElement = (Element) portElement
				.getElementsByTagName("id").item(0);
			    if (portIdElement == null) {
				System.out
				    .println("There is no id element in port range in vehicle configuration file");
				return jsonError("vehicle configuration: no ports/port/id element");
			    }
			    String portIdStr = portIdElement.getTextContent();

			    // should check that it's an integer
			    int portId = Integer.parseInt(portIdStr);

			    if (portId2EcuId.containsKey(portId)) {
				return jsonError("vehicle configuration: port " + portIdStr + "declared several times");
			    }
			    portId2EcuId.put(portId, ecuId);

			    System.out.println("      <id>" + portIdStr
					       + "</id>");
			    System.out.println("    </port>");

			    String q5 = "insert into Port (portId,ecu_id) values (" + portId + "," + ecu_id + ")";
			    rows = mysql.update(q5);
			}

			System.out.println("  </ports>");
		    }
		}
	    }
	    System.out.println("</ecu>");

	    // links
	    NodeList links = root.getElementsByTagName("links");

	    for (int i = 0; i < links.getLength(); i++) {
		// link
		Element linkElement = (Element) links.item(i);

		// type
		Element typeElement = (Element) linkElement
		    .getElementsByTagName("type").item(0);
		if (typeElement == null) {
		    System.out
			.println("There is no type element in link range in vehicle configuration file");
		    return jsonError("vehicle configuration: no link/type element");
		}
		String typeStr = typeElement.getTextContent();
		int type = Integer.parseInt(typeStr);

		// fromPort
		Element fromPortElement = (Element) linkElement
		    .getElementsByTagName("from").item(0);
		if (fromPortElement == null) {
		    System.out
			.println("There is no from element in link range in vehicle configuration file");
		    return jsonError("vehicle configuration: no link/from element");
		}
		String fromPortStr = fromPortElement.getTextContent();
		int fromPortId = Integer.parseInt(fromPortStr);

		//				Integer fromEcuId;
		//				if (fromPortId >= 100)
		//					fromEcuId = -1;
		//				else
		//					fromEcuId = portId2EcuId.get(fromPortId);
		System.out.println("table " + portId2EcuId);
		int fromEcuId;
		try {
		    fromEcuId = portId2EcuId.get(fromPortId);
		} catch (NullPointerException e) {
		    System.out.println(portId2EcuId + " is not in the table");
		    return jsonError("vehicle configuration: unlisted port " + fromPortId);
		    //throw e;
		}
				
		// toPort
		Element toPortElement = (Element) linkElement
		    .getElementsByTagName("to").item(0);
		if (toPortElement == null) {
		    System.out
			.println("There is no to element in link range vehicle configuration file");
		    return jsonError("vehicle configuration: no link/to element");
		}
		String toPortStr = toPortElement.getTextContent();
		int toPortId = Integer.parseInt(toPortStr);

		//				Integer toEcuId;
		//				if (toPortId >= 100)
		//					toEcuId = -1;
		//				else
		//					toEcuId = portId2EcuId.get(toPortId);
		int toEcuId = portId2EcuId.get(toPortId);
				
		String q7 = "insert into Link (fromEcuId,toEcuId,"
		    + "fromPortId,toPortId,type,vehicleConfig_id)"
		    + " values (" + fromEcuId +
		    "," + toEcuId +
		    "," + fromPortId +
		    "," + toPortId +
		    "," + type +
		    "," + c3 + ")";
		rows = mysql.update(q7);

		System.out.println("Done saving config!!!!!!");
	    }

	} catch (ParserConfigurationException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("vehicle configuration: ParserConfigurationException");
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("vehicle configuration: FileNotFoundException");
	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("vehicle configuration: SAXException");
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    return jsonError("vehicle configuration: I/O exception");
	}

	return jsonOK();

    }

    @WebMethod
	public String compileApp(String appname, String version)
	throws PluginWebServicesException {

	String q1 = "select id from Application where name = '" + appname + "'"
	    + "and state < '060'";
	String c1 = mysql.getOne(q1);

	if (c1.equals("error")) {
	    return jsonError("compileApp: internal db error 1");
	}

	if (c1.equals("none")) {
	    return jsonError("compileApp: no such app " + appname);
	}

	String q2 = "select id from Application where name = '" + appname +
	    "' and version = '" + version + "'" + "and state < '060'";
;
	String c2 = mysql.getOne(q2);

	if (c2.equals("error")) {
	    return jsonError("compileApp: internal db error 2");
	}

	if (c2.equals("none")) {
	    return jsonError("compileApp: app " + appname + " has no version [" + version + "]");
	}

	String q3 = "select state from Application where name = '" + appname +
	    "' and version = '" + version + "'" + "and state < '060'";
;
	String c3 = mysql.getOne(q3);

	if (c3.equals("error")) {
	    return jsonError("compileApp: internal db error 3");
	}

	if (c3.equals("none")) {
	    return jsonError("compileApp: internal db error 4");
	}

	if (c3.compareTo("020-uploaded") < 0) {
	    return jsonError("compileApp: application in wrong state: " + c3);
	}

	String zipFile = "/home/arndt/moped/moped/webportal/moped_plugins/" + appname + "/" + version + "/" + appname + ".jar";
	CompressUtils util = new CompressUtils();
	System.out.println("Calling unzip on " + zipFile);
	String dest;
	try {
	    dest = util.unzip(zipFile);
	} catch (MopedException e) {
	    return jsonError(e.getMsg());
	}
		
	System.out.println("Unzipped into: " + dest); 
	dest = dest.substring(0,  dest.length() - 11); //Remove "j2meclasses"
		
	new File(dest + appname + ".suite").delete();

	String reply[] = new String[1];
	boolean s = suiteGen.generateSuite(dest, reply); // + "/" + fullClassName);
	if (s) {
	    String q1u = "update Application set state='030-compiled' where id=" + c2;
	    int rows1u = mysql.update(q1u);
	    if (rows1u != 1)
		return jsonError("compile: internal db error 5");

	    return jsonOK(reply[0]);
	} else {
	    return jsonError(reply[0]);
	}
    }


    @WebMethod
	public String deleteVehicle(String vin)
	throws PluginWebServicesException {

	String cs1 = checkVIN(vin);
	if (cs1 != null)
	    return cs1;

	String q1 = "delete from Vehicle where vin = '" + vin + "'";
	int rows = mysql.update(q1);
	if (rows > 0) {
	    return jsonOK();
	} else {
	    // say why
	    return jsonError("deleteVehicle: couldn't delete " + vin);
	}
    }

    @WebMethod
	public String addVehicle(String name, String vin, String type)
	throws PluginWebServicesException {

	String q1 = "insert into Vehicle (vin,name,vehicleConfig_id) select '" + vin + "','" + name + "',c.id from VehicleConfig c where c.name = '" + type + "'";
	int rows = mysql.update(q1);
	if (rows != 0) {
	    IoSession session = ServerHandler.getSession(vin);
	    if (session != null) {
		String is_sim = MdcInjectionFilter.getProperty(session, "is_sim");
		System.out.println("vehicle " + vin + " simulator: " + is_sim);
		    String q2 = "update Vehicle set simulator=" + is_sim +
			" where vin='" + vin + "'";
		int rows2 = mysql.update(q2);
	    }

	    return jsonOK();
	} else {
	    // distinguish between causes.
	    return jsonError("addVehicle failed");
	}
    }

    @WebMethod
	public String infoVehicle(String vin)
	throws PluginWebServicesException {

	String cs1 = checkVIN(vin);
	if (cs1 != null)
	    return cs1;


	String q1 = "select INSTALLED_APPS,vin,v.name,description,c.name from Vehicle v, VehicleConfig c where vin = '" + vin + "' and c.id = vehicleConfig_id";
	MySqlIterator it = mysql.getIterator(q1);

	JSONObject o = new JSONObject();
	JSONObject o1 = new JSONObject();

	if (it == null) {
	    o.put("result", o1);
	    o.put("error", true);
	    return o.toString();
	}

	// should only be one row
	it.next();
	o1.put("apps", it.getString(1));
	o1.put("vin", it.getString(2));
	o1.put("name", it.getString(3));
	o1.put("description", it.getString(4));
	o1.put("type", it.getString(5));

	o.put("result", o1);
	o.put("error", false);

	return o.toString();
    }

    @WebMethod
	public String listVehicles()
	throws PluginWebServicesException {
	String q1 = "select VIN from Vehicle";
	ArrayList<String> a = new ArrayList<String>();

	MySqlIterator it = mysql.getIterator(q1);

	JSONObject o = new JSONObject();
	JSONArray o1 = new JSONArray();

	if (it == null) {
	    o.put("result", o1);
	    o.put("error", true);
	    return o.toString();
	}

	while (it.next()) {
	    JSONObject o2 = new JSONObject();
	    o2.put("vin", it.getString(1));
	    o1.put(o2);
	}
	o.put("result", o1);
	o.put("error", false);

	return o.toString();
    }

    @WebMethod
	public String listUserVehicles(int user_id)
	throws PluginWebServicesException {

	String cs1 = checkUser(user_id);
	if (cs1 != null)
	    return cs1;

	String q2 = "select v.vin,v.name from Vehicle v, UserVehicleAssociation a where a.vehicle_id = v.id and a.user_ID = " + user_id;
	ArrayList<String> a = new ArrayList<String>();

	MySqlIterator it = mysql.getIterator(q2);

	JSONObject o = new JSONObject();
	JSONArray o1 = new JSONArray();

	if (it == null) {
	    o.put("result", o1);
	    o.put("error", true);
	    return o.toString();
	}

	while (it.next()) {
	    JSONObject o2 = new JSONObject();
	    o2.put("vin", it.getString(1));
	    o2.put("name", it.getString(2));
	    o1.put(o2);
	}
	o.put("result", o1);
	o.put("error", false);

	return o.toString();

    }

    @WebMethod
	public String listVehicleConfigs()
	throws PluginWebServicesException {
	String q1 = "select name,brand from VehicleConfig";

	MySqlIterator it = mysql.getIterator(q1);

	JSONObject o = new JSONObject();
	JSONArray o1 = new JSONArray();

	if (it == null) {
	    o.put("result", o1);
	    o.put("error", true);
	    return o.toString();
	}

	while (it.next()) {
	    JSONObject o2 = new JSONObject();
	    o2.put("name", it.getString(1));
	    o2.put("brand", it.getString(2));
	    o1.put(o2);
	}
	o.put("result", o1);
	o.put("error", false);

	return o.toString();
    }

    private int cmpVersions(String s1, String s2) {
	String [] a1 = s1.split("\\.");
	String [] a2 = s2.split("\\.");

	int i = 0;
	int n1 = a1.length;
	int n2 = a2.length;
	int n;

	if (n1 < n2)
	    n = n1;
	else
	    n = n2;

	while (i < n) {
	    String p1 = a1[i];
	    String p2 = a2[i];

	    int cmp;

	    try {
		int i1 = Integer.parseInt(p1);
		int i2 = Integer.parseInt(p2);
		if (i1 < i2)
		    cmp = -1;
		else if (i1 > i2)
		    cmp = 1;
		else
		    cmp = 0;
	    } catch (NumberFormatException e) {
		cmp = p1.compareTo(p2);
	    }

	    if (cmp < 0)
		return -1;
	    if (cmp > 0)
		return 1;
	    i++;
	}
	if (n1 < n2)
	    return -1;
	else if (n1 > n2)
	    return 1;
	else
	    return 0;
    }

    @WebMethod
	public String listApplications()
	throws PluginWebServicesException {
	String q1 = "select a.id,name,publisher,version,state,c.vehicleConfigName from Application a, AppConfig c where c.application_id=a.id and state < '060' ORDER BY name";

	MySqlIterator it = mysql.getIterator(q1);

	JSONObject o = new JSONObject();
	JSONArray o1 = new JSONArray();

	if (it == null) {
	    o.put("result", o1);
	    o.put("error", true);
	    return o.toString();
	}

	int n = 0;
	while (it.next()) {
	    JSONObject o2 = new JSONObject();
	    o2.put("id", it.getString(1));
	    o2.put("name", it.getString(2));
	    o2.put("publisher", it.getString(3));
	    o2.put("version", it.getString(4));
	    o2.put("state", it.getString(5));
	    o2.put("vehicleConfig", it.getString(6));

	    int cmp;
	    String n1 = (String) o2.get("name");
	    String v1 = (String) o2.get("version");
	    int k = -1;
	    for (int i = 0; i < n; i++) {
		JSONObject o3 = (JSONObject) o1.get(i);
		String n2 = (String) o3.get("name");
		cmp = n1.compareTo(n2);
		if (cmp < 0) {
		    k = i;
		    break;
		}
		if (cmp == 0) {
		    String v2 = (String) o3.get("version");
		    //cmp = v1.compareTo(v2);
		    cmp = cmpVersions(v1, v2);
		    if (cmp < 0) {
			k = i;
			break;
		    }
		}
	    }

	    if (k == -1)
		k = n;

	    for (int i = n-1; i >= k; i--) {
		o1.put(i+1, o1.get(i));
	    }
	    o1.put(k, o2);
	    n++;
	}

	o.put("result", o1);
	o.put("error", false);

	return o.toString();
    }

    @WebMethod
	public String listUserVehicleAssociations(int user_id)
	throws PluginWebServicesException {

	String cs1 = checkUser(user_id);
	if (cs1 != null)
	    return cs1;

	String q2 = "select v.vin,a.activeVehicle from"
	    + " UserVehicleAssociation a,Vehicle v where"
	    + " a.user_ID = " + user_id
	    + " and a.vehicle_id = v.id";

	MySqlIterator it = mysql.getIterator(q2);

	JSONObject o = new JSONObject();
	JSONArray o1 = new JSONArray();

	if (it == null) {
	    o.put("result", o1);
	    o.put("error", true);
	    return o.toString();
	}

	while (it.next()) {
	    JSONObject o2 = new JSONObject();
	    o2.put("vin", it.getString(1));
	    o2.put("active", it.getString(2).equals("1"));
	    o1.put(o2);
	}
	o.put("result", o1);
	o.put("error", false);

	return o.toString();
    }

    @WebMethod
	public String listInstalledApps()
	throws PluginWebServicesException {
	String q1 = "select application_id,ecuId,a.name,vin,a.state,v.state,a.version from VehiclePlugin v, Application a where a.id = v.application_id";

	MySqlIterator it = mysql.getIterator(q1);

	JSONObject o = new JSONObject();
	JSONArray o1 = new JSONArray();

	if (it == null) {
	    o.put("result", o1);
	    o.put("error", true);
	    return o.toString();
	}

	while (it.next()) {
	    JSONObject o2 = new JSONObject();
	    o2.put("appId", it.getString(1));
	    o2.put("ecu", it.getString(2));
	    o2.put("name", it.getString(3));
	    o2.put("vin", it.getString(4));
	    o2.put("applicationState", it.getString(5));
	    o2.put("installationState", it.getString(6));
	    o2.put("version", it.getString(7));
	    o1.put(o2);
	}
	o.put("result", o1);
	o.put("error", false);

	return o.toString();
    }

    @WebMethod
	public String addUserVehicleAssociation(int user_id, String vin,
						boolean activeVehicle)
	throws PluginWebServicesException {

	String cs1 = checkUser(user_id);
	if (cs1 != null)
	    return cs1;

	String cs2 = checkVIN(vin);
	if (cs2 != null)
	    return cs2;

	String q1 = "insert into UserVehicleAssociation"
	    + " (user_ID,vehicle_id,activeVehicle) select "
	    + user_id + ",v.id," + activeVehicle
	    + " from Vehicle v where v.vin = '" + vin + "'";
	int rows = mysql.update(q1);
	if (rows == 0) {
	    // say more
	    return jsonError("addUserVehicleAssociation failed");
	} else {
	    return jsonOK();
	}
    }

    @WebMethod
	public String setUserVehicleAssociationActive
	(int user_id, String vin, boolean active)
	throws PluginWebServicesException {
	int a;
	if (active)
	    a = 1;
	else
	    a = 0;

	String cs1 = checkUser(user_id);
	if (cs1 != null)
	    return cs1;

	String cs2 = checkVIN(vin);
	if (cs2 != null)
	    return cs2;


	// we should fetch v.id in a separate operation
	String q1 = "update UserVehicleAssociation a, Vehicle v"
	    + " set a.activeVehicle = " + a + " where a.user_ID = "
	    + user_id + " and a.vehicle_id=v.id and v.vin='" + vin + "'";
	int rows = mysql.update(q1);
	if (rows == 0) {
	    // say more
	    return jsonError("setUserVehicleAssociationActive failed");
	} else {
	    return jsonOK();
	}
    }

    @WebMethod
	public String deleteUserVehicleAssociation(int user_id, String vin)
	throws PluginWebServicesException {

	String cs1 = checkUser(user_id);
	if (cs1 != null)
	    return cs1;

	String cs2 = checkVIN(vin);
	if (cs2 != null)
	    return cs2;


	String q1 =
	    "select id from Vehicle where"
	    + " vin = '" + vin + "'";
	String c1 = mysql.getOne(q1);

	if (c1.equals("none"))
	    return jsonError("deleteUserVehicleAssociation: no such car " + vin);

	// check user too

	String q2 = "delete from UserVehicleAssociation where"
	    + " user_ID = " + user_id
	    + " and vehicle_id = " + c1;
	int rows = mysql.update(q2);
	if (rows == 0) {
	    // say more
	    return jsonError("deleteUserVehicleAssociation failed");
	} else {
	    return jsonOK();
	}
    }

    @WebMethod
	public String jsontest()
	throws PluginWebServicesException {

	JSONObject o = new JSONObject();
	JSONArray o1 = new JSONArray();
	JSONObject o2 = new JSONObject();
	o2.put("a", o1);
	o.put("error", false);
	o.put("result", o1);
	return o.toString();
    }

    @WebMethod
	public String [] [] stringtest()
	throws PluginWebServicesException {
	String ss [] [] = new String [2] [];
	ss[0] = new String [2];
	ss[1] = new String [2];
	ss[0][0] = "hej";
	ss[0][1] = "hopp";
	ss[1][0] = "tjo";
	ss[1][1] = "ping";
	return ss;
    }

    @WebMethod
	public boolean checkpassword(String pwd, String hash)
	throws PluginWebServicesException {

	MessageContext msgctx = ctx.getMessageContext();
	Map headers = (Map) msgctx.get(MessageContext.HTTP_REQUEST_HEADERS);
	System.out.println(msgctx);
	System.out.println(headers);
	List<String> users = (List<String>) headers.get("Login");
	System.out.println(users.size());
	System.out.println(users.get(0));

	PHPass p = new PHPass(8);
	return p.CheckPassword(pwd, hash);
    }

}
