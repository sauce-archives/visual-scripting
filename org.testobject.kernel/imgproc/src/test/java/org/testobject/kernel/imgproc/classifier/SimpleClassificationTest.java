package org.testobject.kernel.imgproc.classifier;

import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.testobject.kernel.pipeline.SimpleClassification;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.Meta;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.kernel.imgproc.classifier.*;
import org.testobject.kernel.ocr.TextRecognizer;
import org.testobject.kernel.pipeline.Classification;
import org.testobject.kernel.pipeline.Stages;


/**
 * 
 * @author enijkamp
 *
 */
public class SimpleClassificationTest {
	
    @Test
    public void testTwitter3() throws IOException {
        // input
        Image.Int image = ImageUtil.read("android/4_0_3/replay/twitter/3.png");
        
        // classifiers
        Classifier[] widgetClassifiers = {
    	        new GroupClassifier(),
    	        new TextCharClassifier(),
    	        new TextWordClassifier(),
    	        new IconClassifier(),
    	        new ButtonClassifier(),
    	        new TextBoxClassifier(),
    	        new PopupClassifier() };

        // classification
        Classification classification = new SimpleClassification(TextRecognizer.Builder.mock(new String[] {}), widgetClassifiers);

        // blob
        Blob blob = classification.toBlob(image, Stages.Builder.mock());

        // print
        BlobUtils.printMeta(blob);

        // buttons
        {
            {
            	List<Blob> blobs = locate(blob, new Rectangle(19, 57, 84, 49));
                assertTrue(containsType(blobs, Classes.Button.class));
            }        
        }
    }
    
    @Test
    public void testTwitter4() throws IOException {
        // input
        Image.Int image = ImageUtil.read("android/4_0_3/replay/twitter/4.png");
        
        // classifiers
        Classifier[] widgetClassifiers = {
    	        new GroupClassifier(),
    	        new TextCharClassifier(),
    	        new TextWordClassifier(),
    	        new IconClassifier(),
    	        new ButtonClassifier(),
    	        new TextBoxClassifier(),
    	        new PopupClassifier() };

        // classification
        Classification classification = new SimpleClassification(TextRecognizer.Builder.mock(new String[] {}), widgetClassifiers);

        // blob
        Blob blob = classification.toBlob(image, Stages.Builder.mock());

        // print
        BlobUtils.printMeta(blob);

        // buttons
        {
            {
            	List<Blob> blobs = locate(blob, new Rectangle(175, 470, 132, 61));
                assertTrue(containsType(blobs, Classes.Button.class));
            }        
        }
    }

    @Test
    public void testTwitterHome() throws IOException {
        // input
        Image.Int image = ImageUtil.read("android/4_0_3/screenshots/twitter/home.png");

        // classifiers
        Classifier[] widgetClassifiers = {
    	        new GroupClassifier(),
    	        new TextCharClassifier(),
    	        new TextWordClassifier(),
    	        new IconClassifier(),
    	        new ButtonClassifier(),
    	        new TextBoxClassifier(),
    	        new PopupClassifier() };

        // classification
        Classification classification = new SimpleClassification(TextRecognizer.Builder.mock(new String[] {}), widgetClassifiers);

        // blob
        Blob blob = classification.toBlob(image, Stages.Builder.mock());

        // print
        BlobUtils.printMeta(blob);

        // buttons
        {
            {
            	List<Blob> blobs = locate(blob, new Rectangle(415, 46, 57, 49));
                assertTrue(containsType(blobs, Classes.Button.class));
            }        
        }
        
        // icons
        {
        	// FIXME (en)
            {
        		List<Blob> blobs = locate(blob, new Rectangle(54, 11, 18, 14));
        		// assertTrue(containsType(blobs, Classes.Icon.class));
        	}
            {
            	List<Blob> blobs = locate(blob, new Rectangle(90, 11, 18, 14));
                // assertTrue(containsType(blobs, Classes.Icon.class));
            }
        }
        
        // text
        {
            {
                List<Blob> blobs = locate(blob, new Rectangle(424, 10, 12, 18));
                assertTrue(containsType(blobs, Classes.TextChar.class));
            }    
            {
            	List<Blob> blobs = locate(blob, new Rectangle(444, 10, 12, 18));
                assertTrue(containsType(blobs, Classes.TextChar.class));
            } 
            {
            	List<Blob> blobs = locate(blob, new Rectangle(458, 10, 11, 18));
                assertTrue(containsType(blobs, Classes.TextChar.class));
            } 
        }
    }

    private static boolean containsType(List<Blob> blobs, Class<? extends Meta> type) {
    	for(Blob blob : blobs) {
    		if(blob.meta.getClass().equals(type)) {
    			return true;
    		}
    	}
    	return false;
    }

    private static List<Blob> locate(Blob root, Rectangle rect) {
        Rectangle query = enlarge(root.bbox, rect, 2);
        List<Blob> result = new LinkedList<>();
        for(Blob child : root.children) {
            contained(child, query, result);
        }
        return result;
    }
    
    private static void contained(Blob blob, Rectangle rect, List<Blob> result) {
        if(rect.contains(blob.bbox) && isFuzzySameSize(rect, blob.bbox, 8)) {
        	result.add(blob);
        }

        for(Blob child : blob.children) {
            contained(child, rect, result);
        }
    }

    private static boolean isFuzzySameSize(Rectangle rect1, Rectangle rect2, int tolerance) {
        if(Math.abs(rect1.x - rect2.x) > tolerance) {
            return false;
        }

        if(Math.abs(rect1.y - rect2.y) > tolerance) {
            return false;
        }

        if(Math.abs(rect1.width - rect2.width) > tolerance) {
            return false;
        }

        if(Math.abs(rect1.height - rect2.height) > tolerance) {
            return false;
        }

        return true;
    }

    private static Rectangle enlarge(Rectangle max, Rectangle rect, int fat) {
        return new Rectangle(Math.max(0, rect.x - fat), Math.max(0, rect.y - fat), Math.min(max.width, rect.width + (fat*2)), Math.min(max.height, rect.height + (fat*2)));
    }

}
