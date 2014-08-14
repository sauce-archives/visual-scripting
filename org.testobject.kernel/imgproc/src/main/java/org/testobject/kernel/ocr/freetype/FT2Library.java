package org.testobject.kernel.ocr.freetype;

import java.nio.ByteBuffer;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/* 
 * mappings: gnome2 -> fontconfig -> cairo -> freetype2
 * 
 * read 
 *    http://www.freetype.org/freetype2/docs/reference/ft2-base_interface.html#FT_LOAD_TARGET_XXX
 *
 *
 * source 
 *    1) apt-get source gnome-control-center
 *    2) http://www.google.com/codesearch/p?hl=en#9OZVrqQu3Xg/trunk/googleclient/third_party/cairo/src/cairo-ft-font.c  
 *    
 *    a) capplets/appearance/appearance-font.c -> cb_show_details -> open_pattern  =>  HINT_SLIGHT = FC_HINT_SLIGHT 
 *    b) cairo.h -> cairo-ft-font.c                                                =>  FC_HINT_SLIGHT = CAIRO_HINT_STYLE_SLIGHT
 * 
 * 
 * smoothing      ->  FT_Render_Mode
 *   
 *   ANTIALIAS_NONE       =  FC_RGBA_NONE                        =  CAIRO_ANTIALIAS_NONE     = FT_RENDER_MODE_MONO
 *   ANTIALIAS_GRAYSCALE  =  FC_ANTIALIAS = true                 =  CAIRO_ANTIALIAS_SUBPIXEL = FT_RENDER_MODE_NORMAL
 *   ANTIALIAS_RGBA       =  FC_ANTIALIAS = true, FC_RGBA_RGB    =  CAIRO_SUBPIXEL_ORDER*    = FT_RENDER_MODE_LCD*
 * 
 * hinting        ->  FT_LOAD_TARGET_XXX
 * 
 *   HINT_NONE    =  FC_HINT_NONE    =  CAIRO_HINT_STYLE_NONE    =  FT_LOAD_NO_HINTING
 *   HINT_SLIGHT  =  FC_HINT_SLIGHT  =  CAIRO_HINT_STYLE_SLIGHT  =  FT_LOAD_TARGET_LIGHT
 *   HINT_MEDIUM  =  FC_HINT_MEDIUM  =  CAIRO_HINT_STYLE_MEDIUM  =  FT_LOAD_TARGET_LCD | FT_LOAD_TARGET_LCD_V
 *   HINT_FULL    =  FC_HINT_FULL    =  CAIRO_HINT_STYLE_FULL    =  FT_LOAD_TARGET_LCD | FT_LOAD_TARGET_LCD_V
 *   
 * subpixel order ->  FT_LOAD_TARGET_XXX
 * 
 *   RGBA_RGB    =  FC_RGBA_RGB   =  CAIRO_SUBPIXEL_ORDER_RGB    =  FT_LOAD_TARGET_LCD
 *   RGBA_BGR    =  FC_RGBA_BGR   =  CAIRO_SUBPIXEL_ORDER_BGR    =  FT_LOAD_TARGET_LCD
 *   RGBA_VRGB   =  FC_RGBA_VRGB  =  CAIRO_SUBPIXEL_ORDER_VRGB   =  FT_LOAD_TARGET_LCD_V
 *   RGBA_VBGR   =  FC_RGBA_VBGR  =  CAIRO_SUBPIXEL_ORDER_VBGR   =  FT_LOAD_TARGET_LCD_V
 * 
 */
public interface FT2Library extends Library
{
    class FT_Vector extends Structure
    {
    	public static class ByReference extends FT_Vector implements Structure.ByReference
        {
        }

        public NativeLong x;
        public NativeLong y;
    }

    class FT_BBox extends Structure
    {
        public NativeLong xMin;
        public NativeLong yMin;
        public NativeLong xMax;
        public NativeLong yMax;
    }

    class FT_Glyph_Metrics extends Structure
    {
        public NativeLong width;
        public NativeLong height;

        public NativeLong horiBearingX;
        public NativeLong horiBearingY;
        public NativeLong horiAdvance;

        public NativeLong vertBearingX;
        public NativeLong vertBearingY;
        public NativeLong vertAdvance;
    }

