package org.testobject.matching.image.test;

import java.util.LinkedList;
import java.util.List;

import org.testobject.commons.math.algebra.Rectangle;

public class TestData {

	public static class SearchImage {

		public String image;
		public Rectangle.Int template;
		public double maxProbability = 1.0;
		public double minProbability = 0.85;

		public SearchImage(String image) {
			this.image = image;
		}

		public SearchImage(String image, int x, int y, int width, int height) {
			this.image = image;
			this.template = new Rectangle.Int(x, y, width, height);
		}

		public SearchImage withMaxProbability(double maxProbability) {
			this.maxProbability = maxProbability;
			return this;
		}

		public SearchImage withMinProbability(double minProbability) {
			this.minProbability = minProbability;
			return this;
		}

		public boolean hasMatch() {
			return template != null;
		}
	}

	public static class Sample {

		public String template;
		public List<SearchImage> searchImages = new LinkedList<>();
		public Rectangle.Int templateRectangle;
		public String recordImage;

		public Sample(String query) {
			this.template = query;
		}

		public Sample(String recordImage, Rectangle.Int templateRectangle) {
			this.recordImage = recordImage;
			this.templateRectangle = templateRectangle;
		}

		public Sample add(SearchImage searchImage) {
			searchImages.add(searchImage);
			return this;
		}

		public boolean recordImageExists() {
			return recordImage != null;
		}
	}

	public static List<Sample> calendar1() {
		List<Sample> samples = new LinkedList<>();

		samples.add(
				new Sample("samples/template/calendar/4.1.2/1/q1.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/1/480x800.png", 21, 52, 170, 46))
						.add(new SearchImage("samples/search/calendar/4.1.2/1/768x1280.png", 30, 68, 222, 61))
				//.add(new SearchImage("samples/search/calendar/4.1.2/1/2560x1600.png", 0, 0))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/1/q2.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/1/480x800.png", 272, 274, 94, 30))
						.add(new SearchImage("samples/search/calendar/4.1.2/1/768x1280.png", 443, 360, 137, 44))
						.add(new SearchImage("samples/search/calendar/4.1.2/1/2560x1600.png", 1327, 425, 137, 42))
				);

