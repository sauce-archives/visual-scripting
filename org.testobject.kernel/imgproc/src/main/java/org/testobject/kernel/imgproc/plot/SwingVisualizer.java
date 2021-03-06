package org.testobject.kernel.imgproc.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.testobject.kernel.imgproc.util.ImageUtil;

/**
 * 
 * @author enijkamp
 * 
 */
public class SwingVisualizer implements Visualizer {

	private static class GetSizeOfFigure implements Graphics {

		private int height = 0, width = 0;

		private final java.awt.Graphics graphics;
		private final float scale;

		public GetSizeOfFigure(java.awt.Graphics graphics, float scale) {
			this.graphics = graphics;
			this.scale = scale;
		}

		public int getHeight() {
			return this.height;
		}

		public int getWidth() {
			return this.width;
		}

		private int scale(int value) {
			return (int) (value * this.scale);
		}

		private void checkBounds(int x, int y, int w, int h) {
			if ((scale(x) + scale(w)) > this.width) {
				this.width = (scale(x) + scale(w));
			}
			if ((scale(y) + scale(h)) > this.height) {
				this.height = (scale(y) + scale(h));
			}
		}

		@Override
		public void setColor(Color color) {

		}

		@Override
		public void setStrokeWidth(int width) {

		}

		@Override
		public void setFont(Font font) {
			this.graphics.setFont(font);
		}

		@Override
		public void fillRect(int x, int y, int w, int h) {
			checkBounds(x, y, w, h);
		}

		@Override
		public void drawLine(int x1, int y1, int x2, int y2) {
			if (Math.max(x1, x2) > this.width) {
				this.width = Math.max(x1, x2);
			}
			if (Math.max(y1, y2) > this.height) {
				this.height = Math.max(y1, y2);
			}
		}

		@Override
		public void drawRect(int x, int y, int w, int h) {
			checkBounds(x, y, w, h);
		}

		@Override
		public void drawOval(int x, int y, int w, int h) {
			checkBounds(x, y, w, h);
		}

		@Override
		public void drawString(String text, int x, int y) {
			if ((scale(x) + this.graphics.getFontMetrics().getStringBounds(text, this.graphics).getWidth()) > this.width) {
				this.width = (int) (scale(x) + this.graphics.getFontMetrics().getStringBounds(text, this.graphics).getWidth());
			}
			if ((scale(y) + this.graphics.getFont().getSize()) > this.height) {
				this.height = scale(y);
			}
		}

		@Override
		public void drawImage(BufferedImage image, int x, int y, int w, int h) {
			checkBounds(x, y, w, h);
		}

		@Override
		public void setAlpha(float alpha) {
			
		}
	}

	public static class DrawFigure implements Figure {

		// state
		public List<Renderable> layers = new LinkedList<>();
		public float scale = 1f;

		public DrawFigure(Renderable... renderable) {
			this.layers.addAll(Arrays.asList(renderable));
		}

		public Rectangle getSizeOfFigure() {
			// mock for graphics
			final BufferedImage mockBuffer = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);

			// bounding box
			final Rectangle box = new Rectangle(0, 0, 0, 0);

			// draw
			for (final Renderable renderable : this.layers) {
				if (renderable instanceof HasCustomScaling) {
					final HasCustomScaling custom = (HasCustomScaling) renderable;
					custom.setScale(this.scale);
					final GetSizeOfFigure mockGraphics = new GetSizeOfFigure(mockBuffer.getGraphics(), 1f);
					renderable.render(mockGraphics);
					box.width = mockGraphics.getWidth() > box.getWidth() ? mockGraphics.getWidth() : (int) box.getWidth();
					box.height = mockGraphics.getHeight() > box.getHeight() ? mockGraphics.getHeight() : (int) box.getHeight();
				} else {
					final GetSizeOfFigure mockGraphics = new GetSizeOfFigure(mockBuffer.getGraphics(), this.scale);
					renderable.render(mockGraphics);
					box.width = mockGraphics.getWidth() > box.getWidth() ? mockGraphics.getWidth() : (int) box.getWidth();
					box.height = mockGraphics.getHeight() > box.getHeight() ? mockGraphics.getHeight() : (int) box.getHeight();
				}
			}

