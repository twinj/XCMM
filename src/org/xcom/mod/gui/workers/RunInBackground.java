package org.xcom.mod.gui.workers;

import java.awt.Component;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.xcom.main.shared.Main;
import org.xcom.main.shared.Main.Error;

public abstract class RunInBackground<T> extends SwingWorker<T, Void>
			implements
				PropertyChangeListener {
	
	protected Main worker;
	protected ProgressMonitor progressMonitor;
	protected SyncProgress sync = new SyncProgress();
	protected Random random = new Random();
	private Component parent;
	private JComponent src;
	
	public RunInBackground(Component parent, Main main, String workMessage, JComponent src) {
		
		this.worker = main;
		this.parent = parent;
		this.src = src;
		progressMonitor = new ProgressMonitor(parent, workMessage, "", 0, 100);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T doInBackground() throws Exception {
		
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (src != null) {
			src.setEnabled(false);
		}
		worker.run();
		return (T) worker.getRet();
	}
	
	@Override
	protected void done() {
		T ret = null;
		//Toolkit.getDefaultToolkit().beep();
		
		try {
			ret = get();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace(System.err);
		}
		
		Error e = worker.getError();
		
		after(e, ret);
		parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if (src != null) {
			src.setEnabled(true);
		}
	}
	protected abstract void after(Error e, T ret);
	
	public class SyncProgress {
		
		int progress = 0;
		
		public synchronized int getProgress() {
			return progress;
		}
		public synchronized void plusProgress(int p) {
			progress += p;
		}
	}
	
	public SyncProgress getSync() {
		return sync;
	}
	
	public void setSync(SyncProgress sync) {
		this.sync = sync;
	}
	
	/**
	 * Invoked when task's progress property changes.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressMonitor.setProgress(progress);
			String message = String.format("Completed %d%%.\n", progress);
			progressMonitor.setNote(message);
			Main.print(message);
			
			if (progressMonitor.isCanceled() || this.isDone()) {
				// Toolkit.getDefaultToolkit().beep();
				if (progressMonitor.isCanceled()) {
					this.cancel(true);
				} else {
					progressMonitor.close();
				}
			}
		}
	}
}
