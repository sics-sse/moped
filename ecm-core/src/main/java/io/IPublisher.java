package io;

import java.util.HashMap;

/**
 * This class should be implemented by all cloud-based publishers
 * 
 * @author emmanuel
 *
 */
public interface IPublisher {
	/**
	 * Set the maximum rate at which values should be published.
	 * Only values that will have changed will be considered for
	 * publishing.
	 * @param rate Rate in milliseconds, negative to turn off
	 */
	public void setRate(int rate);

	/**
	 * Get the rate limit.
	 * @return the current rate limit, a negative number when no
	 * rate limiting is setup.
	 */
	public int getRate();
	
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
	 * Attempt to publish the new value for a key.
	 * This will attempt to publish the new value for a key.  Publication might not
	 * occur due to rate limiting conditions or if the value for that key has not
	 * changed since last time it was updated. 
	 * @param key Key to publish
	 * @param value Current value for the key
	 * @return true if the attempt to publish was made, false otherwise.
	 */
	public boolean publish(String key, String value);
	
}
