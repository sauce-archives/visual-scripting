package org.testobject.kernel.classification.gui;

import static org.testobject.kernel.classification.gui.SwingUtils.item;
import static org.testobject.kernel.classification.parser.Parser.Builder.cache;
import static org.testobject.kernel.classification.parser.Parser.Builder.classify;
import static org.testobject.kernel.classification.parser.Parser.Builder.expandFix;
import static org.testobject.kernel.classification.parser.Parser.Builder.group;
import static org.testobject.kernel.classification.parser.Parser.Builder.locators;
import static org.testobject.kernel.classification.parser.Parser.Builder.lowpass;
import static org.testobject.kernel.classification.parser.Parser.Builder.optimize;
import static org.testobject.kernel.classification.parser.Parser.Builder.plan;
import static org.testobject.kernel.classification.parser.Parser.Builder.prune;
import static org.testobject.kernel.classification.parser.Parser.Builder.segment;
import static org.testobject.kernel.classification.parser.Parser.Builder.tie;
import static org.testobject.kernel.classification.parser.Parser.Builder.Breeding.loosers;
import static org.testobject.kernel.classification.parser.Parser.Builder.Contestants.flat;
import static org.testobject.kernel.classification.parser.Parser.Builder.Contestants.hierarchical;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.math.algebra.Size;
import org.testobject.commons.tools.gui.DirectoryChooser;
import org.testobject.commons.tools.gui.ImageChooser;
import org.testobject.commons.tools.plot.ScalableFigure;
import org.testobject.commons.tools.plot.Visualizer.Renderable;
import org.testobject.commons.util.functional.Functions;
import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.kernel.api.classification.classifiers.Classifier;
import org.testobject.kernel.api.classification.classifiers.Classifier.Qualifier;
import org.testobject.kernel.api.classification.graph.Depiction;
import org.testobject.kernel.api.classification.graph.Element;
import org.testobject.kernel.api.classification.graph.Locator;
import org.testobject.kernel.api.classification.graph.Node;
import org.testobject.kernel.api.classification.graph.Variable;
import org.testobject.kernel.classification.classifiers.advanced.MaskSegment;
import org.testobject.kernel.classification.classifiers.advanced.Registry;
import org.testobject.kernel.classification.classifiers.advanced.Registry.Sample;
import org.testobject.kernel.classification.graph.Printer;
import org.testobject.kernel.classification.gui.ParserGuiTool.Model.Graph;
import org.testobject.kernel.classification.gui.Tree.BlobTranslater;
import org.testobject.kernel.classification.gui.Tree.LocatorTranslater;
import org.testobject.kernel.classification.gui.Tree.NodeTranslater;
import org.testobject.kernel.classification.gui.Tree.RenderGraphTranslater;
import org.testobject.kernel.classification.gui.Tree.Translater;
import org.testobject.kernel.classification.parser.Operations;
import org.testobject.kernel.classification.parser.Operations.Input;
import org.testobject.kernel.classification.parser.Parser;
import org.testobject.kernel.classification.parser.Parser.Builder.Naming;
import org.testobject.kernel.classification.parser.Plan;
import org.testobject.kernel.classification.util.Find;
import org.testobject.kernel.classification.util.Find.Adapter;
import org.testobject.kernel.classification.util.MaskUtil;
import org.testobject.kernel.classification.util.NodeUtil;
import org.testobject.kernel.imaging.segmentation.BlobUtils;
import org.testobject.kernel.imaging.segmentation.Mask;


/**
 * 
 * @author enijkamp
 * 
 */
//TODO decouple mvc (en)
//TODO use guice similar to old runtime (en)
public class ParserGuiTool {

	public static class Model {

		public static enum Graph {
			SEGMENTS,
			GROUPS,
			CLASSES,
			WINNERS,
			PRUNE,
			SYNTHETIC,
			LOCATORS
		}
		
		public static class Pass {
			
			public class Nodes {
				public org.testobject.kernel.api.classification.graph.Node segments;
				public org.testobject.kernel.api.classification.graph.Node groups;
				public org.testobject.kernel.api.classification.graph.Node classify;
				public org.testobject.kernel.api.classification.graph.Node prune;
				public org.testobject.kernel.api.classification.graph.Node winners;
				public org.testobject.kernel.imaging.procedural.Node synthetic;
				public org.testobject.kernel.api.classification.graph.Locator.Node locators;
			}
			
