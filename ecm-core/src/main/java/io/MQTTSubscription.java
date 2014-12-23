package io;

import java.text.ParseException;
import java.util.*;

/**
 * Created by emmanuel on 2014-10-07. Code from moquette-mqtt
 */
public class MQTTSubscription {
  /**
   * Verify if the 2 topics matching respecting the rules of MQTT Appendix A
   */
  //TODO reimplement with iterators or with queues
  public static boolean matchTopics(String msgTopic, String subscriptionTopic) {
    try {
      List<MQTTToken> msgTokens = MQTTSubscription.parseTopic(msgTopic);
      List<MQTTToken> subscriptionTokens = MQTTSubscription.parseTopic(subscriptionTopic);
      int i = 0;
      MQTTToken subToken = null;
      for (; i< subscriptionTokens.size(); i++) {
        subToken = subscriptionTokens.get(i);
        if (subToken != MQTTToken.MULTI && subToken != MQTTToken.SINGLE) {
          if (i >= msgTokens.size()) {
            return false;
          }
          MQTTToken msgToken = msgTokens.get(i);
          if (!msgToken.equals(subToken)) {
            return false;
          }
        } else {
          if (subToken == MQTTToken.MULTI) {
            return true;
          }
          if (subToken == MQTTToken.SINGLE) {
            //skip a step forward
          }
        }
      }
      //if last token was a SINGLE then treat it as an empty
//            if (subToken == Token.SINGLE && (i - msgTokens.size() == 1)) {
//               i--;
//            }
      return i == msgTokens.size();
    } catch (ParseException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected static List<MQTTToken> parseTopic(String topic) throws ParseException {
    List res = new ArrayList<MQTTToken>();
    String[] splitted = topic.split("/");

    if (splitted.length == 0) {
      res.add(MQTTToken.EMPTY);
    }

    if (topic.endsWith("/")) {
      //Add a fictious space
      String[] newSplitted = new String[splitted.length + 1];
      System.arraycopy(splitted, 0, newSplitted, 0, splitted.length);
      newSplitted[splitted.length] = "";
      splitted = newSplitted;
    }

    for (int i = 0; i < splitted.length; i++) {
      String s = splitted[i];
      if (s.isEmpty()) {
//                if (i != 0) {
//                    throw new ParseException("Bad format of topic, expetec topic name between separators", i);
//                }
        res.add(MQTTToken.EMPTY);
      } else if (s.equals("#")) {
        //check that multi is the last symbol
        if (i != splitted.length - 1) {
          throw new ParseException("Bad format of topic, the multi symbol (#) has to be the last one after a separator", i);
        }
        res.add(MQTTToken.MULTI);
      } else if (s.contains("#")) {
        throw new ParseException("Bad format of topic, invalid subtopic name: " + s, i);
      } else if (s.equals("+")) {
        res.add(MQTTToken.SINGLE);
      } else if (s.contains("+")) {
        throw new ParseException("Bad format of topic, invalid subtopic name: " + s, i);
      } else {
        res.add(new MQTTToken(s));
      }
    }

    return res;
  }

}
