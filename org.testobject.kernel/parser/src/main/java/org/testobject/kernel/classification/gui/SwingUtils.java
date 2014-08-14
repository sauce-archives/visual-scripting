package org.testobject.kernel.classification.gui;

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * 
 * @author enijkamp
 *
 */
public class SwingUtils {
	
	public static void expand(JTree tree, int level) {
		DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
		while (currentNode != null) {
	       if (currentNode.getLevel() <= level) {
	    	   tree.expandPath(new TreePath(currentNode.getPath()));
	       }
	       currentNode = currentNode.getNextNode();
       }
	}
	
	public static JMenuItem item(String title, final ActionListener listener) {
		JMenuItem item = new JMenuItem(title);
		item.addActionListener(listener);
		return item;
	}
	
	public static void fixScrollRendering(JViewport viewport) {
		viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
	}
	
	public static JLabel toSwingImage(final BufferedImage image) {
        JLabel label = new JLabel(new ImageIcon(image));
        label.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        return label;
	}
}