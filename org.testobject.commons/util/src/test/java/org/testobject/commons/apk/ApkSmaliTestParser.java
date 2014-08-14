package org.testobject.commons.apk;

import java.io.File;
import java.io.IOException;

import brut.androlib.AndrolibException;

public class ApkSmaliTestParser {

	public static void main(String[] args) throws AndrolibException, IOException {
		File dexFile = new File("/Users/tal/Desktop/classes.dex");

		for (String test : new ApkSmaliParser().parse(dexFile)) {
			System.out.println(test);
		}

	}
}
