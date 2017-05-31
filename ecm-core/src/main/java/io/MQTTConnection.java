/**
 * @author emmanuel
 *
 */
package io;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class MQTTConnection {
	/**
	 * URL for receiving or publishing MQTT data, this will be almost compatible
	 * with the URL that the Paho library implements. However, in addition to
	 * the server and port specification, the location stored here will also
	 * contain the receiving topic, pretty much in a similar way to how HTTP
	 * URLs are built.
	 */
	private String location = null;
    private String the_server;
    private String the_uniqueId;
    private MqttConnectOptions the_connection;

	/**
	 * The class maintains a mapping between the (cleaned) server part of the
	 * URLs that it recognises as part of its constructor and Client connections
	 * within the Paho library.
	 */
	private static HashMap<String, MQTTConnectionHandler> servers = new HashMap<>();
	/**
	 * MQTT Client connection to use for that connection, this will be one of the
	 * ones that are referred to from the static hashmap stored in the class.
	 */
	private MQTTConnectionHandler handler = null;
	/**
	 * Should data be retained at the server, default is not to retain to ease
	 * on the server requirements.
	 */
	private boolean retain = false;
	/**
	 * QoS to use when sending messages to the server, default is 0 to ease on
	 * server resources. Can only be one of 0, 1 or 2, as per the MQTT
	 * specification.
	 */
	private int qos = 0;

	/**
	 * Turn this on to get additional debugging information on the standard
	 * output.
	 */
	private static boolean debug = false;

    private String uniqueId;

	/**
	 * Creates an MQTT connection. The scheme of the URL, in other words, the
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
	 * @param uniqueId
	 *            Unique identifier identifying the MQTT client.
	 * @param location
	 *            URL at which to perform the HTTP operation, see above for all
	 *            sugaring possibilities.
	 */
	public MQTTConnection(String uniqueId, String location) {
	    this.uniqueId = uniqueId;
	    initconnection(uniqueId, location);
	}

    public void initconnection(String uniqueId, String location) {
		if (uniqueId.length() > 23)
			uniqueId = uniqueId.substring(uniqueId.length() - 23);
		System.out.println("mqtt-id: " + uniqueId);
		
		//TODO: Find a better way than this quick-n-dirty fix to get rid of all these folder that MQTT creates for temporary files
		//TODO: Does this work or does it remove too much, too early???
		File currDir = new File(".");
		for (File subDir : currDir.listFiles()) {
			if (subDir.getName().contains("-tcpioteclipseorg")) {
				for (File contents : subDir.listFiles())
					contents.delete();
				
				subDir.delete();
			}
		}
		
		int colon = location.indexOf(AbstractIO.SCHEME_END);
		if (colon >= 0) {
			boolean ssl = false; // Will be true if SSL connection is requested.
			// Will be true for clean sessions, defaults to picking up the
			// default from Paho.
			boolean cleanSession = MqttConnectOptions.CLEAN_SESSION_DEFAULT;
			// Isolate the URL scheme
			String scheme = location.substring(0, colon);

			// Don't do anything if it does not start with mqtt (which includes
			// mqtts).
			if (scheme.indexOf("mqtt") >= 0) {
				// Split the URL scheme and finds out and recognise all the
				// keywords that we are interested in and implement.
				for (String token : StringUtils.split(scheme, AbstractIO.SCHEME_SEPARATOR)) {
					if (token.equalsIgnoreCase("ssl")) {
						if (debug) {
							System.out.println("Using SSL encrypted connection");
						}
						ssl = true;
					}
					if (token.equalsIgnoreCase("clean")) {
						if (debug) {
							System.out.println("Forcing clean session");
						}
						cleanSession = true;
					}
					if (token.equalsIgnoreCase("unclean")) {
						if (debug) {
							System.out.println("Forcing unclean session");
						}
						cleanSession = false;
					}
					if (token.equalsIgnoreCase("retain")) {
						if (debug) {
							System.out.println("Will retain messages at server");
						}
						this.retain = true;
					}
					if (token.indexOf("qos") >= 0) {
						// Pick up the QoS as the integer that directly follows
						// the letters qos
						String qos = token.substring(3);

						if (debug) {
							System.out.println("Using QoS of " + qos);
						}
						this.qos = Integer.parseInt(qos);
					}

					if (token.indexOf("mqtts") >= 0) {
						if (debug) {
							System.out.println("Using SSL encrypted connection");
						}
						ssl = true;
					}
				}
			}

			// Now reconstruct a location that is almost compatible with the
			// Paho library, except that it also contains the topic to post to,
			// in addition to the server and port to connect to.
			if (ssl) {
				this.location = "ssl" + location.substring(colon);
			} else {
				this.location = "tcp" + location.substring(colon);
			}

			// Create an MqttClient connection to the server if not done yet.
			int dblSlash = this.location.indexOf("//");
			int slash = this.location.indexOf("/", dblSlash + 2);
			String server = this.location.substring(0, slash);

			// Look for user name and password information from the URL and
			// actively modify the server variable so that it will contain a URL
			// that can cleanly be passed to the Paho library to open the
			// connection.
			String username = null;
			String password = null;
			int arobas = server.indexOf('@');
			if (arobas >= 0) {
				String userPass = server.substring(dblSlash + 2, arobas);
				int pwColon = server.indexOf(':');
				if (pwColon >= 0) {
					username = userPass.substring(0, pwColon + 1);
					password = userPass.substring(pwColon + 1);
				} else {
					username = userPass;
				}
				server = server.substring(0, dblSlash + 2)
						+ server.substring(arobas + 1);
			}

			// If we already have a connection to that server, reuse the
			// connection, otherwise create a new. Note that this means
			// that the current implementation cannot really support different
			// connection-wide options towards the same server. For example,
			// clean/unclean will not be respected at the second server
			// connection opening, neither will the user name and password.
			if (!servers.containsKey(server)) {
				try {
					// Create connection options instance and arrange for it to
					// carry the connection-wide parameters that we isolated
					// above, i.e. both the user name and password, but also
					// requests for a clean session.
					MqttConnectOptions connection = new MqttConnectOptions();
					this.the_connection = connection;
					connection.setCleanSession(cleanSession);
					if (username != null) {
						connection.setUserName(username);
					}
					if (password != null) {
						connection.setPassword(password.toCharArray());
					}
					if (debug) {
						System.out.println("Connecting to " + server
								+ " with options:" + connection);
					}
					// Open connection to server and remember that connection
					// for future (re)use.
					this.the_uniqueId = uniqueId;
					this.the_server = server;
					this.handler = new MQTTConnectionHandler(server, uniqueId, connection);
					servers.put(server, this.handler);
				} catch (MqttException e) {
					System.err.println("Cannot connect to MQTT server at " + server + ": " + e);
				}
			} else {
				this.handler = servers.get(server);
			}
		}
		if (debug)
		    System.out.println("end of MQTT constructor ");
	}

  public MQTTConnection(String location) {
	  this(MqttClient.generateClientId(), location);
  }
	
	public String getLocation() {
		return location;
	}

  public String getTopic() {
    int dblSlash = location.indexOf("//");
    int slash = location.indexOf("/", dblSlash + 2);
    String topic = StringUtils.trim(location.substring(slash));

    return topic;
  }
	
	public boolean send(String topic, byte[] data) {
		// Create an MQTT message with proper payload, retainment and QoS.
	    if (debug)
		System.out.println("MQTT send");

		MqttMessage message = new MqttMessage();
		message.setPayload(data);
		message.setQos(qos);
		message.setRetained(retain);
		
		if (debug) {
			System.out.println("Pushing to " + topic + ": " + message
					+ " (qos:" + qos + " retained:" + retain + ")");
		}

		// Try sending
		try {
			handler.client.publish(topic, message);
		} catch (MqttException e) {
			System.err.println("Cannot send message to " + topic
					   + ": " +e);
			try {
			    this.handler = new MQTTConnectionHandler(the_server, the_uniqueId, the_connection);
			} catch (MqttException e2) {
			    System.err.println("Cannot restart connection "
					   + ": " +e2);
			}
			return false;
		}
		return true;

		
	}

	public boolean subscribe(String filter, IMessage dispatch) {
    return this.handler.subscribe(filter, dispatch);
	}

  public boolean subscribe(IMessage dispatch) {
    return subscribe(getTopic(), dispatch);
  }

  private class MQTTConnectionHandler implements MqttCallback {
    private MqttClient client;
    private HashMap<String, IMessage> dispatchers = new HashMap<>();

      private String uri, id;
      MqttConnectOptions options;

    public MQTTConnectionHandler(String uri, String id, MqttConnectOptions options) throws MqttException {
      // Open connection to remote server using a Paho compliant
      // URL.
      this.client = new MqttClient(uri, id);

      this.uri = uri;
      this.id = id;
      this.options = options;

      // Open connection to server
      this.client.connect(options);
    }


    public boolean subscribe(String filter, IMessage dispatch) {
	System.out.println("MQTTConnectionHandler subscribe (" + filter + ")");
      client.setCallback(this);            // Arrange to receive all messages here
      try {
	  System.out.println(" MQTT1");
        client.subscribe(filter);          // Subscribe to topic filter
	  System.out.println(" MQTT2");
        dispatchers.put(filter, dispatch); // Remember filter for re-dispatch on arrival
	  System.out.println(" MQTT3");

        return true;
      } catch (MqttException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return false;
    }

    @Override
    public void connectionLost(Throwable cause) {
      System.err.println("Connection to MQTT server at "+client.getServerURI()+" lost. (" + location + ") " + cause);

      try {
	  this.client = new MqttClient(this.uri, this.id);
	  this.client.connect(this.options);
      } catch (MqttException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
      // TODO Auto-generated method stub

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
	System.out.println("Receiving on " + topic + ": " + message);

      if (debug) {
        System.out.println("Receiving on " + topic + ": " + message);
      }

      // Looks among all existing subscriptions for the one(s) that match the
      // incoming topic and thus should be triggered with the content of the message
      System.out.println("dispatcher: " + dispatchers.size());
      System.out.println("dispatcher set: " + dispatchers.entrySet().size());

      for (Map.Entry<String,IMessage> entry : dispatchers.entrySet()) {
	  System.out.println("testing dispatcher");
        if (MQTTSubscription.matchTopics(topic,entry.getKey())) {
            System.out.println("Topic " + topic + " matches " + entry.getKey() + ", forwarding");
          if (debug) {
            System.out.println("Topic " + topic + " matches " + entry.getKey() + ", forwarding");
          }
          entry.getValue().receive(topic + ":" + message.toString());
        }
      }
    }
  }
	
}
