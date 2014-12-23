package io;

import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;

public class WSReceiver extends AbstractReceiver implements IReceiver {
	/**
	 * Receiving URL for the MQTT data, this will be almost compatible with the
	 * URL that the Paho library implements. However, in addition to the server
	 * and port specification, the location stored here will also contain the
	 * receiving topic, pretty much in a similar way to how HTTP URLs are built.
	 */
	private String location = null;

	private WSConnection connection = null;

	private int hmSize = 0;

	/**
	 */
	public WSReceiver(String location, String opening) {
    this.location = WSConnection.wsLocation(location);
    if (this.location != null) {
			try {
				this.connection = new WSConnection(this.location, getHeaders());
				this.connection.connect(opening);
			} catch (Exception e) {
				System.err
						.println("Cannot open connection to remote web socket at "
								+ this.location);
			}
		}
	}

  public WSReceiver(String location) {
    this(location, null);
  }

  @Override
  public boolean subscribe(IMessage msg) {
    return connection.subscribe(addDispatcher(msg));
  }

  @Override
  public boolean subscribe(String rx, int grp, IMessage msg) {
    return connection.subscribe(addDispatcher(rx, grp, msg));
  }

  @Override
  public boolean unsubscribe() {
    return false;
  }

}
