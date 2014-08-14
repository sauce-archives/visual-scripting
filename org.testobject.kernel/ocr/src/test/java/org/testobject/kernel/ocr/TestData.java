package org.testobject.kernel.ocr;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.testobject.commons.math.algebra.Rectangle;

public class TestData {

	public static class TextMatches {

		private SortedMap<String, List<TextMatch>> matches;

		public TextMatches() {
			matches = new TreeMap<String, List<TextMatch>>();
		}

		public Map<String, List<TextMatch>> getMatches() {
			return matches;
		}

		public TextMatches add(String path, TextMatch match) {

			if (!matches.containsKey(path)) {
				matches.put(path, new LinkedList<TextMatch>());
			}
			matches.get(path).add(match);

			return this;
		}

		public static List<Object[]> toTestData(TextMatches goodOcrMatches) {
			List<Object[]> testData = new LinkedList<>();

			Set<Entry<String, List<TextMatch>>> testMatchesMap = goodOcrMatches.getMatches().entrySet();
			for (Entry<String, List<TextMatch>> entry : testMatchesMap) {
				String path = entry.getKey();
				List<TextMatch> testMatches = entry.getValue();
				for (TextMatch textMatch : testMatches) {
					testData.add(new Object[] { path, textMatch });
				}
			}

			return testData;
		}

	}

	public static class TextMatch {

		private String text;
		private Rectangle.Int region;

		public TextMatch(String text, int x, int y, int w, int h) {
			this.text = text;
			region = new Rectangle.Int(x, y, w, h);
		}

		public String getText() {
			return text;
		}

		public Rectangle.Int getRegion() {
			return region;
		}

		@Override
		public String toString() {
			return Arrays.toString(new Object[] { text, region });
		}
	}

