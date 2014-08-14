package org.testobject.kernel.imaging.segmentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;

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
        private final Size.Int size;
        
        public RasterFromArray(boolean [][]raster) 
        {
            this.raster = raster;
            this.size   = new Size.Int(0, 0);
            size.h      = raster.length;
            size.w      = raster[0].length;
        }

        public Size.Int getSize()
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {true, true, true, true},
                {true, true, true, true},
                {true, true, true, true}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {false}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
        BooleanRaster raster = new RasterFromArray(new boolean[][] {
                {true}
        });
        
        Blob root = FillBlobBuilder.detectAllBlobs(raster);
        
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