		// fails to find calendar icon on big resolution - grey area has slightly bigger probability
		samples.add(
				new Sample("samples/template/calendar/4.1.2/1/q3.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/1/480x800.png", 414, 49, 47, 45))
						.add(new SearchImage("samples/search/calendar/4.1.2/1/768x1280.png", 569, 65, 62, 59))
				//	.add(new SearchImage("samples/search/calendar/4.1.2/1/2560x1600.png", 2113, 4, 96, 92))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/1/q4.png")
				//	.add(new SearchImage("samples/search/calendar/4.1.2/1/480x800.png", 0, 0))
				//	.add(new SearchImage("samples/search/calendar/4.1.2/1/768x1280.png", 0, 0))
				//	.add(new SearchImage("samples/search/calendar/4.1.2/1/2560x1600.png", 0, 0))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/1/q5.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/1/480x800.png", 275, 80, 34, 28))
						.add(new SearchImage("samples/search/calendar/4.1.2/1/768x1280.png", 363, 104, 49, 40))
						.add(new SearchImage("samples/search/calendar/4.1.2/1/2560x1600.png", 255, 70, 49, 40))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/1/q6.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/1/480x800.png", 4, 428, 31, 25))
						.add(new SearchImage("samples/search/calendar/4.1.2/1/768x1280.png", 5, 569, 40, 32))
				//	.add(new SearchImage("samples/search/calendar/4.1.2/1/2560x1600.png", 2397, 569))
				);

		return samples;
	}

	public static List<Sample> calendar2() {
		List<Sample> samples = new LinkedList<>();

		samples.add(
				new Sample("samples/template/calendar/4.1.2/2/q1.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/2/480x800.png", 73, 56, 200, 38))

				// when scaling of big image is applied 'General settings' has lower probability then similarly looking grey area						
				//						.add(new SearchImage("samples/search/calendar/4.1.2/2/2560x1600.png", 1042, 174, 261, 50))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/2/q2.png")
				//.add(new SearchImage("samples/search/calendar/4.1.2/2/480x800.png", 381, 184))
				//.add(new SearchImage("samples/search/calendar/4.1.2/2/768x1280.png", 637, 374))
				//.add(new SearchImage("samples/search/calendar/4.1.2/2/2560x1600.png", 2141, 370))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/2/q3.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/2/480x800.png", 17, 47, 54, 53))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/768x1280.png", 22, 62, 71, 70))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/2560x1600.png", 19, 0, 110, 108))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/2/q4.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/2/480x800.png", 343, 61, 130, 27))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/768x1280.png", 578, 78, 183, 39))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/2560x1600.png", 2370, 36, 183, 39))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/2/q5.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/2/480x800.png", 32, 635, 54, 29))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/768x1280.png", 44, 817, 71, 39))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/2560x1600.png", 1146, 927, 71, 39).withMinProbability(0.84))
				);

		samples.add(
				new Sample("samples/template/calendar/4.1.2/2/q6.png")
						.add(new SearchImage("samples/search/calendar/4.1.2/2/480x800.png", 33, 287, 77, 35))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/768x1280.png", 44, 384, 101, 46))
						.add(new SearchImage("samples/search/calendar/4.1.2/2/2560x1600.png", 1146, 510, 101, 46))
				);

		return samples;
	}

	public static List<Sample> gps1() {
		List<Sample> samples = new LinkedList<>();

		samples.add(
				new Sample("samples/template/gps/2.3.3/1/q1.png")
						.add(new SearchImage("samples/search/gps/2.3.3/1/480x800.png", 54, 140, 93, 74))
						.add(new SearchImage("samples/search/gps/2.3.3/1/768x1280.png", 45, 187, 122, 97))
						.add(new SearchImage("samples/search/gps/2.3.3/1/1280x800.png", 56, 100, 65, 52))
						.add(new SearchImage("samples/search/gps/4.1.2/1/480x800.png", 54, 140, 93, 74))
						.add(new SearchImage("samples/search/gps/4.1.2/1/768x1280.png", 45, 187, 122, 97))
						.add(new SearchImage("samples/search/gps/4.1.2/1/2560x1600.png", 113, 153, 122, 97))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/1/q2.png")
						.add(new SearchImage("samples/search/gps/2.3.3/1/480x800.png", 331, 228, 97, 28))
						.add(new SearchImage("samples/search/gps/2.3.3/1/768x1280.png", 412, 303, 127, 37).withMinProbability(0.84))
						.add(new SearchImage("samples/search/gps/2.3.3/1/1280x800.png", 243, 159, 58, 17).withMinProbability(0.84))
						.add(new SearchImage("samples/search/gps/4.1.2/1/2560x1600.png", 479, 269, 127, 37).withMinProbability(0.82))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/1/q3.png")
						.add(new SearchImage("samples/search/gps/2.3.3/1/480x800.png", 44, 595, 80, 65))
						.add(new SearchImage("samples/search/gps/2.3.3/1/768x1280.png", 215, 589, 105, 85))
						.add(new SearchImage("samples/search/gps/2.3.3/1/1280x800.png", 875, 96, 56, 46))
						.add(new SearchImage("samples/search/gps/4.1.2/1/2560x1600.png", 1755, 147, 105, 85))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/1/q5.png")
						.add(new SearchImage("samples/search/gps/2.3.3/1/480x800.png", 205, 532, 71, 30))
						.add(new SearchImage("samples/search/gps/2.3.3/1/768x1280.png", 609, 504, 100, 43))
						.add(new SearchImage("samples/search/gps/2.3.3/1/1280x800.png", 710, 159, 43, 18).withMinProbability(0.83))
						.add(new SearchImage("samples/search/gps/4.1.2/1/2560x1600.png", 1412, 266, 100, 43))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/1/q6.png")
						.add(new SearchImage("samples/search/gps/2.3.3/1/480x800.png", 343, 280, 69, 99))
						.add(new SearchImage("samples/search/gps/2.3.3/1/768x1280.png", 245, 374, 91, 130))
						.add(new SearchImage("samples/search/gps/2.3.3/1/1280x800.png", 523, 91, 49, 70))
						.add(new SearchImage("samples/search/gps/4.1.2/1/2560x1600.png", 1049, 136, 91, 130))
				);

		return samples;
	}

	public static List<Sample> gps2() {
		List<Sample> samples = new LinkedList<>();

		samples.add(
				new Sample("samples/template/gps/2.3.3/2/q1.png")
						.add(new SearchImage("samples/search/gps/2.3.3/2/480x800.png", 20, 52, 47, 45))
						.add(new SearchImage("samples/search/gps/2.3.3/2/768x1280.png", 27, 70, 62, 59))
						.add(new SearchImage("samples/search/gps/2.3.3/2/1280x800.png", 14, 31, 47, 45))
				//	.add(new SearchImage("samples/search/gps/4.1.2/2/2560x1600.png", 29, 14))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/2/q2.png")
						.add(new SearchImage("samples/search/gps/2.3.3/2/480x800.png", 330, 52, 47, 45))
						.add(new SearchImage("samples/search/gps/2.3.3/2/768x1280.png", 456, 70, 62, 59))
						.add(new SearchImage("samples/search/gps/2.3.3/2/1280x800.png", 1103, 38, 33, 32).withMinProbability(0.83))
						.add(new SearchImage("samples/search/gps/4.1.2/2/2560x1600.png", 2080, 28, 62, 59))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/2/q3.png")
						.add(new SearchImage("samples/search/gps/2.3.3/2/480x800.png", 214, 274, 53, 153))
						.add(new SearchImage("samples/search/gps/2.3.3/2/768x1280.png", 343, 407, 86, 246))
						.add(new SearchImage("samples/search/gps/2.3.3/2/1280x800.png", 320, 325, 81, 231))

				// here too big of a scaling factor is needed
				//.add(new SearchImage("samples/search/gps/4.1.2/2/2560x1600.png", 621, 585, 156, 446))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/2/q4.png")
						.add(new SearchImage("samples/search/gps/2.3.3/2/480x800.png", 12, 507, 67, 66))
						.add(new SearchImage("samples/search/gps/2.3.3/2/768x1280.png", 16, 804, 88, 87))
						.add(new SearchImage("samples/search/gps/2.3.3/2/1280x800.png", 3, 748, 47, 46))
						.add(new SearchImage("samples/search/gps/4.1.2/2/2560x1600.png", 8, 1402, 88, 87))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/2/q5.png")
						.add(new SearchImage("samples/search/gps/2.3.3/2/480x800.png", 245, 643, 40, 29))
						.add(new SearchImage("samples/search/gps/2.3.3/2/768x1280.png", 335, 1027, 53, 39))

						// too small scaling is needed
						//	.add(new SearchImage("samples/search/gps/2.3.3/2/1280x800.png", 898, 423, 24, 18))
						.add(new SearchImage("samples/search/gps/4.1.2/2/2560x1600.png", 1788, 770, 57, 42).withMinProbability(0.81))
				);

		samples.add(
				new Sample("samples/template/gps/2.3.3/2/q6.png")
						.add(new SearchImage("samples/search/gps/2.3.3/2/480x800.png", 417, 53, 41, 43))
						.add(new SearchImage("samples/search/gps/2.3.3/2/768x1280.png", 573, 70, 54, 57))
						.add(new SearchImage("samples/search/gps/2.3.3/2/1280x800.png", 1170, 38, 29, 30))
						.add(new SearchImage("samples/search/gps/4.1.2/2/2560x1600.png", 2213, 28, 54, 57).withMinProbability(0.84))
				);

		return samples;
	}

	public static List<Sample> gps3() {
		List<Sample> samples = new LinkedList<>();

		samples.add(
				new Sample("samples/template/gps/2.3.3/1/q1.png")
						.add(new SearchImage("samples/search/gps/2.3.3/1/480x800.png", 54, 140, 93, 74))
						.add(new SearchImage("samples/search/gps/2.3.3/1/768x1280.png", 45, 187, 122, 97))
						.add(new SearchImage("samples/search/gps/2.3.3/1/1280x800.png", 56, 100, 65, 52))
						.add(new SearchImage("samples/search/gps/4.1.2/1/480x800.png", 54, 140, 93, 74))
						.add(new SearchImage("samples/search/gps/4.1.2/1/768x1280.png", 45, 187, 122, 97))
						.add(new SearchImage("samples/search/gps/4.1.2/1/2560x1600.png", 113, 153, 122, 97))
				);

		return samples;
	}

	public static List<Sample> manager1() {
		List<Sample> samples = new LinkedList<>();

		samples.add(
				new Sample("samples/template/manager/2.3.3/1/q1.png")
						.add(new SearchImage("samples/search/manager/2.3.3/1/480x800.png", 256, 112, 68, 70))
						.add(new SearchImage("samples/search/manager/2.3.3/1/768x1280.png", 32, 371, 89, 92))
						.add(new SearchImage("samples/search/manager/2.3.3/1/1280x800.png", 331, 72, 48, 49))
				//	.add(new SearchImage("samples/search/manager/4.1.2/1/2560x1600.png", 0, 0))
				);

		samples.add(
				new Sample("samples/template/manager/2.3.3/1/q4.png")
						.add(new SearchImage("samples/search/manager/2.3.3/1/480x800.png", 24, 117, 63, 61))
						.add(new SearchImage("samples/search/manager/2.3.3/1/768x1280.png", 31, 154, 89, 86))
						.add(new SearchImage("samples/search/manager/2.3.3/1/1280x800.png", 15, 76, 44, 43))
				//.add(new SearchImage("samples/search/manager/4.1.2/1/2560x1600.png", 0, 0))
				);

		samples.add(
				new Sample("samples/template/manager/2.3.3/1/q5.png")
						.add(new SearchImage("samples/search/manager/2.3.3/1/480x800.png", 248, 284, 73, 62))
						.add(new SearchImage("samples/search/manager/2.3.3/1/768x1280.png", 396, 379, 96, 81))
						.add(new SearchImage("samples/search/manager/2.3.3/1/1280x800.png", 486, 76, 51, 44))
						.add(new SearchImage("samples/search/manager/4.1.2/1/2560x1600.png", 647, 154, 103, 88))
				);

		samples.add(
				new Sample("samples/template/manager/2.3.3/1/q6.png")
						.add(new SearchImage("samples/search/manager/2.3.3/1/480x800.png", 261, 198, 116, 33))
						.add(new SearchImage("samples/search/manager/2.3.3/1/768x1280.png", 35, 485, 163, 47).withMinProbability(0.78))
						.add(new SearchImage("samples/search/manager/2.3.3/1/1280x800.png", 335, 131, 81, 23).withMinProbability(0.77))
				//.add(new SearchImage("samples/search/manager/4.1.2/1/2560x1600.png", 0, 0))
				);

		return samples;
	}

	public static List<Sample> notepad1() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/template/notepad/2.3.3/1/q1.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/1/480x800.png", 3, 434, 44, 40))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/768x1280.png", 3, 551, 63, 57))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/1280x800.png", 1, 289, 31, 28))
						.add(new SearchImage("samples/search/notepad/4.1.2/1/2560x1600.png", 3, 526, 63, 57))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/1/q2.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/1/480x800.png", 30, 133, 76, 66))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/768x1280.png", 60, 153, 100, 87).withMinProbability(0.74))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/1280x800.png", 178, 88, 53, 46).withMinProbability(0.72))
						.add(new SearchImage("samples/search/notepad/4.1.2/1/2560x1600.png", 359, 128, 100, 87).withMinProbability(0.81))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/1/q3.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/1/480x800.png", 305, 246, 74, 85))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/768x1280.png", 515, 305, 97, 111))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/1280x800.png", 1002, 163, 52, 60).withMinProbability(0.83))
				//						.add(new SearchImage("samples/search/notepad/4.1.2/1/2560x1600.png", 2008, 280))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/1/q4.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/1/480x800.png", 73, 504, 76, 37))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/768x1280.png", 98, 645, 100, 49))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/1280x800.png", 47, 334, 53, 26))
						.add(new SearchImage("samples/search/notepad/4.1.2/1/2560x1600.png", 98, 622, 92, 45))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/1/q5.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/1/480x800.png", 297, 133, 80, 69))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/768x1280.png", 503, 153, 105, 91))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/1280x800.png", 996, 87, 56, 49))
						.add(new SearchImage("samples/search/notepad/4.1.2/1/2560x1600.png", 1996, 128, 105, 91))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/1/q6.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/1/480x800.png", 175, 213, 53, 28))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/768x1280.png", 297, 258, 70, 37))
						.add(new SearchImage("samples/search/notepad/2.3.3/1/1280x800.png", 596, 140, 37, 20).withMinProbability(0.8))
						.add(new SearchImage("samples/search/notepad/4.1.2/1/2560x1600.png", 1184, 233, 91, 49))
				);
		return samples;
	}

	public static List<Sample> notepad2() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/template/notepad/2.3.3/2/q1.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/2/480x800.png", 5, 67, 40, 35))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/768x1280.png", 6, 86, 57, 50))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/1280x800.png", 3, 51, 28, 25))
						.add(new SearchImage("samples/search/notepad/4.1.2/2/2560x1600.png", 6, 60, 57, 50))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/2/q2.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/2/480x800.png", 89, 224, 67, 37))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/768x1280.png", 118, 297, 88, 49))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/1280x800.png", 58, 156, 47, 26).withMinProbability(0.79))
						.add(new SearchImage("samples/search/notepad/4.1.2/2/2560x1600.png", 117, 271, 88, 49))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/2/q3.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/2/480x800.png", 9, 754, 45, 42))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/768x1280.png", 12, 1220, 59, 55))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/1280x800.png", 5, 769, 32, 30))
						.add(new SearchImage("samples/search/notepad/4.1.2/2/2560x1600.png", 12, 1444, 59, 55))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/2/q4.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/2/480x800.png", 334, 67, 34, 34))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/768x1280.png", 573, 89, 45, 45))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/1280x800.png", 1184, 52, 24, 24).withMinProbability(0.82))
						.add(new SearchImage("samples/search/notepad/4.1.2/2/2560x1600.png", 2365, 63, 45, 45))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/2/q5.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/2/480x800.png", 86, 763, 64, 30))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/768x1280.png", 114, 1229, 84, 40))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/1280x800.png", 56, 774, 45, 21))
						.add(new SearchImage("samples/search/notepad/4.1.2/2/2560x1600.png", 111, 1454, 84, 40))
				);

		samples.add(
				new Sample("samples/template/notepad/2.3.3/2/q6.png")
						.add(new SearchImage("samples/search/notepad/2.3.3/2/480x800.png", 384, 66, 40, 37))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/768x1280.png", 637, 86, 57, 53))
						.add(new SearchImage("samples/search/notepad/2.3.3/2/1280x800.png", 1216, 52, 28, 26))
						.add(new SearchImage("samples/search/notepad/4.1.2/2/2560x1600.png", 2429, 60, 57, 53))
				);
		return samples;
	}

	public static List<Sample> settings1() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/template/settings/2.3.3/1/q1.png")
						.add(new SearchImage("samples/search/settings/2.3.3/1/480x800.png", 10, 106, 47, 41))
						.add(new SearchImage("samples/search/settings/2.3.3/1/768x1280.png", 14, 141, 62, 54))
				//.add(new SearchImage("samples/search/settings/2.3.3/1/1280x800.png", 0, 0))
				);

		samples.add(
				new Sample("samples/template/settings/2.3.3/1/q2.png")
						.add(new SearchImage("samples/search/settings/2.3.3/1/480x800.png", 12, 200, 44, 48))
						.add(new SearchImage("samples/search/settings/2.3.3/1/768x1280.png", 15, 266, 58, 63))
						.add(new SearchImage("samples/search/settings/2.3.3/1/1280x800.png", 7, 133, 31, 34))
				);

		samples.add(
				new Sample("samples/template/settings/2.3.3/1/q3.png")
						.add(new SearchImage("samples/search/settings/2.3.3/1/480x800.png", 13, 488, 44, 48))
						.add(new SearchImage("samples/search/settings/2.3.3/1/768x1280.png", 18, 649, 58, 63))
						.add(new SearchImage("samples/search/settings/2.3.3/1/1280x800.png", 8, 325, 31, 34))
				);

		samples.add(
				new Sample("samples/template/settings/2.3.3/1/q4.png")
						.add(new SearchImage("samples/search/settings/2.3.3/1/480x800.png", 66, 304, 102, 36))
						.add(new SearchImage("samples/search/settings/2.3.3/1/768x1280.png", 84, 401, 144, 51).withMinProbability(0.84))
						.add(new SearchImage("samples/search/settings/2.3.3/1/1280x800.png", 41, 202, 72, 25).withMinProbability(0.76))
				);
		return samples;
	}

	public static List<Sample> settings2() {

		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/template/settings/2.3.3/2/q4.png")
						//	.add(new SearchImage("samples/search/settings/2.3.3/2/480x800.png", 0, 0))
						.add(new SearchImage("samples/search/settings/2.3.3/2/768x1280.png", 26, 810, 240, 49))
						.add(new SearchImage("samples/search/settings/2.3.3/2/1280x800.png", 14, 409, 120, 25).withMinProbability(0.76))
				);

		samples.add(
				new Sample("samples/template/settings/2.3.3/2/q5.png")
						.add(new SearchImage("samples/search/settings/2.3.3/2/480x800.png", 10, 83, 83, 26).withMinProbability(0.74))
						.add(new SearchImage("samples/search/settings/2.3.3/2/768x1280.png", 9, 108, 118, 37))
						.add(new SearchImage("samples/search/settings/2.3.3/2/1280x800.png", 5, 54, 59, 19))
				);

		samples.add(
				new Sample("samples/template/settings/2.3.3/2/q6.png")
						.add(new SearchImage("samples/search/settings/2.3.3/2/480x800.png", 6, 45, 67, 23).withMinProbability(0.72))
						.add(new SearchImage("samples/search/settings/2.3.3/2/768x1280.png", 6, 57, 95, 33))
						.add(new SearchImage("samples/search/settings/2.3.3/2/1280x800.png", 4, 28, 48, 17).withMinProbability(0.71))
				);

		return samples;
	}

	public static List<Sample> googleSearchButton() {
		List<Sample> samples = new LinkedList<>();

		samples.add(
				new Sample("samples/search-button.png")
						.add(new SearchImage("samples/replay.png", 559, 414, 39, 35).withMinProbability(0.84))
				);

		return samples;
	}

	public static List<Sample> bahn() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("android/4_0_3/matching/framebuffers/bahn/record.android_403.png", new Rectangle.Int(85, 470, 136, 23))
						.add(new SearchImage("android/4_0_3/matching/framebuffers/bahn/replay.android_233.png", 85, 478, 136, 23))
				);

		return samples;
	}

	public static List<Sample> bahn2() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("android/4_0_3/matching/framebuffers/bahn/record.android_403.png", new Rectangle.Int(88, 344, 82, 22))
						.add(new SearchImage("android/4_0_3/matching/framebuffers/bahn/replay.android_233.png", 129, 414, 90, 24))
				);

		return samples;
	}

	public static List<Sample> bahn3() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("android/4_0_3/matching/framebuffers/bahn/record.android_403.png", new Rectangle.Int(85, 531, 160, 27))
						.add(new SearchImage("android/4_0_3/matching/framebuffers/bahn/replay.android_233.png", 83, 538, 176, 29))
				);

		return samples;
	}

	public static List<Sample> bahn4() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("android/4_0_3/matching/framebuffers/bahn/record.android_403.png", new Rectangle.Int(87, 659, 109, 23))
						.add(new SearchImage("android/4_0_3/matching/framebuffers/bahn/replay.android_233.png", 86, 666, 119, 25)
								.withMinProbability(0.84))
				);

		return samples;
	}

	public static List<Sample> manager_button() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/manager_black_record.png", new Rectangle.Int(377, 729, 69, 25))
						.add(new SearchImage("samples/manager_black_replay.png", 954, 503, 48, 16).withMinProbability(0.78))
				);

		return samples;
	}

	public static List<Sample> manager_button2() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/manager_black_record.png", new Rectangle.Int(242, 702, 109, 82))
						.add(new SearchImage("samples/manager_black_replay.png", 866, 487, 72, 50))
				);

		return samples;
	}

	public static List<Sample> manager_button3() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/manager_black_record.png", new Rectangle.Int(360, 702, 104, 82))
						.add(new SearchImage("samples/manager_black_replay.png", 945, 486, 69, 55))
				);

		return samples;
	}

	public static List<Sample> false_positive() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positive_record.png", new Rectangle.Int(244, 706, 105, 73))
						.add(new SearchImage("samples/false_positive_replay.png").withMaxProbability(0.77))
				);

		return samples;
	}

	public static List<Sample> false_positive2() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positive_2_record.png", new Rectangle.Int(49, 371, 276, 180))
						.add(new SearchImage("samples/false_positive_2_replay.png").withMaxProbability(0.70))
				);

		return samples;
	}

	public static List<Sample> contrast() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/contrast_record.png", new Rectangle.Int(238, 576, 96, 109))
						.add(new SearchImage("samples/contrast_replay.png", 238, 576, 96, 109))
				);

		return samples;
	}

	public static List<Sample> false_positive_google() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/google_record.png", new Rectangle.Int(145, 412, 98, 23))
						.add(new SearchImage("samples/false_positives/google_replay.png").withMaxProbability(0.7))
				);

		return samples;
	}

	public static List<Sample> false_positive_google1() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/google_record_1.png", new Rectangle.Int(9, 344, 407, 63))
						.add(new SearchImage("samples/false_positives/google_replay_1.png").withMaxProbability(0.7))
				);

		return samples;
	}

	public static List<Sample> false_positive_eon() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/eon_record.png", new Rectangle.Int(219, 439, 47, 30))
						.add(new SearchImage("samples/false_positives/eon_replay.png").withMaxProbability(0.72))
				);

		return samples;
	}

	public static List<Sample> false_negative_google() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_negatives/google_record.png", new Rectangle.Int(21, 392, 108, 28))
						.add(new SearchImage("samples/false_negatives/google_replay.png", 181, 175, 112, 29))
				);

		return samples;
	}

	public static List<Sample> incorrect_position() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/google_position_record.png", new Rectangle.Int(14, 378, 453, 58))
						.add(new SearchImage("samples/google_position_replay.png", 15, 521, 453, 55))
				);

		return samples;
	}

	public static List<Sample> false_negative_ok_button() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_negatives/ok_button_record.png", new Rectangle.Int(244, 445, 179, 80))
						.add(new SearchImage("samples/false_negatives/ok_button_replay.png", 259, 450, 152, 68))
				);

		return samples;
	}

	public static List<Sample> false_negative_pencil() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_negatives/pencil_record.png", new Rectangle.Int(162, 131, 80, 74))
						.add(new SearchImage("samples/false_negatives/pencil_record.png", 162, 131, 80, 74))
				);

		return samples;
	}

	public static List<Sample> false_negative_press() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_negatives/press_record.png", new Rectangle.Int(170, 261, 67, 27))
						.add(new SearchImage("samples/false_negatives/press_replay.png", 170, 261, 67, 27))
				);

		return samples;
	}

	public static List<Sample> false_negative_back_button() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_negatives/back_button_record.png", new Rectangle.Int(95, 718, 73, 31))
						.add(new SearchImage("samples/false_negatives/back_button_replay.png", 97, 496, 50, 21).withMinProbability(0.81))
				);

		return samples;
	}

	public static List<Sample> false_positive_cancel_button() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/cancel_button_record.png", new Rectangle.Int(242, 702, 109, 82))
						.add(new SearchImage("samples/false_positives/cancel_button_replay.png").withMaxProbability(0.74))
				);

		return samples;
	}

	public static List<Sample> wrong_match_never_button() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/wrong_match/never_button_record.png", new Rectangle.Int(20, 475, 145, 80))
						.add(new SearchImage("samples/wrong_match/never_button_replay.png", 20, 475, 145, 80))
				);

		return samples;
	}

	public static List<Sample> low_probability_suche() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/low_probability/suche_record.png", new Rectangle.Int(387, 169, 63, 20))
						.add(new SearchImage("samples/low_probability/suche_replay.png", 565, 205, 57, 17))
				);

		return samples;
	}

	public static List<Sample> false_positive_bahn() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/bahn_record.png", new Rectangle.Int(3, 285, 469, 82))
						.add(new SearchImage("samples/false_positives/bahn_replay.png").withMaxProbability(0.79))
				);

		return samples;
	}

	public static List<Sample> false_positive_bahn2() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/bahn_record.png", new Rectangle.Int(3, 285, 469, 82))
						.add(new SearchImage("samples/false_positives/bahn_replay2.png").withMaxProbability(0.79))
				);

		return samples;
	}

	public static List<Sample> false_negative_eon() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_negatives/eon_record.png", new Rectangle.Int(211, 416, 59, 28))
						.add(new SearchImage("samples/false_negatives/eon_replay.png", 211, 416, 59, 28).withMinProbability(0.8))
				);

		return samples;
	}

	public static List<Sample> false_negative_generated() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_negatives/generated_record.png", new Rectangle.Int(46, 46, 108, 108))
						.add(new SearchImage("samples/false_negatives/generated_replay.png", 46, 46, 108, 108).withMinProbability(0.8))
				);

		return samples;
	}

	public static List<Sample> wrong_position_calculator() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/wrong_match/calculator.png", new Rectangle.Int(111, 532, 119, 138))
						.add(new SearchImage("samples/wrong_match/calculator.png", 111, 532, 119, 138).withMinProbability(0.8))
				);

		return samples;
	}

	public static List<Sample> wrong_match_ich_bin_kunde() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/wrong_match/ich_bin_kunde_record.png", new Rectangle.Int(19, 444, 444, 67))
						.add(new SearchImage("samples/wrong_match/ich_bin_kunde_replay.png", 19, 444, 444, 67).withMinProbability(0.8))
				);

		return samples;
	}

	public static List<Sample> false_positive_hotel() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/select_a_hotel_record.png", new Rectangle.Int(178, 125, 148, 37))
						.add(new SearchImage("samples/false_positives/select_a_hotel_replay.png").withMaxProbability(0.79))
				);

		return samples;
	}

	public static List<Sample> false_positive_hotel_search_button() {
		List<Sample> samples = new LinkedList<>();
		samples.add(
				new Sample("samples/false_positives/hotel_search_button_record.png", new Rectangle.Int(16, 644, 449, 104))
						.add(new SearchImage("samples/false_positives/hotel_search_button_replay.png").withMaxProbability(0.79))
				);

		return samples;
	}
}
