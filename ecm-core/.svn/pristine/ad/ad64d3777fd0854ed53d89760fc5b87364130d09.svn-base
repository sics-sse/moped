package messages;

import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallMessage.
 */
public class RestoreMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private int remoteEcuId;

	private String pluginName;

	private int callbackPortID;

	/** The context. */
	// key: portName(String), value: portId(Integer)
	private HashMap<String, Integer> portInitialContext;

	private byte[] binaryFile;

	public RestoreMessage(int remoteEcuId, String pluginName,
			int callbackPortID, HashMap<String, Integer> portInitialContext,
			byte[] binaryFile) {
		super(MessageType.RESTORE);
		this.remoteEcuId = remoteEcuId;
		this.pluginName = pluginName;
		this.callbackPortID = callbackPortID;
		this.portInitialContext = portInitialContext;
		this.binaryFile = binaryFile;
	}

	public int getRemoteEcuId() {
		return remoteEcuId;
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

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	public byte[] getBinaryFile() {
		return binaryFile;
	}

	public void setBinaryFile(byte[] binaryFile) {
		this.binaryFile = binaryFile;
	}

}
