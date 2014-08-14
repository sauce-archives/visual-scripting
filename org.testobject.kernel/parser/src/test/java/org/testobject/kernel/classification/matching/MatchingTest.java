package org.testobject.kernel.classification.matching;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.junit.Test;
import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;

/**
 * 
 * @author enijkamp
 *
 */
public class MatchingTest {
	
	private static final boolean debug = Debug.toDebugMode(false);
	
	@Test
	public void position() {
		Point.Int point1 = Point.Int.from(33, 487);
		Point.Int point2 = Point.Int.from(32, 542);
		
		System.out.println(l2(point1, point2));
		
		double match = Matcher.Util.position(800, 400, point1, point2);
		
		assertThat(match, is(closeTo(0.9, 1e-2)));
	}
	
	@Test
	public void fingerprintZero() {
		BufferedImage buf1 = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
		BufferedImage buf2 = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
		
		{
			Graphics graphics = buf1.getGraphics();
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, 100, 30);
		}
		
		{
			Graphics graphics = buf2.getGraphics();
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, 100, 30);
		}
		
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        Image.Int image2 = ImageUtil.Convert.toImageInt(buf2);

        ImageFingerprint f1 = new ImageFingerprint(ImageUtil.toSquare(image1), 0xf2, 0xf1, 0xf0);
        ImageFingerprint f2 = new ImageFingerprint(ImageUtil.toSquare(image2), 0xf2, 0xf1, 0xf0);
        
        System.out.println(ImageFingerprint.lumaDistanceL1(f1, f2));
        
        double match = Matcher.Util.fingerprints(f1, f2);
        System.out.println(match);
        
        assertThat(match, is(closeTo(0, 1e-2)));
	}
	
	@Test
	public void fingerprintMismatch1() throws IOException {
        Image.Int image1 = readImage("fingerprints/lesen.png");
        Image.Int image2 = readImage("fingerprints/srpski.png");

        ImageFingerprint f1 = new ImageFingerprint(ImageUtil.toSquare(image1), 0xf2, 0xf1, 0xf0);
        ImageFingerprint f2 = new ImageFingerprint(ImageUtil.toSquare(image2), 0xf2, 0xf1, 0xf0);
        
        System.out.println(ImageFingerprint.lumaDistanceL1(f1, f2));
        
        double match = Matcher.Util.fingerprints(f1, f2);
        System.out.println(match);
        
        assertThat(match, is(closeTo(0.39, 1e-2)));
	}
	
	@Test
	public void fingerprintMismatch2() throws IOException {
        Image.Int image1 = readImage("fingerprints/wortherkunft.png");
        Image.Int image2 = readImage("fingerprints/einzelnachweis.png");

        ImageFingerprint f1 = new ImageFingerprint(ImageUtil.toSquare(image1), 0xf2, 0xf1, 0xf0);
        ImageFingerprint f2 = new ImageFingerprint(ImageUtil.toSquare(image2), 0xf2, 0xf1, 0xf0);
        
        if(debug) {
        	VisualizerUtil.show("luma1", f1.restoreLuma());
        	VisualizerUtil.show("luma2", f2.restoreLuma());
        }
        
        System.out.println(ImageFingerprint.lumaDistanceL1(f1, f2));
        
        double match = Matcher.Util.fingerprints(f1, f2);
        System.out.println(match);
        
        assertThat(match, is(closeTo(0.69, 1e-2)));
	}
	
	@Test
	public void fingerprintFuzzy1() throws IOException {
        Image.Int image1 = readImage("fingerprints/wortherkunft.png");
        Image.Int image2 = readImage("fingerprints/wortherkunft_fuzzy.png");

        ImageFingerprint f1 = new ImageFingerprint(ImageUtil.toSquare(image1), 0xf2, 0xf1, 0xf0);
        ImageFingerprint f2 = new ImageFingerprint(ImageUtil.toSquare(image2), 0xf2, 0xf1, 0xf0);
        
        if(debug) {
        	VisualizerUtil.show("luma1", f1.restoreLuma());
        	VisualizerUtil.show("luma2", f2.restoreLuma());
        }
        
        System.out.println(ImageFingerprint.lumaDistanceL1(f1, f2));
        
        double match = Matcher.Util.fingerprints(f1, f2);
        System.out.println(match);
        
        assertThat(match, is(closeTo(0.83, 1e-2)));
	}
	
	@Test
	public void fingerprintFuzzy2() throws IOException {
        Image.Int image1 = readImage("fingerprints/spaeter_1.png");
        Image.Int image2 = readImage("fingerprints/spaeter_2.png");

        ImageFingerprint f1 = new ImageFingerprint(ImageUtil.toSquare(image1), 0xf2, 0xf1, 0xf0);
        ImageFingerprint f2 = new ImageFingerprint(ImageUtil.toSquare(image2), 0xf2, 0xf1, 0xf0);
        
        if(debug) {
        	VisualizerUtil.show("luma1", f1.restoreLuma(), 10f);
        	VisualizerUtil.show("luma2", f2.restoreLuma(), 10f);
        }
        
        System.out.println(ImageFingerprint.lumaDistanceL1(f1, f2));
        
        double match = Matcher.Util.fingerprints(f1, f2);
        System.out.println(match);
        
        assertThat(match, is(closeTo(0.78, 1e-2)));
	}
	
	
	@Test
	public void fingerprintFuzzy3() throws IOException {
        Image.Int image1 = readImage("fingerprints/wortherkunft.png");
        Image.Int image2 = readImage("fingerprints/weblinks.png");

        ImageFingerprint f1 = new ImageFingerprint(ImageUtil.toSquare(image1), 0xf2, 0xf1, 0xf0);
        ImageFingerprint f2 = new ImageFingerprint(ImageUtil.toSquare(image2), 0xf2, 0xf1, 0xf0);
        
        if(debug) {
        	VisualizerUtil.show("luma1", f1.restoreLuma(), 10f);
        	VisualizerUtil.show("luma2", f2.restoreLuma(), 10f);
        }
        
        System.out.println(ImageFingerprint.lumaDistanceL1(f1, f2));
        
        double match = Matcher.Util.fingerprints(f1, f2);
        System.out.println(match);
        
        assertThat(match, is(closeTo(0.62, 1e-2)));
	}

	@Test
	public void positive() {
		
		Point.Int point1 = Point.Int.from(0, 0);
		
		for(int i = 0; i < 100; i++) {
			Point.Int point2 = Point.Int.from(i, i);
			double match = Matcher.Util.position(800, 400, point1, point2);
			
			System.out.printf("%12f -> %12f\n", l2(point1, point2), match);
			
			assertThat(match, is(greaterThan(0.0001)));
		}
	}
	
	private static double l2(Point.Int p1, Point.Int p2) {
		return Math.sqrt(((p1.x - p2.x) * (p1.x - p2.x)) + ((p1.y - p2.y) * (p1.y - p2.y)));
	}
	
	private static org.testobject.commons.util.image.Image.Int readImage(String file) throws IOException {
		return ImageUtil.Read.read(FileUtil.readFileFromClassPath(file));
	}
}
