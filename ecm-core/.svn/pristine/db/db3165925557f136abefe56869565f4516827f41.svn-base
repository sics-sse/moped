package io;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * This class controls a number of web operations in order to perform some basic
 * rate limiting and error recovery. Several web operations can be grouped
 * (typically for a given remote server) and the class will stop accepting
 * requests once a maximum number of unfinished current connections have been
 * reached. In addition, the class will count failures and will stop serving
 * request to that server for a (controllable) amount of time, to let the server
 * recover from possible errors.
 * 
 * @author Emmanuel Frécon
 * 
 */
public class WebPoster {
	private int reqCount = 0; // Total number of requests since start
	private volatile int connectionCount = 0; // Number of on-going connections
	private volatile int failureCount = 0; // Number of failed requests
	private long waitUntil = -1; // No more requests until this time
	private String identifier = null; // Identifier of poster
	private int maxConnections = 15; // Max number of simultaneous connections
	private int maxFailures = 5; // Number of failures before respite
	private int respit = 60 * 15; // Quarter of an hour

	/**
	 * Create a WebPoster with all good defaults.
	 */
	public WebPoster() {
		this("WebPoster");
	}

	/**
	 * Create a WebPoster with an identifier that will help recognising the
	 * poster in debugging operations of various sorts.
	 * 
	 * @param id
	 *            Identifier of the poster
	 */
	public WebPoster(String id) {
		this.identifier = id;
	}

	/**
	 * Create a WebPoster with an identifier that will help recognising the
	 * poster in debugging operations of various sorts but also a specific
	 * maximum number of simultaneous connections.
	 * 
	 * @param id
	 *            Identifier of the poster.
	 * @param max
	 *            Maximum number of on-going connections.
	 */
	public WebPoster(String id, int max) {
		this.identifier = id;
		this.maxConnections = max;
	}

	/**
	 * Provide a string representation of the web poster, this representations
	 * provides a summary of the current number of connections and the current
	 * number of failures, together with their maximum allowed.
	 */
	public String toString() {
		return identifier + "--" + connectionCount + "/" + maxConnections
				+ "--" + failureCount + "/" + maxFailures;
	}

