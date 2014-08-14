package org.testobject.commons.apk;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.testobject.commons.apk.ApkParser.InstrumentationTest;
import org.testobject.commons.util.config.Configuration;
import org.testobject.commons.util.config.Constants;
import org.testobject.commons.util.io.FileUtil;

import com.google.common.collect.Lists;

public class ApkParserTest {

	private static final Properties config = Configuration.load(Configuration.APP);
	private static final String workFolder = config.getProperty(Constants.application_work_folder);
	private static Path resourcePath = Paths.get(workFolder, "org.testobject.file.toolkit");

	@Test
	public void testParser() throws CorruptApkException {

		ApkData aral = new ApkParser(FileUtil.toFileFromSystem("aral.apk"), false, resourcePath).parse();

		BufferedImage aralIcon = aral.getIcon();
		assertThat(aralIcon.getHeight(), is(72));
		assertThat(aralIcon.getWidth(), is(72));
		assertThat(aral.getAppName(), is("Aral"));
		assertThat(aral.getAppVersion(), is("1.0"));
		assertThat(aral.getAppPackage(), is("com.aral.aral"));
		assertThat(aral.getLaunchableActivity(), is("com.aral.aral.ui.MainActivity"));
		assertThat(aral.getMinSdkLevel(), is(8));
		assertThat(aral.getTargetSdkLevel(), is(15));
		assertFalse(aral.getNativeCode().hasArm());
		assertFalse(aral.getNativeCode().hasIntel());

		ApkData twitter = new ApkParser(FileUtil.toFileFromSystem("twitter.apk"), false, resourcePath).parse();

		BufferedImage twitterIcon = twitter.getIcon();
		assertThat(twitterIcon.getHeight(), is(72));
		assertThat(twitterIcon.getWidth(), is(72));
		assertThat(twitter.getAppName(), is("Twitter"));
		assertThat(twitter.getAppVersion(), is("3.3.1"));
		assertThat(twitter.getAppPackage(), is("com.twitter.android"));
		assertThat(twitter.getLaunchableActivity(), is("com.twitter.android.StartActivity"));
		assertThat(twitter.getMinSdkLevel(), is(7));
		assertThat(twitter.getTargetSdkLevel(), is(11));
		assertFalse(twitter.getNativeCode().hasArm());
		assertFalse(twitter.getNativeCode().hasIntel());

		ApkData noTargetSdk = new ApkParser(FileUtil.toFileFromSystem("noTargetSdk.apk"), false, resourcePath).parse();
		assertThat(noTargetSdk.getMinSdkLevel(), is(7));
		assertThat(noTargetSdk.getTargetSdkLevel(), is(7));
	}

	@Test
	public void testUnescapeAppNameCharacters() throws CorruptApkException {

		ApkData bosch = new ApkParser(FileUtil.toFileFromSystem("bosch.apk"), false, resourcePath).parse();
		assertThat(bosch.getAppName(), is("GLM measure&document"));
	}

	@Test
	public void testNativeCode() throws CorruptApkException {
		ApkNativeCode aral = new ApkParser(FileUtil.toFileFromSystem("aral.apk"), false, resourcePath).parse().getNativeCode();
		assertFalse(aral.hasArm());
		assertFalse(aral.hasIntel());

		ApkNativeCode chrome = new ApkParser(FileUtil.toFileFromSystem("chrome.apk"), false, resourcePath).parse().getNativeCode();
		assertTrue(chrome.hasArm());
		assertFalse(chrome.hasIntel());

		ApkNativeCode opera = new ApkParser(FileUtil.toFileFromSystem("opera.apk"), false, resourcePath).parse().getNativeCode();
		assertTrue(opera.hasArm());
		assertFalse(opera.hasIntel());
	}

	@Test(expected = CorruptApkException.class)
	public void testCorruptApkExceptionWithNonZip() throws CorruptApkException {
		new ApkParser(FileUtil.toFileFromSystem("empty.txt"), false, resourcePath).parse();
	}

	@Test(expected = CorruptApkException.class)
	public void testCorruptApkExceptionWithZip() throws CorruptApkException {
		new ApkParser(FileUtil.toFileFromSystem("nonApk.apk"), false, resourcePath).parse();
	}

	@Test
	public void noIconTest() throws CorruptApkException {
		new ApkParser(FileUtil.toFileFromSystem("noIconTest.apk"), false, resourcePath).parse();
	}

