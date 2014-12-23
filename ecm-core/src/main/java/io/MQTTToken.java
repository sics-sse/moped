package io;

/**
 * Created by emmanuel on 2014-10-07. Code from moquette-mqtt
 */
public class MQTTToken {
  static final MQTTToken EMPTY = new MQTTToken("");
  static final MQTTToken MULTI = new MQTTToken("#");
  static final MQTTToken SINGLE = new MQTTToken("+");
  String name;

  protected MQTTToken(String s) {
    name = s;
  }

  protected String name() {
    return name;
  }

  protected boolean match(MQTTToken t) {
    if (t == MULTI || t == SINGLE) {
      return false;
    }

    if (this == MULTI || this == SINGLE) {
      return true;
    }

    return equals(t);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MQTTToken other = (MQTTToken) obj;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return name;
  }
}