			public class Images {
				public BufferedImage segments;
				public BufferedImage groups;
				public BufferedImage synthetic;
			}
			
			public final Nodes nodes = new Nodes();
			public final Images images = new Images();
			
		}
		
		public final BufferedImage raw;
		public final Pass[] pass;
		
		public Model(BufferedImage raw, Pass[] pass) {
			this.raw = raw;
			this.pass = pass;
		}
	}
	
	private static class Parsing {
		
		private static class Tie {
			
			private final Size.Int size;
			
			public Tie(org.testobject.commons.math.algebra.Size.Int size) {
				this.size = size;
			}

			public Functions.VoidFunction1<Node> input(final Model.Pass pass) {
				return new Functions.VoidFunction1<Node>() {
					@Override
					public void apply(Node node) {
						pass.nodes.segments = node;
						pass.images.segments = Draw.Segments.draw(node, size.w, size.h);
					}
				};
			}

			public Functions.VoidFunction1<Node> group(final Model.Pass pass) {
				return new Functions.VoidFunction1<Node>() {
					@Override
					public void apply(Node node) {
						pass.nodes.groups = node;
						pass.images.groups = Draw.Groups.draw(node, size.w, size.h);
					}
				};
			}

			public Functions.VoidFunction1<Node> classify(final Model.Pass pass) {
				return new Functions.VoidFunction1<Node>() {
					@Override
					public void apply(Node node) {
						pass.nodes.classify = node;
					}
				};
			}

			public Functions.VoidFunction1<Node> optimize(final Model.Pass pass) {
				return new Functions.VoidFunction1<Node>() {
					@Override
					public void apply(Node node) {
						pass.nodes.winners = node;
						pass.nodes.synthetic = toRenderGraph(node);
						pass.images.synthetic = Draw.Synthetic.draw(pass.nodes.synthetic);
					}
				};
			}
			
			public Functions.VoidFunction1<Node> prune(final Model.Pass pass) {
				return new Functions.VoidFunction1<Node>() {
					@Override
					public void apply(Node node) {
						pass.nodes.prune = node;
					}
				};
			}
			
			public Functions.VoidFunction1<org.testobject.kernel.api.classification.graph.Locator.Node> locators(final Model.Pass pass) {
				return new Functions.VoidFunction1<org.testobject.kernel.api.classification.graph.Locator.Node>() {
					@Override
					public void apply(org.testobject.kernel.api.classification.graph.Locator.Node node) {
						pass.nodes.locators = node;
					}
				};
			}
		}
		
		public static Model parse(Registry registry, BufferedImage image) {
			
			Image.Int raw = ImageUtil.Convert.toImage(image);
			Size.Int size = new Size.Int(raw.w, raw.h);

			Model.Pass[] pass = { new Model.Pass() };

			Naming naming = Naming.create();
			Tie tie = new Tie(size);
			
			Operations.Map<Node, Node> lowpass = cache(tie(classify(segments()), tie.classify(pass[0])));
			Operations.Map<Node, Node> prune = tie(prune(), tie.prune(pass[0]));
			Input input = cache(tie(segment(0.0f, 50000d, 30), tie.input(pass[0])));
			Operations.Map<Node, Node> group = cache(tie(group(), tie.group(pass[0])));
			Operations.Map<Node, Node> expand = expandFix();
			Operations.Map<Node, org.testobject.kernel.api.classification.graph.Locator.Node> locators = tie(locators(), tie.locators(pass[0]));

			Plan plan1 =
					plan(naming.stage())
						.caches(lowpass, input, group)
						.input(input)
						.map(group)
						.map(prune)
						.map(lowpass)
						.map(expand)
						.map(locators)
					.build();

			print("parsing");
			List<org.testobject.kernel.api.classification.graph.Locator.Node> result1 = new Parser.Executor<org.testobject.kernel.api.classification.graph.Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan1).execute(raw);
			print();
			
			print("result (1)");
			print(result1.get(0));
			print();
			
			return new Model(image, pass);
		}

