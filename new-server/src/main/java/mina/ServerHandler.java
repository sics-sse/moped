package mina;

import service.CallMySql;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.net.InetSocketAddress;
import java.net.InetAddress;

import messages.PingcarPacket;
import messages.InitPacket;
import messages.Init2Packet;
import messages.InstallAckPacket;
import messages.InstallLinuxAckPacket;
import messages.MessageType;
import messages.Packet;
import messages.RestoreAckPacket;
import messages.UninstallAckPacket;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.logging.MdcInjectionFilter;

import cache.Cache;
import cache.VehiclePluginRecord;

public class ServerHandler extends IoHandlerAdapter {
	
    // session sets
    private final Set<IoSession> sessions = Collections  
	.synchronizedSet(new HashSet<IoSession>());  
    // vehicle sets
    private final static Map<String, IoSession> vehicles = Collections.
	synchronizedMap(new HashMap<String, IoSession>());
    
    private ArrayList<String> ackMessages = new ArrayList<String>();
	
    private CallMySql mysql = new CallMySql();

    public void sessionClosed(IoSession session) {
	String vin = (String) session.getAttribute("vehicle");
	vehicles.remove(vin);
	sessions.remove(session);
	session.close(false);
	System.out.println("Vehicle " + vin + " leaves the connection");
    }

    public void messageSent(IoSession session, Object message) throws Exception {
	System.out.println("Message sent from server... " + message);
    }
	
    public void exceptionCaughtXXX(IoSession session, Throwable cause) throws Exception {
	System.out.println("IoSession exception... " + cause);
    }

