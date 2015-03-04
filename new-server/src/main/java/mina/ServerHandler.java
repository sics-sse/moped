package mina;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import messages.InitPacket;
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
	
	public void sessionClosed(IoSession session) {
		String vin = (String) session.getAttribute("vehicle");
		vehicles.remove(vin);
		sessions.remove(session);
		session.close(false);
		System.out.println("Vehicle " + vin + " leaves the connection");
	}

	public void messageSent(IoSession session, Object message) throws Exception {
		System.out.println("Message sent from server...");
	}
	
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
	}

	public void messageReceived(IoSession session, Object packageMessage)
			throws Exception {
		System.out.println("Message received on server...");
		
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
				
				vehicles.put(vin, session);
				System.out.println("Vehicle " + vin + " joins the connection");
				break;
			case MessageType.INSTALL_LINUX_ACK:
				InstallLinuxAckPacket installLinuxAckPacket = (InstallLinuxAckPacket) packageMessage;
				vin = installLinuxAckPacket.getVin();
				pluginName = installLinuxAckPacket.getPluginName();
				System.out.println("[VIN = "+vin+"]" + pluginName + " arrived in the Linux");
				ackMessages.add(vin + "_" + 
						pluginName.substring(0, pluginName.lastIndexOf(".")));

				break;
			case MessageType.INSTALL_ACK:
				InstallAckPacket installAckPacket = (InstallAckPacket) packageMessage;
				vin = installAckPacket.getVin();
				int installAppId = installAckPacket.getAppId();
				pluginName = installAckPacket.getPluginName();
				
				System.out.println("[VIN = "+vin+"]" + pluginName + " arrived in the Autosar");
				
				System.out.println("AppId:"+installAppId);
				VehiclePluginRecord vehiclePluginRecord = Cache.getCache().getVehiclePluginRecord(vin, installAppId, pluginName);
				System.out.println("EcuId:"+vehiclePluginRecord.getEcuId());
				System.out.println("PluginName:"+vehiclePluginRecord.getPluginName());
				if(vehiclePluginRecord == null) {
					System.out.println("[ Fail to fetch vehicle plugin record!!!]");
				} else {
					System.out.println("111");
					//vehiclePluginDao.saveVehiclePlugin(vin, installAppId, vehiclePluginRecord);
					System.out.println("222");
					boolean isInstalled = Cache.getCache().IsAllPluginInstalled(vin, installAppId);
					if(isInstalled) {
						System.out.println("333");
						//vehicleDao.addApp(vin, installAppId);
					}
				}
				
				break;
			case MessageType.UNINSTALL_ACK:
			    System.out.println("UNINSTALL_ACK not converted");
				UninstallAckPacket uninstallAckPackage = (UninstallAckPacket) packageMessage;
				vin = uninstallAckPackage.getVin();
				String vehiclePluginNameForUninstallAck = uninstallAckPackage
						.getPluginName();
				
				//				int uninstallAppId = vehiclePluginDao.getApplicationId(vin, vehiclePluginNameForUninstallAck);
				//				
				//				vehiclePluginDao.removeVehiclePlugin(vin,
				//						vehiclePluginNameForUninstallAck);
				
				System.out.println("  " + vin + " " + vehiclePluginNameForUninstallAck);

				//boolean isDelete = Cache.getCache().updateUninstallCacheAndCheckIfRemovable(vin, uninstallAppId, vehiclePluginNameForUninstallAck);
				//if(isDelete) {
				    //					vehicleDao.removeOneAppId(vin, uninstallAppId);
				//  System.out.println("  should be deleted");
				//}
				
				break;
			case MessageType.RESTORE_ACK:
				RestoreAckPacket restoreAckPacket = (RestoreAckPacket) packageMessage;
				vin = restoreAckPacket.getVin();
				String pluginName4Restore = restoreAckPacket.getPluginName();
				System.out.println("Restore " + pluginName4Restore + " in Vehicle VIN ("+vin+") ");
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
