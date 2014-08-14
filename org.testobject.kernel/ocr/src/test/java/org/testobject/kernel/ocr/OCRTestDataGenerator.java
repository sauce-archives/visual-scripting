package org.testobject.kernel.ocr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.kernel.imaging.segmentation.Group;
import org.testobject.kernel.imaging.segmentation.GroupBuilder;
import org.testobject.kernel.ocr.OCR.Result;
import org.testobject.kernel.ocr.tesseract.OptimizedTesseractOCR;

import samples.ResourceResolver;

public class OCRTestDataGenerator {

	public enum Resizer {
		NEAREST_NEIGHBOR {
			@Override
			public BufferedImage resize(BufferedImage source,
					int width, int height) {
				return commonResize(source, width, height,
						RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			}
		},
		BILINEAR {
			@Override
			public BufferedImage resize(BufferedImage source,
					int width, int height) {
				return commonResize(source, width, height,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			}
		},
		BICUBIC {
			@Override
			public BufferedImage resize(BufferedImage source,
					int width, int height) {
				return commonResize(source, width, height,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			}
		},
		PROGRESSIVE_BILINEAR {
			@Override
			public BufferedImage resize(BufferedImage source,
					int width, int height) {
				return progressiveResize(source, width, height,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			}
		},
		PROGRESSIVE_BICUBIC {
			@Override
			public BufferedImage resize(BufferedImage source,
					int width, int height) {
				return progressiveResize(source, width, height,
						RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			}
		},
		AVERAGE {
			@Override
			public BufferedImage resize(BufferedImage source,
					int width, int height) {
				Image img2 = source.getScaledInstance(width, height,
						Image.SCALE_AREA_AVERAGING);
				BufferedImage img = new BufferedImage(width, height,
						source.getType());
				Graphics2D g = img.createGraphics();
				try {
					g.drawImage(img2, 0, 0, width, height, null);
				} finally {
					g.dispose();
				}
				return img;
			}
		};

		public abstract BufferedImage resize(BufferedImage source,
				int width, int height);

		private static BufferedImage progressiveResize(BufferedImage source,
				int width, int height, Object hint) {
			int w = Math.max(source.getWidth() / 2, width);
			int h = Math.max(source.getHeight() / 2, height);
			BufferedImage img = commonResize(source, w, h, hint);
			while (w != width || h != height) {
				BufferedImage prev = img;
				w = Math.max(w / 2, width);
				h = Math.max(h / 2, height);
				img = commonResize(prev, w, h, hint);
				prev.flush();
			}
			return img;
		}

		private static BufferedImage commonResize(BufferedImage source,
				int width, int height, Object hint) {
			BufferedImage img = new BufferedImage(width, height,
					source.getType());
			Graphics2D g = img.createGraphics();
			try {
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
				g.drawImage(source, 0, 0, width, height, null);
			} finally {
				g.dispose();
			}
			return img;
		}
	};

	abstract class MouseListener implements MouseMotionListener, java.awt.event.MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mouseDragged(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {};

		@Override
		public void mouseExited(MouseEvent e) {}

		@Override
		public void mouseMoved(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

	}

	class SelectionListener extends MouseListener {

		private final ImagePanel panel;
		private Point startPoint;

		public SelectionListener(ImagePanel panel) {
			this.panel = panel;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			startPoint = event.getPoint();
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			if (startPoint == null) {
				return;
			}

			panel.setSelection(new java.awt.Rectangle(startPoint.x, startPoint.y, (int) (event.getX() - startPoint.getX()), (int) (event
					.getY() - startPoint.getY())));
			panel.repaint();
		}

	}

	class RunOCRListener extends MouseListener {

		private final TextArea log;
		private final ImagePanel imagePanel;

		private Point startPoint = null;

		public RunOCRListener(ImagePanel imagePanel, TextArea log) {
			this.imagePanel = imagePanel;
			this.log = log;
		}

		@Override
		public void mousePressed(MouseEvent event) {
			startPoint = event.getPoint();
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			Point endPoint = event.getPoint();
			BufferedImage image = imagePanel.image;
			Rectangle.Int region = new Rectangle.Int(startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
			if (region.w == 0 || region.h == 0) {
				return;
			}

			System.out.println("scanning on region " + region);

			log.setText(log.getText().concat("\n " + "scanning on region " + region));
			List<Result> text = ocr.getText(image, region, 400, 3);
			for (Result result : text) {
				log.setText(log.getText().concat("\n " + result));
				System.out.println(result);
			}
			log.setText(log.getText().concat("\n " + "Done " + text.size() + " results"));
			System.out.println("Done " + text.size() + " results");

			//			log.setText(log.getText().concat("\n " + region + ", \"" + text.get(0).getText().replace("\n", "") + "\", " + imagePanel.getFile()));
			//			log.setText(log.getText().concat("\n\"" + text.get(0).getText().replace("\n", "") + "\",\"" + imagePanel.getFile()) + "\"," + region.x + "," + region.y + "," + region.w + "," + region.h);
			if (text.isEmpty() == false) {
				log.setText(log.getText().concat("\n.add(\"" + imagePanel.getFile()) + "\", new TextMatch(\""
						+ text.get(0).getText().replace("\n", "") + "\"," + region.x + "," + region.y + "," + region.w + "," + region.h
						+ "))");
			}

			List<Group<Result>> groups = new GroupBuilder<Result>().buildGroups(text, new Insets(0, 0, 0, 0));
			imagePanel.setGroups(groups);

			imagePanel.repaint();
		}

	}

	@SuppressWarnings("serial")
	public static class ImagePanel extends JPanel {

		private File file;
		private BufferedImage image;
		private java.awt.Rectangle selectionBox;
		private List<Group<Result>> groups;

		public ImagePanel() {}

		public ImagePanel(String path) {
			setImage(path);
		}

		public ImagePanel(BufferedImage image) {
			setImage(image);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image, 0, 0, null);
			if (selectionBox != null) {
				g.setColor(Color.RED);
				g.drawRect(selectionBox.x, selectionBox.y, selectionBox.width, selectionBox.height);

				if (groups != null) {
					g.setColor(Color.GREEN);
					for (Group<Result> group : groups) {
						g.drawRect(group.getBoundingBox().x, group.getBoundingBox().y, group.getBoundingBox().w, group.getBoundingBox().h);
					}
				}
			}
		}

		public void setGroups(List<Group<Result>> groups) {
			this.groups = groups;

		}

		public void setSelection(java.awt.Rectangle selectionBox) {
			this.selectionBox = selectionBox;

			this.repaint();
		}

		public void setImage(File file) {
			try {
				this.file = file;
				this.image = ImageIO.read(file);
				//				this.image = ImageUtil.scale(image, image.getWidth() * 3, image.getHeight() * 3);
				//				this.image = Resizer.PROGRESSIVE_BILINEAR.resize(image, image.getWidth() *3, image.getHeight() * 3);
				this.setPreferredSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
				this.repaint();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void setImage(String path) {
			try {
				this.file = new File(path);
				this.image = ImageIO.read(ResourceResolver.getResource(path));
				this.setPreferredSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
				this.repaint();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		public void setImage(BufferedImage image) {
			this.image = image;
			this.setPreferredSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
			this.repaint();
		}

		public File getFile() {
			return file;
		}

	}

	private final OCR ocr = new OptimizedTesseractOCR();

	private final JFrame frame = new JFrame();

	private ImagePanel imagePanel;

	public OCRTestDataGenerator() {
		this(new ImagePanel());
	}

	public OCRTestDataGenerator(String path) {
		this(new ImagePanel(path));
	}

	public OCRTestDataGenerator(BufferedImage image) {
		this(new ImagePanel(image));
	}

	public OCRTestDataGenerator(final ImagePanel imagePanel) {
		this.imagePanel = imagePanel;

		// frame
		{
			this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.frame.setTitle("Select OCR Region");
		}

		TextArea textArea = new TextArea("test");
		textArea.setRows(10);

		SelectionListener selectionListener = new SelectionListener(imagePanel);
		imagePanel.addMouseListener(new RunOCRListener(imagePanel, textArea));
		imagePanel.addMouseListener(selectionListener);
		imagePanel.addMouseMotionListener(selectionListener);

		//select file dialog
		ActionListener selectFileListener = new ActionListener() {

			private File file;

			@Override
			public void actionPerformed(ActionEvent event) {
				java.awt.FileDialog fileDialog = new java.awt.FileDialog((java.awt.Frame) null, "Select a png.", FileDialog.LOAD);
				if (file != null) {
					fileDialog.setDirectory(file.getParent());
				}
				fileDialog.setVisible(true);
				File[] files = fileDialog.getFiles();
				file = files[0];
				imagePanel.setImage(file);
			}
		};

		// menu-bar
		final JMenuBar bar = new JMenuBar();
		{
			JMenu menu = new JMenu("File");
			{
				JMenuItem open = new JMenuItem("Open File...");
				open.addActionListener(selectFileListener);
				menu.add(open);
			}

			bar.add(menu);
			this.frame.setJMenuBar(bar);
		}

		//Create a split pane with the two scroll panes in it.
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(new JScrollPane(imagePanel));
		splitPane.setRightComponent(new JScrollPane(textArea));

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(150);

		frame.add(splitPane);

	}

	public void display() {
		this.frame.pack();
		this.frame.setVisible(true);
	}

	public void setImage(String path) {
		imagePanel.setImage(path);
	}

	public void setRegion(Rectangle.Int region) {
		imagePanel.setSelection(new java.awt.Rectangle(region.x, region.y, region.w, region.h));
	}

	public static void main(String[] args) {
		final OCRTestDataGenerator gui = new OCRTestDataGenerator();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui.display();
			}
		});
	}

}