		public static Model parseComplex(Registry registry, BufferedImage image) {
			
			Image.Int raw = ImageUtil.Convert.toImage(image);
			Size.Int size = new Size.Int(raw.w, raw.h);

			Model.Pass[] pass = { new Model.Pass(), new Model.Pass(), new Model.Pass(), new Model.Pass() };

			Naming naming = Naming.create();
			Tie tie = new Tie(size);
			
			Operations.Map<Node, Node> lowpass = cache(tie(classify(segments()), tie.classify(pass[0])));
			Operations.Map<Node, Node> prune = tie(prune(), tie.prune(pass[0]));

			Input input1 = cache(tie(segment(0.0f, 50000d, 30), tie.input(pass[1])));
			Operations.Map<Node, Node> group1 = cache(tie(group(), tie.group(pass[1])));
			Operations.Map<Node, Node> classify1 = tie(classify(images(registry)), tie.classify(pass[1]));
			Operations.Reduce<Node, Node> optimize1 = tie(optimize(group1, flat), tie.optimize(pass[1]));

			Input input2 = tie(segment(0.5f, 4500d, 20), tie.input(pass[2]));
			Operations.Map<Node, Node> group2 = tie(group(), tie.group(pass[2]));
			Operations.Map<Node, Node> classify2 = tie(classify(icons(registry)), tie.classify(pass[2]));
			Operations.Reduce<Node, Node> optimize2 = tie(optimize(group1, flat), tie.optimize(pass[2]));

			Operations.Reduce<Node, Node> optimize3 = tie(optimize(lowpass(loosers), lowpass, hierarchical), tie.optimize(pass[3]));
//			Operations.Reduce<Node, Node> optimize3 = tie(optimize(lowpass(winners), lowpass, hierarchical), tie.optimize(pass[3]));
			
			Operations.Map<Node, org.testobject.kernel.api.classification.graph.Locator.Node> locators3 = tie(locators(), tie.locators(pass[3]));

			Plan plan1 =
					plan(naming.stage())
						.caches(lowpass, input1, group1)
						.input(input1)
						.map(group1)
						.map(prune)
						.map(lowpass)
					.build();
			
			Plan plan2 =
					plan(naming.stage())
						.map(plan(naming.pass())
								.input(input1)
								.map(group1)
								.map(classify1)
								.reduce(optimize1),
							 plan(naming.pass())
								.input(input2)
								.map(group2)
								.map(classify2)
								.reduce(optimize2))
						.reduce(optimize3)
						.map(locators3)
					.build();

			print("parsing");
			List<Node> result1 = new Parser.Executor<Node>(Parser.Executor.Progress.Builder.sysout(), plan1).execute(raw);
			print();
			List<Locator.Node> result2 = new Parser.Executor<Locator.Node>(Parser.Executor.Progress.Builder.sysout(), plan2).execute(raw);
			print();
			
			print("result (1)");
			print(result1.get(0));
			print();
			
			print("result (2)");
			print(result2.get(0));
			print();
			
			return new Model(image, pass);
		}
		
		private static void print(Node node) {
			Printer.print(node, Variable.Names.Geometric.position, Variable.Names.Geometric.size);
		}
		
		private static void print(Locator.Node node) {
			Locator.Printer.print(node, 1, System.out, Variable.Names.Geometric.position, Variable.Names.Geometric.size);
		}
		
		private static void print(String string) {
			System.out.println(">>> " + string + " <<<");
			System.out.println();
		}
		
		private static void print() {
			System.out.println();
		}

		private static Classifier images(Registry registry) {
			List<Sample> images = registry.get(Qualifier.Factory.Class.image);
			return new org.testobject.kernel.classification.classifiers.advanced.Image.Classifier(images);
		}
		
		private static Classifier icons(Registry registry) {
			List<Sample> images = registry.get(Qualifier.Factory.Class.icon);
			return new org.testobject.kernel.classification.classifiers.advanced.Icon.Classifier(images);
		}

		private static Classifier segments() {
			return new MaskSegment.Classifier();
		}

		private static org.testobject.kernel.imaging.procedural.Node toRenderGraph(Node source) {
			org.testobject.kernel.imaging.procedural.Node.Builder target = org.testobject.kernel.imaging.procedural.Node.Builder.node();
			toGraph(source, target);

			return target.build();
		}

