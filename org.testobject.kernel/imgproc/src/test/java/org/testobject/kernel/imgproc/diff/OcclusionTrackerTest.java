package org.testobject.kernel.imgproc.diff;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.util.ImageUtil;

/**
 * 
 * @author enijkamp
 *
 */
public class OcclusionTrackerTest
{
    private OcclusionTracker newTracker()
    {
        return new StackOcclusionTracker();
    }
    
    /**
     * The test covers the case of a single occlusion (appears & disappears).
     */
    @Test
    public void testSimpleOcclusion() throws Throwable
    {
        // drawings
    	Rectangle.Int rect1 = new Rectangle.Int(10, 20, 30, 40);
        
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // draw
        Graphics g = buf1.createGraphics();
        g.setColor(Color.red);
        g.fillRect(rect1.x, rect1.y, rect1.w, rect1.h);
        g.dispose();
        
        // convert
        Image.Int image0 = ImageUtil.toImageInt(buf0);
        Image.Int image1 = ImageUtil.toImageInt(buf1);
        Image.Int image2 = ImageUtil.toImageInt(buf2);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas1 = ImageComparator.compare(image0, image1, tolerance);
        List<Rectangle.Int> deltas2 = ImageComparator.compare(image1, image2, tolerance);

        // assert delta 1
        Assert.assertEquals(1, deltas1.size());
        Rectangle.Int delta1 = deltas1.get(0);
        Assert.assertEquals(rect1, delta1);
        
        // assert delta 2
        Assert.assertEquals(1, deltas2.size());
        Rectangle.Int delta2 = deltas2.get(0);
        Assert.assertEquals(rect1, delta2);
        
        // identify occlusions
        OcclusionTracker tracker = newTracker();
        Rectangle.Int occlusion1 = tracker.process(image0, image1, delta1);
        Rectangle.Int occlusion2 = tracker.process(image1, image2, delta2);
        
        // assert occlusions
        Assert.assertEquals(rect1, occlusion1);
        Assert.assertTrue(occlusion2.isEmpty());
    }
    
