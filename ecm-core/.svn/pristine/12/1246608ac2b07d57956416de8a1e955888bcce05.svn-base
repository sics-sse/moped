package messages;

import java.util.ArrayList;
import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallMessage.
 */
public class InstallMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int remoteEcuId;

	private byte pluginId;

	private String executablePluginName;

	private int callbackPortID;

	/** The context. */
	// key: portName(String), value: portId(Integer)
	private HashMap<String, Integer> portInitialContext;

	private ArrayList<LinkContextEntry> linkContext;

	private byte[] binaryFile;

	public InstallMessage(int remoteEcuId, byte pluginId,
			String executablePluginName, int callbackPortID,
			HashMap<String, Integer> portInitialContext,
			ArrayList<LinkContextEntry> linkContext, byte[] binaryFile) {
		super(MessageType.INSTALL);
		this.remoteEcuId = remoteEcuId;
		this.pluginId = pluginId;
		this.executablePluginName = executablePluginName;
		this.callbackPortID = callbackPortID;
		this.portInitialContext = portInitialContext;
		this.linkContext = linkContext;
		this.binaryFile = binaryFile;
	}

	public int getRemoteEcuId() {
		return remoteEcuId;
	}

	public String getExecutablePluginName() {
		return executablePluginName;
	}

	public byte getPluginId() {
		return pluginId;
	}

	public void setExecutablePluginName(String executablePluginName) {
		this.executablePluginName = executablePluginName;
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

	public byte[] getBinaryFile() {
		return binaryFile;
	}

	public void setBinaryFile(byte[] binaryFile) {
		this.binaryFile = binaryFile;
	}

}
