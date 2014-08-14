package org.testobject.kernel.ocr.freetype;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * 
 * @author enijkamp
 * 
 */
public class FT2Helper {

	static void checkReturnCode(int error) throws FreeTypeException {
		if (error != 0) {
			throw new FreeTypeException(error);
		}
	}
	
	static Pointer FT_Init_FreeType(FT2Library freetype) throws FreeTypeException {
		PointerByReference pp = new PointerByReference();
		checkReturnCode(freetype.FT_Init_FreeType(pp));
		return pp.getValue();
	}

	static FT2Library.FT_Face FT_New_Memory_Face(FT2Library freetype, Pointer library, ByteBuffer buffer, long face_index) throws FreeTypeException {
		PointerByReference pp = new PointerByReference();
		checkReturnCode(freetype.FT_New_Memory_Face(library, buffer, new NativeLong(buffer.remaining()), new NativeLong(face_index), pp));
		return new FT2Library.FT_Face(pp.getValue());
	}

	static int FT_IMAGE_TAG(int x1, int x2, int x3, int x4) {
		return (x1 << 24) | (x2 << 16) | (x3 << 8) | x4;
	}

	public static int to26_6(float value) {
		return Math.round(Math.scalb(value, 6));
	}

	static int round26_6(NativeLong value) {
		return round26_6(value.longValue());
	}

	static int round26_6(long value) {
		if (value < 0) {
			return (int) ((value - 32) >> 6);
		} else {
			return (int) ((value + 32) >> 6);
		}
	}

	static long FT_FixMul(long a, long b) {
		long tmp = a * b;
		if (tmp < 0) {
			tmp -= 0x8000;
		} else {
			tmp += 0x8000;
		}
		return tmp >> 16;
	}

	static boolean copyGlyphToBufferedImageGray(FT2Library.FT_Bitmap bitmap, BufferedImage img, int x, int y) {
		if (x + bitmap.width > img.getWidth()) {
			return false;
		}
		if (y + bitmap.rows > img.getHeight()) {
			return false;
		}

		final DataBufferByte dataBuffer = (DataBufferByte) img.getRaster().getDataBuffer();
		final byte[] data = dataBuffer.getData();
		final int stride = ((ComponentSampleModel) img.getSampleModel()).getScanlineStride();

		ByteBuffer bb = bitmap.buffer.getByteBuffer(0, Math.abs(bitmap.pitch) * bitmap.rows);
		int bbOff = (bitmap.pitch < 0) ? (-bitmap.pitch * (bitmap.rows - 1)) : 0;
		int dataOff = dataBuffer.getOffset() + y * stride + x;

		for (int r = 0; r < bitmap.rows; r++, bbOff += bitmap.pitch, dataOff += stride) {
			for (int c = 0; c < bitmap.width; c++) {
				data[dataOff + c] = (byte) (bb.get(bbOff + c) ^ 0xff);
			}
		}

		return true;
	}

	static boolean copyGlyphToBufferedImageIntAlpha(FT2Library.FT_Bitmap bitmap, BufferedImage img, int x, int y, Color color) {
		if (x + bitmap.width > img.getWidth()) {
			return false;
		}
		if (y + bitmap.rows > img.getHeight()) {
			return false;
		}

		final DataBufferInt dataBuffer = (DataBufferInt) img.getRaster().getDataBuffer();
		final int[] data = dataBuffer.getData();
		final int stride = ((SinglePixelPackedSampleModel) img.getSampleModel()).getScanlineStride();

		ByteBuffer bb = bitmap.buffer.getByteBuffer(0, Math.abs(bitmap.pitch) * bitmap.rows);
		int bbOff = (bitmap.pitch < 0) ? (-bitmap.pitch * (bitmap.rows - 1)) : 0;
		int dataOff = dataBuffer.getOffset() + y * stride + x;

		int colorValue = (color == null ? Color.WHITE : color).getRGB() & 0xFFFFFF;

		for (int r = 0; r < bitmap.rows; r++, bbOff += bitmap.pitch, dataOff += stride) {
			for (int c = 0; c < bitmap.width; c++) {
				data[dataOff + c] = colorValue | (bb.get(bbOff + c) << 24);
			}
		}

		return true;
	}

