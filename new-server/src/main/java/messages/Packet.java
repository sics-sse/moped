package messages;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class PackageMessage.
 */
public abstract class Packet implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The message type. */
	private int messageType;
	
	/** The vin. */
	private String vin;

	/**
	 * Instantiates a new package message.
	 */
	public Packet() {
	}

	/**
	 * Instantiates a new package message.
	 *
	 * @param messageType the message type
	 * @param vin the vin
	 */
	public Packet(int messageType, String vin) {
		this.messageType = messageType;
		this.vin = vin;
	}

	/**
	 * Gets the message type.
	 *
	 * @return the message type
	 */
	public int getMessageType() {
		return messageType;
	}

	/**
	 * Sets the message type.
	 *
	 * @param messageType the new message type
	 */
	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	/**
	 * Gets the vin.
	 *
	 * @return the vin
	 */
	public String getVin() {
		return vin;
	}

	/**
	 * Sets the vin.
	 *
	 * @param vin the new vin
	 */
	public void setVin(String vin) {
		this.vin = vin;
	}

}
