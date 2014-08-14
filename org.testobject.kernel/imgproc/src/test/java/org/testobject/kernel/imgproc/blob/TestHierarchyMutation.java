package org.testobject.kernel.imgproc.blob;

import static org.testobject.kernel.imgproc.blob.BlobUtils.printMeta;

import java.awt.Dimension;
import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.kernel.imgproc.blob.*;
import org.testobject.kernel.imgproc.classifier.Classes;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;

/**
 * 
 * @author nijkamp
 *
 */
public class TestHierarchyMutation
{
    static class RasterFromArray implements BooleanRaster
    {
        private final int [][] raster;
        private final Dimension size;
        
        public RasterFromArray(int [][] raster) 
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
            return raster[y][x] == 1;
        }

        public void set(int x, int y, boolean what)
        {
            throw new UnsupportedOperationException();
        }        
    }
    
    static int[][] image =
    {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 1, 1, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0},
        {0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0},
        {0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0},
        {0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0},
        {0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
    };
    
    private static Context dummyContext()
    {
        return new Context(null, null);
    }
    
    @Test
    public void testInsert()
    {
        // 1. hierarchy
        BooleanRaster raster = new RasterFromArray(image);
        Blob blobs = FillBlobBuilder.detectAllBlobs(raster);
        
        // print blobs
        {
            System.out.println("blob hierarchy:");
            BlobUtils.printBlob(blobs, 0);
            System.out.println();
        }
        
        // assert
        {
            Assert.assertEquals(6, BlobUtils.countBlobs(blobs));
        }
        
        // print before
        {
            System.out.println("meta hierarchy (before):");
            BlobUtils.printMeta(blobs);
            System.out.println();
        }
        
        // 2. mutation
        Classifier[] classifiers = { new GroupClassifier() };
        Mutator mutator = new VisitingMutator(Arrays.asList(classifiers));
        mutator.mutate(dummyContext(), blobs);
        
        // print after
        {
            System.out.println("widget hierarchy (after):");
            BlobUtils.printMeta(blobs);
            System.out.println();
        }
        
        // assert
        {
            Assert.assertEquals(7, BlobUtils.countBlobs(blobs));
            Blob insert = blobs.children.get(0);
            Assert.assertEquals(Classes.Group.class, insert.meta.getClass());
            Assert.assertEquals(6, BlobUtils.countBlobs(insert));
        }
    }
}
