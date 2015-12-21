package messages;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Enum MessageType.
 */
public class MessageType extends Enum implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final byte INIT = 0;
	public static final byte INSTALL_ACK = 1;
	public static final byte UNINSTALL_ACK = 2;
	public static final byte PUBLISH = 3;
	public static final byte INSTALL = 4;
	public static final byte UNINSTALL = 5;
	public static final byte RESTORE = 6;
	public static final byte PLUGIN_MESSAGE = 7;
    //public static final byte PORT_LINK_CONTEXT_MESSAGE = 8;
    //public static final byte PORT_LINK_ACK_MESSAGE = 9;
	public static final byte RESTORE_ACK = 10;
	public static final byte LOAD = 11;
    //public static final byte LOAD_ACK = 12;
    //public static final byte SUBSCRIBE = 13;
    //public static final byte REQUEST_ID = 14;
    //public static final byte REQUEST_ID_ACK = 15;
	public static final byte PWM = 16;
	public static final byte INSTALL_LINUX_ACK = 17;
	public static final byte PINGCAR = 18;
	public static final byte INIT2 = 19;
	protected MessageType(byte enumValue) {
		super(enumValue);
		// TODO Auto-generated constructor stub
	}

}
