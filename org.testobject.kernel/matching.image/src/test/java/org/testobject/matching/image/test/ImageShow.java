package org.testobject.matching.image.test;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ImageShow extends JPanel {

	public static void displayImage(String imgPath) {
		try {
			JFrame frame = new JFrame("Result");
			BufferedImage img = ImageIO.read(new File(imgPath));

			ImageIcon icon = new ImageIcon(img);
			JLabel label = new JLabel(icon);
			JOptionPane.showMessageDialog(frame, label);

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
