package org.testobject.kernel.replay;

import org.testobject.kernel.locator.api.Locator;
import org.testobject.kernel.locator.api.Container;

/**
 * 
 * @author nijkamp
 * 
 */
// FIXME move to shared module (en)
public class LocatorMatcher {
	// FIXME go deeper until ambiguity is resolved (en)
	// TODO unify code with LocatorIdentity code in equality-tree (and later fuzzy-match logic) (en)
	public static boolean equalsRecursive(Locator locator1, Locator locator2) {
		if (equalsLocator(locator1, locator2) == false) {
			return false;
		}

		if (locator1 instanceof Container != locator2 instanceof Container) {
			return false;
		}

		if (locator1 instanceof Container && locator2 instanceof Container) {
			Container container1 = (Container) locator1;
			Container container2 = (Container) locator2;
			// FIXME this does not work for paths in replay unit test (containers are almost ut empty) (en)
//			if (container1.getChilds().length != container2.getChilds().length) {
//				return false;
//			}
			for (Locator child1 : container1.getChilds()) {
				if (containsLocator(container2.getChilds(), child1) == false) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean containsLocator(Locator[] locators, Locator probe) {
		for (Locator child : locators) {
			if (equalsLocator(child, probe)) {
				return true;
			}
		}
		return false;
	}

	public static boolean equalsLocator(Locator locator1, Locator locator2) {
		// FIXME check contours
		// if(instanceOf(locator1, locator2, IconButton.class))
		// {
		// // get icons
		// IconButton icon1 = (IconButton) locator1;
		// IconButton icon2 = (IconButton) locator2;
		//
		// // convert to finger prints
		// IconFingerprint print1 = new IconFingerprint(ImageUtil.toImageInt(icon1.image), 0xf2, 0xf1, 0xf0);
		// IconFingerprint print2 = new IconFingerprint(ImageUtil.toImageInt(icon2.image), 0xf2, 0xf1, 0xf0);
		//
		// // distance
		// double distance = IconFingerprint.lumaDistance2(print1, print2);
		//
		// // fuzzy match
		// return distance < .5;
		// }

		return locator1.equals(locator2);
	}

//	private static boolean instanceOf(Locator locator1, Locator locator2, Class<? extends Locator> clazz) {
//		return locator1.getClass().equals(clazz) && locator2.getClass().equals(clazz);
//	}
}
