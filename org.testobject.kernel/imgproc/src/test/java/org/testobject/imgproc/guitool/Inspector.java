package org.testobject.imgproc.guitool;

/**
 * 
 * @author nijkamp
 * 
 */
public class Inspector
{
	/*
    public static Shell newShell(Display display, List<Rectangle> diffImage, State before, State after, List<Script.Responses.Response> diffLocator, Point click, Blob target)
    {
        final Shell shell = new Shell(display);
        shell.setText("inspector");
        shell.setSize(1200, 1000);
        shell.setLayout(new FillLayout());
        new Inspector(display, shell, diffImage, before, after, diffLocator, click, target);
        return shell;
    }
    
    public static class State
    {
        public final int timestamp;
        public final BufferedImage image;
        public final Locator locator;
        public final Map<Locator, Blob> locatorToBlob;
        public final Blob blob;

        public State(BufferedImage image, int timestamp)
        {
            this(image, null, null, null, timestamp);
        }
        
        public State(BufferedImage image, Blob blob, Locator locator, Map<Locator, Blob> locatorToBlob, int timestamp)
        {
            this.image = toABGR(image);
            this.blob = blob;
            this.locator = locator;
            this.locatorToBlob = locatorToBlob;
            this.timestamp = timestamp;
        }
        
        private BufferedImage toABGR(BufferedImage source)
        {
            if(source.getType() != BufferedImage.TYPE_4BYTE_ABGR)
            {
                return ImageUtil.convert(source, BufferedImage.TYPE_4BYTE_ABGR);
            }
            else
            {
                return source;
            }
        }
    }

    private static class View
    {
        enum Types
        {
            SOURCE, BLOB, META, LOCATOR
        }
        
        private static class Node extends LocatorTree.LocatorNode
        {            
            public Blob blob;

            public Node()
            {
                super();
            }

            public Node(Node parent, Blob blob)
            {
                super(parent);
                this.blob = blob;
            }

            @Override
            public String toString()
            {
                if (blob != null)
                {
                    return blob.meta.getClass().getSimpleName() + " (" + blob.id + ", " + blob.color + ")";
                }
                else
                {
                    return super.toString();
                }
            }
        }
        
        private final Display display;
        private final Composite composite;
        private final Label timestamp;
        private final Label image;
        private final CheckboxTreeViewer tree;

        private Blob highlight;
        private Point click;
        private Blob target;
        private List<Script.Responses.Response> diffLocator;
        private List<Rectangle> diffImage;
        private State state;

        private Types type = Types.SOURCE;
        private final Set<Blob> drawBlobs = new HashSet<Blob>();

        public View(final Display display, final Composite parent)
        {
            // composite
            {
                this.display = display;
                this.composite = new Composite(parent, SWT.NONE);
                this.composite.setLayout(new GridLayout(1, false));
                this.composite.setLayoutData(new GridData(GridData.FILL_BOTH));
            }
            
            // timestamp
            {
                this.timestamp = new Label(composite, SWT.NONE);
            }

            // image
            {
                this.image = new Label(composite, SWT.BORDER);
                this.image.addPaintListener(new PaintListener()
                {
                    @Override
                    public void paintControl(PaintEvent event)
                    {
                        GC gc = event.gc;
                        gc.setAdvanced(true);
                        
                        if(diffImage != null)
                        {
                            gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
                            gc.getGCData().alpha = 50;
                            for(Rectangle box : diffImage)
                            {
                                gc.fillRectangle(box.x, box.y, box.w, box.h);
                            }
                        }
                        
                        if(target != null)
                        {
                            BufferedImage buffer = new BufferedImage(target.getSize().w, target.getSize().h, BufferedImage.TYPE_4BYTE_ABGR);
                            Utils.toBufferedImage(buffer, target, 0, 0, Color.green);
                            Image targetImage = new Image(display, SwtUtils.toImage(buffer));
                            
                            gc.getGCData().alpha = 110;
                            gc.drawImage(targetImage, target.bbox.x, target.bbox.y);
                        }
                        
                        if (click != null)
                        {
                            gc.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
                            gc.getGCData().alpha = 110;
                            gc.fillOval(click.x - 10, click.y - 10, 20, 20);
                            gc.getGCData().alpha = 255;
                            gc.fillOval(click.x - 2, click.y - 2, 4, 4);
                        }
                        
                        if(highlight != null)
                        {
                            gc.setForeground(display.getSystemColor(SWT.COLOR_RED));
                            gc.getGCData().alpha = 100;
                            
                            Rectangle box = highlight.getBoundingBox();

                            // draw a border to make small blobs more visible
                            final int border = 3;
                            gc.setLineWidth(border);
                            gc.drawRectangle(box.x-border, box.y-border, box.w+border, box.h+border);
                            
                            // fill
                            gc.setBackground(display.getSystemColor(SWT.COLOR_GREEN));                            
                            gc.fillRectangle(box.x, box.y, box.w, box.h);
                        }
                    }
                });
            }

            // views
            {
                Composite views = new Composite(composite, SWT.NONE);
                views.setLayout(new RowLayout());

                Button source = new Button(views, SWT.PUSH);
                source.setText("source");
                source.addSelectionListener(new SelectionListener()
                {
                    @Override
                    public void widgetSelected(SelectionEvent event)
                    {
                        resetView(display, Types.SOURCE);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent event)
                    {

                    }
                });

                Button blob = new Button(views, SWT.PUSH);
                blob.setText("blob");
                blob.addSelectionListener(new SelectionListener()
                {
                    @Override
                    public void widgetSelected(SelectionEvent event)
                    {
                        resetView(display, Types.BLOB);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent event)
                    {

                    }
                });

                Button meta = new Button(views, SWT.PUSH);
                meta.setText("meta");
                meta.addSelectionListener(new SelectionListener()
                {
                    @Override
                    public void widgetSelected(SelectionEvent event)
                    {
                        resetView(display, Types.META);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent event)
                    {

                    }
                });

                Button locator = new Button(views, SWT.PUSH);
                locator.setText("locator");
                locator.addSelectionListener(new SelectionListener()
                {
                    @Override
                    public void widgetSelected(SelectionEvent event)
                    {
                        resetView(display, Types.LOCATOR);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent event)
                    {

                    }
                });
            }

            // tree
            {
                this.tree = new CheckboxTreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
                this.tree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
                this.tree.setContentProvider(new SimpleTree.ContentProvider());
                this.tree.setLabelProvider(new LocatorTree.ColorLabelProvider(display));
                this.tree.addSelectionChangedListener(new ISelectionChangedListener()
                {                    
                    @Override
                    public void selectionChanged(SelectionChangedEvent event)
                    {
                        if (event.getSelection() instanceof IStructuredSelection)
                        {
                            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                            if(selection.getFirstElement() instanceof Node)
                            {
                                Node element = (Node) selection.getFirstElement();
                                if(element != null)
                                {
                                    resetHighlight(element.blob);
                                }
                            }
                            else
                            {
                                LocatorTree.LocatorNode element = (LocatorTree.LocatorNode) selection.getFirstElement();
                                if(element != null)
                                {
                                    resetHighlight(state.locatorToBlob.get(element.locator));
                                }
                            }
                        }
                    }
                });
                this.tree.addCheckStateListener(new ICheckStateListener()
                {
                    public void checkStateChanged(CheckStateChangedEvent event)
                    {
                        if(event.getElement() instanceof Node)
                        {
                            Node element = (Node) event.getElement();
                            if (event.getChecked())
                            {
                                drawBlobs.add(element.blob);
                            }
                            else
                            {
                                drawBlobs.remove(element.blob);
                            }
                            drawImage();
                        }
                    }
                });
            }
        }

        public void setClick(Point click)
        {
            this.click = click;
        }
        
        public void setTarget(Blob target)
        {
            this.target = target;
        }

        public void setDiffImage(List<Rectangle> diffImage)
        {
            this.diffImage = diffImage;
        }
        
        public void setState(State state, List<Script.Responses.Response> diffLocator)
        {
            this.state = state;
            this.diffLocator = diffLocator;
            
            resetTimestamp();
            drawImage();
            rebuildTree();
        }
        
        private void resetTimestamp()
        {
            this.timestamp.setText("timestamp=" + state.timestamp);
        }

        private void resetView(Display display, Types type)
        {
            this.type = type;
            drawBlobs.clear();
            drawImage();
            rebuildTree();
        }
        
        private void resetHighlight(Blob highlight)
        {
            this.highlight = highlight;
            this.image.redraw();
        }

        private void drawImage()
        {
            if (type == Types.SOURCE)
            {
                this.image.setImage(new Image(display, SwtUtils.toImage(state.image)));
            }
            else if (type == Types.BLOB)
            {
                BufferedImage buffer = whiteCopy(state.image);
                for (Blob blob : drawBlobs)
                {
                    Utils.toBufferedImage(buffer, blob, blob.bbox.x, blob.bbox.y);
                }
                this.image.setImage(new Image(display, SwtUtils.toImage(buffer)));
            }
            else if (type == Types.META)
            {
                BufferedImage buffer = whiteCopy(state.image);
                for (Blob blob : drawBlobs)
                {
                    org.testobject.runtime.imgproc.classifier.Utils.renderMeta(buffer, blob);
                }
                this.image.setImage(new Image(display, SwtUtils.toImage(buffer)));
            }
            else if (type == Types.LOCATOR)
            {
                this.image.setImage(new Image(display, SwtUtils.toImage(state.image)));
            }
        }
        
        private BufferedImage whiteCopy(BufferedImage source)
        {            
            BufferedImage buffer = new BufferedImage(source.getWidth(), source.getH(), BufferedImage.TYPE_4BYTE_ABGR);
            float[] factors =
            { .5f, .5f, .5f, 1f };
            float[] offsets =
            { 150f, 150f, 150f, 0f };
            RescaleOp whiten = new RescaleOp(factors, offsets, null);
            whiten.filter(source, buffer);
            return buffer;
        }

        private void rebuildTree()
        {
            final Node root = new Node();
            if (type == Types.SOURCE || type == Types.BLOB)
            {
                if(state.blob != null)
                {
                    rebuildTreeBlobs(state.blob, root);
                }
            }
            else if (type == Types.META)
            {
                if(state.blob != null)
                {
                    rebuildTreeMetas(state.blob, root);
                }
            }
            else if (type == Types.LOCATOR)
            {
                if(state.locator != null)
                {
                    rebuildTreeLocators(state.locator, root);
                }
            }
            tree.setInput(root);
        }

        private void rebuildTreeBlobs(Blob blob, Node parent)
        {
            Node node = new Node(parent, blob);
            parent.childs.add(node);
            for (Blob child : blob.children)
            {
                rebuildTreeBlobs(child, node);
            }
        }

        private void rebuildTreeMetas(Blob blob, Node parent)
        {
            Node node = new Node(parent, blob);
            parent.childs.add(node);
            for (Blob child : blob.children)
            {
                if (hasMeta(child))
                {
                    rebuildTreeMetas(child, node);
                }
            }
        }

        private boolean hasMeta(Blob blob)
        {
            if (blob.meta instanceof Meta.Blob == false && blob.meta instanceof Classes.Group == false)
            {
                return true;
            }
            for (Blob child : blob.children)
            {
                if (hasMeta(child))
                {
                    return true;
                }
            }
            return false;
        }

        private void rebuildTreeLocators(Locator locator, Node parent)
        {
            LocatorTree.buildChilds(parent, locator, diffLocator);
        }
    }

    private final Composite composite;
    private final View before, after;

    public Inspector(final Display display, final Composite parent, List<Rectangle> diffImage, State before, State after, List<Script.Responses.Response> diffLocator, Point click, Blob target)
    {
        this.composite = new Composite(parent, SWT.NONE);
        this.composite.setLayout(new GridLayout(2, true));

        this.before = new View(display, composite);
        this.after = new View(display, composite);

        this.setTransition(diffImage, before, after, diffLocator, click, target);
    }

    public void setTransition(List<Rectangle> diffImage, State before, State after, List<Script.Responses.Response> diffLocator, Point click, Blob target)
    {
        // before
        {
            this.before.setClick(click);
            this.before.setTarget(target);
            this.before.setState(before, diffLocator);
        }

        // after
        {
            this.after.setState(after, diffLocator);
            this.after.setDiffImage(diffImage);
        }
    }
    */
}
