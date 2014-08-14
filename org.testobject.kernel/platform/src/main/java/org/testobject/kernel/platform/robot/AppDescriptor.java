package org.testobject.kernel.platform.robot;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.testobject.kernel.platform.robot.AppDescriptor.AndroidAppDescriptor;
import org.testobject.kernel.platform.robot.AppDescriptor.WebAppDescriptor;

@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AndroidAppDescriptor.class, name = "android"),
		@JsonSubTypes.Type(value = WebAppDescriptor.class, name = "web")
})
public abstract class AppDescriptor extends Descriptor {

	public AppDescriptor() {
		super();
	}

	public AppDescriptor(Map<String, String> configuration) {
		super(configuration);
	}

	public static class AndroidAppDescriptor extends AppDescriptor {

		public interface Keys extends AppDescriptor.Keys {
			String APK_PATH = "ApkPath";
			String LAUNCHABLE_ACTIVITY = "LaunchableActivity";
		}

		public AndroidAppDescriptor() {
			super();
		}

		@JsonCreator
		public AndroidAppDescriptor(@JsonProperty("configuration") Map<String, String> configuration) {
			super(configuration);
		}

		@JsonIgnore
		public String getLaunchableActivity() {
			String value = configuration.get(Keys.LAUNCHABLE_ACTIVITY);
			checkNotNull(AndroidAppDescriptor.Keys.LAUNCHABLE_ACTIVITY, value);
			return value;
		}

		@JsonIgnore
		public String getPackageFilePath() {
			String value = configuration.get(Keys.APK_PATH);
			checkNotNull(Keys.APK_PATH, value);
			return value;
		}

		@Override
		public String toString() {
			return configuration.toString();
		}

	}

	public static class WebAppDescriptor extends AppDescriptor {

		public interface Keys extends AppDescriptor.Keys {
			String URL = "url";
			String BROWSER_ACTIVITY = "browser_activity";
			String BROWSER_PROCESS = "browser_process";
		}

		public WebAppDescriptor() {
			super();
		}

		@JsonCreator
		public WebAppDescriptor(@JsonProperty("configuration") Map<String, String> configuration) {
			super(addName(configuration));
		}

		// FIXME put specific browser here and use code from AndroidDeviceUtility.ANDROID_BROWSER_PACKAGE_NAME (en)
		private static Map<String, String> addName(Map<String, String> configuration) {
			String packageId = "com.android.browser";
			configuration.put(AndroidAppDescriptor.Keys.NAME, packageId);
			return configuration;
		}

		@JsonIgnore
		public String getUrl() {
			String value = configuration.get(Keys.URL);
			checkNotNull(Keys.URL, value);
			return value;
		}

		@JsonIgnore
		public String getBrowserActivity() {
			String activity = configuration.get(Keys.BROWSER_ACTIVITY);
			return activity != null ? activity : "com.android.browser/.BrowserActivity";
		}

		@JsonIgnore
		public String getBrowserProcess() {
			String process = configuration.get(Keys.BROWSER_PROCESS);
			return process != null ? process : "com.android.browser";
		}

	}

}
