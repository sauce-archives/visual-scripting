package org.testobject.kernel.ocr.cairo;

import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * 
 * http://www.google.com/codesearch#9OZVrqQu3Xg/trunk/googleclient/third_party/cairo/src/cairo.h
 * http://research.operationaldynamics.com/bzr/java-gnome/mainline/src/bindings/org/freedesktop/cairo/
 * 
 * @author nijkamp
 * 
 */
public class Cairo
{
    private Cairo()
    {
    }

    static
    {
        Native.register("libcairo.so.2");
    }

    public interface cairo_operator_t
    {
        int CAIRO_OPERATOR_CLEAR = 0;

        int CAIRO_OPERATOR_SOURCE = 1;
        int CAIRO_OPERATOR_OVER = 2;
        int CAIRO_OPERATOR_IN = 3;
        int CAIRO_OPERATOR_OUT = 4;
        int CAIRO_OPERATOR_ATOP = 5;

        int CAIRO_OPERATOR_DEST = 6;
        int CAIRO_OPERATOR_DEST_OVER = 7;
        int CAIRO_OPERATOR_DEST_IN = 8;
        int CAIRO_OPERATOR_DEST_OUT = 9;
        int CAIRO_OPERATOR_DEST_ATOP = 10;

        int CAIRO_OPERATOR_XOR = 11;
        int CAIRO_OPERATOR_ADD = 12;
        int CAIRO_OPERATOR_SATURATE = 13;
    }

    public interface cairo_pattern_type_t
    {
        int CAIRO_PATTERN_TYPE_SOLID = 0;
        int CAIRO_PATTERN_TYPE_SURFACE = 1;
        int CAIRO_PATTERN_TYPE_LINEAR = 2;
        int CAIRO_PATTERN_TYPE_RADIAL = 3;
    }

    public interface cairo_filter_t
    {
        int CAIRO_FILTER_FAST = 0;
        int CAIRO_FILTER_GOOD = 1;
        int CAIRO_FILTER_BEST = 2;
        int CAIRO_FILTER_NEAREST = 3;
        int CAIRO_FILTER_BILINEAR = 4;
        int CAIRO_FILTER_GAUSSIAN = 5;
    }
    
    public interface cairo_format_t
    {
        int CAIRO_FORMAT_ARGB32 = 0;
        int CAIRO_FORMAT_RGB24 = 1;
        int CAIRO_FORMAT_A8 = 2;
        int CAIRO_FORMAT_A1 = 3;
    }
    
    public interface cairo_font_slant_t
    {
    	int CAIRO_FONT_SLANT_NORMAL = 0;
    	int CAIRO_FONT_SLANT_ITALIC = 1;
    	int CAIRO_FONT_SLANT_OBLIQUE = 2;
    }
    
    public interface cairo_font_weight_t
    {
    	int CAIRO_FONT_WEIGHT_NORMAL = 0;
    	int CAIRO_FONT_WEIGHT_BOLD = 1;
    }
    
    public interface cairo_subpixel_order_t
    {
    	int CAIRO_SUBPIXEL_ORDER_DEFAULT = 0;
    	int CAIRO_SUBPIXEL_ORDER_RGB = 1;
    	int CAIRO_SUBPIXEL_ORDER_BGR = 2;
    	int CAIRO_SUBPIXEL_ORDER_VRGB = 3;
    	int CAIRO_SUBPIXEL_ORDER_VBGR = 4;
    }
    
    public interface cairo_antialias_t
    {
    	int CAIRO_ANTIALIAS_DEFAULT = 0;
    	int CAIRO_ANTIALIAS_NONE = 1;
    	int CAIRO_ANTIALIAS_GRAY = 2;
    	int CAIRO_ANTIALIAS_SUBPIXEL = 3;
    }
    
    public interface cairo_hint_style_t
    {
    	int CAIRO_HINT_STYLE_DEFAULT = 0;
    	int CAIRO_HINT_STYLE_NONE = 1;
    	int CAIRO_HINT_STYLE_SLIGHT = 2;
    	int CAIRO_HINT_STYLE_MEDIUM = 3;
    	int CAIRO_HINT_STYLE_FULL = 4;
    }

    /* >>>>> image <<<<< */
    public static native Pointer cairo_create(Pointer target);
    
    public static native void cairo_set_source_rgba(Pointer cr, double red, double green, double blue, double alpha);
    
    public static native void cairo_set_source_rgb(Pointer cr, double red, double green, double blue);

    public static native void cairo_set_operator(Pointer cr, int op);

    public static native void cairo_paint(Pointer cr);

    public static native void cairo_paint_with_alpha(Pointer cr, double alpha);

    public static native void cairo_destroy(Pointer cr);

    public static native void cairo_arc_negative(Pointer cr, double xc, double yc, double radious, double angle1, double angle2);

    public static native void cairo_save(Pointer cr);

    public static native void cairo_restore(Pointer cr);

    public static native void cairo_translate(Pointer cr, double tx, double ty);

    public static native void cairo_scale(Pointer cr, double sx, double sy);

    public static native void cairo_rotate(Pointer cr, double angle);

    public static native void cairo_fill(Pointer cr);

    public static native void cairo_rectangle(Pointer cr, double x, double y, double width, double height);

    public static native void cairo_stroke(Pointer cr);

    public static native void cairo_set_line_width(Pointer cr, double width);

    public static native void cairo_clip(Pointer cr);

    public static native void cairo_pattern_destroy(Pointer pattern);

    public static native void cairo_set_source(Pointer cr, Pointer source);

    public static native void cairo_pattern_set_filter(Pointer pattern, int filter);

    public static native Pointer cairo_pattern_create_for_surface(Pointer surface);

    public static native Pointer cairo_image_surface_create_for_data(Pointer data, int format, int width, int height, int stride);
    
    public static native Pointer cairo_image_surface_create(int format, int width, int height);

    public static native void cairo_surface_destroy(Pointer surface);
    
    public static native int cairo_surface_write_to_png(Pointer surface, String filename);
    
    public static native void cairo_move_to(Pointer cr, double x, double y);
    
    /* >>>>> fonts <<<<< */
    
    public static native void cairo_set_font_face(Pointer cr, Pointer font_face);
    
    public static native Pointer cairo_get_font_face();
    
    public static native void cairo_select_font_face(Pointer cr, String family, int slant, int weight);
    
    public static native void cairo_set_font_size(Pointer cr, double size);
    
    public static native void cairo_show_text(Pointer cr, String text);
    
    public static native Pointer cairo_font_options_create();
    
    public static native void cairo_set_font_options(Pointer cr, Pointer options);
    
    public static native void cairo_font_options_set_subpixel_order(Pointer options, int subpixel_order);
    
    public static native void cairo_font_options_set_antialias(Pointer options, int antialias);
    
    public static native void cairo_font_options_set_hint_style(Pointer options, int hint_style);
    
    public static native void cairo_surface_get_font_options(Pointer surface, Pointer options);
    
    public static native void cairo_font_options_destroy(Pointer options);
    
    /* >>>>>> freetype <<<<<< */
    
    public static native Pointer cairo_ft_font_face_create_for_ft_face (Pointer ft_face, int load_flags);


    /* >>>>> xlib <<<<< */
    public static native Pointer cairo_xlib_surface_create(Pointer dpy, Pointer drawable, Pointer visual, int width, int height);
}
