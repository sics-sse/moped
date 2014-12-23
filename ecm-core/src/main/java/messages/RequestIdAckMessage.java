package messages;

public class RequestIdAckMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	private int id;

	public RequestIdAckMessage() {
	}

	public RequestIdAckMessage(int id) {
		super(MessageType.REQUEST_ID_ACK);
		this.id = id;
	}

	public int getId() {
		return id;
	}

}
