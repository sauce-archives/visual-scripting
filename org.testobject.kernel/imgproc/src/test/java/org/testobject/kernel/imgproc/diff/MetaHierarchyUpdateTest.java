package org.testobject.kernel.imgproc.diff;


/**
 * 
 * @author nijkamp
 *
 */
public class MetaHierarchyUpdateTest
{
    private static final int BLACK_WHITE_THRESHOLD = 30;
    private static final boolean DEBUG = false;
	
    // FIXME port to blob meta code (en)
    
    /*
    @Test
    public void testUpdate() throws Throwable
    {
    	// >> mock <<
        final Font font = new Font("Sans", Font.PLAIN, 12);
        Rectangle screen = new Rectangle(800, 600);
        Rectangle dialog = new Rectangle(50, 50, 200, 200);
        String dialogTitle = "Frame";
        Rectangle button = new Rectangle(20, 150, 80, 35);
        String buttonTitle = "Okay";
        Rectangle button2 = new Rectangle(120, 150, 80, 35);
        String button2Title = "Abc";
        
        BufferedImage background = new BufferedImage(screen.w, screen.h, BufferedImage.TYPE_INT_RGB);
        
        BufferedImage[] buffers =
        {
            drawDialog(background, font, dialog, dialogTitle, button, buttonTitle),
            drawButton(drawDialog(background, font, dialog, dialogTitle, button, buttonTitle), font, button2, button2Title),
        };
        
        if(DEBUG)
        {
            ImageUtil.show(buffers[0]);
        }
        
        
        
        // >> blob <<   
        // convert
        Image.Int[] images = new Image.Int[buffers.length];
        for(int i = 0; i < images.length; i++)
        {
            images[i] = ImageUtil.toImageInt(buffers[i]);
        }
        
        // delta
        int tolerance = 5;
        List<Rectangle> deltas = ImageComparator.compare(images[0], images[1], tolerance);
        Assert.assertEquals(1, deltas.size());
        Rectangle region = deltas.get(0);
        Assert.assertEquals(button2, region);

        // > 1. blob (incremental) <
        Blob blob0 = build(images[0]);

        // > 2. blob (full) <
        Blob blob1 = build(images[1]);
        
        // > 3. blob (validation) <
        {
            // build blob
            Blob blob1Update = build(images[0]);

            // find parent
            Blob blobOldParent = BlobRebuilder.findParent(blob1Update, region);
            Assert.assertTrue(dialog.contains(blobOldParent.bbox));
                   
            // get hierarchy for region
            Image.Int subImage = ImageUtil.crop(images[1], blobOldParent.bbox);
            
            // update hierarchy
            Blob blobNewParent = build(subImage);
            BlobRebuilder.updateBlob(blob1Update, blobOldParent, blobNewParent);

            // assert
            Assert.assertTrue(equalBlobs(blob1, blob1Update));
        }
        
        
        
        // >> meta <<
	    Classifier[] classifiers = Dependencies.order(
            new Classifier[]
            {
                new GroupClassifier(),
                new TextClassifier(),
                new ButtonClassifier(),
                new DialogClassifier()
            }
        );
	    
	    // > 1. node (incremental) <
        Node meta0 = NodeBuilder.build(blob0);
        
        for(Classifier classifier : classifiers)
        {
            new VisitingMutator(classifier).mutate(meta0);
        }
        
        // FIXME follow back-pointers to top-most parent (en)
        
        // find parent
        Node metaOldParent = NodeRebuilder.findParent(meta0, region);
        Assert.assertEquals(Classifier.Dialog.class, getWidget(getParent(meta0, metaOldParent)).getClass());
        
        // get blob branch
        Blob blobOldParent = metaOldParent.getBlobs().get(0);
        Assert.assertTrue(dialog.contains(blobOldParent.bbox));
        
        // crop region
        Image.Int subImage = ImageUtil.crop(images[1], blobOldParent.bbox);
        if(DEBUG)
        {
            ImageUtil.show(ImageUtil.toBufferedImage(subImage));
        }

        // FIXME take colors into account (en)
        
        // update blob hierarchy
        Blob blobNewParent = build(subImage);
        BlobRebuilder.updateBlob(blob0, blobOldParent, blobNewParent);
        
        // update meta hierarchy
        Node metaNewParent = NodeBuilder.build(blobNewParent);
        NodeRebuilder.updateNode(meta0, metaOldParent, metaNewParent);
                
        // classify delta nodes
        for(Classifier classifier : classifiers)
        {
            new VisitingMutator(classifier).mutate(metaNewParent);
        }

        // > 2. node (full) <
        Node node1 = NodeBuilder.build(blob1);
        for(Classifier classifier : classifiers)
        {
            new VisitingMutator(classifier).mutate(node1);
        }
        
        // assert
        if(DEBUG)
        {
            System.out.println(">> incremental <<");
            org.testobject.imgproc.meta.Utils.printNode(meta0);
            System.out.println(">> full <<");
            org.testobject.imgproc.meta.Utils.printNode(node1);
        }
        Assert.assertTrue(equalNodes(node1, meta0));
    }
       
    private static Node getParent(Node parent, Node probe)
    {
        for(Node child : parent.getChildren())
        {
            if(child == probe)
            {
                return parent;
            }
            else
            {
                return getParent(child, probe);
            }
        }
        throw new IllegalArgumentException();
    }
    
    private static Classifier.Widget getWidget(Node node)
    {
    	Assert.assertFalse(node.getWidget() instanceof Classifier.Blob);
    	return node.getWidget();
    }
    
    private static boolean equalBlobs(Blob blob1, Blob blob2)
    {
        if(!blob1.bbox.equals(blob2.bbox))
            return false;
        
        if(blob1.area != blob2.area)
            return false;
        
        if(blob1.children.size() != blob2.children.size())
            return false;
        
        for(int y = 0; y < blob1.bbox.h; y++)
        {
            for(int x = 0; x < blob1.bbox.w; x++)
            {
                if(blob1.get(x, y) != blob2.get(x, y))
                    return false;
            }
        }
        
        for(int i = 0; i < blob1.children.size(); i++)
        {
            if(!equalBlobs(blob1.children.get(i), blob2.children.get(i)))
                return false;
        }
        
        return true;
    }
    
    private static boolean equalNodes(Node node1, Node node2)
    {
        if(!node1.getBoundingBox().equals(node2.getBoundingBox()))
            return false;
        
        if(node1.getChildren().size() != node2.getChildren().size())
            return false;
        
        if(node1.getWidget().getClass() != node2.getWidget().getClass())
        	return false;
        
        for(int i = 0; i < node1.getChildren().size(); i++)
        {
            if(!equalNodes(node1.getChildren().get(i), node2.getChildren().get(i)))
                return false;
        }
        
        return true;        
    }
    
    private static Blob build(Image.Int image)
    {
        // laplace
        Image.Int edges = new Image.Int(image.h, image.w, image.type);
        new FastGimpLaplaceEdgeDetector().process(image, edges);
        
        // black/white
        Image.Bool bw = ImageUtil.toBlackWhite(edges, BLACK_WHITE_THRESHOLD);
        ArrayRaster raster = Utils.toRaster(bw);
        
        // identify blobs
        return new LinearBlobBuilder().build(raster);
    }
    */
}
