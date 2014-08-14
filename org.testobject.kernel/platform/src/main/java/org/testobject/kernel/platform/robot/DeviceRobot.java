package org.testobject.kernel.platform.robot;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.testobject.commons.events.Orientation;
import org.testobject.kernel.platform.robot.AppDescriptor.AndroidAppDescriptor;
import org.testobject.kernel.platform.robot.AppInstance.NativeAppInstance;

import com.google.common.collect.Lists;

/**
 * 
 * @author enijkamp
 *
 */
public interface DeviceRobot extends Closeable {
	
	interface HasOrientationCallback {
		
		void updateOrientation(Orientation orientation);
		
	}

	class TestResult {

		public enum Status {
			UNDEFINED, OK, ERROR, FAILURE
		}

		private final String testClass;
		private final String testMethod;

		private Status status = Status.OK;
		private long startTime;
		private long duration;
		private String stackTrace;
		private List<String> screenshots;
		
		@JsonCreator
		public TestResult(
				@JsonProperty("testClass") String testClass, 
				@JsonProperty("testMethod") String testMethod, 
				@JsonProperty("status") Status status,
				@JsonProperty("startTime") long startTime,
				@JsonProperty("duration") long duration,
				@JsonProperty("stackTrace") String stackTrace)
		{
			this.testClass = testClass;
			this.testMethod = testMethod;
			this.status = status;
			this.startTime = startTime;
			this.duration = duration;
			this.stackTrace = stackTrace;
			this.screenshots = Lists.newLinkedList();
		}
		
		public TestResult(String testClass, String testMethod) {
			this.testClass = testClass;
			this.testMethod = testMethod;
			this.screenshots = Lists.newLinkedList();
		}

		public String getTestClass() {
			return testClass;
		}

		public String getTestMethod() {
			return testMethod;
		}

		public TestResult start() {
			this.startTime = System.currentTimeMillis();

			return this;
		}

		public long getStartTime() {
			return startTime;
		}

		public void setStatus(Status status) {
			this.status = status;
		}

		public Status getStatus() {
			return status;
		}

		public void setStackTrace(String stackTrace) {
			this.stackTrace = stackTrace;
		}

		public String getStackTrace() {
			return stackTrace;
		}

		public void finish() {
			this.duration = System.currentTimeMillis() - startTime;
		}

		public long getDuration() {
			return duration;
		}

		public List<String> getScreenshots() {
			return screenshots;
		}

		public void addScreenshot(String path) {
			screenshots.add(path);
		}
	}

	AppDescriptor install(Path appFile);

	boolean uninstall(Path appFile);

	boolean uninstall(AppDescriptor appDesc);

	AppInstance launch(AppDescriptor appDesc);

	AppDescriptor getAppDescriptor(Path appFile);

	void kill(AppInstance appInstance);

	void reset(AppDescriptor appDescriptor);

	void setNetworkSpeed(NetworkSpeed networkSpeed);

	void setGpsLocation(double latitude, double longitude, double elevation);

	void setGpsLocation(double latitude, double longitude, double elevation, double speed, double bearing);

	void setOrientation(Orientation orientation);

	void callReceive(String phoneNumber);

	void callHangup(String phoneNumber);

	void smsReceive(String phoneNumber, String message);

	void setLocale(String locale);

	void addGoogleAccount();

	void executeShellCommand(String shellCommand);

	void pushFile(String sourcePath, String destinationPath);

	ExerciserMonkeyResponse executeMonkeyCommand(String packageName, int eventCount, int throttle, int seed);

	Orientation getOrientation();

	BufferedImage getScreenshot();

	BufferedImage getScreenshot(String path);

	BufferedImage getFullScreenshot();

	boolean isNetworkEnabled();

	void enableNetwork();

	void disableNetwork();

	void close();

	Collection<TestResult> runTests(String testPackage, List<String> instrumentationTestList, String testRunner);

	class Factory {
		public static DeviceRobot mock() {
			return new DeviceRobot() {

				Orientation orientation = Orientation.PORTRAIT;

				@Override
				public AppDescriptor install(Path appFile) {
					return new AndroidAppDescriptor();
				}

				@Override
				public boolean uninstall(Path appFile) {
					return false;
				}

				@Override
				public boolean uninstall(AppDescriptor appDesc) {
					return false;
				}

				@Override
				public AppInstance launch(AppDescriptor appDesc) {
					return new NativeAppInstance("foo");
				}

				@Override
				public void kill(AppInstance appInstance) {}

				@Override
				public void reset(AppDescriptor appDesc) {}

				@Override
				public void setNetworkSpeed(NetworkSpeed networkSpeed) {}

				@Override
				public void setGpsLocation(double longitude, double latitude, double elevation) {}

				@Override
				public void setOrientation(Orientation orientation) {
					this.orientation = orientation;
				}

				@Override
				public Orientation getOrientation() {
					return orientation;
				}

				@Override
				public BufferedImage getScreenshot() {
					return new BufferedImage(10, 10, 0);
				}

				@Override
				public BufferedImage getFullScreenshot() {
					return getScreenshot();
				}

				public AppDescriptor getAppDescriptor(Path appFile) {
					return new AndroidAppDescriptor();
				}

				@Override
				public void callReceive(String phoneNumber) {}

				@Override
				public void callHangup(String phoneNumber) {}

				@Override
				public void smsReceive(String phoneNumber, String message) {}

				@Override
				public void executeShellCommand(String shellCommand) {}

				@Override
				public ExerciserMonkeyResponse executeMonkeyCommand(String packageName, int eventCount, int throttle, int seed) {
					return new ExerciserMonkeyResponse(true, "");
				}

				@Override
				public void setGpsLocation(double latitude, double longitude, double elevation, double speed, double bearing) {

				}

				@Override
				public Collection<TestResult> runTests(String testPackage, List<String> instrumentationTestList, String testRunner) {

					return null;
				}
				
				@Override
				public boolean isNetworkEnabled() {
					return true;
				}

				@Override
				public void enableNetwork() {}

				@Override
				public void disableNetwork() {}

				@Override
				public void setLocale(String locale) {}

				@Override
				public void pushFile(String sourcePath, String destinationPath) {}

				@Override
				public BufferedImage getScreenshot(String path) {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public void addGoogleAccount() {
					// TODO Auto-generated method stub

				}

				@Override
				public void close() {}

			};
		}
	}

}
