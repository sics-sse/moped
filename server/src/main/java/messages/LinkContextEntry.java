package messages;

import java.io.Serializable;

public class LinkContextEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int fromPortId;
	private int toPortId;
	private int remotePortId;

	public LinkContextEntry() {
	}

	public LinkContextEntry(int fromPortId, int toPortId, int remotePortId) {
		this.fromPortId = fromPortId;
		this.toPortId = toPortId;
		this.remotePortId = remotePortId;
	}

	public int getFromPortId() {
		return fromPortId;
	}

	public void setFromPortId(int fromPortId) {
		this.fromPortId = fromPortId;
	}

	public int getToPortId() {
		return toPortId;
	}

	public void setToPortId(int toPortId) {
		this.toPortId = toPortId;
	}

	public int getRemotePortId() {
		return remotePortId;
	}

	public void setRemotePortId(int remotePortId) {
		this.remotePortId = remotePortId;
	}

}
