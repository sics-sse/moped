package io;

import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_17;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by emmanuel on 2014-10-08.
 */
public class WSConnection extends WebSocketClient {
  /**
   * Turn this on to get additional debugging information on the standard
   * output.
   */
  private static boolean debug = false;

  /**
   * Initial message to send each time the connection is (re)opened. This can
   * be used to send a subscription message or similar.  Maybe do we want to 
   * expand this to a series of messages or similar.
   */
  private String initMessage = null;

  /**
   * Queued of messages to sent.  The queue will grow as clients will try to
   * send messages but the handshake with the server hasn't finished yet.
   */
  public Queue<String> queue = new LinkedList<>();

  /**
   * Maximum size of the queue.  When too many messages are enqueued and the
   * queue has reached this size, older messages will be removed to make space
   * for new ones.  Setting this value to -1 will lead to an infinite queue.
   * Setting it to zero will turn off enqueuing.
   */
  public static int QUEUE_MAX = 20;

  /**
   * Accounts for state of connection, i.e. will be set to true when the
   * handshake with the server has succeeded and the remote end is ready to
   * accept messages (or to send some).
   */
  private boolean opened = false;

  /**
   * List of dispatchers that have been registered to receive incoming
   * messages as sent by server.
   */
  public List<IMessage> subscriptions = new ArrayList<>();

  /**
   * Should we automatically try to reconnect when connection has been closed.
   */
  public boolean autoReconnect = false;

  public WSConnection(URI uri, Map<String,String> headers) {
    super(uri, new Draft_17(), headers, 0);
  }
  public WSConnection(String location, Map<String,String> headers) throws URISyntaxException {
    this(new URI(location), headers);
  }
  public WSConnection(URI uri) {
    super(uri);
  }

  public WSConnection(String location) throws URISyntaxException {
    this(new URI(location));
  }

  /**
   * Send (or queue, see below) text to remote server.  If the connection is
   * alive, the text will be sent directly.  Otherwise, the text will be queued
   * to be sent as soon as the connection is alive.  The queuing behaviour is
   * controlled by QUEUE_MAX, which includes a way to automatically turn off
   * that feature.
   * @param txt  Text to be sent to remote server
   */
  public void send(String txt) {
    if (opened) {
      if (debug) {
        System.out.println("Sending to " + getURI() + ": " + txt);
      }
      super.send(txt);
    } else {
      // Max size reached, remove oldest element in queue.
      if (QUEUE_MAX > 0 && queue.size() >= QUEUE_MAX) {
        String oldest = queue.poll();
        if (debug) {
          System.out.println("Removed '" + oldest + "' from too large queue!");
        }
      }

      if (QUEUE_MAX != 0) {
        queue.offer(txt);
        if (debug) {
          System.out.println("Enqueued '" + txt + "' for sending, not connected (yet?).");
        }
      }
    }
  }

  /**
   * Connect to server, registering an empty initialisation message (i.e. the
   * most usual case).
   */
  public void connect() {
    this.connect(null);
  }

  /**
   * Connect to the server, making sure that a message will be automatically
   * be sent to the server as soon as the connection has been opened and is
   * working as it should.
   * @param init Initial message to send to server when connection is opened.
   */
  public void connect(String init) {
    initMessage = init;
    super.connect();
  }

  /**
   * Reconnect to server, keep the initial message if there was one so as to
   * resend it when the connection has been reopened.
   */
  public void reconnect() {
    // Don't change the init message!!
    super.connect();
  }

  /**
   * Return current initial message
   * @return the current initial message, if any. null if no such message.
   */
  public String getInit() {
    return initMessage;
  }

  /**
   * Arrange an interface to be called back whenever data has been sent
   * by the server back to the client.
   * @param dispatcher Interface to call on message reception
   * @return true if subscription was a success.
   */
  public boolean subscribe(IMessage dispatcher) {
    return subscriptions.add(dispatcher);
  }

  /**
   * Remove a dispatcher interface that had been registered using @unsubscribe
   * @param dispatcher Interface to remove
   * @return true if the interface was registered and could be removed.
   */
  public boolean unsubscribe(IMessage dispatcher) {
    return subscriptions.remove(dispatcher);
  }

  /**
   * Construct a proper URI out of the (possibly sugared) URI.  This procedure
   * supports sugaring with the "ssl" keyword so as to be compatible and in
   * range with other protocols that do not support the terminal "s".
   * @param location Sugared location
   * @return a real URI string that can be used to opened a websocket.
   */
  public static String wsLocation(String location) {
    String wsLoc = null;

    int colon = location.indexOf(AbstractIO.SCHEME_END);
    if (colon >= 0) {
      boolean ssl = false; // Will be true if SSL connection is requested.
      // Isolate the URL scheme
      String scheme = location.substring(0, colon);

      // Don't do anything if it does not start with ws (which includes
      // wss).
      if (scheme.indexOf("ws") >= 0) {
        // Split the URL scheme and finds out and recognise all the
        // keywords that we are interested in and implement.
        for (String token : StringUtils.split(scheme, AbstractIO.SCHEME_SEPARATOR)) {
          if (token.equalsIgnoreCase("ssl")) {
            if (debug) {
              System.out
                      .println("Using SSL encrypted connection");
            }
            ssl = true;
          }
          if (token.indexOf("wss") >= 0) {
            if (debug) {
              System.out
                      .println("Using SSL encrypted connection");
            }
            ssl = true;
          }
        }
      }

      // Now reconstruct a location that is almost compatible with the
      // Paho library, except that it also contains the topic to post to,
      // in addition to the server and port to connect to.
      if (ssl) {
        wsLoc = "wss" + location.substring(colon);
      } else {
        wsLoc = "ws" + location.substring(colon);
      }
    }
    return wsLoc;
  }

  /**
   * Send initial message and queued messages whenver the connection has
   * been opened.
   * @param handshakedata
   */
  @Override
  public void onOpen(ServerHandshake handshakedata) {
    int len = queue.size();   // Compute size of queue

    // Remember that connection is now opened.
    System.out.println("opened connection");
    opened = true;

    // If we had an initial message associated to this connection, now is
    // a good time to send it!
    if (initMessage != null) {
      if (debug) {
        System.out.println("Sending init message '" + initMessage + "'");
      }
      send(initMessage);
    }

    // Send content of queue, if any.
    if (len > 0) {
      if (debug) {
        System.out.println("Sending content of queue ("+len+" elements)");
        while (queue.peek() != null) {
          send(queue.poll());  // Use our own send to benefit from debug.
        }
      }
    }
  }

  /**
   * Dispatch message to subscribers
   * @param message
   */
  @Override
  public void onMessage(String message) {
    if (debug) {
      System.out.println("received: " + message);
    }
    for (IMessage d : subscriptions) {
      d.receive(message);
    }
  }

  /**
   * Callback for closed connection.  We will try to reconnect if appropriate.
   * @param code
   * @param reason
   * @param remote
   */
  @Override
  public void onClose(int code, String reason, boolean remote) {
    if (debug) {
      System.out.println("Connection closed by "
              + (remote ? "remote peer" : "us"));
    }
    opened = false;
    queue.clear();   // Nothing should be in queue.

    // If the remote end had closed the connection, try reconnecting if we
    // should.
    if (remote && autoReconnect) {
      reconnect();
    }
  }

  @Override
  public void onError(Exception ex) {
    ex.printStackTrace();
    // if the error is fatal then onClose will be called additionally
  }
}
