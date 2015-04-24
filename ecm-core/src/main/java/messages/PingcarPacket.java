package messages;

public class PingcarPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
    public int type;
    public int val;
    public String msg;

	public PingcarPacket() {}
	
	public PingcarPacket(String vin, int val) {
	    // what does this do?
	    super(MessageType.PINGCAR, vin);
	    this.val = val;
	}

	/**
	 * Gets the install message data list.
	 *
	 * @return the install message data list
	 */
	public int getVal() {
		return val;
	}

	/**
	 * Sets the install message data list.
	 *
	 * @param installMessageDataList the new install message data list
	 */
    public void setVal(int val) {
		this.val = val;
	}
}
