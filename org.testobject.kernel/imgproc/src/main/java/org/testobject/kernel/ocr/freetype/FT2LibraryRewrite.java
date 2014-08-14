package org.testobject.kernel.ocr.freetype;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * 
 * @author enijkamp
 *
 */
public interface FT2LibraryRewrite {
	
	interface FT242 extends Library {
		
		class Wrapper {
			
			static class Handler implements InvocationHandler {
				
				private final FT242 proxee;
				private final Map<String, Method> methods = new HashMap<>();
				
				public Handler(FT242 proxee) {
					this.proxee = proxee;
					for(Method method : proxee.getClass().getMethods()) {
						methods.put(method.getName().replace("TO242_", "FT_"), method);
					}
				}

				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					
					if(methods.containsKey(method.getName()) == false) {
						throw new IllegalArgumentException(method.getName());
					}
					
					return methods.get(method.getName()).invoke(proxee, args);
				}
			}
			
			public static FT2Library wrap(final FT242 proxee) {
				return (FT2Library) Proxy.newProxyInstance(FT2Library.class.getClassLoader(), 
						new Class<?>[]{ FT2Library.class }, new Handler(proxee));
			}
		}
		
	    int TO242_Init_FreeType(PointerByReference alibrary);

	    int TO242_Done_FreeType(Pointer library);

	    void TO242_Library_Version(Pointer library, IntByReference amajor, IntByReference aminor, IntByReference apatch);

	    int TO242_Get_TrueType_Engine_Type(Pointer library);

	    int TO242_New_Memory_Face(Pointer library, ByteBuffer file_base, NativeLong file_size, NativeLong face_index, PointerByReference aface);

	    int TO242_Library_SetLcdFilter(Pointer library, int filter);
	    
	    int TO242_Done_Face(Pointer face);

	    int TO242_Set_Char_Size(Pointer face, int char_width, int char_height, int horz_resolution, int vert_resolution);

	    int TO242_Set_Pixel_Sizes(Pointer face, int pixel_width, int pixel_height);

	    int TO242_Load_Glyph(Pointer face, int glyph_index, int load_flags);

	    int TO242_Load_Char(Pointer face, NativeLong char_index, int load_flags);
	  
	    int TO242_Render_Glyph(Pointer pointer, int render_mode);

	    int TO242_Get_Kerning(Pointer face, int leTO_glyph, int right_glyph, int kern_mode, FT2Library.FT_Vector akerning);

	    int TO242_Get_Char_Index(Pointer face, NativeLong char_code);

	    NativeLong TO242_Get_First_Char(Pointer face, IntByReference agindex);

	    NativeLong TO242_Get_Next_Char(Pointer face, NativeLong char_code, IntByReference agindex);

	}
	
	interface FT244 extends Library {
		
		class Wrapper {
			
			private static class Handler implements InvocationHandler {
				
				private final FT244 proxee;
				private final Map<String, Method> methods = new HashMap<>();
				
				public Handler(FT244 proxee) {
					this.proxee = proxee;
					for(Method method : proxee.getClass().getMethods()) {
						methods.put(method.getName().replace("TO244_", "FT_"), method);
					}
				}

				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					
					if(methods.containsKey(method.getName()) == false) {
						throw new IllegalArgumentException(method.getName());
					}
					
					return methods.get(method.getName()).invoke(proxee, args);
				}
			}
			
			public static FT2Library wrap(final FT244 proxee) {
				return (FT2Library) Proxy.newProxyInstance(FT2Library.class.getClassLoader(), 
						new Class<?>[]{ FT2Library.class }, new Handler(proxee));
			}
		}
		
	    int TO244_Init_FreeType(PointerByReference alibrary);

	    int TO244_Done_FreeType(Pointer library);

	    void TO244_Library_Version(Pointer library, IntByReference amajor, IntByReference aminor, IntByReference apatch);

	    int TO244_Get_TrueType_Engine_Type(Pointer library);

	    int TO244_New_Memory_Face(Pointer library, ByteBuffer file_base, NativeLong file_size, NativeLong face_index, PointerByReference aface);

	    int TO244_Library_SetLcdFilter(Pointer library, int filter);
	    
	    int TO244_Done_Face(Pointer face);

	    int TO244_Set_Char_Size(Pointer face, int char_width, int char_height, int horz_resolution, int vert_resolution);

	    int TO244_Set_Pixel_Sizes(Pointer face, int pixel_width, int pixel_height);

	    int TO244_Load_Glyph(Pointer face, int glyph_index, int load_flags);

	    int TO244_Load_Char(Pointer face, NativeLong char_index, int load_flags);
	  
	    int TO244_Render_Glyph(Pointer pointer, int render_mode);

	    int TO244_Get_Kerning(Pointer face, int leTO_glyph, int right_glyph, int kern_mode, FT2Library.FT_Vector akerning);

	    int TO244_Get_Char_Index(Pointer face, NativeLong char_code);

	    NativeLong TO244_Get_First_Char(Pointer face, IntByReference agindex);

	    NativeLong TO244_Get_Next_Char(Pointer face, NativeLong char_code, IntByReference agindex);

	}
	
	interface FT248 extends Library {
		
		class Wrapper {
			
			private static class Handler implements InvocationHandler {
				
				private final FT248 proxee;
				private final Map<String, Method> methods = new HashMap<>();
				
				public Handler(FT248 proxee) {
					this.proxee = proxee;
					for(Method method : proxee.getClass().getMethods()) {
						methods.put(method.getName().replace("TO248_", "FT_"), method);
					}
				}

				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					
					if(methods.containsKey(method.getName()) == false) {
						throw new IllegalArgumentException(method.getName());
					}
					
					return methods.get(method.getName()).invoke(proxee, args);
				}
			}
			
			public static FT2Library wrap(final FT248 proxee) {
				return (FT2Library) Proxy.newProxyInstance(FT2Library.class.getClassLoader(), 
						new Class<?>[]{ FT2Library.class }, new Handler(proxee));
			}
		}
		
	    int TO248_Init_FreeType(PointerByReference alibrary);

	    int TO248_Done_FreeType(Pointer library);

	    void TO248_Library_Version(Pointer library, IntByReference amajor, IntByReference aminor, IntByReference apatch);

	    int TO248_Get_TrueType_Engine_Type(Pointer library);

	    int TO248_New_Memory_Face(Pointer library, ByteBuffer file_base, NativeLong file_size, NativeLong face_index, PointerByReference aface);

	    int TO248_Library_SetLcdFilter(Pointer library, int filter);
	    
	    int TO248_Done_Face(Pointer face);

	    int TO248_Set_Char_Size(Pointer face, int char_width, int char_height, int horz_resolution, int vert_resolution);

	    int TO248_Set_Pixel_Sizes(Pointer face, int pixel_width, int pixel_height);

	    int TO248_Load_Glyph(Pointer face, int glyph_index, int load_flags);

	    int TO248_Load_Char(Pointer face, NativeLong char_index, int load_flags);
	  
	    int TO248_Render_Glyph(Pointer pointer, int render_mode);

	    int TO248_Get_Kerning(Pointer face, int leTO_glyph, int right_glyph, int kern_mode, FT2Library.FT_Vector akerning);

	    int TO248_Get_Char_Index(Pointer face, NativeLong char_code);

	    NativeLong TO248_Get_First_Char(Pointer face, IntByReference agindex);

	    NativeLong TO248_Get_Next_Char(Pointer face, NativeLong char_code, IntByReference agindex);

	}

}
