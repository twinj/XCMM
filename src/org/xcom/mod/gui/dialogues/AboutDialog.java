package org.xcom.mod.gui.dialogues;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.xcom.mod.gui.XCMGUI;


public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public AboutDialog() {
		setMinimumSize(new Dimension(355, 255));
		setResizable(false);
		setAlwaysOnTop(true);

		initUI();
	}

	public final void initUI() {

		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(400, 300));
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(panel);
		panel.setLayout(null);

		JButton close = new JButton("Close");
		close.setBounds(270, 184, 59, 23);
		panel.add(close);
		close.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});

		close.setAlignmentX(0.5f);

		JEditorPane editorPane = new JEditorPane();
		editorPane.setText("\u2606\u250C\u2500\u2510\u3000\u2500\u2510\u2606\r\n\u3000\u2502\u2592\u2502 /\u2592/\t\tVersion: 1.00\r\n\u3000\u2502\u2592\u2502/\u2592/\r\n\u3000\u2502\u2592 /\u2592/\u2500\u252C\u2500\u2510\tXCOM Mod Manager\r\n\u3000\u2502\u2592\u2502\u2592|\u2592\u2502\u2592\u2502\r\n\u250C\u2534\u2500\u2534\u2500\u2510-\u2518\u2500\u2518 \tBrought to you by: \r\n\u2502\u2592\u250C\u2500\u2500\u2518\u2592\u2592\u2592\u2502\tDaemonjax, dose206, twinj\r\n\u2514\u2510\u2592\u2592\u2592\u2592\u2592\u2592\u250C\u2518\r\n\u3000\u2514\u2510\u2592\u2592\u2592\u2592\u250C\u2518");
		editorPane.setEditable(false);
		editorPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		editorPane.setBounds(10, 21, 319, 152);
		panel.add(editorPane);


		setModalityType(ModalityType.APPLICATION_MODAL);

		setTitle("About XCMM");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(XCMGUI.getFrame());
	}
}