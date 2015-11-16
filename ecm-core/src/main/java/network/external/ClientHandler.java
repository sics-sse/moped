package network.external;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import messages.InitPacket;
import messages.Init2Packet;
import messages.PingcarPacket;
import messages.InstallLinuxAckPacket;
import messages.InstallMessage;
import messages.InstallPacket;
import messages.InstallPacketData;
import messages.LinkContextEntry;
import messages.MessageType;
import messages.Packet;
import messages.RestoreMessage;
import messages.RestorePacket;
import messages.UninstallMessage;
import messages.UninstallPacket;
import messages.UninstallPacketData;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import utils.PropertyAPI;
import db.DataRecord;

// TODO: Auto-generated Javadoc
/**
 * The Class ClientHandler.
 */
public class ClientHandler extends IoHandlerAdapter {
	private String APP_DIR;
	
	private byte pluginIdAllocator = 1;
	
	/** The manager. */
	private SocketCommunicationManager manager;

	/** The logger. */
	private Logger logger = Logger.getLogger(ClientHandler.class);

	/**
	 * Instantiates a new client handler.
	 * 
	 * @param manager
	 *            the manager
	 */
	public ClientHandler(SocketCommunicationManager manager) {
		this.manager = manager;
		APP_DIR = PropertyAPI.getInstance().getProperty("APP_DIR");
		
		(new File(APP_DIR)).mkdirs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#sessionOpened(org.apache
	 * .mina.core.session.IoSession)
	 */
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		String vin = manager.getVin();
		
		System.out.println("Opening a session to " + session.getRemoteAddress() + " from vin:" + vin);
		System.out.println("Local session address: " + session.getLocalAddress());
		
