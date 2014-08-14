package org.testobject.kernel.imaging.diff;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;

/**
 * 
 * @author enijkamp
 *
 */
public class ScanLineImageComparatorTest {
	
	public static final boolean debug = false;
	
    public static void main(String ... args) throws Exception {        
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
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        Image.Int image2 = ImageUtil.Convert.toImageInt(buf2);
        
        // delta
        int tolerance = 5;
        long start = System.currentTimeMillis();
        List<Rectangle.Int> boxes = ScanLineImageComparator.compare(image1, image2, tolerance);
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
    public void testCircle() throws Throwable {
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // draw
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.red);
            g.fillOval(10, 10, 40, 80);
            g.dispose();
        }
        
        // convert
        Image.Int image0 = ImageUtil.Convert.toImageInt(buf0);
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas1 = ScanLineImageComparator.compare(image0, image1, tolerance);
        
        // plot        
        if(debug) {
            Graphics g = buf1.createGraphics();
            for(Rectangle.Int box : deltas1)
            {
                g.setColor(Color.blue);
                g.drawRect(box.x, box.y, box.w, box.h);
            }
            g.dispose();            
            ImageUtil.show(buf1, "1");
        }

        // assert
        Assert.assertEquals(1, deltas1.size());
        Rectangle.Int delta = deltas1.get(0);
        Assert.assertEquals(10, delta.x);
        Assert.assertEquals(11, delta.y);
        Assert.assertEquals(39, delta.w);
        Assert.assertEquals(79, delta.h);
    }
    
    @Test
    public void testBoxes() throws Throwable {
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
        Image.Int image0 = ImageUtil.Convert.toImageInt(buf0);
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas = ScanLineImageComparator.compare(image0, image1, tolerance);

        // assert
        Assert.assertEquals(1, deltas.size());
        Rectangle.Int delta = deltas.get(0);
        Assert.assertEquals(40, delta.w);
        Assert.assertEquals(80, delta.h);
    }
    
    @Test
    public void testSinglePixelThinLine() throws Throwable {
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // draw
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.red);
            g.fillRect(2, 0, 1, 100);
            g.dispose();
        }
        
        // convert
        Image.Int image0 = ImageUtil.Convert.toImageInt(buf0);
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        
        // deltas
        int tolerance = 1;
        List<Rectangle.Int> deltas = ScanLineImageComparator.compare(image0, image1, tolerance);

        // assert
        Assert.assertEquals(1, deltas.size());
        Rectangle.Int delta = deltas.get(0);
        Assert.assertEquals(2, delta.x);
        Assert.assertEquals(0, delta.y);
        Assert.assertEquals(1, delta.w);
        Assert.assertEquals(100, delta.h);
    }
    
    
    @Test
    public void testLines() throws Throwable {
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        Rectangle.Int rect1 = new Rectangle.Int(0, 0, 1, 100);
        Rectangle.Int rect2 = new Rectangle.Int(4, 0, 3, 100);
        
        // draw
        {
            Graphics g = buf0.createGraphics();
            g.setColor(Color.red);
            g.fillRect(rect1.x, rect1.y, rect1.w, rect1.h);
            g.dispose();
        }
        
        // draw
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.red);
            g.fillRect(rect2.x, rect2.y, rect2.w, rect2.h);
            g.dispose();
        }
        
        // convert
        Image.Int image0 = ImageUtil.Convert.toImageInt(buf0);
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        
        // deltas
        int tolerance = 1;
        List<Rectangle.Int> diffs = ScanLineImageComparator.compare(image0, image1, tolerance);
        
        // plot        
        if(debug) {
            Graphics g = buf1.createGraphics();
            for(Rectangle.Int box : diffs)
            {
                g.setColor(Color.blue);
                g.drawRect(box.x, box.y, box.w, box.h);
            }
            g.dispose();            
            ImageUtil.show(buf1, "1");
        }

        // assert
        Assert.assertEquals(2, diffs.size());
        {
	        Rectangle.Int diff = diffs.get(0);
	        Assert.assertTrue(diff.equals(rect1) || diff.equals(rect2));
        }
        {
	        Rectangle.Int diff = diffs.get(1);
	        Assert.assertTrue(diff.equals(rect1) || diff.equals(rect2));
        }

    }
    
    @Test
    public void testPartialBoxes() throws Throwable {
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
        Image.Int image0 = ImageUtil.Convert.toImageInt(buf0);
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas = ScanLineImageComparator.compare(image0, image1, tolerance);

        // assert
        Assert.assertEquals(1, deltas.size());
        Rectangle.Int delta = deltas.get(0);
        Assert.assertEquals(40, delta.w);
        Assert.assertEquals(80, delta.h);
    }
    
    @Test
    public void testChars() throws Throwable {
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
        Image.Int image1 = ImageUtil.Convert.toImageInt(buf1);
        Image.Int image2 = ImageUtil.Convert.toImageInt(buf2);
        
        // delta
        long start = System.currentTimeMillis();
        List<Rectangle.Int> boxes = ScanLineImageComparator.compare(image1, image2, tolerance);
        long end = System.currentTimeMillis();        
        System.out.println("deltas -> " + boxes.size() + " boxes took " + (end - start) + "ms");
        
        // assert
        final int newJvm = 7, oldJvm = 5;
        Assert.assertTrue(newJvm == boxes.size() || oldJvm == boxes.size());
    }
}