			return box;
		}

		private void drawToAwt(java.awt.Graphics2D graphicsAwt) {
			for (final Renderable renderable : this.layers) {
				if (renderable instanceof HasCustomScaling) {
					final HasCustomScaling custom = (HasCustomScaling) renderable;
					custom.setScale(this.scale);
					renderable.render(new SwingGraphics(graphicsAwt, 1f));
				} else {
					renderable.render(new SwingGraphics(graphicsAwt, this.scale));
				}
			}
		}

		public BufferedImage drawToBuffer() {
			// determine size
			final Rectangle size = getSizeOfFigure();

			// buffer
			final BufferedImage rgb = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
			final java.awt.Graphics2D graphicsImage = (Graphics2D) rgb.getGraphics();

			try {
				drawToAwt(graphicsImage);
				return rgb;
			} finally {
				graphicsImage.dispose();
			}
		}

		@Override
		public void setScale(float scale) {
			this.scale = scale;
		}

		public float getScale() {
			return scale;
		}

		@Override
		public void addLayer(Renderable renderable) {
			this.layers.add(renderable);
		}
	}

	private class SwingFigure implements Figure, Window {
		
		@SuppressWarnings("serial")
		private class ImagePanel extends JPanel {
			
			private final DrawFigure figure;

			public ImagePanel(final DrawFigure figure) {
				this.figure = figure;
			}
			
			@Override
			public void paintComponent(java.awt.Graphics graphicsAwt) {
				super.paintComponent(graphicsAwt);
				Graphics2D graphicsAwt2D = (Graphics2D) graphicsAwt;
				if (thresholding == false) {
					// draw directly to component
					final BufferedImage buffer = figure.drawToBuffer();
					graphicsAwt2D.drawImage(buffer, 0, 0, null);
					last = buffer;
				} else {
					// draw to virtual buffer
					final BufferedImage buffer = ImageUtil.toBlackWhite(figure.drawToBuffer(), threshold);
					new SwingGraphics(graphicsAwt2D, 1f).drawImage(buffer, 0, 0, buffer.getWidth(), buffer.getHeight());
					last = buffer;
				}
			}
			
			public void resize() {
				final Rectangle size = figure.getSizeOfFigure();
				this.setPreferredSize(new Dimension((int) size.getX() + (int) size.getWidth(), (int) size.getY() + (int) size.getHeight()));
			}
		}
		

		// const
		private final float SCALE_STEP = 0.5f;

		// swing
		private final JFrame frame = new JFrame();

		// data
		private final DrawFigure figure;
		private BufferedImage last;
		
		// properties
		private boolean thresholding = false;
		private int threshold = 10;
		
		// mouse
		private MouseListener mouseListener;

		public SwingFigure(String title, final DrawFigure figure) {
			// members
			{
				this.figure = figure;
			}
			// frame
			{
				this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				this.frame.setTitle(title);
				this.frame.setSize(800, 800);
				this.frame.setVisible(true);
			}
			// menu
			{
				final JMenuBar menubar = new JMenuBar();
				final JMenu file = new JMenu("File");
				menubar.add(file);
				{
					final JMenuItem item = new JMenuItem("Save");
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							try {
								final String filename = JOptionPane.showInputDialog("Name");
								ImageIO.write(figure.drawToBuffer(), "png", new File(filename + ".png"));
							} catch (final IOException ex) {
								JOptionPane.showMessageDialog(null, ex.toString());
							}
						}
					});
					file.add(item);
				}
				final JMenu colors = new JMenu("Colors");
				menubar.add(colors);
				{
					final JMenuItem item = new JMenuItem("Threshold");
					item.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							final int threshold = Integer.parseInt(JOptionPane.showInputDialog("Limit"));
							setThreshold(threshold);
						}
					});
					colors.add(item);
				}
				this.frame.setJMenuBar(menubar);
			}
			// status bar
			final JLabel status = new JLabel("status");
			{
				final JPanel statusPanel = new JPanel();
				statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
				status.setHorizontalAlignment(SwingConstants.LEFT);
				statusPanel.add(status);

				this.frame.add(statusPanel, BorderLayout.SOUTH);
			}
			// panel
			final ImagePanel imagePanel = new ImagePanel(figure);
			final JScrollPane scrollPanel = new JScrollPane(imagePanel);
			{
				this.frame.getContentPane().add(scrollPanel, BorderLayout.CENTER);

				imagePanel.addMouseMotionListener(new MouseMotionListener() {

					@Override
					public void mouseMoved(MouseEvent e) {
						float scale = figure.getScale();
						double x = Math.floor(e.getX() / scale);
						double y = Math.floor(e.getY() / scale);
						Color color = getColor(e.getX(), e.getY());
						status.setText("scale: " + scale + " position (x,y): " + x + "," + y + " color (a, r,g,b): " + color.getAlpha()
								+ "," + color.getRed() + "," + color.getGreen() + "," + color.getBlue());
					}

					@Override
					public void mouseDragged(MouseEvent e) {
					}
				});
				
				imagePanel.addMouseMotionListener(new MouseMotionListener() {

					@Override
					public void mouseMoved(MouseEvent e) {
						if(mouseListener != null) {
							float scale = figure.getScale();
							int x = (int) Math.floor(e.getX() / scale);
							int y = (int) Math.floor(e.getY() / scale);
							mouseListener.move(x, y);
						}
					}

					@Override
					public void mouseDragged(MouseEvent e) {
					}
				});
			}
			// toolbar
			{
				final JToolBar toolbar = new JToolBar();
				{
					final JButton button = new JButton("Zoom in");
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							increaseScale(+SwingFigure.this.SCALE_STEP);
							imagePanel.resize();
							scrollPanel.revalidate();
						}
					});
					toolbar.add(button);
				}
				{
					final JButton button = new JButton("Zoom out");
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							increaseScale(-SwingFigure.this.SCALE_STEP);
							imagePanel.resize();
							scrollPanel.revalidate();
						}
					});
					toolbar.add(button);
				}
				{
					final JButton button = new JButton("Threshold");
					button.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							toogleThresholding();
						}
					});
					toolbar.add(button);
				}
				this.frame.getContentPane().add(toolbar, BorderLayout.NORTH);
			}
		}

		public Color getColor(int x, int y) {
			if (last != null && x < last.getWidth() && y < last.getHeight()) {
				return new Color(last.getRGB(x, y));
			} else {
				return Color.BLACK;
			}
		}
		
		public void setThreshold(int threshold) {
			this.threshold = threshold;
			this.frame.repaint();
		}

		public void toogleThresholding() {
			this.thresholding = !this.thresholding;
			this.frame.repaint();
		}

		public void increaseScale(float step) {
			this.figure.scale += step;
			this.frame.repaint();
		}

		@Override
		public void setTitle(String title) {
			this.frame.setTitle(title);
		}

		@Override
		public void setScale(float scale) {
			this.figure.scale = scale;
			this.frame.repaint();
		}

		@Override
		public void addLayer(Renderable renderable) {
			this.figure.layers.add(renderable);
			this.frame.repaint();
		}

		@Override
		public void close() {
			final WindowEvent wev = new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING);
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
		}

		@Override
		public Figure getFigure() {
			return this.figure;
		}

		@Override
		public void repaint() {
			this.frame.repaint();
		}

		@Override
		public void setMouseListener(MouseListener mouseListener) {
			this.mouseListener = mouseListener;
		}
	}

	public SwingVisualizer() {

	}

	@Override
	public DrawFigure figure(Renderable... renderable) {
		return new DrawFigure(renderable);
	}

	@Override
	public SwingFigure show(String title, Figure figure) {
		return new SwingFigure(title, (DrawFigure) figure);
	}

	@Override
	public BufferedImage draw(Figure figure) {
		final DrawFigure draw = (DrawFigure) figure;
		return draw.drawToBuffer();
	}
}