		// Send VIN to Server
		//InitPacket initPackage = new InitPacket(vin);
		Init2Packet initPackage = new Init2Packet(vin);
		initPackage.is_simulator = manager.getIs_Simulator();
		session.write(initPackage);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		String vin = manager.getVin();
		System.out.println("Closed session " + session.getRemoteAddress() + " from vin:" + vin);

	}

        @Override
	public void exceptionCaught(IoSession session, Throwable t) throws Exception {
		String vin = manager.getVin();
		System.out.println("Exception " + session.getRemoteAddress() + " from vin:" + vin + " " + t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.mina.core.service.IoHandlerAdapter#mesprivate final static String WSDL_PORT = "9998";sageReceived(org.apache
	 * .mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(IoSession session, Object packet)
			throws Exception {
		System.out.println("Message received on ECM...");
		
		//TODO: TEMP TEST PRINT-OUT
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(packet);
			byte[] testBytes = bos.toByteArray();
			System.out.print("TEST BYTES:");
			for (int i = 0; i < 10; i++) {
				System.out.print(" " + (testBytes[i] & 0xFF));
			}
			System.out.println("");
			System.out.print("... ");
			for (int i = testBytes.length - 10; i < testBytes.length; i++) {
				System.out.print(" " + (testBytes[i] & 0xFF));
			}
			System.out.println("");
		} finally {
		  try {
		    if (out != null) {
		      out.close();
		    }
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		  try {
		    bos.close();
		  } catch (IOException ex) {
		    // ignore close exception
		  }
		}
		
		Packet p = (Packet) packet;
		System.out.println(">>> ecm-core/ClientHandler " + p.getMessageType());
		switch (p.getMessageType()) {
		case MessageType.INSTALL:
			System.out.println("Install Packet arrived");
			InstallPacket installPacket = (InstallPacket) packet;
			unpackInstallPackage(installPacket);
			break;
		case MessageType.UNINSTALL:
			System.out.println("Uninstall Packet arrived");
			UninstallPacket uninstallPacket = (UninstallPacket) packet;
			unpackUninstallPackage(uninstallPacket);
			break;
		case MessageType.RESTORE:
			System.out.println("Restore Packet arrived");
			RestorePacket restorePacket = (RestorePacket) packet;
			unpackRestorePackage(restorePacket);
			break;
		case MessageType.PINGCAR:
		    PingcarPacket pingcarPacket = (PingcarPacket) packet;
		    System.out.println("Ping Packet arrived (" + pingcarPacket.type + " " + pingcarPacket.val + " " + pingcarPacket.msg + ")");
		    if (pingcarPacket.type == 44) {
			System.out.println("told to shut down");
			System.exit(0);
		    }

			break;
		default:
			System.out.println("???");
			System.out.println("Type_" + p.getMessageType() + ": " + p.getVin()); //TODO: TEMP_DBG
			break;
		}
	}

	/**
	 * Unpack install package.
	 * 
	 * @param packageMessage
	 *            the package message
	 */
	private void unpackInstallPackage(InstallPacket packet) {
		System.out.println("In the unpackInstallPackage");
		List<InstallPacketData> installPacketDataList = packet
				.getInstallPacketDataList();
		System.out.println("installPacketDataList.size() = " + installPacketDataList.size());
		for (InstallPacketData installPacketData : installPacketDataList) {
			int sendingPortID = installPacketData.getSendingPortID();
			int callbackPortID = installPacketData.getCallbackPortID();
			int reference = installPacketData.getReference();
			
			// Temporarily register DataRecord in temporary HasMap in ECM until
			// installAck message comes and then permantely register DataRecord
			// to DB
			String pluginName = installPacketData.getPluginName();
			
			// send back about APP arrival in the Linux
			InstallLinuxAckPacket installLinuxAckPacket = new InstallLinuxAckPacket(manager.getVin(), pluginName);
		System.out.println("<<< ecm-core/ClientHandler INSTALL_LINUX_ACK");
			manager.write(installLinuxAckPacket);
			
			int appId = installPacketData.getAppId();
			// "plugin://tests/ClassLoaderInput/ClassLoaderInput.suite";
			String executablePluginName = installPacketData.getExecutablePluginName();
			String location = APP_DIR + File.separator + pluginName;
			HashMap<String, Integer> portInitialContext = installPacketData
					.getPortInitialContext();

			ArrayList<LinkContextEntry> portLinkingContext = installPacketData
					.getPortLinkingContext();
			if(portLinkingContext == null) {
				System.out.println("portLinkingContext is null");
//				System.exit(-1);
			} else {
				System.out.println("portLinkingContext is not null");
				//TEMP_DBG
				for (Iterator<LinkContextEntry> ctxtIter = portLinkingContext.iterator(); ctxtIter.hasNext(); ) {
				    LinkContextEntry ctxt = ctxtIter.next();
				    System.out.println("ctxtIter: " + ctxt.getFromPortId() + " -> " + ctxt.getToPortId() + " via " + ctxt.getRemotePortId());
					
				    if (pluginName.equals(manager.getEcm().subscriberName + ".suite") && ctxt.getFromPortId() == 22) {
					manager.getEcm().subscriberPort = ctxt.getToPortId();
					System.out.println("port " + manager.getEcm().subscriberPort + " is a subscriber");
				    }
				}


			}

			System.out.println("reference = " + reference);
			DataRecord dataRecord = new DataRecord(appId, reference, sendingPortID,
					callbackPortID, pluginName, executablePluginName, location, portInitialContext,
					portLinkingContext);
			manager.getEcm().insertTmpDBRecord(pluginIdAllocator, dataRecord);
			

			byte[] binaryFile = installPacketData.getBinaryFile();
			System.out.println("binaryFile.length = " + binaryFile.length);
			generateFile(binaryFile, location);
			
			System.out.println("Binary file stored at " + location);

			// NOTE: change back value of pluginName somehow after test of integration
			InstallMessage installMessage = new InstallMessage(reference, pluginIdAllocator++, executablePluginName,
					callbackPortID, portInitialContext, portLinkingContext, binaryFile);

		System.out.println("<<< ecm-core/ClientHandler INSTALL");
			manager.getEcm().process(installMessage);

			System.out.println("Package installed");
		}
	}


	/**
	 * Unpack uninstall package.
	 * 
	 * @param packageMessage
	 *            the package message
	 */
	private void unpackUninstallPackage(UninstallPacket packet) {

		List<UninstallPacketData> uninstallPacketDataList = packet
				.getUninstallPacketDataList();
		System.out.println("unpackUninstallPackage");
		for (UninstallPacketData uninstallPacketData : uninstallPacketDataList) {
			String pluginName = uninstallPacketData.getPluginName();
			int callbackPortID = uninstallPacketData.getCallbackPortID();
			int reference = uninstallPacketData.getReference();
			
			manager.getEcm().addPluginIdPluginName2UninstallCache(pluginIdAllocator, pluginName);
			
		System.out.println("unpackUninstallPackage " + reference +
				   " " + callbackPortID +
				   " " + pluginName +
				   " " + pluginIdAllocator);

			UninstallMessage uninstallMessage = new UninstallMessage(reference, pluginIdAllocator++,
					pluginName, callbackPortID);

		System.out.println("<<< ecm-core/ClientHandler UNINSTALL");
			manager.getEcm().process(uninstallMessage);
		}
	}

	/**
	 * Unpack restore package.
	 * 
	 * @param packageMessage
	 *            the package message
	 */
	private void unpackRestorePackage(RestorePacket packet) {
		List<InstallPacketData> installPacketDataList = packet
				.getInstallMessageDataList();
		for (InstallPacketData installPacketData : installPacketDataList) {
			logger.debug("Install Message is building");
			int callbackPortID = installPacketData.getCallbackPortID();
			int reference = installPacketData.getReference();
			String pluginName = installPacketData.getPluginName();
			HashMap<String, Integer> portInitialContext = installPacketData
					.getPortInitialContext();

			String location = System.getProperty("user.dir") + File.separator
					+ "src" + File.separator + "main" + File.separator
					+ "resources" + File.separator + "local" + File.separator
					+ "ECM" + File.separator + pluginName;

			byte[] binaryFile = installPacketData.getBinaryFile();
			generateFile(binaryFile, location);

			RestoreMessage restoreMessage = new RestoreMessage(reference, pluginName,
					callbackPortID, portInitialContext, binaryFile);

		System.out.println("<<< ecm-core/ClientHandler RESTORE");
			manager.getEcm().process(restoreMessage);
		}
	}
	
	// save jar file in the ECM
	private void generateFile(byte[] data, String path) {
		try {
			OutputStream output = null;
			try {
			    System.out.println("generateFile " + path);
			    System.out.println("generateFile " + path + " " + data.length);
				output = new BufferedOutputStream(new FileOutputStream(path));
				output.write(data);
			} finally {
				output.close();
			}
		} catch (FileNotFoundException ex) {
			System.out.println("File not found.");
		} catch (Exception ex) {
			ex.printStackTrace();
		} 
	}

}
