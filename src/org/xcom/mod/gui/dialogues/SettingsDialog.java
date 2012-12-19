package org.xcom.mod.gui.dialogues;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import org.xcom.main.shared.Main;
import org.xcom.main.shared.XmlSaveException;
import org.xcom.main.shared.entities.Config;
import org.xcom.mod.gui.XCMGUI;
import org.xcom.mod.gui.shared.GetFilePanel;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.gui.workers.ExtractInBackGround;

import com.lipstikLF.LipstikLookAndFeel;
import com.lipstikLF.theme.DefaultTheme;
import com.lipstikLF.theme.KlearlooksTheme;
import com.lipstikLF.theme.LightGrayTheme;

public class SettingsDialog extends JDialog {
	
	private static final String HTTP_WWW_GILDOR_ORG = "http://www.gildor.org/";

	private static final long serialVersionUID = 1L;
	private JTextField textFieldName;
	
	private final ButtonGroup themeButtonGroup = new ButtonGroup();
	
	private SettingsDialog THIS = this;
	private JButton close;
	private GetFilePanel xcomInstallPanel;
	private GetFilePanel unpackedPathPanel;
	private GetFilePanel decompressorPanel;
	private GetFilePanel extractorPanel;
	
	public SettingsDialog(final Config config) {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				textFieldName.setText(config.getAuthor());
				xcomInstallPanel.getTextField().setText(config.getXcomPath());
				unpackedPathPanel.getTextField().setText(config.getUnpackedPath());
				decompressorPanel.getTextField().setText(config.getCompressorPath());
				extractorPanel.getTextField().setText(config.getExtractorPath());
				setLocationRelativeTo(XCMGUI.getFrame());
			}
		});
		setMinimumSize(new Dimension(275, 275));
		setResizable(false);
		
		initUI();
		
	}
	
	public final void initUI() {
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("XCMM Config Settings");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane);
		
		JPanel generalTab = new JPanel();
		tabbedPane.addTab("General", null, generalTab, null);
		generalTab.setPreferredSize(new Dimension(400, 300));
		generalTab.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		generalTab.setLayout(null);
		
		Box getAuthorHBox = Box.createHorizontalBox();
		getAuthorHBox.setBounds(4, 14, 255, 20);
		generalTab.add(getAuthorHBox);
		
		JLabel lblYourAuthorName = new JLabel("Your author name:");
		getAuthorHBox.add(lblYourAuthorName);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		getAuthorHBox.add(horizontalStrut);
		
		textFieldName = new JTextField();
		lblYourAuthorName.setLabelFor(textFieldName);
		getAuthorHBox.add(textFieldName);
		textFieldName.setColumns(10);
		
		xcomInstallPanel = new GetFilePanel("XCOM installation path:", null,
					JFileChooser.DIRECTORIES_ONLY);
		xcomInstallPanel.setPreferredSize(new Dimension(240, 50));
		xcomInstallPanel.setMinimumSize(new Dimension(240, 50));
		
		xcomInstallPanel.setBounds(4, 50, 255, 50);
		generalTab.add(xcomInstallPanel);
		
		unpackedPathPanel = new GetFilePanel("Unpacked game resources:", null,
							JFileChooser.DIRECTORIES_ONLY);
		unpackedPathPanel.setPreferredSize(new Dimension(240, 50));
		unpackedPathPanel.setMinimumSize(new Dimension(240, 50));
		unpackedPathPanel.setSize(255, 50);
		unpackedPathPanel.setLocation(4, 110);
				
		generalTab.add(unpackedPathPanel);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBounds(4, 170, 255, 25);
		generalTab.add(horizontalBox);
		
		JButton btnSaveSettings = new JButton("Save");
		btnSaveSettings.setPreferredSize(new Dimension(67, 23));
		btnSaveSettings.setMinimumSize(new Dimension(67, 23));
		btnSaveSettings.setMaximumSize(new Dimension(67, 23));
		horizontalBox.add(btnSaveSettings);
		btnSaveSettings.addActionListener(new SaveAction());
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue);
		
		close = new JButton("Cancel");
		close.setPreferredSize(new Dimension(67, 23));
		close.setMinimumSize(new Dimension(67, 23));
		close.setMaximumSize(new Dimension(67, 23));
		horizontalBox.add(close);
		close.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		close.setAlignmentX(0.5f);
		
		JPanel toolsTab = new JPanel();
		tabbedPane.addTab("Gildor's Tools", null, toolsTab, null);
		toolsTab.setRequestFocusEnabled(false);
		toolsTab.setVisible(false);
		toolsTab.setEnabled(false);
		toolsTab.setLayout(null);

		decompressorPanel = new GetFilePanel("Unreal Package Decompresor:", new java.io.File(Main.USER_DIR), JFileChooser.FILES_ONLY );
		decompressorPanel.setPreferredSize(new Dimension(255, 50));
		decompressorPanel.setBounds(4, 50, 255, 50);
		decompressorPanel.setMinimumSize(new Dimension(255, 50));
		decompressorPanel.setAlignmentX(0.0f);
		toolsTab.add(decompressorPanel);
		
		extractorPanel = new GetFilePanel("Unreal Package Extractor:", new java.io.File(Main.USER_DIR), JFileChooser.FILES_ONLY );
		extractorPanel.setPreferredSize(new Dimension(255, 50));
		extractorPanel.setMinimumSize(new Dimension(250, 50));
		extractorPanel.setAlignmentX(0.0f);
		extractorPanel.setBounds(4, 110, 255, 50);
		toolsTab.add(extractorPanel);
	
		JButton button_2 = new JButton("Save");
		button_2.setMaximumSize(new Dimension(67, 23));
		button_2.setMinimumSize(new Dimension(67, 23));
		button_2.setPreferredSize(new Dimension(67, 23));
		button_2.addActionListener(new SaveAction());
		button_2.setBounds(4, 170, 67, 23);
		toolsTab.add(button_2);
		
		JPanel themeTab = new JPanel();
		tabbedPane.addTab("Themes", null, themeTab, null);
		tabbedPane.setEnabledAt(2, false);
		themeTab.setVisible(false);
		themeTab.setEnabled(false);
		themeTab.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP,
					null, null));
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("System default");
		rdbtnNewRadioButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				XCMGUI.getFrame().pack();
			}
			
		});
		themeButtonGroup.add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("Lipstik - Default");
		rdbtnNewRadioButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				LipstikLookAndFeel.setMyCurrentTheme(new DefaultTheme());
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton_2);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Lipstik - Light grey");
		rdbtnNewRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				LipstikLookAndFeel.setMyCurrentTheme(new LightGrayTheme());
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("Lipstik - Klearlooks");
		rdbtnNewRadioButton_3.setSelected(true);
		rdbtnNewRadioButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				LipstikLookAndFeel.setMyCurrentTheme(new KlearlooksTheme());
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton_3);
		
		JButton btnNewButton = new JButton("Ok");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GroupLayout gl_themeTab = new GroupLayout(themeTab);
		gl_themeTab
					.setHorizontalGroup(gl_themeTab
								.createParallelGroup(Alignment.TRAILING)
								.addGroup(
											gl_themeTab
														.createSequentialGroup()
														.addContainerGap(6, Short.MAX_VALUE)
														.addGroup(
																	gl_themeTab
																				.createParallelGroup(Alignment.LEADING)
																				.addGroup(
																							gl_themeTab
																										.createSequentialGroup()
																										.addGroup(
																													gl_themeTab
																																.createParallelGroup(
																																			Alignment.LEADING)
																																.addComponent(
																																			rdbtnNewRadioButton_1)
																																.addGroup(
																																			gl_themeTab
																																						.createSequentialGroup()
																																						.addGroup(
																																									gl_themeTab
																																												.createParallelGroup(
																																															Alignment.LEADING)
																																												.addComponent(
																																															rdbtnNewRadioButton_3)
																																												.addComponent(
																																															rdbtnNewRadioButton_2)
																																												.addComponent(
																																															rdbtnNewRadioButton))
																																						.addGap(72)))
																										.addGap(72)).addGroup(
																							Alignment.TRAILING,
																							gl_themeTab.createSequentialGroup()
																										.addComponent(btnNewButton)
																										.addContainerGap()))));
		gl_themeTab.setVerticalGroup(gl_themeTab.createParallelGroup(Alignment.LEADING)
					.addGroup(
								Alignment.TRAILING,
								gl_themeTab.createSequentialGroup().addContainerGap(42, Short.MAX_VALUE)
											.addComponent(rdbtnNewRadioButton_1).addPreferredGap(
														ComponentPlacement.RELATED).addComponent(
														rdbtnNewRadioButton_2).addPreferredGap(
														ComponentPlacement.RELATED).addComponent(
														rdbtnNewRadioButton_3).addPreferredGap(
														ComponentPlacement.RELATED).addComponent(rdbtnNewRadioButton)
											.addGap(46).addComponent(btnNewButton).addGap(21)));
		themeTab.setLayout(gl_themeTab);
	}
	/**
	 * Save action
	 * 
	 * @author Daniel Kemp
	 * 
	 */
	class SaveAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComponent src = (JButton) e.getSource();
			
			final Config config = XCMGUI.getConfig();
			
			final String name = textFieldName.getText();
			final String unpackedPath = unpackedPathPanel.getTextField().getText();
			final String xComPath = xcomInstallPanel.getTextField().getText();
			final String compressorPath = decompressorPanel.getTextField().getText();
			final String extractorPath = extractorPanel.getTextField().getText();
			
			Boolean isOk = true;
			
			String msg = "ERROR";
			String title = "Incorrect setting.";
			int op = JOptionPane.ERROR_MESSAGE;
			
			boolean xPathValid = XCMGUI.isXComPathValid(xComPath);
			
			if (xPathValid) {
				config.setXcomPath(xComPath);
			}
			if (Files.exists(Paths.get(compressorPath))) {
				config.setCompressorPath(compressorPath);
			}
			if (Files.exists(Paths.get(extractorPath))) {
				config.setExtractorPath(extractorPath);
			}
			
			config.setAuthor(name);
			config.setUnpackedPath(unpackedPath);
			
			final Path cookedCore = Paths.get(config.getCookedPath().toString(), "Core.upk");
			
			if (name.isEmpty() || unpackedPath.isEmpty() || xComPath.isEmpty()) {
				isOk = false;
				msg = "Setting field cannot be empty.";
				
			} else if (!xPathValid) {
				isOk = false;
				msg = "The system cannot verify your XCOM installtion.";
				
			} else if (name.equals("unknown")) {
				isOk = false;
				msg = "Name field cannot be 'unknown'.";
				
			} else if (Files.notExists(Paths.get(compressorPath))
						|| Files.notExists(Paths.get(extractorPath))) {
				
				boolean yes = false;
				
				try {
					yes = getToolsToPerformInitialVerification(unpackedPath, cookedCore, src);
				} catch (HeadlessException | MalformedURLException ignore) {
					ignore.printStackTrace(System.err);
				}
				if (!yes) {
					msg = "You need to select a valid path for Gildor's tools.";
				} else {
					msg = "The settings have been updated.";
				}
			} else if (!Main.isUnPackedPathValid(unpackedPath)) {
				
				if (Files.isDirectory(Paths.get(unpackedPath))) {
					isOk = false;
					try {
						if (!Main.isDecompressed(cookedCore)) {
							int n = JOptionPane
										.showConfirmDialog(
													THIS,
													"Would you like to unpack the core upk file here to verify the current path?",
													"Unpack Core.upk", JOptionPane.YES_NO_OPTION);
							
							switch (n) {
								case JOptionPane.YES_OPTION :
									DecompressInBackGround dib = new DecompressInBackGround(cookedCore);
									
									dib.execute();
									try {
										dib.get();
									} catch (InterruptedException | ExecutionException ignore) {
										ignore.printStackTrace(); // already handled
										break;
									}
									new ExtractInBackGround(cookedCore, THIS).execute();
									
									break;
							}
							msg = null;
							isOk = true;
						} else {
							msg = "Your Core.upk is already decompressed. Unpacking now.";
							isOk = true;
							new ExtractInBackGround(cookedCore, THIS).execute();
						}
					} catch (HeadlessException | IOException ignore) {
						ignore.printStackTrace(System.err);
					}
					
				} else {
					isOk = false;
					msg = "The system cannot verify your unpacked resources. Please choose a valid path.";
				}
				
				if (!isOk) {
					msg = "The system cannot verify your unpacked resources.";
				}
			} else if (isOk) { // Is Ok
			
				msg = "The settings have been updated.";
				title = "Settings saved.";
				op = JOptionPane.PLAIN_MESSAGE;
			}
						
			try {
				Main.saveXml(config);
			} catch (XmlSaveException ex) {
				msg = "There was an error saving the settings file. Try again. Check for OS permissions.";
				title = "Settings not saved.";
				ex.printStackTrace(System.err);
			}
			dispose();
			if (msg != null) {
				JOptionPane.showMessageDialog(getContentPane(), msg, title, op);
			}
		}
		/**
		 * 
		 * @param unpackedPath
		 * @param cookedCore
		 * @param src
		 * @throws HeadlessException
		 * @throws MalformedURLException
		 */
		public boolean getToolsToPerformInitialVerification(final String unpackedPath,
					final Path cookedCore, final JComponent src) throws HeadlessException,
					MalformedURLException {
			
			final Path decompress = Paths.get("tools\\decompress.exe");
			final Path extract = Paths.get("tools\\extract.exe");
			final Path upkToExtract = Paths.get(unpackedPath, "Core.upk");
			
			int n1 = JOptionPane
						.showConfirmDialog(
									THIS,
									"To get started you need to download Gildor's tools or select a working path.\nAfter the download the system will test to verify the tools.\n\nWould you like to download?",
									"Download Gildor's tools?", JOptionPane.YES_NO_OPTION);
			
			switch (n1) {
				case JOptionPane.YES_OPTION :
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							if (src != null) {
								src.setEnabled(false);
							}
							close.setEnabled(false);
							THIS.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							Main.openDesktopBrowser(HTTP_WWW_GILDOR_ORG);
							XCMGUI.decompressWithToolCheck(cookedCore, decompress, true);
							XCMGUI.extractWithToolCheck(upkToExtract, extract, null, true, THIS);
							
							return null;
						}
						@Override
						protected void done() {
							try {
								get();
							} catch (InterruptedException | ExecutionException ex) {
								ex.printStackTrace();
							}
							THIS.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							if (src != null) {
								src.setEnabled(true);
							}
							close.setEnabled(true);
						}
					}.execute();
					break;
				default :
					return false;
			}
			return true;
		}
	}
}
