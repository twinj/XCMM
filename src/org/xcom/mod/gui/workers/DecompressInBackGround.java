package org.xcom.mod.gui.workers;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.xcom.mod.Main;
import org.xcom.mod.gui.XCMGUI;

public class DecompressInBackGround extends SwingWorker<Void, Void> {
	
	private List<Path> decomFiles = new ArrayList<>();
	private JComponent src;
	
	public DecompressInBackGround(Path decomFile) {
		super();
		this.decomFiles.add(decomFile);
	}
	
	public DecompressInBackGround(List<Path> decomFiles) {
		super();
		this.decomFiles = decomFiles;
	}
	
	public DecompressInBackGround(List<Path> decomFiles, JComponent src) {
		super();
		this.decomFiles = decomFiles;
		this.src = src;
	}
	
	@Override
	protected Void doInBackground() {
		
		for (Path p : decomFiles) {
			Main.decompress(p);
		}
		return null;
	}
	
	@Override
	protected void done() {
		try {
			get();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace(System.err);
			decompressionFailed();
			return;			
		}
		for (Path p : decomFiles) {
			try {
				Main.sortGameFiles(p);
			} catch (IOException ex) {
				errorOrganisingFiles();
				ex.printStackTrace(System.err);
			}
		}
		if (src != null) {
			src.setEnabled(true);
		}
	}
	protected void decompressionFailed() {
		JOptionPane.showMessageDialog(XCMGUI.getFrame(),
				"There was an error decompressing the file(s)",
				"Gildor's Unreal Decompressor", JOptionPane.ERROR_MESSAGE);
	}
	
	protected void decompressionComplete() {
		JOptionPane.showMessageDialog(XCMGUI.getFrame(),
				"Decompressing the files has completed.",
				"Gildor's Unreal Decompressor", JOptionPane.PLAIN_MESSAGE);
	}
	
	protected void errorOrganisingFiles() {
		JOptionPane.showMessageDialog(XCMGUI.getFrame(),
				"There was an IO error when sorint out the game resources.",
				"Game file handler", JOptionPane.ERROR_MESSAGE);
	}
}
