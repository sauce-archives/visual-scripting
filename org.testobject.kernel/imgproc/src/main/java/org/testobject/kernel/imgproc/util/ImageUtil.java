package org.testobject.kernel.imgproc.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.testobject.commons.util.file.FileUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.Image.Int.Type;

/**
 * 
 * @author nijkamp
 * 
 */
public class ImageUtil {

	public static Image.Int fill(Image.Int image, Color color) {
		int rgb = color.getRGB();
		for (int i = 0; i < image.pixels.length; i++) {
			image.pixels[i] = rgb;
		}

		return image;
	}
	
	public static Image.Int grow(Image.Int image, int xOffset, int yOffset, int width, int height) {
		Image.Int grow = new Image.Int(image.w + width, image.h + height);
		paint(image, grow, xOffset, yOffset);
		return grow;
	}

	public static Image.Int grow(Image.Int image, int width, int height) {
		Image.Int grow = new Image.Int(image.w + width, image.h + height);
		paint(image, grow, 1, 1);
		return grow;
	}

	public static void paint(Image.Int src, Image.Int dest, int xOffset, int yOffset) {
		for (int y = 0; y < src.h; y++) {
			System.arraycopy(src.pixels, y * src.w, dest.pixels, dest.w * (y + yOffset) + xOffset, src.w);
		}
	}

	public static Image.Int read(String file) throws IOException {
		BufferedImage read = ImageIO.read(FileUtil.toFileFromSystem(file));
		return toImage(read);
	}

	public static Image.Int read(URL url) throws IOException {
		return toImage(ImageIO.read(url));
	}

	public static Image.Int read(URI url) throws IOException {
		return toImage(ImageIO.read(url.toURL()));
	}

	public static Image.Int read(InputStream is) throws IOException {
		return toImage(ImageIO.read(is));
	}

	public static int[][] getPixels(java.awt.Image image) throws IOException {
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		int pix[] = new int[w * h];
		PixelGrabber grabber = new PixelGrabber(image, 0, 0, w, h, pix, 0, w);

		try {
			if (grabber.grabPixels() != true) {
				throw new IOException("Grabber returned false: " + grabber.status());
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		int pixels[][] = new int[w][h];
		for (int x = w; x-- > 0;) {
			for (int y = h; y-- > 0;) {
				pixels[x][y] = pix[y * w + x];
			}
		}

		return pixels;
	}

	public static BufferedImage crop(BufferedImage source, Rectangle rect) {
		BufferedImage dest = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), source.getType());
		Graphics g = dest.getGraphics();
		g.drawImage(source, 0, 0, (int) rect.getWidth(), (int) rect.getHeight(), (int) rect.getX(), (int) rect.getY(), (int) rect.getX()
				+ (int) rect.getWidth(), (int) rect.getY() + (int) rect.getHeight(), null);
		g.dispose();
		return dest;
	}

	public static Image.Bool crop(Image.Bool source, Rectangle rect) {
		Image.Bool target = new Image.Bool(rect.height, rect.width);
		for (int y = 0, offset = 0; y < rect.height; y++, offset += rect.width) {
			final int start = y * source.w + rect.x;
			System.arraycopy(source.pixels, start, target.pixels, offset, rect.width);
		}
		return target;
	}

	public static Image.Byte crop(Image.Byte source, Rectangle rect) {
		Image.Byte target = new Image.Byte(rect.width, rect.height);
		for (int y = 0, offset = 0; y < rect.height; y++, offset += rect.width) {
			final int start = y * source.w + rect.x;
			System.arraycopy(source.pixels, start, target.pixels, offset, rect.width);
		}
		return target;
	}

	public static Image.Int crop(Image.Int source, Rectangle rect) {
		Image.Int target = new Image.Int(rect.width, rect.height, source.type);
		int offset = rect.y;
		for (int y = 0; y < rect.height; y++) {
			final int start = (y + offset) * source.w + rect.x;
			System.arraycopy(source.pixels, start, target.pixels, y * rect.width, Math.min(source.w, rect.width));
		}
		return target;
	}
	
