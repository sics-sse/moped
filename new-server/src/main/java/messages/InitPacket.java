package messages;

// TODO: Auto-generated Javadoc
/**
 * The Class InitPackage.
 */
public class InitPacket extends Packet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
    //    public boolean is_simulator;

	/**
	 * Instantiates a new inits the package.
	 *
	 * @param vin the vin
	 */
	public InitPacket(String vin) {
		super(MessageType.INIT,vin);
	}
}
