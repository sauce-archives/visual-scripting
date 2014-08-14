package org.testobject.commons.util.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author enijkamp
 *
 */
public class Configuration {
	
	private static final Log log = LogFactory.getLog(Configuration.class);
	
	public static final File APP = new File(System.getProperty("user.home") + File.separator + ".testobject" + File.separator
			+ "configuration.properties");
	public static final File POOL = new File(System.getProperty("user.home") + File.separator + ".testobject_devices" + File.separator
			+ "configuration.properties");
	public static final File MONITORING = new File(System.getProperty("user.home") + File.separator + ".testobject_monitoring" + File.separator
			+ "configuration.properties");
	
	public static Properties load(File file) {
		CompositeConfiguration config = new CompositeConfiguration();
		try {
			config.addConfiguration(new EnvironmentConfiguration());
			config.addConfiguration(new PropertiesConfiguration(file));
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}

		try{
			Properties properties = ConfigurationConverter.getProperties(config);
			properties.store(new FileOutputStream(new File(file.getParentFile(), file.getName() + ".computed")), "computed list of properties");
			StringBuilder b = new StringBuilder("computed configuration file: ").append("\n");
			for (Entry<Object, Object> entry : properties.entrySet()) {
				b.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
			}
			log.info(b.toString());
			
			return properties;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
}