	public static TextMatches getGoodOcrMatches() {

		TextMatches textMatches = new TextMatches();

		textMatches
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("9:35", 419, 4, 56, 29))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("9:35", 419, 4, 56, 29))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("2013", 188, 57, 86, 39))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("26", 425, 63, 26, 23))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("SUN", 30, 112, 41, 23))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("MON", 96, 113, 39, 22))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("24", 197, 113, 25, 25))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("WED", 221, 114, 42, 26))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("THU", 286, 112, 42, 29))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("SAT", 414, 112, 39, 28))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("23", 135, 111, 23, 24))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("TUE", 161, 115, 38, 19))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("25", 261, 111, 25, 24))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("26", 327, 115, 23, 16))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("27", 389, 111, 24, 26))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("8", 14, 135, 21, 22))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("9", 10, 226, 37, 43))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("2", 12, 716, 26, 34))
				.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("AM", 5, 155, 34, 18))

				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("9:35", 690, 5, 69, 42))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("September", 26, 68, 221, 64))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("2013", 253, 73, 111, 57))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("SUN", 61, 148, 52, 32))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("MON", 158, 153, 56, 25))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("TUE", 272, 150, 45, 24))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("WED", 366, 147, 54, 33))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("THU", 469, 149, 52, 30))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("FRI", 589, 150, 38, 27))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("SAT", 682, 148, 49, 31))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("AM", 7, 206, 37, 26))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("22", 110, 151, 37, 26))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("23", 214, 149, 36, 34))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("24", 317, 149, 38, 30))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("25", 421, 151, 39, 29))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("26", 524, 148, 34, 33))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("27", 625, 150, 34, 28))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("28", 729, 149, 35, 26))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("8", 16, 179, 32, 31))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("9", 16, 313, 31, 31))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("10", 7, 441, 36, 33))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("12", 8, 701, 37, 25))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("PM", 6, 723, 39, 23))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("2", 21, 957, 25, 35))
				.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("3", 20, 1084, 26, 36))

				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("AM", 12, 282, 47, 33))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("8", 26, 249, 36, 34))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("9", 28, 371, 48, 64))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("26", 1388, 194, 60, 53))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("1", 2053, 263, 31, 37))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("3", 2325, 500, 53, 56))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("22", 293, 202, 46, 41))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("26", 2328, 441, 51, 42))
				.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("23", 2108, 436, 68, 62))

				.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("COS", 370, 94, 82, 65))
				.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("DELETE", 365, 218, 95, 39))
				.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("sin", 45, 310, 80, 59))
				.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("e", 194, 568, 89, 80))

				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("COS", 407, 65, 147, 108))
				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("tan", 319, 258, 66, 54))

				.add("samples/search/calculator/4.2.2/2/480x800.png", new TextMatch("DELETE", 362, 214, 101, 38))
				.add("samples/search/calculator/4.2.2/2/480x800.png", new TextMatch("9", 264, 307, 69, 69))

				.add("samples/search/calculator/4.2.2/2/800x1280.png", new TextMatch("COS", 185, 261, 61, 44))
				.add("samples/search/calculator/4.2.2/2/800x1280.png", new TextMatch("tan", 308, 260, 83, 46))
				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("9", 321, 544, 53, 65))
				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("8", 177, 542, 71, 74))
				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("6", 318, 675, 62, 76))
				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("5", 188, 676, 52, 86))
				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("4", 52, 678, 58, 82))
				.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("3", 294, 812, 107, 94))
				.add("samples/search/calculator/4.2.2/2/800x1280.png", new TextMatch("e", 330, 368, 54, 55))
				.add("samples/search/calculator/4.2.2/2/800x1280.png", new TextMatch("sin", 55, 263, 57, 44))

				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("GPS", 25, 193, 57, 36))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Essentials", 84, 190, 134, 40))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Notepad", 258, 193, 120, 43))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Android", 258, 360, 106, 36))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Manager", 107, 362, 121, 39))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Super", 24, 358, 82, 48))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Launcher", 257, 521, 145, 49))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Settings", 22, 524, 114, 56))
				.add("samples/search/manager/2.3.3/1/480x800.png", new TextMatch("Contacts", 255, 696, 121, 34))
				.add("samples/search/calculator/4.2.2/2/480x800.png", new TextMatch("4", 24, 426, 75, 92))
				.add("samples/search/calculator/4.2.2/2/480x800.png", new TextMatch("5", 160, 432, 48, 72))

				.add("samples/search/manager/2.3.3/1/768x1280.png", new TextMatch("Super", 411, 258, 102, 53))
				.add("samples/search/manager/2.3.3/1/768x1280.png", new TextMatch("Launcher", 397, 705, 204, 60))
				.add("samples/search/manager/2.3.3/1/768x1280.png", new TextMatch("Storage", 177, 691, 140, 69))
				.add("samples/search/manager/2.3.3/1/768x1280.png", new TextMatch("Bar", 148, 924, 66, 50))
				.add("samples/search/manager/2.3.3/1/768x1280.png", new TextMatch("Google", 406, 921, 132, 63))

				.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("Compass", 60, 36, 82, 33))
				.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("here", 920, 415, 38, 39))
				.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("menu", 1111, 420, 43, 22))
				.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("here", 920, 415, 38, 39))

				.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("The", 1437, 747, 84, 71))
				.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("dashboard", 1519, 771, 147, 41))
				.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("elements", 2367, 750, 125, 97))
				.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("select", 1941, 772, 82, 33))
				.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("the", 2154, 771, 48, 38))
				.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("here", 1845, 766, 61, 45))
				.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("add", 2310, 767, 58, 48))

				.add("samples/search/notepad/2.3.3/2/1280x800.png", new TextMatch("content", 57, 211, 91, 28))

				.add("samples/search/notepad/4.1.2/2/2560x1600.png", new TextMatch("content", 118, 377, 191, 63))
				.add("samples/search/notepad/4.1.2/2/2560x1600.png", new TextMatch("The", 28, 375, 89, 46))
				.add("samples/search/notepad/4.1.2/2/2560x1600.png", new TextMatch("Neigungsmesstechnik", 989, 128, 287, 53))

				.add("samples/search/notepad/2.3.3/2/480x800.png", new TextMatch("Magnolia", 135, 124, 103, 29))
				.add("samples/search/notepad/2.3.3/2/480x800.png", new TextMatch(".com", 111, 151, 51, 24))

				.add("samples/search/notepad/2.3.3/2/768x1280.png", new TextMatch("Priocept", 106, 164, 103, 38))
				.add("samples/search/notepad/2.3.3/2/768x1280.png", new TextMatch("implementer.", 218, 194, 171, 46))
				.add("samples/search/notepad/2.3.3/2/768x1280.png", new TextMatch("partner", 468, 167, 110, 43))
				.add("samples/search/notepad/2.3.3/2/768x1280.png", new TextMatch("Magnolia", 352, 165, 112, 37))
				.add("samples/search/notepad/2.3.3/2/768x1280.png", new TextMatch("and", 104, 193, 58, 35))
				.add("samples/search/notepad/2.3.3/2/768x1280.png", new TextMatch("is", 212, 167, 30, 32))

				.add("samples/search/settings/2.3.3/1/768x1280.png", new TextMatch("Call", 92, 264, 76, 58))
				.add("samples/search/settings/2.3.3/1/768x1280.png", new TextMatch("networks", 306, 141, 189, 57))
				.add("samples/search/settings/2.3.3/1/768x1280.png", new TextMatch("Sound", 84, 383, 151, 74))
				.add("samples/search/settings/2.3.3/1/768x1280.png", new TextMatch("Location", 81, 654, 183, 58))

				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("Location", 41, 327, 95, 32))
				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("security", 154, 326, 98, 37))
				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("Applications", 41, 395, 145, 31))
				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("Storage", 40, 581, 88, 40))
				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("keyboard", 165, 655, 106, 31))
				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("output", 179, 718, 97, 33))
				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("Voice", 36, 712, 67, 37))
				.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("&", 132, 322, 24, 36));

		return textMatches;
	}

	public static TextMatches getBadOcrMatches() {

		TextMatches textMatches = new TextMatches();
		textMatches
// upper lower case
		.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("cos", 370, 94, 82, 65))
