package fresta.link;

import java.util.Hashtable;

import sics.port.Port;
import messages.LinkContextEntry;
import fresta.configs.Configuration;
import fresta.pirte.PIRTE;

public class Linker {
	private PIRTE pirte;
	// key: PluginPPortId Integer, value PluginRPortId
	private Hashtable<Integer, Integer> pport2pport = new Hashtable<Integer, Integer>(); 
	//TODO: Move port list from Hashtable to a List
	// key: PluginPPortID Integer, value: VirtualRPortId
	private Hashtable<Integer, Integer> pportId2vrportId= new Hashtable<Integer, Integer>();
	// key: PluginPPortID Integer, value: PluginRPortId
	private Hashtable<Integer, Integer> pportId2rportId = new Hashtable<Integer, Integer>();
	// key: PluginRPortId, value: VirtualPPort Integer
	private Hashtable<Integer, Integer> rportId2vpportId = new Hashtable<Integer, Integer>();
	
	public Linker(PIRTE pirte) {
		this.pirte = pirte;
	}

//	public VirtualPort getVirtualPort(int portId) {
//		VirtualPort port = (VirtualPort) portId2VirtualPort.get(new Integer(
//				portId));
//		return port;
//	}
//
//	public PluginPort getPluginPort(int portId) {
//		return (PluginPort) portId2PluginPort.get(new Integer(portId));
//	}
	
	public int getVirtualRPortId(int pportId) {
	    //	    System.out.println("getVirtualRPortId, pportId2vrportId = "
	    //			       + pportId2vrportId + " " + pportId);
	    //	    System.out.println(" pportId2rportId = " + pportId2rportId);
		return pportId2vrportId.get(pportId);
	}
	
	public int getVirtualPPortId(int rportId) {
	    //	    System.out.println("getVirtualPPortId, rportId2vpportId = "
	    //			       + pportId2vrportId + " " + rportId);
	    //	    System.out.println(" pportId2rportId = " + pportId2rportId);
		return rportId2vpportId.get(rportId);
	}
	
	public int getPluginRPortId(int pportId) {
		return pportId2rportId.get(pportId);
	}

	public void link(LinkContextEntry entry) {
		int fromPortId = entry.getFromPortId();
		int toPortId = entry.getToPortId();
		int remotePortId = entry.getRemotePortId();
		if (remotePortId == Configuration.PPORT2VPORT) {
			// from PluginPort to VirtualPort
			pportId2vrportId.put(fromPortId, toPortId);
		} else if (remotePortId == Configuration.PPORT2PPORT) {
			// from PluginPort to PluginPort and they are in the same ECU
			pportId2rportId.put(fromPortId, toPortId);
		} else if (remotePortId == Configuration.VPORT2PPORT) {
			// from VirtualPPort to PluginRPort
			rportId2vpportId.put(toPortId, fromPortId);
		} else if (remotePortId > 0 ) {
			// from PluginPort to PluginPort but they are in different ECUs
		}
	}
	
//	public void link(LinkContextEntry entry) {
//		int fromPortId = entry.getFromPortId();
//		int toPortId = entry.getToPortId();
//		int remotePortId = entry.getRemotePortId();
//		if (remotePortId == Configuration.PPORT2VPORT) {
//			// from PluginPort to VirtualPort
//			PluginPPort pport = (PluginPPort) portId2PluginPort
//					.get(new Integer(fromPortId));
//			VirtualRPort vport = (VirtualRPort) portId2VirtualPort
//					.get(new Integer(toPortId));
//			System.out.println("PPort: "+fromPortId+", vport:"+toPortId);
//			pport.addConnectedRPort(vport);
//		} else if (remotePortId == Configuration.PPORT2PPORT) {
//			// from PluginPort to PluginPort
//			PluginPPort fromPort = (PluginPPort) portId2PluginPort
//					.get(new Integer(fromPortId));
//			PluginRPort toPort = (PluginRPort) portId2PluginPort
//					.get(new Integer(toPortId));
//			if(toPort == null) {
//				waitingLinks.put(new Integer(toPortId), new Integer(fromPortId));
////				System.out.println("@@@ Error: Fail to gain PluginRPort Id = " + toPortId + ", fromPortID = " + fromPortId);
//				//System.exit(-1);
//			} else
//				fromPort.addConnectedRPort(toPort);
//		} else if (remotePortId == Configuration.VPORT2PPORT) {
//			// from VirtualPort to PluginPort
//			VirtualPPort vport = (VirtualPPort) portId2VirtualPort
//					.get(new Integer(fromPortId));
//			PluginRPort rport = (PluginRPort) portId2PluginPort
//					.get(new Integer(toPortId));
//			//vport.addConnectedRPort(rport); // may be removed in the future
//			if(vport == null) {
//				System.out.println("too bad");
//				System.exit(-1);
//			}
//			else {
//				rport.setVport(vport);
//				
//			}
//		} else if (remotePortId > 0 ) {
//			PluginPPort pport = (PluginPPort) portId2PluginPort
//					.get(new Integer(fromPortId));
//			VirtualRPort vport = (VirtualRPort) portId2VirtualPort
//					.get(new Integer(toPortId));
//			vport.registerP2PContext(fromPortId, remotePortId);
//			pport.addConnectedRPort(vport);
//		}
//	}

	public void registerPort(Port port) {
		/*
		if (port instanceof PluginPPort) {
			PluginPort pluginport = (PluginPort) port;
			int portId = pluginport.getId();
			portId2PluginPort.put(new Integer(portId), port);
//			System.out.println("@@@ register plugin port ID: "+portId);
		}
		else if(port instanceof PluginRPort) {
			PluginPort pluginport = (PluginPort) port;
			int portId = pluginport.getId();
			portId2PluginPort.put(new Integer(portId), port);
//			System.out.println("@@@ register plugin port ID: "+portId);
			
			// check waiting links
			if(waitingLinks.containsKey(new Integer(portId))) {
				PluginRPort toPort = (PluginRPort) portId2PluginPort
						.get(new Integer(portId));
				Integer fromPortId = (Integer) waitingLinks.get(new Integer(portId));
				PluginPPort fromPort = (PluginPPort) portId2PluginPort
						.get(fromPortId);
				
				fromPort.addConnectedRPort(toPort);
				
				// remove the item
				waitingLinks.remove(new Integer(portId));
//				System.out.println("Find Port " + portId + " and make the link with Port "+fromPortId);
			}
		}
		else {
			if (port instanceof VirtualPPort) {
				VirtualPPort vpport = (VirtualPPort) port;
				vpport.setPirte(pirte);
				int portId = vpport.getId();
				portId2VirtualPort.put(new Integer(portId), port);
			} else if (port instanceof VirtualRPort) {
				VirtualRPort rport = (VirtualRPort) port;
				int portId = rport.getId();
				portId2VirtualPort.put(new Integer(portId), port);
			} else {
				System.out.println("Error: wrong type of Port in "
						+ "registerPort in Class " + this.getClass().getName());
			}
		}
		*/
	}

	public PIRTE getPirte() {
		return pirte;
	}
}
