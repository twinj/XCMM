package org.xcom.mod.gui.dialogues;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import org.xcom.mod.Main;
import org.xcom.mod.exceptions.XmlSaveException;
import org.xcom.mod.gui.XCMGUI;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.gui.workers.ExtractInBackGround;
import org.xcom.mod.pojos.Config;

import com.lipstikLF.LipstikLookAndFeel;
import com.lipstikLF.theme.DefaultTheme;
import com.lipstikLF.theme.KlearlooksTheme;
import com.lipstikLF.theme.LightGrayTheme;

public class SettingsDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private JTextField textField;
	private JTextField textFieldInstall;
	private JTextField textFieldUnpacked;
	
	private final ButtonGroup themeButtonGroup = new ButtonGroup();
	private JTextField textField_1;
	private JTextField textField_2;
	
	private SettingsDialog THIS = this;
	
	public SettingsDialog(final Config config) {
		setMinimumSize(new Dimension(275, 275));
		setResizable(false);
		
		initUI();
		
		textField.setText(config.getAuthor());
		textFieldInstall.setText(config.getXcomPath());
		textFieldUnpacked.setText(config.getUnpackedPath());
		
		JPanel toolsTab = new JPanel();
		getContentPane().add(toolsTab);
		toolsTab.setRequestFocusEnabled(false);
		toolsTab.setVisible(false);
		toolsTab.setEnabled(false);
		toolsTab.setLayout(null);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBounds(28, 5, 207, 23);
		horizontalBox.setMinimumSize(new Dimension(235, 0));
		horizontalBox.setAlignmentX(0.0f);
		toolsTab.add(horizontalBox);
		
		JLabel lblUnrealPackageDecompresor = new JLabel(
				"Unreal Package Decompresor");
		lblUnrealPackageDecompresor.setHorizontalAlignment(SwingConstants.LEFT);
		horizontalBox.add(lblUnrealPackageDecompresor);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue_2);
		
		JButton button = new JButton("Browse");
		button.setAlignmentX(1.0f);
		horizontalBox.add(button);
		
		textField_1 = new JTextField();
		textField_1.setBounds(28, 33, 207, 20);
		textField_1.setText((String) null);
		textField_1.setColumns(10);
		toolsTab.add(textField_1);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setMinimumSize(new Dimension(235, 0));
		horizontalBox_1.setAlignmentX(0.0f);
		horizontalBox_1.setBounds(28, 60, 207, 23);
		toolsTab.add(horizontalBox_1);
		
		JLabel lblUnrealPackageExtractor = new JLabel("Unreal Package Extractor");
		lblUnrealPackageExtractor.setHorizontalAlignment(SwingConstants.LEFT);
		horizontalBox_1.add(lblUnrealPackageExtractor);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_3);
		
		JButton button_1 = new JButton("Browse");
		button_1.setAlignmentX(1.0f);
		horizontalBox_1.add(button_1);
		
		textField_2 = new JTextField();
		textField_2.setText((String) null);
		textField_2.setColumns(10);
		textField_2.setBounds(28, 88, 207, 20);
		toolsTab.add(textField_2);
		
		JPanel themeTab = new JPanel();
		themeTab.setEnabled(false);
		themeTab.setVisible(false);
		getContentPane().add(themeTab);
		themeTab.setBorder(new TitledBorder(null, "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("System default");
		rdbtnNewRadioButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException | UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				XCMGUI.getFrame().repaint();
				THIS.repaint();
				XCMGUI.getFrame().pack();
			}
			
		});
		themeButtonGroup.add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("Lipstik - Default");
		rdbtnNewRadioButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException | UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				LipstikLookAndFeel.setMyCurrentTheme(new DefaultTheme());
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				XCMGUI.getFrame().repaint();
				THIS.repaint();
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton_2);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Lipstik - Light grey");
		rdbtnNewRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException | UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				LipstikLookAndFeel.setMyCurrentTheme(new LightGrayTheme());
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				XCMGUI.getFrame().repaint();
				THIS.repaint();
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnNewRadioButton_3 = new JRadioButton(
				"Lipstik - Klearlooks");
		rdbtnNewRadioButton_3.setSelected(true);
		rdbtnNewRadioButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException | UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				LipstikLookAndFeel.setMyCurrentTheme(new KlearlooksTheme());
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				XCMGUI.getFrame().repaint();
				THIS.repaint();
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
		gl_themeTab.setHorizontalGroup(gl_themeTab.createParallelGroup(
				Alignment.TRAILING).addGroup(
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
																		.createParallelGroup(Alignment.LEADING)
																		.addComponent(rdbtnNewRadioButton_1)
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
																						.addGap(72))).addGap(72))
										.addGroup(
												Alignment.TRAILING,
												gl_themeTab.createSequentialGroup()
														.addComponent(btnNewButton).addContainerGap()))));
		gl_themeTab.setVerticalGroup(gl_themeTab.createParallelGroup(
				Alignment.LEADING).addGroup(
				Alignment.TRAILING,
				gl_themeTab.createSequentialGroup()
						.addContainerGap(42, Short.MAX_VALUE)
						.addComponent(rdbtnNewRadioButton_1)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(rdbtnNewRadioButton_2)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(rdbtnNewRadioButton_3)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(rdbtnNewRadioButton).addGap(46)
						.addComponent(btnNewButton).addGap(21)));
		themeTab.setLayout(gl_themeTab);
	}
	
	public final void initUI() {
		
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("XCMM Config Settings");
		setLocationRelativeTo(XCMGUI.getFrame());
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane);
		
		JPanel generalTab = new JPanel();
		tabbedPane.addTab("General", null, generalTab, null);
		generalTab.setPreferredSize(new Dimension(400, 300));
		generalTab.setBorder(new TitledBorder(UIManager
				.getBorder("TitledBorder.border"), "", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		generalTab.setLayout(null);
		
		JButton close = new JButton("Cancel");
		close.setBounds(167, 183, 89, 23);
		generalTab.add(close);
		close.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		close.setAlignmentX(0.5f);
		
		Box getAuthorHBox = Box.createHorizontalBox();
		getAuthorHBox.setBounds(10, 11, 246, 20);
		generalTab.add(getAuthorHBox);
		
		JLabel lblYourAuthorName = new JLabel("Your author name:");
		getAuthorHBox.add(lblYourAuthorName);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		getAuthorHBox.add(horizontalStrut);
		
		textField = new JTextField();
		lblYourAuthorName.setLabelFor(textField);
		getAuthorHBox.add(textField);
		textField.setColumns(10);
		
		JButton btnSaveSettings = new JButton("Save");
		btnSaveSettings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComponent src = (JComponent) e.getSource();
				
				final Config config = XCMGUI.getConfig();
				
				final String name = textField.getText();
				final String unpackedPath = textFieldUnpacked.getText();
				final String xComPath = textFieldInstall.getText();
				
				final Path cookedCore = Paths.get(config.getCookedPath().toString(),
						"Core.upk");
				
				Boolean isOk = true;
				
				String msg = "ERROR";
				String title = "Incorrect setting.";
				int op = JOptionPane.ERROR_MESSAGE;
				
				if (name.isEmpty() || unpackedPath.isEmpty() || xComPath.isEmpty()) {
					msg = "Setting field cannot be empty.";
					
				} else if (!XCMGUI.isXComPathValid(xComPath)) {
					isOk = false;
					msg = "The system cannot verify your XCOM installtion.";
					
				} else if (name.equals("unknown")) {
					isOk = false;
					msg = "Name field cannot be 'unknown'.";
					
				} else if (!Main.isUnPackedPathValid(unpackedPath)) {
					
					if (Files.isDirectory(Paths.get(unpackedPath))) {
						isOk = false;
						try {
							if (!Main.isDecompressed(cookedCore)) {
								int n = JOptionPane.showConfirmDialog(
										XCMGUI.getFrame(),
										"Would you like to unpack the core upk file here to verify the current path?",
										"Unpack Core.upk", JOptionPane.YES_NO_OPTION);
								
								Path decompress = Paths.get("tools\\decompress.exe");
								Path extract = Paths.get("tools\\extract.exe");
								
								switch (n) {
									case JOptionPane.YES_OPTION :
										if (Files.notExists(decompress) || Files.notExists(extract)) {
											getToolsToPerformInitialVerification(unpackedPath,
													cookedCore, src);
										} else {
											
											new DecompressInBackGround(cookedCore).execute();
											new ExtractInBackGround(Paths.get(unpackedPath,
													"Core.upk")).execute();
										}
										break;
								}
								msg = null;
								isOk = true;
							} else {
								msg = "Your Core.upk is already decompressed. Unpacking now.";
								
								new ExtractInBackGround(Paths.get(unpackedPath, "Core.upk"))
										.execute();
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
					
					config.setAuthor(name);
					config.setUnpackedPath(unpackedPath);
					config.setXcomPath(xComPath);
					
					try {
						Main.saveXml(config);
					} catch (XmlSaveException ex) {
						msg = "There was an error saving the settings file. Try again. Check for OS permissions.";
						title = "Settings not saved.";
						ex.printStackTrace(System.err);
					}
					dispose();
				}
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
			public void getToolsToPerformInitialVerification(
					final String unpackedPath, final Path cookedCore, JComponent src)
					throws HeadlessException, MalformedURLException {
				
				final Path decompress = Paths.get("tools\\decompress.exe");
				final Path extract = Paths.get("tools\\extract.exe");
				final Path upkToExtract = Paths.get(unpackedPath, "Core.upk");
				
				int n1 = JOptionPane.showConfirmDialog(
						XCMGUI.getFrame(),
						"To get started you need to download and unzip Gildor's tools.\nAfter the download the system will try the tools.",
						"Download Gildor's tools?", JOptionPane.YES_NO_OPTION);
				
				switch (n1) {
					case JOptionPane.YES_OPTION :
						Main.openDesktopBrowser("http://www.gildor.org/");
						XCMGUI.decompressWithToolCheck(cookedCore, decompress);
						XCMGUI.extractWithToolCheck(upkToExtract, extract, null);
						break;
				} // end second switch
			} // end first switch
			
		});
		btnSaveSettings.setBounds(10, 183, 89, 23);
		generalTab.add(btnSaveSettings);
		
		textFieldInstall = new JTextField();
		textFieldInstall.setBounds(10, 80, 246, 20);
		generalTab.add(textFieldInstall);
		textFieldInstall.setColumns(10);
		
		Box horizontalXCOMBrowse = Box.createHorizontalBox();
		horizontalXCOMBrowse.setBounds(10, 55, 246, 23);
		generalTab.add(horizontalXCOMBrowse);
		horizontalXCOMBrowse.setMinimumSize(new Dimension(235, 0));
		horizontalXCOMBrowse.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel lblNewLabel = new JLabel("XCOM installation path:");
		horizontalXCOMBrowse.add(lblNewLabel);
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setLabelFor(textFieldInstall);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalXCOMBrowse.add(horizontalGlue);
		
		JButton browseButton2 = new JButton("Browse");
		browseButton2.setAlignmentX(Component.RIGHT_ALIGNMENT);
		horizontalXCOMBrowse.add(browseButton2);
		
		Box horizontalUnpackedBrowse = Box.createHorizontalBox();
		horizontalUnpackedBrowse.setBounds(10, 124, 246, 23);
		generalTab.add(horizontalUnpackedBrowse);
		
		JLabel lblUnpackedGameResources = new JLabel("Unpacked game resources:");
		lblUnpackedGameResources.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalUnpackedBrowse.add(lblUnpackedGameResources);
		lblUnpackedGameResources.setHorizontalTextPosition(SwingConstants.LEFT);
		lblUnpackedGameResources.setHorizontalAlignment(SwingConstants.LEFT);
		
		textFieldUnpacked = new JTextField();
		textFieldUnpacked.setBounds(10, 152, 246, 20);
		generalTab.add(textFieldUnpacked);
		textFieldUnpacked.setColumns(10);
		lblUnpackedGameResources.setLabelFor(textFieldUnpacked);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalUnpackedBrowse.add(horizontalGlue_1);
		
		JButton browseButton = new JButton("Browse");
		horizontalUnpackedBrowse.add(browseButton);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 42, 246, 20);
		generalTab.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 111, 246, 20);
		generalTab.add(separator_1);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(new java.io.File(textFieldUnpacked.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				int returnVal = fc.showOpenDialog(XCMGUI.getFrame());
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					textFieldUnpacked.setText(file.getAbsolutePath());
				}
			}
		});
		browseButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(new java.io.File(textFieldInstall.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				int returnVal = fc.showOpenDialog(XCMGUI.getFrame());
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					textFieldInstall.setText(file.getAbsolutePath());
				}
			}
		});
	}
}
