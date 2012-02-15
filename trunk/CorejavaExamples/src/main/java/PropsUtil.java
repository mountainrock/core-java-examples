package util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropsUtil {

	static Map<String, PropertiesConfiguration> configs= new HashMap<String, PropertiesConfiguration>();
	

	public static final Logger LOG = LoggerFactory.getLogger(PropsUtil.class);

	public static void init(final String propFile) {
		try {
			PropertiesConfiguration config = null;
			if (config != null) {
				LOG.warn("Properties file already initialized  from - " + config.getBasePath());
				return;
			}
			config = new PropertiesConfiguration(propFile);
			LOG.debug("Loading properties file " + config.getBasePath());
			print();
			config.setDelimiterParsingDisabled(true);
			LOG.debug("Properties loaded..");
			configs.put(propFile, config);
		} catch (final ConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static void print() {
		LOG.debug("Loading the following search properties :");
		
		for (final Iterator<Map.Entry<String, PropertiesConfiguration>> it = configs.entrySet().iterator(); it.hasNext();) {
			final Map.Entry<String, PropertiesConfiguration> entry = it.next();
			final String fileName = entry.getKey();
			final PropertiesConfiguration config = entry.getValue();
			LOG.debug("------------ Property file : "+ fileName+"-------------");
			// do something with the key and the value  
			for (final Iterator keys = config.getKeys(); keys.hasNext();) {
				final String key = (String) keys.next();
				LOG.debug(key + " = " + config.getProperty(key));
			}
		}
	}

	public static List getList(final String fileName, final String key) {
		LOG.debug("[" + key + "=" + getConfig(fileName).getProperty(key) + "]");
		return getConfig(fileName).getList(key);
	}

	public static PropertiesConfiguration getConfig(final String fileName) {
		return  configs.get(fileName);
	}

	public static String get(final String fileName, final String key) {
		return getConfig(fileName).getString(key) ;
	}

	public static boolean getBoolean(final String fileName, final String key) {
		return getConfig(fileName).getBoolean(key);
	}


}