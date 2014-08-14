package org.testobject.kernel.ocr.cairo;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public class Glib
{
    private Glib()
    {
    }

    static
    {
        Native.register("libglibmm-2.4.so.1");
    }
    
    public static native Pointer g_malloc(NativeLong n_bytes);
    
    public static native void g_free(Pointer object);
}
