package org.testobject.matching.image.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.matching.image.OpenCVMatcher;
import org.testobject.matching.image.OpenCVMatcher.ScanMatch;
import org.testobject.matching.image.test.TestData.Sample;

import samples.ResourceResolver;

public class MatchingDebugUtil {
	
	private static List<Sample> samples = TestData.calendar2();
	//private static List<Sample> samples = TestData.wrong_position_calculator();
	//private static List<Sample> samples = TestData.false_negative_google();
	
	//private static List<Sample> samples = TestData.false_negative_ok_button();
	//private static List<Sample> samples = TestData.false_positive_eon();
	//private static List<Sample> samples = TestData.false_positive_google1();
	//private static List<Sample> samples = TestData.false_positive();
	
	private static int SAMPLE_ID = 4;
	private static int IMAGE_ID = 0;
	
	public static void main(String[] args) throws IOException {		
		debug();
	}
		
	private static void debug() {
		
		try {
						
			Sample sample = samples.get(SAMPLE_ID);
			
			Image.Int template = null;
			if (sample.recordImageExists()) {					
				Image.Int recordImage = ImageUtil.Convert.toImage(ImageIO.read(ResourceResolver.getResource(sample.recordImage)));
				template = ImageUtil.Cut.crop(recordImage, sample.templateRectangle); 
			} else {	
				template = ImageUtil.Convert.toImage(ImageIO.read(ResourceResolver.getResource(sample.template)));
			}
					
			BufferedImage searchImage = ImageIO.read(ResourceResolver.getResource(sample.searchImages.get(IMAGE_ID).image));			
			List<ScanMatch> matches = OpenCVMatcher.findMatches(ImageUtil.Convert.toImage(searchImage), template); 		

			for (int i = 0; i < 1; i++) {
				System.out.println(matches.get(i));
				
				System.out.println(matches.get(i).result);
				
				Graphics2D graph = searchImage.createGraphics();
				graph.setColor(Color.RED);
				Rectangle.Int result = matches.get(i).result;
				graph.draw(new java.awt.Rectangle(result.x, result.y, result.w, result.h));
				graph.dispose();				
			}
			
			File templateTemp = File.createTempFile("query-image", "");
			ImageIO.write(ImageUtil.Convert.toBufferedImage(template), "png", templateTemp);
			ImageShow.displayImage(templateTemp.getAbsolutePath());			
			
			File temp = File.createTempFile("debug-image", "");
			ImageIO.write(searchImage, "png", temp);
			ImageShow.displayImage(temp.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}