		private static void toGraph(Node source, org.testobject.kernel.imaging.procedural.Node.Builder target) {

			for (Variable<?> variable : source.getElement().getLabel().getFeatures()) {
				if (variable.getValue() instanceof Depiction) {
					Depiction depiction = (Depiction) variable.getValue();
					org.testobject.kernel.imaging.procedural.Node.Builder child = 
							org.testobject.kernel.imaging.procedural.Node.Builder
								.node(depiction.getTranslate())
									.child(org.testobject.kernel.imaging.procedural.Node.Builder.node(depiction.getScale()))
										.child(depiction.getGraph());

					target.child(child);
				}
			}

			for (Node child : source.getChildren()) {
				toGraph(child, target);
			}
		}
	}
	
	@SuppressWarnings("serial")
	private static class LayerFigure extends ScalableFigure {
		
		public final Layers.Raw rawLayer = new Layers.Raw();
		public final Layers.Raw segmentsLayer = new Layers.Raw();
		public final Layers.Raw groupsLayer = new Layers.Raw();
		public final Layers.Raw syntheticLayer = new Layers.Raw();
		
		public final Layers.BoundingBoxes boxesLayer = new Layers.BoundingBoxes();
		public final Layers.Box boxLayer = new Layers.Box();
		public final Layers.Touch<Node> touchLayer = new Layers.Touch<>();
		
		public void setActiveLayer(Renderable renderable) {
			this.removeLayers();
			this.addLayer(renderable);
			this.addLayer(this.boxesLayer);
			this.addLayer(this.boxLayer);
			this.addLayer(this.touchLayer);
		}
	}
	
	@SuppressWarnings("serial")
	private static class TargetPanel extends JPanel {
		
		private final DecimalFormat df = new DecimalFormat("0.000");
		
		private BufferedImage segment;
		private Qualifier qualifier;
		private double likelihood;
		
		@Override
		public void paint(java.awt.Graphics g) {
			super.paint(g);
			
			if(segment != null) {
				g.drawString("qualifier = " + qualifier, 10, 20);
				g.drawString("likelihood = " + df.format(likelihood), 10, 40);
				g.drawImage(segment, 10, 50, null);
			}
		}
		
		public void setTarget(BufferedImage image, Node node) {
			// cut by mask
			{
				Image.Int cutByMask = BlobUtils.Cut.cutByMask(ImageUtil.Convert.toImage(image), Mask.Builder.create(node.getElement().getMasks()));
				this.segment = ImageUtil.Convert.toBufferedImage(cutByMask);
			}
			
			// labels
			{
				this.qualifier = node.getElement().getLabel().getQualifier();
				this.likelihood = node.getElement().getLabel().getLikelihood().photometric();
			}
		}
		
	}

	private class Status {

		public final JLabel status;
		public double x = 0, y = 0;
		public Color color = Color.black;

		public Status(JLabel status) {
			this.status = status;
		}

		public void refresh() {
			status.setText("scale: " + figure.scale + " position (x,y): " + x + "," + y
					+ " color (a,r,g,b): " + color.getAlpha() + "," + color.getRed() + ","
					+ color.getGreen() + "," + color.getBlue());
		}
	}
	
	@SuppressWarnings("serial")
	private class TrainDialog extends JDialog {
		
		private final JComboBox<String> combo;
		private final JTextField id;
		
		public TrainDialog(final JFrame parent, final Element element, final BufferedImage image) {
			super(parent);
			
			{
				this.setModal(true);
				this.setTitle("Train segment");
				this.setResizable(false);
			}
			
			{
				this.getContentPane().setLayout(new BorderLayout());
			}
			
			{
				Box box = Box.createVerticalBox();
			    {
			    	box.add(Box.createHorizontalStrut(400));
			    	box.add(Box.createVerticalStrut(20));
			    	box.add(Box.createVerticalGlue());
					{
						Image.Int cutByMask = BlobUtils.Cut.cutByMask(ImageUtil.Convert.toImage(image), Mask.Builder.create(element.getMasks()));
						JLabel label = SwingUtils.toSwingImage(ImageUtil.Convert.toBufferedImage(cutByMask));
						box.add(label);
					}
					
					box.add(Box.createVerticalStrut(20));
					
					{
						this.combo = new JComboBox<String>();
						{
							combo.addItem(Classifier.Qualifier.Factory.Class.image);
							combo.addItem(Classifier.Qualifier.Factory.Class.icon);
						}
						box.add(combo);
					}
					
					box.add(Box.createVerticalStrut(20));
					
					{
						this.id = new JTextField();
						box.add(id);
					}
					
					box.add(Box.createVerticalStrut(20));
			    }
			    this.add(box, BorderLayout.PAGE_START);
			}
			
		    {
			    JPanel buttons = new JPanel(new GridLayout(1, 2));
				{
					{
						JButton button = new JButton("Train");
						button.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								ParserGuiTool.this.train(ImageUtil.Convert.toImage(image), element, combo.getSelectedItem().toString(), id.getText());
								ParserGuiTool.this.parse(image);
								TrainDialog.this.dispose();
							}
						});
						getRootPane().setDefaultButton(button);
						buttons.add(button);
					}
					{
						JButton button = new JButton("Cancel");
						button.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								TrainDialog.this.dispose();
							}
						});
						buttons.add(button);
					}
				}
				this.add(buttons, BorderLayout.PAGE_END);
			}
			
		    {
				this.pack();
				this.setLocationRelativeTo(parent);
				this.setVisible(true);
		    }
		}
		
	}
	
	private class MouseCallback implements MouseMotionListener, MouseListener {
		
		@Override
		public void mouseMoved(MouseEvent e) {
			
			// scale
			Point.Double s = scale(e, figure.getScale());
			
			// status
			{
				status.x = s.x;
				status.y = s.y;
				status.color = figure.rawLayer.getColor((int) s.x, (int) s.y);
				status.refresh();
			}
			
			// touch
			if(figure.touchLayer.isEnabled()) {
				Find.Result<Node> result = figure.touchLayer.move(s.x, s.y);
				
				System.out.println(NodeUtil.toPathString(result.path));
				
				if(result.assigned) {
					targetPanel.setTarget(model.raw, result.path.getLast());
				}
				
				repaint();
			}
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			
			// scale
			Point.Double s = scale(e, figure.getScale());
			
			if(figure.touchLayer.isEnabled()) {
				Find.Result<Node> result = figure.touchLayer.move(s.x, s.y);
				
				if(result.assigned) {
					new TrainDialog(frame, result.path.getLast().getElement(), model.raw);
				}
			}
		}

		private org.testobject.commons.math.algebra.Point.Double scale(MouseEvent e, float scale) {
			// x y
			int x = e.getX();
			int y = e.getY();
			
			// scale
			double sx = Math.floor(x / scale);
			double sy = Math.floor(y / scale);
			
			return new Point.Double(sx, sy);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			
		}
	}

	// const
	private final float SCALE_STEP = 0.5f;

	// swing
	private final JFrame frame = new JFrame();
	private final JScrollPane imagePanel;
	private final TargetPanel targetPanel;
	private final JTree tree;

	// layers
	private final LayerFigure figure = new LayerFigure();
	
	// ui
	private final JMenu layerMenu = new JMenu("Layer");
	private final JMenu graphMenu = new JMenu("Graph");
	
	// state
	private final Registry.Mutable registry = new Registry.Mutable();
	private final Status status;
	private Model model;
	private int pass;
	private Graph graph;
	private File currentDir = null;

	public ParserGuiTool(File imageFile) throws IOException {
		// frame
		{
			this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		// figure
		{
			this.figure.setActiveLayer(this.figure.rawLayer);
			this.figure.resize();
		}

		// image pane
		{
			this.imagePanel = new JScrollPane(this.figure);
			this.imagePanel.setPreferredSize(new Dimension(600, 800));
			SwingUtils.fixScrollRendering(this.imagePanel.getViewport());
		}
		
		// target pane
		{
			this.targetPanel = new TargetPanel();
		}

		// layout
		{
			this.frame.getContentPane().setLayout(new BorderLayout());
		}

		// tree
		{
			this.tree = Tree.Factory.empty();
			this.tree.setPreferredSize(new Dimension(150, 500));
			this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
			this.tree.addTreeSelectionListener(new TreeSelectionListener() {
				public void valueChanged(TreeSelectionEvent e) {
					TreeNode node = (TreeNode) tree.getLastSelectedPathComponent();
					if (node == null) {
						return;
					}
					
					Rectangle.Int box = Tree.Util.getBoundingBox(node);
					figure.boxLayer.setBox(box);
					System.out.println(box);

					imagePanel.repaint();
				}
			});
		}

		// pane
		{
			// tree
			{
				JScrollPane treeScrollPane = new JScrollPane(tree);
				{
					SwingUtils.fixScrollRendering(treeScrollPane.getViewport());
					treeScrollPane.setPreferredSize(new Dimension(200, 600));
				}
						
				JScrollPane targetScrollPane = new JScrollPane(targetPanel);
				{
					SwingUtils.fixScrollRendering(targetScrollPane.getViewport());
					targetScrollPane.setPreferredSize(new Dimension(200, 200));
				}
				
				JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScrollPane, targetScrollPane);
				JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, imagePanel);
				this.frame.getContentPane().add(centerPane, BorderLayout.CENTER);
			}
		}

		// menu-bar
		{
			JMenuBar bar = new JMenuBar();
			{
				JMenu fileMenu = new JMenu("File");
				fileMenu.add(item("Load Image", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						ImageChooser.choose(frame, new ImageChooser.OnLoad() {
							
							@Override
							public void load(File ... files) {
								ParserGuiTool.this.parse(files[0]);
							}
						}, currentDir, false);
					}
				}));
				fileMenu.add(item("Exit", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						frame.dispose();
					}
				}));
				bar.add(fileMenu);
			}
			{
				bar.add(graphMenu);
				bar.add(layerMenu);
			}
			{
				JMenu layer = new JMenu("Options");
				layer.add(item("Selection box", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						figure.boxLayer.setEnabled(!figure.boxLayer.isEnabled());
						repaint();
					}
				}));
				layer.add(item("Bounding boxes", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						figure.boxesLayer.setEnabled(!figure.boxesLayer.isEnabled());
						repaint();
					}
				}));
				layer.add(item("Touch box", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						figure.touchLayer.setEnabled(!figure.touchLayer.isEnabled());
						repaint();
					}
				}));
				bar.add(layer);
			}
			this.frame.setJMenuBar(bar);
		}

		// toolbar
		{
			final JToolBar toolbar = new JToolBar();
			{
				final JButton button = new JButton("Zoom in");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						increaseScale(+SCALE_STEP);
						figure.resize();
						imagePanel.revalidate();
						repaint();
					}
				});
				toolbar.add(button);
			}
			{
				final JButton button = new JButton("Zoom out");
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						increaseScale(-SCALE_STEP);
						figure.resize();
						imagePanel.revalidate();
						repaint();
					}
				});
				toolbar.add(button);
			}
			this.frame.getContentPane().add(toolbar, BorderLayout.NORTH);
		}

		// status bar
		{
			final JLabel label = new JLabel("status");
			final JPanel statusPanel = new JPanel();
			statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
			label.setHorizontalAlignment(SwingConstants.LEFT);
			statusPanel.add(label);

			this.frame.add(statusPanel, BorderLayout.SOUTH);
			this.status = new Status(label);
		}

		// mouse
		{
			MouseCallback callback = new MouseCallback();
			figure.addMouseMotionListener(callback);
			figure.addMouseListener(callback);
		}

		// state
		{
//			this.train(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/icons/regions.png")), Qualifier.Factory.Class.icon, "region");
//			this.train(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/icons/tour.png")), Qualifier.Factory.Class.icon, "tour");
//			this.train(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/icons/map.png")), Qualifier.Factory.Class.icon, "map");
//			
//			this.train(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/images/regions.png")), Qualifier.Factory.Class.image, "region");
//			this.train(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/images/tour.png")), Qualifier.Factory.Class.image, "tour");
//			this.train(ImageUtil.Read.read(new File("../resources/src/main/resources/android/4_0_3/test/komoot/train/images/map.png")), Qualifier.Factory.Class.image, "map");

			this.parse(imageFile);
			
			this.setActiveLayer(figure.rawLayer);
			this.fillGraph(0, Graph.LOCATORS);
		}
	}

	private void rebuildGraphMenu(JMenu graph, Model model) {
		graph.removeAll();
		for(int i = 0; i < model.pass.length; i++) {
			graph.add(createGraphMenu(i));
		}
	}

	private JMenu createGraphMenu(final int i) {
		JMenu pass = new JMenu("Pass " + i);
		
		if(model != null) {
			if(model.pass[i].nodes.segments != null) {
				JMenu menu = new JMenu("Segments");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraph(i, Graph.SEGMENTS);
					}
				}));
				menu.add(item("Save", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						DirectoryChooser.choose(frame, new DirectoryChooser.OnLoad() {
							@Override
							public void load(File file) {
								try {
									MaskUtil.saveMasks(ImageUtil.Convert.toImage(model.raw), model.pass[i].nodes.segments, file);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, currentDir);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].nodes.groups != null) {
				JMenu menu = new JMenu("Groups");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraph(i, Graph.GROUPS);
					}
				}));
				menu.add(item("Save", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						DirectoryChooser.choose(frame, new DirectoryChooser.OnLoad() {
							@Override
							public void load(File file) {
								try {
									MaskUtil.saveMasks(ImageUtil.Convert.toImage(model.raw), model.pass[i].nodes.groups, file);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, currentDir);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].nodes.classify != null) {
				JMenu menu = new JMenu("Classes");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraph(i, Graph.CLASSES);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].nodes.prune != null) {
				JMenu menu = new JMenu("Prune");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraph(i, Graph.PRUNE);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].nodes.winners != null) {
				JMenu menu = new JMenu("Winners");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraph(i, Graph.WINNERS);
					}
				}));
				menu.add(item("Save", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						DirectoryChooser.choose(frame, new DirectoryChooser.OnLoad() {
							@Override
							public void load(File file) {
								try {
									MaskUtil.saveMasks(ImageUtil.Convert.toImage(model.raw), model.pass[i].nodes.winners, file);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}, currentDir);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].nodes.synthetic != null) {
				JMenu menu = new JMenu("Synthetic");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraph(i, Graph.SYNTHETIC);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].nodes.locators != null) {
				JMenu menu = new JMenu("Locators");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						fillGraph(i, Graph.LOCATORS);
					}
				}));
				pass.add(menu);
			}
		}
		
		return pass;
	}
	
	private void rebuildLayerMenu(JMenu graph, Model model) {
		graph.removeAll();
		for(int i = 0; i < model.pass.length; i++) {
			graph.add(createLayerMenu(i));
		}
	}
	
	private JMenu createLayerMenu(final int i) {
		JMenu pass = new JMenu("Pass " + i);
		
		if(model != null) {
			{
				JMenu menu = new JMenu("Raw");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						setActiveLayer(figure.rawLayer);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].images.segments != null) {
				JMenu menu = new JMenu("Segments");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						figure.segmentsLayer.setImage(model.pass[i].images.segments);
						setActiveLayer(figure.segmentsLayer);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].images.groups != null) {
				JMenu menu = new JMenu("Groups");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						figure.groupsLayer.setImage(model.pass[i].images.groups);
						setActiveLayer(figure.groupsLayer);
					}
				}));
				pass.add(menu);
			}
			if(model.pass[i].images.synthetic != null) {
				JMenu menu = new JMenu("Synthetic");
				menu.add(item("Show", new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						figure.syntheticLayer.setImage(model.pass[i].images.synthetic);
						setActiveLayer(figure.syntheticLayer);
					}
				}));
				pass.add(menu);
			}
		}
		
		return pass;
	}
	
	private void fillGraph(int passIndex, Graph graph) {

		// keep last selection
		{
			this.pass = passIndex;
			this.graph = graph;
		}
		
		Model.Pass pass = model.pass[passIndex];
		
		if(graph == Graph.SEGMENTS) {
			setBoxes(new BlobTranslater(), pass.nodes.segments);
			setTouches(pass.nodes.segments);
		}
		
		if(graph == Graph.GROUPS) {
			setBoxes(new NodeTranslater(), pass.nodes.groups);
			setTouches(pass.nodes.groups);	
		}
		
		if(graph == Graph.CLASSES) {
			setBoxes(new NodeTranslater(), pass.nodes.classify);
			setTouches(pass.nodes.classify);
		}
		
		if(graph == Graph.WINNERS) {
			setBoxes(new NodeTranslater(), pass.nodes.winners);
			setTouches(pass.nodes.winners);	
		}
		
		if(graph == Graph.PRUNE) {
			setBoxes(new NodeTranslater(), pass.nodes.prune);
			setTouches(pass.nodes.prune);
		}
		
		if(graph == Graph.SYNTHETIC) {
			setBoxes(new RenderGraphTranslater(), pass.nodes.synthetic);
		}
		
		if(graph == Graph.LOCATORS) {
			setBoxes(new LocatorTranslater(), pass.nodes.locators);
			setTouches(pass.nodes.winners);	
		}
		
		SwingUtils.expand(tree, 1);
		this.repaint();
	}
	
	private <T> void setBoxes(Translater<T> translator, T node) {
		this.figure.boxesLayer.setBoxes(Tree.Factory.fill(tree, translator, node));
	}
	
	private void setTouches(final Node node) {
		Adapter<Node> adapter = new Adapter<Node>() {
			@Override
			public LinkedList<Node> at(Point.Int location) {
				return NodeUtil.locate(node, location.x, location.y);
			}

			@Override
			public List<Node> childs(Node parent) {
				 return parent.getChildren();
			}

			@Override
			public Rectangle.Double toBox(Node node) {
				return toDoubleRect(node.getElement().getBoundingBox());
			}

			@Override
			public Mask toMask(Node t) {
				return Mask.Builder.create(t.getElement().getMasks());
			}
			
			private Rectangle.Double toDoubleRect(Rectangle.Int box) {
				return new Rectangle.Double(box.x, box.y, box.w, box.h);
			}

			@Override
			public boolean isGroup(Node t) {
				return t.getElement().getMasks().size() > 1;
			}
		};
		this.figure.touchLayer.setNodes(adapter, this.model.raw.getWidth(), this.model.raw.getHeight());
	}

	private void setActiveLayer(Layers.Raw renderable) {
		this.figure.setActiveLayer(renderable);
		this.repaint();
	}
	
	private void repaint() {
		this.frame.repaint(50);
	}

	private void increaseScale(float step) {
		this.figure.scale += step;
		this.status.refresh();
		this.repaint();
	}
	
	private void train(Image.Int image, Element element, String type, String id) {
		train(BlobUtils.Cut.cutByMask(image, Mask.Builder.create(element.getMasks())), type, id);
	}
		
	private void train(Image.Int cutByMask, String type, String id) {
		
		if(type.equals(Qualifier.Factory.Class.icon)) {
			org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Icon.Trainer();
			Sample sample = trainer.train(cutByMask, id);
			registry.put(sample);
		}
		
		if(type.equals(Qualifier.Factory.Class.image)) {
			org.testobject.kernel.classification.classifiers.advanced.Image.Trainer trainer = new org.testobject.kernel.classification.classifiers.advanced.Image.Trainer();
			Sample sample = trainer.train(cutByMask, id);
			registry.put(sample);
		}
	}
	
	private void parse(File file) {
		frame.setTitle(file.getName());
		ParserGuiTool.this.currentDir = file.getParentFile();
		try {
			parse(ImageIO.read(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void parse(BufferedImage image) {
		
		this.model = Parsing.parse(registry, image);
		
		this.rebuildGraphMenu(graphMenu, model);
		this.rebuildLayerMenu(layerMenu, model);
		
		this.fillGraph(pass, graph);
		this.figure.rawLayer.setImage(image);
		this.setActiveLayer(figure.rawLayer);

		this.repaint();
	}

	public void display() {
		this.frame.pack();
		this.frame.setVisible(true);
	}

	public static void main(String... args) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		// final File file = new File("../resources/src/main/resources/android/4_0_3/test/komoot/framebuffer/menu.png");
		// final File file = new File("../resources/src/main/resources/android/4_0_3/screenshots/emulator/komoot/me.png");
		// final File file = new File("../resources/src/main/resources/android/4_0_3/test/twitter/framebuffer/dialog.png");
		// final File file = new File("../resources/src/main/resources/android/4_0_3/replay/aral/3/before.raw.png");
		// final File file = new File("../resources/src/main/resources/android/4_0_3/matching/framebuffers/komoot/position_2_2_3.png");
		final File file = new File("../resources/src/main/resources/android/4_0_3/matching/framebuffers/komoot/fertig_2_3_3.png");
		
		final ParserGuiTool gui = new ParserGuiTool(file);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.display();
			}
		});
	}
}
