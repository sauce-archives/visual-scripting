package org.testobject.commons.util.platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.io.FileUtil;

/**
 * 
 * @author enijkamp
 * 
 */
public class SharedObject {

	public static final Log log = LogFactory.getLog(SharedObject.class);

	private static final String LIBS = "libs";
	private static final String PATH = "testobject";

	public static File getExtractedPath(String name) {
		return new File(getTempPath() + toPlatformPath() + toPlatformSharedObject(name));
	}
	
	public static File extractSharedObject(String name) {
		return copySharedObject(toPlatformPath(), toPlatformSharedObject(name));
	}
	
	public static File copySharedObject(String path, String name) {
		File source = new File(path + name);
		File target = new File(getTempPath() + path + name);
		
		if(target.exists() == false) {
			log.info("Copying '" + source.getAbsolutePath() + "' to '" + target.getAbsolutePath() + "'");
			new File(getTempPath() + path).mkdirs();
			try (InputStream in = FileUtil.readFileFromClassPath(source);
					OutputStream out = new FileOutputStream(target)) {
				FileUtil.transfer(in, out);
			} catch (Exception e) {
				throw new RuntimeException(
						"Failed to copy required shared object '" + name + "'", e);
			}
		}
		return target;
	}
	
	private static String toPlatformPath() {
		if(OS.isLinux()) {
			return LIBS + "/unix/x86_" + Arch.getArchBits() + "/";
		} else {
			throw new IllegalStateException();
		}
	}
	
	private static String toPlatformSharedObject(String name) {
		if(OS.isLinux()) {
			String prefix = "lib";
			String postfix = ".so";
			return prefix + name + postfix;
		} else {
			throw new IllegalStateException();
		}
	}
	
	private static String getTempPath() {
		return System.getProperty("java.io.tmpdir") + "/" + PATH + "/";
	}

}
