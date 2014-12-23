package io;


import org.apache.commons.lang3.StringUtils;

/**
 * Created by emmanuel on 2014-10-08.
 */
public class PublisherFactory {
  public static IPublisher publisher(String location, String template) {
    int colon = location.indexOf("://");
    if (colon < 0) {
      return null;
    }

    // Isolate the URL scheme
    String scheme = location.substring(0, colon);
    for (String token : StringUtils.split(scheme, '+')) {
      String lToken = token.toLowerCase();
      if (lToken.startsWith("http")) {
        return new HTTPPublisher(location, template);
      } else if (lToken.startsWith("mqtt")) {
        return new MQTTPublisher(location, template);
      } else if (lToken.startsWith("ws")) {
        return new WSPublisher(location, template);
      }
    }

    return null;
  }
}
