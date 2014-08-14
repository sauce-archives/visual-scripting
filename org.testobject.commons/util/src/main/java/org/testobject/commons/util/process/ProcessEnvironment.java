package org.testobject.commons.util.process;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * 
 * @author enijkamp
 *
 */
public class ProcessEnvironment {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setEnv(Map<String, String> newEnv) {
		try {
			{
				Field theEnvironmentField = getEnvField();
				Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
				env.putAll(newEnv);
			}
			{
				Field theCaseInsensitiveEnvironmentField = getCaseInsensitiveEnvField();
				Map<String, String> env = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
				env.putAll(newEnv);
			}
		} catch (NoSuchFieldException e) {
			try {
				Class[] classes = Collections.class.getDeclaredClasses();
				Map<String, String> env = System.getenv();
				for (Class cl : classes) {
					if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
						Field field = cl.getDeclaredField("m");
						field.setAccessible(true);
						Object obj = field.get(env);
						Map<String, String> processEnv = (Map<String, String>) obj;
						processEnv.clear();
						processEnv.putAll(newEnv);
					}
				}
			} catch (Exception e2) {
				throw new RuntimeException(e2);
			}
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
	}

	private static Field getEnvField() throws ClassNotFoundException, NoSuchFieldException, SecurityException {
		Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
		theEnvironmentField.setAccessible(true);

		return theEnvironmentField;
	}

	private static Field getCaseInsensitiveEnvField() throws ClassNotFoundException, NoSuchFieldException, SecurityException {
		Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
		theEnvironmentField.setAccessible(true);

		return theEnvironmentField;
	}
}
