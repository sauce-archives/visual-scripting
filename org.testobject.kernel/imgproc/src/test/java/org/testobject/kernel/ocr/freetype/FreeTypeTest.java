package org.testobject.kernel.ocr.freetype;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.testobject.commons.util.file.FileUtil;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeException;
import org.testobject.kernel.ocr.freetype.FreeTypeGrayscaleFontRenderer;
import org.testobject.kernel.ocr.freetype.FreeTypeLoader;

/**
 * 
 * @author enijkamp
 *
 */
public class FreeTypeTest {
	
	@Test
	public void testFreeType242_C() throws FreeTypeException {
		Stats stats = render('C', FreeTypeLoader.FT_2_4_2);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_2));
		assertThat(stats.intensity, is(47930));
	}
	
	@Test
	public void testFreeType244_C() throws FreeTypeException {
		Stats stats = render('C', FreeTypeLoader.FT_2_4_4);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_4));
		assertThat(stats.intensity, is(48311));
	}
	
	@Test
	public void testFreeType248_C() throws FreeTypeException {
		Stats stats = render('C', FreeTypeLoader.FT_2_4_8);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_8));
		assertThat(stats.intensity, is(47974));
	}
	
	@Test
	public void testFreeType242_F() throws FreeTypeException {
		Stats stats = render('F', FreeTypeLoader.FT_2_4_2);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_2));
		assertThat(stats.intensity, is(37797));
	}
	
	@Test
	public void testFreeType244_F() throws FreeTypeException {
		Stats stats = render('F', FreeTypeLoader.FT_2_4_4);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_4));
		assertThat(stats.intensity, is(37797));
	}
	
	@Test
	public void testFreeType248_F() throws FreeTypeException {
		Stats stats = render('F', FreeTypeLoader.FT_2_4_8);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_8));
		assertThat(stats.intensity, is(37797));
	}
	
	@Test
	public void testFreeType242_G() throws FreeTypeException {
		Stats stats = render('G', FreeTypeLoader.FT_2_4_2);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_2));
		assertThat(stats.intensity, is(43973));
	}
	
	@Test
	public void testFreeType244_G() throws FreeTypeException {
		Stats stats = render('G', FreeTypeLoader.FT_2_4_4);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_4));
		assertThat(stats.intensity, is(44406));
	}
	
	@Test
	public void testFreeType248_G() throws FreeTypeException {
		Stats stats = render('G', FreeTypeLoader.FT_2_4_8);
		
		assertThat(stats.version, CoreMatchers.is(FreeTypeLoader.FT_2_4_8));
		assertThat(stats.intensity, is(44012));
	}
	
	private Stats render(char chr, FreeTypeLoader.Version versionIn) throws FreeTypeException {
		FreeTypeLoader.Version versionOut;
		int intensity = 0;
		
		FreeTypeLoader.Instance instance = FreeTypeLoader.loadRewrite(versionIn);
		{
			File font = FileUtil.toFileFromSystem("android/4_0_3/fonts/sans-serif/Roboto-Regular.ttf");
			BufferedImage image = new FreeTypeGrayscaleFontRenderer(instance.freetype, 160, 240, FT2Library.FT_LCD_FILTER_LIGHT).drawChar(font, 16f, chr);
			for(int y = 0; y < image.getHeight(); y++) {
				for(int x = 0; x < image.getWidth(); x++) {
					intensity += (image.getRGB(x, y) >> 16) & 0xff;
				}
			}
			
			versionOut = instance.version;
		}
		FreeTypeLoader.unload(instance);
		
		return new Stats(intensity, versionOut);
	}

	private static class Stats {
		public final int intensity;
		public final FreeTypeLoader.Version version;
		
		public Stats(int intensity, FreeTypeLoader.Version version) {
			this.intensity = intensity;
			this.version = version;
		}
	}

}
