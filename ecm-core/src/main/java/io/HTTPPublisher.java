package io;

import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;

/**
 * This class will attempt to publish the value of a key to a (remote) URL. The
 * class supports sugaring on the URL scheme to be able to not only support
 * regular GET operations, but also PUT or other less widely used HTTP
 * operations. Additional (and different) sugaring can be used to convert the
 * name of the key and its current value as part of the data carried by the HTTP
 * operation or as part of the URL being requested itself. This is better
 * described in the constructor.
 * 
 * @author Emmanuel Frécon
 */
public class HTTPPublisher extends AbstractPublisher implements IPublisher {
	/**
	 * Will perform an HTTP GET operation.
	 */
	public static final int TYPE_GET = 0;
	/**
	 * Will perform an HTTP POST operation.
	 */
	public static final int TYPE_POST = 1;
	/**
	 * Will perform an HTTP PUT operation.
	 */
	public static final int TYPE_PUT = 2;
	/**
	 * Will perform an HTTP DELETE operation.
	 */
	public static final int TYPE_DELETE = 3;

	private String location = null; // URL to publish to
	private String template = null; // Template to send
	private int type = TYPE_GET; // Type of the HTTP operation

	/*
	 * There will be as many WebPoster as there are servers (as in hostname and
	 * port) registered. This hashmap keeps track of them to ensure we can reuse
	 * the same WebPoster from several HTTPPublishers.
	 */
	private static HashMap<String, WebPoster> servers = new HashMap<String, WebPoster>();
	/*
	 * Current WebPoster used for this HTTPPublisher, i.e. one of those
	 * contained in the servers hashmap.
	 */
	private WebPoster server = null;
	
	private static boolean debug = false;

	/**
	 * Create a new HTTP publisher that will arrange to publish keys and their
	 * values whenever necessary. The scheme of the URL, in other words, the
	 * characters straight before the first : sign, can be subject to sugaring
	 * using the + sign as a separator. For example, a URL starting with
	 * <code>https+put://</code> will be understood as an HTTP PUT operation
	 * performed within an SSL protected HTTP connection.
	 * <p>
	 * 
	 * Apart from <code>http</code> and <code>https</code>, the following
	 * sugaring keywords are recognised:
	 * <dl>
	 * <dt>get</dt>
	 * <dd>HTTP GET operation, the default, template is ignored</dd>
	 * <dt>put</dt>
	 * <dd>HTTP PUT operation</dd>
	 * <dt>post</dt>
	 * <dd>HTTP POST operation</dd>
	 * <dt>delete</dt>
	 * <dd>HTTP DELETE operation, template is ignored</dd>
	 * <dt>del</dt>
	 * <dd>HTTP DELETE operation, template is ignored</dd>
	 * <dt>ssl</dt>
	 * <dd>Same as specifying <code>https</code></dd>
	 * </dl>
	 * 
	 * In addition, both the location and the template can be object of another
	 * type of sugaring. Strings enclosed by %-signs will be replaced by the
	 * value of the key or the value as follows: <code>%key%</code> will be
	 * replaced by the name of the key. <code>%value%</code> will be replaced by
	 * the value of the key and any string matching the name of the key enclosed
	 * by <code>%</code> signs will be replaced by the value of the key.
	 * 
	 * @param location
	 *            URL at which to perform the HTTP operation, see above for all
	 *            sugaring possibilities.
	 * @param template
	 *            Data to push as part of the HTTP operation, this will
	 *            typically only be used for PUT or POST operations.
	 */
	public HTTPPublisher(String location, String template) {
		/*
		 * Look for the first colon sign to be able to perform sugaring on the
		 * URL scheme.
		 */
		int colon = location.indexOf(SCHEME_END);
		if (colon >= 0) {
			boolean https = false;
			String scheme = location.substring(0, colon);

			/* Only do something if we know we are dealing with HTTP */
			if (scheme.indexOf("http") >= 0) {
				/* Split on + sign and look for known keywords */
				for (String token : StringUtils.split(scheme, SCHEME_SEPARATOR)) {
					if (token.indexOf("post") >= 0) {
						this.type = TYPE_POST;
					} else if (token.indexOf("put") >= 0) {
						this.type = TYPE_PUT;
					} else if (token.indexOf("del") >= 0) {
						this.type = TYPE_DELETE;
					}

					if (token.indexOf("ssl") >= 0) {
						https = true;
					}

					if (token.indexOf("https") >= 0) {
						https = true;
					}
				}
			}

			/*
			 * Rearrange so we've removed all scheme sugaring and now have a
			 * clean(er) URL that we will store in the location.
			 */
			if (https) {
				this.location = "https" + location.substring(colon);
			} else {
				this.location = "http" + location.substring(colon);
			}

			// Create a webposter for the server if we hadn't one already
			int dblSlash = this.location.indexOf("//");
			int slash = this.location.indexOf("/", dblSlash + 2);
			String server = this.location.substring(0, slash);
			if (!servers.containsKey(server)) {
				this.server = new WebPoster(server);
				servers.put(server, this.server);
				if (debug) {
					System.out.println(String.format("Registering new server at %s -- %s", server, this.server));
				}
			} else {
				this.server = servers.get(server);
				if (debug) {
					System.out.println(String.format("Reusing existing server at %s -- %s", server, this.server));
				}
			}
		}

		this.template = template;
	}

	/**
	 * This will attempt to publish the new value for a key. Publication might
	 * not occur due to rate limiting conditions or if the value for that key
	 * has not changed since last time it was updated. The HTTP operation will
	 * be as specified by the sugaring described in the constructor and all
	 * protocol headers will be added as HTTP headers. Web HTTP posting is
	 * delegated to the {@link WebPoster} class, a class that performs web
	 * operations in a controlled manner using threads.
	 * 
	 * @param key
	 *            Key to publish
	 * @param value
	 *            Current value for the key
	 * @return <code>true</code> if the attempt to publish was made,
	 *         <code>false</code> otherwise.
	 */
	public boolean publish(String key, String value) {
		if (debug) {
			System.out.println(String.format("Considering <%s,%s> for publication at %s", key, value, this.server));
		}
		if (shouldPublish(key, value)) {
			final String url = replace(location, key, value);
			final String data = replace(template, key, value);
			final HashMap<String, String> hdrs = getHeaders();

			switch (type) {
			case TYPE_PUT:
				server.put(url, data, hdrs);
				return true;
			case TYPE_POST:
				server.post(url, data, hdrs);
				return true;
			case TYPE_DELETE:
				// no data for DELETE operations
				server.delete(url, hdrs);
				return true;
			case TYPE_GET:
				// no data for GET operations
				server.get(url, hdrs);
				return true;
			}
		}

		return false;
	}
}
