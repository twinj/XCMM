package org.xcom.mod.gui.listeners;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.xcom.mod.Main;
import org.xcom.mod.tools.xshape.MHash;
import org.xcom.mod.tools.xshape.exceptions.CalculateHashException;



public abstract class GetHashButton implements ActionListener {
    
    private Component frame;

    public GetHashButton(Component frame) {
        this.frame = frame;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(getFile());
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDragEnabled(true);
        
        int returnVal = fc.showOpenDialog(frame);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            String message = null;
            try {
                message = "Hash [" + file.getName() + "] ["
                            + MHash.getHashPrintString(file.toPath()) + "]";
                Main.print(Main.MAIN, message, "");
            } catch (CalculateHashException e1) {
                message = "There was an error calculating the SHA hash.";
                e1.printStackTrace();
            }
            String title = "File Hash(es)";
            JOptionPane.showMessageDialog(frame, message, title,
                        JOptionPane.YES_NO_OPTION);
        } else {}
    }
    
    protected abstract File getFile();
}
