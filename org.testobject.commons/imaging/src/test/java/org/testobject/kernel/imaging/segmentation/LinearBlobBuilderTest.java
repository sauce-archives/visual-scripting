package org.testobject.kernel.imaging.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imaging.segmentation.TestUtils.init;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.util.image.ImageUtil;

/**
 * 
 * @author enijkamp
 *
 */
public class LinearBlobBuilderTest
{
    @Test
    public void testSimple() 
    {
        ArrayRaster raster = init(new int[][] {
                {0, 0, 0, 0},
                {0, 1, 1, 0},
                {0, 0, 0, 0}
        });
        
        Blob root = new LinearBlobBuilder().build(raster);
        
        assertThat(root.area, IsEqual.equalTo(10));
        
        Rectangle.Int bbox = root.bbox;
        assertEquals(4, bbox.w);
        assertEquals(3, bbox.h);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());
        
        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(2));
        
        Rectangle.Int cbbox = child.bbox;
        assertEquals(2, cbbox.w);
        assertEquals(1, cbbox.h);
        assertEquals(1, cbbox.x);
        assertEquals(1, cbbox.y);
    }
    
    @Test
    public void testEmpty() 
    {
        ArrayRaster raster = init(new int[][] {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
        
        Blob root = new LinearBlobBuilder().build(raster);
        
        assertThat(root.area, IsEqual.equalTo(12));
        
        Rectangle.Int bbox = root.bbox;
        assertEquals(4, bbox.w);
        assertEquals(3, bbox.h);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(0, root.children.size());
    }

    @Test
    public void testFilled() 
    {
        ArrayRaster raster = init(new int[][] {
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1}
        });
        
        Blob root = new LinearBlobBuilder().build(raster);
        
        assertThat(root.area, IsEqual.equalTo(0));
        
        Rectangle.Int bbox = root.bbox;
        assertEquals(4, bbox.w);
        assertEquals(3, bbox.h);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());

        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(12));
        
        Rectangle.Int cbbox = child.bbox;
        assertEquals(4, cbbox.w);
        assertEquals(3, cbbox.h);
        assertEquals(0, cbbox.x);
        assertEquals(0, cbbox.y);
    }

    @Test
    public void testTiny() 
    {
        ArrayRaster raster = init(new int[][] {
                {0}
        });
        
        Blob root = new LinearBlobBuilder().build(raster);
        
        assertThat(root.area, IsEqual.equalTo(1));
        
        Rectangle.Int bbox = root.bbox;
        assertEquals(1, bbox.w);
        assertEquals(1, bbox.h);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(0, root.children.size());
    }

    @Test
    public void testTinyFilled() 
    {
        ArrayRaster raster = init(new int[][] {
                {1}
        });
        
        Blob root = new LinearBlobBuilder().build(raster);
        
        assertThat(root.area, IsEqual.equalTo(0));
        
        Rectangle.Int bbox = root.bbox;
        assertEquals(1, bbox.w);
        assertEquals(1, bbox.h);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());

        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(1));
        
        Rectangle.Int cbbox = child.bbox;
        assertEquals(1, cbbox.w);
        assertEquals(1, cbbox.h);
        assertEquals(0, cbbox.x);
        assertEquals(0, cbbox.y);
    }
    
    @Test
    public void testLastRow() 
    {
        ArrayRaster raster = init(new int[][] {
                {1, 1, 1},
                {1, 0, 1},
        });
        
        Blob root = new LinearBlobBuilder().build(raster);
        
        assertThat(root.area, IsEqual.equalTo(1));
        
        Rectangle.Int bbox = root.bbox;
        assertEquals(3, bbox.w);
        assertEquals(2, bbox.h);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());

        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(5));
        
        Rectangle.Int cbbox = child.bbox;
        assertEquals(3, cbbox.w);
        assertEquals(2, cbbox.h);
        assertEquals(0, cbbox.x);
        assertEquals(0, cbbox.y);
    }

    public static ArrayRaster readRasterFromImage(String filename, int threshold) throws IOException, InterruptedException
    {
        BufferedImage image = ImageIO.read(new File(filename));
        final BufferedImage bwImage = ImageUtil.Convert.toBlackWhite(image, threshold);
        final Size.Int size = new Size.Int(image.getWidth(), image.getHeight());
        final int WHITE = Color.WHITE.getRGB();
        final boolean[][] bwRaster = new boolean[size.h][size.w];
        for (int x = 0; x < size.w; x++)
        {
            for (int y = 0; y < size.h; y++)
            {
                bwRaster[y][x] = bwImage.getRGB(x, y) == WHITE;
            }
        }

        return new ArrayRaster(bwRaster, size);
    }

    public static void main(String[] args) throws Throwable
    {
        final ArrayRaster raster = readRasterFromImage("src/test/resources/org/crowdbase/algorithms/imgproc/editor/editor_laplace.png", 100);

        long total = 0;

        LinearBlobBuilder builder = new LinearBlobBuilder();
        for (int i = 0; i < 100; i++)
        {
            long start = System.currentTimeMillis();
            builder.build(raster);
            long time = System.currentTimeMillis() - start;
            total += time;
        }

        System.out.println(total / 100 + "ms");

        /*
        Utils.printBlob(root, 0);
        System.out.println("Detection of " + Utils.countBlobs(root) + " blobs took " + time + "ms");
        
        showBlob(root, tracker);
        
        for(Blob c : root.children) {
            if(c.numpoints > 100) {
                showBlob(c, tracker);
            }
        }
        
        Utils.displayHierarchy(builder.build(raster));
        */
        
    }

}
