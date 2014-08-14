package org.testobject.kernel.imaging.fingerprint;

// Resampling using "magic" kernel, see http://assassinationscience.com/johncostella/magic/
public class Resample
{
    public static class Kernel 
    {
        public final int min;
        public final int max;
        
        public final double[] weights;
        
        private Kernel(int min, double [] weights)
        {
            this.min = min;
            this.max = min + weights.length - 1;
            this.weights = weights;
        }
        
        private static double weight(double x)
        {
            if (x < -1.5 || x > 1.5)
            {
                return 0.0;
            }
            
            if (x < -0.5) 
            {
                double z = x + 1.5; 
                return 0.5 * z * z;
            }
            
            if (x > 0.5) {
                double z = x - 1.5; 
                return 0.5 * z * z;
            }
            
            return 0.75 - x * x;
        }
        
        public static Kernel magic(double step, double start)
        {
            if(step < 1.0)
            {
                // three-point kernel
                int middle = (int) (0.5 + start);
                double f = start - middle;
                double [] w = { 0.5 * (f - 0.5) * (f - 0.5), 0.75 - f * f, 0.5 * (f + 0.5) * (f + 0.5)};
                
                return new Kernel(middle - 1, w);
            }
            else 
            {
                // n-point kernel
                double hw = 1.5 * step; 
                int min = (int)Math.ceil(start - hw); 
                int n = ((int)Math.floor(start + hw)) - min + 1;
                double [] w = new double[n];

                double sum = 0.0;
                for(int m = 0; m < n; m++)
                {
                    int i = min + m;
                    double ww = weight( (i - start) / step);
                    sum += ww;
                    w[m] = ww;
                }
                
                double scale = 1. / sum;
                for(int m = 0; m < n; m++)
                {
                    w[m] *= scale;
                }
                
                return new Kernel(min, w);
            }
        }
    }
    
    public static Kernel [] buildKernels(double step, double start, int length) 
    {
        Kernel [] out = new Kernel[length];
        
        for(int i = 0; i < length; i++)
        {
            double x = start + i * step;
            out[i] = Kernel.magic(step, x);
        }
        
        return out;
    }
    
    /**
     * Describes resampling transformation
     */
    public interface ITransformation
    {
        double getSourceOrigin();
        double getSourceSpacing();
        double getDestOrigin();
        double getDestSpacing();
        int getDestLength();
    }
    
    public static Kernel[] buildKernels(ITransformation t)
    {
        return buildKernels(t.getDestSpacing() / t.getSourceSpacing(), 
                ((t.getDestOrigin() + t.getDestSpacing() / 2) - (t.getSourceOrigin() + t.getSourceSpacing() / 2)) / t.getSourceSpacing(), 
                t.getDestLength());
    }
    
    // example of 1-dim resampling of source
    public static double [] resample(ITransformation t, double [] source, int offset, int length)
    {
        Kernel [] kernels = buildKernels(t);
        
        double [] out = new double[kernels.length];
        
        for(int i = 0; i < out.length; i++) 
        {
            Kernel k = kernels[i];
            
            double acc = 0.0;
            for(int j = k.min; j <= k.max; j++)
            {
                if(j < offset)
                {
                    acc += source[offset] * k.weights[j - k.min];
                }
                else if(j >= offset + length)
                {
                    acc += source[offset + length - 1] * k.weights[j - k.min];
                }
                else
                {
                    acc += source[j] * k.weights[j - k.min];
                }
            }
            
            out[i] = acc;
        }
        
        return out;
    }
}