    public void messageReceived(IoSession session, Object packageMessage)
	throws Exception {
	
	String is_sim;
	System.out.println("Message received on server...");
		
	InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
	InetAddress inetAddress = socketAddress.getAddress();

	if (packageMessage instanceof Packet) {
	    Packet p = (Packet) packageMessage;
	    String vin, pluginName;
	    switch (p.getMessageType()) {
	    case MessageType.INIT:
		InitPacket initPackageMessage = (InitPacket) packageMessage;
		vin = initPackageMessage.getVin();

		    sessions.add(session);
		    session.setAttribute("vehicle", vin);
		    MdcInjectionFilter.setProperty(session, "vehicle", vin);
				
		    is_sim = "0";
		    MdcInjectionFilter.setProperty(session, "is_sim", is_sim);

		    vehicles.put(vin, session);
		    System.out.println("Vehicle " + vin + " joins the connection (simulator " + is_sim + ")");

		    System.out.println("from " + inetAddress.getHostAddress());

		    String q11 = "update Vehicle set simulator=" + is_sim +
			" where vin = '" + vin + "'";
		    int rows2 = mysql.update(q11);
		    if (rows2 == 0) {
			System.out.println("unknown car");
		    }
		break;
	    case MessageType.INIT2:
		Init2Packet init2PackageMessage = (Init2Packet) packageMessage;
		vin = init2PackageMessage.getVin();

		// This wasn't a good idea: it may well happen that a car
		// is restarted but the server doesn't notice it
		if (false && vehicles.containsKey(vin)) {
		    System.out.println("Vehicle " + vin + " tries to connect again - telling it to go away");

		    PingcarPacket pingcarPacket = new PingcarPacket(vin, 0);
		    pingcarPacket.type = 44;
		    pingcarPacket.msg = "duplicate VIN";
		    session.write(pingcarPacket);

		    // Just leave it hanging now. If we close it, the car
		    // will retry (when using my unchecked-in changes)
		    //session.close(false);
		} else {

		    sessions.add(session);
		    session.setAttribute("vehicle", vin);
		    MdcInjectionFilter.setProperty(session, "vehicle", vin);
				
		    if (init2PackageMessage.is_simulator)
			is_sim = "1";
		    else
			is_sim = "0";
		    MdcInjectionFilter.setProperty(session, "is_sim", is_sim);

		    vehicles.put(vin, session);
		    System.out.println("Vehicle " + vin + " joins the connection (simulator " + is_sim + ")");

		    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    Date date = new Date();

		    System.out.println("from " + inetAddress.getHostAddress() + " at " + dateFormat.format(date));

		    String q1 = "update Vehicle set simulator=" + is_sim +
			" where vin = '" + vin + "'";
		    int rows = mysql.update(q1);
		    if (rows == 0) {
			System.out.println("unknown car");
		    }
		}
		break;
	    case MessageType.INSTALL_LINUX_ACK:
		InstallLinuxAckPacket installLinuxAckPacket =
		    (InstallLinuxAckPacket) packageMessage;
		vin = installLinuxAckPacket.getVin();
		pluginName = installLinuxAckPacket.getPluginName();
		System.out.println("[VIN = "+vin+"]" + pluginName +
				   " arrived in the Linux");
		ackMessages.add(vin + "_" + 
				pluginName.substring
				(0, pluginName.lastIndexOf(".")));

		break;
	    case MessageType.INSTALL_ACK:
		InstallAckPacket installAckPacket =
		    (InstallAckPacket) packageMessage;
		vin = installAckPacket.getVin();
		int installAppId = installAckPacket.getAppId();
		pluginName = installAckPacket.getPluginName();
				
		System.out.println("[VIN = "+vin+"]" + pluginName +
				   " arrived in the Autosar");
				
		System.out.println("AppId:"+installAppId);
		VehiclePluginRecord vehiclePluginRecord =
		    Cache.getCache().getVehiclePluginRecord
		    (vin, installAppId, pluginName);
		if (vehiclePluginRecord == null) {
		    System.out.println
			("INSTALL_ACK: couldn't find VehiclePlugin for " +
			 pluginName + " in cache!");
		    // or return;
		    break;
		}
		System.out.println("EcuId:"+vehiclePluginRecord.getEcuId());
		System.out.println
		    ("PluginName:"+vehiclePluginRecord.getPluginName());
		if(vehiclePluginRecord == null) {
		    System.out.println
			("[ Fail to fetch vehicle plugin record!!!]");
		} else {
		    String q0 = "delete from VehiclePlugin where vin = '" +
			vin + "' and application_id = " + installAppId;
		    int rows0 = mysql.update(q0);
		    System.out.println("rows0 = " + rows0);

		    String q1 = "insert into VehiclePlugin (vin,name,application_id,ecuId,sendingPortId,callbackPortId,location,state,executablePluginName) values ('" +
			vin + "','" +
			pluginName + "'," +
			installAppId + "," +
			vehiclePluginRecord.getEcuId() + "," +
			vehiclePluginRecord.getSendingPortId() + "," +
			vehiclePluginRecord.getCallbackPortId() + ",'" +
			vehiclePluginRecord.getLocation() + "','" +
			"installed" + "','" +
			vehiclePluginRecord.getExecutablePluginName() + "')";
		    int rows = mysql.update(q1);

		    boolean isInstalled
			= Cache.getCache().IsAllPluginInstalled
			(vin, installAppId);
		    if(isInstalled) {
			String q2 = "select INSTALLED_APPS from Vehicle where vin = '" + vin + "'";
			String c2 = mysql.getOne(q2);

			// check that appId is not
			// already present

			System.out.println("installed before: " + c2);

			String cidc = "," + installAppId + ",";
			System.out.println("cidc = " + cidc);
			if (c2.contains(cidc)) {
			    System.out.println("already installed");
			    break;
			}
			    
			if (c2.matches("," + installAppId + ",") ||
			    c2.matches("^" + installAppId + "$") ||
			    c2.matches("^" + installAppId + ",") ||
			    c2.matches("," + installAppId + "$")) {
			    System.out.println("already installed");
			    break;
			}

			if(c2 == null || c2.equals("")) {
			    c2 = "" + installAppId;
			} else {
			    c2 += ",";
			    c2 += installAppId;
			}
			String q3 = "update Vehicle set INSTALLED_APPS = '" + c2 + "' where vin = '" + vin + "'";
			int rows3 = mysql.update(q3);
		    } else {
			System.out.println("not all installed");
		    }
		}
				
		break;
	    case MessageType.UNINSTALL_ACK:
		UninstallAckPacket uninstallAckPackage
		    = (UninstallAckPacket) packageMessage;
		vin = uninstallAckPackage.getVin();
		pluginName
		    = uninstallAckPackage.getPluginName();
				
		String q4 = "delete from VehiclePlugin where vin = '" + vin +
		    "' and name = '" + pluginName + "'";
		int rows4 = mysql.update(q4);
		System.out.println("rows4 = " + rows4);

		System.out.println("  " + vin + " " + pluginName);

		//boolean isDelete = Cache.getCache().updateUninstallCacheAndCheckIfRemovable(vin, uninstallAppId, vehiclePluginNameForUninstallAck);
		//if(isDelete) {
		//					vehicleDao.removeOneAppId(vin, uninstallAppId);
		//  System.out.println("  should be deleted");
		//}
				
		break;
	    case MessageType.RESTORE_ACK:
		RestoreAckPacket restoreAckPacket =
		    (RestoreAckPacket) packageMessage;
		vin = restoreAckPacket.getVin();
		String pluginName4Restore = restoreAckPacket.getPluginName();
		System.out.println("Restore " + pluginName4Restore +
				   " in Vehicle VIN ("+vin+") ");
		break;
	    default:
		System.out.println("????");
		break;
	    }
	}
	else if (packageMessage instanceof String) {
	    //TODO: Step 1: use strings instead of packets (which are unnecessarily complicated), using existing TextLineCodecFactory-ProtocolCodecFilter
	    //		Step 2: possiblty implement a codec filter, targeted at our messages
	    System.out.println("This is a string packet!!!");
	    System.out.println("PACKET: " + (String)packageMessage);
			
	    //			InitPacket initPackageMessage = (InitPacket) packageMessage;
	    //			String vin = initPackageMessage.getVin();
	    //			sessions.add(session);
	    //			session.setAttribute("vehicle", vin);
	    //			MdcInjectionFilter.setProperty(session, "vehicle", vin);
	    //			
	    //			vehicles.put(vin, session);
	    //			System.out.println("Vehicle " + vin + " joins the connection");
	}
	else {
	    System.out.println("ERROR:: a non-Packet message was received");
	}
    }
	
    public boolean existsAckMessage(String msg) {
	System.out.println(ackMessages);
	if (ackMessages.contains(msg)) {
	    ackMessages.remove(msg);
	    return true;
	}
		
	return false;
    }
	
    public static IoSession getSession(String vin) {
	return vehicles.get(vin);
    }
}
