package messages;

public class DebugPacket extends Packet {
	private static final long serialVersionUID = 1L;

	public DebugPacket(String msgAsVin) {
		super(100, msgAsVin);
	}
}
