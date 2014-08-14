package org.testobject.kernel.imaging.fingerprint;

/**
 * 
 * @author enijkamp
 *
 */
public class Scale
{
    public double min;
    public double max;
    
    public Scale()
    {
        this(Double.MAX_VALUE, -Double.MAX_VALUE);
    }
    
    public Scale(double min, double max)
    {
        this.min = min;
        this.max = max;
    }

    public double normalize(double value)
    {
        return (value - min) / (max - min);
    }
    
    public double restore(double normalized)
    {
        return normalized * (max - min) + min;
    }
    
    public void normalize(double [] array)
    {
        for(int i = 0; i < array.length; i++)
        {
            array[i] = normalize(array[i]);
        }
    }

    public void restore(double [] array)
    {
        for(int i = 0; i < array.length; i++)
        {
            array[i] = restore(array[i]);
        }
    }

    public void normalize(double [][] array)
    {
        for(int i = 0; i < array.length; i++)
        {
            normalize(array[i]);
        }
    }

    public void restore(double [][] array)
    {
        for(int i = 0; i < array.length; i++)
        {
            restore(array[i]);
        }
    }
    
    public void addToScale(double probe)
    {
        min = Math.min(min, probe);
        max = Math.max(max, probe);
    }

    public void addToScale(double [] probe)
    {
        for(int i = 0; i < probe.length; i++)
        {
            addToScale(probe[i]);
        }
    }

    public void addToScale(double [][] probe)
    {
        for(int i = 0; i < probe.length; i++)
        {
            addToScale(probe[i]);
        }
    }
    
    public static Scale of(double [] array)
    {
        Scale scale = new Scale();
        
        scale.addToScale(array);
        
        return scale;
    }

    public static Scale of(double [][] array)
    {
        Scale scale = new Scale();
        
        scale.addToScale(array);
        
        return scale;
    }
}
