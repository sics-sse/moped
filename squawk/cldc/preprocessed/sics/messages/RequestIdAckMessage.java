package sics.messages;

public class RequestIdAckMessage extends Message {

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
