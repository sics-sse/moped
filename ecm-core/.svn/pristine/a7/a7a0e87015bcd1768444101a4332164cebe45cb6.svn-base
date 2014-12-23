package io;

import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * This class provides the ground mechanisms for all publisher. All publisher can
 * have a set of headers, headers that will be specific to the protocol implementation
 * but are store and queried through this class. In addition, the class provides the
 * base capability for rate limiting, in order to only publish when the specific
 * time period has passed and when the value for a key have changed.
 *
 * @author emmanuel
 */
public abstract class AbstractPublisher extends AbstractIO {
	/* The rate at which to publish, in milliseconds.  Negative to
	 * always publish (which is the default in order to not trigger
	 * any rate limiting by default).
	 */
	private int rate = -1;
	
	/* HashMap containing the protocol specific headers for that
	 * publisher.
	 */
	private HashMap<String,String> headers = new HashMap<String,String>();
	
	/* HashMap to store the history of the keys, this will map
	 * (dynamically) any key to its value and the time at which
	 * the key was published.
	 */
	private HashMap<String,LatestValue> keys = new HashMap<String,LatestValue>();
	
	/**
	 * Set the maximum rate at which values should be published.
	 * Only values that will have changed will be considered for
	 * publishing.
	 * @param rate Rate in milliseconds, negative to turn off
	 */
	public void setRate(int rate) {
		this.rate = rate;
	}

	/**
	 * Get the rate limit.
	 * @return the current rate limit, a negative number when no
	 * rate limiting is setup.
	 */
	public int getRate() {
		return rate;
	}
	
	/**
	 * Replace sugar strings with their values.
	 * This will replace all occurrences of %key%, %value% and of the
	 * name of the key surrounded by %-signs by their value in the string
	 * passed as a template.
	 * @param template Template in which to perform substitution
	 * @param key Current key to be published
	 * @param value Current value of key
	 * @return Substituted string
	 */
	protected String replace(String template, String key, String value) {
		String result = null;
		
		result = StringUtils.replace(template, "%key%", key);
		result = StringUtils.replace(result, "%value%", value);
		result = StringUtils.replace(result, "%"+key+"%", value);
		
		return result;
	}
	
	/**
	 * Rate limiting publication decision.
	 * This method will decide if an update for the key and its value passed
	 * as a parameter should occur or not, based on the current rate limiting
	 * parameters and on the previous value for the key.
	 * @param key Key to be updated
	 * @param value Current value of key.
	 * @return true if this update should be published, false otherwise.
	 */
	protected boolean shouldPublish(String key, String value) {
		long now = System.currentTimeMillis();
		
		if (keys.containsKey(key)) {
			/* We already know about that key, see if it has changed since
			 * last time we were called for that key and if too much time 
			 * was spent since the last update.
			 */
			LatestValue latest = keys.get(key);
			// Only consider an update if the value has changed in the first place.
			if (!latest.value.equals(value)) {
				latest.value = value;
				// Push if we've passed the time to publish.
				if (now - latest.lastPush >= rate) {
					latest.lastPush = now;
					return true;
				}
			}
		} else {
			/* This is a new key, remember it and arrange to publish it. */
			LatestValue latest = new LatestValue(value, now);
			keys.put(key, latest);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Add a protocol header key and value.
	 * Add a key and value that will be used by the protocol implementation every
	 * time a publication update is being made.
	 * @param key Protocol-specific key
	 * @param value Protocol-specific value 
	 */
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	/**
	 * Query current set of protocol headers.
	 * @return The list of protocol headers
	 */
	public HashMap<String, String> getHeaders() {
		return headers;
	}

	/**
	 * This class will hold the latest value for a key, including the time
	 * at which this value was set.
	 * @author emmanuel
	 *
	 */
	private class LatestValue {
		public String value = null;   // Current value
		public long lastPush = -1;    // Last time it was published
		
		public LatestValue() {
		}
		
		public LatestValue(String value) {
			this.value = value;
		}

		public LatestValue(String value, long lastPush) {
			this.value = value;
			this.lastPush = lastPush;
		}
	}

}