    interface FT_Generic_Finalizer extends Callback
    {
        public void invoke(Pointer object);
    }

    class FT_Generic extends Structure
    {
        public Pointer data;
        public FT_Generic_Finalizer finalizer;
    }

    class FT_Bitmap extends Structure
    {
        public int rows;
        public int width;
        public int pitch;
        public Pointer buffer;
        public short num_grays;
        public byte pixel_mode;
        public byte palette_mode;
        public Pointer palette;
    }

    class FT_Size_Metrics extends Structure
    {
        public short x_ppem; /* horizontal pixels per EM               */
        public short y_ppem; /* vertical pixels per EM                 */
        public NativeLong x_scale; /* scaling values used to convert font    */
        public NativeLong y_scale; /* units to 26.6 fractional pixels        */
        public NativeLong ascender; /* ascender in 26.6 frac. pixels          */
        public NativeLong descender; /* descender in 26.6 frac. pixels         */
        public NativeLong height; /* text h in 26.6 frac. pixels       */
        public NativeLong max_advance; /* max horizontal advance, in 26.6 pixels */
    }

    class FT_Size extends Structure
    {
    	public static class ByReference extends FT_Size implements Structure.ByReference
        {
        }

        public FT_Face.ByReference face; /* parent face object              */
        public FT_Generic generic; /* generic pointer for client uses */
        public FT_Size_Metrics metrics; /* size metrics                    */
        public Pointer internal;
    }

    class FT_Outline extends Structure
    {
        public short n_contours; /* number of contours in glyph        */
        public short n_points; /* number of points in the glyph      */

        public Pointer points; /* the outline's points               */
        public Pointer tags; /* the points flags                   */
        public Pointer contours; /* the contour end points             */

        public int flags; /* outline masks                      */
    }

    class FT_GlyphSlot extends Structure
    {
        public static class ByReference extends FT_GlyphSlot implements Structure.ByReference
        {
        }

        public Pointer library;
        public Pointer face;
        public Pointer next;
        public int reserved; /* retained for binary compatibility */
        public FT_Generic generic;

        public FT_Glyph_Metrics metrics;
        public NativeLong linearHoriAdvance;
        public NativeLong linearVertAdvance;
        public FT_Vector advance;
        public int format;

        public FT_Bitmap bitmap;
        public int bitmap_left;
        public int bitmap_top;

        public FT_Outline outline;

        public int num_subglyphs;
        public Pointer subglyphs;

        public Pointer control_data;
        public NativeLong control_len;

        public NativeLong lsb_delta;
        public NativeLong rsb_delta;

        public Pointer other;
        public Pointer internal;
    }

    class FT_CharMap extends Structure
    {
    	public static class ByReference extends FT_CharMap implements Structure.ByReference
        {
        }

        public FT_Face.ByReference face;
        public int encoding;
        public short platform_id;
        public short encoding_id;
    }

    class FT_Face extends Structure
    {
    	public static class ByReference extends FT_Face implements Structure.ByReference
        {
        }

        public FT_Face()
        {
        }

        public FT_Face(Pointer p)
        {
            super(p);
            read();
            setAutoSynch(false);
        }

        public NativeLong num_faces;
        public NativeLong face_index;
        public NativeLong face_flags;
        public NativeLong style_flags;
        public NativeLong num_glyphs;

        public String family_name;
        public String style_name;

        public int num_fixed_sizes;
        public Pointer available_sizes; /* FT_Bitmap_Size* */
        public int num_charmaps;
        public Pointer charmaps; /* FT_CharMap* */

        public FT_Generic generic;

        /*# The following member variables (down to `underline_thickness') */
        /*# are only relevant to scalable outlines; cf. @FT_Bitmap_Size    */
        /*# for bitmap fonts.                                              */
        public FT_BBox bbox;

        public short units_per_EM;
        public short ascender;
        public short descender;
        public short height;
        public short max_advance_width;
        public short max_advance_height;
        public short underline_position;
        public short underline_thickness;

        public FT_GlyphSlot.ByReference glyph;
        public FT_Size.ByReference size;
        public FT_CharMap.ByReference charmap;

