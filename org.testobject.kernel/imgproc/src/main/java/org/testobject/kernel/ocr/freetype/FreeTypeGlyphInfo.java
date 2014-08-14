package org.testobject.kernel.ocr.freetype;

import static org.testobject.kernel.ocr.freetype.FT2Helper.round26_6;

public class FreeTypeGlyphInfo
{

    final int width;
    final int height;
    final int offsetX;
    final int offsetY;
    final int advanceX;
    final int advanceY;

    FreeTypeGlyphInfo(FT2Library.FT_GlyphSlot slot)
    {
        if (slot.format == FT2Library.FT_GLYPH_FORMAT_BITMAP)
        {
            this.width = slot.bitmap.width;
            this.height = slot.bitmap.rows;
            this.offsetX = slot.bitmap_left;
            this.offsetY = slot.bitmap_top;
        }
        else
        {
            this.width = 0;
            this.height = 0;
            this.offsetX = 0;
            this.offsetY = 0;
        }

        this.advanceX = FT2Helper.round26_6(slot.advance.x);
        this.advanceY = FT2Helper.round26_6(slot.advance.y);
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getOffsetX()
    {
        return offsetX;
    }

    public int getOffsetY()
    {
        return offsetY;
    }

    public int getAdvanceX()
    {
        return advanceX;
    }

    public int getAdvanceY()
    {
        return advanceY;
    }
}
