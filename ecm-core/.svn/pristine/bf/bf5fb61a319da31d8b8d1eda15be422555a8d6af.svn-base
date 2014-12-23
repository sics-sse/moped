package io;

import java.util.HashMap;

public interface IReceiver {

  /**
   * Set the rate at which values should be polled, for protocols
   * that require polling.
   * @param rate Rate in milliseconds, negative to turn off
   */
  public void setPoll(int rate);

  /**
   * Get the polling rate
   * @return the polling rate.
   */
  public int getPoll();


  /**
	 * Add a protocol header key and value.
	 * Add a key and value that will be used by the protocol implementation every
	 * time a publication update is being made. Not all protocols implements or
	 * require this
	 * @param key Protocol-specific key
	 * @param value Protocol-specific value 
	 */
	public void addHeader(String key, String value);

	/**
	 * Query current set of protocol headers.
	 * @return The list of protocol headers
	 */
	public HashMap<String, String> getHeaders();


  /**
   * Subscribe
   */
  public boolean subscribe(IMessage msg);

  public boolean subscribe(String rx, int grp, IMessage msg);

  public boolean unsubscribe();
}
