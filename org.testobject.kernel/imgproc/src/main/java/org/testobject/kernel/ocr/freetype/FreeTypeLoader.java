package org.testobject.kernel.ocr.freetype;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testobject.commons.util.platform.SharedObject;
import org.testobject.kernel.ocr.freetype.FT2LibraryRewrite.FT242;
import org.testobject.kernel.ocr.freetype.FT2LibraryRewrite.FT244;
import org.testobject.kernel.ocr.freetype.FT2LibraryRewrite.FT248;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * 
 * @author enijkamp
 *
 */
public class FreeTypeLoader {
	
	private static final Log log = LogFactory.getLog(FreeTypeLoader.class);
	
	public final static Version FT_2_4_2 = new Version(2, 4, 2);
	public final static Version FT_2_4_4 = new Version(2, 4, 4);
	public final static Version FT_2_4_8 = new Version(2, 4, 8);
	
	private static final String FT2_Rewrite = "freetype_to";
	
	static {
		delete(FT_2_4_2);
		delete(FT_2_4_4);
		delete(FT_2_4_8);
	}
	
	private static void delete(Version version) {
		File file = SharedObject.getExtractedPath(FT2_Rewrite + "." + version);
		if(file.exists()) {
			log.info("Deleting '"+file.getAbsolutePath()+"'");
			file.delete();
		}
	}
	
	public static class Instance {
		public final Version version;
		public final File file;
		public final FT2Library freetype;
		
		public Instance(File file, Version version, FT2Library freetype) {
			this.file = file;
			this.version = version;
			this.freetype = freetype;
		}
	}
	
	public static class Version {
		public final int major, minor, patch;

		public Version(int major, int minor, int patch) {
			this.major = major;
			this.minor = minor;
			this.patch = patch;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Version == false) {
				return false;
			}

			Version version = (Version) obj;
			return version.major == major && version.minor == minor && version.patch == patch;
		}

		@Override
		public String toString() {
			return major + "." + minor + "." + patch;
		}
	}
	
	public static synchronized Instance loadVanilla() throws FreeTypeException {
		if (Platform.isWindows()) {
			return loadVanilla("freetype6");
		} else {
			return loadVanilla("freetype");
		}
	}
	
	public static synchronized Instance loadVanilla(String lib) throws FreeTypeException {
		
		// extract from jar
		File sharedobject = new File(lib);
		
		// rewrite symbols
		FT2Library freetype = (FT2Library) Native.loadLibrary(sharedobject.getAbsolutePath(), FT2Library.class);
		
		// check
		Pointer library = FT2Helper.FT_Init_FreeType(freetype);
		Version version;
		try {
			if(checkLibrary(freetype, library) == false) {
				throw new IllegalStateException();
			}
			version = getVersion(freetype, library);
		} finally {
			freetype.FT_Done_FreeType(library);
		}
		
		return new Instance(sharedobject, version, freetype);
	}
	
	public static synchronized Instance loadRewrite() throws FreeTypeException {
		return loadRewrite(FT_2_4_2);
	}
	
	public static synchronized Instance loadRewrite(Version version) throws FreeTypeException {
		// extract from jar
		File sharedobject = SharedObject.extractSharedObject(FT2_Rewrite + "." + version);
		
		// rewrite symbols
		FT2Library freetype;
		if(FT_2_4_2.equals(version)) {
			FT242 proxee = (FT242) Native.loadLibrary(sharedobject.getAbsolutePath(), FT242.class);
			freetype = FT242.Wrapper.wrap(proxee);
		} else if(FT_2_4_4.equals(version)) {
			FT244 proxee = (FT244) Native.loadLibrary(sharedobject.getAbsolutePath(), FT244.class);
			freetype = FT244.Wrapper.wrap(proxee);			
		} else if(FT_2_4_8.equals(version)) {
			FT248 proxee = (FT248) Native.loadLibrary(sharedobject.getAbsolutePath(), FT248.class);
			freetype = FT248.Wrapper.wrap(proxee);
		} else {
			throw new IllegalStateException();
		}
		
		// check
		Pointer library = FT2Helper.FT_Init_FreeType(freetype);
		try {
			if(checkLibrary(freetype, library) == false) {
				throw new IllegalStateException();
			}
			if(version.equals(getVersion(freetype, library)) == false) {
				throw new IllegalStateException();
			}
		} finally {
			freetype.FT_Done_FreeType(library);
		}
		
		return new Instance(sharedobject, version, freetype);
	}
	
	public static synchronized void unload(Instance instance) {
		NativeLibrary library = NativeLibrary.getInstance(instance.file.getAbsolutePath());
		library.dispose();
	}
	
	static Version getVersion(FT2Library freetype, Pointer library) {
		IntByReference major = new IntByReference();
		IntByReference minor = new IntByReference();
		IntByReference patch = new IntByReference();
		freetype.FT_Library_Version(library, major, minor, patch);

		return new Version(major.getValue(), minor.getValue(), patch.getValue());
	}

	static boolean checkLibrary(FT2Library freetype, Pointer library) {
		
		IntByReference major = new IntByReference();
		IntByReference minor = new IntByReference();
		IntByReference patch = new IntByReference();
		freetype.FT_Library_Version(library, major, minor, patch);

		int engine = -1;
		if (major.getValue() > 2 || (major.getValue() == 2 && minor.getValue() >= 2)) {
			// FT_Get_TrueType_Engine_Type requires FreeType 2.2.x
			engine = freetype.FT_Get_TrueType_Engine_Type(library);
		}

		log.info(String.format("FreeType2 version: %d.%d.%d TrueType engine: %s",
				major.getValue(), minor.getValue(), patch.getValue(), trueTypeEngineToString(engine)));

		final int MIN_MAJOR = 2;
		final int MIN_MINOR = 3;

		if (major.getValue() > MIN_MAJOR) {
			return true;
		} else if (major.getValue() == MIN_MAJOR && minor.getValue() >= MIN_MINOR) {
			return true;
		} else {
			log.warn("FreeType2 library too old");
			return false;
		}
	}
	
	static String trueTypeEngineToString(int engine) {
		switch (engine) {
		case FT2Library.FT_TRUETYPE_ENGINE_TYPE_NONE:
			return "NONE";
		case FT2Library.FT_TRUETYPE_ENGINE_TYPE_UNPATENTED:
			return "UNPATENTED";
		case FT2Library.FT_TRUETYPE_ENGINE_TYPE_PATENTED:
			return "PATENTED";
		default:
			return "unknown: " + engine;
		}
	}

}
