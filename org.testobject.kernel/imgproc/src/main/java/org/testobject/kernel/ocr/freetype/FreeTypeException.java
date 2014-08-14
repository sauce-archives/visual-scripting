package org.testobject.kernel.ocr.freetype;


/**
 * Encapsulates a FreeType2 error code.
 * 
 */
@SuppressWarnings("serial")
public class FreeTypeException extends RuntimeException
{    
    private final int errorCode;

    public FreeTypeException(int errorCode)
    {
        this.errorCode = errorCode;
    }

    /**
     * Returns the error message for the FreeType2 error code
     * @return the error message
     */
    @Override
    public String getMessage()
    {
        if (errorCode >= 0 && errorCode < ERROR_MESSAGES.length)
        {
            String msg = ERROR_MESSAGES[errorCode];
            if (msg != null)
            {
                return msg;
            }
        }
        return "unknown error code: " + errorCode;
    }

    /**
     * Returns the FreeType2 error code
     * @return the FreeType2 error code
     */
    public int getErrorCode()
    {
        return errorCode;
    }

    private static String[] ERROR_MESSAGES;
    static
    {
        ERROR_MESSAGES = new String[0xBA];

        ERROR_MESSAGES[0x01] = "cannot open resource";
        ERROR_MESSAGES[0x02] = "unknown file format";
        ERROR_MESSAGES[0x03] = "broken file";
        ERROR_MESSAGES[0x04] = "invalid FreeType version";
        ERROR_MESSAGES[0x05] = "module version is too low";
        ERROR_MESSAGES[0x06] = "invalid argument";
        ERROR_MESSAGES[0x07] = "unimplemented feature";
        ERROR_MESSAGES[0x08] = "broken table";
        ERROR_MESSAGES[0x09] = "broken offset within table";
        ERROR_MESSAGES[0x0A] = "array allocation size too large";

        ERROR_MESSAGES[0x10] = "invalid glyph index";
        ERROR_MESSAGES[0x11] = "invalid character code";
        ERROR_MESSAGES[0x12] = "unsupported glyph image format";
        ERROR_MESSAGES[0x13] = "cannot render this glyph format";
        ERROR_MESSAGES[0x14] = "invalid outline";
        ERROR_MESSAGES[0x15] = "invalid composite glyph";
        ERROR_MESSAGES[0x16] = "too many hints";
        ERROR_MESSAGES[0x17] = "invalid pixel size";

        ERROR_MESSAGES[0x20] = "invalid object handle";
        ERROR_MESSAGES[0x21] = "invalid library handle";
        ERROR_MESSAGES[0x22] = "invalid module handle";
        ERROR_MESSAGES[0x23] = "invalid face handle";
        ERROR_MESSAGES[0x24] = "invalid size handle";
        ERROR_MESSAGES[0x25] = "invalid glyph slot handle";
        ERROR_MESSAGES[0x26] = "invalid charmap handle";
        ERROR_MESSAGES[0x27] = "invalid cache manager handle";
        ERROR_MESSAGES[0x28] = "invalid stream handle";

        ERROR_MESSAGES[0x30] = "too many modules";
        ERROR_MESSAGES[0x31] = "too many extensions";

        ERROR_MESSAGES[0x40] = "out of memory";
        ERROR_MESSAGES[0x41] = "unlisted object";

        ERROR_MESSAGES[0x51] = "cannot open stream";
        ERROR_MESSAGES[0x52] = "invalid stream seek";
        ERROR_MESSAGES[0x53] = "invalid stream skip";
        ERROR_MESSAGES[0x54] = "invalid stream read";
        ERROR_MESSAGES[0x55] = "invalid stream operation";
        ERROR_MESSAGES[0x56] = "invalid frame operation";
        ERROR_MESSAGES[0x57] = "nested frame access";
        ERROR_MESSAGES[0x58] = "invalid frame read";

        ERROR_MESSAGES[0x60] = "raster uninitialized";
        ERROR_MESSAGES[0x61] = "raster corrupted";
        ERROR_MESSAGES[0x62] = "raster overflow";
        ERROR_MESSAGES[0x63] = "negative h while rastering";

        ERROR_MESSAGES[0x70] = "too many registered caches";

        ERROR_MESSAGES[0x80] = "invalid opcode";
        ERROR_MESSAGES[0x81] = "too few arguments";
        ERROR_MESSAGES[0x82] = "stack overflow";
        ERROR_MESSAGES[0x83] = "code overflow";
        ERROR_MESSAGES[0x84] = "bad argument";
        ERROR_MESSAGES[0x85] = "division by zero";
        ERROR_MESSAGES[0x86] = "invalid reference";
        ERROR_MESSAGES[0x87] = "found debug opcode";
        ERROR_MESSAGES[0x88] = "found ENDF opcode in execution stream";
        ERROR_MESSAGES[0x89] = "nested DEFS";
        ERROR_MESSAGES[0x8A] = "invalid code range";
        ERROR_MESSAGES[0x8B] = "execution context too long";
        ERROR_MESSAGES[0x8C] = "too many function definitions";
        ERROR_MESSAGES[0x8D] = "too many instruction definitions";
        ERROR_MESSAGES[0x8E] = "SFNT font table missing";
        ERROR_MESSAGES[0x8F] = "horizontal header (hhea) table missing";
        ERROR_MESSAGES[0x90] = "locations (loca) table missing";
        ERROR_MESSAGES[0x91] = "name table missing";
        ERROR_MESSAGES[0x92] = "character map (cmap) table missing";
        ERROR_MESSAGES[0x93] = "horizontal metrics (hmtx) table missing";
        ERROR_MESSAGES[0x94] = "PostScript (post) table missing";
        ERROR_MESSAGES[0x95] = "invalid horizontal metrics";
        ERROR_MESSAGES[0x96] = "invalid character map (cmap) format";
        ERROR_MESSAGES[0x97] = "invalid ppem value";
        ERROR_MESSAGES[0x98] = "invalid vertical metrics";
        ERROR_MESSAGES[0x99] = "could not find context";
        ERROR_MESSAGES[0x9A] = "invalid PostScript (post) table format";
        ERROR_MESSAGES[0x9B] = "invalid PostScript (post) table";

        ERROR_MESSAGES[0xA0] = "opcode syntax error";
        ERROR_MESSAGES[0xA1] = "argument stack underflow";
        ERROR_MESSAGES[0xA2] = "ignore";

        ERROR_MESSAGES[0xB0] = "`STARTFONT' field missing";
        ERROR_MESSAGES[0xB1] = "`FONT' field missing";
        ERROR_MESSAGES[0xB2] = "`SIZE' field missing";
        ERROR_MESSAGES[0xB3] = "`CHARS' field missing";
        ERROR_MESSAGES[0xB4] = "`STARTCHAR' field missing";
        ERROR_MESSAGES[0xB5] = "`ENCODING' field missing";
        ERROR_MESSAGES[0xB6] = "`BBX' field missing";
        ERROR_MESSAGES[0xB7] = "`BBX' too big";
        ERROR_MESSAGES[0xB8] = "Font header corrupted or missing fields";
        ERROR_MESSAGES[0xB9] = "Font glyphs corrupted or missing fields";
    }
}
