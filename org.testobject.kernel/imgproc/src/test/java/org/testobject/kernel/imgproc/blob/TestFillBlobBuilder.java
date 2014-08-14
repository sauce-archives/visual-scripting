package org.testobject.kernel.imgproc.blob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BooleanRaster;
import org.testobject.kernel.imgproc.blob.FillBlobBuilder;
import org.testobject.kernel.imgproc.blob.BlobUtils;

/**
 * 
 * @author enijkamp
 *
 */
public class TestFillBlobBuilder
{
    static class RasterFromArray implements BooleanRaster
    {
        private final boolean [][]raster;
        private final Dimension size;
        
        public RasterFromArray(boolean [][]raster) 
        {
            this.raster = raster;
            this.size   = new Dimension();
            size.height  = raster.length;
            size.width = raster[0].length;
        }

        public Dimension getSize()
        {
            return size;
        }

        public boolean get(int x, int y)
        {
            return raster[y][x];
        }

        public void set(int x, int y, boolean what)
        {
            throw new UnsupportedOperationException();
        }
        
    }
    
    @Test
    public void testSimple() 
    {
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {false, false, false, false},
                {false, true,  true,  false},
                {false, false, false, false}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {true, true, true, true},
                {true, true, true, true},
                {true, true, true, true}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {false}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {true}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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

//    public static void main(String[] args) throws Throwable
//    {
//        BooleanRaster raster = BlobUtils.readRasterFromImage("src/test/resources/org/crowdbase/algorithms/imgproc/editor/editor_laplace.png", 100);
//        //   	Utils.writeRaster(raster, "raster.bin");
//
//        //    	IBooleanRaster raster = Utils.readRaster("raster.bin");
//
//        long total = 0;
//        
//        Blob root = null;
//        for(int i = 0; i < 10; i++)
//        {
//            long start = System.currentTimeMillis();
//            root = FillBlobBuilder.detectAllBlobs(raster);
//            long time = System.currentTimeMillis() - start;
//            total += time;
//        }
//        
//        System.out.println(total / 10 + "ms");
//
//        //Utils.printBlob(root, 0);
//
//        System.out.println("Detected " + BlobUtils.countBlobs(root) + " blobs");
//
//        /*
//        showBlob(root, tracker);
//        
//        for(Blob c : root.children) {
//        	if(c.numpoints > 100) {
//        		showBlob(c, tracker);
//        	}
//        }
//        */
//
//        BlobUtils.displayHierarchy(root);
//    }
    
}
