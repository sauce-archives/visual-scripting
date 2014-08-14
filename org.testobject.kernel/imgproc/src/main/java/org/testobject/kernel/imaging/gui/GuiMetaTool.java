package org.testobject.kernel.imaging.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.blob.Meta;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.classifier.ButtonClassifier;
import org.testobject.kernel.imgproc.classifier.Classifier;
import org.testobject.kernel.imgproc.classifier.Context;
import org.testobject.kernel.imgproc.classifier.Dependencies;
import org.testobject.kernel.imgproc.classifier.GroupClassifier;
import org.testobject.kernel.imgproc.classifier.IconClassifier;
import org.testobject.kernel.imgproc.classifier.ImageClassifier;
import org.testobject.kernel.imgproc.classifier.PopupClassifier;
import org.testobject.kernel.imgproc.classifier.TextBoxClassifier;
import org.testobject.kernel.imgproc.classifier.TextCharClassifier;
import org.testobject.kernel.imgproc.classifier.TextLineClassifier;
import org.testobject.kernel.imgproc.classifier.TextParagraphClassifier;
import org.testobject.kernel.imgproc.classifier.TextWordClassifier;
import org.testobject.kernel.imgproc.classifier.VisitingMutator;

/**
 * 
 * @author enijkamp
 * @author mike
 */
@SuppressWarnings("serial")
public class GuiMetaTool extends JFrame {
	
	private static final Classifier[] CLASSIFIER = Dependencies.order(new Classifier[] {
	        new GroupClassifier(),
	        new ImageClassifier(),
	        new ButtonClassifier(),
	        new TextCharClassifier(),
	        new TextBoxClassifier(),
	        new TextWordClassifier(),
	        new TextLineClassifier(),
	        new TextParagraphClassifier(),
	        new IconClassifier(),
	        new PopupClassifier() 
	});

	static class BlobNode extends DefaultMutableTreeNode {
		public final Blob blob;

		BlobNode(Blob blob) {
			this.blob = blob;
		}

		public String toString() {
			// FIXME hack (en)
			return (Meta.blob == blob.meta ? "Blob" : blob.meta) + " (" + blob.id + ")";
		}
	}

	static class BlobTree extends JTree {
		public void setBlob(Blob blob) {
			BlobNode treeNode = buildBlobNode(blob);
			((DefaultTreeModel) super.getModel()).setRoot(treeNode);
		}

		private static BlobNode buildBlobNode(Blob blob) {
			BlobNode node = new BlobNode(blob);

			if (blob.children.size() > 0) {
				for (Blob child : blob.children) {
					node.add(buildBlobNode(child));
				}
			}

			return node;
		}
	}

	static class ImagePanel extends JPanel {
		private BufferedImage image = null;

		private BoundingBox currentBbox = null;

		public ImagePanel() {
			setImage(null);
		}

		public void setImage(BufferedImage image) {
			this.image = image;
			this.currentBbox = null;

			if (image == null) {
				this.setSize(1000, 600);
			} else {
				this.setSize(image.getWidth(), image.getHeight());
			}
			this.setPreferredSize(getSize());
			this.setMinimumSize(getSize());
		}

		public void setCurrentBlob(Blob blob) {
			this.currentBbox = blob;
			System.out.println(blob);
			this.repaint(50);
		}

		@Override
		public void paintComponent(Graphics graphics) {
			Graphics2D g = (Graphics2D) graphics;
			if (image == null) {
				Dimension size = getSize();
				g.fillRect(0, 0, size.width, size.height);
			} else {
				g.drawImage(image, 0, 0, null);
				if (currentBbox != null) {
					Composite oldComposite = g.getComposite();
					g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

					Rectangle r = currentBbox.getBoundingBox();
					g.setPaint(Color.RED);

					// draw a border to make small blobs more visible
					int border = 10;
					for (int i = 0; i < 5; i++) {
						g.drawRect(r.x - border + i, r.y - border + i, r.width + 2 * border - 2 * i, r.height + 2 * border - 2 * i);
					}

					g.setPaint(Color.GREEN);
					g.fillRect(r.x, r.y, r.width, r.height);
					g.setComposite(oldComposite);
				}
			}
		}
	}

	private final BlobTree tree = new BlobTree();

	private final ImagePanel imagePanel = new ImagePanel();
	private final JFileChooser imageFileChooser = new JFileChooser();

	public void setImage(File file) {
		BufferedImage image;
		try {
			image = ImageIO.read(file);
			setImage(image, "Gui Tool: " + file);

			imageFileChooser.setCurrentDirectory(file.getParentFile());
			imageFileChooser.setSelectedFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setImage(BufferedImage buf, String title) {
		Image.Int image = ImageUtil.toImageInt(buf);

		GraphBlobBuilder builder = new GraphBlobBuilder(image.w, image.h);

		Context context = new Context(image, image);

		Blob blobs = builder.build(image)[0];

		for (Classifier classifier : CLASSIFIER) {
			new VisitingMutator(classifier).mutate(context, blobs);
		}

		tree.setBlob(blobs);

		imagePanel.setImage(buf);

		setTitle(title);
	}

	public GuiMetaTool() {
		super.setTitle("Gui Tool");

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				// Returns the last path element of the selection.
				// This method is useful only when the selection model allows a single selection.
				BlobNode node = (BlobNode) tree.getLastSelectedPathComponent();

				if (node == null)
					// Nothing is selected.
					return;

				imagePanel.setCurrentBlob(node.blob);
			}
		});

		JScrollPane leftPane = new JScrollPane(tree);
		leftPane.setPreferredSize(new Dimension(250, 200));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, imagePanel);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(splitPane, BorderLayout.CENTER);

		MenuBar mb = new MenuBar();
		Menu menu = new Menu("File");
		menu.add(new MenuItem("Reload Image") {
			{
				super.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						setImage(imageFileChooser.getSelectedFile());
					}
				});
			}
		});
		menu.add(new MenuItem("Load Image") {
			{
				super.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						onLoadImage();
					}
				});
			}
		});
		menu.add(new MenuItem("Load from Screen") {
			{
				super.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						onLoadScreen();
					}
				});
			}
		});
		menu.add(new MenuItem("Exit") {
			{
				super.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						onExit();
					}
				});
			}
		});
		mb.add(menu);
		setMenuBar(mb);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				onExit();
			}
		});
	}

	private void onExit() {
		super.dispose();
	}

	private void onLoadImage() {
		int rc = imageFileChooser.showOpenDialog(this);
		if (rc == JFileChooser.APPROVE_OPTION) {
			setImage(imageFileChooser.getSelectedFile());
		}
	}

	private void onLoadScreen() {
		try {
			BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			setImage(image, "Image from Screen");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String... av) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		final GuiMetaTool gui = new GuiMetaTool();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.setImage(new File("src/test/resources/android/4_0_3/screenshots/komoot/MeinKomoot.png"));
				gui.pack();
				gui.setVisible(true);
			}
		});
	}
}
