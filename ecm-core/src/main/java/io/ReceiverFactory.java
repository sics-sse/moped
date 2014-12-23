package io;

import org.apache.commons.lang3.StringUtils;

/**
 * This class will construct proper receivers based on the scheme in the
 * location (URI).  It analyses the scheme of the URI, while supporting the
 * sugaring that is used by all under classes for giving protocol specific
 * options.
 *
 * Created by emmanuel on 2014-10-08.
 */
public class ReceiverFactory {

  public static IReceiver receiver(String location, String init) {
    int colon = location.indexOf("://");
    if (colon < 0) {
      return null;
    }

    // Isolate the URL scheme
    String scheme = location.substring(0, colon);
    for (String token : StringUtils.split(scheme, AbstractIO.SCHEME_SEPARATOR)) {
      String lToken = token.toLowerCase();
      if (lToken.startsWith("mqtt")) {
        return new MQTTReceiver(location);
      } else if (lToken.startsWith("ws")) {
        return new WSReceiver(location, init);
      }
    }

    return null;
  }

  public static IReceiver receiver(String location) {
    return receiver(location, null);
  }
}
