package io;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

/**
 * This class will attempt to publish the value of a key to a (remote) MQTT
 * server, at a given topic. The class supports sugaring on the URL scheme to be
 * able to support different connection and messaging options for the MQTT
 * operations to occur. Additional (and different) sugaring can be used to
 * convert the name of the key and its current value as part of the data carried
 * by the MQTT message or as part of the topic receiving the data itself. This
 * is better described in the constructor.
 * 
 * @author Emmanuel Frécon
 */
public class MQTTPublisher extends AbstractPublisher implements IPublisher {
	/**
	 * Template to use to convert the data to be posted to the topic. At
	 * present, all data is converted to string, but we might add some options
	 * recognised as part of the sugaring in the constructor to automatically
	 * convert integers or floats, or to force conversion whenever possible and
	 * relevant.
	 */
	private String template = null;

	private MQTTConnection connection = null;

    public String vin = "unknown";

	/**
	 * Creates an MQTT publisher that will arrange to publish keys and their
	 * values whenever necessary. The scheme of the URL, in other words, the
	 * characters straight before the first : sign, can be subject to sugaring
	 * using the + sign as a separator. For example, a URL starting with
	 * <code>mqtt+ssl://</code> will be understood as an MQTT connection secured
	 * using the SSL protocol.
	 * <p>
	 * 
	 * Apart from <code>mqtt</code> and <code>mqtts</code>, the following
	 * sugaring keywords are recognised:
	 * <dl>
	 * <dt>clean</dt>
	 * <dd>Force a clean session at connection time, the default is as per the
	 * Paho default.</dd>
	 * <dt>unclean</dt>
	 * <dd>Force an unclean session at connection time, the default is as per
	 * the Paho default.</dd>
	 * <dt>retain</dt>
	 * <dd>Messages will be requested to be retained at server.</dd>
	 * <dt>qos0</dt>
	 * <dd>Force QoS 0</dd>
	 * <dt>qos1</dt>
	 * <dd>Force QoS 1</dd>
	 * <dt>qos2</dt>
	 * <dd>Force QoS 2</dd>
	 * <dt>ssl</dt>
	 * <dd>Same as specifying <code>mqtts</code></dd>
	 * </dl>
	 * 
	 * The server and port part of the URL can also contain a username and
	 * password specification, details that will be provided when opening the
	 * connection to the MQTT server. These should be represented as a
	 * colon-separated pair, prepended to the server and port specification and
	 * separated from that specification by an @ sign.
	 * 
	 * In addition, both the location and the template can be object of another
	 * type of sugaring. Strings enclosed by %-signs will be replaced by the
	 * value of the key or the value as follows: <code>%key%</code> will be
	 * replaced by the name of the key. <code>%value%</code> will be replaced by
	 * the value of the key and any string matching the name of the key enclosed
	 * by <code>%</code> signs will be replaced by the value of the key.
	 * 
	 * @param uniqueId
	 *            Unique identifier identifying the MQTT client.
	 * @param location
	 *            URL at which to perform the HTTP operation, see above for all
	 *            sugaring possibilities.
	 * @param template
	 *            Data to push as part of the HTTP operation, this will
	 *            typically only be used for PUT or POST operations.
	 */
	public MQTTPublisher(String uniqueId, String location, String template) {
		connection = new MQTTConnection(uniqueId, location);
		this.template = template;
	}

  public MQTTPublisher(String location, String template) {
    connection = new MQTTConnection(location);
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
	    if (false) {
		// Arndt: temporary
		// skip the actual publishing, but write on stdout what would
		// have happened
		System.out.println("publishing: " + key + " " + value);
		return true;
	    } else {

		if (shouldPublish(key, value)) {
		    String url = replace(connection.getLocation(), key, value);
		    String template1 = StringUtils.replace(template, "%VIN%",
							   vin);
		    String data = replace(template1, key, value);

		    // Isolate the topic to send to from the location. We do this this
		    // late in order to be able to have topics that would reflect, for
		    // example, the name of the key being considered for a push.
		    int dblSlash = url.indexOf("//");
		    int slash = url.indexOf("/", dblSlash + 2);
		    String topic = StringUtils.trim(url.substring(slash));
			
		    return connection.send(topic, data.getBytes());
		}

		return false;
	    }
	}
}
