package io;

public class MQTTReceiver extends AbstractReceiver implements IReceiver {
  private MQTTConnection connection = null;

  public MQTTReceiver(String uniqueId, String location) {
    connection = new MQTTConnection(uniqueId, location);
  }

  public MQTTReceiver(String location) {
    connection = new MQTTConnection(location);
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
