package org.xcom.mod.gui.workers;

import java.awt.Cursor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.xcom.mod.Main;
import org.xcom.mod.gui.XCMGUI;

public class ExtractInBackGround extends SwingWorker<Void, Void> {
	
	private List<Path> decomFiles = new ArrayList<>();
	private JComponent src = null;
	
	public ExtractInBackGround(Path decomFile) {
		super();
		this.decomFiles.add(decomFile);
	}
	
	public ExtractInBackGround(List<Path> decomFiles) {
		super();
		this.decomFiles = decomFiles;
	}
	
	public ExtractInBackGround(List<Path> decomFiles, JComponent src) {
		this(decomFiles);
		this.src = src;
	}
	
	public ExtractInBackGround(Path decomFile, JComponent src) {
		this(decomFile);
		this.src = src;
	}
	
	@Override
	protected Void doInBackground() {
		XCMGUI.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		if (src != null) {
			src.setEnabled(false);
		}
		for (Path p : decomFiles) {
			Main.extract(p);
		}
		return null;
	}
	
	@Override
	protected void done() {
		try {
			XCMGUI.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			if (src != null) {
				src.setEnabled(true);
			}
			
			get();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace(System.err);
			extractionFailed();
			return;			
		}	
		
		extractionComplete();
		
	}
	protected void extractionFailed() {
		JOptionPane.showMessageDialog(XCMGUI.getFrame(),
				"There was an error extracting the resources.",
				"Gildor's Unreal Extractor", JOptionPane.ERROR_MESSAGE);
	}
	
	protected void extractionComplete() {
		JOptionPane.showMessageDialog(XCMGUI.getFrame(),
				"Extracting the resources has completed.",
				"Gildor's Unreal Extractor", JOptionPane.PLAIN_MESSAGE);
	}

}
