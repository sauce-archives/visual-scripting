package org.testobject.kernel.imaging.fingerprint;


import org.testobject.commons.util.image.Image;

/**
 * 
 * 1. Convert to YCrCb color space
 * 2. Isomorphically resample Y, Cr, and Cb images to 8x8
 * 4. Normalize Y component (corresponds to auto-correction of contrast)
 * 5. Normalize Cr and Cb components (corresponds to auto-correction of color saturation). But color normalization should not shift color, nor should it
 *    scale it up too much (because black-and-white images will be distorted).
 * 6. Match by finding the closest match of luma matrix in the database. Experiments suggest that if squared luma distance ({@link #lumaDistance2} is less than 0.01,
 *    we have a perfect match.
 * 7. In case of ambiguity (several images match luma), consider luma + chroma.
 * 
 * @author enijkamp
 *
 */
public class ImageFingerprint
{
    
    public static final int SIZE = 32;
    
    public final int size;
    public final double [] lumaFingerprint;
    public final double [] chromaCrFingerprint;
    public final double [] chromaCbFingerprint;
    
    @SuppressWarnings("unused")
    private final Scale lumaScale;
    @SuppressWarnings("unused")
    private final Scale chromaScale;
    
    public ImageFingerprint(Image.Int image, int bgColorR, int bgColorG, int bgColorB, int printSize)
    {
    	this.size = printSize;
    	
        Image.Double yy = YCrCb.y(image);
        Image.Double cr = YCrCb.cr(image);
        Image.Double cb = YCrCb.cb(image);
        
        Image.Double luma = Upsample.resample(size, yy, YCrCb.y(bgColorR, bgColorG, bgColorB));
        Image.Double chromaCr = Upsample.resample(size, cr, YCrCb.cr(bgColorR, bgColorG, bgColorB));
        Image.Double chromaCb = Upsample.resample(size, cb, YCrCb.cb(bgColorR, bgColorG, bgColorB));
        
        Scale lumaScale = Scale.of(luma.pixels);
        lumaScale.normalize(luma.pixels);
        
        Scale chromaScale = Scale.of(chromaCr.pixels);
        chromaScale.addToScale(chromaCb.pixels);
        
        // we want only to scale, no color shift. To achieve that we need to make sure that
        // "colorless point", which is (cr=128, cb=128), stays intact during the scaling.
        // doing isomorphic color scale is like auto-correcting color saturation
        // also: do not scale color intensity more than 2x, for the sake of BW icons
        
        double maxhigh = chromaScale.max - 128.0;
        double maxlow  = 128. - chromaScale.min;
        
        double max = Math.max(maxlow, maxhigh);
        if(max < 64.) max = 64.; // do not scale too much!
        
        chromaScale = new Scale(128.0 - max, 128. + max);
        
        chromaScale.normalize(chromaCr.pixels);
        chromaScale.normalize(chromaCb.pixels);

        double [] lumaFingerprint = new double[size * size];
        double [] chromaCrFingerprint = new double[size * size];
        double [] chromaCbFingerprint = new double[size * size];
        for(int i = 0; i < size * size; i++)
        {
            lumaFingerprint[i] = luma.pixels[i];
            chromaCrFingerprint[i] = chromaCr.pixels[i];
            chromaCbFingerprint[i] = chromaCb.pixels[i];
        }
        
        this.lumaFingerprint = lumaFingerprint;
        this.chromaCrFingerprint = chromaCrFingerprint;
        this.chromaCbFingerprint = chromaCbFingerprint;
        
        this.lumaScale = lumaScale;
        this.chromaScale = chromaScale;
    }
    
    public ImageFingerprint(Image.Int image, int bgColorR, int bgColorG, int bgColorB)
    {
    	this(image, bgColorR, bgColorG, bgColorB, SIZE);
    }
    
    public Image.Int restoreImage()
    {
        Image.Double luma     = new Image.Double(size, size);
        Image.Double chromaCr = new Image.Double(size, size);
        Image.Double chromaCb = new Image.Double(size, size);
        for(int i = 0; i < size * size; i++)
        {
            luma.pixels[i] = lumaFingerprint[i];
            chromaCr.pixels[i] = chromaCrFingerprint[i];
            chromaCb.pixels[i] = chromaCbFingerprint[i];
        }
        
        new Scale(0.0, 255.).restore(luma.pixels);
        new Scale(0.0, 255.).restore(chromaCr.pixels);
        new Scale(0.0, 255.).restore(chromaCb.pixels);
        
        Image.Int out = YCrCb.rgb(luma, chromaCr, chromaCb);
        
        return out;
    }
 
