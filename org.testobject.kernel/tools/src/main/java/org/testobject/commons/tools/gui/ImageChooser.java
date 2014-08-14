package org.testobject.commons.tools.gui;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * 
 * @author enijkamp
 *
 */
public class ImageChooser {
	
	public interface OnLoad {
		void load(File... file);
	}

    public static void choose(Component parent, OnLoad onLoad) {
          choose(parent, onLoad,  null, false);
    }
	
	public static void choose(Component parent, OnLoad onLoad, File currentDir, boolean multipleFiles) {
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(multipleFiles);
        if(currentDir!=null){
            fileChooser.setCurrentDirectory(currentDir);
        }
		int rc = fileChooser.showOpenDialog(parent);
		if (rc == JFileChooser.APPROVE_OPTION) {
			onLoad.load(fileChooser.getSelectedFile() != null ? new File[]{fileChooser.getSelectedFile()} : fileChooser.getSelectedFiles());
		}
	}

}
