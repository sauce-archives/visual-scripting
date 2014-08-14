package org.testobject.kernel.ocr.freetype;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.jna.Pointer;

public class FreeTypeFace implements Closeable {

	private FT2Library freetype;
	private Pointer library;
	private FT2Library.FT_Face face;

	public FreeTypeFace(FT2Library freetype, Pointer library, ByteBuffer file) throws FreeTypeException {
		this.freetype = freetype;
		this.library = library;
		this.face = FT2Helper.FT_New_Memory_Face(freetype, library, file, 0);
	}
	
    public void close() throws IOException
    {
        close0();
    }
    
    public FT2Library.FT_Face getFace() {
		return face;
	}
    
    private void close0() throws IOException
    {
    	freetype.FT_Done_Face(face.getPointer());
        if (library != null)
        {
            int err = freetype.FT_Done_FreeType(library);
            freetype = null;
            library = null;
            face = null;
            FT2Helper.checkReturnCode(err);
        }
    }

}
