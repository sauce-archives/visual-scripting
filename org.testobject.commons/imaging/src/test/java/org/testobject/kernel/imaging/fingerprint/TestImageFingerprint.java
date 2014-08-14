package org.testobject.kernel.imaging.fingerprint;

import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;
import org.testobject.kernel.imaging.fingerprint.ImageFingerprint;
import org.testobject.kernel.imaging.fingerprint.Scale;

import javax.imageio.ImageIO;

public class TestImageFingerprint
{    
    public static void main(String ... av) throws Exception
    {
        Image.Int image11 = ImageUtil.Convert.toImageInt(ImageIO.read(FileUtil.readFileFromClassPath("samples/icon11.png")));
        Image.Int image34 = ImageUtil.Convert.toImageInt(ImageIO.read(FileUtil.readFileFromClassPath("samples/icon11.png")));

        ImageFingerprint f11 = new ImageFingerprint(image11, 0xf2, 0xf1, 0xf0);
        ImageFingerprint f34 = new ImageFingerprint(image34, 0xf2, 0xf1, 0xf0);
        
        show(f11.restoreImage());
        show(f34.restoreImage());
        
        System.out.println("chroma mass 2: " + f11.chromaMass2());
        System.out.println("chroma mass L1: " + f11.chromaMassL1());

        System.out.println("chroma mass 2: " + f34.chromaMass2());
        System.out.println("chroma mass L1: " + f34.chromaMassL1());
    }
    
    public static void show(Image.Double image)
    {
        Image.Int out = new Image.Int(image.w, image.h, Image.Int.Type.RGB);
        
        Scale scale = Scale.of(image.pixels);
        
        for(int i = 0; i < image.h; i++)
        {
            for(int j = 0; j < image.w; j++)
            {
                double r = scale.normalize(image.get(j,i)) * 255.0;
                
                int rr = (int) r;
                
                int rgb = 0xff000000 + (rr << 16) + (rr << 8) + rr;
                
                out.set(j, i, rgb);
            }
        }
        
        show(out);
    }
    
    public static void show(Image.Int image)
    {
        ImageUtil.show(ImageUtil.Convert.toBufferedImage(image));
    }

}
