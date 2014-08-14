package org.testobject.kernel.ocr.freetype;

import static org.testobject.kernel.ocr.freetype.FT2Helper.round26_6;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class FreeTypeFont implements Closeable
{

    /**
     * The maximum font file size for {@link #create(java.io.File) }
     */
    public static final int MAX_FONT_FILE_SIZE = 32 * 1024 * 1024;

    /** need to keep ByteBuffer alive - it is used by native code */
    ByteBuffer fontBuffer;
    FT2Library freetype;
    Pointer library;
    FT2Library.FT_Face face;

    private FreeTypeFont(FT2Library freetype, Pointer library, ByteBuffer file) throws FreeTypeException
    {
        this.fontBuffer = file;
        this.freetype = freetype;
        this.library = library;
        this.face = FT2Helper.FT_New_Memory_Face(freetype, library, file, 0);
    }

    public void close() throws IOException
    {
        close0();
    }

    public String getFamilyName() throws IOException
    {
        ensureOpen();
        return face.family_name;
    }

    public String getStyleName() throws IOException
    {
        ensureOpen();
        return face.style_name;
    }

    public void setCharSize(float width, float height, int horizontalResolution, int verticalResolution) throws IOException
    {
        ensureOpen();
        FT2Helper.checkReturnCode(freetype.FT_Set_Char_Size(face.getPointer(), FT2Helper.to26_6(width), FT2Helper.to26_6(height), horizontalResolution, verticalResolution));
        face.read();
    }

    public void setPixelSize(int width, int height) throws IOException
    {
        ensureOpen();
        FT2Helper.checkReturnCode(freetype.FT_Set_Pixel_Sizes(face.getPointer(), width, height));
        face.read();
    }

    public int getAscent() throws IOException
    {
        ensureOpen();
        if (face.isScalable())
        {
            return FT2Helper.round26_6(face.size.metrics.ascender);
        }
        else
        {
            return face.ascender;
        }
    }

    public int getDescent() throws IOException
    {
        ensureOpen();
        // match AWT sign
        if (face.isScalable())
        {
            return -FT2Helper.round26_6(face.size.metrics.descender);
        }
        else
        {
            return -face.descender;
        }
    }

    public int getMaxAscent() throws IOException
    {
        ensureOpen();
        return roundMaybeScaleY(face.bbox.yMax);
    }

    public int getMaxDescent() throws IOException
    {
        ensureOpen();
        return -roundMaybeScaleY(face.bbox.yMin);
    }

    public int getLineHeight() throws IOException
    {
        ensureOpen();
        if (face.isScalable())
        {
            return FT2Helper.round26_6(face.size.metrics.height);
        }
        else
        {
            return face.height;
        }
    }

    public int getLeading() throws IOException
    {
        ensureOpen();
        int height;
        if (face.isScalable())
        {
            height = FT2Helper.round26_6(face.size.metrics.height);
        }
        else
        {
            height = face.height;
        }
        return height - roundMaybeScaleY(face.bbox.yMax) + roundMaybeScaleY(face.bbox.yMin);
    }

    public int getUnderlinePosition() throws IOException
    {
        ensureOpen();
        return roundMaybeScaleY(face.underline_position);
    }

    public int getUnderlineThickness() throws IOException
    {
        ensureOpen();
        return roundMaybeScaleY(face.underline_thickness);
    }
    
    public void setLCDFilter(int flags) throws FreeTypeException
    {
        FT2Helper.checkReturnCode(freetype.FT_Library_SetLcdFilter(library, flags));
    }

    public int getGlyphForCodePoint(int codepoint) throws IOException
    {
        ensureOpen();
        return freetype.FT_Get_Char_Index(face.getPointer(), new NativeLong(codepoint));
    }

    public boolean hasKerning() throws IOException
    {
        ensureOpen();
        return face.hasKerning();
    }

    public Point getKerning(int leftGlyph, int rightGlyph) throws IOException
    {
        ensureOpen();
        if (face.hasKerning())
        {
            FT2Library.FT_Vector vec = new FT2Library.FT_Vector();
            vec.setAutoSynch(false);
            FT2Helper.checkReturnCode(freetype.FT_Get_Kerning(face.getPointer(), leftGlyph, rightGlyph, FT2Library.FT_KERNING_DEFAULT, vec));
            vec.read();
            return new Point(FT2Helper.round26_6(vec.x), FT2Helper.round26_6(vec.y));
        }
        else
        {
            return new Point();
        }
    }

    public FreeTypeGlyphInfo loadGlyph(int glyphIndex) throws IOException
    {
        ensureOpen();
        FT2Helper.checkReturnCode(freetype.FT_Load_Glyph(face.getPointer(), glyphIndex, FT2Library.FT_LOAD_RENDER));
        return makeGlyphInfo();
    }

    public FreeTypeGlyphInfo loadCodePoint(int codepoint) throws IOException
    {
        return loadCodePoint(codepoint, FT2Library.FT_LOAD_RENDER);
    }
    
    public FreeTypeGlyphInfo loadCodePoint(int codepoint, int flags) throws IOException
    {
        ensureOpen();
        FT2Helper.checkReturnCode(freetype.FT_Load_Char(face.getPointer(), new NativeLong(codepoint), flags));
        return makeGlyphInfo();
    }
    
    public FreeTypeGlyphInfo loadCodePoint(int codepoint, int loadFlags, int renderFlags) throws IOException
    {
        ensureOpen();
        FT2Helper.checkReturnCode(freetype.FT_Load_Char(face.getPointer(), new NativeLong(codepoint), loadFlags));
        FT2Helper.checkReturnCode(freetype.FT_Render_Glyph(face.glyph.getPointer(), renderFlags));
        return makeGlyphInfo();
    }    
    
    public boolean copyGlpyhToBufferedImageGray(BufferedImage img, int x, int y) throws IOException
    {
        ensureGlyphLoaded();
        FT2Library.FT_Bitmap bitmap = face.glyph.bitmap;
        if (bitmap.buffer == null)
        {
            return false;
        }
        if(img.getType() != BufferedImage.TYPE_BYTE_GRAY)
        {
            throw new IllegalArgumentException();
        }
        return FT2Helper.copyGlyphToBufferedImageGray(bitmap, img, x, y);
    }

    public boolean copyGlpyhToBufferedImageAlpha(BufferedImage img, int x, int y, Color color) throws IOException
    {
        ensureGlyphLoaded();
        FT2Library.FT_Bitmap bitmap = face.glyph.bitmap;
        if (bitmap.buffer == null)
        {
            return false;
        }
        if(img.getType() != BufferedImage.TYPE_INT_ARGB)
        {
            throw new IllegalArgumentException();
        }
        return FT2Helper.copyGlyphToBufferedImageIntAlpha(bitmap, img, x, y, color);
    }
    
    public boolean copyGlpyhToBufferedImageColor(BufferedImage img, int x, int y) throws IOException
    {
        ensureGlyphLoaded();
        FT2Library.FT_Bitmap bitmap = face.glyph.bitmap;
        if (bitmap.buffer == null)
        {
            return false;
        }
        if(img.getType() != BufferedImage.TYPE_INT_ARGB)
        {
            throw new IllegalArgumentException();
        }
        return FT2Helper.copyGlyphToBufferedImageIntColor(bitmap, img, x, y);
    }
    
    public boolean copyGlyphToByteBuffer(ByteBuffer dst, int stride) throws IOException
    {
        ensureGlyphLoaded();
        FT2Library.FT_Bitmap bitmap = face.glyph.bitmap;
        if (bitmap.buffer == null)
        {
            return false;
        }
        return FT2Helper.copyGlyphToByteBuffer(bitmap, dst, stride);
    }

    public boolean copyGlyphToByteBufferColor(ByteBuffer dst, int stride, byte[] bgColor, byte[] fgColor) throws IOException
    {
        ensureGlyphLoaded();

        if (bgColor.length != fgColor.length)
        {
            throw new IllegalArgumentException("color arrays must have same length");
        }

        short[] colors = new short[bgColor.length * 2];
        for (int i = 0; i < bgColor.length; i++)
        {
            int bg = bgColor[i] & 255;
            colors[i * 2 + 0] = (short) bg;
            colors[i * 2 + 1] = (short) ((fgColor[i] & 255) - bg);
        }

        FT2Library.FT_Bitmap bitmap = face.glyph.bitmap;
        if (bitmap.buffer == null)
        {
            return false;
        }
        return FT2Helper.copyGlyphToByteBuffer(bitmap, dst, stride, colors);
    }

    public static FreeTypeFont create(FT2Library freetype, ByteBuffer font) throws IOException
    {
        return new FreeTypeFont(freetype, FT2Helper.FT_Init_FreeType(freetype), font);
    }

    public static FreeTypeFont create(FT2Library freetype, File font) throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(font, "r");
        try
        {
            int size = (int) Math.min(MAX_FONT_FILE_SIZE, raf.length());
            ByteBuffer fontBuffer = ByteBuffer.allocateDirect(size);
            raf.getChannel().read(fontBuffer);
            fontBuffer.flip();
            return new FreeTypeFont(freetype, FT2Helper.FT_Init_FreeType(freetype), fontBuffer);
        }
        finally
        {
            raf.close();
        }
    }
    
    public static FreeTypeFace createFreeTypeFace(FT2Library freetype, File font) throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(font, "r");
        try
        {
            int size = (int) Math.min(MAX_FONT_FILE_SIZE, raf.length());
            ByteBuffer fontBuffer = ByteBuffer.allocateDirect(size);
            raf.getChannel().read(fontBuffer);
            fontBuffer.flip();
            return new FreeTypeFace(freetype, FT2Helper.FT_Init_FreeType(freetype), fontBuffer);
        }
        finally
        {
            raf.close();
        }
    }

    public static FreeTypeFont create(FT2Library freetype, InputStream font) throws IOException
    {
        ByteBuffer fontBuffer = FT2Helper.inputStreamToByteBuffer(font);
        return new FreeTypeFont(freetype, FT2Helper.FT_Init_FreeType(freetype), fontBuffer);
    }

    private FreeTypeGlyphInfo makeGlyphInfo()
    {
        face.glyph.read();
        return new FreeTypeGlyphInfo(face.glyph);
    }

    private int roundMaybeScaleY(NativeLong value)
    {
        return roundMaybeScaleY(value.longValue());
    }

    private int roundMaybeScaleY(long value)
    {
        if (face.isScalable())
        {
            value = FT2Helper.FT_FixMul(value, face.size.metrics.y_scale.longValue());
        }
        return FT2Helper.round26_6(value);
    }

    final void ensureOpen() throws IOException
    {
        if (library == null)
        {
            throw new ClosedChannelException();
        }
    }

    final void ensureGlyphLoaded() throws IOException
    {
        ensureOpen();
        if (face.glyph == null)
        {
            throw new IllegalStateException("No glyph loaded");
        }
    }

    private void close0() throws IOException
    {
        if (library != null)
        {
            int err = freetype.FT_Done_FreeType(library);
            library = null;
            face = null;
            fontBuffer = null;
            FT2Helper.checkReturnCode(err);
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        close0();
    }

	public Pointer getFace() {
		return face.getPointer();
	}
}