// special character
		.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("!", 367, 423, 67, 81))
// single character
		.add("samples/search/settings/2.3.3/1/1280x800.png", new TextMatch("Display", 34, 257, 90, 39))
		.add("samples/search/settings/2.3.3/1/768x1280.png", new TextMatch("Display", 82, 524, 165, 63))
		.add("samples/search/notepad/4.1.2/2/2560x1600.png", new TextMatch("title", 114, 262, 116, 80))
		.add("samples/search/calculator/4.2.2/2/480x800.png", new TextMatch("1", 38, 559, 44, 81))
		.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("ln", 45, 436, 70, 62))
		.add("samples/search/calculator/4.2.2/2/800x1280.png", new TextMatch("ln", 61, 376, 40, 40))
		.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("ln", 48, 365, 71, 60))
		.add("samples/search/calculator/4.2.2/1/480x800.png", new TextMatch("log", 201, 440, 84, 71))
		.add("samples/search/calculator/4.2.2/1/800x1280.png", new TextMatch("log", 187, 378, 60, 37))
		.add("samples/search/calculator/4.2.2/2/800x1280.png", new TextMatch("log", 185, 372, 59, 47))
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("FRI", 357, 115, 32, 18))
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("28", 452, 114, 25, 18))
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("PM", 5, 541, 32, 25))
// additional special car
		.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("8", 26, 233, 38, 59))
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("10", 9, 329, 24, 25))
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("12", 9, 516, 28, 30))
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("1", 18, 622, 20, 28))
//contains a '1' digit
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("11", 5, 430, 31, 27))
		.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("11", 8, 570, 33, 31))
		.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("1", 10, 826, 37, 30))
		.add("samples/search/calendar/4.1.2/1/2560x1600.png", new TextMatch("1", 2185, 507, 51, 49))
		
		
// to small < 20pix
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("22", 72, 111, 23, 24))
		.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("3G", 606, 3, 30, 25))
		.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("elements", 1196, 418, 72, 32))
		.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("dashboard", 763, 423, 73, 20))
		.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("Is", 829, 424, 20, 21))
		.add("samples/search/gps/2.3.3/2/1280x800.png", new TextMatch("empty", 848, 418, 48, 28))
		.add("samples/search/gps/4.1.2/2/2560x1600.png", new TextMatch("empty", 1693, 773, 93, 42))
		.add("samples/search/notepad/2.3.3/2/480x800.png", new TextMatch("Priocept", 27, 122, 96, 32))
		.add("samples/search/notepad/2.3.3/2/480x800.png", new TextMatch("priocept", 32, 144, 78, 31))
		.add("samples/search/calendar/4.1.2/1/480x800.png", new TextMatch("3G", 356, 4, 25, 16))
