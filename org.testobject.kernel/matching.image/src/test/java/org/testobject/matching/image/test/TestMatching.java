package org.testobject.matching.image.test;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Ignore;
import org.junit.Test;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.matching.image.OpenCVMatcher;
import org.testobject.matching.image.OpenCVMatcher.ScanMatch;
import org.testobject.matching.image.test.TestData.Sample;
import org.testobject.matching.image.test.TestData.SearchImage;

import samples.ResourceResolver;

public class TestMatching {	
	
	private static final DecimalFormat df = new DecimalFormat("0.00");

	@Test
	public void calendar1() throws IOException {
		assertTrue(checkSamples(TestData.calendar1()));				
	}
	
	@Test
	public void calendar2() throws IOException {
		assertTrue(checkSamples(TestData.calendar2()));				
	}	

	@Test
	public void gps1() throws IOException {
		assertTrue(checkSamples(TestData.gps1()));				
	}	

	@Test
	public void gps2() throws IOException {
		assertTrue(checkSamples(TestData.gps2()));				
	}	
	
	@Test
	public void gps3() throws IOException {
		assertTrue(checkSamples(TestData.gps3()));				
	}	

	@Test
	public void manager1() throws IOException {
		assertTrue(checkSamples(TestData.manager1()));				
	}		

	@Test
	public void notepad1() throws IOException {
		assertTrue(checkSamples(TestData.notepad1()));				
	}		

	@Test
	public void notepad2() throws IOException {
		assertTrue(checkSamples(TestData.notepad2()));				
	}		

	@Test
	public void settings1() throws IOException {
		assertTrue(checkSamples(TestData.settings1()));				
	}	

	@Test
	public void settings2() throws IOException {
		assertTrue(checkSamples(TestData.settings2()));				
	}		

	@Test
	public void googleSearchButton() throws IOException {
		assertTrue(checkSamples(TestData.googleSearchButton()));				
	}	
	
	@Test
	public void contrast() throws IOException {
		assertTrue(checkSamples(TestData.contrast()));				
	}	

	@Test
	public void bahn() throws IOException {
		assertTrue(checkSamples(TestData.contrast()));				
	}
	
	@Test
	public void bahn2() throws IOException {
		assertTrue(checkSamples(TestData.contrast()));				
	}
	
	@Test
	public void bahn3() throws IOException {
		assertTrue(checkSamples(TestData.bahn3()));				
	}
	
	@Test
	public void bahn4() throws IOException {
		assertTrue(checkSamples(TestData.bahn4()));				
	}
	
	@Test
	public void manager_button() throws IOException {
		assertTrue(checkSamples(TestData.manager_button()));				
	}	

	@Test
	public void manager_button2() throws IOException {
		assertTrue(checkSamples(TestData.manager_button2()));				
	}
	
	@Test
	public void manager_button3() throws IOException {
		assertTrue(checkSamples(TestData.manager_button3()));				
	}	
	
	@Test
	public void false_positive() throws IOException {
		assertTrue(checkSamples(TestData.false_positive()));				
	}		

	@Test
	public void false_positive2() throws IOException {
		assertTrue(checkSamples(TestData.false_positive2()));				
	}	

