package org.testobject.imgproc.guitool;

/**
 * 
 * @author nijkamp
 *
 */
public class InspectorMain
{
	/*
    public static final String imageFileBefore = "android/4_0_3/replay/twitter/1.png";
    public static final String imageFileAfter = "android/4_0_3/replay/twitter/2.png";

    public static void main(String... args) throws IOException
    {
        final Display display = new Display();

        Image.Int before = ImageUtil.read(imageFileBefore);
        Image.Int after = ImageUtil.read(imageFileAfter);

        Point click = new Point(180, 115);

        int tolerance = 5;
        List<Rectangle> diffImage = ImageComparator.compare(before, after, tolerance);

        Blob beforeBlob = getBlob(before);
        Blob afterBlob = getBlob(after);

        List<Blob> path = Util.locate(beforeBlob, click);
        Blob target = Util.getLastWidget(path);

        Font font = new Font("Ubuntu", Font.PLAIN, 12);
        Learner<Blob> learner = FreeTypeFactory.newLearner(SimpleClassification.BLACK_WHITE_THRESHOLD, 96);
        Memory<Blob> memory = learner.learnFont(font);
        TextRecognizer<Blob> recognizer = FreeTypeFactory.newBlobRecognizer(memory);
        Locators.Transform transform = new Locators.Transform(recognizer);
        Map<Locator, Blob> beforeLocatorToBlob = new IdentityHashMap<Locator, Blob>();
        Map<Locator, Blob> afterLocatorToBlob = new IdentityHashMap<Locator, Blob>();
        Locator beforeLocator = transform.blobToLocator(beforeBlob, beforeLocatorToBlob);
        Locator afterLocator = transform.blobToLocator(afterBlob, afterLocatorToBlob);
        List<Script.Responses.Response> diffLocator = Locators.Diff.between(beforeLocator, afterLocator);

        State beforeState = new State(beforeImage, beforeBlob, beforeLocator, beforeLocatorToBlob, 0);
        State afterState = new State(afterImage, afterBlob, afterLocator, afterLocatorToBlob, 10);

        final Shell shell = Inspector.newShell(display, diffImage, beforeState, afterState, diffLocator, click, target);

        shell.open();
        while (!shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    public static Blob getBlob(org.testobject.commons.util.image.Image.Int image)
    {
        final int BLACK_WHITE_THRESHOLD = 10;

        final FastEdgeDetector laplace = new FastGimpLaplaceEdgeDetector();
        final LinearBlobBuilder builder = new LinearBlobBuilder();

        final Classifier[] classifiers = Dependencies.order(new Classifier[]
        { new GroupClassifier(), new TextClassifier(), new ButtonClassifier(), new HorizontalTabPanelClassifier() });

        // laplace
        org.testobject.runtime.util.image.Image.Int edges = new org.testobject.runtime.util.image.Image.Int(image.h, image.w, org.testobject.runtime.util.image.Image.Int.TYPE_RGB);
        laplace.process(image, edges);

        // black/white
        org.testobject.runtime.util.image.Image.Bool bw = new org.testobject.runtime.util.image.Image.Bool(edges.h, edges.w);
        ImageUtil.toBlackWhite(edges, bw, BLACK_WHITE_THRESHOLD);

        // blobs
        ArrayRaster raster = Utils.toRaster(bw);
        Blob blob = builder.build(raster);
        
        // context
        Classifier.Context context = new Classifier.Context(image);

        // classification
        for (Classifier classifier : classifiers)
        {
            new VisitingMutator(classifier).mutate(context, blob);
        }

        return blob;
    }
    */
}