// more than one word
		.add("samples/search/settings/2.3.3/1/768x1280.png", new TextMatch("securiet", 303, 651, 187, 73))
// noise
		.add("samples/search/notepad/2.3.3/2/1280x800.png", new TextMatch("title", 52, 153, 77, 37))
		.add("samples/search/notepad/2.3.3/2/1280x800.png", new TextMatch("The", 12, 211, 47, 31))
		.add("samples/search/notepad/4.1.2/2/2560x1600.png", new TextMatch("The", 19, 382, 100, 62))
		.add("samples/search/notepad/4.1.2/2/2560x1600.png", new TextMatch("The", 26, 265, 92, 61))
// special noise
		.add("samples/search/calculator/4.2.2/2/480x800.png", new TextMatch("85", 409, 93, 67, 71))
		.add("samples/search/calculator/4.2.2/2/800x1280.png", new TextMatch("85", 449, 68, 92, 95))
		.add("samples/search/calendar/4.1.2/1/768x1280.png", new TextMatch("26", 572, 74, 59, 53));

		return textMatches;
	}

	public static List<String> allImages() {

		List<String> paths = new LinkedList<String>();

		//		paths.add("search/calendar/4.1.2/1/480x800.png");
		//		paths.add("search/calendar/4.1.2/1/768x1280.png");
		//		paths.add("search/calendar/4.1.2/1/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/1/480x800.png");
		//		paths.add("search/calendar/4.1.2/1/768x1280.png");
		//		paths.add("search/calendar/4.1.2/1/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/1/480x800.png");
		//		paths.add("search/calendar/4.1.2/1/768x1280.png");
		//		paths.add("search/calendar/4.1.2/1/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/1/480x800.png");
		//		paths.add("search/calendar/4.1.2/1/768x1280.png");
		//		paths.add("search/calendar/4.1.2/1/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/1/480x800.png");
		//		paths.add("search/calendar/4.1.2/1/768x1280.png");
		//		paths.add("search/calendar/4.1.2/1/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/1/480x800.png");
		//		paths.add("search/calendar/4.1.2/1/768x1280.png");
		//		paths.add("search/calendar/4.1.2/1/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/2/480x800.png");
		//		paths.add("search/calendar/4.1.2/2/480x800.png");
		//		paths.add("search/calendar/4.1.2/2/768x1280.png");
		//		paths.add("search/calendar/4.1.2/2/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/2/480x800.png");
		//		paths.add("search/calendar/4.1.2/2/768x1280.png");
		//		paths.add("search/calendar/4.1.2/2/2560x1600.png");
		//		paths.add("search/calendar/4.1.2/2/480x800.png");
		//		paths.add("search/calendar/4.1.2/2/768x1280.png");
		//		paths.add("search/calendar/4.1.2/2/2560x1600.png");
		//		paths.add("template/calendar/4.1.2/2/q5.png");
		//		paths.add("search/calendar/4.1.2/2/480x800.png");
		//		paths.add("search/calendar/4.1.2/2/768x1280.png");
		//		paths.add("search/calendar/4.1.2/2/2560x1600.png");
		//		paths.add("template/calendar/4.1.2/2/q6.png");
		//		paths.add("search/calendar/4.1.2/2/480x800.png");
		//		paths.add("search/calendar/4.1.2/2/768x1280.png");
		//		paths.add("search/calendar/4.1.2/2/2560x1600.png");
		//		paths.add("search/gps/2.3.3/1/480x800.png");
		//		paths.add("search/gps/2.3.3/1/768x1280.png");
		//		paths.add("search/gps/2.3.3/1/1280x800.png");
		//		paths.add("search/gps/4.1.2/1/480x800.png");
		//		paths.add("search/gps/4.1.2/1/768x1280.png");
		//		paths.add("search/gps/4.1.2/1/2560x1600.png");
		//		paths.add("search/gps/2.3.3/1/480x800.png");
		//		paths.add("search/gps/2.3.3/1/768x1280.png");
		//		paths.add("search/gps/2.3.3/1/1280x800.png");
		//		paths.add("search/gps/4.1.2/1/2560x1600.png");
		//		paths.add("search/gps/2.3.3/1/480x800.png");
		//		paths.add("search/gps/2.3.3/1/768x1280.png");
		//		paths.add("search/gps/2.3.3/1/1280x800.png");
		//		paths.add("search/gps/4.1.2/1/2560x1600.png");
		//		paths.add("search/gps/2.3.3/1/480x800.png");
		//		paths.add("search/gps/2.3.3/1/768x1280.png");
		//		paths.add("search/gps/2.3.3/1/1280x800.png");
		//		paths.add("search/gps/4.1.2/1/2560x1600.png");
		//		paths.add("search/gps/2.3.3/1/480x800.png");
		//		paths.add("search/gps/2.3.3/1/768x1280.png");
		//		paths.add("search/gps/2.3.3/1/1280x800.png");
		//		paths.add("search/gps/4.1.2/1/2560x1600.png");
		//		paths.add("search/gps/2.3.3/2/480x800.png");
		//		paths.add("search/gps/2.3.3/2/768x1280.png");
		//		paths.add("search/gps/2.3.3/2/1280x800.png");
		//		paths.add("search/gps/4.1.2/2/2560x1600.png");
		//		paths.add("search/gps/2.3.3/2/480x800.png");
		//		paths.add("search/gps/2.3.3/2/768x1280.png");
		//		paths.add("search/gps/2.3.3/2/1280x800.png");
		//		paths.add("search/gps/4.1.2/2/2560x1600.png");
		//		paths.add("search/gps/2.3.3/2/480x800.png");
		//		paths.add("search/gps/2.3.3/2/768x1280.png");
		//		paths.add("search/gps/2.3.3/2/1280x800.png");
		//		paths.add("search/gps/4.1.2/2/2560x1600.png");
		//		paths.add("search/gps/2.3.3/2/480x800.png");
		//		paths.add("search/gps/2.3.3/2/768x1280.png");
		//		paths.add("search/gps/2.3.3/2/1280x800.png");
		//		paths.add("search/gps/4.1.2/2/2560x1600.png");
		//		paths.add("search/gps/2.3.3/2/480x800.png");
		//		paths.add("search/gps/2.3.3/2/768x1280.png");
		//		paths.add("search/gps/2.3.3/2/1280x800.png");
		//		paths.add("search/gps/4.1.2/2/2560x1600.png");
		//		paths.add("search/gps/2.3.3/2/480x800.png");
		//		paths.add("search/gps/2.3.3/2/768x1280.png");
		//		paths.add("search/gps/2.3.3/2/1280x800.png");
		//		paths.add("search/gps/4.1.2/2/2560x1600.png");
		//		paths.add("search/manager/2.3.3/1/480x800.png");
		//		paths.add("search/manager/2.3.3/1/768x1280.png");
		//		paths.add("search/manager/2.3.3/1/1280x800.png");
		//		paths.add("search/manager/4.1.2/1/2560x1600.png");
		//		paths.add("search/manager/2.3.3/1/480x800.png");
		//		paths.add("search/manager/2.3.3/1/768x1280.png");
		//		paths.add("search/manager/2.3.3/1/1280x800.png");
		//		paths.add("search/manager/4.1.2/1/2560x1600.png");
		//		paths.add("search/manager/2.3.3/1/480x800.png");
		//		paths.add("search/manager/2.3.3/1/768x1280.png");
		//		paths.add("search/manager/2.3.3/1/1280x800.png");
		//		paths.add("search/manager/4.1.2/1/2560x1600.png");
		//		paths.add("search/manager/2.3.3/1/480x800.png");
		//		paths.add("search/manager/2.3.3/1/768x1280.png");
		//		paths.add("search/manager/2.3.3/1/1280x800.png");
		//		paths.add("search/manager/4.1.2/1/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/1/480x800.png");
		//		paths.add("search/notepad/2.3.3/1/768x1280.png");
		//		paths.add("search/notepad/2.3.3/1/1280x800.png");
		//		paths.add("search/notepad/4.1.2/1/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/1/480x800.png");
		//		paths.add("search/notepad/2.3.3/1/768x1280.png");
		//		paths.add("search/notepad/2.3.3/1/1280x800.png");
		//		paths.add("search/notepad/4.1.2/1/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/1/480x800.png");
		//		paths.add("search/notepad/2.3.3/1/768x1280.png");
		//		paths.add("search/notepad/2.3.3/1/1280x800.png");
		//		paths.add("search/notepad/4.1.2/1/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/1/480x800.png");
		//		paths.add("search/notepad/2.3.3/1/768x1280.png");
		//		paths.add("search/notepad/2.3.3/1/1280x800.png");
		//		paths.add("search/notepad/4.1.2/1/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/1/480x800.png");
		//		paths.add("search/notepad/2.3.3/1/768x1280.png");
		//		paths.add("search/notepad/2.3.3/1/1280x800.png");
		//		paths.add("search/notepad/4.1.2/1/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/1/480x800.png");
		//		paths.add("search/notepad/2.3.3/1/768x1280.png");
		//		paths.add("search/notepad/2.3.3/1/1280x800.png");
		//		paths.add("search/notepad/4.1.2/1/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/2/480x800.png");
		//		paths.add("search/notepad/2.3.3/2/768x1280.png");
		//		paths.add("search/notepad/2.3.3/2/1280x800.png");
		//		paths.add("search/notepad/4.1.2/2/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/2/480x800.png");
		//		paths.add("search/notepad/2.3.3/2/768x1280.png");
		//		paths.add("search/notepad/2.3.3/2/1280x800.png");
		//		paths.add("search/notepad/4.1.2/2/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/2/480x800.png");
		//		paths.add("search/notepad/2.3.3/2/768x1280.png");
		//		paths.add("search/notepad/2.3.3/2/1280x800.png");
		//		paths.add("search/notepad/4.1.2/2/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/2/480x800.png");
		//		paths.add("search/notepad/2.3.3/2/768x1280.png");
		//		paths.add("search/notepad/2.3.3/2/1280x800.png");
		//		paths.add("search/notepad/4.1.2/2/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/2/480x800.png");
		//		paths.add("search/notepad/2.3.3/2/768x1280.png");
		//		paths.add("search/notepad/2.3.3/2/1280x800.png");
		//		paths.add("search/notepad/4.1.2/2/2560x1600.png");
		//		paths.add("search/notepad/2.3.3/2/480x800.png");
		//		paths.add("search/notepad/2.3.3/2/768x1280.png");
		//		paths.add("search/notepad/2.3.3/2/1280x800.png");
		//		paths.add("search/notepad/4.1.2/2/2560x1600.png");
		//		paths.add("search/settings/2.3.3/1/480x800.png");
		//		paths.add("search/settings/2.3.3/1/768x1280.png");
		//		paths.add("search/settings/2.3.3/1/1280x800.png");
		//		paths.add("search/settings/2.3.3/1/480x800.png");
		//		paths.add("search/settings/2.3.3/1/768x1280.png");
		//		paths.add("search/settings/2.3.3/1/1280x800.png");
		//		paths.add("search/settings/2.3.3/1/480x800.png");
		paths.add("search/settings/2.3.3/1/768x1280.png");
		paths.add("search/settings/2.3.3/1/1280x800.png");
		paths.add("search/settings/2.3.3/1/480x800.png");
		paths.add("search/settings/2.3.3/1/768x1280.png");
		paths.add("search/settings/2.3.3/1/1280x800.png");
		paths.add("search/settings/2.3.3/2/480x800.png");
		paths.add("search/settings/2.3.3/2/768x1280.png");
		paths.add("search/settings/2.3.3/2/1280x800.png");
		paths.add("search/settings/2.3.3/2/480x800.png");
		paths.add("search/settings/2.3.3/2/768x1280.png");
		paths.add("search/settings/2.3.3/2/1280x800.png");
		paths.add("search/settings/2.3.3/2/480x800.png");
		paths.add("search/settings/2.3.3/2/768x1280.png");
		paths.add("search/settings/2.3.3/2/1280x800.png");
		//		paths.add("search/settings/2.3.3/2/480x800.png");
		//		paths.add("search/settings/2.3.3/2/768x1280.png");
		//		paths.add("search/settings/2.3.3/2/1280x800.png");
		//		paths.add("search/calculator/4.2.2/1/480x800.png");
		//		paths.add("search/calculator/4.2.2/1/800x1280.png");
		//		paths.add("search/calculator/4.2.2/2/480x800.png");
		//		paths.add("search/calculator/4.2.2/2/800x1280.png");

		return paths;

	}
}