	@Test
	public void noMinSdkTest() throws CorruptApkException {
		ApkData noMinSdk = new ApkParser(FileUtil.toFileFromSystem("noMinSdk.apk"), false, resourcePath).parse();
		assertThat(noMinSdk.getMinSdkLevel(), is(1));
		assertThat(noMinSdk.getTargetSdkLevel(), is(1));

		ApkData noMinSdk2 = new ApkParser(FileUtil.toFileFromSystem("noMinSdk2.apk"), false, resourcePath).parse();
		assertThat(noMinSdk2.getMinSdkLevel(), is(1));
		assertThat(noMinSdk2.getTargetSdkLevel(), is(11));
	}

	@Test
	public void testSmaliTestParser() throws CorruptApkException {
		ApkData apkData = new ApkParser(FileUtil.toFileFromSystem("NotePadTest.apk"), true, resourcePath).parse();

		List<InstrumentationTest> tests = apkData.getInstrumentationTests();
		List<String> testFullNames = Lists.newLinkedList();

		for (InstrumentationTest instrumentationTest : tests) {
			String fullName = instrumentationTest.getClassName() + "#" + instrumentationTest.getTestName();
			testFullNames.add(fullName);
		}

		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest#testEditNote"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest#testAddNote"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest#testRemoveNote"));

		apkData = new ApkParser(FileUtil.toFileFromSystem("mainapplicationtest.apk"), true, resourcePath).parse();

		tests = apkData.getInstrumentationTests();
		testFullNames = Lists.newLinkedList();
		
		for (InstrumentationTest instrumentationTest : tests) {
			String fullName = instrumentationTest.getClassName() + "#" + instrumentationTest.getTestName();
			testFullNames.add(fullName);
		}

		assertTrue(testFullNames.contains("de.codecentric.mondiagradle.app.test.MainActivityTest1#testRun"));
		assertTrue(testFullNames.contains("de.codecentric.mondiagradle.app.test.MainActivityTest#testRun"));
		
		try {
			apkData = new ApkParser(FileUtil.toFileFromSystem("ladybug.apk.apk"), true, resourcePath).parse();
		} catch (RuntimeException e) {
			assertTrue(testFullNames.contains("de.codecentric.mondiagradle.app.test.MainActivityTest#testRun"));
		}


	}

	@Test
	public void testSmaliTestParserMultiClass() throws CorruptApkException {
		ApkData apkData = new ApkParser(FileUtil.toFileFromSystem("NotePadTest_multi_class.apk"), true, resourcePath).parse();
		List<InstrumentationTest> tests = apkData.getInstrumentationTests();
		List<String> testFullNames = Lists.newLinkedList();

		for (InstrumentationTest instrumentationTest : tests) {
			String fullName = instrumentationTest.getClassName() + "#" + instrumentationTest.getTestName();
			testFullNames.add(fullName);
			System.out.println(fullName);
		}

		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest#testEditNote"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest#testAddNote"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest#testRemoveNote"));

		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest1#testEditNote1"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest1#testAddNote1"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest1#testRemoveNote1"));

		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest2#testEditNote2"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest2#testAddNote2"));
		assertTrue(testFullNames.contains("com.robotium.test.NotePadTest2#testRemoveNote2"));

		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest#testEditNote"));
		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest#testAddNote"));
		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest#testRemoveNote"));

		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest1#testEditNote1"));
		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest1#testAddNote1"));
		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest1#testRemoveNote1"));

		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest2#testEditNote2"));
		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest2#testAddNote2"));
		assertTrue(testFullNames.contains("com.inbar.test.NotePadTest2#testRemoveNote2"));
	}

	@Test
	public void testParseTestRunner() throws CorruptApkException {
		ApkData apkData = new ApkParser(FileUtil.toFileFromSystem("NotePadTest.apk"), true, resourcePath).parse();
		String testRunnerClass = apkData.getTestRunnerClass();
		assertTrue(testRunnerClass.equals("android.test.InstrumentationTestRunner"));
	}

	//	public static void main(String... args) throws CorruptApkException {
	//		Path resourcePath = Paths.get(Configuration.load().getProperty(Constants.application_work_folder), "org.testobject.file.toolkit");
	//		ApkData apkData = new ApkParser(new File("/Users/tal/Desktop/auto-away.apk"), false, resourcePath).parse();
	//		System.out.println(apkData.getAppVersion());
	//	}

}