    /**
     * The test covers the case of disjoint occlusions.
     */
    @Test
    public void testDisjointOcclusions() throws Throwable
    {
        // drawings
    	Rectangle.Int rect1 = new Rectangle.Int(10, 10, 10, 10);
    	Rectangle.Int rect2 = new Rectangle.Int(20, 30, 40, 50);
        
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // draw 1
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.red);
            g.fillRect(rect1.x, rect1.y, rect1.w, rect1.h);
            g.dispose();
        }
        
        // draw 2
        {
            Graphics g = buf2.createGraphics();
            g.setColor(Color.blue);
            g.fillRect(rect2.x, rect2.y, rect2.w, rect2.h);
            g.dispose();
        }
        
        // convert
        Image.Int image0 = ImageUtil.toImageInt(buf0);
        Image.Int image1 = ImageUtil.toImageInt(buf1);
        Image.Int image2 = ImageUtil.toImageInt(buf2);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas1 = ImageComparator.compare(image0, image1, tolerance);
        List<Rectangle.Int> deltas2 = ImageComparator.compare(image1, image2, tolerance);

        // assert delta 1
        Assert.assertEquals(1, deltas1.size());
        Assert.assertEquals(rect1, deltas1.get(0));
        Rectangle.Int delta1 = deltas1.get(0);
        
        // assert delta 2
        Assert.assertEquals(2, deltas2.size());
        Rectangle.Int delta2a = deltas2.get(0);
        Rectangle.Int delta2b = deltas2.get(1);
        
        // identify occlusions
        OcclusionTracker tracker = newTracker();
        Rectangle.Int occlusion1 = tracker.process(image0, image1, delta1);
        Rectangle.Int occlusion2a = tracker.process(image1, image2, delta2a);
        Rectangle.Int occlusion2b = tracker.process(image1, image2, delta2b);        
        
        // asserts
        Assert.assertEquals(rect1, occlusion1);
        if(rect1.equals(delta2a))
        {
            Assert.assertTrue(occlusion2a.isEmpty());
            Assert.assertEquals(rect2, occlusion2b);
        }
        else if(rect1.equals(delta2b))
        {
            Assert.assertEquals(rect2, occlusion2a);
            Assert.assertTrue(occlusion2b.isEmpty());
        }
        else
        {
            Assert.fail();
        }
    }
    
    /**
     * The test covers the case of overlapping occlusions. 
     * Exemplary creating a new class file in eclipse where the context-menu is used to 
     * open a dialog-window would yield similar occlusions.
     */
    @Test
    public void testOverlappingOcclusions() throws Throwable
    {
        // drawings
    	Rectangle.Int menu = new Rectangle.Int(10, 10, 40, 80);
        Rectangle.Int dialog = new Rectangle.Int(40, 40, 40, 40);
        
        // buffers
        BufferedImage buf0 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        BufferedImage buf2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        
        // draw (context menu)
        {
            Graphics g = buf1.createGraphics();
            g.setColor(Color.red);
            g.fillRect(menu.x, menu.y, menu.w, menu.h);
            g.dispose();
        }
        
        // draw (dialog)
        {
            Graphics g = buf2.createGraphics();
            g.setColor(Color.blue);
            g.fillRect(dialog.x, dialog.y, dialog.w, dialog.h);
            g.dispose();
        }
        
        // convert
        Image.Int image0 = ImageUtil.toImageInt(buf0);
        Image.Int image1 = ImageUtil.toImageInt(buf1);
        Image.Int image2 = ImageUtil.toImageInt(buf2);
        
        // deltas
        int tolerance = 5;
        List<Rectangle.Int> deltas1 = ImageComparator.compare(image0, image1, tolerance);
        List<Rectangle.Int> deltas2 = ImageComparator.compare(image1, image2, tolerance);

        // assert delta (context menu)
        Assert.assertEquals(1, deltas1.size());
        Rectangle.Int delta1 = deltas1.get(0);
        Assert.assertEquals(menu, delta1);
        
        // assert delta (dialog)
        Rectangle.Int intersect = dialog.union(menu);
        Assert.assertEquals(1, deltas2.size());
        Rectangle.Int delta2 = deltas2.get(0);
        Assert.assertEquals(intersect, delta2);
        
        // identify occlusions
        OcclusionTracker tracker = newTracker();
        Rectangle.Int occlusion_menu = tracker.process(image0, image1, delta1);
        Rectangle.Int occlusion_dialog = tracker.process(image1, image2, delta2);
        
        // assert occlusions
        Assert.assertEquals(menu, occlusion_menu);
        Assert.assertEquals(dialog, occlusion_dialog);
    }
    
    /**
     * The test covers a moving occlusion (e.g. a dialog window).
     * This requires the tracker to keep track of multiple layers of occlusions.
     */
    @Test
    public void testMovingOcclusion() throws Throwable
    {
        // drawings
    	Rectangle.Int[] rects = 
        {
            new Rectangle.Int(10, 10, 20, 20),
            new Rectangle.Int(20, 20, 40, 40),
            new Rectangle.Int(5, 5, 20, 20)
        };
        
        // buffers
        BufferedImage[] bufs =
        {
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        };
        
        // draw
        Color[] colors = { Color.blue, Color.red, Color.green };
        for(int i = 0; i < rects.length; i++)
        {
            Graphics g = bufs[i+1].createGraphics();
            g.setColor(colors[i]);
            g.fillRect(rects[i].x, rects[i].y, rects[i].w, rects[i].h);
            g.dispose();
        }
        
        // convert
        Image.Int[] images =
        {
            ImageUtil.toImageInt(bufs[0]),
            ImageUtil.toImageInt(bufs[1]),
            ImageUtil.toImageInt(bufs[2]),
            ImageUtil.toImageInt(bufs[3]),
            ImageUtil.toImageInt(bufs[4])
        };
        
        // deltas
        int tolerance = 5;
        List<List<Rectangle.Int>> deltas = new ArrayList<>();
        deltas.add(ImageComparator.compare(images[0], images[1], tolerance));
        deltas.add(ImageComparator.compare(images[1], images[2], tolerance));
        deltas.add(ImageComparator.compare(images[2], images[3], tolerance));
        deltas.add(ImageComparator.compare(images[3], images[4], tolerance));

        // assert
        for(List<Rectangle.Int> list : deltas)
        {
            Assert.assertEquals(1, list.size());
        }
        
        // identify occlusions
        OcclusionTracker tracker = newTracker();
        Rectangle.Int[] occlusions =
        {
            tracker.process(images[0], images[1], deltas.get(0).get(0)),
            tracker.process(images[1], images[2], deltas.get(1).get(0)),
            tracker.process(images[2], images[3], deltas.get(2).get(0)),
            tracker.process(images[3], images[4], deltas.get(3).get(0))
        };
        
        // assert        
        Assert.assertEquals(rects[0], occlusions[0]);
        Assert.assertEquals(rects[1], occlusions[1]);
        Assert.assertEquals(rects[2], occlusions[2]);
        Assert.assertTrue(occlusions[3].isEmpty());
    }
    
    /**
     * The test covers a stack of multiple occlusions.
     */
    @Test
    public void testStacking() throws Throwable
    {
        // drawings
    	Rectangle.Int[] rects = 
        {
            new Rectangle.Int(10, 10, 20, 20),
            new Rectangle.Int(20, 20, 40, 40),
            new Rectangle.Int(5, 5, 20, 20)
        };
        
        // buffers
        BufferedImage[] bufs =
        {
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        };
        
        // draw 1
        for(int i = 1; i < 4; i++)
        {
            Graphics g = bufs[i].createGraphics();
            g.setColor(Color.red);
            g.fillRect(rects[0].x, rects[0].y, rects[0].w, rects[0].h);
            g.dispose();
        }
        
        // draw 2
        for(int i = 2; i < 4; i++)
        {
            Graphics g = bufs[i].createGraphics();
            g.setColor(Color.blue);
            g.fillRect(rects[1].x, rects[1].y, rects[1].w, rects[1].h);
            g.dispose();
        }
        
        // draw 3
        for(int i = 3; i < 4; i++)
        {
            Graphics g = bufs[i].createGraphics();
            g.setColor(Color.green);
            g.fillRect(rects[2].x, rects[2].y, rects[2].w, rects[2].h);
            g.dispose();
        }
        
        // convert
        Image.Int[] images =
        {
            ImageUtil.toImageInt(bufs[0]),
            ImageUtil.toImageInt(bufs[1]),
            ImageUtil.toImageInt(bufs[2]),
            ImageUtil.toImageInt(bufs[3]),
            ImageUtil.toImageInt(bufs[4])
        };
        
        // deltas
        int tolerance = 5;
        List<List<Rectangle.Int>> deltas = new ArrayList<>();
        deltas.add(ImageComparator.compare(images[0], images[1], tolerance));
        deltas.add(ImageComparator.compare(images[1], images[2], tolerance));
        deltas.add(ImageComparator.compare(images[2], images[3], tolerance));
        deltas.add(ImageComparator.compare(images[3], images[4], tolerance));

        // assert
        for(List<Rectangle.Int> list : deltas)
        {
            Assert.assertEquals(1, list.size());
        }
        
        // identify occlusions
        OcclusionTracker tracker = newTracker();
        Rectangle.Int[] occlusions =
        {
            tracker.process(images[0], images[1], deltas.get(0).get(0)),
            tracker.process(images[1], images[2], deltas.get(1).get(0)),
            tracker.process(images[2], images[3], deltas.get(2).get(0)),
            tracker.process(images[3], images[4], deltas.get(3).get(0))
        };
        
        // assert        
        Assert.assertEquals(rects[0], occlusions[0]);
        Assert.assertEquals(rects[1], occlusions[1]);
        Assert.assertEquals(rects[2], occlusions[2]);
        Assert.assertTrue(occlusions[3].isEmpty());
    }
    
    
    /**
     * The test covers a stack of multiple occlusions.
     */
    @Test
    public void testStackingPipeline() throws Throwable
    {
        // drawings
    	Rectangle.Int[] rects = 
        {
            new Rectangle.Int(10, 10, 40, 40),
            new Rectangle.Int(15, 15, 5, 5)
        };
        
        // buffers
        BufferedImage[] bufs =
        {
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB),
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB)
        };
        
        // draw 1
        for(int i = 1; i <= 2; i++)
        {
            Graphics g = bufs[i].createGraphics();
            g.setColor(Color.red);
            g.fillRect(rects[0].x, rects[0].y, rects[0].w, rects[0].h);
            g.dispose();
        }
        
        // draw 2
        {
            Graphics g = bufs[2].createGraphics();
            g.setColor(Color.blue);
            g.fillRect(rects[1].x, rects[1].y, rects[1].w, rects[1].h);
            g.dispose();
        }
        
        // convert
        Image.Int[] images =
        {
            ImageUtil.toImageInt(bufs[0]),
            ImageUtil.toImageInt(bufs[1]),
            ImageUtil.toImageInt(bufs[2]),
            ImageUtil.toImageInt(bufs[3])
        };
        
        // deltas
        int tolerance = 5;
        List<List<Rectangle.Int>> deltas = new ArrayList<List<Rectangle.Int>>();
        deltas.add(ImageComparator.compare(images[0], images[1], tolerance));
        deltas.add(ImageComparator.compare(images[1], images[2], tolerance));
        deltas.add(ImageComparator.compare(images[2], images[3], tolerance));

        // assert
        for(List<Rectangle.Int> list : deltas)
        {
            Assert.assertEquals(1, list.size());
        }
        
        // identify occlusions
        OcclusionTracker tracker = newTracker();
        Rectangle.Int[] occlusions =
        {
            tracker.process(images[0], images[1], deltas.get(0).get(0)),
            tracker.process(images[1], images[2], deltas.get(1).get(0)),
            tracker.process(images[2], images[3], deltas.get(2).get(0))
        };
        
        // assert        
        Assert.assertEquals(rects[0], occlusions[0]);
        Assert.assertEquals(rects[1], occlusions[1]);
        Assert.assertTrue(occlusions[2].isEmpty());
    }
}