    public Image.Int restoreLuma()
    {
        Image.Double luma     = new Image.Double(size, size);
        Image.Double chromaCr = new Image.Double(size, size);
        Image.Double chromaCb = new Image.Double(size, size);
        for(int i = 0; i < size * size; i++)
        {
            luma.pixels[i] = lumaFingerprint[i];
        }
        for(int i = 0; i < luma.h; i++)
        {
            for(int j = 0; j < luma.w; j++)
            {
                chromaCr.set(j, i, 128);
                chromaCb.set(j, i, 128);
            }
        }
        
        new Scale(0.0, 255.).restore(luma.pixels);
        
        Image.Int out = YCrCb.rgb(luma, chromaCr, chromaCb);
        
        return out;
    }
    
    public Image.Int restoreChroma()
    {
        Image.Double luma     = new Image.Double(size, size);
        Image.Double chromaCr = new Image.Double(size, size);
        Image.Double chromaCb = new Image.Double(size, size);
        for(int i = 0; i < size * size; i++)
        {
            chromaCr.pixels[i] = chromaCrFingerprint[i];
            chromaCb.pixels[i] = chromaCbFingerprint[i];
        }
        for(int i = 0; i < luma.h; i++)
        {
            for(int j = 0; j < luma.w; j++)
            {
                luma.set(j, i, 128);
            }
        }
        
        new Scale(0.0, 255.).restore(chromaCr.pixels);
        new Scale(0.0, 255.).restore(chromaCb.pixels);
        
        Image.Int out = YCrCb.rgb(luma, chromaCr, chromaCb);
        
        return out;
    }
    
    public double chromaMass2()
    {
        double acc = 0.0;
        for(int i = 0; i < chromaCrFingerprint.length; i++)
        {
            double diffCr = chromaCrFingerprint[i] - 0.5;
            double diffCb = chromaCbFingerprint[i] - 0.5;
            
            acc += diffCr * diffCr + diffCb * diffCb;
        }
        
        return acc;
    }

    public double chromaMassL1()
    {
        double acc = 0.0;
        for(int i = 0; i < chromaCrFingerprint.length; i++)
        {
            double diffCr = chromaCrFingerprint[i] - 0.5;
            double diffCb = chromaCbFingerprint[i] - 0.5;
            
            acc += Math.abs(diffCr) + Math.abs(diffCb);
        }
        
        return acc;
    }
    
    /**
     * Squared Euclidian distance between two luma components.
     * 
     * @param f1
     * @param f2
     * @return
     */
    public static double lumaDistance2(ImageFingerprint f1, ImageFingerprint f2)
    {
        double acc = 0.0;
        for(int i = 0; i < f1.lumaFingerprint.length; i++)
        {
            double diff = f1.lumaFingerprint[i] - f2.lumaFingerprint[i];
            
            acc += diff * diff;
        }
        
        return acc;
    }

    /**
     * Squared Euclidian distance between two chroma components.
     * 
     * @param f1
     * @param f2
     * @return
     */
    public static double chromaDistance2(ImageFingerprint f1, ImageFingerprint f2)
    {
        double acc = 0.0;
        for(int i = 0; i < f1.chromaCrFingerprint.length; i++)
        {
            double diffCr = f1.chromaCrFingerprint[i] - f2.chromaCrFingerprint[i];
            double diffCb = f1.chromaCbFingerprint[i] - f2.chromaCbFingerprint[i];
            
            acc += diffCr * diffCr + diffCb * diffCb;
        }
        
        return acc;
    }

    /**
     * L1-distance between two luma components.
     * 
     * @param f1
     * @param f2
     * @return
     */
    public static double lumaDistanceL1(ImageFingerprint f1, ImageFingerprint f2)
    {
        double acc = 0.0;
        for(int i = 0; i < f1.lumaFingerprint.length; i++)
        {
            double diff = f1.lumaFingerprint[i] - f2.lumaFingerprint[i];
            
            acc += Math.abs(diff);
        }
        
        return acc;
    }

    /**
     * L1-distance between two chroma components.
     * 
     * @param f1
     * @param f2
     * @return
     */
    public static double chromaDistanceL1(ImageFingerprint f1, ImageFingerprint f2)
    {
        double acc = 0.0;
        for(int i = 0; i < f1.chromaCrFingerprint.length; i++)
        {
            double diffCr = f1.chromaCrFingerprint[i] - f2.chromaCrFingerprint[i];
            double diffCb = f1.chromaCbFingerprint[i] - f2.chromaCbFingerprint[i];
            
            acc += Math.abs(diffCr) + Math.abs(diffCb);
        }
        
        return acc;
    }
}
