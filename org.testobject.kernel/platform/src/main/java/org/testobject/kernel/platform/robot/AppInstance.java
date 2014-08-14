package org.testobject.kernel.platform.robot;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "objectType")
@JsonSubTypes({

		@JsonSubTypes.Type(value = AppInstance.WebAppInstance.class),
		@JsonSubTypes.Type(value = AppInstance.NativeAppInstance.class)
})
public interface AppInstance {

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "objectType")
	public static class WebAppInstance implements AppInstance {
		private String browserProcess;

		public WebAppInstance() {}

		public WebAppInstance(String browserProcess) {
			this.browserProcess = browserProcess;
		}

		public String getBrowserProcess() {
			return browserProcess;
		}

		public void setBrowserProcess(String browserProcess) {
			this.browserProcess = browserProcess;
		}

		@Override
		public String toString() {
			return WebAppInstance.class.getName() + " - " + browserProcess;
		}
	}

	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "objectType")
	public static class NativeAppInstance implements AppInstance {

		private String packageName;

		public NativeAppInstance() {}

		public NativeAppInstance(String packageName) {
			this.packageName = packageName;
		}

		public String getPackageName() {
			return packageName;
		}

		public void setPackageName(String packageName) {
			this.packageName = packageName;
		}

		@Override
		public String toString() {
			return NativeAppInstance.class.getName() + " - " + packageName;
		}
	}

}