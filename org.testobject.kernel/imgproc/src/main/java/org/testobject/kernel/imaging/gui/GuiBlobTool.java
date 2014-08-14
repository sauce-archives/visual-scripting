package org.testobject.kernel.imaging.gui;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.util.ImageUtil;
import org.testobject.commons.util.image.Image;
import org.testobject.kernel.imgproc.blob.Blob;
import org.testobject.kernel.imgproc.blob.BlobUtils;
import org.testobject.kernel.imgproc.blob.GraphBlobBuilder;
import org.testobject.kernel.imgproc.blob.Group;
import org.testobject.kernel.imgproc.blob.GroupBuilder;

/**
 * Simple Gui tool to visualize blob hierarchy
 * 
 * @author mike
 */
@SuppressWarnings("serial")
public class GuiBlobTool extends JFrame {
	
	private static final boolean GROUPING = false;
	private static final int GROUPS_FAT_X = 8;
	private static final int GROUPS_FAT_Y = 8;

	private Image.Int image;

	static class BlobGroup {
		public final List<Blob> blobs = new LinkedList<Blob>();
		public final Rectangle bbox = new Rectangle();
	}

	static class BlobNode extends DefaultMutableTreeNode {
		public final BoundingBox bbox;

		BlobNode(BoundingBox bbox) {
			this.bbox = bbox;
		}

		public String toString() {
			if (bbox instanceof Blob) {
				return "" + ((Blob) bbox).id;
			} else {
				return "group";
			}
		}
	}

	static class BlobTree extends JTree {
		private final JPopupMenu popup = new JPopupMenu();

