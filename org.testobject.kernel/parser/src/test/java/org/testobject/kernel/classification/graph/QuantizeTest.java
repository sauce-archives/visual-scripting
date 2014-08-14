package org.testobject.kernel.classification.graph;

import java.io.IOException;

import org.junit.Test;
import org.testobject.commons.tools.plot.VisualizerUtil;
import org.testobject.commons.util.config.Debug;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.imaging.color.contrast.Quantize;

/**
 * 
 * @author enijkamp
 *
 */
public class QuantizeTest {
	
    public static final boolean debug = Debug.toDebugMode(false);

    @Test
    public void quantize() throws IOException {
        Image.Int raw = ImageUtil.Read.read(FileUtil.readFileFromClassPath("android/4_0_3/screenshots/emulator/astro/home.png"));
        // Image.Int raw = ImageUtil.Read.read(FileUtil.readFileFromClassPath("icon/komoot.png"));
        for(int n = 2; n < 8; n++) {
            Image.Int quant = Quantize.quantize(raw, n);
            if(debug) VisualizerUtil.show("" + n, quant);
        }
        if(debug) System.in.read();
    }
}
