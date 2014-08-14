package org.testobject.kernel.imgproc.diff;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.util.ImageUtil;

/**
 * 
 * @author nijkamp
 *
 */
public class ImageComparatorTest
{
    public static void main(String ... args) throws Exception
    {        
        // buffers
        BufferedImage buf1 = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf2 = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        
        // draw
        {
            Graphics g = buf2.createGraphics();
            g.setColor(Color.red);
            g.fillOval(50, 50, 100, 100);
            g.setFont(new Font("Arial", Font.PLAIN, 40));
            g.drawString("X e t s m u", 100, 300);
            g.dispose();
        }
        
        // convert
        Image.Int image1 = ImageUtil.toImageInt(buf1);
        Image.Int image2 = ImageUtil.toImageInt(buf2);
        
        // delta
        int tolerance = 5;
        long start = System.currentTimeMillis();
        List<Rectangle.Int> boxes = ImageComparator.compare(image1, image2, tolerance);
        long end = System.currentTimeMillis();        
        System.out.println("deltas -> " + boxes.size() + " boxes took " + (end - start) + "ms");
        
        // plot        
        {
            Graphics g = buf2.createGraphics();
            for(Rectangle.Int box : boxes)
            {
                g.setColor(Color.blue);
                g.drawRect(box.x, box.y, box.w, box.h);
            }
            g.dispose();            
        }
        ImageUtil.show(buf1, "1");
        ImageUtil.show(buf2, "2");
    }
    
    @Test
    public void testBoxes() throws Throwable
    {
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // draw
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.red);
            g.fillRect(10, 10, 40, 80);
            g.dispose();
        }
        
        // convert
        Image.Int image0 = ImageUtil.toImageInt(buf0);
        Image.Int image1 = ImageUtil.toImageInt(buf1);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas1 = ImageComparator.compare(image0, image1, tolerance);

        // assert
        Assert.assertEquals(1, deltas1.size());
        Rectangle.Int delta = deltas1.get(0);
        Assert.assertEquals(40, delta.w);
        Assert.assertEquals(80, delta.h);
    }
    
    @Test
    public void testPartialBoxes() throws Throwable
    {
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // white
        {
            Graphics g = buf0.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, buf0.getWidth(), buf0.getHeight());
            g.dispose();
        }
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, buf1.getWidth(), buf1.getHeight());
            g.dispose();
        }
        
        // draw
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.red);
            g.fillRect(10, 10, 40, 80);
            g.setColor(Color.white);
            g.fillRect(15, 10, 20, 20);
            g.dispose();
        }
        
        // convert
        Image.Int image0 = ImageUtil.toImageInt(buf0);
        Image.Int image1 = ImageUtil.toImageInt(buf1);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas1 = ImageComparator.compare(image0, image1, tolerance);

        // assert
        Assert.assertEquals(1, deltas1.size());
        Rectangle.Int delta = deltas1.get(0);
        Assert.assertEquals(40, delta.w);
        Assert.assertEquals(80, delta.h);
    }
    
    @Test
    public void testChars() throws Throwable
    {
        // read
        BufferedImage buf1 = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf2 = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        
        // draw
        {
            Graphics g = buf2.createGraphics();
            g.setColor(Color.red);
            g.fillOval(50, 50, 100, 100);
            g.setFont(new Font("Arial", Font.PLAIN, 40));
            g.drawString("X e t s m u", 100, 300);
            g.dispose();
        }
        
        // convert
        int tolerance = 5;
        Image.Int image1 = ImageUtil.toImageInt(buf1);
        Image.Int image2 = ImageUtil.toImageInt(buf2);
        
        // delta
        long start = System.currentTimeMillis();
        List<Rectangle.Int> boxes = ImageComparator.compare(image1, image2, tolerance);
        long end = System.currentTimeMillis();        
        System.out.println("deltas -> " + boxes.size() + " boxes took " + (end - start) + "ms");
        
        // assert
        final int newJvm = 7, oldJvm = 5;
        Assert.assertTrue(newJvm == boxes.size() || oldJvm == boxes.size());
    }
}
