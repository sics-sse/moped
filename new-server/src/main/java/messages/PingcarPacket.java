package messages;

public class PingcarPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
    public int type;
    public int val;
    public String msg;

	public PingcarPacket() {
	    type = 0;
	    val = 0;
	    msg = "unknown";
	}
	
	public PingcarPacket(String vin, int val) {
	    // what does this do?
	    super(MessageType.PINGCAR, vin);
	    this.val = val;
	    type = 0;
	    msg = "unknown";
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
