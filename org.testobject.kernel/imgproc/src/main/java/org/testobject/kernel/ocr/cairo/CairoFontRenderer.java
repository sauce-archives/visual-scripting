package org.testobject.kernel.ocr.cairo;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.ocr.freetype.FT2Library;
import org.testobject.kernel.ocr.freetype.FreeTypeLoader;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.ocr.freetype.FreeTypeFace;
import org.testobject.kernel.ocr.freetype.FreeTypeFont;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import static org.testobject.kernel.ocr.cairo.Cairo.cairo_create;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_destroy;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_font_options_create;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_font_options_destroy;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_font_options_set_antialias;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_font_options_set_hint_style;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_font_options_set_subpixel_order;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_ft_font_face_create_for_ft_face;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_image_surface_create_for_data;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_move_to;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_paint;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_set_font_face;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_set_font_options;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_set_font_size;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_set_source_rgb;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_show_text;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_surface_destroy;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_antialias_t.CAIRO_ANTIALIAS_GRAY;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_antialias_t.CAIRO_ANTIALIAS_SUBPIXEL;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_format_t.CAIRO_FORMAT_ARGB32;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_hint_style_t.CAIRO_HINT_STYLE_SLIGHT;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_subpixel_order_t.CAIRO_SUBPIXEL_ORDER_DEFAULT;
import static org.testobject.kernel.ocr.cairo.Cairo.cairo_subpixel_order_t.CAIRO_SUBPIXEL_ORDER_RGB;

/**
 * 
 * @author enijkamp
 * 
 */
public class CairoFontRenderer {

	public enum Settings {
		DEFAULT(CAIRO_SUBPIXEL_ORDER_RGB, CAIRO_ANTIALIAS_SUBPIXEL, CAIRO_HINT_STYLE_SLIGHT), GRAY(CAIRO_SUBPIXEL_ORDER_DEFAULT,
		        CAIRO_ANTIALIAS_GRAY, CAIRO_HINT_STYLE_SLIGHT);

		public final int subpixelOrder;
		public final int antialias;
		public final int hintStyle;

		private Settings(int subpixelOrder, int antialias, int hintStyle) {
			this.subpixelOrder = subpixelOrder;
			this.antialias = antialias;
			this.hintStyle = hintStyle;
		}
	}

	private final Settings settings;
	private final double scaled_size;

	private final FreeTypeFace ft_face;
	private final Pointer cr_face;

	public CairoFontRenderer(File fontFile, int size, double baselineDpi, double targetDpi) throws IOException {
		this(fontFile, size, targetDpi, baselineDpi, Settings.DEFAULT);
	}

	public CairoFontRenderer(File fontFile, int size, double baselineDpi, double targetDpi, Settings settings) throws IOException {
		this.settings = settings;
		// FIXME something is off with this mapping, for font size 10pt we have to pass 13.3pt as argument (en)
		this.scaled_size = size * (targetDpi / baselineDpi) * (targetDpi / baselineDpi);
		ft_face = FreeTypeFont.createFreeTypeFace(FreeTypeLoader.loadRewrite().freetype, fontFile);

		cr_face = cairo_ft_font_face_create_for_ft_face(ft_face.getFace().getPointer(), FT2Library.FT_LOAD_TARGET_LIGHT);
	}

	public BufferedImage drawString(String string) throws IOException {

		// FT2Helper.INSTANCE.FT_Set_Char_Size(ft_face.getPointer(), to26_6(0), to26_6((float)size), 96, 96);

		// FIXME this is a waste of memory (en)
		// data
		final int width = 400, height = 200;
		final int stride = 4 * width;
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Pointer surface = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, w, h);
		Pointer surfaceData = Glib.g_malloc(new NativeLong(stride * height));
		Pointer surface = cairo_image_surface_create_for_data(surfaceData, CAIRO_FORMAT_ARGB32, width, height, stride);
		Pointer cr = cairo_create(surface);

		// fill
		{
			cairo_set_source_rgb(cr, 1.0, 1.0, 1.0);
			cairo_paint(cr);
		}
		// font
		{
			cairo_set_font_face(cr, cr_face);

			cairo_set_font_size(cr, scaled_size);
		}
		// rendering
		{
			Pointer options = cairo_font_options_create();
			cairo_font_options_set_subpixel_order(options, settings.subpixelOrder);
			cairo_font_options_set_antialias(options, settings.antialias);
			cairo_font_options_set_hint_style(options, settings.hintStyle);
			cairo_set_font_options(cr, options);
			cairo_font_options_destroy(options);
		}
		// draw
		{
			cairo_set_source_rgb(cr, 0.0, 0.0, 0.0);
			cairo_move_to(cr, 10.0, 100.0);
			cairo_show_text(cr, string);
		}
		// transfer memory
		{
			int[] pixelsCairo = new int[width * height];
			surfaceData.read(0, pixelsCairo, 0, width * height);

			int[] pixelsJava = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			System.arraycopy(pixelsCairo, 0, pixelsJava, 0, pixelsCairo.length);

			/*
			 * Visualizer vis = new SwingVisualizer(); vis.figure("memory", new Renderable() { public void
			 * render(Graphics g) { g.drawImage(image, 0, 0, image.getWidth(), image.getH()); } });
			 */
		}
		// free
		{
			//	INSTANCE.FT_Done_Face(ft_face.getFace().getPointer());
			cairo_destroy(cr);
			cairo_surface_destroy(surface);
			Glib.g_free(surfaceData);
		}
		// crop
		{
			// VisualizerUtil.show("memory", image, 20f);
			Image.Int trimmed = ImageUtil.trim(ImageUtil.toImageInt(image), Color.WHITE.getRGB());
			return ImageUtil.toBufferedImage(trimmed);
		}
	}

}
