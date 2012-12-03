package org.xcom.mod.gui.workers;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.xcom.main.shared.Main;

public abstract class DownloadWorker extends SwingWorker<Path, Void> {
	
	private URL url;
	@SuppressWarnings("unused")
	private Path saveToDir;
	private String saveAs;
	private JComponent src;
	
	public DownloadWorker(final String url, final Path saveToDir,
			final String saveAs, final JComponent src) throws MalformedURLException {
		
		super();	
		this.url = new URL(url);	
		this.saveToDir = saveToDir;
		this.saveAs = saveAs;
		this.src = src;
	}
	
	@Override
	protected Path doInBackground() throws Exception {
		if (src !=null) {
			src.setEnabled(false);
		}
		return Main.download(saveAs, url);
	}
	
	@Override
	protected void done() {
		try {
			get();
		} catch (InterruptedException | ExecutionException ex) {
			 if (src !=null) {
				 src.setEnabled(true);
			 }
			ex.printStackTrace(System.err);
		}
	}
	
	protected void downloadComplete() {
		JOptionPane.showMessageDialog(Main.getFrame(), saveAs
				+ " has finished downloading.", "Download finished.",
				JOptionPane.PLAIN_MESSAGE);
	}
	
	protected void downloadFailedMsg() {
		JOptionPane.showMessageDialog(Main.getFrame(), "The download from ["
				+ url + "] failed.", "Download Failed", JOptionPane.ERROR_MESSAGE);
	}
	
	protected void downloadUnzipped(String filename) {
		JOptionPane.showMessageDialog(Main.getFrame(), filename
				+ " has been setup.", "Download unzipped.", JOptionPane.PLAIN_MESSAGE);
	}
	
	protected void downloadUnzipFailed() {
		JOptionPane.showMessageDialog(Main.getFrame(), "Unzipping " + saveAs
				+ " has failed.", "Download unzip failed.", JOptionPane.ERROR_MESSAGE);
	}
	
	protected void downloadCompleteAndUnzipped() {
		JOptionPane.showMessageDialog(Main.getFrame(), "[" + saveAs
				+ "] has finished downloading and has been unzipped.",
				"Task finished.", JOptionPane.PLAIN_MESSAGE);
	}
}
