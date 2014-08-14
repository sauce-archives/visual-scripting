package org.testobject.kernel.imgproc.classifier;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.classifier.TestUtils.classify;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.plot.Visualizer;
import org.testobject.kernel.imgproc.plot.VisualizerUtil;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.ocr.FontRenderer;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeLoader;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.blob.Group;
import org.testobject.kernel.imgproc.blob.GroupBuilder;
import org.testobject.kernel.imgproc.classifier.Classes.Widget;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;

/**
 * 
 * @author enijkamp
 * 
 */
@RunWith(value = Parameterized.class)
public class TextCharClassifierTest {

	private final static boolean DEBUG = Debug.toDebugMode(false);

	private static final Object[][] freetypes = {
			{ FreeTypeLoader.loadRewrite(FreeTypeLoader.FT_2_4_2) },
			//{ FreeTypeLoader.loadRewrite(FT_2_4_4) },
			//{ FreeTypeLoader.loadRewrite(FT_2_4_8) },
	};

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(freetypes);
	}

	private final FreeTypeLoader.Instance instance;

	public TextCharClassifierTest(FreeTypeLoader.Instance instance) {
		this.instance = instance;
	}

	@BeforeClass
	public static void beforeClass() {
		FreeTypeLoader.Instance instance = FreeTypeLoader.loadRewrite();
		assertThat(instance.version.equals(FreeTypeLoader.FT_2_4_2) || instance.version.equals(FreeTypeLoader.FT_2_4_4) || instance.version.equals(FreeTypeLoader.FT_2_4_8), is(true));
	}

	@Test
	public void testStatisticsCancel_Ca() throws IOException {

		// compute
		final File font = toFont("Roboto-Bold.ttf");
		final char[] chars = { 'C', 'a' };
		final int size = 12;
		final int blob = 3;
		// debug
		renderChars(font, chars, size);

		// go
		TextCharClassifier.Statistics stats = computeStatistics(toTwitterPath("Cancel.png"), font, chars, size, blob);

		// debug
		printStats(stats);

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_8)) {
			// coverage: 0.84210527
			// matches:
			// [char=a, size=12, x=12, dist=76]
			// [char=C, size=12, x=1, dist=168]

			assertThat(stats.coverage > 0.84, is(true));
			assertThat(stats.candidates.size() >= 2, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('a'));
				assertThat(cand.x, is(12));
				assertThat(cand.distance < 80, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('C'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 170, is(true));
			}
		}

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_4)) {

			// coverage: 0.8947368
			// matches:
			// [char=C, size=12, x=1, dist=145]
			// [char=a, size=12, x=12, dist=172]

			assertThat(stats.coverage > 0.89f, is(true));
			assertThat(stats.candidates.size() >= 2, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('C'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 150, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('a'));
				assertThat(cand.x, is(12));
				assertThat(cand.distance < 180, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_2)) {
			// coverage: 0.84210527
			// matches:
			// [char=a, size=12, x=12, dist=77]
			// [char=C, size=12, x=1, dist=180]

			assertThat(stats.coverage > 0.84f, is(true));
			assertThat(stats.candidates.size() >= 2, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('a'));
				assertThat(cand.x, is(12));
				assertThat(cand.distance < 80, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('C'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 190, is(true));
			}
		}
	}

	@Test
	public void testStatisticsCancel_nc() throws IOException {

		// compute
		final File font = toFont("Roboto-Bold.ttf");
		final char[] chars = { 'n', 'c' };
		final int size = 12;
		final int blob = 5;
		// debug
		renderChars(font, chars, size);

		// go
		TextCharClassifier.Statistics stats = computeStatistics(toTwitterPath("Cancel.png"), font, chars, size, blob);

		// debug
		printStats(stats);

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_8)) {
			// coverage: 0.9444444
			// matches:
			// [char=n, size=12, x=1, dist=103]
			// [char=c, size=12, x=10, dist=178]

			assertThat(stats.coverage > 0.94f, is(true));
			assertThat(stats.candidates.size() >= 2, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('n'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 110, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('c'));
				assertThat(cand.x, is(10));
				assertThat(cand.distance < 180, is(true));
			}
		}

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_4)) {
			// coverage: 0.8333333
			// matches:
			// [char=n, x= 1, dist=258]
			// [char=c, x=10, dist=415]

			assertThat(stats.coverage > 0.8f, is(true));
			assertThat(stats.candidates.size() >= 2, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('n'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 260, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('c'));
				assertThat(cand.x, is(10));
				assertThat(cand.distance < 420, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_2)) {
			// coverage: 0.9444444
			// matches:
			// [char=n, size=12, x=1, dist=103]
			// [char=c, size=12, x=10, dist=177]
			// [char=c, size=12, x=0, dist=2200]

			assertThat(stats.coverage > 0.83f, is(true));
			assertThat(stats.candidates.size() >= 2, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('n'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 110, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('c'));
				assertThat(cand.x, is(10));
				assertThat(cand.distance < 200, is(true));
			}
		}
	}

	@Test
	public void testStatisticsCancel_e() throws IOException {

		// compute
		final File font = toFont("Roboto-Bold.ttf");
		final char[] chars = { 'e' };
		final int size = 12;
		final int blob = 4;
		// debug
		renderChars(font, chars, size);

		// go
		TextCharClassifier.Statistics stats = computeStatistics(toTwitterPath("Cancel.png"), font, chars, size, blob);

		// debug
		printStats(stats);

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_8)) {
			// coverage: 0.875
			// matches:
			// [char=e, size=12, x=0, dist=738]

			assertThat(stats.coverage > 0.85f, is(true));
			assertThat(stats.candidates.size(), is(1));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(0));
				assertThat(cand.distance < 850, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_4)) {
			// coverage: 0.75
			// matches:
			// [char=e, size=12, x=1, dist=108]
			// [char=e, size=12, x=2, dist=1491]

			assertThat(stats.coverage > 0.7f, is(true));
			assertThat(stats.candidates.size() >= 1, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 120, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_2)) {
			// coverage: 0.875
			// matches:
			// [char=e, size=12, x=0, dist=747]

			assertThat(stats.coverage > 0.85f, is(true));
			assertThat(stats.candidates.size(), is(1));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(0));
				assertThat(cand.distance < 800, is(true));
			}
		}
	}

	@Test
	public void testStatisticsTest_t() throws IOException {

		// compute
		final File font = toFont("Roboto-Regular.ttf");
		final char[] chars = { 't' };
		final int size = 16;
		final int blob = 3;

		// debug
		renderChars(font, chars, size);

		// go
		TextCharClassifier.Statistics stats = computeStatistics(toTwitterPath("Test.png"), font, chars, size, blob);

		// debug
		printStats(stats);

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_8)) {
			// coverage: 0.71428573
			// matches:
			// [char=t, size=16, x=1, dist=1086]
			// [char=t, size=16, x=2, dist=3425]

			assertThat(stats.coverage > 0.7f, is(true));
			assertThat(stats.candidates.size() >= 1, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('t'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 1100, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_4)) {
			// coverage: 0.71428573
			// matches:
			// [char=t, size=16, x=1, dist=1908]

			assertThat(stats.coverage > 0.7f, is(true));
			assertThat(stats.candidates.size() >= 1, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('t'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 2000, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_2)) {
			// coverage: 0.71428573
			// matches:
			// [char=t, size=16, x=1, dist=1105]
			// [char=t, size=16, x=2, dist=3425]

			assertThat(stats.coverage > 0.7f, is(true));
			assertThat(stats.candidates.size() >= 1, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('t'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 1200, is(true));
			}
		}
	}

	@Test
	public void testStatisticsTweet() throws IOException {

		// compute
		final File font = toFont("Roboto-Bold.ttf");
		final char[] chars = { 'T', 'w', 'e', 't' };
		final int size = 12;
		final int blob = 3;
		TextCharClassifier.Statistics stats = computeStatistics(toTwitterPath("Tweet.png"), font, chars, size, blob);

		// debug
		printStats(stats);

		// assert
		{
			assertThat(stats.inverted, is(true));
		}

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_8)) {
			// coverage: 0.80851066
			// matches:
			// [char=e, size=12, x=33, dist=550]
			// [char=t, size=12, x=42, dist=594]
			// [char=w, size=12, x=12, dist=690]
			// [char=e, size=12, x=15, dist=1559]

			assertThat(stats.coverage > 0.8f, is(true));
			assertThat(stats.candidates.size() >= 4, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(33));
				assertThat(cand.distance < 560, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('t'));
				assertThat(cand.x, is(42));
				assertThat(cand.distance < 600, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('w'));
				assertThat(cand.x, is(12));
				assertThat(cand.distance < 700, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(24));
				assertThat(cand.distance < 800, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_4)) {
			// coverage: 0.82978725
			// matches:
			// [char=e, size=12, x=34, dist=488]
			// [char=t, size=12, x=42, dist=645]
			// [char=e, size=12, x=25, dist=677]
			// [char=w, size=12, x=12, dist=690]

			assertThat(stats.coverage > 0.8f, is(true));
			assertThat(stats.candidates.size() >= 4, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(34));
				assertThat(cand.distance < 500, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('t'));
				assertThat(cand.x, is(42));
				assertThat(cand.distance < 650, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(25));
				assertThat(cand.distance < 680, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('w'));
				assertThat(cand.x, is(12));
				assertThat(cand.distance < 700, is(true));
			}
		}

		if (instance.version.equals(FreeTypeLoader.FT_2_4_2)) {
			// coverage: 0.80851066
			// matches:
			// [char=e, size=12, x=33, dist=555]
			// [char=t, size=12, x=42, dist=582]
			// [char=w, size=12, x=12, dist=690]
			// [char=e, size=12, x=15, dist=1559]
			assertThat(stats.coverage > 0.8f, is(true));
			assertThat(stats.candidates.size() >= 4, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(33));
				assertThat(cand.distance < 560, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('t'));
				assertThat(cand.x, is(42));
				assertThat(cand.distance < 600, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('w'));
				assertThat(cand.x, is(12));
				assertThat(cand.distance < 700, is(true));
			}
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('e'));
				assertThat(cand.x, is(24));
				assertThat(cand.distance < 800, is(true));
			}
		}
	}

	@Test
	public void testStatisticsStatusbar_5() throws IOException {

		// compute
		final File font = toFont("Roboto-Regular.ttf");
		final char[] chars = { '5' };
		final int size = 16;
		final int blob = 2;
		TextCharClassifier.Statistics stats = computeStatistics("android/4_0_3/classifier/text/positives/statusbar/5.png", font, chars, size, blob);

		// debug
		printStats(stats);

		// assert
		{
			// coverage: 0.8181818
			// [char=5, size=16, x=1, dist=1751]

			assertThat(stats.coverage > 0.8f, is(true));
			assertThat(stats.candidates.size() >= 1, is(true));
			{
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is('5'));
				assertThat(cand.x, is(1));
				assertThat(cand.distance < 1800, is(true));
			}
		}
	}

	@Test
	public void testStatisticsBird() throws IOException {

		// compute
		final File font = toFont("Roboto-Bold.ttf");
		final char[] chars = { 'i', 'l', 'I' };
		final int size = 12;
		final int blob = 2;
		TextCharClassifier.Statistics stats = computeStatistics("android/4_0_3/classifier/text/negatives/twitter/bird.png", font, chars, size, blob);

		// debug
		renderChars(font, chars, size);
		printStats(stats);

		// assert
		{
			assertThat(stats.coverage < .2f, is(true));
		}
	}

	@Test
	public void testStatistics20Apr_2() throws IOException {
		// input
		final String image = toTwitterPath("20Apr.png");

		// render
		final String font = "Roboto-Regular.ttf";
		final char[] chars = { '2' };
		final int size = 13;
		final int blob = 2;

		// result
		final double coverage = 0.6d;
		Match[] matches = {
				new Match('2', 1, 1200)
		};

		// test
		testMatches(image, font, chars, size, blob, coverage, matches);
	}

	@Test
	public void testStatistics20Apr_0() throws IOException {
		// input
		final String image = toTwitterPath("20Apr.png");

		// render
		final String font = "Roboto-Regular.ttf";
		final char[] chars = { '0' };
		final int size = 13;
		final int blob = 3;

		// result
		final double coverage = 0.7d;
		Match[] matches = {
				new Match('0', 1, 1700)
		};

		// test
		testMatches(image, font, chars, size, blob, coverage, matches);
	}

	@Test
	public void testStatistics20Apr_A() throws IOException {
		// input
		final String image = toTwitterPath("20Apr.png");

		// render
		final String font = "Roboto-Regular.ttf";
		final char[] chars = { 'A' };
		final int size = 13;
		final int blob = 6;

		// result
		final double coverage = 0.6d;
		Match[] matches = {
				new Match('A', 2, 650)
		};

		// test
		testMatches(image, font, chars, size, blob, coverage, matches);
	}

	@Test
	public void testStatistics20Apr_p() throws IOException {
		// input
		final String image = toTwitterPath("20Apr.png");

		// render
		final String font = "Roboto-Regular.ttf";
		final char[] chars = { 'p' };
		final int size = 13;
		final int blob = 4;

		// result
		final double coverage = 0.7d;
		Match[] matches = {
				new Match('p', 1, 1410)
		};

		// test
		testMatches(image, font, chars, size, blob, coverage, matches);
	}

	@Test
	public void testStatistics20Apr_r() throws IOException {
		// input
		final String image = toTwitterPath("20Apr.png");

		// render
		final String font = "Roboto-Regular.ttf";
		final char[] chars = { 'r' };
		final int size = 13;
		final int blob = 5;

		// result
		final double coverage = 0.6d;
		Match[] matches = {
				new Match('r', 1, 750)
		};

		// test
		testMatches(image, font, chars, size, blob, coverage, matches);
	}
	
	@Test
	public void testStatisticsDiscard_D() throws IOException {
		// input
		final String image = toTwitterPath("Discard.png");

		// render
		final String font = "Roboto-Regular.ttf";
		final char[] chars = { 'D' };
		final int size = 14;
		final int blob = 2;
		
		// result
		final double coverage = 0.72d;
		Match[] matches = {
				new Match('D', 0, 1300)
		};

		// test
		testMatches(image, font, chars, size, blob, coverage, matches);
	}

	private static class Match {
		public final char chr;
		public final int x;
		public final double dist;

		public Match(char chr, int x, double dist) {
			this.chr = chr;
			this.x = x;
			this.dist = dist;
		}
	}

	private void testMatches(String image, String font, char[] chars, int size, int blob, double coverage, Match[] matches)
			throws IOException {
		// debug
		renderChars(toFont(font), chars, size);

		// go
		TextCharClassifier.Statistics stats = computeStatistics(image, toFont(font), chars, size, blob);

		// debug
		printStats(stats);

		// assert
		if (instance.version.equals(FreeTypeLoader.FT_2_4_2)) {

			assertThat(stats.coverage >= coverage, is(true));
			assertThat(stats.candidates.size() >= matches.length, is(true));
			for (Match match : matches) {
				TextCharClassifier.Candidate cand = stats.candidates.poll();
				assertThat(cand.chr, is(match.chr));
				assertThat(cand.x, is(match.x));
				assertThat(cand.distance < match.dist, is(true));
			}
		}
	}

	private TextCharClassifier.Statistics computeStatistics(String image, File font, char[] chars, int size, int blob) throws IOException {
		// train
		List<TextCharClassifier.Sample> trainSamples = new LinkedList<>();
		{
			FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(instance.freetype, 160, 240, FT2Library.FT_LCD_FILTER_LIGHT);

			for (char chr : chars) {
				TextCharClassifier.Histogram trainHist = TextCharClassifier.generateHistogram(renderer, font, size, chr);
				trainSamples.add(new TextCharClassifier.Sample(chr, size, font.getName(), trainHist));
			}
		}

		// test
		Image.Int testImage = ImageUtil.read(image);
		Blob[] blobs = new GraphBlobBuilder(testImage.w, testImage.h).build(testImage);
		return TextCharClassifier.computeStatistiscs(trainSamples, testImage, blobs[blob]);
	}

	@Test
	public void testPositiveTwitterScreenshots() throws Throwable {
		// classify
		Classifier[] before = {};

		// positives
		TestUtils.testPositives("android/4_0_3/classifier/text/positives/twitter", before, new TextCharClassifier(), DEBUG);
	}

	@Test
	public void testNegativeTwitterScreenshots() throws Throwable {
		// classify
		Classifier[] before = {};

		// positives
		TestUtils.testNegatives("android/4_0_3/classifier/text/negatives/twitter", before, new TextCharClassifier(), DEBUG);
	}

	@Test
	public void testPositiveStatusBarScreenshots() throws Throwable {
		// classify
		Classifier[] before = {};

		// positives
		TestUtils.testPositives("android/4_0_3/classifier/text/positives/statusbar", before, new TextCharClassifier(), DEBUG);
	}

	@Test
	public void testTweetInverse() throws Throwable {
		// classify
		Classifier[] before = {};

		// positives
		TestUtils.Result<Widget> result = TestUtils.classify(ImageUtil.read(toTwitterPath("Tweet.png")), before,
                new TextCharClassifier(), DEBUG);

		// assert
		Assert.assertThat(result.widgets.size(), is(1));
	}

	@Test
	public void testTest() throws Throwable {
		// classify
		Classifier[] before = {};

		// positives
		TestUtils.Result<Widget> result = TestUtils.classify(ImageUtil.read(toTwitterPath("Test.png")), before,
                new TextCharClassifier(), DEBUG);

		// assert
		Assert.assertThat(result.widgets.size(), is(4));
	}

	public static void main(String[] args) throws Throwable {
		// classify
		{
			BufferedImage buf = readImage("android/4_0_3/android/screenshots/twitter/tweet_screen.png");

			showBlobHierarchy(buf);

			showResults(buf, classifyText(buf));
		}

		// groups
		{
			Image.Int buf = ImageUtil.toImage(readImage(toTwitterPath("Tweet.png")));

			Blob[] blobs = new GraphBlobBuilder(buf.w, buf.h).build(buf);
			GroupBuilder<Blob> groupBuilder = new GroupBuilder<Blob>();

			VisualizerUtil.show(BlobUtils.drawHierarchy(blobs));

			BufferedImage buffer = new BufferedImage(buf.w, buf.h, BufferedImage.TYPE_INT_RGB);
			for (Blob blob : blobs[0].children) {
				drawGroup(groupBuilder, blob, buffer.getGraphics());
			}
			VisualizerUtil.show("groups", buffer);
		}

		System.in.read();
	}

	private void renderChars(File fontFile, char[] chars, int fontSize) {
		if (DEBUG) {
			FontRenderer renderer = new FreeTypeGrayscaleFontRenderer(instance.freetype, 160, 240, FT2Library.FT_LCD_FILTER_LIGHT);

			if (DEBUG) {
				for (char chr : chars)
					VisualizerUtil.show("train char '" + chr + "'", renderer.drawChar(fontFile, fontSize, chr), 20f);
			}
		}
	}

	private static void printStats(TextCharClassifier.Statistics stats) {
		if (DEBUG) {
			VisualizerUtil.show("histogram", TextCharClassifier.drawHistogram(stats.histogram));
			System.out.println("coverage: " + stats.coverage);
			System.out.println("matches:");
			for (TextCharClassifier.Candidate match : stats.candidates) {
				System.out.println("   " + match);
			}
		}
	}

	private static TestUtils.Result<Classes.TextChar> classifyText(BufferedImage bufferedImage) throws Throwable {
		Classifier[] before = { new GroupClassifier() };
		return TestUtils.classify(ImageUtil.toImage(bufferedImage), before, new TextCharClassifier(), false);
	}

	private static void showResults(BufferedImage bufferedImage, final TestUtils.Result<Classes.TextChar> result) {
		if (DEBUG) {
			VisualizerUtil.show("texts", VisualizerUtil.toRenderable(bufferedImage), new Visualizer.Renderable() {
				@Override
				public void render(Visualizer.Graphics graphics) {
					for (TestUtils.Result.BlobMatch text : result.widgets) {
						graphics.setColor(Color.RED);
						Rectangle bbox = text.blob.bbox;
						graphics.drawRect(bbox.x, bbox.y, (int) bbox.getWidth(), (int) bbox.getHeight());
					}
				}
			});
		}
	}

	private static void showBlobHierarchy(BufferedImage bufferedImage) {
		if (DEBUG) {
			VisualizerUtil.show(BlobUtils.drawHierarchy(new GraphBlobBuilder(bufferedImage.getWidth(), bufferedImage.getHeight())
					.build(ImageUtil.toImage(bufferedImage))));
		}
	}

	private static BufferedImage readImage(String file) throws IOException {
		return ImageIO.read(ClassLoader.getSystemResourceAsStream(file));
	}

	private static final File toFont(String font) {
		return FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/" + font);
	}

	private static void drawGroup(GroupBuilder<Blob> groupBuilder, Blob blob, java.awt.Graphics g) {
		List<Group<Blob>> groups = groupBuilder.buildGroups(blob.children, 6, 6);
		if (groups.isEmpty() == false) {
			for (Group<Blob> group : groups) {
				g.drawRect(group.getBoundingBox().x, group.getBoundingBox().y, group.getBoundingBox().width, group.getBoundingBox().height);
			}
		}
		for (Blob child : blob.children) {
			drawGroup(groupBuilder, child, g);
		}
	}

	private static String toTwitterPath(String file) {
		return "android/4_0_3/classifier/text/positives/twitter/" + file;
	}
}
