package sics.link;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.squawk.VM;

import sics.configs.Configuration;
import sics.messages.LinkContextEntry;
import sics.port.PluginPPort;
import sics.port.PluginPort;
import sics.port.PluginRPort;
import sics.port.Port;
import sics.port.VirtualPPort;
import sics.port.VirtualPort;
import sics.port.VirtualRPort;
import sics.PIRTE;

public class Linker {

	private PIRTE pirte;
	// key: PluginPPortId Integer, value PluginRPortId
	private Hashtable<Integer, Integer> pport2pport = new Hashtable<Integer, Integer>(); 
	//TODO: Move port list from Hashtable to a List
	// key: PluginPPortID Integer, value: VirtualRPortId
	private Hashtable<Integer, Integer> pportId2vrportId= new Hashtable<Integer, Integer>();
	// key: PluginPPortID Ínteger, value: PluginRPortId
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
	
    private void showtables() {
	    VM.println("pport2pport: " + pport2pport);
	    VM.println("pportId2vrportId: " + pportId2vrportId);
	    VM.println("pportId2rportId: " + pportId2rportId);
	    VM.println("rportId2vpportId: " + rportId2vpportId);
    }

	public int getVirtualRPortId(int pportId) {
	    //showtables();
	    //VM.println("getVirtualRPortId " + pportId);

	    Enumeration<Integer> keys = pportId2vrportId.keys();
	    while(keys.hasMoreElements()) {
		Integer from = keys.nextElement();
		Integer to = pportId2vrportId.get(from);
		//VM.println(" link " + from + " -> " + to);
		if (from.intValue() == pportId)
		    return to.intValue();
	    }


	    Integer p = pportId2vrportId.get(new Integer(pportId));
	    //VM.println("getVirtualRPortId -> " + p);
	    //VM.println("getVirtualRPortId -> val " + p.intValue());
	    return p.intValue();
	}
	
	public int getVirtualPPortId(int rportId) {
	    //showtables();
	    //VM.println("getVirtualPPortId " + rportId);
		Integer vpportId = rportId2vpportId.get(rportId);
		if (vpportId == null)
			return -100; //TEMP
		return vpportId.intValue();
	}
	
	public int getPluginRPortId(int pportId) {
	    //showtables();
	    //VM.println("getPluginRPortId " + pportId);
		//TODO: What is this while-loop for???
		Enumeration<Integer> keys = pportId2rportId.keys();
		while(keys.hasMoreElements()) {
			Integer from = keys.nextElement();
			Integer to = pportId2rportId.get(from);

			//VM.println(" link " + from + " -> " + to);
			if (from.intValue() == pportId)
			    return to.intValue();
		}
		
		Integer p = pportId2rportId.get(pportId);
		//VM.println("getPluginRPortId -> " + p);
		//VM.println("getPluginRPortId -> val " + p.intValue());
		return p;
	}

	public void link(LinkContextEntry entry) {
		int fromPortId = entry.getFromPortId();
		int toPortId = entry.getToPortId();
		int remotePortId = entry.getRemotePortId();
		if (remotePortId == Configuration.PPORT2VPORT) {
			// from PluginPort to VirtualPort
			VM.println("Linking " + fromPortId + " -> " + toPortId + " as P->V");
			pportId2vrportId.put(fromPortId, toPortId);
		} else if (remotePortId == Configuration.PPORT2PPORT) {
			// from PluginPort to PluginPort and they are in the same ECU
			VM.println("Linking " + fromPortId + " -> " + toPortId + " as P->P on the same ECU");
			pportId2rportId.put(fromPortId, toPortId);
			// Arndt "same ECU" is not true - this is also for
			// SCU->VCU, which the last clause here seems to be
			// made for, but remotePortId is never positive.
			// 11 here could be 10, and depends on the direction
			// SCU->VCU or the other way
			// (so what about actually same ECU? does that work at
			// all?)
			pportId2vrportId.put(fromPortId, 11);

		} else if (remotePortId == Configuration.VPORT2PPORT) {
			// from VirtualPPort to PluginRPort
			VM.println("Linking " + toPortId + " -> " + fromPortId + " as V->P");
			rportId2vpportId.put(toPortId, fromPortId);
		} else if (remotePortId > 0 ) {
			// from PluginPort to PluginPort but they are in different ECUs
			// In this case, remotePortId is just remote plug-in port ID 
			pportId2rportId.put(fromPortId, remotePortId);
			// PV entry is also needed to register
			pportId2vrportId.put(fromPortId, toPortId);
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
