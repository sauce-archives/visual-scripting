package org.testobject.kernel.imaging.color.models;

import org.testobject.commons.util.image.Image;

/**
 * 
 * Standard NTSC conversion formula that is used for calculation the effective luminance of a pixel.
 *
 * http://en.wikipedia.org/wiki/YCbCr
 * http://www.mathworks.de/help/toolbox/images/ref/rgb2gray.html
 * 
 * @author enijkamp
 *
 */
public class YCrCb
{
	/*
	 * 
	 * YCbCr (256 levels) can be computed directly from 8-bit RGB as follows:
	 * Y = 0.299 R + 0.587 G + 0.114 B
	 * Cb = - 0.1687 R - 0.3313 G + 0.5 B + 128
	 * Cr = 0.5 R - 0.4187 G - 0.0813 B + 128
	 * NOTE - Not all image file formats store image samples in the order R0, G0, B0, ... Rn, Gn, Bn. 
	 * Be sure to verify the sample order before converting an RGB file to JFIF.
	 * 
	 * YCbCr to RGB Conversion
	 * RGB can be computed directly from YCbCr (256 levels) as follows:
	 * R = Y + 1.402 (Cr-128)
	 * G = Y - 0.34414 (Cb-128) - 0.71414 (Cr-128)
	 * B = Y + 1.772 (Cb-128)
	 * 
	 */

	static double y(double r, double g, double b)
	{
		return 0.299 * r + 0.587 * g + 0.114 * b;
	}

	static double cb(double r, double g, double b)
	{
		return -0.1687 * r - 0.3313 * g + 0.5 * b + 128.;
	}

	static double cr(double r, double g, double b)
	{
		return 0.5 * r - 0.4187 * g - 0.0813 * b + 128.;
	}

	static double r(double y, double cr, double cb)
	{
		return y + 1.402 * (cr - 128.);
	}

	static double g(double y, double cr, double cb)
	{
		return y - 0.34414 * (cb - 128.) - 0.71414 * (cr - 128.);

	}

	static double b(double y, double cr, double cb)
	{
		return y + 1.772 * (cb - 128.);
	}

	public static Image.Double y(Image.Int image)
	{
		Image.Double out = new Image.Double(image.w, image.h);

		for (int i = 0; i < image.h; i++)
		{
			for (int j = 0; j < image.w; j++)
			{
				int rgb = image.get(j, i);

				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = (rgb >> 0) & 0xff;

				out.pixels[i * out.w + j] = y(r, g, b);
			}
		}

		return out;
	}

	public static Image.Double cr(Image.Int image)
	{
		Image.Double out = new Image.Double(image.w, image.h);

		for (int i = 0; i < image.h; i++)
		{
			for (int j = 0; j < image.w; j++)
			{
				int rgb = image.get(j, i);

				int r = (rgb >> 16) & 0xff;
				int g = (rgb >> 8) & 0xff;
				int b = (rgb >> 0) & 0xff;

				out.pixels[i * out.w + j] = cr(r, g, b);
			}
		}

		return out;
	}

	public static Image.Double cb(Image.Int image)
	{
		Image.Double out = new Image.Double(image.w, image.h);

		for (int i = 0; i < image.h; i++)
		{
			for (int j = 0; j < image.w; j++)
			{
				int rgb = image.get(j, i);

				int r = (rgb >>> 16) & 0xff;
				int g = (rgb >>> 8) & 0xff;
				int b = (rgb >>> 0) & 0xff;

				out.pixels[i * out.w + j] = cb(r, g, b);
			}
		}

		return out;
	}

	public static Image.Int rgb(Image.Double yy, Image.Double cr, Image.Double cb)
	{
		Image.Int out = new Image.Int(yy.w, yy.h);

		for (int i = 0; i < out.h; i++)
		{
			for (int j = 0; j < out.w; j++)
			{
				double r = r(yy.get(j, i), cr.get(j, i), cb.get(j, i));
				double g = g(yy.get(j, i), cr.get(j, i), cb.get(j, i));
				double b = b(yy.get(j, i), cr.get(j, i), cb.get(j, i));

				if (r < 0.)
					r = 0.;
				if (r > 255.)
					r = 255.;
				if (g < 0.)
					g = 0.;
				if (g > 255.)
					g = 255.;
				if (b < 0.)
					b = 0.;
				if (b > 255.)
					b = 255.;

				int rgb = 0xff000000 + (((int) r) << 16) + (((int) g) << 8) + (((int) b) << 0);

				out.pixels[i * out.w + j] = rgb;
			}
		}

		return out;
	}

	public static void main(String... av)
	{
		double r = 240.0;
		double g = 0.0;
		double b = 0.0;

		double y = y(r, g, b);
		double cr = cr(r, g, b);
		double cb = cb(r, g, b);

		double rr = r(y, cr, cb);
		double gg = g(y, cr, cb);
		double bb = b(y, cr, cb);

		if (Math.abs(r - rr) > 2.0)
		{
			throw new AssertionError();
		}

		if (Math.abs(g - gg) > 2.0)
		{
			throw new AssertionError();
		}

		if (Math.abs(b - bb) > 2.0)
		{
			throw new AssertionError();
		}
	}
}
