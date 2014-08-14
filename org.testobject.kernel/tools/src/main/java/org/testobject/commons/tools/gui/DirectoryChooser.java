package org.testobject.commons.tools.gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 
 * @author enijkamp
 *
 */
public class DirectoryChooser {
	
	public interface OnLoad {
		void load(File file);
	}

    public static void choose(Component parent, OnLoad onLoad) {
          choose(parent, onLoad,  null);
    }
	
	public static void choose(Component parent, OnLoad onLoad, File currentDir) {
		JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(currentDir!=null){
            fileChooser.setCurrentDirectory(currentDir);
        }
		int rc = fileChooser.showOpenDialog(parent);
		if (rc == JFileChooser.APPROVE_OPTION) {
			onLoad.load(fileChooser.getSelectedFile());
		}
	}

}