		{
			getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

			JMenuItem item = new JMenuItem("show x-histogram");
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					showHistogramX();
				}

			});

			popup.add(item);

			item = new JMenuItem("show y-histogram");
			item.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					showHistogramY();
				}

			});

			popup.add(item);

			addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent ev) {
					mouseReleased(ev);
				}

				@Override
				public void mouseReleased(MouseEvent ev) {
					if (ev.isPopupTrigger()) {
						// thanks to urbanq for the bug fix!
						int row = BlobTree.this.getRowForLocation(ev.getX(), ev.getY());
						if (row == -1)
							return;
						BlobTree.this.setSelectionRow(row);
						popup.show(ev.getComponent(), ev.getX(), ev.getY());
					}
				}

			});
		}

		private JFrame locateJFrame() {
			Component c = this;

			while (c != null) {
				if (c instanceof JFrame) {
					return (JFrame) c;
				}

				c = c.getParent();
			}

			return null;
		}

		private void showHistogramX() {
			BlobNode node = (BlobNode) getLastSelectedPathComponent();

			if (node == null)
				return;

			if (node.bbox instanceof Blob) {
				Blob blob = (Blob) node.bbox;
				HistogramPlot.displayHistogramPlotXAsDialog(locateJFrame(), "blob id: " + blob.id, blob.children);
			} else {
				@SuppressWarnings("unchecked")
				Group<Blob> blob = (Group<Blob>) node.bbox;
				HistogramPlot.displayHistogramPlotXAsDialog(locateJFrame(), "group", blob.getContent());
			}
		}

		private void showHistogramY() {
			BlobNode node = (BlobNode) getLastSelectedPathComponent();

			if (node == null)
				return;

			if (node.bbox instanceof Blob) {
				Blob blob = (Blob) node.bbox;
				HistogramPlot.displayHistogramPlotYAsDialog(locateJFrame(), "blob id: " + blob.id, blob.children);
			} else {
				@SuppressWarnings("unchecked")
				Group<Blob> blob = (Group<Blob>) node.bbox;
				HistogramPlot.displayHistogramPlotYAsDialog(locateJFrame(), "group", blob.getContent());
			}

		}

		public void setBlob(Blob blob) {
			BlobNode node = buildBlobNode(new GroupBuilder<Blob>(), blob);
			((DefaultTreeModel) super.getModel()).setRoot(node);
		}

		private static Comparator<BoundingBox> COMPARATOR_X = new Comparator<BoundingBox>() {

			public int compare(BoundingBox x1, BoundingBox x2) {
				Rectangle r1 = x1.getBoundingBox();
				Rectangle r2 = x2.getBoundingBox();

				if (r1.x < r2.x) {
					return -1;
				} else if (r1.x > r2.x) {
					return 1;
				}

				return 0;
			}

		};

		private static BlobNode buildBlobNode(GroupBuilder<Blob> bld, Group<Blob> string) {
			BlobNode node = new BlobNode(string);

			PriorityQueue<BoundingBox> sorter = new PriorityQueue<BoundingBox>(1, COMPARATOR_X);
			sorter.addAll(string.getContent());

			BoundingBox box;
			while ((box = sorter.poll()) != null) {
				node.add(buildBlobNode(bld, (Blob) box));
			}

			return node;
		}

		private static BlobNode buildBlobNode(GroupBuilder<Blob> builder, Blob blob) {
			BlobNode node = new BlobNode(blob);

			if (blob.children.size() > 0) {
				if(GROUPING) {
					// try to detect horizontal strings of blobs there
					List<Group<Blob>> strings = builder.buildGroups(blob.children, GROUPS_FAT_X, GROUPS_FAT_Y); // merge (mostly) horizontally
	
					final int y = blob.bbox.y;
					Comparator<BoundingBox> COMPARATOR = new Comparator<BoundingBox>() {
						public int compare(BoundingBox x1, BoundingBox x2) {
							Rectangle r1 = x1.getBoundingBox();
							Rectangle r2 = x2.getBoundingBox();
	
							int y1 = (r1.y - y) / 10;
							int y2 = (r2.y - y) / 10;
	
							if (y1 < y2) {
								return -1;
							} else if (y1 > y2) {
								return 1;
							}
	
							if (r1.x < r2.x) {
								return -1;
							} else if (r1.x > r2.x) {
								return 1;
							}
	
							return 0;
						}
	
					};
	
					PriorityQueue<Group<Blob>> sorter = new PriorityQueue<Group<Blob>>(1, COMPARATOR);
	
					sorter.addAll(strings);
	
					Group<Blob> s;
					while ((s = sorter.poll()) != null) {
						if (s.getContent().size() == 1) {
							node.add(buildBlobNode(builder, (Blob) s.getContent().get(0)));
						} else {
							node.add(buildBlobNode(builder, s));
						}
					}
				} else {
					for(Blob child : blob.children) {
						node.add(buildBlobNode(builder, child));
					}
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

		public void setCurrentBlob(BoundingBox bbox) {
			this.currentBbox = bbox;
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
					System.out.println(currentBbox.getBoundingBox());

					Rectangle r = currentBbox.getBoundingBox();
					if (currentBbox instanceof Blob) {
						g.setPaint(Color.RED);
						Blob blob = (Blob) currentBbox;
						for (int y = 0; y < r.height; y++) {
							for (int x = 0; x < r.width; x++) {
								if (blob.get(x, y)) {
									g.drawLine(r.x + x, r.y + y, r.x + x, r.y + y);
								}
							}
						}
					} else {
						Composite oldComposite = g.getComposite();
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
						g.setPaint(Color.RED);
						// draw a border to make small blobs more visible
						int border = 10;
						for (int i = 0; i < 5; i++) {
							g.drawRect(r.x - border + i, r.y - border + i, r.width + 2 * border - 2 * i, r.height + 2 * border - 2 * i);
						}
						g.setPaint(Color.GREEN);
						// g.fillRect(r.x, r.y, r.w, r.h);
						g.setComposite(oldComposite);
					}
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

	public void setImage(BufferedImage image, String title) {
		this.image = ImageUtil.toImage(image);

		GraphBlobBuilder builder = new GraphBlobBuilder(image.getWidth(), image.getHeight());
		Blob[] blobs = builder.build(this.image);

		tree.setBlob(blobs[0]);
		imagePanel.setImage(ImageUtil.toBufferedImage(BlobUtils.drawHierarchy(blobs)));

		setTitle(title);
	}

	public GuiBlobTool() {
		super.setTitle("Gui Tool");

		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				// Returns the last path element of the selection.
				// This method is useful only when the selection model allows a single selection.
				BlobNode node = (BlobNode) tree.getLastSelectedPathComponent();

				if (node == null)
					// Nothing is selected.
					return;

				imagePanel.setCurrentBlob(node.bbox);
			}
		});

		JScrollPane leftPane = new JScrollPane(tree);
		leftPane.setPreferredSize(new Dimension(200, 200));

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
		menu.add(new MenuItem("Save Blobs") {
			{
				super.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						saveBlobs();
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
		} catch (HeadlessException e) {
			e.printStackTrace();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private void saveBlobs() {
		try {
            if(new File("/tmp/blobs").exists() == false) new File("/tmp/blobs").mkdir();
			Blob[] blobs = new GraphBlobBuilder(this.image.w, this.image.h).build(this.image);
			for (int i = 0; i < blobs.length; i++) {
				Blob blob = blobs[i];
				Image.Int image = BlobUtils.cutByMask(this.image, blob);
				ImageIO.write(ImageUtil.toBufferedImage(image), "png", new File("/tmp/blobs/blob" + i + ".png"));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String... av) throws Exception {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		final GuiBlobTool gui = new GuiBlobTool();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.setImage(new File("src/test/resources/android/4_0_3/screenshots/kaufda/kaufda-angebot.png"));
				gui.pack();
				gui.setVisible(true);
			}
		});

	}

}