	@Test
	@Ignore
	public void false_positive_google() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_google()));				
	}	

	@Test
	@Ignore
	public void false_positive_google1() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_google1()));				
	}		
	
	@Test
	public void false_negative_google() throws IOException {
		assertTrue(checkSamples(TestData.false_negative_google()));				
	}	

	@Test
	public void incorrect_position() throws IOException {
		assertTrue(checkSamples(TestData.incorrect_position()));				
	}	
	
	@Test
	public void false_positive_eon() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_eon()));				
	}	

	@Test
	public void false_negative_ok_button() throws IOException {
		assertTrue(checkSamples(TestData.false_negative_ok_button()));				
	}	
	
	@Test
	public void false_negative_pencil() throws IOException {
		assertTrue(checkSamples(TestData.false_negative_pencil()));				
	}	

	@Test
	public void false_negative_back_button() throws IOException {
		assertTrue(checkSamples(TestData.false_negative_back_button()));				
	}	
	
	@Test
	public void false_positive_cancel_button() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_cancel_button()));				
	}		

	@Test
	public void false_negative_press() throws IOException {
		assertTrue(checkSamples(TestData.false_negative_press()));				
	}	

	@Test
	public void wrong_match_never_button() throws IOException {
		assertTrue(checkSamples(TestData.wrong_match_never_button()));				
	}

	@Test
	public void low_probability_suche() throws IOException {
		assertTrue(checkSamples(TestData.low_probability_suche()));				
	}	

	@Test
	public void false_positive_bahn() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_bahn()));				
	}	

	@Test
	public void false_positive_bahn2() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_bahn2()));				
	}	
	
	@Ignore
	@Test
	public void false_negative_eon() throws IOException {
		assertTrue(checkSamples(TestData.false_negative_eon()));				
	}

	@Test
	public void false_negative_generated() throws IOException {
		assertTrue(checkSamples(TestData.false_negative_generated()));				
	}	

	@Test
	public void wrong_position_calculator() throws IOException {
		assertTrue(checkSamples(TestData.wrong_position_calculator()));				
	}	

	@Ignore
	@Test
	public void wrong_match_ich_bin_kunde() throws IOException {
		assertTrue(checkSamples(TestData.wrong_match_ich_bin_kunde()));				
	}
	
	@Ignore
	@Test
	public void false_positive_hotel() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_hotel()));				
	}	

	@Ignore
	@Test
	public void false_positive_hotel_search_button() throws IOException {
		assertTrue(checkSamples(TestData.false_positive_hotel_search_button()));				
	}	
	
	private boolean checkSamples(List<Sample> samples) throws IOException {

		for (int i = 0; i < samples.size(); i++) {		
			Sample sample = samples.get(i);

			for (SearchImage searchImage : sample.searchImages) {			
				
				Image.Int template = null;
				if (sample.recordImageExists()) {					
					Image.Int recordImage = ImageUtil.Convert.toImage(ImageIO.read(ResourceResolver.getResource(sample.recordImage)));
					template = ImageUtil.Cut.crop(recordImage, sample.templateRectangle); 
				} else {	
					template = ImageUtil.Convert.toImage(ImageIO.read(ResourceResolver.getResource(sample.template)));
				}
				
				BufferedImage searchBufferedImage = ImageIO.read(ResourceResolver.getResource(searchImage.image));			
				List<ScanMatch> matches = OpenCVMatcher.findMatches(ImageUtil.Convert.toImage(searchBufferedImage), template); 				
				
				assertTrue("Probability is bigger than max alowed " + matches.get(0).probability + " > " + searchImage.maxProbability
						+ " " + matches.get(0).scale + " " + sample.template + " " + searchImage.image, 
						matches.get(0).probability <= searchImage.maxProbability);
				
				if (searchImage.maxProbability >= searchImage.minProbability) {					
					assertTrue("Probability is lower than min allowed " + matches.get(0).probability + " < " + searchImage.minProbability
							+ " " + matches.get(0).scale + " " + sample.template + " " + searchImage.image, 
							matches.get(0).probability > searchImage.minProbability);
				}
				
				
				if (searchImage.hasMatch() == false) {
					return true;
				}
				
				if (TestUtil.areClose(searchImage.template, matches.get(0).result, 10)) {
					System.out.println("SUCCESS: " + " Probability: " + matches.get(0).probability + " Scale: " + matches.get(0).scale);
				} else {
					System.err.println("FAIL: ");
					System.err.println("Expected: " + searchImage.template);
					System.err.println("Got: " + matches.get(0).result);
					System.err.println("Scale: " + df.format(matches.get(0).scale));					
					System.err.println(sample.template);
					System.err.println(searchImage.image);
					
					for (ScanMatch match : matches) {
						System.err.println(match);
					}
					
					return false;
				}
			}
		}
		
		return true;
	}
}
