package org.testobject.kernel.imgproc.blob;


/**
 * 
 * @author nijkamp
 *
 */
public interface Meta
{    
    class Blob implements Meta {};
    
    Meta blob = new Blob();
}