	public static Image.Int crop(Image.Int source, org.testobject.commons.math.algebra.Rectangle.Int rect) {
		Image.Int target = new Image.Int(rect.w, rect.h, source.type);
		int offset = rect.y;
		for (int y = 0; y < rect.h; y++) {
			final int start = (y + offset) * source.w + rect.x;
			System.arraycopy(source.pixels, start, target.pixels, y * rect.w, Math.min(source.w, rect.w));
		}
		return target;
	}

	public static Image.Bool toBlackWhite(Image.Int rgb, int threshold) {
		final Image.Bool bool = new Image.Bool(rgb.h, rgb.w);
		toBlackWhite(rgb, bool, threshold);
		return bool;
	}

	public static void toBlackWhite(Image.Int rgb, Image.Bool bool, int threshold) {
		for (int y = 0; y < rgb.h; y++) {
			for (int x = 0; x < rgb.w; x++) {
				int pix = rgb.pixels[y * rgb.w + x];
				bool.pixels[y * bool.w + x] = rgbToBlackWhite(pix, threshold);
			}
		}
	}

	public static BufferedImage toBlackWhite(BufferedImage image, int threshold) {
		final int WHITE = Color.WHITE.getRGB(), BLACK = Color.BLACK.getRGB();
		final int[] pixels = toRgbPixels(image);
		for (int i = 0; i < pixels.length; i++) {
			// true == white == fg
			pixels[i] = rgbToBlackWhite(pixels[i], threshold) ? WHITE : BLACK;
		}
		return toBufferedImage(pixels, image.getWidth(null), image.getHeight(null));
	}

	public final static boolean rgbToBlackWhite(int pix, int threshold) {
		final int r = (pix >> 16) & 0xff;
		final int g = (pix >> 8) & 0xff;
		final int b = (pix >> 0) & 0xff;
		final int intensity = ((r * 306) + (g * 601) + (b * 117)) >> 10;
		return (intensity > threshold); // true == white == fg
	}

	public static BufferedImage toBufferedImage(Image.Int image) {
		if (image.type == Type.RGB) {
			final ColorModel colorModel = new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff);
			final SampleModel sampleModel = colorModel.createCompatibleSampleModel(image.w, image.h);
			final DataBuffer dataBuffer = new DataBufferInt(image.pixels, image.w * image.h);
			final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
			return new BufferedImage(colorModel, raster, false, null);
		}

