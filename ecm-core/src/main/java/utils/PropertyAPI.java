package utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * The Class PropertyAPI.
 */
public class PropertyAPI {
	
	/** The file path. */
	private final static String filePath = "settings.properties";
	
	/** The instance. */
	private static PropertyAPI instance = new PropertyAPI();
	
	/** The props. */
	private Properties props;

	/**
	 * Instantiates a new property api.
	 */
	private PropertyAPI() {
		props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			props.load(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the single instance of PropertyAPI.
	 *
	 * @return single instance of PropertyAPI
	 */
	public static PropertyAPI getInstance() {
		return instance;
	}

	/**
	 * Gets the property.
	 *
	 * @param key the key
	 * @return the property
	 */
	public String getProperty(String key) {
		String value = props.getProperty(key);
		return value;
	}
}
