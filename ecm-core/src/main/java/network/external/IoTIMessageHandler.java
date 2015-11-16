package network.external;

import messages.PluginMessage;
import ecm.Ecm;
import io.IMessage;

public class IoTIMessageHandler implements IMessage {
	private Ecm ecm;

	public IoTIMessageHandler(Ecm ecm) {
		this.ecm = ecm;
	}

	@Override
	public void receive(String msg) {
	    System.out.println("[IOT] - " + msg);
	    PluginMessage pluginMessage;
	    pluginMessage = new PluginMessage(-1, msg);
	    ecm.process(pluginMessage);
	    if (false) {
		int startIdIndex = msg.indexOf("\"id\":") + 6;
		String stripPrefix =  msg.substring(startIdIndex);
		int endIdIndex =stripPrefix.indexOf("\",\"");
		String key = stripPrefix.substring(0, endIdIndex);
		if(key.equals("platoon")) {
		    int startValIndex = stripPrefix.indexOf("\"current_value\":");
		    stripPrefix = stripPrefix.substring(startValIndex + 17);
		    int endValIndex = stripPrefix.indexOf("\"}]}");
		    String val = stripPrefix.substring(0, endValIndex);
			
		    // TODO: refine package encapsulation
		    // forward to specific ECU
		    pluginMessage = new PluginMessage(-1, val);
		    ecm.process(pluginMessage);
		} else {
		    // ignore other values so far
		}
	    }
	}

}
