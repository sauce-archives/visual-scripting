package org.testobject.matching.image.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import samples.ResourceResolver;

@SuppressWarnings("serial")
public class TestFilesUtil extends JFrame {

	public static void main(String[] args) throws IOException {
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				
				BufferedImage image;
				try {
					
					image = ImageIO.read(ResourceResolver.getResource("samples/wrong_match/ich_bin_kunde_record.png"));
					new TestFilesUtil().start(image);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}); 
	}     

	void start(BufferedImage image) {
		ImagePanel panel = new ImagePanel(image);
		add(panel);
		setVisible(true);
		setSize(panel.getMaximumSize());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
	}

	class ImagePanel extends JPanel {
		private BufferedImage img;
		private Rectangle selection;
		
		public ImagePanel(BufferedImage img) {
			this.img = img;
			Dimension size = new Dimension(img.getWidth(), img.getHeight());
			setPreferredSize(size);
			setMinimumSize(size);
			setMaximumSize(size);
			setSize(size);
			setLayout(null);
			
			this.addMouseMotionListener(new MouseMotionListener(){

				@Override
				public void mouseDragged(MouseEvent event) {
					selection.width = event.getX() - selection.x;
					selection.height = event.getY() - selection.y;
					repaint();					
				}

				@Override
				public void mouseMoved(MouseEvent event) {					
				}				
			}); 
			
			this.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent arg0) {}

				@Override
				public void mouseEntered(MouseEvent arg0) {}

				@Override
				public void mouseExited(MouseEvent arg0) {}

				@Override
				public void mousePressed(MouseEvent event) {				
					selection = new Rectangle(event.getX(), event.getY(), 0, 0);
				}

				@Override
				public void mouseReleased(MouseEvent event) {
					System.out.println("new Rectangle.Int(" + selection.x + ", " + selection.y + ", " + selection.width + ", " + selection.height + ")");				
				}			
			}); 
		}

		public void paintComponent(Graphics g) {
			g.drawImage(img, 0, 0, null);
			g.setColor(Color.RED);
			
			if (selection != null) {				
				g.drawRect(selection.x, selection.y, selection.width, selection.height);
			}
			g.dispose();			
		}
	}
}
