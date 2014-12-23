package messages;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallMessage.
 */
public class LoadMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int remoteEcuId;
	
	private String executablePluginName;

	private int callbackPortID;

	/** The context. */
	// key: portName(String), value: portId(Integer)
	private HashMap<String, Integer> portInitialContext;

	private ArrayList<LinkContextEntry> linkContext;
	
	private byte[] binaryFile;

	public LoadMessage(int remoteEcuId, String executablePluginName, int callbackPortID,
			HashMap<String, Integer> portInitialContext, ArrayList<LinkContextEntry> linkContext, byte[] binaryFile) {
		super(MessageType.LOAD);
		this.remoteEcuId = remoteEcuId;
		this.executablePluginName = executablePluginName;
		this.callbackPortID = callbackPortID;
		this.portInitialContext = portInitialContext;
		this.linkContext = linkContext;
		this.binaryFile = binaryFile;
	}

	public int getRemoteEcuId() {
		return remoteEcuId;
	}

	public void setRemoteEcuId(int remoteEcuId) {
		this.remoteEcuId = remoteEcuId;
	}

	public int getCallbackPortID() {
		return callbackPortID;
	}

	public void setCallbackPortID(int callbackPortID) {
		this.callbackPortID = callbackPortID;
	}

	public HashMap<String, Integer> getPortInitialContext() {
		return portInitialContext;
	}

	public void setPortInitialContext(
			HashMap<String, Integer> portInitialContext) {
		this.portInitialContext = portInitialContext;
	}

	public ArrayList<LinkContextEntry> getLinkContext() {
		return linkContext;
	}

	public void setLinkContext(ArrayList<LinkContextEntry> linkContext) {
		this.linkContext = linkContext;
	}
	
	public String getExecutablePluginName() {
		return executablePluginName;
	}

	public void setExecutablePluginName(String executablePluginName) {
		this.executablePluginName = executablePluginName;
	}

	public byte[] getBinaryFile() {
		return binaryFile;
	}

	public void setBinaryFile(byte[] binaryFile) {
		this.binaryFile = binaryFile;
	}

}
