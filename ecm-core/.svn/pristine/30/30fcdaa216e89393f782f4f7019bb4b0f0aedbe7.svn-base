package messages;

import java.util.ArrayList;

public class LinkMessage extends Message {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String pluginName;
	// value: LinkContextEntry
	private ArrayList<LinkContextEntry> context;
	private int callbackPortID;

	public LinkMessage(String pluginName, ArrayList<LinkContextEntry> context, int callbackPortID) {
		super(MessageType.PORT_LINK_CONTEXT_MESSAGE);
		this.pluginName = pluginName;
		this.context = context;
		this.callbackPortID = callbackPortID;
	}

	public String getPluginName() {
		return pluginName;
	}

	public ArrayList<LinkContextEntry> getContext() {
		return context;
	}

	public int getCallbackPortID() {
		return callbackPortID;
	}

}
