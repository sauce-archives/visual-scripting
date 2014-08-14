package org.testobject.kernel.imaging.fingerprint;

import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imaging.fingerprint.Resample.ITransformation;
import org.testobject.kernel.imaging.fingerprint.Resample.Kernel;

/**
 * 
 * @author enijkamp
 *
 */
public class Upsample
{

    private static class Transformation implements ITransformation
    {
        private final int max;
        private final int size;

        private Transformation(int max, int size) {
            this.max = max;
            this.size = size;
        }

        @Override
        public double getSourceSpacing() { return 1. / max; }

        @Override
        public double getSourceOrigin() { return 0; }

        @Override
        public double getDestSpacing() { return 1. / size; }

        @Override
        public double getDestOrigin() { return 0; }

        @Override
        public int getDestLength() { return size; }
    }

    public static Image.Double scaleup(final int size, final Image.Double orig, double bgColor)
    {
        if(orig.h == size && orig.w == size)
        {
            return orig;
        }
        
        if(orig.w > size || orig.h > size)
        {
            throw new IllegalArgumentException("original image can not be larger than output one (" + size + "x" + size + ")");
        }
        
        final int max = Math.max(orig.w, orig.h);

        final Kernel [] ks = Resample.buildKernels(new Transformation(max, size));

        // first, resample in X-direction
        Image.Double tmp = new Image.Double(orig.h, size);
        for(int i = 0; i < orig.h; i++)
        {
            for(int j = 0; j < size; j++)
            {
                Kernel k = ks[j];
                
                double acc = 0.0;
                for(int n = k.min; n <= k.max; n++)
                {
                    double pixel;
                    
                    if(n < 0) 
                    {
                        pixel = bgColor;
                    }
                    else if(n >= orig.w)
                    {
                        pixel = bgColor;
                    }
                    else
                    {
                        pixel = orig.get(n, i);
                    }
                    
                    acc += k.weights[n - k.min] * pixel;
                }
                
                tmp.set(j, i, acc);
            }
        }
        
        // now resample in Y-direction
        Image.Double out = new Image.Double(size, size);
        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
            {
                Kernel k = ks[j];
                
                double acc = 0.0;
                for(int n = k.min; n <= k.max; n++)
                {
                    double pixel;
                    
                    if(n < 0) 
                    {
                        pixel = bgColor;
                    }
                    else if(n >= tmp.h)
                    {
                        pixel = bgColor;
                    }
                    else
                    {
                        pixel = tmp.get(i, n);
                    }
                    
                    acc += k.weights[n - k.min] * pixel;
                }
                
                out.set(i, j, acc);
            }
        }
        
        return out;
    }
    
    public static Image.Double resample(final int size, final Image.Double orig, double bgColor)
    {
        if(orig.h == size && orig.w == size)
        {
            return orig;
        }
        
        final int max = Math.max(orig.w, orig.h);
        
        // first, resample in X-direction
        Image.Double tmp = new Image.Double(size, orig.h);
        ITransformation transformation = new ITransformation()
        {
            @Override
            public double getSourceSpacing() { return 1. / max; }
            
            @Override
            public double getSourceOrigin() { return 0; }
            
            @Override
            public double getDestSpacing() { return 1. / size; }
            
            @Override
            public double getDestOrigin() { return 0; }
            
            @Override
            public int getDestLength() { return size; }
        };
        
        final Kernel [] ks = Resample.buildKernels(transformation);
        
        for(int y = 0; y < orig.h; y++)
        {
            for(int x = 0; x < size; x++)
            {
                Kernel k = ks[x];
                
                double acc = 0.0;
                for(int n = k.min; n <= k.max; n++)
                {
                    double pixel;
                    
                    if(n < 0) 
                    {
                        pixel = bgColor;
                    }
                    else if(n >= orig.w)
                    {
                        pixel = bgColor;
                    }
                    else
                    {
                        pixel = orig.get(n, y);
                    }
                    
                    acc += k.weights[n - k.min] * pixel;
                }
                
                tmp.set(x, y, acc);
            }
        }
        
        // now resample in Y-direction
        Image.Double out = new Image.Double(size, size);
        for(int i = 0; i < size; i++)
        {
            for(int j = 0; j < size; j++)
            {
                Kernel k = ks[j];
                
                double acc = 0.0;
                for(int n = k.min; n <= k.max; n++)
                {
                    double pixel;
                    
                    if(n < 0) 
                    {
                        pixel = bgColor;
                    }
                    else if(n >= tmp.h)
                    {
                        pixel = bgColor;
                    }
                    else
                    {
                        pixel = tmp.get(i,n);
                    }
                    
                    acc += k.weights[n - k.min] * pixel;
                }
                
                out.set(i,j, acc);
            }
        }
        
        return out;
    }
    
}
