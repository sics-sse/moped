package fresta.port.instances;

import messages.PublishMessage;
import autosar.RTE;
import sics.port.EcuVirtualRPort;

public class VirtualPublishRPort extends EcuVirtualRPort {
	public VirtualPublishRPort(int id) {
		super(id);
	}

	@Override
	public void deliver(Object data) {
		String dataStr = (String)data;
		int barIndex = dataStr.indexOf('|');
		String key = dataStr.substring(0, barIndex);
		String value = dataStr.substring(barIndex+1);
		PublishMessage publishMessage = new PublishMessage(key, value);
		RTE.getInstance().addRteMessage(publishMessage);
	}
	
}
