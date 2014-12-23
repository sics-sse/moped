package messages;

//import java.util.LinkedHashMap;

// TODO: Auto-generated Javadoc
/**
 * The Class InstallAckMessage.
 */
public class PublishMessage extends Message {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	// private LinkedHashMap<String, String> messages;
	private String key;
	private String value;

	public PublishMessage(String key, String value) {
		super(MessageType.PUBLISH);
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	// public PublishMessage(LinkedHashMap<String, String> messages) {
	// super(MessageType.PUBLISH);
	// this.messages = messages;
	// }

	// public LinkedHashMap<String, String> getMessages() {
	// return messages;
	// }

}
