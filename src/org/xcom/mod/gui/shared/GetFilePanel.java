package org.xcom.mod.gui.shared;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.xcom.main.shared.Main;

public class GetFilePanel extends JPanel {
	
	private static final long serialVersionUID = -735880124320827434L;
	
	private final JTextField textField;
	private final Box horizontal;
	private final JLabel lbl;
	private final JButton browseButton;
	private Box verticalBox;
	private Component verticalStrut;
	private Component verticalGlue;
	
	public GetFilePanel(final String label, final File openPath, final int selectMode) {
		this.setBorder(null);
		setLayout(new BorderLayout(0, 0));
		
		verticalBox = Box.createVerticalBox();
		verticalBox.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(verticalBox, BorderLayout.NORTH);
		lbl = new JLabel(label);

		textField = new JTextField();
		verticalBox.add(textField);
		textField.setColumns(10);
		
		verticalStrut = Box.createVerticalStrut(20);
		verticalStrut.setPreferredSize(new Dimension(0, 5));
		verticalStrut.setMinimumSize(new Dimension(0, 5));
		verticalBox.add(verticalStrut);
		lbl.setLabelFor(textField);
		
		horizontal = Box.createHorizontalBox();
		verticalBox.add(horizontal);
		horizontal.setMinimumSize(new Dimension(235, 0));
		horizontal.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		horizontal.add(lbl);
		lbl.setHorizontalAlignment(SwingConstants.LEFT);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontal.add(horizontalGlue);
		
		browseButton = new JButton("Browse");
		browseButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		horizontal.add(browseButton);
		
		verticalGlue = Box.createVerticalGlue();
		verticalBox.add(verticalGlue);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(openPath == null ? new java.io.File(textField.getText())
							: openPath);
				fc.setFileSelectionMode(selectMode);
				
				int returnVal = fc.showOpenDialog(Main.getFrame());
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					textField.setText(file.getAbsolutePath());
				}
			}
		});
	}
	
	public JTextField getTextField() {
		return this.textField;
	}
	
	public Box getHorizontal() {
		return this.horizontal;
	}
	
	public JLabel getLbl() {
		return this.lbl;
	}
	
	public JButton getBrowseButton() {
		return this.browseButton;
	}
}
