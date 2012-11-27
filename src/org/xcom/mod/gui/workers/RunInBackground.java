package org.xcom.mod.gui.workers;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.xcom.mod.Main;
import org.xcom.mod.Main.Error;

public abstract class RunInBackground<T> extends SwingWorker<T, Void>
		implements
			PropertyChangeListener {
	
	protected Main main;
	protected ProgressMonitor progressMonitor;
	protected SyncProgress sync = new SyncProgress();
	protected Random random = new Random();
	private Component parent;
	private JComponent src;
	
	public RunInBackground(Component parent, Main main, String workMessage, JComponent src) {
		
		this.main = main;
		// main.setSync(this);
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
		
		// Thread joinable = new Thread(main);
		// joinable.start();
		// setProgress(1);
		main.run();
		
		// while (sync.getProgress() < 100 && !main.getDone() && !isCancelled()) {
		// // Sleep for up to one second.
		// setProgress(Math.min(sync.getProgress(), 100));
		// Thread.sleep(random.nextInt(2000));
		// }
		return (T) main.getRet();
	}
	
	@Override
	protected void done() {
		Toolkit.getDefaultToolkit().beep();
		T ret = null;
		
		try {
			ret = get();
		} catch (InterruptedException | ExecutionException ex) {
			ex.printStackTrace(System.err);
		}
		
		Error e = main.getError();
		
		// setProgress(Math.min(100, 100));
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