	/**
	 * Schedule an HTTP get operation for a URL
	 * 
	 * @param url
	 *            URL to get
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean get(final String url) {
		return get(url, null);
	}

	/**
	 * Schedule an HTTP get operation for a URL, with additional and specific
	 * headers.
	 * 
	 * @param url
	 *            URL to get
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean get(final String url, final Map<String, String> hdrs) {
		final String reqId = identifier + "/" + reqCount;
		return get(reqId, url, hdrs);
	}

	/**
	 * Schedule an HTTP get operation for a URL, with additional and specific
	 * headers and a specific identifier for that request
	 * 
	 * @param reqId
	 *            Identifier for that request, in order to ease debugging and
	 *            introspection.
	 * @param url
	 *            URL to get
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean get(final String reqId, final String url,
			final Map<String, String> hdrs) {
		return req(reqId, url, null, "GET", hdrs);
	}

	/**
	 * Schedule an HTTP delete operation for a URL.
	 * 
	 * @param url
	 *            URL to delete
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean delete(final String url) {
		return delete(url, null);
	}

	/**
	 * Schedule a HTTP delete operation for a URL, with additional and specific
	 * headers.
	 * 
	 * @param url
	 *            URL to delete
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean delete(final String url, final Map<String, String> hdrs) {
		final String reqId = identifier + "/" + reqCount;
		return delete(reqId, url, hdrs);
	}

	/**
	 * Schedule a HTTP delete operation for a URL, with additional and specific
	 * headers and a specific identifier for that request
	 * 
	 * @param reqId
	 *            Identifier for that request, in order to ease debugging and
	 *            introspection.
	 * @param url
	 *            URL to delete
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean delete(final String reqId, final String url,
			final Map<String, String> hdrs) {
		return req(reqId, url, null, "DELETE", hdrs);
	}

	/**
	 * Schedule an HTTP post operation for a URL
	 * 
	 * @param url
	 *            URL to post to.
	 * @param data
	 *            data to submit
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean post(final String url, final String data) {
		return post(url, data, null);
	}

	/**
	 * Schedule an HTTP post operation for a URL, with additional and specific
	 * headers.
	 * 
	 * @param url
	 *            URL to post to
	 * @param data
	 *            data to submit
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean post(final String url, final String data,
			final Map<String, String> hdrs) {
		final String reqId = identifier + "/" + reqCount;
		return post(reqId, url, data, hdrs);
	}

	/**
	 * Schedule a HTTP post operation for a URL, with additional and specific
	 * headers and a specific identifier for that request
	 * 
	 * @param reqId
	 *            Identifier for that request, in order to ease debugging and
	 *            introspection.
	 * @param url
	 *            URL to post to
	 * @param data
	 *            data to submit
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean post(final String reqId, final String url,
			final String data, final Map<String, String> hdrs) {
		return req(reqId, url, data, "POST", hdrs);
	}

	/**
	 * Schedule an HTTP put operation for a URL
	 * 
	 * @param url
	 *            URL to put date to.
	 * @param data
	 *            data to submit
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean put(final String url, final String data) {
		return put(url, data, null);
	}

	/**
	 * Schedule an HTTP put operation for a URL, with additional and specific
	 * headers.
	 * 
	 * @param url
	 *            URL to put data to
	 * @param data
	 *            data to submit
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean put(final String url, final String data,
			final Map<String, String> hdrs) {
		final String reqId = identifier + "/" + reqCount;
		return put(reqId, url, data, hdrs);
	}

	/**
	 * Schedule a HTTP put operation for a URL, with additional and specific
	 * headers and a specific identifier for that request
	 * 
	 * @param reqId
	 *            Identifier for that request, in order to ease debugging and
	 *            introspection.
	 * @param url
	 *            URL to put data to
	 * @param data
	 *            data to submit
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	public boolean put(final String reqId, final String url, final String data,
			final Map<String, String> hdrs) {
		return req(reqId, url, data, "PUT", hdrs);
	}

	/**
	 * Schedule an HTTP operation for a URL, with additional and specific
	 * headers and a specific identifier for that request
	 * 
	 * @param reqId
	 *            Identifier for that request, in order to ease debugging and
	 *            introspection.
	 * @param url
	 *            URL to post to
	 * @param data
	 *            data to submit
	 * @param method
	 *            HTTP operation to perform, e.g. GET, PUT, DELETE, etc.
	 * @param hdrs
	 *            Map of key and values to add to the HTTP headers.
	 * @return <code>true</code> if the request could be scheduled and met the
	 *         requirements when it comes to maximum of requests and failures,
	 *         <code>false</code> otherwise.
	 */
	private boolean req(final String reqId, final String url,
			final String data, final String method,
			final Map<String, String> hdrs) {
		reqCount++; // Count number of request made in total
		// Give up if we've reached the maximum number of ongoing connections.
		if (connectionCount >= maxConnections) {
			System.err.println(String.format(
					"WARNING %d %s ongoing connections, giving up",
					connectionCount, identifier));
			return false;
		} else if (failureCount >= maxFailures) {
			// Wait or reset once we've reached the maximum failure count
			Date currentDate = new Date();
			if (waitUntil < 0) {
				waitUntil = currentDate.getTime() + respit * 1000;
				System.err.println(String.format(
						"WARNING %d %s failed conns., giving up for %d secs.",
						failureCount, identifier, respit));
			} else if (currentDate.getTime() > waitUntil) {
				System.err.println(String.format(
						"INFO respit for %s has passed, will try again",
						identifier));
				failureCount = 0;
				waitUntil = -1;
			}
			return false;
		} else {
			// Everything's fine, go and try requesting for the URL in a
			// separate thread.
			String threadName;
			connectionCount++;
			threadName = this.identifier + "-" + connectionCount;
			new Thread(threadName) {
				public void run() {
					requestData(reqId, url, data, method, hdrs);
				}
			}.start();
		}
		return true;
	}

	/**
	 * Perform URL requesting operation, returned data is lost (but can be
	 * printed out on the stdout if necessary.
	 * 
	 * @param reqId
	 *            Identifier of the request
	 * @param urlStr
	 *            URL to request at
	 * @param data
	 *            Data to join to the request (for PUT or POST for example).
	 * @param method
	 *            HTTP method to call
	 * @param hdrs
	 *            HTTP headers to add to the standard ones.
	 */
	private void requestData(String reqId, String urlStr, String data,
			String method, Map<String, String> hdrs) {
		try {
			if (true) {
				System.err.println(String.format(
						"(%s) Web %s request to %s with %s", reqId, method,
						urlStr, data));
			}
			URL url = new URL(urlStr);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);

			// Perform request, pushing data in the request if necessary.
			((HttpURLConnection) conn).setRequestMethod(method);
			for (Map.Entry<String, String> e : hdrs.entrySet()) {
				conn.setRequestProperty(e.getKey(), e.getValue());
			}
			if (data != null) {
				conn.setRequestProperty("Content-Length",
						Integer.toString(data.length()));

				DataOutputStream wr = new DataOutputStream(
						conn.getOutputStream());
				wr.writeBytes(data);
				wr.flush();
				wr.close();
			}

			// Read response (but we will discard it, only log)
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				if (false) {
					System.err.println(String.format("(%s) received: %s",
							reqId, line));
				}
			}
			rd.close();
		} catch (Exception e) {
			failureCount++;
			e.printStackTrace();
		} finally {
			failureCount = 0;
			connectionCount--;
			if (false) {
				System.err.println(String.format("(%s) closed", reqId));
			}
		}
	}
}
