package org.testobject.kernel.inference.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.testobject.commons.math.algebra.Point;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.tools.plot.ScalableFigure;
import org.testobject.commons.tools.plot.Visualizer.Renderable;
import org.testobject.kernel.classification.gui.Layers;
import org.testobject.kernel.classification.gui.SwingUtils;
import org.testobject.kernel.classification.gui.Tree;
import org.testobject.kernel.classification.gui.ParserGuiTool.Model.Pass;
import org.testobject.kernel.classification.gui.ParserGuiTool.Model.Pass.Images;
import org.testobject.kernel.classification.gui.ParserGuiTool.Model.Pass.Nodes;

/**
 * 
 * @author enijkamp
 *
 */
public class InterpreterGuiTool {
	
	@SuppressWarnings("serial")
	private static class View extends JPanel {
		
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
					public org.testobject.kernel.api.classification.graph.Node group;
					public org.testobject.kernel.api.classification.graph.Node classify;
					public org.testobject.kernel.api.classification.graph.Node prune;
					public org.testobject.kernel.api.classification.graph.Node winners;
					public org.testobject.kernel.imaging.procedural.Node synthetic;
					public org.testobject.kernel.api.classification.graph.Locator.Node locators;
				}
				
				public class Images {
					public BufferedImage segments;
					public BufferedImage group;
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
		
		private static class LayerFigure extends ScalableFigure {
			
			public final Layers.Raw rawLayer = new Layers.Raw();
			public final Layers.Raw segmentsLayer = new Layers.Raw();
			public final Layers.Raw groupsLayer = new Layers.Raw();
			public final Layers.Raw syntheticLayer = new Layers.Raw();
			
			public final Layers.BoundingBoxes boxesLayer = new Layers.BoundingBoxes();
			public final Layers.Box boxLayer = new Layers.Box();
			public final Layers.Touch touchLayer = new Layers.Touch();
			
			public void setActiveLayer(Renderable renderable) {
				this.removeLayers();
				this.addLayer(renderable);
				this.addLayer(this.boxesLayer);
				this.addLayer(this.boxLayer);
				this.addLayer(this.touchLayer);
			}

		}
		
		private class MouseCallback implements MouseMotionListener, MouseListener {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				
				// scale
				Point.Double s = scale(e, figure.getScale());
				
				// touch
				if(figure.touchLayer.isEnabled()) {
					figure.touchLayer.move(s.x, s.y);
					repaint();
				}
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				
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

		// state
		private final Model model;
		
		// layers
		private final LayerFigure figure = new LayerFigure();
		
		// swing
		private final JScrollPane imagePane;
		private final JScrollPane treePane;
		private final JTree tree;
		
		public View(Model model) {
			// members
			{
				this.model = model;
			}
			
			// figure
			{
				this.setActiveLayer(this.figure.rawLayer);
				this.figure.resize();
			}
			
			// TODO add combo box to select layer (en)
			
			// image pane
			{
				this.imagePane = new JScrollPane(this.figure);
				this.imagePane.setPreferredSize(new Dimension(600, 800));
				SwingUtils.fixScrollRendering(this.imagePane.getViewport());
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

						imagePane.repaint();
					}
				});
			}
			
			// tree pane 
			{
				this.treePane = new JScrollPane(tree);
				{
					SwingUtils.fixScrollRendering(treePane.getViewport());
					treePane.setPreferredSize(new Dimension(200, 600));
				}
			}
			
			// TODO add combo box to select tree (en)
			
			// pane
			{
				JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, imagePane);
				this.add(pane, BorderLayout.CENTER);
			}
			
			// mouse
			{
				MouseCallback callback = new MouseCallback();
				figure.addMouseMotionListener(callback);
				figure.addMouseListener(callback);
			}
		}
		
		private void setActiveLayer(Renderable renderable) {
			this.figure.setActiveLayer(renderable);
			this.repaint();
		}
	}

}