		if (image.type == Type.ARGB) {
			final ColorModel colorModel = new DirectColorModel(32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000);
			final SampleModel sampleModel = colorModel.createCompatibleSampleModel(image.w, image.h);
			final DataBuffer dataBuffer = new DataBufferInt(image.pixels, image.w * image.h);
			final WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);
			return new BufferedImage(colorModel, raster, false, null);
		}

		throw new IllegalArgumentException();
	}

	public static BufferedImage toBufferedImageMemoryImageSource(Image.Int image) {
		final BufferedImage img = new BufferedImage(image.w, image.h, BufferedImage.TYPE_INT_RGB);
		final java.awt.Image piximg = Toolkit.getDefaultToolkit().createImage(
				new MemoryImageSource(image.w, image.h, image.pixels, 0, image.w));
		img.getGraphics().drawImage(piximg, 0, 0, null);
		return img;
	}

	public static BufferedImage toBufferedImage(int[] pixels, int w, int h) {
		final BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final java.awt.Image piximg = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(w, h, pixels, 0, w));
		img.getGraphics().drawImage(piximg, 0, 0, null);
		return img;
	}

	public static Image.Int toImageInt(Image.Double in) {
		Image.Int out = new Image.Int(in.w, in.h);
		for (int y = 0; y < in.h; y++) {
			for (int x = 0; x < in.w; x++) {
				int rgb = (int) (in.pixels[y * in.w + x]);
				out.pixels[y * out.w + x] = toInt(rgb, rgb, rgb);
			}
		}
		return out;
	}

	public static Image.Int toImageInt(Image.Byte in) {
		Image.Int out = new Image.Int(in.w, in.h);
		for (int y = 0; y < in.h; y++) {
			for (int x = 0; x < in.w; x++) {
				int rgb = (int) (in.pixels[y * in.w + x]) & 0xff;
				out.pixels[y * out.w + x] = toInt(rgb, rgb, rgb);
			}
		}
		return out;
	}

	public static Image.Int toImageInt(BufferedImage buffer) {
		int[] pixels = ImageUtil.toRgbPixels(buffer);
		return new Image.Int(Arrays.copyOf(pixels, pixels.length), buffer.getWidth(), buffer.getHeight());
	}

	public static Image.Byte toImageByte(BufferedImage buffer) {
		Image.Byte image = new Image.Byte(buffer.getHeight(), buffer.getWidth());
		toImage(buffer, image);
		return image;
	}

	public static void toImage(BufferedImage buffer, Image.Byte image) {
		int[] pixels = ImageUtil.toRgbPixels(buffer);
		for (int y = 0; y < buffer.getHeight(); y++) {
			for (int x = 0; x < buffer.getWidth(); x++) {
				int rgb = pixels[y * buffer.getWidth() + x];
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = (rgb >> 0) & 0xff;
				float brightness = getBrightness(r, g, b);
				byte packed = (byte) (brightness * 255f);
				image.pixels[y * image.w + x] = packed;
			}
		}
	}

	public static Image.Byte toImageByte(Image.Int buffer) {
		Image.Byte image = new Image.Byte(buffer.w, buffer.h);
		toImage(buffer, image);
		return image;
	}

	public static void toImage(Image.Int buffer, Image.Byte image) {
		int[] pixels = buffer.pixels;
		for (int y = 0; y < buffer.h; y++) {
			for (int x = 0; x < buffer.w; x++) {
				image.pixels[y * image.w + x] = toByte(pixels[y * buffer.w + x]);
			}
		}
	}

	public static byte toByte(int rgb) {
		int r = (rgb >> 16) & 0xff;
		int g = (rgb >> 8) & 0xff;
		int b = (rgb >> 0) & 0xff;
		float brightness = getBrightness(r, g, b);
		return (byte) (brightness * 255f);
	}

	public static BufferedImage toGrayscaleImage(BufferedImage image) {
		// FIXME find more efficient way for grayscale conversion (en)
		BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int rgb = image.getRGB(x, y);
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = (rgb >> 0) & 0xff;
				int brightness = (int) (getBrightness(r, g, b) * 255f);
				int grayRgb = (brightness << 16) | (brightness << 8) | (brightness << 0);
				gray.setRGB(x, y, grayRgb);
			}
		}
		return gray;
	}

	public static Image.Int toGrayscaleImage(Image.Int image) {
		// FIXME find more efficient way for grayscale conversion (en)
		Image.Int gray = new Image.Int(image.w, image.h);
		for (int y = 0; y < image.h; y++) {
			for (int x = 0; x < image.w; x++) {
				int rgb = image.pixels[y * image.w + x];
				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = (rgb >> 0) & 0xff;
				int brightness = (int) (getBrightness(r, g, b) * 255f);
				int grayRgb = (brightness << 16) | (brightness << 8) | (brightness << 0);
				gray.pixels[y * gray.w + x] = grayRgb;
			}
		}
		return gray;
	}

	public static float getBrightness(float r, float g, float b) {
		r /= 255f;
		g /= 255f;
		b /= 255f;
		return (r + g + b) / 3.0f;
	}

	public static Image.Int toImage(BufferedImage image) {
		Image.Int.Type type = toImageType(image.getType());
		return new Image.Int(toRgbPixels(image), 0, 0, image.getWidth(), image.getHeight(), image.getWidth(), type);
	}
	
	private static Image.Int.Type toImageType(int type) {
		if(type == BufferedImage.TYPE_INT_ARGB) {
			return Image.Int.Type.ARGB;
		}
		
		if(type == BufferedImage.TYPE_INT_RGB) {
			return Image.Int.Type.RGB;
		}
		
		return Image.Int.Type.RGB;
		
		// FIXME (en)
		// throw new IllegalArgumentException("type " + type);
	}

	public static int[] toRgbPixels(BufferedImage image) {
		if (image.getData().getDataBuffer() instanceof DataBufferInt) {
			return ((DataBufferInt) image.getData().getDataBuffer()).getData();
		} else {
			return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
		}
	}

	public static BufferedImage convert(BufferedImage src, int bufImgType) {
		final BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(), bufImgType);
		final Graphics2D g = img.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return img;
	}

	public static BufferedImage deepCopy(BufferedImage bi) {
		final ColorModel cm = bi.getColorModel();
		final boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		final WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	public static Image.Int deepCopy(Image.Int buffer) {
		int[] copy = Arrays.copyOf(buffer.pixels, buffer.pixels.length);
		return new Image.Int(copy, buffer.x, buffer.y, buffer.w, buffer.h, buffer.scanline, buffer.type);
	}

	public static Image.Bool trim(Image.Bool source) {
		Rectangle box = getBoundingBox(source);

		if (box.isEmpty()) {
			return new Image.Bool(0, 0);
		} else {
			return crop(source, box);
		}
	}

	public static Image.Int trim(Image.Int source, int background) {
		Rectangle box = getBoundingBox(source, background);

		if (box.isEmpty()) {
			return new Image.Int(0, 0);
		} else {
			return crop(source, box);
		}
	}

	public static BufferedImage trim(BufferedImage source, int background) {
		Rectangle box = getBoundingBox(toImageInt(source), background);

		if (box.isEmpty()) {
			return new BufferedImage(0, 0, source.getType());
		} else {
			return crop(source, box);
		}
	}

	public static Rectangle getBoundingBox(Image.Bool source) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

		int width = source.w;
		boolean[] pixels = source.pixels;
		for (int y = 0; y < source.h; y++) {
			for (int x = 0; x < source.w; x++) {
				if (pixels[y * width + x]) {
					minX = (x < minX ? x : minX);
					minY = (y < minY ? y : minY);
					maxX = (x > maxX ? x : maxX);
					maxY = (y > maxY ? y : maxY);
				}
			}
		}

		if (maxX == Integer.MIN_VALUE || maxY == Integer.MIN_VALUE) {
			return new Rectangle(0, 0, 0, 0);
		} else {
			return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
		}
	}

	public static Rectangle getBoundingBox(Image.Int source, int background) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

		int width = source.w;
		int height = source.h;
		int[] pixels = source.pixels;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (pixels[y * width + x] != background) {
					minX = (x < minX ? x : minX);
					minY = (y < minY ? y : minY);
					maxX = (x > maxX ? x : maxX);
					maxY = (y > maxY ? y : maxY);
				}
			}
		}

		if (maxX == Integer.MIN_VALUE || maxY == Integer.MIN_VALUE) {
			return new Rectangle(0, 0, 0, 0);
		} else {
			return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
		}
	}

	public static void show(final BufferedImage image, String title) {
		show(image, image.getWidth() + 100, image.getHeight() + 100, title);
	}

	public static void show(final BufferedImage image) {
		show(image, image.getWidth() + 100, image.getHeight() + 100);
	}

	public static void show(final BufferedImage image, final int width, final int height) {
		show(image, width, height, "");
	}

	public static void show(final BufferedImage image, final int width, final int height, final String title) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			@SuppressWarnings("serial")
			public void run() {
				new JFrame() {
					{
						setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
						setTitle(title);
						setSize(width, height);
						setVisible(true);
						getContentPane().setLayout(new BorderLayout());

						final JPanel panel = new JPanel() {
							@Override
							public void paintComponent(Graphics g) {
								super.paintComponent(g);
								g.drawImage(image, 50, 50, null);
							}

						};

						getContentPane().add(panel, BorderLayout.CENTER);
					}

				};
			}
		});
	}

	public static int[] toRGB(int color) {
		int[] rgb = new int[3];
		toRGB(color, rgb);
		return rgb;
	}

	public static void toRGB(int color, int[] rgb) {
		rgb[0] = (color >> 16) & 0xff;
		rgb[1] = (color >> 8) & 0xff;
		rgb[2] = (color >> 0) & 0xff;
	}

	public static int toInt(int[] rgb) {
		return toInt(rgb[0], rgb[1], rgb[2]);
	}

	public static int toInt(int r, int g, int b) {
		return toInt(255, r, g, b);
	}

	public static int toInt(int a, int r, int g, int b) {
		return (a << 24) | (r << 16) | (g << 8) | (b << 0);
	}
}
