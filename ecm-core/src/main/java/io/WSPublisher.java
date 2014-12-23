package io;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class WSPublisher extends AbstractPublisher implements IPublisher {
	/**
	 * Receiving URL for the MQTT data, this will be almost compatible with the
	 * URL that the Paho library implements. However, in addition to the server
	 * and port specification, the location stored here will also contain the
	 * receiving topic, pretty much in a similar way to how HTTP URLs are built.
	 */
	private String location = null;
	/**
	 * Template to use to convert the data to be posted to the topic. At
	 * present, all data is converted to string, but we might add some options
	 * recognised as part of the sugaring in the constructor to automatically
	 * convert integers or floats, or to force conversion whenever possible and
	 * relevant.
	 */
	private String template = null;

	/**
	 * Turn this on to get additional debugging information on the standard
	 * output.
	 */
	private static boolean debug = true;

	private WSConnection connection = null;

	private int hmSize = 0;

	/**
	 */
	public WSPublisher(String location, String template) {
    this.location = WSConnection.wsLocation(location);
    if (this.location != null) {
			try {
				this.connection = new WSConnection(this.location, getHeaders());
				this.connection.connect();
			} catch (Exception e) {
				System.err
						.println("Cannot open connection to remote web socket at "
								+ this.location);
			}
      hmSize = getHeaders().size();
		}

		this.template = template;
	}

	/**
	 * This will attempt to publish the new value for a key. Publication might
	 * not occur due to rate limiting conditions or if the value for that key
	 * has not changed since last time it was updated. The MQTT message options
	 * will be as specified by the sugaring described in the constructor.
	 * Current implementation happens synchronously in the main thread, but we
	 * might want to change for an implementation directly on top of
	 * MqttAsyncClient in the future.
	 * 
	 * @param key
	 *            Key to publish
	 * @param value
	 *            Current value for the key
	 * @return <code>true</code> if the attempt to publish was made,
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean publish(String key, String value) {
		// Disconnect and reconnect if headers have changed since last time,
		// this is to overcome a limitation of the WebSocket implementation that
		// can only specify the headers at creation time.
		if (connection != null && getHeaders().size() != hmSize) {
			System.out.println("Reconnecting to " + location
					+ " (headers have changed)");
			connection.close();
			connection = null;
		}

		if (connection == null) {
			try {
				connection = new WSConnection(location, getHeaders());
				connection.connect();
			} catch (Exception e) {
				System.err
						.println("Cannot open connection to remote web socket at "
								+ location);
			}
			hmSize = getHeaders().size();
		}

		if (shouldPublish(key, value)) {
			String data = replace(template, key, value);
			if (debug) {
				System.out.println("Pushing to " + location + ": " + data);
			}
			if (connection != null) {
				try {
					connection.send(data);
				} catch (Exception e) {
					System.err.println("Cannot send '"+data+"' to "+location);
				}
			}
			return true;
		}

		return false;
	}
}
