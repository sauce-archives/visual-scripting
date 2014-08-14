package org.testobject.kernel.ocr.freetype;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Iterates over all codepoint of a font
 *
 */
public final class FreeTypeCodePointIterator
{
	private FT2Library freetype;
    private final FreeTypeFont font;
    private final Pointer face;
    private final IntByReference glyphIndex;
    private NativeLong codePoint;

    FreeTypeCodePointIterator(FT2Library freetype, FreeTypeFont font)
    {
    	this.freetype = freetype;
        this.font = font;
        this.face = font.face.getPointer();
        this.glyphIndex = new IntByReference();
    }

    /**
     * Fetch the next codepoint and glyph index
     *
     * @return true if the iterator has more codepoints.
     * @throws IOException if an error has occured.
     */
    public boolean nextCodePoint() throws IOException
    {
        font.ensureOpen();

        if (codePoint == null)
        {
            codePoint = freetype.FT_Get_First_Char(face, glyphIndex);
        }
        else if (glyphIndex.getValue() == 0)
        {
            return false;
        }
        else
        {
            codePoint = freetype.FT_Get_Next_Char(face, codePoint, glyphIndex);
        }

        return glyphIndex.getValue() != 0;
    }

    /**
     * Returns the glyph index for the current code point.
     * Different code points may use the same glyph.
     *
     * @return the glyph index.
     */
    public int getGlyphIndex()
    {
        ensureGlyphIndex();
        return glyphIndex.getValue();
    }

    /**
     * Returns the current unicode codepoint.
     * @return the unicode codepoint
     */
    public int getCodePoint()
    {
        ensureGlyphIndex();
        return codePoint.intValue();
    }

    private void ensureGlyphIndex()
    {
        if (glyphIndex.getValue() == 0)
        {
            throw new NoSuchElementException();
        }
    }
}