        public boolean isScalable()
        {
            return (face_flags.intValue() & FT_FACE_FLAG_SCALABLE) != 0;
        }

        public boolean hasKerning()
        {
            return (face_flags.intValue() & FT_FACE_FLAG_KERNING) != 0;
        }
    }

    int FT_FACE_FLAG_SCALABLE = 1;
    int FT_FACE_FLAG_FIXED_SIZES = 1 << 1;
    int FT_FACE_FLAG_FIXED_WIDTH = 1 << 2;
    int FT_FACE_FLAG_SFNT = 1 << 3;
    int FT_FACE_FLAG_HORIZONTAL = 1 << 4;
    int FT_FACE_FLAG_VERTICAL = 1 << 5;
    int FT_FACE_FLAG_KERNING = 1 << 6;
    int FT_FACE_FLAG_FAST_GLYPHS = 1 << 7;
    int FT_FACE_FLAG_MULTIPLE_MASTERS = 1 << 8;
    int FT_FACE_FLAG_GLYPH_NAMES = 1 << 9;
    int FT_FACE_FLAG_EXTERNAL_STREAM = 1 << 10;
    int FT_FACE_FLAG_HINTER = 1 << 11;

    int FT_KERNING_DEFAULT = 0; 
   
    int FT_LOAD_DEFAULT = 0;
    int FT_LOAD_RENDER  = 4;

    int FT_GLYPH_FORMAT_BITMAP = FT2Helper.FT_IMAGE_TAG('b', 'i', 't', 's');

    int FT_TRUETYPE_ENGINE_TYPE_NONE       = 0;
    int FT_TRUETYPE_ENGINE_TYPE_UNPATENTED = 1;
    int FT_TRUETYPE_ENGINE_TYPE_PATENTED   = 2;

    int FT_LCD_FILTER_NONE    = 0;
    int FT_LCD_FILTER_DEFAULT = 1;
    int FT_LCD_FILTER_LIGHT   = 2;
    int FT_LCD_FILTER_LEGACY  = 16;
    
    int FT_RENDER_MODE_NORMAL = 0;
    int FT_RENDER_MODE_LIGHT  = 1;
    int FT_RENDER_MODE_MONO   = 2;
    int FT_RENDER_MODE_LCD    = 3;
    int FT_RENDER_MODE_LCD_V  = 4;
    
    int FT_LOAD_TARGET_LCD   = FT2Helper.FT_LOAD_TARGET(FT_RENDER_MODE_LCD);
    int FT_LOAD_TARGET_LIGHT = FT2Helper.FT_LOAD_TARGET(FT_RENDER_MODE_LIGHT);
    int FT_LOAD_TARGET_MONO  = FT2Helper.FT_LOAD_TARGET(FT_RENDER_MODE_MONO);
    
    
    int FT_Init_FreeType(PointerByReference alibrary);

    int FT_Done_FreeType(Pointer library);

    void FT_Library_Version(Pointer library, IntByReference amajor, IntByReference aminor, IntByReference apatch);

    int FT_Get_TrueType_Engine_Type(Pointer library);

    int FT_New_Memory_Face(Pointer library, ByteBuffer file_base, NativeLong file_size, NativeLong face_index, PointerByReference aface);

    int FT_Library_SetLcdFilter(Pointer library, int filter);
    
    int FT_Done_Face(Pointer face);

    int FT_Set_Char_Size(Pointer face, int char_width, int char_height, int horz_resolution, int vert_resolution);

    int FT_Set_Pixel_Sizes(Pointer face, int pixel_width, int pixel_height);

    int FT_Load_Glyph(Pointer face, int glyph_index, int load_flags);

    int FT_Load_Char(Pointer face, NativeLong char_index, int load_flags);
  
    int FT_Render_Glyph(Pointer pointer, int render_mode);

    int FT_Get_Kerning(Pointer face, int left_glyph, int right_glyph, int kern_mode, FT_Vector akerning);

    int FT_Get_Char_Index(Pointer face, NativeLong char_code);

    NativeLong FT_Get_First_Char(Pointer face, IntByReference agindex);

    NativeLong FT_Get_Next_Char(Pointer face, NativeLong char_code, IntByReference agindex);

}
