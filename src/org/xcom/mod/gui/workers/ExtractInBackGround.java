package org.xcom.mod.gui.workers;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.xcom.main.shared.Main;

public class ExtractInBackGround extends SwingWorker<Void, Void>
			implements
				PropertyChangeListener {
	
	private List<Path> decomFiles = new ArrayList<>();
	private ProgressMonitor progressMonitor = new ProgressMonitor(Main.getFrame(),
				"Extracting resources..", "", 0, 100);
	
	private JComponent src = null;
	private Component parent = null;
	//private boolean monitorProgress = true;
	private String currentUpk;
	
	private volatile Process proc;
	private int print;
	
	public ExtractInBackGround(Path decomFile, Component parent) {
		super();
		this.decomFiles.add(decomFile);
		this.parent = parent;
		progressMonitor.setProgress(0);
		addPropertyChangeListener(this);
	}
	
	public ExtractInBackGround(List<Path> decomFiles, Component parent) {
		super();
		this.decomFiles = decomFiles;
		this.parent = parent;
		progressMonitor.setProgress(0);
		addPropertyChangeListener(this);
	}
	
	public ExtractInBackGround(List<Path> decomFiles, JComponent src, Component parent) {
		this(decomFiles, parent);
		this.src = src;
	}
	
	public ExtractInBackGround(Path decomFile, JComponent src, Component parent) {
		this(decomFile, parent);
		this.src = src;
	}
	
	@Override
	protected Void doInBackground() {
		int progress = 0;
		progressMonitor.setMillisToDecideToPopup(500);
		
		if (parent != null) {
			parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		if (src != null) {
			src.setEnabled(false);
		}
		
		for (Path p : decomFiles) {
			currentUpk = p.getFileName().toString();
			
			progressMonitor.setMillisToDecideToPopup(500);
			setProgress(0);
			
			proc = Main.extract(p);
			
			try (BufferedReader br = new BufferedReader(new InputStreamReader(proc
						.getInputStream()));) {
				
				String line = "";
				line = br.readLine();
				line = br.readLine();
				line = br.readLine();
				
				String[] numbers = line.split(" ")[1].split("/");
				Integer limit = Integer.parseInt(numbers[1]);
				
				int i = 0;
				
				while (line != "Done ..." && !isCancelled()) {
					if (line != null) {
						if (i % (limit / 1000) == 0 ) {
							numbers = line.split(" ")[1].split("/");
							Float n = (float) Integer.parseInt(numbers[0]);
							progress = (int) (n / limit * 100.0);
							
							setProgress(Math.min(progress, 100));	
						}
						char[] buffer = new char[512];
						
						if (br.ready()) {
							br.read(buffer);
							
							String test = new String(buffer);
							test += br.readLine();
							
							if (test.contains("Done ...")) {
								line = "Done ...";
								continue;
							}
						}
						// Main.print(XCMGUI.FACING_STREAM, line);
					} 
					line = br.readLine();
					i++;
				}
				if (!isCancelled()) {
					setProgress(100);
				} 				
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			Main.print(Main.MAIN, "EXTRACTED RESOURCES [", currentUpk, "]");
		}
		return null;
	}
	@Override
	protected void done() {
		try {
			if (parent != null) {
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
			if (src != null) {
				src.setEnabled(true);
			}		
			setProgress(100);
			if (progressMonitor.isCanceled()) {
				extractionCancelled();
			} else if (this.isDone()) {
				extractionComplete();		
			}	
			get();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace(System.err);
			extractionFailed();
			return;
		}	
	}
	
	/**
	 * Invoked when task's progress property changes.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			
			int progress = (int) evt.getNewValue();
			print ++;
			progressMonitor.setProgress(progress);
			
			String message = String.format("[%s] %d%% complete.\n", currentUpk, progress);
			progressMonitor.setNote(message);
			if (print % 10 == 0) {
				Main.print(message);
			}
			
			if (progressMonitor.isCanceled() || this.isDone()) {
				Toolkit.getDefaultToolkit().beep();
				
				if (progressMonitor.isCanceled()) {
					proc.destroy();
					this.cancel(true);
					Main.print("Extraction cancelled.\n");
					extractionCancelled();
				} else {
					Main.print("Extraction process finished.\n");
				}
				if (src != null) {
					src.setEnabled(true);
				}
			}
		}
		
	}
	protected void extractionFailed() {
		JOptionPane.showMessageDialog(Main.getFrame(),
					"There was an error extracting the resources.", "Gildor's Unreal Extractor",
					JOptionPane.ERROR_MESSAGE);
	}
	
	protected void extractionComplete() {
		JOptionPane.showMessageDialog(Main.getFrame(),
					"Extracting the resources has completed.", "Gildor's Unreal Extractor",
					JOptionPane.PLAIN_MESSAGE);
	}
	
	protected void extractionCancelled() {
		JOptionPane.showMessageDialog(Main.getFrame(),
					"Process stopped.", "Gildor's Unreal Extractor",
					JOptionPane.WARNING_MESSAGE);
	}
	
}
