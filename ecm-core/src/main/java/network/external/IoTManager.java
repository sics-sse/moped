package network.external;

import ecm.Ecm;
import io.IPublisher;
import io.IReceiver;
import messages.PublishPacket;

public class IoTManager implements Runnable {
	private Ecm ecm;
	private IPublisher publisher;
	private IReceiver receiver;
	
	public IoTManager(IPublisher publisher) { //, IReceiver receiver) {
		this.publisher = publisher;
		this.receiver = null;
		this.ecm = null;
	}
	
    public IoTManager(IPublisher publisher, IReceiver receiver) {
		this.publisher = publisher;
		this.receiver = receiver;
	}
	
	public void run() {
	    if (receiver != null) {
		IoTIMessageHandler ioTIMessageHandler = new IoTIMessageHandler(ecm);
		System.out.println("subscribe IoTIMessageHandler");
		receiver.subscribe(ioTIMessageHandler);
	    }
	}

	public void sendPacket(PublishPacket packet) {
		String key = packet.getKey();
		String value = packet.getValue();
		publisher.publish(key, value);
	}
	
	public void setEcm(Ecm ecm) {
		this.ecm = ecm;
	}
}
