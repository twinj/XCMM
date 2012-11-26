package org.xcom.mod.gui.workers;

import java.awt.Component;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import org.xcom.mod.Main;

public abstract class RunInBackground extends SwingWorker<Main, Void> implements
            PropertyChangeListener {
    
    protected Main main;
    private ProgressMonitor progressMonitor;
    private SyncProgress sync = new SyncProgress();
    
    public RunInBackground(Component parent, Main main, String workMessage) {
        
        this.main = main;
        main.setSync(this);
        progressMonitor = new ProgressMonitor(parent, workMessage, "", 0, 100);
        setProgress(0);
    }
    
    @Override
    public Main doInBackground() {
        
        Random random = new Random();
        Thread joinable = new Thread(main);
        
        joinable.start();
        
        while (sync.getProgress() < 100 && !main.getDone()) {
            // Sleep for up to one second.
            setProgress(Math.min(sync.getProgress(), 100));
            try {
                Thread.sleep(random.nextInt(800));
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
        // try {
        // joinable.join();
        // } catch (InterruptedException e) {
        // e.printStackTrace(System.err);
        // }
        if (main.getError() != Main.Error.NOTHING) {
            progressMonitor.close();
            return null;
        }
        // while (sync.getProgress() < 100) {
        // // Sleep for up to one second.
        // sync.plusProgress(1);
        // setProgress(Math.min(sync.getProgress(), 100));
        // try {
        // Thread.sleep(random.nextInt(300));
        // } catch (InterruptedException ignore) {
        // }
        // }
        if (sync.getProgress() < 100) {
            // Sleep for up to one second.
            sync.plusProgress(100);
            setProgress(Math.min(sync.getProgress(), 100));
        }
        return null;
    }
    
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
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    this.cancel(true);
                    Main.print("Task canceled.\n");
                } else Main.print("Task completed.\n");
            }
        }
    }
}
