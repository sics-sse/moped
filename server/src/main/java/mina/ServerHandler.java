package mina;

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
import dao.DatabasePluginDao;
import dao.VehicleDao;
import dao.VehiclePluginDao;

public class ServerHandler extends IoHandlerAdapter {
	private VehiclePluginDao vehiclePluginDao;
	private VehicleDao vehicleDao;
	
	// session sets
	private final Set<IoSession> sessions = Collections  
            .synchronizedSet(new HashSet<IoSession>());  
    // vehicle sets
    private final static Map<String, IoSession> vehicles = Collections.synchronizedMap(new HashMap<String, IoSession>());
	
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

	@Override
	public void sessionClosed(IoSession session) {
		String vin = (String) session.getAttribute("vehicle");
		vehicles.remove(vin);
		sessions.remove(session);
		session.close(false);
		System.out.println("Vehicle " + vin + " leaves the connection");
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
	}
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
	}

	@Override
	public void messageReceived(IoSession session, Object packageMessage)
			throws Exception {
		if (packageMessage instanceof Packet) {
			Packet p = (Packet) packageMessage;
			String vin;
			switch (p.getMessageType()) {
			case MessageType.INIT:
				InitPacket initPackageMessage = (InitPacket) packageMessage;
				vin = initPackageMessage.getVin();
				sessions.add(session);
				session.setAttribute("vehicle", vin);
				MdcInjectionFilter.setProperty(session, "vehicle", vin);
				
				vehicles.put(vin, session);
				break;
			case MessageType.INSTALL_LINUX_ACK:
				InstallLinuxAckPacket installLinuxAckPacket = (InstallLinuxAckPacket) packageMessage;
				String linux_ack_vin = installLinuxAckPacket.getVin();
				String linux_ack_pluginName = installLinuxAckPacket.getPluginName();
				System.out.println("[VIN = "+linux_ack_vin+"]" + linux_ack_pluginName + " arrived in the Linux");
				break;
			case MessageType.INSTALL_ACK:
				InstallAckPacket installAckPacket = (InstallAckPacket) packageMessage;
				vin = installAckPacket.getVin();
				int installAppId = installAckPacket.getAppId();
				String pluginName = installAckPacket.getPluginName();
				
				System.out.println("[VIN = "+vin+"]" + pluginName + " arrived in the Autosar");
				
				System.out.println("AppId:"+installAppId);
				VehiclePluginRecord vehiclePluginRecord = Cache.getCache().getVehiclePluginRecord(vin, installAppId, pluginName);
				System.out.println("EcuId:"+vehiclePluginRecord.getEcuId());
				System.out.println("PluginName:"+vehiclePluginRecord.getPluginName());
				if(vehiclePluginRecord == null) {
					System.out.println("[ Fail to fetch vehicle plugin record!!!]");
				} else {
					System.out.println("111");
					vehiclePluginDao.saveVehiclePlugin(vin, installAppId, vehiclePluginRecord);
					System.out.println("222");
					boolean isInstalled = Cache.getCache().IsAllPluginInstalled(vin, installAppId);
					if(isInstalled) {
						System.out.println("333");
						vehicleDao.addApp(vin, installAppId);
					}
				}
				
				
				break;
			case MessageType.UNINSTALL_ACK:
				UninstallAckPacket uninstallAckPackage = (UninstallAckPacket) packageMessage;
				vin = uninstallAckPackage.getVin();
				String vehiclePluginNameForUninstallAck = uninstallAckPackage
						.getPluginName();
				
				int uninstallAppId = vehiclePluginDao.getApplicationId(vin, vehiclePluginNameForUninstallAck);
				
				vehiclePluginDao.removeVehiclePlugin(vin,
						vehiclePluginNameForUninstallAck);
				
				boolean isDelete = Cache.getCache().updateUninstallCacheAndCheckIfRemovable(vin, uninstallAppId, vehiclePluginNameForUninstallAck);
				if(isDelete) {
					vehicleDao.removeOneAppId(vin, uninstallAppId);
				}
				
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
	}
	
	public static IoSession getSession(String vin) {
		return vehicles.get(vin);
	}
}
