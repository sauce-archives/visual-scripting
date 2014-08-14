package org.testobject.kernel.platform.robot;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;

public class Descriptor {

	public interface Keys {
		public String NAME = "name";
		public String DESCRIPTION = "description";
	}

	protected final Map<String, String> configuration;

	public Descriptor() {
		this(new HashMap<String, String>());
	}

	public Descriptor(Map<String, String> configuration) {
		this.configuration = configuration;
	}

	@JsonIgnore
	public String getName() {
		String name = configuration.get(Keys.NAME);
		checkNotNull(Keys.NAME, name);
		return name;
	}

	@JsonIgnore
	public String getDescription() {
		String name = configuration.get(Keys.DESCRIPTION);
		checkNotNull(Keys.DESCRIPTION, name);
		return name;
	}

	public Map<String, String> getConfiguration() {
		return configuration;
	}
	
	public boolean containsKey(String key) {
		return configuration.containsKey(key);
	}

	protected static void checkNotNull(String key, String value) {
		if (value == null) {
			throw new IllegalStateException(String.format("%s must not be null", key));
		}
	}

	@Override
	public String toString() {
		return configuration.toString();
	}

	@Override
	public int hashCode() {
		return (configuration == null) ? 0 : configuration.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Descriptor other = (Descriptor) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		return true;
	}
}