	static boolean copyGlyphToBufferedImageIntColor(FT2Library.FT_Bitmap bitmap, BufferedImage img, int x, int y) {
		if (x + bitmap.width > img.getWidth()) {
			return false;
		}
		if (y + bitmap.rows > img.getHeight()) {
			return false;
		}

		final DataBufferInt dataBuffer = (DataBufferInt) img.getRaster().getDataBuffer();
		final int[] data = dataBuffer.getData();
		final int stride = ((SinglePixelPackedSampleModel) img.getSampleModel()).getScanlineStride();

		ByteBuffer bb = bitmap.buffer.getByteBuffer(0, Math.abs(bitmap.pitch) * bitmap.rows);
		int bbOff = (bitmap.pitch < 0) ? (-bitmap.pitch * (bitmap.rows - 1)) : 0;
		int dataOff = dataBuffer.getOffset() + y * stride + x;

		for (int row = 0; row < bitmap.rows; row++, bbOff += bitmap.pitch, dataOff += stride) {
			for (int col = 0; col < bitmap.width / 3; col++) {
				int r = 255 - bb.get(bbOff + col * 3 + 0);
				int g = 255 - bb.get(bbOff + col * 3 + 1);
				int b = 255 - bb.get(bbOff + col * 3 + 2);
				int argb = 255 << 24 | r << 16 | g << 8 | b << 0;
				data[dataOff + col] = argb;
			}
		}

		return true;
	}

	static boolean copyGlyphToByteBuffer(FT2Library.FT_Bitmap bitmap, ByteBuffer dst, int stride) {
		ByteBuffer bb = bitmap.buffer.getByteBuffer(0, Math.abs(bitmap.pitch) * bitmap.rows);
		int bbOff = (bitmap.pitch < 0) ? (-bitmap.pitch * (bitmap.rows - 1)) : 0;
		int dstOff = dst.position();

		for (int r = 0; r < bitmap.rows; r++, bbOff += bitmap.pitch, dstOff += stride) {
			bb.clear().position(bbOff).limit(bbOff + bitmap.width);
			dst.position(dstOff);
			dst.put(bb);
		}

		return true;
	}

	static boolean copyGlyphToByteBuffer(FT2Library.FT_Bitmap bitmap, ByteBuffer dst, int stride, short[] colors) {
		ByteBuffer bb = bitmap.buffer.getByteBuffer(0, Math.abs(bitmap.pitch) * bitmap.rows);
		int bbOff = (bitmap.pitch < 0) ? (-bitmap.pitch * (bitmap.rows - 1)) : 0;
		int dstRowOff = dst.position();
		int width = bitmap.width;

		for (int r = 0; r < bitmap.rows; r++, bbOff += bitmap.pitch, dstRowOff += stride) {
			int dstOff = dstRowOff;
			for (int c = 0; c < width; c++) {
				int value = bb.get(bbOff + c) & 255;
				if (value >= 0x80) {
					value++;
				}
				for (int i = 0; i < colors.length; i += 2, dstOff++) {
					dst.put(dstOff, (byte) (colors[i] + ((colors[i + 1] * value) >> 8)));
				}
			}
			dst.position(dstOff);
		}

		return true;
	}

	static ByteBuffer inputStreamToByteBuffer(InputStream is) throws IOException {
		final int PAGE_SIZE = 4096;
		final ArrayList<byte[]> pages = new ArrayList<byte[]>();
		while (true) {
			byte[] page = new byte[PAGE_SIZE];
			int pagePos = 0;
			int read;
			do {
				read = is.read(page, pagePos, PAGE_SIZE - pagePos);
				if (read <= 0) {
					break;
				}
				pagePos += read;
			} while (pagePos < PAGE_SIZE);

			if (pagePos == PAGE_SIZE) {
				pages.add(page);
			} else {
				ByteBuffer fontBuffer = ByteBuffer.allocateDirect(pages.size() * PAGE_SIZE + pagePos);
				for (int i = 0, n = pages.size(); i < n; i++) {
					fontBuffer.put(pages.get(i));
				}
				pages.clear();
				fontBuffer.put(page, 0, pagePos).flip();
				return fontBuffer;
			}
		}
	}

	static final int FT_LOAD_TARGET(int mode) {
		return (mode & 15) << 16;
	}

}