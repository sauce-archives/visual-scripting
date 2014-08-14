package org.testobject.kernel.imgproc.blob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.testobject.kernel.imgproc.blob.TestUtils.init;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.testobject.kernel.imgproc.blob.ArrayRaster;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.LinearBlobBuilder;
import org.testobject.kernel.imgproc.util.ImageUtil;

/**
 * 
 * @author enijkamp
 *
 */
public class TestLinearBlobBuilder
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
        
        Rectangle bbox = root.bbox;
        assertEquals(4, bbox.width);
        assertEquals(3, bbox.height);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());
        
        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(2));
        
        Rectangle cbbox = child.bbox;
        assertEquals(2, cbbox.width);
        assertEquals(1, cbbox.height);
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
        
        Rectangle bbox = root.bbox;
        assertEquals(4, bbox.width);
        assertEquals(3, bbox.height);
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
        
        Rectangle bbox = root.bbox;
        assertEquals(4, bbox.width);
        assertEquals(3, bbox.height);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());

        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(12));
        
        Rectangle cbbox = child.bbox;
        assertEquals(4, cbbox.width);
        assertEquals(3, cbbox.height);
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
        
        Rectangle bbox = root.bbox;
        assertEquals(1, bbox.width);
        assertEquals(1, bbox.height);
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
        
        Rectangle bbox = root.bbox;
        assertEquals(1, bbox.width);
        assertEquals(1, bbox.height);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());

        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(1));
        
        Rectangle cbbox = child.bbox;
        assertEquals(1, cbbox.width);
        assertEquals(1, cbbox.height);
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
        
        Rectangle bbox = root.bbox;
        assertEquals(3, bbox.width);
        assertEquals(2, bbox.height);
        assertEquals(0, bbox.x);
        assertEquals(0, bbox.y);
        
        assertEquals(1, root.children.size());

        Blob child = root.children.get(0);

        assertThat(child.area, IsEqual.equalTo(5));
        
        Rectangle cbbox = child.bbox;
        assertEquals(3, cbbox.width);
        assertEquals(2, cbbox.height);
        assertEquals(0, cbbox.x);
        assertEquals(0, cbbox.y);
    }

    public static ArrayRaster readRasterFromImage(String filename, int threshold) throws IOException, InterruptedException
    {
        BufferedImage image = ImageIO.read(new File(filename));
        final BufferedImage bwImage = ImageUtil.toBlackWhite(image, threshold);
        final Dimension size = new Dimension(image.getWidth(), image.getHeight());
        final int WHITE = Color.WHITE.getRGB();
        final boolean[][] bwRaster = new boolean[size.height][size.width];
        for (int x = 0; x < size.width; x++)
        {
            for (int y = 0; y < size.height; y++)
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
