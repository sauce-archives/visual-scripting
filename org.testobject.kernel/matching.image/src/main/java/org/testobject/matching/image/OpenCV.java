package org.testobject.matching.image;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * 
 * @author enijkamp, lbielski
 *
 */
public class OpenCV {

	public static BufferedImage createBufferedImage(int w, int h)
	{
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		int[] nBits = { 8, 8, 8, 8 };
		ColorModel cm = new ComponentColorModel(cs, nBits,
				true, false,
				Transparency.TRANSLUCENT,
				DataBuffer.TYPE_BYTE);

		SampleModel sm = cm.createCompatibleSampleModel(w, h);
		DataBufferByte db = new DataBufferByte(w * h * 4); //4 channels buffer
		WritableRaster r = WritableRaster.createWritableRaster(sm, db, new Point(0, 0));
		BufferedImage bm = new BufferedImage(cm, r, false, null);
		return bm;
	}

	public static byte[] convertBufferedImageToByteArray(BufferedImage img) {
		BufferedImage cvImg = createBufferedImage(img.getWidth(), img.getHeight());
		Graphics2D g = cvImg.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();
		return ((DataBufferByte) cvImg.getRaster().getDataBuffer()).getData();
	}

	public static Mat convertBufferedImageToMat(BufferedImage img) {
		byte[] data = convertBufferedImageToByteArray(img);

		Mat mat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC4);
		mat.put(0, 0, data);

		return mat;
	}

	public static BufferedImage matToBufferedImage(Mat bgr) {
		int width = bgr.width();
		int height = bgr.height();
		BufferedImage image;
		WritableRaster raster;

		if (bgr.channels() == 1) {
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			raster = image.getRaster();

			byte[] px = new byte[1];

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					bgr.get(y, x, px);
					raster.setSample(x, y, 0, px[0]);
				}
			}
		} else if(bgr.channels() == 3){
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			raster = image.getRaster();

			byte[] px = new byte[3];
			int[] rgb = new int[3];

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					bgr.get(y, x, px);
					rgb[0] = px[2];
					rgb[1] = px[1];
					rgb[2] = px[0];
					raster.setPixel(x, y, rgb);
				}
			}
		} else {
			image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			raster = image.getRaster();
			
			byte[] px = new byte[4];
			int[] rgb = new int[4];

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					bgr.get(y, x, px);
					rgb[0] = px[0];
					rgb[1] = px[1];
					rgb[2] = px[2];
					rgb[3] = px[3];
					raster.setPixel(x, y, rgb);
				}
			}
		}

		return image;
	}

}