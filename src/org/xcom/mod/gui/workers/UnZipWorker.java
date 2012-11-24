package org.xcom.mod.gui.workers;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.xcom.mod.Main;
import org.xcom.mod.gui.XCMGUI;

public abstract class UnZipWorker extends SwingWorker<Void, Void> {
	
	private Path saveToDir;
	private Path zipDir;
	
	public UnZipWorker(Path zipDir, Path saveToDir) {
		this.saveToDir = saveToDir;
		this.zipDir = zipDir;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		Main.unZip(zipDir, saveToDir);
		return null;
	}
	
	@Override
	protected void done() {
		try {
			get();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace();
		}
	}
	
	protected void unzipComplete(String filename) {
		JOptionPane.showMessageDialog(XCMGUI.getFrame(), filename
				+ " has finished unzipping.", "Unzip finished.",
				JOptionPane.PLAIN_MESSAGE);
	}
	
	protected void unZipFailed() {
		JOptionPane.showMessageDialog(XCMGUI.getFrame(),
				"An error has occured while unzipping the file.", "Unzip failed",
				JOptionPane.ERROR_MESSAGE);
	}
	
}
